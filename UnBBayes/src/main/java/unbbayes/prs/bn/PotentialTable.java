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
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.util.FloatCollection;
import unbbayes.util.SetToolkit;


/**
 * Tabela de Potencial.
 * 
 * @author Michael e Rommel
 * @version 21 de Setembro de 2001
 */
public abstract class PotentialTable implements Cloneable, java.io.Serializable {
	public static final int PRODUCT_OPERATOR = 0;
	public static final int DIVISION_OPERATOR = 1;
	public static final int PLUS_OPERATOR = 2;
	public static final int MINUS_OPERATOR = 3;

	private boolean modified;
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

	/**
	 * Vari�veis que pertencem � tabela
	 */
	protected ArrayList<Node> variaveis;

	/**
	 * Dados armazenados em forma de lista do tipo primitivo float
	 */
	protected FloatCollection dados;
	
	/**
	 * Copy of the table data
	 */
	protected FloatCollection dataCopy;

	/**
	 * Fatores utilizados para converter coordenadas lineares em
	 * multidimensionais.
	 */
	protected int[] fatores;

	/**
	 * Inicializa os dados e variaveis.
	 */
	public PotentialTable() {
		modified = true;
		dados = new FloatCollection();
		dataCopy = new FloatCollection();
		variaveis = new ArrayList<Node>();
	}
	
	public void copyData() {
		int dataSize = dados.size;
		for (int i = 0; i < dataSize; i++) {
			dataCopy.add(dados.data[i]);
		}
	}
	
	public void restoreData() {
		int dataSize = dados.size;
		for (int i = 0; i < dataSize; i++) {
			dados.data[i] = dataCopy.data[i];
		}
	}

	/**
	 * Tem que ser chamado quando h� mudan�a em alguma vari�vel desta tabela
	 */
	public void variableModified() {
	   modified = true;
	}

	/**
	 * Retorna uma COPIA da lista de vari�veis desta tabela.
	 * 
	 * @return COPIA da lista de variaveis desta tabela.
	 */
	public ArrayList<Node> cloneVariables() {
		return SetToolkit.clone(variaveis);
	}

	public final int indexOfVariable(Node node) {
		return variaveis.indexOf(node);
	}

	public final int variableCount() {
		return variaveis.size();
	}

	public void setVariableAt(int index, Node node) {
		variableModified();
		variaveis.set(index, node);
	}

	public final Node getVariableAt(int index) {
		return variaveis.get(index);
	}
	
	public final int getVariableIndex(Node variable){
		for(int i = 0; i < variaveis.size(); i++){
			if(variaveis.get(i) == variable){
				return i; 
			}
		}
		return -1; 
	}

	public void addValueAt(int index, float value) {
		dados.add(index, value);
	}
	
	public final void removeValueAt(int index) {
		dados.remove(index);
	}

	public int tableSize() {
	   return dados.size;
	}

	/**
	 * Retorna uma c�pia da tabela.
	 * 
	 * @return c�pia da tabela.
	 */
	public Object clone() {
		PotentialTable auxTab = newInstance();
		auxTab.variaveis = SetToolkit.clone(variaveis);
		int sizeDados = dados.size;
		for (int c = 0; c < sizeDados; c++) {
			auxTab.dados.add(dados.data[c]);
		}
		return auxTab;
	}

	/**
	 * Insere celula na tabela pelas coordenadas.
	 * 
	 * @param coordenadas
	 *            Coordenada na tabela.
	 * @param valor
	 *            Valor a ser colocado na coordenada especificada
	 */
	public void setValue(int[] coordenadas, float valor) {
		dados.data[getLinearCoord(coordenadas)] = valor;
	}

	/**
	 * Insere valor na posi��o (linear) na lista de dados.
	 * 
	 * @param index
	 *            posicao linear onde o valor entrar�
	 * @param valor
	 *            valor a ser colocado na posicao especificada.
	 */
	public final void setValue(int index, float valor) {
		dados.data[index] = valor;
	}

	/**
	 * Retorna o valor da c�lula com o respectivo �ndice.
	 * 
	 * @param index
	 *            �ndice linear do valor na tabela a ser retornado.
	 * @return valor na tabela correspondente ao indice linear especificado.
	 */
	public final float getValue(int index) {
		return dados.data[index];
	}

	/**
	 * Retorna o valor na tabela a partir do vetor de coordenadas
	 * 
	 * @param coordenadas
	 *            coordenadas do valor a ser pego.
	 * @return valor na tabela especificada pelas coordenadas.
	 */
	public final float getValue(int[] coordenadas) {
		return dados.data[getLinearCoord(coordenadas)];
	}

