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
	
	/** When a change to the selected edition panel is triggered, a {@link java.beans.PropertyChangeEvent} with this name will be notified (usually, followed by the index of the changed tabs)  */
	public static String MEBN_EDITION_PANEL_CHANGE_PROPERTY = "unbbayes.gui.mebn.extension.editor.MEBN_EDITION_PANEL_CHANGE_PROPERTY";

	/**
	 * Builds a panel to edit a MEBN domain.
	 * @param mebn : this is the MEBN to be edited by the built panel.
	 * @param mediator : this object implements mediator pattern. I.e. it is an object for centralized communication
	 * between different objects. It is usually an object of {@link unbbayes.controller.mebn.MEBNController}
	 * @return a new instance of JPanel. A {@link java.beans.PropertyChangeListener} will be triggered when this panel is
	 * selected, so, use addPropertyChangeListener(IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY ,java.beans.PropertyChangeListener)
	 * to subscribe.
	 * 
	 */
	public abstract JPanel buildPanel(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator);
	
	
}
