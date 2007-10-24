package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import unbbayes.draw.DrawFlatPentagon;
import unbbayes.prs.mebn.context.NodeFormulaTree;

/**
 * The individual of the ContextNode class represent a type of constraint impose
 * to the MFrag arguments. The constraint is represented by a formula of first 
 * order logic. 
 * */

public class ContextNode extends MultiEntityNode {
	
	private static final long serialVersionUID = 8186266877724939663L;

	private DomainMFrag mFrag;
	
	/**
	 * All innner terms of this node.
	 */
	private List<ContextNode> innerTermOfList;
	 
	/**
	 * All nodes where this node is an inner term.
	 */
	private List<ContextNode> innerTermFromList;
	
	/* formula */
	
	/* Nota: when the formulaTree of the node is setted, the atributes isValidFormula, 
	 * variableList and exemplarList have to be setted too for mantain the 
	 * consistency of the object*/
	private NodeFormulaTree formulaTree; 
	
	private boolean isValidFormula = false; //Tell if the formula is valid for this implementation 
	 
	private Set<OrdinaryVariable> variableList; //Variables of the formula
	
	private List<OrdinaryVariable> exemplarList; // Variables used in quantifiers  
	
	/* the formula in the PowerLoom format */
	
	private boolean formulaTreeTurned; //The formula don't is equal to formulaTree!!! 
	private String formula; 
	
	/* draw */ 
	
	private static Color color = new Color(176, 252, 131); 
	
    private DrawFlatPentagon drawContextNode;
    
    /**
     * Create the context node and add your default possible values 
     * @param name
     * @param mFrag
     */
    public ContextNode(String name, DomainMFrag mFrag) {
    	
    	super(); 
    	
    	setName(name); 
    	setLabel(" "); 
    	
    	this.mFrag = mFrag; 
    	
    	innerTermOfList = new ArrayList<ContextNode>();
    	innerTermFromList = new ArrayList<ContextNode>();
    	
    	/* draw */
    	size.x = 100;
    	size.y = 20; 
    	drawContextNode = new DrawFlatPentagon(position, size);
        drawElement.add(drawContextNode);

    }
    
	/**
	 * Set the MFrag where this node resides.
	 * @param frag The MFrag where this node resides.
	 */
	public void setMFrag(DomainMFrag frag) {
		mFrag = frag;
	}
	
	public DomainMFrag getMFrag(){
		return mFrag; 
	}
	
	public NodeFormulaTree getFormulaTree(){
		return formulaTree; 
	}
	
	/**
	 * Set the formula tree of the context node. 
	 * @param formulaTree a valid or invalid formula
	 */
	public void setFormulaTree(NodeFormulaTree formulaTree){
		
		this.formulaTree = formulaTree; 
		
		this.variableList = formulaTree.getVariableList(); 
		this.exemplarList = new ArrayList<OrdinaryVariable>();
		this.isValidFormula = isFormulaValida(formulaTree); 
		
		updateLabel(); 
	}
	
	/**
	 * Evaluate if the formula is valid
	 * (for this implementation that have some restrictions. See documentation)
	 */
	private boolean isFormulaValida(NodeFormulaTree formulaTree){
		return true; 
	}
	
    /**
     * Gera a string que representa uma formula a partir de sua arvore
     * @param formulaTree: raiz da arvore que contem a formula
     * @return a string contendo a formula no formado padrao da FOL. 
     */
    private String getFormulaForTree(NodeFormulaTree formulaTree){
    	
    	return formulaTree.getFormulaViewText(); 
    
    }
	
	/**
	 * Method responsible for deleting this context node. It makes sure to clean 
	 * the innerTermFromList and the innerTermOfList.
	 *
	 */
    
	public void delete() {
		
		for (ContextNode node: innerTermFromList) {
			node.removeInnerTermOfList(node);
		}
		for (ContextNode node: innerTermOfList) {
			// If this context node is not part of the MFrag, it means it is 
			// being used just by the context node being delete. So it can also 
			// be deleted.
			if (!mFrag.containsContextNode(node))
				node.delete();
			else {
				node.removeInnerTermFromList(node);
			}
		}
		
		mFrag.removeContextNode(this);
		
	}
	
	/*-------------------------------------------------------------*/
	
	/**
	 * Method responsible for removing the given context node from the list of 
	 * inner terms this context node has.
	 * @param contextNode The context node to remove.
	 */
	public void removeInnerTermOfList(ContextNode contextNode) {
		innerTermOfList.remove(contextNode);
	}
	
	/**
	 * Method responsible for removing the given context node from the list of 
	 * nodes where this context node is an inner term.
	 * @param contextNode The context node to remove.
	 */
	public void removeInnerTermFromList(ContextNode contextNode) {
		innerTermFromList.remove(contextNode);
	}
	
	/**
	 * 
	 */
	public void addInnerTermOfList(ContextNode contextNode){
		innerTermOfList.add(contextNode); 
	}
	
	public void addInnerTermFromList(ContextNode contextNode){
		innerTermFromList.add(contextNode); 
	}
	
	/*-------------------------------------------------------------*/
	
	/**
     *  Gets all context node's color.
     *
     * @return The color of all context node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all context node.
     *
     * @return The new color of all context node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }
	 
	@Override
	public void setSelected(boolean b) {
		drawContextNode.setSelected(b);
		super.setSelected(b);
	}
	
	@Override
	public void paint(Graphics2D graphics) {
		drawContextNode.setFillColor(getColor());
		super.paint(graphics);
	}
	
	/**
	 * update the label of this node. 
	 * The label is the formula that represents this context node.  
	 */
	
    public String updateLabel(){
    	
    	String label; 
    	if(formulaTree != null){
    	   label = formulaTree.getFormulaViewText(); 
    	}
    	else{
    	   label = " "; 
    	}
    	
    	setLabel(label);
    	return label; 
    }	
    
    /**
     * Return the formula. (format only for view)
     */
    
    public String toString(){
    	return formulaTree.getFormulaViewText();
    }

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public DrawFlatPentagon getDrawContextNode() {
		return drawContextNode;
	}

	public void setDrawContextNode(DrawFlatPentagon drawContextNode) {
		this.drawContextNode = drawContextNode;
	}

	public List<OrdinaryVariable> getExemplarList() {
		return exemplarList;
	}

	public void setExemplarList(List<OrdinaryVariable> exemplarList) {
		this.exemplarList = exemplarList;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public boolean isFormulaTreeTurned() {
		return formulaTreeTurned;
	}

	public void setFormulaTreeTurned(boolean formulaTreeTurned) {
		this.formulaTreeTurned = formulaTreeTurned;
	}

	public void setInnerTermFromList(List<ContextNode> innerTermFromList) {
		this.innerTermFromList = innerTermFromList;
	}

	public void setInnerTermOfList(List<ContextNode> innerTermOfList) {
		this.innerTermOfList = innerTermOfList;
	}

	public boolean isValidFormula() {
		return isValidFormula;
	}

	public void setValidFormula(boolean isValidFormula) {
		this.isValidFormula = isValidFormula;
	}

	public Set<OrdinaryVariable> getVariableList() {
		return variableList;
	}

	public void setVariableList(Set<OrdinaryVariable> variableList) {
		this.variableList = variableList;
	}

	public static void setColor(Color color) {
		ContextNode.color = color;
	}
    
}
 
