/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Class that contains methods for evaluate the context nodes of a MFrag. 
 * 
 * @author Laecio Santos (laecio@gmail.com)
 */
public class ContextNodeEvaluator {

	private KnowledgeBase kb; 
	private SSBNAlgorithmInterationHelper interationHelper; 
	
	private Map<String, List<LiteralEntityInstance>> valuesEntityMap; 
	
	public ContextNodeEvaluator(KnowledgeBase kb){
		
		this.kb = kb; 
		
		//Inicialization of the lists
		this.valuesEntityMap = new TreeMap<String, List<LiteralEntityInstance>>(); 
		
	}
	
	/**
	 * Evaluate a context node. Should have a OVInstance for each ordinary 
	 * variable present in the context node or a exception will be throw. 
	 * 
	 * @param contextNode 
	 * @param ovInstances
	 * @return the result of the evaluation of context node (true or false)
	 * @throws OVInstanceFaultException One or more ordinary variable of the context 
	 *                                  node don't have one associated OVInstance
	 */
	public boolean evaluateContextNode(ContextNode contextNode, List<OVInstance> ovInstances) 
	         throws OVInstanceFaultException{

		List<OrdinaryVariable> ovFaultList = contextNode.getOVFaultForOVInstanceSet(ovInstances); 

		if(!ovFaultList.isEmpty()){
			throw new OVInstanceFaultException(ovFaultList); 
		}else{
            boolean result = kb.evaluateContextNodeFormula(contextNode, ovInstances);
			return result; 
		}
		
	}
	
	/**
	 * Evaluate a search context node. A search context node is a node that return
	 * instances of the Knowledge Base that satisfies a restriction. 
	 * 
	 * Ex.: z = StarshipZone(st). 
	 * -> return all the z's. 
	 * 
	 * @param context
	 * @param ovInstances
	 * @return
	 * @throws InvalidContextNodeFormulaException
	 * @throws OVInstanceFaultException 
	 */
	public List<String> evalutateSearchContextNode(ContextNode context, List<OVInstance> ovInstances) 
	        throws InvalidContextNodeFormulaException, OVInstanceFaultException{
		
			List<String> entitiesResult = kb.evaluateSingleSearchContextNodeFormula(context, ovInstances); 
			return entitiesResult;
	}
	
	
	public boolean isContextNodeSearchValidFormat(ContextNode context) throws OVInstanceFaultException{
		return true; 
	}
	
	/**
	 * Check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 * 
	 * @return true evaluation OK
	 *         false evaluation FALSE. 
	 */
	public boolean evaluateRelatedContextNodes (ResidentNode residentNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance) throws OVInstanceFaultException{

		return evaluateRelatedContextNodes(residentNode, ovInstances, mFragInstance, 
				residentNode.getOrdinaryVariableList()); 
	
	}
	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	public boolean evaluateRelatedContextNodes (InputNode inputNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance) throws OVInstanceFaultException{

		return evaluateRelatedContextNodes(inputNode, ovInstances, mFragInstance, 
				inputNode.getOrdinaryVariableList()); 
		
	}
	
	//Note: don't observes the transitivity in context nodes. 
	private boolean evaluateRelatedContextNodes (MultiEntityNode multiEntityNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance, List<OrdinaryVariable> ovList) throws OVInstanceFaultException{

		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (multiEntityNode.getMFrag().isUsingDefaultCPT()) {
			return false;
		};

		Collection<ContextNode> contextNodeList = multiEntityNode.getMFrag().getContextByOVCombination(
				ovList);

		for(ContextNode context: contextNodeList){
//			logManager.append("Evaluating Context Node: " + context.getLabel());
			if(!evaluateContextNode(context, ovInstances)){
//				logManager.appendln("  > Result = FALSE. Use default distribution ");
				return false;  
			}else{
//				logManager.appendln("  > Result = TRUE.");
			}		
		}

		return true; 
	}
	
