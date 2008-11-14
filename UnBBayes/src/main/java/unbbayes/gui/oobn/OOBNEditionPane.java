/**
 * 
 */
package unbbayes.gui.oobn;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import unbbayes.controller.NetworkController;
import unbbayes.gui.GlobalOptionsDialog;
import unbbayes.gui.GraphAction;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.PNEditionPane;
import unbbayes.prs.Network;
import unbbayes.prs.oobn.impl.BasicOOBNClass;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNEditionPane extends PNEditionPane {

	private OOBNClassWindow linkedWindow = null;
	
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
		super.getTbEdition().getBtnAddContinuousNode().setVisible(false);
		super.getTbEdition().getBtnAddDecisionNode().setVisible(false);
		super.getTbEdition().getBtnAddUtilityNode().setVisible(false);
		
		super.getBtnCompile().setVisible(false);
		super.getBtnHierarchy().setVisible(false);
		super.getBtnPreviewNet().setVisible(false);
		
		super.getBtnEvaluate().setVisible(false);
		
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
	
	
	

}
