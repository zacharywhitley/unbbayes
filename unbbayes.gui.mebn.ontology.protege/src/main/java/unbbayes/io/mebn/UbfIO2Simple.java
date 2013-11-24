/**
 * 
 */
package unbbayes.io.mebn;

import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO;


/**
 * @author Shou Matsumoto
 *
 */
public class UbfIO2Simple extends UbfIO2 {

	/**
	 * @deprecated
	 */
	public UbfIO2Simple() {
		// TODO change the following code\
		try {
			this.setProwlIO(OWLAPICompatiblePROWL2IO.newInstance());		// load PR-OWL ontology using protege
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			this.setName(UbfIO2Simple.class.getSimpleName());	// the name must be different from the superclass
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
