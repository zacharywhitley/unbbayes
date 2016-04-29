

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser.DatatypeHandling;

/**
 * <p>
 * This sample application is intended to illustrate how to prepare, configure and run a <a
 * href="http://www.openrdf.org">Sesame</a> repository using the <a
 * href="http://www.ontotext.com/owlim/">OWLIM</a> SAIL. The basic operations are demonstrated in separate
 * methods: 
 * page namespaces, 
 * evaluate queries, 
 * add and delete statements, 
 * save and load files, etc.
 * </p>
 * <p>
 * Addition and removal are performed only when the input parameter 'updates' is set to 'true'. Thus,
 * potentially slow and irrelevant delete operations are avoided in case the example is adapted for loading
 * large data-sets.
 * </p>
 * <p>
 * This application can be used also as an easy test-bed for loading and querying different ontologies and
 * data-sets without needing to build a separate application.
 * </p>
 * <p>
 * The command line parameters are given as key=value' pairs.
 * A full list of parameters is given in the online OWLIM documentation at http://owlim.ontotext.com 
 * </p>
 * 
 * Copyright (c) 2005-2012 Ontotext AD
 */
public class TripleStoreConnectionTest {
	
	// Command line parameters
	public static String PARAM_CONFIG = "config";
	public static String PARAM_URL = "url";
	public static String PARAM_REPOSITORY = "repository";
	public static String PARAM_USERNAME = "username";
	public static String PARAM_PASSWORD = "password";

	// Query and miscellaneous parameters
	public static String PARAM_QUERYFILE = "queryfile";
	public static String PARAM_SHOWRESULTS = "showresults";
	public static String PARAM_SHOWSTATS = "showstats";
	public static String PARAM_UPDATES = "updates";

	// Export parameters
	public static String PARAM_EXPORT_FILE = "exportfile";
	public static String PARAM_EXPORT_FORMAT = "exportformat";
	public static String PARAM_EXPORT_TYPE = "exporttype";
	
	// Loading parameters
	public static String PARAM_PRELOAD = "preload";
	public static String PARAM_CONTEXT = "context";
	public static String PARAM_VERIFY = "verify";
	public static String PARAM_STOP_ON_ERROR = "stoponerror";
	public static String PARAM_PRESERVE_BNODES = "preservebnodes";
	public static String PARAM_DATATYPE_HANDLING = "datatypehandling";
	public static String PARAM_CHUNK_SIZE = "chunksize";

	private static SimpleDateFormat logTimestamp = new SimpleDateFormat("HH:mm:ss ");

	private static void log(String message) {
		System.out.println(logTimestamp.format(new Date()) + message);
	}

	/**
	 * This is the entry point of the example application. First, the command-line parameters are initialized.
	 * Then these parameters are passed to an instance of the GettingStarted application and used to create,
	 * Initialize and login to the local instance of Sesame.
	 * 
	 * @param args
	 *            Command line parameters
	 */
	public static void main(String[] args) {
		
//		testSampleRepository(args); 
//		testLUBMRepository(args);
		testLUBMWebRepository(args); 

	}

