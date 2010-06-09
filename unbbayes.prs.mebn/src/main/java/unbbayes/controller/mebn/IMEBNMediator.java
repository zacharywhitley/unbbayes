package unbbayes.controller.mebn;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.exception.InvalidOperationException;
import unbbayes.gui.mebn.MEBNEditionPane;
import unbbayes.gui.mebn.cpt.CPTFrame;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.extension.jpf.PluginAwareFileExtensionIODelegator;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
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
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * This is the extracted interface for MEBNController.
 * This is a Mediator design pattern since almost all communication is managed using
 * this.
 * @author Shou Matsumoto
 *
 */
public interface IMEBNMediator extends INetworkMediator  {

	public abstract void openPanel(ResidentNode node);

	public abstract void setResetButtonActive();

	public abstract ResidentNode getResidentNodeActive();

	public abstract InputNode getInputNodeActive();

	public abstract ContextNode getContextNodeActive();

	public abstract Node getNodeActive();

	public abstract void enableMTheoryEdition();

	/**
	 * Set the name of the MTheory active.
	 * @param name The new name
	 */
	public abstract void renameMTheory(String name)
			throws DuplicatedNameException, ReservedWordException;

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
	public abstract void setDescriptionTextForSelectedObject(String text);

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

	public abstract boolean insertEdge(Edge edge)
			throws MEBNConstructionException, CycleFoundException;

	public abstract void insertDomainMFrag();

	public abstract void removeDomainMFrag(MFrag domainMFrag);

	/**
	 * Set the mFrag how the active MFrag and show its graph. 
	 * Show the tool bar of edition of the MFrag. 
	 */
	public abstract void setCurrentMFrag(MFrag mFrag);

	/**
	 * rename the MFrag and update its name in the title of the graph
	 * @param mFrag
	 * @param name
	 * @throws ReservedWordException 
	 */
	public abstract void renameMFrag(MFrag mFrag, String name)
			throws DuplicatedNameException, ReservedWordException;

	public abstract MFrag getCurrentMFrag();

	public abstract ResidentNode insertDomainResidentNode(double x, double y)
			throws MFragDoesNotExistException;

	public abstract void renameDomainResidentNode(ResidentNode resident,
			String newName) throws DuplicatedNameException,
			ReservedWordException;

	/**
	 * Adds a possible value (state) into a resident node...
	 * @param resident
	 * @param value
	 * @throws ReservedWordException 
	 */
	public abstract StateLink addPossibleValue(ResidentNode resident,
			String nameValue) throws DuplicatedNameException,
			ReservedWordException;

	/**
	 * Adds a possible value (state) into a resident node. If the state already
	 * is a possible value of the resident node, nothing is made. 
	 */
	public abstract StateLink addPossibleValue(ResidentNode resident,
			CategoricalStateEntity state);

	public abstract StateLink addObjectEntityAsPossibleValue(
			ResidentNode resident, ObjectEntity state);

	/**
	 * Verifies if a exists a possible value (in the container). 
	 * @param name
	 * @return
	 */
	public abstract boolean existPossibleValue(String name);

	public abstract void setGloballyExclusiveProperty(StateLink state,
			boolean value);

	public abstract void addBooleanAsPossibleValue(ResidentNode resident);

	/**
	 *  Adds a possible value (state) for a resident node...
	 * @param resident
	 * @param value
	 */
	public abstract void removePossibleValue(ResidentNode resident,
			String nameValue);

	public abstract void removeAllPossibleValues(ResidentNode resident);

	public abstract boolean existsPossibleValue(ResidentNode resident,
			String nameValue);

	public abstract void setEnableTableEditionView();

	public abstract void setUnableTableEditionView();

	public abstract InputNode insertGenerativeInputNode(double x, double y)
			throws MFragDoesNotExistException;

	/**
	 * Set the input node for be a instance of a resident node.
	 * Update the graph.
	 *
	 * @param input
	 * @param resident
	 * @throws CycleFoundException
	 */
	public abstract void setInputInstanceOf(InputNode input,
			ResidentNode resident) throws CycleFoundException;

	public abstract void updateArgumentsOfObject(Object node);

	/**
	 * Update the input intance of atribute (in the view) of the input node for the value current
	 * @param input The input node active
	 */
	public abstract void updateInputInstanceOf(InputNode input);

