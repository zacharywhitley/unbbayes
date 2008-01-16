package unbbayes.prs.mebn.exception;


public class DuplicatedNameException extends Exception{

	private DuplicatedNameException(String msg){
		super(msg); 
	}
	
	public DuplicatedNameException(){
		super(); 
	}
	
}
