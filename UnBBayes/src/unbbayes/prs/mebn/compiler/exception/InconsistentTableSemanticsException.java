package unbbayes.prs.mebn.compiler.exception;

import java.util.ResourceBundle;

import unbbayes.prs.mebn.exception.MEBNException;

public class InconsistentTableSemanticsException extends MEBNException {
	
	private static final long serialVersionUID = 346643383279L;
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.compiler.resources.Resources");

	
	public InconsistentTableSemanticsException () {
		super (resource.getString("UnexpectedTokenFound"));
	}
	
	public InconsistentTableSemanticsException(String msg) {
		super(msg);
	}
	
	public InconsistentTableSemanticsException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
