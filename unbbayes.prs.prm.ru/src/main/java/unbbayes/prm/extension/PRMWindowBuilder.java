/**
 * 
 */
package unbbayes.prm.extension;

import unbbayes.prm.view.MainInternalFrame;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.builder.NamedWindowBuilder;

/**
 * @author David Saldaña.
 *
 */
public class PRMWindowBuilder extends NamedWindowBuilder {

	private static final String PLUGIN_NAME = "PRM Plugin";

	/* 
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		MainInternalFrame ret = new MainInternalFrame(PLUGIN_NAME);
		return ret;
	}

	/* 
	 * @see unbbayes.util.extension.builder.NamedWindowBuilder#getName()
	 */
	public String getName() {
		return PLUGIN_NAME;
	}
}
