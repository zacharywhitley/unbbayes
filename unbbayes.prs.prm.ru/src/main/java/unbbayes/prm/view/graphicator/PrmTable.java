package unbbayes.prm.view.graphicator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.prm.model.AggregateFunctionName;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;

public class PrmTable extends JPanel {
	/**
	 * Default serial.
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(PrmTable.class);

	/**
	 * Combbox to choose the aggregate function.
	 */
//	private JComboBox comboAggregateFunctions;

	/**
	 * Graphic table to show the CPT.
	 */
	private JTable table;

	private AttributeStates[] parentStates;

	private PotentialTable auxCPT;

	/**
	 * Create the panel.
	 */
	public PrmTable(AttributeStates[] parents, AttributeStates child,
			final Window dialogParent) {
		this.parentStates = parents;

		// Layout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		setLayout(gridBagLayout);

		// Aggregate function
		// JPanel panel_1 = new JPanel();
		// GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		// gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		// gbc_panel_1.fill = GridBagConstraints.BOTH;
		// gbc_panel_1.gridx = 0;
		// gbc_panel_1.gridy = 0;
		// add(panel_1, gbc_panel_1);
		//
		// JLabel lblAggregateFunction = new JLabel("Aggregate function:");
		// panel_1.add(lblAggregateFunction);
		//
		// comboAggregateFunctions = new JComboBox();
		// comboAggregateFunctions.setModel(new DefaultComboBoxModel(
		// AggregateFunctionName.values()));
		// panel_1.add(comboAggregateFunctions);

		// CPT Table
		table = new GUIPotentialTable(getPotentialTable(child)).makeTable();
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.insets = new Insets(0, 0, 5, 0);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 1;
		add(new JScrollPane(table), gbc_table);

		// Accept button.
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);

		JButton btnNewButton = new JButton("Accept");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dialogParent.dispose();
			}
		});
		panel.add(btnNewButton);

		// Init CPT
		initianize();
	}

	private void initianize() {
		int columnCount = table.getColumnCount();
		for (int i = 1; i < columnCount; i++) {
			table.setValueAt(1, 0, i);
		}
	}

	/**
	 * Convert relational data to nodes in order to get a PotentialTable.
	 * 
	 * @return Potential table for attribute child.
	 */
	private PotentialTable getPotentialTable(AttributeStates childStates) {
		// Create a child
		ProbabilisticNode childNode = new ProbabilisticNode();
		childNode.setName(childStates.getAttribute().getAttribute().getName());
		String[] states1 = childStates.getStates();

		// Add node states
		for (String state : states1) {
			childNode.appendState(state);
		}

		// Conditional Probabilities Table.
		auxCPT = childNode.getProbabilityFunction();
		auxCPT.addVariable(childNode);

		// One node for each parent.
		ProbabilisticNode[] nodes = new ProbabilisticNode[parentStates.length];

		// Fill each parent.
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new ProbabilisticNode();

			// Node name
			nodes[i].setName(parentStates[i].getAttribute().getAttribute()
					.getName());

			String[] states = parentStates[i].getStates();

			// Add node states
			for (String state : states) {
				nodes[i].appendState(state);
			}

			// Add edges
			auxCPT.addVariable(nodes[i]);
		}

		return childNode.getProbabilityFunction();
	}

	public PotentialTable getCPD() {
		// int rows = childStates.getStates().length;
		// int columns = 0;
		//
		// for (AttributeStates p : parentStates) {
		// columns *= p*.
		// }
		// auxCPT.getValue();
		return auxCPT;
	}

//	public AggregateFunctionName getSelectedAggregateFunc() {
//		return AggregateFunctionName.valueOf(comboAggregateFunctions
//				.getSelectedItem().toString());
//	}
}
