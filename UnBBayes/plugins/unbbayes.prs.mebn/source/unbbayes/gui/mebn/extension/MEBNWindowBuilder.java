/**
 * 
 */
package unbbayes.gui.mebn.extension;

import unbbayes.gui.mebn.MEBNNetworkWindow;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.builder.NamedWindowBuilder;

/**
 * 
 * This class instantiates a Network window (multi entity network) using most basic parameters.
 * @author Shou Matsumoto
 *
 */
public class MEBNWindowBuilder extends NamedWindowBuilder {
	
	/**
	 * Default constructor. Basically, it initializes names
	 */
	public MEBNWindowBuilder() {
		this.setName("MEBN");	// TODO use resource files instead
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		MEBNNetworkWindow ret = new MEBNNetworkWindow(new MultiEntityBayesianNetwork(this.getName()));
//		ret.setVisible(false);
		ret.setModuleName(this.getName());
		ret.setTitle(this.getName());
		return ret;
	}
	
}
