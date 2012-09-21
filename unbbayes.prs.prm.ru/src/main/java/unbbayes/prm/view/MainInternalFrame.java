/**
 * 
 */
package unbbayes.prm.view;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import unbbayes.gui.NetworkWindow;
import unbbayes.io.BaseIO;
import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.controller.dao.imp.DBControllerImp;
import unbbayes.prm.controller.prm.IPrmController;
import unbbayes.prm.controller.prm.PrmController;
import unbbayes.prm.view.dialogs.DialogNewDBSchema;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;

/**
 * Main PRM internal window.
 * 
 * @author David Saldaña
 * 
 */
public class MainInternalFrame extends UnBBayesModule {
	/**
	 * Default serial version.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Logger
	 */
	Logger log = Logger.getLogger(MainInternalFrame.class);

	/**
	 * Module name.
	 */
	private static final String MODULE_NAME = "PRM - RU";

	/**
	 * Default constructor.
	 * 
	 * @param title
	 *            Window title
	 */
	public MainInternalFrame(String title) {
		setTitle(title);

		// Select a source of DB schema.
		DialogNewDBSchema newDBSchema = new DialogNewDBSchema();
		newDBSchema.setModal(true);
		newDBSchema.setVisible(true);

		// If the database source was selected.
		if (newDBSchema.isDialogAccepted()) {
			String urlDB = newDBSchema.getUrl();

			// Database controller
			IDBController sl = new DBControllerImp();
			sl.init(urlDB);

			// PRM controller
			IPrmController prmController = new PrmController();

			// Graphic
			// RelationalGraphicator relGraphicator = new RelationalGraphicator(
			// dbSchema);
			PRMProcessPanel relGraphicator = new PRMProcessPanel(sl,
					prmController, this);
			this.add(relGraphicator);

		} else {
			this.dispose();
		}
	}

	/*
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	public String getModuleName() {
		return MODULE_NAME;
	}

	/*
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File file) throws IOException {
		log.debug("Open a file " + file.getName());
		// TODO open definition file file
		return null;
	}

	/*
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		// return controller.getIO();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return null;
		// return this.getPrm();
	}

	/**
	 * Delegate the graph to the correct window that can render the graph. It
	 * may use pluggin support.
	 * 
	 * @param graph
	 */
	public void delegateToGraphRenderer(Graph graph) {
		Debug.println(this.getClass(), "Opening compiled network: " + graph);
		if (graph instanceof ProbabilisticNetwork) {
			NetworkWindow bnWindow = new NetworkWindow(
					(ProbabilisticNetwork) graph);
			this.getUnbbayesFrame().addWindow(bnWindow);
			
			bnWindow.changeToPNCompilationPane();
			bnWindow.setVisible(true);
		}
	}
}
