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
	 * @param prm the prm to set
	 */
	public void setPrm(IPRM prm) {
		this.prm = prm;
	}

	/**
	 * At least one constructor must be visible for subclasses in order to allow
	 * inheritance.
	 */
	protected PRMWindow() {
		this.setController(PRMController.newInstance());
		this.setIconController(IconController.getInstance());
	}
	
	/**
	 * Default constructor method
	 * @return
	 */
	public static PRMWindow newInstance() {
		PRMWindow ret = new PRMWindow();
		ret.setPrm(PRM.newInstance("NEW PRM PROJECT"));
		ret.getPrm().addPRMClass(PRMClass.newInstance(ret.getPrm(),"NewClass"));
		ret.initComponents();
		ret.initListeners();
		return ret;
	}

	

	/**
	 * Constructor method using parameters
	 * @param title
	 */
	public static PRMWindow newInstance(String title) {
		PRMWindow ret = PRMWindow.newInstance();
		ret.setTitle(title);
		return ret;
	}
	
	/**
	 * Constructor method using parameters
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
		this.setTabPanel(new JTabbedPane());
		this.getTopPanel().add(this.getTabPanel(),BorderLayout.CENTER);
		
		// adding status label
		this.setStatusLabel(new JLabel("Ready"));
		this.getTopPanel().add(this.getStatusLabel(), BorderLayout.SOUTH);

		this.getContentPane().add(this.getTopPanel());
		
		
		// adding schema panel
		this.setSchemaPanel(SchemaPanel.newInstance(this, this.getController(), this.getPrm()));
		this.getTabPanel().addTab("Schema", this.getIconController().getTableIcon(), this.getSchemaPanel());
		
		// adding scheleton panel using the same prm class list (the list is a tree because we've foreseen PRM class hierarchy)
		this.setSkeletonPanel(SkeletonPanel.newInstance(this, this.getController(), this.getPrm()));
		this.getTabPanel().addTab("Skeleton", this.getIconController().getCompileIcon(), this.getSkeletonPanel());
	}
	
	/**
	 * Initialize listeners
	 */
	protected void initListeners() {
		// update panels when tabs are switched
		this.getTabPanel().addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				try{
					getSkeletonPanel().resetComponents();
				} catch (Exception exc) {
					Debug.println(this.getClass(), "Could not reset sekeleton panel", exc);
				}
			}
			public void mouseReleased(MouseEvent e) {}
		});
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	public String getModuleName() {
		return "PRM - ALPHA";
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	public UnBBayesModule openFile(File arg0) throws IOException {
		Graph g = null;
		
		// This IO is instantiated at PRMController' constructor.
		BaseIO io = this.getIO();
		
		try {
			g = io.load(arg0);
		} catch (FileExtensionIODelegator.MoreThanOneCompatibleIOException e) {
			// More than one I/O was found to be compatible. Ask user to select one.
			String[] possibleValues = FileExtensionIODelegator.getNamesFromIOs(e.getIOs());
	    	String selectedValue = (String)JOptionPane.showInputDialog(
	    			this, 
	    			"Select I/O handler", 
	    			"Conflict",
	    			JOptionPane.INFORMATION_MESSAGE, 
	    			null,
	    			possibleValues, 
	    			possibleValues[0]);
	    	if (selectedValue != null) {
	    		g = FileExtensionIODelegator.findIOByName(e.getIOs(), selectedValue).load(arg0);
	    	} else {
	    		// user appears to have cancelled
	    		this.dispose();
		    	return null;
	    	}
		}
		
		PRMWindow window = null;
		
		try {
			ConfigurationsController.getInstance().addFileToListRecentFiles(arg0); 
			window = PRMWindow.newInstance((IPRM)g);	
			window.setName(arg0.getName().toLowerCase()); 
		} catch (Exception e) {
			throw new RuntimeException("Unsupported graph format",e);
		}
		
		// we do not use this current instance. Instead, dispose it and return the new instance of window
		this.dispose();
		return window;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return this.getController().getIO();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.getPrm();
	}
	
	/**
	 * Delegate the graph to the correct window that can render the graph.
	 * It may use pluggin support.
	 * @param graph
	 */
	public void delegateToGraphRenderer(Graph graph) {
		Debug.println(this.getClass(), "Opening compiled network: " + graph);
		if (graph instanceof ProbabilisticNetwork) {
			NetworkWindow bnWindow = new NetworkWindow((ProbabilisticNetwork)graph);
			this.getUnbbayesFrame().addWindow(bnWindow);
			bnWindow.setVisible(true);
		}
	}
	
	

	/**
	 * @return the controller
	 */
	public IPRMController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(IPRMController controller) {
		this.controller = controller;
	}

	/**
	 * @return the tabPanel
	 */
	public JTabbedPane getTabPanel() {
		return tabPanel;
	}

	/**
	 * @param tabPanel the tabPanel to set
	 */
	public void setTabPanel(JTabbedPane tabPanel) {
		this.tabPanel = tabPanel;
	}

	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return iconController;
	}

	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}

	/**
	 * @return the schemaPanel
	 */
	public SchemaPanel getSchemaPanel() {
		return schemaPanel;
	}

	/**
	 * @param schemaPanel the schemaPanel to set
	 */
	public void setSchemaPanel(SchemaPanel schemaPanel) {
		this.schemaPanel = schemaPanel;
	}

	/**
	 * @return the skeletonPanel
	 */
	public SkeletonPanel getSkeletonPanel() {
		return skeletonPanel;
	}

	/**
	 * @param skeletonPanel the skeletonPanel to set
	 */
	public void setSkeletonPanel(SkeletonPanel skeletonPanel) {
		this.skeletonPanel = skeletonPanel;
	}

	/**
	 * @return the status
	 */
	public JLabel getStatusLabel() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatusLabel(JLabel status) {
		this.status = status;
	}

	/**
	 * @return the topPanel
	 */
	public JPanel getTopPanel() {
		return topPanel;
	}

	/**
	 * @param topPanel the topPanel to set
	 */
	public void setTopPanel(JPanel topPanel) {
		this.topPanel = topPanel;
	}

	/**
	 * The status
	 * @param status
	 */
	public void setStatus(String status) {
		this.getStatusLabel().setText(status);
	}
	
	/**
	 * The status
	 * @return
	 */
	public String getStatus() {
		return this.getStatusLabel().getText();
	}
	
}
