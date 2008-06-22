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
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode.EvaluationSSBNNodeState;
import unbbayes.prs.mebn.ssbn.exception.ImplementationError;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.util.PositionAdjustmentUtils;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.util.Debug;

public class ExplosiveSSBNGenerator extends AbstractSSBNGenerator  {

	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");

	private long recursiveCallLimit = 100L;

	private long recursiveCallCount = 0;

	public static long stepCount = 0L;
	public static String queryName = "";

	private List<SSBNNode> findingList; 
	
	public ExplosiveSSBNGenerator(){
		super();  
		
		findingList = new ArrayList<SSBNNode>(); 
	}

	/**
	 * Avaliação dos nós de contexto
	 * 
	 * 1) Verificar se todas as variaveis ordinarias estao OK. Se tiver avaliar
	 * 2) Caso não estejam, fazer busca. 
	 * 3) Caso a busca não retorne nada, tentar o caso do XOR. 
	 */
	
	/**
	 * The SSBN Node is generate in a process with tree partes:
	 * 1) Building of the network
	 * 2) Generate CPT's for the nodes of the created network
	 * 3) Set the finding nodes and propagate the evidences
	 */

	public SituationSpecificBayesianNetwork generateSSBN(Query query)
	           throws SSBNNodeGeneralException, ImplementationRestrictionException, 
	                  MEBNException {

		Debug.setDebug(true); 

		ssbnNodeList = new SSBNNodeList(); 
		ssbnNodesMap = new TreeMap<String, SSBNNode>(); 
		warningList = new ArrayList<SSBNWarning>(); 

		// THE PREPARATION
		SSBNNode queryNode = query.getQueryNode();
		this.setKnowledgeBase(query.getKb());
		stepCount = 0L;
		queryName = queryNode.getUniqueName();
		logManager.clear();
		this.recursiveCallCount = 0;

		List<Query> queryList = new ArrayList<Query>(); 
		queryList.add(query); 
		
		// THE PROCESS
		queryNode.setPermanent(true); 
		ssbnNodesMap.put(queryNode.getUniqueName(), queryNode); 
		
		this.generateRecursive(queryNode, ssbnNodeList, 
				queryNode.getProbabilisticNetwork());

		this.generateCPTForAllSSBNNodes(queryNode); 
		
		this.removeNotPermanentNodes(ssbnNodeList); 

		//THE END
		logManager.appendln("\n");
		logManager.appendln("SSBN generation finished");

		printAndSaveCurrentNetwork(queryNode);

		try {
			logManager.writeToDisk("LogSSBN.log", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		SituationSpecificBayesianNetwork ssbn = new SituationSpecificBayesianNetwork(
				queryNode.getProbabilisticNetwork(), findingList, queryList); 
		
		ssbn.setWarningList(this.warningList); 
		
		return ssbn;
	}


	private void generateRecursive(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net) throws SSBNNodeGeneralException, 
			                                 ImplementationRestrictionException, 
			                                 MEBNException {

		//generate below

		if(currentNode.getEvaluationState()== EvaluationSSBNNodeState.EVALUATED_COMPLETE){
			return; 
		}

		if(currentNode.getEvaluationState() == EvaluationSSBNNodeState.NOT_EVALUATED){
			
			if(currentNode.getEvaluationState() != EvaluationSSBNNodeState.EVALUATING_BELOW){
				currentNode.setEvaluationState(EvaluationSSBNNodeState.EVALUATING_BELOW); 
				logManager.appendln("\nGENERATE RECURSIVE DOWN: " + currentNode.getName() ); 
				generateRecursiveDown(currentNode, seen, net); 
				currentNode.setEvaluationState(EvaluationSSBNNodeState.EVALUATED_BELOW); 
			}
			
		}

		//verify if OK for generate up
		if(currentNode.isPermanent()){
			
			if(currentNode.getEvaluationState() != EvaluationSSBNNodeState.EVALUATING_UP){
				currentNode.setEvaluationState(EvaluationSSBNNodeState.EVALUATING_UP); 
				
				// Step where the node is created for the first time
				logManager.appendln("\n");
				logManager.appendln("Node " + currentNode + " created");
				printAndSaveCurrentNetwork(currentNode);
				
				logManager.appendln("\nGENERATE RECURSIVE UP: " + currentNode.getName() ); 
				generateRecursiveUp(currentNode, seen, net, null);
				currentNode.setEvaluationState(EvaluationSSBNNodeState.EVALUATED_COMPLETE); 
			}
			
		}

	}

	/*-------------------------------------------------------------------------
	 * Private
	 *------------------------------------------------------------------------*/

	/**
	 * Generate recursive method for the parents of a node. 
	 * All the nodes created will be marked how permanent. 
	 * 
	 * - If the node has a finding, create the SSBNNode, set it how finding and retorne it
	 * 
	 * - Tratamento especial para os nós de findings, pois para estes, precisamos
	 * apenas da primeira geração de pais... (não precisamos rodar o algoritmo 
	 * recursivamente nos pais). 
	 * 
	 * Pre-requisites: 
	 * - The node current node has a OVInstance for all OrdinaryVariable argument
	 * 
	 */
	private SSBNNode generateRecursiveUp(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net, MFragInstance mFragInstance) throws SSBNNodeGeneralException, 
			ImplementationRestrictionException, 
			MEBNException {

		logManager.appendln("\n\n[U] Recursive call count = " + recursiveCallCount); 
		logManager.appendln("[U]" + currentNode + ":-------------EVALUATING NODE: " + currentNode.getName() + "--------------\n"); 

		currentNode.setPermanent(true); //up always permanent...

		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}

		this.recursiveCallCount++;

	
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 

		//------------------------- STEP 1: Add and evaluate resident nodes fathers -------------

		logManager.appendln("[U]" + currentNode +  "A:- Analyse resident nodes fathers");
		for (ResidentNode residentNode : currentNode.getResident().getResidentNodeFatherList()) {

			/*
			 * Analyze if it has one ov instance for each ordinary variable. If this 
			 * is not true, verify if it has some context node able to recover 
			 * the instances that match with the ordinary variable.  
			 */
			List<OrdinaryVariable> ovProblematicList = getOVInstancesForWhichNotExistOV(
					residentNode.getOrdinaryVariableList(), currentNode.getArguments()); 

			if(!ovProblematicList.isEmpty()){
				List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
						residentNode.getMFrag(), currentNode, residentNode, 
						ovProblematicList, ovInstancesList, true); 

				for(SSBNNode ssbnnode: createdNodes){
					
					if(!currentNode.getParents().contains(ssbnnode)){
						
						if(!currentNode.isFinding()){ 
							generateRecursive(ssbnnode, seen, net);	
							ssbnnode.setPermanent(true);
						}
						
						currentNode.addParent(ssbnnode, true);
					}
				}
			}else{ //ovProblematicList.isEmpty

				List<OVInstance> arguments = takeNecessaryArgumentsForNode(
						currentNode.getArguments(), residentNode);

				SSBNNode ssbnNode = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(residentNode, arguments)); 
				
				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
				if(ssbnNode == null){
					ssbnNode = SSBNNode.getInstance(net,residentNode, new ProbabilisticNode());
					for(OVInstance ovInstance: arguments){
						ssbnNode.addArgument(ovInstance);
					}
					ssbnNodeList.add(ssbnNode);
					ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 
					if(!currentNode.isFinding()){
						generateRecursive(ssbnNode, seen, net);
						ssbnNode.setPermanent(true); 	
					}
				}else{
					logManager.appendln("Node already exists:" + ssbnNode); 
					if(!currentNode.isFinding()){
						generateRecursive(ssbnNode, seen, net);
						ssbnNode.setPermanent(true);
						//TODO method for set the new permanent nodes with this sett.
					}
				}
				
				if(!currentNode.getParents().contains(ssbnNode)){
					currentNode.addParent(ssbnNode, true);
				}
			}

		}

