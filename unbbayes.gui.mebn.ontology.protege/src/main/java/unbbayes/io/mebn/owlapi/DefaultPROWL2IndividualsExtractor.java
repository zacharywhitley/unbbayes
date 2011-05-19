/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link IPROWL2IndividualsExtractor}
 * @author Shou Matsumoto
 *
 */
public class DefaultPROWL2IndividualsExtractor implements
		IPROWL2IndividualsExtractor {

	/**
	 * The default constructor is not public in order to allow easy code modification,
	 * but it is not private, so that inheritance is possible.
	 * @deprecated
	 */
	protected DefaultPROWL2IndividualsExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Use this static method as the default constructor.
	 * @return instance of {@link DefaultPROWL2IndividualsExtractor}
	 */
	public static IPROWL2IndividualsExtractor newInstance() {
		IPROWL2IndividualsExtractor ret = new DefaultPROWL2IndividualsExtractor();
		return ret;
	}

	/** 
	 * This method returns PR-OWL2 individuals and PR-OWL1 individuals too. It performs
	 * string-based namespace comparisons in order to decide whether to include or exclude individuals.
	 * @see unbbayes.io.mebn.owlapi.IPROWL2IndividualsExtractor#getPROWL2Individuals(org.semanticweb.owlapi.model.OWLOntology, org.semanticweb.owlapi.reasoner.OWLReasoner)
	 */
	public Collection<OWLIndividual> getPROWL2Individuals(OWLOntology ontology, OWLReasoner reasoner) {
		// this version does not use a reasoner
		
		// return value
		Collection<OWLIndividual> ret = new HashSet<OWLIndividual>();
		
		// initial assertion
		if (ontology == null) {
			// no ontology provided. Check if we can extract from reasoner
			if (reasoner == null || reasoner.getRootOntology() == null) {
				// we could not extract ontology from reasoner
				return ret;
			} else {
				// reasoner is not null and we can extract ontology from it.
				ontology = reasoner.getRootOntology();
			}
		}
		
		// by now, ontology should be non-null
		
		// OBS. because of open-world assumption, it is impossible to query individuals that are not declared as PR-OWL2 individuals,
		
		// get all classes and check if they are PR-OWL 2 or PR-OWL 1 classes
		for (OWLClass owlClass : ontology.getClassesInSignature(true)) {
			try {
				if (owlClass.getIRI().toString().startsWith(IPROWL2ModelUser.PROWL2_NAMESPACEURI)
						|| owlClass.getIRI().toString().startsWith(IPROWL2ModelUser.OLD_PROWL_NAMESPACEURI)) {
					// extract their individuals
					for (OWLIndividual owlIndividual : owlClass.getIndividuals(ontology.getOWLOntologyManager().getOntologies())) {
						try {
							if (owlIndividual.isAnonymous()) {
								continue;
							}
							// do not add individuals declared in PR-OWL2 definition ontology
							if (!owlIndividual.asOWLNamedIndividual().getIRI().toString().startsWith(IPROWL2ModelUser.PROWL2_NAMESPACEURI)) {
								ret.add(owlIndividual);
							}
						} catch (Exception e) {
							try {
								Debug.println(this.getClass(), e.getMessage(), e);
							} catch (Throwable t) {
								t.printStackTrace();
							}
							continue;
						}
					}
				}
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), e.getMessage(), e);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.IPROWL2IndividualsExtractor#resetPROWL2IndividualsExtractor()
	 */
	public void resetPROWL2IndividualsExtractor() {
		// TODO Auto-generated method stub

	}

}
