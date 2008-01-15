package unbbayes.gui.mebn.auxiliary;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;


/**
 * Altera a cor do campo de texto quando o usuario está o 
 * editando. 
 */
public class FocusListenerTextField implements FocusListener{
	
	public void focusGained(FocusEvent e){
		JTextField field = (JTextField)e.getSource(); 
		field.setBorder(BorderFactory.createEtchedBorder()); 
		field.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected()); //verde 
	}
	
	public void focusLost(FocusEvent e){
		JTextField field = (JTextField)e.getSource(); 
		field.setBorder(null); 
		field.setBackground(ToolKitForGuiMebn.getColorTextFieldUnselected()); 
	}
}