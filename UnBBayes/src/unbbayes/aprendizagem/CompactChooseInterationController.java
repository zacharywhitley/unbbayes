
package unbbayes.aprendizagem;

import javax.swing.JComboBox;
import unbbayes.util.NodeList;
public class CompactChooseInterationController {
	
	private CompactChooseWindow frame;
	
	public CompactChooseInterationController(CompactChooseWindow frame){
		this.frame = frame;		
	}
	
	public void actionOk(JComboBox variablesCombo, NodeList variablesVector){
		frame.dispose();
		String name = (String)variablesCombo.getSelectedItem();
            TVariavel aux;
            for (int i = 0 ; i < variablesVector.size() ;i++ ){
                aux = (TVariavel)variablesVector.get(i);
                if (aux.getName().equals(name)){
                    System.out.println("Sigla = " + aux.getName());
                    aux.isRep(true);
                    break;
                }
            }            
	}
	
	public void actionCancel(){
	    frame.dispose();	
	}	

}
