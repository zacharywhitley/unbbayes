package unbbayes.prm.view.table;

import javax.swing.JFrame;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.view.instances.InstancesTableViewer;

public class InstancesTableTest {
	private static String DB_URL = "jdbc:derby:examples/movies/MovieTest.db";

	public InstancesTableTest() throws Exception {
		// Get schema
		DBControllerImp ds = new DBControllerImp();
		ds.init(DB_URL);

		Database relSchema = ds.getRelSchema();

		// Parent 1
		Table t = relSchema.getTable(0);
		InstancesTableViewer dt = new InstancesTableViewer(t,
				ds.getTableValues( t), null);

		// Show table
		JFrame frame = new JFrame();
		frame.getContentPane().add(dt);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(870, 640);
		frame.setVisible(true);
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new InstancesTableTest();

	}

}
