/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.exception.InvalidParentException;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineImpl implements MarkovEngineInterface {
	
	private float probabilityErrorMargin = 0.0001f;

	private Map<Long, List<NetworkAction>> networkActionsMap;
	private long transactionCounter;
	private AssetAwareInferenceAlgorithm inferenceAlgorithm;

	/**
	 * 
	 */
	public MarkovEngineImpl() {
		this.initialize();
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#initialize()
	 */
	public boolean initialize() {
		// prepare map storing transaction keys
		setNetworkActionsMap(new ConcurrentHashMap<Long, List<NetworkAction>>());	// concurrent hash map is known to be thread safe yet fast.
		setTransactionCounter(0);
		
		// prepare inference algorithm for the BN
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(new ProbabilisticNetwork("DAGGRE"));
		
		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
		junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
		
		// prepare inference algorithm for asset network
		setInferenceAlgorithm((AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm));
		
		// users seem to start with 0 assets (assets are logarithmic, so 0 assets == 1 q table)
		getInferenceAlgorithm().setDefaultInitialAssetQuantity(1);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#startNetworkActions()
	 */
	public synchronized long startNetworkActions() {
		getNetworkActionsMap().put(++transactionCounter, new ArrayList<NetworkAction>());
		return transactionCounter;
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
			actions.clear();	// reset actions first
			actions.addAll(netChangeActions);	// netChangeActions comes first
			actions.add(new RebuildNetworkAction(new Date()));	// <rebuild action> is inserted between netChangeActions and otherActions
			actions.addAll(otherActions);	// otherActions comes later
			
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
		public RebuildNetworkAction(Date whenCreated) {
			this.whenCreated = whenCreated;
		}
		public void execute() {
			// TODO rebuild BN
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
		if (((ProbabilisticNetwork)getInferenceAlgorithm().getNetwork()).getNode(Long.toString(questionId)) != null) {
			// duplicate question
			throw new IllegalArgumentException("Question ID " + questionId + " is already present.");
		}
		if (initProbs != null && !initProbs.isEmpty()) {
			float sum = 0;
			for (Float prob : initProbs) {
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
			synchronized (inferenceAlgorithm.getNetwork()) {
				ProbabilisticNetwork network = (ProbabilisticNetwork) inferenceAlgorithm.getNetwork();
				network.addNode(node);
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
		public long getTransactionKey() {
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
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)
	 */
	public boolean addQuestionAssumption(long transactionKey, Date occurredWhen, long sourceQuestionId, List<Long> assumptiveQuestionIds,  List<Float> cpd) throws IllegalArgumentException {
		
		// initial assertions
		
		// check existence of child
		Node child = ((ProbabilisticNetwork)getInferenceAlgorithm().getNetwork()).getNode(Long.toString(sourceQuestionId));
		if (child == null) {
			// child node does not exist
			throw new IllegalArgumentException("Question ID " + sourceQuestionId + " does not exist.");
		}
		// check existence of parents
		int expectedSizeOfCPD = child.getStatesSize();	// this var will keep track of expected size of cpd if it is non-null && non-empty
		for (Long assumptiveQuestionId : assumptiveQuestionIds) {
			Node parent = ((ProbabilisticNetwork)getInferenceAlgorithm().getNetwork()).getNode(Long.toString(assumptiveQuestionId));
			
			if (parent == null) {
				// parent node does not exist
				throw new IllegalArgumentException("Question ID " + assumptiveQuestionId + " does not exist.");
			}
			
		}
		// check size of cpd
		if (cpd != null 
				&& !cpd.isEmpty()
				&& cpd.size() != expectedSizeOfCPD) {
			// size of cpd is inconsistent
			throw new IllegalArgumentException("Expected size of cpd is "+ expectedSizeOfCPD + ", but was " + cpd.size());
		}
		
		
		// obtain the list which stores the actions in order and check if it was initialized
		List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
		if (actions == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		// instantiate the action object for adding a question
		AddQuestionAssumptionNetworkAction newAction = new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen, sourceQuestionId, assumptiveQuestionIds, cpd);
		
		// let's add action to the managed list. Prepare index of where in actions we should add newAction
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
			
			ProbabilisticNetwork network = (ProbabilisticNetwork) inferenceAlgorithm.getNetwork();	// network containing question
			
			synchronized (network) {
				child = (ProbabilisticNode) network.getNode(Long.toString(sourceQuestionId));
				
				// if cpd is empty, then we shall substitute the old edges going to child. So, delete all of them first.
				if (cpd == null || cpd.isEmpty()) {
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
		public long getTransactionKey() {
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

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addCash(long, java.util.Date, long, float, java.lang.String)
	 */
	public boolean addCash(long transactionKey, Date occurredWhen, long userId,
			float assets, String description) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<Float> addTrade(long transactionKey, Date occurredWhen,
			long tradeId, long userId, long questionId, List<Float> oldValues,
			List<Float> newValues, List<Long> assumptionIds,
			List<Integer> assumedStates, Boolean allowNegative)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#resolveQuestion(long, java.util.Date, long, int)
	 */
	public boolean resolveQuestion(long transactionKey, Date occurredWhen,
			long questionID, int settledState) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#revertTrade(long, java.util.Date, java.lang.Long, java.lang.Long)
	 */
	public boolean revertTrade(long transactionKey, Date occurredWhen,
			Long startingTradeId, Long questionId)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbsList(java.util.List, java.util.List, java.util.List)
	 */
	public List<List<Float>> getProbsList(List<Long> questionIds,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getPossibleQuestionAssumptions(long, java.util.List)
	 */
	public List<Long> getPossibleQuestionAssumptions(long questionId,
			List<Long> assumptionIds) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getAssetsIfStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> getAssetsIfStates(long userID, long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getEditLimits(long, long, int, java.util.List, java.util.List)
	 */
	public List<Float> getEditLimits(long userId, long questionId,
			int questionState, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getCash(long, java.util.List, java.util.List)
	 */
	public float getCash(long userId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#assetsCommittedByUserQuestion(long, long, java.util.List, java.util.List)
	 */
	public float assetsCommittedByUserQuestion(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#assetsCommittedByUserQuestions(long, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> assetsCommittedByUserQuestions(long userId,
			List<Long> questionId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public float scoreUserQuestionEv(long userId, Long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUser(java.util.List, java.util.List, java.util.List)
	 */
	public float scoreUser(List<Long> userIds, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#previewTrade(long, long, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> previewTrade(long userID, long questionID,
			List<Float> oldValues, List<Float> newValues,
			List<Integer> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#determineBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	public List<Float> determineBalancingTrade(long userID, long questionID,
			List<Integer> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
	 */
	public List<QuestionEvent> getQuestionHistory(Long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreDetails(long, java.util.List, java.util.List)
	 */
	public List<ScoreDetail> getScoreDetails(long userId,
			List<Integer> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
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

}
