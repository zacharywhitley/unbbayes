package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import unbbayes.io.LogManager;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * 
 * @author Laecio Santos (laecio@gmail.com)
 * @author Shou Matsumoto (cardialfly@gmail.com)
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */
public abstract class AbstractSSBNGenerator implements ISSBNGenerator{

	public static LogManager logManager = new LogManager();

	private KnowledgeBase knowledgeBase; 

	//all the ssbn nodes created. 
	protected SSBNNodeList ssbnNodeList;
	
	protected Map<String, SSBNNode> ssbnNodesMap; 

	protected List<SSBNWarning> warningList; 
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	
	/**
	 * Generate the ssbn nodes parents of root and for the ssbn nodes parents of 
	 * findingsDown and child of root.
	 *  
	 * @param root
	 * @param findingsDown
	 * @throws MEBNException
	 * @throws SSBNNodeGeneralException 
	 */
	protected void generateCPTForAllSSBNNodes(SSBNNode root) throws MEBNException, SSBNNodeGeneralException{
		generateCPTForAllSSBNNodes(root, 0);
	}
	
	/*
	 * 1 - generate cpt for parents
	 * 2 - generate cpt for node
	 * 3 - generate cpt for children
	 */
	private void generateCPTForAllSSBNNodes(SSBNNode root, int level) throws MEBNException, SSBNNodeGeneralException{
		
//		logManager.appendln(getSpaceForLevel(level) + "Generate CPT for node " + root); 
//		logManager.appendln(getSpaceForLevel(level) + "Parents:"); 
		
		if(root.isCptAlreadyGenerated()){
			return; 
		}else{
			
			//------------------ PARENTS
			for(SSBNNode parent: root.getParents()){
				generateCPTForAllSSBNNodes(parent, level + 1); 
			}

			//------------------ NODE
//			logManager.appendln(getSpaceForLevel(level) + "CPT for root"); 
			if(root.isCptAlreadyGenerated()){
				return; 
			}
			generateCPT(root);
			root.setCptAlreadyGenerated(true); 

			//------------------ CHILDREN
//			logManager.appendln(getSpaceForLevel(level) + "Children:"); 
			for(SSBNNode child: root.getChildren()){
				generateCPTForAllSSBNNodes(child, level + 1); 
			}
		}
	}
	
	/**
	 * Generate the cpt for the ssbnNode
	 * 
	 * out-assertives:
	 * - The CPT of the probabilistic node referenced by the ssbnNode is setted
	 *   with the CPT generated. 
	 * @throws SSBNNodeGeneralException 
	 */
	private void generateCPT(SSBNNode ssbnNode) throws MEBNException, SSBNNodeGeneralException {
		
		//Change the ov of the fathers for the ov when it is a father. 
		//This is for solve the problem of recursion, when the SSBNNode 
		//recursive has one ordinary variable for the case that it is the
		//child and other ordinary variable for when it is father: 
		//
		//Ex: DistFromOwn(st, t) and DistFromOwn(st, tPrev). 
		
		ssbnNode.changeArgumentsToResidentMFrag(); 
		
		for(SSBNNode parent: ssbnNode.getParents()){
			parent.turnArgumentsForMFrag(ssbnNode.getResident().getMFrag()); 
		}
		
		logManager.appendln("---- CPT for node: " + ssbnNode.getUniqueName() + "-----"); 
		logManager.appendln("Parents:"); 
		for(SSBNNode parent: ssbnNode.getParents()){
			logManager.appendln(parent.toString()); 
		}
		logManager.appendln("Init"); 
		
		if(ssbnNode.isPermanent()){
			//Generate the cpt of the context father ssbnnode
			if(ssbnNode.getContextFatherSSBNNode()!=null){ 
				try {
					generateCPTForNodeWithContextFather(ssbnNode);
				} catch (InvalidOperationException e1) {
					e1.printStackTrace();
					throw new SSBNNodeGeneralException(e1.getMessage()); 
				}
			}else{
				ssbnNode.getCompiler().generateCPT(ssbnNode);
			}
		}
		
		logManager.appendln("End");
		
	}
	
	

			
	
	/**
	 * Remove of the network all nodes in what the atribute permanent = false. 
	 * 
	 * @param pn
	 */
	protected void removeNotPermanentNodes(Collection<SSBNNode> listSSBNNode){
		logManager.appendln("...Removing not permanent nodes begin");
		
		List<SSBNNode> nodesToDelete = new ArrayList<SSBNNode>(); 
		
		for(SSBNNode node: listSSBNNode){
			if(!node.isPermanent()){
				nodesToDelete.add(node); 
			}
		}
		
		for(SSBNNode node: nodesToDelete){
			logManager.appendln("...Removing node " + node.getName());
			node.delete(); 
			listSSBNNode.remove(node); 
		}
		
		logManager.appendln("...Removing not permanent nodes end");
	}

	
	/**
	 * Verifies if it has OVInstances for all ordinary variable. If it has, return 
	 * null, otherwise, return the list of ordinary variables that does not have an 
	 * OVInstance. 
	 * 
	 * @param ordVariableList
	 * @param ovInstanceList
	 * @return List of ordinary variables that does not have an OVInstance. 
	 */
	protected List<OrdinaryVariable> getOVInstancesForWhichNotExistOV(
			       Collection<OrdinaryVariable> ordVariableList, 
			       Collection<OVInstance> ovInstanceList){
    	
    	List<OrdinaryVariable> ovProblemList = new ArrayList<OrdinaryVariable>(); 
    	
    	for(OrdinaryVariable ov: ordVariableList){
    		boolean found = false; 
    		for(OVInstance ovInstance: ovInstanceList){
    			if(ov.equals(ovInstance.getOv())){
    				found = true; 
    				break; 
    			}
    		}
    		if(!found) ovProblemList.add(ov); 
    	}
    	
    	return ovProblemList; 
	}
	

