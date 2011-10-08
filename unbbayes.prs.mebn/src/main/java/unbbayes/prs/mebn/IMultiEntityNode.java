package unbbayes.prs.mebn;

import java.awt.geom.Point2D;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.Entity;

public interface IMultiEntityNode {

	/**
	 * It returns the node's type. Not used for this node.
	 * @see Node#getType()
	 */
	// Please, avoid using @Override annotation, since it makes interface extraction (refactor) very difficult,
	// because it supposes a inherited method is declared always inside a class, 
	// and fails if it becomes declared inside an interface.
	//	@Override
	public abstract int getType();

	public abstract MFrag getMFrag();

	/**
	 * Method responsible for removing this node from its MFrag.
	 *
	 */
	public abstract void removeFromMFrag();

	public abstract void addArgument(Argument arg);

	public abstract void removeArgument(Argument arg);

	public abstract void addInnerTermOfList(MultiEntityNode instance);

	public abstract void addInnerTermFromList(MultiEntityNode instance);

	public abstract void addPossibleValue(Entity possibleValue);

	/**
	 * Remove the possible value with the name 
	 * @param possibleValue name of the possible value
	 */
	public abstract void removePossibleValueByName(String possibleValue);

	/**
	 * Remove all possible values of the node
	 */
	public abstract void removeAllPossibleValues();

	/**
	 * Verifies if the possible value is on the list of possible values
	 * of the node. 
	 * @param possibleValue name of the possible value
	 * @return true if it is present or false otherside
	 */
	public abstract boolean existsPossibleValueByName(String possibleValue);

	public abstract List<Argument> getArgumentList();

	/**
	 * @param argNumber Number of the argument to be recover
	 * @return the argument or null if don't have a argument with the specific number
	 */
	public abstract Argument getArgumentNumber(int argNumber);

	public abstract List<MultiEntityNode> getInnerTermOfList();

	public abstract List<MultiEntityNode> getInnerTermFromList();

	/**
	 * !!!Maybe dead code!!!
	 * It should be avoided. Use DomainResidentNode.getPossibleValueLinkList
	 * whenever possible.
	 * @return a list which elements were added by MultiEntityNode.addPossibleValue(value)... It
	 * obviously doesn't contain entity instances (since they are dead codes)
	 */
	public abstract List<Entity> getPossibleValueList();

	/**
	 * Verify if the entity is a state of the node 
	 * Warning: the search will be for the entity and not for the
	 * name of entity.
	 * @param entity The entity 
	 * @return true if the entity is a state, false otherside
	 */
	public abstract boolean hasPossibleValue(Entity entity);

	/**
	 * Verify if the node has a state with the name
	 * @param stateName Name of state
	 * @return true if have a state with the name, false otherside
	 */
	public abstract boolean hasPossibleValue(String stateName);

	/**
	 * This method is responsible for returning the index where this state is 
	 * located.
	 * @param stateName State's name desired to know the index.
	 * @return Returns the state position, index. -1 if the state is not found.
	 */
	public abstract int getPossibleValueIndex(String stateName);

	/**
	 * 
	 * @param ovs Set of OrdinaryVariables to be searched inside its arguments.
	 * @return True if all OVs within "ovs" are used as argument. False otherwise.
	 */
	public abstract boolean hasAllOVs(OrdinaryVariable... ovs);

	/**
	 * 
	 * @return how many different ovs appear inside this node, including inner nodes.
	 */
	public abstract int getAllOVCount();

	/**
	 * Counts how many simple arg relationships are present within ArgumentList
	 * @return
	 */
	public abstract int getSimpleArgRelationshipCount();

	/**
	 * Returns the node's size (x,y) where x = width and y = height.
	 * 
	 * @return The node's size.
	 */
	//by young
	public abstract Point2D.Double getSize();

}