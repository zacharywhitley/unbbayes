/**
 * 
 */
package unbbayes.prs.mebn;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.prs.INode;

/**
 * This is an interface for objects that can extract relationships between owl properties
 * and arguments of nodes.
 * @author Shou Matsumoto
 * @see unbbayes.gui.mebn.extension.ArgumentMappingDialog
 * @see unbbayes.gui.mebn.extension.DefinesUncertaintyOfPanel
 */
public interface IMappingArgumentExtractor {
	
	/** If {@link #getOWLPropertiesOfArgumentsOfSelectedNode(INode, MultiEntityBayesianNetwork, OWLOntology)} returns a map to this code, the mapping is EXPLICITLY set to "undefined" */
	public static final Integer UNDEFINED_CODE = new Integer(0);	
	/** If {@link #getOWLPropertiesOfArgumentsOfSelectedNode(INode, MultiEntityBayesianNetwork, OWLOntology)} returns a map to this code, the mapping was a "isSubjectIn" */
	public static final Integer SUBJECT_CODE = new Integer(1);
	/** If {@link #getOWLPropertiesOfArgumentsOfSelectedNode(INode, MultiEntityBayesianNetwork, OWLOntology)} returns a map to this code, the mapping was a "isObjectIn" */
	public static final Integer OBJECT_CODE = new Integer(2);
	
	/**
	 * This method obtains all mapped arguments from the selected node.
	 * @param selectedNode : the selected node to extract properties
	 * @param mebn : mebn containing selectedNode. This will be used to extract mappings from ontology to selectedNode
	 * @param ontology: ontology containing the OWL property mapped to selectedNode
	 * @return a mapping from an argument to its OWL property. The last Integer element indicates if the argument
	 * is explicitly undefined ({@link #UNDEFINED_CODE}), a subject of the owl property ({@link #SUBJECT_CODE}) 
	 * or object ({@link #OBJECT_CODE}), or something else (any other code).
	 */
	public Map<Argument, Map<OWLProperty<?, ?>, Integer>> getOWLPropertiesOfArgumentsOfSelectedNode(INode selectedNode, MultiEntityBayesianNetwork mebn, OWLOntology ontology);
}
