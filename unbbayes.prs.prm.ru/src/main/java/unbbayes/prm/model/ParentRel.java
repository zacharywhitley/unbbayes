package unbbayes.prm.model;

/**
 * Parent relationship.
 * 
 * @author David Saldaña.
 * 
 */
public class ParentRel {

	private Attribute child;
	private Attribute parent;

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

}
