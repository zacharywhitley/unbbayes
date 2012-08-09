/**
 * 
 */
package unbbayes.gui.prm;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import unbbayes.controller.ConfigurationsController;
import unbbayes.controller.IconController;
import unbbayes.controller.prm.IPRMController;
import unbbayes.controller.prm.PRMController;
import unbbayes.gui.NetworkWindow;
import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.PRM;
import unbbayes.prs.prm.PRMClass;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;

/**
 * Main PRM internal window
 * 
 * @author Shou Matsumoto
 * 
 */
public class PRMWindow extends UnBBayesModule {

	private IPRMController controller;

	private IPRM prm;

	private JTabbedPane tabPanel;

	private IconController iconController;

	private SchemaPanel schemaPanel;

	private SkeletonPanel skeletonPanel;

	private JLabel status;

	private JPanel topPanel;

	/**
	 * @return the prm
	 */
	public IPRM getPrm() {
		return prm;
	}

	/**
	 * @param prm
	 *            the prm to set
	 */
	public void setPrm(IPRM prm) {
		this.prm = prm;
	}

	/**
	 * At least one constructor must be visible for subclasses in order to allow
	 * inheritance.
	 */
	protected PRMWindow() {
		controller = PRMController.newInstance();
		iconController=IconController.getInstance();
	}

	/**
	 * Default constructor method
	 * 
	 * @return
	 */
	public static PRMWindow newInstance() {
		PRMWindow ret = new PRMWindow();
		ret.setPrm(PRM.newInstance("NEW PRM PROJECT"));
		ret.getPrm()
				.addPRMClass(PRMClass.newInstance(ret.getPrm(), "NewClass"));
		ret.initComponents();
		ret.initListeners();
		return ret;
	}

	/**
	 * Constructor method using parameters
	 * 
	 * @param title
	 */
	public static PRMWindow newInstance(String title) {
		PRMWindow ret = PRMWindow.newInstance();
		ret.setTitle(title);
		return ret;
	}

	/**
	 * Constructor method using parameters
	 * 
	 * @param prm
	 */
	public static PRMWindow newInstance(IPRM prm) {
		// assertion
		if (prm == null) {
			return PRMWindow.newInstance();
		}
		PRMWindow ret = new PRMWindow();
		ret.setPrm(prm);
		ret.initComponents();
		ret.initListeners();
		return ret;
	}

	/**
	 * Build up the actual GUI
	 */
	protected void initComponents() {

		topPanel = new JPanel(new BorderLayout());

		// adding a tab pane as the top container
		tabPanel=(new JTabbedPane());
		topPanel.add(tabPanel, BorderLayout.CENTER);

		// adding status label
		status = (new JLabel("Ready"));
		topPanel.add(status, BorderLayout.SOUTH);

		this.getContentPane().add(topPanel);

		// adding schema panel
		schemaPanel=(SchemaPanel.newInstance(this, controller,
				this.getPrm()));
		tabPanel.addTab("Schema",
				iconController.getTableIcon(), schemaPanel);

		// Adding skeleton panel using the same prm class list (the list is a
		// tree because we've foreseen PRM class hierarchy)
		skeletonPanel= SkeletonPanel.newInstance(this, controller,
				this.getPrm());
		tabPanel.addTab("Skeleton",
				iconController.getCompileIcon(),
				skeletonPanel);
	}

	/**
	 * Initialize listeners
	 */
	protected void initListeners() {
		// update panels when tabs are switched
		tabPanel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				try {
					skeletonPanel.resetComponents();
				} catch (Exception exc) {
					Debug.println(this.getClass(),
							"Could not reset sekeleton panel", exc);
				}
			}

			public void mouseReleased(MouseEvent e) {
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	public String getModuleName() {
		return "PRM - ALPHA";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File arg0) throws IOException {
		Graph g = null;

		// This IO is instantiated at PRMController' constructor.
		BaseIO io = this.getIO();

		try {
			g = io.load(arg0);
		} catch (FileExtensionIODelegator.MoreThanOneCompatibleIOException e) {
			// More than one I/O was found to be compatible. Ask user to select
			// one.
			String[] possibleValues = FileExtensionIODelegator
					.getNamesFromIOs(e.getIOs());
			String selectedValue = (String) JOptionPane.showInputDialog(this,
					"Select I/O handler", "Conflict",
					JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
					possibleValues[0]);
			if (selectedValue != null) {
				g = FileExtensionIODelegator.findIOByName(e.getIOs(),
						selectedValue).load(arg0);
			} else {
				// user appears to have cancelled
				this.dispose();
				return null;
			}
		}

		PRMWindow window = null;

		try {
			ConfigurationsController.getInstance().addFileToListRecentFiles(
					arg0);
			window = PRMWindow.newInstance((IPRM) g);
			window.setName(arg0.getName().toLowerCase());
		} catch (Exception e) {
			throw new RuntimeException("Unsupported graph format", e);
		}

		// we do not use this current instance. Instead, dispose it and return
		// the new instance of window
		this.dispose();
		return window;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return controller.getIO();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.getPrm();
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
			bnWindow.setVisible(true);
		}
	}
}
