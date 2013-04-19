package edu.gmu.ace.daggre;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
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
import unbbayes.prs.bn.inference.extension.AssetPropagationInferenceAlgorithm.AssetPropagationInferenceAlgorithmMemento;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm.IAssetNetAlgorithmMemento;
import unbbayes.prs.bn.inference.extension.IQValuesToAssetsConverter;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import edu.gmu.ace.daggre.ScoreSummary.SummaryContribution;

/**
 * This is the default implementation of {@link MarkovEngineInterface}.
 * This class is basically a wrapper for the functionalities offered by
 * {@link AssetAwareInferenceAlgorithm}. 
 * It adds history feature and transactional behaviors to
 * {@link AssetAwareInferenceAlgorithm}.
 * @author Shou Matsumoto
 * @version January 30, 2013
 */
public class MarkovEngineImpl implements MarkovEngineInterface, IQValuesToAssetsConverter {
	
	private boolean isToUseQValues = false;
	
	private boolean isToForceBalanceQuestionEntirely = false;//true;

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

	private float probabilityErrorMargin = 0.001f;
	
	private float probabilityErrorMarginBalanceTrade = 0.000000005f;
	
	private float assetErrorMargin = 0.5f;

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
	
	private boolean isToThrowExceptionOnInvalidAssumptions = true;

//	/** Set this value to < 0 to disable this feature */
//	private float forcedInitialQValue = 100.0;
//
//	private double currencyMultiplier = 1.0;

	private boolean isToDoFullPreview = false;

	private Map<Long, Set<Long>> tradedQuestionsMap;

	private boolean isToReturnEVComponentsAsScoreSummary = false;


	private Map<Long,StatePair> resolvedQuestionsAndNumberOfStates;


	private boolean isToIntegrateConsecutiveResolutions = true;


	private boolean isToRetriveOnlyTradeHistory = true;

	/** This is the default initial value of {@link #getMaxConditionalProbHistorySize()} */
	public static int DEFAULT_MAX_CONDITIONAL_PROB_HISTORY_SIZE = 10;

	private int maxConditionalProbHistorySize = DEFAULT_MAX_CONDITIONAL_PROB_HISTORY_SIZE;

	private HashMap<Clique, List<ParentActionPotentialTablePair>> lastNCliquePotentialMap;


	private BaseIO io;


	private float nodePositionRandomRange = 800;
	
	public static String DEFAULT_EXPORTED_NODE_PREFIX = "N";
	
	private String exportedNodePrefix = DEFAULT_EXPORTED_NODE_PREFIX;



	private Map<VirtualTradeAction, Set<Long>> virtualTradeToAffectedQuestionsMap;

	private boolean isToStoreOnlyCliqueChangeHistory = true;

	/** If set to true, all nodes in a same clique will be fully connected during {@link RebuildNetworkAction#execute()},
	 * so that newly created cliques are always extensions of old cliques. */
	private boolean isToFullyConnectNodesInCliquesOnRebuild = true;
	
	/**
	 * If true, actions created in {@link RebuildNetworkAction#execute()} to fully connect nodes in edited clique
	 * will be included in to history, so that next rebuild will re-use the same arcs. <br/>
	 * CAUTION: re-using the arcs may need extra care not to include cycles.
	 * @see #isToFullyConnectNodesInCliquesOnRebuild()
	 */
	private boolean isToIncludeFullConnectActionsOfRebuildIntoHistory = false;
	
	/** If false, {@link CliqueSensitiveTradeSpecification#getClique()} will be set to null after execution of balance trade, and
	 * old cliques will not remain in history */
	private boolean isToKeepCliquesOfBalanceTradeInMemory = false;
	
	/** If true, balancing trades will always be re-calculated (instead of precisely re-executing previous balancing trades) 
	 * when actions are re-executed from history 
	 * @see RebuildNetworkAction#execute()
	 * @see BalanceTradeNetworkAction#execute()*/
	private boolean isToRecalculateBalancingTradeOnRebuild = false;//isToForceBalanceQuestionEntirely;
	
	/** If true, marginals of actions executed in {@link RebuildNetworkAction#execute()} will be compared to the marginals which can be found in the history */
	private boolean isToCompareProbOnRebuild = false;
	
	/** If true, users will be only initialized if they make trade. */
	private boolean isToLazyInitializeUsers = true;
	
	/** This is mainly used for finding cycles in {@link RebuildNetworkAction#execute()} */
	private MSeparationUtility mseparationUtility = MSeparationUtility.newInstance();

	/** This is used in lazy user initialization. If user did never trade, but assets were queried, this map will be used. */
	private Map<Long, Float> uninitializedUserToAssetMap;

	/** If true, {@link #previewBalancingTrades(long, long, List, List)}, {@link #previewBalancingTrade(long, long, List, List)}, and
	 * {@link BalanceTradeNetworkAction#execute()} will also return balancing trades that may result in negative assets*/
	private boolean isToAllowNegativeInBalanceTrade = false;

	/** If true, {@link #collapseSimilarBalancingTrades(List)} will try to group similar balancing trades into 1 trade */
	private boolean isToCollapseSimilarBalancingTrades = false;
	
	
	
	/** If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)}
	 * will attempt to use house account to run corrective trades when the old probabilities provided by the caller is different
	 * from the actual probabilities retrieved from the Bayes net before trade. */
	private boolean isToUseCorrectiveTrades = true;

	/** If true, {@link #commitNetworkActions(long, boolean)} will place all {@link AddCashNetworkAction} before trades. */
	private boolean isToSortAddCashAction = true;
	
	/** If true, {@link RebuildNetworkAction#execute()} will "see" trades which are in the same transaction and created after the rebuild action. */
	private boolean isToLookAheadForTradesCreatedAfterRebuild = true;

	/** If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)} will
	 * throw exception when the question has probability 0 or 1 in any state */
	private boolean isToThrowExceptionInTradesToResolvedQuestions = false;
	
	/** If true, cash of each user will be stored when questions are resolved */
	private boolean isToStoreCashBeforeResolveQuestion = true;
	
	/** This is a map which stores which user has gained how much cash in what resolved questions. The mapping is: userId -> (questionId -> cash gain). */
	private Map<Long, Map<Long,Float>> userIdToResolvedQuestionCashGainMap;
	
	/** This is a map which stores what was the user's cash before resolved questions. The mapping is: userId -> (questionId -> cash). */
	private Map<Long, Map<Long,Float>> userIdToResolvedQuestionCashBeforeMap;

	/** If true, {@link PrintNetStatisticsNetworkAction} will be inserted into the actions to be executed in {@link #commitNetworkActions(long, boolean)}
	 * immediately after the last occurrence of {@link StructureChangeNetworkAction} */
	private boolean isToPrintNetStatisticsAfterLastStructureChange = false;

	/** If true, {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} will just return for uninitialized 
	 * users which has no position even after other (non-executed) actions the same transaction */
	private boolean isToAddUninitializedUserInBalanceTradeOfHugeTransaction = true;

