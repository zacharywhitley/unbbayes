package edu.gmu.ace.scicast;


/**
 * This is virtually a java bean which stores 
 * @author Shou Matsumoto
 * @see MarkovEngineInterface#getLinkComplexitySuggestions(java.util.List, java.util.List, int, boolean, boolean)
 */
public class LinkSugestionImpl implements LinkSuggestion {

	private static final long serialVersionUID = -5901359069307053886L;
	
	private int priorComplexity = Integer.MAX_VALUE;
	private int posteriorComplexity  = Integer.MAX_VALUE;
	private Long suggestedParentId = Long.MIN_VALUE;
	private Long suggestedChildId = Long.MIN_VALUE;

	/**
	 * Default constructor is made public just to keep compatibility with beans design pattern.
	 * @see #getInstance(int, int, Long)
	 */
	public LinkSugestionImpl() { }
	
	/**
	 * Default constructor method initializing fields.
	 * @param priorComplexity
	 * @param posteriorComplexity
	 * @param suggestedParent
	 * @return a new instance
	 */
	public static LinkSuggestion getInstance(int priorComplexity, int posteriorComplexity, Long suggestedParent, Long suggestedChild) {
		LinkSugestionImpl ret = new LinkSugestionImpl();
		ret.setPriorComplexity(priorComplexity);
		ret.setPosteriorComplexity(posteriorComplexity);
		ret.setSuggestedParentId(suggestedParent);
		ret.setSuggestedChildId(suggestedChild);
		return ret;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkSuggestion#getPriorComplexity()
	 */
	public int getPriorComplexity() {
		return this.priorComplexity;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkSuggestion#getPosteriorComplexity()
	 */
	public int getPosteriorComplexity() {
		return this.posteriorComplexity;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkSuggestion#getSuggestedParentId()
	 */
	public Long getSuggestedParentId() {
		return this.suggestedParentId;
	}

	/**
	 * @param priorComplexity the priorComplexity to set
	 */
	public void setPriorComplexity(int priorComplexity) {
		this.priorComplexity = priorComplexity;
	}

	/**
	 * @param posteriorComplexity the posteriorComplexity to set
	 */
	public void setPosteriorComplexity(int posteriorComplexity) {
		this.posteriorComplexity = posteriorComplexity;
	}

	/**
	 * @param suggestedParentId the suggestedParentId to set
	 */
	public void setSuggestedParentId(Long suggestedParentId) {
		this.suggestedParentId = suggestedParentId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String ret = "Parent = " + getSuggestedParentId() + ";\n";
		ret += "Child = " + getSuggestedChildId() + ";\n";
		ret += "Prior complexity = " + getPriorComplexity() + ";\n";
		ret += "Posterior complexity = " + getPosteriorComplexity() + ";\n";
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.LinkSuggestion#getSuggestedChildId()
	 */
	public Long getSuggestedChildId() {
		return suggestedChildId;
	}

	/**
	 * @param suggestedChildId the suggestedChildId to set
	 */
	public void setSuggestedChildId(Long suggestedChildId) {
		this.suggestedChildId = suggestedChildId;
	}

}
