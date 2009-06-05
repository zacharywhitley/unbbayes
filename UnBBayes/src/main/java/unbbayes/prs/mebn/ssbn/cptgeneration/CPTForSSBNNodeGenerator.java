package unbbayes.prs.mebn.ssbn.cptgeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.io.ILogManager;
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

public class CPTForSSBNNodeGenerator {

	public ILogManager logManager; 
	
	public CPTForSSBNNodeGenerator(ILogManager logManager){
		this.logManager = logManager; 
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
			
			logManager.appendln(level, "\nGenerate CPT for node " + root); 
			
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
		
//		logManager.appendln("---- CPT for node: " + ssbnNode.getUniqueName() + "-----"); 
//		logManager.appendln("Parents:"); 
//		for(SSBNNode parent: ssbnNode.getParents()){
//			logManager.appendln(parent.toString()); 
//		}
//		logManager.appendln("Init"); 
		
		if(ssbnNode.isPermanent()){
			//Generate the cpt of the context father ssbnnode
			if(ssbnNode.getContextFatherSSBNNode()!=null){ 
				try {
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
		

//		    GUIPotentialTable gpt; 
		
//			logManager.appendln("\nGenerate table for node (with context father): " + ssbnNode);
//			logManager.appendln("Parents:");
//			for(SSBNNode parent: ssbnNode.getParents()){
//				logManager.appendln("  " + parent);
//			}
			
			Debug.setDebug(false);
			
			Map<String, List<SSBNNode>> mapParentsByEntity = new HashMap<String, List<SSBNNode>>(); 
			Map<String, PotentialTable> mapCPTByEntity = new HashMap<String, PotentialTable>(); 
			
			ContextFatherSSBNNode contextFather = ssbnNode.getContextFatherSSBNNode();
			for(LiteralEntityInstance entity: contextFather.getPossibleValues()){
				mapParentsByEntity.put(entity.getInstanceName(), new ArrayList<SSBNNode>()); 
			}
			
			//Step 0: Calcular a tabela do nó de contexto

			ssbnNode.getContextFatherSSBNNode().generateCPT();
			
//			gpt = new GUIPotentialTable(ssbnNode.getContextFatherSSBNNode().getProbNode().getPotentialTable()); 
//			gpt.showTable("Table for Node " + ssbnNode.getContextFatherSSBNNode());
			
			Collection<SSBNNode> generalParents = new ArrayList<SSBNNode>(); //Independent of the entity problematic
			
			System.out.println("OVProblematic = " + contextFather.getOvProblematic().getName() + " " + contextFather.getOvProblematic().getMFrag().getName() + contextFather.getOvProblematic().getValueType().getName());
			
			for(SSBNNode parent: ssbnNode.getParents()){
				
				System.out.println("Ordinary variables for parent " + parent.getName());
				boolean contain = false; 
				for(OrdinaryVariable ov: parent.getOVs()){
					contain = ov.equals(contextFather.getOvProblematic());
				}
				
//				if(!parent.getOVs().contains(contextFather.getOvProblematic())){ //For some motive don't go!!!
				if(!contain){	
					generalParents.add(parent); 
				}else{
					String entity = parent.getArgumentByOrdinaryVariable(contextFather.getOvProblematic()).getEntity().getInstanceName(); 
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
				    parent.removeChildNode(tempNode); 
				}
				
				mapCPTByEntity.put(entity.getInstanceName(), cpt);
				Debug.println("Tabela armazenada: " + entity.getInstanceName() + " " + cpt.tableSize());
			
				//TODO remove the parents of the tempNode because it is added to the list
				//of child nodes of the other node!!!
				for(SSBNNode parent: groupParents){
//					tempNode.removeParentNode(parent); 
				}
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
			
			Debug.setDebug(true);
			
			logManager.appendln("CPT OK\n");
		
	}
	
}
