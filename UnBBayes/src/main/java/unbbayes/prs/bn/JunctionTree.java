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
import unbbayes.util.SetToolkit;

/**
 * This class represents a junction tree for Bayes Nets.
 *@author     Michael
 *@author     Rommel
 */
public class JunctionTree implements java.io.Serializable, IJunctionTree {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	
	
	private boolean initialized;

	private float totalEstimatedProb;

	
	private List<Clique> cliques;

	/**
	 *  List of Associated separatorsMap.
	 */
	private Collection<Separator> separators = new ArrayList<Separator>();
	private Map<Clique, Set<Separator>> separatorsMap = new HashMap<Clique, Set<Separator>>();

	/** If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage */
	private static boolean isToUseSingletonListsInAbsorb = true;
	
	/** This is the singleton temporary list used by {@link #absorb(Clique, Clique)} when {@link #isToUseSingletonListsInAbsorb()} == true */
	private static List<Node> singletonListForAbsorb = new ArrayList<Node>(7);

//	/**
//	 * Pre-calculated coordinates for optimizing the method absorb
//	 */
//	private int coordSep[][][];

	/**
	 * Default constructor for juction tree. It initializes the list {@link #getCliques()}
	 * and the separatorsMap obtainable from {@link #getSeparator(Clique, Clique)},
	 * {@link #getSeparatorAt(int)}, {@link #getSeparatorsSize()}
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
	 * Basically, this method propagates evidences by calling
	 * {@link #coleteEvidencia(Clique)} and {@link #distributeEvidences(Clique)},
	 * passing as their arguments the clique specified in the argument.
	 * @param rootClique: this clique will be considered as the root node
	 * of the junction tree during this propagation, and evidences will
	 * only be propagated across this node and descendants.
	 * This is useful in order to force the propagation to be limited
	 * to a subtree of the junction tree, specially if such subtree is supposedly
	 * disconnected from other portions, and no other evidence is expected in other
	 * cliques disconnected from the current clique.
	 * If null, then the 1st clique in {@link #getCliques()} will be used.
	 * @param isToContinueOnEmptySep : if false, propagation will not recursively
	 * guarantee global consistency to subtrees connected with empty separators.
	 * If evidences are expected to be across several cliques, set this to true,
	 * otherwise, if evidences are expected to be present only at the subtree
	 * of rootClique, then set this as false.
	 * @throws Exception
	 * @see unbbayes.prs.bn.IJunctionTree#consistency()
	 * @see {@link #consistency()}
	 * @see #coleteEvidencia(Clique)
	 * @see #distributeEvidences(Clique)
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
	 * Processes the collection of evidences.
	 * It absorbs child cliques.
	 *@param  clique  clique.
	 */
	protected void collectEvidence(Clique clique, boolean isToContinueOnEmptySep) throws Exception {
		for (Clique auxClique : clique.getChildren()) {
			if (isToContinueOnEmptySep) {
				// call recursive regardless of separator
				this.collectEvidence(auxClique,isToContinueOnEmptySep);
				absorb(clique, auxClique);
			} else {
				// call recursive only if separator can propagate evidence (non-empty and more than 1 state)
				Separator sep = getSeparator(clique, auxClique); 
				if (sep.getProbabilityFunction().tableSize() > 1) {
					this.collectEvidence(auxClique,isToContinueOnEmptySep);
					absorb(clique, auxClique);
				}
			}
		}

		totalEstimatedProb *= clique.normalize();
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
			nodesInClique2NotInSeparator.addAll(clique2.getNodes());
			nodesInClique2NotInSeparator.removeAll(sep.getNodes());
		} else {
			nodesInClique2NotInSeparator = new ArrayList<Node>(clique2.getNodes());
			nodesInClique2NotInSeparator.removeAll(sep.getNodes());
		}

//		PotentialTable dummyTable =
//			(PotentialTable) clique2.getProbabilityFunction().clone();
		PotentialTable dummyTable =
				(PotentialTable) clique2.getProbabilityFunction().getTemporaryClone();
		
