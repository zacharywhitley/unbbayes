package unbbayes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.io.log.ILogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;

/**
 * This class runs UnBBayes in text-mode
 * Currently, it works only for UnBBayes-MEBN by
 * loading a UBF, PLM, generating SSBN using Laskey algorithm
 * and printing into sysout the result.
 * 
 * It may not be useful now, but it might a good sample as API.
 * 
 * We are expecting to extend this class in order to execute
 * other modules as text mode as well.
 * 
 * @author Shou Matsumoto
 *
 */
public class TextModeRunner {
	
	private boolean isLogEnabled = false;
	
	public TextModeRunner() {
		super();
	}

	/**
	 * Startups knowledge base
	 * @param knowledgeBase
	 * @param mebn
	 * @return
	 */
	public KnowledgeBase createKnowledgeBase(KnowledgeBase knowledgeBase, MultiEntityBayesianNetwork mebn){
		// Must remove unwanted findings entered previously 
		knowledgeBase.clearKnowledgeBase();
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			knowledgeBase.createEntityDefinition(entity);
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				knowledgeBase.createRandomVariableDefinition(resident);
			}
		}
		
		for(ObjectEntityInstance instance: mebn.getObjectEntityContainer().getListEntityInstances()){
			 knowledgeBase.insertEntityInstance(instance); 
		}
		
		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(IResidentNode residentNode : mfrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
					knowledgeBase.insertRandomVariableFinding(finding); 
				}
			}
		}
		return knowledgeBase;
	}

	/**
	 * Prints nodes and its states.
	 * This is a fairly complete report of the generated ssbn.
	 * @param ssbn
	 */
	public void logNodesAndItsProbabilities(SSBN ssbn) {
		if (!this.isLogEnabled()) {
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
	 * @throws Exception 
	 */
	public ProbabilisticNetwork executeQueryLaskeyAlgorithm(List<Query> listQueries, KnowledgeBase knowledgeBase, 
			MultiEntityBayesianNetwork mebn) throws Exception {
		
		ProbabilisticNetwork probabilisticNetwork = null; 
		
		
	    knowledgeBase = createKnowledgeBase(knowledgeBase, mebn); 	
		
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
	    
		ISSBNGenerator ssbngenerator = new LaskeySSBNGenerator(parameters);
		ssbngenerator.setLogEnabled(this.isLogEnabled());
		
		SSBN ssbn = ssbngenerator.generateSSBN(listQueries, knowledgeBase); 
		
		
		probabilisticNetwork = ssbn.getProbabilisticNetwork();

		ProbabilisticNetwork specificSituationBayesianNetwork = probabilisticNetwork;

		ssbn.compileAndInitializeSSBN();
		
		
		// logging probabilities of the nodes
		this.logNodesAndItsProbabilities(ssbn);
		
		
		
		
		return specificSituationBayesianNetwork ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
	}
	
	/**
	 * Entry point for laskey algorithm
	 * @param mebn
	 * @param kb
	 * @param nameOfResidentNodeInQuery
	 * @param argValuesInOrder
	 * @return
	 * @throws Exception
	 */
	public ProbabilisticNetwork callLaskeyAlgorithm(MultiEntityBayesianNetwork mebn,
			KnowledgeBase kb,
			String nameOfResidentNodeInQuery, String...argValuesInOrder ) throws Exception {
		
		ResidentNode residentNode = mebn.getDomainResidentNode(nameOfResidentNodeInQuery);
		if (residentNode == null) {
			// node may not be in a list of domain resident node.
			for (MFrag mfrag : mebn.getMFragList()) {
				for (Node node : mfrag.getNodes()) {
					nameOfResidentNodeInQuery.equalsIgnoreCase(node.getName());
					residentNode = (ResidentNode)node;
					break;
				}
				if (residentNode != null) {
					break;
				}
			}
		}
		if (residentNode == null) {
			return null;
		}
		
		// creating ObjectEntityInstance - arguments
		ObjectEntityInstance[] arguments = new ObjectEntityInstance[argValuesInOrder.length];
		for (int i = 0; i < argValuesInOrder.length; i++) {
			ObjectEntity oe = mebn.getObjectEntityContainer().getObjectEntityByType(residentNode.getOrdinaryVariableList().get(i).getValueType());
			arguments[i] = new ObjectEntityInstance(argValuesInOrder[i],oe);
		}
		
        //ALG2
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		
		
		List<Argument> arglist = residentNode.getArgumentList();
		
		if (arglist.size() != arguments.length) {
			throw new InconsistentArgumentException();
		}
		
		for (int i = 1; i <= arguments.length; i++) {

			//TODO It has to get in the right order. For some reason in argList, 
			// sometimes the second argument comes first
			for (Argument argument : arglist) {
				if (argument.getArgNumber() == i) {
					OrdinaryVariable ov = argument.getOVariable(); 
					OVInstance ovInstance = OVInstance.getInstance(
							ov, 
							LiteralEntityInstance.getInstance(arguments[i-1].getName(), ov.getValueType()));
					ovInstanceList.add(ovInstance); 
					break;
				}
			}


		}
		
		Query query = new Query(residentNode, ovInstanceList); 
		
		List<Query> queryList = new ArrayList<Query>();
		queryList.add(query); 

		return this.executeQueryLaskeyAlgorithm(queryList,kb,mebn);
        
	}
	
	/**
	 * Starts populating the findings
	 * @param mebn
	 * @param knowledgeBase
	 * @return
	 */
	public KnowledgeBase fillFindings(MultiEntityBayesianNetwork mebn,
			KnowledgeBase knowledgeBase) {
		// fill findings
		for (ResidentNode resident : mebn.getDomainResidentNodes()) {
			try {
				 knowledgeBase.fillFindings(resident);
			 } catch (Exception e) {
				 e.printStackTrace();
				 continue;
			 }
		}
		return knowledgeBase;
	}

	/**
	 * Reads a UBF, a PLM and executes a query using laskey's algorithm
	 * @param args : <ubffile> <kbfile> <querynode> [<queryparams>]*
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("params: <ubffile> <kbfile> <querynode> [<queryparams>]*");
			return;
		}
		try {
			
			TextModeRunner textModeRunner = new TextModeRunner();
			
			// load ubf/owl
			
			UbfIO ubf = UbfIO.getInstance();
			MultiEntityBayesianNetwork mebn = ubf.loadMebn(new File(args[0]));

			
			// initialize kb
			
			KnowledgeBase knowledgeBase = PowerLoomKB.getNewInstanceKB();
			knowledgeBase = textModeRunner.createKnowledgeBase(knowledgeBase, mebn);
			
			// load kb
			knowledgeBase.loadModule(new File(args[1]), true);
			
			
			knowledgeBase = textModeRunner.fillFindings(mebn,knowledgeBase);
			
			
			// extract params for queries
			String[] queryParam = new String[args.length - 3];
			for (int i = 0; i < queryParam.length; i++) {
				queryParam[i] = args[i+3];
			}
			
			ProbabilisticNetwork net = textModeRunner.callLaskeyAlgorithm(mebn, knowledgeBase, 
					args[2], queryParam);
			
			// do something to net if you want to do so
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @return the isLogEnabled
	 */
	public boolean isLogEnabled() {
		return isLogEnabled;
	}

	/**
	 * @param isLogEnabled the isLogEnabled to set
	 */
	public void setLogEnabled(boolean isLogEnabled) {
		SSBNDebugInformationUtil.setEnabled(isLogEnabled);
		this.isLogEnabled = isLogEnabled;
	}

	

}
