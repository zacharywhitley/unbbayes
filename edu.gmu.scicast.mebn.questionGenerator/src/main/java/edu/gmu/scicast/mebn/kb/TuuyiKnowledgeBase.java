/**
 * 
 */
package edu.gmu.scicast.mebn.kb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBase;
import unbbayes.prs.mebn.ssbn.OVInstance;
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
	private boolean isToUseExpressionsToQueryExpandableIndividuals = false;

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
	 * Attempts to extract  {@link OntologyClient} {@link Term#getId()} from the related owl individual.
	 * @param individual
	 * @param reasoner
	 * @param rootOntology
	 * @return null if not found.
	 */
	public Integer getTermIdFromOWLIndividual (OWLIndividual individual, OWLReasoner reasoner, OWLOntology rootOntology) {
		if (individual instanceof TuuyiTermOWLIndividual) {
			return ((TuuyiTermOWLIndividual) individual).getTermID();
		}
		// extract owl data property that stores term ID
		OWLDataProperty hasUID = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_UID_PROPERTY_NAME, getQuestionGeneratorOntologyPrefixManager());
				
		// check if we can query less broader terms by ID. If not, query by name.
		Set<OWLLiteral> uids = reasoner.getDataPropertyValues(individual.asOWLNamedIndividual(), hasUID);
		
		// extract the related individuals
		if (uids != null && !uids.isEmpty()) {
			// use the 1st ID
			return uids.iterator().next().parseInteger();
		}
		// query by name
		Term term = getOntologyClient().getTermBySimpleName(this.extractName(individual));
		if (term != null) {
			return term.getId();
		}
		term = getOntologyClient().matchTermByName(this.extractName(individual));
		if (term != null && term.getSimpleName().equals(this.extractName(individual))) {
			return term.getId();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#searchFinding(unbbayes.prs.mebn.ResidentNode, java.util.Collection)
	 */
	public StateLink searchFinding(ResidentNode resident, Collection<OVInstance> listArguments) {
		try {
			return super.searchFinding(resident, listArguments);
		} catch (Exception e) {
			Debug.println(getClass(), "Was not able to search findings of " + resident + " given arguments " + listArguments, e);
		}
		return null;
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
		if (matchingIndividuals == null || matchingIndividuals.isEmpty()) {
			return Collections.emptyList();
		}
		// collection of individuals that matches DL query and also matches condition to trigger search on remote server (i.e. consider individual as if they were classes in remote server, and get individuals in remote server -- expand)
		Collection<OWLIndividual> toExpand = null;
		if (isToUseExpressionsToQueryRemoteIndividuals()) {
			// this is a slow operation, so only execute it if the flag is set
			toExpand = super.getOWLIndividuals("(" + expression + ") that " + REMOTE_CLASS_NAME, reasoner, rootOntology);
		}
		
		// a mapping from term ID (of tuuyi server) and owl individuals created temporary only to represent such terms 
		Map<Integer, TuuyiTermOWLIndividual> termIDToOWLIndividualMap = new HashMap<Integer, TuuyiTermOWLIndividual>();
		
		// current implementation assumes that we are using DL reasoner
		if (reasoner != null) {
			Collection<OWLIndividual> toIterate = toExpand;
			if (!isToUseExpressionsToQueryRemoteIndividuals()) {
				// only consider individuals which isToExpand == true
				toIterate = new ArrayList<OWLIndividual>(matchingIndividuals.size());
				// this iteration may not be so fast for huge ontologies, but in most of cases it's faster than querying an expression (to fill the list "toExpand")
				for (OWLIndividual owlIndividual : matchingIndividuals) {
					if (this.isToExpand(owlIndividual,reasoner,rootOntology)) {
						toIterate.add(owlIndividual);
					}
				}
			}
			
			// query server so that we can get less broader instances (inverse of skol:broader) of individuals to expand
			for (OWLIndividual expandable : toIterate) {
				matchingIndividuals.addAll(this.expandTermIndividual(expandable, termIDToOWLIndividualMap, reasoner, rootOntology));
			}
		} else {
			throw new RuntimeException("Current implementation requires usage of a valid DL reasoner.");
		}
		
		// delete individuals that are marked to be "excluded" in upper ontology
		Collection<OWLIndividual> toExclude = null;
		if (isToUseExpressionsToQueryRemoteIndividuals()) {
			// collection of individuals that were tagged to be excluded from queries
			toExclude = super.getOWLIndividuals("(" + expression + ") that " + IS_TO_EXCLUDE_DATA_PROPERTY_NAME + " value true", reasoner, rootOntology);
			// removing from an array list will remove all individuals that matches with Object#equal, so they will check for IRI (since Object#equal is overwritten by OWLNamedIndividual to check for IRIs).
			matchingIndividuals.removeAll(toExclude);
		} else {
			// check one-by-one if the property IS_TO_EXCLUDE_DATA_PROPERTY_NAME is true, and remove them
			OWLDataProperty isToExclude = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_TO_EXCLUDE_DATA_PROPERTY_NAME, getQuestionGeneratorOntologyPrefixManager());
			// this may look slower, but in most of cases it is still faster than using OWLClassExpression to query for individuals.
			toExclude = new ArrayList<OWLIndividual>(matchingIndividuals.size());	// instantiate now, because it was null. Make sure this is non-null
			for (OWLIndividual owlIndividual : matchingIndividuals) {
				Set<OWLLiteral> values = null;
				try {
					values = reasoner.getDataPropertyValues(owlIndividual.asOWLNamedIndividual(), isToExclude);
				} catch (Exception e) {
					Debug.println(getClass(), "Could not use reasoner to resolve " + owlIndividual 
							+ ". All exceptions in reasoner will be considered as non-existing individual/assertion.", e);
					continue;
				}
				
				if (values != null && !values.isEmpty() && values.iterator().next().parseBoolean()) {	// only check 1st value, because this OWL property is expected to happen only once.
					toExclude.add(owlIndividual);	// delete this individual
				}
			}
		}
		// remove all marked individuals at once
		matchingIndividuals.removeAll(toExclude);
		// also remove all stubs that has same term id. 
		for (OWLIndividual owlIndividual : toExclude) {
			// and also delete stubs that has same term ID. Supposedly, there no duplicates in such stubs, because only 1 stub was created for each term id previously
			Integer termID = this.getTermIdFromOWLIndividual(owlIndividual, reasoner, rootOntology);	// extract ID
			TuuyiTermOWLIndividual tuuyiIndividualToDelete = termIDToOWLIndividualMap.get(termID);		// check if we have created a stub individual for this term id
			if (tuuyiIndividualToDelete != null) {
				matchingIndividuals.remove(tuuyiIndividualToDelete);
			}
		}
		
		return matchingIndividuals;
	}
	
	/**
	 * This method implements an algorithm to handle the maxDepth object property of the scicast question generator upper ontology.
	 * This starts querying remote server if an individual is flagged to do so.
	 * @param expandable : the owl individual to be checked
	 * @param termIDToOWLIndividualMap : input/output argument. this map will be filled with tuuyi ontology's term ID and respective OWL individual stubs created just in order to
	 * represent terms as instances of OWLNamedIndividual. Terms that are already mapped here (i.e. terms with same ID) won't be created again.
	 * @param reasoner
	 * @param rootOntology
	 * @return set of owl individuals created.
	 */
	protected Collection<? extends OWLIndividual> expandTermIndividual( OWLIndividual expandable, Map<Integer, TuuyiTermOWLIndividual> termIDToOWLIndividualMap, OWLReasoner reasoner, OWLOntology rootOntology) {

		// basic assertions
		if (reasoner == null) {
			throw new UnsupportedOperationException("A DL reasoner is expected");
		} else if (rootOntology == null) {
			rootOntology = reasoner.getRootOntology();
		}
		if (expandable == null || !expandable.isNamed()) {
			// ignore anonymous individuals
			return Collections.emptyList();
		}
		if (termIDToOWLIndividualMap == null) {
			// make sure it's not null
			termIDToOWLIndividualMap = new HashMap<Integer, TuuyiTermOWLIndividual>();
		}
		
		// this will store less broader terms (results of query to Tuuyi ontology server) after the next if-clause
		Set<Integer> termDescendants = new HashSet<Integer>();	
		
		// get the term ID of this individual 
		Integer termID = getTermIdFromOWLIndividual(expandable, reasoner, rootOntology);
		
		// extract the related individuals
		if (termID != null) {
			// extract owl data property that indicates how deep in hierarchy we shall query the remote server ontology
			OWLDataProperty hasMaxDepth = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_MAX_DEPTH_PROPERTY_NAME, getQuestionGeneratorOntologyPrefixManager());
			// data property that stores relationship/property ID
			OWLDataProperty hierarchyPropertyID = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HIERARCHY_PROPERTY_ID, getQuestionGeneratorOntologyPrefixManager());
			// data property that stores whether hierarchyPropertyID shall be interpreted as being an inverse property or not
			OWLDataProperty isInverseHierarchyPropertyID = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_INVERSE_HIERARCHY_PROPERTY_ID, getQuestionGeneratorOntologyPrefixManager());
			
			// check for values stored at hasMaxDepth data property (indicates how deep we will go into the tuuyi ontolgy hierarchy)
			// This is how many levels in hierarchy we shall expand individual. if it is to expand, then maxDepth will be > 0
			int maxDepth = 0;
			
			// check what property we shall use to query for descendants. 
			Integer propertyId = null;
			Set<OWLLiteral> propertyIDs = reasoner.getDataPropertyValues(expandable.asOWLNamedIndividual(), hierarchyPropertyID);
			if (propertyIDs != null && !propertyIDs.isEmpty()) {
				// use the 1st instance
				propertyId = propertyIDs.iterator().next().parseInteger();
				// If this is present, and maxDepth is unspecified, then maxDepth is 1 by default.
				maxDepth = 1;
			}
			
			// read max depth from ontology
			Set<OWLLiteral> maxDepthValues = reasoner.getDataPropertyValues(expandable.asOWLNamedIndividual(), hasMaxDepth);
			if (maxDepthValues != null && !maxDepthValues.isEmpty()) {
				// only check the 1st occurrence
				maxDepth = maxDepthValues.iterator().next().parseInteger();
				if (maxDepth < 0) {
					// negative numbers represent that we shall search for the whole structure.
					maxDepth = Integer.MAX_VALUE;
				}
			}
			
			// check if propertyId shall be considered as being inverse property or not
			boolean isInverse = true;	// Note: it is supposed to be considered as true, by default.
			Set<OWLLiteral> inverseLiterals = reasoner.getDataPropertyValues(expandable.asOWLNamedIndividual(), isInverseHierarchyPropertyID);
			if (inverseLiterals != null && !inverseLiterals.isEmpty()) {
				// use the 1st instance
				isInverse = inverseLiterals.iterator().next().parseBoolean();
			}
			
			// recursively find descendants
