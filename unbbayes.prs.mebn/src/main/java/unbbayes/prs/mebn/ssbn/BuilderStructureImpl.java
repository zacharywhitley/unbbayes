package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.resources.ResourcesSSBNAlgorithmLog;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.util.Debug;

/**
 * Build the Grand BN. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BuilderStructureImpl implements IBuilderStructure{

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
	
	/**@deprecated use {@link #newInstance()} instead*/
	protected BuilderStructureImpl(){
		
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
	}

	/*
	 * Evaluate a node, creating the necessary nodes and edges to mark it how 	
	 * finished. Two cases: 
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
		
		//Note: In this implementation don't is averiguated if already have a equal MFragInstance. 
		
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
			logManager.printText(thisLevel2, true, "Unfinished node = " + node + " setted true");
			logManager.skipLine(); 
		}
	}
	
	/**
	 * Evaluate the fathers of a node in a MFrag. 
	 * 
	 * @param ssbnNode
	 * @param mFragInstance
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private void evaluateMFragInstance(MFragInstance mFragInstance, SimpleSSBNNode ssbnNode) 
	      throws ImplementationRestrictionException, SSBNNodeGeneralException{
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		if (logManager != null) {
			logManager.printText(level2, true, "Evaluate MFragInstance" + mFragInstance); 
		}
		
		// 1) Test if the MFragInstance already was evaluated
		if(mFragInstance.isEvaluated()){
			return; //Be careful here if you change the algorithm for search for
			        //mFragInstances equal to the that are evaluated... In this case
			        //maybe a new evaluation of the mfrag will be necessary 
			        //because the enter node should be other. 
		}
		
	    // 2) Evaluate MFragInstance context
		try {
			level3 = new IdentationLevel(level2); 
			if (logManager != null) {
				logManager.printText(level3, true, "Evaluate MFrag Context Nodes");
			}
			evaluateMFragContextNodes(mFragInstance);
			
		} catch (ImplementationRestrictionException e) {
			//Stop the evaluation when the context nodes fail. 
			//TODO warning... the evaluation continue, using the default distribution
//			throw e; 
//		} catch (OVInstanceFaultException e) {
//			throw new ImplementationRestrictionException(e.getMessage()); 
		} catch (MFragContextFailException e) {
			//TODO warning... the evaluation continue, using the default distribution
//			throw new SSBNNodeGeneralException(e.getMessage()); 
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
	 * <b>
	 * 
	 * Pre-Requisites: <b>
	 * The context node already are evaluated. <b>
	 * 
	 * Pos-Requisites: <b>
	 * set the mFragInstance of the node<b>
	 * Set the node how finished<b>
	 * set the mFragInstance how finished (all nodes parents necessary are generated)<b>
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
		
		//--- 1) Evaluate if the node is a finding. 
		
		IResidentNode resident = node.getResidentNode(); 
		
		List<OVInstance> argumentList = new ArrayList<OVInstance>(); 
		
		for(int i = 0; i < node.getOvArray().length; i++){
			argumentList.add(OVInstance.getInstance(node.getOvArray()[i], node.getEntityArray()[i])); 
		}
		
		StateLink exactValue = kb.searchFinding(
				node.getResidentNode(), argumentList);
		
		/*
		 * Nota: como o algoritmo apenas sobe, é necessário continuar a avaliação
		 * acima mesmo quando o nó for setado como um finding, pois acima dele pode
		 * ter uma query ou nó que influência a query. (Caso fosse feita a avaliação
		 * acima e abaixo, não seria necessária esta subida, mas o algoritmo seria 
		 * mais complexo). Isto gerará um monte de nós candidados a serem excluidos
		 * no próximo passo.  
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
		
		
		//---- 2) Create the parents of node from the resident nodes
		
		//If the context node of the MFrag don't are evaluated, the creation of 
		//the parents isn't possible
		if(mFragInstance.isUseDefaultDistribution()){
			
			if (logManager != null) {
				logManager.printText(level4, false, " -> Node can't be evaluated: mfrag using default distribution");
			}
			return; 
		
		}
		
		OrdinaryVariable[] ovFilledArray = node.getOvArray(); 
		LiteralEntityInstance[] entityFilledArray = node.getEntityArray(); 
	
		if (logManager != null) {
			logManager.printText(level4, false, " 1) Evaluate the resident node parents");
		}
		
		for(ResidentNode residentNodeParent: resident.getResidentNodeFatherList()){
			
			List<SimpleSSBNNode> createdNodesList = createParents(node, 
					ovFilledArray, entityFilledArray, residentNodeParent);
			
			if (logManager != null) {
				logManager.printText(level4, false, "Resident parents generates from the resident node " + 
					residentNodeParent);
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
			
			if(inputNodeParent.getResidentNodePointer().getResidentNode().equals(resident)){
				//Special case: the recursivity.
				if (logManager != null) {
					logManager.printText(level4, false, " Recursivity treatment: " + resident);
				}
				
				SimpleSSBNNode newNode = createRecursiveParents(node, ovFilledArray, 
						entityFilledArray, inputNodeParent);
				
				if(newNode != null){
					evaluateNodeInMFragInstance(mFragInstance, newNode); 
				}
				
			}else{
				List<SimpleSSBNNode> createdNodesList = createParents(node, ovFilledArray, 
						entityFilledArray, inputNodeParent);	
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
	 * @param ovFilledArray           Ordinary variables that already are instanciated
	 * @param entityFilledArray       Entity values for the ordinary variablese of the ovFilledArray
	 * @param residentNodeParent      Parent to be evaluated
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
			ResidentNode residentNodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		//fix a unknown Bug (mistic)... 
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
	private  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
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
	private  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
			JacketNode nodeParent) throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
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
		
		//Create the new node... We pass by each combination for the ov fault list. 
		//Note that if we don't have any ov fault, we should have one stub element
		//in the possibleCombinationsForOvFaultList for pass for this loop one time. 
		for(String[] possibleCombination: possibleCombinationsForOvFaultList){
			
//			Debug.println("Combination=" + possibleCombination);
			
			SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(
					nodeParent.getResidentNode()); 
			
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
			
			newNode = addNodeToMFragInstance(node, newNode); 
			
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

	
	//The format of the MFrag in the recursion is: 
	//      INPUTNODE_A(NODE_A) PAI NODE_A 
	//That follows: 
	//       RESIDENTCHILD = NODE_A
	// (RESIDENTCHILD is the reference of the input INPUTNODE_A
	
	private  SimpleSSBNNode createRecursiveParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
			InputNode inputNodeParent) 
	             throws ImplementationRestrictionException, 
	                    SSBNNodeGeneralException {
		
		ResidentNode residentNode = node.getResidentNode(); 
		
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
	
		LiteralEntityInstance ovOrdereableActualValue = node.getEntityForOv(ovOrdereable); 
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
			
			LiteralEntityInstance ovOrdereablePreviusValue = 
				LiteralEntityInstance.getInstance(prev.getName(), ovOrdereable.getValueType());

			//3) Mount the father 

			/*
			 * Nota: uma pequena restrição aqui (fácil de ser retirada entretanto):
			 * Consideramos que o nó pai e o nó filho possuem os mesmos argumentos com 
			 * excessão do argumento recursivo. Isto mantém a compatibilidade com as 
			 * considerações feitas no algoritmo anterior.  
			 */ 

			SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(residentNode); 
			for(int i = 0; i < node.getOvArray().length; i++){
				if(node.getOvArray()[i].equals(ovOrdereable)){
					newNode.setEntityForOv(node.getOvArray()[i], ovOrdereablePreviusValue); 
				}else{
					newNode.setEntityForOv(node.getOvArray()[i], node.getEntityArray()[i]); 
				}
			}

			if(numberNodes > maxNumberNodes){
				//TODO define exception
				throw new SSBNNodeGeneralException("Max of nodes created: " + numberNodes); 
			}
			newNode = addNodeToMFragInstance(node, newNode);

			return newNode; 
		
		}else{
			return null; 
		}
		
	}

	/**
	 * Add the father node to the MFragInstance of the child node and add the link
	 * between the two nodes. Verify also if the father (newNode) already exists 
	 * in the ssbn, take the alread exist object if positive (return it). 
	 * 
	 * @param child  The node alread present in mFragInstance 
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

	/**
	 * Evaluate the context nodes of a MFrag using the ordinary variables already
	 * instanciated. <b>
	 * 
	 * - Ordinary variables don't instanciated yet will be instanciated. <b>
	 * - Should have more than one reference for a ordinary variable <b>
	 * - Should have reference uncertainty problem (how return this problem) <b>
	 * - Should have ordinary variables that don't have instance for it <b>
	 * 
	 * Cases: 
	 * - Trivial case
	 * - Simple Search (one entity for ov)
	 * - Compost Search (more than one entity)
	 * - Undefined Context (more than one possible result)
	 * 
	 * @param mfrag MFrag evaluated
	 * @param ovInstances Ordinary variables already instanciated. 
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
					logManager.printText(level5, false, "All ov's of the context node are setted");
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
							if (logManager != null) {
								logManager.printText(level5, false, resultOv);
							}
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

//					ssbn.getLogManager().printText(level5, false, "Try 2: Use the iteration strategy");
//					ssbn.getLogManager().printText(level5, false, "...still not implemented.\n");
					
					//---> 3) Use the Interation with user Strategy. 
					//TODO To be developed yet... 
					
					//Note: if the user add new variables, this should alter the result
					//of previous avaliations... maybe all the algorithm should be
					//evaluated again. A solution is only permit that the user
					//add a entity already at the knowledge base. The entity added 
					//should be put again the evaluation tree for verify possible
					//inconsistency. 
					
//					notInstanciatedOVList = mFragInstance.getListNotInstanciatedOV(); 
//					Debug.println("\nOVInstances don't found = " + notInstanciatedOVList.size());
//					for(OrdinaryVariable ov: notInstanciatedOVList){
//						Debug.println(ov.getName());
//					}
//					if (notInstanciatedOVList.size() != 0){
//						Debug.println("Try 2: Use the iteration aproach");
//						for(OrdinaryVariable ov: notInstanciatedOVList){
//							if(interationHelper!=null){
//								OVInstance ovInstance = interationHelper.getInstanceValueForOVFault(ov);
//								if(ovInstance != null){
//									mFragInstance.addInstanciatedOV(ovInstance.getOv(),	ovInstance.getEntity()); 
//								}
//							}
//						}
//					}

					//---> 4) Use the uncertainty Strategy. 
					if (logManager != null) {
						logManager.printText(level5, false, 
							"Try 2: Use the uncertain reference strategy");
					}
					
					//Utilized only in the specific case z = RandomVariable(x), 
					//where z is the unknow variable. (Should have only one unknow variable)
					
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
			//consider all the possible values. Note the use of the Closed Word
			//Asspetion. 
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
		
		List<String[]> possibleCombinationsList = mFragInstance.recoverAllCombinationsEntitiesPossibles(); 
		if (logManager != null) {
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
		List<LiteralEntityInstance> list =  avaliator.searchEntitiesForOrdinaryVariable(ovFault); 
		
		if((list == null)||(list.size()==0)){
			return null; 
		}else{
			result = new ArrayList<String>(); 
			for(LiteralEntityInstance lei: list){
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
			
			throw new RuntimeException(e.getMessage()); 
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
	
}



