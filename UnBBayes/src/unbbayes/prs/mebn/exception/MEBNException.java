package unbbayes.prs.mebn.exception;

public class MEBNException extends Exception{
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 6287201364470449002L;

	public MEBNException(String e){
    	super(e); 
    }

	public MEBNException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
