package unbbayes.prs.mebn.ssbn.exception;

/**
 * Represent a implementation error in the SSBN algorithm. Some methods 
 * assume some pre-requisites. This exception should be used when one of 
 * this requisites don't was observed. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class ImplementationError extends RuntimeException{

	public ImplementationError(){
		
	}
	
	public ImplementationError(String msg){
		super(msg); 
	}
	
}
