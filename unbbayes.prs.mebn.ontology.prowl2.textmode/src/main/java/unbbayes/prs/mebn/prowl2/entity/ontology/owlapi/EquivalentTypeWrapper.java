/**
 * 
 */
package unbbayes.prs.mebn.prowl2.entity.ontology.owlapi;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeIsInUseException;
import unbbayes.util.Debug;

/**
 * This is a wrapper of multiple instances of equivalent {@link Type} in terms of {@link Object#equals(Object)}.
 * This wrapper is necessary because the GUI (e.g., {@link unbbayes.gui.mebn.finding.FindingArgumentPane}) 
 * uses {@link Object#equals(Object)} to check
 * if an {@link unbbayes.prs.mebn.entity.ObjectEntityInstance} can be displayed in the dropdown list
 * as a possible value for the ordinary variable in the argument of a node.
 * @author Shou Matsumoto
 *
 */
public class EquivalentTypeWrapper extends Type {

	private Collection<Type> equivalentTypes;
	private Type wrappedType;

	


	/**
	 * @param wrappedType : type that this wrapper wraps
	 * @param container : see {@link Type}
	 * @param equivalentTypes : collection of other types to be considered as equivalent (in terms of {@link Object#equals(Object)})
	 * to this type.
	 * @throws TypeAlreadyExistsException
	 */
	public EquivalentTypeWrapper(String newName, Type wrappedType, TypeContainer container, Collection<Type> equivalentTypes) throws TypeAlreadyExistsException {
		super(newName, container);
		this.wrappedType = wrappedType;
		this.equivalentTypes = equivalentTypes;
	}


	/**
	 * @return the equivalentTypes
	 */
	public Collection<Type> getEquivalentTypes() {
		if (equivalentTypes == null) {
			equivalentTypes = new HashSet<Type>();
		}
		return equivalentTypes;
	}



	/**
	 * @param equivalentTypes the equivalentTypes to set
	 */
	public void setEquivalentTypes(Collection<Type> equivalentTypes) {
		this.equivalentTypes = equivalentTypes;
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.Type#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		try {
			boolean isEqual =  getWrappedType().equals(obj);
			if (isEqual) {
				return true;
			}
			// check for equivalent alternatives
			for (Type equivalent : getEquivalentTypes()) {
				if (equivalent == null) {
					continue;
				}
				if (equivalent.equals(obj)) {
					return true;
				}
			}
			// no equivalent was found in the list too
		} catch (Exception e) {
			Debug.println(getClass(), "Failed to compare this type " + this + " with " + obj, e);
		}
		
		// also check if type is reachable from the other end
		if ((obj instanceof EquivalentTypeWrapper)
				&& ((EquivalentTypeWrapper)obj).equals(this)) {
			return true;
		}
		
		return super.equals(obj);
	}



	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return wrappedType.hashCode();
	}


	/**
	 * @return
	 * @see unbbayes.prs.mebn.entity.Type#getName()
	 */
	public String getName() {
		return wrappedType.getName();
	}


	/**
	 * @param user
	 * @see unbbayes.prs.mebn.entity.Type#addUserObject(java.lang.Object)
	 */
	public void addUserObject(Object user) {
		wrappedType.addUserObject(user);
	}


	/**
	 * @param user
	 * @see unbbayes.prs.mebn.entity.Type#removeUserObject(java.lang.Object)
	 */
	public void removeUserObject(Object user) {
		wrappedType.removeUserObject(user);
	}


	/**
	 * @return
	 * @see unbbayes.prs.mebn.entity.Type#typeIsUsed()
	 */
	public boolean typeIsUsed() {
		return wrappedType.typeIsUsed();
	}


	/**
	 * @throws TypeIsInUseException
	 * @see unbbayes.prs.mebn.entity.Type#delete()
	 */
	public void delete() throws TypeIsInUseException {
		wrappedType.delete();
	}


	/**
	 * @return
	 * @see unbbayes.prs.mebn.entity.Type#toString()
	 */
	public String toString() {
		String ret = wrappedType.toString();
		ret += " (\t";
		for (Type type : getEquivalentTypes()) {
			ret += type.toString() + "\t";
		}
		ret += ") ";
		return ret;
	}


	/**
	 * @param anotherType
	 * @return
	 * @see unbbayes.prs.mebn.entity.Type#compareTo(unbbayes.prs.mebn.entity.Type)
	 */
	public int compareTo(Type anotherType) {
		return wrappedType.compareTo(anotherType);
	}


	/**
	 * @param name
	 * @throws TypeAlreadyExistsException
	 * @see unbbayes.prs.mebn.entity.Type#renameType(java.lang.String)
	 */
	public void renameType(String name) throws TypeAlreadyExistsException {
		wrappedType.renameType(name);
	}


	/**
	 * @return
	 * @see unbbayes.prs.mebn.entity.Type#getIsTypeOfList()
	 */
	public List<Object> getIsTypeOfList() {
		return wrappedType.getIsTypeOfList();
	}


	/**
	 * @param isTypeOfList
	 * @see unbbayes.prs.mebn.entity.Type#setIsTypeOfList(java.util.List)
	 */
	public void setIsTypeOfList(List<Object> isTypeOfList) {
		wrappedType.setIsTypeOfList(isTypeOfList);
	}


	/**
	 * @return
	 * @see unbbayes.prs.mebn.entity.Type#hasOrder()
	 */
	public boolean hasOrder() {
		return wrappedType.hasOrder();
	}


	/**
	 * @param hasOrder
	 * @see unbbayes.prs.mebn.entity.Type#setHasOrder(boolean)
	 */
	public void setHasOrder(boolean hasOrder) {
		wrappedType.setHasOrder(hasOrder);
	}


	/**
	 * @return the wrappedType
	 */
	public Type getWrappedType() {
		return wrappedType;
	}


	/**
	 * @param wrappedType the wrappedType to set
	 */
	public void setWrappedType(Type wrappedType) {
		this.wrappedType = wrappedType;
	}
	
	public void addEquivalentType(Type type) {
		this.getEquivalentTypes().add(type);
	}
	
	public void removeEquivalentType(Type type) {
		this.getEquivalentTypes().remove(type);
	}
	
	public void clearEquivalentType() {
		getEquivalentTypes().clear();
	}
	

}