	/**
	 * Search for a context node that recover the entities that match with the 
	 * ordinary variable list. 
	 * A) Case this search returns entities, one SSBNNode will be create for each entity (the entity is a argument of the fatherNode
	 * that is the node referenced by the SSBNNOde). 
	 * B) Case this search don't return entities, all the entities of the knowledge base will be recover and one SSBNNode
	 * will be create for each entity. Is this case a object ContextFatherSSBNNode 
	 * wiil be added in the ssbnnode orgin.  
	 * 
	 * Note: already put the created nodes in the lists of created nodes. 
	 * 
	 * @param mFrag
	 * @param originNode
	 * @param fatherNode
	 * @param ovList list of ov's for what don't have a value. (for this implementation, 
	 *                this list should contain only one element). 
	 * @param ovInstances
	 * 
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	protected List<SSBNNode> createSSBNNodesOfEntitiesSearchForResidentNode(
			MFrag mFrag, SSBNNode originNode, ResidentNode fatherNode, 
			List<OrdinaryVariable> ovList, List<OVInstance> ovInstances, 
			boolean searchIfNotFound) 
			throws ImplementationRestrictionException, SSBNNodeGeneralException {

		if(ovList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("OrdVariableProblemLimit")); 
		}
		
		OrdinaryVariable ovProblematic = ovList.get(0);
		
		//search
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovList);
		
		if(contextNodeList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeSearh") + ": " + contextNodeList); 
		}
		if(contextNodeList.size() < 1){
			throw new SSBNNodeGeneralException(resource.getString("NoContextNodeFather")); 
		}
		
		//contextNodeList have only one element
		ContextNode context = contextNodeList.toArray(new ContextNode[contextNodeList.size()])[0];

		//Complex case: evaluate search context nodes. 
		logManager.appendln("Context Node: " + context.getLabel()); 
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(knowledgeBase); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
			if(result.isEmpty()){
				if(searchIfNotFound){
					logManager.appendln("Evaluate Search Context Node for " + context + "[" + ovInstances + "]" + "return null"); 

					if(originNode.getContextFatherSSBNNode() != null){
						ContextFatherSSBNNode contextFatherSSBNNode = originNode.getContextFatherSSBNNode();

						if(contextFatherSSBNNode.getOvProblematic().equals(ovProblematic)){

							List<SSBNNode> nodes = new ArrayList<SSBNNode>();

							for(LiteralEntityInstance entity: contextFatherSSBNNode.getPossibleValues()){
								
								SSBNNode node =  createSSBNNodeForEntitySearch(originNode.getProbabilisticNetwork(), 
										fatherNode, ovInstances, ovProblematic, entity.getInstanceName());
								
								nodes.add(node);
								
							}

							return nodes;
						}
						else{
							throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeFather"));
						}
					}
					else{

						//THE XOR ALGORITH...

						/*
						 * All the instances of the entity will be considered and the 
						 * context node will be father. 
						 */

						List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 

						//Add the context node how father
						ContextFatherSSBNNode contextFatherSSBNNode = new ContextFatherSSBNNode(
								originNode.getProbabilisticNetwork(), context); 
						contextFatherSSBNNode.setOvProblematic(ovProblematic);

						originNode.setContextFatherSSBNNode(contextFatherSSBNNode);

						//in this implementation only this is necessary, because the treat
						//of context nodes how fathers will be """trivial""", using the XOR 
						//strategy. For a future implementation that accept different 
						//distributions for the residentNode of the ContextNode, the
						//arguments of the resident node will have to be filled with the OVInstances
						//for the analized of the resident node formula. (very complex!).  

						//Search for all the entities present in kb. 
						result = knowledgeBase.getEntityByType(ovProblematic.getValueType().getName());
						for(String entity: result){
							SSBNNode node =  createSSBNNodeForEntitySearch(originNode.getProbabilisticNetwork(), 
									fatherNode, ovInstances, ovProblematic, entity);
							contextFatherSSBNNode.addPossibleValue(LiteralEntityInstance.getInstance(entity, ovProblematic.getValueType()));
							nodes.add(node); 
						}					

