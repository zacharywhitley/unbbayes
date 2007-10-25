package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.kb.KnowledgeBase;

public class ContextNodeAvaliator {

	private KnowledgeBase kb; 
	
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
	public Boolean evaluateContextNodesForOV(DomainMFrag mFrag, List<OVInstance> ovInstanceList){
		
		Collection<ContextNode> contextNodeList; 
		Set<ContextNode> simpleContextNode = new HashSet<ContextNode>();
		Set<ContextNode> complexContextNode = new HashSet<ContextNode>();
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		
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
			if(!result) return result; 
		}
		
		/*
		 * Passo 3: Possivelmente fazer averiguações de completude das informações e possivel pergunta ao
		 * usuário sobre informações extras neste passo.
		 */
		for(ContextNode context: complexContextNode){
			if(!context.isFormulaComplexValida(ovInstanceList)){
				//TODO throw exception???
			}
		}
		
		/* Passo 4: avaliar os nós complexos e retornar lista*/
		for(ContextNode context: complexContextNode){
			List<String> entitiesResult = kb.evaluateComplexContextFormula(context, ovInstanceList); 
		}
		
		return true; 
	}
	
}
