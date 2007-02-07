package unbbayes.prs.mebn.table.exception;

import unbbayes.prs.mebn.exception.MEBNException;

public class ProbabilityFunctionEvalException extends MEBNException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6920907903169151465L;

	public ProbabilityFunctionEvalException(String msg) {
		super(msg);
	}
	
	public ProbabilityFunctionEvalException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
