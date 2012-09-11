package unbbayes.prm.view.table;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PrmTable extends JPanel {
	private JTable table;

	/**
	 * Create the panel.
	 */
	public PrmTable() {
		
		table = new JTable();
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null},
				{null, null, null},
				{null, null, null},
			},
			new String[] {
				"New column", "New column", "New column"
			}
		));
		add(table);

	}

}
