package unbbayes.controller;

import java.util.ArrayList;
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
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;

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
	private OrdinaryVariable ordVariableNodeActive; 
	private Node nodeActive; 
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.controller.resources.ControllerResources");

	public MEBNController(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork,
			NetworkWindow screen) {
		
		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
		this.screen = screen;
		mebnEditionPane = new MEBNEditionPane(screen, this);
	
	}

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
	
	public void enableMTheoryEdition(){
		
		mebnEditionPane.setMTheoryBarActive(); 
		mebnEditionPane.setNameMTheory(this.multiEntityBayesianNetwork.getName()); 	    
		mebnEditionPane.setMTheoryTreeActive(); 
		
	}
	
	public void setNameMTheory(String name){
		
		multiEntityBayesianNetwork.setName(name);
		mebnEditionPane.setNameMTheory(name); 
		
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
	
	public void insertDomainMFrag() {
		
		DomainMFrag domainMFrag = new DomainMFrag(resource.getString("domainMFragName")
				+ multiEntityBayesianNetwork.getDomainMFragNum(), multiEntityBayesianNetwork); 
		
		multiEntityBayesianNetwork.addDomainMFrag(domainMFrag); 
		
		mebnEditionPane.getMTheoryTree().updateTree();
		mebnEditionPane.setMTheoryTreeActive(); 
	    
	    showGraphMFrag(domainMFrag); 
	    
	    mebnEditionPane.setMFragBarActive(); 
	    mebnEditionPane.setTxtNameMFrag(domainMFrag.getName()); 	    
	    mebnEditionPane.setMTheoryTreeActive(); 
		
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
		mebnEditionPane.getMTheoryTree().updateTree(); 	
	}
	
	public void setCurrentMFrag(MFrag mFrag){
		
		showGraphMFrag(mFrag); 
	    
		mebnEditionPane.setMFragBarActive(); 
		mebnEditionPane.setTxtNameMFrag(mFrag.getName()); 		    
		mebnEditionPane.setMTheoryTreeActive(); 
	}
	
	/**
	 * rename the MFrag and update its name in the title of the graph
	 * @param mFrag
	 * @param name
	 */
	public void renameMFrag(MFrag mFrag, String name){
			mFrag.setName(name);
			
			if(this.getCurrentMFrag() == mFrag){
				mebnEditionPane.showTitleGraph(name); 
			}
	}
	
	/**
	 * Apenas mostra o grafo da MFrag e a seleciona como MFrag ativa. 
	 * @param mFrag
	 */
	public void showGraphMFrag(MFrag mFrag){
		
		multiEntityBayesianNetwork.setCurrentMFrag(mFrag); 
	    screen.getGraphPane().resetGraph(); 
	    mebnEditionPane.showTitleGraph(mFrag.getName()); 
	    
	}
	
	public MFrag getCurrentMFrag(){
		return multiEntityBayesianNetwork.getCurrentMFrag(); 
	}
	
	public OrdinaryVariable insertOrdinaryVariable(double x, double y) throws MFragDoesNotExistException {
		
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		String name = resource.getString("ordinaryVariableName") + domainMFrag.getOrdinaryVariableNum(); 
		Type type = Type.getDefaultType(); 
		OrdinaryVariable ov = new OrdinaryVariable(name, type, domainMFrag);
		
		ov.setPosition(x, y);
		ov.setDescription(ov.getName());
		domainMFrag.addOrdinaryVariable(ov);
		
		ordVariableNodeActive = ov; 
		setOrdVariableNodeActive(ov); 
		
	    return ov;
	    
	}
	
	/*---------------------------- Domain Resident Node ----------------------------*/	
	
	public DomainResidentNode insertDomainResidentNode(double x, double y) throws MFragDoesNotExistException {
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
		
		mebnEditionPane.setEditArgumentsTabActive(node);
		mebnEditionPane.setResidentNodeTabActive(node); 
		mebnEditionPane.setArgumentTabActive(); 
		mebnEditionPane.setResidentBarActive(); 
		mebnEditionPane.setTxtNameResident(((ResidentNode)node).getName()); 
	    
	    return node;
	}
	
	public void renameDomainResidentNode(DomainResidentNode resident, String newName){
		resident.setName(newName);	
		mebnEditionPane.repaint(); 
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
		
		mebnEditionPane.showTableEdit();
		
	}
	
	public void setUnableTableEditionView(){
		
		mebnEditionPane.hideTopComponent(); 
		
	}	
	
	public boolean isResidentNodeUsed(){
		
		return false;
		
	}
	
	public boolean isContextNodeUsed(){
		
		return false; 
	
	}
	
	public boolean isInputNodeUsed(){
	
		return false; 
	
	}
	
	/*---------------------------- Generative Input Node ----------------------------*/		
	
	public GenerativeInputNode insertGenerativeInputNode(double x, double y) throws MFragDoesNotExistException {
		
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
		
		mebnEditionPane.setInputBarActive(); 	
		mebnEditionPane.setTxtNameInput(((InputNode)node).getName()); 		    
		mebnEditionPane.setInputNodeActive(node); 
		mebnEditionPane.setTxtInputOf(""); 	
	 	
		return node; 
	}	
	
	public void setInputInstanceOf(GenerativeInputNode input, ResidentNode resident){
		
		input.setInputInstanceOf((DomainResidentNode)resident);
		mebnEditionPane.getInputNodePane().updateArgumentPane(); 
		mebnEditionPane.setTxtInputOf(resident.getName()); 
		mebnEditionPane.updateUI(); 
	
	}
	
	public void updateArgumentsOfObject(Object node){
		
		if (node instanceof GenerativeInputNode){
			((GenerativeInputNode)node).updateLabel(); 
		}
		
	}
	
	/**
	 * Update the input intance of atribute (in the view) of the input node for the value current 
	 * @param input The input node active
	 */
	public void updateInputInstanceOf(GenerativeInputNode input){
		
		Object target = input.getInputInstanceOf(); 
		
		if (target == null){
			mebnEditionPane.setTxtInputOf(""); 
		}
		else{
			if (target instanceof ResidentNode){
				mebnEditionPane.setTxtInputOf(((ResidentNode)target).getName()); 
			}
		}
		
	}
	
	/*---------------------------- ContextNode ----------------------------*/	
	
	public ContextNode insertContextNode(double x, double y) throws MFragDoesNotExistException {
		
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
		
		setContextNodeActive(node); 
		
		return node; 
	}	
	
	
	/*-------------------------------- outros -------------------------*/
	
	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode){
            ((ContextNode)selected).delete();
            mebnEditionPane.getMTheoryTree().updateTree(); 
            mebnEditionPane.setMTheoryTreeActive();  
        }
        else{
        	
        	if (selected instanceof DomainResidentNode){
                ((DomainResidentNode)selected).delete();
                mebnEditionPane.getMTheoryTree().updateTree(); 
                mebnEditionPane.setMTheoryTreeActive();  
        		this.setUnableTableEditionView(); 
        	}
        	else{
            	if (selected instanceof GenerativeInputNode){
                    ((GenerativeInputNode)selected).delete();
                     mebnEditionPane.getMTheoryTree().updateTree(); 
                     mebnEditionPane.setMTheoryTreeActive(); 
            	}else{
            	 if (selected instanceof OrdinaryVariable){
                     ((OrdinaryVariable)selected).delete();
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
					if(node instanceof OrdinaryVariable){
						ordVariableNodeActive = (OrdinaryVariable)node; 
						setOrdVariableNodeActive((OrdinaryVariable)node); 
					}
				}
			}
			
		}	
	    mebnEditionPane.showTitleGraph(multiEntityBayesianNetwork.getCurrentMFrag().getName());  
	}
	
	public void updateFormulaActiveContextNode(){
		String formula = contextNodeActive.updateLabel(); 
		mebnEditionPane.setFormula(formula); 
	}

	private void setResidentNodeActive(ResidentNode residentNodeActive){ 
	   nodeActive = residentNodeActive; 
	   mebnEditionPane.setResidentBarActive(); 
	   mebnEditionPane.setEditArgumentsTabActive(residentNodeActive); 
	   mebnEditionPane.setResidentNodeTabActive((DomainResidentNode)residentNodeActive); 
	   mebnEditionPane.setTxtNameResident((residentNodeActive).getName()); 	
	   mebnEditionPane.setArgumentTabActive(); 	
	   this.setUnableTableEditionView(); 
	}
	
	private void setInputNodeActive(InputNode inputNodeActive){
		nodeActive = inputNodeActive; 
		mebnEditionPane.setInputBarActive(); 
		mebnEditionPane.setTxtNameInput((inputNodeActive).getName()); 				
		mebnEditionPane.setInputNodeActive((GenerativeInputNode)inputNodeActive); 
		updateInputInstanceOf((GenerativeInputNode)inputNodeActive); 
		this.setUnableTableEditionView(); 
	}
	
	private void setContextNodeActive(ContextNode contextNodeActive){
		nodeActive = contextNodeActive; 
		mebnEditionPane.setContextBarActive(); 
		mebnEditionPane.setFormulaEdtionActive(contextNodeActive); 					
		mebnEditionPane.setTxtNameContext((contextNodeActive).getName()); 					
		this.setUnableTableEditionView(); 			
	}

	private void setOrdVariableNodeActive(OrdinaryVariable ov){
		nodeActive = ov; 
		mebnEditionPane.setOrdVariableBarActive(ov); 
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
		Type type = Type.getDefaultType(); 
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
		mebnEditionPane.getEditArgumentsTab().update();
		
		
	}
	
	public void removeOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable){
		
		ResidentNode resident = (ResidentNode) screen.getGraphPane().getSelected(); 
		resident.removeArgument(ordinaryVariable);
		mebnEditionPane.getEditArgumentsTab().update(); 
		mebnEditionPane.updateUI(); 
		
	}	
	
	public void setOVariableSelectedInResidentTree(OrdinaryVariable oVariableSelected){
		mebnEditionPane.getEditArgumentsTab().setTxtName(oVariableSelected.getName()); 
		mebnEditionPane.getEditArgumentsTab().setTreeResidentActive(); 
	}
	
	public void setOVariableSelectedInMFragTree(OrdinaryVariable oVariableSelected){
		mebnEditionPane.getEditArgumentsTab().setTxtName(oVariableSelected.getName()); 
		mebnEditionPane.getEditArgumentsTab().setTreeMFragActive(); 
	}	
	
	public void renameOVariableOfResidentTree(String name){
		OrdinaryVariable ov = mebnEditionPane.getEditArgumentsTab().getResidentOVariableTree().getOVariableSelected(); 
	    ov.setName(name); 
	    ov.updateLabel(); 
		mebnEditionPane.getEditArgumentsTab().setTxtName(ov.getName()); 
		mebnEditionPane.getEditArgumentsTab().update(); 
	}
	
	public void renameOVariableOfMFragTree(String name){
		OrdinaryVariable ov = mebnEditionPane.getEditArgumentsTab().getMFragOVariableTree().getOVariableSelected(); 
	    ov.setName(name); 
	    ov.updateLabel(); 
		mebnEditionPane.getEditArgumentsTab().setTxtName(ov.getName()); 
		mebnEditionPane.getEditArgumentsTab().update(); 
	}
	
	public void renameOVariableInArgumentEditionPane(String name){
		if (mebnEditionPane.getEditArgumentsTab().isTreeResidentActive()){
			renameOVariableOfResidentTree(name); 
		}
		else{
			renameOVariableOfMFragTree(name); 
		}
	}
	
	/*---------------------------- Formulas ----------------------------*/	
		
	public void selectOVariableInEdit(OrdinaryVariable ov){
	    OVariableEditionPane editionPane = mebnEditionPane.getEditOVariableTab(); 
		editionPane.setNameOVariableSelected(ov.getName()); 
		//editionPane.setTypeOVariableSelected(ov.getType()); 
		
	}
	
	/*--------------------------------- Entidades ---------------------*/
	
	/**
	 * Adiciona uma nova entidade com o nome passado como parametro
	 * pelo usuario. O tipo da entidade sera um nome gerado automaticamente, a 
	 * partir do passado pelo usuário. 
	 */
	public ObjectEntity addObjectEntity() throws TypeException{

		
		String name = resource.getString("entityName") + ObjectEntity.getEntityNum();

		ObjectEntity objectEntity = ObjectEntity.createObjectEntity(name);
		
		return objectEntity; 
		
	}
	
	public void renameObjectEntity(ObjectEntity entity, String name) throws TypeException{
		
		entity.setName(name); 
		
	}
	
	public void removeObjectEntity(ObjectEntity entity) throws Exception{
		ObjectEntity.removeEntity(entity); 
		try{
		   Type.removeType(entity.getType());
		}
		catch(Exception e){
			
		}
	}
	
	
	
	
	
	
	
	/*-------------------Uso do PowerLoom--------------*/
	
	public void preencherKB(){

			PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
			
			for(ObjectEntity entity: ObjectEntity.getListEntity()){
				test.executeConceptDefinition(entity); 
			}
			
			for(ResidentNode resident: multiEntityBayesianNetwork.getCurrentMFrag().getResidentNodeList()){	
				test.executeRandonVariableDefinition((DomainResidentNode) resident); 
			}
			
			
			
	}
	
	public void executeContext(){
		
		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
		
		for(ContextNode context: ((DomainMFrag)(multiEntityBayesianNetwork.getCurrentMFrag())).getContextNodeList()){
			
			boolean resultado = test.executeContextFormula(context);
		    
			System.out.println("Contexto " + context.getName() + resultado); 
		}
		
	}
	
	public void makeEntityAssert(String assertComand){
		    PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
		    
		    test.executeEntityFinding(assertComand); 
		
	}
	
	public void makeRelationAssert(String assertComand){
	    PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
	    
	    test.executeRandonVariableFinding(assertComand); 
	}
	
	public void saveDefinitionsFile(){
		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 

		System.out.println("[PL] saving module"); 
		test.saveDefinitionsFile(); 
		System.out.println("[PL] file save sucefull");
    
	}
	
	/*
	public void linkOrdVariable2Entity(String nameOV, String entity){
		
		ArrayList<OrdinaryVariable> listOV = (ArrayList<OrdinaryVariable>)this.getCurrentMFrag().getOrdinaryVariableList(); 
		
		for(OrdinaryVariable ov : listOV){
			if(ov.getName().compareTo(nameOV) == 0){
				
				try{ 
				ov.setEntity(new ObjectEntity(entity, "Boolean")); //warn: o tipo aqui eh apenas para testarmos... 
				System.out.println(" -> Linkado: " + ov.getName() + " a " + entity); 
				}
				catch(Exception e){
					e.printStackTrace(); 
				}
				break; 
			}
		}
	}

*/
	
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		return multiEntityBayesianNetwork;
	}

	public void setMultiEntityBayesianNetwork(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork) {
		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
	}

	public MEBNEditionPane getMebnEditionPane() {
		return mebnEditionPane;
	}

	public void setMebnEditionPane(MEBNEditionPane mebnEditionPane) {
		this.mebnEditionPane = mebnEditionPane;
	}
}
