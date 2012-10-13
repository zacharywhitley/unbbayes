package unbbayes.prs.bn;

import java.util.Collection;
import java.util.List;

import unbbayes.prs.INode;

/**
 * This interface represents a junction tree for BN.
 * @see JunctionTree for default implementation
 * @author Shou Matsumoto
 *
 */
public interface IJunctionTree {

	/**
	 * @return estimated total probability
	 */
	public abstract float getN();

	/**
	 * Stores the separator in junction three
	 * @param sep
	 */
	public abstract void addSeparator(Separator sep);
	
	/**
	 * Deletes a separator from junction tree and clears all indexes
	 * @param sep
	 */
	public abstract void removeSeparator(Separator sep);
	
	/**
	 * This method returns cliques containing all specified nodes
	 * @param nodes : only cliques containing these nodes will be returned.
	 * @param maxCount : no more than this quantity of cliques will be returned.
	 * @return list of cliques
	 */
	public List<Clique> getCliquesContainingAllNodes(Collection<INode> nodes, int maxCount);

	/**
	 * This method returns separators containing all specified nodes
	 * @param nodes : only cliques containing these nodes will be returned.
	 * @param maxCount : no more than this quantity of cliques will be returned.
	 * @return list of separators
	 */
	public List<Separator> getSeparatorsContainingAllNodes(Collection<INode> nodes, int maxCount);
	
//	/**
//	 * The current size of the collection of separators stored in this
//	 * junction tree.
//	 * @return
//	 */
//	public abstract int getSeparatorsSize();

//	/**
//	 * Obtains the separator in a given index.
//	 * @param index
//	 * @return
//	 */
//	public abstract Separator getSeparatorAt(int index);
	
	/**
	 *@return    list with associated cliques
	 */
	public abstract List<Clique> getCliques();
	
	/**
	 * @return list with associated separators
	 */
	public abstract Collection<Separator> getSeparators();

	/**
	 *  Verifies global consistency.
	 *  It applies the "collect" (propagate messages from leaves to root clique) and then "distribute" (propagate
	 *  messages from root clique to leaves).
	 */
	public abstract void consistency() throws Exception;

	/**
	 *  Initialize belief
	 */
	public abstract void initBeliefs() throws Exception;

	/**
	 * Returns the Separator associated with these Cliques, assuming no orientation.
	 *
	 * @param clique1 Clique 1
	 * @param clique2 Clique 2
	 * @return The separator associated with these Cliques or null if this separator doesn't exist.
	 */
	public abstract Separator getSeparator(Clique clique1, Clique clique2);
	
	/**
	 * Initializes the probability potential of a random variable. Particularly, it
	 * initializes potentials of either a {@link Separator} or a {@link Clique}
	 * @param rv
	 */
	public void initBelief(IRandomVariable rv);

}