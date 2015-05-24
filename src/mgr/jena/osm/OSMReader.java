package mgr.jena.osm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mgr.jena.Utils.MapSorter;
import mgr.jena.recommendation.userbased.UserNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OSMReader {
private String lgdFile = "mgr.osm";
	

	public void tagsCount()
	{
		List<OSMNode> osmList = new ArrayList<OSMNode>();
		File fXmlFile = new File(lgdFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			Element osm = doc.getDocumentElement();
			NodeList nList = osm.getElementsByTagName("node");
			Map<String,Integer> tagsMap = new HashMap<String, Integer>();
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					NodeList tags = eElement.getElementsByTagName("tag");
					for (int i = 0; i < tags.getLength(); i++) {
						Node n = tags.item(i);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element e = (Element) n;
							if(e.hasAttribute("k") && e.hasAttribute("v") && e.getAttribute("k").equalsIgnoreCase("tourism"))
							{
								String k = e.getAttribute("v").toLowerCase();
								if(!tagsMap.containsKey(k))
									tagsMap.put(k, 1);
								else
								{
									tagsMap.put(k, tagsMap.get(k) + 1);
								}
							}
						}
					}
				}
			}
			tagsMap = MapSorter.sortByValue(tagsMap);
			Iterator<Entry<String, Integer>> it = tagsMap.entrySet().iterator();
		    String s = "";
			while (it.hasNext()) {
		        Map.Entry<String, Integer> pair = it.next();
		        if(pair.getValue() < 20)
		        {
		        	it.remove(); // avoids a ConcurrentModificationException
		        }
		        else
		        {
		        	s += "private double tourism_" + pair.getKey() + " ;\n";
		        }
		    }
			System.out.println(s);
			System.out.println(tagsMap.size());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<OSMNode> readOSMNodes()
	{
		tagsCount();
		List<OSMNode> osmList = new ArrayList<OSMNode>();
		File fXmlFile = new File(lgdFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			Element osm = doc.getDocumentElement();
			NodeList nList = osm.getElementsByTagName("node");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String lat = eElement.getAttribute("lat");
					String lon = eElement.getAttribute("lon");
					String id = eElement.getAttribute("id");
					NodeList tags = eElement.getElementsByTagName("tag");
					String name = getValueByName(tags, "name");
					OSMNode yb = new OSMNode(id, name, lat, lon);
					for (int i = 0; i < tags.getLength(); i++) {
						Node n = tags.item(i);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element e = (Element) n;
							if(e.hasAttribute("k") && e.hasAttribute("v") && !e.getAttribute("k").equalsIgnoreCase("name"))
							{
								yb.addValue(e.getAttribute("k"), e.getAttribute("v"));
							}
						}
					}
					osmList.add(yb);
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return osmList;
	}
	
	private String getValueByName(NodeList nList , String name)
	{
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement.hasAttribute("k") && eElement.getAttribute("k").equalsIgnoreCase(name))
				{
					return eElement.getAttribute("v");
				}
			}
		}
		return "";
	}
}
