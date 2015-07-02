

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.io.BaseIO;
import unbbayes.io.log.ILogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.exception.ReservedWordException;
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
import unbbayes.util.Debug;

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
	
	private boolean isLogEnabled = true;
	private ISSBNGenerator ssbngenerator;
	
	public TextModeRunner() {
		super();
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
	    
		ssbngenerator = new LaskeySSBNGenerator(parameters);
	}

	/**
	 * Startups knowledge base.
	 * This method basically imports data from mebn to knowledgeBase
	 * @param knowledgeBase : knowledge base to export data into
	 * @param mebn : MTheory containing data to export to knowledgeBase
	 * @return knowledgeBase
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
	
	
	public void printLogNodesAndItsProbabilities(SSBN ssbn) {
		
		ILogManager logManager = ssbn.getLogManager();
		ProbabilisticNetwork probabilisticNetwork = ssbn.getProbabilisticNetwork();
		if (probabilisticNetwork != null && probabilisticNetwork.getNodes() != null) {
			//logManager.appendSpecialTitle("Result Query: " + ssbn.getQueryList());
			System.out.println("Result Query: " + ssbn.getQueryList());
			for (Node node: probabilisticNetwork.getNodes()) {
				if (!(node instanceof ProbabilisticNode)) {
					continue;
				}
				ProbabilisticNode prob = (ProbabilisticNode)node;
				//logManager.appendSectionTitle(prob.toString());
				System.out.println(prob.toString());
				for (int i = 0 ; i < prob.getStatesSize() ; i++) {
					System.out.print(prob.getStateAt(i));
					System.out.print(" = ");
					System.out.print(Float.toString(prob.getMarginalAt(i)));
					System.out.print(", ");
				}
				System.out.println("");
			}
			System.out.println("");
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
		
		
	    knowledgeBase = createKnowledgeBase(knowledgeBase, mebn); 	
		
		
		getSSBNgenerator().setLogEnabled(this.isLogEnabled());
		
		SSBN ssbn = getSSBNgenerator().generateSSBN(listQueries, knowledgeBase); 
		
		// the following is done in getSSBNgenerator().generateSSBN(listQueries, knowledgeBase); 
//		ssbn.compileAndInitializeSSBN();
		
		
		// logging probabilities of the nodes
		//this.logNodesAndItsProbabilities(ssbn);
		this.printLogNodesAndItsProbabilities(ssbn);

		return (ProbabilisticNetwork) ssbn.getNetwork() ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
	}
	
	public ISSBNGenerator getSSBNgenerator() {
		return ssbngenerator;
	}

	public void setSSBNgenerator(ISSBNGenerator ssbngenerator) {
		this.ssbngenerator = ssbngenerator;
	}

	/**
	 * Entry point for laskey algorithm.
	 * @param mebn
	 * @param kb
	 * @param nameOfResidentNodeInQuery
	 * @param argValuesInOrder
	 * @return
	 * @throws Exception
	 * @deprecated use {@link #callLaskeyAlgorithm(MultiEntityBayesianNetwork, KnowledgeBase, Collection)} with a collection
	 * with only 1 entry.
	 * with a map with 1 key instead.
	 */
	public ProbabilisticNetwork callLaskeyAlgorithm(MultiEntityBayesianNetwork mebn,
			KnowledgeBase kb,
			String nameOfResidentNodeInQuery, String...argValuesInOrder ) throws Exception {
		
		// call the other method using a list with only 1 query entry
		return this.callLaskeyAlgorithm(mebn, kb, Collections.singletonList(new QueryNodeNameAndArguments(nameOfResidentNodeInQuery, argValuesInOrder)));
	}
		 
	
	/**
	 * Entry point for laskey algorithm (w/ multiple query support)
	 * @param mebn
	 * @param kb
	 * @param queryNodeNameAndParameters	: a collection consisting of tuples: name of the node and all arguments
	 * are lists of arguments.
	 * @return a bayesian network.
	 * @throws Exception
	 */
	public ProbabilisticNetwork callLaskeyAlgorithm(MultiEntityBayesianNetwork mebn,
			KnowledgeBase kb,	Collection<QueryNodeNameAndArguments> queryNodeNamesAndParameters) throws Exception {
		
		// initial assetions
		if (queryNodeNamesAndParameters == null || queryNodeNamesAndParameters.isEmpty()) {
			return null;
		}
		
		// this list will contain the queries
		List<Query> queryList = new ArrayList<Query>();
		
		// extract resident nodes from the names in the keys of queryNodeNameAndParameters
		for (QueryNodeNameAndArguments queryInfo : queryNodeNamesAndParameters) {
			ResidentNode residentNode = mebn.getDomainResidentNode(queryInfo.getNodeName());
			if (residentNode == null) {
				try {
					Debug.println(getClass(), queryInfo.getNodeName() + " is not in the list of domain resident nodes. Now searching contents of MFrags...");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				// node may not be in a list of domain resident node.
				for (MFrag mfrag : mebn.getMFragList()) {
					for (Node node : mfrag.getNodes()) {
						queryInfo.getNodeName().equalsIgnoreCase(node.getName());
						residentNode = (ResidentNode)node;
						break;
					}
					if (residentNode != null) {
						break;
					}
				}
			}
			if (residentNode == null) {
				// could not find the resident node.�@Just ignore
				try {
					Debug.println(getClass(), "Could not find resident node " + queryInfo.getNodeName());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			
			// creating ObjectEntityInstance (i.e. arguments)
			ObjectEntityInstance[] arguments = new ObjectEntityInstance[queryInfo.getArguments().length];
			for (int i = 0; i < queryInfo.getArguments().length; i++) {
				ObjectEntity oe = mebn.getObjectEntityContainer().getObjectEntityByType(residentNode.getOrdinaryVariableList().get(i).getValueType());
				arguments[i] = new ObjectEntityInstance(queryInfo.getArguments()[i],oe);
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
			
			queryList.add(query); 
		}
		
		
		if (queryList.isEmpty()) {
			// no query 
			return null;
		}
		
		return this.executeQueryLaskeyAlgorithm(queryList,kb,mebn);
        
	}
	
	/**
	 * Starts populating the findings.
	 * This is the opposite of {@link #createKnowledgeBase(KnowledgeBase, MultiEntityBayesianNetwork)}.
	 * That is, from knowledgeBase, populate some findings in mebn.
	 * @param mebn : resident nodes in this object will be populated with findings in knowledgeBase
	 * @param knowledgeBase : findings will be read from here.
	 * @return knowledgeBase
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
	 * Clears content of knowledgeBase and findings in mebn.
	 * @param mebn : resident nodes whose findings are going to be cleared
	 * @param knowledgeBase : knowledgeBase to be cleared
	 * @return knowledgeBase
	 */
	public KnowledgeBase clearKnowledgeBase(MultiEntityBayesianNetwork mebn,
			KnowledgeBase knowledgeBase) {
		knowledgeBase.clearKnowledgeBase();
		if (mebn != null) {
			for (MFrag mfrag : mebn.getMFragList()) {
				if (mfrag != null) {
					for (ResidentNode resident : mfrag.getResidentNodeList()) {
						resident.cleanRandomVariableFindingList();
					}
				}
			}
		}
		return knowledgeBase;
	}
	
	/**
	 * It saves the content of kb into the file.
	 * This method basically clears the knowledge base (in order to remove any old knowledge),
	 * refills the knowledge base using information in mebn, and then saves it to file.
	 * @param file : file to store knowledge base
	 * @param kb : knowledge base to save
	 * @param mebn : the MTheory whose the knowledge base knows about.
	 */
	public void saveKnowledgeBase(File file, KnowledgeBase kb, MultiEntityBayesianNetwork mebn) {
		
		// reset KB in order to clear any garbage (e.g. old, logically deleted data)
		kb.clearKnowledgeBase();
		
		// refill knowledge base with entities 
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			kb.createEntityDefinition(entity);
		}

		// refill knowledge base with resident node definitions
		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				kb.createRandomVariableDefinition(resident);
			}
		}
		
		// refill knowledge base with instances of entities
		for(ObjectEntityInstance instance: mebn.getObjectEntityContainer().getListEntityInstances()){
			kb.insertEntityInstance(instance); 
		}
		
		// refill knowledge base with findings about resident nodes
		// TODO use a map instead of cubic search
		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(IResidentNode residentNode : mfrag.getResidentNodeList()){
				for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
					kb.insertRandomVariableFinding(finding); 
				}
			}
		}
		
		// finally, save the kb
		kb.saveFindings(mebn, file);
	}
	
	/**
	 * Saves a MTheory into a file by using an I/O class.
	 * @param file : the file to save mebn
	 * @param io : the I/O class to be used in order to save mebn
	 * @param mebn : MTheory to be saved.
	 * @throws IOMebnException : if any inconsistency is found in mebn or io which 
	 * impedes storing mebn.
	 * @throws IOException : generic I/O exception. This is usually not related to
	 * MultiEntityBayesianNetwork logic or format consistency.
	 */
	public void saveMEBN(File file, BaseIO io, MultiEntityBayesianNetwork mebn) throws IOMebnException, IOException {
		io.save(file, mebn);
	}
	
	/**
	 * This is just a very simple representation of a query (name of a resident node and
	 * instances of its arguments) using only Strings.
	 * @author Shou Matsumoto
	 *
	 */
	public class QueryNodeNameAndArguments {
		private final String nodeName;
		private final String[] arguments;
		
		/** Constructor initializing fields
		 * @param nodeName	: name of the node to query
		 * @param arguments	: arguments of the node to query
		 */
		public QueryNodeNameAndArguments(String nodeName, String... arguments) {
			super();
			this.nodeName = nodeName;
			this.arguments = arguments;
		}

		/**
		 * @return the nodeName
		 */
		public String getNodeName() {
			return nodeName;
		}

		/**
		 * @return the arguments
		 */
		public String[] getArguments() {
			return arguments;
		}
	}

	public void addBuiltInRVs(MultiEntityBayesianNetwork mebn){
		BuiltInRV builtin = new BuiltInRVAnd();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVEqualTo();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVExists();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVForAll();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVIff();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVImplies();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVNot();
		mebn.addBuiltInRVList(builtin);
		builtin = new BuiltInRVOr();
		mebn.addBuiltInRVList(builtin);
		
	}
	
	public MultiEntityBayesianNetwork generateMEBN(){
		MEBNController mebnc;
		MultiEntityBayesianNetwork mebn;
		OrdinaryVariable ordv;
		ResidentNode rn, an;
		Argument arg;
		ObjectEntity oe;
		MFrag newfrag;
		
		mebn = new MultiEntityBayesianNetwork("MEBN");
		//mebnc = new MEBNController(mebn, null);
		
		//mebnc.insertDomainMFrag();
		// create the Mfrag
		newfrag = new MFrag("SmallMFrag", mebn);
		mebn.addDomainMFrag(newfrag);
		
		addBuiltInRVs(mebn);
		
		//mebn.getObjectEntityContainer().addEntityInstance(entityInstance);
		try {
			
			//create the object entity
			oe= mebn.getObjectEntityContainer().createObjectEntity("Observation");
			
			//mebn.getTypeContainer().createType("Observation");
			//create the new ordinary variable
			newfrag.plusOrdinaryVariableNum();
			//OrdinaryVariable ov = new OrdinaryVariable("Obs", mebn.getTypeContainer().getType("Observation"), newfrag);
			OrdinaryVariable ov = new OrdinaryVariable("O1", TypeContainer.getDefaultType(), newfrag);
			Type type = mebn.getTypeContainer().getType("Observation_label");
			ov.setValueType(type);
			ov.updateLabel();
			ov.setName("O1");
			ov.setDescription(ov.getName());
			newfrag.addOrdinaryVariable(ov); // by shou
			mebn.plusDomainResidentNodeNum();
			// create the resident node
			rn = new ResidentNode("InputObs", newfrag);
			mebn.getNamesUsed().add("InputObs");
			

			newfrag.addResidentNode(rn);
			
			//Create the obs for the new rn
			//arg = new Argument("Obs", rn);
			//rn.addArgument(arg);
			rn.addArgument(ov, true);
			
			mebn.plusDomainResidentNodeNum();
			// add another node
			// create the resident node
			an = new ResidentNode("Assessment", newfrag);
			mebn.getNamesUsed().add("Assessment");
			newfrag.addResidentNode(an);
			//an.addArgument(ov, true);
			// add possible values to the rn
			CategoricalStateEntity value = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity("Good");
			mebn.getNamesUsed().add("Good"); 
			StateLink link = rn.addPossibleValueLink(value);
			rn.setTypeOfStates(IResidentNode.CATEGORY_RV_STATES);
			an.setTypeOfStates(IResidentNode.CATEGORY_RV_STATES);
			link = an.addPossibleValueLink(value);
			value.addNodeToListIsPossibleValueOf(rn);
			value.addNodeToListIsPossibleValueOf(an);
			
			//value = mebn.getCategoricalStatesEntityContainer().getCategoricalState("Good");
			
			value = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity("Bad");
			mebn.getNamesUsed().add("Bad"); 
			link = rn.addPossibleValueLink(value);
			link = an.addPossibleValueLink(value);
			value.addNodeToListIsPossibleValueOf(rn);
			value.addNodeToListIsPossibleValueOf(an);
			
			String tf = "if any O1 have ( InputObs = Good ) [   Good = 0.85,   Bad = .15] else[   Good = 0.05 ,    Bad = .95]";
			
			an.setTableFunction(tf);
			
			
			Edge e = new Edge(rn,an);
			newfrag.addEdge(e);
			
			//rn.addChild(an);
			//an.addParent(rn);
			
			
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MEBNConstructionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (CycleFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return mebn;
	}
	
	
	/**
	 * Reads a UBF, a PLM and executes a query using laskey's algorithm
	 * @param args : <ubffile> <kbfile> <querynode> [<queryparams>]*
	 */
	public static void main(String[] args) {
		
		String kbFileName = "./target/test-classes/2examples.plm";
		String nodeName = "Assessment";
		unbbayes.util.Debug.setDebug(true);
//		if (args.length < 3) {
//			System.out.println("params: <ubffile> <kbfile> <querynode> [<queryparams>]*");
//			return;
//		}
		try {
			
			TextModeRunner textModeRunner = new TextModeRunner();
		
			MultiEntityBayesianNetwork mebn = textModeRunner.generateMEBN();
			//int x = 1/0;
			// load ubf/owl
			
			UbfIO ubf = UbfIO.getInstance();
			//MultiEntityBayesianNetwork mebn = ubf.loadMebn(new File(args[0]));
			//mebn = textModeRunner.generateMEBN();
			
			// initialize kb
			ubf.save(new File("mitretest.ubf"), mebn);
			KnowledgeBase knowledgeBase = PowerLoomKB.getNewInstanceKB();
			knowledgeBase = textModeRunner.createKnowledgeBase(knowledgeBase, mebn);
			
			// load kb
//			knowledgeBase.loadModule(new File(args[1]), true);
			knowledgeBase.loadModule(new File(kbFileName), true);
			
			
			knowledgeBase = textModeRunner.fillFindings(mebn,knowledgeBase);
			
			
			// extract params for queries
			String[] queryParam = new String[0];
//			String[] queryParam = new String[args.length - 3];
//			for (int i = 0; i < queryParam.length; i++) {
//				queryParam[i] = args[i+3];
//			}
			
			ProbabilisticNetwork net = textModeRunner.callLaskeyAlgorithm(mebn, knowledgeBase, 
					nodeName, queryParam);
			
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
