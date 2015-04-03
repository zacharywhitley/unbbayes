/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.id.UtilityNode;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;

/**
 * This class represents a junction tree for Bayes Nets.
 *@author     Michael
 *@author     Rommel
 */
public class JunctionTree implements java.io.Serializable, IJunctionTree {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	
	
	private boolean initialized = false;

	private float totalEstimatedProb;

	
	private List<Clique> cliques;

	/**
	 *  List of Associated separatorsMap.
	 */
	private Collection<Separator> separators = new ArrayList<Separator>();
	private Map<Clique, Set<Separator>> separatorsMap = new HashMap<Clique, Set<Separator>>();


	private boolean isUsingSafeOrdering = true;

	/** If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage */
	private static boolean isToUseSingletonListsInAbsorb = true;
	
	/** This is the singleton temporary list used by {@link #absorb(Clique, Clique)} when {@link #isToUseSingletonListsInAbsorb()} == true */
	private static List<Node> singletonListForAbsorb = new ArrayList<Node>(7);

//	/**
//	 * Pre-calculated coordinates for optimizing the method absorb
//	 */
//	private int coordSep[][][];

	/**
	 * Default constructor for junction tree. It initializes the list {@link #getCliques()}.
	 */
	public JunctionTree() {
//		separatorsMap = new ArrayList<Separator>();
		cliques = new ArrayList<Clique>();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getN()
	 */
	public float getN() {
		return totalEstimatedProb;
	}
	
	/**
	 * Caution: {@link Separator#setInternalIdentificator(int)} must be set
	 * to a value before calling this method.
	 * This method does not update {@link Clique#getParent()} and {@link Clique#getChildren()}.
	 * @see unbbayes.prs.bn.IJunctionTree#addSeparator(unbbayes.prs.bn.Separator)
	 */
	public void addSeparator(Separator sep) {
		separators.add(sep);
		Set<Separator> seps = separatorsMap.get(sep.getClique1());
		if (seps == null) {
			seps = new HashSet<Separator>();
			seps.add(sep);
			separatorsMap.put(sep.getClique1(), seps);
		} else {
			seps.add(sep);
		}
		seps = separatorsMap.get(sep.getClique2());
		if (seps == null) {
			seps = new HashSet<Separator>();
			seps.add(sep);
			separatorsMap.put(sep.getClique2(), seps);
		} else {
			seps.add(sep);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#removeSeparator(unbbayes.prs.bn.Separator)
	 */
	public void removeSeparator(Separator sep) {
		if (sep != null) {
			Set<Separator> seps = separatorsMap.get(sep.getClique1());
			if (seps != null) {
				seps.remove(sep);
				// if the list became empty, delete the key too
				if (seps.isEmpty()) {
					separatorsMap.remove(sep.getClique1());
				}
			}
			seps = separatorsMap.get(sep.getClique2());
			if (seps != null) {
				seps.remove(sep);
				// if the list became empty, delete the key too
				if (seps.isEmpty()) {
					separatorsMap.remove(sep.getClique2());
				}
			}
		}
		separators.remove(sep);
	}
	

//	/**
//	 * (non-Javadoc)
//	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorsSize()
//	 */
//	public int getSeparatorsSize() {
//		return separatorsMap.size();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorAt(int)
//	 */
//	public Separator getSeparatorAt(int index) {
//		return separatorsMap.get(index);
//	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getCliques()
	 */
	public List<Clique> getCliques() {
		return cliques;
	}
	

	/**
	 * This method propagates the evidence of all cliques
	 * accessible from the 1st clique at {@link #getCliques()}
	 * (which is supposedly the root clique of the junction tree).
	 * In the current implementation, the root clique is connected
	 * to all disconnected sub-cliques through empty separators, therefore,
	 * calling this method will automatically consider
	 * all disconnected sub-trees as well.
	 * @see unbbayes.prs.bn.IJunctionTree#consistency()
	 * @see #consistency(Clique)
	 */
	public void consistency() throws Exception {
		this.consistency(cliques.get(0),true);	// 2nd arg == true -> assume there are evidences in any clique - so call recursive even when separator is empty
	}
	
	/**
	 * This is basically the same of {@link #consistency()}, but
	 * it is customized for {@link #initBeliefs()}, so that underflow is avoided.
	 * @throws Exception
	 */
	protected void initConsistency() throws Exception {
		Clique rootClique = cliques.get(0);
		totalEstimatedProb = 1;
		collectEvidence(rootClique,true, true);
		distributeEvidences(rootClique, true);
	}
	
	/**
	 * Basically, this method propagates evidences by calling
	 * {@link #coleteEvidencia(Clique)} and {@link #distributeEvidences(Clique)},
	 * passing as their arguments the clique specified in the argument.
	 * @param rootClique this clique will be considered as the root node
	 * of the junction tree during this propagation, and evidences will
	 * only be propagated across this node and descendants.
	 * This is useful in order to force the propagation to be limited
	 * to a subtree of the junction tree, specially if such subtree is supposedly
	 * disconnected from other portions, and no other evidence is expected in other
	 * cliques disconnected from the current clique.
	 * If null, then the 1st clique in {@link #getCliques()} will be used.
	 * 
	 * @param isToContinueOnEmptySep : if false, propagation will not recursively
	 * guarantee global consistency to subtrees connected with empty separators.
	 * If evidences are expected to be across several cliques, set this to true,
	 * otherwise, if evidences are expected to be present only at the subtree
	 * of rootClique, then set this as false.
	 * @throws Exception
	 * @see unbbayes.prs.bn.IJunctionTree#consistency()
	 * @see {@link #consistency()}
	 * @see #distributeEvidences(Clique)
	 * @see #collectEvidence(Clique, boolean, boolean)
	 */
	public void consistency(Clique rootClique, boolean isToContinueOnEmptySep) throws Exception {
		if (rootClique == null) {
			rootClique = cliques.get(0);
		}
		totalEstimatedProb = 1;
		collectEvidence(rootClique, isToContinueOnEmptySep);
		distributeEvidences(rootClique, isToContinueOnEmptySep);
	}
	

	/**
	 * Simply delegates to {@link #collectEvidence(Clique, boolean)} passing true in its second argument
	 * @param clique
	 * @throws Exception
	 * @deprecated use {@link #collectEvidence(Clique, boolean)} instead
	 */
	@Deprecated
	protected void coleteEvidencia(Clique clique) throws Exception {
		this.collectEvidence(clique, true);
	}
	
	/**
	 * Delegates to {@link #collectEvidence(Clique, boolean, boolean)} setting the last argument to false.
	 */
	protected void collectEvidence(Clique clique, boolean isToContinueOnEmptySep) throws Exception {
		this.collectEvidence(clique, isToContinueOnEmptySep, false);
	}
	/**
	 * Processes the collection of evidences.
	 * It absorbs child cliques.
	 *@param  clique  clique.
	 *@param isToContinueOnEmptySep: if false, will stop propagation if empty separator is found.
	 *@param isInitialization: if true, will attempt to run optimizations for the context of {@link #initBeliefs()}.
	 */
	private void collectEvidence(Clique clique, boolean isToContinueOnEmptySep, boolean isInitialization) throws Exception {
		for (Clique auxClique : clique.getChildren()) {
			if (isToContinueOnEmptySep) {
				// call recursive regardless of separator
				this.collectEvidence(auxClique,isToContinueOnEmptySep, isInitialization);
				absorb(clique, auxClique);
				if (isInitialization) {
					// not normalizing now may cause underflow if there are plenty of child cliques during initBelief
					clique.normalize();
				}
			} else {
				// call recursive only if separator can propagate evidence (non-empty and more than 1 state)
				Separator sep = getSeparator(clique, auxClique); 
				if (sep.getProbabilityFunction().tableSize() > 1) {
					this.collectEvidence(auxClique,isToContinueOnEmptySep, isInitialization);
					absorb(clique, auxClique);
					if (isInitialization) {
						// not normalizing now may cause underflow if there are plenty of child cliques during initBelief
						clique.normalize();
					}
				}
			}
		}

		if (!isInitialization) {
			totalEstimatedProb *= clique.normalize();
		} else if (clique.getChildren().isEmpty()) {
			// leaf cliques may not have been normalized in initialization
			clique.normalize();
		}
	}

	/**
	 * Simply delegates to {@link #distributeEvidences(Clique, boolean)}
	 * @param clique
	 * @deprecated use {@link #distributeEvidences(Clique, boolean)} instead
	 */
	@Deprecated
	protected void distributeEvidences(Clique clique) {
		this.distributeEvidences(clique, true);
	}
	/**
	 * Processes the distribution of evidences.
	 * It distributes potentials to child cliques
	 *@param  clique  clique.
	 */
	protected void distributeEvidences(Clique clique, boolean isToContinueOnEmptySep) {
		for (Clique auxClique : clique.getChildren()) {
			
			absorb(auxClique, clique);
			
			if (isToContinueOnEmptySep) {
				// call recursive regardless of separator
				distributeEvidences(auxClique,isToContinueOnEmptySep);
			} else {
				// call recursive only if separator can propagate evidence (non-empty and more than 1 state)
				Separator sep = getSeparator(clique, auxClique); 
				if (sep.getProbabilityFunction().tableSize() > 1) {
					distributeEvidences(auxClique,isToContinueOnEmptySep);
				}
			}
		}
	}
	
	/**
	 * Propagates the probabilities from clique2 to clique1.
	 * @param clique1
	 * @param clique2
	 */
	protected void absorb(Clique clique1, Clique clique2) {
		Separator sep = getSeparator(clique1, clique2);
		if (sep == null) {
//			Debug.println(getClass(), clique1 + " and " + clique2 + " are disconnected.");
			return;
		}
		PotentialTable sepTab = sep.getProbabilityFunction();
		if (sepTab.tableSize() <= 0) {
//			Debug.println(getClass(), clique1 + " and " + clique2 + " has empty separator.");
			return;
		}
		
		List<Node> nodesInClique2NotInSeparator = null;
		if (isToUseSingletonListsInAbsorb) {
			nodesInClique2NotInSeparator = singletonListForAbsorb;
			nodesInClique2NotInSeparator.clear();
			nodesInClique2NotInSeparator.addAll(clique2.getNodesList());
			nodesInClique2NotInSeparator.removeAll(sep.getNodes());
		} else {
			nodesInClique2NotInSeparator = new ArrayList<Node>(clique2.getNodesList());
			nodesInClique2NotInSeparator.removeAll(sep.getNodes());
		}

//		PotentialTable dummyTable = (PotentialTable) clique2.getProbabilityFunction().clone();
		PotentialTable dummyTable = (PotentialTable) clique2.getProbabilityFunction().getTemporaryClone();
		
		for (Node nodeToRemove : nodesInClique2NotInSeparator) {
			dummyTable.removeVariable(nodeToRemove);
		}
		

//		PotentialTable originalSeparatorTable = (PotentialTable) sepTab.clone();
		// TODO the following only works because PotentialTable#getTemporaryClone() returns 2 different singleton instances alternately
		PotentialTable originalSeparatorTable = (PotentialTable) sepTab.getTemporaryClone();
		if (originalSeparatorTable == dummyTable) {
			// PotentialTable#getTemporaryClone() stopped working as desired. Use normal clone, instead of temporary clones.
			Debug.println(getClass(), "[CAUTION] PotentialTable#getTemporaryClone() is not returning 2 singleton instances alternately.");
			
			// needs to re-create dummyTable, because it was overwritten
			dummyTable = (PotentialTable) clique2.getProbabilityFunction().clone();
			for (Node nodeToRemove : nodesInClique2NotInSeparator) {
				dummyTable.removeVariable(nodeToRemove);
			}
		}

//		for (int i = sepTab.tableSize() - 1; i >= 0; i--) {
//			// this is not a safe operation, because it is assuming that variables in clique tables and separator tables follow the same ordering. fix it
//			sepTab.setValue(i, dummyTable.getValue(i));
//		}
		// the above loop was substituted with the following code, in order to keep safety regarding the order of variables
		if (isUsingSafeOrdering) {
			// assume we will not have problems regarding ordering of variables, so use unsafe (but fast) copy
			System.arraycopy(dummyTable.getValues(), 0, sepTab.getValues(), 0, sepTab.tableSize());
		} else {
			// we may have cliques with variables not ordered in same way, so we need to check such ordering
			boolean isOrdered = true; // becomes false if there is any variable not in the same order
			int numVars = sepTab.variableCount();
			// check if dummyTable and sepTab has same ordering of variables
			for (int i = 0; i < numVars; i++) {
				if (!sepTab.getVariableAt(i).equals(dummyTable.getVariableAt(i))) {
					isOrdered = false;
					break;
				}
			}
			if (isOrdered) {
				// just use fast copy of table data
				System.arraycopy(dummyTable.getValues(), 0, sepTab.getValues(), 0, sepTab.tableSize());
			} else {
				// filling table with 1 (neutral value in products) and then multiplying with dummyTable is equivalent to copying the content of dummyTable to sepTab
				sepTab.fillTable(1f);
				sepTab.directOpTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);	// this operation is supposedly safe (regarding the order of variables).
			}
		}

		dummyTable.directOpTab(
			originalSeparatorTable,
			PotentialTable.DIVISION_OPERATOR);

		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);
    }
	
	/**
	 * This method also updates the {@link IRandomVariable#getInternalIdentificator()}
	 * and {@link #isInitialized()}
	 * @see unbbayes.prs.bn.IJunctionTree#initBeliefs()
	 */
	public void initBeliefs() throws Exception {
		if (!isInitialized()) {
			Clique auxClique;
	
			int sizeCliques = cliques.size();
			for (int k = 0; k < sizeCliques; k++) {
				auxClique = cliques.get(k);
				// also, update the internal id, for fast comparison
				// TODO internal id should be updated in this.updateCliqueAndSeparatorInternalIdentificators() only, but it's here just for fast execution
				auxClique.setInternalIdentificator(k);
				this.initBelief(auxClique);
			}
	
			int separatorId = -1;
			for (Separator auxSep : getSeparators()) {
				// also, update the internal id, for fast comparison
				// TODO internal id should be updated in this.updateCliqueAndSeparatorInternalIdentificators() only, but it's here just for fast execution
				auxSep.setInternalIdentificator(separatorId--);
				this.initBelief(auxSep);
			}
			
			this.initConsistency();
			copyTableData();
			// TODO uncomment the following after replacing the routine for updating the internal ids
//			this.updateCliqueAndSeparatorInternalIdentificators();
			setInitialized(true);
		} else {
			restoreTableData();						
		}
	}
	
	/**
	 * Initializes the probability potential of a random variable. Particularly, it
	 * initializes potentials of either a {@link Separator} or a {@link Clique}
	 * @param rv
	 * @see #initBelief(Separator)
	 * @see #initBelief(Clique)
	 */
	public void initBelief(IRandomVariable rv) {
		if (rv instanceof Clique) {
			this.initBelief((Clique)rv);
			return;
		} else if (rv instanceof Separator) {
			this.initBelief((Separator)rv);
			return;
		}
		throw new IllegalArgumentException(rv + " != " + Clique.class.getName() + " && " + rv + " != " + Separator.class.getName());
	}
	
	/**
	 * Initializes the probability potential of a separator
	 * @param auxSep
	 */
	public void initBelief(Separator auxSep) {
		// initialize table related to probabilistic nodes
		PotentialTable auxTabPot = auxSep.getProbabilityFunction();
//		int sizeDados = auxTabPot.tableSize();
//		for (int c = 0; c < sizeDados; c++) {
//			auxTabPot.setValue(c, 1);
//		}
		// the above code was substituted by the following
		auxTabPot.fillTable(1f);

		// initialize table related to utility nodes
		PotentialTable auxUtilTab = auxSep.getUtilityTable();
//		int utilTableSize = auxUtilTab.tableSize();
//		for (int i = 0; i < utilTableSize; i++) {
//			auxUtilTab.setValue(i, 0);
//		}
		// the above code was substituted by the following
		auxUtilTab.fillTable(0f);
		
	}

	/**
	 * Initializes the probability potential of a clique
	 * @param clique
	 */
	public void initBelief(Clique clique) {
		PotentialTable auxTabPot = clique.getProbabilityFunction();
		PotentialTable auxUtilTab = clique.getUtilityTable();

		int tableSize = auxTabPot.tableSize();
		for (int c = 0; c < tableSize; c++) {
			auxTabPot.setValue(c, 1);
		}

		ProbabilisticNode auxVP;
		int sizeAssociados = clique.getAssociatedProbabilisticNodes().size();
		for (int c = 0; c < sizeAssociados; c++) {
			auxVP = (ProbabilisticNode) clique.getAssociatedProbabilisticNodes().get(c);
			auxTabPot.opTab(auxVP.getProbabilityFunction(), PotentialTable.PRODUCT_OPERATOR);
		}

		tableSize = auxUtilTab.tableSize();
		for (int i = 0; i < tableSize; i++) {
			auxUtilTab.setValue(i, 0);
		}
		UtilityNode utilNode;
		sizeAssociados = clique.getAssociatedUtilityNodes().size();
		for (int i = 0; i < sizeAssociados; i++) {
			utilNode = (UtilityNode) clique.getAssociatedUtilityNodes().get(i);
			auxUtilTab.opTab(utilNode.getProbabilityFunction(), PotentialTable.PLUS_OPERATOR);
		}
	}

	private void restoreTableData() {
		int sizeCliques = cliques.size();
		for (int k = 0; k < sizeCliques; k++) {
			Clique auxClique = (Clique) cliques.get(k);
			auxClique.getProbabilityFunction().restoreData();
			auxClique.getUtilityTable().restoreData();
		}
		
		for (Separator auxSep : getSeparators()) {
			auxSep.getProbabilityFunction().restoreData();
			auxSep.getUtilityTable().restoreData();
		}
	}
	
	private void copyTableData() {
		int sizeCliques = cliques.size();
		for (int k = 0; k < sizeCliques; k++) {
			Clique auxClique = (Clique) cliques.get(k);
			auxClique.getProbabilityFunction().copyData();
			auxClique.getUtilityTable().copyData();
		}
		
		for (Separator auxSep : getSeparators()) {
			auxSep.getProbabilityFunction().copyData();
			auxSep.getUtilityTable().copyData();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparator(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	public Separator getSeparator(Clique clique1, Clique clique2) {
		Set<Separator> seps = separatorsMap.get(clique1);	// separators reachable from clique 1
		if (seps == null) {
			seps = Collections.emptySet();	// make sure we use non-null collections
		}
		
		Clique theOtherClique = clique2;	// the clique other that the one used to extract separator
		
		// compare with size of separators reachable from clique 2, because we will do linear search in case of multiple separators
		Set<Separator> seps2 = separatorsMap.get(clique2);	// separators reachable from clique 2
		if (seps2 == null) {
			seps2 = Collections.emptySet();	// make sure we use non-null collections
		}
		
		if (seps2.size() < seps.size()) {
			// use separators reachable from clique 2, because it's smaller, and set the other clique as clique 1
			seps = seps2;
			theOtherClique = clique1;	
		}
		
		for (Separator separator : seps) {
//			if (separator.getClique2().equals(clique2) || separator.getClique1().equals(clique2)) {
			if (separator.getClique2().getInternalIdentificator() == theOtherClique.getInternalIdentificator()
					|| separator.getClique1().getInternalIdentificator() == theOtherClique.getInternalIdentificator()) {
				return separator;
			}
		}
//		if (seps != null) {
//			for (Separator separator : seps) {
////				if (separator.getClique2().equals(clique2) || separator.getClique1().equals(clique2)) {
//				if (separator.getClique2().getInternalIdentificator() == clique2.getInternalIdentificator()
//						|| separator.getClique1().getInternalIdentificator() == clique2.getInternalIdentificator()) {
//					return separator;
//				}
//			}
//		}
//		seps = separatorsMap.get(clique2);
//		if (seps != null) {
//			for (Separator separator : seps) {
//				if (separator.getClique2().equals(clique1)) {
//					return separator;
//				}
//			}
//		}
//		int sizeSeparadores = separatorsMap.size();
//		for (int indSep = 0; indSep < sizeSeparadores; indSep++) {
//			Separator separator = (Separator) separatorsMap.get(indSep);
//			if (((separator.getClique1() == clique1) && (separator.getClique2() == clique2))
//				|| ((separator.getClique2() == clique1) && (separator.getClique1() == clique2))) {
//				return separator;
//			}
//		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparators()
	 */
	public Collection<Separator> getSeparators() {
		return separators;
	}
	
	/**
	 * This is just for subclasses to be able to customize the behavior of {@link #getSeparators()}
	 * @param separators
	 */
	protected void setSeparators(Collection<Separator> separators) {
		this.separators = separators;
	}
	
	/**
	 *  Finds all paths between two cliques.
	 *  If the clique structure is a tree, then this method should be equivalent to {@link #getPath(Clique, Clique)}.
	 *  If the clique structure is a graph (i.e. it has loops), then this method finds all paths connecting the two cliques.
     * @param from : a clique to start from. This needs to be a clique in this junction tree.
     * @param to : a clique to finish search. This also needs to be in this junction tree.
     * @return : a collection of lists representing the sequence of cliques that forms a path between the two cliques provided in the arguments.
     * The first element should be the "from" argument, and the last element shall be the "to" argument.
     * @see #getParents(Clique)
     * @see Clique#getChildren()
	 */
	public Collection<List<Clique>> getPaths(Clique from, Clique to) {
		// basic assertions
		if (from == null || to == null) {
			return Collections.emptySet();
		}
		// don't search for a path if the two cliques are the same
		if (from.equals(to)) {
			return Collections.singleton(Collections.singletonList(from));
		}
		
		// initiate a recursive call
		return this.getRoutesRec(from, to, new ArrayList<Clique>(), new HashSet<Clique>());
	}
	
	/**
	 * This is used in {@link #getPaths(Clique, Clique)}
	 * It makes a recursive visit to the cliques in this junction tree.
	 * @param processingPath (input): registers the cliques already visited, in order to prevent cycles. This must be non null.
	 * The initial call to this recursive method shall set this argument as an empty, modifiable list.
	 * @param deadCliques : nodes that we are certain that there is no route to to. The initial call to this recursive method shall set
	 * this argument as an empty, modifiable set.
	 * @return a set of all path from "from" to "to". The path is represented as a list containing all
	 * cliques included in the path.
	 */
	protected Collection<List<Clique>> getRoutesRec(Clique from, Clique to, List<Clique> processingPath, Set<Clique> deadCliques) {
		
		Collection<List<Clique>> ret = new HashSet<List<Clique>>(); // Initialize the collection (of paths) to return
		
		
		// mark the current clique as "evaluated", but don't use processingPath directly, since we don't want it to be an output parameter
		List<Clique> processingPathCurrentScope = new ArrayList<Clique>(processingPath);
		processingPathCurrentScope.add(from);
		
		
		// initialize a set of dead clique for my scope
		// note that if a node is dead for my scope (currently processing path), it may not be dead for my upper scope (another path)
		// that's why I must create deadNodes for my scope
		Set<Clique> deadNodesForMyScope = new HashSet<Clique>(deadCliques);
		
		// iterate on parents
		for (Clique parent : getParents(from)) {
			
			if (deadCliques.contains(parent)) {
				// we know dead cliques have no path to the "to" clique... So ignore
				continue;
			}
			if (processingPathCurrentScope.contains(parent)) {
				// this is a cycle. Ignore this sub-path
				continue;
			}
			if (to.equals(parent)) {
				// path found!
				List<Clique> path = new ArrayList<Clique>(processingPathCurrentScope);
				path.add(parent);
				ret.add(path);
				
				// we should find more routes, so lets continue 
				continue;	 // no need to make recursive call
			}
			
			// recursive call
			Collection<List<Clique>> rec = this.getRoutesRec(parent, to, processingPathCurrentScope, deadNodesForMyScope);
			if (rec.isEmpty()) {
				// we recursively know that there is no path from adjacent to setTo, so, it is dead
				deadNodesForMyScope.add(parent);
			} // else, we found some path
			ret.addAll(rec);
		}
		
		// iterate on children;
		for (Clique child : from.getChildren()) {
			
			if (deadCliques.contains(child)) {
				// we know dead cliques have no path to the "to" clique... So ignore
				continue;
			}
			if (processingPathCurrentScope.contains(child)) {
				// this is a cycle. Ignore this sub-path
				continue;
			}
			if (to.equals(child)) {
				// path found!
				List<Clique> path = new ArrayList<Clique>(processingPathCurrentScope);
				path.add(child);
				ret.add(path);
				
				// we should find more routes, so lets continue 
				continue;	 // no need to make recursive call
			}
			
			// recursive call
			Collection<List<Clique>> rec = this.getRoutesRec(child, to, processingPathCurrentScope, deadNodesForMyScope);
			if (rec.isEmpty()) {
				// we recursively know that there is no path from adjacent to setTo, so, it is dead
				deadNodesForMyScope.add(child);
			} // else, we found some path
			ret.addAll(rec);
		}
		
		return ret;
	}
	
    /**
     * Finds the shortest path between two cliques.
     * This method assumes that both cliques have a common root
     * (i.e. the junction tree is actually a single tree, with a path present between all pairs of cliques).
     * This method should work faster than {@link #getPaths(Clique, Clique)},
     * because it uses several assumptions, like unique path assumption between cliques.
     * @param from : a clique to start from. This needs to be a clique in this junction tree.
     * @param to : a clique to finish search. This also needs to be in this junction tree.
     * @return : a list of sequence of cliques that forms a path between two cliques in this junction tree.
     * @see #getPaths(Clique, Clique)
     */
    public List<Clique> getPath(Clique from, Clique to) {
    	
    	// build path from the clique "to" to the root of junction tree
    	List<Clique> pathToRoot = new ArrayList<Clique>();
    	pathToRoot.add(to);
    	if (to.equals(from)) {
    		// they were the same clique, so path only contains the clique itself. Return.
    		return pathToRoot;
    	}
    	while (to.getParent() != null) {
    		to = to.getParent();
    		pathToRoot.add(to);
    		if (to.equals(from)) {
    			// we just found the correct path, so just return it, but in reverse order (because we started from the "to" clique)
    			Collections.reverse(pathToRoot);
    			return pathToRoot;
    		}
    	}
    	
    	// now go from the clique "from" to the root of junction tree.
    	// if we found any clique we visited previously (i.e. it's in pathToRoot), then we found a path.
    	List<Clique> ret = new ArrayList<Clique>();
    	int indexOfFirstMatchInPath = -1;	// this will contain the position in pathToRoot where there is a clique in common between the two paths to the root
    	do { 
    		// the 1st iteration will never match any of the cliques in path, and clique "to" is never a root of junction tree anyway,
    		// because if there is a match (or it's a root), the method should have returned already.
    		ret.add(from);
    		from = from.getParent();
		} while ((from != null) && ((indexOfFirstMatchInPath = pathToRoot.indexOf(from)) < 0));
    	if (from == null || indexOfFirstMatchInPath < 0) { // note that checking indexOfFirstMatchInPath is redundant, but we do it here just for value safety
    		// we found a root, but was not in pathToRoot (so the cliques did not share the same root) 
    		throw new IllegalArgumentException("The junction tree is expected to be a single tree (possibly with empty separators), but cliques " + from + " and " + to + " did not have a common root.");
    	}
    	
    	// Now we have ret = ["from" -> ... -> "child of common clique"], 
    	// and pathToRoot = ["to" -> ... -> "common clique" -> ... -> "root"] (note: "common clique" can be the "root" too).
    	// The path ["from" -> ... -> "child of common clique" -> "common clique" -> ... -> "to"] is the path we want to return, 
    	// and it can be obtained by getting a sublist of pathToRoot (ending at the common clique), reversing its order, and appending it at the end of ret.
    	pathToRoot = pathToRoot.subList(0, indexOfFirstMatchInPath+1);	// +1 because the common clique needs to be included
    	Collections.reverse(pathToRoot);
    	ret.addAll(pathToRoot);
    	
    	return ret;
    }

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getCliquesContainingAllNodes(java.util.Collection, int)
	 */
	public List<Clique> getCliquesContainingAllNodes(Collection<INode> nodes, int maxCount) {
		if (maxCount <= 0) {
			return new ArrayList<Clique>();
		}
		if (nodes == null || nodes.isEmpty()) {
			return new ArrayList<Clique>(this.getCliques());
		}
		List<Clique> ret = new ArrayList<Clique>();
//		for (Clique clique : this.getCliques()) {
//			if (clique.getNodes() != null && clique.getNodes().containsAll(nodes)) {
//				ret.add(clique);
//				if (ret.size() >= maxCount) {
//					return ret;
//				}
//			}
//		}
		// visit nodes in the junction tree starting from pivot
		TreeVariable pivot = (TreeVariable) nodes.iterator().next();	// use 1st node as pivot
		
		// get clique/separator connected to pivot
//		if (cliqueOrSeparatorPivot == null) {
//			cliqueOrSeparatorPivot = pivot.getAssociatedClique();
//		}
		IRandomVariable cliqueOrSeparatorPivot = pivot.getAssociatedClique();
		
		// from pivot, visit adjacent cliques in looking for the cliques containing all nodes
		if (cliqueOrSeparatorPivot instanceof Separator) {
			HashSet<Clique> visited = new HashSet<Clique>();
			// visit clique 1
			maxCount -= this.visitCliquesSeparatorsContainingAllNodesRecursive(
					((Separator)cliqueOrSeparatorPivot).getClique1(), nodes, maxCount, (List)ret, false, visited
				);
			// we do not need to visit second clique, because the above call has visited it recursively
//			// visit clique 2 if we added less than maxCount cliques into ret
//			if (maxCount > 0) {
//				this.visitCliquesSeparatorsContainingAllNodesRecursive(
//						((Separator)cliqueOrSeparator).getClique2(), nodes, maxCount, (List)ret, false, visited
//					);
//			}
		} else if (cliqueOrSeparatorPivot instanceof Clique) {
			// recursively visit clique and adjacents
			this.visitCliquesSeparatorsContainingAllNodesRecursive((Clique)cliqueOrSeparatorPivot, nodes, maxCount, (List)ret, false, new HashSet<Clique>());
		}
		return ret;
	}
	
	/**
	 * Runs the junction tree recursively looking for cliques/separators containing all nodes.
	 * @param pivot : current clique in iteration
	 * @param nodesToContain : cliques/separators shall contain all these nodes in order to be considered
	 * @param maxCount : max number of cliques to be included in output
	 * @param output : this list will be modified and contain the cliques/separators containing all nodes
	 * in nodesToContain.
	 * @param isToVisitSeparators : if true, output will be filled with separators. If false, output will be
	 * filled with cliques.
	 * @param visitedCliques : this will be used in order to track which cliques were already visited.
	 * @return how many cliques/separators were visited and added to output
	 */
	protected int visitCliquesSeparatorsContainingAllNodesRecursive(Clique pivot, Collection<INode> nodesToContain, 
			int maxCount, List<IRandomVariable> output, boolean isToVisitSeparators, Set<Clique> visitedCliques) {
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
			if (pivot.getParent() != null && !visitedCliques.contains(pivot.getParent())) {
				Separator separator = getSeparator(pivot, pivot.getParent());
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
							pivot.getParent(), nodesToContain, maxCount-addedQuantity, output, isToVisitSeparators, visitedCliques
						);
					// do not visit other cliques if we reached maxCount
					if (maxCount - addedQuantity <= 0) {
						return addedQuantity;
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
			if (pivot.getParent() != null && !visitedCliques.contains(pivot.getParent()) && !SetToolkit.containsExact(output, pivot.getParent())) {
				addedQuantity += this.visitCliquesSeparatorsContainingAllNodesRecursive(
						pivot.getParent(), nodesToContain, maxCount-addedQuantity, output, isToVisitSeparators, visitedCliques
					);
				// do not visit other cliques if we reached maxCount
				if (maxCount - addedQuantity <= 0) {
					return addedQuantity;
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
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorsContainingAllNodes(java.util.Collection, int)
	 */
	public List<Separator> getSeparatorsContainingAllNodes(Collection<INode> nodes, int maxCount) {
		if (maxCount <= 0) {
			return new ArrayList<Separator>();
		}
		if (nodes == null || nodes.isEmpty()) {
			return new ArrayList<Separator>(this.getSeparators());
		}
		List<Separator> ret = new ArrayList<Separator>();
		
		// visit nodes in the junction tree starting from pivot
		TreeVariable pivot = (TreeVariable) nodes.iterator().next();	// use 1st node as pivot
		
		// get clique/separator connected to pivot
		IRandomVariable cliqueOrSeparator = pivot.getAssociatedClique();
		
		// from pivot, visit adjacent cliques in looking for the cliques containing all nodes
		if (cliqueOrSeparator instanceof Separator) {
			// visit clique 1
			maxCount -= this.visitCliquesSeparatorsContainingAllNodesRecursive(
					((Separator)cliqueOrSeparator).getClique1(), nodes, maxCount, (List)ret, true, new HashSet<Clique>()
				);
			// visit clique 2 if we added less than maxCount cliques into ret
			if (maxCount > 0) {
				this.visitCliquesSeparatorsContainingAllNodesRecursive(
						((Separator)cliqueOrSeparator).getClique2(), nodes, maxCount, (List)ret, true, new HashSet<Clique>()
					);
			}
		} else if (cliqueOrSeparator instanceof Clique) {
			// recursively visit clique and adjacents
			this.visitCliquesSeparatorsContainingAllNodesRecursive((Clique)cliqueOrSeparator, nodes, maxCount, (List)ret, true, new HashSet<Clique>());
		}
		return ret;
	}

	/**
	 * This is available just to allow subclasses to personalize map's behavior
	 * @return the separatorsMap
	 */
	protected Map<Clique, Set<Separator>> getSeparatorsMap() {
		return this.separatorsMap;
	}

	/**
	 * This is available just to allow subclasses to personalize map's behavior
	 * @param separatorsMap the separatorsMap to set
	 */
	protected void setSeparatorsMap(Map<Clique, Set<Separator>> separatorsMap) {
		this.separatorsMap = separatorsMap;
	}

	/**
	 * If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage
	 * Turn this to false in concurrent calls of {@link #absorb(Clique, Clique)}
	 * @return the isToUseSingletonListsInAbsorb
	 */
	public static boolean isToUseSingletonListsInAbsorb() {
		return isToUseSingletonListsInAbsorb;
	}

	/**
	 * If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage.
	 * Turn this to false in concurrent calls of {@link #absorb(Clique, Clique)}
	 * @param isToUseSingletonListsInAbsorb the isToUseSingletonListsInAbsorb to set
	 */
	public static void setToUseSingletonListsInAbsorb(
			boolean isToUseSingletonListsInAbsorb) {
		JunctionTree.isToUseSingletonListsInAbsorb = isToUseSingletonListsInAbsorb;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getCliquesContainingMostOfNodes(java.util.Collection)
	 */
	public List<Clique> getCliquesContainingMostOfNodes(Collection<INode> nodes) {
		// initial assertions
		if (nodes == null || nodes.isEmpty() || this.getCliques() == null ){	// no node was specified
			return Collections.emptyList();
		}
		
		// the variable to return
		List<Clique> ret = new ArrayList<Clique>();	
		int sizeOfIntersection = 0;	// try to find clique which maximizes this variable
		
		// simply do a linear search on cliques and use the clique having the largest intersection
		// TODO find if there is a more efficient way
		for (Clique clique : this.getCliques()) {
			// extract intersection between argument and the nodes in current clique
			List<INode> intersection = new ArrayList<INode>(nodes);
			intersection.retainAll(clique.getNodesList());
			
			// check whether this has maximum size
			if (intersection.size() >= sizeOfIntersection) {
				if (intersection.size() > sizeOfIntersection) {
					// strictly greater, so do not consider the cliques previously added to ret
					ret.clear();
				}
				// NOTE: by starting sizeOfIntersection at zero, and by using ">" for comparison, we are ignoring empty intersections
				ret.add(clique);
				sizeOfIntersection = intersection.size(); 
			}
		}
		
		// NOTE: if ret is null at this point, then nodes were not present in the junction tree
		return ret;
	}

	/**
	 * This is here only for backward compatibility.
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorsSize()
	 * @deprecated use {@link #getSeparators()} and then {@link Collection#size()} instead.
	 */
	public int getSeparatorsSize() {
		return this.getSeparators().size();
	}

	/**
	 * This is here only for backward compatibility.
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorAt(int)
	 * @deprecated use {@link #getSeparator(Clique, Clique)}, {@link #getSeparatorsContainingAllNodes(Collection, int)}, or 
	 * {@link #getSeparators()} (the latter is for iteration).
	 */
	public Separator getSeparatorAt(int index) {
		if (separators instanceof List) {
			return (Separator) ((List)separators).get(index);
		}
		// Treat index as if it were the internal id to find. Find separator whose internal id is index
		for (Separator separator : separators) {
			int id = separator.getInternalIdentificator();
			if (id >= 0 && id == index) {
				// in this case, ids of the separators start from 0, and then goes 1, 2, 3...
				return separator;
			}
			if (id < 0 && -(id+1) == index) {
				// in this case, ids of the separators start from -1, and then goes -2, -3, -4...
				// so, if id == -1, then -(id+1) ==0. If id == -2, then -(id+1) == 1, and so on. By doing this, we can compare with index.
				return separator;
			}
		}
		// iterate to index, if this is a non-indexed collection.
		int i = 0;
		for (Separator separator : separators) {
			if (i == index) {
				return separator;
			}
			i++;
		}
		return null;
	}

	/**
	 * Reorganizes the junction tree hierarchy so that the given cluster becomes the root of the tree it belongs.
	 * The root of the tree it belongs is the nearest clique that we obtain by iteratively visiting {@link Clique#getParent()} 
	 * until we get {@link Clique#getParent()} == null.
	 * It will recursively move {@link Clique#getParent()} to {@link Clique#getChildren()} until
	 * the clique becomes a root clique.
	 * {@link #getSeparators()} and {@link #getSeparatorsMap()} may get changed, because
	 * some algorithms expects {@link Separator#getClique1()} to be a parent clique 
	 * (and this method may reverse such hierarchy).
	 * @param cliqueToBecomeRoot : this clique will become the root of the tree it belongs.
	 * @see Clique#getChildren()
	 */
	public void moveCliqueToRoot(Clique cliqueToBecomeRoot) {
		// basic assertion and stop condition
		if (cliqueToBecomeRoot == null || cliqueToBecomeRoot.getParent() == null) {
			return;	// there is nothing to do
		}
		
		// extract the parent clique, so that we can set it as a child of current clique
		Clique parentClique = cliqueToBecomeRoot.getParent();
		
		// call recursive first, so that my immediate parent becomes the root of the tree
		moveCliqueToRoot(parentClique);
		
		// at this point, parentClique is the root of the tree this clique belongs
		
		// convert parent clique to child of current clique
		parentClique.removeChild(cliqueToBecomeRoot);
		cliqueToBecomeRoot.addChild(parentClique);
		parentClique.setParent(cliqueToBecomeRoot);
		
		// some of the algorithms expects the 1st clique of separator to be a parent, and the other to be a child, 
		// so we need to revert order in separator too. Separator does not allow such changes, so we need to substitute separator
		Separator oldSeparator = getSeparator(parentClique, cliqueToBecomeRoot);	// this represents the link parentClique->cliqueToBecomeRoot
		removeSeparator(oldSeparator);	// delete this separator, so that we can include a new one
		
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
		
		
		// become the new root
		cliqueToBecomeRoot.setParent(null);
		
	}

	/**
     * This method moves the root clique to 1st index, and then updates {@link IRandomVariable#getInternalIdentificator()}
     * accordingly to its order of appearance in {@link IJunctionTree#getCliques()} and {@link IJunctionTree#getSeparators()}.
     * This is necessary because some implementations assumes that {@link IRandomVariable#getInternalIdentificator()} is synchronized with indexes.
	 */
	public void updateCliqueAndSeparatorInternalIdentificators() {
    	if (cliques != null && !cliques.isEmpty()) {
    		// move the new root clique to the 1st entry in the list of cliques in junction tree, because some algorithms assume the 1st element is the root;
    		Clique root = cliques.get(0);
    		while (root.getParent() != null) { 
    			root = root.getParent(); // go up in hierarchy until we find the root
    		}
    		int indexOfRoot = cliques.indexOf(root);
    		if (indexOfRoot > 0) {
    			// move root to the beginning (index 0) of the list
    			Collections.swap(cliques, 0, indexOfRoot);
    		}
    		
    		// redistribute internal identifications accordingly to indexes
    		for (int i = 0; i < cliques.size(); i++) {
    			cliques.get(i).setIndex(i);
    			cliques.get(i).setInternalIdentificator(i);
    		}
    	}
    	// do the same for separators
    	int separatorIndex = -1;
    	for (Separator sep : separators) {
    		sep.setInternalIdentificator(separatorIndex--);
    	}
	}
	
	/**
	 * Simply updates {@link Clique#getParent()}
	 * @param parent : the clique to become parent
	 * @param child : the clique to become child
	 * @return true if there was some change. False otherwise.
	 * @see #getParents(Clique)
	 */
	public boolean addParent(Clique parent, Clique child) {
		Clique oldParent = child.getParent();
		child.setParent(parent);
		return parent == oldParent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#removeCliques(java.util.Collection)
	 */
	public boolean removeCliques(Collection<Clique> cliques) {
		// basic assertion
		if (cliques == null || cliques.isEmpty()) {
			return false;	// there was nothing to remove
		}
		
		boolean ret = false;
		
		// we need to remove each clique and connect children to parent
		for (Clique cliqueToRemove : cliques) {
			if (cliqueToRemove == null) {
				continue;
			}
			// extract children
			List<Clique> children = cliqueToRemove.getChildren();
			if (children == null) {	// just make sure the list of children is never null
				children = Collections.emptyList();
			}
			
			// handle parent
			List<Clique> parents = new ArrayList(getParents(cliqueToRemove));
			for (Clique parent : parents) {
				// disconnect from parent
				parent.removeChild(cliqueToRemove);
				// delete separator between empty clique to delete and its parent
				this.removeSeparator(this.getSeparator(parent, cliqueToRemove));
				
				if (!children.isEmpty()) {
					// make each children to point to parent, instead of to the clique that will be deleted
					for (Clique child : children) {
						// extract/create the old/new separators in advance
						Separator separatorToRemove = this.getSeparator(cliqueToRemove, child);
						Separator separatorToInclude = new Separator(parent, child, false);	// false:=don't update relationship between child and parent
						separatorToInclude.setInternalIdentificator(separatorToRemove.getInternalIdentificator());	// reuse ID
//						separatorToInclude.setNodes(separatorToRemove.getNodesList());
//						separatorToInclude.setProbabilityFunction(separatorToRemove.getProbabilityFunction());
						
						// delete separators between empty clique and its children
						this.removeSeparator(separatorToRemove);
						
						// update the references of parent/child
						this.addParent(parent, child);
						parent.addChild(child);
						
						// create empty separators from parent clique to children
						this.addSeparator(separatorToInclude);
					}
				} // or else, there was no children. Empty clique was a leaf, so no need to process children.
			}
			
			// handle case when clique to delete is a root
			if ( (parents == null || parents.isEmpty())
					&& !children.isEmpty()) {
				// parent is null, and there are children
				
				// the empty clique was a root (but it will be removed), so pick one (any) children to become a new root
				Clique parent = children.get(0);
				parent.setParent(null);	  // this will make sure the new parent is a root, and also disconnect it from empty clique.
				
				// also make sure the separator between the new root and empty clique is removed
				this.removeSeparator(this.getSeparator(cliqueToRemove, parent));
				
				// get the remaining children
				if (children.size() > 1) {
					children = children.subList(1, children.size());  // don't modify original list
				} else {	// there was only 1 child, and it became a parent
					children = Collections.emptyList();	// there is no remaining children
				}
				
				// connect remaining children (i.e. brothers) to new root, by using empty separators
				for (Clique child : children) {
//					// create separator from parent to children. 
//					// Separator(Clique,Clique) will also update Clique#getParent() of child clique, and Clique#getChildren() of parent clique
//					this.addSeparator(new Separator(parent, child));
//					// also make sure the separator between this child and empty clique is removed
//					this.removeSeparator(this.getSeparator(cliqueToRemove, child));
					
					// extract/create the old/new separators in advance
					Separator separatorToRemove = this.getSeparator(cliqueToRemove, child);
					Separator separatorToInclude = new Separator(parent, child, false);	// false:=don't update relationship between child and parent
					separatorToInclude.setInternalIdentificator(separatorToRemove.getInternalIdentificator());	// reuse ID
//					separatorToInclude.setNodes(separatorToRemove.getNodesList());
//					separatorToInclude.setProbabilityFunction(separatorToRemove.getProbabilityFunction());
					
					// delete separators between empty clique and its children
					this.removeSeparator(separatorToRemove);
					
					// update the references of parent/child
					this.addParent(parent, child);
					parent.addChild(child);
					
					// create empty separators from parent clique to children
					this.addSeparator(separatorToInclude);
				}
				
				// some algorithms require the root clique to be the 1st in list, so reorder
				int indexOfNewRoot = this.getCliques().indexOf(parent);
				if (indexOfNewRoot > 0) {
					// swap with clique at index 0 (probably, this will swap with empty clique)
					Collections.swap(this.getCliques(), 0, indexOfNewRoot);	
				}
				
			}	// or else, the empty clique was the only clique in the junction tree
			
			// finally, remove the empty clique from the list of cliques in junction tree
			if (this.getCliques().remove(cliqueToRemove)) {
				ret = true;
			}
			
		}	// end of for each empty clique
		
		// rebuild indexes, because some methods assumes that internal identificators and indexes are the same
		this.updateCliqueAndSeparatorInternalIdentificators();
		
		return ret;
		
	}
	
	/**
	 * This method will look for predecessor by recursively checking {@link #getParents(Clique)}.
	 * @see unbbayes.prs.bn.IJunctionTree#isPredecessor(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	public boolean isPredecessor(Clique predecessorToTest, Clique clique) {
		
		// basic assertions
		if (clique == null || predecessorToTest == null) {
			return false;
		}
		
		// check for parents
		List<Clique> parents = getParents(clique);	// this is empty if current clique is a root
		if (parents != null) {	// this check should not be necessary, but I'm doing it just for precaution (because classes extending getParents may return null here)
			for (Clique parent : parents) {
				
				// if it is a parent, then it is also a predecessor
				if (parent.equals(predecessorToTest)) {
					return true;
				}
				
				// check predecessors recursively
				if (isPredecessor(predecessorToTest, parent)) {
					// predecessorToTest is a predecessor of my parent, so it is also my predecessor
					return true;
				}
			}
		}
		
		// at this point, we did not find predecessorToTest in the set of all predecessors of clique
		return false;
	}

	/**
	 * Extracts the parent of the provided clique.
	 * This is useful when the {@link IJunctionTree} structure
	 * allows multiple parents (e.g. loopy clique structures)
	 * @param clique : parents of this clique will be searched.
	 * @return parents of the clique. In a tree structure, this will contain only 1 element.
	 * @see Clique#getParent()
	 */
	public List<Clique> getParents(Clique clique) {
		if (clique == null || clique.getParent() == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(clique.getParent());
	}

	/**
	 * @return true if {@link #absorb(Clique, Clique)} should assume that ordering of variables in cliques follow same ordering, 
	 * so we can copy contents of clique tables (of same set of nodes) without being concerned about the ordering of variables
	 * (and thus use quick array copy).
	 * False otherwise.
	 * <br/> <br/>
	 * For example, if cliques are {A,B,C}, and {B,C,D}, they are using safe ordering (because common variables follows the same order),
	 * and thus this attribute must be set to true. 
	 * If the same cliques are {A,C,B}, and {B,C,D}, then nodes B and C are not following the same order in the two cliques,
	 * and thus this attribute must be set to false.
	 */
	public boolean isUsingSafeOrdering() {
		return isUsingSafeOrdering;
	}

	/**
	 * @param isUsingSafeOrdering : true if {@link #absorb(Clique, Clique)} should assume that ordering of variables in cliques follow same ordering, 
	 * so we can copy contents of clique tables (of same set of nodes) without being concerned about the ordering of variables
	 * (and thus use quick array copy).
	 * False otherwise.
	 * <br/> <br/>
	 * For example, if cliques are {A,B,C}, and {B,C,D}, they are using safe ordering (because common variables follows the same order),
	 * and thus this attribute must be set to true. 
	 * If the same cliques are {A,C,B}, and {B,C,D}, then nodes B and C are not following the same order in the two cliques,
	 * and thus this attribute must be set to false.
	 */
	public void setUsingSafeOrdering(boolean isUsingSafeOrdering) {
		this.isUsingSafeOrdering = isUsingSafeOrdering;
	}

	/**
	 * This value indicates whether the belief tables of this junction tree were initialized or not.
	 * If they were initialized already, implementations can use cached values instead of re-initializing beliefs again.
	 * @return if true, {@link #initBeliefs()} will simply call {@link #restoreTableData()} to get beliefs from cache.
	 * If false, then {@link #initBeliefs()} will  perform the full initialization process: 
	 * call {@link #initBelief(Clique)}, call {@link #initBelief(Separator)}, call {@link #initConsistency()}, 
	 * and {@link #copyTableData()}.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * This value indicates whether the belief tables of this junction tree were initialized or not.
	 * If they were initialized already, implementations can use cached values instead of re-initializing beliefs again.
	 * @param initialized : if true, {@link #initBeliefs()} will simply call {@link #restoreTableData()} to get beliefs from cache.
	 * If false, then {@link #initBeliefs()} will  perform the full initialization process: 
	 * call {@link #initBelief(Clique)}, call {@link #initBelief(Separator)}, call {@link #initConsistency()}, 
	 * and {@link #copyTableData()}.
	 */
	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * @see unbbayes.prs.bn.IJunctionTree#getCliquesConnectedToNodes(java.util.Collection, java.util.Collection)
	 * @see Collection#contains(Object)
	 */
	public Collection<Clique> getCliquesConnectedToNodes( Collection<INode> nodes, Collection<Clique> cliquesToIgnore) {
		// TODO this method is sub-optimal due to recursive calls, although recursive procedures are usually easier to analyze/maintain
		
		// basic assertion
		if (nodes == null || nodes.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		
		// make sure the collection of cliques to ignore is non-null, because we'll use it as a common context between searches for each node.
		if (cliquesToIgnore == null) {
			cliquesToIgnore = Collections.EMPTY_SET;
		}
		
		// this will be the set to be returned by this method
		Collection<Clique> ret = new HashSet<Clique>();
		
		if (nodes.size() == 1) {
			// first, search for a clique containing the node, 
			
			// this is the node to look for
			INode node = nodes.iterator().next();
			
			// find 1st clique containing node, so that we can use it as a pivot
			Clique pivot = null;
			// don't use TreeVariable#getAssociatedClique, because we don't know if this node is associated with a clique in this junction tree
			// #getCliquesContainingAllNodes(nodes, maxCount) uses TreeVariable#getAssociatedClique internally, so I don't want to use it.
			for (Clique clique : getCliques()) {
				if (clique.getNodesList().contains(node)) {
					pivot = clique;
					break;
				}
			}
			
			// ret will be used as an input/output argument for the next method. 
			// The input is the cliquesToIgnore, and the output will be the new cliques inserted to ret.
			ret.addAll(cliquesToIgnore);
			
			// visit neighbor cliques recursively. Stop if we reached any clique in cliquesToIgnore;
			// insert connected cliques (those that can be reached without passing through empty separator) into the collection to return;
			this.visitConnectedCliquesRecursive(pivot, ret); 
			
			// we don't want cliquesToIgnore to be in the collection to return
			ret.removeAll(cliquesToIgnore);
			
		} else {
			// for multiple nodes, just run this method for each node, but incrementing cliquesToIgnore
			// use a copy, because we'll modify the content, but we don't want to change the original
			cliquesToIgnore = new HashSet<Clique>(cliquesToIgnore);
			
			for (INode node : nodes) {
				Collection<Clique> cliquesCurrentIteration = this.getCliquesConnectedToNodes(Collections.singletonList(node), cliquesToIgnore);
				// the value to return is the aggregation of cliques returned by calling this method for each node
				ret.addAll(cliquesCurrentIteration);
				// don't consider these cliques in next iteration, because they were considered already and don't need to be re-added
				cliquesToIgnore.addAll(cliquesCurrentIteration);	
			}
		}
		
		return ret;
	}

	/**
	 * Obtains a collection of cliques that can be reached from a pivot
	 * without passing through an empty separator.
	 * @param pivot : the clique to start recursive search
	 * @param visitedCliques : this is an input/output argument that will be filled
	 * with the cliques that could be reached from pivot without reaching an empty separator.
	 * If a clique in this collection was reached, the recursive search will stop
	 * (thus this can also avoid double-visiting the same subtree).
	 */
	private void visitConnectedCliquesRecursive(Clique pivot, Collection<Clique> visitedCliques) {
		// basic assertion
		if (pivot == null) {
			return;
		}
		
		// stop condition
		if (visitedCliques == null || visitedCliques.contains(pivot)) {
			return; // visited already, or cannot fill output argument anyway, so stop
		}
		
		// mark pivot as visited already, so that recursive calls to parent/children won't visit this again
		visitedCliques.add(pivot);
		
		// recursively visit parent(s)
		List<Clique> parents = getParents(pivot);
		if (parents != null) {
			// This list will have more than 1 element if instance of JunctionTree is not actually a tree 
			// (e.g. when the instance is actually a directed graph of cliques).
			for (Clique parent : parents) {
				// check separator between the cliques
				Separator separator = getSeparator(parent, pivot);
				if (separator == null || separator.getNodesList() == null || separator.getNodesList().isEmpty()) {
					// don't consider path that contains an empty separator (i.e. don't consider subtree of junction tree disconnected from pivot)
					continue;
				}
				// this will fill visitedCliques with all cliques that we can reach from parent, without passing through empty separators
				visitConnectedCliquesRecursive(parent, visitedCliques);
			}
		}
		
		// recursively visit children too
		List<Clique> children = pivot.getChildren();
		if (children != null) {
			for (Clique child : children) {
				// check separator between the cliques
				Separator separator = getSeparator(pivot , child);
				if (separator == null || separator.getNodesList() == null || separator.getNodesList().isEmpty()) {
					// don't consider path that contains an empty separator (i.e. don't consider subtree of junction tree disconnected from pivot)
					continue;
				}
				// this will fill visitedCliques with all cliques that we can reach from parent, without passing through empty separators
				visitConnectedCliquesRecursive(child, visitedCliques);
			}
		}
		
	}
	
	/**
	 * Checks whether there is a path between two cliques in current junction tree
	 * whose all separators in the path contains the specified node.
	 * @param from : the clique to start search from
	 * @param to : the clique to search. If this is equal to the clique to start search from,
	 * then this method will immediately return the list <code> [from,to] </code>.
	 * @param nodesInPath : all the separators between the two cliques must contain these nodes.
	 * If null or empty, then the separators can be arbitrary.
	 * @return The path if it exists. Empty otherwise.
	 */
	public List<Clique> getPathContainingNodes(Clique from, Clique to, Collection<INode> nodesInPath) {
		// basic assertions
//		if (from == null || to == null) {
//			return Collections.emptyList();
//		}
//		if (from.equals(to)) {
//			return Collections.singletonList(from);
//		}
//		
		// the above code was migrated to recursive search
		
		// make sure nodesInPath is non-null
		if (nodesInPath == null) {
			nodesInPath = Collections.emptyList();
		}
		
		// do a recursive search
		List<Clique> path = new ArrayList<Clique>();
		if (hasPathContainingNodesRecursive(from, to, nodesInPath, path)) {
			// found path
			return path;
		}
		
		// did not find path
		return Collections.emptyList();
	}
	
	/**
	 * This is a recursive search used in {@link #getPathContainingNodes(Clique, Clique, Collection)}
	 */
	protected boolean hasPathContainingNodesRecursive(Clique from, Clique to, Collection<INode> nodesInPath, List<Clique> currentPath) {
		// basic assertions
		if (from == null || to == null) {
			return false;
		}
		
		// make current clique as visited
		currentPath.add(from);
		
		if (from.equals(to)) {
			return true;
		}
		
		// call recursive for parents
		for (Clique parent : this.getParents(from)) {
			Separator separator = this.getSeparator(parent, from);
			if (separator == null || !separator.getNodesList().containsAll(nodesInPath)) {
				continue;	// ignore path not containing nodes
			}
			if (currentPath.contains(parent)) {
				continue;	// ignore path we already visited
			}
			
			List<Clique> newPath = new ArrayList<Clique>(currentPath);
			if (hasPathContainingNodesRecursive(parent, to, nodesInPath, newPath)) {
				// add all new cliques we found in newPath to currentPath
				currentPath.addAll(newPath.subList(currentPath.size(), newPath.size()));
				return true;
			}
		}
		
		// call recursive for children
		List<Clique> newPath = new ArrayList<Clique>();
		for (Clique child : from.getChildren()) {
			Separator separator = this.getSeparator(from, child);
			if (separator == null || !separator.getNodesList().containsAll(nodesInPath)) {
				continue;	// ignore path not containing nodes
			}
			if (currentPath.contains(child)) {
				continue;	// ignore path we already visited
			}
			
			newPath.clear();
			newPath.addAll(currentPath);
			if (hasPathContainingNodesRecursive(child, to, nodesInPath, newPath)) {
				// add all new cliques we found in newPath to currentPath
				currentPath.addAll(newPath.subList(currentPath.size(), newPath.size()));
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#isUsingApproximation()
	 */
	public boolean isUsingApproximation() {
		return false;
	}

//	/**
//	 * @param separatorsMap the separatorsMap to set
//	 */
//	public void setSeparators(List<Separator> separatorsMap) {
//		this.separators = separatorsMap;
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see unbbayes.prs.bn.IJunctionTree#removeSeparator(unbbayes.prs.bn.Separator)
//	 */
//	public void removeSeparator(Separator sep) {
//		this.separators.remove(sep);
//	}
}