/**
 * 
 */
package unbbayes.prs.mebn.entity.ontology.owlapi;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import unbbayes.io.mebn.owlapi.IOWLAPIObjectEntityBuilder;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.IObjectEntityBuilder;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityContainer;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * 
 * This is an extension of {@link ObjectEntityContainer} which instantiates and manages
 * {@link OWLAPIObjectEntity} instead of {@link ObjectEntity}.
 * @author Shou Matsumoto
 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO
 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO
 *
 */
public class OWLAPIObjectEntityContainer extends ObjectEntityContainer {

	private MultiEntityBayesianNetwork mebn;
	
//	private boolean isToCreateOWLEntity = true;
	

	/**
	 * Default constructor initializing fields.
	 *  This is an extension of {@link ObjectEntityContainer} which instantiates and manages
	 *  {@link OWLAPIObjectEntity} instead of {@link ObjectEntity}.
	 * @param _typeConteiner : responsible for instantiating and managing {@link Type}
	 * @param mebn : the instance of {@link MultiEntityBayesianNetwork} related to entities to be managed by {@link OWLAPIObjectEntityContainer}.
	 * Fields like {@link MultiEntityBayesianNetwork#getStorageImplementor()} and {@link MultiEntityBayesianNetwork#getTypeContainer()} will be referenced.
	 * @see ObjectEntityContainer#ObjectEntityContainer(TypeContainer)
	 */
	public OWLAPIObjectEntityContainer(MultiEntityBayesianNetwork mebn) {
		super(mebn.getTypeContainer(),OWLAPIObjectEntityBuilder.getInstance(mebn, true));
		this.setDefaultRootEntityName(OWLAPIObjectEntity.THING);
		this.setMEBN(mebn);
		if (getRootObjectEntity() == null) {
			createRootObjectEntity();
		}
			
	}

