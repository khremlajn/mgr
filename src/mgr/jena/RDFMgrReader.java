package mgr.jena;
import org.apache.jena.query.spatial.EntityDefinition;
import org.apache.jena.query.spatial.SpatialDatasetFactory;
import org.apache.jena.query.spatial.SpatialIndex;
import org.apache.jena.query.spatial.SpatialIndexLucene;
import org.apache.jena.query.spatial.SpatialQuery;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.query.*;
//import org.apache.jena.query.spatial.EntityDefinition

public class RDFMgrReader {
	
	static Model model;
	
	 private static final String LUCENE_INDEX_PATH = "target/test/TDBDatasetWithLuceneSpatialIndex";
	 private static final File LUCENE_INDEX_DIR = new File(LUCENE_INDEX_PATH);
	 private static final String LUCENE_TDB_PATH = "target/test/TDB";
	 private static final File LUCENE_TDB_DIR = new File(LUCENE_TDB_PATH);
	
	/*public static void main(String[] args) throws FileNotFoundException {
		
		long startTime = System.nanoTime();
		model = ModelFactory.createDefaultModel();
		
		model.read("londonMgrDump.rdf","N-TRIPLES");
		
		//InputStream inputStream = new FileInputStream("londonMgrDump.rdf");
		
		//model.read(inputStream, null, "N-TRIPLES") ;
		//RDFDataMgr.read(model, inputStream, Lang.NTRIPLES) ;
		SearchInRadius(51.5072,0.1275,10);
		//model.write(System.out,"Turtle");
		
		//Dataset spatialDataset = initInMemoryDatasetWithLuceneSpatitalIndex(LUCENE_INDEX_DIR);
		//loadData(spatialDataset, "londonMgrDump.rdf");
		
		//queryData(spatialDataset);
		//destroy(spatialDataset);

	}
	*/
	
	public static void SearchInRadius2(double longnitude, double latitude, int km)
	{
		
	}
	
	public static void SearchInRadius(double longnitude, double latitude, int km)
	{
		Point2D.Double a = new Point2D.Double(-100, -100);
		Point2D.Double b = new Point2D.Double(100, 100);
		// Rectangle2D.Double c = new Rectangle2D.Double(a.x, a.y, b.x - a.x,
		// b.y - a.y);
		String polygon = "POLYGON((" + a.x + " " + a.y + "," + b.x + " " + a.y
		+ "," + b.x + " " + b.y + "," + a.x + " " + b.y + "," + a.x
		+ " " + a.y + "))";
		String filter = "FILTER (ogc:intersects(?geo, ogc:geomFromText('"
		+ polygon + "')))";
		
		String queryString = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX bif: <http://www.openlinksw.com/schemas/bif#>",
				"Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>",
				"Prefix lgdo:<http://linkedgeodata.org/ontology/>",
				"Prefix geom: <http://geovocab.org/geometry#>",
				"Prefix ogc: <http://www.opengis.net/ont/geosparql#>",
				"Select * {",
				  "?b a lgdo:Restaurant .",
				  "?b rdfs:label ?l .",
				  "?b geom:geometry ?geom .",
				  "?b spatial:nearby(51.398 0.0 10 'mi').",
				  //"?geom ogc:asWKT ?geo .",
				  
				  //"Filter(bif:st_intersects(?geo, bif:st_geomFromText('POLYGON((50 -10, 55 -10, 55 10, 50 10, 50 -10))')))",
				  //filter,
				  //"Filter(bif:st_intersects (?geo, bif:st_point (0.0, 51.3980144), 10)) .",
				"}");
		
		
		/*
		String queryString = new StringBuilder()
        .append("PREFIX lgdo: <http://linkedgeodata.org/ontology/>\n")
        //.append("PREFIX spatial: <http://jena.apache.org/spatial#>\n")
        .append("Prefix spatial: <http://geovocab.org/spatial#>\n")
        .append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n")
        .append("Prefix geo: <http://geovocab.org/geometry#>\n")
        .append("Prefix bif: <http://www.openlinksw.com/schemas/bif#>\n")
        .append("Select *\n")
        .append("From <http://linkedgeodata.org>\n")
        .append("{\n")
        .append("?s a lgdo:Restaurant .\n")
        .append("?s rdfs:label ?l .\n")
        .append("?s geo:geometry ?g .\n")
        //.append("?place spatial:withinCircle (-0.14 51.53 10 'miles')\n")
        .append("Filter(bif:st_intersects (?gg, bif:st_point (51.3980144 0.0), 10.0)) .\n")
        .append("}\n")
        .toString();*/
		Query query = QueryFactory.create(queryString) ;
		  try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		    ResultSet results = qexec.execSelect() ;
		    int size = 0;
		    while(results.hasNext())
		    {
		      results.nextSolution();
		      QuerySolution soln = results.nextSolution() ;
		      Resource r = soln.getResource("geom") ; // Get a result variable - must be a resource
		      StmtIterator si =  r.listProperties();
		      while(si.hasNext())
		      {
		    	  Statement s = si.next();
		    	  String out = s.getObject().toString();
		    	  size = 1;
		      }
		      size = size +1;
		    }
		    System.out.println(size);
		  }
	}

}
