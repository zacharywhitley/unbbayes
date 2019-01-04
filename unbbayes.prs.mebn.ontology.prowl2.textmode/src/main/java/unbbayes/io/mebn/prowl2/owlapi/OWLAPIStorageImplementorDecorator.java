/**
 * 
 */
package unbbayes.io.mebn.prowl2.owlapi;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

/**
 * This is the default implementation of {@link IOWLAPIStorageImplementorDecorator}
 * @author Shou Matsumoto
 *
 */
public class OWLAPIStorageImplementorDecorator implements IOWLAPIStorageImplementorDecorator {

	private OWLOntology adaptee;
	private OWLReasoner owlReasoner;
	private OWLReasonerConfiguration owlReasonerConfiguration;
	
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
		try {
			ret.setOWLReasonerConfiguration(new SimpleConfiguration());	// set default reasoner configuration
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.prowl2.IOWLAPIStorageImplementorDecorator#getAdaptee()
	 */
	public OWLOntology getAdaptee() {
		return adaptee;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.prowl2.IOWLAPIStorageImplementorDecorator#setAdaptee(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public void setAdaptee(OWLOntology adaptee) {
		if (this.adaptee == adaptee) {
			return;		// do nothing if nothing is changed
		}
		this.adaptee = adaptee;
		this.setOWLReasoner(this.generateReasoner(this.adaptee, this.getOWLReasonerConfiguration()));
	}

	/**
	 * Generates a new OWL Reasoner (Hermit) using the ontology and configuration.
	 * This method is called in {@link #setAdaptee(OWLOntology)} when the adaptee changes.
	 * @param ontology
	 * @param configuration
	 * @return a new instance of Hermit OWLReasoner
	 * 
	 */
	protected OWLReasoner generateReasoner(OWLOntology ontology, OWLReasonerConfiguration configuration) {
		try {
			if (ontology != null) {
				OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
				if (configuration != null) {
					return factory.createReasoner(ontology, configuration);
				} else {
					return factory.createReasoner(ontology);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.prowl2.IOWLAPIStorageImplementorDecorator#getOWLReasoner()
	 */
	public OWLReasoner getOWLReasoner() {
		return this.owlReasoner;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.prowl2.IOWLAPIStorageImplementorDecorator#setOWLReasoner(org.semanticweb.owlapi.reasoner.OWLReasoner)
	 */
	public void setOWLReasoner(OWLReasoner owlReasoner) {
		this.owlReasoner = owlReasoner;
	}

	/**
	 * @return the oWLReasonerConfiguration
	 */
	public OWLReasonerConfiguration getOWLReasonerConfiguration() {
		return owlReasonerConfiguration;
	}

	/**
	 * @param oWLReasonerConfiguration the oWLReasonerConfiguration to set
	 */
	public void setOWLReasonerConfiguration(OWLReasonerConfiguration owlReasonerConfiguration) {
		this.owlReasonerConfiguration = owlReasonerConfiguration;
	}

}
