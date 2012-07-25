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
	private Set<Separator> separators = new HashSet<Separator>();
	private Map<Clique, Set<Separator>> separatorsMap = new HashMap<Clique, Set<Separator>>();

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
	
	/*
	 * (non-Javadoc)
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
	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#consistency()
	 */
	public void consistency() throws Exception {
		totalEstimatedProb = 1;
		Clique raiz = cliques.get(0);
		coleteEvidencia(raiz);
		distributeEvidences(raiz);
	}

	/**
	 * Processes the collection of evidences.
	 * It absorbs child cliques.
	 *@param  clique  clique.
	 */
	protected void coleteEvidencia(Clique clique) throws Exception {
		for (Clique auxClique : clique.getChildren()) {
			this.coleteEvidencia(auxClique);
			
//			Separator sep = getSeparator(clique, auxClique); 
//			clique.absorb(auxClique, sep.getPotentialTable());
			absorb(clique, auxClique);
		}

		totalEstimatedProb *= clique.normalize();
	}

	/**
	 * Processes the distribution of evidences.
	 * It distributes potentials to child cliques
	 *@param  clique  clique.
	 */
	protected void distributeEvidences(Clique clique) {
		for (Clique auxClique : clique.getChildren()) {
			
//			Separator sep = getSeparator(clique, auxClique); 
//			auxClique.absorb(clique, sep.getPotentialTable());
			absorb(auxClique, clique);
			if (!auxClique.getChildren().isEmpty()) {
				distributeEvidences(auxClique);
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
		ArrayList<Node> toDie = SetToolkit.clone(clique2.getNodes());
		for (int i = 0; i < sepTab.variableCount(); i++) {
			toDie.remove(sepTab.getVariableAt(i));			
		}

		PotentialTable dummyTable =
			(PotentialTable) clique2.getProbabilityFunction().clone();
			
		for (int i = 0; i < toDie.size(); i++) {
			dummyTable.removeVariable(toDie.get(i));
		}

		PotentialTable originalSeparatorTable =
			(PotentialTable) sepTab.clone();

		for (int i = sepTab.tableSize() - 1; i >= 0; i--) {
			sepTab.setValue(i, dummyTable.getDoubleValue(i));
		}

		dummyTable.directOpTab(
			originalSeparatorTable,
			PotentialTable.DIVISION_OPERATOR);

		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);
    }
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#initBeliefs()
	 */
	public void initBeliefs() throws Exception {
		if (! initialized) {
			Clique auxClique;
	
			int sizeCliques = cliques.size();
			for (int k = 0; k < sizeCliques; k++) {
				auxClique = cliques.get(k);
				this.initBelief(auxClique);
			}
	
			for (Separator auxSep : getSeparators()) {
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
		Set<Separator> seps = separatorsMap.get(clique1);
		if (seps != null) {
			for (Separator separator : seps) {
				if (separator.getClique2().equals(clique2) || separator.getClique1().equals(clique2)) {
					return separator;
				}
			}
		}
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
		// TODO optimize using indexing or other algorithms instead of linear search
		List<Clique> ret = new ArrayList<Clique>();
		for (Clique clique : this.getCliques()) {
			if (clique.getNodes() != null && clique.getNodes().containsAll(nodes)) {
				ret.add(clique);
				if (ret.size() >= maxCount) {
					return ret;
				}
			}
		}
		return ret;
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