						return nodes;
					}
				}
				else{
					throw new SSBNNodeGeneralException(resource.getString("IncompleteInformation")); 
				} 
			}
			else{
				
				logManager.appendln("Result list for evaluate search node: " + result); 
				
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				for(String entity: result){
					SSBNNode node = createSSBNNodeForEntitySearch(originNode.getProbabilisticNetwork(), 
							fatherNode, ovInstances, ovProblematic, entity);
					nodes.add(node); 
				}
				return nodes; 
			}
			
		} catch (InvalidContextNodeFormulaException ie) {
			throw new SSBNNodeGeneralException(resource.getString("InvalidContextNodeFormula") 
					+ ": " + context.getLabel());  
		} catch (OVInstanceFaultException e) {
			throw new ImplementationRestrictionException(resource.getString("OnlyOneFreeVariableRestriction"));
		}
	}
	

	/**
	 * ...for input nodes fathers (because this nodes demand a different treatment
	 * of the arguments). 
	 * 
	 * @param mFrag
	 * @param originNode Nó que originou a criação do input node que causou a busca. 
	 * @param fatherNode Input node que tem o argumento defeituoso
	 * @param ovProblemList Lista das variaveis ordinárias problemáticas
	 * @param ovInstances Lista dos argumentos do ssbnNode.
	 * @return
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException 
	 */
	protected List<SSBNNodeJacket> createSSBNNodesOfEntitiesSearchForInputNode(
			SSBNNode originNode, InputNode fatherNode, 
			List<OrdinaryVariable> ovProblemList, List<OVInstance> ovInstances) 
			throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		MFrag mFrag = fatherNode.getMFrag(); 
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(this.getKnowledgeBase()); 
		
		if(ovProblemList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("OrdVariableProblemLimit")); 
		}
		
		//search 
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovProblemList);
		int size = contextNodeList.size(); 
		
		if(size > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		if(size < 1){
			throw new SSBNNodeGeneralException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		
		ContextNode context = contextNodeList.toArray(new ContextNode[size])[0];
		OrdinaryVariable ov = ovProblemList.get(0);
		
		logManager.appendln("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result;
			try {
				result = avaliator.evalutateSearchContextNode(context, ovInstances);
			} catch (OVInstanceFaultException e) {
				throw new ImplementationRestrictionException(resource.getString("OrdVariableProblemLimit")); 
			}
			
			if(result.isEmpty()){

				if(originNode.getContextFatherSSBNNode() != null){
					ContextFatherSSBNNode contextFatherSSBNNode = originNode.getContextFatherSSBNNode();
					
					if(contextFatherSSBNNode.getOvProblematic().equals(ov)){
						
						List<SSBNNodeJacket> nodes = new ArrayList<SSBNNodeJacket>();
						
						for(LiteralEntityInstance entity: contextFatherSSBNNode.getPossibleValues()){
							SSBNNodeJacket ssbnNodeJacket =  createSSBNNodeForEntitySearch(originNode, 
									fatherNode, ov, entity.getInstanceName());
							nodes.add(ssbnNodeJacket); 
						}
						
						return nodes;
					}
					else{
						throw new ImplementationRestrictionException(resource.getString("TwoContextFathersError"));
					}
				}
				else{
					
					List<SSBNNodeJacket> nodes = new ArrayList<SSBNNodeJacket>(); 
					logManager.appendln("Result is empty"); 
					
//					Add the context node how father
					ContextFatherSSBNNode contextFatherSSBNNode = new ContextFatherSSBNNode(
							originNode.getProbabilisticNetwork(), context); 
					contextFatherSSBNNode.setOvProblematic(ov);
					
					//Search for all the entities present in kb. 
					result = avaliator.getEntityByType(ov.getValueType().getName());
					logManager.appendln("Search returns " + result.size() + " results"); 
					for(String entity: result){
						SSBNNodeJacket ssbnNodeJacket = createSSBNNodeForEntitySearch(originNode, 
								fatherNode, ov, entity);
						contextFatherSSBNNode.addPossibleValue(
								LiteralEntityInstance.getInstance(entity, ov.getValueType()));
						nodes.add(ssbnNodeJacket); 
					}
					
					originNode.setContextFatherSSBNNode(contextFatherSSBNNode);
					
					return nodes;  
				}
			}
			else{
				List<SSBNNodeJacket> nodes = new ArrayList<SSBNNodeJacket>(); 
				for(String entity: result){
					SSBNNodeJacket ssbnNodeJacket = createSSBNNodeForEntitySearch(originNode, 
							fatherNode, ov, entity);
					nodes.add(ssbnNodeJacket); 
				}
				return nodes; 
			}
			
		} catch (InvalidContextNodeFormulaException ie) {
			throw new SSBNNodeGeneralException(resource.getString("InvalidContextNodeFormula") 
					+ ": " + context.getLabel());  
		}
	}

	
	/**
	 * 
	 * Notes: already insert the created node in the lists of nodes. 
	 * 
	 * @param probabilisticNetwork
	 * @param residentNode
	 * @param ovInstances
	 * @param ov
	 * @param entity
	 * @return
	 */
	private SSBNNode createSSBNNodeForEntitySearch(ProbabilisticNetwork probabilisticNetwork, 
			ResidentNode residentNode, List<OVInstance> ovInstances, OrdinaryVariable ov, String entity) {
		
		List<OVInstance> arguments = takeNecessaryArgumentsForNode(ovInstances, residentNode);
		arguments.add(OVInstance.getInstance(ov, entity, ov.getValueType())); 
		
		SSBNNode testSSBNNode = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(residentNode, ovInstances)); 
		
		if(testSSBNNode == null){
			testSSBNNode =  SSBNNode.getInstance(probabilisticNetwork,residentNode);

			for(OVInstance ovInstance: arguments){
				testSSBNNode.addArgument(ovInstance);
			}
			ssbnNodeList.add(testSSBNNode);
			ssbnNodesMap.put(testSSBNNode.getUniqueName(), testSSBNNode); 
			
		}
	
		return testSSBNNode;
	}
	
	protected List<OVInstance> takeNecessaryArgumentsForNode(Collection<OVInstance> ovInstanceList, ResidentNode node) {
	
		List<OVInstance> ret = new ArrayList<OVInstance>(); 
		
		for(OVInstance ovInstance: ovInstanceList){
			if(node.getOrdinaryVariableByName(ovInstance.getOv().getName()) != null){
				ret.add(ovInstance); 
			}
		}
		
		return ret; 
	}


	/**
	 * Evaluate of recursion in the MEBN model. Return the node before in 
	 * the recursion.
	 * 
	 * - Already include the node created in the ssbnNodeList. 
	 * 
	 * @param currentNode
	 * @param seen
	 * @param net
	 * @param residentNode
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	protected SSBNNodeJacket getPreviousNode(SSBNNode currentNode, SSBNNodeList seen, 
			ProbabilisticNetwork net, ResidentNode residentNode, InputNode inputNode) 
	        throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		logManager.appendln("Build Previous Node");
		//Find for ordereable object entity.
		List<OrdinaryVariable> ovOrdereableList = residentNode.getOrdinaryVariablesOrdereables();
		
		if(ovOrdereableList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneOrdereableVariable"));
		}
		
		if(ovOrdereableList.size() < 1){
			throw new ImplementationRestrictionException(resource.getString("RVNotRecursive"));
		}
		
		OrdinaryVariable ovOrdereableResident = ovOrdereableList.get(0); 
		logManager.appendln("Ov Ordereable found:" + ovOrdereableResident);
		ObjectEntity objectEntityOrdereable = currentNode.getResident().getMFrag().
                        getMultiEntityBayesianNetwork().getObjectEntityContainer().
                        getObjectEntityByType(ovOrdereableResident.getValueType()); 
	
		OVInstance ovInstanceOrdereable = null; 
		for(OVInstance ovInstance: currentNode.getArguments()){
			if(ovInstance.getOv() == ovOrdereableResident){
				ovInstanceOrdereable = ovInstance; 
				break; 
			}
		}
		
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
		
		if(prev == null){
			return null; //end of the recursion
		}else{
			//Create the new SSBNNode with the setted values. 
			SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNode, 
					new ProbabilisticNode());

			SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode);
			
			for(OVInstance instance: currentNode.getArguments()){
				if(instance != ovInstanceOrdereable){
					if(residentNode.getOrdinaryVariableByName(instance.getOv().getName()) != null){
						ssbnNodeJacket.addOVInstanceOfInputMFrag(instance);
						ssbnNodeJacket.addOVInstanceOfResidentMFrag(instance);
					}
				}else{
					OVInstance newOVInstance = OVInstance.getInstance(ovOrdereableResident, 
							prev.getName(), ovOrdereableResident.getValueType());
					ssbnNodeJacket.addOVInstanceOfResidentMFrag(newOVInstance);
					
					int index = residentNode.getOrdinaryVariableList().indexOf(ovOrdereableResident);
					OrdinaryVariable ovOrdereableInput = inputNode.getOrdinaryVariableByIndex(index);
					newOVInstance = OVInstance.getInstance(ovOrdereableInput, 
							prev.getName(), ovOrdereableResident.getValueType());
					ssbnNodeJacket.addOVInstanceOfInputMFrag(newOVInstance);
				
				}
			}
			
			ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
			
			SSBNNode alreadyExistentSSBNNode = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(residentNode, ssbnNode.getArguments())); 
			

			if(alreadyExistentSSBNNode != null){
				alreadyExistentSSBNNode.setRecursiveOVInstanceList(ssbnNodeJacket.getOvInstancesOfInputMFrag());
				ssbnNodeJacket.getSsbnNode().delete();
				ssbnNodeJacket.setSsbnNode(alreadyExistentSSBNNode);
				
			}else{
				ssbnNode.setRecursiveOVInstanceList(ssbnNodeJacket.getOvInstancesOfInputMFrag());
			    ssbnNodeList.add(ssbnNode);	
				ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 
			}
					
			logManager.appendln("Build Previous Node End");
			return ssbnNodeJacket;
		}
	}
	
	

	/**
	 * Evaluate of recursion in the MEBN model. Return the node before in 
	 * the recursion.
	 * 
	 * - Already include the node created in the ssbnNodeList. 
	 * 
	 * @param currentNode
	 * @param seen
	 * @param net
	 * @param residentNode
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	protected SSBNNode getProcNode(SSBNNode currentNode, SSBNNodeList seen, 
			ProbabilisticNetwork net, ResidentNode residentNode, 
			List<OVInstance> listOvInstancesInput, InputNode inputNode) 
	        throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		logManager.appendln("Build Proc. Node");
		//Find for ordereable object entity.
		List<OrdinaryVariable> ovOrdereableList = residentNode.getOrdinaryVariablesOrdereables();
		
		if(ovOrdereableList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneOrdereableVariable"));
		}
		
		if(ovOrdereableList.size() < 1){
			throw new ImplementationRestrictionException(resource.getString("RVNotRecursive"));
		}
		
		OrdinaryVariable ovOrdereableResident = ovOrdereableList.get(0); 
		logManager.appendln("Ov Ordereable found:" + ovOrdereableResident);
		ObjectEntity objectEntityOrdereable = currentNode.getResident().getMFrag().
                        getMultiEntityBayesianNetwork().getObjectEntityContainer().
                        getObjectEntityByType(ovOrdereableResident.getValueType()); 
	
		OVInstance ovInstanceOrdereable = null; 
		for(OVInstance ovInstance: currentNode.getArguments()){
			if(ovInstance.getOv() == ovOrdereableResident){
				ovInstanceOrdereable = ovInstance; 
				break; 
			}
		}
		
		if(ovInstanceOrdereable == null){
			throw new SSBNNodeGeneralException();
		}
		
		String nameEntity = ovInstanceOrdereable.getEntity().getInstanceName(); 
		
		ObjectEntityInstanceOrdereable objectEntityInstanceOrdereable = 
			(ObjectEntityInstanceOrdereable)objectEntityOrdereable.getInstanceByName(nameEntity);
		
		if(objectEntityInstanceOrdereable == null){
			throw new SSBNNodeGeneralException();
		}
		
		ObjectEntityInstanceOrdereable proc = objectEntityInstanceOrdereable.getProc(); 
		
		if(proc == null){
			logManager.appendln("Proc is null... no action to do"); 
			return null; //end of the recursion
		}else{
			//Create the new SSBNNode with the setted values. 
			SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNode, 
					new ProbabilisticNode());
		 
			for(OVInstance instance: currentNode.getArguments()){
				if(instance != ovInstanceOrdereable){
					if(residentNode.getOrdinaryVariableByName(instance.getOv().getName()) != null){
						ssbnNode.addArgument(instance);
						listOvInstancesInput.add(instance); 
					}
				}else{
					OVInstance newOVInstance = OVInstance.getInstance(ovOrdereableResident, 
							proc.getName(), ovOrdereableResident.getValueType());
					ssbnNode.addArgument(newOVInstance); 
					
					int index = residentNode.getOrdinaryVariableList().indexOf(ovOrdereableResident);
					OrdinaryVariable ovOrdereableInput = inputNode.getOrdinaryVariableByIndex(index);
					newOVInstance = OVInstance.getInstance(ovOrdereableInput, 
							objectEntityInstanceOrdereable.getName(), ovOrdereableResident.getValueType());
					listOvInstancesInput.add(newOVInstance); 
					
					logManager.appendln("\n\n created:" + ovOrdereableResident + " " + proc.getName());
				}
			}
//			SSBNNode testSSBNNode = getSSBNNodeIfItAlreadyExists(ssbnNode.getResident(), 
//					ssbnNode.getArguments(), ssbnNodeList); 
			
			logManager.append("Search for " + ssbnNode.getUniqueName() + " = " + ssbnNodesMap.get(ssbnNode.getUniqueName())); 
			SSBNNode testSSBNNode = ssbnNodesMap.get(ssbnNode.getUniqueName()); 
			
			if(testSSBNNode != null){
				ssbnNode.delete(); 
				ssbnNode = testSSBNNode; 
			}else{
				ssbnNodeList.add(ssbnNode);
				ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 
			}
			
			logManager.appendln("Build Proc Node End: Node created:" + ssbnNode);
			return ssbnNode;
		}
	}

	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	protected boolean evaluateRelatedContextNodes (ResidentNode residentNode, 
			List<OVInstance> ovInstances) throws OVInstanceFaultException{
		return evaluatedRelatedContextNodes(residentNode.getMFrag(), ovInstances, 
				residentNode.getOrdinaryVariableList()); 
	}
	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 * @throws OVInstanceFaultException 
	 */
	protected boolean evaluateRelatedContextNodes(InputNode inputNode, 
			List<OVInstance> ovInstances) throws OVInstanceFaultException{
		return evaluatedRelatedContextNodes(inputNode.getMFrag(), ovInstances, 
				inputNode.getOrdinaryVariableList());
	}
	
	private boolean evaluatedRelatedContextNodes(MFrag mFrag, 
			List<OVInstance> ovInstances, Collection<OrdinaryVariable> ovList) throws OVInstanceFaultException{
		//Debug.setDebug(true); 
		
		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (mFrag.isUsingDefaultCPT()) {
			return false;
		};
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(this.getKnowledgeBase()); 
		
		Collection<ContextNode> contextNodeList = mFrag.getContextByOVCombination(ovList);
		
		for(ContextNode context: contextNodeList){
			logManager.appendln("Context Node: " + context.getLabel());
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				mFrag.setAsUsingDefaultCPT(true); 
				logManager.appendln("Result = FALSE. Use default distribution ");
				return false;  
			}else{
				logManager.appendln("Result = TRUE.");
			}		
		}
		
		return true; 
		
	}
	

	
	
	/* 
	 * version for input nodes. 
	 */
	private SSBNNodeJacket createSSBNNodeForEntitySearch(SSBNNode originNode, 
			InputNode fatherNode, OrdinaryVariable ov, String entityName) 
	        throws SSBNNodeGeneralException {
		
		SSBNNode ssbnNode = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),
				(ResidentNode)fatherNode.getResidentNodePointer().getResidentNode(), new ProbabilisticNode()); 
		
		SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode); 
		
		//Add OVInstance created for the entity search
		addArgumentToSSBNNodeOfInputNode(fatherNode, ssbnNodeJacket,  
				OVInstance.getInstance(ov, entityName, ov.getValueType()));	
		
		//Add all the other OVInstances
		for(OVInstance instance: originNode.getArguments()){
			addArgumentToSSBNNodeOfInputNode(fatherNode, ssbnNodeJacket, instance);
		}
		
		//Suport for avoid double creation of probabilistic nodes. 
		ssbnNodeJacket.setArgumentsOfResidentMFrag();
		
		SSBNNode test = ssbnNodesMap.get(ssbnNode.getUniqueName()); 
		
		if(test != null){
			ssbnNodeJacket.getSsbnNode().delete(); 
			ssbnNodeJacket.setSsbnNode(test); 
		}
		
		ssbnNodeList.add(ssbnNodeJacket.getSsbnNode());
		ssbnNodesMap.put(ssbnNodeJacket.getSsbnNode().getUniqueName(), ssbnNodeJacket.getSsbnNode()); 	
		
