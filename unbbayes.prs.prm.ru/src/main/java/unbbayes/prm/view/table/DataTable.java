package unbbayes.prm.view.table;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

public class DataTable extends JPanel {
	private static Logger log = Logger.getLogger(DataTable.class);

	private JTable graphicTable;

	private Table table;

	public DataTable(Table table, Iterator<DynaBean> dataIterator) {
		this.table = table;
		setLayout(new GridLayout(1, 1));

		graphicTable = new JTable();
		graphicTable.setModel(getTableModel(dataIterator));

		add(new JScrollPane(graphicTable));
	}

	private TableModel getTableModel(Iterator<DynaBean> dataIterator) {
		int columns = table.getColumnCount();
		
		// Column names
		String[] columnNames = new String[columns];
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i]= table.getColumn(i).getName();
		}
		
		
				
		List<String[]> rows = new ArrayList<String[]>();
		int cont=0;
		// Iterate over data
		while (dataIterator.hasNext()) {
			DynaBean bean = dataIterator.next();
			String[] row = new String[columns];
			
			// get each value from iterator
			for (int i = 0; i < columns; i++) {
				row[i] = String.valueOf(bean.get(columnNames[i]));
			}
			rows.add(row);
			log.debug("Agregar fila" + cont++);
		}
		
		
		
		Object[][] a = rows.toArray(new String[0][0]);
		
	
		DefaultTableModel tableModel = new DefaultTableModel(a, columnNames);
		return tableModel;
	}
}


