/**
 * 
 */
package unbbayes.prs.bn;

import java.awt.Color;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.ArrayMap;
import unbbayes.util.Debug;


/**
 * This class is a probabilistic node which wraps another node
 * @author Shou Matsumoto
 *
 */
public class ProbabilisticNodeWrapper extends ProbabilisticNode {

	private static final long serialVersionUID = -8661863135673641252L;
	
	private INode wrappedNode;

	/**
	 * Default constructor initializing field
	 * @param wrappedNode
	 */
	public ProbabilisticNodeWrapper(INode wrappedNode) {
		this.setWrappedNode(wrappedNode);
	}

	/**
	 * @return the wrappedNode
	 */
	public INode getWrappedNode() {
		return wrappedNode;
	}

	/**
	 * @param wrappedNode the wrappedNode to set
	 */
	public void setWrappedNode(INode wrappedNode) {
		this.wrappedNode = wrappedNode;
	}

	/**
	 * @param text
	 * @see unbbayes.prs.INode#setDescription(java.lang.String)
	 */
	public void setDescription(String text) {
		this.wrappedNode.setDescription(text);
	}

	/**
	 * @param name
	 * @see unbbayes.prs.INode#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.wrappedNode.setName(name);
	}

	/**
	 * @param children
	 * @see unbbayes.prs.INode#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<INode> children) {
		this.wrappedNode.setChildNodes(children);
	}

	/**
	 * @param parents
	 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> parents) {
		this.wrappedNode.setParentNodes(parents);
	}

	/**
	 * @param child
	 * @throws InvalidParentException
	 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		this.wrappedNode.addChildNode(child);
	}

	/**
	 * @param child
	 * @see unbbayes.prs.INode#removeChildNode(unbbayes.prs.INode)
	 */
	public void removeChildNode(INode child) {
		this.wrappedNode.removeChildNode(child);
	}

