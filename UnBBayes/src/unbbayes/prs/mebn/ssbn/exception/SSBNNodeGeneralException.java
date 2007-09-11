/**
 * 
 */
package unbbayes.prs.mebn.ssbn.exception;


import java.util.ResourceBundle;

/**
 * @author shou matsumoto
 * @since September 11, 2007
 *
 */
public class SSBNNodeGeneralException extends Exception {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	

	/**
	 * 
	 */
	public SSBNNodeGeneralException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SSBNNodeGeneralException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SSBNNodeGeneralException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SSBNNodeGeneralException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
