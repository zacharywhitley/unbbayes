package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.log.ISSBNLogManager;
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
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.BuilderStructureImpl;
import unbbayes.prs.mebn.ssbn.IBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.IBuilderStructure;
import unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator;
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
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.util.Debug;

/**
 * Implementation of the Laskey's SSBN Algorithm
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * 
 * @version 2010-05-19 - refactor to implement IMediatorAwareSSBNGenerator
 * @author Shou Matsumoto
 */

public class LaskeySSBNGenerator implements IMediatorAwareSSBNGenerator{
	
	private boolean isLogEnabled = true;
	
	private Parameters parameters; 
	
	private IBuilderStructure builderStructure; 
	private IPruneStructure pruneStructure; 
	private IBuilderLocalDistribution buildLocalDistribution; 
	
	private ResourceBundle resourceLog = 
		unbbayes.util.ResourceController.newInstance().getBundle(ResourcesSSBNAlgorithmLog.class.getName()); 
	
	private final boolean addFindings = true;
	
	private INetworkMediator mediator;
	
	public LaskeySSBNGenerator(LaskeyAlgorithmParameters _parameters){
		
		setParameters(_parameters); 
		
		//Set the default implementations. 
		setBuilderStructure(BuilderStructureImpl.newInstance()); 
		setPruneStructure(PruneStructureImpl.newInstance()); 
		setBuildLocalDistribution(BuilderLocalDistributionImpl.newInstance()); 
	
	}
	
	
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase knowledgeBase)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {
		
		long time0 = System.currentTimeMillis(); 
		
		SSBN ssbn = null; 
		
		//Step 1: 
		if(Boolean.valueOf(getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION))){
			ssbn = initialization(queryList, knowledgeBase); 
			ssbn.setParameters(getParameters()); 
		}
		
		ISSBNLogManager logManager = null;
		if (ssbn != null) {
			logManager = ssbn.getLogManager();
		}
		
		//Step 2: 
		if(Boolean.valueOf(getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_BUILDER))){
			
			if (logManager != null) {
				logManager.skipLine(); 
				logManager.printText(null, false, resourceLog.getString("004_Step2_BuildingGrandBN")); 
				logManager.skipLine(); 
			}
			
			buildStructure(ssbn); 
			
			if (logManager != null) {
				logManager.printText(null, false, resourceLog.getString("010_StepFinished")); 
				logManager.skipLine(); 
				logManager.printBox2Bar(); 
				logManager.printBox2("List of nodes: "); 
				printSimpleSSBNNodeList(ssbn); 
				logManager.printBox2Bar(); 
				logManager.skipLine(); 
				logManager.printSectionSeparation(); 
			}
			
		}
		
		//Step 3: 
		if(Boolean.valueOf(getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_PRUNE))){
			
			if (logManager != null) {
				logManager.skipLine();
				logManager.printText(null, false, resourceLog.getString("005_Step3_PruneGrandBN")); 
				logManager.skipLine(); 
			}
			
			pruneStruture(ssbn); 
			
			if (logManager != null) {
				logManager.printText(null, false, resourceLog.getString("010_StepFinished")); 
				logManager.skipLine(); 
				logManager.printBox2Bar(); 
				logManager.printBox2("List of nodes: "); 
				printSimpleSSBNNodeList(ssbn); 
				logManager.printBox2Bar(); 
				logManager.skipLine(); 
				logManager.printSectionSeparation(); 
			}
		}
		
		//Step 4: 
		if(Boolean.valueOf(getParameters().getParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION))){
			
			if (logManager != null) {
				logManager.skipLine();
				logManager.printText(null, false, resourceLog.getString("006_Step4_BuildCPT")); 
				logManager.skipLine(); 
			}
			
			buildLocalDistribution(ssbn);
			if (logManager != null) {
				logManager.printText(null, false, resourceLog.getString("010_StepFinished")); 
				logManager.skipLine(); 
				logManager.printSectionSeparation();
			}
		}
		
		// adjust position of nodes
		try {
			PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(ssbn.getProbabilisticNetwork());
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		if (logManager != null) {
			long time1 = System.currentTimeMillis(); 
			long deltaTime = time1 - time0; 
			SSBNDebugInformationUtil.printAndSaveCurrentNetwork(ssbn); 
			logManager.printBox1Bar();
			logManager.printBox1(resourceLog.getString("007_ExecutionSucces")); 
			logManager.printBox1(resourceLog.getString("009_Time") + ": " + deltaTime + " ms"); 
			logManager.printBox1Bar(); 
			logManager.skipLine(); 
		}
		
		this.cleanUpSSBN(ssbn);
		
		if (ssbn.getNetwork().getNodeCount() != ssbn.getNetwork().getNodeIndexes().keySet().size()) {
			// inconsistency on the quantity of indexed nodes and actual nodes
			// force synchronization
			ssbn.getNetwork().getNodeIndexes().clear();
			for (int i = 0; i < ssbn.getNetwork().getNodes().size(); i++) {
				ssbn.getNetwork().getNodeIndexes().put(ssbn.getNetwork().getNodes().get(i).getName(), i);
			}
		}
		
		try {
			this.compileAndInitializeSSBN(ssbn);;
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		// show on display
		this.showSSBN(ssbn);
		
		return ssbn;
	}
	
	/**
	 * Compiles SSBN, fill findings, and propagate findings.
	 * @param ssbn : the SSBN to be compiled and whose findings will be propagated.
	 * @throws Exception : when compilation/propagation fails.
	 * @see SSBN#compileAndInitializeSSBN()
	 */
	protected void compileAndInitializeSSBN(SSBN ssbn) throws Exception {
		ssbn.compileAndInitializeSSBN();
	}
	
	/**
	 * Uses mediator to display the SSBN
	 * @param mediator : MEBNController
	 * @param  ssbn : the ssbn to show
	 */
	protected void showSSBN(SSBN ssbn) {
//		if (this.getMediator() == null) {
//			// if there is no mediator, we cannot go on
//			return;
//		}
//		NetworkWindow window = new NetworkWindow(ssbn.getNetwork());
//		this.getMediator().getScreen().getUnbbayesFrame().addWindow(window);
//		window.setVisible(true);
		// use the above code to show compiled network in a separate internal frame
		if (this.getMediator() instanceof IMEBNMediator) {
			((IMEBNMediator)this.getMediator()).setSpecificSituationBayesianNetwork(ssbn.getProbabilisticNetwork());
			((IMEBNMediator)this.getMediator()).setToTurnToSSBNMode(true);	// if this is false, ((IMEBNMediator)this.getMediator()).turnToSSBNMode() will not work
			((IMEBNMediator)this.getMediator()).turnToSSBNMode();
			((IMEBNMediator)this.getMediator()).getScreen().getEvidenceTree().updateTree(true);;
		}
	}
	
	private void cleanUpSSBN(SSBN ssbn){
		ssbn.getSimpleSsbnNodeList().clear();
		for (SSBNNode node : ssbn.getSsbnNodeList()) {
			node.clearArgumentsForMFrag();
//			node.setCompiler(null); // this will set resident node's compiler to null!!
		}
		
//		System.gc();
	}

	private void printSimpleSSBNNodeList(SSBN ssbn) {
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
				String parentIdList = " ";
				for(INode nodeParent: node.getParentNodes()){
					parentIdList+= ((SimpleSSBNNode)nodeParent).getId() + " "; 
				}
				logManager.printBox2(
						"   - " + node.toString() + " Parents = [" + parentIdList +"]");
			}
			logManager.appendln("");
		}
	}
	
	//Initialization
	//Note: the findings are taken by the structure and not by the knowledge base
	private SSBN initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		
		SSBN ssbn = new SSBN(); 
		if (!this.isLogEnabled()) {
			ssbn.setLogManager(null);
		}
		
		MultiEntityBayesianNetwork mebn = null; 

		//log
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printBox1Bar(); 
			logManager.printBox1(resourceLog.getString("001_Title")); 
			logManager.printBox1("Queries:"); 
			for(int i = 0; i < queryList.size(); i++){
				logManager.printBox1("    (" + i + ") " + queryList.get(i).toString()); 
			}
			logManager.printBox1(resourceLog.getString("002_Algorithm") + ": " + 
					resourceLog.getString("002_001_LaskeyAlgorithm")); 
			logManager.printBox1Bar(); 
			
			logManager.skipLine(); 
			logManager.printText(null, false, resourceLog.getString("003_Step1_Initialization")); 
			logManager.skipLine(); 
			
		}
		ssbn.setKnowledgeBase(knowledgeBase); 
		
		//We assume that all the queries is referent to the same MEBN
		mebn = queryList.get(0).getMebn(); 
		
		//Parameters: 

		IdentationLevel in = new IdentationLevel(null); 
		
		//Add queries to the list of nodes
		IdentationLevel in1 = new IdentationLevel(in); 
		if (logManager != null) {
			logManager.printText(in1, true, 
				resourceLog.getString("011_BuildingSSBNForQueries")); 
		}
		for(Query query: queryList){
			SimpleSSBNNode ssbnNode = SimpleSSBNNode.getInstance(query.getResidentNode()); 
			query.setSSBNNode(ssbnNode); 
			
			for(OVInstance argument : query.getArguments()){
				ssbnNode.setEntityForOv(argument.getOv(), argument.getEntity()); 	
			}
			
			ssbnNode.setFinished(false); 
			ssbn.addSSBNNodeIfItDontAdded(ssbnNode);
			ssbn.addQueryToTheQueryList(query); 
			
			if (logManager != null) {
				logManager.printText(in1, false, " - " + ssbnNode); 
			}
			                                                                    
		}
		
		if (logManager != null) {
			logManager.skipLine(); 
		}
		//Add findings to the list of nodes
		
		if(addFindings){
			in1 = new IdentationLevel(in); 
			if (logManager != null) {
				logManager.printText(in1, true, 
					resourceLog.getString("012_BuildingSSBNForFindings")); 
			}
			for(MFrag mFrag: mebn.getMFragList()){
				for(ResidentNode residentNode: mFrag.getResidentNodeList()){
					for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
						
						if (logManager != null) {
							logManager.printText(in1, false, " - " + finding); 
						}
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
		
		if (logManager != null) {
			logManager.skipLine(); 
			logManager.printText(null, false, resourceLog.getString("010_StepFinished")); 
			logManager.skipLine(); 
			logManager.printSectionSeparation(); 
		}
		return ssbn; 
		
	}
	
	/**
	 * This method builds the SSBN structure (nodes and arcs) by using the {@link SimpleSSBNNode}, which are
	 * nodes created especially in order to use small amount of memory (e.g. by not using space for huge CPTs or other
	 * complementary classes). The actual BN and its probability distribution are generated at {@link #buildLocalDistribution(SSBN)}.
	 * @param ssbn
	 * @throws ImplementationRestrictionException
	 * @throws SSBNNodeGeneralException
	 * @see #getBuilderStructure()
	 */
	protected void buildStructure(SSBN ssbn) throws ImplementationRestrictionException, SSBNNodeGeneralException{
		getBuilderStructure().buildStructure(ssbn); 
	}
	
	/**
	 * Runs commands so that the generated SSBN are pruned (i.e. nodes that are independent to
	 * query nodes are not included in SSBN).
	 * {@link #getPruneStructure()} is used in order to prune.
	 * @param ssbn
	 * @see #getPruneStructure()
	 */
	protected void pruneStruture(SSBN ssbn){
		getPruneStructure().pruneStructure(ssbn); 
	}
	
	/**
	 * Builds an instance of {@link ProbabilisticNetwork} and
	 * initializes the probability distribution (e.g. initializes CPT).
	 * Subclasses may extend this method in order to personalize how the actual BN will be built.
	 * For example, if there are too many parents per node and CPT is occupying too much space,
	 * this method may be overwritten so that the CPT is initialized in another format, or
	 * to write the BN into a file, instead of initializing an instance of {@link ProbabilisticNetwork}.
	 * @param ssbn
	 * @throws MEBNException
	 * @throws SSBNNodeGeneralException
	 * @see #getBuildLocalDistribution()
	 */
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
	 * @return the isLogEnabled
	 */
	public boolean isLogEnabled() {
		return isLogEnabled;
	}


	/**
	 * @param isLogEnabled the isLogEnabled to set
	 */
	public void setLogEnabled(boolean isLogEnabled) {
		this.isLogEnabled = isLogEnabled;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGenerator#getLastIterationCount()
	 */
	public int getLastIterationCount() {
		// not a iterative method, so it is just 1 iteration
		return 1;
	}


	/**
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}


	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
}
