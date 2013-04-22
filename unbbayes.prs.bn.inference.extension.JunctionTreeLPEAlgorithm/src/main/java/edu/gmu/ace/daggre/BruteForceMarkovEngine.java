/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm.JointPotentialTable;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
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
		this.setToLazyInitializeUsers(false);
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
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getProbLists(java.util.List, java.util.List, java.util.List, boolean, unbbayes.prs.bn.ProbabilisticNetwork, boolean)
	 */
	public Map<Long,List<Float>> getProbLists(List<Long> questionIds, List<Long>assumptionIds, List<Integer> assumedStates, 
			boolean isToNormalize, ProbabilisticNetwork net, boolean canChangeNet) throws IllegalArgumentException {
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
					algorithm.propagate(false);
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
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getAssetsIfStates(long, java.util.List, java.util.List, unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm, boolean, unbbayes.prs.bn.Clique)
	 */
	protected List getAssetsIfStates(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, AssetAwareInferenceAlgorithm alg, boolean isToReturnQValuesInsteadOfAssets, Clique clique,
			PotentialTable cptOfQuestionGivenAssumptionsObtainedFromClique)
			throws IllegalArgumentException {
		
		if (assumptionIds != null && assumedStates != null && assumptionIds.size() != assumedStates.size()) {
			throw new IllegalArgumentException("This implementation requires assumptionIds and assumedStates to have same size.");
		}
		
		BruteForceAssetAwareInferenceAlgorithm algorithm = (BruteForceAssetAwareInferenceAlgorithm) alg;
		// basic assertion
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm cannot be null");
		}
		
		@SuppressWarnings("rawtypes")
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
			// execute only the actions which does not change network structure
			List<NetworkAction> executedActions = getExecutedActions();
			List<NetworkAction> actionsToExecuteAgain = new ArrayList<NetworkAction>();
			for (NetworkAction action : executedActions) {
				if (!action.isStructureConstructionAction()) {
					actionsToExecuteAgain.add(action);
				}
			}
			this.execute(actionsToExecuteAgain);
		}

		/* (non-Javadoc)
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.RebuildNetworkAction#isToExecuteAction(edu.gmu.ace.daggre.NetworkAction)
		 */
		protected boolean isToExecuteAction(NetworkAction action) {
			return !action.isStructureConstructionAction();
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
					
					synchronized (getDefaultInferenceAlgorithm()) {
						Node node = getProbabilisticNetwork().getNode(Long.toString(getQuestionId()));
						if (node != null) {
//							getDefaultInferenceAlgorithm().setAsPermanentEvidence(node, getSettledState(), isToDeleteResolvedNode());
						} else {
							throw new InexistingQuestionException("Node " + getQuestionId() + " is not present in network.", getQuestionId());
						}
					}
					
					// do not allow getUserToAssetAwareAlgorithmMap() to be changed here. I.e. do not allow new users to be added now
					for (Entry<Long, AssetAwareInferenceAlgorithm> userIdAndAlgorithm : getUserToAssetAwareAlgorithmMap().entrySet()) {
						AssetAwareInferenceAlgorithm algorithm = userIdAndAlgorithm.getValue();
						synchronized (algorithm.getAssetNetwork()) {
							Node assetNode = algorithm.getAssetNetwork().getNode(Long.toString(getQuestionId()));
							if (assetNode == null ) {
								throw new InexistingQuestionException("Node " + getQuestionId() + " is not present in asset net of user " + algorithm.getAssetNetwork(), getQuestionId());
							}
							if (isToStoreCashBeforeResolveQuestion()) {
								// update getUserIdToResolvedQuestionCashBeforeMap() and UserIdToResolvedQuestionCashGainMap() by obtaining cash before and after asset update
								synchronized (getUserIdToResolvedQuestionCashBeforeMap()) {  
									synchronized (getUserIdToResolvedQuestionCashGainMap()) { 
										// extract the mapping where we are going to store the cash before
										Map<Long, Float> questionToCashBefore = getUserIdToResolvedQuestionCashBeforeMap().get(userIdAndAlgorithm.getKey());
										if (questionToCashBefore == null) {
											// generate entry if this is the first time
											questionToCashBefore = new HashMap<Long, Float>();
										}
										// extract the mapping where we are going to store the gains
										Map<Long, Float> questionToGain = getUserIdToResolvedQuestionCashGainMap().get(userIdAndAlgorithm.getKey());
										if (questionToGain == null) {
											// generate entry if this is the first time
											questionToGain = new HashMap<Long, Float>();
										}
										// obtain cash before
										float cashBefore = getCash(userIdAndAlgorithm.getKey(), null, null);
										// immediately store the cash before
										questionToCashBefore.put(getQuestionId(), cashBefore);
										getUserIdToResolvedQuestionCashBeforeMap().put(userIdAndAlgorithm.getKey(), questionToCashBefore);
										// resolve the question in the asset structure of this user
										algorithm.setAsPermanentEvidence(assetNode, getSettledState(),isToDeleteResolvedNode());
										// obtain the difference between cash before and after.
										float gain = getCash(userIdAndAlgorithm.getKey(), null, null) - cashBefore;
										// update the mapping if the difference was "large" enough
										if (Math.abs(gain) > getProbabilityErrorMargin()) {
											// put the mapping from question ID (of settled question) to gain from that question
											questionToGain.put(getQuestionId(), gain);
											getUserIdToResolvedQuestionCashGainMap().put(userIdAndAlgorithm.getKey(), questionToGain);
										}
									}
								}
							} else {
								algorithm.setAsPermanentEvidence(assetNode, getSettledState(),isToDeleteResolvedNode());
							}
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

	public synchronized boolean commitNetworkActions(long transactionKey, boolean isToRebuildNetOnStructureChange) throws IllegalArgumentException, ZeroAssetsException {
		
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
			List<NetworkAction> addCashActions = null;	// will store actions which adds cash to users
			if (isToSortAddCashAction()) {
				// only instantiate if we will use it
				addCashActions = new ArrayList<NetworkAction>();
			}
			List<NetworkAction> otherActions = new ArrayList<NetworkAction>();	// will store actions which does not change network structure
			// collect all network change actions and other actions, regarding their original order
			for (NetworkAction action : actions) {
				if (action.isStructureConstructionAction()) {
					netChangeActions.add(action);
				} else if (isToSortAddCashAction() && (action instanceof AddCashNetworkAction)) {
					addCashActions.add(action);
				} else {
					// Note: if isToSortAddCashAction() == false, AddCashNetworkAction will be included here
					otherActions.add(action);
				}
			}
			
			// this is the action which rebuilds the network if there is any action changing network structure
			RebuildNetworkAction rebuildNetworkAction = null;
			
			// change the content of actions. Since we are using the same list object, this should also change the values stored in getNetworkActionsMap.
			if (!netChangeActions.isEmpty() && isToRebuildNetOnStructureChange) {
				// only make changes (reorder actions and recompile network) if there is any action changing the structure of network.
				actions.clear();	// reset actions first
				
				/*
				 * mark the structure change actions as executed, 
				 * so that RebuildNetworkAction can handle them when rebuilding network.
				 * CAUTION: be careful not to change the behavior of RebuildNetworkAction
				 */
				for (NetworkAction action : netChangeActions) {
					// mark action as executed
					action.setWhenExecutedFirstTime(new Date());	// normal execution of action -> update attribute "whenExecutedFirst"
					if (!(action.equals(rebuildNetworkAction))) {
						synchronized (getExecutedActions()) {
							// this is partially ordered by NetworkAction#getWhenCreated(), because actions is ordered by NetworkAction#getWhenCreated()
							getExecutedActions().add(action);	
						}
					}
				}
//				actions.addAll(netChangeActions);	// netChangeActions comes first
				rebuildNetworkAction = new RebuildNetworkAction(netChangeActions.get(0).getTransactionKey(), new Date(), null, null);
				actions.add(rebuildNetworkAction);	// <rebuild action> is inserted between netChangeActions and otherActions
				if (isToSortAddCashAction()) {
					// note: if isToSortAddCashAction() == true, addCashActions was supposedly initialized and filled, but let's just make sure and use try-catch
					try {
						actions.addAll(addCashActions);
					} catch (NullPointerException e) {
						// populate message with more useful info
						throw new RuntimeException("The flag \"isToSortAddCashAction\" is " + isToSortAddCashAction() 
								+ ", and an attempt to sort addCash operations was performed. However, the list of actions was null. " +
								"This is probably due to incompatible version of Markov Engine. " +
								"Please, check version, or try changing the value of the flag \"isToSortAddCashAction\".", e);
					}
				}
				actions.addAll(otherActions);	// otherActions comes later
			}
			
			// TODO trades of same user to same node given compatible assumptions (all nodes in same clique) can be integrated to 1 trade
			
			// then, execute all actions
			for (int i = 0; i < actions.size(); i++) {
				NetworkAction action = actions.get(i);
				
				// check if we can integrate several resolutions into a single mass resolution (if there are resolutions in sequence)
				if (isToIntegrateConsecutiveResolutions() && 
						(action instanceof ResolveQuestionNetworkAction) 
						&& (i+1 < actions.size()) && (actions.get(i+1) instanceof ResolveQuestionNetworkAction)) {
					
					// store the series of resolutions
					List<ResolveQuestionNetworkAction> consecutiveResolutions = new ArrayList<MarkovEngineImpl.ResolveQuestionNetworkAction>();
					// add the 2 consecutive resolutions to the list
					consecutiveResolutions.add((ResolveQuestionNetworkAction) action);
					consecutiveResolutions.add((ResolveQuestionNetworkAction)actions.get(++i));
					
					// check if there are more resolutions following the above two
					while (++i < actions.size()) {
						if (actions.get(i) instanceof ResolveQuestionNetworkAction) {
							consecutiveResolutions.add((ResolveQuestionNetworkAction) actions.get(i));
						} else {
							// the i should be pointing to the last instance of ResolveQuestionNetworkAction 
							// (so that in next iteration of for it will point to an action which is not a ResolveQuestionNetworkAction)
							--i;
							break;
						}
					}
					// note: it's OK if i >= actions.size() happens at this point, 
					// because ResolveSetOfQuestionsNetworkAction will be executed and the "for" loop will finish
					
					// substitute consecutive resolutions to a single mass resolution
					action = new ResolveSetOfQuestionsNetworkAction(transactionKey, action.getWhenCreated(), consecutiveResolutions);
				}
				
				// actually run action
				try {
					action.execute();
				} catch (ZeroAssetsException z) {
					// do not consider this action anymore
					removeNetworkActionFromQuestionMap(action, action.getQuestionId());
					// remove transaction before releasing the lock to actions
					getNetworkActionsMap().remove(transactionKey);
					throw z;
				}
				
				// mark action as executed
				action.setWhenExecutedFirstTime(new Date());	// normal execution of action -> update attribute "whenExecutedFirst"
				if (!action.equals(rebuildNetworkAction)) {
					synchronized (getExecutedActions()) {
						// this is partially ordered by NetworkAction#getWhenCreated(), because actions is ordered by NetworkAction#getWhenCreated()
						//if (!getExecutedActions().contains(action)) {
							getExecutedActions().add(action);	
						//}
						// TODO do we need to store RebuildNetworkAction in the history?
					}
				}
			}
			
			// remove transaction before releasing the lock to actions
			getNetworkActionsMap().remove(transactionKey);
			
		}	// release lock to actions
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestion(long, java.util.Date, long, int, java.util.List)
	 */
	public synchronized boolean addQuestion(Long transactionKey, Date occurredWhen,
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
		
		synchronized (this.getResolvedQuestions()) {
			if (this.getResolvedQuestions().containsKey(questionId)) {
				throw new IllegalArgumentException("Question " + questionId + " was already resolved.");
			}
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
		}
		
		// instantiate the action object for adding a question
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs, false));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs, false));
			// also add into index of questions being created in transaction
			synchronized (getQuestionsToBeCreatedInTransaction()) {
				Set<Long> set = getQuestionsToBeCreatedInTransaction().get(transactionKey);
				if (set == null) {
					set = new HashSet<Long>();
				}
				set.add(questionId);
				getQuestionsToBeCreatedInTransaction().put(transactionKey, set);
			}
		}
		
		return true;
	}

	// TODO BruteForceMarkovEngine needs doBalanceTrade correctly implemented

}
