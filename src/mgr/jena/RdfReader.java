package mgr.jena;

import java.io.File ;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException ;
import java.io.InputStream;

import mgr.jena.Utils.MapSorter;
import mgr.jena.database.UsersDatabaseCreator;
import mgr.jena.gui.MarkerColors;
import mgr.jena.osm.OSMNode;
import mgr.jena.osm.OSMReview;
import mgr.jena.osm.OSMUser;
import mgr.jena.recommendation.itembased.RdfBusiness;
import mgr.jena.recommendation.userbased.ItemNode;
import mgr.jena.recommendation.userbased.UserNode;
import mgr.jena.recommendation.userbased.UserRecommender;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.spatial.* ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.FSDirectory ;
import org.openstreetmap.gui.jmapviewer.Demo;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntTools;
import com.hp.hpl.jena.ontology.OntTools.Path;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class RdfReader {
	static {
		LogCtl.setLog4j();
	}
	static Logger log = LoggerFactory.getLogger("JenaSpatialExample");
	
	private final String LUCENE_INDEX_PATH = "target/test/TDBDatasetWithLuceneSpatialIndex";
	private final File LUCENE_INDEX_DIR = new File(LUCENE_INDEX_PATH);
	private final String LUCENE_TDB_PATH = "target/test/TDB";
	private final File LUCENE_TDB_DIR = new File(LUCENE_TDB_PATH);
	private final String RDFFILE = "mgr.nt";
	
	private Dataset spatialDataset;
	
    private static final String  SOLR_DATA_PATH      = "src/test/resources/SolrHome/SolrARQCollection/data";
    private static final File    SOLR_DATA_DIR       = new File(SOLR_DATA_PATH);
    
    public RdfReader()
    {
    	try {
			spatialDataset = initInMemoryDatasetWithLuceneSpatitalIndex(LUCENE_INDEX_DIR);
			loadData();
			//CreateOntology();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void CreateOntology()
    {
    	//QueryExecutionFactory qef = 
    	String c = OWL.Class.toString();
    	String p =  "http://www.w3.org/2002/07/owl#DatatypeProperty";
    	String pr =  "http://www.w3.org/2002/07/owl#Property";
    	String query = "Construct { ?s ?p ?o } { ?s a <" + p
                + "> . ?s ?p ?o . }";
    	
    	String query2 = "Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?s ?p ?o } { ?s a ?t ; ?p ?o . Filter(?t = owl:Class || ?t = owl:Property || ?t = owl:DatatypeProperty || ?t = owl:NamedIndividual) }";
    	
    	String query3 = "Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?s ?p ?o . ?o ?x ?y } { ?s a ?t ; ?p ?o . Optional { ?o ?x ?y } . Filter(?t = owl:Class || ?t = owl:Property || ?t = owl:DatatypeProperty || ?t = owl:NamedIndividual) }";
    	
    	Model m = spatialDataset.getDefaultModel();
    	Model ontology = QueryExecutionFactory.create(query3, m).execConstruct();
        FileOutputStream fos;
		try {
			fos = new FileOutputStream("mgr.owl");
			ontology.write(fos,"TURTLE");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//Model result = qef.createQueryExecution(query).execConstruct();

        // result.write(System.out, "TURTLE");

        //return result;
    }
    public void getAllProperties()
    {
    	String pre = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX lgd: <http://linkedgeodata.org/triplify/>",
				"PREFIX lgdo: <http://linkedgeodata.org/ontology/>");

		String qs = "SELECT * WHERE { ?s ?p ?o . FILTER regex(STR(?s), \"http://linkedgeodata.org/triplify/node\" ) . }";
		
		Map<String, Integer> mapS = new HashMap<String, Integer>();
		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			//QueryExecUtils.executeQuery(q, qexec);
			ResultSet results = qexec.execSelect() ;
		    while(results.hasNext())
		    {
		    	QuerySolution s = results.next();
		    	if(s.contains("s") && s.contains("p") && s.contains("o"))
		    	{
		    		String[] tokenS = s.get("s").toString().split("/");
		    		String p = s.get("p").toString();
		    		//double mark  = s.get("p").asLiteral().getDouble();
		    		if(tokenS.length > 0)
		    		{
		    			String nodeID = tokenS[tokenS.length - 1];
		    			nodeID = nodeID.replace("node", "");
		    			if(!mapS.containsKey(p))
		    			{
		    				mapS.put(p, 1);
		    			}
		    			else
		    			{
		    				mapS.put(p, mapS.get(p) + 1);
		    			}
		    			//long nodeIDLong = Long.parseLong(nodeID);
		    			
		    		}
		    	}
		    }
		} finally {
			spatialDataset.end();
		}
		mapS = MapSorter.sortByValue(mapS);
		Iterator<Entry<String , Integer>> it1 = mapS.entrySet().iterator();
        String properties = ""; 
		while (it1.hasNext()) {
	        Map.Entry<String , Integer> pair1 = it1.next();
	         properties +=  ",(" + pair1.getKey() + ", Inter)";
        }
		System.out.println(properties);
		System.out.println("test");
    }
    
    public void FindJenaDistance() {

        // Jena implementation 

        long startTime = System.currentTimeMillis();
        
        // this file needs to be created by doing "Save As.." and "RDF/XML" for a 'normal' OWL file. Otherwise we get Jena parse errors
        String inputFileName = "mgr.owl";
        

        String ns = "http://linkedgeodata.org/";
        
        Model model = spatialDataset.getDefaultModel();
        //OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        InputStream in = FileManager.get().open(inputFileName);
        model.read(in,"TURTLE");
        
        System.out.format("Ontology load time: (%7.2f sec)%n%n", (System.currentTimeMillis() - startTime) / 1000.0);        
        
        //OntClass fromSubClass = model.getOntClass("http://linkedgeodata.org/restaurant");        
        //OntClass toSuperClass = model.getOntClass("http://http://linkedgeodata.org/pub");
        Resource fromSubClass = model.getResource("http://linkedgeodata.org/triplify/node910280833");
        Resource toSuperClass = model.getResource("http://linkedgeodata.org/triplify/node907845241");
        
        
        Path path = OntTools.findShortestPath(model, fromSubClass, toSuperClass, Filter.any);

        if (path != null){
            int superClasses = 0;
            for (Statement s: path) {
                superClasses++;
            	/*
            	if (s.getObject().toString().startsWith(ns)) {
                    // filter out OWL Classes
                    superClasses++;
                    System.out.println(s.getObject());
                }*/
            }
            System.out.println("Shortest distance from " + fromSubClass + " to " + toSuperClass + " = " + superClasses);
        }else if (fromSubClass == toSuperClass){
            System.out.println("Same node");
        }else {
            System.out.println("No path from " + fromSubClass + " to " + toSuperClass);
        }   

        System.out.format("\nProcessing time: (%7.2f sec)%n%n", (System.currentTimeMillis() - startTime) / 1000.0);
		
    }
    
    public Map<String, OSMUser> loadUsersOSM(Map<MapMarker,OSMNode> nodes)
    {
    	Map<String, OSMUser> users = new HashMap<String, OSMUser>();
    	String pre = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX lgd: <http://linkedgeodata.org/triplify/>",
				"PREFIX lgdo: <http://linkedgeodata.org/ontology/>");

		String qs = "SELECT * WHERE { ?s lgdo:hasreview ?o . ?o lgdo:mark ?m . FILTER regex(STR(?s), \"http://linkedgeodata.org/triplify/user/\" ) . }";	
		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			ResultSet results = qexec.execSelect() ;
		    
		    while(results.hasNext())
		    {
		    	QuerySolution s = results.next();
		    	if(s.contains("s") && s.contains("m") && s.contains("o"))
		    	{
		    		String[] tokenS = s.get("s").toString().split("/");
		    		double mark  = s.get("m").asLiteral().getDouble();
		    		String[] tokenO = s.get("o").toString().split("/");
		    		if(tokenS.length > 0 && tokenO.length > 0)
		    		{
		    			String userID = tokenS[tokenS.length - 1];
		    			String nodeID = tokenO[tokenO.length - 1];
		    			nodeID = nodeID.replace(userID, "");
		    			nodeID = nodeID.replace("review", "");
		    			long nodeIDLong = Long.parseLong(nodeID);
		    			OSMUser un = null;
		    			un = users.get(userID);
		    			if(un == null)
		    			{
		    				un = new OSMUser(userID);
		    			}
		    			Iterator<Entry<MapMarker , OSMNode>> it1 = nodes.entrySet().iterator();
		    	        while (it1.hasNext()) {
		    		        Map.Entry<MapMarker , OSMNode> pair1 = it1.next();
		    		        OSMNode on = pair1.getValue();
		    		        if(on.id == nodeIDLong)
		    		        {
		    		        	un.addReview(new OSMReview(on, mark));
		    		        	break;
		    		        }
		    	        }
		    			users.put(userID, un);
		    		}
		    	}
		    }
		} finally {
			spatialDataset.end();
		}
		return users;
    }
    
    /*
    public Map<String, UserNode> loadUserRecommendations()
    {
    	Map<String, UserNode> users = new HashMap<String, UserNode>();
    	String pre = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX lgd: <http://linkedgeodata.org/triplify/>",
				"PREFIX lgdo: <http://linkedgeodata.org/ontology/>");

		String qs = "SELECT * WHERE { ?s lgdo:hasreview ?o . ?o lgdo:mark ?m . FILTER regex(STR(?s), \"http://linkedgeodata.org/triplify/user/\" ) . }";	
		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			ResultSet results = qexec.execSelect() ;
		    
		    while(results.hasNext())
		    {
		    	QuerySolution s = results.next();
		    	if(s.contains("s") && s.contains("m") && s.contains("o"))
		    	{
		    		String[] tokenS = s.get("s").toString().split("/");
		    		double mark  = s.get("m").asLiteral().getDouble();
		    		String[] tokenO = s.get("o").toString().split("/");
		    		if(tokenS.length > 0 && tokenO.length > 0)
		    		{
		    			String userID = tokenS[tokenS.length - 1];
		    			String nodeID = tokenO[tokenO.length - 1];
		    			nodeID = nodeID.replace(userID, "");
		    			nodeID = nodeID.replace("review", "");
		    			UserNode un = null;
		    			un = users.get(userID);
		    			if(un == null)
		    			{
		    				un = new UserNode(userID);
		    			}
		    			un.AddItem(new ItemNode(Long.parseLong(nodeID), mark));
		    			users.put(userID, un);
		    		}
		    	}
		    }
		} finally {
			spatialDataset.end();
		}
		//remove users with not enough reviews
		Iterator<Entry<String, UserNode>> it = users.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, UserNode> pair = it.next();
	        if(pair.getValue().getItemsSize() < 5)
	        {
	        	it.remove();
	        }
	    }
		return users;
    }
    */
    
    
    
