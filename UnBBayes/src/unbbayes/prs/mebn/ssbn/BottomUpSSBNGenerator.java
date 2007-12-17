package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import unbbayes.io.XMLIO;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * Algorith for generate the Situation Specific Bayesian Network.  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com) 
 * @author Shou Matsumoto
 */
public class BottomUpSSBNGenerator implements ISSBNGenerator {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private KBFacade kb = null;
	
	private long recursiveCallLimit = 999999999999999999L;
	
	private long recursiveCallCount = 0;
	
	//all the ssbnnode created. 
	private List<SSBNNode> ssbnNodeList; 
	
	

	/**
	 * 
	 */
	public BottomUpSSBNGenerator() {
	}
	

	
	/*-------------------------------------------------------------------------
	 * Public
	 *------------------------------------------------------------------------/
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException, 
	                                                             ImplementationRestrictionException {
		
		ssbnNodeList = new ArrayList<SSBNNode>(); 
		
		// As the query starts, let's clear the flags used by the previous query
		query.getMebn().clearMFragsIsUsingDefaultCPTFlag();
		
		// some data extraction
		SSBNNode querynode = query.getQueryNode();
		this.kb = query.getKb();

		// initialization

		// call recursive
		this.recursiveCallCount = 0;
		SSBNNode root = this.generateRecursive(querynode, new SSBNNodeList(), 
				                               querynode.getProbabilisticNetwork());
		
		//Debug. 
		PositionAdjustmentUtils utils = new PositionAdjustmentUtils(); 
		utils.adjustPositionProbabilisticNetwork(querynode.getProbabilisticNetwork()); 
		
		this.printNetworkInformation(querynode); //only Debug informations
		
		return querynode.getProbabilisticNetwork();
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
	
	
	
	
	
	/*-------------------------------------------------------------------------
	 * Private
	 *------------------------------------------------------------------------/
	
	/**
	 * The recursive part of SSBN bottom-up generation algorithm
	 * 
	 * Pre-requisites: 
	 *     - The node current node has a OVInstance for all OrdinaryVariable argument
	 * 
	 * Pos-requisites:
	 *     - The probabilistic table of the currentNode will be filled and all
	 *     nodes fathers evaluateds. 
	 * 
	 * @param currentNode node currently analyzed. 
	 * @param seen all the nodes analized previously (doesn't contain the current node)
	 * @param net the ProbabilisticNetwork 
	 * @return The currentNode with its auxiliary structures of the analysis. 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net) throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}
		this.recursiveCallCount++;
		
		Debug.println("\n-------------Step: " + currentNode.getName() + "--------------\n"); 
		
		// check for cycle
		if (seen.contains(currentNode)) {
//			return null; 
			//TODO Criar método para avaliar ciclos
		}
		
		//------------------------- STEP 1: search findings -------------------
		

		Debug.println(currentNode + ":A - Search findings");
        //check if querynode has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = kb.searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			seen.add(currentNode); 
			Debug.println("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			
	        generateCPT(currentNode);
	        
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		
		//------------------------- STEP 2: analyse context nodes. -------------
		
		// evaluates querynode's mfrag's context nodes 
		//(if not OK, sets MFrag's flag to use default CPT)	
		
		Debug.println(currentNode + ":B - Analyse context nodes");
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 
        
		boolean result = false;
		
		try {
			result = evaluateRelatedContextNodes(currentNode.getResident(), ovInstancesList);
		} catch (OVInstanceFaultException e) {
			//pre-requisite. 
			e.printStackTrace();
		}
		
		if(!result){
			Debug.println("Context Node fail for " + currentNode.getResident().getMFrag()); 
			currentNode.setUsingDefaultCPT(true);
			return currentNode; 
		}
		
		
		
		
		
       //------------------------- STEP 3: Add and evaluate resident nodes fathers -------------
		
		Debug.println(currentNode + "C:- Analyse resident nodes fathers");
		for (DomainResidentNode residentNode : currentNode.getResident().getResidentNodeFatherList()) {

			/*
			 * Analyse if have one ov instance for each ordinary variable. If this 
			 * don't is true, verify if have some context node able to recover 
			 * the instances that match with the ordinary variable.  
			 */
            List<OrdinaryVariable> ovProblemList = analyseOVInstancesListCompletudeForOVList(
            		residentNode.getOrdinaryVariableList(), currentNode.getArguments()); 
            
