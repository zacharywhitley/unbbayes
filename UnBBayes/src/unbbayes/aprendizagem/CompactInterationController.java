
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;

public class CompactInterationController {
    
     
    private  CompactFileWindow frame;	
    
	public CompactInterationController(CompactFileWindow frame){
		this.frame = frame;
	}
	
	public void actionYes(NodeList variablesVector){
		frame.dispose();
		new CompactChooseWindow(variablesVector);				
	}
	
	public void actionNo(){
	    frame.dispose();			
	}

}
