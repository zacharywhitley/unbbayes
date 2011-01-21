/**
 * 
 */
package unbbayes.prs.mebn;


/**
 * This class instantiates subclasses of MEBN elements (i.e. {@link MultiEntityBayesianNetwork},
 * {@link MFrag}, {@link ResidentNode}, etc.) containing fields necessary to 
 * represent some PR-OWL2 elements.
 * @author Shou Matsumoto
 *
 */
public class PROWL2MEBNFactory extends DefaultMEBNElementFactory {
	
	/** This class implements singleton pattern */
	private static class SingletonHolder {
		static final PROWL2MEBNFactory INSTANCE = new PROWL2MEBNFactory();
	}
	
	/**
	 * The default constructor is visible just to allow inheritance.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected PROWL2MEBNFactory() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method.
	 * @return a singleton instance of this factory
	 */
	public static IMEBNElementFactory getInstance() {
//		return new PROWL2MEBNFactory();
		// uncomment the line above and comment the line below in order to stop using singleton pattern
		return SingletonHolder.INSTANCE;
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.mebn.IMEBNElementFactory#createResidentNode(java.lang.String, unbbayes.prs.mebn.MFrag)
//	 */
//	public ResidentNode createResidentNode(String name, MFrag mfrag) {
//		return OWLPropertyAwareResidentNode.getInstance(name, mfrag);
//	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.DefaultMEBNElementFactory#createMEBN(java.lang.String)
	 */
	public MultiEntityBayesianNetwork createMEBN(String name) {
		return IRIAwareMultiEntityBayesianNetwork.getInstance(name);
	}
	
	
}
