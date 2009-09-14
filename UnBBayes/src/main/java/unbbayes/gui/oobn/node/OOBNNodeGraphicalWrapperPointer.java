/**
 * 
 */
package unbbayes.gui.oobn.node;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.util.ArrayMap;

/**
 * @author Shou Matsumoto
 * A pointer which makes it possible to treat OOBNNodeGraphicalWrapper as static,
 * in that way any changes on any instance would modify all others.
 * 
 * This class does that by delegating everything to a wrapped object. That
 * wrapped object should be the same object for every pointer.
 */
public class OOBNNodeGraphicalWrapperPointer extends OOBNNodeGraphicalWrapper {

	private OOBNNodeGraphicalWrapper pointsTo = null;
	
	/**
	 * @param wrappedNode
	 */
	private OOBNNodeGraphicalWrapperPointer(IOOBNNode wrappedNode) {
		super(wrappedNode);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Creates an instance of a "static" OOBNNodeGraphicalWrapper.
	 * Any changes on this pointer would affect the OOBNNodeGraphicalWrapper
	 * which it points to
	 */
	protected OOBNNodeGraphicalWrapperPointer(OOBNNodeGraphicalWrapper pointsTo) {
		// the call to super here is undesired, but the language forces us to...
		super(pointsTo.getWrappedNode());
		this.pointsTo = pointsTo;
	}
	
	
	/**
	 * Creates an instance of a "static" OOBNNodeGraphicalWrapper.
	 * Any changes on this pointer would affect the OOBNNodeGraphicalWrapper
	 * which it points to
	 */
	public static OOBNNodeGraphicalWrapperPointer newInstance(OOBNNodeGraphicalWrapper pointsTo) {
		return new OOBNNodeGraphicalWrapperPointer(pointsTo);
	}

	/**
	 * @param child
	 * @throws InvalidParentException
	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
	 */
	public void addChild(Node child) throws InvalidParentException {
		pointsTo.addChild(child);
	}

	/**
	 * @param explanationPhrase
	 * @see unbbayes.prs.Node#addExplanationPhrase(unbbayes.prs.bn.ExplanationPhrase)
	 */
	public void addExplanationPhrase(ExplanationPhrase explanationPhrase) {
		pointsTo.addExplanationPhrase(explanationPhrase);
	}

	/**
	 * @param stateNo
	 * @see unbbayes.prs.bn.TreeVariable#addFinding(int)
	 */
	public void addFinding(int stateNo) {
		pointsTo.addFinding(stateNo);
	}

	/**
	 * @param valores
	 * @see unbbayes.prs.bn.TreeVariable#addLikeliHood(float[])
	 */
	public void addLikeliHood(float[] valores) {
		pointsTo.addLikeliHood(valores);
	}

	/**
	 * @param parent
	 * @throws InvalidParentException
	 * @see unbbayes.prs.Node#addParent(unbbayes.prs.Node)
	 */
	public void addParent(Node parent) throws InvalidParentException {
		pointsTo.addParent(parent);
	}

	/**
	 * @param state
	 * @see unbbayes.prs.bn.ProbabilisticNode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		pointsTo.appendState(state);
	}

	/**
	 * 
	 * @see unbbayes.prs.Node#atualizatamanhoinfoestados()
	 */
	public void atualizatamanhoinfoestados() {
		pointsTo.atualizatamanhoinfoestados();
	}

	/**
	 * 
	 * @see unbbayes.prs.Node#clearAdjacents()
	 */
	public void clearAdjacents() {
		pointsTo.clearAdjacents();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone()
	 */
	public Object clone() {
		return pointsTo.clone();
	}

	/**
	 * @param raio
	 * @return
	 * @see unbbayes.prs.bn.ProbabilisticNode#clone(double)
	 */
	public ProbabilisticNode clone(double raio) {
		return pointsTo.clone(raio);
	}

	/**
	 * @param arg0
	 * @return
	 * @see unbbayes.prs.Node#compareTo(unbbayes.prs.Node)
	 */
	public int compareTo(Node arg0) {
		return pointsTo.compareTo(arg0);
	}

	/**
	 * @param obj
	 * @return
	 * @see unbbayes.prs.Node#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return pointsTo.equals(obj);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getAdjacents()
	 */
	public ArrayList<Node> getAdjacents() {
		return pointsTo.getAdjacents();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getDescription()
	 */
	public String getDescription() {
		return pointsTo.getDescription();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.TreeVariable#getEvidence()
	 */
	public int getEvidence() {
		return pointsTo.getEvidence();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getExplanationDescription()
	 */
	public String getExplanationDescription() {
		return pointsTo.getExplanationDescription();
	}

	/**
	 * @param node
	 * @return
	 * @throws Exception
	 * @see unbbayes.prs.Node#getExplanationPhrase(java.lang.String)
	 */
	public ExplanationPhrase getExplanationPhrase(String node) throws Exception {
		return pointsTo.getExplanationPhrase(node);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getInformationType()
	 */
	public int getInformationType() {
		return pointsTo.getInformationType();
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#getInputColor()
	 */
	public Color getInputColor() {
		return pointsTo.getInputColor();
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#getInstanceColor()
	 */
	public Color getInstanceColor() {
		return pointsTo.getInstanceColor();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getLabel()
	 */
	public String getLabel() {
		return pointsTo.getLabel();
	}

	/**
	 * @param index
	 * @return
	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
	 */
	public float getMarginalAt(int index) {
		return pointsTo.getMarginalAt(index);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getMean()
	 */
	public double[] getMean() {
		return pointsTo.getMean();
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#getName()
	 */
	public String getName() {
		return pointsTo.getName();
	}


	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#getOutputColor()
	 */
	public Color getOutputColor() {
		return pointsTo.getOutputColor();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getPhrasesMap()
	 */
	public ArrayMap<String, ExplanationPhrase> getPhrasesMap() {
		return pointsTo.getPhrasesMap();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getPosition()
	 */
	public Double getPosition() {
		return pointsTo.getPosition();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.ProbabilisticNode#getPotentialTable()
	 */
	public PotentialTable getPotentialTable() {
		return pointsTo.getPotentialTable();
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#getPrivateColor()
	 */
	public Color getPrivateColor() {
		return pointsTo.getPrivateColor();
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#getStandardDeviation()
	 */
	public double[] getStandardDeviation() {
		return pointsTo.getStandardDeviation();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.ProbabilisticNode#getType()
	 */
	public int getType() {
		return pointsTo.getWrappedNode().getType();
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#getWrappedNode()
	 */
	public IOOBNNode getWrappedNode() {
		return pointsTo.getWrappedNode();
	}

	/**
	 * @return
	 * @see unbbayes.prs.bn.TreeVariable#hasEvidence()
	 */
	public boolean hasEvidence() {
		return pointsTo.hasEvidence();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return pointsTo.hashCode();
	}

	/**
	 * 
	 * @see unbbayes.prs.bn.TreeVariable#initMarginalList()
	 */
	public void initMarginalList() {
		pointsTo.initMarginalList();
	}

	/**
	 * @param parent
	 * @return
	 * @see unbbayes.prs.Node#isChildOf(unbbayes.prs.Node)
	 */
	public boolean isChildOf(Node parent) {
		return pointsTo.isChildOf(parent);
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#isImmutableNode()
	 */
	public boolean isImmutableNode() {
		return pointsTo.isImmutableNode();
	}

	/**
	 * @param child
	 * @return
	 * @see unbbayes.prs.Node#isParentOf(unbbayes.prs.Node)
	 */
	public boolean isParentOf(Node child) {
		return pointsTo.isParentOf(child);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see unbbayes.prs.Node#isPointInDrawableArea(int, int)
	 */
	public boolean isPointInDrawableArea(int x, int y) {
		return pointsTo.isPointInDrawableArea(x, y);
	}

	/**
	 * @return
	 * @see unbbayes.prs.Node#isSelected()
	 */
	public boolean isSelected() {
		return pointsTo.isSelected();
	}

	/**
	 * 
	 * @see unbbayes.prs.Node#makeAdjacents()
	 */
	public void makeAdjacents() {
		pointsTo.makeAdjacents();
	}

	/**
	 * 
	 * @see unbbayes.prs.bn.ProbabilisticNode#removeLastState()
	 */
	public void removeLastState() {
		pointsTo.removeLastState();
	}

	/**
	 * @param index
	 * @see unbbayes.prs.Node#removeStateAt(int)
	 */
	public void removeStateAt(int index) {
		pointsTo.removeStateAt(index);
	}

	/**
	 * @param adjacents
	 * @see unbbayes.prs.Node#setAdjacents(java.util.ArrayList)
	 */
	public void setAdjacents(ArrayList<Node> adjacents) {
		pointsTo.setAdjacents(adjacents);
	}

	/**
	 * @param children
	 * @see unbbayes.prs.Node#setChildren(java.util.ArrayList)
	 */
	public void setChildren(ArrayList<Node> children) {
		pointsTo.setChildren(children);
	}

	/**
	 * @param texto
	 * @see unbbayes.prs.Node#setDescription(java.lang.String)
	 */
	public void setDescription(String texto) {
		pointsTo.setDescription(texto);
	}

	/**
	 * @param text
	 * @see unbbayes.prs.Node#setExplanationDescription(java.lang.String)
	 */
	public void setExplanationDescription(String text) {
		pointsTo.setExplanationDescription(text);
	}

	/**
	 * @param isImmutableNode
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setImmutableNode(boolean)
	 */
	public void setImmutableNode(boolean isImmutableNode) {
		pointsTo.setImmutableNode(isImmutableNode);
	}

	/**
	 * @param informationType
	 * @see unbbayes.prs.Node#setInformationType(int)
	 */
	public void setInformationType(int informationType) {
		pointsTo.setInformationType(informationType);
	}

	/**
	 * @param inputColor
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setInputColor(java.awt.Color)
	 */
	public void setInputColor(Color inputColor) {
		pointsTo.setInputColor(inputColor);
	}

	/**
	 * @param instanceColor
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setInstanceColor(java.awt.Color)
	 */
	public void setInstanceColor(Color instanceColor) {
		pointsTo.setInstanceColor(instanceColor);
	}

	/**
	 * @param label
	 * @see unbbayes.prs.Node#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		pointsTo.setLabel(label);
	}

	/**
	 * @param mean
	 * @see unbbayes.prs.Node#setMean(double[])
	 */
	public void setMean(double[] mean) {
		pointsTo.setMean(mean);
	}

	/**
	 * @param name
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setName(java.lang.String)
	 */
	public void setName(String name) {
		pointsTo.setName(name);
	}


	/**
	 * @param outputColor
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setOutputColor(java.awt.Color)
	 */
	public void setOutputColor(Color outputColor) {
		pointsTo.setOutputColor(outputColor);
	}

	/**
	 * @param parents
	 * @see unbbayes.prs.Node#setParents(java.util.ArrayList)
	 */
	public void setParents(ArrayList<Node> parents) {
		pointsTo.setParents(parents);
	}

	/**
	 * @param phrasesMap
	 * @return
	 * @see unbbayes.prs.Node#setPhrasesMap(unbbayes.util.ArrayMap)
	 */
	public ArrayMap<String, ExplanationPhrase> setPhrasesMap(
			ArrayMap<String, ExplanationPhrase> phrasesMap) {
		return pointsTo.setPhrasesMap(phrasesMap);
	}

	/**
	 * @param x
	 * @param y
	 * @see unbbayes.prs.Node#setPosition(double, double)
	 */
	public void setPosition(double x, double y) {
		pointsTo.setPosition(x, y);
	}

	/**
	 * @param privateColor
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setPrivateColor(java.awt.Color)
	 */
	public void setPrivateColor(Color privateColor) {
		pointsTo.setPrivateColor(privateColor);
	}

	/**
	 * @param b
	 * @see unbbayes.prs.bn.ProbabilisticNode#setSelected(boolean)
	 */
	public void setSelected(boolean b) {
		pointsTo.setSelected(b);
	}

	/**
	 * @param is
	 * @see unbbayes.prs.Node#setSizeIsVariable(boolean)
	 */
	public void setSizeIsVariable(boolean is) {
		pointsTo.setSizeIsVariable(is);
	}

	/**
	 * @param width
	 * @param height
	 * @see unbbayes.prs.Node#setSizeVariable(double, double)
	 */
	public void setSizeVariable(double width, double height) {
		pointsTo.setSizeVariable(width, height);
	}

	/**
	 * @param standardDeviation
	 * @see unbbayes.prs.Node#setStandardDeviation(double[])
	 */
	public void setStandardDeviation(double[] standardDeviation) {
		pointsTo.setStandardDeviation(standardDeviation);
	}

	/**
	 * @param state
	 * @param index
	 * @see unbbayes.prs.Node#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String state, int index) {
		pointsTo.setStateAt(state, index);
	}

	/**
	 * @param states
	 * @see unbbayes.prs.Node#setStates(java.util.List)
	 */
	public void setStates(List<String> states) {
		pointsTo.setStates(states);
	}

	/**
	 * @param wrappedNode
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#setWrappedNode(unbbayes.prs.oobn.IOOBNNode)
	 */
	public void setWrappedNode(IOOBNNode wrappedNode) {
		pointsTo.setWrappedNode(wrappedNode);
	}

	/**
	 * @return
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper#toString()
	 */
	public String toString() {
		return pointsTo.toString();
	}
	

	
	
	
	
}
