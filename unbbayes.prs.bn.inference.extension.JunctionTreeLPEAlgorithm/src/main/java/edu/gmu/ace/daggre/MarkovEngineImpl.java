package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Collection;
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
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor.CliqueEvidenceUpdater;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor.NoCliqueException;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm.ExpectedAssetCellMultiplicationListener;
import unbbayes.prs.bn.inference.extension.AssetPropagationInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm.IAssetNetAlgorithmMemento;
import unbbayes.prs.bn.inference.extension.IQValuesToAssetsConverter;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import edu.gmu.ace.daggre.ScoreSummary.SummaryContribution;

/**
 * This is the default implementation of {@link MarkovEngineInterface}.
 * This class is basically a wrapper for the functionalities offered by
 * {@link AssetAwareInferenceAlgorithm}. 
 * It adds history feature and transactional behaviors to
 * {@link AssetAwareInferenceAlgorithm}.
 * @author Shou Matsumoto
 * @version July 01, 2012
 */
public class MarkovEngineImpl implements MarkovEngineInterface, IQValuesToAssetsConverter {
	
	private boolean isToUseQValues = false;
	
	

	/** 
	 * This is used in {@link #getAssetsIfStates(long, List, List, AssetAwareInferenceAlgorithm, boolean)} 
	 * as argument of {@link InCliqueConditionalProbabilityExtractor#buildCondicionalProbability(INode, List, unbbayes.prs.Graph, IInferenceAlgorithm, CliqueEvidenceUpdater)}
	 */
	public static final CliqueEvidenceUpdater ASSET_CLIQUE_EVIDENCE_UPDATER = new CliqueEvidenceUpdater() {
		/** 
		 * Does the same of {@link InCliqueConditionalProbabilityExtractor#DEFAULT_CLIQUE_EVIDENCE_UPDATER}, 
		 * but modifies marginalMultiplier so that occurrences of 0.0f are substituted to {@link Float#POSITIVE_INFINITY}.
		 * This is necessary, because impossible states are represented as {@link Float#POSITIVE_INFINITY}
		 * in assets (which is in logarithm space, whose assets <= 0 are possible when 0<q-value<=1).
		 */
		public void updateEvidenceWithinClique(Clique clique, PotentialTable cliqueTable, float[] marginalMultiplier, Node nodeWithEvidence) {
			for (int i = 0; i < marginalMultiplier.length; i++) {
				if (marginalMultiplier[i] == 0f) {
					marginalMultiplier[i] = Float.POSITIVE_INFINITY;
				}
			}
			cliqueTable.updateEvidences(marginalMultiplier, cliqueTable.indexOfVariable(nodeWithEvidence));
			/*
			 *  We want X * Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY for any X, 
			 *  but 0 * Float.POSITIVE_INFINITY == Float.NaN, and -1 * Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY. 
			 *  So, change Float.NaN and Float.NEGATIVE_INFINITY to Float.POSITIVE_INFINITY to get desired behavior
			 */
			for (int i = 0; i < cliqueTable.tableSize(); i++) {
				float value = cliqueTable.getValue(i);
				// Note: (Float.NaN != Float.NaN) is a special property of Float.NaN
				if ((value != value) || value == Float.NEGATIVE_INFINITY) { 
					cliqueTable.setValue(i, Float.POSITIVE_INFINITY);
				}
			}
		}
	};

