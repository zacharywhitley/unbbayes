/**
 * 
 */
package unbbbayes.prs.mebn.ssbn.extension.ssmsbn;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSMSBNGenerator;

/**
 * This class uses builder pattern to instanciate an algorithm for multiple
 * sectioned situation specific bayesian network
 * 
 * @author rafaelmezzomo
 * @author estevaoaguiar
 */
public class SSMSBNAlgorithmBuilder implements ISSBNGeneratorBuilder {

	private String name;

	/**
	 * Public default constructor must be public. Our plugin framework requires it.
	 */
	public SSMSBNAlgorithmBuilder() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#buildSSBNGenerator
	 * ()
	 */
	public ISSBNGenerator buildSSBNGenerator() throws InstantiationException {

		return LaskeySSMSBNGenerator.newInstance();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#setName(java.lang
	 * .String)
	 */
	public void setName(String name) {
		this.name = name;
	}

}
