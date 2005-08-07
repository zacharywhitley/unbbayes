/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.id.*;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Classe que representa uma Árvore de Junção para Redes Bayesianas.
 *
 *@author     Michael
 *@author     Rommel
 */
public class JunctionTree implements java.io.Serializable {
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	/**
	 *  Probabilidade total estimada.
	 */
	private float n;

	/**
	 *  Lista de Cliques Associados.
	 */
	private List cliques;

	/**
	 *  Lista de Separadores Associados.
	 */
	private List separators;

	/**
	 *  Contrói uma nova árvore de junção. Inicializa a lista de separadores e
	 *  cliques.
	 */
	public JunctionTree() {
		separators = new ArrayList();
		cliques = new ArrayList();
	}

	/**
	 * Retorna a probabidade total estimada
	 *
	 * @return probabilidade total estimada
	 */
	public float getN() {
		return n;
	}
	
	public void addSeparator(Separator sep) {
		separators.add(sep);
	}

	public int getSeparatorsSize() {
		return separators.size();
	}

	public Separator getSeparatorAt(int index) {
		return (Separator) separators.get(index);
	}

	/**
	 *  Retorna o List com os cliques associados.
	 *
	 *@return    Vetor com os cliques associados.
	 */
	public List getCliques() {
		return cliques;
	}
	

	/**
	 *  Verifica a consistência global.
	 *  Aplica o algoritmo Colete seguido do Distribua no clique raiz da árvore.
	 */
	public void consistency() throws Exception {
		n = 1;
		Clique raiz = (Clique) cliques.get(0);
		coleteEvidencia(raiz);
		distributeEvidences(raiz);
	}

	/**
	 *  Processa a coleta de evidências.
	 *
	 *@param  clique  clique.
	 *@return         sucesso da coleta de evidências.
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
			absorb(clique, auxClique);
		}

		n *= clique.normalize();
	}

	/**
	 *  Processa a distribuição de evidências.
	 *
	 *@param  clique  clique.
	 */
	protected void distributeEvidences(Clique clique) {
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			
//			Separator sep = getSeparator(clique, auxClique); 
//			auxClique.absorb(clique, sep.getPotentialTable());
			absorb(auxClique, clique);
			if (auxClique.getChildrenSize() != 0) {
				distributeEvidences(auxClique);
			}
		}
	}
	
	protected void absorb(Clique clique1, Clique clique2) {
		PotentialTable sepTab = getSeparator(clique1, clique2).getPotentialTable();
		NodeList toDie = SetToolkit.clone(clique2.getNodes());
		
		for (int i = 0; i < sepTab.variableCount(); i++) {
			toDie.remove(sepTab.getVariableAt(i));			
		}

		PotentialTable dummyTable =
			(PotentialTable) clique2.getPotentialTable().clone();
			
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

		clique1.getPotentialTable().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);
    }

	/**
	 *  Inicia crenças da árvore.
	 */
	public void initBeliefs() throws Exception {
		// TODO Unconment the if else code below after fixing restoreTableData()
//		if (! initialized) {
			Clique auxClique;
			PotentialTable auxTabPot;
			PotentialTable auxUtilTab;
	
			int sizeCliques = cliques.size();
			for (int k = 0; k < sizeCliques; k++) {
				auxClique = (Clique) cliques.get(k);
				auxTabPot = auxClique.getPotentialTable();
				auxUtilTab = auxClique.getUtilityTable();
	
				int tableSize = auxTabPot.tableSize();
				for (int c = 0; c < tableSize; c++) {
					auxTabPot.setValue(c, 1);
				}
	
				ProbabilisticNode auxVP;
				int sizeAssociados = auxClique.getAssociatedProbabilisticNodes().size();
				for (int c = 0; c < sizeAssociados; c++) {
					auxVP = (ProbabilisticNode) auxClique.getAssociatedProbabilisticNodes().get(c);
					auxTabPot.opTab(auxVP.getPotentialTable(), PotentialTable.PRODUCT_OPERATOR);
				}
	
				tableSize = auxUtilTab.tableSize();
				for (int i = 0; i < tableSize; i++) {
					auxUtilTab.setValue(i, 0);
				}
				UtilityNode utilNode;
				sizeAssociados = auxClique.getAssociatedUtilityNodes().size();
				for (int i = 0; i < sizeAssociados; i++) {
					utilNode = (UtilityNode) auxClique.getAssociatedUtilityNodes().get(i);
					auxUtilTab.opTab(utilNode.getPotentialTable(), PotentialTable.PLUS_OPERATOR);
				}
			}
	
			Separator auxSep;
			int sizeSeparadores = separators.size();
			for (int k = 0; k < sizeSeparadores; k++) {
				auxSep = (Separator) separators.get(k);
				auxTabPot = auxSep.getPotentialTable();
				int sizeDados = auxTabPot.tableSize();
				for (int c = 0; c < sizeDados; c++) {
					auxTabPot.setValue(c, 1);
				}
	
				auxUtilTab = auxSep.getUtilityTable();
				sizeDados = auxUtilTab.tableSize();
				for (int i = 0; i < sizeDados; i++) {
					auxUtilTab.setValue(i, 0);
				}
			}
			
			consistency();
			copyTableData();
	}
	
	private void copyTableData() {
		int sizeCliques = cliques.size();
		for (int k = 0; k < sizeCliques; k++) {
			Clique auxClique = (Clique) cliques.get(k);
			auxClique.getPotentialTable().copyData();
			auxClique.getUtilityTable().copyData();
		}
		
		int sizeSeparadores = separators.size();
		for (int k = 0; k < sizeSeparadores; k++) {
			Separator auxSep = (Separator) separators.get(k);
			auxSep.getPotentialTable().copyData();
		}
	}

	/**
	 * Returns the Separator associated with these Cliques, assuming no orientation.
	 *
	 * @param clique1 Clique 1
	 * @param clique2 Clique 2
	 * @return The separator associated with these Cliques or null if this separator doesn't exist.
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
}