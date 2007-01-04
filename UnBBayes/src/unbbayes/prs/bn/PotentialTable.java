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


import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import unbbayes.gui.table.ColumnGroup;
import unbbayes.gui.table.GroupableTableCellRenderer;
import unbbayes.gui.table.GroupableTableColumnModel;
import unbbayes.gui.table.GroupableTableHeader;
import unbbayes.prs.Node;
import unbbayes.util.FloatCollection;
import unbbayes.util.NodeList;
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
	 * Variáveis que pertencem à tabela
	 */
	protected NodeList variaveis;

	/**
	 * Dados armazenados em forma de lista do tipo primitivo float
	 */
	protected FloatCollection dados;
	
	/**
	 * Copy of the table data
	 */
	private FloatCollection dataCopy;

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
		variaveis = new NodeList();
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
	 * Tem que ser chamado quando há mudança em alguma variável desta tabela
	 */
	public void variableModified() {
	   modified = true;
	}

	/**
	 * Retorna uma COPIA da lista de variáveis desta tabela.
	 * 
	 * @return COPIA da lista de variaveis desta tabela.
	 */
	public NodeList cloneVariables() {
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
	 * Retorna uma cópia da tabela.
	 * 
	 * @return cópia da tabela.
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
	 * Insere valor na posição (linear) na lista de dados.
	 * 
	 * @param index
	 *            posicao linear onde o valor entrará
	 * @param valor
	 *            valor a ser colocado na posicao especificada.
	 */
	public final void setValue(int index, float valor) {
		dados.data[index] = valor;
	}

	/**
	 * Retorna o valor da célula com o respectivo índice.
	 * 
	 * @param index
	 *            índice linear do valor na tabela a ser retornado.
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
	 * Insere variável na tabela.
	 * 
	 * @param variavel
	 *            variavel a ser inserida na tabela.
	 */
	public void addVariable(Node variavel) {
		/** @todo Reimplementar este método de forma correta. */
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
	 * Retira a variável da tabela. Utilizado também para marginalização
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
	 * Retorna a coordenada linear referente à coordenada multidimensional
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
	 * Calcula os fatores necessários para transformar as coordenadas lineares
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
	 * Retorna valor em coordenada a partir do índice na lista.
	 * 
	 * @param index
	 *            índice linear na tabela.
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
// assert false : "Operador não suportado!";
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
	
	/**
	 * This method is responsible to represent the potential table as a JTable.
	 * @return Returns the JTable representing this potential table.
	 * TODO MIGRATE TO A DIFFERENT CLASS - GUI.TABLE.PROBABILISTICTABLE
	 */
	public JTable makeTable() {
		JTable table;
		int nStates = 1;
		// Number of variables
		int nVariables = variableCount();
		Node node = getVariableAt(0);
		NumberFormat df = NumberFormat.getInstance(Locale.getDefault());
		df.setMaximumFractionDigits(4);

		// calculate the number of states by multiplying the number of
		// states that each father (variables) has. Where variable 0 is the
		// node itself. That is why we divide the table size by the number 
		// of states in the node itself. 
		/*
		 * Ex: states = 12 / 3;
		 * 
		 * |------------------------------------------------------| 
		 * | Father 2     |      State 1      |      State 2      |
		 * |--------------|-------------------|-------------------| 
		 * | Father 1     | State 1 | State 2 | State 1 | State 2 |
		 * |------------------------------------------------------| 
		 * | Node State 1 |    1    |    1    |    1    |    1    | 
		 * | Node State 2 |    0    |    0    |    0    |    0    |
		 * | Node State 3 |    0    |    0    |    0    |    0    |
		 * |------------------------------------------------------|
		 * 
		 */
		nStates = tableSize() / node.getStatesSize();

		// the number of rows is the number of states the node has.
		int rows = node.getStatesSize();

		// the number of columns is the number of states that we calculated
		// before plus one that is the column where the fathers names and
		// the states of the node itself will be placed.
		int columns = nStates + 1;
		
		// Constructing the data of the data model.
		/*
		 * Ex: data[3][4 + 1]
		 * |------------------------------------------------------| 
		 * | Node State 1 |    1    |    1    |    1    |    1    | 
		 * | Node State 2 |    0    |    0    |    0    |    0    |
		 * | Node State 3 |    0    |    0    |    0    |    0    |
		 * |------------------------------------------------------|
		 */
		String[][] data = new String[rows][columns];

		// Constructing the first header's row
		/*
		 * Ex: Following the example above this is the first header's row. 
		 * 
		 * |--------------|-------------------|-------------------| 
		 * | Father 1     | State 1 | State 2 | State 1 | State 2 |
		 * |------------------------------------------------------| 
		 * 
		 */
		String[] column = new String[data[0].length];
		Node firtHeaderNode;
		// If there is no father, this is going to be the first header's 
		// row:
		/*
		 * |-----------|---------------| 
		 * | State     |  Probability  |
		 * |---------------------------| 
		 * 
		 */
		if (nVariables == 1) {
			column[0] = "State";
			column[1] = "Probability";
		} else {
			firtHeaderNode = getVariableAt(1);
			/*
			 * Ex: Here we get the variable "Father 1" and set its name in 
			 *     the header. 
			 * 
			 * |--------------| 
			 * | Father 1     |
			 * |--------------- 
			 * 
			 */
			column[0] = firtHeaderNode.getName();
			for (int i = 0; i < data[0].length - 1; i++) {
				if (nVariables > 1) {
					// Reapeats all states in the node until there are cells to
					// fill.
					/*
					 * Ex: Following the example above. Here the states go. 
					 * 
					 * |-------------------|-------------------| 
					 * | State 1 | State 2 | State 1 | State 2 |
					 * ----------------------------------------| 
					 * 
					 */
					column[i + 1] = firtHeaderNode.getStateAt(i % firtHeaderNode.getStatesSize());
				}
			}
		}
		
		// Filling the data of the data model.
		/*
		 * Ex: Fill the data[3][5] constructed above.
		 * |------------------------------------------------------| 
		 * | Node State 1 |    1    |    1    |    1    |    1    | 
		 * | Node State 2 |    0    |    0    |    0    |    0    |
		 * | Node State 3 |    0    |    0    |    0    |    0    |
		 * |------------------------------------------------------|
		 */
		// The values are arranged in the potential table as a vector.
		/*
		 * Ex: This would be the vector in the potential table.
		 * |-------------------------------------------------------------------| 
		 * | Vector Position | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 
		 * | Vector Value    | 1 | 0 | 0 | 1 | 0 | 0 | 1 | 0 | 0 | 1 | 0  | 0  |
		 * |-------------------------------------------------------------------|
		 */
		// So, for each column we jump the number of values corresponding 
		// that column, that is, the number of rows. 
		for (int c = 1, n = 0; c < columns; c++, n += rows) {
			for (int r = 0; r < rows; r++) {
				// So, data[0][3] = vector[6 + 0] = 1 
				data[r][c] = "" + "" + df.format(getValue(n + r));
			}
		}
		// Now that we filled the values, we are going to put this node's
		// states name.
		/*
		 * Ex: Fill the data[i][0] constructed above, that is, its states 
		 *     name.
		 * |--------------- 
		 * | Node State 1 | 
		 * | Node State 2 |
		 * | Node State 3 |
		 * |---------------
		 */ 
		for (int i = 0; i < rows; i++) {
			data[i][0] = node.getStateAt(i);
		}
		
		// Constructing the table so far.
		/*
		 * Ex: The table so far, following the example above.
		 * 
		 * |--------------|-------------------|-------------------| 
		 * | Father 1     | State 1 | State 2 | State 1 | State 2 |
		 * |------------------------------------------------------| 
		 * | Node State 1 |    1    |    1    |    1    |    1    | 
		 * | Node State 2 |    0    |    0    |    0    |    0    |
		 * | Node State 3 |    0    |    0    |    0    |    0    |
		 * |------------------------------------------------------|
		 * 
		 */
		DefaultTableModel model = new DefaultTableModel();
		model.setDataVector(data, column);
		table = new JTable();
		// Setup to allow grouping the header.
		table.setColumnModel(new GroupableTableColumnModel());
		table.setTableHeader(new GroupableTableHeader(
				(GroupableTableColumnModel) table.getColumnModel()));
		table.setModel(model);
		
		// Setup Column Groups
		GroupableTableColumnModel cModel = (GroupableTableColumnModel) table
				.getColumnModel();
		ColumnGroup cNodeGroup = null;
		ColumnGroup cNodeTempGroup = null;
		ColumnGroup cGroup = null;
		List<ColumnGroup> cGroupList = new ArrayList<ColumnGroup>();
		List<ColumnGroup> previousCGroupList = new ArrayList<ColumnGroup>();
		int columnIndex;
		boolean firstNode = true;
		int sizeColumn = 1;
		// Sets default color for parents name in first column.
		/*
		 * |--------------- 
		 * | Father 2     |
		 * |--------------| 
		 * | Father 1     |
		 * |--------------- 
		 * 
		 */
		cModel.getColumn(0).setHeaderRenderer(new GroupableTableCellRenderer());
		// Sets default color for node's states
		/*
		 * |--------------- 
		 * | Node State 1 | 
		 * | Node State 2 |
		 * | Node State 3 |
		 * |---------------
		 * 
		 */
		cModel.getColumn(0).setCellRenderer(new GroupableTableCellRenderer(Color.BLACK, Color.YELLOW));
		// Fill all other headers, but the first (that has already been 
		// set). It ignores k = 0 (the node itself) and k = 1 (the fist 
		// father).
		for (int k = 2; k < nVariables; k++) {
			Node parent = getVariableAt(k);
			int nPreviousParentStates = getVariableAt(k-1).getStatesSize();
			sizeColumn *= nPreviousParentStates;
			// Set the node name as a header in the first column
			if (!firstNode) {
				cNodeTempGroup = cNodeGroup;
				cNodeGroup = new ColumnGroup(new GroupableTableCellRenderer(), parent.getName());
				cNodeGroup.add(cNodeTempGroup);
			} else {
				cNodeGroup = new ColumnGroup(new GroupableTableCellRenderer(), parent.getName());
				cNodeGroup.add(cModel.getColumn(0));
			}
			columnIndex = 1;
			cGroup = null;
			while (columnIndex <= nStates) {
				for (int i = 0; i < parent.getStatesSize(); i++) {
					cGroup = new ColumnGroup(parent.getStateAt(i));
					if (!firstNode) {
						for (int j = 0; j < nPreviousParentStates; j++) {
							ColumnGroup group = previousCGroupList.get(columnIndex-1);
							cGroup.add(group);
							columnIndex++;
						}
					} else {
						for (int j = 0; j < sizeColumn; j++) {
							cGroup.add(cModel.getColumn(columnIndex++));
						}
					}
					cGroupList.add(cGroup);
				}
			}
			previousCGroupList = cGroupList;
			cGroupList = new ArrayList<ColumnGroup>();
			firstNode = false;
			// Update the number of states
			nStates /= nPreviousParentStates;
		}
		// It adds all parents' node name as header
		if (cNodeGroup != null) {
			cModel.addColumnGroup(cNodeGroup);
		}
		// It adds only the first row (here it is the last parent's states) 
		// of the header that has all other headers (all other parent's states)
		// as sub-headers.
		if (previousCGroupList != null) {
			for (ColumnGroup group : previousCGroupList) {
				cModel.addColumnGroup(group);
			}
		}
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		return table;
	}

	/**
	 * Show the potential table. Used for DEBUG.
	 * 
	 * @param title Title of the window to be shown.
	 */
	public void showTable(String title) {
		JDialog diag = new JDialog();
		diag.getContentPane().add(new JScrollPane(makeTable()));
		diag.pack();
		diag.setVisible(true);
		diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		diag.setTitle(title);
	}
}