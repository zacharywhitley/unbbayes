package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

public class MFragContextNodeAvaliator2  implements IMFragContextNodeAvaliator {

	private KnowledgeBase kb; 
	private SSBN ssbn; 
	
	//Max number that can be guarded in a int variable (int with 32 bits) 
	private final int MAX_INT_NUMBER = 2147483647; 
	
	public MFragContextNodeAvaliator2 (SSBN ssbn) {
		this.ssbn = ssbn; 
		this.kb = ssbn.getKnowledgeBase(); 
	}
	
	/**
	 * Evaluate the context nodes of a MFrag using the ordinary variables already
	 * Instantiated. <b>
	 * 
	 * - Ordinary variables don't instantiated yet will be instantiated. <b>
	 * - Should have more than one reference for a ordinary variable <b>
	 * - Should have reference uncertainty problem (how return this problem) <b>
	 * - Should have ordinary variables that don't have instance for it <b>
	 * 
	 * Cases: 
	 * - Trivial case
	 * - Simple Search (one entity for ov)
	 * - Compose Search (more than one entity)
	 * - Undefined Context (more than one possible result)
	 * 
	 * @param mfrag MFrag evaluated
	 * @param ovInstances Ordinary variables already instantiated. 
	 * @throws SSBNNodeGeneralException 
	 * @throws ImplementationRestrictionException 
	 * @throws OVInstanceFaultException 
	 * @throws MFragContextFailException 
	 */
	public MFragInstance evaluateMFragContextNodes(MFragInstance mFragInstance) 
	                   throws ImplementationRestrictionException, 
	                          MFragContextFailException{
		
		
		
		//Consider that the Tree with the know ordinary variables are already mounted. 
		//Consider that the only ordinary variables filled are the already know OV
		List<OVInstance> originalOvInstanceList = mFragInstance.getOVInstanceList(); 
		List<OrdinaryVariable> originalOVFilledList = new ArrayList(); 
		
		for(OVInstance ovInstance: originalOvInstanceList){
			originalOVFilledList.add(ovInstance.getOv()); 
		}
				
		ISSBNLogManager logManager = ssbn.getLogManager();
//		ISSBNLogManager logManager = EmptySSBNLogManager(); 
		
		if (logManager != null) {
			logManager.printText(IdentationLevel.LEVEL_4, false, "1) Loop for evaluate context nodes.");
		}
		
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb);
		
		List<ContextNode> possibleReferenceUncertainNodes = new 
				ArrayList<ContextNode>(); 
		
		//For each context node: 
		// 1- If don't have empty variable ordinaries, evaluate the node. 
		// 2 - If have empty variables ordinaries, 
		//     A - If the node is in the format OV(X)=Y, keep in the possibleReferenceUncertainNodes list
		//     B - Otherwise, try to evaluate using the search strategy. 
		
		
		List<ContextNode> scheduleList = new ArrayList<ContextNode>(); 
		
		//Algorithm for find a perfect order to evaluate the context nodes
		System.out.println("Build schedule list for context nodes evaluation");
		boolean[] evaluatedOV = new boolean[mFragInstance.getOvList().length]; 
		boolean[] evaluatedContextNode = new boolean[mFragInstance.getContextNodeList().size()]; 
		int[] complexityContextNode = new int[mFragInstance.getContextNodeList().size()]; 
		
		ContextNode[] contextNodeOfMFrag = new ContextNode[mFragInstance.getContextNodeList().size()]; 
		contextNodeOfMFrag = mFragInstance.getContextNodeList().toArray(contextNodeOfMFrag); 
		
		
		for(int i = 0; i < contextNodeOfMFrag.length; i++){
			
			complexityContextNode[i] = 0; 
			
			if((contextNodeOfMFrag[i].getFormulaTree().getTypeNode() == EnumType.SIMPLE_OPERATOR)&&
					(contextNodeOfMFrag[i].getFormulaTree().getNodeVariable() instanceof BuiltInRVNot)){
				complexityContextNode[i] = 1; 
			}
			
			System.out.println("ContextNode " + contextNodeOfMFrag[i] + ", Complexity = " + complexityContextNode[i]);
			
		}
		
