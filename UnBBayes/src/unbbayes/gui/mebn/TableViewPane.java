package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.DomainResidentNode;
;

/** 
 * Pane that show the probabilistic table of the selected resident node
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */ 

public class TableViewPane extends JPanel{

	private DomainResidentNode residentNode; 
	private JButton teste; 
	private JButton btnEditTable; 
	private JButton btnCompileTable; 
	private JToolBar jtbTable;
	
	private MEBNController mebnController; 
	
	TableViewPane(NetworkController _controller, DomainResidentNode _residentNode){
		
		super(); 
		this.setLayout(new BorderLayout()); 
		
		mebnController = _controller.getMebnController(); 
		
		residentNode = _residentNode; 
		
		teste = new JButton("TABLE");
		
		btnEditTable = new JButton("EDIT");
		btnEditTable.setBackground(ToolKitForGuiMebn.getBorderColor()); 
		btnEditTable.setForeground(Color.WHITE);
		btnCompileTable = new JButton("COMP"); 
		
		addListeners(); 
		
		jtbTable = new JToolBar();		
		jtbTable.setLayout(new GridLayout(1,2)); 
		jtbTable.add(btnEditTable); 
		jtbTable.add(btnCompileTable); 
		jtbTable.setFloatable(false); 
		
		
		this.add(jtbTable, BorderLayout.SOUTH);
		this.add(teste, BorderLayout.CENTER); 
	}
	
	private void addListeners(){
		
		btnEditTable.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
    			mebnController.setEnableTableEditionView(); 
        	}
		}); 
	}
	
}
