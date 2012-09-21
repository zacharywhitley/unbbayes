package unbbayes.prm.view.instances;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

public interface IInstanceTableListener {

	void attributeSelected(Table table, Column uniqueIndexColumn,
			Object indexValue, Column column, Object value);
}
