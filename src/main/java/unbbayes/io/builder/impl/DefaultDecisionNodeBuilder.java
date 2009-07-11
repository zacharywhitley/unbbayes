/**
 * 
 */
package unbbayes.io.builder.impl;

import unbbayes.io.builder.INodeBuilder;
import unbbayes.io.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;

/**
 * @author Shou Matsumoto
 * @see IProbabilisticNetworkBuilder
 *
 */
public class DefaultDecisionNodeBuilder implements INodeBuilder {

	/**
	 * Default implementation of builder for decision nodes
	 * 
	 * @see IProbabilisticNetworkBuilder
	 * @see INodeBuilder
	 */
	protected DefaultDecisionNodeBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Construction method for 
	 * default implementation of builder for decision nodes
	 * 
	 * @see IProbabilisticNetworkBuilder
	 * @see INodeBuilder
	 */
	public static DefaultDecisionNodeBuilder newInstance(){
		return new DefaultDecisionNodeBuilder();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.builder.INodeBuilder#buildNode()
	 */
	public Node buildNode() {
		return new DecisionNode();
	}

}
