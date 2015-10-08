/**
 * 
 */
package unbbayes.gui.medg;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.medg.IMEDGNode;
import unbbayes.prs.medg.MultiEntityUtilityNode;

/**
 * @author Shou Matsumoto
 *
 */
public class MultiEntityUtilityNodePanelBuilder implements
		IProbabilityFunctionPanelBuilder {

	private Node probabilityFunctionOwner;
	
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.resources.GuiResources.class.getName(),
			Locale.getDefault(), getClass().getClassLoader());
	
	/**
	 * 
	 */
	public MultiEntityUtilityNodePanelBuilder() {
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#buildProbabilityFunctionEditionPanel()
	 */
	public JPanel buildProbabilityFunctionEditionPanel() {

		// extract the node and the controller
		final IMEDGNode node = (IMEDGNode) this.getProbabilityFunctionOwner();
		final IMEBNMediator mediator = node.getMediator();
		
		// just make really, really sure the controller knows we are editing this node
		mediator.setActiveResidentNode(node.asResidentNode());
		mediator.getMebnEditionPane().setEditArgumentsTabActive(node.asResidentNode());	// this is just to guarantee that the arguments tab is initialized
		
		// instantiate the panel to edit this node. It is the same panel to edit resident nodes
		MEDGNodePanel ret = new MEDGNodePanel((MEBNController) mediator, node, getResource().getString("utilityNodeColorLabel"));
		
		// but in utility nodes, we don't need to edit states
		ret.getMEDGNodeToolBar().getBtnEditStates().setEnabled(false);
		ret.getMEDGNodeToolBar().getBtnEditStates().setVisible(false);
		ret.getMEDGNodeToolBar().remove(ret.getMEDGNodeToolBar().getBtnEditStates());
		ret.getMEDGNodeToolBar().setBtnEditStates(null);
		
		return ret;
	
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#getProbabilityFunctionOwner()
	 */
	public Node getProbabilityFunctionOwner() {
		return probabilityFunctionOwner;
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#setProbabilityFunctionOwner(unbbayes.prs.Node)
	 */
	public void setProbabilityFunctionOwner(Node probabilityFunctionOwner) {
		this.probabilityFunctionOwner = probabilityFunctionOwner;
	}


	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}

}
