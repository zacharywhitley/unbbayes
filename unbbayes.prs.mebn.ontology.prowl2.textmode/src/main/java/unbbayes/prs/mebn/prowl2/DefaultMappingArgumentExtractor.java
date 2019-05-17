package unbbayes.prs.mebn.prowl2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link IMappingArgumentExtractor}
 * @author Shou Matsumoto
 *
 */
public class DefaultMappingArgumentExtractor implements
		IMappingArgumentExtractor {

	/**
	 * The default constructor is not visible in public.
	 * Use {@link #newInstance()} instead.
	 */
	protected DefaultMappingArgumentExtractor() {}
	
	/**
	 * Use this method as default constructor
	 * @return
	 */
	public static IMappingArgumentExtractor newInstance() {
		return new DefaultMappingArgumentExtractor();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMappingArgumentExtractor#getOWLPropertiesOfArgumentsOfSelectedNode(unbbayes.prs.INode, unbbayes.prs.mebn.MultiEntityBayesianNetwork, org.semanticweb.owlapi.model.OWLOntology)
	 */
	@SuppressWarnings("deprecation")
	public Map<Argument, Map<OWLProperty, Integer>> getOWLPropertiesOfArgumentsOfSelectedNode( 
			INode selectedNode, MultiEntityBayesianNetwork mebn,
			OWLOntology ontology) {
		
		Map<Argument, Map<OWLProperty, Integer>> ret = new HashMap<Argument, Map<OWLProperty,Integer>>();
		
		// assert
		if (mebn == null || selectedNode == null) {
			return ret;
		}
		
		// Ignore nodes that are not resident nodes
		if (!(selectedNode instanceof ResidentNode)) {
			try {
				Debug.println(this.getClass(), selectedNode + " is not a resident node, thus its argument mappings are going to be ignored.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return ret;
		}
		
		// convert to resident node
		ResidentNode resident = (ResidentNode)selectedNode;
		
		// extract owl ontology
//		OWLOntology ontology = null;
//		if (this.getMebn().getStorageImplementor() != null &&  this.getMebn().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
//			ontology = ((IOWLAPIStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getAdaptee();
//		}

		// ignore mebn that do not have an associated ontology
		if (ontology == null) {
			try {
				Debug.println(this.getClass(), mebn + " does not contain an OWL2 ontology.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return ret;
		}
		
		// extract owl factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// iterate on arguments
		List<Argument> argumentList = resident.getArgumentList();
		if (argumentList != null) {
			for (Argument argument : argumentList) {
				// extract IRIs of "subjectFrom" mapping
				Collection<IRI> iris = IRIAwareMultiEntityBayesianNetwork.getIsSubjectFromMEBN(mebn, argument);
				if (iris != null) {
					// extract properties from IRI
					for (IRI iri : iris) {
						// add mapping to ret. If mapping exists, just add another entry
						Map<OWLProperty, Integer> mapping = ret.get(argument);
						if (mapping == null) {
							mapping = new HashMap<OWLProperty, Integer>();
						}
						// check if iri is an object property or data property
						if (ontology.containsDataPropertyInSignature(iri, true)) {
							// this IRI is a data property
							mapping.put(factory.getOWLDataProperty(iri), SUBJECT_CODE); // true means "add as subject of owl property"
							try {
								Debug.println(this.getClass(), argument + " is subject of data property " + iri);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else {
							// default: this IRI is an object property
							mapping.put(factory.getOWLObjectProperty(iri), SUBJECT_CODE); // true means "add as subject of owl property"
							try {
								Debug.println(this.getClass(), argument + " is subject of object property " + iri);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						ret.put(argument, mapping);
					}
				}
				// extract IRIs of "objectOf" mapping
				iris = IRIAwareMultiEntityBayesianNetwork.getIsObjectFromMEBN(mebn, argument);
				if (iris != null) {
					// extract properties from IRI
					for (IRI iri : iris) {
						// add mapping to ret. If mapping exists, just add another entry
						Map<OWLProperty, Integer> mapping = ret.get(argument);
						if (mapping == null) {
							mapping = new HashMap<OWLProperty, Integer>();
						}
						// check if iri is an object property or data property
						if (ontology.containsDataPropertyInSignature(iri, true)) {
							// this IRI is a data property. 
							mapping.put(factory.getOWLDataProperty(iri), OBJECT_CODE);	// false means "add as object of owl property"
							try {
								Debug.println(this.getClass(), argument + " is object of data property " + iri + ", but we are not checking type consistency yet.");
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else {
							// default: this IRI is an object property
							mapping.put(factory.getOWLObjectProperty(iri), OBJECT_CODE);  // false means "add as object of owl property"
							try {
								Debug.println(this.getClass(), argument + " is object of of object property " + iri);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						ret.put(argument, mapping);
					}
				}
			}
		}
		
		return ret;
	}

}
