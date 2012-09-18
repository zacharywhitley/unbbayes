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
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.view.graphicator.editor.TableModelWithGraphics;
import unbbayes.prm.view.graphicator.editor.TableRenderer;

public class InstancesTableViewer extends JPanel implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(InstancesTableViewer.class);

	private JTable graphicTable;

	private Table table;

	private Object[][] data;

	private IInstanceTableListener listener;

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

				// if (cellValue == null) {
				// cellValue = new ImageIcon(
				// TableRenderer.class
				// .getResource(TableRenderer.IMAGE_PATH
				// + "question.png"));
				// }
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

		Object val = data[row][column];

		// Notify only when there is data without evidence.
		if (val == null) {
			log.debug("Value=" + val);
			listener.attributeSelected(data, row, column, table);
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
