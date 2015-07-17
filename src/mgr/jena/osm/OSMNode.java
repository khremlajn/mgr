package mgr.jena.osm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import mgr.jena.Utils.MapSorter;
import mgr.jena.recommendation.userbased.UserNode;

public class OSMNode 
{
	public long id;
	public double lat;
	public double lon;
	public String name;
	private Map<String,String> values;
	
	public String toString()
	{
		String val = "";
		val =  "id: " + id + "\nname: " + name + "\nlat: " + lat + "\nlon: " + lon +"\n";
		Iterator<Entry<String, String>> it = values.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, String> pair = it.next();
	        val += String.format("%s : %s \n", pair.getKey() , pair.getValue());
	    }
		return val;
		//return "id: " + id + " name: " + name + " lat: " + lat + " lon: " + lon; 
	}
	
	public OSMNode(String id, String name, String lat, String lon)
	{
		this.id = Long.parseLong(id);
		this.name = name;
		this.lat = Double.parseDouble(lat);
		this.lon = Double.parseDouble(lon);
		values = new HashMap<String, String>();
	}
	
	public void addValue(String key,String value)
	{
		values.put(key, value);
	}
	
	public String getValue(String key)
	{
		return values.get(key);
	}
	
	public Boolean containsKey(String key)
	{
		return this.values.containsKey(key);
	}
	
	public Boolean containsKeyValue(String key,String value)
	{
		if(this.values.containsKey(key))
		{
			if(this.values.get(key).equalsIgnoreCase(value))
			{
				return true;
			}
		}
		return false;
	}
	
	public String getValue(int place)
	{
		String value = "";
		try
		{
			value = values.values().toArray()[place].toString();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		return value;
	}
}
