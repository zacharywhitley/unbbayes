package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;


public interface IBuilderStructure {

	/**
	 * 
	 * @param ssbn Contains a SSBN object with the queries and the findings into 
	 *             the node list. 
	 * @return
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	public void buildStructure(SSBN ssbn) throws ImplementationRestrictionException, 
	                                             SSBNNodeGeneralException; 
	
}
