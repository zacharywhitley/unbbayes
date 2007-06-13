package unbbayes.prs.mebn;

import java.util.List;
import java.util.Vector;

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
	
	/**
	 * set the input instance of this node for the BuiltInRV 
	 * @param rv
	 */
	public void setInputInstanceOf(BuiltInRV rv){
		inputInstanceOfNode = null; 
		inputInstanceOfRV = rv; 

	}
	
	/** 
	 * set the input instance of this node for the ResidentNode or BuiltInRV
	 * @param node
	 */
	public void setInputInstanceOf(ResidentNode node){
		inputInstanceOfRV = null; 
		inputInstanceOfNode = node; 
		
	}	
	
	/**
	 * set the input instance of propriety for null... 
	 */
	public void setInputInstanceOf(){
		inputInstanceOfRV = null; 
		inputInstanceOfNode = null; 
	}

	/** 
	 * return the value of the property input instance of 
	 * @return one BuiltInRV or one ResidentNode
	 */	
	public Object getInputInstanceOf(){
		if (inputInstanceOfNode != null){
			return inputInstanceOfNode; 
		}
		else{
			return inputInstanceOfRV;
		}
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
		inputInstanceOfNode = null;
	}


}
