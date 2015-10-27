/**
 * 
 */
package unbbayes.io.medg;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO;

/**
 * @author Shou Matsumoto
 *
 */
public class PROWL2DecisionIO extends OWLAPICompatiblePROWL2IO {

	/**
	 * Default constructor is not public, because we prefer constructor method {@link #getInstance()},
	 * but it is kept protected to allow easy extension.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected PROWL2DecisionIO() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method
	 * @return
	 */
	public static MebnIO getInstance() {
		// TODO Auto-generated method stub
		return new PROWL2DecisionIO();
	}

}
