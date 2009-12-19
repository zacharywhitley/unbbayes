package unbbayes.prs.mebn.ssbn.exception;

import java.util.ResourceBundle;

public class MFragContextFailException extends Exception{

	private static ResourceBundle resource = 
		ResourceBundle.getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	
	
	
	public MFragContextFailException(String msg){
		super(msg); 
	}
	
    public MFragContextFailException(){
		super(); 
	}
}