	/**
	 * @param parent
	 * @throws InvalidParentException
	 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode parent) throws InvalidParentException {
		this.wrappedNode.addParentNode(parent);
	}

	/**
	 * @param parent
	 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode parent) {
		this.wrappedNode.removeParentNode(parent);
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getDescription()
	 */
	public String getDescription() {
		return this.wrappedNode.getDescription();
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getAdjacentNodes()
	 */
	public List<INode> getAdjacentNodes() {
		return this.wrappedNode.getAdjacentNodes();
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getName()
	 */
	public String getName() {
		return this.wrappedNode.getName();
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getChildNodes()
	 */
	public List<INode> getChildNodes() {
		return this.wrappedNode.getChildNodes();
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getParentNodes()
	 */
	public List<INode> getParentNodes() {
		return this.wrappedNode.getParentNodes();
	}

	/**
	 * @param state
	 * @see unbbayes.prs.INode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		this.wrappedNode.appendState(state);
	}

	/**
	 * 
	 * @see unbbayes.prs.INode#removeLastState()
	 */
	public void removeLastState() {
		this.wrappedNode.removeLastState();
	}

	/**
	 * @param index
	 * @see unbbayes.prs.INode#removeStateAt(int)
	 */
	public void removeStateAt(int index) {
		this.wrappedNode.removeStateAt(index);
	}

	/**
	 * @param state
	 * @param index
	 * @see unbbayes.prs.INode#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String state, int index) {
		this.wrappedNode.setStateAt(state, index);
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getStatesSize()
	 */
	public int getStatesSize() {
		return this.wrappedNode.getStatesSize();
	}

	/**
	 * @param index
	 * @return
	 * @see unbbayes.prs.INode#getStateAt(int)
	 */
	public String getStateAt(int index) {
		return this.wrappedNode.getStateAt(index);
	}

	/**
	 * @param states
	 * @see unbbayes.prs.INode#setStates(java.util.List)
	 */
	public void setStates(List<String> states) {
		this.wrappedNode.setStates(states);
	}

	/**
	 * @return
	 * @see unbbayes.prs.INode#getType()
	 */
	public int getType() {
		return this.wrappedNode.getType();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone(double)
	 */
	public ProbabilisticNode clone(double radius) {
		// TODO Auto-generated method stub
		throw new RuntimeException(new CloneNotSupportedException("Cannot clone " + getWrappedNode() + " when wrapped in " + getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#getClone()
	 */
	public ProbabilisticNode getClone() {
		// TODO Auto-generated method stub
		throw new RuntimeException(new CloneNotSupportedException("Cannot clone " + getWrappedNode() + " when wrapped in " + getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone()
	 */
	public Object clone() {
		// TODO Auto-generated method stub
		throw new RuntimeException(new CloneNotSupportedException("Cannot clone " + getWrappedNode() + " when wrapped in " + getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#basicClone()
	 */
	public ProbabilisticNode basicClone() {
		// TODO Auto-generated method stub
		throw new RuntimeException(new CloneNotSupportedException("Cannot clone " + getWrappedNode() + " when wrapped in " + getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#getProbabilityFunction()
	 */
	public PotentialTable getProbabilityFunction() {
		// TODO Auto-generated method stub
		try {
			return (PotentialTable) ((IRandomVariable)getWrappedNode()).getProbabilityFunction();
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(),e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#marginal()
	 */
	protected void marginal() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).marginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#removeAllStates()
	 */
	public void removeAllStates() {
		while (states.size() > 1) {
			removeLastState();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#getInternalIdentificator()
	 */
	public int getInternalIdentificator() {
		// TODO Auto-generated method stub
		return ((IRandomVariable)getWrappedNode()).getInternalIdentificator();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#setInternalIdentificator(int)
	 */
	public void setInternalIdentificator(int internalIdentificator) {
		// TODO Auto-generated method stub
		((IRandomVariable)getWrappedNode()).setInternalIdentificator(internalIdentificator);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#removeStates()
	 */
	public void removeStates() {
		while (getStatesSize() > 0) {
			removeStateAt(0);
		}
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#initMarginalList()
	 */
	public void initMarginalList() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).initMarginalList();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#isMarginalList()
	 */
	public boolean isMarginalList() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).isMarginalList();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#copyMarginal()
	 */
	public void copyMarginal() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).copyMarginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#restoreMarginal()
	 */
	public void restoreMarginal() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).restoreMarginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
	 */
	public float getMarginalAt(int index) {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).getMarginalAt(index);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setMarginalAt(int, float)
	 */
	public void setMarginalAt(int index, float value) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).setMarginalAt(index, value);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setMarginalProbabilities(float[])
	 */
	public void setMarginalProbabilities(float[] marginalProbabilities) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).setMarginalProbabilities(marginalProbabilities);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#resetEvidence()
	 */
	public void resetEvidence() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).resetEvidence();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#hasEvidence()
	 */
	public boolean hasEvidence() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).hasEvidence();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getEvidence()
	 */
	public int getEvidence() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).getEvidence();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#resetLikelihood()
	 */
	public void resetLikelihood() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).resetLikelihood();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#hasLikelihood()
	 */
	public boolean hasLikelihood() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).hasLikelihood();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addFinding(int)
	 */
	public void addFinding(int stateIndex) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).addFinding(stateIndex);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addFinding(int, boolean)
	 */
	public void addFinding(int stateIndex, boolean isNegative) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).addFinding(stateIndex, isNegative);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addLikeliHood(float[])
	 */
	public void addLikeliHood(float[] likelihood) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).addLikeliHood(likelihood);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addLikeliHood(float[], java.util.List)
	 */
	public void addLikeliHood(float[] likelihood, List<INode> dependencies) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).addLikeliHood(likelihood, dependencies);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getAssociatedClique()
	 */
	public IRandomVariable getAssociatedClique() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).getAssociatedClique();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setAssociatedClique(unbbayes.prs.bn.IRandomVariable)
	 */
	public void setAssociatedClique(IRandomVariable clique) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).setAssociatedClique(clique);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#updateEvidences()
	 */
	protected void updateEvidences() {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).updateEvidences();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getLikelihood()
	 */
	public float[] getLikelihood() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).getLikelihood();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getLikelihoodParents()
	 */
	public List<INode> getLikelihoodParents() {
		// TODO Auto-generated method stub
		return ((TreeVariable)getWrappedNode()).getLikelihoodParents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setLikelihoodParents(java.util.List)
	 */
	protected void setLikelihoodParents(List<INode> likelihoodParents) {
		// TODO Auto-generated method stub
		((TreeVariable)getWrappedNode()).setLikelihoodParents(likelihoodParents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getInformationType()
	 */
	public int getInformationType() {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).getInformationType();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setInformationType(int)
	 */
	public void setInformationType(int informationType) {
		// TODO Auto-generated method stub
		((Node)getWrappedNode()).setInformationType(informationType);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addExplanationPhrase(unbbayes.prs.bn.ExplanationPhrase)
	 */
	public void addExplanationPhrase(ExplanationPhrase explanationPhrase) {
		// TODO Auto-generated method stub
		((Node)getWrappedNode()).addExplanationPhrase(explanationPhrase);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getExplanationPhrase(java.lang.String)
	 */
	public ExplanationPhrase getExplanationPhrase(String node) throws Exception {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).getExplanationPhrase(node);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		// TODO Auto-generated method stub
		((Node)getWrappedNode()).setLabel(label);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getLabel()
	 */
	public String getLabel() {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).getLabel();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#updateLabel()
	 */
	public String updateLabel() {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).updateLabel();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setChildren(java.util.ArrayList)
	 */
	public void setChildren(ArrayList<Node> children) {
		getChildNodes().clear();
		getChildNodes().addAll(children);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setParents(java.util.ArrayList)
	 */
	public void setParents(ArrayList<Node> parents) {
		getParentNodes().clear();
		getParentNodes().addAll(parents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
	 */
	public void addChild(Node child) throws InvalidParentException {
		addChildNode(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeChild(unbbayes.prs.Node)
	 */
	public void removeChild(Node child) {
		removeChildNode(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addParent(unbbayes.prs.Node)
	 */
	public void addParent(Node parent) throws InvalidParentException {
		addParentNode(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeParent(unbbayes.prs.Node)
	 */
	public void removeParent(Node parent) {
		removeParentNode(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isParentOf(unbbayes.prs.Node)
	 */
	public boolean isParentOf(Node child) {
		return getChildNodes().contains(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isChildOf(unbbayes.prs.Node)
	 */
	public boolean isChildOf(Node parent) {
		return getParentNodes().contains(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setExplanationDescription(java.lang.String)
	 */
	public void setExplanationDescription(String text) {
		// TODO Auto-generated method stub
		((Node)getWrappedNode()).setExplanationDescription(text);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setPhrasesMap(unbbayes.util.ArrayMap)
	 */
	public ArrayMap<String, ExplanationPhrase> setPhrasesMap(
			ArrayMap<String, ExplanationPhrase> phrasesMap) {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).setPhrasesMap(phrasesMap);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getAdjacents()
	 */
	public ArrayList<Node> getAdjacents() {
		List<INode> ret = getAdjacentNodes();
		if (ret instanceof ArrayList) {
			return (ArrayList)ret;
		}
		return new ArrayList(ret);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getChildren()
	 */
	public ArrayList<Node> getChildren() {
		List<INode> ret = getChildNodes();
		if (ret instanceof ArrayList) {
			return (ArrayList)ret;
		}
		return new ArrayList(ret);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getParents()
	 */
	public ArrayList<Node> getParents() {
		List<INode> ret = getParentNodes();
		if (ret instanceof ArrayList) {
			return (ArrayList)ret;
		}
		return new ArrayList(ret);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getExplanationDescription()
	 */
	public String getExplanationDescription() {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).getExplanationDescription();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getPhrasesMap()
	 */
	public ArrayMap<String, ExplanationPhrase> getPhrasesMap() {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).getPhrasesMap();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#clone(unbbayes.prs.Node)
	 */
	public void clone(Node clone) {
		throw new RuntimeException(new CloneNotSupportedException("Cannot clone " + getWrappedNode() + " when wrapped in " + getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#atualizatamanhoinfoestados()
	 */
	public void atualizatamanhoinfoestados() {
		// TODO Auto-generated method stub
		((Node)getWrappedNode()).atualizatamanhoinfoestados();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#hasState(java.lang.String)
	 */
	public boolean hasState(String state) {
		for (int i = 0; i < getStatesSize(); i++) {
			if (getStateAt(i).equals(state)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#makeAdjacents()
	 */
	public void makeAdjacents() {
		getAdjacentNodes().addAll(getParentNodes());
		getAdjacentNodes().addAll(getChildNodes());
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#clearAdjacents()
	 */
	public void clearAdjacents() {
		getAdjacentNodes().clear();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setDisplayMode(java.lang.String)
	 */
	public void setDisplayMode(String s) {
		// TODO Auto-generated method stub
		((Node)getWrappedNode()).setDisplayMode(s);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getDisplayMode()
	 */
	public String getDisplayMode() {
		// TODO Auto-generated method stub
		return ((Node)getWrappedNode()).getDisplayMode();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setAdjacents(java.util.ArrayList)
	 */
	public void setAdjacents(ArrayList<Node> adjacents) {
		clearAdjacents();
		getAdjacentNodes().addAll(adjacents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#toString()
	 */
	public String toString() {
		return ((Object)getWrappedNode()).toString();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return super.equals(obj) || getWrappedNode().equals(obj);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#compareTo(unbbayes.prs.Node)
	 */
	public int compareTo(Node arg0) {
		return this.getName().compareTo(arg0.getName());
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isPointInDrawableArea(int, int)
	 */
	public boolean isPointInDrawableArea(int x, int y) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return super.isPointInDrawableArea(x, y);
		}
		return ((Node)getWrappedNode()).isPointInDrawableArea(x, y);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isSelected()
	 */
	public boolean isSelected() {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return isSelected();
		}
		return ((Node)getWrappedNode()).isSelected();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSelected(boolean)
	 */
	public void setSelected(boolean b) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.setSelected(b);
			return;
		}
		((Node)getWrappedNode()).setSelected(b);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getPosition()
	 */
	public Double getPosition() {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return super.getPosition();
		}
		return ((Node)getWrappedNode()).getPosition();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setPosition(double, double)
	 */
	public void setPosition(double x, double y) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.setPosition(x, y);
			return;
		}
		((Node)getWrappedNode()).setPosition(x, y);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getColor()
	 */
	public Color getColor() {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return super.getColor();
		}
		return ((Node)getWrappedNode()).getColor();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setColor(java.awt.Color)
	 */
	public void setColor(Color c) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.setColor(c);
			return;
		}
		((Node)getWrappedNode()).setColor(c);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getWidth()
	 */
	public int getWidth() {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return super.getWidth();
		}
		return ((Node)getWrappedNode()).getWidth();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getHeight()
	 */
	public int getHeight() {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return super.getHeight();
		}
		return ((Node)getWrappedNode()).getHeight();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getSize()
	 */
	public Double getSize() {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			return super.getSize();
		}
		return ((Node)getWrappedNode()).getSize();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSize(double, double)
	 */
	public void setSize(double width, double height) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.setSize(width, height);
			return;
		}
		((Node)getWrappedNode()).setSize(width, height);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSizeVariable(double, double)
	 */
	public void setSizeVariable(double width, double height) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.setSizeVariable(width, height);
			return;
		}
		((Node)getWrappedNode()).setSizeVariable(width, height);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSizeIsVariable(boolean)
	 */
	public void setSizeIsVariable(boolean is) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.setSizeIsVariable(is);
			return;
		}
		((Node)getWrappedNode()).setSizeIsVariable(is);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addNodeNameChangedListener(unbbayes.prs.Node.NodeNameChangedListener)
	 */
	public void addNodeNameChangedListener(NodeNameChangedListener listener) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.addNodeNameChangedListener(listener);
			return;
		}
		((Node)getWrappedNode()).addNodeNameChangedListener(listener);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeNodeNameChangedListener(unbbayes.prs.Node.NodeNameChangedListener)
	 */
	public void removeNodeNameChangedListener(NodeNameChangedListener listener) {
		// TODO Auto-generated method stub
		if (getWrappedNode() == null) {
			super.removeNodeNameChangedListener(listener);
			return;
		}
		((Node)getWrappedNode()).removeNodeNameChangedListener(listener);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#hashCode()
	 */
	public int hashCode() {
		return getWrappedNode().hashCode();
	}
	
}
