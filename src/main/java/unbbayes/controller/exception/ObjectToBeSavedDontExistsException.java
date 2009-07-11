package unbbayes.controller.exception;

public class ObjectToBeSavedDontExistsException extends Exception{

	public ObjectToBeSavedDontExistsException(){
		super(); 
	}
	
	public ObjectToBeSavedDontExistsException(String msg){
		super(msg); 
	}
	
}
