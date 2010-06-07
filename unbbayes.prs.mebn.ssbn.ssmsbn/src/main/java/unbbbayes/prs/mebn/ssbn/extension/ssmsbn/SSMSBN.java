/**
 * 
 */
package unbbbayes.prs.mebn.ssbn.extension.ssmsbn;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.util.Debug;


/**
 * Representation class for SSMSBN, which is both a MSBN and SSBN.
 * 
 * @author rafaelmezzomo
 * @author estevaoaguiar
 */
public class SSMSBN extends SSBN {

	/**
	 *  Using protect for the factory method
	 */
	protected SSMSBN() {
		super();
	}
	/**
	 * @return a new instance of SSMSBN
	 */
	public static SSMSBN newInstance(){
		SSMSBN ret = new SSMSBN();
		return ret;
	}
	/**
	 * try compile and inicialize SSMSBN.
	 * catch exception if it fails.
	 * 
	 * The exception will only be shown at debug perspective.
	 */
	public void compileAndInitializeSSBN() throws Exception{
		try {
			super.compileAndInitializeSSBN();
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failed to compile and initialize the generated SSMSBN.", e);
		}
	}
}
