/**
 * 
 */
package edu.gmu.ace.daggre;

import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.CPTBruteForceAssetAwareInferenceAlgorithm;
import unbbayes.prs.bn.inference.extension.IAssetAwareInferenceAlgorithmBuilder;
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

}
