package unbbayes.util.designpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Hosts visitor classes (visitor design pattern).
 * Visitor users may extend this class.
 * This is not actually a common use of visitor pattern, but an adaptation of it.
 * @author Shou Matsumoto
 *
 */
public abstract class DefaultVisitorHost implements VisitorHost {
	private List<Visitor> visitors = new ArrayList<Visitor>();

	/* (non-Javadoc)
	 * @see unbbayes.util.designpatterns.VisitorHost#addVisitor(unbbayes.util.designpatterns.Visitor)
	 */
	public void addVisitor(Visitor visitor) {
		visitors.add(visitor);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.designpatterns.VisitorHost#clearVisitors()
	 */
	public void clearVisitors() {
		this.visitors = new ArrayList<Visitor>(); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.designpatterns.VisitorHost#visitAll()
	 */
	public void visitAll() {
		for (Visitor visitor : this.visitors) {
			visitor.visit();
		}
	}
	
	
	/**
	 * 
	 * @return a list containing all visitors hosted by this class
	 */
	public List<Visitor> getVisitors() {
		return this.visitors;
	}
}
