/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.HashMap;
import java.util.Map;

import edu.gmu.ace.daggre.MarkovEngineImpl.ProbabilityAndAssetTablesMemento;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.CPTBruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
import unbbayes.prs.bn.inference.extension.BruteForceAssetAwareInferenceAlgorithm.JointPotentialTable;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This is an extension of {@link BruteForceMarkovEngine},
 * but it uses {@link CPTBruteForceAssetAwareInferenceAlgorithm}
 * as the main algorithm.
 * @author Shou Matsumoto
 *
 */
public class CPTBruteForceMarkovEngine extends BruteForceMarkovEngine {

	protected CPTBruteForceMarkovEngine() {
		super();
		this.setDefaultInitialAssetTableValue(1);	// brute force is still using q-values
		this.setAssetAwareInferenceAlgorithmBuilder(new IAssetAwareInferenceAlgorithmBuilder() {
			public AssetAwareInferenceAlgorithm build(IInferenceAlgorithm probDelegator, float initQValues) {
				return (AssetAwareInferenceAlgorithm) CPTBruteForceAssetAwareInferenceAlgorithm.getInstance(probDelegator, initQValues);
			}
		});
		this.initialize();
	}
	
	/**
	 * Default constructor method
	 * @return
	 */
	public static CPTBruteForceMarkovEngine getInstance() {
		CPTBruteForceMarkovEngine ret = getInstance((float) Math.E, 100f, 1000);
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
	public static CPTBruteForceMarkovEngine getInstance(float logBase, float currencyConstant, float initialUserAssets) {
		return CPTBruteForceMarkovEngine.getInstance(logBase, currencyConstant, initialUserAssets, false);
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
	public static CPTBruteForceMarkovEngine getInstance(float logBase, float currencyConstant, float initialUserAssets, boolean isToThrowExceptionOnInvalidAssumptions) {
		CPTBruteForceMarkovEngine ret = new CPTBruteForceMarkovEngine();
		ret.setCurrentCurrencyConstant(currencyConstant);
		ret.setCurrentLogBase(logBase);
		ret.setDefaultInitialAssetTableValue((float) ret.getQValuesFromScore(initialUserAssets));
		ret.setToThrowExceptionOnInvalidAssumptions(isToThrowExceptionOnInvalidAssumptions);
		ret.setToUseQValues(true);
		return ret;
	}

	
	/**
	 * This is an extension of {@link ProbabilityAndAssetTablesMemento}
	 * which also stores current joint tables.
	 * @author Shou Matsumoto
	 * @see ProbabilityAndAssetTablesMemento
	 */
	public class CPTMemento extends JointProbabilityAndAssetTablesMemento {

		/**
		 * Stores probability tables of cliques/separators of {@link MarkovEngineImpl#getProbabilisticNetwork()},
		 * asset tables of {@link MarkovEngineImpl#getUserToAssetAwareAlgorithmMap()},
		 * joint probability table, and joint asset tables.
		 */
		public CPTMemento() {
			super();
			
		}
		/**
		 * @see edu.gmu.ace.daggre.MarkovEngineImpl.ProbabilityAndAssetTablesMemento#restore()
		 */
		protected void restore() {
			super.restore();
			synchronized (getUserToAssetAwareAlgorithmMap()) {
				for (AssetAwareInferenceAlgorithm algorithm : getUserToAssetAwareAlgorithmMap().values()) {
					((CPTBruteForceAssetAwareInferenceAlgorithm)algorithm).updateCPT();
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
	 * @see edu.gmu.ace.daggre.BruteForceMarkovEngine#getMemento()
	 */
	public ProbabilityAndAssetTablesMemento getMemento() {
		return new CPTMemento();
	}
	
}
