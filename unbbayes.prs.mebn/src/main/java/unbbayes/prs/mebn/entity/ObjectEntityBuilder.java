/**
 * 
 */
package unbbayes.prs.mebn.entity;

import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * Default implementation of {@link IObjectEntityBuilder}. It generates instances of {@link ObjectEntity}.
 * @author Shou Matsumoto
 * @author Guilherme Carvalho
 *
 */
public class ObjectEntityBuilder implements IObjectEntityBuilder {

//	private MultiEntityBayesianNetwork mebn = null;
	
	private TypeContainer typeContainer = new TypeContainer();
	
	/**
	 * Default constructor is kept protected in order to allow inheritance.
	 */
	protected ObjectEntityBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor method for this class.
	 * @param typeContainer : This will be set to {@link #setTypeContainer(TypeContainer)}.
	 * @return a new instance of {@link ObjectEntityBuilder}
	 */
	public static IObjectEntityBuilder getInstance(TypeContainer typeContainer) {
		ObjectEntityBuilder ret = new ObjectEntityBuilder();
		ret.setTypeContainer(typeContainer);
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.IObjectEntityBuilder#getObjectEntity(java.lang.String)
	 */
	public ObjectEntity getObjectEntity(String name) {
		if(this.typeContainer == null){
			throw new IllegalStateException("setTypeContainer(TypeContainer) must be called.");
		}
		
		try {
			return new ObjectEntity(name, this.typeContainer);
		} catch (TypeException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @return the typeContainer
	 * @see #getObjectEntity(String)
	 */
	public TypeContainer getTypeContainer() {
		return typeContainer;
	}

	/**
	 * @param typeContainer the typeContainer to set
	 * @see #getObjectEntity(String)
	 */
	public void setTypeContainer(TypeContainer typeContainer) {
		this.typeContainer = typeContainer;
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.mebn.entity.IObjectEntityBuilder#setMEBN(unbbayes.prs.mebn.MultiEntityBayesianNetwork)
//	 */
//	public void setMEBN(MultiEntityBayesianNetwork mebn) {
//		this.mebn = mebn;
//	}

}