//			termDescendants = getOntologyClient().getTermDescendants(termId, propertyId, isInverse)
			fillTermDescendantsRecursive(termDescendants,termID, propertyId, isInverse,maxDepth); // the set termDescendants will be filled with term ids
		}
		
		Collection<OWLIndividual> ret = new HashSet<OWLIndividual>();
		
		// convert terms to temporary owl individuals (so that they can be returned by this method)
		if (termDescendants != null) {
			for (Integer descendantID : termDescendants) {
				// we need to get the name, so get it from its ID
				Term descendant = getOntologyClient().getTermById(descendantID);
				if (descendant != null && descendant.getSimpleName() != null && descendant.getSimpleName().trim().length() > 0) {
					// do not create duplicate individuals
					if (!termIDToOWLIndividualMap.containsKey(descendant.getId())) {
						// this is a temporary owl individual associated with no data factory. By default, use same namespace of the external ontology
						TuuyiTermOWLIndividual tuuyiTermOWLIndividual = new TuuyiTermOWLIndividual(IRI.create(EXTERNAL_ONTOLOGY_NAMESPACE_URI + "#" + descendant.getSimpleName()), descendant.getId());
						termIDToOWLIndividualMap.put(descendant.getId(), tuuyiTermOWLIndividual);
						ret.add(tuuyiTermOWLIndividual);
					}
				} else {
					System.err.println("Could not extract name of term with ID = " + descendantID);
				}
			}
		}
	
		return ret;
	}

	/**
	 * Recursively calls {@link OntologyClient#getTermDescendants(int, Integer, boolean)}
	 * decreasing maxDepth for each recursive call, until it reaches zero.
	 * @param termIdToLookForDescendants : will be passed to {@link OntologyClient#getTermDescendants(int, Integer, boolean)}
	 * @param propertyId: {@link OntologyClient#getTermDescendants(int, Integer, boolean)}
	 * @param isInverse: {@link OntologyClient#getTermDescendants(int, Integer, boolean)}
	 * @param maxDepth : will stop recursivity when <= 0
	 * @param descendants : this set will be filled with descendants. It is also referenced by the method, so that recursivity also stops when
	 * reached elements in this set again.
	 */
	private void fillTermDescendantsRecursive(Set<Integer> descendants, Integer termIdToLookForDescendants, Integer descendantPropertyId, boolean isInverse, int maxDepth) {
		// condition to finish recursion
		if (maxDepth <= 0 || descendants.contains(termIdToLookForDescendants)) {
			return;
		}
		
		// descendants at depth 1
		descendants.addAll(getOntologyClient().getTermDescendants(termIdToLookForDescendants, descendantPropertyId, isInverse));
		
		// check recursively for descendants at next levels
		Set<Integer> nextDepth = new HashSet<Integer>();
		for (Integer nextDepthTermId : descendants) {
			this.fillTermDescendantsRecursive(nextDepth,nextDepthTermId, descendantPropertyId, isInverse, maxDepth-1);
		}
		
		// append the descendants of next level and finish
		descendants.addAll(nextDepth);
		
	}

	/**
	 * This method returns true if this individual is defined in remote ontology accessible at {@link #getOntologyClient()}
	 * @param expandable : individual to be considered
	 * @param reasoner : if non-null, this reasoner will be used to check for type and properties
	 * @param rootOntology : if resoner is not specified, then this ontology will be used in order to check for types and properties.
	 * @return If type of individual is {@link TuuyiOntologyUser#REMOTE_CLASS_NAME}, then this method will return true.
	 * @see #getQuestionGeneratorOntologyPrefixManager()
	 */
	protected boolean isToExpand(OWLIndividual expandable, OWLReasoner reasoner, OWLOntology rootOntology) {
		if (reasoner == null) {
			throw new UnsupportedOperationException("Current version requires presence of DL reasoner.");
		}
		// prepare the OWL class to compare with. If the types of the individual contains this class, then we shall return true
		OWLClass remoteClass = rootOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(REMOTE_CLASS_NAME, getQuestionGeneratorOntologyPrefixManager());
		
		// obtain what are the types of this individual
		NodeSet<OWLClass> types = reasoner.getTypes(expandable.asOWLNamedIndividual(), false);
		for (OWLClass owlClass : types.getFlattened()) {
			if (owlClass.equals(remoteClass)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#parseExpression(java.lang.String)
	 */
	public OWLClassExpression parseExpression(String expression) {
		OWLClassExpression ret = null;
		try {
			ret = super.parseExpression(expression);
		} catch (Exception e) {
			e.printStackTrace();
			// return owl:Nothing
			OWLModelManager owlModelManager = getOWLModelManager();
			if (owlModelManager != null) {
				return owlModelManager.getOWLDataFactory().getOWLNothing();
			} else {
				return new OWLClassImpl(null, OWLRDFVocabulary.OWL_NOTHING.getIRI());
			}
		}
		return ret;
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

	/**
	 * @return the isToUseExpressionsToQueryExpandableIndividuals: if false, then {@link #getOWLIndividuals(String, OWLReasoner, OWLOntology)} will
	 * avoid using {@link OWLClassExpression} to query for individuals related to the ontology {@link TuuyiOntologyUser#EXTERNAL_ONTOLOGY_NAMESPACE_URI}.
	 */
	public boolean isToUseExpressionsToQueryRemoteIndividuals() {
		return isToUseExpressionsToQueryExpandableIndividuals;
	}

	/**
	 * @param isToUseExpressionsToQueryExpandableIndividuals the isToUseExpressionsToQueryExpandableIndividuals to set. 
	 * If false, then {@link #getOWLIndividuals(String, OWLReasoner, OWLOntology)} will
	 * avoid using {@link OWLClassExpression} to query for individuals related to the ontology {@link TuuyiOntologyUser#EXTERNAL_ONTOLOGY_NAMESPACE_URI}.
	 */
	public void setToUseExpressionsToQueryExpandableIndividuals(
			boolean isToUseExpressionsToQueryExpandableIndividuals) {
		this.isToUseExpressionsToQueryExpandableIndividuals = isToUseExpressionsToQueryExpandableIndividuals;
	}

}
