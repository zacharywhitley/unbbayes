package unbbayes.gui.mebn.extension.node;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.IdentityNode;

public class IdentityNodePanelBuilder implements IProbabilityFunctionPanelBuilder {

	private Node probabilityFunctionOwner;
	
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.mebn.resources.Resources.class.getName(),
			Locale.getDefault(), getClass().getClassLoader());
	
	/**
	 * 
	 */
	public IdentityNodePanelBuilder() {
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#buildProbabilityFunctionEditionPanel()
	 */
	public JPanel buildProbabilityFunctionEditionPanel() {

		// extract the node and the controller
		final IdentityNode node = (IdentityNode) this.getProbabilityFunctionOwner();
		final IMEBNMediator mediator = node.getMediator();
		
		// just make really, really sure the controller knows we are editing this node
		mediator.setActiveResidentNode(node);
		mediator.getMebnEditionPane().setEditArgumentsTabActive(node);	// this is just to guarantee that the arguments tab is initialized
		
		// instantiate the panel to edit this node. It is the same panel to edit resident nodes
		IdentityNodePanel ret = new IdentityNodePanel((MEBNController) mediator, node, getResource().getString("IdentityNode"));
		
		// but in identity nodes, we don't need to edit states not LPD
		ret.getNodeToolBar().getBtnEditStates().setEnabled(false);
		ret.getNodeToolBar().getBtnEditStates().setVisible(false);
		ret.getNodeToolBar().remove(ret.getNodeToolBar().getBtnEditStates());
		ret.getNodeToolBar().setBtnEditStates(null);
		ret.getNodeToolBar().getBtnEditTable().setEnabled(false);
		ret.getNodeToolBar().getBtnEditTable().setVisible(false);
		ret.getNodeToolBar().remove(ret.getNodeToolBar().getBtnEditTable());
		ret.getNodeToolBar().setBtnEditTable(null);
		
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
