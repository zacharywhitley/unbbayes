/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.controller;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.exception.InvalidOperationException;
import unbbayes.gui.MEBNEditionPane;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.io.XMLIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.ObjectEntityHasInstancesException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.BottomUpSSBNGenerator;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.Debug;
import unbbayes.util.NodeList;

/**
 * Controller of the MEBN structure. 
 * 
 * All the methods of the gui classes that change the model (MEBN classes) make 
 * call a method of this controller (MVC model). 
 *
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.5 11/15/07
 */

public class MEBNController  {

	/*-------------------------------------------------------------------------*/
	/* Static attributes                                                       */
	/*-------------------------------------------------------------------------*/
	
	private KnowledgeBase knowledgeBase; 
	
	/*-------------------------------------------------------------------------*/
	/* Atributes                                                               */
	/*-------------------------------------------------------------------------*/
	
	private NetworkWindow screen;
	private MEBNEditionPane mebnEditionPane;
	private MultiEntityBayesianNetwork multiEntityBayesianNetwork;
	private ProbabilisticNetwork specificSituationBayesianNetwork; 
	
	/* the attribute below is a singleton, but we should instantiate it ASAP */
	//private KnowledgeBase knowledgeBase =  PowerLoomKB.getInstanceKB();
	
	/*-------------------------------------------------------------------------*/
	/* Control of the nodes actives                                            */
	/*-------------------------------------------------------------------------*/

	private ResidentNode residentNodeActive;
	private InputNode inputNodeActive;
	private ContextNode contextNodeActive;
	private OrdinaryVariable ovNodeActive;
	private MFrag mFragActive; 
	
	private TypeElementSelected typeElementSelected; 
	private Node nodeActive;

	/*-------------------------------------------------------------------------*/
	/* Control of Graph Active                                                 */
	/*-------------------------------------------------------------------------*/
	
	private boolean showSSBNGraph = false; 
	
	/*-------------------------------------------------------------------------*/
	/* Control of state of the kb                                            */
	/*-------------------------------------------------------------------------*/
	
	private boolean baseCreated = false; 
	private boolean findingCreated = false; 
	private boolean generativeCreated = false; 
	
	/*-------------------------------------------------------------------------*/
	/* Constants                                            */
	/*-------------------------------------------------------------------------*/
	
	private static final String NAME_GENERATIVE_FILE = "generative.plm"; 
	private static final String NAME_FINDING_FILE = "findings.plm"; 
	
	/*-------------------------------------------------------------------------*/
	/* Others (resources, utils, etc                                           */
	/*-------------------------------------------------------------------------*/

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.controller.resources.ControllerResources");

	private NumberFormat df;
	
	/*-------------------------------------------------------------------------*/
	/* Private enumerations                                                    */
	/*-------------------------------------------------------------------------*/
	
	private enum TypeElementSelected{
		NODE, 
		MFRAG, 
		MTHEORY
	}
	
	/*-------------------------------------------------------------------------*/
	/* Constructors                                                            */
	/*-------------------------------------------------------------------------*/	
	
	/**
	 * Constructor
	 * Create also the MEBNEditionPane. 
	 * 
	 * @param multiEntityBayesianNetwork
	 * @param screen
	 */
	public MEBNController(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork,
			NetworkWindow screen) {

		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
		this.screen = screen;
		this.mebnEditionPane = new MEBNEditionPane(screen, this);

		df = NumberFormat.getInstance(Locale.getDefault());
		df.setMaximumFractionDigits(4);
		
	}

	
	
	
	/*-------------------------------------------------------------------------*/
	/*                                                                         */
	/*-------------------------------------------------------------------------*/	
	
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

