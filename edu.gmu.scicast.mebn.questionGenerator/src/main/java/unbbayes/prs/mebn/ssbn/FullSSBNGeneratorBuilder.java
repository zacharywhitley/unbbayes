/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;

/**
 * This builder generates a {@link ISSBNGenerator} that does not 
 * cut SSBN nodes.
 * @author Shou Matsumoto
 */
public class FullSSBNGeneratorBuilder implements ISSBNGeneratorBuilder {

	/**
	 * Default constructor with no parameters must be public,
	 * so that the plugin infrastructure works.
	 */
	public FullSSBNGeneratorBuilder() {}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGeneratorBuilder#buildSSBNGenerator()
	 */
	public ISSBNGenerator buildSSBNGenerator() throws InstantiationException {
		return FullSSBNGenerator.getInstance();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGeneratorBuilder#getName()
	 */
	public String getName() {
		return "Question generator for SciCast";
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.ISSBNGeneratorBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {}

}
