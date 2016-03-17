package unbbayes.prs.mebn.kb.extension.triplestore;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder;

public class TriplestoreKnowledgeBaseBuilder implements IKnowledgeBaseBuilder{

	private String builderName = "Triplestore Base";
	
	/**
	 * Default constructor must be public for plug-in support
	 */
	public TriplestoreKnowledgeBaseBuilder() {
		super();
		this.setName("Triplestore Reasoner for PR-OWL2 RL");
	}

	/**
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBaseBuilder#buildKB(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public KnowledgeBase buildKB(MultiEntityBayesianNetwork mebn,
			IMEBNMediator mediator) throws InstantiationException {
		return TriplestoreKnowledgeBase.getInstance(mebn, mediator);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder#buildKB()
	 */
	@Deprecated
	public KnowledgeBase buildKB() throws InstantiationException {
		return this.buildKB(null,null);
	}

	public String getName() {
		return builderName;
	}

	public void setName(String name) {
		builderName = name;
	}
	
}

