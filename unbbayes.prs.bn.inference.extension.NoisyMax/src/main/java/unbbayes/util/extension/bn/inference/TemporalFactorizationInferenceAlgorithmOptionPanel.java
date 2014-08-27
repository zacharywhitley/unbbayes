/**
 * 
 */
package unbbayes.util.extension.bn.inference;


/**
 * @author Shou Matsumoto
 *
 */
public class TemporalFactorizationInferenceAlgorithmOptionPanel extends InferenceAlgorithmOptionPanel {

	private static final long serialVersionUID = 320504845349281555L;

	/**
	 * Default constructor must be public so that plugin infrastructure
	 * can instantiate it easily
	 */
	public TemporalFactorizationInferenceAlgorithmOptionPanel() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#getInferenceAlgorithm()
	 */
	public IInferenceAlgorithm getInferenceAlgorithm() {
		ICIFactorizationJunctionTreeAlgorithm algorithm = new ICIFactorizationJunctionTreeAlgorithm();
		algorithm.setOptionPanel(this);
		return algorithm;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#revertChanges()
	 */
	public void revertChanges() {
		// TODO Auto-generated method stub

	}

}