package edu.gmu.ace.scicast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import unbbayes.io.BaseIO;
import unbbayes.io.IPrintStreamBuilder;
import unbbayes.io.IReaderBuilder;
import unbbayes.io.NetIO;
import unbbayes.io.StringPrintStreamBuilder;
import unbbayes.io.StringReaderBuilder;
import unbbayes.io.ValueTreeNetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IJunctionTree;
import unbbayes.prs.bn.IJunctionTreeBuilder;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.IncrementalJunctionTreeAlgorithm;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.LoopyJunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.StructureOnlyJunctionTree;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.ITableFunction;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor.CliqueEvidenceUpdater;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor.NoCliqueException;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.bn.inference.extension.AbstractAssetNetAlgorithm.EvidenceType;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm.ExpectedAssetCellMultiplicationListener;
import unbbayes.prs.bn.inference.extension.AssetPropagationInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.AssetPropagationInferenceAlgorithm.AssetPropagationInferenceAlgorithmMemento;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm.IAssetNetAlgorithmMemento;
import unbbayes.prs.bn.inference.extension.IQValuesToAssetsConverter;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
import unbbayes.prs.bn.valueTree.IValueTree;
import unbbayes.prs.bn.valueTree.IValueTreeFactionChangeEvent;
import unbbayes.prs.bn.valueTree.IValueTreeFactionChangeListener;
import unbbayes.prs.bn.valueTree.IValueTreeNode;
import unbbayes.prs.bn.valueTree.ValueTreeNode;
import unbbayes.prs.bn.valueTree.ValueTreeProbabilisticNode;
import unbbayes.prs.builder.INodeBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;
import unbbayes.util.SingleValueList;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import edu.gmu.ace.scicast.ScoreSummary.SummaryContribution;

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

	/** This is the default builder for creating instances of {@link ValueTreeProbabilisticNode} (i.e. nodes using value trees) */
	public static final INodeBuilder DEFAULT_VALUE_TREE_PROB_NODE_BUILDER = new INodeBuilder() {
		/**
		 * @see unbbayes.prs.builder.INodeBuilder#getNodeClass()
		 */
		public Class getNodeClass() {
			return ValueTreeProbabilisticNode.class;
		}
		
		/**
		 * @see unbbayes.prs.builder.INodeBuilder#buildNode()
		 */
		public Node buildNode() {
			return new ValueTreeProbabilisticNode();
		}
	};

	/** This object will be used in order to initialize {@link #getDefaultCPTNormalizer()} */
	public static final ITableFunction DEFAULT_CPT_NORMALIZER = new NormalizeTableFunction();

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
	
	/**Default value of {@link #isToThrowExceptionOnInvalidAssumptions()}*/
	public static final boolean IS_TO_THROW_EXCEPTION_ON_INVALID_ASSUMPTION = true;
	private boolean isToThrowExceptionOnInvalidAssumptions = IS_TO_THROW_EXCEPTION_ON_INVALID_ASSUMPTION;

//	/** Set this value to < 0 to disable this feature */
//	private float forcedInitialQValue = 100.0;
//
//	private double currencyMultiplier = 1.0;

	private boolean isToDoFullPreview = false;

	private Map<Long, Set<Long>> tradedQuestionsMap;

	private boolean isToReturnEVComponentsAsScoreSummary = false;


	private Map<Long,StatePair> resolvedQuestionsAndNumberOfStates;


	private boolean isToIntegrateConsecutiveResolutions = true;
	
	private boolean isToAggregateAddQuestionAction = false; //true;


	private boolean isToRetriveOnlyTradeHistory = true;

	/** This is the default initial value of {@link #getMaxConditionalProbHistorySize()} */
	public static int DEFAULT_MAX_CONDITIONAL_PROB_HISTORY_SIZE = 10;

	private int maxConditionalProbHistorySize = DEFAULT_MAX_CONDITIONAL_PROB_HISTORY_SIZE;

	private HashMap<Clique, List<ParentActionPotentialTablePair>> lastNCliquePotentialMap;


	private BaseIO io = new NetIO();
	{
		((NetIO)io).setDefaultNodeNamePrefix("N");
	}
	

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
	private boolean isToUseCorrectiveTrades = false;

	/** If true, {@link #commitNetworkActions(long, boolean)} will place all {@link AddCashNetworkAction} before trades. */
	private boolean isToSortAddCashAction = true;
	
	/** If true, {@link RebuildNetworkAction#execute()} will "see" trades which are in the same transaction and created after the rebuild action. */
	private boolean isToLookAheadForTradesCreatedAfterRebuild = true;

	/** If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)} will
	 * throw exception when the question has probability 0 or 1 in any state */
	private boolean isToThrowExceptionInTradesToResolvedQuestions = false;
	
	/** If true, cash of each user will be stored when questions are resolved */
	private boolean isToStoreCashBeforeResolveQuestion = false;
	
	/** This is a map which stores which user has gained how much cash in what resolved questions. The mapping is: userId -> (questionId -> cash gain). */
	private Map<Long, Map<Long,Float>> userIdToResolvedQuestionCashGainMap;
	
	/** This is a map which stores what was the user's cash before resolved questions. The mapping is: userId -> (questionId -> cash). */
	private Map<Long, Map<Long,Float>> userIdToResolvedQuestionCashBeforeMap;

	/** If true, {@link PrintNetStatisticsNetworkAction} will be inserted into the actions to be executed in {@link #commitNetworkActions(long, boolean)}
	 * immediately after the last occurrence of {@link StructureChangeNetworkAction} */
	private boolean isToPrintNetStatisticsAfterLastStructureChange = false;
	
	/** When true, {@link #addQuestionAssumption(Long, Date, long, List, List)} won't try to re-run the trades.
	 * however, when {@link #isToAddArcsOnlyToProbabilisticNetwork()} is false, this may result in a non-optimal BN. */
	private boolean isToAddArcsWithoutReboot = false;
	
	/** If this is true and {@link #isToAddArcsWithoutReboot()} is also true, then an algorithm for
	 * adding arcs without re-running the history will be executed, and the resulting BN will be near optimal,
	 * because for probabilistic networks it is easier to do such optimization. */
	private boolean isToAddArcsOnlyToProbabilisticNetwork = true;//false;

	/** If true, {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} will just return for uninitialized 
	 * users which has no position even after other (non-executed) actions the same transaction */
	private boolean isToAddUninitializedUserInBalanceTradeOfHugeTransaction = true;

	private Map<Long, Set<AddQuestionNetworkAction>> questionsToBeCreatedInTransaction = new HashMap<Long, Set<AddQuestionNetworkAction>>();

	/** If true, probabilities will be printed after execution of actions */
	private boolean isToPrintProbs = false;

	private boolean isToPrintRootClique = false;

	private boolean isToTraceHistory = true;

	private boolean isToExportOnlyCurrentSharedProbabilisticNet = false;

	private NetIO netIOToExportSharedNetToSting = new ValueTreeNetIO();
	{
		netIOToExportSharedNetToSting.setDefaultNodeNamePrefix("N");
	}
	
	private boolean isToAddArcsOnAddTrade = true;
	private boolean isToAddVirtualArcsOnAddTrade = false;

	private ITableFunction defaultCPTNormalizer = DEFAULT_CPT_NORMALIZER;
	
	/** This collection will hold what edges are tagged as virtual. This mapping is from child to parent */
