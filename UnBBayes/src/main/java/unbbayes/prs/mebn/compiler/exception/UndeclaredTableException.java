package unbbayes.prs.mebn.compiler.exception;

import java.util.ResourceBundle;

import unbbayes.prs.mebn.exception.MEBNException;

public class UndeclaredTableException extends MEBNException {

	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.compiler.resources.Resources");

	
	public UndeclaredTableException(String e) {
		super(e);
	}

	public UndeclaredTableException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UndeclaredTableException() {
		super(resource.getString("TableUndeclared"));
	}

	public UndeclaredTableException(Exception e) {
		super(e);
	}

}
