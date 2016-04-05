/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.resources.ResourcesSSBNAlgorithmLog;
import unbbayes.prs.mebn.ssbn.BuilderStructureImpl;
import unbbayes.prs.mebn.ssbn.ContextNodeEvaluationState;
import unbbayes.prs.mebn.ssbn.ContextNodeEvaluator;
import unbbayes.prs.mebn.ssbn.IBuilderStructure;
import unbbayes.prs.mebn.ssbn.ILiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.MFragInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleContextNodeFatherSSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNodeUtils;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.util.Debug;

/**
 * This class is used by {@link SSIDGenerator} in order to
 * build the SSBN structure with continuous nodes.
 * This is supposed to be an extension of {@link BuilderStructureImpl} (because most of the codes are the same).
 * However, it does not actually extend {@link BuilderStructureImpl}, because {@link BuilderStructureImpl}
 * offers only private constructors, hence it is impossible to extend.
 * This class also uses some techniques that makes this class more extensible than {@link BuilderStructureImpl}
 * @author Shou Matsumoto
 *
 */
public class SSIDBuilderStructure implements IBuilderStructure {

	/** How many parents a resident node with {@link ResidentNode#isToLimitQuantityOfParentsInstances()} will have. */
	public static final int MAX_NUM_PARENTS_IN_CHAIN = 2;

	private List<SimpleSSBNNode> notFinishedNodeList; 
	
	private KnowledgeBase kb; 
	
	private SSBN ssbn; 
	
	private boolean internalDebug = false; 
	
	private long maxNumberNodes = 10000000;  
	private long numberNodes = 0; 
	
	private ResourceBundle resourceLog = 
		unbbayes.util.ResourceController.newInstance().getBundle(ResourcesSSBNAlgorithmLog.class.getName()); 
	
	//Log
	private IdentationLevel level1; 
	private IdentationLevel level2; 
	private IdentationLevel level3; 
	private IdentationLevel level4; 
	private IdentationLevel level5; 
	private IdentationLevel level6; 
	
	/** @deprecated use {@link #newInstance(KnowledgeBase)} or {@link #newInstance()} instead */
	protected SSIDBuilderStructure(){
		
	}
	
	/** Default contructor method */
	public static IBuilderStructure newInstance(){
		return SSIDBuilderStructure.newInstance(null); 
	}
	
	/**constructor method initializing fields*/
	public static IBuilderStructure newInstance(KnowledgeBase kb){
		SSIDBuilderStructure builder = new SSIDBuilderStructure(); 
		builder.setKb(kb);
		return builder; 
	}
	