//	public static void main(String... argv) throws IOException {
//		Dataset spatialDataset = initInMemoryDatasetWithLuceneSpatitalIndex(LUCENE_INDEX_DIR);
//		//Dataset spatialDataset = initTDBDatasetWithLuceneSpatitalIndex(LUCENE_INDEX_DIR, LUCENE_TDB_DIR);
//		//Dataset spatialDataset = createLuceneAssembler() ;
//		//Dataset spatialDataset = createSolrAssembler() ;
//		
//		loadData(spatialDataset, RDFFILE);
//		UserRecommender ur = new UserRecommender();
//		loadUserRecommendations(ur, spatialDataset);
//		ur.getRecommendations("z6qJ5ZJz979Mpv7eatTIHA");
//		
//		//findSimilarUser(spatialDataset,"");
//		//queryData(spatialDataset);
//		
//		/*
//		OntModel onto = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
//		String inputFileName = "/home/arek/Desktop/Ontology2.owl";
//	    try {
//	        InputStream in = FileManager.get().open(inputFileName);
//	        onto.read(in, "RDF/XML");
//	    } catch (JenaException je) {
//	        System.out.println("ERROR" + je.getMessage());
//	        je.printStackTrace();
//	        System.exit(0);
//	    }
//		*/
//		
//		
//		/////////////////
//		/*
//		PostgresHelper client = new PostgresHelper(
//				DbContract.HOST, 
//				DbContract.DB_NAME,
//				DbContract.USERNAME,
//				DbContract.PASSWORD);
//		try {
//			if (client.connect()) {
//				System.out.println("DB connected");
//				
//				UsersDatabaseCreator udc = new UsersDatabaseCreator();
//				udc.generateDatabase(client);
//				
//				
//			}
//			
//		} catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//		}
//		*/
//		
//		
//		/////////////////
//		
//		
//		
//		/*
//		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//		Model schema = ModelFactory.createDefaultModel();
//		InputStream inputStream = new FileInputStream("foodsOntology.owl");
//		RDFDataMgr.read(schema, inputStream, Lang.NTRIPLES) ; */
//		//Model schema = ModelFactory.createDefaultModel();
//		//schema.read("foodsOntology.owl","N-TRIPLES");
//		//Model schema = FileManager.get().loadModel("foodsOntology.owl");
//		//reasoner = reasoner.bindSchema(schema);
//		//InfModel infmodel = ModelFactory.createInfModel(reasoner, spatialDataset.getDefaultModel());
//		//Resource restaurants = infmodel.getResource("http://linkedgeodata.org/ontology/Restaurant");
//		//System.out.println("restaurants *:");
//		//printStatements(infmodel, restaurants, null, null);
//		//destroy(spatialDataset);
//	}
	
	public void printStatements(Model m, Resource s, Property p, Resource o) {
	    for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
	        Statement stmt = i.nextStatement();
	        System.out.println(" - " + PrintUtil.print(stmt));
	    }
	}
	
	private void findSimilarUser(Dataset spatialDataset, String user1)
	{
		String similarUser = "";
		String pre = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX lgd: <http://linkedgeodata.org/triplify/>",
				"PREFIX lgdo: <http://linkedgeodata.org/ontology/>");

		
		//String qs = "SELECT * WHERE { ?s ?p ?o . FILTER regex(STR(?s), \"http://linkedgeodata.org/triplify/user/\" ) . }";
		String qs = "SELECT * WHERE { ?s lgdo:hasreview ?o . ?o lgdo:mark ?m . FILTER regex(STR(?s), \"http://linkedgeodata.org/triplify/user/\" ) . }";
		
		/*
		String qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s a lgdo:Restaurant . ?s lgdo:cuisine ?c . ?s spatial:nearby (55.9531 -3.1889 10.0 'miles') ;",
				"      rdfs:label ?label", " }");
		*/

		spatialDataset.begin(ReadWrite.READ);
		int size = 0;
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			//QueryExecUtils.executeQuery(q, qexec);
			ResultSet results = qexec.execSelect() ;
		    
		    while(results.hasNext())
		    {
		    	QuerySolution s = results.next();
		    	String t = s.toString();
		    	size = size + 1;
		    }
		} finally {
			spatialDataset.end();
		}
		System.out.println(size);
	}
	
	
	//
	
	private void destroy(Dataset spatialDataset){

		SpatialIndex index = (SpatialIndex)spatialDataset.getContext().get(SpatialQuery.spatialIndex);
		if (index instanceof SpatialIndexLucene){
			deleteOldFiles(LUCENE_INDEX_DIR);
			deleteOldFiles(LUCENE_TDB_DIR);
		}
		
//		else if (index instanceof SpatialIndexSolr){
//			SolrServer server = ((SpatialIndexSolr)index).getServer();
//			
//			if (server instanceof EmbeddedSolrServer){
//				server.shutdown();
//				deleteOldFiles(SOLR_DATA_DIR);
//			}
//		} 
		
	}
    private void emptyAndDeleteDirectory(File dir) {
        File[] contents = dir.listFiles() ;
        if (contents != null) {
            for (File content : contents) {
                if (content.isDirectory()) {
                    emptyAndDeleteDirectory(content) ;
                } else {
                    content.delete() ;
                }
            }
        }
        dir.delete() ;
    }
    
    private Dataset initInMemoryDatasetWithLuceneSpatitalIndex(File indexDir) throws IOException{
		SpatialQuery.init();
		deleteOldFiles(indexDir);
		indexDir.mkdirs();
		return createDatasetByCode(indexDir);
		
    }
    
    private Dataset initTDBDatasetWithLuceneSpatitalIndex(File indexDir, File TDBDir) throws IOException{
		SpatialQuery.init();
		deleteOldFiles(indexDir);
		deleteOldFiles(TDBDir);
		indexDir.mkdirs();
		TDBDir.mkdir();
		return createDatasetByCode(indexDir, TDBDir);
    }
    
	private void deleteOldFiles(File indexDir) {
		if (indexDir.exists())
			emptyAndDeleteDirectory(indexDir);
	}
	
	private Dataset createDatasetByCode(File indexDir) throws IOException {
		// Base data
		Dataset ds1 = DatasetFactory.createMem();
		return joinDataset(ds1, indexDir);
	}
	
	private Dataset createDatasetByCode(File indexDir, File TDBDir) throws IOException {
		// Base data
		Dataset ds1 = TDBFactory.createDataset(TDBDir.getAbsolutePath());
		return joinDataset(ds1, indexDir);
	}
	
	private Dataset joinDataset(Dataset baseDataset, File indexDir) throws IOException{
		EntityDefinition entDef = new EntityDefinition("entityField", "geoField");
		
		// you need JTS lib in the classpath to run the examples
		entDef.setSpatialContextFactory(SpatialQuery.JTS_SPATIAL_CONTEXT_FACTORY_CLASS);
		
		// set custom goe predicates
		entDef.addSpatialPredicatePair(ResourceFactory.createResource("http://localhost/jena_example/#latitude_1"), ResourceFactory.createResource("http://localhost/jena_example/#longitude_1"));
		entDef.addSpatialPredicatePair(ResourceFactory.createResource("http://localhost/jena_example/#latitude_2"), ResourceFactory.createResource("http://localhost/jena_example/#longitude_2"));
		entDef.addWKTPredicate(ResourceFactory.createResource("http://localhost/jena_example/#wkt_1"));
		entDef.addWKTPredicate(ResourceFactory.createResource("http://localhost/jena_example/#wkt_2"));
		

		// Lucene, index in File system.
		Directory dir = FSDirectory.open(indexDir);

		// Join together into a dataset
		Dataset ds = SpatialDatasetFactory.createLucene(baseDataset, dir, entDef);

		return ds;
	}

	public Dataset createLuceneAssembler() {
		log.info("Construct lucene spatial dataset using an assembler description");

		Dataset ds = DatasetFactory.assemble("src/test/resources/spatial-config.ttl",
				"http://localhost/jena_example/#spatial_dataset");
		

		return ds;
	}
	
	public Dataset createSolrAssembler() {
		log.info("Construct solr spatial dataset using an assembler description");
		
		Dataset ds = DatasetFactory.assemble("src/test/resources/spatial-solr-config.ttl",
				"http://localhost/jena_example/#spatial_dataset");
		return ds;
	}
	
	public void addReview(OSMUser u, OSMNode n, double mark)
	{
		String lgd = "http://linkedgeodata.org/triplify/";
		String lgdo = "http://linkedgeodata.org/ontology/";
		String s = lgd + "user/" + u.getUserID();
		Model model = spatialDataset.getDefaultModel();
		Resource reviewS = model.createResource(lgd + "review" + u.getUserID() + Long.toString(n.id));
		Property reviewP = model.createProperty(lgdo + "likes");
		Resource nodeO = model.getResource(lgd + "node" + Long.toString(n.id));
		Statement statement = model.createStatement(reviewS, reviewP, nodeO);
		model.add(statement);
		Property markP = model.createProperty(lgdo + "mark");
		RDFNode markO = model.createTypedLiteral(mark);
		statement = model.createStatement(reviewS, markP, markO);
		model.add(statement);
		Property hasreviewP = model.createProperty(lgdo + "hasreview");
		statement = model.createStatement(model.createResource(s), hasreviewP, reviewS);
		model.add(statement);
		try
		{
			FileOutputStream outputStream = new FileOutputStream(RDFFILE);
			RDFDataMgr.write(outputStream, model , Lang.NTRIPLES) ;
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	public void addData(String subject, String predicate, Object object)
	{
		Model model = spatialDataset.getDefaultModel();
		//Resource r = model.createResource(subject).addProperty(predicate, object);
		Resource s = model.createResource(subject);
		Property p = model.createProperty(predicate);
		RDFNode o = null;
		if(object instanceof Double)
		{
			o = model.createTypedLiteral(object);
		}
		else if(object instanceof String)
		{
			o = model.createTypedLiteral(object);
		}
		else
		{
			o = s;
		}
		Statement statement = model.createStatement(s, p, o);
		model.add(statement);
		try
		{
			//FileOutputStream outputStream = new FileOutputStream(RDFFILE);
			FileOutputStream outputStream = new FileOutputStream("output.nt");
			RDFDataMgr.write(outputStream, model , Lang.NTRIPLES) ;
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		
	}
	
	public Map<String, RdfBusiness> getAllNodes()
	{
		Model schema = FileManager.get().loadModel("mgr.owl");
		//Model data = FileManager.get().loadModel("file:data/owlDemoData.rdf");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		InfModel infmodel = ModelFactory.createInfModel(reasoner, spatialDataset.getDefaultModel());
		//Property p = infmodel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		RDFNode o = infmodel.getResource("http://linkedgeodata.org/meta/Node");
		//ResIterator it = spatialDataset.getDefaultModel().listResourcesWithProperty(RDF.type,o);
		ResIterator it =  infmodel.listResourcesWithProperty(RDF.type,o);;
		Property p = null;
		RDFNode n = null;
		Map<String, RdfBusiness> poiMap = new HashMap<String, RdfBusiness>();
		int counter = 0;
		while(it.hasNext()) {
	        
			Resource res = it.nextResource();
	        StmtIterator st =  infmodel.listStatements(res,p,n);
	        RdfBusiness poi = new RdfBusiness();
	        String[] tokens = res.toString().split("/"); 
        	String nodeID =  tokens[tokens.length - 1];
	        while(st.hasNext())
	        {
	        	Statement s = st.nextStatement();
	        	Triple t = s.asTriple();
	        	String predicate = t.getPredicate().toString().replaceAll("#", "/");
	        	tokens = predicate.split("/");
	        	String key = tokens[tokens.length - 1];
	        	String val = t.getObject().toString();
	        	poi.addValue(key, val);
	        }
	        poiMap.put(nodeID,poi);
	        counter ++;
	        System.out.println(counter);
	    }
		return poiMap;
	}

	private void loadData() throws FileNotFoundException {
		log.info("Start loading");
		long startTime = System.nanoTime();
		spatialDataset.begin(ReadWrite.WRITE);
		try {
			Model m = spatialDataset.getDefaultModel();
			InputStream inputStream = new FileInputStream(RDFFILE);
			RDFDataMgr.read(m, inputStream, Lang.NTRIPLES) ;
			//RDFDataMgr.read(m, file,Lang.NTRIPLES);
			// RDFDataMgr.read(dataset, "D.ttl") ;
			spatialDataset.commit();
		} finally {
			spatialDataset.end();
		}

		long finishTime = System.nanoTime();
		double time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("Finish loading - %.2fms", time));
	}

	public void queryData() {
		log.info("START");
		long startTime = System.nanoTime();
		String pre = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX lgdo: <http://linkedgeodata.org/ontology/>");

		System.out.println("nearby");
		String qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s a lgdo:Restaurant . ?s lgdo:cuisine ?c . ?s spatial:nearby (55.9531 -3.1889 10.0 'miles') ;",
				"      rdfs:label ?label", " }");
		/*
		String qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s a lgdo:Restaurant . ?s lgdo:cuisine ?c . ?s spatial:nearby (51.3000 0.0 10.0 'miles') ;",
				"      rdfs:label ?label", " }");*/

		spatialDataset.begin(ReadWrite.READ);
		int size = 0;
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			//QueryExecUtils.executeQuery(q, qexec);
			ResultSet results = qexec.execSelect() ;
		    
		    while(results.hasNext())
		    {
		    	QuerySolution s = results.next();
		    	//String t = s.toString();
		    	size = size + 1;
		    }
		} finally {
			spatialDataset.end();
		}
		long finishTime = System.nanoTime();
		double time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		System.out.println(size);
		/*
		System.out.println("withinCircle");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:withinCircle (51.3000 -2.71000 100.0 'miles' 3) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		
		System.out.println("withinBox");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:withinBox (51.1000 -4.0000 51.4000 0.0000 -1) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));

		System.out.println("interesectBox");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:intersectBox (51.1000 -4.0000 51.4000 0.0000) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		
		
		System.out.println("north");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:north (51.3000 0.0000) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		
		System.out.println("south");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:south (51.3000 0.0000) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		
		System.out.println("east");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:east (51.3000 0.0000) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		
		System.out.println("west");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:west (51.3000 0.0000) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		
		System.out.println("west2");
		startTime = System.nanoTime();
		qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:withinBox (51.1 -180.0000 51.9 0.0000) ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			QueryExecUtils.executeQuery(q, qexec);
		} finally {
			spatialDataset.end();
		}
		finishTime = System.nanoTime();
		time = (finishTime - startTime) / 1.0e6;
		log.info(String.format("FINISH - %.2fms", time));
		*/
	}

}