		//OV already filled before the evaluation of the MFrag 
		for(int i = 0; i < mFragInstance.getOvList().length; i++){
			List<OVInstance> ovInstanceForOV = mFragInstance.getOVInstanceListForOrdinaryVariable(mFragInstance.getOvList()[i]); 
			
			if((ovInstanceForOV!=null) && (ovInstanceForOV.size() > 0)){
				evaluatedOV[i] = true; 
			}
			
			System.out.println("Evaluated ov " + i + ": " + evaluatedOV[i] + ". OV = " + mFragInstance.getOvList()[i]);
			
		}
		
		//Calcule the complexity of context nodes 
		
		
//		//1 - Eliminate first the nodes that don't have ordinary variables 
//		for(int i = 0; i < contextNodeOfMFrag.length ; i++){
//			
//			List<OrdinaryVariable> ovInstancesFault = contextNodeOfMFrag[i].getOVFaultForOVInstanceSet(ovInstances); 
//			if(ovInstancesFault.size() == 0){
//				evaluatedContextNode[i] = true; 
//				scheduleList.add(contextNodeOfMFrag[i]); 
//			}
//		}
		
		
		
		//*************************************************************************************************************************
		//***********                               2 - SCHEDULE NOT FILLED NODES                     *****************************
		//*************************************************************************************************************************
		
		boolean scheduleFinish = false; 
		
		//If we have only one or none context nodes, we don't need calculate the schedule
		if(mFragInstance.getContextNodeList().size() <= 0){
			scheduleList.addAll(mFragInstance.getContextNodeList()); 
			scheduleFinish = true; 
		}
		
		while (scheduleFinish == false){
			
			scheduleFinish = true; 
			
			int minOVFault = MAX_INT_NUMBER; //This is the max number to be keeped in integer variable
			int maxOV = 0;
			
			int minComplexity = MAX_INT_NUMBER;
			
			ContextNode selectedNode = null; 
			int selectedIndex = MAX_INT_NUMBER; 
			
			for(int i = 0; i < contextNodeOfMFrag.length; i ++){
				if(!evaluatedContextNode[i]){
					scheduleFinish = false; 
					
					//Calculate the quantity of ov fault 
					int ovFaultQuantity = 0; 
					for(OrdinaryVariable ov: contextNodeOfMFrag[i].getOrdinaryVariablesInArgument()){
						for (int j = 0; j < mFragInstance.getOvList().length; j++){
							if(mFragInstance.getOvList()[j].equals(ov)){
								if(!evaluatedOV[j]){
									ovFaultQuantity+= 1; 
								}
								break; 
							}
						}
					}
					
					if(ovFaultQuantity < minOVFault){
						minOVFault = ovFaultQuantity; 
						maxOV = contextNodeOfMFrag[i].getOrdinaryVariablesInArgument().size(); 
						selectedNode = contextNodeOfMFrag[i]; 
						minComplexity = complexityContextNode[i]; 
						selectedIndex = i; 
					}else{
						if(ovFaultQuantity == minOVFault){
							//Consider complexity better than number of arguments because NOT is really expensive!
							if(complexityContextNode[i] < minComplexity){
								maxOV = contextNodeOfMFrag[i].getOrdinaryVariablesInArgument().size(); 
								selectedNode = contextNodeOfMFrag[i]; 
								minComplexity = complexityContextNode[i]; 
								selectedIndex = i; 
							}else{
								if(contextNodeOfMFrag[i].getOrdinaryVariablesInArgument().size() > maxOV){
									maxOV = contextNodeOfMFrag[i].getOrdinaryVariablesInArgument().size(); 
									selectedNode = contextNodeOfMFrag[i]; 
									minComplexity = complexityContextNode[i]; 
									selectedIndex = i; 
								}
							}
						}
					}
				}
			}
			
			if(!scheduleFinish){
				
				System.out.println("Selected Node = " + selectedNode + ". OVFault = " + minOVFault + ", OV = " + maxOV);
				
				scheduleList.add(selectedNode); 
				evaluatedContextNode[selectedIndex] = true; 

				//Update the evaluation of the ordinary variables of this node 
				for(OrdinaryVariable ov: selectedNode.getOrdinaryVariablesInArgument()){
					for (int j = 0; j < mFragInstance.getOvList().length; j++){
						if(mFragInstance.getOvList()[j].equals(ov)){
							evaluatedOV[j] = true; 
							System.out.println("Evaluated ov " + j + ": " + evaluatedOV[j]);
						}
					}
				}
			}
		}
		
