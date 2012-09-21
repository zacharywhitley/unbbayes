package unbbayes.prm.view.instances;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.util.helper.DBSchemaHelper;
import unbbayes.prm.view.graphicator.editor.TableModelWithGraphics;
import unbbayes.prm.view.graphicator.editor.TableRenderer;

public class InstancesTableViewer extends JPanel implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(InstancesTableViewer.class);

	/**
	 * Component to show the table.
	 */
	private JTable graphicTable;

	/**
	 * Table to show.
	 */
	private Table table;

	/**
	 * Data stored in the db.
	 */
	private Object[][] data;

	/**
	 * Listener.
	 */
	private IInstanceTableListener listener;

	/**
	 * This is the index of the column that represents the table index.
	 */
	private int indexColumn;

	private Column uniqueIndexColumn;

	/**
	 * Constructor.
	 * 
	 * @param table
	 *            Database table to draw.
	 * @param dataIterator
	 *            data to insert in the table.
	 */
	public InstancesTableViewer(Table table, Iterator<DynaBean> dataIterator,
			IInstanceTableListener listener) {
		this.table = table;
		this.listener = listener;
		setLayout(new GridLayout(1, 1));

		graphicTable = new JTable();
		graphicTable.setModel(getTableModel(dataIterator));

		// Render for null values.
		for (int i = 0; i < table.getColumnCount(); i++) {
			graphicTable.getColumnModel().getColumn(i)
					.setCellRenderer(new MyRenderer());
		}

		add(new JScrollPane(graphicTable));

		graphicTable.addMouseListener(this);
	}

	private TableModel getTableModel(Iterator<DynaBean> dataIterator) {
		int columns = table.getColumnCount();

		// Column names
		String[] columnNames = new String[columns];
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = table.getColumn(i).getName();

			// Column Index
			if (table.getColumn(i).isPrimaryKey()) {
				indexColumn = i;
				uniqueIndexColumn = table.getColumn(i);
			}
		}

		List<Object[]> rows = new ArrayList<Object[]>();
		int cont = 0;
		// Iterate over data.
		while (dataIterator.hasNext()) {
			DynaBean bean = dataIterator.next();
			Object[] row = new Object[columns];

			// Get each value from iterator
			for (int i = 0; i < columns; i++) {
				Object cellValue = bean.get(columnNames[i]);
				row[i] = cellValue;
			}
			rows.add(row);
			log.debug("Agregar fila" + cont++);
		}

		data = rows.toArray(new Object[0][0]);

		TableModel tableModel = new TableModelWithGraphics(columnNames, data);
		return tableModel;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = graphicTable.getSelectedRow();
		int column = graphicTable.getSelectedColumn();

		// Index value
		Object indexValue = data[row][indexColumn];

		// value
		Object value = data[row][column];

		// Notify only when there is data without evidence.
		if (value == null) {
			log.debug("Value=" + value);
			
			
			listener.attributeSelected(table, uniqueIndexColumn, indexValue,
					table.getColumn(column), value);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This class is created to renderer the icon to show the null data as a
	 * image.
	 * 
	 * @author David Saldaña.
	 * 
	 */
	class MyRenderer extends DefaultTableCellRenderer {
		ImageIcon icon = new ImageIcon(
				TableRenderer.class.getResource(TableRenderer.IMAGE_PATH
						+ "question.png"));
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object,
		 * boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (value == null) {
				setIcon(icon);
				setText("");
			} else {
				setIcon(null);
				setText(String.valueOf(value));
			}
			return this;
		}
	}
}
