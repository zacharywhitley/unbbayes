/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import unbbayes.controller.FileHistoryController;
import unbbayes.controller.NetworkController;
import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.exception.InvalidOperationException;
import unbbayes.gui.FileIcon;
import unbbayes.gui.GraphAction;
import unbbayes.gui.GraphPane;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.gui.mebn.DescriptionPane;
import unbbayes.gui.mebn.MEBNEditionPane;
import unbbayes.gui.mebn.MEBNNetworkWindow;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.WarningPanel;
import unbbayes.gui.mebn.cpt.CPTFrame;
import unbbayes.gui.mebn.cpt.CPTFrameFactory;
import unbbayes.gui.mebn.cpt.ICPTFrameFactory;
import unbbayes.io.BaseIO;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.extension.jpf.PluginAwareFileExtensionIODelegator;
import unbbayes.io.log.ILogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.extension.IPluginNode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityContainer;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.SoftEvidenceEntity;
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
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.prs.mebn.exception.ReservedWordException;
import unbbayes.prs.mebn.extension.IMEBNPluginNode;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.IMediatorAwareSSBNGenerator;
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
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.dto.INodeClassDataTransferObject;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;
import unbbayes.util.mebn.extension.manager.MEBNPluginNodeManager;

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
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 2.0 06/18/2011 - (feature:3317031) Added turnToMTheoryMode
 */

public class MEBNController extends NetworkController implements IMEBNMediator{
	
	/** If true, {@link #openCPTDialog(ResidentNode)} will always re-instantiate a new LPD dialog. */
	private boolean enableLPDEditorCache = false;

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
//	private ProbabilisticNetwork specificSituationBayesianNetwork; 
	private IInferenceAlgorithm ssbnAlgorithm; 
	
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
	private HashMap<ResidentNode, JFrame> mapCpt = 
		new HashMap<ResidentNode, JFrame>(); 
	
	
	/*-------------------------------------------------------------------------*/
	/* Constants                                            */
	/*-------------------------------------------------------------------------*/
	
	//Save or not files of powerloom
	// TODO remove these hard coded debug files, because knowledge base may not be power loom!!!!
	private boolean saveDebugFiles = false;
	private static final String NAME_GENERATIVE_FILE = "generative.plm"; 
	private static final String NAME_FINDING_FILE = "findings.plm";

	/** This is the default instance to be returned by {@link #getLPDFrameFactory()} */
	public static final ICPTFrameFactory DEFAULT_CPT_FRAME_FACTORY = new CPTFrameFactory(); 
	
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
	/* Data transfer objects                                                   */
	/*-------------------------------------------------------------------------*/
	
	/** This map is used by {@link IMEBNMediator#getProperty(String)} and {@link IMEBNMediator#getProperty(String)#setPropertyMap(Map)} */
	private Map<String, Object> propertyMap = new HashMap<String, Object>();
	
	// This object manages plugin-loaded nodes.
	private MEBNPluginNodeManager pluginNodeManager = MEBNPluginNodeManager.newInstance();

	private boolean isToTurnToSSBNMode = true;

	private boolean isToUseSimpleSoftEvidenceInKB = false;
	

	private boolean isToIncludeSoftEvidences = false; 
	
	
	
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
        
        // adding a listener to reload IO if plugin reload action is triggered
		UnBBayesPluginContextHolder.newInstance().addListener(new UnBBayesPluginContextHolder.OnReloadActionListener() {
			public void onReload(EventObject arg0) {
				setBaseIO(setUpPluginIO());
			}
		});

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
		
