package unbbayes.prs.mebn.kb.powerloom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;
import edu.isi.powerloom.Environment;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.powerloom.logic.TruthValue;
import edu.isi.stella.Module;
import edu.isi.stella.Stella_Object;

/**
 * This class uses PowerLoom for building the knowledge base from the MTheory.
 * The KB will be used for evaluating the context nodes. The PowerLoom is used
 * as a reasoner for the first-order logic sentences defined in the context
 * nodes.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @author Rommel Novaes Carvalho (rommel.carvalho@gmail.com)
 * @version 1.1 (2007/12/26)
 */
public class PowerLoomKB implements KnowledgeBase {

	private Module moduleGenerative;

	private Module moduleFinding;

	private Environment environment = null;

	private static final String POWER_LOOM_KERNEL_MODULE = "/PL-KERNEL/PL-USER/";

	private static final boolean CASE_SENSITIVE = false;

	private String moduleGenerativeName = "GENERATIVE_MODULE";

	private String moduleFindingName = "FINDINGS_MODULE";

	public static final String MODULE_NAME = "/PL-KERNEL/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE";
	
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

	private static PowerLoomKB singleton = null;

	private PowerLoomKB() {

		Debug.println("Initializing...");
		PLI.initialize();
		Debug.println("Done.");

		Module fatherModule = PLI.getModule(POWER_LOOM_KERNEL_MODULE,
				environment);

		moduleGenerative = PLI.createModule(moduleGenerativeName, fatherModule,
				CASE_SENSITIVE);
		moduleFinding = PLI.createModule(moduleFindingName, moduleGenerative,
				CASE_SENSITIVE);

		Debug.println(moduleGenerative.moduleFullName);
		Debug.println(moduleFinding.moduleFullName);
		PLI.sChangeModule(moduleFindingName, environment);

	}

