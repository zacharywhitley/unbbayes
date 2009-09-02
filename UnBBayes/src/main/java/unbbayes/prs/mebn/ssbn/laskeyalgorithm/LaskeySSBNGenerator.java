package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.log.IdentationNivel;
import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.resources.ResourcesSSBNAlgorithmLog;
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
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;

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
	
	private ResourceBundle resourceLog = 
		ResourceBundle.getBundle(ResourcesSSBNAlgorithmLog.class.getName()); 
	
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
		
		long time0 = System.currentTimeMillis(); 
		
		SSBN ssbn = null; 
		
		//Step 1: 
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION))){
			ssbn = initialization(queryList, knowledgeBase); 
			ssbn.setParameters(parameters); 
		}
		
		//Step 2: 
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_BUILDER))){
			
			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printText(null, false, resourceLog.getString("004_Step2_BuildingGrandBN")); 
			ssbn.getLogManager().skipLine(); 
			
			buildStructure(ssbn); 
			
			ssbn.getLogManager().printText(null, false, resourceLog.getString("010_StepFinished")); 
			
			ssbn.getLogManager().appendln("List of nodes: "); 
			printSimpleSSBNNodeList(ssbn); 

			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printSectionSeparation(); 
		}
		
		//Step 3: 
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_PRUNE))){
			
			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printText(null, false, resourceLog.getString("005_Step3_PruneGrandBN")); 
			ssbn.getLogManager().skipLine(); 
			
			pruneStruture(ssbn); 
			
			ssbn.getLogManager().printText(null, false, resourceLog.getString("010_StepFinished")); 
			
			ssbn.getLogManager().appendln("\nList of nodes: "); 
			printSimpleSSBNNodeList(ssbn); 

			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printSectionSeparation(); 
		}
		
		//Step 4: 
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION))){
			
			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printText(null, false, resourceLog.getString("006_Step4_BuildCPT")); 
			ssbn.getLogManager().skipLine(); 
			
			buildLocalDistribution(ssbn);
			ssbn.getLogManager().printText(null, false, resourceLog.getString("010_StepFinished")); 
			
			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printSectionSeparation();
		}
		
		long time1 = System.currentTimeMillis(); 
		long deltaTime = time1 - time0; 

		SSBNDebugInformationUtil.printAndSaveCurrentNetwork(ssbn); 
		
		ssbn.getLogManager().printBox1Bar(); 
		ssbn.getLogManager().printBox1(resourceLog.getString("007_ExecutionSucces")); 
		ssbn.getLogManager().printBox1(resourceLog.getString("009_Time") + ": " + deltaTime + " ms"); 
		ssbn.getLogManager().printBox1Bar(); 
		
		this.cleanUpSSBN(ssbn);
		
		return ssbn;
	}
	
	private void cleanUpSSBN(SSBN ssbn){
		ssbn.getSimpleSsbnNodeList().clear();
		for (SSBNNode node : ssbn.getSsbnNodeList()) {
			node.clearArgumentsForMFrag();
			node.setCompiler(null);
		}
		
		System.gc();
	}

	private void printSimpleSSBNNodeList(SSBN ssbn) {
		for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
			String parentIdList = " ";
			for(INode nodeParent: node.getParentNodes()){
			      parentIdList+= ((SimpleSSBNNode)nodeParent).getId() + " "; 
			}
			ssbn.getLogManager().appendln(
					"   - " + node.toString() + " Parents = [" + parentIdList +"]");
		}
		ssbn.getLogManager().appendln("");
	}
	
	//Initialization
	//Note: the findings are taken by the structure and not by the knowledge base
	private SSBN initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		
		SSBN ssbn = new SSBN(); 
		
		MultiEntityBayesianNetwork mebn = null; 

		//log
		ssbn.getLogManager().printBox1Bar(); 
		ssbn.getLogManager().printBox1(resourceLog.getString("001_Title")); 
		ssbn.getLogManager().printBox1("Queries:"); 
		for(int i = 0; i < queryList.size(); i++){
			ssbn.getLogManager().printBox1("    (" + i + ") " + queryList.get(i).toString()); 
		}
		ssbn.getLogManager().printBox1(resourceLog.getString("002_Algorithm") + ": " + 
				                       resourceLog.getString("002_001_LaskeyAlgorithm")); 
		ssbn.getLogManager().printBox1Bar(); 
		
		ssbn.getLogManager().skipLine(); 
		ssbn.getLogManager().printText(null, false, resourceLog.getString("003_Step1_Initialization")); 
		ssbn.getLogManager().skipLine(); 
		
		ssbn.setKnowledgeBase(knowledgeBase); 
		
		//We assume that all the queries is referent to the same MEBN
		mebn = queryList.get(0).getMebn(); 
		
		//Parameters: 

		IdentationNivel in = new IdentationNivel(null); 
		
		//Add queries to the list of nodes
		IdentationNivel in1 = new IdentationNivel(in); 
		ssbn.getLogManager().printText(in1, true, 
				resourceLog.getString("011_BuildingSSBNForQueries")); 
		
		for(Query query: queryList){
			SimpleSSBNNode ssbnNode = SimpleSSBNNode.getInstance(query.getResidentNode()); 
			query.setSSBNNode(ssbnNode); 
			
			for(OVInstance argument : query.getArguments()){
				ssbnNode.setEntityForOv(argument.getOv(), argument.getEntity()); 	
			}
			
			ssbnNode.setFinished(false); 
			ssbn.addSSBNNodeIfItDontAdded(ssbnNode);
			ssbn.addQueryToTheQueryList(query); 
			
			ssbn.getLogManager().printText(in1, false, " - " + ssbnNode); 
			
			                                                                    
		}
		
		ssbn.getLogManager().skipLine(); 
		
		//Add findings to the list of nodes
		
		if(addFindings){
			in1 = new IdentationNivel(in); 
			ssbn.getLogManager().printText(in1, true, 
					resourceLog.getString("012_BuildingSSBNForFindings")); 

			for(MFrag mFrag: mebn.getMFragList()){
				for(ResidentNode residentNode: mFrag.getResidentNodeList()){
					for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
						
						ssbn.getLogManager().printText(in1, false, " - " + finding); 
						
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
						ssbnNode = ssbn.addSSBNNodeIfItDontAdded(ssbnNode); 

						ssbnNode.setState(finding.getState()); 
						ssbnNode.setFinished(false); 
						
						ssbn.addFindingToTheFindingList(ssbnNode); 
					}
				}
			}
		}
		
		ssbn.getLogManager().skipLine(); 
		ssbn.getLogManager().printText(null, false, resourceLog.getString("010_StepFinished")); 
		
		ssbn.getLogManager().skipLine(); 
		ssbn.getLogManager().printSectionSeparation(); 
		
		return ssbn; 
		
	}
	
	//Build Structure
	private void buildStructure(SSBN ssbn) throws ImplementationRestrictionException, SSBNNodeGeneralException{
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
