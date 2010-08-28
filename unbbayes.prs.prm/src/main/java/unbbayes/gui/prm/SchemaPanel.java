/**
 * 
 */
package unbbayes.gui.prm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import unbbayes.controller.IconController;
import unbbayes.controller.prm.IPRMController;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.builders.IPRMClassBuilder;
import unbbayes.prs.prm.builders.PRMClassBuilder;
import unbbayes.util.Debug;

/**
 * Panel for PRM class (entity) edition
 * @author Shou Matsumoto
 *
 */
public class SchemaPanel extends JPanel {
	
	public static String EDITION_PANE = "editionPane";
	
	private IPRMController controller;
	
	private JTree prmTables;
	private IPRM prm;
	
	private JScrollPane tableScrollPane;
	private JScrollPane editionScrollPane;
	private JSplitPane splitPane;
	private JToolBar classTreeEditionToolBar;
	private JButton addPRMClassButton;
	private IconController iconController;
	private JButton removePRMClassButton;

	private JPanel classTreeEditionPanel;

	private PRMWindow prmWindow;
	

	/**
	 * At least one constructor must be protected in order to
	 * allow inheritance.
	 */
	protected SchemaPanel() {
		super();
		this.setLayout(new BorderLayout(5,5));
		this.iconController = IconController.getInstance();
	}

