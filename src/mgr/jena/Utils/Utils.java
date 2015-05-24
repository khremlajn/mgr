package mgr.jena.Utils;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

public class Utils {
	
	
	private static double LOCAL_PI = 3.1415926535897932385; 

	private static double toRadians(double degrees) 
	{
	  double radians = degrees * LOCAL_PI / 180.0;
	  return radians;
	}

	public static double directDistance(double lat1, double lng1, double lat2, double lng2) 
	{
	  double earthRadius = 3958.75;
	  double dLat = toRadians(lat2-lat1);
	  double dLng = toRadians(lng2-lng1);
	  double a = Math.sin(dLat/2) * Math.sin(dLat/2) + 
	             Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * 
	             Math.sin(dLng/2) * Math.sin(dLng/2);
	  double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	  double dist = earthRadius * c;
	  return dist;
	  //double meterConversion = 1609.00;
	  //return dist * meterConversion;
	}
	
	public static String generateRandomString(int minLength, int maxLength)
	{
		String name = RandomStringUtils.randomAlphanumeric(maxLength).toLowerCase();
		Random r = new Random();
		int n = r.nextInt(maxLength);
		n += minLength;
		if(n > maxLength)
		{
			n = maxLength;
		}
		return name.substring(0,n);
	}
	
	public static int generateRandomInt(int min,int max)
	{
		Random r = new Random();
		int  n = r.nextInt(max);
		n += min;
		if( n > max)
		{
			n = max;
		}
		return n;
	}
}
