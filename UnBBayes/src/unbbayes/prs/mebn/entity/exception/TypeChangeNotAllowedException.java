package unbbayes.prs.mebn.entity.exception;

/**
 * This class is used when the type can not be changed.
 * @author Rommel Carvalho
 *
 */
public class TypeChangeNotAllowedException extends TypeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -173074015948619170L;
	
	public TypeChangeNotAllowedException() {
		super();
	}
	
	public TypeChangeNotAllowedException(String msg) {
		super(msg);
	}

}