	private static void testSampleRepository(String[] args){
		// Special handling for JAXP XML parser that limits entity expansion
		// see
		// http://java.sun.com/j2se/1.5.0/docs/guide/xml/jaxp/JAXP-Compatibility_150.html#JAXP_security
		System.setProperty("entityExpansionLimit", "1000000");

		// Parse all the parameters
		Parameters params = new Parameters(args);

		// Set default values for missing parameters
		params.setDefaultValue(PARAM_CONFIG, "./owlim.ttl");
		
		params.setDefaultValue(PARAM_SHOWRESULTS, "true");
		params.setDefaultValue(PARAM_SHOWSTATS, "false");
		params.setDefaultValue(PARAM_UPDATES, "false");
		params.setDefaultValue(PARAM_QUERYFILE, "./queries/sample.sparql");
		params.setDefaultValue(PARAM_EXPORT_FORMAT, RDFFormat.NTRIPLES.getName());

		params.setDefaultValue(PARAM_PRELOAD, "./preload");
		params.setDefaultValue(PARAM_VERIFY, "true");
		params.setDefaultValue(PARAM_STOP_ON_ERROR, "true");
		params.setDefaultValue(PARAM_PRESERVE_BNODES, "true");
		params.setDefaultValue(PARAM_DATATYPE_HANDLING, DatatypeHandling.VERIFY.name());
		params.setDefaultValue(PARAM_CHUNK_SIZE, "500000");
		
		log("Using parameters:");
		log(params.toString());

		TriplestoreOld gettingStartedApplication = null;
		try {
			long initializationStart = System.currentTimeMillis();
			
			// The ontologies and datasets specified in the 'import' parameter
			// of the Sesame configuration file are loaded during initialization.
			// Thus, for large datasets the initialisation could take
			// considerable time.
			gettingStartedApplication = new TriplestoreOld(params.getParameters());

			// Demonstrate the basic operations on a repository
			
			//*********************** LOADING ******************************
			gettingStartedApplication.loadFiles();		
			gettingStartedApplication.showInitializationStatistics(System.currentTimeMillis()
					- initializationStart);
	
			//*********************** QUERYING ******************************			
			gettingStartedApplication.iterateNamespaces();
			gettingStartedApplication.evaluateQueries();
			
			//*********************** UPDATING ******************************	
			gettingStartedApplication.insertAndDeleteStatement();
		
			//*********************** EXPORT ******************************	
			gettingStartedApplication.export();
		
		} catch (Throwable ex) {
			log("An exception occured at some point during execution:");
			ex.printStackTrace();
		} finally {
			if (gettingStartedApplication != null)
				gettingStartedApplication.shutdown();
		}
	}
	
	private static void testLUBMRepository(String[] args){
		// Special handling for JAXP XML parser that limits entity expansion
		// see
		// http://java.sun.com/j2se/1.5.0/docs/guide/xml/jaxp/JAXP-Compatibility_150.html#JAXP_security
		System.setProperty("entityExpansionLimit", "1000000");

		// Parse all the parameters
		Parameters params = new Parameters(args);

		// Set default values for missing parameters
		params.setDefaultValue(PARAM_CONFIG, "./owlim.ttl");
		
		params.setDefaultValue(PARAM_SHOWRESULTS, "true");
		params.setDefaultValue(PARAM_SHOWSTATS, "false");
		
		params.setDefaultValue(PARAM_UPDATES, "false");
		
//		params.setDefaultValue(PARAM_QUERYFILE, "./queries/lubmqueries.sparql");
		params.setDefaultValue(PARAM_QUERYFILE, "./queries/lubmqueries2.sparql");
		
		params.setDefaultValue(PARAM_EXPORT_FORMAT, RDFFormat.NTRIPLES.getName());

		params.setDefaultValue(PARAM_PRELOAD, "./preload");
		params.setDefaultValue(PARAM_VERIFY, "true");
		params.setDefaultValue(PARAM_STOP_ON_ERROR, "true");
		params.setDefaultValue(PARAM_PRESERVE_BNODES, "true");
		params.setDefaultValue(PARAM_DATATYPE_HANDLING, DatatypeHandling.VERIFY.name());
		params.setDefaultValue(PARAM_CHUNK_SIZE, "500000");
		
//		params.setDefaultValue(PARAM_URL, "	http://localhost:8080/openrdf-sesame");
//		params.setDefaultValue(PARAM_URL, "	http://localhost:8080/openrdf-workbench");
		
		// Graphdb 6.6.3
		params.setDefaultValue(PARAM_URL, "http://localhost:8080/graphdb-workbench-free");
		
		params.setDefaultValue(PARAM_REPOSITORY, "LUBM1RL");
//		params.setDefaultValue(PARAM_REPOSITORY, "LUBM2-QL");
		
		log("Using parameters:");
		log(params.toString());

		TriplestoreOld gettingStartedApplication = null;
		try {
			long initializationStart = System.currentTimeMillis();
			
			// The ontologies and datasets specified in the 'import' parameter
			// of the Sesame configuration file are loaded during initialization.
			// Thus, for large datasets the initialisation could take
			// considerable time.
			gettingStartedApplication = new TriplestoreOld(params.getParameters());

			
			// Demonstrate the basic operations on a repository
			
			//*********************** LOADING ******************************
//			gettingStartedApplication.loadFiles();		
//			gettingStartedApplication.showInitializationStatistics(System.currentTimeMillis()
//					- initializationStart);
	
			//*********************** QUERYING ******************************			
			gettingStartedApplication.iterateNamespaces();
			gettingStartedApplication.evaluateQueries();
			
			//*********************** UPDATING ******************************	
//			gettingStartedApplication.insertAndDeleteStatement();
		
			//*********************** EXPORT ******************************	
//			gettingStartedApplication.export();
		
		} catch (Throwable ex) {
			log("An exception occured at some point during execution:");
			ex.printStackTrace();
		} finally {
			if (gettingStartedApplication != null)
				gettingStartedApplication.shutdown();
		}
	}
	
