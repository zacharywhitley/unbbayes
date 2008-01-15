/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * @author Shou Matsumoto
 *
 */
public class Query {
	
	// TODO complete this class
	
	private MultiEntityBayesianNetwork mebn = null;
	private KnowledgeBase kb = null;
	private SSBNNode queryNode = null;
	
	/**
	 * Default query. 
	 */
	public Query(KnowledgeBase kb, SSBNNode queryNode, MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn; 
		this.kb = kb; 
		this.queryNode = queryNode;  
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}

	public MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}

	public void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}

	public SSBNNode getQueryNode() {
		return queryNode;
	}

	public void setQueryNode(SSBNNode queryNode) {
		this.queryNode = queryNode;
	}
	
	

	
}
