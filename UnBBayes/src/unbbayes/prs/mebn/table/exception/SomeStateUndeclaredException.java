package unbbayes.prs.mebn.table.exception;

import java.util.ResourceBundle;

public class SomeStateUndeclaredException extends
		InconsistentTableSemanticsException {

	private static final long serialVersionUID = -3141592653589793238L;
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.table.resources.Resources");

	public SomeStateUndeclaredException() {
		super(resource.getString("SomeStateUndeclared"));
	}
	
	public SomeStateUndeclaredException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	public SomeStateUndeclaredException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

}
