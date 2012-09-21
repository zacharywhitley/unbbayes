package unbbayes.prm.util.helper;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

public class DBSchemaHelper {
	private static Logger log = Logger.getLogger(DBSchemaHelper.class);

	public static Column getUniqueIndex(Table t) {
		Index[] indices = t.getUniqueIndices();

		// we assume there is only one index.
		if (indices.length == 1) {
			return t.findColumn(indices[0].getName());
		} else if (indices.length > 1) {
			log.error("More than one unique index for table is not supported yet.");
		}
		return null;
	}
}
