/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Classes implementing this interface can extract non-PR-OWL classes from an ontology.
 * @author Shou Matsumoto
 *
 */
public interface INonPROWLClassExtractor {

	/**
	 * This method is a helper that returns non-PR-OWL classes from an ontology.
	 * This is useful to obtain classes that do not belong to probabilistic ontology.
	 * @param ontology
	 * @return
	 */
	public Collection<OWLClassExpression> getNonPROWLClasses(OWLOntology ontology);
	

	/**
	 * This method does the opposite of {@link #getNonPROWLClasses(OWLOntology)}.
	 * This method may be helpful, because the open-world assumption of OWL prohibits
	 * us to obtain PR-OWL classes by querying classes that are not {@link #getNonPROWLClasses(OWLOntology)}.
	 * @param ontology
	 * @return
	 */
	public Collection<OWLClassExpression> getPROWLClasses(OWLOntology ontology);
	
	/**
	 * This method will reset objects of this interface (e.g. clear caches).
	 */
	public void resetNonPROWLClassExtractor();
	
	
}