		logManager.appendln(currentNode + " returned from the resident node parents' recursion"); 






		//------------------------- STEP 2: Add and evaluate input nodes fathers -------------

		logManager.appendln(currentNode + "B:- Analyze input nodes fathers");
		for (InputNode inputNode : currentNode.getResident().getInputNodeFatherList()) {

			ResidentNode residentNodeTargetOfInput = inputNode.getResidentNodePointer().getResidentNode(); 

			logManager.appendln(currentNode.getName() + "Evaluate input " + residentNodeTargetOfInput.getName()); 


			//Step 0: ----------Evaluate recursion... Node1(x2) -> Node1(x1)------------

			if(currentNode.getResident() == residentNodeTargetOfInput){

				SSBNNodeJacket previousNode = getPreviousNode(currentNode, seen, net, 
						residentNodeTargetOfInput, inputNode);

				if(previousNode != null){

					previousNode.setArgumentsOfResidentMFrag();
					
					if(!currentNode.isFinding()){
						previousNode.getSsbnNode().setPermanent(true); 
						generateRecursive(previousNode.getSsbnNode(), seen, net);
					}
					
//					previousNode.setArgumentsOfInputMFrag();

					/* it takes the parameters' names back to normal */
					if(!currentNode.getParents().contains(previousNode)){
						currentNode.addParent(previousNode.getSsbnNode(), true);
					}
				}
				continue;			
			}


			//Step 1: evaluate context and search for findings
			List<OrdinaryVariable> ovProblematicList = 
				getOVInstancesForWhichNotExistOV(inputNode.getOrdinaryVariableList(), 
						currentNode.getArguments()); 

			if(!ovProblematicList.isEmpty()){

				List<SSBNNodeJacket> parentList = 
					createSSBNNodesOfEntitiesSearchForInputNode(
						currentNode, inputNode, ovProblematicList, 
						currentNode.getArgumentsAsList()); 

				for(SSBNNodeJacket ssbnNodeJacket: parentList){

					SSBNNode ssbnNode = ssbnNodeJacket.getSsbnNode();

					ssbnNode.addArgumentsForMFrag(currentNode.getResident().getMFrag(), 
							ssbnNodeJacket.getOvInstancesOfInputMFrag()); 
					
//					ssbnNodeJacket.setSsbnNode(checkForDoubleSSBNNode(ssbnNode)); 
					if(!currentNode.isFinding()){
						ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
						ssbnNodeJacket.getSsbnNode().setPermanent(true); 
						generateRecursive(ssbnNode, seen, net);	// algorithm's core
					}
					
					logManager.appendln("Node Created: " + ssbnNode.toString());

					//TODO Analisar se é pra setar para input ou resident
//					ssbnNodeJacket.setArgumentsOfInputMFrag(); 
					ssbnNodeJacket.setArgumentsOfResidentMFrag(); 
					
					if(!currentNode.getParents().contains(ssbnNode)){
						currentNode.addParent(ssbnNode, true);
					}

				}

			}else{ //ovProblematicList.isEmpty()


				SSBNNode ssbnNode = SSBNNode.getInstance(net, residentNodeTargetOfInput, 
						new ProbabilisticNode());
				
				SSBNNodeJacket ssbnNodeJacket = new SSBNNodeJacket(ssbnNode); 

				for(OVInstance ovInstance: currentNode.getArguments()){
					addArgumentToSSBNNodeOfInputNode(inputNode, ssbnNodeJacket, ovInstance);
				}

				ssbnNodeJacket.setArgumentsOfResidentMFrag(); 

				SSBNNode test = ssbnNodesMap.get(ssbnNode.getUniqueName());
				
				if(test != null){
					ssbnNodeJacket.getSsbnNode().delete(); 
					ssbnNodeJacket.setSsbnNode(test); 
				}

				seen.add(ssbnNodeJacket.getSsbnNode());
				ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 

				boolean contextNodesOK = false;

				try {
					contextNodesOK = evaluateRelatedContextNodes(inputNode, currentNode.getArgumentsAsList());
				} catch (OVInstanceFaultException e) {
					//ovProblemList is empty... this exception never will be catch.
					e.printStackTrace();
				} 

				if(!currentNode.isFinding()){
					ssbnNodeJacket.getSsbnNode().setPermanent(true); 
				}
				
				if(contextNodesOK){
					if(!currentNode.isFinding()){
						//Step 2: context and findngs ok... do the work...	
						this.generateRecursive(ssbnNode, seen, net);	// algorithm's core	
					}
				}else{
					logManager.append("Context Nodes False for " + ssbnNode + " (input node)"); 
					ssbnNode.setUsingDefaultCPT(true); 
				}

				ssbnNodeJacket.setArgumentsOfInputMFrag(); 

				if(!currentNode.getParents().contains(ssbnNode)){
					currentNode.addParent(ssbnNode, true);
				}
			}
		}

