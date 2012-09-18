package unbbayes.prm.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.gui.NetworkWindow;
import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.controller.dao.PrmProcessState;
import unbbayes.prm.controller.prm.IPrmController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.util.PathFinderAlgorithm;
import unbbayes.prm.view.graphicator.IGraphicTableListener;
import unbbayes.prm.view.graphicator.PrmTable;
import unbbayes.prm.view.graphicator.RelationalGraphicator;
import unbbayes.prm.view.graphicator.editor.TableRenderer;
import unbbayes.prm.view.instances.IInstanceTableListener;
import unbbayes.prm.view.instances.InstancesTableViewer;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.bn.PotentialTable;

public class PRMProcessPanel extends JPanel implements IGraphicTableListener,
		IInstanceTableListener, ActionListener {
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

	/**
	 * Buttons
	 */
	JToggleButton buttonSelectorAtt;
	JToggleButton buttonProbModel;
	JToggleButton buttonCompile;
	JToggleButton buttomPartitioning;

	// Parent to Probabilistic model
	private Attribute parentPM;
	// Children for probabilistic model
	private Attribute childPM;
	private IPrmController prmController;
	private JDesktopPane unbbayesDesktop;

	private JSplitPane outerSplit;

	/**
	 * Create the panel.
	 * 
	 * @param dbController
	 * @param prmController
	 * @param unbbayesDesktop
	 */
	public PRMProcessPanel(IDBController dbController,
			IPrmController prmController, JDesktopPane unbbayesDesktop) {
		this.dbController = dbController;
		this.prmController = prmController;
		this.unbbayesDesktop = unbbayesDesktop;

		setLayout(new GridLayout(1, 1));

		JPanel panelGraph = new JPanel();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 30, 10, 10, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 20000.0,
				Double.MIN_VALUE };
		panelGraph.setLayout(gridBagLayout);

		JPanel panelProcess = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		panelGraph.add(panelProcess, gbc_panel);
		panelProcess.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		buttonProbModel = new JToggleButton("> Prob. Model");
		panelProcess.add(buttonProbModel);

		buttonSelectorAtt = new JToggleButton("> Selector attribute");
		buttonSelectorAtt.setEnabled(false);
		panelProcess.add(buttonSelectorAtt);

		buttomPartitioning = new JToggleButton("> Partitioning");
		buttomPartitioning.setEnabled(false);
		buttomPartitioning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Select a table and then choose an attribute.");
				rg.setPrmState(PrmProcessState.Partitioning);
			}
		});
		panelProcess.add(buttomPartitioning);

		buttonCompile = new JToggleButton("> Compile");
		buttonCompile.addActionListener(this);
		buttonCompile.setEnabled(false);
		panelProcess.add(buttonCompile);

		// Get relational schema
		relSchema = dbController.getRelSchema();
		rg = new RelationalGraphicator(relSchema, this);

		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.fill = GridBagConstraints.BOTH;
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 3;
		panelGraph.add(rg, gbc_chckbxNewCheckBox);

		// add(panelGraph);

		outerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelGraph, null);

		outerSplit.setOneTouchExpandable(true);

		outerSplit.setDividerSize(6);
		outerSplit.setBorder(null);

		add(outerSplit);
	}

	public void selectedTable(Table t) {
		log.debug("Selected table");
		InstancesTableViewer dt = new InstancesTableViewer(t,
				dbController.getTableValues(relSchema, t), this);
		outerSplit.setRightComponent(dt);
		outerSplit.setDividerLocation(400);
	}

	public void selectedAttributes(Attribute[] attributes) {
		// log.debug("Selected column" + collumns[0].getName());
		switch (rg.getPrmState()) {
		case Partitioning:
			partitioning(attributes);
			break;
		case SelectorAttribute:
			selectorAttribute(attributes);
			break;
		case ProbModel:
			break;
		case Compile:
			break;
		}

	}

	private void selectorAttribute(Attribute[] attribute) {
		selectorTable = attribute[0].getTable();
		selectorAttribute = attribute[0].getAttribute();

		int answer = JOptionPane.showConfirmDialog(
				this,
				"Do you agree with selector attribute = "
						+ selectorTable.getName() + "."
						+ selectorAttribute.getName());

		if (answer == 0) {
			buttonProbModel.setEnabled(true);
			buttonSelectorAtt.setSelected(true);

			// Show toolbar
			// rg.showPalette();
		}

	}

	/**
	 * Create N partitions based on the data of the specified columns.
	 * 
	 * @param t
	 * @param columns
	 * @deprecated
	 */
	private void partitioning(Attribute[] attributes) {
		log.debug("Partitioning");

		Column[] cols = new Column[attributes.length];
		for (int i = 0; i < cols.length; i++) {
			cols[i] = attributes[i].getAttribute();
		}

		String[] partitionNames = dbController.getPossibleValues(relSchema,
				attributes[0].getTable(), cols);

		String pvs = "";
		int i = 0;
		for (String val : partitionNames) {
			log.debug("Possible value=" + val);
			pvs += "\n" + ++i + ". " + val;
		}

		if (partitionNames.length > 0) {
			int answer = JOptionPane.showConfirmDialog(this,
					"The partitions are:" + pvs);
			log.debug("" + answer);
			if (answer == 0) {
				buttomPartitioning.setSelected(true);
				buttonSelectorAtt.setEnabled(true);

				// Change state
				rg.setPrmState(PrmProcessState.SelectorAttribute);

				// Message
				JOptionPane.showMessageDialog(this,
						"Choose a selector attribute.");
			}
		}
	}

	/**
	 * This method is called when an attribute is selected.
	 */
	@Override
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
			PathFinderAlgorithm paths = new PathFinderAlgorithm(parentPM, childPM);

			// FIXME validate ID or FK, because it works only for descriptive
			// attributes.

			prmController.addParent(newRel);
			rg.drawRelationShip(newRel);

			// Show CPT buttons.
			showCPTButtons(newRel);

			parentPM = null;
			childPM = null;

			// TODO unselect all the graphic attributes.
		} else {
			log.warn("Error, neither parent nor children are not null");
		}

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
	@Override
	public void selectedCPD(Attribute attribute) {
		log.debug("Show CPD for " + attribute.getAttribute().getName());

		// Get parents
		Attribute[] parents = prmController.parentsOf(attribute);

		// Ask user to introduce CPD.
		PotentialTable cpd = showCPDTableDialog(attribute);

		// Notify CPD to controller.
		prmController.setCPD(attribute, cpd);
	}

	private PotentialTable showCPDTableDialog(Attribute attribute) {
		Attribute[] parents = prmController.parentsOf(attribute);
		AttributeStates[] parentStates = new AttributeStates[parents.length];

		// Fill possible values for parents.
		for (int i = 0; i < parentStates.length; i++) {
			String[] possibleValues = dbController.getPossibleValues(relSchema,
					parents[i]);
			parentStates[i] = new AttributeStates(parents[i], possibleValues);
		}

		// Get possible values from DB.
		// FIXME what happens when there is no values??
		String[] childValues = dbController.getPossibleValues(relSchema,
				attribute);
		AttributeStates childStates = new AttributeStates(attribute,
				childValues);

		// Graphic table
		PrmTable table = new PrmTable(parentStates, childStates);

		// show
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.add(table);
		dialog.pack();
		dialog.setVisible(true);

		return table.getCPD();
	}

	/**
	 * Action performed for compile button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Create the graph. Look at PRMToBNCompiler.
		Graph compiledPRM = prmController.compile();

		// Create a new internal window
		// Display generated BN
		NetworkWindow netWindow = new NetworkWindow((Network) compiledPRM);

		unbbayesDesktop.add(netWindow);

		netWindow.setVisible(true);

	}

	@Override
	public void attributeSelected(Object[] data, int row, int column, Table t) {
		log.debug("An attribute without evidence is selected");

	}

}