		for (Node nodeToRemove : nodesInClique2NotInSeparator) {
			dummyTable.removeVariable(nodeToRemove);
		}
		

//		PotentialTable originalSeparatorTable =
//			(PotentialTable) sepTab.clone();
		PotentialTable originalSeparatorTable =
				(PotentialTable) sepTab.getTemporaryClone();

		for (int i = sepTab.tableSize() - 1; i >= 0; i--) {
			sepTab.setValue(i, dummyTable.getValue(i));
		}

		dummyTable.directOpTab(
			originalSeparatorTable,
			PotentialTable.DIVISION_OPERATOR);

		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);
    }
	
	/**
	 * This method also updates the {@link IRandomVariable#getInternalIdentificator()}
	 * @see unbbayes.prs.bn.IJunctionTree#initBeliefs()
	 */
	public void initBeliefs() throws Exception {
		if (! initialized) {
			Clique auxClique;
	
			int sizeCliques = cliques.size();
			for (int k = 0; k < sizeCliques; k++) {
				auxClique = cliques.get(k);
				// also, update the internal id, for fast comparison
				auxClique.setInternalIdentificator(k);
				this.initBelief(auxClique);
			}
	
			int separatorId = -1;
			for (Separator auxSep : getSeparators()) {
				// also, update the internal id, for fast comparison
				auxSep.setInternalIdentificator(separatorId--);
				this.initBelief(auxSep);
			}
			
			consistency();
			copyTableData();
			initialized = true;
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
		int sizeDados = auxTabPot.tableSize();
		for (int c = 0; c < sizeDados; c++) {
			auxTabPot.setValue(c, 1);
		}

		// initialize table related to utility nodes
		PotentialTable auxUtilTab = auxSep.getUtilityTable();
		sizeDados = auxUtilTab.tableSize();
		for (int i = 0; i < sizeDados; i++) {
			auxUtilTab.setValue(i, 0);
		}
		
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
		Clique theOtherClique = clique2;	// the clique other that the one used to extract separator
		
		// compare with size of separators reachable from clique 2, because we will do linear search in case of multiple separators
		Set<Separator> seps2 = separatorsMap.get(clique2);	// separators reachable from clique 2
		if (seps2.size() < seps.size()) {
			// use separators reachable from clique 2, because it's smaller, and set the other clique as clique 1
			seps = seps2;
			theOtherClique = clique1;	
		}
		
		if (seps != null) {
			for (Separator separator : seps) {
//				if (separator.getClique2().equals(clique2) || separator.getClique1().equals(clique2)) {
				if (separator.getClique2().getInternalIdentificator() == theOtherClique.getInternalIdentificator()
						|| separator.getClique1().getInternalIdentificator() == theOtherClique.getInternalIdentificator()) {
					return separator;
				}
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
		IRandomVariable cliqueOrSeparator = pivot.getAssociatedClique();
		
		// from pivot, visit adjacent cliques in looking for the cliques containing all nodes
		if (cliqueOrSeparator instanceof Separator) {
			HashSet<Clique> visited = new HashSet<Clique>();
			// visit clique 1
			maxCount -= this.visitCliquesSeparatorsContainingAllNodesRecursive(
					((Separator)cliqueOrSeparator).getClique1(), nodes, maxCount, (List)ret, false, visited
				);
			// we do not need to visit second clique, because the above call has visited it recursively
//			// visit clique 2 if we added less than maxCount cliques into ret
//			if (maxCount > 0) {
//				this.visitCliquesSeparatorsContainingAllNodesRecursive(
//						((Separator)cliqueOrSeparator).getClique2(), nodes, maxCount, (List)ret, false, visited
//					);
//			}
		} else if (cliqueOrSeparator instanceof Clique) {
			// recursively visit clique and adjacents
			this.visitCliquesSeparatorsContainingAllNodesRecursive((Clique)cliqueOrSeparator, nodes, maxCount, (List)ret, false, new HashSet<Clique>());
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