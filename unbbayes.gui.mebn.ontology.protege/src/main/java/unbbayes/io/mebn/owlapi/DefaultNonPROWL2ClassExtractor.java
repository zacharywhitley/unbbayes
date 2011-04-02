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
		// TODO Auto-generated constructor stub
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
			
			Collection<OWLClassExpression> classesToRemove = new HashSet<OWLClassExpression>();
			classesToRemove.add(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
			for (OWLClassExpression classToRemove : ret) {
				try {
					// compare the prefix of the class' URI and PR-OWL2's URI to determine if this class is a PR-OWL2 class
					if (classToRemove.asOWLClass().getIRI().toString().startsWith(IPROWL2ModelUser.PROWL2_NAMESPACEURI)) {
						classesToRemove.add(classToRemove);
					}
				} catch (Throwable e) {
					e.printStackTrace();
					continue;
				}
			}
			
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


}
