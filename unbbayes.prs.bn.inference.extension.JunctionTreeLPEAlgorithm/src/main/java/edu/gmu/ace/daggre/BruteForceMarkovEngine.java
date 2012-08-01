/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm.JointPotentialTable;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class BruteForceMarkovEngine extends MarkovEngineImpl {

	protected BruteForceMarkovEngine() {
		super();
		this.setDefaultInitialAssetTableValue(1);	// brute force is still using q-values
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
						if (jointQTable.getValue(i) < min) {
							min = jointQTable.getValue(i);
						}
					}
				}
				ret.add(getScoreFromQValues(min));	
			}
			
		}
		
		return ret;
	}
	
	

}
