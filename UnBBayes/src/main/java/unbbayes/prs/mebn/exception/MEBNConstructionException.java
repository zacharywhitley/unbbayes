package unbbayes.prs.mebn.exception;

/**
 * Exception in the construction of the MEBN elements
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0
 */

public class MEBNConstructionException extends MEBNException{

	public MEBNConstructionException(String txt){
		super(txt); 
	}
	
	public MEBNConstructionException(){
		super(); 
	}
	
}
