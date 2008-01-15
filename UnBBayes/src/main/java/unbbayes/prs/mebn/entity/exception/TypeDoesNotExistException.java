package unbbayes.prs.mebn.entity.exception;

/**
 * This class is used when the type being removed does not exist or when the
 * type being set for an entity does not exist. Therefore, it can not be
 * removed.
 * 
 * @author Rommel Carvalho
 * 
 */
public class TypeDoesNotExistException extends TypeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8761706192288738829L;

	public TypeDoesNotExistException() {
		super();
	}

	public TypeDoesNotExistException(String msg) {
		super(msg);
	}

}
