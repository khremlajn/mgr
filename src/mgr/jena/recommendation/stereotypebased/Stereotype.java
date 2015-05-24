package mgr.jena.recommendation.stereotypebased;

import java.util.ArrayList;
import java.util.List;

public class Stereotype {
	
	private List<Double> values; 
	
	public Stereotype()
	{
		values = new ArrayList<Double>();
		//Set initial stereotype values on 0
		for(int i=0;i<StereotypeEnum.values().length;i++)
		{
			values.add(0.0);
		}
	}
	
	public void setValue(StereotypeEnum e, double val)
	{
		values.set(e.ordinal(), val);
	}
	
	public double getValue(StereotypeEnum e)
	{
		return values.get(e.ordinal());
	}
	
	public List<Double> getValues()
	{
		return values;
	}
	
}
