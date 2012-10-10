package unbbayes.util.extension.bn.inference;


/**
 * This is a common interface for inference algorithms which can initialize
 * the values in {@link unbbayes.prs.bn.IRandomVariable#getInternalIdentificator()}.
 * For instance, in order to change such values during
 * {@link IInferenceAlgorithmListener#onBeforeRun(IInferenceAlgorithm)}
 * or {@link IInferenceAlgorithmListener#onAfterRun(IInferenceAlgorithm)}.
 * @author Shou Matsumoto
 *
 */
public interface IRandomVariableAwareInferenceAlgorithm extends IInferenceAlgorithm {

	/**
	 * Initializes the values of {@link unbbayes.prs.bn.IRandomVariable#getInternalIdentificator()}
	 * of the nodes in {@link #getNetwork()}. 
	 * By default, the internal id will be the index of the node within the network.
	 * @see IInferenceAlgorithmListener#onBeforeRun(IInferenceAlgorithm)
	 * @see #run()
	 */
	public void initInternalIdentificators();
}
