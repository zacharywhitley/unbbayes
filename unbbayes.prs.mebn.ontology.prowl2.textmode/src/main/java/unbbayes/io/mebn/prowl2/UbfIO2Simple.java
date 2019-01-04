/**
 * 
 */
package unbbayes.io.mebn.prowl2;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.prowl2.owlapi.OWLAPICompatiblePROWL2IO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;


/**
 * @author Shou Matsumoto
 *
 */
public class UbfIO2Simple extends UbfIO2 implements MebnIO {

	/**
	 * @deprecated
	 */
	public UbfIO2Simple() {
		
	}
	
	public static UbfIO2Simple getInstance() {
		UbfIO2Simple ret = new UbfIO2Simple();
		try {
			ret.setProwlIO(OWLAPICompatiblePROWL2IO.newInstance());		// load PR-OWL ontology using protege
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			ret.setName(UbfIO2Simple.class.getSimpleName());	// the name must be different from the superclass
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.prowl2.UbfIO2#loadMebn(java.io.File)
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
