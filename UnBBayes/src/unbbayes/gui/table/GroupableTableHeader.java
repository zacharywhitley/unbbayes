package unbbayes.gui.table;
/*
 * (swing1.1beta3)
 * 
 */

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
  * GroupableTableHeader
  *
  * @version 1.0 10/20/98
  * @author Rommel N Carvalho
  */

public class GroupableTableHeader extends JTableHeader {
	private static final String uiClassID = "GroupableTableHeaderUI";
	protected Vector columnGroups = null;

	public GroupableTableHeader(TableColumnModel model) {
		super(model);
		setUI(new GroupableTableHeaderUI());
		setReorderingAllowed(false);
	}

	public void setReorderingAllowed(boolean b) {
		reorderingAllowed = false;
	}

	public void addColumnGroup(ColumnGroup g) {
		if (columnGroups == null) {
			columnGroups = new Vector();
		}
		columnGroups.addElement(g);
	}

	public Enumeration getColumnGroups(TableColumn col) {
		if (columnGroups == null)
			return null;
		Enumeration enum = columnGroups.elements();
		while (enum.hasMoreElements()) {
			ColumnGroup cGroup = (ColumnGroup) enum.nextElement();
			Vector v_ret = (Vector) cGroup.getColumnGroups(col, new Vector());
			if (v_ret != null) {
				return v_ret.elements();
			}
		}
		return null;
	}

	public void setColumnMargin() {
		if (columnGroups == null)
			return;
		int columnMargin = getColumnModel().getColumnMargin();
		Enumeration enum = columnGroups.elements();
		while (enum.hasMoreElements()) {
			ColumnGroup cGroup = (ColumnGroup) enum.nextElement();
			cGroup.setColumnMargin(columnMargin);
		}
	}

}
