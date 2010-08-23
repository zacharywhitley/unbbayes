/**
 * 
 */
package unbbayes.gui.mebn.extension;

import javax.swing.JPanel;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.gui.mebn.ontology.protege.ProtegeOWLTabPanel;
import unbbayes.io.mebn.MEBNStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import edu.stanford.smi.protegex.owl.ui.cls.OWLClassesTab;

/**
 * A builder for a tab showing the protege's OWL property editor
 * 
 * @author Shou Matsumoto
 *
 */
public class ProtegeOWLClassPanelBuilder extends JPanel implements IMEBNEditionPanelBuilder {

	/**
	 * Default constructor must be public to enable plugin support
	 */
	public ProtegeOWLClassPanelBuilder() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn,
			IMEBNMediator mediator) {
		// we do not need this plugin if mebn is not bound to a owl project
		if (mebn.getStorageImplementor() == null 
				|| !(mebn.getStorageImplementor() instanceof MEBNStorageImplementorDecorator)
				|| ((MEBNStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee() == null) {
			return null;
		}
		return ProtegeOWLTabPanel.newInstance(OWLClassesTab.class.getName(), mebn, mediator);
	}

}
