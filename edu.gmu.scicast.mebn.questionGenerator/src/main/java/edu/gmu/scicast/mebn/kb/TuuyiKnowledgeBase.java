/**
 * 
 */
package edu.gmu.scicast.mebn.kb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBase;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;

import edu.gmu.scicast.mebn.TuuyiOntologyUser;

/**
 * This class extends {@link OWL2KnowledgeBase} in order to fulfill
 * requirements for SciCast's MEBN-based question generator.
 * @author Shou Matsumoto
 *
 */
public class TuuyiKnowledgeBase extends PROWL2KnowledgeBase implements TuuyiOntologyUser {
	
	private OntologyClient ontologyClient = null;

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
//		Collection<OWLIndividual> notFiltered = new ArrayList<OWLIndividual>(super.getOWLIndividuals(expression, reasoner, rootOntology)); // use an array list, so that we can use Object#equals(Object) to remove objects
		Collection<OWLIndividual> notFiltered = (super.getOWLIndividuals(expression, reasoner, rootOntology));
		Collection<OWLIndividual> toExclude = super.getOWLIndividuals("(" + expression + ") and " + IS_TO_EXCLUDE_DATA_PROPERTY_NAME + " value true", reasoner, rootOntology);
		notFiltered.removeAll(toExclude); 
		return notFiltered;
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
}
