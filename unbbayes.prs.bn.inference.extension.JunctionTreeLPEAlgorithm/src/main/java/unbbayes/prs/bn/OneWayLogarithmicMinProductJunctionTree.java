/**
 * 
 */
package unbbayes.prs.bn;


/**
 * This is a specialization of {@link LogarithmicMinProductJunctionTree}
 * whose the {@link #consistency()}
 * will only propagate evidences from the leaves
 * to the root.
 * This is useful if we want to calculate
 * min-values without calculating min-states.
 * @author Shou Matsumoto
 * @see unbbayes.prs.bn.inference.extension.AssetPropagationInferenceAlgorithm
 */
public class OneWayLogarithmicMinProductJunctionTree extends
		LogarithmicMinProductJunctionTree {

	private static final long serialVersionUID = -1602903677775231765L;

	/**
	 * This is a specialization of {@link LogarithmicMinProductJunctionTree}
	 * whose the {@link #consistency()}
	 * will only propagate evidences from the leaves
	 * to the root.
	 * This is useful if we want to calculate
	 * min-values without calculating min-states.
	 */
	public OneWayLogarithmicMinProductJunctionTree() {
		super();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#consistency()
	 */
	@Override
	public void consistency() throws Exception {
		collectEvidence(getCliques().get(0), true);
		// do not call distributeEvidences(raiz);
	}

}
