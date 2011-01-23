/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * I/O classes using PR-OWL2 ontology models should implement this class
 * @author Shou Matsumoto
 *
 */
public interface IPROWL2ModelUser {
	// Some names of the classes in PR_OWL2 definition File
	
	public static final String ARGUMENT = "Argument";
	public static final String CONTEXT_NODE = "ContextNode";
	public static final String DOMAIN_MFRAG = "DomainMFrag";
	public static final String DOMAIN_RESIDENT = "DomainRN";
	public static final String GENERATIVE_INPUT = "GenerativeIN";
	public static final String MTHEORY = "MTheory";	
	public static final String ORDINARY_VARIABLE = "OrdinaryVariable";
	public static final String COND_RELATIONSHIP = "CondRelationship";
	public static final String EXEMPLAR = "Exemplar";
	public static final String MFRAG = "MFrag";
	public static final String NODE = "Node";
	public static final String PROB_ASSIGN = "ProbAssign";
	public static final String PROB_DIST = "ProbDist";
	
	/** This is the default URI of a PR-OWL2 ontology */
	public static final String PROWL2_NAMESPACEURI =  "http://www.pr-owl.org/pr-owl.owl";
	
	/** This is a prefix manager for {@value IPROWL2ModelUser#PROWL2_NAMESPACEURI} */
	public static final PrefixManager PROWL2_DEFAULTPREFIXMANAGER = new DefaultPrefixManager(IPROWL2ModelUser.PROWL2_NAMESPACEURI + '#');
	
	/**
	 * Obtains the default prefix manager, which will be used in order to extract classes by name/ID.
	 * If ontology == null, it returns {@link #PROWL2_DEFAULTPREFIXMANAGER} 
	 * (thus, if ontology == null, it returns a PR-OWL ontology prefix).
	 * @param ontology : ontology being read.
	 * @return a prefix manager or null if it could not be created at all
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology);
	
	/**
	 * Extracts a user-friendly name from an OWL object
	 * @param owlObject
	 * @return
	 */
	public String extractName(OWLObject owlObject);
	
}
