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
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormula;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 * @author Laecio Lima dos Santos
 */
public class BottomUpSSBNGenerator implements ISSBNGenerator {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private KBFacade kb = null;
	
	private MFrag mfrag = null;
	
	private final long RECURSIVE_CALL_LIMIT = 999999999999999999L;
	
	private long recursiveCallCount = 0;
	
	/**
	 * 
	 */
	public BottomUpSSBNGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException {
		
		// As the query starts, let's clear the flags used by the previous query
		query.getMebn().clearMFragsIsUsingDefaultCPTFlag();
		
		// some data extraction
		
		SSBNNode querynode = query.getQueryNode();
		this.kb = query.getKb();

		// initialization

		// call recursive
		this.recursiveCallCount = 0;
		this.generateRecursive(querynode, new SSBNNodeList(), querynode.getProbabilisticNetwork());
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return querynode.getProbabilisticNetwork();
	}
	
	/*
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 */
	private boolean contextNodeEvaluation (SSBNNode queryNode) {
		
		Debug.setDebug(true); 
		
		for(OVInstance ovInstance: queryNode.getArguments()){
			Debug.println("OVInstance = " + ovInstance);
		}
		
		DomainMFrag mFrag = queryNode.getResident().getMFrag();
		
		// We assume if MFrag is already set to use Default, then some context has failed previously and there's no need to evaluate again.		
		if (mFrag.isUsingDefaultCPT()) {
			return false;
		}
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB(), kb); 
		
		List<OrdinaryVariable> ovList = queryNode.getResident().getOrdinaryVariableList(); 
		
		List<OVInstance> ovInstances = new ArrayList<OVInstance>(); 
		ovInstances.addAll(queryNode.getArguments()); 
		
