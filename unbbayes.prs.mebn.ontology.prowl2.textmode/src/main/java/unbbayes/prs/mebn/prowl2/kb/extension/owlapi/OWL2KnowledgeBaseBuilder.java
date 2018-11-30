/**
 * 
 */
package unbbayes.prs.mebn.prowl2.kb.extension.owlapi;


import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder;

/**
 * This builder creates a {@link KnowledgeBase} which is an adaptor to {@link org.semanticweb.owlapi.reasoner.OWLReasoner}
 * @author Shou Matsumoto
 *
 */
public class OWL2KnowledgeBaseBuilder implements IKnowledgeBaseBuilder {

	private String builderName = "OWL2 Reasoner";
	
	/**
	 * Default constructor must be public for plug-in support
	 */
	public OWL2KnowledgeBaseBuilder() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * It builds an {@link OWL2KnowledgeBase}.
	 * Users of this method should be cautious, because this method sets the value of {@link OWL2KnowledgeBase#getDefaultOWLReasoner()}
	 * as the one obtained from {@link MultiEntityBayesianNetwork#getStorageImplementor()} (the objects are the same), but that does not mean that changing the reasoner of
	 * {@link MultiEntityBayesianNetwork#getStorageImplementor()} will also change {@link OWL2KnowledgeBase#getDefaultOWLReasoner()} (this kind
	 * of synchronization is not possible by mere object reference - because the object itself is replaced - so such consistency check should be
	 * done externally).
	 * @see unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder#buildKB(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public KnowledgeBase buildKB(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) throws InstantiationException {
		return OWL2KnowledgeBase.getInstance(mebn, mediator);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder#getName()
	 */
	public String getName() {
		return builderName;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		builderName = name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder#buildKB()
	 */
	@Deprecated
	public KnowledgeBase buildKB() throws InstantiationException {
		return this.buildKB(null,null);
	}

}
