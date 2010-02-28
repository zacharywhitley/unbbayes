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
package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.MultiEntityNode;

/**
 * This class represents the MEBN theory entity. MEBN logic treats the world as
 * being comprised of entities that have attributes and are related to other
 * entities. The logic assumes uniqueness or each concept (i.e. unique name
 * assumption), so each entity in a MEBN model has a unique identifier and no
 * unique identifier can be assigned to more than one entity.
 * 
 * @author Rommel Carvalho
 * 
 */
public abstract class Entity{
	
	protected String name;

	protected Type type;

	private List<MultiEntityNode> listIsPossibleValueOf = new ArrayList<MultiEntityNode>();

	/**
	 * Create a new Entity. 
	 * @param _container
	 * @param _name
	 * @param _type
	 */
	protected Entity(String _name, Type _type){
		name = _name; 
		type = _type; 
	}
	

	/**
	 * Returns the entity's type.
	 * 
	 * @return The entity's type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Adds the given node to the list where this entity is a possible value of.
	 * 
	 * @param node
	 *            The node to be added.
	 */
	public void addNodeToListIsPossibleValueOf(MultiEntityNode node) {
		listIsPossibleValueOf.add(node);
	}

	/**
	 * Removes the given node from the list where this entity is a possible
	 * value of.
	 * 
	 * @param node
	 *            The node to be removed.
	 */
	public void removeNodeFromListIsPossibleValueOf(MultiEntityNode node) {
		listIsPossibleValueOf.remove(node);
	}
	
	public boolean isPossibleValueOf(MultiEntityNode node) {
		return listIsPossibleValueOf.contains(node);
	}
	
	public boolean isPossibleValueOf(String nodeName) {
		for (MultiEntityNode node : listIsPossibleValueOf) {
			if (node.getName().equals(nodeName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the entity's name.
	 * @return The entity's name.
	 */
	public String getName() {
		return name;
	}
	
	public String toString(){
		return name; 
	}
	

}
