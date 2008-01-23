package unbbayes.io.mebn.exceptions;

public class IOMebnException extends Exception{
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	

	String object; /* what thing is the causing of the exception */
	
	public IOMebnException (String e){
		super(e); 
	}
	
	public IOMebnException (String e, String extra){
		super(e + ": " + extra); 
		this.object = extra; 
	}	
	
	/**
	 * Verity if the exception have an description extra
	 * @return true if exist description extra or false otherside. 
	 */
	public boolean hasDescriptionExtra(){
		if (object == null){
			return false; 
		}
		else{
			return true; 
		}
	}
	
	public String getDescriptionExtra(){
		return object;
	}
	
	public String setDescriptionExtra(String extra){
		return this.object = extra; 
	}

}