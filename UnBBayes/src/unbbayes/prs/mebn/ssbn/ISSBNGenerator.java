/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public interface ISSBNGenerator {
	public ProbabilisticNetwork generateSSBN(Query query);
}
