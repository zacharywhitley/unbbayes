package unbbayes.gui.table;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Displays JLabel for "editing" - i.e. clicking.
 */
public class ReplaceTextCellEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;

	protected JTextField textField = new JTextField();

	public ReplaceTextCellEditor() {
		super(new JTextField());
		this.textField = (JTextField) this.editorComponent;
	}

	public void setValue(Object value) {
		textField.setText((value != null) ? value.toString() : "");
	}

	public Object getCellEditorValue() {
		return textField.getText();
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		if (isSelected == false) {
			return null;
		}

		this.textField.setText("");

		return this.textField;
	}

}
