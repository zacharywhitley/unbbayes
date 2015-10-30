/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is the default implementation of {@link INonPROWLClassExtractor}
 * for PR-OWL2 ontologies
 * @author Shou Matsumoto
 *
 */
public class DefaultNonPROWL2ClassExtractor extends DefaultNonPROWLClassExtractor {
	

	/**
	 * The default constructor is only visible for subclasses to allow inheritance.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected DefaultNonPROWL2ClassExtractor() {
		// change the namespace of the scheme file to point to correct PR-OWL 2 URI
		HashSet<String> prowlOntologyNamespaceURIs = new HashSet<String>();
		prowlOntologyNamespaceURIs.add(IPROWL2ModelUser.PROWL2_NAMESPACEURI);
		setPROWLOntologyNamespaceURIs(prowlOntologyNamespaceURIs);
	}
	
	/**
	 * This is the default constructor method.
	 * @return
	 */
	public static INonPROWLClassExtractor getInstance() {
		return new DefaultNonPROWL2ClassExtractor();
	}

	/**
	 * Obtains all classes that does not contain {@link IPROWL2ModelUser#PROWL2_NAMESPACEURI} in its namespace.
	 * OWL:Thing is not included.
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#getNonPROWLClasses(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public Collection<OWLClassExpression> getNonPROWLClasses(OWLOntology ontology) {
		// use cache.
		if (this.getNonPROWLClassesCache() != null && !this.getNonPROWLClassesCache().isEmpty()) {
			return this.getNonPROWLClassesCache();
		}
		
		// the cache is clear. Reload classes 
		Collection<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		
		try {
			// TODO stop using the "find all and remove undesired" strategy
			
			// add all classes (note: looking for all subclasses of owl:Thing is not enough, because some top classes may not be explicitly asserted as subclass of owl:Thing)
			ret.addAll(ontology.getClassesInSignature(true));
			
			// remove owl:Thing
			ret.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
			
			// remove all classes that are part of PR-OWL 2 specification
			Collection<OWLClassExpression> classesToRemove = new HashSet<OWLClassExpression>();
			classesToRemove.add(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
			classesToRemove.addAll(getPROWLClasses(ontology));
			
			// remove all detected classes
			ret.removeAll(classesToRemove);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		// update cache
		this.setNonPROWLClassesCache(ret);

		// do not return ret itself, because it will allow direct access to cache
		return new HashSet<OWLClassExpression>(ret);
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.DefaultNonPROWLClassExtractor#getPROWLClasses(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public Collection<OWLClassExpression> getPROWLClasses(OWLOntology ontology) {
		Collection<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		for (OWLClassExpression classToRemove : ontology.getClassesInSignature(true)) {
			// check if the URI of classToRemove starts with at least one of the URIs specified in getPROWLOntologyNamespaceURIs
			Collection<String> prowlSchemeURIs = getPROWLOntologyNamespaceURIs();
			if (prowlSchemeURIs != null) {
				// check URIs one by one
				for (String prowlSchemeURI : prowlSchemeURIs) {
					try {
						// compare the prefix of the class' URI and PR-OWL2's URI to determine if this class is a PR-OWL2 class
						if (classToRemove.asOWLClass().getIRI().toString().startsWith(prowlSchemeURI)) {
							ret.add(classToRemove);
							break;	// if we found at least one, then we don't need to check other URIs
						}
					} catch (Throwable e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		return ret;
	}

	



}
