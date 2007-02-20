package unbbayes.prs.mebn.entity;

import java.util.Set;
import java.util.TreeSet;

import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;

/**
 * This class represents the possible type for an Entity.
 * 
 * @author Rommel Carvalho
 * 
 */
public class Type {

	private static Set<String> listOfTypes = new TreeSet<String>();
	
	// Below we have the default types allowed by the MEBN logic and
	// definition of PR-OWL.
	static {
		listOfTypes.add("Boolean");
		listOfTypes.add("CategoryLabel");
		// TODO make sure the TypeLabel is necessary!
		listOfTypes.add("TypeLabel");
	}

	/**
	 * Adds a new type to the list of possible entity's type.
	 * 
	 * @param newType
	 *            The new type to be added.
	 * @throws TypeAlreadyExistsException
	 *             Thrown if the new type already exists, in other words, if
	 *             there is a type with the same name already.
	 */
	public static void addType(String newType)
			throws TypeAlreadyExistsException {
		if (!listOfTypes.add(newType))
			throw new TypeAlreadyExistsException("The type " + newType
					+ " already exists. Please try again.");
	}

	/**
	 * Remvoes an old type from the list of possible entity's type.
	 * 
	 * @param oldType
	 *            The old type to be removed.
	 * @throws TypeDoesNotExistException 
	 * @throws TypeDoesNotExistException
	 *             Thrown if the old type does not exist, in other words, if
	 *             there is no type with the same name given.
	 */
	public static void removeType(String oldType)
			throws TypeDoesNotExistException {
		if (!listOfTypes.remove(oldType))
			throw new TypeDoesNotExistException("The type " + oldType
					+ " does not exist. Please try again.");
	}

	/**
	 * Verify if the type already exists.
	 * @param aType The type to be verified.
	 * @return True if the type exists and false otherwise.
	 */
	public static boolean hasType(String aType) {
		return listOfTypes.contains(aType);
	}
	
	public static Set<String> getListOfTypes(){
		return listOfTypes; 
	}

}
