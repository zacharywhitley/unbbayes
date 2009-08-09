package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Auxiliar methods for treatment of the SimpleSSBNNode and conversion this for
 * normal SSBNNode's. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class SimpleSSBNNodeUtils {
	
	/**
	 * Translate the SimpleSSBNNode's for the SSBNNode. The SimpleSSBNNode was 
	 * created for economize memory in the step of build the network. For posterior
	 * steps, how generation of the CPTs, is necessary more informations that the
	 * information offer by the SimpleSSBNNode. The SSBNNode contain all the 
	 * information necessary. This method translate the simpleSSBNNodes to the 
	 * correspondent SSBNNode and add the informations about parents (context node 
	 * parents too). 
	 * 
	 * @param simpleSSBNNodeList
	 * @param pn The probabilisticNetwork where will be created the ProbabilisticNodes 
	 *           (referenced by the SSBNNodes)
	 *           
	 * @return The list contain the SSBNNodes correspondent to the simpleSSBNNodeList.
	 *         In this list don't are returned the ContextFatherSSBNNode's correspondent 
	 *         to the SimpleContextNodeFatherSSBNNode's. This is setted how parent
	 *         at the SSBNNode that originated it. 
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	public static List<SSBNNode> translateSimpleSSBNNodeListToSSBNNodeList( 
			List<SimpleSSBNNode> simpleSSBNNodeList, ProbabilisticNetwork pn) throws 
			                SSBNNodeGeneralException,  ImplementationRestrictionException{
		
		List<SSBNNode> listSSBNNodes = new ArrayList<SSBNNode>(); 
		Map<SimpleSSBNNode, SSBNNode> correspondencyMap = new HashMap<SimpleSSBNNode, SSBNNode>(); 
		
		Map<ContextNode, ContextFatherSSBNNode> mapContextNode = 
			    new HashMap<ContextNode, ContextFatherSSBNNode>(); 
		
		//1 Create all the nodes with its states 
		
		for(SimpleSSBNNode simple: simpleSSBNNodeList){
			
			SSBNNode ssbnNode = SSBNNode.getInstance(pn, simple.getResidentNode()); 
			correspondencyMap.put(simple, ssbnNode);
			listSSBNNodes.add(ssbnNode); 
			
			//Arguments. 
			for(int i = 0; i < simple.getOvArray().length; i++){
				OVInstance ovInstance = OVInstance.getInstance(
						simple.getOvArray()[i],	simple.getEntityArray()[i]); 
				ssbnNode.addArgument(ovInstance);
			}
			
			//Finding. 
			if(simple.isFinding()){
				ssbnNode.setValue(simple.getState()); 
			}
			
			
			//Default distribution
			if(simple.isDefaultDistribution()){
				ssbnNode.setUsingDefaultCPT(true); 
			}
			
			ssbnNode.setPermanent(true); 
			
			//The values of the ordinary variables are different dependeing on what MFrag we are dealing
			
			// lets deal first at resident node's MFrag
			OrdinaryVariable[] residentOvArray = ssbnNode.getResident().getOrdinaryVariableList().toArray(
															new OrdinaryVariable[ssbnNode.getResident().getOrdinaryVariableList().size()]
												  ); 
			
			List<OVInstance> argumentsForResidentMFrag = new ArrayList<OVInstance>(); 
			for(int i = 0; i < residentOvArray.length; i++){
				OVInstance ovInstance = OVInstance.getInstance(residentOvArray[i], simple.getEntityArray()[i]); 
				argumentsForResidentMFrag.add(ovInstance); 
			}
			
			ssbnNode.addArgumentsForMFrag(
					ssbnNode.getResident().getMFrag(), 
					argumentsForResidentMFrag); 
			
			
			// lets map OVs of every input node pointing to current SSBNNode
			for(InputNode inputNode: simple.getResidentNode().getInputInstanceFromList()){
				OrdinaryVariable[] ovArray = 
					inputNode.getResidentNodePointer().getOrdinaryVariableArray(); 
				
				List<OVInstance> argumentsForMFrag = new ArrayList<OVInstance>(); 
				for(int i = 0; i < ovArray.length; i++){
					OVInstance ovInstance = OVInstance.getInstance(ovArray[i], simple.getEntityArray()[i]); 
					argumentsForMFrag.add(ovInstance); 
				}
				
				ssbnNode.addArgumentsForMFrag(
						inputNode.getMFrag(), 
						argumentsForMFrag); 
			}
			
			//Treat the context node father
			List<SimpleContextNodeFatherSSBNNode> simpleContextNodeList = 
				simple.getContextParents(); 
			
			if(simpleContextNodeList.size() > 0){
				if(simpleContextNodeList.size() > 1){
					throw new ImplementationRestrictionException(
							ImplementationRestrictionException.MORE_THAN_ONE_CTXT_NODE_SEARCH); 
				}else{
					//We have only one context node father
					ContextNode contextNode = simpleContextNodeList.get(0).getContextNode(); 
					ContextFatherSSBNNode contextFather = mapContextNode.get(contextNode); 
					if(contextFather == null){
						contextFather = new ContextFatherSSBNNode(pn, contextNode);
						
						List<LiteralEntityInstance> possibleValueList = new ArrayList<LiteralEntityInstance>(); 
						for(String entity: simpleContextNodeList.get(0).getPossibleValues()){
							possibleValueList.add(LiteralEntityInstance.getInstance(entity, simpleContextNodeList.get(0).getOvProblematic().getValueType())); 
						}
						
						for(LiteralEntityInstance lei: possibleValueList){
							contextFather.addPossibleValue(lei);
						}
						
						contextFather.setOvProblematic(simpleContextNodeList.get(0).getOvProblematic()); 
						mapContextNode.put(contextNode, contextFather); 
					}
					
					try {
						ssbnNode.setContextFatherSSBNNode(contextFather);
					} catch (InvalidParentException e) {
						//This exception don't occur in this case... 
						e.printStackTrace();
						throw new RuntimeException(e.getMessage()); 
					} 
					
				}
				
			}
			
			simple.setProbNode(ssbnNode.getProbNode());
		}
		
		//Create the parent structure 
		
		for(SimpleSSBNNode simple: simpleSSBNNodeList){
			
//			if(simple.getParents().size()==0){
//				if(simple.getChildNodes().size()==0){
//					continue; //This node is out of the network. 
//				}
//			}
			
			SSBNNode ssbnNode = correspondencyMap.get(simple); 
			
			Runtime rt = Runtime.getRuntime(); 
			
			System.out.println("SSBNNode = " + ssbnNode);
			for(SimpleSSBNNode parent: simple.getParents()){
				System.out.println("Free Memory = " + rt.freeMemory() / 1024 / 1024 + "MB");
				System.out.println("   > add parent = " + parent);
				SSBNNode parentSSBNNode = correspondencyMap.get(parent); 
				ssbnNode.addParent(parentSSBNNode, false); 
			}
			
		}
		
		System.out.println("end..");
		
		return listSSBNNodes; 
	}
	
	/**
	 * The objective of this method is separete desconected networks in singles
	 * networks. The argument is a list of nodes that have its relations by the 
	 * parents atributes. From this relations, the nodes are separated in singles 
	 * networks where if two nodes have a edge between its, its are in the 
	 * same single network. 
	 * 
	 * @param simpleSSBNNodeList List contain the SimpleSSBNNodes 
	 * @return A list of lists of ssbn nodes, where each of this list are a 
	 *         single conected network. 
	 */
	public static List<SimpleSSBNNode>[] individualizeDesconectedNetworks(
			List<SimpleSSBNNode> simpleSSBNNodeList){
		
		List<SimpleSSBNNodeNetworkAssociation> nodeNetAssociationList = 
			new ArrayList<SimpleSSBNNodeNetworkAssociation>(); 
		
		for(SimpleSSBNNode simpleSSBNNode: simpleSSBNNodeList){
			SimpleSSBNNodeNetworkAssociation nodeNetAssociation = 
				new SimpleSSBNNodeNetworkAssociation(simpleSSBNNode); 
			nodeNetAssociationList.add(nodeNetAssociation); 
		}
		
		int nextNetworkId = 0; 
		
		for(SimpleSSBNNodeNetworkAssociation nodeNetAssociation: nodeNetAssociationList){
			
			if(nodeNetAssociation.getNetworkId() == -1){
				nodeNetAssociation.setNetworkId(nextNetworkId); 
				nextNetworkId++; 
				setNetIdToAdjacentNodes(nodeNetAssociation, nodeNetAssociationList); 
			}
			
		}
		
		List<SimpleSSBNNode>[] nodesPerNetworkArray = new List[nextNetworkId]; 
		
		for(int i = 0; i < nodesPerNetworkArray.length; i++){
			nodesPerNetworkArray[i] = new ArrayList<SimpleSSBNNode>(); 
		}
		
		for(SimpleSSBNNodeNetworkAssociation nodeNetAssociation: nodeNetAssociationList){
			nodesPerNetworkArray[nodeNetAssociation.getNetworkId()].add(nodeNetAssociation.getNode()); 
		}
		
		return nodesPerNetworkArray; 
	}
	
	//Recursive... 
	private static void setNetIdToAdjacentNodes(SimpleSSBNNodeNetworkAssociation nodeNetAssociation, 
			List<SimpleSSBNNodeNetworkAssociation> nodeNetAssociationList){
		
		for(SimpleSSBNNode parentNode: nodeNetAssociation.getNode().getParents()){
			for(SimpleSSBNNodeNetworkAssociation nodeParentTest: nodeNetAssociationList){
				if(parentNode == nodeParentTest.getNode()){
					if(nodeParentTest.getNetworkId() == -1){
						nodeParentTest.setNetworkId(nodeNetAssociation.getNetworkId()); 
						setNetIdToAdjacentNodes(nodeParentTest, nodeNetAssociationList);
					}
				}
			}
		}
		
		for(INode parentNode: nodeNetAssociation.getNode().getChildNodes()){
			for(SimpleSSBNNodeNetworkAssociation nodeParentTest: nodeNetAssociationList){
				if(parentNode == nodeParentTest.getNode()){
					if(nodeParentTest.getNetworkId() == -1){
						nodeParentTest.setNetworkId(nodeNetAssociation.getNetworkId()); 
						setNetIdToAdjacentNodes(nodeParentTest, nodeNetAssociationList); 
					}
				}
			}
		}
		
	}
	
	private static class SimpleSSBNNodeNetworkAssociation{
		final SimpleSSBNNode node; 
		int networkId = -1; 
		
		SimpleSSBNNodeNetworkAssociation(SimpleSSBNNode _node){
			node = _node; 
		}
		
		int getNetworkId(){
			return networkId; 
		}
		
		SimpleSSBNNode getNode(){
			return node; 
		}
		
		void setNetworkId(int _networkId){
			this.networkId = _networkId; 
		}
	}
	
	
}
