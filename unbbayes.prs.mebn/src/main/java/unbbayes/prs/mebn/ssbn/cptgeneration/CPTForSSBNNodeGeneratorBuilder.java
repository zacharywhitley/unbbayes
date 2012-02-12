/**
 * 
 */
package unbbayes.prs.mebn.ssbn.cptgeneration;

import unbbayes.io.log.ISSBNLogManager;

/**
 * Default implementation of {@link ICPTForSSBNNodeGeneratorBuilder}
 * @author Shou Matsumoto
 *
 */
public class CPTForSSBNNodeGeneratorBuilder implements ICPTForSSBNNodeGeneratorBuilder {

	/**
	 * Default constructor must remain public
	 */
	public CPTForSSBNNodeGeneratorBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.gmu.seor.prognos.unbbayesplugin.continuous.ssbn.IHybridCPTForSSBNNodeGeneratorBuilder#buildCPTForSSBNNodeGenerator(unbbayes.io.log.ISSBNLogManager)
	 */
	public CPTForSSBNNodeGenerator buildCPTForSSBNNodeGenerator(ISSBNLogManager logManager) {
		return new CPTForSSBNNodeGenerator(logManager);
	}
}
