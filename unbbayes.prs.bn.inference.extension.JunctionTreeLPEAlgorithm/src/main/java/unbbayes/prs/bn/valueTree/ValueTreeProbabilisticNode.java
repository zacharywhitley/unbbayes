package unbbayes.prs.bn.valueTree;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.extension.IPluginNode;
import unbbayes.util.Debug;

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
		super();
		this.getProbabilityFunction().addVariable(this);
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

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#toString()
	 */
	public String toString() {
		String ret = getName() + "[";
		int stateSize = getStatesSize();
		for (int i = 0; i < stateSize; i++) {
			try {
				ret += getStateAt(i) + "=" + getMarginalAt(i) + "; ";
			} catch (Exception e) {
				Debug.println(getClass(), "Could not extract marginal of node " + getName() + " at index " + i, e);
			}
		}
		ret += "]";
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#marginal()
	 */
	protected void marginal() {
		super.marginal();
		// prepare list (copy) of shadow nodes, so that we can use them as anchors afterwards.
		List<IValueTreeNode> otherShadowNodes = new ArrayList<IValueTreeNode>(getStatesSize());
		for (int i = 0; i < getStatesSize(); i++) {
			otherShadowNodes.add(valueTree.getShadowNode(i));
		}
		// change the probabilities in the shadow nodes as well
		for (int i = 0; i < getStatesSize(); i++) {
			// note: otherShadowNodes can contain valueTree.getShadowNode(i) because it will not be considered as anchor if the node to change and anchor is the same.
			valueTree.changeProb(valueTree.getShadowNode(i), null, getMarginalAt(i), otherShadowNodes);
		}
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
//	 */
//	public float getMarginalAt(int index) {
//		return super.getMarginalAt(index);
//		return valueTree.getProb(valueTree.getShadowNode(index), null);
//	}
	
	

}
