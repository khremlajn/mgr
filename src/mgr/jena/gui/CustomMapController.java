package mgr.jena.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class CustomMapController extends DefaultMapController {

	MainWindow parent;
	
	
	public CustomMapController(JMapViewer map, MainWindow parent) {
		super(map);
		this.parent = parent;
		// TODO Auto-generated constructor stub
	}
	
	public void mouseClicked(MouseEvent e) {

	    if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1){

	         Point p = e.getPoint();
	            int X = p.x+3;
	            int Y = p.y+3;
	            List<MapMarker> ar = map.getMapMarkerList();
	            Iterator<MapMarker> i = ar.iterator();
	            while (i.hasNext()) {

	                MapMarker mapMarker = (MapMarker) i.next();
	                if(!mapMarker.isVisible()) continue;
	                Point MarkerPosition = map.getMapPosition(mapMarker.getLat(), mapMarker.getLon());
	                if( MarkerPosition != null){

	                    int centerX =  MarkerPosition.x;
	                    int centerY = MarkerPosition.y;

	                    // calculate the radius from the touch to the center of the dot
	                    double radCircle  = Math.sqrt( (((centerX-X)*(centerX-X)) + (centerY-Y)*(centerY-Y)));

	                    // if the radius is smaller then 23 (radius of a ball is 5), then it must be on the dot
	                    if (radCircle < 8){
	                        parent.markerClicked(mapMarker);                       }
	                    
	                }
	            }
	    }

	    else if (super.isDoubleClickZoomEnabled() && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
	        map.zoomIn(e.getPoint());
	    }
	}

}
