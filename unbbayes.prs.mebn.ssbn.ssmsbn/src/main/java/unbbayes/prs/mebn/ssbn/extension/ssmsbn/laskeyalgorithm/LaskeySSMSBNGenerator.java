/**
 * 
 */
package unbbayes.prs.mebn.ssbn.extension.ssmsbn.laskeyalgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.MSBNController;
import unbbayes.io.log.IdentationLevel;
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
import unbbayes.prs.mebn.ssbn.extension.ssmsbn.ISSMSBNBuilder;
import unbbayes.prs.mebn.ssbn.extension.ssmsbn.SSMSBNBuilder;
import unbbayes.prs.mebn.ssbn.extension.ssmsbn.SSMSBNBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.DSeparationPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.Debug;

/**
 * This class implements the algorithm of Dr. Laskey, but resulting in a MSBN
 * 
 * @author rafaelmezzomo
 * @author estevaoaguiar
 */
public class LaskeySSMSBNGenerator extends LaskeySSBNGenerator {
	

	private Parameters parameters; 
	
	private IBuilderStructure builderStructure; 
	private IPruneStructure pruneStructure; 
	private IBuilderLocalDistribution buildLocalDistribution; 
	
	private ResourceBundle resourceLog = 
		unbbayes.util.ResourceController.newInstance().getBundle(ResourcesSSBNAlgorithmLog.class.getName()); 
	
	private final boolean addFindings = true;
	
	private INetworkMediator mediator;
	
	private ISSMSBNBuilder ssmsbnBuilder;
	
	/**
	 * The constructor is protected because we're using factory method
	 * 
	 * @param parameters
	 */
	protected LaskeySSMSBNGenerator(LaskeyAlgorithmParameters parameters) {
		super(parameters);
		this.setParameters(parameters);
		// TODO Auto-generated constructor stub
	}
	
	public static ISSBNGenerator newInstance(){
		//Initialize Laskey algorithm using default parameter values
		LaskeyAlgorithmParameters param = new LaskeyAlgorithmParameters();
		param.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		param.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
		ISSBNGenerator ret = new LaskeySSMSBNGenerator(param);
		//Inicialization with the SSMSBNBuilder
		((LaskeySSMSBNGenerator)ret).setSsmsbnBuilder(SSMSBNBuilder.newInstance());
	
		
		// assure the initialization of prune structure, using default pruners
		List<IPruner> pruners = new ArrayList<IPruner>();
		pruners.add(BarrenNodePruner.newInstance());	// barren node pruning is enabled by default
		pruners.add(DSeparationPruner.newInstance());	// d-separated node pruning is enabled by default
		((LaskeySSMSBNGenerator)ret).setPruneStructure(PruneStructureImpl.newInstance(pruners));
		
		// Inicialization with the BuilderLocalDistribution for SSMSBN
		((LaskeySSMSBNGenerator)ret).setBuildLocalDistribution(SSMSBNBuilderLocalDistribution.newInstance());
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ssmsbn.laskeyalgorithm.LaskeySSBNGenerator#showSSBN(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	protected void showSSBN(SSBN ssbn) {
		AbstractMSBN msbn = (AbstractMSBN)ssbn.getNetwork();
		MSBNController controller = new MSBNController((SingleAgentMSBN)msbn);
		
		this.getMediator().getScreen().getDesktopPane().add(controller.getPanel());
		controller.getPanel().setSize(controller.getPanel().getPreferredSize());
		controller.getPanel().setVisible(true);
		
	}
	
	
	
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
			
			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printBox2Bar(); 
			ssbn.getLogManager().printBox2("List of nodes: "); 
			printSimpleSSBNNodeList(ssbn); 
			ssbn.getLogManager().printBox2Bar(); 
			
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
			
