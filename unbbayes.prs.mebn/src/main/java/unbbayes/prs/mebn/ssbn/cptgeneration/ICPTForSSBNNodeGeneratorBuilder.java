package unbbayes.prs.mebn.ssbn.cptgeneration;

import unbbayes.io.log.ISSBNLogManager;


/**
 * A builder for {@link CPTForSSBNNodeGenerator}
 * @author Shou Matsumoto
 *
 */
public interface ICPTForSSBNNodeGeneratorBuilder {

	/**
	 * Build method for {@link CPTForSSBNNodeGenerator}.
	 * @param logManager
	 * @return a new instance
	 */
	public CPTForSSBNNodeGenerator buildCPTForSSBNNodeGenerator(
			ISSBNLogManager logManager);

}