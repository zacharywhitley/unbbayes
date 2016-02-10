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

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;

public class GUIPotentialTable {
	
	private PotentialTable potentialTable;

	private boolean isToGroupHeaders = true;
	
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.resources.ControllerResources.class.getName(), Locale.getDefault(), GUIPotentialTable.class.getClassLoader());
	
	/**
	 * Constructor which initializes {@link #isToGroupHeaders()} with false.
	 * @see #GUIPotentialTable(PotentialTable, boolean)
	 */
	public GUIPotentialTable(PotentialTable potentialTable) {
		this(potentialTable, true);
	}
	
	/**
	 * Default constructor initializing fields
	 * @param potentialTable : the conditional probability table that will be displayed by {@link #makeTable()}
	 * @param isToGroupHeaders : {@link #setToGroupHeaders(boolean)} will be initialized this value. If true, then 
	 * headers may occupy more than one column. If false, then headers will occupy only 1 column.
	 */
	public GUIPotentialTable(PotentialTable potentialTable, boolean isToGroupHeaders) {
		this.potentialTable = potentialTable;
		this.isToGroupHeaders = isToGroupHeaders;
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
		int nVariables = potentialTable.variableCount();
		Node node = (Node)potentialTable.getVariableAt(0);
		NumberFormat df = NumberFormat.getInstance(Locale.getDefault());
		df.setMaximumFractionDigits(4);

		// calculate the number of states by multiplying the number of
		// states that each parent (variables) has. Where variable 0 is the
		// node itself. That is why we divide the table size by the number 
		// of states in the node itself. 
		/*
		 * Ex: states = 12 / 3;
		 * 
		 * |------------------------------------------------------| 
		 * | Parent 2     |      State 1      |      State 2      |
		 * |--------------|-------------------|-------------------| 
		 * | Parent 1     | State 1 | State 2 | State 1 | State 2 |
		 * |------------------------------------------------------| 
		 * | Node State 1 |    1    |    1    |    1    |    1    | 
		 * | Node State 2 |    0    |    0    |    0    |    0    |
		 * | Node State 3 |    0    |    0    |    0    |    0    |
		 * |------------------------------------------------------|
		 * 
		 */
		nStates = potentialTable.tableSize() / node.getStatesSize();

		// the number of rows is the number of states the node has.
		int rows = node.getStatesSize();

		// the number of columns is the number of states that we calculated
		// before plus one that is the column where the parents names and
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
		 * | Parent 1     | State 1 | State 2 | State 1 | State 2 |
		 * |------------------------------------------------------| 
		 * 
		 */
		String[] column = new String[data[0].length];
		Node firstHeaderNode;
		// If there is no parent, this is going to be the first header's 
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
			firstHeaderNode = (Node)potentialTable.getVariableAt(1);
			/*
			 * Ex: Here we get the variable "Parent 1" and set its name in 
			 *     the header. 
			 * 
			 * |--------------| 
			 * | Parent 1     |
			 * |--------------- 
			 * 
			 */
			column[0] = firstHeaderNode.getName();
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
					column[i + 1] = firstHeaderNode.getStateAt(i % firstHeaderNode.getStatesSize());
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
				data[r][c] = "" + "" + df.format(potentialTable.getValue(n + r));
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
		 * | Parent 1     | State 1 | State 2 | State 1 | State 2 |
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
		
		// if isToGroupHeaders() == true, then headers will be grouped. If false, then values in headers will repeat
		table.setTableHeader(new GroupableTableHeader(
				(GroupableTableColumnModel) table.getColumnModel(), new GroupableTableHeaderUI(isToGroupHeaders())));
		
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
		 * | Parent 2     |
		 * |--------------| 
		 * | Parent 1     |
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
		// set). It ignores k = 0 (the node itself) and k = 1 (the first 
		// parent).
		for (int k = 2; k < nVariables; k++) {
			Node parent = (Node)potentialTable.getVariableAt(k);
			int nPreviousParentStates = potentialTable.getVariableAt(k-1).getStatesSize();
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
		
		// the following code was migrated from SEN controller to here
		// (feature:3315773) Allow copy/paste between JTable and Excel
		new ExcelAdapter(table);
		
		// (feature:3315761) Allow the selection of a single cell
		table.setCellSelectionEnabled(true);
		table.setRowSelectionAllowed(true);
		
		

		// Change the text cell editor to replace text instead of appending it for all columns.
		ReplaceTextCellEditor cellEditor = new ReplaceTextCellEditor();
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellEditor(cellEditor);
		}
		

		// Shows the caret while editing cell.
		table.setSurrendersFocusOnKeystroke(true);
		
		this.fillWithDefaultTableChangeListener(table);
		
		return table;
	}

	/**
	 * Show the potential table. Used for DEBUG.
	 * 
	 * @param title Title of the window to be shown.
	 * @deprecated Use it only for debugging purpose.
	 */
	public void showTable(String title) {
		JDialog diag = new JDialog();
		diag.getContentPane().add(new JScrollPane(makeTable()));
		diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		diag.setTitle(title);
		diag.pack();
		diag.setVisible(true);
	}
	
	/**
	 * This method is called in {@link #makeTable()} to fill the JTable with a default
	 * table change listener, which is to change the values of {@link #getPotentialTable()}.
	 * @param table
	 */
	public void fillWithDefaultTableChangeListener(final JTable table) {
		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (getPotentialTable() == null || getPotentialTable().getVariablesSize() <= 0) {
					Debug.println("No probabilistic table found");
					return;
				}
				// extract first 
				INode node = getPotentialTable().getVariableAt(0);
				// Change state name or reset to its previous value.
				if (e.getColumn() == 0) {
					if (!table.getValueAt(e.getLastRow(), e.getColumn()).toString().trim().equals("")) {
						node.setStateAt(table.getValueAt(e.getLastRow(),
								e.getColumn()).toString(), e.getLastRow()
								- (table.getRowCount() - node.getStatesSize()));
					} else {
						table.revalidate();
						table.setValueAt(node.getStateAt(e.getLastRow()
								- (table.getRowCount() - node.getStatesSize())), e.getLastRow(),
								e.getColumn());
					}
				// Change the CPT cell or reset to its previous value.
				} else if (getPotentialTable() != null) {
					String valueText = table.getValueAt(e.getLastRow(),
							e.getColumn()).toString().replace(',', '.');
					try {
						float value = Float.parseFloat(valueText);
						getPotentialTable().setValue((e.getColumn() - 1) * node.getStatesSize() + e.getLastRow(), value);
					} catch (NumberFormatException nfe) {
						// Just shows the error message if the value is not empty.
						if (!valueText.trim().equals("")) {
							JOptionPane.showMessageDialog(null, 
									getResource().getString("numberFormatError"), 
									getResource().getString("error"),
									JOptionPane.ERROR_MESSAGE);
						}
						table.revalidate();
						table.setValueAt(""
								+ getPotentialTable().getValue((e.getColumn() - 1) * node.getStatesSize() + e.getLastRow()),
								e.getLastRow(), e.getColumn());
					}
				}
			}
		});
	}
	/**
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return resource;
	}
	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		GUIPotentialTable.resource = resource;
	}
	/**
	 * @return the potentialTable
	 */
	public PotentialTable getPotentialTable() {
		return potentialTable;
	}
	/**
	 * @param potentialTable the potentialTable to set
	 */
	public void setPotentialTable(PotentialTable potentialTable) {
		this.potentialTable = potentialTable;
	}
	/**
	 * @return the isToGroupHeaders : if true, then {@link #makeTable()} will generate grouped table headers (i.e. some headers will occupy the width of several columns).
	 * If false, the headers will not be grouped (i.e. the width of headers will be always 1 column).
	 */
	public boolean isToGroupHeaders() {
		return isToGroupHeaders;
	}
	/**
	 * @param isToGroupHeaders the isToGroupHeaders to set : if true, then {@link #makeTable()} will generate grouped table headers (i.e. some headers will occupy the width of several columns).
	 * If false, the headers will not be grouped (i.e. the width of headers will be always 1 column).
	 */
	public void setToGroupHeaders(boolean isToGroup) {
		isToGroupHeaders = isToGroup;
	}

}
