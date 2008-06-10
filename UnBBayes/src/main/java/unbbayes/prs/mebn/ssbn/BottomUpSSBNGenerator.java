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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;

/**
 * Algorithm for generating the Situation Specific Bayesian Network (SSBN).  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com) 
 * @author Shou Matsumoto
 */
public class BottomUpSSBNGenerator extends AbstractSSBNGenerator {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private long recursiveCallLimit = 999999999999999999L;
	
	private long recursiveCallCount = 0;
	

	public static long stepCount = 0L;
	public static String queryName = "";
	

	/**
	 * 
	 */
	public BottomUpSSBNGenerator() {
		super();  
	}
	

	
	/*-------------------------------------------------------------------------
	 * Public
	 *------------------------------------------------------------------------/
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public SituationSpecificBayesianNetwork generateSSBN(Query query) throws SSBNNodeGeneralException, 
	                                                             ImplementationRestrictionException, MEBNException {
		
		ssbnNodeList = new SSBNNodeList(); 
		
		// As the query starts, let's clear the flags used by the previous query
		query.getMebn().clearMFragsIsUsingDefaultCPTFlag();
		setKnowledgeBase(query.getKb()); 
		
		// some data extraction
		SSBNNode queryNode = query.getQueryNode();

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
		
		try {
			logManager.writeToDisk("LogSSBN.log", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return new SituationSpecificBayesianNetwork(queryNode.getProbabilisticNetwork(), new ArrayList(), new ArrayList());
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
		
		// The cicles are avaliated in the ssbn final
//		if (seen.contains(currentNode)) {
//			//TODO evaluate cycles
//		}
		
		//------------------------- STEP 1: search findings -------------------
		

		logManager.appendln(currentNode + ":A - Search findings");
        //check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = this.getKnowledgeBase().searchFinding(currentNode.getResident(), currentNode.getArguments()); 
		
		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			seen.add(currentNode); 
			logManager.appendln("Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 
			
	        generateCPT(currentNode); //this method don't do anything for findings...
	        
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
        
		boolean evaluateRelatedContextNodesResult = false;
		
		try {
			evaluateRelatedContextNodesResult = evaluateRelatedContextNodes(currentNode.getResident(), ovInstancesList);
		} catch (OVInstanceFaultException e) {
			//pre-requisite.
			e.printStackTrace();
		}
		
		if(!evaluateRelatedContextNodesResult){
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
            List<OrdinaryVariable> ovProblematicList = analyzeOVInstancesListCompletudeForOVList(
            		residentNode.getOrdinaryVariableList(), currentNode.getArguments()); 
            
    		if(!ovProblematicList.isEmpty()){
    			List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
    					residentNode.getMFrag(), currentNode, residentNode, 
    					ovProblematicList, ovInstancesList, true); 
    			
    		    for(SSBNNode ssbnnode: createdNodes){
    		    	generateRecursive(ssbnnode, seen, net);	
    		    	
    		    	if(!ssbnnode.isFinding()){
    		    		currentNode.addParent(ssbnnode, true);
    		    	}else{
    		    		currentNode.addParent(ssbnnode, false);
    		    	}	
    		    }
    		}else{ //ovProblematicList.isEmpty
    			SSBNNode ssbnnode = null; 
    			
				List<OVInstance> arguments = fillArguments(currentNode.getArguments(), residentNode);
				SSBNNode testSSBNNodeDuplicated = getSSBNNodeIfItAlreadyExists(
						residentNode, arguments, ssbnNodeList); 
				
				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
				if(testSSBNNodeDuplicated == null){
					ssbnnode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode(), false);
					for(OVInstance ovInstance: arguments){
						ssbnnode.addArgument(ovInstance);
					}
					ssbnNodeList.add(ssbnnode);
	    			generateRecursive(ssbnnode, seen, net);	
				}else{
					ssbnnode = testSSBNNodeDuplicated;
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
			
			ResidentNode residentNodeTargetOfInput = inputNode.getResidentNodePointer().getResidentNode(); 

			logManager.appendln(currentNode.getName() + "Evaluate input " + residentNodeTargetOfInput.getName()); 
			
			
			
			//Step 0: ----------Evaluate recursion... Node1(x2) -> Node1(x1)------------
			if(currentNode.getResident() == residentNodeTargetOfInput){
				
				SSBNNodeJacket previousNode = getPreviousNode(currentNode, seen, net, residentNodeTargetOfInput, inputNode);

				if(previousNode != null){
					
					//yes... exist recursion...
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
            List<OrdinaryVariable> ovProblematicList = 
            	analyzeOVInstancesListCompletudeForOVList(inputNode.getOrdinaryVariableList(), 
            			currentNode.getArguments()); 
            
    		if(!ovProblematicList.isEmpty()){
    			
    			List<SSBNNodeJacket> parentList = createSSBNNodesOfEntitiesSearchForInputNode(
    					currentNode, inputNode, ovProblematicList, currentNode.getArgumentsAsList()); 
    			
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
    		   
    		}else{ //ovProblematicList.isEmpty()
    			

				SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNodeTargetOfInput, new ProbabilisticNode(), false);
				SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode); 
				
				for(OVInstance ovInstance: currentNode.getArguments()){
					addArgumentToSSBNNodeOfInputNode(inputNode, ssbnNodeJacket, ovInstance);
				}
				
				ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
				
				ssbnNodeJacket.setSsbnNode(checkForDoubleSSBNNode(ssbnNode));
				
				ssbnNodeList.add(ssbnNode);
    			
    			boolean contextNodesOK = false;
				
    			try {
					contextNodesOK = evaluateRelatedContextNodes(inputNode, currentNode.getArgumentsAsList());
				} catch (OVInstanceFaultException e) {
					//ovProblemList is empty... this exception never will be catch.
					e.printStackTrace();
				} 
				
    			if(contextNodesOK){
        			//Step 2: context and findngs ok... do the work...	
    				this.generateRecursive(ssbnNode, seen, net);	// algorithm's core	
    			}else{
    				logManager.append("Context Nodes False for " + ssbnNode + " (input node)"); 
    				ssbnNode.setUsingDefaultCPT(true); 
    			}
    			
				ssbnNodeJacket.setArgumentsOfInputMFrag(); 
				
				if(!ssbnNode.isFinding()){
					currentNode.addParent(ssbnNode, true);
				}else{
					currentNode.addParent(ssbnNode, false);
				}
    		}
		}

		logManager.appendln(currentNode + "E:- generate CPT");
		
		if(currentNode.getContextFatherSSBNNode()!=null){ 
			try {
				generateCPTForNodeWithContextFather(currentNode); 
			} catch (InvalidOperationException e1) {
				e1.printStackTrace();
				throw new SSBNNodeGeneralException(e1.getMessage()); 
			}
		}else{
		    generateCPT(currentNode);
		}
		
		logManager.appendln(currentNode + " return of input parents recursion"); 
		
		return currentNode;
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
			
			ssbnNode.getCompiler().generateCPT(ssbnNode);
			
			logManager.appendln("CPT OK\n");
		
	}

	
}