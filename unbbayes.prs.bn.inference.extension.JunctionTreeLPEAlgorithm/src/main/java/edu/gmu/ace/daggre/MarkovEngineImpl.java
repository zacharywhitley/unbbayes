package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link MarkovEngineInterface}.
 * This class is basically a wrapper for the functionalities offered by
 * {@link AssetAwareInferenceAlgorithm}. 
 * It adds history feature and transactional behaviors to
 * {@link AssetAwareInferenceAlgorithm}.
 * @author Shou Matsumoto
 * @version July 01, 2012
 */
public class MarkovEngineImpl implements MarkovEngineInterface {
	
	private float probabilityErrorMargin = 0.0001f;

	private Map<Long, List<NetworkAction>> networkActionsMap;
	
	private HashMap<Long, List<NetworkAction>> networkActionsIndexedByQuestions;	// HashMap allows null key
	
	private Long transactionCounter;

	private Map<Long, AssetAwareInferenceAlgorithm> userToAssetAwareAlgorithmMap;

	private boolean isToAddCashProportionally = false;

	private float defaultInitialQTableValue = 1;

	private float currentLogBase = 10;

	private double currentCurrencyConstant = 10;

	private ProbabilisticNetwork probabilisticNetwork;

	private AssetAwareInferenceAlgorithm defaultInferenceAlgorithm;

	private IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor;

//	private AssetAwareInferenceAlgorithm inferenceAlgorithm;
	

	/**
	 * Default constructor is protected to allow inheritance.
	 * Use {@link #getInstance()} to actually instantiate objects of this class.
	 */
	protected MarkovEngineImpl() {
		this.initialize();
	}
	
	/**
	 * Constructor method design pattern.
	 * If any implementation of {@link MarkovEngineInterface} shall substitute
	 * this class, then changing this method will guarantee that
	 * all callers will be automatically instantiating the new implementation instead of
	 * {@link MarkovEngineImpl} without any further change in source code.
	 * Callers may implement factory design pattern for a better
	 * use of the constructor method design pattern.
	 * @return new instance of some class implementing {@link MarkovEngineInterface}
	 */
	public static MarkovEngineInterface getInstance() {
		return new MarkovEngineImpl();
	}
	
	/**
	 * Translates asset Q values to scores using logarithm functions.
	 * <br/>
	 * Score = b*log(assetQ), with log being the logarithm of base {@link #getCurrentLogBase()},
	 * and b = {@link #getCurrentCurrencyConstant()}.
	 * @param assetQ
	 * @return the score value
	 * @see #getQValuesFromScore(float)
	 */
	public float getScoreFromQValues(float assetQ) {
		return (float) ((Math.log(assetQ)/Math.log(getCurrentLogBase())) * getCurrentCurrencyConstant());
	}
	
