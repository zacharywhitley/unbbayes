package unbbayes.gui.table;
/*
 * (swing1.1beta3)
 * 
 */

import java.util.*;

import javax.swing.table.*;

/**
  * GroupableTableHeader
  *
  * @version 1.0 10/20/98
  * @author Rommel N Carvalho
  */

public class GroupableTableHeader extends JTableHeader {
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

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
		Enumeration enumeration = columnGroups.elements();
		while (enumeration.hasMoreElements()) {
			ColumnGroup cGroup = (ColumnGroup) enumeration.nextElement();
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
		Enumeration enumeration = columnGroups.elements();
		while (enumeration.hasMoreElements()) {
			ColumnGroup cGroup = (ColumnGroup) enumeration.nextElement();
			cGroup.setColumnMargin(columnMargin);
		}
	}

}