    		if(!ovProblemList.isEmpty()){
    			List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
    					residentNode.getMFrag(), currentNode, residentNode, 
    					ovProblemList, ovInstancesList); 
    			
    		    for(SSBNNode ssbnnode: createdNodes){
    		    	
    		    	generateRecursive(ssbnnode, seen, net);	
    		    	
    		    	if(!ssbnnode.isFinding()){
    		    		currentNode.addParent(ssbnnode, true);
    		    	}else{
    		    		currentNode.addParent(ssbnnode, false);
    		    	}	
    		    }
    		    
    		}else{
    			SSBNNode ssbnnode = null; 
    			
				List<OVInstance> arguments = fillArguments(currentNode.getArguments(), residentNode);
				SSBNNode testSSBNNode = getSSBNNode(residentNode, arguments); 
				
				//This test is necessary for not create two equals ssbnnodes (duplicated bayesian nodes). 
				if(testSSBNNode == null){
					ssbnnode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode(), false);
					for(OVInstance ovInstance: arguments){
						ssbnnode.addArgument(ovInstance);
					}
					ssbnNodeList.add(ssbnnode);
	    			generateRecursive(ssbnnode, seen, net);	
				}else{
					ssbnnode = testSSBNNode;
				}
				
    			if(!ssbnnode.isFinding()){
    				currentNode.addParent(ssbnnode, true);
    			}else{
    				currentNode.addParent(ssbnnode, false); 
    			}
    		}
			
		}
		
		Debug.println(currentNode.getResident().getName() + " return of resident parents recursion"); 
		
		
		
		
		
	    //------------------------- STEP 4: Add and evaluate input nodes fathers -------------

		Debug.println(currentNode + "D:- Analyse input nodes fathers");
		for (GenerativeInputNode inputNode : currentNode.getResident().getInputNodeFatherList()) {
			
			DomainResidentNode residentNode = 
				(DomainResidentNode)inputNode.getResidentNodePointer().getResidentNode(); 

			Debug.println(currentNode.getName() + "Evaluate input " + residentNode.getName()); 
			
			//Evaluate recursion... 
			if(currentNode.getResident() == residentNode){
				
				SSBNNodeJacket previousNode = getPreviousNode(currentNode, seen, net, residentNode, inputNode);

				if(previousNode != null){
					
					previousNode.setArgumentsOfResidentMFrag();
					generateRecursive(previousNode.getSsbnNode(), seen, net);
					previousNode.setArgumentsOfInputMFrag();

					/* voltar nomes dos parametros para o normal */

					if(!previousNode.getSsbnNode().isFinding()){
						currentNode.addParent(previousNode.getSsbnNode(), true);
					}else{
						currentNode.addParent(previousNode.getSsbnNode(), false);
					}
				}
				
				continue;			
			}
			
			//Step 1: evaluate context and search for findings
            List<OrdinaryVariable> ovProblemList = 
            	analyseOVInstancesListCompletudeForOVList(inputNode.getOrdinaryVariableList(), 
            			currentNode.getArguments()); 
            
    		if(!ovProblemList.isEmpty()){
    			
    			List<SSBNNodeJacket> parentList = createSSBNNodesOfEntitiesSearchForInputNode(
    					currentNode, inputNode, ovProblemList, currentNode.getArgumentsAsList()); 
    			
    		    for(SSBNNodeJacket ssbnNodeJacket: parentList){
    		    	
    		    	SSBNNode ssbnnode = ssbnNodeJacket.getSsbnNode();
    		    	ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
    		    	
    		    	Debug.println("Node Created: " + ssbnnode.toString());
    		    	
    		    	generateRecursive(ssbnnode, seen, net);	// algorithm's core
    		    	
    		    	ssbnNodeJacket.setArgumentsOfInputMFrag(); 
        			if(!ssbnnode.isFinding()){
        				currentNode.addParent(ssbnnode, true);
        			}else{
        				currentNode.addParent(ssbnnode, false);
        			}
        			
    		    }
    		}else{
    			
    			//The argument list is OK. 
    			
    			boolean contextNodesOK = false;
				
    			try {
					contextNodesOK = evaluateRelatedContextNodes(inputNode, currentNode.getArgumentsAsList());
				} catch (OVInstanceFaultException e) {
					//ovProblemList is empty... this exception never will be catch.
					e.printStackTrace();
				} 
				
    			if(contextNodesOK){
    				SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNode, new ProbabilisticNode(), false);
    				SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode); 
    				
    				for(OVInstance ovInstance: currentNode.getArguments()){
    					addArgumentToSSBNNodeOfInputNode(inputNode, ssbnNodeJacket, ovInstance);
    				}
    				
    				ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
    				
//    				ssbnNode = checkForDoubleSSBNNodeForInputEvaliation(ssbnNode); //TODO analyse... 
    				
    				ssbnNodeList.add(ssbnNode); //TODO cuidado para não adicionar elementos repetidos na lista... 
    				
    				this.generateRecursive(ssbnNode, seen, net);	// algorithm's core
    				
    				ssbnNodeJacket.setArgumentsOfInputMFrag(); 
    				
    				if(!ssbnNode.isFinding()){
    					currentNode.addParent(ssbnNode, true);
    				}else{
    					currentNode.addParent(ssbnNode, false);
    				}
    			}else{
    				//TODO O que?
    			}
    		}
		}

		Debug.println(currentNode + "E:- generate CPT");
		
		if(currentNode.getContextFatherSSBNNode()!=null){
//			try {
//				currentNode.getContextFatherSSBNNode().generateCPT();
//				generateCPTForNodeWithContextFather(currentNode); 
//			} catch (InvalidOperationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SSBNNodeGeneralException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (MEBNException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}else{
		    generateCPT(currentNode);
		}
		
		Debug.println(currentNode.getResident().getName() + " return of input parents recursion"); 
		
		return currentNode;
	}

	/**
	 *
	 */
	private List<OVInstance> fillArguments(Collection<OVInstance> ovInstanceList, DomainResidentNode node) {
	
		List<OVInstance> ret = new ArrayList<OVInstance>(); 
		
		for(OVInstance ovInstance: ovInstanceList){
			if(node.getOrdinaryVariableByName(ovInstance.getOv().getName()) != null){
				ret.add(ovInstance); 
			}
		}
		
		return ret; 
	}

	
	/*
	 * Construct the CPT's for the nodes of the ssbn. 
	 */
	private ProbabilisticNetwork createCPTs(SSBNNode root){
		return null; 
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
	private SSBNNodeJacket getPreviousNode(SSBNNode currentNode, SSBNNodeList seen, 
			ProbabilisticNetwork net, DomainResidentNode residentNode, GenerativeInputNode inputNode) 
	        throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		Debug.println("Build Previous Node");
		//Find for ordereable object entity.
		List<OrdinaryVariable> ovOrdereableList = residentNode.getOrdinaryVariablesOrdereables();
		
		if(ovOrdereableList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneOrdereableVariable"));
		}
		
		if(ovOrdereableList.size() < 1){
			throw new ImplementationRestrictionException(resource.getString("RVNotRecursive"));
		}
		
		OrdinaryVariable ovOrdereableResident = ovOrdereableList.get(0); 
		Debug.println("Ov Ordereable found:" + ovOrdereableResident);
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
					new ProbabilisticNode(), false);
			
			ssbnNode = checkForDoubleSSBNNode(ssbnNode); //!! strange, but possible... 
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
			
			ssbnNodeList.add(ssbnNode);
			
			Debug.println("Build Previous Node End");
			return ssbnNodeJacket;
		}
	}
	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	private boolean evaluateRelatedContextNodes (DomainResidentNode residentNode, 
			List<OVInstance> ovInstances) throws OVInstanceFaultException{
		
		Debug.setDebug(true); 
		
		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (residentNode.getMFrag().isUsingDefaultCPT()) {
			return false;
		};
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		Collection<ContextNode> contextNodeList = residentNode.getMFrag().getContextByOVCombination(
				residentNode.getOrdinaryVariableList());
		
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getLabel());
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				residentNode.getMFrag().setAsUsingDefaultCPT(true); 
				Debug.println("Result = FALSE. Use default distribution ");
				return false;  
			}else{
				Debug.println("Result = TRUE.");
			}		
		}
		
		return true; 
	}

	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 * @throws OVInstanceFaultException 
	 */
	private boolean evaluateRelatedContextNodes(GenerativeInputNode inputNode, 
			List<OVInstance> ovInstances) throws OVInstanceFaultException{
		
		Debug.setDebug(true); 
		
		// We assume if MFrag is already set to use Default, then some context has failed previously and there's no need to evaluate again.		
		if (inputNode.getMFrag().isUsingDefaultCPT()) {
			return false;
		}
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		Collection<ContextNode> contextNodeList = inputNode.getMFrag().getContextByOVCombination(
				inputNode.getOrdinaryVariableList());
		
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getLabel()); 
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				inputNode.getMFrag().setAsUsingDefaultCPT(true); 
				Debug.println("Result = FALSE. Use default distribution ");
				return true;  
			}else{
				Debug.println("Result = TRUE.");
			}
			
			Debug.println(""); 
		}
		return true;
	}
	
	/**
	 * Search for a context node that recover the entities that math with the 
	 * ordinary variable list. Case this search returns entities, one SSBNNode 
	 * will be create for each entity (the entity is a argument of the fatherNode
	 * that is the node referenced by the SSBNNOde). Case this search don't return 
	 * entities, all the entities of the knowledge base will be recover and one SSBNNode
	 * will be create for each entity. Is this case a object ContextFatherSSBNNode 
	 * wiil be added in the ssbnnode orgin.  
	 * 
	 * @param mFrag
	 * @param avaliator
	 * @param ovList list of ov's for what don't have a value. (for this implementation, 
	 *                this list should contain only one element). 
	 * @param ovInstances
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private List<SSBNNode> createSSBNNodesOfEntitiesSearchForResidentNode(DomainMFrag mFrag, SSBNNode originNode, 
			DomainResidentNode fatherNode, List<OrdinaryVariable> ovList, List<OVInstance> ovInstances) 
			throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		//Complex case: evaluate search context nodes. 
		Debug.println("Have ord. variables incomplete!"); 
		
		if(ovList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("OrdVariableProblemLimit")); 
		}
		
		OrdinaryVariable ovProblematic = ovList.get(0);
		
		//search
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovList);
		
		if(contextNodeList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		if(contextNodeList.size() < 1){
			throw new SSBNNodeGeneralException(); 
		}
		
		//contextNodeList have only one element
		ContextNode context = contextNodeList.toArray(new ContextNode[contextNodeList.size()])[0];
		
		Debug.println("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
			if(result.isEmpty()){
				
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
						throw new ImplementationRestrictionException("Um nó não pode ter dois nós de contexto pais!");
					}
				}
				else{
					
					/*
					 * All the instances of the entity will be considered and the 
					 * context node will be father. 
					 */
					
					List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
					
					//Add the context node how father
					ContextFatherSSBNNode contextFatherSSBNNode = new ContextFatherSSBNNode(
							originNode.getProbabilisticNetwork(), context); 
					contextFatherSSBNNode.setOvProblematic(ovProblematic);

					//in this implementation only this is necessary, because the treat
					//of context nodes how fathers will be trivial, using the XOR 
					//strategy. For a future implementation that accept different 
					//distribuitions for the residentNode of the ContextNode, the
					//arguments of the residente will have to be fill with the OVInstances
					//for the analize of the resident node formula. (very complex!).  
					
					//Search for all the entities present in kb. 
					result = kb.getEntityByType(ovProblematic.getValueType().getName());
					for(String entity: result){
						SSBNNode node =  createSSBNNodeForEntitySearch(originNode.getProbabilisticNetwork(), 
								fatherNode, ovInstances, ovProblematic, entity);
						contextFatherSSBNNode.addPossibleValue(LiteralEntityInstance.getInstance(entity, ovProblematic.getValueType()));
						nodes.add(node); 
					}
					
					originNode.setContextFatherSSBNNode(contextFatherSSBNNode);
					
					return nodes;
				}
				  
			}
			else{
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
		}
	}

	private SSBNNode createSSBNNodeForEntitySearch(ProbabilisticNetwork probabilisticNetwork, 
			DomainResidentNode residentNode, List<OVInstance> ovInstances, OrdinaryVariable ov, String entity) {
		
		SSBNNode ssbnnode = null; 	
		
		List<OVInstance> arguments = fillArguments(ovInstances, residentNode);
		arguments.add(OVInstance.getInstance(ov, entity, ov.getValueType())); 
		
		SSBNNode testSSBNNode = getSSBNNode(residentNode, arguments); 
		
		if(testSSBNNode == null){
			ssbnnode =  SSBNNode.getInstance(probabilisticNetwork,residentNode);
			for(OVInstance ovInstance: arguments){
				ssbnnode.addArgument(ovInstance);
			}
			ssbnNodeList.add(ssbnnode);
		}else{
			ssbnnode = testSSBNNode;
		}
		
		return ssbnnode;
	
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
	private List<SSBNNodeJacket> createSSBNNodesOfEntitiesSearchForInputNode(SSBNNode originNode, 
			GenerativeInputNode fatherNode, List<OrdinaryVariable> ovProblemList, List<OVInstance> ovInstances) 
			throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		DomainMFrag mFrag = fatherNode.getMFrag(); 
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		//Complex case: evaluate search context nodes. 
		Debug.println("Have ord. variables incomplete!"); 
		
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
		
		Debug.println("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
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
						throw new ImplementationRestrictionException("Um nó não pode ter dois nós de contexto pais!");
					}
				}
				else{
					
					List<SSBNNodeJacket> nodes = new ArrayList<SSBNNodeJacket>(); 
					Debug.println("Result is empty"); 
//					Add the context node how father
					
					ContextFatherSSBNNode contextFatherSSBNNode = new ContextFatherSSBNNode(
							originNode.getProbabilisticNetwork(), context); 
					contextFatherSSBNNode.setOvProblematic(ov);
					
					//Search for all the entities present in kb. 
					result = kb.getEntityByType(ov.getValueType().getName());
					Debug.println("Search returns " + result.size() + " results"); 
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

	/* 
	 * version for input nodes. 
	 */
	private SSBNNodeJacket createSSBNNodeForEntitySearch(SSBNNode originNode, 
			GenerativeInputNode fatherNode, OrdinaryVariable ov, String entityName) 
	        throws SSBNNodeGeneralException {
		
		SSBNNode ssbnNode = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),
				(DomainResidentNode)fatherNode.getResidentNodePointer().getResidentNode(), new ProbabilisticNode(), false); 
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
		ssbnNode = checkForDoubleSSBNNode(ssbnNode);
		
		Debug.println(" ");
		Debug.println("SSBNNode created:" + ssbnNode.getName());
		Debug.println("Input MFrag Arguments: " + ssbnNodeJacket.getOvInstancesOfInputMFrag());
		Debug.println("Resident MFrag Arguments: " + ssbnNodeJacket.getOvInstancesOfResidentMFrag());
		Debug.println(" ");
		
		ssbnNodeList.add(ssbnNode);
		
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
	private SSBNNode checkForDoubleSSBNNode(SSBNNode ssbnnode) {
		
		SSBNNode testSSBNNode = getSSBNNode(ssbnnode.getResident(), ssbnnode.getArguments()); 
		
		if(testSSBNNode != null){
			ssbnnode.delete(); 
			ssbnnode = testSSBNNode; 
		}
		
		return ssbnnode;
		
	}
	
	/*
	 * This method is used for avoid the creation of double equals ssbnnodes
	 * (ssbnnodes with the same resident node and the same arguments, because this
	 * nodes will have the same probabilistic node). 
	 *  
	 * @param ssbnnode SSBNNode with the arguments setted. 
	 * @return
	 */
	private SSBNNode checkForDoubleSSBNNodeForInputEvaliation(SSBNNode ssbnnode) {
		
		SSBNNode testSSBNNode = getSSBNNode(ssbnnode.getResident(), ssbnnode.getArguments()); 
		
		if(testSSBNNode != null){
			/* Esta versão difere da anterior porque ao invés de retornar o ssbnnode já
			 * existente, ela retorna o mesmo ssbnnode que foi passado como parametro, 
			 * mudando apenas a referencia para o probabilistic node para o já 
			 * anteriormente existente. Isto é necessário porque precisaremos dos
			 * nomes dos argumentos referentes aquele input node especifico (diversos
			 * input nodes podem ter o mesmo probabilistic node como referencia, 
			 * porém nomes de argumentos diferentes, o que deve ser mantido para 
			 * a correta avaliação das cpt's dos nós filhos. 
			 * (Sorry for the portuguese...)
			 */
			ssbnnode.setProbNode(testSSBNNode.getProbNode()); 
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
	private void addArgumentToSSBNNodeOfInputNode(GenerativeInputNode inputNode, 
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
	
	/**
	 * Verifies if have OVInstances for all ordinary variable. Case OK, return 
	 * null, otherside, retur the list of ordinary variables that don't have a 
	 * OVInstance. 
	 * 
	 * @param ordVariableList
	 * @param ovInstanceList
	 * @return List of ordinary variables that don't have a OVInstance. 
	 */
	private List<OrdinaryVariable> analyseOVInstancesListCompletudeForOVList(
			       Collection<OrdinaryVariable> ordVariableList, Collection<OVInstance> ovInstanceList){
    	
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
	 * Return the SSBNNode for the residentNode with the ovInstanceList how 
	 * arguments. 
	 * @param residentNode
	 * @param ovInstanceList
	 * @return
	 */
	private SSBNNode getSSBNNode(DomainResidentNode residentNode, Collection<OVInstance> ovInstanceList){
		
		for(SSBNNode ssbnNode : ssbnNodeList){
			if(ssbnNode.getResident() == residentNode){
				Collection<OVInstance> ssbnNodeArguments = ssbnNode.getArguments(); 
				if(ovInstanceList.size()!= ssbnNodeArguments.size()){
					return null; 
				}else{
					for(OVInstance ovInstance: ovInstanceList){
						boolean find = false; 
						for(OVInstance ssbnNodeArgument: ssbnNodeArguments){
							if(ssbnNodeArgument.equals(ovInstance)){
								find = true; 
								break; 
							}
						}
						if(!find){
							return null; 
						}
					}
					//Find all the ovInstances! Is the same SSBNNode
					return ssbnNode; 
				}
			}
		}
		
		return null; 
	}

	

	/*
	 * Generate the cpt for the ssbnNode
	 * 
	 * out-assertives:
	 * - The CPT of the probabilistic node referenced by the ssbnNode is setted
	 *   with the CPT generated. 
	 */
	private void generateCPT(SSBNNode ssbnNode) {
		try {
			
			Debug.println("\nGenerate table for node " + ssbnNode);
			Debug.println("Parents:");
			for(SSBNNode parent: ssbnNode.getParents()){
				Debug.println("  " + parent);
			}
			
			Debug.setDebug(false);
			
			ssbnNode.getCompiler().generateCPT(ssbnNode);
			
			Debug.setDebug(true);
			Debug.println("CPT OK\n");
		
		} catch (MEBNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void generateCPTForNodeWithContextFather(SSBNNode ssbnNode) throws SSBNNodeGeneralException, MEBNException {
//		try {
			
			Debug.println("\nGenerate table for node (with context father): " + ssbnNode);
			Debug.println("Parents:");
			for(SSBNNode parent: ssbnNode.getParents()){
				Debug.println("  " + parent);
			}
			
			Debug.setDebug(false);
			
			Map<String, List<SSBNNode>> mapParentsByEntity = new HashMap<String, List<SSBNNode>>(); 
			Map<String, PotentialTable> mapCPTByEntity = new HashMap<String, PotentialTable>(); 
			
			ContextFatherSSBNNode contextFather = ssbnNode.getContextFatherSSBNNode();
			OrdinaryVariable ovProblematic = contextFather.getOvProblematic(); 
			
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				mapParentsByEntity.put(entity.getInstanceName(), new ArrayList<SSBNNode>()); 
			}
			
			//Step 0: Calcular a tabela do nó de contexto
			
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
				ssbnNode.removeParent(ssbnNode); //TODO Conferir arcos...
			}
			
			//Step 2: Construir as tabelas para os diversos grupos de pais
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				
				ArrayList<SSBNNode> groupParents = new ArrayList<SSBNNode>(); 
				groupParents.addAll(mapParentsByEntity.get(entity.getInstanceName())); 
				groupParents.addAll(generalParents); 
				for(SSBNNode parent: groupParents){
					ssbnNode.addParent(parent, false); 
				}
				PotentialTable cpt = ssbnNode.getCompiler().generateCPT(ssbnNode); 
				mapCPTByEntity.put(entity.getInstanceName(), cpt);
				for(SSBNNode parent: groupParents){
					ssbnNode.removeParent(parent); 
				}
				
			}			
			
			//Step 3: Fazer o XOR das tabelas obtidas utilizando a tabela do nó de contexto
			
			
			
			//Step 4: Setar a tabela gerada como a CPT do ssbnNode
			
			Debug.setDebug(true);
			Debug.println("CPT OK\n");
		
//		} catch (MEBNException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	
	/*-------------------------------------------------------------------------
	 * Debug Methods 
	 *------------------------------------------------------------------------/
	
	/*
	 * debug method. 
	 */
	private void printParents(SSBNNode node, int nivel){
		for(SSBNNode parent: node.getParents()){
			for(int i = 0; i < nivel; i++){
				System.out.print("   "); 
			}
			System.out.println(parent.toString());
			printParents(parent, nivel + 1); 
		}
	}


	/*
	 * This debug method print the informations about the network build by
	 * the SSBN algorith and save a file xmlbif with the bayesian network build. 
	 * 
	 * @param querynode
	 */
	private void printNetworkInformation(SSBNNode querynode) {
		Debug.println("\n"); 
		Debug.println("Network: "); 
		Debug.println("QueryNode = " + querynode.toString());
		printParents(querynode, 0); 
		
		Debug.println("\nEdges:");
		for(Edge edge: querynode.getProbabilisticNetwork().getEdges()){
			Debug.println(edge.toString());
		}
		
		Debug.println("\nNodes:");
		for(int i = 0; i < querynode.getProbabilisticNetwork().getNodes().size(); i++){
			Debug.println(querynode.getProbabilisticNetwork().getNodeAt(i).toString());
		}
		
	    XMLIO netIO = new XMLIO(); 
		
		try {
			netIO.save(new File("rede.xml"), querynode.getProbabilisticNetwork());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private void printNodeStructureBeforeCPT(SSBNNode ssbnNode){
		System.out.println("--------------------------------------------------");
		System.out.println("- Node: " + ssbnNode.toString());
		System.out.println("- Parents: ");
		for(SSBNNode parent: ssbnNode.getParents()){
			System.out.println("-    " + parent.getName());
			System.out.println("-            Arguments: ");
			for(OVInstance ovInstance: parent.getArguments()){
				System.out.println("-                " + ovInstance.toString());
			}
		}
		System.out.println("--------------------------------------------------");	
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
	private class SSBNNodeJacket{
		
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

		public Collection<OVInstance> getOvInstancesOfInputMFrag() {
			return ovInstancesOfInputMFrag;
		}

//		public void setOvInstancesOfInputMFrag(
//				List<OVInstance> ovInstancesOfInputMFrag) {
//			this.ovInstancesOfInputMFrag = ovInstancesOfInputMFrag;
//		}

		public void addOVInstanceOfInputMFrag(OVInstance ovInstance){
			this.ovInstancesOfInputMFrag.add(ovInstance);
		}
		
		public Collection<OVInstance> getOvInstancesOfResidentMFrag() {
			return ovInstancesOfResidentMFrag;
		}
		

//		public void setOvInstancesOfResidentMFrag(
//				List<OVInstance> ovInstancesOfResidentMFrag) {
//			this.ovInstancesOfResidentMFrag = ovInstancesOfResidentMFrag;
//		}

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