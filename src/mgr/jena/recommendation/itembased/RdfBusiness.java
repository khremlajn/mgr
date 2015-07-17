package mgr.jena.recommendation.itembased;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

public class RdfBusiness {
	
	private Map<String,String> values;
	
	public RdfBusiness()
	{
		values = new HashMap<String, String>();
	}
	
	public Map<String,String> getValues()
	{
		return this.values;
	}
	
	public void addValue(String key,String value)
	{
		this.values.put(key, value);
	}
	
	public String getValue(String key)
	{
		return values.get(key);
	}
	
	public double calculateSimilarity(RdfBusiness poi, Map<String,Double> w)
	{
		double similarity = 0.0;
		Iterator<Entry<String , String>> it1 = this.values.entrySet().iterator(); 
		JaroWinkler algorithm = new JaroWinkler();
		while (it1.hasNext()) {
	        Map.Entry<String , String> pair1 = it1.next();
	        String val1 =  poi.getValue(pair1.getKey());
	        if(val1 != null && w.containsKey(pair1.getKey()))
	        { 
	        	similarity +=getSimilarity(val1, pair1.getValue(),algorithm) * w.get(pair1.getKey())/100.0;
	        }
        }
		if(similarity >1.0) similarity = 1.0;
		return similarity;
	}
	
	private double getSimilarity(String val1,String val2,JaroWinkler algorithm)
	{
		
		return algorithm.getSimilarity(val1, val2);
	}
	
}
