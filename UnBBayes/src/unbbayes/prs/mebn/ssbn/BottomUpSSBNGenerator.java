/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import unbbayes.io.XMLIO;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * Algorith for generate the Situation Specific Bayesian Network.  
 * 
 * @author Shou Matsumoto
 * @author Laecio Lima dos Santos
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException, 
	                                                             ImplementationRestrictionException {
		
		ssbnNodeList = new ArrayList<SSBNNode>(); 
		
		ProbabilisticNetwork network = null; 
		
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
		
		network = this.createCPTs(root); 
		
		//Debug. 
		this.printNetworkInformation(querynode); //only Debug informations
		
		return network;
	}
	
	
	
	
	
	/**
	 * The recursive part of SSBN bottom-up generation algorithm
	 * 
	 * Pre-requisites: 
	 *     - The node current node has a OVInstance for all OrdinaryVariable argument
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
			//TODO. 
//			throw new SSBNNodeGeneralException(this.resource.getString("CycleFound"));
			            /* Analisar melhor isto, pois um nó ter que ser obrigado a 
			             * ser avaliado duas vezes não quer necessariamente dizer que
			             * há um ciclo na rede. ZoneNature, por exemplo, será analisado
			             * duas vezes porque é pai de dois nós residentes. Achar uma
			             * forma de manter esta informação e o nó anteriormente
			             * criado para ZoneNature automaticamente virar o pai das
			             * chamadas posteriores. 
			             */
		}
		 
		
		
		
		
		//------------------------- STEP 1: search findings. -------------------
		
        //check if querynode has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = kb.searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			seen.add(currentNode); 
			Debug.println("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
 
		
		
		
		
		//------------------------- STEP 2: analyse context nodes. -------------
		
		// evaluates querynode's mfrag's context nodes 
		//(if not OK, sets MFrag's flag to use default CPT)	
		
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 
        
		boolean result = false;
		
		try {
			result = evaluateRelatedContextNodes(currentNode.getResident(), ovInstancesList);
		} catch (OVInstanceFaultException e) {
			//pre-requisite
			e.printStackTrace();
		}
		
		if(!result){
			Debug.println("Context Node fail for " + currentNode.getResident().getMFrag()); 
			return currentNode; 
		}
		
		
		
		
		
       //------------------------- STEP 3: Add resident nodes fathers -------------
		
		for (DomainResidentNode residentNode : currentNode.getResident().getResidentNodeFatherList()) {

			/*
			 * Analyse if have one ov instance for each ordinary variable. If this 
			 * don't is true, verify if have some context node able to recover 
			 * the instances that match with the ordinary variable.  
			 */
            List<OrdinaryVariable> ovProblemList = analyseOVInstancesListCompletudeForOVList(
            		residentNode.getOrdinaryVariableList(), currentNode.getArguments()); 
            
    		if(!ovProblemList.isEmpty()){
    			List<SSBNNode> createdNodes = createSSBNNodesForEntitiesSearch(
    					residentNode.getMFrag(), currentNode, residentNode, 
    					ovProblemList, ovInstancesList); 
    			
    		    for(SSBNNode ssbnnode: createdNodes){
    		    	
    		    	if(!ssbnnode.isContext()){
    		    		generateRecursive(ssbnnode, seen, net);	
    		    	}
    		    	
    		    	if(!ssbnnode.isFinding() && !ssbnnode.isContext()){
    		    		currentNode.addParent(ssbnnode, true);
    		    		net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
    		    	}else{
    		    		currentNode.addParent(ssbnnode, false);
    		    	}	
    		    }
    		    
    		}else{
    			SSBNNode ssbnnode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode(), false);
				fillArguments(currentNode.getArguments(), residentNode, ssbnnode);
				
    			generateRecursive(ssbnnode, seen, net);	
    			if(!ssbnnode.isFinding()){
    				currentNode.addParent(ssbnnode, true);
    				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
    			}else{
    				currentNode.addParent(ssbnnode, false); 
    			}
    		}
			
		}
		
		Debug.println(currentNode.getResident().getName() + " return of resident parents recursion"); 
		
		
		
		
		
	    //------------------------- STEP 4: Add input nodes fathers -------------
		
		for (GenerativeInputNode inputNode : currentNode.getResident().getInputNodeFatherList()) {
			DomainResidentNode residentNode = 
				(DomainResidentNode)inputNode.getResidentNodePointer().getResidentNode(); 

			//Evaluate recursion... 
			if(currentNode.getResident() == residentNode){
				treatRandonVariableRecursion(currentNode, seen, net, residentNode);
				break; 
			}
			
			//Step 1: evaluate context and search for findings
            List<OVInstance> listOVInstances = new ArrayList<OVInstance>(); 
            listOVInstances.addAll(currentNode.getArguments()); 
            Debug.println(currentNode.getName() + " Evaluate input " + residentNode.getName()); 
			
            List<OrdinaryVariable> ovProblemList = 
            	analyseOVInstancesListCompletudeForOVList(inputNode.getOrdinaryVariableList(), 
            			currentNode.getArguments()); 
            
    		if(!ovProblemList.isEmpty()){
    			
    			List<SSBNNode> parentList = createSSBNNodesForEntitiesSearch(
    					inputNode.getMFrag(), currentNode, inputNode, ovProblemList, listOVInstances); 
    			
    		    for(SSBNNode ssbnnode: parentList){
    		    	Debug.println("Node Created: " + ssbnnode.toString());
    		    	if(!ssbnnode.isContext()){
    		    		generateRecursive(ssbnnode, seen, net);	// algorithm's core
    		    	}
        			if(!ssbnnode.isFinding()  && !ssbnnode.isContext()){
        				currentNode.addParent(ssbnnode, true);
        				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
        			}else{
        				currentNode.addParent(ssbnnode, false);
        			}
    		    }
    		}else{
    			boolean contextNodesOK = false;
				
    			try {
					contextNodesOK = evaluateRelatedContextNodes(inputNode, listOVInstances);
				} catch (OVInstanceFaultException e) {
					//ovProblemList is empty... this exception never will be catch.
					e.printStackTrace();
				} 
				
    			if(contextNodesOK){
    				SSBNNode ssbnnode = SSBNNode.getInstance(net, residentNode, new ProbabilisticNode(), false);
    				
    				for(OVInstance instance: currentNode.getArguments()){
    					addArgumentToSSBNNode(inputNode, residentNode, ssbnnode, instance);
    				}
    				
    				this.generateRecursive(ssbnnode, seen, net);	// algorithm's core
    				
    				if(!ssbnnode.isFinding()){
    					currentNode.addParent(ssbnnode, true);
    					net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
    				}else{
    					currentNode.addParent(ssbnnode, false);
    				}
    			}
    		}
		}
		
		Debug.println(currentNode.getResident().getName() + " return of input parents recursion"); 
		
		return currentNode;
	}

	/**
	 *
	 */
	private void fillArguments(Collection<OVInstance> ovInstanceList, DomainResidentNode node, SSBNNode fatherNode) {
		for(OVInstance ovInstance: ovInstanceList){
			if(node.getOrdinaryVariableByName(ovInstance.getOv().getName()) != null){
				fatherNode.addArgument(ovInstance); 
			}
		}
	}

	
	/*
	 * Construct the CPT's for the nodes of the ssbn. 
	 */
	private ProbabilisticNetwork createCPTs(SSBNNode root){
		return null; 
	}
	
	
	/**
	 * Evaluate of recursion in the MEBN model. 
	 * 
	 * @param currentNode
	 * @param seen
	 * @param net
	 * @param residentNode
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	private void treatRandonVariableRecursion(SSBNNode currentNode, SSBNNodeList seen, 
			ProbabilisticNetwork net, DomainResidentNode residentNode) 
	        throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		//Valuate if the node is recursive. 
		
		
		//Call recursion
		OrdinaryVariable ovOrdereable = null; 
		ObjectEntity objectEntityOrdereable = null; 
		
		//Find for ordereable object entity. 
		for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
			ObjectEntity oe = currentNode.getResident().getMFrag().
			                        getMultiEntityBayesianNetwork().getObjectEntityContainer().
			                        getObjectEntityByType(ov.getValueType()); 
			if(oe.isOrdereable()){
				objectEntityOrdereable = oe; 
				ovOrdereable = ov; 
				break; 
			}
		}
		
		if(objectEntityOrdereable == null){
			throw new SSBNNodeGeneralException();
		}
		
		OVInstance ovInstanceOrdereable = null; 
		for(OVInstance ovInstance: currentNode.getArguments()){
			if(ovInstance.getOv() == ovOrdereable){
				ovInstanceOrdereable = ovInstance; 
				break; 
			}
		}
		
		if(ovInstanceOrdereable == null){
			throw new SSBNNodeGeneralException();
		}
		
		String nameEntity = ovInstanceOrdereable.getEntity().getInstanceName(); 
		
		ObjectEntityInstanceOrdereable objectEntityInstance = 
			(ObjectEntityInstanceOrdereable)objectEntityOrdereable.getInstanceByName(nameEntity);
		
		if(objectEntityInstance == null){
			throw new SSBNNodeGeneralException();
		}
		
		ObjectEntityInstanceOrdereable prev = objectEntityInstance.getPrev(); 
		
		if(prev == null){
			return; //end of the recursion
		}else{

			OVInstance newOvInstance = OVInstance.getInstance(ovOrdereable, 
					prev.getName(), ovOrdereable.getValueType());
			
			//Create the new SSBNNode with the setted values. 
			SSBNNode ssbnnode = SSBNNode.getInstance(net, residentNode, 
					new ProbabilisticNode(), false);
			
			for(OVInstance instance: currentNode.getArguments()){
				if(instance != ovInstanceOrdereable){
					if(residentNode.getOrdinaryVariableByName(instance.getOv().getName()) != null){
						ssbnnode.addArgument(instance); 
					}
				}else{
					ssbnnode.addArgument(newOvInstance);
				}
			}

			this.generateRecursive(ssbnnode, seen, net);	// algorithm's core
			
			if(!ssbnnode.isFinding()){
				currentNode.addParent(ssbnnode, true);
				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
			}else{
				currentNode.addParent(ssbnnode, false);
			}
			
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
		}
		
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
	 * will be create for each entity, and a SSBNNode will be create for the
	 * context node that don't return values. 
	 * 
	 * @param mFrag
	 * @param avaliator
	 * @param ovList list of ov's for what don't have a value. (for this implementation, 
	 *                this list should contain only one element). 
	 * @param ovInstances
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	private List<SSBNNode> createSSBNNodesForEntitiesSearch(DomainMFrag mFrag, SSBNNode originNode, 
			DomainResidentNode fatherNode, List<OrdinaryVariable> ovList, List<OVInstance> ovInstances) 
			throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		//Complex case: evaluate search context nodes. 
		Debug.println("Have ord. variables incomplete!"); 
		
		if(ovList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("OrdVariableProblemLimit")); 
		}
		
		//search
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovList);
		
		if(contextNodeList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		if(contextNodeList.size() < 1){
			throw new SSBNNodeGeneralException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		
		//contextNodeList have only one element
		ContextNode context = contextNodeList.toArray(new ContextNode[contextNodeList.size()])[0];
		OrdinaryVariable ov = ovList.get(0);
		
		Debug.println("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
			if(result.isEmpty()){
				
				/*
				 * All the instances of the entity will be considered and the 
				 * context node will be father. 
				 */
				
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
		        
				//Add the context node how father
				DomainResidentNode residentNode = context.getNodeSearch(ovInstances); 
				SSBNNode nodeContext = SSBNNode.getInstance(originNode.getProbabilisticNetwork(), residentNode); 
				nodeContext.setNodeAsContext(); 
				nodes.add(nodeContext); 
				    //in this implementation only this is necessary, because the treat
				    //of context nodes how fathers will be trivial, using the XOR 
				    //strategy. For a future implementation that accept different 
				    //distribuitions for the residentNode of the ContextNode, the
				    //arguments of the residente will have to be fill with the OVInstances
				    //for the analize of the resident node formula. (very complex!).  
			    
				//Search for all the entities present in kb. 
				result = kb.getEntityByType(ov.getValueType().getName());
				for(String entity: result){
					SSBNNode node =  createSSBNNodeForEntitySearch(originNode.getProbabilisticNetwork(), 
							fatherNode, ovInstances, ov, entity);
		        	nodes.add(node); 
				}
				return nodes;  
			}
			else{
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				for(String entity: result){
					SSBNNode node = createSSBNNodeForEntitySearch(originNode.getProbabilisticNetwork(), 
							fatherNode, ovInstances, ov, entity);
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
		
		OVInstance instance = OVInstance.getInstance(ov, entity, ov.getValueType()); 
		SSBNNode node = SSBNNode.getInstance(probabilisticNetwork,residentNode); 
		node.addArgument(instance); 
		fillArguments(ovInstances, residentNode, node);
		
		return node;
	
	}
	
	/**
	 * ...for input nodes fathers (because this nodes demand a different treatment
	 * of the arguments). 
	 * 
	 * @param mFrag
	 * @param originNode
	 * @param fatherNode
	 * @param ovList
	 * @param ovInstances
	 * @return
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException 
	 */
	private List<SSBNNode> createSSBNNodesForEntitiesSearch(DomainMFrag mFrag, SSBNNode originNode, 
			GenerativeInputNode fatherNode, List<OrdinaryVariable> ovList, List<OVInstance> ovInstances) 
			throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		//Complex case: evaluate search context nodes. 
		Debug.println("Have ord. variables incomplete!"); 
		
		if(ovList.size() > 1){
			throw new ImplementationRestrictionException(resource.getString("OrdVariableProblemLimit")); 
		}
		
		//search 
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovList);
		int size = contextNodeList.size(); 
		
		if(size > 1){
			throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		if(size < 1){
			throw new SSBNNodeGeneralException(resource.getString("MoreThanOneContextNodeSearh")); 
		}
		
		ContextNode context = contextNodeList.toArray(new ContextNode[size])[0];
		OrdinaryVariable ov = ovList.get(0);
		
		Debug.println("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
			if(result.isEmpty()){

				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				Debug.println("Result is empty"); 
//				Add the context node how father
				DomainResidentNode domainResidentNode = context.getNodeSearch(ovInstances); 
				SSBNNode nodeContext = SSBNNode.getInstance(originNode.getProbabilisticNetwork(), 
						domainResidentNode, new ProbabilisticNode(), false); 
				nodeContext.setNodeAsContext(); 
				nodes.add(nodeContext); 
				
				//Search for all the entities present in kb. 
				result = kb.getEntityByType(ov.getValueType().getName());
				Debug.println("Search returns " + result.size() + " results"); 
				DomainResidentNode residentNode = 
					(DomainResidentNode)fatherNode.getResidentNodePointer().getResidentNode(); 
		        for(String entity: result){
		        	SSBNNode ssbnnode = createSSBNNodeForEntitySearch(originNode, 
		        			fatherNode, ov, residentNode, entity);
					nodes.add(ssbnnode); 
				}
				return nodes;  
			}
			else{
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				DomainResidentNode residentNode = 
					(DomainResidentNode)fatherNode.getResidentNodePointer().getResidentNode(); 
				for(String entity: result){
					SSBNNode ssbnnode = createSSBNNodeForEntitySearch(originNode, 
							fatherNode, ov, residentNode, entity);
					nodes.add(ssbnnode); 
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
	private SSBNNode createSSBNNodeForEntitySearch(SSBNNode originNode, 
			GenerativeInputNode fatherNode, OrdinaryVariable ov, 
			DomainResidentNode residentNode, String entityName) throws SSBNNodeGeneralException {
		
		SSBNNode ssbnnode = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),
				residentNode, new ProbabilisticNode(), false); 
		
		addArgumentToSSBNNode(fatherNode, residentNode, ssbnnode,  
				OVInstance.getInstance(ov, entityName, ov.getValueType()));	
		
		for(OVInstance instance: originNode.getArguments()){
			addArgumentToSSBNNode(fatherNode, residentNode, ssbnnode, instance);
		}
		return ssbnnode;
	}

	/**
	 * Add instance how a argument of ssbnnode. ResidentNode is the node of
	 * the SSBNNode and is the reference of the input node. 
	 */
	private void addArgumentToSSBNNode(GenerativeInputNode inputNode, 
			DomainResidentNode residentNode, SSBNNode ssbnnode, OVInstance instance) 
	        throws SSBNNodeGeneralException {
		
		OrdinaryVariable ovOrigin = instance.getOv(); 
		int index = inputNode.getResidentNodePointer().getOrdinaryVariableIndex(ovOrigin);
		if(index > -1){
			OrdinaryVariable destination = residentNode.getOrdinaryVariableList().get(index);
			ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
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
	
	
}