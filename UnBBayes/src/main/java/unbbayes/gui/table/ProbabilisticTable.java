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
package unbbayes.gui.table;


import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;

/**
 * @author Rommel N Carvalho
 *
 * This class is responsible for...
 * TODO FINISH THIS CLASS
 */
public class ProbabilisticTable extends JTable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	private PotentialTable potTab;
	private Node node;
	private int columns;
	private int variables;
	private int states;
    
    private final Pattern decimalPattern = Pattern.compile("[0-9]*([.|,][0-9]+)?");
    private Matcher matcher;

	public ProbabilisticTable(Node _node, ProbabilisticTableModel _ptm) {
        super(_ptm);
        dataModel.addTableModelListener(this);
        //_ptm.addTableModelListener(this);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //this.setTableHeader(null);
		node = _node;
        createDefaultColumnsFromModel();
        setTableHeader(createDefaultTableHeader());
	}
    
    /**
     * @see javax.swing.JTable#createDefaultTableHeader()
     */
    protected JTableHeader createDefaultTableHeader() {
        //GroupableTableHeader gth = new GroupableTableHeader(columnModel);
        
        if (node instanceof ITabledVariable) {
            potTab = ((ITabledVariable) node).getPotentialTable();

            states = 1;
            variables = potTab.variableCount();
            
            if (variables > 2) {

                // calculate the number of states by multiplying the number of 
                // states that each father (variables) has. Where variable 0 is the
                // node itself. That is why it starts at 1. In here we consider that
                // the last father is already the name of the columns, that is why
                // we have variables - 1.
                /* Ex: states = 2;
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
                for (int count = 1; count < variables - 1; count++) {
                    states *= potTab.getVariableAt(count).getStatesSize();
                }
                
                // the number of columns is the number of states that we calculate
                // before plus one that is the column where the fathers names and 
                // the states of the node itself will be placed.
                columns = states + 1;

                Vector<Vector<ColumnGroup>> cgv = new Vector<Vector<ColumnGroup>>(variables - 2);
                Vector<ColumnGroup> cgf = new Vector<ColumnGroup>(variables - 1);
                ColumnGroup cg;
    
                // put the name of the father and its states' name in the right 
                // place and group. Remember that the last father is already the 
                // name of the columns.
                for (int k = variables - 2, l = 0; k >= 1; k--, l++) {
                    Node variable = potTab.getVariableAt(k);
                    
                    cgv.add(new Vector<ColumnGroup>(states));
                    
                    // the number of states is the multiplication of the number of
                    // states of the other fathers above this one.
                    states /= variable.getStatesSize();
                    
                    // put the name of the father in the first column.
                    cg = new ColumnGroup(variable.getName());
                    if (l == 0) {
                        cg.add(columnModel.getColumn(0));
                    } else {
                        cg.add(cgf.get(l - 1));
                    }
                    cgf.add(cg);
                   // gth.addColumnGroup(cg);
                    
                    // put the name of the states of this father in the lth vector 
                    // and tth position, repeating the name if necessary (for each 
                    // state of the father above).
                    for (int i = 0; i < columns - 1; i++) {
                        String name = variable.getStateAt((i / states) % variable.getStatesSize());
                        System.out.println(name);
                        cg = new ColumnGroup(name);
                        ((Vector<ColumnGroup>)cgv.get(l)).add(cg);
                        int temp = potTab.getVariableAt(k + 1).getStatesSize();
                        for (int t = i * temp; t < (i + 1) * temp; t++) {
                        //for (int t = i; t < columns; t += temp) {
                            if (l == 0) {
                                cg.add(columnModel.getColumn(t + 1));
                            } else {
                                cg.add(((Vector)cgv.get(l - 1)).get(t));
                            }
                        }
                        //gth.addColumnGroup(cg);
                    }
                    columns = states;
                }
            }
            
        } else {
            return null;
        }
        
        return new JTableHeader();
    }

	/**
	 * @see javax.swing.event.TableModelListener#tableChanged(TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		if (e.getLastRow() < variables - 1) {
			return;
		}
		if (e.getColumn() == 0) {
            System.out.println("Entrou " + getValueAt(e.getLastRow(), e.getColumn()));
			if (!getValueAt(e.getLastRow(), e.getColumn()).equals("")) {
				node.setStateAt(
					getValueAt(e.getLastRow(), e.getColumn()).toString(),
                    e.getLastRow());
                System.out.println(node.getStateAt(e.getLastRow()));
			}
		} else {
			try {
				String temp = getValueAt(e.getLastRow(), e.getColumn()).toString().replace(',', '.');
                System.out.println(temp);
                matcher = decimalPattern.matcher(temp);
                    if (!matcher.matches()) {
                        JOptionPane.showMessageDialog(null, /*resource.getString("decimalError")*/"Decimal Error", /*resource.getString("decimalException")*/"Decimal Exception", JOptionPane.ERROR_MESSAGE);
                    }
				float valor = Float.parseFloat(temp);
				potTab.setValue(
					(e.getColumn() - 1) * node.getStatesSize()
						+ e.getLastRow()
						- variables
						+ 1,
					valor);
			} catch (Exception pe) {
				//System.err.println(
				//	resource.getString("potentialTableException"));
			}
		}
	}

}
