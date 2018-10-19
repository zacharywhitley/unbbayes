/**
 * 
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;


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

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.UbfIO2#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException,
			IOMebnException {
		boolean debugMode = Debug.isDebugMode();	// backup previous config
		Debug.setDebug(false);						// disable trace
		MultiEntityBayesianNetwork mebn = super.loadMebn(file);
		mebn.setStorageImplementor(null);
		Debug.setDebug(debugMode);					// restore
		return mebn;
	}
	
	

}
