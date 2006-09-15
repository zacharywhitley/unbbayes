package unbbayes.gui.mebn;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.FindingMFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

public class MTheoryTabbedPane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384250925715415144L;

	public MTheoryTabbedPane() {
		super();
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("MTheory");
		mebn.addMFrag(new DomainMFrag("DomainMFrag 01", mebn));
		mebn.addMFrag(new DomainMFrag("DomainMFrag 02", mebn));
		mebn.addMFrag(new FindingMFrag("FindingMFrag 01", mebn));
		mebn.addMFrag(new DomainMFrag("DomainMFrag 03", mebn));
		mebn.addMFrag(new DomainMFrag("DomainMFrag 04", mebn));
		mebn.addMFrag(new FindingMFrag("FindingMFrag 02", mebn));
		mebn.addMFrag(new FindingMFrag("FindingMFrag 03", mebn));
		mebn.addMFrag(new FindingMFrag("FindingMFrag 04", mebn));
		
		this.addTab("MTheory Tree", new MTheoryTree(mebn));
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("MTheory Tabbed Pane");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MTheoryTabbedPane());
		frame.pack();
		frame.setVisible(true);
	}
}
