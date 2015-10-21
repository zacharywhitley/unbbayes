/**
 * 
 */
package unbbayes.io.medg.owlapi;

import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;

/**
 * This interface extends {@link IPROWL2ModelUser} in order
 * to add support for PR-OWL 2 decision profile.
 * @author Shou Matsumoto
 *
 */
public interface IPROWL2DecisionModelUser extends IPROWL2ModelUser {
	
	/** This is the default URI of a PR-OWL2 ontology */
	public static final String PROWL2_DECISION_NAMESPACEURI =  "http://www.pr-owl.org/pr-owl2-decision.owl";
	
}
