package mgr.jena.gui;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JMenu;

import mgr.jena.RdfReader;
import mgr.jena.Utils.MapSorter;
import mgr.jena.osm.OSMNode;
import mgr.jena.osm.OSMReader;
import mgr.jena.osm.OSMReview;
import mgr.jena.osm.OSMUser;
import mgr.jena.recommendation.stereotypebased.StereotypeRecommender;
import mgr.jena.recommendation.userbased.UserNode;
import mgr.jena.recommendation.userbased.UserRecommender;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;

import com.hp.hpl.jena.rdf.model.RDFReader;
import com.roots.map.MapPanel;



public class MainWindow {

	private JFrame frame;
	private HashMap<String, Point2D> locations;
	private JCheckBoxMenuItem mnUnvisitedVisibility;
	private JCheckBoxMenuItem mnVisitedVisibility;
	private JCheckBoxMenuItem mnRecommendedVisibility;
	
	private JCheckBoxMenuItem mnUserRecommendation;
	private JCheckBoxMenuItem mnStereotypeRecommendation;
	private JCheckBoxMenuItem mnItemRecommendation;
	private JMenuItem mnRecommend;
	private JMapViewer map;
	
	JPanel nodePanel;
	
	JTextArea nodePane;
	StarRater starRater;
	
	private HashMap<MapMarker,OSMNode> nodes;
	private RdfReader rdfReader;
	private OSMUser currentUser = null;
	private OSMNode currentNode = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void initializeLocations()
	{
		// lat,lon
		locations = new HashMap<String, Point2D>();
		locations.put("Phoenix", new Point2D.Double(33.4483333, -112.0733333));
		locations.put("Las Vegas", new Point2D.Double(36.175, -115.1363889));
		locations.put("Charlotte", new Point2D.Double(35.2269444, -80.8433333));
		locations.put("Edinburgh", new Point2D.Double(55.95, -3.2));
		locations.put("Pittsburgh", new Point2D.Double(40.4405556, -79.9961111));
		locations.put("Montreal", new Point2D.Double(45.5, -73.583333));
		locations.put("Madison", new Point2D.Double(43.0730556, -89.4011111));
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
		rdfReader = new RdfReader();
		nodes = new HashMap<MapMarker, OSMNode>();
		OSMReader osmReader = new OSMReader();
		List<OSMNode> osmnodes = osmReader.readOSMNodes();
		for(int i=0;i<osmnodes.size();i++)
		{
			OSMNode node = osmnodes.get(i);
			MapMarkerDot d = new MapMarkerDot(node.lat,node.lon);
		    d.setColor(MarkerColors.UNVISITED_NODE);
		    map.addMapMarker(d);
		    nodes.put(d, node);
		}
		map.repaint();
	}
	
	private void changePosition()
	{
		JPanel panel = new JPanel();
		JLabel l1 = new JLabel("Enter cordinates");
		JLabel l2 = new JLabel("lat: ");
		JLabel l3 = new JLabel("lon: ");
		JTextField tfLat = new JTextField();
		tfLat.setPreferredSize(new Dimension(100, 30));
		JTextField tfLon = new JTextField();
		tfLon.setPreferredSize(new Dimension(100, 30));
		panel.add(l1);
		panel.add(l2);
		panel.add(tfLat);
		panel.add(l3);
		panel.add(tfLon);
		int result = JOptionPane.showConfirmDialog(frame, panel, "Enter coordinates", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION)
		{
			double lat = Double.parseDouble(tfLat.getText());
			double lon = Double.parseDouble(tfLon.getText());
			changePosition(new Point2D.Double(lat, lon));
		}
	}
	
	private void changePosition(Point2D position)
	{
		map.setDisplayPosition(new Coordinate(position.getX(),position.getY()),10);
		map.repaint();
		//JOptionPane.showMessageDialog(frame, "Position changed to : " + position.toString() );
	}
	
