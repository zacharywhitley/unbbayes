package unbbayes.controller;

import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import unbbayes.gui.MEBNEditionPane;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.finding.EntityFindingEditionPane;
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
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.util.Debug;

/**
 * Controller of the MEBN structure. 
 * 
 * Utilizado pelas classes de GUI para fazer solicitar altera��es na 
 * estrutura do MEBN. Faz as atualiza��es necess�rias nas telas ap�s a 
 * a��o sobre a estrutura do MEBN ser atualizada. 
 *   
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 05/29/07
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
	private MFrag mFragActive; /* The MFrag active in the view */
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
	
	/**
	 * Set the name of the MTheory active. 
	 * @param name The new name
	 */
	public void setNameMTheory(String name){
		
		multiEntityBayesianNetwork.setName(name);
		mebnEditionPane.setNameMTheory(name); 
		
	}
	
	/*---------------------------- Edge ---------------------------*/
	
    /**
     *  Faz a ligac�o do arco desejado entre pai e filho. Deve preencher de forma
     *  correta as listas que precisam ser atualizadas. 
     *
     * @param  edge  um <code>TArco</code> que representa o arco a ser ligado
     * @since
     */
	
    public void insertEdge(Edge edge) throws MEBNConstructionException, CycleFoundException, Exception{
    	
    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag(); 
    	
    	((DomainMFrag)mFragCurrent).addEdge(edge);
    	
    }
	
	
	/*---------------------------- MFrag ----------------------------*/
	
	public void insertDomainMFrag() {
		
		//The name of the MFrag is unique
		
		String name = null; 
        
		int domainMFragNum = multiEntityBayesianNetwork.getDomainMFragNum(); 
		
		while (name == null){
			name = name = resource.getString("domainMFragName") + multiEntityBayesianNetwork.getDomainMFragNum(); 
			if(multiEntityBayesianNetwork.getMFragByName(name) != null){
				name = null;
				multiEntityBayesianNetwork.setDomainMFragNum(++domainMFragNum); 
			}
		}
		
		DomainMFrag domainMFrag = new DomainMFrag(name, multiEntityBayesianNetwork); 
		
		multiEntityBayesianNetwork.addDomainMFrag(domainMFrag); 
		
		mebnEditionPane.getMTheoryTree().updateTree();
	    
	    showGraphMFrag(domainMFrag); 
	    
	    mebnEditionPane.setMFragBarActive(); 
	    mebnEditionPane.setTxtNameMFrag(domainMFrag.getName()); 	    
	    mebnEditionPane.setMTheoryTreeActive(); 
		
	}
	
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
		if(mFragActive != domainMFrag){
			multiEntityBayesianNetwork.setCurrentMFrag(mFragActive); 
		}
		else{
		    if(multiEntityBayesianNetwork.getDomainMFragList().size() != 0){
		    	mFragActive = multiEntityBayesianNetwork.getDomainMFragList().get(0); 
			    showGraphMFrag(mFragActive); 	    
		    }
		    else{
		       showGraphMFrag(); 
		    }
		}
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
	public void renameMFrag(MFrag mFrag, String name) throws DuplicatedNameException{
			
           if (multiEntityBayesianNetwork.getMFragByName(name) != null){
        	   throw new DuplicatedNameException(); 
           }
		    
		    mFrag.setName(name);
			
			if(this.getCurrentMFrag() == mFrag){
				mebnEditionPane.showTitleGraph(name); 
			}
	}
	

	
	/**
	 * Show the graph of the MFrag and select it how active MFrag. 
	 * 
	 * @param mFrag
	 */
	public void showGraphMFrag(MFrag mFrag){
		
		multiEntityBayesianNetwork.setCurrentMFrag(mFrag); 
	    screen.getGraphPane().resetGraph(); 
	    mebnEditionPane.showTitleGraph(mFrag.getName()); 
	    mFragActive = mFrag; 
	    
	}
	
	/**
	 * Show a empty MFrag graph. 
	 * Use when don't have any MFrag in the MTheory. 
	 *
	 */
	public void showGraphMFrag(){

		multiEntityBayesianNetwork.setCurrentMFrag(null); 
		screen.getGraphPane().showEmptyGraph();
		mebnEditionPane.hideTopComponent();
		mebnEditionPane.setEmptyBarActive(); 
		mFragActive = null; 
		
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
		Type type = TypeContainer.getDefaultType(); 
		OrdinaryVariable ov = new OrdinaryVariable(name, type, domainMFrag);
		
		ov.setPosition(x, y);
		ov.setDescription(ov.getName());
		domainMFrag.addOrdinaryVariable(ov);
		
		ordVariableNodeActive = ov; 
		setOrdVariableNodeActive(ov); 
		
		mebnEditionPane.setEditOVariableTabActive(); 
		
	    return ov;
	    
	}
	
	/*---------------------------- Domain Resident Node ----------------------------*/	
	
	public DomainResidentNode insertDomainResidentNode(double x, double y) throws MFragDoesNotExistException {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
		}
		
		DomainMFrag domainMFrag = (DomainMFrag) currentMFrag;
		
		//The name of the Domain Resident Node is unique into MFrag
		
		String name = null; 
        
		int residentNodeNum = domainMFrag.getDomainResidentNodeNum(); 
		
		while (name == null){
			name = name = resource.getString("residentNodeName") + 
			                        multiEntityBayesianNetwork.getDomainResidentNodeNum(); 
			if(domainMFrag.getDomainResidentNodeByName(name) != null){
				name = null;
				multiEntityBayesianNetwork.plusDomainResidentNodeNum(); 
			}
		}
		DomainResidentNode node = new DomainResidentNode(name, domainMFrag);
		
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
	
	public void renameDomainResidentNode(DomainResidentNode resident, String newName)
	                                   throws DuplicatedNameException{
		if(((DomainMFrag)mFragActive).getDomainResidentNodeByName(newName) == null){; 
		   resident.setName(newName);	
		   mebnEditionPane.repaint();
		}
		else{
			throw new DuplicatedNameException(); 
		}
	}
	
	/**
	 * Adiciona um possivel valor (estado) no nodo resident... 
	 * @param resident
	 * @param value
	 */
	public void addPossibleValue(DomainResidentNode resident, String nameValue){
		
		CategoricalStatesEntity value = multiEntityBayesianNetwork.getCategoricalStatesEntityContainer().createCategoricalEntity(nameValue); 
		resident.addPossibleValue(value); 
		value.addNodeToListIsPossibleValueOf(resident); 
				
	}
	
	public void addBooleanAsPossibleValue(DomainResidentNode resident){
	
		resident.addPossibleValue(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getTrueStateEntity());
		resident.addPossibleValue(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getFalseStateEntity());
		resident.addPossibleValue(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getAbsurdStateEntity()); 
		
	}
	
	/**
	 * Adiciona um possivel valor (estado) no nodo resident... 
	 * @param resident
	 * @param value
	 */
	public void removePossibleValue(DomainResidentNode resident, String nameValue){
		resident.removePossibleValueByName(nameValue); 	
	}	
	
	public void removeAllPossibleValues(DomainResidentNode resident){
		resident.removeAllPossibleValues(); 	
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
	
	/**
	 * Set the input node for be a instance of a resident node. 
	 * Update the graph. 
	 * 
	 * @param input
	 * @param resident
	 * @throws CycleFoundException
	 */
	public void setInputInstanceOf(GenerativeInputNode input, ResidentNode resident) throws CycleFoundException{
		
		input.setInputInstanceOf((DomainResidentNode)resident);
		mebnEditionPane.getInputNodePane().updateArgumentPane(); 
		mebnEditionPane.setTxtInputOf(resident.getName()); 
	}
	
	public void updateArgumentsOfObject(Object node){
		
		if (node instanceof GenerativeInputNode){
			((GenerativeInputNode)node).updateLabel(); 
		}else{
			if(node instanceof ContextNode){
				((ContextNode)node).updateLabel(); 
			}
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
                     screen.getGraphPane().update(); 
                     mebnEditionPane.setMTheoryTreeActive(); 
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
	
	public void unselectNodes(){
		if(multiEntityBayesianNetwork.getCurrentMFrag() != null){
	       mebnEditionPane.setMFragBarActive(); 
	       mebnEditionPane.setTxtNameMFrag(multiEntityBayesianNetwork.getCurrentMFrag().getName()); 	    
	       mebnEditionPane.setMTheoryTreeActive(); 
		}
		else{
			//The program still is in the MTheory screen edition
		}
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
		mebnEditionPane.setEditOVariableTabActive(); 
	}
	
	/*---------------------------- Ordinary Variable ----------------------------*/	
		
	/**
	 * Create a ordinary variable and add it in the
	 * current MFrag (if it is a DomainMFrag). 
	 * 
	 */
	
	public OrdinaryVariable addNewOrdinaryVariableInMFrag(){

		DomainMFrag domainMFrag = (DomainMFrag) multiEntityBayesianNetwork.getCurrentMFrag();
		
		String name = null; 
        
		int ordinaryVariableNum = domainMFrag.getOrdinaryVariableNum(); 
		
		while (name == null){
			name = resource.getString("ordinaryVariableName") + domainMFrag.getOrdinaryVariableNum(); 
			if(domainMFrag.getOrdinaryVariableByName(name) != null){
				name = null;
				domainMFrag.setOrdinaryVariableNum(++ordinaryVariableNum); 
			}
		}
		
		Type type = multiEntityBayesianNetwork.getTypeContainer().getDefaultType(); 
		
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
		ordinaryVariable.removeIsOVariableOfList(resident);
		
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
	
	@Deprecated
	public void renameOVariableOfResidentTree(String name){
		OrdinaryVariable ov = mebnEditionPane.getEditArgumentsTab().getResidentOVariableTree().getOVariableSelected(); 
	    ov.setName(name); 
	    ov.updateLabel(); 
		mebnEditionPane.getEditArgumentsTab().setTxtName(ov.getName()); 
		mebnEditionPane.getEditArgumentsTab().update(); 
	}
	
	@Deprecated
	public void renameOVariableOfMFragTree(String name){
		OrdinaryVariable ov = mebnEditionPane.getEditArgumentsTab().getMFragOVariableTree().getOVariableSelected(); 
	    ov.setName(name); 
	    ov.updateLabel(); 
		mebnEditionPane.getEditArgumentsTab().setTxtName(ov.getName()); 
		mebnEditionPane.getEditArgumentsTab().update(); 
	}
	
	@Deprecated
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
	
	/*--------------------------------- Object Entities ---------------------*/
	
	/**
	 * Adiciona uma nova entidade com o nome passado como parametro
	 * pelo usuario. O tipo da entidade sera um nome gerado automaticamente, a 
	 * partir do passado pelo usu�rio. 
	 */
	public ObjectEntity createObjectEntity() throws TypeException{

		String name = null; 
        
		int entityNum = multiEntityBayesianNetwork.getObjectEntityContainer().getEntityNum();
		
		while (name == null){
			name = resource.getString("entityName") + 
			            multiEntityBayesianNetwork.getObjectEntityContainer().getEntityNum();
			if(multiEntityBayesianNetwork.getObjectEntityContainer().getObjectEntityByName(name) != null){
				name = null;
				multiEntityBayesianNetwork.getObjectEntityContainer().setEntityNum(++entityNum); 
			}
		}
		
		ObjectEntity objectEntity = multiEntityBayesianNetwork.getObjectEntityContainer().createObjectEntity(name);
		
		return objectEntity; 
	}
	
	public void renameObjectEntity(ObjectEntity entity, String name) throws TypeAlreadyExistsException{
		
		entity.setName(name); 
		
	}
	
	public void removeObjectEntity(ObjectEntity entity) throws Exception{
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntity(entity); 
		try{
			multiEntityBayesianNetwork.getTypeContainer().removeType(entity.getType());
		}
		catch(Exception e){
			
		}
	}
	
	public void createEntityIntance(ObjectEntity entity, String nameInstance) throws EntityInstanceAlreadyExistsException{
		
		if(multiEntityBayesianNetwork.getObjectEntityContainer().getEntityInstanceByName(nameInstance)!=null){
			throw new EntityInstanceAlreadyExistsException(); 
		}
		else{
			try {
				ObjectEntityInstance instance = entity.addInstance(nameInstance);
				multiEntityBayesianNetwork.getObjectEntityContainer().addEntityInstance(instance); 
			} catch (TypeException e1) {
				e1.printStackTrace();
			} catch(EntityInstanceAlreadyExistsException e){
				e.printStackTrace();
			}
		}
	}
	
	public void renameEntityIntance(ObjectEntityInstance entity, String newName) throws EntityInstanceAlreadyExistsException{
		
		if(multiEntityBayesianNetwork.getObjectEntityContainer().getEntityInstanceByName(newName)!=null){
			throw new EntityInstanceAlreadyExistsException(); 
		}
		else{
			entity.setName(newName); 
		}
	}
	
	/*-------------------Uso do PowerLoom--------------*/
	
	/**
	 * Insert the MEBN Generative into KB. 
	 * (Object Entities and Domain Resident Nodes) 
	 */
	public void preencherKB(){

		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
		
		for(ObjectEntity entity: multiEntityBayesianNetwork.getObjectEntityContainer().getListEntity()){
			test.executeConceptDefinition(entity); 
		}
		
		for(DomainMFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getDomainResidentNodeList()){	
				test.executeRandonVariableDefinition((DomainResidentNode)resident); 
			}
		}
		
		EntityFindingEditionPane entityPane = new EntityFindingEditionPane(this);
		JFrame frameTest = new JFrame(); 
		frameTest.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		frameTest.setContentPane(entityPane); 
		frameTest.pack(); 
		frameTest.setVisible(true); 
	}
	
	/**
	 * Execute the list of context nodes of the current MFrag. 
	 * (this version only print the result in console)
	 */
	public void executeContext(){
		
		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
		
		for(ContextNode context: ((DomainMFrag)(multiEntityBayesianNetwork.getCurrentMFrag())).getContextNodeList()){
			
			boolean resultado = test.executeContextFormula(context);
		    
			Debug.println(this.getClass(), "Contexto " + context.getName() + "=" + resultado); 
		}
		
	}
	
	/**
	 * Put a new assert of a entity in the KB. 
	 * 
	 * Sintaxe PowerLoom: 
	 * (assert (Starship Enterprise))
	 * 
	 * @param assertComand Assert in powerloom sintaxe
	 */
	public void makeEntityAssert(String assertComand){
		    PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
		    
		    test.executeEntityFinding(assertComand); 
		
	}
	
	/**
	 * Put a new relation assert in the KB. 
	 * 
	 * Sintaxe PowerLoom: 
	 * (assert(= (StarshipZone(Enterprise))  ZN_BlackHoleBoundary))
	 * 
	 * @param assertComand Assert in powerloom sintaxe
	 */	
	public void makeRelationAssert(String assertComand){
	    PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
	    
	    test.executeRandonVariableFinding(assertComand); 
	}
	
	public void saveDefinitionsFile(String fileName){
		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 

		Debug.println(this.getClass(), "[PowerLoom] Saving module..."); 
		test.saveDefinitionsFile(fileName + ".plm"); 
		Debug.println(this.getClass(), "[PowerLoom] ...File save sucefull");
    
	}
	
	public void loadDefinitionsFile(String fileName){
		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 

		Debug.println(this.getClass(), "[PowerLoom] Loading module..."); 
		test.loadDefinitionsFile(fileName + ".plm"); 
		Debug.println(this.getClass(), "[PowerLoom] ...File load sucefull");
    
	}
	
	/**
	 *Apenas de teste...
	 *
	 * Preenche uma vari�vel ordin�ria com uma entidade... 
	 * 
	 * @param nameOV Nome da OV que ser� linkada (jah deve existir)
	 * @param entity Entidade a ser criada e linkada. 
	 */
	public void linkOrdVariable2Entity(String nameOV, String entity){
		//TODO tirar este metodo da versao final
		ArrayList<OrdinaryVariable> listOV = (ArrayList<OrdinaryVariable>)this.getCurrentMFrag().getOrdinaryVariableList(); 
		
		for(OrdinaryVariable ov : listOV){
			if(ov.getName().compareTo(nameOV) == 0){
				
				try{ 
					ObjectEntity oe = multiEntityBayesianNetwork.getObjectEntityContainer().createObjectEntity(entity); 
					ov.setEntity(oe); //warn: o tipo aqui eh apenas para testarmos... 
					Debug.println(this.getClass(), " Linkado: " + ov.getName() + " a " + entity); 
				}
				catch(TypeException e){
					e.printStackTrace(); 
				}
				catch(Exception e){
					e.printStackTrace(); 
				}
				break; 
			}
		}
	}
	
	public String executeCommand(String command){
		PowerLoomKB test = PowerLoomKB.getInstanceKB(); 
		return test.executeCommand(command); 
	}
	
	
	
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