		System.out.println("Final Schedule List for evaluation of context nodes: ");
		for(ContextNode contextNode: scheduleList){
			System.out.println(contextNode);
		}
		
		
		
		
		//*************************************************************************************************************************
		//***********                               3 - EVALUATE NODES                                *****************************
		//*************************************************************************************************************************
		
		//This list keep the ordinary variables evaluated. The values are in the 
		//entityTree inside the MFragInstance. 
		List<OrdinaryVariable> ovFilledList = new ArrayList<OrdinaryVariable>(); 
		
		for(OVInstance ovInstance: originalOvInstanceList){
			ovFilledList.add(ovInstance.getOv()); 
		}
		
		for(ContextNode contextNode: scheduleList){
			
			if (logManager != null) {
				logManager.printText(IdentationLevel.LEVEL_4, false, "Context Node: " + contextNode);
			}
			
			//  We divide the variables ordinaries of the context nodes in three sets: 
			// 
			//            -----------------------------
			//            |    A    |    B   |    C   |
			//            -----------------------------
			//
			//    Set A = variables ordinaries not filled yet
			//        B = variables ordinaries filled after the evaluation of others context nodes 
			//        C = variables ordinaries already filled before the evaluation of context nodes 
			//
			
			//---> 1) Verify if the context node is solved only with the know arguments. 
			List<OrdinaryVariable> ovSetAB = getNotFilledOVs(originalOVFilledList, contextNode.getOrdinaryVariablesInArgument());
			List<OrdinaryVariable> ovSetA = getNotFilledOVs(ovFilledList, contextNode.getOrdinaryVariablesInArgument());
			List<OrdinaryVariable> ovSetBC = getFilledOVs(ovFilledList, contextNode.getOrdinaryVariablesInArgument()); 
			List<OrdinaryVariable> ovSetB = getNotFilledOVs(originalOVFilledList, ovSetBC); 
			
			List<String[]> possibleCombinationsForOvSetB = mFragInstance.recoverCombinationsEntitiesPossibles(
					originalOvInstanceList.toArray(new OVInstance[originalOvInstanceList.size()]),
					ovSetB.toArray(
			                new OrdinaryVariable[ovSetB.size()])); 
			
			if(ovSetAB.size() == 0){
				
				//Dont't update the evaluation tree, because every necessary information for evaluate the 
				//context node already is available. 
				
				if (logManager != null) {
					logManager.printText(IdentationLevel.LEVEL_5, false, "All ov's of the context node are setted");
				}
				
				Boolean resultBoolean = kb.evaluateContextNodeFormula(contextNode, originalOvInstanceList);
				
				boolean result = false; 
				
				if(resultBoolean!= null){
					if(resultBoolean.booleanValue()){
						result = true; 
					}
				}else{
					throw new MFragContextFailException("Error evaluating node " + contextNode); 
				}
				
				if(result){
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_OK); 
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_5, false, "Node evaluated OK");
					}
					
