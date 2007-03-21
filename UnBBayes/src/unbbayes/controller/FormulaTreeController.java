package unbbayes.controller;

import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.formula.FormulaTree;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.entity.Entity;

public class FormulaTreeController {

	private FormulaTree formulaTree;
	private MEBNController mebnController; 
	private NetworkController networkController; 
	private ContextNode contextNode; 
	private FormulaEditionPane formulaEditionPane; 
	
	public FormulaTreeController(NetworkController _controller, 
			                     ContextNode context, FormulaEditionPane _formulaEditionPane){
	
		this.networkController = _controller;
		this.mebnController = _controller.getMebnController(); 
		this.contextNode = context; 
		this.formulaEditionPane = _formulaEditionPane; 
	
		formulaTree = new FormulaTree(this, context); 
	
	}
	
	
	public FormulaTree getFormulaTree(){
		
		return formulaTree; 
	
	}
	
	public void addOperatorAnd(){
		 formulaTree.addOperatorAnd(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorOr(){
		formulaTree.addOperatorOr(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorNot(){
		formulaTree.addOperatorNot(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorEqualTo(){
		formulaTree.addOperatorEqualTo(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}	

	public void addOperatorIf(){
		 formulaTree.addOperatorIf(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorImplies(){
		formulaTree.addOperatorImplies();
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorForAll(){
		formulaTree.addOperatorForAll(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorExists(){
		formulaTree.addOperatorExists(); 
		mebnController.updateFormulaActiveContextNode(); 
	}	
	
	public void setNodeChoiceActive(){
		formulaEditionPane.setNodeTabActive();
	}
	
	public void setOVariableChoiveActive(){
		formulaEditionPane.setOVariableTabActive(); 
	}
	
	public void setVariableChoiceActive(){
		formulaEditionPane.setVariableTabActive(); 
	}
	
	public void addEntity(Entity entity){
		formulaTree.addEntity(entity);
	}
	
}
