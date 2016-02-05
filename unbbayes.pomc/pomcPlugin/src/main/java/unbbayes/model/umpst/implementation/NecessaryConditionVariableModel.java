/**
 * 
 */
package unbbayes.model.umpst.implementation;

import java.util.List;

import unbbayes.model.umpst.entity.EntityModel;


/**
 * Variable object of Necessary condition
 * @author Diego Marques
 */
public class NecessaryConditionVariableModel {
	
	private String id;
	private NodeFormulaTree formulaTree;
	private String formula;

	/**
	 * Constructor of effect variable object
	 */
	public NecessaryConditionVariableModel(String id, NodeFormulaTree formulaTree) {
		
		this.id = id;
		this.formulaTree = formulaTree;
		
		setFormula(null);
	}
	
	/**
	 * update the label of this node. 
	 * The label is the formula that represents this context node.  
	 */	
    public String updateLabel() {
    	String formula; 
    	if (formulaTree != null) {
    		formula = formulaTree.getFormulaViewText();    		
    	} else{
    		formula = " ";
    	}
    	setFormula(formula);
    	return formula; 
    }

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	public NodeFormulaTree getFormulaTree(){
		return formulaTree; 
	}
	
	public void setFormulaTree(NodeFormulaTree formulaTree){		
		this.formulaTree = formulaTree;
		updateLabel();
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}	
}
