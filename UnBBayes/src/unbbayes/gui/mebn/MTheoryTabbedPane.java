package unbbayes.gui.mebn;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MTheoryTabbedPane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384250925715415144L;

	public MTheoryTabbedPane() {
		super();
		this.addTab("MTheory Tree", new MTheoryTree());
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("MTheory Tabbed Pane");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MTheoryTabbedPane());
		frame.pack();
		frame.setVisible(true);
	}
}
