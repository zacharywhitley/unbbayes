/**
 * 
 */
package unbbayes.model.umpst.exception;

/**
 * Incompatible number of elements
 * @author Diego Marques
 */
public class IncompatibleQuantityException extends Exception {
	
	public IncompatibleQuantityException() {
		super();
	}
	
	public IncompatibleQuantityException(String msg) {
		super(msg);
	}
}