//		ssbnNodeJacket.setSsbnNode(checkForDoubleSSBNNode(ssbnNode));
		
		logManager.appendln(" ");
		logManager.appendln("SSBNNode created:" + ssbnNodeJacket.getSsbnNode());
		logManager.appendln("Input MFrag Arguments: " + ssbnNodeJacket.getOvInstancesOfInputMFrag());
		logManager.appendln("Resident MFrag Arguments: " + ssbnNodeJacket.getOvInstancesOfResidentMFrag());
		logManager.appendln(" ");
		
		
		return ssbnNodeJacket;
	}

	/*
	 * This method is used for avoid the creation of double equals ssbnnodes
	 * (ssbnnodes with the same resident node and the same arguments, because this
	 * nodes will have the same probabilistic node). 
	 *  
	 * @param ssbnnode SSBNNode with the arguments setted. 
	 * @return
	 */
	@Deprecated //Wrong method... getSSBNNodeIfItAlreadyExists should be used
	protected SSBNNode checkForDoubleSSBNNode(SSBNNode ssbnnode) {
		
		//TODO Wrong method... getSSBNNodeIfItAlreadyExists should be used
		
		SSBNNode testSSBNNode = getSSBNNodeIfItAlreadyExists(ssbnnode.getResident(), 
				ssbnnode.getArguments(), ssbnNodeList); 
		
		if(testSSBNNode != null){
			ssbnnode.delete(); 
			ssbnnode = testSSBNNode; 
		}
		
		return ssbnnode;
		
	}

	
	/** 
	 * Add instance how a argument of a ssbnnode originate of a input node. 
	 *
	 * @param inputNode Node that originate the SSBNNode
	 * @param ssbnnodeJacket Jacket with reference to the SSBNNode
	 * @param ovInstanceInputMFrag OVInstance of the input MFrag (MFrag where exists the input node)
	 * @throws SSBNNodeGeneralException
	 */
	protected void addArgumentToSSBNNodeOfInputNode(InputNode inputNode, 
			SSBNNodeJacket ssbnnodeJacket, OVInstance ovInstanceInputMFrag) throws SSBNNodeGeneralException {
		
		ResidentNode residentNode = inputNode.getResidentNodePointer().getResidentNode(); 
		OrdinaryVariable ovInputMFrag = ovInstanceInputMFrag.getOv(); 
		int index = inputNode.getResidentNodePointer().getOrdinaryVariableIndex(ovInputMFrag);
		if(index > -1){
			ssbnnodeJacket.addOVInstanceOfInputMFrag(ovInstanceInputMFrag); 
			OrdinaryVariable ovResidentMFrag = residentNode.getOrdinaryVariableList().get(index);
			ssbnnodeJacket.addOVInstanceOfResidentMFrag(
					OVInstance.getInstance(ovResidentMFrag, ovInstanceInputMFrag.getEntity()));
		}
	}
	
	protected void addArgumentToSSBNNodeOfResidentNode(ResidentNode residentNode, 
			InputNode inputNode, SSBNNodeJacket ssbnNodeJacket, OVInstance ovInstanceResidentMFrag){
		
		OrdinaryVariable ovResidentMFrag = ovInstanceResidentMFrag.getOv(); 
		int index = residentNode.getOrdinaryVariableIndex(ovResidentMFrag); 
		
		if(index > -1){
			ssbnNodeJacket.addOVInstanceOfResidentMFrag(ovInstanceResidentMFrag); 
			OrdinaryVariable ovInputMFrag = inputNode.getOrdinaryVariableByIndex(index); 
			ssbnNodeJacket.addOVInstanceOfInputMFrag(
					OVInstance.getInstance(ovInputMFrag, ovInstanceResidentMFrag.getEntity())); 
		}
		
	}
	
	protected OVInstance getListArgumentsOfInputVariableInOriginalMFrag(InputNode inputNode, OVInstance ovInstanceInputMFrag){
		ResidentNode residentNode = inputNode.getResidentNodePointer().getResidentNode(); 
		OrdinaryVariable ovInputMFrag = ovInstanceInputMFrag.getOv(); 
		int index = inputNode.getResidentNodePointer().getOrdinaryVariableIndex(ovInputMFrag);
		if(index > -1){
			OrdinaryVariable ovResidentMFrag = residentNode.getOrdinaryVariableList().get(index);
			return OVInstance.getInstance(ovResidentMFrag, ovInstanceInputMFrag.getEntity());
		}else{
			return null; 
		}
		
	}
	
	/**
	 * The XOR algorith: 
	 * Generate the cpt of 
	 * - the context node father
	 * - the node that have the indeterminated parents
	 * 
	 * Pre-requisites: 
	 * 
	 */
	protected void generateCPTForNodeWithContextFather(SSBNNode ssbnNode) 
	      throws SSBNNodeGeneralException, MEBNException, InvalidOperationException {
		

//		    GUIPotentialTable gpt; 
		
			logManager.appendln("\nGenerate table for node (with context father): " + ssbnNode);
			logManager.appendln("Parents:");
			for(SSBNNode parent: ssbnNode.getParents()){
				logManager.appendln("  " + parent);
			}
			
			//Debug.setDebug(false);
			
			Map<String, List<SSBNNode>> mapParentsByEntity = new HashMap<String, List<SSBNNode>>(); 
			Map<String, PotentialTable> mapCPTByEntity = new HashMap<String, PotentialTable>(); 
			
			ContextFatherSSBNNode contextFather = ssbnNode.getContextFatherSSBNNode();
			OrdinaryVariable ovProblematic = contextFather.getOvProblematic(); 
			
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				mapParentsByEntity.put(entity.getInstanceName(), new ArrayList<SSBNNode>()); 
			}
			
			//Step 0: Calcular a tabela do nó de contexto

			ssbnNode.getContextFatherSSBNNode().generateCPT();
			
