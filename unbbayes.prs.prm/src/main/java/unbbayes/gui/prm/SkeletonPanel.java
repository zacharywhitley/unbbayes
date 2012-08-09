/**
 * 
 */
package unbbayes.gui.prm;

import javax.swing.JLabel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import unbbayes.controller.prm.IPRMController;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.util.Debug;

/**
 * A skeleton panel.
 * 
 * @author Shou Matsumoto
 * 
 */
public class SkeletonPanel extends SchemaPanel {

	private IPRMController controller;

	/**
	 * At least one constructor is visible for subclasses to allow inheritance
	 */
	protected SkeletonPanel() {
		super();
	}

	/**
	 * Default construction method with a double buffer and a flow layout.
	 * 
	 * @param layout
	 */
	public static SkeletonPanel newInstance(PRMWindow prmWindow,
			IPRMController controller, IPRM prm) {
		SkeletonPanel ret = new SkeletonPanel();
		// ret.setLayout(new BorderLayout(5,5));
		ret.setPrmWindow(prmWindow);
		ret.controller = controller;
		ret.setPrm(prm);
		ret.initComponents();
		ret.initListeners();

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.prm.SchemaPanel#initComponents()
	 */
	protected void initComponents() {
		super.initComponents();

		// hide toolbars for adding/removing class
		this.getClassTreeEditionToolBar().setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.gui.prm.SchemaPanel#initListeners()
	 */
	protected void initListeners() {
		// we are overwriting this method to allow opening object edition panel
		// instead of class edition panel when an
		// event happens

		// what happens if we select a class/entity in a prm tree
		this.getPrmClasses().addTreeSelectionListener(
				new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent e) {
						if (e.isAddedPath()) {
							if (e.getPath().getParentPath() == null) {
								// TODO panel to edit sequences, constraints,
								// etc.
								Debug.println(this.getClass(),
										"Root object selected. Do not change");
								return;
							}
							String className = e.getPath()
									.getLastPathComponent().toString();
							IPRMClass prmClass = getPrm().findPRMClassByName(
									className);
							if (prmClass != null) {
								getEditionScrollPane().setViewportView(
										ObjectPanel.newInstance(
												SkeletonPanel.this, prmClass));
								getEditionScrollPane().updateUI();
								getEditionScrollPane().repaint();
								updateUI();
								repaint();
							} else {
								getEditionScrollPane().setViewportView(
										new JLabel("Error reading class "
												+ className));
								updateUI();
								repaint();
							}
						}
					}
				});

	}

	/**
	 * @return the controller
	 */
	public IPRMController getController() {
		return controller;
	}

	/**
	 * @param controller
	 *            the controller to set
	 */
	public void setController(IPRMController controller) {
		this.controller = controller;
	}
	// /* (non-Javadoc)
	// * @see java.awt.Component#repaint()
	// */
	// public void repaint() {
	// try {
	// if (this.getPrm() != null && this.getPrmClasses() != null) {
	// try {
	// this.resetClassTree();
	// } catch (Exception e) {
	// Debug.println(this.getClass(), "Could not reset class tree", e);
	// }
	// }
	// super.repaint();
	// } catch (Exception e) {
	// Debug.println(this.getClass(), "Failed to repaint skeleton panel", e);
	// }
	// }
}
