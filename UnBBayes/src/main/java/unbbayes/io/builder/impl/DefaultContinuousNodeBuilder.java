/**
 * 
 */
package unbbayes.io.builder.impl;

import unbbayes.io.builder.INodeBuilder;
import unbbayes.io.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.hybridbn.ContinuousNode;

/**
 * @author Shou Matsumoto
 *
 */
public class DefaultContinuousNodeBuilder implements INodeBuilder {

	/**
	 * Default implementation of builder for continuous nodes
	 * 
	 * @see IProbabilisticNetworkBuilder
	 * @see INodeBuilder
	 */
	protected DefaultContinuousNodeBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Construction method for 
	 * default implementation of builder for continuous nodes
	 * 
	 * @see IProbabilisticNetworkBuilder
	 * @see INodeBuilder
	 */
	public static DefaultContinuousNodeBuilder newInstance(){
		return new DefaultContinuousNodeBuilder();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.builder.INodeBuilder#buildNode()
	 */
	public Node buildNode() {
		return new ContinuousNode();
	}

}
