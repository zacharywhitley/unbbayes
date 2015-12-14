/**
 * 
 */
package unbbayes.prs.mebn.entity;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * This is a builder for {@link ObjectEntity}
 * @author Shou Matsumoto
 * @author Guilherme Carvalho
 * @see ObjectEntityContainer
 */
public interface IObjectEntityBuilder {
	
	/**
	 * @return a new instance of {@link ObjectEntity}, created from attributes in {@link #setMEBN(MultiEntityBayesianNetwork)}
	 * @param name : The name of the new {@link ObjectEntity}.
	 */
	public ObjectEntity getObjectEntity(String name);
	
	/**
	 * @param isToCreateOWLEntity : if true, then an OWL entity will be created in the owl ontology
	 * when {@link #getObjectEntity(String)} is called.
	 */
	public void setToCreateOWLEntity(boolean isToCreateOWLEntity);
	
//	/**
//	 * sets the current instance of {@link MultiEntityBayesianNetwork}, which is used by this object in order to instantiate a new {@link ObjectEntity}.
//	 * @param mebn
//	 */
	//public void setMEBN(MultiEntityBayesianNetwork mebn);
}
