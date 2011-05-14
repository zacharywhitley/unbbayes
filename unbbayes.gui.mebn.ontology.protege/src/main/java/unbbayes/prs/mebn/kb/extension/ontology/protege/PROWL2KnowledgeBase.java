/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.ontology.protege;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.mebn.owlapi.DefaultNonPROWL2ClassExtractor;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * This class extends {@link OWL2KnowledgeBase} in order to fulfill
 * PR-OWL2 specific reasoning requirements.
 * @author Shou Matsumoto
 *
 */
public class PROWL2KnowledgeBase extends OWL2KnowledgeBase {

	/**
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	public PROWL2KnowledgeBase() {
		super();
		// change prowl class extractor so that it uses PROWL2-specific routines
		this.setNonPROWLClassExtractor(DefaultNonPROWL2ClassExtractor.getInstance());
	}
	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(null, mebn, mediator);
	}

	/**
	 * Constructor method initializing fields
	 * @param reasoner : explicitly sets the value of {@link #getDefaultOWLReasoner()}; If null, {@link #getDefaultOWLReasoner()} will be extracted from {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * of {@link #getDefaultMEBN()}.
	 * @param mebn : this value will be set to {@link #setDefaultMEBN(MultiEntityBayesianNetwork)}
	 * @param mediator : this value will be set to {@link #setDefaultMediator(IMEBNMediator)}
	 * @return
	 */
	public static KnowledgeBase getInstance(OWLReasoner reasoner, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		OWL2KnowledgeBase ret = new PROWL2KnowledgeBase();
		ret.setDefaultOWLReasoner(reasoner);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}
}
