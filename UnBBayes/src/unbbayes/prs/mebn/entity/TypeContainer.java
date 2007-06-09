package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeIsInUseException;

/**
 * Container for the types of a MEBN. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 06/03/07
 *
 */
public class TypeContainer {

	/* List of types in the MEBN */
	
	private Set<Type> listOfTypes;
	
	// Below we have the default types allowed by the MEBN logic and
	// definition of PR-OWL.
	
	public static Type typeBoolean; 
	public static Type typeCategoryLabel; 
	public static Type typeLabel; 
	
	public TypeContainer(){
		
		listOfTypes = new TreeSet<Type>(); 
		
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
	public void removeType(String oldType) throws TypeDoesNotExistException, TypeIsInUseException {
		
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
	
	/**
	 * Create a type with the name specified and add this to the
	 * list of types. 
	 * @param nameType Name of the type that will be create
	 * @return The new Type
	 * @throws TypeAlreadyExistsException Type don't can be create because it already exists. 
	 */
	public Type createType(String nameType) throws TypeAlreadyExistsException{
		
		Type newType = new Type(nameType, this); 
		listOfTypes.add(newType); 
		return newType; 
	}
	
	/**
	 * Remove the type. 
	 * 
	 * @param type
	 * @throws TypeDoesNotExistException
	 */
	public void removeType(Type type) throws TypeDoesNotExistException{
		
		if(listOfTypes.contains(type)){
		   
		   listOfTypes.remove(type);
		   
		}
		else{
			
			 throw new TypeDoesNotExistException(); 
		
		}
	
	}
	
	/**
	 * Verify if the type already exists.
	 * @param aType The type to be verified.
	 * @return True if the type exists and false otherwise.
	 */
	
	public boolean hasType(Type type){
		
		return listOfTypes.contains(type); 
		
	}
	
	public static Type getDefaultType(){
		return typeCategoryLabel; 
	}
	
	public Set<Type> getListOfTypes(){
		return listOfTypes; 
	}
	
	/**
	 * Return a list with the names of the types. 
	 */
	public List<String> getTypesNames(){
		
		ArrayList<String> list = new ArrayList<String>(); 
		for(Type type: listOfTypes){
			list.add(type.getName()); 
		}
		
		return list; 
	}
	
	public String getLabelSuffix(){
		
		return "_label"; 
		
	}
	
	/**
	 * Verify if the type with the name exists and return it.
	 * @param aType The type to be search.
	 * @return the type if it exists or null otherwise.
	 */
	public Type getType(String aType) {
		
		for(Type type: listOfTypes){
			 
			 if(type.getName().equals(aType)){
				 return type; 
			 }
		}
		
		return null; 
		 
	}
}
