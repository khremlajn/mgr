package mgr.jena.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import mgr.jena.PostgresHelper;
import mgr.jena.Utils.Utils;

public class UsersDatabaseCreator {
	
	private int usersNumber = 1000;
	private int minNodes = 10;
	private int maxNodes = 100;
	private PostgresHelper ph;
	private int minNameLength = 7;
	private int maxNameLength = 20;
	public void generateDatabase(PostgresHelper ph)
	{
		this.ph = ph;
		//generate random database
		//create new users and them to database
		for(int i=0;i<usersNumber;i++)
		{
			int userID = createUser();
			int nodesCount = Utils.generateRandomInt(minNodes, maxNodes);
			createRatings(userID,nodesCount);
		}
	}
	
	private boolean createRatings(int userID , int nodesCount)
	{
		try {
			java.sql.ResultSet rs = ph.execQuery(String.format("Select id from nodes where random() < 0.1 limit %d",nodesCount));
			while(rs.next()) {
				int nodeID = rs.getInt(1);
				if(nodeID > 0)
				{
					Map<String, Object> values = new HashMap<String, Object>(); 
					values.put("user_id", userID);
					values.put("node_id", nodeID);
					int score = Utils.generateRandomInt(1, 5);
					values.put("score", score);
					ph.insert("user_nodes", values);
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private int createUser()
	{
		String name = Utils.generateRandomString(minNameLength, maxNameLength);
		Map<String, Object> values = new HashMap<String, Object>();  
		values.put("name", name);
		try {
			return ph.insert("users", values);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
}
