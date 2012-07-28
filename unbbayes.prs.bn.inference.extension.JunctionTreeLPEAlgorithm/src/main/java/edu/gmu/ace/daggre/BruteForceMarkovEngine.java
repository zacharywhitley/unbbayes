/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.gmu.ace.daggre.MarkovEngineImpl.InexistingQuestionException;

import unbbayes.prs.INode;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm.JointPotentialTable;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class BruteForceMarkovEngine extends MarkovEngineImpl {

	protected BruteForceMarkovEngine() {
		super();
		this.setAssetAwareInferenceAlgorithmBuilder(new IAssetAwareInferenceAlgorithmBuilder() {
			public AssetAwareInferenceAlgorithm build(IInferenceAlgorithm probDelegator, float initQValues) {
				return (AssetAwareInferenceAlgorithm) BruteForceAssetAwareInferenceAlgorithm.getInstance(probDelegator, initQValues);
			}
		});
		this.initialize();
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
		return ret;
	}
	
	public class BruteForceAddTradeNetworkAction extends AddTradeNetworkAction {
		private JointPotentialTable previousJointQTable;
		public BruteForceAddTradeNetworkAction(long transactionKey,
				Date occurredWhen, String tradeKey, long userId,
				long questionId, List<Float> newValues,
				List<Long> assumptionIds, List<Integer> assumedStates,
				boolean allowNegative) {
			super(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues,
					assumptionIds, assumedStates, allowNegative);
		}

		public void execute() {
			// extract user's asset network from user ID
			BruteForceAssetAwareInferenceAlgorithm algorithm = null;
			try {
				algorithm = (BruteForceAssetAwareInferenceAlgorithm) getAlgorithmAndAssetNetFromUserID(getUserId());
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not extract delta from user " + getUserId() + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			if (algorithm == null) {
				throw new RuntimeException("Could not extract delta from user " + getUserId() + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			}
			
			synchronized (getProbabilisticNetwork()) {
				synchronized (algorithm.getAssetNetwork()) {
					if (!algorithm.getNetwork().equals(getProbabilisticNetwork())) {
						// this should never happen, but some desync may happen.
						Debug.println(getClass(), "[Warning] desync of network detected.");
						algorithm.setNetwork(getProbabilisticNetwork());
					}
					// do trade. Since algorithm is linked to actual networks, changes will affect the actual networks
					// 2nd boolean == true := overwrite assumptionIds and assumedStates when necessary
					setOldValues(executeTrade(getQuestionId(), getNewValues(), getAssumptionIds(), getAssumedStates(), isAllowNegative(), algorithm, true, false));
					// backup the previous delta so that we can revert this trade
					previousJointQTable = (JointPotentialTable) algorithm.getJointQTable().clone();
				}
			}
		}
		/** 
		 * As discussed with Dr. Robin Hanson (<a href="mailto:rhanson@gmu.edu">rhanson@gmu.edu</a>) on
		 * 07/29/2012 during the DAGGRE algorithm meeting, the {@link MarkovEngineInterface#revertTrade(long, Date, Date, Long)}
		 * should only set the user's delta into the point prior to when {@link #execute()} was called.
		 * CAUTION: this method is not actually reverting a trade. It is setting
		 * the asset tables to values prior to the execution of {@link #execute()}.
		 */
		public void revert() throws UnsupportedOperationException {
			// extract user's asset network from user ID
			BruteForceAssetAwareInferenceAlgorithm algorithm = null;
			try {
				algorithm = (BruteForceAssetAwareInferenceAlgorithm) getAlgorithmAndAssetNetFromUserID(getUserId());
			} catch (InvalidParentException e) {
				throw new RuntimeException("Could not extract delta from user " + getUserId() + ". Perhaps the Bayesian network became invalid because of a structure change previously committed.");
			}
			if (algorithm == null) {
				throw new RuntimeException("Could not extract delta from user " + getUserId() + ". You may be using old or incompatible version of Markov Engine or UnBBayes.");
			}
			synchronized (algorithm.getAssetNetwork()) {
				algorithm.setJointQTable(previousJointQTable);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, java.util.Date, java.lang.String, long, long, java.util.List, java.util.List, java.util.List, boolean)
	 */
	@Override
	public List<Float> addTrade(long transactionKey, Date occurredWhen,
			String tradeKey, long userId, long questionId,
			List<Float> newValues, List<Long> assumptionIds,
			List<Integer> assumedStates, boolean allowNegative)
			throws IllegalArgumentException {

		// initial assertions
		if (occurredWhen == null) {
			throw new IllegalArgumentException("Argument \"occurredWhen\" is mandatory.");
		}
		if (this.getNetworkActionsMap().get(transactionKey) == null) {
			// startNetworkAction should have been called.
			throw new IllegalArgumentException("Invalid transaction key: " + transactionKey);
		}
		
		
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
			for (Float asset : ret) {
				if (asset <= 0) {
					return null;
				}
			}
		}
		
		// NOTE: preview trade is performed *before* the insertion of a new action into the transaction, 
		// because we only want the transaction to be altered if the preview trade has returned successfully.
		
		// instantiate the action object for adding trade
		AddTradeNetworkAction newAction = new BruteForceAddTradeNetworkAction(transactionKey, occurredWhen, tradeKey, userId, questionId, newValues, assumptionIds, assumedStates, allowNegative);
		
		this.addNetworkAction(transactionKey, newAction);
		
		// return the previewed asset values
		return ret;
	}

//	/* (non-Javadoc)
//	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getAssetsIfStates(long, java.util.List, java.util.List, unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm, boolean)
//	 */
//	@Override
//	protected List<Float> getAssetsIfStates(long questionId,
//			List<Long> assumptionIds, List<Integer> assumedStates,
//			AssetAwareInferenceAlgorithm algorithm,
//			boolean isToReturnQValuesInsteadOfAssets)
//			throws IllegalArgumentException {
//		
//		// basic assertion
//		if (algorithm == null) {
//			throw new NullPointerException("AssetAwareInferenceAlgorithm cannot be null");
//		}
//		PotentialTable assetTable = null;	// asset tables (clique table containing delta) are instances of potential tables
//		synchronized (algorithm.getAssetNetwork()) {
//			AssetNode mainNode = (AssetNode) algorithm.getAssetNetwork().getNode(Long.toString(questionId));
//			if (mainNode == null) {
//				throw new InexistingQuestionException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork(), questionId);
//			}
//			List<INode> parentNodes = new ArrayList<INode>();
//			if (assumptionIds != null) {
//				for (Long id : assumptionIds) {
//					INode node = algorithm.getAssetNetwork().getNode(Long.toString(id));
//					if (node == null) {
//						throw new InexistingQuestionException("Question " + questionId + " not found in asset structure of user " + algorithm.getAssetNetwork(), questionId);
//					}
//					parentNodes.add(node);
//				}
//			}
//			assetTable = ((BruteForceAssetAwareInferenceAlgorithm)algorithm).getJointQTable();
//		}
//		
//		// convert cpt to a list of float, given assumedStates.
//		// TODO change the way it sum-out/min-out/max-out the unspecified states in the clique potential
//		List<Float> ret = new ArrayList<Float>(assetTable.tableSize());
//		for (int i = 0; i < assetTable.tableSize(); i++) {
//			assetTable.getMultidimensionalCoord(i);
//			asdf
//		}
//		return ret;
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see edu.gmu.ace.daggre.MarkovEngineImpl#getProbLists(java.util.List, java.util.List, java.util.List, boolean)
//	 */
//	@Override
//	public Map<Long, List<Float>> getProbLists(List<Long> questionIds,
//			List<Long> assumptionIds, List<Integer> assumedStates,
//			boolean isToNormalize) throws IllegalArgumentException {
//		// TODO Auto-generated method stub
//		return super.getProbLists(questionIds, assumptionIds, assumedStates,
//				isToNormalize);
//	}
	

}
