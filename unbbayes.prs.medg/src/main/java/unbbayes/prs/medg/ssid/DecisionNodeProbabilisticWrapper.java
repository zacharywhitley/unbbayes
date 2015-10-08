/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.awt.Color;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.ArrayMap;

/**
 * This is simply a {@link ProbabilisticNode} which wraps a {@link DecisionNode}
 * @author Shou Matsumoto
 *
 */
public class DecisionNodeProbabilisticWrapper extends ProbabilisticNode {
	
	private static final long serialVersionUID = 1523254128871340483L;
	
	private DecisionNode decisionNode = null;

	/**
	 * 
	 */
	public DecisionNodeProbabilisticWrapper(DecisionNode decisionNode) {
		this.setDecisionNode(decisionNode);
	}

	/**
	 * @return the decisionNode
	 */
	public DecisionNode getDecisionNode() {
		return decisionNode;
	}

	/**
	 * @param decisionNode the decisionNode to set
	 */
	public void setDecisionNode(DecisionNode decisionNode) {
		this.decisionNode = decisionNode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#getType()
	 */
	public int getType() {
		return getDecisionNode().getType();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone(double)
	 */
	public ProbabilisticNode clone(double radius) {
		return (ProbabilisticNode) this.clone();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone()
	 */
	public Object clone() {
		return new DecisionNodeProbabilisticWrapper((DecisionNode) getDecisionNode().clone());
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#basicClone()
	 */
	public ProbabilisticNode basicClone() {
		return (ProbabilisticNode) this.clone();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#getProbabilityFunction()
	 */
	public PotentialTable getProbabilityFunction() {
		PotentialTable table = new ProbabilisticTable();
		table.addVariable(this.getDecisionNode());
		return table;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#marginal()
	 */
	protected void marginal() {
		getDecisionNode().updateMarginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		getDecisionNode().appendState(state);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#removeLastState()
	 */
	public void removeLastState() {
		getDecisionNode().removeLastState();
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#removeStates()
	 */
	public void removeStates() {
		getDecisionNode().removeStates();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#removeStateAt(int)
	 */
	public void removeStateAt(int index) {
		getDecisionNode().removeStateAt(index);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#updateMarginal()
	 */
	public void updateMarginal() {
		getDecisionNode().updateMarginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#initMarginalList()
	 */
	public void initMarginalList() {
		getDecisionNode().initMarginalList();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#isMarginalList()
	 */
	public boolean isMarginalList() {
		return getDecisionNode().isMarginalList();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#copyMarginal()
	 */
	public void copyMarginal() {
		getDecisionNode().copyMarginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#restoreMarginal()
	 */
	public void restoreMarginal() {
		getDecisionNode().restoreMarginal();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
	 */
	public float getMarginalAt(int index) {
		return getDecisionNode().getMarginalAt(index);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setMarginalAt(int, float)
	 */
	public void setMarginalAt(int index, float value) {
		getDecisionNode().setMarginalAt(index, value);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setMarginalProbabilities(float[])
	 */
	public void setMarginalProbabilities(float[] marginalProbabilities) {
		getDecisionNode().setMarginalProbabilities(marginalProbabilities);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#resetEvidence()
	 */
	public void resetEvidence() {
		getDecisionNode().resetEvidence();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#hasEvidence()
	 */
	public boolean hasEvidence() {
		return getDecisionNode().hasEvidence();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getEvidence()
	 */
	public int getEvidence() {
		return getDecisionNode().getEvidence();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#resetLikelihood()
	 */
	public void resetLikelihood() {
		getDecisionNode().resetLikelihood();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#hasLikelihood()
	 */
	public boolean hasLikelihood() {
		return getDecisionNode().hasLikelihood();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addFinding(int)
	 */
	public void addFinding(int stateIndex) {
		getDecisionNode().addFinding(stateIndex);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addFinding(int, boolean)
	 */
	public void addFinding(int stateIndex, boolean isNegative) {
		getDecisionNode().addFinding(stateIndex, isNegative);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addLikeliHood(float[])
	 */
	public void addLikeliHood(float[] likelihood) {
		getDecisionNode().addLikeliHood(likelihood);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addLikeliHood(float[], java.util.List)
	 */
	public void addLikeliHood(float[] likelihood, List<INode> dependencies) {
		getDecisionNode().addLikeliHood(likelihood, dependencies);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getAssociatedClique()
	 */
	public IRandomVariable getAssociatedClique() {
		return getDecisionNode().getAssociatedClique();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setAssociatedClique(unbbayes.prs.bn.IRandomVariable)
	 */
	public void setAssociatedClique(IRandomVariable clique) {
		getDecisionNode().setAssociatedClique(clique);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#updateEvidences()
	 */
	protected void updateEvidences() {
		if (getEvidence() != -1 && getAssociatedClique() != null) {						
			PotentialTable auxTab = (PotentialTable)getAssociatedClique().getProbabilityFunction();
			int index = auxTab.indexOfVariable(this);
			auxTab.updateEvidences(marginalList, index);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getLikelihood()
	 */
	public float[] getLikelihood() {
		return getDecisionNode().getLikelihood();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#getLikelihoodParents()
	 */
	public List<INode> getLikelihoodParents() {
		return getDecisionNode().getLikelihoodParents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#setLikelihoodParents(java.util.List)
	 */
	protected void setLikelihoodParents(List<INode> likelihoodParents) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length > 0) {
			System.err.println(stackTrace[0] + " is not supported. Call addLikeliHood instead.");
		}
		this.addLikeliHood(getLikelihood(), likelihoodParents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getInformationType()
	 */
	public int getInformationType() {
		return getDecisionNode().getInformationType();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setInformationType(int)
	 */
	public void setInformationType(int informationType) {
		getDecisionNode().setInformationType(informationType);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addExplanationPhrase(unbbayes.prs.bn.ExplanationPhrase)
	 */
	public void addExplanationPhrase(ExplanationPhrase explanationPhrase) {
		getDecisionNode().addExplanationPhrase(explanationPhrase);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getExplanationPhrase(java.lang.String)
	 */
	public ExplanationPhrase getExplanationPhrase(String node) throws Exception {
		return getDecisionNode().getExplanationPhrase(node);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setDescription(java.lang.String)
	 */
	public void setDescription(String texto) {
		getDecisionNode().setDescription(texto);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setName(java.lang.String)
	 */
	public void setName(String name) {
		getDecisionNode().setName(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		getDecisionNode().setLabel(label);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getLabel()
	 */
	public String getLabel() {
		return getDecisionNode().getLabel();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#updateLabel()
	 */
	public String updateLabel() {
		return getDecisionNode().updateLabel();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setChildren(java.util.ArrayList)
	 */
	public void setChildren(ArrayList<Node> children) {
		getDecisionNode().setChildren(children);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setParents(java.util.ArrayList)
	 */
	public void setParents(ArrayList<Node> parents) {
		getDecisionNode().setParents(parents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
	 */
	public void addChild(Node child) throws InvalidParentException {
		getDecisionNode().addChild(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeChild(unbbayes.prs.Node)
	 */
	public void removeChild(Node child) {
		getDecisionNode().removeChild(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addParent(unbbayes.prs.Node)
	 */
	public void addParent(Node parent) throws InvalidParentException {
		getDecisionNode().addParent(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeParent(unbbayes.prs.Node)
	 */
	public void removeParent(Node parent) {
		getDecisionNode().removeParent(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isParentOf(unbbayes.prs.Node)
	 */
	public boolean isParentOf(Node child) {
		return getDecisionNode().isParentOf(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isChildOf(unbbayes.prs.Node)
	 */
	public boolean isChildOf(Node parent) {
		return getDecisionNode().isChildOf(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setExplanationDescription(java.lang.String)
	 */
	public void setExplanationDescription(String text) {
		getDecisionNode().setExplanationDescription(text);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setPhrasesMap(unbbayes.util.ArrayMap)
	 */
	public ArrayMap<String, ExplanationPhrase> setPhrasesMap(
			ArrayMap<String, ExplanationPhrase> phrasesMap) {
		return getDecisionNode().setPhrasesMap(phrasesMap);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getDescription()
	 */
	public String getDescription() {
		return getDecisionNode().getDescription();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getAdjacents()
	 */
	public ArrayList<Node> getAdjacents() {
		return getDecisionNode().getAdjacents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getName()
	 */
	public String getName() {
		return getDecisionNode().getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getChildren()
	 */
	public ArrayList<Node> getChildren() {
		return getDecisionNode().getChildren();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getParents()
	 */
	public ArrayList<Node> getParents() {
		return getDecisionNode().getParents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getExplanationDescription()
	 */
	public String getExplanationDescription() {
		return getDecisionNode().getExplanationDescription();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getPhrasesMap()
	 */
	public ArrayMap<String, ExplanationPhrase> getPhrasesMap() {
		return getDecisionNode().getPhrasesMap();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#atualizatamanhoinfoestados()
	 */
	public void atualizatamanhoinfoestados() {
		getDecisionNode().atualizatamanhoinfoestados();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#hasState(java.lang.String)
	 */
	public boolean hasState(String state) {
		return getDecisionNode().hasState(state);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String state, int index) {
		getDecisionNode().setStateAt(state, index);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getStatesSize()
	 */
	public int getStatesSize() {
		return getDecisionNode().getStatesSize();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getStateAt(int)
	 */
	public String getStateAt(int index) {
		return getDecisionNode().getStateAt(index);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#makeAdjacents()
	 */
	public void makeAdjacents() {
		getDecisionNode().makeAdjacents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#clearAdjacents()
	 */
	public void clearAdjacents() {
		getDecisionNode().clearAdjacents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setDisplayMode(java.lang.String)
	 */
	public void setDisplayMode(String s) {
		getDecisionNode().setDisplayMode(s);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getDisplayMode()
	 */
	public String getDisplayMode() {
		return getDecisionNode().getDisplayMode();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setAdjacents(java.util.ArrayList)
	 */
	public void setAdjacents(ArrayList<Node> adjacents) {
		getDecisionNode().setAdjacents(adjacents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setStates(java.util.List)
	 */
	public void setStates(List<String> states) {
		getDecisionNode().setStates(states);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#toString()
	 */
	public String toString() {
		return getDecisionNode().toString();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return getDecisionNode().equals(obj);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#compareTo(unbbayes.prs.Node)
	 */
	public int compareTo(Node arg0) {
		return getDecisionNode().compareTo(arg0);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isPointInDrawableArea(int, int)
	 */
	public boolean isPointInDrawableArea(int x, int y) {
		return getDecisionNode().isPointInDrawableArea(x, y);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#isSelected()
	 */
	public boolean isSelected() {
		return getDecisionNode().isSelected();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSelected(boolean)
	 */
	public void setSelected(boolean b) {
		getDecisionNode().setSelected(b);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getPosition()
	 */
	public Double getPosition() {
		return getDecisionNode().getPosition();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setPosition(double, double)
	 */
	public void setPosition(double x, double y) {
		getDecisionNode().setPosition(x, y);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getColor()
	 */
	public Color getColor() {
		return getDecisionNode().getColor();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setColor(java.awt.Color)
	 */
	public void setColor(Color c) {
		getDecisionNode().setColor(c);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getWidth()
	 */
	public int getWidth() {
		return getDecisionNode().getWidth();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getHeight()
	 */
	public int getHeight() {
		return getDecisionNode().getHeight();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getSize()
	 */
	public Double getSize() {
		return getDecisionNode().getSize();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSize(double, double)
	 */
	public void setSize(double width, double height) {
		getDecisionNode().setSize(width, height);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSizeVariable(double, double)
	 */
	public void setSizeVariable(double width, double height) {
		getDecisionNode().setSizeVariable(width, height);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSizeIsVariable(boolean)
	 */
	public void setSizeIsVariable(boolean is) {
		getDecisionNode().setSizeIsVariable(is);
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addNodeNameChangedListener(unbbayes.prs.Node.NodeNameChangedListener)
	 */
	public void addNodeNameChangedListener(NodeNameChangedListener listener) {
		getDecisionNode().addNodeNameChangedListener(listener);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeNodeNameChangedListener(unbbayes.prs.Node.NodeNameChangedListener)
	 */
	public void removeNodeNameChangedListener(NodeNameChangedListener listener) {
		getDecisionNode().removeNodeNameChangedListener(listener);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		getDecisionNode().addChildNode(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode parent) throws InvalidParentException {
		getDecisionNode().addParentNode(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getAdjacentNodes()
	 */
	public List<INode> getAdjacentNodes() {
		return getDecisionNode().getAdjacentNodes();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getChildNodes()
	 */
	public List<INode> getChildNodes() {
		return getDecisionNode().getChildNodes();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getParentNodes()
	 */
	public List<INode> getParentNodes() {
		return getDecisionNode().getParentNodes();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeChildNode(unbbayes.prs.INode)
	 */
	public void removeChildNode(INode child) {
		getDecisionNode().removeChildNode(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode parent) {
		getDecisionNode().removeParentNode(parent);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<INode> children) {
		getDecisionNode().setChildNodes(children);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> parents) {
		getDecisionNode().setParentNodes(parents);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#hashCode()
	 */
	public int hashCode() {
		return getDecisionNode().hashCode();
	}
	
	

}
