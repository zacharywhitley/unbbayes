package unbbayes.prm.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.controller.dao.PrmProcessState;
import unbbayes.prm.controller.prm.IPrmController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.view.graphicator.IGraphicTableListener;
import unbbayes.prm.view.graphicator.RelationalGraphicator;
import unbbayes.prm.view.graphicator.editor.TableRenderer;

public class PRMProcessPanel extends JPanel implements IGraphicTableListener {
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
	private Attribute childrenPM;
	private IPrmController prmController;

	/**
	 * Create the panel.
	 * 
	 * @param dbController
	 * @param prmController
	 */
	public PRMProcessPanel(IDBController dbController,
			IPrmController prmController) {
		this.dbController = dbController;
		this.prmController = prmController;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 30, 10, 10, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 20000.0,
				Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);
		panel.setLayout(new GridLayout(0, 4, 0, 0));

		buttonProbModel = new JToggleButton("> Prob. Model");
		panel.add(buttonProbModel);

		buttonSelectorAtt = new JToggleButton("> Selector attribute");
		buttonSelectorAtt.setEnabled(false);
		panel.add(buttonSelectorAtt);

		buttomPartitioning = new JToggleButton("> Partitioning");
		buttomPartitioning.setEnabled(false);
		buttomPartitioning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Select a table and then choose an attribute.");
				rg.setPrmState(PrmProcessState.Partitioning);
			}
		});
		panel.add(buttomPartitioning);

		buttonCompile = new JToggleButton("> Compile");
		buttonCompile.setEnabled(false);
		panel.add(buttonCompile);

		// Get relational schema
		relSchema = dbController.getRelSchema();
		rg = new RelationalGraphicator(relSchema, this);

		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.fill = GridBagConstraints.BOTH;
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 3;
		add(rg, gbc_chckbxNewCheckBox);

	}

	public void selectedTable(Table t) {
		// TODO Auto-generated method stub

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

	@Override
	public void selectedAttribute(Attribute selectedAttribute) {
		log.debug("Selected attribute"
				+ selectedAttribute.getAttribute().getName());

		// FIXME validate ID or FK, because it works only for descriptive
		// attributes.

		// Select the parent
		if (parentPM == null) {
			parentPM = selectedAttribute;
		} else if (childrenPM == null) {
			childrenPM = selectedAttribute;

			// New parent relationship
			ParentRel newRel = new ParentRel(parentPM, childrenPM);
			prmController.addParent(newRel);
			rg.drawRelationShip(newRel);

			// Show CPT buttons.
			showCPTButtons(newRel);

			parentPM = null;
			childrenPM = null;

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

	@Override
	public void selectedCPD(Attribute attribute) {
		log.debug("Show CPD for " + attribute.getAttribute().getName());

		// Get parents
		Attribute[] parents = prmController.parentsOf(attribute);

		// TODO Ask user to introduce CPD.
		JOptionPane.showMessageDialog(this, "CPT table");
		double[][] cpd = null;

		// Notify CPD to controller.
		prmController.setCPD(attribute, cpd);
	}

}
