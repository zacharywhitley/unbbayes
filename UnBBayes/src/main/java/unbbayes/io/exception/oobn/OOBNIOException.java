/**
 * 
 */
package unbbayes.io.exception.oobn;

import java.io.IOException;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNIOException extends IOException {

	/**
	 * 
	 */
	public OOBNIOException() {
		super();
	}

	/**
	 * @param s
	 */
	public OOBNIOException(String s) {
		super(s);
	}
	
	/**
	 * 
	 */
	public OOBNIOException(Throwable t) {
		super(t.getMessage());
		this.setStackTrace(t.getStackTrace());
	}

	/**
	 * @param s
	 */
	public OOBNIOException(String s, Throwable t) {
		super(s);
		this.setStackTrace(t.getStackTrace());
	}
	

}
