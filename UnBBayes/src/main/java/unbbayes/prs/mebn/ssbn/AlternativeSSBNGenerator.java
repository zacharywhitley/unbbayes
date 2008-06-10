package unbbayes.prs.mebn.ssbn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode.EvaluationState;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.util.Debug;

public class AlternativeSSBNGenerator extends AbstractSSBNGenerator  {

	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");

	private long recursiveCallLimit = 100L;

	private long recursiveCallCount = 0;

	public static long stepCount = 0L;
	public static String queryName = "";

	private List<SSBNNode> findingList; 
	
	public AlternativeSSBNGenerator(){
		super();  
		Debug.setDebug(true); 
		
		 findingList = new ArrayList<SSBNNode>(); 
	}

	/**
	 * Avaliação dos nós de contexto
	 * 
	 * 1) Verificar se todas as variaveis ordinarias estao OK. Se tiver avaliar
	 * 2) Caso não estejam, fazer busca. 
	 * 3) Caso a busca não retorne nada, tentar o caso do XOR. 
	 */
	
	/**
	 * The SSBN Node is generate in a process with tree partes:
	 * 1) Building of the network
	 * 2) Generate CPT's for the nodes of the created network
	 * 3) Set the finding nodes and propagate the evidences
	 */

	public SituationSpecificBayesianNetwork generateSSBN(Query query)
	throws SSBNNodeGeneralException,
	ImplementationRestrictionException, MEBNException {

		Debug.setDebug(true); 

		ssbnNodeList = new SSBNNodeList(); 
		ssbnNodesMap = new TreeMap<String, SSBNNode>(); 

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

//		MFragInstance mFragInstanceRootNode = new MFragInstance(queryNode.getResident().getMFrag()); 

		queryNode.setPermanent(true); 
		ssbnNodesMap.put(queryNode.getUniqueName(), queryNode); 
		
		this.generateRecursive(queryNode, ssbnNodeList, 
				queryNode.getProbabilisticNetwork());

		this.removeNotPermanentNodes(ssbnNodeList); 
		
		this.generateCPTForAllSSBNNodes(queryNode); 

		this.setFindingsAndPropagateEvidences(null, null); 

		logManager.appendln("\n");
		logManager.appendln("SSBN generation finished");

		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(queryNode);

		try {
			logManager.writeToDisk("LogSSBN.log", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		SituationSpecificBayesianNetwork ssbn = new SituationSpecificBayesianNetwork(
				queryNode.getProbabilisticNetwork(), findingList, new ArrayList()); 
		
		return ssbn;
	}


	private void generateRecursive(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net) throws SSBNNodeGeneralException, ImplementationRestrictionException, MEBNException {

		//generate below

		if(currentNode.getEvaluationState()== EvaluationState.EVALUATED_COMPLETE){
			return; 
		}

		if(currentNode.getEvaluationState() == EvaluationState.NOT_EVALUATED){
			if(currentNode.getEvaluationState() != EvaluationState.EVALUATING_BELOW){
				currentNode.setEvaluationState(EvaluationState.EVALUATING_BELOW); 
				logManager.appendln("\nGENERATE RECURSIVE DOWN: " + currentNode.getName() ); 
				generateRecursiveDown(currentNode, seen, net); 
				currentNode.setEvaluationState(EvaluationState.EVALUATED_BELOW); 
			}
		}

		//verify if OK for generate up
		if((currentNode.isPermanent())&&(!currentNode.isFinding())){
			if(currentNode.getEvaluationState() != EvaluationState.EVALUATING_UP){
				currentNode.setEvaluationState(EvaluationState.EVALUATING_UP); 
				logManager.appendln("\nGENERATE RECURSIVE UP: " + currentNode.getName() ); 
				generateRecursiveUp(currentNode, seen, net, null);
				currentNode.setEvaluationState(EvaluationState.EVALUATED_COMPLETE); 
			}
		}

	}

	/*-------------------------------------------------------------------------
	 * Private
	 *------------------------------------------------------------------------*/

	/**
	 * Generate recursive method for the parents of a node. 
	 * All the nodes created will be marked how permanent. 
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

		logManager.appendln("\n\n[U] Recursive call count = " + recursiveCallCount); 

		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}

		this.recursiveCallCount++;

		logManager.appendln("[U]" + currentNode + ":-------------EVALUATING NODE: " + currentNode.getName() + "--------------\n"); 





		//------------------------- STEP 1: search findings -------------------

		logManager.appendln("[U]" + currentNode + ":A - Search findings");
		//check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = getKnowledgeBase().searchFinding(currentNode.getResident(), currentNode.getArguments()); 

		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			
			if(seen.contains(currentNode)){
				seen.add(currentNode);
			}
			
			logManager.appendln("[U]" + currentNode + ":Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			
			findingList.add(currentNode); 
			
			return currentNode;
		}else{
			logManager.appendln("[U]" + currentNode + ": Search finding fail"); 
		}

		// if the program reached this line, the node doesn't have a finding
		if(seen.contains(currentNode)){
			seen.add(currentNode);
		}


		//------------------------- STEP 2: analyze context nodes. -------------

		// evaluates querynode's mfrag's context nodes 
		//(if not OK, sets MFrag Instance's flag to use default CPT)	

		logManager.appendln("[U]" + currentNode + ":B - Analyse context nodes");
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 

		boolean evaluateRelatedContextNodesResult = false;

		try {
			evaluateRelatedContextNodesResult = evaluateRelatedContextNodes(
					currentNode.getResident(), ovInstancesList, mFragInstance);
		} catch (OVInstanceFaultException e) {
			//pre-requisite.
			e.printStackTrace();
			logManager.appendln("[U]" + currentNode + "----- FATAL !!!!!!!\n\n"); 
			return null; 
		}

		if(!evaluateRelatedContextNodesResult){
			logManager.appendln("[U]" + currentNode + "Context Node fail for " + currentNode.getResident().getMFrag()); 
			currentNode.setUsingDefaultCPT(true);
			return currentNode; 
		}

		// Step where the node is created for the first time
		logManager.appendln("\n");
		logManager.appendln("Node " + currentNode + " created");
		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(currentNode);




		//------------------------- STEP 3: Add and evaluate resident nodes fathers -------------

		logManager.appendln("[U]" + currentNode +  "C:- Analyse resident nodes fathers");
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
						ovProblematicList, ovInstancesList, true); 

				for(SSBNNode ssbnnode: createdNodes){
					
					if(!currentNode.getParents().contains(ssbnnode)){
						generateRecursive(ssbnnode, seen, net);	
						
						if(!ssbnnode.isFinding()){
							currentNode.addParent(ssbnnode, true);
						}else{
							currentNode.addParent(ssbnnode, false);
						}
					}
					ssbnnode.setPermanent(true); 
				}
			}else{ //ovProblematicList.isEmpty

				List<OVInstance> arguments = fillArguments(currentNode.getArguments(), residentNode);
				
//				SSBNNode testSSBNNodeDuplicated = getSSBNNodeIfItAlreadyExists(
//						residentNode, arguments, ssbnNodeList); 

				SSBNNode ssbnNode = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(residentNode, arguments)); 
				
				logManager.appendln("Verify if the node already exists:" + 
						residentNode.getName() + currentNode.getArguments() + 
						" = " + (ssbnNode == null) ); 

				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
				if(ssbnNode == null){
					ssbnNode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode(), false);
					for(OVInstance ovInstance: arguments){
						ssbnNode.addArgument(ovInstance);
					}
					ssbnNodeList.add(ssbnNode);
					ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 
					generateRecursive(ssbnNode, seen, net);	
				}

				ssbnNode.setPermanent(true); 
				
				if(!currentNode.getParents().contains(ssbnNode)){
					currentNode.addParent(ssbnNode, true);
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

				previousNode.getSsbnNode().setPermanent(true); 

				if(previousNode != null){

					//yes... exist recursion...
					previousNode.setArgumentsOfResidentMFrag();

//					//TODO treatment of the MFragInstance
//					MFragInstance mFragInputNode = new MFragInstance(
//							previousNode.getSsbnNode().getResident().getMFrag()); 

					generateRecursive(previousNode.getSsbnNode(), seen, net);

//					previousNode.setArgumentsOfInputMFrag();

					/* it takes the parameters' names back to normal */
					if(!currentNode.getParents().contains(previousNode)){
						if(!previousNode.getSsbnNode().isFinding()){
							currentNode.addParent(previousNode.getSsbnNode(), true);
						}else{
							currentNode.addParent(previousNode.getSsbnNode(), false);
						}
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

					SSBNNode ssbnNode = ssbnNodeJacket.getSsbnNode();
					ssbnNodeJacket.setArgumentsOfResidentMFrag(); 

//					ssbnNodeJacket.setSsbnNode(checkForDoubleSSBNNode(ssbnNode)); 
					ssbnNodeJacket.getSsbnNode().setPermanent(true); 

					logManager.appendln("Node Created: " + ssbnNode.toString());
					//TODO treatment of the MFragInstance
//					MFragInstance mFragInputNode = new MFragInstance(
//							ssbnNode.getResident().getMFrag()); 
					generateRecursive(ssbnNode, seen, net);	// algorithm's core

					ssbnNodeJacket.setArgumentsOfInputMFrag(); 
					
					if(!currentNode.getParents().contains(ssbnNode)){
						if(!ssbnNode.isFinding()){
							currentNode.addParent(ssbnNode, true);
						}else{
							currentNode.addParent(ssbnNode, false);
						}
					}

				}

			}else{ //ovProblematicList.isEmpty()


				SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNodeTargetOfInput, new ProbabilisticNode(), false);
				ssbnNode.setPermanent(true); 
				SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode); 

