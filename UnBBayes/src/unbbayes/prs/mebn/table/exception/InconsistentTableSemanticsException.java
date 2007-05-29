package unbbayes.prs.mebn.table.exception;

import unbbayes.prs.mebn.exception.MEBNException;

public class InconsistentTableSemanticsException extends MEBNException {
	
	
	public InconsistentTableSemanticsException(String msg) {
		super(msg);
	}
	
	public InconsistentTableSemanticsException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
