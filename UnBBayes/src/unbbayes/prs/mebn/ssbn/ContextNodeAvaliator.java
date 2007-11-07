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
import unbbayes.util.Debug;

/**
 * Class that contains methods for evaluate the context nodes of a MFrag. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ContextNodeAvaliator {

	private KnowledgeBase kb; 
    private KBFacade kbFacade; 
	
	public ContextNodeAvaliator(KnowledgeBase kb, KBFacade kbFacade){
		
		this.kb = kb; 
		this.kbFacade = kbFacade; 
		
	}

	/**
	 * Evaluate a context node. 
	 * 
	 * @param node
	 * @param ovInstances
	 * @return
	 * @throws OVInstanceFaultException
	 */
	public boolean evaluateContextNode(ContextNode node, List<OVInstance> ovInstances) throws OVInstanceFaultException{
		
		List<OrdinaryVariable> ovFaultList = node.getOVFaultForOVInstanceSet(ovInstances); 
		
		if(!ovFaultList.isEmpty()){
			throw new OVInstanceFaultException(ovFaultList); 
		}else{
			return kb.evaluateSimpleFormula(node, ovInstances);
		}
		
	}
	
	/**
	 * Evaluate a search context node. A search context node is a node that return
	 * instances of the Knowledge Base that satisfies a restriction. 
	 * 
	 * Ex.: z = StarshipZone(st). 
	 * -> return all the z's. 
	 * 
	 * Note: for this implementation, only formulas in the example format are 
	 * accept. For all others formats, a exception InvalidContextNodeFormula will
	 * be throw. 
	 * 
	 * @param context
	 * @param ovInstances
	 * @return
	 * @throws InvalidContextNodeFormula
	 */
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
	public void evaluateContextNodes(DomainMFrag mFrag, List<OVInstance> ovInstanceList, List<OrdinaryVariable> ordVariableList){
		
		Debug.setDebug(true); 
		
		Collection<ContextNode> contextNodeList; 
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		contextNodeList = mFrag.getContextByOVCombination(ovList); 
		
		Debug.println(""); 
		Debug.println("Evaluating... "); 
		Debug.println(""); 
		
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getFormula()); 
			try{
				if(!evaluateContextNode(context, ovInstanceList)){
					Debug.println("Result = FALSE. Use default distribution "); 
//					return false;  //use the default distribution. 
				}
			}
			catch(OVInstanceFaultException e){
				try {
					Debug.println("OVInstance Fault. Try evaluate a search. "); 
					List<String> result = evalutateSearchContextNode(context, ovInstanceList);
					if(result.isEmpty()){
						
						OrdinaryVariable rigthTerm = context.getFreeVariable(); 
						result = kbFacade.getEntityByType(rigthTerm.getValueType().getName());
						
						Debug.println("No information in Knowlege Base"); 
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
						
//						return false; 
					}else{
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
//						return true; 
					}
				} catch (InvalidContextNodeFormula ie) {
					Debug.println("Invalid Context Node: the formula don't is accept."); 
					// TODO Auto-generated catch block
					ie.printStackTrace();
				} 
			}
			Debug.println(""); 
		}
		
//		return true; 
		
	}
	
}
