package unbbayes.prm.model;

/**
 * Parent relationship.
 * 
 * @author David Saldaña.
 * 
 */
public class ParentRel {

	/**
	 * Child of the relationship.
	 */
	private Attribute child;
	/**
	 * Parent of the relationship.
	 */
	private Attribute parent;

	/**
	 * This attribute represents the path that the parent requires to get
	 * connected to the child.
	 */
	private Attribute[] path;

	public ParentRel(Attribute parent, Attribute children) {
		this.parent = parent;
		this.child = children;
	}

	public Attribute getChild() {
		return child;
	}

	public void setChild(Attribute children) {
		this.child = children;
	}

	public Attribute getParent() {
		return parent;
	}

	public void setParent(Attribute parent) {
		this.parent = parent;
	}

	public Attribute[] getPath() {
		return path;
	}

	public void setPath(Attribute[] path) {
		this.path = path;
	}

}
