/**
 * 
 */
package unbbayes.gui.prm;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import unbbayes.gui.table.ColumnGroup;
import unbbayes.gui.table.GroupableTableCellRenderer;
import unbbayes.gui.table.GroupableTableColumnModel;
import unbbayes.gui.table.GroupableTableHeader;
import unbbayes.gui.table.ReplaceTextCellEditor;
import unbbayes.prs.prm.IDependencyChain;
import unbbayes.prs.prm.IPRMDependency;
import unbbayes.prs.prm.cpt.IPRMCPT;

/**
 * A table to render {@link IPRMCPT}
 * @author Shou Matsumoto
 *
 */
public class PRMTableGUI extends JTable {

	private IPRMCPT prmCPT;
	
	/**
	 * Default constructor
	 */
	protected PRMTableGUI() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dm
	 */
	protected PRMTableGUI(TableModel dm) {
		super(dm);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dm
	 * @param cm
	 */
	protected PRMTableGUI(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param numRows
	 * @param numColumns
	 */
	protected PRMTableGUI(int numRows, int numColumns) {
		super(numRows, numColumns);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param rowData
	 * @param columnNames
	 */
	protected PRMTableGUI(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param rowData
	 * @param columnNames
	 */
	protected PRMTableGUI(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dm
	 * @param cm
	 * @param sm
	 */
	protected PRMTableGUI(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method.
	 * @param prmCPT : this is the PRM's CPT representation.
	 * @return
	 */
	public static PRMTableGUI newInstance(IPRMCPT prmCPT){
		PRMTableGUI ret = new PRMTableGUI();
		ret.prmCPT = prmCPT;
		ret.initComponents();
		ret.initListeners();
		return ret;
	}
	
	/**
	 * Calls {@link #initComponents()} and {@link #initListeners()}
	 * after clearing selection and components.
	 */
	public void resetComponents() {
		this.clearSelection();
		this.removeAll();
		if (this.getPrmCPT() != null) {
			this.initComponents();
			this.initListeners();
		}
	}

	/**
	 * Initialize the components of this table
	 */
	protected void initComponents() {
		
		// Total number of variables = number of parents plus the variable containing this this (|parents| + 1)
		int nVariables = this.getPrmCPT().getPRMDependency().getIncomingDependencyChains().size() + 1;
		
		// extracting the random variable (a IPRMDependency is a 1-1 representation to IAttributeDescriptor)
		IPRMDependency node = this.getPrmCPT().getPRMDependency();
		
		// setting up how to render floating point numbers
		NumberFormat df = NumberFormat.getInstance(Locale.getDefault());
		df.setMaximumFractionDigits(4);

		// calculate how many columns (of actual data - float) do we have
		// the number of actual columns is the product of the number of states of the parents
		int nStates = 1;
		for (IDependencyChain chain : node.getIncomingDependencyChains()) {
			nStates *= chain.getDependencyFrom().getAttributeDescriptor().getStatesSize();
		}

		// the number of rows is the number of states the node has.
		int rows = node.getAttributeDescriptor().getStatesSize();

		if (rows == 0) {
			// no table to render if table is empty
			return;
		}
		
		// the number of columns is the number of states that we calculated
		// before plus one that is the column where the parent's names and
		// the states of the node itself will be placed.
		int columns = nStates + 1;
		
		// Constructing the data of the data model.
		/*
		 * Ex: data = 3 rows * (4 + 1) columns
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
		 * |------------------------|-------------------|-------------------| 
		 * | Aggregate(Parent 1)    | State 1 | State 2 | State 1 | State 2 |
		 * |----------------------------------------------------------------| 
		 * 
		 */
		String[] column = new String[data[0].length];
		IDependencyChain firstHeaderDependencyChain;
		IPRMDependency firstHeaderNode;
		
		if (nVariables == 1) {
			// If there is no parent, this is going to be the first header's row:
			/*
			 * |-----------|---------------| 
			 * | State     |  Probability  |
			 * |---------------------------| 
			 * 
			 */
			column[0] = "State";
			column[1] = "Probability";
		} else {
			firstHeaderDependencyChain = node.getIncomingDependencyChains().get(0);
			firstHeaderNode = firstHeaderDependencyChain.getDependencyFrom();
			/*
			 * Ex: Here we get the variable "Parent 1" and set its name in 
			 *     the header. 
			 * 
			 * |------------------------------| 
			 * | Aggregate(Class1.Parent1)    |
			 * |------------------------------ 
			 * 
			 * TODO use "Aggregate(FKChain.Class1.Parent1)" format
			 * 
			 * or
			 * 
			 * |--------------| 
			 * | Parent 1     |
			 * |--------------- 
			 * 
			 */
			if (firstHeaderDependencyChain.getAggregateFunction() != null) {
				// inverse fk -> there must be an aggregate function
				column[0] = firstHeaderDependencyChain.getAggregateFunction().getName() 
							+ "(" 
							+ firstHeaderNode.getAttributeDescriptor().getPRMClass().getName()
							+ "."
							+ firstHeaderNode.getAttributeDescriptor().getName()
							+ ")";
			} else if (firstHeaderDependencyChain.getForeignKeyChain() != null && !firstHeaderDependencyChain.getForeignKeyChain().isEmpty() ) {
				// parent using ordinal FK -> no need for aggregate function
				column[0] = firstHeaderNode.getAttributeDescriptor().getPRMClass().getName()
				+ "."
				+ firstHeaderNode.getAttributeDescriptor().getName();
			} else {
				// parent from same class
				column[0] = firstHeaderNode.getAttributeDescriptor().getName();
			}
			for (int i = 0; i < data[0].length - 1; i++) {
				if (nVariables > 1) {
					// Reapeats all states in the node while there are cells to
					// fill.
					/*
					 * Ex: Following the example above. Here goes the state's header. 
					 * 
					 * |-------------------|-------------------| 
					 * | State 1 | State 2 | State 1 | State 2 |
					 * ----------------------------------------| 
					 * 
					 */
					column[i + 1] = firstHeaderNode.getAttributeDescriptor().getStateAt(i % firstHeaderNode.getAttributeDescriptor().getStatesSize());
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
		// The values are arranged in the potential this as a vector.
		/*
		 * Ex: This would be the vector in the potential this.
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
				data[r][c] = "" + df.format(this.getPrmCPT().getTableValues().get(n + r));
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
			data[i][0] = node.getAttributeDescriptor().getStateAt(i);
		}
		
		// Constructing the this so far.
		/*
		 * Ex: The this so far, following the example above.
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
		// Setup to allow grouping the header.
		this.setColumnModel(new GroupableTableColumnModel());
		this.setTableHeader(new GroupableTableHeader(
				(GroupableTableColumnModel) this.getColumnModel()));
		this.setModel(model);
		
		// Setup Column Groups
		GroupableTableColumnModel cModel = (GroupableTableColumnModel) this
				.getColumnModel();
		PRMColumnGroup cNodeGroup = null;
		PRMColumnGroup cNodeTempGroup = null;
		PRMColumnGroup cGroup = null;
		List<PRMColumnGroup> cGroupList = new ArrayList<PRMColumnGroup>();
		List<PRMColumnGroup> previousCGroupList = new ArrayList<PRMColumnGroup>();
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
		cModel.getColumn(0).setHeaderRenderer(new PRMGroupableTableCellRenderer());
		// Sets default color for node's states
		/*
		 * |--------------- 
		 * | Node State 1 | 
		 * | Node State 2 |
		 * | Node State 3 |
		 * |---------------
		 * 
		 */
		cModel.getColumn(0).setCellRenderer(new PRMGroupableTableCellRenderer(Color.BLACK, Color.YELLOW));
		// Fill all other headers, except the first (because it has been 
		// set already). It ignores k = 0 (the fist 
		// parent). Note that (nVariables - 1) == node.getIncomingDependencyChains().size()
		for (int k = 1; k < (nVariables - 1); k++) {
			// obtain the other parents from dependency chain
			IDependencyChain parentDependencyChain = node.getIncomingDependencyChains().get(k);	// this is an edge pointing from parent to this node
			IPRMDependency parent = parentDependencyChain.getDependencyFrom();
			int nPreviousParentStatesSize = node.getIncomingDependencyChains().get(k-1).getDependencyFrom().getAttributeDescriptor().getStatesSize();
			sizeColumn *= nPreviousParentStatesSize;
			// extract the name of the element of this header: "<AggregateFunction>.<NodeName>"
			String parentNameOnTableHeader;
			if (parentDependencyChain.getAggregateFunction() != null) {
				// parent using inverse FK -> need aggregate function
				// TODO use "Aggregate(FKChain.Class1.Parent1)" format
				parentNameOnTableHeader = parentDependencyChain.getAggregateFunction().getName() 
										+ "(" 
										+ parent.getAttributeDescriptor().getPRMClass().getName()
										+ "."
										+ parent.getAttributeDescriptor().getName()
										+ ")";
			} else if (parentDependencyChain.getForeignKeyChain() != null && !parentDependencyChain.getForeignKeyChain().isEmpty()) {
				// parent using direct FK  -> no need for aggregate function
				parentNameOnTableHeader = parent.getAttributeDescriptor().getPRMClass().getName()
										+ "."
										+ parent.getAttributeDescriptor().getName();
			} else {
				// parent of same class
				parentNameOnTableHeader = parent.getAttributeDescriptor().getName();
			}
			// Set the node name as a header in the first column
			if (!firstNode) {
				cNodeTempGroup = cNodeGroup;
				cNodeGroup = new PRMColumnGroup(new PRMGroupableTableCellRenderer(), parentNameOnTableHeader);
				cNodeGroup.add(cNodeTempGroup);
			} else {
				cNodeGroup = new PRMColumnGroup(new PRMGroupableTableCellRenderer(), parentNameOnTableHeader);
				cNodeGroup.add(cModel.getColumn(0));
			}
			columnIndex = 1;
			cGroup = null;
			while (columnIndex <= nStates) {
				for (int i = 0; i < parent.getAttributeDescriptor().getStatesSize(); i++) {
					cGroup = new PRMColumnGroup(parent.getAttributeDescriptor().getStateAt(i));
					if (!firstNode) {
						for (int j = 0; j < nPreviousParentStatesSize; j++) {
							PRMColumnGroup group = previousCGroupList.get(columnIndex-1);
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
			cGroupList = new ArrayList<PRMColumnGroup>();
			firstNode = false;
			// Update the number of states
			nStates /= nPreviousParentStatesSize;
		}
		// It adds all parents' node name as header
		if (cNodeGroup != null) {
			cModel.addColumnGroup(cNodeGroup);
		}
		// It adds only the first row (here it is the last parent's states) 
		// of the header that has all other headers (all other parent's states)
		// as sub-headers.
		if (previousCGroupList != null) {
			for (PRMColumnGroup group : previousCGroupList) {
				cModel.addColumnGroup(group);
			}
		}
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Shows the caret while editing cell.
		this.setSurrendersFocusOnKeystroke(true);
		
		// resize column so that the first column is allways bigger, if there are parents
		if (getPrmCPT().getPRMDependency().getIncomingDependencyChains() != null 
				&& !getPrmCPT().getPRMDependency().getIncomingDependencyChains().isEmpty()) {
			this.getColumnModel().getColumn(0).setPreferredWidth(200); //1st column is bigger
		}

	}

	/**
	 * Initialize listeners of this table
	 */
	protected void initListeners() {
		this.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getColumn() == 0 && getPrmCPT() != null) {
					// Change state name or reset to its previous value.
					if (!PRMTableGUI.this.getValueAt(e.getLastRow(), e.getColumn()).toString().trim().equals("")) {
						getPrmCPT().getPRMDependency().getAttributeDescriptor().setStateAt(
								PRMTableGUI.this.getValueAt(e.getLastRow(),e.getColumn()).toString(), 
								e.getLastRow() - (PRMTableGUI.this.getRowCount() - getPrmCPT().getPRMDependency().getAttributeDescriptor().getStatesSize()));
					} else {
						PRMTableGUI.this.revalidate();
						PRMTableGUI.this.setValueAt(
								getPrmCPT().getPRMDependency().getAttributeDescriptor().getStateAt(
											e.getLastRow() - (PRMTableGUI.this.getRowCount() - getPrmCPT().getPRMDependency().getAttributeDescriptor().getStatesSize())), 
								e.getLastRow(), 
								e.getColumn());
					}
				} else if (getPrmCPT() != null) {
					// Change the CPT cell or reset to its previous value.
					// extract value from JTable
					String valueText = PRMTableGUI.this.getValueAt(e.getLastRow(),e.getColumn()).toString().replace(',', '.');
					try {
						// update node's table
						getPrmCPT().getTableValues().set(
								(e.getColumn() - 1) * getPrmCPT().getPRMDependency().getAttributeDescriptor().getStatesSize() + e.getLastRow(), 
								Float.parseFloat(valueText));
					} catch (NumberFormatException nfe) {
						// Just shows the error message if the value is not empty.
						if (!valueText.trim().equals("")) {
							JOptionPane.showMessageDialog(
									null, 
									"Invalid number format", 
									"Number format error",
									JOptionPane.ERROR_MESSAGE);
						}
						PRMTableGUI.this.revalidate();
						// revert value
						PRMTableGUI.this.setValueAt(
								(getPrmCPT().getTableValues().get((e.getColumn() - 1) * getPrmCPT().getPRMDependency().getAttributeDescriptor().getStatesSize() + e.getLastRow())).toString(),
								e.getLastRow(), 
								e.getColumn());
					}
				}
			}
		});
		
		// Change the text cell editor to replace text instead of appending it for all columns.
		ReplaceTextCellEditor cellEditor = new ReplaceTextCellEditor();
		for (int i = 0; i < this.getColumnModel().getColumnCount(); i++) {
			this.getColumnModel().getColumn(i).setCellEditor(cellEditor);
		}
			
		
	}
	
	

	/* (non-Javadoc)
	 * @see javax.swing.JTable#updateUI()
	 */
	public void updateUI() {
		this.resetComponents();
		super.updateUI();
	}

	/**
	 * @return the prmDependency
	 */
	public IPRMCPT getPrmCPT() {
		return prmCPT;
	}

	/**
	 * @param prmDependency the prmDependency to set
	 */
	public void setPrmCPT(IPRMCPT prmCPT) {
		this.prmCPT = prmCPT;
	}

	/**
	 * This is a workarownd to fix a bug in {@link GroupableTableCellRenderer}
	 * @author Shou Matsumoto
	 *
	 */
	public class PRMGroupableTableCellRenderer extends GroupableTableCellRenderer {
		
		/**
		 * @see GroupableTableCellRenderer#GroupableTableCellRenderer()
		 */
		protected PRMGroupableTableCellRenderer() {
			super();
		}

		/**
		 * @param foregroundColor
		 * @param backgroundColor
		 * @see GroupableTableCellRenderer#GroupableTableCellRenderer(Color, Color)
		 */
		protected PRMGroupableTableCellRenderer(Color foregroundColor,
				Color backgroundColor) {
			super(foregroundColor, backgroundColor);
		}

		/*
		 * (non-Javadoc)
		 * @see unbbayes.gui.table.GroupableTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean selected, boolean focused, int row, int column) {
			if (table != null) {
				return super.getTableCellRendererComponent(table, value, selected, focused, row, column);
			} else {
				setForeground(Color.WHITE);
				setBackground(Color.BLUE);
				setHorizontalAlignment(SwingConstants.CENTER);
				setText(value != null ? value.toString() : " ");
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				return this;
			}
		}
	}
	
	/**
	 * This is a workaround to fix bug in {@link ColumnGroup}
	 * @author Shou Matsumoto
	 *
	 */
	public class PRMColumnGroup extends ColumnGroup {

		/**
		 * @see ColumnGroup#ColumnGroup(String)
		 * @param text
		 */
		protected PRMColumnGroup(String text) {
			this(new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                            JTableHeader header = null;
                            if (table != null) {
                            	header = table.getTableHeader();
                            }
                            if (header != null) {
                                setForeground(header.getForeground());
                                setBackground(header.getBackground());
                                setFont(header.getFont());
                            }
                            setHorizontalAlignment(JLabel.CENTER);
                            setText((value == null) ? "" : value.toString());
                            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                            return this;
                        }
                    },text);
		}

		/**
		 * 
		 * @see ColumnGroup#ColumnGroup(TableCellRenderer, String)
		 * @param renderer
		 * @param text
		 */
		protected PRMColumnGroup(TableCellRenderer renderer, String text) {
			super(renderer, text);
		}
		
	}
	
}
