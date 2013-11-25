/**
 * 
 */
package edu.gmu.scicast.mebn.kb;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBase;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;

/**
 * This class extends {@link OWL2KnowledgeBase} in order to fulfill
 * requirements for SciCast's MEBN-based question generator.
 * @author Shou Matsumoto
 *
 */
public class TuuyiKnowledgeBase extends PROWL2KnowledgeBase {
	
	private OntologyClient ontologyClient = null;

	/**
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	public TuuyiKnowledgeBase() {
		super();
	}

	/**
	 * Simply delegates to {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator, OntologyClient)},
	 * passing a default {@link OntologyClient}.
	 * @return
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(mebn, mediator, new OntologyClient());
	}
	/**
	 * Constructor method initializing fields
	 * @param mebn : this value will be set to {@link #setDefaultMEBN(MultiEntityBayesianNetwork)}
	 * @param mediator : this value will be set to {@link #setDefaultMediator(IMEBNMediator)}
	 * @return
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator, OntologyClient ontologyClient) {
		TuuyiKnowledgeBase ret = new TuuyiKnowledgeBase();
		ret.setDefaultOWLReasoner(null);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		ret.setOntologyClient(ontologyClient);
		return ret;
	}

	/**
	 * @return the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 */
	public OntologyClient getOntologyClient() {
		return ontologyClient;
	}

	/**
	 * @param ontologyClient : the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 */
	public void setOntologyClient(OntologyClient ontologyClient) {
		this.ontologyClient = ontologyClient;
	}
}
