/**
 * 
 */
package unbbayes.io.mebn.prowl2.owlapi;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Classes implementing this interface can extract PR-OWL2 individuals from an 
 * OWLAPI compatible ontology.
 * @author Shou Matsumoto
 *
 */
public interface IPROWL2IndividualsExtractor {

	/**
	 * This method is a helper that returns PR-OWL2 individuals (i.e. individuals
	 * of PR-OWL 2 classes) from an ontology.
	 * This is useful to remove individuals temporary from an ontology, because
	 * PR-OWL2 individuals should be treated as MEBN elements rather than OWL ontology.
	 * @param ontology : ontology to be searched
	 * @param reasoner : if set to non-null, this reasoner will be used in order to extract prowl2 individuals.
	 * @return
	 */
	public Collection<OWLIndividual> getPROWL2Individuals(OWLOntology ontology, OWLReasoner reasoner);
	
//	/**
//	 * This is the opposite of {@link #getPROWL2Individuals(OWLOntology, OWLReasoner)}.
//	 * It returns individuals that are not part of PR-OWL 2 specification.
//	 * @param ontology : ontology to be searched
//	 * @param reasoner : if set to non-null, this reasoner will be used in order to extract prowl2 individuals.
//	 * @return
//	 */
//	public Collection<OWLIndividual> getNonPROWL2Individuals(OWLOntology ontology, OWLReasoner reasoner);
	
	/**
	 * This method will reset objects of this interface (e.g. clear caches).
	 */
	public void resetPROWL2IndividualsExtractor();
}
