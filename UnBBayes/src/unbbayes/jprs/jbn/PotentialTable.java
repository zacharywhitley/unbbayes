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

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import unbbayes.util.DoubleCollection;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;
import java.util.ResourceBundle;


/**
 * Tabela de Potencial.
 *
 *@author     Michael e Rommel
 *@version    21 de Setembro de 2001
 */
public abstract class PotentialTable implements Cloneable, java.io.Serializable {

    public static final int PRODUCT_OPERATOR = 0;
    public static final int DIVISION_OPERATOR = 1;
    public static final int PLUS_OPERATOR = 2;
    public static final int MINUS_OPERATOR = 3;

    private boolean modified;
    
    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.jprs.jbn.resources.JbnResources");

    /**
     * Vari�veis que pertencem � tabela
     */
    protected NodeList variaveis;

    /**
     * Dados armazenados em forma de lista do tipo primitivo double
     */
    protected DoubleCollection dados;
    
    /**
     * Copy of the table data
     */
    private DoubleCollection dataCopy;

    /**
     * Fatores utilizados para converter coordenadas lineares em multidimensionais.
     */
    protected int[] fatores;

    /**
     * Inicializa os dados e variaveis.
     */
    public PotentialTable() {
        modified = true;
        dados = new DoubleCollection();
        dataCopy = new DoubleCollection();
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
     * Tem que ser chamado quando h� mudan�a em alguma vari�vel desta tabela
     */
    public void variableModified() {
       modified = true;
    }

    /**
     *  Retorna uma COPIA da lista de vari�veis desta tabela.
     *
     *@return    COPIA da lista de variaveis desta tabela.
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

    public void addValueAt(int index, double value) {
        dados.add(index, value);
    }
    
    public final void setValueAt(int index, double value) {
    	dados.data[index] = value;
    }

    public final void removeValueAt(int index) {
        dados.remove(index);
    }

    public int tableSize() {
       return dados.size;
    }


    /**
     *  Retorna uma c�pia da tabela.
     *
     *@return    c�pia da tabela.
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
     *  Insere celula na tabela pelas coordenadas.
     *
     *@param  coordenadas  Coordenada na tabela.
     *@param  valor Valor a ser colocado na coordenada especificada
     */
    public void setValue(int[] coordenadas, double valor) {
        dados.data[getLinearCoord(coordenadas)] = valor;
    }


    /**
     *  Insere valor na posi��o (linear) na lista de dados.
     *
     *@param  index  posicao linear onde o valor entrar�
     *@param  valor  valor a ser colocado na posicao especificada.
     */
    public final void setValue(int index, double valor) {
        dados.data[index] = valor;
    }


    /**
     *  Retorna o valor da c�lula com o respectivo �ndice.
     *
     *@param  index  �ndice linear do valor na tabela a ser retornado.
     *@return        valor na tabela correspondente ao indice linear especificado.
     */
    public final double getValue(int index) {
        return dados.data[index];
    }


    /**
     *  Retorna o valor na tabela a partir do vetor de coordenadas
     *
     *@param  coordenadas  coordenadas do valor a ser pego.
     *@return              valor na tabela especificada pelas coordenadas.
     */
    public final double getValue(int[] coordenadas) {
        return dados.data[getLinearCoord(coordenadas)];
    }


    /**
     *  Insere vari�vel na tabela.
     *
     *@param  variavel  variavel a ser inserida na tabela.
     */
    public void addVariable(Node variavel) {
        /** @todo Reimplementar este m�todo de forma correta. */
        variableModified();
        int noEstados = variavel.getStatesSize();
        int noCelBasica = this.dados.size;
        if (variaveis.size() == 0) {
            for (int i = 0; i < noEstados; i++) {
                dados.add(0.0);
            }
        }
        else {
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
     *  Retira a vari�vel da tabela. Utilizado tamb�m para marginaliza��o generalizada.
     *
     *@param  variavel  Variavel a ser retirada da tabela.
     */
    public abstract void removeVariable(Node variavel);

    /**
     * Returns a new instance of a PotentialTable of the current implemented sub-class.
     * @return a new instance of a PotentialTable of the current implemented sub-class.
     */
    public abstract PotentialTable newInstance();


    protected void sum(int control, int index, int coord, int base) {
        if (control == -1) {            
            int linearCoordDestination = coord - base;
            double value = dados.data[linearCoordDestination] + dados.data[coord];
            dados.data[linearCoordDestination] = value;
            dados.remove(coord);
            return;
        }
		
		Node node = variaveis.get(control);
		if (control == index) {	
	        for (int i = node.getStatesSize()-1; i >= 1; i--) {
	            sum(control-1, index, coord + i*fatores[control], i*fatores[index]);
	        }	
		} else {
	        for (int i = node.getStatesSize()-1; i >= 0; i--) {
	            sum(control-1, index, coord + i*fatores[control], base);
	        }
		}
    }

/*
    protected void sum(int control, int index, int coord[]) {
        if (control == -1) {
            int linearCoordToKill = getLinearCoord(coord);
            int linearCoordDestination = linearCoordToKill - coord[index]*fatores[index];
            double value = dados.data[linearCoordDestination] + dados.data[linearCoordToKill];
            dados.data[linearCoordDestination] = value;
            dados.remove(linearCoordToKill);
            return;
        }

        int fim = (index == control) ? 1 : 0;
        Node node = variaveis.get(control);
        for (int i = node.getStatesSize()-1; i >= fim; i--) {
            coord[control] = i;
            sum(control-1, index, coord);
        }
    }
*/


    protected void finding(int control, int index, int coord[], int state) {
        if (control == -1) {
            int linearCoordToKill = getLinearCoord(coord);
            if (coord[index] == state) {
                int linearCoordDestination = linearCoordToKill - coord[index]*fatores[index];
                double value = dados.data[linearCoordToKill];
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
     * Retorna a coordenada linear referente � coordenada multidimensional especificada.
     *
     * @param coord coordenada multidimensional.
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
     * Calcula os fatores necess�rios para transformar as coordenadas
     * lineares em multidimensionais.
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
     *  Retorna valor em coordenada a partir do �ndice na lista.
     *
     *@param  index  �ndice linear na tabela.
     *@return        array das coordenadas respectivo ao indice linear especificado.
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
     * @param tab table to operate.
     * @param operator operator to use, defined in this class constants.
     */
    protected void directOpTab(PotentialTable tab, int operator) {
        if (tableSize() != tab.tableSize()) {
            throw new RuntimeException(resource.getString("TableSizeException"));
        }

        for (int k = tableSize()-1; k >= 0; k--) {
            double b = tab.dados.data[k];
            double a = dados.data[k];
            dados.data[k] = operate(a, b, operator);
        }
    }


    /**
     *  Opera tabela do parametro com esta.
     *
     *@param  tab      tabela a ser operada com esta.
     *@param  operator  operador a ser utilizado, definido pelas
     *                  constantes desta classe.
     */
    protected void opTab(PotentialTable tab, int operator) {    	
        int[] index = new int[variaveis.size()];
        for (int c = variaveis.size()-1; c >= 0; c--) {
        	index[c] = tab.variaveis.indexOf(variaveis.get(c));
        }
        calcularFatores();
        tab.calcularFatores();
        fastOpTab(0, 0, 0, index, tab, operator);
    }
    
    
    private void fastOpTab(int c, int linearA, int linearB, int index[], PotentialTable tab, int operator) {
    	if (c >= variaveis.size()) {
    		dados.data[linearA] = operate(dados.data[linearA], tab.dados.data[linearB],operator);
    		return;    		    		
    	}
    	if (index[c] == -1) {
    		for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		fastOpTab(c+1, linearA + i*fatores[c] , linearB, index, tab, operator);
    		}
    	} else {
	    	for (int i = variaveis.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		fastOpTab(c+1, linearA + i*fatores[c] , linearB + i*tab.fatores[index[c]], index, tab, operator);
    		}
    	}
    }

    private double operate(double a, double b, int operator) {
        switch (operator) {
            case MINUS_OPERATOR:
                b = -b;
            case PLUS_OPERATOR:
                return a + b;
            case DIVISION_OPERATOR:
                if (b == 0.0) {
                    return 0.0;
                }
                b = 1 / b;
            case PRODUCT_OPERATOR:
                return a * b;
            default:
                throw new RuntimeException(resource.getString("OperatorException"));
        }
    }


    /**
     *  Mostra a tabela de potenciais. Utilizado para DEBUG.
     *
     * @param title T�tulo da janela que ser� mostrada
     */
    public void mostrarTabela(String title) {
        JTable tabela;
        int noVariaveis;
        Node no = getVariableAt(0);

        int nEstados = 1;
        noVariaveis = variableCount();

        for (int count = 1; count < noVariaveis; count++) {
            nEstados *= getVariableAt(count).getStatesSize();
        }

        tabela = new JTable(no.getStatesSize() + noVariaveis - 1, nEstados + 1);

        for (int k = noVariaveis - 1, l = 0; k < tabela.getRowCount(); k++, l++) {
            tabela.setValueAt(no.getStateAt(l), k, 0);
        }

        for (int k = noVariaveis - 1, l = 0; k >= 1; k--, l++) {
            Node auxNo = getVariableAt(k);
            nEstados /= auxNo.getStatesSize();
            tabela.setValueAt(auxNo.getName(), l, 0);
            for (int i = 0; i < tabela.getColumnCount() - 1; i++) {
                tabela.setValueAt(auxNo.getStateAt((i / nEstados) % auxNo.getStatesSize()), l, i + 1);
            }
        }

        nEstados = no.getStatesSize();
        for (int i = 1, k = 0; i < tabela.getColumnCount(); i++, k += nEstados) {
            for (int j = noVariaveis - 1, l = 0; j < tabela.getRowCount(); j++, l++) {
                tabela.setValueAt("" + dados.data[k + l], j, i);
            }
        }

        tabela.setTableHeader(null);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JDialog diag = new JDialog();
        diag.getContentPane().add(new JScrollPane(tabela));
        diag.pack();
        diag.setVisible(true);
        diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        diag.setTitle(title);
    }
}

