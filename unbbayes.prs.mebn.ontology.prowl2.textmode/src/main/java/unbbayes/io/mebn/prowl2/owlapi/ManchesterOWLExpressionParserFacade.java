package unbbayes.io.mebn.prowl2.owlapi;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * This is the default implementation of {@link IOWLClassExpressionParserFacade},
 * which parses expressions (OWL class expression) in Manchester-OWL syntax,
 * similar to the ones used in DL-query tab of protege.
 * @author Shou Matsumoto
 */
public class ManchesterOWLExpressionParserFacade implements IOWLClassExpressionParserFacade {
	

	private Logger logger = Logger.getLogger(getClass());
	

	private Map<String, Map<String, OWLClassExpression>> ontologyClassExpressionCache = new HashMap<String, Map<String, OWLClassExpression>>();


	private OWLOntology ontology;


	/**
	 * Default constructor is kept protected to allow subclasses to extend
	 * this class easily, but also to make sure not to let other classes to 
	 * instantiate this class directly.
	 * @deprecated use {@link #getInstance(OWLOntology)} instead.
	 */
	protected ManchesterOWLExpressionParserFacade() {}
	


//	/**
//	 * Default constructor (initializing attributes) is kept protected to allow subclasses to extend
//	 * this class easily, but also to make sure not to let other classes to 
//	 * instantiate this class directly.
//	 * @param ontology
//	 * @deprecated use {@link #getInstance(OWLOntology)} instead.
//	 */
//	protected ManchesterOWLExpressionParserFacade(OWLOntology ontology) {
//		super();
//		this.ontology = ontology;
//	}
	
	
	/**
	 * Default constructor method for {@link ManchesterOWLExpressionParserFacade}.
	 * @return a new instance of {@link ManchesterOWLExpressionParserFacade}.
	 */
	public static IOWLClassExpressionParserFacade getInstance(OWLOntology ontology) {
		ManchesterOWLExpressionParserFacade ret = new ManchesterOWLExpressionParserFacade();
		ret.setOntology(ontology);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.prowl2.owlapi.IOWLClassExpressionParserFacade#parseExpression(java.lang.String)
	 */
	@Override
	public OWLClassExpression parseExpression(String expression) {
		
		if (getOntology() == null) {
			throw new IllegalStateException("Unable to parse expression " 
											+ expression + ", because the ontology is null. Please, set the ontology to a non-null value.");
		}
		
		// check cache
		String ontologyCacheKey = getOntology().getOntologyID().getOntologyIRI().toString();
		Map<String, OWLClassExpression> cache = getOntologyClassExpressionCache().get(ontologyCacheKey );
		if (cache == null) {
			cache = new HashMap<String, OWLClassExpression>();
		} 
		if (cache.containsKey(expression)) {
			getLogger().debug(expression + " is cached. Returning cache...");
			return cache.get(expression);
		}
		
		ManchesterOWLSyntaxParser parser = buildParser(getOntology(), expression);
		
		// finally, parse the expression. This line will throw exception when called twice (apparently, the expression is completely consumed when this method is called).
		OWLClassExpression classExpression = parser.parseClassExpression();
		
		// update cache
		cache.put(expression, classExpression);
		getOntologyClassExpressionCache().put(ontologyCacheKey, cache);
		
		return classExpression;
		
	
	}
	

	/**
	 * This method instantiates a new instance of {@link ManchesterOWLSyntaxParser}
	 * to be used by {@link #parseExpression(String)}
	 * @param ontology : root ontology containing OWL entities used in the expression
	 * @param expression : parser will be instantiated with this expression to be parsed.
	 * @return parser for manchester owl syntax
	 */
	protected ManchesterOWLSyntaxParser buildParser(OWLOntology ontology, String expression) {
		try {
			getLogger().debug("Using OWLAPI's manchester owl syntax to parse expression: " + expression);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// set up the parser
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setDefaultOntology(ontology);
		parser.setStringToParse(expression);
		
		// this should enable support for short names/forms
		BidirectionalShortFormProvider shortFormProvider = new BidirectionalShortFormProviderAdapter(
				ontology.getOWLOntologyManager(), ontology.getImportsClosure(), new SimpleShortFormProvider());
		
		// set a entity/syntax checker that uses the above short form map
		parser.setOWLEntityChecker(new ShortFormEntityChecker(shortFormProvider));
		
		return parser;
	}
	


	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return the ontologyClassExpressionCache
	 */
	public Map<String, Map<String, OWLClassExpression>> getOntologyClassExpressionCache() {
		return ontologyClassExpressionCache;
	}

	/**
	 * @param ontologyClassExpressionCache the ontologyClassExpressionCache to set
	 */
	public void setOntologyClassExpressionCache(Map<String, Map<String, OWLClassExpression>> ontologyClassExpressionCache) {
		this.ontologyClassExpressionCache = ontologyClassExpressionCache;
	}



	/**
	 * @return the ontology to be used in {@link #parseExpression(String)} to check consistency 
	 * (e.g. making sure that only existing OWL entities are used in the expressions)
	 */
	public OWLOntology getOntology() {
		return ontology;
	}



	/**
	 * @param the ontology to be used in {@link #parseExpression(String)} to check consistency 
	 * (e.g. making sure that only existing OWL entities are used in the expressions)
	 */
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
		getOntologyClassExpressionCache().clear();
	}

	

}
