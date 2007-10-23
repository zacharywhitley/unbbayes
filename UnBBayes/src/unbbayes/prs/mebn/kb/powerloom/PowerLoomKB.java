package unbbayes.prs.mebn.kb.powerloom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
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
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.OVInstance;
import edu.isi.powerloom.Environment;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.powerloom.logic.TruthValue;
import edu.isi.stella.Module;
import edu.isi.stella.Stella_Object;

/**
 * Use the PowerLoom for build the knowledge base from the MTheory. The Knowledge
 * base will be used for answer about the context restrictions. The PowerLoom is
 * used how a reasoner for the first-order logic. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (06/29/07)
 */
public class PowerLoomKB implements KnowledgeBase{

    private Module moduleGenerative; 
    private Module moduleFinding; 
    
    private Environment environment = null; 
    
	private static final String POWER_LOOM_KERNEL_MODULE = "/PL-KERNEL/PL-USER/"; 
	private static final boolean CASE_SENSITIVE = false; 
	
	private String moduleGenerativeName = "GENERATIVE_MODULE"; 
	private String moduleFindingName = "FINDINGS_MODULE"; 
	
	/* 
	 * Estrutura dos módulos: 
	 * 
	 * - PL-USER
	 * - GENERATIVE_MODULE
	 * - FINDING_MODULE (possivelmente vários) 
	 *             -> deve ser filho do módulo pai... mas cada um deve ter nome individual
	 *             -> fazer uma espécie de controle de versionamento automatico para o usuário não se perder
	 * 
	 * Algum controle para indicar quais são os módulos de findings e quais são
	 * os módulos generatives para um dado arquivo PR-OWL (UBF).
	 * Problema para manter a consistência destes arquivos. (usuário alterando Generative) 
	 */
	
	private static final String POSSIBLE_STATE_SUFIX = "_state"; 
	
	private DebugPowerLoom debug = new DebugPowerLoom(true); 
	
	private static PowerLoomKB singleton = null; 
	
	private PowerLoomKB(){
		
		debug.println("Initializing...");
	    PLI.initialize();
	    debug.println("Done.");
	
	    Module fatherModule = PLI.getModule(POWER_LOOM_KERNEL_MODULE, environment); 
	    
	    moduleGenerative = PLI.createModule(moduleGenerativeName, fatherModule, CASE_SENSITIVE); 
	    moduleFinding = PLI.createModule(moduleFindingName, moduleGenerative, CASE_SENSITIVE); 

	    debug.println(moduleGenerative.moduleFullName); 
	    debug.println(moduleFinding.moduleFullName); 
        PLI.sChangeModule(moduleFindingName, environment);
	    
	}
	
	public static PowerLoomKB getInstanceKB(){
		
		if(singleton == null){
			singleton = new PowerLoomKB(); 
		}
		
		return singleton; 
			
	}
	
	
	
	
	
