/**
 * 
 */
package edu.gmu.ace.scicast;

/**
 * This is just the default implementation of {@link LinkStrength}.
 * It's a java bean, so many frameworks/libraries should be able to handle this natively.
 * @author Shou Matsumoto
 */
public class LinkStrengthImpl implements LinkStrength {

	
	private Long parent = null;
	private Long child = null;
	private float linkStrength = Float.NaN;
	
	/**
	 * The default constructor is kept public in order to keep compatibility with java beans design pattern
	 */
	public LinkStrengthImpl() {}
	
	/**
	 * Constructor method initializing fields.
	 * @param parent
	 * @param child
	 * @param linkStrength
	 * @return a new instance of {@link LinkStrength}
	 */
	public static LinkStrength getInstance(Long parent, Long child, float linkStrength) {
		LinkStrengthImpl ret = new LinkStrengthImpl();
		ret.setParent(parent);
		ret.setChild(child);
		ret.setLinkStrength(linkStrength);
		return ret;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkStrength#getParent()
	 */
	public Long getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkStrength#getChild()
	 */
	public Long getChild() {
		return child;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkStrength#getLinkStrength()
	 */
	public float getLinkStrength() {
		return linkStrength;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Long parent) {
		this.parent = parent;
	}

	/**
	 * @param child the child to set
	 */
	public void setChild(Long child) {
		this.child = child;
	}

	/**
	 * @param linkStrength the linkStrength to set
	 */
	public void setLinkStrength(float linkStrength) {
		this.linkStrength = linkStrength;
	}

}
