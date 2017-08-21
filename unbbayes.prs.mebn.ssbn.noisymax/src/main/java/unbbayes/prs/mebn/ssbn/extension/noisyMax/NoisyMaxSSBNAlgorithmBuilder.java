/**
 * 
 */
package unbbayes.prs.mebn.ssbn.extension.noisyMax;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;

/**
 * @author Shou Matsumoto
 *
 */
public class NoisyMaxSSBNAlgorithmBuilder implements ISSBNGeneratorBuilder {

	private String name = "SSBN with Noisy MAX";

	/**
	 * 
	 */
	public NoisyMaxSSBNAlgorithmBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#buildSSBNGenerator()
	 */
	public ISSBNGenerator buildSSBNGenerator() throws InstantiationException {
		
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
	    
		ISSBNGenerator ssbngenerator = new NoisyMaxSSBNAlgorithm(parameters);
		
		return ssbngenerator;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

}
