package unbbayes.prs.bn.valueTree;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.extension.IPluginNode;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;

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
//		for (int i = 0; i < getStatesSize(); i++) {
//			otherShadowNodes.add(valueTree.getShadowNode(i));
//		}
		// change the probabilities in the shadow nodes as well
		for (int i = 0; i < getStatesSize(); i++) {
			// TODO change nodes closer to root nodes first
			// note: otherShadowNodes can contain valueTree.getShadowNode(i) because it will not be considered as anchor if the node to change and anchor is the same.
			valueTree.changeProb(valueTree.getShadowNode(i), null, getMarginalAt(i), otherShadowNodes);
			// do not change probability of nodes which were already edited.
			otherShadowNodes.add(valueTree.getShadowNode(i));
		}
	}

	/**
	 * Calls {@link #basicClone()} internally
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone()
	 */
	public Object clone() {
		ValueTreeProbabilisticNode cloned = (ValueTreeProbabilisticNode) this.basicClone();
		PotentialTable auxTab = cloned.getProbabilityFunction();
		// copy variables
		for (int i = 0; i < this.getProbabilityFunction().getVariablesSize(); i++) {
			auxTab.addVariable(this.getProbabilityFunction().getVariableAt(i));
		}
		if (auxTab.getVariablesSize() != this.getProbabilityFunction().getVariablesSize()) {
			throw new IllegalStateException("Cloned CPT of node " + this + " has " + auxTab.getVariablesSize() + " of size, while original has " + this.getProbabilityFunction().getVariablesSize());
		}
		
		// perform fast array copy
		auxTab.setValues(this.getProbabilityFunction().getValues());
		auxTab.setSumOperation(((PotentialTable) this.getProbabilityFunction()).getSumOperation());
		
		
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode
				.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode
				.getExplanationColor().getRGB());
		cloned.setPosition(this.getPosition().getX(), this.getPosition().getY());
		cloned.setParents(SetToolkit.clone(parents));
		cloned.setChildren(SetToolkit.clone(this.getChildren()));
		cloned.setAdjacents(SetToolkit.clone(this.getAdjacents()));
		cloned.setSelected(this.isSelected());
		cloned.setExplanationDescription(this.getExplanationDescription());
		cloned.setPhrasesMap(this.getPhrasesMap());
		cloned.setInformationType(this.getInformationType());

		return cloned;
	}

	/** 
	 * Also clones {@link #getValueTree()}
	 * @see unbbayes.prs.bn.ProbabilisticNode#basicClone()
	 */
	public ProbabilisticNode basicClone() {
		ProbabilisticNode cloned = new ProbabilisticNode();
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		// cloned.setPosition(this.getPosition().getX(),
		// this.getPosition().getY());
		cloned.setStates(SetToolkit.clone(states));
		if (super.marginalList != null) {
			float[] marginais = new float[super.marginalList.length];
			System.arraycopy(super.marginalList, 0, marginais, 0,
					marginais.length);
			cloned.setMarginalProbabilities(marginais);
			cloned.copyMarginal();
		}
		if (this.hasEvidence()) {
			cloned.addFinding(this.getEvidence());
		}
		cloned.setInternalIdentificator(this.getInternalIdentificator());
		// clone value tree
		try {
			((ValueTreeProbabilisticNode) cloned).setValueTree((IValueTree) this.getValueTree().clone());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		return cloned;
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
//	 */
//	public float getMarginalAt(int index) {
//		return super.getMarginalAt(index);
//		return valueTree.getProb(valueTree.getShadowNode(index), null);
//	}
	
	

}
