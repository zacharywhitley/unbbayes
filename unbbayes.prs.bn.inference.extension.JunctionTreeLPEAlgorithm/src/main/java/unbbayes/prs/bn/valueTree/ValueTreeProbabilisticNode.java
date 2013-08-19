package unbbayes.prs.bn.valueTree;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.extension.IPluginNode;

/**
 * This node works as the shadow node in value trees, so 
 * states of this node are the nodes in value trees visible in public
 * @author Shou Matsumoto
 *
 */
public class ValueTreeProbabilisticNode extends ProbabilisticNode implements IPluginNode {

	private IValueTree valueTree = ValueTree.getInstance(this);
	
	/**
	 * Default constructor is kept public so that plugin infrastructure can instantiate it,
	 */
	public ValueTreeProbabilisticNode() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.extension.IPluginNode#getNode()
	 */
	public Node getNode() {
		return this;
	}

	/**
	 * @return the valueTree
	 */
	public IValueTree getValueTree() {
		return valueTree;
	}

	/**
	 * @param valueTree the valueTree to set
	 */
	public void setValueTree(IValueTree valueTree) {
		this.valueTree = valueTree;
	}

}