//	private Map<INode,List<INode>> virtualParentMapping = new HashMap<INode,List<INode>>();
	private List<Edge> virtualArcs = null;//new ArrayList<Edge>();


	/** If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)}
	 * will call {@link #addQuestionAssumption(Long, Date, long, List, List)} in order to connect nodes that are not connected, but was used in trades*/
	private boolean isToAddArcsOnAddTradeAndUpdateJT = true;

	/** If true, {@link #importState(String)} and {@link #exportState()} will consider compressed format (e.g. zip and then base64 encoding) */
	private boolean isToCompressExportedState = true;
	private boolean isToReturnIdentifiersInExportState = false;

	private boolean isToAllowNonBayesianUpdate = true;

	/** Folder where files generated by {@link #exportState()} will be stored, and where {@link #importState(String)} will look for files when {@link #isToReturnIdentifiersInExportState()} is true. */
	private String statesFolderName = "states/";

	/** Extension of files generated by {@link #exportState()} and to be read by {@link #importState(String)} when {@link #isToReturnIdentifiersInExportState()} is true */
	private String exportedStateFileExtension = ".state";
	
	/** If {@link #getComplexityFactor(Map)} is called for a node that does not exist, this will be considered the default size (number of states) of a node. */
	private int defaultNodeSize = 13;

	private boolean isToThrowExceptionOnDynamicJunctionTreeCompilationFailure = false;	// set this to true to debug dynamic junction tree compilation

	private int dynamicJunctionTreeNetSizeThreshold = 1;
	
	private String defaultComplexityFactorName = COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE; //COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE;

	private boolean isToUseMaxForSubnetsInLinkSuggestion = true;
	
	/** If false, dynamic/incremental junction tree compilation will be disabled when value trees are used */
	private boolean isToUseDynamicJunctionTreeWithValueTrees = false;

	private Map<String,Map<String,Double>> singleExistingArcComplexityCache = new HashMap<String, Map<String,Double>>();

	/** If false, then {@link #loadApplicationPropertyFile()} will be disabled. The default is true. */
	private boolean isToLoadApplicationPropertyFile = true;
	
	
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
		return MarkovEngineImpl.getInstance(logBase, currencyConstant, initialUserAssets, IS_TO_THROW_EXCEPTION_ON_INVALID_ASSUMPTION);
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#initialize()
	 */
	public synchronized boolean initialize() {
		// reset counter of transactions
		setTransactionCounter(0);
		
		// prepare map storing network actions
		setNetworkActionsMap(new ConcurrentHashMap<Long, List<NetworkAction>>());	// concurrent hash map is known to be thread safe yet fast.
		
		// reset map of history indexed by question
		if (getNetworkActionsIndexedByQuestions() != null) {
			getNetworkActionsIndexedByQuestions().clear();
		}
		
		setExecutedActions(new ArrayList<NetworkAction>());	// initialize list of network actions ordered by the moment of execution
		
		setProbabilisticNetwork(new ProbabilisticNetwork("DAGGRE"));
		// disable log
		getProbabilisticNetwork().setCreateLog(false);
		// disable hierarchic tree
		getProbabilisticNetwork().setHierarchicTree(null);
		
		// prepare inference algorithm for the BN
		IncrementalJunctionTreeAlgorithm junctionTreeAlgorithm = new IncrementalJunctionTreeAlgorithm(getProbabilisticNetwork());
		// whether we shall enable dynamic junction tree compilation or not
		junctionTreeAlgorithm.setDynamicJunctionTreeNetSizeThreshold(getDynamicJunctionTreeNetSizeThreshold());
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
		
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			// this method will make all other flags consistent each other
			this.setToAddArcsOnlyToProbabilisticNetwork(true);
		}
		
		// reset cache of complexity factors of each existing arc
		Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
		if (arcComplexityCache == null) {
			setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
		} else {
			arcComplexityCache.clear();
		}
		
		// for debugging
		this.setToThrowExceptionOnDynamicJunctionTreeCompilationFailure(isToThrowExceptionOnDynamicJunctionTreeCompilationFailure());
		
		this.loadApplicationPropertyFile();
		
		return true;
	}

	/**
	 * This method will look for "application.property" file and will read some attributes from it.
	 * @see ApplicationPropertyHolder
	 */
	public void loadApplicationPropertyFile() {
		if (!isToLoadApplicationPropertyFile()) {
			return;
		}
		try {
			// make sure the property is fresh
			ApplicationPropertyHolder.reloadProperties();
			// extract the application property object
			Properties property = ApplicationPropertyHolder.getProperty();
			if (property != null) {
				// iterate on all specified properties
				for (Entry<Object, Object> propertyEntry : property.entrySet()) {
					try {
						// the prefix for properties designed for this class is the name of this class with a period for separating the name.
						if (propertyEntry.getKey().toString().startsWith(this.getClass().getName()+".")) {
							// extract the name of the key without the prefix
							String name = propertyEntry.getKey().toString().substring(this.getClass().getName().length()+1);
							
							try {
								// first, try a getter/setter
								// extract the getter, so that we can extract the corresponding setter later
								// the name of the getter method
								String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
								Method getterMethod = null;
								try {
									getterMethod = this.getClass().getMethod(getterName);
								} catch (NoSuchMethodException e) {
									try {
										Debug.println(getClass(), "Getter " + getterName + " did not exist. Trying " + name +"...", e);
										// try the name directly
										getterName = name;
										getterMethod = this.getClass().getMethod(getterName);
									} catch (NoSuchMethodException e2) {
										// use "is" instead.
										getterName = "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
										Debug.println(getClass(), "Getter " + name + " did not exist. Trying " + getterName + " instead...", e);
										getterMethod = this.getClass().getMethod(getterName);
									}
								}
								// find the corresponding setter method. The type of argument will be extracted from the type of getter
								if (getterMethod != null) {
									String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
									Method setterMethod = null;
									try {
										setterMethod = this.getClass().getMethod(setterName, getterMethod.getReturnType());
									} catch (NoSuchMethodException e) {
										Debug.println(getClass(), "Setter " + setterName + " didn't exist.", e);
										setterName = "set" + name.substring(2);
										Debug.println(getClass(), "Trying " + setterName + " instead...", e);
										setterMethod = this.getClass().getMethod(setterName, getterMethod.getReturnType());
									}
									// call the setter
									if (getterMethod.getReturnType().isAssignableFrom(boolean.class)) {
										setterMethod.invoke(this, Boolean.parseBoolean(propertyEntry.getValue().toString()));
									} else if (getterMethod.getReturnType().isAssignableFrom(int.class)) {
										setterMethod.invoke(this, Integer.parseInt(propertyEntry.getValue().toString()));
									} else if (getterMethod.getReturnType().isAssignableFrom(long.class)) {
										setterMethod.invoke(this, Long.parseLong(propertyEntry.getValue().toString()));
									} else if (getterMethod.getReturnType().isAssignableFrom(float.class)) {
										setterMethod.invoke(this, Float.parseFloat(propertyEntry.getValue().toString()));
									} else if (getterMethod.getReturnType().isAssignableFrom(double.class)) {
										setterMethod.invoke(this, Double.parseDouble(propertyEntry.getValue().toString()));
									} else if (getterMethod.getReturnType().isAssignableFrom(String.class)) {
										setterMethod.invoke(this, propertyEntry.getValue().toString());
									} else {
										System.err.println("Unknown method in application.properties: " + propertyEntry);
									}
								}
							} catch (Exception e) {
								Debug.println(getClass(), propertyEntry + " was not specifying a valid method.", e);
								// if it was not a getter/setter, check if the property is a field
								try {
									Field field = this.getClass().getField(name);
									if (field.getType().isAssignableFrom(boolean.class)) {
										field.set(this, Boolean.parseBoolean(propertyEntry.getValue().toString()));
									} else if (field.getType().isAssignableFrom(int.class)) {
										field.set(this, Integer.parseInt(propertyEntry.getValue().toString()));
									} else if (field.getType().isAssignableFrom(long.class)) {
										field.set(this, Long.parseLong(propertyEntry.getValue().toString()));
									} else if (field.getType().isAssignableFrom(float.class)) {
										field.set(this, Float.parseFloat(propertyEntry.getValue().toString()));
									} else if (field.getType().isAssignableFrom(double.class)) {
										field.set(this, Double.parseDouble(propertyEntry.getValue().toString()));
									} else if (field.getType().isAssignableFrom(String.class)) {
										field.set(this, propertyEntry.getValue().toString());
									} else {
										System.err.println("Unknown field in application.properties: " + propertyEntry);
									}
								} catch (NoSuchFieldException e2) {
									e2.printStackTrace();
								}
							}
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#startNetworkActions()
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#commitNetworkActions(long)
	 */
	public synchronized boolean commitNetworkActions(long transactionKey)
			throws IllegalArgumentException, ZeroAssetsException {
		return this.commitNetworkActions(transactionKey, true);
	}
	
	/**
	 * @param isToRebuildNetOnStructureChange : if false, it does not rebuild network when a change in structure is detected.
	 * If true, it will rebuild (re-generate network structure and call {@link IInferenceAlgorithm#run()}).
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#commitNetworkActions(long)
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
			List<AddQuestionAssumptionNetworkAction> arcActions = new ArrayList<AddQuestionAssumptionNetworkAction>();	// will store actions which adds arcs (so that we can aggregate them if possible)
			boolean isToRebuildFromHistory = !isToAddArcsWithoutReboot;	// if an action which is set as a trigger for rebuild is found, this flag will turn on
			for (NetworkAction action : actions) {
				if (action.isTriggerForRebuild()) {
					// there is at least one trigger for re-running history, so remember that and instantiate a RebuildNetworkAction afterwards.
					isToRebuildFromHistory = true;
				}
				if (action.isStructureConstructionAction()) {
					if (isToAggregateAddQuestionAction()
							&& !isToRebuildFromHistory 
							&& isToAddArcsOnlyToProbabilisticNetwork()
							&& (action instanceof AddQuestionAssumptionNetworkAction)) {
						// currently, we can only aggregate AddQuestionAssumption under these conditions
						arcActions.add((AddQuestionAssumptionNetworkAction) action);
					} else {
						netChangeActions.add(action);
					}
				} else if (isToSortAddCashAction && (action instanceof AddCashNetworkAction)) {
					addCashActions.add(action);
				} else {
					// Note: if isToSortAddCashAction() == false, AddCashNetworkAction will be included here
					otherActions.add(action);
				}
			}
			
			// if we aggregated arcs, then add aggregated action at the end of the list of actions storing those who will change net structure
			if (!arcActions.isEmpty()) {
				netChangeActions.add(new AggregatedQuestionAssumptionNetworkAction(arcActions));
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
						&& ((ResolveQuestionNetworkAction)action).isHardEvidenceAction()	// can only integrate hard evidence, because soft evidence must be executed in sequence) {
						&& (i+1 < actions.size()) && (actions.get(i+1) instanceof ResolveQuestionNetworkAction)
						&& ((ResolveQuestionNetworkAction)actions.get(i+1)).isHardEvidenceAction() // can only integrate hard evidence, because soft evidence must be executed in sequence) {
						&& !action.getQuestionId().equals(actions.get(i+1).getQuestionId())) {	// do not aggregate consecutive settlements to same question (possible in negative hard evidence)
					
					// store the series of resolutions
					List<ResolveQuestionNetworkAction> consecutiveResolutions = new ArrayList<MarkovEngineImpl.ResolveQuestionNetworkAction>();
					// add the 2 consecutive resolutions to the list
					consecutiveResolutions.add((ResolveQuestionNetworkAction) action);
					consecutiveResolutions.add((ResolveQuestionNetworkAction)actions.get(++i));
					
					// store what question Ids were already handled, so that we stop when there were 2 settlements to same question within same transaction
					// (this can happen because settling a state to 0% won't settle the whole question)
					Set<Long> questionIdsConsidered = new HashSet<Long>();
					questionIdsConsidered.add(action.getQuestionId());
					questionIdsConsidered.add(actions.get(i).getQuestionId());	// because we called ++i before, now i points to the second action
					
					// check if there are more resolutions following the above two
					while (++i < actions.size()) {
						if (actions.get(i) instanceof ResolveQuestionNetworkAction 
								&& ((ResolveQuestionNetworkAction)actions.get(i)).isHardEvidenceAction() 	// can only integrate hard evidence, because soft evidence must be executed in sequence
								&& !questionIdsConsidered.contains(actions.get(i).getQuestionId())) {		// do not aggregate if there is a settlement to this question already in the aggregated instance
							consecutiveResolutions.add((ResolveQuestionNetworkAction) actions.get(i));
							questionIdsConsidered.add(actions.get(i).getQuestionId());	// do not forget to update the set of question IDs handled already.
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
					if (isToPrintProbs) {
						// print date, isBalance, marginals
						System.out.print(action.getWhenCreated().toString());
						System.out.print(","+((action instanceof BalanceTradeNetworkAction)?"balance":((action instanceof ResolveQuestionNetworkAction)?"resolve":"trade")));
						for (Node node : getProbabilisticNetwork().getNodes()) {
							for (int j = 0; j < node.getStatesSize(); j++) {
								System.out.print(","+((TreeVariable)node).getMarginalAt(j));
							}
						}
						
						if (isToPrintRootClique) {
							PotentialTable table = getProbabilisticNetwork().getJunctionTree().getCliques().get(0).getProbabilityFunction();
							for (int j = 0; j < table.tableSize(); j++) {
								System.out.print("," + table.getValue(j));
							}
						}
						System.out.println();
					}
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
			
			// if it is configured not to use history, then clear it here.
			// TODO fix strong dependencies of methods in NetworkAction to getExecutedActions(), so that they can work without it as well
			if (!isToTraceHistory()) {
				synchronized (getExecutedActions()) {
					getExecutedActions().clear();
				}
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
				// getVirtualTradeToAffectedQuestionsMap() is a mapping from virtual trade to questions
				for (VirtualTradeAction virtualTrade : getVirtualTradeToAffectedQuestionsMap().keySet()) {
					for (Long relatedQuestion : getVirtualTradeToAffectedQuestionsMap().get(virtualTrade)) {
						// delete from the "normal" mapping
						removeNetworkActionFromQuestionMap(virtualTrade, relatedQuestion);
					}
				}
				// clean the "inverse" mapping
				getVirtualTradeToAffectedQuestionsMap().clear();
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addQuestion(long, java.util.Date, long, int, java.util.List)
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
			AddQuestionNetworkAction questionAction = new AddQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs);
			this.addNetworkAction(transactionKey, questionAction);
			
			// also add into index of questions being created in transaction
			synchronized (getQuestionsToBeCreatedInTransaction()) {
				Set<AddQuestionNetworkAction> set = getQuestionsToBeCreatedInTransaction().get(transactionKey);
				if (set == null) {
					set = new HashSet<AddQuestionNetworkAction>();
				}
				set.add(questionAction);
				getQuestionsToBeCreatedInTransaction().put(transactionKey, set);
			}
		}
		
		return true;
	}
	
	/**
	 * TODO value tree and dynamic/incremental JT compilation doesn't seem to work well together. Use {@link #setDynamicJunctionTreeNetSizeThreshold(int)} with high value to disable dynamic/incremental JT compilation.
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addQuestion(java.lang.Long, java.util.Date, long, int, java.util.List, java.lang.String)
	 */
	public boolean addQuestion(Long transactionKey, Date occurredWhen, long questionId, int numberStates, List<Float> initProbs, String structure ) throws IllegalArgumentException {
		// delegate to old method if no value tree structure was provided
		if (structure == null || structure.trim().isEmpty()) {
			return addQuestion(transactionKey, occurredWhen, questionId, numberStates, initProbs);
		}
		// TODO implement assets for value trees.
		if (!isToAddArcsOnlyToProbabilisticNetwork()) {
			throw new UnsupportedOperationException("Current implementation of value trees cannot be used with assets, so please turn on the flag \"isToAddArcsOnlyToProbabilisticNetwork\"");
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
//		if (initProbs != null && !initProbs.isEmpty()) {
//			float sum = 0;
//			for (Float prob : initProbs) {
//				if (prob < 0 || prob > 1) {
//					throw new IllegalArgumentException("Invalid probability declaration found: " + prob);
//				}
//				sum += prob;
//			}
//			// check if sum of initProbs is 1 (with error margin)
//			if (!(((1 - getProbabilityErrorMargin()) < sum) && (sum < (1 + getProbabilityErrorMargin())))) {
//				throw new IllegalArgumentException("Inconsistent prior probability: " + sum);
//			}
//		}

		// parse the string of value tree structure and convert it to a tree of IValueTreeNode
		List<IValueTreeNode> childrenOfRootOfValueTree = new ArrayList<IValueTreeNode>();	// these will be the 1st level children (children of root)
		List<IValueTreeNode> shadowNodes = new ArrayList<IValueTreeNode>();					// these will be the shadow nodes in childrenOfRootOfValueTree or in its descendants
		
		// this method will fill the last two lists.
		this.fillValueTreeFromString(structure, childrenOfRootOfValueTree, shadowNodes, questionId);
		
		
		// instantiate the action object for adding a question
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new AddValueTreeQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs, childrenOfRootOfValueTree, shadowNodes));
			this.commitNetworkActions(transactionKey);
		} else {
			AddQuestionNetworkAction questionAction = new AddValueTreeQuestionNetworkAction(transactionKey, occurredWhen, questionId, numberStates, initProbs, childrenOfRootOfValueTree, shadowNodes);
			this.addNetworkAction(transactionKey, questionAction);
			
			// also add into index of questions being created in transaction
			synchronized (getQuestionsToBeCreatedInTransaction()) {
				Set<AddQuestionNetworkAction> set = getQuestionsToBeCreatedInTransaction().get(transactionKey);
				if (set == null) {
					set = new HashSet<AddQuestionNetworkAction>();
				}
				set.add(questionAction);
				getQuestionsToBeCreatedInTransaction().put(transactionKey, set);
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param structure
	 * The format will probably be
	 * [3 [ 4 [ 3 [ 31 28 30] [..... ].....    3 years, 4 q in first year, 3 m in first quarter, 31, 28 30 days by month , etc...
	 * <br/>
	 * And then a list of mappings to the shadow nodes, for example if we exposed the quarters: 
	 * [0,0 ], [0,1],[0,2],[0,3],[1,0], [1,1],[1,2],[1,3],[2,0], [2,1],[2,2],[2,3]
	 * @param childrenOfRootOfValueTree : output variable. Will be filled with the 1st level children of root (i.e.
	 * the list itself will contain the immediate children of the root node), and each node will be filled
	 * with the respective hierarchy of value tree nodes (i.e. children and parents also correctly filled).
	 * @param shadowNodes : output variable. Will be filled with nodes in the hierarchy of childrenOfRootOfValueTree
	 * which shall be considered as shadow nodes (i.e. the states which will be visible from other nodes in Bayes net).
	 */
	public void fillValueTreeFromString(String structure, List<IValueTreeNode> childrenOfRootOfValueTree, List<IValueTreeNode> shadowNodes, long rootId) {
		if (structure == null || structure.trim().isEmpty()) {
			// do nothing
			return;
		}
		if (childrenOfRootOfValueTree == null) {
			// this list will be ignored by caller, but instantiate it here just in order to unificate the code which will parse the string
			childrenOfRootOfValueTree = new ArrayList<IValueTreeNode>();
		}
		if (shadowNodes == null) {
			// this list will be ignored by caller, but instantiate it here just in order to unify the code which will parse the string
			shadowNodes = new ArrayList<IValueTreeNode>();
		}
		// use a tokenizer to read
		StreamTokenizer st = new StreamTokenizer(new StringReader(structure));
		st.ordinaryChar('[');
		st.ordinaryChar(']');
		st.eolIsSignificant(false);
		
		try {
			// read only the structure
			this.readStructureRootBlock(st,childrenOfRootOfValueTree, rootId);
			
			// then, read the shadow node block
			this.readShadowNodeBlock(st,childrenOfRootOfValueTree, shadowNodes);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not parse value tree structure",e);
		}
		
	}


	/**
	 * Example of expected format: "[ 2 [ 3 [ 0 2 [ 0 2 ] 2 ] 2 ] ]"
	 * <br/>
	 * <br/>
	 * This method reads the root_block statement in the BNF:
	 * <pre>
	 * <code>
	 * root_block ::= "[" number [ "[" child_block "]" ] "]"
	 * child_block ::=  number [ "[" child_block "]" ] child_block*    
	 * </code>
	 * </pre>
	 * And number is simply a numeric value.
	 * @param st : It is expected to be pointing at or before "[" before execution, and will be pointing after "]" at the end of execution.
	 * @param childrenOfRootOfValueTree : input/output argument - the list where we can reach all the nodes currently created.
	 * @param parent : parent in current recursion
	 * @throws IOException : exceptions from the stream tokenizer
	 */
	private void readStructureRootBlock(StreamTokenizer st, List<IValueTreeNode> childrenOfRootOfValueTree, long rootId) throws IOException {
		// check that structure starts with "["
		if (st.ttype != '[') {
			if (st.nextToken() != '[') {
				throw new IllegalArgumentException("The provided value tree structure definition block doesn't seem to start with an open square bracket \"[\"");
			}
		} 
//		else if (st.ttype != ('[')) {
//			throw new IllegalArgumentException("The provided value tree structure definition block doesn't seem to start with an open square bracket \"[\"");
//		}
		// assert number
		if (st.nextToken() != st.TT_NUMBER) {
			String suffixOfErrorMessage = "unknown token type "+st.ttype;
			if (st.ttype == st.TT_EOF) {
				suffixOfErrorMessage = "the end of string.";
			} else if (st.ttype == st.TT_WORD) {
				suffixOfErrorMessage = st.sval;
			}
			throw new IllegalArgumentException("Expected a number to be provided as quantity of children of root node, but found " + suffixOfErrorMessage);
		}
		// read the number of children
		int numChildren = (int) st.nval;
		// this list will hold the children created in this level
		List<IValueTreeNode> childrenOfThisLevel = new ArrayList<IValueTreeNode>(numChildren);
		// create children
		for (int i = 0; i < numChildren; i++) {
			IValueTreeNode node = ValueTreeNode.getInstance(rootId + "_" + i, null);
			// use uniform faction initially
			node.setFaction(1f/numChildren);
			// set this node as the immediate child of root
			node.setParent(null);
			// children of root are inserted in this list
			childrenOfRootOfValueTree.add(node);
			// also, mark this node as the child created in this level
			childrenOfThisLevel.add(node);
		}
		if (st.nextToken() == '[') {
			// go to next level
			// make sure to put the cursor after "[" and check if string did not end
			if (st.nextToken() == st.TT_EOF) {
				throw new IllegalArgumentException("The block indicating " + numChildren + " children of root was not properly opened.");
			}
			st.pushBack();
			// at this point, the cursor of st is before "[" and next element is not the end of string
			this.readStructureChildBlock(st, childrenOfThisLevel);
			// at this point, the cursor should be before "]"
			if (st.nextToken() != ']') {
				throw new IllegalArgumentException("The block indicating " + numChildren + " children of root was not properly closed.");
			}
			st.nextToken();
		}
		if (st.ttype != ']') {
			throw new IllegalArgumentException("The provided value tree structure definition root block doesn't seem to end with an closed square bracket \"]\"");
		}
		// set cursor after "]"
		st.nextToken();
	}
	
	/**
	 * <br/>
	 * <br/>
	 * This method reads the child_block statement in the BNF:
	 * <pre>
	 * <code>
	 * root_block ::= "[" number [ "[" child_block "]" ] "]"
	 * child_block ::=  number [ "[" child_block "]" ] child_block*  
	 * </code>
	 * </pre>
	 * And number is simply a numeric value.
	 * @param st : expected to have the cursor at "[" before execution, and to have it before "]" at the end of execution.
	 * @param parents : they are the nodes created at the previous level. Nodes created in this level will be linked to some node in this list.
	 * Nodes created in 1st child_block will become children of 1st node of this list, nodes created in 2nd child_block will
	 * become children of the 2nd node of this list, and so on.
	 * @throws IOException
	 */
	private void readStructureChildBlock(StreamTokenizer st, List<IValueTreeNode> parents) throws IOException {
		// the quantity of number in child_block must match the number of parents.
		for (IValueTreeNode parent : parents) {
			if (st.nextToken() != st.TT_NUMBER) {
				String suffixOfErrorMessage = "unknown token type "+st.ttype;
				if (st.ttype == st.TT_EOF) {
					suffixOfErrorMessage = "the end of string.";
				} else if (st.ttype == st.TT_WORD) {
					suffixOfErrorMessage = st.sval;
				}
				throw new IllegalArgumentException("Expected a number to be provided as quantity of children of non-root node " 
						+ parent
						+ ", but found " + suffixOfErrorMessage);
			}
			int numChildren = (int) st.nval;
			
			List<IValueTreeNode> childrenOfThisLevel = new ArrayList<IValueTreeNode>(numChildren);
			// create children
			for (int i = 0; i < numChildren; i++) {
				IValueTreeNode node = ValueTreeNode.getInstance(parent.getName() + "_" + i, null);
				// use uniform faction initially
				node.setFaction(1f/numChildren);
				// set this node as the immediate child of root
				node.setParent(parent);
				// children of root are inserted in this list
				if (parent.getChildren() == null) {
					parent.setChildren(new ArrayList<IValueTreeNode>(numChildren));
				}
				parent.getChildren().add(node);
				// also, mark this node as the child created in this level
				childrenOfThisLevel.add(node);
			}
			if (st.nextToken() == st.TT_EOF) {
				throw new IllegalArgumentException("Unexpected end of string found when reading the next number of children of " + parent);
			}
			if (st.ttype == '[') {
				// go to next level
				// make sure to put the cursor after "[" and check if string did not end
				if (st.nextToken() == st.TT_EOF) {
					throw new IllegalArgumentException("The block indicating " + numChildren + " children of root was not properly opened.");
				}
				st.pushBack();
				// at this point, the cursor of st is before "[" and next element is not the end of string
				this.readStructureChildBlock(st, childrenOfThisLevel);
				// at this point, the cursor should be before "]"
				if (st.nextToken() != ']') {
					throw new IllegalArgumentException("The block indicating " + numChildren + " children of root was not properly closed.");
				}
			} else {
				st.pushBack();
			}
		}
		
	}
	
	/**
	 * Reads only the shadow node block.
	 * Example of expected format: [1,0],[1,1],[1,2],[0,1]
	 * @param st : expected to be at or before the first "[" of 
	 * @param childrenOfRootOfValueTree
	 * @param shadowNodes
	 * @throws IOException : thrown by the tokenizer.
	 */
	private void readShadowNodeBlock(StreamTokenizer st, List<IValueTreeNode> childrenOfRootOfValueTree, List<IValueTreeNode> shadowNodes) throws IOException {
		if (st == null || childrenOfRootOfValueTree == null) {
			// cannot do anything
			return;
		}
		// move the cursor until we reach the first "["
		while (st.ttype != '[') {
			if (st.ttype == st.TT_EOF) {
				// there is nothing to do, because string has reached end before reaching the first block of shadow nodes.
				return;
			}
			st.nextToken();
		}
		if (shadowNodes == null) {
			// this won't be used by caller, but instantiate just in order to allow same code to work in both null and non-null cases
			shadowNodes = new ArrayList<IValueTreeNode>();
		}
		
		while (st.nextToken() != st.TT_EOF) {
			// at this point, cursor shall be on a number (which represent index of children in the path to shadow node)
			if (st.ttype != st.TT_NUMBER) {
				throw new IllegalArgumentException("Expected a number after \"[\" in a shadow node block definition.");
			}
			// read string and step into the hierarchy of the value tree to obtain the shadow node
			IValueTreeNode shadowNode = null;
			List<IValueTreeNode> childrenOfThisIteration = childrenOfRootOfValueTree;	// initialize children of iteration to children of root
			// go down from root to leaf
			while (st.ttype == st.TT_NUMBER) {
				// read the number
				int childIndex = (int) st.nval;
				if (childIndex < 0 || childrenOfThisIteration == null || childrenOfThisIteration.size() <= childIndex) {
					throw new IllegalArgumentException(childIndex + " is not a valid index as a child of " + ((shadowNode != null)?shadowNode:"root"));
				}
				// extract the node of this step in the path
				shadowNode = childrenOfThisIteration.get(childIndex);
				// if "]", then stop
				if (st.nextToken() == ']') {
					break;
				}
				
				// at this point, the next token shall be ","
				if (st.ttype != ',') {
					throw new IllegalArgumentException("Expected \",\" or \"]\" after a number.");
				}
				
				// children of the next iteration is the children of current node
				childrenOfThisIteration = shadowNode.getChildren();
				
				// move cursor, so that in the next iteration the cursor is at the next number
				st.nextToken();
			}
			if (st.ttype != ']') {
				throw new IllegalArgumentException("A shadow node declaration is expected to end with \"]\". The last node reached from the declaration was " + shadowNode);
			}
				
			// set the shadow node
			if (shadowNode != null) {
				shadowNodes.add(shadowNode);
			} else {
				throw new IllegalArgumentException("Could not find shadow node.");
			}
			
			// move cursor until the next "[" (can expect "," too)
			while (st.ttype != '[') {
				if (st.ttype == st.TT_EOF) {
					// there is nothing to do, because string has reached end before reaching the first block of shadow nodes.
					return;
				}
				st.nextToken();
			}
			
		}
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

			// reset cache of complexity factors of each existing arc
			Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
			if (arcComplexityCache == null) {
				setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
			} else {
				arcComplexityCache.clear();
			}
			
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
				node = getDefaultInferenceAlgorithm().createNodeInProbabilisticNetwork(Long.toString(this.questionId), numberStates, initProbs, isToUpdateJunctionTreeAndAssetNets, net,null);
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
			// check whether network is initialized (junction tree was created) or not
			synchronized (getProbabilisticNetwork()) {
				if (getProbabilisticNetwork().getJunctionTree() == null || getProbabilisticNetwork().getJunctionTree().getCliques() == null
						|| getProbabilisticNetwork().getJunctionTree().getCliques().isEmpty()) {
					// network was not initialized, so needs to rebuild net
					return true;
				}
			}
			// network was initialized, so no need to rebuild it from scratch, so do not return flag to rebuild
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
	
	/**
	 * This is a network action which adds new node containing value trees.
	 * @author Shou Matsumoto
	 */
	public class AddValueTreeQuestionNetworkAction extends AddQuestionNetworkAction {

		private static final long serialVersionUID = 7769963256881143458L;
		private List<IValueTreeNode> childrenOfRootOfValueTree;
		private List<IValueTreeNode> shadowNodes;

		/**
		 * Default constructor initializing fields
		 * @param transactionKey
		 * @param occurredWhen
		 * @param questionId
		 * @param numberStates
		 * @param initProbs
		 * @param childrenOfRootOfValueTree
		 * @param shadowNodes
		 */
		public AddValueTreeQuestionNetworkAction(Long transactionKey,
				Date occurredWhen, long questionId, int numberStates,
				List<Float> initProbs,
				List<IValueTreeNode> childrenOfRootOfValueTree,
				List<IValueTreeNode> shadowNodes) {
			super(transactionKey, occurredWhen, questionId, numberStates, initProbs);
			this.childrenOfRootOfValueTree = childrenOfRootOfValueTree;
			this.shadowNodes = shadowNodes;
		}
		

		/**
		 * Overwrites the method in the superclass just to make sure the new node is an instance of {@link ValueTreeProbabilisticNode},
		 * it is filled with correct {@link IValueTree}, {@link IValueTree#getShadowNode(int)},
		 * and with proper {@link IValueTree#addFactionChangeListener(unbbayes.prs.bn.valueTree.IValueTreeFactionChangeListener)}
		 * being called.
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.AddQuestionNetworkAction#execute(unbbayes.prs.bn.ProbabilisticNetwork, boolean)
		 */
		public void execute(ProbabilisticNetwork net,
				boolean isToUpdateJunctionTreeAndAssetNets) {
			

			// reset cache of complexity factors of each existing arc
			Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
			if (arcComplexityCache == null) {
				setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
			} else {
				arcComplexityCache.clear();
			}
			
			
			if (!isToUseDynamicJunctionTreeWithValueTrees()) {
				setDynamicJunctionTreeNetSizeThreshold(Integer.MAX_VALUE);
			}
			
			if (!isToAddArcsOnlyToProbabilisticNetwork()) {
				throw new UnsupportedOperationException("Current implementation of value trees cannot be used with assets, so please turn on the flag \"isToAddArcsOnlyToProbabilisticNetwork\"");
			}
			INode node = null;	// the new node 
			synchronized (getDefaultInferenceAlgorithm()) {
				node = getDefaultInferenceAlgorithm().createNodeInProbabilisticNetwork(
						Long.toString(this.getQuestionId()), 	// name
						0, 										// no state initially (states will be automatically added when we set shadow nodes)
						this.getNewValues(), 					// probably ignored
						isToUpdateJunctionTreeAndAssetNets, 	// reuse config from caller
						net, 									// reuse from caller
						DEFAULT_VALUE_TREE_PROB_NODE_BUILDER	// instantiate ValueTreeProbabilityNode instead of other type of nodes
					);
			}
			if (node == null) {
				throw new RuntimeException("Failed to create question " + getQuestionId() + " in the shared Bayes net.");
			}
			
			if (isToUpdateJunctionTreeAndAssetNets) {
				// create node in all asset networks too
				synchronized (getUserToAssetAwareAlgorithmMap()) {
					for (AssetAwareInferenceAlgorithm userAlgorithm : getUserToAssetAwareAlgorithmMap().values()) {
						userAlgorithm.addDisconnectedNodeIntoAssetNet(node, net, userAlgorithm.getAssetNetwork());
					}
				}
			}
			
			// properly initialize value tree 
			if (node instanceof ValueTreeProbabilisticNode) {
				final ValueTreeProbabilisticNode root = (ValueTreeProbabilisticNode) node;
				if (this.getChildrenOfRootOfValueTree() != null) {
					for (IValueTreeNode child : this.getChildrenOfRootOfValueTree()) {
						// add nodes to value tree recursively
						this.addNodeAndDescendantToValueTreeRecursively(child,root.getValueTree());
					}
				}
				
				// properly set shadow nodes
				if (this.getShadowNodes() != null) {
					for (IValueTreeNode shadowNode : this.getShadowNodes()) {
						root.getValueTree().setAsShadowNode(shadowNode);	// this should also update the marginals
					}
					
					// we need to update associated clique table, because new shadow nodes implies new states, thus the clique table needs to expand
					IRandomVariable associatedCliqueOrSeparator = root.getAssociatedClique();
					if (associatedCliqueOrSeparator == null 					   	// AssetAwareInferenceAlgorithm#createNodeInProbabilisticNetwork should have created a clique where the node belongs
							|| !(associatedCliqueOrSeparator instanceof Clique)) { 	// the node we just created is disconnected from everything else, so there shall not be any separator containing it
						if (isToUpdateJunctionTreeAndAssetNets) {
							throw new RuntimeException("Adding new value tree node " + root + " did not automatically create a new clique in junction tree. The clique was: " + associatedCliqueOrSeparator);
						}
					} else {
						// extract the clique potential
						PotentialTable cliqueTable = ((Clique)associatedCliqueOrSeparator).getProbabilityFunction();
						if (cliqueTable == null) {
							throw new RuntimeException("No potential table for clique " + associatedCliqueOrSeparator + " was found.");
						}
						// the size of clique table must be this
						int tableSize = root.getStatesSize();
						
						// the node we just created is supposedly associated with only 1 clique, and the clique is supposed to have only the node
						switch (cliqueTable.variableCount()) {
						case 0:
							// inserting the variable shall initialize the table with proper number of shadow nodes (i.e. proper number of states)
							cliqueTable.addVariable(root);
							break;
						case 1:
							// expand the table to proper size
							while (cliqueTable.tableSize() < tableSize) {
								// just expand and fill with any value. Zero should use less space in java class objects (because it's a default global constant specified by the language)
								cliqueTable.addValueAt(0, 0f); 	// the table will be filled with marginals later (after the swith-case)
							}
							break;
						default:
							throw new RuntimeException("Table of newly created clique/separator " + associatedCliqueOrSeparator
									+ " is expected to contain only the value tree node " + root + ", but table contained " + cliqueTable.variableCount() + " variables.");
						}
						// now, update content of clique table. It must be equal to the marginal
						for (int i = 0; i < tableSize; i++) {
							cliqueTable.setValue(i, root.getMarginalAt(i));
						}
					}
				}
				
				// set listener so that a normal trade is automatically performed if probability of shadow node (i.e. marginals) are changed by the value tree algorithm
				// do not execute the default listener (so delete it), because it updates marginals (marginal should be kept as old value, so that likelihood ratio is correctly calculated)
				root.getValueTree().clearFactionChangeListener();	
				root.getValueTree().addFactionChangeListener(new IValueTreeFactionChangeListener() {
					/** this method assumes that it is executed after the default one (which updates marginal and cpt based on updates in value tree) */
					public void onFactionChange(Collection<IValueTreeFactionChangeEvent> changes) {
						// change marginal of root if faction of a shadow node has changed
						List<IValueTreeNode> shadows = getShadowNodes();
						if (shadows == null) {
							// there is nothing to do
							Debug.println(getClass(), root + " has no shadow node, so changes in value tree won't change its CPT");
							return;
						}
						// at this point, there are shadow nodes. Check if shadow nodes are included in nodes which where changed
						boolean hasChanged = false;
						for (IValueTreeFactionChangeEvent change : changes) {	// assuming that changes only have nodes that actually changed
							// only consider shadow nodes (nodes which represents the states present in root node)
							if (shadows.contains(change.getNode())) { 
//								// index of shadow node is supposedly the same of index of respective state in root node
//								int indexOfShadowNode = root.getValueTree().getShadowNodeStateIndex(change.getNode());
//								// obtain the previous faction for comparison
//								float factionBefore = root.getValueTree().getShadowNode(indexOfShadowNode).getFaction();
//								// obtain the current faction
//								float factionAfter = change.getNode().getFaction();
//								// check if factions really have changed, just to make sure
//								if (Math.abs(factionAfter - factionBefore) >= AssetAwareInferenceAlgorithm.ERROR_MARGIN) {
									// mark that something has changed
									hasChanged = true;
									break;
//								}
							} else {
								// check if ancestor of shadow has changed, because a change in ancestor will change probability of shadow
								for (IValueTreeNode shadow : shadows) {
									if (change.getNode().isAncestorOf(shadow)) {
										hasChanged = true;
										break;
									}
								}
							}
						}
						// if something has changed, then update the marginals accordingly to all shadow nodes
						if (hasChanged) {
							// extract the value tree and keep it handy, because we'll use it frequently.
							IValueTree vt = root.getValueTree();
							// fill the probability of trade by using the marginal
							int statesSize = vt.getShadowNodeSize();
							if (root.getStatesSize() != statesSize) {
								throw new IllegalStateException("A desynchronization of shadow nodes found. There are " 
										+ statesSize + " shadow nodes, but root has " + root.getStatesSize()
										+ " possible states. This is an unexpected fatal problem, so it is suggested to roll back your system and check ME/UnBBayes version compatibility.");
							}
							// index of shadow nodes and states of root are supposedly synchronized
							List<Float> newValues = new ArrayList<Float>(statesSize);
							for (int i = 0; i < statesSize; i++) {
								// calculate the correct marginal from the probability of shadow node
								newValues.add(vt.getProb(vt.getShadowNode(i), null));
							}
							// do the trade in order to update the junction tree and set marginal to correct value
							addTrade(
									null, 							// commit immediately
									new Date(), 					// created now
									"Adjust from VT of " + root, 	// just for identification
									Long.MIN_VALUE, 				// user is ignored, so can be any user
									getQuestionId(), 				
									newValues, 
									null, 
									null, 
									true
								);
						}
					}
				});
			}
			
		}

		/**
		 * Recursively add node and its descendant to value tree
		 * @param node : node to add
		 * @param valueTree : value tree where node will be inserted
		 */
		private void addNodeAndDescendantToValueTreeRecursively(IValueTreeNode node, IValueTree valueTree) {
			// make sure the node can reference the value tree
			node.setValueTree(valueTree);
			// add the child into the value tree
			valueTree.addNode(node);
			// make recursive calls to children
			if (node.getChildren() != null) {
				for (IValueTreeNode child : node.getChildren()) {
					this.addNodeAndDescendantToValueTreeRecursively(child, valueTree);
				}
			}
		}
		
		


		/**
		 * @return the childrenOfRootOfValueTree
		 */
		public List<IValueTreeNode> getChildrenOfRootOfValueTree() {
			return childrenOfRootOfValueTree;
		}

		/**
		 * @param childrenOfRootOfValueTree the childrenOfRootOfValueTree to set
		 */
		public void setChildrenOfRootOfValueTree(
				List<IValueTreeNode> childrenOfRootOfValueTree) {
			this.childrenOfRootOfValueTree = childrenOfRootOfValueTree;
		}

		/**
		 * @return the shadowNodes
		 */
		public List<IValueTreeNode> getShadowNodes() {
			return shadowNodes;
		}

		/**
		 * @param shadowNodes the shadowNodes to set
		 */
		public void setShadowNodes(List<IValueTreeNode> shadowNodes) {
			this.shadowNodes = shadowNodes;
		}


		/* (non-Javadoc)
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.AddQuestionNetworkAction#getNumberStates()
		 */
		public int getNumberStates() {
			if (getShadowNodes() == null) {
				return 0;
			}
			return getShadowNodes().size();
		}
	}

	/**
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)
	 * @see MarkovEngineImpl#isToAddArcsWithoutReboot()
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
		
		if (isToAddArcsOnlyToProbabilisticNetwork() || isToAddArcsWithoutReboot()) {
			if (cpd != null && !cpd.isEmpty()) {
				throw new UnsupportedOperationException("The current version of the Markov Engine does not allow replacement of the arcs, so a null/empty cpd must be provided");
			}
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
	 * This simply represents an aggregated set of {@link AddQuestionAssumptionNetworkAction}.
	 * This may be useful for optimizing a sequence of {@link AddQuestionAssumptionNetworkAction}
	 * when {@link MarkovEngineImpl#isToAddArcsOnlyToProbabilisticNetwork} == true.
	 * @author Shou Matsumoto
	 */
	public class AggregatedQuestionAssumptionNetworkAction extends AddQuestionAssumptionNetworkAction {
		private Map<Long, List<Long>> links;

		/**
		 * Constructor initializing fields
		 * @param actions
		 */
		public AggregatedQuestionAssumptionNetworkAction(List<AddQuestionAssumptionNetworkAction> actions)  {
			// simply use values from the 1st action being aggregated
			super(actions.get(0).getTransactionKey(), actions.get(0).getWhenCreatedMillis(),  actions.get(0).getQuestionId(),  actions.get(0).getAssumptionIds(), null);
			
			if (!isToAddArcsOnlyToProbabilisticNetwork()) {
				throw new UnsupportedOperationException("This version of Markov Engine can only aggregate requests for adding arcs when the flag isToAddArcsOnlyToProbabilisticNetwork == true.");
			}
			
			// initialize mapping of links
			Map<Long, List<Long>> mapping = new HashMap<Long, List<Long>>();
			this.setLinks(mapping);
			
			// fill mapping of links accordingly to the actions in the provided list
			for (AddQuestionAssumptionNetworkAction action : actions) {
				// extract ID of child node
				Long childID = action.getQuestionId();
				if (childID == null) {
					throw new IllegalArgumentException("An AddQuestionAssumptionNetworkAction specifying no child question ID was found.");
				}
				
				// get the list of parents already mapped
				List<Long> parentIDs = mapping.get(childID);
				if (parentIDs == null) {
					// 1st time to add mapping. Put list to map
					parentIDs = new ArrayList<Long>(0);
					mapping.put(childID, parentIDs);
				}
				
				// add new parents to map
				if (action.getAssumptionIds() != null) {
					for (Long newParentID : action.getAssumptionIds()) {
						// avoid redundancy
						if (!parentIDs.contains(newParentID)) {
							parentIDs.add(newParentID);
						}
					}
				}
				
			}
		}
		
		/**
		 * Add set of arcs specified in the mapping
		 */
		public void execute(ProbabilisticNetwork network) {
			long currentTimeMillis = System.currentTimeMillis();
			
			// the conditions that we need to reboot (re-run trades) are in the following if-clause. 
			// The condition cpd == null indicates that we are not substituting any arc (if cpd != null, then we are trying to substitute arcs)
			if (isToAddArcsWithoutReboot() && !isTriggerForRebuild()) {
				// extract the object responsible for managing the probabilistic network alone.
				// we need to change structure and junction tree without rebuilding and re-running trades
				AssetAwareInferenceAlgorithm probAlgorithm = getDefaultInferenceAlgorithm();
				
				// extract child node and parent nodes to be used by probabilistic network
				Map<INode, List<INode>> childrenAndParents = new HashMap<INode, List<INode>>(getLinks().size());
				synchronized (network) {
					for (Entry<Long, List<Long>> entry : getLinks().entrySet()) {
						// the child node
						INode child = network.getNode(Long.toString(entry.getKey()));
						if (child == null) {
							throw new RuntimeException("Question " + entry.getKey() + " was not found.");
						}
						// the parent nodes
						List<INode> parents = new ArrayList<INode>(entry.getValue().size());
						for (Long assumptiveQuestionId : entry.getValue()) {
							INode parent = network.getNode(Long.toString(assumptiveQuestionId));
							if (parent == null) {
								throw new RuntimeException("Assumption " + assumptiveQuestionId + " was not found.");
							}
							parents.add(parent);
						}
						// update mapping
						childrenAndParents.put(child, parents);
					}
				}
				
				if (isToAddArcsOnlyToProbabilisticNetwork()) {
					// reset users, because changing probabilistic network without changing asset network will make the algorithm inconsistent anyway.
					synchronized (getUserToAssetAwareAlgorithmMap()) {
						getUserToAssetAwareAlgorithmMap().clear();
					}
					// run method that adds arcs assuming that we are only changing probabilistic network
					synchronized (probAlgorithm) {
						try {
							// true means optimizations will be performed assuming that only prob net will be changed
							probAlgorithm.addEdgesToNet(childrenAndParents, true, getVirtualArcs());
						} catch (InvalidParentException e) {
							// TODO do not use exception translation
							throw new RuntimeException(e);
						}	
					}
				} else {
					throw new UnsupportedOperationException("This version of Markov Engine can only aggregate requests for adding arcs when the flag isToAddArcsOnlyToProbabilisticNetwork == true.");
				}
			} else {
				throw new UnsupportedOperationException("This version of Markov Engine can only aggregate requests for adding arcs when we shall not rebuild net.");
			}
			
			try {
				Debug.println(getClass(), "Executed add set of assumptions, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			
		}

		/** @return mapping representing the links (from child to all its parents) */
		public Map<Long, List<Long>> getLinks() { return links; }

		/** @param links : mapping representing the links (from child to all its parents) */
		public void setLinks(Map<Long, List<Long>> links) { this.links = links; }
	}
	/**
	 * Represents a network action for adding a direct dependency (edge) into a BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#addQuestionAssumption(long, Date, long, long, List)
	 * @see MarkovEngineImpl#isToAddArcsWithoutReboot()
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
			
			if (isToAddArcsOnlyToProbabilisticNetwork() || isToAddArcsWithoutReboot()) {
				if (this.cpd != null && !this.cpd.isEmpty()) {
					throw new UnsupportedOperationException("The current version of the Markov Engine does not allow replacement of the arcs, so a null/empty cpd must be provided");
				}
			}
			
			// the conditions that we need to reboot (re-run trades) are in the following if-clause. 
			// The condition cpd == null indicates that we are not substituting any arc (if cpd != null, then we are trying to substitute arcs)
			if (isToAddArcsWithoutReboot() && !isTriggerForRebuild()) {
				// we need to change structure and junction tree without rebuilding and re-running trades
				// extract the object responsible for managing the probabilistic network alone.
				AssetAwareInferenceAlgorithm probAlgorithm = getDefaultInferenceAlgorithm();
				
				// extract child node and parent nodes to be used by probabilistic network
				INode child = null;
				List<INode> parents = new ArrayList<INode>(assumptiveQuestionIds.size());
				synchronized (network) {
					// the child node
					child = network.getNode(Long.toString(sourceQuestionId));
					// the parent nodes
					for (Long assumptiveQuestionId : assumptiveQuestionIds) {
						INode parent = network.getNode(Long.toString(assumptiveQuestionId));
						parents.add(parent);
					}
				}
				
				if (isToAddArcsOnlyToProbabilisticNetwork()) {
					// reset users, because changing probabilistic network without changing asset network will make the algorithm inconsistent anyway.
					synchronized (getUserToAssetAwareAlgorithmMap()) {
						getUserToAssetAwareAlgorithmMap().clear();
					}
					// run method that adds arcs assuming that we are only changing probabilistic network
					synchronized (probAlgorithm) {
						try {
							// true means optimizations will be performed assuming that only prob net will be changed
							probAlgorithm.addEdgesToNet(Collections.singletonMap(child, parents), true, getVirtualArcs());
						} catch (InvalidParentException e) {
							// TODO do not use exception translation
							throw new RuntimeException(e);
						}	
					}
				} else {
					// call AssetAwareInferenceAlgorithm#addEdgesToNet to update shared probabilistic net
					synchronized (probAlgorithm) {
						// call method not using assumption of global consistency and normalization (i.e. use false),
						// so that it results in same junction tree of asset networks
						try {
							probAlgorithm.addEdgesToNet(Collections.singletonMap(child, parents), false, getVirtualArcs());
						} catch (InvalidParentException e) {
							// TODO do not use exception translation
							throw new RuntimeException(e);
						}	
					}
					// call AssetPropagationInferenceAlgorithm#addEdgesToNet to update each user's asset net
					synchronized (getUserToAssetAwareAlgorithmMap()) {
						for (AssetAwareInferenceAlgorithm userAlgorithm : getUserToAssetAwareAlgorithmMap().values()) {
							try {
								// no need to use asset nodes in the arguments, because AssetPropagationInferenceAlgorithm automatically converts.
								userAlgorithm.getAssetPropagationDelegator().addEdgesToNet(Collections.singletonMap(child, parents), false, getVirtualArcs());
							} catch (InvalidParentException e) {
								// TODO do not use exception translation
								throw new RuntimeException(e);
							}	
						}
					}
				}
			} else {
				// the network will be compiled and trades will be re-executed afterwards
				this.addArcsAssumingReboot(network);
			}
			try {
				Debug.println(getClass(), "Executed add assumption, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			
		}
		
		/**
		 * This template method implements the logic of {@link #execute(ProbabilisticNetwork)}
		 * when {@link MarkovEngineImpl#isToAddArcsWithoutReboot()} == false.
		 * Therefore, this method adds arcs to the Bayes net assuming that the caller
		 * will reboot (rebuild the junction tree and re-run trades) afterwards.
		 * @param network
		 */
		protected void addArcsAssumingReboot(ProbabilisticNetwork network) {
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
					} catch (Exception e) {
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
					getDefaultCPTNormalizer().applyFunction((ProbabilisticTable) potTable);
				}
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
		/** Adding arcs will trigger a reboot if {@link MarkovEngineImpl} is configured to reboot after adding arcs or network is uninitialized */
		public boolean isTriggerForRebuild() { 
			// if it is configured not to add arcs without reboot, then immediately return that we shall rebuild net
			if (!isToAddArcsWithoutReboot()) {
				return true;
			}
			// if cpd was specified, then we need to replace arcs, but that's only possible in this implementation by rebooting and rerunning trades
			// TODO the current commitNetworkActions assumes that the history is filled in order to rebuild network, so 
			// cannot set cpt if it is set not to rebuild net.
			if (cpd != null) {
				return true;
			}
			
			// if it is configured to delete resolved questions, then arcs may be involving deleted questions
			if (isToDeleteResolvedNode()) {
				// if arcs are being added to resolved questions, nodes may not be present anymore, and in this case we need to reboot
				if (getResolvedQuestions().containsKey(sourceQuestionId)) {
					return true;
				}
				for (Long assumptionId : assumptiveQuestionIds) {
					if (getResolvedQuestions().containsKey(assumptionId)) {
						return true;
					}
				}
			}
			// or else, check that network was initialized (junction tree was created) already.
			synchronized (getProbabilisticNetwork()) {
				if (getProbabilisticNetwork().getJunctionTree() == null || getProbabilisticNetwork().getJunctionTree().getCliques() == null
						|| getProbabilisticNetwork().getJunctionTree().getCliques().isEmpty()) {
					// network not initialized yet, so need to rebuild
					return true;
				}
			}
			// network is initialized already, and engine is configured to add arcs without reboot, so do not return flag to rebuild
			return false; 
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
			if (isToAddArcsOnlyToProbabilisticNetwork()) {
				return;	// do nothing
			}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addCash(long, java.util.Date, long, float, java.lang.String)
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
			if (newValues != null) {
				newValues = new ArrayList<Float>(newValues);	// do not use original, to reduce side effects
			}
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
			this.execute(!isToAddArcsOnlyToProbabilisticNetwork);	// only update assets if it is not configured to update only prob network
			try {
				Debug.println(getClass(), "Executed trade, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		/** Calls {@link #execute(boolean, true)}
		 * @see #execute(boolean, boolean) */
		public void execute(boolean isToUpdateAssets) {
			this.execute(isToUpdateAssets, !isToThrowExceptionOnInvalidAssumptions(), true);
		}
		public void execute(boolean isToUpdateAssets, boolean isToUpdateAssumptionIds, boolean isToUpdateMarginals) {
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
//				if (getWhenExecutedFirstTimeMillis() > 0) {
					// for optimization, only update history if this is first execution.
					// first, check that node exists
					node = getProbabilisticNetwork().getNode(Long.toString(tradeSpecification.getQuestionId()));
					if (node == null) {
						throw new InexistingQuestionException("Question " + tradeSpecification.getQuestionId() + " not found.", tradeSpecification.getQuestionId());
					}
//				}
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
							false, this,
							isToUpdateMarginals
						);
					
					// store what was the conditional probability before the trade, if it was not specified in the tradeSpecification
					if (tradeSpecification.getOldProbabilities() == null || tradeSpecification.getOldProbabilities().isEmpty()) {
						tradeSpecification.setOldProbabilities(oldConditionalProb);
					}
					
					algorithm.setToUpdateAssets(backup);	// revert config of assets
					// backup the previous delta so that we can revert this trade
//					qTablesBeforeTrade = algorithm.getAssetTablesBeforeLastPropagation();
					// add this question to the mapping of questions traded by the user
					if (isToTraceHistory()) {
						Set<Long> questions = getTradedQuestionsMap().get(tradeSpecification.getUserId());
						if (questions == null) {
							questions = new HashSet<Long>();
							getTradedQuestionsMap().put(tradeSpecification.getUserId(), questions);
						}
						questions.add(tradeSpecification.getQuestionId());
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
	 * This is the {@link NetworkAction} command
	 * representing {@link MarkovEngineImpl#addTrade(Long, Date, long, List, List, List, List, List)},
	 * more specifically for changing probabilities of value tree nodes.
	 * It is expected that this type of trades only changes probability of the value tree, not
	 * of other nodes in the external bayes net.
	 * @author Shou Matsumoto
	 */
	public class AddTradeValueTreeNetworkAction extends AddTradeNetworkAction {

		private static final long serialVersionUID = -5538538423172739941L;
		private List<Integer> referencePath;
		private List<Integer> targetPath;
		private Collection<IValueTreeNode> nodesNotToChange = null;
		/**
		 * Constructor not initializing only {@link #getNodesNotToChange()}
		 * @param transactionKey
		 * @param occurredWhen
		 * @param questionId
		 * @param newValues
		 * @param targetPath
		 * @param referencePath
		 * @see #AddTradeValueTreeNetworkAction(Long, Date, long, List, List, List, Collection)
		 */
		public AddTradeValueTreeNetworkAction(Long transactionKey, Date occurredWhen, long questionId, List<Float> newValues,
				List<Integer> targetPath, List<Integer> referencePath) {
			super(transactionKey, occurredWhen, "", Long.MIN_VALUE, questionId, null, newValues, Collections.EMPTY_LIST, Collections.EMPTY_LIST, true);
			this.setTargetPath(targetPath);
			this.setReferencePath(referencePath);
		}
		/**
		 * Default constructor initializing all the fields
		 * @param transactionKey
		 * @param occurredWhen
		 * @param questionId
		 * @param newValues
		 * @param targetPath
		 * @param referencePath
		 * @param nodesNotToChange : if this is non-null, then best effort will be done not to change factions of these nodes
		 */
		public AddTradeValueTreeNetworkAction(Long transactionKey, Date occurredWhen, long questionId, List<Float> newValues,
				List<Integer> targetPath, List<Integer> referencePath, Collection<IValueTreeNode> nodesNotToChange) {
			this(transactionKey, occurredWhen, questionId, newValues, targetPath, referencePath);
			this.nodesNotToChange = nodesNotToChange;
		}
		
		/**
		 * @see AddTradeNetworkAction#execute(boolean, boolean, boolean)
		 */
		public void execute(boolean isToUpdateAssets, boolean isToUpdateAssumptionIds, boolean isToUpdateMarginals) {
			
			synchronized (getProbabilisticNetwork()) {
				ValueTreeProbabilisticNode root = null;
				if (getWhenExecutedFirstTimeMillis() <= 0) {
					// for optimization, only update history if this is first execution.
					// first, check that node exists
					Node node = getProbabilisticNetwork().getNode(Long.toString(getQuestionId()));
					if (node == null) {
						throw new InexistingQuestionException("Question " + getQuestionId() + " not found.", getQuestionId());
					}
					if (node instanceof ValueTreeProbabilisticNode) {
						root = (ValueTreeProbabilisticNode) node;
					} else {
						throw new IllegalStateException(node + " is supposed to have a value tree, but did not.");
					}
				}
				
				// if there are multiple values specified in newValues, then we are actually attempting to change the probability of parent
				boolean isToTradeOnParent = false;	// this will become true if target path is actually pointing to parent
				float lastNonNullProb = Float.NaN;	// will hold the last non-null probability specification found
				if (getNewValues() != null) {
					int counterOfNonNull = 0;
					for (Float prob : getNewValues()) {
						if (prob != null) {
							lastNonNullProb = prob;
							counterOfNonNull++;
							if (counterOfNonNull > 1) {
								isToTradeOnParent = true;
								break;
							}
						}
					}
					if (counterOfNonNull == 0) {
						// there is actually nothing to trade
						Debug.println("All probabilities were set to null, so there is nothing to change.");
						return;
					}
				} else {
					// we are not changing probability
					Debug.println("Probabilities were set to null, so there is nothing to change.");
					return;
				}
				
				// extract the value tree node to trade
				IValueTreeNode target = root.getValueTree().getNodeInPath(targetPath);
//				if (targetPath != null) {
//					List<IValueTreeNode> children = root.getValueTree().get1stLevelNodes();
//					for (Integer index : targetPath) {
//						if (children == null) {
//							throw new IllegalArgumentException(targetPath + " is not a valid target path for value tree of " + root);
//						}
//						target = children.get(index);
//						children = target.getChildren();
//					}
//				}
				
				// extract the reference node to trade
				IValueTreeNode anchor = root.getValueTree().getNodeInPath(referencePath);
//				if (referencePath != null) {
//					List<IValueTreeNode> children = root.getValueTree().get1stLevelNodes();
//					for (Integer index : referencePath) {
//						if (children == null) {
//							throw new IllegalArgumentException(referencePath + " is not a valid reference path for value tree of " + root);
//						}
//						anchor = children.get(index);
//						children = anchor.getChildren();
//					}
//				}
				
				
				// do trade here, and also get the old probability
				List<Float> oldConditionalProb = null;
				if (isToTradeOnParent) {
					// in this case, we are actually trading on children of target
					if (target.getChildren() == null || target.getChildren().size() != getNewValues().size()) {
						throw new IllegalArgumentException(getNewValues() + " is supposed to be new a probability distribution for children of target " + target 
								+ ", but the target doesn't have that quantity of children.");
					}
					oldConditionalProb = new ArrayList<Float>(getNewValues().size());
					// this list will hold nodes which probabilities shall not be changed. It usually starts from empty, but can be initialized by getNodesNotToChange().
					// make sure we pre-allocate enough space for the list to hold both getNodesNotToChange() and the nodes to be changed in this routine
					List<IValueTreeNode> nodesNotToChange = new ArrayList<IValueTreeNode>(getNewValues().size() + ((getNodesNotToChange()!=null)?getNodesNotToChange().size():0));
					if (getNodesNotToChange() == null) {
						nodesNotToChange.addAll(getNodesNotToChange());
					}
					for (int i = 0; i < getNewValues().size(); i++) {
						IValueTreeNode childOfTarget = target.getChildren().get(i);
						Float prob = getNewValues().get(i);
						if (prob != null) {
							// change the probability of this child
							oldConditionalProb.add(root.getValueTree().changeProb(childOfTarget, anchor, prob, getNodesNotToChange()));
							// this node shall not be changed in next iteration.
							nodesNotToChange.add(childOfTarget);
						} else {
//							oldConditionalProb.add(root.getValueTree().getProb(childOfTarget, anchor));
							oldConditionalProb.add(null);
						}
					}
					
				} else {
					// in this case, lastNonNullProb should have the only non-null probability specified
					oldConditionalProb = Collections.singletonList(root.getValueTree().changeProb(target, anchor, lastNonNullProb, null));
				}
				
				
				// store what was the conditional probability before the trade, if it was not specified in the tradeSpecification
				if (getTradeSpecification().getOldProbabilities() == null || getTradeSpecification().getOldProbabilities().isEmpty()) {
					getTradeSpecification().setOldProbabilities(oldConditionalProb);
				}
				
				// add this question to the mapping of questions traded by the user
				if (isToTraceHistory()) {
					Set<Long> questions = getTradedQuestionsMap().get(getUserId());
					if (questions == null) {
						questions = new HashSet<Long>();
						getTradedQuestionsMap().put(getUserId(), questions);
					}
					questions.add(getQuestionId());
				}
				
//				// connect the traded nodes if necessary
//				if (isToAddArcsOnAddTrade()) {
//					try {
//						this.simpleAddEdge((List)assumptionNodes, child, net);
//					} catch (InvalidParentException e) {
//						throw new RuntimeException("Could not automatically add arc from " + assumptionNodes + " to "+child,e);
//					}
//				}
			}
			
			
		}
		

		public List<Integer> getReferencePath() { return referencePath; }
		public void setReferencePath(List<Integer> referencePath) { this.referencePath = referencePath; }
		public List<Integer> getTargetPath() { return targetPath; }
		public void setTargetPath(List<Integer> targetPath) { this.targetPath = targetPath; }

		/**
		 * @return the nodesNotToChange
		 */
		public Collection<IValueTreeNode> getNodesNotToChange() {
			return nodesNotToChange;
		}

		/**
		 * @param nodesNotToChange the nodesNotToChange to set
		 */
		public void setNodesNotToChange(Collection<IValueTreeNode> nodesNotToChange) {
			this.nodesNotToChange = nodesNotToChange;
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
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.AddTradeNetworkAction#execute(boolean)
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
							false, getParentAction(),
							true	// is to update marginal probabilities too
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
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.AddTradeNetworkAction#getUserId()
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
		if (!isToTraceHistory()) {
			return Collections.emptyList();
		}
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
					synchronized (getVirtualTradeToAffectedQuestionsMap()) {
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
		if (!isToTraceHistory()) {
			return Collections.emptyList();
		}
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
					synchronized (getVirtualTradeToAffectedQuestionsMap()) {
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
		if (getMaxConditionalProbHistorySize() <= 0) {
			return;
		}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addTrade(java.lang.Long, java.util.Date, java.lang.String, long, long, java.util.List, java.util.List, java.util.List, java.util.List, boolean)
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
	 * TODO value tree and dynamic/incremental JT compilation doesn't seem to work well together. Use {@link #setDynamicJunctionTreeNetSizeThreshold(int)} with high value to disable dynamic/incremental JT compilation.
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addTrade(java.lang.Long, java.util.Date, long, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, long questionId, List<Integer> targetPath, List<Integer> referencePath, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException{
		
		// if no value tree path was specified, then just handle it like the old trade
		if (targetPath == null || targetPath.isEmpty()) {
			return this.addTrade(
					transactionKey, occurredWhen, "", 
					new TradeSpecificationImpl(Long.MIN_VALUE, questionId, null, newValues, assumptionIds, assumedStates), 
					true
				);
		}
		
		// at this point, targetPath != null
		if (referencePath != null) {
			// check that reference path is always an ancestor of target path
			if (referencePath.size() >= targetPath.size()) {
				throw new IllegalArgumentException("The reference path must point to an ancestor of target path.");
			}
			// check if beginning of targetPath is equal to referencePath (i.e. check if referencePath is pointing to ancestor of targetPath)
			// note: at this point, the length of referencePath is strictly smaller than targetPath
			for (int i = 0; i < referencePath.size(); i++) {
				if (!referencePath.get(i).equals(targetPath.get(i))) {
					throw new IllegalArgumentException("The reference path must point to an ancestor of target path. The step " + i  + " of the paths are pointing to different nodes in value trees.");
				}
			}
		}
		
		// at this point, reference path is either null, empty, or pointing to ancestor of target path

		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (transactionKey != null && this.getNetworkActionsMap().get(transactionKey) == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		
		// returned value is the same of preview trade
		List<Float> ret = new ArrayList<Float>();
		
		if (getDefaultInferenceAlgorithm() == null) {
			throw new IllegalStateException("Default algorithm not found.");
		}
		
		// check whether question exists, and extract necessary nodes if possible
		int shadowNodeIndex = -1;	// this will contain the index of the shadow node
		int numStates = -1;			// this will hold the number of states that the node (root) shall have.
		synchronized (getDefaultInferenceAlgorithm()) {
			synchronized (getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork()) {
				ProbabilisticNetwork net = getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork();
				if (net == null) {
					throw new IllegalStateException("Network was not properly initialized.");
				}
				Node node = net.getNode(""+questionId);
				// this will be filled with the network action which will add the node if node was not found.
				AddQuestionNetworkAction actionAddingNodeIfNodeIsNull = null;	
				if (node == null) {
					// Perhaps the nodes are still going to be added within the context of this transaction.
					boolean isNodeToBeCreatedWithinTransaction = false;
					Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
					
					if (mapTransactionQuestions == null || transactionKey == null) {
						// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
						throw new InexistingQuestionException("Question " + questionId + " not found.",questionId);
					}
					
					// use the mapping in order to check whether the parent will be created in the same transaction
					synchronized (mapTransactionQuestions) {
						Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
						if (questionsInSameTransaction == null) {
							throw new InexistingQuestionException("Question " + questionId + " not found.",questionId);
						}
						// search for the assumption ID in questionsToBeCreatedInTransaction
						for (AddQuestionNetworkAction questionActInMapping : questionsInSameTransaction) {
							if (questionActInMapping.getQuestionId().longValue() == questionId) {
								isNodeToBeCreatedWithinTransaction = true;
								actionAddingNodeIfNodeIsNull = questionActInMapping;
								numStates = questionActInMapping.getNumberStates();
								break;
							}
						}
					}
					if (!isNodeToBeCreatedWithinTransaction) {
						throw new InexistingQuestionException("Question " + questionId + " not found.",questionId);
					}
					
				} else {
					numStates = node.getStatesSize();
				}
				
				// if node exists, then target node must be in the value tree
				if (node != null) {
					if (!(node instanceof ValueTreeProbabilisticNode)) {
						// note: at this point, the target was non-null and non-empty, so the caller is trying to do a trade on value tree
						throw new IllegalArgumentException("Attempted to change probability of value tree, but question " + node + " does not have a value tree.");
					}
					ValueTreeProbabilisticNode root = (ValueTreeProbabilisticNode) node;
					if (root.getValueTree() == null) {
						throw new IllegalStateException(root + " is expected to have a value tree, but the value tree was null." +
								" This is probably a bug in the engine, or an inconsistent version of the library." +
								" Please, verify whether the version of UnBBayes and Markov Engine are compatible each other.");
					}
					// move along the path to identify the target
					List<IValueTreeNode> children = root.getValueTree().get1stLevelNodes();
					for (Integer index : targetPath) {
						if (children == null || index == null || index < 0 || index >= children.size()) {
							throw new IllegalArgumentException(index + " in path " + targetPath + " is an invalid index as a path in the value tree of question " + questionId);
						}
						children = children.get(index).getChildren();
					}
					// at this point, targetPath is pointing to an existing node in the value tree
					// note: no need to check reference node, because at this point we know that reference node is either null, empty, or ancestor of target.
				}
				
				// check presence of assumption
				if (assumptionIds != null && !assumptionIds.isEmpty()) {
					// try to convert the list of IDs of assumptions, to list of actual nodes. Also check if assumptions exist
					List<INode> assumptionNodes = new ArrayList<INode>(assumptionIds.size()+1);	// allocate space sufficient for all assumptions + traded node
					boolean hasNodes = true;	// this will become false if at least 1 node does not exist yet
					for (Long assumptionId : assumptionIds) {
						// check if node exist
						Node assumptionNode = getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(""+assumptionId);
						if (assumptionNode == null) {
							hasNodes = false;
							break;
						}
						assumptionNodes.add(assumptionNode);
					}
					
					// check if there is a clique containing all assumptions and traded node (if we can check it now)
					boolean hasClique = false;
					if (hasNodes && node != null) {
						// the clique to find must have traded node and all assumptions.
						assumptionNodes.add(node);
						List<Clique> cliquesContainingAllNodes = getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getJunctionTree().getCliquesContainingAllNodes(assumptionNodes, 1);
						// if the returned collection is non-null and non-empty, then we have a clique.
						hasClique = (cliquesContainingAllNodes != null && !cliquesContainingAllNodes.isEmpty());
					}
					
					// handle both issues (inexisting node or clique) here at once
					if (!hasNodes || !hasClique) {
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
								throw new InvalidAssumptionException("A problem was found when handling assumptions " + assumptionIds
										+ "of question " + questionId
										+ ((!hasNodes)?". Some questions were not found.":". No common clique was found."));
							}
						} else {
							throw new InvalidAssumptionException("A problem was found when handling assumptions " + assumptionIds
									+ "of question " + questionId
									+ ((!hasNodes)?". Some questions were not found.":". No common clique was found."));
						}
					}
					
					// at this point, assumptions exist
					// if we have assumptions, then the target node must be shadow node
					// or a parent of a shadow node if newValues doesn't contain null or newValues.size() > 1.
					if (node != null) {
						if (!(node instanceof ValueTreeProbabilisticNode) || ((ValueTreeProbabilisticNode) node).getValueTree() == null) {
							throw new IllegalArgumentException("Attempted to make a trade on path " + targetPath + " of node " + node
									+ ", but the node doesn't seem to have a value tree.");
						}
						ValueTreeProbabilisticNode root = (ValueTreeProbabilisticNode) node;
						// find the shadow node from the node itself
						if (newValues.size() == 1) {
							// check if target node itself is shadow node
							List<IValueTreeNode> children = root.getValueTree().get1stLevelNodes();
							IValueTreeNode shadow = null;
							for (Integer index : targetPath) {
								if (children == null || index == null || index < 0 || index >= children.size()) {
									throw new IllegalArgumentException(index + " in path " + targetPath + " doesn't look like a path in the value tree of question " 
											+ questionId);
								}
								shadow = children.get(index);
								children = shadow.getChildren();
							}
							if (shadow != null) {
								shadowNodeIndex = root.getValueTree().getShadowNodeStateIndex(shadow);
							}
							if (shadowNodeIndex < 0) {
								// note: if shadow == null, then shadowNodeIndex is -1 anyway
								throw new IllegalArgumentException("A trade to question "
										+ questionId + " assuming " + assumptionIds 
										+ " is expected to be targetted to a shadow node, but the path " + targetPath + " doesn't seem to be a shadow node.");
							}
						} else  {
							// special case: check if this is a parent of all shadow nodes
							// check if target node itself is shadow node
							List<IValueTreeNode> children = root.getValueTree().get1stLevelNodes();
							IValueTreeNode parentOfShadow = null;
							for (Integer index : targetPath) {
								if (children == null || index == null || index < 0 || index >= children.size()) {
									throw new IllegalArgumentException(index + " in path " + targetPath + " doesn't look like a path in the value tree of question " 
											+ questionId);
								}
								parentOfShadow = children.get(index);
								children = parentOfShadow.getChildren();
							}
							if (parentOfShadow == null || parentOfShadow.getChildren() == null) {
								throw new IllegalArgumentException("A trade to question "
										+ questionId + " assuming " + assumptionIds 
										+ " is expected to be targetted to a parent of all shadow nodes, but the path " + targetPath + " doesn't seem to be a parent of all shadow nodes.");
							}
							// at this point, parentOfShadow is non-null and parentOfShadow.getChildren() are non-null too
							for (IValueTreeNode shadow : parentOfShadow.getChildren()) {
								if (root.getValueTree().getShadowNodeStateIndex(shadow) < 0) {
									throw new IllegalArgumentException("A trade to question "
											+ questionId + " assuming " + assumptionIds 
											+ " is expected to be targetted to a shadow node, but the path " + targetPath + " doesn't seem to be a shadow node.");
								}
							}
						} 
					} else {	// node is null
						// find the shadow node from the action which will create the node
						if (actionAddingNodeIfNodeIsNull == null) {
							throw new IllegalArgumentException("Attempting to trade on value tree, but the current transaction (ID=" 
									+ transactionKey + ") is not creating question " + questionId 
									+ " as a node with value trees.");
						}
						if (actionAddingNodeIfNodeIsNull instanceof AddValueTreeQuestionNetworkAction) {
							AddValueTreeQuestionNetworkAction valueTreeAction = (AddValueTreeQuestionNetworkAction) actionAddingNodeIfNodeIsNull;
							if (newValues.size() == 1) {
								// check if target node itself is shadow node
								List<IValueTreeNode> children = valueTreeAction.getChildrenOfRootOfValueTree();
								IValueTreeNode shadow = null;
								for (Integer index : targetPath) {
									if (children == null || index == null || index < 0 || index >= children.size()) {
										throw new IllegalArgumentException(index + " in path " + targetPath + " doesn't look like a path in the value tree of question " 
												+ questionId + ", which is a question still to be created in transaction " + transactionKey);
									}
									shadow = children.get(index);
									children = shadow.getChildren();
								}
								if (shadow != null) {
									shadowNodeIndex = valueTreeAction.getShadowNodes().indexOf(shadow);
								}
								if (shadowNodeIndex < 0) {
									// note: if shadow == null, then shadowNodeIndex is -1 anyway
									throw new IllegalArgumentException("A trade to question "
											+ questionId + " (yet to be created on transaction " + transactionKey + ") assuming " + assumptionIds 
											+ " is expected to be targetted to a shadow node, but the path " + targetPath + " doesn't seem to be a shadow node.");
								}
							} else  {
								// special case: check if this is a parent of all shadow nodes
								// check if target node itself is shadow node
								List<IValueTreeNode> children = valueTreeAction.getChildrenOfRootOfValueTree();
								IValueTreeNode parentOfShadow = null;
								for (Integer index : targetPath) {
									if (children == null || index == null || index < 0 || index >= children.size()) {
										throw new IllegalArgumentException(index + " in path " + targetPath + " doesn't look like a path in the value tree of question " 
												+ questionId + ", which is a question still to be created in transaction " + transactionKey);
									}
									parentOfShadow = children.get(index);
									children = parentOfShadow.getChildren();
								}
								if (parentOfShadow == null || parentOfShadow.getChildren() == null) {
									throw new IllegalArgumentException("A trade to question "
											+ questionId + " (yet to be created on transaction " + transactionKey + ") assuming " + assumptionIds 
											+ " is expected to be targetted to a parent of all shadow nodes, but the path " + targetPath + " doesn't seem to be a parent of all shadow nodes.");
								}
								// at this point, parentOfShadow is non-null and parentOfShadow.getChildren() are non-null too
								for (IValueTreeNode shadow : parentOfShadow.getChildren()) {
									if (!valueTreeAction.getShadowNodes().contains(shadow)) {
										throw new IllegalArgumentException("A trade to question "
												+ questionId + " (yet to be created on transaction " + transactionKey + ") assuming " + assumptionIds 
												+ " is expected to be targetted to a shadow node, but the path " + targetPath + " doesn't seem to be a shadow node.");
									}
								}
							} 
						} else {
							throw new IllegalArgumentException("Attempting to trade on value tree, but the current transaction (ID=" 
									+ transactionKey + ") is not creating question " + questionId 
									+ " as a node with value trees.");
						}
					} // end if (node != null) else
				}	// end if there are assumptions
			}	// end sync
		}	// end sync
		
		// first, check that we could extract the number of states with no problem
		if (numStates < 0) {
			throw new RuntimeException("Could not extract the quantity of states/choices of question " + questionId);
		}
		
		// note: at this point, if there are assumptions, then we are trading on shadow nodes. In such case, we can do normal trade.
		if (assumptionIds != null && !assumptionIds.isEmpty()) {
			
			// we need to set newValues to full have same size of the states of the nodes (but can have null indicating "unspecified").
			if (newValues.size() == 1 ) {
				// change newValues so that it has all null and 1 value as the specified in newValues
				if (shadowNodeIndex >= 0) {
					// store old prob before filling list of new values with nulls
					Float prob = newValues.get(0);
					// use new instance, so that we don't change the original list (a list provided by the caller of this method)
					newValues = new ArrayList<Float>(numStates);
					for (int i = 0; i < numStates; i++) {
						// fill non-specified states with nulls.
						newValues.add((shadowNodeIndex == i)?prob:null);
					}
				} else {
					throw new IllegalArgumentException("Question " + questionId + " has value tree, and this is a trade that has assumptions " + assumptionIds
							+ ", but the shadow node was not found.");
				}
			} else if (newValues.size() != numStates) {
				throw new IllegalArgumentException("Question " + questionId + " has " + numStates + " possible choices/shadow nodes, and the size of provided probability" 
						+ newValues + " did not match with it.");
			}
		}
		
		if (assumptionIds != null && !assumptionIds.isEmpty()) {
			// just trade as in old way
			if (transactionKey == null) {
				transactionKey = this.startNetworkActions();
				AddTradeNetworkAction newAction = new AddTradeNetworkAction(
						transactionKey, occurredWhen, "", Long.MIN_VALUE, questionId, 
						null, newValues, 
						assumptionIds, assumedStates, true
					);
				this.addNetworkAction(transactionKey, newAction);
				this.commitNetworkActions(transactionKey);
			} else {
				// instantiate the action object for adding trade
				AddTradeNetworkAction newAction = new AddTradeNetworkAction(
						transactionKey, occurredWhen, "", Long.MIN_VALUE, questionId, 
						null, newValues, 
						assumptionIds, assumedStates, true
					);
				this.addNetworkAction(transactionKey, newAction);
			}
		} else {
			// trade on value tree assuming nothing else
			if (transactionKey == null) {
				transactionKey = this.startNetworkActions();
				AddTradeValueTreeNetworkAction newAction = new AddTradeValueTreeNetworkAction(
						transactionKey, occurredWhen, questionId, newValues, 
						targetPath, referencePath
					);
				this.addNetworkAction(transactionKey, newAction);
				this.commitNetworkActions(transactionKey);
			} else {
				// instantiate the action object for adding trade
				AddTradeValueTreeNetworkAction newAction = new AddTradeValueTreeNetworkAction(
						transactionKey, occurredWhen, questionId, newValues, 
						targetPath, referencePath
					);
				this.addNetworkAction(transactionKey, newAction);
			}
		}
		
		// return the previewed asset values
		return ret;
	
	}
	
	/**
	 * Just delegates to {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)}.
	 * This delegation is not in the opposite direction, because {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)}
	 * is currently being used more frequently than this method, so we want to make the call stack smaller.
	 * In future implementations, {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)} will be 
	 * delegating to this method instead.
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addTrade(java.lang.Long, java.util.Date, java.lang.String, edu.gmu.ace.scicast.TradeSpecification, boolean)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, TradeSpecification tradeSpecification, boolean allowNegative) throws IllegalArgumentException {
		// automatically set allowNegative == true when isToAddArcsOnlyToProbabilisticNetwork().
		allowNegative = allowNegative || isToAddArcsOnlyToProbabilisticNetwork();
		
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
			Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
			
			
			if (mapTransactionQuestions == null || transactionKey == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				throw e;
			}
			
			// use the mapping in order to check whether the parent will be created in the same transaction
			synchronized (mapTransactionQuestions) {
				Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
				if (questionsInSameTransaction == null) {
					throw e;
				}
				// search for the assumption ID in questionsToBeCreatedInTransaction
				for (AddQuestionNetworkAction questionActInMapping : questionsInSameTransaction) {
					if (questionActInMapping.getQuestionId().equals(e.getQuestionId())) {
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
	 * Similar to {@link ResolveQuestionNetworkAction},
	 * but it resolves a question having value trees.
	 * @author Shou Matsumoto
	 */
	public class ResolveValueTreeNetworkAction extends ResolveQuestionNetworkAction {
		private static final long serialVersionUID = 4931668616942760741L;
		private List<List<Integer>> targetPaths;
		private List<List<Integer>> referencePaths;
		private List<List<Float>> settlements;
		private boolean isToForceQuestionRemoval;

		/** Default constructor initializing fields  */
		public ResolveValueTreeNetworkAction(Long transactionKey,
				Date occurredWhen, long questionId,
				List<List<Integer>> targetPaths,
				List<List<Integer>> referencePaths,
				List<List<Float>> settlements, boolean isToForceQuestionRemoval) {
			super(transactionKey, occurredWhen, questionId, null);
			this.setToForceQuestionRemoval(isToForceQuestionRemoval);
			this.setTargetPaths(targetPaths);
			this.setReferencePaths(referencePaths);
			this.setSettlements(settlements);
		}
		
		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.ResolveQuestionNetworkAction#execute()
		 */
		public void execute() {
			

			// reset cache of complexity factors of each existing arc
			Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
			if (arcComplexityCache == null) {
				setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
			} else {
				arcComplexityCache.clear();
			}
			
			
			// change probability of each target, but not changing probability of targets already traded
			Set<IValueTreeNode> targetsHandled = new HashSet<IValueTreeNode>(getTargetPaths().size());
			
			// keep value tree handy, so that we can use its code to extract nodes or to do other types of queries
			IValueTree valueTree = null;
			// before that, get the traded node
			Node node = getProbabilisticNetwork().getNode(getQuestionId().toString());
			if (node == null ) {
				throw new NullPointerException("Question " + getQuestionId() + " doesn't seem to exist.");
			}
			if (!(node instanceof ValueTreeProbabilisticNode)) {
				throw new ClassCastException("Question " + getQuestionId() + " doesn't seem to have a value tree.");
			}
			// finally, extract the value tree from the node we have.
			valueTree = ((ValueTreeProbabilisticNode) node).getValueTree();
			if (valueTree == null) {
				// there is nothing to do
				throw new NullPointerException("Attempted to perform a trade on null value tree in question " + getQuestionId());
			}
			
			// for each specified target node, do the trade
			for (int i = 0; i < getTargetPaths().size(); i++) {
				List<Integer> targetPath = getTargetPaths().get(i);
				List<Integer> referencePath = null;
				if (getReferencePaths() != null && i < getReferencePaths().size()) {
					// the reference path may be smaller than the target
					// (i.e. some trades may be performed without specifying the reference - in such case, it is a trade which the anchor is the root of value tree)
					referencePath = getReferencePaths().get(i);
				}
				// reuse the code in add trade action, because we are basically doing series of trades
				new AddTradeValueTreeNetworkAction(
						getTransactionKey(), 
						getWhenCreated(), 
						getQuestionId(), 
						getSettlements().get(i), 	// the size of settlment is supposedly equal or larger than targets
						targetPath, 
						referencePath, 
						targetsHandled
					).execute();
				// in the next iteration, the vt node we just changed shall not be changed.
				targetsHandled.add(valueTree.getNodeInPath(targetPath));	// if the previous execution has passed without exception, then target node supposedly exist.
			}
			
			// check if any value tree node has settled to 100%. If so, question is closed.
			for (IValueTreeNode valueTreeNode : valueTree.getNodes()) {	
				if (isToForceQuestionRemoval()	// if it was set to force this node to be deleted, then delete it
						|| Math.abs(valueTree.getProb(valueTreeNode, null) - 1d) < getDefaultInferenceAlgorithm().ERROR_MARGIN) { // or else, check if there is any node in 100%
					// TODO only check nodes which had factions changed, perhaps by using the faction change listener
					// this value was settled to approximately 100%, so needs to delete node.
					List<Float> marginals = null;
					if (isToObtainProbabilityOfResolvedQuestions()) {
						// extract the root of value tree, because we'll access it a lot in order to get marginals
						TreeVariable root = (TreeVariable) valueTree.getRoot();
						// store the settlements (of shadow nodes) before we delete the node
						marginals = new ArrayList<Float>(root.getStatesSize());
						for (int i = 0; i < root.getStatesSize(); i++) {
							marginals.add(root.getMarginalAt(i));
						}
					}
					// Supposedly, 100% can only happen if a faction is 100%. Delete the question itself if so
					getDefaultInferenceAlgorithm().setAsPermanentEvidence(
							node, 	// this node will be deleted, because a state became 100%
							null, 	// passing null indicates that probability won't be changed
							true);	// true means node will be deleted (actually, absorbed).

					if (isToObtainProbabilityOfResolvedQuestions() && marginals != null) {
						synchronized (getResolvedQuestions()) {
							// this mapping contains the questions which were deleted by settlement
							getResolvedQuestions().put(getQuestionId(), new StatePair(marginals));
						}
					}
					break;	// no need to check other nodes, because supposedly only 1 node is settled to 100%.
				}
			}
			
		}

		/* (non-Javadoc)
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.ResolveQuestionNetworkAction#getSettlement()
		 */
		public List<Float> getSettlement() {
			List<Float> ret = new ArrayList<Float>(getSettlements().size());
			for (List<Float> probs : getSettlements()) {
				if (probs.size() == 1) {
					ret.add(probs.get(0));
				} else {
					// impossible to convert this setttlement as a list of single probabilities.
					return null;
				}
			}
			return ret;
		}
		/** Do not be detected as a hard evidence or else it will be integrated with other settlements (and we won't like it to happen). */
		public boolean isHardEvidenceAction() { return false;  }
		public List<List<Float>> getSettlements() { return settlements; }
		public void setSettlements(List<List<Float>> settlements) { this.settlements = settlements; }
		public List<List<Integer>> getReferencePaths() { return referencePaths; }
		public void setReferencePaths(List<List<Integer>> referencePaths) { this.referencePaths = referencePaths; }
		public List<List<Integer>> getTargetPaths() { return targetPaths; }
		public void setTargetPaths(List<List<Integer>> targetPaths) { this.targetPaths = targetPaths; }

		/**
		 * @return the isToForceQuestionRemoval
		 */
		public boolean isToForceQuestionRemoval() {
			return isToForceQuestionRemoval;
		}

		/**
		 * @param isToForceQuestionRemoval the isToForceQuestionRemoval to set
		 */
		public void setToForceQuestionRemoval(boolean isToForceQuestionRemoval) {
			this.isToForceQuestionRemoval = isToForceQuestionRemoval;
		}
		
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
		private final List<Float> settlement;
		private List<Float> marginalWhenResolved;
		private long whenExecutedFirst = -1;
		/** Default constructor initializing fields */
		public ResolveQuestionNetworkAction (long transactionKey, Date occurredWhen, long questionId, List<Float> settlement) {
			this.transactionKey = transactionKey;
			this.occurredWhen = (occurredWhen == null)?-1:occurredWhen.getTime();
			this.questionId = questionId;
			this.settlement = settlement;
		}
		public void execute() {
			
			long currentTimeMillis = System.currentTimeMillis();
			

			// reset cache of complexity factors of each existing arc
			Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
			if (arcComplexityCache == null) {
				setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
			} else {
				arcComplexityCache.clear();
			}
			
			
			TreeVariable probNode = null;
			synchronized (getProbabilisticNetwork()) {
				probNode = (TreeVariable) getProbabilisticNetwork().getNode(Long.toString(questionId));
				if (probNode != null) {	
					// if probNode.hasEvidence(), then it was resolved already
					if (probNode.hasEvidence() 
//							&& probNode.getEvidence() != settledState
							) {
						throw new RuntimeException("Attempted to resolve question " + questionId + " to " + getSettlement() + ", but it was already resolved to " + probNode.getEvidence());
					}
					// extract marginal 
					marginalWhenResolved = new ArrayList<Float>(probNode.getStatesSize());
					for (int i = 0; i < probNode.getStatesSize(); i++) {
						marginalWhenResolved.add(probNode.getMarginalAt(i));
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
							Map<Long, List<Float>> marginalsBeforeResolution = null;
							Map<Clique, PotentialTable> previousCliquePotentials = null;
							if (isToTraceHistory()) {
								// backup marginals, so that we can make comparison and add history of indirect changes in marginals
								marginalsBeforeResolution = getProbLists(null, null, null);
								// backup clique potentials
								previousCliquePotentials = getCurrentCliquePotentials();
							}
							
							// check if we need to take some 0% state out from it
							if (isMovingZeroProbability(getSettlement(), getProbList((TreeVariable) node))) {
								if (isToAllowNonBayesianUpdate()) {
									moveUpZeroProbability();
								} else {
									throw new IllegalArgumentException("Attempting to settle an impossible state of question " + questionId + " to a probability other than 0%.");
								}
							}
							
							// actually add hard evidences and propagate (only on BN, because getDefaultInferenceAlgorithm().isToUpdateAssets() == false)
							getDefaultInferenceAlgorithm().setAsPermanentEvidence(node, getSettlement(), isToDeleteResolvedNode());
							
							if (isToTraceHistory()) {
								// store marginals in history
								addVirtualTradeIntoMarginalHistory(this, marginalsBeforeResolution);	
								// store cliques in history of conditional probability (w/ limited size)
								addToLastNCliquePotentialMap(this, previousCliquePotentials);	
							}
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
											assetAlgorithm.setAsPermanentEvidence(Collections.singletonMap(assetNode, getSettlement()),isToDeleteResolvedNode());
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
									assetAlgorithm.setAsPermanentEvidence(Collections.singletonMap(assetNode, getSettlement()),isToDeleteResolvedNode());
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
			if (isToObtainProbabilityOfResolvedQuestions()) {
				// extract what is the settled state, so that we can use it to check whether this was a negative hard evidence
				// (which shall not be considered actually as "resolved" - because we only resolved a state)
				Integer settledState = this.getSettledState();
				synchronized (getResolvedQuestions()) {
					if (settledState == null || settledState >= 0) {
						getResolvedQuestions().put(getQuestionId(), new StatePair(getSettlement()));
					} // else, this is a negative finding, so shall not be considered as actually "resolved"
				}
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
		/**
		 * Uses {@link AssetAwareInferenceAlgorithm#getEvidenceType(List)}
		 * to determine whether {@link #getSettlement()} is a hard evidence.
		 * @see edu.gmu.ace.scicast.NetworkAction#isHardEvidenceAction()
		 */
		public boolean isHardEvidenceAction() {
			synchronized (getDefaultInferenceAlgorithm()) {
				EvidenceType evidenceType = getDefaultInferenceAlgorithm().getEvidenceType(settlement);
				return (evidenceType == EvidenceType.HARD_EVIDENCE || evidenceType == EvidenceType.NEGATIVE_HARD_EVIDENCE);
			}
		}
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

		public Integer getSettledState() {
			synchronized (getDefaultInferenceAlgorithm()) {
				return getDefaultInferenceAlgorithm().getResolvedState(getSettlement());
			}
		}
		public List<Float> getSettlement() { return settlement; }
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
			super(transactionKey, occurredWhen, Long.MIN_VALUE, null);
			if (resolutions == null || resolutions.isEmpty()) {
				throw new IllegalArgumentException("Invalid resolution: "+ resolutions);
			}
			this.resolutions = resolutions;
		}

		/** @see edu.gmu.ace.scicast.MarkovEngineImpl.ResolveQuestionNetworkAction#execute() */
		public void execute() {
			long currentTimeMillis = System.currentTimeMillis();
			

			// reset cache of complexity factors of each existing arc
			Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
			if (arcComplexityCache == null) {
				setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
			} else {
				arcComplexityCache.clear();
			}
			
			
			// mapping to be used to update history of resolved nodes at the end of this action
			Map<Long, StatePair> mapForHistory = new HashMap<Long, MarkovEngineImpl.StatePair>();
			
			synchronized (getProbabilisticNetwork()) {
				
				// map of evidences to be passed to the algorithm in order to resolve all questions in 1 step
				// A tree map with name comparator will allow me to use the same map in BN and asset net 
				// (i.e. keys - nodes - with same name will be considered as same key, although they are different instances)
				Map<INode, List<Float>> mapOfEvidences = new TreeMap<INode, List<Float>>(new Comparator<INode>() {
					public int compare(INode o1, INode o2) {
//						if (o1 instanceof IRandomVariable && o2 instanceof IRandomVariable) {
//							return ((IRandomVariable)o1).getInternalIdentificator() - ((IRandomVariable)o2).getInternalIdentificator();
//						}
//						return o1.getName().compareTo(o2.getName());
						return ((IRandomVariable)o1).getInternalIdentificator() - ((IRandomVariable)o2).getInternalIdentificator();
					}
				});
				
				// check consistency and fill the map of evidences and the map for the history
				boolean hasMovedUpFrom0Already = false;	// this is not to run moveUpZeroProbability multiple times
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
//						if (probNode.getEvidence() != action.getSettledState()) {	// note: probNode.getEvidence() < 0 if there is no evidence
							// store the marginal of this node before the resolution
							ArrayList<Float> marginalBeforeResolution = new ArrayList<Float>(probNode.getStatesSize());
							for (int i = 0; i < probNode.getStatesSize(); i++) {
								marginalBeforeResolution.add(probNode.getMarginalAt(i));
							}
							action.setMarginalWhenResolved(marginalBeforeResolution);
//						}
						
						// check if we need to take some 0% state out from it
						if (!hasMovedUpFrom0Already 
								&& isMovingZeroProbability(action.getSettlement(), marginalBeforeResolution)) {
							if (isToAllowNonBayesianUpdate()) {
								moveUpZeroProbability();
								hasMovedUpFrom0Already = true;
							} else {
								throw new IllegalArgumentException("Attempting to settle an impossible state of question " + action.getQuestionId() + " to a probability other than 0%.");
							}
						}
						
						// in the map which will be passed to the algorithm, mark that this node has an evidence at this state
						mapOfEvidences.put(probNode, action.getSettlement());
						// update the map to be used to update the history all at once at the end of this method
						mapForHistory.put(action.getQuestionId(), new StatePair(action.getSettlement()));
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
											assetAlgorithm.setAsPermanentEvidence(Collections.singletonMap((INode)assetAlgorithm.getAssetNetwork().getNode(resolution.getQuestionId().toString()), resolution.getSettlement()), isToDeleteResolvedNode());
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
			if (isToObtainProbabilityOfResolvedQuestions()) {
				for (Entry<Long, StatePair> entry : mapForHistory.entrySet()) {
					// extract what is the settled state, so that we can use it to check whether this was a negative hard evidence
					Integer settledState = entry.getValue().getResolvedState();
					synchronized (getResolvedQuestions()) {
						if (settledState == null || settledState >= 0) {
							// only include those which are not negative finding (in negative finding we are resolving states rather than questions)
							getResolvedQuestions().put(entry.getKey(),entry.getValue());
						} // else, this is a negative finding, so shall not be considered as actually "resolved"
					}
				}
			}
			
			try {
				Debug.println(getClass(), "Executed resolve set, " + ((System.currentTimeMillis()-currentTimeMillis)));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		/** The set of ResolveQuestionNetworkAction which this action integrates */
		public Collection<ResolveQuestionNetworkAction> getResolutions() { return resolutions; }

		/** @see edu.gmu.ace.scicast.MarkovEngineImpl.ResolveQuestionNetworkAction#setWhenExecutedFirstTime(java.util.Date)*/
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) {
			super.setWhenExecutedFirstTime(whenExecutedFirst);
			for (ResolveQuestionNetworkAction action : getResolutions()) {
				action.setWhenExecutedFirstTime(whenExecutedFirst);
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#resolveQuestion(long, java.util.Date, long, int)
	 */
	public boolean resolveQuestion(Long transactionKey, Date occurredWhen, long questionId, int settledState) throws IllegalArgumentException {
		
		// if settledState is negative, then it is specifying a negative finding (finding that sets state to 0% and does not delete the question)
		boolean isNegativeFinding = settledState < 0;
		if (isNegativeFinding) {
			// make sure settledState points to the state to be settled, 
			// because when this is a negative finding, the state to be set to 0% is -settledState - 1.
			settledState = -settledState - 1;
		}
		
		// extract the node identified by questionId, so that we can know how many states it has, 
		// and from that generate a list of probability of settlement (which its size shall be equal to the number of states)
		Node node = null;
		synchronized (getProbabilisticNetwork()) {
			node = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		// this will store how many states the question have, so that we can create a list of floats with same size, to represent its probability
		int stateSize = -1;
		// node may be something yet to be created in the transaction. Check if that condition apply
		if (node == null) {
			// check if node will be created in same transaction
			boolean isOK = false;
			if (transactionKey != null) {
				Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
				if (mapTransactionQuestions == null) {
					// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
					throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
				}
				
				// use the mapping in order to check whether the parent will be created in the same transaction
				synchronized (mapTransactionQuestions) {
					Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
					if (questionsInSameTransaction == null) {
						throw new InexistingQuestionException("Question ID " + questionId + " does not exist and is not specified in transaction : " + transactionKey, questionId);
					}
					// search for the assumption ID in questionsToBeCreatedInTransaction
					for (AddQuestionNetworkAction questionActionInMapping : questionsInSameTransaction) {
						if (questionActionInMapping.getQuestionId().longValue() == questionId) {
							stateSize = questionActionInMapping.getNumberStates();
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
			// node != null at this point. Check consistency of the argument "settledState"
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
			// remember how many states question has
			stateSize = node.getStatesSize();
		}
		
		// create a list of floats containing 1 in settledState and 0 in all other states, so that we can pass it to the method responsible for creating a network action for resolving questions
		List<Float> settlement = new ArrayList<Float>(stateSize);
		for (int i = 0; i < stateSize; i++) {
			// add 1 if this state is settledState (0 if negative finding). Add 0 (null if negative finding) otherwise.
			settlement.add((settledState == i)?(isNegativeFinding?0f:1f):(isNegativeFinding?null:0f));
		}
		
		// simply delegate to the method which accepts the list of settlement
		return this.resolveQuestion(transactionKey, occurredWhen, questionId, settlement);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#resolveQuestion(java.lang.Long, java.util.Date, long, java.util.List)
	 */
	public boolean resolveQuestion(Long transactionKey, Date occurredWhen, long questionId, List<Float> settlement ) throws IllegalArgumentException{
		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (settlement == null) {
			throw new IllegalArgumentException("No settlement was specified for question " + questionId);
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
//						List<NetworkAction> actions = getNetworkActionsMap().get(transactionKey);
//						if (actions == null) {
//							throw new IllegalArgumentException("Transaction key " + transactionKey + " was not started or was concluded.");
//						}
//						synchronized (actions) {
//							for (NetworkAction networkAction : actions) {
//								if (networkAction instanceof AddQuestionNetworkAction
//										&& networkAction.getQuestionId() != null
//										&& networkAction.getQuestionId().longValue() == questionId) {
//									if (settledState >= ((AddQuestionNetworkAction)networkAction).getNumberStates()) {
//										throw new IllegalArgumentException("Question " + questionId + " has no state " + settledState);
//									}
//									isOK = true;
//									break;
//								}
//							}
//						}
				Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
				
				
				if (mapTransactionQuestions == null) {
					// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
					throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
				}
				
				// use the mapping in order to check whether the parent will be created in the same transaction
				synchronized (mapTransactionQuestions) {
					Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
					if (questionsInSameTransaction == null) {
						throw new InexistingQuestionException("Question ID " + questionId + " does not exist and is not specified in transaction : " + transactionKey, questionId);
					}
					// search for the assumption ID in questionsToBeCreatedInTransaction
					for (AddQuestionNetworkAction idInMapping : questionsInSameTransaction) {
						if (idInMapping.getQuestionId().longValue() == questionId) {
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
			if (settlement.size() != node.getStatesSize()) {
				throw new IllegalArgumentException("The list of probability provided to question " + questionId + " had size " + settlement.size() 
						+ ", but the question actually had " + node.getStatesSize() + " states.");
			}
			if (node instanceof TreeVariable ) {
				TreeVariable var = (TreeVariable) node;
				if (var.hasEvidence()) {
//					int state = (settledState<0)?((-settledState)-1):settledState;
//					if (var.getEvidence() != state || var.getMarginalAt(state) <= 0f) {
						throw new IllegalArgumentException("Question " + questionId + " is already resolved.");
//					}
				}
			}
		}
		
		// check if settlement can be categorized to a supported type of evidence
		synchronized (getDefaultInferenceAlgorithm()) {
			if (getDefaultInferenceAlgorithm().getEvidenceType(settlement) == null) {
				throw new IllegalArgumentException(settlement + " could not be categorized into any supported type of settlement for question " + questionId);
			}
		}
		
		
		
		// create network actions
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			// instantiate the action object for adding a question
			this.addNetworkAction(transactionKey, new ResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settlement));
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for adding a question
			this.addNetworkAction(transactionKey, new ResolveQuestionNetworkAction(transactionKey, occurredWhen, questionId, settlement));
		}
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#resolveValueTreeQuestion(java.lang.Long, java.util.Date, long, java.util.List, java.util.List, java.util.List)
	 */
	public boolean resolveValueTreeQuestion(Long transactionKey,
			Date occurredWhen, long questionId,
			List<List<Integer>> targetPaths,
			List<List<Integer>> referencePaths, List<List<Float>> settlements)
			throws IllegalArgumentException {
		return this.resolveValueTreeQuestion(transactionKey, occurredWhen, questionId, targetPaths, referencePaths, settlements, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#resolveValueTreeQuestion(java.lang.Long, java.util.Date, long, java.util.List, java.util.List, java.util.List, boolean)
	 */
	public boolean resolveValueTreeQuestion(Long transactionKey, Date occurredWhen, long questionId, 
			List<List<Integer>> targetPaths, List<List<Integer>> referencePaths,List<List<Float>> settlements, 
			boolean isToForceQuestionRemoval) 
			throws IllegalArgumentException {
		
		// basic assertions
		if (settlements == null || settlements.isEmpty()
				|| targetPaths == null || targetPaths.isEmpty()) {
			return false;
		}
		if (transactionKey != null && this.getNetworkActionsMap().get(transactionKey) == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		// if date was null, consider it as now
		if (occurredWhen == null) {
			occurredWhen = new Date();
		}
		if (targetPaths == null || targetPaths.isEmpty()) {
			throw new IllegalArgumentException("Path to value tree node to resolve was not provided. If you want to settle shadow nodes (i.e. exposed states), you may want to use resolveQuestion instead.");
		}
		
		// make sure all targets has respective settlement
		if (settlements.size() < targetPaths.size()) {
			throw new IllegalArgumentException(targetPaths.size() + " target nodes were specified, but there were only " + settlements.size() 
					+ " settlements specified. They are supposed to have the same size.");
		}

		if (referencePaths != null) {
			int pathSize = Math.min(referencePaths.size(),targetPaths.size());
			for (int i = 0; i < pathSize; i++) {
				List<Integer> referencePath = referencePaths.get(i);
				List<Integer> targetPath = targetPaths.get(i);
				// check that reference path is always an ancestor of target path
				if (referencePath.size() >= targetPath.size()) {
					throw new IllegalArgumentException("The reference path must point to an ancestor of target path.");
				}
				// check if beginning of targetPath is equal to referencePath (i.e. check if referencePath is pointing to ancestor of targetPath)
				// note: at this point, the length of referencePath is strictly smaller than targetPath
				for (int pathStep = 0; pathStep < referencePath.size(); pathStep++) {
					if (!referencePath.get(pathStep).equals(targetPath.get(pathStep))) {
						throw new IllegalArgumentException("The reference path must point to an ancestor of target path. The step " + pathStep  + " of the paths are pointing to different nodes in value trees.");
					}
				}
			}
		}
		
		// at this point, reference path is either null, empty, or pointing to ancestor of target path
		
		
		if (getDefaultInferenceAlgorithm() == null) {
			throw new IllegalStateException("Default algorithm not found.");
		}
		
		// check whether question/node exists, and extract necessary nodes if possible
		synchronized (getDefaultInferenceAlgorithm()) {
			synchronized (getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork()) {
				ProbabilisticNetwork net = getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork();
				if (net == null) {
					throw new IllegalStateException("Network was not properly initialized.");
				}
				Node node = net.getNode(""+questionId);
				// this will be filled with the network action which will add the node if node was not found.
				AddValueTreeQuestionNetworkAction actionAddingNodeIfNodeIsNull = null;	
				if (node == null) {
					// Perhaps the nodes are still going to be added within the context of this transaction.
					Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
					
					if (mapTransactionQuestions == null || transactionKey == null) {
						// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
						throw new InexistingQuestionException("Question " + questionId + " not found.",questionId);
					}
					
					// use the mapping in order to check whether the parent will be created in the same transaction
					synchronized (mapTransactionQuestions) {
						Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
						if (questionsInSameTransaction == null) {
							throw new InexistingQuestionException("Question " + questionId + " not found.",questionId);
						}
						// search for the assumption ID in questionsToBeCreatedInTransaction
						for (AddQuestionNetworkAction questionActInMapping : questionsInSameTransaction) {
							if (questionActInMapping.getQuestionId().longValue() == questionId
									&& questionActInMapping instanceof AddValueTreeQuestionNetworkAction) {
								actionAddingNodeIfNodeIsNull = (AddValueTreeQuestionNetworkAction) questionActInMapping;
								break;
							}
						}
					}
					if (actionAddingNodeIfNodeIsNull == null) {
						throw new InexistingQuestionException("Question " + questionId + " not found.",questionId);
					}
					
				}
				
				// this set will contain what target nodes were already checked, so that we can see if new target overlaps (is descendant or ancestor) with prevous ones 
				Set<IValueTreeNode> checkedTargets = new HashSet<IValueTreeNode>();	
				// check if target path is consistent with what the node is
				for (List<Integer> targetPath : targetPaths) {
					if (targetPath == null || targetPath.isEmpty()) {
						throw new IllegalArgumentException("Provided target paths " + targetPaths + " contains null or empty path.");
					}
					List<IValueTreeNode> children = null;	// this will be used while navigating along the path of target
					if (node != null) {
						// if node exists, then target node must be in the value tree
						if (!(node instanceof ValueTreeProbabilisticNode)) {
							// note: at this point, the target was non-null and non-empty, so the caller is trying to do a trade on value tree
							throw new IllegalArgumentException("Attempted to settle value tree, but question " + node + " does not have a value tree.");
						}
						
						ValueTreeProbabilisticNode root = (ValueTreeProbabilisticNode) node;
						if (root.getValueTree() == null) {
							throw new IllegalStateException(root + " is expected to have a value tree, but the value tree was null." +
									" This is probably a bug in the engine, or an inconsistent version of the library." +
									" Please, verify whether the version of UnBBayes and Markov Engine are compatible each other.");
						}
						children = root.getValueTree().get1stLevelNodes();
					} else {
						// find the value tree nodes from the action which will be used to create the node
						children = actionAddingNodeIfNodeIsNull.getChildrenOfRootOfValueTree();
					}
					// move along the path to identify the target. Cannot use ValueTree#.getNodeInPath(path) value tree may not be instantiated yet (due to transaction not committed yet)
					IValueTreeNode target = null;
					for (Integer index : targetPath) {
						if (children == null || index == null || index < 0 || index >= children.size()) {
							throw new IllegalArgumentException(index + " in path " + targetPath + " is an invalid index as a path in the value tree of question " + questionId);
						}
						target = children.get(index);
						children = target.getChildren();
					}
					// note: at this point, target is non-null, because targetPath was non-null, non-empty, and if index was not in children, then it would have thrown exception
					//check that there are no two target paths that have overlaps, because this version does not allow such construction
					for (IValueTreeNode prevTarget : checkedTargets) {
						if (prevTarget.isAncestorOf(target) || target.isAncestorOf(prevTarget)) {
							throw new IllegalArgumentException("This version of ME cannot resolve multiple value tree nodes when specified nodes are descendant of another specified node in same value tree. " + prevTarget + " was detected to be a descendant/ancestor of " + target);
						}
					}
					checkedTargets.add(target);
				}
				
				// at this point, targetPath is pointing to an existing node in the value tree
				// note: no need to check reference node, because at this point we know that reference node is either null, empty, or ancestor of target.
			}
		}
		
		
		// trade on value tree assuming nothing else
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			ResolveValueTreeNetworkAction newAction = new ResolveValueTreeNetworkAction(
					transactionKey, occurredWhen, questionId, targetPaths, referencePaths,
					settlements, isToForceQuestionRemoval
					);
			this.addNetworkAction(transactionKey, newAction);
			this.commitNetworkActions(transactionKey);
		} else {
			// instantiate the action object for adding trade
			ResolveValueTreeNetworkAction newAction = new ResolveValueTreeNetworkAction(
					transactionKey, occurredWhen, questionId, targetPaths, referencePaths,
					settlements, isToForceQuestionRemoval
					);
			this.addNetworkAction(transactionKey, newAction);
		}
		
		
		
		
		return true;
	}
	
	

	/* (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#revertTrade(Long, java.util.Date, java.lang.Long, java.lang.Long)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> getProbList(long questionId, List<Integer> targetPath,  List<Integer> referencePath, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		// if no path is provided, then handle it the same way as the old routine
		if (targetPath == null || targetPath.isEmpty()) {
			return this.getProbList(questionId, assumptionIds, assumedStates);
		}
		// check if reference is a sublist of target
		if (referencePath != null) {
			// check if beginning of targetPath is equal to referencePath (i.e. check if referencePath is pointing to ancestor of targetPath)
			// note: at this point, the length of referencePath is strictly smaller than targetPath
			for (int i = 0; i < Math.min(referencePath.size(),targetPath.size()); i++) {
				if (!referencePath.get(i).equals(targetPath.get(i))) {
					throw new IllegalArgumentException("The reference and target path must be along same path in value tree. The step " + i  + " of the paths are pointing to different nodes in value trees.");
				}
			}
			// if reference is below or equal to target, then probability is 100% anyway
			if (referencePath.size() >= targetPath.size()) {
				return Collections.singletonList(1f);
			}
		}
		
		// extract the root and the target node of value tree
		ValueTreeProbabilisticNode root = null;
		IValueTreeNode target = null;
		synchronized (getDefaultInferenceAlgorithm()) {
			synchronized (getProbabilisticNetwork()) {
				// the root
				root = (ValueTreeProbabilisticNode) getProbabilisticNetwork().getNode(""+questionId);
				if (root == null || root.getValueTree() == null) {
					throw new IllegalArgumentException("Could not find root of value tree identified by the ID " + questionId);
				}
				// navigate along the target path
				target = root.getValueTree().getNodeInPath(targetPath);
			}
		}
		// note: at this point, root is non-null supposedly
		if (target == null) {
			throw new IllegalArgumentException("Could not find target path " + targetPath + " of question " + root);
		}
		
		// check if this is a simple case of query to value tree assuming nothing external
		// also extract the anchor
		IValueTreeNode anchor = null;
		synchronized (getDefaultInferenceAlgorithm()) {
			synchronized (getProbabilisticNetwork()) {
				anchor = root.getValueTree().getNodeInPath(referencePath);
				if (assumptionIds == null || assumptionIds.isEmpty()) {
					// simply query the value tree
					// return the obtained value
					return Collections.singletonList(root.getValueTree().getProb(target, anchor));
				} 
			}
		}
		
		/* At this point, we are calculating the following prob:
		 * If x is a non-shadow node in value tree, a,b,c are either normal states or shadow nodes of external network,
		 * and s is a shadow node which is also an ancestor of x in same value tree, then:
		 * P(x|a,b,c) = p(s|a,b,c)*p(x|s)
		 */
		
		// obtain p(s|a,b,c) by using existing method, because shadow nodes are ordinal states of BN. This should also check whether assumptions are valid
		List<Float> probShadowsGivenAssumptions = this.getProbList(questionId, assumptionIds, assumedStates);
		
		// this will hold the probability of the reference node, so that we'll divide this from the target and we'll get a proper result
		float referenceProb = 1f;
		if (referencePath != null && !referencePath.isEmpty()) {
			List<Float> referenceProbList = this.getProbList(questionId, referencePath, null, assumptionIds, assumedStates);
			if (referenceProbList == null || referenceProbList.size() != 1) {
				throw new IllegalArgumentException("Probability of reference " + referencePath + " is not defined for question " + questionId);
			}
			// at this point, the list has 1 element
			referenceProb = referenceProbList.get(0);
		}
		
		// check if target node is a shadow node itself
		int targetNodeStateIndexIfShadow = root.getValueTree().getShadowNodeStateIndex(target);
		if (targetNodeStateIndexIfShadow >= 0) {
			// simply return from the list of prob of shadow nodes
			return Collections.singletonList(probShadowsGivenAssumptions.get(targetNodeStateIndexIfShadow) / referenceProb);
		}
		
		// check which state is the s and also an ancestor of x
		synchronized (getDefaultInferenceAlgorithm()) {
			synchronized (getProbabilisticNetwork()) {
				int indexOfShadowClosestToTarget = -1;	// this will hold the index of shadow node which is either ancestor or descendant of target node
				IValueTreeNode shadow = null;			// this will hold the shadow node itself
				boolean isAncestorShadow = true;		// this will become false if shadow node was a descendant of target node
				for (int i = 0; i < root.getValueTree().getShadowNodeSize(); i++) {
					// because value tree is a tree and shadow nodes are mutually exclusive, the first one to find is the one we want
					shadow = root.getValueTree().getShadowNode(i);
					if (shadow.isAncestorOf(target)) {
						// shadow node is ancestor of target
						indexOfShadowClosestToTarget = i;
						break;
					} else if (target.isAncestorOf(shadow)) {
						// shadow node is descendant of target
						isAncestorShadow = false;
						indexOfShadowClosestToTarget = i;
						break;
					}
				}
				if (indexOfShadowClosestToTarget < 0 || shadow == null) {
					throw new RuntimeException("Could not find any shadow node which is either descendant or ancestor of target node " + target
							+ ". This indicates that target node is disconnected from value tree, so it is a potential inconsistency.");
				}
				
				// note: if anchor is descendant of target, then it is immediately 100% (this was handled at the beginning of this method),
				// but if anchor is ancestor of target and shadow is ancestor of anchor, then anchor is separating the shadow and target, 
				if (anchor != null && anchor.isAncestorOf(target) && shadow.isAncestorOf(anchor)) {
					// so the prob of shadow won't supposedly affect anchor.
					return Collections.singletonList(root.getValueTree().getProb(target, anchor));
				}
				
				if (isAncestorShadow) {
					// otherwise... I'm assuming that the probability of shadow separates anchor, so anchor does not affect prob of target
					// this is mainly because P(s|anchor,assumptions) is not well defined.
					// TODO verify if when anchor->...->shadow->...->target, then the assumption of anchor is really ignored.
					// retrieve p(x|s). If s is descendant, then p(x|s) = 100%
					float probTargetGivenShadow = isAncestorShadow?root.getValueTree().getProb(target, shadow):1f;
					
					// return the value p(s|a,b,c)*p(x|s)
					return Collections.singletonList(probShadowsGivenAssumptions.get(indexOfShadowClosestToTarget)*probTargetGivenShadow  / referenceProb) ;
				}
				
				// at this point, shadow node is descendant. Simulate the probability of ancestor (target) if descendant (shadow) were at the probability given external nodes
				// use a clone so that we won't break the original
				ValueTreeProbabilisticNode clone = (ValueTreeProbabilisticNode) root.basicClone();
				
				// first, extract the node from cloned value tree
				IValueTreeNode cloneShadow = clone.getValueTree().getNode(shadow.getName());	
				IValueTreeNode cloneAnchor = null;
				if (anchor != null) {
					cloneAnchor = clone.getValueTree().getNode(anchor.getName());
				}
				
				// change probability of shadow node of clone to the value we have assuming the other assumptions
				clone.getValueTree().changeProb(cloneShadow, cloneAnchor, probShadowsGivenAssumptions.get(indexOfShadowClosestToTarget), null);
				
				// extract the probability of target after the propagation and return it
				return Collections.singletonList(clone.getValueTree().getProb(clone.getValueTree().getNode(target.getName()), anchor));
			}
		}
	}
	
		
			
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getProbLists(java.util.List, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
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
						try {
							questionId = Long.parseLong(mainNode.getName());
						} catch (NumberFormatException e) {
							e.printStackTrace();
							if (getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator() instanceof IncrementalJunctionTreeAlgorithm) {
								((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).clearVirtualNodes();
							}
							continue;
						}
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
			IncrementalJunctionTreeAlgorithm jtAlgorithm = new IncrementalJunctionTreeAlgorithm(pn);
			jtAlgorithm.setDynamicJunctionTreeNetSizeThreshold(getDynamicJunctionTreeNetSizeThreshold());
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getPossibleQuestionAssumptions(long, java.util.List)
	 */
	public List<Long> getPossibleQuestionAssumptions(long questionId, List<Long> assumptionIds) throws IllegalArgumentException {
		
		// this is the list to return
		List<Long> ret = new ArrayList<Long>();
		
		// extract main node
		Node mainNode = null;
		synchronized (getProbabilisticNetwork()) {
			mainNode = getProbabilisticNetwork().getNode(Long.toString(questionId));
		}
		if (mainNode == null) {
			throw new InexistingQuestionException("Question " + questionId + " not found.", questionId);
		}
		
		if (this.isRunningApproximation()) { // if we are using loopy BP, then only allow trades on directly connected nodes
			// TODO implement a more clean condition to allow trade with loopy BP
			
			synchronized (getProbabilisticNetwork()) {
				// add IDs of all parent nodes
				for (INode parent : mainNode.getParentNodes()) {
					try {
						ret.add(Long.parseLong(parent.getName()));
					} catch (NumberFormatException e) {
						// invalid node name, but ignore this error because it's not fatal at this point
						Debug.println(getClass(), "Unable to extract question ID of parent " + parent + " of node " + mainNode + ": " + e.getMessage(), e);
					}
				}
				// add IDs of all child nodes
				for (INode child : mainNode.getChildNodes()) {
					try {
						ret.add(Long.parseLong(child.getName()));
					} catch (NumberFormatException e) {
						// invalid node name, but ignore this error because it's not fatal at this point
						Debug.println(getClass(), "Unable to extract question ID of child " + child + " of node " + mainNode + ": " + e.getMessage(), e);
					}
				}
			}
			
			// at this point, ret contains IDs of all nodes that are connected to node identified by questionId
			
			// check if there is any ID in assumptionIds that is not connected to question. 
			List<Long> nonParentOrChildAssumptions = new ArrayList<Long>(assumptionIds);
			nonParentOrChildAssumptions.removeAll(ret);
			
			// If there is any node not connected to the question, then we shall return empty
			if (!nonParentOrChildAssumptions.isEmpty()) {
				return Collections.emptyList();
			}
			
		} else { // ordinal case: allow trade if it is in same clique
			// this object is going to be used to extract what nodes can become assumptions in a conditional soft evidence
			IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
			if (conditionalProbabilityExtractor == null) {
				throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
			}
			if (getProbabilisticNetwork() == null) {
				// there is no way to find a node from a null network
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
			if (returnedNodes != null){
				for (INode node : returnedNodes) {
					if (node != null) {
						// question IDs and node names are supposedly equal
						ret.add(Long.parseLong(node.getName()));
					}
				}
			}
		}
		
		
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getAssetsIfStates(long, long, java.util.List, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Float> getAssetsIfStates(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return null;
		}
		
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getEditLimits(long, long, int, java.util.List, java.util.List)
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
		
		// do not run algorithm related to assets if the engine is set not to use assets
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			// return a list indicating all possible probabilities are allowed (i.e. from 0 to 1)
			List<Float> ret = new ArrayList<Float>(2);
			ret.add(0f);
			ret.add(1f);
			return ret;
		}
		
		// this object is going to be used to extract what nodes can become assumptions in a conditional soft evidence
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = getConditionalProbabilityExtractor();	
		if (conditionalProbabilityExtractor == null) {
			throw new RuntimeException("Could not reuse conditional probability extractor of the current default inference algorithm. Perhaps you are using incompatible version of Markov Engine or UnBBayes.");
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getCash(long, java.util.List, java.util.List)
	 */
	public float getCash(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return Float.NaN;
		}
		
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public float scoreUserQuestionEv(long userId, Long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return Float.NaN;
		}
		
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#scoreUserQuestionEvStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return null;
		}
		// do not compute expected score locally (i.e. compute globally)
		return this.scoreUserQuestionEvStates(userId, questionId, assumptionIds, assumedStates, false);
	}
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#scoreUserQuestionEvStates(long, long, java.util.List, java.util.List, boolean)
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, 
			List<Long>assumptionIds, List<Integer> assumedStates, boolean isToComputeLocally) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return null;
		}
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
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return null;
		}
		
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#scoreUserEv(long, java.util.List, java.util.List)
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return Float.NaN;
		}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#scoreUserEv(long, java.util.List, java.util.List)
	 * @see AssetAwareInferenceAlgorithm#getExpectedAssetCellListeners()
	 * @see AssetAwareInferenceAlgorithm#calculateExpectedAssets()
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates, List<ExpectedAssetCellMultiplicationListener> assetCellListener) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return Float.NaN;
		}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#previewTrade(edu.gmu.ace.scicast.TradeSpecification)
	 */
	public List<Float> previewTrade(TradeSpecification tradeSpecification) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return null;
		}
		return this.previewTrade(tradeSpecification.getUserId(), tradeSpecification.getQuestionId(), tradeSpecification.getProbabilities(), 
				tradeSpecification.getAssumptionIds(), tradeSpecification.getAssumedStates());
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#previewTrade(long, long, java.util.List, java.util.List, java.util.List)
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
		
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			// if we are not using users, then this method should return the asset changes
			List<Float> oldValues = getProbList(questionId, assumptionIds, assumedStates);
			// this is the list to be returned
			List<Float> ret = new ArrayList<Float>(newValues.size());
			if (oldValues.size() < 2) {
				throw new IllegalStateException("Question " + questionId + " seems to have ess than 2 states, so its probability cannot be changed.");
			}
			for (int i = 0; i < newValues.size(); i++) {
				if (!isToAllowNonBayesianUpdate() && oldValues.get(i).equals(0f) && newValues.get(i) > 0f) {
					// there is a 0% state being changed, so cannot use bayesian updating.
					throw new IllegalArgumentException("Probability of question " + questionId + " given " + assumptionIds + "=" + assumedStates + " has probability " + oldValues + " (there is a 0% state being changed), thus cannot be modified with bayesian update.");
				}
				// probs and assets are all supposedly related by indexes
//				if (newValues.get(i) == null) {
					ret.add(0f);
//				} else {
//					if (oldValues.get(i).equals(0f)) {
//						ret.add(this.getScoreFromQValues(Float.NaN));
//					} else {
//						ret.add(this.getScoreFromQValues(newValues.get(i)/oldValues.get(i)));
//					}
//				}
			}
			return ret;
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
				null, // we are not interested in obtaining what other questions' marginals were affected by this trade
				true	// is to update marginal probabilities too
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
	 * This will use non-bayesian update to change all occurrences of 0%
	 * to some very small probability (and then normalize),
	 * so that impossible states becomes possible to happen (and thus, can be updated with bayes rule).
	 * This is called by {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)}
	 * if {@link #isToAllowNonBayesianUpdate()} is true
	 */
	public void moveUpZeroProbability() {
		Debug.println(getClass(), "Invoked non bayesian update method to move up states with 0% probability.");
		
		// the net to verify is the shared BN
		ProbabilisticNetwork net = getProbabilisticNetwork();
		
		// TODO this is a relatively slow operation. See if we can synchronize smaller blocks instead of the whole network
		synchronized (net) {	
			
			// find zeros in junction tree and substitute with a small number
			IJunctionTree junctionTree = net.getJunctionTree();
			if (junctionTree == null) {
				return;
			}
			
			// use the smaller of error margins used in this class by other methods as the value to substitute
//			float valueToSubstitute = Math.min(Math.abs(getProbabilityErrorMargin()/1000f), Math.abs(getProbabilityErrorMarginBalanceTrade()/1000f));
			float valueToSubstitute = Float.MIN_NORMAL;
			
			// iterate on cliques
			for (Clique clique : junctionTree.getCliques()) {
				boolean isChanged = false;
				// extract clique table and iterate on its cells
				PotentialTable table = clique.getProbabilityFunction();
				int tableSize = table.tableSize();
				for (int i = 0; i < tableSize; i++) {
					if (table.getValue(i) <= 0f) {
						// found a zero. Substitute current entry
						table.setValue(i, valueToSubstitute);
						isChanged = true;
					}
				}
				// normalize current table
				if (isChanged) {
					table.normalize();
				}
			}
			// also iterate on separators
			for (Separator separator : junctionTree.getSeparators()) {
				boolean isChanged = false;
				// extract separator table and iterate on its cells
				PotentialTable table = separator.getProbabilityFunction();
				int tableSize = table.tableSize();
				for (int i = 0; i < tableSize; i++) {
					if (table.getValue(i) <= 0f) {
						// found a zero. Substitute current entry
						table.setValue(i, valueToSubstitute);
						isChanged = true;
					}
				}
				// normalize current table
				if (isChanged) {
					table.normalize();
				}
			}
			
			// propagate, just to make sure we have global consistency
			try {
				junctionTree.consistency();
			} catch (Exception e) {
				// it's OK to keep it this way, because the next evidence propagation will force global consistency, and this is an approximation (and a non bayesian update) anyway.
				// but at least print a stack trace indicating that something is happening
				Debug.println(null, "Could not use Junction Tree propagation to force global consistency after moving up 0% probabilities.", e);
			}
			
			// update marginals of nodes too, because they are used as some sort of cache when marginals are used/queried
			// This is the same code of unbbayes.prs.bn.SingleEntityNetwork#updateMarginals(), which is "copied" here because of method visibility (it's protected). 
			// TODO Move this code (and also unbbayes.prs.bn.SingleEntityNetwork#updateMarginals()) to unbbayes.prs.bn.IncrementalJunctionTreeAlgorithm.
			for (Node node : net.getNodes()) {
				if (node.getStatesSize() > 0 && (node instanceof TreeVariable)) {
					((TreeVariable)node).updateMarginal();
				}
			}
		}
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
	 * @param isToUpdateMarginals : if this is false, then marginal probabilities of all nodes will not
	 * be updated at the end of execution of this method. Otherwise, the marginal probabilities will be updated by default.
	 * Use this feature in order to avoid marginal updating when several propagations are expected to be executed in a sequence, and
	 * the marginals are not required to be updated at each propagation (so that we won't run the marginal updating several times
	 * unnecessarily).
	 * @return the probabilities before trade. This list will have the same structure of newValues, but its content will be filled
	 * with the probabilities before applying the trade.
	 * @see #addTrade(long, Date, String, long, long, List, List, List, boolean)
	 * @see #previewTrade(long, long, List, List, List)
	 * @see #getProbabilityErrorMargin()
	 * @see #isToUseCorrectiveTrades()
	 */
	@SuppressWarnings("unchecked")
	protected List<Float> executeTrade(long questionId, List<Float> oldValues, List<Float> newValues, 
			List<Long> assumptionIds, List<Integer> assumedStates, 
			boolean isToAllowNegative, AssetAwareInferenceAlgorithm algorithm, boolean isToUpdateAssumptionIds, boolean isPreview, 
			NetworkAction parentTrade, boolean isToUpdateMarginals) {
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
//		for (int i = 0; i < child.getStatesSize(); i++) {
//			if (child.getMarginalAt(i) == 0.0f || child.getMarginalAt(i) == 1.0f) {
//				if (isToThrowExceptionInTradesToResolvedQuestions()) {
//					throw new IllegalArgumentException("State " + i + " of question " + child + " given " + assumptionIds +  " = " + assumedStates + " has probability " + child.getMarginalAt(i) + " and cannot be changed.");
//				} else {
//					return null;
//				}
//			}
//		}
		
		
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
		float localSum = 0;
		float sumOfNonNullNewValues = 0;	// value of localSum at last iteration. Will be used to calculate non-specified probability
		int counter = 0;	// counter in which possible values are mod childNodeStateSize
		boolean hasUndefinedProb = false;	// will become true if there is null probability
		for (Float probability : newValues) {
			if (probability == null) {
				// we need to adjust this value posteriorly proportionally to current prob
				hasUndefinedProb = true;
				counter++;
				continue;
			}
			if (probability < (0-getProbabilityErrorMargin()) || probability > (1+getProbabilityErrorMargin())) {
				throw new IllegalArgumentException("Invalid probability declaration found: " + probability);
			}
			localSum += probability;
			counter++;
			if (counter >= child.getStatesSize()) {
				// check if sum of conditional probability given current state of parents is 1
				if (!(((1 - getProbabilityErrorMargin()) < localSum) && (localSum < (1 + getProbabilityErrorMargin())))) {
					if (!(((1 - getProbabilityErrorMargin()*2) < localSum) && (localSum < (1 + getProbabilityErrorMargin()*2)))) {
						throw new IllegalArgumentException("Inconsistent or not normalized probability specification: " + localSum);
					}
				}
				counter = 0;
				sumOfNonNullNewValues = localSum;	// simply store the last local sum, because we will use this value only when there is no next iteration
				localSum = 0;
			}
		}
		
		// TODO allow null in probability when states of parents are not specified
		if (hasUndefinedProb && newValues.size() != child.getStatesSize()) {
			throw new UnsupportedOperationException("Current version does not support null values in probabilities when specifying full conditional probability without specifying states of assumptions." +
					" You'll either need to fully specify the states of the parents, or not to use null in the probability argument.");
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
			// if there are unspecified new values, we need to know the sum of old probability of uspecified states, because such value will be used for proportional adjustment
			float sumUnspecifiedOldValues = 0f;
			if (hasUndefinedProb) {
				for (int i = 0; i < newValues.size(); i++) {	// at this point, newValues.size() == child.statesSize()
					multidimensionalCoord[0] = i; // only iterate over states of the main node (i.e. index 0 of multidimensionalCoord)
					if (newValues.get(i) == null) {
						sumUnspecifiedOldValues += potential.getValue(multidimensionalCoord);
					} 
				}
			}
			// modify content of potential table according to newValues 
			for (int i = 0; i < newValues.size(); i++) {	// at this point, newValues.size() == child.statesSize()
				multidimensionalCoord[0] = i; // only iterate over states of the main node (i.e. index 0 of multidimensionalCoord)
				float oldProb = potential.getValue(multidimensionalCoord);
				condProbBeforeTrade.add(oldProb);
				if (newValues.get(i) == null) {
					if (sumUnspecifiedOldValues <= 0f) {
						if (sumOfNonNullNewValues < 0f) {
							// the old values were 0%, but the trade is trying to set to above 0, so this is mathematically impossible
							throw new IllegalArgumentException(newValues + " is attempting to make a impossible state " + i + " to become possible. " +
									"This is semantically incoherent.");
						}
						// in this case, the probability of specified states were already 0, and we are trying to set to 0, so don't change
						potential.setValue(multidimensionalCoord, 0f);
					} else {
						// distribute the remaining probability (1-lastSum) to other states proportionally 
						potential.setValue(multidimensionalCoord, oldProb*(1-sumOfNonNullNewValues)/sumUnspecifiedOldValues);
					}
				} else {
					potential.setValue(multidimensionalCoord, newValues.get(i));
				}
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
		
		
		// check if we are moving up any probability from 0%, because in such case we don't have a bayesian update 
		// (i.e. there is no likelihood ratio to multiply to 0 in order to result in non-zero)
		if (isToAllowNonBayesianUpdate() && isMovingZeroProbability(newValues, condProbBeforeTrade)) {
			this.moveUpZeroProbability();
		}
		
		// connect the nodes used in trade if there is no clique containing all nodes simultaneously
		if (!isPreview && isToAddArcsOnAddTradeAndUpdateJT()) {
			// make connection only if the flag isToAddArcsOnAddTradeAndUpdateJT is turned on
			synchronized (getDefaultInferenceAlgorithm()) {
				// check if there is a clique containing all nodes
				Collection<INode> nodes = new ArrayList<INode>(assumptionNodes);
				nodes.add(child);
				if (getDefaultInferenceAlgorithm().getJunctionTree().getCliquesContainingAllNodes(nodes , 1).isEmpty()) {
					try {
						// if there is no clique containing all nodes, we need to connect them.
						if (isToAddArcsOnAddTrade()) {	// only add connection if flag is turned on
							// actually attempts to add arc
							this.simpleAddEdge((List)assumptionNodes, child, net, false);
						} 
					} catch (InvalidParentException e) {
						throw new RuntimeException("Could not automatically add arc from " + assumptionNodes + " to "+child,e);
					}
				}
			}
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
				if (isToPrintProbs) {
					System.err.println("Expected prob before trade is " + oldValues + ", but was "+ condProbBeforeTrade);
				}
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
								new CorrectiveTradeAction(parentTrade, questionId, condProbBeforeTrade, oldValues), // intantiate a network action representing the corrective trade
								isToUpdateMarginals
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
							if (isToTraceHistory()) {
								marginalsBefore = getProbLists(null, null, null);
							}
							
							if (memento == null && getMaxConditionalProbHistorySize() > 0 ) {
								// store cliques for conditional probabilities
								memento = algorithm.getMemento();
							}
						}
					}
					// Note: this memento is also used in order to store clique tables, so that we can retrieve history of any conditional probabilities

					// store marginal before trade, if the parent trade was not executed previously
					if (isToTraceHistory() && parentTrade != null && parentTrade.getWhenExecutedFirstTime() == null) {
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
					algorithm.propagate(isToUpdateMarginals);
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
		

		// connect the traded nodes if necessary
		if (!isPreview) {
			try {
				if (isToAddArcsOnAddTrade()) {
					// actually attempts to add arc
					this.simpleAddEdge((List)assumptionNodes, child, net, false);
				} else if (isToAddVirtualArcsOnAddTrade()) {
					this.simpleAddEdge((List)assumptionNodes, child, net, true);
				}
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not automatically add arc from " + assumptionNodes + " to "+child,e);
			}
		}
	
		
		return condProbBeforeTrade;
	}

	/**
	 * Check if this trade is trying to change some impossible (0%) state.
	 * @param newProb: trade (probability to set)
	 * @param oldProb: current probability
	 * @return if this is true, then oldProb has a 0% state, but the same state in newProb is above 0%.
	 * In such case, {@link #moveUpZeroProbability()} shall be used to move 0% out from 0%.
	 * If any of the arguments is null, then will return false too.
	 * If the sizes of the arguments are different, it will only compare the states in smaller list.
	 */
	protected boolean isMovingZeroProbability(List<Float> newProb, List<Float> oldProb) {
		if (newProb == null || oldProb == null) {
			// if there is no state to be compared, return false too.
			return false;
		}
//		if (newProb.size() != oldProb.size()) {
//			throw new IllegalArgumentException("The number of states in the probabilities being compared is different. New probability is " + newProb + ", and old is " + oldProb);
//		}
		int size = Math.min(oldProb.size(), newProb.size());
		for (int i = 0; i < size; i++) {
			if (oldProb.get(i).equals(0f) && newProb.get(i) > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add arcs from a set of nodes to another node if
	 * they can be connected without generating cycles.
	 * @param nodesFrom : nodes to add arc from
	 * @param nodeTo : node to add arc to
	 * @param net : network where arc will be included
	 * @param isVirtual : if true, the edges created in this method will be actually considered as
	 * virtual and will be included in {@link #getVirtualParentMapping()} instead of net.
	 * @throws InvalidParentException : inherent from {@link ProbabilisticNetwork#addEdge(Edge)}
	 * @see #getMSeparationUtility()
	 * @return the arcs created by this method
	 */
	protected List<Edge> simpleAddEdge(List<Node> nodesFrom, Node nodeTo, ProbabilisticNetwork net, boolean isVirtual) throws InvalidParentException {
		if (nodeTo == null || nodesFrom == null || net == null) {
			return Collections.EMPTY_LIST;
		}
		// the list to be returned by this method
		List<Edge> ret = new ArrayList<Edge>(nodesFrom.size());
		// fill ret
		for (Node assumption : nodesFrom) {
			
			// basic assertion
			if (assumption == null || nodeTo.equals(assumption)) {
				Debug.println(getClass(), 
						"Some node in in " + nodesFrom + " was null, or they were the same node as the target.");
				continue;	// ignore and try the best
			}
			
			// check if there is an arc between these 2 nodes
			if (nodeTo.getParentNodes().contains(assumption) || assumption.getParentNodes().contains(nodeTo)){
				// ignore this combination of nodes, because they are already connected
				continue;
			}
			
			// we shall create an arc node1->node2 or node2->node1, but without creating cycles. 
			// Supposedly, this will connect parents of children (which are dependencies but not connected directly)
			
			Edge createdEdge = null;	// prepare the variable to hold the new edge being created
			
			// use the m-separation utility component in order to find a path between node1 and node2
			if (!isVirtual 	// getMSeparationUtility().getRoutes shall be called only when we are actually including the edge. 
					&& getMSeparationUtility().getRoutes(nodeTo,assumption, null, null, 1).isEmpty()) {
				// there is no route from child to assumption, so we can create assumption->child without generating cycle
				createdEdge = new Edge( assumption ,  nodeTo);
			} else { // supposedly, we can always add edges in one of the directions (i.e. there is no way we add arc in each direction and both result in cycle)
				// if this is virtual edge, then the direction doesn't matter here, because direction will be tested again and occasionally inverted when other actual arcs are included
				// there is a route from child to assumption, so we cannot create assumption->child (it will create a cycle if we do so), so create child->assumption
				createdEdge = new Edge(nodeTo, assumption);
			}
			
			if (!isVirtual) {
//				// use heavy weight add arc operation if 
//				if (isToAddArcsOnAddTradeAndUpdateJT() && getDefaultInferenceAlgorithm().getNetwork().equals(net)) {
//					// heavy weight add arc operation.
//					addQuestionAssumption(null , new Date(), Long.parseLong(createdEdge.getDestinationNode().getName()), Collections.singletonList(Long.parseLong(createdEdge.getOriginNode().getName())), null);
//				} else {
					// add the new arc/edge to the network 
					net.addEdge(createdEdge);
//				}
			} else if (this.getVirtualArcs() != null) {
				// TODO do not add duplicate arcs
				if (!this.getVirtualArcs().contains(createdEdge)) {
					this.getVirtualArcs().add(createdEdge);
				}
//				// add arcs to the list of edges to be tagged as virtual (which will be used only when net structure changes - and cpts are used to store BN temporary)
//				List<INode> virtualParents = this.getVirtualArcs().get(createdEdge.getDestinationNode());
//				if (virtualParents == null) {
//					// make sure the mapping is initialized
//					virtualParents = new ArrayList<INode>();
//					this.getVirtualArcs().put(createdEdge.getDestinationNode(), virtualParents);
//				}
//				// add parent to the mapping
//				virtualParents.add(createdEdge.getOriginNode());
				// the edge shall not be actually created, in this case
			}
			// and add it to the list to be returned.
			ret.add(createdEdge);
		}
		return ret;
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
		
		// check if assumed state of resolved assumption is valid
		if (originalAssumptionIds != null && originalAssumedStates != null) {
			Map<Long, StatePair> resolvedQuestions = this.getResolvedQuestions();
			synchronized (resolvedQuestions) {
				for (int i = 0; i < originalAssumptionIds.size(); i++) {
					Long assumptionId = originalAssumptionIds.get(i);
					StatePair statePair = resolvedQuestions.get(assumptionId);
					if (statePair != null) {
						// this is a resolved assumption. Check if the specified state matches resolution
						if (statePair.getResolvedState().intValue() != originalAssumedStates.get(i).intValue()) {
							// there is no way to balance a resolved question assuming to a state different to the settled.
							throw new IllegalArgumentException(assumptionId + " is a question resolved to state " + statePair.getResolvedState() 
									+ ", but the argument assumes state " + originalAssumedStates.get(i));
						}
					}
				}
			}
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
		// do not consider resolved nodes
		for (int i = 0; i < fullParentNodes.size(); i++) {
			INode node = fullParentNodes.get(i);
			if (getResolvedQuestions().containsKey(Long.parseLong(node.getName()))) {
				fullParentNodes.remove(i);
				i--;
			}
		}
		
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
				if (balancingTrade != null && !balancingTrade.isEmpty() && !balancingTrade.contains(Float.NaN)) {
					ret.add(new CliqueSensitiveTradeSpecificationImpl(Long.parseLong(userAssetAlgorithm.getAssetNetwork().getName()), questionId, balancingTrade, fullAssumptionIds, fullAssumedStates, clique));
				}
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
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return Collections.emptyList();
		}
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
							true, algorithm, true, false, null, 
							false	// do not need to update marginals at this point
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#previewBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	public List<Float> previewBalancingTrade(long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			return Collections.emptyList();
		}
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
		
		
		if (this.getResolvedQuestions().containsKey(questionId)) {
			// there is no way to balance a resolved question
//			return Collections.EMPTY_LIST;
			throw new IllegalArgumentException("Question " + questionId + " is resolved already and cannot be balanced anymore.");
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
		
		// check if assumed state of resolved assumption is valid
		if (assumptionIds != null && assumedStates != null) {
			for (int i = 0; i < assumptionIds.size(); i++) {
				Long assumptionId = assumptionIds.get(i);
				StatePair statePair = this.getResolvedQuestions().get(assumptionId);
				if (statePair != null) {
					// this is a resolved assumption. Check if the specified state matches resolution
					if (statePair.getResolvedState().intValue() == assumedStates.get(i).intValue()) {
						// states matches, so we can simply ignore the assumption (remove from list of assumptions)
						assumptionIds.remove(i);
						assumedStates.remove(i);
						i--;
					} else {
						// there is no way to balance a resolved question assuming to a state different to the settled.
						throw new IllegalArgumentException(assumptionId + " is a question resolved to state " + statePair.getResolvedState() 
								+ ", but the argument assumes state " + assumedStates.get(i));
					}
				}
			}
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
			return null;
//			throw new IllegalArgumentException("Attempted to balance question " + questionId + " given assumptions " + assumptionIds
//					+ ". At least one of the questions was already resolved.");
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
			if (!isToRecalculateBalancingTradeOnRebuild && this.getWhenExecutedFirstTimeMillis() > 0) {
				// re-execute the same trades if getExecutedTrades() will contain something
				if (getExecutedTrades() != null && !getExecutedTrades().isEmpty()) {
					// backup original trade specification, because we will change it several times
					TradeSpecification backup = this.getTradeSpecification();
					try {
						// re-execute old trades
						for (TradeSpecification tradeSpecification : getExecutedTrades() ) {
							// execute the trade without releasing lock
							this.setTradeSpecification(tradeSpecification);
							super.execute(isToUpdateAssets,!isToThrowExceptionOnInvalidAssumptions(), false);	// do not update marginals at this point
						}
					} catch (Exception e) {
						// restore original trade specification
						this.setTradeSpecification(backup);
						throw new RuntimeException(e);
					}
					this.setTradeSpecification(backup);
				}
				// update all marginals if everything went OK
				synchronized (getDefaultInferenceAlgorithm()) {
					for (Node node : getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNodesCopy()) {
						((TreeVariable)node).updateMarginal();
					}
				}
				return;
			}
			// store the old marginals now and call setOldValues(oldValues) later, because a call to super will overwrite this value
			List<Float> oldMarginal = null;
			if (this.getWhenExecutedFirstTime() == null) {
				oldMarginal = getProbList(getQuestionId(), null, null);
			}
			
			// prepare the list of trades actually executed by this balance operation
			setExecutedTrades(new ArrayList<TradeSpecification>(1));
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
						balancingTrades = previewBalancingTrades(algorithm, getQuestionId(), null, null, clique, isToAllowNegativeInBalanceTrade);
					} else {
						balancingTrades = previewBalancingTrades(algorithm,
								// preview shall use original (backup) configurations for questions/assumptions, instead of this.getAssumptionIds() and this.getAssumedStates(),
								// because setTradeSpecification(tradeSpecification) is called in next block, so this.getAssumptionIds() and this.getAssumedStates() is referencing the clique in previuous loop
								backup.getQuestionId(), backup.getAssumptionIds(), backup.getAssumedStates(), 
								clique, isToAllowNegativeInBalanceTrade);
					}
					try {
						for (TradeSpecification tradeSpecification : balancingTrades ) {
							// execute the trade without releasing lock
							this.setTradeSpecification(tradeSpecification);
							super.execute(isToUpdateAssets,!isToThrowExceptionOnInvalidAssumptions(), false); //no need to update marginal at this point
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
				
				// update all marginals if everything went OK
				synchronized (getDefaultInferenceAlgorithm()) {
					for (Node node : getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNodesCopy()) {
						((TreeVariable)node).updateMarginal();
					}
				}
				
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#doBalanceTrade(java.lang.Long, java.util.Date, java.lang.String, long, long, java.util.List, java.util.List)
	 */
	public boolean doBalanceTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException, InvalidAssumptionException, InexistingQuestionException {
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			throw new UnsupportedOperationException("Balancing trades can only be performed when assets are managed by this engine. Please, turn the flag \"isToAddArcsOnlyToProbabilisticNetwork\" on to enable assets.");
		}
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
			Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
			
			
			if (mapTransactionQuestions == null || transactionKey == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				throw e;
			}
			
			// use the mapping in order to check whether the parent will be created in the same transaction
			synchronized (mapTransactionQuestions) {
				Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
				if (questionsInSameTransaction == null) {
					throw e;
				}
				// search for the assumption ID in questionsToBeCreatedInTransaction
				for (AddQuestionNetworkAction idInMapping : questionsInSameTransaction) {
					if (idInMapping.getQuestionId().equals(e.getQuestionId())) {
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
			Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
			
			
			if (mapTransactionQuestions == null) {
				// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
				throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
			}
			
			// use the mapping in order to check whether the parent will be created in the same transaction
			synchronized (mapTransactionQuestions) {
				Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
				if (questionsInSameTransaction == null) {
					throw new InexistingQuestionException("Question ID " + questionId + " does not exist and is not specified in transaction : " + transactionKey, questionId);
				}
				// search for the assumption ID in questionsToBeCreatedInTransaction
				for (AddQuestionNetworkAction actionInMapping : questionsInSameTransaction) {
					if (actionInMapping.getQuestionId().longValue() == questionId) {
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
					Map<Long, Set<AddQuestionNetworkAction>> mapTransactionQuestions = this.getQuestionsToBeCreatedInTransaction();
					
					
					if (mapTransactionQuestions == null) {
						// desynchronized call detected. This is unlikely to happen, but subclasses may cause this
						throw new NullPointerException("Desync detected in transaction " + transactionKey + ": the mapping which traces questions created in this transaction is null.");
					}
					
					// use the mapping in order to check whether the parent will be created in the same transaction
					synchronized (mapTransactionQuestions) {
						Set<AddQuestionNetworkAction> questionsInSameTransaction = mapTransactionQuestions.get(transactionKey);
						if (questionsInSameTransaction == null) {
							throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist and is not specified in transaction : " + transactionKey, assumptiveQuestionId);
						}
						// search for the assumption ID in questionsToBeCreatedInTransaction
						for (AddQuestionNetworkAction actInMapping : questionsInSameTransaction) {
							if (actInMapping.getQuestionId().equals(assumptiveQuestionId)) {
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
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
				if (getNetworkActionsIndexedByQuestions() != null) {
					synchronized (this.getNetworkActionsIndexedByQuestions()) {
						ret.addAll(this.getNetworkActionsIndexedByQuestions().get(null));
					}
				}
			}
		} else if (assumptionIds == null || assumptionIds.isEmpty()) {
			// History of marginals. Retrieve from the network actions indexed by question ID
			List<NetworkAction> list = null;
			if (this.getNetworkActionsIndexedByQuestions() != null) {
				synchronized (this.getNetworkActionsIndexedByQuestions()) {
					list = this.getNetworkActionsIndexedByQuestions().get(questionId);
				}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getScoreSummaryObject(long, java.lang.Long, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getScoreSummaryObject(long, java.lang.Long, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getScoreSummary(long, java.lang.Long, java.util.List, java.util.List)
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getScoreDetails(long, java.lang.Long, java.util.List, java.util.List)
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
		
		// if engine is configured to update only the probabilistic network, then we don't need to bother with user's asset algorithms
		if (isToAddArcsOnlyToProbabilisticNetwork()) {
			// simply return the default (house account) algorithm, because it won't use assets by default
			return getDefaultInferenceAlgorithm();
		}
		
		// value to be returned
		AssetAwareInferenceAlgorithm algorithm = null;
		
		synchronized (getUserToAssetAwareAlgorithmMap()) {
			// only create new instance if RebuildNetworkAction, ResolveQuestionNetworkAction, etc. are not blocking.
			algorithm = getUserToAssetAwareAlgorithmMap().get(userID);
			if (algorithm == null) {
				// first time user is referenced. Prepare inference algorithm for the user
//				IncrementalJunctionTreeAlgorithm junctionTreeAlgorithm = new IncrementalJunctionTreeAlgorithm(getProbabilisticNetwork());
//				// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
//				junctionTreeAlgorithm.setLikelihoodExtractor(AssetAwareInferenceAlgorithm.DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR);
				// the above code was substituted by the following, because we want to reduce memory usage a little bit by also reusing instance of IncrementalJunctionTreeAlgorithm (previously, it was only reusing the BN)
				IncrementalJunctionTreeAlgorithm junctionTreeAlgorithm = (IncrementalJunctionTreeAlgorithm) getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator();	// currently, no need to synchronize, because this wont be changing at all
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
					Map<INode, List<Float>> evidences = new HashMap<INode, List<Float>>();
					synchronized (getResolvedQuestions()) {
						for (Long resolvedQuestionId : getResolvedQuestions().keySet()) {
							evidences.put(algorithm.getAssetNetwork().getNode(resolvedQuestionId.toString()), getResolvedQuestions().get(resolvedQuestionId).getSettlement());
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
		if (getNetworkActionsIndexedByQuestions() == null) {
			return -1;
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
		if (getNetworkActionsIndexedByQuestions() != null) {
			synchronized (getNetworkActionsIndexedByQuestions()) {
				if (questionId == null) {
					questionId = networkAction.getQuestionId();
				}
				list = getNetworkActionsIndexedByQuestions().get(questionId);
			}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getJointProbability(java.util.List, java.util.List)
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
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getJointProbability(java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	public float getJointProbability(List<Long>questionIds, List<Integer> questionStates, List<List<Integer>> targetPaths, List<List<Integer>> referencePaths) throws IllegalArgumentException{

		if (questionIds == null || questionIds.isEmpty()) {
			// no question was provided
			return 0f;
		}
		
		// if no path is provided, then handle it the same way as the old routine
		if (targetPaths == null || targetPaths.isEmpty()) {
			return this.getJointProbability(questionIds, questionStates);
		}
		
		if (questionIds.size() < targetPaths.size()) {
			throw new IllegalArgumentException("Some question IDs were not specified for value tress of paths " + targetPaths);
		}
		
		// another simpler case: there is no other nodes from external network, and the target is pointing to only 1 value tree node
		// TODO integrate with a more generic case
		if (questionIds.size() == 1 && targetPaths.size() == 1) {
			
			// then, simply return marginal from value tree
			synchronized (getDefaultInferenceAlgorithm()) {
				synchronized (getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork()) {
					
					// make sure reference path is the prefix of the target path
					List<Integer> targetPath = targetPaths.get(0);
					if (targetPath == null) {
						// then simply solve as there is no value tree at all
						return getJointProbability(questionIds, questionStates);
					}
					
					// if referencePaths is null/empty, use empty list anyway. Otherwise, use the 1st list (supposedly the only, because there is only 1 target anyway)
					List<Integer> referencePath = ((referencePaths == null || referencePaths.isEmpty())?Collections.EMPTY_LIST:referencePaths.get(0));
					
					// pass along both paths. If we find divergence, then there is something wrong
					int commonSizeTargetReference = Math.min(targetPath.size(), referencePath.size());
					for (int i = 0; i < commonSizeTargetReference; i++) {
						if (referencePath.get(i) != targetPath.get(i)) {
							throw new IllegalArgumentException("Reference path " + referencePath + " and target path " + targetPath
									+ " are diverging in step " + i + ". Reference/anchors are supposed to be null or ancestor of target.");
						}
					}
					
					// at this point, we are sure that the reference is ancestor or equal to target
					if (referencePath != null && referencePath.size() >= targetPath.size()) {
						// in this case, reference is descendant or equal to target path. Prob assuming descendant is supposedly 100%
						return 1f;
					}
					
					// find the root of the value tree
					Node node = getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(""+questionIds.get(0));
					if (node == null || !(node instanceof ValueTreeProbabilisticNode)) {
						throw new IllegalArgumentException(questionIds.get(0) + " was not found or not a root of a value tree.");
					}
					ValueTreeProbabilisticNode root = (ValueTreeProbabilisticNode) node;
					
					// find the target node
					IValueTreeNode target = root.getValueTree().getNodeInPath(targetPath);
					
					// find the reference/anchor node
					IValueTreeNode anchor = root.getValueTree().getNodeInPath(referencePath);	// this will remain null if referencePath is null or empty
					
					// simply return the probability of target given anchor
					return root.getValueTree().getProb(target , anchor);
				}
			}
		}
		
		/* This case is more complex, because uses external Bayes net.
		 * Given that x,y are non-shadow nodes in different value trees, and a is a set of nodes in external net 
		 * (which can be states of ordinal nodes or shadow nodes of different value trees), 
		 * and also given that sx and sy are the shadow nodes close to x and y respectively then:
		 * 
		 * P(x|(...)) = P(sx|(...))P(x|sx)
		 * P(y|(...)) = P(sy|(...))P(y|sy)
		 * 
		 * Therefore:
		 * 
		 * p(x,y,a) = p(x|y,a)p(y|a)p(a)  
		 * = p(x|y,a)p(sy|a)p(y|sy)p(a) 
		 * = p(sx|y,a)p(x|sx)p(sy|a)p(y|sy)p(a)
		 * = p(sx,y,a)p(x|sx)p(sy|a)p(y|sy)p(a) / p(y,a)
		 * = p(y|sx,a)p(sx,a)p(x|sx)p(sy|a)p(y|sy)p(a) / p(y|a)p(a)
		 * = p(y|sx,a)p(sx,a)p(x|sx)p(sy|a)p(y|sy)p(a) / p(sy|a)p(y|sy)p(a)
		 * = p(y|sx,a)p(sx,a)p(x|sx)
		 * = p(sy|sx,a)p(y|sy)p(sx,a)p(x|sx)
		 * = p(sy|sx,a)p(sx,a)p(y|sy)p(x|sx)
		 * 
		 * 		= p(sy,sx,a)p(y|sy)p(x|sx)
		 * 
		 * Therefore, we can calculate the joint probability P(sy,sx,a), the conditionals
		 * P(y|sy) and P(x|sx), and then multiply them in order to get the joint.
		 * 
		 * TODO check if we can divide by P(sy|y) instead of multiplying by P(y|sy)=1 if y is ancestor of sy
		 */
		
		// make the size of states and question IDs equal
		if (questionStates != null) {
			// do not change original list, because we don't want collateral effect on arguments of caller
			List<Integer> states = new ArrayList<Integer>(questionIds.size());	
			for (int i = 0; i < questionIds.size(); i++) {
				if (i < questionStates.size()) {
					states.add(questionStates.get(i));
				} else {
					states.add(null);
				}
			}
			questionStates = states;
		} else {
			// simply instantiate a new list with same size of the list of questions.
			int size = questionIds.size();
			questionStates = new ArrayList<Integer>(size);
			for (int i = 0; i < size; i++) {
				// can fill with anything
				questionStates.add(null);
			}
		}
		
		
		// now, we need to calculate p(sy,sx,a), which is the joint prob only of shadow nodes or states in external Bayes net
		
		// fill questionStates (the new one) with correct states (of shadows). If question is a value tree, make sure to fill it with index of shadow node closest to the path
		// the following map will be filled with a mapping from target node to closest shadow node, so that we can calculate P(x|sx) posteriory.
		Map<IValueTreeNode, IValueTreeNode> targetNodeToClosestShadowNodeMap = new HashMap<IValueTreeNode, IValueTreeNode>();
		// the following map traces what target nodes were generated from the same root. Can be used to detect following cases:
		// If X is ancestor of Y, then P(X,Y) = P(X|Y)P(Y) = P(Y). Therefore, in this case the ancestor can be ignored.
		// If neither ones are ancestors of the other, then P(X,Y) = P(X|Y)P(Y) = 0*P(Y) = 0, therefore in this case we can immediately return zero
		Map<INode, IValueTreeNode> rootToRelevantTargetMapping = new HashMap<INode, IValueTreeNode>();	
        boolean hasDescendantShadow = false;		// this will become true if shadow node was a descendant of target node
		for (int indexOfQuestion = 0; (indexOfQuestion < questionIds.size()) && (indexOfQuestion < targetPaths.size()); indexOfQuestion++) {
			synchronized (getProbabilisticNetwork()) {
				// extract the node
				Node node = getProbabilisticNetwork().getNode("" + questionIds.get(indexOfQuestion));
				if ((node instanceof ValueTreeProbabilisticNode) 
						 && (targetPaths.get(indexOfQuestion) != null)
						 && !targetPaths.get(indexOfQuestion).isEmpty()) {
					// the root of the value tree
					ValueTreeProbabilisticNode root = (ValueTreeProbabilisticNode) node;
					// find the target node
					IValueTreeNode target = root.getValueTree().getNodeInPath(targetPaths.get(indexOfQuestion));
					if (target == null) {
						throw new IllegalArgumentException(targetPaths.get(indexOfQuestion) +" is an invalid value tree path for node " + root);
					}
					
					// this is a value tree question, and there is a target path defined, so find the shadow node closest to the target
					int indexOfShadowClosestToTarget = -1;	// this will hold the index of shadow node which is either ancestor or descendant of target node
					IValueTreeNode shadow = null;			// this will hold the shadow node itself
					for (int i = 0; i < root.getValueTree().getShadowNodeSize(); i++) {
						// because value tree is a tree and shadow nodes are mutually exclusive, the first one to find is the one we want
						shadow = root.getValueTree().getShadowNode(i);
						if (shadow.isAncestorOf(target)) {
							// shadow node is ancestor of target
							indexOfShadowClosestToTarget = i;
							break;
						} else if (target.isAncestorOf(shadow)) {
							// shadow node is descendant of target
							hasDescendantShadow = true;
							indexOfShadowClosestToTarget = i;
							break;
						} else if (target.equals(shadow)) {
							// the node is the shadow node itself
							indexOfShadowClosestToTarget = i;
							break;
						}
					}
					// note: at this point, the list of question ids and list of question states have the same size, because we made such adjustments previously
					if (indexOfShadowClosestToTarget >= 0) {
						// set the question state to the shadow node
						questionStates.set(indexOfQuestion, indexOfShadowClosestToTarget);
					} else {
						throw new IllegalArgumentException("The path " + targetPaths.get(indexOfQuestion)
								+ " was specified to question "+ root + ", but no shadow node could be identified near the path.");
					}
					
					
					// check if there is no node with same root
					IValueTreeNode targetSharingRoot = rootToRelevantTargetMapping.get(root);
					if (targetSharingRoot == null || targetSharingRoot.isAncestorOf(target)) {
						// put new entry. If old one is ancestor, then this will also delete the ancestor and put the descendant
						targetNodeToClosestShadowNodeMap.remove(targetSharingRoot);
						rootToRelevantTargetMapping.put(root,target);	// replace the old
					} else {
						// there is a node which shares the same root, and the new one may be an ancestor or not
						if (target.isAncestorOf(targetSharingRoot)) {
							// always use the descendants, because P(X,Y) when X is descendant of Y is P(X)
							// delete the old target, if there were.
							targetNodeToClosestShadowNodeMap.remove(target);
							target = targetSharingRoot;
							// no need to update the other mapping, because targetSharingRoot is already there
						} else {
							// if they are in independent branches, then joint is always zero, because P(X,Y,(...)) when X and Y can never happen together is 0%
							return 0f;
						}
					}
					
					// repetitions will be automatically replaced (because P(X,X) = P(X), it's OK to replace old entry and not to double count)
					targetNodeToClosestShadowNodeMap.put(target, shadow);
				}
			}
		}
		
		// TODO merge the special case (only 1 node provided) to the following cases
		
//		if (hasDescendantShadow) {
//			// Don't know how to efficiently calculate. 
//			// May use P(x,y,z,a) = P(x|y,z,a)*P(y,z,a) = P(x|y,z,a)*P(y|z,a)P(z,a) = P(x|y,z,a)*P(y|z,a)P(z|a)P(a)
//			throw new UnsupportedOperationException("The current version of ME cannot calculate joint probability when at least one hidden node in a value tree is an ancestor of shadow node.");
//			// TODO can we use the following prop? p(x,sx) = p(x|sx)p(sx); if x->sx, then p(x|sx)p(sx) = 1*p(sx); if sx->x, then p(x|sx)p(sx) = p(x)p(sx)/p(sx) = p(x)
//		}
		
		
		// get the shadows' and external network's joint prob P(sx,sy,a). Will throw exception if questionStates is not fully specified
		float jointProb = this.getJointProbability(questionIds, questionStates);
		
		// for each node that has value tree and target path is pointing to some non-shadow node, get P(x|sx) and multiply with joint
		for (Entry<IValueTreeNode, IValueTreeNode> entry : targetNodeToClosestShadowNodeMap.entrySet()) {
			IValueTreeNode target = entry.getKey();
			IValueTreeNode shadow = entry.getValue();
			// multiply the probability P(x|sx)
			if (shadow.isAncestorOf(target)) {
				jointProb *= target.getValueTree().getProb(target, shadow);
			} else if (target.isAncestorOf(shadow)) {
				// TODO check if it is OK to divide by P(sx|x) when x is ancestor of sx
				jointProb /= target.getValueTree().getProb(shadow, target);
			} else {
				// Note: the situation if (target.isAncestorOf(shadow)) was already solved, so no need to handle here
				// shadow and target are the same, so don't need to multiply, because P(X|X) = 1 and 1 is null value in multiplications.
				// supposedly, P(x|sx) is also 1 if x is ancestor of sx.
			} 
		}
		
		// If x is antecessor of sx: 
		// P(x) = .5, P(sx|x) = .8 -> p(x|sx)p(sx)/p(x) = .8 -> p(x|sx) = .8*p(x)/p(sx) = .8*.5/(.5*.8) = 1
		// P(x,a) = p(x|a)p(a) ?=? p(sx|a)p(sx|x)p(a) = p(sx,a)p(x|sx) = p(sx,a)p(sx|x)p(x)/p(sx) = p(sx,a)p(sx)p(x)/p(sx)p(x) = p(sx,a)
		// p(x|a) = 1 - ( 1 - p(x) ) ( (1-p(sx|a)/p(sx)) / (1-) ) 
		
		return jointProb;
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getQuestionAssumptionGroups()
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
//		private final Integer statesSize;
		private final List<Float> settlement;
		public StatePair(List<Float> settlement) {
			this.settlement = settlement;
		}
		public Integer getStatesSize() { return getSettlement().size(); /*statesSize;*/ }
		public String toString() {return ""+getSettlement() ;}
		/** This will return consisten value only if resolution is a hard evidence. Will return negative if negative evidence, and null if soft evidence. */
		public Integer getResolvedState() { 
			synchronized (getDefaultInferenceAlgorithm()) {
				return getDefaultInferenceAlgorithm().getResolvedState(settlement);
			}
		}
		public List<Float> getSettlement() { return settlement; }
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#exportNetwork(java.io.File)
	 * @deprecated use {@link MarkovEngineImpl#exportState()}
	 */
	@Deprecated
	public synchronized void exportNetwork(File file) throws IOException, IllegalStateException {
		if (isToExportOnlyCurrentSharedProbabilisticNet()) {
			// simply call exportCurrentSharedNetwork
			String stringRepresentationOfNet = this.exportState();
			// store the returned string to a file
			PrintStream stream = new PrintStream(file);
			// TODO use an I/O class instead of using the file here
			stream.print(stringRepresentationOfNet);
			stream.flush();
			stream.close();
			return;
		}
		
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
		if (io instanceof ValueTreeNetIO) {
			// make sure to save the virtual arcs
			((ValueTreeNetIO) io).setVirtualParents(this.convertArcsToParentMapping(getVirtualArcs()));
		}
		io.save(file, netToSave);
	}
	

	/**
	 * As the name suggests, it creates a new map which represents the arcs.
	 * @param arcs : list of {@link Edge} to be converted to a map
	 * @return : a mapping from child to its parents
	 * @see #convertParentMappingToArcs(Map)
	 * @see ValueTreeNetIO#getVirtualParents()
	 * @see #getVirtualParentMapping()
	 */
	protected Map<INode, List<INode>> convertArcsToParentMapping( List<Edge> arcs) {
		Map<INode, List<INode>> ret = new HashMap<INode, List<INode>>();
		if (arcs != null) {
			for (Edge edge : arcs) {
				List<INode> parentList = ret.get(edge.getDestinationNode());
				if (parentList == null) {
					// make sure the value of the mapping (which is a list) is initialized
					parentList = new ArrayList<INode>();
					ret.put(edge.getDestinationNode(), parentList);
				}
				// include the parent in this edge in the list (value mapped)
				parentList.add(edge.getOriginNode());
			}
		}
		return ret;
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
			if (!isToTraceHistory()) {
				throw new IllegalStateException("This functionality cannot be used when isToTraceHistory == false");
			}
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
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#importNetwork(java.io.File)
	 * @deprecated use {@link MarkovEngineImpl#importState(String)}
	 */
	@Deprecated
	public synchronized void importNetwork(File file) throws IOException, IllegalStateException {
		
		if (isToExportOnlyCurrentSharedProbabilisticNet()) {
			// TODO use an I/O class instead of handling I/O here
			BufferedReader reader = new BufferedReader(new FileReader(file));
			// read file and store to a string
			String netString = "";	// string to be passed to importCurrentSharedNetwork
			String line;	// line of file currently being read
			// read file line-by-line
			while ((line = reader.readLine()) != null) {
				netString += line;
			} ;
			reader.close();
			// simply delegate to importCurrentSharedNetwork
			this.importState(netString);
			return;
		}
		
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
	 * @see #getComplexityFactor(Map)
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
		
		// extract statistics regarding whether it is running approximation or not
		ret.setRunningApproximation(isRunningApproximation());
		
		// TODO extract statistics of parents of nodes
		
		
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
	 * @deprecated this is not used anymore
	 */
	@Deprecated
	public boolean isToThrowExceptionInTradesToResolvedQuestions() {
		return isToThrowExceptionInTradesToResolvedQuestions;
	}

	/**
	 * If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction)} will
	 * throw exception when the question has probability 0 or 1 in any state
	 * @param isToThrowExceptionInTradesToResolvedQuestions the isToThrowExceptionInTradesToResolvedQuestions to set
	 * @deprecated this is not used anymore
	 */
	@Deprecated
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
	protected Map<Long, Set<AddQuestionNetworkAction>> getQuestionsToBeCreatedInTransaction() {
		return questionsToBeCreatedInTransaction;
	}

	/**
	 * @param questionsToBeCreatedInTransaction the questionsToBeCreatedInTransaction to set
	 */
	protected void setQuestionsToBeCreatedInTransaction(
			Map<Long, Set<AddQuestionNetworkAction>> questionsToBeCreatedInTransaction) {
		this.questionsToBeCreatedInTransaction = questionsToBeCreatedInTransaction;
	}

	/**
	 * When true, {@link #addQuestionAssumption(Long, Date, long, List, List)} won't try to re-run the trades.
	 * however, when {@link #isToAddArcsOnlyToProbabilisticNetwork()} is false, this may result in a non-optimal BN.
	 * <br/>
	 * <br/>
	 * The following conditions are not supported entirely, so they may still require reboot:
	 * <br/>
	 * - non null cpd is provided in {@link #addQuestionAssumption(Long, Date, long, List, List)}: non null
	 * cpd means that arcs will be substituted, but that's not supported by the algorithm which adds arcs
	 * without rebooting. Therefore, this may reboot the engine.
	 * <br/>
	 * - arcs are added to resolved nodes: resolved nodes are usually removed from the shared Bayes net,
	 * so the algorithm for adding arcs without rebooting may not be able to properly handle such request,
	 * therefore, this may trigger a reboot.
	 * @return the isToAddArcsWithoutReboot
	 */
	public boolean isToAddArcsWithoutReboot() {
		return this.isToAddArcsWithoutReboot;
	}

	/**
	 * When true, {@link #addQuestionAssumption(Long, Date, long, List, List)} won't try to re-run the trades.
	 * however, when {@link #isToAddArcsOnlyToProbabilisticNetwork()} is false, this may result in a non-optimal BN.
	 * <br/>
	 * <br/>
	 * The following conditions are not supported entirely, so they may still require reboot:
	 * <br/>
	 * - non null cpd is provided in {@link #addQuestionAssumption(Long, Date, long, List, List)}: non null
	 * cpd means that arcs will be substituted, but that's not supported by the algorithm which adds arcs
	 * without rebooting. Therefore, this may reboot the engine.
	 * <br/>
	 * - arcs are added to resolved nodes: resolved nodes are usually removed from the shared Bayes net,
	 * so the algorithm for adding arcs without rebooting may not be able to properly handle such request,
	 * therefore, this may trigger a reboot.
	 * @param isToAddArcsWithoutReboot the isToAddArcsWithoutReboot to set
	 */
	public void setToAddArcsWithoutReboot(boolean isToAddArcsWithoutReboot) {
		this.isToAddArcsWithoutReboot = isToAddArcsWithoutReboot;
		if (!isToAddArcsWithoutReboot) {
			// if we are setting not to add arcs without reboot, then the related flag toAddArcsOnlyToProbabilisticNetwork also needs to be turned off
			this.setToAddArcsOnlyToProbabilisticNetwork(false);
			this.setToTraceHistory(true);	// we need to use history in order to reboot
		}
	}

	/**
	 * If this is true and {@link #isToAddArcsWithoutReboot()} is also true, then an algorithm for
	 * adding arcs without re-running the history will be executed, and the resulting BN will be near optimal,
	 * because for probabilistic networks it is easier to do such optimization.
	 * @return the isToAddArcsOnlyToProbabilisticNetwork
	 */
	public boolean isToAddArcsOnlyToProbabilisticNetwork() {
		return this.isToAddArcsOnlyToProbabilisticNetwork;
	}

	/**
	 * If this is true and {@link #isToAddArcsWithoutReboot()} is also true, then an algorithm for
	 * adding arcs without re-running the history will be executed, and the resulting BN will be near optimal,
	 * because for probabilistic networks it is easier to do such optimization.
	 * @param isToAddArcsOnlyToProbabilisticNetwork the isToAddArcsOnlyToProbabilisticNetwork to set
	 */
	public void setToAddArcsOnlyToProbabilisticNetwork(
			boolean isToAddArcsOnlyToProbabilisticNetwork) {
		this.isToAddArcsOnlyToProbabilisticNetwork = isToAddArcsOnlyToProbabilisticNetwork;
		if (isToAddArcsOnlyToProbabilisticNetwork == true) {
			// setting this flag to true must also set isToAddArcsWithoutReboot to true
			this.setToAddArcsWithoutReboot(true);
			// reset all mappings and indexes related to user assets, because they won't be used anymore
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				getUserToAssetAwareAlgorithmMap().clear();
			}
			
			// map of users loaded lazily also needs to be reset, because users aren't important when we are only managing prob network
			synchronized (getUninitializedUserToAssetMap()) {
				getUninitializedUserToAssetMap().clear();
			}
			
			// mapping of cash gains per resolved questions are also unnecessary when users are not present
			synchronized (getUserIdToResolvedQuestionCashGainMap()) {
				getUserIdToResolvedQuestionCashGainMap().clear();
			}
			// same reason of the above mapping
			synchronized (getUserIdToResolvedQuestionCashBeforeMap()) {
				getUserIdToResolvedQuestionCashBeforeMap().clear();
			}
			
			// if we are only handling prob network, then the requirements indicates that we won't be using history too.
			this.setToTraceHistory(false);
			
			// do not update assets if isToAddArcsOnlyToProbabilisticNetwork == true
			this.getDefaultInferenceAlgorithm().setToUpdateAssets(false);
			
			// in this mode, we won't need to obtain probability of resolved questions either.
			this.setToObtainProbabilityOfResolvedQuestions(false);
			
			this.getDefaultInferenceAlgorithm().setToDeleteEmptyCliques(true);
		}  else {
			this.getDefaultInferenceAlgorithm().setToDeleteEmptyCliques(false);
		}
		
	}

	/**
	 * If true, probabilities will be printed after execution of actions like a csv file
	 * @return the isToPrintProbs
	 */
	protected boolean isToPrintProbs() {
		return isToPrintProbs;
	}

	/**
	 * If true, probabilities will be printed after execution of actions, like a csv
	 * @param isToPrintProbs the isToPrintProbs to set
	 */
	protected void setToPrintProbs(boolean isToPrintProbs) {
		this.isToPrintProbs = isToPrintProbs;
	}

	/**
	 * If this is true and {@link #isToPrintProbs()} == true,
	 * then clique potential of root clique will be printed as well.
	 * @return the isToPrintRootClique
	 */
	protected boolean isToPrintRootClique() {
		return isToPrintRootClique;
	}

	/**
	 * If this is true and {@link #isToPrintProbs()} == true,
	 * then clique potential of root clique will be printed as well.
	 * @param isToPrintRootClique the isToPrintRootClique to set
	 */
	protected void setToPrintRootClique(boolean isToPrintRoot) {
		this.isToPrintRootClique = isToPrintRoot;
	}
	
	/**
	 * This method uses {@link #getNetIOToExportSharedNetToString()} and will call 
	 * {@link NetIO#setPrintStreamBuilder(unbbayes.io.IPrintStreamBuilder)}
	 * with {@link StringPrintStreamBuilder} in order to use {@link NetIO} for printing net files to string instead of
	 * files. 
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#exportState()
	 */
	public synchronized String exportState() {	// TODO check if we can remove the synchronized
		// TODO use a specific I/O class instead of handling I/O here 
		
		// this I/O class will print a network representation in some format
		NetIO io = getNetIOToExportSharedNetToString();	// NetIO represents BN as human-readable text in Hugin NET specification.
		
		// the above I/O class will print the network to a stream built by this builder
		IPrintStreamBuilder streamBuilder = null;
		
		
		// set the streamBuilder accordingly to flags
		if (isToReturnIdentifiersInExportState()) {
			// this will save to file, and return an identifier
			
			if (isToCompressExportedState()) {
				// use a zipped file
				streamBuilder =  new IPrintStreamBuilder() {
					// TODO stop using attributes related to objects built by this builder
					private String id = null;
					
					/** @see unbbayes.io.IPrintStreamBuilder#getPrintStreamFromFile(java.io.File) */
					public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException {
						try {
							// make sure the file (and folder) exists. This won't do anything if the file already exists.
							file.getParentFile().mkdirs();
							file.createNewFile();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						
						id = file.getName().replace(getExportedStateFileExtension(), "");	// id is the file name with no folder name and no extension
						
						// this will zip the content and then write to a file
						ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
						
						// it seems that a ZipOutputStream requires at least 1 entry (content)
						try {
							zipOutputStream.putNextEntry(new ZipEntry(id+".net"));
						} catch (IOException e) {
							throw new RuntimeException("Was not able to create a new ZIP entry for file " + file.toString(), e);
						}
						
						return new PrintStream(zipOutputStream);
					}
					
					/** Return the identifier (i.e. file name) */
					public String toString() { return id; }
				};
			} else {
				// don't zip the file
				streamBuilder =  new IPrintStreamBuilder() {
					// TODO stop using attributes related to objects built by this builder
					private String id = null;
					
					/** @see unbbayes.io.IPrintStreamBuilder#getPrintStreamFromFile(java.io.File) */
					public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException {
						try {
							// make sure the file (and folder) exists. This won't do anything if the file already exists.
							file.getParentFile().mkdirs();
							file.createNewFile();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						id = file.getName().replace(getExportedStateFileExtension(), "");	// id is the file name with no folder name and no extension
						return new PrintStream(new FileOutputStream(file));
					}
					
					/** Return the identifier (i.e. file name) */
					public String toString() { return id; }
				};
			}
			
		} else if (isToCompressExportedState()) { 
			// return the exported network itself, but it needs to be compressed
			
			// this will zip the content and encode to base64
			streamBuilder =  new IPrintStreamBuilder() {
				// TODO stop using attributes related to objects built by this builder
				private ByteArrayOutputStream byteArrayOutputStream;
				
				/** @see unbbayes.io.IPrintStreamBuilder#getPrintStreamFromFile(java.io.File) */
				public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException {
					try {
						// ignore the file and write to a byte array instead
						byteArrayOutputStream = new ByteArrayOutputStream();
						// this will zip the content, encode to base64, and then write to byte array
						ZipOutputStream zipOutputStream = new ZipOutputStream(new Base64OutputStream(byteArrayOutputStream));
						// it seems that a ZipOutputStream requires at least 1 entry (content)
						zipOutputStream.putNextEntry(new ZipEntry(file.getName()+".net"));
						return new PrintStream(zipOutputStream);
					} catch (Exception e) {
						// TODO stop doing exception translation
						throw new RuntimeException(e);
					}
				}
				
				/** Return the content encoded to base64 */
				public String toString() { return byteArrayOutputStream.toString(); }
			};
			
		} else {	
			// return the network representation itself, but don't compress
			
//			// prepare an I/O class which actually prints to string instead of file
//			StringBuilder stringBuilder = new StringBuilder();	// this string will be updated
//			
//			// By using StringPrintStreamBuilder, net will be printed to string instead of file
//			io.setPrintStreamBuilder(new StringPrintStreamBuilder(stringBuilder));
			
			// the above code was substituted with the following
			
			// this will return the content created by the I/O class, without any transformation
			streamBuilder =  new IPrintStreamBuilder() {
				// TODO stop using attributes related to objects built by this builder
				private ByteArrayOutputStream byteArrayOutputStream;
				
				/** @see unbbayes.io.IPrintStreamBuilder#getPrintStreamFromFile(java.io.File) */
				public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException {
					// ignore the file and write to a byte array instead
					byteArrayOutputStream = new ByteArrayOutputStream();
					return new PrintStream(byteArrayOutputStream);
				}
				/** Return the content of the bytearray, converted to string  */
				public String toString() { return byteArrayOutputStream.toString(); }
			};
		}
		
		// tell I/O class to build streams by using this builder
		if (streamBuilder != null) {
			io.setPrintStreamBuilder(streamBuilder);
		} else {
			throw new RuntimeException("Could not find any builder for the output stream where the network snapshot will be written to. This is a bug. Please, contact administrator.");
		}
		
		
		// retrieve current BN and use the I/O class to generate a proper representation
		synchronized (getDefaultInferenceAlgorithm()) {
			// TODO create the virtual arcs
			if (isToAddVirtualArcsOnAddTrade()) {
				throw new UnsupportedOperationException("Virtual arcs are not implmented yet.");
			}
			
			// for the purpose of this method, we don't need to represent impossible states, so set them to a very low probability instead
//			moveUpZeroProbability();
			
			// make sure the cpts of all nodes are consistent with the current status of junction trees.
			getDefaultInferenceAlgorithm().updateCPTFromJT();
			
			if (getDefaultInferenceAlgorithm().getNet() != getProbabilisticNetwork()) {
				throw new RuntimeException("Multiple instances of shared Bayes net was found");
			}
			// save to string. The file is ignored if we use StringPrintStreamBuilder.
			try {
				// use current timestamp as default file name (milliseconds from midnight, January 1, 1970 UTC)
				io.save(new File(getStatesFolderName() + String.valueOf(System.currentTimeMillis())+getExportedStateFileExtension()), getDefaultInferenceAlgorithm().getNet());
			} catch (FileNotFoundException e) {
				throw new RuntimeException("StringPrintStreamBuilder was supposed to print to strings and ignore files, but a FileNotFoundException was thrown. This is probably a bug in a library being used by the Markov Engine. Please, check your version of UnBBayes.",e);
			}
			// remove the virtual arcs
			if (isToAddVirtualArcsOnAddTrade()) {
				throw new UnsupportedOperationException("Virtual arcs are not implmented yet.");
			}
		}
		
		// return the string representation.
		// This can a representation of the network (compressed or not), or a file identifier.
		return streamBuilder.toString();
	}

	/**
	 * This method uses {@link #getNetIOToExportSharedNetToString()} and will call 
	 * {@link NetIO#setReaderBuilder(unbbayes.io.IReaderBuilder)}
	 * with {@link StringReaderBuilder} in order to use 
	 * {@link NetIO} for printing net files to string instead of files. 
	 * <br/>
	 * Note: this will also set {@link #setToAddArcsOnlyToProbabilisticNetwork(boolean)} to true,
	 * which is expected to reset users.
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#importState(java.lang.String)
	 * @param netString: this is a string representation of the network. It is expected to be something returned from {@link #exportState()}.
	 * If {@link #isToReturnIdentifiersInExportState()} is true, then it is just an identifier of a local file to be loaded.
	 * If {@link #isToCompressExportedState()} is true, then the string is expected to be a compressed representation, instead of being some 
	 * human-readable format.
	 */
	public synchronized void importState(final String netString) throws IllegalArgumentException {

		// reset cache of complexity factors of each existing arc
		Map<String, Map<String, Double>> arcComplexityCache = getSingleExistingArcComplexityCache();
		if (arcComplexityCache == null) {
			setSingleExistingArcComplexityCache(new HashMap<String, Map<String,Double>>());
		} else {
			arcComplexityCache.clear();
		}
		
		
		// indicate that we are only using probabilistic net
		this.setToAddArcsOnlyToProbabilisticNetwork(true);
		// this shall also reset existing users

		// this I/O class will read a network representation
		NetIO io = getNetIOToExportSharedNetToString();
		
		// reset/initialize and specify the mapping of what arcs are tagged as virtual. This will be filled posteriory when we load the network from this IO
		if (io instanceof ValueTreeNetIO) {
			((ValueTreeNetIO) io).setVirtualParents(new HashMap<INode, List<INode>>());
		}
		
		String netIdentifier = "This_File_Will_Be_Ignored";

		// check the mode we are reading: using an identifier
		if (isToReturnIdentifiersInExportState()) {
			// this will load from file, given identifier. Make sure we specify correct file in correct folder.
			netIdentifier = getStatesFolderName() + netString + getExportedStateFileExtension();
			
			io.setReaderBuilder(new IReaderBuilder() {
				public Reader getReaderFromFile(File file) throws FileNotFoundException {
					// check if this is a zip file
					boolean isZip = false;
					try {
						isZip = isZipFile(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (isZip) {
						// This stream will read a file input stream, and then unzip it
						ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
						try {
							// move cursor to 1st zipped entry  
							zipInputStream.getNextEntry();
						} catch (IOException e) {
							throw new RuntimeException("Could not unzip the content of imported file " + file.getPath(),e);
						}	
						// Convert stream to reader, and wraps with buffer
						return new BufferedReader(new InputStreamReader(zipInputStream));
					} else {
						// just read the file normally
						return (new BufferedReader(new FileReader(file)));
					}
				}
			});
//		} else if (isToCompressExportedState()) {
//			io.setReaderBuilder(new IReaderBuilder() {
//				public Reader getReaderFromFile(File file) throws FileNotFoundException {
//					// reads a file input stream, decode from base64, unzip, convert to reader, and wraps with buffer
//					try {
//						// This stream will read netString, decode from base64 encoding, and then unzip it
//						ZipInputStream zipInputStream = new ZipInputStream(new Base64InputStream(new ByteArrayInputStream(netString.getBytes())));
//						zipInputStream.getNextEntry(); // move cursor to 1st zipped entry  
//						return new BufferedReader(new InputStreamReader(zipInputStream));	// Convert stream to reader, and wraps with buffer
//					} catch (Exception e) {
//						throw new RuntimeException(e);
//					}
//				}
//			});
		} else {
//			// By using StringReaderBuilder, net will be read uncompresed NET format from string, instead of file
//			io.setReaderBuilder(new StringReaderBuilder(netString));
			io.setReaderBuilder(new IReaderBuilder() {
				public Reader getReaderFromFile(File file) throws FileNotFoundException {
					// reads a file input stream, decode from base64, unzip, convert to reader, and wraps with buffer
					try {
//						if (netString.matches("\\s*net\\s*\\{.*\\}\\s*\\z")) {
						// regex is expensive when string is very large, so I'm only checking initial 20 chars and last 10 chars
						// TODO perform a more robust, yet inexpensive check
						if (netString.substring(0, Math.min(netString.length(), 20)).replaceAll("\\s+", "").startsWith("net{")
								&& netString.substring(Math.max(netString.length()-10, 0), netString.length()).replaceAll("\\s+", "").endsWith("}")) {
							// consider this as a Hugin Net file specification (not zipped and not encoded to base64)
							return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(netString.getBytes())));
						} else {
							// consider this as a zip and base64-encoded string
							// This stream will read netString, decode from base64 encoding, and then unzip it
							ZipInputStream zipInputStream = new ZipInputStream(new Base64InputStream(new ByteArrayInputStream(netString.getBytes())));
							zipInputStream.getNextEntry(); // move cursor to 1st zipped entry  
							return new BufferedReader(new InputStreamReader(zipInputStream));	// Convert stream to reader, and wraps with buffer
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
			
		
		
		
		// load the network
		ProbabilisticNetwork probNet = null;
		try {
			probNet = (ProbabilisticNetwork) io.load(new File(netIdentifier));
		} catch (LoadException e) {
			throw new RuntimeException("The format of the string does not seem to be valid. Please check its content.",e);
		} catch (IOException e) {
			throw new RuntimeException("The file should have been ignored by a StringReaderBuilder, but it was not. This is probabily a bug in a library used by Markov Engine. Please, check your version of UnBBayes.",e);
		}
		if (probNet == null) {
			throw new IllegalArgumentException("Could not load network from the provided string " + netString);
		}
		
		// make sure all cpts are normalized
		ITableFunction normalizer = getDefaultCPTNormalizer();
		for (Node node : probNet.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				ProbabilisticNode probNode = (ProbabilisticNode) node;
				normalizer.applyFunction((ProbabilisticTable) probNode.getProbabilityFunction());
			}
		}
		
		// replace shared Bayes net
		this.setProbabilisticNetwork(probNet);
		synchronized (getDefaultInferenceAlgorithm()) {
			try {
				getDefaultInferenceAlgorithm().setRelatedProbabilisticNetwork(probNet);
			} catch (InvalidParentException e) {
				throw new IllegalArgumentException("The specified network is not consistent. Please, check its content again.",e);
			}
			
			// compile the loaded bayes net
			getDefaultInferenceAlgorithm().run();
		}
		
		// update the mapping of 
		if (io instanceof ValueTreeNetIO) {
			this.setVirtualArcs(this.convertParentMappingToArcs(((ValueTreeNetIO) io).getVirtualParents()));
			// ((ValueTreeNetIO) io).getVirtualParents() is not necessary anymore
			((ValueTreeNetIO) io).setVirtualParents(null);
		}
	}
	

	/**
	 * @return true if a file is a ZIP File.
	 * This is tested by checking the 4 initial bytes of the file
	 * (it must be 0x504b0304).
	 * @see #importState(String)
	 * @see #isToCompressExportedState()
	 * @see #isToReturnIdentifiersInExportState()
	 */
	public static boolean isZipFile(File file) throws IOException {
	    if(file.isDirectory()) {
	        return false;
	    }
	    if(!file.canRead()) {
	        throw new IOException("Cannot read file "+file.getAbsolutePath());
	    }
	    if(file.length() < 4) {
	        return false;
	    }
	    DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
	    int test = in.readInt();
	    in.close();
	    return test == 0x504b0304;
	}

	/**
	 * @param virtualParents : a mapping from child (key) to its parents (value)
	 * @return as the name suggests, it creates a new list of new edges (not present in {@link #getProbabilisticNetwork()}) from
	 * the mapping which maps children (keys) to their parents (value).
	 * @see #convertArcsToParentMapping(List)
	 * @see ValueTreeNetIO#getVirtualParents()
	 * @see #getVirtualParentMapping()
	 */
	protected List<Edge> convertParentMappingToArcs( Map<INode, List<INode>> virtualParents) {
		List<Edge> ret = new ArrayList<Edge>();
		if (virtualParents != null) {
			// each child(key)-parent(a node in the mapped list) pair becomes a new instance of Edge
			for (Entry<INode, List<INode>> entry : virtualParents.entrySet()) {
				if (entry.getValue() != null) {
					for (INode parent : entry.getValue()) {
						// TODO use INode instead of casting them to Node
						ret.add(new Edge((Node)parent, (Node)entry.getKey()));
					}
				}
			}
		}
		return ret;
	}

	/**
	 * @return : default object to be used in order to normalize cpt tables.
	 * @see #importState(String)
	 * @see AddQuestionAssumptionNetworkAction#addArcsAssumingReboot(ProbabilisticNetwork)O
	 */
	public ITableFunction getDefaultCPTNormalizer() {
		return defaultCPTNormalizer;
	}

	/**
	 * If this flag is off, then history of trades will not be traced anymore.
	 * In other words, methods like {@link #addVirtualTradeIntoMarginalHistory(ResolveQuestionNetworkAction, Map)}
	 * or {@link #addVirtualTradeIntoMarginalHistory(NetworkAction, Map)} and
	 * {@link #getQuestionHistory(Long, List, List)} or {@link #getExecutedActions()}
	 * will return immediately.
	 * @return the isToTraceHistory
	 */
	public boolean isToTraceHistory() {
		return isToTraceHistory;
	}

	/**
	 * If this flag is off, then history of trades will not be traced anymore.
	 * In other words, methods like {@link #addVirtualTradeIntoMarginalHistory(ResolveQuestionNetworkAction, Map)}
	 * or {@link #addVirtualTradeIntoMarginalHistory(NetworkAction, Map)} and
	 * {@link #getQuestionHistory(Long, List, List)} or {@link #getExecutedActions()}
	 * will return immediately.
	 * @param isToTraceHistory the isToTraceHistory to set
	 */
	public void setToTraceHistory(boolean isToTraceHistory) {
		this.isToTraceHistory = isToTraceHistory;
		if (!isToTraceHistory) {
			// if this flag is off, history shall not be stored anymore
			setMaxConditionalProbHistorySize(0);
			getExecutedActions().clear();
			
			// reset map which manages which questions have trades from a given user
			setTradedQuestionsMap(new HashMap<Long, Set<Long>>());
			
			// if we are only using probabilistic network, we won't be storing history of trades (it is not required, so let's not use space)
			setLastNCliquePotentialMap(new HashMap<Clique, List<ParentActionPotentialTablePair>>());
			
			// also stop tracing virtual trades, because storing history is not required
			setVirtualTradeToAffectedQuestionsMap(new HashMap<VirtualTradeAction, Set<Long>>());
			
			setNetworkActionsIndexedByQuestions(null);	// do not trace actions
			
			// if history is not used, we can only export current configuration of the network
			setToExportOnlyCurrentSharedProbabilisticNet(true);
			
			// if we are not storing history, then there is no need to store cash before resolve question
			setToStoreCashBeforeResolveQuestion(false);
		} else {
			setNetworkActionsIndexedByQuestions(new HashMap<Long, List<NetworkAction>>());	// HashMap allows null key.
			setMaxConditionalProbHistorySize(DEFAULT_MAX_CONDITIONAL_PROB_HISTORY_SIZE);
		}
	}
	
	/**
	 * This will simply return the maximum size of cliques (in number of variables), after adding arcs if requested.
	 * This method simply delegates to {@link #getComplexityFactors(Map)} and extracts the value mapped from
	 * key {@link edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE}
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getNewComplexityFactor(java.util.Map)
	 * @see #getComplexityFactors(Map)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 */
	public int getComplexityFactor(Map<Long, Collection<Long>> newDependencies, boolean isLocal, boolean isSum, boolean isComplexityBeforeModification) {
		if (isSum) {
			// return the factor identified by COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE (i.e. the sum of clique table sizes) 
			return this.getComplexityFactors(newDependencies, isLocal, isComplexityBeforeModification).get(COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE).intValue();
		}
		return this.getComplexityFactors(newDependencies, isLocal, isComplexityBeforeModification).get(getDefaultComplexityFactorName()).intValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactor(java.util.Map)
	 */
	public int getComplexityFactor(Map<Long, Collection<Long>> newDependencies) {
		return this.getComplexityFactor(newDependencies, false, false, false);
	}
	
	/**
	 * This will simply return the maximum size of clique tables and also the sum of sizes of clique tables, 
	 * after adding arcs if requested.
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getNewComplexityFactors(java.util.Map)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 */
	public Map<String,Double> getComplexityFactors(Map<Long, Collection<Long>> newDependencies, boolean isLocal, boolean isComplexityBeforeModification) {
		
		ProbabilisticNetwork netToCheck = null;	// this will hold a copy of the Bayes net whose complexity will be checked
		AssetAwareInferenceAlgorithm algorithm = getDefaultInferenceAlgorithm();	// this will be used to clone the probabilistic network
		
		// I want to lock the algorithm and then lock the shared net in this order. Since other portions of this class also locks in the same order, this prevents deadlocks.
		synchronized (algorithm) {	
			ProbabilisticNetwork sharedBn = getProbabilisticNetwork();
			// the following code was commented out, because I assume the shared bayes net and the bayes net handled by the default algorithms are the same
			// ...and besides, I don't need them to be the same in the context of this method.
//			ProbabilisticNetwork sharedBn = algorithm.getRelatedProbabilisticNetwork();
//			if (sharedBn == null || !sharedBn.equals(getProbabilisticNetwork())) {
//				// this should be unnecessary, but just for backward compatibility and to make sure
//				sharedBn = getProbabilisticNetwork();
//			}
			if (isComplexityBeforeModification) {
				// TODO this is not thread safe
				netToCheck = sharedBn;
			} else {
				synchronized (sharedBn) {
					// clone the network. Use a clone regardless of whether we'll change it or not, because we may have parallel engine access
					netToCheck = algorithm.cloneProbabilisticNetwork(sharedBn);
					// TODO do not clone clique/separator potentials
				}
			}
		} // we don't need to keep the lock anymore, because we are working with a clone
		
		// nodes in this collection will be considered for local (in respect to each disconnected subnets) complexity
		Collection<INode> nodesToConsiderForLocalComplexityFactor = new HashSet<INode>();
		
		// this will be used for caching complexity factors. If this is non-null, it means that we should include complexity factor in cache
		Edge keyOfComplexityCache = null;	
		
		// check if new arcs shall be considered
		if (newDependencies != null && !newDependencies.isEmpty()) {
			boolean isModified = false;	// this is used to check if there was any modification in net structure
			// add arcs if necessary
			for (Entry<Long, Collection<Long>> entry : newDependencies.entrySet()) {
				if (entry.getKey() == null) {
					continue; // ignore null keys
				}
				// the key of the entry is the destination (i.e. target, child, or the node where the arc is pointing to), 
				Node destinationNode = netToCheck.getNode(entry.getKey().toString());
				if (!isComplexityBeforeModification && destinationNode == null) {
					// create node if it does not exist. 
					destinationNode = new ProbabilisticNode();
					destinationNode.setName(entry.getKey().toString());
//					// We don't need to care about # of states, because our objective is to find treewidth in terms of # of nodes in cliques
//					// Make sure the node has only 1 state, because we need a simple, yet consistent node.
//					destinationNode.removeStates();
//					if (destinationNode.getStatesSize() < 1) {
//						destinationNode.appendState("s");	// make sure there is at least 1 state, though
//					}
					// now, the method returns the clique size in terms of states, so use a default value as the number of states of unknown nodes
					int defaultNodeSize = getDefaultNodeSize();
					for (int i = 0; destinationNode.getStatesSize() < defaultNodeSize; i++) {
						destinationNode.appendState("s"+i);	// name of the state can be anything
					}
					// also make sure the Conditional Probability Table is consistent, just to make sure junction tree compilation won't throw exceptions
					PotentialTable cpt = (PotentialTable)((ProbabilisticNode)destinationNode).getProbabilityFunction();
					cpt.addVariable(destinationNode);
//					cpt.setValue(0, 1);	// we only have 1 state and no parents at this point, so this must be set to 100%
					// fill with uniform distribution
					int tableSize = cpt.tableSize();
					for (int i = 0; i < tableSize; i++) {
						cpt.setValue(i, 1f/defaultNodeSize);
					}
					netToCheck.addNode(destinationNode);	// make sure it is included in the network
					isModified = true;	// mark this net as modified
				}
				
				// whech whether we shall obtain complexity metrics of the disconnected subnet containing this node
				if (isLocal && destinationNode != null) {	// make sure we don't add null nodes
					nodesToConsiderForLocalComplexityFactor.add(destinationNode);
				}
				
				// values are the node the arc is pointing from (i.e. parent, or the origin of the arc)
				if (entry.getValue() != null) {
					for (Long originNodeId : entry.getValue()) {
						if (originNodeId == null) {
							continue; // ignore null values
						}
						// extract the origin node
						Node originNode = netToCheck.getNode(originNodeId.toString());
						if (!isComplexityBeforeModification && originNode == null) {
							// create node if it does not exist. 
							originNode = new ProbabilisticNode();
							originNode.setName(originNodeId.toString());
//							// We don't need to care about # of states, because our objective is to find treewidth in terms of # of nodes in cliques
//							// Make sure the node has only 1 state, because we need a simple, yet consistent node.
//							originNode.removeStates();
//							if (originNode.getStatesSize() < 1) {
//								originNode.appendState("s");	// make sure there is at least 1 state, though
//							}
							// now, the method returns the clique size in terms of states, so use a default value as the number of states of unknown nodes
							int defaultNodeSize = getDefaultNodeSize();
							for (int i = 0; originNode.getStatesSize() < defaultNodeSize; i++) {
								originNode.appendState("s"+i);	// name of the state can be anything
							}
							// also make sure the Conditional Probability Table is consistent, just to make sure junction tree compilation won't throw exceptions
							PotentialTable cpt = (PotentialTable)((ProbabilisticNode)originNode).getProbabilityFunction();
							cpt.addVariable(originNode);
//							cpt.setValue(0, 1);	// we only have 1 state and no parents at this point, so this must be set to 100%
							// fill with uniform distribution
							int tableSize = cpt.tableSize();
							for (int i = 0; i < tableSize; i++) {
								cpt.setValue(i, 1f/defaultNodeSize);
							}
							netToCheck.addNode(originNode);	// make sure it is included in the network
							// no need to set isModified = true here, because it will be set after this if-clause anyway (since we'll add new edge for sure)
						}
						

						// whech whether we shall obtain complexity metrics of the disconnected subnet containing this node
						if (isLocal && originNode != null) {	// make sure we don't add null nodes
							nodesToConsiderForLocalComplexityFactor.add(originNode);
						}
						
						// only include/exclude arcs if flag to disable modification is off
						if (!isComplexityBeforeModification) {
							// only include new arc if it does not exist
							if ((netToCheck.hasEdge(originNode, destinationNode) < 0)
									&& (netToCheck.hasEdge(destinationNode, originNode) < 0)) {		// just checking existence of link for both direction
								// TODO hasEdge seems to be a redundant check, because something similar is done in addEdge
								try {
									// finally, include the new arc
									netToCheck.addEdge(new Edge(originNode, destinationNode));
								} catch (InvalidParentException e) {
									throw new IllegalArgumentException(e);
								}
								isModified = true;	// mark this net as modified
							} else { 
								// the edge exists (either originNode->destinationNode, or the opposite)
								// extract the existing edge
								Edge edgeToRemove = netToCheck.getEdge(originNode, destinationNode);
								if (edgeToRemove == null) { // just checking if it was in the opposite direction
									edgeToRemove = netToCheck.getEdge(destinationNode, originNode);
								}
								if (edgeToRemove == null) {
									// Network#hasEdge and Network#getEdge are inconsistent each other (the prior is saying that there is a link, but the latter cannot retrieve it)
									throw new IllegalStateException("The network cloned from the shared BN indicates existence of link between  nodes " 
											+ originNode + " and " + destinationNode 
											+ ", but the link cannot be retrieved. This is probably a bug in UnBBayes core, or in some plug-in. Please, check the version you are using.");
								}
								if (newDependencies.size() <= 1) {
									// by setting key of cache to non-null, the complexity factor will be cached if no cache is found
									keyOfComplexityCache = edgeToRemove;
									// check cache
									Map<String, Map<String, Double>> complexityCache = getSingleExistingArcComplexityCache();
									if (complexityCache != null && complexityCache.containsKey(keyOfComplexityCache.toString())) {
										// simply return cached entry;
										return complexityCache.get(keyOfComplexityCache.toString());
									} // else cache doesn't exist, and complexity factor will be cached later (keyOfComplexityCache != null is a flag to force caching)
								}
								
								//remove the edge in case the edge exists;
								netToCheck.removeEdge(edgeToRemove);	// simply remove the edge, without caring about the CPT (because we just need the JT structure, not the probabilities)
								isModified = true; // needs to mark this net as modified
							}
						}	// end of if flag to disable arc inclusion/exclusion is off
					}	// end of for
				}	// end of if (entry.getValue() != null)
			}	// end of for each entry in map
			
			
			// only re-compile junction tree if we really modified network structure.
			if (isModified) {
				// Set a junction tree builder which will not try to fill probabilities or assure globally consistent during compilation, 
				// because we don't need the clique potentials in order to get number of variables.
				netToCheck.setJunctionTreeBuilder(new IJunctionTreeBuilder() {
					public IJunctionTree buildJunctionTree(Graph network) throws InstantiationException, IllegalAccessException {
						return new StructureOnlyJunctionTree((ProbabilisticNetwork) network);
					}
				});
				
				// make sure new nodes are visible for the JT compiler
				netToCheck.resetNodesCopy();
				// ...and re-compile junction tree (because we have changes in Bayes net structure).
				try {
					// try to reuse same 
					IInferenceAlgorithm probAlgorithm = getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator();
					// backup some attributes that are sensitive to context
					Graph backup = probAlgorithm.getNetwork();
					if (probAlgorithm instanceof IncrementalJunctionTreeAlgorithm) {
						// this is needed for dynamic junction tree to work, so keep backup
						ProbabilisticNetwork previousNetBackup = ((IncrementalJunctionTreeAlgorithm) probAlgorithm).getNetPreviousRun();
						// also keep backup of whether to use approximation or not 
						int loopyBPCliqueSizeThreshold = ((IncrementalJunctionTreeAlgorithm) probAlgorithm).getLoopyBPCliqueSizeThreshold();
						
						// disable approximation by setting the threshold very high
						((IncrementalJunctionTreeAlgorithm) probAlgorithm).setLoopyBPCliqueSizeThreshold(Integer.MAX_VALUE);
						
						if (((IncrementalJunctionTreeAlgorithm) probAlgorithm).isLoopy()) {
							// do not measure complexity of approximate structure. Try using exact structure.
							// disabling incremental JT compilation will compile an exact structure
							((IncrementalJunctionTreeAlgorithm) probAlgorithm).setNetPreviousRun(null); // setting this to null should disable incremental JT compilation
						}
						
						// compile the cloned network
						probAlgorithm.setNetwork(netToCheck);
						probAlgorithm.run();
						
						// restore backup for dynamic JT compilation
						((IncrementalJunctionTreeAlgorithm) probAlgorithm).setNetPreviousRun(previousNetBackup);
						// restore backup for loopy bp threshold
						((IncrementalJunctionTreeAlgorithm) probAlgorithm).setLoopyBPCliqueSizeThreshold(loopyBPCliqueSizeThreshold);
					} else {
						Debug.println(getClass(), "The associated probability algorithm is not a incremental junction tree algorithm");
//						netToCheck.compile();	// this is usually faster to compile JT, but doesn't use any algorithm-level optimization (like dynamic junction tree compilation)
						probAlgorithm.setNetwork(netToCheck);
						probAlgorithm.run();
					}
					// restore backup
					probAlgorithm.setNetwork(backup);
				} catch (Exception e) {
					// this shall also revert the change, because getProbabilisticNetwork() shall contain the shared bayes net
					getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator().setNetwork(getProbabilisticNetwork());
					// TODO stop using exception translation
					throw new RuntimeException("Unable to compile the network after including (or removing) arcs: " + newDependencies, e);
				}	
			}
		} // end of if (newDependencies != null && !newDependencies.isEmpty())
	
		// return the maximum number of variables. 
		Map<String, Double> complexityFactor = this.getComplexityFactor(netToCheck, nodesToConsiderForLocalComplexityFactor); // nodesToConsiderForLocalComplexityFactor is empty if isLocal == false
		
		// check if we should update cache of complexity factors for single existing arcs
		if (keyOfComplexityCache != null) {	
			// if this variable is non-null, then the condition to use cache was satisfied. 
			// But if the program reached this point, then the cache did not exist, so overwrite cache
			Map<String, Map<String, Double>> cache = getSingleExistingArcComplexityCache();
			if (cache != null) {
				Map<String, Double> oldCachedValue = cache.put(keyOfComplexityCache.toString(), complexityFactor);
				if (oldCachedValue != null) {
					Debug.println(getClass(), "Cache was not supposed to exist for " + keyOfComplexityCache + ", but found " + oldCachedValue);
					Debug.println(getClass(), "Resetting cache due to desync... ");
					cache.clear();
				}
			}
		}
		
		return complexityFactor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactors(java.util.Map)
	 */
	public Map<String,Double> getComplexityFactors(Map<Long, Collection<Long>> newDependencies) {
		return this.getComplexityFactors(newDependencies, false, false);
	}
	
	/**
	 * @deprecated this is only kept for backward compatibility. Use {@link #getComplexityFactor(ProbabilisticNetwork, Collection)} instead.
	 */
	@Deprecated
	protected Map<String,Double> getComplexityFactor(ProbabilisticNetwork net) {
		return this.getComplexityFactor(net, (List)Collections.emptyList());
	}
	
	/**
	 * This is used in {@link #getComplexityFactors(Map)} in order to return the maximum table size of a clique,
	 * and the sum of clique table sizes.
	 * Subclasses can overwrite this method in order to make {@link #getComplexityFactors(Map)} to return desired metrics from a {@link ProbabilisticNetwork}.
	 * @param net : network to be evaluated (we can use {@link ProbabilisticNetwork#getJunctionTree()} to retrieve junction tree)
	 * @param nodesToConsiderForLocalComplexityFactor : if this is non-null and non-empty,
	 * then only the complexity factors local to disconnected subnets containing these nodes will be considered.
	 * @return maximum clique table size (identified with key {@link edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE});
	 * and the sum of clique table sizes (identified with key {@link edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE})
	 * If the network does not have cliques, then 0 will be returned by default.
	 * @see #getNetStatistics()
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 */
	protected Map<String,Double> getComplexityFactor(ProbabilisticNetwork net, Collection<INode> nodesToConsiderForLocalComplexityFactor) {
		// TODO remove redundancy with #getNetStatistics().
		
		// the map to return
		Map<String,Double> ret = new HashMap<String, Double>();
		ret.put(COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE, 0d);	// initialize with zeros
		ret.put(COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE, 0d);	// initialize with zeros
		
		// initial assertion
		if (net == null || net.getJunctionTree() == null || net.getJunctionTree().getCliques() == null) {
			// no clique to consider, so return immediately
			return ret;
		}
		
		int maxCliqueTableSize = 0;	 // maximum clique table size found so far
		long sumCliqueTableSize = 0; // sum of clique table sizes known so far
		
		
		
		// extract the cliques to be considered in order to estimate the metrics of complexity
//		Collection<Clique> cliquesToConsider = net.getJunctionTree().getCliques();	// the default is to consider all cliques.
//		if (nodesToConsiderForLocalComplexityFactor != null && !nodesToConsiderForLocalComplexityFactor.isEmpty()) {
//			// if nodes were specified, only consider cliques local (i.e. in the same disconnected subnet) to such nodes
//			cliquesToConsider =  net.getJunctionTree().getCliquesConnectedToNodes(nodesToConsiderForLocalComplexityFactor, null);
//		}
		Collection<Collection<Clique>> cliquesToConsider = new ArrayList<Collection<Clique>>();
		if (nodesToConsiderForLocalComplexityFactor != null && !nodesToConsiderForLocalComplexityFactor.isEmpty()) {
			// if nodes were specified, only consider cliques local (i.e. in the same disconnected subnet) to such nodes
			if (isToUseMaxForSubnetsInLinkSuggestion()) {
				// we'll calculate the max of complexity of the subnet of each node, so separate the set by cliques connected to each node
				for (INode node : nodesToConsiderForLocalComplexityFactor) {
					// each set of cliques represents the set of cliques connected to current node
					cliquesToConsider.add(net.getJunctionTree().getCliquesConnectedToNodes(Collections.singletonList(node), null));
				}
			} else {
				// calculate the integrated complexity of the subnet of all node (if disconnected, consider both together)
				cliquesToConsider.add(net.getJunctionTree().getCliquesConnectedToNodes(nodesToConsiderForLocalComplexityFactor, null));
			}
		} else {
			// the default is to consider all cliques together (i.e. get global complexity).
			cliquesToConsider.add(net.getJunctionTree().getCliques()); 
		}
		
		// iterate on each set of cliques. If global complexity is calculated, or if !isToUseMaxForSubnetsInLinkSuggestion(), then there is only 1 set with all the cliques to consider
		for (Collection<Clique> cliques : cliquesToConsider) {
			// prepare variables to get max and sum clique sizes for current set of cliques
			int currentMaxCliqueTableSize = 0;
			int currentSumCliqueTableSize = 0;
			// iterate on cliques in order to get the metrics
			for (Clique clique : cliques) {
//			// clique.getNodesList().size() is supposedly the number of variables/nodes in this clique,
//			// but I prefer to use clique.getProbabilityFunction().getVariablesSize(), which is the number of variables/nodes in the clique table (this is more reliable).
//			int valueInThisClique = clique.getProbabilityFunction().getVariablesSize();	// the number of variables/nodes in current clique
				// using table size instead
				int valueInThisClique = clique.getProbabilityFunction().tableSize();	// this is also the state space of current clique
//			// we may be using a junction tree builder that does not fill clique tables, so needs to calculate state space on the fly
//			int valueInThisClique = 1;
//			for (Node node : clique.getNodesList()) {
//				valueInThisClique *= node.getStatesSize();
//			}
				// update the max
				if (valueInThisClique > currentMaxCliqueTableSize) {
					// this is the maximum we know so far
					currentMaxCliqueTableSize = valueInThisClique;
				}
				
				// also update the sum
				currentSumCliqueTableSize += valueInThisClique;
			}
			
			// store the largest (of all set of cliques) of the sum/max clique sizes 
			if (currentSumCliqueTableSize > sumCliqueTableSize) {
				sumCliqueTableSize = currentSumCliqueTableSize;
			}
			if (currentMaxCliqueTableSize > maxCliqueTableSize) {
				maxCliqueTableSize = currentMaxCliqueTableSize;
			}
		}
		
		// put the obtained values into the map to be returned, and return it
		ret.put(COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE, (double) maxCliqueTableSize);
		ret.put(COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE, (double) sumCliqueTableSize);	
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactor(java.lang.Long, java.util.List, boolean, boolean, boolean)
	 */
	public int getComplexityFactor(Long childQuestionId, List<Long> parentQuestionIds, boolean isLocal, boolean isSum, boolean isComplexityBeforeModification) {
		if (childQuestionId == null) {
			// special case: we want the current complexity, not the complexity after including new arcs
			return this.getComplexityFactor((Map)null, isLocal, isSum, isComplexityBeforeModification);
		}
//		if (parentQuestionIds == null) {
//			// make sure we don't pass null to map's value
//			parentQuestionIds = Collections.EMPTY_LIST;
//		}
		return this.getComplexityFactor((Map)Collections.singletonMap(childQuestionId, parentQuestionIds), isLocal, isSum, isComplexityBeforeModification);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactor(java.lang.Long, java.util.List)
	 */
	public int getComplexityFactor(Long childQuestionId, List<Long> parentQuestionIds) {
		return this.getComplexityFactor(childQuestionId, parentQuestionIds, false, false, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactor(java.util.List, java.util.List, boolean, boolean, boolean)
	 */
	public int getComplexityFactor(List<Long> childQuestionIds, List<Long> parentQuestionIds, boolean isLocal, boolean isSum, boolean isComplexityBeforeModification) {
		// initial assertions to avoid NullPointerException
		if (childQuestionIds == null /*|| parentQuestionIds == null*/) {
			// special case: we want the current complexity, not the complexity after including new arcs
			return this.getComplexityFactor((Map)null, isLocal, isSum, isComplexityBeforeModification);
		}
		if (parentQuestionIds == null) {
			// make sure we don't use null
			parentQuestionIds = Collections.EMPTY_LIST;
		}
		
		// this will be the map to be passed to #getComplexityFactor(Map). 
		Map<Long, Collection<Long>> newDependencies = new HashMap<Long, Collection<Long>>();
		
		// fill the map. 
		for (int i = 0; i < childQuestionIds.size(); i++) {
			// If childQuestionIds.size() < parentQuestionIds.size(), then last few values in parentQuestionIds will be ignored.
			
			// get the value to become the key of the map
			Long childId = childQuestionIds.get(i);
			// TODO shall we allow null keys?
			
			// extract the value
			Collection<Long> parentIds = newDependencies.get(childId);
			if (parentIds == null) {
				// if this is the first time we find childId, then instantiate value of the map
				// use Collections.EMPTY_LIST to save memory, if we know for sure this list won't be filled
				parentIds = (i < parentQuestionIds.size())?new ArrayList<Long>():Collections.EMPTY_LIST;	
				// by default, Collection is a reference, so pushing it to the map and then modifying the collection shall also modify the value in the map.
				newDependencies.put(childId, parentIds);
			}
			
			// If childQuestionIds.size() > parentQuestionIds.size(), then 
			if (i < parentQuestionIds.size()) {
				// extract the ID of the parent and add to the value in the map
				Long parentId = parentQuestionIds.get(i);
				// TODO shall we allow null values?
				parentIds.add(parentId);
				// since parentIds is a reference (to values in the map), we don't need to push it to the map again
			}
		}
		
		// delegate to #getComplexityFactor(Map).
		return getComplexityFactor(newDependencies, isLocal, isSum, isComplexityBeforeModification);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactor(java.util.List, java.util.List)
	 */
	public int getComplexityFactor(List<Long> childQuestionIds, List<Long> parentQuestionIds) {
		return this.getComplexityFactor(childQuestionIds, parentQuestionIds, false, false, false);
	}

	/**
	 * If true, then {@link #exportNetwork(File)} will simply call
	 * {@link #exportState()} and store it to a file.
	 * If false, then it will perform the normal export behavior, which
	 * is to export all nodes and arcs including the resolved ones.
	 * The {@link #importNetwork(File)} will also call {@link #importState(String)}
	 * if this is true.
	 * @return the isToExportOnlyCurrentSharedProbabilisticNet
	 */
	public boolean isToExportOnlyCurrentSharedProbabilisticNet() {
		return isToExportOnlyCurrentSharedProbabilisticNet;
	}

	/**
	 * If true, then {@link #exportNetwork(File)} will simply call
	 * {@link #exportState()} and store it to a file.
	 * If false, then it will perform the normal export behavior, which
	 * is to export all nodes and arcs including the resolved ones.
	 * The {@link #importNetwork(File)} will also call {@link #importState(String)}
	 * if this is true.
	 * @param isToExportOnlyCurrentSharedProbabilisticNet the isToExportOnlyCurrentSharedProbabilisticNet to set
	 */
	public void setToExportOnlyCurrentSharedProbabilisticNet(
			boolean isToExportOnlyCurrentSharedProbabilisticNet) {
		this.isToExportOnlyCurrentSharedProbabilisticNet = isToExportOnlyCurrentSharedProbabilisticNet;
	}

	/**
	 * This {@link NetIO} is used in {@link #exportState()} and
	 * {@link #importState(String)} in order to export/import
	 * current shared net from file.
	 * The {@link #exportState()} will call {@link NetIO#setPrintStreamBuilder(unbbayes.io.IPrintStreamBuilder)}
	 * with {@link StringPrintStreamBuilder} in order to use {@link NetIO} for printing net files to string instead of
	 * file. Similarly, {@link #importState(String)} will call
	 * {@link NetIO#setReaderBuilder(unbbayes.io.IReaderBuilder)}
	 * with {@link StringReaderBuilder}
	 * in order to use {@link NetIO} for reading net files from string instead of from file.
	 * <br/>
	 * <br/>
	 * This method is kept protected in order to allow access from subclasses,
	 * but this shall be handled with care, since it will directly impact
	 * where {@link #exportState()} and {@link #importState(String)}
	 * will reference to.
	 * @return the netIOToExportSharedNetToSting
	 * @see #setNetIOToExportSharedNetToSting(NetIO)
	 */
	protected NetIO getNetIOToExportSharedNetToString() {
		return netIOToExportSharedNetToSting;
	}

	/**
	 * This {@link NetIO} is used in {@link #exportState()} and
	 * {@link #importState(String)} in order to export/import
	 * current shared net from file.
	 * The {@link #exportState()} will call {@link NetIO#setPrintStreamBuilder(unbbayes.io.IPrintStreamBuilder)}
	 * with {@link StringPrintStreamBuilder} in order to use {@link NetIO} for printing net files to string instead of
	 * file. Similarly, {@link #importState(String)} will call
	 * {@link NetIO#setReaderBuilder(unbbayes.io.IReaderBuilder)}
	 * with {@link StringReaderBuilder}
	 * in order to use {@link NetIO} for reading net files from string instead of from file.
	 * <br/>
	 * <br/>
	 * This method is kept protected in order to allow access from subclasses,
	 * but this shall be handled with care, since it will directly impact
	 * where {@link #exportState()} and {@link #importState(String)}
	 * will reference to.
	 * @param netIOToExportSharedNetToSting the netIOToExportSharedNetToSting to set
	 * @see #getNetIOToExportSharedNetToString()
	 */
	protected void setNetIOToExportSharedNetToSting( NetIO netIOToExportSharedNetToSting) {
		this.netIOToExportSharedNetToSting = netIOToExportSharedNetToSting;
	}

	/**
	 * If true, then trades on P(X|Y) will cause Y and X to be connected by an arc,
	 * so that the joint probability distribution can still be represented
	 * in terms of CPTs of each node given only their parents..
	 * @return the isToAddArcsOnAddTrade
	 */
	public boolean isToAddArcsOnAddTrade() {
		return isToAddArcsOnAddTrade;
	}

	/**
	 * If true, then trades on P(X|Y) will cause Y and X to be connected by an arc,
	 * so that the joint probability distribution can still be represented
	 * in terms of CPTs of each node given only their parents..
	 * @param isToAddArcsOnAddTrade the isToAddArcsOnAddTrade to set
	 */
	public void setToAddArcsOnAddTrade(boolean isToAddArcsOnAddTrade) {
		this.isToAddArcsOnAddTrade = isToAddArcsOnAddTrade;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getVersionInfo()
	 */
	public String getVersionInfo() {
		return "UnBBayes SciCast Markov Engine 1.6.14";
	}

	/**
	 * @return the virtualArcs : these arcs will be tagged as "virtual", so arcs will be temporary created
	 * during structure changes, so that cycles are not created because of the presence of such virtual arcs (when creating new "normal" arcs); 
	 * or conditional probabilities of nodes not directly connected becomes
	 * fully specifyable by only specifying nodes and their cpts given parents when probabilities are needed to be stored temporary.
	 * By setting this to null, virtual parents will be disabled.
	 * @see #exportState()
	 * @see #importState(String)
	 * @see AddQuestionAssumptionNetworkAction
	 * @see ValueTreeNetIO#getVirtualParents()
	 * @see #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)
	 * @see #simpleAddEdge(List, Node, ProbabilisticNetwork)
	 * 
	 */
	public List<Edge> getVirtualArcs() {
		return this.virtualArcs;
	}
	
//	/**
//	 * @return the virtualArcs : these arcs will (mapping which specifies parents = values of a child = key) be tagged as "virtual", so arcs will be temporary created
//	 * during structure changes, so that conditional probabilities of nodes not directly connected becomes
//	 * fully specifyable by only specifying nodes and their cpts given parents.
//	 * By setting this to null, virtual parents will be disabled.
//	 * @see #exportState()
//	 * @see #importState(String)
//	 * @see AddQuestionAssumptionNetworkAction
//	 * @see ValueTreeNetIO#getVirtualParents()
//	 * @see #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)
//	 * @see #simpleAddEdge(List, Node, ProbabilisticNetwork)
//	 * 
//	 */
//	public Map<INode,List<INode>> getVirtualParentMapping() {
//		return this.virtualParentMapping;
//	}

	/**
	 * @param virtualArcs : these arcs are tagged as "virtual", so arcs will only be temporary created
	 * during structure changes, so that cycles are not created because of the presence of such virtual arcs (when creating new "normal" arcs); 
	 * or conditional probabilities of nodes not directly connected becomes
	 * fully specifyable by only specifying nodes and their cpts given parents when probabilities are needed to be stored temporary.
	 * By setting this to null, virtual parents will be disabled.
	 * @see #exportState()
	 * @see #importState(String)
	 * @see AddQuestionAssumptionNetworkAction
	 * @see ValueTreeNetIO#setVirtualParents(Map)
	 * @see #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)
	 * @see #simpleAddEdge(List, Node, ProbabilisticNetwork)
	 */
	public void setVirtualArcs(List<Edge> virtualArcs) {
		this.virtualArcs = virtualArcs;
	}
	
//	/**
//	 * @param virtualParents : these arcs (mapping which specifies parents = values of a child = key) are tagged as "virtual", so arcs will only be temporary created
//	 * during structure changes, so that conditional probabilities of nodes not directly connected becomes
//	 * fully specifyable by only specifying nodes and their cpts given parents.
//	 * By setting this to null, virtual parents will be disabled.
//	 * @see #exportState()
//	 * @see #importState(String)
//	 * @see AddQuestionAssumptionNetworkAction
//	 * @see ValueTreeNetIO#setVirtualParents(Map)
//	 * @see #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)
//	 * @see #simpleAddEdge(List, Node, ProbabilisticNetwork)
//	 */
//	public void setVirtualParentMapping(Map<INode,List<INode>> virtualParents) {
//		this.virtualParentMapping = virtualParents;
//	}

	/**
	 * @return the isToAddVirtualArcsOnAddTrade
	 */
	public boolean isToAddVirtualArcsOnAddTrade() {
		return isToAddVirtualArcsOnAddTrade;
	}

	/**
	 * @param isToAddVirtualArcsOnAddTrade the isToAddVirtualArcsOnAddTrade to set
	 */
	public void setToAddVirtualArcsOnAddTrade(boolean isToAddVirtualArcsOnAddTrade) {
		if (isToAddVirtualArcsOnAddTrade) {
			if (!this.isToAddVirtualArcsOnAddTrade) {
				setVirtualArcs(new ArrayList<Edge>());
			}
		} else {
			setVirtualArcs(null);
		}
		this.isToAddVirtualArcsOnAddTrade = isToAddVirtualArcsOnAddTrade;
	}

	/**
	 *  If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)}
	 * will call {@link #addQuestionAssumption(Long, Date, long, List, List)} in order to force connection of nodes
	 * that participates in the trade.
	 * @return the isToAddArcsOnAddTradeAndUpdateJT
	 */
	public boolean isToAddArcsOnAddTradeAndUpdateJT() {
		return isToAddArcsOnAddTradeAndUpdateJT;
	}

	/**
	 *  If true, {@link #executeTrade(long, List, List, List, List, boolean, AssetAwareInferenceAlgorithm, boolean, boolean, NetworkAction, boolean)}
	 * will call {@link #addQuestionAssumption(Long, Date, long, List, List)} in order to force connection of nodes
	 * that participates in the trade.
	 * @param isToAddArcsOnAddTradeAndUpdateJT the isToAddArcsOnAddTradeAndUpdateJT to set
	 */
	public void setToAddArcsOnAddTradeAndUpdateJT(
			boolean isToAddArcsOnAddTradeAndUpdateJT) {
		this.isToAddArcsOnAddTradeAndUpdateJT = isToAddArcsOnAddTradeAndUpdateJT;
	}

	/**
	 * @return If true, {@link #importState(String)} and {@link #exportState()} will consider compressed format (e.g. zip and then base64 encoding)
	 */
	public boolean isToCompressExportedState() {
		return isToCompressExportedState;
	}

	/**
	 * @param isToCompressExportedState : If true, {@link #importState(String)} and {@link #exportState()} will consider compressed format (e.g. zip and then base64 encoding)
	 */
	public void setToCompressExportedState(boolean isToCompressExportedState) {
		this.isToCompressExportedState = isToCompressExportedState;
	}

	/**
	 * @return if this is true, then
	 * {@link #exportState()} will save a file in local repository and return an identifier.
	 * Additionally, {@link #importState(String)} will consider that its argument is an identifier,
	 * and will attempt to load network from local file.
	 */
	public boolean isToReturnIdentifiersInExportState() {
		return isToReturnIdentifiersInExportState;
	}

	/**
	 * @param isToReturnIdentifiersInExportState : if this is true, then
	 * {@link #exportState()} will save a file in local repository and return an identifier.
	 * Additionally, {@link #importState(String)} will consider that its argument is an identifier,
	 * and will attempt to load network from local file.
	 */
	public void setToReturnIdentifiersInExportState(
			boolean isToReturnIdentifiersInExportState) {
		this.isToReturnIdentifiersInExportState = isToReturnIdentifiersInExportState;
	}

	/**
	 * @return if true, then updates in probability that are not bayesian can be performed.
	 * For instance, this will allow states that are 0% (impossible) to be changed to non 0.
	 * @see #moveUpZeroProbability()
	 */
	public boolean isToAllowNonBayesianUpdate() {
		return isToAllowNonBayesianUpdate;
	}

	/**
	 * @param isToAllowNonBayesianUpdate: if true, then updates in probability that are not bayesian can be performed.
	 * For instance, this will allow states that are 0% (impossible) to be changed to non 0.
	 * @see #moveUpZeroProbability()
	 */
	public void setToAllowNonBayesianUpdate(boolean isToAllowNonBayesianUpdate) {
		this.isToAllowNonBayesianUpdate = isToAllowNonBayesianUpdate;
	}

	/**
	 * @return Folder where files generated by {@link #exportState()} will be stored, 
	 * and where {@link #importState(String)} will look for files when {@link #isToReturnIdentifiersInExportState()} is true.
	 */
	public String getStatesFolderName() {
		return statesFolderName;
	}

	/**
	 * @param statesFolderName : folder where files generated by {@link #exportState()} will be stored, 
	 * and where {@link #importState(String)} will look for files when {@link #isToReturnIdentifiersInExportState()} is true.
	 */
	public void setStatesFolderName(String statesFolderName) {
		this.statesFolderName = statesFolderName;
	}

	/**
	 * @return Extension of files generated by {@link #exportState()} and to be read by {@link #importState(String)} when {@link #isToReturnIdentifiersInExportState()} is true.
	 */
	public String getExportedStateFileExtension() {
		return exportedStateFileExtension;
	}

	/**
	 * @param exportedStateFileExtension: Extension of files generated by {@link #exportState()} and to be read by {@link #importState(String)} when {@link #isToReturnIdentifiersInExportState()} is true.
	 */
	public void setExportedStateFileExtension(String exportedStateFileExtension) {
		this.exportedStateFileExtension = exportedStateFileExtension;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#addJointTrade(java.lang.Long, java.util.Date, java.util.List, java.util.List)
	 */
	public boolean addJointTrade(Long transactionKey, Date occurredWhen, List<Long> targetVariables, List<Float> newValues) {
		throw new UnsupportedOperationException("Not implemented yet");
		
	}
	
	/**
	 * This method can be used to switch the behavior of exception handling of junction tree algorithm of probabilities.
	 * If set to true, a failure in dynamic junction tree compilation will result in exception.
	 * If set to false, a failure in dynamic junction tree compilation will simply trigger default (ordinal) junction tree compilation.
	 *  Set this to true to debug dynamic junction tree compilation feature.
	 * {@link AssetAwareInferenceAlgorithm#getProbabilityPropagationDelegator()} of
	 *  {@link #getDefaultInferenceAlgorithm()} must be an instance of {@link IncrementalJunctionTreeAlgorithm} in order for this to work.
	 *  This needs to be called AFTER {@link #initialize()}
	 * @param isToHalt : this value will be delegated to {@link IncrementalJunctionTreeAlgorithm#setToHaltOnDynamicJunctionTreeFailure(boolean)}
	 * of {@link AssetAwareInferenceAlgorithm#getProbabilityPropagationDelegator()} of
	 *  {@link #getDefaultInferenceAlgorithm()}.
	 * @see IncrementalJunctionTreeAlgorithm#run()
	 * @see IncrementalJunctionTreeAlgorithm#setDynamicJunctionTreeNetSizeThreshold(int)
	 * @deprecated this is for the purpose of debugging, so it may be eventually removed in future releases.
	 */
	public void setToThrowExceptionOnDynamicJunctionTreeCompilationFailure(boolean isToHalt) {
		this.isToThrowExceptionOnDynamicJunctionTreeCompilationFailure = isToHalt;
		IInferenceAlgorithm algorithm = getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator();
		if (algorithm instanceof IncrementalJunctionTreeAlgorithm) {
			((IncrementalJunctionTreeAlgorithm) algorithm).setToHaltOnDynamicJunctionTreeFailure(isToHalt);
		}
	}

	/**
	 * @return If {@link #getComplexityFactor(Map)} is called for a node that does not exist, this will be considered the default size (number of states) of a node.
	 */
	public int getDefaultNodeSize() {
		return defaultNodeSize;
	}

	/**
	 * @param defaultNodeSize : If {@link #getComplexityFactor(Map)} is called for a node that does not exist, this will be considered the default size (number of states) of a node.
	 */
	public void setDefaultNodeSize(int defaultNodeSize) {
		this.defaultNodeSize = defaultNodeSize;
	}

	/**
	 * Set this to true to debug dynamic junction tree compilation feature.
	 * @return the isToThrowExceptionOnDynamicJunctionTreeCompilationFailure
	 * @see #setToThrowExceptionOnDynamicJunctionTreeCompilationFailure(boolean)
	 * @see IncrementalJunctionTreeAlgorithm#setToHaltOnDynamicJunctionTreeFailure(boolean)
	 * @deprecated this is for debugging purpose.
	 */
	protected boolean isToThrowExceptionOnDynamicJunctionTreeCompilationFailure() {
		return this.isToThrowExceptionOnDynamicJunctionTreeCompilationFailure;
	}

	/**
	 * @return {@link #getProbabilisticNetwork()} will be compiled with dynamic junction tree compilation if number of nodes is above this value.
	 * @see IncrementalJunctionTreeAlgorithm#getDynamicJunctionTreeNetSizeThreshold()
	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
	 */
	public int getDynamicJunctionTreeNetSizeThreshold() {
		return dynamicJunctionTreeNetSizeThreshold;
	}

	/**
	 * @param dynamicJunctionTreeNetSizeThreshold : {@link #getProbabilisticNetwork()} will be compiled with dynamic junction tree compilation if number of nodes is above this value.
	 * @see IncrementalJunctionTreeAlgorithm#setDynamicJunctionTreeNetSizeThreshold(int)
	 * @see #getDynamicJunctionTreeNetSizeThreshold()
	 */
	public void setDynamicJunctionTreeNetSizeThreshold(
			int dynamicJunctionTreeNetSizeThreshold) {
		this.dynamicJunctionTreeNetSizeThreshold = dynamicJunctionTreeNetSizeThreshold;
		IInferenceAlgorithm algorithm = getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator();
		if (algorithm instanceof IncrementalJunctionTreeAlgorithm) {
			((IncrementalJunctionTreeAlgorithm) algorithm).setDynamicJunctionTreeNetSizeThreshold(dynamicJunctionTreeNetSizeThreshold);
		}
	}
	

	/**
	 * @return the key of {@link #getComplexityFactors(Map)} returned by default by {@link #getComplexityFactor(Map)}.
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Long, List)
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 */
	public String getDefaultComplexityFactorName() {
		return defaultComplexityFactorName;
	}

	/**
	 * @param defaultComplexityFactorName : the key of {@link #getComplexityFactors(Map)} returned by default by {@link #getComplexityFactor(Map)}.
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Long, List)
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 */
	public void setDefaultComplexityFactorName(String defaultComplexityFactorName) {
		this.defaultComplexityFactorName = defaultComplexityFactorName;
	}
	
	/**
	 * @return the error margin which will be used when comparing probabilities in loopy BP.
	 * If a loop does not result in change in clique potential larger than this value, then a loop will stop.
	 * @see IncrementalJunctionTreeAlgorithm#getProbErrorMargin()
	 */
	public float getProbErrorMargin() {
		try {
			return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).getProbErrorMargin();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return Float.MIN_NORMAL;
	}

	/**
	 * @param probErrorMargin : the error margin which will be used when comparing probabilities in loopy BP.
	 * If a loop does not result in change in clique potential larger than this value, then a loop will stop.
	 * @see IncrementalJunctionTreeAlgorithm#setProbErrorMargin(float)
	 */
	public void setProbErrorMargin(float probErrorMargin) {
		((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setProbErrorMargin(probErrorMargin);
//		try {
//			((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setProbErrorMargin(probErrorMargin);
//		} catch (ClassCastException e) {
//			Debug.println(getClass(), e.getMessage(), e);
//		} catch (NullPointerException e) {
//			Debug.println(getClass(), e.getMessage(), e);
//		}
	}


	/**
	 * @return the max number of iterations the loopy belief propagation.
	 * @see IncrementalJunctionTreeAlgorithm#getMaxLoopyBPIteration()
	 */
	public int getMaxLoopyBPIteration() {
		try {
			return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).getMaxLoopyBPIteration();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return 1;
	}

	/**
	 * @param maxLoopyBPIteration : the max number of iterations the loopy belief propagation.
	 * @see IncrementalJunctionTreeAlgorithm#setMaxLoopyBPIteration(int)
	 */
	public void setMaxLoopyBPIteration(int maxLoopyBPIteration) {
		((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setMaxLoopyBPIteration(maxLoopyBPIteration);
//		try {
//			((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setMaxLoopyBPIteration(maxLoopyBPIteration);
//		} catch (ClassCastException e) {
//			Debug.println(getClass(), e.getMessage(), e);
//		} catch (NullPointerException e) {
//			Debug.println(getClass(), e.getMessage(), e);
//		}
	}
	
	/**
	 * @return the time in milliseconds allowed for the loopy belief propagation to run iterations.
	 * After an iteration (a collect/propagate evidence loop) finishes, the time will be checked, 
	 * and the loopy BP will stop if the time exceeded this amount.
	 * @see IncrementalJunctionTreeAlgorithm#getMaxLoopyBPTimeMillis()
	 */
	public long getMaxLoopyBPTimeMillis() {
		try {
			return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).getMaxLoopyBPTimeMillis();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return Long.MAX_VALUE;
	}

	/**
	 * @param maxLoopyBPTimeMillis : the time in milliseconds allowed for the loopy belief propagation to run iterations.
	 * After an iteration (a collect/propagate evidence loop) finishes, the time will be checked, 
	 * and the loopy BP will stop if the time exceeded this amount.
	 * @see IncrementalJunctionTreeAlgorithm#setMaxLoopyBPTimeMillis(long)
	 */
	public void setMaxLoopyBPTimeMillis(long maxLoopyBPTimeMillis) {
//		try {
//			((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setMaxLoopyBPTimeMillis(maxLoopyBPTimeMillis);
//		} catch (ClassCastException e) {
//			Debug.println(getClass(), e.getMessage(), e);
//		} catch (NullPointerException e) {
//			Debug.println(getClass(), e.getMessage(), e);
//		}
		((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setMaxLoopyBPTimeMillis(maxLoopyBPTimeMillis);
	}


	/**
	 * @return if the max clique size of the junction tree is above this threshold, then loopy BP will be enabled.
	 * @see IncrementalJunctionTreeAlgorithm#buildLoopyCliques(ProbabilisticNetwork)
	 * @see IncrementalJunctionTreeAlgorithm#run()
	 */
	public int getLoopyBPCliqueSizeThreshold() {
		try {
			return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).getLoopyBPCliqueSizeThreshold();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * @param loopyBPCliqueSizeThreshold the loopyBPCliqueSizeThreshold to set.
	 * If the max clique size of the junction tree is above this threshold, then loopy BP will be enabled.
	 * @see IncrementalJunctionTreeAlgorithm#buildLoopyCliques(ProbabilisticNetwork)
	 * @see IncrementalJunctionTreeAlgorithm#run()
	 */
	public void setLoopyBPCliqueSizeThreshold(int loopyBPCliqueSizeThreshold) {
		((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setLoopyBPCliqueSizeThreshold(loopyBPCliqueSizeThreshold);
	}
	

	/**
	 * @return it will just delegate to {@link IncrementalJunctionTreeAlgorithm#isLoopy()}.
	 */
	public boolean isRunningApproximation() {
		try {
			return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).isLoopy();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		// if it is not associated with a IncrementalJunctionTreeAlgorithm, then by default consider that this doesn't have loops
		return false;
	}
	
	/**
	 * This will be delegated to {@link IncrementalJunctionTreeAlgorithm#isToSplitVirtualNodeClique()}.
	 * If true, the loopy BP algorithm will also attempt to split cliques that were temporary created for soft evidence.
	 */
	public boolean isToSplitVirtualNodeClique() {
		try {
			return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).isToSplitVirtualNodeClique();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return false;
	}

	/**
	 * This will be delegated to {@link IncrementalJunctionTreeAlgorithm#setToSplitVirtualNodeClique(boolean)}.
	 * If true, the loopy BP algorithm will also attempt to split cliques that were temporary created for soft evidence.
	 */
	public void setToSplitVirtualNodeClique(boolean isToSplitVirtualNodeClique) {
		try {
			((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToSplitVirtualNodeClique(isToSplitVirtualNodeClique);
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		} catch (NullPointerException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
	}

	
	/**
	 * @param p : expected distribution
	 * @param q : approximate distribution
	 * @return The Kullback–Leibler divergence D(p||q)
	 */
	public static float getKLDistance(List<Float> p , List<Float> q) {
		// basic assertions
		if ((p == null && q == null) || (p.isEmpty() && q.isEmpty())) {
			// by default, if both are null/empty, consider them equal distribution
			return 0f;	// 0 means no divergence
		}
		if (p == null || q == null || (p.size() != q.size())) {
			// no way to calculate kl distance
			return Float.POSITIVE_INFINITY;
		}
		
		// calculate kl distance := sum of expected log-divergence
		float sum = 0f;
		
		// at this point, p.size() == q.size()
		for (int i = 0; i < p.size(); i++) {
			if (p.get(i) > 0) {	// this if is just to consider 0*ln(0) = 0
				sum += p.get(i) * Math.log(p.get(i)/q.get(i));	// log-divergence is ln(p/q). Multiply by p to get its expectation.
			}
		}
		
		return sum;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactorPerAssumption(java.util.List, java.util.List, int, boolean, boolean, boolean, boolean)
	 */
	public List<Entry<Entry<Long,Long>, Integer>> getComplexityFactorPerAssumption(List<Long> childIds, List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor,
			boolean isLocal, boolean isSum, boolean isComplexityBeforeModification) {
		
		// make sure we don't have null lists
		if (childIds == null) {
			childIds = Collections.EMPTY_LIST;
		}
		if (parentIds == null) {
			parentIds = Collections.EMPTY_LIST;
		}
		
		
		// if childIds is empty, then get complexity factor for existing arcs containing parentIds (if parentIds is null or empty, then consider all arcs);
		if (childIds.isEmpty()) {
			
			// fill childIds and parentIds with pairs of nodes in existing arcs
			childIds = new ArrayList<Long>();		// use another list, because the original may be immutable
			List<Long> idsToConsider = parentIds;	// backup what was parentIds before using another instance (to avoid immutable list)
			parentIds = new ArrayList<Long>();		// again, use another list, because original may be immutable
			
			synchronized (getProbabilisticNetwork()) {
				for (Edge edge : getProbabilisticNetwork().getEdges()) {
					if (!idsToConsider.isEmpty()) {	// there is something in the filter
						// check if any node in this arc is in filter
						try {
							if (!idsToConsider.contains(Long.parseLong(edge.getDestinationNode().getName()))		// child is not in idsToConsider
									&& !idsToConsider.contains(Long.parseLong(edge.getOriginNode().getName()))) {	// and parent is not in idsToConsider
								continue;	// ignore this arc, because it did not pass the filter
							}
						} catch (NumberFormatException e) {
							Debug.println(getClass(), e.getMessage(), e);
							continue;	// consider that it did not pass the filter
						} catch (NullPointerException e) {
							Debug.println(getClass(), e.getMessage(), e);
							continue;	// consider that it did not pass the filter
						}
					}
					
					// at this point, it passed the filter, so include arc 
					try {
						// the child of the arc
						childIds.add(Long.parseLong(edge.getDestinationNode().getName()));
					} catch (NumberFormatException e) {
						Debug.println(getClass(), e.getMessage(), e);
						continue;	// don't add in parentIds
					} catch (NullPointerException e) {
						Debug.println(getClass(), e.getMessage(), e);
						continue;	// don't add in parentIds
					}
					
					try {
						// the parent of the arc
						parentIds.add(Long.parseLong(edge.getOriginNode().getName()));
					} catch (NumberFormatException e) {
						Debug.println(getClass(), e.getMessage(), e);
						childIds.remove(childIds.size()-1);	// revert change in childIds
						continue;
					} catch (NullPointerException e) {
						Debug.println(getClass(), e.getMessage(), e);
						childIds.remove(childIds.size()-1); // revert change in childIds
						continue;
					}
					
				}	// end of for each edge
			}	// release lock on probabilistic net
		}	// end of if childId.isEmpty
		

		// we will ignore last elements if one list is larger than the other (except when childIds is empty)
		int length = Math.min(childIds.size(), parentIds.size());	
		
		// prepare the list to return
		List<Entry<Entry<Long,Long>, Integer>> ret = new ArrayList<Entry<Entry<Long,Long>, Integer>>(length);
		
		// estimate complexity factor for each arc and add to list to be returned.
		for (int i = 0; i < length; i++) {
			// extract the pair of ids (this will be the link to be evaluated)
			Long childId = childIds.get(i);
			Long parentId = parentIds.get(i);
			
			// get complexity factor for current arc parentId -> childId
			int complexityFactor = this.getComplexityFactor(Collections.singletonList(childId), Collections.singletonList(parentId), isLocal, isSum, isComplexityBeforeModification);
			// ignore assumptions with complexity larger than the threshold
			if (complexityFactor > complexityFactorLimit) {
				continue;
			}
			
			if (sortByComplexityFactor) {
				// use insertion sort to sort by complexity value (i.e. by Entry#getValue())
				int indexToAdd = 0;	// index in ret where the new entry will be inserted to
				// search for a position where complexity factor at index is strictly higher
				for (; indexToAdd < ret.size(); indexToAdd++) {
					if (ret.get(indexToAdd).getValue().intValue() > complexityFactor) {
						break;
					}
				}
				// insert at the position where the next value will become strictly higher than this, but the previous value is still smaller or equal.
				ret.add(indexToAdd, new java.util.AbstractMap.SimpleEntry(new java.util.AbstractMap.SimpleEntry(parentId, childId), complexityFactor));
			} else {
				// just insert at the end of the list
				ret.add(new java.util.AbstractMap.SimpleEntry(new java.util.AbstractMap.SimpleEntry(parentId, childId), complexityFactor));
			}
			
		}
		
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactorPerAssumption(java.util.List, java.util.List, int, boolean)
	 */
	public List<Entry<Entry<Long,Long>, Integer>> getComplexityFactorPerAssumption(List<Long> childIds, List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor) {
		return getComplexityFactorPerAssumption(childIds, parentIds, complexityFactorLimit, sortByComplexityFactor, false, false, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactorPerAssumption(java.lang.Long, java.util.List, int, boolean, boolean, boolean, boolean)
	 */
	public List<Entry<Entry<Long,Long>, Integer>> getComplexityFactorPerAssumption(final Long childId, final List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor,
			boolean isLocal, boolean isSum, boolean isComplexityBeforeModification) {
		
		// simply wrap #getComplexityFactorPerAssumption(java.util.List, java.util.List, int, boolean)
		
		List<Long> childIds = null;	// this will be the 1st list in #getComplexityFactorPerAssumption(java.util.List, java.util.List, int, boolean)
		if (childId != null) {
			// create a list virtually containing parentIds.size() copies of childId
			childIds = new SingleValueList<Long>(childId, ((parentIds==null)?0:parentIds.size()));
		}
		return this.getComplexityFactorPerAssumption(childIds, parentIds, complexityFactorLimit, sortByComplexityFactor, isLocal, isSum, isComplexityBeforeModification);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getComplexityFactorPerAssumption(java.lang.Long, java.util.List, int, boolean)
	 */
	public List<Entry<Entry<Long,Long>, Integer>> getComplexityFactorPerAssumption(final Long childId, final List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor){
		return this.getComplexityFactorPerAssumption(childId, parentIds, complexityFactorLimit, sortByComplexityFactor, false, false, false);
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#removeQuestionAssumption(java.lang.Long, java.util.Date, long, java.util.List)
	 */
	public boolean removeQuestionAssumption(Long transactionKey, Date occurredWhen, long childQuestionId, List<Long> parentQuestionIds) throws IllegalArgumentException {
		// initial assertions
		if (!isToAddArcsOnlyToProbabilisticNetwork()) {
			throw new UnsupportedOperationException("Current version cannot remove arcs when assets are managed by Markov Engine");
		}
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
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
		
		
		// check existence of child
		boolean isPresentInNetOrTransactionOrHistory = false;	// this will become true if child is in network, or it is being created in current transaction, or it is present in the history
		Node child  = null;
		synchronized (getProbabilisticNetwork()) {
			child = getProbabilisticNetwork().getNode(Long.toString(childQuestionId));
		}
		if (child == null) {
			// child node does not exist. Check if there was some previous action (in same transaction) adding such node
			synchronized (actions) {
				for (NetworkAction networkAction : actions) {
					if (networkAction instanceof AddQuestionNetworkAction) {
						AddQuestionNetworkAction addQuestionNetworkAction = (AddQuestionNetworkAction) networkAction;
						if (addQuestionNetworkAction.getQuestionId().equals(childQuestionId)) {
							isPresentInNetOrTransactionOrHistory = true;
							break;
						}
					}
				}
			}
			if (!isPresentInNetOrTransactionOrHistory) {
				// see if we can find the node in history. If so, RebuildNetworkAction can add the arcs correctly
				synchronized (this.getResolvedQuestions()) {
					StatePair stateSizeAndResolution = this.getResolvedQuestions().get(childQuestionId);
					if (stateSizeAndResolution != null) {
						isPresentInNetOrTransactionOrHistory = true;
					}
				}
			}
			if (!isPresentInNetOrTransactionOrHistory) {	
				// sourceQuestionId was not found neither in transaction nor in history
				throw new InexistingQuestionException("Question ID " + childQuestionId + " does not exist.", childQuestionId);
			}
		} else {
			// initialize the value of expectedSizeOfCPD using the number of states of future owner of the cpd
			isPresentInNetOrTransactionOrHistory = true;
		}
		
		
		// do not allow null values for collections
		if (parentQuestionIds == null) {
			parentQuestionIds = new ArrayList<Long>();
		}
		
		// check existence of parents or arcs
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
//							System.out.println(addQuestionNetworkAction.getQuestionId());
							if (addQuestionNetworkAction.getQuestionId().equals(assumptiveQuestionId)) {
								// size of cpd = MULT (<quantity of states of child and parents>).
								hasFound = true;
								break;
							}
						}
					}
//					if (!hasFound) {
//						// see if we can find the node in history. If so, RebuildNetworkAction can add the arcs correctly
//						synchronized (this.getResolvedQuestions()) {
//							StatePair statePair = this.getResolvedQuestions().get(assumptiveQuestionId);
//							if (statePair != null) {
//								hasFound = true;
//							}
//						}
//					}
					if (!hasFound) {	
						// parent was not found
						throw new InexistingQuestionException("Question ID " + assumptiveQuestionId + " does not exist.", assumptiveQuestionId);
					} 
					// at this point, parent does not exist in net, but it will be created in same transaction
					// check if arc is in same transaction too
					hasFound = false;
					for (NetworkAction networkAction : actions) {
						if (networkAction instanceof AggregatedQuestionAssumptionNetworkAction) {
							AggregatedQuestionAssumptionNetworkAction action = (AggregatedQuestionAssumptionNetworkAction) networkAction;
							for (Entry<Long, List<Long>> entry : action.getLinks().entrySet()) {
								// check if link will be added in some transaction, regardless of direction
								if ((entry.getKey().equals(childQuestionId) && entry.getValue().contains(assumptiveQuestionId))
										|| (entry.getKey().equals(assumptiveQuestionId) && entry.getValue().contains(childQuestionId))) {
									// there is a link childQuestionId->assumptiveQuestionId or assumptiveQuestionId->childQuestionId
									hasFound = true;
									break;
								}
							}
						} else if (networkAction instanceof AddQuestionAssumptionNetworkAction) {
							AddQuestionAssumptionNetworkAction action = (AddQuestionAssumptionNetworkAction) networkAction;
							// check if link will be added in some transaction, regardless of direction
							if ((action.getQuestionId().equals(childQuestionId) && action.getAssumptionIds().contains(assumptiveQuestionId))
									|| (action.getQuestionId().equals(assumptiveQuestionId) && action.getAssumptionIds().contains(childQuestionId))) {
								// there is a link childQuestionId->assumptiveQuestionId or assumptiveQuestionId->childQuestionId
								hasFound = true;
								break;
							}
						}
					}
					if (!hasFound) {	
						// parent was not found
						throw new InvalidAssumptionException("There is no link between " + assumptiveQuestionId + " and " + childQuestionId + ", and current transaction will not create it either.");
					}
				}
			} else { // parent node exists in the net.
				boolean hasFound = false;
				if (child != null) {
					// check if the arc exists in net
					ProbabilisticNetwork net = getProbabilisticNetwork();
					synchronized (net) {
						if ((net.hasEdge(parent, child) < 0) && (net.hasEdge(child, parent) < 0)) {
							throw new InvalidAssumptionException("There is no link between " + assumptiveQuestionId + " and " + childQuestionId + " in current network.");
						}
					}
					hasFound = true;
				} else {
					// check if new arc will be inserted in current transaction
					synchronized (actions) {
						for (NetworkAction networkAction : actions) {
							if (networkAction instanceof AggregatedQuestionAssumptionNetworkAction) {
								AggregatedQuestionAssumptionNetworkAction action = (AggregatedQuestionAssumptionNetworkAction) networkAction;
								for (Entry<Long, List<Long>> entry : action.getLinks().entrySet()) {
									// check if link will be added in some transaction, regardless of direction
									if ((entry.getKey().equals(childQuestionId) && entry.getValue().contains(assumptiveQuestionId))
											|| (entry.getKey().equals(assumptiveQuestionId) && entry.getValue().contains(childQuestionId))) {
										// there is a link childQuestionId->assumptiveQuestionId or assumptiveQuestionId->childQuestionId
										hasFound = true;
										break;
									}
								}
							} else if (networkAction instanceof AddQuestionAssumptionNetworkAction) {
								AddQuestionAssumptionNetworkAction action = (AddQuestionAssumptionNetworkAction) networkAction;
								// check if link will be added in some transaction, regardless of direction
								if ((action.getQuestionId().equals(childQuestionId) && action.getAssumptionIds().contains(assumptiveQuestionId))
										|| (action.getQuestionId().equals(assumptiveQuestionId) && action.getAssumptionIds().contains(childQuestionId))) {
									// there is a link childQuestionId->assumptiveQuestionId or assumptiveQuestionId->childQuestionId
									hasFound = true;
									break;
								}
							}
						}
					}	// end of synchronized(actions)
				}
				if (!hasFound) {
					throw new InvalidAssumptionException("Question " + childQuestionId + " not created yet, and there is no link between " + assumptiveQuestionId + " and " + childQuestionId);
				}
			}
			
		}
		
		// instantiate the action object for adding the edge
		if (transactionKey == null) {
			transactionKey = this.startNetworkActions();
			this.addNetworkAction(transactionKey, new RemoveQuestionAssumptionNetworkAction(transactionKey, occurredWhen.getTime(), childQuestionId, parentQuestionIds));
			this.commitNetworkActions(transactionKey);
		} else {
			this.addNetworkAction(transactionKey, new RemoveQuestionAssumptionNetworkAction(transactionKey, occurredWhen.getTime(), childQuestionId, parentQuestionIds));
		}
		
		return true;
	}
	
	

	/**
	 * This is a network action for removing direct dependencies from shared BN.
	 * @author Shou Matsumoto
	 * @see MarkovEngineImpl#removeQuestionAssumption(Long, Date, long, List)
	 * @see MarkovEngineImpl#commitNetworkActions(long, boolean)
	 * @see MarkovEngineImpl#getDefaultInferenceAlgorithm()
	 * @see MarkovEngineImpl#getProbabilisticNetwork()
	 * @see AddQuestionAssumptionNetworkAction
	 * @see MarkovEngineImpl#addQuestionAssumption(Long, Date, long, List, List)
	 */
	public class RemoveQuestionAssumptionNetworkAction extends StructureChangeNetworkAction {
		private static final long serialVersionUID = -2353757267946670700L;
		private Long transactionKey;
		private long time;
		private long childQuestionId;
		private List<Long> parentQuestionIds;
		private Map<Long, List<Long>> removedLinks = null;
		private Long whenExecutedFirstTime = null;
		
		/** Default constructor initializing fields */
		public RemoveQuestionAssumptionNetworkAction(Long transactionKey, long time, long childQuestionId, List<Long> parentQuestionIds) {
			this.transactionKey = transactionKey;
			this.time = time;
			this.childQuestionId = childQuestionId;
			this.parentQuestionIds = parentQuestionIds;
		}

		/* (non-Javadoc)
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.StructureChangeNetworkAction#isStructureConstructionAction()
		 */
		public boolean isStructureConstructionAction() {
			return false; // force commitNetworkAction not to reorder this action to the beginning
		}

		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.scicast.NetworkAction#revert()
		 */
		public void revert() throws UnsupportedOperationException {
			if (getRemovedLinks() != null) {
				// revert this action by re-creating all arcs we just removed.
				// TODO figure out a way to revert eventual changes in conditional probabilities caused by arc deletion.
				List<AddQuestionAssumptionNetworkAction> arcActions = new ArrayList<MarkovEngineImpl.AddQuestionAssumptionNetworkAction>(getRemovedLinks().values().size());
				for (Entry<Long, List<Long>> entry : getRemovedLinks().entrySet()) {
					arcActions.add(new AddQuestionAssumptionNetworkAction(Long.MIN_VALUE, System.currentTimeMillis(), entry.getKey(), entry.getValue(), null));
				}
				if (!arcActions.isEmpty()) {
					// add all arcs at once.
					new AggregatedQuestionAssumptionNetworkAction(arcActions).execute();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.scicast.NetworkAction#isTriggerForRebuild()
		 */
		public boolean isTriggerForRebuild() { 
			// if it is configured not to add arcs without reboot, then immediately return that we shall rebuild net
			if (!isToAddArcsWithoutReboot()) {
				return true;
			}
			
			// if it is configured to delete resolved questions, then arcs may be involving deleted questions
//			if (isToDeleteResolvedNode()) {
//				// if arcs are involving resolved questions, nodes may not be present anymore, and in this case we need to reboot
//				if (getResolvedQuestions().containsKey(childQuestionId)) {
//					return true;
//				}
//				for (Long assumptionId : parentQuestionIds) {
//					if (getResolvedQuestions().containsKey(assumptionId)) {
//						return true;
//					}
//				}
//			}
			// or else, check that network was initialized (junction tree was created) already.
			synchronized (getProbabilisticNetwork()) {
				if (getProbabilisticNetwork().getJunctionTree() == null || getProbabilisticNetwork().getJunctionTree().getCliques() == null
						|| getProbabilisticNetwork().getJunctionTree().getCliques().isEmpty()) {
					// network not initialized yet, so need to rebuild
					return true;
				}
			}
			// network is initialized already, and engine is configured to add arcs without reboot, so do not return flag to rebuild
			return false; 
		}

		public boolean isHardEvidenceAction() {return false; }
		public Integer getSettledState() {return null;}
		public Boolean isCorrectiveTrade() {return false;}
		public NetworkAction getCorrectedTrade() {return null; }
		public Long getTransactionKey() {return transactionKey; }
		public Long getQuestionId() { return childQuestionId; }
		public List<Long> getAssumptionIds() { return parentQuestionIds; }
		public List<Integer> getAssumedStates() { return Collections.EMPTY_LIST; }
		public void setWhenExecutedFirstTime(Date whenExecutedFirst) { whenExecutedFirstTime = (whenExecutedFirst==null)?null:whenExecutedFirst.getTime(); /*set to null or convert to long*/ }
		public void setOldValues(List<Float> oldValues) {}
		public void setNewValues(List<Float> newValues) {}
		public Date getWhenCreated() { return new Date(time); }
		public Date getWhenExecutedFirstTime() { return (whenExecutedFirstTime==null)?null:new Date(whenExecutedFirstTime); /*return null or convert to Date*/}
		public List<Float> getOldValues() { return Collections.EMPTY_LIST; }
		public List<Float> getNewValues() {return Collections.EMPTY_LIST;}
		public String getTradeId() { return ""; }
		public Long getUserId() { return null; }
		
		/*
		 * (non-Javadoc)
		 * @see edu.gmu.ace.scicast.MarkovEngineImpl.StructureChangeNetworkAction#execute(unbbayes.prs.bn.ProbabilisticNetwork)
		 */
		public void execute(ProbabilisticNetwork net) {
			
			
			if (!isToAddArcsOnlyToProbabilisticNetwork()) {
				throw new UnsupportedOperationException("Current version cannot remove arcs when assets are managed by Markov Engine");
			}
			
			List<Edge> removedEdges = null;
			synchronized (getDefaultInferenceAlgorithm()) {
				Map<INode, List<INode>> nodes = null;
				ProbabilisticNetwork network = (ProbabilisticNetwork)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator().getNetwork();
				synchronized (network) { // obtain nodes from question IDs;
					// obtain the child node
					INode childNode = network.getNode(""+childQuestionId);
					
					// convert parent ids to nodes
					List<INode> parentNodes = new ArrayList<INode>(parentQuestionIds.size());
					for (Long parentId : parentQuestionIds) {
						if (parentId == null) {
							continue;	// ignore null entries
						}
						// extract the node from net
						Node parentNode = network.getNode(parentId.toString());
						if (parentNode != null) {
							parentNodes.add(parentNode);
						} else {
							// ignore nodes not in net
							Debug.println(getClass(), "Question" + parentId + " was not found in BN.");
						}
					}
					
					if (childNode != null && !parentNodes.isEmpty()) {
						nodes = Collections.singletonMap(childNode, parentNodes);
					} 
				}
				if (nodes != null) {
					removedEdges = getDefaultInferenceAlgorithm().removeEdgesNotConsideringAssets(nodes);
				}
			}
			
			// make sure removedLinks is non-null, because we will fill it
			if (removedLinks == null) {
				// this mapping is used in #revert()
				removedLinks = new HashMap<Long, List<Long>>(removedEdges.size());
//			} else {
//				removedLinks.clear();
			}
			
			// fill removedLinks based on what was returned by the algorithm object. It will be used in #revert()
			if (removedEdges != null) {
				for (Edge removedEdge : removedEdges) {	
					try {
						// extract the ID of the child node
						long childId = Long.parseLong(removedEdge.getDestinationNode().getName());
						List<Long> parents = removedLinks.get(childId);
						if (parents == null) {
							// this is the 1st time that the mapping is used for this child. Initialize.
							parents = new ArrayList<Long>();
							removedLinks.put(childId, parents);
						}
						// extract the id of the parent node
						long parentId = Long.parseLong(removedEdge.getOriginNode().getName());
						// add new parent to this mapping (from child to list of parents)
						parents.add(parentId);
					} catch (NumberFormatException e) {
						// TODO handle cases when there are non-numeric nodes
						Debug.println(getClass(), e.getMessage(), e);
					} catch (NullPointerException e) {
						// TODO handle cases when there are null nodes or null links
						Debug.println(getClass(), e.getMessage(), e);
					}
				}
			} else {
				Debug.println(getClass(), childQuestionId + " and parents " + parentQuestionIds + " were not found in network.");
			}
		}

		/**
		 * @return links removed by {@link #execute(ProbabilisticNetwork)}
		 * @see #revert()
		 */
		public Map<Long, List<Long>> getRemovedLinks() { return removedLinks; }

		/**
		 * @param removedLinks : links removed by {@link #execute(ProbabilisticNetwork)}
		 * @see #revert()
		 */
		public void setRemovedLinks(Map<Long, List<Long>> removedLinks) { this.removedLinks = removedLinks; }
	}
	
	/**
	 * This method just delegates to {@link IncrementalJunctionTreeAlgorithm#isToCreateVirtualNode()}
	 * @return true if soft evidences (trades) must use virtual nodes (which is safer). False otherwise (faster).
	 */
	public boolean isToCreateVirtualNode() {
		if (getDefaultInferenceAlgorithm() == null || getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator() == null
				|| !(getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator() instanceof IncrementalJunctionTreeAlgorithm)) {
			return true;	// default value
		}
		// simply delegate
		return ((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).isToCreateVirtualNode();
	}

	/**
	 * This method just delegates to {@link IncrementalJunctionTreeAlgorithm#setToCreateVirtualNode(boolean)}
	 * @param isToCreateVirtualNode : true if soft evidences (trades) must use virtual nodes (which is safer). False otherwise (faster).
	 */
	public void setToCreateVirtualNode(boolean isToCreateVirtualNode) {
		if (getDefaultInferenceAlgorithm() == null || getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator() == null
				|| !(getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator() instanceof IncrementalJunctionTreeAlgorithm)) {
			return;	// don't do anything
		}
		// simply delegate
		((IncrementalJunctionTreeAlgorithm)getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCreateVirtualNode(isToCreateVirtualNode);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getLinkComplexitySuggestions(java.util.List, java.util.List, int, boolean, boolean)
	 */
	public List<LinkSuggestion> getLinkComplexitySuggestions( List<Long> questionIds1, List<Long> questionIds2, int complexityFactorLimit, 
			boolean isToForceDirection, boolean sortByComplexityFactor) {
		
		// basic assertions
		if (questionIds1 == null || questionIds2 == null || complexityFactorLimit <= 0) {
			return Collections.emptyList();
		}
		
		// check how many pairs of nodes (i.e. how many arcs) were specified completely in the argument
		int numPairs = Math.min(questionIds1.size(), questionIds2.size());
		if (numPairs <= 0) {
			// there is no arc to handle
			return Collections.emptyList();
		}
		
		// this is the list to be returned. Its max size is the number of arcs.
		List<LinkSuggestion> ret = new ArrayList<LinkSuggestion>(numPairs); 
		// fill the list for each pair of nodes
		for (int i = 0; i < numPairs; i++) {
			
			// extract the ids of current pair of questions
			Long parentId = questionIds1.get(i);
			Long childId = questionIds2.get(i);
			
			// create an instance of LinkSuggestion for the arc question1->question2
			LinkSuggestion bestLinkSuggestion = null;	// this object will hold the best suggestion we know so far
			try {
				List<Long> listWith1Id = Collections.singletonList(parentId);	// this is just because the interface expects a list instead of a single value
				bestLinkSuggestion = LinkSugestionImpl.getInstance(
						this.getComplexityFactor(childId, listWith1Id, true, true, true), 	// true*3 := local complexity, using sum of clique sizes, before modification
						this.getComplexityFactor(childId, listWith1Id, true, true, false), 	// true,true,true := local complexity, using sum of clique sizes, after modification
						parentId,
						childId
					);
				// ignore suggestions that has higher complexity than the specified limit
				if (bestLinkSuggestion.getPosteriorComplexity() > complexityFactorLimit) {
					bestLinkSuggestion = null;
				}
			} catch (Exception e) {
				Debug.println(getClass(), "Failed to fill link suggestion: " + parentId + " -> " + childId, e);
			}
			
			
			if (!isToForceDirection) {
				// check if an arc with inverse direction yields better result
				parentId = questionIds2.get(i);
				childId = questionIds1.get(i);
				try {
					// create an instance of LinkSuggestion for the arc with direction question2->question1
					List<Long> listWith1Id = Collections.singletonList(parentId);	// this is just because the interface expects a list instead of a single value
					LinkSuggestion inverseLinkSuggestion = LinkSugestionImpl.getInstance(
							this.getComplexityFactor(childId, listWith1Id, true, true, true), 	// true*3 := local complexity, using sum of clique sizes, before modification
							this.getComplexityFactor(childId, listWith1Id, true, true, false), 	// true,true,true := local complexity, using sum of clique sizes, after modification
							parentId,
							childId
						);
					// ignore suggestions that has higher complexity than the specified limit 
					if (inverseLinkSuggestion.getPosteriorComplexity() > complexityFactorLimit) {
						inverseLinkSuggestion = null;
					}
					// pick the best suggestion
					if (bestLinkSuggestion == null) {
						// if inverse is also worse than the limit, then both directions yields bad links (so keep best link suggestion on null)
						bestLinkSuggestion = inverseLinkSuggestion;
					} else if ( (inverseLinkSuggestion != null) 
							&& inverseLinkSuggestion.getPosteriorComplexity() < bestLinkSuggestion.getPosteriorComplexity()) {
						// the inverse link yields better result than the previous one
						bestLinkSuggestion = inverseLinkSuggestion;
					}	// or else, new suggestion is either null or worse than the old suggestion, so keep the old suggestion 
					
				} catch (Exception e) {
					Debug.println(getClass(), "Failed to fill link suggestion: " + parentId + " -> " + childId, e);
				}
				
			}
			
			// include the link suggestion if it is valid (i.e. better than specified limit)
			if (bestLinkSuggestion != null) {	// if worse than specified limit, it is null. Don't add null values.
				ret.add(bestLinkSuggestion);
			} else {
				Debug.println(getClass(), "Questions " + questionIds1 + " and " + questionIds2 + " cannot be connected without exceeding complexity factor " + complexityFactorLimit);
			}
		}
		
		// sort the results if necessary
		if (sortByComplexityFactor) {
			// just reorder by complexity factor
			Collections.sort(ret, new Comparator<LinkSuggestion>() {
				public int compare(LinkSuggestion o1, LinkSuggestion o2) {
					// this should let the Collections#sort to sort by LinkSuggestion#getPosteriorComplexity()
					int complexity1 = Integer.MAX_VALUE;
					if (o1 != null) {
						complexity1 = o1.getPosteriorComplexity();
					}
					int complexity2 = Integer.MAX_VALUE;
					if (o2 != null) {
						complexity2 = o2.getPosteriorComplexity();
					}
					return complexity1 - complexity2;
				}
			});
		}
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getLinkComplexitySuggestion(java.lang.Long, java.lang.Long, int, boolean)
	 */
	public LinkSuggestion getLinkComplexitySuggestion(Long questionId1, Long questionId2, int complexityFactorLimit, boolean isToForceDirection) {
		// just call the other method, but passing to it a list with only 1 element.
		List<LinkSuggestion> ret = this.getLinkComplexitySuggestions(Collections.singletonList(questionId1), Collections.singletonList(questionId2), complexityFactorLimit, isToForceDirection, false);
		if (ret != null && !ret.isEmpty()) {
			// return anything that was returned (so, just return the 1st element).
			return ret.get(0);
		}
		// if nothing was returned, then return null
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getLinkStrength(java.lang.Long, java.lang.Long)
	 */
	public float getLinkStrength(Long questionId1, Long questionId2) {
		
		// basic assertion
		if (questionId1 == null || questionId2 == null) {
			return Float.NaN;
		}
		
		// return the mutual information between the two nodes
		float mutualInfo = Float.NaN;	// initialize with an invalid value
		
		// always lock the algorithm first before locking the net to avoid deadlock
		AssetAwareInferenceAlgorithm algorithm = getDefaultInferenceAlgorithm();
		synchronized (algorithm) {
			
			// check that we can use the junction tree algorithm to extract the mutual information
			if (!(algorithm.getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm)) {
				throw new IllegalStateException("Unable to calculate mutual information because there was no instance of " + JunctionTreeAlgorithm.class.getName() + ".");
			}
			// extract the algorithm to be used to calculate mutual information
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) algorithm.getProbabilityPropagationDelegator();
			
			// extract the network and the nodes
			ProbabilisticNetwork net = algorithm.getNet();	// this is supposedly the same net handled by the junctionTreeAlgorithm and by the ME
			
			synchronized (net) {
				try {
					// extract the nodes from the network
					ProbabilisticNode node1 = (ProbabilisticNode) net.getNode(Long.toString(questionId1));
					if (node1 == null) {
						Debug.println(getClass(), "Unable to extract node " + questionId1);
						return Float.NaN;
					}
					ProbabilisticNode node2 = (ProbabilisticNode) net.getNode(Long.toString(questionId2));
					if (node2 == null) {
						Debug.println(getClass(), "Unable to extract node " + questionId2);
						return Float.NaN;
					}
					
					mutualInfo = (float) junctionTreeAlgorithm.getMutualInformation(node1, node2);
					
				} catch (Exception e) {
					Debug.println(getClass(), "Unable to get mutual information between questions " + questionId1 + " and " + questionId2, e);
				}
			}
		}
		
		return mutualInfo;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getLinkStrengthList(java.util.List, java.util.List)
	 */
	public List<Float> getLinkStrengthList(List<Long> questionIds1, List<Long> questionIds2) {
		
		// basic assertion
		if (questionIds1 == null || questionIds2 == null) {
			return Collections.EMPTY_LIST;
		}
		
		// this is the list to be filled and returned
		List<Float> ret = new ArrayList<Float>();
		
		// extract the size of the smaller list, because we can only iterate on pairs (one question in each list)
		int numPairs = Math.min(questionIds1.size(), questionIds2.size());
		
		// iterate on each pair of questions
		for (int i = 0; i < numPairs; i++) {
			// extract the pair of question ids
			Long questionId1 = questionIds1.get(i);
			Long questionId2 = questionIds2.get(i);
			
			// ignore null values
			if (questionId1 == null || questionId2 == null) {
				continue;
			}
			
			// simply call the wrapped method
			ret.add(this.getLinkStrength(questionId1, questionId2));
		}
	
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getLinkStrengthAll()
	 */
	public List<LinkStrength> getLinkStrengthAll() {
		
		// prepare the list to be returned
		List<LinkStrength> ret = new ArrayList<LinkStrength>();
		
		// this is the Bayes net model used by the engine
		ProbabilisticNetwork net = getProbabilisticNetwork();
		
		synchronized (net) {
			// iterate on all existing arcs
			for (Edge edge : net.getEdges()) {
				try {
					// extract the ids of the nodes connected by current arc
					Long parentId = Long.parseLong(edge.getOriginNode().getName());
					Long childId = Long.parseLong(edge.getDestinationNode().getName());
					// just add to the ret the link strength
					ret.add(
						// the following should instantiate an object of LinkStrength
						LinkStrengthImpl.getInstance(
							parentId, 									// id of one of the question linked by this arc
							childId, 									// the other question Id
							this.getLinkStrength(parentId, childId)		// the metric for the strength of the link between the 2 questions
						)
					);
				} catch (Exception e) {
					Debug.println(getClass(), "Unable to handle arc " + edge, e);
				}
			}
		}
		
		return ret;
	
	}

	/**
	 * @return the isToUseMaxForSubnetsInLinkSuggestion : if true,
	 * then {@link #getComplexityFactor(ProbabilisticNetwork, Collection)} will
	 * return the max of complexity factors of cliques local to each node one by one, 
	 * instead of considering all the cliques connected to the specified nodes together.
	 * <br/>
	 * <br/>
	 * For instance, if two disconnected nodes are provided (i.e. each node belongs to
	 * different cliques which are disconnected each other, and such cliques contain only 1 node each) and this is false, then
	 * {@link #getComplexityFactor(ProbabilisticNetwork, Collection)} will return
	 * the sum of clique table sizes 
	 */
	public boolean isToUseMaxForSubnetsInLinkSuggestion() {
		return isToUseMaxForSubnetsInLinkSuggestion;
	}

	/**
	 * @param isToUseMaxForSubnetsInLinkSuggestion the isToUseMaxForSubnetsInLinkSuggestion to set
	 */
	public void setToUseMaxForSubnetsInLinkSuggestion(
			boolean isToUseMaxForSubnetsInLinkSuggestion) {
		this.isToUseMaxForSubnetsInLinkSuggestion = isToUseMaxForSubnetsInLinkSuggestion;
	}
	
	/**
	 * @return true if {@link #run()} must disable dynamic JT compilation when {@link LoopyJunctionTree#isLoopy()} is true. False otherwise.
	 * @see IncrementalJunctionTreeAlgorithm#isToCompileNormallyWhenLoopy()
	 */
	public boolean isToCompileNormallyWhenLoopy() {
		AssetAwareInferenceAlgorithm algorithm = getDefaultInferenceAlgorithm();
		synchronized (algorithm) {
			if (algorithm.getProbabilityPropagationDelegator() instanceof IncrementalJunctionTreeAlgorithm) {
				return ((IncrementalJunctionTreeAlgorithm)algorithm.getProbabilityPropagationDelegator()).isToCompileNormallyWhenLoopy();
			} else {
				Debug.println(getClass(), "Unable to find associated instance of " + IncrementalJunctionTreeAlgorithm.class.getName());
			}
		}
		return false;
	}

	/**
	 * @param isToCompileNormallyWhenLoopy : set to true if {@link #run()} must disable dynamic JT compilation when 
	 * {@link LoopyJunctionTree#isLoopy()} is true. Set to false otherwise.
	 * @see IncrementalJunctionTreeAlgorithm#setToCompileNormallyWhenLoopy(boolean)
	 */
	public void setToCompileNormallyWhenLoopy(boolean isToCompileNormallyWhenLoopy) {
		AssetAwareInferenceAlgorithm algorithm = getDefaultInferenceAlgorithm();
		synchronized (algorithm) {
			if (algorithm.getProbabilityPropagationDelegator() instanceof IncrementalJunctionTreeAlgorithm) {
				((IncrementalJunctionTreeAlgorithm)algorithm.getProbabilityPropagationDelegator()).setToCompileNormallyWhenLoopy(isToCompileNormallyWhenLoopy);
			} else {
				Debug.println(getClass(), "Unable to find associated instance of " + IncrementalJunctionTreeAlgorithm.class.getName());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.gmu.ace.scicast.MarkovEngineInterface#getLinkStrengthComplexityAll()
	 */
	public List<LinkStrengthComplexity> getLinkStrengthComplexityAll() {

		
		// prepare the list to be returned
		List<LinkStrengthComplexity> ret = new ArrayList<LinkStrengthComplexity>();
		
		// this is the Bayes net model used by the engine
		ProbabilisticNetwork net = getProbabilisticNetwork();
		
		synchronized (net) {
			// iterate on all existing arcs
			for (Edge edge : net.getEdges()) {
				try {
					// extract the ids of the nodes connected by current arc
					Long parentId = Long.parseLong(edge.getOriginNode().getName());
					Long childId = Long.parseLong(edge.getDestinationNode().getName());
					// just add to the ret the link strength
					ret.add(
						// the following should instantiate an object of LinkStrength
						LinkStrengthComplexityImpl.getInstance(
							parentId, 									// id of one of the question linked by this arc
							childId, 									// the other question Id
							this.getLinkStrength(parentId, childId),	// the metric for the strength of the link between the 2 questions
							this.getComplexityFactor(childId, Collections.singletonList(parentId))	// the complexity factor you'll get if you drop this link
						)
					);
				} catch (Exception e) {
					Debug.println(getClass(), "Unable to handle arc " + edge, e);
				}
			}
		}
		
		return ret;
	
	
	}

	/**
	 * If false, dynamic/incremental junction tree compilation will be disabled when value trees are used
	 * @return the isToUseDynamicJunctionTreeWithValueTrees
	 * @see AddValueTreeQuestionNetworkAction#execute(ProbabilisticNetwork, boolean)
	 */
	public boolean isToUseDynamicJunctionTreeWithValueTrees() {
		return isToUseDynamicJunctionTreeWithValueTrees;
	}

	/**
	 * If false, dynamic/incremental junction tree compilation will be disabled when value trees are used
	 * @param isToUseDynamicJunctionTreeWithValueTrees the isToUseDynamicJunctionTreeWithValueTrees to set
	 * @see AddValueTreeQuestionNetworkAction#execute(ProbabilisticNetwork, boolean)
	 */
	public void setToUseDynamicJunctionTreeWithValueTrees(
			boolean isToUseDynamicJunctionTreeWithValueTrees) {
		this.isToUseDynamicJunctionTreeWithValueTrees = isToUseDynamicJunctionTreeWithValueTrees;
	}

	/**
	 * @return the singleExistingArcComplexityCache : a cache for {@link #getComplexityFactors(Map, boolean, boolean)} for cases when
	 * the argument is a single (1) existing arc. If {@link #getComplexityFactors(Map, boolean, boolean)} is called with a single entry,
	 * and that entry is an existing arc in {@link #getProbabilisticNetwork()}, then this cache is used.
	 * The key is a string representation of an arc.
	 * @see #initialize()
	 * @see ResolveQuestionNetworkAction
	 * @see ResolveSetOfQuestionsNetworkAction
	 * @see ResolveValueTreeNetworkAction
	 * @see AddQuestionAssumptionNetworkAction
	 * @see AddValueTreeQuestionNetworkAction
	 * @see AddQuestionNetworkAction
	 * @see RemoveQuestionAssumptionNetworkAction
	 * @see #importState(String)
	 */
	public Map<String,Map<String,Double>> getSingleExistingArcComplexityCache() {
		return singleExistingArcComplexityCache;
	}

	/**
	 * @param singleExistingArcComplexityCache : : a cache for {@link #getComplexityFactors(Map, boolean, boolean)} for cases when
	 * the argument is a single (1) existing arc. If {@link #getComplexityFactors(Map, boolean, boolean)} is called with a single entry,
	 * and that entry is an existing arc in {@link #getProbabilisticNetwork()}, then this cache is used.
	 * @see #initialize()
	 * @see ResolveQuestionNetworkAction
	 * @see ResolveSetOfQuestionsNetworkAction
	 * @see ResolveValueTreeNetworkAction
	 * @see AddQuestionAssumptionNetworkAction
	 * @see AddValueTreeQuestionNetworkAction
	 * @see AddQuestionNetworkAction
	 * @see RemoveQuestionAssumptionNetworkAction
	 * @see #importState(String)
	 */
	public void setSingleExistingArcComplexityCache(
			Map<String,Map<String,Double>> singleExistingArcComplexityCache) {
		this.singleExistingArcComplexityCache = singleExistingArcComplexityCache;
	}

	/**
	 * @return the isToAggregateAddQuestionAction : if true, a sequence of {@link AddQuestionAssumptionNetworkAction} 
	 * will be aggregated into a single call of {@link AggregatedQuestionAssumptionNetworkAction}.
	 */
	public boolean isToAggregateAddQuestionAction() {
		return isToAggregateAddQuestionAction;
	}

	/**
	 * @param isToAggregateAddQuestionAction the isToAggregateAddQuestionAction to set: if true, a sequence of {@link AddQuestionAssumptionNetworkAction} 
	 * will be aggregated into a single call of {@link AggregatedQuestionAssumptionNetworkAction}.
	 */
	public void setToAggregateAddQuestionAction(
			boolean isToAggregateAddQuestionAction) {
		this.isToAggregateAddQuestionAction = isToAggregateAddQuestionAction;
	}

	/**
	 * @return the isToLoadApplicationPropertyFile : If false, then {@link #loadApplicationPropertyFile()} will be disabled. The default is true.
	 */
	public boolean isToLoadApplicationPropertyFile() {
		return isToLoadApplicationPropertyFile;
	}

	/**
	 * @param isToLoadApplicationPropertyFile the isToLoadApplicationPropertyFile to set : If false, then {@link #loadApplicationPropertyFile()} will be disabled. The default is true.
	 */
	public void setToLoadApplicationPropertyFile(
			boolean isToLoadApplicationPropertyFile) {
		this.isToLoadApplicationPropertyFile = isToLoadApplicationPropertyFile;
	}

	
	
	
	
//	/**
//	 * Exception thrown when some state at 0% probability is attempted to be changed to non 0 probability.
//	 * This usually an error, because a bayes rule cannot make impossible states to become possible.
//	 * @author Shou Matsumoto
//	 *
//	 */
//	class ChangeZeroProbabilityStateException extends RuntimeException {
//		public ChangeZeroProbabilityStateException() {}
//		public ChangeZeroProbabilityStateException(String message) { super(message); }
//		public ChangeZeroProbabilityStateException(Throwable cause) { super(cause);}
//		public ChangeZeroProbabilityStateException(String message, Throwable cause) { super(message, cause); }
//	}

}
