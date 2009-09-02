package unbbayes.prs.mebn.ssbn.cptgeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.io.log.ILogManager;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.ContextFatherSSBNNode;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
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

	public ILogManager logManager; 
	
	public CPTForSSBNNodeGenerator(ILogManager _logManager){
		this.logManager = _logManager; 
	}
	
	/**
	 * Generate the SSBN nodes parents of root and for the SSBN nodes parents of 
	 * findingsDown and child of root.
	 *  
	 * @param root
	 * @param findingsDown
	 * @throws MEBNException
	 * @throws SSBNNodeGeneralException 
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
			
			logManager.appendln("\nGenerate CPT for node " + root); 
			
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
				logManager.appendln("ERROR IN THE CPT EVALUATION OF NODE " + root.getName());
				throw e; 
			}
			catch (SSBNNodeGeneralException e) {
				logManager.appendln("ERROR IN THE CPT EVALUATION OF NODE " + root.getName());
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
		
		logManager.appendln("---- CPT for node: " + ssbnNode.getUniqueName() + "-----"); 
		logManager.appendln("Parents:"); 
		for(SSBNNode parent: ssbnNode.getParents()){
			logManager.appendln(parent.toString()); 
		}
//		logManager.appendln("Init"); 
		
		if(ssbnNode.isPermanent()){
			//Generate the cpt of the context father ssbnnode
			if(ssbnNode.getContextFatherSSBNNode()!=null){ 
				try {
					logManager.appendln(" Context Parent Node: " + ssbnNode.getContextFatherSSBNNode()); 
					generateCPTForNodeWithContextFather(ssbnNode);
				} catch (InvalidOperationException e1) {
					logManager.appendln("ERROR IN THE CPT EVALUATION OF NODE " + ssbnNode.getName()); 
					e1.printStackTrace();
					throw new SSBNNodeGeneralException(e1.getMessage()); 
				}
			}else{
				ssbnNode.getCompiler().generateCPT(ssbnNode);
				PotentialTable cpt = ssbnNode.getProbNode().getPotentialTable(); 
				GUIPotentialTable gpt = new GUIPotentialTable(cpt); 
//				gpt.showTable("Table for Node " + ssbnNode);
			}
		}
		
//		logManager.appendln("End");
		
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
	
			Debug.setDebug(false);
			
			Map<String, List<SSBNNode>> mapParentsByEntity = new HashMap<String, List<SSBNNode>>(); 
			Map<String, PotentialTable> mapCPTByEntity = new HashMap<String, PotentialTable>(); 
			
			ContextFatherSSBNNode contextFather = ssbnNode.getContextFatherSSBNNode();
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				mapParentsByEntity.put(entity.getInstanceName(), new ArrayList<SSBNNode>()); 
			}
			
			logManager.appendln("\nGenerate table for node (with context father): " + ssbnNode);
			logManager.appendln("Parents:");
			for(SSBNNode parent: ssbnNode.getParents()){
				logManager.appendln("  " + parent);
			}
			logManager.appendln("OVProblematic = " + contextFather.getOvProblematic().getName() 
					+ " " + contextFather.getOvProblematic().getMFrag().getName() 
					+ contextFather.getOvProblematic().getValueType().getName());
			

			//Step 0: Generate the context node CPT. 

			if(!ssbnNode.getContextFatherSSBNNode().isCptGenerated()){
				ssbnNode.getContextFatherSSBNNode().generateCPT();
			}

//		    GUIPotentialTable gpt; 			
//			gpt = new GUIPotentialTable(ssbnNode.getContextFatherSSBNNode().getProbNode().getPotentialTable()); 
//			gpt.showTable("Table for Node " + ssbnNode.getContextFatherSSBNNode());
			
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
					mapParentsByEntity.get(entity).add(parent); 
				}
			}
			
			int sizeCPTOfEntity = 0; 
			
			PotentialTable cptResidentNode = ssbnNode.getProbNode().getPotentialTable(); 
						
			//Step 2: Build the CPT's for the diverses groups of parents. 
			int parentPosition = 1; 
			
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				
				ArrayList<SSBNNode> parentsForEntityList = new ArrayList<SSBNNode>(); 
				
				List<SSBNNode> parentsByEntityList = mapParentsByEntity.get(entity.getInstanceName()); 
				
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
				
//				gpt = new GUIPotentialTable(cpt); 
//				gpt.showTable("Table for Node " + ssbnNode + " - " + groupParents);
				
				//Remove the temp node of the list of children of the node. 
				for(SSBNNode parent: parentsForEntityList){
					parent.getProbNode().getChildren().remove(tempNode.getProbNode()); 
				    parent.removeChildNode(tempNode); 
				}
				
				mapCPTByEntity.put(entity.getInstanceName(), cpt);
				Debug.println("Tabela armazenada: " + entity.getInstanceName() + " " + cpt.tableSize());
			
				//TODO remove the parents of the tempNode because it is added to the list
				//of child nodes of the other node!!!
				for(SSBNNode parent: parentsForEntityList){
//					tempNode.removeParentNode(parent); 
				}
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
				List<SSBNNode> parentsForEntity = mapParentsByEntity.get(entity); 
				
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
			
			Debug.setDebug(false);
			
			logManager.appendln("CPT OK\n");
		
	}
	
}
