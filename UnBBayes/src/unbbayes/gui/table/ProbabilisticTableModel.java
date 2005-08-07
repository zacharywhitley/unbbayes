package unbbayes.gui.table;

import java.text.NumberFormat;
import java.util.Locale;


import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;

/**
 * @author Rommel N Carvalho
 *
 * This class is responsible for...
 */
public class ProbabilisticTableModel extends AbstractTableModel {
    
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

    private PotentialTable potTab;
    private final NumberFormat df;
    private final Node node;
    private int rows;
    private int columns;
    private int variables;
    private int states;
    
    private String[][] data;
    private String[] columnName;
    
    public ProbabilisticTableModel(final Node _node) {
        
        node = _node;
        
        df = NumberFormat.getInstance(Locale.US);
        df.setMaximumFractionDigits(4);
        
        calcData();
        printData();
    }
    
    private void printData() {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                System.out.print(" |" + data[i][j] + "| ");
            }
            System.out.println();
        }
    }
    
    private void calcData() {
        
        if (node instanceof ITabledVariable) {
            potTab = ((ITabledVariable) node).getPotentialTable();

            states = 1;
            variables = potTab.variableCount();

            // calculate the number of states by multiplying the number of 
            // states that each father (variables) has. Where variable 0 is the
            // node itself. That is why it starts at 1.
            /* Ex: states = 2 * 2;
             *
             * |------------------------------------------------------|
             * |   Father 2   |      State 1      |      State 2      |
             * |--------------|-------------------|-------------------|
             * |   Father 1   | State 1 | State 2 | State 1 | State 2 |
             * |------------------------------------------------------|
             * | Node State 1 |    1    |    1    |    1    |    1    |
             * | Node State 2 |    0    |    0    |    0    |    0    |
             *
             */
            for (int count = 1; count < variables; count++) {
                states *= potTab.getVariableAt(count).getStatesSize();
            }
            
            // the number of rows is the number of states the node has.
            rows = node.getStatesSize();
            
            // the number of columns is the number of states that we calculate
            // before plus one that is the column where the fathers names and 
            // the states of the node itself will be placed.
            columns = states + 1;

            data = new String[rows][columns];
            columnName = new String[columns];
            
            // put the name of the father and its states' name in the right 
            // place.
            int lastFather = variables - 1;
            Node variable = potTab.getVariableAt(lastFather);
            
            // the number of states is the multiplication of the number of
            // states of the other fathers above this one.
            states /= variable.getStatesSize();
            
            if (lastFather != 0) {
                // put the name of the father as the first column.
                columnName[0] = variable.getName();
                
                // put the name of the states of this father as the ith column, 
                // repeating the name if necessary (for each state of the father 
                // above).
                for (int i = 0; i < variable.getStatesSize(); i++) {
                    String name = variable.getStateAt(i);
                    for (int j = i; j < columns - 1; j += variable.getStatesSize()) {
                        columnName[j + 1] = name;
                    }
                }
            } else {
                columnName[0] = "State";
                columnName[1] = "Probability";
            }

            // put the name of the states of the node in the first column.
            for (int k = 0; k < rows; k++) {
                //tabela.setValueAt(node.getStateAt(k), k, 0);
                data[k][0] = node.getStateAt(k);
            }

            // now states is the number of states that the node has.
            states = node.getStatesSize();
            
            // put the values of the probabilistic table in the jth row and ith
            // column, picking up the values in a double collection in potTab.
            for (int i = 1, k = 0; i < columns; i++, k += states) {
                for (int j = 0; j < rows; j++) {
                    //tabela.setValueAt("" + df.format(potTab.getValue(k + j)), j, i);
                    data[j][i] = "" + df.format(potTab.getValue(k + j));
                }
            }
            
        } else {
            // decision
            
            // the number of rows in this case is the number of states of the 
            // node and the number of columns is always 1.
            rows = node.getStatesSize();
            columns = 1;
            
            // there is no potential table and the number of variables is the 
            // number of parents this node has.
            potTab = null;
            variables = node.getParents().size();
            
            data = new String[rows][columns];

            // put the name of each state in the first and only column.
            for (int i = 0; i < node.getStatesSize(); i++) {
                //tabela.setValueAt(node.getStateAt(i), i, 0);
                data[i][0] = node.getStateAt(i);
            }
            
        }
        
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return columns;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return rows;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	//public Class getColumnClass(int columnIndex) {
        //if (columnIndex == 0) {
        //    return String.class;
        //}
		//return Float.class;
    //    return Object.class;
	//}

	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return columnName[column];
	}

	/**
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	/**
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int row, int column) {
		super.setValueAt(value, row, column);
        fireTableChanged(new TableModelEvent(this, row, row, column));
	}

}
