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
		stereotype = new Stereotype("empty");
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
		stereotype = new Stereotype("empty");
		List<Double> marks = new ArrayList<Double>();
		List<Integer> counters = new ArrayList<Integer>();
		int size = StereotypeEnum.values().length;
		for(int i=0;i<size;i++)
		{
			marks.add(0.0);
			counters.add(0);
		}
		for(OSMReview x : this.itemReviews.values())
		{
			//amenities
			if(x.getNode().containsKeyValue("amenity", "restaurant"))
			{
				
			}
			else if(x.getNode().containsKeyValue("amenity", "fast_food"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				
			}
			else if(x.getNode().containsKeyValue("amenity", "cafe"))
			{
				int i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.restaurants_italian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "pub"))
			{
				int i = StereotypeEnum.party.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "bar"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.party.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "theatre"))
			{
				int i = StereotypeEnum.art.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "cinema"))
			{
				int i = StereotypeEnum.cinemas.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "nightclub"))
			{
				int i = StereotypeEnum.party.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "bbq"))
			{
				int i = StereotypeEnum.party.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "ice_cream"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "arts_centre"))
			{
				int i = StereotypeEnum.art.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("amenity", "theatre"))
			{
				int i = StereotypeEnum.art.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			
			//cuisines
			if(x.getNode().containsKeyValue("cuisine", "burger"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.restaurants_american.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "pizza"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.restaurants_italian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "american"))
			{
				int i = StereotypeEnum.restaurants_american.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "sandwich"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "chinese"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "coffee_shop"))
			{
				int i = StereotypeEnum.party.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "italian"))
			{
				int i = StereotypeEnum.restaurants_italian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "mexican"))
			{
				int i = StereotypeEnum.restaurants_american.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "indian"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "chicken"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "asian"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "ice_cream"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "fish_and_chips"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "thai"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "japanese"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "french"))
			{
				int i = StereotypeEnum.restaurants_european.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "sushi"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "regional"))
			{
				int i = StereotypeEnum.restaurants_international.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "steak_house"))
			{
				int i = StereotypeEnum.restaurants_american.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "greek"))
			{
				int i = StereotypeEnum.restaurants_mediterranean.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "vietnamese"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "seafood"))
			{
				int i = StereotypeEnum.restaurants_mediterranean.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "kebab"))
			{
				int i = StereotypeEnum.restaurants_fast_food.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "international"))
			{
				int i = StereotypeEnum.restaurants_international.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "breakfast"))
			{
				int i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "vegetarian"))
			{
				int i = StereotypeEnum.restaurants_vegetarian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "lebanese"))
			{
				int i = StereotypeEnum.restaurants_asian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "spanish"))
			{
				int i = StereotypeEnum.restaurants_european.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "coffee"))
			{
				int i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
				i = StereotypeEnum.restaurants_italian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("cuisine", "portuguese"))
			{
				int i = StereotypeEnum.restaurants_european.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			
			if(x.getNode().containsKey("diet:vegan"))
			{
				int i = StereotypeEnum.restaurants_vegetarian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("diet:vegetarian"))
			{
				int i = StereotypeEnum.restaurants_vegetarian.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("smoking"))
			{
				int i = StereotypeEnum.smoker.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("delivery"))
			{
				int i = StereotypeEnum.home_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("takeaway"))
			{
				int i = StereotypeEnum.home_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("outdoor_seating"))
			{
				int i = StereotypeEnum.outdoor_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("drive_through"))
			{
				int i = StereotypeEnum.home_eating.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("attraction") || x.getNode().containsKeyValue("tourism", "attraction"))
			{
				int i = StereotypeEnum.sightseeing.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("wifi"))
			{
				int i = StereotypeEnum.internet.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("internet_access"))
			{
				int i = StereotypeEnum.internet.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			if(x.getNode().containsKey("wheelchair"))
			{
				
			}
			
			//tourisms
			if(x.getNode().containsKeyValue("tourism", "hotel"))
			{
				int i = StereotypeEnum.hotels_expensive.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("tourism", "guest_house"))
			{
				int i = StereotypeEnum.hotels_cheap.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("tourism", "artwork"))
			{
				int i = StereotypeEnum.art.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("tourism", "museum"))
			{
				int i = StereotypeEnum.art.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("tourism", "motel"))
			{
				int i = StereotypeEnum.hotels_cheap.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("tourism", "hostel"))
			{
				int i = StereotypeEnum.hotels_cheap.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
			else if(x.getNode().containsKeyValue("tourism", "gallery"))
			{
				int i = StereotypeEnum.art.ordinal();
				counters.set(i, counters.get(i) + 1);
				marks.set(i, marks.get(i) + x.getDiscreteMark());
			}
		}
		return stereotype;
	}
	
	
	/*
	public Stereotype calculateStereotype()
	{
		stereotype = new Stereotype("empty");
		List<Double> marks = new ArrayList<Double>();
		List<Integer> counters = new ArrayList<Integer>();
		int size = StereotypeEnum.values().length;
		for(int i=0;i<size;i++)
		{
			marks.add(0.0);
			counters.add(0);
		}
		for(OSMReview x : this.itemReviews.values())
		{
			for(int i=0;i<size;i++)
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
		
		for(int i=0;i<size;i++)
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
			stereotype.setValue(i, mark);
			//stereotype.setValue(StereotypeEnum.values()[i], mark);
		}
		return stereotype;
	}*/
	
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
