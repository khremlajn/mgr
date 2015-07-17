package mgr.jena.recommendation.stereotypebased;

import java.awt.Point;
import java.awt.geom.Point2D;
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

import org.encog.mathutil.matrices.Matrix;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodBubble;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodRBF;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodSingle;

import com.vividsolutions.jts.util.Assert;



public class StereotypeRecommender {
	
	SOMNetwork network;
	
	public StereotypeRecommender()
	{
		network = new SOMNetwork();
	}
	
	public String winner(OSMUser user)
	{
		int win = network.winner(user);
		return network.getStereotype(win);
	}
	
	public Map<OSMNode , Double> getRecommendations(String userID, Map<String, OSMUser> users)
	{
		Map<OSMNode , Double> recommendations = new HashMap<OSMNode, Double>();
		OSMUser user = users.get(userID);
		if(user != null)
		{
			//train network with users data
			network.train(users);
			//find users from the same winner node
			int winnerNode = network.winner(user);
			Iterator<Entry<String, OSMUser>> itUser = users.entrySet().iterator();
		    List<OSMUser> similarUsers = new ArrayList<OSMUser>();
		    
			while (itUser.hasNext()) {
		    	Map.Entry<String, OSMUser> pairUser = itUser.next();
		        if(pairUser.getValue() == user) continue;
		        int winner = network.winner(pairUser.getValue());
		        if(winnerNode == winner)
		        {
		        	similarUsers.add(pairUser.getValue());
		        }
		    }
			Map<OSMNode , Point2D> recommendedNodes = new HashMap<OSMNode, Point2D>();
			for(int i=0;i<similarUsers.size();i++)
			{
				OSMUser u = similarUsers.get(i);
				Map<Long, OSMReview> reviews =  u.getReviews();
				Iterator<Entry<Long, OSMReview>> it = reviews.entrySet().iterator();
				while (it.hasNext()) {
			    	Map.Entry<Long, OSMReview> pairUser = it.next();
			    	Point2D point = new Point2D.Double(1.0,pairUser.getValue().getMark());
			    	if(recommendedNodes.containsKey(pairUser.getValue().getNode()))
			    	{
			    		Point2D val = recommendedNodes.get(pairUser.getValue().getNode());
			    		point = new Point2D.Double(val.getX() + 1, val.getY() + pairUser.getValue().getMark());
			    	}
			    	recommendedNodes.put(pairUser.getValue().getNode(), point);
				}	
			}
			Iterator<Entry<OSMNode,Point2D>> it1 = recommendedNodes.entrySet().iterator();
			while (it1.hasNext()) {
		    	Map.Entry<OSMNode,Point2D> pair = it1.next();
		    	Point2D point = pair.getValue();
		    	double val = point.getY() / point.getX() + point.getX();
		    	//don't add nodes that were already seen by user
		    	if(!user.getReviews().containsKey(pair.getKey().id))
		    	{
		    		recommendations.put(pair.getKey(), val);
		    	}
			}
			recommendations = MapSorter.sortByValue(recommendations);
		}
		return recommendations;
	}
}
