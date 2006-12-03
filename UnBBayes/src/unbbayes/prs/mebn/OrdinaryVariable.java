package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;

public class OrdinaryVariable {
 
	
	private String name; 
	
	private MFrag mFrag;
	
	private List<Node> isOVariableOfList; 
	
	public OrdinaryVariable(String name, MFrag mFrag){
		
		this.name = name; 
		this.mFrag = (MFrag)mFrag; 
		
		isOVariableOfList = new ArrayList<Node>(); 
		
	}
	
	/**
	 * Method responsible for return the MFrag where the Ordinary 
	 * Variable are inside.
	 */	
	
	public MFrag getMFrag(){
		return mFrag; 
	}
	 
	/**
	 * Method responsible for return the name of the OV. 
	 */	
	
	public String getName(){
		return name; 
	}
	
	/**
	 * Turn the name of the Ordinary Variable 
	 * @param name The new name 
	 */
	
	public void setName(String name){
		this.name = name; 
	}
	
	/**
	 * Add a node in the list of nodes when this o variable is
	 * present (if the node alredy is in the list, don't do nothing). 
	 * @param node
	 */
	
	protected void addIsOVariableOfList(Node node){
	   if(!isOVariableOfList.contains(node)){
	      isOVariableOfList.add(node);
	   }
	}
	
	/**
	 * Remove a node of the IsOVariableList. 
	 * @param node
	 */
	
	protected void removeIsOVariableOfList(Node node){
		isOVariableOfList.remove(node);
	}
	
}
 
