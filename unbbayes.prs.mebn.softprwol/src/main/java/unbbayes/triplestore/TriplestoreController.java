package unbbayes.triplestore;

import java.util.List;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser.DatatypeHandling;

public class TriplestoreController {

	Triplestore triplestore = null;
	Parameters params = null; 
	
	public TriplestoreController(){
		
	}
	
	public void startConnection(Parameters params){
		
		this.params = params; 
		
		// Special handling for JAXP XML parser that limits entity expansion
		// see
		// http://java.sun.com/j2se/1.5.0/docs/guide/xml/jaxp/JAXP-Compatibility_150.html#JAXP_security
		System.setProperty("entityExpansionLimit", "1000000");

		// Set default values for missing parameters
		params.setDefaultValue(Triplestore.PARAM_CONFIG, "./owlim.ttl");
		
		params.setDefaultValue(Triplestore.PARAM_SHOWRESULTS, "true");
		params.setDefaultValue(Triplestore.PARAM_SHOWSTATS, "false");
		
		params.setDefaultValue(Triplestore.PARAM_UPDATES, "false");
		
//		params.setDefaultValue(PARAM_QUERYFILE, "./queries/lubmqueries.sparql");
		params.setDefaultValue(Triplestore.PARAM_QUERYFILE, "./queries/lubmqueries2.sparql");
		
		params.setDefaultValue(Triplestore.PARAM_EXPORT_FORMAT, RDFFormat.NTRIPLES.getName());

		params.setDefaultValue(Triplestore.PARAM_PRELOAD, "./preload");
		params.setDefaultValue(Triplestore.PARAM_VERIFY, "true");
		params.setDefaultValue(Triplestore.PARAM_STOP_ON_ERROR, "true");
		params.setDefaultValue(Triplestore.PARAM_PRESERVE_BNODES, "true");
		params.setDefaultValue(Triplestore.PARAM_DATATYPE_HANDLING, DatatypeHandling.VERIFY.name());
		params.setDefaultValue(Triplestore.PARAM_CHUNK_SIZE, "500000");
		
		// Old versions 
//		params.setDefaultValue(PARAM_URL, "	http://localhost:8080/openrdf-sesame");
//		params.setDefaultValue(PARAM_URL, "	http://localhost:8080/openrdf-workbench");
		
		// Graphdb 6.6.3
//		params.setDefaultValue(TripleStoreDriver.PARAM_URL, "http://localhost:8080/graphdb-workbench-free");
//		params.setDefaultValue(TripleStoreDriver.PARAM_REPOSITORY, "LUBM1RL");
		
//		params.setDefaultValue(PARAM_REPOSITORY, "LUBM2-QL");

		try {
			long initializationStart = System.currentTimeMillis();
			
			// The ontologies and datasets specified in the 'import' parameter
			// of the Sesame configuration file are loaded during initialization.
			// Thus, for large datasets the initialization could take
			// considerable time.
			triplestore = new Triplestore(params.getParameters());
			Boolean connected = triplestore.connectRemoteRepository(); 
			
			if(connected){
				System.out.println("Connection OK!");
			}else{
				System.out.println("Connection not OK.");
			}
		
			
		} catch (Throwable ex) {
			ex.printStackTrace();
		}		
		
	}
	
	public boolean isConnected(){
		return triplestore.isConnected(); 
	}
	
	public void stopConnection(){
		
		if (triplestore != null){
			triplestore.shutdown();
		}
		
	}
	
	public void iterateNamespaces() throws Exception{
		triplestore.iterateNamespaces();
	}
	
	public List<String[]> executeSelectQuery(String query){
		
		List<String[]> list = triplestore.executeSelectQuery(query); 
		
		return list; 
		
	}
	
	public boolean executeAskQuery(String query){
		return triplestore.executeAskQuery(query); 
	}
}
