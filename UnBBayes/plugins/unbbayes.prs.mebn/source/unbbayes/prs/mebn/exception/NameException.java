package unbbayes.prs.mebn.exception;

public class NameException extends Exception{

	private String name; 
	
	protected NameException(String msg, String name){
		super(msg); 
		this.name = name; 
	}
	
	public NameException(){
		super(); 
	}
	
}