			ssbn.getLogManager().skipLine(); 
			ssbn.getLogManager().printBox2Bar(); 
			ssbn.getLogManager().printBox2("List of nodes: "); 
			printSimpleSSBNNodeList(ssbn); 
			ssbn.getLogManager().printBox2Bar(); 

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
		
		try {
			SSBNDebugInformationUtil.printAndSaveCurrentNetwork(ssbn); 
			ssbn.getLogManager().printBox1Bar(); 
			ssbn.getLogManager().printBox1(resourceLog.getString("007_ExecutionSucces")); 
			ssbn.getLogManager().printBox1(resourceLog.getString("009_Time") + ": " + deltaTime + " ms"); 
			ssbn.getLogManager().printBox1Bar(); 
			ssbn.getLogManager().skipLine();

		} catch (Exception e) {
			//e.printStackTrace();
			Debug.println(this.getClass(), "Error printing log", e);
		}
		 
		
		this.cleanUpSSBN(ssbn);
		
		try {
			ssbn.compileAndInitializeSSBN();
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		// show on display
		this.showSSBN(ssbn);
		
		return ssbn;
	}
	

	
	protected void cleanUpSSBN(SSBN ssbn){
		ssbn.getSimpleSsbnNodeList().clear();
		for (SSBNNode node : ssbn.getSsbnNodeList()) {
			node.clearArgumentsForMFrag();
			node.setCompiler(null);
		}
		
		System.gc();
	}

	protected void printSimpleSSBNNodeList(SSBN ssbn) {
		for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
			String parentIdList = " ";
			for(INode nodeParent: node.getParentNodes()){
			      parentIdList+= ((SimpleSSBNNode)nodeParent).getId() + " "; 
			}
			ssbn.getLogManager().printBox2(
					"   - " + node.toString() + " Parents = [" + parentIdList +"]");
		}
		ssbn.getLogManager().appendln("");
	}
	
	//Initialization
	//Note: the findings are taken by the structure and not by the knowledge base
	protected SSBN initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		/**
		 * Using builder design pattern.
		 */
		SSBN ssbn = this.getSsmsbnBuilder().buildSSMSBN();

		
		
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

		IdentationLevel in = new IdentationLevel(null); 
		
		//Add queries to the list of nodes
		IdentationLevel in1 = new IdentationLevel(in); 
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
			ssbn.addQueryToQueryList(query); 
			
			ssbn.getLogManager().printText(in1, false, " - " + ssbnNode); 
			
			                                                                    
		}
		
		ssbn.getLogManager().skipLine(); 
		
		//Add findings to the list of nodes
		
		if(addFindings){
			in1 = new IdentationLevel(in); 
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
						
						ssbn.addFindingToFindingList(ssbnNode); 
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
	protected void buildStructure(SSBN ssbn) throws ImplementationRestrictionException, SSBNNodeGeneralException{
		getBuilderStructure().buildStructure(ssbn); 
	}
	
	//Prune Structure
	protected void pruneStruture(SSBN ssbn){
		getPruneStructure().pruneStructure(ssbn); 
	}
	
	//Build Local Distribution
	protected void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException{
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

	

	public INetworkMediator getMediator() {
		return mediator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @return the ssmsbn
	 */
	public ISSMSBNBuilder getSsmsbnBuilder() {
		return ssmsbnBuilder;
	}

	/**
	 * @param ssmsbn the ssmsbn to set
	 */
	public void setSsmsbnBuilder(ISSMSBNBuilder ssmsbn) {
		this.ssmsbnBuilder = ssmsbn;
	}

	/**
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}

	/**
	 * @param the parameters to set
	 */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the resourceLog
	 */
	public ResourceBundle getResourceLog() {
		return resourceLog;
	}

	/**
	 * @param resourceLog the resourceLog to set
	 */
	public void setResourceLog(ResourceBundle resourceLog) {
		this.resourceLog = resourceLog;
	}

	/**
	 * @return the addFindings
	 */
	public boolean isAddFindings() {
		return addFindings;
	}
	
	
}