	private void recommend()
	{
		if(currentUser == null)
		{
			return;
		}
		
		Map<String, OSMUser> users = rdfReader.loadUsersOSM(nodes);
		Map<Long , Double> usersRec = new HashMap<Long,Double>();
		if(mnUserRecommendation.isSelected())
		{
			UserRecommender userRecommender = new UserRecommender();
			usersRec = userRecommender.getRecommendations(currentUser.getUserID() , users);
			
		}
		Map<OSMNode,Double> stereotypeRec = new HashMap<OSMNode,Double>();
		if(mnStereotypeRecommendation.isSelected())
		{
			StereotypeRecommender stereotypeReccommender = new StereotypeRecommender();
			stereotypeRec = stereotypeReccommender.getRecommendations(currentUser.getUserID(), users);
		}
		Iterator<Entry<Long , Double>> it = usersRec.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Long , Double> pair = it.next();
	        Iterator<Entry<MapMarker , OSMNode>> it1 = nodes.entrySet().iterator();
	        while (it1.hasNext()) {
		        Map.Entry<MapMarker , OSMNode> pair1 = it1.next();
		        if(pair1.getValue().id == pair.getKey())
		        {
		        	MapMarkerDot m = (MapMarkerDot) pair1.getKey();
		        	m.setBackColor(MarkerColors.RECOMMENDED_NODE);
		        	m.setColor(MarkerColors.RECOMMENDED_NODE);
		        	break;
		        }
	        }
	    }
	    int maxCounter = 20;
	    int counter = 0;
	    Iterator<Entry<OSMNode , Double>> itS = stereotypeRec.entrySet().iterator();
	    while (itS.hasNext() && counter < maxCounter) {
	        Map.Entry<OSMNode , Double> pair = itS.next();
	        Iterator<Entry<MapMarker , OSMNode>> itS1 = nodes.entrySet().iterator();
	        while (itS1.hasNext()) {
		        Map.Entry<MapMarker , OSMNode> pair1 = itS1.next();
		        if(pair1.getValue() == pair.getKey())
		        {
		        	counter++;
		        	MapMarkerDot m = (MapMarkerDot) pair1.getKey();
		        	m.setBackColor(MarkerColors.RECOMMENDED_NODE);
		        	m.setColor(MarkerColors.RECOMMENDED_NODE);
		        	break;
		        }
	        }
	    }
	}
	
	private void recommendationVisibility()
	{
		if(mnUserRecommendation.getState() || mnStereotypeRecommendation.getState() || mnItemRecommendation.getState())
		{
			mnRecommend.setEnabled(true);
		}
		else
		{
			mnRecommend.setEnabled(false);
		}
	}
	
	public void markerClicked(MapMarker mapMarker)
	{
		if(nodes.containsKey(mapMarker))
		{
			currentNode = nodes.get(mapMarker);
			nodePane.setText(currentNode.toString());
			if(mapMarker.getColor() == MarkerColors.UNVISITED_NODE)
			{
				starRater.setSelection(0);
				starRater.setVisible(true);
			}
			else if(mapMarker.getColor() == MarkerColors.VISITED_NODE)
			{
				if(currentUser.getReviews().containsKey(currentNode.id))
				{
					OSMReview r = currentUser.getReviews().get(currentNode.id);
					int selection = (int)(r.getMark() * 2.0);
					starRater.setSelection(selection);
				}
				starRater.setVisible(true);
			}
			else
			{
				starRater.setSelection(0);
				starRater.setVisible(true);
			}
			//System.out.println(nodes.get(mapMarker).toString());
		}
		//System.out.println(mapMarker.toString());
	}
	
	private void refreshNodesVisibility()
	{
		for(int i=0;i<map.getMapMarkerList().size();i++)
		{
			MapMarkerDot m = (MapMarkerDot) map.getMapMarkerList().get(i);
			Color type = m.getColor();
			if(type == MarkerColors.UNVISITED_NODE)
			{
				m.setVisible(mnUnvisitedVisibility.isSelected());
			}
			else if(type == MarkerColors.VISITED_NODE)
			{
				m.setVisible(mnVisitedVisibility.isSelected());
			}
			else if(type == MarkerColors.RECOMMENDED_NODE)
			{
				m.setVisible(mnRecommendedVisibility.isSelected());
			}
		}
		map.repaint();
	}
	
	private void clearNodeArea()
	{
		currentNode = null;
		nodePane.setText("");
		starRater.setVisible(false);
	}
	
	private void addReview(OSMNode n, double mark)
	{
		rdfReader.addReview(currentUser, n, mark);
	}
	
	private void refreshUser()
	{
		if(currentUser != null)
		{
			mnRecommend.setEnabled(true);
			Iterator<Entry<MapMarker,OSMNode>> it = nodes.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<MapMarker,OSMNode> pair = it.next();
		        MapMarkerDot m = (MapMarkerDot) pair.getKey();
		        if(currentUser.getReviews().containsKey(pair.getValue().id))
		        {
		        	m.setBackColor(MarkerColors.VISITED_NODE);
		        	m.setColor(MarkerColors.VISITED_NODE);
		        }
		        else
		        {
		        	m.setBackColor(MarkerColors.UNVISITED_NODE);
		        	m.setColor(MarkerColors.UNVISITED_NODE);
		        }
		    }
		    map.repaint();
		}
		else
		{
			mnRecommend.setEnabled(false);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		initializeLocations();
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		
		JMenuBar menuBar = new JMenuBar();
		
		
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem eMenuItemExit = new JMenuItem("Exit");
		eMenuItemExit.setMnemonic(KeyEvent.VK_E);
		eMenuItemExit.setToolTipText("Exit application");
		eMenuItemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
		
		mnFile.add(eMenuItemExit);
		
		
		JMenu mnSettings = new JMenu("Settings");
		mnSettings.setMnemonic(KeyEvent.VK_S);
		mnUnvisitedVisibility = new JCheckBoxMenuItem("Show unvisited POI",true);
		mnUnvisitedVisibility.setSelected(true);
		mnUnvisitedVisibility.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				refreshNodesVisibility();
			}
		});
		mnVisitedVisibility = new JCheckBoxMenuItem("Show visited POI",true);
		mnVisitedVisibility.setSelected(true);
		mnVisitedVisibility.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				refreshNodesVisibility();
			}
		});
		mnRecommendedVisibility = new JCheckBoxMenuItem("Show recommended POI",true);
		mnRecommendedVisibility.setSelected(true);
		mnRecommendedVisibility.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				refreshNodesVisibility();
			}
		});
		mnSettings.add(mnUnvisitedVisibility);
		mnSettings.add(mnVisitedVisibility);
		mnSettings.add(mnRecommendedVisibility);
		
		JMenu mnUser = new JMenu("User");
		mnUser.setMnemonic(KeyEvent.VK_U);
		JMenuItem eMenuItemLogin = new JMenuItem("Change User");
		eMenuItemLogin.setMnemonic(KeyEvent.VK_C);
		eMenuItemLogin.setToolTipText("Login as a new User");
		eMenuItemLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	Map<String, OSMUser> users = rdfReader.loadUsersOSM(nodes);
            	System.out.println("Number of users: " + users.size());
            	UsersDialog ud = new UsersDialog(users,currentUser);
                String userID = ud.getResult();
                ud.dispose();
                clearNodeArea();
                if(userID != "" )
                {
                	currentUser =  users.get(userID);
                	if(currentUser == null)
                	{
                		currentUser = new OSMUser(userID);
                    	users.put(userID, currentUser);
                	}
                	refreshUser();
                }
            }
        });
		mnUser.add(eMenuItemLogin);
		
		JMenu mnLocation = new JMenu("Location");
		mnLocation.setMnemonic(KeyEvent.VK_L);
		JMenuItem mnCordinates = new JMenuItem("Cordinates");
		mnCordinates.setToolTipText("Change current position");
		mnCordinates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	changePosition();
            }
        });
		mnLocation.add(mnCordinates);
		
		JMenuItem[] mnLocations = new JMenuItem[locations.size()];
		Iterator<Entry<String, Point2D>> it = locations.entrySet().iterator();
	    int i = 0;
		while (it.hasNext()) {
	        final Map.Entry<String, Point2D> pair = it.next();
	        mnLocations[i] = new JMenuItem(pair.getKey());
	        mnLocations[i].addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent event) {
	                changePosition(pair.getValue());
	            }
	        });
	        mnLocation.add(mnLocations[i]);
	    }
		
		JMenu mnRecommendation = new JMenu("Recommendation");
		mnRecommendation.setMnemonic(KeyEvent.VK_R);
		
		mnRecommend = new JMenuItem("Recommend");
		mnRecommend.setEnabled(false);
		mnRecommend.setToolTipText("Recommend items to user");
		mnRecommend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                recommend();
            }
        });
		
		mnUserRecommendation = new JCheckBoxMenuItem("User Recommendation",true);
		mnUserRecommendation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				recommendationVisibility();
			}
		});
		
		mnStereotypeRecommendation = new JCheckBoxMenuItem("Stereotype Recommendation",true);
		mnStereotypeRecommendation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				recommendationVisibility();
			}
		});
		
		mnItemRecommendation = new JCheckBoxMenuItem("Item Recommendation",true);
		mnItemRecommendation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				recommendationVisibility();
			}
		});
		
		mnRecommendation.add(mnRecommend);
		mnRecommendation.add(mnUserRecommendation);
		mnRecommendation.add(mnStereotypeRecommendation);
		mnRecommendation.add(mnItemRecommendation);
		
		menuBar.add(mnFile);
		menuBar.add(mnSettings);
		menuBar.add(mnUser);
		menuBar.add(mnLocation);
		menuBar.add(mnRecommendation);
		
		//GpxDrawHelper h = new GpxDrawHelper(arg0);
		
		frame.setJMenuBar(menuBar);
		
		map = new JMapViewer();
		//map.setZoom(10);
		map.setDisplayPosition(new Coordinate(locations.get("Edinburgh").getX(),locations.get("Edinburgh").getY()),10);
		WayPoint w = new WayPoint(new LatLon(map.getPosition().getLat(), map.getPosition().getLon()));
		w.put("cuisine", "vegetarian");
		map.repaint();
		CustomMapController mapController = new CustomMapController(map,this);
	    mapController.setMovementMouseButton(MouseEvent.BUTTON1);
	    nodePanel = new JPanel();
	    nodePane = new JTextArea();
	    nodePane.setEditable(false);  
	    nodePane.setCursor(null);  
	    nodePane.setOpaque(false);  
	    nodePane.setFocusable(false);  
	    nodePane.setFont(UIManager.getFont("Label.font"));      
	    nodePane.setWrapStyleWord(true);  
	    nodePane.setLineWrap(true);
	    nodePane.setPreferredSize(new Dimension(180,600));
	    starRater = new StarRater(10);
	    starRater.setVisible(false);
	    starRater.addStarListener(new StarRater.StarListener() {
	    	public void handleSelection(int selection) {
	    		double mark = (double)selection / 2.0;
	    		OSMReview r = new OSMReview(currentNode,mark);
	    		currentUser.addReview(r);
	    		refreshUser();
	    		addReview(currentNode, mark);
	    	}
	    });
	    nodePanel.add(starRater);
	    nodePanel.add(nodePane);
	    
	    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,map, nodePanel);
	    splitPane.setDividerLocation(600);
	    map.setMinimumSize(new Dimension(600,600));
	    frame.add(splitPane);
		
	    
		/*
		MapPanel mapPanel = new MapPanel(); // just a JPanel extension, add to any swing/awt container
		mapPanel.setZoom(10); // set some zoom level (1-18 are valid)
		double lon = 6.94;
		double lat = 50.95;
		Point position = mapPanel.computePosition(new Point2D.Double(lon, lat));
		mapPanel.setCenterPosition(position); // sets to the computed position
		
		mapPanel.repaint(); // if already visible trigger a repaint here
		
		frame.add(mapPanel);
		*/
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
