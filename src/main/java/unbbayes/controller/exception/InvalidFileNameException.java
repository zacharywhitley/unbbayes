package unbbayes.controller.exception;

public class InvalidFileNameException extends Exception{

	public InvalidFileNameException(){
		super(); 
	}
	
	public InvalidFileNameException(String msg){
		super(msg); 
	}
}