		typeElementSelected = TypeElementSelected.MTHEORY; 
		mebnEditionPane.setDescriptionText(multiEntityBayesianNetwork.getDescription()); 
	}

	/**
	 * Set the name of the MTheory active.
	 * @param name The new name
	 */
	public void setNameMTheory(String name){

		multiEntityBayesianNetwork.setName(name);
		mebnEditionPane.setNameMTheory(name);
		mebnEditionPane.getMTheoryTree().updateTree(); 

	}
	
	/**
	 * Set the description text of the selected object
	 * 
	 * Objects: 
	 * - MTheory
	 * - MFrag
	 * 
	 * - Resident Node
	 * - Input Node
	 * - Context Node
	 * 
	 * - Ordinary Variable
	 * 
	 * - State
	 * 
	 * - Object Entity
	 * 
	 * @param text
	 */
	public void setDescriptionTextForSelectedObject(String text){
		saveDescriptionTextOfPreviousElement(text); 	
	}
	
	/*-------------------------------------------------------------------------*/
	/* Edge                                                                    */
	/*-------------------------------------------------------------------------*/

    /**
     *  Connects a parent and its child with an edge. We must fill correctly the lists
     *  that need updates.
     *
     * @param  edge  a <code>TArco</code> which represent an edge to connect
     * @since
     */

    public void insertEdge(Edge edge) throws MEBNConstructionException, CycleFoundException, Exception{

    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag();

    	mFragCurrent.addEdge(edge);

    }

	
    
	/*-------------------------------------------------------------------------*/
	/* MFrag                                                                    */
	/*-------------------------------------------------------------------------*/

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

		MFrag domainMFrag = new MFrag(name, multiEntityBayesianNetwork);

		multiEntityBayesianNetwork.addDomainMFrag(domainMFrag);

		mebnEditionPane.getMTheoryTree().updateTree();

	    showGraphMFrag(domainMFrag);

	    mebnEditionPane.setMFragBarActive();
	    mebnEditionPane.setTxtNameMFrag(domainMFrag.getName());
	    mebnEditionPane.setMTheoryTreeActive();
	    
		typeElementSelected = TypeElementSelected.MFRAG; 
		mebnEditionPane.setDescriptionText(domainMFrag.getDescription()); 
	}

	public void removeDomainMFrag(MFrag domainMFrag) {
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

		mebnEditionPane.hideTopComponent();
		mebnEditionPane.setMFragBarActive();
		mebnEditionPane.setTxtNameMFrag(mFrag.getName());
		mebnEditionPane.setMTheoryTreeActive();
		
		typeElementSelected = TypeElementSelected.MFRAG; 
		mebnEditionPane.setDescriptionText(mFrag.getDescription()); 
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

	
	
	/*-------------------------------------------------------------------------*/
	/* Resident Node                                                           */
	/*-------------------------------------------------------------------------*/

	public ResidentNode insertDomainResidentNode(double x, double y) throws MFragDoesNotExistException {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
		}

		MFrag domainMFrag = (MFrag) currentMFrag;

		//The name of the Domain Resident Node is unique into MFrag

		String name = null;

		int residentNodeNum = domainMFrag.getDomainResidentNodeNum();

		while (name == null){
			name = resource.getString("residentNodeName") +
			                        multiEntityBayesianNetwork.getDomainResidentNodeNum();
			if(domainMFrag.getDomainResidentNodeByName(name) != null){
				name = null;
				multiEntityBayesianNetwork.plusDomainResidentNodeNum();
			}
		}
		ResidentNode node = new ResidentNode(name, domainMFrag);

		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addResidentNode(node);

		residentNodeActive = node;
		nodeActive = node;

		mebnEditionPane.setEditArgumentsTabActive(node);
		mebnEditionPane.setResidentNodeTabActive(node);
		mebnEditionPane.setArgumentTabActive();
		mebnEditionPane.setResidentBarActive();
		mebnEditionPane.setTxtNameResident(((ResidentNode)node).getName());

		mebnEditionPane.setDescriptionText(node.getDescription()); 
		typeElementSelected = TypeElementSelected.NODE; 
		
	    return node;
	}

	public void renameDomainResidentNode(ResidentNode resident, String newName)
	                                   throws DuplicatedNameException{
		if(((MFrag)mFragActive).getDomainResidentNodeByName(newName) == null){;
		   resident.setName(newName);
		   mebnEditionPane.repaint();
		}
		else{
			throw new DuplicatedNameException();
		}
	}

	
	
	/*-------------------------------------------------------------------------*/
	/* Resident Node: Possible values                                          */
	/*-------------------------------------------------------------------------*/
		
	/**
	 * Adds a possible value (state) into a resident node...
	 * @param resident
	 * @param value
	 */
	public StateLink addPossibleValue(ResidentNode resident, String nameValue){

		CategoricalStateEntity value = multiEntityBayesianNetwork.getCategoricalStatesEntityContainer().createCategoricalEntity(nameValue);
		StateLink link = resident.addPossibleValueLink(value);
		value.addNodeToListIsPossibleValueOf(resident);

		return link;

	}
	
	/**
	 * Adds a possible value (state) into a resident node. If the state already
	 * is a possible value of the resident node, nothing is made. 
	 */
	public StateLink addPossibleValue(ResidentNode resident, CategoricalStateEntity state){
		
		StateLink link = null; 
		
		if(!resident.hasPossibleValue(state)){
			link = resident.addPossibleValueLink(state);
			state.addNodeToListIsPossibleValueOf(resident);	
		}
		
		return link; 
		
	}
	
	public StateLink addObjectEntityAsPossibleValue(ResidentNode resident, ObjectEntity state){
		
		StateLink stateLink = null; 
		
		if(!resident.hasPossibleValue(state)){
			stateLink = resident.addPossibleValueLink(state);
			state.addNodeToListIsPossibleValueOf(resident);	
		}
		
		return stateLink; 
		
	}
	
	/**
	 * Verifies if a exists a possible value (in the container). 
	 * @param name
	 * @return
	 */
    public boolean existPossibleValue(String name){
		
    	//TODO uma versão decente...
		try {
			multiEntityBayesianNetwork.getCategoricalStatesEntityContainer().getCategoricalState(name);
			return true; 
		} catch (CategoricalStateDoesNotExistException e) {
			return false; 
		} 
    	
	}

	public void setGloballyExclusiveProperty(StateLink state, boolean value){
		state.setGloballyExclusive(value); 
	}

	public void addBooleanAsPossibleValue(ResidentNode resident){

		resident.addPossibleValueLink(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getTrueStateEntity());
		resident.addPossibleValueLink(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getFalseStateEntity());
		resident.addPossibleValueLink(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getAbsurdStateEntity());

	}

	/**
	 *  Adds a possible value (state) for a resident node...
	 * @param resident
	 * @param value
	 */
	public void removePossibleValue(ResidentNode resident, String nameValue){
		resident.removePossibleValueByName(nameValue);
	}

	public void removeAllPossibleValues(ResidentNode resident){
		resident.removeAllPossibleValues();
	}

	public boolean existsPossibleValue(ResidentNode resident, String nameValue){
		return resident.existsPossibleValueByName(nameValue);
	}


	public void setEnableTableEditionView(){

		mebnEditionPane.showTableEditionPane((ResidentNode)this.getResidentNodeActive());
		

	}

	public void setUnableTableEditionView(){

		mebnEditionPane.hideTopComponent();

	}

	
	
	/*-------------------------------------------------------------------------*/
	/* Input Node                                                              */
	/*-------------------------------------------------------------------------*/

	public InputNode insertGenerativeInputNode(double x, double y) throws MFragDoesNotExistException {

		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
		}

		MFrag domainMFrag = (MFrag) currentMFrag;
		InputNode node = new InputNode(resource.getString("inputNodeName") + domainMFrag.getGenerativeInputNodeNum(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addInputNode(node);

		inputNodeActive = node;
		nodeActive = node;

		mebnEditionPane.setInputBarActive();
		mebnEditionPane.setTxtNameInput(((InputNode)node).getName());
		mebnEditionPane.setInputNodeActive(node);
		mebnEditionPane.setTxtInputOf("");
		
		mebnEditionPane.setDescriptionText(node.getDescription()); 
		typeElementSelected = TypeElementSelected.NODE; 

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
	public void setInputInstanceOf(InputNode input, ResidentNode resident) throws CycleFoundException{

		input.setInputInstanceOf((ResidentNode)resident);
		mebnEditionPane.getInputNodePane().updateArgumentPane();
		mebnEditionPane.setTxtInputOf(resident.getName());
	}

	public void updateArgumentsOfObject(Object node){

		if (node instanceof InputNode){
			((InputNode)node).updateLabel();
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
	public void updateInputInstanceOf(InputNode input){

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

	
	
	/*-------------------------------------------------------------------------*/
	/* Context Node                                                            */
	/*-------------------------------------------------------------------------*/

	public ContextNode insertContextNode(double x, double y) throws MFragDoesNotExistException {

		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException(resource.getString("withoutMFrag"));
		}

		MFrag domainMFrag = (MFrag) currentMFrag;
		ContextNode node = new ContextNode(resource.getString("contextNodeName") + domainMFrag.getContextNodeNum(), domainMFrag);
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addContextNode(node);

		contextNodeActive = node;
		nodeActive = node;

		setContextNodeActive(node);

		mebnEditionPane.setDescriptionText(node.getDescription()); 
		typeElementSelected = TypeElementSelected.NODE; 
		
		return node;
	}


	
	/*-------------------------------------------------------------------------*/
	/* Graph                                                                   */
	/*-------------------------------------------------------------------------*/

	public void deleteSelected(Object selected) {
        if (selected instanceof ContextNode){
            ((ContextNode)selected).delete();
            mebnEditionPane.getMTheoryTree().updateTree();
            mebnEditionPane.setMTheoryTreeActive();
        }
        else{

        	if (selected instanceof ResidentNode){
                ((ResidentNode)selected).delete();
                mebnEditionPane.getMTheoryTree().updateTree();
                mebnEditionPane.setMTheoryTreeActive();
        		this.setUnableTableEditionView();
        	}
        	else{
            	if (selected instanceof InputNode){
                    ((InputNode)selected).delete();
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

	public void selectNode(Node node){
		
		//Before select the new node, save the description of the previous
		
		saveDescriptionTextOfPreviousElement(mebnEditionPane.getDescriptionText());
		
		typeElementSelected = TypeElementSelected.NODE; 
		
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
						ovNodeActive = (OrdinaryVariable)node;
						setOrdVariableNodeActive((OrdinaryVariable)node);
					}
				}
			}

		}
	    mebnEditionPane.showTitleGraph(multiEntityBayesianNetwork.getCurrentMFrag().getName());
	    mebnEditionPane.setDescriptionText(node.getDescription()); 
	}

	private void saveDescriptionTextOfPreviousElement(String text) {
		switch(typeElementSelected){
		case MFRAG:
			mFragActive.setDescription(text); 
			break; 
		case MTHEORY:
			multiEntityBayesianNetwork.setDescription(text); 
			break; 
		case NODE:
			if(nodeActive!=null){
				nodeActive.setDescription(text); 
			}
			break; 
		}
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
	   mebnEditionPane.setResidentNodeTabActive((ResidentNode)residentNodeActive);
	   mebnEditionPane.setTxtNameResident((residentNodeActive).getName());
	   mebnEditionPane.setArgumentTabActive();
	   
	   if(mebnEditionPane.isTableEditionPaneShow()){
		   mebnEditionPane.showTableEditionPane((ResidentNode)residentNodeActive);
	   }
	}

	private void setInputNodeActive(InputNode inputNodeActive){
		nodeActive = inputNodeActive;
		mebnEditionPane.setInputBarActive();
		mebnEditionPane.setTxtNameInput((inputNodeActive).getName());
		mebnEditionPane.setInputNodeActive((InputNode)inputNodeActive);
		updateInputInstanceOf((InputNode)inputNodeActive);
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


	
	/*-------------------------------------------------------------------------*/
	/* Ordinary Variable                                                       */
	/*-------------------------------------------------------------------------*/

	public OrdinaryVariable insertOrdinaryVariable(double x, double y) throws MFragDoesNotExistException {

		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			throw new MFragDoesNotExistException();
		}

		MFrag domainMFrag = (MFrag) currentMFrag;
		String name = resource.getString("ordinaryVariableName") + domainMFrag.getOrdinaryVariableNum();
		Type type = TypeContainer.getDefaultType();
		OrdinaryVariable ov = new OrdinaryVariable(name, type, domainMFrag);

		ov.setPosition(x, y);
		ov.setDescription(ov.getName());
		domainMFrag.addOrdinaryVariable(ov);

		ovNodeActive = ov;
		setOrdVariableNodeActive(ov);

		mebnEditionPane.setEditOVariableTabActive();

		mebnEditionPane.setDescriptionText(ov.getDescription()); 
		typeElementSelected = TypeElementSelected.NODE; 
		
	    return ov;

	}

	/**
	 * Create a ordinary variable and add it in the
	 * current MFrag (if it is a DomainMFrag).
	 *
	 */

	public OrdinaryVariable addNewOrdinaryVariableInMFrag(){

		MFrag domainMFrag = (MFrag) multiEntityBayesianNetwork.getCurrentMFrag();

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

	
	
	/*-------------------------------------------------------------------------*/
	/* Object Entities                                                         */
	/*-------------------------------------------------------------------------*/

	/**
	 * Adds a new entity with a name passed as its argument.
	 * The entity type will be an automatically generated type, based on
	 * what the user has passed as its argument.
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

	/**
	 * Rename a object entity. 
	 * @param entity
	 * @param name
	 * @throws TypeAlreadyExistsException
	 */
	public void renameObjectEntity(ObjectEntity entity, String name) throws TypeAlreadyExistsException{
		entity.setName(name);
	}
	
	
    /**
    * Remove a object entity. 
    * @param entity
    * @throws Exception
    */
	public void removeObjectEntity(ObjectEntity entity) throws Exception{
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntity(entity);
		try{
			multiEntityBayesianNetwork.getTypeContainer().removeType(entity.getType());
		}
		catch(Exception e){

		}
	}

	/**
	 * Set the property isOrdereable of the entity
	 * @param entity
	 * @param isOrdereable
	 * @throws ObjectEntityHasInstancesException
	 */
	public void setIsOrdereableObjectEntityProperty(ObjectEntity entity, boolean isOrdereable) throws ObjectEntityHasInstancesException{
		entity.setOrdereable(isOrdereable); 
	}
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Object Entities Instances                                               */
	/*-------------------------------------------------------------------------*/

	/**
	 * Create a new Object Entity Instance of the Object Entity. 
	 * @param entity
	 * @param nameInstance
	 * @throws EntityInstanceAlreadyExistsException
	 */
	public void createEntityIntance(ObjectEntity entity, String nameInstance) 
	throws EntityInstanceAlreadyExistsException, InvalidOperationException{

		if(entity.isOrdereable()){
			throw new InvalidOperationException();
		}
		
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
	
	public void createEntityIntanceOrdereable(ObjectEntity entity, 
			String nameInstance, ObjectEntityInstanceOrdereable previous) 
	throws EntityInstanceAlreadyExistsException, InvalidOperationException{

		if(!entity.isOrdereable()){
			throw new InvalidOperationException();
		}
		
		if(multiEntityBayesianNetwork.getObjectEntityContainer().getEntityInstanceByName(nameInstance)!=null){
			throw new EntityInstanceAlreadyExistsException();
		}
		else{
			try {
				ObjectEntityInstanceOrdereable instance = (ObjectEntityInstanceOrdereable)entity.addInstance(nameInstance);
				
				instance.setPrev(previous);
				if(previous != null){
				   previous.setProc(instance);
				}
				
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

	public void removeEntityInstance(ObjectEntityInstance entity) {
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(entity);
	}
	
	public void removeEntityInstanceOrdereable(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.removeEntityInstanceOrdereableReferences(entity);	
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(entity);
	}

	public void upEntityInstance(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.upEntityInstance(entity);
	}

	public void downEntityInstance(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.downEntityInstance(entity);
	}
	
	
	
	/*-------------------------------------------------------------------------*/
	/*Findings                                                                 */
	/*-------------------------------------------------------------------------*/
	
	public void createRandonVariableFinding(ResidentNode residentNode, 
			ObjectEntityInstance[] arguments, Entity state){
		
		RandomVariableFinding finding = new RandomVariableFinding(
				residentNode, 
				arguments, 
				state, 
				this.multiEntityBayesianNetwork);
		
		residentNode.addRandonVariableFinding(finding); 
	}
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Edition of CPT's                                                         */
	/*-------------------------------------------------------------------------*/
	
	public void saveCPT(ResidentNode residentNode, String cpt){
		residentNode.setTableFunction(cpt);
	}
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Knowledge Base                                                          */
	/*-------------------------------------------------------------------------*/

	private KnowledgeBase getKnowledgeBase(){
		
	    if(knowledgeBase == null){
	    	//TODO put a screen of wait if the eagle loader don't was active
	    	mebnEditionPane.getGraphPanel().setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
	    	knowledgeBase = PowerLoomKB.getNewInstanceKB(); 
	    	mebnEditionPane.getGraphPanel().setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
	    }
	    return knowledgeBase; 
	    
	}
	
	/**
	 * Insert the MEBN Generative into KB.
	 * (Object Entities and Domain Resident Nodes)
	 */
	private void loadGenerativeMEBNIntoKB(){
		
		KnowledgeBase knowledgeBase = getKnowledgeBase();

		for(ObjectEntity entity: multiEntityBayesianNetwork.getObjectEntityContainer().getListEntity()){
			knowledgeBase.createEntityDefinition(entity);
		}

		for(MFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				knowledgeBase.createRandomVariableDefinition(resident);
			}
		}
		
		this.saveGenerativeMTheory(new File(MEBNController.NAME_GENERATIVE_FILE)); 
	}
	
	/**
	 * Insert the findings into KB.
	 */
	private void loadFindingsIntoKB(){
		
		KnowledgeBase knowledgeBase = getKnowledgeBase();		
		
		for(ObjectEntityInstance instance: multiEntityBayesianNetwork.getObjectEntityContainer().getListEntityInstances()){
			 knowledgeBase.insertEntityInstance(instance); 
		}
		
		for(MFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
			for(ResidentNode residentNode : mfrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandonVariableFindingList()){
					knowledgeBase.insertRandomVariableFinding(finding); 
				}
			}
		}
		
		this.saveDefaultTemporaryFindingsFile(); 
	}

	public void clearKnowledgeBase(){
		getKnowledgeBase().clearKnowledgeBase();
	}
	
	public void saveGenerativeMTheory(File file){
		getKnowledgeBase().saveGenerativeMTheory(getMultiEntityBayesianNetwork(), file);
	}

	public void saveFindingsFile(File file){
		createKnowledgeBase(); 	
		getKnowledgeBase().saveFindings(getMultiEntityBayesianNetwork(), file);
	}
	
	private void saveDefaultTemporaryFindingsFile() {
		getKnowledgeBase().saveFindings(getMultiEntityBayesianNetwork(), new File(MEBNController.NAME_FINDING_FILE));
	}

	public void loadFindingsFile(File file) throws MEBNException{
		Exception lastException = null;
		createKnowledgeBase(); 	
		getKnowledgeBase().loadModule(file);
		for (ResidentNode resident : this.multiEntityBayesianNetwork.getDomainResidentNodes()) {
			Debug.println(this.getClass(), "Loading finding of: " + resident.getName());
			try {
				 this.knowledgeBase.fillFindings(resident);
			 } catch (Exception e) {
				 e.printStackTrace();
				 lastException = e;
				 continue;
			 }
		}
		if (lastException != null) {
			throw new MEBNException(lastException);
		}
	}
	
	private void createKnowledgeBase(){
		loadGenerativeMEBNIntoKB(); 
		loadFindingsIntoKB(); 
//		baseCreated = true; 
	}

	/**
	 * Execute a query. 
	 * 
	 * @param residentNode
	 * @param arguments
	 * @return
	 * @throws InconsistentArgumentException
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 */
	public ProbabilisticNetwork executeQuery(ResidentNode residentNode, ObjectEntityInstance[] arguments)
	                           throws InconsistentArgumentException, SSBNNodeGeneralException, 
	                                  ImplementationRestrictionException, MEBNException {
		
		ProbabilisticNetwork probabilisticNetwork = null; 
		
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode); 
		
		List<Argument> arglist = residentNode.getArgumentList();
		
		if (arglist.size() != arguments.length) {
			throw new InconsistentArgumentException();
		}
		
		for (int i = 1; i <= arguments.length; i++) {
			try {
				//TODO It has to get in the right order. For some reason in argList, sometimes the second argument comes first
				for (Argument argument : arglist) {
					if (argument.getArgNumber() == i) {
						queryNode.addArgument(argument.getOVariable(), arguments[i-1].getName());
						break;
					}
				}
				
			} catch (SSBNNodeGeneralException e) {
				throw new InconsistentArgumentException(e);
			}
		}
		
//		if(!baseCreated){
	    	createKnowledgeBase(); 	
//	    }
		
//		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
//		kb.loadModule(new File(BottomUpSSBNGeneratorTest.KB_GENERATIVE_FILE)); 
//		kb.loadModule(new File(BottomUpSSBNGeneratorTest.KB_FINDING_FILE)); 
		
		Query query = new Query(getKnowledgeBase(), queryNode, multiEntityBayesianNetwork);
		
		ISSBNGenerator ssbngenerator = new BottomUpSSBNGenerator(getKnowledgeBase());
		
			probabilisticNetwork = ssbngenerator.generateSSBN(query);
			
			if(!query.getQueryNode().isFinding()){
				XMLIO netIO = new XMLIO(); 

				try {
					netIO.save(new File("ssbn.xml"), probabilisticNetwork);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JAXBException e) {
					e.printStackTrace();
				}

				if(this.compileNetwork(probabilisticNetwork)){
					showSSBNGraph = true; 
					specificSituationBayesianNetwork = probabilisticNetwork;
					this.getMebnEditionPane().getNetworkWindow().changeToSSBNCompilationPane(specificSituationBayesianNetwork);
				}
			}else{
				JOptionPane.showMessageDialog(getScreen(), 
						query.getQueryNode().toString() + " = " + query.getQueryNode().getActualValues().toArray()[0]);
			}
			
		
		return specificSituationBayesianNetwork ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
		
	}

	
	
	
	
	/*--------------------------------------------------------------------------
	 * ATENÇÃO: ESTES MÉTODOS SÃO CÓPIAS DOS MÉTODOS PRESENTES EM SENCONTROLLER...
	 * DEVIDO A FALTA DE TEMPO, AO INVÉS DE FAZER UM REFACTORY PARA COLOCÁLOS NO
	 * NETWORKCONTROLLER, DEIXANDO ACESSIVEL AO SENCONTROLLER E AO MEBNCONTROLLER, 
	 * VOU APENAS ADAPTÁLOS AQUI PARA O USO NO MEBNCONTROLLER... MAS DEPOIS ISTO
	 * NECESSITARÁ DE UM REFACTORY PARA MANTER AS BOAS PRÁTICAS DA PROGRAMAÇÃO 
	 * E PARA FACILITAR A MANUTENÇÃO. (laecio santos)
	 *--------------------------------------------------------------------------/
	
		/** Load resource file from this package */
	private static ResourceBundle resourcePN = ResourceBundle
			.getBundle("unbbayes.controller.resources.ControllerResources");
	
	/**
	 * Compiles the bayesian network. If there was any problem during compilation, the error
	 * message will be shown as a <code>JOptionPane</code> .
	 * 
	 * @return true if the net was compiled without any problem, false if there was a problem
	 * @since
	 * @see JOptionPane
	 */
	public boolean compileNetwork(ProbabilisticNetwork network) {
		long ini = System.currentTimeMillis();
		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			network.compile();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), resourcePN
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			return false;
		}

		// Order by node description just to make tree's visualization easy.
		NodeList nos = network.getNodesCopy();
		boolean haTroca = true;
		while (haTroca) {
			haTroca = false;
			for (int i = 0; i < nos.size() - 1; i++) {
				Node node1 = nos.get(i);
				Node node2 = nos.get(i + 1);
				if (node1.getDescription().compareToIgnoreCase(
						node2.getDescription()) > 0) {
					nos.set(i + 1, node1);
					nos.set(i, node2);
					haTroca = true;
				}
			}
		}

		/* isto será feito dentro do changeToSSBNCompilationPane */
//		screen.getEvidenceTree().updateTree();  hehe... ainda não temos uma evidence tree... sorry!
		

		screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		//TODO controle of status for the mebn edition pane 
//		screen.setStatus(resource.getString("statusTotalTime")
//				+ df.format(((System.currentTimeMillis() - ini)) / 1000.0)
//				+ resource.getString("statusSeconds"));
		return true;

	}
	
	/**
	 * Initializes the junction tree's known facts
	 */
	public void initialize() {
		try {
		    specificSituationBayesianNetwork.initialize();
			screen.getEvidenceTree().updateTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Propagates the bayesian network's evidences ( <code>TRP</code> ).
	 * 
	 * @since
	 */
	public void propagate() {
		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		boolean temLikeliHood = false;
		try {
			specificSituationBayesianNetwork.updateEvidences();
			if (!temLikeliHood) {
				screen.setStatus(resourcePN
						.getString("statusEvidenceProbabilistic")
						+ df.format(specificSituationBayesianNetwork.PET() * 100.0));
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(screen, e.getMessage(), resourcePN
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
		}
		screen.getEvidenceTree().updateTree();
		screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	
	
	/*--------------------------------------------------------------------------
	 * FIM DOS MÉTODOS CÓPIA
	 *-------------------------------------------------------------------------/
	
	
	
	
	
	/**
	 * @return false if don't have one ssbn pre-generated. True if the mode is change. 
	 */
	public boolean turnToSSBNMode(){
		if(specificSituationBayesianNetwork != null){
			showSSBNGraph = true; 
			this.getMebnEditionPane().getNetworkWindow().changeToSSBNCompilationPane(specificSituationBayesianNetwork);			
		    return true;  
		}else{
			return false;
		}
	}
	
	
	/*-------------------------------------------------------------------------*/
	/* Get's e Set's                                                           */
	/*-------------------------------------------------------------------------*/

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

	public NetworkWindow getScreen() {
		return screen;
	}

	public void setScreen(NetworkWindow screen) {
		this.screen = screen;
	}

	public ProbabilisticNetwork getSpecificSituationBayesianNetwork() {
		return specificSituationBayesianNetwork;
	}

	public void setSpecificSituationBayesianNetwork(
			ProbabilisticNetwork specificSituationBayesianNetwork) {
		this.specificSituationBayesianNetwork = specificSituationBayesianNetwork;
	}

	public boolean isShowSSBNGraph() {
		return showSSBNGraph;
	}

	public void setShowSSBNGraph(boolean showSSBNGraph) {
		this.showSSBNGraph = showSSBNGraph;
	}

	public void setEditionMode(){
		showSSBNGraph = false; 
	}
	
}
