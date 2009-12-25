/**
 * 
 */
package unbbayes.gui.oobn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.GraphAction;
import unbbayes.gui.PNEditionPane;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNEditionPane extends PNEditionPane {

	private OOBNClassWindow linkedWindow = null;
	
	
	private JButton changeNodeTypeButton = null;
	
	private IconController iconController = IconController.getInstance();
	
	
	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.oobn.resources.OOBNGuiResource.class.getName());
	
	
	/**
	 * @param window
	 * @param _controller
	 */
	protected OOBNEditionPane(OOBNClassWindow window, NetworkController _controller) {
		super(window , _controller);
		// TODO Auto-generated constructor stub
		
	}
	
	/**
	 * @param window
	 * @param _controller
	 */
	public static OOBNEditionPane newInstance(OOBNClassWindow window, NetworkController _controller) {
		OOBNEditionPane ret =  new OOBNEditionPane( window,  _controller);
		ret.filterUnusedToolBarEditionButtons();
		ret.setUpButtonListeners();
		ret.setLinkedWindow(window);
		ret.setUpOOBNSpecificButtons();
		return ret;
	}
	
	/**
	 * Alters the behavior of superclass' (PNEditionPane') edition buttons
	 * @see PNEditionPane
	 */
	public void setUpButtonListeners() {
		this.getTbEdition().getBtnAddProbabilisticNode().addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent ae) {
	            	getLinkedWindow().getGraphPane().setAction(GraphAction.CREATE_PROBABILISTIC_NODE);
	            	Debug.println(this.getClass(), "Hey, yo! I'm gonna add a node from OOBNEditionPane!!");
	        }
		});
	}
	
	/**
	 * Hides some superclass' (PNEditionPane') edition buttons
	 * which are not very useful for oobn class edition
	 * @see PNEditionPane
	 */
	public void filterUnusedToolBarEditionButtons() {
		super.getTbEdition().getBtnAddPluginButton().setVisible(false);
		super.getTbEdition().getBtnAddDecisionNode().setVisible(false);
		super.getTbEdition().getBtnAddUtilityNode().setVisible(false);
		 
		// the two below is set to false because we cannot get the dimensions correctly by now
		super.getBtnPrintNet().setVisible(false);
		super.getBtnSaveNetImage().setVisible(false);
		
		 
		super.getBtnCompile().setVisible(false);
		super.getBtnHierarchy().setVisible(false);
		super.getBtnPreviewNet().setVisible(false);
		
		super.getBtnEvaluate().setVisible(false);
		
	}
	
	/**
	 * Sets up the buttons for OOBN edition;
	 * like the button to change node's type
	 */
	public void setUpOOBNSpecificButtons() {
		
		// setting up button to change node type
		
		JButton button = new JButton(this.getIconController().getChangeNodeTypeIcon());
		
		this.setChangeNodeTypeButton(button);
		button.setToolTipText(resource.getString("changeNodeType"));
		
		this.getChangeNodeTypeButton().addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
				getLinkedWindow().getGraphPane().showNodeTypeChangePopup(getChangeNodeTypeButton()
												, getChangeNodeTypeButton().getX()
												, getChangeNodeTypeButton().getY());
				
			}
			
		});
		
		super.getTbEdition().add(button);
		
		
		
		
		
	}

	/**
	 * @return the linkedWindow
	 */
	public OOBNClassWindow getLinkedWindow() {
		return linkedWindow;
	}

	/**
	 * @param linkedWindow the linkedWindow to set
	 */
	public void setLinkedWindow(OOBNClassWindow linkedWindow) {
		this.linkedWindow = linkedWindow;
	}

	/**
	 * @return the changeNodeTypeButton
	 */
	public JButton getChangeNodeTypeButton() {
		return changeNodeTypeButton;
	}

	/**
	 * @param changeNodeTypeButton the changeNodeTypeButton to set
	 */
	public void setChangeNodeTypeButton(JButton changeNodeTypeButton) {
		this.changeNodeTypeButton = changeNodeTypeButton;
	}

	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return iconController;
	}

	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}
	
	
	

}
