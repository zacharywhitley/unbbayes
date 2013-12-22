/**
 * 
 */
package unbbayes.gui.mebn.extension.ssbn;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;

/**
 * @author Shou Matsumoto
 *
 */
public class FullSSBNGenerator extends LaskeySSBNGenerator {
	
	/**
	 * This is a default instance of {@link LaskeyAlgorithmParameters} to be used by {@link FullSSBNGenerator}.
	 * This instance basically returns "true" for everything, except for {@link LaskeyAlgorithmParameters#DO_PRUNE}
	 * (will return "false" in such case).
	 */
	public static final LaskeyAlgorithmParameters DEFAULT_ALGORITHM_PARAM = new LaskeyAlgorithmParameters() {
		public String getParameterValue(int parameter) {
			if (parameter == LaskeyAlgorithmParameters.DO_PRUNE) {
				return "false";
			}
			return "true";
		}
	};

	/**
	 * default constructor is kept protected in order to facilitate inheritance
	 */
	protected FullSSBNGenerator() {
		// initialize with default parameters
		super(DEFAULT_ALGORITHM_PARAM);
	}
	
	/**
	 * Default constructor method.
	 * @return an instance of {@link ISSBNGenerator}
	 */
	public static ISSBNGenerator getInstance() {
		return new FullSSBNGenerator();
	}

}
