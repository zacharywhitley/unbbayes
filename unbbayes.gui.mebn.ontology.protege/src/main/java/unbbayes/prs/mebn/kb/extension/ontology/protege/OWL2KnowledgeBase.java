/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.ontology.protege;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.io.mebn.owlapi.DefaultNonPROWLClassExtractor;
import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.INonPROWLClassExtractor;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.io.mebn.protege.ProtegeStorageImplementorDecorator;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ontology.protege.IOWLClassExpressionParserFacade;
import unbbayes.prs.mebn.ontology.protege.OWLClassExpressionParserFacade;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/**
 * This knowledge base delegates inference to {@link OWLReasoner}.
 * This class reuses {@link OWLReasoner} from {@link MultiEntityBayesianNetwork#getStorageImplementor()} if necessary (when no reasoner is provided).
 * The ability to specify a reasoner will make users able to call this Knowledge Base no matter what kind of {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * is provided. This aspect must be dealt with caution, because it means that changing a reasoner of {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * would not change the reasoner of this class.
 * @author Shou Matsumoto
 *
 */
public class OWL2KnowledgeBase implements KnowledgeBase, IOWLClassExpressionParserFacade, IPROWL2ModelUser {

	private OWLReasoner defaultOWLReasoner;
	
	private MultiEntityBayesianNetwork defaultMEBN;
	
	private IMEBNMediator defaultMediator;
	
	private IOWLClassExpressionParserFacade owlClassExpressionParserDelegator;
	
	private IPROWL2ModelUser prowlModelUserDelegator;
	
	private long maximumBuzyWaitingCount = 20;
	
	private long sleepTimeWaitingReasonerInitialization = 1000;
	
	private INonPROWLClassExtractor nonPROWLClassExtractor;

	/**
	 * The default constructor is only visible in order to allow inheritance
	 * @deprecated use {@link #getInstance(OWLReasoner, MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	protected OWL2KnowledgeBase() {
		super();
		try {
			this.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor method initializing fields
	 * @param reasoner : explicitly sets the value of {@link #getDefaultOWLReasoner()}; If null, {@link #getDefaultOWLReasoner()} will be extracted from {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * of {@link #getDefaultMEBN()}.
	 * @param mebn : this value will be set to {@link #setDefaultMEBN(MultiEntityBayesianNetwork)}
	 * @param mediator : this value will be set to {@link #setDefaultMediator(IMEBNMediator)}
	 * @return
	 */
	public static KnowledgeBase getInstance(OWLReasoner reasoner, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		OWL2KnowledgeBase ret = new OWL2KnowledgeBase();
		ret.setDefaultOWLReasoner(reasoner);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}
	
