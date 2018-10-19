/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import unbbayes.prs.mebn.entity.IObjectEntityBuilder;

/**
 * @author Shou Matsumoto
 *
 */
public interface IOWLAPIObjectEntityBuilder extends IObjectEntityBuilder {

	/**
	 * @return the isToCreateOWLEntity : if true, then an OWL entity shall be created in the owl ontology
	 * when {@link IObjectEntityBuilder#getObjectEntity(String)} is called.
	 */
	public boolean isToCreateOWLEntity();

	/**
	 * @param isToCreateOWLEntity : if true, then an OWL entity shall be created in the owl ontology
	 * when {@link IObjectEntityBuilder#getObjectEntity(String)} is called.
	 */
	public void setToCreateOWLEntity(boolean isToCreateOWLEntity);
	
}
