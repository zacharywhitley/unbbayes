package unbbayes.prs.mebn.entity.exception;

/**
 * This class is used when the type being added already exists. 
 * Therefore, it can not be added.
 * @author Rommel Carvalho
 *
 */
public class TypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5036670577194690304L;

	public TypeException() {
		super();
	}
	
	public TypeException(String msg) {
		super(msg);
	}

}
