/**
 * 
 */
package unbbayes.util.extension.bn.inference;

/**
 * This is a listener for actions in the inference algorithm.
 * @author Shou Matsumoto
 *
 */
public interface IInferenceAlgorithmListener {

	/**
	 * This method is called before {@link IInferenceAlgorithm#run()}
	 * @param algorithm : algorithm calling {@link IInferenceAlgorithm#run()}
	 */
	public void onBeforeRun(IInferenceAlgorithm algorithm);

	/**
	 * This method is called after {@link IInferenceAlgorithm#run()}
	 * @param algorithm : algorithm calling {@link IInferenceAlgorithm#run()}
	 */
	public void onAfterRun(IInferenceAlgorithm algorithm);
	
	/**
	 * This method is called before {@link IInferenceAlgorithm#reset()}
	 * @param algorithm : algorithm calling {@link IInferenceAlgorithm#reset()}
	 */
	public void onBeforeReset(IInferenceAlgorithm algorithm);
	
	/**
	 * This method is called after {@link IInferenceAlgorithm#reset()}
	 * @param algorithm : algorithm calling {@link IInferenceAlgorithm#reset()}
	 */
	public void onAfterReset(IInferenceAlgorithm algorithm);
	
	/**
	 * This method is called before {@link IInferenceAlgorithm#propagate()}
	 * @param algorithm : algorithm calling {@link IInferenceAlgorithm#propagate()}
	 */
	public void onBeforePropagate(IInferenceAlgorithm algorithm);
	

	/**
	 * This method is called after {@link IInferenceAlgorithm#propagate()}
	 * @param algorithm : algorithm calling {@link IInferenceAlgorithm#propagate()}
	 */
	public void onAfterPropagate(IInferenceAlgorithm algorithm);
	
}
