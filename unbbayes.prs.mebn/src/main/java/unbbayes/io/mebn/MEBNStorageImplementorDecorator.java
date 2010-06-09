/**
 * 
 */
package unbbayes.io.mebn;

import unbbayes.util.IBridgeImplementor;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Implements a "Implementor" of Bridge design pattern
 * for storing MEBN ontologies.
 * Also, is an wrapper to IBridgeImplementor for storing Protege's OWL model
 * @author Shou Matsumoto
 *
 */
public class MEBNStorageImplementorDecorator implements IBridgeImplementor {

	OWLModel adaptee = null; // the adapted Protege's OWLModel
	
	
	
	/**
	 * Implements a "Implementor" of Bridge design pattern
	 * for storing MEBN ontologies.
	 * Also, is an wrapper to IBridgeImplementor for storing Protege's OWL model
	 * @param adaptee : OWLModel to be stored
	 */
	public MEBNStorageImplementorDecorator(OWLModel adaptee) {
		super();
		this.adaptee = adaptee;
	}

	/**
	 * Currently, it does nothing, but it might be useful for implementing
	 * "save current" feature.
	 */
	public void execute() {
		return;
	}

	/**
	 * @return the adaptee
	 */
	public OWLModel getAdaptee() {
		return adaptee;
	}

	/**
	 * @param adaptee the adaptee to set
	 */
	public void setAdaptee(OWLModel adaptee) {
		this.adaptee = adaptee;
	}
	
	

}
