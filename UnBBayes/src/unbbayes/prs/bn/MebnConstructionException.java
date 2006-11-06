package unbbayes.prs.bn;

/**
 * Exception in the construction of the Mebn estructure. 
 * 
 *  @author Laecio Lima dos Santos (laecio@gmail.com)
 *  @version 1.0 (11/04/2006)
 */

public class MebnConstructionException extends MebnException{

	public MebnConstructionException(){
		super(); 
	}
	
	public MebnConstructionException(String mensage){
		super(mensage); 
	}	
	
}
