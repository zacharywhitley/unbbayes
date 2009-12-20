/**
 * 
 */
package unbbayes.prs.mebn.compiler.exception;

import java.util.ResourceBundle;

import unbbayes.prs.mebn.exception.MEBNException;

/**
 * @author Shou Matsumoto
 *
 */
public class InstanceException extends MEBNException {
	private static final long serialVersionUID = 123456789456L;
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.prs.mebn.compiler.resources.Resources.class.getName());

	
	public InstanceException () {
		super (resource.getString("SSBNInstanceFailure"));
	}
	
	public InstanceException(String msg) {
		super(msg);
	}
	
	public InstanceException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public InstanceException(Throwable cause) {
		super(resource.getString("SSBNInstanceFailure"), cause);
	}
}
