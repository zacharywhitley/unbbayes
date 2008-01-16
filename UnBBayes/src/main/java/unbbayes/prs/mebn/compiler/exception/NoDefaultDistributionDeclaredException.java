/**
 * 
 */
package unbbayes.prs.mebn.compiler.exception;

import java.util.ResourceBundle;

/**
 * @author Shou Matsumoto
 * 
 */
public class NoDefaultDistributionDeclaredException extends InconsistentTableSemanticsException {
	private static final long serialVersionUID = 3141592653589793238L;
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.compiler.resources.Resources");

	/**
	 * The default constructor sets it's message to NoDefaultDistributionDeclared
	 * from resource "unbbayes.prs.mebn.table.resources.Resources"
	 *
	 */
	public NoDefaultDistributionDeclaredException(){
		super(resource.getString("NoDefaultDistributionDeclared"));
	}
	
	
	public NoDefaultDistributionDeclaredException(String msg) {
		super(msg);
	}
	
	public NoDefaultDistributionDeclaredException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
