package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.resources.ResourcesSSBNAlgorithmLog;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.util.Debug;

/**
 * Build the Grand BN from the query nodes and the finding nodes
 * 
 * (Based on SSBN Algorithm proposed in
 *  "MEBN - A language for first-order bayesian knowledges bases"
 *  by Laskey, in Artificial Intelligence #172, 2008)  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class BuilderStructureImpl implements IBuilderStructure{

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
	IdentationLevel level1; 
	IdentationLevel level2; 
	IdentationLevel level3; 
	IdentationLevel level4; 
	IdentationLevel level5; 
	IdentationLevel level6;

	
	private BuilderStructureImpl(){
	}
	
	public static BuilderStructureImpl newInstance(){
		return new BuilderStructureImpl(); 
	}
	
	public static BuilderStructureImpl newInstance(KnowledgeBase _kb){
		BuilderStructureImpl builder = new BuilderStructureImpl(); 
		builder.kb = _kb;
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
		this.kb = ssbn.getKnowledgeBase(); 
		
		try {
			this.maxNumberNodes = Long.valueOf(
					ssbn.getParameters().getParameterValue(LaskeyAlgorithmParameters.NUMBER_NODES_LIMIT)); 
		} catch (Exception e) {
			// ignore (use default value)
			Debug.println(getClass(), "Failed to extract parameter NUMBER_NODES_LIMIT", e);
		}
		
		// reset current quantity of generated nodes.
		// CAUTION: failing to reset this value will cause the algorithm to stop
		// working after some quantity of executions,
		// because numberNodes is an attribute instead of local variable.
		numberNodes = 0;
		
		for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
			notFinishedNodeList.add(node);
			numberNodes++; 
		}		
		
		//Evaluate all not finished nodes
		
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
			
			//Update list of not finished nodes 
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
	}

	/*
	 * Evaluate a node, creating the necessary nodes and edges to mark it how 	
	 * finished. 
	 * 
	 * Two cases: 
	 * - Generated by a input node (contains arguments for the input MFrag)
	 * - Generated by a finding/query node
	 */
	private void evaluateUnfinishedRV(SimpleSSBNNode node) throws ImplementationRestrictionException, 
	                  SSBNNodeGeneralException{
		
		IdentationLevel thisLevel2; 
		level2 = new IdentationLevel(level1); 
		thisLevel2 = level2; 
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		
		if (logManager != null) {
			logManager.printText(level2, false, "Evaluate unfinished node" + ": " + node);
		}
		
		//Note: In this implementation is not checked if already have an equal MFragInstance. 
		
		//Build the MFragInstance related to the node
		MFragInstance mFragInstance = MFragInstance.getInstance(node.getResidentNode().getMFrag()); 
		
		//Add the arguments values to the MFrag Instance. 
		for(int i = 0; i < node.getOvArray().length; i++){
			try {
				mFragInstance.addOVValue(node.getOvArray()[i], node.getEntityArray()[i].getInstanceName());
			} catch (MFragContextFailException e) {
				//this is a bug... the context can't fail here. 
				throw new SSBNNodeGeneralException(e.getMessage()); 
			} 
		}
		
		node.setMFragInstance(mFragInstance); 
		
		//Evaluate the mFragInstances and create its nodes.
		evaluateMFragInstance(mFragInstance, node); 
		
		node.setFinished(true);
		
		if (logManager != null) {
			logManager.printText(thisLevel2, true, "Unfinished node = " + node + " setted true");
			logManager.skipLine(); 
		}
	}
	
	/**
	 * Evaluate the MFrag Instance:
	 * 1) Evaluate context nodes
	 * 2) Create parents of the original ssbnNode (based on entities found)
	 * 
	 * @param ssbnNode
	 * @param mFragInstance
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private void evaluateMFragInstance(MFragInstance mFragInstance, SimpleSSBNNode ssbnNode) 
	      throws ImplementationRestrictionException, SSBNNodeGeneralException{
		
		IMFragContextNodeAvaliator mFragContextNodeAvaliator =    
				new MFragContextNodeAvaliator(ssbn); 
		
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level2, true, "Evaluate MFragInstance" + mFragInstance); 
		}
		
		// 1) Test if the MFragInstance already was evaluated
		if(mFragInstance.isEvaluated()){
			return; //Be careful here if you change the algorithm for search for
			        //mFragInstances equal to the that are evaluated... In this case
			        //maybe a new evaluation of the MFrag will be necessary 
			        //because the enter node should be other. 
		}
		
	    // 2) Evaluate MFragInstance context
		try {
			level3 = new IdentationLevel(level2); 
			if (logManager != null) {
				logManager.printText(level3, true, "Evaluate MFrag Context Nodes");
			}
			
			mFragContextNodeAvaliator.evaluateMFragContextNodes(mFragInstance);
			
		} catch (ImplementationRestrictionException e) {
			//Stop the evaluation when the context nodes fail. 
			//TODO warning... the evaluation continue, using the default distribution
//			throw e; 
//		} catch (OVInstanceFaultException e) {
//			throw new ImplementationRestrictionException(e.getMessage()); 
			mFragInstance.setUseDefaultDistribution(true); 
			
		} catch (MFragContextFailException e) {
			//TODO warning... the evaluation continue, using the default distribution
//			throw new SSBNNodeGeneralException(e.getMessage()); 
			mFragInstance.setUseDefaultDistribution(true); 
		
		} 
		
		// 3) Create the nodes of the MFragInstance
		level3 = new IdentationLevel(level2); 
		if (logManager != null) {
			logManager.printText(level3, true, "Create the nodes of MFrag ");
		}
		evaluateNodeInMFragInstance(mFragInstance, ssbnNode);	
		
	}	
	
	/**
	 * Evaluate a node in this MFrag: Verify if the node is a finding and create its parents. 
	 * 
	 * This procedure is recursive (inside the MFrag Instance): the parents of the 
	 * parents will be created too, except for the parents originated from input nodes 
	 * (that will be evaluated only in other MFrag).  
	 * <b>
	 * 
	 * Pre-Requisites: <b>
	 * Context node already evaluated. <b>
	 * 
	 * Pos-Requisites: <b>
	 * Set the mFragInstance of the node<b>
	 * Set the node how finished<b>
	 * Set the mFragInstance how finished (all nodes parents necessary are generated)<b>
	 * 
	 * Notes: <b>
	 * - The evaluation of the recursivity should be in this method. 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private void evaluateNodeInMFragInstance(MFragInstance mFragInstance, SimpleSSBNNode node) 
	       throws ImplementationRestrictionException, SSBNNodeGeneralException{
		
		level4 = new IdentationLevel(level3); 
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level4, false, "Create parents of node " + node);
		}
		
		node.setMFragInstance(mFragInstance); 
		
		//------------------------------------------------------------------------------
		// Step 1: Verify if node is a finding 
		//------------------------------------------------------------------------------
		
		IResidentNode resident = node.getResidentNode(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		for(int i = 0; i < node.getOvArray().length; i++){
			argumentList.add(OVInstance.getInstance(node.getOvArray()[i], node.getEntityArray()[i])); 
		}
		
		StateLink exactValue = kb.searchFinding(
				node.getResidentNode(), argumentList);
		
		/*
		 * Note: how the algorithm is bottom-up, is necessary continue the evaluation 
		 * to up even if the node is setted how a finding, because, above it can have
		 * a query or a node that influence the query. 
		 */
		if(exactValue!= null){
			//The node is a finding... 
			node.setState(exactValue.getState());
			ssbn.addFindingToTheFindingList(node); 
			
			if (logManager != null) {
				logManager.printText(level4, false, " -> Node " + node + 
					" set as a finding. Exact Value = " + exactValue);
			}
		}
		
		
		//------------------------------------------------------------------------------
		// Step 2: Create the parents of node from the resident nodes
		//------------------------------------------------------------------------------
		
		//If the context node of the MFrag don't are evaluated, the creation of 
		//the parents isn't possible
		if(mFragInstance.isUseDefaultDistribution()){
			
			if (logManager != null) {
				logManager.printText(level4, false, 
						" -> Node can't be evaluated: MFrag using default distribution");
			}
			return; 
		
		}
		
		OrdinaryVariable[] ovFilledArray = node.getOvArray(); 
		ILiteralEntityInstance[] entityFilledArray = node.getEntityArray(); 
	
		if (logManager != null) {
			logManager.printText(level4, false, " 1) Evaluate the resident node parents");
		}
		
		for(ResidentNode residentNodeParent: resident.getResidentNodeFatherList()){
			
			List<SimpleSSBNNode> createdNodesList = createParents(node, 
					ovFilledArray, entityFilledArray, residentNodeParent);
			
			if (logManager != null) {
				logManager.printText(level4, false, 
						"Resident parents generates from the resident node " + 
					residentNodeParent);
			}
			
			int count = 0;
			
			//Evaluate each new node created into the MFrag Instance
			for(SimpleSSBNNode newNode: createdNodesList){
				if (logManager != null) {
					logManager.printText(level4, false, "Evaluate " + count + " - "+ newNode);
				}
				
				count= count + 1 ; 
				evaluateNodeInMFragInstance(mFragInstance, newNode); 
			}
			
		}
		
		//------------------------------------------------------------------------------
		// Step 3: Create the parents of node from the input nodes
		//------------------------------------------------------------------------------
		if (logManager != null) {
			logManager.printText(level4, false, " 2) Evaluate the input node parents");
		}
		
		for(InputNode inputNodeParent: resident.getParentInputNodesList()){
			
			//RECURSIVITY 
			
			if(inputNodeParent.getResidentNodePointer().getResidentNode().equals(resident) ){
				
				if (logManager != null) {
					logManager.printText(level4, false, " Recursivity treatment: " + resident);
				}
				
				// check if there is any argument tagged as "ordereable"
				boolean isOrdered = false;
				for (Argument arg : inputNodeParent.getArgumentList()) {
					if (arg.getOVariable().getValueType().hasOrder()) {
						isOrdered = true;
						break;
					}
				}
				
				if (isOrdered) {
					// assume that one of the arguments has linear order property (i.e. automatically assume T0 < T1 < T2 < ...)
					SimpleSSBNNode newNode = createRecursiveParents(node, 
							ovFilledArray, 
							entityFilledArray, 
							inputNodeParent);
					
					if(newNode != null){
						evaluateNodeInMFragInstance(mFragInstance, newNode); 
					}
				} else {
					// treat it the same way of any other node. 
					// Users must use context nodes and findings in order to 
					// guarantee acyclic network
					createParents(node, ovFilledArray,  entityFilledArray, inputNodeParent);
				}
				
			}else{
				createParents(node, ovFilledArray, entityFilledArray, inputNodeParent);	
			}
			
		}
		
		node.setFinished(true); 
		if (logManager != null) {
			logManager.printText(level4, false, "Node " + node + " setted finished");
		}

		mFragInstance.setEvaluated(true); 
	}

	/**
	 * Create the SSBNNodes parents for a node (version when the parent is a 
	 * resident node). 
	 * 
	 * @param node                    Node for witch the parents will be evaluated
	 * @param ovFilledArray           Ordinary variables that already are instantiated
	 * @param entityFilledArray       Entity values for the ordinary variables of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, ILiteralEntityInstance[] entityFilledArray,
			ResidentNode residentNodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		//fix a unknown Bug (mystic)... 
		if(residentNodeParent.equals(node.getResidentNode())){
			return new ArrayList<SimpleSSBNNode>(); 
		}
		
		JacketNode nodeParent = new JacketNode(residentNodeParent); 
		
		return createParents(node, ovFilledArray, entityFilledArray, nodeParent); 
	}

	/**
	 * Create the SSBNNodes parents for a node (version when the parent is a input node)
	 * Note: this method don't treat recursivity. 
	 * 
	 * @param node                    Node for witch the parents will be evaluated
	 * @param ovFilledArray           Ordinary variables that already are instantiated
	 * @param entityFilledArray       Entity values for the ordinary variables of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, ILiteralEntityInstance[] entityFilledArray,
			InputNode inputNodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {

		
		JacketNode nodeParent = new JacketNode(inputNodeParent); 
		
		return createParents(node, ovFilledArray, entityFilledArray, nodeParent); 
	}	
	
	/**
	 * Create the SSBNNodes parents for a node 
	 * 
	 * @param node                    Node for witch the parents will be evaluated
	 * @param ovFilledArray           Ordinary variables that already are instantiated
	 * @param entityFilledArray       Entity values for the ordinary variables of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */	
	private  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, 
			ILiteralEntityInstance[] entityFilledArray,
			JacketNode nodeParent) 
					throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
