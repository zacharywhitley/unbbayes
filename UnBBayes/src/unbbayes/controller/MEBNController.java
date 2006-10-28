package unbbayes.controller;

import java.util.ResourceBundle;

import unbbayes.gui.NetworkWindow;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

public class MEBNController {

	private NetworkWindow screen;

	private MultiEntityBayesianNetwork multiEntityBayesianNetwork;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.controller.resources.ControllerResources");

	public MEBNController(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork,
			NetworkWindow screen) {
		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
		this.screen = screen;
	}

	public void insertDomainMFrag(String name) {
		multiEntityBayesianNetwork.addDomainMFrag(new DomainMFrag("DomainMFrag "
				+ multiEntityBayesianNetwork.getMFragCount(), multiEntityBayesianNetwork));
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
	}

	public void insertContextNode(double x, double y) throws Exception {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		if (!(currentMFrag instanceof DomainMFrag)) {
			// TODO Criar uma exception específica para isso...
			throw new Exception(
					"Context nodes must be added only in domain MFrags!");
		}
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		ContextNode node = new ContextNode(resource.getString("contextNodeName") + domainMFrag.getContextNodeCount(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addContextNode(node);
	}
	
	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode)
            ((ContextNode)selected).delete();
    }

}
