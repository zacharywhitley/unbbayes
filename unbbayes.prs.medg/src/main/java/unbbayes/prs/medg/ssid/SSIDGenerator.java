/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.NetworkController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.NetworkWindow;
import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.impl.UniformTableFunction;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.resources.ResourcesSSBNAlgorithmLog;
import unbbayes.prs.mebn.ssbn.BuilderStructureImpl;
import unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBN.State;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNodeUtils;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.prs.medg.MultiEntityUtilityNode;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This class extends LaskeySSBNGenerator in order to support continuous nodes.
 * This class reimplements most of the methods in the superclass in order
 * to adhere to template method design pattenr.
 * @author Shou Matsumoto
 *
 */
public class SSIDGenerator extends LaskeySSBNGenerator implements IMediatorAwareSSBNGenerator {
	
	private INetworkMediator mediator = null;
	
	/**
	 * @return the isToCompileFinalSSBN
	 */
	public boolean isToCompileFinalSSBN() {
		return isToCompileFinalSSBN;
	}

	/**
	 * @param isToCompileFinalSSBN the isToCompileFinalSSBN to set
	 */
	public void setToCompileFinalSSBN(boolean isToSetUpFindings) {
		this.isToCompileFinalSSBN = isToSetUpFindings;
	}

	private ResourceBundle resourceLog = 
		unbbayes.util.ResourceController.newInstance().getBundle(ResourcesSSBNAlgorithmLog.class.getName(),
				Locale.getDefault(), SSIDGenerator.class.getClassLoader()); 
	
	private Map<SSBN, INetworkMediator> ssbnToBNMediatorMap = new HashMap<SSBN, INetworkMediator>();

	private final LaskeyAlgorithmParameters parameters;

	private boolean isToCompileFinalSSBN = false;	// true;

	private boolean isToAddAllContinuousNodes = false;

	private IInferenceAlgorithm bnInferenceAlgorithm = new JunctionTreeAlgorithm();

	private boolean addFindings = true;;