	/**
	 * Execute the set of queries in a repository already loaded. 
	 * Conecta através do servidor web (TomCat), que deve estar ligado. 
	 * 
	 * @param args
	 */
	private static void testLUBMWebRepository(String[] args){
		// Special handling for JAXP XML parser that limits entity expansion
		// see
		// http://java.sun.com/j2se/1.5.0/docs/guide/xml/jaxp/JAXP-Compatibility_150.html#JAXP_security
		System.setProperty("entityExpansionLimit", "1000000");

		// Parse all the parameters
		Parameters params = new Parameters(args);

		// Set default values for missing parameters
		params.setDefaultValue(PARAM_CONFIG, "./owlim.ttl");
		
		params.setDefaultValue(PARAM_SHOWRESULTS, "true");
		params.setDefaultValue(PARAM_SHOWSTATS, "false");
		
		params.setDefaultValue(PARAM_UPDATES, "false");
		
//		params.setDefaultValue(PARAM_QUERYFILE, "./queries/lubmqueries.sparql");
		params.setDefaultValue(PARAM_QUERYFILE, "./queries/lubmqueries2.sparql");
		
		params.setDefaultValue(PARAM_EXPORT_FORMAT, RDFFormat.NTRIPLES.getName());

		params.setDefaultValue(PARAM_PRELOAD, "./preload");
		params.setDefaultValue(PARAM_VERIFY, "true");
		params.setDefaultValue(PARAM_STOP_ON_ERROR, "true");
		params.setDefaultValue(PARAM_PRESERVE_BNODES, "true");
		params.setDefaultValue(PARAM_DATATYPE_HANDLING, DatatypeHandling.VERIFY.name());
		params.setDefaultValue(PARAM_CHUNK_SIZE, "500000");
		
		// Old versions 
//		params.setDefaultValue(PARAM_URL, "	http://localhost:8080/openrdf-sesame");
//		params.setDefaultValue(PARAM_URL, "	http://localhost:8080/openrdf-workbench");
		
		// Graphdb 6.6.3
		params.setDefaultValue(PARAM_URL, "http://localhost:8080/graphdb-workbench-free");
		
		params.setDefaultValue(PARAM_REPOSITORY, "LUBM1RL");
//		params.setDefaultValue(PARAM_REPOSITORY, "LUBM2-QL");
		
		log("Using parameters:");
		log(params.toString());

		TriplestoreOld gettingStartedApplication = null;
		try {
			long initializationStart = System.currentTimeMillis();
			
			// The ontologies and datasets specified in the 'import' parameter
			// of the Sesame configuration file are loaded during initialization.
			// Thus, for large datasets the initialisation could take
			// considerable time.
			gettingStartedApplication = new TriplestoreOld(params.getParameters());
	
			//*********************** QUERYING ******************************			
			gettingStartedApplication.iterateNamespaces();
			gettingStartedApplication.evaluateQueries();

		
		} catch (Throwable ex) {
			log("An exception occured at some point during execution:");
			ex.printStackTrace();
		} finally {
			if (gettingStartedApplication != null)
				gettingStartedApplication.shutdown();
		}
	}
	


}