//			gpt = new GUIPotentialTable(ssbnNode.getContextFatherSSBNNode().getProbNode().getPotentialTable()); 
//			gpt.showTable("Table for Node " + ssbnNode.getContextFatherSSBNNode());
			
			//Step 1: Dividir os pais em grupos de acordo com a variavel problemática
			Collection<SSBNNode> parents = ssbnNode.getParents(); 
			Collection<SSBNNode> generalParents = new ArrayList<SSBNNode>(); //Independent of the entity problematic
			
			for(SSBNNode parent: parents){
				if(!parent.getOVs().contains(ovProblematic)){
					generalParents.add(parent); 
				}else{
					String entity = parent.getArgumentByOrdinaryVariable(ovProblematic).getEntity().getInstanceName(); 
					mapParentsByEntity.get(entity).add(parent); 
				}
			}
			
			int sizeCPTOfEntity = 0; 
			
			PotentialTable cptResidentNode = ssbnNode.getProbNode().getPotentialTable(); 
						
			//Step 2: Construir as tabelas para os diversos grupos de pais
			int position = 1; 
			
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				
				ArrayList<SSBNNode> groupParents = new ArrayList<SSBNNode>(); 
				
				groupParents.addAll(mapParentsByEntity.get(entity.getInstanceName())); //Sempre na mesma ordem? 
				
				List<SSBNNode> parentsByEntity = mapParentsByEntity.get(entity.getInstanceName()); 
				for(SSBNNode node: parentsByEntity){
					int index = cptResidentNode.getVariableIndex(node.getProbNode()); 
					cptResidentNode.moveVariableWithoutMoveData(index, position); 
					position++; 
				}
				
				groupParents.addAll(generalParents); //OK, sempre estarão na mesma ordem. 
				for(SSBNNode node: generalParents){
					int index = cptResidentNode.getVariableIndex(node.getProbNode()); 
					cptResidentNode.moveVariableWithoutMoveData(index, position); 
					position++; 
				}
				
				//Gera a tabela para este grupo de pais (o conjunto de tabelas será 
				//utilizado no xor. 
				SSBNNode tempNode = SSBNNode.getInstance(ssbnNode.getResident()); 
				for(SSBNNode parent: groupParents){
					tempNode.addParent(parent, false); 
				}
				PotentialTable cpt = tempNode.getCompiler().generateCPT(tempNode); 
				sizeCPTOfEntity = cpt.tableSize();
				
