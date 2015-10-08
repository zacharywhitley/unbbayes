/**
 * 
 */
package unbbayes.io.medg;

import unbbayes.io.mebn.UbfIO;

/**
 * @author Shou Matsumoto
 *
 */
public class MEDGUBFIO extends UbfIO {

	/**
	 * @deprecated
	 */
	public MEDGUBFIO() {
		super();
		this.setProwlIO(PROWLDecisionIO.getInstance());
	}
	
	/** This method was added in order to make sure {@link MEDGUBFIO#getInstance()} returns this class instead of {@link UbfIO} */
	public static UbfIO getInstance() {
		return new MEDGUBFIO();
	}

}
