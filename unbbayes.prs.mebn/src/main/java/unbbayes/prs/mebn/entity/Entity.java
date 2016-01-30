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
import java.util.regex.Pattern;

import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.util.Debug;

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
	
	/** Default name pattern for {@link INameChecker#isValidName(String)} */
	public static final Pattern DEFAULT_INSTANCE_NAME_PATTERN = Pattern.compile("[a-zA-Z_0-9]*");
	
	private List<INameChecker> instanceNameCheckChainOfResponsibility = new ArrayList<Entity.INameChecker>();
	
	/**
	 * Create a new Entity. 
	 * @param _container
	 * @param _name
	 * @param _type
	 */
	protected Entity(String _name, Type _type){
		name = _name; 
		type = _type; 
		this.getInstanceNameCheckChainOfResponsibility().add(new INameChecker() {
			public boolean isValidName(String name) {
				if (name == null || name.trim().length() <= 0) {
					// do now allow null or empty names
					return false;
				}
				return DEFAULT_INSTANCE_NAME_PATTERN.matcher(name).matches();
			}
		});
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
	public void removeNodeFromListIsPossibleValueOf(IMultiEntityNode node) {
		listIsPossibleValueOf.remove(node);
	}
	
	public boolean isPossibleValueOf(IMultiEntityNode node) {
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
	
	/**
	 * @param instanceName : name to check validity
	 * @return true if the given instanceName matches {@link #getInstanceNamePattern()}.
	 * False otherwise.
	 * @see #getInstanceNameCheckChainOfResponsibility()
	 */
	public boolean isValidInstanceName (String instanceName) {
		try {
			// do boolean AND operation (i.e. return false on first occurrence of "false")
			for (INameChecker check : this.getInstanceNameCheckChainOfResponsibility()) {
				if (!check.isValidName(instanceName)) {
					return false;
				}
			}
			// all check returned true
			// Note: if the chain of responsibility is empty, no check is done, and it will return true.
			return true;
		} catch (Exception e) {
			// exception is considered as a "false"
			Debug.println(getClass(), e.getMessage(), e);
		}
		return false;
	}


	

	/**
	 * Objects implementing this interface will adhere to
	 * chain of responsibility design pattern in order for {@link #isValidName()}
	 * to check whether an instance name is valid or not.
	 */
	public interface INameChecker {
		/**
		 * @return true if the name is valid
		 */
		public boolean isValidName(String name);
	}


	/**
	 * The content of this list will be executed in {@link #isValidInstanceName(String)}
	 * in order to check whether a name is valid as an instance of this entity.
	 * If this list contains more than 1 object, they will be aggregated with boolean AND
	 * operation.
	 * @return the instanceNameCheckChainOfResponsibility
	 */
	public List<INameChecker> getInstanceNameCheckChainOfResponsibility() {
		return instanceNameCheckChainOfResponsibility;
	}


	/**
	 * The content of this list will be executed in {@link #isValidInstanceName(String)}
	 * in order to check whether a name is valid as an instance of this entity.
	 * If this list contains more than 1 object, they will be aggregated with boolean AND
	 * operation.
	 * @param instanceNameCheckChainOfResponsibility the instanceNameCheckChainOfResponsibility to set
	 */
	public void setInstanceNameCheckChainOfResponsibility(
			List<INameChecker> instanceNameCheckerChainOfResponsibility) {
		this.instanceNameCheckChainOfResponsibility = instanceNameCheckerChainOfResponsibility;
	}
}
