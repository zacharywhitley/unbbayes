
package unbbayes.aprendizagem;

import javax.swing.JPanel;
import javax.swing.JCheckBox;

public class ChooseInterationController {
	
	
	private ChooseVariablesWindow frame;	
	
	
	public ChooseInterationController(ChooseVariablesWindow frame){		
		this.frame = frame;		
	}
	
	/**
	 * This method a state on the variable, this state represents
	 * if the variable will participate of the network learning 
	 * process
	 **/
	public void setVariablesState(){
		frame.dispose();				
		JPanel choosePanel = frame.getChoosePanel();
		int length = choosePanel.getComponentCount();
		TVariavel variable;
		for(int i = 0 ;i < length; i++){
		    variable = frame.getVariable(i);
		    variable.setParticipa(((JCheckBox)choosePanel.getComponent(i)).isSelected());		    
		}				
	}

}