	public abstract ContextNode insertContextNode(double x, double y)
			throws MFragDoesNotExistException;

	public abstract void setActionGraphNone();

	public abstract void setActionGraphCreateEdge();

	public abstract void setActionGraphCreateContextNode();

	public abstract void setActionGraphCreateInputNode();

	public abstract void setActionGraphCreateResidentNode();

	public abstract void setActionGraphCreateOrdinaryVariableNode();

	/**
	 * Delete the selected item of the graph (a node or a edge)
	 */
	public abstract void deleteSelectedItem();

	public abstract void deleteSelected(Object selected);

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#selectNode(unbbayes.prs.Node)
	 */
	public abstract void selectNode(Node node);

	public abstract Node getSelectedNode();

	public abstract void unselectNodes();

	public abstract void updateFormulaActiveContextNode();

	public abstract OrdinaryVariable insertOrdinaryVariable(double x, double y)
			throws MFragDoesNotExistException;

	public abstract void renameOrdinaryVariable(OrdinaryVariable ov, String name)
			throws DuplicatedNameException, ReservedWordException;

	public abstract void setOrdinaryVariableType(OrdinaryVariable ov, Type type);

	/**
	 * Create a ordinary variable and add it in the
	 * current MFrag (if it is a DomainMFrag).
	 *
	 */

	public abstract OrdinaryVariable addNewOrdinaryVariableInMFrag();

	/**
	 * Create a new ordinary variable and add this in the resident
	 * node active. Add this in the MFrag list of ordinary variables too.
	 * @return new ordinary variable
	 */
	public abstract OrdinaryVariable addNewOrdinaryVariableInResident()
			throws OVariableAlreadyExistsInArgumentList,
			ArgumentNodeAlreadySetException;

	/**
	 * Remove one ordinary variable of the current MFrag.
	 * @param ov
	 */
	public abstract void removeOrdinaryVariableOfMFrag(OrdinaryVariable ov);

	/**
	 * Add one ordinary variable in the list of arguments of the resident node active.
	 * @param ordinaryVariable ov for add
	 */
	public abstract void addOrdinaryVariableInResident(
			OrdinaryVariable ordinaryVariable)
			throws ArgumentNodeAlreadySetException,
			OVariableAlreadyExistsInArgumentList;

	public abstract void removeOrdinaryVariableInResident(
			OrdinaryVariable ordinaryVariable);

	public abstract void setOVariableSelectedInResidentTree(
			OrdinaryVariable oVariableSelected);

	public abstract void setOVariableSelectedInMFragTree(
			OrdinaryVariable oVariableSelected);

	@Deprecated
	public abstract void renameOVariableOfResidentTree(String name)
			throws DuplicatedNameException, ReservedWordException;

	@Deprecated
	public abstract void renameOVariableOfMFragTree(String name)
			throws DuplicatedNameException, ReservedWordException;

	@Deprecated
	public abstract void renameOVariableInArgumentEditionPane(String name)
			throws DuplicatedNameException, ReservedWordException;

	public abstract void selectOVariableInEdit(OrdinaryVariable ov);

	/**
	 * Adds a new entity with a name passed as its argument.
	 * The entity type will be an automatically generated type, based on
	 * what the user has passed as its argument.
	 */
	public abstract ObjectEntity createObjectEntity() throws TypeException;

	/**
	 * Rename a object entity. 
	 * @param entity
	 * @param name
	 * @throws TypeAlreadyExistsException
	 * @throws ReservedWordException 
	 */
	public abstract void renameObjectEntity(ObjectEntity entity, String name)
			throws TypeAlreadyExistsException, DuplicatedNameException,
			ReservedWordException;

	/**
	 * Remove a object entity. 
	 * @param entity
	 * @throws Exception
	 */
	public abstract void removeObjectEntity(ObjectEntity entity)
			throws Exception;

	/**
	 * Set the property isOrdereable of the entity
	 * @param entity
	 * @param isOrdereable
	 * @throws ObjectEntityHasInstancesException
	 */
	public abstract void setIsOrdereableObjectEntityProperty(
			ObjectEntity entity, boolean isOrdereable)
			throws ObjectEntityHasInstancesException;

