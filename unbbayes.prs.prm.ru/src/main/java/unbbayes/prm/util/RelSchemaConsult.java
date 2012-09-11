package unbbayes.prm.util;

import java.util.HashMap;

import org.apache.commons.collections.map.HashedMap;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

public class RelSchemaConsult {

	private Database database;

	/**
	 * Index tables by name.
	 * 
	 * Key: table name Value: table object
	 */
	private HashMap<String, Table> tables;

	public RelSchemaConsult(Database database) {
		this.database = database;
		tables = new HashMap<String, Table>();

		// Index tables
		Table[] tables2 = database.getTables();
		for (Table table : tables2) {
			tables.put(table.getName(), table);
		}
	}

	public Table getTableByName(String tableName) {
		return tables.get(tableName);

	}
}
