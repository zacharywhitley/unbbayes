/**
 * 
 */
package edu.gmu.scicast.mebn.kb;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBaseBuilder;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;


/**
 * This builder creates a {@link unbbayes.prs.mebn.kb.KnowledgeBase} which is an adaptor to {@link OntologyClient},
 * but it is adapted for PROWL2-specific routines in order to be able to automatically generate questions 
 * for the scicast project, based on MEBN reasoning.
 * It overwrites {@link #buildKB(MultiEntityBayesianNetwork, IMEBNMediator)} so that
 * it returns an instance of {@link TuuyiKnowledgeBase}.
 * @author Shou Matsumoto
 *
 */
public class TuuyiDataBaseBuilder extends OWL2KnowledgeBaseBuilder {

	/**
	 * Default constructor must be public for plug-in support
	 */
	public TuuyiDataBaseBuilder() {
		super();
		this.setName("Tuuyi Ontology");
	}

	/**
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBaseBuilder#buildKB(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public KnowledgeBase buildKB(MultiEntityBayesianNetwork mebn,
			IMEBNMediator mediator) throws InstantiationException {
		return TuuyiKnowledgeBase.getInstance(mebn, mediator);
	}

}
