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
	private static Color colorContext = new Color(176, 252, 131); 
	private static Color colorResident = new Color(254, 250, 158);
	private static Color colorInput = new Color(220, 220, 220); 
	
	
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

	public static Color getColorContext() {
		return colorContext;
	}

	public static void setColorContext(Color colorContext) {
		ToolKitForGuiMebn.colorContext = colorContext;
	}

	public static Color getColorInput() {
		return colorInput;
	}

	public static void setColorInput(Color colorInput) {
		ToolKitForGuiMebn.colorInput = colorInput;
	}

	public static Color getColorResident() {
		return colorResident;
	}

	public static void setColorResident(Color colorResident) {
		ToolKitForGuiMebn.colorResident = colorResident;
	}

}
