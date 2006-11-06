package unbbayes.controller;

import java.util.ResourceBundle;

import unbbayes.gui.NetworkWindow;
import unbbayes.prs.Edge;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
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

	
	/*---------------------------- Edge ---------------------------*/
	
    /**
     *  Faz a ligacão do arco desejado entre pai e filho.
     *
     * @param  arco  um <code>TArco</code> que representa o arco a ser ligado
     * @since
     */
    public void insertEdge(Edge arco) {
    	
    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag(); 
    	mFragCurrent.addEdge(arco);
    	
    }
	
	
	/*---------------------------- MFrag ----------------------------*/
	
	public void insertDomainMFrag(String name) {
		
		multiEntityBayesianNetwork.addDomainMFrag(new DomainMFrag("DomainMFrag "
				+ multiEntityBayesianNetwork.getMFragCount(), multiEntityBayesianNetwork)); 
		
		screen.getMebnEditionPane().getMTheoryTree().updateTree(); 
	    screen.getMebnEditionPane().setMTheoryTreeActive(); 
	    
	    screen.getGraphPane().resetGraph(); 
	
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
	}
	
	public void setCurrentMFrag(MFrag mFrag){
		
		multiEntityBayesianNetwork.setCurrentMFrag(mFrag); 
	    screen.getGraphPane().resetGraph(); 
		
	}	


	
	/*---------------------------- ContextNode ----------------------------*/	
	
	public void insertContextNode(double x, double y) throws Exception {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		if (!(currentMFrag instanceof DomainMFrag)) {
			// TODO Criar uma exception específica para isso...
			throw new Exception("Context nodes must be added only in domain MFrags!");
		}
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		ContextNode node = new ContextNode(resource.getString("contextNodeName") + domainMFrag.getContextNodeCount(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addContextNode(node);
	}
	
	public void insertResidentNode(double x, double y) throws Exception {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		if (!(currentMFrag instanceof DomainMFrag)) {
			// TODO Criar uma exception específica para isso...
			throw new Exception("Resident nodes must be added only in domain MFrags!");
		}
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		DomainResidentNode node = new DomainResidentNode(resource.getString("contextNodeName") + domainMFrag.getDomainResidentNodeCount(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addDomainResidentNode(node);
	}	
	
	public void insertInputNode(double x, double y) throws Exception {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		if (!(currentMFrag instanceof DomainMFrag)) {
			// TODO Criar uma exception específica para isso...
			throw new Exception("Input nodes must be added only in domain MFrags!");
		}
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		GenerativeInputNode node = new GenerativeInputNode(resource.getString("contextNodeName") + domainMFrag.getGenerativeInputNodeCount(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addGenerativeInputNode(node);
	}		
	
	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode)
            ((ContextNode)selected).delete();
    }

}
