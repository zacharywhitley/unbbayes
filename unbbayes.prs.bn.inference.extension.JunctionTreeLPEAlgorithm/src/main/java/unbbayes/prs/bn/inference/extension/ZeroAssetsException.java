/**
 * 
 */
package unbbayes.prs.bn.inference.extension;


/**
 * This is an exception thrown mainly by {@link MarkovEngineInterface#commitNetworkActions(long)}
 * when the execution of {@link MarkovEngineInterface#addTrade(long, java.util.Date, String, long, long, java.util.List, java.util.List, java.util.List, boolean)}
 * (with allowNegative == false) has resulted in a negative asset value at the end of the transaction.
 * @author Shou Matsumoto
 *
 */
public class ZeroAssetsException extends RuntimeException {

	/**
	 * 
	 */
	public ZeroAssetsException() {
		this("Assets <= 0");
	}

	/**
	 * @param message
	 */
	public ZeroAssetsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ZeroAssetsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ZeroAssetsException(String message, Throwable cause) {
		super(message, cause);
	}

}
