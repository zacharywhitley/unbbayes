package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;
import unbbayes.util.Debug;

/**
 * The EntityTree guard the configurations of the values of the ordinary variables
 * that evaluate multiples context nodes in true result. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class EntityTree{
	
	final EntityNode root; 
	
	private ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	
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
	 * @param ovArray
	 * @param entityValuesArray
	 * @throws MFragContextFailException If the tree is destroyed after this remotion (some level disappear) 
	 */
	public void deletePath(OrdinaryVariable ovArray[], String entityValuesArray[]) throws MFragContextFailException{
		
		//1 - Search the ordinary variable most near to the root of the tree and starting in it, and 
		//search in which path is the deleted value. 
		//2 - Down the tree selecting the entity in each level correspondent to the deleted entity
		//3 - After arrive in the leaf, or in the last ov searched, delete the marked path. 
		
//		List<EntityNode> nodesToRemoveList = new ArrayList<EntityNode>(); 
		
		System.out.println("Entities:");
		for(int i = 0 ; i < entityValuesArray.length; i++){
			System.out.println(entityValuesArray[i]);
		}
		
		List<EntityNode> lastNodeOfPossiblePathList = new ArrayList<EntityNode>(); 
		lastNodeOfPossiblePathList.add(root); 
		
		printNode(root, 0); 
		
		while(lastNodeOfPossiblePathList.get(0).getChildren().size() != 0){
			
			//Test if the next ov is one of the setted ovs. 
			int indexOV = -1; 
			for (int i = 0; i < ovArray.length; i++){
				if (ovArray[i].equals(lastNodeOfPossiblePathList.get(0).getChildren().get(0).getOv())){
					indexOV = i; 
					break; 
				}
			}
			
			List<EntityNode> newLastNodeOfPossiblePathList = new ArrayList<EntityNode>(); 
			
			//More a level without any ordinary variable of the list of the user 
			if (indexOV == -1){
				for(EntityNode node: lastNodeOfPossiblePathList){
					newLastNodeOfPossiblePathList.addAll(node.getChildren());  //Advance lastNodeOfPossiblePath to the next level of the tree 
				}
				
				lastNodeOfPossiblePathList = newLastNodeOfPossiblePathList; 
				
				continue;
				
			}
			
			//If we arrive here, we have a level with information that the user want delete 
			//Evaluation in each path, which of them have information
			
			boolean findingEntity = false; 
			
			for(EntityNode node: lastNodeOfPossiblePathList){
				
				for(EntityNode child: node.getChildren()){
					if (child.getEntityName().equals(entityValuesArray[indexOV])){
						newLastNodeOfPossiblePathList.add(child); 
						findingEntity = true; 
						break; 
					}
				}
			}
			
			//Entity not found in any path!!! 
			if(!findingEntity){
				System.out.println("Entity not found in any path: " + entityValuesArray[indexOV] + " ov: " + ovArray[indexOV]);
				System.out.println("Remotion of the path don't made");
				return; 
			}else{
				lastNodeOfPossiblePathList = newLastNodeOfPossiblePathList; 
			}
			
		}
		
		//Remove the paths 
		for(EntityNode node: lastNodeOfPossiblePathList){
			System.out.println("Irá deletar nó: " + node.getEntityName());
			this.destroyPath(node);
			if(isTreeEmpty()){
				throw new MFragContextFailException(resource.getString("EmptyTree")); 
			}
		}
		
	}
	
	public void printNode(EntityNode node, int level){
		
		for(int i = 0; i <= level; i++ ){
			System.out.print("  ");
		}
		
		System.out.println(node.getOv() + " " + node.getEntityName());
		
		for(EntityNode child:node.getChildren()){
			printNode(child, level+1);
		}
	}
	
	/**
	 * Update the tree of evaluation of the context nodes of a MFrag with the new 
	 * result. 
	 * 
	 * @param ovArray               Sequence of the ordinary variables of the context node
	 * @param entityValuesArray     Sequence of possible evaluations for the OV set. 
	 * 
	 * @throws MFragContextFailException throw if the new results added to the tree
	 *                                   don't are possible against the previous 
	 *                                   result. The context node set of the mfrag
	 *                                   fail (don't is consistent). 
	 */
	public void updateTreeForNewInformation(
			                 OrdinaryVariable ovArray[], 
			                 List<String[]> entityValuesArray) 
         	throws MFragContextFailException{
		
		//Walk in the tree path to path. 
		//Build path: from the last node, take all the parents. 
		
		//Evaluate each path of the tree against the new information. 
		
		//1) For each OV
		//1.1) OV don't is in the new evaluation: OK, all results 
		//1.2) OV is in the new evaluation: delete all paths with not value among the new values
		
		//2) Add OV that don't is still in the tree
		
		//walk in each path
		for(EntityNode lastNodeOfAPath: getNodesOfLastLevel()){
			
			//Mount the entitiesForOrdinaryVariable information. 
			String[] entitiesForOrdinaryVariables = new String[ovArray.length];
			
			//Search if the ordinary variable is in the ovArray and add the 
			//information about the entity if it is found. Walk for all the tree
			//search for the ordinary variable. 
			EntityNode testNode = lastNodeOfAPath; 
			
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
			
			for(String[] resultPossible : entityValuesArray){
				
				int ovValid = 0; 
				
				for(int index = 0; index < ovArray.length; index++){
					if(entitiesForOrdinaryVariables[index] != null){
						if(!resultPossible[index].equals(entitiesForOrdinaryVariables[index])){
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
					resultToBeUsed.add(resultPossible); 
				}
			}
			
			if(resultToBeUsed.size() == 0){ //This don't is a valid way!!! 
				destroyPath(lastNodeOfAPath); 
			}else{
				//Add the new ordinary variables and its evaluation to the path
				for(String[] result: resultToBeUsed){
					EntityNode parentNode = lastNodeOfAPath; 
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
		
		//Verify if the tree was destroyed with the new informations: 
		//The evaluation is false... 
		if(isTreeEmpty()){
			throw new MFragContextFailException(resource.getString("EmptyTree")); 
		}
		
	}	
	
	/**
	 * Update the tree of evaluation of the context nodes of a MFrag with the new 
	 * result. 
	 * 
	 * @param ovArray               Sequence of the ordinary variables of the context node
	 * @param entityValuesArray     Sequence of possible evaluations for the OV set. 
	 * 
	 * @throws MFragContextFailException throw if the new results added to the tree
	 *                                   don't are possible against the previous 
	 *                                   result. The context node set of the mfrag
	 *                                   fail (don't is consistent). 
	 */
	public void updatePathOnTreeWithNewInformation(
	                               List<OVInstance> path, 
			                       OrdinaryVariable ovArray[], 
			                       List<String[]> entityValuesArray) 
         	throws MFragContextFailException{
		
		
		//Find selected path 
	
		List<EntityNode> possibleWays = new ArrayList<EntityNode>(); 
		possibleWays.add(root); 
		
		int qtdOVFound = 0; 
		
		while(true){
			
			List<EntityNode> newList = new ArrayList<EntityNode>(); 
			
			//Search for the index of the node on path 
			int index = -1;
			for(int i = 0; i < path.size(); i++){
				if(path.get(i).getOv().equals(possibleWays.get(0).getOv())){
					index = i; 
					qtdOVFound++; 
					break; //we have the index!
				}
			}
			
			if(index != -1){
				for(EntityNode node: possibleWays){
					if(node.getEntityName().equals(path.get(index).getEntity().getInstanceName())){
						newList.add(node);
					}
				}
				possibleWays = newList; 
			}
			
			//Advance to the next level 
			newList = new ArrayList<EntityNode>(); 
			for(EntityNode node: possibleWays){
				if(node.getChildren().size()!=0){
					newList.addAll(node.getChildren()); 
				}
			}
			
			if(newList.size()!=0){
				possibleWays = newList; 
			}else{
				break; 
			}
		}
		
		if(qtdOVFound != path.size()){
			//TODO Exception... the path don't is in the tree 
			throw new IllegalStateException("Not all variables of the informed path are on tree!!!"); 
		}
		
		for(EntityNode lastNodeOfAPath: possibleWays){
			
			for(String[] arrayEntity: entityValuesArray){
				
				EntityNode parentNode = lastNodeOfAPath; 
				
				for(int i = 0; i < ovArray.length; i++){
					EntityNode node = new EntityNode(arrayEntity[i], ovArray[i], parentNode); 
					parentNode.addChildren(node);		
					parentNode = node; 
				}
				
			}
		}
	}		
	
	/**
	 * Get the list of ordinary variables of the tree. 
	 * 
	 * @return A list containing the ordinary variables of the tree 
	 */
	public List<OrdinaryVariable> getListOfOrdinaryVariables(){
		
		List<OrdinaryVariable> list = new ArrayList<OrdinaryVariable>(); 
		
		// Each ordinary variable is in a level of the tree. So is sufficient get 
		// a root node and climb the tree for the first branch until arrive to a leaf, getting the 
		// ordinary variable of each level. 
		EntityNode node = root; 
		
		//node should have a node of the last level 
		while(node.getChildren().size()!=0){
			node = node.getChildren().get(0); 
			list.add(node.getOv()); 
		}
		
		return list; 
		
	}
	
	/**
	 * Build the combination of possible outcomes for the Ordinary Variables 
	 * contained in ovSearchArray. To this, uses the branches of the Entity Tree
	 * that satisfies the ordinary variables already fulfilled. 
	 * 
	 * @param knownOVArray
	 * @param knownEntityArray
	 * @param ovSearchArray
	 * @return Array with all the possible outcomes for the ordinary variables 
	 * in ovSearchArray. The elements of the return are on the same order 
	 */
	public List<String[]> recoverCombinationsEntitiesPossibles(
			OrdinaryVariable[] knownOVArray,
			ILiteralEntityInstance[] knownEntityArray,
			OrdinaryVariable[] ovSearchArray){
		
		List<String[]> combinationList = new ArrayList<String[]>(); 
		
		List<EntityNode> nodesOfLeafLevel = getNodesOfLastLevel(); 
		
		//Search in each path
		for(EntityNode lastNodeOfPath: nodesOfLeafLevel){
			
			EntityNode nodeOfPath = lastNodeOfPath; 
			int ovFoundCount = 0; 
			String[] tempEntityArray = new String[ovSearchArray.length]; 
			
			//Node for node... 
			while(nodeOfPath.getOv() != null){ //the root
				
				for(int index = 0; index < knownOVArray.length; index++){
					if(nodeOfPath.getOv().equals(knownOVArray[index])){
						if(nodeOfPath.entityName.equalsIgnoreCase(knownEntityArray[index].getInstanceName())){
							ovFoundCount++; 
						}else{
							break; 
						}
					}
				}
				
	            //see if the ov is one of the ov fault... 
				for(int index = 0; index < ovSearchArray.length; index++){
					if(nodeOfPath.getOv().equals(ovSearchArray[index])){
						tempEntityArray[index] = nodeOfPath.getEntityName(); 
					}
				}
				
				nodeOfPath = nodeOfPath.getParent(); 

			} //while (Node of a path)
			
			if(ovFoundCount == knownOVArray.length){ //All OV found
				
				boolean resultComplete = true; 
				for(int i = 0; i < ovSearchArray.length; i++){
					if(tempEntityArray[i] == null){
						resultComplete = false; 
					}
				}
				
				if(resultComplete){
					combinationList.add(tempEntityArray);
				}else{
					throw new RuntimeException("Ordinary Variable fault!!!"); 
				}
				
			}
			
		}//for path
		
		return combinationList; 
		
	}
	
	/** 
	 * Recover all the possible combinations of entities 
	 */
	public List<String[]> recoverCombinationsEntitiesPossibles(){
		
		List<String[]> combinationList = new ArrayList<String[]>(); 
		
		List<EntityNode> nodesOfLastLevel = getNodesOfLastLevel(); 
		
		//Search in each path
		for(EntityNode lastNodeOfPath: nodesOfLastLevel){
			
			EntityNode nodeOfPath = lastNodeOfPath; 
			
			String[] entityArray = new String[this.getNumberOfLevels()]; 
			
			int i = 0; 
			while(nodeOfPath != root){
				entityArray[i] = nodeOfPath.getEntityName();
				nodeOfPath = nodeOfPath.getParent(); 
				i++; 
			}
			
			combinationList.add(entityArray); 
		}//for path
		
		return combinationList; 
		
	}
	
	/**
	 * Return all ov instances 
	 */
	public List<OVInstance> getOVInstanceList(){
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		List<EntityNode> entityNodeList = getTreeHowList(); 
		
		for(EntityNode entityNode: entityNodeList){
			ILiteralEntityInstance lei = LiteralEntityInstance.getInstance(
					entityNode.getEntityName(), entityNode.getOv().getValueType()); 
			
			OVInstance ovInstance = OVInstance.getInstance(entityNode.getOv(), 
					lei);
			
			ovInstanceList.add(ovInstance); 
		}
		
		return ovInstanceList; 
	}
	
	/**
	 * Return all ov instances for the ordinary variable. If don't have ov instances
	 * return a empty list. 
	 */
	public List<OVInstance> getOVInstanceListForOrdinaryVariable(OrdinaryVariable ov){
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		
		List<EntityNode> entityNodeList = getNodesOfLevel(ov);
		
		for(EntityNode entityNode: entityNodeList){
			ILiteralEntityInstance lei = LiteralEntityInstance.getInstance(
					entityNode.getEntityName(), entityNode.getOv().getValueType()); 
			
			OVInstance ovInstance = OVInstance.getInstance(entityNode.getOv(), 
					lei);
			
			ovInstanceList.add(ovInstance); 
		}
		
		return ovInstanceList; 
	}		
	
	
	/*--------------------------------------------------------------------------
	 * Private Methods
	 *-------------------------------------------------------------------------*/
	
	private int getIndexOfOv(OrdinaryVariable[] ovArray, OrdinaryVariable ov){
		
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
	 * @param node leaf of the path
	 */
	private void destroyPath(EntityNode node){
		
		EntityNode parent = node.getParent(); 
		
	   if(parent != null){
		   parent.removeChildren(node);  
		   System.out.println("Removed children = " + node);
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
	
	private boolean isTreeEmpty(){
	
		if(root.getChildren().size()==0){
			return true; 
		}else{
			return false; 
		}
		
	}
	
	/**
	 * Get the level of a ov in the tree. The root have level 0, its children 
	 * have level 1. If the ov don't is in the tree, return -1.  
	 */
	private int getOvLevel(OrdinaryVariable ov){
		
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
	
	private int getNumberOfLevels(){
		
		EntityNode node = root; 
		int numberOfLevels = 0; 
		
		//node should have a node of the last level 
		while(node.getChildren().size()!=0){
			node = node.getChildren().get(0);
			numberOfLevels++;
		}
		
		return numberOfLevels; 
		
	}
	
	private List<EntityNode> getTreeHowList(){
		EntityNode node = root; 
		
		List<EntityNode> list = new ArrayList<EntityNode>(); 
		
		addChildrenToList(node, list); 
		
		return list; 
	}
	
	private void addChildrenToList(EntityNode node, List<EntityNode> list){
		
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
	 * Get all nodes of the level where is ov. If ov = null, return the root (level 0) 
	 */
	private List<EntityNode> getNodesOfLevel(OrdinaryVariable ov){
		
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
			if(this.getOv()!=null){
				string+= this.getOv().getName();
			}
			string+="="; 
			string+= this.getEntityName(); 
			string+="]"; 
			
			return string; 
		}
	}
	
	
	
	/*--------------------------------------------------------------------------
	 * Debug Methods
	 *-------------------------------------------------------------------------*/
	
	public void printTree(){
		printNodeChildren(root, 1); 
	}
	
	public void printNodeChildren(EntityNode node, int identation){
		
		int newIdentation = identation + 1; 
		
		for(EntityNode nodeChild : node.getChildren()){
			for(int i = 0; i < identation; i++){
				Debug.print("  ");
			}
			Debug.println(nodeChild.toString()); 
			printNodeChildren(nodeChild, newIdentation); 
		}
	}
}
