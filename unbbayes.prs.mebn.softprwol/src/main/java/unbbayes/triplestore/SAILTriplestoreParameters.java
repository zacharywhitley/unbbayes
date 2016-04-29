package unbbayes.triplestore;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser.DatatypeHandling;

import unbbayes.util.Parameters;

public class SAILTriplestoreParameters extends Parameters{
	
	public SAILTriplestoreParameters(){
		//Default parameters values
		
		addParameter(Triplestore.PARAM_CONFIG, "./owlim.ttl");
		
		addParameter(Triplestore.PARAM_SHOWRESULTS, "true");
		addParameter(Triplestore.PARAM_SHOWSTATS, "false");
		
		addParameter(Triplestore.PARAM_UPDATES, "false");
		
		addParameter(Triplestore.PARAM_QUERYFILE, "");
		
		addParameter(Triplestore.PARAM_EXPORT_FORMAT, RDFFormat.NTRIPLES.getName());
		addParameter(Triplestore.PARAM_EXPORT_FILE, "");
		addParameter(Triplestore.PARAM_EXPORT_TYPE, "");

		addParameter(Triplestore.PARAM_PRELOAD, "./preload");
		addParameter(Triplestore.PARAM_VERIFY, "true");
		addParameter(Triplestore.PARAM_CONTEXT , "");
		addParameter(Triplestore.PARAM_STOP_ON_ERROR, "true");
		addParameter(Triplestore.PARAM_PRESERVE_BNODES, "true");
		addParameter(Triplestore.PARAM_DATATYPE_HANDLING, DatatypeHandling.VERIFY.name());
		addParameter(Triplestore.PARAM_CHUNK_SIZE, "500000");
		
		addParameter(Triplestore.PARAM_REPOSITORY, "");
		addParameter(Triplestore.PARAM_URL, "");
		addParameter(Triplestore.PARAM_USERNAME, "");
		addParameter(Triplestore.PARAM_PASSWORD, "");
		
		addParameter(Triplestore.PARAM_QUERY_LANGUAGE, "SPARQL");
	}
	
}