	private Map<Long, Set<Long>> questionsToBeCreatedInTransaction = new HashMap<Long, Set<Long>>(); 
	
	
	/**
	 * Default constructor is protected to allow inheritance.
	 * Use {@link #getInstance()} to actually instantiate objects of this class.
	 */
	protected MarkovEngineImpl() {
		setIO(new NetIO());
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
		return MarkovEngineImpl.getInstance(logBase, currencyConstant, initialUserAssets, isToThrowExceptionOnInvalidAssumptions, false, DEFAULT_MAX_CONDITIONAL_PROB_HISTORY_SIZE);
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
			boolean isToThrowExceptionOnInvalidAssumptions, boolean isToUseQValues, int maxConditionalProbHistorySize) {
		MarkovEngineImpl ret = (MarkovEngineImpl) MarkovEngineImpl.getInstance();
		ret.setMaxConditionalProbHistorySize(maxConditionalProbHistorySize);
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
		defaultAlgorithm.setToUpdateAssets(false);					// optimization: do not update assets at all for the default (markov engine, or "house") user
		setDefaultInferenceAlgorithm(defaultAlgorithm);				
		
		
		
		// several methods in this class reuse the same conditional probability extractor. Extract it here
		setConditionalProbabilityExtractor(jeffreyRuleLikelihoodExtractor.getConditionalProbabilityExtractor());
		
		// initialize the map managing users (and the algorithms responsible for changing values of users asset networks)
		setUserToAssetAwareAlgorithmMap(new HashMap<Long, AssetAwareInferenceAlgorithm>()); // concurrent hash map is known to be thread safe yet fast.
		
		// initialize map which manages which questions have trades from a given user
		setTradedQuestionsMap(new HashMap<Long, Set<Long>>());
		
		// initialize set containing what questions were resolved
		setResolvedQuestions(new HashMap<Long,StatePair>());
		
		// initialize the history of last N clique potentials, with N = getMaxConditionalProbHistorySize()
		setLastNCliquePotentialMap(new HashMap<Clique, List<ParentActionPotentialTablePair>>());
		
		// list of all instances of virtual trades created by this markov engine in order to trace the marginal probabilities
		setVirtualTradeToAffectedQuestionsMap(new HashMap<VirtualTradeAction, Set<Long>>());
		
		// map of users loaded lazily
		setUninitializedUserToAssetMap(new HashMap<Long, Float>());
		
		// mapping from resolved (usually deleted afterwards) questions to what where their resolutions and total number of states
		setResolvedQuestions(new HashMap<Long,StatePair>());
		
		// mapping of cash gains per resolved questions
		setUserIdToResolvedQuestionCashGainMap(new HashMap<Long, Map<Long,Float>>());
		
		// mapping of cash before resolved question
		setUserIdToResolvedQuestionCashBeforeMap(new HashMap<Long, Map<Long,Float>>());
		
		// also reset the mapping of questions being created in transactions
		synchronized (getQuestionsToBeCreatedInTransaction()) {
			getQuestionsToBeCreatedInTransaction().clear();
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#startNetworkActions()
	 */
	public long startNetworkActions() {
		long ret = Long.MIN_VALUE;
		synchronized (transactionCounter) {
			if (transactionCounter == Long.MAX_VALUE) {
				// rotate explicitly to negative value instead of doing overflow
				ret = transactionCounter;
				transactionCounter = Long.MIN_VALUE;
			} else if (transactionCounter == -1L) {
				// avoid using the 0.
				ret = transactionCounter;
				transactionCounter = 1L;
			} else {
				ret = ++transactionCounter;
			}
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
		return this.commitNetworkActions(transactionKey, true);
	}
	
	/**
	 * @param isToRebuildNetOnStructureChange : if false, it does not rebuild network when a change in structure is detected.
	 * If true, it will rebuild (re-generate network structure and call {@link IInferenceAlgorithm#run()}).
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#commitNetworkActions(long)
	 */
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
			if (isToSortAddCashAction) {
				// only instantiate if we will use it
				addCashActions = new ArrayList<NetworkAction>();
			}
			List<NetworkAction> otherActions = new ArrayList<NetworkAction>();	// will store actions which does not change network structure
			// collect all network change actions and other actions, regarding their original order
			boolean isToRebuildFromHistory = false;	// if an action which is set as a trigger for rebuild is found, this flag will turn on
			for (NetworkAction action : actions) {
				if (action.isTriggerForRebuild()) {
					// there is at least one trigger for re-running history, so remember that and instantiate a RebuildNetworkAction afterwards.
					isToRebuildFromHistory = true;
				}
				if (action.isStructureConstructionAction()) {
					netChangeActions.add(action);
				} else if (isToSortAddCashAction && (action instanceof AddCashNetworkAction)) {
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
				
				if (!isToRebuildFromHistory) {
					// netChangeActions comes first, if they do not trigger a RebuildNetworkAction.
					actions.addAll(netChangeActions); 
				} else {
					/*
					 * mark the structure change actions as executed, 
					 * so that RebuildNetworkAction can handle them when rebuilding network.
					 * CAUTION: be careful not to change the behavior of RebuildNetworkAction
					 */
					for (NetworkAction action : netChangeActions) {
						if (action == null) {
							continue;	// ignore null entries
						}
						// mark action as executed
						action.setWhenExecutedFirstTime(new Date());	// normal execution of action -> update attribute "whenExecutedFirst"
						synchronized (getExecutedActions()) {
							// this is partially ordered by NetworkAction#getWhenCreated(), because actions is ordered by NetworkAction#getWhenCreated()
							getExecutedActions().add(action);	
						}
					}
					// Note: if they trigger a RebuildNetworkAction, then actions in getExecutedActions() will be re-executed, 
					// and netChangeActions will be executed anyway during rebuild, so no need to add them into actions.
					rebuildNetworkAction = new RebuildNetworkAction(netChangeActions.get(0).getTransactionKey(), new Date(), null, null); // negative millisecond means rebuild filter not specified by date/time
					actions.add(rebuildNetworkAction);	// <rebuild action> is inserted before addCashActions and otherActions
				}
				if (isToPrintNetStatisticsAfterLastStructureChange()) {
					actions.add(new PrintNetStatisticsNetworkAction());
				}
				if (isToSortAddCashAction) {
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
//					if (action.getUserId() != null && action.getUserId().longValue() == 3487L && action.getQuestionId() != null && action.getQuestionId().longValue() == 208L) {
//						try {
//							AssetNetwork assetNetwork = getAlgorithmAndAssetNetFromUserID(action.getUserId()).getAssetNetwork();
//							System.out.println(i+" [Before] User = " + assetNetwork);
//							for (Clique clique : assetNetwork.getJunctionTree().getCliquesContainingAllNodes((List)Collections.singletonList(assetNetwork.getNode(action.getQuestionId().toString())), Integer.MAX_VALUE)) {
//								System.out.println(i+" [Before] Clique = " + clique);
//								System.out.print("[ ");
//								for (float asset : clique.getProbabilityFunction().getValues()) {
//									System.out.print( asset  + " , ");
//								}
//								System.out.print(" ]");
//							}
//							System.out.println(i+" [Before] Score = " + scoreUserEv(action.getUserId(), null, null));
//							System.out.println(i+" [Before] Cash = " + getCash(action.getUserId(), null, null));
//							System.out.println(i+" [Before] Score per state of Q" + action.getQuestionId() +  " = " + scoreUserQuestionEvStates(action.getUserId(), action.getQuestionId(), null, null));
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						
//					}
					action.execute();
//					if (action.getUserId() != null && action.getUserId().longValue() == 3487L && action.getQuestionId() != null && action.getQuestionId().longValue() == 208L) {
//						try {
//							AssetNetwork assetNetwork = getAlgorithmAndAssetNetFromUserID(action.getUserId()).getAssetNetwork();
//							System.out.println(i+ "[After] User = " + assetNetwork);
//							for (Clique clique : assetNetwork.getJunctionTree().getCliquesContainingAllNodes((List)Collections.singletonList(assetNetwork.getNode(action.getQuestionId().toString())), Integer.MAX_VALUE)) {
//								System.out.println(i+ "[After] Clique = " + clique);
//								System.out.print("[ ");
//								for (float asset : clique.getProbabilityFunction().getValues()) {
//									System.out.print( asset  + " , ");
//								}
//								System.out.print(" ]");
//							}
//							System.out.println(i+ "[After] Score = " + scoreUserEv(action.getUserId(), null, null));
//							System.out.println(i+ "[After] Cash = " + getCash(action.getUserId(), null, null));
//							System.out.println(i+ "[After] Score per state of Q" + action.getQuestionId() +  " = " + scoreUserQuestionEvStates(action.getUserId(), action.getQuestionId(), null, null));
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						
//					}
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
			
			// remove from index of questions being created in transaction
			synchronized (getQuestionsToBeCreatedInTransaction()) {
				getQuestionsToBeCreatedInTransaction().remove(transactionKey);
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
		private static final long serialVersionUID = 8411524472507631510L;
		private final long whenCreated;
		private final long transactionKey;
		private long whenExecutedFirst = -1;
		private final long tradesStartingWhen;
		private final Long questionId;
		/**
		 * Default constructor.
		 * @param transactionKey : mandatory key which can be obtained from {@link MarkovEngineImpl#startNetworkActions()}
		 * @param whenCreated : when this action was created.
		 * @param tradesStartingWhen : filter for the playback. Playback will stop after the first
		 * {@link AddTradeNetworkAction} created after this date (represented as milliseconds - see {@link Date#getTime()}). If < 0, all actions will be played back.
		 * @param questionId : another filter for the playback. 
		 * If tradesStartingWhen == null, this argument will be ignored.
		 * Playback will stop after the first
		 * {@link AddTradeNetworkAction} with {@link AddTradeNetworkAction#getQuestionId()} matching
		 * this questionId and created after tradesStartingWhen.
		 */
		public RebuildNetworkAction(long transactionKey, Date whenCreated, Date tradesStartingWhen, Long questionId) {
			this.transactionKey = transactionKey;
			this.whenCreated = (whenCreated==null)?-1:whenCreated.getTime();
			this.tradesStartingWhen = (tradesStartingWhen==null)?-1:tradesStartingWhen.getTime();
			this.questionId = questionId;
		}
		/** Rebuild the BN */
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			
			// we must re-run all executed history.
			// CAUTION: it is expected that the commit method has also included the new changes in network structure here in this list
			List<NetworkAction> actions = getExecutedActions();
			
			// network to be reset
			ProbabilisticNetwork net = getProbabilisticNetwork();
			
			// the list "actions" without network change actions
			List<NetworkAction> actionsToExecute = new ArrayList<NetworkAction>();	
			
			// TODO check if we really need to synchronize on actions 
			// (because commit is supposedly synchronized already, and actions are only modified in commit)
			synchronized (actions) {
				
				// separate the content of actions into those building network structure and those not changing structure
				List<NetworkAction> networkConstructionActions = new ArrayList<NetworkAction>(); 
				
				// trace what cliques shall become fully connected.
				Set<List<Long>> probCliquesToFullyConnect = null;	// use Ids, because the nodes may have been resolved (thus removed)
				if (isToFullyConnectNodesInCliquesOnRebuild()) {
					// only use extra space in memory if needed
					probCliquesToFullyConnect = new HashSet<List<Long>>();
				}
				
				// check the content of actions, and separate them by type of action (whether it changes the net structure or not)
				for (NetworkAction action : actions) {
					
					if (action.isStructureConstructionAction()) {
						
						// this one changes network structure
						networkConstructionActions.add(action);
						// note: "resolve questions" are not included here 
						
					} else {	// this one does not change network structure or it is a "resolve question"
						// check if action is a trade, and if so, fully connect questions used by the trade
						probCliquesToFullyConnect = this.addActionIntoSetOfQuestionsToFullyConnect(probCliquesToFullyConnect, action);
						
						// this action will be executed after the network structure is re-generated
						actionsToExecute.add(action);
						
					}
				}
				
				// also fill probCliquesToFullyConnect regarding the actions which were not executed, but are in same transaction
				if (getTransactionKey() != null) {
					// Obtain the timestamp of the last action which changed network structure. 
					// We don't need to handle trades happening after creation of such action.
					// TODO this is a weak check, because events happening at the same millisecond will also be included.
					Date lastNetConstructionActionTimestamp = networkConstructionActions.get(networkConstructionActions.size()-1).getWhenCreated();
//					long lastNetConstructionActionTimestamp = networkConstructionActions.get(networkConstructionActions.size()-1).getWhenCreatedMillis();
					
					// extract the actions pertaining in the same transaction
					List<NetworkAction> actionsInSameTransaction = getNetworkActionsMap().get(getTransactionKey());
					// TODO perhaps we can optimize this, because getNetworkActionsMap().get(getTransactionKey()) was sweeped previously at commitNetworkAction
					if (actionsInSameTransaction != null) {
						// handle actions in the same transaction until we find the 1st action executed strictly after the last action which changes net structure 
						for (NetworkAction action : actionsInSameTransaction) {
							if (!(action instanceof AddTradeNetworkAction)){
								// only consider trades. Ignore other types of actions.
								// this is important, because there's no guarantee that other types of actions are sorted by action.getWhenCreated()
								continue;
							}
							if (!isToLookAheadForTradesCreatedAfterRebuild()
									&& lastNetConstructionActionTimestamp.before(action.getWhenCreated())) {
								// do not fully connect questions regarding trades which were created strictly after the last net construction action
								// Note: we are assuming that trades are sorted by action.getWhenCreated().
								break;
							}
							probCliquesToFullyConnect = this.addActionIntoSetOfQuestionsToFullyConnect(probCliquesToFullyConnect, action);
						}
					}
				}
				
				
				// clear the current network and junction tree now, so that we can re-generate the structure by running networkConstructionActions posteriorly
				synchronized (getDefaultInferenceAlgorithm()) {
					synchronized (net) {
						// clear content of net in a fast way
						net.clear();
						
						// create a new junction tree
						try {
							net.setJunctionTree(net.getJunctionTreeBuilder().buildJunctionTree(net));
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
				}
				
				// execute the trades related to network construction before actionsToExecute
				for (NetworkAction changeAction : networkConstructionActions) {
					// no need to call changeAction.setWhenExecutedFirstTime(whenExecutedFirst), because it was supposedly executed during commitNetworkActions(transactionKey)
					if (changeAction instanceof AddQuestionNetworkAction) {
						// TODO use dependency injection in order to avoid using if-instanceof
						synchronized (getProbabilisticNetwork()) {
							((AddQuestionNetworkAction)changeAction).execute(getProbabilisticNetwork(), false);	// false := do not update junction tree now
						}
					} else {
						changeAction.execute();
					}
					/*
					 * Caution: do not change this behavior (recreate network structure
					 * using the actions from the history), because
					 * #commitNetworkAction is relying in this behavior
					 * in order to add edges before resolution of nodes
					 */
				}
				
				// fully connect nodes in cliques only after the new structure is ready, so that we check path and avoid cycles.
				if (isToFullyConnectNodesInCliquesOnRebuild()) {
					
					// fully connect nodes in cliques affected by the newly added arcs. 
					for (List<Long> idsOfNodesInClique : probCliquesToFullyConnect) {
						
						// iterate over combinations of 2 questions in the list 
						for (int i = 0; i < idsOfNodesInClique.size()-1; i++) {
							for (int j = i+1; j < idsOfNodesInClique.size(); j++) {
								
								// extract the 2 nodes in the new (rebuilt) network. Same nodes supposedly have same names (ids).
								Node node1 = getProbabilisticNetwork().getNode(idsOfNodesInClique.get(i).toString());
								Node node2 = getProbabilisticNetwork().getNode(idsOfNodesInClique.get(j).toString());
								
								// basic assertion
								if (node1 == null || node2 == null || node1.equals(node2)) {
									Debug.println(getClass(), 
											"Could not find "+ i + "th node or " + j + "th node in clique " + idsOfNodesInClique 
											+ ", or they were the same node.");
									continue;	// ignore and try the best
								}
								
								// check if there is an arc between these 2 nodes
								if (node1.getParents().contains(node2) || node2.getParents().contains(node1)){
									// ignore this combination of nodes, because they are already connected
									continue;
								}
								
								// we shall create an arc node1->node2 or node2->node1, but without creating cycles. 
								// Supposedly, this will connect parents of children (which are dependencies but not connected directly)
								
								// prepare the placeholder for the action which will add the new arc
								AddQuestionAssumptionNetworkAction action = null;
								
								// use the m-separation utility component in order to find a path between node1 and node2
								if (getMSeparationUtility().getRoutes(node1, node2, null, null, 1).isEmpty()) {
									
									// there is no route from node1 to node2, so we can create node2->node1 without generating cycle
									action = new AddQuestionAssumptionNetworkAction(
											getTransactionKey(), getWhenCreatedMillis(), Long.parseLong(node1.getName()), Collections.singletonList(Long.parseLong(node2.getName())), null);
									
								} else { // supposedly, we can always add edges in one of the directions (i.e. there is no way we add arc in each direction and both result in cycle)
									
									// there is a route from node1 to node2, so we cannot create node2->node1 (it will create a cycle if we do so), so create node1->node2
									action = new AddQuestionAssumptionNetworkAction(
											getTransactionKey(), getWhenCreatedMillis() , Long.parseLong(node2.getName()), Collections.singletonList(Long.parseLong(node1.getName())), null);
								}
								
								// check if we should consider adding this new arc into the history, so that they reappear when rebuilding the network again
								// CAUTION: if we add them to history, the direction will be virtually fixed 
								// (so it may result in undesired restrictions in the future if we don't want cycles)
								if (isToIncludeFullConnectActionsOfRebuildIntoHistory()) {
									
									// mark action as executed before it is actually executed 
									// (this is the default behavior of commitNetworkTransaction on actions changing net structure)
									action.setWhenExecutedFirstTimeMillis(System.currentTimeMillis());	
									
									// add action into history
									synchronized (getExecutedActions()) {
										getExecutedActions().add(action);	
									}
								}
								
								// execute the new action which connects nodes in cliques
								action.execute();
							}
						}
					}
				}
				
				
				
			}
			
			// clear the mapping which stores the resolved questions, so that the actions during the rebuild do not think that questions are resolved already
			synchronized (getResolvedQuestions()) {
				getResolvedQuestions().clear();
			}
			
			// recompile network and execute actionsToExecute (actions which does not change network structure) now
			this.execute(actionsToExecute); 
			
			try {
				Debug.println(getClass(), "Executed rebuild, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		/**
		 * Checks what questions (main question and assumptions) are used in the NetworkAction argument and
		 * includes them in the Set argument.
		 * This is used posteriorly in {@link #execute()} in order to fully connect such questions,
		 * so that trades made previously are still in the same clique.
		 * Basically, this method checks whether the action is a trade (balancing trades inclusively), 
		 * and if so, it fully connects the questions used by that trade.
		 * @param probCliquesToFullyConnect : the set argument to be modified.
		 * @param action : the NetworkAction object to be checked.
		 * @return probCliquesToFullyConnect
		 */
		protected  Set<List<Long>> addActionIntoSetOfQuestionsToFullyConnect( Set<List<Long>> probCliquesToFullyConnect, NetworkAction action) {
			// check what cliques were affected by trades, so that I can fully connect nodes within them.
			if (isToFullyConnectNodesInCliquesOnRebuild() && action instanceof AddTradeNetworkAction) {
				
				// NOTE: I'm assuming that when the action was executed, it executed on correct clique
				// i.e. nodes were within cliques when trade was initially executed.
				// TODO check if this assumption is reliable
				
				// if this is a trade, we can store which nodes should be in the same clique, and what clique is it
				if (action instanceof BalanceTradeNetworkAction){
					
					BalanceTradeNetworkAction balanceTrade = (BalanceTradeNetworkAction) action;
					
					if (balanceTrade.getExecutedTrades() == null) {	// this was never executed
						// store the combinations of questions regarding what the user specified in balancing trade
						if (balanceTrade.getQuestionId() != null && balanceTrade.getAssumptionIds() != null && !balanceTrade.getAssumptionIds().isEmpty()) {
							// variable to store the ids of the assumptions and traded question
							List<Long> questionsInThisTradeSpec = new ArrayList<Long>();
							
							// questionsInThisTradeSpec will contain the traded node...
							questionsInThisTradeSpec.add(balanceTrade.getQuestionId());
							
							// ... and all its assumptions
							// NOTE: I'm assuming that tradeSpec.getAssumptionIds() won't have repetitions and don't include the traded question itself
							questionsInThisTradeSpec.addAll(balanceTrade.getAssumptionIds());
							
							// store the questions to become fully connect
							// note: at this point, 
							// since balanceTrade.getQuestionId() != null && balanceTrade.getAssumptionIds() != null && !balanceTrade.getAssumptionIds().isEmpty(),
							// questionsInThisTradeSpec has always 2 or more elements.
							probCliquesToFullyConnect.add(questionsInThisTradeSpec);
						} 
					} else {	// this balancing trade was executed already
						// balance trades have special treatment, because it is actually a set of several trades
						for (TradeSpecification tradeSpec : balanceTrade.getExecutedTrades()) { // iterate over the set of trades this balance trade represents
							
							// Note: since we are storing ids instead of the clique itself, we do not need to check whether this 
							// trade specification is CliqueSensitiveTradeSpecification or not
							
							// variable to store the ids of the assumptions and traded question
							List<Long> questionsInThisTradeSpec = new ArrayList<Long>();
							
							// questionsInThisTradeSpec will contain the traded node...
							questionsInThisTradeSpec.add(tradeSpec.getQuestionId());
							
							// ... and all its assumptions
							if (tradeSpec.getAssumptionIds() != null) {
								// NOTE: I'm assuming that tradeSpec.getAssumptionIds() won't have repetitions and don't include the traded question itself
								questionsInThisTradeSpec.addAll(tradeSpec.getAssumptionIds());
							}
							
							// store the questions to become fully connect
							if (questionsInThisTradeSpec.size() > 1) {
								probCliquesToFullyConnect.add(questionsInThisTradeSpec);
							}
						}
					}
				} else { // treat all other types of trades as ordinal trades
					
					// variable to store the ids of the assumptions and traded question
					List<Long> questionsInThisTrade = new ArrayList<Long>();
					
					// questionsInThisTrade will contain the traded node...
					questionsInThisTrade.add(action.getQuestionId());
					
					// ... and all its assumptions
					if (action.getAssumptionIds() != null) {
						// NOTE: I'm assuming that action.getAssumptionIds() won't have repetitions and don't include the traded question itself
						questionsInThisTrade.addAll(action.getAssumptionIds());
					}
					
					// store the questions to become fully connect
					if (questionsInThisTrade.size() > 1) {
						probCliquesToFullyConnect.add(questionsInThisTrade);
					}
				}
			}
			return probCliquesToFullyConnect;
		}
		
		/** Rebuild the BN */
		public void execute(List<NetworkAction> actions) {
			// BN to rebuild
			ProbabilisticNetwork net = getProbabilisticNetwork();
			// Supposedly, only 1 transaction can commit, so we only need to block net to block threads reading net
			synchronized (getDefaultInferenceAlgorithm()) {
				synchronized (net) {
					if (net.getNodeCount() > 0) {
						getDefaultInferenceAlgorithm().run();
					}
					getLastNCliquePotentialMap().clear();
				}
			}
			// TODO instead of clearing and re-doing virtual trades, keep them and do not add them again
			// clean record of virtual trades created before
			synchronized (getVirtualTradeToAffectedQuestionsMap()) {
				synchronized (getNetworkActionsIndexedByQuestions()) {
					// getVirtualTradeToAffectedQuestionsMap() is a mapping from virtual trade to questions
					for (VirtualTradeAction virtualTrade : getVirtualTradeToAffectedQuestionsMap().keySet()) {
						// getNetworkActionsIndexedByQuestions() is a mapping from questions to actions (including virtual trades)
						for (Long relatedQuestion : getVirtualTradeToAffectedQuestionsMap().get(virtualTrade)) {
							// delete from the "normal" mapping
//							getNetworkActionsIndexedByQuestions().get(relatedQuestion).remove(virtualTrade);
							removeNetworkActionFromQuestionMap(virtualTrade, relatedQuestion);
						}
					}
					// clean the "inverse" mapping
					getVirtualTradeToAffectedQuestionsMap().clear();
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
							
							// compare probability before 
							if (isToCompareProbOnRebuild()
									&& (action instanceof AddTradeNetworkAction || action instanceof ResolveQuestionNetworkAction)) {
								List<Float> probList = getProbList(action.getQuestionId(), null, null);
								for (int i = 0; i < probList.size(); i++) {
									if (Math.abs(probList.get(i) - action.getOldValues().get(i)) > getProbabilityErrorMargin()){
										if (action instanceof AddTradeNetworkAction) {
											throw new IllegalStateException("Re-execution before trade " + action.getTradeId() 
													+ " has caused marginal of question " + action.getQuestionId() 
													+ " to change from " + action.getOldValues() + " to " + probList);
										} else {
											throw new IllegalStateException("Re-execution of action before resolution of question " 
													+ action.getQuestionId() + " to state " + action.getSettledState()
													+ " has caused its probability to change from " + action.getOldValues() + " to " + probList);
										}
									}
								}
							}
							
							// redo all trades using the history
							// actions with action.isStructureChangeAction() == false are supposedly the ones changing probabilities/assets and not changing structure
							action.execute(); // this is not an ordinal execution of the action, so do not update attribute whenExecutedFirst.
							
							// compare probability after 
							if (isToCompareProbOnRebuild() && action instanceof AddTradeNetworkAction) {
								List<Float> probList = getProbList(action.getQuestionId(), null, null);
								for (int i = 0; i < probList.size(); i++) {
									if (Math.abs(probList.get(i) - action.getNewValues().get(i)) > getProbabilityErrorMargin()){
										throw new IllegalStateException("Re-execution of trade " + action.getTradeId() 
												+ " has caused marginal of question " + action.getQuestionId() 
												+ " to change from " + action.getNewValues() + " to " + probList);
									}
								}
							}
						}
						
						// from this point, play back actions without updating assets in trades
						for (int i = indexOfLastActionPlayedBack; i < actions.size(); i++) {
							NetworkAction action = actions.get(i);
							if (action.equals(this)) {
								// do not execute actions which comes after myself
								break;
							}
							if (!action.isStructureConstructionAction()) {
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
//		/** By overwriting this method, you can control which actions are executed in {@link #execute(List)} */
//		protected boolean isToExecuteAction(NetworkAction action) { return true; /*!action.isStructureChangeAction();*/ }
		public Date getWhenCreated() { 
			if (whenCreated < 0) {
				return null;
			}
			return new Date(whenCreated); 
		}
		/** This action reboots the network, but does not change the structure by itself */
		public boolean isStructureConstructionAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return null; }
		public List<Float> getOldValues() { return null; }
		public void setOldValues(List<Float> oldValues) {}
		public List<Float> getNewValues() { return null; }
		public void setNewValues(List<Float> newValues) {}
		public String getTradeId() { return null; }
		/** This is non-null only if this action is trying to revert a trade related to this question ID */
		public Long getQuestionId() { return this.questionId; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirst < 0) {
				return null;
			}
			return new Date(whenExecutedFirst) ; 
		}
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirst = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirst = whenExecutedFirst.getTime(); 
			}
		}
//		/** This is a filter used when reverting trades */
		public Date getTradesStartingWhen() { 
			if (tradesStartingWhen < 0) {
				return null;
			}
			return new Date(tradesStartingWhen); 
		}
		/** This is a filter used when reverting trades, in milliseconds
		 * @see Date#getTime() */
//		public long getTradesStartingWhen() { return tradesStartingWhen; }
		public boolean isHardEvidenceAction() {return false; }
		public Integer getSettledState() {return null;}
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** This action does not trigger another rebuild */
		public boolean isTriggerForRebuild() { return false; }
		/**
		 * @return the whenCreated
		 */
		public long getWhenCreatedMillis() {
			return whenCreated;
		}
		/**
		 * @return the whenExecutedFirst
		 */
		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirst;
		}
		/**
		 * @param whenExecutedFirst the whenExecutedFirst to set
		 */
		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirst = whenExecutedFirst;
		}
	}
	
	/**
	 * This is an network action for {@link MarkovEngineImpl#revertTrade(long, Date, Date, Long)}.
	 * Currently, there is no difference to {@link RebuildNetworkAction},
	 * so this is only a placeholder for future changes.
	 * @author Shou Matsumoto
	 */
	public class RevertTradeNetworkAction extends RebuildNetworkAction {
		private static final long serialVersionUID = -8931208967252690587L;

		/** Default constructor initializing fields */
		public RevertTradeNetworkAction(long transactionKey, Date whenCreated, Date tradesStartingWhen, Long questionId) {
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
		} else {	// i.e. initProbs == null || initProbs.isEmpty()
			// Instantiate initProbs. Reuse same instance if it was passed as argument
//			if (initProbs == null) {
//				initProbs = new ArrayList<Float>();
//			}
//			// prior probability was not set. Assume it to be uniform distribution
//			for (int i = 0; i < numberStates; i++) {
//				initProbs.add(1f/numberStates);
//			}
			// the above code was migrated to AddQuestionNetworkAction#execute()
		}
		
		// instantiate the action object for adding a question
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs));
			
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
	
	/** Class of network actions which changes network structure */
	public abstract class StructureChangeNetworkAction implements NetworkAction {
		private static final long serialVersionUID = -3914118334576683558L;
		public boolean isStructureConstructionAction() { return true; }
		/**
		 * Changes the structure of the current network
		 * @see MarkovEngineImpl#getProbabilisticNetwork()
		 */
		public void execute() {
			this.execute(getProbabilisticNetwork());
		}
		/**
		 * Changes the structure of the specified network
		 * @param net : the specified network
		 */
		public abstract void execute(ProbabilisticNetwork net);
	}
	
	/**
	 * Represents a network action for adding a question into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestion(long, Date, long, int, List)
	 */
	public class AddQuestionNetworkAction extends StructureChangeNetworkAction {
		private static final long serialVersionUID = -945067175977550970L;
		private final long transactionKey;
		private final long occurredWhen;
		private final long questionId;
		private final int numberStates;
		private List<Float> initProbs;
		private long whenExecutedFirst = -1;
		private boolean isToUpdateJunctionTreeAndAssetNets = true;
		/** Default constructor initializing fields */
		public AddQuestionNetworkAction(long transactionKey, Date occurredWhen,
				long questionId, int numberStates, List<Float> initProbs) {
			super();
			this.transactionKey = transactionKey;
			this.occurredWhen = (occurredWhen == null)?-1:occurredWhen.getTime();
			this.questionId = questionId;
			this.numberStates = numberStates;
			if (initProbs != null && !initProbs.isEmpty()) {
				// use a copy, so that changes in the original do not affect this object
				this.initProbs = new ArrayList<Float>(initProbs);
			} else {
				this.initProbs = null;
			}
			isToUpdateJunctionTreeAndAssetNets = true;
		}
		public AddQuestionNetworkAction(long transactionKey, Date occurredWhen,
				long questionId, int numberStates, List<Float> initProbs, boolean isToUpdateJunctionTreeAndAssetNets) {
			this(transactionKey, occurredWhen, questionId, numberStates, initProbs);
			this.setToUpdateJunctionTreeAndAssetNets(isToUpdateJunctionTreeAndAssetNets);
		}
		/**
		 * Adds a new question into the specified network
		 * @param net : the specified network
		 */
		public void execute(ProbabilisticNetwork net) {
			long currentTimeMillis = System.currentTimeMillis();
			this.execute(net, isToUpdateJunctionTreeAndAssetNets());
			try {
				Debug.println(getClass(), "Executed add question, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		/**
		 * Create a node in net
		 * @param net : network where the node will be added.
		 * @param isToUpdateJunctionTreeAndAssetNets : if true, the junction tree will also be updated automatically assuming that the new node will remain disconnected
		 * from the rest of the network. If true, this method will also update asset networks of all users.
		 * @see AssetAwareInferenceAlgorithm#createAssetNetFromProbabilisticNet(ProbabilisticNetwork)
		 * @see AssetAwareInferenceAlgorithm#addDisconnectedNodeIntoAssetNet(INode)
		 */
		public void execute(ProbabilisticNetwork net, boolean isToUpdateJunctionTreeAndAssetNets) {
			
			INode node = null;	// the new node 
			synchronized (getDefaultInferenceAlgorithm()) {
				node = getDefaultInferenceAlgorithm().createNodeInProbabilisticNetwork(Long.toString(this.questionId), numberStates, initProbs, isToUpdateJunctionTreeAndAssetNets, net);
			}
			if (node == null) {
				throw new RuntimeException("Failed to create question " + questionId + " in the shared Bayes net.");
			}
			
			if (isToUpdateJunctionTreeAndAssetNets) {
				// create node in all asset networks too
				synchronized (getUserToAssetAwareAlgorithmMap()) {
					for (AssetAwareInferenceAlgorithm userAlgorithm : getUserToAssetAwareAlgorithmMap().values()) {
						userAlgorithm.addDisconnectedNodeIntoAssetNet(node, net, userAlgorithm.getAssetNetwork());
					}
				}
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
		}
		public boolean isHardEvidenceAction() {return false; }
		public Date getWhenCreated() { 
			if (occurredWhen < 0) {
				return null;
			}
			return new Date(this.occurredWhen); 
		}
		public Long getTransactionKey() { return transactionKey;}
		public Long getQuestionId() { return questionId; }
		public int getNumberStates() { return numberStates; }
		public Long getUserId() { return null; }
		public String toString() { return super.toString() + "{" + this.transactionKey + ", " + this.getQuestionId() + "}"; }
		public List<Float> getOldValues() { return initProbs; }
		public void setOldValues(List<Float> oldValues) {initProbs = oldValues;}
		public List<Float> getNewValues() { return initProbs; }
		public void setNewValues(List<Float> newValues) {this.initProbs = newValues;}
		public String getTradeId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirst < 0) {
				return null;
			}
			return new Date(whenExecutedFirst) ; 
		}
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirst = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirst = whenExecutedFirst.getTime(); 
			}
		}
		/** returns {@link #getNumberStates()} */
		public Integer getSettledState() {return numberStates;}
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** From Jan 2013, adding disconnected questions does not trigger another rebuild, unless network was not compiled yet */
		public boolean isTriggerForRebuild() { 
			synchronized (getProbabilisticNetwork()) {
				if (getProbabilisticNetwork().getJunctionTree() == null || getProbabilisticNetwork().getJunctionTree().getCliques() == null
						|| getProbabilisticNetwork().getJunctionTree().getCliques().isEmpty()) {
					return true;
				}
			}
			return false; 
		}
		/**
		 * @return the isToUpdateJunctionTreeAndAssetNets
		 */
		public boolean isToUpdateJunctionTreeAndAssetNets() {
			return isToUpdateJunctionTreeAndAssetNets;
		}
		/**
		 * @param isToUpdateJunctionTreeAndAssetNets the isToUpdateJunctionTreeAndAssetNets to set
		 */
		public void setToUpdateJunctionTreeAndAssetNets(
				boolean isToUpdateJunctionTreeAndAssetNets) {
			this.isToUpdateJunctionTreeAndAssetNets = isToUpdateJunctionTreeAndAssetNets;
		}
		public long getWhenCreatedMillis() {
			return occurredWhen;
		}
		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirst;
		}
		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirst = whenExecutedFirst;
		}
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
				// see if we can find the node in history. If so, RebuildNetworkAction can add the arcs correctly
				synchronized (this.getResolvedQuestions()) {
					StatePair stateSizeAndResolution = this.getResolvedQuestions().get(childQuestionId);
					if (stateSizeAndResolution != null) {
						childNodeStateSize = stateSizeAndResolution.getStatesSize();
					}
				}
			}
			if (childNodeStateSize < 0) {	
				// sourceQuestionId was not found neither in transaction nor in history
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
							// see if we can find the node in history. If so, RebuildNetworkAction can add the arcs correctly
							synchronized (this.getResolvedQuestions()) {
								StatePair statePair = this.getResolvedQuestions().get(assumptiveQuestionId);
								if (statePair != null) {
									expectedSizeOfCPD *= statePair.getStatesSize();
									hasFound = true;
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
			this.addNetworkAction(transactionKey, new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen.getTime(), childQuestionId, parentQuestionIds, cpd));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddQuestionAssumptionNetworkAction(transactionKey, occurredWhen.getTime(), childQuestionId, parentQuestionIds, cpd));
		}
		
		return true;
	}
	
	/**
	 * Represents a network action for adding a direct dependency (edge) into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestionAssumption(long, Date, long, long, List)
	 */
	public class AddQuestionAssumptionNetworkAction extends StructureChangeNetworkAction {
		private static final long serialVersionUID = -6698600239325323921L;
		private final long transactionKey;
		private final long occurredWhen;
		private final long sourceQuestionId;
		private final List<Long> assumptiveQuestionIds;
		private List<Float> cpd;
		private long whenExecutedFirst = -1;

		/** Default constructor initializing fields */
		public AddQuestionAssumptionNetworkAction(long transactionKey,
				long occurredWhen, long sourceQuestionId,
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
		/**
		 * Add arc to the specified network
		 * @param net : the specified network
		 */
		public void execute(ProbabilisticNetwork network) {
			long currentTimeMillis = System.currentTimeMillis();
			ProbabilisticNode child;	// this is the main node (the main question we are modifying)
			
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
			try {
				Debug.println(getClass(), "Executed add assumption, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Reverting an addQuestion operation is not supported yet.");
		}
		public Date getWhenCreated() { 
			if (occurredWhen < 0) {
				return null;
			}
			return new Date(this.occurredWhen); 
		}
		public Long getTransactionKey() { return transactionKey; }
		public Long getQuestionId() { return sourceQuestionId; }
		public List<Long> getAssumptionIds() { return assumptiveQuestionIds; }
		/** Adding a new edge is a structure change */
		public Long getUserId() { return null; }
		public List<Float> getOldValues() { return cpd; }
		public void setOldValues(List<Float> cpd) { this.cpd = cpd; }
		public List<Float> getNewValues() { return cpd; }
		public void setNewValues(List<Float> newValues) {this.cpd = newValues;}
		public String getTradeId() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirst < 0) {
				return null;
			}
			return new Date(whenExecutedFirst) ; 
		}
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirst = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirst = whenExecutedFirst.getTime(); 
			}
		}
		public boolean isHardEvidenceAction() {return false; }
		public Integer getSettledState() {return null;}
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** On Jan 2013, adding arcs is the only trigger for rebuild */
		public boolean isTriggerForRebuild() { return true; }
		public long getWhenCreatedMillis() {
			return occurredWhen;
		}
		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirst;
		}
		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirst = whenExecutedFirst;
		}
	}
	
	/**
	 * Represents an action for adding cash to {@link AssetNetwork} (i.e. increase the min-Q value with a certain amount).
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addCash(long, Date, long, float, String)
	 * @see MarkovEngineImpl#isToAddCashProportionally()
	 * @see MarkovEngineImpl#setToAddCashProportionally(boolean)
	 * TODO not to initialize user until the user makes a trade.
	 */
	public class AddCashNetworkAction implements NetworkAction {
		private static final long serialVersionUID = 3388244461494885573L;
		private final long transactionKey;
		private final long occurredWhen;
		private final long userId;
		private float delta;	// how much assets were added
		private final String description;
		/** becomes true once {@link #execute()} was called */
		private boolean wasExecutedPreviously = false;
		private long whenExecutedFirst = -1;
//		private Float oldCash = Float.NaN;
		/** Default constructor initializing fields */
		public AddCashNetworkAction (long transactionKey, long occurredWhen, long userId, float assets, String description) {
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
			this.userId = userId;
			this.delta = assets;
			this.description = description;
		}
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			
			// add cash to the mapping of lazily loaded users, if user was not initialized yet
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				if (isToLazyInitializeUsers() && !getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
					// user was not initialized. Check if cash was added to user previously.
					Float assetOfLazyUser = getUninitializedUserToAssetMap().get(userId);
					if (assetOfLazyUser == null) {
						// this is the first time we add cash to uninitialized user. So, we are adding cash to the default initial value.
						assetOfLazyUser = getDefaultInitialAssetTableValue();
					} 
					// just add more cash to what the user already have
					if (isToUseQValues()) {
						float ratio = (float) Math.pow(getCurrentLogBase(), delta / getCurrentCurrencyConstant() );
						getUninitializedUserToAssetMap().put(userId, assetOfLazyUser * ratio);
					} else {
						getUninitializedUserToAssetMap().put(userId, assetOfLazyUser + delta);
					}
					try {
						Debug.println(getClass(), "Executed add cash, " + ((System.currentTimeMillis()-currentTimeMillis)));
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return;
				}
			}
			
			// extract user's asset net and related algorithm
			AssetAwareInferenceAlgorithm inferenceAlgorithm = null;
			
			try {
				inferenceAlgorithm = getAlgorithmAndAssetNetFromUserID(userId);
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not create asset tables for user " + userId, e);
			}
//			oldCash = getCash(userId, null, null);
			synchronized (inferenceAlgorithm.getAssetNetwork()) {
				inferenceAlgorithm.addAssets(delta);
			}
			
			this.wasExecutedPreviously = true;
			try {
				Debug.println(getClass(), "Executed add cash, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		public void revert() throws UnsupportedOperationException {
			if (wasExecutedPreviously) {
				// undoing a add operation is equivalent to adding the inverse value
				this.delta = -this.delta;
				this.execute();
				// this is not an ordinal execution of the action, so do not update whenExecutedFirst.
			}
		}
		public Date getWhenCreated() { 
			if (occurredWhen < 0) {
				return null;
			}
			return new Date(this.occurredWhen); 
		}
		/**this operation does not change network structure*/
		public boolean isStructureConstructionAction() { return false;	 }
		public Long getTransactionKey() { return transactionKey; }
		public float getAssets() { return delta; }
		public void setAssets(float assets) { this.delta = assets; }
		public Long getUserId() { return userId; }
		public String getDescription() { return description; }
//		public List<Float> getOldValues() { return Collections.singletonList(oldCash ); }
		// TODO return the cash before execution
		public List<Float> getOldValues() { return null; }
		public void setOldValues(List<Float> oldValues) {}
		// TODO return the cash after execution
		public List<Float> getNewValues() { return Collections.singletonList(delta); }
		/** Set delta to the first element of the list. If list is invalid, set to zero. */
		public void setNewValues(List<Float> newValues) {
			if (newValues == null || newValues.isEmpty()) {
				this.delta = 0;
			} else {
				this.delta = newValues.get(0);
			}
		}
		/** Returns the description */
		public String getTradeId() { return description; }
		public Long getQuestionId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirst < 0) {
				return null;
			}
			return new Date(whenExecutedFirst) ; 
		}
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirst = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirst = whenExecutedFirst.getTime(); 
			}
		}
		public boolean isHardEvidenceAction() {return false; }
		public Integer getSettledState() {return null;}
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** This action does not trigger another rebuild */
		public boolean isTriggerForRebuild() { return false; }
		/**
		 * @return the occurredWhen
		 */
		public long getWhenCreatedMillis() {
			return occurredWhen;
		}
		/**
		 * @return the whenExecutedFirst
		 */
		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirst;
		}
		/**
		 * @param whenExecutedFirst the whenExecutedFirst to set
		 */
		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirst = whenExecutedFirst;
		}
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
			this.addNetworkAction(transactionKey, new AddCashNetworkAction(transactionKey, occurredWhen.getTime(), userId, assets, description));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new AddCashNetworkAction(transactionKey, occurredWhen.getTime(), userId, assets, description));
		}
		
		
		return true;
	}

	/**
	 * This is the {@link NetworkAction} command
	 * representing {@link MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)}
	 * @author Shou Matsumoto
	 */
	public class AddTradeNetworkAction implements NetworkAction {
		private static final long serialVersionUID = 3286763933079436306L;
		private final long whenCreated;
		private final long transactionKey;
		private final String tradeKey;
		private TradeSpecification tradeSpecification;
		private final boolean allowNegative;
//		private Map<IRandomVariable, DoublePrecisionProbabilisticTable> qTablesBeforeTrade;
		private List<Float> oldValues = null;
		private long whenExecutedFirst = -1;	// negative means invalid millisecond
		private List<Float> newValues;
//		/** link from this original trade to all virtual trades (representation of changes in marginals caused by this original trade) */
//		private List<DummyTradeAction> affectedQuestions = new ArrayList<MarkovEngineImpl.DummyTradeAction>();
//		private final List<Integer> originalAssumedStates;
//		private final List<Long> originalAssumptionIds;
		/** Default constructor initializing fields */
		public AddTradeNetworkAction(long transactionKey, Date occurredWhen, String tradeKey, Long userId, Long questionId, 
				List<Float> oldValues, List<Float> newValues, 
				List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) {
			this.transactionKey = transactionKey;
			this.whenCreated = (occurredWhen==null)?-1:occurredWhen.getTime();
			this.tradeKey = tradeKey;
			this.newValues = newValues;
			this.allowNegative = allowNegative;
			if (assumptionIds != null) {
				// fill trade specification with an instance that we are sure that is mutable, because executeTrade may change its content
				assumptionIds = new ArrayList<Long>(assumptionIds);
			}
			if (assumedStates != null) {
				// fill trade specification with an instance that we are sure that is mutable, because executeTrade may change its content
				assumedStates = new ArrayList<Integer>(assumedStates);
			}
//			if (newValues == null || newValues.isEmpty()) {
//				throw new IllegalArgumentException("Probability must be set to some value.");
//			}
			// fill trade specification
			this.tradeSpecification = new TradeSpecificationImpl(userId, questionId, oldValues, newValues, assumptionIds, assumedStates);
			
		}
		/** Calls {@link #execute(true)}
		 * @see #execute(boolean) */
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			this.execute(true);
			try {
				Debug.println(getClass(), "Executed trade, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		/** Calls {@link #execute(boolean, true)}
		 * @see #execute(boolean, boolean) */
		public void execute(boolean isToUpdateAssets) {
			this.execute(isToUpdateAssets, !isToThrowExceptionOnInvalidAssumptions());
		}
		public void execute(boolean isToUpdateAssets, boolean isToUpdateAssumptionIds) {
			// extract user's asset network from user ID
			AssetAwareInferenceAlgorithm algorithm = null;
			try {
				algorithm = getAlgorithmAndAssetNetFromUserID(tradeSpecification.getUserId());
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not extract delta from user " + tradeSpecification.getUserId() + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			if (algorithm == null) {
				throw new RuntimeException("Could not extract delta from user " + tradeSpecification.getUserId() + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			}
			
			synchronized (getProbabilisticNetwork()) {
				Node node = null;
				if (getWhenExecutedFirstTimeMillis() > 0) {
					// for optimization, only update history if this is first execution.
					// first, check that node exists
					node = getProbabilisticNetwork().getNode(Long.toString(tradeSpecification.getQuestionId()));
					if (node == null) {
						throw new InexistingQuestionException("Question " + tradeSpecification.getQuestionId() + " not found.", tradeSpecification.getQuestionId());
					}
				}
				synchronized (algorithm.getAssetNetwork()) {
					if (!algorithm.getNetwork().equals(getProbabilisticNetwork())) {
						throw new IllegalStateException("Asset net of user " + algorithm.getAssetNetwork() 
								+ " is not synchronized with the Bayes Net. This could be caused by incompatible versions of markov engine and libraries.");
						// this should never happen, but some desync may happen.
//						Debug.println(getClass(), "[Warning] desync of network detected.");
//						algorithm.setNetwork(getProbabilisticNetwork());
					}
					
					// check if we should do clique-sensitive operation, like balance trade
					if (tradeSpecification instanceof CliqueSensitiveTradeSpecification) {
						CliqueSensitiveTradeSpecification cliqueSensitiveTradeSpecification = (CliqueSensitiveTradeSpecification) tradeSpecification;
						// extract the clique from tradeSpecification. Note: this is supposedly an asset clique
						if (cliqueSensitiveTradeSpecification.getClique() != null) {
							// fortunately, the algorithm doesn't care if it is an asset or prob clique, so provide the asset clique to algorithm
							algorithm.getEditCliques().add(cliqueSensitiveTradeSpecification.getClique());
							// by forcing the algorithm to update only this clique, we are forcing the balance trade to balance the provided clique
						}
					}
					
					// backup config of assets
					boolean backup = algorithm.isToUpdateAssets();
					algorithm.setToUpdateAssets(isToUpdateAssets);
					
					// do trade. The var "algorithm" has a reference to the network to be changed
					List<Float> oldConditionalProb = executeTrade(	// this method returns what was the conditional prob before trade
							tradeSpecification.getQuestionId(), 
							tradeSpecification.getOldProbabilities(),
							tradeSpecification.getProbabilities(), 
							tradeSpecification.getAssumptionIds(), 
							tradeSpecification.getAssumedStates(), 
							allowNegative, algorithm, 
							isToUpdateAssumptionIds, // if this boolean is true, then it will overwrite assumptionIds and assumedStates on out-of-clique edits
							false, this
						);
					
					// store what was the conditional probability before the trade, if it was not specified in the tradeSpecification
					if (tradeSpecification.getOldProbabilities() == null || tradeSpecification.getOldProbabilities().isEmpty()) {
						tradeSpecification.setOldProbabilities(oldConditionalProb);
					}
					
					algorithm.setToUpdateAssets(backup);	// revert config of assets
					// backup the previous delta so that we can revert this trade
//					qTablesBeforeTrade = algorithm.getAssetTablesBeforeLastPropagation();
					// add this question to the mapping of questions traded by the user
					Set<Long> questions = getTradedQuestionsMap().get(tradeSpecification.getUserId());
					if (questions == null) {
						questions = new HashSet<Long>();
						getTradedQuestionsMap().put(tradeSpecification.getUserId(), questions);
					}
					questions.add(tradeSpecification.getQuestionId());
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
			throw new UnsupportedOperationException("This version of ME cannot revert trades due to memory issues");
		}
		public Date getWhenCreated() { 
			if (whenCreated < 0) {
				return null;
			}
			return new Date(this.whenCreated); 
		}
		public boolean isStructureConstructionAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return tradeSpecification.getUserId(); }
		public Long getQuestionId() { return tradeSpecification.getQuestionId(); }
		public List<Long> getAssumptionIds() { return tradeSpecification.getAssumptionIds(); }
		public List<Integer> getAssumedStates() { return tradeSpecification.getAssumedStates(); }
		public boolean isAllowNegative() { return allowNegative; }
		public List<Float> getOldValues() { return oldValues ; }
		public void setOldValues(List<Float> oldValues) { this.oldValues = oldValues;};
		public String getTradeId() { return tradeKey; }
//		/** Mapping from {@link Clique}/{@link Separator} to q-tables before {@link #execute()}*/
//		public Map<IRandomVariable, DoublePrecisionProbabilisticTable> getqTablesBeforeTrade() { return qTablesBeforeTrade; }
		public List<Float> getNewValues() { return newValues; }
		public void setNewValues(List<Float> newValues) { this.newValues = newValues; /*this.tradeSpecification.setProbabilities(newValues);*/ }
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirst < 0) {
				return null;
			}
			return new Date(whenExecutedFirst) ; 
		}
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirst = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirst = whenExecutedFirst.getTime(); 
			}
		}
		/** 
		 * Trades containing the marginals of affected questions before and after this trade 
		 * In another word, this is a link from this original trade to all virtual trades 
		 * (representation of changes in marginals caused by this original trade)
		 */
//		public List<DummyTradeAction> getAffectedQuestions() { return affectedQuestions; }
		public boolean isHardEvidenceAction() {return false; }
		public Integer getSettledState() {return null;}
		/**
		 * @param tradeSpecification the tradeSpecification to set
		 */
		public void setTradeSpecification(TradeSpecification tradeSpecification) {
			this.tradeSpecification = tradeSpecification;
		}
		/**
		 * @return the tradeSpecification
		 */
		public TradeSpecification getTradeSpecification() {
			return tradeSpecification;
		}
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** This action does not trigger another rebuild */
		public boolean isTriggerForRebuild() { return false; }
		/**
		 * @return the whenCreated
		 */
		public long getWhenCreatedMillis() {
			return whenCreated;
		}
		/**
		 * @return the whenExecutedFirst
		 */
		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirst;
		}
		/**
		 * @param whenExecutedFirst the whenExecutedFirst to set
		 */
		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirst = whenExecutedFirst;
		}
	}
	
	/**
	 * This represents a trade caused by a {@link ImportNetworkAction}.
	 * These "virtual" trades represents changes in the probability caused
	 * when importing a network in which the CPT is not uniform 
	 * (in such case, the probabilities shall be adjusted in order to reflect the
	 * imported non-uniform cpt)
	 * @author Shou Matsumoto
	 */
	public class VirtualTradeAction extends AddTradeNetworkAction {
		private static final long serialVersionUID = 8661055377387997997L;
		private final NetworkAction parentAction;

		public VirtualTradeAction(NetworkAction parentAction, Long questionId, List<Float> newValues, 
				List<Long> assumptionIds, List<Integer> assumedStates) {
			// initialize fields using values of parentAction mostly.
			super(parentAction.getTransactionKey(), parentAction.getWhenCreated(), parentAction.getTradeId(), parentAction.getUserId(), 
					questionId, null, newValues, assumptionIds, assumedStates, true);
			this.setWhenExecutedFirstTime(parentAction.getWhenExecutedFirstTime());
			this.parentAction = parentAction;
		}

		/* (non-Javadoc)
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.AddTradeNetworkAction#execute(boolean)
		 */
		public void execute(boolean isToUpdateAssets) {
			setOldValues(getProbList(getTradeSpecification().getQuestionId(), null, null));
			getTradeSpecification().setOldProbabilities(
					executeTrade(
							getQuestionId(), 
							getTradeSpecification().getOldProbabilities(),
							getTradeSpecification().getProbabilities(), 
							getAssumptionIds() , getAssumedStates() , true, 
							getDefaultInferenceAlgorithm(), !isToThrowExceptionOnInvalidAssumptions(), 
							false, getParentAction()
						)
				);
			setNewValues(getProbList(getTradeSpecification().getQuestionId(), null, null));
		}
		public NetworkAction getParentAction() { return parentAction; }
		/** Return true if {@link #getParentAction()} is a corrective trade */
		public Boolean isCorrectiveTrade() {return parentAction.isCorrectiveTrade();}
		/** Just delegate to parent */
		public NetworkAction getCorrectedTrade() {return parentAction.getCorrectedTrade(); }

		/** 
		 * Just delegates to {@link #getParentAction()}
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.AddTradeNetworkAction#getUserId()
		 */
		public Long getUserId() { return parentAction.getUserId(); }
		
	}
	
	/**
	 * Objects of this class represent marginals of questions which were changed due to another trade of
	 * (directly or indirectly) dependent questions.<br/>
	 * {@link #getOldValues()} will contain the old marginal (before the trade).<br/>
	 * {@link #getNewValues()} will contain the new marginal (after the trade).<br/>
	 * {@link #getQuestionId()} will contain the affected question.<br/>
	 * {@link #getParentAction()} will contain the original trade 
	 * (trade which made the marginals of {@link #getQuestionId()} to change).<br/>
	 * @author Shou Matsumoto
	 */
	public class DummyTradeAction extends VirtualTradeAction {
		private static final long serialVersionUID = 2421096723861974085L;
		private final Integer settledState;
		/**
		 * Default constructor initializing fields.
		 * @param parentAction : original trade (trade which made the marginals of questionId to change
		 * @param questionId : the affected question
		 * @param oldMarginal : the old marginal (before the trade)
		 * @param newMarginal :  the new marginal (after the trade)
		 */
		public DummyTradeAction(NetworkAction parentAction, long questionId, List<Float> oldMarginal, List<Float> newMarginal) {
			// initialize fields using values of parentAction mostly.
			super(parentAction, questionId, newMarginal, parentAction.getAssumptionIds(), parentAction.getAssumedStates());
			this.settledState = null;
			this.setWhenExecutedFirstTime(parentAction.getWhenExecutedFirstTime());
			this.setOldValues(oldMarginal);
		}
		/**
		 * This is equivalent to {@link DummyTradeAction#DummyTradeAction(NetworkAction, long, List, List, Integer)},
		 * but the question id will be set to {@link NetworkAction#getQuestionId()} of the parent action
		 * @param parentAction : original resolution. {@link NetworkAction#getQuestionId()} of this parameter
		 * will be used for {@link #getQuestionId()}.
		 * @param settledState : settled state of a resolved question
		 */
		public DummyTradeAction(ResolveQuestionNetworkAction parentAction, List<Float> oldMarginal, List<Float> newMarginal, Integer settledState) {
			// initialize fields using values of parentAction mostly.
			super(parentAction, parentAction.getQuestionId(), newMarginal, parentAction.getAssumptionIds(), parentAction.getAssumedStates());
			this.settledState = settledState;
			this.setWhenExecutedFirstTimeMillis(parentAction.getWhenExecutedFirstTimeMillis());
			this.setOldValues(oldMarginal);
		}
		public Date getWhenExecutedFirstTime() {
			if (getParentAction() == null) {
				return null;
			}
			return getParentAction().getWhenExecutedFirstTime();
		}
		public Integer getSettledState() { return settledState; }
		
	}
	
	/**
	 * Objects of this class represents trades automatically executed for correction.
	 * {@link MarkovEngineInterface#addTrade(Long, Date, String, long, long, List, List, List, List, boolean)}
	 * and {@link MarkovEngineInterface#addTrade(Long, Date, String, TradeSpecification, boolean)}
	 * can specify the old probabilities (in the latter case, at {@link TradeSpecification#setOldProbabilities(List)}).
	 * If the old probabilities are specified, then {@link MarkovEngineImpl} is expected to set the probability to
	 * that value before letting the user do the actual trade. The change of probability before the actual trade
	 * is the corrective trade, which is represented by this class.
	 * @author Shou Matsumoto
	 *
	 */
	public class CorrectiveTradeAction extends DummyTradeAction {
		private static final long serialVersionUID = 4348158555507387962L;
		/**
		 * Default constructor initializing fields.
		 * @param correctedTrade : original trade (the trade being corrected)
		 * @param questionId : the affected question
		 * @param oldCondProb : the old probability (before the trade)
		 * @param newCondProb :  the new probability (after the trade)
		 */
		public CorrectiveTradeAction(NetworkAction correctedTrade, long questionId, List<Float> oldCondProb, List<Float> newCondProb) {
			// initialize fields using values of parentAction mostly.
			super(correctedTrade, questionId, null, null);
			this.getTradeSpecification().setOldProbabilities(oldCondProb);
			this.getTradeSpecification().setProbabilities(newCondProb);
		}
		/** This is the only type of trade which is used specially to correct the probabilities when the actual
		 * probability is different from what the caller has provided. */
		public Boolean isCorrectiveTrade() {return Boolean.TRUE;}
		/** The parent action is the corrected trade */
		public NetworkAction getCorrectedTrade() {return getParentAction(); }
	}
	
	/**
	 * Compare the marginal probabilities in marginalsBefore with the current marginal probabilities,
	 * and calls {@link MarkovEngineImpl#addNetworkActionIntoQuestionMap(NetworkAction, Long)}
	 * in order to insert a new virtual trade into the history.
	 * Basically, this method inserts new entries into the history of trades regarding
	 * the changes in marginal probabilities caused by indirect trades.
	 * @param parentAction : the {@link AddTradeNetworkAction} which caused the marginals of
	 * other nodes to change. {@link AddTradeNetworkAction#execute()} is expected to be executed before this method.
	 * If {@link NetworkAction#isCorrectiveTrade()} == true, then this trade will be automatically considered as indirect trade.
	 * @param marginalsBefore : marginal probabilities before the execution of parentAction.
	 * @return the list of virtual trades created and inserted to {@link MarkovEngineImpl#getNetworkActionsIndexedByQuestions()}
	 * @see MarkovEngineImpl#addNetworkActionIntoQuestionMap(NetworkAction, Long)
	 */
	protected List<VirtualTradeAction> addVirtualTradeIntoMarginalHistory( NetworkAction parentAction, Map<Long, List<Float>> marginalsBefore) {
//		Debug.println(getClass(), "\n\n!!!Entered addVirtualTradeIntoMarginalHistory\n\n");
		List<VirtualTradeAction> ret = new ArrayList<VirtualTradeAction>();
		// get the marginals after trade
		Map<Long, List<Float>> marginalsAfter = getProbLists(null, null, null);
		// for each question, compare the marginals and if changed, add to affectedQuestions
		for (Long question : marginalsBefore.keySet()) {
			if ( !(parentAction.isCorrectiveTrade())	// corrective trades are never considered as direct trade
					&&  question.equals(parentAction.getQuestionId())) {
				// We are only interested on indirect trade here. Do not double-count the direct trade.
				continue;	
			}
			List<Float> oldMarginal = marginalsBefore.get(question);
			List<Float> newMarginal = marginalsAfter.get(question);
			if (newMarginal != null) {
				boolean hasChanged = false;
				// compare marginals
				for (int i = 0; i < newMarginal.size(); i++) {
					if (Math.abs(newMarginal.get(i) - oldMarginal.get(i)) > getProbabilityErrorMargin()) {
						hasChanged = true;
						break;
					}
				}
				if (hasChanged) {
					// generate a virtual trade representing a change of marginal probability of another node caused by this trade.
					DummyTradeAction virtualTrade = null;
//					if (parentAction instanceof DummyTradeAction) {
//						virtualTrade = (DummyTradeAction)parentAction;
//					} else {
						virtualTrade = new DummyTradeAction(parentAction, question, oldMarginal, newMarginal);
//					}
					// indicate that this trade is related to question (although indirectly).
					addNetworkActionIntoQuestionMap(virtualTrade, question);
					
					// add virtual trade to the inverse mapping (from virtual trade, find related questions)
					synchronized (getNetworkActionsIndexedByQuestions()) {
						Set<Long> relatedQuestions = getVirtualTradeToAffectedQuestionsMap().get(virtualTrade);
						if (relatedQuestions == null) {
							relatedQuestions = new HashSet<Long>();
							getVirtualTradeToAffectedQuestionsMap().put(virtualTrade, relatedQuestions);
						}
						relatedQuestions.add(question);
					}
					
					ret.add(virtualTrade);
				}
			}
		}
		return ret;
	}
	
	/**
	 * This is the same of {@link #addVirtualTradeIntoMarginalHistory(NetworkAction, Map)}, but specialized for {@link ResolveQuestionNetworkAction}
	 */
	protected List<DummyTradeAction> addVirtualTradeIntoMarginalHistory( ResolveQuestionNetworkAction parentAction, Map<Long, List<Float>> marginalsBefore) {
//		Debug.println(getClass(), "\n\n!!!Entered addVirtualTradeIntoMarginalHistory for ResolveQuestionNetworkAction\n\n");
		List<DummyTradeAction> ret = new ArrayList<MarkovEngineImpl.DummyTradeAction>();
		// get the marginals after trade
		Map<Long, List<Float>> marginalsAfter = getProbLists(null, null, null);
		// for each question, compare the marginals and if changed, add to affectedQuestions
		for (Long question : marginalsBefore.keySet()) {
			if (question.equals(parentAction.getQuestionId())) {
				// We are only interested on indirect trade here. Do not double-count the direct trade.
				continue;	
			}
			List<Float> oldMarginal = marginalsBefore.get(question);
			List<Float> newMarginal = marginalsAfter.get(question);
			if (newMarginal != null) {
				boolean hasChanged = false;
				// compare marginals
				for (int i = 0; i < newMarginal.size(); i++) {
					if (Math.abs(newMarginal.get(i) - oldMarginal.get(i)) > getProbabilityErrorMargin()) {
						hasChanged = true;
						break;
					}
				}
				if (hasChanged) {
					// generate a dummy trade representing a change of marginal probability of another node caused by this trade.
					DummyTradeAction dummyTrade = new DummyTradeAction(parentAction, oldMarginal, newMarginal, parentAction.getSettledState());
					// indicate that this trade is related to question (although indirectly).
					addNetworkActionIntoQuestionMap(dummyTrade, question);
					
					// add virtual trade to the inverse mapping (from virtual trade, find related questions)
					synchronized (getNetworkActionsIndexedByQuestions()) {
						Set<Long> relatedQuestions = getVirtualTradeToAffectedQuestionsMap().get(dummyTrade);
						if (relatedQuestions == null) {
							relatedQuestions = new HashSet<Long>();
							getVirtualTradeToAffectedQuestionsMap().put(dummyTrade, relatedQuestions);
						}
						relatedQuestions.add(question);
					}
					
					ret.add(dummyTrade);
				}
			}
		}
		return ret;
	}
	
	/**
	 * This method adds a new element in {@link #getLastNCliquePotentialMap()} if
	 * the current clique potentials of {@link #getProbabilisticNetwork()} has changed
	 * compared to previousCliquePotentials.
	 * @param parentAction : this object will be used to instantiate {@link ParentActionPotentialTablePair}, which
	 * is the time of the values of {@link #getLastNCliquePotentialMap()}.
	 * @param previousCliquePotentials : contains the clique potentials (tables) before an edit. These tables will be
	 * be compared to the current clique tables.
	 */
	protected void addToLastNCliquePotentialMap(NetworkAction parentAction, Map<Clique, PotentialTable> previousCliquePotentials) {
//		Debug.println(getClass(), "\n\n!!!Entered addToLastNCliquePotentialMap\n\n");
		// iterate on all cliques and compare differences
		for (Clique clique : previousCliquePotentials.keySet()) {
			
			// extract the clique potentials after the trade (to be stored in potential map - the history)
			PotentialTable currentTable = clique.getProbabilityFunction();
			
			// true if the clique potentials have changed
			boolean isChanged = false;
			
			// check if clique potentials have changed
			if (isToStoreOnlyCliqueChangeHistory()) { // if isToStoreOnlyCliqueChangeHistory() == true, then we have to compare clique potentials before and after edit
				// extract the clique potentials before the trade
				PotentialTable lastTable = previousCliquePotentials.get(clique);
				if (lastTable == null) {
					throw new IllegalStateException("Clique " + clique + " was not found in " + previousCliquePotentials
							+ ", which is a snapshot of cliques before a trade on question " + parentAction.getQuestionId()
							+ ". This is an unexpected state of the system, which may have been caused by a clique modified during a trade.");
				}
				
				
				if (currentTable.tableSize() != lastTable.tableSize()) {
//				throw new IllegalStateException("The size of clique table of " + clique + " has changed from "
//						+ lastTable.tableSize() + " to " + currentTable.tableSize()
//						+ " during a trade. This is an unexpected behavior of the system.");
					isChanged = true; // this can happen if we resolve a question
				} else {
					// check if content has changed, regarding an error margin
					for (int i = 0; i < currentTable.tableSize(); i++) {
						if (Math.abs(currentTable.getValue(i) - lastTable.getValue(i)) > getProbabilityErrorMargin()) {
							isChanged = true;
							break;
						}
					}
				}
			}
			
			// store only the cliques which were changed. If isToStoreOnlyCliqueChangeHistory == false, then store anyway
			if (isChanged || !isToStoreOnlyCliqueChangeHistory()) {
				// store in the history
				synchronized (getLastNCliquePotentialMap()) {
					List<ParentActionPotentialTablePair> history = getLastNCliquePotentialMap().get(clique);
					if (history == null) {
						// this is the first time this clique has changed. Initialize.
						history = new ArrayList<MarkovEngineImpl.ParentActionPotentialTablePair>();
						getLastNCliquePotentialMap().put(clique, history);
					}
					// delete entries if the size will become greater than getMaxConditionalProbHistorySize().
					// Note: getMaxConditionalProbHistorySize() > 0 at this point, because of "if (memento != null && getMaxConditionalProbHistorySize() > 0)"
					while (history.size() + 1 > getMaxConditionalProbHistorySize()) {
						ParentActionPotentialTablePair removed = history.remove(0);	// remove the initial entries (old records)
						removed.setParentAction(null);
					}
					// add entry in the history
					history.add(new ParentActionPotentialTablePair(parentAction, (PotentialTable) currentTable.clone()));
				}
			}
		} // end of iteration over cliques
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) throws IllegalArgumentException {
		return this.addTrade(
				transactionKey, occurredWhen, tradeKey, 
				new TradeSpecificationImpl(userId, questionId, null, newValues, assumptionIds, assumedStates), 
				allowNegative
		);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(java.lang.Long, java.util.Date, java.lang.String, long, long, java.util.List, java.util.List, java.util.List, java.util.List, boolean)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, 
			List<Float> oldValues, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) 
			throws IllegalArgumentException{
		return this.addTrade(
				transactionKey, occurredWhen, tradeKey, 
				new TradeSpecificationImpl(userId, questionId, oldValues, newValues, assumptionIds, assumedStates), 
				allowNegative
			);
	}
	
	
	/**
	 * Just delegates to {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)}.
	 * This delegation is not in the opposite direction, because {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)}
	 * is currently being used more frequently than this method, so we want to make the call stack smaller.
	 * In future implementations, {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)} will be 
	 * delegating to this method instead.
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(java.lang.Long, java.util.Date, java.lang.String, edu.gmu.ace.daggre.TradeSpecification, boolean)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, TradeSpecification tradeSpecification, boolean allowNegative) throws IllegalArgumentException {
		
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (transactionKey != null && this.getNetworkActionsMap().get(transactionKey) == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		if (tradeSpecification == null) {
			throw new IllegalArgumentException("Argument \"tradeSpecification\" is mandatory.");
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
		
		// extract some values from tradeSpecification which are frequently used
		Long userId = tradeSpecification.getUserId();
		Long questionId = tradeSpecification.getQuestionId();
		List<Float> newValues = tradeSpecification.getProbabilities();
		List<Long> assumptionIds = tradeSpecification.getAssumptionIds();
		List<Integer> assumedStates = tradeSpecification.getAssumedStates();
		
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
//			List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey); // getNetworkActionsMap() is supposedly a concurrent map
//			synchronized (actions) {	// actions is not a concurrent list, so must lock it
//				for (NetworkAction action : actions) {
//					if ((action instanceof AddQuestionNetworkAction) && (e.getQuestionId() != null) && (e.getQuestionId().equals(action.getQuestionId()))) {
//						// this action will create the question which was not found.
//						isNodeToBeCreatedWithinTransaction = true;
//						break;
//					}
//				}
//			}
			Map<Long, Set<Long>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
			
			
			if (mapTransactionQuestions == null || transactionKey == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				throw e;
			}
			
			// use the mapping in order to check whether the parent will be created in the same transaction
			synchronized (mapTransactionQuestions) {
				Set<Long> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
				if (questionsInSameTransaction == null) {
					throw e;
				}
				// search for the assumption ID in questionsToBeCreatedInTransaction
				for (Long idInMapping : questionsInSameTransaction) {
					if (idInMapping.equals(e.getQuestionId())) {
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
			if (transactionKey != null) {
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
			} else {
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
			AddTradeNetworkAction newAction = new AddTradeNetworkAction(
					transactionKey, occurredWhen, tradeKey, userId, questionId, 
					tradeSpecification.getOldProbabilities(), newValues, 
					assumptionIds, assumedStates, allowNegative
				);
			this.addNetworkAction(transactionKey, newAction);
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for adding trade
			AddTradeNetworkAction newAction = new AddTradeNetworkAction(
					transactionKey, occurredWhen, tradeKey, userId, questionId, 
					tradeSpecification.getOldProbabilities(), newValues, 
					assumptionIds, assumedStates, allowNegative
				);
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
		private static final long serialVersionUID = -8173040985188570655L;
		private final long transactionKey;
		private final long occurredWhen;
		private final long questionId;
		private final int settledState;
		private List<Float> marginalWhenResolved;
		private long whenExecutedFirst = -1;
		/** Default constructor initializing fields */
		public ResolveQuestionNetworkAction (long transactionKey, Date occurredWhen, long questionId, int settledState) {
			this.transactionKey = transactionKey;
			this.occurredWhen = (occurredWhen == null)?-1:occurredWhen.getTime();
			this.questionId = questionId;
			this.settledState = settledState;
			
		}
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			TreeVariable probNode = null;
			synchronized (getProbabilisticNetwork()) {
				probNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(questionId));
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
					synchronized (getDefaultInferenceAlgorithm()) {
						Node node = getProbabilisticNetwork().getNode(Long.toString(questionId));
						if (node != null) {
							// backup marginals, so that we can make comparison and add history of indirect changes in marginals
							Map<Long, List<Float>> marginalsBeforeResolution = getProbLists(null, null, null);
							// backup clique potentials
							Map<Clique, PotentialTable> previousCliquePotentials = getCurrentCliquePotentials();
							
							// actually add hard evidences and propagate (only on BN, because getDefaultInferenceAlgorithm().isToUpdateAssets() == false)
							getDefaultInferenceAlgorithm().setAsPermanentEvidence(node, settledState, isToDeleteResolvedNode());
							
							// store marginals in history
							addVirtualTradeIntoMarginalHistory(this, marginalsBeforeResolution);	
							// store cliques in history of conditional probability (w/ limited size)
							addToLastNCliquePotentialMap(this, previousCliquePotentials);	
						} else {
							throw new InexistingQuestionException("Node " + questionId + " is not present in network.", questionId);
						}
					}
					
					// do not allow getUserToAssetAwareAlgorithmMap() to be changed here. I.e. do not allow new users to be added now
					for (Entry<Long, AssetAwareInferenceAlgorithm> userIdAndAlgorithm : getUserToAssetAwareAlgorithmMap().entrySet()) {
						IAssetNetAlgorithm assetAlgorithm = userIdAndAlgorithm.getValue().getAssetPropagationDelegator();
						synchronized (assetAlgorithm.getAssetNetwork()) {
							INode assetNode = assetAlgorithm.getAssetNetwork().getNode(Long.toString(questionId));
							if (assetNode != null) {
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
											questionToCashBefore.put(questionId, cashBefore);
											getUserIdToResolvedQuestionCashBeforeMap().put(userIdAndAlgorithm.getKey(), questionToCashBefore);
											// resolve the question in the asset structure of this user
											assetAlgorithm.setAsPermanentEvidence(Collections.singletonMap(assetNode, settledState),isToDeleteResolvedNode());
											// obtain the difference between cash before and after.
											float gain = getCash(userIdAndAlgorithm.getKey(), null, null) - cashBefore;
											// update the mapping if the difference was "large" enough
											if (Math.abs(gain) > getProbabilityErrorMargin()) {
												// put the mapping from question ID (of settled question) to gain from that question
												questionToGain.put(questionId, gain);
												getUserIdToResolvedQuestionCashGainMap().put(userIdAndAlgorithm.getKey(), questionToGain);
											}
										}
									}
								} else {
									// just update assets in this case
									assetAlgorithm.setAsPermanentEvidence(Collections.singletonMap(assetNode, settledState),isToDeleteResolvedNode());
								}
							} else {
								try {
									throw new InexistingQuestionException("Node " + questionId + " is not present in asset net of user " + assetAlgorithm.getAssetNetwork(), questionId);
								} catch (Throwable t) {
									t.printStackTrace();
								}
							}
						}
					}
				}
			}
			synchronized (getResolvedQuestions()) {
				getResolvedQuestions().put(getQuestionId(), new StatePair(probNode.getStatesSize(), settledState));
			}
			try {
				Debug.println(getClass(), "Executed resolve, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		public void revert() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Current version cannot revert a resolution of a question.");
		}
		public Date getWhenCreated() { 
			if (occurredWhen < 0) {
				return null;
			}
			return new Date(this.occurredWhen); 
		}
		/** Although this method changes the structure, we do not want to call {@link RebuildNetworkAction} after this action, so return false */
		public boolean isStructureConstructionAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		/** this is not an operation performed by a particular user */ 
		public Long getUserId() { return null;}
		public Long getQuestionId() { return questionId; }
		public Integer getSettledState() { return settledState; }
		public List<Float> getOldValues() { return marginalWhenResolved; }
		public void setOldValues(List<Float> oldValues) { marginalWhenResolved = oldValues;}
		/** Builds the list of new values (1 for the resolved state and 0 otherwise) under request */
		public List<Float> getNewValues() {
			//need to use this, because we cannot retrieve what was the number of states of this question locally...
			StatePair statePair = getResolvedQuestions().get(questionId);
			if (statePair == null) {
				return null;
			}
			List<Float> ret = new ArrayList<Float>(statePair.getStatesSize());
			for (int i = 0; i < statePair.getStatesSize(); i++) {
				// add 1 if it is the resolved state. 0 otherwise.
				ret.add((i == statePair.getResolvedState().intValue())?1f:0f);
			}
			return ret; 
		}
		public void setNewValues(List<Float> newValues) { 
			throw new UnsupportedOperationException("The probability of settled questions is always 100% to the settle state, and cannot be changed.");
		}
		public void setMarginalWhenResolved(List<Float> newValue) { marginalWhenResolved = newValue; }
		public String getTradeId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirst < 0) {
				return null;
			}
			return new Date(whenExecutedFirst) ; 
		}
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirst = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirst = whenExecutedFirst.getTime(); 
			}
		}
		public boolean isHardEvidenceAction() {return true; }
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** This action does not trigger another rebuild */
		public boolean isTriggerForRebuild() { return false; }
		public long getWhenCreatedMillis() {
			return occurredWhen;
		}
		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirst;
		}
		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirst = whenExecutedFirst;
		}
	}
	
	/**
	 * This method is used in {@link ResolveQuestionNetworkAction#execute()} in order to backup clique potentials,
	 * so that they can be compared posteriorly in order to fill {@link MarkovEngineImpl#getLastNCliquePotentialMap()}
	 * @return : a map from cliques to its current tables
	 */
	protected Map<Clique, PotentialTable> getCurrentCliquePotentials() {
		Map<Clique, PotentialTable> ret = new HashMap<Clique, PotentialTable>();
		
		if (getProbabilisticNetwork() != null && getProbabilisticNetwork().getJunctionTree() != null){
			List<Clique> cliques = getProbabilisticNetwork().getJunctionTree().getCliques();
			if (cliques != null) {
				for (Clique clique : cliques) {
					ret.put(clique, (PotentialTable) clique.getProbabilityFunction().clone());
				}
			}
		}
		
		return ret;
	}
	/**
	 * Integrates a set of ResolveQuestionNetworkAction
	 * into a single bayesian network propagation.
	 * This is basically for optimization 
	 * (junction tree algorithm can propagate all hard evidences in single propagation instead of 1 propagation per each evidence)
	 * @author Shou Matsumoto
	 */
	public class ResolveSetOfQuestionsNetworkAction extends ResolveQuestionNetworkAction {
		private static final long serialVersionUID = -5871486893244279962L;
		/** The set of ResolveQuestionNetworkAction which this action integrates */
		private final Collection<ResolveQuestionNetworkAction> resolutions;
		
		/** Default constructor using fields */
		public ResolveSetOfQuestionsNetworkAction(long transactionKey, Date occurredWhen, Collection<ResolveQuestionNetworkAction> resolutions) {
			super(transactionKey, occurredWhen, Long.MIN_VALUE, Integer.MIN_VALUE);
			if (resolutions == null || resolutions.isEmpty()) {
				throw new IllegalArgumentException("Invalid resolution: "+ resolutions);
			}
			this.resolutions = resolutions;
		}

		/** @see edu.gmu.ace.daggre.MarkovEngineImpl.ResolveQuestionNetworkAction#execute() */
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			// mapping to be used to update history of resolved nodes at the end of this action
			Map<Long, StatePair> mapForHistory = new HashMap<Long, MarkovEngineImpl.StatePair>();
			
			synchronized (getProbabilisticNetwork()) {
				
				// map of evidences to be passed to the algorithm in order to resolve all questions in 1 step
				// A tree map with name comparator will allow me to use the same map in BN and asset net 
				// (i.e. keys - nodes - with same name will be considered as same key, although they are different instances)
				Map<INode, Integer> mapOfEvidences = new TreeMap<INode, Integer>(new Comparator<INode>() {
					public int compare(INode o1, INode o2) {
//						if (o1 instanceof IRandomVariable && o2 instanceof IRandomVariable) {
//							return ((IRandomVariable)o1).getInternalIdentificator() - ((IRandomVariable)o2).getInternalIdentificator();
//						}
//						return o1.getName().compareTo(o2.getName());
						return ((IRandomVariable)o1).getInternalIdentificator() - ((IRandomVariable)o2).getInternalIdentificator();
					}
				});
				
				// check consistency and fill the map of evidences and the map for the history
				for (ResolveQuestionNetworkAction action : resolutions) {
					TreeVariable probNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(action.getQuestionId()));
					if (probNode != null) {	
						// if probNode.hasEvidence(), then it was resolved already
						if (probNode.hasEvidence() && probNode.getEvidence() != action.getSettledState()) {
							throw new RuntimeException("Attempted to resolve question " + action.getQuestionId() 
									+ " to state " + action.getSettledState() 
									+ ", but it was already resolved to " + probNode.getEvidence());
						} else {
							// do nothing if node is already settled at settledState
						}
						if (probNode.getEvidence() != action.getSettledState()) {	// note: probNode.getEvidence() < 0 if there is no evidence
							// store the marginal of this node before the resolution
							ArrayList<Float> marginalBeforeResolution = new ArrayList<Float>(probNode.getStatesSize());
							for (int i = 0; i < probNode.getStatesSize(); i++) {
								marginalBeforeResolution.add(probNode.getMarginalAt(i));
							}
							action.setMarginalWhenResolved(marginalBeforeResolution);
						}
						// in the map which will be passed to the algorithm, mark that this node has an evidence at this state
						mapOfEvidences.put(probNode, action.getSettledState());
						// update the map to be used to update the history all at once at the end of this method
						mapForHistory.put(action.getQuestionId(), new StatePair(probNode.getStatesSize(), action.getSettledState()));
					} else {
						throw new InexistingQuestionException("Question " + action.getQuestionId() + " does not exist.", action.getQuestionId());
					}
				}
				
				// propagate findings on probabilistic network by using the default algorithm
				synchronized (getDefaultInferenceAlgorithm()) {
					// CAUTION: this portion assumes that getDefaultInferenceAlgorithm().isToUpdateAssets() == false
					getDefaultInferenceAlgorithm().setAsPermanentEvidence(mapOfEvidences, isToDeleteResolvedNode());
				}
				
				// TODO revert on some error
				
				// Update only the asset nets of the users by using the asset nets + algorithms allocated for each user.
				// CAUTION: Do not release lock to global BN until we change all asset nets
				synchronized (getUserToAssetAwareAlgorithmMap()) {
					// do not allow getUserToAssetAwareAlgorithmMap() to be changed here. I.e. do not allow new users to be added now
					for (Entry<Long, AssetAwareInferenceAlgorithm> userIdAndAlgorithm : getUserToAssetAwareAlgorithmMap().entrySet()) {
						IAssetNetAlgorithm assetAlgorithm = userIdAndAlgorithm.getValue().getAssetPropagationDelegator();
						synchronized (assetAlgorithm.getAssetNetwork()) {
							if (isToStoreCashBeforeResolveQuestion()) {
								// update evidence one-by-one and store cash before/after them. Note that cash is only dependent to assets, that's why we can do this after we updated the shared BN
								for (ResolveQuestionNetworkAction resolution : getResolutions()) {
									// update getUserIdToResolvedQuestionCashBeforeMap() and getUserIdToResolvedQuestionCashGainMap() by obtaining cash before and after asset update
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
											questionToCashBefore.put(resolution.getQuestionId(), cashBefore);
											getUserIdToResolvedQuestionCashBeforeMap().put(userIdAndAlgorithm.getKey(), questionToCashBefore);
											// resolve the question in the asset structure of this user
											assetAlgorithm.setAsPermanentEvidence(Collections.singletonMap((INode)assetAlgorithm.getAssetNetwork().getNode(resolution.getQuestionId().toString()), resolution.getSettledState()), isToDeleteResolvedNode());
											// obtain the difference between cash before and after.
											float gain = getCash(userIdAndAlgorithm.getKey(), null, null) - cashBefore;
											// update the mapping if the difference was "large" enough
											if (Math.abs(gain) > getProbabilityErrorMargin()) {
												// put the mapping from question ID (of settled question) to gain from that question
												questionToGain.put(resolution.getQuestionId(), gain);
												getUserIdToResolvedQuestionCashGainMap().put(userIdAndAlgorithm.getKey(), questionToGain);
											}
										}
									}
								}
							} else {
								// just update evidences all by once
								assetAlgorithm.setAsPermanentEvidence(mapOfEvidences, isToDeleteResolvedNode());
							}
						}
					}
				}
			}
			
			// update the history of resolved questions
			synchronized (getResolvedQuestions()) {
				getResolvedQuestions().putAll(mapForHistory);
			}
			try {
				Debug.println(getClass(), "Executed resolve set, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		/** The set of ResolveQuestionNetworkAction which this action integrates */
		public Collection<ResolveQuestionNetworkAction> getResolutions() { return resolutions; }

		/** @see edu.gmu.ace.daggre.MarkovEngineImpl.ResolveQuestionNetworkAction#setWhenExecutedFirstTime(java.util.Date)*/
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) {
			super.setWhenExecutedFirstTime(whenExecutedFirst);
			for (ResolveQuestionNetworkAction action : getResolutions()) {
				action.setWhenExecutedFirstTime(whenExecutedFirst);
			}
		}
		
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
		synchronized (this.getResolvedQuestions()) {
			if (this.getResolvedQuestions().containsKey(questionId)) {
				throw new IllegalArgumentException("Question " + questionId + " was resolved already.");
			}
		}
		Node node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (node == null) {
			// check if node will be created in same transaction
			boolean isOK = false;
			if (transactionKey != null) {
//				List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey);
//				if (actions == null) {
//					throw new IllegalArgumentException("Transaction key " + transactionKey + " was not started or was concluded.");
//				}
//				synchronized (actions) {
//					for (NetworkAction networkAction : actions) {
//						if (networkAction instanceof AddQuestionNetworkAction
//								&& networkAction.getQuestionId() != null
//								&& networkAction.getQuestionId().longValue() == questionId) {
//							if (settledState >= ((AddQuestionNetworkAction)networkAction).getNumberStates()) {
//								throw new IllegalArgumentException("Question " + questionId + " has no state " + settledState);
//							}
//							isOK = true;
//							break;
//						}
//					}
//				}
				Map<Long, Set<Long>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
				
				
				if (mapTransactionQuestions == null) {
					// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
					throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
				}
				
				// use the mapping in order to check whether the parent will be created in the same transaction
				synchronized (mapTransactionQuestions) {
					Set<Long> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
					if (questionsInSameTransaction == null) {
						throw new InexistingQuestionException("Question ID " + questionId + " does not exist and is not specified in transaction : " + transactionKey, questionId);
					}
					// search for the assumption ID in questionsToBeCreatedInTransaction
					for (Long idInMapping : questionsInSameTransaction) {
						if (idInMapping.longValue() == questionId) {
							isOK = true;
							break;
						}
					}
				}
			}
			if (!isOK) {
				throw new InexistingQuestionException("Question ID " + questionId + " was not found.", questionId);
			}
		} else {
			// node != null at this point. Check consistency
			if (settledState >= node.getStatesSize()) {
				throw new IllegalArgumentException("Question " + questionId + " has no state " + settledState);
			}
			if (node instanceof TreeVariable ) {
				TreeVariable var = (TreeVariable) node;
				if (var.hasEvidence()) {
					int state = (settledState<0)?((-settledState)-1):settledState;
					if (var.getEvidence() != state || var.getMarginalAt(state) <= 0f) {
						throw new IllegalArgumentException("Question " + questionId + " is already resolved, hence it cannot be resolved to " + settledState);
					}
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
		
//		return this.getProbList(questionId, assumptionIds, assumedStates, true);
		Map<Long, List<Float>> probLists = this.getProbLists(Collections.singletonList(questionId), assumptionIds, assumedStates, true, null, false);
		if (probLists != null) {
			return probLists.get(questionId);
		}
		return null;
	}
	
	/**
	 * This method offers the same functionality of {@link #getProbList(long, List, List)}, but
	 * there is an additional argument to indicate whether or not to normalize the result.
	 * @param isToNormalize : if false, the returned list will contain the clique potentials without normalization.
	 * This may be useful for obtaining the clique potentials instead of the conditional probabilities.
	 * @param net : network to be used in order to extract the probabilities
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 * @see #getProbList(long, List, List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, boolean isToNormalize) throws IllegalArgumentException {
		return this.getProbList(questionId, assumptionIds, assumedStates, isToNormalize, null);
	}
	/**
	 * This method offers the same functionality of {@link #getProbList(long, List, List)}, but
	 * there is an additional argument to indicate whether or not to normalize the result.
	 * @param isToNormalize : if false, the returned list will contain the clique potentials without normalization.
	 * This may be useful for obtaining the clique potentials instead of the conditional probabilities.
	 * @param net : network to be used in order to extract the probabilities
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 * @see #getProbList(long, List, List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, 
			boolean isToNormalize, ProbabilisticNetwork net) throws IllegalArgumentException {
		Map<Long, List<Float>> probLists = this.getProbLists(
				Collections.singletonList(questionId), assumptionIds, assumedStates, isToNormalize, net,
				net != getProbabilisticNetwork()); // if net != shared net, then we can change it
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
		return this.getProbLists(questionIds, assumptionIds, assumedStates, true, null, false);
	}

	
	/**
	 * This method offers the same functionality of {@link #getProbLists(List, List, List)}, but
	 * there is an additional argument to indicate whether or not to normalize the result.
	 * @param isToNormalize : if false, the returned list will contain the clique potentials without normalization.
	 * This may be useful for obtaining the clique potentials instead of the conditional probabilities.
	 * @param net : net to be used to extract probabilities.
	 * {@link #getProbabilisticNetwork()} will be used if not specified.
	 * @param canChangeNet : if true, values in net may be changed after this call. Setting this to true
	 * may improve speed. This value is ignored if net == null.
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 */
	public Map<Long,List<Float>> getProbLists(List<Long> questionIds, List<Long>assumptionIds, List<Integer> assumedStates, 
			boolean isToNormalize, ProbabilisticNetwork net, boolean canChangeNet) throws IllegalArgumentException {
		
		// initial assertion: check consistency of assumptionIds and assumedStates
		if (assumptionIds != null && assumedStates != null) {
			if (assumedStates.size() != assumptionIds.size()) {
				throw new IllegalArgumentException("assumptionIds.size() == " + assumptionIds.size() 
						+ ", assumedStates.size() == " + assumedStates.size());
			}
		}
		
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
		if (conditionalProbabilityExtractor == null) {
			throw new RuntimeException(
					"Could not reuse conditional probability extractor of the current default inference algorithm. " +
					"Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
		}
		
		// this is the object to be returned
		Map<Long,List<Float>> ret = new HashMap<Long, List<Float>>();
		
		// if assumptions are not in same clique, we can still use BN propagation to obtain probabilities.
		ProbabilisticNetwork netToUseWhenNotInSameClique = null;	// this is going to be a clone of the shared BN
		List<PotentialTable> cptList = new ArrayList<PotentialTable>();	// will contain conditional probability within clique
		
		// use the shared prob net, if not specified
		if (net == null) {
			net = getProbabilisticNetwork();
			// do not allow changes in the shared net
			canChangeNet = false;
		}
		
		// treat assumptions at this point, so that we can handle resolved questions in assumptions too
		List<INode> parentNodes = new ArrayList<INode>();
		boolean hasIncompatibleResolvedAssumption = false;
		if (assumptionIds != null) {
			// prepare a list to store assumptions which were resolved - they will be deleted from the assumptionIds if provided states are compatible.
			List<Long> resolvedAssumptionIds = new ArrayList<Long>();
			List<Integer> indexesOfResolvedAssumptionIds = new ArrayList<Integer>();	// stores the index of resolved assumptions, so that we can use it in assumedStates
			for (Long id : assumptionIds) {
				INode node = net.getNode(Long.toString(id));
				if (node == null) {
					// Node not in current BN. Check whether it was resolved
					synchronized (getResolvedQuestions()) {
						// check if id was resolved
						StatePair statePair = getResolvedQuestions().get(id);
						if (statePair != null) {
							if (assumedStates == null || assumptionIds.indexOf(id) >= assumedStates.size() || assumedStates.get(assumptionIds.indexOf(id)) == null) {
								// the state was not specified. Just ignore the assumption in such case
								resolvedAssumptionIds.add(id);
								indexesOfResolvedAssumptionIds.add(assumptionIds.indexOf(id));
							} else  if (statePair.getResolvedState() != assumedStates.get(assumptionIds.indexOf(id))) {
								// found a resolved assumption which is not compatible with the resolution (i.e. assumed a state which differs from resolved state)
								// we should return NaN in this case.
								hasIncompatibleResolvedAssumption = true;
								break;
							} else {
								// in all other cases (e.g. the assumption was resolved, and the specified state is equal to the settled state), 
								// then just ignore the assumption
								resolvedAssumptionIds.add(id);
								indexesOfResolvedAssumptionIds.add(assumptionIds.indexOf(id));
							}
						} else {
							// this question actually does not exist
							throw new InexistingQuestionException("Assumption " + id + " not found", id);
						}
					}
				} else {
					parentNodes.add(node);
				}
			}
			// update the assumptionIds if there were resolved questions in it
			if (!resolvedAssumptionIds.isEmpty()) {
				// update assumedStates
				List<Integer> auxAssumedStates = new ArrayList<Integer>();
				// fill auxAssumedStates only with states which are not of resolved assumptions
				if (assumedStates != null) {
					for (int j = 0; j < assumedStates.size(); j++) {
						if (!indexesOfResolvedAssumptionIds.contains(j)) {
							auxAssumedStates.add(assumedStates.get(j));
						}
					}
				}
				assumedStates = auxAssumedStates;
				
				// update the assumptionIds
				// use a clone, so that we won't change the original list
				assumptionIds = new ArrayList<Long>(assumptionIds);
				// filter the resolved assumptions 
				assumptionIds.removeAll(resolvedAssumptionIds);
			}
		}
		
		// if we need to get probabilities of all nodes, and there are assumptions, then we need propagation anyway
		List<Long> questionIdsWithoutResolvedQuestions = (questionIds == null)?null:new ArrayList<Long>(questionIds);
		if (!hasIncompatibleResolvedAssumption && questionIds == null && assumptionIds != null && assumedStates != null && !assumptionIds.isEmpty() && !assumedStates.isEmpty()) {
			// netToUseWhenAssumptionsAreNotInSameClique != null if we need propagation
			if (canChangeNet) {
				netToUseWhenNotInSameClique = net;
			} else {
				netToUseWhenNotInSameClique = getDefaultInferenceAlgorithm().cloneProbabilisticNetwork(net);
			}
		} else {
			// first, attempt to fill cptList with conditional probabilities that can be calculated without propagation.
			int sizeOfQuestionIdsOrTotalNodeNum = getProbabilisticNetwork().getNodeCount();
			if (questionIds != null && !questionIds.isEmpty()) {
				sizeOfQuestionIdsOrTotalNodeNum = questionIds.size();
			}
			for (int i = 0; i < sizeOfQuestionIdsOrTotalNodeNum; i++) {
				synchronized (net) {
					INode mainNode = null;
					Long questionId = null;
					if (questionIds != null && !questionIds.isEmpty()) {
						questionId = questionIds.get(i);
						mainNode = net.getNode(Long.toString(questionId));
					} else {
						mainNode = net.getNodeAt(i);
						questionId = Long.parseLong(mainNode.getName());
					}
					if (mainNode == null ) { // note: questionId == null will not happen here, because it would have thrown nullpointerexception
						// this  boolean was commented out because it was only useful when questionId == null, which cannot happen at this point
//						boolean isResolvedQuestion = false;
						if (isToObtainProbabilityOfResolvedQuestions()) {
							// If it is a resolved question, we can get marginal (non-assumptive) probabilities from history.
							// TODO also build conditional probability
							try {
								Debug.println(getClass(), "Node " + questionId + " was not found. Retrieving marginal probability from history...");
							} catch (Throwable t) {
								t.printStackTrace();
							}
							// Retrieve from history
							synchronized (getResolvedQuestions()) {
//								// if questionId == null, retrieve all resolved questions. 
//								Collection<Long> questionsToSearch = null;
//								if (questionId == null) {
//									questionsToSearch = getResolvedQuestions().keySet();
//								} else {
//									questionsToSearch = Collections.singletonList(questionId);
//								}
//								// iterate either on single node or all resolved nodes
//								for (Long id : questionsToSearch) {
//									StatePair statePair = getResolvedQuestions().get(id);
//									if (statePair != null) {
//										// build probability of resolution (1 state is 100% and others are 0%)
//										int stateCount = statePair.getStatesSize();	// retrieve quantity of states
//										List<Float> marginal = new ArrayList<Float>(stateCount);	// list of marginal probs
//										for (int k = 0; k < stateCount; k++) {
//											// set the settled state as 100%, and all others to 0%
//											if (hasIncompatibleResolvedAssumption) {
//												marginal.add(Float.NaN);	// if there is incompatible assumption, it is NaN no matter what was the settled state
//											} else {
//												marginal.add((k==statePair.getResolvedState())?1f:0f);
//											}
//										}
//										ret.put(questionId, marginal);
//										isResolvedQuestion = true;	// remember that this question was actually resolved previously
//									}
//								}
								// note: the above code was commented and substituted with the following because questionId == null cannot happen at this point
								StatePair statePair = getResolvedQuestions().get(questionId);
								if (statePair != null) {
									if (questionIdsWithoutResolvedQuestions != null) {
										// questionIdsWithoutResolvedQuestions == null means we are treating all nodes anyway, so we do not need to filter nodes.
										// this list is used when there are out-of-clique assumptions. Because ret is already filled with probs of resolved questions, we only need
										// to complete it with questions not resolved yet. So, this list stores which questions were not resolved yet.
										questionIdsWithoutResolvedQuestions.remove(questionId);	
									}
									// build probability of resolution (1 state is 100% and others are 0%)
									int stateCount = statePair.getStatesSize();	// retrieve quantity of states
									List<Float> marginal = new ArrayList<Float>(stateCount);	// list of marginal probs
									for (int k = 0; k < stateCount; k++) {
										// set the settled state as 100%, and all others to 0%
										if (hasIncompatibleResolvedAssumption) {
											marginal.add(Float.NaN);	// if there is incompatible assumption, it is NaN no matter what was the settled state
										} else {
											marginal.add((k==statePair.getResolvedState())?1f:0f);
										}
									}
									ret.put(questionId, marginal);
									continue; // resolved question was treated, so we do not need to execute the remaining code of current loop
								}
							}
						}
						// the following code was commented out because when resolved question was trated, then continue was called anyway
//						if (isResolvedQuestion) {
//							// note: resolved question was treated completely, so we do not need to execute the remaining code of current loop
//							continue;
//						}
						throw new InexistingQuestionException("Question " + questionId + " not found", questionId);
					} else if (hasIncompatibleResolvedAssumption) { // note: "else" means mainNode != null
						// fill with NaN
						List<Float> prob = new ArrayList<Float>(mainNode.getStatesSize());
						for (int index = 0; index < mainNode.getStatesSize(); index++) {
							prob.add(Float.NaN);
						}
						ret.put(questionId, prob);
						continue; // this question was treated (as NaN), so we do not need to execute the remaining code of current loop (no need to fill cptList)
					} else { // mainNode != null && !hasIncompatibleResolvedAssumption
						// this is not a resolved question, and we are not dealing with resolved assumptions.
					}
					if (assumptionIds != null && assumptionIds.contains(questionId) && assumedStates != null && assumedStates.size() > assumptionIds.indexOf(questionId)) {
						// this is requesting for a probability conditioned to itself... If we assume something, then the probability is 1 for assumed state.
						List<Float> prob = new ArrayList<Float>(mainNode.getStatesSize());
						for (int index = 0; index < mainNode.getStatesSize(); index++) {
							prob.add((index==assumedStates.get(assumptionIds.indexOf(questionId)))?1f:0f);
						}
						ret.put(questionId, prob);
					} else if (isToNormalize) { // questionId is not in assumption
						try {
							cptList.add((PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, net, null));
						} catch (NoCliqueException e) {
							Debug.println(getClass(), "Could not extract potentials within clique. Trying global propagation.", e);
							netToUseWhenNotInSameClique = getDefaultInferenceAlgorithm().cloneProbabilisticNetwork(net);
							cptList.clear();
							break;	// do not fill cptList anymore, because if we need to do 1 propagation anyway, the computational cost is the same for any quantity of nodes to propagate
						}
					} else {  // questionId is not in assumption and it is not to normalize
						// by specifying a non-normalized junction tree algorithm to conditionalProbabilityExtractor, we can force it not to normalize the result
						cptList.add((PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(mainNode, parentNodes, net, getDefaultInferenceAlgorithm().getAssetPropagationDelegator()));
					}
				}	// unlock prob network
			}	// end of loop
		}
		
		if (hasIncompatibleResolvedAssumption) {
			// at this point, ret is supposedly filled with NaN for all queried nodes
			return ret;
		}
		
		// If we need to do propagation, do it in non-critical portion of code
		if (netToUseWhenNotInSameClique != null) {
			// note: ret is supposedly alredy filled with probs of resolved nodes, so we need only to fill probs of unresolved nodes
			// this method is supposedly not including resolved questions.
			ret.putAll(this.previewProbPropagation(questionIdsWithoutResolvedQuestions, assumptionIds, assumedStates, netToUseWhenNotInSameClique));
			return ret;
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
	 * This is just a method for converting {@link TreeVariable#getMarginalAt(int)} to a list of floats.
	 * @param node a node containing marginal probability
	 * @return the marginal probability converted to list of floats
	 */
	protected List<Float> getProbList(TreeVariable node) {
		if (node == null) {
			return null;
		}
		List<Float> ret = new ArrayList<Float>(node.getStatesSize());
		for (int i = 0; i < node.getStatesSize(); i++) {
			ret.add(node.getMarginalAt(i));
		}
		return ret;
	}


	/**
	 * It uses findingNodeIDs and findingStates to set and propagate findings
	 * in pn, and return the marginal of node identified by filterNodes.
	 * This method do not include resolved questions.
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
//				Treat resolved nodes
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
	@SuppressWarnings("unchecked")
	public List<Float> getAssetsIfStates(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		
		// initial assertion
		if (assumptionIds != null && assumptionIds.contains(questionId)) {
			throw new IllegalArgumentException("Question " + questionId + " should not assume itself: " + assumptionIds);
		}
		
		// check that user was created (lazily or completely)
		List<Float> unitializedAssets = getListOfAssetsOfLazyOrUnInitializedUser(userId, questionId, assumptionIds, assumedStates);
		if (unitializedAssets != null) {
			return unitializedAssets;
		}
		
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
		
		return this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false, null, null);	// false := return assets instead of q-values
	}
	
	/**
	 * This method uses checks {@link #getUserToAssetAwareAlgorithmMap()} to
	 * see if a user was created. If not created yet, then it uses
	 *  {@link #getUninitializedUserToAssetMap()}  in order to see if the
	 *  user is lazily initialized.
	 *  If the user is lazily initialized, it creates a list whose size is
	 *  number of states of questionId * number of states of nodes in assumptionIds
	 *  which states were not specified in assumedStates.
	 *  The list is then filled with the content of {@link #getUninitializedUserToAssetMap()}
	 * @param userId : this user will be tested whether user was already created or not
	 * @param questionId : the returned list will represent the table of this question and the assumptions
	 * @param assumptionIds 
	 * @param assumedStates
	 * @return
	 */
	protected List<Float> getListOfAssetsOfLazyOrUnInitializedUser(long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) {
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			if (isToLazyInitializeUsers() && !getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
				// user was not created yet. Check if user is in the collection of lazily initialized users
				int sizeOfRet = 0;	// this will be the states of question multiplied by states of assumptions
				synchronized (getProbabilisticNetwork()) {
					// extract the node, so that we know how many states there are
					Node node = getProbabilisticNetwork().getNode(Long.toString(questionId));
					if (node == null) {
						throw new InexistingQuestionException("Question " + questionId + " not found in the network", questionId);
					}
					sizeOfRet = node.getStatesSize();
					// multiply by the sizes of the assumptions
					if (assumptionIds != null) {
						for (int i = 0; i < assumptionIds.size(); i++) {
							if (assumedStates == null || assumedStates.size() <= i || assumedStates.get(i) == null) {
								// the state of this assumption was not specified. 
								// Hence, the size of the list to return must contain all the possible states of this assumption.
								// So, the size will be multiplied by the number of states of this assumption
								Node assumption = getProbabilisticNetwork().getNode(Long.toString(assumptionIds.get(i)));
								if (assumption == null) {
									throw new InexistingQuestionException("Assumption " + assumptionIds.get(i) + " not found in the network", assumptionIds.get(i));
								}
								sizeOfRet *= assumption.getStatesSize();
							}
						}
					}
				}
				// prepare the list to return
				List<Float> ret = new ArrayList<Float>(sizeOfRet);
				Float asset = null;	// if user was created lazily, this will become non-null
				synchronized (getUninitializedUserToAssetMap()) {
					asset = getUninitializedUserToAssetMap().get(userId);
				}
				if (asset == null) {
					//user was not created yet. Hence, by default, user's asset is the initial value
					asset = getDefaultInitialAssetTableValue();
				} // else, the user is stored in the collection of lazily initialized users. The variable "asset" will contain initial value + manna
				
				for (int i = 0; i < sizeOfRet; i++) {
					ret.add(asset);
				}
				
				return ret;
			}
		}
		return null;
	}
	
	/**
	 * This method is similar to {@link #getListOfAssetsOfLazyOrUnInitializedUser(long, long, List, List)},
	 * but it returns a single value instead of a list.
	 * This method uses checks {@link #getUserToAssetAwareAlgorithmMap()} to
	 * see if a user was created. If not created yet, then it uses
	 *  {@link #getUninitializedUserToAssetMap()}  in order to see if the
	 *  user is lazily initialized.
	 *  If the user is lazily initialized, it creates a list whose size is
	 *  number of states of questionId * number of states of nodes in assumptionIds
	 *  which states were not specified in assumedStates.
	 *  The list is then filled with the content of {@link #getUninitializedUserToAssetMap()}
	 * @param userId : this user will be tested whether user was already created or not
	 * @param questionId : the returned list will represent the table of this question and the assumptions
	 * @param assumptionIds 
	 * @param assumedStates
	 * @return
	 */
	protected Float getAssetsOfLazyOrUnInitializedUser(long userId) {
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			if (isToLazyInitializeUsers() && !getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
				// user was not created yet. Check if user is in the collection of lazily initialized users
				Float asset = null;	// if user was created lazily, this will become non-null
				synchronized (getUninitializedUserToAssetMap()) {
					asset = getUninitializedUserToAssetMap().get(userId);
				}
				if (asset == null) {
					//user was not created yet. Hence, by default, user's asset is the initial value
					asset = getDefaultInitialAssetTableValue();
				} // else, the user is stored in the collection of lazily initialized users. The variable "asset" will contain initial value + manna
				
				if (isToUseQValues()) {
					return getScoreFromQValues(asset);
				} 
				return asset;
			}
		}
		return null;
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
	 * @param clique : only assets in this clique will be considered. If null, a clique containing questionId and assumptionIds will be used
	 * @param cptOfQuestionGivenAssumptionsObtainedFromClique: table extracted from clique containing the
	 * clique potentials of questionId given assumptionIds at states assumedStates
	 * @return the change in user delta if a given states occurs if the specified assumptions are met. 
	 * The indexes are relative to the indexes of the states.
	 * In the case of a binary question this will return a [if_true, if_false] value, if multiple choice will return a [if_0, if_1, if_2...] value list
	 * For example, assuming that the question identified by questionId is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the delta of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the delta of the
	 * question while it is in state "true".
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List getAssetsIfStates(long questionId, List<Long> assumptionIds, List<Integer> assumedStates, 
			AssetAwareInferenceAlgorithm algorithm, boolean isToReturnQValuesInsteadOfAssets, Clique clique, PotentialTable cptOfQuestionGivenAssumptionsObtainedFromClique)
				throws IllegalArgumentException {
		// basic assertion
		if (algorithm == null) {
			throw new NullPointerException("AssetAwareInferenceAlgorithm cannot be null");
		}
		
		// some assumptions may be ignored because they were resolved. 
		// In such case, unignoredAssumedStates will contain only the states of assumptions which were not ignored
		List<Integer> unignoredAssumedStates = (assumedStates==null)?null:new ArrayList<Integer>(assumedStates.size());
		
		PotentialTable assetTable = cptOfQuestionGivenAssumptionsObtainedFromClique;	// asset tables (clique table containing delta) are instances of potential tables
		boolean hasIncompatibleResolvedAssumption = false;
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
						synchronized (getResolvedQuestions()) {
							// check if id was resolved
							StatePair statePair = getResolvedQuestions().get(id);
							if (statePair != null) {
								if (assumedStates == null || assumptionIds.indexOf(id) >= assumedStates.size() || assumedStates.get(assumptionIds.indexOf(id)) == null) {
									// the state was not specified. Just ignore the assumption in such case
								} else  if (statePair.getResolvedState() != assumedStates.get(assumptionIds.indexOf(id))) {
									// found a resolved assumption which is not compatible with the resolution (i.e. assumed a state which differs from resolved state)
									// we should return NaN in this case.
									hasIncompatibleResolvedAssumption = true;
								} else {
									// in all other cases (e.g. the assumption was resolved, and the specified state is equal to the settled state), 
									// then just ignore the assumption
								}
							} else {
								// this question actually does not exist
								throw new InexistingQuestionException("Assumption " + id + " not found", id);
							}
						}
					} else {
						if (unignoredAssumedStates != null) {
							// add state to unignoredAssumedStates so that we can use it later in order to extract assets in correct positions regarding only non-questions
							if (assumptionIds.indexOf(id) <  assumedStates.size()) {
								// only add state if the state was really specified
								unignoredAssumedStates.add(assumedStates.get(assumptionIds.indexOf(id)));
							} else {
								// by default, null should be considered as ignorable state
								unignoredAssumedStates.add(null);
							}
						} 
						parentNodes.add(node);
					}
				}
			}
			if (assetTable == null || parentNodes.isEmpty()) {
				// only re-obtain asset table if not specified or we are calculating marginal assets
				if (parentNodes.isEmpty()) { // we are not calculating the conditional delta. We are calculating delta of 1 node only (i.e. "marginal" delta)
					boolean backup = mainNode.isToCalculateMarginal();	// backup old config
					IRandomVariable associatedCliqueSepBkp = mainNode.getAssociatedClique();	// backup old clique/separator associated w/ this node
					mainNode.setToCalculateMarginal(true);		// force marginalization to calculate something.
					if (clique != null) {
						mainNode.setAssociatedClique(clique);	// force marginalization to use the current clique
					}
					mainNode.updateMarginal(); 					// make sure values of mainNode.getMarginalAt(index) is up to date
					mainNode.setToCalculateMarginal(backup);	// revert to previous config
					if (clique != null) {
						mainNode.setAssociatedClique(associatedCliqueSepBkp);	// restore to backup
					}
				}
				if (conditionalProbabilityExtractor instanceof InCliqueConditionalProbabilityExtractor) {
					InCliqueConditionalProbabilityExtractor inCliqueConditionalProbabilityExtractor = (InCliqueConditionalProbabilityExtractor) conditionalProbabilityExtractor;
					assetTable = (PotentialTable) inCliqueConditionalProbabilityExtractor.buildCondicionalProbability(
							mainNode, parentNodes, algorithm.getAssetNetwork(), algorithm.getAssetPropagationDelegator(), ASSET_CLIQUE_EVIDENCE_UPDATER, clique
							);
				} else {
					throw new UnsupportedOperationException("getConditionalProbabilityExtractor() with instance other than " 
							+ InCliqueConditionalProbabilityExtractor.class.getName() + " is not supported by this version.");
				}
			}
		}
		
		// convert cpt to a list, given assumedStates.
		// TODO change the way it sum-out/min-out/max-out the unspecified states in the clique potential
		List ret = new ArrayList(assetTable.tableSize());
		for (int i = 0; i < assetTable.tableSize(); i++) {
			boolean isToSkip = false;
			// filter entries which are incompatible with unignoredAssumedStates
			if (unignoredAssumedStates != null && !unignoredAssumedStates.isEmpty()) {
				// extract coordinate of the states (e.g. [2,1,0] means state of mainNode = 2, state of parent1 = 1, and parent2 == 0)
				int[] multidimensionalCoord = assetTable.getMultidimensionalCoord(i);
				// note: size of unignoredAssumedStates is 1 unit smaller than multidimensionalCoord, because unignoredAssumedStates does not contain the main node
				if (multidimensionalCoord.length != unignoredAssumedStates.size() + 1) {
					throw new RuntimeException("Multi dimensional coordinate of index " + i + " has size " + multidimensionalCoord.length
							+ ". Expected " + (unignoredAssumedStates.size()+1));
				}
				// start from index 1, because index 0 of multidimensionalCoord is the main node
				for (int j = 1; j < multidimensionalCoord.length; j++) {
					if ((unignoredAssumedStates.get(j-1) != null)
							&& (unignoredAssumedStates.get(j-1) != multidimensionalCoord[j])) {
						isToSkip = true;
						break;
					}
				}
			}
			if (!isToSkip) {
				if (hasIncompatibleResolvedAssumption) {
					// if the resolved assumption is not in the resolved state, the value is undefined.
					ret.add(Float.NaN);
				} else if (isToReturnQValuesInsteadOfAssets) {
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
		
		// if user was not initialized yet, try calculating the interval of edits without initializing user
		try {
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				if (isToLazyInitializeUsers() && !getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
					// get the cash of uninitialized user
					Float asset = getAssetsOfLazyOrUnInitializedUser(userId);
					if (isToUseQValues()) {
						// getAssetsOfLazyOrUnInitializedUser returns asset values, but AssetAwareInferenceAlgorithm.calculateIntervalOfAllowedEdit
						// expects q-values if isToUseQValues() == true. So, convert assets to q-values
						asset = (float) getQValuesFromScore(asset);
					}
					// user was not initialized yet
					float editInterval[] = AssetAwareInferenceAlgorithm.calculateIntervalOfAllowedEdit(
							isToUseQValues(), 															// just pass configuration of this object
							getProbList(questionId, assumptionIds, assumedStates).get(questionState), 	// P(T=t|A)
							asset, asset,																// Min(T=t|A) = Min(T!=t|A) = asset
							this																		// MarkovEngineImpl can convert asssets<->qvalues
							);
					
					// convert editInterval to a list
					List<Float> ret = new ArrayList<Float>(editInterval.length);
					for (float interval : editInterval) {
						ret.add(interval);
					}
					return ret;
				}
			}
		} catch (Throwable t) {
			Debug.println(getClass(), t.getMessage(), t);
			// if edit limit could not be calculated from uninitialized user, we have to initialize user anyway.
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
		// initial assertion: assert that the bayes net was compiled
		if (getProbabilisticNetwork() == null || getProbabilisticNetwork().getJunctionTree() == null) {
			throw new IllegalStateException("The Bayes net was not initialized. Nodes must be added.");
		}
		
		// check if user is not initialized, or user is lazily initialized. If so, we do not need calculation
		Float asset = getAssetsOfLazyOrUnInitializedUser(userId);
		if (asset != null) {
			// the user was not initialized, or it is lazily initialized.
			return asset;
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
		
		float ret = Float.NEGATIVE_INFINITY;	// value to return
		synchronized (algorithm.getAssetNetwork()) {
			Map<INode, Integer> conditions = new HashMap<INode, Integer>();
			// set up findings
			if (assumptionIds != null) {
				for (int i = 0; i < assumptionIds.size(); i++) {
					AssetNode node = (AssetNode) algorithm.getAssetNetwork().getNode(Long.toString(assumptionIds.get(i)));
					if (node == null) {
						synchronized (getResolvedQuestions()) {
							// check if id was resolved
							StatePair statePair = getResolvedQuestions().get(assumptionIds.get(i));
							if (statePair != null) {
								if (assumedStates == null || assumptionIds.indexOf(assumptionIds.get(i)) >= assumedStates.size() || assumedStates.get(assumptionIds.indexOf(assumptionIds.get(i))) == null) {
									// the state was not specified. Just ignore the assumption in such case
									continue; // by continuing, this assumption won't be added into conditions (so it won't be set to Infinite in min propagation)
								} else  if (statePair.getResolvedState() != assumedStates.get(assumptionIds.indexOf(assumptionIds.get(i)))) {
									// found a resolved assumption which is not compatible with the resolution (i.e. assumed a state which differs from resolved state)
									// we should return NaN in this case.
									return Float.NaN;
								} else {
									// in all other cases (e.g. the assumption was resolved, and the specified state is equal to the settled state), 
									// then just ignore the assumption
									continue; // by continuing, this assumption won't be added into conditions (so it won't be set to Infinite in min propagation)
								}
							} else {
								// this question actually does not exist
								throw new InexistingQuestionException("Assumption " + assumptionIds.get(i) + " does not exist.", assumptionIds.get(i));
							}
						}
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
		/*
		 * Note:
		 * Expected[ Expected( Assets(X=x1|A) ) + ... + Expected( Assets(X=xn|A) ) ] 
		 * = P(X=x1) * Expected( Assets(X=x1|A) ) + ... + P(X=xn) * Expected(Assets(X=xn|A)) 
		 * = expected global assets.
		 * Hence, we can just call scoreUserEv instead.
		 * However, we must at least check that questionId exist
		 */
		if (questionId != null) {
			Node node = null;
			synchronized (getProbabilisticNetwork()) {
				node = getProbabilisticNetwork().getNode(questionId.toString());
			}
			synchronized (getResolvedQuestions()) {
				if (node == null && !getResolvedQuestions().containsKey(questionId)) {
					// node is not in network, and it is not a resolved question either.
					throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
				}
			}
		}
		return scoreUserEv(userId, assumptionIds, assumedStates);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEvStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// do not compute expected score locally (i.e. compute globally)
		return this.scoreUserQuestionEvStates(userId, questionId, assumptionIds, assumedStates, false);
	}
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEvStates(long, long, java.util.List, java.util.List, boolean)
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, 
			List<Long>assumptionIds, List<Integer> assumedStates, boolean isToComputeLocally) throws IllegalArgumentException {
		// TODO not to recalculate values in disconnected cliques, because they are not likely to change
		// initial assertion: if assumptions were specified and states were not specified
		if (assumptionIds != null && assumedStates != null && assumedStates.size() != assumptionIds.size()) {
			throw new IllegalArgumentException("Expected size of assumedStates is " + assumptionIds.size() + ", but was " + assumedStates.size());
		}
		
		// check if user was properly initialized. If not, we can just return stored or constant values
		List<Float> listOfAssetsOfLazyInitializedUser = this.getListOfAssetsOfLazyOrUnInitializedUser(userId, questionId, assumptionIds, assumedStates);
		if (listOfAssetsOfLazyInitializedUser != null) {
			// the user is not completely initialized yet, so return the content obtained from getListOfAssetsOfLazyInitializedUser
			return listOfAssetsOfLazyInitializedUser;
		}
		
		// obtain the main node
		INode node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (node == null) {
			// check if node was resolved
			synchronized (getResolvedQuestions()) {
				// check if id was resolved
				StatePair statePair = getResolvedQuestions().get(questionId);
				if (statePair != null) {
					// resolved questions have special treatment
					List<Float> ret = new ArrayList<Float>(statePair.getStatesSize());
					for (int i = 0; i < statePair.getStatesSize(); i++) {
						// the settled state will have the global/conditional score. Everything else will be NaN
						if (statePair.getResolvedState() != null && i == statePair.getResolvedState().intValue()) {
							// Note: this portion will be called only once
							ret.add(this.scoreUserEv(userId, assumptionIds, assumedStates));
						} else {
							ret.add(Float.NaN);
						}
					}
					// return immediately
					return ret;
				}
			}
			// if reached this portion, then this question actually does not exist
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
		}
		
		// list to return
		List<Float> ret = new ArrayList<Float>(node.getStatesSize());
		
		
		// this is the mapt to be passetd to the local expected score calculation
		Map<INode, Integer> conditionsForLocalExpectedScore = null;
		AssetAwareInferenceAlgorithm algorithm = null;
		// fill map if isToComputeLocally, or the two lists if not
		if (isToComputeLocally) {
			// use mapping from node to state
			conditionsForLocalExpectedScore = new HashMap<INode, Integer>();	// mapping from prob node to assumed states
			if (assumptionIds != null) {
				// extract the nodes
				synchronized (getProbabilisticNetwork()) {
					for (int index = 0; index < assumptionIds.size(); index++) {
						INode assumptionNode = getProbabilisticNetwork().getNode(assumptionIds.get(index).toString());
						if (assumptionNode == null) {
//							throw new IllegalArgumentException("Question " + assumptionIds.get(index) + " is not present in the system.");
							Debug.println(getClass(), "Question " + assumptionIds.get(index) + " is not present in the system.");
							continue;
						}
						if (assumedStates.size() <= index || assumedStates.get(index) == null) {
							Debug.println(getClass(), "State in index " + index + " will be ignored.");
							continue;
						}
						conditionsForLocalExpectedScore.put(assumptionNode, assumedStates.get(index));
					}
				}
			}
			try {
				// extract the algorithm to be used for local calculation
				algorithm = getAlgorithmAndAssetNetFromUserID(userId);
			} catch (Exception e) {
				// exception translation is not a good habit, but at least the original is included in cause
				throw new RuntimeException(e);
			}
		}
		
		// if we assume the question itself, do different operations
		if (assumptionIds == null || !assumptionIds.contains(questionId)) {	// questionId is not assumed
			// this is a non-null list of assumptions to pass to scoreUserEv (assumptionIds & questionId) 
			List<Long> assumptionsIncludingThisQuestion = null;
			List<Integer> assumedStatesIncludingThisState = null;
			
			if (!isToComputeLocally) {
				// use 2 lists: one with assumption nodes and another with states
				assumptionsIncludingThisQuestion = new ArrayList<Long>();
				// similarly, non-null list of states to pass to scoreUserEv (assumedStates and states of questionId)
				assumedStatesIncludingThisState = new ArrayList<Integer>(); // do not reuse 
				if (assumptionIds != null) {
					assumptionsIncludingThisQuestion.addAll(assumptionIds);
				}
				if (assumedStates != null) {
					assumedStatesIncludingThisState.addAll(assumedStates);
				}
				// the last element in the list will contain the question itself
				assumptionsIncludingThisQuestion.add(questionId);
				assumedStatesIncludingThisState.add(0);
			}
			
			// just calculate conditional expected score given each state of questionId... Use assumptionsIncludingThisQuestion and states
			for (int i = 0; i < node.getStatesSize(); i++) {
				if (isToComputeLocally) {
					// set the state of main node to the current state
					conditionsForLocalExpectedScore.put(node, i);
					
					// calculate expected assets locally
					synchronized (algorithm.getRelatedProbabilisticNetwork()) {
						synchronized (algorithm.getAssetNetwork()) {
							ret.add((float) algorithm.calculateExpectedLocalAssets(conditionsForLocalExpectedScore));
						}
					}
					
				} else { 
					// TODO optimize
					assumedStatesIncludingThisState.set(assumedStatesIncludingThisState.size()-1, i);
					ret.add(this.scoreUserEv(userId, assumptionsIncludingThisQuestion, assumedStatesIncludingThisState));
				}
			}
		} else {	// questionId is assumed
			// at this point, assumptionIds is not null and contains questionId
			// extract what was assumed
			Integer assumedStateOfThisQuestion = assumedStates.get(assumptionIds.indexOf(questionId));
			// calculate conditional expected score only for the evidence state, and the other states will have 0
			for (int i = 0; i < node.getStatesSize(); i++) {
				// TODO optimize
				if (i == assumedStateOfThisQuestion) {
					// the assumed state will have the value added
					if (isToComputeLocally) {
						try {
							// calculate expected assets locally
							ret.add((float)algorithm.calculateExpectedLocalAssets(conditionsForLocalExpectedScore));
						} catch (Exception e) {
							// exception translation is not a good habit, but at least the original is included in cause
							throw new RuntimeException(e);
						}
					} else {
						ret.add(this.scoreUserEv(userId, assumptionIds, assumedStates));
					}
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
		
		// check if user was properly initialized. If not, we can just return stored or constant values
		List<Float> listOfAssetsOfLazyInitializedUser = this.getListOfAssetsOfLazyOrUnInitializedUser(userId, questionId, assumptionIds, assumedStates);
		if (listOfAssetsOfLazyInitializedUser != null) {
			// the user is not completely initialized yet, so return the content obtained from getListOfAssetsOfLazyInitializedUser
			return listOfAssetsOfLazyInitializedUser;
		}
		
		// obtain the main node, so that we can obtain how many states it has
		INode node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		// this var will hold the number of states that the main node (identified by questionId) has
		int statesSize = 0;	// the number of states will be used as the size of the list to return (and for iteration)
		if (node == null) {
			synchronized (getResolvedQuestions()) {
				// check if id was resolved
				StatePair statePair = getResolvedQuestions().get(questionId);
				if (statePair == null) {
					// this question actually does not exist, because it is neither in the shared BN nor in the resolved questions
					throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
				} else {
					// extract number of states from the history of resolved questions
					statesSize = statePair.getStatesSize();
				}
			}
		} else {
			// extract number of states from the current node
			statesSize = node.getStatesSize();
		}
		
		// list to return
		List<Float> ret = new ArrayList<Float>(statesSize);
		
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
			for (int i = 0; i < statesSize; i++) {
				// TODO optimize
				assumedStatesIncludingThisState.set(assumedStatesIncludingThisState.size()-1, i);
				ret.add(this.getCash(userId, assumptionsIncludingThisQuestion, assumedStatesIncludingThisState));
			}
		} else {	// questionId is assumed
//			throw new IllegalArgumentException("Assumptions must not contain the question itself.");
			Integer assumedStateOfThisQuestion = assumedStatesIncludingThisState.get(indexOfThisQuestion);
			// calculate conditional expected score only for the evidence state, and the other states will have 0
			for (int i = 0; i < statesSize; i++) {
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
						synchronized (getResolvedQuestions()) {
							// check if id was resolved
							StatePair statePair = getResolvedQuestions().get(id);
							if (statePair != null) {
								if (assumedStates == null || assumptionIds.indexOf(id) >= assumedStates.size() || assumedStates.get(assumptionIds.indexOf(id)) == null) {
									// the state was not specified. Just ignore the assumption in such case
									continue; // by continuing, this assumption won't be added into nodeNameToStateMap (so it won't be used in hard evidence propagation)
								} else  if (statePair.getResolvedState() != assumedStates.get(assumptionIds.indexOf(id))) {
									// found a resolved assumption which is not compatible with the resolution (i.e. assumed a state which differs from resolved state)
									// we should return NaN in this case.
									return Float.NaN;
								} else {
									// in all other cases (e.g. the assumption was resolved, and the specified state is equal to the settled state), 
									// then just ignore the assumption
									continue; // by continuing, this assumption won't be added into nodeNameToStateMap (so it won't be used in hard evidence propagation)
								}
							} else {
								// this question actually does not exist
								throw new InexistingQuestionException("Assumption " + id + " not found", id);
							}
						}
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
		
		// check if user is not initialized, or user is lazily initialized. If so, we do not need calculation
		Float asset = getAssetsOfLazyOrUnInitializedUser(userId);
		if (asset != null) {
			// the user was not initialized, or it is lazily initialized.
			return asset;
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
	
	/**
	 * Just delegates to {@link #previewTrade(long, long, List, List, List)}.
	 * In future implementations, {@link #previewTrade(long, long, List, List, List)} will be 
	 * delegating to this method instead.
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#previewTrade(edu.gmu.ace.daggre.TradeSpecification)
	 */
	public List<Float> previewTrade(TradeSpecification tradeSpecification) throws IllegalArgumentException {
		return this.previewTrade(tradeSpecification.getUserId(), tradeSpecification.getQuestionId(), tradeSpecification.getProbabilities(), 
				tradeSpecification.getAssumptionIds(), tradeSpecification.getAssumedStates());
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#previewTrade(long, long, java.util.List, java.util.List, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Float> previewTrade(long userId, long questionId,List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		
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
		
		// check if user was properly initialized. If not, we can just return stored or constant values
		List<Float> assets = this.getListOfAssetsOfLazyOrUnInitializedUser(userId, questionId, assumptionIds, assumedStates);
		if (assets != null) {
			// the user is not completely initialized yet, so return values calculated from getListOfAssetsOfLazyInitializedUser
			// extract the current (old) probability, in order to calculate the ratio
			List<Float> oldValues = getProbList(questionId, assumptionIds, assumedStates);
			// this is the list to be returned
			List<Float> ret = new ArrayList<Float>(newValues.size());
			for (int i = 0; i < newValues.size(); i++) {
				// probs and assets are all supposedly related by indexes
				ret.add(assets.get(i) + this.getScoreFromQValues(newValues.get(i)/oldValues.get(i)));
			}
			return ret;
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
		List<Float> oldValues = this.executeTrade(
				questionId, 
				null,	// if this is null, it won't do corrective trades
				newValues, 
				assumptionIds, 
				assumedStates, 
				true,  // 1st boolean == true := allow negative delta, since this is a preview. 
				algorithm, 
				!isToThrowExceptionOnInvalidAssumptions(),  // whether do or don't overwrite assumptionIds and assumedStates
				!isToDoFullPreview(), 
				null // we are not interested in obtaining what other questions' marginals were affected by this trade
			);	
		
		if (isToDoFullPreview()) {
			// TODO optimize (executeTrade and getAssetsIfStates have redundant portion of code)
			// return the asset position
			return this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false, null, null); // false := return assets instead of q-values
		}
		
		// just return estimated values
		// obtain q-values
//		List<Float> qValues = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, true);
		assets = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, false, null, null);
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
	 * @param oldValues : this is a list (ordered collection) representing the probability values before the edit. 
	 * If {@link #isToUseCorrectiveTrades()} == false, this value will just be ignored.
	 * If null, these values will be simply extracted from the network referenced by the argument "algorithm".
	 * If non-null, the house account (a special user which will not have limitations regarding assets) will add a trade
	 * in order to set the current probability to the value specified in this argument, if the current probability
	 * differs (with error margin {@link #getProbabilityErrorMargin()}) from the specified here.
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
	 * If false, they will be only input arguments. Note: if  assumptionIds and assumedStates are immutable lists, then
	 * they will be only input arguments anyway.
	 * @param parentTrade : (optional) points to the trade action executing this method. This will be used to fill
	 * trade history of conditional probabilities.
	 * @return the probabilities before trade. This list will have the same structure of newValues, but its content will be filled
	 * with the probabilities before applying the trade.
	 * @see #addTrade(long, Date, String, long, long, List, List, List, boolean)
	 * @see #previewTrade(long, long, List, List, List)
	 * @see #getProbabilityErrorMargin()
	 * @see #isToUseCorrectiveTrades()
	 */
	protected List<Float> executeTrade(long questionId, List<Float> oldValues, List<Float> newValues, 
			List<Long> assumptionIds, List<Integer> assumedStates, 
			boolean isToAllowNegative, AssetAwareInferenceAlgorithm algorithm, boolean isToUpdateAssumptionIds, boolean isPreview, 
			NetworkAction parentTrade) {
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
				if (isToThrowExceptionInTradesToResolvedQuestions()) {
					throw new IllegalArgumentException("State " + i + " of question " + child + " given " + assumptionIds +  " = " + assumedStates + " has probability " + child.getMarginalAt(i) + " and cannot be changed.");
				} else {
					return null;
				}
			}
		}
		
		
		// this var will store the correct size of cpd. If negative, owner of the cpd was not found.
		int expectedSizeOfCPD = child.getStatesSize();
		
		// convert the set of assumptions and states to valid set
		List<Long> oldAssumptionIds = null;	// this will store a copy of the original assumptions list
		if (assumptionIds != null) {
			oldAssumptionIds = new ArrayList<Long>(assumptionIds);
			List<Long> validAssumptions = this.getMaximumValidAssumptionsSublists(questionId, oldAssumptionIds, 1).get(0);
			if (isToUpdateAssumptionIds) {
				// CAUTION: this is modifying an input argument (this is the desired behavior)
				try {
					assumptionIds.clear();
				} catch (UnsupportedOperationException e) {
					// this list is immutable. Use a mutable instance instead.
					assumptionIds = new ArrayList<Long>(assumptionIds.size());
				}
				if (validAssumptions != null) {
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
				try {
					assumedStates.clear();
				} catch (UnsupportedOperationException e) {
					// this list is immutable. Use a mutable instance instead.
					assumedStates = new ArrayList<Integer>(assumedStates.size());
				}
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
//			if (parent.hasEvidence()) {
////				throw new IllegalArgumentException("Question " + parent + " is already resolved and cannot be used.");
//				Debug.println(getClass(),"Question " + parent + " is already resolved and cannot be used.");
//				continue;
//			}
//			for (int i = 0; i < parent.getStatesSize(); i++) {
//				if (parent.getMarginalAt(i) == 0.0f || parent.getMarginalAt(i) == 1.0f) {
////					throw new IllegalArgumentException("State " + i + " of question " + parent + " has probability " + parent.getMarginalAt(i) + " and cannot be changed.");
//					Debug.println(getClass(),"State " + i + " of question " + parent + " has probability " + parent.getMarginalAt(i) + " and cannot be changed.");
//					continue;
//				}
//			}
			assumptionNodes.add(parent);
			// size of cpd if  = MULT (<quantity of states of child and parents>).
			expectedSizeOfCPD *= parent.getStatesSize();
		}
		
		// check consistency of newValues
		if (newValues == null) {
			throw new RuntimeException("Cannot set probability to null.");
		}
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
		
		// note: at this point, newValues is consistent, so we can compare it to oldValues to see if oldValues is consistent
		if (isToUseCorrectiveTrades() && oldValues != null && !oldValues.isEmpty()	// these are conditions for not ignoring oldValues
				&& oldValues.size() != newValues.size()) { // if we should not ignore oldValues, then the size of oldValues must be the same of newValues
			throw new IllegalArgumentException("Inconsistent specification of old probabilities: the size of oldValues should be the same of newValues.");
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
		
		// the list to be returned (the old values). This will also be used to compare with old values specified by caller 
		// (so that we can execute corrective trades)
		List<Float> condProbBeforeTrade = new ArrayList<Float>(newValues.size());	// force probBeforeTrade to have the same size of newValues
		
		// change content of potential according to newValues and assumedStates
		if (assumedStates == null || assumedStates.isEmpty()) {
			for (int i = 0; i < newValues.size(); i++) {
				condProbBeforeTrade.add(potential.getValue(i));
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
				condProbBeforeTrade.add(potential.getValue(multidimensionalCoord));
				potential.setValue(multidimensionalCoord, newValues.get(i));
			}
		}
		
		// note: we already checked that the size of oldValues and newValues matched. 
		if (isToUseCorrectiveTrades() && oldValues != null && !oldValues.isEmpty() // these are conditions for not ignoring oldValues
				&& condProbBeforeTrade.size() != oldValues.size()) {	// Now, need to check whether sizes of oldValues and condProbBeforeTrade matches.
			throw new IllegalStateException("The current Bayes net indicates that question " + questionId + " has " + condProbBeforeTrade.size()
					+ " states, but the old probabilities provided by caller had " + oldValues.size() 
					+ " states. This may indicate inconsistency of the Bayes net, or you may be using incompatible version of Markov Engine.");
		}
		
		if (isPreview) {
			// don't actually propagate if we only need to preview
			return condProbBeforeTrade;
		}
		
		// if the oldValues was specified by the caller, then we have to check whether it matches with current probability. If not, we must do correction
		if (isToUseCorrectiveTrades() && oldValues != null && !oldValues.isEmpty() ){	// these are conditions for not ignoring oldValues
			// compare current probability with the probability specified by caller as being the probability before trade
			boolean isDifferent = false;
			// note: we already checked that the size of oldValues and condProbBeforeTrade matched
			for (int i = 0; i < oldValues.size(); i++) {
				if (Math.abs(oldValues.get(i) - condProbBeforeTrade.get(i)) > getProbabilityErrorMargin()) {
					isDifferent = true;
					break;
				}
			}
			if (isDifferent) {	// must do a corrective trade
				// use a house account (with infinite assets) in order to do the corrective trade
				AssetAwareInferenceAlgorithm houseAccountAlgorithm = algorithm;
				if (algorithm.getNetwork() == getProbabilisticNetwork()) {
					// if this is changing the global shared net, use the default algorithm (which is for administrative/house user)
					houseAccountAlgorithm = getDefaultInferenceAlgorithm();
				} // or else, use the original algorithm, but setting setToUpdateAssets(false). 
				synchronized (houseAccountAlgorithm) {
					synchronized (houseAccountAlgorithm.getNetwork()) {
						// store current config regarding whether to update assets
						boolean backupConfig = houseAccountAlgorithm.isToUpdateAssets();
						houseAccountAlgorithm.setToUpdateAssets(false);	// force algorithm not to update assets (simulate infinite assets)
						// recursively call executeTrade in order to set the current probability to the ones specified in oldValues
						this.executeTrade(
								questionId, 
								null,		// disable corrective trade this time, by setting oldValues = null, so that we don't generate loop
								oldValues,	// the trade shall set the current probability to oldValues
								assumptionIds, assumedStates, 
								isToAllowNegative, houseAccountAlgorithm, isToUpdateAssumptionIds, isPreview, 
								new CorrectiveTradeAction(parentTrade, questionId, condProbBeforeTrade, oldValues) // intantiate a network action representing the corrective trade
						);
						// restore config
						houseAccountAlgorithm.setToUpdateAssets(backupConfig);
					}
				}
				// Note: because this recursive call to executeTrade calls addToLastNCliquePotentialMap and addVirtualTradeIntoMarginalHistory,
				// and these 2 methods instantiates VirtualTradeAction, whose returns isCorrectiveTrade == true when the wrapped
				// trade is a CorrectiveTradeAction, this call guarantees that the QuestionEvent retrieved in getQuestionHistory
				// will have isCorrectiveTrade == true (so, we can be detect that a corrective trade has changed the probability).
				// Additionally, we won't include the CorrectiveTradeAction into getExecutedActions(), because we don't want
				// the corrective trade to be re-executed on rebuild (we want it to be always re-calculated on-the-fly)
			}
		}
		
		// this map will hold marginal probabilities before the trade, so that we can store the history of changes in marginal probabilities of other nodes
		Map<Long, List<Float>> marginalsBefore = null;	// note: marginalsBefore is usually different to condProbBeforeTrade, because marginalsBefore is unconditional prob
		
		// fill array of likelihood with values in CPT
		float [] likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		child.addLikeliHood(likelihood, assumptionNodes);
		
		// propagate soft evidence
		synchronized (algorithm) {
			synchronized (algorithm.getRelatedProbabilisticNetwork()) {
				synchronized (algorithm.getAssetNetwork()) {
//					boolean backup = algorithm.isToAllowQValuesSmallerThan1();
					algorithm.setToAllowZeroAssets(isToAllowNegative);
					
					// Note: memento (snapshot) costs more than algorithm.revertLastProbabilityUpdate(), but it can revert 2 or more updates.
					// in our case, if the cash goes below zero, we'll revert 2 operations: the min propagation and the asset/prob update
					IAssetNetAlgorithmMemento memento = null;
					if (algorithm.getRelatedProbabilisticNetwork() == getProbabilisticNetwork()) {
						// we are making changes in the global shared bayes net, not its clone
						if ( !isToAllowNegative ) {
							// if this trade does not allow negative, or if we need to store the history of conditional probabilities, then create snapshot now
							memento = algorithm.getMemento();
						}
						if (!child.getParents().isEmpty() || !child.getChildren().isEmpty()) {
							// question connected to another question. So, we need to store changes in probabilities
							// Store marginals
							marginalsBefore = getProbLists(null, null, null);
							
							if (memento == null && getMaxConditionalProbHistorySize() > 0 ) {
								// store cliques for conditional probabilities
								memento = algorithm.getMemento();
							}
						}
					}
					// Note: this memento is also used in order to store clique tables, so that we can retrieve history of any conditional probabilities

					// store marginal before trade, if the parent trade was not executed previously
					if (parentTrade != null && parentTrade.getWhenExecutedFirstTime() == null) {
						if (marginalsBefore != null) {
							// reuse the map of marginals obtained previously
							parentTrade.setOldValues(marginalsBefore.get(questionId));
						} else {
							// obtain marginal from node
							parentTrade.setOldValues(getProbList(child));
						}
						// Note: the difference between oldValues and tradeSpecification.oldProbabilities is that the prior is marginal prob, and the latter can be conditional prob
					}
					
					// actually update the probabilities and assets
					algorithm.propagate();
//					algorithm.setToAllowQValuesSmallerThan1(backup);
					
					// store marginal after trade, if the parent trade was not executed previously
					if (parentTrade != null && parentTrade.getWhenExecutedFirstTime() == null) {
						// obtain marginal from node
						parentTrade.setNewValues(getProbList(child));
					}
					
					// check that minimum is below 0
					if (algorithm.isToUpdateAssets() && !isToAllowNegative) {
						// calculate minimum by running min propagation and then calculating global assets posterior to min propagation
						algorithm.runMinPropagation(null);
						// Note: if we are using q-values, 1 means 0 (because of log scale).
						if (algorithm.calculateExplanation(null) <= (this.isToUseQValues()?1:0)) {
							// Only revert the last operations (the min propagation and asset/prob update) if we are updating the original network
							if (algorithm.getRelatedProbabilisticNetwork() == getProbabilisticNetwork()) {
								try {
									algorithm.setMemento(memento);
								} catch (Exception e) {
									// this is an exception translation (a bad habit), but allows us to add personalized message.
									throw new RuntimeException("Could not revert last trade after a negative asset was detected. The probability or assets may be inconsistent now.",e);
								}
							}
							throw new ZeroAssetsException(algorithm.getAssetNetwork() + ", Cash <= 0");
						}
						// if successful, only revert last min propagation
						algorithm.undoMinPropagation();
					}
					
					// check if prob clique potentials have changed. If so, store in the history of conditional probabilities
					// only store in the history if we are currently editing the shared Bayes net
					if (memento != null && (getMaxConditionalProbHistorySize() > 0 && (!child.getParents().isEmpty() || !child.getChildren().isEmpty()) )) { 
						// note: memento != null includes the condition: algorithm.getRelatedProbabilisticNetwork() == getProbabilisticNetwork()
						if (memento instanceof AssetPropagationInferenceAlgorithmMemento) {
							// update the map getLastNCliquePotentialMap()
							addToLastNCliquePotentialMap(parentTrade, ((AssetPropagationInferenceAlgorithmMemento) memento).getProbCliques());
						} else {
							throw new UnsupportedOperationException("This version of the markov engine can only support "
									+ AssetPropagationInferenceAlgorithmMemento.class.getName()
									+ ". This may have been caused by incompatible libraries or version numbers.");
						}
					}
					
				}
			}
		}
		
		// compare prior and posterior marginal, and fill affectedQuestions
		if (marginalsBefore != null) {	
			// note: if getWhenExecutedFirstTime() == null, then marginalsBefore == null
			addVirtualTradeIntoMarginalHistory(parentTrade, marginalsBefore); // link from original trade to virtual trades
		}
		
		return condProbBeforeTrade;
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
	 * Suppose that the asset clique structure is as the following:
	 * <br/><br/>
	 *    | c1 c1 c2 c2	<br/>
	 *    | b1 b2 b1 b2	<br/>
	 * -----------------	<br/>
	 * a1 | x1 x3 x5 x7	<br/>
	 * a2 | x2 x4 x6 x8	<br/>
	 * 	<br/>	<br/>
	 * If we are balancing a1 and a2 given assumption c1, then we shall make
	 * x1 = x2 AND x3 = x4, 
	 * which is equivalent to balancing a1 and a2 given b1 and c1,
	 * and then balancing a1 and a2 given b2 and c1.
	 * The following routines converts "balance a1 and a2 given assumption c1"
	 * to "balance a1 and a2 given b1 and c1, and then balance a1 and a2 given b2 and c1".
	 * @param isToAllowNegative : if false, the preview will not generate trades that will result in negative assets
	 * 
	 */
	protected List<TradeSpecification> previewBalancingTrades(AssetAwareInferenceAlgorithm userAssetAlgorithm, long questionId, List<Long> originalAssumptionIds, 
			List<Integer> originalAssumedStates, Clique clique, boolean isToAllowNegative) throws IllegalArgumentException {
		// initial assertions
		if (originalAssumptionIds != null && !originalAssumptionIds.isEmpty() && this.getPossibleQuestionAssumptions(questionId, originalAssumptionIds).isEmpty()) {
			if (this.isToThrowExceptionOnInvalidAssumptions()) {
				new InvalidAssumptionException(originalAssumptionIds + " are invalid assumptions for question " + questionId);
			}
			// convert the set of assumptions to a valid set
			List<Long> oldAssumptionIds = originalAssumptionIds;
			// note: getMaximumValidAssumptionsSublists will always return at least 1 element (at least the empty list)
			originalAssumptionIds = this.getMaximumValidAssumptionsSublists(questionId, originalAssumptionIds, 1).get(0);
			// change assumedStates accordingly to new assumptionIds
			originalAssumedStates = this.convertAssumedStates(originalAssumptionIds, oldAssumptionIds, originalAssumedStates);

		}
		if (originalAssumptionIds == null) {
			originalAssumptionIds = Collections.emptyList();
		}
		if (originalAssumedStates == null) {
			originalAssumedStates = Collections.emptyList();
		}
		
		List<TradeSpecification> ret = new ArrayList<TradeSpecification>();
		
		// asset net algorithm of the user (this will be used to extract cliques and nodes)
//		AssetAwareInferenceAlgorithm userAssetAlgorithm = null;
//		try {
//			userAssetAlgorithm = getAlgorithmAndAssetNetFromUserID(userId);
//		} catch (InvalidParentException e) {
//			throw new RuntimeException("Could not instantiate user " + userId,e);
//		}
		
		// node identified by questionId
		AssetNode mainNode = null;	
		synchronized (userAssetAlgorithm) {
			mainNode = (AssetNode) userAssetAlgorithm.getAssetNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			throw new IllegalArgumentException("Could not find clique containing nodes: " + questionId + " and assumptions " + originalAssumptionIds);
		}
		
		// create a list of nodes in clique without the main node
		List<INode> fullParentNodes = new ArrayList<INode>(clique.getNodes());
		fullParentNodes.remove(mainNode);
		
		// similarly, create a list of question IDs without the main node
		List<Long> fullAssumptionIds = new ArrayList<Long>();
		for (INode parent : fullParentNodes) {
			if (parent instanceof ProbabilisticNode
					&& ((ProbabilisticNode)parent).hasEvidence()) {
				// ignore hard evidences
				continue;
			}
			fullAssumptionIds.add(Long.parseLong(parent.getName()));
		}
		
		// object to be used in order to extract cliques
		InCliqueConditionalProbabilityExtractor cliqueExtractor = (InCliqueConditionalProbabilityExtractor) this.getConditionalProbabilityExtractor();
		
		
		if (fullParentNodes.isEmpty()) { // we are not calculating the conditional asset. We are calculating asset of 1 node only (i.e. "marginal" asset)
			boolean backup = mainNode.isToCalculateMarginal();	// backup old config
			IRandomVariable cliqueSepBkp = mainNode.getAssociatedClique();	// backup the clique/separator associated with this node
			mainNode.setToCalculateMarginal(true);		// force marginalization to calculate something.
			if (clique.getNodes().contains(mainNode)) {
				mainNode.setAssociatedClique(clique);		// force marginalization to use clique provided from caller of this method
			} else if (cliqueSepBkp instanceof Separator) {
				// make sure we are using cliques, never separators
				mainNode.setAssociatedClique(((Separator) cliqueSepBkp).getClique1());	// use 1st clique, by default
			}
			mainNode.updateMarginal(); 					// make sure values of mainNode.getMarginalAt(index) is up to date
			mainNode.setToCalculateMarginal(backup);	// revert to previous config
			mainNode.setAssociatedClique(cliqueSepBkp);	// revert to previous clique/separator
		}
		// generate a table with mainNode as the 1st variable, so that we can easily iterate over all states of parents
		// TODO use another way to iterate over parents
		PotentialTable table = (PotentialTable) cliqueExtractor.buildCondicionalProbability(mainNode, fullParentNodes , userAssetAlgorithm.getAssetNetwork(), userAssetAlgorithm, ASSET_CLIQUE_EVIDENCE_UPDATER, clique);
		for (int i = 0; i < table.tableSize(); i += mainNode.getStatesSize()) {	//iterate over parents, and ignore main node (that's why i += mainNode.getStatesSize())
			
			// convert i to states of all nodes
			int[] coord = table.getMultidimensionalCoord(i);	
			if (coord[0] != 0) {
				// supposedly, the indexes starts from zero
				throw new RuntimeException("Index of conditional assets ("+ mainNode + "|"+fullParentNodes + ") became unsynchronized. ");
			}
			
			// if the assets are already close enough, no need to balance this column
			boolean isCloseEnough = true;
			for (int mainNodeStateIndex = 1; mainNodeStateIndex < mainNode.getStatesSize(); mainNodeStateIndex++) {
				coord[0] = mainNodeStateIndex-1;
				float valuePrev = table.getValue(coord);
				coord[0] = mainNodeStateIndex;
				float valueAfter = table.getValue(coord);
				if (Math.abs(valueAfter - valuePrev) > getAssetErrorMargin()) {
					isCloseEnough = false;
					break;
				}
			}
			coord[0] = 0;	// keep the index of the main node always on 1st state
			if (isCloseEnough) {
				// assets are already close enough, no need to balance this column
				continue;
			}
			
			// check if the states related to cell i of table matches originalAssumedStates
			boolean matches = true;
			for (int nodeIndex = 1; nodeIndex < coord.length; nodeIndex++) {
				int indexInOriginalAssumptionsList = originalAssumptionIds.indexOf(Long.parseLong(table.getVariableAt(nodeIndex).getName()));
				if (indexInOriginalAssumptionsList >= 0) {
					// this node is specified in originalAssumptionIds. Filter it.
					if (coord[nodeIndex] != originalAssumedStates.get(indexInOriginalAssumptionsList)) {
						matches = false;
						break;
					}
				}
				// if some node is resolved, then we shall only consider the resolved state
				if ((table.getVariableAt(nodeIndex) instanceof ProbabilisticNode)
						&& ((ProbabilisticNode)table.getVariableAt(nodeIndex)).hasEvidence()) {
					if (coord[nodeIndex] != ((ProbabilisticNode)table.getVariableAt(nodeIndex)).getEvidence()) {
						matches = false;
						break;
					}
				}
			}
			if (matches) {
				// fill fullAssumedStates with current states of parents (i.e. values in coord)
				List<Integer> fullAssumedStates = new ArrayList<Integer>();
				for (int nodeIndex = 1; nodeIndex < coord.length; nodeIndex++) {// coord has mainNode in its 0th index, so start from 1
					// we assume that the ordering of nodes related to fullAssumedStates and coord are the same
					if ((table.getVariableAt(nodeIndex) instanceof ProbabilisticNode)
							&& ((ProbabilisticNode)table.getVariableAt(nodeIndex)).hasEvidence()) {
						// ignore hard evidences
						continue;
					}
					fullAssumedStates.add(coord[nodeIndex]);
				}
				if (fullAssumptionIds.size() != fullAssumedStates.size()) {
					throw new RuntimeException("Failed to consistently handle resolved questions in assumption: " 
							+ fullAssumptionIds + " = " + fullAssumedStates);
				}
				List<Float> balancingTrade = this.previewBalancingTrade(userAssetAlgorithm, questionId, fullAssumptionIds, fullAssumedStates, clique, table, isToAllowNegative);
				// the name of the asset net is supposedly the user ID
				ret.add(new CliqueSensitiveTradeSpecificationImpl(Long.parseLong(userAssetAlgorithm.getAssetNetwork().getName()), questionId, balancingTrade, fullAssumptionIds, fullAssumedStates, clique));
			}
		}
		
		// reduce the quantity of balancing trades
		return this.collapseSimilarBalancingTrades(ret);
	}
	
	/**
	 * Some set of trades with same probabilities and common set of assumptions can be
	 * collapsed into a single trade. This method converts such set of trades into
	 * a collapsed set of trades.
	 * @param trades : trade specifications obtained from 
	 * {@link #previewBalancingTrades(AssetAwareInferenceAlgorithm, long, List, List, Clique, boolean)}
	 * @see #splitTradesByTargetQuestion(List)
	 * @see #splitTradesByCliques(List)
	 * @see #splitTradesByAssumptions(List)
	 * @see #splitTradesByProbability(List)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List<TradeSpecification> collapseSimilarBalancingTrades( List<TradeSpecification> trades) {
		if (!isToCollapseSimilarBalancingTrades()) {
			return trades;
		}
		
		// split the list of trades by using some categories (e.g. split the trades by same probabilities and same assumptions)
		List<List<TradeSpecification>> splittedList = new ArrayList<List<TradeSpecification>>(1);
		
		// initialize the list with the trades
		splittedList.add(trades);
		
		// TODO use dependency injection in order to create a common method for all splitTrades* methods
		
		// divide the trades by questions
		splittedList = splitTradesByTargetQuestion(splittedList);
		
		// divide the trades by cliques
		splittedList = (List)splitTradesByCliques((List)splittedList);
		
		// divide the trades by assumptions
		splittedList = splitTradesByAssumptions(splittedList);
		
		// divide the trades by probability
		splittedList = splitTradesByProbability(splittedList);
		
		// This list will contain the collaped trades
		List<TradeSpecification> ret = new ArrayList<TradeSpecification>(trades.size());
		
		// if same probability is being applied for all states in an assumption, merge
		for (List<TradeSpecification> list : splittedList) {
			ret.addAll(this.mergeBalancingTradesWithSameAssumptions(list));
		}
		
		return ret;
	}

	
	/**
	 * If a set of trades sets the probability to a same value, uses same assumptions (but the states may be different), and
	 * some of the assumptions are using all possible states, then such assumption can be deleted from trade.
	 * This method deletes such assumptions.
	 * @see #mergeBalancingTradesWithSameAssumptions(List, int).
	 */
	protected List<TradeSpecification> mergeBalancingTradesWithSameAssumptions( List<TradeSpecification> list) {
		return this.mergeBalancingTradesWithSameAssumptions(list, 0);
	}
	
	/**
	 * If a set of trades sets the probability to a same value, uses same assumptions (but the states may be different), and
	 * some of the assumptions are using all possible states, then such assumption can be deleted from trade.
	 * <br/>
	 * <br/>
	 * E.g.
	 * <br/>
	 * <br/>
	 * Suppose that the nodes have 2 states:
	 * <br/>
	 * Assumptions = [0,1]
	 * <br/>
	 * Assumed states = [0,0], [0,1]
	 * <br/>
	 * Since the states for node 1 are 0 and 1 (i.e. all possible states of node 1), this assumption can be reduced to:
	 * <br/>
	 * <br/>
	 * Assumptions = [0]
	 * <br/>
	 * Assumed states = [0].
	 * <br/><br/>
	 * Another example:
	 * <br/><br/>
	 * Suppose all questions have 3 states ([0,1,2], [3,4,5], [6,7,8] respectively), and the assumed states are:<br/>
	 * [0,3,6] <br/>
	 * [0,3,7]<br/>
	 * [0,3,8]<br/>
	 * [0,4,7]<br/>
	 * [1,3,7]<br/>
	 * [2,3,7]<br/>
	 * <br/><br/>
	 * Then the method will do:
	 * <br/><br/>
	 * 1st step (checks the 1st column of the assumed statess):
	 * (mapping from the value of 1st column the respective values in the other columns)<br/>
	 * 		0 -> [3,6], [3,7], [3,8], [4,7]	<br/>
	 * 		1 -> [3,7]<br/>
	 * 		2 -> [3,7]<br/>
	 * <br/><br/>
	 * Because all states {0,1,2} have mappings, 
	 * and [3,7] is common to all states of 1st node, the assumptions [0,3,7], [1,3,7], [2,3,7] will be merged to [_,3,7], resulting in:
	 * <br/><br/>
	 * [0,3,6] <br/>
	 * [0,3,8]<br/>
	 * [0,4,7]<br/>
	 * [_,3,7]<br/>
	 * <br/><br/>
	 * 2nd step (treats the 2nd column of the assumed states):
	 * (mapping from the value of 2nd column the respective values in the other columns - )<br/>
	 * 		3 -> [0,6], [0,8]; [0,7], [1,7], [2,7] (these last three actually represents line [3,7])<br/>
	 * 		4 -> [0,7]<br/>
	 * 		5 -> null<br/>
	 * <br/><br/>
	 * Because state 5 is not mapped, 2nd step does nothing
	 * <br/><br/>
	 * 3rd step (checks the 3rd column of the assumed statess):
	 * (mapping from the value of 3rd column the respective values in the other columns)<br/>
	 * 		6 -> [0,3],	<br/>
	 * 		7 -> [0,4]; [0,3], [1,3], [2,3] (the last three actually represents line [3,7])<br/>
	 * 		8 -> [0,3]<br/>
	 * <br/><br/>
	 * Because all states {6,7,8} have mappings, 
	 * and [0,3] is common to all states of 1st node, the assumptions [0,3,6], [0,3,7], [0,3,8] will be merged to [0,3,_], resulting in:
	 * <br/><br/>
	 * [0,4,7]<br/>
	 * [_,3,7]<br/>
	 * [0,3,_]<br/>
	 * <br/><br/>
	 * Hence, the 6 assumed states were merged into the following 3 assumptions:
	 * <br/><br/>
	 * [node1, node2, node3] -> states [0,4,7]<br/>
	 * [node2, node3] -> states [3,7]<br/>
	 * [node1, node2] -> states [0,3]<br/>
	 * <br/><br/>
	 * Another simple example for one assumption with 3 possible states:
	 * <br/><br/>
	 * [0]
	 * [1]
	 * [2]
	 * <br/><br/>
	 * Step 1:	<br/>
	 * 		0 -> []	<br/>
	 * 		1 -> []<br/>
	 * 		2 -> []<br/>
	 * <br/><br/>
	 * All states {0,1,2} have mapping, and [] is a common assumption. [0], [1], [2] will be merged, resulting in:
	 * <br/><br/>
	 * [_]
	 * <br/><br/>
	 * Hence, the assumption will become empty.
	 * <br/><br/>
	 * Another example:
	 * <br/><br/>
	 * [0,3,6]<br/>
	 * [0,3,7]<br/>
	 * [0,3,8]<br/>
	 * [0,4,7]<br/>
	 * [1,3,7]<br/>
	 * [1,4,7]<br/>
	 * [2,3,7]<br/>
	 * [2,4,7]<br/>
	 * <br/><br/>
	 * Step (column) 1:
	 * <br/><br/>
	 * 		0 -> [3,6], [3,7], [3,8], [4,7]	<br/>
	 * 		1 -> [3,7], [4,7]<br/>
	 * 		2 -> [3,7], [4,7]<br/>
	 * <br/><br/>
	 * All states are mapped, and [3,7], [4,7] are common.
	 * <br/><br/>
	 * [0,3,6]<br/>
	 * [0,3,8]<br/>
	 * [_,3,7]<br/>
	 * [_,4,7]<br/>
	 * <br/><br/>
	 * Step (column) 2:
	 * <br/><br/>
	 * 		3 -> [0,6], [0,7], [0,8], [1,7], [2,7]	<br/>
	 * 		4 -> [0,7], [1,7], [2,7]<br/>
	 * <br/><br/>
	 * Not all states are mapped. Ignore this step.
	 * <br/><br/>
	 * Step (column) 3:
	 * <br/><br/>
	 * 		6 -> [0,3]	<br/>
	 * 		7 -> [0,3], [1,3], [2,3], [0,4], [1,4], [2,4]<br/>
	 * 		8 -> [0,3]<br/>
	 * <br/><br/>
	 * All states are mapped, and [0,3] is common.
	 * <br/><br/>
	 * [_,3,7]<br/>
	 * [_,4,7]<br/>
	 * [0,3,_]<br/>
	 * <br/><br/>
	 * Hence, the 6 assumed states were merged into the following 3 assumptions:
	 * <br/><br/>
	 * [node2, node3] -> states [3,7]<br/>
	 * [node2, node3] -> states [4,7]<br/>
	 * [node1, node2] -> states [0,3]<br/>
	 * @param list :  list of TradeSpecification with same assumptions and same probabilities, of same question, and same clique.
	 * @param indexOfAssumptionToStart : assumptions will be analysed from this index. This value should be usually set to 0
	 * when calling this method. 
	 * For example, if the assumptions are [3,6,8] and this argument is 1, then the assumption at index 0 (i.e. assumption 3)
	 * will not be considered.
	 * @return list with some TradeSpecification using all states of an assumption merged together
	 * @see #collapseSimilarBalancingTrades(List)
	 */
	protected List<TradeSpecification> mergeBalancingTradesWithSameAssumptions( List<TradeSpecification> list, int indexOfAssumptionToStart) {
		// initial assertion
		if (list == null || list.isEmpty()) {
			return Collections.emptyList();
		}
		
		// it is assumed that all elements in the list have the same assumed questions and there is no repetitions
		
		/*
		 * This matrix contains the original assumed states and will be referenced by the steps in this algorithm
		 * [0,3,6]
		 * [0,3,7]
		 * [0,3,8]
		 * [0,4,7]
		 * [1,3,7]
		 * [2,3,7]
		 */
		List<List<Integer>> originalAssumedStates = new ArrayList<List<Integer>>(list.size());
		// it is assumed that the input argument has no repetitions.
		
		// fill originalAssumedStates
		for (TradeSpecification spec : list) {
			// check consistency
			if (!spec.getAssumptionIds().equals(list.get(0).getAssumptionIds())) {
				// all assumptions must be the same
				throw new IllegalArgumentException("Expected assumptions " + list.get(0).getAssumptionIds() 
						+ " for all trades in the argument, but found " + spec.getAssumptionIds());
			}
			if (spec.getAssumedStates().size() != list.get(0).getAssumptionIds().size()) {
				// all lists (of assumed states) must be with same size = number of assumed questions
				throw new IllegalArgumentException("The states assumed in the argument should have size " + list.get(0).getAssumptionIds().size() 
						+ ", but the assumption " + spec.getAssumptionIds() + " had states " + spec.getAssumedStates());
			}
			if (originalAssumedStates.contains(spec.getAssumedStates())) {
				// the list should not have repeated assumptions (2 trades with exactly the same assumptions - questions + states)
				throw new IllegalArgumentException("Repeated assumption found: questions = " + spec.getAssumptionIds()
						+ ", states = " + spec.getAssumedStates());
			}
			// use a cloned array instead of a reference to spec.getAssumedStates(), because we want to keep the values intact when spec.getAssumedStates() is changed
			originalAssumedStates.add(new ArrayList<Integer>(spec.getAssumedStates()));
		}
		
		/*
		 * This will contain the resulting lists. For example,
		 * [0,4,7]
		 * [_,3,7]
		 * [0,3,_]
		 * The underscore "_" will be represented as null.
		 * The content will be initialized with originalAssumedStates
		 */
//		List<List<Integer>> resultingList = new ArrayList<List<Integer>>(originalAssumedStates);
		
		// the list to actually return. This has index initially synchronized with originalAssumedStates
		List<TradeSpecification> ret = new ArrayList<TradeSpecification>(list);
		
		// this list will contain the nodes (i.e. instances of class ProbabilisticNode) of the assumptions
		List<ProbabilisticNode> assumptionNodes = new ArrayList<ProbabilisticNode>(originalAssumedStates.get(0).size());
		// fill assumptionNodes
		synchronized (getProbabilisticNetwork()) {
			for (Long nodeId : list.get(0).getAssumptionIds()) {
				if (nodeId == null) {
					throw new IllegalArgumentException("Null is an invalid question ID for an assumption.");
				}
				// names of nodes are the ids
				Node node = getProbabilisticNetwork().getNode(nodeId.toString());
				if (node == null) {
					throw new IllegalArgumentException("Could not find node with name " + nodeId);
				}
				assumptionNodes.add((ProbabilisticNode) node);
			}
		}
		
		// at this point, all lists in originalAssumedStates have same size and represents the same state
		
		// another consistency check: if there is no assumption, but there are more than 1 combination of assumptions, then there is some repetition
		if (originalAssumedStates.get(0).size() <= 0 && originalAssumedStates.size() > 1) {
			throw new IllegalArgumentException("There is no assumption, but there are " + originalAssumedStates.size() + " trades to the same question " + list.get(0).getQuestionId());
		}
		
		// Do the steps. The number of steps is the number of assumed questions we have (size of any list in originalAssumedStates)
		// each step treats the "column" of the originalAssumedStates.
		int iterations = originalAssumedStates.get(0).size();	// extract this number only 1 time, to avoid change of context by calling originalAssumedStates.get(0).size() multiple times
		
		// we loop on indexOfAssumptionToStart until the result of a step has changed (i.e. loop ignoring columns which did not change). 
		// If changed, call recursive.
		// That's is: we only do a loop on indexOfAssumptionToStart because we want to increment indexOfAssumptionToStart until we get to the index of
		// assumption which will actually change. The actual steps will be performed by a recursive call.
		for (; indexOfAssumptionToStart < iterations; indexOfAssumptionToStart++) {
			/*
			 * this is the mapping which will store the results of each step. For example:
			 * 		0 -> [3,6], [3,7], [3,8], [4,7]	
			 * 		1 -> [3,7]
			 * 		2 -> [3,7]
			 */
			Map<Integer,List<List<Integer>>> mapping = new HashMap<Integer,List<List<Integer>>>();
			
			// use values in originalAssumedStates (instead of resultingList) and fill mapping
			// We use originalAssumedStates instead of resultingList because we don't want intersections of states with wildcards
			// to be ignored (e.g. [_,3,7] and [0,3,_] have [0,3,7] as intersection, and don't want it to be ignored at each iteration).
			for (List<Integer> assumedStates : originalAssumedStates) {
				// this is the key to be used in the mapping (i.e. value of the current column)
				Integer key = assumedStates.get(indexOfAssumptionToStart);
				
				// extract the mapped values
				List<List<Integer>> mappedValues = mapping.get(key);
				if (mappedValues == null) {
					// this is the 1st time we use reference this key, so create the mappedStates
					mappedValues = new ArrayList<List<Integer>>();
					mapping.put(key, mappedValues);
				}
				
				// generate a list containing the values except the current column
				// e.g. if current column is the 2nd, and assumedStates = [1,2,3], then generate [1,3]
				List<Integer> statesExceptCurrentColumn = new ArrayList<Integer>(assumedStates.size() - 1);
				for (int i = 0; i < assumedStates.size(); i++) {
					if (i != indexOfAssumptionToStart) {
						statesExceptCurrentColumn.add(assumedStates.get(i));
					}
				}
				
				// add the statesExceptCurrentColumn to the mapping
				mappedValues.add(statesExceptCurrentColumn);	// this will update mapping too, because it is a reference
				// note: we assume that the input argument had no repetitions
			}
			
			// this is the node currently being used in the key of mapping (i.e. node representing the current column of originalAssumedStates)
			ProbabilisticNode currentNode = assumptionNodes.get(indexOfAssumptionToStart);
			
			// check basic consistency of key of mapping
			if (currentNode.getStatesSize() < mapping.keySet().size()) {
				// I cannot have more keys than the quantity of states of the currrent node, because the keys represents the states of current node...
				throw new IllegalArgumentException("Inconsistent state found. Question " + currentNode + " has " + currentNode.getStatesSize()
						+ ", but the trade is using " + mapping.keySet().size() + " states.");
			}
			
			// check if there is any state not in key
			if (currentNode.getStatesSize() > mapping.keySet().size()) {
				// if the size does not match, then there is some state not being mapped.
				continue;  // this step does nothing
			}
			
			// check if there is a value common to all keys
			List<List<Integer>> commonValues = new ArrayList<List<Integer>>();
			if (!mapping.isEmpty()) {
				// use this iterator to iterate over keys, so that we can compare the values in 1st key to values in the other keys
				Iterator<Integer> keyIterator = mapping.keySet().iterator();
				// iterate over values mapped from the 1st key.
				for (List<Integer> valueMappedFrom1stKey : mapping.get(keyIterator.next())) {
					boolean isCommon = true;
					while (keyIterator.hasNext()) {
						// extract the next key
						Integer keyOtherThan1st = keyIterator.next();
						if (!mapping.get(keyOtherThan1st).contains(valueMappedFrom1stKey)) {
							// the values mapped from current key does not contain the current value of the 1st key, so this value is not common
							isCommon = false;
							break;
						}
					}
					if (isCommon) {
						// note: value may be an empty list
						commonValues.add(valueMappedFrom1stKey);
					}
				}
			}
			
			// delete trade specs with assumed states matching commonValues, pick one (last) 
			if (!ret.isEmpty() && !commonValues.isEmpty()) {
				// this map traces what trade specifications should have wildcards ("_")
				// this basically contains trade specs whose assumed states contains the values in commonValues,
				// (the keys are the values in commonValues)
				// so that for each value in commonValues, we can pick the last specification whose assumed states contains values in commonValues.
				// the picked trade specs will then have wildcards ("_") added, and the other trade specs will just be deleted.
				// Note a map is used because I want only 1 trade specification for the same common value 
				Map<List<Integer>, TradeSpecification> wildCardedSpecs = new HashMap<List<Integer>, TradeSpecification>();
				// delete the lines regarding the common value from 
				Set<TradeSpecification> specsToDelete = new HashSet<TradeSpecification>();	// this will also contain wildCardedSpecs
				for (TradeSpecification spec : ret) {
					List<Integer> line = spec.getAssumedStates();
					
					// use a clone, so that we can delete a column and compare
					List<Integer> lineWithoutColumn = new ArrayList<Integer>(line);
					lineWithoutColumn.remove(indexOfAssumptionToStart);
					int indexOfLineToDelete = commonValues.indexOf(lineWithoutColumn);
					if (indexOfLineToDelete >= 0) {
						specsToDelete.add(spec);
						// put last spec into the mapping of wildcards
						wildCardedSpecs.put(commonValues.get(indexOfLineToDelete), spec);
					}
				}
				ret.removeAll(specsToDelete);
				
				// pick trade spec from the map of wildcards and add to ret
				for (TradeSpecification tradeSpecification : wildCardedSpecs.values()) {
					// mark current column with wildcard state (i.e. delete current column from assumption).
					tradeSpecification.getAssumptionIds().remove(indexOfAssumptionToStart);
					tradeSpecification.getAssumedStates().remove(indexOfAssumptionToStart);
					ret.add(tradeSpecification);
				}
				
				// the results have changed. Call recursive for the new list
				ret = this.mergeBalancingTradesWithSameAssumptions(ret, indexOfAssumptionToStart);
				
				// we don't need to iterate on indexOfAssumptionToStart anymore, because the recursive call did check all columns after indexOfAssumptionToStart
				break;
			}
			
//			// generate the common value with the wildcard (e.g. [_,3,7])
//			for (List<Integer> commonValue : commonValues) {
//				List<Integer> commonValueWithWildcard = new ArrayList<Integer>(commonValue);
//				// note: the wildcard "_" is represented as null
//				commonValueWithWildcard.add(columnIndex, null);
//				// add the common value with wildcard into resultingList
//				resultingList.add(commonValueWithWildcard);
//			}
		} // end of iteration for each column
		
		
		return ret;
	}
	
	/**
	 * Divides the input list into a list of lists whose each
	 * sub list has the same value of {@link TradeSpecification#getQuestionId()}.
	 * The content will not be re-ordered
	 * @param splittedList : it is assumed to be a list generated by {@link #previewBalancingTrades(AssetAwareInferenceAlgorithm, long, List, List, Clique, boolean)}.
	 * @return
	 * @see #collapseSimilarBalancingTrades(List)
	 */
	protected List<List<TradeSpecification>> splitTradesByTargetQuestion( List<List<TradeSpecification>> listBeforeSplit) {
		if (listBeforeSplit == null || listBeforeSplit.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<List<TradeSpecification>> ret = new ArrayList<List<TradeSpecification>>();
		
		for (List<TradeSpecification> list : listBeforeSplit) {
			// instantiate a list which stores the indexes to split the list by using list.subList
			List<Integer> splitPoints = new ArrayList<Integer>();
			splitPoints.add(0); // by default, include the 1st element as the split point
			for (int i = 0; i < list.size() - 1; i++) {
				if (!list.get(i).getQuestionId().equals(list.get(i+1).getQuestionId())) {
					// dont add split point if the cliques are equal
					// (i.e. add split point if cliques are different)
					splitPoints.add(i+1);
				}
			}
			splitPoints.add(list.size()); // by default, include the last index (exclusive) as the split point
			for (int i = 0; i < splitPoints.size()-1; i++) {
				ret.add(list.subList(splitPoints.get(i), splitPoints.get(i+1)));
			}
		}
		
		return ret;
	}
	
	/**
	 * Divides the input list into a list of lists whose each
	 * sub list has the same value of {@link CliqueSensitiveTradeSpecification#getCliqueId()}.
	 * The content will not be re-ordered
	 * @param splittedList : it is assumed to be a list generated by {@link #previewBalancingTrades(AssetAwareInferenceAlgorithm, long, List, List, Clique, boolean)}.
	 * @return
	 * @see #collapseSimilarBalancingTrades(List)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<List<? extends TradeSpecification>> splitTradesByCliques( List<List<CliqueSensitiveTradeSpecification>> listBeforeSplit) {
		if (listBeforeSplit == null || listBeforeSplit.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<List<? extends TradeSpecification>> ret = new ArrayList<List<? extends TradeSpecification>>();
		
		for (List<CliqueSensitiveTradeSpecification> list : listBeforeSplit) {
			// instantiate a list which stores the indexes to split the list by using list.subList
			List<Integer> splitPoints = new ArrayList<Integer>();
			splitPoints.add(0); // by default, include the 1st element as the split point
			for (int i = 0; i < list.size() - 1; i++) {
				if (!list.get(i).getCliqueId().equals(list.get(i+1).getCliqueId())) {
					// dont add split point if the cliques are equal
					// (i.e. add split point if cliques are different)
					splitPoints.add(i+1);
				}
			}
			splitPoints.add(list.size()); // by default, include the last index (exclusive) as the split point
			for (int i = 0; i < splitPoints.size()-1; i++) {
				ret.add((List)list.subList(splitPoints.get(i), splitPoints.get(i+1)));
			}
		}
		
		return ret;
	}

	/**
	 * Divides the input list into a list of lists whose each
	 * sub list has the same value of {@link TradeSpecification#getProbabilities()}.
	 * <br/>
	 * <br/>
	 * CAUTION: the contents in the sub-lists will be re-ordered by the values inside the probability vector.
	 * Hence, this method ASSUMES THAT {@link #splitTradesByAssumptions(List)}, {@link #splitTradesByCliques(List)}, {@link #splitTradesByTargetQuestion(List)}
	 * were called before this method, so that trades in different contexts are not mixed togather.
	 * @param splittedList : it is assumed to be a list generated by {@link #previewBalancingTrades(AssetAwareInferenceAlgorithm, long, List, List, Clique, boolean)}.
	 * @return
	 * @see #collapseSimilarBalancingTrades(List)
	 * @see #getProbabilityErrorMarginBalanceTrade()
	 */
	protected List<List<TradeSpecification>> splitTradesByProbability( List<List<TradeSpecification>> listBeforeSplit) {
		if (listBeforeSplit == null || listBeforeSplit.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<List<TradeSpecification>> ret = new ArrayList<List<TradeSpecification>>();
		
		for (List<TradeSpecification> list : listBeforeSplit) {
			
			// quick sort the content by the probabilities
			Collections.sort(list, new Comparator<TradeSpecification>() {
				/** The Comparator specifies how to compare two TradeSpecification in the quicksort.
				 *  The compared values are the contents of the probability vector. 
				 *  Do something similar to a string comparison (sort by 1st prob, then by 2nd prob, and so on).
				 *  That is, the values in smaller indexes are considered to be more significative. */
				public int compare(TradeSpecification spec1, TradeSpecification spec2) {
					// extract the probability vectors here, so that we don't change context frequently
					List<Float> prob1 = spec1.getProbabilities();
					List<Float> prob2 = spec2.getProbabilities();
					// do basic assertions
					if (prob1 == null) {
						if (prob2 == null) { // both are null
							return 0;
						} else { // only prob1 is null
							return -1;
						}
					} else if (prob2 == null) { // only prob2 is null
						return 1;
					}
					// at this point, both are non-null
					for (int i = 0; i < prob1.size() && i < prob2.size(); i++) {
						// probs differ if their differences are greater than an error margin
						if (Math.abs(prob1.get(i) - prob2.get(i)) > getProbabilityErrorMarginBalanceTrade()) {
							return ((prob1.get(i) - prob2.get(i)) > 0)?1:-1;
						}
					}
					// If reached this point, contents of same index have matched.
					return  prob1.size() - prob2.size(); // Size of vectors may be different. Use the size for comparison in this case.
				}
			});
			
			// instantiate a list which stores the indexes to split the list by using list.subList
			List<Integer> splitPoints = new ArrayList<Integer>();
			splitPoints.add(0); // by default, include the 1st element as the split point
			for (int i = 0; i < list.size() - 1; i++) {
				// probabilities are different if their sizes are different or their contents are different
				boolean isDifferent = list.get(i).getProbabilities().size() != list.get(i+1).getProbabilities().size();
				if (!isDifferent) {
					// their sizes are equal. Now check content
					for (int j = 0; j < list.get(i).getProbabilities().size(); j++) {
						// contents are different if their differences is greater than error margin
						if (Math.abs(list.get(i).getProbabilities().get(j) - list.get(i+1).getProbabilities().get(j)) > getProbabilityErrorMarginBalanceTrade()) {
							isDifferent = true;
							break;
						}
					}
				}	// else their sizes are diffferent
				if (isDifferent) {
					// dont add split point if the assumptions have same size and one contain all the other 
					// (i.e. add split point if assumptions are different)
					splitPoints.add(i+1);
				}
			}
			splitPoints.add(list.size()); // by default, include the last index (exclusive) as the split point
			for (int i = 0; i < splitPoints.size()-1; i++) {
				ret.add(list.subList(splitPoints.get(i), splitPoints.get(i+1)));
			}
		}
		
		return ret;
	}

	/**
	 * Divides the input list into a list of lists whose each
	 * sub list has the same assumptions.
	 * The content will not be re-ordered
	 * @param splittedList : it is assumed to be a list generated by {@link #previewBalancingTrades(AssetAwareInferenceAlgorithm, long, List, List, Clique, boolean)}.
	 * The assumptions in {@link TradeSpecification#getAssumptionIds()} are order-sensitive 
	 * (i.e. [1,2,3] is not equal to [3,2,1], although they are the same assumptions)
	 * @return
	 * @see #collapseSimilarBalancingTrades(List)
	 */
	protected List<List<TradeSpecification>> splitTradesByAssumptions( List<List<TradeSpecification>> listBeforeSplit) {
		if (listBeforeSplit == null || listBeforeSplit.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<List<TradeSpecification>> ret = new ArrayList<List<TradeSpecification>>();
		
		for (List<TradeSpecification> list : listBeforeSplit) {
			// instantiate a list which stores the indexes to split the list by using list.subList
			List<Integer> splitPoints = new ArrayList<Integer>();
			splitPoints.add(0); // by default, include the 1st element as the split point
			for (int i = 0; i < list.size() - 1; i++) {
//				if (list.get(i).getAssumptionIds().size() != list.get(i+1).getAssumptionIds().size()
//						|| !list.get(i).getAssumptionIds().containsAll(list.get(i+1).getAssumptionIds())) {
				// the above check is more complete, but we assume that assumptions are order-sensitive
				if (!list.get(i).getAssumptionIds().equals(list.get(i+1).getAssumptionIds())) {
					// dont add split point if the assumptions have same size and one contain all the other 
					// (i.e. add split point if assumptions are different)
					splitPoints.add(i+1);
				}
			}
			splitPoints.add(list.size()); // by default, include the last index (exclusive) as the split point
			for (int i = 0; i < splitPoints.size()-1; i++) {
				ret.add(list.subList(splitPoints.get(i), splitPoints.get(i+1)));
			}
		}
		
		return ret;
	}

	/**
	 * This method will determine the states of balancing trades which would minimize impact once the question is resolved.
	 * This is different from {@link #previewBalancingTrade(long, long, List, List)} in a sense that this method
	 * can return a set of trades in order to exit from a question given incomplete assumptions.
	 * Ideally this balancing trade is a set of trades where all assetsifStates states where equal so settling the question would have no effect. 
	 * <br/><br/>
	 * CAUTION: in a multi-thread environment, use {@link #doBalanceTrade(long, Date, String, long, long, List, List)} if you want to commit a trade
	 * which will balance the user's assets given assumptions, instead of using this method to calculate the balancing
	 * trade and then run {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)}.
	 * @param userID: the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be balanced.
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in assumedStates.
	 * @param assumedStates : indicates the states of the nodes in assumptionIDs.
	 * If it does not have the same size of assumptionIDs, {@link Math#min(assumptionIDs.size(), assumedStates.size())} shall be considered. 
	 * @return a list of {@link TradeSpecification} which represents the sequence of trades to be executed in order to
	 * exit from this question given assumptions.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @see #doBalanceTrade(long, Date, String, long, long, List, List)
	 */
	public List<TradeSpecification> previewBalancingTrades(long userId, long questionId, List<Long> originalAssumptionIds, 
			List<Integer> originalAssumedStates) throws IllegalArgumentException {
		// value to return
		List<TradeSpecification> balancingTrades =  new ArrayList<TradeSpecification>();
		
		// if this user was not initialized, then user did not trade at all. In such case, there is no balancing trade
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			if (isToLazyInitializeUsers() && !getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
				return Collections.emptyList();	// return empty list.
			}
		}
		
		// algorithm to be used to extract data related to assets and probabilities
		AssetAwareInferenceAlgorithm algorithm = null;
		try {
			// we need to propagate eventually, so we need clones
			algorithm = (AssetAwareInferenceAlgorithm) getAlgorithmAndAssetNetFromUserID(userId).clone();
		} catch (Exception e) {
			throw new RuntimeException("Could not obtain algorithm for the user " + userId, e);
		}
		// obtain the cliques which will be affected by this balance operation
		Collection<Clique> cliques = null;
		if (isToForceBalanceQuestionEntirely()) {
			cliques = getAssetCliqueFromQuestionIDAndAssumptions(questionId, null, algorithm);
		} else {
			cliques = getAssetCliqueFromQuestionIDAndAssumptions(questionId, originalAssumptionIds, algorithm);
		}
		for (Clique clique : cliques) {
			// these will be the specification of balancing trades of the current clique.
			List<TradeSpecification> balancingTradesForCurrentClique = null;
			// obtain trade values for exiting the user from a question given assumptions, for the current clique
			if (isToForceBalanceQuestionEntirely()) {
				balancingTradesForCurrentClique = previewBalancingTrades(algorithm, questionId, null, null, clique, isToAllowNegativeInBalanceTrade());
			} else {
				balancingTradesForCurrentClique = previewBalancingTrades(algorithm, questionId, originalAssumptionIds, originalAssumedStates, clique, isToAllowNegativeInBalanceTrade());
			}
			if (!balancingTradesForCurrentClique.isEmpty()) {
				// don't forget to include the current balancing trades into the global set of balancing trades
				balancingTrades.addAll(balancingTradesForCurrentClique);
				// we need to execute the current balancing trades now, because it will influence the balancing trades of next clique.
				for (TradeSpecification tradeSpecification : balancingTradesForCurrentClique ) {
					// check if we should do clique-sensitive operation
					if (tradeSpecification instanceof CliqueSensitiveTradeSpecification) {
						CliqueSensitiveTradeSpecification cliqueSensitiveTradeSpecification = (CliqueSensitiveTradeSpecification) tradeSpecification;
						// extract the clique from tradeSpecification. Note: this is supposedly an asset clique
						if (cliqueSensitiveTradeSpecification.getClique() != null) {
							// fortunately, the algorithm doesn't care if it is an asset or prob clique, so privide the asset clique to algorithm
							algorithm.getEditCliques().add(cliqueSensitiveTradeSpecification.getClique());
							// by forcing the algorithm to update only this clique, we are forcing the balance trade to balance the provided clique
						}
					}
					
					// do trade. Since algorithm is linked to actual networks, changes will affect the actual networks
					executeTrade(
							tradeSpecification.getQuestionId(), 
							tradeSpecification.getOldProbabilities(),
							tradeSpecification.getProbabilities(), 
							tradeSpecification.getAssumptionIds(), 
							tradeSpecification.getAssumedStates(),
							true, algorithm, true, false, null
					);
				}
			}
		}
		return balancingTrades;
	}
	
	/**
	 * Obtains all asset cliques containing questionId and originalAssumptionIds simultaneously
	 * @param questionId
	 * @param originalAssumptionIds
	 * @param userAssetAlgorithm
	 * @return the set of asset cliques
	 */
	protected Collection<Clique> getAssetCliqueFromQuestionIDAndAssumptions(long questionId,  List<Long> originalAssumptionIds, AssetAwareInferenceAlgorithm userAssetAlgorithm) {
		Collection<Clique> cliques = null;
		synchronized (userAssetAlgorithm) {
			// generate a list of nodes containing questionId and originalAssumptionIds
			Collection<INode> nodes = new ArrayList<INode>();
			// node identified by questionId
			Node mainNode = userAssetAlgorithm.getAssetNetwork().getNode(Long.toString(questionId));
			if (mainNode == null) {
				throw new IllegalArgumentException("Node " + questionId + " does not exist.");
			}
			nodes.add(mainNode);
			if (originalAssumptionIds != null) {
				for (Long assumptionId : originalAssumptionIds) {
					Node node = userAssetAlgorithm.getAssetNetwork().getNode(Long.toString(assumptionId));
					if (node == null) {
						throw new IllegalArgumentException("Node " + assumptionId + " does not exist.");
					}
					nodes.add(node);
				}
			}
			if (getConditionalProbabilityExtractor() instanceof InCliqueConditionalProbabilityExtractor) {
				cliques = ((InCliqueConditionalProbabilityExtractor)getConditionalProbabilityExtractor()).getCliquesContainingAllNodes(getProbabilisticNetwork(), nodes, Integer.MAX_VALUE);
			} else {
				cliques = ((InCliqueConditionalProbabilityExtractor)InCliqueConditionalProbabilityExtractor.newInstance()).getCliquesContainingAllNodes(getProbabilisticNetwork(), nodes, Integer.MAX_VALUE);
			}
		}
		if (cliques == null || cliques.isEmpty()) {
			throw new IllegalArgumentException("Could not find clique containing nodes: " + questionId + " and assumptions " + originalAssumptionIds);
		}
		return cliques;
	}

	/**
	 * The balancing trade with a complete set of assumptions can be obtained by calculating the probabilities 
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
	public List<Float> previewBalancingTrade(long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		
		// if user was not initialized, then we do not need to balance (i.e. return a trade which does not change probability)
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			if (isToLazyInitializeUsers() && !getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
				return this.getProbList(questionId, assumptionIds, assumedStates);
			}
		}
		
		AssetAwareInferenceAlgorithm algorithm;
		try {
			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
		} catch (InvalidParentException e) {
			throw new RuntimeException(e);
		}
		
		return this.previewBalancingTrade(algorithm, questionId, assumptionIds, assumedStates, null, null, isToAllowNegativeInBalanceTrade());
	}
	
	/**
	 * Performs the same as described in {@link MarkovEngineInterface#previewBalancingTrade(long, long, List, List)},
	 * but we can specify which clique to consider.
	 * @param algorithm: the algorithm with the asset net of the user
	 * @param questionId
	 * @param assumptionIds
	 * @param assumedStates
	 * @param clique : if set to null, then the clique containing all nodes will be picked from the junction tree
	 * @param cptOfQuestionGivenAssumptionsObtainedFromClique: table extracted from clique containing the
	 * clique potentials of questionId given assumptionIds at states assumedStates
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected List<Float> previewBalancingTrade(AssetAwareInferenceAlgorithm algorithm, long questionId, 
			List<Long> assumptionIds, List<Integer> assumedStates, 
			Clique clique, PotentialTable cptOfQuestionGivenAssumptionsObtainedFromClique, boolean isToAllowNegative) throws IllegalArgumentException {
		if (assumptionIds != null && assumedStates != null && assumptionIds.size() != assumedStates.size()) {
			throw new IllegalArgumentException("This method does not allow assumptionIds and assumedStates with different sizes.");
		}
		
		if (assumptionIds != null && !assumptionIds.isEmpty() && this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
			if (this.isToThrowExceptionOnInvalidAssumptions()) {
				new InvalidAssumptionException(assumptionIds + " are invalid assumptions for question " + questionId);
			}
			// convert the set of assumptions to a valid set
			List<Long> oldAssumptionIds = assumptionIds;
			// note: getMaximumValidAssumptionsSublists will always return at least 1 element (at least the empty list)
			assumptionIds = this.getMaximumValidAssumptionsSublists(questionId, assumptionIds, 1).get(0);
			// change assumedStates accordingly to new assumptionIds
			assumedStates = this.convertAssumedStates(assumptionIds, oldAssumptionIds, assumedStates);

		}
		
		// extract user's asset network from user ID
//		AssetAwareInferenceAlgorithm algorithm = null;
//		try {
//			algorithm = this.getAlgorithmAndAssetNetFromUserID(userId);
//		} catch (InvalidParentException e) {
//			throw new RuntimeException("Could not extract delta from user " + userId + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
//		}
		if (algorithm == null) {
//			throw new RuntimeException("Could not extract delta from user " + userId + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			throw new RuntimeException("The provided algorithm to a trade in question " + questionId 
					+ " given " + assumptionIds + " = " + assumedStates 
					+ " to clique " + clique
					+ (isToAllowNegative?"":" not") 
					+ " allowing negative assets is null. You may be using old or incompatible version of Markov Engine or UnBBayes.");
		}
		// list to be used to store list of probabilities temporary
		List<Float> probList = this.getProbList(questionId, assumptionIds, assumedStates, true, algorithm.getRelatedProbabilisticNetwork());
		if (probList == null) {
			throw new RuntimeException("Could not obtain probability of question " + questionId + ", with assumptions = " + assumptionIds + ", states = " + assumedStates);
		}
		if (probList.contains(0f)) {
			throw new IllegalArgumentException("Attempted to balance question " + questionId + " given assumptions " + assumptionIds
					+ ". At least one of the questions was already resolved.");
		}
		
		// obtain q1, q1, ... , qn (the asset's q values)
		@SuppressWarnings("rawtypes")
		List qValues = this.getAssetsIfStates(questionId, assumptionIds, assumedStates, algorithm, true, clique, cptOfQuestionGivenAssumptionsObtainedFromClique);	// true := return q-values instead of asset
		
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
			throw new ZeroAssetsException("Overflow detected when calculating the balancing trade of user " 
					+ algorithm.getAssetNetwork().getName() + " on question " + questionId);
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
		
		// check that this trade may cause the cash to go negative
//		if (!isToAllowNegative) {
//			// for each entry in probList, 
//			for (int state = 0; state < probList.size(); state++) {
//				// the name of the asset net is supposedly the ID of the user
//				List<Float> editLimits = getEditLimits(Long.parseLong(algorithm.getAssetNetwork().getName()), questionId, state, assumptionIds, assumedStates);
//				if (probList.get(state) < editLimits.get(0) || editLimits.get(1) < probList.get(state)) {
//					// TODO change this probability automatically in order to fit into the edit limit
//					throw new ZeroAssetsException("Setting P("+questionId+"|"+assumptionIds+"="+assumedStates+") = " 
//							+ probList + " may result in negative assets. The new probability at state "+state+" is not within edit limit " 
//							+ editLimits + ".");
//				}
//			}
//		}
		
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
		private static final long serialVersionUID = -2802963614618187679L;
		private List<TradeSpecification> executedTrades = null;
		/** Default constructor initializing fields\ */
		public BalanceTradeNetworkAction(long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) {
			super(transactionKey, occurredWhen, tradeKey, userId, questionId, null, null, assumptionIds, assumedStates, true);
		}
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			this.execute(true);
			try {
				Debug.println(getClass(), "Executed balance, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		/** Virtually does {@link MarkovEngineImpl#previewBalancingTrade(long, long, List, List)} and then {@link AddTradeNetworkAction#execute()}.
		 * it calls super's {@link #execute(boolean, false)} 
		 * @see #execute(boolean, boolean)*/
		public void execute(boolean isToUpdateAssets) {
			// if ME is set not to re-calculate the balancing trades again, and if this action was executed previously, we shall re-run getExecutedTrades(), 
			if (!isToRecalculateBalancingTradeOnRebuild() && this.getWhenExecutedFirstTimeMillis() > 0) {
				// re-execute the same trades if getExecutedTrades() will contain something
				if (getExecutedTrades() != null && !getExecutedTrades().isEmpty()) {
					// backup original trade specification, because we will change it several times
					TradeSpecification backup = this.getTradeSpecification();
					try {
						// re-execute old trades
						for (TradeSpecification tradeSpecification : getExecutedTrades() ) {
							// execute the trade without releasing lock
							this.setTradeSpecification(tradeSpecification);
							super.execute(isToUpdateAssets);
						}
					} catch (Exception e) {
						// restore original trade specification
						this.setTradeSpecification(backup);
						throw new RuntimeException(e);
					}
					this.setTradeSpecification(backup);
				}
				return;
			}
			// store the old marginals now and call setOldValues(oldValues) later, because a call to super will overwrite this value
			List<Float> oldMarginal = null;
			if (this.getWhenExecutedFirstTime() == null) {
				oldMarginal = getProbList(getQuestionId(), null, null);
			}
			
			// prepare the list of trades actually executed by this balance operation
			setExecutedTrades(new ArrayList<TradeSpecification>());
			synchronized (getProbabilisticNetwork()) {
				// backup original trade specification, because we will change it several times
				TradeSpecification backup = this.getTradeSpecification();
				
				// algorithm to be used to extract data related to assets and probabilities
				AssetAwareInferenceAlgorithm algorithm = null;
				try {
					algorithm = getAlgorithmAndAssetNetFromUserID(getUserId());
				} catch (InvalidParentException e) {
					throw new RuntimeException("Could not obtain algorithm for the user " + getUserId(), e);
				}
				// obtain the cliques which will be affected by this balance operation
				Collection<Clique> cliques = null;
				if (isToForceBalanceQuestionEntirely()) {
					cliques = getAssetCliqueFromQuestionIDAndAssumptions(getQuestionId(), null, algorithm);
				} else {
					cliques = getAssetCliqueFromQuestionIDAndAssumptions(getQuestionId(), getAssumptionIds(), algorithm);
				}
				for (Clique clique : cliques) {
					// obtain trade values for exiting the user from a question given assumptions, for the current clique
					List<TradeSpecification> balancingTrades = null;
					if (isToForceBalanceQuestionEntirely()) {
						balancingTrades = previewBalancingTrades(algorithm, getQuestionId(), null, null, clique, isToAllowNegativeInBalanceTrade());
					} else {
						balancingTrades = previewBalancingTrades(algorithm,
								// preview shall use original (backup) configurations for questions/assumptions, instead of this.getAssumptionIds() and this.getAssumedStates(),
								// because setTradeSpecification(tradeSpecification) is called in next block, so this.getAssumptionIds() and this.getAssumedStates() is referencing the clique in previuous loop
								backup.getQuestionId(), backup.getAssumptionIds(), backup.getAssumedStates(), 
								clique, isToAllowNegativeInBalanceTrade());
					}
					try {
						for (TradeSpecification tradeSpecification : balancingTrades ) {
							// execute the trade without releasing lock
							this.setTradeSpecification(tradeSpecification);
							super.execute(isToUpdateAssets);
							// mark this trade as executed
							if (isToKeepCliquesOfBalanceTradeInMemory()) {
								// if tradeSpecification instanceof CliqueSensitiveTradeSpecification, then the clique will be kept in memory.
								// if not, then tradeSpecification will not contain cliques anyway, so add to getExecutedTrades().
								getExecutedTrades().add(tradeSpecification);
							} else if (tradeSpecification instanceof CliqueSensitiveTradeSpecification) {
								// if we should not keep clique in history, and tradeSpecification instanceof CliqueSensitiveTradeSpecification,
								// then we need to discard it and substitute to a specification which does not store clique
								getExecutedTrades().add(
										new TradeSpecificationImpl(
												tradeSpecification.getUserId(), 
												tradeSpecification.getQuestionId(), 
												tradeSpecification.getProbabilities(), 
												tradeSpecification.getAssumptionIds(), 
												tradeSpecification.getAssumedStates()
										)
								);
								// let the garbage collector discard the tradeSpecification
							}
						}
					} catch (Exception e) {
						// restore original trade specification
						this.setTradeSpecification(backup);
						throw new RuntimeException(e);
					}
				}
				
				// restore trade specification, because it is probably containing the ones returned by the preview
				this.setTradeSpecification(backup);
				
				if (this.getWhenExecutedFirstTime() == null) {
					// overwrite the old marginals, because a call to super has changed this value.
					this.setOldValues(oldMarginal);
					// by default, set the probabilities of this action to the marginal after trade
					this.setNewValues(getProbList(getQuestionId(), null, null));
					// the above code will also do the following
//					backup.setProbabilities(getProbList(getQuestionId(), null, null));
				}
			}
		}
		public void revert() throws UnsupportedOperationException {
			super.revert();
		}
		/** @param executedTrades the trades actually executed by this balance operation */
		public void setExecutedTrades(List<TradeSpecification> executedTrades) { this.executedTrades = executedTrades; }
		/** @return the trades actually executed by this balance operation */
		public List<TradeSpecification> getExecutedTrades() { return executedTrades; }
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
			if (assumptionIds != null && !assumptionIds.isEmpty() && this.getPossibleQuestionAssumptions(questionId, assumptionIds).isEmpty()) {
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
//			List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey); // getNetworkActionsMap() is supposedly a concurrent map
//			synchronized (actions) {	// actions is not a concurrent list, so must lock it
//				for (NetworkAction action : actions) {
//					if ((action instanceof AddQuestionNetworkAction) && (e.getQuestionId().equals(action.getQuestionId()))) {
//						// this action will create the question which was not found.
//						isNodeToBeCreatedWithinTransaction = true;
//						break;
//					}
//				}
//			}
			// obtain the mapping of what questions are being created in transactions
			Map<Long, Set<Long>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
			
			
			if (mapTransactionQuestions == null || transactionKey == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				throw e;
			}
			
			// use the mapping in order to check whether the parent will be created in the same transaction
			synchronized (mapTransactionQuestions) {
				Set<Long> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
				if (questionsInSameTransaction == null) {
					throw e;
				}
				// search for the assumption ID in questionsToBeCreatedInTransaction
				for (Long idInMapping : questionsInSameTransaction) {
					if (idInMapping.equals(e.getQuestionId())) {
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
//			List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey);
//			if (actions == null) {
//				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
//				// actions cannot be null, because we checked this condition before
//				throw new IllegalStateException("Desync detected in transaction " + transactionKey + ": the transaction was deleted while doBalanceTrade was in execution.");
//			}
//			synchronized (actions) {
//				for (NetworkAction networkAction : actions) {
//					if (networkAction instanceof AddQuestionNetworkAction) {
//						AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
//						if (addQuestionNetworkAction.getQuestionId()!= null && addQuestionNetworkAction.getQuestionId().longValue() == questionId) {
//							willCreateNodeOnCommit = true;
//							break;
//						}
//					}
//				}
//			}
			// obtain the mapping of what questions are being created in transactions
			Map<Long, Set<Long>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
			
			
			if (mapTransactionQuestions == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
			}
			
			// use the mapping in order to check whether the parent will be created in the same transaction
			synchronized (mapTransactionQuestions) {
				Set<Long> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
				if (questionsInSameTransaction == null) {
					throw new InexistingQuestionException("Question ID " + questionId + " does not exist and is not specified in transaction : " + transactionKey, questionId);
				}
				// search for the assumption ID in questionsToBeCreatedInTransaction
				for (Long idInMapping : questionsInSameTransaction) {
					if (idInMapping.longValue() == questionId) {
						willCreateNodeOnCommit = true;
						break;
					}
				}
			}
			if (!willCreateNodeOnCommit) {	
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
//					List<NetworkAction> actions = this.getNetworkActionsMap().get(transactionKey); 
					
					
					// obtain the mapping of what questions are being created in transactions
					Map<Long, Set<Long>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
					
					
					if (mapTransactionQuestions == null) {
						// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
						throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
					}
					
					// use the mapping in order to check whether the parent will be created in the same transaction
					synchronized (mapTransactionQuestions) {
						Set<Long> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
						if (questionsInSameTransaction == null) {
							throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist and is not specified in transaction : " + transactionKey, assumptiveQuestionId);
						}
						// search for the assumption ID in questionsToBeCreatedInTransaction
						for (Long idInMapping : questionsInSameTransaction) {
							if (idInMapping.equals(assumptiveQuestionId)) {
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
			}
		}
		
		// do nothing if user was not initialized or user is lazily initialized (in both cases, user did neveer trade, so balancing is useless)
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			if (!getUserToAssetAwareAlgorithmMap().containsKey(userId)) {
				if (transactionKey == null) {
					return true;
				} else if (!isToAddUninitializedUserInBalanceTradeOfHugeTransaction) { // trades may be performed in same transaction, so user may become initialized within transaction
					// extract the actions pertaining in the same transaction
					List<NetworkAction> actionsInSameTransaction = getNetworkActionsMap().get(transactionKey); 
					// TODO change this portion in order to use some index
					if (actionsInSameTransaction == null) {
						throw new IllegalArgumentException("Transaction with key " + transactionKey + " was not initialized or was already commited.");
					}
					// this variable will become true if the user has a trade before this balancing trade.
					boolean hasUserTradeBeforeBalance = false;
					// handle actions in the same transaction until we find the 1st transaction to be executed strictly after this balancing trade
					for (NetworkAction action : actionsInSameTransaction) {
						if (action == null || !(action instanceof AddTradeNetworkAction)){
							// only consider trades. Ignore other types of actions.
							// this is important, because there's no guarantee that other types of actions are sorted by action.getWhenCreated()
							continue;
						}
						if (action.getWhenCreated().after(occurredWhen)) {
							// do not consider trades performed after the current balancing command
							// Note: we are assuming that trades are sorted by action.getWhenCreated().
							break;	
						}
						if (action.getUserId() != null && action.getUserId().longValue() == userId) {
							// found a trade of user "userId", so we may need a balancing trade.
							hasUserTradeBeforeBalance = true;
							break;
						}
					}
					if (!hasUserTradeBeforeBalance) {
						// There was no trade, so no balancing trade is necessary.
						return true;	// just return and do nothing
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

	/**
	 * If no assumptions are provided (assumptionIds == null or empty), then this method
	 * uses {@link #getNetworkActionsIndexedByQuestions()} to retrieve the trade history.
	 * If {@link #isToRetriveOnlyTradeHistory()} is false, {@link AddCashNetworkAction},
	 * {@link AddQuestionAssumptionNetworkAction} and {@link AddQuestionNetworkAction} will
	 * also be retrieved.
	 * <br/><br/>
	 * If assumptionIds != null, then it will use {@link #getLastNCliquePotentialMap()}
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
	 */
	public List<QuestionEvent> getQuestionHistory(Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		
		// change the size of assumptionIds and assumedStates to the minimum of the two lists
		if (assumptionIds != null) {
			if (assumedStates == null) {
				assumptionIds = null;
			} else if (assumptionIds.size() != assumedStates.size()) {
				if (assumptionIds.size() < assumedStates.size()) {
					// delete last few elements from assumedStates
					int diff = assumedStates.size() - assumptionIds.size();
					for (int i = 0; i < diff ; i++) {
						assumedStates.remove(assumedStates.size()-1);
					}
				} else {
					// delete last few elements from assumptionIds
					int diff = assumptionIds.size() - assumedStates.size();
					for (int i = 0; i < diff ; i++) {
						assumptionIds.remove(assumptionIds.size()-1);
					}
				}
			}
		} 
		
		// at this point, assumptionIds == null || assumptionIds.size() == assumedStates.size()
		
		// never return the object itself (because we do not want the caller to change content of the mappings)
		List<QuestionEvent> ret = new ArrayList<QuestionEvent>(); // the list to be returned
		if (questionId == null) { // We have to retrieve history of actions unrelated to any question.
			// Supposedly, trades are always related to some question, so we do not need to retrieve trades
			if (!isToRetriveOnlyTradeHistory()) {
				synchronized (this.getNetworkActionsIndexedByQuestions()) {
					ret.addAll(this.getNetworkActionsIndexedByQuestions().get(null));
				}
			}
		} else if (assumptionIds == null || assumptionIds.isEmpty()) {
			// History of marginals. Retrieve from the network actions indexed by question ID
			List<NetworkAction> list = null;
			synchronized (this.getNetworkActionsIndexedByQuestions()) {
				list = this.getNetworkActionsIndexedByQuestions().get(questionId);
			}
			if (list == null) {
				return Collections.emptyList();
			}
			// at this point, list != null
			synchronized (list) {
				if (list.isEmpty()) {
					// never return the object itself (because we do not want the caller to change content of getNetworkActionsIndexedByQuestions())
					return Collections.emptyList();	
				}
				// fill ret with values in list, but do basic filtering
				for (NetworkAction action : list) {
					if (isToRetriveOnlyTradeHistory() 
							&& (!(action instanceof AddTradeNetworkAction) ) 
							|| (action.getWhenExecutedFirstTime() == null)) {	// do not consider actions which were not executed
						// do not consider AddQuestionNetworkAction if engine is configured to ignore them
						continue;
					}
					ret.add(action);
				}
			}
		} else {// retrieve from the last N changes in the clique, with N = getMaxConditionalProbHistorySize()
			// obtain the extractor of cliques containing all nodes
			InCliqueConditionalProbabilityExtractor conditionalProbabilityExtractor = null;
			try {
				conditionalProbabilityExtractor = (InCliqueConditionalProbabilityExtractor) getConditionalProbabilityExtractor();
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage()+ " : cannot use current conditional probability extractor to obtain cliques." , e);
			}
			if (conditionalProbabilityExtractor == null) {
				Debug.println(getClass(), "Could not extract conditional probability extractor for cliques. Will attempt to use default.");
				conditionalProbabilityExtractor = (InCliqueConditionalProbabilityExtractor) AssetAwareInferenceAlgorithm.DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR.getConditionalProbabilityExtractor();
			}
			
			ProbabilisticNode mainNode = null;	// node of questionId
			Clique clique = null;	// clique w/ mainNode and all assumed nodes
			Map<INode, Integer> assumptions = new HashMap<INode, Integer>();	// encodes the assumptionIds and assumedStates
			// list of nodes containing questionId and assumptionIds
			List<INode> nodes = new ArrayList<INode>();
			synchronized (getProbabilisticNetwork()) {
				mainNode = (ProbabilisticNode) getProbabilisticNetwork().getNode(Long.toString(questionId));
				if (mainNode == null) {
					Debug.println("Could not find question "  + questionId);
					return ret;
				}
				for (Long assumptionId : assumptionIds) {
					INode assumptionNode = getProbabilisticNetwork().getNode(Long.toString(assumptionId));
					if (assumptionNode == null) {
						Debug.println("Could not find question "  + assumptionId);
						return ret;
					}
					nodes.add(assumptionNode);
					
					// also update the mapping from node to assumed state
					assumptions.put(assumptionNode, assumedStates.get(assumptionIds.indexOf(assumptionId)));
				}
				if (nodes.contains(mainNode)) {
					Debug.println("Could not retrieve history of trades of question + " + questionId
							+" with assumptions " + assumptionIds + ", because the assumptions contains the question itself.");
					return ret;
				}
				nodes.add(mainNode);
				// extract the clique containing all nodes
				clique = conditionalProbabilityExtractor.getCliqueContainingAllNodes(getProbabilisticNetwork(), nodes );
			}
			if (clique == null) {
				Debug.println("Could not retrieve clique containing question "+ questionId + " and assumptions " + assumptionIds);
				return ret;
			}
			// at this point, clique != null
			
			// obtain the last N changes in the clique potential, with N = getMaxConditionalProbHistorySize()
			List<ParentActionPotentialTablePair> cliquePotentialHistory = getLastNCliquePotentialMap().get(clique);
			
			if (cliquePotentialHistory != null) {
				// iterate over the N changes, obtain the conditional probabilities, and fill ret with instances of QuestionEvent.
				
				List<Float> oldMarginal = null;
				for (ParentActionPotentialTablePair tradeAndCliqueTable : cliquePotentialHistory) {
					// prepare the list of marginal probability
					List<Float> newMarginal = null;
					synchronized (getDefaultInferenceAlgorithm()) {
						// obtain the marginal probability using the clique's potential table at that moment, given assumptions
						newMarginal = getDefaultInferenceAlgorithm().getMarginalFromPotentialTable(mainNode, tradeAndCliqueTable.getTable(), assumptions );
					}
					if (newMarginal != null) {
						// do not add entry if oldMarginal and newMarginal have no diferences
						boolean hasDifference = (oldMarginal == null);
						if (oldMarginal != null) {
							if ( newMarginal.size() == oldMarginal.size()) {
								for (int i = 0; i < newMarginal.size(); i++) {
									if (Math.abs(newMarginal.get(i) - oldMarginal.get(i)) > getProbabilityErrorMargin()) {
										hasDifference = true;
										break;
									}
								}
							} else {
								Debug.println("New marginal is " + newMarginal + ", and old marginal is " + oldMarginal);
							}
						} 
						if (hasDifference) {
							// fill ret with a proper instance of QuestionEvent
							if (tradeAndCliqueTable.getParentAction() instanceof ResolveQuestionNetworkAction) {
								// for some reason, this type detection is not working automatically sometimes... So, I added this explicit if-then-else
								ret.add(new DummyTradeAction(((ResolveQuestionNetworkAction)tradeAndCliqueTable.getParentAction()), oldMarginal, newMarginal, tradeAndCliqueTable.getParentAction().getSettledState()));
							} else {
								ret.add(new DummyTradeAction(tradeAndCliqueTable.getParentAction(), questionId, oldMarginal, newMarginal));
							}
							// oldMarginal of the next iteration is the current newMarginal
							oldMarginal = newMarginal;
						}
					} else {
						Debug.println(getClass(), "Could not extract marginal of question " + questionId + ", assuming " + assumptionIds + " = " + assumedStates 
								+ ", from entry " + cliquePotentialHistory.indexOf(tradeAndCliqueTable) + " of the history of conditional probabilities.");
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
		
		// fill data related to cash before resolving questions. Filter it by questionId if necessary
		Map<Long, Float> filteredCashBefore = Collections.EMPTY_MAP; 
		
		// extract map representing cash of this user before resolving questions
		synchronized (getUserIdToResolvedQuestionCashBeforeMap()) {
			Map<Long, Float> cashBeforeResolvedQuestion = getUserIdToResolvedQuestionCashBeforeMap().get(userId);
			if (cashBeforeResolvedQuestion != null) {
				if (questionId != null) {
					// it's filtered by question ID, so return only this entry
					Float value = cashBeforeResolvedQuestion.get(questionId);
					if (value != null) {
						filteredCashBefore = Collections.singletonMap(questionId, value);
					}
				} else {// not filtered by question ID, so return all available entries
					// use a copy, because we do not want to change the original
					filteredCashBefore = new HashMap<Long, Float>(cashBeforeResolvedQuestion);
				}
			}
		}
		// prepare a final variable to be used in order to pass it to the anonymous subclass of ScoreSummary
		final Map<Long, Float> cashBefore = filteredCashBefore;
		
		// fill data related to gains in cash per resolved question. Filter cashContributionPerResolvedQuestion by questionId if necessary
		Map<Long, Float> filteredContribution = Collections.EMPTY_MAP; 
		// extract map representing gains of this user by resolved question
		synchronized (getUserIdToResolvedQuestionCashGainMap()) {
			Map<Long, Float> cashContributionPerResolvedQuestion = getUserIdToResolvedQuestionCashGainMap().get(userId);
			if (cashContributionPerResolvedQuestion != null) {
				if (questionId != null) {
					// it's filtered by question ID, so return only this entry
					Float value = cashContributionPerResolvedQuestion.get(questionId);
					if (value != null) {
						filteredContribution = Collections.singletonMap(questionId, value);
					}
				} else {// not filtered by question ID, so return all available entries
					// use a copy, because we do not want to change the original
					filteredContribution = new HashMap<Long, Float>(cashContributionPerResolvedQuestion);
				}
			}
			// note: cash of uninitialized users supposedly did not change, so we do not include them.
		}
		// prepare a final variable to be used in order to pass it to the anonymous subclass of ScoreSummary
		final Map<Long, Float> gain = filteredContribution;
		
		// integrate all data into one ScoreSummary and return.
		return new ScoreSummary() {
			private static final long serialVersionUID = -6182458469603900246L;
			public float getCash() { return cash; }
			public float getScoreEV() {return scoreEV; }
			public List<SummaryContribution> getScoreComponents() { return scoreEVPerStateList; }
			public List<SummaryContribution> getIntersectionScoreComponents() { return Collections.emptyList(); }
			public Map<Long, Float> getCashContributionPerResolvedQuestion() {return gain; }
			public Map<Long, Float> getCashBeforeResolvedQuestion() { return cashBefore; }
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
//		final Node dummyNode = new Node() {
//			private static final long serialVersionUID = 2328320084149877840L;
//			public int getType() { return 0; }
//			/** Object#equals() will call this method */
//			public String getName() {return ""+questionId; }
//		};
		
		// prepare listener to be notified when cells of asset tables and prob tables are multiplied
		ExpectedAssetCellMultiplicationListener listener = new ExpectedAssetCellMultiplicationListener() {
			
			private Map<IRandomVariable, List<Long>> questionsCache = new HashMap<IRandomVariable, List<Long>>();
			
			/** Fill content of cliqueComponents or sepComponents */
			public void onModification(IRandomVariable probCliqueOrSep, IRandomVariable assetCliqueOrSep, int indexInProbTable, int indexInAssetTable, double offeredValue) {
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
				
				// if we are adding a component related to a separator, its value contributes negatively
				final float value = (float) (((probCliqueOrSep instanceof Separator))?-offeredValue:offeredValue);	// this value will be used to fill SummaryContribution
				
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
					public float getContributionToScoreEV() { return value; }
				});
				
			}	// end of inner method
		};	// end of anonymous inner class
		
		// obtain the conditional expected score, passing the listener as argument, so that cliqueComponents and sepComponents are filled properly
		final float scoreEV = this.scoreUserEv(userId, assumptionIds, assumedStates, Collections.singletonList(listener));
		
		
		// fill data related to cash before resolving questions. Filter it by questionId if necessary
		Map<Long, Float> filteredCashBefore = Collections.EMPTY_MAP; 
		
		// extract map representing cash of this user before resolving questions
		synchronized (getUserIdToResolvedQuestionCashBeforeMap()) {
			Map<Long, Float> cashBeforeResolvedQuestion = getUserIdToResolvedQuestionCashBeforeMap().get(userId);
			if (cashBeforeResolvedQuestion != null) {
				if (questionId != null) {
					// it's filtered by question ID, so return only this entry
					Float value = cashBeforeResolvedQuestion.get(questionId);
					if (value != null) {
						filteredCashBefore = Collections.singletonMap(questionId, value);
					}
				} else {// not filtered by question ID, so return all available entries
					// use a copy, because we do not want to change the original
					filteredCashBefore = new HashMap<Long, Float>(cashBeforeResolvedQuestion);
				}
			}
		}
		// prepare a final variable to be used in order to pass it to the anonymous subclass of ScoreSummary
		final Map<Long, Float> cashBefore = filteredCashBefore;
		
		// fill data related to gains in cash per resolved question. Filter cashContributionPerResolvedQuestion by questionId if necessary
		Map<Long, Float> filteredContribution = Collections.EMPTY_MAP; 
		// extract map representing gains of this user by resolved question
		synchronized (getUserIdToResolvedQuestionCashGainMap()) {
			Map<Long, Float> cashContributionPerResolvedQuestion = getUserIdToResolvedQuestionCashGainMap().get(userId);
			if (cashContributionPerResolvedQuestion != null) {
				if (questionId != null) {
					// it's filtered by question ID, so return only this entry
					Float value = cashContributionPerResolvedQuestion.get(questionId);
					if (value != null) {
						filteredContribution = Collections.singletonMap(questionId, value);
					}
				} else {// not filtered by question ID, so return all available entries
					// use a copy, because we do not want to change the original
					filteredContribution = new HashMap<Long, Float>(cashContributionPerResolvedQuestion);
				}
			}
			// note: cash of uninitialized users supposedly did not change, so we do not include them.
		}
		// prepare a final variable to be used in order to pass it to the anonymous subclass of ScoreSummary
		final Map<Long, Float> gain = filteredContribution;
		
		// integrate all data into one ScoreSummary and return.
		return new ScoreSummary() {
			private static final long serialVersionUID = -7912423075696534526L;
			public float getCash() { return cash; }
			public float getScoreEV() {return scoreEV; }
			public List<SummaryContribution> getScoreComponents() { return cliqueComponents; }
			public List<SummaryContribution> getIntersectionScoreComponents() { return sepComponents; }
			public Map<Long, Float> getCashContributionPerResolvedQuestion() { return gain;}
			public Map<Long, Float> getCashBeforeResolvedQuestion() { return cashBefore; }
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
		for (SummaryContribution contribution : summary.getIntersectionScoreComponents()) {
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
	 * This value is also used in {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)}
	 * in order to check whether the old probability specified in the argument differs from the probability before the trade
	 * (if so, the house account may add a trade for correction).
	 * @param probabilityErrorMargin the probabilityErrorMargin to set
	 */
	public void setProbabilityErrorMargin(float probabilityErrorMargin) {
		this.probabilityErrorMargin = probabilityErrorMargin;
	}

	/**
	 * This is the error margin used when comparing two probability values.
	 * If ((prob1 - probabilityErrorMargin) < prob2) && (prob2 < (prob1 + probabilityErrorMargin)), then prob1 == prob2.
	 * This value is also used in {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)}
	 * in order to check whether the old probability specified in the argument differs from the probability before the trade
	 * (if so, the house account may add a trade for correction).
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
//				JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(getProbabilisticNetwork());
//				// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
//				junctionTreeAlgorithm.setLikelihoodExtractor(AssetAwareInferenceAlgorithm.DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR);
				// the above code was substituted by the following, because we want to reduce memory usage a little bit by also reusing instance of JunctionTreeAlgorithm (previously, it was only reusing the BN)
				JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator();	// currently, no need to synchronize, because this wont be changing at all
				// prepare default inference algorithm for asset network
				if (!isToLazyInitializeUsers()) {
					algorithm = getAssetAwareInferenceAlgorithmBuilder().build(junctionTreeAlgorithm, getDefaultInitialAssetTableValue());
				} else if (isToUseQValues()) {
					// getAssetsOfLazyOrUnInitializedUser contains assets, but if engine is configured to use q-values, then we must convert
					algorithm = getAssetAwareInferenceAlgorithmBuilder().build(junctionTreeAlgorithm, (float) getQValuesFromScore(getAssetsOfLazyOrUnInitializedUser(userID)));
				} else {
					algorithm = getAssetAwareInferenceAlgorithmBuilder().build(junctionTreeAlgorithm, getAssetsOfLazyOrUnInitializedUser(userID));
				}
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
				
				// if me is not configured to delete nodes on resolve, we need to resolve nodes in the new assetNet
				if (!isToDeleteResolvedNode()) {
					// convert getResolvedQuestions() to Map<INode, Integer>, so that we can call algorithm.setAsPermanentEvidence 
					Map<INode, Integer> evidences = new HashMap<INode, Integer>();
					synchronized (getResolvedQuestions()) {
						for (Long resolvedQuestionId : getResolvedQuestions().keySet()) {
							evidences.put(algorithm.getAssetNetwork().getNode(resolvedQuestionId.toString()), getResolvedQuestions().get(resolvedQuestionId).getResolvedState());
						}
					}
					algorithm.setAsPermanentEvidence(evidences, isToDeleteResolvedNode());
				}
				
				// make sure this user is not considered unititialized anymore, so that methods related to lazy loading do not consider this user
				synchronized (getUninitializedUserToAssetMap()) {
					getUninitializedUserToAssetMap().remove(userID);
				}
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
			int indexOfFirstActionCreatedAfterNewAction = actions.size()-1;	// this will point to the first action created after occurredWhen
			// Make sure the action list is ordered by the date. Insert new action to a correct position when necessary.
			for (; indexOfFirstActionCreatedAfterNewAction >= 0; indexOfFirstActionCreatedAfterNewAction--) {
				if (!actions.get(indexOfFirstActionCreatedAfterNewAction).getWhenCreated().after(newAction.getWhenCreated())) {
					// add here if the current action was not created after the new action (i.e. getWhenCreated is equal or before)
					break;
				}
			}
			
			// insert new action at the correct position (after the last occurrence of actions created before the new action to be inserted)
			actions.add(++indexOfFirstActionCreatedAfterNewAction, newAction);
			
			// insert new action into the map to be used for searching actions by question id
			this.addNetworkActionIntoQuestionMap(newAction, null);
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
					if (addQuestionNetworkAction.getQuestionId().equals(newAction.getQuestionId())) {
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
			this.addNetworkActionIntoQuestionMap(newAction, null);
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
	 * This can be interpreted as the "house" user
	 * (the user which represents the Markov Engine itself, with special priviledges).
	 */
	public AssetAwareInferenceAlgorithm getDefaultInferenceAlgorithm() {
		return defaultInferenceAlgorithm;
	}
	
	/**
	 * @param defaultInferenceAlgorithm : an instance of {@link AssetAwareInferenceAlgorithm}
	 * which is not necessary related to some user.
	 * This can be interpreted as the "house" user
	 * (the user which represents the Markov Engine itself, with special priviledges).
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
	 * @param questionId : the question id to be considered as the key.
	 * Null is allowed as the key (in this case, {@link NetworkAction#getQuestionId()} will be used as the key).
	 * @param networkAction : the object to be inserted.
	 * If {@link NetworkAction#isCorrectiveTrade()} == true, then the object will be inserted immediately before the
	 * trade being corrected {@link NetworkAction#getCorrectedTrade()}.
	 * {@link NetworkAction#getQuestionId()} will be used as the key of {@link #getNetworkActionsIndexedByQuestions()},
	 * if questionId is null. 
	 * @return the index where networkAction was inserted
	 */
	protected int addNetworkActionIntoQuestionMap(NetworkAction networkAction, Long questionId) {
		// initial assertion
		if (networkAction == null) {
			throw new NullPointerException("networkAction == null");
		}
		// get list of actions from getNetworkActionsIndexedByQuestions.
		List<NetworkAction> list = null;
		synchronized (getNetworkActionsIndexedByQuestions()) {
			if (questionId == null) {
				questionId = networkAction.getQuestionId();
			}
			list = getNetworkActionsIndexedByQuestions().get(questionId);
			if (list == null) {
				// this is the fist time an action with questionId is inserted
				list = new ArrayList<NetworkAction>();
				getNetworkActionsIndexedByQuestions().put(questionId, list);
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
			} else if (networkAction.isCorrectiveTrade() && networkAction.getCorrectedTrade() != null) {
				// if this is a corrective trade, always add it before the trade being corrected
				indexOfNewElement = list.indexOf(networkAction.getCorrectedTrade());
				if (indexOfNewElement < 0) {
					// if the trade being corrected is not present in the list, add at the end.
					indexOfNewElement = list.size();	// we shall return this value
					list.add(networkAction);
				} else {
					list.add(indexOfNewElement, networkAction);
				}
			} else {
				// NOTE: returning indexOfNewElement >= 0 indicates that the network action was added somewhere in the list
				for (indexOfNewElement = 0; indexOfNewElement < list.size(); indexOfNewElement++) {
					if ( ( list.get(indexOfNewElement) == null )
							|| ( list.get(indexOfNewElement).getWhenCreated() == null) ) { // at this point, list.get(indexOfNewElement) != null
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
	
	/**
	 * This method removes networkAction from {@link #getNetworkActionsIndexedByQuestions()}.
	 * @param questionId : the question id to be considered as the key.
	 * Null is allowed as the key (in this case, {@link NetworkAction#getQuestionId()} will be used as the key).
	 * @param networkAction : the object to be removed.
	 * {@link NetworkAction#getQuestionId()} will be used as the key of {@link #getNetworkActionsIndexedByQuestions()},
	 * if questionId is null. 
	 * @return true if {@link #getNetworkActionsIndexedByQuestions()} was changed as a result of this method 
	 */
	protected boolean removeNetworkActionFromQuestionMap(NetworkAction networkAction, Long questionId) {
		// initial assertion
		if (networkAction == null) {
			throw new NullPointerException("networkAction == null");
		}
		// get list of actions from getNetworkActionsIndexedByQuestions.
		List<NetworkAction> list = null;
		synchronized (getNetworkActionsIndexedByQuestions()) {
			if (questionId == null) {
				questionId = networkAction.getQuestionId();
			}
			list = getNetworkActionsIndexedByQuestions().get(questionId);
		}
		if (list == null || list.isEmpty()) {
			// no entry to delete
			return false;
		}
		// at this point, list != null
		boolean hasRemoved = false;
		synchronized (list) {
			hasRemoved = list.remove(networkAction);
		}
		return hasRemoved;
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

	/**
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionAssumptionGroups()
	 * @see #getQuestionAssumptionGroups(List)
	 */
	public List<List<Long>> getQuestionAssumptionGroups() {
		return this.getQuestionAssumptionGroups(null);
	}
	
	/**
	 * @return the list of indexes of {@link #getQuestionAssumptionGroups()} indicating
	 * which clique is parent of which clique.
	 * In other words, this method returns the content of the first argument (which is actually an output argument)
	 * of {@link #getQuestionAssumptionGroups(List)}. Hence, it is equivalent to calling:
	 * <br/><br/>
	 * List ret = new ArrayList();
	 * <br/>
	 * getQuestionAssumptionGroups(ret)
	 * <br/>
	 * return ret;
	 * <br/><br/>
	 * Example:
	 * <br/><br/>
	 * Suppose the original junction tree is  [1,2] <- [1,3] -> [1,4]
	 * <br/>
	 * (i.e. [1,3] is the "parent" of [1,2] and [1,4]).
	 * <br/><br/>
	 * Also, suppose that {@link #getQuestionAssumptionGroups()} returns the list [ [1,2], [1,3], [1,4] ].
	 * <br/>
	 * In this list, clique [1,2] is at position 0; [1,3] at position 1, and [1,4] at 2.
	 * <br/><br/>
	 * Then, this method will return [ 1 , -1 , 1 ].
	 * <br/><br/>
	 * It indicates that the clique at position 0 (i.e. [1,2]) has its parent at position 1 
	 * (clique [1,3]), the clique at position 1 (clique [1,3]) 
	 * has parent at position -1 (which is an invalid position - indicates
	 * that it is a root clique), and the clique at position 2 (clique [1,4])
	 * has parent at position 1 (clique [1,3]).
	 * @see #getQuestionAssumptionGroups()
	 * @see #getQuestionAssumptionGroups(List)
	 */
	public List<Integer> getParentCliqueIndexes() {
		List<Integer> ret = new ArrayList<Integer>();
		this.getQuestionAssumptionGroups(ret);
		return ret;
	}
	
	/**
	 * 
	 * @param cliqueParentOutputList : this is actually an output argument. If not null, this list
	 * will be filled with indexes of the parents of the returned cliques. 
	 * <br/><br/>
	 * Example:
	 * <br/><br/>
	 * Suppose the original junction tree is  [1,2] <- [1,3] -> [1,4]
	 * <br/>
	 * (i.e. [1,3] is the "parent" of [1,2] and [1,4]).
	 * <br/><br/>
	 * Also, suppose that this method returns the list [ [1,2], [1,3], [1,4] ].
	 * <br/>
	 * In this list, clique [1,2] is at position 0; [1,3] at position 1, and [1,4] at 2.
	 * <br/><br/>
	 * Then, the cliqueParentOutputList will be [ 1 , -1 , 1 ].
	 * <br/><br/>
	 * It indicates that the clique at position 0 (i.e. [1,2]) has its parent at position 1 
	 * (clique [1,3]), the clique at position 1 (clique [1,3]) 
	 * has parent at position -1 (which is an invalid position - indicates
	 * that it is a root clique), and the clique at position 2 (clique [1,4])
	 * has parent at position 1 (clique [1,3]).
	 * @return a list of any possible assumptions groups (likely cliques in some implementations). 
	 */
	public List<List<Long>> getQuestionAssumptionGroups(List<Integer> cliqueParentOutputList) {
		// value to be returned
		List<List<Long>> ret = new ArrayList<List<Long>>();
		
		// initial assertion
		if (getProbabilisticNetwork() == null) {
			return ret;
		}
		
		// extract cliques
		synchronized (getProbabilisticNetwork()) {
			if (getProbabilisticNetwork().getJunctionTree() == null 
					|| getProbabilisticNetwork().getJunctionTree().getCliques() == null) {
				return ret;
			}
			List<Clique> cliques = null;
			List<Clique> originalCliqueList = getProbabilisticNetwork().getJunctionTree().getCliques();
			if (originalCliqueList != null) {
				// we are using another instance, so that we can release the lock and let other threads change the clique structre as they wish
				cliques = new ArrayList<Clique>(originalCliqueList);
			}
			
			if (cliques == null) {
				return ret;
			}
			
			// a mapping from clique to its index which will be used to fill cliquePrentOutputList
			Map<Clique, Integer> cliqueIndexMap = null; 
			if (cliqueParentOutputList != null) {
				cliqueIndexMap = new HashMap<Clique, Integer>();
			}
			
			// populate list of question ids based on nodes in cliques
			int index = 0;	// index to be used in order to fill cliqueIndexMap
			for (Clique clique : cliques) {
				if (clique.getNodes().isEmpty() || clique.getProbabilityFunction().tableSize() <= 1) {
					// this clique is empty. Do not consider this
					continue;
				}
				
				// fill mapping from clique to its index. This index can be used in "ret" too, because ret and cliques are synchronized by index.
				if (cliqueParentOutputList != null) {
					cliqueIndexMap.put(clique, index);
					index++;
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
			
			// at this point, "ret" is filled, and its ordering is the same of the ordering in "cliques", so their indexes are the same
			
			// use the cliqueIndexMap to fill cliquePrentOutputList
			if (cliqueParentOutputList != null && cliqueIndexMap != null && !cliqueIndexMap.isEmpty()) {
				for (Clique clique : cliques) {
					if (clique.getNodes().isEmpty() || clique.getProbabilityFunction().tableSize() <= 1) {
						// ret is not considering this clique, so cliquePrentOutputList should not
						continue;
					}
					Clique parent = clique.getParent();
					if (parent == null) {
						cliqueParentOutputList.add(-1); // negative means no parent clique
					} else {
						Integer cliqueIndex = cliqueIndexMap.get(parent);
						if (cliqueIndex != null) {
							cliqueParentOutputList.add(cliqueIndex);
						} else {
							cliqueParentOutputList.add(-1); // negative means no parent clique
						}
					}
				}
				if (ret.size() != cliqueParentOutputList.size()) {
					throw new IllegalStateException("Desync ocurred while obtaining parents of cliques.");
				}
			}
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
		private Map<Long, Float> uninitializedUserMap;
		private Set<Long> usersPresentInSystemBefore;

		/**
		 * Stores probability tables of cliques/separators of {@link MarkovEngineImpl#getProbabilisticNetwork()}
		 * and asset tables of {@link MarkovEngineImpl#getUserToAssetAwareAlgorithmMap()}.
		 * It also stores the content of {@link MarkovEngineImpl#getUninitializedUserToAssetMap()}
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
				// store what users are present now, so that we can delete new users from system when we restore
				usersPresentInSystemBefore = new HashSet<Long>(getUserToAssetAwareAlgorithmMap().keySet());
			}
			// also store the users which were not initialized yet.
			synchronized (getUninitializedUserToAssetMap()) {
				uninitializedUserMap = new HashMap<Long, Float>(getUninitializedUserToAssetMap());
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
					// reset cache of assets too
					if (algorithm.getAssetPropagationDelegator() instanceof AssetPropagationInferenceAlgorithm) {
						AssetPropagationInferenceAlgorithm assetAlgorithm = (AssetPropagationInferenceAlgorithm) algorithm.getAssetPropagationDelegator();
						// this should reset min asset cache
						assetAlgorithm.setToCacheUnconditionalMinAssets(assetAlgorithm.isToCacheUnconditionalMinAssets());
					}
				}
			}
			// also, restore uninitialized users.
			synchronized (getUninitializedUserToAssetMap()) {
				getUninitializedUserToAssetMap().clear();
				getUninitializedUserToAssetMap().putAll(uninitializedUserMap);
				// remove newly created users (delete users which were not in in the system when this memento was created)
				synchronized (getUserToAssetAwareAlgorithmMap()) {
					Set<Long> usersToDelete = new HashSet<Long>();
					for (Long userToCheck : getUserToAssetAwareAlgorithmMap().keySet()) {
						if (!usersPresentInSystemBefore.contains(userToCheck)) {
							usersToDelete.add(userToCheck);
						}
					}
					for (Long userToDelete : usersToDelete) {
						getUserToAssetAwareAlgorithmMap().remove(userToDelete);
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
	 * If true, methods like {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)}
	 * or {@link #previewTrade(long, long, List, List, List)} will throw exception
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
	public Collection<Long> getTradedQuestions(Long userId) {
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
	protected void setTradedQuestionsMap(Map<Long, Set<Long>> tradedQuestionsMap) {
		this.tradedQuestionsMap = tradedQuestionsMap;
	}

	/**
	 * Map which stores which questions were traded by a user
	 * @return the tradedQuestionsMap
	 */
	protected Map<Long, Set<Long>> getTradedQuestionsMap() {
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

	/**
	 * Stores a mapping from what questions were resolved,
	 * to a mapping from how many states and which of them was the resolution.
	 * @param resolvedQuestionsAndNumberOfStates the resolvedQuestionsAndNumberOfStates to set
	 */
	public void setResolvedQuestions(Map<Long,StatePair> statesSizeOfResolvedQuestions) {
		this.resolvedQuestionsAndNumberOfStates = statesSizeOfResolvedQuestions;
	}

	/**
	 * Stores a mapping from what questions were resolved,
	 * to how many states the original question had, and .
	 * @return the resolvedQuestionsAndNumberOfStates
	 */
	public Map<Long,StatePair> getResolvedQuestions() {
		return resolvedQuestionsAndNumberOfStates;
	}
	
	/**
	 * If true, {@link #commitNetworkActions(long)} will attempt to 
	 * collect all question resolutions which are in sequence
	 * and execute them in 1 BN propagation.
	 * @param isToIntegrateConsecutiveResolutions the isToIntegrateConsecutiveResolutions to set
	 */
	public void setToIntegrateConsecutiveResolutions(
			boolean isToIntegrateConsecutiveResolutions) {
		this.isToIntegrateConsecutiveResolutions = isToIntegrateConsecutiveResolutions;
	}

	/**
	 * If true, {@link #commitNetworkActions(long)} will attempt to 
	 * collect all question resolutions which are in sequence
	 * and execute them in 1 BN propagation.
	 * @return the isToIntegrateConsecutiveResolutions
	 */
	public boolean isToIntegrateConsecutiveResolutions() {
		return isToIntegrateConsecutiveResolutions;
	}

	/**
	 * If true, {@link #getQuestionHistory(Long, List, List)} will
	 * only consider history of trades, and will ignore history
	 * of {@link #addQuestion(Long, Date, long, int, List)}
	 * or {@link #addCash(Long, Date, long, float, String)}.
	 * @param isToRetriveOnlyTradeHistory the isToRetriveOnlyTradeHistory to set
	 */
	public void setToRetriveOnlyTradeHistory(
			boolean isToInserAddQuestionActionIntoHistory) {
		this.isToRetriveOnlyTradeHistory = isToInserAddQuestionActionIntoHistory;
	}

	/**
	 * If true, {@link #getQuestionHistory(Long, List, List)} will
	 * only consider history of trades, and will ignore history
	 * of {@link #addQuestion(Long, Date, long, int, List)}
	 * or {@link #addCash(Long, Date, long, float, String)}.
	 * @return the isToRetriveOnlyTradeHistory
	 */
	public boolean isToRetriveOnlyTradeHistory() {
		return isToRetriveOnlyTradeHistory;
	}

	/**
	 * Trades (i.e. {@link AddTradeNetworkAction#execute()}) will only store the 
	 * history of conditional probabilities (i.e. clique potentials) until the number
	 * of stored clique tables reaches this value. 
	 * Old histories will be deleted when new entries are added.
	 * Set this value to 0 or below in order to disable history of conditional probabilities 
	 * (this will also clear {@link #getLastNCliquePotentialMap()}).
	 * <br/>
	 * <br/>
	 * CAUTION: increasing this value will not automatically fill the history. Only
	 * trades performed after the increment will be affected.
	 * <br/>
	 * <br/>
	 * {@link RebuildNetworkAction#execute()} uses this value and 
	 * {@link #getLastNCliquePotentialMap()} in order to check
	 * whether a clique was edited, so that it can automatically connect
	 * the nodes of modified cliques before any change in network structure (if {@link #isToFullyConnectNodesInCliquesOnRebuild()} == true). 
	 * Hence, starting this value with zero, performing some trades, and then
	 * seting this value to non-zero will make {@link RebuildNetworkAction#execute()} to think that 
	 * the feature of history of cliques is enabled (because this value is above zero), but
	 * there is no change in the cliques (because there is no content in the history). Consequently, in such scenario,
	 * {@link RebuildNetworkAction#execute()} will not connect the nodes in the clique.
	 * Because of this, it is suggested to re-initialize the markov engine if you change this value.
	 * @param maxConditionalProbHistorySize the maxConditionalProbHistorySize to set
	 */
	public void setMaxConditionalProbHistorySize( int maxConditionalProbHistorySize ) {
		
		this.maxConditionalProbHistorySize = maxConditionalProbHistorySize;
		
		// update getLastNCliquePotentialMap() regarding the new size
		if (getLastNCliquePotentialMap() != null) {
			if (maxConditionalProbHistorySize <= 0 ) {
				// the history of conditional probability is de facto disabled
				synchronized (getLastNCliquePotentialMap()) {
					getLastNCliquePotentialMap().clear();
				}
			} else {
				// resize the history if necessary
				synchronized (getLastNCliquePotentialMap()) {
					// delete elements from the history if the new size is smaller than current
					for (List<ParentActionPotentialTablePair> value : getLastNCliquePotentialMap().values()) {
						while (maxConditionalProbHistorySize < value.size()) {
							value.remove(0);
						}
					}
				}
			}
		}
	}

	/**
	 * Trades (i.e. {@link AddTradeNetworkAction#execute()}) will only store the 
	 * history of conditional probabilities (i.e. clique potentials) until the number
	 * of stored clique tables reaches this value. 
	 * Old histories will be deleted when new entries are added.
	 * Set this value to 0 or below in order to disable history of conditional probabilities.
	 * <br/>
	 * <br/>
	 * CAUTION: increasing this value will not automatically fill the history. Only
	 * trades performed after the increment will be affected.
	 * <br/>
	 * <br/>
	 * {@link RebuildNetworkAction#execute()} uses this value and 
	 * {@link #getLastNCliquePotentialMap()} in order to check
	 * whether a clique was edited, so that it can automatically connect
	 * the nodes of modified cliques before any change in network structure (if {@link #isToFullyConnectNodesInCliquesOnRebuild()} == true). 
	 * Hence, starting this value with zero, performing some trades, and then
	 * seting this value to non-zero will make {@link RebuildNetworkAction#execute()} to think that 
	 * the feature of history of cliques is enabled (because this value is above zero), but
	 * there is no change in the cliques (because there is no content in the history). Consequently, in such scenario,
	 * {@link RebuildNetworkAction#execute()} will not connect the nodes in the clique.
	 * Because of this, it is suggested to re-initialize the markov engine if you change this value.
	 * @return the maxConditionalProbHistorySize
	 */
	public int getMaxConditionalProbHistorySize() {
		return maxConditionalProbHistorySize;
	}

	/**
	 * This map stores the history of last N (with N = {@link #getMaxConditionalProbHistorySize()}) clique potentials, modified over edits.
	 * This map can be useful for retrieving the history of conditional probabilities.
	 * The values of this map are a list (ordered by the date/time of the trade) of a pair object.
	 * The pair object contains the clique table and the trade which caused the change in the clique table.
	 * @param lastNCliquePotentialMap the lastNCliquePotentialMap to set
	 */
	public void setLastNCliquePotentialMap(HashMap<Clique, List<ParentActionPotentialTablePair>> lastNCliquePotentialMap) {
		this.lastNCliquePotentialMap = lastNCliquePotentialMap;
	}

	/**
	 * This map stores the history of last N (with N = {@link #getMaxConditionalProbHistorySize()}) clique potentials, modified over edits.
	 * This map can be useful for retrieving the history of conditional probabilities.
	 * The values of this map are a list (ordered by the date/time of the trade) of a pair object.
	 * The pair object contains the clique table and the trade which caused the change in the clique table.
	 * @return the lastNCliquePotentialMap
	 */
	public HashMap<Clique, List<ParentActionPotentialTablePair>> getLastNCliquePotentialMap() {
		return lastNCliquePotentialMap;
	}
	
	/**
	 * This is a class representing a pair: one object of AddTradeNetworkAction
	 * and one object of PotentialTable.
	 * @author Shou Matsumoto
	 */
	public class ParentActionPotentialTablePair {
		private NetworkAction parentAction;
		private PotentialTable table;
		public ParentActionPotentialTablePair(NetworkAction parentAction, PotentialTable table) {
			this.parentAction = parentAction;
			this.table = table;
		}
		public NetworkAction getParentAction() { return parentAction; }
		public void setParentAction(NetworkAction action) { parentAction = action; }
		public PotentialTable getTable() { return table; }
	}

	/**
	 * This class is used to represent a resolution of a question,
	 * which is a pair: total number of states, and resolved state
	 * @author Shou Matsumoto
	 */
	public class StatePair {
		private final Integer statesSize;
		private final Integer resolvedState;
		public StatePair(Integer statesSize, Integer resolvedState) {
			this.statesSize = statesSize;
			this.resolvedState = resolvedState;
		}
		public Integer getStatesSize() { return statesSize; }
		public Integer getResolvedState() { return resolvedState; }
		public String toString() {return "["+ resolvedState +"/" + statesSize + "]";}
		
	}

	/**
	 * This method implements the following requirements:<br/><br/>
	 * 1. Export the BN in a ".net" format (which can supposedly be opened in Hugin, 
	 * UnBBayes and Netica). Tecnically, this method will use {@link MarkovEngineImpl#getIO()}
	 * in order to store/load networks. <br/>
	 * 1.1. It will be exported to a file in this iteration.<br/>
	 * 1.2. The exported BN will have the CPTs with uniform distribution.<br/>
	 * 1.3. The exported BN will contain the resolved questions 
	 * (which will be retrieved from the history).<br/>
	 * 1.4. The position (x and y coordinates) of the nodes will be random.<br/>
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#exportNetwork(java.io.File)
	 */
	public synchronized void exportNetwork(File file) throws IOException, IllegalStateException {
		
		// the exported net will have this name
		String netName = "Exported_" + new Date().toString();
		netName.replace(" ", "_");
		netName.replace(":", "-");
		
		// use a new instance of the network and rebuild the structure from history, 
		// because we want the resolved questions to be present in the exported network.
		ProbabilisticNetwork netToSave = new ProbabilisticNetwork(netName);
		
		// retrieve nodes from history, because of the following requirement:
		//		1.3. The exported BN will contain the resolved questions (which will be retrieved from the history).
		
		synchronized (getExecutedActions()) {
			// since we are reusing network actions (which initializes CPT w/ uniform distribution),
			// the following requirement is also satisfied
			//		1.2. The exported BN will have the CPTs with uniform distribution.
			List<NetworkAction> actions = getExecutedActions();
			for (NetworkAction action : actions) {
				
				// only execute actions related to structure changes
				if (action.isStructureConstructionAction()) {
					
					if (action instanceof AddQuestionNetworkAction) {
						((AddQuestionNetworkAction)action).execute(netToSave,false); // false := do not consider assets and junction tree
						//		1.4. The position (x and y coordinates) of the nodes will be random.
						Node node = netToSave.getNode(action.getQuestionId().toString());
						node.setPosition(50 + Math.random()*getNodePositionRandomRange() , 50 + Math.random()*getNodePositionRandomRange());
					} else if (action instanceof AddQuestionAssumptionNetworkAction) {
						((AddQuestionAssumptionNetworkAction)action).execute(netToSave);
					} else if (action instanceof ImportNetworkAction) {
						// drop everything and substitute with the imported network at this moment
						// TODO optimize
						synchronized (getDefaultInferenceAlgorithm()) {
							netToSave = getDefaultInferenceAlgorithm().cloneProbabilisticNetwork(((ImportNetworkAction)action).getImportedNet());
						}
					}
				}
			}
		}
		
		// for compatibility with hugin and netica, names of nodes should not start with numbers.
		for (Node node : netToSave.getNodes()) {
			node.setName(getExportedNodePrefix() + node.getName());
		}
		
		//		1. Export the BN in a ".net" format (which can supposedly be opened in Hugin, UnBBayes and Netica). 
		//		1.1. It will be exported to a file in this iteration
		BaseIO io = this.getIO();
		synchronized (io) {
			io.save(file, netToSave);
		}
	}
	

	/**
	 * This class represents a network action for substituting the current network
	 * with another (imported) network.
	 * This method will attempt to re-run all trades from the history.
	 * @author Shou Matsumoto
	 */
	public class ImportNetworkAction extends StructureChangeNetworkAction {
		private static final long serialVersionUID = 1370086072061130060L;
		private final long transactionKey;
		private final long occurredWhen;
		private final ProbabilisticNetwork importedNet;
		private long whenExecutedFirstTime = -1;

		/** Default constructor initializing fields */
		public ImportNetworkAction(Long transactionKey, long occurredWhen, ProbabilisticNetwork importedNet) {
			if (importedNet == null) {
				throw new IllegalArgumentException("Imported network cannot be null.");
			}
			this.importedNet = importedNet;
			this.transactionKey = transactionKey;
			this.occurredWhen = occurredWhen;
		}
		
		/**
		 * Substitutes the current network and re-run trades
		 */
		@Override
		public void execute(ProbabilisticNetwork net) {
			if (net != getProbabilisticNetwork()) {
				throw new UnsupportedOperationException("Not implemented for arbitrary network yet. Must be called for MarkovEngineImpl#getProbabilisticNetwork()");
			}
			this.execute();
		}
		
		/** Substitutes the current network and re-run trades */
		public void execute() {
			// this is the network prior to import. Need to keep it, so that we can revert to it in case of exception
			ProbabilisticNetwork previousNet = null;
			
			synchronized (getProbabilisticNetwork()) {
				// this is the net to be overwritten (the current net)
				ProbabilisticNetwork currentNet = getProbabilisticNetwork();
				
				// backup previous net in case of exceptions
				synchronized (getDefaultInferenceAlgorithm()) {
					previousNet = getDefaultInferenceAlgorithm().cloneProbabilisticNetwork(currentNet);
				}
				
				try {
					// clean current net, so that we can re-add all nodes again
					currentNet.clear();
					
					// read the imported net and replicate structure
					
					// replicate nodes. Use clones, because we don't want the importedNet to be changed
					for (Node node : importedNet.getNodes()) {
						ProbabilisticNode newNode = ((ProbabilisticNode)node).basicClone();
						if (newNode.getProbabilityFunction().getVariablesSize() <= 0) {
							newNode.getProbabilityFunction().addVariable(newNode);
						}
						currentNet.addNode(newNode);
					}
					// replicate edges
					for (Edge oldEdge : importedNet.getEdges()) {
						Node node1 = currentNet.getNode(oldEdge.getOriginNode().getName());
						Node node2 = currentNet.getNode(oldEdge.getDestinationNode().getName());
						Edge newEdge = new Edge(node1, node2);
						try {
							currentNet.addEdge(newEdge);
						} catch (InvalidParentException e) {
							throw new RuntimeException("Could not clone edge " + oldEdge +" of network " + importedNet , e);
						}
					}
					// 2.3. If the CPTs of the imported BN are not uniform, then they will be initially converted to uniform.
					// So, make them uniform regardless of imported net.
					for (Node node : currentNet.getNodes()) {
						// force CPT to become uniform
						float value = (float) (1.0/node.getStatesSize());	// calculate the uniform prob value
						PotentialTable newCPT = ((ProbabilisticNode)node).getProbabilityFunction();
						for (int i = 0; i < newCPT.tableSize(); i++) {
							newCPT.setValue(i, value);
						}
					}
					
					// the following actions (re-run trades and add virtual trades) must only be executed if this is the 1st time we run this import action
					if (this.getWhenExecutedFirstTime() == null) {
						// and re-run all trades from the history (i.e. handle the import feature as if it is an ordinal request for changing the network structure). 
						List<NetworkAction> actionsToExecute = new ArrayList<NetworkAction>();
						synchronized (getExecutedActions()) {
							for (NetworkAction action : getExecutedActions()) {
								// only consider non-structure change actions (i.e. trades, resolutions, balance, add cash...)
								if (!action.isStructureConstructionAction()) {
									actionsToExecute.add(action);
								}
							}
						}
						
						// re-use RebuildNetworkAction to re-run trades. It will rebuild junction tree (and all user's asset tables).
						if (getExecutedActions().contains(this)) {
							// make sure the current action is not in the history which will be executed by the RebuildNetworkAction (or else it will loop)
							getExecutedActions().remove(this);
							new RebuildNetworkAction(-1, null, null, null).execute(actionsToExecute);
							getExecutedActions().add(this);
						} else {
							new RebuildNetworkAction(-1, null, null, null).execute(actionsToExecute);
						}
						
						// "virtual" trades will be performed in order to set the current conditional probabilities equal to the non-uniform CPTs
						for (Node importedNode : importedNet.getNodes()) {
							long questionId = Long.parseLong(importedNode.getName());
							if (getResolvedQuestions().containsKey(questionId)) {
								continue;	// ignore trades in resolved nodes
							}
							
							// CPT of the imported node
							PotentialTable cpt = ((ProbabilisticNode)importedNode).getProbabilityFunction();
							
							// check if cpt is non-uniform distribution
							boolean isUniformDistribution = true;
							// this value is expected in a uniform distribution
							float uniformValue = (float) (1.0/importedNode.getStatesSize());
							for (int i = 0; i < cpt.tableSize(); i++) {
								// it is not a uniform distribution if some value in cpt has value other than the uniformValue (with some error margin)
								if (Math.abs(cpt.getValue(i) - uniformValue) > getProbabilityErrorMargin()) {
									isUniformDistribution = false;
									break;
								}
							}
							if (!isUniformDistribution) {
								// add a "virtual" trade which will make the conditional probability to match the cpt
								
								// use a clone, so that we can delete variables which were already resolved
								cpt = (PotentialTable) cpt.clone();
								
								// set of nodes in the cpt which were already resolved
								Set<INode> resolvedNodesInCPT = new HashSet<INode>();
								
								// set impossible states (i.e. complement of resolved states of resolved nodes) to zero, 
								// so that we can sum out (remove nodes from cpt) later.
								for (int i = 0; i < cpt.tableSize(); i++) {	// iterate over cells in cpt
									// convert i into a vector which indicates what node is in what state at current cell of cpt
									int[] coord = cpt.getMultidimensionalCoord(i);
									// for each node in the cpt, check if its state matches the resolutions
									// we are only interested in parents in the cpt, so start from index 1 (index 0 is the imported node itself)
									for (int nodeIndex = 1; nodeIndex < cpt.variableCount(); nodeIndex++) {
										INode nodeInCPT = cpt.getVariableAt(nodeIndex);	// this is one of the nodes pointed by the cell of this cpt
										StatePair resolution = getResolvedQuestions().get(Long.parseLong(nodeInCPT.getName()));
										if (resolution != null) {
											// this node in the cpt was resolved previously 
											resolvedNodesInCPT.add(nodeInCPT);
											if (coord[nodeIndex] != resolution.getResolvedState().intValue()) {
												// resolved to a state other than this, so set this cell to zero
												cpt.setValue(i, 0f);
											}
										}
									}
								}
								
								// sum out the resolved node from cpt
								for (INode node : resolvedNodesInCPT) {
									// we do not need to normalize, because incompatible states were supposedly set to zero
									cpt.removeVariable(node, false);	
								}
								
								// at this point, cpt contains only non-resolved nodes
								
								// prepare list of parents according to cpt
								List<Long> assumptionIds = new ArrayList<Long>(cpt.getVariablesSize());
								
								// index 0 has importedNode. We are only interested on parents of importedNode, so start from index 1
								for (int i = 1; i < cpt.getVariablesSize(); i++) {
									long parentId = Long.parseLong(cpt.getVariableAt(i).getName());
									// assumptionIds will contain parents of importedNode in the same order of in cpt.
									assumptionIds.add(parentId);
								}
								
								// prepare list of states of the parents
								List<Integer> assumedStates = new ArrayList<Integer>(assumptionIds.size());
								for (int i = 0; i< assumptionIds.size(); i++) {
									// initialize with invalid state
									assumedStates.add(Integer.MAX_VALUE);
								}
								
								// prepare trade value
								List<Float> newValues = new ArrayList<Float>();
								
								// Do virtual trades. Extract trade value from cpt
								for (int i = 0; i < cpt.tableSize(); i++) {
									
									// convert the cell index of cpt to a vector indicating states of the node and states of the parents
									int[] coord = cpt.getMultidimensionalCoord(i);
									
									// values to be used as a trade
									newValues.add(cpt.getValue(i));	
									
									// check whether this cell represents the last state of importedNode. 
									// If so, the next iteration has another combination of parents' states (so run trade now).
									// Currently, we are adding 1 trade per combination of parent's states.
									// TODO all these trades can actually be performed in 1 propagation. Do them in 1 propagation instead of doing 1 propagation per states of parents.
									if (coord[cpt.indexOfVariable(importedNode)] == importedNode.getStatesSize() - 1) {
										
										// extract from coord the current states of the parents associated with the current cell of cpt
										for (int nodeIndex = 0; nodeIndex < assumedStates.size(); nodeIndex++) {
											// coord has 1 additional node in index 0 (the importedNode itself),
											// but assumedStates should only have states of its parents, so index in coord will be nodeIndex+1
											assumedStates.set(nodeIndex, coord[nodeIndex+1]);
										}
										
										// execute the "virtual" trade
										VirtualTradeAction virtualTrade = new VirtualTradeAction(this, questionId, newValues, assumptionIds, assumedStates);
										addNetworkActionIntoQuestionMap(virtualTrade, questionId);
										virtualTrade.execute();
										virtualTrade.setWhenExecutedFirstTimeMillis(System.currentTimeMillis());
										
										// note virtual trades should be added to history, so that future rebuild actions can execute them again when re-running history
										synchronized (getExecutedActions()) {
											getExecutedActions().add(virtualTrade);
										}
										
										// reset newValues, because the next iteration will fill it with different values
										newValues = new ArrayList<Float>();
									}
								}
								
							}
							
							
						} // end of iteration on each imported nodes
					}	// end of block executed only on 1st execution of this import action
					
				} catch (Throwable e) {
					// 2.2. If any trade in the history cannot be performed in the imported BN (e.g. nodes/arcs are missing), it will throw an exception.
					
					// revert to the previous network
					setProbabilisticNetwork(previousNet);
					synchronized (getDefaultInferenceAlgorithm()) {
						try {
							getDefaultInferenceAlgorithm().setRelatedProbabilisticNetwork(previousNet);
						} catch (Exception e1) {
							e.printStackTrace();
							// Exception translation is not a good practice, but we need to convert it to runtime exception so that we can
							// throw it to the caller without changing method signature of execute().
							throw new RuntimeException("Fatal: could not revert bayes net of the default algorithm to the previous state after an exception.",e1);
						}
					}
					
					// make sure this action is not included in the list of executed actions
					// or else the RebuildNetworkAction will loop
					synchronized (getExecutedActions()) {
						getExecutedActions().remove(this);
					}
					
					// rebuild structure and re-run trades, because we don't know when the exception was thrown
					new RebuildNetworkAction(-1, null, null, null).execute();
					
					// Exception translation is not a good practice, but we need to convert it to runtime exception so that we can
					// throw it to the caller without changing method signature of execute().
					throw new RuntimeException(e);
				}
			}
			
			// we don't need the previous net (because the execution went OK). Dispose
			try {
				previousNet.clear();
			} catch (Throwable t) {
				Debug.println(getClass(), "Ignored exception " + t.getMessage(), t);
			}
		}
		
		/** Not defined */
		public void revert() throws UnsupportedOperationException {throw new UnsupportedOperationException("Revert not supported for " + this.getClass());}
		public Date getWhenCreated() { 
			if (occurredWhen < 0) {
				return null;
			}
			return new Date(this.occurredWhen); 
		}
		public Date getWhenExecutedFirstTime() {
			if (whenExecutedFirstTime < 0) {
				return null;
			}
			return new Date(whenExecutedFirstTime) ; 
		}
		public List<Float> getOldValues() { return null; }
		public void setOldValues(List<Float> oldValues) { }
		public List<Float> getNewValues() { return null; }
		public void setNewValues(List<Float> newValues) {}
		public String getTradeId() { return getImportedNet().getId(); }
		public Integer getSettledState() {return null; }
		public boolean isHardEvidenceAction() { return false; }
		public Long getTransactionKey() { return transactionKey; }
		public Long getUserId() { return null; }
		public Long getQuestionId() { return null; }
		public List<Long> getAssumptionIds() {  return null; }
		public List<Integer> getAssumedStates() { return null; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { 
			if (whenExecutedFirst == null) {
				this.whenExecutedFirstTime = -1;	// negative represents invalid date/time
			} else {
				this.whenExecutedFirstTime = whenExecutedFirst.getTime(); 
			}
		}
		public ProbabilisticNetwork getImportedNet() { return importedNet; }
		public Boolean isCorrectiveTrade() {return getCorrectedTrade() != null;}
		public NetworkAction getCorrectedTrade() {return null; }
		/** Importing network may trigger rebuild, because it may add new arcs */
		public boolean isTriggerForRebuild() { return true; }

		public long getWhenCreatedMillis() {
			return occurredWhen;
		}

		public long getWhenExecutedFirstTimeMillis() {
			return whenExecutedFirstTime;
		}

		public void setWhenExecutedFirstTimeMillis(long whenExecutedFirst) {
			this.whenExecutedFirstTime = whenExecutedFirst;
		}
	}
	
	


	/**
	 * This method implements the following requirements: <br/> <br/>
	 * 2. Import the BN from a ".net" format, and re-run all trades from the history 
	 * (i.e. handle the import feature as if it is an ordinal request for changing the network structure).<br/> 
	 * 2.1. It will be imported to a file in this iteration. Tecnically, this method will use {@link MarkovEngineImpl#getIO()}
	 * in order to store/load networks.<br/>
	 * 2.2. If any trade in the history cannot be performed in the imported BN 
	 * (e.g. nodes/arcs are missing), it will throw an exception.<br/>
	 * 2.3. If the CPTs of the imported BN are not uniform, then they will be initially converted to uniform, 
	 * all trades will be re-run from the history, and then "virtual" trades 
	 * (changes in probabilities which will not change user's assets) will be performed in order to set 
	 * the current conditional probabilities equal to the non-uniform CPTs (i.e. if any CPT of the imported BN is not uniform, 
	 * then it will be treated like a trade performed at the moment of the import).<br/>
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#importNetwork(java.io.File)
	 */
	public synchronized void importNetwork(File file) throws IOException, IllegalStateException {
		
		ProbabilisticNetwork net = null;	// this will be the imported network
		
		// 2. Import the BN from a ".net" format, 
		// 2.1. It will be imported to a file in this iteration
		BaseIO io = this.getIO();
		synchronized (io) {
			net = (ProbabilisticNetwork) io.load(file);
		}
		
		// make sure the prefixes of names of nodes are removed
		for (Node node : net.getNodes()) {
			node.setName(node.getName().replace(getExportedNodePrefix(), ""));
		}
		
		// instantiate an action event for the "import" action and execute immediately
		long transactionKey = this.startNetworkActions();
		// this will be the action representing (and will be stored in the history)
		ImportNetworkAction importAction = new ImportNetworkAction(transactionKey, System.currentTimeMillis(), net);
		// do not forget to update history of actions, so that future rebuilds can execute this action again
		this.addNetworkAction(transactionKey, importAction);
		// execute action. This shall update the history as well
		this.commitNetworkActions(transactionKey, false);	
		
	}
	
	/**
	 * This method returns some statistics of the currently used
	 * probabilistic network.
	 * For example, the returned object will contain several information, including the size of the network
	 * organized by number of possible states of a node.
	 * In a clique-based implementation, this will also include
	 * information related to clique size.
	 * @return instance of {@link NetStatistics}
	 * @see NetStatistics
	 * @see NetStatisticsImpl
	 */
	public NetStatistics getNetStatistics() {
		NetStatistics ret = new NetStatisticsImpl();
		
		// extract network statistics
		ProbabilisticNetwork net = this.getProbabilisticNetwork();
		if (net == null) {
			throw new IllegalStateException("No network is instantiated.");
		}
		
		ret.setNumArcs(net.getEdges().size());
		
		// count the quantity of nodes by quantity of states
		Map<Integer, Integer> numberOfStatesToNumberOfNodesMap = new HashMap<Integer, Integer>();
		// also get the max number of parents per node
		int maxNumParents = 0;
		synchronized (net) {
			for (Node node : net.getNodes()) {
				Integer count = numberOfStatesToNumberOfNodesMap.get(node.getStatesSize());
				if (count == null) {
					count = 0;
				}
				count++;
				numberOfStatesToNumberOfNodesMap.put(node.getStatesSize(), count);
				if (node.getParents().size() > maxNumParents) {
					maxNumParents = node.getParents().size();
				}
			}
		}
		
		ret.setNumberOfStatesToNumberOfNodesMap(numberOfStatesToNumberOfNodesMap);
		ret.setMaxNumParents(maxNumParents);
		
		// see the maximum clique table size and degree of freedom
		int maxCliqueTableSize = 0;
		long df = 0;
		int sumOfCliqueSizes = 0;
		int sumOfCliqueSizesWithoutResolvedCliques = 0;
		for (Clique clique : net.getJunctionTree().getCliques()) {
			if (clique.getProbabilityFunction().tableSize() > maxCliqueTableSize) {
				maxCliqueTableSize = clique.getProbabilityFunction().tableSize();
			}
			if ((clique.getProbabilityFunction().tableSize() - 1) > 0) {
				df += (clique.getProbabilityFunction().tableSize() - 1);
			}
			sumOfCliqueSizes += clique.getProbabilityFunction().tableSize();
			if (clique.getProbabilityFunction().tableSize() > 1) {
				sumOfCliqueSizesWithoutResolvedCliques += clique.getProbabilityFunction().tableSize();
			}
		}
		
		ret.setSumOfCliqueTableSizes(sumOfCliqueSizes);
		ret.setSumOfCliqueTableSizesWithoutResolvedCliques(sumOfCliqueSizesWithoutResolvedCliques);
		
		// subtract the degree of freedom from separators
		for (Separator sep : net.getJunctionTree().getSeparators()) {
			 sumOfCliqueSizes += sep.getProbabilityFunction().tableSize();
			 if (sep.getProbabilityFunction().tableSize() > 1) {
					sumOfCliqueSizesWithoutResolvedCliques += sep.getProbabilityFunction().tableSize();
				}
			 if (sep.getProbabilityFunction().tableSize() > 0) {
				 df -= (sep.getProbabilityFunction().tableSize() - 1);
			 }
		}
		ret.setSumOfCliqueAndSeparatorTableSizes(sumOfCliqueSizes);
		ret.setSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques(sumOfCliqueSizesWithoutResolvedCliques);
		ret.setDegreeOfFreedom((int) df);
		
		// extract other statistics of cliques
		ret.setNumberOfCliques(net.getJunctionTree().getCliques().size());
		ret.setNumberOfSeparators(net.getJunctionTree().getSeparators().size());
		ret.setMaxCliqueTableSize(maxCliqueTableSize);
		ret.setNumberOfNonEmptyCliques(this.getQuestionAssumptionGroups().size());
		
		// extract statistics of parents of nodes
		
		return ret;
	}
	
	/**
	 * This is just a simple network action which prints out the current
	 * network statistics after the last network change is committed in
	 * {@link MarkovEngineImpl#commitNetworkActions(long, boolean)}.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#getNetStatistics()
	 * @see MarkovEngineImpl#isToPrintNetStatisticsAfterLastStructureChange()
	 */
	public class PrintNetStatisticsNetworkAction implements NetworkAction {
		private static final long serialVersionUID = -7620744234553916038L;
		public Date getWhenCreated() { return new Date(); }
		public Date getWhenExecutedFirstTime() {return new Date(); }
		public List<Float> getOldValues() { return null; }
		public List<Float> getNewValues() { return null; }
		public String getTradeId() {return ""; }
		public Integer getSettledState() { return null; }
		public Boolean isCorrectiveTrade() { return null; }
		public Long getUserId() { return null;}
		/**
		 * Just print to console the current network statistics
		 * @see MarkovEngineImpl#getNetStatistics()
		 */
		public void execute() {
			NetStatistics statistics = getNetStatistics();
			System.out.println(statistics);
		}
		public void revert() throws UnsupportedOperationException {}
		public boolean isStructureConstructionAction() { return false; }
		public boolean isTriggerForRebuild() { return false; }
		public boolean isHardEvidenceAction() { return false; }
		public Long getTransactionKey() { return null; }
		public Long getQuestionId() { return null; }
		public List<Long> getAssumptionIds() { return null; }
		public List<Integer> getAssumedStates() { return null; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { }
		public NetworkAction getCorrectedTrade() { return null; }
		public void setOldValues(List<Float> oldValues) { }
		public void setNewValues(List<Float> newValues) { }
	}

	/**
	 * This object will be used by {@link #exportNetwork(File)}
	 * and {@link #importNetwork(File)} in order to 
	 * load/save the Bayes net from/into files.
	 * @param io the io to set
	 */
	public void setIO(BaseIO io) {
		this.io = io;
	}

	/**
	 * This object will be used by {@link #exportNetwork(File)}
	 * and {@link #importNetwork(File)} in order to 
	 * load/save the Bayes net from/into files.
	 * By changing this object, more file formats will be available.
	 * @return the io
	 */
	public BaseIO getIO() {
		return io;
	}

	/**
	 * {@link #exportNetwork(File)} will set the position of the nodes 
	 * using {@link Math#random()} multiplied by this value.
	 * By changing this object, more file formats will be available.
	 * @param nodePositionRandomRange the nodePositionRandomRange to set
	 */
	public void setNodePositionRandomRange(float nodePositionRandomRange) {
		this.nodePositionRandomRange = nodePositionRandomRange;
	}

	/**
	 * {@link #exportNetwork(File)} will set the position of the nodes 
	 * using {@link Math#random()} multiplied by this value.
	 * @return the nodePositionRandomRange
	 */
	public float getNodePositionRandomRange() {
		return nodePositionRandomRange;
	}

	/**
	 * The names of the nodes exported by {@link #exportNetwork(File)} will
	 * have this string as prefix.
	 * Additionally, {@link #importNetwork(File)} will assume that
	 * the imported nodes have this value as prefix (hence, the prefixes will be removed before
	 * import).
	 * @return the exportedNodePrefix
	 */
	public String getExportedNodePrefix() {
		return exportedNodePrefix;
	}

	/**
	 * The names of the nodes exported by {@link #exportNetwork(File)} will
	 * have this string as prefix.
	 * Additionally, {@link #importNetwork(File)} will assume that
	 * the imported nodes have this value as prefix (hence, the prefixes will be removed before
	 * import).
	 * @param exportedNodePrefix the exportedNodePrefix to set
	 */
	public void setExportedNodePrefix(String exportedNodePrefix) {
		this.exportedNodePrefix = exportedNodePrefix;
	}

	/**
	 * Stores all instances of virtual trades created by this markov engine in order to trace the marginal probabilities,
	 * and maps to what question IDs the virtual trades are linked to.
	 * This can be thought as a submapping of the inverse of {@link #getNetworkActionsIndexedByQuestions()}
	 * (from virtual trades, find related questions)
	 * @param virtualTradeToAffectedQuestionsMap the virtualTradeToAffectedQuestionsMap to set
	 * @see #addVirtualTradeIntoMarginalHistory(ResolveQuestionNetworkAction, Map)
	 * @see #addVirtualTradeIntoMarginalHistory(NetworkAction, Map)
	 */
	public void setVirtualTradeToAffectedQuestionsMap(Map<VirtualTradeAction, Set<Long>> virtualTradeToAffectedQuestionsMap) {
		this.virtualTradeToAffectedQuestionsMap = virtualTradeToAffectedQuestionsMap;
	}

	/**
	 * Stores all instances of virtual trades created by this markov engine in order to trace the marginal probabilities,
	 * and maps to what question IDs the virtual trades are linked to.
	 * This can be thought as a submapping of the inverse of {@link #getNetworkActionsIndexedByQuestions()}
	 * (from virtual trades, find related questions)
	 * @return the virtualTradeToAffectedQuestionsMap
	 * @see #addVirtualTradeIntoMarginalHistory(ResolveQuestionNetworkAction, Map)
	 * @see #addVirtualTradeIntoMarginalHistory(NetworkAction, Map)
	 */
	public Map<VirtualTradeAction, Set<Long>> getVirtualTradeToAffectedQuestionsMap() {
		return virtualTradeToAffectedQuestionsMap;
	}

	/**
	 * This is the error margin used when comparing two asset values.
	 * If {@link Math#abs(double)} of asset1-asset2 is smaller than assetErrorMargin, then asset1 == asset2.
	 * @param assetErrorMargin the assetErrorMargin to set
	 */
	public void setAssetErrorMargin(float assetErrorMargin) {
		this.assetErrorMargin = assetErrorMargin;
	}

	/**
	 * This is the error margin used when comparing two asset values.
	 * If {@link Math#abs(double)} of asset1-asset2 is smaller than assetErrorMargin, then asset1 == asset2.
	 * @return the assetErrorMargin
	 */
	public float getAssetErrorMargin() {
		return assetErrorMargin;
	}

	/**
	 * If true, {@link BalanceTradeNetworkAction} will balance a question given all
	 * possible assumptions, no matter what was the actual assumption.
	 * @param isToForceBalanceQuestionEntirely the isToForceBalanceQuestionEntirely to set
	 */
	public void setToForceBalanceQuestionEntirely(
			boolean isToForceBalanceQuestionEntirely) {
		this.isToForceBalanceQuestionEntirely = isToForceBalanceQuestionEntirely;
	}

	/**
	 * If true, {@link BalanceTradeNetworkAction} will balance a question given all
	 * possible assumptions, no matter what was the actual assumption.
	 * @return the isToForceBalanceQuestionEntirely
	 */
	public boolean isToForceBalanceQuestionEntirely() {
		return isToForceBalanceQuestionEntirely;
	}

	/**
	 * If true, {@link #addToLastNCliquePotentialMap(NetworkAction, Map)} will
	 * store only the {@link #getLastNCliquePotentialMap()} cliques which have actually changed
	 * (note: checking whether a clique potential have changed may be computationally expensive).
	 * @param isToStoreOnlyCliqueChangeHistory the isToStoreOnlyCliqueChangeHistory to set
	 */
	public void setToStoreOnlyCliqueChangeHistory(
			boolean isToStoreOnlyCliqueChangeHistory) {
		this.isToStoreOnlyCliqueChangeHistory = isToStoreOnlyCliqueChangeHistory;
	}

	/**
	 * If true, {@link #addToLastNCliquePotentialMap(NetworkAction, Map)} will
	 * store only the {@link #getLastNCliquePotentialMap()} cliques which have actually changed
	 * (note: checking whether a clique potential have changed may be computationally expensive).
	 * @return the isToStoreOnlyCliqueChangeHistory
	 */
	public boolean isToStoreOnlyCliqueChangeHistory() {
		return isToStoreOnlyCliqueChangeHistory;
	}

	/**
	 * @param isToFullyConnectNodesInCliquesOnRebuild : if set to true, 
	 * all nodes in a same clique will be fully connected during {@link RebuildNetworkAction#execute()},
	 * so that newly created cliques are always extensions of old cliques.
	 */
	public void setToFullyConnectNodesInCliquesOnRebuild(
			boolean isToFullyConnectNodesInCliquesOnRebuild) {
		this.isToFullyConnectNodesInCliquesOnRebuild = isToFullyConnectNodesInCliquesOnRebuild;
	}

	/**
	 * @return if true, all nodes in a same clique will be fully connected during {@link RebuildNetworkAction#execute()},
	 * so that newly created cliques are always extensions of old cliques.
	 */
	public boolean isToFullyConnectNodesInCliquesOnRebuild() {
		return isToFullyConnectNodesInCliquesOnRebuild;
	}

	/**
	 * This is mainly used for finding cycles in {@link RebuildNetworkAction#execute()}
	 * @param mseparationUtility the mseparationUtility to set
	 * @see MSeparationUtility#getRoutes(INode, INode, Map, Set, int)
	 */
	public void setMSeparationUtility(MSeparationUtility mseparationUtility) {
		this.mseparationUtility = mseparationUtility;
	}

	/**
	 * This is mainly used for finding cycles in {@link RebuildNetworkAction#execute()}
	 * @return the mseparationUtility
	 * @see MSeparationUtility#getRoutes(INode, INode, Map, Set, int)
	 */
	public MSeparationUtility getMSeparationUtility() {
		return mseparationUtility;
	}

	/**
	 * If true, actions created in {@link RebuildNetworkAction#execute()} to fully connect nodes in edited clique
	 * will be included in to history, so that next rebuild will re-use the same arcs. <br/>
	 * CAUTION: re-using the arcs may need extra care not to include cycles.
	 * @param isToIncludeFullConnectActionsOfRebuildIntoHistory the isToIncludeFullConnectActionsOfRebuildIntoHistory to set
	 */
	public void setToIncludeFullConnectActionsOfRebuildIntoHistory(
			boolean isToIncludeFullConnectActionsOfRebuildIntoHistory) {
		this.isToIncludeFullConnectActionsOfRebuildIntoHistory = isToIncludeFullConnectActionsOfRebuildIntoHistory;
	}

	/**
	 * If true, actions created in {@link RebuildNetworkAction#execute()} to fully connect nodes in edited clique
	 * will be included in to history, so that next rebuild will re-use the same arcs. <br/>
	 * CAUTION: re-using the arcs may need extra care not to include cycles.
	 * @return the isToIncludeFullConnectActionsOfRebuildIntoHistory
	 */
	public boolean isToIncludeFullConnectActionsOfRebuildIntoHistory() {
		return isToIncludeFullConnectActionsOfRebuildIntoHistory;
	}

	/**
	 * If false, {@link CliqueSensitiveTradeSpecification#getClique()} will be set to null after execution of balance trade, and
	 * old cliques will not remain in history
	 * @param isToKeepCliquesOfBalanceTradeInMemory the isToKeepCliquesOfBalanceTradeInMemory to set
	 */
	public void setToKeepCliquesOfBalanceTradeInMemory(
			boolean isToKeepCliquesOfBalanceTradeInMemory) {
		this.isToKeepCliquesOfBalanceTradeInMemory = isToKeepCliquesOfBalanceTradeInMemory;
	}

	/**
	 * If false, {@link CliqueSensitiveTradeSpecification#getClique()} will be set to null after execution of balance trade, and
	 * old cliques will not remain in history
	 * @return the isToKeepCliquesOfBalanceTradeInMemory
	 */
	public boolean isToKeepCliquesOfBalanceTradeInMemory() {
		return isToKeepCliquesOfBalanceTradeInMemory;
	}

	/**
	 * If true, balancing trades will always be re-calculated (instead of precisely re-executing previous balancing trades) 
	 * when actions are re-executed from history 
	 * @see RebuildNetworkAction#execute()
	 * @see BalanceTradeNetworkAction#execute()
	 * @see #isToForceBalanceQuestionEntirely()
	 * @param isToRecalculateBalancingTradeOnRebuild the isToRecalculateBalancingTradeOnRebuild to set
	 */
	public void setToRecalculateBalancingTradeOnRebuild(
			boolean isToRecalculateBalancingTradeOnRebuild) {
		this.isToRecalculateBalancingTradeOnRebuild = isToRecalculateBalancingTradeOnRebuild;
	}

	/**
	 * If true, balancing trades will always be re-calculated (instead of precisely re-executing previous balancing trades) 
	 * when actions are re-executed from history 
	 * @see RebuildNetworkAction#execute()
	 * @see BalanceTradeNetworkAction#execute()
	 * @see #isToForceBalanceQuestionEntirely()
	 * @return the isToRecalculateBalancingTradeOnRebuild
	 */
	public boolean isToRecalculateBalancingTradeOnRebuild() {
		return isToRecalculateBalancingTradeOnRebuild;
	}

	/**
	 * @param isToCompareProbOnRebuild the isToCompareProbOnRebuild to set
	 */
	public void setToCompareProbOnRebuild(boolean isToCompareProbOnRebuild) {
		this.isToCompareProbOnRebuild = isToCompareProbOnRebuild;
	}

	/**
	 * @return the isToCompareProbOnRebuild
	 */
	public boolean isToCompareProbOnRebuild() {
		return isToCompareProbOnRebuild;
	}

	/**
	 * This is used in lazy user initialization. If user did never trade, but assets were queried, this map will be used.
	 * @param uninitializedUserToAssetMap the uninitializedUserToAssetMap to set
	 */
	protected void setUninitializedUserToAssetMap(
			Map<Long, Float> uninitializedUserToAssetMap) {
		this.uninitializedUserToAssetMap = uninitializedUserToAssetMap;
	}

	/**
	 * This is used in lazy user initialization. If user did never trade, but assets were queried, this map will be used.
	 * @return the uninitializedUserToAssetMap
	 */
	protected Map<Long, Float> getUninitializedUserToAssetMap() {
		return uninitializedUserToAssetMap;
	}

	/**
	 *  If true, users will be only initialized if they make trade.
	 * @param isToLazyInitializeUsers the isToLazyInitializeUsers to set
	 */
	public void setToLazyInitializeUsers(boolean isToLazyInitializeUsers) {
		this.isToLazyInitializeUsers = isToLazyInitializeUsers;
		if (!isToLazyInitializeUsers) {
			// reset mapping of non-inintialized users, because it won't be used anymore
			getUninitializedUserToAssetMap().clear();
		}
	}

	/**
	 *  If true, users will be only initialized if they make trade.
	 * @return the isToLazyInitializeUsers
	 */
	public boolean isToLazyInitializeUsers() {
		return isToLazyInitializeUsers;
	}

	/**
	 * If true, {@link #previewBalancingTrades(long, long, List, List)},
	 * {@link #previewBalancingTrade(long, long, List, List)}, and
	 * {@link BalanceTradeNetworkAction#execute()} 
	 * will also return balancing trades that may result in negative assets
	 * @param isToAllowNegativeInBalanceTrade the isToAllowNegativeInBalanceTrade to set
	 */
	public void setToAllowNegativeInBalanceTrade(
			boolean isToAllowNegativeInBalanceTrade) {
		this.isToAllowNegativeInBalanceTrade = isToAllowNegativeInBalanceTrade;
	}

	/**
	 * If true, {@link #previewBalancingTrades(long, long, List, List)},
	 * {@link #previewBalancingTrade(long, long, List, List)},  and
	 * {@link BalanceTradeNetworkAction#execute()}
	 * will also return balancing trades that may result in negative assets
	 * @return the isToAllowNegativeInBalanceTrade
	 */
	public boolean isToAllowNegativeInBalanceTrade() {
		return isToAllowNegativeInBalanceTrade;
	}

	/**
	 *  If true, {@link #collapseSimilarBalancingTrades(List)} will try to group similar balancing trades into 1 trade
	 * @param isToCollapseSimilarBalancingTrades the isToCollapseSimilarBalancingTrades to set
	 */
	public void setToCollapseSimilarBalancingTrades(
			boolean isToCollapseSimilarBalancingTrades) {
		this.isToCollapseSimilarBalancingTrades = isToCollapseSimilarBalancingTrades;
	}

	/**
	 *  If true, {@link #collapseSimilarBalancingTrades(List)} will try to group similar balancing trades into 1 trade
	 * @return the isToCollapseSimilarBalancingTrades
	 */
	public boolean isToCollapseSimilarBalancingTrades() {
		return isToCollapseSimilarBalancingTrades;
	}

	/**
	 * @param probabilityErrorMarginBalanceTrade the probabilityErrorMarginBalanceTrade to set
	 */
	public void setProbabilityErrorMarginBalanceTrade(
			float probabilityErrorMarginBalanceTrade) {
		this.probabilityErrorMarginBalanceTrade = probabilityErrorMarginBalanceTrade;
	}

	
	/**
	 * This is the error margin for probabilities regarding the {@link #previewBalancingTrade(long, long, List, List)}
	 * (because operations regarding a balancing trade requires more precision).
	 * This value is used in {@link #splitTradesByAssumptions(List)}
	 * so that probabilities whose differences are within this margin will be considered as equal.
	 * @return the probabilityErrorMarginBalanceTrade
	 */
	public float getProbabilityErrorMarginBalanceTrade() {
		return probabilityErrorMarginBalanceTrade;
	}

	/**
	 * If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)}
	 * will attempt to use house account to run corrective trades when the old probabilities provided by the caller is different
	 * from the actual probabilities retrieved from the Bayes net before trade.
	 * @param isToUseCorrectiveTrades the isToUseCorrectiveTrades to set
	 */
	public void setToUseCorrectiveTrades(boolean isToUseCorrectiveTrades) {
		this.isToUseCorrectiveTrades = isToUseCorrectiveTrades;
	}

	/**
	 * If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)}
	 * will attempt to use house account to run corrective trades when the old probabilities provided by the caller is different
	 * from the actual probabilities retrieved from the Bayes net before trade.
	 * @return the isToUseCorrectiveTrades
	 */
	public boolean isToUseCorrectiveTrades() {
		return isToUseCorrectiveTrades;
	}

	/**
	 * If true, {@link #commitNetworkActions(long, boolean)} will place all {@link AddCashNetworkAction} before trades.
	 * This is useful when {@link #isToLazyInitializeUsers()} is set to true, because when 
	 * users are lazily initialized, calling {@link #addCash(Long, Date, long, float, String)}
	 * before the trades is supposedly faster, because adding cash to uninitialized users is faster.
	 * @return the isToSortAddCashAction
	 */
	public boolean isToSortAddCashAction() {
		return isToSortAddCashAction;
	}

	/**
	 * If true, {@link #commitNetworkActions(long, boolean)} will place all {@link AddCashNetworkAction} before trades.
	 * This is useful when {@link #isToLazyInitializeUsers()} is set to true, because when 
	 * users are lazily initialized, calling {@link #addCash(Long, Date, long, float, String)}
	 * before the trades is supposedly faster, because adding cash to uninitialized users is faster.
	 * @param isToSortAddCashAction the isToSortAddCashAction to set
	 */
	public void setToSortAddCashAction(boolean isToSortAddCashAction) {
		this.isToSortAddCashAction = isToSortAddCashAction;
	}

	/**
	 * If true, {@link RebuildNetworkAction#execute()} will "see" trades which are in the same transaction and created after the rebuild action.
	 * In other words, it will attempt to connect questions used in trades which happened after the occurrence of the last
	 * {@link AddQuestionNetworkAction} or {@link AddQuestionAssumptionNetworkAction}, if they are in the same transaction.
	 * @return the isToLookAheadForTradesCreatedAfterRebuild
	 */
	public boolean isToLookAheadForTradesCreatedAfterRebuild() {
		return isToLookAheadForTradesCreatedAfterRebuild;
	}

	/**
	 * If true, {@link RebuildNetworkAction#execute()} will "see" trades which are in the same transaction and created after the rebuild action.
	 * In other words, it will attempt to connect questions used in trades which happened after the occurrence of the last
	 * {@link AddQuestionNetworkAction} or {@link AddQuestionAssumptionNetworkAction}, if they are in the same transaction.
	 * @param isToLookAheadForTradesCreatedAfterRebuild the isToLookAheadForTradesCreatedAfterRebuild to set
	 */
	public void setToLookAheadForTradesCreatedAfterRebuild(
			boolean isToLookAheadForTradesCreatedAfterRebuild) {
		this.isToLookAheadForTradesCreatedAfterRebuild = isToLookAheadForTradesCreatedAfterRebuild;
	}

	/**
	 * If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)} will
	 * throw exception when the question has probability 0 or 1 in any state
	 * @return the isToThrowExceptionInTradesToResolvedQuestions
	 */
	public boolean isToThrowExceptionInTradesToResolvedQuestions() {
		return isToThrowExceptionInTradesToResolvedQuestions;
	}

	/**
	 * If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)} will
	 * throw exception when the question has probability 0 or 1 in any state
	 * @param isToThrowExceptionInTradesToResolvedQuestions the isToThrowExceptionInTradesToResolvedQuestions to set
	 */
	public void setToThrowExceptionInTradesToResolvedQuestions(
			boolean isToThrowExceptionInTradesToResolvedQuestions) {
		this.isToThrowExceptionInTradesToResolvedQuestions = isToThrowExceptionInTradesToResolvedQuestions;
	}

	/**
	 * If true, cash of each user will be stored when questions are resolved
	 * @return the isToStoreCashBeforeResolveQuestion
	 */
	public boolean isToStoreCashBeforeResolveQuestion() {
		return isToStoreCashBeforeResolveQuestion;
	}

	/**
	 * If true, cash of each user will be stored when questions are resolved
	 * @param isToStoreCashBeforeResolveQuestion the isToStoreCashBeforeResolveQuestion to set
	 */
	public void setToStoreCashBeforeResolveQuestion(
			boolean isToStoreCashBeforeResolveQuestion) {
		this.isToStoreCashBeforeResolveQuestion = isToStoreCashBeforeResolveQuestion;
	}

	/**
	 * This is a map which stores which user has gained how much cash in what resolved questions. The mapping is: userId -> (questionId -> cash gain).
	 * @return the userIdToResolvedQuestionCashGainMap
	 */
	protected Map<Long, Map<Long,Float>> getUserIdToResolvedQuestionCashGainMap() {
		return userIdToResolvedQuestionCashGainMap;
	}

	/**
	 * This is a map which stores which user has gained how much cash in what resolved questions. The mapping is: userId -> (questionId -> cash gain).
	 * @param userIdToResolvedQuestionCashGainMap the userIdToResolvedQuestionCashGainMap to set
	 */
	protected void setUserIdToResolvedQuestionCashGainMap(
			Map<Long, Map<Long,Float>> userIdToResolvedQuestionCashGainMap) {
		this.userIdToResolvedQuestionCashGainMap = userIdToResolvedQuestionCashGainMap;
	}

	/**
	 * This is a map which stores what was the user's cash before resolved questions. The mapping is: userId -> (questionId -> cash).
	 * @return the userIdToResolvedQuestionCashBeforeMap
	 */
	protected Map<Long, Map<Long,Float>> getUserIdToResolvedQuestionCashBeforeMap() {
		return userIdToResolvedQuestionCashBeforeMap;
	}

	/**
	 * This is a map which stores what was the user's cash before resolved questions. The mapping is: userId -> (questionId -> cash).
	 * @param userIdToResolvedQuestionCashBeforeMap the userIdToResolvedQuestionCashBeforeMap to set
	 */
	protected void setUserIdToResolvedQuestionCashBeforeMap(
			Map<Long, Map<Long,Float>> userIdToResolvedQuestionCashBeforeMap) {
		this.userIdToResolvedQuestionCashBeforeMap = userIdToResolvedQuestionCashBeforeMap;
	}

	/**
	 * If true, {@link PrintNetStatisticsNetworkAction} will be inserted into the actions to be executed in {@link #commitNetworkActions(long, boolean)}
	 * immediately after the last occurrence of {@link StructureChangeNetworkAction}
	 * @return the isToPrintNetStatisticsAfterLastStructureChange
	 */
	public boolean isToPrintNetStatisticsAfterLastStructureChange() {
		return isToPrintNetStatisticsAfterLastStructureChange;
	}

	/**
	 * If true, {@link PrintNetStatisticsNetworkAction} will be inserted into the actions to be executed in {@link #commitNetworkActions(long, boolean)}
	 * immediately after the last occurrence of {@link StructureChangeNetworkAction}
	 * @param isToPrintNetStatisticsAfterLastStructureChange the isToPrintNetStatisticsAfterLastStructureChange to set
	 */
	public void setToPrintNetStatisticsAfterLastStructureChange(
			boolean isToPrintNetStatisticsAfterLastStructureChange) {
		this.isToPrintNetStatisticsAfterLastStructureChange = isToPrintNetStatisticsAfterLastStructureChange;
	}

	/**
	 * If false, {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} will just return for uninitialized 
	 * users which has no position even after other (non-executed) actions the same transaction.
	 * If true, {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} will add an action into the transaction
	 * even though it is an uninitialized user and no trade is registered before the balancing trade.
	 * Setting this to true will make {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} to run
	 * a bit faster in huge transactions, because it won't check existence of trades before the {@link #doBalanceTrade(Long, Date, String, long, long, List, List)}
	 * It is true by default.
	 * @return the isToAddUninitializedUserInBalanceTradeOfHugeTransaction
	 */
	public boolean isToAddUninitializedUserInBalanceTradeOfHugeTransaction() {
		return this.isToAddUninitializedUserInBalanceTradeOfHugeTransaction;
	}

	/**
	 * If false, {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} will just return for uninitialized 
	 * users which has no position even after other (non-executed) actions the same transaction
	 * If true, {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} will add an action into the transaction
	 * even though it is an uninitialized user and no trade is registered before the balancing trade.
	 * Setting this to true will make {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} to run
	 * a bit faster in huge transactions, because it won't check existence of trades before the {@link #doBalanceTrade(Long, Date, String, long, long, List, List)}
	 * It is true by default.
	 * @param isToAddUninitializedUserInBalanceTradeOfHugeTransaction the isToAddUninitializedUserInBalanceTradeOfHugeTransaction to set
	 */
	public void setToAddUninitializedUserInBalanceTradeOfHugeTransaction(
			boolean isToAddUninitializedUserInBalanceTradeOfHugeTransaction) {
		this.isToAddUninitializedUserInBalanceTradeOfHugeTransaction = isToAddUninitializedUserInBalanceTradeOfHugeTransaction;
	}

	/**
	 * This mapping is an index to efficiently search for questions which will be created
	 * in the same transaction, in order to check consistency of presence of questions.
	 * @return the questionsToBeCreatedInTransaction
	 */
	protected Map<Long, Set<Long>> getQuestionsToBeCreatedInTransaction() {
		return questionsToBeCreatedInTransaction;
	}

	/**
	 * @param questionsToBeCreatedInTransaction the questionsToBeCreatedInTransaction to set
	 */
	protected void setQuestionsToBeCreatedInTransaction(
			Map<Long, Set<Long>> questionsToBeCreatedInTransaction) {
		this.questionsToBeCreatedInTransaction = questionsToBeCreatedInTransaction;
	}



}
