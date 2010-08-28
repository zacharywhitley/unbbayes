/**
 * 
 */
package unbbayes.gui.prm.extension;

import unbbayes.gui.prm.PRMWindow;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.builder.NamedWindowBuilder;

/**
 * @author Shou Matsumoto
 *
 */
public class PRMWindowBuilder extends NamedWindowBuilder {

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		PRMWindow ret = PRMWindow.newInstance(this.getName());
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.builder.NamedWindowBuilder#getName()
	 */
	public String getName() {
		return "PRM - ALPHA";
	}
	
	

}