	/**
	 * Note: This class was created for mantain the normal evaluation of the 
	 * GIA's SSBN Generator Algorithm implementation. 
	 * 
	 * @see evaluateSearchContextNodes
	 */
	public List<OVInstance> evaluateSearchContextNodesRestrict(
			MFrag mFrag, 
			List<OrdinaryVariable> ovFaultList, 
			List<OVInstance> ovInstanceList)	
			throws ImplementationRestrictionException, SSBNNodeGeneralException{
		
		List<OVInstance> listOVInstance = evaluateSearchContextNodes(
				mFrag, ovFaultList, ovInstanceList); 
		
		//Verify if all the OV fault are filled. 
		for(OrdinaryVariable ov: ovFaultList){
			boolean find = false; 
			for(OVInstance ovInstance: listOVInstance){
				if(ovInstance.getOv().equals(ov)){
					find = true; 
					break; 
				}
			}
			if(!find){
				return null; 
			}
		}
		
		//All the OV Fault are filled 
		return listOVInstance; 
		
	}
	
	
	/**
	 * Evaluate one context node with unknown ordinary variables. Only will have
	 * return if the result is one (and only one) entity for each ordinary
	 * variable. 
	 * 
	 * Restrictions: 
	 * 1. Each context node used in search should have only one OV fault. 
	 * 
	 * TODO: Analyze: 
	 * - When have more than one OV instance for one OV in the ovInstanceList
	 * 
	 * @param mFrag        MFrag of context node
	 * @param ovFaultList  List of Fault Ordinary Variables 
	 * @param ovInstanceList  List of OVINstances (know ordinary variables)
	 * 
	 * @return: Only will have return if the result is one (and only one) entity for each
	 *          ordinary variable. Otherside return null 
	 * 
	 * @throws ImplementationRestrictionException
	 * @throws SSBNNodeGeneralException
	 */
	public List<OVInstance> evaluateSearchContextNodes(
			MFrag mFrag, 
			List<OrdinaryVariable> ovFaultList, 
			List<OVInstance> ovInstanceList) 
	throws ImplementationRestrictionException, SSBNNodeGeneralException {
				
		//List of Ordinary Variable instances for each ordinary variable. 
		Map<OrdinaryVariable, List<OVInstance>> mapOVInstanceMap;
		
		mapOVInstanceMap = new HashMap<OrdinaryVariable, List<OVInstance>>(); 
		
		for(OVInstance ovInstance: ovInstanceList){
			List<OVInstance> list = new ArrayList<OVInstance>(); 
			list.add(ovInstance); 
			mapOVInstanceMap.put(ovInstance.getOv(), list); 
		}
		
		Collection<ContextNode> cnList = mFrag.getContextNodeByOrdinaryVariableRelated(ovFaultList); 
		
		boolean changed = false; 
		
		// The idea is in the iteration "i" try to find information about some of 
		// the unknown ordinary variables. This information is used in the next
		// iteration for try to find information about the others ordinary 
		// variables. This is made until a iteration where nothing information
		// was found. 
		
		do{
//			logManager.appendlnIfTrue(debug, "Interaction " + i++);
			
			Collection<ContextNode> solvedNodes = new ArrayList<ContextNode>(); 
			changed = false; 
			
OUT_LOOP:  for(ContextNode context: cnList){
	
//	             System.out.println("Nó de contexto avaliado: " + context);
				 List<OrdinaryVariable> ovFaultTempList = new ArrayList<OrdinaryVariable>(); 
				 List<OVInstance> ovInstanceForCnList = new ArrayList<OVInstance>(); 
				 ovInstanceForCnList.addAll(ovInstanceList); 
				 
				 boolean doIntersection = false; 
				 
				 //Step 1: evaluate if the node should be solved
    		    for(OrdinaryVariable ov: context.getVariableList()){
					
//    		    	System.out.println("Ordinary variable: " + ov);
			 		List<OVInstance> instanceListForOV = mapOVInstanceMap.get(ov); 
		 			
		 			if(instanceListForOV!=null){
						if(instanceListForOV.size() == 1){
//							System.out.println("   OVInstance: " + instanceListForOV.get(0));
							if(!ovInstanceForCnList.contains(instanceListForOV.get(0))){
								ovInstanceForCnList.add(instanceListForOV.get(0)); 
							}
						}else{
//							System.out.println("  Do intersection");
							doIntersection = true; 
							ovFaultTempList.add(ov);
						}
					}else{
//						System.out.println(" OV Fault!!! ");
						ovFaultTempList.add(ov);
					}
				}
    		    
				//The algorithm consider only trivial cases
			    if(ovFaultTempList.size() > 1){
//			    	System.out.println("OV Fault List maior que 1");
			    	continue OUT_LOOP; 
			    }else{

			    	try {
//			    		System.out.println("Tentando avaliar nó de contexto: ");
			    		List<String> result = evalutateSearchContextNode(context, ovInstanceForCnList);

//			    		System.out.println("Result: " + result);

			    		if((result != null) && (result.size() > 0)){
//			    			System.out.println("In");
			    			List<OVInstance> ovInstanceListResult = new ArrayList<OVInstance>(); 

			    			//Using the restriction....
			    			List<OrdinaryVariable> contextNodeOvFaultList = context.getOVFaultForOVInstanceSet(ovInstanceForCnList); 
			    			OrdinaryVariable ovFault = contextNodeOvFaultList.get(0); 

			    			for(String ovInstanceName: result){
			    				ovInstanceListResult.add(OVInstance.getInstance(ovFault, 
			    						LiteralEntityInstance.getInstance(ovInstanceName, ovFault.getValueType()))); 
			    			}

			    			if(doIntersection){
			    				//Do intersection
			    				OrdinaryVariable ov = ovFaultTempList.get(0);
			    				List<OVInstance> instanceListForOV = mapOVInstanceMap.get(ov); 
			    				ovInstanceListResult = intersection(instanceListForOV, ovInstanceListResult); 
			    			}

			    			mapOVInstanceMap.put(ovFault, ovInstanceListResult); 
//			    			System.out.println("Out ["  + ovFault + " " + 
//			    			ovInstanceListResult + "]");
			    		}

			    		solvedNodes.add(context); 
//			    		System.out.println("Houve alteracao");
			    		changed = true; 

//			    		System.out.println("\nMapOVInstance");
			    		for(OrdinaryVariable ov: mapOVInstanceMap.keySet()){
//			    			System.out.println("> OV=" + ov + "[");
			    			List<OVInstance> ovInstanceListResult = mapOVInstanceMap.get(ov); 
			    			for(OVInstance ovI: ovInstanceListResult){
//			    				System.out.println("   " + ovI);
			    			}
//			    			System.out.println("]");

			    		}
//			    		System.out.println("\n");

//			    		if(verifyOVFaultList(ovFaultList, ovInsntanceList)); 

			    	} catch (InvalidContextNodeFormulaException e) {
			    		e.printStackTrace();
			    		break OUT_LOOP; 
			    	} catch (OVInstanceFaultException e) {
			    		e.printStackTrace();
			    		break OUT_LOOP; 
			    	} 
			    } //else
				
		   	} //for 
			
			for(ContextNode context: solvedNodes){
				cnList.remove(context);
			}
			
		}while(changed); 
		
		/* 
		 * For this version, should have only one entity how result for each 
		 * empty ordinary variable.  
		 */
		List<OVInstance> listResult = new ArrayList<OVInstance>(); 
		for(OrdinaryVariable ov: ovFaultList){
			List<OVInstance> listOVInstance = mapOVInstanceMap.get(ov); 
			if(listOVInstance!=null){
				if(listOVInstance.size() == 1){
					listResult.add( listOVInstance.get(0) );
				}else{
					throw new ImplementationRestrictionException(
							ImplementationRestrictionException.ONLY_ONE_OVINSTANCE_FOR_OV); 
				}
			}
		}
		
		return listResult; 
	}