	/**
	 * Translates scores to asset Q values using logarithm functions.
	 * <br/>
	 * Score = b*log(assetQ), with log being the logarithm of base {@link #getCurrentLogBase()},
	 * and b = {@link #getCurrentCurrencyConstant()}.
	 * @param score
	 * @return the asset q value
	 * @see #getScoreFromQValues(float)
	 */
	public float getQValuesFromScore(float score) {
		/*
		 * Score = b*log(assetQ)
		 * -> Score / b = log(assetQ)
		 * -> power(baseOfLog, (Score / b)) = power(baseOfLog, log(assetQ))
		 * -> power(baseOfLog, (Score / b)) = assetQ
		 */
		return (float) Math.pow(getCurrentLogBase(), (score/getCurrentCurrencyConstant()));
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#initialize()
	 */
	public synchronized boolean initialize() {
		// reset counter of transactions
		setTransactionCounter(0);
		
		// prepare map storing network actions
		setNetworkActionsMap(new ConcurrentHashMap<Long, List<NetworkAction>>());	// concurrent hash map is known to be thread safe yet fast.
		setNetworkActionsIndexedByQuestions(new HashMap<Long, List<NetworkAction>>());	// HashMap allows null key.
		
		
		setProbabilisticNetwork(new ProbabilisticNetwork("DAGGRE"));
		// disable log
		getProbabilisticNetwork().setCreateLog(false);
		
		// prepare inference algorithm for the BN
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
		JeffreyRuleLikelihoodExtractor jeffreyRuleLikelihoodExtractor = (JeffreyRuleLikelihoodExtractor) JeffreyRuleLikelihoodExtractor.newInstance();
		junctionTreeAlgorithm.setLikelihoodExtractor(jeffreyRuleLikelihoodExtractor);
		// prepare default inference algorithm for asset network
		setDefaultInferenceAlgorithm((AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm));
		// usually, users seem to start with 0 assets (assets are logarithmic, so 0 assets == 1 q table), but let's use the value of getDefaultInitialQTableValue
		getDefaultInferenceAlgorithm().setDefaultInitialAssetQuantity(getDefaultInitialQTableValue());
		getDefaultInferenceAlgorithm().setToPropagateForGlobalConsistency(false);	// force algorithm to do min-propagation of assets only when prompted
		
		// several methods in this class reuse the same conditional probability extractor. Extract it here
		setConditionalProbabilityExtractor(jeffreyRuleLikelihoodExtractor.getConditionalProbabilityExtractor());
		
		// initialize the map managing users (and the algorithms responsible for changing values of users asset networks)
		setUserToAssetAwareAlgorithmMap(new ConcurrentHashMap<Long, AssetAwareInferenceAlgorithm>()); // concurrent hash map is known to be thread safe yet fast.
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#startNetworkActions()
	 */
	public long startNetworkActions() {
		long ret = Long.MIN_VALUE;
		synchronized (transactionCounter) {
			ret = ++transactionCounter;
			// NOTE: getNetworkActionsMap is supposedly an instance of concurrent map, so we do not need to synchronize it
			getNetworkActionsMap().put(ret, new ArrayList<NetworkAction>());
		}
		System.out.println("[startNetworkActions]" + ret);
		return ret;
//		getNetworkActionsMap().put(++transactionCounter, new ArrayList<NetworkAction>());
//		return transactionCounter;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#commitNetworkActions(long)
	 */
	public synchronized boolean commitNetworkActions(long transactionKey)
			throws IllegalArgumentException, ZeroAssetsException {
		
		// TODO revert actions on error.
		
		// initial assertion : make sure transactionKey is valid
		List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		/*
		 * Reorder actions so that changes in BN structure comes first,
		 * insert a "rebuild structure" action after the last BN structure change actions in a row,
		 * and then execute the actions in sequence.
		 * 
		 * E.g. assuming that <add node> and <add edge> changes the BN structures (and the others doesn't), then:
		 * 
		 * [<add node 1>, <add node 2>, <add edge 1>, <add trade 1>, <add edge 2>, <get cash 1>]
		 * 
		 * will be converted to:
		 * 
		 * [<add node 1>, <add node 2>, <add edge 1>, <add edge 2>, <rebuild 1> , <add trade 1>, <get cash 1>]
		 * 
		 * The <rebuild 1> action shall read the history and redo all trades (only trades which is not a BN structure change).
		 */
		synchronized (actions) {	// make sure actions are not changed by other threads
			List<NetworkAction> netChangeActions = new ArrayList<NetworkAction>();	// will store actions which changes network structures
			List<NetworkAction> otherActions = new ArrayList<NetworkAction>();	// will store actions which does not change network structure
			// collect all network change actions and other actions, respecting their original order
			for (NetworkAction action : actions) {
				if (action.isStructureChangeAction()) {
					netChangeActions.add(action);
				} else {
					otherActions.add(action);
				}
			}
			
			// change the content of actions. Since we are using the same list object, this should also change the values stored in getNetworkActionsMap.
			if (!netChangeActions.isEmpty()) {
				// only make changes (reorder actions and recompile network) if there is any action changing the structure of network.
				actions.clear();	// reset actions first
				actions.addAll(netChangeActions);	// netChangeActions comes first
				actions.add(new RebuildNetworkAction(netChangeActions.get(0).getTransactionKey(), new Date()));	// <rebuild action> is inserted between netChangeActions and otherActions
				actions.addAll(otherActions);	// otherActions comes later
			}
			
			// then, execute all actions
			for (NetworkAction action : actions) {
				action.execute();
			}
		}	// release lock to actions
		
		return true;
	}
	
	/**
	 * Represents a network action for rebuilding BN, all asset nets, and
	 * then redoing all trades.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#commitNetworkActions(long)
	 */
	public class RebuildNetworkAction implements NetworkAction {
		private final Date whenCreated;
		private final long transactionKey;
		/** Default constructor */
		public RebuildNetworkAction(long transactionKey, Date whenCreated) {
			this.transactionKey = transactionKey;
			this.whenCreated = whenCreated;
		}
		/** Rebuild the BN */
		public void execute() {
			// BN to rebuild
			ProbabilisticNetwork net = getProbabilisticNetwork();
			// make sure no one is using the probabilistic network yet.
			synchronized (getDefaultInferenceAlgorithm()) {
				synchronized (net) {
					if (net.getNodeCount() > 0) {
						getDefaultInferenceAlgorithm().run();
					}
				}
			}
			// TODO rebuild all user asset nets
			// TODO redo all trades using the history
			// Note: if we are rebooting the system, the history is supposedly empty
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Cannot revert a network rebuild action.");
		}
		public Date getWhenCreated() { return whenCreated; }
		/** This action reboots the network, but does not change the structure by itself */
		public boolean isStructureChangeAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return null; }
		public List<Float> getPercent() { return null; }
		public String getTradeId() { return null; }
		public Long getQuestionId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestion(long, java.util.Date, long, int, java.util.List)
	 */
	public synchronized boolean addQuestion(long transactionKey, Date occurredWhen,
			long questionId, int numberStates, List<Float> initProbs)
			throws IllegalArgumentException {
		
		// initial assertions
		if (numberStates <= 0) {
			// invalid quantity of states
			throw new IllegalArgumentException("Attempted to add a question with " + numberStates + " states.");
		}
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		synchronized (getProbabilisticNetwork()) {
			if (getProbabilisticNetwork().getNode(Long.toString(questionId)) != null) {
				// duplicate question
				throw new IllegalArgumentException("Question ID " + questionId + " is already present.");
			}
		}
		if (initProbs != null && !initProbs.isEmpty()) {
			float sum = 0;
			for (Float prob : initProbs) {
				if (prob < 0 || prob > 1) {
					throw new IllegalArgumentException("Invalid probability declaration found: " + prob);
				}
				sum += prob;
			}
			// check if sum of initProbs is 1 (with error margin)
			if (!(((1 - getProbabilityErrorMargin()) < sum) && (sum < (1 + getProbabilityErrorMargin())))) {
				throw new IllegalArgumentException("Inconsistent prior probability: " + sum);
			}
		} else {	// i.e. initProbs == null || initProbs.isEmpty()
			// Instantiate initProbs. Reuse same instance if it was passed as argument
			if (initProbs == null) {
				initProbs = new ArrayList<Float>();
			}
			// prior probability was not set. Assume it to be uniform distribution
			for (int i = 0; i < numberStates; i++) {
				initProbs.add(1f/numberStates);
			}
		}
		
		// instantiate the action object for adding a question
		this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs));
		
		return true;
	}
	
	/**
	 * Represents a network action for adding a question into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestion(long, Date, long, int, List)
	 */
	public class AddQuestionNetworkAction implements NetworkAction {
		private final long transactionKey;
		private final Date occurredWhen;
		private final long questionId;
		private final int numberStates;
		private final List<Float> initProbs;
		/** Default constructor initializing fields */
		public AddQuestionNetworkAction(long transactionKey, Date occurredWhen,
				long questionId, int numberStates, List<Float> initProbs) {
			super();
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.questionId = questionId;
			this.numberStates = numberStates;
			this.initProbs = initProbs;
		}
		/**
		 * Adds a new question into the current network
		 */
		public void execute() {
			// create new node
			ProbabilisticNode node = new ProbabilisticNode();
			node.setName(Long.toString(this.questionId));
			// add states
			for (int i = 0; i < this.numberStates; i++) {
				node.appendState(Integer.toString(i));
			}
			// initialize CPT (actually, it is a prior probability, because we do not have parents yet).
			PotentialTable potTable = node.getProbabilityFunction();
			if (potTable.getVariablesSize() <= 0) {
				potTable.addVariable(node);
			}
			// fill cpt
			for (int i = 0; i < potTable.tableSize(); i++) {
				potTable.setValue(i, this.initProbs.get(i));
			}
			// add node into the network
			synchronized (getProbabilisticNetwork()) {
				getProbabilisticNetwork().addNode(node);
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new javax.help.UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
		}
		public Date getWhenCreated() { return this.occurredWhen; }
		public Long getTransactionKey() { return transactionKey;
		}
		public Long getQuestionId() { return questionId; }
		public int getNumberStates() { return numberStates; }
		/** Adding a new node is a structure change */
		public boolean isStructureChangeAction() { return true; }
		public Long getUserId() { return null; }
		public String toString() { return super.toString() + "{" + this.transactionKey + ", " + this.getQuestionId() + "}"; }
		public List<Float> getPercent() { return initProbs; }
		public String getTradeId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)
	 */
	public boolean addQuestionAssumption(long transactionKey, Date occurredWhen, long childQuestionId, List<Long> parentQuestionIds,  List<Float> cpd) throws IllegalArgumentException {
		
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		
		// check existence of transactionKey
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		int childNodeStateSize = -1;	// this var stores the quantity of states of the node identified by sourceQuestionId.
		
		// check existence of child
		Node child  = null;
		synchronized (getProbabilisticNetwork()) {
			child = getProbabilisticNetwork().getNode(Long.toString(childQuestionId));
		}
		if (child == null) {
			// child node does not exist. Check if there was some previous transaction adding such node
			synchronized (actions) {
				for (NetworkAction networkAction : actions) {
					if (networkAction instanceof AddQuestionNetworkAction) {
						AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
						if (addQuestionNetworkAction.getQuestionId() == childQuestionId) {
							childNodeStateSize = addQuestionNetworkAction.getNumberStates();
							break;
						}
					}
				}
			}
			if (childNodeStateSize < 0) {	
				// if negative, then expectedSizeOfCPD was not updated, so sourceQuestionId was not found in last loop
				throw new IllegalArgumentException("Question ID " + childQuestionId + " does not exist.");
			}
		} else {
			// initialize the value of expectedSizeOfCPD using the number of states of future owner of the cpd
			childNodeStateSize = child.getStatesSize();
		}
		
		// this var will store the correct size of cpd. If negative, owner of the cpd was not found.
		int expectedSizeOfCPD = childNodeStateSize;
		
		// do not allow null values for collections
		if (parentQuestionIds == null) {
			parentQuestionIds = new ArrayList<Long>();
		}
		
		// check existence of parents
		for (Long assumptiveQuestionId : parentQuestionIds) {
			Node parent =null;
			synchronized (getProbabilisticNetwork()) {
				parent = getProbabilisticNetwork().getNode(Long.toString(assumptiveQuestionId));
			}
			if (parent == null) {
				// parent node does not exist. Check if there was some previous transaction adding such node
				synchronized (actions) {
					boolean hasFound = false;
					for (NetworkAction networkAction : actions) {
						if (networkAction instanceof AddQuestionNetworkAction) {
							AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
							if (addQuestionNetworkAction.getQuestionId() == assumptiveQuestionId) {
								// size of cpd = MULT (<quantity of states of child and parents>).
								expectedSizeOfCPD *= addQuestionNetworkAction.getNumberStates();
								hasFound = true;
								break;
							}
						}
					}
					if (!hasFound) {	
						// parent was not found
						throw new IllegalArgumentException("Question ID " + assumptiveQuestionId + " does not exist.");
					}
				}
			} else{
				// size of cpd = MULT (<quantity of states of child and parents>).
				expectedSizeOfCPD *= parent.getStatesSize();
			}
			
		}
		
		// check consistency of size of cpd
		if (cpd != null && !cpd.isEmpty()){
			if (cpd.size() != expectedSizeOfCPD) {
				// size of cpd is inconsistent
				throw new IllegalArgumentException("Expected size of cpd of question " + childQuestionId + " is "+ expectedSizeOfCPD + ", but was " + cpd.size());
			}
			// check value consistency
			float sum = 0;
			int counter = 0;	// counter in which possible values are mod childNodeStateSize
			for (Float probability : cpd) {
				if (probability < 0 || probability > 1) {
					throw new IllegalArgumentException("Invalid probability declaration found: " + probability);
				}
				sum += probability;
				counter++;
				if (counter >= childNodeStateSize) {
					// check if sum of conditional probability given current state of parents is 1
					if (!(((1 - getProbabilityErrorMargin()) < sum) && (sum < (1 + getProbabilityErrorMargin())))) {
						throw new IllegalArgumentException("Inconsistent prior probability: " + sum);
					}
					counter = 0;
					sum = 0;
				}
			}
		}
		
		// instantiate the action object for adding the edge
		this.addNetworkAction(transactionKey, new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen, childQuestionId, parentQuestionIds, cpd));
		
		return true;
	}
	
	/**
	 * Represents a network action for adding a direct dependency (edge) into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestionAssumption(long, Date, long, long, List)
	 */
	public class AddQuestionAssumptionNetworkAction implements NetworkAction {
		private final long transactionKey;
		private final Date occurredWhen;
		private final long sourceQuestionId;
		private final List<Long> assumptiveQuestionIds;
		private final List<Float> cpd;

		/** Default constructor initializing fields */
		public AddQuestionAssumptionNetworkAction(long transactionKey,
				Date occurredWhen, long sourceQuestionId,
				List<Long> assumptiveQuestionIds, List<Float> cpd) {
			super();
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.sourceQuestionId = sourceQuestionId;
			this.assumptiveQuestionIds = assumptiveQuestionIds;
			this.cpd = cpd;
		}
		public void execute() {
			ProbabilisticNode child;	// this is the main node (the main question we are modifying)
			
			ProbabilisticNetwork network = getProbabilisticNetwork();	// network containing question
			
			synchronized (network) {
				child = (ProbabilisticNode) network.getNode(Long.toString(sourceQuestionId));
				
				// if cpd is non-empty, then we shall substitute the old edges going to child. So, delete all of them first.
				if (cpd != null && !cpd.isEmpty()) {
					Set<Edge> edgesToRemove = new HashSet<Edge>();
					for (Edge edge : network.getEdges()) {
						if (edge.getDestinationNode().equals(child)) {
							edgesToRemove.add(edge);
						}
					}
					for (Edge edge : edgesToRemove) {
						network.removeEdge(edge);
					}
				}
				
				for (Long assumptiveQuestionId : assumptiveQuestionIds) {
					// obtain  nodes
					ProbabilisticNode parent = (ProbabilisticNode) network.getNode(Long.toString(assumptiveQuestionId));
					// Instantiate new edge
					Edge edge = new Edge(parent,child);
					// add edge into the network
					try {
						network.addEdge(edge);
					} catch (InvalidParentException e) {
						throw new RuntimeException("Could not add edge from " + parent + " to " + child, e);
					}
				}
			}
			
			// extract CPT 
			PotentialTable potTable = child.getProbabilityFunction();
			synchronized (potTable) {
				if (cpd == null || cpd.isEmpty()) {
					// fill table with uniform distribution 
					for (int i = 0; i < potTable.tableSize(); i++) {
						potTable.setValue(i, 1f/child.getStatesSize());
					}
				} else {
					// fill table using values provided in cpd
					for (int i = 0; i < potTable.tableSize(); i++) {
						potTable.setValue(i, this.cpd.get(i));
					}
					// normalize table
					new NormalizeTableFunction().applyFunction((ProbabilisticTable) potTable);
				}
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new javax.help.UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
		}
		public Date getWhenCreated() { return this.occurredWhen; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getQuestionId() { return sourceQuestionId; }
		public List<Long> getAssumptionIds() { return assumptiveQuestionIds; }
		/** Adding a new edge is a structure change */
		public boolean isStructureChangeAction() { return true; }
		public Long getUserId() { return null; }
		public List<Float> getPercent() { return cpd; }
		public String getTradeId() { return null; }
		public List<Integer> getAssumedStates() { return null; }
	}

	/**
	 * Represents an action for adding cash to {@link AssetNetwork} (i.e. increase the min-Q value with a certain amount).
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addCash(long, Date, long, float, String)
	 * @see MarkovEngineImpl#isToAddCashProportionally()
	 * @see MarkovEngineImpl#setToAddCashProportionally(boolean)
	 */
	public class AddCashNetworkAction implements NetworkAction {
		private final long transactionKey;
		private final Date occurredWhen;
		private final long userId;
		private float assets;
		private final String description;
		/** becomes true once {@link #execute()} was called */
		private boolean wasExecutedPreviously = false;
		/** Default constructor initializing fields */
		public AddCashNetworkAction (long transactionKey, Date occurredWhen, long userId, float assets, String description) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.userId = userId;
			this.assets = assets;
			this.description = description;
		}
		public void execute() {
			
			// extract user's asset net and related algorithm
			AssetAwareInferenceAlgorithm inferenceAlgorithm = null;
			
			try {
				inferenceAlgorithm = getAlgorithmAndAssetNetFromUserID(userId);
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not create asset tables for user " + userId, e);
			}
			
			synchronized (inferenceAlgorithm.getAssetNetwork()) {
				if (isToAddCashProportionally()) {
					/*
					 * Calculate ratio of change (newCash/oldCash) and apply same ratio to all cells.
					 */
					// calculate old cash (i.e. old minQ) value
					float minQ = Float.NaN;
					
					inferenceAlgorithm.runMinPropagation();
					minQ = inferenceAlgorithm.calculateExplanation(new ArrayList<Map<INode,Integer>>());
					inferenceAlgorithm.undoMinPropagation();
					
					if (!Float.isNaN(minQ)) {
						// calculate ratio
						float ratio = (minQ + getQValuesFromScore(assets)) / minQ;
						// multiply ratio to all cells of asset tables of cliques
						for (Clique clique : inferenceAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
							PotentialTable assetTable = clique.getProbabilityFunction();
							for (int i = 0; i < assetTable.tableSize(); i++) {
								assetTable.setValue(i, assetTable.getValue(i) * ratio);
							}
						}
						// mult ratio to all cells in asset tables of separators
						for (Separator separator : inferenceAlgorithm.getAssetNetwork().getJunctionTree().getSeparators()) {
							PotentialTable assetTable = separator.getProbabilityFunction();
							for (int i = 0; i < assetTable.tableSize(); i++) {
								assetTable.setValue(i, assetTable.getValue(i) * ratio);
							}
						}
					} else {
						throw new RuntimeException("Could not extract minimum assets of user " + userId);
					}
				} else {
					float qValue = getQValuesFromScore(assets);
					// add assets to all cells in asset tables of cliques
					for (Clique clique : inferenceAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
						PotentialTable assetTable = clique.getProbabilityFunction();
						for (int i = 0; i < assetTable.tableSize(); i++) {
							assetTable.setValue(i, assetTable.getValue(i) + qValue);
						}
					}
					// add assets to all cells in asset tables of separators
					for (Separator separator : inferenceAlgorithm.getAssetNetwork().getJunctionTree().getSeparators()) {
						PotentialTable assetTable = separator.getProbabilityFunction();
						for (int i = 0; i < assetTable.tableSize(); i++) {
							assetTable.setValue(i, assetTable.getValue(i) + qValue);
						}
					}
				}
			}
			
			this.wasExecutedPreviously = true;
		}
		public void revert() throws UnsupportedOperationException {
			if (wasExecutedPreviously) {
				// undoing a add operation is equivalent to adding the inverse value
				this.assets = -this.assets;
				this.execute();
			}
		}
		public Date getWhenCreated() { return occurredWhen; }
		/**this operation does not change network structure*/
		public boolean isStructureChangeAction() { return false;	 }
		public Long getTransactionKey() { return transactionKey; }
		public float getAssets() { return assets; }
		public void setAssets(float assets) { this.assets = assets; }
		public Long getUserId() { return userId; }
		public String getDescription() { return description; }
		public List<Float> getPercent() { return null; }
		/** Returns the description */
		public String getTradeId() { return description; }
		public Long getQuestionId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
	}

	
	
	/**
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addCash(long, java.util.Date, long, float, java.lang.String)
	 * @see MarkovEngineImpl#isToAddCashProportionally()
	 * @see MarkovEngineImpl#setToAddCashProportionally(boolean)
	 */
	public boolean addCash(long transactionKey, Date occurredWhen, long userId, float assets, String description) throws IllegalArgumentException {
		if (Float.compare(0f, assets) == 0) {
			// nothing to add
			return false;
		}
		// check if assets can be translated to asset Q values
		try {
			float qValue = this.getQValuesFromScore(assets);
			if (Float.isInfinite(qValue) || Float.isNaN(qValue)) {
				throw new IllegalArgumentException("q-value is " + (Float.isInfinite(qValue)?"infinite":"not a number"));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot calculate asset's q-value from score = " 
						+ assets 
						+ ", log base = " + getCurrentLogBase()
						+ ", currency constant (b-value) = " + getCurrentCurrencyConstant()
					, e);
		}
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		
		
		// instantiate the action object for adding cash
		this.addNetworkAction(transactionKey, new AddCashNetworkAction(transactionKey, occurredWhen, userId, assets, description));
		
		
		return true;
	}

	/**
	 * This is the {@link NetworkAction} command
	 * representing {@link MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)}
	 * @author Shou Matsumoto
	 */
	public class AddTradeNetworkAction implements NetworkAction {
		private final Date whenCreated;
		private final long transactionKey;
		private final String tradeKey;
		private final long userId;
		private final long questionId;
		private final List<Float> newValues;
		private List<Long> assumptionIds;
		private final List<Integer> assumedStates;
		private final boolean allowNegative;
		private Map<IRandomVariable, PotentialTable> qTablesBeforeTrade;
		/** Default constructor initializing fields */
		public AddTradeNetworkAction(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, 
				List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) {
			this.transactionKey = transactionKey;
			this.whenCreated = occurredWhen;
			this.tradeKey = tradeKey;
			this.userId = userId;
			this.questionId = questionId;
			this.newValues = newValues;
			this.assumptionIds = assumptionIds;
			this.assumedStates = assumedStates;
			this.allowNegative = allowNegative;
			
		}
		public void execute() {
			// extract user's asset network from user ID
			AssetAwareInferenceAlgorithm algorithm = null;
			try {
				algorithm = getAlgorithmAndAssetNetFromUserID(userId);
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			if (algorithm == null) {
				throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			}
			
			synchronized (getProbabilisticNetwork()) {
				synchronized (algorithm.getAssetNetwork()) {
					if (!algorithm.getNetwork().equals(getProbabilisticNetwork())) {
						// this should never happen, but some desync may happen.
						Debug.println(getClass(), "[Warning] desync of network detected.");
						algorithm.setNetwork(getProbabilisticNetwork());
					}
					// do trade. Since algorithm is linked to actual networks, changes will affect the actual networks
					executeTrade(questionId, newValues, assumptionIds, assumedStates, allowNegative, algorithm);
					// backup the previous assets so that we can revert this trade
					qTablesBeforeTrade = algorithm.getAssetTablesBeforeLastPropagation();
				}
			}
		}
		/** 
		 * As discussed with Dr. Robin Hanson (<a href="mailto:rhanson@gmu.edu">rhanson@gmu.edu</a>) on
		 * 07/29/2012 during the DAGGRE algorithm meeting, the {@link MarkovEngineInterface#revertTrade(long, Date, Date, Long)}
		 * should only set the user's assets into the point prior to when {@link #execute()} was called.
		 * CAUTION: this method is not actually reverting a trade. It is setting
		 * the asset tables to values prior to the execution of {@link #execute()}.
		 */
		public void revert() throws UnsupportedOperationException {
			// extract user's asset network from user ID
			AssetAwareInferenceAlgorithm algorithm = null;
			try {
				algorithm = getAlgorithmAndAssetNetFromUserID(userId);
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			if (algorithm == null) {
				throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			}
			synchronized (algorithm.getAssetNetwork()) {
				// revert clique's q-tables
				for (Clique clique : algorithm.getAssetNetwork().getJunctionTree().getCliques()) {
					// current table and previous table are supposed to have same size
					clique.getProbabilityFunction().setValues(getqTablesBeforeTrade().get(clique).getValues());
				}
				// revert separator's q-tables
				for (Separator separator : algorithm.getAssetNetwork().getJunctionTree().getSeparators()) {
					// current table and previous table are supposed to have same size
					separator.getProbabilityFunction().setValues(getqTablesBeforeTrade().get(separator).getValues());
				}
			}
		}
		public Date getWhenCreated() { return whenCreated; }
		public boolean isStructureChangeAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return userId; }
		public Long getQuestionId() { return questionId; }
		public List<Long> getAssumptionIds() { return assumptionIds; }
		public List<Integer> getAssumedStates() { return assumedStates; }
		public boolean isAllowNegative() { return allowNegative; }
		public List<Float> getPercent() { return newValues; }
		public String getTradeId() { return tradeKey; }
		/** Mapping from {@link Clique}/{@link Separator} to q-tables before {@link #execute()}*/
		public Map<IRandomVariable, PotentialTable> getqTablesBeforeTrade() { return qTablesBeforeTrade; }
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<Float> addTrade(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) throws IllegalArgumentException {
		
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		
		// returned value is the same of preview trade
		List<Float> ret = null;
		try {
			ret = this.previewTrade(userId, questionId, newValues, assumptionIds, assumedStates);
		} catch (IllegalStateException e) {
			Debug.println(getClass(), 
					"[" + occurredWhen + "] failed to preview trade " + tradeKey 
					+ " of user " + userId
					+ " of transaction " + transactionKey
					+ " of question " + questionId
					+ " given assumptions: " + assumptionIds + "; states: " + assumedStates
					+ " because the shared Bayesian Network was not properly initialized."
					, e
				);
		}
		
		// NOTE: preview trade is performed *before* the insertion of a new action into the transaction, 
		// because we only want the transaction to be altered if the preview trade has returned successfully.
		
		// instantiate the action object for adding trade
		AddTradeNetworkAction newAction = new AddTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues, assumptionIds, assumedStates, allowNegative);
		
		this.addNetworkAction(transactionKey, newAction);
		
		// return the previewed asset values
		return ret;
	}

	

	/**
	 * This is the {@link NetworkAction} command representing
	 * {@link MarkovEngineImpl#resolveQuestion(long, Date, long, int)}.
	 * @author Shou Matsumoto
	 */
	public class ResolveQuestionNetworkAction implements NetworkAction {
		private final long transactionKey;
		private final Date occurredWhen;
		private final long questionId;
		private final int settledState;
		private List<Float> marginalWhenResolved;
		/** Default constructor initializing fields */
		public ResolveQuestionNetworkAction (long transactionKey, Date occurredWhen, long questionId, int settledState) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.questionId = questionId;
			this.settledState = settledState;
			
		}
		public void execute() {
			synchronized (getProbabilisticNetwork()) {
				TreeVariable node = (TreeVariable) getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(questionId));
				
				// extract marginal 
				marginalWhenResolved = new ArrayList<Float>(node.getStatesSize());
				for (int i = 0; i < node.getStatesSize(); i++) {
					marginalWhenResolved.add(node.getMarginalAt(i));
				}
				
				// TODO revert on some error
				getProbabilisticNetwork().removeNode(node);
				// do not release lock to global BN until we change all asset nets
				for (AssetAwareInferenceAlgorithm assetAlgorithm : getUserToAssetAwareAlgorithmMap().values()) {
					synchronized (assetAlgorithm.getAssetNetwork()) {
						assetAlgorithm.setAsPermanentEvidence(node, settledState);
					}
				}
			}
			
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Current version cannot revert a resolution of a question.");
		}
		public Date getWhenCreated() { return occurredWhen; }
		/** Although this method changes the structure, we do not want to call {@link RebuildNetworkAction} after this action, so return false */
		public boolean isStructureChangeAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		/** this is not an operation performed by a particular user */ 
		public Long getUserId() { return null;}
		public Long getQuestionId() { return questionId; }
		public int getSettledState() { return settledState; }
		public List<Float> getPercent() { return marginalWhenResolved; }
		public String getTradeId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#resolveQuestion(long, java.util.Date, long, int)
	 */
	public boolean resolveQuestion(long transactionKey, Date occurredWhen,
			long questionId, int settledState) throws IllegalArgumentException {

		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (settledState < 0) {
			throw new IllegalArgumentException("Question " + questionId + " has no state " + settledState);
		}
		Node node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (node == null) {
			throw new IllegalArgumentException("Question ID " + questionId + " was not found.");
		}
		if (settledState >= node.getStatesSize()) {
			throw new IllegalArgumentException("Question " + questionId + " has no state " + settledState);
		}

		
		// instantiate the action object for adding a question
		this.addNetworkAction(transactionKey, new ResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settledState));
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#revertTrade(long, java.util.Date, java.lang.Long, java.lang.Long)
	 */
	public boolean revertTrade(long transactionKey, Date occurredWhen,  Date tradesStartingWhen, Long questionId) throws IllegalArgumentException {
		// TODO Auto-generated method stub

		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		
		// initial assertion: check consistency of assumptionIds and assumedStates
		if (assumptionIds != null && assumedStates != null) {
			if (assumedStates.size() != assumptionIds.size()) {
				throw new IllegalArgumentException("assumptionIds.size() == " + assumptionIds.size() + ", assumedStates.size() == " + assumedStates.size());
			}
		}
		
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
		if (conditionalProbabilityExtractor == null) {
			throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
		}
		
		
		PotentialTable cpt = null;
		synchronized (getProbabilisticNetwork()) {
			INode mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
			if (mainNode == null) {
				throw new IllegalArgumentException("Question " + questionId + " not found");
			}
			List<INode> parentNodes = new ArrayList<INode>();
			if (assumptionIds != null) {
				for (Long id : assumptionIds) {
					INode node = getProbabilisticNetwork().getNode(Long.toString(id));
					if (node == null) {
						throw new IllegalArgumentException("Question " + questionId + " not found");
					}
					parentNodes.add(node);
				}
			}
			cpt = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, getProbabilisticNetwork(), null);
		}
		
		// convert cpt to a list of float, given assumedStates.
		List<Float> ret = new ArrayList<Float>(cpt.tableSize());
		for (int i = 0; i < cpt.tableSize(); i++) {
			boolean isToSkip = false;
			// filter entries which are incompatible with assumedStates
			if (assumedStates != null && !assumedStates.isEmpty()) {
				// extract coordinate of the states (e.g. [2,1,0] means state of mainNode = 2, state of parent1 = 1, and parent2 == 0)
				int[] multidimensionalCoord = cpt.getMultidimensionalCoord(i);
				// note: size of assumedStates is 1 unit smaller than multidimensionalCoord, because multidimensionalCoord contains the main node
				if (multidimensionalCoord.length != assumedStates.size() + 1) {
					throw new RuntimeException("Multi dimensional coordinate of index " + i + " has size " + multidimensionalCoord.length
							+ ". Expected " + assumedStates.size());
				}
				// iterate from index 1, because we do not consider the main node (which is in index 0 of multidimensionalCoord)
				for (int j = 1; j < multidimensionalCoord.length; j++) {
					if ((assumedStates.get(j-1) != null)
							&& (assumedStates.get(j-1) != multidimensionalCoord[j])) {
						isToSkip = true;
						break;
					}
				}
			}
			if (!isToSkip) {
				ret.add(cpt.getValue(i));
			}
		}
		return ret;
		
	}


	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getPossibleQuestionAssumptions(long, java.util.List)
	 */
	public List<Long> getPossibleQuestionAssumptions(long questionId, List<Long> assumptionIds) throws IllegalArgumentException {
		// this object is going to be used to extract what nodes can become assumptions in a conditional soft evidence
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
		if (conditionalProbabilityExtractor == null) {
			throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
		}
		
		// extract main node
		Node mainNode = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			throw new IllegalArgumentException("Question " + questionId + " not found.");
		}
		// extract assumption nodes
		List<INode> assumptions = new ArrayList<INode>();
		if (assumptionIds != null) {
			for (Long id : assumptionIds) {
				Node node = null;
				synchronized (getProbabilisticNetwork()) {
					node = getProbabilisticNetwork().getNode(Long.toString(id));
				}
				if (node == null) {
					throw new IllegalArgumentException("Question " + id + " not found.");
				}
				assumptions.add(node);
			}
		}
		
		// obtain possible condition nodes
		List<INode> returnedNodes = null;
		synchronized (getProbabilisticNetwork()) {
			returnedNodes = conditionalProbabilityExtractor.getValidConditionNodes(mainNode, assumptions, getProbabilisticNetwork(), null);
		}
		// convert condition nodes to ids
		List<Long> ret = new ArrayList<Long>();
		if (returnedNodes != null){
			for (INode node : returnedNodes) {
				if (node != null) {
					// question IDs and node names are supposedly equal
					ret.add(Long.parseLong(node.getName()));
				}
			}
		}
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getAssetsIfStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> getAssetsIfStates(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// initial assertion: check consistency of assumptionIds and assumedStates
//		if (assumptionIds != null && assumedStates != null) {
//			if (assumedStates.size() != assumptionIds.size()) {
//				throw new IllegalArgumentException("assumptionIds.size() == " + assumptionIds.size() + ", assumedStates.size() == " + assumedStates.size());
//			}
//		}
		// the above assertion is not necessary in this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm); 
		
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		// we can use the same object in order to extract conditional assets, because asset cliques extends prob cliques.
//		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
//		if (conditionalProbabilityExtractor == null) {
//			throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
//		}
		
		// extract user's asset network from user ID
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		return this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false);	// false := return assets instead of q-values
	}
	