	/**
	 * Create a new Object Entity Instance of the Object Entity. 
	 * @param entity
	 * @param nameInstance
	 * @throws EntityInstanceAlreadyExistsException
	 * @throws InvalidOperationException
	 * @throws DuplicatedNameException
	 * @throws ReservedWordException 
	 */
	public abstract void createEntityIntance(ObjectEntity entity,
			String nameInstance) throws EntityInstanceAlreadyExistsException,
			InvalidOperationException, DuplicatedNameException,
			ReservedWordException;

	public abstract void createEntityIntanceOrdereable(ObjectEntity entity,
			String nameInstance, ObjectEntityInstanceOrdereable previous)
			throws EntityInstanceAlreadyExistsException,
			InvalidOperationException, DuplicatedNameException,
			ReservedWordException;

	public abstract void renameEntityIntance(ObjectEntityInstance entity,
			String newName) throws EntityInstanceAlreadyExistsException,
			DuplicatedNameException, ReservedWordException;

	public abstract void removeEntityInstance(ObjectEntityInstance entity);

	public abstract void removeEntityInstanceOrdereable(
			ObjectEntityInstanceOrdereable entity);

	public abstract void upEntityInstance(ObjectEntityInstanceOrdereable entity);

	public abstract void downEntityInstance(
			ObjectEntityInstanceOrdereable entity);

	public abstract void createRandomVariableFinding(ResidentNode residentNode,
			ObjectEntityInstance[] arguments, Entity state);

	public abstract void saveCPT(ResidentNode residentNode, String cpt);

	public abstract void openCPTDialog(ResidentNode residentNode);

	public abstract void closeCPTDialog(ResidentNode residentNode);

	/**
	 * @deprecated use {@link #getCPTEditionFrame(ResidentNode)} instead.
	 */
	public abstract CPTFrame getCPTDialog(ResidentNode residentNode);

	/**
	 * Obtains the currently used knowledge base
	 * @return instance of {@link KnowledgeBase}
	 */
	public abstract KnowledgeBase getKnowledgeBase();

	public abstract void clearFindingsIntoGUI();

	public abstract void clearKnowledgeBase();

	public abstract void saveGenerativeMTheory(File file);

	public abstract void saveFindingsFile(File file);

	public abstract void loadFindingsFile(File file) throws UBIOException,
			MEBNException;

