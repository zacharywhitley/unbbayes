/**
 * 
 */
package unbbayes.prs.mebn.ssbn.pruner;

import unbbayes.prs.mebn.ssbn.SSBN;

/**
 * Interface for classes which prunes an already generated SSBN.
 * Implementations of this might be combined to personalize prune algorithm.
 * @author Shou Matsumoto
 *
 */
public interface IPruner {

	/**
	 * Prunes a ssbn using this specific algorithm.
	 * Several implementations of this can be combined to build a more complex algorithm.
	 * Please, note that in order to do so, it expects
	 * evidences and query nodes to be distinguishable.
	 * @param ssbn
	 */
	public void prune(SSBN ssbn);
	
}
