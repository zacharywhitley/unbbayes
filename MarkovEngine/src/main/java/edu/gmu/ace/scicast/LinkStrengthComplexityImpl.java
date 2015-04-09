package edu.gmu.ace.scicast;

/**
 * This is just the default implementation of {@link LinkStrengthComplexity}
 * @author Shou Matsumoto
 *
 */
public class LinkStrengthComplexityImpl extends LinkStrengthImpl implements LinkStrengthComplexity {

	private static final long serialVersionUID = 279382826470416663L;
	
	private float complexityFactor = Float.NaN;
	
	/**
	 * The default constructor is kept public in order to keep compatibility with java beans design pattern
	 */
	public LinkStrengthComplexityImpl() {}
	
	/**
	 * Constructor method initializing fields.
	 * @param parent
	 * @param child
	 * @param linkStrength
	 * @return a new instance of {@link LinkStrengthComplexity}
	 */
	public static LinkStrength getInstance(Long parent, Long child, float linkStrength) {
		LinkStrengthImpl ret = new LinkStrengthComplexityImpl();
		ret.setParent(parent);
		ret.setChild(child);
		ret.setLinkStrength(linkStrength);
		return ret;
	}
	
	/**
	 * Constructor method initializing fields.
	 * @param parent
	 * @param child
	 * @param linkStrength
	 * @param complexityFactor
	 * @return a new instance of {@link LinkStrengthComplexity}
	 */
	public static LinkStrengthComplexity getInstance(Long parent, Long child, float linkStrength, float complexityFactor) {
		LinkStrengthComplexityImpl ret = new LinkStrengthComplexityImpl();
		ret.setParent(parent);
		ret.setChild(child);
		ret.setLinkStrength(linkStrength);
		ret.setComplexityFactor(complexityFactor);
		return ret;
	}
	
	

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkStrengthComplexity#getComplexityFactor()
	 */
	public float getComplexityFactor() {
		return complexityFactor;
	}

	/**
	 * @param complexityFactor the complexityFactor to set
	 * @see #getComplexityFactor()
	 */
	public void setComplexityFactor(float complexityFactor) {
		this.complexityFactor = complexityFactor;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkStrengthImpl#toString()
	 */
	public String toString() {
		return getParent() + "->" + getChild() + " ; " + getLinkStrength() + " ; " + getComplexityFactor();
	}
	
	

}
