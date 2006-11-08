package unbbayes.gui.mebn;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import unbbayes.controller.NetworkController;
import unbbayes.gui.NetworkWindow;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

public class MTheoryTabbedPane extends JTabbedPane {

	/**
	 * Tabbed Pane of MEBN that contains the tabs panes for each
	 * action of user. 
	 */
	
	private static final long serialVersionUID = 7384250925715415144L;

	public MTheoryTabbedPane(final NetworkController controller) {
		
		super();	
		this.addTab("MTheory Tree", new MTheoryTree(controller));
		
	}
	
//TODO novo metodo para testes 
	
	public static void main(String[] args) {
		
		MultiEntityBayesianNetwork mebn = null;  
		NetworkController controller; 
		NetworkWindow screen; 
		
		File file = new File("examples/mebn/Starship16.owl"); 

		PrOwlIO prOwlIo = new PrOwlIO(); 
		try{
		   mebn = prOwlIo.loadMebn(file);  
		}
		catch(Exception e){
			System.out.println("Exception Found!!!"); 
		}
		
		screen = new NetworkWindow(mebn); 
		controller = new NetworkController(mebn, screen); 
		
		JFrame frame = new JFrame("MTheory Tabbed Pane");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MTheoryTabbedPane(controller));
		frame.pack();
		frame.setVisible(true);
		
	}
	
}
