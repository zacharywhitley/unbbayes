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
	
	/* List of types in the MEBN */
	
	private static Set<Type> listOfTypes = new TreeSet<Type>();
	
	// Below we have the default types allowed by the MEBN logic and
	// definition of PR-OWL.
	
	public static Type typeBoolean; 
	public static Type typeCategoryLabel; 
	public static Type typeLabel; 
	
	static {
		try{
			typeBoolean = new Type("Boolean"); 
			typeCategoryLabel = new Type("CategoryLabel"); 
			typeLabel = new Type("TypeLabel"); 
			
			listOfTypes.add(typeBoolean);
			listOfTypes.add(typeCategoryLabel);
			listOfTypes.add(typeLabel);               
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
	}
	
	
	
	/*-------------------------- Methods for the objets Type --------------------------------------*/
	
    private String name;
	
	private List<Object> isTypeOfList; 
	
	/**
	 * Adds a new type to the list of possible entity's type.
	 * 
	 * @param newType
	 *            The new type to be added.
	 * @throws TypeAlreadyExistsException
	 *             Thrown if the new type already exists, in other words, if
	 *             there is a type with the same name already.
	 */
	
	private Type(String newType) throws TypeAlreadyExistsException {
		
		name = newType; 
		isTypeOfList = new ArrayList<Object>(); 
		
		if (Type.getType(newType) != null){
			throw new TypeAlreadyExistsException(); 
		}
		
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
	

	/*-------------------------- Methods statics for the set of Types --------------------------------------*/
	
	/**
	 * Removes an old type from the list of possible entity's type.
	 * 
	 * @param oldType
	 *            The old type to be removed.
	 * @throws TypeIsInUseException 
	 *             Thrown if the type don't can be removed because it is
	 *             in use for some object
	 * @throws TypeDoesNotExistException
	 *             Thrown if the old type does not exist, in other words, if
	 *             there is no type with the same name given.
	 */
	public static void removeType(String oldType) throws TypeDoesNotExistException, TypeIsInUseException {
		
		 for(Type type: listOfTypes){
			 
			 if(type.getName() == oldType){
				 type.delete(); 
				 listOfTypes.remove(type); 
			 }
			 else{
				 throw new TypeDoesNotExistException(); 		 
			 }
			 
		 }
		 
	}
	
	public static Type createType(String nameType) throws TypeAlreadyExistsException{
		
		Type newType = new Type(nameType); 
		listOfTypes.add(newType); 
		return newType; 
	}
	
	public static void removeType(Type type) throws TypeDoesNotExistException{
		
		if(listOfTypes.contains(type)){
		   listOfTypes.remove(type);
		}
		else{
			 throw new TypeDoesNotExistException(); 
		}
	
	}
	
	/**
	 * Verify if the type with the name exists and return it.
	 * @param aType The type to be search.
	 * @return the type if it exists or null otherwise.
	 */
	public static Type getType(String aType) {
		
		for(Type type: listOfTypes){
			 
			 if(type.getName() == aType){
				 return type; 
			 }
		}
		
		return null; 
		 
	}
	
	/**
	 * Verify if the type already exists.
	 * @param aType The type to be verified.
	 * @return True if the type exists and false otherwise.
	 */
	
	public static boolean hasType(Type type){
		
		return listOfTypes.contains(type); 
		
	}
	
	public static Type getDefaultType(){
		return typeCategoryLabel; 
	}
	
	public static Set<Type> getListOfTypes(){
		return listOfTypes; 
	}
	
}
