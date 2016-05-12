package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.IBuilderStructure;
import unbbayes.prs.mebn.ssbn.ILiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.IMFragContextNodeAvaliator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.MFragContextNodeAvaliator;
import unbbayes.prs.mebn.ssbn.MFragInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleContextNodeFatherSSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/** 
 * Build SSBN Grand-BN based on Bayes-Ball Algorithm. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class BayesBallStructureBuilder implements IBuilderStructure{

	private KnowledgeBase kb; 
	
	private SSBN ssbn; 
	
	private ISSBNLogManager logManager ;
	
	private IMFragContextNodeAvaliator mFragContextNodeAvaliator;
	
	private List<BayesBallNode> nodeList; 
	
	private BayesBallStructureBuilder(){
	}
	
	public static IBuilderStructure newInstance(){
		return new BayesBallStructureBuilder(); 
	}
	
	/** 
	 * Build the structure of the SSBN Network. Evaluate the nodes marked unvisited
	 * until the list of unvisited nodes be empty. The evaluated node received the 
	 * bayes-ball from a parent of a child and, following the rules of Bayes-Ball
	 * algorithm, pass the ball to all children, all parents or block it. 
	 */
	public void buildStructure(SSBN _ssbn) throws ImplementationRestrictionException, 
	                                              SSBNNodeGeneralException{

		Debug.println("Build Structure");
		
		this.ssbn = _ssbn; 
		this.kb = ssbn.getKnowledgeBase(); 
		this.logManager = ssbn.getLogManager(); 
		this.mFragContextNodeAvaliator = new MFragContextNodeAvaliator(ssbn); 

		//All nodes 
		nodeList = new ArrayList<BayesBallNode>();  //all nodes visualized

		//Scheduled nodes 
		List<BayesBallNode> unvaluedNodeList = new ArrayList<BayesBallNode>(); 

		BayesBallNode targetNode = (BayesBallNode)ssbn.getQueryList().get(0).getSSBNNode(); 

		targetNode.setReceivedBallFromChild(true);

		unvaluedNodeList.add(targetNode); 
		nodeList.add(targetNode); 

		while (unvaluedNodeList.size() > 0){

			System.out.println(" ");
			System.out.println("-------------- NEW ITERATION -----------------");
			
			// This version of Bayes-Ball algorithm treat only probabilistic nodes 
			for(BayesBallNode node: unvaluedNodeList){
				
				Debug.println(" ");
				Debug.println("Evaluating node: " + node);
				Debug.println(" ");
				
				//Evaluated Node: 
				//Here, we have to: 
				// - Recover MFragInstance 
				// - evaluate context nodes and set node as default distribution 
				// - Verify if node is a finding 
				try {
					evaluateNode(node);
				} catch (MFragContextFailException e) {
					// TODO Auto-generated catch block
					// TODO throws exception or set default distribution
					e.printStackTrace();
					node.setDefaultDistribution(true);
				} 

				node.setVisited(true);
				
				if(node.isDefaultDistribution()){
					continue; 
				}

				//Receiving ball from child 	
				//  Node isn't a finding:  
				//  -> pass ball to parents 
				//  -> pass ball to children 
				//  Node is a finding (observed):  
				//  -> block 
				
				if (node.isReceivedBallFromChild()){
					Debug.println("Node " + node + " is receiving ball from child.");
					if (!node.isObserved()){
						if(!node.isEvaluatedTop()){
							evaluateTop(node);
						}
						if(!node.isEvaluatedBottom()){
							evaluateBottom(node);
						}
					}
				}

				//Receiving ball from parent 
				//  Node isn't a finding:  
				//  -> pass ball to children 
				//  Node is a finding (observed):
				//  -> pass ball to parents 
				
				if (node.isReceivedBallFromParent()){
					Debug.println("Node " + node + " is receiving ball from parent.");
					if (!node.isObserved()){
						if(!node.isEvaluatedBottom()){
							evaluateBottom(node);
						}
					} else{
						if(!node.isEvaluatedTop()){
							evaluateTop(node);
						}
					}
				}
			} //For

			//Schedule nodes to be evaluated in the next iteration 
			unvaluedNodeList.clear(); 
			Debug.println("");
			Debug.println("----------------------------------");
			Debug.println("Updating list of unvaluated nodes");
			for(BayesBallNode node: nodeList){
				if(!node.isVisited()){
					Debug.println(node + " UP " + 
							node.isEvaluatedBottom() + " DOWN " + node.isEvaluatedTop() + 
							" CHILD " + node.isReceivedBallFromChild() + " FATH " + node.isReceivedBallFromParent());
					unvaluedNodeList.add(node); 
				}
			}
			Debug.println("------------------------------------");
			Debug.println("");			
		} //While

		Debug.println("");
		Debug.println("Evaluation finished!!!");

		Debug.println("");
		Debug.println("Final list of Nodes:");
		
		for(BayesBallNode node: nodeList){
			
			ssbn.addSSBNNode(node);
			
			Debug.println(node + " UP " + 
					node.isEvaluatedBottom() + " DOWN " + node.isEvaluatedTop() + 
					" CHILD " + node.isReceivedBallFromChild() + " FATH " + node.isReceivedBallFromParent());
			
			for(INode nodeChild: node.getParentNodes()){
				Debug.println("   > " + nodeChild);
			}
		}

	}
	
	/**
	 * Verify if the node is a finding and evaluate its MFragInstance 
	 * 
	 * @param node
	 * @throws SSBNNodeGeneralException
	 * @throws MFragContextFailException
	 */
	private void evaluateNode(BayesBallNode node) throws SSBNNodeGeneralException, 
	                                                     MFragContextFailException{
		
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
		
		if(exactValue != null){
			node.setState(exactValue.getState());
			node.setObserved(true);
			ssbn.addFindingToFindingList(node); 
			Debug.println("Node is a finding. Exact Value = " + exactValue.getState());
			
		}
		
		//------------------------------------------------------------------------------
		// Step 2: Evaluate MFragInstance 
		//------------------------------------------------------------------------------
		
		if(node.getMFragInstance()!=null){
			
			Debug.println("Node already have a MFrag Instance: " + node.getMFragInstance());
			
			if(node.getMFragInstance().isUsingDefaultDistribution()){
				node.setDefaultDistribution(true);
				Debug.println("MFrag Instance using default distibution: node setted to default distribution.");
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
			
			if (mFragInstance.isUsingDefaultDistribution()){
				node.setDefaultDistribution(true);
				Debug.println("MFrag Instance using default distibution: node setted to default distribution.");
			}
			
		}
	}

	private MFragInstance createAndEvaluateMFragInstance(MFrag mFrag, List<OVInstance> ovInstanceList)
			throws SSBNNodeGeneralException, MFragContextFailException {
		
		MFragInstance mFragInstance = MFragInstance.getInstance(mFrag); 
		
		Debug.println("Created new MFrag Instance for MFrag " + mFrag );
		
		//TODO Change for this point don't throw more a MFragContextFailException
		for(OVInstance ovi: ovInstanceList){
			mFragInstance.addOVValue(ovi.getOv(), ovi.getEntity().getInstanceName());
		}
				
		//TODO We should verify if we already have a MFragInstance with the filled arguments 
		
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
			e.printStackTrace();
			Debug.println("MFrag Instance using default distribution");
			
		} catch (MFragContextFailException e) {
			//TODO warning... the evaluation continue, using the default distribution
//				throw new SSBNNodeGeneralException(e.getMessage()); 
			e.printStackTrace();
			mFragInstance.setUseDefaultDistribution(true); 
			Debug.println("MFrag Instance using default distribution");
		
		} 	
		
		mFragInstance.setEvaluated(true);
		
		return mFragInstance; 
	}	
	
	/**
	 * Evaluate nodes under the current node 
	 * 
	 * @param nodeList
	 * @param node
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	private void evaluateBottom(BayesBallNode node) throws SSBNNodeGeneralException, 
	                                                       ImplementationRestrictionException {
		
		Debug.println("Evaluate bottom");
		
		/* ***************************************************************************
		/*                      RESIDENT NODES IN SOME MFRAGS 
		 * ***************************************************************************/
		
		for(ResidentNode childrenNode: node.getResidentNode().getResidentNodeChildList()){
			
			//*** NEW NODE CREATED ***
			
			//This algorithm accept only one context node parent for node 
			int contextParentsCount = 0; 
			
			List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 

			//Fill the ovFault list, in relation with the child node. The ordinary
			//variables of the MFragInstance will be used forward. 
			for(OrdinaryVariable ov2: childrenNode.getOrdinaryVariableList()){
				
				boolean find = false; 
				
				for(OrdinaryVariable ov: node.getOvArray()){
					if(ov2.equals(ov)){
						find = true; 
						break; 
					}
				}
				
				if(!find){
					Debug.println("OV Fault = " + ov2);
					ovFaultForNewNodeList.add(ov2); 
				}
				
			} // End-for getOrdinaryVariableList	
			
			
			//Verify if already have a context node parent for this resident node 
			List<SimpleContextNodeFatherSSBNNode> contextNodeFatherList = 
					new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
			
			List<String[]> possibleCombinationsForOvFaultList = new ArrayList<String[]>(); 
			
			if(ovFaultForNewNodeList.size() > 0){
				
				for(OrdinaryVariable ov: ovFaultForNewNodeList){
					
					SimpleContextNodeFatherSSBNNode contextNodeFather = 
						node.getMFragInstance().getContextNodeFatherForOv(ov); 
					
					Debug.println("Context node for ov " + ov + ":" + contextNodeFather);
					
					if(contextNodeFather != null){
						contextParentsCount++;
						contextNodeFatherList.add(contextNodeFather); 

						if(contextParentsCount > 1)	{
							//In this implementation, only one context node parent is accept 
							throw new ImplementationRestrictionException(
									ImplementationRestrictionException.MORE_THAN_ONE_CTXT_NODE_SEARCH); 
						}
						
					} //Other side, the ov was solved using the IsA nodes. 
				} //end-for ovFaultForNewNodeList
				
				//Mount the combination of possible values for the ordinary variable fault

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
			
			//Create the new node... We pass by each combination of the ov fault list. 
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
							newNode.setEntityForOv(ov2, 
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
					System.out.println("Node already exist(F): " + newNode);
				}else{		
					nodeList.add(newNode); 
					newNode.setMFragInstance(node.getMFragInstance());
					System.out.println("Created Node(F): " + newNode);
				}
				
				//Set reference for the ordinary variables list in input MFrag 
				newNode.setVisited(false);
				newNode.setReceivedBallFromParent(true);
				newNode.addParent(node);

			} // end while iterator 			
		}
		
		/* ***************************************************************************
		/*                      RESIDENT NODES IN OTHER MFRAGS 
		 * ***************************************************************************/
		
		List<MFrag> mFragAlreadyEvaluatedList = new ArrayList<MFrag>(); 
		
		for(InputNode inputNode: node.getResidentNode().getInputInstanceFromList()){
			
			//*************************************************************************
			//*                          RECURSIVITY 
			//*************************************************************************
			
			if (inputNode.getMFrag().equals(node.getMFragInstance().getMFragOrigin())){

				System.out.println("Treat recursivity below. ");

				ResidentNode residentNodeChild = inputNode.getResidentNodePointer().getResidentNode(); 
				List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 

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

				if(ovFaultForNewNodeList.size()> 0){
					
					if (logManager != null) {
						logManager.printText(IdentationLevel.LEVEL_4, 
								false, " Recursivity treatment: " + node.getResidentNode());
					}

					// Check if there is one of the not filled ordinary variable 
					// tagged as "ordereable". 
					boolean isOrdered = false;
					for (OrdinaryVariable ov : ovFaultForNewNodeList) {
						if (ov.getValueType().hasOrder()) {
							isOrdered = true;
							break;
						}
					}

					if (isOrdered) {
						//CREATION OF NEW NODES: POINT 2/4 
						//Assume that one of the arguments has linear order property 
						// (i.e. automatically assume T0 < T1 < T2 < ...)
						BayesBallNode newNode = createRecursiveChildren(node, 
								node.getOvArray(), 
								node.getEntityArray(), 
								inputNode);

						if(newNode != null){

							if(nodeList.contains(newNode)){
								int index = nodeList.indexOf(newNode);
								newNode = nodeList.get(index); 
								System.out.println("Node already exists (A): " + newNode);
							}else{
								nodeList.add(newNode); 
								System.out.println("Created Node(A): " + newNode);
							}

							newNode.setVisited(false);
							newNode.setReceivedBallFromParent(true);

							//TODO verify which method utilize. 
//							newNode.addInputMFragInstance(node.getMFragInstance());
							newNode.setMFragInstance(node.getMFragInstance());

							newNode.addParent(node);
						}
						
						break; 
						
					} //isOrdered
				} //ovFault > 0  //TODO ovFault pode já estar preenchido em algum caso? 
			} // same MFrag 
			
			
			boolean testDoubleInputNode = false; 
			
			// Verify if already have an input node for this node in the input MFrag. 
			// If yes, and if some of the ordinary variable of the new input node is 
			// different of the ov of the old input node, mark testDoubleInputNode true. 
			if(node.getOvArrayForMFrag(inputNode.getMFrag()) != null){
				
				MFrag mFragInputNode = inputNode.getMFrag(); 
				
				for (int i = 0 ; i < node.getOvArrayForMFrag(mFragInputNode).length; i++){
					
					if (!(node.getOvArrayForMFrag(mFragInputNode)[i].equals(
							inputNode.getOrdinaryVariableByIndex(i)))){
						
						testDoubleInputNode = true;
						
						System.out.println("Double Input Node for " + inputNode);
						
						break; //abort on first ov different 
					}
				}
			}

			// When we have two input nodes of the same resident node in one MFrag, 
			// we need create only on of it. The other should be created originated
			// by the context nodes of the MFrag. 
			if (testDoubleInputNode) break; 
			
			//TODO Verify! The evaluation already finish on testDoubleInputNode
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
			
			Debug.println("Evaluate children of input node");
			//Create node for resident nodes children of input node. 
			for(ResidentNode residentNodeChild: inputNode.getResidentNodeChildList()){
				
				Debug.println("Resident node children: " + residentNodeChild );
				
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
						Debug.println("OV Fault = " + ov2);
						ovFaultForNewNodeList.add(ov2); 
					}
				} // End-for ov2	
				
				//Mount the combination of possible values for the ordinary variable fault
				List<String[]> possibleCombinationsForOvFaultList = new ArrayList<String[]>(); 
				
				List<SimpleContextNodeFatherSSBNNode> contextNodeFatherList = 
						new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
				
				if(ovFaultForNewNodeList.size() > 0){
					
					for(OrdinaryVariable ov: ovFaultForNewNodeList){
						
						SimpleContextNodeFatherSSBNNode contextNodeFather = 
							mFragInstance.getContextNodeFatherForOv(ov); 
						Debug.println("Context node for ov " + ov + ":" + contextNodeFather);
						
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
					
//					System.out.println("Created node: " + newNode);
					
					if(nodeList.contains(newNode)){
						int index = nodeList.indexOf(newNode);
						newNode = nodeList.get(index); 
						System.out.println("Node already exists (B): " + newNode);
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
					
//					System.out.println("Created Node: " + newNode);

				} // end while iterator 
				
			}//End-for childNode 
			
		}
		
		node.setEvaluatedBottom(true);
		
	}

	private void evaluateTop(BayesBallNode node) 
			throws ImplementationRestrictionException, 
			       SSBNNodeGeneralException {
		
		System.out.println("Evaluate top");
		
		/* ***************************************************************************
		/*                              RESIDENT NODES
		 * ***************************************************************************/
		
		for(ResidentNode fatherNode: node.getResidentNode().getResidentNodeFatherList()){
			
			//TODO InputNode also is a Resident Node Father??? Analyse!!! 
			if(fatherNode.equals(node.getResidentNode())){
				break; 
			}
			
			System.out.println("Father Node=" + fatherNode);
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
					} //In other case the ov was solved using the IsA nodes. 
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
			
			while (iterator.hasNext()){	
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
					System.out.println("Node already exists (C): " + newNode);
				}else{
					nodeList.add(newNode); 
					System.out.println("Created Node(C): " + newNode);
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
			
			int contextParentsCount = 0; 
			List<OrdinaryVariable> ovFaultForNewNodeList = new ArrayList<OrdinaryVariable>(); 
			
			//TODO Analyze this verification 
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
			
			
			//*****************************************************************
			//*                        RECURSIVITY                            *
			//*****************************************************************
			
			if ((ovFaultForNewNodeList.size() > 0) && 
			    (fatherInputNode.getResidentNodePointer().getResidentNode().equals(
					node.getResidentNode()))){
				
				if (logManager != null) {
					logManager.printText(IdentationLevel.LEVEL_4, 
							false, " Recursivity treatment: " + node.getResidentNode());
				}
				
				// Check if there is one of the not filled ordinary bariable 
				// is  tagged as "ordereable". 
				boolean isOrdered = false;
				for (OrdinaryVariable ov : ovFaultForNewNodeList) {
					if (ov.getValueType().hasOrder()) {
						isOrdered = true;
						break;
					}
				}
				
				if (isOrdered) {
					//CREATION OF NEW NODES: POINT 2/4 
					//Assume that one of the arguments has linear order property 
					// (i.e. automatically assume T0 < T1 < T2 < ...)
					BayesBallNode newNode = createRecursiveParent(node, 
							node.getOvArray(), 
							node.getEntityArray(), 
							fatherInputNode);
					
					if(newNode != null){
						
						if(nodeList.contains(newNode)){
							int index = nodeList.indexOf(newNode);
							newNode = nodeList.get(index); 
							System.out.println("Node already exists (D): " + newNode);
						}else{
							nodeList.add(newNode); 
							System.out.println("Created Node(D): " + newNode);
						}
	
						newNode.setVisited(false);
						newNode.setReceivedBallFromChild(true);
						
						//TODO verify which method utilize. 
//						newNode.addInputMFragInstance(node.getMFragInstance());
						newNode.setMFragInstance(node.getMFragInstance());
						
						node.addParent(newNode);
					}
					
					System.out.println("Vai sair do loop!");
					break; //for 
				}
				
			} // IF (recursivity) 
			
			System.out.println("Ops! Não saiu não!");
			
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
					System.out.println("Node already exists (E): " + newNode);
				}else{
					nodeList.add(newNode); 
					System.out.println("Created Node(E): " + newNode);
				}
				
				newNode.setVisited(false);
				newNode.setOVArrayForMFrag(node.getMFragInstance().getMFragOrigin(), ovArrayMFrag);	
				newNode.setReceivedBallFromChild(true);
//				newNode.addInputMFragInstance(node.getMFragInstance());
				node.addParent(newNode);
				
			} //end while iterator 
			
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
		

	/**
	 * Create recursive parents of a node (only for nodes with only a ordereable 
	 * ordinary variable). 
	 * 
	 * @param oldNode
	 * @param ovFilledArray
	 * @param entityFilledArray
	 * @param inputNodeParent
	 * @return
	 * @throws ImplementationRestrictionException
	 * @throws SSBNNodeGeneralException
	 */

	private  BayesBallNode createRecursiveParent(SimpleSSBNNode oldNode,
			                                       OrdinaryVariable[] ovFilledArray, 
			                                       ILiteralEntityInstance[] entityFilledArray,
			                                       InputNode inputNodeParent) 
	                        throws ImplementationRestrictionException, 
	                               SSBNNodeGeneralException {
		
		ResidentNode residentNode = oldNode.getResidentNode(); 
		
		System.out.println("Create Recursive Parent");
		
		//1) FIND THE ENTITY ORDEREABLE 

		if(residentNode.getOrdinaryVariablesOrdereables().size() != 1){
			if(residentNode.getOrdinaryVariablesOrdereables().size() > 1){
				throw new ImplementationRestrictionException(
						ImplementationRestrictionException.MORE_THAN_ONE_ORDEREABLE_VARIABLE);
			}else{
				throw new ImplementationRestrictionException(
						ImplementationRestrictionException.RV_NOT_RECURSIVE);
			}
		}
		
		OrdinaryVariable ovOrdereable = residentNode.getOrdinaryVariablesOrdereables().get(0); 
		
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
			System.out.println("nameEntity: " + nameEntity);
			throw new SSBNNodeGeneralException();
		}
		
		ObjectEntityInstanceOrdereable previousElement = objectEntityInstanceOrdereable.getPrev(); 

		if(previousElement != null){

			if (ssbn.getLogManager() != null) {
				ssbn.getLogManager().printText(IdentationLevel.LEVEL_5, false, 
						"Previous node of " + objectEntityInstanceOrdereable + " = " + previousElement );
			}
			
			ILiteralEntityInstance previousEntityValue = 
				LiteralEntityInstance.getInstance(previousElement.getName(), ovOrdereable.getValueType());

			//3) BUILD THE FATHER 

			// Note: the parent and children have to have the same arguments,  
			// with exception of the recursive ordinary variable.  
			// (This keep the compatibility with the considerations of the previous
			// implemented algorithm).  
			
			BayesBallNode newNode = BayesBallNode.getInstance(residentNode); 
			
			for(int i = 0; i < oldNode.getOvArray().length; i++){
				
				// The new node is build by the resident node, and not for 
				// the input node. Because of this, its ordinary variable is the 
				// ov of the resident node. The input node is only a trick for 
				// model the recursion in the generative MFrag. 
				
				if(oldNode.getOvArray()[i].equals(ovOrdereable)){
					newNode.setEntityForOv(oldNode.getOvArray()[i], previousEntityValue); 
				}else{
					newNode.setEntityForOv(oldNode.getOvArray()[i], oldNode.getEntityArray()[i]); 
				}
				
			}
			
			//Set the ordinary variables for MFrag 
			OrdinaryVariable[] ovArrayMFrag = 
					new OrdinaryVariable[residentNode.getOrdinaryVariableList().size()];
			
			for(int i = 0; i < residentNode.getOrdinaryVariableList().size(); i++){
				ovArrayMFrag[i] = inputNodeParent.getOrdinaryVariableByIndex(i); 
			}
			newNode.setOVArrayForMFrag(residentNode.getMFrag(), ovArrayMFrag);

//			if(numberNodes > maxNumberNodes){
//				//TODO define exception
//				throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
//			}

			return newNode; 
		
		}else{
			return null; 
		}
		
	}
	
	private  BayesBallNode createRecursiveChildren(SimpleSSBNNode oldNode,
			OrdinaryVariable[] ovFilledArray, 
			ILiteralEntityInstance[] entityFilledArray,
			InputNode inputNodeParent) 
					throws ImplementationRestrictionException, 
					SSBNNodeGeneralException {

		ResidentNode residentNode = oldNode.getResidentNode(); 

		System.out.println("Create Recursive Children");

		//1) FIND THE ENTITY ORDEREABLE 

		if(residentNode.getOrdinaryVariablesOrdereables().size() != 1){
			if(residentNode.getOrdinaryVariablesOrdereables().size() > 1){
				throw new ImplementationRestrictionException(
						ImplementationRestrictionException.MORE_THAN_ONE_ORDEREABLE_VARIABLE);
			}else{
				throw new ImplementationRestrictionException(
						ImplementationRestrictionException.RV_NOT_RECURSIVE);
			}
		}

		OrdinaryVariable ovOrdereable = residentNode.getOrdinaryVariablesOrdereables().get(0); 

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
			System.out.println("nameEntity: " + nameEntity);
			throw new SSBNNodeGeneralException();
		}

		ObjectEntityInstanceOrdereable nextElement = objectEntityInstanceOrdereable.getProc(); 

		if(nextElement != null){

			if (ssbn.getLogManager() != null) {
				ssbn.getLogManager().printText(IdentationLevel.LEVEL_5, false, 
						"Proximous node of " + objectEntityInstanceOrdereable + " = " + nextElement );
			}

			ILiteralEntityInstance nextEntityValue = 
					LiteralEntityInstance.getInstance(nextElement.getName(), ovOrdereable.getValueType());

			//3) BUILD THE FATHER 

			// Note: the parent and children have to have the same arguments,  
			// with exception of the recursive ordinary variable.  
			// (This keep the compatibility with the considerations of the previous
			// implemented algorithm).  

			BayesBallNode newNode = BayesBallNode.getInstance(residentNode); 

			for(int i = 0; i < oldNode.getOvArray().length; i++){

				// The new node is build by the resident node, and not for 
				// the input node. Because of this, its ordinary variable is the 
				// ov of the resident node. The input node is only a trick for 
				// model the recursion in the generative MFrag. 

				if(oldNode.getOvArray()[i].equals(ovOrdereable)){
					newNode.setEntityForOv(oldNode.getOvArray()[i], nextEntityValue); 
				}else{
					newNode.setEntityForOv(oldNode.getOvArray()[i], oldNode.getEntityArray()[i]); 
				}

			}

			//Set the ordinary variables for MFrag 
			OrdinaryVariable[] ovArrayMFrag = 
					new OrdinaryVariable[residentNode.getOrdinaryVariableList().size()];

			for(int i = 0; i < residentNode.getOrdinaryVariableList().size(); i++){
				ovArrayMFrag[i] = inputNodeParent.getOrdinaryVariableByIndex(i); 
			}
			newNode.setOVArrayForMFrag(residentNode.getMFrag(), ovArrayMFrag);

			//if(numberNodes > maxNumberNodes){
			////TODO define exception
			//throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
			//}

			return newNode; 

		}else{
			return null; 
		}

	}	

}
