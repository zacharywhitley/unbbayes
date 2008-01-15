package unbbayes.gui.mebn.auxiliary;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import unbbayes.controller.IconController;

public class ButtonLabel extends JButton{
	
	private int SIZE_LABEL = 15; 
	
	public ButtonLabel(String label, ImageIcon icon){
		super(); 
		int sizeLabel = label.length();
		if(sizeLabel < SIZE_LABEL){
			for(int i = sizeLabel; i < SIZE_LABEL; i++){
				label+=" "; 
			}
		}
		setIcon(icon); 
		setText(label); 
    	setBackground(new Color(193, 210, 205)); //Verde quase cinza
    	setForeground(Color.BLACK);
    }
	
}
