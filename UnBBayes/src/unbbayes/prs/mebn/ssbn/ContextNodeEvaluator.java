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

/**
 * Class that contains methods for evaluate the context nodes of a MFrag. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ContextNodeEvaluator {

	private KnowledgeBase kb; 
	
	private MultiEntityBayesianNetwork mebn; 
	
	/*
	 * Only tests... 
	 */
	public static void main(String[] args){
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(); 
		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
		
		kb.loadModule(new File("examples/mebn/sample.plm"));
		KBFacade kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 
		
		PrOwlIO io = new PrOwlIO(); 
		try {
			avaliator.mebn = io.loadMebn(new File("examples/mebn/StarTrek30.owl"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOMebnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DomainMFrag mFrag = avaliator.mebn.getMFragByName("Starship_MFrag"); 
		
		LiteralEntityInstance literalEntityInstance; 
		OVInstance ovInstance; 
		OrdinaryVariable ov; 
		
		Type type = null;
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		List<OrdinaryVariable> ordVariableList = new ArrayList<OrdinaryVariable>(); 
		
		type = avaliator.mebn.getTypeContainer().getType("Starship_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("!ST0", type); 
		ov = new OrdinaryVariable("st", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = avaliator.mebn.getTypeContainer().getType("TimeStep_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("!T0", type); 
		ov = new OrdinaryVariable("t", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = avaliator.mebn.getTypeContainer().getType("Zone_label"); 
		ov = new OrdinaryVariable("z", type, mFrag); 
		ordVariableList.add(ov); 
		
		Boolean result = avaliator.evaluateContextNodesForOV(mFrag, ovInstanceList, ordVariableList); 
		
		System.out.println("Result= " + result);
		
	}
	
	public void evaluate(ContextNode node, OVInstance... ovInstances){
		
		/* 
		 * evaluateSimpleFormula or evaluateComplexFormula... O primeiro sera utilizado se houver 
		 * ovInstances para todos os elementos da formula. O segundo será utilizado caso haja alguma 
		 * ov que não possui uma entity instance para ela. 
		 * 
		 * z = StarshipZone(st) 
		 * StarshipZone é uma variavel randomica. 
		 */
		
		// TODO please, call MFrag.setAsUsingDefaultCPT(true) when any context node has failed. It will notify all SSBNNodes to use default CPT.
		
		
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
		
		kb.loadModule(new File("examples/mebn/sample.plm"));
		KBFacade kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 
		
		
		Collection<ContextNode> contextNodeList; 
		Set<ContextNode> simpleContextNode = new HashSet<ContextNode>();
		Set<ContextNode> complexContextNode = new HashSet<ContextNode>();
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		/* Passo 1: procurar os nós de contexto que atendem as especificações */
		contextNodeList = mFrag.getContextByOVCombination(ovList); 
		
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

		for(ContextNode context: simpleContextNode){
			result = kb.evaluateSimpleFormula(context, ovInstanceList);
			//if(!result) return result;
			System.out.println("Resulado Formula = " + result);
		}
		
		/*
		 * Passo 3: Possivelmente fazer averiguações de completude das informações e possivel pergunta ao
		 * usuário sobre informações extras neste passo.
		 */
		for(ContextNode context: complexContextNode){
			if(!context.isFormulaComplexValida(ovInstanceList)){
				//TODO throw exception???
				System.out.println("Fail...");
			}
		}
		
		/* Passo 4: avaliar os nós complexos e retornar lista*/
		for(ContextNode context: complexContextNode){
	
			OrdinaryVariable rigthTerm = context.getFreeVariable(); 
			
			List<String> entitiesResult = kbFacade.getEntityByType(rigthTerm.getValueType().getName()); 
			
//			List<String> entitiesResult = kb.evaluateComplexContextFormula(context, ovInstanceList); 
			System.out.println("List Result = ");
			for(String item: entitiesResult){
				System.out.println(item);
			}
		}
		return true; 
	}	
}
