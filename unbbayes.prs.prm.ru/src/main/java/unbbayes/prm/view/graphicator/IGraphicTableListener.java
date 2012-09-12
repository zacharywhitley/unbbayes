package unbbayes.prm.view.graphicator;


import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import unbbayes.prm.model.Attribute;


public interface IGraphicTableListener {
	void selectedTable(Table t);

	void selectedAttributes(Attribute[] attributes);
	
	void selectedAttribute(Attribute attribute);
	
	void selectedCPD(Attribute attribute);
}
