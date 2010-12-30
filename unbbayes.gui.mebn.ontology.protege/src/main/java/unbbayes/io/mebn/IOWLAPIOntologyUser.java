/**
 * 
 */
package unbbayes.io.mebn;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This interface is mainly used by classes which shall have some knowledge about
 * what OWL Model manager (class which stores OWL ontology informations) is being
 * used by currently running process, but details should be hidden.
 * For example, a MEBN might need to know the last OWL Model Manager used to load an
 * ontology, in order to perform "save current" action; or to store 
 * whole parts of OWL ontology "in-memory" so that, when saving, reusing
 * the stored OWL model allows us to store also the .
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
	
}
