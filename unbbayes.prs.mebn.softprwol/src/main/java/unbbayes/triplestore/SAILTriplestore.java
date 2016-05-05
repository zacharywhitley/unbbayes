package unbbayes.triplestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.debug.Debug;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;

import unbbayes.triplestore.exception.InvalidQuerySintaxException;
import unbbayes.triplestore.exception.TriplestoreException;
import unbbayes.triplestore.exception.TriplestoreQueryEvaluationException;
import unbbayes.util.Parameters;

public class SAILTriplestore implements Triplestore{

	Parameters parameters; 
	
	private String url = "";
	private String repositoryId = "";
	
	// The repository manager
	private RepositoryManager repositoryManager;

	// From repositoryManager.getRepository(...) - the actual repository we will
	// work with
	private Repository repository;

	// From repository.getConnection() - the connection through which we will
	// use the repository
	private RepositoryConnection repositoryConnection;
	
	// A map of namespace-to-prefix
	private Map<String, String> namespacePrefixes = new HashMap<String, String>();
	
	private QueryLanguage queryLanguage; 
	
	boolean isConnected = false; 
	
	public SAILTriplestore(Parameters parameters){
		this.parameters = parameters; 
		
		queryLanguage = QueryLanguage.SPARQL; 
	}

	@Override
	public void setParameterValue(String parameter, String value) {
		this.parameters.setParameterValue(parameter, value); 
	}

