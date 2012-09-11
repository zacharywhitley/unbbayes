/**
 * 
 */
package unbbayes.prm.view;

import javax.swing.JFrame;

import org.apache.ddlutils.model.Table;

import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.view.graphicator.IGraphicTableListener;
import unbbayes.prm.view.graphicator.RelationalGraphicator;

/**
 * @author David Saldaña
 * 
 */
public class RelGraphicatorTest {
	private static String DB_URL = "jdbc:derby:examples/movies/MovieTest.db";

	public RelGraphicatorTest() {
		// Get schema
		DBControllerImp ds = new DBControllerImp();
		ds.init(DB_URL);

		// Graphics
		RelationalGraphicator rl = new RelationalGraphicator(ds.getRelSchema(),
				new IGraphicTableListener() {

					@Override
					public void selectedAttributes(Attribute[] attributes) {
						System.out.println("selected attributes");

					}

					@Override
					public void selectedAttribute(Attribute attribute) {
						System.out.println("Selected atribute");
					}

					@Override
					public void selectedTable(Table t) {
						
						
					}
				});
		rl.showPalette();

		JFrame frame = new JFrame();
		frame.getContentPane().add(rl);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setJMenuBar(menuBar);
		frame.setSize(870, 640);
		frame.setVisible(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RelGraphicatorTest();
	}

}