	private IAssetAwareInferenceAlgorithmBuilder assetAwareInferenceAlgorithmBuilder = new IAssetAwareInferenceAlgorithmBuilder() {
		public AssetAwareInferenceAlgorithm build( IInferenceAlgorithm probDelegator, float initQValue) {
			AssetAwareInferenceAlgorithm ret = (AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(probDelegator);
			ret.setToUseQValues(isToUseQValues());
			ret.setDefaultInitialAssetTableValue(initQValue);
			return ret;
		}
	};

	private float probabilityErrorMargin = 0.005f;

	private Map<Long, List<NetworkAction>> networkActionsMap;
	
	private HashMap<Long, List<NetworkAction>> networkActionsIndexedByQuestions;	// HashMap allows null key
	
	private Long transactionCounter = 0L;

	private Map<Long, AssetAwareInferenceAlgorithm> userToAssetAwareAlgorithmMap;

//	private boolean isToAddCashProportionally = true;

	private float defaultInitialAssetTableValue = AssetPropagationInferenceAlgorithm.IS_TO_USE_Q_VALUES?1:0;

	private float currentLogBase = (float) 2;

	private float currentCurrencyConstant = 100;

	private ProbabilisticNetwork probabilisticNetwork;

	private AssetAwareInferenceAlgorithm defaultInferenceAlgorithm;

	private IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor;

	private List<NetworkAction> executedActions;

//	private boolean isToReturnConditionalAssets = false;

//	private AssetAwareInferenceAlgorithm inferenceAlgorithm;
	
	private boolean isToObtainProbabilityOfResolvedQuestions = true;
	
	private boolean isToDeleteResolvedNode = true;
	
	private boolean isToThrowExceptionOnInvalidAssumptions = false;

//	/** Set this value to < 0 to disable this feature */
//	private float forcedInitialQValue = 100.0;
//
//	private double currencyMultiplier = 1.0;

	private boolean isToDoFullPreview = false;

	private Map<Long, List<Long>> tradedQuestionsMap = new HashMap<Long, List<Long>>();

	private boolean isToReturnEVComponentsAsScoreSummary = false;

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
	public static MarkovEngineInterface getInstance(float logBase, float currencyConstant, float initialUserAssets) {
		return MarkovEngineImpl.getInstance(logBase, currencyConstant, initialUserAssets, false);
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
	public static MarkovEngineInterface getInstance(float logBase, float currencyConstant, float initialUserAssets, boolean isToThrowExceptionOnInvalidAssumptions) {
		return MarkovEngineImpl.getInstance(logBase, currencyConstant, initialUserAssets, isToThrowExceptionOnInvalidAssumptions, false);
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
	 * @param isToThrowExceptionOnInvalidAssumptions : if true, methods in this class
	 * will throw exceptions if invalid assumptions are passed. If false, the 
	 * program will try to ignore invalid assumptions.
	 * @param isToUseQValues : if true, assets will be handled in terms of q values
	 * (values which changes are linearly proportional to the changes in probabilities).
	 * If false, assets will be handled directly (changes in assets are logarithmically
	 * proportional to changes in probabilities).
	 * @return new instance of some class implementing {@link MarkovEngineInterface}
	 */
	public static MarkovEngineInterface getInstance(float logBase, float currencyConstant, float initialUserAssets, 
			boolean isToThrowExceptionOnInvalidAssumptions, boolean isToUseQValues) {
		MarkovEngineImpl ret = (MarkovEngineImpl) MarkovEngineImpl.getInstance();
		ret.setToUseQValues(isToUseQValues);
		ret.setCurrentCurrencyConstant(currencyConstant);
		ret.setCurrentLogBase(logBase);
		if (isToUseQValues) {
			ret.setDefaultInitialAssetTableValue((float) ret.getQValuesFromScore(initialUserAssets));
		} else {
			ret.setDefaultInitialAssetTableValue(initialUserAssets);
		}
		ret.setToThrowExceptionOnInvalidAssumptions(isToThrowExceptionOnInvalidAssumptions);
		return ret;
	}
	

	/**
	 * Translates asset Q values to scores using logarithm functions.
	 * <br/>
	 * Score = b*x*log(assetQ), with log being the logarithm of base {@link #getCurrentLogBase()},
	 * x = {@link #getCurrencyConstantMultiplier()}, 
	 * and b = {@link #getCurrentCurrencyConstant()}.
	 * @param assetQ
	 * @return the score value
	 * @see #getQValuesFromScore(float)
	 */
	public float getScoreFromQValues(double assetQ) {
		return (float) ((Math.log(assetQ)/Math.log(getCurrentLogBase())) * (getCurrentCurrencyConstant())) ;
//		return (float) ((Math.log(assetQ)/Math.log(getCurrentLogBase())) * (getCurrentCurrencyConstant()* getCurrencyConstantMultiplier())) ;
	}
	
	/**
	 * Translates scores to asset Q values using logarithm functions.
	 * <br/>
	 * Score = b*x*log(assetQ), with log being the logarithm of base {@link #getCurrentLogBase()},
	 * x = {@link #getCurrencyConstantMultiplier()}, 
	 * and b = {@link #getCurrentCurrencyConstant()}.
	 * @param score
	 * @return the asset q value
	 * @see #getScoreFromQValues(float)
	 */
	public double getQValuesFromScore(float score) {
		if (Float.isInfinite(score)) {
			throw new ZeroAssetsException("Attempted to get q-values from " + score);
		}
		/*
		 * Score = b*log(assetQ)
		 * -> Score / b = log(assetQ)
		 * -> power(baseOfLog, (Score / b)) = power(baseOfLog, log(assetQ))
		 * -> power(baseOfLog, (Score / b)) = assetQ
		 */
		double value = Math.pow(getCurrentLogBase(), (score/(getCurrentCurrencyConstant())));
		if (Double.isInfinite(value)) {
			throw new ZeroAssetsException("Overflow detected when converting " + score + " assets to q-values.");
		}
		return value;
//		return Math.pow(getCurrentLogBase(), (score/(getCurrentCurrencyConstant() * getCurrencyConstantMultiplier())));
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
		
		setExecutedActions(new ArrayList<NetworkAction>());	// initialize list of network actions ordered by the moment of execution
		
		setProbabilisticNetwork(new ProbabilisticNetwork("DAGGRE"));
		// disable log
		getProbabilisticNetwork().setCreateLog(false);
		// disable hierarchic tree
		getProbabilisticNetwork().setHierarchicTree(null);
		
		// prepare inference algorithm for the BN
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
		JeffreyRuleLikelihoodExtractor jeffreyRuleLikelihoodExtractor = (JeffreyRuleLikelihoodExtractor) AssetAwareInferenceAlgorithm.DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR;
		junctionTreeAlgorithm.setLikelihoodExtractor(jeffreyRuleLikelihoodExtractor);
		// prepare default inference algorithm for asset network
		AssetAwareInferenceAlgorithm defaultAlgorithm = getAssetAwareInferenceAlgorithmBuilder().build(junctionTreeAlgorithm, getDefaultInitialAssetTableValue());
		defaultAlgorithm.setToCalculateLPE(false);	// we are only interested on min-values, never min-states
		// user MarkovEngineImpl to convert from q values to assets
		defaultAlgorithm.setqToAssetConverter(this);
		// usually, users seem to start with 0 delta (delta are logarithmic, so 0 delta == 1 q table), but let's use the value of getDefaultInitialQTableValue
		defaultAlgorithm.setDefaultInitialAssetTableValue(getDefaultInitialAssetTableValue());
		defaultAlgorithm.setToPropagateForGlobalConsistency(false);	// force algorithm to do min-propagation of delta only when prompted
		defaultAlgorithm.setToCalculateMarginalsOfAssetNodes(false);// OPTIMIZATION : do not calculate marginals of asset nodes without explicit call
		defaultAlgorithm.setToAllowZeroAssets(true);		// the default algorithm is used only by the markov engine, never by a user, so can have 0 asset
		defaultAlgorithm.setToLogAssets(false);						// optimization: do not generate logs
		defaultAlgorithm.setToUpdateOnlyEditClique(true);			// optimization: only update current clique, if it is to update at all
		defaultAlgorithm.setToUpdateSeparators(false);				// optimization: do not touch separators of asset junction tree
		defaultAlgorithm.setToUpdateAssets(false);					// optimization: do not update assets at all for the default (markov engine) user
		setDefaultInferenceAlgorithm(defaultAlgorithm);				
		
		
		
		// several methods in this class reuse the same conditional probability extractor. Extract it here
		setConditionalProbabilityExtractor(jeffreyRuleLikelihoodExtractor.getConditionalProbabilityExtractor());
		
		// initialize the map managing users (and the algorithms responsible for changing values of users asset networks)
		setUserToAssetAwareAlgorithmMap(new ConcurrentHashMap<Long, AssetAwareInferenceAlgorithm>()); // concurrent hash map is known to be thread safe yet fast.
		
		// initialize map which manages which questions have trades from a given user
		setTradedQuestionsMap(new HashMap<Long, List<Long>>());
		
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
//		System.out.println("[startNetworkActions]" + ret);
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
			
			// this is the action which rebuilds the network if there is any action changing network structure
			RebuildNetworkAction rebuildNetworkAction = null;
			
			// change the content of actions. Since we are using the same list object, this should also change the values stored in getNetworkActionsMap.
			if (!netChangeActions.isEmpty()) {
				// only make changes (reorder actions and recompile network) if there is any action changing the structure of network.
				actions.clear();	// reset actions first
				actions.addAll(netChangeActions);	// netChangeActions comes first
				rebuildNetworkAction = new RebuildNetworkAction(netChangeActions.get(0).getTransactionKey(), new Date(), null, null);
				actions.add(rebuildNetworkAction);	// <rebuild action> is inserted between netChangeActions and otherActions
				actions.addAll(otherActions);	// otherActions comes later
			}
			
			// TODO trades of same user to same node given compatible assumptions (all nodes in same clique) can be integrated to 1 trade
			
			// then, execute all actions
			for (NetworkAction action : actions) {
				action.execute();
				// mark action as executed
				action.setWhenExecutedFirstTime(new Date());	// normal execution of action -> update attribute "whenExecutedFirst"
				if (!(action.equals(rebuildNetworkAction))) {
					synchronized (getExecutedActions()) {
						// this is partially ordered by NetworkAction#getWhenCreated(), because actions is ordered by NetworkAction#getWhenCreated()
						getExecutedActions().add(action);	
						// TODO do we need to store RebuildNetworkAction in the history?
					}
				}
			}
			
			// remove transaction before releasing the lock to actions
			getNetworkActionsMap().remove(transactionKey);
			
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
		private final Long transactionKey;
		private Date whenExecutedFirst = null;
		private final Date tradesStartingWhen;
		private final Long questionId;
		/**
		 * Default constructor.
		 * @param transactionKey : mandatory key which can be obtained from {@link MarkovEngineImpl#startNetworkActions()}
		 * @param whenCreated : when this action was created.
		 * @param tradesStartingWhen : filter for the playback. Playback will stop after the first
		 * {@link AddTradeNetworkAction} created after this date. If null, all actions will be played back.
		 * @param questionId : another filter for the playback. 
		 * If tradesStartingWhen == null, this argument will be ignored.
		 * Playback will stop after the first
		 * {@link AddTradeNetworkAction} with {@link AddTradeNetworkAction#getQuestionId()} matching
		 * this questionId and created after tradesStartingWhen.
		 */
		public RebuildNetworkAction(Long transactionKey, Date whenCreated, Date tradesStartingWhen, Long questionId) {
			this.transactionKey = transactionKey;
			this.whenCreated = whenCreated;
			this.tradesStartingWhen = tradesStartingWhen;
			this.questionId = questionId;
		}
		/** Rebuild the BN */
		public void execute() {
			List<NetworkAction> actions = getExecutedActions();
			// reset network
			ProbabilisticNetwork net = getProbabilisticNetwork();
			// the list "actions" without network change actions
			List<NetworkAction> actionsToExecute = new ArrayList<NetworkAction>();	
			synchronized (actions) {
				synchronized (getDefaultInferenceAlgorithm()) {
					// clear content of net
					synchronized (net) {
						ArrayList<Node> nodesToRemove = new ArrayList(net.getNodes());
						for (Node node : nodesToRemove) {
							net.removeNode(node);
						}
						try {
							net.setJunctionTree(net.getJunctionTreeBuilder().buildJunctionTree(net));
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
				}
				// separate the content of actions into those changing network structure and those not changing structure
				List<NetworkAction> networkChangeActions = new ArrayList<NetworkAction>(); 
				for (NetworkAction action : actions) {
					if (action.isStructureChangeAction()) {
						networkChangeActions.add(action);
					} else {
						actionsToExecute.add(action);
					}
				}
				// execute the trades related to network construction before actionsToExecute
				for (NetworkAction changeAction : networkChangeActions) {
					if (this.isToExecuteAction(changeAction)) {
						changeAction.execute();
					}
				}
			}
			// recompile network and execute actions which does not change network structure
			this.execute(actionsToExecute); 
		}
		/** Rebuild the BN */
		protected void execute(List<NetworkAction> actions) {
			// BN to rebuild
			ProbabilisticNetwork net = getProbabilisticNetwork();
			// Supposedly, only 1 transaction can commit, so we only need to block net to block threads reading net
			synchronized (getDefaultInferenceAlgorithm()) {
				synchronized (net) {
					if (net.getNodeCount() > 0) {
						getDefaultInferenceAlgorithm().run();
					}
				}
			}
			// rebuild all user asset nets
			synchronized (getUserToAssetAwareAlgorithmMap()) { // do not allow getUserToAssetAwareAlgorithmMap() to be changed here. I.e. do not allow new users to be added now
				if (!getUserToAssetAwareAlgorithmMap().isEmpty()) {	// we only need to rebuild user's assets if there were some users
					// by removing all users, we are forcing markov engine to build user's asset network when first prompted
					getUserToAssetAwareAlgorithmMap().clear();	
					// by executing some action related to user, user will be re-created.
					// hence, "read-only" users are not rebuild, because such users did never commit a network action.
					synchronized (actions) {
						
						int indexOfLastActionPlayedBack = 0;
						// Note: if we are rebooting the system, the history is supposedly empty. We only need to rebuild user assets if history is not empty
						for (; indexOfLastActionPlayedBack < actions.size(); indexOfLastActionPlayedBack++) {
							NetworkAction action = actions.get(indexOfLastActionPlayedBack);
							if (action.equals(this)) {
								// do not execute actions which comes after myself
								break;
							}
							/*
							 * conditions regarding revertTrade: 
							 * getTradesStartingWhen() == null -> redo all trades no matter the value of getQuestionId()
							 * getTradesStartingWhen() != null
							 * 			getQuestionId() == null -> redo trades until first trade after getTradesStartingWhen()
							 * 			getQuestionId() != null -> redo trades until first trade on getQuestionId() after getTradesStartingWhen() 
							 * 
							 * So, redo all actions until we find a trade which matches filtering criteria.
							 */
							if (getTradesStartingWhen() != null && action instanceof AddTradeNetworkAction) {
								// only consider trades when filtering by date and question
								if (action.getWhenCreated() != null && !action.getWhenCreated().before(this.getTradesStartingWhen())) {
									// only consider trades created after or equals to getTradesStartingWhen()
									if (this.getQuestionId() == null) {
										// no filter for questions
										break;	// stop redoing trades from here
									} else if (action.getQuestionId() != null && action.getQuestionId().equals(this.getQuestionId())) {
										break;	// stop redoing trades from here
									}
								}
							}
							// redo all trades using the history
							if (this.isToExecuteAction(action)) {
								// actions with action.isStructureChangeAction() == false are supposedly the ones changing probabilities/assets and not changing structure
								action.execute();
								// this is not an ordinal execution of the action, so do not update attribute whenExecutedFirst.
							}
						}
						
						// from this point, play back actions without updating assets in trades
						for (int i = indexOfLastActionPlayedBack; i < actions.size(); i++) {
							NetworkAction action = actions.get(i);
							if (action.equals(this)) {
								// do not execute actions which comes after myself
								break;
							}
							if (!action.isStructureChangeAction()) {
								// actions with action.isStructureChangeAction() == false are supposedly the ones changing probabilities/assets and not changing structure
								if (action instanceof AddTradeNetworkAction) {
									AddTradeNetworkAction tradeAction = (AddTradeNetworkAction) action;
									tradeAction.execute(false);
								} else {
									action.execute();
								}
								// this is not an ordinal execution of the action, so do not update attribute whenExecutedFirst.
							}
						}
					}
				}
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Cannot revert a network rebuild action.");
		}
		/** By overwriting this method, you can control which actions are executed with assets changes */
		protected boolean isToExecuteAction(NetworkAction action) { return true /*!action.isStructureChangeAction()*/; }
		public Date getWhenCreated() { return whenCreated; }
		/** This action reboots the network, but does not change the structure by itself */
		public boolean isStructureChangeAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return null; }
		public List<Float> getPercent() { return null; }
		public String getTradeId() { return null; }
		/** This is non-null only if this action is trying to revert a trade related to this question ID */
		public Long getQuestionId() { return this.questionId; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
		/** This is a filter used when reverting trades */
		public Date getTradesStartingWhen() { return tradesStartingWhen; }
	}
	
	/**
	 * This is an network action for {@link MarkovEngineImpl#revertTrade(long, Date, Date, Long)}.
	 * Currently, there is no difference to {@link RebuildNetworkAction},
	 * so this is only a placeholder for future changes.
	 * @author Shou Matsumoto
	 */
	public class RevertTradeNetworkAction extends RebuildNetworkAction {
		/** Default constructor initializing fields */
		public RevertTradeNetworkAction(Long transactionKey, Date whenCreated, Date tradesStartingWhen, Long questionId) {
			super(transactionKey, whenCreated, tradesStartingWhen, questionId);
		}
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
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs));
		}
		
		return true;
	}
	
	/**
	 * Represents a network action for adding a question into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestion(long, Date, long, int, List)
	 */
	public class AddQuestionNetworkAction implements NetworkAction {
		private final Long transactionKey;
		private final Date occurredWhen;
		private final long questionId;
		private final int numberStates;
		private final List<Float> initProbs;
		private Date whenExecutedFirst;
		/** Default constructor initializing fields */
		public AddQuestionNetworkAction(Long transactionKey, Date occurredWhen,
				long questionId, int numberStates, List<Float> initProbs) {
			super();
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.questionId = questionId;
			this.numberStates = numberStates;
			if (initProbs != null) {
				// use a copy, so that changes in the original do not affect this object
				this.initProbs = new ArrayList<Float>(initProbs);
			} else {
				this.initProbs = null;
			}
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
			throw new UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
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
		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)
	 */
	public boolean addQuestionAssumption(Long transactionKey, Date occurredWhen, long childQuestionId, List<Long> parentQuestionIds,  List<Float> cpd) throws IllegalArgumentException {
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		
		if ( (parentQuestionIds == null || parentQuestionIds.isEmpty() )
				&& (cpd == null || cpd.isEmpty())) {
			// this is neither a request for adding a new dependency nor for substituting cpd.
			return false;
		}
		
		// this is the list of actions in a same transaction.
		List<NetworkAction> actions = null;
		if (transactionKey != null) {
			actions = this.getNetworkActionsMap().get(transactionKey);
			// check existence of transactionKey
			if (actions == null) {
				// startNetworkAction should have been called.
				throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
			}
			// action should be at least a non-null empty list if startNetworkAction was called previously
		}
		
		
		int childNodeStateSize = -1;	// this var stores the quantity of states of the node identified by sourceQuestionId.
		
		// check existence of child
		Node child  = null;
		synchronized (getProbabilisticNetwork()) {
			child = getProbabilisticNetwork().getNode(Long.toString(childQuestionId));
		}
		if (child == null) {
			// child node does not exist. Check if there was some previous action (in same transaction) adding such node
			if (actions != null) {
				synchronized (actions) {
					for (NetworkAction networkAction : actions) {
						if (networkAction instanceof AddQuestionNetworkAction) {
							AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
							if (addQuestionNetworkAction.getQuestionId().equals(childQuestionId)) {
								childNodeStateSize = addQuestionNetworkAction.getNumberStates();
								break;
							}
						}
					}
				}
			}
			if (childNodeStateSize < 0) {	
				// if negative, then expectedSizeOfCPD was not updated, so sourceQuestionId was not found in last loop
				throw new InexistingQuestionException("Question ID " + childQuestionId + " does not exist.", childQuestionId);
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
				if (actions != null) {
					synchronized (actions) {
						boolean hasFound = false;
						for (NetworkAction networkAction : actions) {
							if (networkAction instanceof AddQuestionNetworkAction) {
								AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
//							System.out.println(addQuestionNetworkAction.getQuestionId());
								if (addQuestionNetworkAction.getQuestionId().equals(assumptiveQuestionId)) {
									// size of cpd = MULT (<quantity of states of child and parents>).
									expectedSizeOfCPD *= addQuestionNetworkAction.getNumberStates();
									hasFound = true;
									break;
								}
							}
						}
						if (!hasFound) {	
							// parent was not found
							throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist.", assumptiveQuestionId);
						}
					}
				} else {
					throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist.", assumptiveQuestionId);
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
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen, childQuestionId, parentQuestionIds, cpd));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen, childQuestionId, parentQuestionIds, cpd));
		}
		
		return true;
	}
	
	/**
	 * Represents a network action for adding a direct dependency (edge) into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestionAssumption(long, Date, long, long, List)
	 */
	public class AddQuestionAssumptionNetworkAction implements NetworkAction {
		private final Long transactionKey;
		private final Date occurredWhen;
		private final long sourceQuestionId;
		private final List<Long> assumptiveQuestionIds;
		private final List<Float> cpd;
		private Date whenExecutedFirst;

		/** Default constructor initializing fields */
		public AddQuestionAssumptionNetworkAction(Long transactionKey,
				Date occurredWhen, long sourceQuestionId,
				List<Long> assumptiveQuestionIds, List<Float> cpd) {
			super();
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.sourceQuestionId = sourceQuestionId;
			if (assumptiveQuestionIds != null) {
				// use a copy, so that changes in the original do not affect this object
				this.assumptiveQuestionIds = new ArrayList<Long>(assumptiveQuestionIds);
			} else {
				this.assumptiveQuestionIds = null;
			}
			if (cpd != null) {
				// use a copy, so that changes in the original do not affect this object
				this.cpd = new ArrayList<Float>(cpd);
			} else {
				this.cpd = null;
			}
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
			throw new UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
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
		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
	}

	/**
	 * Represents an action for adding cash to {@link AssetNetwork} (i.e. increase the min-Q value with a certain amount).
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addCash(long, Date, long, float, String)
	 * @see MarkovEngineImpl#isToAddCashProportionally()
	 * @see MarkovEngineImpl#setToAddCashProportionally(boolean)
	 */
	public class AddCashNetworkAction implements NetworkAction {
		private final Long transactionKey;
		private final Date occurredWhen;
		private final long userId;
		private float delta;	// how much assets were added
		private final String description;
		/** becomes true once {@link #execute()} was called */
		private boolean wasExecutedPreviously = false;
		private Date whenExecutedFirst;
		/** Default constructor initializing fields */
		public AddCashNetworkAction (Long transactionKey, Date occurredWhen, long userId, float assets, String description) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.userId = userId;
			this.delta = assets;
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
				inferenceAlgorithm.addAssets(delta);
			}
			
			this.wasExecutedPreviously = true;
		}
		public void revert() throws UnsupportedOperationException {
			if (wasExecutedPreviously) {
				// undoing a add operation is equivalent to adding the inverse value
				this.delta = -this.delta;
				this.execute();
				// this is not an ordinal execution of the action, so do not update whenExecutedFirst.
			}
		}
		public Date getWhenCreated() { return occurredWhen; }
		/**this operation does not change network structure*/
		public boolean isStructureChangeAction() { return false;	 }
		public Long getTransactionKey() { return transactionKey; }
		public float getAssets() { return delta; }
		public void setAssets(float assets) { this.delta = assets; }
		public Long getUserId() { return userId; }
		public String getDescription() { return description; }
		public List<Float> getPercent() { return null; }
		/** Returns the description */
		public String getTradeId() { return description; }
		public Long getQuestionId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
	}

	
	
	/**
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addCash(long, java.util.Date, long, float, java.lang.String)
	 * @see MarkovEngineImpl#isToAddCashProportionally()
	 * @see MarkovEngineImpl#setToAddCashProportionally(boolean)
	 */
	public boolean addCash(Long transactionKey, Date occurredWhen, long userId, float assets, String description) throws IllegalArgumentException {
		if (Float.compare(0f, assets) == 0) {
			// nothing to add
			return false;
		}
		// check if delta can be translated to asset Q values
//		try {
//			double qValue = this.getQValuesFromScore(assets);
//			if (Double.isInfinite(qValue) || Double.isNaN(qValue)) {
//				throw new IllegalArgumentException("q-value is " + (Double.isInfinite(qValue)?"infinite":"not a number"));
//			}
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Cannot calculate asset's q-value from score = " 
//						+ assets 
//						+ ", log base = " + getCurrentLogBase()
//						+ ", currency constant (b-value) = " + getCurrentCurrencyConstant()
//					, e);
//		}
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		
		// instantiate the action object for adding cash
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new AddCashNetworkAction(transactionKey, occurredWhen, userId, assets, description));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddCashNetworkAction(transactionKey, occurredWhen, userId, assets, description));
		}
		
		
		return true;
	}

	/**
	 * This is the {@link NetworkAction} command
	 * representing {@link MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)}
	 * @author Shou Matsumoto
	 */
	public class AddTradeNetworkAction implements NetworkAction {
		private final Date whenCreated;
		private final Long transactionKey;
		private final String tradeKey;
		private final long userId;
		private final long questionId;
		private List<Float> newValues;
		private List<Long> assumptionIds;
		private final List<Integer> assumedStates;
		private final boolean allowNegative;
//		private Map<IRandomVariable, DoublePrecisionProbabilisticTable> qTablesBeforeTrade;
		private List<Float> oldValues = null;
		private Date whenExecutedFirst;
//		private final List<Integer> originalAssumedStates;
//		private final List<Long> originalAssumptionIds;
		/** Default constructor initializing fields */
		public AddTradeNetworkAction(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, 
				List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) {
			this.transactionKey = transactionKey;
			this.whenCreated = occurredWhen;
			this.tradeKey = tradeKey;
			this.userId = userId;
			this.questionId = questionId;
//			if (originalAssumptionIds != null) {
//				// use a copy, so that changes in the original do not affect this object
//				this.originalAssumptionIds = new ArrayList<Long>(originalAssumptionIds);
//			} else {
//				this.originalAssumptionIds = null;
//			}
//			if (originalAssumedStates != null) {
//				// use a copy, so that changes in the original do not affect this object
//				this.originalAssumedStates = originalAssumedStates;
//			} else {
//				this.originalAssumedStates = null;
//			}
			if (newValues != null) {
				// use a copy, so that changes in the original do not affect this object
				this.newValues = new ArrayList<Float>(newValues);
			} else {
				this.newValues = null;
			}
			if (assumptionIds != null) {
				// use a copy, so that changes in the original do not affect this object
				this.assumptionIds = new ArrayList<Long>(assumptionIds);
			} else {
				this.assumptionIds = null;
			}
			if (assumedStates != null) {
				// use a copy, so that changes in the original do not affect this object
				this.assumedStates = new ArrayList<Integer>(assumedStates);
			} else {
				this.assumedStates = null;
			}
			this.allowNegative = allowNegative;
			
		}
		public void execute() {
			this.execute(true);
		}
		public void execute(boolean isToUpdateAssets) {
			// extract user's asset network from user ID
			AssetAwareInferenceAlgorithm algorithm = null;
			try {
				algorithm = getAlgorithmAndAssetNetFromUserID(userId);
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			if (algorithm == null) {
				throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			}
			
			synchronized (getProbabilisticNetwork()) {
				synchronized (algorithm.getAssetNetwork()) {
					if (!algorithm.getNetwork().equals(getProbabilisticNetwork())) {
						// this should never happen, but some desync may happen.
						Debug.println(getClass(), "[Warning] desync of network detected.");
						algorithm.setNetwork(getProbabilisticNetwork());
					}
					// backup config of assets
					boolean backup = algorithm.isToUpdateAssets();
					algorithm.setToUpdateAssets(isToUpdateAssets);
					// do trade. Since algorithm is linked to actual networks, changes will affect the actual networks
					// 2nd boolean == true := overwrite assumptionIds and assumedStates when necessary
					oldValues = executeTrade(questionId, newValues, assumptionIds, assumedStates, allowNegative, algorithm, true, false);
					algorithm.setToUpdateAssets(backup);	// revert config of assets
					// backup the previous delta so that we can revert this trade
//					qTablesBeforeTrade = algorithm.getAssetTablesBeforeLastPropagation();
					
					// add this question to the mapping of questions traded by the user
					List<Long> questions = getTradedQuestionsMap().get(userId);
					if (questions == null) {
						questions = new ArrayList<Long>();
						getTradedQuestionsMap().put(userId, questions);
					}
					if (!questions.contains(questionId)) {
						questions.add(questionId);
					}
				}
			}
		}
		/** 
		 * As discussed with Dr. Robin Hanson (<a href="mailto:rhanson@gmu.edu">rhanson@gmu.edu</a>) on
		 * 07/29/2012 during the DAGGRE algorithm meeting, the {@link MarkovEngineInterface#revertTrade(long, Date, Date, Long)}
		 * should only set the user's delta into the point prior to when {@link #execute()} was called.
		 * CAUTION: this method is not actually reverting a trade. It is setting
		 * the asset tables to values prior to the execution of {@link #execute()}.
		 * 
		 * @since 2012/07/24 : due to the memory usage, reverting a trade will replay all the history until
		 * this trade. 
		 * 
		 */
		public void revert() throws UnsupportedOperationException {
//			// extract user's asset network from user ID
//			AssetAwareInferenceAlgorithm algorithm = null;
//			try {
//				algorithm = getAlgorithmAndAssetNetFromUserID(userId);
//			} catch (InvalidParentException e) {
//				throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
//			}
//			if (algorithm == null) {
//				throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
//			}
//			synchronized (algorithm.getAssetNetwork()) {
//				// TODO make sure resolved questions are OK
//				// revert clique's q-tables
//				for (Clique clique : algorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//					// current table and previous table are supposed to have same size
//					((DoublePrecisionProbabilisticTable)clique.getProbabilityFunction()).setValues(getqTablesBeforeTrade().get(clique).getValues());
//				}
//				// revert separator's q-tables
//				for (Separator separator : algorithm.getAssetNetwork().getJunctionTree().getSeparators()) {
//					// current table and previous table are supposed to have same size
//					((DoublePrecisionProbabilisticTable)separator.getProbabilityFunction()).setValues(getqTablesBeforeTrade().get(separator).getValues());
//				}
//			}
		}
		public Date getWhenCreated() { return whenCreated; }
		public boolean isStructureChangeAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return userId; }
		public Long getQuestionId() { return questionId; }
		public List<Long> getAssumptionIds() { return assumptionIds; }
		public List<Integer> getAssumedStates() { return assumedStates; }
		public boolean isAllowNegative() { return allowNegative; }
		public List<Float> getPercent() { return oldValues ; }
		public void setOldValues(List<Float> oldValues) { this.oldValues = oldValues;};
		public String getTradeId() { return tradeKey; }
//		/** Mapping from {@link Clique}/{@link Separator} to q-tables before {@link #execute()}*/
//		public Map<IRandomVariable, DoublePrecisionProbabilisticTable> getqTablesBeforeTrade() { return qTablesBeforeTrade; }
		public List<Float> getNewValues() { return newValues; }
		public void setNewValues(List<Float> newValues) { this.newValues = newValues; }
		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
//		/** @return the original values of {@link #getAssumedStates()} (because some may be ignored) */
//		public List<Integer> getOriginalAssumedStates() { return originalAssumedStates; }
//		/** @return the original values of {@link #getAssumptionIds() (because some may be ignored)  */
//		public List<Long> getOriginalAssumptionIds() { return originalAssumptionIds; }
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) throws IllegalArgumentException {
		
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (transactionKey != null && this.getNetworkActionsMap().get(transactionKey) == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		// if this.isToThrowExceptionOnInvalidAssumptions() == false, preview trade will not throw InvalidAssumptionException. 
//		if (!this.isToThrowExceptionOnInvalidAssumptions()) {
//			try {
//				// So we must check whether assumptions are valid or not here
//				if (assumptionIds != null && !assumptionIds.isEmpty() && this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
//					// convert the set of assumptions to a valid set
//					List<Long> oldAssumptionIds = assumptionIds;
//					// note: getMaximumValidAssumptionsSublists will always return at least 1 element
//					assumptionIds = this.getMaximumValidAssumptionsSublists(questionId, assumptionIds, 1).get(0);
//					// change assumedStates accordingly to new assumptionIds
//					assumedStates = this.convertAssumedStates(assumptionIds, oldAssumptionIds, assumedStates);
//				}
//			} catch (InexistingQuestionException e) {
//				Debug.println(getClass(), e.getMessage(), e);
//				// ignore, because questions can be created afterwards
//			}
//		}
		
		// returned value is the same of preview trade
		List<Float> ret = new ArrayList<Float>();
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
		} catch (InexistingQuestionException e) {
			// Perhaps the nodes are still going to be added within the context of this transaction.
			boolean isNodeToBeCreatedWithinTransaction = false;
			List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey); // getNetworkActionsMap() is supposedly a concurrent map
			synchronized (actions) {	// actions is not a concurrent list, so must lock it
				for (NetworkAction action : actions) {
					if ((action instanceof AddQuestionNetworkAction) && (e.getQuestionId() == action.getQuestionId().longValue())) {
						// this action will create the question which was not found.
						isNodeToBeCreatedWithinTransaction = true;
						break;
					}
				}
			}
			if (!isNodeToBeCreatedWithinTransaction) {
				throw e;
			}
		} catch (InvalidAssumptionException e) {
			// If new nodes/edges are added within the same transaction, there are still some chances for the assumptions to become valid.
			// However, it is very hard to check such conditions right now. So, ignore this exception if such chance may occur.
			boolean isToIgnoreThisException = false;
			List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey); // getNetworkActionsMap() is supposedly a concurrent map
			synchronized (actions) {	// actions is not a concurrent list, so must lock it
				for (NetworkAction action : actions) {
					if (action instanceof AddQuestionNetworkAction || action instanceof AddQuestionAssumptionNetworkAction) {
						// there is a small chance for the assumptions to become correct
						isToIgnoreThisException = true;
						break;
						// TODO implement algorithm for anticipating what #getPossibleQuestionAssumptions will return after the commitment this transaction and never ignore InvalidAssumptionException
					}
				}
			}
			if (!isToIgnoreThisException && this.isToThrowExceptionOnInvalidAssumptions()) {
				throw e;
			}
		}
		
		// do not allow trade if the preview results in zero or negative assets and negative assets are not allowed
		if (!allowNegative) {
			for (int i = 0; i < ret.size(); i++) {
				Float asset = ret.get(i);
				if (asset <= 0) {
					if (transactionKey == null) {
						throw new ZeroAssetsException("Asset of state " + i + " of question " + questionId + " went to " + asset);
					} else {
						return null;
					}
				}
			}
		}
		
		// NOTE: preview trade is performed *before* the insertion of a new action into the transaction, 
		// because we only want the transaction to be altered if the preview trade has returned successfully.
		
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			AddTradeNetworkAction newAction = new AddTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues, assumptionIds, assumedStates, allowNegative);
			this.addNetworkAction(transactionKey, newAction);
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for adding trade
			AddTradeNetworkAction newAction = new AddTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues, assumptionIds, assumedStates, allowNegative);
			this.addNetworkAction(transactionKey, newAction);
		}
		
		// return the previewed asset values
		return ret;
	}

	

	/**
	 * This is the {@link NetworkAction} command representing
	 * {@link MarkovEngineImpl#resolveQuestion(long, Date, long, int)}.
	 * @author Shou Matsumoto
	 */
	public class ResolveQuestionNetworkAction implements NetworkAction {
		private final Long transactionKey;
		private final Date occurredWhen;
		private final long questionId;
		private final int settledState;
		private List<Float> marginalWhenResolved;
		private Date whenExecutedFirst;
		/** Default constructor initializing fields */
		public ResolveQuestionNetworkAction (Long transactionKey, Date occurredWhen, long questionId, int settledState) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.questionId = questionId;
			this.settledState = settledState;
			
		}
		public void execute() { 
			this.execute(null); 
		}
		public void execute(Long userId) {
			synchronized (getProbabilisticNetwork()) {
				TreeVariable probNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(questionId));
				if (probNode != null) {	
					// if probNode.hasEvidence(), then it was resolved already
					if (probNode.hasEvidence() && probNode.getEvidence() != settledState) {
						throw new RuntimeException("Attempted to resolve question " + questionId + " to state " + settledState + ", but it was already resolved to " + probNode.getEvidence());
					}
					// do nothing if node is already settled at settledState
					if (probNode.getEvidence() != settledState) {
						
						// extract marginal 
						marginalWhenResolved = new ArrayList<Float>(probNode.getStatesSize());
						for (int i = 0; i < probNode.getStatesSize(); i++) {
							marginalWhenResolved.add(probNode.getMarginalAt(i));
						}
					}
				} else {
					try {
						Debug.println(getClass(), "Node " + questionId + " is not present in the shared Bayes Net.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					// must keep running, because RevertTradeNetworkAction#revert() may be calling this method just to resolve questions in the asset networks
				}
				// TODO revert on some error
				
				// do not release lock to global BN until we change all asset nets
				synchronized (getUserToAssetAwareAlgorithmMap()) {
					Collection<AssetAwareInferenceAlgorithm> usersToChange = null;
					if (userId == null) {
						// update all stored users
						usersToChange = getUserToAssetAwareAlgorithmMap().values();
					} else if (getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
						// update only asset net of the specified user
						usersToChange = Collections.singletonList(getUserToAssetAwareAlgorithmMap().get(userId));
					} else {
						// update nothing
						usersToChange = Collections.emptyList();
					}
					
					synchronized (getDefaultInferenceAlgorithm()) {
						Node node = getProbabilisticNetwork().getNode(Long.toString(questionId));
						if (node != null) {
							getDefaultInferenceAlgorithm().setAsPermanentEvidence(node, settledState, isToDeleteResolvedNode());
						} else {
							throw new InexistingQuestionException("Node " + questionId + " is not present in network.", questionId);
						}
					}
					
					// do not allow getUserToAssetAwareAlgorithmMap() to be changed here. I.e. do not allow new users to be added now
					for (AssetAwareInferenceAlgorithm algorithm : usersToChange) {
						IAssetNetAlgorithm assetAlgorithm = algorithm.getAssetPropagationDelegator();
						synchronized (assetAlgorithm.getAssetNetwork()) {
							Node assetNode = assetAlgorithm.getAssetNetwork().getNode(Long.toString(questionId));
							if (assetNode == null && (assetAlgorithm instanceof AssetPropagationInferenceAlgorithm)) {
								// in this algorithm, setAsPermanentEvidence will only use assetNode for name comparison and to check size of states, 
								// so we can try using a stub node
								assetNode = new Node() {	// a node just for purpose of searching nodes in lists
									public int getType() { return Node.DECISION_NODE_TYPE; }	 // not important, but mandatory because this is abstract method
									public String getName() { return Long.toString(questionId); }// important for search
									public int getStatesSize() { return Integer.MAX_VALUE; }	 // important for state size consistency check
								};
							}
							if (assetNode != null) {
								assetAlgorithm.setAsPermanentEvidence(assetNode, settledState,isToDeleteResolvedNode());
//								if (!isToDeleteResolvedNode()) {
//									// delete only from list 
//									assetAlgorithm.getAssetNetwork().getNodeIndexes().remove(assetNode.getName());
//									assetAlgorithm.getAssetNetwork().getNodes().remove(assetNode);
//									assetAlgorithm.getAssetNetwork().getNodesCopy().remove(assetNode);
//								}
							} else {
								try {
//									Debug.println(getClass(), "Node " + questionId + " is not present in asset net of user " + assetAlgorithm.getAssetNetwork());
									throw new InexistingQuestionException("Node " + questionId + " is not present in asset net of user " + assetAlgorithm.getAssetNetwork(), questionId);
								} catch (Throwable t) {
									t.printStackTrace();
								}
							}
						}
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
		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#resolveQuestion(long, java.util.Date, long, int)
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
			this.addNetworkAction(transactionKey, new ResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settledState));
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for adding a question
			this.addNetworkAction(transactionKey, new ResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settledState));
		}
		
		return true;
	}
	
//	/**
//	 * This is the {@link NetworkAction} command representing
//	 * {@link MarkovEngineImpl#revertTrade(long, Date, Date, Long)}
//	 * @author Shou Matsumoto
//	 */
//	public class RevertTradeNetworkAction implements NetworkAction {
//		private final Date occurredWhen;
//		private final Date tradesStartingWhen;
//		private final Long transactionKey;
//		private final Long questionId;
//		private List<Float> probBeforeRevert = null;
//		private Date whenExecutedFirst;
//		/** Default constructor initializing fields */
//		public RevertTradeNetworkAction (Long transactionKey, Date occurredWhen,  Date tradesStartingWhen, Long questionId) {
//			this.transactionKey = transactionKey;
//			this.occurredWhen = occurredWhen;
//			this.tradesStartingWhen = tradesStartingWhen;
//			this.questionId = questionId;
//		}
//		/**
//		 * This method just calls the {@link AddTradeNetworkAction#revert()} of the
//		 * first instance of {@link AddTradeNetworkAction} of question {@link #getQuestionId()}, 
//		 * executed after {@link #getTradesStartingWhen()}, for each possible user.
//		 * {@link AddTradeNetworkAction#revert()} is supposed to set user's assets to the values
//		 * prior to that edit.
//		 * It will read the network actions in {@link MarkovEngineImpl#getExecutedActions()} 
//		 * (i.e. only the actions actually executed) and will delete values from
//		 * {@link MarkovEngineImpl#getExecutedActions()} to indicate that some
//		 * actions will not be considered as \"executed\" anymore.
//		 * The history is not affected, because {@link MarkovEngineImpl#getNetworkActionsIndexedByQuestions()}
//		 * is not altered.
//		 */
//		public void execute() {
//			// fill probBeforeRevert
//			if (questionId != null) {
//				boolean backup = isToObtainProbabilityOfResolvedQuestions();
//				setToObtainProbabilityOfResolvedQuestions(true);
//				probBeforeRevert = getProbList(questionId, null, null);
//				setToObtainProbabilityOfResolvedQuestions(backup);
//			} 
//			/*
//			 * NOTE: it is not guaranteed that actions in different transactions were executed in the same ordering/sequence of action.getWhenCreated()
//			 * (it is only guaranteed that actions in same transaction were executed in the same ordering/sequence of action.getWhenCreated()).
//			 * The argument tradesStartingWhen is supposedly a filter for action.getWhenCreated(), but
//			 * for coherence, we must mainly consider the ordering of actual execution (because such ordering is
//			 * what impacts the final values of the shared Bayes net and user's assets).
//			 * That's why I'm searching for the first action in getExecutedActions() whose  tradesStartingWhen < action.getWhenCreated()
//			 * and then replaying the actions prior to it.
//			 */
//			synchronized (getUserToAssetAwareAlgorithmMap()) {
//				// do not allow new users to be created now
//				synchronized (getExecutedActions()) {
//					// actions to call revert()
//					List<AddTradeNetworkAction> tradeActionsToRevert = new ArrayList<AddTradeNetworkAction>();	
//					
//					// will store what users were already processed, and what questions should be resolved again after reverting trades
//					Map<Long, List<ResolveQuestionNetworkAction>> treatedUserToActionsToResolveMap = new HashMap<Long, List<ResolveQuestionNetworkAction>>();
//					
//					for (NetworkAction action : getExecutedActions()) {
//						if (questionId != null  && !questionId.equals(action.getQuestionId())) {
//							// ignore actions not matching question ID
//							continue;
//						}
//						// If date of action is after tradesStartingWhen
//						if (action.getWhenCreated().compareTo(tradesStartingWhen) >= 0 ) {
//							// If treatedUserToActionsToResolveMap.size() >= getUserToAssetAwareAlgorithmMap().size(), 
//							// then all users were already treated. So, there's no need to fill tradeActionsToRevert anymore.
//							if (treatedUserToActionsToResolveMap.size() < getUserToAssetAwareAlgorithmMap().size()) { 
//								if ( !treatedUserToActionsToResolveMap.containsKey(action.getUserId())	// action of a user not processed yet
//										&& action instanceof AddTradeNetworkAction) {					// action is a trade
//									tradeActionsToRevert.add((AddTradeNetworkAction)action);
//									// init this map, which stores processed users and list of questions to resolve again (i.e. all resolutions coming after this action).
//									treatedUserToActionsToResolveMap.put(action.getUserId(), new ArrayList<MarkovEngineImpl.ResolveQuestionNetworkAction>());
//								}
//							} 
//						}
//						// If executed after any AddTradeNetworkAction, the ResolveQuestionNetworkAction must be executed regardless of its value of getWhenCreated()
//						if (!treatedUserToActionsToResolveMap.isEmpty() && action instanceof ResolveQuestionNetworkAction) {
//							// fill values of treatedUserToActionsToResolveMap with all resolutions executed after the AddTradeNetworkAction to be reverted.
//							for (Long userId : treatedUserToActionsToResolveMap.keySet()) {
//								treatedUserToActionsToResolveMap.get(userId).add((ResolveQuestionNetworkAction) action);
//							}
//						}
//					}
//					
//					// execute revert() so that user's assets are reverted to that moment 
//					for (AddTradeNetworkAction action : tradeActionsToRevert) {
//						action.revert();
//					}
//					
//					// AddTradeNetworkAction#revert() will bring asset tables to the moment before the trade, including its structure. 
//					// Questions must be resolved again in order to synchronize the structure of asset nets and shared Bayes net.
//					for (Long userId : treatedUserToActionsToResolveMap.keySet()) {
//						for (ResolveQuestionNetworkAction action : treatedUserToActionsToResolveMap.get(userId)) {
//							action.execute(userId);	// resolve question only for this user
//						}
//					}
//				}
//			}
//			/*
//			 * Note: we should not remove the "reverted" trades from getExecutedActions(), because 
//			 * the revert is only reseting the assets of the users.
//			 * In order for the probabilities to be restorable from history, all trades
//			 * including the reverted ones should be part of the getExecutedActions().
//			 */
//		}
//		public void revert() throws UnsupportedOperationException {
//			throw new UnsupportedOperationException("Current version cannot revert a \"revert\" operation.");
//		}
//		public Date getWhenCreated() { return occurredWhen; }
//		public List<Float> getPercent() { return probBeforeRevert; }
//		public String getTradeId() { return null; }
//		public boolean isStructureChangeAction() { return false; }
//		public Long getTransactionKey() { return transactionKey; }
//		public Long getUserId() { return null; }
//		public Long getQuestionId() { return questionId; }
//		public List<Long> getAssumptionIds() { return null; }
//		public List<Integer> getAssumedStates() { return null; }
//		public Date getTradesStartingWhen() { return tradesStartingWhen; }
//		public Date getWhenExecutedFirstTime() {return whenExecutedFirst ; }
//		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { this.whenExecutedFirst = whenExecutedFirst; }
//	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#revertTrade(Long, java.util.Date, java.lang.Long, java.lang.Long)
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
			this.addNetworkAction(transactionKey, new RevertTradeNetworkAction(transactionKey, occurredWhen, tradesStartingWhen, questionId));
			this.commitNetworkActions(transactionKey);
		} else {
			// add action into transaction
			this.addNetworkAction(transactionKey, new RevertTradeNetworkAction(transactionKey, occurredWhen, tradesStartingWhen, questionId));
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		return this.getProbList(questionId, assumptionIds, assumedStates, true);
	}
	
	/**
	 * This method offers the same functionality of {@link #getProbList(long, List, List)}, but
	 * there is an additional argument to indicate whether or not to normalize the result.
	 * @param isToNormalize : if false, the returned list will contain the clique potentials without normalization.
	 * This may be useful for obtaining the clique potentials instead of the conditional probabilities.
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 * @see #getProbList(long, List, List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, boolean isToNormalize) throws IllegalArgumentException {
		Map<Long, List<Float>> probLists = this.getProbLists(Collections.singletonList(questionId), assumptionIds, assumedStates, isToNormalize);
		if (probLists != null) {
			return probLists.get(questionId);
		}
		return null;
	}
		
			
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbLists(java.util.List, java.util.List, java.util.List)
	 */
	public Map<Long,List<Float>> getProbLists(List<Long> questionIds, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		return this.getProbLists(questionIds, assumptionIds, assumedStates, true);
	}

	/**
	 * This method offers the same functionality of {@link #getProbLists(List, List, List)}, but
	 * there is an additional argument to indicate whether or not to normalize the result.
	 * @param isToNormalize : if false, the returned list will contain the clique potentials without normalization.
	 * This may be useful for obtaining the clique potentials instead of the conditional probabilities.
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 */
	public Map<Long,List<Float>> getProbLists(List<Long> questionIds, List<Long>assumptionIds, List<Integer> assumedStates, boolean isToNormalize) throws IllegalArgumentException {
		
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
		
		// this is the object to be returned
		Map<Long,List<Float>> ret = new HashMap<Long, List<Float>>();
		
		// if assumptions are not in same clique, we can still use BN propagation to obtain probabilities.
		ProbabilisticNetwork netToUseWhenAssumptionsAreNotInSameClique = null;	// this is going to be a clone of the shared BN
		List<PotentialTable> cptList = new ArrayList<PotentialTable>();	// will contain conditional probability within clique
		
		// if we need to get probabilities of all nodes, and there are assumptions, then we need propagation anyway
		if (questionIds == null && assumptionIds != null && assumedStates != null && !assumptionIds.isEmpty() && !assumedStates.isEmpty()) {
			// netToUseWhenAssumptionsAreNotInSameClique != null if we need propagation
			netToUseWhenAssumptionsAreNotInSameClique = getDefaultInferenceAlgorithm().cloneProbabilisticNetwork(getProbabilisticNetwork());
		} else {
			// first, attempt to fill cptList with conditional probabilities that can be calculated without propagation.
			int howManyIterations = getProbabilisticNetwork().getNodeCount();
			if (questionIds != null && !questionIds.isEmpty()) {
				howManyIterations = questionIds.size();
			}
			for (int i = 0; i < howManyIterations; i++) {
				synchronized (getProbabilisticNetwork()) {
					INode mainNode = null;
					Long questionId = null;
					if (questionIds != null && !questionIds.isEmpty()) {
						questionId = questionIds.get(i);
						mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
					} else {
						mainNode = getProbabilisticNetwork().getNodeAt(i);
						questionId = Long.parseLong(mainNode.getName());
					}
					if (mainNode == null || questionId == null) {
						boolean isResolvedQuestion = false;
						if (isToObtainProbabilityOfResolvedQuestions()) {
							// If it is a resolved question, we can get marginal (non-assumptive) probabilities from history.
							// TODO also build conditional probability
							try {
								Debug.println(getClass(), "Node " + questionId + " was not found. Retrieving marginal probability from history...");
							} catch (Throwable t) {
								t.printStackTrace();
							}
							// Retrieve from history
							List<QuestionEvent> history = getQuestionHistory(questionId, null, null);
							// search from the end, because resolutions are mostly at the end of the history
							for (int j = history.size()-1; j >= 0; j--) {
								if (history.get(j) instanceof ResolveQuestionNetworkAction) {
									ResolveQuestionNetworkAction action = (ResolveQuestionNetworkAction) history.get(j);
									// build probability of resolution (1 state is 100% and others are 0%)
									int stateCount = action.getPercent().size();	// retrieve quantity of states == size of marginal probability
									List<Float> marginal = new ArrayList<Float>(stateCount);	// list of marginal probs
									for (int k = 0; k < stateCount; k++) {
										// set the settled state as 100%, and all others to 0%
										marginal.add((k==action.getSettledState())?1f:0f);
									}
									ret.put(questionId, marginal);
									isResolvedQuestion = true;
									break;
								}
							}
						}
						if (isResolvedQuestion) {
							continue;
						}
						throw new InexistingQuestionException("Question " + questionId + " not found", questionId);
					}
					List<INode> parentNodes = new ArrayList<INode>();
					if (assumptionIds != null) {
						for (Long id : assumptionIds) {
							INode node = getProbabilisticNetwork().getNode(Long.toString(id));
							if (node == null) {
								throw new InexistingQuestionException("Question " + questionId + " not found", questionId);
							}
							parentNodes.add(node);
						}
					}
					if (isToNormalize) {
						try {
							cptList.add((PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, getProbabilisticNetwork(), null));
						} catch (NoCliqueException e) {
							Debug.println(getClass(), "Could not extract potentials within clique. Trying global propagation.", e);
							netToUseWhenAssumptionsAreNotInSameClique = getDefaultInferenceAlgorithm().cloneProbabilisticNetwork(getProbabilisticNetwork());
							break;	// do not fill cptList anymore, because if we need to do 1 propagation anyway, the computational cost if the same for any quantity of nodes to propagate
						}
					} else {
						// by specifying a non-normalized junction tree algorithm to conditionalProbabilityExtractor, we can force it not to normalize the result
						cptList.add((PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, getProbabilisticNetwork(), getDefaultInferenceAlgorithm().getAssetPropagationDelegator()));
					}
				}	// unlock prob network
			}	// end of loop
		}
		
		// If we need to do propagation, do it in non-critical portion of code
		if (netToUseWhenAssumptionsAreNotInSameClique != null) {
			return this.previewProbPropagation(questionIds, assumptionIds, assumedStates, netToUseWhenAssumptionsAreNotInSameClique);
		}
		
		// at this point of code, all probabilities can be estimated from cptList
		for (PotentialTable cpt : cptList) {
			List<Float> marginal = new ArrayList<Float>();
			// convert cpt to a list of float (marginal), given assumedStates.
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
					marginal.add(cpt.getValue(i));
				}
			}
			ret.put(Long.parseLong(cpt.getVariableAt(0).getName()), marginal);	// cpt.getVariableAt(0) is the main (child) node of the conditional prob
		}
		return ret;
		
	}


	/**
	 * It uses findingNodeIDs and findingStates to set and propagate findings
	 * in pn, and return the marginal of node identified by filterNodes
	 * @param filterNodes : only marginals of nodes in this list will be returned.
	 * If null, marginals of all nodes will be returned.
	 * @param findingNodeIDs : nodes to add findings
	 * @param findingStates : findings of findingNodes
	 * @param pn : network to consider. Use a clone of {@link #getProbabilisticNetwork()} if you don't want
	 * the shared BN to be changed.
	 * @return mapping from node ID to marginals of nodes
	 */
	protected Map<Long, List<Float>> previewProbPropagation(List<Long> filterNodes, List<Long> findingNodeIDs, List<Integer> findingStates, ProbabilisticNetwork pn) {
		// make arguments are not null
		if (pn == null) {
			throw new NullPointerException("ProbabilisticNetwork cannot be null");
		}
		if (findingNodeIDs == null) {
			findingNodeIDs = Collections.emptyList();
		}
		if (findingStates == null) {
			findingStates = Collections.emptyList();
		}
		
		// propagate findings only when there are findings to propagate
		if (!findingNodeIDs.isEmpty() && !findingStates.isEmpty()) {
			JunctionTreeAlgorithm jtAlgorithm = new JunctionTreeAlgorithm(pn);
			if (jtAlgorithm.getInferenceAlgorithmListeners() != null) {
				// delete any extra operation performed prior and after compilation/propagation/reset of the network,
				// because we only need to perform propagation from current state (we never do initialization or finalization)
				jtAlgorithm.getInferenceAlgorithmListeners().clear();
			}
			// fill findings
			for (int i = 0; i < findingNodeIDs.size(); i++) {
				TreeVariable findingNode = (TreeVariable) pn.getNode(Long.toString(findingNodeIDs.get(i)));
				if (findingNode == null) {
					throw new InexistingQuestionException("Node" + findingNodeIDs.get(i) + " does not exist.", findingNodeIDs.get(i));
				}
				if (i < findingStates.size()) {
					// set evidence in the probabilistic network
					if (findingStates.get(i) < 0) {
						// set finding as negative (i.e. finding setting a state to 0%)
						findingNode.addFinding(Math.abs(findingStates.get(i)+1), true);
					} else {
						findingNode.addFinding(findingStates.get(i));
					}
				}
			}
			// propagate finding
			jtAlgorithm.propagate();
		}
		
		// this is the value to be returned
		Map<Long, List<Float>> ret = new HashMap<Long, List<Float>>();
		
		// iterate over nodes to extract marginal
		int sizeOfRet = 0;
		if (filterNodes != null && !filterNodes.isEmpty()) {
			sizeOfRet = filterNodes.size();
		} else {
			sizeOfRet = pn.getNodeCount();
		}
		for (int i = 0; i < sizeOfRet; i++) {
			// extract node to be used to extract marginal probabilities
			TreeVariable mainNode = null;
			if (filterNodes != null && !filterNodes.isEmpty()) {
				mainNode = (TreeVariable) pn.getNode(Long.toString(filterNodes.get(i)));
			} else {
				mainNode = (TreeVariable) pn.getNodeAt(i);
			}
			if (mainNode == null) {
				throw new InexistingQuestionException("Could not extract some of the nodes from the network: " + filterNodes + ", index " + i, null); 
			}
			
			
			// extract marginal probabilities
			List<Float> marginal = new ArrayList<Float>(mainNode.getStatesSize());
			for (int j = 0; j < mainNode.getStatesSize(); j++) {
				marginal.add(mainNode.getMarginalAt(j));
			}
			
			// the ID and the name of the node are supposedly the same
			ret.put(Long.parseLong(mainNode.getName()), marginal);
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
		if (getProbabilisticNetwork() == null) {
			// there is no way to find a node from a null network
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
		}
		// extract main node
		Node mainNode = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
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
					throw new InexistingQuestionException("Question " + id + " not found.", id);
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
		
		// extract user's asset network from user ID
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.", e);
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		return this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false);	// false := return assets instead of q-values
	}
	
	/**
	 * This method is used in {@link #getAssetsIfStates(long, long, List, List)} and {@link #previewTrade(long, long, List, List, List)}
	 * in order to extract the conditional delta from the {@link AssetNetwork} related to a given algorithm.
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param assumptionIds : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The order
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * @param algorithm : the algorithm to be used in order to extract info from {@link AssetNetwork}. 
	 * {@link AssetAwareInferenceAlgorithm#getAssetNetwork()} is used in order to extract the instance of {@link AssetNetwork}.
	 * @param isToReturnQValuesInsteadOfAssets : if false, the returned list will contain values returned by {@link #getScoreFromQValues(float)} 
	 * (i.e. logarithm values, instead of the q-values stored in the asset tables). 
	 * If true, the returned list will contain q-values (values actually stored in the delta table) instead of what the DAGGRE side call "delta" (the logarithm values).
	 * @return the change in user delta if a given states occurs if the specified assumptions are met. 
	 * The indexes are relative to the indexes of the states.
	 * In the case of a binary question this will return a [if_true, if_false] value, if multiple choice will return a [if_0, if_1, if_2...] value list
	 * For example, assuming that the question identified by questionId is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the delta of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the delta of the
	 * question while it is in state "true".
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	protected List getAssetsIfStates(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, AssetAwareInferenceAlgorithm algorithm, boolean isToReturnQValuesInsteadOfAssets)
			throws IllegalArgumentException {
		// basic assertion
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm cannot be null");
		}
		PotentialTable assetTable = null;	// asset tables (clique table containing delta) are instances of potential tables
		synchronized (algorithm.getAssetNetwork()) {
			AssetNode mainNode = (AssetNode) algorithm.getAssetNetwork().getNode(Long.toString(questionId));
			if (mainNode == null) {
				throw new InexistingQuestionException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork(), questionId);
			}
			List<INode> parentNodes = new ArrayList<INode>();
			if (assumptionIds != null) {
				for (Long id : assumptionIds) {
					INode node = algorithm.getAssetNetwork().getNode(Long.toString(id));
					if (node == null) {
						throw new InexistingQuestionException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork(), questionId);
					}
					parentNodes.add(node);
				}
			}
			if (parentNodes.isEmpty()) { // we are not calculating the conditional delta. We are calculating delta of 1 node only (i.e. "marginal" delta)
				boolean backup = mainNode.isToCalculateMarginal();	// backup old config
				mainNode.setToCalculateMarginal(true);		// force marginalization to calculate something.
//				double[] marginal = mainNode.(); 					
//				mainNode.setToCalculateMarginal(backup);	// revert to previous config
//				List ret = new ArrayList(mainNode.getStatesSize());
//				for (int i = 0; i < mainNode.getStatesSize(); i++) {
//					if (isToReturnQValuesInsteadOfAssets) {
//						// return q-values directly
//						ret.add(marginal[i]);
//					} else {
//						// convert q-values to delta (i.e. logarithmic values)
//						ret.add((float) this.getScoreFromQValues(marginal[i]));
//					}
//				}
//				return ret;
				mainNode.updateMarginal(); 					// make sure values of mainNode.getMarginalAt(index) is up to date
				mainNode.setToCalculateMarginal(backup);	// revert to previous config
			}
			if (conditionalProbabilityExtractor instanceof InCliqueConditionalProbabilityExtractor) {
				InCliqueConditionalProbabilityExtractor inCliqueConditionalProbabilityExtractor = (InCliqueConditionalProbabilityExtractor) conditionalProbabilityExtractor;
				assetTable = (PotentialTable) inCliqueConditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, algorithm.getAssetNetwork(), algorithm.getAssetPropagationDelegator(), ASSET_CLIQUE_EVIDENCE_UPDATER);
			} else {
				throw new UnsupportedOperationException("getConditionalProbabilityExtractor() with instance other than " 
						+ InCliqueConditionalProbabilityExtractor.class.getName() + " is not supported by this version.");
			}
		}
		
		// convert cpt to a list, given assumedStates.
		// TODO change the way it sum-out/min-out/max-out the unspecified states in the clique potential
		List ret = new ArrayList(assetTable.tableSize());
		for (int i = 0; i < assetTable.tableSize(); i++) {
			boolean isToSkip = false;
			// filter entries which are incompatible with assumedStates
			if (assumedStates != null && !assumedStates.isEmpty()) {
				// extract coordinate of the states (e.g. [2,1,0] means state of mainNode = 2, state of parent1 = 1, and parent2 == 0)
				int[] multidimensionalCoord = assetTable.getMultidimensionalCoord(i);
				// note: size of assumedStates is 1 unit smaller than multidimensionalCoord, because assumedStates does not contain the main node
				if (multidimensionalCoord.length != assumedStates.size() + 1) {
					throw new RuntimeException("Multi dimensional coordinate of index " + i + " has size " + multidimensionalCoord.length
							+ ". Expected " + (assumedStates.size()+1));
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
					if (algorithm.isToUseQValues()) {
						ret.add(assetTable.getValue(i));
					} else {
						ret.add(this.getQValuesFromScore(assetTable.getValue(i)));
					}
				} else {
					// convert q-values to delta (i.e. logarithmic values)
					if (algorithm.isToUseQValues()) {
						ret.add(this.getScoreFromQValues(assetTable.getValue(i)));
					} else {
						ret.add(assetTable.getValue(i));
					}
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
			throw new InexistingQuestionException("assumptionIds contains null ID.", null);
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
		TreeVariable mainNode = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(questionId));
			if (mainNode == null) {
				throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
			}
			if (mainNode.hasEvidence()) {
				throw new IllegalArgumentException("Question " + mainNode + " is already resolved and cannot be changed.");
			}
			for (int i = 0; i < mainNode.getStatesSize(); i++) {
				if (mainNode.getMarginalAt(i) == 0.0f || mainNode.getMarginalAt(i) == 1.0f) {
					throw new IllegalArgumentException("State " + i + " of question " + mainNode + " has probability " + mainNode.getMarginalAt(i) + " and cannot be changed.");
				}
			}
		}
		// extract assumption nodes
		List<INode> assumptions = new ArrayList<INode>();
		if (assumptionIds != null) {
			for (Long id : assumptionIds) {
				TreeVariable node = null;
				synchronized (getProbabilisticNetwork()) {
					node = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(id));
					if (node == null) {
						throw new InexistingQuestionException("Question " + id + " not found.", id);
					}
					if (node.hasEvidence()) {
						throw new IllegalArgumentException("Question " + node + " is already resolved and cannot be changed.");
					}
					for (int i = 0; i < node.getStatesSize(); i++) {
						if (node.getMarginalAt(i) == 0.0f || node.getMarginalAt(i) == 1.0f) {
							throw new IllegalArgumentException("State " + i + " of question " + node + " has probability " + node.getMarginalAt(i) + " and cannot be changed.");
						}
					}
				}
				assumptions.add(node);
			}
		}
		
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
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
			throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		float ret = Float.NEGATIVE_INFINITY;	// value to return
		synchronized (algorithm.getAssetNetwork()) {
			Map<INode, Integer> conditions = new HashMap<INode, Integer>();
			// set up findings
			if (assumptionIds != null) {
				for (int i = 0; i < assumptionIds.size(); i++) {
					AssetNode node = (AssetNode) algorithm.getAssetNetwork().getNode(Long.toString(assumptionIds.get(i)));
					if (node == null) {
						throw new InexistingQuestionException("Question " + assumptionIds.get(i) + " does not exist.", assumptionIds.get(i));
					}
					Integer stateIndex = assumedStates.get(i);
					if (stateIndex != null) {
						// set evidence in the probabilistic network
						conditions.put(node, stateIndex);
					}
				}
			}
			// run only min-propagation (i.e. calculate min-q given assumptions)
			algorithm.runMinPropagation(conditions);
			// obtain min-q value and explanation (states which cause the min-q values)
			// TODO use the explanation (set of states that causes the min assets) for something
			try {
				ret = algorithm.calculateExplanation(null);	// by offering null, only min value is calculated (the states are not retrieved)
			} catch (RuntimeException e) {
				algorithm.undoMinPropagation();
				throw e;
			}
			// undo min-propagation, because the next iteration of asset updates should be based on non-min delta
			algorithm.undoMinPropagation();	
		}
		
		if (algorithm.isToUseQValues()) {
			// convert q-values to score and return
			return getScoreFromQValues(ret);
		}
		
		return ret;
	}


	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public float scoreUserQuestionEv(long userId, Long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		List<Float> evPerStates = this.scoreUserQuestionEvStates(userId, questionId, assumptionIds, assumedStates);
		if (evPerStates == null || evPerStates.isEmpty()) {
			throw new RuntimeException("Failed to obtain estimated values.");
		}
		float sum = 0;
		for (Float ev : evPerStates) {
			sum += ev;
		}
		return sum;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEvStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// initial assertion: if assumptions were specified and states were not specified
		if (assumptionIds != null && assumedStates != null && assumedStates.size() != assumptionIds.size()) {
			throw new IllegalArgumentException("Expected size of assumedStates is " + assumptionIds.size() + ", but was " + assumedStates.size());
		}
		// obtain the main node
		INode node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (node == null) {
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
		}
		
		// list to return
		List<Float> ret = new ArrayList<Float>(node.getStatesSize());
		
		// this is a non-null list of assumptions to pass to scoreUserEv (assumptionIds & questionId)
		List<Long> assumptionsIncludingThisQuestion = new ArrayList<Long>();
		// similarly, non-null list of states to pass to scoreUserEv (assumedStates and states of questionId)
		List<Integer> assumedStatesIncludingThisState = new ArrayList<Integer>();	// do not reuse 
		if (assumptionIds != null) {
			assumptionsIncludingThisQuestion.addAll(assumptionIds);
		}
		if (assumedStates != null) {
			assumedStatesIncludingThisState.addAll(assumedStates);
		}
		
		// if questionId is in assumption, this var will have the index of questionId in assumptionsIncludingThisQuestion
		int indexOfThisQuestion = assumptionsIncludingThisQuestion.indexOf(questionId);
		
		// if we assume the question itself, do different operations
		if (indexOfThisQuestion < 0) {	// questionId is not assumed
			
			// the last element in the list will contain the question itself
			assumptionsIncludingThisQuestion.add(questionId);
			assumedStatesIncludingThisState.add(0);
			
			// just calculate conditional expected score given each state of questionId... Use assumptionsIncludingThisQuestion and states
			for (int i = 0; i < node.getStatesSize(); i++) {
				// TODO optimize
				assumedStatesIncludingThisState.set(assumedStatesIncludingThisState.size()-1, i);
				ret.add(this.scoreUserEv(userId, assumptionsIncludingThisQuestion, assumedStatesIncludingThisState));
			}
		} else {	// questionId is assumed
//			throw new IllegalArgumentException("Assumptions must not contain the question itself.");
			Integer assumedStateOfThisQuestion = assumedStatesIncludingThisState.get(indexOfThisQuestion);
			// calculate conditional expected score only for the evidence state, and the other states will have 0
			for (int i = 0; i < node.getStatesSize(); i++) {
				// TODO optimize
				if (i == assumedStateOfThisQuestion) {
					// the assumed state will have the value added
					ret.add(this.scoreUserEv(userId, assumptionsIncludingThisQuestion, assumedStatesIncludingThisState));
				} else {
					// other states will have expected value = 0f
					ret.add(0f);
				}
			}
		}

		
		return ret;
	}
	
	/**
	 * This method performs the same idea of {@link #scoreUserQuestionEvStates(long, long, List, List)},
	 * but instead of calculating the expected score per state, it calculates the cash (min assets)
	 * per state of a question.
	 * @param userId
	 * @param questionId
	 * @param assumptionIds
	 * @param assumedStates
	 * @return cash (min assets) per state of a question.
	 * @throws IllegalArgumentException
	 */
	public List<Float> getCashPerStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// initial assertion: if assumptions were specified and states were not specified
		if (assumptionIds != null && assumedStates != null && assumedStates.size() != assumptionIds.size()) {
			throw new IllegalArgumentException("Expected size of assumedStates is " + assumptionIds.size() + ", but was " + assumedStates.size());
		}
		// obtain the main node
		INode node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (node == null) {
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
		}
		
		// list to return
		List<Float> ret = new ArrayList<Float>(node.getStatesSize());
		
		// this is a non-null list of assumptions to pass to scoreUserEv (assumptionIds & questionId)
		List<Long> assumptionsIncludingThisQuestion = new ArrayList<Long>();
		// similarly, non-null list of states to pass to scoreUserEv (assumedStates and states of questionId)
		List<Integer> assumedStatesIncludingThisState = new ArrayList<Integer>();	// do not reuse 
		if (assumptionIds != null) {
			assumptionsIncludingThisQuestion.addAll(assumptionIds);
		}
		if (assumedStates != null) {
			assumedStatesIncludingThisState.addAll(assumedStates);
		}
		
		// if questionId is in assumption, this var will have the index of questionId in assumptionsIncludingThisQuestion
		int indexOfThisQuestion = assumptionsIncludingThisQuestion.indexOf(questionId);
		
		// if we assume the question itself, do different operations
		if (indexOfThisQuestion < 0) {	// questionId is not assumed
			
			// the last element in the list will contain the question itself
			assumptionsIncludingThisQuestion.add(questionId);
			assumedStatesIncludingThisState.add(0);
			
			// just calculate conditional expected score given each state of questionId... Use assumptionsIncludingThisQuestion and states
			for (int i = 0; i < node.getStatesSize(); i++) {
				// TODO optimize
				assumedStatesIncludingThisState.set(assumedStatesIncludingThisState.size()-1, i);
				ret.add(this.getCash(userId, assumptionsIncludingThisQuestion, assumedStatesIncludingThisState));
			}
		} else {	// questionId is assumed
//			throw new IllegalArgumentException("Assumptions must not contain the question itself.");
			Integer assumedStateOfThisQuestion = assumedStatesIncludingThisState.get(indexOfThisQuestion);
			// calculate conditional expected score only for the evidence state, and the other states will have 0
			for (int i = 0; i < node.getStatesSize(); i++) {
				// TODO optimize
				if (i == assumedStateOfThisQuestion) {
					// the assumed state will have the value added
					ret.add(this.getCash(userId, assumptionsIncludingThisQuestion, assumedStatesIncludingThisState));
				} else {
					// other states will have expected value = 0f
					ret.add(0f);
				}
			}
		}
		
		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserEv(long, java.util.List, java.util.List)
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		return this.scoreUserEv(userId, assumptionIds, assumedStates, null);
	}
	
	/**
	 * This method performs what is described in {@link #scoreUserEv(long, List, List)},
	 * but we can specify a list of listeners which will be notified
	 * when cells of asset tables and probability tables are multiplied
	 * during invocation of {@link AssetAwareInferenceAlgorithm#calculateExpectedAssets()}.
	 * @param assetCellListener : the list of listeners to be notified.
	 * {@link AssetAwareInferenceAlgorithm#setExpectedAssetCellListeners(List)} will be called passing
	 * this argument.
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserEv(long, java.util.List, java.util.List)
	 * @see AssetAwareInferenceAlgorithm#getExpectedAssetCellListeners()
	 * @see AssetAwareInferenceAlgorithm#calculateExpectedAssets()
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates, List<ExpectedAssetCellMultiplicationListener> assetCellListener) throws IllegalArgumentException {
		
		// this map will contain names of nodes identified by assumptionIds and the respective states in assumedStates
		Map<String, Integer> nodeNameToStateMap = new HashMap<String, Integer>();
		
		// check consistency of the assumption nodes
		if (assumptionIds != null) {
			for (int i = 0; i < assumptionIds.size(); i++) {
				Long id = assumptionIds.get(i);
				INode assumption = null;
				synchronized (getProbabilisticNetwork()) {
					assumption = getProbabilisticNetwork().getNode(Long.toString(id));
					if (assumption == null) {
						throw new InexistingQuestionException("Question " + id + " not found.", id);
					}
					// check state consistency
					if ( (assumedStates != null)  && (i < assumedStates.size()) ) {
						Integer state = assumedStates.get(i);
						if (state != null) {
							if (state < 0 || state >= assumption.getStatesSize()) {
								throw new IllegalArgumentException("Question " + id + " has no state " + assumedStates.get(i));
							}
							// add only nodes and states which are OK
							nodeNameToStateMap.put(assumption.getName(), state);
						}
					}
				}
			}
		}
		
		AssetAwareInferenceAlgorithm origAlgorithm;
		try {
			origAlgorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Failed to initialize user " + userId, e);
		}
		
		// if there are assumptions, we must clone the network. If not, we can use the original network
		if (!nodeNameToStateMap.isEmpty()) {
			AssetAwareInferenceAlgorithm clonedAlgorithm;
			try {
				synchronized (origAlgorithm.getNetwork()) {
					clonedAlgorithm = (AssetAwareInferenceAlgorithm) origAlgorithm.clone(false);
				}
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(this.getClass().getName() + " - Failed to clone Bayes Net during calculation of expected assets of user "
						+ userId + ", given conditions " + assumptionIds + " = " + assumedStates, e);
			}
			
			// force cloned algorithm not to do some unnecessary things, like updating assets
			clonedAlgorithm.setToLogAssets(false);
			clonedAlgorithm.setToUpdateAssets(false);
			clonedAlgorithm.setToPropagateForGlobalConsistency(false);
			clonedAlgorithm.setToAllowZeroAssets(true);
			clonedAlgorithm.setToCalculateMarginalsOfAssetNodes(false);
			
			// the asset network is not going to change, so we cal use the original
			clonedAlgorithm.setAssetNetwork(origAlgorithm.getAssetNetwork());
			
			// treat assumptions as findings and propagate them
			for (String name : nodeNameToStateMap.keySet()) {
				// Findings must be in the cloned network instead of original network
				ProbabilisticNode node = (ProbabilisticNode) ((ProbabilisticNetwork)clonedAlgorithm.getNetwork()).getNode(name);
				// set evidence in the probabilistic network
				Integer stateIndex = nodeNameToStateMap.get(name);
				if (stateIndex < 0) {
					// set finding as negative (i.e. finding setting a state to 0%)
					node.addFinding(Math.abs(stateIndex+1), true);
				} else {
					node.addFinding(stateIndex);
				}
			}
			// propagate finding (no need to lock, because it is a cloned net)
			clonedAlgorithm.propagate();
			
			// from here, use the clone as if it is the original
			origAlgorithm = clonedAlgorithm;
		}
		
		float ret = Float.NaN;
		synchronized (origAlgorithm.getNetwork()) {
			synchronized (origAlgorithm.getAssetNetwork()) {
				// both prob net and asset net must be locked
				List<ExpectedAssetCellMultiplicationListener> backup = null;
				if (assetCellListener != null) {
					backup = origAlgorithm.getExpectedAssetCellListeners();
					// assetCellListener will be executed on each multiplication between asset table and prob table
					origAlgorithm.setExpectedAssetCellListeners(assetCellListener);
				}
				ret = (float) origAlgorithm.calculateExpectedAssets();
				if (assetCellListener != null) {
					// backup must be restored in this critical section.
					origAlgorithm.setExpectedAssetCellListeners(backup);
				}
			}
		}
		return ret;
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
//			if (assumedStates == null) {
//				throw new IllegalArgumentException("assumedStates is not expected to be null when assumptionIds is not null.");
//			} else if (assumedStates.size() != assumptionIds.size()) {
//				throw new IllegalArgumentException("Size of assumedStates is expected to be " + assumptionIds.size() + ", but was " + assumedStates.size());
//			}
			// assumptionIds must not contain null value
			if (assumptionIds.contains(null)) {
				throw new InexistingQuestionException("Null assumption found.", null);
			}
			// assumedStates must not contain null value
//			if (assumedStates.contains(null)) {
//				throw new IllegalArgumentException("Assumption with state == null found.");
//			}
		}
		
		// check if the assumptions are semantically valid
		if (assumptionIds != null && !assumptionIds.isEmpty() && this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
			if (isToThrowExceptionOnInvalidAssumptions()) {
				throw new InvalidAssumptionException(assumptionIds + " are invalid assumptions for question " + questionId);
				// Note: cannot check assumptions of questions which were not added to the shared Bayes net yet
			}
			// convert the set of assumptions to a valid set
			List<Long> oldAssumptionIds = assumptionIds;
			// note: getMaximumValidAssumptionsSublists will always return at least 1 element
			assumptionIds = this.getMaximumValidAssumptionsSublists(questionId, assumptionIds, 1).get(0);
			// change assumedStates accordingly to new assumptionIds
			assumedStates = this.convertAssumedStates(assumptionIds, oldAssumptionIds, assumedStates);
		}
		
		// this algorithm will be alive only during the context of this method. It will contain clones of the bayesian network and asset net
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			
			// extract original algorithm, just in order to clone it
			AssetAwareInferenceAlgorithm originalAlgorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
			if (originalAlgorithm == null) {
				throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			
			// clone the algorithm. It shall also clone the bayesian network and asset network
			if (isToDoFullPreview()) {
				algorithm = (AssetAwareInferenceAlgorithm) originalAlgorithm.clone();
			} else {
				// we can use the original one if we are not going to do a full preview
				// (we don't need to clone networks if we are not going to preview literally everything)
				algorithm = originalAlgorithm;
			}
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Could not clone networks of user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		// do trade on specified algorithm (which only contains link to copies of BN and asset net)
		// 1st boolean == true := allow negative delta, since this is a preview. 
		// 2nd boolean == false := do not overwrite assumptionIds and assumedStates
		List<Float> oldValues = this.executeTrade(questionId, newValues, assumptionIds, assumedStates, true, algorithm, false, !isToDoFullPreview());	
		
		if (isToDoFullPreview()) {
			// TODO optimize (executeTrade and getAssetsIfStates have redundant portion of code)
			// return the asset position
			return this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false); // false := return assets instead of q-values
		}
		
		// just return estimated values
		// obtain q-values
//		List<Float> qValues = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, true);
		List<Float> assets = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false);
//		if (oldValues != null && (oldValues.size() == newValues.size()) && (qValues.size() == newValues.size())) {
//			// they are all related by indexes
//			List<Float> ret = new ArrayList<Float>(newValues.size());
//			for (int i = 0; i < newValues.size(); i++) {
//				ret.add(this.getScoreFromQValues((newValues.get(i)/oldValues.get(i)*qValues.get(i))));
//			}
//			return ret;
//		} else {
//			Debug.println(getClass(), "new = " + newValues + ", old = " + oldValues + ", q-values = " + qValues);
//		}
		if (oldValues != null && (oldValues.size() == newValues.size()) && (assets.size() == newValues.size())) {
			// they are all related by indexes
			List<Float> ret = new ArrayList<Float>(newValues.size());
			for (int i = 0; i < newValues.size(); i++) {
				ret.add(assets.get(i) + this.getScoreFromQValues((newValues.get(i)/oldValues.get(i))));
			}
			return ret;
		} else {
			Debug.println(getClass(), "new = " + newValues + ", old = " + oldValues + ", assets = " + assets);
		}
		
		
		return null;
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
	 * The content of this list may be changed after execution of this method. After execution of this method,
	 * this list will contain only valid assumptions, if {@link #isToThrowExceptionOnInvalidAssumptions()} == false and
	 * isToUpdateAssumptionIds == true.
	 * @param assumedStates : this list specifies a filter for states of nodes in assumptionIDs.
	 * If it does not have the same size of assumptionIDs,�@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * The content of this list may be changed after execution of this method. After execution of this method,
	 * this list will contain a sublist of the original assumedStates, with the indexes 
	 * synchronized with the indexes of assumptionIDs, if {@link #isToThrowExceptionOnInvalidAssumptions()} == false and
	 * isToUpdateAssumptionIds == true.
	 * @param isToAllowNegative : if true, negative delta (values smaller than 1 in the asset q table) is allowed.
	 * @param algorithm : algorithm to be used in order to update probability and delta. 
	 * {@link AssetAwareInferenceAlgorithm#getNetwork()} will be used to access the Bayes net.
	 * {@link AssetAwareInferenceAlgorithm#getAssetNetwork()} will be used to access the asset net.
	 * @param isToUpdateAssumptionIds : if true, assumptionIds and assumedStates will be both input and output arguments.
	 * If false, they will be only input arguments. 
	 * @see #addTrade(long, Date, String, long, long, List, List, List, boolean)
	 * @see #previewTrade(long, long, List, List, List)
	 * @return the probabilities before trade. This list will have the same structure of newValues, but its content will be filled
	 * with the probabilities before applying the trade.
	 */
	protected List<Float> executeTrade(long questionId,List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates, 
			boolean isToAllowNegative, AssetAwareInferenceAlgorithm algorithm, boolean isToUpdateAssumptionIds, boolean isPreview) {
		// basic assertions
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm was not specified.");
		}
		
		// obtain the network. Note: if you want the original to be untouched, you must provide the argument algorithm linked to a copied BN instead of the original
		ProbabilisticNetwork net = algorithm.getRelatedProbabilisticNetwork();
		if (net == null || !net.equals(getProbabilisticNetwork())) {
			throw new RuntimeException("Could not obtain bayesian network for user " + algorithm.getAssetNetwork() + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		
		// check existence of child
		TreeVariable child = (TreeVariable) net.getNode(Long.toString(questionId));
		if (child == null) {
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
		}
		
		if (child.hasEvidence()) {
			throw new IllegalArgumentException("Question " + child + " is already resolved and cannot be changed.");
		}
		for (int i = 0; i < child.getStatesSize(); i++) {
			if (child.getMarginalAt(i) == 0.0f || child.getMarginalAt(i) == 1.0f) {
				throw new IllegalArgumentException("State " + i + " of question " + child + " has probability " + child.getMarginalAt(i) + " and cannot be changed.");
			}
		}
		
		
		// this var will store the correct size of cpd. If negative, owner of the cpd was not found.
		int expectedSizeOfCPD = child.getStatesSize();
		
		// convert the set of assumptions and states to valid set
		List<Long> oldAssumptionIds = null;	// this will store a copy of the original assumptions list
		if (assumptionIds != null) {
			oldAssumptionIds = new ArrayList<Long>(assumptionIds);
			if (isToUpdateAssumptionIds) {
				// CAUTION: this is modifying an input argument (this is the desired behavior)
				assumptionIds.clear();
			}
			List<Long> validAssumptions = this.getMaximumValidAssumptionsSublists(questionId, oldAssumptionIds, 1).get(0);
			if (validAssumptions == null) {
				assumptionIds = null;
			} else {
				if (isToUpdateAssumptionIds) {
					assumptionIds.addAll(validAssumptions); 
				}
			}
		} else {
			assumptionIds = new ArrayList<Long>();
		}
		// change assumedStates accordingly to new assumptionIds
		if (assumedStates != null) {
			List<Integer> oldAssumedStates = new ArrayList<Integer>(assumedStates);
			if (isToUpdateAssumptionIds) {
				// CAUTION: this is modifying an input argument (this is the desired behavior)
				assumedStates.clear();
			}
			List<Integer> validStates = this.convertAssumedStates(assumptionIds, oldAssumptionIds, oldAssumedStates); 
			if (validStates == null) {
				assumedStates = null;
			} else {
				if (isToUpdateAssumptionIds) {
					assumedStates.addAll(validStates);
				}
			}
		} else {
			assumedStates = new ArrayList<Integer>();
		}
		
		
		// extract assumptions
		List<INode> assumptionNodes = new ArrayList<INode>();
		for (Long assumptiveQuestionId : assumptionIds) {
			TreeVariable parent = (TreeVariable) net.getNode(Long.toString(assumptiveQuestionId));
			if (parent == null) {
				throw new InexistingQuestionException("Question " + assumptiveQuestionId + " not found.", assumptiveQuestionId);
			} 
			if (parent.hasEvidence()) {
				throw new IllegalArgumentException("Question " + parent + " is already resolved and cannot be changed.");
			}
			for (int i = 0; i < parent.getStatesSize(); i++) {
				if (parent.getMarginalAt(i) == 0.0f || parent.getMarginalAt(i) == 1.0f) {
					throw new IllegalArgumentException("State " + i + " of question " + parent + " has probability " + parent.getMarginalAt(i) + " and cannot be changed.");
				}
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
			if (probability < (0-getProbabilityErrorMargin()) || probability > (1+getProbabilityErrorMargin())) {
				throw new IllegalArgumentException("Invalid probability declaration found: " + probability);
			}
			sum += probability;
			counter++;
			if (counter >= child.getStatesSize()) {
				// check if sum of conditional probability given current state of parents is 1
				if (!(((1 - getProbabilityErrorMargin()) < sum) && (sum < (1 + getProbabilityErrorMargin())))) {
					if (!(((1 - getProbabilityErrorMargin()*2) < sum) && (sum < (1 + getProbabilityErrorMargin()*2)))) {
						throw new IllegalArgumentException("Inconsistent prior probability: " + sum);
					}
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
		
		// the list to be returned (the old values)
		List<Float> oldValues = new ArrayList<Float>(newValues.size());	// force oldValues to have the same size of newValues
		
		// change content of potential according to newValues and assumedStates
		if (assumedStates == null || assumedStates.isEmpty()) {
			for (int i = 0; i < newValues.size(); i++) {
				oldValues.add(potential.getValue(i));
				potential.setValue(i, newValues.get(i));
			}
		} else {
			// instantiate multi-dimensional coordinate to be used in order to change values in potential table
			int[] multidimensionalCoord = potential.getMultidimensionalCoord(0);
			// move multidimensionalCoord to the point related to assumedStates
			for (int i = 1; i < multidimensionalCoord.length; i++) { // index 0 is for the main node (which is not specified in assumedStates), so it is not used.
				multidimensionalCoord[i] = assumedStates.get(i-1);
			}
			// modify content of potential table according to newValues 
			for (int i = 0; i < newValues.size(); i++) {	// at this point, newValues.size() == child.statesSize()
				multidimensionalCoord[0] = i; // only iterate over states of the main node (i.e. index 0 of multidimensionalCoord)
				oldValues.add(potential.getValue(multidimensionalCoord));
				potential.setValue(multidimensionalCoord, newValues.get(i));
			}
		}
		
		if (isPreview) {
			// don't actually propagate if we only need to preview
			return oldValues;
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
					algorithm.setToAllowZeroAssets(isToAllowNegative);
					
					// Note: memento costs more than algorithm.revertLastProbabilityUpdate(), but it can revert 2 or more updates.
					// in our case, we'll the min propagation, and the asset/prob update
					IAssetNetAlgorithmMemento memento = null;
					if (!isToAllowNegative) {
						memento = algorithm.getMemento();
					}
					
					algorithm.propagate();
//					algorithm.setToAllowQValuesSmallerThan1(backup);
					
					// check that minimum is below 0
					if (!isToAllowNegative) {
						// calculate minimum by running min propagation and then calculating global assets posterior to min propagation
						algorithm.runMinPropagation(null);
						// Note: if we are using q-values, 1 means 0 (because it's not log scale).
						if (algorithm.calculateExplanation(null) <= (this.isToUseQValues()?1:0)) {
							try {
								algorithm.setMemento(memento);
							} catch (NoSuchFieldException e) {
								throw new RuntimeException("Could not revert last trade after a negative asset was detected.",e);
							}
							throw new ZeroAssetsException("Cash <= 0");
						}
						// if successful, only revert last min propagation
						algorithm.undoMinPropagation();
					}
				}
			}
		}
		
		return oldValues;
	}

	/**
	 * Obtains a list of sublists of assumptionIds which makes the assumptions to become valid.
	 * Only sublists with maximum size are returned.
	 * <br/><br/>
	 * E.g.
	 * <br/><br/>
	 * Suppose questionId == 0, and assumptionIds := [1,2,3]. <br/>
	 * Also, suppose that assumptions [1,2], [1,3], [1], [2], [3], [] are all
	 * valid assumptions of questionId == 0. <br/>
	 * Then, this method shall return [[1,2], [1,3]], because these are the
	 * combinations of valid assumptions with maximum size == 2.
	 * @see #getPossibleQuestionAssumptions(long, List)
	 * @param questionId : Only valid assumptions of this question will be considered.
	 * @param assumptionIds : the returned lists will be combinations of these questions.
	 * @param maxCount : maximum quantity of combinations to be returned.
	 * E.g. if maxCount == 3, then only 3 lists will be returned by this method.
	 * If maxCount <= 0, then an empty list will be returned.
	 * @return list of lists representing all combinations of valid assumptions with maximum size.
	 * Naturally, all lists in this list will have the same size.
	 */
	public List<List<Long>> getMaximumValidAssumptionsSublists(long questionId, List<Long> assumptionIds, int maxCount) {
		// variable to return
		List<List<Long>> ret = new ArrayList<List<Long>>();
		
		// check conditions in which we do not need to calculate
		if (maxCount <= 0) {
			// it is requesting for 0 sublists
			return ret;
		}
		if (assumptionIds == null || assumptionIds.isEmpty()) {
			// "empty" is the only sublist of "empty".
			ret.add(new ArrayList<Long>());	
			return ret;
		}
		
		// do normal calculation
		if (!this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
			// if all questions in assumptionIds are valid, this is already the maximum sublist.
			ret.add(assumptionIds);
		} else {
			
			int maxSize = Integer.MIN_VALUE; // stores the current maximum quantity of questions in the valid combination of assumptions
			int currentMaxCount = maxCount;	 // counter representing maxCount, but it is reset every time the maxSize is updated
			
			// determine the combination of valid assumptions with smaller size, by removing 1 question from assumptionIds and calling recursively
			for (Long assumptionToRemove : assumptionIds) {
				
				// create a copy of assumptionIds, but removing the assumptionToRemove
				List<Long> assumptionsWithout1Question = new ArrayList<Long>(assumptionIds);
				assumptionsWithout1Question.remove(assumptionToRemove);
				
				// recursively calculate the combinations of assumptions without assumptionToRemove. 
				List<List<Long>> recursiveRet = null;
				
				if (maxSize == assumptionIds.size()-1) {
					// maxSize is already at theoretical max, so we only need currentMaxCount more sublists
					recursiveRet = this.getMaximumValidAssumptionsSublists(questionId, assumptionsWithout1Question, currentMaxCount);
				} else {
					// do not use currentMaxCount, because we don't know if maxSize is really the global maximum
					recursiveRet = this.getMaximumValidAssumptionsSublists(questionId, assumptionsWithout1Question, maxCount);
				}
				
				// check if recursiveRet has max size
				if (!recursiveRet.isEmpty() && !recursiveRet.get(0).isEmpty() && recursiveRet.get(0).size() >= maxSize) {
					if (recursiveRet.get(0).size() > maxSize) {
						// if its strictly higher, then currentMaxCount and maxSize must track the new maximum (we must forget the "older" maximums)
						maxSize = recursiveRet.get(0).size();	// all elements have supposedly the same size, so use get(0) to extract new maximum
						currentMaxCount = maxCount;		// reset the counter
						ret.clear();	// "forget" the older maximums
					}
					// add content of recursiveRet into ret, but limited to currentMaxCount
					for (int i = 0; (i < recursiveRet.size() && currentMaxCount > 0); i++, currentMaxCount--) {
						ret.add(recursiveRet.get(i));
					}
				}
			}
		}
		
		if (ret.isEmpty()) {
			// if nothing was found, add the empty list, which is always a valid assumption.
			ret.add(new ArrayList<Long>());
		}
		
		return ret;
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
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#previewBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	public List<Float> previewBalancingTrade(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		if (assumptionIds != null && assumedStates != null && assumptionIds.size() != assumedStates.size()) {
			throw new IllegalArgumentException("This method does not allow assumptionIds and assumedStates with different sizes.");
		}
		
		if (assumptionIds != null && !assumptionIds.isEmpty() && this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
			if (this.isToThrowExceptionOnInvalidAssumptions()) {
				new InvalidAssumptionException(assumptionIds + " are invalid assumptions for question " + questionId);
			}
			// convert the set of assumptions to a valid set
			List<Long> oldAssumptionIds = assumptionIds;
			// note: getMaximumValidAssumptionsSublists will always return at least 1 element
			assumptionIds = this.getMaximumValidAssumptionsSublists(questionId, assumptionIds, 1).get(0);
			// change assumedStates accordingly to new assumptionIds
			assumedStates = this.convertAssumedStates(assumptionIds, oldAssumptionIds, assumedStates);

		}
		
		// extract user's asset network from user ID
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
		}
		if (algorithm == null) {
			throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		// list to be used to store list of probabilities temporary
		List<Float> probList = this.getProbList(questionId, assumptionIds, assumedStates);
		if (probList == null) {
			throw new RuntimeException("Could not obtain probability of question " + questionId + ", with assumptions = " + assumptionIds + ", states = " + assumedStates);
		}
		if (probList.contains(0f)) {
			throw new IllegalArgumentException("Attempted to balance question " + questionId + " given assumptions " + assumptionIds
					+ ". At least one of the questions was already resolved.");
		}
		
		// obtain q1, q1, ... , qn (the asset's q values)
		List qValues = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, true);	// true := return q-values instead of asset
		
		// list to be used to store probabilities and products of q temporary
		List<Double> products = new ArrayList<Double>(probList.size());
		for (Float prob : probList) {
			products.add((double)prob);
		}
		probList.clear();	// no need to use probList anymore
		
		// basic assertion
		if (qValues.size() != products.size()) {
			throw new RuntimeException("List of probabilities and list of delta have different sizes (" 
					+ products.size() + ", and " + qValues.size() + " respectively). You may be using incompatible version of Markov Engine or UnBBayes.");
		}
		
		// this is a denominator (bottom side of a division) common to all solutions P1,P2,...,PN
		double commonDenominator = 0;	
		
		// Calculate q1*q2*...*qi-1*qi+1*...*qN*pi and store it in the list "probabilities". This is also a factor in commonDenominator.
		for (int i = 0; i < products.size(); i++) {
			
			// product := pi*(q1*q2*...*qi-1*qi+1*...*qN)
			double product = products.get(i);	
			for (int indexOfQ = 0; indexOfQ < qValues.size(); indexOfQ++) {
				if (indexOfQ != i) {
					Object qValue = qValues.get(indexOfQ);
					if (qValue instanceof Float) {
						product *= (Float)qValue;
					} else if (qValue instanceof Double) {
						product *= (Double)qValue;
					} else {
						throw new RuntimeException(qValue + " in q-values [" + qValues + "] cannot be used to calculate balancing trade.");
					}
				}
			}
			
			products.set(i, product); // TODO solve double-float underflow/overflow problem
			
			// commonDenominator := ( (q2*q3*q4*...*qN * p1) + (q1*q3*q4*...*qN * p2) + ... + (q1*q2*...*qi-1*qi+1*...*qN * pi) + ... + (q1*q2*...*qN-1 * pN) )
			commonDenominator += product;
		}
		
		if (Double.isInfinite(commonDenominator)) {
			throw new ZeroAssetsException("Overflow detected when calculating the balancing trade of user " + userId + " on question " + questionId);
		}
		
		if (commonDenominator == 0.0d) {
			// there is no solution
			return null;
		}
		
		// Calculate P1,P2,...,PN and store it in probList. 
//		for (int i = 0; i < products.size(); i++) {
//			// Pi = ((q1*q2*...*qi-1*qi+1*...*qN)*pi) / commonDenominator
//			products.set(i, (products.get(i)/commonDenominator)); // TODO solve double-float precision problem
//		}
		// Note at this point, probList is empty, because probList.clear() was called
		for (Double product : products) {
			probList.add((float) (product/commonDenominator));
		}
		
//		return products;
		return probList;
	}
	
	/**
	 * Network action representing {@link MarkovEngineImpl#doBalanceTrade(long, Date, String, long, long, List, List)}
	 * This is actually an {@link AddTradeNetworkAction}, but with values of {@link AddTradeNetworkAction#getNewValues()}
	 * set to a probability distribution which will balance the assets of a question given assumptions.
	 * @author Shou Matsumoto
	 */
	public class BalanceTradeNetworkAction extends AddTradeNetworkAction {
		/** Default constructor initializing fields\ */
		public BalanceTradeNetworkAction(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) {
			super(transactionKey, occurredWhen, tradeKey, userId, questionId, null, assumptionIds, assumedStates, false);
		}
		/** Virtually does {@link MarkovEngineImpl#previewBalancingTrade(long, long, List, List)} and then {@link AddTradeNetworkAction#execute()} */
		public void execute() {
			synchronized (getProbabilisticNetwork()) {
				// obtain trade values for exiting the user from a question given assumptions
				this.setNewValues(previewBalancingTrade(getUserId(), getQuestionId(), getAssumptionIds(), getAssumedStates()));
				// execute the trade without releasing lock
				super.execute();
			}
		}
		public void revert() throws UnsupportedOperationException {
			super.revert();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#doBalanceTrade(java.lang.Long, java.util.Date, java.lang.String, long, long, java.util.List, java.util.List)
	 */
	public boolean doBalanceTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException, InvalidAssumptionException, InexistingQuestionException {
		
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (transactionKey != null && !this.getNetworkActionsMap().containsKey(transactionKey)) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		try {
			if (this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
				if (isToThrowExceptionOnInvalidAssumptions()) {
					throw new InvalidAssumptionException(assumptionIds + " are invalid assumptions for question " + questionId);
				}
				// only use valid assumptions
				// note: getMaximumValidAssumptionsSublists will always return at least 1 element
				List<Long> oldAssumptionIds = assumptionIds;
				assumptionIds = this.getMaximumValidAssumptionsSublists(questionId, assumptionIds, 1).get(0);
				// change assumedStates accordingly to new assumptionIds
				assumedStates = this.convertAssumedStates(assumptionIds, oldAssumptionIds, assumedStates);
				
			}
		} catch (InexistingQuestionException e) {
			// Perhaps the nodes are still going to be added within the context of this transaction.
			boolean isNodeToBeCreatedWithinTransaction = false;
			List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey); // getNetworkActionsMap() is supposedly a concurrent map
			synchronized (actions) {	// actions is not a concurrent list, so must lock it
				for (NetworkAction action : actions) {
					if ((action instanceof AddQuestionNetworkAction) && (e.getQuestionId() == action.getQuestionId().longValue())) {
						// this action will create the question which was not found.
						isNodeToBeCreatedWithinTransaction = true;
						break;
					}
				}
			}
			if (!isNodeToBeCreatedWithinTransaction) {
				throw e;
			}
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
		TreeVariable mainNode  = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(questionId));
			if (mainNode != null) {
				if (mainNode.hasEvidence()) {
					throw new IllegalArgumentException("Question " + mainNode + " is already resolved and cannot be changed.");
				}
				for (int i = 0; i < mainNode.getStatesSize(); i++) {
					if (mainNode.getMarginalAt(i) == 0.0f || mainNode.getMarginalAt(i) == 1.0f) {
						throw new IllegalArgumentException("State " + i + " of question " + mainNode + " has probability " + mainNode.getMarginalAt(i) + " and cannot be changed.");
					}
				}
			}
		}
		
		if (mainNode == null) {
			// node does not exist. Check if there was some previous actions (in same transaction) adding such node
			if (transactionKey == null) {
				// this transaction has only this action (so, there is no previous actions adding such node)
				throw new InexistingQuestionException("Question ID " + questionId + " does not exist.", questionId);
			}
			boolean willCreateNodeOnCommit = false;	// if true, the node will be created in this transaction
			
			// iterate on the actions in the same transaction
			List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
			if (actions == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				// actions cannot be null, because we checked this condition before
				throw new IllegalStateException("Desync detected in transaction " + transactionKey + ": the transaction was deleted while doBalanceTrade was in execution.");
			}
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
				throw new InexistingQuestionException("Question ID " + questionId + " does not exist.", questionId);
			}
		}
		
		// check existence of assumptions
		if (assumptionIds != null) {
			for (Long assumptiveQuestionId : assumptionIds) {
				TreeVariable parent =null;
				synchronized (getProbabilisticNetwork()) {
					parent = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(assumptiveQuestionId));
					if (parent != null) {
						if (parent.hasEvidence()) {
							throw new IllegalArgumentException("Question " + parent + " is already resolved and cannot be changed.");
						}
						for (int i = 0; i < parent.getStatesSize(); i++) {
							if (parent.getMarginalAt(i) == 0.0f || parent.getMarginalAt(i) == 1.0f) {
								throw new IllegalArgumentException("State " + i + " of question " + parent + " has probability " + parent.getMarginalAt(i) + " and cannot be changed.");
							}
						}
					}
				}
				if (parent == null) {
					// parent node does not exist. Check if there was some previous transaction adding such node
					if (transactionKey == null) {
						// this transaction has only this action (so, there is no previous actions adding such node)
						throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist.", assumptiveQuestionId);
					}
					boolean hasFound = false;
					// iterate on the actions in the same transaction
					List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
					if (actions == null) {
						// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
						// actions cannot be null, because we checked this condition before
						throw new IllegalStateException("Desync detected in transaction " + transactionKey + ": the transaction was deleted while doBalanceTrade was in execution.");
					}
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
						throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist.", assumptiveQuestionId);
					}
				}
			}
		}
		
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new BalanceTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, assumptionIds, assumedStates));
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for balancing trade
			this.addNetworkAction(transactionKey, new BalanceTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, assumptionIds, assumedStates));
		}
		
		return true;
	}
	

	/**
	 * This method takes assumedStates, which is synchronized with oldAssumptionIds
	 * by index, and returns assumedStates converted to the indexing 
	 * related to assumptionIds.
	 * @param newAssumptionIds : a sublist of oldAssumptionIds
	 * @param oldAssumptionIds : list of questions
	 * @param assumedStates : states of questions in oldAssumptionIds
	 * @return  sublist of assumedStates synchronized with assumptionIds by index.
	 */
	protected List<Integer> convertAssumedStates(List<Long> newAssumptionIds,  List<Long> oldAssumptionIds, List<Integer> assumedStates) {
		if (oldAssumptionIds == null || newAssumptionIds == null) {
			return assumedStates;
		}
		if (!oldAssumptionIds.containsAll(newAssumptionIds)) {
			throw new IllegalArgumentException("Cannot translate the states " + assumedStates 
					+ " of questions " + oldAssumptionIds + " to questions " + newAssumptionIds
					+ ", because the latter is not a subset of the former.");
		}
		if (assumedStates != null) {
			List<Integer> oldAssumedStates = assumedStates;
			assumedStates = new ArrayList<Integer>();
			for (Long assumption : newAssumptionIds) {
				int indexInOldAssumption = oldAssumptionIds.indexOf(assumption);
				if (indexInOldAssumption < oldAssumedStates.size()) {
					assumedStates.add(oldAssumedStates.get(indexInOldAssumption));
				}
			}
		}
		return assumedStates;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
	 */
	public List<QuestionEvent> getQuestionHistory(Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
//		TODO obtain indirect impact
		
		List<NetworkAction> list = null;
		synchronized (this.getNetworkActionsIndexedByQuestions()) {
			list = this.getNetworkActionsIndexedByQuestions().get(questionId);
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
				if (assumptionIds == null || assumptionIds.isEmpty()) {
					// no filter. Add everything
					ret.add(action);
				} else if (action.getAssumptionIds() != null && action.getAssumptionIds().containsAll(assumptionIds)) {
					// matched assumptionIDs filter. Check assumedStates filter
					boolean hasWrongMatch = false;
					if (action.getAssumedStates() != null && !action.getAssumedStates().isEmpty()) {
						// if any states in the filter doesn't match the states in action.getAssumedStates(), do not add
						for (int i = 0; (i < assumedStates.size()) && (i < assumptionIds.size()); i++) {
							// extract index of assumption in "action".
							int indexOfQuestionInNetworkAction = action.getAssumptionIds().indexOf(assumptionIds.get(i));
							if (indexOfQuestionInNetworkAction < 0) {
								// supposedly, at this point, indexOfQuestionInNetworkAction >=0 , because action.getAssumptionIds().containsAll(assumptionIDs)
								try {
									Debug.println(getClass(), "Trade " + action.getTradeId() + " claims that it is using question " + assumptionIds.get(i) 
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
											+ ") than�@in assumedStates (" + action.getAssumedStates().size() + ").");
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
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreSummaryObject(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public ScoreSummary getScoreSummaryObject(long userId, final Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		if (isToReturnEVComponentsAsScoreSummary()) {
			return this.getScoreEVComponent(userId, questionId, assumptionIds, assumedStates);
		}
		return getScoreEVQuestionStateComponent(userId, questionId, assumptionIds, assumedStates);
	}
	
	/**
	 * This is an implementation of {@link SummaryContribution}
	 * which carries the values of {@link MarkovEngineImpl#scoreUserQuestionEvStates(long, long, List, List)}
	 * @author Shou Matsumoto
	 */
	public class ScoreEVPerQuestionSummaryContribution implements SummaryContribution {
		private List<Long> questionId;
		private List<Integer> stateIndex;
		private float scoreEvOfState;

		/** Constructor initializing fields */
		public ScoreEVPerQuestionSummaryContribution(Long questionId, int stateIndex, float scoreEvOfState) {
			super();
			this.questionId = Collections.singletonList(questionId);
			this.stateIndex = Collections.singletonList(stateIndex);
			this.scoreEvOfState = scoreEvOfState;
		}
		/** This is a {@link Collections#singletonList(Object)} (a list with only 1 element) containing only the ID of the question */
		public List<Long> getQuestions() {
			return questionId;
		}
		/** This is a {@link Collections#singletonList(Object)} (a list with only 1 element) containing only the index of the state  */
		public List<Integer> getStates() {
			return stateIndex;
		}
		
		/** 
		 * Contains an entry of {@link MarkovEngineImpl#scoreUserQuestionEvStates(long, long, List, List)}
		 * (i.e. the value of {@link MarkovEngineImpl#scoreUserQuestionEvStates(long, long, List, List)}
		 * of the state {@link #getStates()} ) 
		 */
		public float getContributionToScoreEV() {
			return scoreEvOfState;
		}
		
	}
	
	
	/**
	 * This method is used in {@link #getScoreSummaryObject(long, Long, List, List)}
	 * in order to describe the value of {@link #scoreUserEv(long, List, List)}
	 * in terms of estimated scores given states of questions 
	 * (i.e. in terms of {@link #scoreUserQuestionEvStates(long, long, List, List)}).
	 * @param userId
	 * @param questionId
	 * @param assumptionIds
	 * @param assumedStates
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected ScoreSummary getScoreEVQuestionStateComponent(long userId, final Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// obtain the conditional cash
		final float cash = this.getCash(userId, assumptionIds, assumedStates);
		
		// obtain the conditional expected score, 
		final float scoreEV = this.scoreUserEv(userId, assumptionIds, assumedStates);
		
		// this list will contain the expected score given states of the povided question
		final List<SummaryContribution> scoreEVPerStateList = new ArrayList<ScoreSummary.SummaryContribution>();
		
		// scoreUserQuestionEvStates will be called for questions in this list
		List<Long> questionsToConsider = new ArrayList<Long>();
		if (questionId != null) {
			// questionId was explicitly specified, so use only this question ID
			questionsToConsider.add(questionId);
		} else {
			// look for questions which the user has traded directly
			questionsToConsider.addAll(this.getTradedQuestions(userId));
		}
		
		// fill scoreEVPerStateList with values returned by scoreUserQuestionEvStates
		for (Long idOfQuestionToConsider : questionsToConsider) {
			if (this.hasQuestion(idOfQuestionToConsider)) {
				List<Float> expectedScorePerState = this.scoreUserQuestionEvStates(userId, idOfQuestionToConsider, assumptionIds, assumedStates);
				if (expectedScorePerState == null || expectedScorePerState.isEmpty()) {
					throw new RuntimeException("scoreUserQuestionEvStates is returning a null/empty list for user = " + userId 
							+ ", question = " + idOfQuestionToConsider
							+ ", assumptions = " + assumptionIds 
							+ ", states of the assumptions = " + assumedStates);
				}
				// ScoreEVPerQuestionSummaryContribution is the content of scoreEVPerStateList
				for (int state = 0; state < expectedScorePerState.size(); state++) {
					scoreEVPerStateList.add(new ScoreEVPerQuestionSummaryContribution(idOfQuestionToConsider, state, expectedScorePerState.get(state)));
				}
			}
		}
		
		// integrate all data into one ScoreSummary and return.
		return new ScoreSummary() {
			public float getCash() { return cash; }
			public float getScoreEV() {return scoreEV; }
			public List<SummaryContribution> getScoreComponents() { return scoreEVPerStateList; }
			public List<SummaryContribution> getIntersectionScoreComponents() { return Collections.emptyList(); }
		};
	}
	
	/**
	 * Checks whether a question exists in the system.
	 * @param questionId : id of the question to look for
	 * @return true if a question with ID == questionId is present in the system.
	 * False otherwise.
	 */
	public boolean hasQuestion(Long questionId) {
		synchronized (getProbabilisticNetwork()) {
			return getProbabilisticNetwork().getNodeIndex(""+questionId) >= 0;
		}
	}

	/**
	 * This method implements the old specification of {@link #getScoreSummaryObject(long, Long, List, List), which is to
	 * return how much each cell in clique table contributes to the overall expected score (i.e. how #scoreUserEv(long, List, List)
	 * is calculated from the clique potentials and asset tables).
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreSummaryObject(long, java.lang.Long, java.util.List, java.util.List)
	 */
	protected ScoreSummary getScoreEVComponent(long userId, final Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		
		// obtain the conditional cash
		final float cash = this.getCash(userId, assumptionIds, assumedStates);
		
		// this list will contain info related to expected score components (prob cell * asset cell) of cliques
		final List<SummaryContribution> cliqueComponents = new ArrayList<ScoreSummary.SummaryContribution>();
		
		// this list will contain info related to expected score components (prob cell * asset cell) of separators
		final List<SummaryContribution> sepComponents = new ArrayList<ScoreSummary.SummaryContribution>();
		
		// this is a dummy node used just for searching nodes in a list, by using Object#equals()
		final Node dummyNode = new Node() {
			public int getType() { return 0; }
			/** Object#equals() will call this method */
			public String getName() {return ""+questionId; }
		};
		
		// prepare listener to be notified when cells of asset tables and prob tables are multiplied
		ExpectedAssetCellMultiplicationListener listener = new ExpectedAssetCellMultiplicationListener() {
			
			private Map<IRandomVariable, List<Long>> questionsCache = new HashMap<IRandomVariable, List<Long>>();
			
			/** Fill content of cliqueComponents or sepComponents */
			public void onModification(IRandomVariable probCliqueOrSep, IRandomVariable assetCliqueOrSep, int indexInProbTable, int indexInAssetTable, final double value) {
				// Note: we assume indexInAssetTable == indexInProbTable and probCliqueOrSep matches assetCliqueOrSep, so we'll only look at probCliqueOrSep
				
				// This variable will point to either cliqueComponents or sepComponents, depending of the type of probCliqueOrSep
				List<SummaryContribution> whereToAdd = null;
				if (probCliqueOrSep instanceof Clique) {
					whereToAdd = cliqueComponents;	// if clique, new SummaryContribution shall be added to cliqueComponents
				} else if (probCliqueOrSep instanceof Separator) {
					whereToAdd = sepComponents;	// if separator, new SummaryContribution shall be added to sepComponents
				} else {
					// ignore other cases
					return;	
				};
				
				// Prepare data to be used to fill new SummaryContribution
				// Note: SummaryContribution#getContributionToScoreEV() is the argument "value", so we do not need to create new variable for it
				
				
				// indexInProbTable is pointing to some cell in this table
				PotentialTable table = (PotentialTable) probCliqueOrSep.getProbabilityFunction();	// only nodes related to this table is considered
				
				
				// check cache first (we want to reuse objects for the questions, because they will repeat a lot)
				List<Long> cache = questionsCache.get(probCliqueOrSep);
				
				// this will be the SummaryContribution#getQuestions()
				final List<Long> questions = (cache != null)?cache:new ArrayList<Long>(table.getVariablesSize());
				
				// this boolean var will remain false if node whose Node#getName() == questionId is not in table
				boolean matchesFilter = false;	
				
				if (cache == null) {
					// update cache if cache was not present
					questionsCache.put(probCliqueOrSep, questions);
					// Fill the list "questions" regarding the filter (i.e. "questionId")
					for (int i = 0; i < table.variableCount(); i++) {
						try {
							Long idOfCurrentNode = Long.parseLong(table.getVariableAt(i).getName()); // the name is supposedly the ID
							if (idOfCurrentNode == questionId) {
								matchesFilter = true;
							}
							questions.add(idOfCurrentNode);	
						} catch (NumberFormatException e) {
							Debug.println(getClass(), table.getVariableAt(i) + " is not a question ID :" + e.getMessage(), e);
							// continue;
						}
					}
				} else {
					matchesFilter = questionId == null || cache.contains(questionId);
				}
				
				if (questionId != null && !matchesFilter) {
					return;	// if it does not match filter, we do not need to update the list of SummaryContribution
				}
				
				// this will be the SummaryContribution#getStates()
				final List<Integer> states = new ArrayList<Integer>();	
				// only fill states if this is not an empty clique/separator (empty separator can happen if it is a virtual separator)
				if (indexInProbTable >= 0 && table.tableSize() > 0) {
					// multidimensionalCoord indicates what states are related to the index "indexInProbTable" in the table "table"
					int[] multidimensionalCoord = table.getMultidimensionalCoord(indexInProbTable);	 // array with states of each node in table
					for (int i = 0; i < multidimensionalCoord.length; i++) {
						states.add(multidimensionalCoord[i]);
					}
				}
				
				// update the list
				whereToAdd.add(new SummaryContribution() {
					public List<Long> getQuestions() { return questions; }
					public List<Integer> getStates() { return states; }
					public float getContributionToScoreEV() { return (float) value; }
				});
				
			}	// end of inner method
		};	// end of anonymous inner class
		
		// obtain the conditional expected score, passing the listener as argument, so that cliqueComponents and sepComponents are filled properly
		final float scoreEV = this.scoreUserEv(userId, assumptionIds, assumedStates, Collections.singletonList(listener));
		
		// integrate all data into one ScoreSummary and return.
		return new ScoreSummary() {
			public float getCash() { return cash; }
			public float getScoreEV() {return scoreEV; }
			public List<SummaryContribution> getScoreComponents() { return cliqueComponents; }
			public List<SummaryContribution> getIntersectionScoreComponents() { return sepComponents; }
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreSummary(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public List<Properties> getScoreSummary(long userId, Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		ScoreSummary summary = this.getScoreSummaryObject(userId, questionId, assumptionIds, assumedStates);
		List<Properties> ret = new ArrayList<Properties>();
		Properties rootProperty = new Properties();
		rootProperty.put(SCOREEV_PROPERTY, Float.toString(summary.getScoreEV()));
		rootProperty.put(CASH_PROPERTY, Float.toString(summary.getCash()));
		rootProperty.put(SCORE_COMPONENT_SIZE_PROPERTY, Integer.toString(summary.getScoreComponents().size()));
		ret.add(rootProperty);
		for (SummaryContribution contribution : summary.getScoreComponents()) {
			Properties prop = new Properties();
			if (contribution.getQuestions() != null && !contribution.getQuestions().isEmpty()) {
				String commaSeparatedQuestions = "";
				for (Long id : contribution.getQuestions()) {
					commaSeparatedQuestions += "," + id;
				}
				// remove first comma and add to property
				prop.put(QUESTIONS_PROPERTY, commaSeparatedQuestions.substring(commaSeparatedQuestions.indexOf(',')+1));
			}
			if (contribution.getStates() != null && !contribution.getStates().isEmpty()) {
				String commaSeparatedStates = "";
				for (Integer state : contribution.getStates()) {
					commaSeparatedStates += "," + state;
				}
				// remove first comma and add to property
				prop.put(STATES_PROPERTY, commaSeparatedStates.substring(commaSeparatedStates.indexOf(',')+1));
			}
			prop.put(SCOREEV_PROPERTY, Float.toString(contribution.getContributionToScoreEV()));
			ret.add(prop);
		}
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreDetails(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public List<Properties> getScoreDetails(long userId, Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		// not required in the 1st release
		return Collections.singletonList(new Properties());
//		throw new UnsupportedOperationException("Operation not supported by this version.");
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
	 * (algorithm for managing bayesian network and delta), 
	 * which is related to {@link AssetNetwork} 
	 * (class representing user and asset q tables)
	 * @return the userToAssetAwareAlgorithmMap
	 */
	protected Map<Long, AssetAwareInferenceAlgorithm> getUserToAssetAwareAlgorithmMap() {
		return userToAssetAwareAlgorithmMap;
	}

	/**
	 * Mapping from user ID to {@link AssetAwareInferenceAlgorithm}
	 * (algorithm for managing bayesian network and delta), 
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
		synchronized (getProbabilisticNetwork()) {
			if (getProbabilisticNetwork() == null || getProbabilisticNetwork().getJunctionTree() == null) {
				throw new IllegalStateException("Failed to initialize user " + userID + ", because the shared Bayesian Network was not properly initialized. Please, initialize network and commit transaction.");
			}
		}
		
		// value to be returned
		AssetAwareInferenceAlgorithm algorithm = null;
		
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			// only create new instance if RebuildNetworkAction, ResolveQuestionNetworkAction, etc. are not blocking.
			algorithm = getUserToAssetAwareAlgorithmMap().get(userID);
			if (algorithm == null) {
				// first time user is referenced. Prepare inference algorithm for the user
				JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
				// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
				junctionTreeAlgorithm.setLikelihoodExtractor(AssetAwareInferenceAlgorithm.DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR);
				// prepare default inference algorithm for asset network
				algorithm = getAssetAwareInferenceAlgorithmBuilder().build(junctionTreeAlgorithm, getDefaultInitialAssetTableValue());
				algorithm.setToCalculateLPE(false);	// we are only interested on min-values, never min-states
				// set markov engine as the converter between q-values and assets
				algorithm.setqToAssetConverter(this);
				// force algorithm to call min-propagation only when prompted (i.e. only when runMinPropagation() is called)
				algorithm.setToPropagateForGlobalConsistency(false);
				// OPTIMIZATION : do not calculate marginals of asset nodes without explicit call
				algorithm.setToCalculateMarginalsOfAssetNodes(false);
				junctionTreeAlgorithm.setVerifyConsistencyCommandList(null);	// users don't need to verify consistency of shared bayes net
				
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
			
			// insert new action into the map to be used for searching actions by question id
			this.addNetworkActionIntoQuestionMap(newAction);
		}
		
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
		
		synchronized (actions) {
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
	}

//	/**
//	 * @param isToAddCashProportionally the isToAddCashProportionally to set.
//	 * If true, {@link #addCash(long, Date, long, float, String)} will 
//	 * increase cash (min-q) proportionally to the current value of min-q.
//	 * <br/>
//	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
//	 * is set to add 10, then the final min-q is 15 (triple of the original min-q), hence
//	 * other q-values will be also multiplied by 3.
//	 * <br/>
//	 * If false, the values added by {@link #addCash(long, Date, long, float, String)}
//	 * will only increase q-values absolutely.
//	 * <br/>
//	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
//	 * is set to add 10, then 10 will also be added to the other q-values.
//	 */
//	public void setToAddCashProportionally(boolean isToAddCashProportionally) {
//		this.isToAddCashProportionally = isToAddCashProportionally;
//	}
//
//	/**
//	 * @return the isToAddCashProportionally.
//	 * If true, {@link #addCash(long, Date, long, float, String)} will 
//	 * increase cash (min-q) proportionally to the current value of min-q.
//	 * <br/>
//	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
//	 * is set to add 10, then the final min-q is 15 (triple of the original min-q), hence
//	 * other q-values will be also multiplied by 3.
//	 * <br/>
//	 * If false, the values added by {@link #addCash(long, Date, long, float, String)}
//	 * will only increase q-values absolutely.
//	 * <br/>
//	 * E.g. if current min-q is 5, and {@link #addCash(long, Date, long, float, String)}
//	 * is set to add 10, then 10 will also be added to the other q-values.
//	 */
//	public boolean isToAddCashProportionally() {
//		return isToAddCashProportionally;
//	}

	/**
	 * Assets are managed by a data structure known as the asset tables
	 * (they are clique-tables containing non-normalized float values).
	 * When an asset table is instantiated (i.e. when a new user is created,
	 * and then a new asset table is created for that user), each
	 * cell of the asset table should be filled with default (uniform) values initially.
	 * <br/><br/>
	 * Note: the delta (S-values) and q-values (values actually stored in
	 * the tables) are related with a logarithm relationship
	 * S = b*log(q). So, the values in the asset tables may not actually be
	 * the values of delta directly.
	 * @return the value to be filled into each cell of the q-tables when the tables are initialized.
	 * @see #initialize()
	 */
	public float getDefaultInitialAssetTableValue() {
		return defaultInitialAssetTableValue;
	}
	

	/**
	 * Assets are managed by a data structure known as the asset's q-tables
	 * (sometimes referred as q-tables or asset tables)
	 * They are clique-tables containing non-normalized float values.
	 * When an asset table is instantiated (i.e. when a new user is created,
	 * and then a new asset table is created for that user), each
	 * cell of the asset table should be filled with default (uniform) values initially.
	 * <br/><br/>
	 * Note: the delta (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q). So, the values in the asset tables may not actually be
	 * the values of delta directly.
	 * <br/><br/>
	 * This method updates the value of {@link #getCurrencyConstantMultiplier()}
	 * @param defaultValue : the value to be filled into each cell of the q-tables when the tables are initialized.
	 * @see #initialize()
	 */
	public void setDefaultInitialAssetTableValue(float defaultValue) {
		if (isToUseQValues() && defaultValue <= 0) {
			throw new IllegalArgumentException("Cannot set default q table value to " + defaultValue);
		}
		this.defaultInitialAssetTableValue = defaultValue;
		if (Float.isInfinite(defaultInitialAssetTableValue)) {
			throw new IllegalArgumentException("Cannot set default asset table value to infinite");
		}
		if (Float.isNaN(defaultInitialAssetTableValue)) {
			throw new IllegalArgumentException("Cannot set default asset table value to NaN");
		}
	}
	
	/**
	 * Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q) with log being a logarithm function of some basis. 
	 * @return the base of the current logarithm function used
	 * for converting q-values to delta.
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
	 * for converting q-values to delta.
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
	 * is the delta).
	 * @return the current value of b, the "unit of currency""
	 * @see #getCurrentLogBase()
	 * @see #setCurrentLogBase(float)
	 * @see #setCurrentCurrencyConstant(float)
	 * @see #getQValuesFromScore(float)
	 * @see #getScoreFromQValues(float)
	 */
	public float getCurrentCurrencyConstant() {
		return currentCurrencyConstant ;
	}
	

	/**
	 *  Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with b being a constant for defining the "unit
	 * of currency" (more precisely, this constant defines how sensitive
	 * is the delta).
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
		if (conditionalProbabilityExtractor instanceof InCliqueConditionalProbabilityExtractor) {
			this.conditionalProbabilityExtractor = conditionalProbabilityExtractor;
		} else {
			throw new UnsupportedOperationException("getConditionalProbabilityExtractor() with instance other than " 
					+ InCliqueConditionalProbabilityExtractor.class.getName() + " is not supported by this version.");
		}
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
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getJointProbability(java.util.List, java.util.List)
	 */
	public float getJointProbability(List<Long> questionIds,
			List<Integer> states) throws IllegalArgumentException {
		// initial assertion
		if (questionIds == null || questionIds.isEmpty() || states == null || states.isEmpty()) {
			throw new IllegalArgumentException("This method cannot calculate joint probability without specifying the nodes and states");
		}
		
		// this is an argument to be passed to inference algorithm
		Map<ProbabilisticNode, Integer> nodesAndStatesToConsider = new HashMap<ProbabilisticNode, Integer>();
		
		// fill nodesAndStatesToConsider. Note: at this point, both questionIds and states are non-null
		for (int i = 0; i < questionIds.size(); i++) {
			Long questionId = questionIds.get(i);
			if (questionId == null) {
				continue;	// ignore
			}
			Integer state = null;
			if (i < states.size()) {
				state  = states.get(i);
			}
			if (state == null) {
				continue;	// ignore
			}
			synchronized (getProbabilisticNetwork()) {
				ProbabilisticNode node = (ProbabilisticNode) getProbabilisticNetwork().getNode(Long.toString(questionId));
				if (node == null) {
					throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
				}
				// negative states can be interpreted as "not" that state (i.e. assume 0% for that state)
				// TODO check if we'll really need this feature
				if (state < 0 && Math.abs(state + 1) >= node.getStatesSize()) {
					throw new IllegalArgumentException("State " + Math.abs(state + 1) + " is invalid for question " + questionId);
				}
				if (state >= node.getStatesSize()) {
					throw new IllegalArgumentException("State " + state + " is invalid for question " + questionId);
				}
				// add this node+state into argument
				nodesAndStatesToConsider.put(node, state);
			}
		}
		
		return getDefaultInferenceAlgorithm().getJointProbability(nodesAndStatesToConsider);
	}

//	/**
//	 * If true, {@link #getAssetsIfStates(long, long, List, List)} will return
//	 * a list in the following format (note - A(X) returns the delta of the question X, and A(X=x1|Y=y1,Z=z1) is the
//	 * delta of state x1 of question X given state y1 of question Y, and state z1 of question Z):
//	 * <br/><br/>
//	 * Suppose we are calling {@link #getAssetsIfStates(long, long, List, List)} for question X given assumptions [Y, Z]
//	 * (and also suppose that all 3 questions have 2 states). Then, the returned list is:<br/>
//	 * [A(X=x0|Y=y0,Z=z0) ; A(X=x1|Y=y1,Z=z0); A(X=x0|Y=y0,Z=z0); A(X=x1|Y=y1,Z=z0);  A(X=x0|Y=y0,Z=z1) ; A(X=x1|Y=y1,Z=z1); A(X=x0|Y=y0,Z=z1); A(X=x1|Y=y1,Z=z1)]
//	 * @param isToReturnConditionalAssets the isToReturnConditionalAssets to set
//	 */
//	public void setToReturnConditionalAssets(boolean isToReturnConditionalAssets) {
//		this.isToReturnConditionalAssets = isToReturnConditionalAssets;
//	}
//
//	/**
//	 * @return the isToReturnConditionalAssets
//	 */
//	public boolean isToReturnConditionalAssets() {
//		return isToReturnConditionalAssets;
//	}
	
	/**
	 * @param executedActions the executedActions to set
	 */
	public void setExecutedActions(List<NetworkAction> executedActions) {
		this.executedActions = executedActions;
	}

	/**
	 * @return the executedActions
	 */
	public List<NetworkAction> getExecutedActions() {
		return executedActions;
	}

	/** 
	 * Special type of {@link IllegalArgumentException} thrown when assumptions are impossible for a question. 
	 * @see MarkovEngineImpl#getPossibleQuestionAssumptions(long, List)
	 * @see MarkovEngineImpl#previewTrade(long, long, List, List, List)
	 */
	public class InvalidAssumptionException extends IllegalArgumentException {
		private static final long serialVersionUID = 4296752629503621028L;
		public InvalidAssumptionException() { super(); }
		public InvalidAssumptionException(String message, Throwable cause) { super(message, cause); }
		public InvalidAssumptionException(String s) { super(s); }
		public InvalidAssumptionException(Throwable cause) { super(cause); }
	}
	
	/** 
	 * Special type of {@link IllegalArgumentException} thrown when questions are not in {@link MarkovEngineImpl#getProbabilisticNetwork()}.
	 */
	public class InexistingQuestionException extends IllegalArgumentException {
		private static final long serialVersionUID = -2360508821973951850L;
		private final Long questionId;
		public InexistingQuestionException(Long questionId) { super(); this.questionId = questionId; }
		public InexistingQuestionException(String message, Throwable cause, Long questionId) { super(message, cause); this.questionId = questionId; }
		public InexistingQuestionException(String s, Long questionId) { super(s); this.questionId = questionId; }
		public InexistingQuestionException(Throwable cause, Long questionId) { super(cause); this.questionId = questionId; }
		public Long getQuestionId() { return questionId; }
	}

	/**
	 * If true, #get
	 * @return the isToObtainProbabilityOfResolvedQuestions
	 */
	public boolean isToObtainProbabilityOfResolvedQuestions() {
		return isToObtainProbabilityOfResolvedQuestions;// && !isToDeleteResolvedNode();
	}

	/**
	 * @param isToObtainProbabilityOfResolvedQuestions the isToObtainProbabilityOfResolvedQuestions to set
	 */
	public void setToObtainProbabilityOfResolvedQuestions(
			boolean isToObtainProbabilityOfResolvedQuestions) {
		this.isToObtainProbabilityOfResolvedQuestions = isToObtainProbabilityOfResolvedQuestions;
	}

	/**
	 * If true, {@link ResolveQuestionNetworkAction#execute()} will delete the resolved question.
	 * @return the isToDeleteResolvedNode
	 */
	public boolean isToDeleteResolvedNode() {
		return isToDeleteResolvedNode;
	}

	/**
	 * If true, {@link ResolveQuestionNetworkAction#execute()} will delete the resolved question.
	 * @param isToDeleteResolvedNode the isToDeleteResolvedNode to set
	 */
	public void setToDeleteResolvedNode(boolean isToDeleteResolvedNode) {
		this.isToDeleteResolvedNode = isToDeleteResolvedNode;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionAssumptionGroups()
	 */
	public List<List<Long>> getQuestionAssumptionGroups() {
		// value to be returned
		List<List<Long>> ret = new ArrayList<List<Long>>();
		
		// initial assertion
		if (getProbabilisticNetwork() == null) {
			return ret;
		}
		
		// extract cliques
		List<Clique> cliques = null;
		synchronized (getProbabilisticNetwork()) {
			if (getProbabilisticNetwork().getJunctionTree() == null 
					|| getProbabilisticNetwork().getJunctionTree().getCliques() == null) {
				return ret;
			}
			List<Clique> originalCliqueList = getProbabilisticNetwork().getJunctionTree().getCliques();
			if (originalCliqueList != null) {
				// we are using another instance, so that we can release the lock and let other threads change the clique structre as they wish
				cliques = new ArrayList<Clique>(originalCliqueList);
			}
		}
		// TODO check it is OK to release lock here
		if (cliques == null) {
			return ret;
		}
		
		// populate list of question ids based on nodes in cliques
		for (Clique clique : cliques) {
			if (clique.getNodes().isEmpty() || clique.getProbabilityFunction().tableSize() <= 1) {
				// this clique is empty. Do not consider this
				continue;
			}
			List<Long> questionIds = new ArrayList<Long>();
			for (Node node : clique.getNodes()) {
				try {
					questionIds.add(Long.parseLong(node.getName()));
				} catch (Exception e) {
					Debug.println(getClass(), e.getMessage(), e);
					// ignore exception
				}
			}
			ret.add(questionIds);
		}
		return ret;
	}
	
	/**
	 * This is a memento class (see Memento design pattern), which stores the important
	 * states clique and separator tables of probability  and assets in a given moment.
	 * This is useful for reverting the last trade.
	 * @author Shou Matsumoto
	 *
	 */
	public class ProbabilityAndAssetTablesMemento {
		protected Map<IRandomVariable, PotentialTable> probTableMap;
		protected Map<AssetAwareInferenceAlgorithm, Map<IRandomVariable, PotentialTable>> assetTableMap;

		/**
		 * Stores probability tables of cliques/separators of {@link MarkovEngineImpl#getProbabilisticNetwork()}
		 * and asset tables of {@link MarkovEngineImpl#getUserToAssetAwareAlgorithmMap()}.
		 */
		public ProbabilityAndAssetTablesMemento() {
			probTableMap = new HashMap<IRandomVariable, PotentialTable>();
			synchronized (getProbabilisticNetwork()) {
				for (Clique clique : getProbabilisticNetwork().getJunctionTree().getCliques()) {
					probTableMap.put(clique, (PotentialTable) clique.getProbabilityFunction().clone());
				}
				for (Separator separator : getProbabilisticNetwork().getJunctionTree().getSeparators()) {
					probTableMap.put(separator, (PotentialTable) separator.getProbabilityFunction().clone());
				}
			}
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				assetTableMap = new HashMap<AssetAwareInferenceAlgorithm, Map<IRandomVariable,PotentialTable>>();
				for (AssetAwareInferenceAlgorithm algorithm : getUserToAssetAwareAlgorithmMap().values()) {
					Map<IRandomVariable,PotentialTable> assetTables = new HashMap<IRandomVariable, PotentialTable>();
					synchronized (algorithm.getAssetNetwork()) {
						for (Clique clique : algorithm.getAssetNetwork().getJunctionTree().getCliques()) {
							assetTables.put(clique, (PotentialTable) clique.getProbabilityFunction().clone());
						}
						for (Separator separator : algorithm.getAssetNetwork().getJunctionTree().getSeparators()) {
							assetTables.put(separator, (PotentialTable) separator.getProbabilityFunction().clone());
						}
					}
					assetTableMap.put(algorithm, assetTables);
				}
			}
		}
		/** Restore the {@link MarkovEngineImpl} to the state of this memento */
		protected void restore() {
			synchronized (getProbabilisticNetwork()) {
				for (Clique clique : getProbabilisticNetwork().getJunctionTree().getCliques()) {
					clique.getProbabilityFunction().setValues(probTableMap.get(clique).getValues());
//					clique.getProbabilityFunction().copyData();
				}
				for (Separator separator : getProbabilisticNetwork().getJunctionTree().getSeparators()) {
					separator.getProbabilityFunction().setValues(probTableMap.get(separator).getValues());
//					separator.getProbabilityFunction().copyData();
				}
				// make sure marginals are restored.
				for (Node node : getProbabilisticNetwork().getNodes()) {
					((TreeVariable)node).updateMarginal();
				}
			}
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				for (AssetAwareInferenceAlgorithm algorithm : getUserToAssetAwareAlgorithmMap().values()) {
					Map<IRandomVariable,PotentialTable> assetTables = assetTableMap.get(algorithm);
					if (assetTables == null) {
						continue;
					}
					synchronized (algorithm.getAssetNetwork()) {
						for (Clique clique : algorithm.getAssetNetwork().getJunctionTree().getCliques()) {
							clique.getProbabilityFunction().setValues(assetTables.get(clique).getValues());
//							clique.getProbabilityFunction().copyData();
						}
						for (Separator separator : algorithm.getAssetNetwork().getJunctionTree().getSeparators()) {
							separator.getProbabilityFunction().setValues(assetTables.get(separator).getValues());
//							separator.getProbabilityFunction().copyData();
						}
					}
				}
			}
		}
		public Map<IRandomVariable, PotentialTable> getProbTableMap() {
			return new HashMap<IRandomVariable, PotentialTable>(probTableMap);
		}
		public Map<AssetAwareInferenceAlgorithm, Map<IRandomVariable, PotentialTable>> getAssetTableMap() {
			return new HashMap<AssetAwareInferenceAlgorithm, Map<IRandomVariable,PotentialTable>>(assetTableMap);
		}
	}
	
	/**
	 * This is useful for reverting the last trade.
	 * @return memento object storing the state of the markov engine.
	 */
	public ProbabilityAndAssetTablesMemento getMemento() {
		return this.new ProbabilityAndAssetTablesMemento();
	}
	
	/**
	 * This is useful for reverting the last trade.
	 * @param memento  object storing the state of the markov engine.
	 */
	public void restoreMemento(ProbabilityAndAssetTablesMemento memento) {
		memento.restore();
	}

	/**
	 * @param isToThrowExceptionOnInvalidAssumptions the isToThrowExceptionOnInvalidAssumptions to set
	 */
	public void setToThrowExceptionOnInvalidAssumptions(
			boolean isToThrowExceptionOnInvalidAssumptions) {
		this.isToThrowExceptionOnInvalidAssumptions = isToThrowExceptionOnInvalidAssumptions;
	}

	/**
	 * If true, methods like {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)}
	 * or {@link #previewTrade(long, long, List, List, List)} will throw exception
	 * @return the isToThrowExceptionOnInvalidAssumptions
	 */
	public boolean isToThrowExceptionOnInvalidAssumptions() {
		return isToThrowExceptionOnInvalidAssumptions;
	}

	/**
	 * This builder is used at {@link #initialize()} and
	 * {@link #getAlgorithmAndAssetNetFromUserID(long)} in order
	 * to create new instances of {@link AssetAwareInferenceAlgorithm}
	 * @param assetAwareInferenceAlgorithmBuilder the assetAwareInferenceAlgorithmBuilder to set
	 */
	public void setAssetAwareInferenceAlgorithmBuilder(IAssetAwareInferenceAlgorithmBuilder algorithmBuilder) {
		this.assetAwareInferenceAlgorithmBuilder = algorithmBuilder;
	}

	/**
	 * This builder is used at {@link #initialize()} and
	 * {@link #getAlgorithmAndAssetNetFromUserID(long)} in order
	 * to create new instances of {@link AssetAwareInferenceAlgorithm}
	 * @return the assetAwareInferenceAlgorithmBuilder
	 */
	public IAssetAwareInferenceAlgorithmBuilder getAssetAwareInferenceAlgorithmBuilder() {
		return assetAwareInferenceAlgorithmBuilder;
	}

//	/**
//	 * If this value is > 0, then {@link #setDefaultInitialAssetTableValue(double)} will
//	 * force the initial q values to be at most this value.
//	 * {@link #getQValuesFromScore(double)} and {@link #getScoreFromQValues(double)}
//	 * will be adjusted regarding change.
//	 * @param forcedInitialQValue the forcedInitialQValue to set
//	 */
//	public void setForcedInitialQValue(double forcedInitialQValue) {
//		this.forcedInitialQValue = forcedInitialQValue;
//	}
//
//	/**
//	 * If this value is > 0, then {@link #setDefaultInitialAssetTableValue(double)} will
//	 * force the initial q values to be at most this value.
//	 * {@link #getQValuesFromScore(double)} and {@link #getScoreFromQValues(double)}
//	 * will be adjusted regarding change.
//	 * @return the forcedInitialQValue
//	 */
//	public double getForcedInitialQValue() {
//		return forcedInitialQValue;
//	}

//	/**
//	 *  
//	 * We have Assets = b*log_c(q); 
//	 *  <br/><br/>
//	 * b := {@link #getCurrentCurrencyConstant()}<br/>
//	 * c := {@link #getCurrentLogBase()}<br/>
//	 * q := q-values (values stored in q-tables)<br/>
//	 * Assets := assets (values expected by the caller of this method)<br/>
//	 *  <br/><br/>
//	 * If we want to change the relationship to Assets = b*x*log_c(q'); 
//	 * with x > 1, so that q' < q maintaining Assets intact, then:
//	 *  <br/><br/>
//	 * b*x*log_c(q') = b*log_c(q);<br/>
//	 * x = log_c(q) / log_c(q')
//	 *  <br/><br/>
//	 * x := currencyMultiplier<br/>
//	 * q' := new values to be stored in q-tables : {@link #getForcedInitialQValue()}<br/>
//	 *  <br/><br/>
//	 *  This method changes the value of x.
//	 * @param currencyMultiplier the currencyMultiplier to set
//	 * @see #setDefaultInitialAssetTableValue(double)
//	 */
//	protected void setCurrencyConstantMultiplier(double currencyMultiplier) {
//		this.currencyMultiplier = currencyMultiplier;
//	}
//
//	/**
//	 * We have Assets = b*log_c(q); 
//	 *  <br/><br/>
//	 * b := {@link #getCurrentCurrencyConstant()}<br/>
//	 * c := {@link #getCurrentLogBase()}<br/>
//	 * q := q-values (values stored in q-tables)<br/>
//	 * Assets := assets (values expected by the caller of this method)<br/>
//	 *  <br/><br/>
//	 * If we want to change the relationship to Assets = b*x*log_c(q'); 
//	 * with x > 1, so that q' < q maintaining Assets intact, then:
//	 *  <br/><br/>
//	 * b*x*log_c(q') = b*log_c(q);<br/>
//	 * x = log_c(q) / log_c(q')
//	 *  <br/><br/>
//	 * x := currencyMultiplier<br/>
//	 * q' := new values to be stored in q-tables : {@link #getForcedInitialQValue()}<br/>
//	 *  <br/><br/>
//	 *  This method changes the value of x.
//	 * @return the currencyMultiplier
//	 * @see #setDefaultInitialAssetTableValue(double)
//	 */
//	protected double getCurrencyConstantMultiplier() {
//		return currencyMultiplier;
//	}
	

	/**
	 * If true, {@link #previewTrade(long, long, List, List, List)} will 
	 * actually do a trade on a cloned network.
	 * @param isToDoFullPreview the isToDoFullPreview to set
	 */
	public void setToDoFullPreview(boolean isToDoFullPreview) {
		this.isToDoFullPreview = isToDoFullPreview;
	}

	/**
	 * If true, {@link #previewTrade(long, long, List, List, List)} will 
	 * actually do a trade on a cloned network.
	 * @return the isToDoFullPreview
	 */
	public boolean isToDoFullPreview() {
		return isToDoFullPreview;
	}


	/**
	 * @return the isToUseQValues
	 */
	public boolean isToUseQValues() {
		return isToUseQValues;
	}

	/**
	 * @param isToUseQValues the isToUseQValues to set
	 */
	public void setToUseQValues(boolean isToUseQValues) {
		this.isToUseQValues = isToUseQValues;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "[isToUseQValues=" + isToUseQValues()  
		+ ", isToDeleteResolvedNode=" + isToDeleteResolvedNode() 
		+ ", isToObtainProbabilityOfResolvedQuestions=" + isToObtainProbabilityOfResolvedQuestions()
		+ ", isToThrowExceptionOnInvalidAssumptions=" + isToThrowExceptionOnInvalidAssumptions()
		+ "]";
	}

	/**
	 * @param userId : user to consider
	 * @return a list of what questions were traded by this user. This is a non-null value.
	 * This list is updated in {@link AddTradeNetworkAction#execute()}
	 */
	public List<Long> getTradedQuestions(Long userId) {
		if (getTradedQuestionsMap().containsKey(userId)) {
			// do not return the original list, because we do not want the map to be changed from external access.
			return new ArrayList<Long>(getTradedQuestionsMap().get(userId));
		}
		// do not return null.
		return Collections.emptyList();
	}

	/**
	 * Map which stores which questions were traded by a user
	 * @param tradedQuestionsMap the tradedQuestionsMap to set
	 */
	protected void setTradedQuestionsMap(Map<Long, List<Long>> tradedQuestionsMap) {
		this.tradedQuestionsMap = tradedQuestionsMap;
	}

	/**
	 * Map which stores which questions were traded by a user
	 * @return the tradedQuestionsMap
	 */
	protected Map<Long, List<Long>> getTradedQuestionsMap() {
		return tradedQuestionsMap;
	}

	/**
	 * If true, {@link #getScoreSummary(long, Long, List, List)} and {@link #getScoreSummaryObject(long, Long, List, List)}
	 * will return a specification of how {@link #scoreUserEv(long, List, List)} is calculated
	 * from clique potentials and asset tables. If false, {@link #getScoreSummary(long, Long, List, List)} and {@link #getScoreSummaryObject(long, Long, List, List)}
	 * will have the default behavior: return {@link #scoreUserQuestionEv(long, Long, List, List)}
	 * @param isToReturnEVComponentsAsScoreSummary the isToReturnEVComponentsAsScoreSummary to set
	 */
	public void setToReturnEVComponentsAsScoreSummary(
			boolean isToReturnEVComponentsAsScoreSummary) {
		this.isToReturnEVComponentsAsScoreSummary = isToReturnEVComponentsAsScoreSummary;
	}

	/**
	 * If true, {@link #getScoreSummary(long, Long, List, List)} and {@link #getScoreSummaryObject(long, Long, List, List)}
	 * will return a specification of how {@link #scoreUserEv(long, List, List)} is calculated
	 * from clique potentials and asset tables. If false, {@link #getScoreSummary(long, Long, List, List)} and {@link #getScoreSummaryObject(long, Long, List, List)}
	 * will have the default behavior: return {@link #scoreUserQuestionEv(long, Long, List, List)}
	 * @return the isToReturnEVComponentsAsScoreSummary
	 */
	public boolean isToReturnEVComponentsAsScoreSummary() {
		return isToReturnEVComponentsAsScoreSummary;
	}
	
}
