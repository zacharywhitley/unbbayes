package unbbayes.util.designpatterns;

/**
 * This is not actually a common use of visitor pattern, but an adaptation of it.
 * @author Shou Matsumoto
 *
 */
public interface Visitor {
	/**
	 * executes the method managed by visitor pattern
	 */
	public void visit();
}
