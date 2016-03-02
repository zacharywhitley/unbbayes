/**
 * 
 */
package unbbayes.prs.mebn.entity.ontology.owlapi;

import unbbayes.io.mebn.owlapi.IOWLAPIObjectEntityBuilder;
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
//		if(getRootObjectEntity() == null) {
//			try {
//				
//				setRootObjectEntity(getObjectEntityByName(THING));
//				ObjectEntity rootObjectEntity = getRootObjectEntity();
//				
//				if(rootObjectEntity == null) {
//					Type objectEntityType = getTypeContainer().getType(THING + "_label");
//					if (objectEntityType != null) {
//						// remove existing type
//						getTypeContainer().removeType(objectEntityType);
//					}
//					
////					rootObjectEntity = new OWLAPIObjectEntity(OBJECT_ENTITY, getTypeContainer()); 
//					rootObjectEntity = new OWLAPIObjectEntity(THING, getMEBN(), true);	// true will force Thing to become owl:Thing
//					rootObjectEntity.getType().addUserObject(rootObjectEntity); 
//				
//					plusEntityNum();
//					
//					setRootObjectEntity(rootObjectEntity);
//				}
//				
//				addEntity(rootObjectEntity, null);
//	
//				this.getMapObjectChilds().put(rootObjectEntity, new ArrayList<ObjectEntity>());
//				this.getMapObjectParents().put(rootObjectEntity, (List) Collections.emptyList());
//				
//				refreshTreeModel();
//				
//			} catch (TypeException e) {
//				throw new RuntimeException(e);
//			}
//		}
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
		this.setToCreateOWLEntity(isToCreateOWLEntity);
		return this.createObjectEntity(name);
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

}
