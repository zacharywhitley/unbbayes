package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.kb.KnowledgeBase;

public class BuilderStructureImpl implements BuilderStructure{

	//The structure is build from the query nodes. The finding nodes that 
	//don't is in a path don't will be created. Solution: don't search the
	//finding nodes before the algorithm evalution. (r-SSBN)
	//Bad  - Evaluated if a ssbnnode is a finding
	//Good - In the other option, is verified the duplicated ssbn node possibility for 
	//       see if the node is a finding. The number of evidences should be
    //       very large! 
	
	// For each node Y in fragment F, if all parents of Y are in SSBN and 
	//all arcs from parents of Y to Y are in SSBN then mark Y finished, 
	//else mark Y unfinished
	
	//(The laskey algorithm only is up!!! If you add the finding nodes 
	//for necessity, you shoul'd have to up and to down. This difficult 
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
		// TODO Auto-generated method stub
		return false;
	}

}
