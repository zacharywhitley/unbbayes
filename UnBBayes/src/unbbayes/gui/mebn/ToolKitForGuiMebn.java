package unbbayes.gui.mebn;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;


/**
 * Classe auxiliar para métodos de 
 * @author Laecio
 *
 */
public class ToolKitForGuiMebn {

	private static Color borderColor = Color.BLUE; 
	
	public static TitledBorder getBorderForTabPanel(String title){
		
		TitledBorder titledBorder; 
		
		titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor), title); 
		titledBorder.setTitleColor(borderColor); 
		titledBorder.setTitleJustification(TitledBorder.CENTER); 
		
		return titledBorder;  
	}
	
	public static void setBorderColor(Color newColor){
		borderColor = newColor; 
	}
	
	public static Color getBorderColor(){
		return borderColor; 
	}

}
