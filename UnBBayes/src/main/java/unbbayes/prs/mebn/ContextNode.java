/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import unbbayes.draw.DrawFlatPentagon;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;

/**
 * The individual of the ContextNode class represent a type of constraint impose
 * to the MFrag arguments. The constraint is represented by a formula of first 
 * order logic. 
 * */

public class ContextNode extends MultiEntityNode {
	
	private static final long serialVersionUID = 8186266877724939663L;

	private MFrag mFrag;
	
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
	 
	private Set<OrdinaryVariable> variableSet; //Variables of the formula
	
	private Set<OrdinaryVariable> exemplarSet; // Variables used in quantifiers  
	
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
    public ContextNode(String name, MFrag mFrag) {
    	
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
	public void setMFrag(MFrag frag) {
		mFrag = frag;
	}
	
	public MFrag getMFrag(){
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
		
		this.variableSet = formulaTree.getVariableList(); 
		this.exemplarSet = formulaTree.getExemplarList();
		this.isValidFormula = formulaTree.isFormulaValida(); 
		
		updateLabel(); 
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
     * Avalia se o nó de contexto é avaliavel com as OVInstances passadas como
     * argumentos. Ele é avaliável caso não fiquem variáveis ordinárias sem 
     * preenchimento. 
     * 
     * Considera-se que haja apenas uma OVInstance para cada OV. Analisar se é 
     * necessário enfraquecer esta hipotese e complementar o método. 
     *
     * @param ovInstanceSet
     * @return
     */
    public boolean isAvaliableForOVInstanceSet(Collection<OVInstance> ovInstanceSet){
    	
    	for(OrdinaryVariable ov: variableSet){
    		
    		boolean found = false; 
    		for(OVInstance ovInstance: ovInstanceSet){
    			if(ov.equals(ovInstance.getOv())){
    				found = true; 
    				break; 
    			}
    		}
    		
    		if(!found) return false; 
    		
    	}
    	
    	return true; 
    	
    }
    
    /**
     * Return all the ordinary variables that don't have one ovInstance into
     * ovInstanceSet. 
     */
    public List<OrdinaryVariable> getOVFaultForOVInstanceSet(Collection<OVInstance> ovInstanceSet){
        
    	List<OrdinaryVariable> ret = new ArrayList<OrdinaryVariable>(); 
    	
    	for(OrdinaryVariable ov: variableSet){
    		
    		boolean found = false; 
    		for(OVInstance ovInstance: ovInstanceSet){
    			if(ov.equals(ovInstance.getOv())){
    				found = true; 
    				continue; 
    			}
    		}
    		
    		if(!found) ret.add(ov); 
    		
    	}
    	
    	return ret; 
    
    }
    
    
    private boolean isParametListCorrect(OrdinaryVariable ov, Collection<OVInstance> ovInstanceSet){
    	
    	boolean found = false; 
		
    	for(OVInstance ovInstance: ovInstanceSet){
			if(ov.equals(ovInstance.getOv())){
				if(ovInstance.getEntity() != null){
					found = true; 
					break; 
				}else{
					return false; 
				}
			}
		}
		
		return found; 
		
    }
    
	public boolean isFormulaComplexValida(Collection<OVInstance> ovInstanceList){
		
		if((formulaTree.getTypeNode() == EnumType.SIMPLE_OPERATOR) && (formulaTree.getSubTypeNode() == EnumSubType.EQUALTO)){
			
			List<NodeFormulaTree> children = formulaTree.getChildren();
			if(children.size() == 2){
				
				NodeFormulaTree leftChildren = children.get(0); 
				if((leftChildren.getTypeNode() == EnumType.OPERAND) &&  (leftChildren.getSubTypeNode() == EnumSubType.NODE)){
					ResidentNodePointer pointer = (ResidentNodePointer)leftChildren.getNodeVariable(); 
					for(OrdinaryVariable ov: pointer.getOrdinaryVariableList()){ 
						if(!isParametListCorrect(ov, ovInstanceList)) return false; 
					}
				}else{
					return false; 
				}
				
				NodeFormulaTree rigthChildren = children.get(1); 
				if((rigthChildren.getTypeNode() == EnumType.OPERAND) &&  (rigthChildren.getSubTypeNode() == EnumSubType.OVARIABLE)){
					return true; 
				}else{
					return false; 
				}
			}
		}
		return false; 
	}
	
	public ResidentNode getNodeSearch(Collection<OVInstance> ovInstanceList) throws InvalidContextNodeFormulaException{
		if(!isFormulaComplexValida(ovInstanceList)){
			throw new InvalidContextNodeFormulaException(); 
		}else{
			NodeFormulaTree leftChildren = formulaTree.getChildren().get(0);
			ResidentNodePointer pointer = (ResidentNodePointer)leftChildren.getNodeVariable(); 
			return pointer.getResidentNode(); 
		}
	}
	
	public OrdinaryVariable getFreeVariable(){
		
			List<NodeFormulaTree> children = formulaTree.getChildren();
			NodeFormulaTree rigthChildren = children.get(1); 
			return (OrdinaryVariable)rigthChildren.getNodeVariable(); 
		
	}
	
    /**
     * The only case of complex node acept in this version of code is: 
     *           RandonVariable(args...) = ov, 
     *  where args are the ovs arguments of the RandonVariable and all is
     *  correctly associated with theirs ov instances. 
     *            
     * @param ovInstanceSet
     * @return
     */
    public boolean isAvaliableComplexContextNode(Collection<OVInstance> ovInstanceSet){
    	
    	
    	
    	return true; 
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

	public Set<OrdinaryVariable> getExemplarList() {
		return exemplarSet;
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
		return variableSet;
	}

	public static void setColor(Color color) {
		ContextNode.color = color;
	}
    
}
 
