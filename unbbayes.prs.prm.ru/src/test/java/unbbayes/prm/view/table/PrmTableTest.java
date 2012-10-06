package unbbayes.prm.view.table;

import javax.swing.JFrame;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.sun.msv.reader.xmlschema.AttributeState;

import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prm.view.graphicator.PrmTable;

public class PrmTableTest {
	private static String DB_URL = "jdbc:derby:examples/movies/MovieTest.db";

	public PrmTableTest() throws Exception {
		// Get schema
		DBControllerImp ds = new DBControllerImp();
		ds.init(DB_URL);

		Database relSchema = ds.getRelSchema();

		// Parent 1
		Table table1 = relSchema.getTable(0);
		Column column1 = table1.getColumn(0);
		Attribute attribute1 = new Attribute(table1, column1);
		AttributeStates paretStates1 = new AttributeStates(attribute1,
				new String[] { "state1-0", "state1-1" });

		// Parent 2
		Table table2 = relSchema.getTable(1);
		Column column2 = table2.getColumn(0);
		Attribute attribute2 = new Attribute(table2, column2);
		AttributeStates paretStates2 = new AttributeStates(attribute2,
				new String[] { "state2-0", "state2-1" });

		// Child
		Table tableChild = relSchema.getTable(2);
		Column columnChild = tableChild.getColumn(1);
		Attribute attribute = new Attribute(tableChild, columnChild);
		AttributeStates attChild = new AttributeStates(attribute, new String[] {
				"childState1", "childState2" });

		// Show table
		JFrame frame = new JFrame();

		PrmTable prmTable = new PrmTable(new AttributeStates[] { paretStates1,
				paretStates2 }, attChild, frame);

		frame.getContentPane().add(prmTable);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(870, 640);
		frame.setVisible(true);
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new PrmTableTest();

	}

}
