package unbbayes.io.mebn.owlapi;

import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link INonPROWLClassExtractor}
 * @author Shou Matsumoto
 *
 */
public class DefaultNonPROWLClassExtractor implements INonPROWLClassExtractor {
	
	private String prowlOntologyNamespaceURI = "http://www.pr-owl.org/pr-owl.owl";
	
	private Collection<OWLClassExpression> nonPROWLClassesCache = new HashSet<OWLClassExpression>();
	
	/**
	 * The default constructor is only visible for subclasses to allow inheritance.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected DefaultNonPROWLClassExtractor() {
		
	}
	
	/**
	 * This is the default constructor method.
	 * @return
	 */
	public static INonPROWLClassExtractor getInstance() {
		return new DefaultNonPROWLClassExtractor();
	}
	
	/**
	 * This method is a facilitator that returns non-PR-OWL classes from an ontology.
	 * This is useful to obtain classes that do not belong to probabilistic ontology.
	 * @param ontology
	 * @return
	 * @see #getNonPROWLClassesCache() : this is a cache of this method (it contains the last returned value). If
	 * this value is set to non-null, this method will just return {@link #getNonPROWLClassesCache()}
	 */
	public Collection<OWLClassExpression> getNonPROWLClasses(OWLOntology ontology) {
		
		// use cache.
		if (this.getNonPROWLClassesCache() != null && !this.getNonPROWLClassesCache().isEmpty()) {
			return this.getNonPROWLClassesCache();
		}
		
		// the cache is clear. Reload classes 
		Collection<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		
		try {
			// TODO find out a class expression that queries owl:Thing - <PR-OWL classes> (Note: Thing and not(<PR-OWL classes>) returns nothing, because the "not" operation seems to have special meaning)
			
			// add all classes (note: looking for all subclasses of owl:Thing is not enough, because some top classes may not be explicitly asserted as subclass of owl:Thing)
			ret.addAll(ontology.getClassesInSignature(true));
			
			// remove PR-OWL 1 elements (we are removing here because we do not know how to query Thing and not(<PR-OWL classes>) in open-world assumption)
			// TODO find out a way to query Thing and not(<PR-OWL classes>) in open-world assumption
			
			ret.removeAll(this.getPROWLClasses(ontology));
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		// update cache
		this.setNonPROWLClassesCache(ret);
		
		// do not return ret itself, because it will allow direct access to cache
		return new HashSet<OWLClassExpression>(ret);
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
		
		for (OWLClassExpression subclass : owlClass.getSubClasses(ontology)) {
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
	 * @return the prowlOntologyNamespaceURI
	 */
	public String getProwlOntologyNamespaceURI() {
		return prowlOntologyNamespaceURI;
	}

	/**
	 * @param prowlOntologyNamespaceURI the prowlOntologyNamespaceURI to set
	 */
	public void setProwlOntologyNamespaceURI(String prowlOntologyNamespaceURI) {
		this.prowlOntologyNamespaceURI = prowlOntologyNamespaceURI;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#getPROWLClasses(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public Collection<OWLClassExpression> getPROWLClasses(OWLOntology ontology) {
		// returning value
		Collection<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		
		// use PR-OWL namespace (prefix)
		PrefixManager prefixManager = new DefaultPrefixManager(this.getProwlOntologyNamespaceURI() + '#');
		
		// add ArgRelationship
		OWLClass classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ARGUMENT_RELATIONSHIP, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add SimpleArgRelationship (this is because SimpleArgRelationship is not an asserted subclass of argument relationship)
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.SIMPLE_ARGUMENT_RELATIONSHIP, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add BuiltInRV
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.BUILTIN_RV, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add CondRelationship
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.CONDRELATIONSHIP, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add Entity
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass("Entity", prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add Exemplar
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.EXEMPLAR, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add MFrag
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MFRAG, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add MTheory
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MTHEORY, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add Node
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.NODE, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add OVariable
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ORDINARY_VARIABLE, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add ProbAssign
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.PROBABILITYASSIGNMENT, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add ProbDist
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.PROBABILITYDISTRIBUTION, prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));

		// add ProbDist
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass("ProbDist", prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		// add ProbAssign
		classToAdd = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass("ProbAssign", prefixManager);
		ret.add(classToAdd);
		ret.addAll(this.getAssertedDescendants(classToAdd, ontology));
		
		return ret;
	}
}