	/**
	 * @return the mebn
	 */
	public MultiEntityBayesianNetwork getMEBN() {
		return mebn;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.ObjectEntityContainer#createRootObjectEntity()
	 */
	protected void createRootObjectEntity() {
		if (getMEBN() == null) {
			return;
		}
		super.createRootObjectEntity();
	}

	/**
	 * @param mebn the mebn to set
	 */
	public void setMEBN(MultiEntityBayesianNetwork mebn) {
		if (this.mebn == mebn) {
			return;	// there is no change
		}
		this.mebn = mebn;
		
		// extract the old container, so that we can copy its content to this container
		ObjectEntityContainer oldContainer = mebn.getObjectEntityContainer();
		if (oldContainer == this) {
			return;	// there is no need to change
		}
		
		// reset the content of this container
//		for (ObjectEntity entity : new ArrayList<ObjectEntity>(this.getListEntity())) {	// iterate on cloned list, because original list will be changed
//			this.clearAllInstances(entity);
//			try {
//				this.removeEntity(entity);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		this.getListEntity().clear();
		this.getListEntityInstances().clear();
		
		// fill this_ container with content of old container;
		this.setTypeContainer(oldContainer.getTypeContainer());
//		this.setEntityNum(oldContainer.getEntityNum());
//		this.getListEntity().addAll(oldContainer.getListEntity());
//		this.getListEntityInstances().addAll(oldContainer.getListEntityInstances());
		
	}
	
//	/**
//	 * @see unbbayes.prs.mebn.entity.ObjectEntityContainer#createObjectEntity(java.lang.String)
//	 * @return an instance created by {@link #createObjectEntity(String, boolean)}. 
//	 * The boolean argument will be filled with the value returned from {@link #isToCreateOWLEntity()} 
//	 */
//	public ObjectEntity createObjectEntity(String name) throws TypeException {
//		return this.createObjectEntity(name, isToCreateOWLEntity());
//	}

	/** 
	 * This method returns an instance of {@link OWLAPIObjectEntity} instead of {@link ObjectEntity}
	 * @param name : the name of the new object entity to create.
	 * @param isToCreateOWLEntity : if true, an OWL entity will be created in the owl ontology.
	 * @see unbbayes.prs.mebn.entity.ObjectEntityContainer#createObjectEntity(java.lang.String)
	 * @see OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)
	 * @deprecated invoke {@link #setToCreateOWLEntity(boolean)} and then use {@link #createObjectEntity(String)}.
	 */
	public ObjectEntity createObjectEntity(String name, boolean isToCreateOWLEntity) throws TypeException {
//		OWLAPIObjectEntity objEntity = new OWLAPIObjectEntity(name, getMEBN(), isToCreateOWLEntity);
//		objEntity.getType().addUserObject(objEntity); 
//		
//		//	the following line is the same of superclass' private method addEntity(objEntity)
//		getListEntity().add(objEntity);
//	
//		plusEntityNum(); 
//		
//		return objEntity; 
		boolean backup = this.isToCreateOWLEntity();
		try {
			this.setToCreateOWLEntity(isToCreateOWLEntity);
			return this.createObjectEntity(name);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally  {
			this.setToCreateOWLEntity(backup);
		}
	}

	/**
	 * @return the isToCreateOWLEntity : if true, then an OWL entity will be created in the owl ontology
	 * when {@link #createObjectEntity(String)} is called.
	 * @see #createObjectEntity(String, boolean)
	 * @see #getOWLAPIObjectEntityBuilder()
	 */
	public boolean isToCreateOWLEntity() {
		return getOWLAPIObjectEntityBuilder().isToCreateOWLEntity();
	}

	/**
	 * @param isToCreateOWLEntity : if true, then an OWL entity will be created in the owl ontology
	 * when {@link #createObjectEntity(String)} is called.
	 * @see #createObjectEntity(String, boolean)
	 */
	public void setToCreateOWLEntity(boolean isToCreateOWLEntity) {
		getOWLAPIObjectEntityBuilder().setToCreateOWLEntity(isToCreateOWLEntity);
	}

	/**
	 * @return {@link #getOWLAPIObjectEntityBuilder()} cast to {@link IOWLAPIObjectEntityBuilder}.
	 */
	public IOWLAPIObjectEntityBuilder getOWLAPIObjectEntityBuilder() {
		
		// check if we can retrieve some builder from getObjectEntityBuilder()
		IObjectEntityBuilder builder = getObjectEntityBuilder();
		// check if builder is compatible. TODO see if there is a better way of doing this check.
		if (builder != null && (builder instanceof IOWLAPIObjectEntityBuilder)) {
			// return the instance itself
			return (IOWLAPIObjectEntityBuilder) builder;
		}
		
		// return an adapted version of getObjectEntityBuilder()
		return new IOWLAPIObjectEntityBuilder() {
			public ObjectEntity getObjectEntity(String name) { return getObjectEntityBuilder().getObjectEntity(name); }
			public boolean isToCreateOWLEntity() { return false; }
			public void setToCreateOWLEntity(boolean isToCreateOWLEntity) {
				if (isToCreateOWLEntity) {
					// do not allow this to be true
					throw new UnsupportedOperationException(); 
				}
			}
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.ObjectEntityContainer#addEntity(unbbayes.prs.mebn.entity.ObjectEntity, unbbayes.prs.mebn.entity.ObjectEntity)
	 */
	protected void addEntity(ObjectEntity entity, ObjectEntity parentObjectEntity) {
		super.addEntity(entity, parentObjectEntity);
		// we need to handle hierarchy in OWL
		if (parentObjectEntity == null || getRootObjectEntity().equals(parentObjectEntity)) {
			return;	// no need to handle hierarchy in OWL if parent was not specified or parent is default (root is the default)
		}
		
		// extract owl ontology where superclass/subclass assertions will be included
		OWLOntology ontology = getOWLOntology();
		if (ontology == null) {
			throw new IllegalArgumentException("Could not extract OWL ontology from MEBN instance: " + getMEBN());
		}
		
		// TODO migrate the following to ObjectEntity or OWLAPIObjectEntity
		
		// extract the OWL class of entity, but in order to do so, extract the IRI first
		IRI entityIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(getMEBN(), entity);
		if (entityIRI == null) {	// generate IRI if it was not mapped in mebn
			entityIRI = IRI.create(ontology.getOntologyID().getOntologyIRI().getStart() + "#" + entity.getName());
			IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(getMEBN(), entity, entityIRI);
		}
		OWLClassExpression entityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(entityIRI);
		
		// extract the OWL class of entity
		IRI parentIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(getMEBN(), parentObjectEntity);
		if (parentIRI == null) {	// generate IRI if it was not mapped in mebn
			parentIRI = IRI.create(ontology.getOntologyID().getOntologyIRI().getStart() + "#" + parentObjectEntity.getName());
			IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(getMEBN(), parentObjectEntity, parentIRI);
		}
		OWLClassExpression parentClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(parentIRI);
		
		// include "subclassof" assertion
		ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit
				ontology, 	// ontology where axiom will be added
				ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(	
						entityClass, 	
						parentClass		
				)
		);
	}
	
	/**
	 * @return the owlOntology : an ontology will be extracted from {@link IOWLAPIStorageImplementorDecorator#getAdaptee()}
	 * at {@link MultiEntityBayesianNetwork#getStorageImplementor()}.
	 * @see #getMEBN();
	 */
	private OWLOntology getOWLOntology() {
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

}
