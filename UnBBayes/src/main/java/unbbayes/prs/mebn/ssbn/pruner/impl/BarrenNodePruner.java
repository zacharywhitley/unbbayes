/**
 * 
 */
package unbbayes.prs.mebn.ssbn.pruner.impl;

import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;

/**
 * @author Shou Matsumoto
 * Prunes a node using barren node concept.
 * A barren node is a node which has no query or finding node as its descendant.
 */
public class BarrenNodePruner implements IPruner {

	/**
	 * default constructor is made protected in order to make it easier to extend
	 */
	protected BarrenNodePruner() {
		super();
	}
	
	/**
	 * Default construction method
	 * @return
	 */
	public static BarrenNodePruner newInstance() {
		return new BarrenNodePruner();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.pruner.IPruner#prune(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	public void prune(SSBN ssbn) {
		throw new java.lang.IllegalStateException("Not yet implemented");
	}

}
