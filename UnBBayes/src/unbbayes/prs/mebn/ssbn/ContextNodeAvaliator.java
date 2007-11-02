package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormula;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;

public class ContextNodeAvaliator {

	private KnowledgeBase kb; 
    private KBFacade kbFacade; 
	static MultiEntityBayesianNetwork mebn;
	
	public ContextNodeAvaliator(){

		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
		
		kb.loadModule(new File("testeGenerative.plm")); 
		kb.loadModule(new File("testeFindings.plm")); 
		
		kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 
		
	}
	
	public ContextNodeAvaliator(MultiEntityBayesianNetwork mebn, KnowledgeBase kb, KBFacade kbFacade){
		
		this.mebn = mebn; 
		this.kb = kb; 
		this.kbFacade = kbFacade; 
		
	}
	
	public static void main(String[] args){
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(); 
		
		PrOwlIO io = new PrOwlIO(); 
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek36.owl"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOMebnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DomainMFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		
		LiteralEntityInstance literalEntityInstance; 
		OVInstance ovInstance; 
		OrdinaryVariable ov; 
		
		Type type = null;
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		List<OrdinaryVariable> ordVariableList = new ArrayList<OrdinaryVariable>(); 
		
		type = mebn.getTypeContainer().getType("Starship_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("!ST0", type); 
		ov = new OrdinaryVariable("st", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("TimeStep_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("!T0", type); 
		ov = new OrdinaryVariable("t", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("Zone_label"); 
		ov = new OrdinaryVariable("z", type, mFrag); 
		ordVariableList.add(ov); 
		
		Boolean result = avaliator.evaluateContextNodesForOV(mFrag, ovInstanceList, ordVariableList); 
		
		System.out.println("Result= " + result);
		
	}
	
	
	public boolean evaluateContextNode(ContextNode node, List<OVInstance> ovInstances) throws OVInstanceFaultException{
		
		List<OrdinaryVariable> ovFaultList = node.getOVFaultForOVInstanceSet(ovInstances); 
		
		if(!ovFaultList.isEmpty()){
			throw new OVInstanceFaultException(ovFaultList); 
		}else{
			return kb.evaluateSimpleFormula(node, ovInstances);
		}
		
	}
	
	public List<String> evalutateSearchContextNode(ContextNode context, List<OVInstance> ovInstances) throws InvalidContextNodeFormula{
		
		if(!context.isFormulaComplexValida(ovInstances)){
			throw new InvalidContextNodeFormula(); 
		}else{
			List<String> entitiesResult = kb.evaluateComplexContextFormula(context, ovInstances); 
			return entitiesResult;		
		}
	}
	
	/**
	 * True - All nodes ok
	 * False - Use default distribution
	 * 
	 * List<entities> resultado. -> normal, busca geral. 
	 * 
	 * @param mFrag
	 * @param ovInstanceList
	 * @param ordVariableList
	 */
	public Boolean evaluateContextNodes(DomainMFrag mFrag, List<OVInstance> ovInstanceList, List<OrdinaryVariable> ordVariableList){
		
		Collection<ContextNode> contextNodeList; 
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		contextNodeList = mFrag.getContextByOVCombination(ovList); 
		
		for(ContextNode context: contextNodeList){
			try{
				if(!evaluateContextNode(context, ovInstanceList)){
					return false;  //use the default distribuition. 
				}
			}
			catch(OVInstanceFaultException e){
				try {
					List<String> result = evalutateSearchContextNode(context, ovInstanceList);
					if(result.isEmpty()){
						OrdinaryVariable rigthTerm = context.getFreeVariable(); 
						result = kbFacade.getEntityByType(rigthTerm.getValueType().getName());
						return false; 
					}else{
						return true; 
					}
				} catch (InvalidContextNodeFormula ie) {
					// TODO Auto-generated catch block
					ie.printStackTrace();
				} 
			}
		}
		
		return true; 
		
	}
	
	/**
	 * 
	 * 
	 * @param node
	 * @param ovInstances
	 */
	public void evaluate(ContextNode node, OVInstance... ovInstances){
		
		/* 
		 * evaluateSimpleFormula or evaluateComplexFormula... O primeiro sera utilizado se houver 
		 * ovInstances para todos os elementos da formula. O segundo será utilizado caso haja alguma 
		 * ov que não possui uma entity instance para ela. 
		 * 
		 * z = StarshipZone(st) 
		 * StarshipZone é uma variavel randomica. 
		 */
		
	}
	
	/**
	 * Evaluate all the context nodes with the ordinary variables. 
	 * 
	 * OVInstance shoud have the entityInstance setted or the algorith will
	 * return a list of entities that solve the ov.
	 *
	 * @return true if all the context nodes are OK
	 *         false if one context node are not OK
	 *         null if the evaluate return a list of entities caused by a kb search
	 *         (all others context nodes are OK)
	 */
	public Boolean evaluateContextNodesForOV(DomainMFrag mFrag, List<OVInstance> ovInstanceList, List<OrdinaryVariable> ordVariableList){
		
		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
		
		kb.loadModule(new File("testeGenerative.plm")); 
		kb.loadModule(new File("testeFindings.plm")); 
		
		KBFacade kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 
		
		Collection<ContextNode> contextNodeList; 
		Set<ContextNode> simpleContextNode = new HashSet<ContextNode>();
		Set<ContextNode> complexContextNode = new HashSet<ContextNode>();
		
		System.out.println("MFrag: " + mFrag.getName());
		for(OVInstance ovInstance: ovInstanceList){
			System.out.println("OVInstance: " + ovInstance);
		}
		for(OrdinaryVariable ovInstance: ordVariableList){
			System.out.println("OV: " + ovInstance);
		}
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		/* Passo 1: procurar os nós de contexto que atendem as especificações */
		contextNodeList = mFrag.getContextByOVCombination(ovList); 
		
		System.out.println(); 
		System.out.println("Nós de contexto a serem avaliados: ");
		for(ContextNode context: contextNodeList){
			System.out.println(context);	
		}
		
		/* Passo 2: averiguar quais nós de contexto serão avaliados de forma 
		 * simples e quais serão avaliados de forma complexa.  */
		for(ContextNode context: contextNodeList){
			if (context.isAvaliableForOVInstanceSet(ovInstanceList)){
				simpleContextNode.add(context); 
			}else{
				complexContextNode.add(context); 
			}
		}
		
		/* Passo 2.b: Os nós simples já podem ser avaliados neste passo */
		boolean result; 

		System.out.println(); 
		System.out.println("Avaliação dos nós normais: ");
		for(ContextNode context: simpleContextNode){
			System.out.println(context);
			result = kb.evaluateSimpleFormula(context, ovInstanceList);
			//if(!result) return result;
			System.out.println("Resultado: "+ result);
		}
		
		/*
		 * Passo 3: Possivelmente fazer averiguações de completude das informações e possivel pergunta ao
		 * usuário sobre informações extras neste passo.
		 */
		System.out.println(); 
		System.out.println("Avaliação dos nós de busca: ");

		System.out.println(); 
		System.out.println("Verificação de validade: ");
		for(ContextNode context: complexContextNode){
			System.out.println(context); 
			if(!context.isFormulaComplexValida(ovInstanceList)){
				//TODO throw exception???
				System.out.println("Formula Inválida");
			}else{
				System.out.println("Formula Válida");
			}
		}
		
		/* Passo 4: avaliar os nós complexos e retornar lista*/

		System.out.println(); 
		System.out.println("Resolução: ");
		for(ContextNode context: complexContextNode){
			System.out.println(context); 
			OrdinaryVariable rigthTerm = context.getFreeVariable(); 
			
			List<String> entitiesResult = kbFacade.getEntityByType(rigthTerm.getValueType().getName()); 
			
//			List<String> entitiesResult = kb.evaluateComplexContextFormula(context, ovInstanceList); 
			System.out.println("Resultado = ");
			for(String item: entitiesResult){
				System.out.println(item);
			}
		}
		
		return true; 
	}	
}
