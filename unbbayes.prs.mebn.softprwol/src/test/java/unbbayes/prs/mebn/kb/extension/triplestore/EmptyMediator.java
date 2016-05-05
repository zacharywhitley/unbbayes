package unbbayes.prs.mebn.kb.extension.triplestore;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextArea;

import unbbayes.controller.SENController;
import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.exception.InvalidOperationException;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.mebn.MEBNEditionPane;
import unbbayes.gui.mebn.cpt.CPTFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.exception.UBIOException;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
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
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.prs.mebn.exception.ReservedWordException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.mebn.extension.manager.MEBNPluginNodeManager;

public class EmptyMediator implements IMEBNMediator {

	public EmptyMediator(){
		
	}
	
	public IInferenceAlgorithm getInferenceAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm) {
		// TODO Auto-generated method stub

	}

	public SENController getSENController() {
		// TODO Auto-generated method stub
		return null;
	}

	public SingleEntityNetwork getSingleEntityNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeEvidence(Node node) {
		// TODO Auto-generated method stub

	}

	public void createTable(Node node) {
		// TODO Auto-generated method stub

	}

	public void createContinuousDistribution(ContinuousNode node) {
		// TODO Auto-generated method stub

	}

	public void createDiscreteTable(Node node) {
		// TODO Auto-generated method stub

	}

	public JTable makeTable(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean compileNetwork() {
		// TODO Auto-generated method stub
		return false;
	}

	public void evaluateNetwork() {
		// TODO Auto-generated method stub

	}

	public Node insertContinuousNode(double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}

	public Node insertProbabilisticNode(double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}

	public Node insertDecisionNode(double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}

	public Node insertUtilityNode(double x, double y) {
		// TODO Auto-generated method stub
		return null;
	}

	public void showExplanationProperties(ProbabilisticNode node) {
		// TODO Auto-generated method stub

	}

	public void insertState(Node node) {
		// TODO Auto-generated method stub

	}

	public void removeState(Node node) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public NetworkWindow getScreen() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveNetImage() {
		// TODO Auto-generated method stub

	}

	public void saveTableImage() {
		// TODO Auto-generated method stub

	}

	public String getLog() {
		// TODO Auto-generated method stub
		return null;
	}

	public JDialog showLog() {
		// TODO Auto-generated method stub
		return null;
	}

	public void previewPrintLog(JTextArea texto, JDialog dialog) {
		// TODO Auto-generated method stub

	}

	public void previewPrintTable() {
		// TODO Auto-generated method stub

	}

	public void previewPrintNet(JComponent rede, Rectangle retangulo) {
		// TODO Auto-generated method stub

	}

	public void printNet(JComponent network, Rectangle rectangle) {
		// TODO Auto-generated method stub

	}

	public void printTable() {
		// TODO Auto-generated method stub

	}

	public Rectangle calculateNetRectangle() {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseIO getBaseIO() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBaseIO(BaseIO baseIO) {
		// TODO Auto-generated method stub

	}

	public void setScreen(NetworkWindow screen) {
		// TODO Auto-generated method stub

	}

	public void openPanel(ResidentNode node) {
		// TODO Auto-generated method stub

	}

	public void setResetButtonActive() {
		// TODO Auto-generated method stub

	}

	public IResidentNode getResidentNodeActive() {
		// TODO Auto-generated method stub
		return null;
	}

	public InputNode getInputNodeActive() {
		// TODO Auto-generated method stub
		return null;
	}

	public ContextNode getContextNodeActive() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNodeActive() {
		// TODO Auto-generated method stub
		return null;
	}

	public void enableMTheoryEdition() {
		// TODO Auto-generated method stub

	}

	public void renameMTheory(String name) throws DuplicatedNameException,
			ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void setDescriptionTextForSelectedObject(String text) {
		// TODO Auto-generated method stub

	}

	public boolean insertEdge(Edge edge) throws MEBNConstructionException,
			CycleFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	public void insertDomainMFrag() {
		// TODO Auto-generated method stub

	}

	public void removeDomainMFrag(MFrag domainMFrag) {
		// TODO Auto-generated method stub

	}

	public void setCurrentMFrag(MFrag mFrag) {
		// TODO Auto-generated method stub

	}

	public void renameMFrag(MFrag mFrag, String name)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public MFrag getCurrentMFrag() {
		// TODO Auto-generated method stub
		return null;
	}

	public IResidentNode insertDomainResidentNode(double x, double y)
			throws MFragDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	public void renameDomainResidentNode(ResidentNode resident, String newName)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public StateLink addPossibleValue(ResidentNode resident, String nameValue)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub
		return null;
	}

	public StateLink addPossibleValue(ResidentNode resident,
			CategoricalStateEntity state) {
		// TODO Auto-generated method stub
		return null;
	}

	public StateLink addObjectEntityAsPossibleValue(ResidentNode resident,
			ObjectEntity state) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean existPossibleValue(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setGloballyExclusiveProperty(StateLink state, boolean value) {
		// TODO Auto-generated method stub

	}

	public void addBooleanAsPossibleValue(IResidentNode resident) {
		// TODO Auto-generated method stub

	}

	public void removePossibleValue(IResidentNode resident, String nameValue) {
		// TODO Auto-generated method stub

	}

	public void removeAllPossibleValues(IResidentNode resident) {
		// TODO Auto-generated method stub

	}

	public boolean existsPossibleValue(IResidentNode resident, String nameValue) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEnableTableEditionView() {
		// TODO Auto-generated method stub

	}

	public void setUnableTableEditionView() {
		// TODO Auto-generated method stub

	}

	public InputNode insertGenerativeInputNode(double x, double y)
			throws MFragDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setInputInstanceOf(InputNode input, ResidentNode resident)
			throws CycleFoundException, OVDontIsOfTypeExpected,
			ArgumentNodeAlreadySetException {
		// TODO Auto-generated method stub

	}

	public void updateArgumentsOfObject(Object node) {
		// TODO Auto-generated method stub

	}

	public void updateInputInstanceOf(InputNode input) {
		// TODO Auto-generated method stub

	}

	public ContextNode insertContextNode(double x, double y)
			throws MFragDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setActionGraphNone() {
		// TODO Auto-generated method stub

	}

	public void setActionGraphCreateEdge() {
		// TODO Auto-generated method stub

	}

	public void setActionGraphCreateContextNode() {
		// TODO Auto-generated method stub

	}

	public void setActionGraphCreateInputNode() {
		// TODO Auto-generated method stub

	}

	public void setActionGraphCreateResidentNode() {
		// TODO Auto-generated method stub

	}

	public void setActionGraphCreateOrdinaryVariableNode() {
		// TODO Auto-generated method stub

	}

	public void deleteSelectedItem() {
		// TODO Auto-generated method stub

	}

	public void deleteSelected(Object selected) {
		// TODO Auto-generated method stub

	}

	public void selectNode(Node node) {
		// TODO Auto-generated method stub

	}

	public Node getSelectedNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unselectNodes() {
		// TODO Auto-generated method stub

	}

	public void updateFormulaActiveContextNode() {
		// TODO Auto-generated method stub

	}

	public OrdinaryVariable insertOrdinaryVariable(double x, double y)
			throws MFragDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	public void renameOrdinaryVariable(OrdinaryVariable ov, String name)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void setOrdinaryVariableType(OrdinaryVariable ov, Type type) {
		// TODO Auto-generated method stub

	}

	public OrdinaryVariable addNewOrdinaryVariableInMFrag() {
		// TODO Auto-generated method stub
		return null;
	}

	public OrdinaryVariable addNewOrdinaryVariableInResident()
			throws OVariableAlreadyExistsInArgumentList,
			ArgumentNodeAlreadySetException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeOrdinaryVariableOfMFrag(OrdinaryVariable ov) {
		// TODO Auto-generated method stub

	}

	public void addOrdinaryVariableInResident(OrdinaryVariable ordinaryVariable)
			throws ArgumentNodeAlreadySetException,
			OVariableAlreadyExistsInArgumentList {
		// TODO Auto-generated method stub

	}

	public void removeOrdinaryVariableInResident(
			OrdinaryVariable ordinaryVariable) {
		// TODO Auto-generated method stub

	}

	public void setOVariableSelectedInResidentTree(
			OrdinaryVariable oVariableSelected) {
		// TODO Auto-generated method stub

	}

	public void setOVariableSelectedInMFragTree(
			OrdinaryVariable oVariableSelected) {
		// TODO Auto-generated method stub

	}

	public void renameOVariableOfResidentTree(String name)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void renameOVariableOfMFragTree(String name)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void renameOVariableInArgumentEditionPane(String name)
			throws DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void selectOVariableInEdit(OrdinaryVariable ov) {
		// TODO Auto-generated method stub

	}

	public ObjectEntity createObjectEntity() throws TypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectEntity createObjectEntity(ObjectEntity parentObjectEntity)
			throws TypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public void renameObjectEntity(ObjectEntity entity, String name)
			throws TypeAlreadyExistsException, DuplicatedNameException,
			ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void removeObjectEntity(ObjectEntity entity) throws Exception {
		// TODO Auto-generated method stub

	}

	public void setIsOrdereableObjectEntityProperty(ObjectEntity entity,
			boolean isOrdereable) throws ObjectEntityHasInstancesException {
		// TODO Auto-generated method stub

	}

	public void createEntityIntance(ObjectEntity entity, String nameInstance)
			throws EntityInstanceAlreadyExistsException,
			InvalidOperationException, DuplicatedNameException,
			ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void createEntityIntanceOrdereable(ObjectEntity entity,
			String nameInstance, ObjectEntityInstanceOrdereable previous)
			throws EntityInstanceAlreadyExistsException,
			InvalidOperationException, DuplicatedNameException,
			ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void renameEntityIntance(ObjectEntityInstance entity, String newName)
			throws EntityInstanceAlreadyExistsException,
			DuplicatedNameException, ReservedWordException {
		// TODO Auto-generated method stub

	}

	public void removeEntityInstance(ObjectEntityInstance entity) {
		// TODO Auto-generated method stub

	}

	public void removeEntityInstanceOrdereable(
			ObjectEntityInstanceOrdereable entity) {
		// TODO Auto-generated method stub

	}

	public void upEntityInstance(ObjectEntityInstanceOrdereable entity) {
		// TODO Auto-generated method stub

	}

	public void downEntityInstance(ObjectEntityInstanceOrdereable entity) {
		// TODO Auto-generated method stub

	}

	public void createRandomVariableFinding(ResidentNode residentNode,
			ObjectEntityInstance[] arguments, Entity state) {
		// TODO Auto-generated method stub

	}

	public void saveCPT(IResidentNode residentNode, String cpt) {
		// TODO Auto-generated method stub

	}

	public void openCPTDialog(ResidentNode residentNode) {
		// TODO Auto-generated method stub

	}

	public void closeCPTDialog(ResidentNode residentNode) {
		// TODO Auto-generated method stub

	}

	public CPTFrame getCPTDialog(ResidentNode residentNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public KnowledgeBase getKnowledgeBase() {
		// TODO Auto-generated method stub
		return null;
	}

	public void clearFindingsIntoGUI() {
		// TODO Auto-generated method stub

	}

	public void clearKnowledgeBase() {
		// TODO Auto-generated method stub

	}

	public void saveGenerativeMTheory(File file) {
		// TODO Auto-generated method stub

	}

	public void saveFindingsFile(File file) {
		// TODO Auto-generated method stub

	}

	public void loadFindingsFile(File file) throws UBIOException, MEBNException {
		// TODO Auto-generated method stub

	}

	public Network executeQuery(List<Query> listQueries)
			throws InconsistentArgumentException, SSBNNodeGeneralException,
			ImplementationRestrictionException, MEBNException,
			OVInstanceFaultException, InvalidParentException {
		// TODO Auto-generated method stub
		return null;
	}

	public ProbabilisticNetwork executeQueryLaskeyAlgorithm(
			List<Query> listQueries) throws InconsistentArgumentException,
			SSBNNodeGeneralException, ImplementationRestrictionException,
			MEBNException, OVInstanceFaultException, InvalidParentException {
		// TODO Auto-generated method stub
		return null;
	}

	public ISSBNGenerator getSSBNGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSSBNGenerator(ISSBNGenerator ssbnGenerator) {
		// TODO Auto-generated method stub

	}

	public void openWarningDialog() {
		// TODO Auto-generated method stub

	}

	public void closeWarningDialog() {
		// TODO Auto-generated method stub

	}

	public boolean compileNetwork(ProbabilisticNetwork network) {
		// TODO Auto-generated method stub
		return false;
	}

	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void propagate() {
		// TODO Auto-generated method stub

	}

	public boolean turnToSSBNMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean turnToMTheoryMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMultiEntityBayesianNetwork(
			MultiEntityBayesianNetwork multiEntityBayesianNetwork) {
		// TODO Auto-generated method stub

	}

	public MEBNEditionPane getMebnEditionPane() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMebnEditionPane(MEBNEditionPane mebnEditionPane) {
		// TODO Auto-generated method stub

	}

	public ProbabilisticNetwork getSpecificSituationBayesianNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	public SSBN getSSBN() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSpecificSituationBayesianNetwork(
			ProbabilisticNetwork specificSituationBayesianNetwork) {
		// TODO Auto-generated method stub

	}

	public boolean isShowSSBNGraph() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setShowSSBNGraph(boolean showSSBNGraph) {
		// TODO Auto-generated method stub

	}

	public void setEditionMode() {
		// TODO Auto-generated method stub

	}

	public boolean isToLogNodesAndProbabilities() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setToLogNodesAndProbabilities(boolean toLogNodesAndProbabilities) {
		// TODO Auto-generated method stub

	}

	public Node insertResidentNode(double x, double y)
			throws MFragDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node insertInputNode(double x, double y)
			throws MFragDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	public Network getNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	public Graph getGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unselectAll() {
		// TODO Auto-generated method stub

	}

	public String getMebnIOExtensionPointID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMebnIOExtensionPointID(String mebnIOExtensionPointID) {
		// TODO Auto-generated method stub

	}

	public String getMebnModulePluginID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMebnModulePluginID(String mebnModulePluginID) {
		// TODO Auto-generated method stub

	}

	public void setKnowledgeBase(KnowledgeBase kb) {
		// TODO Auto-generated method stub

	}

	public void resetKnowledgeBase() {
		// TODO Auto-generated method stub

	}

	public JFrame getCPTEditionFrame(ResidentNode residentNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProperty(String key, Object obj) {
		// TODO Auto-generated method stub

	}

	public MEBNPluginNodeManager getPluginNodeManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPluginNodeManager(MEBNPluginNodeManager pluginNodeManager) {
		// TODO Auto-generated method stub

	}

	public IResidentNode getActiveResidentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setActiveResidentNode(IResidentNode activeNode) {
		// TODO Auto-generated method stub

	}

	public boolean isToTurnToSSBNMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setToTurnToSSBNMode(boolean isToTurnToSSBNMode) {
		// TODO Auto-generated method stub

	}

}
