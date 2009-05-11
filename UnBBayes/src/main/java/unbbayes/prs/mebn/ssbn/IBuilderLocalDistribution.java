package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;


public interface IBuilderLocalDistribution {

	/**
	 * 
	 * @param ssbn Contains a SSBN object with the queries and the findings into 
	 *             the node list. 
	 * @param kb
	 * @return
	 * @throws SSBNNodeGeneralException 
	 * @throws MEBNException 
	 */
	public void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException; 
	
}
