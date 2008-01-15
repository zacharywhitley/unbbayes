package unbbayes.prs.mebn.entity.exception;

public class EntityInstanceAlreadyExistsException extends Exception{

	public EntityInstanceAlreadyExistsException(){
		super(); 
	}
	
	public EntityInstanceAlreadyExistsException(String msg){
		super(msg); 
	}
}
