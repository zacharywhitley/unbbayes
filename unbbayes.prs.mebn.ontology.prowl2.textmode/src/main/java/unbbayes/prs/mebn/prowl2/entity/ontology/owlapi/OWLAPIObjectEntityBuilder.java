/**
 * 
 */
package unbbayes.prs.mebn.prowl2.entity.ontology.owlapi;

import unbbayes.io.mebn.prowl2.owlapi.IOWLAPIObjectEntityBuilder;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * This will create an instance of {@link OWLAPIObjectEntity}
 * @author Shou Matsumoto
 * @see OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)
 */
public class OWLAPIObjectEntityBuilder implements IOWLAPIObjectEntityBuilder {

	private MultiEntityBayesianNetwork mebn;
	private boolean isToCreateOWLEntity;
	
	/**
	 * Constructor method initializing fields.
	 * @param mebn : {@link MultiEntityBayesianNetwork} where this entity will be inserted to.
	 * @param isToCreateOWLEntity : it will set {@link #setToCreateOWLEntity(boolean)}
	 * @return an instance of of {@link IOWLAPIObjectEntityBuilder} whose
	 * {@link IOWLAPIObjectEntityBuilder#getObjectEntity(String)} will return an instance of
	 * {@link OWLAPIObjectEntity}.
	 * @see OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)
	 * @see MultiEntityBayesianNetwork#getObjectEntityContainer()
	 * @see MultiEntityBayesianNetwork#getTypeContainer()
	 */
	public static IOWLAPIObjectEntityBuilder getInstance(MultiEntityBayesianNetwork mebn, boolean isToCreateOWLEntity) {
		return new OWLAPIObjectEntityBuilder(mebn, isToCreateOWLEntity);
	}

	/**
	 * Default constructor is protected in order to allow inheritance
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, boolean)} instead
	 */
	protected OWLAPIObjectEntityBuilder() {}
	
	/**
	 * Default constructor initializing fields
	 * @param mebn : {@link #setMEBN(MultiEntityBayesianNetwork)} will be called for this
	 * @param isToCreateOWLEntity : {@link #setToCreateOWLEntity(boolean)} will be called for this
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, boolean)} instead
	 */
	public OWLAPIObjectEntityBuilder(MultiEntityBayesianNetwork mebn, boolean isToCreateOWLEntity) {
		this.setMEBN(mebn);
		this.setToCreateOWLEntity(isToCreateOWLEntity);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.IObjectEntityBuilder#getObjectEntity(java.lang.String)
	 */
	public ObjectEntity getObjectEntity(String name) {
		try {
			return new OWLAPIObjectEntity(name, getMEBN(), isToCreateOWLEntity());
		} catch (TypeException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This will be used for the boolean value of {@link OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)}
	 * @see unbbayes.prs.mebn.entity.IObjectEntityBuilder#setToCreateOWLEntity(boolean)
	 */
	public void setToCreateOWLEntity(boolean isToCreateOWLEntity) {
		this.isToCreateOWLEntity = isToCreateOWLEntity;
	}

	/**
	 * This will be used for the boolean value of {@link OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)}
	 * @see unbbayes.io.mebn.prowl2.owlapi.IOWLAPIObjectEntityBuilder#isToCreateOWLEntity()
	 */
	public boolean isToCreateOWLEntity() {
		return isToCreateOWLEntity;
	}

	/**
	 * @return the mebn
	 * @see OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)
	 */
	public MultiEntityBayesianNetwork getMEBN() {
		return mebn;
	}

	/**
	 * @param mebn the mebn to set
	 * @see OWLAPIObjectEntity#OWLAPIObjectEntity(String, MultiEntityBayesianNetwork, boolean)
	 */
	public void setMEBN(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}

}
