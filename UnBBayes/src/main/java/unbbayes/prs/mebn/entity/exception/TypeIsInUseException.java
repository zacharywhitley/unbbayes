package unbbayes.prs.mebn.entity.exception;

/**
 * Some object is using the type and for this the 
 * operation can't be sucessfull. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (05/18/2007)
 */

public class TypeIsInUseException extends Exception{

	public TypeIsInUseException(){
		super(); 	
	}
	
	public TypeIsInUseException(String msg){
		super(msg); 
	}
	
}
