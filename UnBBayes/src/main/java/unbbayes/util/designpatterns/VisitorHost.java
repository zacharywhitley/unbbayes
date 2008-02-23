package unbbayes.util.designpatterns;

/**
 * Hosts visitor classes (visitor design pattern).
 * All visitor users should implement this class.
 * This is not actually a common use of visitor pattern, but an adaptation of it.
 * @author Shou Matsumoto
 *
 */
public interface VisitorHost {
	/**
	 * Deletes every visitors hosted by the class implementing this method.
	 */
	public void clearVisitors();
	
	/**
	 * Registers a visitor to be run by visitAll
	 * @param visitor a visitor to add.
	 * @see unbbayes.util.designpatterns.VisitorHost#visitAll()
	 */
	public void addVisitor(Visitor visitor);
	
	/**
	 * Execute all visit method of the visitors registered by the class implementing this method.
	 */
	public void visitAll();
	
	
}
