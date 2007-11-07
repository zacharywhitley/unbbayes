/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.Collection;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;

/**
 * @author Shou Matsumoto
 *
 */
public class Query {
	
	// TODO complete this class
	
	private MultiEntityBayesianNetwork mebn = null;
	private KBFacade kb = null;
	private SSBNNode queryNode = null;
	
	/**
	 * Default query. 
	 */
	public Query(KBFacade kb, SSBNNode queryNode) {
		//this.mebn = mebn; 
		this.kb = kb; 
		this.queryNode = queryNode;  
	}

	public KBFacade getKb() {
		return kb;
	}

	public void setKb(KBFacade kb) {
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
