package unbbayes.gui.msbn.extension;

import unbbayes.controller.MSBNController;
import unbbayes.gui.msbn.MSBNWindow;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.builder.NamedWindowBuilder;

/**
 * This class instantiates a {@link MSBNWindow} using most basic parameters.
 * @author Shou Matsumoto
 *
 */
public class MSBNWindowBuilder extends NamedWindowBuilder {

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		SingleAgentMSBN msbn = new SingleAgentMSBN(this.getName());
		MSBNController controller = new MSBNController(msbn);
//		controller.getPanel().setVisible(false);
		return (UnBBayesModule)controller.getPanel();
	}

}
