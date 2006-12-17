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
import unbbayes.prs.mebn.MEBNConstructionException;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.Entity;

/**
 * 
 * @author Laecio
 *
 */

public class MEBNController {

	private NetworkWindow screen;

	private MultiEntityBayesianNetwork multiEntityBayesianNetwork;

	private ResidentNode residentNodeActive; 
	private InputNode inputNodeActive; 
	private ContextNode contextNodeActive; 
	private Node nodeActive; 
	
	public ResidentNode getResidentNodeActive(){
		return residentNodeActive; 
	}
	
	public InputNode getInputNodeActive(){
		return inputNodeActive; 
	}
	
	public ContextNode getContextNodeActive(){
		return contextNodeActive; 
	}
	
	public Node getNodeActive(){
		return nodeActive; 
	}
	
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
     * @param  edge  um <code>TArco</code> que representa o arco a ser ligado
     * @since
     */
    public void insertEdge(Edge edge) throws MEBNConstructionException, Exception{
    	
    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag(); 

    	((DomainMFrag)mFragCurrent).addEdge(edge);
    	
    }
	
	
	/*---------------------------- MFrag ----------------------------*/
	
	public void insertDomainMFrag(String name) {
		
		DomainMFrag domainMFrag = new DomainMFrag(resource.getString("domainMFragName")
				+ multiEntityBayesianNetwork.getDomainMFragNum(), multiEntityBayesianNetwork); 
		
		multiEntityBayesianNetwork.addDomainMFrag(domainMFrag); 
		
		screen.getMebnEditionPane().getMTheoryTree().updateTree();
		screen.getMebnEditionPane().getInputInstanceOfSelection().updateTree(); 
	    screen.getMebnEditionPane().setMTheoryTreeActive(); 
	    
	    screen.getGraphPane().resetGraph(); 
	    
	    screen.getMebnEditionPane().setMFragCardActive(); 
		screen.getMebnEditionPane().setTxtNameMFrag(domainMFrag.getName()); 	    
		screen.getMebnEditionPane().setMTheoryTreeActive(); 
		
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
        screen.getMebnEditionPane().getMTheoryTree().updateTree(); 	
	}
	
	public void setCurrentMFrag(MFrag mFrag){
		
		multiEntityBayesianNetwork.setCurrentMFrag(mFrag); 
	    screen.getGraphPane().resetGraph(); 
	    
	    screen.getMebnEditionPane().setMFragCardActive(); 
		screen.getMebnEditionPane().setTxtNameMFrag(mFrag.getName()); 		    
		screen.getMebnEditionPane().setMTheoryTreeActive(); 
	}
	
	public MFrag getCurrentMFrag(){
		return multiEntityBayesianNetwork.getCurrentMFrag(); 
	}
	
	/*---------------------------- Resident Node ----------------------------*/	
	
