/**
 * 
 */
package unbbayes.prs.medg.ssid;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;

/**
 * This is a builder for {@link StandaloneSSIDGenerator}
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
		
		ISSBNGenerator ssbngenerator = SSIDGenerator.getInstance();
		
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
