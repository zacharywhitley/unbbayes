/**
 * 
 */
package unbbayes.prs.mebn.prowl2.kb.extension.owlapi;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;


/**
 * This builder creates a {@link unbbayes.prs.mebn.kb.KnowledgeBase} which is an adaptor to {@link org.semanticweb.owlapi.reasoner.OWLReasoner},
 * but it is adapted for PROWL2-specific routines.
 * It overwrites {@link #buildKB(MultiEntityBayesianNetwork, IMEBNMediator)} so that
 * it returns an instance of {@link PROWL2KnowledgeBase}.
 * @author Shou Matsumoto
 *
 */
public class PROWL2KnowledgeBaseBuilder extends OWL2KnowledgeBaseBuilder {

	/**
	 * Default constructor must be public for plug-in support
	 */
	public PROWL2KnowledgeBaseBuilder() {
		super();
		this.setName("DL Reasoner for PR-OWL2");
	}

	/**
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBaseBuilder#buildKB(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public KnowledgeBase buildKB(MultiEntityBayesianNetwork mebn,
			IMEBNMediator mediator) throws InstantiationException {
		return PROWL2KnowledgeBase.getInstance(mebn, mediator);
	}

}
