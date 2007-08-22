package unbbayes.prs.mebn.kb.powerloom;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.TruthValue;
import edu.isi.stella.Module;

/**
 * Use the PowerLoom for build the knowledge base from the MTheory. The Knowledge
 * base will be used for answer about the context restrictions. The PowerLoom is
 * used how a reasoner for the first-order logic. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (06/29/07)
 */
public class PowerLoomKB implements KnowledgeBase{

    private String moduleName = "MyModule"; 
	private Module module; 
    
	DebugPowerLoom debug = new DebugPowerLoom(true); 
	
	private static PowerLoomKB singleton = null; 
	
	private PowerLoomKB(){
	    
		debug.println("Initializing...");
	    PLI.initialize();
	    debug.println("Done.");
	
	    Module fatherModule = PLI.getModule("/PL-KERNEL/PL-USER/", null); 
	    module = PLI.createModule(moduleName, fatherModule, false); //Case Sensitive
	   
	    debug.println(module.moduleFullName + " created. "); 
	    PLI.sChangeModule(moduleName, null);
	    
	}
	
	public static PowerLoomKB getInstanceKB(){
		
		if(singleton == null){
			singleton = new PowerLoomKB(); 
		}
		
		return singleton; 
			
	}
	
	public void executeConceptDefinition(ObjectEntity entity){
		
		debug.println("Concept definition: " + entity.getType()); 
		PLI.sCreateConcept(entity.getType().toString(), null, moduleName, null); 
		PLI.sEvaluate("(assert ( closed " + entity.getType() +" ) )", moduleName, null); 
		
	}
	
	public void executeRandonVariableDefinition(DomainResidentNode resident){
		
		debug.println("Randon variable definition: " + resident.getName()); 
		
		List<Entity> states = resident.getPossibleValueList(); 
		
		/* Passo 1: definir a lista de possiveis estados do nó residente */
		String setofList = ""; 
		for(Entity state: states){
			setofList+= state.getName() + ",";
		}
		if(!states.isEmpty()) {
			setofList = setofList.substring(0, setofList.length() - 1); //tirar a virgula final
		}
		
		//definição da imagem da função
		String residentStateListName = resident.getName() + "_state "; 
		PLI.sEvaluate("(defconcept " + residentStateListName + "(?z) :<=> (member-of ?z ( setof " + setofList + ")))", moduleName, null);
		
		/* Passo 2: definir o nó residente */
		String arguments = ""; 
		List<OrdinaryVariable> listVariables = resident.getOrdinaryVariableList(); 
		
		int i = 0; 
		for(OrdinaryVariable variable: listVariables){
			arguments+= "("; 
			arguments+= "?arg_" + i + " " + variable.getValueType(); 
			arguments+= ")"; 
			i++; 
		}
		
		String range = "(" + "?range " +  residentStateListName + ")"; 
		
		PLI.sEvaluate("(deffunction " + resident.getName() + " (" + arguments + range + "))", moduleName, null); 
		
		/*Passo 3: setar mundo fechado*/
		PLI.sEvaluate("(assert (closed " + resident.getName() + "))", moduleName, null);
		
	}
	
	/**
	 * O texto da definicao nao precisa conter o assert... apenas o conteudo do
	 * assert devidamente entre parenteses. 
	 * @param entityFinding
	 */
	public void executeEntityFinding(String entityFinding){
		debug.println("Entity finding: " + entityFinding); 
		PLI.sAssertProposition(entityFinding, moduleName, null); 	
	}

	/**
	 * O texto da definicao nao precisa conter o assert... apenas o conteudo do
	 * assert devidamente entre parenteses. 
	 * @param entityFinding
	 */
	public void executeRandonVariableFinding(String randonVariableFinding){
		debug.println("Randon variable finding: " + randonVariableFinding); 
		PLI.sAssertProposition(randonVariableFinding, moduleName, null); 
	}
	
