package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

public class MFragContextNodeAvaliator  implements IMFragContextNodeAvaliator {

	private KnowledgeBase kb; 
	private SSBN ssbn; 
	
	public MFragContextNodeAvaliator (SSBN ssbn) {
		this.ssbn = ssbn; 
		this.kb = ssbn.getKnowledgeBase(); 
	}
	
	/**
	 * Evaluate the context nodes of a MFrag using the ordinary variables already
	 * instanciated. <b>
	 * 
	 * - Ordinary variables don't instanciated yet will be instanciated. <b>
	 * - Should have more than one reference for a ordinary variable <b>
	 * - Should have reference uncertainty problem (how return this problem) <b>
	 * - Should have ordinary variables that don't have instance for it <b>
	 * 
	 * Cases: 
	 * - Trivial case
	 * - Simple Search (one entity for ov)
	 * - Compost Search (more than one entity)
	 * - Undefined Context (more than one possible result)
	 * 
	 * @param mfrag MFrag evaluated
	 * @param ovInstances Ordinary variables already instanciated. 
	 * @throws SSBNNodeGeneralException 
	 * @throws ImplementationRestrictionException 
	 * @throws OVInstanceFaultException 
	 * @throws MFragContextFailException 
	 */
	public MFragInstance evaluateMFragContextNodes(MFragInstance mFragInstance) 
	                   throws ImplementationRestrictionException, 
	                          MFragContextFailException{
		
		//Log
		IdentationLevel level1 = new IdentationLevel(null);  
		IdentationLevel level2 = new IdentationLevel(level1); 
		IdentationLevel level3 = new IdentationLevel(level2); 
		IdentationLevel level4 = new IdentationLevel(level3); 
		IdentationLevel level5 = new IdentationLevel(level4); 
		
		//Consider that the Tree with the know ordinary variables are already mounted. 
		//Consider that the only ordinary variables filled are the already know OV
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level4, false, "1) Loop for evaluate context nodes.");
		}
		
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb);
		
		List<ContextNode> possibleReferenceUncertainNodes = new 
				ArrayList<ContextNode>(); 
		
		//For each context node: 
		// 1- If don't have empty variable ordinaries, evaluate the node. 
		// 2 - If have empty variables ordinaries, 
		//     A - If the node is in the format OV(X)=Y, keep in the possibleReferenceUncertainNodes list
		//     B - Otherwise, try to evaluate using the search strategy. 
		
		
		for(ContextNode contextNode: mFragInstance.getContextNodeList()){
			
			if (logManager != null) {
				logManager.printText(level4, false, "Context Node: " + contextNode);
			}
			
			//---> 1) Verify if the context node is solved only with the know arguments. 
			List<OrdinaryVariable> ovInstancesFault = contextNode.getOVFaultForOVInstanceSet(ovInstances); 

			if(ovInstancesFault.size() == 0){
				
				if (logManager != null) {
					logManager.printText(level5, false, "All ov's of the context node are setted");
				}
				
				boolean result = kb.evaluateContextNodeFormula(contextNode, ovInstances);
				
				if(result){
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_OK); 
					if (logManager != null) {
						logManager.printText(level5, false, "Node evaluated OK");
					}
					
					continue; 
				}else{
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL);
					mFragInstance.setUseDefaultDistribution(true); 
					if (logManager != null) {
						logManager.printText(level5, false, "Node evaluated FALSE");
					}
					continue; //The MFragInstance continue to be evaluated only
					          //to show to the user a network more complete. 
				}
				
			}else{ //ovInstancesFault.size() != 0		
				

				if(avaliator.testContextNodeFormatRestriction(contextNode)){
					if (logManager != null) {
						logManager.printText(level5, false, 
								"The uncertain reference is possible. ");
					}
					possibleReferenceUncertainNodes.add(contextNode); 
					continue; 
				}
				
				if (logManager != null) {
					logManager.printText(level5, false, "Some ov's of the context node aren't filled");
					logManager.printText(level5, false, "Try 1: Use the search strategy");
				}
				
				//---> 2) Use the Entity Tree Strategy. 
				SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, ovInstances); 

				if(searchResult!= null){  

					if (logManager != null) {
						logManager.printText(level5, false, "Search Result: ");
						for(String[] result: searchResult.getValuesResultList()){
							String resultOv = " > "; 
							for(int i = 0; i < result.length; i++){
								resultOv += result[i] + " "; 
							}
							if (logManager != null) {
								logManager.printText(level5, false, resultOv);
							}
						}
					}
					
					//Result valid results: Add the result to the tree of result.
					try {

						mFragInstance.addOVValuesCombination(
								searchResult.getOrdinaryVariableSequence(), 
								searchResult.getValuesResultList());

						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_OK); 
						
						if (logManager != null) {
							logManager.printText(level5, false, "Node evaluated OK");
						}
						
					} catch (MFragContextFailException e) {
						
						e.printStackTrace(); 
						
						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(level5, false,"Context Node FAIL: use the default distribution");
						}
						
						//Here, the context node fail adding the values for a ordinary variable
						//fault. This fail impossibilite the evaluation of the rest of the MFragInstance, 
						//because, this node, used to recover the possible values, fail. 
						throw e; 
					}

					continue;

				}else{

					//TODO Implement the iteration strategy, where the user gives 
					//values for the ordinary variables not filled yet. 

					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL); 
					mFragInstance.setUseDefaultDistribution(true); 
					if (logManager != null) {
						logManager.printText(level5, false, "Fail: " + 
								ImplementationRestrictionException.INVALID_CTXT_NODE_FORMULA);
						logManager.printText(level4, false,"Still ov fault... nothing more to do. " +
								"Use default distribution");
					}
					
				}
			}
		}
		
		
		//------------------------------------------------------------------------------
		//Verify if the reference uncertain strategy is possible for the candidate nodes
		//------------------------------------------------------------------------------
		
		System.out.println("Evaluate context nodes with the possibility of use reference uncertain nodes");
		
		possibleReferenceUncertainNodes = trySimpleEvaluationOfPossibleReferenceUncertainNodes(
				mFragInstance, possibleReferenceUncertainNodes);	
		
		if(possibleReferenceUncertainNodes.size() > 0) {
			possibleReferenceUncertainNodes = trySearchStrategyForPossibleReferenceUncertainNodes(
					mFragInstance, possibleReferenceUncertainNodes);
		}
		
		if(possibleReferenceUncertainNodes.size() > 0) {
			tryUncertainReferenceStrategy(mFragInstance, possibleReferenceUncertainNodes);
		}

		
		//------------------------------------------------------------------------------
		//Return a list of ordinary variables of the MFrag that don't are evaluated yet. 
		//------------------------------------------------------------------------------
		
		if (logManager != null) {
			logManager.printText(level4, false,"2) Loop for evaluate the IsA nodes"); 
		}
		
		List<OrdinaryVariable> ovDontFoundYetList = new ArrayList<OrdinaryVariable>(); 
		for(OrdinaryVariable ov: mFragInstance.getMFragOrigin().getOrdinaryVariableList()){
			if(mFragInstance.getOVInstanceListForOrdinaryVariable(ov).size() == 0){
				ovDontFoundYetList.add(ov);
			}
		}
		
		//Evaluate this ordinary variables
		for(OrdinaryVariable ov: ovDontFoundYetList){
			//no context node about this ov... the value is unknown, we should 
			//consider all the possible values. Note the use of the Closed Word
			//Asspetion. 
			if (logManager != null) {
				logManager.printText(level4, false,"Evaluate IsA for OV " + ov.getName());
			}
			
			List<String> possibleValues = kb.getEntityByType(ov.getValueType().getName()); 
			
			if (logManager != null) {
				for(String possibleValue: possibleValues){
					logManager.printText(level4, false, "  > " + possibleValue);
				}
			}
			
			String possibleValuesArray[] = possibleValues.toArray(new String[possibleValues.size()]); 
			List<String[]> entityValuesArray = new ArrayList<String[]>(); 
			
			for(String possibleValue: possibleValuesArray){
				entityValuesArray.add(new String[]{possibleValue}); 
			}
				                                                                
			OrdinaryVariable ovArray[] = new OrdinaryVariable[1];
			ovArray[0] = ov; 
			
			try {
				mFragInstance.addOVValuesCombination(ovArray, entityValuesArray);
			} catch (MFragContextFailException e) {
//				e.printStackTrace(); 
				Debug.println(this.getClass(), "", e);
				mFragInstance.setUseDefaultDistribution(true); 
			}
		}
		
		List<String[]> possibleCombinationsList = mFragInstance.recoverAllCombinationsEntitiesPossibles(); 
		if (logManager != null) {
			logManager.printBox3Bar(level4);
			logManager.printBox3(level4, 0, "Result of evalation of the context nodes (Possible combinations): ");
			for(String[] possibleCombination : possibleCombinationsList){
				String ordinaryVariableComb = " > ";
				for(String ordinaryVariableValue: possibleCombination){
					ordinaryVariableComb += " " + ordinaryVariableValue;
				}
				ordinaryVariableComb += " < ";
				logManager.printBox3(level4, 1, ordinaryVariableComb);
			}
			logManager.printBox3Bar(level4);
		}
		
		//Return mFragInstance with the ordinary variables filled. 
		return mFragInstance; 
	}

	

	/**
	 * Try to solve the possible reference uncertain nodes (nodes in format 
	 * ResidentNode(ov1)=ov2, where ov1 or ov2, or both are unkown. The evaluation
	 * is considered OK only if have only one possible result for the ordinary 
	 * variables of the node. 
	 * 
	 * Update the evaluation tree with the values of the ordinary variables found. 
	 * 
	 *  @return list of possible reference uncertain nodes updated only with the
	 *          nodes don't evaluated in this step. 
	 */
	private List<ContextNode> trySimpleEvaluationOfPossibleReferenceUncertainNodes(
			MFragInstance mFragInstance, List<ContextNode> possibleReferenceUncertainNodes)
			throws MFragContextFailException {
		
		//Log
		IdentationLevel level1 = new IdentationLevel(null);  
		IdentationLevel level2 = new IdentationLevel(level1); 
		IdentationLevel level3 = new IdentationLevel(level2); 
		IdentationLevel level4 = new IdentationLevel(level3); 
		IdentationLevel level5 = new IdentationLevel(level4); 
		
		
		System.out.println("TrySimpleEvaluationOfPossibleReferenceUncertainNodes: " + possibleReferenceUncertainNodes);
		ISSBNLogManager logManager = ssbn.getLogManager();
		
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 
		
		boolean iterationWithSucess = true; 
		
		List<ContextNode> auxPossibleReferenceUncertainNodes = new ArrayList<ContextNode>(); 
		
		while ( iterationWithSucess ) {

			iterationWithSucess = false;
			auxPossibleReferenceUncertainNodes.clear(); 

			for(ContextNode contextNode: possibleReferenceUncertainNodes) {
				
				boolean evaluated = false; 

				System.out.println("ContextNode: " + contextNode);

				List<OrdinaryVariable> ovInstancesFaultList = contextNode.getOVFaultForOVInstanceSet(ovInstances); 

				List<OVInstance>  temporaryOVInstanceList = new ArrayList<OVInstance>(); 
				temporaryOVInstanceList.addAll(ovInstances); 

				int countOVFilled = 0; 

				for(OrdinaryVariable ov: ovInstancesFaultList) {
					List<OVInstance> ovInstanceList = mFragInstance.getOVInstanceListForOrdinaryVariable(ov); 
					if (ovInstanceList.size() == 1) {
						temporaryOVInstanceList.add(ovInstanceList.get(0));
						countOVFilled++; 
					}
				}

				//Verify if rest only one ordinary variable fault
				if(countOVFilled == ovInstancesFaultList.size() - 1) {
					SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, temporaryOVInstanceList);

					if( (searchResult != null) && (searchResult.getValuesResultList().size() != 0) ){
						if (searchResult.getValuesResultList().get(0).length == 1) {
							//Result valid results: Add the result to the tree of result.
							try {

								mFragInstance.addOVValuesCombination(
										searchResult.getOrdinaryVariableSequence(), 
										searchResult.getValuesResultList());

								iterationWithSucess = true; 
								evaluated = true; 
								
								mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_OK); 

								if (logManager != null) {
									logManager.printText(level5, false, "Node evaluated OK");
								}

							} catch (MFragContextFailException e) {

								e.printStackTrace(); 

								mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
								mFragInstance.setUseDefaultDistribution(true); 
								evaluated = true; 
								if (logManager != null) {
									logManager.printText(level5, false,"Context Node FAIL: use the default distribution");
								}

								//Here, the context node fail adding the values for a ordinary variable
								//fault. This fail impossibilite the evaluation of the rest of the MFragInstance, 
								//because, this node, used to recover the possible values, fail. 
								//								throw e; 
							}

						}
					}
				}
				
				if (!evaluated) {
					auxPossibleReferenceUncertainNodes.add(contextNode); 
				}

			}
			
			possibleReferenceUncertainNodes.clear(); 
			possibleReferenceUncertainNodes.addAll(auxPossibleReferenceUncertainNodes);
			
		}
		
		return possibleReferenceUncertainNodes;
		
	}
	
	/**
	 * Try to solve the possible reference uncertain nodes (nodes in format 
	 * ResidentNode(ov1)=ov2, where ov1 or ov2, or both are unkown. The evaluation
	 * is considered OK only if have only one possible result for the ordinary 
	 * variables of the node. 
	 * 
	 * Update the evaluation tree with the values of the ordinary variables found. 
	 * 
	 *  @return list of possible reference uncertain nodes updated only with the
	 *          nodes don't evaluated in this step. 
	 */
	private List<ContextNode> trySearchStrategyForPossibleReferenceUncertainNodes(
			MFragInstance mFragInstance, List<ContextNode> possibleReferenceUncertainNodes)
			throws MFragContextFailException {
		
		System.out.println("trySearchStrategyForPossibleReferenceUncertainNodes: " + possibleReferenceUncertainNodes);
		//Log
		IdentationLevel level1 = new IdentationLevel(null);  
		IdentationLevel level2 = new IdentationLevel(level1); 
		IdentationLevel level3 = new IdentationLevel(level2); 
		IdentationLevel level4 = new IdentationLevel(level3); 
		IdentationLevel level5 = new IdentationLevel(level4); 

		ISSBNLogManager logManager = ssbn.getLogManager();

		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 

		List<ContextNode> auxPossibleReferenceUncertainNodes = new ArrayList<ContextNode>(); 

		for(ContextNode contextNode: possibleReferenceUncertainNodes) {

			System.out.println("ContextNode: " + contextNode);

				//---> 2) Use the Entity Tree Strategy. 
				SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, ovInstances); 

				if(searchResult!= null){  

					if (logManager != null) {
						logManager.printText(level5, false, "Search Result: ");
						for(String[] result: searchResult.getValuesResultList()){
							String resultOv = " > "; 
							for(int i = 0; i < result.length; i++){
								resultOv += result[i] + " "; 
							}
							if (logManager != null) {
								logManager.printText(level5, false, resultOv);
							}
						}
					}
					
					//Result valid results: Add the result to the tree of result.
					try {

						mFragInstance.addOVValuesCombination(
								searchResult.getOrdinaryVariableSequence(), 
								searchResult.getValuesResultList());

						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_OK); 
						
						if (logManager != null) {
							logManager.printText(level5, false, "Node evaluated OK");
						}
						
					} catch (MFragContextFailException e) {
						
						e.printStackTrace(); 
						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(level5, false,"Context Node FAIL: use the default distribution");
						}
						
						//Here, the context node fail adding the values for a ordinary variable
						//fault. This fail impossibilite the evaluation of the rest of the MFragInstance, 
						//because, this node, used to recover the possible values, fail. 
						throw e; 
					}
				}else { 
					//--------  SearchResult == Null
					auxPossibleReferenceUncertainNodes.add(contextNode); 
				}

		}

		return auxPossibleReferenceUncertainNodes;
		
	}
	
	/**
	 * For the possible reference uncertain nodes candidates that scape from the
	 * other two tests, try to use the uncertain reference strategy. 
	 * 
	 * @param mFragInstance
	 * @param possibleReferenceUncertainNodes
	 */
	private void tryUncertainReferenceStrategy(MFragInstance mFragInstance,
			List<ContextNode> possibleReferenceUncertainNodes) {
		
		//Log
		IdentationLevel level1 = new IdentationLevel(null);  
		IdentationLevel level2 = new IdentationLevel(level1); 
		IdentationLevel level3 = new IdentationLevel(level2); 
		IdentationLevel level4 = new IdentationLevel(level3); 
		IdentationLevel level5 = new IdentationLevel(level4); 
		
		System.out.println("tryUncertainReferenceStrategy " + possibleReferenceUncertainNodes );
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 
		
		for(ContextNode contextNode: possibleReferenceUncertainNodes){
			System.out.println("ContextNode: " + contextNode);

			List<OrdinaryVariable> ovInstancesFault = contextNode.getOVFaultForOVInstanceSet(ovInstances); 
			
			System.out.println("OVFault" + ovInstancesFault);

			NodeFormulaTree formulaTree = (NodeFormulaTree) contextNode.getFormulaTree(); 
			ArrayList<NodeFormulaTree> listChildren = 
					(ArrayList<NodeFormulaTree>)formulaTree.getChildren(); 

			NodeFormulaTree children1 = listChildren.get(0); 
			NodeFormulaTree children2 = listChildren.get(1); 

			Set<OrdinaryVariable> variableNodeListNode; 
			OrdinaryVariable uncertainReferenceVariable; 

			if (children1.getSubTypeNode() == EnumSubType.NODE) {
				variableNodeListNode = children1.getVariableList();
				uncertainReferenceVariable = (OrdinaryVariable)children2.getVariableList().toArray()[0]; 
			}else {
				variableNodeListNode = children2.getVariableList();
				uncertainReferenceVariable = (OrdinaryVariable)children1.getVariableList().toArray()[0]; 
			}

			//test if have only one combination possible for the variablesNode

			for (OrdinaryVariable ov: variableNodeListNode) {

				List<OVInstance> ovInstanceList = mFragInstance.getOVInstanceListForOrdinaryVariable(ov); 

				if (ovInstanceList.size() != 1) {
					if (ovInstanceList.size() == 0) {
						mFragInstance.setStateEvaluationOfContextNode(
								contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(level5, false,
									"> Context node with variable ordinary fault don't attend the restriction");
							logManager.printText(level5, false,
									"> Have more than one ordinary variable without value: " + ov );
							logManager.printText(level5, false,
									"Context Node FAIL: use the default distribution");
						}
					}else { // greater than 1 
						mFragInstance.setStateEvaluationOfContextNode(contextNode, 
								ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(level5, false,
									"> Context node with variable ordinary fault don't attend the restriction");
							logManager.printText(level5, false,
									"> Have more than one instance possible for the ordinary variable: " + ov );
							logManager.printText(level5, false,
									"Context Node FAIL: use the default distribution");
						}
					}
				}

			}
			
			List<OVInstance> ovInstanceList = mFragInstance.getOVInstanceListForOrdinaryVariable(uncertainReferenceVariable); 
			if(ovInstanceList.size() != 0) {
				mFragInstance.setStateEvaluationOfContextNode(contextNode, 
						ContextNodeEvaluationState.EVALUATION_FAIL); 
				mFragInstance.setUseDefaultDistribution(true); 
				//TODO Algorith Error
			}

			if (!mFragInstance.isUseDefaultDistribution()) {
				//test if the uncertainReferenceVariable is the OVFault
				SimpleContextNodeFatherSSBNNode simpleContextNodeFather = null;
				try{
					simpleContextNodeFather = 
							evaluateUncertaintyReferenceCase(mFragInstance, 
									contextNode, uncertainReferenceVariable);
				}
				catch(ImplementationRestrictionException e){
					mFragInstance.setUseDefaultDistribution(true); 
					if (logManager != null) {
						logManager.printText(level5, false, "Fail: " + e.getMessage());
						logManager.printText(level5, false,
								"Context Node FAIL: use the default distribution");
					}
				}
			}

		}
	}

	/**
	 * Try evaluate the uncertainty reference for the context node and the ov fault. 
	 * Return null if don't have ordinary variables possible for the evaluation. 
	 * 
	 * Notes: <br> 
	 * - In this implementation only one context node should became a parent. <br> 
	 * 
	 * 
	 * @param mFragInstance
	 * @param contextNode
	 * @param referenceUncertainOV
	 * 
	 * @throws ImplementationRestrictionException 
	 */
	public SimpleContextNodeFatherSSBNNode evaluateUncertaintyReferenceCase(MFragInstance mFragInstance, 
			ContextNode contextNode, OrdinaryVariable referenceUncertainOV) throws ImplementationRestrictionException{
		
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb); 
		
		//2 Recover alll the entites of the specifc type

		
		List<String> result = null;
		List<LiteralEntityInstance> list =  avaliator.searchEntitiesForOrdinaryVariable(referenceUncertainOV); 
		
		if((list == null)||(list.size()==0)){
			return null; 
		}else{
			result = new ArrayList<String>(); 
			for(LiteralEntityInstance lei: list){
				result.add(lei.getInstanceName()); 
			}
		}
		
		//3 Analize what entities are possible at the tree and add the result 
		//at the MFragInstance
		
		try {
			//The new ov are at the tree... but, too are at the simple context node parent. 
			
			mFragInstance.addOVValueCombination(referenceUncertainOV, result);
			mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_SEARCH); 
			
			SimpleContextNodeFatherSSBNNode contextParent = new SimpleContextNodeFatherSSBNNode(contextNode, referenceUncertainOV); 
			contextParent.setPossibleValues(result); 
			
			mFragInstance.setContextNodeForOrdinaryVariable(referenceUncertainOV, contextParent); 
			
			System.out.println("Add context Parent for referenceUncertainOV: ");
			System.out.println("OV: " + referenceUncertainOV + "; ContextNode: " + contextParent);
			
			return contextParent; 
			
		} catch (MFragContextFailException e) {
			
			//This exception don't should be throw because we assume that don't 
			//have value for the ordinary variable at the list of OVInstances 
			//of the MFrag and for this, don't exists a way to exists a inconsistency
			//at the addOVValueCombination method.
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getMessage()); 
		} 
	}
}