	/**
	 * Insere vari�vel na tabela.
	 * 
	 * @param variavel
	 *            variavel a ser inserida na tabela.
	 */
	public void addVariable(Node variavel) {
		/** @todo Reimplementar este m�todo de forma correta. */
		variableModified();
		int noEstados = variavel.getStatesSize();
		int noCelBasica = this.dados.size;
		if (variaveis.size() == 0) {
			for (int i = 0; i < noEstados; i++) {
				dados.add(0);
			}
		} else {
			while (noEstados > 1) {
				noEstados--;
				for (int i = 0; i < noCelBasica; i++) {
					dados.add(dados.data[i]);
				}
			}
		}
		variaveis.add(variavel);
	}
	
	/**
	 * Move a variable of position. Only the variables are moved... the data of
	 * the table aren't changed. 
	 * 
	 * ATENTION: Use this method only if you need to fill the table manually and want choice
	 * the order of the variables. Use before to fill the data. 
	 * 
	 * Pre-requisites: 
	 *     - The table has size > initialPosition and size > destinationPosition
	 * 
	 * @param initialPosition
	 * @param destinationPosition
	 */
	public void moveVariableWithoutMoveData(int initialPosition, int destinationPosition){
		
		Node nodeToMove = variaveis.remove(initialPosition); 
		variaveis.add(destinationPosition, nodeToMove); 
		
	}
	
	public int getVariablesSize(){
		return variaveis.size(); 
	}

	/**
	 * Retira a vari�vel da tabela. Utilizado tamb�m para marginaliza��o
	 * generalizada.
	 * 
	 * @param variavel
	 *            Variavel a ser retirada da tabela.
	 */
	public abstract void removeVariable(Node variavel);

	/**
	 * Returns a new instance of a PotentialTable of the current implemented
	 * sub-class.
	 * 
	 * @return a new instance of a PotentialTable of the current implemented
	 *         sub-class.
	 */
	public abstract PotentialTable newInstance();

	protected void sum(int index) {
		boolean marked[]  = new boolean[dados.size];		
		sumAux(variaveis.size() - 1, index, 0, 0, marked);
		
		int j = 0;
		for (int i = 0; i < dados.size; i++) {
			if (marked[i]) {
				continue;				
			}
			dados.data[j++] = dados.data[i];
		}
		
		dados.size = j;
	}
	
	
	/**
	 * Auxiliary method for sum()
	 * 
	 * @param control
	 *            Control index for the recursion. Call with the value
	 *            'variaveis.size - 1'
	 * @param index
	 *            Index of the variable to delete from the table
	 * @param coord
	 *            Call with 0
	 * @param base
	 *            Call with 0
	 * @param marked
	 *            Call with an array of falses.
	 */
	private void sumAux(int control, int index, int coord, int base, boolean[] marked) {
		if (control == -1) {
			int linearCoordDestination = coord - base;
			float value = dados.data[linearCoordDestination] + dados.data[coord];
			dados.data[linearCoordDestination] = value;
			marked[coord] = true;
			return;
		}
		
		Node node = variaveis.get(control);
		if (control == index) {	
			for (int i = node.getStatesSize()-1; i >= 1; i--) {
				sumAux(control-1, index, coord + i*fatores[control], i*fatores[index], marked);
			}	
		} else {
			for (int i = node.getStatesSize()-1; i >= 0; i--) {
				sumAux(control-1, index, coord + i*fatores[control], base, marked);
			}
		}
	}
	

	protected void finding(int control, int index, int coord[], int state) {
		if (control == -1) {
			int linearCoordToKill = getLinearCoord(coord);
			if (coord[index] == state) {
				int linearCoordDestination = linearCoordToKill - coord[index]*fatores[index];
				float value = dados.data[linearCoordToKill];
				dados.data[linearCoordDestination] = value;
			}
			dados.remove(linearCoordToKill);
			return;
		}

		int fim = (index == control) ? 1 : 0;
		Node node = variaveis.get(control);
		for (int i = node.getStatesSize()-1; i >= fim; i--) {
			coord[control] = i;
			finding(control-1, index, coord, state);
		}
	}


	/**
	 * Retorna a coordenada linear referente � coordenada multidimensional
	 * especificada.
	 * 
	 * @param coord
	 *            coordenada multidimensional.
	 * @return coordenada linear correspondente.
	 */
	public final int getLinearCoord(int coord[]) {
		calcularFatores();
		int coordLinear = 0;
		int sizeVariaveis = variaveis.size();
		for (int v = 0; v < sizeVariaveis; v++) {
			coordLinear += coord[v] * fatores[v];
		}
		return coordLinear;
	}


