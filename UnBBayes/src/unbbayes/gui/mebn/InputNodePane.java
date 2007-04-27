package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.GenerativeInputNode;

public class InputNodePane extends JPanel{
	
	private GenerativeInputNode inputNode; 
	private InputInstanceOfTree inputInstanceOfTree; 
	private JScrollPane inputInstanceOfTreeScroll; 
	private MEBNController controller; 
	
	public InputNodePane(){
		
	}
	
	public InputNodePane(MEBNController _controller, GenerativeInputNode _inputNode){
		
		JToolBar jtbInputInstanceOf; 
		JLabel jlInputInstanceOf; 
		JButton btnInputOfResident; 
		JButton btnInputOfBuiltIn; 
		controller = _controller; 
		
		inputNode = _inputNode; 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel("Input Node")); 
		
		jtbInputInstanceOf = new JToolBar(); 
		
		jlInputInstanceOf = new JLabel("Input of: ");
		btnInputOfResident = new JButton("RES"); 
		btnInputOfBuiltIn = new JButton("BUI"); 
		jtbInputInstanceOf.setFloatable(false); 
		
		jtbInputInstanceOf.add(jlInputInstanceOf); 
		jtbInputInstanceOf.add(btnInputOfResident); 
		jtbInputInstanceOf.add(btnInputOfBuiltIn); 
		
		inputInstanceOfTree = new InputInstanceOfTree(controller); 
		//inputInstanceOfTree.setBackground(new Color(240,240,240)); //ligth gray
		//inputInstanceOfTree.setForeground(new Color(240,240,240)); 
		inputInstanceOfTreeScroll = new JScrollPane(inputInstanceOfTree);
		inputInstanceOfTreeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Resident List")); 
		
		setLayout(new BorderLayout());
		this.add(jtbInputInstanceOf, BorderLayout.NORTH); 
		this.add(inputInstanceOfTreeScroll, BorderLayout.CENTER);
		this.setVisible(true);
		
	}
    

}
