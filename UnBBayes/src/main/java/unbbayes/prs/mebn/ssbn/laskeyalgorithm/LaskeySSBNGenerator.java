package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.ArrayList;
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
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.BuilderStructureImpl;
import unbbayes.prs.mebn.ssbn.IBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.IBuilderStructure;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Parameters;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;

/**
 * Implementation of the Laskey's SSBN Algorithm
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class LaskeySSBNGenerator implements ISSBNGenerator{
	
	private Parameters parameters; 
	
	private IBuilderStructure builderStructure; 
	private IPruneStructure pruneStructure; 
	private IBuilderLocalDistribution buildLocalDistribution; 
	
	private final boolean addFindings = true;
	
	public LaskeySSBNGenerator(LaskeyAlgorithmParameters _parameters){
		
		parameters = _parameters; 
		
		//Set the default implementations. 
		setBuilderStructure(BuilderStructureImpl.newInstance()); 
		setPruneStructure(PruneStructureImpl.newInstance()); 
		setBuildLocalDistribution(BuilderLocalDistributionImpl.newInstance()); 
	
	}
	
	//Use Strategy design pattern
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase knowledgeBase)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {
		
		SSBN ssbn = null; 
		
		//Step 1: 
		if(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION).equals("true")){
			ssbn = initialization(queryList, knowledgeBase); 
		}
		
		//Step 2: 
		if(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_BUILDER).equals("true")){
			ssbn.getLogManager().appendln("----> Part 2/4: Builder Structure Started"); 
			buildStructure(ssbn); 
			ssbn.getLogManager().appendln("Builder Structure Finished");
			ssbn.getLogManager().appendSeparator(); 
		}
		
		//Step 3: 
		if(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_PRUNE).equals("true")){
			ssbn.getLogManager().appendln("----> Part 3/4: Prune Structure Started"); 
			pruneStruture(ssbn); 
			ssbn.getLogManager().appendln("Prune Structure Finished");
			ssbn.getLogManager().appendSeparator();
		}
		
		//Step 4: 
		if(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION).equals("true")){
			ssbn.getLogManager().appendln("----> Part 4/4: Generate CPT's started"); 
			buildLocalDistribution(ssbn);
			ssbn.getLogManager().appendln("Generate CPT's Finished");
			ssbn.getLogManager().appendSeparator();
		}
		
		return ssbn;
	}
	
	//Initialization
	//Note: the findings are taken by the structure and not by the knowledge base
	private SSBN initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		
		SSBN ssbn = new SSBN(); 
		
		MultiEntityBayesianNetwork mebn = null; 
		ssbn.getLogManager().addTitle("SSBN Generation for query " + queryList.get(0)); 
		ssbn.getLogManager().appendln("----> Part 1/4: Initialization Started"); 
		
		ssbn.setKnowledgeBase(knowledgeBase); 
		
		//We assume that all the queries is referent to the same MEBN
		mebn = queryList.get(0).getMebn(); 
		
		//Parameters: 

		
		//Add queries to the list of nodes
		ssbn.getLogManager().appendln("Step 1: recover the query nodes");
		for(Query query: queryList){
			SimpleSSBNNode ssbnNode = SimpleSSBNNode.getInstance(query.getResidentNode()); 
			
			for(OVInstance argument : query.getArguments()){
				ssbnNode.setEntityForOv(argument.getOv(), argument.getEntity()); 	
			}
			
			ssbnNode.setFinished(false); 
			ssbn.addSSBNNodeIfItDontAdded(ssbnNode);
			
			ssbn.getLogManager().appendln("    -> " + ssbnNode);
			                                                                    
		}
		
		//Add findings to the list of nodes
		
		if(addFindings){
			ssbn.getLogManager().appendln("\nStep 2: recover the findings nodes");

			for(MFrag mFrag: mebn.getMFragList()){
				for(ResidentNode residentNode: mFrag.getResidentNodeList()){
					for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
						ssbn.getLogManager().appendln("Finding: " + finding);
						ObjectEntityInstance arguments[] = finding.getArguments(); 
						List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
						for(int i = 0; i < arguments.length ; i++){
							OrdinaryVariable ov = residentNode.getArgumentNumber(i + 1).getOVariable();
							LiteralEntityInstance lei = LiteralEntityInstance.getInstance(arguments[i].getName() , ov.getValueType()); 
							ovInstanceList.add(OVInstance.getInstance(ov, lei)); 
						}

						SimpleSSBNNode ssbnNode = SimpleSSBNNode.getInstance(residentNode); 

						for(OVInstance argument : ovInstanceList){
							ssbnNode.setEntityForOv(argument.getOv(), argument.getEntity()); 	
						}

						ssbnNode.setState(finding.getState()); 
						ssbnNode.setFinished(false); 

						ssbn.addSSBNNodeIfItDontAdded(ssbnNode); 

					}
				}
			}
		}
		
		ssbn.getLogManager().appendln("Initialization Finished");
		ssbn.getLogManager().appendSeparator(); 
		
		return ssbn; 
		
	}
	
	//Build Structure
	private void buildStructure(SSBN ssbn){
		getBuilderStructure().buildStructure(ssbn); 
	}
	
	//Prune Structure
	private void pruneStruture(SSBN ssbn){
		getPruneStructure().pruneStructure(ssbn); 
	}
	
	//Build Local Distribution
	private void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException{
		getBuildLocalDistribution().buildLocalDistribution(ssbn); 
	}

	public IBuilderStructure getBuilderStructure() {
		return builderStructure;
	}

	public void setBuilderStructure(IBuilderStructure builderStructure) {
		this.builderStructure = builderStructure;
	}

	public IPruneStructure getPruneStructure() {
		return pruneStructure;
	}

	public void setPruneStructure(IPruneStructure pruneStructure) {
		this.pruneStructure = pruneStructure;
	}

	public IBuilderLocalDistribution getBuildLocalDistribution() {
		return buildLocalDistribution;
	}

	public void setBuildLocalDistribution(
			IBuilderLocalDistribution buildLocalDistribution) {
		this.buildLocalDistribution = buildLocalDistribution;
	}
	
}