					continue; 
				}else{
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL);
					mFragInstance.setUseDefaultDistribution(true); 
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_5, false, "Node evaluated FALSE");
					}
					break; //don't need evaluate the others nodes... the MFrag already don't is true 
				}
				
			}else{ //ovInstancesFault.size() != 0		
				
				if (logManager != null) {
					logManager.printText(IdentationLevel.LEVEL_5, false, "Some ov's of the context node aren't filled with the original MFragInstance OV values.");
				}
				
				//---> 2) Use the Entity Tree Strategy. 
				
				//We need build the ordinary variable instance set for each option and search the OV fault for each combination
				
				//Discover what are the ov not filled yet and what the ov already filled. 
				
				//Initial set = OVInstances that originated the creation of the MFragInstance
				//This initial set already are in the evaluation tree! 
				
				//Discover what are the ov not filled yet and what the ov already filled. 
				//Build the sets of evaluation for the ov already filled. 
				
				// Ex: 
				//
				// Tree: 
				//
				//           a1
				//          /  \
				//         b1  b2
				//          |   |
				//         c1  c1
				//
				// Sets: 
				// S1 = {a1, b1, c1}
				// S2 = {a1, b2, c1}
				//
				// We have to execute the search for each Si. 
				
				if(ovSetB.size()!=0){
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_5, false, "We have values from the evaluation of the others nodes.");
					}
					
					boolean haveValidResult = false; 

					for(String[] resultLine: possibleCombinationsForOvSetB){

						if (logManager != null) {
							logManager.printText(IdentationLevel.LEVEL_5, false, "Evaluation with possible values: " + resultLine);
						}
						
						List<OVInstance> newOVInstanceList = new ArrayList<OVInstance>(); 

						//Add fixed values 
						newOVInstanceList.addAll(originalOvInstanceList); 

						//Add the values of the combination that is under evaluation
						for(int i = 0; i < resultLine.length; i++){

							OVInstance ovInstance = OVInstance.getInstance(ovSetB.get(i), 
									LiteralEntityInstance.getInstance(resultLine[i], ovSetB.get(i).getValueType())); 

							System.out.println("OVInstance = " + ovInstance);
							newOVInstanceList.add(ovInstance); 

						}

						List<OrdinaryVariable> ovFaultList = contextNode.getOVFaultForOVInstanceSet(newOVInstanceList); 
						
						if(ovFaultList.size() != 0){
							
							SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, 
									newOVInstanceList); 

							if(searchResult!= null){
								if (logManager != null) {
									logManager.printText(IdentationLevel.LEVEL_5, false, "Search Result: ");
									for(String[] result: searchResult.getValuesResultList()){
										String resultOv = " > "; 
										for(int i = 0; i < result.length; i++){
											resultOv += result[i] + " "; 
										}
										logManager.printText(IdentationLevel.LEVEL_5, false, resultOv);
									}
								}

								//Result valid results: Add the result to the tree of result.
								try {

									mFragInstance.addOVValuesCombinationForPathCombination(
											newOVInstanceList, 
											searchResult.getOrdinaryVariableSequence(), 
											searchResult.getValuesResultList()); 

									//								mFragInstance.addOVValuesCombination(
									//										searchResult.getOrdinaryVariableSequence(), 
									//										searchResult.getValuesResultList());

									haveValidResult = true; 

								} catch (MFragContextFailException e) {

									//							e.printStackTrace(); 
									if (logManager != null) {
										logManager.printText(IdentationLevel.LEVEL_5, false, e.getMessage());
									}

									mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
									mFragInstance.setUseDefaultDistribution(true); 
									if (logManager != null) {
										logManager.printText(IdentationLevel.LEVEL_5, false,"Context Node FAIL: use the default distribution");
									}

									//Here, the context node fail adding the values for a ordinary variable
									//fault. This fail impossibility the evaluation of the rest of the MFragInstance, 
									//because, this node, used to recover the possible values, fail. 
									throw e; 
								}

							}else{ //search result = null 

								//TODO 

								//Here the strategy changed. We are adding only possible values to the tree, so it never will be broken. 
								//If the searchResult return null/0 elements, the line of the tree for that evaluation have to be destroyed. 
								//If in the final tree, we destroy every paths, the evaluation of the node should be false. 
								OrdinaryVariable[] ovEvaluationArray = new OrdinaryVariable[newOVInstanceList.size()];
								String[] entityValueArray = new String[newOVInstanceList.size()]; 

								for(int i = 0; i < newOVInstanceList.size(); i++){
									ovEvaluationArray[i] = newOVInstanceList.get(i).getOv();
									entityValueArray[i] = newOVInstanceList.get(i).getEntity().getInstanceName(); 
								}

								// This line of evaluation don't is more possible because we don't have evaluation for the new nodes 
								mFragInstance.removePossibleOVValuesEvaluation(ovEvaluationArray, entityValueArray);
								if (logManager != null) {
									logManager.printText(IdentationLevel.LEVEL_5, false,"Evaluation Path fail!");
								}
								continue; 

							}
						}else{ //No OV Fault 

							List<OVInstance> listOVInstanceOfContextNode = new ArrayList<OVInstance>(); 
							
							for(OrdinaryVariable ov: contextNode.getOrdinaryVariablesInArgument()){
								for(OVInstance ovInstance: newOVInstanceList){
									if(ovInstance.getOv().equals(ov)){
										listOVInstanceOfContextNode.add(ovInstance); 
										break; 
									}
								}
							}
							
							Boolean result = kb.evaluateContextNodeFormula(contextNode, 
									                                       listOVInstanceOfContextNode); 
							
							//If the evaluation if false, we have to eliminate this evaluation of ordinary variables  
							if(result){
								haveValidResult = true; 
							}else{
								OrdinaryVariable[] ovEvaluationArray = new OrdinaryVariable[newOVInstanceList.size()];
								String[] entityValueArray = new String[newOVInstanceList.size()]; 

								System.out.println("Evaluation return false: remove the path of the tree. ");
								
								for(int i = 0; i < newOVInstanceList.size(); i++){
									ovEvaluationArray[i] = newOVInstanceList.get(i).getOv();
									entityValueArray[i] = newOVInstanceList.get(i).getEntity().getInstanceName(); 
								}

								for(int i = 0; i < ovEvaluationArray.length; i++){
									System.out.println("Deleted ov = " + ovEvaluationArray[i] + " Instance = " + entityValueArray[i] );
								}
								// This line of evaluation don't is more possible because we don't have evaluation for the new nodes 
								// If the tree don't have more valid ways, a exception MFragContextFailException will be throw, 
								// indicating that is necessary use the default distribution for this node.
								mFragInstance.removePossibleOVValuesEvaluation(ovEvaluationArray, entityValueArray);
								if (logManager != null) {
									logManager.printText(IdentationLevel.LEVEL_5, false,"Evaluation Path fail!");
								}
								continue; 
							}
							
						}
					}

					//Same line are correctly evaluated! 
					if(haveValidResult){
						mFragInstance.setStateEvaluationOfContextNode(contextNode, 
								ContextNodeEvaluationState.EVALUATION_OK); 

						ovFilledList.addAll(ovSetA); 
						
						if (logManager != null) {
							logManager.printText(IdentationLevel.LEVEL_5, false, "Node evaluated OK");
						}

					}else{
						if(avaliator.testContextNodeFormatRestriction(contextNode)){
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, 
										"The uncertain reference is possible. ");
							}
							possibleReferenceUncertainNodes.add(contextNode); 
							//This nodes will be evaluated out of the loop
							
							continue; 
							
						}else{
							mFragInstance.setStateEvaluationOfContextNode(contextNode, 
									ContextNodeEvaluationState.EVALUATION_FAIL); 
							
							mFragInstance.setUseDefaultDistribution(true); 
							
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, "Fail: Invalid Context node Formula");
								logManager.printText(IdentationLevel.LEVEL_4, false,"Still ov fault... nothing more to do. " +
										"Use default distribution");
							}
							
							break; 
						
						}
					}
				} else{
					//---> 2) Use the Entity Tree Strategy. 
					SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, originalOvInstanceList); 

					if(searchResult!= null){  

						if (logManager != null) {
							logManager.printText(IdentationLevel.LEVEL_5, false, "Search Result: ");
							for(String[] result: searchResult.getValuesResultList()){
								String resultOv = " > "; 
								for(int i = 0; i < result.length; i++){
									resultOv += result[i] + " "; 
								}
								logManager.printText(IdentationLevel.LEVEL_5, false, resultOv);
							}
						}

						//Result valid results: Add the result to the tree of result.
						try {

							mFragInstance.addOVValuesCombination(
									searchResult.getOrdinaryVariableSequence(), 
									searchResult.getValuesResultList());

							ovFilledList.addAll(ovSetA); 
							
							mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_OK); 

							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, "Node evaluated OK");
							}

						} catch (MFragContextFailException e) {

							//							e.printStackTrace(); 
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, e.getMessage());
							}

							mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
							mFragInstance.setUseDefaultDistribution(true); 
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false,"Context Node FAIL: use the default distribution");
							}

							//Here, the context node fail adding the values for a ordinary variable
							//fault. This fail impossibility the evaluation of the rest of the MFragInstance, 
							//because, this node, used to recover the possible values, fail. 
							throw e; 
						}
					}else{
						//No results!!! We can only return the default distribution, except if the node is in the format accepted by the uncertainty reference strategy
						
						if(avaliator.testContextNodeFormatRestriction(contextNode)){
							
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, 
										"The uncertain reference is possible. ");
							}
							possibleReferenceUncertainNodes.add(contextNode); 
							
						}else{
							//Here we can try the uncertainty reference strategy!!! 

							mFragInstance.setStateEvaluationOfContextNode(contextNode, 
									ContextNodeEvaluationState.EVALUATION_FAIL); 
							mFragInstance.setUseDefaultDistribution(true); 
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, "Fail: Invalid Context node Formula");
								logManager.printText(IdentationLevel.LEVEL_4, false,"Still ov fault... nothing more to do. " +
										"Use default distribution");
							}
							break; //don't is necessary evaluate the others nodes 
						}
					}
				}
			}
		}
		
		
		
		//*************************************************************************************************************************
		//***********                    4 - EVALUATE UNCERTAINTY REFERENCE NODES                     *****************************
		//*************************************************************************************************************************
		
		System.out.println("Evaluate context nodes with the possibility use of reference uncertain nodes");
		
		if((possibleReferenceUncertainNodes.size() > 0)&&(!mFragInstance.isUsingDefaultDistribution())) {
			tryUncertainReferenceStrategy(mFragInstance, possibleReferenceUncertainNodes);
		}else{
			System.out.println("No nodes fill the requiriments.");
		}

		
		
		
		
		
		//*************************************************************************************************************************
		//***********                                 5 - EVALUATE OF ISA NODES                       *****************************
		//*************************************************************************************************************************
		
		//------------------------------------------------------------------------------
		//Return a list of ordinary variables of the MFrag that don't are evaluated yet. 
		//------------------------------------------------------------------------------
		
		if (logManager != null) {
			logManager.printText(IdentationLevel.LEVEL_4, false,"2) Loop for evaluate the IsA nodes"); 
		}
		
		List<OrdinaryVariable> ovDontFoundYetList = new ArrayList<OrdinaryVariable>(); 
		
		for(OrdinaryVariable ov: mFragInstance.getMFragOrigin().getOrdinaryVariableList()){
			if(mFragInstance.getOVInstanceListForOrdinaryVariable(ov).size() == 0){
				ovDontFoundYetList.add(ov);
			}
		}
		
		if(ovDontFoundYetList.isEmpty() || mFragInstance.isUsingDefaultDistribution()){
			System.out.println("No isA context node need to be evaluated.");
		}else{

			//Evaluate this ordinary variables
			for(OrdinaryVariable ov: ovDontFoundYetList){
				//No context node about this ov... the value is unknown, we should 
				//consider all possible values (Closed Word Assumption)
				if (logManager != null) {
					logManager.printText(IdentationLevel.LEVEL_4, false,"Evaluate IsA for OV " + ov.getName());
				}

				List<String> possibleValues = kb.getEntityByType(ov.getValueType().getName()); 

				if (logManager != null) {
					for(String possibleValue: possibleValues){
						logManager.printText(IdentationLevel.LEVEL_4, false, "  > " + possibleValue);
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
					Debug.println(this.getClass(), "", e);
					mFragInstance.setUseDefaultDistribution(true); 
				}
			}
		}
	
		
		// Display results 
		if(mFragInstance.isUsingDefaultDistribution()){
			System.out.println("MFrag Instance will use the default distribution!");
		}else{
			List<String[]> possibleCombinationsList = mFragInstance.recoverAllCombinationsEntitiesPossibles(); 
			if (logManager != null) {
				logManager.printBox3Bar(IdentationLevel.LEVEL_4);
				logManager.printBox3(IdentationLevel.LEVEL_4, 0, "Result of evalation of the context nodes (Possible combinations): ");
				for(String[] possibleCombination : possibleCombinationsList){
					String ordinaryVariableComb = " > ";
					for(String ordinaryVariableValue: possibleCombination){
						ordinaryVariableComb += " " + ordinaryVariableValue;
					}
					ordinaryVariableComb += " < ";
					logManager.printBox3(IdentationLevel.LEVEL_4, 1, ordinaryVariableComb);
				}
				logManager.printBox3Bar(IdentationLevel.LEVEL_4);
			}
		}
		
		//Return mFragInstance with the ordinary variables filled. 
		return mFragInstance; 
	}

	private List<OrdinaryVariable> getNotFilledOVs(List<OrdinaryVariable> ovFilledList, 
			                                       List<OrdinaryVariable> ovList){
		
		List<OrdinaryVariable> list = new ArrayList<OrdinaryVariable>(); 
		
		for(OrdinaryVariable ov: ovList){
			if(!ovFilledList.contains(ov)){
				list.add(ov); 
			}
		}
		
		return list; 
	}
	
	private List<OrdinaryVariable> getFilledOVs(List<OrdinaryVariable> ovFilledList, 
			List<OrdinaryVariable> ovList){

		List<OrdinaryVariable> list = new ArrayList<OrdinaryVariable>(); 

		for(OrdinaryVariable ov: ovList){
			if(ovFilledList.contains(ov)){
				list.add(ov); 
			}
		}

		return list; 
	}	
	

	/**
	 * Try to solve the possible reference uncertain nodes (nodes in format 
	 * ResidentNode(ov1)=ov2, where ov1 or ov2, or both are unknown). 
	 * 
	 * The evaluation is considered OK only if have only one possible result for the ordinary 
	 * variables of the node. A iteration is made in the context nodes list searching 
	 * for nodes with only one ordinary variable don't filled. With the success in the 
	 * evaluation of one context node, we try use the information for solve others 
	 * context nodes. 
	 * 
	 * Update the evaluation tree (inside MFragInstance) with the values of the ordinary variables found. 
	 * 
	 *  @return list of possible reference uncertain nodes updated only with the
	 *          nodes don't evaluated in this step. 
	 */
	private List<ContextNode> trySimpleEvaluationOfPossibleReferenceUncertainNodes(
			MFragInstance mFragInstance, List<ContextNode> possibleReferenceUncertainNodes)
			throws MFragContextFailException {
		
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
				if (ovInstancesFaultList == null || ovInstancesFaultList.isEmpty()) {
					// we don't need to solve this node, because there is no unknown OV
					// TODO need to check why this block is executed when there are two context nodes filtering the same OV. For instance, not(t = tPrev) and tPrev = Previous(t)
					evaluated = true;
				}

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
									logManager.printText(IdentationLevel.LEVEL_5, false, "Node evaluated OK");
								}

							} catch (MFragContextFailException e) {

								e.printStackTrace(); 

								mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
								mFragInstance.setUseDefaultDistribution(true); 
								evaluated = true; 
								if (logManager != null) {
									logManager.printText(IdentationLevel.LEVEL_5, false,"Context Node FAIL: use the default distribution");
								}

								//Here, the context node fail adding the values for a ordinary variable
								//fault. This fail impossibility the evaluation of the rest of the MFragInstance, 
								//because, this node, used to recover the possible values, fail. 
								//throw e; 
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
	 * ResidentNode(ov1)=ov2, where ov1 or ov2, or both are unknown). The evaluation
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

		ISSBNLogManager logManager = ssbn.getLogManager();

		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 

		List<ContextNode> auxPossibleReferenceUncertainNodes = new ArrayList<ContextNode>(); 

		for(ContextNode contextNode: possibleReferenceUncertainNodes) {

			System.out.println("ContextNode: " + contextNode);
			
				//---> 2) Use the Entity Tree Strategy. 
				SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, ovInstances); 
				
				if(searchResult!= null){  
					
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_5, false, "Search Result: ");
						for(String[] result: searchResult.getValuesResultList()){
							String resultOv = " > "; 
							for(int i = 0; i < result.length; i++){
								resultOv += result[i] + " "; 
							}
							if (logManager != null) {
								logManager.printText(IdentationLevel.LEVEL_5, false, resultOv);
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
							logManager.printText(IdentationLevel.LEVEL_5, false, "Node evaluated OK");
						}
						
					} catch (MFragContextFailException e) {
						
						e.printStackTrace(); 
						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(IdentationLevel.LEVEL_5, false,"Context Node FAIL: use the default distribution");
						}
						
						//Here, the context node fail adding the values for a ordinary variable
						//fault. This fail make impossible evaluate the rest of MFrag 
						//because this node, used to recover the possible values, fail. 
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
	 * For the possible reference uncertain nodes candidates that dont't pass in
	 * other two tests, try to use the uncertain reference strategy. 
	 * 
	 * @param mFragInstance
	 * @param possibleReferenceUncertainNodes
	 */
	private void tryUncertainReferenceStrategy(MFragInstance mFragInstance,
			List<ContextNode> possibleReferenceUncertainNodes) {
		
		System.out.println("tryUncertainReferenceStrategy " + possibleReferenceUncertainNodes );
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList();     //We have to use the tree here!!!  
		                                                                      //and this is a mistake of the previous algorithm also! 
		
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
							logManager.printText(IdentationLevel.LEVEL_5, false,
									"> Context node with variable ordinary fault don't attend the restriction.");
							logManager.printText(IdentationLevel.LEVEL_5, false,
									"> Argument ordinary variable without value: " + ov + ".");
							logManager.printText(IdentationLevel.LEVEL_5, false,
									"Context Node FAIL: use the default distribution");
						}
					}else { // greater than 1 
						mFragInstance.setStateEvaluationOfContextNode(contextNode, 
								ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(IdentationLevel.LEVEL_5, false,
									"> Context node with variable ordinary fault don't attend the restriction.");
							logManager.printText(IdentationLevel.LEVEL_5, false,
									"> Have more than one instance possible for the ordinary variable: " + ov );
							logManager.printText(IdentationLevel.LEVEL_5, false,
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

			if (!mFragInstance.isUsingDefaultDistribution()) {
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
						logManager.printText(IdentationLevel.LEVEL_5, false, "Fail: " + e.getMessage());
						logManager.printText(IdentationLevel.LEVEL_5, false,
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
	private SimpleContextNodeFatherSSBNNode evaluateUncertaintyReferenceCase(MFragInstance mFragInstance, 
			ContextNode contextNode, OrdinaryVariable referenceUncertainOV) throws ImplementationRestrictionException{
		
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb); 
		
		//2 Recover entities of a specific type
		
		List<String> result = null;
		List<ILiteralEntityInstance> list =  avaliator.searchEntitiesForOrdinaryVariable(referenceUncertainOV); 
		
		if((list == null)||(list.size()==0)){
			return null; 
		}else{
			result = new ArrayList<String>(); 
			for(ILiteralEntityInstance lei: list){
				result.add(lei.getInstanceName()); 
			}
		}
		
		//3 Analyze what entities are possible at the tree and add the result to the MFragInstance
		
		try {
			//The new ov is at the tree, but it it too at the simple context node parent. 
			
			mFragInstance.addOVValueCombination(referenceUncertainOV, result);
			mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_SEARCH); 
			
			SimpleContextNodeFatherSSBNNode contextParent = new SimpleContextNodeFatherSSBNNode(contextNode, referenceUncertainOV); 
			contextParent.setPossibleValues(result); 
			
			mFragInstance.setContextNodeForOrdinaryVariable(referenceUncertainOV, contextParent); 
			
			System.out.println("Add context Parent for referenceUncertainOV: ");
			System.out.println("OV: " + referenceUncertainOV + "; ContextNode: " + contextParent);
			
			return contextParent; 
			
		} catch (MFragContextFailException e) {
			
			//This exception shouldn't be thrown because we assume that don't 
			//have value for the ordinary variable at the list of OVInstances 
			//of the MFrag and for this, don't exists a way to exists a inconsistency
			//at the addOVValueCombination method.
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getMessage()); 
		} 
	}
}

