/**
 * 
 */
package unbbayes.io.builder.impl;

import unbbayes.io.builder.INodeBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * @author Shou Matsumoto
 * @see IProbabilisticNetworkBuilder
 *
 */
public class DefaultProbabilisticNodeBuilder implements INodeBuilder {

	/**
	 * Default implementation of builder for probabilistic nodes
	 * @see IProbabilisticNetworkBuilder
	 * @see INodeBuilder
	 */
	protected DefaultProbabilisticNodeBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Construction method for 
	 * default implementation of builder for probabilistic nodes
	 * 
	 * @see IProbabilisticNetworkBuilder
	 * @see INodeBuilder
	 */
	public static DefaultProbabilisticNodeBuilder newInstance(){
		return new DefaultProbabilisticNodeBuilder();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.builder.INodeBuilder#buildNode()
	 */
	public Node buildNode() {
		return new ProbabilisticNode();
	}

}
