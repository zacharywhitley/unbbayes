/**
 * 
 */
package unbbayes.gui.mebn.extension.editor;

import javax.swing.JPanel;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * This class is a part of UnBBayes-MEBN' plugin framework
 * for MEBN editors (tabs to edit a MEBN domain).
 * It is basically a builder for JPanel construction.
 * @author Shou Matsumoto
 *
 */
public interface IMEBNEditionPanelBuilder {

	/**
	 * Builds a panel to edit a MEBN domain.
	 * @param mebn : this is the MEBN to be edited by the built panel.
	 * @param mediator : this object implements mediator pattern. I.e. it is an object for centralized communication
	 * between different objects. It is usually an object of {@link unbbayes.controller.mebn.MEBNController}
	 * @return a new instance of JPanel.
	 */
	public abstract JPanel buildPanel(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator);
	
}
