/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.util.SerializablePoint2D;

public class MultiEntityNode extends Node {
 
	private static final long serialVersionUID = -5435895970322752281L;

	private MFrag mFrag;
	
	private List<Argument> argumentList;
	 
	private List<MultiEntityNode> innerTermOfList;
	 
	private List<MultiEntityNode> innerTermFromList;
	
	private List<Entity> possibleValueList; 
	
	//by young
//	private static final Point DEFAULT_MEBN_SIZE = new Point(100,20); 
	
//	protected static SerializablePoint2D size = new SerializablePoint2D(
//			DEFAULT_MEBN_SIZE.getX(), DEFAULT_MEBN_SIZE.getY());


	private static Color color;
	
	/**
	 * Constructs a MultiEntityNode
	 */	
	
	public MultiEntityNode(){
		
		super();
		
		argumentList = new ArrayList<Argument>(); 
		innerTermOfList = new ArrayList<MultiEntityNode>();
		innerTermFromList = new ArrayList<MultiEntityNode>(); 
		possibleValueList = new ArrayList<Entity>(); 
	}
	
	/**
	 * It returns the node's type. Not used for this node.
	 * @see Node#getType()
	 */
	// Please, avoid using @Override annotation, since it makes interface extraction (refactor) very difficult,
	// because it supposes a inherited method is declared always inside a class, 
	// and fails if it becomes declared inside an interface.
//	@Override
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
	//by young
	// public static Color getColor() {
     //   return color;
    //}

    /**
     *  Sets the new color for all context node.
     *
     * @return The new color of all context node in RGB.
     */
	//by young
    // public static void setColor(int c) {
    //    color = new Color(c);
    //}	
    
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
			if (value.getName().equals(possibleValue)){
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
	
	/**
	 * @param argNumber Number of the argument to be recover
	 * @return the argument or null if don't have a argument with the specific number
	 */
	public Argument getArgumentNumber(int argNumber){
		for(Argument argument: argumentList){
			if(argument.getArgNumber() ==  argNumber){
				return argument; 
			}
		}
		return null; 
	}
	
	public List<MultiEntityNode> getInnerTermOfList(){
		return innerTermOfList; 
	}
	
	public List<MultiEntityNode> getInnerTermFromList(){
		return innerTermFromList; 
	}		
	
	/**
	 * !!!Maybe dead code!!!
	 * It should be avoided. Use DomainResidentNode.getPossibleValueLinkList
	 * whenever possible.
	 * @return a list which elements were added by MultiEntityNode.addPossibleValue(value)... It
	 * obviously doesn't contain entity instances (since they are dead codes)
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
					// recursively searches for the ovs inside a complex argument
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
	
	/**
	 * Returns the node's size (x,y) where x = width and y = height.
	 * 
	 * @return The node's size.
	 */
	//by young
	public Point2D.Double getSize() {

		return size;

	}
	
}
 