				for(OVInstance ovInstance: currentNode.getArguments()){
					addArgumentToSSBNNodeOfInputNode(inputNode, ssbnNodeJacket, ovInstance);
				}

				ssbnNodeJacket.setArgumentsOfResidentMFrag(); 

				SSBNNode test = ssbnNodesMap.get(ssbnNode.getUniqueName());
				
				if(test != null){
					ssbnNodeJacket.getSsbnNode().delete(); 
					ssbnNodeJacket.setSsbnNode(test); 
				}

				seen.add(ssbnNodeJacket.getSsbnNode());
				ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 

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
//					MFragInstance mFragInputNode = new MFragInstance(
//							ssbnNode.getResident().getMFrag()); 
					this.generateRecursive(ssbnNode, seen, net);	// algorithm's core	
				}else{
					logManager.append("Context Nodes False for " + ssbnNode + " (input node)"); 
					ssbnNode.setUsingDefaultCPT(true); 
				}

				ssbnNodeJacket.setArgumentsOfInputMFrag(); 

				if(!currentNode.getParents().contains(ssbnNode)){
					if(!ssbnNode.isFinding()){
						currentNode.addParent(ssbnNode, true);
					}else{
						currentNode.addParent(ssbnNode, false);
					}
				}
			}
		}

		return currentNode;
	}



	/**
	 * Generate the network for the below evidences. 
	 * 
	 * <BR><BR>
	 * <strong>Pos requisites:</strong> 
	 * <br>If the node is a finding, the atribute permanent will be setted how <code>true</code>
	 * 
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


		ResidentNode residentNode = currentNode.getResident(); 

		//some inicializations

		logManager.appendln("\n\n[D] Recursive Call Count = " + recursiveCallCount); 

		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}

		this.recursiveCallCount++; //Yes!!! This increment the recursive call... 

		logManager.appendln("[D]" + currentNode + ": -------------EVALUATING NODE BELOW: " + currentNode.getName() + "--------------\n"); 


		//------------------------- STEP 1: Search for findings -------------

		logManager.appendln("[D]" + currentNode + ":A - Search findings");

		//check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = getKnowledgeBase().searchFinding(currentNode.getResident(), currentNode.getArguments()); 

		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			
			if(!seen.contains(currentNode)){
				seen.add(currentNode); 
			}
			
			logManager.appendln("[D]"  + currentNode + ":Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 

			//If some recursive call return true, this node is permanent... and all the nodes above
			currentNode.setPermanent(true); 
			
			findingList.add(currentNode); 
			
			return true;   //OK! THE PATH TO THIS NODE IS VALID!!!!
		}else{
			logManager.appendln("[D]"  + currentNode + ":Search finding fail"); 
		}

		// if the program reached this line, the node doesn't have a finding
		if(!seen.contains(currentNode)){
			seen.add(currentNode); 
		}
			



		//Evaluating context nodes???
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 

		//------------------------- STEP 2: analyze context nodes. -------------

		// evaluates querynode's mfrag's context nodes 
		//(if not OK, sets MFrag Instance's flag to use default CPT)	

		logManager.appendln("[D]"  + currentNode + ":B - Analyse context nodes");

		boolean evaluateRelatedContextNodesResult = false;

		try {
			evaluateRelatedContextNodesResult = evaluateRelatedContextNodes(
					currentNode.getResident(), ovInstancesList, null);
		} catch (OVInstanceFaultException e) {
			//Pré-requisite...
			e.printStackTrace();
			logManager.appendln("[D]"  + currentNode + ":FATAL!!!!!!!\n\n"); 
			return false; 
		}

		if(!evaluateRelatedContextNodesResult){
			logManager.appendln("Context Node fail for " + currentNode.getResident().getMFrag()); 
			currentNode.setUsingDefaultCPT(true);
			return false; 
		}

		logManager.appendln("[D]" + currentNode + ":B - Resident Nodes below");

		//------------------------- STEP 2: Resident nodes childs in the same MFrag  -------------

		for(ResidentNode r: residentNode.getResidentNodeChildList()){

			logManager.appendln("[D]" + currentNode + ": Resident Node Child analisy -> " + r);

			/*
			 * Analyze if it has one ov instance for each ordinary variable. If this 
			 * is not true, verify if it has some context node able to recover 
			 * the instances that match with the ordinary variable.  
			 */
			List<OrdinaryVariable> ovProblematicList = analyzeOVInstancesListCompletudeForOVList(
					r.getOrdinaryVariableList(), ovInstancesList); 

			if(!ovProblematicList.isEmpty()){

				logManager.appendln("[D]" + currentNode + ": OVariable list problem");

				try{
				    List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
							r.getMFrag(), currentNode, r, 
							ovProblematicList, ovInstancesList, false); 
					
					for(SSBNNode ssbnNode: createdNodes){
						
						if(!currentNode.getParents().contains(ssbnNode)){
							
							generateRecursive(ssbnNode, seen, net);	
							currentNode.addParent(ssbnNode, false);
						    
						    if(ssbnNode.isPermanent()){
						    	currentNode.setPermanent(true); 
						    }
						}
					}
				}
				catch(Exception e){
					// IncompleteInformation Error???
					//The search don't is made for the below part of algotithm
					logManager.appendln("[D]" + currentNode + ":ERRO DRASTICO!!! \n\n"); 
					e.printStackTrace(); 
				}
				finally{
					continue; //Go to next resident node child... 
				}
			}

			logManager.appendln("[D]" + currentNode + ": OVariable list OK");
		
			SSBNNode ssbnNode = null; 

			List<OVInstance> arguments = fillArguments(ovInstancesList, r);
			SSBNNode testSSBNNodeDuplicated = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(r, arguments)); 

			//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
			if(testSSBNNodeDuplicated == null){
				
				ssbnNode = SSBNNode.getInstance(net, r , new ProbabilisticNode(), false);
				ssbnNode.setPermanent(false); 
				
				for(OVInstance ovInstance: arguments){
					ssbnNode.addArgument(ovInstance);
				}
				
				seen.add(ssbnNode);
				ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 
				
				generateRecursive(ssbnNode, seen, net);
				
				if(ssbnNode.isPermanent()){
					currentNode.setPermanent(true); 
				}
				
			}else{
				ssbnNode = testSSBNNodeDuplicated;
				
			}

			if(ssbnNode.isPermanent()){
				currentNode.setPermanent(true); 
				if(!ssbnNode.getParents().contains(currentNode)){
					ssbnNode.addParent(currentNode, true);
				}
			}else{
				//Do nothing... 
			}

			logManager.appendln("exit"); 

		}




		//------------------------- STEP 3: Search childs of inputs that have this node how target -------------

		logManager.appendln("[D]" + currentNode + ":C - Input Nodes below");

		//search all the resident nodes in others MFrags referenced by this ResidentNode. 

		for(InputNode inputNode: residentNode.getInputInstanceFromList()){

			logManager.appendln("[D]" + currentNode + ": Input Node Child analisy -> " + inputNode);

			//Set the arguments of the input node in the new MFrag. 

			SSBNNodeJacket currentNodeInputSSBNNodeJacket = new SSBNNodeJacket(currentNode); 
			for(OVInstance ovInstance: currentNode.getArgumentsAsList()){
				addArgumentToSSBNNodeOfResidentNode(residentNode, 
						inputNode, currentNodeInputSSBNNodeJacket, ovInstance); 
			}
			
			List<OVInstance> ovInstancesInput = currentNodeInputSSBNNodeJacket.getOvInstancesOfInputMFrag(); 
			
			//Analisy of the context nodes of the new MFrag for the inputNode. 
			boolean contextNodesOK = false; 
			
			try {
				contextNodesOK = evaluateRelatedContextNodes (inputNode, 
						ovInstancesInput, null);
			} catch (OVInstanceFaultException e1) {
				// IncompleteInformation Error???
				//The search don't is made for the below part of algotithm
				logManager.appendln("[D]" + currentNode + ":ERRO DRASTICO!!! \n\n"); 
				e1.printStackTrace(); 
			} finally{
				if(!contextNodesOK){
					continue; //This input node don't is valid for the algorith...
				}
			}
			
			for(ResidentNode residentChild: inputNode.getResidentNodeChildList()){
				logManager.appendln("[D]" + currentNode + ": Child of the input -> " + residentChild);
				logManager.appendln("[D]" + currentNode + ": do nothing...");
				
				//Evaluation of the recursion below
				//The format of the MFrag in the recursion is: 
				//      INPUTNODE_A(NODE_A) PAI NODE_A 
				//That folows: 
				//       RESIDENTCHILD = NODE_A
				// (RESIDENTCHILD is the reference of the input INPUTNODE_A
				
				if(residentChild.equals(currentNode.getResident())){
					
					SSBNNode procNode = getProcNode(currentNode, seen, net, 
							residentChild);
					
					if(procNode != null){
//						procNode.setPermanent(false); 
						generateRecursive(procNode, seen, net); 
						procNode.addParent(currentNode, false);
						
						if(procNode.isPermanent()){
							currentNode.setPermanent(true); 
						}
					}
					
					continue; 
					
				}

				/*
				 * Analyze if it has one ov instance for each ordinary variable. If this 
				 * is not true, verify if it has some context node able to recover 
				 * the instances that match with the ordinary variable.  
				 */
				List<OrdinaryVariable> ovProblematicList = analyzeOVInstancesListCompletudeForOVList(
						residentChild.getOrdinaryVariableList(), ovInstancesInput); 

				if(!ovProblematicList.isEmpty()){

					logManager.appendln("[D]" + currentNode + ": OVariable list problem");
					logManager.appendln("[D]" + currentNode + ": TODO OVariable list problem");
					
					try{
						
						//Note: if the entities for the problematic OV don't was found, 
						//the search strategy don't will be used. 
						List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
								residentChild.getMFrag(), currentNode, residentChild, 
								ovProblematicList, ovInstancesInput, false);
						
						for(SSBNNode ssbnNode: createdNodes){
							if(!ssbnNode.getParents().contains(currentNode)){
								generateRecursive(ssbnNode, seen, net);			    	
								ssbnNode.addParent(currentNode, false);
							}
						}
						continue; 
					}
					catch(Exception e){
						logManager.append("\n\nFATAL!!!\n\n");
						continue; //To the next input node
					}
				}


				logManager.appendln("[D]" + currentNode + ": OVariable list OK");
				//ovProblematicList.isEmpty
				SSBNNode ssbnNode = null; 

				List<OVInstance> arguments = fillArguments(ovInstancesList, residentChild);
				SSBNNode testSSBNNodeDuplicated = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(residentChild, arguments)); 

				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
				if(testSSBNNodeDuplicated == null){
					ssbnNode = SSBNNode.getInstance(net, residentChild , new ProbabilisticNode(), false);
					ssbnNode.setPermanent(false); 
					for(OVInstance ovInstance: arguments){
						ssbnNode.addArgument(ovInstance);
					}
					seen.add(ssbnNode);
					ssbnNodesMap.put(ssbnNode.getName(), ssbnNode); 
					generateRecursive(ssbnNode, seen, net);	
				}else{
					ssbnNode = testSSBNNodeDuplicated;
				}

				if(ssbnNode.isPermanent()){
					currentNode.setPermanent(true); 
					if(!ssbnNode.getParents().contains(currentNode)){
					    ssbnNode.addParent(currentNode, true);
					}
				}else{
					//Do nothing... 
				}

				logManager.appendln("exit"); 
				
			}

		}

		logManager.appendln("[D]" + currentNode + ":D - The end");

		return currentNode.isPermanent(); 
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
		if (residentNode.getMFrag().isUsingDefaultCPT()) {
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

	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	private boolean evaluateRelatedContextNodes (InputNode inputNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance) throws OVInstanceFaultException{

		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (inputNode.getMFrag().isUsingDefaultCPT()) {
			return false;
		};

		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(getKnowledgeBase()); 


		//TODO Refazer!!! Esta abordagem nao permite a abordagem da transitividade
		//dos nos de contexto... 

		Collection<ContextNode> contextNodeList = inputNode.getMFrag().getContextByOVCombination(
				inputNode.getOrdinaryVariableList());

		for(ContextNode context: contextNodeList){
			logManager.appendln("Context Node: " + context.getLabel());
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				inputNode.getMFrag().setAsUsingDefaultCPT(true); 
				logManager.appendln("Result = FALSE. Use default distribution ");
				return false;  
			}else{
				logManager.appendln("Result = TRUE.");
			}		
		}

		return true; 
	}

	
	public void evaluateContextNodeSet(MFragInstance mFragInstance, 
			List<OrdinaryVariable> ordinaryVariableList, List<OVInstance> listOVInstance){
		
		/*
		 * Casos: 
		 * 
		 * 1-> Argumentos OK, apenas avaliar validade... trivial
		 * 2-> Faltam argumentos, procurar nós e avaliar resultados que tornem verdadeiro
		 * 3-> Search case (algoritmo do XOR). 
		 */
		
		//Montar conjunto de nós de contexto de interesse
		
		
		//Avaliar conjunto
		
		
		
	}
	
}
