/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.io.LogManager;
import unbbayes.io.XMLIO;
import unbbayes.prs.Edge;
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
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * Algorithm for generating the Situation Specific Bayesian Network (SSBN).  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com) 
 * @author Shou Matsumoto
 */
public class BottomUpSSBNGenerator implements ISSBNGenerator {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private KnowledgeBase kb = null;
	
	private long recursiveCallLimit = 999999999999999999L;
	
	private long recursiveCallCount = 0;
	
	//all the ssbn nodes created. 
	private List<SSBNNode> ssbnNodeList;

	public static long stepCount = 0L;
	public static String queryName = "";
	public static LogManager logManager = new LogManager();
	
	

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
	                                                             ImplementationRestrictionException, MEBNException {
		ssbnNodeList = new ArrayList<SSBNNode>(); 
		
		// As the query starts, let's clear the flags used by the previous query
		query.getMebn().clearMFragsIsUsingDefaultCPTFlag();
		
		// some data extraction
		SSBNNode queryNode = query.getQueryNode();
		this.kb = query.getKb();

		// Log manager initialization
		stepCount = 0L;
		queryName = queryNode.toString();
		logManager.clear();
		
		// initialization

		// call recursive
		this.recursiveCallCount = 0;
		SSBNNode root = this.generateRecursive(queryNode, new SSBNNodeList(), 
				                               queryNode.getProbabilisticNetwork());
		
		logManager.appendln("\n");
		logManager.appendln("SSBN generation finished");
		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(root);
		
		return queryNode.getProbabilisticNetwork();
	}
	
