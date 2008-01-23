/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author Shou Matsumoto
 *
 */
public interface ISSBNGenerator {

	/**
	 * 
	 * @param query
	 * @return The SSBN generated. 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 * @throws MEBNException 
	 */
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException, 
	                                    ImplementationRestrictionException, MEBNException;

}