		Collection<ContextNode> contextNodeList = mFrag.getContextByOVCombination(ovList);
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getLabel()); 
			try{
				if(!avaliator.evaluateContextNode(context, ovInstances)){
					mFrag.setAsUsingDefaultCPT(true); 
					Debug.println("Result = FALSE. Use default distribution ");
					return false;  
				}else{
					Debug.println("Result = TRUE.");
				}
			}
			catch(OVInstanceFaultException e){
				e.printStackTrace(); 
			}
			Debug.println("");
		}
		
		return true; 
		
	}

	/**
	 *
	 * 
	 * @param mFrag
	 * @param avaliator
	 * @param ovList list of ov's for what don't have a value. (for this implementation, 
	 *                this list should contain only one element). 
	 * @param ovInstances
	 */
	private List<SSBNNode> createSSBNNodesForEntitiesSearch(DomainMFrag mFrag, SSBNNode originNode, 
			DomainResidentNode fatherNode, List<OrdinaryVariable> ovList, List<OVInstance> ovInstances) {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB(), kb); 
		
		//Complex case: evaluate search context nodes. 
		Debug.println("Have ord. variables incomplete!"); 
		
		if(ovList.size() > 1){
			//TODO lancar exception
			return null; 
		}
		//busca... 
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovList);
		int size = contextNodeList.size(); 
		
		if(size!=1){
			Debug.print("More than one search node... implementation incomplete."); 
			//TODO throws a exception
			return null; 
		}
		
		ContextNode context = contextNodeList.toArray(new ContextNode[size])[0];
		OrdinaryVariable ov = ovList.get(0);
		
		Debug.println("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
			if(result.isEmpty()){
				//Add the context node how father
				//TODO...
				
				//Search for all the entities present in kb. 
				result = kb.getEntityByType(ov.getValueType().getName());
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
		        for(String entity: result){
		        	OVInstance instance = OVInstance.getInstance(ov, entity, ov.getValueType()); 
					SSBNNode node = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),fatherNode); 
					node.addArgument(instance); 
					for(OVInstance ovInstance: ovInstances){
						node.addArgument(ovInstance); 
					}
					nodes.add(node); 
				}
				return nodes;  
			}
			else{
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				for(String entity: result){
					OVInstance instance = OVInstance.getInstance(ov, entity, ov.getValueType()); 
					SSBNNode node = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),fatherNode); 
					node.addArgument(instance); 
					for(OVInstance ovInstance: ovInstances){
						node.addArgument(ovInstance); 
					}
					nodes.add(node); 
				}
				return nodes; 
			}
			
		} catch (InvalidContextNodeFormula ie) {
			Debug.println("Invalid Context Node: the formula don't is accept."); 
			// TODO throw exception
			ie.printStackTrace();
			return null;
		}
	}
	
	private List<SSBNNode> createSSBNNodesForEntitiesSearchForInputNodes(DomainMFrag mFrag, SSBNNode originNode, 
			GenerativeInputNode fatherNode, List<OrdinaryVariable> ovList, List<OVInstance> ovInstances) throws SSBNNodeGeneralException {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB(), kb); 
		
		//Complex case: evaluate search context nodes. 
		Debug.println("Have ord. variables incomplete!"); 
		
		if(ovList.size() > 1){
			//TODO lancar exception
			return null; 
		}
		//busca... 
		Collection<ContextNode> contextNodeList = mFrag.getSearchContextByOVCombination(ovList);
		int size = contextNodeList.size(); 
		
		if(size!=1){
			Debug.print("More than one search node... implementation incomplete."); 
			//TODO throws a exception
			return null; 
		}
		
		ContextNode context = contextNodeList.toArray(new ContextNode[size])[0];
		OrdinaryVariable ov = ovList.get(0);
		
		Debug.println("Context Node: " + context.getLabel()); 
		
		try {
			List<String> result = avaliator.evalutateSearchContextNode(context, ovInstances);
			
			if(result.isEmpty()){
				Debug.println("Result is empty"); 
				//Add the context node how father
				//TODO...
				
				//Search for all the entities present in kb. 
				result = kb.getEntityByType(ov.getValueType().getName());
				Debug.println("Search returns " + result.size() + " results"); 
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				DomainResidentNode residentNode = (DomainResidentNode)fatherNode.getResidentNodePointer().getResidentNode(); 
		        for(String entity: result){
		        	SSBNNode ssbnnode = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),residentNode); 
					{
						OVInstance instance = OVInstance.getInstance(ov, entity, ov.getValueType()); 
						OrdinaryVariable ovOrigin = instance.getOv(); 
	            		int index = fatherNode.getResidentNodePointer().getOrdinaryVariableIndex(ovOrigin);
	            		if(index > -1){
	            			OrdinaryVariable destination = residentNode.getOrdinaryVariableList().get(index);
	            			ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
	            		}	
					}
					
	            	for(OVInstance instance: originNode.getArguments()){
	            		OrdinaryVariable ovOrigin = instance.getOv(); 
	            		int index = fatherNode.getResidentNodePointer().getOrdinaryVariableIndex(ovOrigin);
	            		if(index > -1){
	            			OrdinaryVariable destination = residentNode.getOrdinaryVariableList().get(index);
	            			ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
	            		}
	            	}
	            	
					nodes.add(ssbnnode); 
				}
				return nodes;  
			}
			else{
				List<SSBNNode> nodes = new ArrayList<SSBNNode>(); 
				DomainResidentNode residentNode = (DomainResidentNode)fatherNode.getResidentNodePointer().getResidentNode(); 
				for(String entity: result){
		        	SSBNNode ssbnnode = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),residentNode); 
					{
						OVInstance instance = OVInstance.getInstance(ov, entity, ov.getValueType()); 
						OrdinaryVariable ovOrigin = instance.getOv(); 
	            		int index = fatherNode.getResidentNodePointer().getOrdinaryVariableIndex(ovOrigin);
	            		if(index > 0){
	            			OrdinaryVariable destination = residentNode.getOrdinaryVariableList().get(index);
	            			ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
	            		}	
					}
					
	            	for(OVInstance instance: originNode.getArguments()){
	            		OrdinaryVariable ovOrigin = instance.getOv(); 
	            		int index = fatherNode.getResidentNodePointer().getOrdinaryVariableIndex(ovOrigin);
	            		if(index > 0){
	            			OrdinaryVariable destination = residentNode.getOrdinaryVariableList().get(index);
	            			ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
	            		}
	            	}
	            	
					nodes.add(ssbnnode); 
				}
				
				//DEBUG
				for(SSBNNode node: nodes){
					Debug.println("Create Node:" + node.getResident().getName() ); 
				    for(OVInstance ovInstance: node.getArguments()){
				    	Debug.println(ovInstance.toString()); 
				    }
				}
				
				return nodes; 
			}
			
		} catch (InvalidContextNodeFormula ie) {
			Debug.println("Invalid Context Node: the formula don't is accept."); 
			// TODO throw exception
			ie.printStackTrace();
			return null;
		}
	}

	private boolean evaluateSimpleContextNodes(DomainMFrag mFrag, List<OVInstance> ovInstances, List<OrdinaryVariable> ovList) {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB(), kb); 
		Collection<ContextNode> contextNodeList = mFrag.getContextByOVCombination(ovList);
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getLabel()); 
			try{
				if(!avaliator.evaluateContextNode(context, ovInstances)){
					mFrag.setAsUsingDefaultCPT(true); 
					Debug.println("Result = FALSE. Use default distribution ");
					return true;  
				}else{
					Debug.println("Result = TRUE.");
//						return; 
				}
			}
			catch(OVInstanceFaultException e){
				e.printStackTrace();
			}
			
			Debug.println(""); 
		}

		return true;
	}
	
	/**
	 * Verifies if have OVInstances for all ordinary variable. 
	 * 
	 * @param ordVariableList
	 * @param ovInstanceList
	 * @return List of ordinary variables that don't have a OVInstance. 
	 */
	private List<OrdinaryVariable> avaliateOVInstancesInformation(Collection<OrdinaryVariable> ordVariableList, Collection<OVInstance> ovInstanceList){
    	
    	List<OrdinaryVariable> ret = new ArrayList<OrdinaryVariable>(); 
    	
    	for(OrdinaryVariable ov: ordVariableList){
    		
    		boolean found = false; 
    		for(OVInstance ovInstance: ovInstanceList){
    			if(ov.equals(ovInstance.getOv())){
    				found = true; 
    				break; 
    			}
    		}
    		
    		if(!found) ret.add(ov); 
    		
    	}
    	
    	return ret; 
    	
		
	}
	
	/*
	 * The recursive part of SSBN bottom-up generation algorithm
	 * currentNode is the node currently analized
	 * seen is all nodes analized previously (doesnt contain currentNode)
	 * net is the resulting ProbabilisticNetwork
	 */
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen, ProbabilisticNetwork net) throws SSBNNodeGeneralException {
		
		if (this.recursiveCallCount > this.RECURSIVE_CALL_LIMIT) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}
		this.recursiveCallCount++;
		
		Debug.println("\n-------------Passo: " + currentNode.getName() + "--------------\n"); 
		
		// check for cycle
		if (seen.contains(currentNode)) {
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
		
		// check if querynode has a known value or it should be a probabilistic node (query the kb)
		         
		// Extract arguments
		StateLink exactValue = kb.searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			                   /* Não serao gerados nós probabilisticos para os
			                    * finding, mas note que existe o SSBNNode. 
			                    */
			currentNode.setNodeAsFinding(exactValue.getState());
			Debug.println("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		// evaluates querynode's mfrag's context nodes (if not OK, sets MFrag's flag to use default CPT)	
		
		boolean result = contextNodeEvaluation(currentNode);
		               /* verificar se devemos analizar mais alguma coisa
		                * caso result = false (esta MFrag já estará marcada 
		                * para utilizar a distribuição default. Não faz muito
		                * sentido, pois os nós pais não terão vo instances
		                * com valores aceitáveis. 
		                */
		 
		// extract parents to treat them recursively
		List<SSBNNode> parents = new ArrayList<SSBNNode>();
		
		// Extract resident nodes

		for (DomainResidentNode residentNode : currentNode.getResident().getResidentNodeFatherList()) {
			
			List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
			ovInstancesList.addAll(currentNode.getArguments()); 
            
            List<OrdinaryVariable> ovProblemList = avaliateOVInstancesInformation(residentNode.getOrdinaryVariableList(), currentNode.getArguments()); 
            
    		if(!ovProblemList.isEmpty()){
    			List<SSBNNode> parentList = createSSBNNodesForEntitiesSearch(
    					residentNode.getMFrag(), currentNode, residentNode, 
    					ovProblemList, ovInstancesList); 
    		    for(SSBNNode ssbnnode: parentList){
    		    	parents.add(ssbnnode);
        			generateRecursive(ssbnnode, seen, net);	// algorithm's core
        			if(!ssbnnode.isFinding()){
        				currentNode.addParent(ssbnnode, true);
        				          /* Aqui está algo a se analizar: quando um nó
        				           * for um finding ele será acrescentado como pai 
        				           * na rede ou não (pelo menos o SSBNNode, sem
        				           * levar em consideração o nó probabilistico). 
        				           * Esta versão inicial não considera isto. 
        				           */
        				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
        			}
    		    }
    		}else{
    			SSBNNode ssbnnode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode(), false);
				for(OVInstance ovInstance: currentNode.getArguments()){
					ssbnnode.addArgument(ovInstance); 
				}
    			parents.add(ssbnnode);
    			generateRecursive(ssbnnode, seen, net);	// algorithm's core
    			if(!ssbnnode.isFinding()){
    				currentNode.addParent(ssbnnode, true);
    				          /* Aqui está algo a se analizar: quando um nó
    				           * for um finding ele será acrescentado como pai 
    				           * na rede ou não (pelo menos o SSBNNode, sem
    				           * levar em consideração o nó probabilistico). 
    				           * Esta versão inicial não considera isto. 
    				           */
    				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
    			}
    		}
			
		}
		
		Debug.println(currentNode.getResident().getName() + " return of resident parents recursion"); 
		
		// Extract input nodes
		for (GenerativeInputNode inputNode : currentNode.getResident().getInputNodeFatherList()) {
			DomainResidentNode residentNode = (DomainResidentNode)inputNode.getResidentNodePointer().getResidentNode(); 

			                /*
			                 * Avaliar se o nó de input é o indicativo de recursividade...
			                 * O processo da recursão deverá ser iniciado aqui. Versão inicial sem
			                 * levar em consideração como será definida a recursividade na GUI
			                 * (Facilmente adaptável). 
			                 */
			
			if(currentNode.getResident() == residentNode){
				
				//Valuate if the node is recursive. 
				
				
				//Call recursion
				OrdinaryVariable ovOrdereable = null; 
				ObjectEntity objectEntity = null; 
				for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
					ObjectEntity oe = currentNode.getResident().getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getObjectEntityByType(ov.getValueType()); 
					if(oe.isOrdereable()){
						objectEntity = oe; 
						ovOrdereable = ov; 
						break; 
					}
				}
				
				if(objectEntity == null){
					//TODO exception
				}
				
				OVInstance ovInstanceOrdereable = null; 
	            for(OVInstance ovInstance: currentNode.getArguments()){
	            	if(ovInstance.getOv() == ovOrdereable){
	            		ovInstanceOrdereable = ovInstance; 
	            		break; 
	            	}
	            }
	            
				if(ovInstanceOrdereable == null){
					//TODO exception
				}
				
				String nameEntity = ovInstanceOrdereable.getEntity().getInstanceName(); 
				
				ObjectEntityInstanceOrdereable objectEntityInstance = (ObjectEntityInstanceOrdereable)objectEntity.getInstanceByName(nameEntity);
				
				if(objectEntityInstance == null){
					//TODO exception
					break; 
				}
				
				ObjectEntityInstanceOrdereable prev = objectEntityInstance.getPrev(); 
				
				if(prev == null){
					//TODO nada a se fazer... retornar (final da recursão!!!!). 
				}else{

					OVInstance newOvInstance = OVInstance.getInstance(ovOrdereable, prev.getName(), ovOrdereable.getValueType());
					
					//Criar o novo SSBNNode com os novos valores setados. 
					SSBNNode ssbnnode = SSBNNode.getInstance(net, residentNode, new ProbabilisticNode(), false);
					
	            	for(OVInstance instance: currentNode.getArguments()){
	            		if(instance != ovInstanceOrdereable){
	            			ssbnnode.addArgument(instance); 
	            		}else{
	            			ssbnnode.addArgument(newOvInstance);
	            		}
	            	}

	            	this.generateRecursive(ssbnnode, seen, net);	// algorithm's core
	            	
	    			if(!ssbnnode.isFinding()){
	    				currentNode.addParent(ssbnnode, true);
	    				          /* Aqui está algo a se analizar: quando um nó
	    				           * for um finding ele será acrescentado como pai 
	    				           * na rede ou não (pelo menos o SSBNNode, sem
	    				           * levar em consideração o nó probabilistico). 
	    				           * Esta versão inicial não considera isto. 
	    				           */
	    				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
	    			}
					
				}
				
				break; 
				
			}
			
			//Step 1: evaluate context and search for findings
            List<OVInstance> listOVInstances = new ArrayList<OVInstance>(); 
            listOVInstances.addAll(currentNode.getArguments()); 
            Debug.println(currentNode.getName() + " Avaliar input " + residentNode.getName()); 
			
            List<OrdinaryVariable> ovProblemList = avaliateOVInstancesInformation(inputNode.getOrdinaryVariableList(), currentNode.getArguments()); 
            Debug.println("OV Problem List: "); 
            for(OrdinaryVariable ov: ovProblemList){
            	Debug.println(ov.toString()); 
            }
            
    		if(!ovProblemList.isEmpty()){
    			List<SSBNNode> parentList = createSSBNNodesForEntitiesSearchForInputNodes(inputNode.getMFrag(), currentNode, inputNode, ovProblemList, listOVInstances); 
    		    for(SSBNNode ssbnnode: parentList){
    		    	Debug.println("Node Created: " + ssbnnode.toString()); 
    		    	parents.add(ssbnnode);
        			generateRecursive(ssbnnode, seen, net);	// algorithm's core
        			if(!ssbnnode.isFinding()){
        				currentNode.addParent(ssbnnode, true);
        				          /* Aqui está algo a se analizar: quando um nó
        				           * for um finding ele será acrescentado como pai 
        				           * na rede ou não (pelo menos o SSBNNode, sem
        				           * levar em consideração o nó probabilistico). 
        				           * Esta versão inicial não considera isto. 
        				           */
        				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
        			}
    		    }
    		}else{
    			boolean contextNodesOK = evaluateSimpleContextNodes(inputNode.getMFrag(), listOVInstances, ovProblemList); 
    	           if(contextNodesOK){
    	        	    SSBNNode ssbnnode = SSBNNode.getInstance(net, residentNode, new ProbabilisticNode(), false);
    					
    	            	for(OVInstance instance: currentNode.getArguments()){
    	            		OrdinaryVariable ov = instance.getOv(); 
    	            		int index = inputNode.getResidentNodePointer().getOrdinaryVariableIndex(ov);
    	            		if(index > -1){
    	            			OrdinaryVariable destination = residentNode.getOrdinaryVariableList().get(index);
    	            			ssbnnode.addArgument(destination, instance.getEntity().getInstanceName());
    	            		}
    	            	}

    	            	parents.add(ssbnnode);
    	            	this.generateRecursive(ssbnnode, seen, net);	// algorithm's core
    	            	
    	    			if(!ssbnnode.isFinding()){
    	    				currentNode.addParent(ssbnnode, true);
    	    				          /* Aqui está algo a se analizar: quando um nó
    	    				           * for um finding ele será acrescentado como pai 
    	    				           * na rede ou não (pelo menos o SSBNNode, sem
    	    				           * levar em consideração o nó probabilistico). 
    	    				           * Esta versão inicial não considera isto. 
    	    				           */
    	    				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
    	    			}
    	            }
    		}
		}
		
		Debug.println(currentNode.getResident().getName() + " return of input parents recursion"); 
		
		// TODO finish this method

		return currentNode;
	}

	private void printParents(SSBNNode node, int nivel){
		for(SSBNNode parent: node.getParents()){
			for(int i = 0; i < nivel; i++){
				System.out.print("   "); 
			}
			System.out.println(parent.toString());
			printParents(parent, nivel + 1); 
		}
	}

	/**
	 * This is how many times a recursive call to the algorithm should be done
	 * @return the recursiveCallLimit
	 */
	public long getRecursiveCallLimit() {
		return RECURSIVE_CALL_LIMIT;
	}

	/**
	 * This is how many times a recursive call to the algorithm should be done
	 * @param recursiveCallLimit the recursiveCallLimit to set
	 */
//	public void setRecursiveCallLimit(long recursiveCallLimit) {
//		this.RECURSIVE_CALL_LIMIT = recursiveCallLimit;
//	}
	
	
}