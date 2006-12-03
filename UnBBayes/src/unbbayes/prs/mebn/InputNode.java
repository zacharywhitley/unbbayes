package unbbayes.prs.mebn;

import java.util.List;

public class InputNode extends MultiEntityNode {

	private List<ResidentNode> residentNodeChildList;

	/*
	 * These two variables (inputInstanceOfRV and inputInstanceOfNode) have an
	 * 'or' relationship. That means that if this input node is an input
	 * instance of RV, than it is not from a node. The oposite is also true. In
	 * other words, if one is not null the other must be null.
	 */
	private BuiltInRV inputInstanceOfRV;
	private ResidentNode inputInstanceOfNode;
	
	public InputNode(){
		super(); 
	}
	
	public void setInputInstanceOfRV(BuiltInRV rv){
		if (inputInstanceOfNode == null){
			inputInstanceOfRV = rv; 
		}
		else{
		
		}
	}
	
	public void setInputInstanceOfNode(ResidentNode node){
		if (inputInstanceOfRV == null){
			inputInstanceOfNode = node; 
		}
		else{
			//TODO levantar excessao
		}
	}	
	
	public ResidentNode getInputInstanceOfNode(){
		return inputInstanceOfNode; 
	}		
	
	public BuiltInRV getInputInstanceOfRV(){
		return inputInstanceOfRV; 
	}	
	

	
	/**
	 * Add a resident node in the list of resident node childs. 
	 * @param node: the node that will be add. 
	 */
	public void addResidentNodeChild(ResidentNode node){
		this.residentNodeChildList.add(node); 
	}

	/**
	 * Method responsible for deleting this input node. 
	 *
	 */
    
	public void delete() {
		
		residentNodeChildList = null; 
		
	}	
	
}
