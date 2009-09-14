package unbbayes.prs.mebn.ssbn.pruner;

import unbbayes.prs.mebn.ssbn.SSBN;

/**
 * This interface is responsible for pruning a SSBN.
 * Please, note that in order to do so, it expects
 * evidences and query nodes to be distinguishable.
 * @author Shou Matsumoto
 *
 */
public interface IPruneStructure {

	/**
	 * Prunes a SSBN.
	 * Please, note that in order to do so, it expects
	 * evidences and query nodes to be distinguishable.
	 * @param ssbn
	 */
	public void pruneStructure(SSBN ssbn); 
	
}
