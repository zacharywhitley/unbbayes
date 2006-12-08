package unbbayes.prs.mebn.entity.exception;

/**
 * This class is used when the type being added already exists. 
 * Therefore, it can not be added.
 * @author Rommel Carvalho
 *
 */
public class TypeAlreadyExistsException extends TypeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7839683593058739015L;

	public TypeAlreadyExistsException() {
		super();
	}
	
	public TypeAlreadyExistsException(String msg) {
		super(msg);
	}

}
