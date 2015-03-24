package mgr.jena;

import java.io.File ;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException ;
import java.io.InputStream;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.spatial.* ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.FSDirectory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Build a spatial search dataset */
public class SpatialExample {
	static {
		LogCtl.setLog4j();
	}
	static Logger log = LoggerFactory.getLogger("JenaSpatialExample");
	
	private static final String LUCENE_INDEX_PATH = "target/test/TDBDatasetWithLuceneSpatialIndex";
	private static final File LUCENE_INDEX_DIR = new File(LUCENE_INDEX_PATH);
	private static final String LUCENE_TDB_PATH = "target/test/TDB";
	private static final File LUCENE_TDB_DIR = new File(LUCENE_TDB_PATH);
	
    private static final String  SOLR_DATA_PATH      = "src/test/resources/SolrHome/SolrARQCollection/data";
    private static final File    SOLR_DATA_DIR       = new File(SOLR_DATA_PATH);
    
    

	public static void main(String... argv) throws IOException {
		Dataset spatialDataset = initInMemoryDatasetWithLuceneSpatitalIndex(LUCENE_INDEX_DIR);
		//Dataset spatialDataset = initTDBDatasetWithLuceneSpatitalIndex(LUCENE_INDEX_DIR, LUCENE_TDB_DIR);
		//Dataset spatialDataset = createLuceneAssembler() ;
		//Dataset spatialDataset = createSolrAssembler() ;
		
		loadData(spatialDataset, "londonMgrDump.nt");
		queryData(spatialDataset);
		
		
		
		/////////////////
		PostgresHelper client = new PostgresHelper(
				DbContract.HOST, 
				DbContract.DB_NAME,
				DbContract.USERNAME,
				DbContract.PASSWORD);
		
		try {
			if (client.connect()) {
				System.out.println("DB connected");
				
				Map values = new HashMap(); 
				values.put("name", "test");
				if (client.insert("users", values) == 1) {
					System.out.println("Record added");
				}
				java.sql.ResultSet rs = client.execQuery("SELECT * FROM users");
				while(rs.next()) {
					
					System.out.printf("%d\t%s\t\n", 
							rs.getInt(1),
							rs.getString(2));
				}
				
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		
		
		/////////////////
		
		
		
		/*
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		Model schema = ModelFactory.createDefaultModel();
		InputStream inputStream = new FileInputStream("foodsOntology.owl");
		RDFDataMgr.read(schema, inputStream, Lang.NTRIPLES) ; */
		//Model schema = ModelFactory.createDefaultModel();
		//schema.read("foodsOntology.owl","N-TRIPLES");
		//Model schema = FileManager.get().loadModel("foodsOntology.owl");
		//reasoner = reasoner.bindSchema(schema);
		//InfModel infmodel = ModelFactory.createInfModel(reasoner, spatialDataset.getDefaultModel());
		//Resource restaurants = infmodel.getResource("http://linkedgeodata.org/ontology/Restaurant");
		//System.out.println("restaurants *:");
		//printStatements(infmodel, restaurants, null, null);
		//destroy(spatialDataset);
	}
	
	public static void printStatements(Model m, Resource s, Property p, Resource o) {
	    for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
	        Statement stmt = i.nextStatement();
	        System.out.println(" - " + PrintUtil.print(stmt));
	    }
	}
	
	private static void destroy(Dataset spatialDataset){

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
    private static void emptyAndDeleteDirectory(File dir) {
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
    
    private static Dataset initInMemoryDatasetWithLuceneSpatitalIndex(File indexDir) throws IOException{
		SpatialQuery.init();
		deleteOldFiles(indexDir);
		indexDir.mkdirs();
		return createDatasetByCode(indexDir);
    }
    
    private static Dataset initTDBDatasetWithLuceneSpatitalIndex(File indexDir, File TDBDir) throws IOException{
		SpatialQuery.init();
		deleteOldFiles(indexDir);
		deleteOldFiles(TDBDir);
		indexDir.mkdirs();
		TDBDir.mkdir();
		return createDatasetByCode(indexDir, TDBDir);
    }
    
	private static void deleteOldFiles(File indexDir) {
		if (indexDir.exists())
			emptyAndDeleteDirectory(indexDir);
	}
	
	private static Dataset createDatasetByCode(File indexDir) throws IOException {
		// Base data
		Dataset ds1 = DatasetFactory.createMem();
		return joinDataset(ds1, indexDir);
	}
	
	private static Dataset createDatasetByCode(File indexDir, File TDBDir) throws IOException {
		// Base data
		Dataset ds1 = TDBFactory.createDataset(TDBDir.getAbsolutePath());
		return joinDataset(ds1, indexDir);
	}
	
	private static Dataset joinDataset(Dataset baseDataset, File indexDir) throws IOException{
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

	public static Dataset createLuceneAssembler() {
		log.info("Construct lucene spatial dataset using an assembler description");

		Dataset ds = DatasetFactory.assemble("src/test/resources/spatial-config.ttl",
				"http://localhost/jena_example/#spatial_dataset");
		

		return ds;
	}
	
	public static Dataset createSolrAssembler() {
		log.info("Construct solr spatial dataset using an assembler description");
		
		Dataset ds = DatasetFactory.assemble("src/test/resources/spatial-solr-config.ttl",
				"http://localhost/jena_example/#spatial_dataset");
		return ds;
	}

	public static void loadData(Dataset spatialDataset, String file) throws FileNotFoundException {
		log.info("Start loading");
		long startTime = System.nanoTime();
		spatialDataset.begin(ReadWrite.WRITE);
		try {
			Model m = spatialDataset.getDefaultModel();
			InputStream inputStream = new FileInputStream("londonMgrDump.nt");
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

	public static void queryData(Dataset spatialDataset) {
		log.info("START");
		long startTime = System.nanoTime();
		String pre = StrUtils.strjoinNL("PREFIX : <http://example/>",
				"PREFIX spatial: <http://jena.apache.org/spatial#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");

		System.out.println("nearby");
		String qs = StrUtils.strjoinNL("SELECT * ",
				" { ?s spatial:nearby (51.3000 0.0 10.0 'miles') ;",
				"      rdfs:label ?label", " }");

		spatialDataset.begin(ReadWrite.READ);
		int size = 0;
		try {
			Query q = QueryFactory.create(pre + "\n" + qs);
			QueryExecution qexec = QueryExecutionFactory.create(q, spatialDataset);
			//QueryExecUtils.executeQuery(q, qexec);
			ResultSet results = qexec.execSelect() ;
		    
		    while(results.hasNext())
		    {
		    	results.next();
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
