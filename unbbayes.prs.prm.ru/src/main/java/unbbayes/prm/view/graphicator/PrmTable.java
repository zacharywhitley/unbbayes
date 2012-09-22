package unbbayes.prm.view.graphicator;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;

public class PrmTable extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(PrmTable.class);

	private JTable table;

	private AttributeStates[] parentStates;

	private PotentialTable auxCPT;

	/**
	 * Create the panel.
	 */
	public PrmTable(AttributeStates[] parents, AttributeStates child) {
		this.parentStates = parents;
		// this.childStates = child;

		setLayout(new GridLayout(1, 1));

		table = new GUIPotentialTable(getPotentialTable(child)).makeTable();

		add(new JScrollPane(table));
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
}
