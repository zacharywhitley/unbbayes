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
import java.util.List;

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
	
	
	private boolean initialized;

	private float totalEstimatedProb;

	
	private List<Clique> cliques;

	/**
	 *  Lista de Separadores Associados.
	 */
	private List<Separator> separators;

//	/**
//	 * Pre-calculated coordinates for optimizing the method absorb
//	 */
//	private int coordSep[][][];

	/**
	 * Default constructor for juction tree. It initializes the list {@link #getCliques()}
	 * and the separators obtainable from {@link #getSeparator(Clique, Clique)},
	 * {@link #getSeparatorAt(int)}, {@link #getSeparatorsSize()}
	 */
	public JunctionTree() {
		separators = new ArrayList<Separator>();
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
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorsSize()
	 */
	public int getSeparatorsSize() {
		return separators.size();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparatorAt(int)
	 */
	public Separator getSeparatorAt(int index) {
		return separators.get(index);
	}

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
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			if (auxClique.getChildrenSize() != 0) {
				this.coleteEvidencia(auxClique);
			}
			
//			Separator sep = getSeparator(clique, auxClique); 
//			clique.absorb(auxClique, sep.getPotentialTable());
			if (auxClique.getProbabilityFunction().getVariablesSize() > 1) {
				absorb(clique, auxClique);
			} else {
				// a clique with only 1 variable is equivalent to a BN with only 1 node (there is no prob dist at all), so no need to propagate
			}
		}

		totalEstimatedProb *= clique.normalize();
	}

	/**
	 * Processes the distribution of evidences.
	 * It distributes potentials to child cliques
	 *@param  clique  clique.
	 */
	protected void distributeEvidences(Clique clique) {
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			
//			Separator sep = getSeparator(clique, auxClique); 
//			auxClique.absorb(clique, sep.getPotentialTable());
			if (auxClique.getProbabilityFunction().getVariablesSize() > 1) {
				absorb(auxClique, clique);
			} else {
				// a clique with only 1 variable is equivalent to a BN with only 1 node (there is no prob dist at all), so no need to propagate
			}
			if (auxClique.getChildrenSize() != 0) {
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
			Debug.println(getClass(), clique1 + " and " + clique2 + " are disconnected.");
			return;
		}
		PotentialTable sepTab = sep.getProbabilityFunction();
		ArrayList<Node> toDie = SetToolkit.clone(clique2.getNodes());
		if (sepTab.tableSize() <= 0) {
			Debug.println(getClass(), clique1 + " and " + clique2 + " has empty separator.");
			return;
		}
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
			sepTab.setValue(i, dummyTable.getValue(i));
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
	
			Separator auxSep;
			int sizeSeparadores = separators.size();
			for (int k = 0; k < sizeSeparadores; k++) {
				auxSep = (Separator) separators.get(k);
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
		
		int sizeSeparadores = separators.size();
		for (int k = 0; k < sizeSeparadores; k++) {
			Separator auxSep = (Separator) separators.get(k);
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
		
		int sizeSeparadores = separators.size();
		for (int k = 0; k < sizeSeparadores; k++) {
			Separator auxSep = (Separator) separators.get(k);
			auxSep.getProbabilityFunction().copyData();
			auxSep.getUtilityTable().copyData();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparator(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	public Separator getSeparator(Clique clique1, Clique clique2) {
		int sizeSeparadores = separators.size();
		for (int indSep = 0; indSep < sizeSeparadores; indSep++) {
			Separator separator = (Separator) separators.get(indSep);
			if (((separator.getClique1() == clique1) && (separator.getClique2() == clique2))
				|| ((separator.getClique2() == clique1) && (separator.getClique1() == clique2))) {
				return separator;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTree#getSeparators()
	 */
	public List<Separator> getSeparators() {
		return separators;
	}

	/**
	 * @param separators the separators to set
	 */
	public void setSeparators(List<Separator> separators) {
		this.separators = separators;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see unbbayes.prs.bn.IJunctionTree#removeSeparator(unbbayes.prs.bn.Separator)
//	 */
//	public void removeSeparator(Separator sep) {
//		this.separators.remove(sep);
//	}
}