		// update mediator of plugin nodes
		if (this.multiEntityBayesianNetwork != null) {
			try {
				for (MFrag mfrag : multiEntityBayesianNetwork.getMFragList()) {
					try {
						for (Node node : mfrag.getNodes()) {
							try {
								if (node instanceof IMEBNPluginNode) {
									IMEBNPluginNode pluginNode = (IMEBNPluginNode) node;
									pluginNode.setMediator(this);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#openPanel(unbbayes.prs.mebn.ResidentNode)
	 */
	public void openPanel(ResidentNode node){
		setCurrentMFrag(node.getMFrag()); 
		selectNode(node); 
		setResidentNodeActive(node); 
	}
	
	
	/*-------------------------------------------------------------------------*/
	/*                                                                         */
	/*-------------------------------------------------------------------------*/	
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setResetButtonActive()
	 */
	public void setResetButtonActive(){
		if(mebnEditionPane.getJtbEdition() != null){
			mebnEditionPane.getJtbEdition().selectBtnResetCursor();
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getResidentNodeActive()
	 */
	public ResidentNode getResidentNodeActive(){
		return residentNodeActive;
	}
	
	public IResidentNode getActiveResidentNode() {
		return this.getResidentNodeActive();
	}
	
	public void setActiveResidentNode(IResidentNode activeNode) {
		this.residentNodeActive = (ResidentNode)activeNode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getInputNodeActive()
	 */
	public InputNode getInputNodeActive(){
		return inputNodeActive;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getContextNodeActive()
	 */
	public ContextNode getContextNodeActive(){
		return contextNodeActive;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getNodeActive()
	 */
	public Node getNodeActive(){
		return nodeActive;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#enableMTheoryEdition()
	 */
	public void enableMTheoryEdition(){

		mebnEditionPane.setMTheoryBarActive();
		mebnEditionPane.setNameMTheory(this.multiEntityBayesianNetwork.getName());
		mebnEditionPane.setMTheoryTreeActive();

		typeElementSelected = TypeElementSelected.MTHEORY; 
		mebnEditionPane.setDescriptionText(multiEntityBayesianNetwork.getDescription(), DescriptionPane.DESCRIPTION_PANE_MTHEORY); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameMTheory(java.lang.String)
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

	// Old version, that not allow duplicated entities
//	private void checkName(String name) throws DuplicatedNameException,
//			ReservedWordException {
//		if(multiEntityBayesianNetwork.getNamesUsed().contains(name)){
//			throw new DuplicatedNameException(name); 
//		}
//		if(mebnFactory.getReservedWords().contains(name)){
//			throw new ReservedWordException(name); 
//		}
//	}
	
	// Allows duplicated name in entities
	private void checkName(String name) throws ReservedWordException {
		
		if(mebnFactory.getReservedWords().contains(name)){
			throw new ReservedWordException(name); 
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setDescriptionTextForSelectedObject(java.lang.String)
	 */
	public void setDescriptionTextForSelectedObject(String text){
		saveDescriptionTextOfPreviousElement(text); 	
	}
	
	/*-------------------------------------------------------------------------*/
	/* Edge                                                                    */
	/*-------------------------------------------------------------------------*/

    /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertEdge(unbbayes.prs.Edge)
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertDomainMFrag()
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeDomainMFrag(unbbayes.prs.mebn.MFrag)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setCurrentMFrag(unbbayes.prs.mebn.MFrag)
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameMFrag(unbbayes.prs.mebn.MFrag, java.lang.String)
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





	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getCurrentMFrag()
	 */
	public MFrag getCurrentMFrag(){
		return multiEntityBayesianNetwork.getCurrentMFrag();
	}

	
	@Override
	public Node insertNode(Node newNode) {
		MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();

		if (currentMFrag == null) {
			return null;
		}

		MFrag domainMFrag = (MFrag) currentMFrag;
		return newNode;
	}
	/*-------------------------------------------------------------------------*/
	/* Resident Node                                                           */
	/*-------------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertDomainResidentNode(double, double)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameDomainResidentNode(unbbayes.prs.mebn.ResidentNode, java.lang.String)
	 */
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
		
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addPossibleValue(unbbayes.prs.mebn.ResidentNode, java.lang.String)
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addPossibleValue(unbbayes.prs.mebn.ResidentNode, unbbayes.prs.mebn.entity.CategoricalStateEntity)
	 */
	public StateLink addPossibleValue(ResidentNode resident, CategoricalStateEntity state){
		
		StateLink link = null; 
		
		if(!resident.hasPossibleValue(state)){
			link = resident.addPossibleValueLink(state);
			state.addNodeToListIsPossibleValueOf(resident);	
		}
		
		return link; 
		
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addObjectEntityAsPossibleValue(unbbayes.prs.mebn.ResidentNode, unbbayes.prs.mebn.entity.ObjectEntity)
	 */
	public StateLink addObjectEntityAsPossibleValue(ResidentNode resident, ObjectEntity state){
		
		StateLink stateLink = null; 
		
		if(!resident.hasPossibleValue(state)){
			stateLink = resident.addPossibleValueLink(state);
			state.addNodeToListIsPossibleValueOf(resident);	
		}
		
		return stateLink; 
		
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#existPossibleValue(java.lang.String)
	 */
    public boolean existPossibleValue(String name){
		
    	//TODO uma versï¾ƒï½£o decente...
		try {
			multiEntityBayesianNetwork.getCategoricalStatesEntityContainer().getCategoricalState(name);
			return true; 
		} catch (CategoricalStateDoesNotExistException e) {
			return false; 
		} 
    	
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setGloballyExclusiveProperty(unbbayes.prs.mebn.entity.StateLink, boolean)
	 */
	public void setGloballyExclusiveProperty(StateLink state, boolean value){
		state.setGloballyExclusive(value); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addBooleanAsPossibleValue(unbbayes.prs.mebn.ResidentNode)
	 */
	public void addBooleanAsPossibleValue(IResidentNode resident){

		resident.addPossibleValueLink(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getFalseStateEntity());
		resident.addPossibleValueLink(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getTrueStateEntity());
		resident.addPossibleValueLink(multiEntityBayesianNetwork.getBooleanStatesEntityContainer().getAbsurdStateEntity());

	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removePossibleValue(unbbayes.prs.mebn.ResidentNode, java.lang.String)
	 */
	public void removePossibleValue(IResidentNode resident, String nameValue){
		resident.removePossibleValueByName(nameValue);
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeAllPossibleValues(unbbayes.prs.mebn.ResidentNode)
	 */
	public void removeAllPossibleValues(IResidentNode resident){
		resident.removeAllPossibleValues();
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#existsPossibleValue(unbbayes.prs.mebn.ResidentNode, java.lang.String)
	 */
	public boolean existsPossibleValue(IResidentNode resident, String nameValue){
		return resident.existsPossibleValueByName(nameValue);
	}


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setEnableTableEditionView()
	 */
	public void setEnableTableEditionView(){

		mebnEditionPane.showTableEditionPane((ResidentNode)this.getResidentNodeActive());
		

	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setUnableTableEditionView()
	 */
	public void setUnableTableEditionView(){

		mebnEditionPane.hideTopComponent();

	}

	
	
	/*-------------------------------------------------------------------------*/
	/* Input Node                                                              */
	/*-------------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertGenerativeInputNode(double, double)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setInputInstanceOf(unbbayes.prs.mebn.InputNode, unbbayes.prs.mebn.ResidentNode)
	 */
	public void setInputInstanceOf(InputNode input, ResidentNode resident) throws CycleFoundException, OVDontIsOfTypeExpected, ArgumentNodeAlreadySetException{

		input.setInputInstanceOf((ResidentNode)resident);
		mebnEditionPane.getInputNodePane().updateArgumentPane();
		mebnEditionPane.setTxtInputOf(resident.getName());
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#updateArgumentsOfObject(java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#updateInputInstanceOf(unbbayes.prs.mebn.InputNode)
	 */
	public void updateInputInstanceOf(InputNode input){

		Object target = input.getInputInstanceOf();

		if (target == null){
			mebnEditionPane.setTxtInputOf("");
		}
		else{
			if (target instanceof IResidentNode){
				mebnEditionPane.setTxtInputOf(((ResidentNode)target).getName());
			}
		}
		
		//by young
		//mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

	}

	
	
	/*-------------------------------------------------------------------------*/
	/* Context Node                                                            */
	/*-------------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertContextNode(double, double)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setActionGraphNone()
	 */
	public void setActionGraphNone(){
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.NONE);	
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnResetCursor(); 
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setActionGraphCreateEdge()
	 */
	public void setActionGraphCreateEdge(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddEdge(); 
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_EDGE);	
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setActionGraphCreateContextNode()
	 */
	public void setActionGraphCreateContextNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddContextNode(); 
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE);	
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setActionGraphCreateInputNode()
	 */
	public void setActionGraphCreateInputNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddInputNode(); 
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_INPUT_NODE);	
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setActionGraphCreateResidentNode()
	 */
	public void setActionGraphCreateResidentNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddResidentNode();
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_RESIDENT_NODE);	
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setActionGraphCreateOrdinaryVariableNode()
	 */
	public void setActionGraphCreateOrdinaryVariableNode(){
		if(mebnEditionPane.getJtbEdition()!=null){
		    mebnEditionPane.getJtbEdition().selectBtnAddOrdinaryVariable();  
		}
	    mebnEditionPane.getNetworkWindow().getGraphPane().setAction(GraphAction.CREATE_ORDINARYVARIABLE_NODE);	
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#deleteSelectedItem()
	 */
	public void deleteSelectedItem(){
		
		Object selected = mebnEditionPane.getNetworkWindow().getGraphPane().getSelected(); 
		if(selected != null){
			deleteSelected(selected); 
		}
		
	
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#deleteSelected(java.lang.Object)
	 */
	public void deleteSelected(Object selected) {
		if (selected instanceof ContextNode){
			((ContextNode)selected).delete();
			multiEntityBayesianNetwork.getNamesUsed().remove(((ContextNode)selected).getName()); 
			mebnEditionPane.getMTheoryTree().removeNode((Node)selected);
			mebnEditionPane.setMTheoryTreeActive();
		}
		else{

			if (selected instanceof IResidentNode){
				((IResidentNode)selected).delete();
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
					else if (selected instanceof Edge) {
							MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag();
							mFragCurrent.removeEdge((Edge) selected);
					} else if (selected instanceof Node) {
						// default behavior: delete the unknown type of node (which is probably a plugin node)
						MFrag mFragCurrent = multiEntityBayesianNetwork.getCurrentMFrag();
						mFragCurrent.removeNode((Node)selected);
					}
				}
			}
		}



	}

	/**
	 * This method extends the superclass by specifically handling
	 * MEBN nodes (it uses instanceof to detect MEBN specific nodes).
	 * In order to handle plugin nodes, it expects that {@link #getPluginNodeManager()}
	 * and {@link MEBNPluginNodeManager#getPluginNodeInformation(Class)} with
	 * node.getClass() as its parameter can retrieve a valid instance of {@link INodeClassDataTransferObject}
	 * containing a valid value of {@link INodeClassDataTransferObject#getProbabilityFunctionPanelBuilder()}
	 * for the node (that is, it expects that {@link #getPluginNodeManager()} can retrieve a correct builder
	 * for a panel to edit the currently selected node).
	 * @param node.
	 * @see unbbayes.controller.NetworkController#selectNode(unbbayes.prs.Node)
	 * @see unbbayes.controller.mebn.IMEBNMediator#selectNode(unbbayes.prs.Node)
	 */
	public void selectNode(Node node){
		
		//Before select the new node, save the description of the previous
		
		saveDescriptionTextOfPreviousElement(mebnEditionPane.getDescriptionText());
		
		typeElementSelected = TypeElementSelected.NODE; 
		
		// TODO stop using if-then-else and use polymorphism instead
		if (node != null) {
			if (node instanceof IPluginNode) {
				this.getScreen().showProbabilityDistributionPanel(
						this.getPluginNodeManager().getPluginNodeInformation(node.getClass()).getProbabilityFunctionPanelBuilder()
				);
				mebnEditionPane.setDescriptionText(node.getDescription(), null); // null means default icon
				this.nodeActive = node;
			} else if (node instanceof IResidentNode){
				residentNodeActive = (ResidentNode)node;
				setResidentNodeActive(residentNodeActive);
			    mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_RESIDENT); 
			} else if(node instanceof InputNode){
				inputNodeActive = (InputNode)node;
				setInputNodeActive(inputNodeActive);
				mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_INPUT); 
			} else if(node instanceof ContextNode){
				contextNodeActive = (ContextNode)node;
			    setContextNodeActive(contextNodeActive);
			    mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_CONTEXT); 
			} else if (node instanceof OrdinaryVariable){
				ovNodeActive = (OrdinaryVariable)node;
				setOrdVariableNodeActive((OrdinaryVariable)node);
				mebnEditionPane.setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_OVARIABLE); 
			} else {
				// unknown node
				Debug.println(this.getClass(), "Unknown type of node: " + node);
				mebnEditionPane.setDescriptionText(node.getDescription(), null); // null means default icon
				this.nodeActive = node;
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getSelectedNode()
	 */
	public Node getSelectedNode() {
		return nodeActive;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#unselectNodes()
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#updateFormulaActiveContextNode()
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertOrdinaryVariable(double, double)
	 */
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
		
//		Type type = TypeContainer.getDefaultType();
		Type type = getInitialType();
		
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
	
	/**
	 * @return this method will return a type which may be considered as the initial type 
	 * when entities are created. This method will basically return the first type
	 * which is not {@link TypeContainer#typeBoolean}, {@link TypeContainer#typeCategoryLabel}, {@link TypeContainer#typeLabel},
	 * or the root type.
	 * @see TypeContainer#getDefaultType()
	 * @see #insertOrdinaryVariable(double, double)
	 */
	public Type getInitialType() {
		
		// extract some containers we'll be using to check types
		TypeContainer typeContainer = multiEntityBayesianNetwork.getTypeContainer();	// this is the main container of types
		ObjectEntityContainer objectEntityContainer = multiEntityBayesianNetwork.getObjectEntityContainer();	// this will be later to check hierarchy of entities
		if (typeContainer == null || objectEntityContainer == null) {
			return TypeContainer.getDefaultType();
		}
		
		// only consider types we know about
		Set<Type> knownTypes = typeContainer.getListOfTypes();
		if (knownTypes == null) {
			return TypeContainer.getDefaultType();
		}
		
		// search for some reasonable type
		for (Type type : knownTypes) {
			
			// ignore invalid types
			if (type == null) {
				continue;
			}
			
			// ignore boolean, type label, and categorical at this point
			if (type.equals(typeContainer.typeBoolean)
					|| type.equals(typeContainer.typeCategoryLabel)
					|| type.equals(typeContainer.typeLabel)) {
				continue;
			}
			
			// check if this is a root type
			// TODO avoid using object entities to check for type hierarchy
			boolean isRoot = false;
			for (Object entity : type.getIsTypeOfList()) {
				if (entity instanceof ObjectEntity) {
					List<ObjectEntity> parents = objectEntityContainer.getParentsOfObjectEntity((ObjectEntity) entity);
					if (parents == null || parents.isEmpty()) {
						isRoot = true;
						break;
					}
				}
			}
			
			// do not return root types
			if (isRoot) {
				continue;
			}
			
			return type;
		}
		
		
		// if nothing was found, use the default
		return TypeContainer.getDefaultType();
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameOrdinaryVariable(unbbayes.prs.mebn.OrdinaryVariable, java.lang.String)
	 */
	public void renameOrdinaryVariable(OrdinaryVariable ov, String name) 
	               throws DuplicatedNameException, ReservedWordException{
		
		   checkName(name); 
		   ov.setName(name); 
	       ov.updateLabel();
	       mebnEditionPane.getNetworkWindow().getGraphPane().update(); 
		   mebnEditionPane.getEditOVariableTab().update(); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setOrdinaryVariableType(unbbayes.prs.mebn.OrdinaryVariable, unbbayes.prs.mebn.entity.Type)
	 */
	public void setOrdinaryVariableType(OrdinaryVariable ov, Type type){
		   ov.setValueType(type); 
		   ov.updateLabel(); 
		
		   //by Young
	 	   mebnEditionPane.getNetworkWindow().getGraphPane().update(); 
		   mebnEditionPane.getEditOVariableTab().update(); 
		
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addNewOrdinaryVariableInMFrag()
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addNewOrdinaryVariableInResident()
	 */
	public OrdinaryVariable addNewOrdinaryVariableInResident() throws OVariableAlreadyExistsInArgumentList,
	                                                                  ArgumentNodeAlreadySetException{

		OrdinaryVariable ov;
		ov = addNewOrdinaryVariableInMFrag();
		addOrdinaryVariableInResident(ov);

		return ov;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeOrdinaryVariableOfMFrag(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void removeOrdinaryVariableOfMFrag(OrdinaryVariable ov){

        MFrag currentMFrag = multiEntityBayesianNetwork.getCurrentMFrag();
        currentMFrag.removeOrdinaryVariable(ov);

	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#addOrdinaryVariableInResident(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void addOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable) throws ArgumentNodeAlreadySetException,
	                                                                                    OVariableAlreadyExistsInArgumentList{

		residentNodeActive.addArgument(ordinaryVariable, true);
		mebnEditionPane.getEditArgumentsTab().update();
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeOrdinaryVariableInResident(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void removeOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable){

		ResidentNode resident = (ResidentNode) this.getScreen().getGraphPane().getSelected();
		resident.removeArgument(ordinaryVariable);
		ordinaryVariable.removeIsOVariableOfList(resident);
		
		mebnEditionPane.getEditArgumentsTab().update();
		mebnEditionPane.getNetworkWindow().getGraphPane().update(); 

	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setOVariableSelectedInResidentTree(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void setOVariableSelectedInResidentTree(OrdinaryVariable oVariableSelected){
		mebnEditionPane.getEditArgumentsTab().setTxtName(oVariableSelected.getName());
		mebnEditionPane.getEditArgumentsTab().setTreeResidentActive();
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setOVariableSelectedInMFragTree(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void setOVariableSelectedInMFragTree(OrdinaryVariable oVariableSelected){
		mebnEditionPane.getEditArgumentsTab().setTxtName(oVariableSelected.getName());
		mebnEditionPane.getEditArgumentsTab().setTreeMFragActive();
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameOVariableOfResidentTree(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameOVariableOfMFragTree(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameOVariableInArgumentEditionPane(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#selectOVariableInEdit(unbbayes.prs.mebn.OrdinaryVariable)
	 */
	public void selectOVariableInEdit(OrdinaryVariable ov){
	    OVariableEditionPane editionPane = mebnEditionPane.getEditOVariableTab();
		editionPane.setNameOVariableSelected(ov.getName());
		//editionPane.setTypeOVariableSelected(ov.getType());

	}

	
	
	/*-------------------------------------------------------------------------*/
	/* Object Entities                                                         */
	/*-------------------------------------------------------------------------*/

	/**
	 * Using List implementation in ObjectEntityContainer
	 * @see unbbayes.controller.mebn.IMEBNMediator#createObjectEntity()
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
	 * Using Tree implementation in {@link ObjectEntityContainer}
	 * @see unbbayes.controller.mebn.IMEBNMediator#createObjectEntity(unbbayes.prs.mebn.entity.ObjectEntity)
	 */
	public ObjectEntity createObjectEntity(ObjectEntity parentObjectEntity) throws TypeException{
		
		if(parentObjectEntity == null) {
			return createObjectEntity();
		}

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

		ObjectEntity objectEntity = multiEntityBayesianNetwork.getObjectEntityContainer().createObjectEntity(name,parentObjectEntity);
		multiEntityBayesianNetwork.getNamesUsed().add(name); 
		
		mebnEditionPane.getToolBarOVariable().updateListOfTypes(); 
		
		return objectEntity;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameObjectEntity(unbbayes.prs.mebn.entity.ObjectEntity, java.lang.String)
	 */
	public void renameObjectEntity(ObjectEntity entity, String name)
				throws TypeAlreadyExistsException, ReservedWordException{
	            //throws TypeAlreadyExistsException, DuplicatedNameException, ReservedWordException{
		
		Set<String> namesUsed = multiEntityBayesianNetwork.getNamesUsed();
		ObjectEntityContainer container = multiEntityBayesianNetwork.getObjectEntityContainer();
		
		checkName(name);
		
		namesUsed.remove(entity.getName());
		//multiEntityBayesianNetwork.getNamesUsed().remove(name); 
		
		if(namesUsed.contains(name)) {
			
			for(ObjectEntity parent: container.getParentsOfObjectEntity(entity)) {
				try {
					container.createObjectEntity(name,parent);
				} catch (TypeException e) {
					e.printStackTrace();
				}
			}
			
			try {
				container.removeEntity(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		else {
			namesUsed.add(name);
			container.renameEntity(entity, name);
			mebnEditionPane.getToolBarOVariable().updateListOfTypes();
		}
 
	}
	
	
    /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeObjectEntity(unbbayes.prs.mebn.entity.ObjectEntity)
	 */
	public void removeObjectEntity(ObjectEntity entity) throws Exception{
		
		ObjectEntityContainer container = multiEntityBayesianNetwork.getObjectEntityContainer();
		
		List<ObjectEntity> entitiesToBeExcluded = container.getDescendantsAndSelf(entity);
		container.removeEntity(entity);

		for(ObjectEntity excludedEntity: entitiesToBeExcluded) {
			multiEntityBayesianNetwork.getNamesUsed().remove(excludedEntity.getName());
		}

		// Will always raise an exception because the getObjectEntityContainer().removeEntity() contains 
		// a method, called delete(), thats remove the same entity.getType() Type. 
//		try{
//			multiEntityBayesianNetwork.getTypeContainer().removeType(entity.getType());
//		}
//		catch(Exception e){
//            e.printStackTrace(); 
//		}
		
		mebnEditionPane.getToolBarOVariable().updateListOfTypes(); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setIsOrdereableObjectEntityProperty(unbbayes.prs.mebn.entity.ObjectEntity, boolean)
	 */
	public void setIsOrdereableObjectEntityProperty(ObjectEntity entity, boolean isOrdereable) throws ObjectEntityHasInstancesException{
		entity.setOrdereable(isOrdereable); 
	}
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Object Entities Instances                                               */
	/*-------------------------------------------------------------------------*/

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#createEntityIntance(unbbayes.prs.mebn.entity.ObjectEntity, java.lang.String)
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#createEntityIntanceOrdereable(unbbayes.prs.mebn.entity.ObjectEntity, java.lang.String, unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#renameEntityIntance(unbbayes.prs.mebn.entity.ObjectEntityInstance, java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeEntityInstance(unbbayes.prs.mebn.entity.ObjectEntityInstance)
	 */
	public void removeEntityInstance(ObjectEntityInstance entity) {
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(entity);
		multiEntityBayesianNetwork.getNamesUsed().remove(entity.getName()); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#removeEntityInstanceOrdereable(unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable)
	 */
	public void removeEntityInstanceOrdereable(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.removeEntityInstanceOrdereableReferences(entity);	
		multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(entity);
		multiEntityBayesianNetwork.getNamesUsed().remove(entity.getName()); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#upEntityInstance(unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable)
	 */
	public void upEntityInstance(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.upEntityInstance(entity);
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#downEntityInstance(unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable)
	 */
	public void downEntityInstance(ObjectEntityInstanceOrdereable entity) {
		ObjectEntityInstanceOrdereable.downEntityInstance(entity);
	}
	
	
	
	/*-------------------------------------------------------------------------*/
	/*Findings                                                                 */
	/*-------------------------------------------------------------------------*/
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#createRandomVariableFinding(unbbayes.prs.mebn.ResidentNode, unbbayes.prs.mebn.entity.ObjectEntityInstance[], unbbayes.prs.mebn.entity.Entity)
	 */
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#saveCPT(unbbayes.prs.mebn.ResidentNode, java.lang.String)
	 */
	public void saveCPT(IResidentNode residentNode, String cpt){
		residentNode.setTableFunction(cpt);
	}
	
	/**
	 * This method will use {@link #getLPDFrameFactory()} to instantiate a JFrame for editing the LPD script.
	 * @see unbbayes.controller.mebn.IMEBNMediator#openCPTDialog(unbbayes.prs.mebn.ResidentNode)
	 */
	public void openCPTDialog(ResidentNode residentNode){
		
		JFrame cptEditionPane = mapCpt.get(residentNode); 
		
		if (!isEnableLPDEditorCache() && cptEditionPane != null) {
			// dispose cache, so that it can be reloaded
			closeCPTDialog(residentNode);
			cptEditionPane = null;
		}
		
		if(cptEditionPane == null){
//			cptEditionPane = new CPTFrame(this, residentNode);
			cptEditionPane = getLPDFrameFactory().buildCPTFrame(this, residentNode);
			mapCpt.put(residentNode, cptEditionPane); 
			cptEditionPane.setVisible(true); 
		}else{
			cptEditionPane.setVisible(true); 
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#closeCPTDialog(unbbayes.prs.mebn.ResidentNode)
	 */
	public void closeCPTDialog(ResidentNode residentNode){
		JFrame cptEditionPane = mapCpt.get(residentNode); 
		if(cptEditionPane != null){
			cptEditionPane.dispose(); 
			mapCpt.remove(residentNode); 
		}
	}
	
	/**
	 * Closes all dialogs related to LPD editors.
	 * This is equivalent to calling {@link #closeCPTDialog(ResidentNode)}
	 * for all resident nodes that were handled by {@link #openCPTDialog(ResidentNode)}.
	 */
	public void closeAllCPTDialogs() {
		if (mapCpt != null) {
			for (ResidentNode residentNode : mapCpt.keySet()) {
				closeCPTDialog(residentNode);
			}
		}
	}
	
	/**
	 * Returns a CPTFrame to edit CPT.
	 * If no instance of CPTFrame is known, it returns null.
	 * @deprecated use {@link #getCPTEditionFrame(ResidentNode)} instead
	 */
	public CPTFrame getCPTDialog(ResidentNode residentNode){
		JFrame frame = this.getCPTEditionFrame(residentNode);
		if (frame instanceof CPTFrame) {
			return (CPTFrame)frame;
		}
		return null; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getCPTEditionFrame(unbbayes.prs.mebn.ResidentNode)
	 */
	public JFrame getCPTEditionFrame(ResidentNode residentNode) {
		return mapCpt.get(residentNode);
	}
	
	/*-------------------------------------------------------------------------*/
	/* Knowledge Base                                                          */
	/*-------------------------------------------------------------------------*/


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getKnowledgeBase()
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
		
//		KnowledgeBase knowledgeBase = getKnowledgeBase();
//
//		for(ObjectEntity entity: multiEntityBayesianNetwork.getObjectEntityContainer().getListEntity()){
//			knowledgeBase.createEntityDefinition(entity);
//		}
//
//		for(MFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
//			for(ResidentNode resident: mfrag.getResidentNodeList()){
//				knowledgeBase.createRandomVariableDefinition(resident);
//			}
//		}
		
		// the above code was substituted by the following
		
		getKnowledgeBase().createGenerativeKnowledgeBase(multiEntityBayesianNetwork);
		
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
			for(IResidentNode residentNode : mfrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
					if (isToUseSimpleSoftEvidenceInKB() || !(finding.getState() instanceof SoftEvidenceEntity)) {
						// if this is hard evidence, add to KB. If we can add soft evidence in KB, then just add it.
						knowledgeBase.insertRandomVariableFinding(finding); 
					}
				}
			}
		}
		
		if(saveDebugFiles){
			this.saveDefaultTemporaryFindingsFile();
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#clearFindingsIntoGUI()
	 */
	public void clearFindingsIntoGUI(){
		
		MultiEntityBayesianNetwork multiEntityBayesianNetwork = getMultiEntityBayesianNetwork();
		
		for(MFrag mfrag: multiEntityBayesianNetwork.getDomainMFragList()){
			for(IResidentNode residentNode : mfrag.getResidentNodeList()){
				residentNode.cleanRandomVariableFindingList(); 
			}
		}
		
		
		// clear instances of all known entities
		// we need to iterate on all instances, because mebn keeps track of names (so we also need to remove the name from mebn)
		for (ObjectEntityInstance instance : new ArrayList<ObjectEntityInstance>(multiEntityBayesianNetwork.getObjectEntityContainer().getListEntityInstances())) {
			multiEntityBayesianNetwork.getObjectEntityContainer().removeEntityInstance(instance);
			multiEntityBayesianNetwork.getNamesUsed().remove(instance.getName());
			Debug.println(getClass(), "Removed entity instance: " + instance);
		}
		
		clearKnowledgeBase();
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#clearKnowledgeBase()
	 */
	public void clearKnowledgeBase(){
		getKnowledgeBase().clearKnowledgeBase();
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#saveGenerativeMTheory(java.io.File)
	 */
	public void saveGenerativeMTheory(File file){
		getKnowledgeBase().saveGenerativeMTheory(getMultiEntityBayesianNetwork(), file);
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#saveFindingsFile(java.io.File)
	 */
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#loadFindingsFile(java.io.File)
	 */
	public void loadFindingsFile(File file) throws UBIOException, MEBNException{
		
		// avoid GUI specific routines
//		mebnEditionPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		mebnEditionPane.setStatus(resource.getString("statusLoadingKB")); 
		
		Exception lastException = null;
		
		//init the powerloom knowledge base
		createKnowledgeBase(); 	
		
		long initialTime = System.currentTimeMillis(); 
		
		getKnowledgeBase().loadModule(file, true);
		
		long finalTime = System.currentTimeMillis(); 
		
		System.out.println("Load time = " + (finalTime - initialTime) + "ms (without time to load on GUI)");
		
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
		
        finalTime = System.currentTimeMillis(); 
		
		System.out.println("Load time = " + (finalTime - initialTime) + "ms (with time to load on GUI)");
		
		
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
		clearKnowledgeBase();
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#executeQuery(java.util.List)
	 */
	public Network executeQuery(List<Query> listQueries)
										    throws InconsistentArgumentException, 
										    SSBNNodeGeneralException, 
										    ImplementationRestrictionException, 
										    MEBNException, 
										    OVInstanceFaultException, InvalidParentException {
		
		Network ret = null; 
		
		mebnEditionPane.setStatus(resource.getString("statusGeneratingSSBN")); 
		this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		// use the public method instead
	    this.resetKnowledgeBase(); 	
		
	    if (this.getSSBNGenerator() instanceof IMediatorAwareSSBNGenerator) {
	    	((IMediatorAwareSSBNGenerator)this.getSSBNGenerator()).setMediator(this);
	    }
	    
	    if (Debug.isDebugMode()) {
	    	Runtime runtime = Runtime.getRuntime();
	    	long memory = runtime.totalMemory() - runtime.freeMemory();
	    	System.out.println("Used memory in bytes: " + memory);
	    	runtime.gc();
	    	memory = runtime.totalMemory() - runtime.freeMemory();
	    	System.out.println("Used memory in bytes: " + memory);
	    }
	    
	    ssbn = this.getSSBNGenerator().generateSSBN(listQueries, getKnowledgeBase()); 
	    if (Debug.isDebugMode()) {
	    	Runtime runtime = Runtime.getRuntime();
	    	long memory = runtime.totalMemory() - runtime.freeMemory();
	    	System.out.println("Used memory in bytes: " + memory);
	    	runtime.gc();
	    	memory = runtime.totalMemory() - runtime.freeMemory();
	    	System.out.println("Used memory in bytes: " + memory);
	    }
	    
	    
	    
		// show on display
		showSSBN(ssbn);
	    
//	    System.out.println("Used memory in bytes: " + memory);
	    
		if (ssbn != null) {
			ret = ssbn.getNetwork();
			if (ret != null && (ret instanceof ProbabilisticNetwork)) {
				// TODO remove the need to use the same panel for edition and compilation -> use different JInternalFrames for compiled network
				setSpecificSituationBayesianNetwork((ProbabilisticNetwork)ret);
			}
			
			try {
				
				if (ssbn.getWarningList().size() > 0){
					openWarningDialog(); 	
				}
				
				// logging probabilities of the nodes
				this.logNodesAndItsProbabilities(ssbn);
				
			} catch (Exception e) {
				e.printStackTrace(); 
				this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				JOptionPane.showMessageDialog(getScreen(), 
						e.getMessage());
			}
		}


		mebnEditionPane.setStatus(resource.getString("statusReady")); 
		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		mebnEditionPane.setStatus(resource.getString("statusReady")); 
		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		return ret ;    
	}

	/**
	 * Uses mediator to display the SSBN
	 * @param mediator : MEBNController
	 * @param  ssbn : the ssbn to show
	 */
	protected void showSSBN(SSBN ssbn) {

//		NetworkWindow window = new NetworkWindow(ssbn.getNetwork());
//		this.getMediator().getScreen().getUnbbayesFrame().addWindow(window);
//		window.setVisible(true);
		// use the above code to show compiled network in a separate internal frame
		
		setSpecificSituationBayesianNetwork(ssbn.getProbabilisticNetwork());
		
		// the following line was commented out because the controller must not overwrite the configuration (value of the attribute) provided by the invoker.
		// If this attribute must be true, then the invoker should explicitly set it from outside, not here.
//		setToTurnToSSBNMode(true);	// if this is false, ((IMEBNMediator)this.getMediator()).turnToSSBNMode() will not work
		
		turnToSSBNMode();
		
		// the following line was migrated to turnToSSBNMode, because this is sensitive to how turnToSSBNMode is implemented
//		getScreen().getEvidenceTree().updateTree(true);
		
	}
	
	/** 
	 * @see unbbayes.controller.mebn.IMEBNMediator#executeQueryLaskeyAlgorithm(java.util.List)
	 * @deprecated use {@link #executeQuery(List)} instead
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
		setSpecificSituationBayesianNetwork(probabilisticNetwork);
		
		try {

			ssbn.compileAndInitializeSSBN();

			if (ssbn.getWarningList().size() > 0){
				openWarningDialog(); 	
			}
			
			// logging probabilities of the nodes
			this.logNodesAndItsProbabilities(ssbn);
			
			this.getMebnEditionPane().getNetworkWindow().changeToSSBNCompilationPane(getSpecificSituationBayesianNetwork());

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
		
		return getSpecificSituationBayesianNetwork() ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getSSBNGenerator()
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setSSBNGenerator(unbbayes.prs.mebn.ssbn.ISSBNGenerator)
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
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#openWarningDialog()
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
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#closeWarningDialog()
	 */
	public void closeWarningDialog(){
		if(warningDialog != null){
			warningDialog.dispose();
			warningDialog = null; 
		}
	}
	
	
	
	
	
	/*--------------------------------------------------------------------------
	 * ATENï¾ƒï¿½ã‚°: ESTES Mï¾ƒæ¬�ODOS Sï¾ƒã‚° Cï¾ƒæ’¤IAS DOS Mï¾ƒæ¬�ODOS PRESENTES EM SENCONTROLLER...
	 * DEVIDO A FALTA DE TEMPO, AO INVï¾ƒå”„ DE FAZER UM REFACTORY PARA COLOCï¾ƒÂ´OS NO
	 * NETWORKCONTROLLER, DEIXANDO ACESSIVEL AO SENCONTROLLER E AO MEBNCONTROLLER, 
	 * VOU APENAS ADAPTï¾ƒÂ´OS AQUI PARA O USO NO MEBNCONTROLLER... MAS DEPOIS ISTO
	 * NECESSITARï¾ƒï¿½DE UM REFACTORY PARA MANTER AS BOAS PRï¾ƒã‚�ICAS DA PROGRAMAï¾ƒï¿½ã‚° 
	 * E PARA FACILITAR A MANUTENï¾ƒï¿½ã‚°. (laecio santos)
	 *--------------------------------------------------------------------------/
	
		/** Load resource file from this package */
	private static ResourceBundle resourcePN = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.mebn.resources.Resources.class.getName());

	private ICPTFrameFactory lpdFrameFactory = DEFAULT_CPT_FRAME_FACTORY;
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#compileNetwork(unbbayes.prs.bn.ProbabilisticNetwork)
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

		/* isto serï¾ƒï½¡ feito dentro do changeToSSBNCompilationPane */
//		screen.getEvidenceTree().updateTree();  hehe... ainda nï¾ƒï½£o temos uma evidence tree... sorry!
		

		this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		//TODO controle of status for the mebn edition pane 
//		screen.setStatus(resource.getString("statusTotalTime")
//				+ df.format(((System.currentTimeMillis() - ini)) / 1000.0)
//				+ resource.getString("statusSeconds"));
		return true;

	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#initialize()
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
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#propagate()
	 */
	public void propagate() {
		this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		boolean temLikeliHood = false;
		try {
			getSpecificSituationBayesianNetwork().updateEvidences();
			if (!temLikeliHood) {
				this.getScreen().setStatus(resourcePN
						.getString("statusEvidenceProbabilistic")
						+ df.format(getSpecificSituationBayesianNetwork().PET() * 100.0));
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
	 * FIM DOS Mï¾ƒæ¬�ODOS Cï¾ƒæ’¤IA
	 *-------------------------------------------------------------------------/
	
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.INetworkMediator#saveNetImage()
	 */
    public void saveNetImage(GraphPane pane) {
        String images[] = { "PNG", "JPG", "GIF", "BMP" };
        JFileChooser chooser = new JFileChooser(FileHistoryController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);

        chooser.setFileView(new FileIcon(getScreen()));
        chooser.addChoosableFileFilter(new SimpleFileFilter( images, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(getScreen());
        if (opcao == JFileChooser.APPROVE_OPTION) {
        	Rectangle r = new Rectangle(0, 0, (int)pane.getBiggestPoint().x, (int)pane.getBiggestPoint().y);
        	Component comp = pane.getGraphViewport();
        	File file = new File(chooser.getSelectedFile().getPath());
        	
        	boolean imageSaved = saveComponentAsImage(comp, r.width, r.height, file);
        	
        	if(imageSaved){
        		FileHistoryController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
        		JOptionPane.showMessageDialog(getScreen(), resource.getString("saveSucess"));
        	}
        	
        }
    }
	
	/**
	 * @return false if don't have one ssbn pre-generated. True if the mode is change. 
	 */
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#turnToSSBNMode()
	 */
	public boolean turnToSSBNMode(){
		if (isToTurnToSSBNMode ) {
			ProbabilisticNetwork specificSituationBayesianNetwork = getSpecificSituationBayesianNetwork();
			if(specificSituationBayesianNetwork != null){
				showSSBNGraph = true; 
				this.getMebnEditionPane().getNetworkWindow().changeToSSBNCompilationPane(specificSituationBayesianNetwork);			
				this.getMebnEditionPane().getNetworkWindow().getGraphPane().setGraphDimension(dimensionSSBNGraph); 
				this.getMebnEditionPane().getNetworkWindow().getGraphPane().update(); 
				
				// the following line was migrated from showSSBN to here, because evidence tree should be updated only if we are actually turning the card layout to ssbn mode
				getScreen().getEvidenceTree().updateTree(true);
				
				return true;  
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Changes to MTheory view.
	 * 
	 * @return false if there is no MTheory to show, and true if the mode is 
	 * successfuly changed.
	 */
	public boolean turnToMTheoryMode(){
		if(multiEntityBayesianNetwork != null){
//			showSSBNGraph = true; 
			this.getMebnEditionPane().getNetworkWindow().changeToMTheoryPane();			
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getMultiEntityBayesianNetwork()
	 */
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		return multiEntityBayesianNetwork;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setMultiEntityBayesianNetwork(unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void setMultiEntityBayesianNetwork(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork) {
		this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getMebnEditionPane()
	 */
	public MEBNEditionPane getMebnEditionPane() {
		return mebnEditionPane;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setMebnEditionPane(unbbayes.gui.mebn.MEBNEditionPane)
	 */
	public void setMebnEditionPane(MEBNEditionPane mebnEditionPane) {
		this.mebnEditionPane = mebnEditionPane;
	}

	public void setKnowledgeBaseToolBar(JToolBar toolbar){
		this.getMebnEditionPane().setKnowledgeBaseToolBar(toolbar);  
	}
	
	public void setDefaultKnowledgeBaseToolBar(){
		this.getMebnEditionPane().setDefaultKnowledgeBaseToolBar();
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getSpecificSituationBayesianNetwork()
	 */
	public ProbabilisticNetwork getSpecificSituationBayesianNetwork() {
		IInferenceAlgorithm algorithm = getBNAlgorithm();
		if (algorithm != null) {
			return (ProbabilisticNetwork) algorithm.getNetwork();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getSSBN()
	 */
	public SSBN getSSBN(){
		return ssbn; 
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setSpecificSituationBayesianNetwork(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
	public void setSpecificSituationBayesianNetwork(
			ProbabilisticNetwork specificSituationBayesianNetwork) {
		IInferenceAlgorithm algorithm = getBNAlgorithm();
		if (algorithm != null) {
			algorithm.setNetwork(specificSituationBayesianNetwork);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#isShowSSBNGraph()
	 */
	public boolean isShowSSBNGraph() {
		return showSSBNGraph;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setShowSSBNGraph(boolean)
	 */
	public void setShowSSBNGraph(boolean showSSBNGraph) {
		this.showSSBNGraph = showSSBNGraph;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setEditionMode()
	 */
	public void setEditionMode(){
		showSSBNGraph = false; 
	}

	private void printArgumentsOfResidentNode(ResidentNode resident){
		Debug.println("Resident: " + resident);
		for(Argument arg: resident.getArgumentList()){
			Debug.println(" [" + arg.getArgNumber() + "]:" + arg.getOVariable());
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#isToLogNodesAndProbabilities()
	 */
	public boolean isToLogNodesAndProbabilities() {
		return toLogNodesAndProbabilities;
	}


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setToLogNodesAndProbabilities(boolean)
	 */
	public void setToLogNodesAndProbabilities(boolean toLogNodesAndProbabilities) {
		this.toLogNodesAndProbabilities = toLogNodesAndProbabilities;
	}
	
	
	
	// Methods for backward-compatibilities
	
	 /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertResidentNode(double, double)
	 */
    
    public Node insertResidentNode(double x, double y) throws MFragDoesNotExistException{
    	return this.insertDomainResidentNode(x, y);
    }
    
    /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#insertInputNode(double, double)
	 */
    public Node insertInputNode(double x, double y) throws MFragDoesNotExistException{
    	return this.insertGenerativeInputNode(x,y);
    }
    
   /*
    * (non-Javadoc)
    * @see unbbayes.controller.NetworkController#getNetwork()
    */
    /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getNetwork()
	 */
    public Network getNetwork() {
    	return multiEntityBayesianNetwork;
    }
    
    /*
     * (non-Javadoc)
     * @see unbbayes.controller.NetworkController#getGraph()
     */
    /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getGraph()
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
    /* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#unselectAll()
	 */
    public void unselectAll(){
    	// as you see, it only unselects nodes
    	if (multiEntityBayesianNetwork != null){
    		this.unselectNodes(); 
    	}    	
    }


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getMebnIOExtensionPointID()
	 */
	public String getMebnIOExtensionPointID() {
		return mebnIOExtensionPointID;
	}


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setMebnIOExtensionPointID(java.lang.String)
	 */
	public void setMebnIOExtensionPointID(String mebnIOExtensionPointID) {
		this.mebnIOExtensionPointID = mebnIOExtensionPointID;
	}


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getMebnModulePluginID()
	 */
	public String getMebnModulePluginID() {
		return mebnModulePluginID;
	}


	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setMebnModulePluginID(java.lang.String)
	 */
	public void setMebnModulePluginID(String mebnModulePluginID) {
		this.mebnModulePluginID = mebnModulePluginID;
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setKnowledgeBase(unbbayes.prs.mebn.kb.KnowledgeBase)
	 */
	public void setKnowledgeBase(KnowledgeBase kb) {
		this.knowledgeBase = kb;
		this.resetKnowledgeBase();
	}
	
	public void setKnowledgeBaseTypeName(String type){
		mebnEditionPane.setKBName(type);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#resetKnowledgeBase()
	 */
	public void resetKnowledgeBase() {
		this.createKnowledgeBase();
	}

	/**
	 * This map is used by {@link IMEBNMediator#getProperty(String)} and {@link IMEBNMediator#getProperty(String)#setPropertyMap(Map)}
	 * @return the propertyMap
	 */
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}

	/**
	 * This map is used by {@link IMEBNMediator#getProperty(String)} and {@link IMEBNMediator#getProperty(String)#setPropertyMap(Map)}
	 * @param propertyMap the propertyMap to set
	 */
	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return this.getPropertyMap().get(key);
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String key, Object obj) {
		this.getPropertyMap().put(key, obj);
	}

	/**
	 * @return the pluginNodeManager
	 */
	public MEBNPluginNodeManager getPluginNodeManager() {
		return pluginNodeManager;
	}

	/**
	 * @param pluginNodeManager the pluginNodeManager to set
	 */
	public void setPluginNodeManager(MEBNPluginNodeManager pluginNodeManager) {
		this.pluginNodeManager = pluginNodeManager;
	}

	/**
	 * If this is false, {@link #turnToSSBNMode()} will return always false.
	 * @return the isToTurnToSSBNMode
	 */
	public boolean isToTurnToSSBNMode() {
		return isToTurnToSSBNMode;
	}

	/**
	 * If this is false, {@link #turnToSSBNMode()} will return always false.
	 * @param isToTurnToSSBNMode the isToTurnToSSBNMode to set
	 */
	public void setToTurnToSSBNMode(boolean isToTurnToSSBNMode) {
		this.isToTurnToSSBNMode = isToTurnToSSBNMode;
	}

	/**
	 * @return the ssbnAlgorithm
	 */
	public IInferenceAlgorithm getBNAlgorithm() {
		if (ssbnAlgorithm == null) {
			ssbnAlgorithm = new JunctionTreeAlgorithm();
		}
		return ssbnAlgorithm;
	}

	/**
	 * @param ssbnAlgorithm the ssbnAlgorithm to set
	 */
	public void setBNAlgorithm(IInferenceAlgorithm ssbnAlgorithm) {
		this.ssbnAlgorithm = ssbnAlgorithm;
	}

	/**
	 * @return the isToUseSimpleSoftEvidenceInKB
	 */
	public boolean isToUseSimpleSoftEvidenceInKB() {
		return isToUseSimpleSoftEvidenceInKB;
	}

	/**
	 * @param isToUseSimpleSoftEvidenceInKB the isToUseSimpleSoftEvidenceInKB to set
	 */
	public void setToUseSimpleSoftEvidenceInKB(boolean isToUseSimpleSoftEvidenceInKB) {
		this.isToUseSimpleSoftEvidenceInKB = isToUseSimpleSoftEvidenceInKB;
	}


	/**
	 * @return the isToIncludeSoftEvidences : set this to true in order
	 * to activate the simple soft/likelihood evidence feature (the feature to add
	 * soft/likelihood evidences in a propositional manner from MEBN GUI but without using the Finding MFrags).
	 * @see unbbayes.gui.mebn.finding.FindingArgumentPane
	 */
	public boolean isToIncludeSoftEvidences() {
		return isToIncludeSoftEvidences;
	}

	/**
	 * @param isToIncludeSoftEvidences the isToIncludeSoftEvidences to set: set this to true in order
	 * to activate the simple soft/likelihood evidence feature (the feature to add
	 * soft/likelihood evidences in a propositional manner from MEBN GUI but without using the Finding MFrags).
	 * @see unbbayes.gui.mebn.finding.FindingArgumentPane
	 */
	public void setToIncludeSoftEvidences(boolean isToIncludeSoftEvidences) {
		this.isToIncludeSoftEvidences = isToIncludeSoftEvidences;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#getLPDFrameFactory()
	 */
	public ICPTFrameFactory getLPDFrameFactory() {
		if (lpdFrameFactory == null) {
			lpdFrameFactory = DEFAULT_CPT_FRAME_FACTORY;
		}
		return lpdFrameFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.mebn.IMEBNMediator#setLPDFrameFactory(unbbayes.gui.mebn.cpt.ICPTFrameFactory)
	 */
	public void setLPDFrameFactory(ICPTFrameFactory lpdFrameFactory) {
		if (this.lpdFrameFactory != lpdFrameFactory) {
			// dispose all the dialogs that are currently open if factory is being changed
			try {
				this.closeAllCPTDialogs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.lpdFrameFactory = lpdFrameFactory;
	}

	/**
	 * @return If true, {@link #openCPTDialog(ResidentNode)} will always re-instantiate a new LPD dialog.
	 */
	public boolean isEnableLPDEditorCache() {
		return enableLPDEditorCache;
	}

	/**
	 * @param enableLPDEditorCache : If true, {@link #openCPTDialog(ResidentNode)} will always re-instantiate a new LPD dialog.
	 */
	public void setEnableLPDEditorCache(boolean enableLPDEditorCache) {
		this.enableLPDEditorCache = enableLPDEditorCache;
	}

	
	
	
}
