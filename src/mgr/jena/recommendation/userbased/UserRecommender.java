package mgr.jena.recommendation.userbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;






import mgr.jena.Utils.MapSorter;
import mgr.jena.osm.OSMNode;
import mgr.jena.osm.OSMReview;
import mgr.jena.osm.OSMUser;

public class UserRecommender {
	
	private int minSimilarNodes = 7;
	private double minSimilarity = 0.7;
	
	private Map<OSMUser , Double> getSimilarUsers(String userID, Map<String, OSMUser> users)
	{
		Map<OSMUser , Double> userSimilarities = new HashMap<OSMUser, Double>();
		OSMUser user = users.get(userID);
		for(OSMUser u : users.values())
		{
			if(u.getUserID().equalsIgnoreCase(user.getUserID()))
			{
				continue;
			}
			userSimilarities.put(u, u.getUserSimilarity(user, minSimilarNodes));
		}
		Map<OSMUser , Double> similarUsers = MapSorter.sortByValue(userSimilarities);
		Iterator<Entry<OSMUser, Double>> it = similarUsers.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<OSMUser, Double> pair = it.next();
	        if(pair.getValue() < minSimilarity)
	        {
	        	it.remove(); // avoids a ConcurrentModificationException
	        }
	    }
		return similarUsers;
	}
	
	public Map<Long , Double> getRecommendations(String userID, Map<String, OSMUser> users)
	{
		OSMUser user = users.get(userID);
		double avgU = user.getAverageMark();
		Map<OSMUser , Double> similarUsers =  getSimilarUsers(userID, users);
		//find all items reviewed by similar users
		Map<Long , NodePoint> items = new HashMap<Long, NodePoint>();
		Iterator<Entry<OSMUser, Double>> itUser = similarUsers.entrySet().iterator();
	    while (itUser.hasNext()) {
	        Map.Entry<OSMUser, Double> pairUser = itUser.next();
	        Iterator<Entry<Long, OSMReview>> itItem = pairUser.getKey().getReviews().entrySet().iterator();
	        double avg = pairUser.getKey().getAverageMark();
	        double sim = pairUser.getValue();
	        while (itItem.hasNext()) {
	        	Map.Entry<Long, OSMReview> pairItem = itItem.next();
	        	double r = pairItem.getValue().getMark();
	        	r = r -  avg;
	        	r = r * sim;
	        	if(!items.containsKey(pairItem.getKey()))
	        	{
	        		items.put(pairItem.getKey(), new NodePoint(r,Math.abs(sim)));
	        	}
	        	else
	        	{
	        		NodePoint np = items.get(pairItem.getKey());
	        		r += np.getX();
	        		np.setY(np.getY() + Math.abs(sim));
	        		items.put(pairItem.getKey(), np);
	        	}
	        }
	    }
	    Map<Long, Double> recommendations = new HashMap<Long, Double>();
	    
	    Iterator<Entry<Long, NodePoint>> itItems = items.entrySet().iterator();
	    while (itItems.hasNext()) {
	        Map.Entry<Long, NodePoint> pairItem = itItems.next();
	        if(!user.getReviews().containsKey(pairItem.getKey()))
	        {
	        	//node is not reviewed by user
	        	NodePoint n = pairItem.getValue();
		        double rec = n.getX() / n.getY() + avgU;
		        recommendations.put(pairItem.getKey(), rec);
	        }
	    }
	    recommendations = MapSorter.sortByValue(recommendations);
		return recommendations;
	}
}
