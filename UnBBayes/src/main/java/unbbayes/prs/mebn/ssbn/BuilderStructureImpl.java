package unbbayes.prs.mebn.ssbn;

import java.util.List;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BuilderStructureImpl implements BuilderStructure{

	//The structure is build from the query nodes. The finding nodes that 
	//don't is in a path don't will be created. Solution: don't search the
	//finding nodes before the algorithm evaluation. (r-SSBN)
	//Bad  - Evaluated if a SSBNNode is a finding
	//Good - In the other option, is verified the duplicated SSBN node possibility for 
	//       see if the node is a finding. The number of evidences should be
	//       very large! 

	// For each node Y in fragment F, if all parents of Y are in SSBN and 
	//all arcs from parents of Y to Y are in SSBN then mark Y finished, 
	//else mark Y unfinished

	//(The Laskey's algorithm only is up!!! If you add the finding nodes 
	//for necessity, you shouldn't have to up and to down. This difficult 
	//the control because the down part is more difficult. The solution should
	//make a simple father structure for evaluate what node is child of 
	//what node... or create all findings how the sugestion (the simple 
	//structure should be a posterior perform). 

	//Question: how the network is connected, always will have a path 
	//between two nodes? Yes! Then the solution of a simple structure don't 
	//work well. 


	//The great question here is the context node evaluation. 

	public boolean buildStructure(SSBN ssbn,
			KnowledgeBase kb) {

		boolean allNodesFinished = false; 

		//While have one or more nodes marked finished = false, this loop 
		//will pass by the node list again.  
		while(!allNodesFinished){

			allNodesFinished = true; 

			for(SimpleSSBNNode node: ssbn.getSsbnNodeList()){
				if(!node.isFinished()){
					allNodesFinished = false; 
				}
			}

		}

		return false;
	}

	/*
	 * Evaluate a node, creating the necessary nodes and edges to mark it how 	
	 * finished. 
	 */
	private void evaluateUnfinishedRV(SimpleSSBNNode node){

		//Verify if the MFrag instance is already evaluated and mark the rv
		//how finished if true. 

		//Build the MFragInstance related to the node
		MFragInstance mFragInstance = new MFragInstance(node.getResidentNode().getMFrag()); 


		for(OVInstance ovInstance: node.getListArguments()){
			mFragInstance.addInstanciatedOV(ovInstance.getOv(), ovInstance.getEntity()); 
		}

       // Evaluate the context nodes to generate the others ordinary variables

		if(mFragInstance.getOVFaultList().size() > 0){
			//Recover the others ov instance...  
			//Lance exception if a ordinary variable can be recover
		}	

		//Search for a duplicated MFragInstance

	}

	/*
	 * 
	 */
	private void evaluateMFragInstance(MFragInstance mFragInstance){

		MFrag mFrag = mFragInstance.getMFragOrigin(); 
		
		//All the ordinary variables are filled... search for all the nodes of 
		//the MFrag.
		
		//Step 0: Evaluate the context 
		
		
		
		//Step 1: Resident Nodes
		//Mount list of possible combinations of the ov instances and mount the
		//list of nodes. 
		for(ResidentNode residentNode: mFrag.getResidentNodeList()){
			SimpleSSBNNode ssbnNode = SimpleSSBNNode.getInstance(residentNode);
			
			for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
				List<LiteralEntityInstance> listEntityInstance = mFragInstance.getInstanciatedOV(ov); 
				if(listEntityInstance.size() == 1){
					ssbnNode.addArgument(OVInstance.getInstance(ov, listEntityInstance.get(0))); 
				}else{
					if(mFragInstance.getContextNodeFather(ov) != null){
						
					}else{
						
					}
				}
			}
		}
		
		//Step 2: Input Nodes

	}

}
