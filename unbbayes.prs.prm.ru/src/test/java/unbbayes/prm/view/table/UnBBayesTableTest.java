package unbbayes.prm.view.table;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;

public class UnBBayesTableTest extends JFrame {

	/**
	 * Serial version.
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UnBBayesTableTest frame = new UnBBayesTableTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UnBBayesTableTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// Probabilistic Node
		ProbabilisticNode newNode = new ProbabilisticNode();
		newNode.setName("K");
		newNode.setDescription("A test node");
		newNode.appendState("State 0");
		newNode.appendState("State 1");

		// CPT
		PotentialTable auxCPT = newNode.getProbabilityFunction();
		auxCPT.addVariable(newNode);

		// Graphic table
		PotentialTable potTab = newNode.getProbabilityFunction();
		potTab.addValueAt(0, 0.3f);
		potTab.addValueAt(1, 0.7f);
	}

}
