package unbbayes.prs.mebn.exception;

import java.util.ResourceBundle;

public class DuplicatedNameException extends Exception{

	private DuplicatedNameException(String msg){
		super(msg); 
	}
	
	public DuplicatedNameException(){
		super(); 
	}
	
}
