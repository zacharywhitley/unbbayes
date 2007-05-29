package unbbayes.prs.mebn.table.exception;

import java.util.ResourceBundle;

public class InvalidProbabilityRangeException extends
		InconsistentTableSemanticsException {

	private static final long serialVersionUID = -3141592653589793238L;
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.table.resources.Resources");

	public InvalidProbabilityRangeException() {
		super(resource.getString("InvalidProbabilityRange"));
	}
	
	public InvalidProbabilityRangeException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	public InvalidProbabilityRangeException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

}
