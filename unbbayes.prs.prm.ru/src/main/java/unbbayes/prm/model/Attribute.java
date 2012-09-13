package unbbayes.prm.model;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * This class represents an attribute or a column of a table. It is required
 * because the ddlutils model does not take into account that a column does not
 * have a table.
 * 
 * @author David Saldaña
 * 
 */
public class Attribute {

	/**
	 * Table that has the attribute.
	 */
	private Table table;

	/**
	 * Interested attribute.
	 */
	private Column attribute;

	public Attribute(Table table, Column attribute) {
		this.table = table;
		this.attribute = attribute;

	}

	public Column getAttribute() {
		return attribute;
	}

	public void setAttribute(Column attribute) {
		this.attribute = attribute;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public Table getTable() {
		return table;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute)) {
			return false;
		}
		Attribute ext = (Attribute) obj;

		boolean sameTableName = this.getTable().getName()
				.equals(ext.getTable().getName());
		boolean sameAttName = this.getAttribute().getName()
				.equals(ext.getAttribute().getName());

		return sameAttName && sameAttName;
	}
}
