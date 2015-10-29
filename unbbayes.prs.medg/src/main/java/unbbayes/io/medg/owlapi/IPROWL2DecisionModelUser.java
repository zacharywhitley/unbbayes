/**
 * 
 */
package unbbayes.io.medg.owlapi;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.io.medg.IPROWLDecisionModelUser;

/**
 * This interface extends {@link IPROWL2ModelUser} in order
 * to add support for PR-OWL 2 decision profile.
 * @author Shou Matsumoto
 *
 */
public interface IPROWL2DecisionModelUser extends IPROWL2ModelUser, IPROWLDecisionModelUser {
	

	public static final String PROWL2_DECISION_URI = "http://www.pr-owl.org/pr-owl2-decision.owl";

	/** This is a prefix manager for {@value #PROWL2_DECISION_URI} */
	public static final PrefixManager PROWL2_DECISION_DEFAULTPREFIXMANAGER = new DefaultPrefixManager(PROWL2_DECISION_URI + '#');
	
	/** This is the name of the OWL class which represents a decision node in PR-OWL 2 Decision profile */
	public static final String DOMAINDECISION = "DomainDecisionNode";
	
	/** This is the name of the OWL class which represents a utility node in PR-OWL 2 Decision profile */
	public static final String DOMAINUTILITY = "DomainUtilityNode";
	
	
	
	
}
