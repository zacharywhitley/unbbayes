package unbbayes.prs.bn;

import java.util.List;

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

	public abstract void addSeparator(Separator sep);

	public abstract int getSeparatorsSize();

	public abstract Separator getSeparatorAt(int index);

	/**
	 *@return    list with associated cliques
	 */
	public abstract List<Clique> getCliques();

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

}