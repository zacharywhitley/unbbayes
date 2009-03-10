package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.List;

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
		
		//Step 2: 
		buildStructure(); 
		
		//Step 3: 
		pruneStruture(); 
		
		//Step 4: 
		buildLocalDistribution(); 
		
		return ssbn;
	}
	
	//Initialization
	private void initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		
		this.knowledgeBase = knowledgeBase; 
		
		
		//We assume that all the queries is referent to the same MEBN
		this.mebn = queryList.get(0).getMebn(); 
		
		//Parameters: 
		
		//recursiveCallLimit
		
		
		//Add queries to the list of nodes
		for(Query query: queryList){
			SimpleSSBNNode node =  SimpleSSBNNode.getInstance(query.getResidentNode()); 
			for(OVInstance ovInstance: query.getArguments()){
				node.addArgument(ovInstance); 
			} 
			ssbn.addSSBNNode(node); 
		}
		
		//Add findings to the list of nodes
		for(MFrag mFrag: this.mebn.getMFragList()){
			for(ResidentNode residentNode: mFrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
					SimpleSSBNNode node = SimpleSSBNNode.getInstance(residentNode); 
					
					ObjectEntityInstance arguments[] = finding.getArguments(); 
					for(int i = 1; i < arguments.length; i++){
						OrdinaryVariable ov = residentNode.getArgumentNumber(i).getOVariable();
						LiteralEntityInstance lei = LiteralEntityInstance.getInstance(arguments[i-1].getName() , ov.getValueType()); 
						node.addArgument(OVInstance.getInstance(ov, lei)); 
					}
					
					node.setState(finding.getState()); 
					ssbn.addSSBNNode(node);
				}
			}
		}
		
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
