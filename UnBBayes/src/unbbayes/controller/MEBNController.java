package unbbayes.controller;

import java.util.ResourceBundle;

import unbbayes.gui.MEBNEditionPane;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.BooleanStatesEntity;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * Controller of MEBN
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class MEBNController {

	private NetworkWindow screen;
	private MEBNEditionPane mebnEditionPane; 

	private MultiEntityBayesianNetwork multiEntityBayesianNetwork;

	/* Nodes actives */
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
		mebnEditionPane = screen.getMebnEditionPane(); 
	
	}

	public void enableMTheoryEdition(){
		
	    screen.getMebnEditionPane().setMTheoryCardActive(); 
	    screen.getMebnEditionPane().setNameMTheory(this.multiEntityBayesianNetwork.getName()); 	    
		screen.getMebnEditionPane().setMTheoryTreeActive(); 
		
	}
	
	public void setNameMTheory(String name){
		
		this.multiEntityBayesianNetwork.setName(name);
		screen.getMebnEditionPane().setNameMTheory(name); 
		
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
		screen.getMebnEditionPane().setMTheoryTreeActive(); 
	    
	    showGraphMFrag(domainMFrag); 
	    
	    screen.getMebnEditionPane().setMFragCardActive(); 
	    screen.getMebnEditionPane().setTxtNameMFrag(domainMFrag.getName()); 	    
		screen.getMebnEditionPane().setMTheoryTreeActive(); 
		
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
		screen.getMebnEditionPane().getMTheoryTree().updateTree(); 	
	}
	
	public void setCurrentMFrag(MFrag mFrag){
		
		showGraphMFrag(mFrag); 
	    
	    screen.getMebnEditionPane().setMFragCardActive(); 
	    screen.getMebnEditionPane().setTxtNameMFrag(mFrag.getName()); 		    
		screen.getMebnEditionPane().setMTheoryTreeActive(); 
	}
	
	/**
	 * Apenas mostra o grafo da MFrag e a seleciona como MFrag ativa. 
	 * @param mFrag
	 */
	public void showGraphMFrag(MFrag mFrag){
		
		multiEntityBayesianNetwork.setCurrentMFrag(mFrag); 
	    screen.getGraphPane().resetGraph(); 
	    screen.getMebnEditionPane().showTitleGraph(mFrag.getName()); 
	    
	}
	
	public MFrag getCurrentMFrag(){
		return multiEntityBayesianNetwork.getCurrentMFrag(); 
	}
	
	/*---------------------------- Domain Resident Node ----------------------------*/	
	
	public void insertDomainResidentNode(double x, double y) throws MFragDoesNotExistException {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		DomainResidentNode node = new DomainResidentNode(resource.getString("residentNodeName") + domainMFrag.getDomainResidentNodeNum(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addDomainResidentNode(node);
		
		residentNodeActive = node; 
		nodeActive = node; 
		
		screen.getMebnEditionPane().setEditArgumentsTabActive(node);
		screen.getMebnEditionPane().setResidentNodeTabActive(node); 
		screen.getMebnEditionPane().setArgumentTabActive(); 
		screen.getMebnEditionPane().setResidentCardActive(); 
	    screen.getMebnEditionPane().setTxtNameResident(((ResidentNode)node).getName()); 
	    
	}
	
	public void renameDomainResidentNode(DomainResidentNode resident, String newName){
		resident.setName(newName);	
		screen.getMebnEditionPane().repaint(); 
	}
	
	/**
	 * Adiciona um possivel valor (estado) no nodo resident... 
	 * @param resident
	 * @param value
	 */
	public void addPossibleValue(DomainResidentNode resident, String nameValue){
		
		CategoricalStatesEntity value = CategoricalStatesEntity.createCategoricalEntity(nameValue); 
		resident.addPossibleValue(value); 
		value.addNodeToListIsPossibleValueOf(resident); 
				
	}
	
	public void addBooleanAsPossibleValue(DomainResidentNode resident){
	
		resident.addPossibleValue(BooleanStatesEntity.getTrueStateEntity());
		resident.addPossibleValue(BooleanStatesEntity.getFalseStateEntity());
		resident.addPossibleValue(BooleanStatesEntity.getAbsurdStateEntity()); 
		
	}
	
	/**
	 * Adiciona um possivel valor (estado) no nodo resident... 
	 * @param resident
	 * @param value
	 */
	public void removePossibleValue(DomainResidentNode resident, String nameValue){
		resident.removePossibleValueByName(nameValue); 	
	}	
	
	public boolean existsPossibleValue(DomainResidentNode resident, String nameValue){
		return resident.existsPossibleValueByName(nameValue); 
	}
	
	
	public void setEnableTableEditionView(){
		
		screen.getMebnEditionPane().showTableEdit();
		
	}
	
	public void setUnableTableEditionView(){
		
		screen.getMebnEditionPane().hideTopComponent(); 
		
	}	
	
	/*---------------------------- Generative Input Node ----------------------------*/		
	
	public void insertGenerativeInputNode(double x, double y) throws MFragDoesNotExistException {
		
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		
		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
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
		screen.getMebnEditionPane().setInputNodeActive(node); 
		screen.getMebnEditionPane().setTxtInputOf(""); 	
	 	
	}	
	
	public void setInputInstanceOf(GenerativeInputNode input, ResidentNode resident){
		
		input.setInputInstanceOf((DomainResidentNode)resident); 
		screen.getMebnEditionPane().setTxtInputOf(resident.getName()); 
		screen.getMebnEditionPane().updateUI(); 
	
	}
	
	/**
	 * Update the input intance of atribute (in the view) of the input node for the value current 
	 * @param input The input node active
	 */
	public void updateInputInstanceOf(GenerativeInputNode input){
		
		Object target = input.getInputInstanceOf(); 
		
		if (target == null){
			screen.getMebnEditionPane().setTxtInputOf(""); 
		}
		else{
			if (target instanceof ResidentNode){
				screen.getMebnEditionPane().setTxtInputOf(((ResidentNode)target).getName()); 
			}
		}
		
	}
	
	/*---------------------------- ContextNode ----------------------------*/	
	
	public void insertContextNode(double x, double y) throws MFragDoesNotExistException {
		
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
		
		if (currentMFrag == null) {
			throw new MFragDoesNotExistException(resource.getString("withoutMFrag"));
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
	
	
	/*-------------------------------- outros -------------------------*/
	
	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode){
            ((ContextNode)selected).delete();
            screen.getMebnEditionPane().getMTheoryTree().updateTree(); 
            screen.getMebnEditionPane().setMTheoryTreeActive();  
        }
        else{
        	
        	if (selected instanceof DomainResidentNode){
                ((DomainResidentNode)selected).delete();
                screen.getMebnEditionPane().getMTheoryTree().updateTree(); 
                screen.getMebnEditionPane().setMTheoryTreeActive();  
        		this.setUnableTableEditionView(); 
        	}
        	else{
            	if (selected instanceof GenerativeInputNode){
                    ((GenerativeInputNode)selected).delete();
                     screen.getMebnEditionPane().getMTheoryTree().updateTree(); 
                     screen.getMebnEditionPane().setMTheoryTreeActive(); 
            	}
            	else{
                    if (selected instanceof Edge) {
                    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag(); 
                    	mFragCurrent.removeEdge((Edge) selected);
                    }
            	}
        	}
        }

	}
	
	/*---------------------------- Nodes ----------------------------*/	
	
	public void selectNode(Node node){
		if (node instanceof ResidentNode){
			residentNodeActive = (ResidentNode)node; 
			setResidentNodeActive(residentNodeActive); 	
		}
		else{
			if(node instanceof InputNode){
				inputNodeActive = (InputNode)node;
				setInputNodeActive(inputNodeActive); 
			}
			else{
				if(node instanceof ContextNode){
					contextNodeActive = (ContextNode)node; 
				    setContextNodeActive(contextNodeActive); 
				}
				else{
					
				}
			}
			
		}	
	    screen.getMebnEditionPane().showTitleGraph(multiEntityBayesianNetwork.getCurrentMFrag().getName());  
	}
	

	private void setResidentNodeActive(ResidentNode residentNodeActive){ 
	   nodeActive = residentNodeActive; 
	   screen.getMebnEditionPane().setResidentCardActive(); 
	   screen.getMebnEditionPane().setEditArgumentsTabActive(residentNodeActive); 
	   screen.getMebnEditionPane().setResidentNodeTabActive((DomainResidentNode)residentNodeActive); 
	   screen.getMebnEditionPane().setTxtNameResident((residentNodeActive).getName()); 	
	   screen.getMebnEditionPane().setArgumentTabActive(); 	
	   this.setUnableTableEditionView(); 
	}
	
	private void setInputNodeActive(InputNode inputNodeActive){
		nodeActive = inputNodeActive; 
		screen.getMebnEditionPane().setInputCardActive(); 
		screen.getMebnEditionPane().setTxtNameInput((inputNodeActive).getName()); 				
		screen.getMebnEditionPane().setInputNodeActive((GenerativeInputNode)inputNodeActive); 
		updateInputInstanceOf((GenerativeInputNode)inputNodeActive); 
		this.setUnableTableEditionView(); 
	}
	
	private void setContextNodeActive(ContextNode contextNodeActive){
		nodeActive = contextNodeActive; 
		screen.getMebnEditionPane().setContextCardActive(); 
		screen.getMebnEditionPane().setFormulaEdtionActive(contextNodeActive); 					
		screen.getMebnEditionPane().setTxtNameContext((contextNodeActive).getName()); 					
		this.setUnableTableEditionView(); 			
	}

	/*---------------------------- Ordinary Variable ----------------------------*/	
		
	/**
	 * Create a ordinary variable and add it in the
	 * current MFrag (if it is a DomainMFrag). 
	 * 
	 */
	
	public OrdinaryVariable addNewOrdinaryVariableInMFrag(){

		DomainMFrag domainMFrag = (DomainMFrag) multiEntityBayesianNetwork.getCurrentMFrag();
		String name = resource.getString("ordinaryVariableName") + domainMFrag.getOrdinaryVariableNum(); 
		String type = Type.getFirstType(); 
		OrdinaryVariable ov = new OrdinaryVariable(name, type, domainMFrag);
		domainMFrag.addOrdinaryVariable(ov);
		
		return ov; 
		
	}
	
	/** 
	 * Create a new ordinary variable and add this in the resident
	 * node active. Add this in the MFrag list of ordinary variables too.
	 * @return new ordinary variable 
	 */
	public OrdinaryVariable addNewOrdinaryVariableInResident() throws OVariableAlreadyExistsInArgumentList, 
	                                                                  ArgumentNodeAlreadySetException{
		
		OrdinaryVariable ov; 
		ov = addNewOrdinaryVariableInMFrag(); 
		addOrdinaryVariableInResident(ov); 
		
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
	
	/**
	 * Add one ordinary variable in the list of arguments of the resident node active.
	 * @param ordinaryVariable ov for add
	 */
	public void addOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable) throws ArgumentNodeAlreadySetException, 
	                                                                                    OVariableAlreadyExistsInArgumentList{
		
		residentNodeActive.addArgument(ordinaryVariable);
		screen.getMebnEditionPane().getEditArgumentsTab().update();
		screen.getMebnEditionPane().updateUI();
		
		
	}
	
	public void removeOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable){
		
		ResidentNode resident = (ResidentNode) screen.getGraphPane().getSelected(); 
		resident.removeArgument(ordinaryVariable);
		screen.getMebnEditionPane().getEditArgumentsTab().update(); 
		screen.getMebnEditionPane().updateUI(); 
		
	}	
	
	public void setOVariableSelectedInResidentTree(OrdinaryVariable oVariableSelected){
		screen.getMebnEditionPane().getEditArgumentsTab().setTxtName(oVariableSelected.getName()); 
		screen.getMebnEditionPane().getEditArgumentsTab().setTreeResidentActive(); 
	}
	
	public void setOVariableSelectedInMFragTree(OrdinaryVariable oVariableSelected){
		screen.getMebnEditionPane().getEditArgumentsTab().setTxtName(oVariableSelected.getName()); 
		screen.getMebnEditionPane().getEditArgumentsTab().setTreeMFragActive(); 
	}	
	
	public void renameOVariableOfResidentTree(String name){
		OrdinaryVariable ov = screen.getMebnEditionPane().getEditArgumentsTab().getResidentOVariableTree().getOVariableSelected(); 
	    ov.setName(name); 
		screen.getMebnEditionPane().getEditArgumentsTab().setTxtName(ov.getName()); 
		screen.getMebnEditionPane().getEditArgumentsTab().update(); 
	}
	
	public void renameOVariableOfMFragTree(String name){
		OrdinaryVariable ov = screen.getMebnEditionPane().getEditArgumentsTab().getMFragOVariableTree().getOVariableSelected(); 
	    ov.setName(name); 
		screen.getMebnEditionPane().getEditArgumentsTab().setTxtName(ov.getName()); 
		screen.getMebnEditionPane().getEditArgumentsTab().update(); 
	}
	
	public void renameOVariableInArgumentEditionPane(String name){
		if (screen.getMebnEditionPane().getEditArgumentsTab().isTreeResidentActive()){
			renameOVariableOfResidentTree(name); 
		}
		else{
			renameOVariableOfMFragTree(name); 
		}
	}
	
	/*---------------------------- Formulas ----------------------------*/	
		
	public void selectOVariableInEdit(OrdinaryVariable ov){
	    OVariableEditionPane editionPane = screen.getMebnEditionPane().getEditOVariableTab(); 
		editionPane.setNameOVariableSelected(ov.getName()); 
		editionPane.setTypeOVariableSelected(ov.getType()); 
		
	}
	
	/*--------------------------------- Entidades ---------------------*/
	
	/**
	 * Adiciona uma nova entidade com o nome passado como parametro
	 * pelo usuario. O tipo da entidade sera um nome gerado automaticamente, a 
	 * partir do passado pelo usuário. 
	 */
	public ObjectEntity addObjectEntity() throws TypeException{

		
		String name = resource.getString("entityName") + ObjectEntity.getEntityNum();

		String nameType = name + "_Label" ; 
		
		Type.addType(nameType);
		
		ObjectEntity objectEntity = new ObjectEntity(name, nameType); 
		 
		return objectEntity; 
		
	}
	
	public void renameObjectEntity(ObjectEntity entity, String name) throws TypeException{
		
		String nameType = name + "_Label" ; 
		
		Type.addType(nameType);
		
		entity.setName(name); 
		entity.setType(nameType);
		
	}
	
	public void removeObjectEntity(ObjectEntity entity){
		ObjectEntity.removeEntity(entity); 
	}
	
}
