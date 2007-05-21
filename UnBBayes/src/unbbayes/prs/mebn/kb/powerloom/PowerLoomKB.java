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
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.TruthValue;
import edu.isi.stella.Module;

/**
 * Usa o PowerLoom para montar uma base de conhecimentos a partir da MTheoria, 
 * permitindo que se use este para responder as restrições impostas pelos nós de 
 * contexto. O PowerLoom é utilizado como um reasoner de Lógica de primeira
 * ordem. 
 * 
 * @author Laecio Lima dos Santos
 */
public class PowerLoomKB implements KnowledgeBase{

    private String moduleName = "MeuModulo"; 
	private Module module; 
    
	private static PowerLoomKB singleton = null; 
	
	private PowerLoomKB(){
	    
		System.out.print("[PL]Initializing...");
	    PLI.initialize();
	    System.out.println("[PL] done.");
	
	    Module fatherModule = PLI.getModule("/PL-KERNEL/PL-USER/", null); 
	    module = PLI.createModule(moduleName, fatherModule, false); //Case Sensitive
	   
	    System.out.println("[PL] " + module.moduleFullName + " created. "); 
	    PLI.sChangeModule(moduleName, null);
	    
	}
	
	public static PowerLoomKB getInstanceKB(){
		
		if(singleton == null){
			singleton = new PowerLoomKB(); 
		}
		
		return singleton; 
			
	}
	
	public void executeConceptDefinition(ObjectEntity entity){
		
		System.out.println("PL: Definição de conceito -> " + entity.getType()); 
		PLI.sCreateConcept(entity.getType().toString(), null, moduleName, null); 
		PLI.sEvaluate("(assert ( closed " + entity.getType() +" ) )", moduleName, null); 
		
	}
	
	public void executeRandonVariableDefinition(DomainResidentNode resident){
		
		System.out.println("PL: Definição de randonVariable -> " + resident.getName()); 
		
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
		System.out.println("PL: fazendo assert -> " + entityFinding); 
		PLI.sAssertProposition(entityFinding, moduleName, null); 
		System.out.println("PL Operacao Finalizada"); 
			
	}

	/**
	 * O texto da definicao nao precisa conter o assert... apenas o conteudo do
	 * assert devidamente entre parenteses. 
	 * @param entityFinding
	 */
	public void executeRandonVariableFinding(String randonVariableFinding){
		System.out.println("PL: fazendo assert -> " + randonVariableFinding); 
		PLI.sAssertProposition(randonVariableFinding, moduleName, null);
		System.out.println("PL: Operacao Finalizada "); 
		
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
		
		String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		formula+= "(";  
		
		/* montar a formula sem preencher os valores da variaveis ordinarias ??? */
		formula+= makeOperatorString(formulaTree); 
		
		formula+= ")"; 
		
		System.out.println("-> Formula gerada: " + formula); 
		
	    TruthValue answer = PLI.sAsk(formula, moduleName, null);
	    
	    if (PLI.isTrue(answer)) {
	        System.out.println("PL: true");
	        return true; 
	      } else if (PLI.isFalse(answer)) {
	        System.out.println("PL: false");
	        return false; 
	      } else if (PLI.isUnknown(answer)) {
	        System.out.println("PL: unknown");
	        return false; 
	      }else{
	    	  return false; 
	      } 
	}
	
	public void saveDefinitionsFile(){
		PLI.sSaveModule(moduleName, "AfirmTeste.plm", "REPLACE", null); 
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
				System.out.println("[DEBUG]Problemas ao procurar o tipo de operador"); 
		
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
	
	public static void main(String[] args){
		
//		PowerLoomKB test = new PowerLoomKB(); 
//		
//		ObjectEntity teste; 
//		
//		try{
//		   Type.addType("Starship_Label"); 
//		   teste = new ObjectEntity("Starship", "Starship_Label"); 
//		   test.executeConceptDefinition(teste); 
//		}
//		catch(Exception e){
//			e.printStackTrace(); 
//		}
//		
//		System.out.println("[PL] saving module"); 
//		test.saveDefinitionsFile(); 
//		System.out.println("[PL] file save sucefull"); 
		
	}
	
	
}
