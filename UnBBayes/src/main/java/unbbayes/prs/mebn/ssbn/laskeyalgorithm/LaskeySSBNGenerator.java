package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.io.LogManager;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.BuilderStructure;
import unbbayes.prs.mebn.ssbn.BuilderStructureImpl;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.PruneStructure;
import unbbayes.prs.mebn.ssbn.PruneStructureImpl;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Implementation of the Laskey's SSBN Algorithm
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class LaskeySSBNGenerator implements ISSBNGenerator{
	
	private List<Query> queries; 
	private KnowledgeBase knowledgeBase; 
	private MultiEntityBayesianNetwork mebn; 
	private SSBN ssbn; 
	
	private BuilderStructure builderStructure; 
	private PruneStructure pruneStructure; 
	
	public LaskeySSBNGenerator(){
		setBuilderStructure(new BuilderStructureImpl()); 
		setPruneStructure(new PruneStructureImpl()); 
	}
	
	//Use Strategy
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase knowledgeBase)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {
		
		//Step 0: Testing the arguments and restrictions 
		
		this.ssbn = new SSBN(); 
		
		if(queryList.size() == 0){
			//TODO
		}
		
		//Step 1: 
		initialization(queryList, knowledgeBase); 
		
		System.out.println("Nodes found = " + ssbn.getSsbnNodeList().size());
		
		//Step 2: 
		buildStructure(); 
		
		//Step 3: 
		pruneStruture(); 
		
		//Step 4: 
		buildLocalDistribution(); 
		
		return ssbn;
	}
	
	//Initialization
	//Note: the findings are taken by the structure and not by the knowledge base
	private void initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		
		System.out.println("\n\n\nInitialization started");
		
		this.knowledgeBase = knowledgeBase; 
		
		//We assume that all the queries is referent to the same MEBN
		this.mebn = queryList.get(0).getMebn(); 
		
		//Parameters: 
		
		//recursiveCallLimit
		
		//Add queries to the list of nodes
		System.out.println("\nStep 1: recover the query nodes");
		for(Query query: queryList){
			SimpleSSBNNode node = ssbn.createSSBNNode(query.getResidentNode(), query.getArguments());  
			node.setFinished(false); 
			System.out.println("    -> " + node);
		}
		
		//Add findings to the list of nodes
		System.out.println("\nStep 2: recover the findings nodes");
		for(MFrag mFrag: mebn.getMFragList()){
			for(ResidentNode residentNode: mFrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
					
					ObjectEntityInstance arguments[] = finding.getArguments(); 
					List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
					for(int i = 0; i < arguments.length ; i++){
						OrdinaryVariable ov = residentNode.getArgumentNumber(i + 1).getOVariable();
						LiteralEntityInstance lei = LiteralEntityInstance.getInstance(arguments[i].getName() , ov.getValueType()); 
						ovInstanceList.add(OVInstance.getInstance(ov, lei)); 
					}
					
					SimpleSSBNNode node = ssbn.createSSBNNode(residentNode, ovInstanceList); 
					node.setState(finding.getState()); 
					node.setFinished(false); 
					System.out.println("    -> " + node);
					
				}
			}
		}
		
		System.out.println("\nInitialization finished");
		
	}
	
	//Build Structure
	private void buildStructure(){
		getBuilderStructure().buildStructure(ssbn, knowledgeBase); 
	}
	
	//Prune Structure
	private void pruneStruture(){
		getPruneStructure().pruneStructure(ssbn); 
	}
	
	//Build Local Distribution
	private void buildLocalDistribution(){
		//Adapte the structure for a SSBNNode with all necessary (if a Simple
		//node is used) and create the CPT of each node. 
	}

	public BuilderStructure getBuilderStructure() {
		return builderStructure;
	}

	public void setBuilderStructure(BuilderStructure builderStructure) {
		this.builderStructure = builderStructure;
	}

	public PruneStructure getPruneStructure() {
		return pruneStructure;
	}

	public void setPruneStructure(PruneStructure pruneStructure) {
		this.pruneStructure = pruneStructure;
	}
	
}
