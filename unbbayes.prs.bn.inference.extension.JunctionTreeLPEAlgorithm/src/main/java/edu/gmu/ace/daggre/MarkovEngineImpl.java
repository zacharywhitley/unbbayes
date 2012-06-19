package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Date;
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
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
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
	private Long transactionCounter;

	private Map<Long, AssetNetwork> userToAssetNetMap;

	private boolean isToAddCashProportionally = false;

	private float defaultInitialQTableValue = 1;

	private float currentLogBase = 10;

	private double currentCurrencyConstant = 10;

	private ProbabilisticNetwork probabilisticNetwork;

	private AssetAwareInferenceAlgorithm inferenceAlgorithm;
	

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
		// prepare map storing transaction keys
		setNetworkActionsMap(new ConcurrentHashMap<Long, List<NetworkAction>>());	// concurrent hash map is known to be thread safe yet fast.
		setTransactionCounter(0);
		
		setProbabilisticNetwork(new ProbabilisticNetwork("DAGGRE"));
		
		// prepare inference algorithm for the BN
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
		
		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
		junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
		
		// prepare inference algorithm for asset network
		setInferenceAlgorithm((AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm));
		
		// usually, users seem to start with 0 assets (assets are logarithmic, so 0 assets == 1 q table), but let's use the value of getDefaultInitialQTableValue
		getInferenceAlgorithm().setDefaultInitialAssetQuantity(getDefaultInitialQTableValue());
		
		setUserToAssetNetMap(new ConcurrentHashMap<Long, AssetNetwork>()); // concurrent hash map is known to be thread safe yet fast.
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#startNetworkActions()
	 */
	public long startNetworkActions() {
		long ret = Long.MIN_VALUE;
		synchronized (transactionCounter) {
			ret = ++transactionCounter;
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
			throws IllegalArgumentException {
		
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
		public RebuildNetworkAction(long transactionKey, Date whenCreated) {
			this.transactionKey = transactionKey;
			this.whenCreated = whenCreated;
		}
		public void execute() {
			// rebuild BN
			// make sure no one is using the probabilistic network yet.
			ProbabilisticNetwork net = getProbabilisticNetwork();
			synchronized (net) {
				// reset
//					getInferenceAlgorithm().reset();
				// recompile
				if (net.getNodeCount() > 0) {
					getInferenceAlgorithm().run();
				}
			}
			// TODO rebuild all user asset nets
			// TODO redo all trades using the history
			// Note: if we are rebooting the system, the history is supposedly empty
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Cannot revert a network rebuild action.");
		}
		public Date getWhenCreated() {
			return whenCreated;
		}
		/** This action reboots the network, but does not change the structure by itself */
		public boolean isStructureChangeAction() {
			return false;
		}
		public Long getTransactionKey() {
			return transactionKey;
		}
		
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
		if (getProbabilisticNetwork().getNode(Long.toString(questionId)) != null) {
			// duplicate question
			throw new IllegalArgumentException("Question ID " + questionId + " is already present.");
		}
		if (initProbs != null && !initProbs.isEmpty()) {
			float sum = 0;
			for (Float prob : initProbs) {
				if (prob < 0) {
					throw new IllegalArgumentException("Negative probability declaration found: " + prob);
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
		
		// obtain the list which stores the actions in order and check if it was initialized
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		// instantiate the action object for adding a question
		AddQuestionNetworkAction newAction = new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs);
		
		// let's add action to the managed list. Prepare index of where in actions we should add newAction
		int indexOfFirstActionCreatedAfterNewAction = -1;	// this will point to the first action created after occurredWhen
		
		// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
		for (int i = 0; i < actions.size(); i++) {
			NetworkAction action = actions.get(i);
			if (action instanceof AddQuestionNetworkAction) {
				AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) action;
				if (addQuestionNetworkAction.getQuestionId() == questionId) {
					// duplicate question in the same transaction
					throw new IllegalArgumentException("Question ID " + questionId + " is already present.");
				}
			}
			if (indexOfFirstActionCreatedAfterNewAction < 0 && action.getWhenCreated().after(occurredWhen)) {
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

		/**
		 * Constructor initializing fields
		 */
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

		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.daggre.NetworkAction#revert()
		 */
		public void revert() throws UnsupportedOperationException {
			throw new javax.help.UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
		}

		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.daggre.NetworkAction#getWhenCreated()
		 */
		public Date getWhenCreated() {
			return this.occurredWhen;
		}

		/**
		 * @return the transactionKey
		 */
		public Long getTransactionKey() {
			return transactionKey;
		}

		/**
		 * @return the questionId
		 */
		public long getQuestionId() {
			return questionId;
		}

		/**
		 * @return the numberStates
		 */
		public int getNumberStates() {
			return numberStates;
		}

		/**
		 * @return the initProbs
		 */
		public List<Float> getInitProbs() {
			return initProbs;
		}
		/** Adding a new node is a structure change */
		public boolean isStructureChangeAction() {
			return true;
		}



		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString() + "{" + this.transactionKey + ", " + this.getQuestionId() + "}";
		}
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)
	 */
	public boolean addQuestionAssumption(long transactionKey, Date occurredWhen, long sourceQuestionId, List<Long> assumptiveQuestionIds,  List<Float> cpd) throws IllegalArgumentException {
		
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
		Node child = getProbabilisticNetwork().getNode(Long.toString(sourceQuestionId));
		if (child == null) {
			// child node does not exist. Check if there was some previous transaction adding such node
			synchronized (actions) {
				for (NetworkAction networkAction : actions) {
					if (networkAction instanceof AddQuestionNetworkAction) {
						AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
						if (addQuestionNetworkAction.getQuestionId() == sourceQuestionId) {
							childNodeStateSize = addQuestionNetworkAction.getNumberStates();
							break;
						}
					}
				}
			}
			if (childNodeStateSize < 0) {	
				// if negative, then expectedSizeOfCPD was not updated, so sourceQuestionId was not found in last loop
				throw new IllegalArgumentException("Question ID " + sourceQuestionId + " does not exist.");
			}
		} else {
			// initialize the value of expectedSizeOfCPD using the number of states of future owner of the cpd
			childNodeStateSize = child.getStatesSize();
		}
		
		// this var will store the correct size of cpd. If negative, owner of the cpd was not found.
		int expectedSizeOfCPD = childNodeStateSize;
		
		// do not allow null values for collections
		if (assumptiveQuestionIds == null) {
			assumptiveQuestionIds = new ArrayList<Long>();
		}
		
		// check existence of parents
		for (Long assumptiveQuestionId : assumptiveQuestionIds) {
			Node parent = getProbabilisticNetwork().getNode(Long.toString(assumptiveQuestionId));
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
				throw new IllegalArgumentException("Expected size of cpd of question " + sourceQuestionId + " is "+ expectedSizeOfCPD + ", but was " + cpd.size());
			}
			// check value consistency
			float sum = 0;
			int counter = 0;	// counter in which possible values are mod childNodeStateSize
			for (Float probability : cpd) {
				if (probability < 0) {
					throw new IllegalArgumentException("Negative probability declaration found: " + probability);
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
		AddQuestionAssumptionNetworkAction newAction = new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen, sourceQuestionId, assumptiveQuestionIds, cpd);
		
		// let's add action to the managed list. 
		synchronized (actions) {
			// Prepare index of where in actions we should add newAction
			int indexOfFirstActionCreatedAfterNewAction = 0;	// this will point to the first action created after occurredWhen
			// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
			for (; indexOfFirstActionCreatedAfterNewAction < actions.size(); indexOfFirstActionCreatedAfterNewAction++) {
				if (actions.get(indexOfFirstActionCreatedAfterNewAction).getWhenCreated().after(occurredWhen)) {
					break;
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
		}
		
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

		/**
		 * Constructor initializing fields
		 */
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

		/**
		 * Adds a new question into the current network
		 */
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

		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.daggre.NetworkAction#revert()
		 */
		public void revert() throws UnsupportedOperationException {
			throw new javax.help.UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
		}

		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.daggre.NetworkAction#getWhenCreated()
		 */
		public Date getWhenCreated() {
			return this.occurredWhen;
		}

		/**
		 * @return the transactionKey
		 */
		public Long getTransactionKey() {
			return transactionKey;
		}

		/**
		 * @return the sourceQuestionId
		 */
		public long getSourceQuestionId() {
			return sourceQuestionId;
		}

		/**
		 * @return the assumptiveQuestionIds
		 */
		public List<Long> getAssumptiveQuestionIds() {
			return assumptiveQuestionIds;
		}

		/**
		 * @return the cpd
		 */
		public List<Float> getCpd() {
			return cpd;
		}
		
		/** Adding a new edge is a structure change */
		public boolean isStructureChangeAction() {
			return true;
		}
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
		public AddCashNetworkAction (long transactionKey, Date occurredWhen, long userId, float assets, String description) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.userId = userId;
			this.assets = assets;
			this.description = description;
		}
		public void execute() {
			// extract user's asset net
			AssetNetwork assetNet = null;
			try {
				assetNet = getAssetNetFromUserID(userId);
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not create asset tables for user " + userId, e);
			}
			
			synchronized (assetNet) {
				if (isToAddCashProportionally()) {
					/*
					 * Calculate ratio of change (newCash/oldCash) and apply same ratio to all cells.
					 */
					// calculate old cash (i.e. old minQ) value
					float minQ = Float.NaN;
					AssetAwareInferenceAlgorithm inferenceAlgorithm = getInferenceAlgorithm();
					inferenceAlgorithm.setAssetNetwork(assetNet);
					inferenceAlgorithm.runMinPropagation();
					minQ = inferenceAlgorithm.calculateExplanation(new ArrayList<Map<INode,Integer>>());
					inferenceAlgorithm.undoMinPropagation();
					if (!Float.isNaN(minQ)) {
						// calculate ratio
						float ratio = (minQ + getQValuesFromScore(assets)) / minQ;
						// multiply ratio to all cells of asset tables of cliques
						for (Clique clique : assetNet.getJunctionTree().getCliques()) {
							PotentialTable assetTable = clique.getProbabilityFunction();
							for (int i = 0; i < assetTable.tableSize(); i++) {
								assetTable.setValue(i, assetTable.getValue(i) * ratio);
							}
						}
						// mult ratio to all cells in asset tables of separators
						for (Separator separator : assetNet.getJunctionTree().getSeparators()) {
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
					for (Clique clique : assetNet.getJunctionTree().getCliques()) {
						PotentialTable assetTable = clique.getProbabilityFunction();
						for (int i = 0; i < assetTable.tableSize(); i++) {
							assetTable.setValue(i, assetTable.getValue(i) + qValue);
						}
					}
					// add assets to all cells in asset tables of separators
					for (Separator separator : assetNet.getJunctionTree().getSeparators()) {
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
		public Date getWhenCreated() {
			return occurredWhen;
		}
		public boolean isStructureChangeAction() {
			return false;	// this operation does not change network structure
		}
		/**
		 * @return the transactionKey
		 */
		public Long getTransactionKey() {
			return transactionKey;
		}
		/**
		 * @return the assets
		 */
		protected float getAssets() {
			return assets;
		}
		/**
		 * @param assets the assets to set
		 */
		protected void setAssets(float assets) {
			this.assets = assets;
		}
		/**
		 * @return the userId
		 */
		protected long getUserId() {
			return userId;
		}
		/**
		 * @return the description
		 */
		protected String getDescription() {
			return description;
		}
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
		
		// initial assertions
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		
		// instantiate the action object for adding cash
		AddCashNetworkAction newAction = new AddCashNetworkAction(transactionKey, occurredWhen, userId, assets, description);
		
		// let's add action to the managed list. 
		synchronized (actions) {
			// Prepare index of where in actions we should add newAction
			int indexOfFirstActionCreatedAfterNewAction = 0;	// this will point to the first action created after occurredWhen
			// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
			for (; indexOfFirstActionCreatedAfterNewAction < actions.size(); indexOfFirstActionCreatedAfterNewAction++) {
				if (actions.get(indexOfFirstActionCreatedAfterNewAction).getWhenCreated().after(occurredWhen)) {
					break;
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
		}
		
		return true;
	}

	
	public class AddTradeNetworkAction implements NetworkAction {

		private final Date whenCreated;
		private final long transactionKey;
		private final String tradeKey;
		private final long userId;
		private final long questionId;
		private final List<Float> newValues;
		private final List<Long> assumptionIds;
		private final List<Integer> assumedStates;
		private final boolean allowNegative;

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
			// TODO Auto-generated method stub
			
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Reverting a trade is not supported yet.");
		}

		public Date getWhenCreated() {
			return whenCreated;
		}
		public boolean isStructureChangeAction() {
			return false;
		}
		public Long getTransactionKey() {
			return transactionKey;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<Float> addTrade(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) throws IllegalArgumentException{
		
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
		
		// returned value is the same of preview trade
		List<Float> ret = this.previewTrade(userId, questionId, newValues, assumptionIds, assumedStates);
		
		// instantiate the action object for adding cash
		AddTradeNetworkAction newAction = new AddTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues, assumptionIds, assumedStates, allowNegative);
		
		// let's add action to the managed list. 
		synchronized (actions) {
			// Prepare index of where in actions we should add newAction
			int indexOfFirstActionCreatedAfterNewAction = 0;	// this will point to the first action created after occurredWhen
			// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
			for (; indexOfFirstActionCreatedAfterNewAction < actions.size(); indexOfFirstActionCreatedAfterNewAction++) {
				if (actions.get(indexOfFirstActionCreatedAfterNewAction).getWhenCreated().after(occurredWhen)) {
					break;
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
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#resolveQuestion(long, java.util.Date, long, int)
	 */
	public boolean resolveQuestion(long transactionKey, Date occurredWhen,
			long questionID, int settledState) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
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
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = null;	
		
		if (getInferenceAlgorithm().getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm jtAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm().getProbabilityPropagationDelegator();
			if (jtAlgorithm.getLikelihoodExtractor() instanceof JeffreyRuleLikelihoodExtractor) {
				conditionalProbabilityExtractor = ((JeffreyRuleLikelihoodExtractor) jtAlgorithm.getLikelihoodExtractor()).getConditionalProbabilityExtractor();
			}
		}
		if (conditionalProbabilityExtractor == null) {
			// instantiate new extractor if nothing was found
			Debug.println(getClass(), "Could not extract conditionalProbabilityExtractor from inference algorithm. Instantiating new.");
			conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();
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
				if (multidimensionalCoord.length != assumedStates.size()) {
					throw new RuntimeException("Multi dimensional coordinate of index " + i + " has size " + multidimensionalCoord.length
							+ ". Expected " + assumedStates.size());
				}
				for (int j = 0; j < multidimensionalCoord.length; j++) {
					if ((assumedStates.get(j) != null)
							&& (assumedStates.get(j) != multidimensionalCoord[j])) {
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
		IArbitraryConditionalProbabilityExtractor extractor = null;	
		
		// use the same extractor as used by the junction tree algorithm
		if (getInferenceAlgorithm().getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm jtAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm().getProbabilityPropagationDelegator();
			if (jtAlgorithm.getLikelihoodExtractor() instanceof JeffreyRuleLikelihoodExtractor) {
				extractor = ((JeffreyRuleLikelihoodExtractor) jtAlgorithm.getLikelihoodExtractor()).getConditionalProbabilityExtractor();
			}
		}
		if (extractor == null) {
			throw new RuntimeException("Could not extract IArbitraryConditionalProbabilityExtractor from inference algorithm. Maybe you are using an incompatible version of Markov Engine or UnBBayes");
		}
		
		// extract main node and assumption nodes
		Node mainNode = null;
		List<INode> assumptions = new ArrayList<INode>();
		synchronized (getProbabilisticNetwork()) {
			mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			throw new IllegalArgumentException("Question " + questionId + " not found.");
		}
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
		
		// obtain possible condition nodes
		List<INode> returnedNodes = null;
		synchronized (getProbabilisticNetwork()) {
			returnedNodes = extractor.getValidConditionNodes(mainNode, assumptions, getProbabilisticNetwork(), null);
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

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getAssetsIfStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> getAssetsIfStates(long userID, long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub

		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getEditLimits(long, long, int, java.util.List, java.util.List)
	 */
	public List<Float> getEditLimits(long userId, long questionId,
			int questionState, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub

		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getCash(long, java.util.List, java.util.List)
	 */
	public float getCash(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		AssetNetwork assetNet;
		try {
			assetNet = this.getAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract asset tables of user " + userId, e);
		}
		
		float ret = Float.NEGATIVE_INFINITY;	// value to return
		synchronized (assetNet) {
			// set up findings
			if (assumptionIds != null) {
				for (int i = 0; i < assumptionIds.size(); i++) {
					AssetNode node = (AssetNode) assetNet.getNode(Long.toString(assumptionIds.get(i)));
					if (node == null) {
						throw new IllegalArgumentException("Question " + assumptionIds.get(i) + " does not exist.");
					}
					Integer stateIndex = assumedStates.get(i);
					if (stateIndex != null) {
						node.addFinding(stateIndex);
					}
				}
			}
			synchronized (getInferenceAlgorithm()) {
				getInferenceAlgorithm().setAssetNetwork(assetNet);
				// run only min-propagation (i.e. calculate min-q given assumptions)
				getInferenceAlgorithm().runMinPropagation();
				// obtain min-q value and explanation (states which cause the min-q values)
				ArrayList<Map<INode, Integer>> statesWithMinQAssets = new ArrayList<Map<INode,Integer>>();	// this is the min-q explanation (states which cause Min-q)
				ret = getInferenceAlgorithm().calculateExplanation(statesWithMinQAssets);	// statesWithMinQAssets will be filled by this method
				// undo min-propagation, because the next iteration of asset updates should be based on non-min assets
				getInferenceAlgorithm().undoMinPropagation();	
				// TODO use the values of statesWithMinQAssets for something (currently, this is ignored)
			}
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
		
//		int childNodeStateSize = -1;	// this var stores the quantity of states of the node identified by questionId.
//		
//		// check existence of child
//		Node child = getProbabilisticNetwork().getNode(Long.toString(questionId));
//		if (child == null) {
//			// child node does not exist. Check if there was some previous transaction adding such node
//			synchronized (actions) {
//				for (NetworkAction networkAction : actions) {
//					if (networkAction instanceof AddQuestionNetworkAction) {
//						AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
//						if (addQuestionNetworkAction.getQuestionId() == sourceQuestionId) {
//							childNodeStateSize = addQuestionNetworkAction.getNumberStates();
//							break;
//						}
//					}
//				}
//			}
//			if (childNodeStateSize < 0) {	
//				// if negative, then expectedSizeOfCPD was not updated, so sourceQuestionId was not found in last loop
//				throw new IllegalArgumentException("Question ID " + sourceQuestionId + " does not exist.");
//			}
//		} else {
//			// initialize the value of expectedSizeOfCPD using the number of states of future owner of the cpd
//			childNodeStateSize = child.getStatesSize();
//		}
//		
//		// this var will store the correct size of cpd. If negative, owner of the cpd was not found.
//		int expectedSizeOfCPD = childNodeStateSize;
//		
//		// do not allow null values for collections
//		if (assumptiveQuestionIds == null) {
//			assumptiveQuestionIds = new ArrayList<Long>();
//		}
//		
//		// check existence of parents
//		for (Long assumptiveQuestionId : assumptiveQuestionIds) {
//			Node parent = getProbabilisticNetwork().getNode(Long.toString(assumptiveQuestionId));
//			if (parent == null) {
//				// parent node does not exist. Check if there was some previous transaction adding such node
//				synchronized (actions) {
//					boolean hasFound = false;
//					for (NetworkAction networkAction : actions) {
//						if (networkAction instanceof AddQuestionNetworkAction) {
//							AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
//							if (addQuestionNetworkAction.getQuestionId() == assumptiveQuestionId) {
//								// size of cpd = MULT (<quantity of states of child and parents>).
//								expectedSizeOfCPD *= addQuestionNetworkAction.getNumberStates();
//								hasFound = true;
//								break;
//							}
//						}
//					}
//					if (!hasFound) {	
//						// parent was not found
//						throw new IllegalArgumentException("Question ID " + assumptiveQuestionId + " does not exist.");
//					}
//				}
//			} else{
//				// size of cpd = MULT (<quantity of states of child and parents>).
//				expectedSizeOfCPD *= parent.getStatesSize();
//			}
//			
//		}
//		
//		// check consistency of size of cpd
//		if (cpd != null && !cpd.isEmpty()){
//			if (cpd.size() != expectedSizeOfCPD) {
//				// size of cpd is inconsistent
//				throw new IllegalArgumentException("Expected size of cpd of question " + sourceQuestionId + " is "+ expectedSizeOfCPD + ", but was " + cpd.size());
//			}
//			// check value consistency
//			float sum = 0;
//			int counter = 0;	// counter in which possible values are mod childNodeStateSize
//			for (Float probability : cpd) {
//				if (probability < 0) {
//					throw new IllegalArgumentException("Negative probability declaration found: " + probability);
//				}
//				sum += probability;
//				counter++;
//				if (counter >= childNodeStateSize) {
//					// check if sum of conditional probability given current state of parents is 1
//					if (!(((1 - getProbabilityErrorMargin()) < sum) && (sum < (1 + getProbabilityErrorMargin())))) {
//						throw new IllegalArgumentException("Inconsistent prior probability: " + sum);
//					}
//					counter = 0;
//					sum = 0;
//				}
//			}
//		}
		
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#determineBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	public List<Float> determineBalancingTrade(long userID, long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
	 */
	public List<QuestionEvent> getQuestionHistory(Long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
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
	public void setNetworkActionsMap(Map<Long, List<NetworkAction>> networkActionsMap) {
		this.networkActionsMap = networkActionsMap;
	}

	/**
	 * This map stores the transactionKeys and all actions
	 * to be executed by the transaction.
	 * @return the networkActionsMap
	 * @see NetworkAction
	 */
	public Map<Long, List<NetworkAction>> getNetworkActionsMap() {
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
	 * @param inferenceAlgorithm the inferenceAlgorithm to set
	 */
	public void setInferenceAlgorithm(AssetAwareInferenceAlgorithm inferenceAlgorithm) {
		this.inferenceAlgorithm = inferenceAlgorithm;
	}

	/**
	 * @return the inferenceAlgorithm
	 */
	public AssetAwareInferenceAlgorithm getInferenceAlgorithm() {
//		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
//		
//		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
//		junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
//		
//		// prepare inference algorithm for asset network
//		AssetAwareInferenceAlgorithm inferenceAlgorithm = (AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm);
//		
//		// usually, users seem to start with 0 assets (assets are logarithmic, so 0 assets == 1 q table), but let's use the value of getDefaultInitialQTableValue
//		inferenceAlgorithm.setDefaultInitialAssetQuantity(getDefaultInitialQTableValue());
		
		return inferenceAlgorithm;
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
	 * Mapping from user ID to {@link AssetNetwork} 
	 * (class representing user and asset q tables)
	 * @return the userToAssetNetMap
	 */
	protected Map<Long, AssetNetwork> getUserToAssetNetMap() {
		return userToAssetNetMap;
	}

	/**
	 * Mapping from user ID to {@link AssetNetwork} 
	 * (class representing user and asset q tables)
	 * @param userToAssetNetMap the userToAssetNetMap to set
	 */
	protected void setUserToAssetNetMap(Map<Long, AssetNetwork> userToAssetNetMap) {
		this.userToAssetNetMap = userToAssetNetMap;
	}
	
	/**
	 * Obtains the asset network (structure representing the user and 
	 * the asset q values).
	 * @param userID
	 * @return instance of AssetNetwork
	 * @throws InvalidParentException 
	 * @see AssetAwareInferenceAlgorithm
	 */
	protected AssetNetwork getAssetNetFromUserID(long userID) throws InvalidParentException {
		AssetNetwork assetNet = null;
		synchronized (getUserToAssetNetMap()) {
			assetNet = getUserToAssetNetMap().get(userID);
			if (assetNet == null) {
				// first time user is referenced. Generate new asset net
				synchronized (getProbabilisticNetwork()) {
					// lock access to network
					assetNet = getInferenceAlgorithm().createAssetNetFromProbabilisticNet(getProbabilisticNetwork());
				}
				assetNet.setName(Long.toString(userID));
				getUserToAssetNetMap().put(userID, assetNet);
			}
		}
		return assetNet;
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

}
