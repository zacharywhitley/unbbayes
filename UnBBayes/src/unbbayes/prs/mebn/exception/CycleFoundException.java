package unbbayes.prs.mebn.exception;

import java.util.ResourceBundle;

/**
 * A cycle found in the partial order of the Resident Nodes. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 (05/27/2007)
 */

/** Load resource file from this package */

public class CycleFoundException extends Exception{

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.resources.Resources");  		
	
	private CycleFoundException(String msg){
		super(msg); 
	}
	
	public CycleFoundException(){
		super(resource.getString("CycleFoundException")); 
	}
	
}
