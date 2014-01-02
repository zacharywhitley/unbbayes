/**
 * 
 */
package edu.gmu.scicast.mebn.kb;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBase;
import unbbayes.util.Debug;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;
import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.Term;

import edu.gmu.scicast.mebn.TuuyiOntologyUser;

/**
 * This class extends {@link OWL2KnowledgeBase} in order to fulfill
 * requirements for SciCast's MEBN-based question generator.
 * @author Shou Matsumoto
 *
 */
public class TuuyiKnowledgeBase extends PROWL2KnowledgeBase implements TuuyiOntologyUser {
	
	private OntologyClient ontologyClient = null;
	private PrefixManager questionGeneratorOntologyPrefixManager = EXTERNAL_ONTOLOGY_DEFAULT_PREFIX_MANAGER;

	/**
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	public TuuyiKnowledgeBase() {
		super();
	}

	/**
	 * Simply delegates to {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator, OntologyClient)},
	 * passing a default {@link OntologyClient}.
	 * @return
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(mebn, mediator, new OntologyClient());
	}
	/**
	 * Constructor method initializing fields
	 * @param mebn : this value will be set to {@link #setDefaultMEBN(MultiEntityBayesianNetwork)}
	 * @param mediator : this value will be set to {@link #setDefaultMediator(IMEBNMediator)}
	 * @return
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator, OntologyClient ontologyClient) {
		TuuyiKnowledgeBase ret = new TuuyiKnowledgeBase();
		ret.setDefaultOWLReasoner(null);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		ret.setOntologyClient(ontologyClient);
		return ret;
	}
	
	/**
	 * This method overwrites the method in the superclass so that it can ignore individuals
	 * whose the OWL data property {@link TuuyiOntologyUser#IS_TO_EXCLUDE_DATA_PROPERTY_NAME} set to true
	 * is not returned, and also to expand classes/individuals at the tuuyi ontology using
	 * the skos:broader property.
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getOWLIndividuals(org.semanticweb.owlapi.model.OWLClassExpression, org.semanticweb.owlapi.reasoner.OWLReasoner, org.semanticweb.owlapi.model.OWLOntology)
	 */
	protected Collection<OWLIndividual> getOWLIndividuals(String expression, OWLReasoner reasoner, OWLOntology rootOntology) {
		// collection of individuals that matches DL query
//		Collection<OWLIndividual> matchingIndividuals = new ArrayList<OWLIndividual>(super.getOWLIndividuals(expression, reasoner, rootOntology)); // use an array list, so that we can use Object#equals(Object) to remove objects
		Collection<OWLIndividual> matchingIndividuals = (super.getOWLIndividuals(expression, reasoner, rootOntology));
		
		// collection of individuals that matches DL query and also matches condition to trigger search on remote server (i.e. consider individual as if they were classes in remote server, and get individuals in remote server -- expand)
		Collection<OWLIndividual> toExpand = super.getOWLIndividuals("(" + expression + ") and " + REMOTE_CLASS_NAME, reasoner, rootOntology);
		
		// extract owl data property that indicates how deep in hierarchy we shall query the remote server ontology
		OWLDataProperty hasMaxDepth = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_MAX_DEPTH_PROPERTY_NAME, getQuestionGeneratorOntologyPrefixManager());
		// extract owl data property that stores ID
		OWLDataProperty hasUID = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_UID_PROPERTY_NAME, getQuestionGeneratorOntologyPrefixManager());
		
		// current implementation assumes that we are using DL reasoner
		if (reasoner != null) {
			// query server so that we can get less broader instances (inverse of skol:broader) of individuals to expand
			for (OWLIndividual expandable : toExpand) {
				if (!expandable.isNamed()) {
					// ignore anonymous individuals
					continue;
				}
				
				List<Integer> termDescendants = null;	// this will store less broader terms (results of query to Tuuyi ontology server)
				
				// check for values stored at hasMaxDepth data property (indicates how deep we will go into the tuuyi ontolgy hierarchy)
				Debug.println(getClass(), "Current version can only expand up to 1 level in hierarchy.");
				// TODO handle cases where hasMaxDepth < 0 or hasMaxDepth > 1
				Set<OWLLiteral> maxDepthValues = reasoner.getDataPropertyValues(expandable.asOWLNamedIndividual(), hasMaxDepth);
				if (maxDepthValues != null && !maxDepthValues.isEmpty() && maxDepthValues.iterator().next().parseInteger() > 0) {	// only check the 1st occurrence
					// check if we can query less broader terms by ID. If not, query by name.
					Set<OWLLiteral> uids = reasoner.getDataPropertyValues(expandable.asOWLNamedIndividual(), hasUID);
					if (uids != null && !uids.isEmpty()) {
						// use the 1st ID
						termDescendants = getOntologyClient().getTermDescendants(uids.iterator().next().parseInteger());
					} else {
						// query by name
						termDescendants = getOntologyClient().getTermDescendants(getOntologyClient().getTermBySimpleName(this.extractName(expandable)).getId());
					}
				}
				
				// convert terms to temporary owl individuals (so that they can be returned by this method)
				if (termDescendants != null) {
					for (Integer descendantID : termDescendants) {
						// we need to get the name, so get it from its ID
						Term descendant = getOntologyClient().getTermById(descendantID);
						if (descendant != null && descendant.getSimpleName() != null && descendant.getSimpleName().trim().length() > 0) {
							// this is a temporary owl individual associated with no data factory. By default, use same namespace of the external ontology
							matchingIndividuals.add(new OWLNamedIndividualImpl(null, IRI.create(EXTERNAL_ONTOLOGY_NAMESPACE_URI + "#" + descendant.getSimpleName())));
						} else {
							System.err.println("Could not extract name of term with ID = " + descendantID);
						}
					}
				}
			}
		} else {
			throw new RuntimeException("Current implementation requires usage of a valid DL reasoner.");
		}
		
		// collection of individuals that were tagged to be excluded from queries
		Collection<OWLIndividual> toExclude = super.getOWLIndividuals("(" + expression + ") and " + IS_TO_EXCLUDE_DATA_PROPERTY_NAME + " value true", reasoner, rootOntology);
		// removing from an array list will remove all individuals that matches with Object#equal, so they will check for IRI (since Object#equal is overwritten by OWLNamedIndividual to check for IRIs).
		matchingIndividuals.removeAll(toExclude); 
		
		return matchingIndividuals;
	}
	

	/**
	 * Obtains the manager of prefixes related to external ontology.
	 * @return prefix manager of http://www.scicast.org/questionGen/scicast.owl
	 */
	public PrefixManager getQuestionGeneratorOntologyPrefixManager() {
		return questionGeneratorOntologyPrefixManager;
	}

	/**
	 * @return the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 */
	public OntologyClient getOntologyClient() {
		return ontologyClient;
	}

	/**
	 * @param ontologyClient : the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 */
	public void setOntologyClient(OntologyClient ontologyClient) {
		this.ontologyClient = ontologyClient;
	}

	/**
	 * @param questionGeneratorOntologyPrefixManager the manager of prefixes related to external ontology: http://www.scicast.org/questionGen/scicast.owl
	 */
	public void setQuestionGeneratorOntologyPrefixManager(
			PrefixManager questionGeneratorOntologyPrefixManager) {
		this.questionGeneratorOntologyPrefixManager = questionGeneratorOntologyPrefixManager;
	}
}
