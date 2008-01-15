package unbbayes.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Cell renderer changing foreground and background colors.
 */
public class GroupableTableCellRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3785644336203425095L;
	private Color foregroundColor = Color.WHITE;
	private Color backgroundColor = Color.BLUE;
	
	public GroupableTableCellRenderer() {
		
	}
	
	public GroupableTableCellRenderer(Color foregroundColor, Color backgroundColor) {
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * 
	 * @param table
	 * @param value
	 * @param selected
	 * @param focused
	 * @param row
	 * @param column
	 * @return
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean selected, boolean focused, int row, int column) {
		JTableHeader header = table.getTableHeader();
		if (header != null) {
			setForeground(foregroundColor);
			setBackground(backgroundColor);
		}
		setHorizontalAlignment(SwingConstants.CENTER);
		setText(value != null ? value.toString() : " ");
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		return this;
	}
}