	/*
	 * 
	 * Deve ser criada uma lista com as variaveis ordinarias que deverao
	 * ser instanciadas antes de se tentar resolver um nó de contexto. 
	 * As variáveis exemplares, por outro lado, não serão nunca instanciadas. 
	 * Usaremos deste ultimo fato para definir o que é uma variável ordinária
	 * e o que é uma variável exemplar: sempre que a variável ordinária não 
	 * estiver preenchida ela é uma exemplar (observe a responsabiliade de quem
	 * chama esta função: deve preencher todas as VO's, não permitindo o 
	 * prosseguimento caso haja alguma nao preenchida). 
	 *
	 */
	
	/*
	 * Nesta versão de teste, cada ov estara com a entidade que a preenche
	 * anexada a esta... A versão final pode utilizar outra estratégia...  
	 */
	
	public boolean executeContextFormula(ContextNode context){
		
		debug.println("Generating formula for context node " + context.getName()); 
		
		String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		formula+= "(";  
		
		/* montar a formula sem preencher os valores da variaveis ordinarias ??? */
		formula+= makeOperatorString(formulaTree); 
		
		formula+= ")"; 
		
		debug.println("Original formula: " + context.getLabel()); 
		debug.println("PowerLoom Formula: " + formula); 
		
	    TruthValue answer = PLI.sAsk(formula, moduleName, null);
	    
	    if (PLI.isTrue(answer)) {
	        debug.println("Result: true");
	        return true; 
	      } else if (PLI.isFalse(answer)) {
	        debug.println("Result: false");
	        return false; 
	      } else if (PLI.isUnknown(answer)) {
	        debug.println("Result: unknown");
	        return false; 
	      }else{
	    	  return false; 
	      } 
	}
	
