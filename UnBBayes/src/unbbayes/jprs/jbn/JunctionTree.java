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
package unbbayes.jprs.jbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Classe que representa uma Árvore de Junção para Redes Bayesianas.
 *
 *@author     Michael
 *@author     Rommel
 */
public class JunctionTree implements java.io.Serializable {
	
	private boolean initialized;

	/**
	 *  Probabilidade total estimada.
	 */
	private double n;

	/**
	 *  Lista de Cliques Associados.
	 */
	private List cliques;

	/**
	 *  Lista de Separadores Associados.
	 */
	private List separators;

	/**
	 * Coordenadas pré-calculadas para otimização
	 * no método absorve.
	 */
	private int coordSep[][][];

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
	public double getN() {
		return n;
	}
	
	public Object clone() {
		JunctionTree cloned = new JunctionTree();
		cloned.setSeparators(SetToolkit.clone(separators));
		cloned.setCliques(SetToolkit.clone(cliques));
		cloned.setN(n);
		if (coordSep != null) {
			int [][][] coordSep1 = new int[coordSep.length][coordSep[0].length][coordSep[0][0].length];
			System.arraycopy(coordSep, 0, coordSep1, 0, coordSep.length);
			cloned.setCoordSep(coordSep1);
		}
		return cloned;
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
	void consistencia() throws Exception {
		n = 1.0;
		Clique raiz = (Clique) cliques.get(0);
		coleteEvidencia(raiz);
		distribuaEvidencia(raiz);
	}

	/**
	 *  Processa a coleta de evidências.
	 *
	 *@param  clique  clique.
	 *@return         sucesso da coleta de evidências.
	 */
	private void coleteEvidencia(Clique clique) throws Exception {
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			if (auxClique.getChildrenSize() != 0) {
				this.coleteEvidencia(auxClique);
			}
			this.absorve(clique, auxClique);
		}

		n *= clique.normalize();
	}

