package mgr.jena.osm;

public class OSMReview {
	private OSMNode node;
	private double mark;
	
	public OSMReview(OSMNode node , double mark)
	{
		this.node = node;
		this.mark = mark;
	}
	
	public void setMark(double mark)
	{
		this.mark = mark;
	}
	
	public double getMark()
	{
		return this.mark;
	}
	
	public OSMNode getNode()
	{
		return node;
	}
	
	public double getDiscreteMark()
	{
		if(this.mark <= 1.0)
		{
			return -1.0;
		}
		else if(this.mark <=1.5)
		{
			return -0.7;
		}
		else if(this.mark <= 2)
		{
			return -0.3;
		}
		else if(this.mark <= 2.5)
		{
			return -0.1;
		}
		else if(this.mark <= 3.0)
		{
			return 0.2;
		}
		else if(this.mark <= 3.5)
		{
			return 0.4;
		}
		else if(this.mark <= 4.0)
		{
			return 0.6;
		}
		else if(this.mark <= 4.5)
		{
			return 0.8;
		}
		else
		{
			return 1.0;
		}
	} 
}
