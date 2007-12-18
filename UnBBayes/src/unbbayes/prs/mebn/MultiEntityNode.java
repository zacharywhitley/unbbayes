package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
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
		
		size.x = 100;
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
     * Gets the default size of a MultiEntityNode 
     * 
     * @return the dafault size of a MultiEntityNode
     */
    public static Point getDefaultSize(){
    	return new Point(100,20);
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
	 * Remove all possible values of the node
	 */
	public void removeAllPossibleValues(){
		possibleValueList.clear(); 
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
	
	/**
	 * !!!Maybe dead code!!!
	 * It should NOT be used for resident nodes. Use DomainResidentNode.getPossibleValueLinkList
	 * instead!!
	 * @return a list which elements were added by MultiEntityNode.addPossibleValue(value)...
	 */
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
	
	
	/**
	 * 
	 * @param ovs Set of OrdinaryVariables to be searched inside its arguments.
	 * @return True if all OVs within "ovs" are used as argument. False otherwise.
	 */
	public boolean hasAllOVs(OrdinaryVariable...ovs) {
		boolean found = false;
		for (OrdinaryVariable ov : ovs) {
			found = false;
			for (Argument arg : this.getArgumentList()) {
				if (arg.isSimpleArgRelationship()) {
					if (arg.getOVariable().equals(ov)){
						found = true;
						break;
					}
				} else {
					// recursivelly searches for the ovs inside a complex argument
					found = arg.getArgumentTerm().hasAllOVs(ovs);
				}				
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	
	protected Collection<OrdinaryVariable> fillAlreadyCountedOVs(Collection<OrdinaryVariable> alreadyCountedOVs) {
		OrdinaryVariable ov = null;
		MultiEntityNode node = null;
		for (Argument arg : this.getArgumentList()) {
			if (arg.isSimpleArgRelationship()) {
				ov = arg.getOVariable();
				if (!alreadyCountedOVs.contains(ov)) {
					alreadyCountedOVs.add(ov);
				}
			} else {
				node = arg.getArgumentTerm();
				if (node != null) {
					node.fillAlreadyCountedOVs(alreadyCountedOVs);
				}				
			}
		}
		return alreadyCountedOVs;
	}
	
	/**
	 * 
	 * @return how many different ovs appear inside this node, including inner nodes.
	 */
	public int getAllOVCount() {
		Collection<OrdinaryVariable> ovs = new ArrayList<OrdinaryVariable>();
		ovs = this.fillAlreadyCountedOVs(ovs);
		return ovs.size();
	}
	
	
	/**
	 * Counts how many simple arg relationships are present within ArgumentList
	 * @return
	 */
	public int getSimpleArgRelationshipCount() {
		int ret = 0;
		for (Argument arg : this.getArgumentList()) {
			if (arg.isSimpleArgRelationship()) {
				ret++;
			}
		}
		return ret;
	}
	
}
 
