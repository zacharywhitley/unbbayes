/**
 * 
 */
package unbbayes.prs.bn;

import java.util.List;

/**
 * This is used in {@link IncrementalJunctionTreeAlgorithm#splitClique(Clique, LoopyJunctionTree, int)} in order to 
 * split large cliques accordingly to some criteria.
 * @author Shou Matsumoto
 */
public interface ICliqueSplitter {

	/**
	 * Creates smaller cliques by splitting a clique provided in the argument.
	 * Implementations can customize the way and the results of the splitting process.
	 * It is responsible for splitting only, and implementations don't have to integrate the resulting
	 * cliques to junction tree.
	 * @param largeClique: clique to split
	 * @param jt : junction tree the clique belongs to. This is just for reference, and implementations may not 
	 * integrate the resulting cliques to it automatically.
	 * @param desiredSize : the desired size of the resulting clique
	 * @return : a list of cliques obtained by splitting the provided clique.
	 */
	public List<Clique> splitClique(Clique cliqueToSplit, IJunctionTree jt, int desiredSize);
	
}