	@Override
	public boolean connectRemoteRepository() throws TriplestoreException{
		
		this.url = parameters.getParameterValue(PARAM_URL);

		this.repositoryId = parameters.getParameterValue(PARAM_REPOSITORY);

		if ((repositoryId == null) || (repositoryId.equals(""))) {
			throw new TriplestoreException("Can't connect to repository. No repository ID specified.");
		}
		
		try {
			RemoteRepositoryManager remoteRepositoryManager = new RemoteRepositoryManager(url);

			String username = parameters.getParameterValue(PARAM_USERNAME);
			String password = parameters.getParameterValue(PARAM_PASSWORD);

			if (username != null || password != null) {
				if (username == null)
					username = "";
				if (password == null)
					password = "";
				remoteRepositoryManager.setUsernameAndPassword(username, password);
			}

			repositoryManager = remoteRepositoryManager;
			repositoryManager.initialize();

			repository = repositoryManager.getRepository(repositoryId);

			if(repository == null){
				throw new TriplestoreException("Unable to establish a connection with the repository " + repositoryId + "."); 
			}
			
			repositoryConnection = repository.getConnection();

			RepositoryResult<Namespace> iter = repositoryConnection.getNamespaces();

			while (iter.hasNext()) {
				Namespace namespace = iter.next();
				String prefix = namespace.getPrefix();
				String name = namespace.getName();
				namespacePrefixes.put(name, prefix);
				Debug.println(prefix + ":\t" + name);
			}
			iter.close();
			
			isConnected = true; 

		} catch (RepositoryException e) {
			throw new TriplestoreException("Unable to establish a connection with the server \n'" 
		                                   + parameters.getParameterValue(PARAM_URL) + "'.");
		} catch (RepositoryConfigException e) {
			throw new TriplestoreException("Unable to establish a connection with the server \n'" 
		                                   + parameters.getParameterValue(PARAM_URL) + "'.");
		}
		
		return isConnected; 
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void shutdown() throws TriplestoreException {
		Debug.println("===== Shutting down ==========");
		if (repository != null) {
			try {
				repositoryConnection.close();
				repository.shutDown();
				repositoryManager.shutDown();
				isConnected = false; 
			} catch (Exception e) {
				throw new TriplestoreException("An exception occurred during shutdown: " + e.getMessage());
			}
		}
	}

	@Override
	public void iterateNamespaces() throws Exception {
		Debug.println("===== Namespace List ==================================");

		Debug.println("Namespaces collected in the repository:");
		RepositoryResult<Namespace> iter = repositoryConnection.getNamespaces();

		while (iter.hasNext()) {
			Namespace namespace = iter.next();
			String prefix = namespace.getPrefix();
			String name = namespace.getName();
			Debug.println(prefix + ":\t" + name);
		}
		iter.close();
	}

	/**
	 * 
	 * @param query
	 * @return null if an error occur; a List of results otherside 
	 * @throws InvalidQuerySintaxException
	 * @throws TriplestoreException
	 * @throws TriplestoreQueryEvaluationException
	 */
	public List<String[]> executeSelectQuery(String query) 
			throws InvalidQuerySintaxException, 
			       TriplestoreException, 
			       TriplestoreQueryEvaluationException {

		List<String[]> resultList = new ArrayList<String[]>(); 

		TupleQuery preparedOperation = (TupleQuery)prepareSelectQueryOperation(query);

		if (preparedOperation == null) {
			Debug.println("Unable to parse query: " + query);
			return null;
		}

		long queryBegin = System.nanoTime();

		TupleQueryResult result;
		int rows = 0;
		
		try {
			result = preparedOperation.evaluate();
			int numColumns = 0; 

			while (result.hasNext()) {
				BindingSet tuple = result.next();

				//Headers of each column of result 
				if (rows == 0) {
					for (Iterator<Binding> iter = tuple.iterator(); iter.hasNext();) {
						System.out.print(iter.next().getName()); //Name of column of graph 
						System.out.print("\t");
						numColumns++; 
					}
					System.out.println();
					System.out.println("---------------------------------------------");
				}
				rows++;

				String[] singleResult = new String[numColumns]; 
				int i = 0; 
				for (Iterator<Binding> iter = tuple.iterator(); iter.hasNext();) {
					try {
						Value value = iter.next().getValue(); 
						System.out.print(formatRDFValue(value) + "\t");

						if(value instanceof URI){
							URI u = (URI) value;
							singleResult[i] = u.toString(); 
							i++; 
						}else{
							if(value instanceof Literal){
								Literal l = (Literal) value; 
								singleResult[i] = l.getLabel(); 
								i++; 
							}else{
								//TODO think other form of error. An exception? 
								Debug.println("Invalid query return!");
								return null;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				resultList.add(singleResult); 
				System.out.println("");

			}
			
			result.close();

		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
			throw new TriplestoreQueryEvaluationException(e1.getMessage()); 
		}

		long queryEnd = System.nanoTime();
		Debug.println(rows + " result(s) in " + (queryEnd - queryBegin) / 1000000 + "ms.");

		return resultList; 
	}

	public boolean executeAskQuery(String query) throws TriplestoreQueryEvaluationException, InvalidQuerySintaxException, TriplestoreException{

		boolean result = false; 

		BooleanQuery preparedOperation = null;

		preparedOperation = (BooleanQuery)prepareAskQueryOperation(query);

		if (preparedOperation == null) {
			Debug.println("Unable to parse query: " + query);
			return result;
		}

		try {
			result = preparedOperation.evaluate();
		} catch (QueryEvaluationException e) {
			throw new TriplestoreQueryEvaluationException(e.getMessage()); 
		}

		return result; 
	}
	
	private TupleQuery prepareSelectQueryOperation(String query) throws TriplestoreException, InvalidQuerySintaxException{

		TupleQuery result = null;

		try {
			result = repositoryConnection.prepareTupleQuery(
					queryLanguage, query);
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new TriplestoreException(e.getMessage()); 
		} catch (MalformedQueryException e) {
			e.printStackTrace();
			throw new InvalidQuerySintaxException(e.getMessage()); 
		}

		return result; 
	}

	private BooleanQuery prepareAskQueryOperation(String query) throws TriplestoreException, InvalidQuerySintaxException{

		BooleanQuery result = null;

		try {
			result = repositoryConnection.prepareBooleanQuery(
					queryLanguage, query);
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new TriplestoreException(e.getMessage()); 
		} catch (MalformedQueryException e) {
			e.printStackTrace();
			throw new InvalidQuerySintaxException(e.getMessage()); 
		}

		return result; 

	}
	
	/**
	 * Auxiliary method, printing an RDF value in a "fancy" manner. In case of URI, qnames are printed for
	 * better readability. 
	 * 
	 * @param value
	 *            The value to format
	 */
	private String formatRDFValue(Value value) throws Exception {
		if (value instanceof URI) {
			URI u = (URI) value;
			String namespace = u.getNamespace();
			String prefix = namespacePrefixes.get(namespace);
			if (prefix == null) {
				prefix = u.getNamespace();
			} else {
				prefix += ":";
			}
			return prefix + u.getLocalName();
		} else {
			return value.toString();
		}
	}

	@Override
	public String getRepositoryURI() {
		if((this.url != null) && (!this.url.equals("")) && (this.repositoryId != null)){
			return this.url + this.repositoryId;
		}else{
			return ""; 
		}
	}
	

}
