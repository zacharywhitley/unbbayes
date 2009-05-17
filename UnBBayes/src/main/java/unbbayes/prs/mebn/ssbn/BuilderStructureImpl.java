package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BuilderStructureImpl implements IBuilderStructure{

	private List<SimpleSSBNNode> notFinishedNodeList; 
	
	private KnowledgeBase kb; 
	
	private SSBN ssbn; 
	
	private boolean internalDebug = false; 
	
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
	 * 
	 * Pre-requisites
	 *     - All nodes of the SSBN are marked not finished. 
	 *     
	 * Pos-requisites
	 *     - All nodes of the SSBN are marked finished.     
	 */
	public void buildStructure(SSBN _ssbn) {
		
		notFinishedNodeList = new ArrayList<SimpleSSBNNode>();
		
		this.ssbn = _ssbn; 
		this.kb = ssbn.getKnowledgeBase(); 
		
		for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
//			System.out.println("     -> " + node);
			notFinishedNodeList.add(node);
		}		
		
		//Evaluate all the not finished nodes
		
		//Cases: 
		//Query and Findings nodes
		//Input nodes still don't evaluated 
		
		int iteration = 0; 
		
		while(!notFinishedNodeList.isEmpty()){
			ssbn.getLogManager().appendln("\n--------------------------------"); 
			ssbn.getLogManager().appendln("---   Iteration = " + iteration + "   ---");
			ssbn.getLogManager().appendln("--------------------------------\n"); 
			
			for(SimpleSSBNNode node: notFinishedNodeList){
		        try {
					evaluateUnfinishedRV(node);
				} catch (ImplementationRestrictionException e) {
					// TODO Auto-generated catch block 
					//TODO throw exceptions of this method. 
					e.printStackTrace();
				} catch (SSBNNodeGeneralException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
			//update the not finished list
			notFinishedNodeList.clear();

			ssbn.getLogManager().appendln("\nNot finished nodes list : ");
			
			for(SimpleSSBNNode node: ssbn.getSimpleSsbnNodeList()){
				if(!node.isFinished()){
					ssbn.getLogManager().appendln("     - " + node.toString());
					notFinishedNodeList.add(node); 
				}
			}
			
			iteration++; 
			
		}
		
		ssbn.getLogManager().appendln("\nNot finished nodes list : ");
		
		System.out.println("Finished");
	}

	/*
	 * Evaluate a node, creating the necessary nodes and edges to mark it how 	
	 * finished. Two cases: 
	 * - Generated by a input node (contains arguments for the input MFrag)
	 * - Generated by a finding/query node
	 */
	private void evaluateUnfinishedRV(SimpleSSBNNode node) throws ImplementationRestrictionException, 
	                  SSBNNodeGeneralException{
		
		ssbn.getLogManager().appendSectionTitle("\n---> Evaluate unfinished node = " + node );
		
		//Note: In this implementation don't is averiguated if already have a equal MFragInstance. 
		
		//Build the MFragInstance related to the node
		MFragInstance mFragInstance = MFragInstance.getInstance(node.getResidentNode().getMFrag()); 
		
		//Add the arguments
		for(int i = 0; i < node.getOvArray().length; i++){
			try {
				mFragInstance.addOVValue(node.getOvArray()[i], node.getEntityArray()[i].getInstanceName());
			} catch (MFragContextFailException e) {
				throw new SSBNNodeGeneralException(e.getMessage()); //a bug... 
			} 
		}
		
		node.setMFragInstance(mFragInstance); 
		
		//Evaluate the mFragInstances and create the nodes of its.
		evaluateMFragInstance(mFragInstance, node); 
		
		node.setFinished(true);
		
		ssbn.getLogManager().appendSectionTitle("---> Unfinished node = " + node + " setted true\n");
		
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

		ssbn.getLogManager().appendSectionTitle("\nEvaluate MFragInstance " + mFragInstance);
		
		// 1) Test if the MFragInstance already was evaluated
		if(mFragInstance.isEvaluated()){
			return; 
		}
		
	    // 2) Evaluate MFragInstance context
		try {
			
			evaluateMFragContextNodes(mFragInstance);
			ssbn.getLogManager().appendSectionTitle("Context Nodes evaluateds");
		
		} catch (ImplementationRestrictionException e) {
			throw e; 
		} catch (SSBNNodeGeneralException e) {
			throw e; 
		} catch (OVInstanceFaultException e) {
			throw new ImplementationRestrictionException(e.getMessage()); 
		} 
		
		// 3) Create the nodes of the MFragInstance
		evaluateNodeInMFragInstance(mFragInstance, ssbnNode);
		
		ssbn.getLogManager().appendSectionTitle("Evaluate MFragInstance finished. \n");
		
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
		
		ssbn.getLogManager().appendln("Create parents of node " + node);
		
		node.setMFragInstance(mFragInstance); 
		
		//--- 1) Evaluate if the node is a finding. 
		
		ResidentNode resident = node.getResidentNode(); 
		
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
			
			ssbn.getLogManager().appendln("Node" + node + 
					" setted how a finding. Exact Value = " + exactValue.getState());
			
		}
		
		
		//---- 2) Create the parents of node from the resident nodes
		
		//If the context node of the MFrag don't are evaluated, the creation of 
		//the parents insn't possible
		if(mFragInstance.isUseDefaultDistribution()){
			
			ssbn.getLogManager().appendln("Node can't be evaluated: mfrag using default distribution");
			return; 
		
		}
		
		OrdinaryVariable[] ovFilledArray = node.getOvArray(); 
		LiteralEntityInstance[] entityFilledArray = node.getEntityArray(); 
	
		ssbn.getLogManager().appendln("Evaluate the resident node parents");
		
		for(ResidentNode residentNodeParent: resident.getResidentNodeFatherList()){
			
			List<SimpleSSBNNode> createdNodesList = createParents(node, 
					ovFilledArray, entityFilledArray, residentNodeParent);
			
			ssbn.getLogManager().appendln("Resident parents generates from the resident node " + 
					residentNodeParent);
			
			int count = 0; 
			for(SimpleSSBNNode newNode: createdNodesList){
				ssbn.getLogManager().appendln("Evaluate " + count + " - "+ newNode); 
				count= count + 1 ; 
				evaluateNodeInMFragInstance(mFragInstance, newNode); 
			}
			
		}
		
		//---- 3) Create the parents of node from the input nodes
		ssbn.getLogManager().appendln("Evaluate the input node parents");
		for(InputNode inputNodeParent: resident.getInputNodeFatherList()){
			
			if(inputNodeParent.getResidentNodePointer().getResidentNode().equals(resident)){
				//Special case: the recursivity.
				System.out.println("Recursivity treatment: " + resident);
				
				SimpleSSBNNode newNode = createRecursiveParents(node, ovFilledArray, 
						entityFilledArray, inputNodeParent);
				
				if(newNode != null){
					evaluateNodeInMFragInstance(mFragInstance, newNode); 
				}
				
			}else{
				List<SimpleSSBNNode> createdNodesList = createParents(node, ovFilledArray, 
						entityFilledArray, inputNodeParent);	
				
				System.out.println("Nodes createds from the input parents: ");
				for(SimpleSSBNNode newNode: createdNodesList){
					System.out.println("Node: " + newNode);
				}
			}
			
		}
		
		node.setFinished(true); 
		System.out.println("Node " + node + " setted finished");

		mFragInstance.setEvaluated(true); 
	}

	/**
	 * Cria SSBNNodes pais de um node para determinado nó residente. 
	 * 
	 * @param node                    Nó para o qual o pai serão avaliados
	 * @param ovFilledArray           Variaveis ordinarias que já possuem os seus valores definidos
	 * @param entityFilledArray       Valores definidos para as variáveis ordinarias de ovFilledArray
	 * @param residentNodeParent      Pai a ser avaliado. 
	 */
	private List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
			ResidentNode residentNodeParent) {
		
		//fix a unknown Bug (mistic)... 
		if(residentNodeParent.equals(node.getResidentNode())){
			return new ArrayList<SimpleSSBNNode>(); 
		}
		
		JacketNode nodeParent = new JacketNode(residentNodeParent); 
		
		return createParents(node, ovFilledArray, entityFilledArray, nodeParent); 
	}

	/**
	 * Cria SSBNNodes pais de um node para determinado nó residente. 
	 * 
	 * @param node                    Nó para o qual o pai serão avaliados
	 * @param ovFilledArray           Variaveis ordinarias que já possuem os seus valores definidos
	 * @param entityFilledArray       Valores definidos para as variáveis ordinarias de ovFilledArray
	 * @param residentNodeParent      Pai a ser avaliado. 
	 */
	private  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
			InputNode inputNodeParent) {

		
		JacketNode nodeParent = new JacketNode(inputNodeParent); 
		
		return createParents(node, ovFilledArray, entityFilledArray, nodeParent); 
	}	
	
	private  List<SimpleSSBNNode> createParents(SimpleSSBNNode node,
			OrdinaryVariable[] ovFilledArray, LiteralEntityInstance[] entityFilledArray,
			JacketNode nodeParent) {
		
//		if(internalDebug){
//			System.out.println("[In] CreateParents");
//			System.out.println("[Arg] Node=" + node.getResidentNode().getName());
//			System.out.print("[Arg] ovFilledArray = [");
//			for(int i = 0; i < ovFilledArray.length; i++){
//				System.out.print(ovFilledArray[i] + " ");
//			}
//			System.out.println("]");
//			System.out.print("[Arg] entityFilledArray= [");
//			for(int i = 0; i < entityFilledArray.length; i++){
//				System.out.print(entityFilledArray[i].getInstanceName() + " ");
//			}
//			System.out.println("]");
//			System.out.println("[Arg] inputNodeParent= " + nodeParent.getResidentNode());
//		}
		
		List<SimpleSSBNNode> ssbnCreatedList = new ArrayList<SimpleSSBNNode>();
		
		int contextParentsCount = 0; 
		
		List<OrdinaryVariable> newNodeOvFaultList = new ArrayList<OrdinaryVariable>(); 
		
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
				newNodeOvFaultList.add(ov); 
			}
		}
		
		//Mount the combination of possible values for the ordinary variable fault
		List<String[]> possibleCombinationsForOvFaultList = new ArrayList<String[]>(); 
		
		List<SimpleContextNodeFatherSSBNNode> contextNodeFatherList = 
			new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
		
		if(newNodeOvFaultList.size()>0){
			
			for(OrdinaryVariable ov: newNodeOvFaultList){
				
				SimpleContextNodeFatherSSBNNode contextNodeFather = 
					node.getMFragInstance().getContextNodeFather(ov); 
				
				if(contextNodeFather != null){
					contextParentsCount++;
					contextNodeFatherList.add(contextNodeFather); 

					if(contextParentsCount > 1)	{
						//TODO Exception??? Is dificult treat more than one context parent? 
					}
				
				}
				
			}
			
			possibleCombinationsForOvFaultList = node.getMFragInstance().
			        recoverCombinationsEntitiesPossibles(
					    ovFilledArray, 
				      	entityFilledArray, 
					    newNodeOvFaultList.toArray(
                    		 new OrdinaryVariable[newNodeOvFaultList.size()])); 
			
			System.out.println("Possible combinations for ov fault: ");
			for(String[] combination: possibleCombinationsForOvFaultList){
				System.out.print("Combination: ");
				for(String entity: combination){
					System.out.print(entity + " ");
				}
			} 
			
			//Treat the uncertainty reference
			
			
		}else{
			possibleCombinationsForOvFaultList.add(new String[0]); //A stub element
		}
		
		//Create the new node... 
		for(String[] possibleCombination: possibleCombinationsForOvFaultList){
			
			SimpleSSBNNode newNode = SimpleSSBNNode.getInstance(
					nodeParent.getResidentNode()); 
			
			//1. Add the ovInstances of the children that the father also have 
			for(int i = 0; i < node.getOvArray().length; i++){
				
				//Para um nó de input IX1 referente ao nó resident RX1 devemos recuperar 
				//a variável ordinária da HomeMFrag de RX1 correspondente a V.O. 
				//da MFrag de IX1 para então setar-mos o ssbnNode. 
				
				OrdinaryVariable correspondentOV = 
					nodeParent.getCorrespondentOrdinaryVariable(node.getOvArray()[i]);
				
				if(correspondentOV != null){
					newNode.setEntityForOv(
							correspondentOV, 
							node.getEntityArray()[i]); 
				}
			}
			
			//2. Create the new OVInstances for the combination
			for(int index = 0; index < newNodeOvFaultList.size(); index++){
				
				OrdinaryVariable correspondentOV = 
					nodeParent.getCorrespondentOrdinaryVariable(newNodeOvFaultList.get(index));
				
				newNode.setEntityForOv(
						correspondentOV, 
						LiteralEntityInstance.getInstance(possibleCombination[index], 
								newNodeOvFaultList.get(index).getValueType())); 
			}
			
			newNode = addNodeToMFragInstance(node, newNode); 
			ssbn.getLogManager().appendln("Created new node: " + newNode);
			
			ssbnCreatedList.add(newNode); 
		}
		
		//Add the context node parent if it exists
		if( contextParentsCount > 0 ){
			for(SimpleContextNodeFatherSSBNNode contextParent : contextNodeFatherList){
				System.out.println("Node (context child) = " + node);
				node.addContextParent(contextParent); 
			}
		}
		
		return ssbnCreatedList; 
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

			System.out.println("Previous node = " + prev + " (" + objectEntityInstanceOrdereable + ")");
			
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
		
		parent = ssbn.addSSBNNodeIfItDontAdded(parent);
		
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
	 */
	public MFragInstance evaluateMFragContextNodes(MFragInstance mFragInstance) 
	                   throws ImplementationRestrictionException, 
	                          SSBNNodeGeneralException, 
	                          OVInstanceFaultException{
		
		ssbn.getLogManager().appendln("Evaluate MFrag Context Nodes for MFrag " + mFragInstance);
		
		//Consider that the tree with the know ordinary variables are already mounted. 
		//Consider that the only ordinary variables filled are the alread know OV
		List<OVInstance> ovInstances = mFragInstance.getOVInstanceList(); 
		
		for(ContextNode contextNode: mFragInstance.getContextNodeList()){
			
			ssbn.getLogManager().appendln(1, "Context Node: " + contextNode);
			
			//---> 1) Verify if the context node is soluted only with the know arguments. 
			List<OrdinaryVariable> ovInstancesFault = contextNode.getOVFaultForOVInstanceSet(ovInstances); 
			
			if(ovInstancesFault.size() == 0){
				
				ssbn.getLogManager().appendln(2, "All ov are setted"); 
				boolean result = kb.evaluateContextNodeFormula(contextNode, ovInstances);
				if(result){
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_OK); 
					ssbn.getLogManager().appendln("Evaluated OK");	
					continue; 
				
				}else{
					ssbn.getLogManager().appendln(2, "Context Node Evaluation fail"); 
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL);
					mFragInstance.setUseDefaultDistribution(true); 
					break; //TODO: the MFragInstance should continue to be evaluated?
				}
			}else{
			
				ssbn.getLogManager().appendln(2,"Evaluate with OV Fault");
				
				ssbn.getLogManager().appendln(2,"Try 1: Use the search strategy");
				
				//---> 2) Use the Entity Tree Strategy. 
				SearchResult searchResult = kb.evaluateSearchContextNodeFormula(contextNode, ovInstances); 

				if(searchResult!= null){  

//					System.out.println("Search Result: ");
//					for(String[] result: searchResult.getValuesResultList()){
//						for(int i = 0; i < result.length; i++){
//							System.out.print(result[i] + " "); 
//						}
//						System.out.println("");
//					}
//					
					//Result valid results: Add the result to the tree of result.
					try {

						mFragInstance.addOVValuesCombination(
								searchResult.getOrdinaryVariableSequence(), 
								searchResult.getValuesResultList());

						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_OK); 
						
						ssbn.getLogManager().appendln(2,"Evaluated OK");
						
					} catch (MFragContextFailException e) {
						e.printStackTrace(); 
						mFragInstance.setStateEvaluationOfContextNode(contextNode, ContextNodeEvaluationState.EVALUATION_FAIL); 
						mFragInstance.setUseDefaultDistribution(true); 
					}

					break;

				}else{

					ssbn.getLogManager().appendln(2,"Try 2: Use the iteration strategy");
					
					//---> 3) Use the Interation with user Strategy. 
					//TODO To be developed yet... 
					
					//Note: if the user add new variables, this should alter the result
					//of previous avaliations... maybe all the algorithm should be
					//evaluated again. A solution is only permit that the user
					//add a entity already at the knowledge base. The entity added 
					//should be put again the evaluation tree for verify possible
					//inconsistency. 
					
//					notInstanciatedOVList = mFragInstance.getListNotInstanciatedOV(); 
//					System.out.println("\nOVInstances don't found = " + notInstanciatedOVList.size());
//					for(OrdinaryVariable ov: notInstanciatedOVList){
//						System.out.println(ov.getName());
//					}
//					if (notInstanciatedOVList.size() != 0){
//						System.out.println("Try 2: Use the iteration aproach");
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
					ssbn.getLogManager().appendln(2,"Try 3: Use the uncertain reference strategy");

					//Utilized only in the specific case z = RandomVariable(x), 
					//where z is the unknow variable. (Should have only one unknow variable)
					
					SimpleContextNodeFatherSSBNNode simpleContextNodeFather = null; 
					
					if(ovInstancesFault.size() == 1){
						try{
							simpleContextNodeFather = 
								evaluateUncertaintyReferenceCase(mFragInstance, 
										contextNode, ovInstancesFault.get(0)); 
							if(simpleContextNodeFather!=null){
								break; //OK!!! Good!!! Yes!!! 
							}
						}
						catch(ImplementationRestrictionException e){
							ssbn.getLogManager().appendln(3,"Fail: " + e.getMessage());
						}
					}
					
					//--> 5) Nothing more to try... context fail
					ssbn.getLogManager().appendln(2,"Still ov fault... nothing more to do. " +
							"Use default distribution");
					mFragInstance.setStateEvaluationOfContextNode(contextNode, 
							ContextNodeEvaluationState.EVALUATION_FAIL); 
					mFragInstance.setUseDefaultDistribution(true);
					//TODO Maybe a warning... Not so drastic! 
					
				}
			}
			
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
	
}