	/**
	 * Calcula os fatores necess�rios para transformar as coordenadas lineares
	 * em multidimensionais.
	 */
	protected void calcularFatores() {
		if (! modified) {
			return;
		}
		modified = false;
		int sizeVariaveis = variaveis.size();
		if (fatores == null || fatores.length < sizeVariaveis) {
		   fatores = new int[sizeVariaveis];
		}
		fatores[0] = 1;
		Node auxNo;
		for (int i = 1; i < sizeVariaveis; i++) {
			auxNo = variaveis.get(i-1);
			fatores[i] = fatores[i-1] * auxNo.getStatesSize();
		}
	}



	/**
	 * Retorna valor em coordenada a partir do �ndice na lista.
	 * 
	 * @param index
	 *            �ndice linear na tabela.
	 * @return array das coordenadas respectivo ao indice linear especificado.
	 */
	public final int[] voltaCoord(int index) {
		calcularFatores();
		int fatorI;
		int sizeVariaveis = variaveis.size();
		int coord[] = new int[sizeVariaveis];
		int i = sizeVariaveis - 1;
		while (index != 0) {
			fatorI = fatores[i];
			coord[i--] = index / fatorI;
			index %= fatorI;
		}
		return coord;
	}

	/**
	 * Operates with the argument table directly.
	 * 
	 * @param tab
	 *            table to operate.
	 * @param operator
	 *            operator to use, defined in this class constants.
	 */
	public final void directOpTab(PotentialTable tab, int operator) {
		if (tableSize() != tab.tableSize()) {
			throw new RuntimeException(resource.getString("TableSizeException") + ": " + tableSize() + " " + tab.tableSize());
		}
		
		switch (operator) {
			case PRODUCT_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dados.data[k] *= tab.dados.data[k];
				}
				break;
			 
			case DIVISION_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					if (tab.dados.data[k] != 0) {
						dados.data[k] /= tab.dados.data[k];
					} else {
						dados.data[k] = 0;						
					}
				}
				break;
			
			case MINUS_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dados.data[k] -= tab.dados.data[k];
				}
				break;
		}
	}


	/**
	 * Opera tabela do parametro com esta.
	 * 
	 * @param tab
	 *            tabela a ser operada com esta.
	 * @param operator
	 *            operador a ser utilizado, definido pelas constantes desta
	 *            classe.
	 */
	public final void opTab(PotentialTable tab, int operator) {		
		int[] index = new int[variaveis.size()];
		for (int c = variaveis.size()-1; c >= 0; c--) {
			index[c] = tab.variaveis.indexOf(variaveis.get(c));
		}
		calcularFatores();
		tab.calcularFatores();
		
		switch (operator) {
			case PRODUCT_OPERATOR:
				fastOpTabProd(0, 0, 0, index, tab);
				break;
				
			case PLUS_OPERATOR:
				fastOpTabPlus(0, 0, 0, index, tab);
				break;
			
			case DIVISION_OPERATOR:
				fastOpTabDiv(0, 0, 0, index, tab);
				break;
				
			default:
// assert false : "Operador n�o suportado!";
		}
	}
	
	
	private void fastOpTabPlus(int c, int linearA, int linearB, int index[], PotentialTable tab) {
		if (c >= variaveis.size()) {			
			dados.data[linearA] += tab.dados.data[linearB];
			return;						
		}
		if (index[c] == -1) {
			for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabPlus(c+1, linearA + i*fatores[c] , linearB, index, tab);
			}
		} else {
			for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabPlus(c+1, linearA + i*fatores[c] , linearB + i*tab.fatores[index[c]], index, tab);
			}
		}
	}

	private void fastOpTabProd(int c, int linearA, int linearB, int index[], PotentialTable tab) {
		if (c >= variaveis.size()) {
			dados.data[linearA] *= tab.dados.data[linearB];
			return;						
		}
		if (index[c] == -1) {
			for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabProd(c+1, linearA + i*fatores[c] , linearB, index, tab);
			}
		} else {
			for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabProd(c+1, linearA + i*fatores[c] , linearB + i*tab.fatores[index[c]], index, tab);
			}
		}
	}
	
	private void fastOpTabDiv(int c, int linearA, int linearB, int index[], PotentialTable tab) {
		if (c >= variaveis.size()) {
			dados.data[linearA] /= tab.dados.data[linearB];
			return;						
		}
		if (index[c] == -1) {
			for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabDiv(c+1, linearA + i*fatores[c] , linearB, index, tab);
			}
		} else {
			for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabDiv(c+1, linearA + i*fatores[c] , linearB + i*tab.fatores[index[c]], index, tab);
			}
		}
	}
	
	
}