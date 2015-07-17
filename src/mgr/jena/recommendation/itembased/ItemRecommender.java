package mgr.jena.recommendation.itembased;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import mgr.jena.Utils.MapSorter;
import mgr.jena.osm.OSMNode;
import mgr.jena.osm.OSMReview;
import mgr.jena.osm.OSMUser;

public class ItemRecommender {
	
	private Map<String,Double> weights;
	
	public ItemRecommender()
	{
		weights = new HashMap<String, Double>();
		weights.put("type", 20.0);
		weights.put("contributor", 1.0);
		weights.put("cuisine", 15.0);
		weights.put("addr%3Astreet",3.0);
		weights.put("addr%3Apostcode",3.0);
		weights.put("addr%3Acity",1.0);
		weights.put("wheelchair",10.0);
		weights.put("internet_access",5.0);
		weights.put("opening_hours",1.0);
		weights.put("addr%3Astate",1.0);
		weights.put("internet_access%3Afee",3.0);
		weights.put("smoking",10.0);
		weights.put("addr%3Acountry",1.0);
		weights.put("attraction",10.0);
		weights.put("operator",5.0);
		weights.put("takeaway",10.0);
		weights.put("capacity",5.0);
		weights.put("internet_access%3Aoperator",5.0);
		weights.put("artwork_type",10.0);
		weights.put("wifi",10.0);
		weights.put("outdoor_seating",10.0);
		weights.put("drive_through",10.0);
		weights.put("designation",10.0);
		weights.put("species",10.0);
		weights.put("delivery",10.0);
		weights.put("historic",10.0);
		weights.put("food",5.0);
		weights.put("gnis%3Areviewed",5.0);
		weights.put("payment%3Abitcoin",5.0);
		weights.put("shop",2.0);
		weights.put("stars",15.0);
		weights.put("diet%3Avegetarian",20.0);
		weights.put("artist_name",50.0);
		weights.put("drive_in",10.0);
		weights.put("microbrewery",15.0);
		weights.put("sport",10.0);
		weights.put("brand",15.0);
		weights.put("wheelchair%3Adescription",15.0);
		weights.put("is_in%3Acity",1.0);
		weights.put("is_in%3Acountry",1.0);
		weights.put("take_away",15.0);
		weights.put("is_in%3Astate_code",1.0);
		weights.put("real_ale",15.0);
		weights.put("diet%3Avegan",30.0);
		weights.put("tomb",10.0);
		weights.put("organic",15.0);
		weights.put("payment%3Acash",5.0);
		weights.put("payment%3Amastercard",5.0);
		weights.put("payment%3Avisa",5.0);
		weights.put("beer_garden",15.0);
		weights.put("man_made",5.0);
		weights.put("adult_entertainment",15.0);
		weights.put("payment%3Adebit_cards",5.0);
		weights.put("payment%3Aamerican_express",5.0);
		weights.put("payment%3Acredit_cards",15.0);
		weights.put("artwork",15.0);
		weights.put("postalCode",1.0);
		weights.put("entrance",2.0);
		weights.put("payment%3Acoins",10.0);
		weights.put("drink%3Aespresso",15.0);
		weights.put("highway",1.0);
		weights.put("natural",10.0);
		weights.put("cost%3Acoffee",5.0);
		weights.put("atm",3.0);
		weights.put("museum",5.0);
		weights.put("internet_access%3Assid",5.0);
		weights.put("national",10.0);
		weights.put("gay",15.0);
		weights.put("brewery",15.0);
		weights.put("club",5.0);
		weights.put("beds",20.0);
		weights.put("gst_number",15.0);
		weights.put("rooms",15.0);
		weights.put("payment%3Acheque",10.0);
		weights.put("payment%3Acheques",10.0);
		weights.put("children",15.0);
		weights.put("payment_type%3Acard",10.0);
		weights.put("dogs",15.0);
		weights.put("photo",10.0);
		weights.put("bar",10.0);
		weights.put("payment%3Avisa_debit",10.0);
		weights.put("real_fire",15.0);
	}
	
	public void calculateItemsSimilarity(Map<String,RdfBusiness> poiMap)
	{
		//Map<OSMNode , Double> recommendation = new HashMap<OSMNode, Double>();
		
		String[] nodeArray = new String[poiMap.size()];
		RdfBusiness[] poiArray = new RdfBusiness[poiMap.size()];
		int index = 0;
		for (Map.Entry<String, RdfBusiness> mapEntry : poiMap.entrySet()) {
			nodeArray[index] = mapEntry.getKey();
			poiArray[index] = mapEntry.getValue();
		    index++;
		}
		//String[] nodeArray = (String[]) poiMap.keySet().toArray();
		double[][] similarity = new double[poiArray.length][poiArray.length];
		long counter = 0;
		System.out.println("Calculate similarity");
		for(int x=0;x<poiArray.length;x++)
		{
			for(int y=0;y<poiArray.length;y++)
			{
				if(x == y )
				{
					similarity[x][y] = 0.0;
				}
				else
				{
					similarity[x][y] = poiArray[x].calculateSimilarity(poiArray[y], weights);
				}
			}
			counter ++;
			System.out.println(counter);
		}
		counter = 0;
		//Save in csv file
		FileWriter writer;
		try {
			writer = new FileWriter("NodesSimilarity.csv");
			for(int x=0;x<similarity.length;x++)
			{
				writer.append(nodeArray[x]);
				Map<String,Double> temp = new HashMap<String, Double>();
				for(int y=0;y<similarity.length;y++)
				{
					temp.put(nodeArray[y], similarity[x][y]);
					//writer.append(", " + similarity[x][y]);
				}
				temp = MapSorter.sortByValue(temp);
				Iterator<Entry<String , Double>> it1 = temp.entrySet().iterator();
				int c =0;
		        while (it1.hasNext()) {
			        Map.Entry<String , Double> pair1 = it1.next();
			        writer.append("," + pair1.getKey() + "," + pair1.getValue().toString());
			        c++;
			        if(c == 10) break;
		        }
				writer.append("\n");
				counter ++;
				System.out.println(counter);
			}
			writer.flush();
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<OSMNode , Double> getRecommendations(OSMUser user,Collection<OSMNode> nodes)
	{
		Map<OSMNode, Double> recommendations =  new HashMap<OSMNode, Double>();
		Iterator<OSMNode> it = nodes.iterator();
		HashMap<String, OSMNode> nodesMap = new HashMap<String, OSMNode>(); 
		while(it.hasNext())
		{
			OSMNode n = it.next();
			nodesMap.put(new Long(n.id).toString(), n);
		}
		try(BufferedReader br = new BufferedReader(new FileReader("NodesSimilarity.csv"))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        String[] tokens = line.split(",");
		        String nodeID = tokens[0].replaceAll("node", "");
		        if(user.getReviews().containsKey(Long.parseLong(nodeID)))
		        {
		        	for(int i=1;i+1<tokens.length;i+=2)
			        {
		        		tokens[i] = tokens[i].replaceAll("node", "");
		        		double d = Double.parseDouble(tokens[i+1]);
		        		OSMNode n = nodesMap.get(tokens[i]);        		
		        		if(!recommendations.containsKey(n) || recommendations.get(n) < d)
		        		{
		        			recommendations.put(n, d);
		        		}
			        }
		        }
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recommendations = MapSorter.sortByValue(recommendations);
		return recommendations;
	}
}
