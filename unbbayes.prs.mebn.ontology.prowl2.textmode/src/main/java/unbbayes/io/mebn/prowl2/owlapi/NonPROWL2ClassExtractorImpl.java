/**
 * 
 */
package unbbayes.io.mebn.prowl2.owlapi;

import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link INonPROWLClassExtractor}
 * for PR-OWL2 ontologies
 * @author Shou Matsumoto
 *
 */
public class NonPROWL2ClassExtractorImpl implements INonPROWLClassExtractor {
	

	private String prowlOntologyNamespaceURI = IPROWL2ModelUser.PROWL2_NAMESPACEURI;
	private Collection<String> prowlOntologyNamespaceURIs = new HashSet<String>();
	{
		// by default, initialize it with 1 element
		prowlOntologyNamespaceURIs.add(IPROWL2ModelUser.PROWL2_NAMESPACEURI);
	}
	
	private Collection<OWLClassExpression> nonPROWLClassesCache = new HashSet<OWLClassExpression>();
	

	

	/**
	 * The default constructor is only visible for subclasses to allow inheritance.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected NonPROWL2ClassExtractorImpl() {
	}
	
	/**
	 * This is the default constructor method.
	 * @return
	 */
	public static INonPROWLClassExtractor getInstance() {
		return new NonPROWL2ClassExtractorImpl();
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

	/*
	 * (non-Javadoc)
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

	


	
	
	
	
	
	
	
	
	
	
	
	


	/**
	 * Obtains the descendants of a given class by visiting subclasses recursively.
	 * Note that this method only returns asserted descendants (so, if subclass is not set explicitly, it will not catch it)
	 * @param owlClass : class to extract descendants. This class itself is not included
	 * @param ontology : ontology where owlClass resides.
	 * @return : a non null set of extracted descendants
	 */
	protected Collection<? extends OWLClassExpression> getAssertedDescendants(OWLClass owlClass, OWLOntology ontology) {
		
		Collection<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		
		// initial assertion
		if (ontology == null || owlClass == null) {
			return ret;
		}
		
		for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSuperClass(owlClass)) {
			OWLClassExpression subclass = axiom.getSubClass();
			ret.add(subclass);
			try {
				ret.addAll(this.getAssertedDescendants(subclass.asOWLClass(), ontology));
			}catch (Exception e) {
				Debug.println(this.getClass(), "Failed to add descendants of " + subclass, e);
			}
		}
		return ret;
	}

	/**
	 * This is a cache for {@link #getNonPROWLClasses(OWLOntology)}.
	 * If this value is null or empty, then {@link #getNonPROWLClasses(OWLOntology)} will reload classes.
	 * @return the nonPROWLClassesCache
	 */
	public Collection<OWLClassExpression> getNonPROWLClassesCache() {
		return nonPROWLClassesCache;
	}

	/**
	 * This is a cache for {@link #getNonPROWLClasses(OWLOntology)}.
	 * If this value is null or empty, then {@link #getNonPROWLClasses(OWLOntology)} will reload classes.
	 * @param nonPROWLClassesCache the nonPROWLClassesCache to set
	 */
	public void setNonPROWLClassesCache(
			Collection<OWLClassExpression> nonPROWLClassesCache) {
		this.nonPROWLClassesCache = nonPROWLClassesCache;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#resetNonPROWLClassExtractor()
	 */
	public void resetNonPROWLClassExtractor() {
		this.setNonPROWLClassesCache(null);
	}


	/**
	 * @deprecated use {@link #getPROWLOntologyNamespaceURIs()} instead
	 * @see unbbayes.io.mebn.owlapi.DefaultNonPROWLClassExtractor#getProwlOntologyNamespaceURI()
	 */
	@Deprecated
	public String getProwlOntologyNamespaceURI() {
		Collection<String> uris = getPROWLOntologyNamespaceURIs();
		if (uris != null && !uris.isEmpty()) {
			// just return the 1st element
			return uris.iterator().next();
		}
		return prowlOntologyNamespaceURI;
	}

	/**
	 * @deprecated use {@link #setPROWLOntologyNamespaceURIs(Collection)} or add elements in {@link #getPROWLOntologyNamespaceURIs()} instead.
	 * @see unbbayes.io.mebn.owlapi.DefaultNonPROWLClassExtractor#setProwlOntologyNamespaceURI(java.lang.String)
	 */
	@Deprecated
	public void setProwlOntologyNamespaceURI(String prowlOntologyNamespaceURI) {
		Collection<String> uris = getPROWLOntologyNamespaceURIs();
		if (uris != null) {
			// just add new element
			uris.add(prowlOntologyNamespaceURI);
		}
		prowlOntologyNamespaceURI = prowlOntologyNamespaceURI;
	}

	
	/**
	 * @return the prowlOntologyNamespaceURIs : OWL classes with URIs starting with these URIs will be considered as part of PR-OWL scheme.
	 * @see #getProwlOntologyNamespaceURI()
	 * @see #getPROWLClasses(OWLOntology)
	 */
	public Collection<String> getPROWLOntologyNamespaceURIs() {
		return prowlOntologyNamespaceURIs;
	}

	/**
	 * @param prowlOntologyNamespaceURIs : OWL classes with URIs starting with these URIs will be considered as part of PR-OWL scheme.
	 * @see #getProwlOntologyNamespaceURI()
	 * @see #getPROWLClasses(OWLOntology)
	 */
	public void setPROWLOntologyNamespaceURIs(Collection<String> prowlOntologyNamespaceURIs) {
		this.prowlOntologyNamespaceURIs = prowlOntologyNamespaceURIs;
	}

}
