/**
 * 
 */
package unbbbayes.prs.mebn.ssbn.extension.ssmsbn;

import unbbayes.prs.mebn.ssbn.SSBN;


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

}
