/**
 * 
 */
package unbbayes.prs.oobn.exception;

import java.util.ResourceBundle;

/**
 * @author Shou Matsumoto
 *
 */
public class DuplicateOOBNClassException extends OOBNException {

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.prs.oobn.resources.Resources.class.getName());  		
	
	
	/**
	 * Exception thrown when there are repeating classes.
	 */
	public DuplicateOOBNClassException() {
		super(resource.getString("DuplicateOOBNClassExceptionMessage"));
	}

	/**
	 * @param message
	 */
	public DuplicateOOBNClassException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DuplicateOOBNClassException(Throwable cause) {
		super(resource.getString("DuplicateOOBNClassExceptionMessage"),cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateOOBNClassException(String message, Throwable cause) {
		super(message, cause);
	}

}