//				gpt = new GUIPotentialTable(cpt); 
//				gpt.showTable("Table for Node " + ssbnNode + " - " + groupParents);
				
				//Remove the temp node of the list of children of the node. 
				for(SSBNNode parent: groupParents){
					parent.getProbNode().getChildren().remove(tempNode.getProbNode()); 
				    parent.getChildren().remove(tempNode); 
				}
				
				mapCPTByEntity.put(entity.getInstanceName(), cpt);
				Debug.println("Tabela armazenada: " + entity.getInstanceName() + " " + cpt.tableSize());
			
				//TODO remove the parents of the tempNode because it is added to the list
				//of child nodes of the other node!!!
//				for(SSBNNode parent: groupParents){
//					tempNode.removeParent(parent); 
//				}
			}			
			
			//Reorganize the variables in table
			int variablesSize = cptResidentNode.getVariablesSize(); 
			int indexContext = cptResidentNode.getVariableIndex(ssbnNode.getContextFatherSSBNNode().getProbNode()); 
			cptResidentNode.moveVariableWithoutMoveData(indexContext, variablesSize - 1); 
			
			//Step 3: Fazer o XOR das tabelas obtidas utilizando a tabela do nó de contexto
			
			Debug.println("Gerando tabela para o nó residente");

			int columnsByEntity = cptResidentNode.tableSize() / ssbnNode.getResident().getPossibleValueListIncludingEntityInstances().size();
			columnsByEntity /= contextFather.getProbNode().getStatesSize(); 
			
			Debug.println("Colunas por entidade= " + columnsByEntity);

			int rows = ssbnNode.getProbNode().getStatesSize(); 
			
			for(int i=0; i < contextFather.getProbNode().getStatesSize(); i++){
			    
				Debug.println("\n i = " + i);
				
				String entity = contextFather.getProbNode().getStateAt(i);
				Debug.println("Entity = " + entity);
				PotentialTable cptEntity = mapCPTByEntity.get(entity); 
				
				//descobrir a posição inicial...
				List<SSBNNode> parentsByEntity = mapParentsByEntity.get(entity); 
				ProbabilisticNode pnEntity = parentsByEntity.get(0).getProbNode(); 
			
				int indexEntityInCptResidentNode = cptResidentNode.getVariableIndex(pnEntity) - 1; //the index 0 is the node itself
				int entityIndex = (indexEntityInCptResidentNode + parentsByEntity.size() - 1)/ parentsByEntity.size(); 
				
				Debug.println("Entity Index=" + entityIndex);
				
				int positionTableEntity = 0; 

				int positionTableResident = entityIndex*columnsByEntity*rows; 
				
				//Key of the algorith!!!
				
				//Repetitions of a colum is based of the number os variables up of this. 
				int repColum = 1; 
				for(int index = indexEntityInCptResidentNode; index >= 1; index --){
		
					repColum*= cptResidentNode.getVariableAt(index).getStatesSize();
				
				}
				
                //Repetitions of a all is based of the number os variables down of this. 
				int repAll = 1; 
				for(int index = indexEntityInCptResidentNode + parentsByEntity.size(); 
				    index < cptResidentNode.getVariablesSize() - 2;  //minus Entity row and Node row.  
				    index++){
		
					repAll*= cptResidentNode.getVariableAt(index).getStatesSize(); 
					
				}
				
				int inOrder = 1; 
				for(SSBNNode node: parentsByEntity){
					inOrder*= node.getResident().getPossibleValueList().size(); 
				}
				
				Debug.println("Index = " + indexEntityInCptResidentNode);
				Debug.println("Repetições = " + repColum);
				Debug.println("Posição na tabela do residente = " + positionTableResident);
				Debug.println("Posição na tabela da entidade = " + positionTableEntity);
				Debug.println("Linhas = " + rows);
				Debug.println("Repitições de tudo = " + repAll);
				Debug.println("Em Ordem = " + inOrder);
				
				while(positionTableEntity < cptEntity.tableSize() - 1){
					int positionTableEntityFinal = -1; 
					
					for(int rAll= 0; rAll < repAll; rAll++){
						
						int positionTableEntityInitial = positionTableEntity; 
						
						for(int order = 0; order < inOrder; order++){
							for(int rCol = 0; rCol < repColum; rCol++){
								int positionAuxEntity = positionTableEntityInitial; 
								for(int k = 0; k < rows; k++){
//									System.out.println("k=" + k + ";rCol=" + rCol + 
//											";order=" + order + ";rAll=" + rAll + 
//											" [" + positionTableResident + "] recebe de " 
//											+ "[" + positionAuxEntity + 
//											"] o valor = " + cptEntity.getValue(positionAuxEntity));
									
//									System.out.print(cptEntity.getValue(positionAuxEntity) + " ");
									cptResidentNode.setValue(positionTableResident, cptEntity.getValue(positionAuxEntity)); 
									positionTableResident++; 
									positionAuxEntity++; 
								}
//								Debug.println("");
							}
							positionTableEntityInitial += rows; 
						}
						
						positionTableEntityFinal = positionTableEntityInitial;
						
					}
					
					positionTableEntity = positionTableEntityFinal; 
				}
			}

