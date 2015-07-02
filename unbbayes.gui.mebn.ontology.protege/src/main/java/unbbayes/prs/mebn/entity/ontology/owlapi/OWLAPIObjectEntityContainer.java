/**
 * 
 */
package unbbayes.prs.mebn.entity.ontology.owlapi;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
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
		super(mebn.getTypeContainer());
		this.setMEBN(mebn);
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
		this.setEntityNum(oldContainer.getEntityNum());
		this.setTypeContainer(oldContainer.getTypeContainer());
		this.getListEntity().addAll(oldContainer.getListEntity());
		this.getListEntityInstances().addAll(oldContainer.getListEntityInstances());
		
	}

	/** 
	 * This method returns an instance of {@link OWLAPIObjectEntity} instead of {@link ObjectEntity}
	 * @see unbbayes.prs.mebn.entity.ObjectEntityContainer#createObjectEntity(java.lang.String)
	 */
	public ObjectEntity createObjectEntity(String name) throws TypeException {
		
		OWLAPIObjectEntity objEntity = new OWLAPIObjectEntity(name, getMEBN());
		objEntity.getType().addUserObject(objEntity); 
		
		//	the following line is the same of superclass' private method addEntity(objEntity)
		getListEntity().add(objEntity);
	
		plusEntityNum(); 
		
		return objEntity; 
	}



}