	/*---------- Methods for save and load modules --------------------*/
	
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
	    debug.println("Saving module..."); 
		PLI.sSaveModule(moduleGenerativeName, file.getAbsolutePath(), "REPLACE", environment); 
	    debug.println("...File save sucefull"); 
	}

	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
	    debug.println("Saving module..."); 
		PLI.sSaveModule(moduleFindingName, file.getAbsolutePath(), "REPLACE", environment); 
	    debug.println("...File save sucefull"); 
	}

	public void loadModule(File file) {
		debug.println("Loading module...");
		PLI.load(file.getAbsolutePath(), environment); 
		debug.println("File load sucefull");
	}
	
    
	
	
	/*---------- Methods for insert elements in the KB --------------------*/
	
	/**
	 * Insert the entity into KB. 
	 * 
	 * Sintaxe: 
	 * (DEFCONCEPT CATEGORY_LABEL)
	 * (ASSERT (CLOSED CATEGORY_LABEL))
	 * 
	 * Nota: As object entities s�o salvas pelo seu tipo (label) ao inv�s de
	 * pelo seu nome porque as suas instancias tem referencia apenas ao tipo. 
	 */
	public void createEntityDefinition(ObjectEntity entity){
		
		LogicObject lo = PLI.sCreateConcept(entity.getType().toString(), null, moduleGenerativeName, environment); 
		debug.println(lo.toString()); 
		
		//PLI.sEvaluate("(assert ( closed " + entity.getType() +" ) )", moduleName, environment);  
	}
	
	/**
	 * Insert the randon variable and your states into KB. 
	 * 
	 * Sintaxe: 
	 * (DEFCONCEPT SRDISTANCE_STATE (?Z) :<=> 
	 *           (MEMBER-OF ?Z (SETOF PHASER1RANGE |&| PULSECANONRANGE)))
	 *           
     * (DEFFUNCTION SRDISTANCE (
     *           (?ARG_0 SENSORREPORT_LABEL) (?ARG_1 TIMESTEP_LABEL) 
     *           (?RANGE SRDISTANCE_STATE)))
	 * 
	 */
	public void createRandonVariableDefinition(DomainResidentNode resident){
		
		List<Entity> states = resident.getPossibleValueList(); 
		
		String range = ""; 
		
		switch(resident.getTypeOfStates()){
		
		case ResidentNode.OBJECT_ENTITY:
			
			if(!resident.getPossibleValueList().isEmpty()){
			   range = "(" + "?range " +  resident.getPossibleValueList().get(0).getType().getName() + ")";
			}
			
			break; 
		
		case ResidentNode.CATEGORY_RV_STATES: 
			
			String setofList = ""; 
			for(Entity state: states){
				setofList+= state.getName() + ",";
			}
			if(!states.isEmpty()) {
				setofList = setofList.substring(0, setofList.length() - 1); //tirar a virgula final
			}
			
			//defini��o da imagem da fun��o
			String residentStateListName = resident.getName() + POSSIBLE_STATE_SUFIX; 
			PLI.sEvaluate("(defconcept " + residentStateListName + 
					"(?z) :<=> (member-of ?z ( setof " + setofList + ")))", moduleGenerativeName, environment);
			
			range = "(" + "?range " +  residentStateListName + ")"; 	
			
			break; 
		
		case ResidentNode.BOOLEAN_RV_STATES:
			
			range = "(" + "?range " +  "Boolean" + ")"; 	
			
			break; 
		
		}
		
		/* Passo 2: definir o n� residente */
		String arguments = ""; 
		List<OrdinaryVariable> listVariables = resident.getOrdinaryVariableList(); 
		
		int i = 0; 
		for(OrdinaryVariable variable: listVariables){
			arguments+= "("; 
			arguments+= "?arg_" + i + " " + variable.getValueType(); 
			arguments+= ")"; 
			i++; 
		}
		
		Stella_Object result = PLI.sEvaluate("(deffunction " + resident.getName() + " (" + arguments + range + "))", moduleGenerativeName, null); 
		debug.println(result.toString()); 
		
		//TODO closed or open world ? 
		//PLI.sEvaluate("(assert (closed " + resident.getName() + "))", moduleName, null);
		
	}
	
	/**
	 * O texto da definicao nao precisa conter o assert... apenas o conteudo do
	 * assert devidamente entre parenteses. 
	 * @param entityFinding
	 */
	public void insertEntityInstance(ObjectEntityInstance entityFinding){
		debug.println("Entity finding: " + entityFinding.getName()); 
		
		//(Starship Enterprise)
		String assertCommand = "("; 
		assertCommand+= entityFinding.getInstanceOf().getType().toString() + " "; 
		assertCommand+= entityFinding.getName(); 
		assertCommand+=")"; 
		
		PlIterator iterator = PLI.sAssertProposition(assertCommand, moduleFindingName, null); 	
		while(iterator.nextP()){
			debug.println(iterator.value.toString()); 
		}
	}

	public void insertRandonVariableFinding(RandomVariableFinding randonVariableFinding){
		
		String finding = ""; 
		finding+= "(=";
		   finding+= "("; 
		   finding+= randonVariableFinding.getNode().getName(); 
		      finding+=" "; 
		         boolean isFirst = true; //usado para não colocar virgula antes do primeiro elemento. 
		         for(Entity argument: randonVariableFinding.getArguments()){
		        	 if(isFirst){
		        		isFirst = false;  
		        	 }else{
		        		 finding+=","; 
		        	 }
		        	 finding+=argument.getName();
		         }
		   finding+= ") "; 
		   finding+= randonVariableFinding.getState().getName();  
		finding+= ")";
		
		PlIterator iterator = PLI.sAssertProposition(finding, moduleFindingName, null); 
		
		while(iterator.nextP()){
			debug.println(iterator.value.toString()); 
		}
	}
	
	/**
	 * O texto da definicao nao precisa conter o assert... apenas o conteudo do
	 * assert devidamente entre parenteses. 
	 * @param entityFinding
	 */
	public void executeRandonVariableFinding(String randonVariableFinding){
		debug.println("Randon variable finding: " + randonVariableFinding); 
		PLI.sAssertProposition(randonVariableFinding, moduleGenerativeName, null); 
	}
	

	/**
	 * Save the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	private void saveDefinitionsFile(String name){
		PLI.sSaveModule(moduleGenerativeName, name, "REPLACE", environment); 
	}
	
	/**
	 * Load the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	private void loadDefinitionsFile(String name){
		PLI.load(name, environment); 
	}
	
	
	private String executeCommand(String command){
		return PLI.sEvaluate(command, moduleFindingName, null).toString();
	}
	
    /* 
     * Estes dois métodos consideram que todos os termos da fórmula estão 
     * preenchidos da forma correta. 
     */
    public Boolean evaluateSimpleFormula(ContextNode context, List<OVInstance> ovInstances){
        
    	String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		formula+= "(";  
		formula+= makeOperatorString(formulaTree, ovInstances); 		
		formula+= ")"; 
		
		debug.println("Original formula: " + context.getLabel()); 
		debug.println("PowerLoom Formula: " + formula); 
		
	    TruthValue answer = PLI.sAsk(formula, moduleFindingName, null);
	    
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
    
    public List<String> evaluateComplexFormula(ContextNode context, List<OVInstance> ovInstances){
    	String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		formula+= "(";  
		formula+= makeOperatorString(formulaTree, ovInstances); 		
		formula+= ")"; 
		
		debug.println("Original formula: " + context.getLabel()); 
		debug.println("PowerLoom Formula: " + formula); 
		
		PlIterator iterator = PLI.sRetrieve(formula, moduleFindingName, null);
	    List result = new ArrayList<String>(); 
		
		if(iterator.nextP()){
			result.add(PLI.getNthString(iterator, 0, moduleFinding, environment)); 
		}
		
		return result; 
    }
	
	public boolean executeContextFormula(ContextNode context, List<OVInstance> ovInstances){
		
		debug.println("Generating formula for context node " + context.getName()); 
		
		String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		formula+= "(";  
		
		/* montar a formula sem preencher os valores da variaveis ordinarias ??? */
		formula+= makeOperatorString(formulaTree, ovInstances); 
		
		formula+= ")"; 
		
		debug.println("Original formula: " + context.getLabel()); 
		debug.println("PowerLoom Formula: " + formula); 
		
	    TruthValue answer = PLI.sAsk(formula, moduleFindingName, null);
	    
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
	
	/*
	 * Build a operator string from the NodeFormulaTree. (a operator and its 
	 * operandos). 
	 * @param operatorNode
	 * @return
	 */
	private String makeOperatorString(NodeFormulaTree operatorNode, List<OVInstance> ovInstances){
		
		String retorno = ""; 
		BuiltInRV builtIn = (BuiltInRV)operatorNode.getNodeVariable(); 
	    
		switch(operatorNode.getTypeNode()){
		   
		case SIMPLE_OPERATOR:
			if(builtIn instanceof BuiltInRVAnd){
				retorno+= makeConective(operatorNode, "AND", ovInstances);
			}else
				if(builtIn instanceof BuiltInRVOr){
					retorno+= makeConective(operatorNode, "OR", ovInstances);	
				}else
					if(builtIn instanceof BuiltInRVEqualTo){
						retorno+= makeEqualStatement(operatorNode, " = ", ovInstances); 	
					}else
						if(builtIn instanceof BuiltInRVIff){
							retorno+= makeConective(operatorNode, "<=>", ovInstances);
						}else
							if(builtIn instanceof BuiltInRVImplies){
								retorno+= makeConective(operatorNode, "=>", ovInstances);
							}else
								if(builtIn instanceof BuiltInRVNot){
									retorno+= makeSingleStatement(operatorNode, "NOT", ovInstances) ; 
								}	    
			                    break;
			
		case QUANTIFIER_OPERATOR:
			
			if(builtIn instanceof BuiltInRVExists){
				retorno+= makeQuantifier(operatorNode, "EXISTS", ovInstances); 
		    }else
		    	if(builtIn instanceof BuiltInRVForAll){
		    		retorno+= makeQuantifier(operatorNode, "FORALL", ovInstances);
			    }
			
			break; 
			
		default: 
				debug.println("ERROR! type of operator don't found"); 
		
		}
		
		return retorno; 
		
	}
	
	private String makeOperandoString(NodeFormulaTree operator, List<OVInstance> ovInstances){
		
		String operando = ""; 
		
		switch(operator.getTypeNode()){
		
		case SIMPLE_OPERATOR:
			operando+= makeOperatorString(operator, ovInstances); 
		break; 	
			
		case QUANTIFIER_OPERATOR:
			operando+= makeOperatorString(operator, ovInstances); 
		break; 	
		
		case OPERANDO: 
			
		   switch(operator.getSubTypeNode()){
		   
		   case OVARIABLE: 
			   OrdinaryVariable ov = (OrdinaryVariable)operator.getNodeVariable(); 
			   OVInstance ovInstance = getOVInstanceForOV(ov, ovInstances); 
			   if(ovInstance != null){
			       operando+= ovInstance.getEntity().getInstanceName(); 
			   }
			   else{ 
				   //TODO exemplar
				   operando+= "?" + ov.getName(); 
			   }
			   break; 
		   
		   case NODE:
			   
			   ResidentNodePointer node = (ResidentNodePointer)operator.getNodeVariable(); 
			   operando+= node.getResidentNode().getName(); 
			   
			   operando+= " "; 
			   
			   for(OrdinaryVariable ordVariable: node.getOrdinaryVariableList()){
				   ovInstance = getOVInstanceForOV(ordVariable, ovInstances); 
				   if(ovInstance != null){
				       operando+= ovInstance.getEntity().getInstanceName(); 
				   }
				   else{
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
	
	private String makeConective(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		
		retorno+= conectiveName; 
		
    	ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperando = listChildren.get(0);
    	
    	retorno+= "( "; 
		retorno+= makeOperandoString(leftOperando, ovInstances);   		
    	retorno+=" ) "; 
    	
    	retorno+= "( "; 
    	NodeFormulaTree rightOperando = listChildren.get(1); 
		retorno+= makeOperandoString(rightOperando, ovInstances);   
    	retorno+=" ) "; 
    	
    	return retorno; 
    	
	}
	
	private String makeEqualStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= conectiveName; 
    	
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperando = listChildren.get(0);
    	
    	retorno+= "( "; 
		retorno+= this.makeOperandoString(leftOperando, ovInstances);   		
    	retorno+=" ) "; 
    	
    	NodeFormulaTree rightOperando = listChildren.get(1); 
		retorno+= this.makeOperandoString(rightOperando, ovInstances);  
		
		return retorno; 
	}
	
	/**
	 * Used for unary operators. 
	 * 
	 * @param node
	 * @param conectiveName
	 * @param ovInstances
	 * @return
	 */
	private String makeSingleStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= conectiveName; 	
		
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	retorno+= "( "; 
		retorno+= this.makeOperandoString(listChildren.get(0), ovInstances);   		
    	retorno+=" ) "; 
		
		return retorno; 
	
	}
	
	private String makeQuantifier(NodeFormulaTree node, String name, List<OVInstance> ovInstances){
    	
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
		retorno+= makeOperatorString(formula, ovInstances);   		
		retorno+=")"; 
		
		return retorno; 
	}
	
	/*
	 * Print in the screen informations for debug. 
	 */
	public class DebugPowerLoom{
		
		private boolean debugActive = true; 
		
		public DebugPowerLoom(){
		}
		
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

	private OVInstance getOVInstanceForOV(OrdinaryVariable ov, List<OVInstance> list){
		
		for(OVInstance ovi: list){
			if (ovi.getOv() == ov){
				return ovi; 
			}
		}
		
		return null; 
	}

	public boolean executeContextFormula(ContextNode context) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
