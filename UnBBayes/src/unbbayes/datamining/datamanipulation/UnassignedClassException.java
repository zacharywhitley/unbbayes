package unbbayes.datamining.datamanipulation;

/**
 * <code>UnassignedClassException</code> is used when
 * a method requires access to the Attribute designated as 
 * the class attribute in a set of Instances, but the Instances does not
 * have any class attribute assigned (such as by setClassIndex()).
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class UnassignedClassException extends RuntimeException 
{ 
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	/**
   * Creates a new <code>UnassignedClassException</code> instance
   * with no detail message.
   */
  public UnassignedClassException() 
  { super(); 
  }

  /**
   * Creates a new <code>UnassignedClassException</code> instance
   * with a specified message.
   *
   * @param message a <code>String</code> containing the message.
   */
  public UnassignedClassException(String message) 
  { super(message); 
  }
}