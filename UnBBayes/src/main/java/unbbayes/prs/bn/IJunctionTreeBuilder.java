package unbbayes.prs.bn;

import unbbayes.prs.Graph;

/**
 * Creates an instance of {@link IJunctionTree}.
 * Objects of this class should be used by {@link ProbabilisticNetwork#compile()}
 * to instantiate the correct default Junction Tree to compile a BN.
 * @author Shou Matsumoto
 *
 */
public interface IJunctionTreeBuilder {

	/**
	 * @return instance of {@link IJunctionTree}
	 * @param network : the junction tree is going to represent this network.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public abstract IJunctionTree buildJunctionTree(Graph network)  
				throws InstantiationException, IllegalAccessException;

}