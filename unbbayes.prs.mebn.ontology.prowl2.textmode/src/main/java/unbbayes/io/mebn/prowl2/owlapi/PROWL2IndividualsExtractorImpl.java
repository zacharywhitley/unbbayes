/**
 * 
 */
package unbbayes.io.mebn.prowl2.owlapi;

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
public class PROWL2IndividualsExtractorImpl implements
		IPROWL2IndividualsExtractor {

//	private INonPROWLClassExtractor nonPROWL2ClassExtractor;

	/**
	 * The default constructor is not public in order to allow easy code modification,
	 * but it is not private, so that inheritance is possible.
	 * @deprecated
	 */
	protected PROWL2IndividualsExtractorImpl() {
//		this.setNonPROWL2ClassExtractor(nonPROWL2ClassExtractor);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Use this static method as the default constructor.
	 * @return instance of {@link PROWL2IndividualsExtractorImpl}
	 * @see #setNonPROWL2ClassExtractor(INonPROWLClassExtractor)
	 */
	public static IPROWL2IndividualsExtractor newInstance() {
		IPROWL2IndividualsExtractor ret = new PROWL2IndividualsExtractorImpl();
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
				if (isPROWLClass(owlClass)) {
					// extract their individuals
					for (OWLIndividual owlIndividual : owlClass.getIndividuals(ontology.getOWLOntologyManager().getOntologies())) {
						try {
							if (owlIndividual.isAnonymous()) {
								continue;
							}
							// do not add individuals declared in PR-OWL2 definition ontology
							if (!isIndividualInPROWL2Definition(owlIndividual)) {
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

	/**
	 * @param owlIndividual: individual to be checked
	 * @return : true if the individual is part of PR-OWL 2 definition
	 */
	public boolean isIndividualInPROWL2Definition(OWLIndividual owlIndividual) {
		return owlIndividual.asOWLNamedIndividual().getIRI().toString().startsWith(IPROWL2ModelUser.PROWL2_NAMESPACEURI);
	}

	/**
	 * @param owlClass : class to be checked
	 * @return : true if class is part of PR-OWL (1 or 2) definition
	 */
	public boolean isPROWLClass(OWLClass owlClass) {
		return owlClass.getIRI().toString().startsWith(IPROWL2ModelUser.PROWL2_NAMESPACEURI)
				|| owlClass.getIRI().toString().startsWith(IPROWL2ModelUser.OLD_PROWL_NAMESPACEURI);
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.IPROWL2IndividualsExtractor#resetPROWL2IndividualsExtractor()
	 */
	public void resetPROWL2IndividualsExtractor() {
		// TODO Auto-generated method stub

	}

//	/*
//	 * (non-Javadoc)
//	 * @see unbbayes.io.mebn.owlapi.IPROWL2IndividualsExtractor#getNonPROWL2Individuals(org.semanticweb.owlapi.model.OWLOntology, org.semanticweb.owlapi.reasoner.OWLReasoner)
//	 */
//	public Collection<OWLIndividual> getNonPROWL2Individuals( OWLOntology ontology, OWLReasoner reasoner) {
//		// this version does not use a reasoner
//		
//		// return value
//		Collection<OWLIndividual> ret = new HashSet<OWLIndividual>();
//		
//		// initial assertion
//		if (ontology == null) {
//			// no ontology provided. Check if we can extract from reasoner
//			if (reasoner == null || reasoner.getRootOntology() == null) {
//				// we could not extract ontology from reasoner
//				return ret;
//			} else {
//				// reasoner is not null and we can extract ontology from it.
//				ontology = reasoner.getRootOntology();
//			}
//		}
//		
//		// by now, ontology should be non-null
//		
//		// OBS. because of open-world assumption, it is impossible to query individuals that are not declared as PR-OWL2 individuals,
//		
//		// get all classes and check if they are PR-OWL 2 or PR-OWL 1 classes
//		for (OWLClass owlClass : ontology.getClassesInSignature(true)) {
//			try {
//				if (!isPROWLClass(owlClass)) {
//					if (owlClass.isOWLThing()) {
//						// TODO
//					} else if (owlClass.isOWLNothing()) {
//						Debug.println(getClass(), "owl:Nothing found: " + owlClass);
//						continue;	// ignore nothings
//					} else {
//						// extract their individuals
//						for (OWLIndividual owlIndividual : owlClass.getIndividuals(ontology.getOWLOntologyManager().getOntologies())) {
//							try {
//								if (owlIndividual.isAnonymous()) {
//									continue;
//								}
//								// do not add individuals declared in PR-OWL2 definition ontology
//								if (!isIndividualInPROWL2Definition(owlIndividual)) {
//									ret.add(owlIndividual);
//								}
//							} catch (Exception e) {
//								try {
//									Debug.println(this.getClass(), e.getMessage(), e);
//								} catch (Throwable t) {
//									t.printStackTrace();
//								}
//								continue;
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//				try {
//					Debug.println(this.getClass(), e.getMessage(), e);
//				} catch (Throwable t) {
//					t.printStackTrace();
//				}
//				continue;
//			}
//		}
//		
//		return ret;
//	}
//
//	/**
//	 * @return the nonPROWL2ClassExtractor: object to be used in order to obtain classes that are not in PR-OWL2 specification
//	 */
//	public INonPROWLClassExtractor getNonPROWL2ClassExtractor() {
//		return nonPROWL2ClassExtractor;
//	}
//
//	/**
//	 * @param nonPROWL2ClassExtractor : object to be used in order to obtain classes that are not in PR-OWL2 specification
//	 */
//	public void setNonPROWL2ClassExtractor(INonPROWLClassExtractor nonPROWL2ClassExtractor) {
//		this.nonPROWL2ClassExtractor = nonPROWL2ClassExtractor;
//	}

}
