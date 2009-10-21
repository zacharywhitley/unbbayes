package unbbayes.util.extension.builder;

import unbbayes.controller.oobn.OOBNController;
import unbbayes.gui.oobn.OOBNWindow;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.impl.ObjectOrientedBayesianNetwork;
import unbbayes.util.extension.UnBBayesModule;

/**
 * This class instantiates a {@link OOBNWindow} using most basic parameters.
 * @author Shou Matsumoto
 *
 */
public class OOBNWindowBuilder extends NamedWindowBuilder {

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		IObjectOrientedBayesianNetwork oobn = ObjectOrientedBayesianNetwork.newInstance(this.getName());
		OOBNController controller = OOBNController.newInstance(oobn);
		return (UnBBayesModule)controller.getPanel();
	}

}
