/**
 * 
 */
package unbbayes.util.extension.builder;

import unbbayes.gui.NetworkWindow;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.extension.UnBBayesModule;

/**
 * 
 * This class instantiates a Network window (multi entity network) using most basic parameters.
 * @author Shou Matsumoto
 *
 */
public class MEBNWindowBuilder extends NamedWindowBuilder {

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		NetworkWindow ret = new NetworkWindow(new MultiEntityBayesianNetwork(this.getName()));
//		ret.setVisible(false);
		return ret;
	}
	
}
