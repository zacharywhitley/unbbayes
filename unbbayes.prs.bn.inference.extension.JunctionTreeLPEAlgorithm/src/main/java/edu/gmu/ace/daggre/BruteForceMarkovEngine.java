/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm.JointPotentialTable;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class BruteForceMarkovEngine extends MarkovEngineImpl {

	protected BruteForceMarkovEngine() {
		super();
		this.setDefaultInitialAssetTableValue(1);	// brute force is still using q-values
		this.setToDeleteResolvedNode(false);
		this.setToObtainProbabilityOfResolvedQuestions(true);
		this.setAssetAwareInferenceAlgorithmBuilder(new IAssetAwareInferenceAlgorithmBuilder() {
			public AssetAwareInferenceAlgorithm build(IInferenceAlgorithm probDelegator, float initQValues) {
				return (AssetAwareInferenceAlgorithm) BruteForceAssetAwareInferenceAlgorithm.getInstance(probDelegator, initQValues);
			}
		});
		this.setToDoFullPreview(true);
		this.initialize();
		this.setToDoFullPreview(true);
		this.setToIntegrateConsecutiveResolutions(false);
	}
	
	/**
	 * Default constructor method
	 * @return
	 */
	public static BruteForceMarkovEngine getInstance() {
		BruteForceMarkovEngine ret = new BruteForceMarkovEngine();
		return ret;
	}

	/**
	 * Default constructor method initializing fields.
	 * @param logBase : see {@link #setCurrentLogBase(float)}
	 * @param currencyConstant : see {@link #setCurrentCurrencyConstant(float)}
	 * @param initialUserAssets : see {@link #setDefaultInitialAssetTableValue(float)}. Although
	 * {@link #setDefaultInitialAssetTableValue(float)} expects a q-value, this argument
	 * accepts the asset values. See {@link #getQValuesFromScore(float)} and
	 * {@link #getScoreFromQValues(float)} to convert assets to q-values and
	 * q-values to assets respectively.
	 * @return new instance of some class implementing {@link MarkovEngineInterface}
	 */
	public static BruteForceMarkovEngine getInstance(float logBase, float currencyConstant, float initialUserAssets) {
		return BruteForceMarkovEngine.getInstance(logBase, currencyConstant, initialUserAssets, false);
	}
	
	/**
	 * Default constructor method initializing fields.
	 * @param logBase : see {@link #setCurrentLogBase(float)}
	 * @param currencyConstant : see {@link #setCurrentCurrencyConstant(float)}
	 * @param initialUserAssets : see {@link #setDefaultInitialAssetTableValue(float)}. Although
	 * {@link #setDefaultInitialAssetTableValue(float)} expects a q-value, this argument
	 * accepts the asset values. See {@link #getQValuesFromScore(float)} and
	 * {@link #getScoreFromQValues(float)} to convert assets to q-values and
	 * q-values to assets respectively.
	 * @param
	 * @return new instance of some class implementing {@link MarkovEngineInterface}
	 */
	public static BruteForceMarkovEngine getInstance(float logBase, float currencyConstant, float initialUserAssets, boolean isToThrowExceptionOnInvalidAssumptions) {
		BruteForceMarkovEngine ret = (BruteForceMarkovEngine) BruteForceMarkovEngine.getInstance();
		ret.setCurrentCurrencyConstant(currencyConstant);
		ret.setCurrentLogBase(logBase);
		ret.setDefaultInitialAssetTableValue((float) ret.getQValuesFromScore(initialUserAssets));
		ret.setToThrowExceptionOnInvalidAssumptions(isToThrowExceptionOnInvalidAssumptions);
		ret.setToUseQValues(true);
		ret.setToDoFullPreview(true);
		ret.setToIntegrateConsecutiveResolutions(false);
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getProbLists(java.util.List, java.util.List, java.util.List, boolean)
	 */
	public Map<Long,List<Float>> getProbLists(List<Long> questionIds, List<Long>assumptionIds, List<Integer> assumedStates, boolean isToNormalize) throws IllegalArgumentException {
		if (!isToNormalize) {
			throw new UnsupportedOperationException("Only normalized operation supported.");
		}
		
		
		// initial assertion: check consistency of assumptionIds and assumedStates
		if (assumptionIds != null && assumedStates != null) {
			if (assumedStates.size() != assumptionIds.size()) {
				throw new IllegalArgumentException("assumptionIds.size() == " + assumptionIds.size() + ", assumedStates.size() == " + assumedStates.size());
			}
		}
		
		// this is the object to be returned
		Map<Long,List<Float>> ret = new HashMap<Long, List<Float>>();
		
		BruteForceAssetAwareInferenceAlgorithm algorithm = (BruteForceAssetAwareInferenceAlgorithm) getDefaultInferenceAlgorithm();
		synchronized (algorithm) {
			synchronized (algorithm.getRelatedProbabilisticNetwork()) {
				if (assumptionIds != null) {
					for (int i = 0; i < assumptionIds.size(); i++) {
						TreeVariable node = (TreeVariable) algorithm.getRelatedProbabilisticNetwork().getNode(String.valueOf(assumptionIds.get(i)));
						node.addFinding(assumedStates.get(i));
					}
					algorithm.propagate();
				}
				if (questionIds != null) {
					for (Long id : questionIds) {
						TreeVariable node = (TreeVariable) algorithm.getRelatedProbabilisticNetwork().getNode(String.valueOf(id));
						List<Float> value = new ArrayList<Float>(node.getStatesSize());
						for (int i = 0; i < node.getStatesSize(); i++) {
							value.add(node.getMarginalAt(i));
						}
						ret.put(id, value);
					}
				} else {
					for (Node node : algorithm.getRelatedProbabilisticNetwork().getNodes()) {
						List<Float> value = new ArrayList<Float>(node.getStatesSize());
						for (int i = 0; i < node.getStatesSize(); i++) {
							value.add(((TreeVariable)node).getMarginalAt(i));
						}
						ret.put(Long.parseLong(node.getName()), value);
					}
				}
				if (assumptionIds != null) {
					for (int i = 0; i < assumptionIds.size(); i++) {
						TreeVariable node = (TreeVariable) algorithm.getRelatedProbabilisticNetwork().getNode(String.valueOf(assumptionIds.get(i)));
						node.resetEvidence();
					}
					algorithm.revertLastProbabilityUpdate();
				}
			}
		}
		
		return ret;
		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getAssetsIfStates(long, java.util.List, java.util.List, unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm, boolean)
	 */
	protected List getAssetsIfStates(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, AssetAwareInferenceAlgorithm alg, boolean isToReturnQValuesInsteadOfAssets)
			throws IllegalArgumentException {
		
		if (assumptionIds != null && assumedStates != null && assumptionIds.size() != assumedStates.size()) {
			throw new IllegalArgumentException("This implementation requires assumptionIds and assumedStates to have same size.");
		}
		
		BruteForceAssetAwareInferenceAlgorithm algorithm = (BruteForceAssetAwareInferenceAlgorithm) alg;
		// basic assertion
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm cannot be null");
		}
		
		List ret = new ArrayList();
		synchronized (algorithm.getAssetNetwork()) {
			AssetNode mainNode = (AssetNode) algorithm.getAssetNetwork().getNode(Long.toString(questionId));
			if (mainNode == null) {
				throw new InexistingQuestionException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork(), questionId);
			}
			// convert assumptionIds and assumedStates to a map representing the conditions
			Map<INode,Integer> conditions = new HashMap<INode,Integer>();
			if (assumptionIds != null) {
				for (int i = 0; i < assumptionIds.size(); i++) {
					INode node = algorithm.getAssetNetwork().getNode(Long.toString(assumptionIds.get(i)));
					if (node == null) {
						throw new InexistingQuestionException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork(), questionId);
					}
					if (node.equals(mainNode)) {
						throw new IllegalArgumentException("Question" + node + " cannot be assumption of itself.");
					}
					if (assumedStates != null && assumedStates.get(i)!= null) {
						conditions.put(node, assumedStates.get(i));
					}
				}
			}
			
			for (int state = 0; state < mainNode.getStatesSize(); state++) {
				conditions.put(mainNode, state);
				// filter by conditions
				JointPotentialTable jointQTable = algorithm.getJointQTable();
				float min = Float.MAX_VALUE;
				for (int i = 0; i < jointQTable.tableSize(); i++) {
					int[] coord = jointQTable.getMultidimensionalCoord(i);
					boolean matches = true;
					for (INode conditionNode : conditions.keySet()) {
						if (coord[jointQTable.getVariableIndex((Node)conditionNode)] != conditions.get(conditionNode)) {
							matches = false;
							break;
						}
					}
					if (matches) {
						if (jointQTable.getValue(i) < min && jointQTable.getValue(i) > 0) {
							min = jointQTable.getValue(i);
						}
					}
				}
				ret.add(getScoreFromQValues(min));	
			}
			
		}
		
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#addNetworkAction(long, edu.gmu.ace.daggre.NetworkAction)
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
			
			// insert new action at the end
			actions.add(newAction);
			
			// insert new action into the map to be used for searching actions by question id
			this.addNetworkActionIntoQuestionMap(newAction, null);
		}
		
	}
	
	/**
	 * This is an extension of {@link ProbabilityAndAssetTablesMemento}
	 * which also stores current joint tables.
	 * @author Shou Matsumoto
	 * @see ProbabilityAndAssetTablesMemento
	 */
	public class JointProbabilityAndAssetTablesMemento extends ProbabilityAndAssetTablesMemento {

		protected JointPotentialTable jointProbTable;
		protected Map<BruteForceAssetAwareInferenceAlgorithm, JointPotentialTable> algorithmToJointAssetMap;
		/**
		 * Stores probability tables of cliques/separators of {@link MarkovEngineImpl#getProbabilisticNetwork()},
		 * asset tables of {@link MarkovEngineImpl#getUserToAssetAwareAlgorithmMap()},
		 * joint probability table, and joint asset tables.
		 */
		public JointProbabilityAndAssetTablesMemento() {
			super();
			synchronized (getDefaultInferenceAlgorithm()) {
				jointProbTable = (JointPotentialTable) ((BruteForceAssetAwareInferenceAlgorithm)getDefaultInferenceAlgorithm()).getJointProbabilityTable().clone();
			}
			algorithmToJointAssetMap = new HashMap<BruteForceAssetAwareInferenceAlgorithm, BruteForceAssetAwareInferenceAlgorithm.JointPotentialTable>();
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				for (AssetAwareInferenceAlgorithm algorithm : getUserToAssetAwareAlgorithmMap().values()) {
					algorithmToJointAssetMap.put(((BruteForceAssetAwareInferenceAlgorithm)algorithm), (JointPotentialTable) ((BruteForceAssetAwareInferenceAlgorithm)algorithm).getJointQTable().clone());
				}
			}
		}
		/**
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.ProbabilityAndAssetTablesMemento#restore()
		 */
		protected void restore() {
			super.restore();
			synchronized (getDefaultInferenceAlgorithm()) {
				((BruteForceAssetAwareInferenceAlgorithm)getDefaultInferenceAlgorithm()).getJointProbabilityTable().setValues(jointProbTable.getValues());
				((BruteForceAssetAwareInferenceAlgorithm)getDefaultInferenceAlgorithm()).updateMarginalsFromJointProbability();
			}
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				for (AssetAwareInferenceAlgorithm algorithm : getUserToAssetAwareAlgorithmMap().values()) {
					((BruteForceAssetAwareInferenceAlgorithm)algorithm).getJointQTable().setValues(algorithmToJointAssetMap.get(algorithm).getValues());
				}
			}
		}
		/* (non-Javadoc)
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.ProbabilityAndAssetTablesMemento#getProbTableMap()
		 */
		@Override
		public Map<IRandomVariable, PotentialTable> getProbTableMap() {
			Map<IRandomVariable, PotentialTable> ret = new HashMap<IRandomVariable, PotentialTable>();
			for (IRandomVariable key : probTableMap.keySet()) {
				ret.put(key, jointProbTable);
			}
			return ret;
		}
		/* (non-Javadoc)
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.ProbabilityAndAssetTablesMemento#getAssetTableMap()
		 */
		@Override
		public Map<AssetAwareInferenceAlgorithm, Map<IRandomVariable, PotentialTable>> getAssetTableMap() {
			Map<AssetAwareInferenceAlgorithm, Map<IRandomVariable, PotentialTable>> ret = new HashMap<AssetAwareInferenceAlgorithm, Map<IRandomVariable,PotentialTable>>();
			for (AssetAwareInferenceAlgorithm algorithm : assetTableMap.keySet()) {
				Map<IRandomVariable, PotentialTable> map = new HashMap<IRandomVariable, PotentialTable>();
				for (IRandomVariable key : assetTableMap.get(algorithm).keySet()) {
					map.put(key, algorithmToJointAssetMap.get(algorithm));
				}
				ret.put(algorithm, map);
			}
			return super.getAssetTableMap();
		}
		
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getMemento()
	 */
	@Override
	public ProbabilityAndAssetTablesMemento getMemento() {
		return new JointProbabilityAndAssetTablesMemento();
	}

	public class BruteForceRevertTradeNetworkAction extends RevertTradeNetworkAction{

		public BruteForceRevertTradeNetworkAction(Long transactionKey,
				Date whenCreated, Date tradesStartingWhen, Long questionId) {
			super(transactionKey, whenCreated, tradesStartingWhen, questionId);
		}

		/* (non-Javadoc)
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.RebuildNetworkAction#execute()
		 */
		@Override
		public void execute() {
			this.execute(getExecutedActions());
		}

		/* (non-Javadoc)
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.RebuildNetworkAction#isToExecuteAction(edu.gmu.ace.daggre.NetworkAction)
		 */
		protected boolean isToExecuteAction(NetworkAction action) {
			return !action.isStructureChangeAction();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#revertTrade(java.lang.Long, java.util.Date, java.util.Date, java.lang.Long)
	 */
	public boolean revertTrade(Long transactionKey, Date occurredWhen,  Date tradesStartingWhen, Long questionId) throws IllegalArgumentException {
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Value of \"occurredWhen\" should not be null.");
		}
		if (tradesStartingWhen == null) {
			throw new IllegalArgumentException("Value of \"tradesStartingWhen\" should not be null.");
		}
		
		synchronized (getExecutedActions()) {
			if (getExecutedActions().isEmpty()) {
				return false;
			}
		}
		
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			// add action into transaction
			this.addNetworkAction(transactionKey, new BruteForceRevertTradeNetworkAction(transactionKey, occurredWhen, tradesStartingWhen, questionId));
			this.commitNetworkActions(transactionKey);
		} else {
			// add action into transaction
			this.addNetworkAction(transactionKey, new BruteForceRevertTradeNetworkAction(transactionKey, occurredWhen, tradesStartingWhen, questionId));
		}
		
		return true;
	}
	
	public class BruteForceResolveQuestionNetworkAction extends ResolveQuestionNetworkAction {
		private ArrayList<Float> marginalWhenResolved;
		/** Default constructor initializing fields */
		public BruteForceResolveQuestionNetworkAction (Long transactionKey, Date occurredWhen, long questionId, int settledState) {
			super(transactionKey, occurredWhen, questionId, settledState);
		}
		public void execute() {
			synchronized (getProbabilisticNetwork()) {
				TreeVariable probNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(getQuestionId()));
				if (probNode != null) {	
					// if probNode.hasEvidence(), then it was resolved already
					if (probNode.hasEvidence() && probNode.getEvidence() != getSettledState()) {
						throw new RuntimeException("Attempted to resolve question " + getQuestionId() + " to state " + getSettledState() + ", but it was already resolved to " + probNode.getEvidence());
					}
					// do nothing if node is already settled at settledState
					if (probNode.getEvidence() != getSettledState()) {
						
						// extract marginal 
						marginalWhenResolved = new ArrayList<Float>(probNode.getStatesSize());
						for (int i = 0; i < probNode.getStatesSize(); i++) {
							marginalWhenResolved.add(probNode.getMarginalAt(i));
						}
					}
				} else {
					try {
						Debug.println(getClass(), "Node " + getQuestionId() + " is not present in the shared Bayes Net.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					// must keep running, because RevertTradeNetworkAction#revert() may be calling this method just to resolve questions in the asset networks
				}
				// TODO revert on some error
				
				// do not release lock to global BN until we change all asset nets
				synchronized (getUserToAssetAwareAlgorithmMap()) {
					Collection<AssetAwareInferenceAlgorithm> usersToChange = null;
					// update all stored users
					usersToChange = getUserToAssetAwareAlgorithmMap().values();
					
					synchronized (getDefaultInferenceAlgorithm()) {
						Node node = getProbabilisticNetwork().getNode(Long.toString(getQuestionId()));
						if (node != null) {
//							getDefaultInferenceAlgorithm().setAsPermanentEvidence(node, getSettledState(), isToDeleteResolvedNode());
						} else {
							throw new InexistingQuestionException("Node " + getQuestionId() + " is not present in network.", getQuestionId());
						}
					}
					
					// do not allow getUserToAssetAwareAlgorithmMap() to be changed here. I.e. do not allow new users to be added now
					for (AssetAwareInferenceAlgorithm algorithm : usersToChange) {
						synchronized (algorithm.getAssetNetwork()) {
							Node assetNode = algorithm.getAssetNetwork().getNode(Long.toString(getQuestionId()));
							if (assetNode == null ) {
								throw new InexistingQuestionException("Node " + getQuestionId() + " is not present in asset net of user " + algorithm.getAssetNetwork(), getQuestionId());
							}
							algorithm.setAsPermanentEvidence(assetNode, getSettledState(),isToDeleteResolvedNode());
						}
					}
				}
			}
		}
		public List<Float> getPercent() { return marginalWhenResolved; }
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#resolveQuestion(java.lang.Long, java.util.Date, long, int)
	 */
	public boolean resolveQuestion(Long transactionKey, Date occurredWhen,
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
			throw new InexistingQuestionException("Question ID " + questionId + " was not found.", questionId);
		}
		if (settledState >= node.getStatesSize()) {
			throw new IllegalArgumentException("Question " + questionId + " has no state " + settledState);
		}
		if (node instanceof TreeVariable) {
			TreeVariable var = (TreeVariable) node;
			if (var.hasEvidence()) {
				int state = (settledState<0)?((-settledState)-1):settledState;
				if (var.getEvidence() != state || var.getMarginalAt(state) <= 0f) {
					throw new IllegalArgumentException("Question " + questionId + " is already resolved, hence it cannot be resolved to " + settledState);
				}
			}
		}

		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			// instantiate the action object for adding a question
			this.addNetworkAction(transactionKey, new BruteForceResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settledState));
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for adding a question
			this.addNetworkAction(transactionKey, new BruteForceResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settledState));
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#doBalanceTrade(java.lang.Long, java.util.Date, java.lang.String, long, long, java.util.List, java.util.List)
	 */
	@Override
	public boolean doBalanceTrade(Long transactionKey, Date occurredWhen,
			String tradeKey, long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException, InvalidAssumptionException,
			InexistingQuestionException {
		throw new UnsupportedOperationException("This operation is not supported by this engine yet");
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#previewBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	@Override
	public List<Float> previewBalancingTrade(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("This operation is not supported by this engine yet");
	}

	

	// TODO BruteForceMarkovEngine needs doBalanceTrade correctly implemented

}