package unbbayes.triplestore;

import java.util.List;

import unbbayes.triplestore.exception.InvalidQuerySintaxException;
import unbbayes.triplestore.exception.TriplestoreException;
import unbbayes.triplestore.exception.TriplestoreQueryEvaluationException;

/**
 * Connect to a triplestore using ***Sesame SAIL*** interface and made operations (queries, inserts, updates, deletes). 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public interface Triplestore{
	
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
	public static String PARAM_QUERY_LANGUAGE = "querylanguage"; 
	
	public void setParameterValue(String parameter, String value); 
	
	public String getRepositoryURI(); 
	
	public boolean connectRemoteRepository() throws TriplestoreException; 
	
	public boolean isConnected(); 
	
	public void shutdown() throws TriplestoreException; 
	
	public void iterateNamespaces() throws Exception; 
	
	public  List<String[]> executeSelectQuery(String query) throws InvalidQuerySintaxException, 
                          TriplestoreException, TriplestoreQueryEvaluationException; 
	
	public boolean executeAskQuery(String query) throws InvalidQuerySintaxException, 
    TriplestoreException, TriplestoreQueryEvaluationException;  
	
}