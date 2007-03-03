package unbbayes.prs.mebn;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.Entity;

public class MultiEntityNode extends Node {
 
	private static final long serialVersionUID = -5435895970322752281L;

	private MFrag mFrag;
	
	private List<Argument> argumentList;
	 
	private List<MultiEntityNode> innerTermOfList;
	 
	private List<MultiEntityNode> innerTermFromList;

	private List<Entity> possibleValueList; 

	private static Color color;
	
	/**
	 * Constructs a MultiEntityNode
	 */	
	
	public MultiEntityNode(){
		
		super();
		
		//TODO Melhorar isso!!! 
		// width
		size.x = 100;
		// height
		size.y = 20;		
		
		argumentList = new ArrayList<Argument>(); 
		innerTermOfList = new ArrayList<MultiEntityNode>();
		innerTermFromList = new ArrayList<MultiEntityNode>(); 
		possibleValueList = new ArrayList<Entity>(); 
	}
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public MFrag getMFrag(){
		return mFrag; 
	}
	
	/**
     *  Gets all context node's color.
     *
     * @return The color of all context node's color.
     */
    public static Color getColor() {
        return color;
    }

    /**
     *  Sets the new color for all context node.
     *
     * @return The new color of all context node in RGB.
     */
    public static void setColor(int c) {
        color = new Color(c);
    }	
	
	/**
	 * Method responsible for removing this node from its MFrag.
	 *
	 */
	public void removeFromMFrag() {
		mFrag = null;
	}
	
	public void addArgument(Argument arg){
		argumentList.add(arg); 
	}
	
	
	public void removeArgument(Argument arg){
		argumentList.remove(arg); 
	}
    

	public void addInnerTermOfList(MultiEntityNode instance){
		innerTermOfList.add(instance); 
	}	
	
	public void addInnerTermFromList(MultiEntityNode instance){
		innerTermFromList.add(instance); 
	}	
	
	public void addPossibleValue(Entity possibleValue){
		possibleValueList.add(possibleValue); 
	}
	
	/**
	 * Remove the possible value with the name 
	 * @param possibleValue name of the possible value
	 */
	public void removePossibleValueByName(String possibleValue){
		
		for(Entity value : possibleValueList){
			if (value.getName().compareTo(possibleValue) == 0){
				possibleValueList.remove(value);
				return; 
			}
		}
		
	}
	
	/**
	 * Verifies if the possible value is on the list of possible values
	 * of the node. 
	 * @param possibleValue name of the possible value
	 * @return true if it is present or false otherside
	 */
	public boolean existsPossibleValueByName(String possibleValue){
		
		for(Entity value : possibleValueList){
			if (value.getName().compareTo(possibleValue) == 0){
				return true; 
			}
		}
		
		return false; 
	}	
	
	public List<Argument> getArgumentList(){
		return argumentList; 
	}
	
	public List<MultiEntityNode> getInnerTermOfList(){
		return innerTermOfList; 
	}
	
	public List<MultiEntityNode> getInnerTermFromList(){
		return innerTermFromList; 
	}		
	
	public List<Entity> getPossibleValueList(){
		return possibleValueList; 
	}
	
	/**
	 * Verify if the entity is a state of the node 
	 * Warning: the search will be for the entity and not for the
	 * name of entity.
	 * @param entity The entity 
	 * @return true if the entity is a state, false otherside
	 */
	public boolean hasPossibleValue(Entity entity) {
		return possibleValueList.contains(entity);
	}
	
	/**
	 * Verify if the node has a state with the name
	 * @param stateName Name of state
	 * @return true if have a state with the name, false otherside
	 */
	public boolean hasPossibleValue(String stateName) {
		for (Entity entity : possibleValueList) {
			if (entity.getName().equals(stateName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method is responsible for returning the index where this state is 
	 * located.
	 * @param stateName State's name desired to know the index.
	 * @return Returns the state position, index. -1 if the state is not found.
	 */
	public int getPossibleValueIndex(String stateName) {
		int index = 0;
		for (Entity entity : possibleValueList) {
			if (entity.getName().equals(stateName)) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
}
 
