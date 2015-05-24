package mgr.jena.recommendation.userbased;

public class ItemNode {
	private long id;
	private double mark;
	
	public ItemNode(long id, double mark)
	{
		this.id = id;
		this.mark = mark;
	}
	
	public long getID()
	{
		return this.id;
	}
	
	public double getMark()
	{
		return this.mark;
	}
}
