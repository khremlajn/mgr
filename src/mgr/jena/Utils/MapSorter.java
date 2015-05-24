package mgr.jena.Utils;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class MapSorter {
	public static Map sortByValue(Map unsortedMap) {
		Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
		sortedMap.putAll(unsortedMap);
		return sortedMap;
	}
	
	public static Map sortByKey(Map unsortedMap) {
		Map sortedMap = new TreeMap(new KeyComparator(unsortedMap));
		sortedMap.putAll(unsortedMap);
		return sortedMap;
	}

	static class ValueComparator implements Comparator {
		 
		Map map;
	 
		public ValueComparator(Map map) {
			this.map = map;
		}
	 
		public int compare(Object keyA, Object keyB) {
			Comparable valueA = (Comparable) map.get(keyA);
			Comparable valueB = (Comparable) map.get(keyB);
			int comp = valueB.compareTo(valueA);
			//return 0 will merge keys
			return comp != 0 ? comp : 1;
		}
	}
	
	static class KeyComparator implements Comparator {
		 
		Map map;
	 
		public KeyComparator(Map map) {
			this.map = map;
		}
	 
		public int compare(Object keyA, Object keyB) {
			Comparable valueA = (Comparable) keyA;
			Comparable valueB = (Comparable) keyB;
			int comp = - valueB.compareTo(valueA);
			//return 0 will merge keys
			return comp != 0 ? comp : -1;
		}
	}
}
