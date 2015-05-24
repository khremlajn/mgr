package mgr.jena.recommendation.userbased;

import java.util.HashMap;
import java.util.Map;

import mgr.jena.recommendation.stereotypebased.Stereotype;

public class UserNode {
	
	private Map<Long, ItemNode> itemNodes;
	private String id;
	
	public UserNode(String id)
	{
		this.id = id;
		itemNodes = new HashMap<Long, ItemNode>();
	}
	
	public String getUserID()
	{
		return this.id;
	}
	
	public Map<Long, ItemNode> getItems()
	{
		return itemNodes;
	}
	
	public void AddItem(ItemNode item)
	{
		itemNodes.put(item.getID(), item);
	}
	
	public ItemNode getItem(long id)
	{
		return itemNodes.get(id);
	}
	
	public ItemNode getItem(int number)
	{
		return (ItemNode)itemNodes.values().toArray()[number];
	}
	
	public int getItemsSize()
	{
		return itemNodes.size();
	}
	
	public double getUserSimilarity(UserNode u, int minSimilarNodes)
	{
		double similarity = 0.0;
		double numerator = 0.0;
		double denumerator1 = 0.0;
		double denumerator2 = 0.0;
		double avgX = this.getAverageMark();
		double avgY = u.getAverageMark();
		for(ItemNode x : this.itemNodes.values())
		{
			ItemNode y = u.getItem(x.getID());
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
	
	public double getAverageMark()
	{
		double average = 0.0;
		int count = 0;
		for(ItemNode in : itemNodes.values())
		{
			average += in.getMark();
			count ++;
		}
		return count > 0 ? average/count : 0;
	}
}
