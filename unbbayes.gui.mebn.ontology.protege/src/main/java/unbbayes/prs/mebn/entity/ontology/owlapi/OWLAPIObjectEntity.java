/**
 * 
 */
package unbbayes.prs.mebn.entity.ontology.owlapi;

import java.util.ArrayList;
import java.util.Collections;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.util.Debug;

/**
 * This is an {@link ObjectEntity} which is also related with {@link org.semanticweb.owlapi.model.OWLIndividual} 
 * and/or {@link org.semanticweb.owlapi.model.OWLClass}
 * @author Shou Matsumoto
 * @see OWLAPIObjectEntityContainer
 */
public class OWLAPIObjectEntity extends ObjectEntity implements IPROWL2ModelUser {
	
	private OWLEntity associatedOWLEntity = null;
	
	/** This is the default instance of {@link #getModelUserDelegator()} used by instances of this class */
	public static final IPROWL2ModelUser DEFAULT_MODEL_USER_DELEGATOR = DefaultPROWL2ModelUser.getInstance();
	
	private IPROWL2ModelUser modelUserDelegator = DEFAULT_MODEL_USER_DELEGATOR;
	
	private MultiEntityBayesianNetwork mebn;

	/**
	 * This is equivalent to {@link #OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)} with 
	 * true as the third argument.
	 * @see #OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)
	 */
	public OWLAPIObjectEntity(String name, MultiEntityBayesianNetwork mebn) throws TypeException {
		this(name, mebn, true);
	}
	/**
	 * @param name : this name will be used to create an OWL entity which will be uniquely identified by {@link #getOWLIRI()}.
	 * @param mebn : the instance of {@link MultiEntityBayesianNetwork} where this object entity will be inserted.
	 * Fields like {@link MultiEntityBayesianNetwork#getTypeContainer()} will be accessed to set up {@link #getType()},
	 * and {@link MultiEntityBayesianNetwork#getStorageImplementor()} will be accessed to set up {@link #getOWLOntology()}.
	 * @param isToCreateOWLEntity : if true, an OWL entity will be created in the owl ontology
	 * referenced by {@link IOWLAPIStorageImplementorDecorator#getAdaptee()} at {@link MultiEntityBayesianNetwork#getStorageImplementor()}.
	 * The new owl entity will be pushed to {@link #setOWLEntity(OWLEntity)}.
	 * The namespace/prefix of the new owl entity will be created accordingly to {@link #getOntologyPrefixManager(OWLOntology)}.
	 * @throws TypeException
	 */
	public OWLAPIObjectEntity(String name, MultiEntityBayesianNetwork mebn, boolean isToCreateOWLEntity) throws TypeException {
		super(name, mebn.getTypeContainer());
		this.setMEBN(mebn);
		
		if (isToCreateOWLEntity) {
			if (name == null || name.isEmpty()) {
				throw new TypeException("Anonym object entity is not allowed.");
			}
			
			// extract storage implementor of MEBN 
			// (a reference to an object that loaded/saved the mebn last time - such object is likely to contain a reference to owl owlOntology)
			OWLOntology ontology = this.getOWLOntology();
			
			if (ontology == null) {
				throw new NullPointerException("Could not find ontology for " + mebn);
			}
			
			// this is the prefix of this ontology
			PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);
			if (currentPrefix == null) {
				throw new NullPointerException("Could not extract ontology prefix from ontology " + ontology 
						+ ". This is probably because getModelUserDelegator is inconsistent.");
			}
			
			// extract factory
			OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
			
			// the entity is new and must be saved.
			
			// MEBN entities are represented as OWL classes. Get them
			OWLClass newEntityClass = null;
			if ("Thing".equalsIgnoreCase(name)) {
				// Entity with "Thing" as its name must be considered as an owl:Thing
				newEntityClass = factory.getOWLThing();
			} else {
				newEntityClass = factory.getOWLClass(name, currentPrefix);
				// check if class exists. If not, create it.
				if (!ontology.containsClassInSignature(newEntityClass.getIRI(), true)
						&& !newEntityClass.isOWLThing() ) {		// just in case of string comparison failing to detect owl:Thing...
					// create an axiom explicitly specifying that newEntityClass is an owl:Thing, and commit axiom.
					ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit
							ontology, 	// ontology where axiom will be added
							factory.getOWLSubClassOfAxiom(	// create "subClassOf" axiom
									newEntityClass, 		// subclass
									factory.getOWLThing()	// superclass = owl:Thing
							)
					);
				}
			}
			