//		if(internalDebug){
//			Debug.println("[In] CreateParents");
//			Debug.println("[Arg] Node=" + node.getResidentNode().getName());
//			Debug.print("[Arg] ovFilledArray = [");
//			for(int i = 0; i < ovFilledArray.length; i++){
//				Debug.print(ovFilledArray[i] + " ");
//			}
//			Debug.println("]");
//			Debug.print("[Arg] entityFilledArray= [");
//			for(int i = 0; i < entityFilledArray.length; i++){
//				Debug.print(entityFilledArray[i].getInstanceName() + " ");
//			}
//			Debug.println("]");
//			Debug.println("[Arg] inputNodeParent= " + nodeParent.getResidentNode());
//		}
		
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
		
		if(ovFaultForNewNodeList.size() > 0){
			
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
			
//			ssbn.getLogManager().printText(level5, false,"Possible combinations for ov fault: ");
//			for(String[] combination: possibleCombinationsForOvFaultList){
//				String combinationS = ""; 
//				for(String entity: combination){
//					combinationS+= " " + entity;
//				}
//				ssbn.getLogManager().printText(level5, false, " >" + combinationS); 
//			} 
			
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
		 *  (in the above example, E is created before X, which is c
		 *  reated before Y)
		 */
		SimpleSSBNNode childOfChain = node;	
		
		//Create the new node... We pass by each combination for the ov fault list. 
		//Note that if we don't have any ov fault, we should have one stub element
		//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
		Iterator<String[]> iterator = possibleCombinationsForOvFaultList.iterator();
		
		while (iterator.hasNext()) {	
			// using iterator instead of for(T obj : iterable), because there 
			//is a place where we need to call iterator.hasNext() inside the loop
			String[] possibleCombination = iterator.next();
			
//			Debug.println("Combination=" + possibleCombination);
			
			SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(
					nodeParent.getResidentNode()); 
			
			OrdinaryVariable[] ovArrayMFrag = 
					new OrdinaryVariable[nodeParent.getResidentNode().getOrdinaryVariableList().size()]; 
			
			//1. Add the ovInstances of the children that the father also have 
			for(int i = 0; i < node.getOvArray().length; i++){
				
				//For a input node IX1 that references the resident node RX1, we 
				//should recover the ordinary variable of the RX1's HomeMFrag that
				//correspond to the OV of the IX1's MFrag and then set it to the 
				//SSBNNode. 
				OrdinaryVariable correspondentOV = 
					nodeParent.getCorrespondentOrdinaryVariable(node.getOvArray()[i]);
				
				if(correspondentOV != null){
					newNode.setEntityForOv(
							correspondentOV,               //ov in the resident MFrag
							node.getEntityArray()[i]); 
				}
				
				//TODO Set the ordinary variables for the Input MFrag 
				//But... we also can have a resident node here... (it's a recursion in this case)
				
				if(nodeParent.isInputNode()){
					int index = nodeParent.getOrdinaryVariableIndex(node.getOvArray()[i]); 
					if(index!= -1){
						ovArrayMFrag[index] = node.getOvArray()[i];
					}
				}
				
			}
			
			//2. Create the new OVInstances for the combination
			for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
				
				OrdinaryVariable correspondentOV = 
					nodeParent.getCorrespondentOrdinaryVariable(ovFaultForNewNodeList.get(index));
				
				newNode.setEntityForOv(
						correspondentOV,
						LiteralEntityInstance.getInstance(
								possibleCombination[index],
								ovFaultForNewNodeList.get(index).getValueType())); 
				
				if(nodeParent.isInputNode()){
					int index2 = nodeParent.getOrdinaryVariableIndex(ovFaultForNewNodeList.get(index)); 
					ovArrayMFrag[index2] = ovFaultForNewNodeList.get(index); 
				}				
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
				// do not forget to add new node into the list of nodes created in this method
				ssbnCreatedList.add(childOfChain);	
			}
			
			newNode = addNodeToMFragInstance(childOfChain, newNode); 
			
			//Set reference for the ordinary variables list in input MFrag 
			if(nodeParent.isInputNode()){
				newNode.setOVArrayForMFrag(node.getMFragInstance().getMFragOrigin(), ovArrayMFrag);
			}
			
			ssbnCreatedList.add(newNode); 
		}
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		
		//Add the context node parent if it exists
		if( contextParentsCount > 0 ){
			
			for(SimpleContextNodeFatherSSBNNode contextParent : contextNodeFatherList){
				if (logManager != null) {
					logManager.printText(level5, false, 
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
	 * If a node is going to have too many parents, and the LPD of node 
	 * can be represented as a chain like the following network: <br/>
	 * 
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
	 * @param childOfChain : this node is node E or X in the above example 
	 *        (i.e. node to be a child of a node in a chain).
	 * @param newParent :  this is the new parent node being currently created. 
	 *        In the above example, if childOfChain is E, then this value should 
	 *        be D. If childOfChain is X, then this value shall be C. If childOfChain, 
	 *        then this value shall be B (or alternatively, A). 
	 *        It assumes that the array of OVs and its instances are already filled
	 * @return node X in the above example if childOfChain is the node E of 
	 *        above example. If childOfChain is the node X, then this method 
	 *        returns the node Y of the example.
	 * @throws SSBNNodeGeneralException when the quantity of nodes created by 
	 *        this object exceeds {@link #maxNumberNodes}
	 */
	protected SimpleSSBNNode createNodeInAChain(SimpleSSBNNode childOfChain, 
			SimpleSSBNNode newParent) throws SSBNNodeGeneralException {
		
		SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(childOfChain.getResidentNode()); 
//		SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(newParent.getResidentNode());
		
		// add one level to the childOfChain (if childOfChain was not a "virtual" 
		// node in a chain, it is going to set to 1).
		newNode.setStepsForChainNodeToReachMainNode(
				childOfChain.getStepsForChainNodeToReachMainNode() + 1);
		
		// copy some default values from original node
		newNode.setMFragInstance(childOfChain.getMFragInstance());
		newNode.setEvaluatedForHomeMFrag(childOfChain.isEvaluatedForHomeMFrag());
		newNode.setDefaultDistribution(childOfChain.isDefaultDistribution());
		
		// mark it as finished, so that it is not re-evaluated 
		// (we do not want chain nodes to have parents other than the ones inherent from the main node)
		newNode.setFinished(true);
		
		// fill OVs with instances
		
		for(int i = 0; i < childOfChain.getOvArray().length; i++){
			// supposedly, new node contains the same OVs of childOfChain
			if ( (childOfChain.getOvArray()[i] != null) && (childOfChain.getEntityArray().length > i) ) {
				newNode.setEntityForOv(childOfChain.getOvArray()[i], childOfChain.getEntityArray()[i]); 
			} else {
				Debug.println(getClass(), "Could not extract OV and its instances of node "
			    + childOfChain + " for index " + i);
			}
		}
//		for(int i = 0; i < newParent.getOvArray().length; i++){
//			// supposedly, new node contains the same OVs of  childOfChain
//			newNode.setEntityForOv(newParent.getOvArray()[i], newParent.getEntityArray()[i]); 
//		}
		
		// check limit of nodes
		if(numberNodes > maxNumberNodes){
			throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
		}
		
		newNode = addNodeToMFragInstance(childOfChain, newNode); 
		
		return newNode;
	}

	//The format of the MFrag in the recursion is: 
	//      INPUTNODE_A(NODE_A)(args) -> NODE_A (args)
	//That follows: 
	//       RESIDENTCHILD = NODE_A
	// (RESIDENTCHILD is the reference of the input INPUTNODE_A
	
	private  SimpleSSBNNode createRecursiveParents(SimpleSSBNNode oldNode,
			OrdinaryVariable[] ovFilledArray, ILiteralEntityInstance[] entityFilledArray,
			InputNode inputNodeParent) 
	             throws ImplementationRestrictionException, 
	                    SSBNNodeGeneralException {
		
		ResidentNode residentNode = oldNode.getResidentNode(); 
		
		//1) FIND THE ENTITY ORDEREABLE 
		List<OrdinaryVariable> ovOrdereableList = residentNode.getOrdinaryVariablesOrdereables();
		
		if(ovOrdereableList.size() > 1){
			throw new ImplementationRestrictionException(
					ImplementationRestrictionException.MORE_THAN_ONE_ORDEREABLE_VARIABLE);
		}
		
		if(ovOrdereableList.size() < 1){
			throw new ImplementationRestrictionException(
					ImplementationRestrictionException.RV_NOT_RECURSIVE);
		}
		
		OrdinaryVariable ovOrdereable = ovOrdereableList.get(0); //Have only one element... 
		
		//2) FIND THE PREVIOUS ELEMENT. 
		ObjectEntity objectEntityOrdereable = residentNode.getMFrag().
                        getMultiEntityBayesianNetwork().getObjectEntityContainer().
                        getObjectEntityByType(ovOrdereable.getValueType()); 
	
		ILiteralEntityInstance ovOrdereableActualValue = oldNode.getEntityForOv(ovOrdereable); 
		OVInstance ovInstanceOrdereable = OVInstance.getInstance(ovOrdereable, ovOrdereableActualValue); 
		
		if(ovInstanceOrdereable == null){
			throw new SSBNNodeGeneralException();
		}
		
		String nameEntity = ovInstanceOrdereable.getEntity().getInstanceName(); 
		
		ObjectEntityInstanceOrdereable objectEntityInstanceOrdereable = 
			(ObjectEntityInstanceOrdereable)objectEntityOrdereable.getInstanceByName(nameEntity);
		
		if(objectEntityInstanceOrdereable == null){
			throw new SSBNNodeGeneralException();
		}
		
		ObjectEntityInstanceOrdereable prev = objectEntityInstanceOrdereable.getPrev(); 

		if(prev != null){

			level5 = new IdentationLevel(level4); 
			if (ssbn.getLogManager() != null) {
				ssbn.getLogManager().printText(level5, false, "Previous node = " + prev + " (" + objectEntityInstanceOrdereable + ")");
			}
			
			ILiteralEntityInstance ovOrdereablePreviusValue = 
				LiteralEntityInstance.getInstance(prev.getName(), ovOrdereable.getValueType());

			//3) Build the father 

			// Note: the parent and children have to have the same arguments,  
			// with exception of the recursive ordinary variable.  
			// (This keep the compatibility with the considerations of the previous
			// implemented algorithm).  
			
			SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(residentNode); 
			for(int i = 0; i < oldNode.getOvArray().length; i++){
				
				// The new node is a build by the resident node, and not for 
				// the input node. Because of this, its ordinary variable is the 
				// ov of the resident node. The input node is only a trick for 
				// model the recursion in the generative MFrag. 
				
				if(oldNode.getOvArray()[i].equals(ovOrdereable)){
					newNode.setEntityForOv(oldNode.getOvArray()[i], ovOrdereablePreviusValue); 
				}else{
					newNode.setEntityForOv(oldNode.getOvArray()[i], oldNode.getEntityArray()[i]); 
				}
				
			}

			if(numberNodes > maxNumberNodes){
				//TODO define exception
				throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
			}
			newNode = addNodeToMFragInstance(oldNode, newNode);

			return newNode; 
		
		}else{
			return null; 
		}
		
	}

	/**
	 * Add the father node to the MFragInstance of the child node and add the link
	 * between the two nodes. Verify also if the father (newNode) already exists 
	 * in the SSBN, take the already exist object if positive (return it). 
	 * 
	 * @param child  The node already present in mFragInstance 
	 * @param parent The new Node to be added. 
	 * @return       the new node
	 */
	private SimpleSSBNNode addNodeToMFragInstance(SimpleSSBNNode child,
			SimpleSSBNNode parent) {
		
		SimpleSSBNNode testNode = parent; 
		
		parent = ssbn.addSSBNNodeIfItDontAdded(testNode);
		
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
		child.getMFragInstance().addEdge(new SimpleEdge(parent, child));
		
		return parent;
	
	}	
	
//	/**
//	 * @param maxQuantityOfParentsInAChain : 
//	 * If a node is going to have too many parents, and the LPD of node can be represented as a chain like the following network: <br/>
//	 * Suppose E is a boolean OR: <br/>
//	 * Parents: A B C D	<br/>
//	 * Child: E                  <br/>
//	 *                      <br/>
//	 * It may be represented as:<br/>
//	 *  <br/>
//	 * A B <br/>
//	 * | / <br/>
//	 * Y C <br/>
//	 * | / <br/>
//	 * X D <br/>
//	 * | / <br/>
//	 * E <br/>
//	 *              <br/><br/>
//	 * Note: X and Y have the same LPD of E (they are also boolean OR) <br/>
//	 * <br/>                                    
//	 * This value indicates the maximum quantity of parents for nodes
//	 * E, X and Y in the above example.
//	 * @see SimpleSSBNNode#isNodeInAVirualChain()
//	 * @see #createNodeInAChain(SimpleSSBNNode)
//	 */
//	public void setMaxQuantityOfParentsInAChain(int maxQuantityOfParentsInAChain) {
//		this.maxQuantityOfParentsInAChain = maxQuantityOfParentsInAChain;
//	}
//
//	/**
//	 * @return 
//	 * If a node is going to have too many parents, and the LPD of node can be represented as a chain like the following network: <br/>
//	 * Suppose E is a boolean OR: <br/>
//	 * Parents: A B C D	<br/>
//	 * Child: E                  <br/>
//	 *                      <br/>
//	 * It may be represented as:<br/>
//	 *  <br/>
//	 * A B <br/>
//	 * | / <br/>
//	 * Y C <br/>
//	 * | / <br/>
//	 * X D <br/>
//	 * | / <br/>
//	 * E <br/>
//	 *              <br/><br/>
//	 * Note: X and Y have the same LPD of E (they are also boolean OR) <br/>
//	 * <br/>                                    
//	 * This value indicates the maximum quantity of parents for nodes
//	 * E, X and Y in the above example.
//	 * @see SimpleSSBNNode#isNodeInAVirualChain()
//	 * @see #createNodeInAChain(SimpleSSBNNode)
//	 */
//	public int getMaxQuantityOfParentsInAChain() {
//		return maxQuantityOfParentsInAChain;
//	}

	/**
	 * This class is used to offer to the resident node and the input node the 
	 * same behavior for the method "createParents"
	 * 
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
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
		
		public int getOrdinaryVariableIndex(OrdinaryVariable ov){
			if(isResidentNode){
				return residentNode.getOrdinaryVariableIndex(ov); 
			}else{
				return inputNode.getResidentNodePointer().getOrdinaryVariableIndex(ov);
			}
		}
		
		public boolean isResidentNode() {
			return isResidentNode;
		}
		
		public boolean isInputNode(){
			return !isResidentNode; 
		}
		
	}
	
}



