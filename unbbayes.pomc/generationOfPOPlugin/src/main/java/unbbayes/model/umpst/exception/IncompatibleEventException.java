/**
 * 
 */
package unbbayes.model.umpst.exception;

/**
 * The event does not match to the possible values.
 * @author Diego Marques
 */
public class IncompatibleEventException extends Exception {
	
	public IncompatibleEventException() {
		super();
	}
	
	public IncompatibleEventException(String msg) {
		super(msg);
	}
}
