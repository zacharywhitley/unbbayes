package unbbayes.io.mebn.owlapi;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

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
public interface IOWLAPIStorageImplementorDecorator extends IBridgeImplementor {

	/** 
	 * Obtain the class implementing storage (I/O) capabilities. In OWLAPI, it can be done
	 * by {@link org.semanticweb.owlapi.model.OWLOntologyManager} accessible from an {@link OWLOntology}
	 * @return the adaptee
	 */
	public abstract OWLOntology getAdaptee();

	/**
	 * The class implementing storage (I/O) capabilities. In OWLAPI, it can be done
	 * by {@link org.semanticweb.owlapi.model.OWLOntologyManager} accessible from an {@link OWLOntology}
	 * @param adaptee the adaptee to set
	 */
	public abstract void setAdaptee(OWLOntology adaptee);
	
	/**
	 * The reasoner responsible for performing inference in the {@link #getAdaptee()}.
	 * @return a reasoner
	 */
	public abstract OWLReasoner getOWLReasoner();
	
	/**
	 * The reasoner responsible for performing inference in the {@link #getAdaptee()}.
	 * @param owlReasoner
	 */
	public abstract void setOWLReasoner(OWLReasoner owlReasoner);

}