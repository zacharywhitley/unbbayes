package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;

public class ArgumentsTypedPane extends JPanel{
	
	ResidentNodePointer pointer; 
	MEBNController mebnController; 
	Object node; 
	
	public ArgumentsTypedPane(Object _node, ResidentNodePointer _pointer, MEBNController _mebnController){
		super(); 
		pointer = _pointer; 
		mebnController = _mebnController; 
		node = _node; 
		
		if(pointer.getNumberArguments() > 5)
		setLayout(new GridLayout(pointer.getNumberArguments() + 1,1)); 
		else setLayout(new GridLayout(6 ,1 )); 
		
		JComboBox argument[] = new JComboBox[pointer.getNumberArguments()]; 
		
		JLabel residentNodeName = new JLabel("Node = " + pointer.getResidentNode().getName()); 
		residentNodeName.setHorizontalAlignment(JLabel.CENTER); 
		residentNodeName.setBackground(Color.YELLOW); 
		add(residentNodeName); 
		
		JToolBar tbArgX; 
		JButton btnArgXNumber; 
		JButton btnArgXType; 
		
		for(int i = 0; i < pointer.getNumberArguments(); i++){
			
			tbArgX = new JToolBar(); 
			
			ArrayList<OrdinaryVariable> ovList = (ArrayList<OrdinaryVariable>)mebnController.getCurrentMFrag().getOrdinaryVariableList(); 
			
			Vector<OrdinaryVariable> list = new Vector<OrdinaryVariable>(); 
			list.add(null); //elemento em branco... 
			for(OrdinaryVariable ov: ovList){
				if(ov.getValueType().compareTo(pointer.getTypeOfArgument(i)) == 0){
					list.add(ov); 
					if(pointer.getOrdinaryVariableList().size() == 0){
					   try{
					      pointer.addOrdinaryVariable(ov, i);
					   }
					   catch(Exception e){
						  e.printStackTrace(); 
					   }
					}
				}
			}
			
			argument[i] = new JComboBox(list); 
			argument[i].addItemListener(new ComboListener(i)); 
			
			btnArgXNumber = new JButton("" + i);
			btnArgXNumber.setBackground(Color.RED); 
			btnArgXType = new JButton(pointer.getTypeOfArgument(i)); 
			btnArgXType.setBackground(Color.LIGHT_GRAY); 
			
			tbArgX.add(btnArgXNumber); 
			tbArgX.add(btnArgXType); 
			tbArgX.add(argument[i]); 
			tbArgX.setFloatable(false); 
			
			add(tbArgX); 
		}
		
		
		
	}
	
	class ComboListener implements ItemListener{
		
		int indice;
		
		public ComboListener(int i){
			indice = i; 
		}
		
		public void itemStateChanged(ItemEvent e){
			
			JComboBox combo = (JComboBox)e.getSource(); 
			if(combo.getSelectedItem() != null){
				OrdinaryVariable ov = (OrdinaryVariable)combo.getSelectedItem(); 
				try{
					pointer.addOrdinaryVariable(ov, indice);
					mebnController.updateArgumentsOfObject(node); 
					System.out.println("[DEBUG]Indice: " + indice + " = " + ov.getName()); 
				}
				catch(OVDontIsOfTypeExpected ex){
					ex.printStackTrace(); 
				}
			}
		}
		
	}
	
}
