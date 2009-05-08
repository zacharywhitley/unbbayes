package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

public class SimpleSSBNNodeUtils {

	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	
	
	public static List<SSBNNode> translateSimpleSSBNNodeListToSSBNNodeList( 
			List<SimpleSSBNNode> list, ProbabilisticNetwork pn) throws SSBNNodeGeneralException{
		
		List<SSBNNode> listSSBNNodes = new ArrayList<SSBNNode>(); 
		Map<SimpleSSBNNode, SSBNNode> correspondencyMap = new HashMap<SimpleSSBNNode, SSBNNode>(); 
		
		//1 Create all the nodes with its states 
		
		for(SimpleSSBNNode simple: list){
			
			if(simple.getParents().size()==0){
				if(simple.getChildNodes().size()==0){
					continue; //This node is out of the network. 
				}
			}
			
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
			
			//The values of the ordinary variables at the differents MFrags
			for(InputNode inputNode: simple.getResidentNode().getInputInstanceFromList()){
				OrdinaryVariable[] ovArray = 
					inputNode.getResidentNodePointer().getOrdinaryVariableArray(); 
				
				List<OVInstance> argumentsForMFrag = new ArrayList<OVInstance>(); 
				for(int i = 0; i < ovArray.length; i++){
					OVInstance ovInstance = OVInstance.getInstance(ovArray[i], simple.getEntityArray()[i]); 
					argumentsForMFrag.add(ovInstance); 
				}
				
				ssbnNode.addArgumentsForMFrag(
						inputNode.getResidentNodePointer().getResidentNode().getMFrag(), 
						argumentsForMFrag); 
			}
			
		}
		
		//Create the parent structure 
		
		for(SimpleSSBNNode simple: list){
			
			if(simple.getParents().size()==0){
				if(simple.getChildNodes().size()==0){
					continue; //This node is out of the network. 
				}
			}
			
			SSBNNode ssbnNode = correspondencyMap.get(simple); 
			
			for(SimpleSSBNNode parent: simple.getParents()){
				SSBNNode parentSSBNNode = correspondencyMap.get(parent); 
				ssbnNode.addParent(parentSSBNNode, false); 
			}
			
		}
		
		return listSSBNNodes; 
	}
	
	
}
