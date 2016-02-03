package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.IBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.IMFragContextNodeAvaliator;
import unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.MFragContextNodeAvaliator;
import unbbayes.prs.mebn.ssbn.MFragInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleContextNodeFatherSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.util.Debug;

/**
 * Generate Situation Specific Bayesian Network based on Bayes Ball algorithm 
 * for verify D-Connected nodes. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BayesBallSSBNGenerator implements IMediatorAwareSSBNGenerator{

	private boolean isLogEnabled = true;
	private INetworkMediator mediator;
	
	private KnowledgeBase kb; 
	private SSBN ssbn; 
	private IMFragContextNodeAvaliator mFragContextNodeAvaliator; 
	private ISSBNLogManager logManager; 
	
	/**
	 * Generate a SSBN using a adaptation of the Bayes Ball algorithm. 
	 */
	public SSBN generateSSBN(List<Query> listQueries, KnowledgeBase kb)
			throws SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {
		
		this.kb = kb; 
		
		System.out.println("");
		System.out.println("---------------------------------");
		System.out.println("       Bayes Ball Algorithm      ");
		System.out.println("---------------------------------");
		System.out.println("");		
		
		//Initialization 
		
		ssbn = new SSBN();
		ssbn.setKnowledgeBase(kb); 
		
		mFragContextNodeAvaliator =    
				new MFragContextNodeAvaliator(ssbn); 
		
		logManager = null;
		if (ssbn != null) {
			logManager = ssbn.getLogManager();
		}
		
		//Step 1: 
//		ssbn = initialization(queryList, knowledgeBase); 
		for(Query query: listQueries){
			System.out.println("Query: " + query);
			BayesBallNode ssbnNode = BayesBallNode.getInstance(query.getResidentNode()); 
			query.setSSBNNode(ssbnNode); 
			
			for(OVInstance argument : query.getArguments()){
				ssbnNode.setEntityForOv(argument.getOv(), argument.getEntity()); 	
			}
			
			ssbnNode.setFinished(false); 
			//TODO verify and possibly remove this
//			ssbn.addSSBNNodeIfItDontAdded(ssbnNode);
			ssbn.addQueryToQueryList(query);                                                          
		}
				
		//Step 2: 
		buildStructure(ssbn); 
		
		//Step 3: 
		List<IPruner> pruners = new ArrayList<IPruner>();
		pruners.add(BarrenNodePruner.newInstance());
		
		IPruneStructure pruneStructure = PruneStructureImpl.newInstance(pruners); 
		pruneStructure.pruneStructure(ssbn);
		
		//Step 4: 
		IBuilderLocalDistribution builder = BuilderLocalDistributionImpl.newInstance(); 
		builder.buildLocalDistribution(ssbn);
				
		System.out.println("Local distribution generated!");
		
		// adjust position of nodes
		try {
			PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(ssbn.getProbabilisticNetwork());
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		System.out.println("Positions adjusted");
		
		if (ssbn.getNetwork().getNodeCount() != ssbn.getNetwork().getNodeIndexes().keySet().size()) {
			// inconsistency on the quantity of indexed nodes and actual nodes
			// force synchronization
			ssbn.getNetwork().getNodeIndexes().clear();
			for (int i = 0; i < ssbn.getNetwork().getNodes().size(); i++) {
				ssbn.getNetwork().getNodeIndexes().put(ssbn.getNetwork().getNodes().get(i).getName(), i);
			}
		}
		
		try {
			ssbn.compileAndInitializeSSBN();
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		System.out.println("Network compiled");
		
		// show on display
		if (this.getMediator() instanceof IMEBNMediator) {
			((IMEBNMediator)this.getMediator()).setSpecificSituationBayesianNetwork(ssbn.getProbabilisticNetwork());
			((IMEBNMediator)this.getMediator()).setToTurnToSSBNMode(true);	// if this is false, ((IMEBNMediator)this.getMediator()).turnToSSBNMode() will not work
			((IMEBNMediator)this.getMediator()).turnToSSBNMode();
			((IMEBNMediator)this.getMediator()).getScreen().getEvidenceTree().updateTree(true);;
		}
		
		return ssbn;
	}
	
	protected void buildStructure(SSBN ssbn) throws ImplementationRestrictionException, 
	                                                SSBNNodeGeneralException{
		
		System.out.println("Build Structure");
		
		//All nodes 
		List<BayesBallNode> nodeList = new ArrayList<BayesBallNode>();  //all nodes visualized
		
		//Scheduled nodes 
		List<BayesBallNode> unvaluedNodeList = new ArrayList<BayesBallNode>(); 
		
		BayesBallNode targetBayesBallNode = (BayesBallNode)ssbn.getQueryList().get(0).getSSBNNode(); 
		
		targetBayesBallNode.setReceivedBallFromChild(true);
		
		unvaluedNodeList.add(targetBayesBallNode); 
		nodeList.add(targetBayesBallNode); 

		while (unvaluedNodeList.size() > 0){
			
			System.out.println("-------------- NOVA ITERACAO -----------------");
			// This version of Bayes Ball algorithm treat only probabilistic nodes 
			for(BayesBallNode node: unvaluedNodeList){
				
				//Evaluated Node: 
				//Here, we have to: 
				// - Recover MFragInstance 
				// - evaluate context nodes and set node as default distribution 
				// - Verify if node is a finding 
				try {
					evaluateNode(node);
				} catch (MFragContextFailException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				node.setVisited(true);

				System.out.println("Evaluating node: " + node);
				//Receiving ball from child 
				//  Not finding 
				//  -> pass ball to parents 
				//  -> pass ball to children 
				//  Finding 
				//  -> block 
				if (node.isReceivedBallFromChild()){
					System.out.println("Is Received ball from child");
					if (!node.isObserved()){
						if(!node.isEvaluatedTop()){
							evaluateTop(nodeList, node);
						}
						if(!node.isEvaluatedBottom()){
							evaluateBottom(nodeList, node);
						}
					}
					//Receiving ball from parent 
					//  Not finding 
					//  -> pass ball to children 
					//  Finding 
					//  -> pass ball to parents 
				}
				
				if (node.isReceivedBallFromParent()){
					System.out.println("Is Received ball from parent");
					if (!node.isObserved()){
						if(!node.isEvaluatedBottom()){
							evaluateBottom(nodeList, node);
						}
					} else{
						if(!node.isEvaluatedTop()){
							evaluateTop(nodeList, node);
						}
					}
				}
				
			}
			
			//Schedule nodes to be evaluated in the next iteration 
			unvaluedNodeList.clear(); 
			System.out.println("");
			System.out.println("----------------------------------");
			System.out.println("Creating list of unvaluated nodes");
			for(BayesBallNode node: nodeList){
				if(!node.isVisited()){
					System.out.println(node + " UP " + 
				            node.isEvaluatedBottom() + " DOWN " + node.isEvaluatedTop() + 
				            " CHILD " + node.isReceivedBallFromChild() + " FATH " + node.isReceivedBallFromParent());
					unvaluedNodeList.add(node); 
				}
			}
			System.out.println("");
			System.out.println("------------------------------------");
		}

		System.out.println("");
		System.out.println("Evaluation finished!!!");
		
		System.out.println("");
		System.out.println("Created Nodes:");
		for(BayesBallNode node: nodeList){
			System.out.println("- Node: " + node);
			ssbn.addSSBNNode(node);
		}
		
	}
	
	private void evaluateNode(BayesBallNode node) throws SSBNNodeGeneralException, MFragContextFailException{
		
		//------------------------------------------------------------------------------
		// Step 1: Verify if node is a finding 
		//------------------------------------------------------------------------------
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		for(int i = 0; i < node.getOvArray().length; i++){
			argumentList.add(OVInstance.getInstance(node.getOvArray()[i], 
					node.getEntityArray()[i])); 
		}
		
		StateLink exactValue = kb.searchFinding(
				node.getResidentNode(), argumentList);
		
		/*
		 * Note: how the algorithm is bottom-up, it is necessary continue the evaluation 
		 * to up even if the node is setted how a finding, because, above it can have
		 * a query or a node that influence the query. 
		 */
		if(exactValue!= null){
			//The node is a finding... 
			node.setState(exactValue.getState());
			node.setObserved(true);
			ssbn.addFindingToFindingList(node); 
			System.out.println("Node is a finding. Exact Value = " + exactValue.getState());
			
		}
		
		//Evaluate MFrag Instance
		
		if(node.getMFragInstance()!=null){
			System.out.println("Node already have a MFrag Instance: " + node.getMFragInstance());
			if(node.getMFragInstance().isUsingDefaultDistribution()){
				node.setDefaultDistribution(true);
				System.out.println("Node using default distribution.");
			}
		}else{
			
			List<OVInstance> oviList = new ArrayList<OVInstance>();
			
			//Add the arguments values to the MFrag Instance. 
			for(int i = 0; i < node.getOvArray().length; i++){
				oviList.add(OVInstance.getInstance(node.getOvArray()[i], node.getEntityArray()[i])); 
			}

			//Create MFrag Instance 
			MFragInstance mFragInstance = createAndEvaluateMFragInstance(
					node.getResidentNode().getMFrag(), oviList); 
			
			node.setMFragInstance(mFragInstance);
			
			
		}
	}

	private MFragInstance createAndEvaluateMFragInstance(MFrag mFrag, List<OVInstance> ovInstanceList)
			throws SSBNNodeGeneralException, MFragContextFailException {
		
		MFragInstance mFragInstance = MFragInstance.getInstance(mFrag); 
		
		System.out.println("Created new MFrag Instance for MFrag " + mFrag );
		
		//TODO Change for this point don't throw more a MFragContextFailException
		for(OVInstance ovi: ovInstanceList){
			mFragInstance.addOVValue(ovi.getOv(), ovi.getEntity().getInstanceName());
		}
				
		//TODO We should verify in this point if we already have a MFragInstance with the filled arguments 
		
		//Evaluate context Nodes
		// 2) Evaluate MFragInstance context
		try {
			if (logManager != null) {
				logManager.printText(IdentationLevel.LEVEL_3, true, "Evaluate MFrag Context Nodes");
			}
			
			//Will build the tree of evaluation of the ordinary variables. 
			mFragContextNodeAvaliator.evaluateMFragContextNodes(mFragInstance);
			
		} catch (ImplementationRestrictionException e) {
			//Stop the evaluation when the context nodes fail. 
			//TODO warning... the evaluation continue, using the default distribution
//				throw e; 
//			} catch (OVInstanceFaultException e) {
//				throw new ImplementationRestrictionException(e.getMessage()); 
			mFragInstance.setUseDefaultDistribution(true); 
			System.out.println("MFrag Instance using default distribution");
			
		} catch (MFragContextFailException e) {
			//TODO warning... the evaluation continue, using the default distribution
//				throw new SSBNNodeGeneralException(e.getMessage()); 
			mFragInstance.setUseDefaultDistribution(true); 
			System.out.println("MFrag Instance using default distribution");
		
		} 	
		
		mFragInstance.setEvaluated(true);
		
		return mFragInstance; 
	}
	
	private void evaluateBottom(List<BayesBallNode> nodeList,
			BayesBallNode node) throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		System.out.println("Evaluate bottom");
		
		
		/* ***************************************************************************
		/*                      RESIDENT NODES IN SOME  MFRAGS 
		 * ***************************************************************************/
		
		for(ResidentNode childrenNode: node.getResidentNode().getResidentNodeChildList()){
			
			//*** NEW NODE CREATED ***
			
			int contextParentsCount = 0; 
			List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 

			//Fill the ovFault list, in relation with the child node. The ordinary
			//variables of the MFragInstance will be used more forward. 
			for(OrdinaryVariable ov2: childrenNode.getOrdinaryVariableList()){
				boolean find = false; 
				for(OrdinaryVariable ov: node.getOvArray()){
					if(ov2.equals(ov)){
						find = true; 
						break; 
					}
				}
				if(!find){
					System.out.println("OV Fault = " + ov2);
					ovFaultForNewNodeList.add(ov2); 
				}
			} // End-for ov	
			
			//Mount the combination of possible values for the ordinary variable fault
			List<String[]> possibleCombinationsForOvFaultList = new ArrayList<String[]>(); 
			
			List<SimpleContextNodeFatherSSBNNode> contextNodeFatherList = 
					new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
			
			if(ovFaultForNewNodeList.size() > 0){
				
				for(OrdinaryVariable ov: ovFaultForNewNodeList){
					
					SimpleContextNodeFatherSSBNNode contextNodeFather = 
						node.getMFragInstance().getContextNodeFatherForOv(ov); 
					System.out.println("Context node for ov " + ov + ":" + contextNodeFather);
					
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
				} //end-for OV
				possibleCombinationsForOvFaultList = node.getMFragInstance().
				        recoverCombinationsEntitiesPossibles(
						    node.getOvArray(), 
					      	node.getEntityArray(), 
						    ovFaultForNewNodeList.toArray(
	                    		 new OrdinaryVariable[ovFaultForNewNodeList.size()])); 
				
				System.out.println("Combination for ov Fault List: ");
				for(int i = 0; i < possibleCombinationsForOvFaultList.size(); i ++){
					String[] vetor = possibleCombinationsForOvFaultList.get(i); 
					for(int j = 0; j < vetor.length; j++){
						System.out.print(vetor[j]+ " , " );
					}
					System.out.println("");
				}
				
			}else{
				possibleCombinationsForOvFaultList.add(new String[0]); //A stub element (see for below)
			}	
			
			//Create the new node... We pass by each combination for the ov fault list. 
			//Note that if we don't have any ov fault, we should have one stub element
			//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
			Iterator<String[]> iterator = possibleCombinationsForOvFaultList.iterator();
			
			while (iterator.hasNext()) {	
				// using iterator instead of for(T obj : iterable), because there 
				//is a place where we need to call iterator.hasNext() inside the loop
				String[] possibleCombination = iterator.next();
				
				BayesBallNode newNode = BayesBallNode.getInstance(childrenNode); 
			
				System.out.println("Generated node: " + newNode);
				
				//Fill the ovFault list, in relation with the child node. The ordinary
				//variables of the MFragInstance will be used more forward. 
				for(OrdinaryVariable ov2: childrenNode.getOrdinaryVariableList()){
					for(int i = 0; i < node.getOvArray().length; i++){
						if(ov2.equals(node.getOvArray()[i])){
							newNode.setEntityForOv(
									ov2, 
									node.getEntityArray()[i]); 
							break; 
						}
					}
				} // end-for ov				
									
				
				//2. Create the new OVInstances for the combination
				for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
					
					OrdinaryVariable correspondentOV = ovFaultForNewNodeList.get(index); 
					
					newNode.setEntityForOv(
							correspondentOV,
							LiteralEntityInstance.getInstance(
									possibleCombination[index],
									ovFaultForNewNodeList.get(index).getValueType())); 

				}
				
				for(int i = 0; i < newNode.getOvArray().length; i++){
					System.out.print(newNode.getOvArray()[i] + " - " );
					System.out.println(newNode.getEntityArray()[i].getInstanceName());
				}
				
				if(nodeList.contains(newNode)){
					int index = nodeList.indexOf(newNode);
					newNode = nodeList.get(index); 
					System.out.println("Node already exists: " + newNode);
				}else{		
					nodeList.add(newNode); 
					newNode.setMFragInstance(node.getMFragInstance());
					System.out.println("Created Node: " + newNode);
				}
				
				//Set reference for the ordinary variables list in input MFrag 
				newNode.setVisited(false);
				newNode.setReceivedBallFromParent(true);
				newNode.addParent(node);
				
				System.out.println("Created Node: " + newNode);

			} // end while iterator 			
		
		}
		
		/* ***************************************************************************
		/*                      RESIDENT NODES IN OTHER MFRAGS 
		 * ***************************************************************************/
		
		List<MFrag> mFragAlreadyEvaluatedList = new ArrayList<MFrag>(); 
		
		for(InputNode inputNode: node.getResidentNode().getInputInstanceFromList()){
			
			
			//Test double input node. 
			boolean testDoubleInputNode = false; 
			
			if(node.getOvArrayForMFrag(inputNode.getMFrag()) != null){
				
				for (int i = 0 ; i < node.getOvArrayForMFrag(inputNode.getMFrag()).length; i++	){
					if (!(node.getOvArrayForMFrag(inputNode.getMFrag())[i].equals(
							inputNode.getOrdinaryVariableByIndex(i)))){
						testDoubleInputNode = true;
						System.out.println("Double Input Node for " + inputNode);
						break; 
					}
				}
			}
//			
			if (testDoubleInputNode) break; 
			
			// When we have two input nodes of the same resident node in one MFrag, 
			// we need create only on of it. The other should be created originated
			// by the context nodes of the MFrag. 
			if (mFragAlreadyEvaluatedList.contains(inputNode.getMFrag())){
				System.out.println("Two ocorrences of same input node in MFrag: " + inputNode);
				System.out.println("The second ocorrence will be bypassed!");
				break; 
			}else{
				mFragAlreadyEvaluatedList.add(inputNode.getMFrag()); 
			}
			
			System.out.println("Evaluate input referente: " + inputNode);
			
			OrdinaryVariable[] ovArray = new OrdinaryVariable[inputNode.getOrdinaryVariableList().size()];
			List<OVInstance> oviArray = new ArrayList<OVInstance>(); 
			
			//Fill the arguments in the new MFrag 
			for(int i = 0; i < inputNode.getOrdinaryVariableList().size(); i++){
				ovArray[i] = inputNode.getOrdinaryVariableByIndex(i); 
				oviArray.add(OVInstance.getInstance(ovArray[i], node.getEntityArray()[i])); 
			}
			
			node.setOVArrayForMFrag(inputNode.getMFrag(), ovArray);
			
			//Evaluate the MFrag of the input node. 
			MFragInstance mFragInstance = null;
			
			try {
				mFragInstance = createAndEvaluateMFragInstance(inputNode.getMFrag(), oviArray);
			} catch (MFragContextFailException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			node.addInputMFragInstance(mFragInstance);
			
			for(ResidentNode residentNodeChild: inputNode.getResidentNodeChildList()){
				
				System.out.println("Evaluate resident node children: " + residentNodeChild );
				
				int contextParentsCount = 0; 
				List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 

				//Fill the ovFault list, in relation with the child node. The ordinary
				//variables of the MFragInstance will be used more forward. 
				for(OrdinaryVariable ov2: residentNodeChild.getOrdinaryVariableList()){
					boolean find = false; 
					for(OrdinaryVariable ov: inputNode.getOrdinaryVariableList()){
						if(ov2.equals(ov)){
							find = true; 
							break; 
						}
					}
					if(!find){
						System.out.println("OV Fault = " + ov2);
						ovFaultForNewNodeList.add(ov2); 
					}
				} // End-for ov		
				
				
				//Mount the combination of possible values for the ordinary variable fault
				List<String[]> possibleCombinationsForOvFaultList = new ArrayList<String[]>(); 
				
				List<SimpleContextNodeFatherSSBNNode> contextNodeFatherList = 
						new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
				
				if(ovFaultForNewNodeList.size() > 0){
					
					for(OrdinaryVariable ov: ovFaultForNewNodeList){
						
						SimpleContextNodeFatherSSBNNode contextNodeFather = 
							mFragInstance.getContextNodeFatherForOv(ov); 
						System.out.println("Context node for ov " + ov + ":" + contextNodeFather);
						
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
					} //end-for OV
					possibleCombinationsForOvFaultList = mFragInstance.
					        recoverCombinationsEntitiesPossibles(
							    ovArray, 
						      	node.getEntityArray(), 
							    ovFaultForNewNodeList.toArray(
		                    		 new OrdinaryVariable[ovFaultForNewNodeList.size()])); 
					
					System.out.println("Combination for ov Fault List: ");
					for(int i = 0; i < possibleCombinationsForOvFaultList.size(); i ++){
						String[] vetor = possibleCombinationsForOvFaultList.get(i); 
						for(int j = 0; j < vetor.length; j++){
							System.out.print(vetor[j]+ " , " );
						}
						System.out.println("");
					}
					
				}else{
					possibleCombinationsForOvFaultList.add(new String[0]); //A stub element (see for below)
				}					
				
				//Create the new node... We pass by each combination for the ov fault list. 
				//Note that if we don't have any ov fault, we should have one stub element
				//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
				Iterator<String[]> iterator = possibleCombinationsForOvFaultList.iterator();
				
				while (iterator.hasNext()) {	
					// using iterator instead of for(T obj : iterable), because there 
					//is a place where we need to call iterator.hasNext() inside the loop
					String[] possibleCombination = iterator.next();
					
					BayesBallNode newNode = BayesBallNode.getInstance(residentNodeChild); 
				
					System.out.println("Generated node: " + newNode);
					
					//Fill the ovFault list, in relation with the child node. The ordinary
					//variables of the MFragInstance will be used more forward. 
					for(OrdinaryVariable ov2: residentNodeChild.getOrdinaryVariableList()){
						for(int i = 0; i < ovArray.length; i++){
							if(ov2.equals(ovArray[i])){
								newNode.setEntityForOv(
										ov2, 
										oviArray.get(i).getEntity()); 
								break; 
							}
						}
					} // end-for ov				
										
					
					//2. Create the new OVInstances for the combination
					for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
						
						OrdinaryVariable correspondentOV = ovFaultForNewNodeList.get(index); 
						
						newNode.setEntityForOv(
								correspondentOV,
								LiteralEntityInstance.getInstance(
										possibleCombination[index],
										ovFaultForNewNodeList.get(index).getValueType())); 

					}
					
					for(int i = 0; i < newNode.getOvArray().length; i++){
						System.out.print(newNode.getOvArray()[i] + " - " );
						System.out.println(newNode.getEntityArray()[i].getInstanceName());
						
					}
					
					if(nodeList.contains(newNode)){
						int index = nodeList.indexOf(newNode);
						newNode = nodeList.get(index); 
						System.out.println("Node already exists: " + newNode);
					}else{		
						nodeList.add(newNode); 
						
						//Solving Issue #03. I need re-evaluate the MFrag of the 
						//not starting from the resident node because the ordinary
						//variables that exists only in the input node can have 
						//others values inside the MFrag.
//						newNode.setMFragInstance(mFragInstance);
						
						System.out.println("Created Node: " + newNode);
					}
					
					//Set reference for the ordinary variables list in input MFrag 
					newNode.setVisited(false);
					newNode.setReceivedBallFromParent(true);
					newNode.addParent(node);
					
					System.out.println("Created Node: " + newNode);

				} // end while iterator 
				
			}//End-for childNode 
			
		}
		
		node.setEvaluatedBottom(true);
		
	}

	private void evaluateTop(List<BayesBallNode> nodeList, BayesBallNode node) throws ImplementationRestrictionException {
		
		System.out.println("Evaluate top");
		
		/* ***************************************************************************
		/*                              RESIDENT NODES
		 * ***************************************************************************/
		
		for(ResidentNode fatherNode: node.getResidentNode().getResidentNodeFatherList()){
			
			int contextParentsCount = 0; 
			List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 
			
			//Fill the ovFault list, in relation with the child node. The ordinary
			//variables of the MFragInstance will be used more forward. 
			for(OrdinaryVariable ov: fatherNode.getOrdinaryVariableList()){
				boolean find = false; 
				for(OrdinaryVariable ov2: node.getOvArray()){
					if(ov2.equals(ov)){
						find = true; 
						break; 
					}
				}
				if(!find){
					System.out.println("OV Fault = " + ov);
					ovFaultForNewNodeList.add(ov); 
				}
			} // End-For ov
			
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
				} //end-for OV
				
				//TODO If I already have one MFragInstance, I can keep the nodes 
				//previously generated for it, avoiding all this process for search 
				//two times for the arguments of the MFrag. 
								
				possibleCombinationsForOvFaultList = node.getMFragInstance().
				        recoverCombinationsEntitiesPossibles(
						    node.getOvArray(), 
					      	node.getEntityArray(), 
						    ovFaultForNewNodeList.toArray(
	                    		 new OrdinaryVariable[ovFaultForNewNodeList.size()])); 
				
				System.out.println("Combination for ov Fault List: ");
				for(int i = 0; i < possibleCombinationsForOvFaultList.size(); i ++){
					String[] vetor = possibleCombinationsForOvFaultList.get(i); 
					for(int j = 0; j < vetor.length; j++){
						System.out.print(vetor[j]+ " , " );
					}
					System.out.println("");
				}
				
			}else{
				possibleCombinationsForOvFaultList.add(new String[0]); //A stub element (see for below)
			} // End If OvFault
			
			//Create the new node... We pass by each combination for the ov fault list. 
			//Note that if we don't have any ov fault, we should have one stub element
			//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
			Iterator<String[]> iterator = possibleCombinationsForOvFaultList.iterator();
			
			while (iterator.hasNext()) {	
				// using iterator instead of for(T obj : iterable), because there 
				//is a place where we need to call iterator.hasNext() inside the loop
				String[] possibleCombination = iterator.next();
				
				BayesBallNode newNode = BayesBallNode.getInstance(fatherNode); 
				
				//Fill the ovFault list, in relation with the child node. The ordinary
				//variables of the MFragInstance will be used more forward. 
				for(OrdinaryVariable ov2: fatherNode.getOrdinaryVariableList()){
					for(int i = 0; i < node.getOvArray().length; i++){
						if(ov2.equals(node.getOvArray()[i])){
							newNode.setEntityForOv(
									ov2, 
									node.getEntityArray()[i]); 
							break; 
						}
					}
				} // end-for ov	
				
				//2. Create the new OVInstances for the combination
				for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
					
					OrdinaryVariable correspondentOV = ovFaultForNewNodeList.get(index); 
					
					newNode.setEntityForOv(
							correspondentOV,
							LiteralEntityInstance.getInstance(
									possibleCombination[index],
									ovFaultForNewNodeList.get(index).getValueType())); 

				}
				
				for(int i = 0; i < newNode.getOvArray().length; i++){
					System.out.print(newNode.getOvArray()[i] + " - " );
					System.out.println(newNode.getEntityArray()[i].getInstanceName());
					
				}
				
				//2. Create the new OVInstances for the combination
				for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
					
					newNode.setEntityForOv(
							ovFaultForNewNodeList.get(index),
							LiteralEntityInstance.getInstance(
									possibleCombination[index],
									ovFaultForNewNodeList.get(index).getValueType())); 

				}
				
				//Set reference for the ordinary variables list in input MFrag 


				if(nodeList.contains(newNode)){
					int index = nodeList.indexOf(newNode);
					newNode = nodeList.get(index); 
					System.out.println("Node already exists: " + newNode);
				}else{
					nodeList.add(newNode); 
					System.out.println("Created Node: " + newNode);
				}
				
				newNode.setVisited(false);
				newNode.setReceivedBallFromChild(true);
				node.addParent(newNode);
				
			} // end while iterator 
			
			ISSBNLogManager logManager = ssbn.getLogManager();
			
			//Add the context node parent if it exists
			if( contextParentsCount > 0 ){
				
				for(SimpleContextNodeFatherSSBNNode contextParent : contextNodeFatherList){
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_5, false, 
							node + " is child of the contextNode " + 
							contextParent.getContextNode().toString());
					}
					
					node.addContextParent(contextParent); 
				}
			}
			
			
		}
		
		
		
		/* ***************************************************************************
		/*                              INPUT NODES
		 * ***************************************************************************/
		
		for (InputNode fatherInputNode: node.getResidentNode().getParentInputNodesList()){
			
			//*** NEW NODE CREATED ***
			int contextParentsCount = 0; 
			List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 
			
//			boolean testDoubleInputNode = false; 
//			
//			if(node.getOvArrayForMFrag(fatherInputNode.getMFrag()) != null){
//				
//				for (int i = 0 ; i < node.getOvArrayForMFrag(fatherInputNode.getMFrag()).length; i++	){
//					if (!(node.getOvArrayForMFrag(fatherInputNode.getMFrag())[i].equals(
//							fatherInputNode.getOrdinaryVariableByIndex(i)))){
//						testDoubleInputNode = true;
//						System.out.println("Double Input Node for " + fatherInputNode);
//						break; 
//					}
//				}
//			}
//			
//			if (testDoubleInputNode) break; 
			
			
			//Fill the ovFault list, in relation with the child node. The ordinary
			//variables of the MFragInstance will be used more forward. 
			for(OrdinaryVariable ov: fatherInputNode.getOrdinaryVariableList()){
				boolean find = false; 
				for(OrdinaryVariable ov2: node.getOvArray()){
					if(ov2.equals(ov)){
						find = true; 
						break; 
					}
				}
				if(!find){
					System.out.println("OV Fault = " + ov);
					ovFaultForNewNodeList.add(ov); 
				}
			} // end-for ov
			
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
				} //end-for OV
				
				//TODO If I already have the MFragInstance, I can keep the nodes 
				//previously generated for it, avoiding all this process for search 
				//two times for the arguments of the MFrag. 
								
				possibleCombinationsForOvFaultList = node.getMFragInstance().
				        recoverCombinationsEntitiesPossibles(
						    node.getOvArray(), 
					      	node.getEntityArray(), 
						    ovFaultForNewNodeList.toArray(
	                    		 new OrdinaryVariable[ovFaultForNewNodeList.size()])); 
				
				System.out.println("Combination for ov Fault List: ");
				for(int i = 0; i < possibleCombinationsForOvFaultList.size(); i ++){
					String[] vetor = possibleCombinationsForOvFaultList.get(i); 
					for(int j = 0; j < vetor.length; j++){
						System.out.print(vetor[j]+ " , " );
					}
					System.out.println("");
				}
				
			}else{
				possibleCombinationsForOvFaultList.add(new String[0]); //A stub element (see for below)
			}
			
			//Create the new node... We pass by each combination for the ov fault list. 
			//Note that if we don't have any ov fault, we should have one stub element
			//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
			Iterator<String[]> iterator = possibleCombinationsForOvFaultList.iterator();
			
			while (iterator.hasNext()) {	
				// using iterator instead of for(T obj : iterable), because there 
				//is a place where we need to call iterator.hasNext() inside the loop
				String[] possibleCombination = iterator.next();
				
				BayesBallNode newNode = BayesBallNode.getInstance(
				fatherInputNode.getResidentNodePointer().getResidentNode()); 
				
				OrdinaryVariable[] ovArrayMFrag = 
						new OrdinaryVariable[fatherInputNode.getResidentNodePointer().getOrdinaryVariableList().size()]; 
				
				//1. Add the ovInstances of the children that the father also have 
				for(int i = 0; i < node.getOvArray().length; i++){
					
					//For a input node IX1 that references the resident node RX1, we 
					//should recover the ordinary variable of the RX1's HomeMFrag that
					//correspond to the OV of the IX1's MFrag and then set it to the 
					//SSBNNode. 
					OrdinaryVariable residentNodeOV = 
						fatherInputNode.getResidentNodePointer().getCorrespondentOrdinaryVariable(node.getOvArray()[i]);
					
					if(residentNodeOV != null){
						newNode.setEntityForOv(
								residentNodeOV, 
								node.getEntityArray()[i]); 			
					}
					
					int index = fatherInputNode.getResidentNodePointer().getOrdinaryVariableIndex(node.getOvArray()[i]); 
					if(index!= -1){
						ovArrayMFrag[index] = node.getOvArray()[i];
					}
					
				}
				
				//2. Create the new OVInstances for the combination
				for(int index = 0; index < ovFaultForNewNodeList.size(); index++){
					
					OrdinaryVariable correspondentOV = 
						fatherInputNode.getResidentNodePointer().getCorrespondentOrdinaryVariable(ovFaultForNewNodeList.get(index));
					
					newNode.setEntityForOv(
							correspondentOV,
							LiteralEntityInstance.getInstance(
									possibleCombination[index],
									ovFaultForNewNodeList.get(index).getValueType())); 
					
					int index2 = fatherInputNode.getResidentNodePointer().getOrdinaryVariableIndex(ovFaultForNewNodeList.get(index)); 
					ovArrayMFrag[index2] = ovFaultForNewNodeList.get(index); 

				}
				
				//Set reference for the ordinary variables list in input MFrag 


				if(nodeList.contains(newNode)){
					int index = nodeList.indexOf(newNode);
					newNode = nodeList.get(index); 
					System.out.println("Node already exists: " + newNode);
				}else{
					nodeList.add(newNode); 
					System.out.println("Created Node: " + newNode);
				}
				
				newNode.setVisited(false);
				newNode.setOVArrayForMFrag(node.getMFragInstance().getMFragOrigin(), ovArrayMFrag);	
				newNode.setReceivedBallFromChild(true);
				newNode.addInputMFragInstance(node.getMFragInstance());
				node.addParent(newNode);
				
			} // end while iterator 
			
			ISSBNLogManager logManager = ssbn.getLogManager();
			
			//Add the context node parent if it exists
			if( contextParentsCount > 0 ){
				
				for(SimpleContextNodeFatherSSBNNode contextParent : contextNodeFatherList){
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_5, false, 
							node + " is child of the contextNode " + 
							contextParent.getContextNode().toString());
					}
					
					node.addContextParent(contextParent); 
				}
			}
			
		} //End-For Input Nodes 
		
		node.setEvaluatedTop(true);
		
	} // End method 	
	
	public INetworkMediator getMediator() {
		return mediator;
	}

	public void setLogEnabled(boolean isLogEnabled) {
		this.isLogEnabled = isLogEnabled;
	}

	public boolean isLogEnabled() {
		return this.isLogEnabled;
	}

	public int getLastIterationCount() {
		// not a iterative method, so it is just 1 iteration
		return 1;
	}

	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}

}
