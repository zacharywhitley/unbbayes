package unbbayes.prs.mebn.compiler.exception;

import java.util.ResourceBundle;

import unbbayes.prs.mebn.exception.MEBNException;

public class TableFunctionMalformedException extends MEBNException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4621101714330356143L;

	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.compiler.resources.Resources");

	
	public TableFunctionMalformedException() {
		super(resource.getString("UnexpectedTokenFound"));
	}
	
	public TableFunctionMalformedException(String msg) {
		super(msg);
	}

}
