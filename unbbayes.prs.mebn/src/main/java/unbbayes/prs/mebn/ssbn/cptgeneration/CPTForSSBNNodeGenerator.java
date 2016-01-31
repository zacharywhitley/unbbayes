package unbbayes.prs.mebn.ssbn.cptgeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.ContextFatherSSBNNode;
import unbbayes.prs.mebn.ssbn.ILiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;

/**
 * Class responsible for generate the CPT's of the SSBN Nodes. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class CPTForSSBNNodeGenerator {

	private ISSBNLogManager logManager; 
	
	IdentationLevel level1 = new IdentationLevel(null); 
	IdentationLevel level2 = new IdentationLevel(level1); 
	IdentationLevel level3 = new IdentationLevel(level2); 
	IdentationLevel level4 = new IdentationLevel(level3); 
	IdentationLevel level5 = new IdentationLevel(level4); 
	IdentationLevel level6 = new IdentationLevel(level5);
	
	public CPTForSSBNNodeGenerator(ISSBNLogManager _logManager){
		this.logManager = _logManager; 
	}
	
	/**
	 * Generate the SSBN nodes parents of root and for the SSBN nodes parents of 
	 * findingsDown and child of root.
	 *  
	 * @param root
	 * @throws MEBNException
	 * @throws SSBNNodeGeneralException 
	 * @deprecated use {@link #generateCPTForAllSSBNNodes(SSBN)} instead.
	 */
	public void generateCPTForAllSSBNNodes(SSBNNode root) throws MEBNException, SSBNNodeGeneralException{
		generateCPTForAllSSBNNodes(root, 0);
	}
	
	/*
	 * Recursively generate the cpt for the node root, its fathers and its 
	 * children. 
	 * 
	 * @param root
	 * @param level
	 * @throws MEBNException
	 * @throws SSBNNodeGeneralException
	 */
	private void generateCPTForAllSSBNNodes(SSBNNode root, int level) throws MEBNException, 
	                    SSBNNodeGeneralException{

		if(root.isCptAlreadyGenerated()){
			return; 
		}else{
			
//			logManager.printText(level2, false,"Generate CPT for node " + root); 
			
			//------------------1) PARENTS
//			logManager.appendln(level, "Parents:"); 
			SSBNNode[] parents = root.getParents().toArray(new SSBNNode[root.getParents().size()]); 
			for(SSBNNode parent: parents){
//				logManager.appendln(level, ">" + parent); 
				generateCPTForAllSSBNNodes(parent, level + 1); 
			}

			//------------------2) NODE
//			logManager.appendln(level, "CPT for root");
//			logManager.appendln(level, ">" + root); 
			if(root.isCptAlreadyGenerated()){
				return; 
			}
			
			try{
			generateCPT(root);
			}
			catch (MEBNException e) {
				if (logManager != null) {
					logManager.printText(level3, false,"ERROR IN THE CPT EVALUATION OF NODE " + root.getName());
				}
				throw e; 
			}
			catch (SSBNNodeGeneralException e) {
				if (logManager != null) {
					logManager.printText(level3, false,"ERROR IN THE CPT EVALUATION OF NODE " + root.getName());
				}
				throw e; 
			}
			
			root.setCptAlreadyGenerated(true); 

			//------------------3) CHILDREN
//			logManager.appendln(level, "Children:"); 
			//To avoid the ConcurrentModificationException: the method 
			//generateCPTForNodeWithContextFather add and remove childs for the
			//children list. 
			SSBNNode[] children = root.getChildren().toArray(new SSBNNode[root.getChildren().size()]); 
			for(SSBNNode child: children){
//				logManager.appendln(level, ">" + child); 
				generateCPTForAllSSBNNodes(child, level + 1); 
			}
		}
	}
	
	/**
	 * Generate the CPT for the ssbnNode
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
		
		if (logManager != null) {
			logManager.printText(level2, false,"Generate CPT for node " + ssbnNode.getUniqueName()); 
			logManager.printText(level3, false,"Parents:"); 
			for(SSBNNode parent: ssbnNode.getParents()){
				logManager.printText(level4, false, parent.toString()); 
			}
		}
//		logManager.appendln("Init"); 
		
		if(ssbnNode.isPermanent()){
			//Generate the cpt of the context father ssbnnode
			if(ssbnNode.getContextFatherSSBNNode()!=null){ 
				try {
					if (logManager != null) {
						logManager.printText(level3, false, " Context Parent Node: " + ssbnNode.getContextFatherSSBNNode()); 
					}
					generateCPTForNodeWithContextFather(ssbnNode);
				} catch (InvalidOperationException e1) {
					if (logManager != null) {
						logManager.printText(level3, false, "ERROR IN THE CPT EVALUATION OF NODE " + ssbnNode.getName()); 
					}
					throw new SSBNNodeGeneralException("ERROR IN THE CPT EVALUATION OF NODE " + ssbnNode ,e1); 
				}
			}else{
				ssbnNode.getCompiler().generateLPD(ssbnNode);
//				PotentialTable cpt = ssbnNode.getProbNode().getProbabilityFunction(); 
//				GUIPotentialTable gpt = new GUIPotentialTable(cpt); 
//				gpt.showTable("Table for Node " + ssbnNode);
			}
		}
		
		if (logManager != null) {
			logManager.printText(level3, false,"Generated"); 
		}
//		logManager.appendln("End");
		
	}
	

	/**
	 * Generate the CPT of a note that have a context father. 
	 * For this, use a XOR strategy where the table of the evaluation of each 
	 * possible state for the context node is generated and the table for the 
	 * ssbnNode is generate from its. 
	 * 
	 * @param ssbnNode
	 * @throws SSBNNodeGeneralException
	 * @throws MEBNException
	 * @throws InvalidOperationException
	 */
	
	protected void generateCPTForNodeWithContextFather(SSBNNode ssbnNode) 
			throws SSBNNodeGeneralException, MEBNException, InvalidOperationException {

		//Active the println for debug. 
		boolean debugMethod = false; 

		
				
		// Create Map linked each entity (state of the context node) to its ssbnNodes and tables. 
		
		Map<String, List<SSBNNode>> mapParentsByEntity = new HashMap<String, List<SSBNNode>>(); 
		Map<String, PotentialTable> mapCPTByEntity = new HashMap<String, PotentialTable>(); 

		ContextFatherSSBNNode contextFather = ssbnNode.getContextFatherSSBNNode();
		for(ILiteralEntityInstance entity: contextFather.getPossibleValues()){
			mapParentsByEntity.put(entity.getInstanceName().toUpperCase(), new ArrayList<SSBNNode>()); 
		}

		if (logManager != null) {
			logManager.printText(level3, false, "Node " + ssbnNode + "have context father.");
			logManager.printText(level3, false," OV Problematic = " + contextFather.getOvProblematic().getName() 
					+ " " + contextFather.getOvProblematic().getMFrag().getName() 
					+ contextFather.getOvProblematic().getValueType().getName());
		}

		

		// Generate the context node CPT. 

		if(!ssbnNode.getContextFatherSSBNNode().isCptGenerated()){
			ssbnNode.getContextFatherSSBNNode().generateCPT();
		}

		
		
		

		// Separate the parent nodes that contain the OV problematic for that
		// nodes that don't contain (generalParents - don't contain)
			
		Collection<SSBNNode> generalParents = new ArrayList<SSBNNode>(); 


		for(SSBNNode parent: ssbnNode.getParents()){	

			boolean containOVProblematic = false; 
			for(OrdinaryVariable ov: parent.getOVs()){
				containOVProblematic = ov.equals(contextFather.getOvProblematic());
				if(containOVProblematic){
					String entity = parent.getArgumentByOrdinaryVariable(ov).getEntity().getInstanceName(); 
					mapParentsByEntity.get(entity.toUpperCase()).add(parent); 
					break; 
				}
			}

			if(!containOVProblematic){	
				generalParents.add(parent); 
			}

		}

		
		
		
		// Organize the nodes in this order:
		// 0 -> Node itself (index 0)
		// 1 -> Generic Nodes
		// 2 -> Entity Nodes
		// 3 -> Context Node (index n - 1)
		
		
		
		//----------------------------------------------------------------------
		// Part 1: Organize the position of the general Parents. 
		//----------------------------------------------------------------------
		
		PotentialTable cptResidentNode = ssbnNode.getProbNode().getProbabilityFunction(); 
		
		int parentPosition = 1; 

		for(SSBNNode parent: generalParents){
			int index = cptResidentNode.getVariableIndex(parent.getProbNode()); 
			cptResidentNode.moveVariableWithoutMoveData(index, parentPosition); 
			parentPosition++; 
		}

		
		
		
		//[TODO] Check if the parent nodes is in the same order for each entity

		//----------------------------------------------------------------------
		// Part 2: - Organize the position of the entity Parents; 
		//         - Generate the CPT's for each entity Parent; 
		//         - Build the structure necessary for create the final CPT
		//----------------------------------------------------------------------
		
		int[] sizeVariable;
		int[] multiplier; 
		boolean firstEvaluateOK = false; 

		// ColumnValidForEntity keep with parent is valid for each entity in 
		// the CPT final (with all the parents)
		
		byte[][] parentValidForEntity = 
				new byte[contextFather.getPossibleValues().size()] [ssbnNode.getParents().size()]; 

		for(int i = 0; i < contextFather.getPossibleValues().size(); i++) {
			for(int j = 0; j < generalParents.size(); j++) {
				parentValidForEntity[i][j] = 1; 
			}
		}

		int indexEntity = -1; 

		for(ILiteralEntityInstance entity: contextFather.getPossibleValues()){


			List<SSBNNode> parentsByEntityList = mapParentsByEntity.get(entity.getInstanceName().toUpperCase()); 

			ArrayList<SSBNNode> parentsForEntityList = new ArrayList<SSBNNode>(); 
			parentsForEntityList.addAll(generalParents); //OK, always in the some order 
			parentsForEntityList.addAll(parentsByEntityList); //Always in the same order. 

			indexEntity++; 

			for(SSBNNode parent: parentsByEntityList){
				int actualParentPosition = cptResidentNode.getVariableIndex(parent.getProbNode()); 
				cptResidentNode.moveVariableWithoutMoveData(actualParentPosition, parentPosition); 

				parentValidForEntity[indexEntity][parentPosition - 1] = 1; 

				parentPosition++;
			}


			//Generate the table for this group of parents (this temp table
			// will be used at the xor. 
			SSBNNode tempNode = SSBNNode.getInstance(ssbnNode.getResident()); 
			for(SSBNNode parent: parentsForEntityList){
				tempNode.addParent(parent, false); 
			}
			PotentialTable cpt = tempNode.getCompiler().generateCPT(tempNode); 

//			if(debugMethod) {
//				GUIPotentialTable gpt; 
//				gpt = new GUIPotentialTable(cpt); 
//				gpt.showTable("Table for Node " + ssbnNode + " - " + entity);
//			}

			//Remove the temp node of the list of children of the node. 
			for(SSBNNode parent: parentsForEntityList){
				parent.getProbNode().getChildren().remove(tempNode.getProbNode()); 
				parent.removeChildNode(tempNode); 
			}

			mapCPTByEntity.put(entity.getInstanceName(), cpt);

			if(!firstEvaluateOK) {

				if(debugMethod) {
					System.out.println(" ");
					System.out.println("Generate table of positions: ");
				}

				sizeVariable = new int[parentsForEntityList.size()]; 
				multiplier   = new int[parentsForEntityList.size()]; 

				int m = 0; 

				for(int i = 0; i < parentsForEntityList.size(); i++) {
					sizeVariable[i] = parentsForEntityList.get(i).getProbNode().getStatesSize(); 

					if(i == 0) {
						m = 1;  
					}else {
						m*= sizeVariable[i-1];
					}

					multiplier[i] = m; 
					
					if(debugMethod) {
						System.out.print("Index = " + i + " " + parentsForEntityList.get(i).getName() + " ");
						System.out.println("Size = " + sizeVariable[i] + " Multiplier = " + multiplier[i] );
					}
				}

				firstEvaluateOK = true; 
			}
		}	

		// Print the Column Valid for debug
		if(debugMethod) {
			for(int i = 0; i < contextFather.getPossibleValues().size(); i ++) {
				for(int j = 0; j < ssbnNode.getParents().size(); j++) {
					System.out.println("ColumnValid[" + i + "," + j + "]= " + parentValidForEntity[i][j]);
				}
			}
		}
		

		
		//----------------------------------------------------------------------
		// Part 1: Reajust position of the context node
		//----------------------------------------------------------------------
		
		cptResidentNode.moveVariableWithoutMoveData(
				cptResidentNode.getVariableIndex(ssbnNode.getContextFatherSSBNNode().getProbNode())
				, cptResidentNode.getVariablesSize() - 1); 

		
		
		
	
		// Generate the vector index (indexTable) for the generation of the final CPT. 
		// This index contain for each column of the final table the state index 
		// for each parent node. 
		
		
		//---------------------------------------------------------------------
		//       0 0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3  <= Column number
		//       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		//---------------------------------------------------------------------
		// 3->   0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1
		// 2->   0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 
		// 1->   0 0 0 0 1 1 1 1 0 0 0 0 1 1 1 1 0 0 0 0 1 1 1 1 0 0 0 0 1 1 1 1
		// 0->   0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1
		//---------------------------------------------------------------------
		// ^ 
		// Node parent number
		

		int numLines = cptResidentNode.getVariablesSize() - 1; //Subtract the ssbnNode; 

		int numColumns = 1; 
		for(int i = 0; i < cptResidentNode.getVariablesSize() - 1 ; i++) {
			numColumns*=cptResidentNode.getVariableAt(i+1).getStatesSize(); 
		}

		if(debugMethod) {
			System.out.println("Num Columns = " + numColumns + " Num Lines = " + numLines);
		}
		
		int[][] indexTable = new int[numLines][numColumns]; 


		int multiplierFull = 1; 

		for(int i=0; i < numLines; i++){

			if(debugMethod) {
				System.out.println("Evaluate node " + (i+1) + " - " + cptResidentNode.getVariableAt(i+1));
				System.out.println("MultiplierFull = " + multiplierFull);
			}
			
			int j = 0; 

			if(i > 0) {
				multiplierFull*= cptResidentNode.getVariableAt(i).getStatesSize(); 
			}

			while (j < (numColumns - 1)) {

				int numState = 0; 

				//Iteration in the states of the variable
				//getVariableAt(i+1) -> eliminate ssbnNode original
				for (int q = 0; q < cptResidentNode.getVariableAt(i+1).getStatesSize(); q++){

					for (int m=0; m < multiplierFull; m++){
						indexTable[i][j] = numState;
						j++; 
					}
					numState++; 
				}

			}

		} 

		// Print the indexTable for debug
		if(debugMethod) {
			for (int i = 0; i < numLines; i ++) {
				for (int j=0; j < numColumns; j++) {
					System.out.print(indexTable[i][j] + " ");
				}
				System.out.println();
			}
		}



		
		// Generate the final CPT
		
		
		int numEntities = contextFather.getProbNode().getStatesSize(); 
		int columnsByEntity = numColumns/numEntities; 

		if(debugMethod) {
			System.out.println("Num Entities = " + numEntities);
			System.out.println("Columns By Entity = " + columnsByEntity);
		}
		
		int columnIndex              = 0; 
		int positionColOriginTable   = 0; 
		
		int positionCellDestTable    = 0; 
		int positionCellTableOrigin  = 0; // Position cell to cell  
		
		for(int entityIndex = 0; entityIndex < numEntities; entityIndex++) {

			if(debugMethod) {
				System.out.println("EntityIndex= " + entityIndex);
			}
			
			String entity = contextFather.getProbNode().getStateAt(entityIndex);
			PotentialTable cptEntity = mapCPTByEntity.get(entity); 

			for(int columnOfEntity = 0; columnOfEntity < columnsByEntity; columnOfEntity++) {

				if(debugMethod) {
					System.out.println("ColumnOfEntity= " + columnOfEntity);
				}
				
				positionColOriginTable = 0; 
				int multiplierPosition = 1; 
				int numEstadosAnt = 0; 

				for(int line = 0; line < (numLines - 1); line++) {      //retire the contextNode of numLines

					if (parentValidForEntity[entityIndex][line] == 1){
						if(numEstadosAnt != 0) { 
							multiplierPosition*= numEstadosAnt; // line + 1: para adicionar o nó residente
							numEstadosAnt = cptResidentNode.getVariableAt(line+1).getStatesSize(); 
						}else {
							//									multiplierPosition*= cptResidentNode.getVariableAt(0).getStatesSize(); 
							numEstadosAnt = cptResidentNode.getVariableAt(line+1).getStatesSize();
						}
					}
					
					if(debugMethod) {
						System.out.print("Line= " + line + " ");
						System.out.println("Multiplier= " + multiplierPosition);
					}

					positionColOriginTable += indexTable[line][columnIndex]*multiplierPosition*parentValidForEntity[entityIndex][line];

				}	

				positionCellTableOrigin = positionColOriginTable*cptResidentNode.getVariableAt(0).getStatesSize();

				for(int stateIndex = 0; stateIndex < cptResidentNode.getVariableAt(0).getStatesSize(); stateIndex++) {
					
					if(debugMethod) {
						System.out.println("State Index = " + stateIndex + " Pos Dest = " + positionCellDestTable + 
								" Pos Org = " + (positionCellTableOrigin + stateIndex));
					}

					cptResidentNode.setValue(positionCellDestTable, cptEntity.getValue(positionCellTableOrigin + stateIndex));

					positionCellDestTable += 1; 	
				}

				columnIndex++; 
			}


		}


//		if(debugMethod) {
//			GUIPotentialTable gpt; 
//			gpt = new GUIPotentialTable(ssbnNode.getProbNode().getProbabilityFunction()); 
//			gpt.showTable("Table for Node " + ssbnNode);
//		}

		if (logManager != null) {
			logManager.printText(level3, false,"CPT OK");
		}

	}
	
	
	/**
	 * Note: This version contained errors and was replaced... 
	 * This is here only for documentation. 
	 * 
	 * The XOR algorith: 
	 * Generate the cpt of 
	 * - the context node father
	 * - the node that have the indeterminated parents
	 * 
	 */
	private void generateCPTForNodeWithContextFatherOldVersion(SSBNNode ssbnNode) 
	      throws SSBNNodeGeneralException, MEBNException, InvalidOperationException {
	
		
		
		Map<String, List<SSBNNode>> mapParentsByEntity = new HashMap<String, List<SSBNNode>>(); 
		Map<String, PotentialTable> mapCPTByEntity = new HashMap<String, PotentialTable>(); 
		
		ContextFatherSSBNNode contextFather = ssbnNode.getContextFatherSSBNNode();
		for(ILiteralEntityInstance entity: contextFather.getPossibleValues()){
			mapParentsByEntity.put(entity.getInstanceName().toUpperCase(), new ArrayList<SSBNNode>()); 
		}
		
		if (logManager != null) {
			logManager.printText(level3, false, "Node " + ssbnNode + "have context father.");
//			logManager.appendln("Parents:");
//			for(SSBNNode parent: ssbnNode.getParents()){
//				logManager.appendln("  " + parent);
//			}
			logManager.printText(level3, false," OV Problematic = " + contextFather.getOvProblematic().getName() 
					+ " " + contextFather.getOvProblematic().getMFrag().getName() 
					+ contextFather.getOvProblematic().getValueType().getName());
		}
		

		//Step 0: Generate the context node CPT. 

		if(!ssbnNode.getContextFatherSSBNNode().isCptGenerated()){
			ssbnNode.getContextFatherSSBNNode().generateCPT();
		}

//	    GUIPotentialTable gpt; 			
//		gpt = new GUIPotentialTable(ssbnNode.getContextFatherSSBNNode().getProbNode().getPotentialTable()); 
//		gpt.showTable("Table for Node " + ssbnNode.getContextFatherSSBNNode());
		
		Collection<SSBNNode> generalParents = new ArrayList<SSBNNode>(); //Independent of the entity problematic
		
		// Step 1: Separate the parent nodes that contain the OV problematic for that
		// nodes that don't contain. 
		for(SSBNNode parent: ssbnNode.getParents()){	
			
			boolean containOVProblematic = false; 
			for(OrdinaryVariable ov: parent.getOVs()){
				containOVProblematic = ov.equals(contextFather.getOvProblematic());
				if(containOVProblematic){
					break; 
				}
			}

			if(!containOVProblematic){	
				generalParents.add(parent); 
			}else{
				String entity = parent.getArgumentByOrdinaryVariable(contextFather.getOvProblematic()).getEntity().getInstanceName(); 
				mapParentsByEntity.get(entity.toUpperCase()).add(parent); 
			}
		}
		
		int sizeCPTOfEntity = 0; 
		
		PotentialTable cptResidentNode = ssbnNode.getProbNode().getProbabilityFunction(); 
					
		//Step 2: Build the CPT's for the diverses groups of parents. 
		int parentPosition = 1; 
		
		for(ILiteralEntityInstance entity: contextFather.getPossibleValues()){
			
			ArrayList<SSBNNode> parentsForEntityList = new ArrayList<SSBNNode>(); 
			
			List<SSBNNode> parentsByEntityList = mapParentsByEntity.get(entity.getInstanceName().toUpperCase()); 
			
			parentsForEntityList.addAll(parentsByEntityList); //TODO Sempre na mesma ordem? 
			
			for(SSBNNode parent: parentsByEntityList){
				int actualParentPosition = cptResidentNode.getVariableIndex(parent.getProbNode()); 
				cptResidentNode.moveVariableWithoutMoveData(actualParentPosition, parentPosition); 
				parentPosition++; 
			}
			
			parentsForEntityList.addAll(generalParents); //OK, always in the some order 
			for(SSBNNode parent: generalParents){
				int index = cptResidentNode.getVariableIndex(parent.getProbNode()); 
				cptResidentNode.moveVariableWithoutMoveData(index, parentPosition); 
				parentPosition++; 
			}
			
			//Generate the table for this group of parents (this temp table
			// will be used at the xor. 
			SSBNNode tempNode = SSBNNode.getInstance(ssbnNode.getResident()); 
			for(SSBNNode parent: parentsForEntityList){
				tempNode.addParent(parent, false); 
			}
			PotentialTable cpt = tempNode.getCompiler().generateCPT(tempNode); 
			sizeCPTOfEntity = cpt.tableSize();
			
//			gpt = new GUIPotentialTable(cpt); 
//			gpt.showTable("Table for Node " + ssbnNode + " - " + groupParents);
			
			//Remove the temp node of the list of children of the node. 
			for(SSBNNode parent: parentsForEntityList){
				parent.getProbNode().getChildren().remove(tempNode.getProbNode()); 
			    parent.removeChildNode(tempNode); 
			}
			
			mapCPTByEntity.put(entity.getInstanceName(), cpt);
			Debug.println("Tabela armazenada: " + entity.getInstanceName() + " " + cpt.tableSize());
		
			//TODO remove the parents of the tempNode because it is added to the list
			//of child nodes of the other node!!!
//			for(SSBNNode parent: parentsForEntityList){
//				tempNode.removeParentNode(parent); 
//			}
		}			
		
		//Reajust the position of the context node
		cptResidentNode.moveVariableWithoutMoveData(
				cptResidentNode.getVariableIndex(ssbnNode.getContextFatherSSBNNode().getProbNode())
				, cptResidentNode.getVariablesSize() - 1); 
		
		
		//Step 3: Make the XOR at the tables generate from the previous instructions
		
		Debug.println("Table for the resident node: ");

		int columnsByEntity = cptResidentNode.tableSize() / 
		                    ssbnNode.getResident().getPossibleValueListIncludingEntityInstances().size();
		columnsByEntity /= contextFather.getProbNode().getStatesSize(); 
		
		Debug.println("Columns per entity = " + columnsByEntity);

		int rows = ssbnNode.getProbNode().getStatesSize(); 
		
		for(int stateIndex = 0; stateIndex < contextFather.getProbNode().getStatesSize(); stateIndex++){
		    
			String entity = contextFather.getProbNode().getStateAt(stateIndex);
			Debug.println("\n State index = " + stateIndex + ": " + entity);
			PotentialTable cptEntity = mapCPTByEntity.get(entity); 
			
			//Discover initial position
			List<SSBNNode> parentsForEntity = mapParentsByEntity.get(entity.toUpperCase()); 

			if (parentsForEntity == null || parentsForEntity.size() <= 0) {
				if (logManager != null) {
					logManager.printBox1("Warning! No parents for entity " + entity + " was found. Aborting XOR table generation for " + contextFather.getProbNode());
				}
				break;
			}
			ProbabilisticNode pnEntity = parentsForEntity.get(0).getProbNode(); //??
		
			int indexEntityInCptResidentNode = cptResidentNode.getVariableIndex(pnEntity) - 1; //the index 0 is the node itself
			int entityIndex = (indexEntityInCptResidentNode + parentsForEntity.size() - 1)/ parentsForEntity.size(); 
			
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
			for(int index = indexEntityInCptResidentNode + parentsForEntity.size(); 
			    index < cptResidentNode.getVariablesSize() - 2;  //minus Entity row and Node row.  
			    index++){
	
				repAll*= cptResidentNode.getVariableAt(index).getStatesSize(); 
				
			}
			
			int inOrder = 1; 
			for(SSBNNode node: parentsForEntity){
				inOrder*= node.getResident().getPossibleValueList().size(); 
			}
			
			Debug.println("Index = " + indexEntityInCptResidentNode);
			Debug.println("RepetiÃ§Ãµes = " + repColum);
			Debug.println("PosiÃ§Ã£o na tabela do residente = " + positionTableResident);
			Debug.println("PosiÃ§Ã£o na tabela da entidade = " + positionTableEntity);
			Debug.println("Linhas = " + rows);
			Debug.println("RepitiÃ§Ãµes de tudo = " + repAll);
			Debug.println("Em Ordem = " + inOrder);
			
			while(positionTableEntity < cptEntity.tableSize() - 1){
				int positionTableEntityFinal = -1; 
				
				for(int rAll= 0; rAll < repAll; rAll++){
					
					int positionTableEntityInitial = positionTableEntity; 
					
					for(int order = 0; order < inOrder; order++){
						for(int rCol = 0; rCol < repColum; rCol++){
							int positionAuxEntity = positionTableEntityInitial; 
							for(int k = 0; k < rows; k++){
//								Debug.println("k=" + k + ";rCol=" + rCol + 
//										";order=" + order + ";rAll=" + rAll + 
//										" [" + positionTableResident + "] recebe de " 
//										+ "[" + positionAuxEntity + 
//										"] o valor = " + cptEntity.getValue(positionAuxEntity));
								
//								Debug.print(cptEntity.getValue(positionAuxEntity) + " ");
								cptResidentNode.setValue(positionTableResident, cptEntity.getValue(positionAuxEntity)); 
								positionTableResident++; 
								positionAuxEntity++; 
							}
//							Debug.println("");
						}
						positionTableEntityInitial += rows; 
					}
					
					positionTableEntityFinal = positionTableEntityInitial;
					
				}
				
				positionTableEntity = positionTableEntityFinal; 
			}
		}

//		gpt = new GUIPotentialTable(ssbnNode.getProbNode().getPotentialTable()); 
//		gpt.showTable("Table for Node " + ssbnNode);
		
		
		if (logManager != null) {
			logManager.printText(level3, false,"CPT OK");
		}
		
	}
	
	/**
	 * @return the logManager
	 */
	public ISSBNLogManager getLogManager() {
		return logManager;
	}

	/**
	 * @param logManager the logManager to set
	 */
	public void setLogManager(ISSBNLogManager logManager) {
		this.logManager = logManager;
	}

	
	/**
	 * It generates CPTs for all {@link SSBNNode} in a {@link SSBN}.
	 * A {@link SSBNNode} returning true for {@link SSBNNode#isCptAlreadyGenerated()}
	 * will be ignored.
	 * @param ssbn
	 * @throws MEBNException
	 * @throws SSBNNodeGeneralException
	 */
	public void generateCPTForAllSSBNNodes(SSBN ssbn)throws MEBNException, SSBNNodeGeneralException{
		// initial assertion
		if (ssbn == null) {
			return;
		}
		// iterate over all nodes
		for (SSBNNode root : ssbn.getSsbnNodeList()) {
			if(root.isCptAlreadyGenerated()){
				// ignore nodes returning true for isCptAlreadyGenerated
				continue; 
			}else{
				generateCPT(root);
				// set flag to ignore next time
				root.setCptAlreadyGenerated(true); 
			}
		}
	}
	
}
