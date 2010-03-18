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
package unbbayes.controller.mebn;

import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import unbbayes.controller.NetworkController;
import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.exception.InvalidOperationException;
import unbbayes.gui.GraphAction;
import unbbayes.gui.mebn.DescriptionPane;
import unbbayes.gui.mebn.MEBNEditionPane;
import unbbayes.gui.mebn.MEBNNetworkWindow;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.WarningPanel;
import unbbayes.gui.mebn.cpt.CPTFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.extension.jpf.PluginAwareFileExtensionIODelegator;
import unbbayes.io.log.ILogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
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
import unbbayes.prs.mebn.exception.ReservedWordException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNWarning;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;
import unbbayes.util.GraphLayoutUtil;

/**
 * Controller of the MEBN structure. 
 * 
 * All the methods of the gui classes that change the model (MEBN classes) make 
 * call a method of this controller (MVC model). 
 *
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.5 11/15/07
 * 
 * @author Shou Matsumoto
 * @version 02/13/2010 - Migrated part of NetworkController's routines to here
 */

public class MEBNController extends NetworkController {

	/** if set to true, this will log every nodes of SSBN and its probability values */
	private boolean toLogNodesAndProbabilities = true;
	
	/*-------------------------------------------------------------------------*/
	/*                                                      */
	/*-------------------------------------------------------------------------*/
	
	private KnowledgeBase knowledgeBase; 
	private MEBNFactory mebnFactory; 
	
	/*-------------------------------------------------------------------------*/
	/* Atributes                                                               */
	/*-------------------------------------------------------------------------*/
	
//	private MEBNNetworkWindow screen;
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
	/* Painels under controll                                                  */
	/*-------------------------------------------------------------------------*/
	private JDialog warningDialog; 
	
	/*-------------------------------------------------------------------------*/
	/* Control of Graph Active                                                 */
	/*-------------------------------------------------------------------------*/
	
	private boolean showSSBNGraph = false; 
	private Dimension dimensionSSBNGraph = new Dimension(1500, 1500); 
	
	/*-------------------------------------------------------------------------*/
	/* Control of state of the kb                                            */
	/*-------------------------------------------------------------------------*/
	
	private boolean baseCreated = false; 
	private boolean findingCreated = false; 
	private boolean generativeCreated = false; 

	
	/*-------------------------------------------------------------------------*/
	/* Pools of frames                                                         */
	/*-------------------------------------------------------------------------*/
	private HashMap<ResidentNode, CPTFrame> mapCpt = 
		new HashMap<ResidentNode, CPTFrame>(); 
	
	
	/*-------------------------------------------------------------------------*/
	/* Constants                                            */
	/*-------------------------------------------------------------------------*/
	
	//Save or not files of powerloom
	// TODO remove these hard coded debug files, because knowledge base may not be power loom!!!!
	private boolean saveDebugFiles = false;
	private static final String NAME_GENERATIVE_FILE = "generative.plm"; 
	private static final String NAME_FINDING_FILE = "findings.plm"; 
	
