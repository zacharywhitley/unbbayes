
package linfca.gerencia.usuario;

public class InvalidUserException extends Exception {
	
	public InvalidUserException() {
		super();		
	}

	/**
	 * Constructor for InvalidUserException.
	 * @param arg0
	 */
	public InvalidUserException(String arg0) {
		super(arg0);
	}
}
