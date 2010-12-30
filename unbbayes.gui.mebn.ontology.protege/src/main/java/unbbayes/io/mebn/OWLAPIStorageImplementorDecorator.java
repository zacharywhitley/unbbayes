/**
 * 
 */
package unbbayes.io.mebn;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import unbbayes.util.IBridgeImplementor;

/**
 * Implements a "Implementor" of Bridge design pattern (and also a strategy design pattern)
 * for storing MEBN ontologies.
 * Also, is an wrapper to IBridgeImplementor for storing OWL2 ontologies using protege and OWL API.
 * This class provides a means to let objects of {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork}
 * to provide a reference to an object representing an OWL ontology.
 * @author Shou Matsumoto
 *
 */
public class OWLAPIStorageImplementorDecorator implements IBridgeImplementor {

	private OWLOntology adaptee;
	
	/**
	 * The default constructor is made protected in order to be visible from subclasses
	 * (this improves extensibility)
	 * @deprecated use {@link #newInstance(OWLOntology)} instead
	 */
	protected OWLAPIStorageImplementorDecorator() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Implements a "Implementor" of Bridge design pattern
	 * for storing MEBN ontologies.
	 * Also, is an wrapper to IBridgeImplementor for storing OWL2 ontologies using Protege and OWL API.
	 * @param adaptee : OWLModelManager containing the ontology to be stored
	 * @return a new instance
	 */
	public static OWLAPIStorageImplementorDecorator newInstance(OWLOntology adaptee) {
		OWLAPIStorageImplementorDecorator ret = new OWLAPIStorageImplementorDecorator();
		ret.setAdaptee(adaptee);
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.IBridgeImplementor#execute()
	 */
	public void execute() {
		try {
			this.getAdaptee().getOWLOntologyManager().saveOntology(this.getAdaptee());
		} catch (Exception e) {
			// we perform exception translation for interface compatibility (this method should not throw an exception)
			throw new RuntimeException("OWLAPI: OWLModelManager#save() failure in " + this.getAdaptee(), e);
		}
	}

	/**
	 * @return the adaptee
	 */
	public OWLOntology getAdaptee() {
		return adaptee;
	}

	/**
	 * @param adaptee the adaptee to set
	 */
	public void setAdaptee(OWLOntology adaptee) {
		this.adaptee = adaptee;
	}

}