	/**
	 * Default construction method
	 * @param prm : the PRM to be displayed by this panel
	 * @return
	 */
	public static SchemaPanel newInstance(PRMWindow prmWindow, IPRMController controller, IPRM prm) {
		SchemaPanel ret = new SchemaPanel();
		ret.prm = prm;
		ret.prmWindow = prmWindow;
		ret.controller = controller;
		ret.initComponents();
		ret.initListeners();
		return ret;
	}
	
	
	/**
	 * Resets and build up the components
	 */
	protected void initComponents() {
		this.removeAll();
		
		this.initializeClassTree();
		
		// scroll pane for the prm tables
		if (this.getPrmClasses() != null) {
			this.setTableScrollPane(new JScrollPane(this.getPrmClasses()));
			this.getTableScrollPane().getViewport().setPreferredSize(new Dimension(250, 600));
		} else {
			Debug.println(this.getClass(), "No prm classes was passed by JTree");
			this.setTableScrollPane(new JScrollPane());
		}
		
		// a panel containing the classes' JTree and a tool bar to edit them
		JPanel prmClassesPanel = new JPanel(new BorderLayout());
		
		// instantiate the tool bar to edit prm classes' tree
		this.setClassTreeEditionToolBar(new JToolBar("Add or remove PRM entities"));
		this.setClassTreeEditionPanel(new JPanel(new FlowLayout(FlowLayout.LEADING, 5,5)));
		
		// buttons for the toolbar
		this.setAddPRMClassButton(new JButton(this.getIconController().getNewClassIcon()));
		this.setRemovePRMClassButton(new JButton(this.getIconController().getDeleteClassIcon()));
		this.getAddPRMClassButton().setToolTipText("New PRM entity");
		this.getRemovePRMClassButton().setToolTipText("Remove PRM entity");
		
		// add buttons to edition panel
		this.getClassTreeEditionPanel().add(this.getAddPRMClassButton());
		this.getClassTreeEditionPanel().add(this.getRemovePRMClassButton());
		
		// add edition panel to toolbar
		this.getClassTreeEditionToolBar().add(this.getClassTreeEditionPanel());
		
		// add the classes' toolbar and JTree to the panel
		prmClassesPanel.add(this.getClassTreeEditionToolBar(), BorderLayout.NORTH);
		prmClassesPanel.add(this.getTableScrollPane(), BorderLayout.CENTER);
		
		// scroll pane for the actual panel to edit class
		this.setEditionScrollPane(new JScrollPane(new JPanel()));
		this.getEditionScrollPane().getViewport().setPreferredSize(new Dimension(400, 600));
		
		// adding the above panels to the top panel
		this.setSplitPane(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, prmClassesPanel, this.getEditionScrollPane()));
		this.getSplitPane().setSize(800,600);
		this.getSplitPane().setDividerSize(7);
		this.getSplitPane().resetToPreferredSizes();
		this.add(this.getSplitPane());
	}
	
	/**
	 * Resets the content of the ClassTree.
	 * Actually, it only recalls {@link #initComponents()}
	 */
	public void resetClassTree() {
		this.initializeClassTree();
	}
	
	/**
	 * Remove all components and call
	 * {@link #initComponents()} and then {@link #initListeners()}
	 */
	public void resetComponents() {
		this.removeAll();
		this.initComponents();
		this.initListeners();
	}
	
	/**
	 * Initialize only the {@link #getPrmClasses()}
	 * using {@link #getPrm()} as data
	 */
	protected void initializeClassTree() {
		// by default, the root node is the PRM itself
		if (this.getPrmClasses() == null) {
			this.setPrmClasses(new JTree(new DefaultMutableTreeNode(this.getPrm().getName())));
		} else {
			((DefaultMutableTreeNode)this.getPrmClasses().getModel().getRoot()).removeAllChildren();
		}
		if (this.getTableScrollPane() != null) {
			this.getTableScrollPane().setViewportView(this.getPrmClasses());
		}
		
		// since the root node is not an entity, do not edit it - set as not visible
//		this.getPrmClasses().setRootVisible(false);

		// obtain the root node
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.getPrmClasses().getModel().getRoot();
		
		// fill tree
		for (IPRMClass prmClass : this.getPrm().getIPRMClasses()) {
			root.add(new DefaultMutableTreeNode(prmClass.getName()));
		}
		
		this.getPrmClasses().updateUI();
		this.getPrmClasses().repaint();
		
		// Changing the icons...
		if (this.getPrmClasses().getCellRenderer() instanceof DefaultTreeCellRenderer) {
			((DefaultTreeCellRenderer)(this.getPrmClasses().getCellRenderer())).setLeafIcon(this.getIconController().getTableIcon());
			((DefaultTreeCellRenderer)(this.getPrmClasses().getCellRenderer())).setOpenIcon(this.getIconController().getCascadeIcon());
			((DefaultTreeCellRenderer)(this.getPrmClasses().getCellRenderer())).setClosedIcon(this.getIconController().getTableIcon());
			((DefaultTreeCellRenderer)(this.getPrmClasses().getCellRenderer())).setClosedIcon(this.getIconController().getTableIcon());
		}
	}

	/**
	 * The icon controller to obtain default icons
	 * @return
	 */
	public IconController getIconController() {
		return this.iconController;
	}

	/**
	 * Adds listeners to components, including the {@link #getPrmClasses()}
	 */
	protected void initListeners() {
		// what happens if we select a class/entity in a prm tree
		this.getPrmClasses().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (e.isAddedPath()) {
					if (e.getPath().getParentPath() == null) {
						// TODO panel to edit sequences, constraints, etc.
						Debug.println(this.getClass(), "Root object selected. Do not change");
						return;
					}
					String className = e.getPath().getLastPathComponent().toString();
					IPRMClass prmClass = getPrm().findPRMClassByName(className);
					if (prmClass != null) {
						getEditionScrollPane().setViewportView(ClassPanel.newInstance(SchemaPanel.this, prmClass));
						getEditionScrollPane().updateUI();
						getEditionScrollPane().repaint();
						updateUI();
						repaint();
					} else {
						getEditionScrollPane().setViewportView(new JLabel("Error reading class " + className));
						updateUI();
						repaint();
					}
				}
			}
		});
		
		// what happens if we push "add" button
		this.getAddPRMClassButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// create a new entity (class)
				getPrm().addPRMClass(getPrm().getPrmClassBuilder().buildPRMClass(getPrm()));
				// update tree of classes
				resetClassTree();
				updateUI();
				repaint();
			}
		});
		
		// what happens if we push "remove" button
		this.getRemovePRMClassButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// find the selected class
				if (getPrmClasses().getSelectionPath() != null) {
					getPrm().removePRMClass(
							getPrm().findPRMClassByName(
									getPrmClasses().getSelectionPath().getLastPathComponent().toString()
							)
					);
				}
				// update tree of classes
				resetClassTree();
				updateUI();
				repaint();
			}
		});
		
	}
	
	/**
	 * Forces update of the {@link #getPrmClasses()}
	 * by using {@link #getPrm()}
	 */
	public void updatePrmClasses() {
		
	}

	/**
	 * @return the prmTables
	 */
	public JTree getPrmClasses() {
		return prmTables;
	}

	/**
	 * @param prmClasses the prmTables to set
	 */
	public void setPrmClasses(JTree prmClasses) {
		this.prmTables = prmClasses;
	}

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
	 * @return the tableScrollPane
	 */
	public JScrollPane getTableScrollPane() {
		return tableScrollPane;
	}

	/**
	 * @param tableScrollPane the tableScrollPane to set
	 */
	public void setTableScrollPane(JScrollPane tableScrollPane) {
		this.tableScrollPane = tableScrollPane;
	}

	/**
	 * @return the editionScrollPane
	 */
	public JScrollPane getEditionScrollPane() {
		return editionScrollPane;
	}

	/**
	 * @param editionScrollPane the editionScrollPane to set
	 */
	public void setEditionScrollPane(JScrollPane editionScrollPane) {
		this.editionScrollPane = editionScrollPane;
	}

	/**
	 * @return the splitPane
	 */
	public JSplitPane getSplitPane() {
		return splitPane;
	}

	/**
	 * @param splitPane the splitPane to set
	 */
	public void setSplitPane(JSplitPane splitPane) {
		this.splitPane = splitPane;
	}

	/**
	 * @return the classTreeEditionToolBar
	 */
	public JToolBar getClassTreeEditionToolBar() {
		return classTreeEditionToolBar;
	}

	/**
	 * @param classTreeEditionToolBar the classTreeEditionToolBar to set
	 */
	public void setClassTreeEditionToolBar(JToolBar classTreeEditionToolBar) {
		this.classTreeEditionToolBar = classTreeEditionToolBar;
	}

	/**
	 * @return the addPRMClassButton
	 */
	public JButton getAddPRMClassButton() {
		return addPRMClassButton;
	}

	/**
	 * @param addPRMClassButton the addPRMClassButton to set
	 */
	public void setAddPRMClassButton(JButton addPRMClassButton) {
		this.addPRMClassButton = addPRMClassButton;
	}

	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}

	/**
	 * @return the removePRMClassButton
	 */
	public JButton getRemovePRMClassButton() {
		return removePRMClassButton;
	}

	/**
	 * @param removePRMClassButton the removePRMClassButton to set
	 */
	public void setRemovePRMClassButton(JButton removePRMClassButton) {
		this.removePRMClassButton = removePRMClassButton;
	}

	/**
	 * @return the classTreeEditionPanel
	 */
	public JPanel getClassTreeEditionPanel() {
		return classTreeEditionPanel;
	}

	/**
	 * @param classTreeEditionPanel the classTreeEditionPanel to set
	 */
	public void setClassTreeEditionPanel(JPanel classTreeEditionPanel) {
		this.classTreeEditionPanel = classTreeEditionPanel;
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
	 * @return the prmWindow
	 */
	public PRMWindow getPrmWindow() {
		return prmWindow;
	}

	/**
	 * @param prmWindow the prmWindow to set
	 */
	public void setPrmWindow(PRMWindow prmWindow) {
		this.prmWindow = prmWindow;
	}


}
