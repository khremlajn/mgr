package mgr.jena.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mgr.jena.Utils.Utils;
import mgr.jena.recommendation.stereotypebased.Stereotype;
import mgr.jena.recommendation.stereotypebased.StereotypeEnum;
import mgr.jena.recommendation.userbased.ItemNode;
import mgr.jena.recommendation.userbased.UserNode;

public class OSMUser {
	private Map<Long, OSMReview> itemReviews;
	private Stereotype stereotype;
	private String id;
	
	public OSMUser(String id)
	{
		this.id = id;
		itemReviews = new HashMap<Long, OSMReview>();
		stereotype = new Stereotype();
	}
	
	public String getUserID()
	{
		return this.id;
	}
	
	public Stereotype getStereotype()
	{
		return this.stereotype;
	}
	
	public Map<Long, OSMReview> getReviews()
	{
		return itemReviews;
	}
	
	public void addReview(OSMReview item)
	{
		itemReviews.put(item.getNode().id, item);
	}
	
	public OSMReview getReview(long id)
	{
		return itemReviews.get(id);
	}
	
	public OSMReview getReview(int number)
	{
		return (OSMReview)itemReviews.values().toArray()[number];
	}
	
	public int getReviewsSize()
	{
		return itemReviews.size();
	}
	
	public double getAverageMark()
	{
		double average = 0.0;
		int count = 0;
		for(OSMReview in : itemReviews.values())
		{
			average += in.getMark();
			count ++;
		}
		return count > 0 ? average/count : 0;
	}
	
	public Stereotype calculateStereotype()
	{
		stereotype = new Stereotype();
		List<Double> marks = new ArrayList<Double>();
		List<Integer> counters = new ArrayList<Integer>();
		for(int i=0;i<StereotypeEnum.values().length;i++)
		{
			marks.add(0.0);
			counters.add(0);
		}
		for(OSMReview x : this.itemReviews.values())
		{
			for(int i=0;i<StereotypeEnum.values().length;i++)
			{
				String enum_name = StereotypeEnum.values()[i].name();
				if(enum_name.contains("amenity") || enum_name.contains("cuisine") || enum_name.contains("tourism"))
				{
					String[] tokens = enum_name.split("_");
					String key = tokens[0];
					String val = enum_name.replace(key + "_", "");
					if(x.getNode().containsKeyValue(key,val))
					{
						counters.set(i, counters.get(i) + 1);
						marks.set(i, marks.get(i) + x.getDiscreteMark());
					}
				}
				else if(enum_name.contains("travelman"))
				{
					for(OSMReview y : this.itemReviews.values())
					{
						if(x == y) continue;
						double distanceMiles = Utils.directDistance(x.getNode().lat, x.getNode().lon, y.getNode().lat, y.getNode().lon);
						if(distanceMiles >= 3.0 )
						{
							counters.set(i, counters.get(i) + 1);
							marks.set(i, marks.get(i) + distanceMiles);
						}
					}
					
					//bigger value if nodes are far from each other
					//lower value if nodes are close to each other
					
				}
				else
				{
					if(x.getNode().getValue(enum_name) != null && !x.getNode().getValue(enum_name).equalsIgnoreCase("no"))
					{
						counters.set(i, counters.get(i) + 1);
						marks.set(i, marks.get(i) + x.getDiscreteMark());
					}
				}
			}
		}
		for(int i=0;i<StereotypeEnum.values().length;i++)
		{
			double mark = 0.0;
			if(counters.get(i) != 0)
			{
				mark = marks.get(i) / counters.get(i);
			}
			
			if(StereotypeEnum.values()[i].name().contains("travelman"))
			{
				if(mark <= 10.0)
				{
					mark = -1.0;
				}
				else if(mark <= 30.0)
				{
					mark = -0.7;
				}
				else if(mark <= 50.0)
				{
					mark = -0.4;
				}
				else if(mark <= 70.0)
				{
					mark = -0.1;
				}
				else if(mark <= 90.0)
				{
					mark = 0.1;
				}
				else if(mark <= 110.0)
				{
					mark = 0.4;
				}
				else if(mark <= 130.0)
				{
					mark = 0.7;
				}
				else if(mark >= 150.0)
				{
					mark = 1.0;
				}
			}
			stereotype.setValue(StereotypeEnum.values()[i], mark);
		}
		return stereotype;
	}
	
	public double getUserSimilarity(OSMUser u, int minSimilarNodes)
	{
		double similarity = 0.0;
		double numerator = 0.0;
		double denumerator1 = 0.0;
		double denumerator2 = 0.0;
		double avgX = this.getAverageMark();
		double avgY = u.getAverageMark();
		for(OSMReview x : this.itemReviews.values())
		{
			OSMReview y = u.getReview(x.getNode().id);
			if(y != null)
			{
				minSimilarNodes --;
				double rx = x.getMark() - avgX;
				double ry = y.getMark() - avgY;
				
				numerator += rx * ry;
				denumerator1 += rx * rx;
				denumerator2 += ry * ry;
			}
		}
		double denumerator = Math.sqrt(denumerator1 * denumerator2);
		if(denumerator != 0)
		{
			similarity = numerator / denumerator;
		}
		return minSimilarNodes < 0 ? similarity : 0;
	}
}