			// set this owl class as the owl entity which represents the new object entity we just created
			this.setAssociatedOWLEntity(newEntityClass);
		}
		
	}

	/**
	 * @return the associatedOWLEntity : the OWL entity in the {@link #getOWLOntology()} which represents this Object entity.
	 * A MEBN object entity and an OWL entity are different things, but this field makes the connection.
	 */
	public OWLEntity getAssociatedOWLEntity() {
		return this.associatedOWLEntity;
	}

	/**
	 * @param associatedOWLEntity : the OWL entity in the {@link #getOWLOntology()} which represents this Object entity.
	 * A MEBN object entity and an OWL entity are different things, but this field makes the connection.
	 */
	public void setAssociatedOWLEntity(OWLEntity associatedOWLEntity) {
		this.associatedOWLEntity = associatedOWLEntity;
	}

	/**
	 * @return the owlOntology : an ontology will be extracted from {@link IOWLAPIStorageImplementorDecorator#getAdaptee()}
	 * at {@link MultiEntityBayesianNetwork#getStorageImplementor()}.
	 * @see #getMEBN();
	 */
	public OWLOntology getOWLOntology() {
		MultiEntityBayesianNetwork mebn = getMEBN();
		if (mebn == null) {
			return null;
		}
		OWLOntology ontology = null;
		// extract storage implementor of MEBN 
		// (a reference to an object that loaded/saved the mebn last time - such object is likely to contain a reference to owl owlOntology)
		if (mebn.getStorageImplementor() != null  && mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
			ontology = ((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee();
		}  else {
			System.err.println("Unable to extract OWL ontology from MEBN " + mebn);
			System.err.println("Creating new ontology...");
			OWLOntologyManager m = OWLManager.createOWLOntologyManager();
			try {
				ontology = m.createOntology();
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException(e);
			}
		}
		return ontology;
	}


	/**
	 * Delegates to {@link #getModelUserDelegator()}
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology) {
		IPROWL2ModelUser delegator = getModelUserDelegator();
		if (delegator == null) {
			return null;
		}
		return delegator.getOntologyPrefixManager(ontology);
	}

	/**
	 * Delegates to {@link #getModelUserDelegator()}
	 */
	public String extractName(OWLObject owlObject) {
		IPROWL2ModelUser delegator = getModelUserDelegator();
		if (delegator == null) {
			return null;
		}
		return delegator.extractName(owlObject);
	}

	/**
	 * @return the modelUserDelegator : calls to {@link #getOntologyPrefixManager(OWLOntology)} and
	 * {@link #extractName(OWLObject)} will be delegated to this object.
	 * @see #DEFAULT_MODEL_USER_DELEGATOR
	 */
	public IPROWL2ModelUser getModelUserDelegator() {
		return modelUserDelegator;
	}

	/**
	 * @param modelUserDelegator : calls to {@link #getOntologyPrefixManager(OWLOntology)} and
	 * {@link #extractName(OWLObject)} will be delegated to this object.
	 * @see #DEFAULT_MODEL_USER_DELEGATOR
	 */
	public void setModelUserDelegator(IPROWL2ModelUser modelUserDelegator) {
		this.modelUserDelegator = modelUserDelegator;
	}
	/**
	 * @return the mebn
	 */
	public MultiEntityBayesianNetwork getMEBN() {
		return mebn;
	}
	/**
	 * @param mebn the mebn to set
	 */
	public void setMEBN(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}
	
	/**
	 * This method also renames {@link #getAssociatedOWLEntity()}
	 * @see unbbayes.prs.mebn.entity.ObjectEntity#setName(java.lang.String)
	 */
	public void setName(String name) throws TypeAlreadyExistsException {
		if (this.getName().equalsIgnoreCase(name)) {
			Debug.println(getClass(), "Not renaming " + getName() + ", because the name is the same.");
			return;
		}
		super.setName(name);
		
		// now, rename the associated owl entity
		
		// extract the entity to rename
		OWLEntity associatedOWLEntity = getAssociatedOWLEntity();
		if (associatedOWLEntity == null) {
			return; // there is no owl entity to rename
		}
		
		// extract the ontology and its manager
		OWLOntology ontology = getOWLOntology();
		if (ontology == null) {
			throw new NullPointerException("Could not extract owl ontology from mebn " + getMEBN());
		}
		OWLOntologyManager manager = ontology.getOWLOntologyManager();	// the manager of the owl ontology
		
		// prepare a new owl class instance just in order to check if an owl entity with the new name exist 
		// (and also make sure the IRI pattern follows the same of constructor, by calling the same methods)
		OWLClass temporaryOWLClass = manager.getOWLDataFactory().getOWLClass(name, getOntologyPrefixManager(ontology));
		if (ontology.containsClassInSignature(temporaryOWLClass.getIRI(), true)) {
			Debug.println(getClass(), "OWL class " + associatedOWLEntity + " is being renamed to " + temporaryOWLClass + ", which already exists.");
			// remove old class
			OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
			remover.visit(associatedOWLEntity.asOWLClass());
			manager.applyChanges(remover.getChanges());	// commit
		} else {
			// actually change name (and commit change)
			manager.applyChanges(new OWLEntityRenamer(manager, Collections.singleton(ontology)).changeIRI(associatedOWLEntity, temporaryOWLClass.getIRI()));
		}
		
		// substitute with the existing class
		this.setAssociatedOWLEntity(temporaryOWLClass);
		
		IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(getMEBN(), this, temporaryOWLClass.getIRI());
	}
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.ObjectEntity#addInstance(java.lang.String)
	 */
	public ObjectEntityInstance addInstance(String name) throws TypeException {
		 ObjectEntityInstance entityInstance = super.addInstance(name);
		 if (entityInstance == null) {
			 return null;
		 }
		 
		 // extract some variables which will be used as shortcuts
		 MultiEntityBayesianNetwork mebn = getMEBN();
		 OWLOntology ontology = getOWLOntology();

		 // ignore entityInstances that were already stored in this ontology (we are not going to save them twice)
		 {
			 IRI storedIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(mebn, entityInstance);
			 if (storedIRI != null && ontology.containsIndividualInSignature(storedIRI, true)) {
				 try {
					 Debug.println(this.getClass(), entityInstance + " is already included in " + ontology + ". IRI = " + storedIRI);
				 } catch (Throwable t) {
					 t.printStackTrace();
				 }
				 return entityInstance;
			 }
		 }
		 
		 // extract the prefix manager to be used to control  the namespace of OWL individual
		 PrefixManager currentPrefix = getOntologyPrefixManager(ontology);
		 // this is the prefix of PR-OWL2 definitions
		 PrefixManager prowl2Prefix = getOntologyPrefixManager(null);
		 
		 // extract/create new OWL individual
		 OWLNamedIndividual owlIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(entityInstance.getName(), currentPrefix);
		 // if it does not exist, create it as an individual of entity
		 if (!ontology.containsIndividualInSignature(owlIndividual.asOWLNamedIndividual().getIRI(), true)) {
			 try {
				Debug.println(this.getClass(), owlIndividual + ", and individual of " + this.getAssociatedOWLEntity() + " is going to be added into " + ontology);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			// create axiom asserting that individual's type is currentOWLEntity and commit change
			ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit change
					 ontology, 	// ontology where axiom will be inserted
					 ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(this.getAssociatedOWLEntity().asOWLClass(), owlIndividual)	// individual's type is the associated owl class
			);
			
			// fill some required properties (e.g. hasUID)
			try {
				ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit
						ontology, 	// where to add axiom
						ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(	// axiom to add datatype property to an individual
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IPROWL2ModelUser.HASUID, prowl2Prefix), // get hasUID datatype property
								owlIndividual, 					// individual having the hasUID property
								"!" + entityInstance.getName()	// value
						)
				);
			} catch (Exception e) {
				// Current version of this IO should work with or without the unique ID
				try {
					Debug.println(this.getClass(), "Could not set UID for " + owlIndividual + " : " + e.getMessage(), e);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			
		 }
		 
		 // add individual in the IRI mapping
		 IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, entityInstance, owlIndividual.getIRI());
		 
		 return entityInstance;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.ObjectEntity#removeInstance(unbbayes.prs.mebn.entity.ObjectEntityInstance)
	 */
	public void removeInstance(ObjectEntityInstance instance) {
		super.removeInstance(instance);
		
		// now, delete the associated owl individual
		
		// extract the ontology and its manager
		OWLOntology ontology = getOWLOntology();
		if (ontology == null) {
			throw new NullPointerException("Could not extract owl ontology from mebn " + getMEBN());
		}
		OWLOntologyManager manager = ontology.getOWLOntologyManager();	// the manager of the owl ontology
		PrefixManager currentPrefix = getOntologyPrefixManager(ontology);	// namespace of current ontology
		
		// extract the IRI and the OWL individual to delete
		IRI iri = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(getMEBN(), instance);
		OWLNamedIndividual individualToRemove = null;
		if (iri != null) {
			// extract the individual from mapped IRI
			individualToRemove = manager.getOWLDataFactory().getOWLNamedIndividual(iri);
		} else {
			// there were no IRI mapped for this instance. Make a guess from its name 
			System.err.println(instance + " is not mapped to any OWL individual in " + getMEBN() + ". Guessing the IRI from its name...");
			individualToRemove = manager.getOWLDataFactory().getOWLNamedIndividual(instance.getName(), currentPrefix);
			iri = individualToRemove.getIRI();
			System.err.println("Guessed IRI = " + iri);
		}
		if ( ( iri == null ) || (individualToRemove == null) || !ontology.containsIndividualInSignature(iri, true) ) {
			return; // there is no individual to delete anyway
		}
		
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		remover.visit(individualToRemove);
		manager.applyChanges(remover.getChanges());	// commit
	}
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.ObjectEntity#removeAllInstances()
	 */
	public void removeAllInstances() {
		for (ObjectEntityInstance instance : new ArrayList<ObjectEntityInstance>(getInstanceList())) {	// iterate on a cloned list, so that changes are not concurrent.
			this.removeInstance(instance);
		}
	}
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.ObjectEntity#delete()
	 */
	protected void delete() throws TypeDoesNotExistException {
		// TODO Auto-generated method stub
		super.delete();
		
		// now, delete the associated owl entity
		
		// extract the entity to delete
		OWLEntity associatedOWLEntity = getAssociatedOWLEntity();
		if (associatedOWLEntity == null) {
			return; // there is no owl entity to delete
		}
		
		// extract the ontology and its manager
		OWLOntology ontology = getOWLOntology();
		if (ontology == null) {
			throw new NullPointerException("Could not extract owl ontology from mebn " + getMEBN());
		}
		OWLOntologyManager manager = ontology.getOWLOntologyManager();	// the manager of the owl ontology
		
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));
		remover.visit(associatedOWLEntity.asOWLClass());
		manager.applyChanges(remover.getChanges());	// commit
		
	}
	
	

}
