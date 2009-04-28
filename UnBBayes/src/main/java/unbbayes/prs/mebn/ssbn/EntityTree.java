package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;

/**
 * The EntityTree guard the configurations of the values of the ordinary variables
 * that evaluate multiples context nodes in true result. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class EntityTree{
	
	final EntityNode root; 
	
	public EntityTree(){
		root = new EntityNode(null, null, null);  
	}
	
	public void updateTreeForNewInformation(OrdinaryVariable ov, String entityValue) 
	      throws MFragContextFailException {
		
		OrdinaryVariable ovArray[] = new OrdinaryVariable[1]; 
		String entityValues[]      = new String[1]; 
		List<String[]> entyValuesArray = new ArrayList<String[]>(); 
		
		ovArray[0] = ov; 
		entityValues[0] = entityValue; 
		entyValuesArray.add(entityValues);
		
		this.updateTreeForNewInformation(ovArray, entyValuesArray); 
		
	}
	
	/**
	 * 
	 * @param ovArray               Sequence of the ordinary variables of the context node
	 * @param entityValuesArray     Sequence of possible evaluations for the OV set. 
	 * 
	 * @throws MFragContextFailException 
	 */
	public void updateTreeForNewInformation(OrdinaryVariable ovArray[], List<String[]> entityValuesArray) 
         	throws MFragContextFailException{
		
		//Walk in the tree path to path. 
		//Build path: from the last node, take all the parents. 
		
		//Evaluate each path of the tree against the new information. 
		
		//1) For each OV
		//1.1) OV don't is in the new evaluation: OK, all results 
		//1.2) OV is in the new evaluation: delete all paths with not value among the new values
		
		//2) Add OV that don't is still in the tree
		
		//walk in each path
		for(EntityNode node: getNodesOfLastLevel()){
			
			//Mount the entitiesForOrdinaryVariable information. 
			String[] entitiesForOrdinaryVariables = new String[ovArray.length];
			
			//Search if the ordinary variable is in the ovArray and add the 
			//information about the entity if it is found. Walk for all the tree
			//search for the ordinary variable. 
			EntityNode testNode = node; 
			
			while(testNode != null){
				
				for(int index = 0; index < ovArray.length; index++){
					if(ovArray[index].equals(testNode.getOv())){
						entitiesForOrdinaryVariables[index] = testNode.getEntityName(); 
					}
				}
				
				testNode = testNode.getParent(); 
				
			}
			
			//Evaluate if it is a valid path against the new information
			List<String[]> resultToBeUsed = new ArrayList<String[]>(); 
			
			for(String[] resultList : entityValuesArray){
				
				int ovValid = 0; 
				
				for(int index = 0; index < ovArray.length; index++){
					if(entitiesForOrdinaryVariables[index] != null){
						if(!resultList[index].equals(entitiesForOrdinaryVariables[index])){
						    break; 
						}else{
							ovValid++; 
						}
					}else{
						ovValid++; 
					}
				}
				
				//This is a valid path
				if(ovValid == ovArray.length){
					resultToBeUsed.add(resultList); 
				}
			}
			
			if(resultToBeUsed.size() == 0){
				//TODO Create the message for the error. 
				throw new MFragContextFailException(); 
			}
			
			for(String[] result: resultToBeUsed){
				
				EntityNode parentNode = node; 
				for(int index = 0; index < ovArray.length; index++){
					if(entitiesForOrdinaryVariables[index] == null){
						EntityNode newNode = new EntityNode(result[index], ovArray[index], parentNode); 
						parentNode.addChildren(newNode); 
						parentNode = newNode; 
					}	
				}
			}
		}
	}	
	
	/**
	 * Monta uma combinação de todos os resultados possíveis para as variáveis 
	 * ordinárias contidas em ovSearchArray, utilizando a arvore de entidades onde 
	 * será utilizada os valores das variáveis já preenchidas. 
	 * 
	 * @param knownOVArray
	 * @param knownEntityArray
	 * @param ovSearchArray
	 * @return Um array com todos os resultados possíveis para a lista ovSearchArray. 
	 *         Os elementos do retorno estão na mesma ordem. 
	 */
	public List<String[]> recoverCombinationsEntitiesPossibles(
			OrdinaryVariable[] knownOVArray,
			LiteralEntityInstance[] knownEntityArray,
			OrdinaryVariable[] ovSearchArray){
		
		//TODO test... 
		
		List<String[]> combinationList = new ArrayList<String[]>(); 
		
		List<EntityNode> nodesOfLastLevel = getNodesOfLastLevel(); 
		
		//Search in each path
		for(EntityNode lastNodeOfPath: nodesOfLastLevel){
			
			EntityNode nodeOfPath = lastNodeOfPath; 
			int ovFoundQuant = 0; 
			String[] tempEntityArray = new String[ovSearchArray.length]; 
			
			//Node for node... 
			while(nodeOfPath != null){
				
				for(int index = 0; index <= knownOVArray.length; index++){
					if(nodeOfPath.getOv().equals(knownOVArray[index])){
						if(nodeOfPath.getEntityName().equals(knownEntityArray[index])){
							ovFoundQuant++; 
						}else{
							break; 
						}
					}
				}
				
				if(ovFoundQuant == knownOVArray.length){
					
					break; 
				
				}else{
		
					for(int index = 0; index <= ovSearchArray.length; index++){
						if(nodeOfPath.getOv().equals(ovSearchArray[index])){
							tempEntityArray[index] = nodeOfPath.getEntityName(); 
						}
					}

					nodeOfPath = nodeOfPath.getParent(); 
				
				}

			} //while (Node of a path)
			
			if(ovFoundQuant == knownOVArray.length){ //All OV found
				combinationList.add(tempEntityArray); 
			}
			
		}//for path
		
		return combinationList; 
		
	}
	
	public int getIndexOfOv(OrdinaryVariable[] ovArray, OrdinaryVariable ov){
		
		int index = 0; 
		
		for(OrdinaryVariable ovTest: ovArray){
			if(ovTest.equals(ov)){
				break; 
			}else{
				index++; 
			}
		}
		
		return index; 
		
	}
	
	/**
	 * Destroy a path in the tree. 
	 * 
	 * @param node
	 */
	public void destroyPath(EntityNode node){
		
		EntityNode parent = node.getParent(); 
		
	   if(parent != null){
		   parent.removeChildren(node);  
		   if(parent.getChildren().size() == 0){
			   destroyPath(parent); 
		   }
	   }
	}
	
	private List<EntityNode> getNodesOfLastLevel(){
		
		EntityNode node = root; 
		
		//node should have a node of the last level 
		while(node.getChildren().size()!=0){
			node = node.getChildren().get(0); 
		}
		
		//then we recover all the nodes that have its same ordinary variable
		return getNodesOfLevel(node.getOv()); 
	}
	
	/**
	 * Get the level of a ov in the tree. The root have level 0, its children 
	 * have level 1. If the ov don't is in the tree, return -1.  
	 */
	public int getOvLevel(OrdinaryVariable ov){
		
		EntityNode node = root; 
		int level = 0; 
		
		while((!node.getOv().equals(ov))||(node == null)){
			node = node.getChildren().get(0); 
			level++; 
		}
		
		if(node!=null){
			return level; 
		}else{
			return -1; 
		}
		
	}

	/**
	 * Get all nodes of the level where is ov. If ov = null, return the root (level 0) 
	 */
	public List<EntityNode> getNodesOfLevel(OrdinaryVariable ov){
		
		List<EntityNode> resultList = new ArrayList<EntityNode>(); 
		
		if(ov == null){
			resultList.add(root); 
		}else{
			List<EntityNode> nodeList = getTreeHowList(); 

			for(EntityNode node: nodeList){
				if(node.getOv().equals(ov)){
					resultList.add(node); 
				}
			}
		}

		return resultList;
		
	}
	
	public List<OVInstance> getOVInstances(){
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		
		List<EntityNode> entityNodeList = getTreeHowList(); 
		for(EntityNode entityNode: entityNodeList){
			LiteralEntityInstance lei = LiteralEntityInstance.getInstance(
					entityNode.getEntityName(), entityNode.getOv().getValueType()); 
			
			OVInstance ovInstance = OVInstance.getInstance(entityNode.getOv(), 
					lei);
			
			ovInstanceList.add(ovInstance); 
		}
		
		return ovInstanceList; 
	}
	
	public List<EntityNode> getTreeHowList(){
		EntityNode node = root; 
		
		List<EntityNode> list = new ArrayList<EntityNode>(); 
		
		addChildrenToList(node, list); 
		
		return list; 
	}
	
	public void addChildrenToList(EntityNode node, List<EntityNode> list){
		
		List<EntityNode> children = node.getChildren(); 
		
		if(children.size() != 0){
			list.addAll(children); 
			for(EntityNode child: children){
				addChildrenToList(child, list); 
			}
		}
	}
	
	public void addLevel(OrdinaryVariable ov, List<String> newNodeList){
		this.addLevel(root, ov, newNodeList); 
	}
	
	private void addLevel(EntityNode root, OrdinaryVariable ov, List<String> newNodeList){
		if(root.getChildren().size()!=0){
			for(EntityNode node: root.getChildren()){
				addLevel(node, ov, newNodeList); 
			}
		}else{
			for(String newNodeName: newNodeList){
				EntityNode newNode = new EntityNode(newNodeName, ov, root); 
				root.addChildren(newNode); 
			}
		}
	}
	
	/**
	 * The node of the EntityTree that guard a value of a ordinary variable
	 * get by the evaluation of a context node. 
	 * 
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
	 */
	private class EntityNode{
		OrdinaryVariable ov; 
		String entityName; 
		EntityNode parent; 
		List<EntityNode> children; 
		
		public EntityNode(String _entityName, OrdinaryVariable _ov, EntityNode _parent){
			this.entityName = _entityName;
			this.ov = _ov; 
			this.parent = _parent; 
			this.children = new ArrayList<EntityNode>(); 
		}
		
		public void addChildren(EntityNode e){
			this.children.add(e); 
		}
		
		public void removeChildren(EntityNode e){
			this.children.remove(e); 
		}
		
		public OrdinaryVariable getOv() {
			return ov;
		}

		public String getEntityName() {
			return entityName;
		}

		public EntityNode getParent() {
			return parent;
		}

		public List<EntityNode> getChildren() {
			return children;
		}
		
		/**
		 * [OX1=ENTITY1]
		 */
		@Override
		public String toString(){
			String string = ""; 
			
			string+="["; 
			string+= this.getOv().getName(); 
			string+="="; 
			string+= this.getEntityName(); 
			string+="]"; 
			
			return string; 
		}
	}
	
	//Debug classes
	public void printTree(){
		printNodeChildren(root, 1); 
	}
	
	public void printNodeChildren(EntityNode node, int identation){
		for(EntityNode nodeChild : node.getChildren()){
			for(int i = 0; i < identation; i++){
				System.out.print("  ");
			}
			System.out.println(nodeChild); 
			identation++; 
			printNodeChildren(nodeChild, identation); 
		}
	}
}