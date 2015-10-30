/**
 * 
 */
package unbbayes.prs.medg;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.PROWL2MEBNFactory;

/**
 * @author Shou Matsumoto
 *
 */
public class PROWL2MEDGFactory extends PROWL2MEBNFactory implements IMEDGElementFactory {
	

	/** This class implements singleton pattern */
	private static class SingletonHolder {
		static final IMEDGElementFactory INSTANCE = new PROWL2MEDGFactory();
	}

	/**
	 * @deprecated use {@link PROWL2MEDGFactory#getInstance()}
	 */
	protected PROWL2MEDGFactory() {}

	
	/**
	 * Default constructor method.
	 * @return a singleton instance of this factory
	 */
	public static IMEDGElementFactory getInstance() {
//		return new PROWL2MEDGFactory();
		// uncomment the line above and comment the line below in order to stop using singleton pattern
		return SingletonHolder.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.medg.IMEDGElementFactory#createDecisionNode(java.lang.String, unbbayes.prs.mebn.MFrag)
	 */
	public MultiEntityDecisionNode createDecisionNode(String name, MFrag mFrag) {
		return new MultiEntityDecisionNode(name, mFrag);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.medg.IMEDGElementFactory#createUtilityNode(java.lang.String, unbbayes.prs.mebn.MFrag)
	 */
	public MultiEntityUtilityNode createUtilityNode(String name, MFrag mFrag) {
		return new MultiEntityUtilityNode(name, mFrag);
	}
}
