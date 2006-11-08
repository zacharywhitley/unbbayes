package unbbayes.controller;

import java.util.ResourceBundle;

import unbbayes.gui.NetworkWindow;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;

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
		
		DomainMFrag domainMFrag = new DomainMFrag("DomainMFrag"
				+ multiEntityBayesianNetwork.getMFragCount(), multiEntityBayesianNetwork); 
		
		multiEntityBayesianNetwork.addDomainMFrag(domainMFrag); 
		
		screen.getMebnEditionPane().getMTheoryTree().updateTree(); 
	    screen.getMebnEditionPane().setMTheoryTreeActive(); 
	    
	    screen.getGraphPane().resetGraph(); 
	    
	    screen.getMebnEditionPane().setMFragCardActive(); 
		screen.getMebnEditionPane().setTxtNameMFrag(domainMFrag.getName()); 	    
	
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
	}
	
	public void setCurrentMFrag(MFrag mFrag){
		
		multiEntityBayesianNetwork.setCurrentMFrag(mFrag); 
	    screen.getGraphPane().resetGraph(); 
	    
	    screen.getMebnEditionPane().setMFragCardActive(); 
		screen.getMebnEditionPane().setTxtNameMFrag(mFrag.getName()); 		    
		
	}
	
	public MFrag getCurrentMFrag(){
		return multiEntityBayesianNetwork.getCurrentMFrag(); 
	}
	
	/*---------------------------- Resident Node ----------------------------*/	
	
	public void insertResidentNode(double x, double y) throws Exception {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		if (!(currentMFrag instanceof DomainMFrag)) {
			// TODO Criar uma exception específica para isso...
			throw new Exception("Resident nodes must be added only in domain MFrags!");
		}
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		DomainResidentNode node = new DomainResidentNode(resource.getString("residentNodeName") + domainMFrag.getDomainResidentNodeCount(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addDomainResidentNode(node);
		 
	    screen.getMebnEditionPane().setResidentCardActive(); 
		screen.getMebnEditionPane().setTxtName(((ResidentNode)node).getName()); 		    
		
	}	
	
	/*---------------------------- Input Node ----------------------------*/		
	
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
		
	    screen.getMebnEditionPane().setInputCardActive(); 	
		screen.getMebnEditionPane().setTxtNameInput(((InputNode)node).getName()); 		    
		//screen.getMebnEditionPane().setinputInstanceOfActive();
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
		
	    screen.getMebnEditionPane().setContextCardActive(); 
		screen.getMebnEditionPane().setTxtNameContext(((ContextNode)node).getName()); 		    
	}	
	
	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode)
            ((ContextNode)selected).delete();
    }

	public void selectNode(Node node){
		if (node instanceof ResidentNode){
			screen.getMebnEditionPane().setResidentCardActive();
			screen.getMebnEditionPane().setTxtName(((ResidentNode)node).getName()); 		  
		}
		else{
			if(node instanceof InputNode){
				screen.getMebnEditionPane().setInputCardActive(); 
				screen.getMebnEditionPane().setTxtNameInput(((InputNode)node).getName()); 				
			}
			else{
				if(node instanceof ContextNode){
					screen.getMebnEditionPane().setContextCardActive(); 
					screen.getMebnEditionPane().setTxtNameContext(((ContextNode)node).getName()); 					
				}
				else{
					
				}
			}
			
		}
	}
	
}
