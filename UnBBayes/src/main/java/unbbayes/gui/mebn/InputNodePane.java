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

/**
 * Pane for edition of a input node. 
 * Contains: 
 * -> A tree for selection of the property "is input of" 
 * -> A pane for selection of the arguments of the input node. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 05/28/07
 *
 */
public class InputNodePane extends JPanel{
	
	private GenerativeInputNode inputNode; 
	private InputInstanceOfTree inputInstanceOfTree; 
	private JScrollPane inputInstanceOfTreeScroll; 
	private JPanel argumentsPane; 
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
		jtbInputInstanceOf.setFloatable(false); 
		
		jtbInputInstanceOf.add(jlInputInstanceOf); 
		
		/* 
		 * No artigo original da Dr. Laskey, um n� de input pode ser instancia
		 * de um n� residente ou de uma built-in... Simplificado nesta vers�o
		 * apenas para residente.  
		 */
        //btnInputOfResident = new JButton("RES"); 
        //btnInputOfBuiltIn = new JButton("BUI"); 
		//jtbInputInstanceOf.add(btnInputOfResident); 
		//jtbInputInstanceOf.add(btnInputOfBuiltIn); 
		
		inputInstanceOfTree = new InputInstanceOfTree(controller); 
		//inputInstanceOfTree.setBackground(new Color(240,240,240)); //ligth gray
		//inputInstanceOfTree.setForeground(new Color(240,240,240)); 
		inputInstanceOfTreeScroll = new JScrollPane(inputInstanceOfTree);
		inputInstanceOfTreeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Resident List")); 
		
		if(_inputNode.getResidentNodePointer() != null){
		    argumentsPane = new ArgumentsTypedPane(inputNode,  _inputNode.getResidentNodePointer(), controller); 
		}
		else{
			argumentsPane = new JPanel(); 
		}
		
		setLayout(new BorderLayout());
		
		this.add(jtbInputInstanceOf, BorderLayout.NORTH); 
		this.add(argumentsPane, BorderLayout.SOUTH); 
		this.add(inputInstanceOfTreeScroll, BorderLayout.CENTER);
		
		this.setVisible(true);
		
	}
    
	public void updateArgumentPane(){
		this.remove(argumentsPane); 
		argumentsPane = new ArgumentsTypedPane(inputNode, inputNode.getResidentNodePointer(), controller);
		this.add(argumentsPane, BorderLayout.SOUTH); 
	}

}