	/**
	 * This method is used in {@link #getAssetsIfStates(long, long, List, List)} and {@link #previewTrade(long, long, List, List, List)}
	 * in order to extract the conditional assets from the {@link AssetNetwork} related to a given algorithm.
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param assumptionIds : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The order
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * @param algorithm : the algorithm to be used in order to extract info from {@link AssetNetwork}. 
	 * {@link AssetAwareInferenceAlgorithm#getAssetNetwork()} is used in order to extract the instance of {@link AssetNetwork}.
	 * @param isToReturnQValuesInsteadOfAssets : if false, the returned list will contain values returned by {@link #getScoreFromQValues(float)} 
	 * (i.e. logarithm values, instead of the q-values stored in the asset tables). 
	 * If true, the returned list will contain q-values (values actually stored in the assets table) instead of what the DAGGRE side call "assets" (the logarithm values).
	 * @return the change in user assets if a given states occurs if the specified assumptions are met. 
	 * The indexes are relative to the indexes of the states.
	 * In the case of a binary question this will return a [if_true, if_false] value, if multiple choice will return a [if_0, if_1, if_2...] value list
	 * For example, assuming that the question identified by questionId is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the assets of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the assets of the
	 * question while it is in state "true".
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	protected List<Float> getAssetsIfStates(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, AssetAwareInferenceAlgorithm algorithm, boolean isToReturnQValuesInsteadOfAssets)
			throws IllegalArgumentException {
		// basic assertion
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm cannot be null");
		}
		PotentialTable assetTable = null;	// asset tables (clique table containing assets) are instances of potential tables
		synchronized (algorithm.getAssetNetwork()) {
			INode mainNode = algorithm.getAssetNetwork().getNode(Long.toString(questionId));
			if (mainNode == null) {
				throw new IllegalArgumentException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork());
			}
			List<INode> parentNodes = new ArrayList<INode>();
			if (assumptionIds != null) {
				for (Long id : assumptionIds) {
					INode node = algorithm.getAssetNetwork().getNode(Long.toString(id));
					if (node == null) {
						throw new IllegalArgumentException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork());
					}
					parentNodes.add(node);
				}
			}
			assetTable = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, algorithm.getAssetNetwork(), algorithm.getAssetPropagationDelegator());
		}
		
		// convert cpt to a list of float, given assumedStates.
		// TODO change the way it sum-out/min-out/max-out the unspecified states in the clique potential
		List<Float> ret = new ArrayList<Float>(assetTable.tableSize());
		for (int i = 0; i < assetTable.tableSize(); i++) {
			boolean isToSkip = false;
			// filter entries which are incompatible with assumedStates
			if (assumedStates != null && !assumedStates.isEmpty()) {
				// extract coordinate of the states (e.g. [2,1,0] means state of mainNode = 2, state of parent1 = 1, and parent2 == 0)
				int[] multidimensionalCoord = assetTable.getMultidimensionalCoord(i);
				// note: size of assumedStates is 1 unit smaller than multidimensionalCoord, because assumedStates does not contain the main node
				if (multidimensionalCoord.length != assumedStates.size() + 1) {
					throw new RuntimeException("Multi dimensional coordinate of index " + i + " has size " + multidimensionalCoord.length
							+ ". Expected " + assumedStates.size());
				}
				// start from index 1, because index 0 of multidimensionalCoord is the main node
				for (int j = 1; j < multidimensionalCoord.length; j++) {
					if ((assumedStates.get(j-1) != null)
							&& (assumedStates.get(j-1) != multidimensionalCoord[j])) {
						isToSkip = true;
						break;
					}
				}
			}
			if (!isToSkip) {
				if (isToReturnQValuesInsteadOfAssets) {
					// return q-values directly
					ret.add(assetTable.getValue(i));
				} else {
					// convert q-values to assets (i.e. logarithmic values)
					ret.add(this.getScoreFromQValues(assetTable.getValue(i)));
				}
			}
		}
		return ret;
		
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getEditLimits(long, long, int, java.util.List, java.util.List)
	 */
	public List<Float> getEditLimits(long userId, long questionId,
			int questionState, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// initial assertion: check consistency of assumptionIds and assumedStates
		if (assumptionIds != null) {
			if ( assumedStates == null) {
				throw new IllegalArgumentException("assumptionIds.size() == " + assumptionIds.size() + ", assumedStates == null.");
			} else if (assumedStates.size() != assumptionIds.size()) {
				throw new IllegalArgumentException("assumptionIds.size() == " + assumptionIds.size() + ", assumedStates.size() == " + assumedStates.size());
			}
		}
		// make sure assumptionIds does not contain null
		if (assumptionIds!= null && assumptionIds.contains(null)) {
			throw new IllegalArgumentException("assumptionIds contains null ID.");
		}
		// make sure assumedStates does not contain null
		if (assumedStates!= null && assumedStates.contains(null)) {
			throw new IllegalArgumentException("assumedStates contains null state.");
		}

		// this object is going to be used to extract what nodes can become assumptions in a conditional soft evidence
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
		if (conditionalProbabilityExtractor == null) {
			throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
		}
		
		// extract main node
		Node mainNode = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			throw new IllegalArgumentException("Question " + questionId + " not found.");
		}
		// extract assumption nodes
		List<INode> assumptions = new ArrayList<INode>();
		if (assumptionIds != null) {
			for (Long id : assumptionIds) {
				Node node = null;
				synchronized (getProbabilisticNetwork()) {
					node = getProbabilisticNetwork().getNode(Long.toString(id));
				}
				if (node == null) {
					throw new IllegalArgumentException("Question " + id + " not found.");
				}
				assumptions.add(node);
			}
		}
		
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		// this vector will contain allowed interval of edit
		float editInterval[] = null;
		synchronized (algorithm.getAssetNetwork()) {
			synchronized (getProbabilisticNetwork()) {
				PotentialTable table = (PotentialTable) getConditionalProbabilityExtractor().buildCondicionalProbability(mainNode, assumptions, getProbabilisticNetwork(), null);
				
				// clone assumedStates and add the state of the main node at index 0
				List<Integer> assumedStatesIncludingMainNode = new ArrayList<Integer>();
				if (assumedStates != null) {
					assumedStatesIncludingMainNode.addAll(assumedStates);
				}
				assumedStatesIncludingMainNode.add(0, questionState);
				if (assumedStatesIncludingMainNode.size() != table.variableCount()) {
					throw new IllegalArgumentException("Expected size of assumedStates is " + table.variableCount() + ", but was " + (assumedStatesIncludingMainNode.size() - 1));
				}
				
				// convert assumedStatesIncludingMainNode to multi-dimensional coordinate (readable by table)
				int [] multidimensionalCoord = new int[assumedStatesIncludingMainNode.size()];
				for (int i = 0; i < multidimensionalCoord.length; i++) {
					multidimensionalCoord[i] = assumedStatesIncludingMainNode.get(i);
				}
				
				// convert multi-dimensional coordinate to linear coordinate (second argument of calculateIntervalOfAllowedEdit) and obtain edit interval. 
				editInterval = algorithm.calculateIntervalOfAllowedEdit(table, table.getLinearCoord(multidimensionalCoord));
			}
		}
		if (editInterval == null) {
			return null;
		}
		// convert editInterval to a list
		List<Float> ret = new ArrayList<Float>(editInterval.length);
		for (float interval : editInterval) {
			ret.add(interval);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getCash(long, java.util.List, java.util.List)
	 */
	public float getCash(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		float ret = Float.NEGATIVE_INFINITY;	// value to return
		synchronized (algorithm.getAssetNetwork()) {
			// set up findings
			if (assumptionIds != null) {
				for (int i = 0; i < assumptionIds.size(); i++) {
					AssetNode node = (AssetNode) algorithm.getAssetNetwork().getNode(Long.toString(assumptionIds.get(i)));
					if (node == null) {
						throw new IllegalArgumentException("Question " + assumptionIds.get(i) + " does not exist.");
					}
					Integer stateIndex = assumedStates.get(i);
					if (stateIndex != null) {
						node.addFinding(stateIndex);
					}
				}
			}
			// run only min-propagation (i.e. calculate min-q given assumptions)
			algorithm.runMinPropagation();
			// obtain min-q value and explanation (states which cause the min-q values)
			ArrayList<Map<INode, Integer>> statesWithMinQAssets = new ArrayList<Map<INode,Integer>>();	// this is the min-q explanation (states which cause Min-q)
			ret = algorithm.calculateExplanation(statesWithMinQAssets);	// statesWithMinQAssets will be filled by this method
			// undo min-propagation, because the next iteration of asset updates should be based on non-min assets
			algorithm.undoMinPropagation();	
			// TODO use the values of statesWithMinQAssets for something (currently, this is ignored)
		}
		
		// convert q-values to score and return
		return getScoreFromQValues(ret);
	}


	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public float scoreUserQuestionEv(long userId, Long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEvStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserEv(long, java.util.List, java.util.List)
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUser(java.util.List, java.util.List, java.util.List)
	 */
	public float scoreUser(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#previewTrade(long, long, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> previewTrade(long userId, long questionId,List<Float> newValues,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		
		// initial assertions
		if (newValues == null || newValues.isEmpty()) {
			throw new IllegalArgumentException("newValues cannot be empty or null");
		}
		if (assumptionIds != null && !assumptionIds.isEmpty()) {
			// size of assumedStates must be equal to assumptionIds
			if (assumedStates == null) {
				throw new IllegalArgumentException("assumedStates is not expected to be null when assumptionIds is not null.");
			} else if (assumedStates.size() != assumptionIds.size()) {
				throw new IllegalArgumentException("Size of assumedStates is expected to be " + assumptionIds.size() + ", but was " + assumedStates.size());
			}
			// assumptionIds must not contain null value
			if (assumptionIds.contains(null)) {
				throw new IllegalArgumentException("Null assumption found.");
			}
			// assumedStates must not contain null value
			if (assumedStates.contains(null)) {
				throw new IllegalArgumentException("Assumption with state == null found.");
			}
		}
		
		// this algorithm will be alive only during the context of this method. It will contain clones of the bayesian network and asset net
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			
			// extract original algorithm, just in order to clone it
			AssetAwareInferenceAlgorithm originalAlgorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
			if (originalAlgorithm == null) {
				throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			
			// clone the algorithm. It shall also clone the bayesian network and asset network
			algorithm = (AssetAwareInferenceAlgorithm) originalAlgorithm.clone();
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Could not clone networks of user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		// do trade on specified algorithm (which only contains link to copies of BN and asset net)
		this.executeTrade(questionId, newValues, assumptionIds, assumedStates, true, algorithm);	// true := allow negative assets, since this is a preview
		
		// TODO optimize (executeTrade and getAssetsIfStates have redundant portion of code)
		
		// return the asset position
		return this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false); // false := return assets instead of q-values
	}
	
	/**
	 * 
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param newValues : this is a list (ordered collection) representing the probability values after the edit. 
	 * For example, suppose T is the target question (i.e. a random variable) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Then, the list must be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/>
	 * If the states of the conditions are specified in assumedStates, then this list will only specify the conditional
	 * probabilities of each states of questionId.
	 * E.g. Again, suppose T is the target question with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.]
	 * Also suppose that assumedStates = (1,0). Then, the content of newValues must be: <br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * 
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this list specifies a filter for states of nodes in assumptionIDs.
	 * If it does not have the same size of assumptionIDs,Å@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @param isToAllowNegative : if true, negative assets (values smaller than 1 in the asset q table) is allowed.
	 * @param algorithm : algorithm to be used in order to update probability and assets. 
	 * {@link AssetAwareInferenceAlgorithm#getNetwork()} will be used to access the Bayes net.
	 * {@link AssetAwareInferenceAlgorithm#getAssetNetwork()} will be used to access the asset net.
	 * @see #addTrade(long, Date, String, long, long, List, List, List, boolean)
	 * @see #previewTrade(long, long, List, List, List)
	 */
	protected void executeTrade(long questionId,List<Float> newValues,
			List<Long> assumptionIds, List<Integer> assumedStates, boolean isToAllowNegative, AssetAwareInferenceAlgorithm algorithm) {
		// basic assertions
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm was not specified.");
		}
		
		// obtain the network. It should be a copy, if the original is should be untouched
		ProbabilisticNetwork net = algorithm.getRelatedProbabilisticNetwork();
		if (net == null || !net.equals(getProbabilisticNetwork())) {
			throw new RuntimeException("Could not obtain bayesian network for user " + algorithm.getAssetNetwork() + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		// check existence of child
		TreeVariable child = (TreeVariable) net.getNode(Long.toString(questionId));
		if (child == null) {
			throw new IllegalArgumentException("Question " + questionId + " not found.");
		}
		
		// this var will store the correct size of cpd. If negative, owner of the cpd was not found.
		int expectedSizeOfCPD = child.getStatesSize();
		
		// do not allow null values for collections
		if (assumptionIds == null) {
			assumptionIds = new ArrayList<Long>();
		}
		
		// extract assumptions
		List<INode> assumptionNodes = new ArrayList<INode>();
		for (Long assumptiveQuestionId : assumptionIds) {
			Node parent = net.getNode(Long.toString(assumptiveQuestionId));
			if (parent == null) {
				throw new IllegalArgumentException("Question " + assumptiveQuestionId + " not found.");
			} 
			assumptionNodes.add(parent);
			// size of cpd if  = MULT (<quantity of states of child and parents>).
			expectedSizeOfCPD *= parent.getStatesSize();
		}
		
		// check consistency of newValues
		if (assumedStates != null && !assumedStates.isEmpty()) {
			// note: if assumedStates is not empty, size of newValues must be equals to the quantity of states of the child (main) node
			if (newValues.size() != child.getStatesSize()) {
				throw new IllegalArgumentException("Expected size of newValues was " + child.getStatesSize() + ", but obtained " + newValues.size());
			}
		} else if (newValues.size() != expectedSizeOfCPD) {
			throw new IllegalArgumentException("Expected size of newValues was " + expectedSizeOfCPD + ", but obtained " + newValues.size());
		}
		
		// check consistency of content of newValues
		float sum = 0;
		int counter = 0;	// counter in which possible values are mod childNodeStateSize
		for (Float probability : newValues) {
			if (probability < 0 || probability > 1) {
				throw new IllegalArgumentException("Invalid probability declaration found: " + probability);
			}
			sum += probability;
			counter++;
			if (counter >= child.getStatesSize()) {
				// check if sum of conditional probability given current state of parents is 1
				if (!(((1 - getProbabilityErrorMargin()) < sum) && (sum < (1 + getProbabilityErrorMargin())))) {
					throw new IllegalArgumentException("Inconsistent prior probability: " + sum);
				}
				counter = 0;
				sum = 0;
			}
		}
		
		
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
		if (conditionalProbabilityExtractor == null) {
			throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
		}
		
		// potential to be used as basis to calculate likelihood ratio for a soft evidence. This is the "current probability"
		PotentialTable potential = null;
		synchronized (net) {
			potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(child, assumptionNodes, net, null);
		}
		if (potential == null) {
			throw new RuntimeException("Could not extract current probabilities. Please, verify the version of your Markov Engine and UnBBayes.");
		}
		
		// change content of potential according to newValues and assumedStates
		if (assumedStates == null || assumedStates.isEmpty()) {
			for (int i = 0; i < newValues.size(); i++) {
				potential.setValue(i, newValues.get(i));
			}
		} else {
			// instantiate multi-dimensional coordinate to be used in order to change values in potential table
			int[] multidimensionalCoord = potential.getMultidimensionalCoord(0);
			// move multidimensionalCoord to the point related to assumedStates
			for (int i = 1; i < multidimensionalCoord.length; i++) { // index 0 is for the main node (which is not specified in assumedStates), so it is not used.
				multidimensionalCoord[i] = assumedStates.get(i-1);
			}
			// modify content of potential table accourding to newValues 
			for (int i = 0; i < newValues.size(); i++) {	// at this point, newValues.size() == child.statesSize()
				multidimensionalCoord[0] = i; // only iterate over states of the main node (i.e. index 0 of multidimensionalCoord)
				potential.setValue(potential.getLinearCoord(multidimensionalCoord), newValues.get(i));
			}
		}
		
		// fill array of likelihood with values in CPT
		float [] likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		child.addLikeliHood(likelihood, assumptionNodes);
		
		// propagate soft evidence
		synchronized (algorithm) {
			synchronized (algorithm.getNetwork()) {
				synchronized (algorithm.getAssetNetwork()) {
//					boolean backup = algorithm.isToAllowQValuesSmallerThan1();
					algorithm.setToAllowQValuesSmallerThan1(isToAllowNegative);
					algorithm.propagate();
//					algorithm.setToAllowQValuesSmallerThan1(backup);
				}
			}
		}
	}

	/**
	 * The balancing trade can be obtained by calculating the probabilities 
	 * P1, P2 , ... , PN (N is the quantity of states of the given node), in which:
	 * <br/><br/>
	 * P1 + P2 + ... + PN = 1
	 * <br/>
	 * q1*P1/p1 = q2*P2/p2 = ... = qN*PN/pN
	 * <br/><br/>
	 * Note: for  1 <= i <= N; Pi is the solution (posterior probability), pi is the current (prior) probability, and qi is the 
	 * asset-q value of the i-th state.
	 * <br/><br/>
	 * The solution of the above equation is:<br/>
	 * Pi = (q1 * q2 * ... * qi-1 * qi+1 * ... * qN * pi) / ( (q2*q3*q4*...*qN * p1) + (q1*q3*q4*...*qN * p2) + ... + (q1*q2*...*qi-1*qi+1*...*qN * pi) + ... + (q1*q2*...*qN-1 * pN) ) 
	 * <br/>
	 * For  1 <= i <= N
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#determineBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	public List<Float> determineBalancingTrade(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		
		// extract user's asset network from user ID
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract assets from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		// obtain q1, q1, ... , qn (the asset's q values)
		List<Float> qValues = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, true);	// true := return q-values instead of assets
		
		// obtain p1, p2, ... , pn (the prior probabilities)
		List<Float> probabilities = this.getProbList(questionId, assumptionIds, assumedStates);
		
		// basic assertion
		if (qValues.size() != probabilities.size()) {
			throw new RuntimeException("List of probabilities and list of assets have different sizes (" 
					+ probabilities.size() + ", and " + qValues.size() + " respectively). You may be using incompatible version of Markov Engine or UnBBayes.");
		}
		
		// this is a denominator (bottom side of a division) common to all solutions P1,P2,...,PN
		double commonDenominator = 0;	
		
		// Calculate q1*q2*...*qi-1*qi+1*...*qN*pi and store it in the list "probabilities". This is also a factor in commonDenominator.
		for (int i = 0; i < probabilities.size(); i++) {
			
			// product := (q1*q2*...*qi-1*qi+1*...*qN)
			double product = 1;	// 1 is the identity value in a multiplication
			for (int indexOfQ = 0; indexOfQ < qValues.size(); indexOfQ++) {
				if (indexOfQ != i) {
					product *= qValues.get(indexOfQ);
				}
			}
			
			// product := (q1*q2*...*qi-1*qi+1*...*qN) * pi
			product *= probabilities.get(i);
			probabilities.set(i, (float)product); // TODO solve double-float precision problem
			
			// commonDenominator := ( (q2*q3*q4*...*qN * p1) + (q1*q3*q4*...*qN * p2) + ... + (q1*q2*...*qi-1*qi+1*...*qN * pi) + ... + (q1*q2*...*qN-1 * pN) )
			commonDenominator += product;
		}
		
		// Calculate P1,P2,...,PN and store it in probabilities. 
		for (int i = 0; i < probabilities.size(); i++) {
			// Pi = ((q1*q2*...*qi-1*qi+1*...*qN)*pi) / commonDenominator
			probabilities.set(i, (float) (probabilities.get(i)/commonDenominator)); // TODO solve double-float precision problem
		}
		
		return probabilities;
	}
	
	/**
	 * Network action representing {@link MarkovEngineImpl#balanceTrade(long, Date, String, long, long, List, List)}
	 * @author Shou Matsumoto
	 */
	public class BalanceTradeNetworkAction implements NetworkAction {
		private final long transactionKey;
		private final Date occurredWhen;
		private final String tradeKey;
		private final long userId;
		private final long questionId;
		private final List<Long> assumptionIds;
		private final List<Integer> assumedStates;
		private List<Float> newValues = null;
		private AddTradeNetworkAction tradeAction;
		/** Default constructor initializing fields\ */
		public BalanceTradeNetworkAction(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.tradeKey = tradeKey;
			this.userId = userId;
			this.questionId = questionId;
			this.assumptionIds = assumptionIds;
			this.assumedStates = assumedStates;
		}
		/** Virtually does {@link MarkovEngineImpl#determineBalancingTrade(long, long, List, List)} and then {@link AddTradeNetworkAction#execute()} */
		public void execute() {
			synchronized (getProbabilisticNetwork()) {
				// obtain trade values for exiting the user from a question given assumptions
				newValues = determineBalancingTrade(userId, questionId, assumptionIds, assumedStates);
				// execute the trade without releasing lock
				tradeAction = new AddTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues, assumptionIds, assumedStates, false);
				tradeAction.execute();
				// TODO check if there is no risk of deadlock in blocking the same variable twice (here and inside AddTradeNetworkAction#execute())
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Not supported by current version of Markov Engine.");
		}
		public Date getWhenCreated() { return occurredWhen; }
		public List<Float> getPercent() { return newValues; }
		public String getTradeId() { return tradeKey; }
		/** balancing should not change structure */
		public boolean isStructureChangeAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return userId; }
		public Long getQuestionId() { return questionId; }
		public List<Long> getAssumptionIds() { return assumptionIds; }
		public List<Integer> getAssumedStates() { return assumedStates; }
		/** This is the action object used in order to execute the trade */
		protected void setTradeAction(AddTradeNetworkAction tradeAction) { this.tradeAction = tradeAction; }
		/** This is the action object used in order to execute the trade */
		public AddTradeNetworkAction getTradeAction() { return tradeAction; }
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#balanceTrade(long, long, long, java.util.List, java.util.List)
	 */
	public void balanceTrade(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		
		// check existence of transaction key
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		// check size of assumptions and states
		if (assumptionIds != null && !assumptionIds.isEmpty()) {
			// size of assumedStates must be equal to assumptionIds
			if (assumedStates == null) {
				throw new IllegalArgumentException("assumedStates is not expected to be null when assumptionIds is not null.");
			} else if (assumedStates.size() != assumptionIds.size()) {
				throw new IllegalArgumentException("Size of assumedStates is expected to be " + assumptionIds.size() + ", but was " + assumedStates.size());
			}
			// assumptionIds must not contain null value
			if (assumptionIds.contains(null)) {
				throw new IllegalArgumentException("Null assumption found.");
			}
			// assumedStates must not contain null value
			if (assumedStates.contains(null)) {
				throw new IllegalArgumentException("Assumption with state == null found.");
			}
		}
		
		// check existence of main node
		Node mainNode  = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			// node does not exist. Check if there was some previous transaction adding such node
			boolean willCreateNodeOnCommit = false;	// if true, the node will be created in this transaction
			synchronized (actions) {
				for (NetworkAction networkAction : actions) {
					if (networkAction instanceof AddQuestionNetworkAction) {
						AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
						if (addQuestionNetworkAction.getQuestionId() == questionId) {
							willCreateNodeOnCommit = true;
							break;
						}
					}
				}
			}
			if (willCreateNodeOnCommit) {	
				throw new IllegalArgumentException("Question ID " + questionId + " does not exist.");
			}
		} 
		
		// check existence of assumptions
		for (Long assumptiveQuestionId : assumptionIds) {
			Node parent =null;
			synchronized (getProbabilisticNetwork()) {
				parent = getProbabilisticNetwork().getNode(Long.toString(assumptiveQuestionId));
			}
			if (parent == null) {
				// parent node does not exist. Check if there was some previous transaction adding such node
				boolean hasFound = false;
				synchronized (actions) {
					for (NetworkAction networkAction : actions) {
						if (networkAction instanceof AddQuestionNetworkAction) {
							AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
							if (addQuestionNetworkAction.getQuestionId() == assumptiveQuestionId) {
								hasFound = true;
								break;
							}
						}
					}
				}
				if (!hasFound) {	
					// parent was not found
					throw new IllegalArgumentException("Question ID " + assumptiveQuestionId + " does not exist.");
				}
			}
		}
		
		// instantiate the action object for balancing trade
		this.addNetworkAction(transactionKey, new BalanceTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, assumptionIds, assumedStates));
		
	}
	

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
	 */
	public List<QuestionEvent> getQuestionHistory(Long questionID, List<Long> assumptionIDs, List<Integer> assumedStates) throws IllegalArgumentException {
		List<NetworkAction> list = null;
		synchronized (this.getNetworkActionsIndexedByQuestions()) {
			list = this.getNetworkActionsIndexedByQuestions().get(questionID);
		}
		if (list == null || list.isEmpty()) {
			// never return the object itself (because we do not want the caller to change content of getNetworkActionsIndexedByQuestions())
			return Collections.emptyList();	
		}
		// at this point, list != null
		List<QuestionEvent> ret = new ArrayList<QuestionEvent>(); // the list to be returned
		synchronized (list) {
			// fill ret with values in list, but filter by assumptionIDs and assumedStates
			for (NetworkAction action : list) {
				if (assumptionIDs == null || assumptionIDs.isEmpty()) {
					// no filter. Add everything
					ret.add(action);
				} else if (action.getAssumptionIds() != null && action.getAssumptionIds().containsAll(assumptionIDs)) {
					// matched assumptionIDs filter. Check assumedStates filter
					boolean hasWrongMatch = false;
					if (action.getAssumedStates() != null && !action.getAssumedStates().isEmpty()) {
						// if any states in the filter doesn't match the states in action.getAssumedStates(), do not add
						for (int i = 0; (i < assumedStates.size()) && (i < assumptionIDs.size()); i++) {
							// extract index of assumption in "action".
							int indexOfQuestionInNetworkAction = action.getAssumptionIds().indexOf(assumptionIDs.get(i));
							if (indexOfQuestionInNetworkAction < 0) {
								// supposedly, at this point, indexOfQuestionInNetworkAction >=0 , because action.getAssumptionIds().containsAll(assumptionIDs)
								try {
									Debug.println(getClass(), "Trade " + action.getTradeId() + " claims that it is using question " + assumptionIDs.get(i) 
											+ " as one of its assumptions, but could not access such question in the trade history.");
								} catch (Throwable t) {
									t.printStackTrace();
								}
								// ignore and go on
								continue;
							}
							if (indexOfQuestionInNetworkAction >= action.getAssumedStates().size()) {
								// there were more assumed questions than assumed states.
								try {
									Debug.println(getClass(), "Trade " + action.getTradeId() 
											+ " has more elements in assumptionIds (" + action.getAssumptionIds().size()  
											+ ") thanÅ@in assumedStates (" + action.getAssumedStates().size() + ").");
								} catch (Throwable t) {
									t.printStackTrace();
								}
								// ignore and go on
								continue;
							}
							if (assumedStates.get(i) != action.getAssumedStates().get(indexOfQuestionInNetworkAction)) {
								hasWrongMatch = true;
								break;
							}
						}
					}
					if (!hasWrongMatch) {
						ret.add(action);
					}
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreSummary(long, java.util.List, java.util.List)
	 */
	public List<Properties> getScoreSummary(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreDetails(long, java.util.List, java.util.List)
	 */
	public List<Properties> getScoreDetails(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * This map stores the transactionKeys and all actions
	 * to be executed by the transaction.
	 * @param networkActionsMap the networkActionsMap to set
	 * @see NetworkAction
	 */
	protected void setNetworkActionsMap(Map<Long, List<NetworkAction>> networkActionsMap) {
		this.networkActionsMap = networkActionsMap;
	}

	/**
	 * This map stores the transactionKeys and all actions
	 * to be executed by the transaction.
	 * @return the networkActionsMap
	 * @see NetworkAction
	 */
	protected Map<Long, List<NetworkAction>> getNetworkActionsMap() {
		return networkActionsMap;
	}

	/**
	 * This is used in {@link #startNetworkActions()}
	 * as the next transaction key.
	 * @param transactionCounter the transactionCounter to set
	 */
	protected void setTransactionCounter(long transactionCounter) {
		this.transactionCounter = transactionCounter;
	}

	/**
	 * This is used in {@link #startNetworkActions()}
	 * as the next transaction key.
	 * @return the transactionCounter
	 */
	public long getTransactionCounter() {
		return transactionCounter;
	}

	

	/**
	 * This is the error margin used when comparing two probability values.
	 * If ((prob1 - probabilityErrorMargin) < prob2) && (prob2 < (prob1 + probabilityErrorMargin)), then prob1 == prob2.
	 * @param probabilityErrorMargin the probabilityErrorMargin to set
	 */
	public void setProbabilityErrorMargin(float probabilityErrorMargin) {
		this.probabilityErrorMargin = probabilityErrorMargin;
	}

	/**
	 * This is the error margin used when comparing two probability values.
	 * If ((prob1 - probabilityErrorMargin) < prob2) && (prob2 < (prob1 + probabilityErrorMargin)), then prob1 == prob2.
	 * @return the probabilityErrorMargin
	 */
	public float getProbabilityErrorMargin() {
		return probabilityErrorMargin;
	}

	/**
	 * Mapping from user ID to {@link AssetAwareInferenceAlgorithm}
	 * (algorithm for managing bayesian network and assets), 
	 * which is related to {@link AssetNetwork} 
	 * (class representing user and asset q tables)
	 * @return the userToAssetAwareAlgorithmMap
	 */
	protected Map<Long, AssetAwareInferenceAlgorithm> getUserToAssetAwareAlgorithmMap() {
		return userToAssetAwareAlgorithmMap;
	}

	/**
	 * Mapping from user ID to {@link AssetAwareInferenceAlgorithm}
	 * (algorithm for managing bayesian network and assets), 
	 * which is related to {@link AssetNetwork} 
	 * (class representing user and asset q tables)
	 * @param map the map to set
	 */
	protected void setUserToAssetAwareAlgorithmMap(Map<Long, AssetAwareInferenceAlgorithm> map) {
		this.userToAssetAwareAlgorithmMap = map;
	}
	
	/**
	 * Obtains the getProbabilisticNetwork() associated with an asset network (structure representing the user and 
	 * the asset q values).
	 * Each asset network is associated with only 1 inference algorithm, so that we do not need to lock
	 * access to inference algorithm in order to perform operations in a scope of a single user.
	 * @param userID
	 * @return instance of AssetNetwork
	 * @throws InvalidParentException 
	 * @throws IllegalStateException : when this method was called before {@link #getProbabilisticNetwork()} is initialized.
	 * @see AssetAwareInferenceAlgorithm
	 */
	protected AssetAwareInferenceAlgorithm getAlgorithmAndAssetNetFromUserID(long userID) throws InvalidParentException, IllegalStateException {
		
		// assert that network was initialized
		boolean isNetworkOK = true;
		synchronized (getProbabilisticNetwork()) {
			if (getProbabilisticNetwork().getJunctionTree() == null) {
				throw new IllegalStateException("Failed to initialize user " + userID + ", because the shared Bayesian Network was not properly initialized. Please, initialize network and commit transaction.");
			}
		}
		
		if (!isNetworkOK) {
			throw new IllegalStateException("Failed to create user " + userID + ". The shared Bayesian Network was not properly initialized/compiled. Please, commit the transaction which initializes/compiles the shared Bayesian Network first.");
		}
		
		// value to be returned
		AssetAwareInferenceAlgorithm algorithm = null;
		
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			algorithm = getUserToAssetAwareAlgorithmMap().get(userID);
			if (algorithm == null) {
				// first time user is referenced. Prepare inference algorithm for the user
				JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
				// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
				junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
				// prepare default inference algorithm for asset network
				algorithm = ((AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm));
				// usually, users seem to start with 0 assets (assets are logarithmic, so 0 assets == 1 q table), but let's use the value of getDefaultInitialQTableValue
				algorithm.setDefaultInitialAssetQuantity(getDefaultInitialQTableValue());
				// force algorithm to call min-propagation only when prompted (i.e. only when runMinPropagation() is called)
				algorithm.setToPropagateForGlobalConsistency(false);	
				
				// generate new asset net
				AssetNetwork assetNet = null;
				synchronized (getProbabilisticNetwork()) {
					// lock access to network
					assetNet = algorithm.createAssetNetFromProbabilisticNet(getProbabilisticNetwork());
				}
				assetNet.setName(Long.toString(userID));
				
				// link algorithm to asset net.
				algorithm.setAssetNetwork(assetNet);
				
				getUserToAssetAwareAlgorithmMap().put(userID, algorithm);
			}
		}
		
		return algorithm;
	}
	
	/**
	 * This method includes an instance of {@link NetworkAction} into {@link #getNetworkActionsMap()}
	 * and other auxiliary structures which manages {@link NetworkAction}
	 * @param transactionKey : identifier of the transaction. This value is returned by {@link #startNetworkActions()}
	 * @param newAction : instance of {@link NetworkAction} to be added
	 * @throws IllegalArgumentException when transactionKey is an invalid key
	 */
	protected void addNetworkAction(long transactionKey,  NetworkAction newAction) throws IllegalArgumentException {
		if (newAction == null) {
			throw new NullPointerException("Attempted to add a null action into transaction " + transactionKey);
		}
		if (newAction instanceof AddQuestionNetworkAction) {
			this.addNetworkAction(transactionKey, (AddQuestionNetworkAction)newAction);
			return;
		}
		// check existence of transaction key
		// NOTE: getNetworkActionsMap is supposedly an instance of concurrent map, so we do not need to synchronize it
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		// let's add action to the managed list. 
		synchronized (actions) {
			// Prepare index of where in actions we should add newAction
			int indexOfFirstActionCreatedAfterNewAction = 0;	// this will point to the first action created after occurredWhen
			// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
			for (; indexOfFirstActionCreatedAfterNewAction < actions.size(); indexOfFirstActionCreatedAfterNewAction++) {
				if (actions.get(indexOfFirstActionCreatedAfterNewAction).getWhenCreated().after(newAction.getWhenCreated())) {
					break;
				}
			}
			
			// insert new action at the correct position
			actions.add(indexOfFirstActionCreatedAfterNewAction, newAction);
		}
		
		// insert new action into the map to be used for searching actions by question id
		this.addNetworkActionIntoQuestionMap(newAction);
	}
	
	/**
	 * This method is similar to {@link #addNetworkAction(long, NetworkAction)},
	 * but it blocks insertion of duplicate questionId.
	 * @param transactionKey : identifier of the transaction. This value is returned by {@link #startNetworkActions()}
	 * @param newAction : instance of {@link AddQuestionNetworkAction} to be added. 
	 * Duplicate {@link AddQuestionNetworkAction#getQuestionId()} will be blocked.
	 * @throws IllegalArgumentException when transactionKey is an invalid key, or {@link AddQuestionNetworkAction#getQuestionId()}
	 * is already present in the transaction.
	 */
	protected void addNetworkAction(long transactionKey,  AddQuestionNetworkAction newAction) throws IllegalArgumentException {
		if (newAction == null) {
			throw new NullPointerException("Attempted to add a null action into transaction " + transactionKey);
		}
		// check existence of transaction key
		// NOTE: getNetworkActionsMap is supposedly an instance of concurrent map, so we do not need to synchronize it
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		// let's add action to the managed list. Prepare index of where in actions we should add newAction
		int indexOfFirstActionCreatedAfterNewAction = -1;	// this will point to the first action created after occurredWhen
		
		
		// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
		for (int i = 0; i < actions.size(); i++) {
			NetworkAction action = actions.get(i);
			if (action instanceof AddQuestionNetworkAction) {
				AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) action;
				if (addQuestionNetworkAction.getQuestionId() == newAction.getQuestionId()) {
					// duplicate question in the same transaction
					throw new IllegalArgumentException("Question ID " + newAction.getQuestionId() + " is already present.");
				}
			}
			if (indexOfFirstActionCreatedAfterNewAction < 0 && action.getWhenCreated().after(newAction.getWhenCreated())) {
				indexOfFirstActionCreatedAfterNewAction = i;
				// do not break, because we are still looking for duplicate occurrences of questionId
			}
		}
		
		// add newAction into actions
		if (indexOfFirstActionCreatedAfterNewAction < 0) {
			// there is no action created after the new action. Add at the end.
			actions.add(newAction);
		} else {
			// insert new action at the correct position
			actions.add(indexOfFirstActionCreatedAfterNewAction, newAction);
		}
		
		// insert new action into the map to be used for searching actions by question id
		this.addNetworkActionIntoQuestionMap(newAction);
	}

	/**
	 * @param isToAddCashProportionally the isToAddCashProportionally to set.
	 * If true, {@link #addCash(long, Date, long, float, String)} will 
	 * increase cash (min-q) proportionally to the current value of min-q.
	 * <br/>
	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
	 * is set to add 10, then the final min-q is 15 (triple of the original min-q), hence
	 * other q-values will be also multiplied by 3.
	 * <br/>
	 * If false, the values added by {@link #addCash(long, Date, long, float, String)}
	 * will only increase q-values absolutely.
	 * <br/>
	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
	 * is set to add 10, then 10 will also be added to the other q-values.
	 */
	public void setToAddCashProportionally(boolean isToAddCashProportionally) {
		this.isToAddCashProportionally = isToAddCashProportionally;
	}

	/**
	 * @return the isToAddCashProportionally.
	 * If true, {@link #addCash(long, Date, long, float, String)} will 
	 * increase cash (min-q) proportionally to the current value of min-q.
	 * <br/>
	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
	 * is set to add 10, then the final min-q is 15 (triple of the original min-q), hence
	 * other q-values will be also multiplied by 3.
	 * <br/>
	 * If false, the values added by {@link #addCash(long, Date, long, float, String)}
	 * will only increase q-values absolutely.
	 * <br/>
	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
	 * is set to add 10, then 10 will also be added to the other q-values.
	 */
	public boolean isToAddCashProportionally() {
		return isToAddCashProportionally;
	}

	/**
	 * Assets are managed by a data structure known as the asset tables
	 * (they are clique-tables containing non-normalized float values).
	 * When an asset table is instantiated (i.e. when a new user is created,
	 * and then a new asset table is created for that user), each
	 * cell of the asset table should be filled with default (uniform) values initially.
	 * <br/><br/>
	 * Note: the assets (S-values) and q-values (values actually stored in
	 * the tables) are related with a logarithm relationship
	 * S = b*log(q). So, the values in the asset tables may not actually be
	 * the values of assets directly.
	 * @return the value to be filled into each cell of the q-tables when the tables are initialized.
	 * @see #initialize()
	 */
	public float getDefaultInitialQTableValue() {
		return defaultInitialQTableValue;
	}
	

	/**
	 * Assets are managed by a data structure known as the asset tables
	 * (they are clique-tables containing non-normalized float values).
	 * When an asset table is instantiated (i.e. when a new user is created,
	 * and then a new asset table is created for that user), each
	 * cell of the asset table should be filled with default (uniform) values initially.
	 * <br/><br/>
	 * Note: the assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q). So, the values in the asset tables may not actually be
	 * the values of assets directly.
	 * @param defaultValue : the value to be filled into each cell of the q-tables when the tables are initialized.
	 * @see #initialize()
	 */
	public void setDefaultInitialQTableValue(float defaultValue) {
		this.defaultInitialQTableValue = defaultValue;
	}
	
	/**
	 * Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q) with log being a logarithm function of some basis. 
	 * @return the base of the current logarithm function used
	 * for converting q-values to assets.
	 * @see #setCurrentLogBase(float)
	 * @see #getCurrentCurrencyConstant()
	 * @see #setCurrentCurrencyConstant(double)
	 * @see #getQValuesFromScore(float)
	 * @see #getScoreFromQValues(float)
	 */
	public float getCurrentLogBase() {
		return currentLogBase ;
	}
	
	/**
	 * Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with log being a logarithm function of some base. 
	 * @param base : the base of the current logarithm function used
	 * for converting q-values to assets.
	 * @see #getCurrentLogBase()
	 * @see #getCurrentCurrencyConstant()
	 * @see #setCurrentCurrencyConstant(float)
	 * @see #getQValuesFromScore(float)
	 * @see #getScoreFromQValues(float)
	 */
	public void setCurrentLogBase(float base) {
		this.currentLogBase = base;
	}
	
	/**
	 *  Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with b being a constant for defining the "unit
	 * of currency" (more precisely, this constant defines how sensitive
	 * is the assets).
	 * @return the current value of b, the "unit of currency""
	 * @see #getCurrentLogBase()
	 * @see #setCurrentLogBase(float)
	 * @see #setCurrentCurrencyConstant(float)
	 * @see #getQValuesFromScore(float)
	 * @see #getScoreFromQValues(float)
	 */
	public double getCurrentCurrencyConstant() {
		return currentCurrencyConstant ;
	}
	

	/**
	 *  Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with b being a constant for defining the "unit
	 * of currency" (more precisely, this constant defines how sensitive
	 * is the assets).
	 * @param b the current value of b to set, the "unit of currency""
	 * @see #getCurrentLogBase()
	 * @see #setCurrentLogBase(float)
	 * @see #getCurrentCurrencyConstant()
	 * @see #getQValuesFromScore(float)
	 * @see #getScoreFromQValues(float)
	 */
	public void setCurrentCurrencyConstant(float b) {
		this.currentCurrencyConstant = b;
	}

	/**
	 * @param probabilisticNetwork the probabilisticNetwork to set
	 */
	public void setProbabilisticNetwork(ProbabilisticNetwork probabilisticNetwork) {
		this.probabilisticNetwork = probabilisticNetwork;
	}

	/**
	 * @return the probabilisticNetwork
	 */
	public ProbabilisticNetwork getProbabilisticNetwork() {
		return probabilisticNetwork;
	}

	/**
	 * @return an instance of {@link AssetAwareInferenceAlgorithm}
	 * which is not necessary related to some user.
	 */
	public AssetAwareInferenceAlgorithm getDefaultInferenceAlgorithm() {
		return defaultInferenceAlgorithm;
	}
	
	/**
	 * @param defaultInferenceAlgorithm : an instance of {@link AssetAwareInferenceAlgorithm}
	 * which is not necessary related to some user.
	 */
	public void setDefaultInferenceAlgorithm(
			AssetAwareInferenceAlgorithm defaultInferenceAlgorithm) {
		this.defaultInferenceAlgorithm = defaultInferenceAlgorithm;
	}

	/**
	 * @param conditionalProbabilityExtractor the conditionalProbabilityExtractor to set
	 */
	public void setConditionalProbabilityExtractor(
			IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor) {
		this.conditionalProbabilityExtractor = conditionalProbabilityExtractor;
	}

	/**
	 * @return the conditionalProbabilityExtractor
	 */
	public IArbitraryConditionalProbabilityExtractor getConditionalProbabilityExtractor() {
		return conditionalProbabilityExtractor;
	}

	/**
	 * This is an index for {@link #getNetworkActionsMap()}. 
	 * The key is the question ID.
	 * By calling {@link Map#get(null)}, all actions not related to any question will be returned.
	 * @param networkActionsIndexedByQuestions the networkActionsIndexedByQuestions to set.
	 * This is a {@link HashMap}, because it is known to allow null keys
	 * (it is not guaranteed that other implementations will allow null keys).
	 * @see #addNetworkAction(long, AddQuestionNetworkAction)
	 * @see #addNetworkAction(long, NetworkAction)
	 */
	protected void setNetworkActionsIndexedByQuestions(
			HashMap<Long, List<NetworkAction>> networkActionsIndexedByQuestions) {
		this.networkActionsIndexedByQuestions = networkActionsIndexedByQuestions;
	}

	/**
	 * This is an index for {@link #getNetworkActionsMap()}. 
	 * The key is the question ID.
	 * By calling {@link Map#get(null)}, all actions not related to any question will be returned.
	 * @return the networkActionsIndexedByQuestions.
	 * This is a {@link HashMap}, because it is known to allow null keys
	 * (it is not guaranteed that other implementations will allow null keys).
	 * @see #addNetworkAction(long, AddQuestionNetworkAction)
	 * @see #addNetworkAction(long, NetworkAction)
	 */
	protected HashMap<Long, List<NetworkAction>> getNetworkActionsIndexedByQuestions() {
		return networkActionsIndexedByQuestions;
	}

	/**
	 * This method adds networkAction into {@link #getNetworkActionsIndexedByQuestions()}
	 * so that the values (lists) in {@link #getNetworkActionsIndexedByQuestions()} are ordered by
	 * {@link NetworkAction#getWhenCreated()}.
	 * @param networkAction : the object to be inserted.
	 * {@link NetworkAction#getQuestionId()} will be used as the key of {@link #getNetworkActionsIndexedByQuestions()}. 
	 * Null is allowed as the key.
	 * @return the index where networkAction was inserted
	 */
	protected int addNetworkActionIntoQuestionMap(NetworkAction networkAction) {
		// initial assertion
		if (networkAction == null) {
			throw new NullPointerException("networkAction == null");
		}
		// get list of actions from getNetworkActionsIndexedByQuestions.
		List<NetworkAction> list = null;
		synchronized (getNetworkActionsIndexedByQuestions()) {
			list = getNetworkActionsIndexedByQuestions().get(networkAction.getQuestionId());
			if (list == null) {
				// this is the fist time an action with questionId is inserted
				list = new ArrayList<NetworkAction>();
				getNetworkActionsIndexedByQuestions().put(networkAction.getQuestionId(), list);
			}
		}
		// at this point, list != null
		int indexOfNewElement = -1;	// index where networkAction is going to be added
		synchronized (list) {
			if (networkAction.getWhenCreated() == null) {
				// if there is no date, add at the end
				indexOfNewElement = list.size();	// we shall return this value
				list.add(networkAction);
				// I'd like to avoid returning the method while still inside a critical portion.
			} else {
				// NOTE: returning indexOfNewElement >= 0 indicates that the network action was added somewhere in the list
				for (indexOfNewElement = 0; indexOfNewElement < list.size(); indexOfNewElement++) {
					if ( ( list.get(indexOfNewElement) == null )
							|| ( list.get(indexOfNewElement).getWhenCreated() == null ) ) { // at this point, list.get(indexOfNewElement) != null
						// add new element always before null values or null dates
						break;
					}
					if (list.get(indexOfNewElement).getWhenCreated().after(networkAction.getWhenCreated())) {
						// new element was created before the current element in the list. Add here. 
						break;
					}
				}
				list.add(indexOfNewElement, networkAction);
			}
		}
		return indexOfNewElement;
	}

}