		logManager.appendln(currentNode + "End");
		
		return currentNode;
	}



	/**
	 * Generate the network for the below evidences. 
	 * 
	 * <BR><BR>
	 * <strong>Pos requisites:</strong> 
	 * <br>If the node is a finding, the atribute permanent will be setted how <code>true</code>
	 * 
	 * 
	 * @param currentNode
	 * @param seen
	 * @param net
	 * 
	 * @return True if the path end with a Finding. Else otherside. 
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 * @throws MEBNException
	 */
	private boolean generateRecursiveDown(SSBNNode currentNode , SSBNNodeList seen, 
			ProbabilisticNetwork net) throws SSBNNodeGeneralException, ImplementationRestrictionException, MEBNException {

		logManager.appendln("\n\n[D] Recursive Call Count = " + recursiveCallCount); 
		logManager.appendln("[D]" + currentNode + ": -------------EVALUATING NODE BELOW: " + currentNode.getName() + "--------------\n"); 

		ResidentNode residentNode = currentNode.getResident(); 

		if (this.recursiveCallCount > this.recursiveCallLimit) {
			throw new SSBNNodeGeneralException(this.resource.getString("RecursiveLimit"));
		}

		this.recursiveCallCount++; 

		//------------------------- STEP 1: Search for findings -------------

		logManager.appendln("[D]" + currentNode + ":A - Search findings");

		//check if query node has a known value or it should be a probabilistic node (query the kb)
		StateLink exactValue = getKnowledgeBase().searchFinding(currentNode.getResident(), currentNode.getArguments()); 

		// Treat returned value
		if (exactValue != null) {
			// there were an exact match
			currentNode.setNodeAsFinding(exactValue.getState());
			
			if(!seen.contains(currentNode)){
				seen.add(currentNode); 
			}
			
			logManager.appendln("[D]"  + currentNode + ":Exact value of " + currentNode.getName() + "=" + exactValue.getState()); 

			//If some recursive call return true, this node is permanent... and all the nodes above
			currentNode.setPermanent(true); 
			findingList.add(currentNode); 
			return true;   //OK! NOW EVALUATE UP FOR FIND THE PARENTS... 
			
		}else{
			logManager.appendln("[D]"  + currentNode + ":Search finding fail"); 
		}

		// if the program reached this line, the node doesn't have a finding
		if(!seen.contains(currentNode)){
			seen.add(currentNode); 
		}

		
		
		
		//------------------------- STEP 2: analyze context nodes. -------------
		logManager.appendln("[D]"  + currentNode + ":B - Analyse context nodes");
		
		List<OVInstance> ovInstancesList = new ArrayList<OVInstance>(); 
		ovInstancesList.addAll(currentNode.getArguments()); 

		boolean evaluateRelatedContextNodesResult = false;

		try {
			evaluateRelatedContextNodesResult = evaluateRelatedContextNodes(
					currentNode.getResident(), ovInstancesList, null);
		} catch (OVInstanceFaultException e) {
			//Pré-requisite...
			logManager.appendln("[D]"  + currentNode + ": OVInstance fault. End down method with fail\n\n"); 
			throw new ImplementationError("OVInstance fault in the method generateRecursiveDown");  
		}

		if(!evaluateRelatedContextNodesResult){
			logManager.appendln("[D]"  + currentNode + ":Context Node fail for " + currentNode.getResident().getMFrag()); 
			currentNode.setUsingDefaultCPT(true);
			return false; 
		}
		
		logManager.appendln("[D]"  + currentNode + ":Context Node evaluation OK for " + currentNode.getResident().getMFrag()); 	

		
		
		
		//------------------------- STEP 2: Resident nodes childs in the same MFrag  -------------
		logManager.appendln("[D]" + currentNode + ":B - Resident Nodes below");

		/*
		 * Para os nós residentes filhos (que estão portanto na mesma MFrag), 
		 * alteração nenhuma deve ser feita nos argumentos do nó corrente. 
		 */
		for(ResidentNode r: residentNode.getResidentNodeChildList()){

			logManager.appendln("[D]" + currentNode + ": Resident Node Child analisy -> " + r);

			/*
			 * Analyze if it has one ov instance for each ordinary variable. If this 
			 * is not true, verify if it has some context node able to recover 
			 * the instances that match with the ordinary variable.  
			 */
			List<OrdinaryVariable> ovProblematicList = getOVInstancesForWhichNotExistOV(
					r.getOrdinaryVariableList(), ovInstancesList); 

			if(!ovProblematicList.isEmpty()){

				logManager.appendln("[D]" + currentNode + ": OVariable list problem");

				try{
				    List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
							r.getMFrag(), currentNode, r, 
							ovProblematicList, ovInstancesList, false); 
					
					for(SSBNNode ssbnNode: createdNodes){
						
						if(!currentNode.getParents().contains(ssbnNode)){
							
							generateRecursive(ssbnNode, seen, net);	
							currentNode.addParent(ssbnNode, true);
						    
						    if(ssbnNode.isPermanent()){
						    	currentNode.setPermanent(true); 
						    }
						}
					}
				}
				catch(SSBNNodeGeneralException e){
					//The search don't is made for the below part of algotithm
					logManager.appendln("[D]" + currentNode + ": Error: entity for ordinary variable don't found: " + ovProblematicList + " !\n"); 
					warningList.add(new SSBNWarning(SSBNWarning.ENTYTY_FAULT, e, currentNode, ovProblematicList)); 
				}
				finally{
					continue; //Go to next resident node child... 
				}
			}

			logManager.appendln("[D]" + currentNode + ": OVariable list OK");
		
			SSBNNode ssbnNode = null; 

			List<OVInstance> arguments = takeNecessaryArgumentsForNode(ovInstancesList, r);
			SSBNNode testSSBNNodeDuplicated = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(r, arguments)); 

			//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
			if(testSSBNNodeDuplicated == null){
				
				ssbnNode = SSBNNode.getInstance(net, r , new ProbabilisticNode());
				ssbnNode.setPermanent(false); 
				
				for(OVInstance ovInstance: arguments){
					ssbnNode.addArgument(ovInstance);
				}
				
				seen.add(ssbnNode);
				ssbnNodesMap.put(ssbnNode.getUniqueName(), ssbnNode); 
				
				generateRecursive(ssbnNode, seen, net);
				
			}else{
				
				ssbnNode = testSSBNNodeDuplicated;
				
			}

			if(ssbnNode.isPermanent()){
				currentNode.setPermanent(true); 
				if(!ssbnNode.getParents().contains(currentNode)){
					ssbnNode.addParent(currentNode, true);
				}
			}

		}

		//------------------------- STEP 3: Search childs of inputs that have this node how target -------------

		logManager.appendln("[D]" + currentNode + ":C - Input Nodes below");

		//search all the resident nodes in others MFrags referenced by this ResidentNode. 

		for(InputNode inputNode: residentNode.getInputInstanceFromList()){

			logManager.appendln("[D]" + currentNode + ": Input Node Child analisy -> " + inputNode);

			//Set the arguments of the input node in the new MFrag. 

			SSBNNodeJacket currentNodeInputSSBNNodeJacket = new SSBNNodeJacket(currentNode); 
			for(OVInstance ovInstance: currentNode.getArgumentsAsList()){
				addArgumentToSSBNNodeOfResidentNode(residentNode, 
						inputNode, currentNodeInputSSBNNodeJacket, ovInstance); 
			}
			
			List<OVInstance> ovInstancesInput = 
				currentNodeInputSSBNNodeJacket.getOvInstancesOfInputMFrag(); 
			
			//Analisy of the context nodes of the new MFrag for the inputNode. 
			boolean contextNodesOK = false; 
			
			try {
				contextNodesOK = evaluateRelatedContextNodes (inputNode, 
						ovInstancesInput, null);
				if(!contextNodesOK){
					continue; //This input node don't is valid for the algorith...
				}
			} catch (OVInstanceFaultException e1) {
				//The search don't is made for the below part of algotithm
				logManager.appendln("[D]" + currentNode + ": Error (Input node instance from )- Evaluation of context nodes don't found all entities that match the ordinary variables"); 
				warningList.add(new SSBNWarning(SSBNWarning.OV_FAULT_EVALUATION_OF_CONTEXT_FOR_INPUT_INSTANCE, e1, currentNode, inputNode)); 
				continue; 
			}
			
			for(ResidentNode residentChild: inputNode.getResidentNodeChildList()){
				logManager.appendln("[D]" + currentNode + ": Child of the input -> " + residentChild);
				logManager.appendln("[D]" + currentNode + ": do nothing...");
				
				//Evaluation of the recursion below
				//The format of the MFrag in the recursion is: 
				//      INPUTNODE_A(NODE_A) PAI NODE_A 
				//That folows: 
				//       RESIDENTCHILD = NODE_A
				// (RESIDENTCHILD is the reference of the input INPUTNODE_A
				
				if(residentChild.equals(currentNode.getResident())){
					
					List<OVInstance> temporaryListArgumentsFather = new ArrayList<OVInstance>(); 
					
					SSBNNode procNode = getProcNode(currentNode, seen, net, 
							residentChild, temporaryListArgumentsFather, inputNode);
					
					if(procNode != null){
						
						//Este nó é input de proxNode... Logo temos que colocar os
						//argumentos para poder obter tal comportamento: 
						currentNode.setRecursiveOVInstanceList(temporaryListArgumentsFather); 
						
						generateRecursive(procNode, seen, net); 
						procNode.addParent(currentNode, false);
						
						//Arrumar os argumentos do nó de input para que quando a 
						//tabela de <proc> for gerada, ele tenha as suas 
						//variaveis ordinárias corretas. 
						currentNode.setRecursiveOVInstanceList(
								currentNodeInputSSBNNodeJacket.getOvInstancesOfInputMFrag()); 
						
						if(procNode.isPermanent()){
							currentNode.setPermanent(true); 
						}
					}
					
					continue; 
				
				}else{
					
					//Acrescentar os argumentos que o nó deve assumir quando estiver 
					//sendo avaliado como nó de input para o dado residente node
					//child (ou seja, na MFrag do resident node child). 
					
					currentNode.addArgumentsForMFrag(residentChild.getMFrag(), 
							currentNodeInputSSBNNodeJacket.getOvInstancesOfInputMFrag());
				}

				/*
				 * Analyze if it has one ov instance for each ordinary variable. If this 
				 * is not true, verify if it has some context node able to recover 
				 * the instances that match with the ordinary variable.  
				 */
				List<OrdinaryVariable> ovProblematicList = getOVInstancesForWhichNotExistOV(
						residentChild.getOrdinaryVariableList(), ovInstancesInput); 

				if(!ovProblematicList.isEmpty()){

					logManager.appendln("[D]" + currentNode + ": OVariable list problem");
					
					try{
						
						//Note: if the entities for the problematic OV don't was found, 
						//the search strategy don't will be used.
						
						//Lembre-se que por pre-requisito, current node já deve ter
						//todos os argumentos esperados... portanto nao teremos que
						//fazer nenhuma alteração nele após esta busca de entidades
						//faltantes 
						
						//Os nós criados terão o nó corrente como pai...   Um pai, 
						//varios filhos, com a entidade faltante variando... 
						List<SSBNNode> createdNodes = createSSBNNodesOfEntitiesSearchForResidentNode(
								residentChild.getMFrag(), currentNode, residentChild, 
								ovProblematicList, ovInstancesInput, false);
						
						for(SSBNNode ssbnNode: createdNodes){
							if(!ssbnNode.getParents().contains(currentNode)){
								generateRecursive(ssbnNode, seen, net);			    	
								ssbnNode.addParent(currentNode, false);
							}
						}
						continue; 
					}
					catch(Exception e){
						logManager.appendln("[D]" + currentNode + ": Error - Not all ordinary variables of the resident node filled"); 
						warningList.add(new SSBNWarning(SSBNWarning.OV_FAULT_RESIDENT_CHILD, e, currentNode, residentChild)); 
						continue; //To the next input node
					}
				}


				logManager.appendln("[D]" + currentNode + ": OVariable list OK");
				//ovProblematicList.isEmpty
				SSBNNode ssbnNode = null; 

				List<OVInstance> arguments = takeNecessaryArgumentsForNode(ovInstancesList, residentChild);
				SSBNNode testSSBNNodeDuplicated = ssbnNodesMap.get(SSBNNode.getUniqueNameFor(residentChild, arguments)); 

				//This test is necessary to avoid creating two equals ssbn nodes (duplicated bayesian nodes). 
				if(testSSBNNodeDuplicated == null){
					ssbnNode = SSBNNode.getInstance(net, residentChild , new ProbabilisticNode());
					ssbnNode.setPermanent(false); 
					for(OVInstance ovInstance: arguments){
						ssbnNode.addArgument(ovInstance);
					}
					seen.add(ssbnNode);
					ssbnNodesMap.put(ssbnNode.getName(), ssbnNode); 
					generateRecursive(ssbnNode, seen, net);	
				}else{
					ssbnNode = testSSBNNodeDuplicated;
				}

				if(ssbnNode.isPermanent()){
					currentNode.setPermanent(true); 
					if(!ssbnNode.getParents().contains(currentNode)){
					    ssbnNode.addParent(currentNode, true);
					}
				}

				logManager.appendln("exit"); 
				
			}

		}

		logManager.appendln("[D]" + currentNode + ":D - The end");

		return currentNode.isPermanent(); 
	}

	public long getRecursiveCallCount() {
		return recursiveCallCount;
	}

	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	private boolean evaluateRelatedContextNodes (ResidentNode residentNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance) throws OVInstanceFaultException{

		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (residentNode.getMFrag().isUsingDefaultCPT()) {
			return false;
		};

		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(getKnowledgeBase()); 


		//TODO Refazer!!! Esta abordagem nao permite a abordagem da transitividade
		//dos nos de contexto... 

		Collection<ContextNode> contextNodeList = residentNode.getMFrag().getContextByOVCombination(
				residentNode.getOrdinaryVariableList());

		for(ContextNode context: contextNodeList){
			logManager.append("Evaluating Context Node: " + context.getLabel());
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				residentNode.getMFrag().setAsUsingDefaultCPT(true); 
				logManager.appendln("  > Result = FALSE. Use default distribution ");
				return false;  
			}else{
				logManager.appendln("  > Result = TRUE.");
			}		
		}

		return true; 
	}

	
	/**
	 * Calls ContextNodeAvaliator's method to check context node's validation.
	 * Evaluate only the context nodes for what have ordinary variables instances
	 * for all the ordinary variables present (ordinal context nodes). 
	 */
	private boolean evaluateRelatedContextNodes (InputNode inputNode, 
			List<OVInstance> ovInstances, MFragInstance mFragInstance) throws OVInstanceFaultException{

		// We assume if MFrag is already set to use Default, then some context
		// has failed previously and there's no need to evaluate again.		
		if (inputNode.getMFrag().isUsingDefaultCPT()) {
			return false;
		};

		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(getKnowledgeBase()); 


		//TODO Refazer!!! Esta abordagem nao permite a abordagem da transitividade
		//dos nos de contexto... 

		Collection<ContextNode> contextNodeList = inputNode.getMFrag().getContextByOVCombination(
				inputNode.getOrdinaryVariableList());

		for(ContextNode context: contextNodeList){
			logManager.appendln("Context Node: " + context.getLabel());
			if(!avaliator.evaluateContextNode(context, ovInstances)){
				inputNode.getMFrag().setAsUsingDefaultCPT(true); 
				logManager.appendln("Result = FALSE. Use default distribution ");
				return false;  
			}else{
				logManager.appendln("Result = TRUE.");
			}		
		}

		return true; 
	}

	
	public void evaluateContextNodeSet(MFragInstance mFragInstance, 
			List<OrdinaryVariable> ordinaryVariableList, List<OVInstance> listOVInstance){
		
		/*
		 * Casos: 
		 * 
		 * 1-> Argumentos OK, apenas avaliar validade... trivial
		 * 2-> Faltam argumentos, procurar nós e avaliar resultados que tornem verdadeiro
		 * 3-> Search case (algoritmo do XOR). 
		 */
		
		//Montar conjunto de nós de contexto de interesse
		
		
		//Avaliar conjunto
		
		
		
	}
	
	

	public void setRecursiveCallCount(long recursiveCallCount) {
		this.recursiveCallCount = recursiveCallCount;
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

	public static void printAndSaveCurrentNetwork(SSBNNode queryNode) {
		stepCount++;
		PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(queryNode.getProbabilisticNetwork()); 
		SSBNDebugInformationUtil.printNetworkInformation(logManager, queryNode, stepCount, queryName); 
	}
	
}
