package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.kb.KnowledgeBase;

public class ContextNodeAvaliator {

	private KnowledgeBase kb; 
	
	public void evaluate(ContextNode node, OVInstance ovInstance){
		
		/* 
		 * evaluateSimpleFormula or evaluateComplexFormula... O primeiro sera utilizado se houver 
		 * ovInstances para todos os elementos da formula. O segundo será utilizado caso haja alguma 
		 * ov que não possui uma entity instance para ela. 
		 * 
		 * z = StarshipZone(st) 
		 * StarshipZone é uma variavel randomica. 
		 */
	}
	
}