	/**
	 * 
	 * <p>
	 * <b>Pre-requisites</b> <br>
	 *     - All nodes of the SSBN are marked not finished. 
	 * <p>    
	 * <b>Pos-requisites</b> <br>
	 *     - All nodes of the SSBN are marked finished.     
	 *     
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	public void buildStructure(SSBN _ssbn) throws ImplementationRestrictionException, 
	                                              SSBNNodeGeneralException {
		
		notFinishedNodeList = new ArrayList<SimpleSSBNNode>();
		
		this.ssbn = _ssbn; 
		if (this.getKb() == null) {
			this.setKb(ssbn.getKnowledgeBase());
		}
		this.kb = ssbn.getKnowledgeBase(); 
		
		try {
			this.setMaxNumberNodes(Long.valueOf(ssbn.getParameters().getParameterValue(LaskeyAlgorithmParameters.NUMBER_NODES_LIMIT))); 
		} catch (Exception e) {
			Debug.println(getClass(), "Failed to obtain parameter NUMBER_NODES_LIMIT from algorithm", e);
		}
		
		// reset current quantity of generated nodes.
		// CAUTION: failing to reset this value will cause the algorithm to stop working after some quantity of executions,
		// because numberNodes is an attribute instead of local variable.
		numberNodes = 0;
		
		for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
			notFinishedNodeList.add(node);
			numberNodes++; 
		}		
		
		
		
		//Evaluate all the not finished nodes
		
		//Cases: 
		//Query and Findings nodes
		//Input nodes still don't evaluated 
		
		int iteration = 0; 
		
	    level1 =  new IdentationLevel(null); 
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		
		if (logManager != null) {
			logManager.skipLine(); 
		}
		
		while(!notFinishedNodeList.isEmpty()){
			
			if (logManager != null) {
				logManager.printText(level1, false, 
					resourceLog.getString("012_IterationNumber") + " "  + iteration); 
			}
			
			for(SimpleSSBNNode node: notFinishedNodeList){
		        try {
					evaluateUnfinishedRV(node);
				} catch (ImplementationRestrictionException e) {
					e.printStackTrace();
					throw e; 
				} catch (SSBNNodeGeneralException e) {
					e.printStackTrace();
					throw e; 
				} 
			}
			
			//update the not finished list
			notFinishedNodeList.clear();

			if (logManager != null) {
				logManager.skipLine(); 
				logManager.printText(level1, false, 
					resourceLog.getString("013_NotFinishedNodesList") + ": ");
			}
			
			for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
				if(!node.isFinished()){
					if (logManager != null) {
						logManager.printText(level1, false, " - " + node.toString());
					}
					notFinishedNodeList.add(node); 
				}
			}
			
			if (logManager != null) {
				logManager.skipLine();
			}
			iteration++; 
			
		}
	
		// convert ssbn nodes to probabilistic nodes
		ProbabilisticNetwork pn =  new ProbabilisticNetwork("SSBN");
		List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(ssbn.getSimpleSsbnNodeList(), pn);
		ssbn.setSsbnNodeList(listSSBNNode); 
		ssbn.setNetwork(pn); 
	}

	/**
	 * Evaluate a node, creating the necessary nodes and edges to mark it as 	
	 * finished. There are two cases to evaluate: 
	 * <br/>
	 * <br/>
	 * - Generated by an input node (contains arguments for the input MFrag)
	 * <br/>
	 * <br/>
	 * - Generated by a finding/query node
	 */
	protected void evaluateUnfinishedRV(SimpleSSBNNode node) throws ImplementationRestrictionException, 
	                  SSBNNodeGeneralException{
		
		IdentationLevel thisLevel2; 
		level2 = new IdentationLevel(level1); 
		thisLevel2 = level2; 
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level2, false, "Evaluate unfinished node" + ": " + node);
		}
		
		//Build the MFragInstance related to the node
		MFragInstance mFragInstance = MFragInstance.getInstance(node.getResidentNode().getMFrag()); 
		
		//Add the arguments
		for(int i = 0; i < node.getOvArray().length; i++){
			try {
				mFragInstance.addOVValue(node.getOvArray()[i], node.getEntityArray()[i].getInstanceName());
			} catch (MFragContextFailException e) {
				//this is a bug... the context can't fail here. 
				throw new SSBNNodeGeneralException(e.getMessage()); 
			} 
		}
		
		node.setMFragInstance(mFragInstance); 
		
		//Evaluate the mFragInstances and create the nodes of its.
		evaluateMFragInstance(mFragInstance, node); 
		
		node.setFinished(true);
		
		if (logManager != null) {
			logManager.printText(thisLevel2, true, "Unfinished node = " + node + " marked as true");
			logManager.skipLine(); 
		}
	}
	
	/**
	 * Evaluate the parents of a node in a MFrag. 
	 * 
	 * @param ssbnNode
	 * @param mFragInstance
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	protected void evaluateMFragInstance(MFragInstance mFragInstance, SimpleSSBNNode ssbnNode) 
	      throws ImplementationRestrictionException, SSBNNodeGeneralException{
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level2, true, "Evaluate MFragInstance" + mFragInstance); 
		}
		// 1) Test if the MFragInstance was already evaluated
		if(mFragInstance.isEvaluated()){
			return; 
		}
		
	    // 2) Evaluate MFragInstance context
		try {
			level3 = new IdentationLevel(level2); 
			if (logManager != null) {
				logManager.printText(level3, true, "Evaluate MFrag Context Nodes");
			}
			evaluateMFragContextNodes(mFragInstance);
			
		} catch (ImplementationRestrictionException e) {
			// ignore this, because we should be able to get some result (e.g. nodes with default distribution) even though evaluation of context nodes are failing
			Debug.println(getClass(), "Failed to evaluate context node, but we will still keep trying to generate the SSID", e);
		} catch (MFragContextFailException e) {
			// ignore this, because we should be able to get some result (e.g. nodes with default distribution) even though evaluation of context nodes are failing
			Debug.println(getClass(), "Failed to evaluate context node, but we will still keep trying to generate the SSID", e);
		} 
		
		// 3) Create the nodes of the MFragInstance
		level3 = new IdentationLevel(level2); 
		if (logManager != null) {
			logManager.printText(level3, true, "Create the nodes of MFrag ");
		}
		evaluateNodeInMFragInstance(mFragInstance, ssbnNode);	
		
	}	
	
	/**
	 * Evaluate a node in this MFrag. Evaluate is verify if the node is a finding
	 * and create its parents. This 
	 * procediment is recursive: the parents of the parents will be created too, 
	 * except for the parents originated from input nodes. 
	 * <br/>
	 * <br/>
	 * Pre-requisites of this method:
	 * <br/>
	 * The context node already are evaluated. <b>
	 * <br/>
	 * <br/>
	 * Effects of this method:
	 * <br/>
	 * <br/>
	 * set the mFragInstance of the node
	 * <br/>
	 * set the node as finished
	 * <br/>
	 * set the mFragInstance as finished (all parents were supposedly generated at the end of this method)
	 * <br/>
	 * <br/>
	 * <br/>
	 * Notes:
	 * <br/>
	 * - The evaluation of recursive nodes should be in this method. 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	protected void evaluateNodeInMFragInstance(MFragInstance mFragInstance, SimpleSSBNNode node) 
	       throws ImplementationRestrictionException, SSBNNodeGeneralException{
		
		level4 = new IdentationLevel(level3); 
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level4, false, "Create parents of node " + node);
		}
		
		node.setMFragInstance(mFragInstance); 
		
		//--- 1) Evaluate if the node is a finding. 
		
		IResidentNode resident = node.getResidentNode(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		for(int i = 0; i < node.getOvArray().length; i++){
			argumentList.add(OVInstance.getInstance(node.getOvArray()[i], node.getEntityArray()[i])); 
		}
		
		StateLink exactValue = kb.searchFinding(
				node.getResidentNode(), argumentList);
		
		/*
		 * Note: we still need to evaluate ancestors, because there may be nodes
		 * not d-separated with the query node there.
		 */
		if(exactValue!= null){
			//The node is a finding... 
			node.setState(exactValue.getState());
			ssbn.addFindingToFindingList(node); 
			
			if (logManager != null) {
				logManager.printText(level4, false, " -> Node " + node + 
					" set as a finding. Exact Value = " + exactValue);
			}
		}
		
		
		//---- 2) Create the parents of node from the resident nodes
		
		//If the context node of the MFrag was not evaluated, we cannot create parents anyway
		if(mFragInstance.isUsingDefaultDistribution()){
			if (logManager != null) {
				logManager.printText(level4, false, " -> Node can't be evaluated: mfrag will be using default distribution");
			}
			return; 
		}
		
		OrdinaryVariable[] ovFilledArray = node.getOvArray(); 
		ILiteralEntityInstance[] entityFilledArray = node.getEntityArray(); 
	
		if (logManager != null) {
			logManager.printText(level4, false, " 1) Evaluate the resident node parents");
		}
		for(ResidentNode residentNodeParent: resident.getResidentNodeFatherList()){
			
			List<SimpleSSBNNode> createdNodesList = createParents(node, ovFilledArray, entityFilledArray, residentNodeParent);
			
			if (logManager != null) {
				logManager.printText(level4, false, "Resident parents generated from the resident node: " +  residentNodeParent);
			}
			int count = 0; 
			for(SimpleSSBNNode newNode: createdNodesList){
				if (logManager != null) {
					logManager.printText(level4, false, "Evaluate " + count + " - "+ newNode);
				}
				count= count + 1 ; 
				evaluateNodeInMFragInstance(mFragInstance, newNode); 
			}
			
		}
		
		//---- 3) Create the parents of node from the input nodes
		if (logManager != null) {
			logManager.printText(level4, false, " 2) Evaluate the input node parents");
		}
		for(InputNode inputNodeParent: resident.getParentInputNodesList()){
			createParents(node, ovFilledArray, entityFilledArray, inputNodeParent);	
		}
		
		node.setFinished(true); 
		if (logManager != null) {
			logManager.printText(level4, false, "Node " + node + " marked as finished");
		}

		mFragInstance.setEvaluated(true); 
	}

	/**
	 * Create the SSBNNodes parents for a node (version when the parent is a 
	 * resident node). 
	 * 
	 * @param node                    Node for witch the parents will be evaluated
	 * @param ovFilledArray           Ordinary variables that already are instanciated
	 * @param entityFilledArray       Entity values for the ordinary variablese of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	protected List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, ILiteralEntityInstance[] entityFilledArray,
			ResidentNode residentNodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		if(residentNodeParent.equals(node.getResidentNode())){
			return new ArrayList<SimpleSSBNNode>(); 
		}
		
		JacketNode nodeParent = new JacketNode(residentNodeParent); 
		
		return createParents(node, ovFilledArray, entityFilledArray, nodeParent); 
	}

	/**
	 * Create the SSBNNodes parents for a node (version when the parent is a input node)
	 * 
	 * @param node                    Node for witch the parents will be evaluated
	 * @param ovFilledArray           Ordinary variables that already are instanciated
	 * @param entityFilledArray       Entity values for the ordinary variablese of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	protected  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, ILiteralEntityInstance[] entityFilledArray,
			InputNode inputNodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {

		
		JacketNode nodeParent = new JacketNode(inputNodeParent); 
		
		return createParents(node, ovFilledArray, entityFilledArray, nodeParent); 
	}	
	
	/**
	 * Create the SSBNNodes parents for a node 
	 * 
	 * @param node                    Node for witch the parents will be evaluated
	 * @param ovFilledArray           Ordinary variables that already are instanciated
	 * @param entityFilledArray       Entity values for the ordinary variablese of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */	
	protected  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, ILiteralEntityInstance[] entityFilledArray,
			JacketNode nodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		
		level5 = new IdentationLevel(level4); 
		
		
		List<SimpleSSBNNode> ssbnCreatedList = new ArrayList<SimpleSSBNNode>();
		
		int contextParentsCount = 0; 
		
		List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 
		
		//Fill the ovFault list. 
		for(OrdinaryVariable ov: nodeParent.getOrdinaryVariableList()){
			boolean find = false; 
			for(OrdinaryVariable ov2: ovFilledArray){
				if(ov2.equals(ov)){
					find = true; 
					break; 
				}
			}
			if(!find){
				ovFaultForNewNodeList.add(ov); 
			}
		}
		
		//Mount the combination of possible values for the ordinary variable fault
		List<String[]> possibleCombinationsForOvFaultList = new ArrayList<String[]>(); 
		
		List<SimpleContextNodeFatherSSBNNode> contextNodeFatherList = 
			new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
		
		if(ovFaultForNewNodeList.size()>0){
			
			for(OrdinaryVariable ov: ovFaultForNewNodeList){
				
				SimpleContextNodeFatherSSBNNode contextNodeFather = 
					node.getMFragInstance().getContextNodeFatherForOv(ov); 
				
				if(contextNodeFather != null){
					contextParentsCount++;
					contextNodeFatherList.add(contextNodeFather); 

					if(contextParentsCount > 1)	{
						//In this implementation, only one context node parent is accept 
						throw new ImplementationRestrictionException(
								ImplementationRestrictionException.MORE_THAN_ONE_CTXT_NODE_SEARCH); 
					}
				}else{
					//In this case the ov was solved using the IsA nodes. 
				}
			}
			
			possibleCombinationsForOvFaultList = node.getMFragInstance().
			        recoverCombinationsEntitiesPossibles(
					    ovFilledArray, 
				      	entityFilledArray, 
					    ovFaultForNewNodeList.toArray(
                    		 new OrdinaryVariable[ovFaultForNewNodeList.size()])); 
			
			
			//Treat the uncertainty reference
			
			
		}else{
			possibleCombinationsForOvFaultList.add(new String[0]); //A stub element (see for below)
		}
		
		/*
		 * if node is going to have too many parents, and the LPD of node can be represented as a chain
		 * E.g. suppose E is a boolean OR:
		 * 			A  B  C	D		   A  B
		 *           \ | / /      =>    \ |
		 *            \|//               Y    C	        Note: X and Y have the same LPD of E (they are also boolean OR)
		 *             E                   \ /
		 *                                  X   D
		 *                                   \ /
		 *                                    E
		 *                                    
		 *  Then childOfChain is going to store the last child in the chain.
		 *  We store the last child, because the child is always created before the parents in the chain 
		 *  (in the above example, E is created before X, which is created before Y)
		 */
		SimpleSSBNNode childOfChain = node;	
		
		//Create the new node... We pass by each combination for the ov fault list. 
		//Note that if we don't have any ov fault, we should have one stub element
		//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
		Iterator<String[]> iterator = possibleCombinationsForOvFaultList.iterator();
		while (iterator.hasNext()) {	
			// using iterator instead of for(T obj : iterable), because there is a place where we need to call iterator.hasNext() inside the loop
			String[] possibleCombination = iterator.next();
			
			
			SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(
					nodeParent.getResidentNode()); 
			
			//1. Add the ovInstances of the children that the parent also have 
			for(int i = 0; i < node.getOvArray().length; i++){
				
				//For a input node IX1 that references the resident node RX1, we 
				//should recover the ordinary variable of the RX1's HomeMFrag that
				//correspond to the OV of the IX1's MFrag and then set it to the 
				//SSBNNode. 
				OrdinaryVariable correspondentOV = 
					nodeParent.getCorrespondentOrdinaryVariable(node.getOvArray()[i]);
				
				if(correspondentOV != null){
					newNode.setEntityForOv(
							correspondentOV, 
							node.getEntityArray()[i]); 
				}
			}
			
			//2. Create the new OVInstances for the combination
			for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
				
				OrdinaryVariable correspondentOV = 
					nodeParent.getCorrespondentOrdinaryVariable(ovFaultForNewNodeList.get(index));
				
				newNode.setEntityForOv(
						correspondentOV, 
						LiteralEntityInstance.getInstance(possibleCombination[index], 
								ovFaultForNewNodeList.get(index).getValueType())); 
			}
			
			if(numberNodes > maxNumberNodes){
				//TODO define exception
				throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
			}
			
			// check whether we should create chains in order to limit the quantity of parents per node
			if (node.getResidentNode().isToLimitQuantityOfParentsInstances() 
					&& (childOfChain.getParentNodes().size() + 1 >= MAX_NUM_PARENTS_IN_CHAIN) 
					&& iterator.hasNext()) {
				// By adding this parent, we will reach MAX_NUM_PARENTS_IN_CHAIN, and there are more parents to add
				// hence, we must create another level in the chain
				// create new node in a chain, and set new node as a pivot for next chain
				childOfChain = this.createNodeInAChain(childOfChain, newNode);
				if (childOfChain != null) {
					// do not forget to add new node into the list of nodes created in this method
					ssbnCreatedList.add(childOfChain);	
				}
			}
			newNode = addNodeToMFragInstance(childOfChain, newNode); 
			if (newNode != null) {
				ssbnCreatedList.add(newNode); 
			}
		}
		
		//Add the context node parent if it exists
		if( contextParentsCount > 0 ){
			
			for(SimpleContextNodeFatherSSBNNode contextParent : contextNodeFatherList){
				if (ssbn.getLogManager() != null) {
					ssbn.getLogManager().printText(level5, false, 
							node + " is child of the contextNode " + 
							contextParent.getContextNode().toString());
				}
				
				node.addContextParent(contextParent); 
			}
		}
		
		return ssbnCreatedList; 
	}	

	/**
	 * 
	 * If a node is going to have too many parents, and the LPD of node can be represented as a chain like the following network: <br/>
	 * Suppose E is a boolean OR: <br/>
	 * Parents: A B C D	<br/>
	 * Child: E                  <br/>
	 *                      <br/>
	 * It may be represented as:<br/>
	 *  <br/>
	 * A B <br/>
	 * | / <br/>
	 * Y C <br/>
	 * | / <br/>
	 * X D <br/>
	 * | / <br/>
	 * E <br/>
	 *              <br/><br/>
	 * Note: X and Y have the same LPD of E (they are also boolean OR) <br/>
	 * <br/>                                    
	 * This value indicates the maximum quantity of parents for nodes
	 * E, X and Y in the above example.
	 * @param childOfChain : this node is node E or X in the above example (i.e. node to be a child of a node in a chain).
	 * @param newParent :  this is the new parent node being currently created. In the above example, if childOfChain is
	 * E, then this value should be D. If childOfChain is X, then this value shall be C. If childOfChain, then this value
	 * shall be B (or alternatively, A). It assumes that the array of OVs and its instances are already filled
	 * @return : node X in the above example if childOfChain is the node E of above example. If childOfChain
	 * is the node X, then this method returns the node Y of the example.
	 * @throws SSBNNodeGeneralException : when the quantity of nodes created by this object exceeds {@link #maxNumberNodes}
	 */
	protected SimpleSSBNNode createNodeInAChain(SimpleSSBNNode childOfChain, SimpleSSBNNode newParent) throws SSBNNodeGeneralException {
		SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(childOfChain.getResidentNode()); 
		
		// add one level to the childOfChain (if childOfChain was not a "virtual" node in a chain, it is going to set to 1).
		newNode.setStepsForChainNodeToReachMainNode(childOfChain.getStepsForChainNodeToReachMainNode() + 1);
		
		// copy some default values from original node
		newNode.setMFragInstance(childOfChain.getMFragInstance());
		newNode.setEvaluatedForHomeMFrag(childOfChain.isEvaluatedForHomeMFrag());
		newNode.setDefaultDistribution(childOfChain.isDefaultDistribution());
		
		// mark it as finished, so that it is not re-evaluated 
		// (we do not want chain nodes to have parents other than the ones inherent from the main node)
		newNode.setFinished(true);
		
		// fill OVs with instances
		
		for(int i = 0; i < childOfChain.getOvArray().length; i++){
			// supposedly, new node contains the same OVs of  childOfChain
			if ( (childOfChain.getOvArray()[i] != null) && (childOfChain.getEntityArray().length > i) ) {
				newNode.setEntityForOv(childOfChain.getOvArray()[i], childOfChain.getEntityArray()[i]); 
			} else {
				Debug.println(getClass(), "Could not extract OV and its instances of node " + childOfChain + " for index " + i);
			}
		}
		
		// check limit of nodes
		if(numberNodes > maxNumberNodes){
			throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
		}
		
		newNode = addNodeToMFragInstance(childOfChain, newNode); 
		
		return newNode;
	}


	/**
	 * Add the parent node to the MFragInstance of the child node and add the link
	 * between the two nodes. Verify also if the parent (newNode) already exists 
	 * in the ssbn, use the existing object if positive. 
	 * 
	 * @param child  The node already present in mFragInstance 
	 * @param parent The new Node to be added. 
	 * @return       the new node
	 */
	protected SimpleSSBNNode addNodeToMFragInstance(SimpleSSBNNode child,
			SimpleSSBNNode parent) {
		
		SimpleSSBNNode testNode = parent; 
		
		parent = ssbn.addSSBNNodeIfItDontAdded(testNode);
		
		if (parent.equals(child)) {
			ssbn.getLogManager().printText(level5, false, "Attempted to include " + parent + " as parent of " + child + ". Returning...");
			return null;
		}
		
		if(parent == testNode){
			numberNodes++; 
			if (ssbn.getLogManager() != null) {
				ssbn.getLogManager().printText(level5, false, "Created new node: " + parent);
			}
		}
		
		try {
			child.addParentNode(parent);
		} catch (InvalidParentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		child.getMFragInstance().addSSBNNode(parent);
//		child.getMFragInstance().addEdge(new SimpleEdge(parent, child));
		
		return parent;
	
	}	

	/**
	 * Evaluate the context nodes of a MFrag using the ordinary variables already instantiated. 
	 * <br/>
	 * <br/>
	 * 
	 * - Ordinary variables which were not instantiated yet will be instantiated. 
	 * <br/>
	 * - It should handle more than one reference to an ordinary variable 
	 * <br/>
	 * - It should handle reference uncertainty 
	 * <br/>
	 * - It should treat ordinary variables that with no instances as well.
	 * <br/>
	 * <br/>
	 * Cases: 
	 * <br/>
	 * - Trivial case
	 * <br/>
	 * - Simple Search (one entity for ov)
	 * <br/>
	 * - Compound Search (more than one entity)
	 * <br/>
	 * - Undefined Context (more than one possible result)
	 * <br/>
	 * 
	 * @param mfrag MFrag evaluated
	 * @param ovInstances Ordinary variables already instantiated. 
	 * @throws SSBNNodeGeneralException 
	 * @throws ImplementationRestrictionException 
	 * @throws OVInstanceFaultException 
	 * @throws MFragContextFailException 
	 */
	public MFragInstance evaluateMFragContextNodes(MFragInstance mFragInstance) 
	                   throws ImplementationRestrictionException, 
	                          MFragContextFailException{
		
		level4 = new IdentationLevel(level3); 
		
		//Consider that the tree with the know ordinary variables are already mounted. 
		//Consider that the only ordinary variables filled are the alread know OV
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level4, false, "1) Loop for evaluate context nodes.");
		}
		for(ContextNode contextNode: mFragInstance.getContextNodeList()){
			
			if (logManager != null) {
				logManager.printText(level4, false, "Context Node: " + contextNode);
			}
			
			//---> 1) Verify if the context node is soluted only with the know arguments. 
			List<OrdinaryVariable> ovInstancesFault = contextNode.getOVFaultForOVInstanceSet(ovInstances); 
			
			level5 = new IdentationLevel(level4); 
			if(ovInstancesFault.size() == 0){
				
				if (logManager != null) {
					logManager.printText(level5, false, "All ov's of the context node were settled");
				}
				boolean result = kb.evaluateContextNodeFormula(contextNode, ovInstances);
				if(result){
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_OK); 
					if (logManager != null) {
						logManager.printText(level5, false, "Node evaluated OK");
					}
					continue; 
				}else{
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL);
					mFragInstance.setUseDefaultDistribution(true); 
					if (logManager != null) {
						logManager.printText(level5, false, "Node evaluated FALSE");
					}
					continue; //The MFragInstance continue to be evaluated only
					          //to show to the user a network more complete. 
				}
			}else{
			
				if (logManager != null) {
					logManager.printText(level5, false, "Some ov's of the context node aren't filled");
					logManager.printText(level5, false, "Try 1: Use the search strategy");
				}
				
				
				//---> 2) Use the Entity Tree Strategy. 
				SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, ovInstances); 

				if(searchResult!= null){  

					if (logManager != null) {
						logManager.printText(level5, false, "Search Result: ");
						for(String[] result: searchResult.getValuesResultList()){
							String resultOv = " > "; 
							for(int i = 0; i < result.length; i++){
								resultOv += result[i] + " "; 
							}
							logManager.printText(level5, false, resultOv); 
						}
					}
					
					//Result valid results: Add the result to the tree of result.
					try {

						mFragInstance.addOVValuesCombination(
								searchResult.getOrdinaryVariableSequence(), 
								searchResult.getValuesResultList());

						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_OK); 
						
						if (logManager != null) {
							logManager.printText(level5, false, "Node evaluated OK");
						}
						
					} catch (MFragContextFailException e) {
						
						e.printStackTrace(); 
						
						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
						if (logManager != null) {
							logManager.printText(level5, false,"Context Node FAIL: use the default distribution");
						}
						
						//Here, the context node fail adding the values for a ordinary variable
						//fault. This fail impossibilite the evaluation of the rest of the MFragInstance, 
						//because, this node, used to recover the possible values, fail. 
						throw e; 
					}

					continue;

				}else{

					//---> 4) Use the uncertainty Strategy. 
					if (logManager != null) {
						logManager.printText(level5, false, 
							"Try 2: Use the uncertain reference strategy");
					}
					
					//Utilized only in the specific case z = RandomVariable(x), 
					//where z is the unknown variable. (Should have only one unknown variable)
					
					SimpleContextNodeFatherSSBNNode simpleContextNodeFather = null; 
					
					if(ovInstancesFault.size() == 1){
						try{
							simpleContextNodeFather = 
								evaluateUncertaintyReferenceCase(mFragInstance, 
										contextNode, ovInstancesFault.get(0)); 
							if(simpleContextNodeFather != null){
								continue; //OK!!! Good!!! Yes!!! 
							}
						}
						catch(ImplementationRestrictionException e){
							mFragInstance.setUseDefaultDistribution(true); 
							if (logManager != null) {
								logManager.printText(level5, false, "Fail: " + e.getMessage());
							}
						}
					}
					
					//--> 5) Nothing more to try... context fail
					if (logManager != null) {
						logManager.printText(level4, false,"Still ov fault... nothing more to do. " +
							"Use default distribution");
					}
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL); 
					mFragInstance.setUseDefaultDistribution(true);
					//TODO Maybe a warning... Not so drastic! 
					
				}
			}
			
		}
		
		
		if (logManager != null) {
			logManager.printText(level4, false,"2) Loop for evaluate the IsA nodes");
		}
		
		//Return a list of ordinary variables of the MFrag that don't are evaluated yet. 
		List<OrdinaryVariable> ovDontFoundYetList = new ArrayList<OrdinaryVariable>(); 
		for(OrdinaryVariable ov: mFragInstance.getMFragOrigin().getOrdinaryVariableList()){
			if(mFragInstance.getOVInstanceListForOrdinaryVariable(ov).size() == 0){
				ovDontFoundYetList.add(ov);
			}
		}
		
		//Evaluate this ordinary variables
		for(OrdinaryVariable ov: ovDontFoundYetList){
			//no context node about this ov... the value is unknown, we should 
			//consider all the possible values. Note the use of the Closed World Assumption. 
			if (logManager != null) {
				logManager.printText(level4, false,"Evaluate IsA for OV " + ov.getName());
			}
			
			List<String> possibleValues = kb.getEntityByType(ov.getValueType().getName()); 
			
			if (logManager != null) {
				for(String possibleValue: possibleValues){
					logManager.printText(level4, false, "  > " + possibleValue);
				}
			}
			
			String possibleValuesArray[] = possibleValues.toArray(new String[possibleValues.size()]); 
			List<String[]> entityValuesArray = new ArrayList<String[]>(); 
			
			for(String possibleValue: possibleValuesArray){
				entityValuesArray.add(new String[]{possibleValue}); 
			}
				                                                                
			OrdinaryVariable ovArray[] = new OrdinaryVariable[1];
			ovArray[0] = ov; 
			
			try {
				mFragInstance.addOVValuesCombination(ovArray, entityValuesArray);
			} catch (MFragContextFailException e) {
//				e.printStackTrace(); 
				Debug.println(this.getClass(), "", e);
				mFragInstance.setUseDefaultDistribution(true); 
			}
		}
		
		if (logManager != null) {
			List<String[]> possibleCombinationsList = mFragInstance.recoverAllCombinationsEntitiesPossibles(); 
			logManager.printBox3Bar(level4);
			logManager.printBox3(level4, 0, "Result of evalation of the context nodes (Possible combinations): ");
			for(String[] possibleCombination : possibleCombinationsList){
				String ordinaryVariableComb = " > ";
				for(String ordinaryVariableValue: possibleCombination){
					ordinaryVariableComb += " " + ordinaryVariableValue;
				}
				ordinaryVariableComb += " < ";
				logManager.printBox3(level4, 1, ordinaryVariableComb); 
			}
			logManager.printBox3Bar(level4); 
		}
		
		//Return mFragInstance with the ordinary variables filled. 
		return mFragInstance; 
	}
	
	/**
	 * Try evaluate the uncertainty reference for the context node and the ov fault. 
	 * Return null if don't have ordinary variables possible for the evaluation. 
	 * 
	 * Notes: <br> 
	 * - In this implementation only one context node should became a parent. <br> 
	 * 
	 * 
	 * @param mFragInstance
	 * @param contextNode
	 * @param ovFault
	 * 
	 * @throws ImplementationRestrictionException 
	 */
	public SimpleContextNodeFatherSSBNNode evaluateUncertaintyReferenceCase(MFragInstance mFragInstance, 
			ContextNode contextNode, OrdinaryVariable ovFault) throws ImplementationRestrictionException{
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb); 
		
		//1 Evaluate if the context node attend to restrictions and fill the ovinstancelist 
		if(!avaliator.testContextNodeFormatRestriction(contextNode)){
			throw new ImplementationRestrictionException(
					ImplementationRestrictionException.INVALID_CTXT_NODE_FORMULA); 
		}; 
		
		Collection<OrdinaryVariable> contextOrdinaryVariableList = contextNode.getVariableList(); 
		
		for(OrdinaryVariable ov: contextOrdinaryVariableList){
			if(!ov.equals(ovFault)){
				List<OVInstance> ovInstanceForOvList = mFragInstance.getOVInstanceListForOrdinaryVariable(ov); 
			    if(ovInstanceForOvList.size() > 1){
			    	throw new ImplementationRestrictionException(
							ImplementationRestrictionException.ONLY_ONE_OVINSTANCE_FOR_OV); 
			    }else{
			    	ovInstanceList.add(ovInstanceForOvList.get(0)); 
			    }
			}
		}
		
		//2 Recover alll the entites of the specifc type

		
		List<String> result = null;
		List<ILiteralEntityInstance> list =  avaliator.searchEntitiesForOrdinaryVariable(ovFault); 
		
		if((list == null)||(list.size()==0)){
			return null; 
		}else{
			result = new ArrayList<String>(); 
			for(ILiteralEntityInstance lei: list){
				result.add(lei.getInstanceName()); 
			}
		}
		
		//3 Analize what entities are possible at the tree and add the result 
		//at the MFragInstance
		
		try {
			//The new ov are at the tree... but, too are at the simple context node parent. 
			
			mFragInstance.addOVValueCombination(ovFault, result);
			mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_SEARCH); 
			
			SimpleContextNodeFatherSSBNNode contextParent = new SimpleContextNodeFatherSSBNNode(contextNode, ovFault); 
			contextParent.setPossibleValues(result); 
			
			mFragInstance.setContextNodeForOrdinaryVariable(ovFault, contextParent); 
			
			return contextParent; 
			
		} catch (MFragContextFailException e) {
			
			//This exception don't should be throw because we assume that don't 
			//have value for the ordinary variable at the list of OVInstances 
			//of the MFrag and for this, don't exists a way to exists a inconsistency
			//at the addOVValueCombination method.
			
			e.printStackTrace();
			
			throw new UnsupportedOperationException(e); 
		} 
	}
	
	/**
	 * This class is used to offer to the resident node and the input node the 
	 * same comportment for the method "createParents"
	 * 
	 * @author Laecio
	 */
	private class JacketNode{
		
		private ResidentNode residentNode; 
		private InputNode inputNode; 
		
		private boolean isResidentNode = false; 
		
		protected JacketNode(ResidentNode _residentNode){
			this.residentNode = _residentNode;
			this.isResidentNode = true; 
		}

		protected JacketNode(InputNode _inputNode){
			this.inputNode = _inputNode;
			this.isResidentNode = false; 		
		}

		public ResidentNode getResidentNode() {
			if(isResidentNode){
				return residentNode;
			}else{
				return inputNode.getResidentNodePointer().getResidentNode(); 
			}
		}
		
		public Collection<OrdinaryVariable> getOrdinaryVariableList() {
			if(isResidentNode){
				return residentNode.getOrdinaryVariableList(); 
			}else{
				return inputNode.getOrdinaryVariableList(); 
			}
		}
		
		public OrdinaryVariable getCorrespondentOrdinaryVariable(OrdinaryVariable ov){
			if(isResidentNode){
				return ov; 
			}else{
				return inputNode.getResidentNodePointer().getCorrespondentOrdinaryVariable(ov);
			}
		}
		
		public boolean isResidentNode() {
			return isResidentNode;
		}
		
		public boolean isInputNode(){
			return !isResidentNode; 
		}
		
	}

	/**
	 * @return the notFinishedNodeList
	 */
	public List<SimpleSSBNNode> getNotFinishedNodeList() {
		return notFinishedNodeList;
	}

	/**
	 * @param notFinishedNodeList the notFinishedNodeList to set
	 */
	public void setNotFinishedNodeList(List<SimpleSSBNNode> notFinishedNodeList) {
		this.notFinishedNodeList = notFinishedNodeList;
	}

	/**
	 * @return the kb
	 */
	public KnowledgeBase getKb() {
		return kb;
	}

	/**
	 * @param kb the kb to set
	 */
	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}

	/**
	 * @return the ssbn
	 */
	public SSBN getSsbn() {
		return ssbn;
	}

	/**
	 * @param ssbn the ssbn to set
	 */
	public void setSsbn(SSBN ssbn) {
		this.ssbn = ssbn;
	}

	/**
	 * @return the internalDebug
	 */
	public boolean isInternalDebug() {
		return internalDebug;
	}

	/**
	 * @param internalDebug the internalDebug to set
	 */
	public void setInternalDebug(boolean internalDebug) {
		this.internalDebug = internalDebug;
	}

	/**
	 * @return the maxNumberNodes
	 */
	public long getMaxNumberNodes() {
		return maxNumberNodes;
	}

	/**
	 * @param maxNumberNodes the maxNumberNodes to set
	 */
	public void setMaxNumberNodes(long maxNumberNodes) {
		this.maxNumberNodes = maxNumberNodes;
	}

	/**
	 * @return the numberNodes
	 */
	public long getNumberNodes() {
		return numberNodes;
	}

	/**
	 * @param numberNodes the numberNodes to set
	 */
	public void setNumberNodes(long numberNodes) {
		this.numberNodes = numberNodes;
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
	 * @return the level1
	 */
	public IdentationLevel getLevel1() {
		return level1;
	}

	/**
	 * @param level1 the level1 to set
	 */
	public void setLevel1(IdentationLevel level1) {
		this.level1 = level1;
	}

	/**
	 * @return the level2
	 */
	public IdentationLevel getLevel2() {
		return level2;
	}

	/**
	 * @param level2 the level2 to set
	 */
	public void setLevel2(IdentationLevel level2) {
		this.level2 = level2;
	}

	/**
	 * @return the level3
	 */
	public IdentationLevel getLevel3() {
		return level3;
	}

	/**
	 * @param level3 the level3 to set
	 */
	public void setLevel3(IdentationLevel level3) {
		this.level3 = level3;
	}

	/**
	 * @return the level4
	 */
	public IdentationLevel getLevel4() {
		return level4;
	}

	/**
	 * @param level4 the level4 to set
	 */
	public void setLevel4(IdentationLevel level4) {
		this.level4 = level4;
	}

	/**
	 * @return the level5
	 */
	public IdentationLevel getLevel5() {
		return level5;
	}

	/**
	 * @param level5 the level5 to set
	 */
	public void setLevel5(IdentationLevel level5) {
		this.level5 = level5;
	}

	/**
	 * @return the level6
	 */
	public IdentationLevel getLevel6() {
		return level6;
	}

	/**
	 * @param level6 the level6 to set
	 */
	public void setLevel6(IdentationLevel level6) {
		this.level6 = level6;
	}
	

}
