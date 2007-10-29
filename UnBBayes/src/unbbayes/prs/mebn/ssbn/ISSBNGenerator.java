/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author Shou Matsumoto
 *
 */
public interface ISSBNGenerator {
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException;
}
