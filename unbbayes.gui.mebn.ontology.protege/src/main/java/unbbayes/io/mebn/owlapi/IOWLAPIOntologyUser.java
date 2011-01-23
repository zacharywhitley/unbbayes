/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * This interface is mainly used by classes which shall have some knowledge about
 * what OWL Model manager (class which stores OWL ontology informations) is being
 * used by currently running process, but details should be hidden.
 * For example, a MEBN might need to know the last OWL Model Manager used to load an
 * ontology, in order to perform "save current" action; or to store 
 * whole parts of OWL ontology "in-memory"..
 * @author Shou Matsumoto
 *
 */
public interface IOWLAPIOntologyUser {

	/**
	 * @return the last used OWL model manager
	 */
	public OWLOntology getLastOWLOntology();
	
	/**
	 * @param owlModelManager : the last used OWL model manager
	 */
	public void setLastOWLOntology(OWLOntology owlModelManager);
	
	/**
	 * Populate a MEBN from a specific ontology instead of a file.
	 * @param mebn : the MEBN to fill
	 * @param ontology : ontology to be read
	 * @param reasoner : reasoner to be used in order to extract MEBN elements from ontology
	 */
	public void loadMEBNFromOntology(MultiEntityBayesianNetwork mebn, OWLOntology ontology, OWLReasoner reasoner);
	
}