	public abstract Network executeQuery(List<Query> listQueries)
			throws InconsistentArgumentException, SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException;

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
	 * 
	 * @deprecated use {@link #executeQuery(List)} instead
	 */
	public abstract ProbabilisticNetwork executeQueryLaskeyAlgorithm(
			List<Query> listQueries) throws InconsistentArgumentException,
			SSBNNodeGeneralException, ImplementationRestrictionException,
			MEBNException, OVInstanceFaultException, InvalidParentException;

	/**
	 * Obtains the current SSBN generation algorithm used by this controller.
	 * @return a non-null value, instance of {@link ISSBNGenerator}
	 * @see #setSSBNGenerator(ISSBNGenerator)
	 */
	public abstract ISSBNGenerator getSSBNGenerator();

	/**
	 * Sets the {@link ISSBNGenerator} to be used as a SSBN generation
	 * algoritym by this controller.
	 * @param ssbnGenerator
	 * @see #getSSBNGenerator()
	 */
	public abstract void setSSBNGenerator(ISSBNGenerator ssbnGenerator);

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#openWarningDialog()
	 */
	public abstract void openWarningDialog();

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#closeWarningDialog()
	 */
	public abstract void closeWarningDialog();

	/**
	 * Compiles the bayesian network. If there was any problem during compilation, the error
	 * message will be shown as a <code>JOptionPane</code> .
	 * 
	 * @return true if the net was compiled without any problem, false if there was a problem
	 * @since
	 * @see JOptionPane
	 */
	public abstract boolean compileNetwork(ProbabilisticNetwork network);

	/**
	 * Initializes the junction tree's known facts
	 */
	public abstract void initialize();

	/**
	 * Propagates the bayesian network's evidences ( <code>TRP</code> ).
	 * 
	 * @since
	 */
	public abstract void propagate();

	/*--------------------------------------------------------------------------
	 * FIM DOS MÉTODOS CÓPIA
	 *-------------------------------------------------------------------------/
	
	
	
	
	
	
	
	
	/**
	 * @return false if don't have one ssbn pre-generated. True if the mode is change. 
	 */
	public abstract boolean turnToSSBNMode();

	public abstract MultiEntityBayesianNetwork getMultiEntityBayesianNetwork();

	public abstract void setMultiEntityBayesianNetwork(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork);

	public abstract MEBNEditionPane getMebnEditionPane();

	public abstract void setMebnEditionPane(MEBNEditionPane mebnEditionPane);

	public abstract ProbabilisticNetwork getSpecificSituationBayesianNetwork();

	public abstract SSBN getSSBN();

	public abstract void setSpecificSituationBayesianNetwork(
			ProbabilisticNetwork specificSituationBayesianNetwork);

	public abstract boolean isShowSSBNGraph();

	public abstract void setShowSSBNGraph(boolean showSSBNGraph);

	public abstract void setEditionMode();

	/**
	 * @return the toLogNodesAndProbabilities
	 */
	public abstract boolean isToLogNodesAndProbabilities();

	/**
	 * @param toLogNodesAndProbabilities the toLogNodesAndProbabilities to set
	 */
	public abstract void setToLogNodesAndProbabilities(
			boolean toLogNodesAndProbabilities);

	/**
	 * Insert a new resident node in the MultiEntityBayesianNetwork with 
	 * the standard label and descritpion.
	 *
	 * @param x The x position of the new node.
	 * @param y The y position of the new node.
	 * @deprecated use {@link #insertDomainResidentNode(double, double)}
	 */

	public abstract Node insertResidentNode(double x, double y)
			throws MFragDoesNotExistException;

	/**
	 * @deprecated use {@link #insertGenerativeInputNode(double, double)}
	 */
	public abstract Node insertInputNode(double x, double y)
			throws MFragDoesNotExistException;

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#getNetwork()
	 */
	public abstract Network getNetwork();

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#getGraph()
	 */
	public abstract Graph getGraph();

	/*
	 * (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#unselectAll()
	 */
	public abstract void unselectAll();

	/**
	 * This ID will be used by {@link PluginAwareFileExtensionIODelegator}
	 * in order to load the correct extension ID for MEBN's IO.
	 * @return the mebnIOExtensionPointID
	 */
	public abstract String getMebnIOExtensionPointID();

	/**
	 * This ID will be used by {@link PluginAwareFileExtensionIODelegator}
	 * in order to load the correct extension ID for MEBN's IO.
	 * @param mebnIOExtensionPointID the mebnIOExtensionPointID to set
	 */
	public abstract void setMebnIOExtensionPointID(String mebnIOExtensionPointID);

	/**
	 * This is the ID of MEBN module.
	 * It will be used by {@link PluginAwareFileExtensionIODelegator} in order
	 * to find the correct plugin context.
	 * @return the mebnModulePluginID
	 */
	public abstract String getMebnModulePluginID();

	/**
	 * It will be used by {@link PluginAwareFileExtensionIODelegator} in order
	 * to find the correct plugin context.
	 * @param mebnModulePluginID the mebnModulePluginID to set
	 */
	public abstract void setMebnModulePluginID(String mebnModulePluginID);

	/**
	 * Sets the current knowledge base and initializes (reset) it.
	 * @param kb
	 */
	public abstract void setKnowledgeBase(KnowledgeBase kb);

	/**
	 * Clears the content of the current knowledge base ({@link #getKnowledgeBase()}),
	 * and fills it using the currently edited MEBN.
	 */
	public abstract void resetKnowledgeBase();
	
	/**
	 * Obtains a frame to edit CPT of a resident node.
	 * @return an instance of a JFrame.
	 */
	public abstract JFrame getCPTEditionFrame(ResidentNode residentNode);
	
	/**
	 * Obtains an object from key.
	 * This is useful if you want to use this mediator to pass objects through methods
	 * (that is, a data transfer object).
	 * @param key : ID of the objects stored by this mediator
	 * @return object
	 * @see #setProperty(String, Object)
	 */
	public abstract Object getProperty(String key);
	
	/**
	 * Add a property setting a key.
	 * This is useful if you want to use this mediator to pass objects through methods
	 * (that is, a data transfer object).
	 * @param key : ID of the objects stored by this mediator
	 * @param obj : object to be stored
	 * @see #getProperty(String)
	 */
	public abstract void setProperty(String key, Object obj);


}