	/**
	 * @param parameters
	 */
	public SSIDGenerator(LaskeyAlgorithmParameters parameters) { // NOPMD by Shou Matsumoto on 15/10/16 10:14
		super(parameters);
		this.parameters = parameters;
//		this.setBuilderStructure(SSIDBuilderStructure.newInstance());
		this.setBuilderStructure(BuilderStructureImpl.newInstance());
		this.setBuildLocalDistribution(SSIDBuilderLocalDistribution.getInstance());
		// disable debug info by default
		try {
			this.setLogEnabled(false);
			SSBNDebugInformationUtil.setEnabled(false);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method only allows access to the instance of {@link LaskeyAlgorithmParameters}
	 * passed as an argument of the constructor {@link #SSIDGenerator(LaskeyAlgorithmParameters)}. 
	 * @return the parameters
	 */
	public LaskeyAlgorithmParameters getParameters() {
		return parameters;
	}
	
	
	
	
	/**
	 * This method calls  {@link #compileNetwork(SSBN)}, {@link #initFindings(SSBN)} and then {@link #propagateFindings(SSBN)} 
	 * @param ssbn
	 * @throws Exception : this exception is inherent from the {@link ProbabilisticNetwork#updateEvidences()}
	 */
	public void compileSSBNAndPropagateFindings(SSBN ssbn) throws Exception {
		
		
		// use specified algorithm.
		IInferenceAlgorithm algorithm = getBNInferenceAlgorithm();
		
		// if we are calling this algorithm from GUI, set up the whole BN module instead of just the inference algorithm
		NetworkWindow bnModule = null;	// this is going to hold the BN module (NetworkWindow is a UnBBayesModule)
		if (getMediator() != null) {
			// Instantiate a new BN module
			bnModule = new NetworkWindow(ssbn.getNetwork());
			
			// extract controller from BN module
			NetworkController controller = bnModule.getController();
			try {
				algorithm.setMediator(controller);
			} catch (Exception e) {
				// algorithm should work without mediator, so let's ignore and try going on
				e.printStackTrace();
			}
			controller.setInferenceAlgorithm(algorithm);
			
			// Make sure the tree (JTree in the left side of compilation panel) is updated with the network changes, if there is any.
			controller.getScreen().getEvidenceTree().resetTree();
		}
		
		if (this.isToCompileFinalSSBN() ) {
			
			// this is the actual BN (i.e. SSBN) to compile
			Network bn = ssbn.getNetwork();
			
			// If it is a SingleEntityNetwork, make sure all variables are properly initialized
			if (bn instanceof SingleEntityNetwork) {
				SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) bn;
				singleEntityNetwork.resetNodesCopy();
				singleEntityNetwork.resetEvidences();
			}
			
			// actually compile network
			algorithm.setNetwork(bn);
			algorithm.run();
			
			if (algorithm.getNetwork() != null && (algorithm.getNetwork() instanceof Network)) {
				// algorithm may create new network. SSBN must be linked to the new network
				ssbn.setNetwork((Network) algorithm.getNetwork());	
			}
			
			// if we instantiated a BN module previously, then change it to the "compiled" view instead of "edit" view
			if (bnModule != null) {
				bnModule.changeToPNCompilationPane();
			}
		
			// fill findings and propagate again
			this.setUpFindings(ssbn, algorithm);
		}
		
		// popup network
		// TODO migrate this logic to showSSBN(SSBN ssbn), but in a thread-safe, state-insensitive way
		if (getMediator() != null) {
			if (getMediator() instanceof IMEBNMediator) {
				// the following code forces GUI to understand that it should not display an SSBN in its card layout panel
				// this is because the network is displayed as a popup		
				((IMEBNMediator)getMediator()).setToTurnToSSBNMode(false);
				((IMEBNMediator)getMediator()).setSpecificSituationBayesianNetwork(null);	// free any previous ssbn
			}
			// add/show popup
			getMediator().getScreen().getUnbbayesFrame().addWindow(bnModule);
			bnModule.setVisible(true);
			bnModule.updateUI();
			getMediator().getScreen().getUnbbayesFrame().repaint();
			bnModule.repaint();
		}
	}
	
	/**
	 * initializes and propagates findings.
	 * It basically translates MEBN findings to DMP findings.
	 * It is desired that the network is already compiled.
	 * CAUTION: the {@link ProbabilisticNetwork} linked to ssbn may not
	 * be the same of the {@link ProbabilisticNetwork} treated by the algorithm.
	 * @param ssbn	: the network generated by the MEBN portion
	 * @param algorithm	: the algorithm responsible for compiling the ssbn
	 * @throws SSBNNodeGeneralException 
	 */
	protected void setUpFindings(SSBN ssbn, IInferenceAlgorithm algorithm) throws SSBNNodeGeneralException {
		// initial assertion
		if (ssbn == null || algorithm == null) {
			throw new SSBNNodeGeneralException(new IllegalArgumentException(this.getClass() + ": SSBN == " + ssbn + "; algorithm == " + algorithm)); // NOPMD by Shou Matsumoto on 15/10/16 10:13
		}

		// TODO check if a reset is necessary
//		algorithm.reset();
		
		// findings of discrete nodes may be directly inserted to the network treated by the algorithm
		Graph g = algorithm.getNetwork();	// net to add findings
		if (g == null) {
			throw new SSBNNodeGeneralException(new IllegalStateException("No network is associated with the underlying ID compilation algorithm." 
					+ ssbn + " and " + algorithm + " should be compiled before this method."));
		}
		
		if (!(g instanceof ProbabilisticNetwork)) {
			throw new SSBNNodeGeneralException(new ClassCastException("The underlying ID compiler is associated with an instance of " + g.getClass().getName()+
					", which is not compatible with " + ProbabilisticNetwork.class.getName()));
		}

		ProbabilisticNetwork net = (ProbabilisticNetwork) g;
		boolean isToPropagate = false;	// this will become true if at least 1 valid finding was found
		if (ssbn.getFindingList() != null) {
			for(SimpleSSBNNode ssbnFindingNode: ssbn.getFindingList()){
				//Not all findings nodes are at the network. 
				if(ssbnFindingNode.getProbNode()!= null){ 
					// extract node and finding state from the network managed by the algorithm
					TreeVariable node = (TreeVariable)net.getNode(ssbnFindingNode.getProbNode().getName());
					String stateName = ssbnFindingNode.getState().getName(); 
					
					// add discrete findings directly to the network managed by the inference algorithm
					// unfortunately, the network managed by the algorithm and the one linked to the SSBN may not be the same (because algorithm may instantiate another network)
					boolean isStateInNode = false; 	// indicates if node contains the specified state (true if evidence is to a valid state)
					for(int i = 0; i < node.getStatesSize(); i++){
						// check if the name of the state in the SSID node is the same in the node in actual network managed by the algorithm
						if(node.getStateAt(i).equals(stateName)){
							node.addFinding(i);
							isStateInNode = true; 
							isToPropagate = true;
							break; 
						}
					}
					if(!isStateInNode){
						throw new SSBNNodeGeneralException(node + " has no state for finding: " + stateName); 
					}
					
				}
			}
		}
		ssbn.setState(State.WITH_FINDINGS); 
		
		
		// update evidences
		if (isToPropagate) {
			algorithm.propagate();
		}
		ssbn.setState(State.FINDINGS_PROPAGATED); 
	
	
	}

	/**
	 * Uses mediator to display the SSBN
	 * @param mediator : MEBNController
	 * @param  ssbn : the ssbn to show
	 */
	protected void showSSBN(SSBN ssbn) {
		// do nothing, because a window is popped up in compileSSBNAndPropagateFindings
	}

	/**
	 * This is a map which works as a cache for controllers responsible for
	 * managing the {@link SSBN#getNetwork()} associated with the SSBN generated
	 * by {@link #generateSSBN(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase)}.
	 * This is useful for tracking the SSBNs compiled by this generator and to
	 * control the GUI displaying such SSBNs.
	 * @return the ssbnToBNMediatorMap
	 */
	public Map<SSBN, INetworkMediator> getSSBNToBNMediatorMap() {
		return ssbnToBNMediatorMap;
	}

	/**
	 * This is a map which works as a cache for controllers responsible for
	 * managing the {@link SSBN#getNetwork()} associated with the SSBN generated
	 * by {@link #generateSSBN(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase)}.
	 * This is useful for tracking the SSBNs compiled by this generator and to
	 * control the GUI displaying such SSBNs.
	 * @param ssbnToBNMediatorMap the ssbnToBNMediatorMap to set
	 */
	public void setSSBNToBNMediatorMap(
			Map<SSBN, INetworkMediator> ssbnToBNMediatorMap) {
		this.ssbnToBNMediatorMap = ssbnToBNMediatorMap;
	}

	/**
	 * This method is a copy of {@link LaskeySSBNGenerator#generateSSBN(List, KnowledgeBase)}. However,
	 * it was extended because some of its internal methods (which we wanted to extend)
	 * was private (thus, impossible to extend).
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#generateSSBN(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase)
	 */
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase knowledgeBase)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {

		
		long time0 = System.currentTimeMillis(); 
		
		SSID ssid = null; 
		
		//Step 1: instantiate SSID, queries and findings.
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION))){
			ssid = initialization(queryList, knowledgeBase); 
			ssid.setParameters(parameters); 
		}
		
		
		ISSBNLogManager logManager = ssid.getLogManager();
		
		//Step 2: build the network from queries and findings
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_BUILDER))){
			
			if (logManager != null) {
				logManager.skipLine(); 
				logManager.printText(null, false, resourceLog.getString("004_Step2_BuildingGrandBN")); 
				logManager.skipLine(); 
			}
			
			getBuilderStructure().buildStructure(ssid); 
			
			if (logManager != null) {
				logManager.printText(null, false, resourceLog.getString("010_StepFinished"));
				logManager.skipLine(); 
				logManager.printBox2Bar(); 
				logManager.printBox2("List of nodes: "); 
				
				printSimpleSSBNNodeList(ssid); 
				
				logManager.printBox2Bar(); 
				logManager.skipLine(); 
				logManager.printSectionSeparation(); 
			}
			
		}
		
		//Step 3: run pruners
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_PRUNE))){
			
			if (logManager != null) {
				logManager.skipLine(); 
				ssid.getLogManager().printText(null, false, resourceLog.getString("005_Step3_PruneGrandBN")); 
				ssid.getLogManager().skipLine(); 
			}
			
			getPruneStructure().pruneStructure(ssid); 
			
			if (logManager != null) {
				logManager.printText(null, false, resourceLog.getString("010_StepFinished")); 
				logManager.skipLine(); 
				logManager.printBox2Bar(); 
				logManager.printBox2("List of nodes: "); 
				
				printSimpleSSBNNodeList(ssid); 
				
				logManager.printBox2Bar(); 
				logManager.skipLine(); 
				logManager.printSectionSeparation(); 
			}
		}
		
		//Step 4: translate SimpleSSBNNode to SSBNNode, and then compile LPDs
		if(Boolean.valueOf(parameters.getParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION))){
			
			if (logManager != null) {
				logManager.skipLine();
				logManager.printText(null, false, resourceLog.getString("006_Step4_BuildCPT")); 
				logManager.skipLine(); 
			}
			
			getBuildLocalDistribution().buildLocalDistribution(ssid);
			
			if (logManager != null) {
				logManager.printText(null, false, resourceLog.getString("010_StepFinished")); 
				logManager.skipLine(); 
				logManager.printSectionSeparation();
			}
		} else {
			// by default, we will set to uniform distribution
			
			// extract the network
			Network net =  ssid.getNetwork();
			if (net == null) {
				// convert ssid to probabilistic network if it was not done yet
				net =  new ProbabilisticNetwork("SSBN");
				List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(ssid.getSimpleSsbnNodeList(), (ProbabilisticNetwork) net);
				ssid.setSsbnNodeList(listSSBNNode); 
				ssid.setNetwork(net); 
			}
			
			// this table function will be used to make the cpts uniform
			UniformTableFunction uniformDistributionGenerator = new UniformTableFunction();
			for (Node node : net.getNodes()) {
				if (node instanceof ProbabilisticNode) {	// we only need to make probabilistic nodes uniform
					try {
						ProbabilisticNode probNode = (ProbabilisticNode) node;
						
						// TODO the following barbarity only happens when probabilistic node is generated by unbbayes.prs.mebn.ssbn.ContextFatherSSBNNode, so needs to be fixed there
						// check if cpt of current node contains itself (cpt needs to contain itself --in index 0-- or else its inconsistent)
						if (probNode.getProbabilityFunction().getVariableIndex(probNode) < 0) {	// just to avoid duplicate entry
							probNode.getProbabilityFunction().addVariable(probNode);
						}
						
						uniformDistributionGenerator.applyFunction((ProbabilisticTable) probNode.getProbabilityFunction());
					} catch (Exception e) {
						Debug.println(SimpleSSBNNodeUtils.class, "Failed to make the distribution uniform for node " + node, e);
					}
				}
			}
		}
		
		// adjust position of nodes
		try {
			PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(ssid.getProbabilisticNetwork());
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		
		if (logManager != null) {
			long time1 = System.currentTimeMillis(); 
			long deltaTime = time1 - time0; 
			
			SSBNDebugInformationUtil.printAndSaveCurrentNetwork(ssid); 
			logManager.printBox1Bar(); 
			logManager.printBox1(resourceLog.getString("007_ExecutionSucces")); 
			logManager.printBox1(resourceLog.getString("009_Time") + ": " + deltaTime + " ms"); 
			logManager.printBox1Bar(); 
			logManager.skipLine(); 
		}
		
		this.cleanUpSSBN(ssid);
		
		if (ssid.getNetwork().getNodeCount() != ssid.getNetwork().getNodeIndexes().keySet().size()) {
			// inconsistency on the quantity of indexed nodes and actual nodes
			// force synchronization
			ssid.getNetwork().getNodeIndexes().clear();
			for (int i = 0; i < ssid.getNetwork().getNodes().size(); i++) {
				ssid.getNetwork().getNodeIndexes().put(ssid.getNetwork().getNodes().get(i).getName(), i);
			}
		}
		
		try {
			this.reorderDecisionNodes(ssid);
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		try {
			this.compileSSBNAndPropagateFindings(ssid);
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		// show on display
		this.showSSBN(ssid);
		
		return ssid;
	}
	
	/**
	 * This method may reorder decision nodes or create new arcs between decision nodes if the underlying ID algorithm
	 * requires such ordering
	 * @param ssid
	 */
	public void reorderDecisionNodes(SSID ssid) {
		
		
		// reorder internally by entity
		
		// reorder by multi-entity decision nodes
		
	}

	/**
	 * This method is the same as {@link LaskeySSBNGenerator#cleanUpSSBN(SSBN)}.
	 * However, this is kept protected instead of private, so that extensions
	 * can be made.
	 * @param ssbn
	 */
	protected void cleanUpSSBN(SSBN ssbn){
		ssbn.getSimpleSsbnNodeList().clear();
		for (SSBNNode node : ssbn.getSsbnNodeList()) {
			node.clearArgumentsForMFrag();
		}
	}
	
	/**
	 * This method does the same as {@link LaskeySSBNGenerator#initialization(List<Query>, KnowledgeBase)}.
	 * However, this is protected instead of private, so that subclasses can extend only this portion of code.
	 * @param queryList
	 * @param knowledgeBase
	 * @return
	 */
	protected SSID initialization(List<Query> queryList, KnowledgeBase knowledgeBase){
		
		SSID ssid = new SSID(); 
		if (!isLogEnabled()) {
			ssid.setLogManager(null);
		}
		
//		MultiEntityBayesianNetwork mebn = null; 

		//log
		ISSBNLogManager logManager = ssid.getLogManager();
		if (logManager != null) {
			logManager.printBox1Bar();
			logManager.printBox1(getResourceForHybridAlgorithm().getString("001_Title")); 
			logManager.printBox1("Queries:"); 
			for(int i = 0; i < queryList.size(); i++){
				logManager.printBox1("    (" + i + ") " + queryList.get(i).toString()); 
			}
			logManager.printBox1(getResourceForHybridAlgorithm().getString("002_Algorithm") + ": " + 
					getResourceForHybridAlgorithm().getString("002_001_LaskeyAlgorithm")); 
			logManager.printBox1Bar(); 
			
			logManager.skipLine(); 
			logManager.printText(null, false, getResourceForHybridAlgorithm().getString("003_Step1_Initialization")); 
			logManager.skipLine(); 
		}
		
		ssid.setKnowledgeBase(knowledgeBase); 
		
		//We assume that all the queries is referent to the same MEBN
		MultiEntityBayesianNetwork mebn = queryList.get(0).getMebn(); 
		
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
			ssid.addSSBNNodeIfItDontAdded(ssbnNode);
			ssid.addQueryToQueryList(query); 
			
			if (logManager != null) {
				logManager.printText(in1, false, " - " + ssbnNode); 
			}
			                                                                    
		}
		
		if (logManager != null) {
			logManager.skipLine(); 
		}
		
		//Add findings to the list of nodes
		
		if (isToAddFindings()) {
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
						ssbnNode = ssid.addSSBNNodeIfItDontAdded(ssbnNode); 
						
						ssbnNode.setState(finding.getState()); 
						ssbnNode.setFinished(false); 
						
						ssid.addFindingToFindingList(ssbnNode); 
					}
				}
			}
			
		}
		
		// add utility nodes to the list of initial nodes
		for(MFrag mfrag: mebn.getMFragList()){
			// we are creating one utility node per each possible combination of arguments in a continuous resident node
			for (Node node : mfrag.getNodes()) {
				if (node instanceof MultiEntityUtilityNode) {
					
					MultiEntityUtilityNode medgUtilityNode = (MultiEntityUtilityNode) node;
					if (medgUtilityNode.getOrdinaryVariableList() == null 
							|| medgUtilityNode.getOrdinaryVariableList().isEmpty()) {
						continue;	// go to next node
					}
					
					// prepare all possible combinations of arguments. The ovs are synchronized by index
					boolean hasAtLeastOneCombination = true;	// true if all Lists in combinator were filled
					List<List<LiteralEntityInstance>> combinator = new ArrayList<List<LiteralEntityInstance>>();
					for (OrdinaryVariable ov : medgUtilityNode.getOrdinaryVariableList()) {
						List<LiteralEntityInstance> contentOfCombinator = new ArrayList<LiteralEntityInstance>();
						// get all values for ov
						List<String> ovValues = knowledgeBase.getEntityByType(ov.getValueType().getName());
						if (ovValues.isEmpty()) {
							// this iterator cannot be filled, because ov has no possible values
							hasAtLeastOneCombination = false;
							break;
						}
						// add all possible values for ov
						for (String ovValue : ovValues) {
							contentOfCombinator.add(LiteralEntityInstance.getInstance(ovValue, ov.getValueType()));
						}
						combinator.add(contentOfCombinator);
					}
					
					// do not add node if there is some ov with no possible value (that means, no combination can be created)
					if (!hasAtLeastOneCombination) {
						continue;	// go to next continuous node
					}
					
					// list of indexes of combinator
					List<Integer> indexes = new ArrayList<Integer>(combinator.size());
					for (int i = 0; i < combinator.size(); i++) {
						// Note that at this point, each list in combinator has at least 1 element, because hasAtLeastOneCombination == true (so, 0 is a valid index)
						indexes.add(0);
					}
					
					/*
					 * iterate on combinator using indexes. 
					 * E.g.:	combinator = {{a,b},{1,2}}; 
					 * 			then indexes must iterate like:
					 * 			{0,0}	(means (a,1));
					 * 			{1,0}	(means (b,1));
					 * 			{0,1}	(means (a,2));
					 * 			{1,1}	(means (b,2));
					 * 	
					 */
					while (hasAtLeastOneCombination) {
						
						// create a continuous ssbn node for each possible combination of combinator
						SimpleSSBNNode ssbnNode = SimpleSSBNNode.getInstance(medgUtilityNode); 
						ssbnNode.setFinished(false); 
						
						// fill arguments of ssbnNode
						for (int i = 0; i < medgUtilityNode.getOrdinaryVariableList().size(); i++) {
							ssbnNode.setEntityForOv(
									medgUtilityNode.getOrdinaryVariableList().get(i), 
									combinator.get(i).get(indexes.get(i))
									);
						}
						
						if (logManager != null) {
							logManager.printText(in1, false, " - " + ssbnNode);
						}
						
						ssid.addSSBNNodeIfItDontAdded(ssbnNode);
						
						List<OVInstance> arguments = new ArrayList<OVInstance>();
						for (Argument residentArgument : ssbnNode.getResidentNode().getArgumentList()) {
							arguments.add(OVInstance.getInstance(residentArgument.getOVariable(), ssbnNode.getEntityForOv(residentArgument.getOVariable())));
						}
						Query query = new Query(ssbnNode.getResidentNode(), arguments);
						query.setSSBNNode(ssbnNode);	// force the simpleSSBNNode to be the same instance
						ssid.addQueryToQueryList(query);
						
						// update indexes and check condition to end loop (it ends when all possible combinations were visited)
						for (int i = 0; i < combinator.size(); i++) {	
							// OBS. combinator, indexes and continuousResidentNode.getOrdinaryVariableList() have same size and are synchronized.
							Integer newIndex = indexes.get(i) + 1;	// obtain current step + 1
							// reset index if there is no mode element in this list
							if (newIndex >= combinator.get(i).size()) {
								// but if this is the last list, there is no more possible combination
								if (i >= combinator.size() - 1) {
									// there is no more combination
									hasAtLeastOneCombination = false;
									break;
								} else {
									newIndex = 0;	// reset
								}
							} 
							indexes.set(i, newIndex);	// update step 
							if (newIndex > 0) {
								// if we did not reset this index, we do not need to update the following indexes
								break;
							}
						}
						
					}	// while only ends when combinator was completely iterated
					
					
				}	// if node is a continuous resident node
			}	// for each node in mfrag
		}	// for each mfrag
	
		
		if (logManager != null) {
			logManager.skipLine();
			logManager.printText(null, false, getResourceForHybridAlgorithm().getString("010_StepFinished")); 
			logManager.skipLine(); 
			logManager.printSectionSeparation(); 
		}
		
		return ssid; 
		
	}
	

	/**
	 * This method is called in {@link #generateSSBN(List, KnowledgeBase)} in order to 
	 * create a log the ssbn using its related log manager. The content of the log
	 * is going to be the information of all nodes in the ssbn
	 * @param ssbn
	 */
	protected void printSimpleSSBNNodeList(SSBN ssbn) {
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

	/**
	 * Sets the resource file for the hybrid SSBN algorithm. Caution: it does not change the
	 * resource file of routines in superclasses.
	 * @return the resourceLog
	 */
	public ResourceBundle getResourceForHybridAlgorithm() {
		return resourceLog;
	}

	/**
	 * Sets the resource file for the hybrid SSBN algorithm. Caution: it does not change the
	 * resource file of routines in superclasses.
	 * @param resourceLog the resourceLog to set
	 */
	public void setResourceForHybridAlgorithm(ResourceBundle resourceLog) {
		this.resourceLog = resourceLog;
	}

	/**
	 * If this is true, {@link #initialization(List, KnowledgeBase)} will add all possible continuous nodes
	 * to SSBN, no matter if they are barren or d-separated nodes.
	 * @param isToAddAllContinuousNodes the isToAddAllContinuousNodes to set
	 */
	public void setToAddAllContinuousNodes(boolean isToAddAllContinuousNodes) {
		this.isToAddAllContinuousNodes = isToAddAllContinuousNodes;
	}

	/**
	 * If this is true, {@link #initialization(List, KnowledgeBase)} will add all possible continuous nodes
	 * to SSBN, no matter if they are barren or d-separated nodes.
	 * @return the isToAddAllContinuousNodes
	 */
	public boolean isToAddAllContinuousNodes() {
		return isToAddAllContinuousNodes;
	}

	/**
	 * @param bnInferenceAlgorithm the bnInferenceAlgorithm to set
	 */
	public void setBNInferenceAlgorithm(IInferenceAlgorithm bnInferenceAlgorithm) {
		this.bnInferenceAlgorithm = bnInferenceAlgorithm;
	}

	/**
	 * @return the bnInferenceAlgorithm
	 */
	public IInferenceAlgorithm getBNInferenceAlgorithm() {
		return bnInferenceAlgorithm;
	}

	/**
	 * @return the addFindings : if true, then {@link #initialization(List, KnowledgeBase)} will consider findings.
	 * If false, they will ignore findings.
	 */
	public boolean isToAddFindings() {
		return addFindings;
	}

	/**
	 * @param addFindings the addFindings to set :  if true, then {@link #initialization(List, KnowledgeBase)} will consider findings.
	 * If false, they will ignore findings.
	 */
	public void setToAddFindings(boolean addFindings) {
		this.addFindings = addFindings;
	}

	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		return this.mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}
	

}