	/**
	 * Save the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	public void saveDefinitionsFile(String name){
		PLI.sSaveModule(moduleName, name, "REPLACE", null); 
	}
	
	private String makeOperatorString(NodeFormulaTree operatorNode){
		
		String retorno = ""; 
		BuiltInRV builtIn = (BuiltInRV)operatorNode.getNodeVariable(); 
	    
		switch(operatorNode.getTypeNode()){
		   
		case SIMPLE_OPERATOR:
			if(builtIn instanceof BuiltInRVAnd){
				retorno+= makeConective(operatorNode, "AND");
			}else
				if(builtIn instanceof BuiltInRVOr){
					retorno+= makeConective(operatorNode, "OR");	
				}else
					if(builtIn instanceof BuiltInRVEqualTo){
						retorno+= makeEqualStatement(operatorNode, " = "); 	
					}else
						if(builtIn instanceof BuiltInRVIff){
							retorno+= makeConective(operatorNode, "<=>");
						}else
							if(builtIn instanceof BuiltInRVImplies){
								retorno+= makeConective(operatorNode, "=>");
							}else
								if(builtIn instanceof BuiltInRVNot){
									retorno+= makeSingleStatement(operatorNode, "NOT") ; 
								}	    
			                    break;
			
		case QUANTIFIER_OPERATOR:
			
			if(builtIn instanceof BuiltInRVExists){
				retorno+= makeQuantifier(operatorNode, "EXISTS"); 
		    }else
		    	if(builtIn instanceof BuiltInRVForAll){
		    		retorno+= makeQuantifier(operatorNode, "FORALL");
			    }
			
			break; 
			
		default: 
				debug.println("ERROR! type of operator don't found"); 
		
		}
		
		return retorno; 
		
	}
	
	private String makeOperandoString(NodeFormulaTree operator){
		
		String operando = ""; 
		
		switch(operator.getTypeNode()){
		
		case SIMPLE_OPERATOR:
			operando+= makeOperatorString(operator); 
		break; 	
			
		case QUANTIFIER_OPERATOR:
			operando+= makeOperatorString(operator); 
		break; 	
		
		case OPERANDO: 
			
		   switch(operator.getSubTypeNode()){
		   
		   case OVARIABLE: 
			   OrdinaryVariable ov = (OrdinaryVariable)operator.getNodeVariable(); 
			   if(ov.getEntity() != null){
			       operando+= ov.getEntity().getName();
			   }
			   else{  //exemplar... 
				   operando+= "?" + ov.getName(); 
			   }
			   break; 
		   
		   case NODE:
			   
			   ResidentNodePointer node = (ResidentNodePointer)operator.getNodeVariable(); 
			   operando+= node.getResidentNode().getName(); 
			   
			   operando+= " "; 
			   
			   for(OrdinaryVariable ordVariable: node.getOrdinaryVariableList()){
				   if(ordVariable.getEntity() != null){
				       operando+= ordVariable.getEntity().getName();
				   }
				   else{  //exemplar... 
					   operando+= "?" + ordVariable.getName(); 
				   }
				   operando+=" "; 
			   }
			   
			   break;   
			   
		   case ENTITY:
			   Entity entity = (Entity) operator.getNodeVariable(); 
			   operando+= entity.getName(); 
		   }
		   
		break; 
		
		}
		
		return operando; 
	}
	
	private String makeConective(NodeFormulaTree node, String conectiveName){
    	
		String retorno = ""; 
		
		retorno+= conectiveName; 
		
    	ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperando = listChildren.get(0);
    	
    	retorno+= "( "; 
		retorno+= makeOperandoString(leftOperando);   		
    	retorno+=" ) "; 
    	
    	retorno+= "( "; 
    	NodeFormulaTree rightOperando = listChildren.get(1); 
		retorno+= makeOperandoString(rightOperando);   
    	retorno+=" ) "; 
    	
    	return retorno; 
    	
	}
	
	private String makeEqualStatement(NodeFormulaTree node, String conectiveName){
    	
		String retorno = ""; 
		retorno+= conectiveName; 
    	
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperando = listChildren.get(0);
    	
    	retorno+= "( "; 
		retorno+= this.makeOperandoString(leftOperando);   		
    	retorno+=" ) "; 
    	
    	NodeFormulaTree rightOperando = listChildren.get(1); 
		retorno+= this.makeOperandoString(rightOperando);  
		
		return retorno; 
	}
	
	private String makeSingleStatement(NodeFormulaTree node, String conectiveName){
    	
		String retorno = ""; 
		retorno+= conectiveName; 	
		

		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree operando = listChildren.get(0); 
    	retorno+= "( "; 
		retorno+= this.makeOperandoString(operando);   		
    	retorno+=" ) "; 
		
		return retorno; 
	
	}
	
	private String makeQuantifier(NodeFormulaTree node, String name){
    	
		String retorno = ""; 
		retorno+= name; 
		
		List<NodeFormulaTree> listChildren = node.getChildren(); 
    	
		/*--------------------- Exemplar variables --------------------------*/
    	NodeFormulaTree listExemplares = listChildren.get(0);  //Var...
    	
    	retorno+="("; 
    	for(NodeFormulaTree exemplar : listExemplares.getChildren()){
    		OrdinaryVariable ov = (OrdinaryVariable)exemplar.getNodeVariable(); 
    		retorno+= "?" + ov.getName() + ",";
    	}
    	
    	//retirar a virgula: 
    	if(listExemplares.getChildren().size() > 0){
    	   retorno = retorno.substring(0, retorno.length() - 2); 
    	}
    	
    	retorno+=")"; 
    	
    	/*-------------------------- Formula ----------------------------------*/
    	NodeFormulaTree formula = listChildren.get(1); 
    	retorno+="("; 
		retorno+= makeOperatorString(formula);   		
		retorno+=")"; 
		
		return retorno; 
	}
	
	/*
	 * Print in the screen informations for debug. 
	 */
	public class DebugPowerLoom{
		
		private boolean debugActive = true; 
		
		public DebugPowerLoom(boolean debugActive){
			this.debugActive = debugActive; 
		}
		
		public void setDebugActive(boolean debugActive){
			this.debugActive = debugActive; 
		}
		
		public void println(String string){
			System.out.println("[KB] " + string); 
		}
		
		/* skip a line */
		public void ln(){
			System.out.println(); 
		}
	}
	
}
