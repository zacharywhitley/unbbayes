/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.ontology.protege;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.IPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.OWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.protege.ProtegeStorageImplementorDecorator;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
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
	
	private long maximumBuzyWaitingCount = 100;
	
	private long sleepTimeWaitingReasonerInitialization = 1000;

	/**
	 * The default constructor is only visible in order to allow inheritance
	 * @deprecated use {@link #getInstance(OWLReasoner, MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	protected OWL2KnowledgeBase() {
		// TODO Auto-generated constructor stub
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
		ret.setProwlModelUserDelegator(DefaultPROWL2ModelUser.getInstance());
		ret.setDefaultOWLReasoner(reasoner);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
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
		Debug.println(this.getClass(), "Ignoring OWL KB clear request.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#clearFindings()
	 */
	public void clearFindings() {
		Debug.println(this.getClass(), "Ignoring OWL KB clear finding request.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createGenerativeKnowledgeBase(unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void createGenerativeKnowledgeBase(MultiEntityBayesianNetwork mebn) {
		this.setDefaultMEBN(mebn);
		this.setDefaultOWLReasoner(null); // this setting forces the delegation of OWL reasoners
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
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#saveGenerativeMTheory(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)
	 */
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#saveFindings(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#loadModule(java.io.File, boolean)
	 */
	public void loadModule(File file, boolean findingModule)
			throws UBIOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSingleSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public List<String> evaluateSingleSearchContextNodeFormula(
			ContextNode context, List<OVInstance> ovInstances)
			throws OVInstanceFaultException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateMultipleSearchContextNodeFormula(java.util.List, java.util.List)
	 */
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(
			List<ContextNode> contextList, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#existEntity(java.lang.String)
	 */
	public boolean existEntity(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#searchFinding(unbbayes.prs.mebn.ResidentNode, java.util.Collection)
	 */
	public StateLink searchFinding(ResidentNode randonVariable,
			Collection<OVInstance> listArguments) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getEntityByType(java.lang.String)
	 */
	public List<String> getEntityByType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#fillFindings(unbbayes.prs.mebn.ResidentNode)
	 */
	public void fillFindings(ResidentNode resident) {
		// TODO Auto-generated method stub

	}

	/** 
	 * If this method is called for save operation, the currently used ontology is saved
	 * (only the deterministic part is saved). If this is called for load operation,
	 * the reasoner is restarted.
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#supportsLocalFile(boolean)
	 */
	public boolean supportsLocalFile(boolean isLoad) {
		if (isLoad) {
			this.getOWLModelManager().getOWLReasonerManager().classifyAsynchronously(this.getOWLModelManager().getReasonerPreferences().getPrecomputedInferences());
			// maybe there would be some synchronization problems, because of protege's asynchronous initialization of reasoners. Let's wait until it becomes ready
			for (long i = 0; i < this.getMaximumBuzyWaitingCount(); i++) {
				// TODO Stop using buzy waiting!!!
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
		} else {
			try {
				this.getDefaultOWLReasoner().getRootOntology().getOWLOntologyManager().saveOntology(this.getDefaultOWLReasoner().getRootOntology());
			} catch (OWLOntologyStorageException e) {
				throw new RuntimeException("Could not save current ontology.", e);
			}
		}
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
						&& this.getDefaultMEBN().getStorageImplementor() instanceof OWLAPIStorageImplementorDecorator) {
					reasoner = ((OWLAPIStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLReasoner();
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


}
