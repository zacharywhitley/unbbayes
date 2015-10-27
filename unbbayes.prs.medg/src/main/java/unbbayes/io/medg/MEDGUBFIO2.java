/**
 * 
 */
package unbbayes.io.medg;

import unbbayes.io.mebn.UbfIO2;

/**
 * @author Shou Matsumoto
 *
 */
public class MEDGUBFIO2 extends UbfIO2 {

	/**
	 * @deprecated
	 */
	public MEDGUBFIO2() {
		super();
		this.setProwlIO(PROWL2DecisionIO.getInstance());
		this.setName("PR-OWL 2 Decision");
	}

}
