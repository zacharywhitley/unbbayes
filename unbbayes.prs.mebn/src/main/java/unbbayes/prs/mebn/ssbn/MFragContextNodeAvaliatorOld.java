package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

public class MFragContextNodeAvaliatorOld implements IMFragContextNodeAvaliator {

	private KnowledgeBase kb; 
	private SSBN ssbn; 
	
	public MFragContextNodeAvaliatorOld (SSBN ssbn) {
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
		
		//Consider that the tree with the know ordinary variables are already mounted. 
		//Consider that the only ordinary variables filled are the already know OV
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 
		
		System.out.println("OVInstanceList = " + mFragInstance.getOVInstanceList());
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level4, false, "1) Loop for evaluate context nodes.");
		}
		
		for(ContextNode contextNode: mFragInstance.getContextNodeList()){
			
			if (logManager != null) {
				logManager.printText(level4, false, "Context Node: " + contextNode);
			}
			
			//---> 1) Verify if the context node is soluted only with the know arguments. 
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
			}else{
			
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

						mFragInstance.setStateEvaluationOfContextNode(contextNode, 
								ContextNodeEvaluationState.EVALUATION_OK); 
						
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

//					ssbn.getLogManager().printText(level5, false, "Try 2: Use the iteration strategy");
//					ssbn.getLogManager().printText(level5, false, "...still not implemented.\n");
					
					//---> 3) Use the Interation with user Strategy. 
					//TODO To be developed yet... 
					
					//Note: if the user add new variables, this should alter the result
					//of previous avaliations... maybe all the algorithm should be
					//evaluated again. A solution is only permit that the user
					//add a entity already at the knowledge base. The entity added 
					//should be put again the evaluation tree for verify possible
					//inconsistency. 
					
//					notInstanciatedOVList = mFragInstance.getListNotInstanciatedOV(); 
//					Debug.println("\nOVInstances don't found = " + notInstanciatedOVList.size());
//					for(OrdinaryVariable ov: notInstanciatedOVList){
//						Debug.println(ov.getName());
//					}
//					if (notInstanciatedOVList.size() != 0){
//						Debug.println("Try 2: Use the iteration aproach");
//						for(OrdinaryVariable ov: notInstanciatedOVList){
//							if(interationHelper!=null){
//								OVInstance ovInstance = interationHelper.getInstanceValueForOVFault(ov);
//								if(ovInstance != null){
//									mFragInstance.addInstanciatedOV(ovInstance.getOv(),	ovInstance.getEntity()); 
//								}
//							}
//						}
//					}

					//---> 4) Use the uncertainty Strategy. 
					if (logManager != null) {
						logManager.printText(level5, false, 
							"Try 2: Use the uncertain reference strategy");
					}
					
					//Utilized only in the specific case z = RandomVariable(x), 
					//where z is the unknow variable. (Should have only one unknow variable)
					
					SimpleContextNodeFatherSSBNNode simpleContextNodeFather = null; 
					
					if(ovInstancesFault.size() == 1){
						try{
							simpleContextNodeFather = 
								evaluateUncertaintyReferenceCase(mFragInstance, 
										contextNode, ovInstancesFault.get(0)); 
							if(simpleContextNodeFather != null){
								continue; //OK!!! Good!!! Yes!!! 
							}
						}
						catch(ImplementationRestrictionException e){
							mFragInstance.setUseDefaultDistribution(true); 
							if (logManager != null) {
								logManager.printText(level5, false, "Fail: " + e.getMessage());
							}
						}
					}
					
					//--> 5) Nothing more to try... context fail
					if (logManager != null) {
						logManager.printText(level4, false,"Still ov fault... nothing more to do. " +
							"Use default distribution");
					}
					
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL); 
					mFragInstance.setUseDefaultDistribution(true);
					if (logManager != null) {
						logManager.printText(level5, false, "Fail: Use default distribution");
					}
					//TODO Maybe a warning... Not so drastic! 
					
				}
			}
			
		}
		
		
		if (logManager != null) {
			logManager.printText(level4, false,"2) Loop for evaluate the IsA nodes"); 
		}
		
		//Return a list of ordinary variables of the MFrag that don't are evaluated yet. 
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
	 * Try evaluate the uncertainty reference for the context node and the ov fault. 
	 * Return null if don't have ordinary variables possible for the evaluation. 
	 * 
	 * Notes: <br> 
	 * - In this implementation only one context node should became a parent. <br> 
	 * 
	 * 
	 * @param mFragInstance
	 * @param contextNode
	 * @param ovFault
	 * 
	 * @throws ImplementationRestrictionException 
	 */
	public SimpleContextNodeFatherSSBNNode evaluateUncertaintyReferenceCase(MFragInstance mFragInstance, 
			ContextNode contextNode, OrdinaryVariable ovFault) throws ImplementationRestrictionException{
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb); 
		
		//1 Evaluate if the context node attend to restrictions and fill the ovinstancelist 
		if(!avaliator.testContextNodeFormatRestriction(contextNode)){
			throw new ImplementationRestrictionException(
					ImplementationRestrictionException.INVALID_CTXT_NODE_FORMULA); 
		}; 
		
		Collection<OrdinaryVariable> contextOrdinaryVariableList = contextNode.getVariableList(); 
		
		for(OrdinaryVariable ov: contextOrdinaryVariableList){
			if(!ov.equals(ovFault)){
				List<OVInstance> ovInstanceForOvList = mFragInstance.getOVInstanceListForOrdinaryVariable(ov); 
			    if(ovInstanceForOvList.size() > 1){
			    	throw new ImplementationRestrictionException(
							ImplementationRestrictionException.ONLY_ONE_OVINSTANCE_FOR_OV); 
			    }else{
			    	ovInstanceList.add(ovInstanceForOvList.get(0)); 
			    }
			}
		}
		
		//2 Recover alll the entites of the specifc type

		
		List<String> result = null;
		List<ILiteralEntityInstance> list =  avaliator.searchEntitiesForOrdinaryVariable(ovFault); 
		
		if((list == null)||(list.size()==0)){
			return null; 
		}else{
			result = new ArrayList<String>(); 
			for(ILiteralEntityInstance lei: list){
				result.add(lei.getInstanceName()); 
			}
		}
		
		//3 Analize what entities are possible at the tree and add the result 
		//at the MFragInstance
		
		try {
			//The new ov are at the tree... but, too are at the simple context node parent. 
			
			mFragInstance.addOVValueCombination(ovFault, result);
			mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_SEARCH); 
			
			SimpleContextNodeFatherSSBNNode contextParent = new SimpleContextNodeFatherSSBNNode(contextNode, ovFault); 
			contextParent.setPossibleValues(result); 
			
			mFragInstance.setContextNodeForOrdinaryVariable(ovFault, contextParent); 
			
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