	public void insertResidentNode(double x, double y) throws MEBNConstructionException {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MEBNConstructionException(resource.getString("withoutMFrag"));
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		DomainResidentNode node = new DomainResidentNode(resource.getString("residentNodeName") + domainMFrag.getDomainResidentNodeNum(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addDomainResidentNode(node);
		
		residentNodeActive = node; 
		nodeActive = node; 
		
		screen.getMebnEditionPane().getInputInstanceOfSelection().updateTree(); 
		screen.getMebnEditionPane().setEditArgumentsTabActive(node);
		screen.getMebnEditionPane().setPossibleValuesEditTabActive(node); 
		screen.getMebnEditionPane().setArgumentTabActive(); 
	    screen.getMebnEditionPane().setResidentCardActive(); 
		screen.getMebnEditionPane().setTxtNameResident(((ResidentNode)node).getName()); 	
		
	}
	
	/**
	 * Adiciona um possivel valor (estado) no nodo resident... 
	 * @param resident
	 * @param value
	 */
	public void addPossibleValue(DomainResidentNode resident, String nameValue){
		
		Entity value = new CategoricalStatesEntity(nameValue); 
		resident.addPossibleValue(value); 
				
	}
	
	/*---------------------------- Input Node ----------------------------*/		
	
	public void insertInputNode(double x, double y) throws MEBNConstructionException {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		
		if (currentMFrag == null) {
			throw new MEBNConstructionException(resource.getString("withoutMFrag"));
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		GenerativeInputNode node = new GenerativeInputNode(resource.getString("inputNodeName") + domainMFrag.getGenerativeInputNodeNum(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addGenerativeInputNode(node);
		
		inputNodeActive = node; 
		nodeActive = node; 
		
	    screen.getMebnEditionPane().setInputCardActive(); 	
		screen.getMebnEditionPane().setTxtNameInput(((InputNode)node).getName()); 		    
		screen.getMebnEditionPane().setInputInstanceOfActive(); 
	}		
	
	public void setInputInstanceOf(GenerativeInputNode input, ResidentNode resident){
		
		input.setInputInstanceOf((DomainResidentNode)resident); 
		screen.getMebnEditionPane().setTxtInputOf(resident.getName()); 
		screen.getMebnEditionPane().updateUI(); 
	
	}
	
	/*---------------------------- ContextNode ----------------------------*/	
	
	public void insertContextNode(double x, double y) throws MEBNConstructionException {
		
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		
		if (currentMFrag == null) {
			throw new MEBNConstructionException(resource.getString("withoutMFrag"));
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		ContextNode node = new ContextNode(resource.getString("contextNodeName") + domainMFrag.getContextNodeNum(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addContextNode(node);
		
		contextNodeActive = node; 
		nodeActive = node; 
		
	    screen.getMebnEditionPane().setContextCardActive();
		screen.getMebnEditionPane().setFormulaEdtionActive(node); 	
		screen.getMebnEditionPane().setTxtNameContext(((ContextNode)node).getName()); 
	}	
	
	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode)
            ((ContextNode)selected).delete();
        else{
        	
        	if (selected instanceof DomainResidentNode)
                ((DomainResidentNode)selected).delete();
        	else{
            	if (selected instanceof GenerativeInputNode)
                    ((GenerativeInputNode)selected).delete();
            
            	else{
                    if (selected instanceof Edge) {
                    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag(); 
                    	mFragCurrent.removeEdge((Edge) selected);
                    }
            	}
        	}
        }
        
        screen.getMebnEditionPane().getMTheoryTree().updateTree(); 
    
	}
	
	/*---------------------------- Nodes ----------------------------*/	
	
	public void selectNode(Node node){
		if (node instanceof ResidentNode){
			residentNodeActive = (ResidentNode)node; 
			nodeActive = node; 
			screen.getMebnEditionPane().setResidentCardActive(); 
			screen.getMebnEditionPane().setEditArgumentsTabActive((ResidentNode)node); 
			screen.getMebnEditionPane().setPossibleValuesEditTabActive((DomainResidentNode)node); 
			screen.getMebnEditionPane().setTxtNameResident(((ResidentNode)node).getName()); 	
			screen.getMebnEditionPane().setArgumentTabActive(); 	
		}
		else{
			if(node instanceof InputNode){
				inputNodeActive = (InputNode)node;
				nodeActive = node; 
				screen.getMebnEditionPane().setInputCardActive(); 
				screen.getMebnEditionPane().setTxtNameInput(((InputNode)node).getName()); 				
				screen.getMebnEditionPane().setInputInstanceOfActive(); 
			}
			else{
				if(node instanceof ContextNode){
					contextNodeActive = (ContextNode)node; 
					nodeActive = node; 
					screen.getMebnEditionPane().setContextCardActive(); 
					screen.getMebnEditionPane().setFormulaEdtionActive((ContextNode)node); 					
					screen.getMebnEditionPane().setTxtNameContext(((ContextNode)node).getName()); 					
				}
				else{
					
				}
			}
			
		}	
	}

	/*---------------------------- Ordinary Variable ----------------------------*/	
		
	/**
	 * Create a ordinary variable and add it in the
	 * current MFrag (if it is a DomainMFrag). 
	 * 
	 */
	
	public OrdinaryVariable addNewOrdinaryVariableInMFrag() throws Exception{
		
        MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		
		if (!(currentMFrag instanceof DomainMFrag)) {
			// TODO Criar uma exception específica para isso...
			throw new Exception("Ordinary Variables must be added only in domain MFrags!");
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		String name = resource.getString("ordinaryVariableName") + domainMFrag.getOrdinaryVariableNum(); 
		OrdinaryVariable ov = new OrdinaryVariable(name, domainMFrag);
		domainMFrag.addOrdinaryVariable(ov);
		
		return ov; 
		
	}
	
	/**
	 * Remove one ordinary variable of the current MFrag.
	 * @param ov
	 */
	public void removeOrdinaryVariableOfMFrag(OrdinaryVariable ov){

        MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
        currentMFrag.removeOrdinaryVariable(ov);
        
	}
	
	public void addOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable){
		
		ResidentNode resident = (ResidentNode) screen.getGraphPane().getSelected(); 
		resident.addOrdinaryVariable(ordinaryVariable);
		screen.getMebnEditionPane().getEditArgumentsTab().update();
		screen.getMebnEditionPane().updateUI(); 
	}
	
	public void removeOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable){
		
		ResidentNode resident = (ResidentNode) screen.getGraphPane().getSelected(); 
		resident.removeOrdinaryVariable(ordinaryVariable);
		screen.getMebnEditionPane().getEditArgumentsTab().update(); 
		screen.getMebnEditionPane().updateUI(); 
		
	}	
	
	/*---------------------------- Formulas ----------------------------*/	
		
	public void selectOVariableInEdit(OrdinaryVariable ov){
	
		screen.getMebnEditionPane().getEditOVariableTab().setNameOVariableSelected(ov.getName()); 
		
	}
	
	
	
}
