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

public class MultiEntityNode extends Node implements IMultiEntityNode {
 
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getType()
	 */
	// Please, avoid using @Override annotation, since it makes interface extraction (refactor) very difficult,
	// because it supposes a inherited method is declared always inside a class, 
	// and fails if it becomes declared inside an interface.
//	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getMFrag()
	 */
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#removeFromMFrag()
	 */
	public void removeFromMFrag() {
		mFrag = null;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#addArgument(unbbayes.prs.mebn.Argument)
	 */
	public void addArgument(Argument arg){
		argumentList.add(arg); 
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#removeArgument(unbbayes.prs.mebn.Argument)
	 */
	public void removeArgument(Argument arg){
		argumentList.remove(arg); 
	}
    

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#addInnerTermOfList(unbbayes.prs.mebn.MultiEntityNode)
	 */
	public void addInnerTermOfList(MultiEntityNode instance){
		innerTermOfList.add(instance); 
	}	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#addInnerTermFromList(unbbayes.prs.mebn.MultiEntityNode)
	 */
	public void addInnerTermFromList(MultiEntityNode instance){
		innerTermFromList.add(instance); 
	}	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#addPossibleValue(unbbayes.prs.mebn.entity.Entity)
	 */
	public void addPossibleValue(Entity possibleValue){
		possibleValueList.add(possibleValue); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#removePossibleValueByName(java.lang.String)
	 */
	public void removePossibleValueByName(String possibleValue){
		
		for(Entity value : possibleValueList){
			if (value.getName().equals(possibleValue)){
				possibleValueList.remove(value);
				return; 
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#removeAllPossibleValues()
	 */
	public void removeAllPossibleValues(){
		possibleValueList.clear(); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#existsPossibleValueByName(java.lang.String)
	 */
	public boolean existsPossibleValueByName(String possibleValue){
		
		for(Entity value : possibleValueList){
			if (value.getName().compareTo(possibleValue) == 0){
				return true; 
			}
		}
		
		return false; 
	}	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getArgumentList()
	 */
	public List<Argument> getArgumentList(){
		return argumentList; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getArgumentNumber(int)
	 */
	public Argument getArgumentNumber(int argNumber){
		for(Argument argument: argumentList){
			if(argument.getArgNumber() ==  argNumber){
				return argument; 
			}
		}
		return null; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getInnerTermOfList()
	 */
	public List<MultiEntityNode> getInnerTermOfList(){
		return innerTermOfList; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getInnerTermFromList()
	 */
	public List<MultiEntityNode> getInnerTermFromList(){
		return innerTermFromList; 
	}		
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getPossibleValueList()
	 */
	public List<Entity> getPossibleValueList(){
		return possibleValueList; 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#hasPossibleValue(unbbayes.prs.mebn.entity.Entity)
	 */
	public boolean hasPossibleValue(Entity entity) {
		return possibleValueList.contains(entity);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#hasPossibleValue(java.lang.String)
	 */
	public boolean hasPossibleValue(String stateName) {
		for (Entity entity : possibleValueList) {
			if (stateName.equals(entity.getName())) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getPossibleValueIndex(java.lang.String)
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
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#hasAllOVs(unbbayes.prs.mebn.OrdinaryVariable)
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getAllOVCount()
	 */
	public int getAllOVCount() {
		Collection<OrdinaryVariable> ovs = new ArrayList<OrdinaryVariable>();
		ovs = this.fillAlreadyCountedOVs(ovs);
		return ovs.size();
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getSimpleArgRelationshipCount()
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
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMultiEntityNode#getSize()
	 */
	//by young
	public Point2D.Double getSize() {

		return size;

	}
	
}
 
