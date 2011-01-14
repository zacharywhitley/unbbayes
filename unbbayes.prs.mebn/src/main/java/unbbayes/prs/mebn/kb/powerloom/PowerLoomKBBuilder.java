/**
 * 
 */
package unbbayes.prs.mebn.kb.powerloom;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder;

/**
 * This class is an adapter for MEBN KB plugin.
 * It enables plugin support for Power Loom Knowledge base.
 * @author Shou Matsumoto
 *
 */
public class PowerLoomKBBuilder implements IKnowledgeBaseBuilder {

	private String name = "Power Loom";
	
	/**
	 * Default constructor.
	 */
	public PowerLoomKBBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.jpf.IKnowledgeBaseBuilder#buildKB()
	 */
	@Deprecated
	public KnowledgeBase buildKB() throws InstantiationException {
		return this.buildKB(null, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder#buildKB(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public KnowledgeBase buildKB(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) throws InstantiationException {
		return PowerLoomKB.getNewInstanceKB();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.jpf.IKnowledgeBaseBuilder#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.jpf.IKnowledgeBaseBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

}
