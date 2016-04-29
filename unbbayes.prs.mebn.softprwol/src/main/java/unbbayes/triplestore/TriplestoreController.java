package unbbayes.triplestore;

import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.mebn.extension.kb.triplestore.DatabaseStatusObserver;
import unbbayes.triplestore.exception.InvalidQuerySintaxException;
import unbbayes.triplestore.exception.TriplestoreQueryEvaluationException;
import unbbayes.triplestore.exception.TriplestoreException;

import unbbayes.util.Parameters;

public class TriplestoreController implements DatabaseStatusObservable {	

	Triplestore triplestore = null;
	Parameters params = null; 
	
	private List<DatabaseStatusObserver> listenerList = 
			new ArrayList<DatabaseStatusObserver>(); 
	
	public TriplestoreController(){
		
	}
	
	public void startConnection(Parameters params) throws TriplestoreException{
		
		this.params = params; 
		
		// Special handling for JAXP XML parser that limits entity expansion
		// see
		// http://java.sun.com/j2se/1.5.0/docs/guide/xml/jaxp/JAXP-Compatibility_150.html#JAXP_security
		System.setProperty("entityExpansionLimit", "1000000");

		long initializationStart = System.currentTimeMillis();

		// The ontologies and datasets specified in the 'import' parameter
		// of the Sesame configuration file are loaded during initialization.
		// Thus, for large datasets the initialization could take
		// considerable time.
		triplestore = new SAILTriplestore(params);
		Boolean connected = triplestore.connectRemoteRepository(); 

		if(connected){
			System.out.println("Connection OK!");
		}else{
			System.out.println("Connection not OK.");
		}

		notifyListeners(); 

	}
	
	public boolean isConnected(){
		return triplestore.isConnected(); 
	}
	
	public void stopConnection() throws TriplestoreException{
		
		if (triplestore != null){
			triplestore.shutdown();
		}
		
	}
	
	public void iterateNamespaces() throws Exception{
		triplestore.iterateNamespaces();
	}
	
	public List<String[]> executeSelectQuery(String query) throws InvalidQuerySintaxException, TriplestoreException, TriplestoreQueryEvaluationException{
		List<String[]> list = triplestore.executeSelectQuery(query); 
		return list; 
	}
	
	public Triplestore getTriplestore() {
		return triplestore;
	}

	public void setTriplestore(Triplestore triplestore) {
		this.triplestore = triplestore;
	}
	
	public boolean executeAskQuery(String query) throws InvalidQuerySintaxException, TriplestoreQueryEvaluationException, TriplestoreException{
		return triplestore.executeAskQuery(query); 
	}
	

	@Override
	public void atach(DatabaseStatusObserver observer) {
		this.listenerList.add(observer); 
	}

	@Override
	public void detach(DatabaseStatusObserver observer) {
		this.listenerList.remove(observer); 
	}

	@Override
	public void notifyListeners() {
		for(DatabaseStatusObserver o: this.listenerList){
			o.update(this.isConnected());
		}
	}
	
}

