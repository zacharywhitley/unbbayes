package unbbayes.prs.mebn.ssbn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.LogManager;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;

public class AlternativeSSBNGenerator extends AbstractSSBNGenerator  {

   private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private long recursiveCallLimit = 999999999999999999L;
	
	private long recursiveCallCount = 0;

	public static long stepCount = 0L;
	public static String queryName = "";
	public static LogManager logManager = new LogManager();
	
	public AlternativeSSBNGenerator(){
		super();  
	}
	
	/**
	 * The SSBN Node is generate in a process with tree partes:
	 * 1) Building of the network
	 * 2) Generate CPT's for the nodes of the created network
	 * 3) Set the finding nodes and propagate the evidences
	 */
	
	public ProbabilisticNetwork generateSSBN(Query query)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException {
		
		ssbnNodeList = new ArrayList<SSBNNode>(); 
		
		// some data extraction
		SSBNNode queryNode = query.getQueryNode();
		this.setKnowledgeBase(query.getKb()); 

		// Log manager initialization
		stepCount = 0L;
		queryName = queryNode.toString();
		logManager.clear();
		
		// initialization

		// call recursive
		this.recursiveCallCount = 0;
		
		SSBNNodeList ssbnNodeList = new SSBNNodeList(); 
		
		MFragInstance mFragInstanceRootNode = new MFragInstance(queryNode.getResident().getMFrag()); 
		SSBNNode root = this.generateRecursiveUp(queryNode, ssbnNodeList, 
				                               queryNode.getProbabilisticNetwork(), 
				                               mFragInstanceRootNode);
		
		this.generateRecursiveDown(queryNode, ssbnNodeList, queryNode.getProbabilisticNetwork()); 
		
		this.generateCPTForAllSSBNNodes(root); 
		
//		this.setFindingsAndPropagateEvidences(null, null); 
		
		logManager.appendln("\n");
		logManager.appendln("SSBN generation finished");
		
		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(root);
		
		try {
			logManager.writeToDisk("LogSSBN.log", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return queryNode.getProbabilisticNetwork();
	}

	
	
	/*-------------------------------------------------------------------------
	 * Private
	 *------------------------------------------------------------------------*/
	
	/**
	 * 
	 * 
	 * - If the node has a finding, create the SSBNNode, set it how finding and retorne it
	 * 
	 * 
	 * 
	 * Pre-requisites: 
	 * - The node current node has a OVInstance for all OrdinaryVariable argument
	 * 
	 */
	private SSBNNode generateRecursiveUp(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net, MFragInstance mFragInstance) throws SSBNNodeGeneralException, 
			                                 ImplementationRestrictionException, 
			                                 MEBNException {
		
		currentNode.setPermanent(true); //up always permanent...
		
		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}
		
		this.recursiveCallCount++;
		
		logManager.appendln("\n\n-------------EVALUATING NODE: " + currentNode.getName() + "--------------\n"); 
		

		
		
		
		//------------------------- STEP 1: search findings -------------------
		
		logManager.appendln(currentNode + ":A - Search findings");
        //check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = getKnowledgeBase().searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			seen.add(currentNode); 
			logManager.appendln("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		
		
		
		//------------------------- STEP 2: analyze context nodes. -------------
		
		// evaluates querynode's mfrag's context nodes 
		//(if not OK, sets MFrag Instance's flag to use default CPT)	
		
		logManager.appendln(currentNode + ":B - Analyse context nodes");
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 
		
	     
		boolean evaluateRelatedContextNodesResult = false;
		
		try {
			evaluateRelatedContextNodesResult = evaluateRelatedContextNodes(
					currentNode.getResident(), ovInstancesList, mFragInstance);
		} catch (OVInstanceFaultException e) {
			//pre-requisite.
			e.printStackTrace();
		}
		
		if(!evaluateRelatedContextNodesResult){
			logManager.appendln("Context Node fail for " + currentNode.getResident().getMFrag()); 
			currentNode.setUsingDefaultCPT(true);
			return currentNode; 
		}
		
		// Step where the node is created for the first time
		logManager.appendln("\n");
		logManager.appendln("Node " + currentNode + " created");
		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(currentNode);
		
		
		
		
       //------------------------- STEP 3: Add and evaluate resident nodes fathers -------------
		
		logManager.appendln(currentNode + "C:- Analyse resident nodes fathers");
		for (ResidentNode residentNode : currentNode.getResident().getResidentNodeFatherList()) {

			/*
			 * Analyze if it has one ov instance for each ordinary variable. If this 
			 * is not true, verify if it has some context node able to recover 
			 * the instances that match with the ordinary variable.  
			 */
            List<OrdinaryVariable> ovProblematicList = analyzeOVInstancesListCompletudeForOVList(
            		residentNode.getOrdinaryVariableList(), currentNode.getArguments()); 
            
    		if(!ovProblematicList.isEmpty()){
    			List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
    					residentNode.getMFrag(), currentNode, residentNode, 
    					ovProblematicList, ovInstancesList); 
    			
    		    for(SSBNNode ssbnnode: createdNodes){
    		    	generateRecursiveUp(ssbnnode, seen, net, mFragInstance);	
    		    	
    		    	if(!ssbnnode.isFinding()){
    		    		currentNode.addParent(ssbnnode, true);
    		    	}else{
    		    		currentNode.addParent(ssbnnode, false);
    		    	}
    		    }
    		}else{ //ovProblematicList.isEmpty
    			SSBNNode ssbnnode = null; 
    			
				List<OVInstance> arguments = fillArguments(currentNode.getArguments(), residentNode);
				SSBNNode testSSBNNodeDuplicated = getSSBNNodeIfItAlreadyExists(
						residentNode, arguments, ssbnNodeList); 
				
				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
				if(testSSBNNodeDuplicated == null){
					ssbnnode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode(), false);
					for(OVInstance ovInstance: arguments){
						ssbnnode.addArgument(ovInstance);
					}
					ssbnNodeList.add(ssbnnode);
	    			generateRecursiveUp(ssbnnode, seen, net, mFragInstance);	
				}else{
					ssbnnode = testSSBNNodeDuplicated;
				}
				
    			if(!ssbnnode.isFinding()){
    				currentNode.addParent(ssbnnode, true);
    			}else{
    				currentNode.addParent(ssbnnode, false); 
    			}
    		}
			
		}
		
		logManager.appendln(currentNode + " returned from the resident node parents' recursion"); 
		
		
		
		

		
	    //------------------------- STEP 4: Add and evaluate input nodes fathers -------------

		logManager.appendln(currentNode + "D:- Analyze input nodes fathers");
		for (InputNode inputNode : currentNode.getResident().getInputNodeFatherList()) {
			
			ResidentNode residentNodeTargetOfInput = inputNode.getResidentNodePointer().getResidentNode(); 

			logManager.appendln(currentNode.getName() + "Evaluate input " + residentNodeTargetOfInput.getName()); 
			
			
			//Step 0: ----------Evaluate recursion... Node1(x2) -> Node1(x1)------------
			
			if(currentNode.getResident() == residentNodeTargetOfInput){
				
				SSBNNodeJacket previousNode = getPreviousNode(currentNode, seen, net, 
						residentNodeTargetOfInput, inputNode);

				if(previousNode != null){
					
					//yes... exist recursion...
					previousNode.setArgumentsOfResidentMFrag();
					
					//TODO treatment of the MFragInstance
					MFragInstance mFragInputNode = new MFragInstance(
							previousNode.getSsbnNode().getResident().getMFrag()); 
					
					generateRecursiveUp(previousNode.getSsbnNode(), seen, net, mFragInputNode);
					
					previousNode.setArgumentsOfInputMFrag();

					/* it takes the parameters' names back to normal */
					if(!previousNode.getSsbnNode().isFinding()){
						currentNode.addParent(previousNode.getSsbnNode(), true);
					}else{
						currentNode.addParent(previousNode.getSsbnNode(), false);
					}
				}
				
				continue;			
			}
			
			
			//Step 1: evaluate context and search for findings
            List<OrdinaryVariable> ovProblematicList = 
            	analyzeOVInstancesListCompletudeForOVList(inputNode.getOrdinaryVariableList(), 
            			currentNode.getArguments()); 
            
    		if(!ovProblematicList.isEmpty()){
    			
    			List<SSBNNodeJacket> parentList = createSSBNNodesOfEntitiesSearchForInputNode(
    					currentNode, inputNode, ovProblematicList, currentNode.getArgumentsAsList()); 
    			
    		    for(SSBNNodeJacket ssbnNodeJacket: parentList){
    		    	
    		    	SSBNNode ssbnnode = ssbnNodeJacket.getSsbnNode();
    		    	ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
    		    	
    		    	logManager.appendln("Node Created: " + ssbnnode.toString());
					//TODO treatment of the MFragInstance
					MFragInstance mFragInputNode = new MFragInstance(
							ssbnnode.getResident().getMFrag()); 
    		    	generateRecursiveUp(ssbnnode, seen, net, mFragInputNode);	// algorithm's core
    		    	
    		    	ssbnNodeJacket.setArgumentsOfInputMFrag(); 
        			if(!ssbnnode.isFinding()){
        				currentNode.addParent(ssbnnode, true);
        			}else{
        				currentNode.addParent(ssbnnode, false);
        			}
       
    		    }
    		   
    		}else{ //ovProblematicList.isEmpty()
    			

				SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNodeTargetOfInput, new ProbabilisticNode(), false);
				SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode); 
				
				for(OVInstance ovInstance: currentNode.getArguments()){
					addArgumentToSSBNNodeOfInputNode(inputNode, ssbnNodeJacket, ovInstance);
				}
				
				ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
				
				ssbnNode = checkForDoubleSSBNNode(ssbnNode);
				
				ssbnNodeList.add(ssbnNode);
    			
    			boolean contextNodesOK = false;
				
    			try {
					contextNodesOK = evaluateRelatedContextNodes(inputNode, currentNode.getArgumentsAsList());
				} catch (OVInstanceFaultException e) {
					//ovProblemList is empty... this exception never will be catch.
					e.printStackTrace();
				} 
				
    			if(contextNodesOK){
        			//Step 2: context and findngs ok... do the work...	
					//TODO treatment of the MFragInstance
					MFragInstance mFragInputNode = new MFragInstance(
							ssbnNode.getResident().getMFrag()); 
    				this.generateRecursiveUp(ssbnNode, seen, net, mFragInputNode);	// algorithm's core	
    			}else{
    				logManager.append("Context Nodes False for " + ssbnNode + " (input node)"); 
    				ssbnNode.setUsingDefaultCPT(true); 
    			}
    			
				ssbnNodeJacket.setArgumentsOfInputMFrag(); 
				
				if(!ssbnNode.isFinding()){
					currentNode.addParent(ssbnNode, true);
				}else{
					currentNode.addParent(ssbnNode, false);
				}
    		}
		}
		
		return currentNode;
	}

	
	/**
	 * Generate the network for the below evidences. 
	 * 
	 * @param currentNode
	 * @param seen
	 * @param net
	 * 
	 * @return True if the path end with a Finding. Else otherside. 
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 * @throws MEBNException
	 */
	private boolean generateRecursiveDown(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net) throws SSBNNodeGeneralException, ImplementationRestrictionException, MEBNException {
		
		
		//some inicializations
		
		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}
		
		this.recursiveCallCount++; //Yes!!! This increment the recursive call... 
		
		logManager.appendln("\n\n-------------EVALUATING NODE BELOW: " + currentNode.getName() + "--------------\n"); 
		
		
		//------------------------- STEP 1: Search for findings -------------
		logManager.appendln(currentNode + ":A - Search findings");
        //check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = getKnowledgeBase().searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			seen.add(currentNode); 
			logManager.appendln("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			
			//If some recursive call return true, this node is permanent... and all the nodes above
			currentNode.setPermanent(true); 
			return true;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		
		
		
	    //------------------------- STEP 2: Resident nodes childs in the same MFrag -------------
		
		
		
		
		
		
		
		//------------------------- STEP 3: Search childs of inputs that have this node how target -------------
		
		
		
		
		
		return false; 
	}
	
	public void setFindingsAndPropagateEvidences(List<SSBNNode> findingsNodes, ProbabilisticNetwork ssbn){
		System.out.println("Findings setados e evidências propagadas");
	}

	public long getRecursiveCallCount() {
		return recursiveCallCount;
	}

	public void setRecursiveCallCount(long recursiveCallCount) {
		this.recursiveCallCount = recursiveCallCount;
	}
	

	
	public static void printAndSaveCurrentNetwork(SSBNNode queryNode) {
		stepCount++;
		PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(queryNode.getProbabilisticNetwork()); 
		SSBNDebugInformationUtil.printNetworkInformation(logManager, queryNode, stepCount, queryName); 
	}
	
	/**
	 * This is how many times a recursive call to the algorithm should be done
	 * @return the recursiveCallLimit
	 */
	public long getRecursiveCallLimit() {
		return recursiveCallLimit;
	}

	/**
	 * This is how many times a recursive call to the algorithm should be done
	 * @param recursiveCallLimit the recursiveCallLimit to set
	 */
	public void setRecursiveCallLimit(long recursiveCallLimit) {
		this.recursiveCallLimit = recursiveCallLimit;
	}
	

	

	
	
	
	
	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	private boolean evaluateRelatedContextNodes (ResidentNode residentNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance) throws OVInstanceFaultException{
		
		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (mFragInstance.isUsingDefaultDistribution()) {
			return false;
		};
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(getKnowledgeBase()); 
		
		
		//TODO Refazer!!! Esta abordagem nao permite a abordagem da transitividade
		//dos nos de contexto... 
		
		Collection<ContextNode> contextNodeList = residentNode.getMFrag().getContextByOVCombination(
				residentNode.getOrdinaryVariableList());
		
		for(ContextNode context: contextNodeList){
			logManager.appendln("Context Node: " + context.getLabel());
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				residentNode.getMFrag().setAsUsingDefaultCPT(true); 
				logManager.appendln("Result = FALSE. Use default distribution ");
				return false;  
			}else{
				logManager.appendln("Result = TRUE.");
			}		
		}
		
		return true; 
	}
	
}
