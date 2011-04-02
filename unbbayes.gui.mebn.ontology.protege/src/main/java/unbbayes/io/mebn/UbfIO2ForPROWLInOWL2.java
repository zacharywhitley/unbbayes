/**
 * 
 */
package unbbayes.io.mebn;

import unbbayes.io.mebn.protege.Protege41CompatiblePROWLIO;


/**
 * @author Shou Matsumoto
 *
 */
public class UbfIO2ForPROWLInOWL2 extends UbfIO2 {

	/**
	 * @deprecated
	 */
	public UbfIO2ForPROWLInOWL2() {
		super();
		try {
			this.setProwlIO(Protege41CompatiblePROWLIO.newInstance());		// load PR-OWL ontology in OWL2 using protege
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Construction method for {@link UbfIO2ForPROWLInOWL2}
	 * @return {@link UbfIO2ForPROWLInOWL2} instance
	 */
	public static UbfIO2 getInstance() {
		UbfIO2 ret = new UbfIO2ForPROWLInOWL2();
		return ret;
	}

}
