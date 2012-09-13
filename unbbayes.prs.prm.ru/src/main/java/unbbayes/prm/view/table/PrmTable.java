package unbbayes.prm.view.table;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.prm.model.AttributeStates;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;

public class PrmTable extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(PrmTable.class);

	private JTable table;

	private AttributeStates[] parentStates;

	private AttributeStates childStates;

	/**
	 * Create the panel.
	 */
	public PrmTable(AttributeStates[] parents, AttributeStates child) {
		this.parentStates = parents;
		this.childStates = child;

		setLayout(new GridLayout(1, 1));

		table = new GUIPotentialTable(getPotentialTable()).makeTable();

		add(new JScrollPane(table));
	}

	private PotentialTable getPotentialTable() {
		// One node for each parent.
		ProbabilisticNode[] node = new ProbabilisticNode[parentStates.length];

		// Fill each parent.
		for (int i = 0; i < node.length; i++) {
			node[i] = new ProbabilisticNode();

			// Node name
			node[i].setName(parentStates[i].getAttribute().getAttribute()
					.getName());

			String[] states = parentStates[i].getStates();

			// Add node states
			for (String state : states) {
				node[i].appendState(state);
			}
		}

		// Create a child
		ProbabilisticNode childNode = new ProbabilisticNode();
		childNode.setName(childStates.getAttribute().getAttribute().getName());
		String[] states = childStates.getStates();

		// Add node states
		for (String state : states) {
			childNode.appendState(state);
		}

		PotentialTable auxCPT = childNode.getProbabilityFunction();
		// Add edges
		for (int i = 0; i < node.length; i++) {
			auxCPT.addVariable(node[i]);
		}

		return childNode.getProbabilityFunction();
	}
}
