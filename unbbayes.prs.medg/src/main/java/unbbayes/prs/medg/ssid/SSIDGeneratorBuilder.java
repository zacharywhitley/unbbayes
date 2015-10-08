/**
 * 
 */
package unbbayes.prs.medg.ssid;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;

/**
 * This is a builder for {@link SSIDGenerator}
 * @author Shou Matsumoto
 *
 */
public class SSIDGeneratorBuilder implements ISSBNGeneratorBuilder {

	private String name = "SSID generator";

	/**
	 * Default constructor is required by the plugin architecture
	 */
	public SSIDGeneratorBuilder() {
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
	    
		ISSBNGenerator ssbngenerator = new SSIDGenerator(parameters);
		
		// change settings of ssbngenerator if you want to
		
		return ssbngenerator;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

}