	/*-------------------------------------------------------------------------*/
	/* Others (resources, utils, etc                                           */
	/*-------------------------------------------------------------------------*/

	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.mebn.resources.Resources.class.getName());;

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
	/* SSBN Mode                                                               */
	/*-------------------------------------------------------------------------*/
		
	private SSBN ssbn = null; 
	
	/*-------------------------------------------------------------------------*/
	/* Extensions                                                              */
	/*-------------------------------------------------------------------------*/
	
	private String mebnIOExtensionPointID = "MEBNIO";
	private String mebnModulePluginID = "unbbayes.prs.mebn";

	private ISSBNGenerator ssbnGenerator;
	
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
			MEBNNetworkWindow screen) {
		
		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
		
		// initialize plugin-aware IO with some attribute customization
        this.setBaseIO(this.setUpPluginIO());

		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
		this.setScreen(screen);
		this.mebnEditionPane = new MEBNEditionPane(screen, this);

		this.mebnFactory = new MEBNFactoryImpl(); 
		
		df = NumberFormat.getInstance(Locale.getDefault());
		df.setMaximumFractionDigits(4);
		
		enableMTheoryEdition(); 
		
		
		try {
			// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
			this.setToLogNodesAndProbabilities(Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
    				this.getClass().getCanonicalName()+".toLogNodesAndProbabilities").toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * This method uses {@link #getMebnIOExtensionPointID()} and
	 * {@link #getMebnModulePluginID()} in order to set up a new instance
	 * of {@link PluginAwareFileExtensionIODelegator} as MEBN's default
	 * IO component.
	 * @return : the generated new IO component.
	 */
	protected BaseIO setUpPluginIO() {
		// instantiate the new IO
		PluginAwareFileExtensionIODelegator ioDelegator = PluginAwareFileExtensionIODelegator.newInstance(false);
		
		// customize the attributes
		ioDelegator.setCorePluginID(this.getMebnModulePluginID());
		ioDelegator.setExtensionPointID(this.getMebnIOExtensionPointID());
		
		// reload plugins
		ioDelegator.reloadPlugins();
		
		// If no plugin was loaded, add a default IO
		if (ioDelegator.getDelegators().isEmpty()) {
			ioDelegator.getDelegators().add(UbfIO.getInstance());
		}
		
		return ioDelegator;
	}


	public void openPanel(ResidentNode node){
		setCurrentMFrag(node.getMFrag()); 
		selectNode(node); 
		setResidentNodeActive(node); 
	}
	
	
	/*-------------------------------------------------------------------------*/
	/*                                                                         */
	/*-------------------------------------------------------------------------*/	
	
	public void setResetButtonActive(){
		if(mebnEditionPane.getJtbEdition() != null){
			mebnEditionPane.getJtbEdition().selectBtnResetCursor();
		}
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

		typeElementSelected = TypeElementSelected.MTHEORY; 
		mebnEditionPane.setDescriptionText(multiEntityBayesianNetwork.getDescription(), DescriptionPane.DESCRIPTION_PANE_MTHEORY); 
	}

	/**
	 * Set the name of the MTheory active.
	 * @param name The new name
	 */
	public void renameMTheory(String name) throws DuplicatedNameException,
	                                              ReservedWordException{

		checkName(name);
		multiEntityBayesianNetwork.getNamesUsed().remove(multiEntityBayesianNetwork.getName()); 
		multiEntityBayesianNetwork.setName(name);
		mebnEditionPane.setNameMTheory(name);
		mebnEditionPane.getMTheoryTree().renameMTheory(name); 
		multiEntityBayesianNetwork.getNamesUsed().add(name); 

	}


	private void checkName(String name) throws DuplicatedNameException,
			ReservedWordException {
		if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
			throw new DuplicatedNameException(name); 
		}
		if(mebnFactory.getReservedWords().contains(name)){
			throw new ReservedWordException(name); 
		}
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
     * 
     * @throws MEBNConstructionException : when construction of a MEBN element fails
     * @throws CycleFoundException : when a resident node's partial order contains cycles
     * 
     */

    public boolean insertEdge(Edge edge) throws  MEBNConstructionException, CycleFoundException {

    	MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag();

    	try {
			mFragCurrent.addEdge(edge);
		} catch (Exception e) {
			throw new MEBNConstructionException(e);
		}
    	
    	return true;

    }

	
    
	/*-------------------------------------------------------------------------*/
	/* MFrag                                                                    */
	/*-------------------------------------------------------------------------*/

	public void insertDomainMFrag() {

		//The name of the MFrag is unique

		String name = null;

		int domainMFragNum = multiEntityBayesianNetwork.getDomainMFragNum();

		while (name == null){
			name = resource.getString("domainMFragName") + 
			       multiEntityBayesianNetwork.getDomainMFragNum();
			
			if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
				name = null;
				multiEntityBayesianNetwork.setDomainMFragNum(++domainMFragNum);
			}
		}

		MFrag mFrag = new MFrag(name, multiEntityBayesianNetwork);
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		multiEntityBayesianNetwork.addDomainMFrag(mFrag);

		mebnEditionPane.getMTheoryTree().addMFrag(mFrag);

	    showGraphMFrag(mFrag);

	    mebnEditionPane.setMFragBarActive();
	    mebnEditionPane.setTxtNameMFrag(mFrag.getName());
	    mebnEditionPane.setMTheoryTreeActive();
	    
		typeElementSelected = TypeElementSelected.MFRAG; 
		mebnEditionPane.setDescriptionText(mFrag.getDescription(), DescriptionPane.DESCRIPTION_PANE_MFRAG); 
	}

	public void removeDomainMFrag(MFrag domainMFrag) {
		multiEntityBayesianNetwork.removeDomainMFrag(domainMFrag);
		multiEntityBayesianNetwork.getNamesUsed().remove(domainMFrag.getName()); 
		mebnEditionPane.getMTheoryTree().removeMFrag(domainMFrag); 
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
	}

	/**
	 * Set the mFrag how the active MFrag and show its graph. 
	 * Show the tool bar of edition of the MFrag. 
	 */
	public void setCurrentMFrag(MFrag mFrag){

		showGraphMFrag(mFrag);

		mebnEditionPane.setMFragBarActive();
		mebnEditionPane.setTxtNameMFrag(mFrag.getName());
		mebnEditionPane.setMTheoryTreeActive();
		mebnEditionPane.showTitleGraph(mFrag.getName()); 
		
		typeElementSelected = TypeElementSelected.MFRAG; 
		mebnEditionPane.setDescriptionText(mFrag.getDescription(), DescriptionPane.DESCRIPTION_PANE_MFRAG); 
		
		setActionGraphNone(); 
	}

	/**
	 * Show the graph of the MFrag and select it how active MFrag.
	 *
	 * @param mFrag
	 */
	private void showGraphMFrag(MFrag mFrag){

		multiEntityBayesianNetwork.setCurrentMFrag(mFrag);
	    this.getScreen().getGraphPane().resetGraph();
	    mebnEditionPane.showTitleGraph(mFrag.getName());
	    mFragActive = mFrag;

	}

	/**
	 * Show a empty MFrag graph.
	 * Use when no MFrag is in a MTheory.
	 *
	 */
	private void showGraphMFrag(){

		multiEntityBayesianNetwork.setCurrentMFrag(null);
		mebnEditionPane.hideTopComponent();
		mebnEditionPane.setEmptyBarActive();
		mFragActive = null;

	}	
	
	/**
	 * rename the MFrag and update its name in the title of the graph
	 * @param mFrag
	 * @param name
	 * @throws ReservedWordException 
	 */
	public void renameMFrag(MFrag mFrag, String name) throws DuplicatedNameException, ReservedWordException{

           checkName(name); 
           
            multiEntityBayesianNetwork.getNamesUsed().remove(mFrag.getName()); 
		    mFrag.setName(name);
		    multiEntityBayesianNetwork.getNamesUsed().add(name); 
		    
			if(this.getCurrentMFrag() == mFrag){
				mebnEditionPane.showTitleGraph(name);
			}
			
			mebnEditionPane.getMTheoryTree().renameMFrag(mFrag); 
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
			if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
				name = null;
				multiEntityBayesianNetwork.plusDomainResidentNodeNum();
			}
		}
		
		ResidentNode node = new ResidentNode(name, domainMFrag);
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addResidentNode(node);
		
		typeElementSelected = TypeElementSelected.NODE; 
		
		residentNodeActive = node;
		nodeActive = node;
		
		//Updating panels
		mebnEditionPane.setEditArgumentsTabActive(node);
		mebnEditionPane.setResidentNodeTabActive(node);
		mebnEditionPane.setArgumentTabActive();
		mebnEditionPane.setResidentBarActive();
		mebnEditionPane.setTxtNameResident(((ResidentNode)node).getName());
		mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_RESIDENT); 
		
		mebnEditionPane.getMTheoryTree().addNode(domainMFrag, node); 
	
		
	    return node;
	}

	public void renameDomainResidentNode(ResidentNode resident, String newName)
	               throws DuplicatedNameException, ReservedWordException{

		checkName(newName); 
		multiEntityBayesianNetwork.getNamesUsed().remove(resident.getName()); 
		resident.setName(newName);
		multiEntityBayesianNetwork.getNamesUsed().add(newName); 
		mebnEditionPane.repaint();
		
		//by young2
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

	}

	
	
	/*-------------------------------------------------------------------------*/
	/* Resident Node: Possible values                                          */
	/*-------------------------------------------------------------------------*/
		
	/**
	 * Adds a possible value (state) into a resident node...
	 * @param resident
	 * @param value
	 * @throws ReservedWordException 
	 */
	public StateLink addPossibleValue(ResidentNode resident, String nameValue) 
	                       throws DuplicatedNameException, ReservedWordException{

		checkName(nameValue); 
		
		CategoricalStateEntity value = multiEntityBayesianNetwork.getCategoricalStatesEntityContainer().createCategoricalEntity(nameValue);
		multiEntityBayesianNetwork.getNamesUsed().add(nameValue); 
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
		
		String name = null; 
		
		while (name == null){
			name = resource.getString("inputNodeName") + multiEntityBayesianNetwork.getGenerativeInputNodeNum(); 
			if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
				name = null; 
				multiEntityBayesianNetwork.plusGenerativeInputNodeNum(); 
			}
		}
		
		InputNode node = new InputNode(name, domainMFrag);
		
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addInputNode(node);

		inputNodeActive = node;
		nodeActive = node;

		mebnEditionPane.setInputBarActive();
		mebnEditionPane.setTxtNameInput(((InputNode)node).getName());
		mebnEditionPane.setInputNodeActive(node);
		mebnEditionPane.setTxtInputOf("");
		mebnEditionPane.getMTheoryTree().addNode(domainMFrag, node); 
		mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_INPUT); 
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
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 
	}

	public void updateArgumentsOfObject(Object node){

		if (node instanceof InputNode){
			((InputNode)node).updateLabel();
		}else{
			if(node instanceof ContextNode){
				((ContextNode)node).updateLabel();
			}
		}
		
		//by Young
		 mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

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
		
		//by young
		//mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

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
		
		String name = null; 
		
		while (name == null){
			name = resource.getString("contextNodeName") + multiEntityBayesianNetwork.getContextNodeNum(); 
			if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
				name = null; 
				multiEntityBayesianNetwork.plusContextNodeNul(); 
			}
		}
		
		ContextNode node = new ContextNode(name, domainMFrag);
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addContextNode(node);

		typeElementSelected = TypeElementSelected.NODE; 
		contextNodeActive = node;
		nodeActive = node;

		setContextNodeActive(node);
		mebnEditionPane.getMTheoryTree().addNode(domainMFrag, node); 
		mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_CONTEXT); 
		
		return node;
	}


	
	/*-------------------------------------------------------------------------*/
	/* Graph                                                                   */
	/*-------------------------------------------------------------------------*/

	public void setActionGraphNone(){
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.NONE);	
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnResetCursor(); 
		}
	}
	
	public void setActionGraphCreateEdge(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddEdge(); 
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_EDGE);	
	}
	
	public void setActionGraphCreateContextNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddContextNode(); 
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE);	
	}
	
	public void setActionGraphCreateInputNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddInputNode(); 
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_INPUT_NODE);	
	}
	
	public void setActionGraphCreateResidentNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddResidentNode();
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_RESIDENT_NODE);	
	}
	
	public void setActionGraphCreateOrdinaryVariableNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddOrdinaryVariable();  
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_ORDINARYVARIABLE_NODE);	
	}
	
	/**
	 * Delete the selected item of the graph (a node or a edge)
	 */
	public void deleteSelectedItem(){
		
		Object selected = mebnEditionPane.getNetworkWindow().getGraphPane().getSelected(); 
		if(selected != null){
			deleteSelected(selected); 
		}
		
	
	}
	
	public void deleteSelected(Object selected) {
		if (selected instanceof ContextNode){
			((ContextNode)selected).delete();
			multiEntityBayesianNetwork.getNamesUsed().remove(((ContextNode)selected).getName()); 
			mebnEditionPane.getMTheoryTree().removeNode((Node)selected);
			mebnEditionPane.setMTheoryTreeActive();
		}
		else{

			if (selected instanceof ResidentNode){
				((ResidentNode)selected).delete();
				multiEntityBayesianNetwork.getNamesUsed().remove(((ResidentNode)selected).getName()); 
				mebnEditionPane.getMTheoryTree().removeNode((Node)selected);
				mebnEditionPane.setMTheoryTreeActive();
				this.setUnableTableEditionView();
			}
			else{
				if (selected instanceof InputNode){
					((InputNode)selected).delete();
					multiEntityBayesianNetwork.getNamesUsed().remove(((InputNode)selected).getName()); 
					mebnEditionPane.getMTheoryTree().removeNode((Node)selected);
					mebnEditionPane.setMTheoryTreeActive();
				}else{
					if (selected instanceof OrdinaryVariable){
						((OrdinaryVariable)selected).delete();
						multiEntityBayesianNetwork.getNamesUsed().remove(((OrdinaryVariable)selected).getName()); 
						this.getScreen().getGraphPane().update();
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#selectNode(unbbayes.prs.Node)
	 */
	public void selectNode(Node node){
		
		//Before select the new node, save the description of the previous
		
		saveDescriptionTextOfPreviousElement(mebnEditionPane.getDescriptionText());
		
		typeElementSelected = TypeElementSelected.NODE; 
		
		if (node instanceof ResidentNode){
			residentNodeActive = (ResidentNode)node;
			setResidentNodeActive(residentNodeActive);
		    mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_RESIDENT); 
		}
		else{
			if(node instanceof InputNode){
				inputNodeActive = (InputNode)node;
				setInputNodeActive(inputNodeActive);
				 mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_INPUT); 
			}
			else{
				if(node instanceof ContextNode){
					contextNodeActive = (ContextNode)node;
				    setContextNodeActive(contextNodeActive);
				    mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_CONTEXT); 
				}
				else{
					if(node instanceof OrdinaryVariable){
						ovNodeActive = (OrdinaryVariable)node;
						setOrdVariableNodeActive((OrdinaryVariable)node);
						 mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_OVARIABLE); 
					}
				}
			}

		}
	    mebnEditionPane.showTitleGraph(multiEntityBayesianNetwork.getCurrentMFrag().getName());
	}

	private void saveDescriptionTextOfPreviousElement(String text) {
		if(typeElementSelected != null){
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
	}
	
	public Node getSelectedNode() {
		return nodeActive;
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
		mebnEditionPane.getNetworkWindow().getGraphPane().update();
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
		
		String name = null; 
		
		while (name == null){
			name = resource.getString("ordinaryVariableName") + domainMFrag.getOrdinaryVariableNum(); 
			if(domainMFrag.getOrdinaryVariableByName(name) != null){
				name = null; 
				domainMFrag.plusOrdinaryVariableNum(); 
			}
		}
		
		Type type = TypeContainer.getDefaultType();
		OrdinaryVariable ov = new OrdinaryVariable(name, type, domainMFrag);

		ov.setPosition(x, y);
		ov.setDescription(ov.getName());
		domainMFrag.addOrdinaryVariable(ov);

		ovNodeActive = ov;
		setOrdVariableNodeActive(ov);

		mebnEditionPane.setEditOVariableTabActive();

		mebnEditionPane.setDescriptionText(ov.getDescription(), DescriptionPane.DESCRIPTION_PANE_OVARIABLE); 
		typeElementSelected = TypeElementSelected.NODE; 
		
		 mebnEditionPane.getEditOVariableTab().update(); 
		
	    return ov;

	}

	public void renameOrdinaryVariable(OrdinaryVariable ov, String name) 
	               throws DuplicatedNameException, ReservedWordException{
		
		   checkName(name); 
		   ov.setName(name); 
	       ov.updateLabel();
	       mebnEditionPane.getNetworkWindow().getGraphPane().update(); 
		   mebnEditionPane.getEditOVariableTab().update(); 
	}
	
	public void setOrdinaryVariableType(OrdinaryVariable ov, Type type){
		   ov.setValueType(type); 
		   ov.updateLabel(); 
		
		   //by Young
	 	   mebnEditionPane.getNetworkWindow().getGraphPane().update(); 
		   mebnEditionPane.getEditOVariableTab().update(); 
		
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
				domainMFrag.plusOrdinaryVariableNum();
			}
		}

		Type type = multiEntityBayesianNetwork.getTypeContainer().getDefaultType();

		OrdinaryVariable ov = new OrdinaryVariable(name, type, domainMFrag);
		domainMFrag.addOrdinaryVariable(ov);

		mebnEditionPane.getEditOVariableTab().update(); 
		
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

		residentNodeActive.addArgument(ordinaryVariable, true);
		mebnEditionPane.getEditArgumentsTab().update();
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

	}
	
	public void removeOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable){

		ResidentNode resident = (ResidentNode) this.getScreen().getGraphPane().getSelected();
		resident.removeArgument(ordinaryVariable);
		ordinaryVariable.removeIsOVariableOfList(resident);
		
		mebnEditionPane.getEditArgumentsTab().update();
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

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
	public void renameOVariableOfResidentTree(String name) 
	                 throws DuplicatedNameException, ReservedWordException{
		
		OrdinaryVariable ov = mebnEditionPane.getEditArgumentsTab().getResidentOVariableTree().getOVariableSelected();
	    
		checkName(name); 
		ov.setName(name);
	    ov.updateLabel();
		mebnEditionPane.getEditArgumentsTab().setTxtName(ov.getName());
		mebnEditionPane.getEditArgumentsTab().update();
	}

	@Deprecated
	public void renameOVariableOfMFragTree(String name) 
	               throws DuplicatedNameException, ReservedWordException{
		OrdinaryVariable ov = mebnEditionPane.getEditArgumentsTab().getMFragOVariableTree().getOVariableSelected();

		checkName(name); 
		ov.setName(name);
	    ov.updateLabel();
		mebnEditionPane.getEditArgumentsTab().setTxtName(ov.getName());
		mebnEditionPane.getEditArgumentsTab().update();
	}

	@Deprecated
	public void renameOVariableInArgumentEditionPane(String name) 
	                 throws DuplicatedNameException, ReservedWordException{
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
			if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
				name = null;
				multiEntityBayesianNetwork.getObjectEntityContainer().setEntityNum(++entityNum);
			}
		}

		ObjectEntity objectEntity = multiEntityBayesianNetwork.getObjectEntityContainer().createObjectEntity(name);
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		mebnEditionPane.getToolBarOVariable().updateListOfTypes(); 
		
		return objectEntity;
	}

	/**
	 * Rename a object entity. 
	 * @param entity
	 * @param name
	 * @throws TypeAlreadyExistsException
	 * @throws ReservedWordException 
	 */
	public void renameObjectEntity(ObjectEntity entity, String name) 
	            throws TypeAlreadyExistsException, DuplicatedNameException, ReservedWordException{
		
		checkName(name); 
		
		multiEntityBayesianNetwork.getNamesUsed().remove(name); 
		entity.setName(name);
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		mebnEditionPane.getToolBarOVariable().updateListOfTypes(); 
	}
	
	
    /**
    * Remove a object entity. 
    * @param entity
    * @throws Exception
    */
	public void removeObjectEntity(ObjectEntity entity) throws Exception{
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntity(entity);
		multiEntityBayesianNetwork.getNamesUsed().remove(entity.getName());
		
		try{
			multiEntityBayesianNetwork.getTypeContainer().removeType(entity.getType());
		}
		catch(Exception e){
            e.printStackTrace(); 
		}
		
		mebnEditionPane.getToolBarOVariable().updateListOfTypes(); 
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
	 * @throws InvalidOperationException
	 * @throws DuplicatedNameException
	 * @throws ReservedWordException 
	 */
	public void createEntityIntance(ObjectEntity entity, String nameInstance) 
	throws EntityInstanceAlreadyExistsException, InvalidOperationException, 
	       DuplicatedNameException, ReservedWordException{

		if(entity.isOrdereable()){
			throw new InvalidOperationException();
		}
		
		checkName(nameInstance); 
		
		if(multiEntityBayesianNetwork.getObjectEntityContainer().getEntityInstanceByName(nameInstance)!=null){
			throw new EntityInstanceAlreadyExistsException();
		}
		else{
			try {
				ObjectEntityInstance instance = entity.addInstance(nameInstance);
				multiEntityBayesianNetwork.getObjectEntityContainer().addEntityInstance(instance);
				multiEntityBayesianNetwork.getNamesUsed().add(nameInstance); 
			} catch (TypeException e1) {
				e1.printStackTrace();
			} catch(EntityInstanceAlreadyExistsException e){
				e.printStackTrace();
			}
		}
	}
	
	public void createEntityIntanceOrdereable(ObjectEntity entity, 
			String nameInstance, ObjectEntityInstanceOrdereable previous) 
	throws EntityInstanceAlreadyExistsException, InvalidOperationException, 
	       DuplicatedNameException, ReservedWordException{

		if(!entity.isOrdereable()){
			throw new InvalidOperationException();
		}
		
		checkName(nameInstance); 
		
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
				multiEntityBayesianNetwork.getNamesUsed().add(nameInstance); 
			} catch (TypeException e1) {
				e1.printStackTrace();
			} catch(EntityInstanceAlreadyExistsException e){
				e.printStackTrace();
			}
		}
	}

	public void renameEntityIntance(ObjectEntityInstance entity, String newName) throws EntityInstanceAlreadyExistsException, 
																						DuplicatedNameException, ReservedWordException{

		checkName(newName); 
		
		if(multiEntityBayesianNetwork.getObjectEntityContainer().getEntityInstanceByName(newName)!=null){
			throw new EntityInstanceAlreadyExistsException();
		}
		else{
			multiEntityBayesianNetwork.getNamesUsed().remove(entity.getName()); 
			entity.setName(newName);
			multiEntityBayesianNetwork.getNamesUsed().add(newName); 
			
		}
	}

	public void removeEntityInstance(ObjectEntityInstance entity) {
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(entity);
		multiEntityBayesianNetwork.getNamesUsed().remove(entity.getName()); 
	}
	
	public void removeEntityInstanceOrdereable(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.removeEntityInstanceOrdereableReferences(entity);	
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(entity);
		multiEntityBayesianNetwork.getNamesUsed().remove(entity.getName()); 
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
	
	public void createRandomVariableFinding(ResidentNode residentNode, 
			ObjectEntityInstance[] arguments, Entity state){
		
		RandomVariableFinding finding = new RandomVariableFinding(
				residentNode, 
				arguments, 
				state, 
				this.multiEntityBayesianNetwork);
		
		residentNode.addRandomVariableFinding(finding); 
	}
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Edition of CPT's                                                         */
	/*-------------------------------------------------------------------------*/
	
	public void saveCPT(ResidentNode residentNode, String cpt){
		residentNode.setTableFunction(cpt);
	}
	
	public void openCPTDialog(ResidentNode residentNode){
		CPTFrame cptEditionPane = mapCpt.get(residentNode); 
		if(cptEditionPane == null){
			cptEditionPane = new CPTFrame(this, residentNode);
			mapCpt.put(residentNode, cptEditionPane); 
			cptEditionPane.setVisible(true); 
		}else{
			cptEditionPane.setVisible(true); 
		}
	}
	
	public void closeCPTDialog(ResidentNode residentNode){
		CPTFrame cptEditionPane = mapCpt.get(residentNode); 
		if(cptEditionPane != null){
			cptEditionPane.dispose(); 
			mapCpt.remove(residentNode); 
		}
	}
	
	public CPTFrame getCPTDialog(ResidentNode residentNode){
		return mapCpt.get(residentNode); 
	}
	
	
	/*-------------------------------------------------------------------------*/
	/* Knowledge Base                                                          */
	/*-------------------------------------------------------------------------*/

	/**
	 * Obtains the currently used knowledge base
	 * @return instance of {@link KnowledgeBase}
	 */
	public KnowledgeBase getKnowledgeBase(){
		
	    if(knowledgeBase == null){
	    	// if no kb is set, use default.
	    	// avoid using GUI specific methods at controller
//	    	mebnEditionPane.getGraphPanel().setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
	    	knowledgeBase = PowerLoomKB.getNewInstanceKB(); 
//	    	mebnEditionPane.getGraphPanel().setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
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
		
		if(saveDebugFiles){
			this.saveGenerativeMTheory(new File(MEBNController.NAME_GENERATIVE_FILE)); 
		}
	}
	
	/**
	 * Insert the findings into KB.
	 */
	private void loadFindingsIntoKB(){
		
		KnowledgeBase knowledgeBase = getKnowledgeBase();
		
		for(ObjectEntityInstance instance: multiEntityBayesianNetwork.getObjectEntityContainer().getListEntityInstances()){
			 knowledgeBase.insertEntityInstance(instance); 
		}
		// TODO use a map instead of cubic search
		for(MFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
			for(ResidentNode residentNode : mfrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
					knowledgeBase.insertRandomVariableFinding(finding); 
				}
			}
		}
		
		if(saveDebugFiles){
			this.saveDefaultTemporaryFindingsFile();
		}
	}
	
	public void clearFindingsIntoGUI(){
		
		for(MFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
			for(ResidentNode residentNode : mfrag.getResidentNodeList()){
				residentNode.cleanRandomVariableFindingList(); 
			}
		}
		
	}

	public void clearKnowledgeBase(){
		getKnowledgeBase().clearKnowledgeBase();
	}
	
	public void saveGenerativeMTheory(File file){
		getKnowledgeBase().saveGenerativeMTheory(getMultiEntityBayesianNetwork(), file);
	}

	public void saveFindingsFile(File file){
		mebnEditionPane.setStatus(resource.getString("statusSavingKB")); 
		this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		createKnowledgeBase(); 	
		getKnowledgeBase().saveFindings(getMultiEntityBayesianNetwork(), file);
		
		mebnEditionPane.setStatus(resource.getString("statusReady")); 
		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	private void saveDefaultTemporaryFindingsFile() {
		getKnowledgeBase().saveFindings(getMultiEntityBayesianNetwork(), new File(MEBNController.NAME_FINDING_FILE));
	}

	public void loadFindingsFile(File file) throws UBIOException, MEBNException{
		
		// avoid GUI specific routines
//		mebnEditionPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		mebnEditionPane.setStatus(resource.getString("statusLoadingKB")); 
		
		Exception lastException = null;
		
		//init the powerloom knowledge base
		createKnowledgeBase(); 	
		getKnowledgeBase().loadModule(file, true);
		
		//init gui
		for (ResidentNode resident : this.multiEntityBayesianNetwork.getDomainResidentNodes()) {
			try {
				 this.knowledgeBase.fillFindings(resident);
			 } catch (Exception e) {
				 e.printStackTrace();
				 lastException = e;
				 continue;
			 }
		}
		
		mebnEditionPane.setStatus(resource.getString("statusReady")); 
		
		// avoid GUI specific routines
//		mebnEditionPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		if (lastException != null) {
			// commenting below... PowerLoom was throwing stack trace as message...
			//throw new MEBNException(lastException);
			throw new MEBNException(resourcePN.getString("loadHasError"));
		}
	}
	
	private void createKnowledgeBase(){
		// Must remove unwanted findings entered previously 
		getKnowledgeBase().clearKnowledgeBase();
		loadGenerativeMEBNIntoKB(); 
		loadFindingsIntoKB(); 
//		baseCreated = true; 
	}

//	/**
//	 * Execute a query. 
//	 * 
//	 * @param residentNode
//	 * @param arguments
//	 * @return
//	 * @throws InconsistentArgumentException
//	 * @throws ImplementationRestrictionException 
//	 * @throws SSBNNodeGeneralException 
//	 * @throws OVInstanceFaultException 
//	 * @throws InvalidParentException 
//	 */
//	public ProbabilisticNetwork executeQuery(ResidentNode residentNode, 
//			ObjectEntityInstance[] arguments)
//	                           throws InconsistentArgumentException, 
//	                                  SSBNNodeGeneralException, 
//	                                  ImplementationRestrictionException, 
//	                                  MEBNException, 
//	                                  OVInstanceFaultException, InvalidParentException {
//		
//		mebnEditionPane.setStatus(resource.getString("statusGeneratingSSBN")); 
//		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//		
//		ProbabilisticNetwork probabilisticNetwork = null; 
//		
//		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode); 
//		
//		List<Argument> arglist = residentNode.getArgumentList();
//		
//		if (arglist.size() != arguments.length) {
//			throw new InconsistentArgumentException();
//		}
//		
//		for (int i = 1; i <= arguments.length; i++) {
//			try {
//				//TODO It has to get in the right order. For some reason in argList, 
//				// sometimes the second argument comes first
//				for (Argument argument : arglist) {
//					if (argument.getArgNumber() == i) {
//						queryNode.addArgument(argument.getOVariable(), arguments[i-1].getName());
//						break;
//					}
//				}
//				
//			} catch (SSBNNodeGeneralException e) {
//				
//				mebnEditionPane.setStatus(resource.getString("statusReady")); 
//				screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//				
//				throw new InconsistentArgumentException(e);
//			}
//		}
//		
//	    createKnowledgeBase(); 	
//
//		Query query = new Query(getKnowledgeBase(), queryNode, multiEntityBayesianNetwork);
//		
//		ISSBNGenerator ssbngenerator = new ExplosiveSSBNGenerator();
//
//		List<Query> listQueries = new ArrayList<Query>(); 
//		listQueries.add(query); 
//		
//		ssbn = ssbngenerator.generateSSBN(listQueries, getKnowledgeBase()); 
//		
//		probabilisticNetwork = ssbn.getProbabilisticNetwork();
//
//		if(!query.getQueryNode().isFinding()){
//
//				showSSBNGraph = true; 
//				specificSituationBayesianNetwork = probabilisticNetwork;
//
//				try {
//					
//					ssbn.compileAndInitializeSSBN();
//					
//					if (ssbn.getWarningList().size() > 0){
//						openWarningDialog(); 	
//					}
//					
//					this.getMebnEditionPane().getNetworkWindow().changeToSSBNCompilationPane(specificSituationBayesianNetwork);
//
//					Dimension sizeOfGraph = PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(specificSituationBayesianNetwork); 
//					Dimension originalDimension = this.getMebnEditionPane().getNetworkWindow().getGraphPane().getGraphDimension(); 
//					if((originalDimension.getHeight() < sizeOfGraph.getHeight()) || 
//							(originalDimension.getWidth() < sizeOfGraph.getWidth())){
//						dimensionSSBNGraph = sizeOfGraph; 
//						this.getMebnEditionPane().getNetworkWindow().getGraphPane().setGraphDimension(sizeOfGraph); 
//						this.getMebnEditionPane().getNetworkWindow().getGraphPane().update(); 
//					}
//					
//				} catch (Exception e) {
//					e.printStackTrace(); 
//					screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//					JOptionPane.showMessageDialog(getScreen(), 
//							e.getMessage());
//				} 
//
//
//			
//		}else{
//			JOptionPane.showMessageDialog(getScreen(), 
//					query.getQueryNode().getName() + " = " + query.getQueryNode().getValue());
//	
//		}
//
//		mebnEditionPane.setStatus(resource.getString("statusReady")); 
//		screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//		
//		return specificSituationBayesianNetwork ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
//	}

	/**
	 * Execute a query using the Laskey's  
	 * 
	 * @param residentNode
	 * @param arguments
	 * @return
	 * @throws InconsistentArgumentException
	 * @throws ImplementationRestrictionException 
	 * @throws SSBNNodeGeneralException 
	 * @throws OVInstanceFaultException 
	 * @throws InvalidParentException 
	 */
	public ProbabilisticNetwork executeQueryLaskeyAlgorithm(List<Query> listQueries)
	                           throws InconsistentArgumentException, 
	                                  SSBNNodeGeneralException, 
	                                  ImplementationRestrictionException, 
	                                  MEBNException, 
	                                  OVInstanceFaultException, InvalidParentException {
		
		ProbabilisticNetwork probabilisticNetwork = null; 
		
		mebnEditionPane.setStatus(resource.getString("statusGeneratingSSBN")); 
		this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
	    createKnowledgeBase(); 	
		
		try{
			ssbn = this.getSSBNGenerator().generateSSBN(listQueries, getKnowledgeBase()); 
		}
		catch(ImplementationRestrictionException e){
			throw e; 
		}
		catch(InvalidParentException e2){
			throw e2; 
		}
		catch(MEBNException e3){
			throw e3; 
		}
		catch(OVInstanceFaultException e4){
			throw e4; 
		}
		catch(SSBNNodeGeneralException e5){
			throw e5; 
		} catch (Exception e6) {
			e6.printStackTrace(); 
			this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			JOptionPane.showMessageDialog(getScreen(), 
					e6.getMessage());
		} 
		
//		String fileName = "SSBN_Test.log"; 
//		try {
//			ssbn.getLogManager().writeToDisk(fileName, true);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} 
		
		probabilisticNetwork = ssbn.getProbabilisticNetwork();

		showSSBNGraph = true; 
		specificSituationBayesianNetwork = probabilisticNetwork;
		
		try {

			ssbn.compileAndInitializeSSBN();

			if (ssbn.getWarningList().size() > 0){
				openWarningDialog(); 	
			}
			
			// logging probabilities of the nodes
			this.logNodesAndItsProbabilities(ssbn);
			
			this.getMebnEditionPane().getNetworkWindow().changeToSSBNCompilationPane(specificSituationBayesianNetwork);
			
			(new GraphLayoutUtil(specificSituationBayesianNetwork)).doLayout();
			
			this.getMebnEditionPane().getNetworkWindow().getGraphPane().update();

		} catch (Exception e) {
			e.printStackTrace(); 
			this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			JOptionPane.showMessageDialog(getScreen(), 
					e.getMessage());
		}

		// TODO - SHOU and ROMMEL - Find a better way to show MSBN instead of BN - plugin of plugin?
//		openMsbnNetwork();
		
		mebnEditionPane.setStatus(resource.getString("statusReady")); 
		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		mebnEditionPane.setStatus(resource.getString("statusReady")); 
		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		return specificSituationBayesianNetwork ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
	}
	
	/**
	 * Obtains the current SSBN generation algorithm used by this controller.
	 * @return a non-null value, instance of {@link ISSBNGenerator}
	 * @see #setSSBNGenerator(ISSBNGenerator)
	 */
	public ISSBNGenerator getSSBNGenerator() {
		if (ssbnGenerator == null) {
			// return a non-null default generator using default settings
			LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
			parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
			parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
			parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
			parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
			ssbnGenerator = new LaskeySSBNGenerator(parameters);
		}
		return ssbnGenerator;
	}
	
	/**
	 * Sets the {@link ISSBNGenerator} to be used as a SSBN generation
	 * algoritym by this controller.
	 * @param ssbnGenerator
	 * @see #getSSBNGenerator()
	 */
	public void setSSBNGenerator(ISSBNGenerator ssbnGenerator) {
		this.ssbnGenerator = ssbnGenerator;
	}
 
// TODO - SHOU and ROMMEL - Find a better way to show MSBN instead of BN - plugin of plugin?
//	private void openMsbnNetwork() {
//		AbstractMSBN msbn = ssbn.getMsbnNetwork();
//		MSBNController controller = new MSBNController((SingleAgentMSBN)msbn);
//		try {
////			controller.compile();
//		} catch (Exception e) {
//			JOptionPane.showMessageDialog(this.getScreen(), e.getMessage(), "MSBN compilation error", JOptionPane.ERROR_MESSAGE);
//			e.printStackTrace();
//		}
//		this.getScreen().getDesktopPane().add(controller.getPanel());
//		controller.getPanel().setSize(controller.getPanel().getPreferredSize());
//		controller.getPanel().setVisible(true);
//	}
	

	/**
	 * Prints nodes and its states
	 * @param ssbn
	 */
	private void logNodesAndItsProbabilities(SSBN ssbn) {
		if (!this.isToLogNodesAndProbabilities()) {
			return;
		}
		ILogManager logManager = ssbn.getLogManager();
		ProbabilisticNetwork probabilisticNetwork = ssbn.getProbabilisticNetwork();
		if (logManager != null && probabilisticNetwork != null && probabilisticNetwork.getNodes() != null) {
			logManager.appendSpecialTitle("Result Query: " + ssbn.getQueryList());
			for (Node node: probabilisticNetwork.getNodes()) {
				if (!(node instanceof ProbabilisticNode)) {
					continue;
				}
				ProbabilisticNode prob = (ProbabilisticNode)node;
				logManager.appendSectionTitle(prob.toString());
				for (int i = 0 ; i < prob.getStatesSize() ; i++) {
					logManager.append(prob.getStateAt(i));
					logManager.append(" = ");
					logManager.append(Float.toString(prob.getMarginalAt(i)));
					logManager.append(", ");
				}
				logManager.appendln(" ");
			}
			logManager.appendln(" ");
			logManager.appendSeparator();
		}
	
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#openWarningDialog()
	 */
	public void openWarningDialog() {
		if(ssbn != null){
			
			if(warningDialog != null){
				warningDialog.setVisible(false); 
				warningDialog.dispose(); 
			}
			
			List<SSBNWarning> listWarnings = ssbn.getWarningList(); 

			warningDialog = new JDialog(); 
			warningDialog.setTitle(resource.getString("ResultDialog")); 
			WarningPanel warningPanel = new WarningPanel(this);
			warningPanel.setListWarningAndUpdateText(listWarnings); 
			warningDialog.setContentPane(warningPanel);
			warningDialog.setMinimumSize(new Dimension(600, 400)); 
			warningDialog.setPreferredSize(new Dimension(600,400));
			warningDialog.setMaximumSize(new Dimension(600, 400)); 
			warningDialog.pack(); 
			warningDialog.setLocationRelativeTo(
					this.getMebnEditionPane().getNetworkWindow()); 
			warningDialog.setVisible(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#closeWarningDialog()
	 */
	public void closeWarningDialog(){
		if(warningDialog != null){
			warningDialog.dispose();
			warningDialog = null; 
		}
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
	private static ResourceBundle resourcePN = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.mebn.resources.Resources.class.getName());
	
	/**
	 * Compiles the bayesian network. If there was any problem during compilation, the error
	 * message will be shown as a <code>JOptionPane</code> .
	 * 
	 * @return true if the net was compiled without any problem, false if there was a problem
	 * @since
	 * @see JOptionPane
	 */
	public boolean compileNetwork(ProbabilisticNetwork network) {
//		long ini = System.currentTimeMillis();
		this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			network.compile();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), resourcePN
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
			this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			return false;
		}

		/* isto será feito dentro do changeToSSBNCompilationPane */
//		screen.getEvidenceTree().updateTree();  hehe... ainda não temos uma evidence tree... sorry!
		

		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

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
			ssbn.reinitializeSSBN(); 
			//by young2
			this.getScreen().getEvidenceTree().updateTree(true);
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
		this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		boolean temLikeliHood = false;
		try {
			specificSituationBayesianNetwork.updateEvidences();
			if (!temLikeliHood) {
				this.getScreen().setStatus(resourcePN
						.getString("statusEvidenceProbabilistic")
						+ df.format(specificSituationBayesianNetwork.PET() * 100.0));
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				ssbn.reinitializeSSBN();
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			JOptionPane.showMessageDialog(this.getScreen(), e.getMessage(), resourcePN
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
		}
		//by young2
		this.getScreen().getEvidenceTree().updateTree(false);
		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
			this.getMebnEditionPane().getNetworkWindow().getGraphPane().setGraphDimension(dimensionSSBNGraph); 
			this.getMebnEditionPane().getNetworkWindow().getGraphPane().update(); 
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


	public ProbabilisticNetwork getSpecificSituationBayesianNetwork() {
		return specificSituationBayesianNetwork;
	}
	
	public SSBN getSSBN(){
		return ssbn; 
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

	private void printArgumentsOfResidentNode(ResidentNode resident){
		Debug.println("Resident: " + resident);
		for(Argument arg: resident.getArgumentList()){
			Debug.println(" [" + arg.getArgNumber() + "]:" + arg.getOVariable());
		}
	}


	/**
	 * @return the toLogNodesAndProbabilities
	 */
	public boolean isToLogNodesAndProbabilities() {
		return toLogNodesAndProbabilities;
	}


	/**
	 * @param toLogNodesAndProbabilities the toLogNodesAndProbabilities to set
	 */
	public void setToLogNodesAndProbabilities(boolean toLogNodesAndProbabilities) {
		this.toLogNodesAndProbabilities = toLogNodesAndProbabilities;
	}
	
	
	
	// Methods for backward-compatibilities
	
	 /**
     * Insert a new resident node in the MultiEntityBayesianNetwork with 
     * the standard label and descritpion.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     * @deprecated use {@link #insertDomainResidentNode(double, double)}
     */
    
    public Node insertResidentNode(double x, double y) throws MFragDoesNotExistException{
    	return this.insertDomainResidentNode(x, y);
    }
    
    /**
     * @deprecated use {@link #insertGenerativeInputNode(double, double)}
     */
    public Node insertInputNode(double x, double y) throws MFragDoesNotExistException{
    	return this.insertGenerativeInputNode(x,y);
    }
    
   /*
    * (non-Javadoc)
    * @see unbbayes.controller.NetworkController#getNetwork()
    */
    public Network getNetwork() {
    	return multiEntityBayesianNetwork;
    }
    
    /*
     * (non-Javadoc)
     * @see unbbayes.controller.NetworkController#getGraph()
     */
    public Graph getGraph(){
    	if(!this.isShowSSBNGraph()){
    		if (multiEntityBayesianNetwork.getCurrentMFrag()!= null){
    			return multiEntityBayesianNetwork.getCurrentMFrag();
    		}else{
    			return multiEntityBayesianNetwork;
    		}
    	} else{
    		return this.getSpecificSituationBayesianNetwork();
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see unbbayes.controller.NetworkController#getLogContent()
     */
    protected String getLogContent() {
    	// this method was overwritten in order to retrieve the log content from the SSBN log manager
    	try {
    		return this.getSSBN().getLogManager().getLog();
    	} catch (NullPointerException e) {
			e.printStackTrace();
		}
    	
    	return "";
    }

    /*
     * (non-Javadoc)
     * @see unbbayes.controller.NetworkController#unselectAll()
     */
    public void unselectAll(){
    	// as you see, it only unselects nodes
    	if (multiEntityBayesianNetwork != null){
    		this.unselectNodes(); 
    	}    	
    }


	/**
	 * This ID will be used by {@link PluginAwareFileExtensionIODelegator}
	 * in order to load the correct extension ID for MEBN's IO.
	 * @return the mebnIOExtensionPointID
	 */
	public String getMebnIOExtensionPointID() {
		return mebnIOExtensionPointID;
	}


	/**
	 * This ID will be used by {@link PluginAwareFileExtensionIODelegator}
	 * in order to load the correct extension ID for MEBN's IO.
	 * @param mebnIOExtensionPointID the mebnIOExtensionPointID to set
	 */
	public void setMebnIOExtensionPointID(String mebnIOExtensionPointID) {
		this.mebnIOExtensionPointID = mebnIOExtensionPointID;
	}


	/**
	 * This is the ID of MEBN module.
	 * It will be used by {@link PluginAwareFileExtensionIODelegator} in order
	 * to find the correct plugin context.
	 * @return the mebnModulePluginID
	 */
	public String getMebnModulePluginID() {
		return mebnModulePluginID;
	}


	/**
	 * It will be used by {@link PluginAwareFileExtensionIODelegator} in order
	 * to find the correct plugin context.
	 * @param mebnModulePluginID the mebnModulePluginID to set
	 */
	public void setMebnModulePluginID(String mebnModulePluginID) {
		this.mebnModulePluginID = mebnModulePluginID;
	}
	
	
	/**
	 * Sets the current knowledge base and initializes (reset) it.
	 * @param kb
	 */
	public void setKnowledgeBase(KnowledgeBase kb) {
		this.knowledgeBase = kb;
		this.resetKnowledgeBase();
	}
	
	/**
	 * Clears the content of the current knowledge base ({@link #getKnowledgeBase()}),
	 * and fills it using the currently edited MEBN.
	 */
	public void resetKnowledgeBase() {
		this.createKnowledgeBase();
	}
	
}
