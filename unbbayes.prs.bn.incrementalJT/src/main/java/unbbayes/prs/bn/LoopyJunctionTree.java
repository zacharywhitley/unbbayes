/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;

/**
 * This is an extension of {@link JunctionTree} which may contain
 * cliques with loops (i.e. it's not a tree structure anymore).
 * When loops are present, 2 propagations (from leaf cliques to root clique and then from root to leaves)
 * does not guarantee convergence, so a loopy belief propagation will be used instead.
 * If the junction tree does not have loops, then this class must behave the same way as in the {@link JunctionTree}.
 * <br/>
 * <br/>
 * Note: this class is not thread-safe.
 * @author Shou Matsumoto
 *
 */
public class LoopyJunctionTree extends JunctionTree {
	private static final long serialVersionUID = 7365295192723077479L;
	
	private boolean isLoopy = false;

	private int maxLoopyBPIteration = 65535;

	private Boolean isModified = false;
	
	private float probErrorMargin = (float) 1e-6;

	private Map<Clique, List<Clique>> cliqueParentMap;

	private long maxLoopyBPTimeMillis = 5000;
	
	private static float[] probBefore = null;
	
//	private Boolean isToUseStaticProbArray = true;

	/**
	 * Default constructor with no fields.
	 */
	public LoopyJunctionTree() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#initConsistency()
	 */
	protected void initConsistency() throws Exception {
		
		this.setModified(false); // just to make sure the modification of the last call won't affect current call.

		if (isLoopy()) {
			long startTime = System.currentTimeMillis();
			int numIterations = 0;
			for (; numIterations < maxLoopyBPIteration; numIterations++) {
				this.setModified(false);
				super.initConsistency();
				if (!isModified()							// there were no considerable change in probability in last loop
						|| (System.currentTimeMillis() - startTime) > maxLoopyBPTimeMillis) {	// time exceeded limit
					if (isModified()) {
						Debug.println(getClass(), "Loopy BP interrupted in " + (System.currentTimeMillis() - startTime) + " ms, because exceeded time limit: " + maxLoopyBPTimeMillis + " ms. ");
					} else {
						Debug.println(getClass(), "Loopy BP converged in " + (System.currentTimeMillis() - startTime) + " ms.");
					}
					break;
				}
			}
			Debug.println(getClass(), "Loopy BP finished in " + numIterations + " iterations.");
		} else {
			// a single loop is enough for convergence
			super.initConsistency();
		}
	
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#consistency(unbbayes.prs.bn.Clique, boolean)
	 */
	public void consistency(Clique rootClique, boolean isToContinueOnEmptySep) throws Exception {
		if (isLoopy()) {
			long startTime = System.currentTimeMillis();
			int numIterations = 0;
			for (; numIterations < maxLoopyBPIteration; numIterations++) {
				this.setModified(false);
				super.consistency(rootClique, isToContinueOnEmptySep);
				if (!isModified()							// there were no considerable change in probability in last loop
						|| (System.currentTimeMillis() - startTime) > maxLoopyBPTimeMillis) {	// time exceeded limit
					if (isModified()) {
						Debug.println(getClass(), "Loopy BP interrupted in " + (System.currentTimeMillis() - startTime) + " ms, because exceeded time limit: " + maxLoopyBPTimeMillis + " ms. ");
					} else {
						Debug.println(getClass(), "Loopy BP converged in " + (System.currentTimeMillis() - startTime) + " ms.");
					}
					break;
				}
			}
			Debug.println(getClass(), "Loopy BP finished in " + numIterations + " iterations.");
		} else {
			// a single loop is enough for convergence
			super.consistency(rootClique, isToContinueOnEmptySep);
		}
	}
	
	
	

	/**
	 * TODO this is not thread-safe.
	 * @see unbbayes.prs.bn.JunctionTree#absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	protected void absorb(Clique clique1, Clique clique2) {

		// only check for modification if we are doing loopy belief propagation and there were no modification yet
		if (isLoopy && !isModified) {
			// extract the clique table before propagation
			PotentialTable cliqueTable = clique1.getProbabilityFunction();
			int tableSize = cliqueTable.tableSize();
			
			// backup clique potentials before absorbing cliques
			float[] probBefore = LoopyJunctionTree.probBefore;	// we may use a static array if flag is on
			if (isToUseSingletonListsInAbsorb()) { 
				// allocate more space if necessary
				if (probBefore == null || tableSize > probBefore.length) {
					LoopyJunctionTree.probBefore = new float[tableSize];
					probBefore = LoopyJunctionTree.probBefore;
				}
			} else {
				// don't use static array. Use a new instance instead
				probBefore = new float[tableSize];
			}
			System.arraycopy(cliqueTable.dataPT.data, 0, probBefore, 0, tableSize);
			
			// absorb
			super.absorb(clique1, clique2);
			
			// check if there were changes in clique potential.
//			cliqueTable = clique1.getProbabilityFunction().getTemporaryClone();	// there may be changes of instances?
//			cliqueTable.normalize();	// TODO check if it is really necessary to compare with normalized table
			for (int i = 0; i < tableSize; i++) {
				if (Math.abs(cliqueTable.getValue(i) - probBefore[i]) > probErrorMargin) {
					isModified = true;
					return;
				}
			}
		} else {
			super.absorb(clique1, clique2);
		}
		
	}
	
	
	/**
	 * Extracts the parent of the provided clique.
	 * This is useful when the {@link IJunctionTree} structure
	 * allows multiple parents (e.g. loopy clique structures).
	 * This will access {@link #getCliqueParentMap()}.
	 * @param clique : parents of this clique will be returned.
	 * @return parents of the clique. In a tree structure, this will contain only 1 element,
	 * but in a structure with loops, this can return multiple elements.
	 * @see unbbayes.prs.bn.JunctionTree#getParents(unbbayes.prs.bn.Clique)
	 * @see #setCliqueParentMap(Map)
	 * @see #initParentMapping()
	 * @see #clearParents(Clique)
	 * @see #addParent(Clique, Clique)
	 * @see #removeParent(Clique, Clique)
	 */
	public List<Clique> getParents(Clique clique) {
		Map<Clique, List<Clique>> parentMap = getCliqueParentMap();
		if (parentMap == null) {
//			return super.getParents(clique);
			initParentMapping();
			parentMap = getCliqueParentMap();
		}
		List<Clique> ret = parentMap.get(clique);
		if (( ret == null || ret.isEmpty() )
				&& (clique.getParent() != null)) {
			Debug.println(getClass(), 
					"Mapping of parents of clique " + clique  
					+ " is desynchronized. Forcing synchronization now and setting " 
					+ clique.getParent() + " as new parent clique.");
			ret = new ArrayList<Clique>(1);
			ret.add(clique.getParent());
			parentMap.put(clique, ret);
		}
		return ((ret == null)?((List)Collections.emptyList()):ret);	// never return null
	}
	

	/**
	 * @return true if this junction "tree" has loops. False otherwise.
	 * If true, then {@link #consistency(Clique, boolean)} will run loopy belief propagation.
	 * @see #getMaxLoopyBPIteration()
	 * @see #setMaxLoopyBPIteration(int)
	 */
	public boolean isLoopy() {
		return isLoopy;
	}

	/**
	 * @param isLoopy : true if this junction "tree" has loops. False otherwise.
	 * If true, then {@link #consistency(Clique, boolean)} will run loopy belief propagation.
	 * @see #getMaxLoopyBPIteration()
	 * @see #setMaxLoopyBPIteration(int)
	 */
	protected void setLoopy(boolean isLoopy) {
		this.isLoopy = isLoopy;
	}
	
	/**
	 * @return the max number of iterations the loopy belief propagation in
	 * {@link #consistency(Clique, boolean)} will execute.
	 */
	public int getMaxLoopyBPIteration() {
		return maxLoopyBPIteration;
	}

	/**
	 * @param maxLoopyBPIteration : the max number of iterations the loopy belief propagation in
	 * {@link #consistency(Clique, boolean)} will execute.
	 */
	public void setMaxLoopyBPIteration(int maxLoopyBPIteration) {
		this.maxLoopyBPIteration = maxLoopyBPIteration;
	}
	
	/**
	 * @return the time in milliseconds allowed for {@link #consistency(Clique, boolean)}
	 * to run iterations in loopy Belief Propagation when {@link #isLoopy()} == true.
	 * After an iteration (a collect/propagate evidence loop) finishes, the time will be checked, 
	 * and the loopy BP will stop if the time exceeded this amount.
	 */
	public long getMaxLoopyBPTimeMillis() {
		return this.maxLoopyBPTimeMillis;
	}

	/**
	 * @param maxLoopyBPTimeMillis : the time in milliseconds allowed for {@link #consistency(Clique, boolean)}
	 * to run iterations in loopy Belief Propagation when {@link #isLoopy()} == true.
	 * After an iteration (a collect/propagate evidence loop) finishes, the time will be checked, 
	 * and the loopy BP will stop if the time exceeded this amount.
	 */
	protected void setMaxLoopyBPTimeMillis(long maxLoopyBPTimeMillis) {
		this.maxLoopyBPTimeMillis = maxLoopyBPTimeMillis;
	}

	/**
	 * @return true if the last run of {@link #consistency(Clique, boolean)} have changed any clique potentials.
	 */
	public Boolean isModified() {
		return isModified;
	}

	/**
	 * @param isModified : true if the last run of {@link #consistency(Clique, boolean)} have changed any clique potentials.
	 * This needs to be set to false before {@link #consistency(Clique, boolean)}, or else the loopy BN will not check whether 
	 * the clique potentials were changed.
	 */
	protected void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	/**
	 * @return the error margin which will be used when comparing probabilities. If the difference between two probability values
	 * are within this margin, then they will be considered equal.
	 */
	public float getProbErrorMargin() {
		return probErrorMargin;
	}

	/**
	 * @param probErrorMargin : the error margin which will be used when comparing probabilities. If the difference between two probability values
	 * are within this margin, then they will be considered equal.
	 */
	public void setProbErrorMargin(float probErrorMargin) {
		this.probErrorMargin = probErrorMargin;
	}
	
	/**
	 * Include a clique to the list of parent cliques of a given child clique.
	 * It does not update separators.
	 * @param parent : the clique to become parent
	 * @param child : the clique to become child
	 * @return true if there was some change in {@link #getCliqueParentMap()}. 
	 * False otherwise.
	 * @see #getCliqueParentMap()
	 * @see #setCliqueParentMap(Map)
	 * @see #clearParents(Clique)
	 * @see #removeParent(Clique, Clique)
	 * @see #initParentMapping()
	 */
	public boolean addParent(Clique parent, Clique child) {
		if (child == null || parent == null) {
			return false;
		}
		
		Map<Clique,List<Clique>> parentMap = getCliqueParentMap();
		// initialize map if it was not initialized yet
		if (parentMap == null) {
			this.initParentMapping();
			parentMap = getCliqueParentMap();
		}
		
		// assertion: map should be initialized at this point
		if (parentMap == null) {
			throw new IllegalStateException("Uninitialized mapping of parent cliques. LoopyJunctionTre#initParentMapping() must be called.");
		}
		
		List<Clique> parentCliques = parentMap.get(child);
		if (parentCliques == null) {
			parentCliques = new ArrayList<Clique>();
			parentMap.put(child, parentCliques);
		}
		
		if (child.getParent() == null) {
			child.setParent(parent);
		}
		
		return parentCliques.add(parent);
		
	}
	
	/**
	 * Initializes the content of {@link #getCliqueParentMap()}.
	 * Any content within it included prior to this method will be removed.
	 * @see #getCliqueParentMap()
	 * @see #setCliqueParentMap(Map)
	 * @see #addParent(Clique, Clique)
	 * @see #removeCliques(java.util.Collection)
	 * @see #clearParents(Clique)
	 * @see #getParents(Clique)
	 * @see Clique#getParent()
	 * @see unbbayes.prs.bn.IncrementalJunctionTreeAlgorithm#splitClique(Clique, LoopyJunctionTree, int)
	 */
	protected void initParentMapping() {
		// basic assertion
		if (this.getCliques() == null) {
			return;
		}
		
		// the mapping that identifies which cliques are parents of what cliques
		Map<Clique, List<Clique>> parentMap = getCliqueParentMap();
		if (parentMap == null) {
			// instantiate a new one
			parentMap = new HashMap<Clique, List<Clique>>();
			this.setCliqueParentMap(parentMap);
		} else {
			// reuse existing one, but make sure it is clean
			parentMap.clear();
		}
		
		// fill map initially with Clique#getParent()
		for (Clique clique : this.getCliques()) {
			if (clique.getParent() != null) {
				List<Clique> parents = new ArrayList<Clique>(1);
				parents.add(clique.getParent());
				parentMap.put(clique, parents);
			}
		}
	}
	
	/**
	 * Removes a parent from the list of parents of the provided clique.
	 * It does not update separators.
	 * @param parent : the clique to be removed from the list of parents
	 * @param child : clique with the list of parents to be altered.
	 * @return the removed object
	 * @see #getCliqueParentMap()
	 * @see #setCliqueParentMap(Map)
	 * @see #clearParents(Clique)
	 * @see #addParent(Clique, Clique)
	 * @see #initParentMapping()
	 */
	public boolean removeParent(Clique parent, Clique child) {
		
		// extract the mapping
		Map<Clique,List<Clique>> parentMap = getCliqueParentMap();
		if (parentMap == null || parentMap.isEmpty()) {
			this.initParentMapping();
			parentMap = getCliqueParentMap();
		}
		
		// extract the entry in mapping
		List<Clique> parents = parentMap.get(child);
		if (parents == null || parents.isEmpty()) {
			// child has no parent.
			// check if content is consistent with child#getParent()
			if (child.getParent() != null) {
				// the list of parent is empty, but child was related to some parent. There was inconsistency.
				Debug.println(getClass(), "Desync detected in mapping of parents of clique " + child  + ". Expected" + child.getParent() + ", but list of parents was " + parents);
				// force synchronization now
				parents = new ArrayList<Clique>();
				parents.add(child.getParent());
				parentMap.put(child, parents);
			} else {
				// nothing to remove anyway
				return false;
			}
		}
		
		// actually remove the parent from list
		if (parents.remove(parent)) {
			// removed some parent. Needs to check consistency with Clique#getParent()
			if (child.getParent() != null) {
				// child had reference to child.getParent(). Needs to check whether we need to update it or not
				if (child.getParent().equals(parent)) {
					// substitute child.getParent() with another parent. See if there is any parent that can fit there
					if (parents.isEmpty()) {
						// no parent to substitute. Set to null
						child.setParent(null);
						parentMap.remove(child);
					} else {
						// substitute with any remaining parent in list. Use 1st then.
						child.setParent(parents.get(0));
					}
				} // else no need to update child.getParent(
			} else { // there was a parent in mapping, but no parent in Clique#getParent()
				// it was inconsistent, but let's try to fix it now
				Debug.println(getClass(), "Desync detected in mapping of parents of clique " + child 
						+ ". List of parents had " + parent + ", but original reference to parent was null.");
				// substitute child.getParent() with another parent. See if there is any parent that can fit there
				if (parents.isEmpty()) {
					// no parent to substitute. Set to null
					child.setParent(null);
					parentMap.remove(child);
				} else {
					// substitute with any remaining parent in list. Use 1st then.
					child.setParent(parents.get(0));
				}
			}
			// don't forget to acknowledge caller that there was some modification
			return true;
		} else { // else parent was not there anyway, so nothing was removed
			// assure consistency with Clique#getParent()
			if (child.getParent() != null && child.getParent().equals(parent)) {
				// there was an inconsistency, but we can fix it here
				Debug.println(getClass(), "Desync detected in mapping of parents of clique " + child 
						+ ". List of parents did not have " + parent + ", but original reference to parent had it.");
				if (parents.isEmpty()) {
					// no parent to substitute. Set to null
					child.setParent(null);
					parentMap.remove(child);
				} else {
					// substitute with any remaining parent in list. Use 1st then.
					child.setParent(parents.get(0));
				}
			}
		}
		
		// at this point, nothing was removed
		return false;
	}
	
	/**
	 * @return all cliques in {@link #getCliques()} whose {@link #getParents(Clique)} are null or empty.
	 * Most of algorithms expect this list to contain only 1 entry.
	 */
	public List<Clique> findRootCliques() {
		List<Clique> ret = new ArrayList<Clique>(1);	// prepare the list to return
		// for each clique, check parents
		for (Clique clique : getCliques()) {
			List<Clique> parents = getParents(clique);
			if (parents == null || parents.isEmpty()) {
				// this clique has no parents
				ret.add(clique);
			}
		}
		return ret;
	}
	
	/**
	 * Clears the list of parents of the provided child.
	 * It will also set {@link Clique#getParent()} to null.
	 * It does not update separators.
	 * @param child : clique to have list of parents cleared.
	 * @see #getCliqueParentMap()
	 * @see #setCliqueParentMap(Map)
	 * @see #addParent(Clique, Clique)
	 * @see #removeParent(Clique, Clique)
	 * @see #initParentMapping()
	 */
	public void clearParents(Clique child) {
		// basic assertion
		if (child == null) {
			return;
		}
		
		// extract map entry
		Map<Clique,List<Clique>> parentMap = getCliqueParentMap();
		if (parentMap != null) {
			parentMap.remove(child); // clear the mapping by deleting the entry
//			List<Clique> parents = parentMap.get(child);  
//			if (parents != null) {
//			// clear the value, but keep the entry
//				parents.clear();
//			}
		}
		
		// make sure this reference is also cleared
		child.setParent(null);
	}
	
	/**
	 * Reorganizes the junction tree hierarchy so that the given cluster becomes the root of the subtree it belongs.
	 * The difference from superclass is that this method assumes that the cluster structure does not necessarily
	 * follow a tree structure, so it updates {@link #getCliqueParentMap()} instead of only updating {@link Clique#getParent()}
	 * @see unbbayes.prs.bn.JunctionTree#moveCliqueToRoot(unbbayes.prs.bn.Clique)
	 * @see #addParent(Clique, Clique)
	 * @see #removeParent(Clique, Clique)
	 * @see #clearParents(Clique)
	 * @see #setCliqueParentMap(Map)
	 * @see #getParents(Clique)
	 */
	public void moveCliqueToRoot(Clique cliqueToBecomeRoot) {
		// basic assertion and stop condition
		if (cliqueToBecomeRoot == null || cliqueToBecomeRoot.getParent() == null) {
			return;	// there is nothing to do
		}
		
		
		// extract the parent clique, so that we can set it as a child of current clique
		List<Clique> parentCliques = new ArrayList(getParents(cliqueToBecomeRoot)); // use a clone of the list, because we will make modifications, and ordinal list doesn't allow concurrent modification.
		if (parentCliques == null || parentCliques.isEmpty()) {
			return;	// it is already a root, so there is nothing to do
			// This is a redundant check, but Clique#getParent() may be desync with getParents(Clique), so I'm double checking anyway
		}
		
		// call recursive, so that at least one of my immediate parent becomes the root of the tree
		moveCliqueToRoot(parentCliques.get(0));	// One of my parents becoming a root is sufficient, so use 1st.
		
		// at this point, my first parent is the root of the tree this clique belongs
		
		// convert all parent cliques to children of current clique (this is the difference from superclass' method)
		for (Clique parentClique : parentCliques) {	
			
			// need to extract separator in advance, because we may not be able to extract separator once we call addParent next
			// some of the algorithms expects the 1st clique of separator to be a parent, and the other to be a child, 
			// so we need to revert order in separator too. Separator does not allow such changes, so we need to substitute separator
			Separator oldSeparator = getSeparator(parentClique, cliqueToBecomeRoot);	// this represents the link parentClique->cliqueToBecomeRoot
			removeSeparator(oldSeparator);	// delete this separator, so that we can include a new one
			
			// make current parent to become a child of cliqueToBecomeRoot
			parentClique.removeChild(cliqueToBecomeRoot);
			cliqueToBecomeRoot.addChild(parentClique);
			addParent(cliqueToBecomeRoot,parentClique);	// update the mapping of parents (this is another difference from superclass' method)
			// parents of cliqueToBecomeRoot will become null/empty (i.e. clique will be set as a root) later, after the "for" loop
			
			// create a new separator. This represents the link cliqueToBecomeRoot->parentClique (the direction is the inverse of old separator)
			Separator newSeparator = new Separator(cliqueToBecomeRoot, parentClique, false);	// false means that the constructor shall not change Clique#getParent and Clique#getChildren
			// copy content from old separator
			newSeparator.setInternalIdentificator(oldSeparator.getInternalIdentificator());
			newSeparator.setNodes(oldSeparator.getNodes());
			newSeparator.setProbabilityFunction(oldSeparator.getProbabilityFunction());	// we can reuse same instance of separator table, because old separator will not be used anymore
			addSeparator(newSeparator);	// include the new separator, and now we finished replacing the separator.
			
			// if any node is pointing to the old separator (with TreeVariable#getAssociatedClique()), then point to the new separator instead
			for (Node node : newSeparator.getNodes()) {
				if ((node instanceof TreeVariable)
						&& ((TreeVariable)node).getAssociatedClique() == oldSeparator) {	// use exact instance equality, instead of Object#equal
					((TreeVariable)node).setAssociatedClique(newSeparator);
				}
			}
		}
		
		
		// become the new root
		clearParents(cliqueToBecomeRoot);	// clear mapping of parent cliques and set Clique#setParent(null).
		
	}

	/**
	 * @return the cliqueParentMap : mapping from a child clique to its parents.
	 * @see #addParent(Clique, Clique)
	 * @see #removeParent(Clique, Clique)
	 * @see #getParents(Clique)
	 */
	protected Map<Clique, List<Clique>> getCliqueParentMap() {
		return cliqueParentMap;
	}

	/**
	 * @param cliqueParentMap : mapping from a child clique to its parents.
	 * @see #addParent(Clique, Clique)
	 * @see #removeParent(Clique, Clique)
	 * @see #getParents(Clique)
	 */
	protected void setCliqueParentMap(Map<Clique, List<Clique>> cliqueParentMap) {
		this.cliqueParentMap = cliqueParentMap;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#removeSeparator(unbbayes.prs.bn.Separator)
	 */
	public void removeSeparator(Separator sep) {
		super.removeSeparator(sep);
		// also remove mapping of parents
		this.removeParent(sep.getClique1(), sep.getClique2());
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#removeCliques(java.util.Collection)
	 */
	public boolean removeCliques(Collection<Clique> cliques) {
		boolean isModified = super.removeCliques(cliques);
		if (isModified) {
			// also remove reference in mapping
			Map<Clique, List<Clique>> map = this.getCliqueParentMap();
			if (map != null && !map.isEmpty()) {
				// remove references in values first
				for (Entry<Clique, List<Clique>> entry : map.entrySet()) {
					entry.getValue().removeAll(cliques);
				}
				// then, remove keys
				for (Clique key : cliques) {
					map.remove(key);
				}
			}
		}
		return isModified;
	}

	/**
	 * This method simply extends superclass to allow compatibility with {@link #getParents(Clique)}.
	 * @see unbbayes.prs.bn.JunctionTree#visitCliquesSeparatorsContainingAllNodesRecursive(unbbayes.prs.bn.Clique, java.util.Collection, int, java.util.List, boolean, java.util.Set)
	 */
	protected int visitCliquesSeparatorsContainingAllNodesRecursive( Clique pivot, Collection<INode> nodesToContain, int maxCount, 
			List<IRandomVariable> output, boolean isToVisitSeparators, Set<Clique> visitedCliques) {

		if (maxCount <= 0) {
			return 0;
		}
		
		// mark current clique as visited
		if (!visitedCliques.add(pivot)) {
			// return immediately if visitedCliques already contained pivot (in such case, Set#add() will return false)
			return 0;
		}
		
		// get the intersection between the nodes in pivot and nodesToContain
		List<INode> intersection = new ArrayList<INode>(nodesToContain);
		intersection.retainAll(pivot.getNodes());
		
		// if pivot contains nothing about nodesToContain, then no separators and adjacent nodes can contain all nodesToContain
		if (intersection.isEmpty()) {
			return 0;
		}
		int addedQuantity = 0;
		if (isToVisitSeparators) { // we are only considering separators
			// visit parent if its not null and not visited yet
			List<Clique> parents = getParents(pivot);
			if (parents != null) {
				for (Clique parent : parents ) {
					if (parent != null && !visitedCliques.contains(parent)) {
						Separator separator = getSeparator(pivot, parent);
						// do not re-visit it if it's in output already
						if (separator!= null && !SetToolkit.containsExact(output, separator)) {
							if (separator.getNodes().containsAll(nodesToContain)) {
								output.add(separator);
								addedQuantity++;
								// do not visit other cliques if we reached maxCount
								if (maxCount - addedQuantity <= 0) {
									return addedQuantity;
								}
							}
							// visit parent
							addedQuantity += this.visitCliquesSeparatorsContainingAllNodesRecursive(
									parent, nodesToContain, maxCount-addedQuantity, output, isToVisitSeparators, visitedCliques
									);
							// do not visit other cliques if we reached maxCount
							if (maxCount - addedQuantity <= 0) {
								return addedQuantity;
							}
						}
					}
				}
			}
			// visit children
			if (pivot.getChildren() != null) {
				for (Clique child : pivot.getChildren()) {
					if (visitedCliques.contains(child)) {
						// do not visit cliques if it was visited already
						continue;
					}
					Separator separator = getSeparator(pivot, child);
					// visit child if its not null and not visited yet
					if (separator!= null && !SetToolkit.containsExact(output, separator)) {
						if (separator.getNodes().containsAll(nodesToContain)) {
							output.add(separator);
							addedQuantity++;
							// do not visit other cliques if we reached maxCount
							if (maxCount - addedQuantity <= 0) {
								return addedQuantity;
							}
						}
						// visit child
						addedQuantity += this.visitCliquesSeparatorsContainingAllNodesRecursive(
								child, nodesToContain, maxCount-addedQuantity, output, isToVisitSeparators, visitedCliques
							);
						// do not visit other cliques if we reached maxCount
						if (maxCount - addedQuantity <= 0) {
							return addedQuantity;
						}
					}
				}
			}
		} else { // we are only considering cliques
			// check pivot
			if (pivot.getNodes() != null && pivot.getNodes().containsAll(nodesToContain)) {
				// add pivot to output
				output.add(pivot);
				addedQuantity++;
				// do not visit other cliques if we reached maxCount
				if (maxCount - addedQuantity <= 0) {
					return addedQuantity;
				}
			}
			// visit parent if its not null, not visited yet, and not in output
			List<Clique> parents = getParents(pivot);
			if (parents != null) {
				for (Clique parent : parents) {
					if (parent != null && !visitedCliques.contains(parent) && !SetToolkit.containsExact(output, parent)) {
						addedQuantity += this.visitCliquesSeparatorsContainingAllNodesRecursive(
									parent, nodesToContain, maxCount-addedQuantity, output, isToVisitSeparators, visitedCliques
								);
						// do not visit other cliques if we reached maxCount
						if (maxCount - addedQuantity <= 0) {
							return addedQuantity;
						}
					}
				}
			}
			// visit children
			if (pivot.getChildren() != null) {
				for (Clique child : pivot.getChildren()) {
					if (visitedCliques.contains(child)) {
						// do not visit cliques if it was visited already
						continue;
					}
					if (!SetToolkit.containsExact(output, child)) {
						addedQuantity += this.visitCliquesSeparatorsContainingAllNodesRecursive(
								child, nodesToContain, maxCount-addedQuantity, output, isToVisitSeparators,visitedCliques
							);
						// do not visit other cliques if we reached maxCount
						if (maxCount - addedQuantity <= 0) {
							return addedQuantity;
						}
					}
				}
			}
		}
		return addedQuantity;
	}

	
	
	

//	/**
//	 * @return if true, {@link #absorb(Clique, Clique)} will use a static float array
//	 * for temporary storing clique potentials for comparison and for updating {@link #isModified()}.
//	 * This can reduce frequency of garbage collection, but will disable parallelism.
//	 */
//	public boolean isToUseStaticProbArray() {
//		return isToUseStaticProbArray;
//	}
//
//	/**
//	 * @param isToUseStaticProbArray : if true, {@link #absorb(Clique, Clique)} will use a static float array
//	 * for temporary storing clique potentials for comparison and for updating {@link #isModified()}.
//	 * This can reduce frequency of garbage collection, but will disable parallelism.
//	 */
//	public void setToUseStaticProbArray(boolean isToUseStaticProbArray) {
//		this.isToUseStaticProbArray = isToUseStaticProbArray;
//	}

	
	

}
