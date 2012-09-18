package unbbayes.prm.view.graphicator.editor;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

public class TableModelWithGraphics extends AbstractTableModel {

	
	private String[] colNames;
	private Object[][] data;

	public TableModelWithGraphics(String[] colNames, Object data[][]) {
		this.colNames = colNames;
		this.data = data;
	}
	
	/**
    *
    */
	private static final long serialVersionUID = -3642207266816170738L;

	public int getColumnCount() {
		return colNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return colNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public Class<? extends Object> getColumnClass(int c) {
		Object value = getValueAt(0, c);
		return (value != null) ? value.getClass() : ImageIcon.class;
	}

	/*
	 * The table is not editable.
	 */
	public boolean isCellEditable(int row, int col) {
		return col == 0;
	}

	/*
	 * Don't need to implement this method unless your table's data can
	 * change.
	 */
	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

}
