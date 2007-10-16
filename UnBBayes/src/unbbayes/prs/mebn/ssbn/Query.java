/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.Collection;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * @author Shou Matsumoto
 *
 */
public class Query {
	
	private MultiEntityBayesianNetwork mebn = null;
	private KnowledgeBase kb = null;
	private SSBNNode queryNode = null;
	
	/**
	 * Default query. 
	 */
	public Query(MultiEntityBayesianNetwork mebn, KnowledgeBase kb, SSBNNode queryNode) {
		this.mebn = mebn; 
		this.kb = kb; 
		this.queryNode = queryNode;  
	}

}
