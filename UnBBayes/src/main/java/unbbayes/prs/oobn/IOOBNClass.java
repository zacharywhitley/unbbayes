/**
 * 
 */
package unbbayes.prs.oobn;

import java.util.Set;

import unbbayes.prs.oobn.exception.OOBNException;

/**
 * @author Shou Matsumoto
 * Represents an OOBN class (containing a set of nodes)
 */
public interface IOOBNClass {

	/**
	 * Obtains all nodes within this oobn class
	 * @return
	 */
	public Set<IOOBNNode> getAllNodes();
	
	/**
	 * 
	 * @return the name of the oobn class
	 */
	public String getClassName();
	
	/**
	 * Setter for class' name
	 * @param name: the name to set
	 * @throws OOBNException when name is invalid
	 */
	public void setClassName(String name) throws OOBNException;
}