	public static void printAndSaveCurrentNetwork(SSBNNode queryNode) {
		stepCount++;
		//Debug. 
		PositionAdjustmentUtils utils = new PositionAdjustmentUtils(); 
		utils.adjustPositionProbabilisticNetwork(queryNode.getProbabilisticNetwork()); 
		
		BottomUpSSBNGenerator.printNetworkInformation(queryNode); //only Debug informations
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
	 *     nodes fathers evaluated. 
	 * 
	 * @param currentNode node currently analyzed. 
	 * @param seen all the nodes analyzed previously (doesn't contain the current node)
	 * @param net the ProbabilisticNetwork 
	 * @return The currentNode with its auxiliary structures of the analysis. 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net) throws SSBNNodeGeneralException, ImplementationRestrictionException, MEBNException {
		
		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}
		this.recursiveCallCount++;
		
		logManager.appendln("\n-------------Step: " + currentNode.getName() + "--------------\n"); 
		
		// check for cycle
		if (seen.contains(currentNode)) {
//			return null; 
			//TODO Criar método para avaliar ciclos
		}
		
		//------------------------- STEP 1: search findings -------------------
		

		logManager.appendln(currentNode + ":A - Search findings");
        //check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = kb.searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			seen.add(currentNode); 
			logManager.appendln("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			
	        generateCPT(currentNode);
	        
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		
		//------------------------- STEP 2: analyze context nodes. -------------
		
		// evaluates querynode's mfrag's context nodes 
		//(if not OK, sets MFrag's flag to use default CPT)	
		
		logManager.appendln(currentNode + ":B - Analyse context nodes");
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
            List<OrdinaryVariable> ovProblemList = analyzeOVInstancesListCompletudeForOVList(
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
				
				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
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
		
		logManager.appendln(currentNode + " returned from the resident node parents' recursion"); 
		
		
		
		
		
	    //------------------------- STEP 4: Add and evaluate input nodes fathers -------------

		logManager.appendln(currentNode + "D:- Analyze input nodes fathers");
		for (InputNode inputNode : currentNode.getResident().getInputNodeFatherList()) {
			
			ResidentNode residentNode = 
				(ResidentNode)inputNode.getResidentNodePointer().getResidentNode(); 

			logManager.appendln(currentNode.getName() + "Evaluate input " + residentNode.getName()); 
			
			//Evaluate recursion... 
			if(currentNode.getResident() == residentNode){
				
				SSBNNodeJacket previousNode = getPreviousNode(currentNode, seen, net, residentNode, inputNode);

				if(previousNode != null){
					
					previousNode.setArgumentsOfResidentMFrag();
					generateRecursive(previousNode.getSsbnNode(), seen, net);
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
            List<OrdinaryVariable> ovProblemList = 
            	analyzeOVInstancesListCompletudeForOVList(inputNode.getOrdinaryVariableList(), 
            			currentNode.getArguments()); 
            
    		if(!ovProblemList.isEmpty()){
    			
    			List<SSBNNodeJacket> parentList = createSSBNNodesOfEntitiesSearchForInputNode(
    					currentNode, inputNode, ovProblemList, currentNode.getArgumentsAsList()); 
    			
    		    for(SSBNNodeJacket ssbnNodeJacket: parentList){
    		    	
    		    	SSBNNode ssbnnode = ssbnNodeJacket.getSsbnNode();
    		    	ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
    		    	
    		    	logManager.appendln("Node Created: " + ssbnnode.toString());
    		    	generateRecursive(ssbnnode, seen, net);	// algorithm's core
    		    	
    		    	ssbnNodeJacket.setArgumentsOfInputMFrag(); 
        			if(!ssbnnode.isFinding()){
        				currentNode.addParent(ssbnnode, true);
        			}else{
        				currentNode.addParent(ssbnnode, false);
        			}
        			
    		    }
    		    
    		    // TODO Remove the code below, if it is not really necessary
    			//PotentialTable pt = currentNode.getContextFatherSSBNNode().getProbNode().getPotentialTable(); 
//    			currentNode.addContextFatherSSBNNode();
    		    
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
    				
//    				ssbnNode = checkForDoubleSSBNNodeForInputEvaliation(ssbnNode); //TODO analyze... 
    				
    				ssbnNodeList.add(ssbnNode); //TODO cuidado para não adicionar elementos repetidos na lista... 
    				this.generateRecursive(ssbnNode, seen, net);	// algorithm's core
    				
    				ssbnNodeJacket.setArgumentsOfInputMFrag(); 
    				
    				if(!ssbnNode.isFinding()){
    					currentNode.addParent(ssbnNode, true);
    				}else{
    					currentNode.addParent(ssbnNode, false);
    				}
    			}else{
    				//TODO What to do?
    			}
    		}
		}

		logManager.appendln(currentNode + "E:- generate CPT");
		
		if(currentNode.getContextFatherSSBNNode()!=null){ 
			try {
				generateCPTForNodeWithContextFather(currentNode); 
			} catch (InvalidOperationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SSBNNodeGeneralException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MEBNException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
		    generateCPT(currentNode);
		}
		
		logManager.appendln(currentNode + " return of input parents recursion"); 
		
		return currentNode;
	}

	/**
	 *
	 */
	private List<OVInstance> fillArguments(Collection<OVInstance> ovInstanceList, ResidentNode node) {
	
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
			
			logManager.appendln("Build Previous Node End");
			return ssbnNodeJacket;
		}
	}
	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	private boolean evaluateRelatedContextNodes (ResidentNode residentNode, 
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
	 * @throws OVInstanceFaultException 
	 */
	private boolean evaluateRelatedContextNodes(InputNode inputNode, 
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
			logManager.appendln("Context Node: " + context.getLabel()); 
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				inputNode.getMFrag().setAsUsingDefaultCPT(true); 
				logManager.appendln("Result = FALSE. Use default distribution ");
				return true;  
			}else{
				logManager.appendln("Result = TRUE.");
			}
			
			logManager.appendln(""); 
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
	private List<SSBNNode> createSSBNNodesOfEntitiesSearchForResidentNode(MFrag mFrag, SSBNNode originNode, 
			ResidentNode fatherNode, List<OrdinaryVariable> ovList, List<OVInstance> ovInstances) 
			throws ImplementationRestrictionException, SSBNNodeGeneralException {
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		//Complex case: evaluate search context nodes. 
		logManager.appendln("Have ord. variables incomplete!"); 
		
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
		
		logManager.appendln("Context Node: " + context.getLabel()); 
		
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
						throw new ImplementationRestrictionException(resource.getString("MoreThanOneContextNodeFather"));
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

					originNode.setContextFatherSSBNNode(contextFatherSSBNNode);
					
					// TODO "translate" this commentary!!! 
					//in this implementation only this is necessary, because the treat
					//of context nodes how fathers will be """trivial""", using the XOR 
					//strategy. For a future implementation that accept different 
					//distributions for the residentNode of the ContextNode, the
					//arguments of the resident node will have to be filled with the OVInstances
					//for the analized of the resident node formula. (very complex!).  
					
					//Search for all the entities present in kb. 
					result = kb.getEntityByType(ovProblematic.getValueType().getName());
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
			throw new ImplementationRestrictionException("O nó de contexto de busca pode ter apenas uma variavel livre!");
		}
	}

	private SSBNNode createSSBNNodeForEntitySearch(ProbabilisticNetwork probabilisticNetwork, 
			ResidentNode residentNode, List<OVInstance> ovInstances, OrdinaryVariable ov, String entity) {
		
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
			InputNode fatherNode, List<OrdinaryVariable> ovProblemList, List<OVInstance> ovInstances) 
			throws SSBNNodeGeneralException, ImplementationRestrictionException {
		
		MFrag mFrag = fatherNode.getMFrag(); 
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
		//Complex case: evaluate search context nodes. 
		logManager.appendln("Have ord. variables incomplete!"); 
		
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
						throw new ImplementationRestrictionException("Um nó não pode ter dois nós de contexto pais!");
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
					result = kb.getEntityByType(ov.getValueType().getName());
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

	/* 
	 * version for input nodes. 
	 */
	private SSBNNodeJacket createSSBNNodeForEntitySearch(SSBNNode originNode, 
			InputNode fatherNode, OrdinaryVariable ov, String entityName) 
	        throws SSBNNodeGeneralException {
		
		SSBNNode ssbnNode = SSBNNode.getInstance(originNode.getProbabilisticNetwork(),
				(ResidentNode)fatherNode.getResidentNodePointer().getResidentNode(), new ProbabilisticNode(), false); 
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
		
		logManager.appendln(" ");
		logManager.appendln("SSBNNode created:" + ssbnNode.getName());
		logManager.appendln("Input MFrag Arguments: " + ssbnNodeJacket.getOvInstancesOfInputMFrag());
		logManager.appendln("Resident MFrag Arguments: " + ssbnNodeJacket.getOvInstancesOfResidentMFrag());
		logManager.appendln(" ");
		
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
	private void addArgumentToSSBNNodeOfInputNode(InputNode inputNode, 
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
	 * Verifies if it has OVInstances for all ordinary variable. If it has, return 
	 * null, otherwise, return the list of ordinary variables that does not have an 
	 * OVInstance. 
	 * 
	 * @param ordVariableList
	 * @param ovInstanceList
	 * @return List of ordinary variables that does not have an OVInstance. 
	 */
	private List<OrdinaryVariable> analyzeOVInstancesListCompletudeForOVList(
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
	private SSBNNode getSSBNNode(ResidentNode residentNode, Collection<OVInstance> ovInstanceList){
		
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
	private void generateCPT(SSBNNode ssbnNode) throws MEBNException {
			
			logManager.appendln("\nGenerate table for node " + ssbnNode);
			logManager.appendln("Parents:");
			for(SSBNNode parent: ssbnNode.getParents()){
				logManager.appendln("  " + parent);
			}
			
			Debug.setDebug(false);
			
			ssbnNode.getCompiler().generateCPT(ssbnNode);
			
			Debug.setDebug(true);
			logManager.appendln("CPT OK\n");
		
	}
	
	private void generateCPTForNodeWithContextFather(SSBNNode ssbnNode) 
	      throws SSBNNodeGeneralException, MEBNException, InvalidOperationException {
		

		    GUIPotentialTable gpt; 
		
			logManager.appendln("\nGenerate table for node (with context father): " + ssbnNode);
			logManager.appendln("Parents:");
			for(SSBNNode parent: ssbnNode.getParents()){
				logManager.appendln("  " + parent);
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

			ssbnNode.getContextFatherSSBNNode().generateCPT();
			
			gpt = new GUIPotentialTable(ssbnNode.getContextFatherSSBNNode().getProbNode().getPotentialTable()); 
			gpt.showTable("Table for Node " + ssbnNode.getContextFatherSSBNNode());
			
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
				
				SSBNNode tempNode = SSBNNode.getInstance(ssbnNode.getResident()); 
				for(SSBNNode parent: groupParents){
					tempNode.addParent(parent, false); 
				}
				PotentialTable cpt = tempNode.getCompiler().generateCPT(tempNode); 
				sizeCPTOfEntity = cpt.tableSize();
				
				gpt = new GUIPotentialTable(cpt); 
				gpt.showTable("Table for Node " + ssbnNode + " - " + groupParents);
				
				mapCPTByEntity.put(entity.getInstanceName(), cpt);
				System.out.println("Tabela armazenada: " + entity.getInstanceName() + " " + cpt.tableSize());
			}			
			
			//Reorganize the variables in table
			int variablesSize = cptResidentNode.getVariablesSize(); 
			int indexContext = cptResidentNode.getVariableIndex(ssbnNode.getContextFatherSSBNNode().getProbNode()); 
			cptResidentNode.moveVariableWithoutMoveData(indexContext, variablesSize - 1); 
			
			//Step 3: Fazer o XOR das tabelas obtidas utilizando a tabela do nó de contexto
			
			System.out.println("Gerando tabela para o nó residente");

			int columnsByEntity = cptResidentNode.tableSize() / ssbnNode.getResident().getPossibleValueListIncludingEntityInstances().size();
			columnsByEntity /= contextFather.getProbNode().getStatesSize(); 
			
			System.out.println("Colunas por entidade= " + columnsByEntity);

			int rows = ssbnNode.getProbNode().getStatesSize(); 
			
			for(int i=0; i < contextFather.getProbNode().getStatesSize(); i++){
			    
				System.out.println("\n i = " + i);
				
				String entity = contextFather.getProbNode().getStateAt(i);
				System.out.println("Entity = " + entity);
				PotentialTable cptEntity = mapCPTByEntity.get(entity); 
				
				//descobrir a posição inicial...
				List<SSBNNode> parentsByEntity = mapParentsByEntity.get(entity); 
				ProbabilisticNode pnEntity = parentsByEntity.get(0).getProbNode(); 
			
				int indexEntityInCptResidentNode = cptResidentNode.getVariableIndex(pnEntity) - 1; //the index 0 is the node itself
				int entityIndex = (indexEntityInCptResidentNode + parentsByEntity.size() - 1)/ parentsByEntity.size(); 
				
				System.out.println("Entity Index=" + entityIndex);
				
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
				
				System.out.println("Index = " + indexEntityInCptResidentNode);
				System.out.println("Repetições = " + repColum);
				System.out.println("Posição na tabela do residente = " + positionTableResident);
				System.out.println("Posição na tabela da entidade = " + positionTableEntity);
				System.out.println("Linhas = " + rows);
				System.out.println("Repitições de tudo = " + repAll);
				System.out.println("Em Ordem = " + inOrder);
				
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
								System.out.println();
							}
							positionTableEntityInitial += rows; 
						}
						
						positionTableEntityFinal = positionTableEntityInitial;
						
					}
					
					positionTableEntity = positionTableEntityFinal; 
				}
			}

			gpt = new GUIPotentialTable(ssbnNode.getProbNode().getPotentialTable()); 
			gpt.showTable("Table for Node " + ssbnNode);
			
			Debug.setDebug(true);
			logManager.appendln("CPT OK\n");
		
	}
	
	
	
	
	
	/*-------------------------------------------------------------------------
	 * Debug Methods 
	 *------------------------------------------------------------------------/
	
	/*
	 * debug method. 
	 */
	public static void printParents(SSBNNode node, int nivel){
		for(SSBNNode parent: node.getParents()){
			for(int i = 0; i <= nivel; i++){
				if (i == 0) {
					logManager.append("  |   ");
				} else {
					logManager.append("   ");
				}
			}
			logManager.appendln(parent.toString());
			printParents(parent, nivel + 1); 
		}
	}


	/*
	 * This debug method print the informations about the network build by
	 * the SSBN algorithm and save a file xmlbif with the bayesian network built. 
	 * 
	 * @param querynode
	 */
	public static void printNetworkInformation(SSBNNode queryNode) {
		//TODO Use a decimal format instead
		String stepCountFormated = "";
		if (stepCount < 10L) {
			stepCountFormated = "00" + stepCount;
		} else if (stepCount < 100L) {
			stepCountFormated = "0" + stepCount;
		} else {
			stepCountFormated = "" + stepCount;
		}
		String netName = queryName + " - Step " + stepCountFormated;
		queryNode.getProbabilisticNetwork().setName(netName);
		File file = new File("examples" + File.separator + "MEBN" + File.separator + "SSBN" + File.separator + netName  + ".xml");
		
		logManager.appendln("\n"); 
		logManager.appendln("  |-------------------------------------------------------");
		logManager.appendln("  |Network: ");
		logManager.appendln("  |" + netName);
		logManager.appendln("  | (" + file.getAbsolutePath() + ")");
		
		logManager.appendln("  |\n  |Current node's branch: ");
		logManager.appendln("  |" + queryNode.getName());
		printParents(queryNode, 0); 
		
		logManager.appendln("  |\n  |Edges:");
		for(Edge edge: queryNode.getProbabilisticNetwork().getEdges()){
			logManager.appendln("  |" + edge.toString());
		}
		
		logManager.appendln("  |\n  |Nodes:");
		for(int i = 0; i < queryNode.getProbabilisticNetwork().getNodes().size(); i++){
			logManager.appendln("  |" + queryNode.getProbabilisticNetwork().getNodeAt(i).toString());
		}
		logManager.appendln("  |-------------------------------------------------------");
		logManager.appendln("\n"); 
		
	    XMLIO netIO = new XMLIO(); 
		
		try {
			netIO.save(file, queryNode.getProbabilisticNetwork());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		try {
			logManager.writeToDisk("teste.txt", false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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