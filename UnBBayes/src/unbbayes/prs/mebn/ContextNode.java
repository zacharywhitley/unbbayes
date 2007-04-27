package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import unbbayes.gui.draw.DrawFlatPentagon;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.BooleanStatesEntity;

/**
 * 
 *
 */

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
	private DefaultMutableTreeNode formulaTree; 
	
	/* the formula in the PowerLoom format */
	
	private boolean formulaTreeTurned; //The formula don't is equal to formulaTree!!! 
	private String formula; 
	
	/* draw */ 
	
	private static Color color = ToolKitForGuiMebn.getColorContext();
	
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
    	
    	/* a lista de possiveis valores de um nó de contexto contém por
    	 * default os valores True, False e Absurd. 
    	 */
    	this.addPossibleValue(BooleanStatesEntity.getTrueStateEntity()); 
    	this.addPossibleValue(BooleanStatesEntity.getFalseStateEntity()); 
    	this.addPossibleValue(BooleanStatesEntity.getAbsurdStateEntity()); 
    	
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
	
	public DefaultMutableTreeNode getFormulaTree(){
		return formulaTree; 
	}
	
	public void setFormulaTree(DefaultMutableTreeNode formulaTree){
		this.formulaTree = formulaTree; 
		updateLabel(); 
	}
	
    /**
     * Gera a string que representa uma formula a partir de sua arvore
     * @param formulaTree: raiz da arvore que contem a formula
     * @return a string contendo a formula no formado padrao da FOL. 
     */
    private String getFormulaForTree(DefaultMutableTreeNode formulaTree){
    	
    	return ((NodeFormulaTree)(formulaTree.getUserObject())).getFormulaViewText(); 
    
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
    	   label = ((NodeFormulaTree)(formulaTree.getUserObject())).getFormulaViewText(); 
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
    	return ((NodeFormulaTree)(formulaTree.getUserObject())).getFormulaViewText();
    }
    
}
 
