/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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

/**
 *  Classe que representa uma �rvore de Jun��o para Redes Bayesianas.
 *
 *@author     Michael
 *@author     Rommel
 */
public class JunctionTree {

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
	 * Coordenadas pr�-calculadas para otimiza��o
	 * no m�todo absorve.
	 */
	private int coordSep[][][];

	/**
	 *  Contr�i uma nova �rvore de jun��o. Inicializa a lista de separadores e
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
	 *  Verifica a consist�ncia global.
	 *  Aplica o algoritmo Colete seguido do Distribua no clique raiz da �rvore.
	 *
	 *@return    resultado da consist�ncia.
	 */
	boolean consistencia() {
		n = 1.0;
		Clique raiz = (Clique) cliques.get(0);
		boolean result = coleteEvidencia(raiz);
		if (result) {
			distribuaEvidencia(raiz);
		}		
		return result;
	}

	/**
	 *  Processa a coleta de evid�ncias.
	 *
	 *@param  clique  clique.
	 *@return         sucesso da coleta de evid�ncias.
	 */
	private boolean coleteEvidencia(Clique clique) {
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			if (auxClique.getChildrenSize() != 0) {
				this.coleteEvidencia(auxClique);
			}
			this.absorve(clique, auxClique);
		}

		boolean[] ok = new boolean[1];
		n *= clique.normalize(ok);
		return ok[0];
	}

	/**
	 *  Processa a distribui��o de evid�ncias.
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
		List toDie = SetToolkit.clone(clique2.getNos());
		toDie.removeAll(separator.getNos());

		PotentialTable originalSeparatorTable =
			(PotentialTable) separator.getPotentialTable().clone();
		PotentialTable dummyTable =
			(PotentialTable) clique2.getPotentialTable().clone();
		for (int i = 0; i < toDie.size(); i++) {
			dummyTable.removeVariable((Node) toDie.get(i));
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
		    Node node = (Node) toDie.get(i);
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
	
	    Node node = (Node) toDie.get(index);
	    int indexVariable = potTable.indexOfVariable(node);
	    for (int i = node.getStatesSize()-1; i >= 0; i--) {
	        coord[indexVariable] = i;
	        retorno += calcularSoma(index+1, coord, toDie, potTable);
	    }
	    return retorno;
	}
	*/

	/**
	 *  Inicia cren�as da �rvore.
	 */
	void iniciaCrencas() {
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
		
		/*		
		for (int i = 0; i < cliques.size(); i++) {
			Clique clique = (Clique) cliques.get(i);
			clique.getUtilityTable().mostrarTabela(""+ i);		
		}
		*/

//		this.calcularCoordenadasMulti();
		consistencia();
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
	 * Sub-m�todo do m�todo iniciaCrencas que pr�-calcula
	 * as coordenadas multidimensionais de todos os
	 * separadores da arvore de jun��o.
	 */
	/*
	private void calcularCoordenadasMulti() {
		ITabledVariable aux;

		coordSep = new int[separators.size()][][];

		int sizeSeparadores = separators.size();
		for (int i = 0; i < sizeSeparadores; i++) {
			aux = (ITabledVariable) separators.get(i);
			coordSep[i] =
				new int[aux
					.getPotentialTable()
					.tableSize()][aux
					.getPotentialTable()
					.variableCount()];

			int sizeDados1 = aux.getPotentialTable().tableSize();
			for (int w = 0; w < sizeDados1; w++) {
				coordSep[i][w] = aux.getPotentialTable().voltaCoord(w);
			}
		}
	}
	*/
}