	/**
	 * Return all the entities that are possible values for the Type 
	 * @param ov
	 * @return
	 */
	public List<LiteralEntityInstance> searchEntitiesForOrdinaryVariable(OrdinaryVariable ov){
		
		List<LiteralEntityInstance> entityList = null; 
		
		if((entityList = valuesEntityMap.get(ov.getValueType().getName())) == null){
			
			List<String> entityStringList = kb.getEntityByType(ov.getValueType().getName());
			entityList = new ArrayList<LiteralEntityInstance>(); 
			for(String entity: entityStringList){
				entityList.add(LiteralEntityInstance.getInstance(entity, ov.getValueType())); 
			}
			valuesEntityMap.put(ov.getValueType().getName(), entityList); 
		}
		
		return entityList; 
	}
	
	/**
	 * Test if a context node are in the correct format
	 * ov = RandonVariable(arg1, arg2,...) or RandonVariable(arg1, arg2,...) = ov. 
	 */
	public boolean testContextNodeFormatRestriction(ContextNode contextNode){

		boolean test; 

		NodeFormulaTree formulaTree = (NodeFormulaTree) contextNode.getFormulaTree();
		
		if(formulaTree.getTypeNode() != EnumType.SIMPLE_OPERATOR){
			//error: don't is a simple operator
			test = false; 
		}else{
			if(formulaTree.getSubTypeNode() != EnumSubType.EQUALTO){
				//error: don't is a equalto operator
				test = false; 
			}else{
				ArrayList<NodeFormulaTree> listChildren = 
					(ArrayList<NodeFormulaTree>)formulaTree.getChildren(); 
				
				if(listChildren.size() != 2){
                     //error: don't have two operators
					test = false; 
				}else{
					NodeFormulaTree children1 = listChildren.get(0); 
					NodeFormulaTree children2 = listChildren.get(1); 
					
					if(children1.getTypeNode()!= EnumType.OPERAND || 
						children2.getTypeNode() != EnumType.OPERAND	){
						test = false; 
					}else{
						if(children1.getSubTypeNode() != EnumSubType.NODE){
							if(children2.getSubTypeNode() != EnumSubType.NODE){
								//error: one of the two should be a node
								test = false; 
							}else{
								if(children1.getSubTypeNode() != EnumSubType.OVARIABLE){
									// error: format should be ov = node
									test = false; 
								}else{
									test = true; 
								}
							}
						}else{
							if(children2.getSubTypeNode() != EnumSubType.OVARIABLE){
								//error: format should be node = ov
								test = false; 
							}else{
								test = true; 
							}
						}
					}
				}
			}
		}
		
		return test; 
		
	}
	
	/**
	 * Return all the entities of one type. 
	 * 
	 * deprecated: 
	 * use the method searchEntitiesForOrdinaryVariable
	 */
	@Deprecated
	public List<String> getEntityByType(String type){
		return kb.getEntityByType(type); 
	}
	

	private List<OVInstance> intersection(List<OVInstance> list1, List<OVInstance> list2){
		
		List<OVInstance> listIntersection =  new ArrayList<OVInstance>(); 
		
		for(OVInstance ovIns: list1){
			if(list2.contains(ovIns)){
				listIntersection.add(ovIns); 
			}
		}
		
		return listIntersection; 
	}

	public SSBNAlgorithmInterationHelper getInterationHelper() {
		return interationHelper;
	}

	public void setInterationHelper(SSBNAlgorithmInterationHelper interationHelper) {
		this.interationHelper = interationHelper;
	}
	
	private KnowledgeBase getKnowledgeBase(){
		return this.kb; 
	}
	
}
