package unbbayes.prs.mebn.table.exception;

import java.util.ResourceBundle;

public class InvalidConditionantException extends
		InconsistentTableSemanticsException {

	private static final long serialVersionUID = -3141592653589793238L;
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.table.resources.Resources");

	public InvalidConditionantException() {
		super(resource.getString("InvalidConditionantFound"));
	}
	
	public InvalidConditionantException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	public InvalidConditionantException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

}