	/**
	 * Initialize default values
	 */
	protected void initialize() {
		this.setProwlModelUserDelegator(DefaultPROWL2ModelUser.getInstance());
		this.setNonPROWLClassExtractor(DefaultNonPROWLClassExtractor.getInstance());
	}
	
	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(null, mebn, mediator);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#clearKnowledgeBase()
	 */
	public void clearKnowledgeBase() {
		if (this.getNonPROWLClassExtractor() != null) {
			// clear the non-prowl class extractor.
			this.getNonPROWLClassExtractor().resetNonPROWLClassExtractor();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#clearFindings()
	 */
	public void clearFindings() {
		if (this.getNonPROWLClassExtractor() != null) {
			// clear the non-prowl class extractor.
			this.getNonPROWLClassExtractor().resetNonPROWLClassExtractor();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createGenerativeKnowledgeBase(unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void createGenerativeKnowledgeBase(MultiEntityBayesianNetwork mebn) {
		this.setDefaultMEBN(mebn);
		this.setDefaultOWLReasoner(null); // this setting forces the delegation of OWL reasoners
		if (this.getNonPROWLClassExtractor() != null) {
			// clear the non-prowl class extractor.
			this.getNonPROWLClassExtractor().resetNonPROWLClassExtractor();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createEntityDefinition(unbbayes.prs.mebn.entity.ObjectEntity)
	 */
	public void createEntityDefinition(ObjectEntity entity) {
		// ignore null elements
		if (entity == null) {
			try {
				Debug.println(this.getClass(), "Entity == null");
			}catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		// if no reasoner was found, we cannot do anything...
		if (this.getDefaultOWLReasoner() == null) {
			throw new IllegalStateException("A reasoner was called in " + this.getClass().getName() + "#createEntityDefinition(ObjectEntity), but no reasoner was specified.");
		}
		// Extract the IRI of the entity
		IRI iri = null;
		if (this.getDefaultMEBN() != null 
				&& this.getDefaultMEBN() instanceof IRIAwareMultiEntityBayesianNetwork) {
			iri = ((IRIAwareMultiEntityBayesianNetwork)this.getDefaultMEBN()).getIriMap().get(entity);
		}
		// create a new IRI if nothing was extracted
		if (iri == null) {
			Debug.println(this.getClass(), "No iri found for " + entity.getName() + ". Creating a new one from name.");
			iri = IRI.create(this.getDefaultOWLReasoner().getRootOntology().getOntologyID().getOntologyIRI().toString() + '#' + entity.getName());
		}
		try {
			Debug.println(this.getClass(), "IRI of entity " + entity + " = " + iri);
		}catch (Throwable t) {
			t.printStackTrace();
		}
		
		// create a new OWL class if it does not exist
		if (!this.getDefaultOWLReasoner().getRootOntology().containsClassInSignature(iri, true)) {
			// create the new class
			OWLClass owlClass = this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri);
			// extract the owl:Thing class, which will be the only superclass of the new class
			OWLClass thing = this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLThing();
			// indicate that new class is subclass of owl:Thing
			OWLAxiom subClassOfThingAxiom = this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, thing);
			// add new class (add axiom)
			AddAxiom addAxiom = new AddAxiom(this.getDefaultOWLReasoner().getRootOntology(), subClassOfThingAxiom);
			// commit changes (it will only update the ontology's java object, not the file)
			this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().applyChange(addAxiom);
		}

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createRandomVariableDefinition(unbbayes.prs.mebn.ResidentNode)
	 */
	public void createRandomVariableDefinition(ResidentNode resident) {
		
		// initial assertion
		if (resident == null) {
			Debug.println(this.getClass(), "There was an attempt to create a random variable definition for a null resident node...");
			return;
		}
		if (this.getDefaultOWLReasoner() == null) {
			throw new IllegalStateException("No reasoner found");
		}
		
		// This method creates object or data properties for resident nodes that are not present in the original OWL2 ontology
		// This is just to make sure that all resident nodes has its correspondent owl property in a OWL2 ontology
		
		IRI iri = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), resident); // we always use the default MEBN instead of the one linked to resident node.
		if (iri == null) {
			Debug.println(this.getClass(), "The IRI is not registered. Creating new one...");
			// generate IRI "automagically"
			iri = IRI.create(this.getOntologyPrefixManager(this.getDefaultOWLReasoner().getRootOntology()).getDefaultPrefix() + resident.getName());
			try {
				Debug.println(this.getClass(), "The new IRI for the OWL property related to node " + resident + " is " + iri);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			// extract factory
			OWLDataFactory factory = this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().getOWLDataFactory();
			
			// prepare variables to hold owl property
			OWLProperty property = null;				// this is the property to be added
			OWLAxiom addPropertyAxiom = null; 			// this is an axiom to add property into to ontology
			OWLAxiom rangeAxiom = null;					// this is an axiom to set property's range

			// generate the correspondent owl property
			if (resident.getArgumentList() != null 
					&& resident.getArgumentList().size() <= 1
					&& resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) {	// TODO stop using integer codes
				// if there was only 1 argument and node type is boolean, use datatype property
				property = factory.getOWLDataProperty(iri);
				// generate axiom that adds property to ontology
				addPropertyAxiom = factory.getOWLSubDataPropertyOfAxiom(property.asOWLDataProperty(), factory.getOWLTopDataProperty());
				// set boolean as range
				rangeAxiom = factory.getOWLDataPropertyRangeAxiom(property.asOWLDataProperty(), factory.getBooleanOWLDatatype());
			} else {
				// the default is to use object property
				property = factory.getOWLObjectProperty(iri);
				// generate axiom that adds property to ontology
				addPropertyAxiom = factory.getOWLSubObjectPropertyOfAxiom(property.asOWLObjectProperty(), factory.getOWLTopObjectProperty());
				// TODO constrain range
			}
			
			// TODO constrain domain using the 1st argument
			
			// add property to ontology and apply change
			this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().addAxiom(this.getDefaultOWLReasoner().getRootOntology(), addPropertyAxiom);
			
			// add range to property and apply change
			if (rangeAxiom != null) {
				this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().addAxiom(this.getDefaultOWLReasoner().getRootOntology(), rangeAxiom);
			}
			
			try {
				Debug.println(this.getClass(), "Added property " + property + " to ontology in order to represent the deterministic side of " + resident);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			// if everything goes OK, register IRI into mebn
			IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(this.getDefaultMEBN(), resident, iri);
		} else {
			// the IRI is already registered
			Debug.println(this.getClass(), "The IRI is already registered.");
		}
		
		try {
			Debug.println(this.getClass(), "The IRI " + iri + " of node " + resident + "is registered in MEBN " + resident.getMFrag().getMultiEntityBayesianNetwork());
		}catch (Throwable t) {
			t.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#insertEntityInstance(unbbayes.prs.mebn.entity.ObjectEntityInstance)
	 */
	public void insertEntityInstance(ObjectEntityInstance entityInstance) {
		// initial assertion
		if (entityInstance == null) {
			Debug.println(this.getClass(), "Attempted to add null object entity instance. Ignoring....");
			return;
		}
		
		try {
			Debug.println(this.getClass(), "Entity finding: " + entityInstance.getName());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		// only create new instance if it does not exist yet, but we can check it only if MEBN was set.
		
		
		// extract IRI to check if instance exists
		IRI iri = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), entityInstance);
		if (iri == null) {
			Debug.println(this.getClass(),"No IRI found for entity instance. Generating new one...");
			
			// assertion
			if (this.getDefaultOWLReasoner() == null) {
				// this KB works only if reasoner is set
				throw new IllegalStateException("No OWL reasoner found.");
			}
			
			// extract factory
			OWLDataFactory factory = this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().getOWLDataFactory();
			
			// create IRI of owl individual
			iri = IRI.create(this.getOntologyPrefixManager(this.getDefaultOWLReasoner().getRootOntology()).getDefaultPrefix() + entityInstance.getName());

			// crate owl individual
			OWLIndividual individual = factory.getOWLNamedIndividual(iri);
			
			// obtain class to add individual. Use the same of the related object entity
			OWLClass entityClass = null;
			IRI classIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), entityInstance.getInstanceOf());
			if (classIRI == null || !this.getDefaultOWLReasoner().getRootOntology().containsClassInSignature(classIRI)) {		
				// use owl:Thing if we could not find the proper class
				entityClass = factory.getOWLThing();
				classIRI = entityClass.getIRI();
				try {
					Debug.println(this.getClass(), "Could not find proper class for individual " + individual + ". Class set to " + entityClass);
				}catch (Throwable t) {
					t.printStackTrace();
				}
			} else {
				// this.getDefaultOWLReasoner().getRootOntology().containsClassInSignature(classIRI) == true, so class exists
				entityClass = factory.getOWLClass(classIRI);
				try {
					Debug.println(this.getClass(), "The class of individual " + individual + " is " + entityClass);
				}catch (Throwable t) {
					t.printStackTrace();
				}
			}
			
			// asserting the class of the new individual
			OWLAxiom classAssertionAxiom = factory.getOWLClassAssertionAxiom(entityClass, individual);
			
			// add axiom and apply change
			this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().addAxiom(this.getDefaultOWLReasoner().getRootOntology(), classAssertionAxiom);
			
			// if everything goes OK, register entity instance in the MEBN
			IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(this.getDefaultMEBN(), entityInstance, iri);
		}
		
		try {
			Debug.println(this.getClass(), "The entity instance " + entityInstance.getName() + " is pointing to OWL individual " + iri);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#insertRandomVariableFinding(unbbayes.prs.mebn.RandomVariableFinding)
	 */
	public void insertRandomVariableFinding( RandomVariableFinding randomVariableFinding) {
		// initial assertion
		if (randomVariableFinding == null) {
			Debug.println(this.getClass(), "Attempted to add null as random variable finding");
			return;	// just ignore
		}
		
		// check if the number of arguments is compatible (this version does not support OWL properties representing n-ary relationships with n > 2 or n < 1)
		// TODO implement n-ary relationships
		if ((randomVariableFinding.getArguments() == null)
				|| (randomVariableFinding.getArguments().length > 2)
				|| (randomVariableFinding.getArguments().length < 1)) {
			throw new IllegalArgumentException("This version does not support findings with " + randomVariableFinding.getArguments().length + " argument(s) yet.");
		}
		
		// extract the IRI of the related OWL property
		IRI iri = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), randomVariableFinding.getNode());
		
		// verify if MEBN contains IRI definition of the OWL property related to this resident node
		if (iri == null) {
			throw new IllegalStateException(this.getDefaultMEBN() + " does not specify the OWL property's IRI for resident node " + randomVariableFinding);
		}
		
		// check if the currently managed ontology contains the extracted owl property's IRI 
		if (!this.getDefaultOWLReasoner().getRootOntology().containsObjectPropertyInSignature(iri, true) 
				&& !this.getDefaultOWLReasoner().getRootOntology().containsDataPropertyInSignature(iri, true) ) {
			throw new IllegalStateException("Ontology " + this.getDefaultOWLReasoner().getRootOntology() + " does not provide property " + iri);
		}
		
		
		// extract the data factory to obtain the owl property
		OWLDataFactory factory = this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().getOWLDataFactory();
		
		// this variable will hold the axioms to be added to ontology (this axiom will represent a particular owl triple "argument -> property -> value" )
		OWLAxiom axiom = null;
		
		// TODO check if we should also add negative axioms (because OWL has open-world assumption, but our SSBN algorithm assumes closed-world assumptions at few points)
//		OWLAxiom negativeAxiom = null;	// because OWL uses open-world assumption, we should sometimes add a negative axiom to make sure the opposite never happens
		
		
		// extract the IRI of the subject of OWL property (which is the 1st argument)
		IRI subjectIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), randomVariableFinding.getArguments()[0]);
		if (subjectIRI == null ) {
			throw new IllegalStateException("MEBN " + this.getDefaultMEBN() + " does not provide the IRI of object entity individual " + randomVariableFinding.getArguments()[0]);
		}
		if (!this.getDefaultOWLReasoner().getRootOntology().containsIndividualInSignature(subjectIRI, true)) {
			throw new IllegalStateException("Ontology " + this.getDefaultOWLReasoner().getRootOntology() + " does not provide individual " + subjectIRI);
		}
		
		// extract the actual owl individual that represents the subject (1st argument of resident node finding)
		OWLIndividual subject = factory.getOWLNamedIndividual(subjectIRI);
		if (subject == null) {
			// this is unlikely to happen, but some factory implementations may return null individuals...
			throw new IllegalStateException("Could not extract OWL individual with IRI " + subjectIRI);
		}
		
		// verify if we should use data property (boolean node with 1 argument) or object property (default)
		if(randomVariableFinding.getNode().getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) {
			
			// this finding may be a data property (if node has only 1 argument) or a non-functional property (if it represents relationships between 2 individuals)
			if (randomVariableFinding.getArguments().length <= 1) {
				
				// This is a data property relating an individual to a boolean value
				OWLDataProperty property = factory.getOWLDataProperty(iri);
				
				// create data property assertion to insert a particular boolean value as property range
				Boolean value = true; // assume that boolean findings are either true or false (there is no finding for absurd)
				if("false".equalsIgnoreCase(randomVariableFinding.getState().getName()) || randomVariableFinding.getState().getName().endsWith("false")){
					// actually, false findings are less likely to happen (that's why we are comparing to "false" instead of "true")
					value = false;
				} 
				
				axiom = factory.getOWLDataPropertyAssertionAxiom(property.asOWLDataProperty(), subject, value);
				
				
			} else { // there are 2 or more arguments: this is a non-functional relation between 2 individuals
				// extract object property
				OWLObjectProperty property = factory.getOWLObjectProperty(iri);
				
				// extract the object (range, or the second argument)
				// TODO implement findings with more than 2 arguments
				IRI objectIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), randomVariableFinding.getArguments()[1]);
				if (objectIRI == null ) {
					throw new IllegalStateException("MEBN " + this.getDefaultMEBN() + " does not provide the IRI of object entity individual " + randomVariableFinding.getArguments()[1]);
				}
				if (!this.getDefaultOWLReasoner().getRootOntology().containsIndividualInSignature(objectIRI, true)) {
					throw new IllegalStateException("Ontology " + this.getDefaultOWLReasoner().getRootOntology() + " does not provide individual " + objectIRI);
				}
				
				// extract the actual owl individual that represents the range (2nd argument of resident node finding)
				OWLIndividual object = factory.getOWLNamedIndividual(objectIRI);
				if (object == null) {
					// this is unlikely to happen, but some factory implementations may return null individuals...
					throw new IllegalStateException("Could not extract OWL individual with IRI " + objectIRI);
				}
				
				// create object property assertion to relate 2 individuals. 
				if(randomVariableFinding.getState().getName().equalsIgnoreCase("false")){
					// in this case, the axiom should be negative (it makes explicit that the relationship does not happen at all)
					axiom = factory.getOWLNegativeObjectPropertyAssertionAxiom(property.asOWLObjectProperty(), subject, object);
				} else {
					// in this case, the axiom should be positive (it makes explicit that the relationship happens - this case happens in most cases)
					axiom = factory.getOWLObjectPropertyAssertionAxiom(property.asOWLObjectProperty(), subject, object);
				}
			}
		} else if (randomVariableFinding.getArguments().length <= 1) { 
			// extract object property
			OWLObjectProperty property = factory.getOWLObjectProperty(iri);
			
			// This seems to be a random variable with "functional" format ( e.g. F(x) = y ) )
			Debug.println(this.getClass(), "Property " + property + " seems to be a function from " + subject + " to " + randomVariableFinding.getState());
			
			// extract the object (value)
			IRI objectIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), randomVariableFinding.getState());
			if (objectIRI == null ) {
				throw new IllegalStateException("MEBN " + this.getDefaultMEBN() + " does not provide the IRI of categorical entity " + randomVariableFinding.getState());
			}
			if (!this.getDefaultOWLReasoner().getRootOntology().containsIndividualInSignature(objectIRI, true)) {
				throw new IllegalStateException("Ontology " + this.getDefaultOWLReasoner().getRootOntology() + " does not provide individual " + objectIRI);
			}
			
			// extract the actual owl individual that represents the range (2nd argument of resident node finding)
			OWLIndividual object = factory.getOWLNamedIndividual(objectIRI);
			if (object == null) {
				// this is unlikely to happen, but some factory implementations may return null individuals...
				throw new IllegalStateException("Could not extract OWL individual with IRI " + objectIRI);
			}
			
			axiom = factory.getOWLObjectPropertyAssertionAxiom(property.asOWLObjectProperty(), subject, object);
			
			// Note: if property is really functional, then the negative axioms should be automatically inferred by reasoner
		} else {
			// this is a functional ternary relation
			// TODO implement ternary function
			throw new IllegalArgumentException("This version does not support ternary functions (a property mapping 2 individuals to 1 individual) like " + iri);
		}
		
		// add axioms and apply changes
		if (axiom != null) {
			this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().addAxiom(this.getDefaultOWLReasoner().getRootOntology(), axiom);
			try {
				Debug.println(this.getClass(), "Added finding " + randomVariableFinding + " to property " + iri + " and subject " + subject);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			Debug.println(this.getClass(), "No axiom could be generated from random variable finding " + randomVariableFinding);
		}

	}

	/**
	 * In this implementation, saving the ontology and saving the findings are the same operation. So,
	 * this method just calls {@link #saveFindings(MultiEntityBayesianNetwork, File)}
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#saveGenerativeMTheory(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)
	 * @see #saveFindings(MultiEntityBayesianNetwork, File)
	 */
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		this.saveFindings(mebn, file);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#saveFindings(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		try {
			Debug.println(this.getClass(), "Attempting to save knowledge base of " + mebn);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		if (file == null) {
			try {
				this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().saveOntology(this.getDefaultOWLReasoner().getRootOntology());
			} catch (OWLOntologyStorageException e) {
				throw new RuntimeException("Could not save current ontology.", e);
			}
		} else {
			// this code should only be called if this.supportsLocalFile(false) == true
			try {
				// warn programmer that this is an unexpected operation...
				System.err.println("WARNING: attempting to save ontology as different file " + file + ". This may cause synchronization problems between the deterministic and probabilistic parts of the ontology.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			try {
				this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().saveOntology(this.getDefaultOWLReasoner().getRootOntology(), IRI.create(file));
			} catch (OWLOntologyStorageException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * This method reoads and synchronizes the reasoner instead of doing the actual loading of OWL ontologies.
	 * This is in order to provide the unforeseen feature of synchronizing the reasoner using a button for loading KB from file
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#loadModule(java.io.File, boolean)
	 */
	public void loadModule(File file, boolean findingModule) throws UBIOException {
		// ignore findingModule because the interface does not specify what it is...
		
		// ignore file
		
		// TODO maybe we could call the I/O classes to actually reload the probabilistic part from the currently edited ontology (in order to provide a mean to edit the probabilistic part using the deterministic editor as well)
		
		// reload reasoner
		this.getOWLModelManager().getOWLReasonerManager().classifyAsynchronously(this.getOWLModelManager().getReasonerPreferences().getPrecomputedInferences());
		
		// maybe there would be some synchronization problems, because of protege's asynchronous initialization of reasoners. Let's wait until it becomes ready
		for (long i = 0; i < this.getMaximumBuzyWaitingCount(); i++) {
			// TODO Stop using buzy waiting!!!
			if (ReasonerStatus.NO_REASONER_FACTORY_CHOSEN.equals(this.getOWLModelManager().getOWLReasonerManager().getReasonerStatus())) {
				// reasoner is not chosen...
				Debug.println(this.getClass(), "No reasoner is chosen.");
				break;
			}
			if (ReasonerStatus.INITIALIZED.equals(this.getOWLModelManager().getOWLReasonerManager().getReasonerStatus())) {
				// reasoner is ready now
				break;
			}
			Debug.println(this.getClass(), "Waiting for reasoner initialization...");
			try {
				// sleep and try reasoner status after
				Thread.sleep(this.getSleepTimeWaitingReasonerInitialization());
			} catch (Throwable t) {
				// a thread sleep should not break normal program flow...
				t.printStackTrace();
			}
		}
		
		// TODO reload Object entities to MEBN
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#existEntity(java.lang.String)
	 */
	public boolean existEntity(String name) {
		// looks like the SSBN algorithm is not using this method at all...
		try {
			// let's try using the simplest way (delegate entirely to protege)
			return !((ProtegeStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLEditorKit().getOWLModelManager().getOWLEntityFinder().getMatchingOWLEntities(name).isEmpty();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// let's try the IRI cache (the IRIs stored in mebn)
		try {
			// TODO stop using exhaustive search (well.. at least this is linear...)
			for (IRI iri : ((IRIAwareMultiEntityBayesianNetwork)this.getDefaultMEBN()).getIriMap().values()) {
				try {
					String iriString = iri.toString();
					if (iriString.substring(iriString.lastIndexOf('#') + 1).equals(name)) {	// should we ignore case?
						// because the string after # is the name of the OWL object, then if it matches we found it.
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// nothing found
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#searchFinding(unbbayes.prs.mebn.ResidentNode, java.util.Collection)
	 */
	public StateLink searchFinding(ResidentNode resident, Collection<OVInstance> listArguments) {
		
		// initial assertions
		if (resident == null) {
			// it is impossible to search a finding if no variable is provided
			Debug.println(this.getClass(), "Attempted to search finding for null resident node.");
			return null;
		}
		if (resident.getArgumentList() == null || listArguments == null) {
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with null arguments. Resident node = " + resident);
		}
		if (resident.getArgumentList().size() != listArguments.size()) {
			throw new IllegalArgumentException("Findings of " + resident + " should have " + resident.getArgumentList().size() + " arguments.");
		}
		
		// from now on, resident.getArgumentList().size() == listArguments.size()
		if (listArguments.size() <= 0) {
			// This version cannot represent resident nodes with no arguments
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with 0 arguments. Resident node = " + resident);
		}
		if (listArguments.size() > 2) {
			// This version cannot handle findings for resident nodes with more than 2 arguments
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes repesenting n-ary relationships with n > 2. Resident node = " + resident);
		}
		
		// extract reasoner and ontology
		OWLReasoner reasoner = this.getDefaultOWLReasoner();
		if (reasoner == null) {
			throw new IllegalStateException("No OWL reasoner provided.");
		}
		OWLOntology ontology = reasoner.getRootOntology();
		
		// extract IRI of the property pointed by the resident node
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), resident);
		if (propertyIRI == null) {
			throw new IllegalStateException(this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + resident);
		}
		if (!ontology.containsObjectPropertyInSignature(propertyIRI, true)
				&& !ontology.containsDataPropertyInSignature(propertyIRI, true)) {
			throw new IllegalStateException("Ontology " + ontology + " does not contain OWL property " + propertyIRI);
		}
		
		// extract the factory to obtain the owl property
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// by now, the resident node has either 1 or 2 arguments
		if (listArguments.size() <= 1) {
			// this is either a functional format (e.g. F(x) = y) or a boolean data property
			
			if (resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) {
				
				OWLNamedIndividual subject = null; // This variable holds the subject (the individual of the 1st argument)
				
				// Holy shit! The SSBN algorithm is discarding instances of ObjectEntityInstance and translating it to OVInstances with no references to the original!! 
				// So we lost the references to the original IRI!!
				// TODO fix the way SSBN algorithm is implemented, so that the OrdinaryVariableInstance is not discarded when OVInstance is created (e.g. use wrapper/adapter).
				// Note: queries by names can also be done, but in this case we have to perform 2 queries (i.e. test if true and test if false - because of open-world assumption)
				try {
					// look for the object entity instance
					ObjectEntityInstance entityInstance = this.getDefaultMEBN().getObjectEntityContainer().getEntityInstanceByName(listArguments.iterator().next().getEntity().getInstanceName());
					IRI subjectIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), entityInstance);
					// TODO should we check if subject exists?
					subject = factory.getOWLNamedIndividual(subjectIRI);
				} catch (Exception e) {
					throw new IllegalStateException("Could not extract the subject of property " + propertyIRI + " from MEBN " + this.getDefaultMEBN(), e);
				}
				
				if (subject == null) {
					throw new IllegalStateException("Could not extract the subject of property " + propertyIRI + " from MEBN " + this.getDefaultMEBN());
				}
				
				// This is a property with 1 argument (subject) to a boolean value. Let's use data property to look for its value
				OWLDataProperty property = factory.getOWLDataProperty(propertyIRI);
				
				// search values using reasoner
				Set<OWLLiteral> booleanLiterals = reasoner.getDataPropertyValues(subject, property);
				if (booleanLiterals != null) {
					for (OWLLiteral owlLiteral : booleanLiterals) {
						try {
							// use only the first boolean value found
							// TODO what should we do if there are 2 or more values and OWL reasoner says it is OK?
							if (owlLiteral.isBoolean()) {
								StateLink link = resident.getPossibleValueByName(String.valueOf(owlLiteral.parseBoolean())); 
								if (link != null) {
									try {
										Debug.println(this.getClass(), "The value of  " + resident + " is " + link);
									} catch (Throwable t) {
										t.printStackTrace();
									}
									return link;
								}
							}
						} catch (Exception e) {
							Debug.println(this.getClass(), "Failed to convert value " + owlLiteral + " to a StateLink for resident node " + resident, e);
							continue;
						}
						Debug.println(this.getClass(), "Failed to convert value " + owlLiteral + " to a StateLink for resident node " + resident);
					}
				}
			} else {
				
				// This one has "functional" format (F(x) = y). Let's extract the property (the function "F" in F(x) = y).
				OWLObjectProperty property = factory.getOWLObjectProperty(propertyIRI);
				
				// create an expression that queries the value of "y" in F(x) = y (note: F is the property and x is the 1st argument)
				// this is done by just asking G(x) where G is the inverse of F
				String expression = "inverse " + this.extractName(property) + " value " + listArguments.iterator().next().getEntity().getInstanceName();
				Debug.println(this.getClass(), "Expression: " + expression);
				
				NodeSet<OWLNamedIndividual> nodeSet = reasoner.getInstances(this.parseExpression(expression), false);
				if (nodeSet != null) {
					for (OWLNamedIndividual individual: nodeSet.getFlattened()) {
						try {
							// use only the first valid individual. Multiple-valued properties should be resolved in another method
							StateLink link = resident.getPossibleValueByName(this.extractName(individual));
							if (link != null) {
								try {
									Debug.println(this.getClass(), "The value of  " + resident + " is " + link);
								} catch (Throwable t) {
									t.printStackTrace();
								}
								return link;
							}
						} catch (Exception e) {
							Debug.println(this.getClass(), "Failed to convert value " + individual + " to a StateLink for resident node " + resident, e);
							continue;
						}
						Debug.println(this.getClass(), "Failed to convert value " + individual + " to a StateLink for resident node " + resident);
					}
				}
			}
		} else {
			// 2 arguments. This is either a simple binary relationship or a ternary relationship (the last one is invalid)
			
			if (resident.getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES) {
				// ternary not supported
				try {
					System.err.println("This knowledge base cannot handle resident nodes repesenting n-ary relationships with n = 3. Resident node = " + resident); 
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			
			// this is binary relationship, so let's use owl object property
			OWLObjectProperty property = factory.getOWLObjectProperty(propertyIRI);
			
			// extract the name of subject and object (i.e. the first and the second arguments respectively)
			String subjectName = null;
			String objectName = null;
			if (listArguments.size() > 1) {
				Iterator<OVInstance> it = listArguments.iterator();
				// this should not be dangerous, because if we reach this code, the number of arguments must be exactly 2
				subjectName = it.next().getEntity().getInstanceName();
				objectName = it.next().getEntity().getInstanceName();
			}
			
			// create an expression that returns the subject if the subject has a link to object. Empty otherwise
			String expressionToParse = "{" +  subjectName + "}" + " that " + this.extractName(property) + " value " + objectName ;
			Debug.println(this.getClass(), "Expression: " + expressionToParse);
			
			// because we are in open-world assumption, we must check if individuals "are" related, "never" related or "unknown"
			if (!reasoner.getInstances(this.parseExpression(expressionToParse), false).isEmpty()) {
				// they are related
				StateLink link = resident.getPossibleValueByName("true"); 
				if (link != null) {
					try {
						Debug.println(this.getClass(), "The value of  " + resident + " is " + link);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return link;
				} else {
					// TODO ask PR-OWL2 creator if it is OK if OWL says it is true, but PR-OWL says it does not handle such value
					System.err.println("Reasoner says that " + resident + " is true, but this resident node cannot handle such value...");
				}
			} 

			// perform the second query to check if they are "never" related
			expressionToParse = "{" +  subjectName + "}" + " that not ( " + this.extractName(property) + " value " + objectName + " )" ;
			Debug.println(this.getClass(), "Expression: " + expressionToParse);
			
			if (!reasoner.getInstances(this.parseExpression(expressionToParse), false).isEmpty()) {
				// they are NEVER related
				StateLink link = resident.getPossibleValueByName("false"); 
				if (link != null) {
					try {
						Debug.println(this.getClass(), "The value of  " + resident + " is " + link);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return link;
				} else {
					// TODO ask PR-OWL2 creator if it is OK if OWL says it is true, but PR-OWL says it does not handle such value
					System.err.println("Reasoner says that " + resident + " is false, but this resident node cannot handle such value...");
				}
			} 
			
			// ok, reasoner cannot say if it is either true or false
		}
		
		// nothing found
		try {
			Debug.println(this.getClass(), "No value for  " + resident + " found.");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * This method seems to be used when individuals of entities are queried.
	 * So, the name in the interface is not very intuitive...
	 * @param type : this is the name of the class concatenated to "_label"
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getEntityByType(java.lang.String)
	 * @deprecated the name of this method is likely to change in the near future
	 */
	public List<String> getEntityByType(String type) {
		Set<String> ret = new HashSet<String>();
		
		if (TypeContainer.typeBoolean.getName().equalsIgnoreCase(type)) {
			
			// if the required type is Boolean, return boolean values (which are known)
			ret.add("true");
			ret.add("false");
			ret.add("absurd");
			// assume we have all and only these 3 values as boolean values in the ontology
		} else if (TypeContainer.typeLabel.getName().equalsIgnoreCase(type)) {
			
			// if the required type is TypeLabel, return all Object entities and non-PR-OWL classes (note that we are interested now in classes, not individuals)
			Debug.println(this.getClass(), "getEntityByType was called for typeLabel...");
			
			// TODO is this OK? It is strange that this method should return names of classes instead of individuals...
			
			// Extracting object entities
			for (ObjectEntity objEntity : this.getDefaultMEBN().getObjectEntityContainer().getListEntity()) {
				ret.add(objEntity.getName());
			}
			
			// Extracting non-PR-OWL classes 
			for (OWLClassExpression nonPROWLClass : this.getNonPROWLClassExtractor().getNonPROWLClasses(this.getDefaultOWLReasoner().getRootOntology())) {
				ret.add(this.extractName(nonPROWLClass));
			}
		} else {
			
			// the values (individuals) are unknown unless we perform a query to reasoner, so let's obtain the reasoner
			OWLReasoner reasoner = this.getDefaultOWLReasoner();
			if (reasoner == null) {
				throw new IllegalStateException("No reasoner found");
			}
			
			// build the expression for reasoner
			String expression = null;
			if (TypeContainer.typeCategoryLabel.getName().equalsIgnoreCase(type)) {
				
				// the required type is CategoricalRVState. Build expression to extract individuals of CategoricalRVState and non-PR-OWL classes
				expression = PROWLModelUser.CATEGORICAL_STATE;
				
				// Obtain non-PR-OWL classes so that we can create an expression using all of them
				for (OWLClassExpression nonPROWLClass : this.getNonPROWLClassExtractor().getNonPROWLClasses(reasoner.getRootOntology())) {
					expression += " or " + this.extractName(nonPROWLClass);
				}
				
			} else {
				// find where is the suffix _label
				int indexOfLabel = type.lastIndexOf("_label");
				if (indexOfLabel < 0 || indexOfLabel >= type.length()) {
					Debug.println(this.getClass(), "Could not detect the required suffix \"_label\" from " + type);
					expression  = type;	// use type as default...
				} else {
					// remove _label from type name (that should result in a class name)
					expression = type.substring(0, indexOfLabel);
				}
			}
			
			Debug.println(this.getClass(),"Expression: " + expression);
			
			// do the query if expression exists
			if (expression != null && expression.trim().length() > 0) {
				for (OWLNamedIndividual individual : reasoner.getInstances(this.parseExpression(expression), false).getFlattened()) {
					ret.add(this.extractName(individual));	// add the name of instance to ret
				}
			}
		}
			
		try {
			Debug.println(this.getClass(), "Extracted entity individuals from " + type + ": " + ret);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return new ArrayList<String>(ret);
	}

	/**
	 * this method adds random variable findings to resident node (thus, "resident" is both Input and Output argument).
	 *  Actually, this method seems to be insignificant now, 
	 *  because the findings can be added directly to the deterministic ontology using protege panel.
	 *  ...But the SSBN algorithm seems to use informations obtained from this method instead of querying the knowledge base, 
	 *  so this method must be implemented...
	 *  
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#fillFindings(unbbayes.prs.mebn.ResidentNode)
	 * @deprecated : finding should be queried instead of statically filled in a collection inside the resident node.
	 * @param resident : {@link unbbayes.prs.mebn.ResidentNode#getRandomVariableFindingList()} will be filled.
	 */
	public void fillFindings(ResidentNode resident) {
		// TODO stop using in-memory findings and start querying the knowledge base each time we need (because the in-memory findings is deprecated)
		
		// assertions
		if (resident == null) {
			Debug.println(this.getClass(), "Invalid attempt to call fillFindings to null.");
			return;
		}
		if (resident.getArgumentList() == null || resident.getArgumentList().size() <= 0) {
			Debug.println(this.getClass(), "Invalid attempt to search findings for node " + resident +  " with no arguments.");
			return;	// this knowledge base has no support for such findings, but that does not mean it should throw an exception...
		}
		if (resident.getArgumentList().size() > 2) {
			Debug.println(this.getClass(), "Invalid attempt to search findings for node " + resident +  " with " + resident.getArgumentList().size() + " arguments.");
			return;	// this knowledge base has no support for such findings, but that does not mean it should throw an exception...
		}
		
		// extract reasoner
		OWLReasoner reasoner = this.getDefaultOWLReasoner();
		if (reasoner == null) {
			throw new IllegalArgumentException("No reasoner found.");
		}
		
		// extract the IRI of the owl property related to the resident node
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), resident);
		if (propertyIRI == null 
				|| ( !reasoner.getRootOntology().containsObjectPropertyInSignature(propertyIRI) 
						&& !reasoner.getRootOntology().containsDataPropertyInSignature(propertyIRI) )) {
			// there would be no finding for this resident node, because the property is not mapped to a resident node or the property does not exist at all
			try {
				Debug.println(this.getClass(), this.getDefaultMEBN() + " contains an invalid link from node " + resident + " to OWL property " + propertyIRI);
			}catch (Throwable t) {
				t.printStackTrace();
			}
			return;	
		}
		
		// extract the factory to obtain property and other OWL objects
		OWLDataFactory factory = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
		
		// only 3 options available: boolean node with 1 argument, categorical node with 1 argument, and boolean node with 2 arguments.
		// TODO add support for n-ary relationships
		if (resident.getArgumentList().size() == 1) {
			// boolean node with 1 argument or categorical node with 1 argument
			if (resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) {
				
				// boolean node with 1 argument: use (boolean) datatype property
				OWLDataProperty property = factory.getOWLDataProperty(propertyIRI);
				
				// create an expression that returns all individuals using that property as true
				// this explicit value check is necessary because OWLReasoner#getDataPropertyValues returns less elements that the query using expression
				// (e.g. if class has an restriction saying property value true, it is caught in expression query, but not caught in  OWLReasoner#getDataPropertyValues)
				String expression = this.extractName(property) + " value true";
				
				// execute query to obtain all individuals using that property as true
				for (OWLNamedIndividual individual : reasoner.getInstances(this.parseExpression(expression), false).getFlattened()) {
					try {
						// generate arguments (it should have only 1 argument if code reaches this point)...
						ObjectEntityInstance argument = resident.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityInstanceByName(this.extractName(individual));
						// generate finding
						RandomVariableFinding finding = new RandomVariableFinding(
								resident, 
								Collections.singletonList(argument).toArray(new ObjectEntityInstance[1]), 
								resident.getPossibleValueByName("true").getState(), 
								this.getDefaultMEBN()
						);
						// add finding
						resident.addRandomVariableFinding(finding);
					} catch (Exception e) {
						e.printStackTrace();
						// keep going on
						System.err.println("Error in " + individual + " -> " + property + " -> xsd:boolean");
						System.err.println("But the system will keep loading other findings.");
					}
				}
				
				// create an expression that returns all individuals using that property as false
				// as once stated, this approach returns more elements than OWLReasoner#getDataPropertyValues
				expression = this.extractName(property) + " value false";
				
				// execute query to obtain all individuals using that property as false
				for (OWLNamedIndividual individual : reasoner.getInstances(this.parseExpression(expression), false).getFlattened()) {
					try {
						// generate arguments (it should have only 1 argument if code reaches this point)...
						ObjectEntityInstance argument = resident.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityInstanceByName(this.extractName(individual));
						// generate finding
						RandomVariableFinding finding = new RandomVariableFinding(
								resident, 
								Collections.singletonList(argument).toArray(new ObjectEntityInstance[1]), 
								resident.getPossibleValueByName("false").getState(), 
								this.getDefaultMEBN()
						);
						// add finding
						resident.addRandomVariableFinding(finding);
					} catch (Exception e) {
						e.printStackTrace();
						// keep going on
						System.err.println("Error in " + individual + " -> " + property + " -> xsd:boolean");
						System.err.println("But the system will keep loading other findings.");
					}
				}
				
				
			} else {
				// this property has a function-like format (i.e. F(x) = y)
				OWLObjectProperty property = factory.getOWLObjectProperty(propertyIRI);
				
				// create an expression that returns all individuals using that property
				String expression = this.extractName(property) + " some Thing";
				
				// execute query to obtain all individuals using that property
				for (OWLNamedIndividual subject : reasoner.getInstances(this.parseExpression(expression), false).getFlattened()) {
					try {
						
						// generate argument (it should have only 1 argument if code reaches this point)...
						ObjectEntityInstance argument = resident.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityInstanceByName(this.extractName(subject));
						
						// for each individual, extract the associated value and create resident node's finding
						for (OWLNamedIndividual object : reasoner.getObjectPropertyValues(subject, property).getFlattened()) {
							try {
								// generate finding
								RandomVariableFinding finding = new RandomVariableFinding(
										resident, 
										Collections.singletonList(argument).toArray(new ObjectEntityInstance[1]), 
										resident.getPossibleValueByName(this.extractName(object)).getState(), 
										this.getDefaultMEBN()
								);
								// add finding
								resident.addRandomVariableFinding(finding);
							} catch (Exception e) {
								e.printStackTrace();
								// keep going on
								System.err.println("Error in " + subject + " -> " + property + " -> " + object);
								System.err.println("But the system will keep loading other findings.");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						// keep going on
						System.err.println("Error in " + subject + " -> " + property +  " -> owl:Thing");
						System.err.println("But the system will keep loading other findings.");
					}
				}
			}
		} else if (resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) {
			// boolean node with 2 arguments: this is representation of a arbitrary relation between two individuals
			OWLObjectProperty property = factory.getOWLObjectProperty(propertyIRI);
			
			// create an expression that returns all individuals using that property
			String expression = this.extractName(property) + " some Thing";
			
			// execute query to obtain all positive individuals using that property
			for (OWLNamedIndividual subject : reasoner.getInstances(this.parseExpression(expression), false).getFlattened()) {
				try {
					
					// generate 1st argument 
					HashSet<ObjectEntityInstance> arguments = new HashSet<ObjectEntityInstance>();
					ObjectEntityInstance argument1 = resident.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityInstanceByName(this.extractName(subject));
					arguments.add(argument1);
					
					// for each individual, extract the associated value and create resident node's finding
					for (OWLNamedIndividual object : reasoner.getObjectPropertyValues(subject, property).getFlattened()) {
						try {
							// generate argument 2
							ObjectEntityInstance argument2 = resident.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityInstanceByName(this.extractName(object));
							arguments.add(argument2);
							
							// generate finding
							RandomVariableFinding finding = new RandomVariableFinding(
									resident, 
									arguments.toArray(new ObjectEntityInstance[2]), 
									resident.getPossibleValueByName("true").getState(), 
									this.getDefaultMEBN()
							);
							// add finding
							resident.addRandomVariableFinding(finding);
						} catch (Exception e) {
							e.printStackTrace();
							// keep going on
							System.err.println("Error in " + subject + " -> " + property + " -> " + object);
							System.err.println("But the system will keep loading other findings.");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					// keep going on
					System.err.println("Error in " + subject + " -> " + property +  " -> owl:Thing");
					System.err.println("But the system will keep loading other findings.");
				}
			}
			
			// TODO find out an effective way to obtain negative findings
			System.err.println("WARNING: negative finding not supported yet - assertions that " + property + " will NOT happen is NOT being loaded...");
		} else {
			Debug.println(this.getClass(), resident + " has a format that is not supported by this knowledge base.");
		}

	}
	
	/**
	 * This method returns false, because OWL knowledge base and the MEBN project itself should be stored in the
	 * same file (so, we'd better stop allowing user to save the knowledge base as a separate file).
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#supportsLocalFile(boolean)
	 * @return false.
	 */
	public boolean supportsLocalFile(boolean isLoad) {
//		if (isLoad) {
//			 this code was migrated to this.loadModule(file, findingModule)
//			this.getOWLModelManager().getOWLReasonerManager().classifyAsynchronously(this.getOWLModelManager().getReasonerPreferences().getPrecomputedInferences());
//			// maybe there would be some synchronization problems, because of protege's asynchronous initialization of reasoners. Let's wait until it becomes ready
//			for (long i = 0; i < this.getMaximumBuzyWaitingCount(); i++) {
//				// TODO Stop using buzy waiting!!!
//				if (ReasonerStatus.INITIALIZED.equals(this.getOWLModelManager().getOWLReasonerManager().getReasonerStatus())) {
//					// reasoner is ready now
//					break;
//				}
//				Debug.println(this.getClass(), "Waiting for reasoner initialization...");
//				try {
//					// sleep and try reasoner status after
//					Thread.sleep(this.getSleepTimeWaitingReasonerInitialization());
//				} catch (Throwable t) {
//					// a thread sleep should not break normal program flow...
//					t.printStackTrace();
//				}
//			}
//		} else {
//			 this code was migrated to saveGenerativeMTheory
//			try {
//				this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().saveOntology(this.getDefaultOWLReasoner().getRootOntology());
//			} catch (OWLOntologyStorageException e) {
//				throw new RuntimeException("Could not save current ontology.", e);
//			}
//		}
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getSupportedLocalFileExtension(boolean)
	 */
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getSupportedLocalFileDescription(boolean)
	 */
	public String getSupportedLocalFileDescription(boolean isLoad) {
		return null;
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSingleSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public List<String> evaluateSingleSearchContextNodeFormula(ContextNode context, List<OVInstance> ovInstances)throws OVInstanceFaultException {
		
		// delegate query to another method
		SearchResult result = this.evaluateSearchContextNodeFormula(context, ovInstances);
		
		// prepare list to return
		List<String> ret = new ArrayList<String>();
		if (result == null) {
			return ret;
		}
		
		// fill list
		for (String[] values : result.getValuesResultList()) {
			if (values == null) {
				continue;
			}
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					ret.add(values[i]);
				}
			}
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,List<OVInstance> ovInstances) {
		// initial assertions
		if (context == null) {
			return null;
		}
		if (ovInstances == null) {
			// use empty list instead of null value to represent situations where no known instances are available
			ovInstances = new ArrayList<OVInstance>();
		}
		
		// extract what ordinary variables we should search (i.e. they do not have values specified in ovInstances)
		OrdinaryVariable missingOV[] = context.getOVFaultForOVInstanceSet(ovInstances).toArray(
		                                		   new OrdinaryVariable[context.getOVFaultForOVInstanceSet(ovInstances).size()]); 
		
		//The search isn't necessary if there is no variable to search. 
		if(missingOV.length == 0){
			return null; 
		}
		
		// extract context node expression
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		// solve context node expression if it can be solved
		SearchResult ret = new SearchResult(missingOV);
		// solve formula tree normally (the last "true" means that we want to evaluate "formulaTree" instead of "not formulaTree")
		if (!this.solveFormulaTree(formulaTree, ovInstances, ret, true)) {	
			// this kind of expression cannot be solved by this version of knowledge base...
			// TODO assure this else clause is never called by implementing solveFormulaTree completely...
			
			try {
				System.err.println(context + " cannot be solved by this knowledge base if the unknown variables are " + missingOV + ". We'll retrieve all possible values of the missing variables by their types...");
			} catch (Throwable e) {
				e.printStackTrace();
			}
			ret = new SearchResult(missingOV);
			
			// extract all individuals for that OV, by type (i.e. the evaluation of the context node is delegated to the upper algorithm, and this method will only extract OVs by type)
			for (int i = 0; i < missingOV.length; i++) {
				return null;
//				try {
//					List<String> entityIndividuals = this.getEntityByType(missingOV[i].getValueType().getName());
//					if (entityIndividuals != null && !entityIndividuals.isEmpty()) {
//						// ret will hold all entity individuals of that type
//						ret.addResult(entityIndividuals.toArray(new String[entityIndividuals.size()]));
//					}
//				} catch (Exception e) {
//					try {
//						Debug.println(this.getClass(), "Could not extract all possible values of OV " + missingOV[i], e);
//					}catch (Throwable t) {
//						t.printStackTrace();
//					}
//				}
			}
		}
		
		// only return ret if it is valid
		if (ret == null || ret.getValuesResultList() == null || ret.getValuesResultList().isEmpty()) {
			return null;
		} else {
			return ret;
		}
	}

	/**
	 * This is a recursive method that evaluates formulaTree and fills knownSearchResults. 
	 * @param formulaTree : the expression to evaluate
	 * @param knownValues : these are known values of ordinary variables.
	 * @param knownSearchResults : an Input/Output argument that holds the evaluated values.
	 * @param isToSolveAsPositiveOperation : if set to false, then "not(formulaTree)" will be evaluated.
	 * @return false if this kind of expression cannot be solved by this knowledge base. True otherwise.
	 */
	protected boolean solveFormulaTree(NodeFormulaTree formulaTree, Collection<OVInstance> knownValues, SearchResult knownSearchResults, boolean isToSolveAsPositiveOperation) {
		// initial assertion
		if (formulaTree == null) {
			// we cannot evaluate a null formula...
			return false;
		}
		try {
			
			// convert the array of missing ordinary variables to a list
			List<OrdinaryVariable> missingOV = new ArrayList<OrdinaryVariable>();
			if (knownSearchResults.getOrdinaryVariableSequence() != null) {
				for (int i = 0; i < knownSearchResults.getOrdinaryVariableSequence().length; i++) {
					missingOV.add(knownSearchResults.getOrdinaryVariableSequence()[i]);
				}
			}
			
			// this version can only solve the following types of formulas:
			// 1. ov1 = ov2
			// 2. not (ov1 = ov2)
			// 3. booleanNode(<1 or 2 arguments>)
			// 4. not booleanNode(<1 or 2 arguments>)
			// 5. ov = nonBooleanNode(<1 argument>)
			// 6. nonBooleanNode(<1 argument>) = ov
			// 7. not (ov = nonBooleanNode(<1 argument>))
			// 8. not (nonBooleanNode(<1 argument>) = ov)
			// TODO implement other types of formulas.
			
			// thus, the top level operand/operator must be equalsTo, not or a node
			if (!(formulaTree.getNodeVariable() instanceof ResidentNode)
					&& !(formulaTree.getNodeVariable() instanceof BuiltInRVEqualTo)
					&& !(formulaTree.getNodeVariable() instanceof BuiltInRVNot)) {
				return false;
			}
			
			// now, we check if it is one of the 8 possible cases...
			
			// 1. ov1 = ov2
			try {
				if (formulaTree.getNodeVariable() instanceof BuiltInRVEqualTo
						&& formulaTree.getChildren().get(0).getSubTypeNode().equals(EnumSubType.OVARIABLE)
						&& formulaTree.getChildren().get(1).getSubTypeNode().equals(EnumSubType.OVARIABLE)) {
					// TODO fill knownSearchResults
					
					// check which ov is unknown
					if (missingOV.contains(formulaTree.getChildren().get(0).getNodeVariable())){
						
					}
					
					return true;
				}
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), "Could not check arguments of " + formulaTree, e);
				} catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
			
			// 3. booleanNode(<1 or 2 arguments>)
			try {
				if (formulaTree.getNodeVariable() instanceof ResidentNode		// this is a (resident) node
						&& (((ResidentNode)formulaTree.getNodeVariable()).getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES)	// this is a boolean node
						&& (((ResidentNode)formulaTree.getNodeVariable()).getArgumentList().size() <= 2)) {		// number of aguments
					// TODO fill knownSearchResults
					return true;
				}
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), "Could not check arguments of " + formulaTree, e);
				} catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
			
			// 5. ov = nonBooleanNode(<1 argument>)
			try {
				if ((formulaTree.getNodeVariable() instanceof BuiltInRVEqualTo)	// =
						&& formulaTree.getChildren().get(0).getSubTypeNode().equals(EnumSubType.OVARIABLE)	// ov
						&& (formulaTree.getChildren().get(1).getNodeVariable() instanceof ResidentNodePointer )	// nonBooleanNode
						&& (((ResidentNodePointer)formulaTree.getChildren().get(1).getNodeVariable()).getResidentNode().getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES)	// "nonBooleanNode" is not boolean node
						&& (((ResidentNodePointer)formulaTree.getChildren().get(1).getNodeVariable()).getResidentNode().getArgumentList().size() == 1) ) {	// <1 argument>		
					
					// extract the ordinary variable ov
					OrdinaryVariable ov = (OrdinaryVariable)formulaTree.getChildren().get(0).getNodeVariable();
					
					// extract the ordinary variable of the argument (only 1) of nonBooleanNode
					OrdinaryVariable argumentOV = ((ResidentNodePointer)formulaTree.getChildren().get(1).getNodeVariable()).getResidentNode().getArgumentList().get(0).getOVariable();
					
					// extract the object entity related to the type of ov (this is necessary because we cannot directly navigate from OV's type to an Entity...)
					Entity ovEntity = this.getDefaultMEBN().getObjectEntityContainer().getObjectEntityByType(ov.getValueType());
					
					// extract the object entity related to the type of argumentOV (this is necessary because we cannot directly navigate from OV's type to an Entity...)
					Entity argumentEntity = this.getDefaultMEBN().getObjectEntityContainer().getObjectEntityByType(argumentOV.getValueType());
					
					// extract reasoner
					OWLReasoner reasoner = this.getDefaultOWLReasoner();
					
					// extract IRI of the object property related to nonBooleanNode
					IRI nonBooleanNodeIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), ((ResidentNodePointer)formulaTree.getChildren().get(1).getNodeVariable()).getResidentNode());
					if (!reasoner.getRootOntology().containsObjectPropertyInSignature(nonBooleanNodeIRI)) {
						// this reasoner can only solve findings that has a reference to "definesUncertaintyOf"
						return false;
					}
					
					// extract the object property
					OWLObjectProperty nonBooleanNodeProperty = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(nonBooleanNodeIRI);
					
					// expression of the query
					String expression = "";
					
					// extract the possible values of OV if it is unknown
					Collection<OWLNamedIndividual> individualsOfOV = new HashSet<OWLNamedIndividual>();
					if (missingOV.contains(ov)) {
						// extract the possible values of ov
						expression += ovEntity.getName() + " that (inverse " + this.extractName(nonBooleanNodeProperty) + " some ";
						if (missingOV.contains(argumentOV)) {
							// the argument is unknown, so we use the entire OWL class
							expression += argumentEntity;
						} else {
							// use enumerated class of the knownValues
							expression += "{ ";
							Iterator<OVInstance> it = knownValues.iterator();
							for (OVInstance knownValue : knownValues) {
								if (knownValue.getOv().equals(argumentOV)) {
									expression += knownValue.getEntity().getInstanceName() + ",";
								}
							}
							// remove the last comma
							if (expression.endsWith(",")) {
								expression = expression.substring(0, expression.lastIndexOf(','));
							}
							expression += " } )";
						}
						
						individualsOfOV = reasoner.getInstances(this.parseExpression(expression), false).getFlattened();
					} 
					
					// extract the possible values of argumentOV if it is unknown
					Collection<OWLNamedIndividual> individualsOfArgumentOV = new HashSet<OWLNamedIndividual>();
					if (missingOV.contains(argumentOV)) {
						// extract the possible values of argumentOV
						expression += argumentEntity.getName() + " that ( " + this.extractName(nonBooleanNodeProperty) + " some ";
						if (missingOV.contains(ov)) {
							// the ov is unknown, so we use the entire OWL class
							expression += ovEntity;
						} else {
							// use enumerated class of the knownValues
							expression += "{ ";
							Iterator<OVInstance> it = knownValues.iterator();
							for (OVInstance knownValue : knownValues) {
								if (knownValue.getOv().equals(ov)) {
									expression += knownValue.getEntity().getInstanceName() + ",";
								}
							}
							// remove the last comma
							if (expression.endsWith(",")) {
								expression = expression.substring(0, expression.lastIndexOf(','));
							}
							expression += " } )";
						}
						
						individualsOfArgumentOV = reasoner.getInstances(this.parseExpression(expression), false).getFlattened();
					}
					
					// fill knownSearchResults

					// add the query results of ov's possible values to knownSearchResults if the query has returned something
					if (individualsOfOV != null && !individualsOfOV.isEmpty()) {
						
						// extract the names of the returned individuals
						List<String> values = new ArrayList<String>();
						for (OWLNamedIndividual individualOfOV : individualsOfOV) {
							values.add(this.extractName(individualOfOV));
						}
						
						// obtain the index in knownSearchResults where we should add the extracted names
						int indexOfNewValue = Arrays.asList(knownSearchResults.getOrdinaryVariableSequence()).indexOf(ov);
						
						// update knownSearchResults if the index is valid
						if (indexOfNewValue >= 0) {
							
							// make sure knownSearchResults will not throw ArrayIndexOutOfBoundException because knownSearchResults.getValuesResultList() was not initialized
							while (knownSearchResults.getValuesResultList().size() <= indexOfNewValue) {
								knownSearchResults.getValuesResultList().add(new String[0]); // fill empty values
							}
							
							knownSearchResults.getValuesResultList().set(indexOfNewValue, values.toArray(new String[values.size()]));
						}
					}
					
					// add the query results of argumentOV's possible values to knownSearchResults if the query has returned something
					if (individualsOfArgumentOV != null && !individualsOfArgumentOV.isEmpty()) {
						
						// extract the names of the returned individuals
						List<String> values = new ArrayList<String>();
						for (OWLNamedIndividual individualOfOV : individualsOfArgumentOV) {
							values.add(this.extractName(individualOfOV));
						}
						
						// obtain the index in knownSearchResults where we should add the extracted names
						int indexOfNewValue = Arrays.asList(knownSearchResults.getOrdinaryVariableSequence()).indexOf(argumentOV);
						
						// update knownSearchResults if the index is valid
						if (indexOfNewValue >= 0) {
							
							// make sure knownSearchResults will not throw ArrayIndexOutOfBoundException because knownSearchResults.getValuesResultList() was not initialized
							while (knownSearchResults.getValuesResultList().size() <= indexOfNewValue) {
								knownSearchResults.getValuesResultList().add(new String[0]); // fill empty values
							}
							
							knownSearchResults.getValuesResultList().set(indexOfNewValue, values.toArray(new String[values.size()]));
						}
					}
					
					return true;
				}
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), "Could not check arguments of " + formulaTree, e);
				} catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
			
			// 6. nonBooleanNode(<1 argument>) = ov
			try {
				if ((formulaTree.getNodeVariable() instanceof BuiltInRVEqualTo)	// =
						&& formulaTree.getChildren().get(1).getSubTypeNode().equals(EnumSubType.OVARIABLE)	// ov
						&& (formulaTree.getChildren().get(0).getNodeVariable() instanceof ResidentNodePointer )	// nonBooleanNode
						&& (((ResidentNodePointer)formulaTree.getChildren().get(0).getNodeVariable()).getResidentNode().getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES)	// "nonBooleanNode" is not boolean node
						&& (((ResidentNodePointer)formulaTree.getChildren().get(0).getNodeVariable()).getResidentNode().getArgumentList().size() == 1) ) {	// <1 argument>		
					// TODO fill knownSearchResults
					return true;
				}
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), "Could not check arguments of " + formulaTree, e);
				} catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
			
			// the "not" cases can be solved recursively by passing "not isToSolveAsPositiveOperation" as argument
			try {
				if (formulaTree.getNodeVariable() instanceof BuiltInRVNot) {
					// 2. not (ov1 = ov2)
					// 4. not booleanNode(<1 or 2 arguments>)
					// 7. not (ov = nonBooleanNode(<1 argument>))
					// 8. not (nonBooleanNode(<1 argument>) = ov)
					return this.solveFormulaTree(formulaTree.getChildren().get(0), knownValues, knownSearchResults, !isToSolveAsPositiveOperation);
				}
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), "Could not check arguments of " + formulaTree, e);
				} catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
		} catch (Exception e) {
			try {
				Debug.println(this.getClass(), formulaTree.toString() + " cannot be solved even when the following ordinary variables are knwon: " + knownValues, e);
			}catch (Throwable t) {
				e.printStackTrace();
				t.printStackTrace();
			}
		}
		
		return false;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateMultipleSearchContextNodeFormula(java.util.List, java.util.List)
	 */
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(List<ContextNode> contextList, List<OVInstance> ovInstances) {

		// prepare list to return
		Map<OrdinaryVariable, List<String>> ret = new HashMap<OrdinaryVariable, List<String>>();
		
		// assertion
		if (contextList == null) {
			return ret;
		}
		
		
		for (ContextNode context : contextList) {
			
			// delegate query to another method
			SearchResult result = this.evaluateSearchContextNodeFormula(context, ovInstances);
			if (result == null) {
				continue;
			}
			
			// fill map
			OrdinaryVariable[] ovSequence = result.getOrdinaryVariableSequence();
			List<String[]> valuesResultList =  result.getValuesResultList();
			for (int i = 0; i < ovSequence.length; i++) {
				ret.put(ovSequence[i], Arrays.asList(valuesResultList.get(i)));
			}
		}
		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * This is the reasoner to be used by this knowledge base in order to perform inference.
	 * If set to null, {@link #getDefaultOWLReasoner()} will begin returning an {@link OWLReasoner} extracted
	 * from {@link MultiEntityBayesianNetwork#getStorageImplementor()} of {@link #getDefaultMEBN()}.
	 * @return the defaultOWLReasoner
	 */
	public OWLReasoner getDefaultOWLReasoner() {
		if (defaultOWLReasoner == null) {
			OWLReasoner reasoner = null;
			try {
				if (this.getDefaultMEBN() != null
						&& this.getDefaultMEBN().getStorageImplementor() != null 
						&& this.getDefaultMEBN().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
					reasoner = ((IOWLAPIStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLReasoner();
				}
			} catch (Throwable t) {
				// it is OK, because we can try extracting the reasoner when KB methods are called and MEBN is passed as arguments
				try {
					Debug.println(this.getClass(), "Could not extract reasoner from mebn " + this.getDefaultMEBN(), t);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			// if reasoner is not available, use the one extracted from MEBN
			Debug.println(this.getClass(), "Extracted reasoner from MEBN: " + reasoner);
			return reasoner;
		}
		return defaultOWLReasoner;
	}

	/**
	 * This is the reasoner to be used by this knowledge base in order to perform inference.
	 * If set to null, {@link #getDefaultOWLReasoner()} will begin returning an {@link OWLReasoner} extracted
	 * from {@link MultiEntityBayesianNetwork#getStorageImplementor()} of {@link #getDefaultMEBN()}.
	 * @param defaultOWLReasoner the defaultOWLReasoner to set
	 */
	public void setDefaultOWLReasoner(OWLReasoner defaultOWLReasoner) {
		this.defaultOWLReasoner = defaultOWLReasoner;
	}


	/**
	 * A {@link MultiEntityBayesianNetwork} to be used by this knowledge base.
	 * This value is initialized in {@link #createGenerativeKnowledgeBase(MultiEntityBayesianNetwork)}.
	 * Methods in this KB will use this MEBN instead of the ones obtainable from the arguments. This is useful
	 * if a MEBN component (e.g. a node) is linked to another MEBN, but informations about OWL specific elements
	 * are stored in "another" MEBN (in this case, this getter/setter should point to the "another" MEBN 
	 *  - the one containing OWL specific data - in order for KB to work).
	 * @return the defaultMEBN
	 */
	public MultiEntityBayesianNetwork getDefaultMEBN() {
		return defaultMEBN;
	}


	/**
	 * A {@link MultiEntityBayesianNetwork} to be used by this knowledge base.
	 * This value is initialized in {@link #createGenerativeKnowledgeBase(MultiEntityBayesianNetwork)}.
	 * Methods in this KB will use this MEBN instead of the ones obtainable from the arguments. This is useful
	 * if a MEBN component (e.g. a node) is linked to another MEBN, but informations about OWL specific elements
	 * are stored in "another" MEBN (in this case, this getter/setter should point to the "another" MEBN 
	 *  - the one containing OWL specific data - in order for KB to work).
	 * @param defaultMEBN the defaultMEBN to set
	 */
	public void setDefaultMEBN(MultiEntityBayesianNetwork defaultMEBN) {
		this.defaultMEBN = defaultMEBN;
	}


	/**
	 * A {@link IMEBNMediator} to be used by this knowledge base if GUI or IO commands needs to be accessed.
	 * @return the defaultMediator
	 */
	public IMEBNMediator getDefaultMediator() {
		return defaultMediator;
	}


	/**
	 * A {@link IMEBNMediator} to be used by this knowledge base if GUI or IO commands needs to be accessed.
	 * @param defaultMediator the defaultMediator to set
	 */
	public void setDefaultMediator(IMEBNMediator defaultMediator) {
		this.defaultMediator = defaultMediator;
	}
	
	/**
	 * If this KB contains a {@link #getDefaultMEBN()} having {@link MultiEntityBayesianNetwork#getStorageImplementor()} an instance of 
	 * {@link ProtegeStorageImplementorDecorator}, then it will extract an instance of {@link OWLModelManager} (the main access point
	 * to Protege's classes) from it.
	 * @return
	 */
	public OWLModelManager getOWLModelManager() {
		try {
			if (this.getDefaultMEBN() != null
					&& this.getDefaultMEBN().getStorageImplementor() != null 
					&& this.getDefaultMEBN().getStorageImplementor() instanceof ProtegeStorageImplementorDecorator) {
				return ((ProtegeStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLEditorKit().getModelManager();
			}
		} catch (Throwable t) {
			// it is OK, because we can try extracting the reasoner when KB methods are called and MEBN is passed as arguments
			try {
				Debug.println(this.getClass(), "Could not extract reasoner from mebn " + this.getDefaultMEBN(), t);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IOWLClassExpressionParserFacade#parseExpression(java.lang.String)
	 */
	public OWLClassExpression parseExpression(String expression) {
		if (this.getOwlClassExpressionParserDelegator() == null) {
			return null;
		}
		return this.getOwlClassExpressionParserDelegator().parseExpression(expression);
	}

	/**
	 * It will lazily instantiate OWLClassExpressionParserFacade if none was specified.
	 * @return the owlClassExpressionParserDelegator
	 */
	public IOWLClassExpressionParserFacade getOwlClassExpressionParserDelegator() {
		if (owlClassExpressionParserDelegator == null) {
			// instantiate the default OWL expression parser
			try {
				// try using protege
				owlClassExpressionParserDelegator = OWLClassExpressionParserFacade.getInstance(((ProtegeStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLEditorKit().getModelManager());
			} catch (Exception e) {
				try {
					Debug.println(this.getClass(), "Could not extract Protege manager for " + this.getDefaultMEBN(), e);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				try {
					// OK. Use OWLAPI if protege was not present
					owlClassExpressionParserDelegator = OWLClassExpressionParserFacade.getInstance(this.getDefaultOWLReasoner().getRootOntology());
				} catch (Exception e2) {
					e.printStackTrace();	// trace the first exception
					throw new IllegalStateException(e2);
				}
			}
		}
		return owlClassExpressionParserDelegator;
	}

	/**
	 * @param owlClassExpressionParserDelegator the owlClassExpressionParserDelegator to set
	 */
	public void setOwlClassExpressionParserDelegator(
			IOWLClassExpressionParserFacade owlClassExpressionParserDelegator) {
		this.owlClassExpressionParserDelegator = owlClassExpressionParserDelegator;
	}

	/**
	 * Calls to {@link IPROWL2ModelUser} will be delegated to this object.
	 * @return the prowlModelUserDelegator
	 */
	public IPROWL2ModelUser getProwlModelUserDelegator() {
		return prowlModelUserDelegator;
	}

	/**
	 * Calls to {@link IPROWL2ModelUser} will be delegated to this object.
	 * @param prowlModelUserDelegator the prowlModelUserDelegator to set
	 */
	public void setProwlModelUserDelegator(
			IPROWL2ModelUser prowlModelUserDelegator) {
		this.prowlModelUserDelegator = prowlModelUserDelegator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#getOntologyPrefixManager(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology) {
		// just delegate...
		if (this.getProwlModelUserDelegator() != null) {
			return this.getProwlModelUserDelegator().getOntologyPrefixManager(ontology);
		}
		return null;
	}

	/**
	 * This is the maximum ammount of time (cycles) we will buzy wait for reasoner synchronization.
	 * @return the maximumBuzyWaitingCount
	 */
	public long getMaximumBuzyWaitingCount() {
		return maximumBuzyWaitingCount;
	}

	/**
	 * 
	 * This is the maximum ammount of time (cycles) we will buzy wait for reasoner synchronization.
	 * @param maximumBuzyWaitingCount the maximumBuzyWaitingCount to set
	 */
	public void setMaximumBuzyWaitingCount(long maximumBuzyWaitingCount) {
		this.maximumBuzyWaitingCount = maximumBuzyWaitingCount;
	}

	/**
	 * This is the time in milliseconds this KB will wait for reasoner synchronization.
	 * @return the sleepTimeWaitingReasonerInitialization
	 */
	public long getSleepTimeWaitingReasonerInitialization() {
		return sleepTimeWaitingReasonerInitialization;
	}

	/**
	 * This is the time in milliseconds this KB will wait for reasoner synchronization.
	 * @param sleepTimeWaitingReasonerInitialization the sleepTimeWaitingReasonerInitialization to set
	 */
	public void setSleepTimeWaitingReasonerInitialization(
			long sleepTimeWaitingReasonerInitialization) {
		this.sleepTimeWaitingReasonerInitialization = sleepTimeWaitingReasonerInitialization;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#extractName(org.semanticweb.owlapi.model.OWLObject)
	 */
	public String extractName(OWLObject owlObject) {
		return this.getProwlModelUserDelegator().extractName(owlObject);
	}

	/**
	 * @return the nonPROWLClassExtractor
	 */
	public INonPROWLClassExtractor getNonPROWLClassExtractor() {
		return nonPROWLClassExtractor;
	}

	/**
	 * @param nonPROWLClassExtractor the nonPROWLClassExtractor to set
	 */
	public void setNonPROWLClassExtractor(
			INonPROWLClassExtractor nonPROWLClassExtractor) {
		this.nonPROWLClassExtractor = nonPROWLClassExtractor;
	}

}
