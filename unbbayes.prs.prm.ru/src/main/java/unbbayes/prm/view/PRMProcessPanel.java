package unbbayes.prm.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.gui.NetworkWindow;
import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.controller.prm.IPrmController;
import unbbayes.prm.controller.prm.PrmCompiler;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.util.PathFinderAlgorithm;
import unbbayes.prm.view.dialogs.ParentPathDialog;
import unbbayes.prm.view.graphicator.IGraphicTableListener;
import unbbayes.prm.view.graphicator.PrmTable;
import unbbayes.prm.view.graphicator.RelationalGraphicator;
import unbbayes.prm.view.graphicator.editor.TableRenderer;
import unbbayes.prm.view.instances.IInstanceTableListener;
import unbbayes.prm.view.instances.InstancesTableViewer;
import unbbayes.prs.Network;
import unbbayes.prs.bn.PotentialTable;

/**
 * Main panel.
 * 
 * @author David Saldaña.
 * 
 */
public class PRMProcessPanel extends JPanel implements IGraphicTableListener,
		IInstanceTableListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(PRMProcessPanel.class);

	RelationalGraphicator rg;
	IDBController dbController;
	private Database relSchema;

	/**
	 * Table of Selector attribute.
	 */
	Table selectorTable;
	/**
	 * Selector attribute.
	 */
	Column selectorAttribute;

	// Parent to Probabilistic model
	private Attribute parentPM;
	// Children for probabilistic model
	private Attribute childPM;
	private IPrmController prmController;
	private MainInternalFrame unbbayesDesktop;

	private JSplitPane outerSplit;
	private JToolBar toolBar;
	private JButton btnLoadPM;
	private JButton btnSavePM;

	/**
	 * Create the panel.
	 * 
	 * @param dbController
	 * @param prmController
	 * @param unbbayesDesktop
	 */
	public PRMProcessPanel(IDBController dbController,
			IPrmController prmController, MainInternalFrame unbbayesDesktop) {
		this.dbController = dbController;
		this.prmController = prmController;
		this.unbbayesDesktop = unbbayesDesktop;

		setLayout(new GridLayout(1, 1));

		JPanel panelGraph = new JPanel();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 10, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 20000.0,
				Double.MIN_VALUE };
		panelGraph.setLayout(gridBagLayout);

		// Get relational schema
		relSchema = dbController.getRelSchema();

		toolBar = new JToolBar();
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.anchor = GridBagConstraints.WEST;
		gbc_toolBar.fill = GridBagConstraints.VERTICAL;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		panelGraph.add(toolBar, gbc_toolBar);

		btnLoadPM = new JButton("");
		btnLoadPM.setEnabled(false);
		btnLoadPM.setToolTipText("Load Probabilistic Model");
		btnLoadPM
				.setIcon(new ImageIcon(
						PRMProcessPanel.class
								.getResource("/com/sun/java/swing/plaf/windows/icons/UpFolder.gif")));
		btnLoadPM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String wd = System.getProperty("user.dir");
				JFileChooser fc = new JFileChooser(wd);
				int rc = fc.showDialog(null, "Select Data File");

				// File selected.
				if (rc == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					loadProbabilisticModel(file);
					// call your function here
				}
			}
		});
		toolBar.add(btnLoadPM);

		btnSavePM = new JButton("");
		btnSavePM.setEnabled(false);
		btnSavePM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String wd = System.getProperty("user.dir");
				JFileChooser fc = new JFileChooser(wd);
				int rc = fc.showSaveDialog(PRMProcessPanel.this);

				// File selected.
				if (rc == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					saveProbabilisticModel(file);
					// call your function here
				}
			}

		});
		btnSavePM.setToolTipText("Save probabilistic model");
		btnSavePM
				.setIcon(new ImageIcon(
						PRMProcessPanel.class
								.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		toolBar.add(btnSavePM);
		rg = new RelationalGraphicator(relSchema, this);

		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.fill = GridBagConstraints.BOTH;
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 1;
		panelGraph.add(rg, gbc_chckbxNewCheckBox);

		// add(panelGraph);

		outerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelGraph, null);

		outerSplit.setOneTouchExpandable(true);

		outerSplit.setDividerSize(6);
		outerSplit.setBorder(null);

		add(outerSplit);
	}

	private void saveProbabilisticModel(File file) {

		// // Save CPDs.
		// HashMap<String, FloatCollection[]> cpds = prmController.getCpds();
		//
		// // Save Parents
		// List<ParentRel> parents = prmController.getParents();
		//
		// try {
		// // use buffering
		// OutputStream fos = new FileOutputStream(file);
		// OutputStream buffer = new BufferedOutputStream(fos);
		// ObjectOutput output = new ObjectOutputStream(buffer);
		// try {
		// output.writeObject(cpds);
		// output.writeObject(parents);
		// } finally {
		// output.close();
		// }
		// } catch (IOException ex) {
		// log.error("Cannot perform output.", ex);
		// }
	}

	private void loadProbabilisticModel(File file) {
		// try {
		// // use buffering
		// InputStream is = new FileInputStream(file);
		// InputStream buffer = new BufferedInputStream(is);
		// ObjectInput input = new ObjectInputStream(buffer);
		// try {
		// HashMap<String, PotentialTable[]> cpds = (HashMap<String,
		// PotentialTable[]>) input
		// .readObject();
		//
		// } finally {
		// input.close();
		// }
		// } catch (ClassNotFoundException ex) {
		// log.error(ex);
		// } catch (IOException ex) {
		// log.error(ex);
		// }
	}

	public void selectedTable(Table t) {
		log.debug("Selected table");
		InstancesTableViewer dt = new InstancesTableViewer(t,
				dbController.getTableValues(t), this);
		outerSplit.setRightComponent(dt);
		outerSplit.setDividerLocation(this.getHeight() * 2 / 3);
	}

	public void selectedAttributes(Attribute[] attributes) {

	}

	/**
	 * This method is called when an attribute is selected.
	 */
	public void selectedAttribute(Attribute selectedAttribute) {
		log.debug("Selected attribute"
				+ selectedAttribute.getAttribute().getName());

		// Select the parent
		if (parentPM == null) {
			parentPM = selectedAttribute;
		} else if (childPM == null) {
			childPM = selectedAttribute;

			// New parent relationship
			ParentRel newRel = new ParentRel(parentPM, childPM);

			PathFinderAlgorithm paths = new PathFinderAlgorithm();
			// Get possible paths
			List<Attribute[]> possiblePaths = paths.getPossiblePaths(relSchema,
					 childPM,parentPM);

			// Only one path is assigned automatically. If more than one exist
			// then ask to user to choose.
			// If there is no possible path, then the relationship is not valid.
			if (possiblePaths.size() == 0) {
				JOptionPane.showInternalMessageDialog(this,
						"There is no possible path between attributes",
						"No path", JOptionPane.WARNING_MESSAGE);

				// Clear the selected attributes and exit.
				parentPM = null;
				childPM = null;

				return;
			} else {
				ParentPathDialog parentPathDialog = new ParentPathDialog(
						possiblePaths);
				parentPathDialog.setModal(true);
				parentPathDialog.setVisible(true);

				// Cancel button
				if (parentPathDialog.isCancelled()) {
					parentPM = null;
					childPM = null;
					return;
				}

				newRel.setPath(parentPathDialog.getSelectedPath());
				newRel.setAggregateFunction(parentPathDialog
						.getSelectedAggregateFunction());
			}

			// FIXME validate ID or FK, because it works only for descriptive
			// attributes.

			addNewRelationShip(newRel);

			parentPM = null;
			childPM = null;

		} else {
			log.warn("Error, neither parent nor children are not null");
		}

	}

	private void addNewRelationShip(ParentRel newRel) {
		prmController.addParent(newRel);
		rg.drawRelationShip(newRel);

		// Show CPT buttons.
		showCPTButtons(newRel);
	}

	private void showCPTButtons(ParentRel newRel) {
		// Show to parent
		TableRenderer parentTable = rg.getGraphicTable(newRel.getParent()
				.getTable().getName());
		parentTable.enableCPDFor(newRel.getParent().getAttribute());

		// Show to child
		TableRenderer childTable = rg.getGraphicTable(newRel.getChild()
				.getTable().getName());
		childTable.enableCPDFor(newRel.getChild().getAttribute());

	}

	/**
	 * This method is called when the user selects a CPD table button.
	 */
	public void selectedCPD(Attribute attribute) {
		log.debug("Show CPD for " + attribute.getAttribute().getName());

		// Ask user to introduce CPD.
		try {
			showCPDTableDialog(attribute);
		} catch (Exception e) {
			log.warn(e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
					JOptionPane.WARNING_MESSAGE);

		}
	}

	private void showCPDTableDialog(final Attribute attribute) throws Exception {
		// Get parents
		ParentRel[] parentRels = prmController.parentsOf(attribute);
		AttributeStates[] parentStates = new AttributeStates[parentRels.length];

		List<PotentialTable> potentialTables = new ArrayList<PotentialTable>();

		// Fill possible values for parents.
		for (int i = 0; i < parentRels.length; i++) {
			Attribute parent2 = parentRels[i].getParent();
			String[] possibleValues = dbController
					.getPossibleValues(parentRels[i].getParent());

			// There are not states.
			if (possibleValues.length == 0) {
				throw new Exception("The parent attribute " + parent2
						+ " does not have possible states.");
			}

			parentStates[i] = new AttributeStates(parent2, possibleValues);
			parentStates[i].setAssociatedIdRel(parentRels[i].getIdRelationsShip());

			// When it is the same parent.
			if (parent2.equals(attribute)) {

				String[] childValues = dbController
						.getPossibleValues(attribute);
				AttributeStates childStates = new AttributeStates(attribute,
						childValues);

				// Table title
				String title = parentRels[i].getPath()[1].toString();

				// Show
				PrmTable table = showPrmTable(title, new AttributeStates[] {},
						childStates);
				potentialTables.add(table.getCPD());
			}
		}

		// Get possible values from DB.
		String[] childValues = dbController.getPossibleValues(attribute);
		AttributeStates childStates = new AttributeStates(attribute,
				childValues);

		// show CPT table
		try {
			PrmTable table = showPrmTable(attribute.toString(), parentStates,
					childStates);

			potentialTables.add(table.getCPD());
			prmController.setCPD(attribute,
					potentialTables.toArray(new PotentialTable[0]));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
			log.error(e);
			e.printStackTrace();
		}
	}

	private PrmTable showPrmTable(String title, AttributeStates[] parentStates,
			AttributeStates childStates) {
		JDialog dialog = new JDialog();
		dialog.setTitle(title);

		// Graphic table
		PrmTable table = new PrmTable(parentStates, childStates, dialog);

		try {
			if (parentStates.length == 0) {
				// Get probability of each state
				double[] stateProbability = dbController
						.getStateProbability(childStates);
				table.setProbabilities(stateProbability);
			}
		} catch (Exception e) {
			log.warn(e);
			e.printStackTrace();
		}

		dialog.setModal(true);
		dialog.getContentPane().add(table);
		dialog.pack();
		dialog.setVisible(true);

		return table;
	}

	/**
	 * Select an attribute without evidence to create a BN.
	 */
	public void attributeSelected(Table table, Column uniqueIndexColumn,
			Object indexValue, Column column, Object value) {
		log.debug("An attribute without evidence is selected");

		try {
			// Create a compiler.
			PrmCompiler compiler = new PrmCompiler(prmController, dbController);

			// Compile
			Network bn = (Network) compiler.compile(table, uniqueIndexColumn,
					indexValue, column, value);

			// Show the SSBN.
			NetworkWindow netWindow = new NetworkWindow(bn);
			netWindow.setVisible(true);

			// Show the result
			unbbayesDesktop.delegateToGraphRenderer(bn,
					compiler.getInferenceAlgorithm());

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
			log.error(e);
			e.printStackTrace();
		}
	}

}