	/**
	 *  Processa a distribuição de evidências.
	 *
	 *@param  clique  clique.
	 */
	private void distribuaEvidencia(Clique clique) {
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			absorve(auxClique, clique);
			if (auxClique.getChildrenSize() != 0) {
				distribuaEvidencia(auxClique);
			}
		}
	}

	protected void absorve(Clique clique1, Clique clique2) {
		Separator separator = getSeparator(clique1, clique2);
		NodeList toDie = SetToolkit.clone(clique2.getNos());
		toDie.removeAll(separator.getNos());

		PotentialTable originalSeparatorTable =
			(PotentialTable) separator.getPotentialTable().clone();
		PotentialTable dummyTable =
			(PotentialTable) clique2.getPotentialTable().clone();
		for (int i = 0; i < toDie.size(); i++) {
			dummyTable.removeVariable(toDie.get(i));
		}

		for (int i = separator.getPotentialTable().tableSize() - 1; i >= 0; i--) {
			separator.getPotentialTable().setValue(i, dummyTable.getValue(i));
		}
		dummyTable = (PotentialTable) separator.getPotentialTable().clone();
		dummyTable.directOpTab(
			originalSeparatorTable,
			PotentialTable.DIVISION_OPERATOR);
		clique1.getPotentialTable().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);

		/*
		Separator separator = getSeparator(clique1, clique2);
		int indSep = separators.indexOf(separator);
		PotentialTable originalSeparatorPotentialTable = (PotentialTable) separator.getTabelaPot().clone();
		int sizeDados = originalSeparatorPotentialTable.tableSize();
		int sizeNosSeparator = separator.getNos().size();
		List toDie = SetToolkit.clone(clique2.getNos());
		toDie.removeAll(separator.getNos());
		
		int divisor = 1;
		for (int i = 0 ; i < toDie.size(); i++) {
		    Node node = toDie.get(i);
		    if (node instanceof DecisionNode) {
		        divisor *= node.getStatesSize();
		    }
		}
		
		int indexList[] = new int[sizeNosSeparator];
		for (int k = 0; k < sizeNosSeparator; k++) {
		    int index = clique2.getNos().indexOf(separator.getNos().get(k));
		    indexList[k] = index;
		}
		
		int coordAux[] = new int[clique2.getNos().size()];
		for (int i = 0; i < sizeDados; i++) {
		    for (int k = 0; k < sizeNosSeparator; k++) {
		        coordAux[indexList[k]] = coordSep[indSep][i][k];
		    }
		
		    double soma = calcularSoma(0, coordAux, toDie, clique2.getTabelaPot()) / divisor;
		    separator.getTabelaPot().setValue(i, soma);
		}
		
		PotentialTable dummyTable = (PotentialTable) separator.getTabelaPot().clone();
		dummyTable.directOpTab(originalSeparatorPotentialTable, PotentialTable.DIVISION_OPERATOR);
		clique1.getTabelaPot().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);
		*/
	}

	/*
	private double calcularSoma(int index, int coord[], List toDie, PotentialTable potTable) {
	    if (index >= toDie.size()) {
	        return potTable.getValue(coord);
	    }
	
	    double retorno = 0.0;
	
	    Node node = toDie.get(index);
	    int indexVariable = potTable.indexOfVariable(node);
	    for (int i = node.getStatesSize()-1; i >= 0; i--) {
	        coord[indexVariable] = i;
	        retorno += calcularSoma(index+1, coord, toDie, potTable);
	    }
	    return retorno;
	}
	*/

	/**
	 *  Inicia crenças da árvore.
	 */
	void iniciaCrencas() throws Exception {
		
		if (! initialized) {
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
					auxTabPot.setValue(c, 1.0);
				}
	
				ProbabilisticNode auxVP;
				int sizeAssociados = auxClique.getAssociatedProbabilisticNodes().size();
				for (int c = 0; c < sizeAssociados; c++) {
					auxVP = (ProbabilisticNode) auxClique.getAssociatedProbabilisticNodes().get(c);
					auxTabPot.opTab(auxVP.getPotentialTable(), PotentialTable.PRODUCT_OPERATOR);
				}
	
				tableSize = auxUtilTab.tableSize();
				for (int i = 0; i < tableSize; i++) {
					auxUtilTab.setValue(i, 0.0);
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
					auxTabPot.setValue(c, 1.0);
				}
	
				auxUtilTab = auxSep.getUtilityTable();
				sizeDados = auxUtilTab.tableSize();
				for (int i = 0; i < sizeDados; i++) {
					auxUtilTab.setValue(i, 0.0);
				}
			}
			
			consistencia();
			copyTableData();
			initialized = true;
		} else {
			restoreTableData();						
		}
	}
	
	private void restoreTableData() {
		int sizeCliques = cliques.size();
		for (int k = 0; k < sizeCliques; k++) {
			Clique auxClique = (Clique) cliques.get(k);
			auxClique.getPotentialTable().restoreData();
			auxClique.getUtilityTable().restoreData();
		}
		
		int sizeSeparadores = separators.size();
		for (int k = 0; k < sizeSeparadores; k++) {
			Separator auxSep = (Separator) separators.get(k);
			auxSep.getPotentialTable().restoreData();
		}
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
			if (((separator.getNo1() == clique1) && (separator.getNo2() == clique2))
				|| ((separator.getNo2() == clique1) && (separator.getNo1() == clique2))) {
				return separator;
			}
		}
		return null;
	}

	/**
	 * Sets the cliques.
	 * @param cliques The cliques to set
	 */
	public void setCliques(List cliques) {
		this.cliques = cliques;
	}

	/**
	 * Sets the coordSep.
	 * @param coordSep The coordSep to set
	 */
	public void setCoordSep(int[][][] coordSep) {
		this.coordSep = coordSep;
	}

	/**
	 * Sets the n.
	 * @param n The n to set
	 */
	public void setN(double n) {
		this.n = n;
	}

	/**
	 * Sets the separators.
	 * @param separators The separators to set
	 */
	public void setSeparators(List separators) {
		this.separators = separators;
	}

}