//			gpt = new GUIPotentialTable(ssbnNode.getProbNode().getPotentialTable()); 
//			gpt.showTable("Table for Node " + ssbnNode);
			
			//Debug.setDebug(true);
			logManager.appendln("CPT OK\n");
		
	}
	

	/**
	 * Return the SSBNNode for the residentNode with the ovInstanceList how 
	 * arguments. 
	 * @param residentNode
	 * @param ovInstanceList
	 * @param ssbnNodeList
	 * @return
	 */
	protected SSBNNode getSSBNNodeIfItAlreadyExists(ResidentNode residentNode, 
			Collection<OVInstance> ovInstanceList, Collection<SSBNNode> ssbnNodeList){
		
		int i = 0; 
		for(SSBNNode ssbnNode : ssbnNodeList){
			if(ssbnNode.getResident() == residentNode){
				Collection<OVInstance> ssbnNodeArguments = ssbnNode.getArguments(); 
				if(ovInstanceList.size()!= ssbnNodeArguments.size()){
					logManager.append("size different!!!");
					return null; 
				}else{
					int j = 0; 
					for(OVInstance ovInstance: ovInstanceList){
						boolean find = false; 
						int k = 0; 
						for(OVInstance ssbnNodeArgument: ssbnNodeArguments){
							if(ssbnNodeArgument.equals(ovInstance)){
								find = true; 
								break; 
							}
						}
						if(!find){
							logManager.append("Not find!!!");
							return null; 
						}
					}
					//Find all the ovInstances! Is the same SSBNNode
					logManager.append("getSSBNNode return that the node " + 
							residentNode.getName() + "[" + ovInstanceList.toString() + 
							"] already exists!");
					
					return ssbnNode; 
				}
			}
		}
		
		logManager.appendln("getSSBNNode return that the node " + 
				residentNode.getName() + "[" + ovInstanceList.toString() + 
				"] DON'T exists!");
		
		return null; 
	}
	
	/*
	 * debug method
	 */
	private String getSpaceForLevel(int level){
		StringBuilder string = new StringBuilder(); 
		for(int i = 0; i < level; i++){
			string.append(' '); 
			string.append(' '); 
//			string.append(level);
			string.append('-');
			string.append(' '); 
		}
		return string.toString(); 
	}
	
	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}
	
	public void setKnowledgeBase(KnowledgeBase kb){
		this.knowledgeBase = kb; 
	}
	
	
	/*-------------------------------------------------------------------------
	 * Private Classes 
	 *------------------------------------------------------------------------/
	
	/*
	 * This class is used for the management of the arguments of a ssbnnode 
	 * build for a InputNode. This is necessary because the arguments of this
	 * ssbn node need to be interchangeable (should refer to the ordinary
	 * variables of the MFrag of the ssbn node when the evaluation is of the 
	 * input how father of a resident node and to the ordinary variables of the 
	 * MFrag where the node is residente if the evaluation is about the 
	 * ssbnnode itself). 
	 */
	protected class SSBNNodeJacket{
		
		private final Integer ARGUMENTS_OF_INPUT_MFRAG = 0; 
		private final Integer ARGUMENTS_OF_RESIDENT_MFRAG = 1; 
		
		private SSBNNode ssbnNode; 
		private List<OVInstance> ovInstancesOfInputMFrag; 
		private List<OVInstance> ovInstancesOfResidentMFrag; 
		private Integer typeAtualArguments; 
		
		public SSBNNodeJacket(SSBNNode ssbnNode){
			this.ssbnNode = ssbnNode;
			this.typeAtualArguments = -1; 
			ovInstancesOfInputMFrag = new ArrayList<OVInstance>();
			ovInstancesOfResidentMFrag = new ArrayList<OVInstance>(); 
		}

		public SSBNNode getSsbnNode() {
			return ssbnNode;
		}

		public void setSsbnNode(SSBNNode ssbnNode) {
			this.ssbnNode = ssbnNode;
		}

		public List<OVInstance> getOvInstancesOfInputMFrag() {
			return ovInstancesOfInputMFrag;
		}

		public void addOVInstanceOfInputMFrag(OVInstance ovInstance){
			this.ovInstancesOfInputMFrag.add(ovInstance);
		}
		
		public Collection<OVInstance> getOvInstancesOfResidentMFrag() {
			return ovInstancesOfResidentMFrag;
		}

		public void addOVInstanceOfResidentMFrag(OVInstance ovInstance){
			this.ovInstancesOfResidentMFrag.add(ovInstance);
		}		
		
		public void setArgumentsOfInputMFrag(){
			if(typeAtualArguments != ARGUMENTS_OF_INPUT_MFRAG){
				ssbnNode.removeAllArguments(); 
				ssbnNode.setArguments(ovInstancesOfInputMFrag);
				typeAtualArguments = ARGUMENTS_OF_INPUT_MFRAG;
			}
		}
		
		public void setArgumentsOfResidentMFrag(){
			if(typeAtualArguments != ARGUMENTS_OF_RESIDENT_MFRAG){
				ssbnNode.removeAllArguments(); 
				ssbnNode.setArguments(ovInstancesOfResidentMFrag);
				typeAtualArguments = ARGUMENTS_OF_RESIDENT_MFRAG;
			}
		}

		public Integer getTypeAtualArguments() {
			return typeAtualArguments;
		}

		public void setTypeAtualArguments(Integer typeAtualArguments) {
			this.typeAtualArguments = typeAtualArguments;
		}
		
		public String toString(){
			return ssbnNode.toString() +  
			       " Input["+ ovInstancesOfInputMFrag + "]" + 
			       " Resident[" + ovInstancesOfResidentMFrag + "]";
		}
		
	}
	
}
