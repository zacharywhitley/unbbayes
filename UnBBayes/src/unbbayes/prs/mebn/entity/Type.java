package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeIsInUseException;

/**
 * This class represents the possible type for an Entity.
 * 
 * @author Rommel Carvalho
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (05/17/07)
 */
public class Type implements Comparable<Type>{
	
	/*-------------------------- Methods for the objets Type --------------------------------------*/
	
    private String name;
	private List<Object> isTypeOfList; 
	private TypeContainer container; 
	
	private boolean hasOrder = false;	// TODO verify the correct location of ordering checker
	
	/**
	 * Adds a new type to the list of possible entity's type.
	 * 
	 * @param newType
	 *            The new type to be added.
	 * @throws TypeAlreadyExistsException
	 *             Thrown if the new type already exists, in other words, if
	 *             there is a type with the same name already.
	 */
	
	protected Type(String newType, TypeContainer container) throws TypeAlreadyExistsException {
		
		name = newType; 
		isTypeOfList = new ArrayList<Object>(); 
		this.container = container; 
		
		if (container.getType(newType) != null){
			throw new TypeAlreadyExistsException(); 
		}
		hasOrder = false;
	}
	
	/**
	 * Adds a new type to the list of possible entity's type. This method
	 * don't verify if already have a type with the same name. 
	 * 
	 * @param newType
	 *            The new type to be added.
	 */
	
	protected Type(String newType){
		
		name = newType; 
		isTypeOfList = new ArrayList<Object>(); 
		hasOrder = false;
	}
	
	public String getName(){
		return name; 
	}
	
	/**
	 * Add a user object for the Type. A user object is a object that 
	 * have the type how your type. One type can't be removed if it has
	 * user objects in your list of user objects. 
	 * 
	 * @param user
	 */
	public void addUserObject(Object user){
		isTypeOfList.add(user); 
	}
	
	public void removeUserObject(Object user){
		isTypeOfList.remove(user); 
	}
	
	public boolean typeIsUsed(){
		return !isTypeOfList.isEmpty(); 
	}
	
	public void delete() throws TypeIsInUseException{
		
		if (!isTypeOfList.isEmpty()){
			throw new TypeIsInUseException(); 
		}
		
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Type){
			if (((Type)obj).getName() == this.name) return true;
			else return false; 
		}
		else return false; 
	}
	
	public String toString(){
		return name; 
	}
	
	public int compareTo(Type anotherType) {
		return name.compareTo(anotherType.getName()); 
	}
	
	/**
	 * Rename the type. 
	 * 
	 * @param name The new name
	 * @throws TypeAlreadyExistsException The name can't be turned because
	 * 						              already have an type with the new name. 
	 */
	public void renameType(String name) throws TypeAlreadyExistsException{
		
		if (container.getType(name) != null){
			throw new TypeAlreadyExistsException(); 
		}
		else{
		    this.name = name;
		}
	
	}

	public List<Object> getIsTypeOfList() {
		return isTypeOfList;
	}

	public void setIsTypeOfList(List<Object> isTypeOfList) {
		this.isTypeOfList = isTypeOfList;
	}

	/**
	 * @return the hasOrder
	 */
	public boolean hasOrder() {
		return hasOrder;
	}

	/**
	 * @param hasOrder the hasOrder to set
	 */
	public void setHasOrder(boolean hasOrder) {
		this.hasOrder = hasOrder;
	}
	
	
	
}