	/**
	 * It uses the singleton design pattern for returning the only instance of
	 * this class.
	 * 
	 * @return the only instance of this class.
	 */
	public static PowerLoomKB getInstanceKB() {

		if (singleton == null) {
			singleton = new PowerLoomKB();
		}

		return singleton;

	}
	
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Methods for save and load modules                                       */
	/*-------------------------------------------------------------------------*/	
	
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		Debug.println("Saving module...");
		PLI.sSaveModule(moduleGenerativeName, file.getAbsolutePath(),
				"REPLACE", environment);
		Debug.println("...File save sucefull");
	}

	/**
	 * @see KnowledgeBase
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		Debug.println("Saving module...");
		PLI.sSaveModule(moduleFindingName, file.getAbsolutePath(), "REPLACE",
				environment);
		Debug.println("...File save sucefull");
	}

	/**
	 * @see KnowledgeBase
	 */
	public void loadModule(File file) {
		Debug.println("Loading module...");
		PLI.load(file.getAbsolutePath(), environment);
		Debug.println("File load sucefull");
	}

	/**
	 * @see KnowledgeBase
	 */
	public void clearKnowledgeBase() {
		PLI.clearModule(moduleFinding);
		PLI.clearModule(moduleGenerative);
	}
	
	
	
	

	/*-------------------------------------------------------------------------*/
	/*Methods for insert elements in the KB                                    */
	/*-------------------------------------------------------------------------*/	
	
	/**
	 * Syntax example: 
	 * (DEFCONCEPT CATEGORY_LABEL)
	 * 
	 * Nota: As object entities s�o salvas pelo seu tipo (label) ao inv�s de
	 * pelo seu nome porque as suas instancias tem referencia apenas ao tipo. 
	 * Note: The object entities are saved by its type (label) instead of its
	 * name because its instances reference only its type.
	 * 
	 * @see KnowledgeBase
	 */
	public void createEntityDefinition(ObjectEntity entity) {

		LogicObject lo = PLI.sCreateConcept(entity.getType().toString(), null,
				moduleGenerativeName, environment);
		Debug.println(lo.toString());

		// PLI.sEvaluate("(assert ( closed " + entity.getType() +" ) )",
		// moduleName, environment);
	}

	/**
	 * Syntax example: 
	 * ;;States definition 
	 * (DEFCONCEPT SRDISTANCE_STATE (?Z) :<=>
	 * (MEMBER-OF ?Z (SETOF PHASER1RANGE PULSECANONRANGE))) 
	 * ;;Arguments definition 
	 * (DEFFUNCTION SRDISTANCE ( (?ARG_0 SENSORREPORT_LABEL) (?ARG_1
	 * TIMESTEP_LABEL) (?RANGE SRDISTANCE_STATE)))
	 * 
	 * @see KnowledgeBase
	 */
	public void createRandomVariableDefinition(DomainResidentNode resident){
		
		List<StateLink> links = resident.getPossibleValueLinkList(); 
		
		String range = ""; 
		
		switch(resident.getTypeOfStates()){
		
		case ResidentNode.OBJECT_ENTITY:

			if (!resident.getPossibleValueLinkList().isEmpty()) {
				String type = resident.getPossibleValueLinkList().get(0)
						.getState().getType().getName();
				range = "(" + "?range " + type + ")";
			}

			break;

		case ResidentNode.CATEGORY_RV_STATES:

			String setofList = "";
			for (StateLink state : links) {
				setofList += state.getState().getName() + " ";
			}

			// definition of the function image
			String residentStateListName = resident.getName()
					+ POSSIBLE_STATE_SUFIX;
			PLI.sEvaluate("(defconcept " + residentStateListName
					+ "(?z) :<=> (member-of ?z ( setof " + setofList + ")))",
					moduleGenerativeName, environment);

			range = "(" + "?range " + residentStateListName + ")";

			break;

		// case ResidentNode.BOOLEAN_RV_STATES:
		//			
		// range = "(" + "?range " + "Boolean" + ")";
		//			
		// break;

		}
		
		/* Step 2: define the resident node with its arguments */
		String arguments = ""; 
		List<OrdinaryVariable> listVariables = resident.getOrdinaryVariableList(); 
		
		int i = 0; 
		for(OrdinaryVariable variable: listVariables){
			arguments+= "("; 
			arguments+= "?arg_" + i + " " + variable.getValueType(); 
			arguments+= ")"; 
			i++; 
		}

		if (resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) {
			Stella_Object result = PLI.sEvaluate("(defrelation "
					+ resident.getName() + " (" + arguments + "))",
					moduleGenerativeName, null);
			Debug.println(result.toString());
		} else {
			Stella_Object result = PLI.sEvaluate("(deffunction "
					+ resident.getName() + " (" + arguments + range + "))",
					moduleGenerativeName, null);
			Debug.println(result.toString());

		}
		// TODO closed or open world ?
		// PLI.sEvaluate("(assert (closed " + resident.getName() + "))",
		// moduleName, null);

	}

	/**
	 * Syntax example: 
	 * (ASSERT (STARSHIP_LABEL ST0))
	 * 
	 * @see KnowledgeBase
	 */
	public void insertEntityInstance(ObjectEntityInstance entityInstance) {
		Debug.println("Entity finding: " + entityInstance.getName());

		// (STARSHIP_LABEL ST0)
		String assertCommand = "(";
		assertCommand += entityInstance.getInstanceOf().getType().toString()
				+ " ";
		assertCommand += entityInstance.getName();
		assertCommand += ")";

		PlIterator iterator = PLI.sAssertProposition(assertCommand,
				moduleFindingName, null);
		while (iterator.nextP()) {
			Debug.println(iterator.value.toString());
		}
	}

	/**
	 * Syntax example: 
	 * (ASSERT (not (ISOWNSTARSHIP ST4))) for boolean states 
	 * or
	 * (ASSERT (= (STARSHIPZONE ST4) Z2)) for other kind of states
	 * 
	 * @see KnowledgeBase
	 */
	public void insertRandomVariableFinding(
	        RandomVariableFinding randonVariableFinding){
		
		String finding = "";
		
		if(randonVariableFinding.getNode().getTypeOfStates() == DomainResidentNode.BOOLEAN_RV_STATES){
			finding+= "(";
			   if(randonVariableFinding.getState().getName().equals("false")){
				   finding+= "NOT";
				   finding+= "("; 
			   }
			   finding+= randonVariableFinding.getNode().getName(); 
			      finding+=" "; 
			         boolean isFirst = true; //usado para não colocar virgula antes do primeiro elemento. 
			         for(Entity argument: randonVariableFinding.getArguments()){
			        	 if(isFirst){
			        		isFirst = false;  
			        	 }else{
			        		 finding+=" "; 
			        	 }
			        	 finding+=argument.getName();
			         }
			if(randonVariableFinding.getState().getName().equals("false")){
			finding+= ") ";
			} 
			finding+= ")";	
		}else{ 
			finding+= "(=";
			finding+= "("; 
			finding+= randonVariableFinding.getNode().getName(); 
			finding+=" "; 
			
			// Used to avoid ',' before the first element
			boolean isFirst = true; 
			for(Entity argument: randonVariableFinding.getArguments()){
			   	 if(isFirst){
			      		isFirst = false;  
			   	 }else{
			       		 finding+=" "; 
			   	 }
			   	 finding+=argument.getName();
			}
			finding+= ") "; 
			finding+= randonVariableFinding.getState().getName();  
			finding+= ")";	
		}

		PlIterator iterator = PLI.sAssertProposition(finding,
				moduleFindingName, null);

		while (iterator.nextP()) {
			Debug.println(iterator.value.toString());
		}
	}

	/**
	 * Syntax example: 
	 * ( Â¬ IsOwnStarship(st) ) for UnBBaes syntax 
	 * (NOT( IsOwnStarship ST4 ) ) for PowerLoom syntax
	 * 
	 * @see KnowledgeBase
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {

		String formula = "";

		NodeFormulaTree formulaTree = (NodeFormulaTree) context
				.getFormulaTree();

		formula += "(";
		if ((formulaTree.getTypeNode() == enumType.OPERAND)
				&& (formulaTree.getSubTypeNode() == enumSubType.NODE)) {
			formula += makeOperandString(formulaTree, ovInstances);
		} else {
			formula += makeOperatorString(formulaTree, ovInstances);
		}
		formula += ")";

		Debug.println("Original formula: " + context.getLabel());
		Debug.println("PowerLoom formula: " + formula);

		TruthValue answer = PLI.sAsk(formula, moduleFindingName, null);

		if (PLI.isTrue(answer)) {
			Debug.println("Result: true");
			return true;
		} else if (PLI.isFalse(answer)) {
			Debug.println("Result: false");
			return false;
		} else if (PLI.isUnknown(answer)) {
			Debug.println("Result: unknown");
			return false;
		} else {
			return false;
		}
	}
    
	/**
	 * @see KnowledgeBase
	 */
    public List<String> evaluateSearchContextNodeFormula(ContextNode context, List<OVInstance> ovInstances)
                                     throws OVInstanceFaultException{
    	String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		List<OrdinaryVariable> ovFaultList = context.getOVFaultForOVInstanceSet(ovInstances); 
		
		//This implementation treat only the case where have only one search variable
		if(ovFaultList.size()>1){
			throw new OVInstanceFaultException(ovFaultList); 
		}
		
		//The search isn't necessary. 
		if(ovFaultList.size() == 0){
			return null; 
		}
		
		//The list have only one element
		OrdinaryVariable ovFault = ovFaultList.get(0); 
		
		//Build the retrieve statement. 
		formula+=" all ";
		
		//List of variables of retrieve
		formula+="(" + "?" + ovFault.getName() + " " + ovFault.getValueType() + ")"; 
		
		//Formula
		formula+= "(";  
		formula+= makeOperatorString(formulaTree, ovInstances); 		
		formula+= ")"; 
		
		Debug.println("Original formula: " + context.getLabel()); 
		Debug.println("PowerLoom Formula: " + formula); 
		
		PlIterator iterator = PLI.sRetrieve(formula, moduleFindingName, null);
	    List result = new ArrayList<String>(); 
		
	    iterator.length();
	    
		while(iterator.nextP()){
			result.add(PLI.getNameInModule(iterator.value, moduleFinding, environment)); 
		}

		return result;
	}

	
    
    
    
    
    /*-------------------------------------------------------------------------*/
    /* Auxiliary Methods (Interface)                                           */
    /*-------------------------------------------------------------------------*/    

	/**
	 * @see KnowledgeBase
	 */
	public boolean existEntity(String name) {
		
		Stella_Object so = PLI.sGetObject(name, moduleFindingName, environment); 
		
		if (so != null){
			return true;
		}else{
			return false; 
		}
	}

	/**
	 * @see KnowledgeBase
	 */
    public StateLink searchFinding(DomainResidentNode randomVariable, Collection<OVInstance> listArguments) {
		
		String finding = "";
		
		if(randomVariable.getTypeOfStates() == DomainResidentNode.BOOLEAN_RV_STATES){
			finding+= randomVariable.getName() + " ";
			for(OVInstance argument: listArguments){
				finding+= " " + argument.getEntity().getInstanceName(); 
			}
			TruthValue value = PLI.sAsk(finding, moduleFindingName, environment);
			
			StateLink exactValue = null; 
			//TODO throw a exception when the node dont have the argument... kb inconsistency. 
			if(PLI.isTrue(value)){
				exactValue = randomVariable.getPossibleValueByName("true"); 
			}else{
				if(PLI.isFalse(value)){
					exactValue = randomVariable.getPossibleValueByName("false"); 
				}else{
					if(PLI.isUnknown(value)){
						exactValue = null; 
					}
				}
			}

			return exactValue; 
			
		}else{
			finding+="all ?x"; 
			finding+="("; 
			finding+= randomVariable.getName();
			
			for(OVInstance argument: listArguments){
				finding+= " " + argument.getEntity().getInstanceName(); 
			}
			
			finding += " ?x"; 
			finding+= ")";
			PlIterator iterator = PLI.sRetrieve(finding, moduleFindingName, environment); 
			
			//TODO throw a exception when the search return more than one result...
			if(iterator.nextP()){
				String state = PLI.getNthString(iterator, 0, moduleFinding, environment);
				return randomVariable.getPossibleValueByName(state); 
			}else{
				return null; 
			}	
		}
		
		
	}
	
	/**
	 * @see KnowledgeBase
	 */
	public List<String> getEntityByType(String type) {
		
		List<String> list = new ArrayList<String>(); 
		
		PlIterator iterator = PLI.sGetConceptInstances(type, moduleFindingName, environment); 
		while (iterator.nextP()){
			list.add(PLI.getNthString(iterator, 0, moduleFinding, environment)); 
		}
			
		return list; 
	}
	
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Auxiliary Methods                                                       */
	/*-------------------------------------------------------------------------*/
	
	/**
	 * It makes an operator string from the NodeFormulaTree. (an operator and
	 * its operands).
	 * 
	 * @param operatorNode
	 * @param ovInstances
	 * @return
	 */
	private String makeOperatorString(NodeFormulaTree operatorNode,
			List<OVInstance> ovInstances) {

		String operator = "";
		BuiltInRV builtIn = (BuiltInRV) operatorNode.getNodeVariable();

		switch (operatorNode.getTypeNode()) {

		case SIMPLE_OPERATOR:
			if(builtIn instanceof BuiltInRVAnd){
				operator+= makeBynaryStatement(operatorNode, "AND", ovInstances);
			}else
				if(builtIn instanceof BuiltInRVOr){
					operator+= makeBynaryStatement(operatorNode, "OR", ovInstances);	
				}else
					if(builtIn instanceof BuiltInRVEqualTo){
						operator+= makeEqualStatement(operatorNode, " = ", ovInstances); 	
					}else
						if(builtIn instanceof BuiltInRVIff){
							operator+= makeBynaryStatement(operatorNode, "<=>", ovInstances);
						}else
							if(builtIn instanceof BuiltInRVImplies){
								operator+= makeBynaryStatement(operatorNode, "=>", ovInstances);
							}else
								if(builtIn instanceof BuiltInRVNot){
									operator+= makeUnaryStatement(operatorNode, "NOT", ovInstances) ; 
								}	    
			                    break;
			
		case QUANTIFIER_OPERATOR:

			if (builtIn instanceof BuiltInRVExists) {
				operator += makeQuantifier(operatorNode, "EXISTS", ovInstances);
			} else if (builtIn instanceof BuiltInRVForAll) {
				operator += makeQuantifier(operatorNode, "FORALL", ovInstances);
			}

			break;

		default:
			Debug.println("ERROR! type of operator don't found");

		}

		return operator;

	}
	
	/**
	 * It makes an operand string from the NodeFormulaTree.
	 * 
	 * @param operator
	 * @param ovInstances
	 * @return
	 */
	private String makeOperandString(NodeFormulaTree operator, List<OVInstance> ovInstances){
		
		String operand = ""; 
		
		switch(operator.getTypeNode()){
		
		case SIMPLE_OPERATOR:
			operand += makeOperatorString(operator, ovInstances);
			break;

		case QUANTIFIER_OPERATOR:
			operand += makeOperatorString(operator, ovInstances); 
		break; 	
		
		case OPERAND: 
			
		   switch(operator.getSubTypeNode()){
		   
		   case OVARIABLE: 
			   OrdinaryVariable ov = (OrdinaryVariable)operator.getNodeVariable(); 
			   OVInstance ovInstance = getOVInstanceForOV(ov, ovInstances); 
			   if(ovInstance != null){
			       operand+= ovInstance.getEntity().getInstanceName(); 
			   }
			   else{ 
				   operand+= "?" + ov.getName(); 
			   }
			   break; 
		   
		   case NODE:
			   
			   ResidentNodePointer node = (ResidentNodePointer)operator.getNodeVariable(); 
			   operand+= node.getResidentNode().getName(); 
			   
			   operand+= " "; 
			   
			   for(OrdinaryVariable ordVariable: node.getOrdinaryVariableList()){
				   ovInstance = getOVInstanceForOV(ordVariable, ovInstances); 
				   if(ovInstance != null){
				       operand+= ovInstance.getEntity().getInstanceName(); 
				   }
				   else{
					   operand+= "?" + ordVariable.getName(); 
				   }
				   operand+=" "; 
			   }
			   
			   break;   
			   
		   case ENTITY:
			   Entity entity = (Entity) operator.getNodeVariable(); 
			   operand+= entity.getName(); 
		   }
		   
		break; 
		
		}

		return operand;
	}
	
	/*
	 * Used for: 
	 * AND
	 * OR
	 * =>
	 * <=>
	 */
	private String makeBynaryStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		
		retorno+= conectiveName; 
		
    	ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperando = listChildren.get(0);
    	
    	retorno+= "( "; 
		retorno+= makeOperandString(leftOperando, ovInstances);   		
    	retorno+=" ) "; 
    	
    	retorno+= "( "; 
    	NodeFormulaTree rightOperando = listChildren.get(1); 
		retorno+= makeOperandString(rightOperando, ovInstances);   
    	retorno+=" ) "; 
    	
    	return retorno; 
    	
	}
	
	/* 
	 * Used for:
	 * =
	 */
	private String makeEqualStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= conectiveName; 
    	
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
		/*
		 * Cases:
		 * StarshipZone(st) = z   (RandomVariable = OrdinaryVariable)
		 * SkyClean(today) = yes  (RandomVariable = State (Categorical or boolean))
		 * st = s                 (OrdinaryVariable = OrdinaryVariable)
		 */
		
    	NodeFormulaTree leftOperand = listChildren.get(0);
    	
    	if((leftOperand.getTypeNode() == enumType.OPERAND)&&(leftOperand.getSubTypeNode() == enumSubType.OVARIABLE)){
    		retorno+= " ";
    		retorno+= this.makeOperandString(leftOperand, ovInstances);
    		retorno+= " ";
    	}else{
    		retorno+= " ( ";
    		retorno+= this.makeOperandString(leftOperand, ovInstances);
    		retorno+= " ) ";
    	}
    	
    	NodeFormulaTree rightOperand = listChildren.get(1); 
		retorno+= this.makeOperandString(rightOperand, ovInstances);  
		
		return retorno; 
	}
	
	/*
	 * Used for:
	 * NOT
	 */
	private String makeUnaryStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= conectiveName; 	
		
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	retorno+= "( "; 
		retorno+= this.makeOperandString(listChildren.get(0), ovInstances);   		
    	retorno+=" ) "; 
		
		return retorno; 
	
	}
	
	/* 
	 * Used for:
	 * ALL
	 * EXISTS
	 */
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

	private OVInstance getOVInstanceForOV(OrdinaryVariable ov,
			List<OVInstance> list) {

		for (OVInstance ovi : list) {
			if (ovi.getOv().equals(ov)) {
				return ovi;
			}
		}

		return null;
	}

	/*
	 * Save the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	private void saveDefinitionsFile(String name){
		PLI.sSaveModule(moduleGenerativeName, name, "REPLACE", environment); 
	}
	
	/*
	 * Load the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	private void loadDefinitionsFile(String name){
		PLI.load(name, environment); 
	}
	
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Other public methods                                                    */
	/*-------------------------------------------------------------------------*/
	
	/**
	 * Debug method used for execute a command directely in the Powerloom. 
	 * 
	 * @param command A valid statement of powerloom (in PowerLoom syntaxe SURROUNDED by parentesis)
	 * @return the result of execution (console text of PowerLoom)
	 */
	public String executeCommand(String command){
		return PLI.sEvaluate(command, moduleFindingName, null).toString();
	}
	
	
	
	
	

}
