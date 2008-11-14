/**
 * 
 */
package unbbayes.prs.oobn;

import java.awt.datatransfer.Transferable;
import java.util.Set;

import unbbayes.prs.Network;
import unbbayes.prs.oobn.exception.OOBNException;

/**
 * @author Shou Matsumoto
 * Represents an OOBN class (containing a set of nodes)
 */
public interface IOOBNClass extends Transferable{

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
	
	/**
	 * Obtains a Network representation of this OOBN class
	 * @return
	 */
	public Network getNetwork();
	
	/**
	 * Obtains a set of output nodes at this class
	 */
	public Set<IOOBNNode> getOutputNodes();
	
	
	/**
	 * Obtains a set of input nodes at this class
	 */	
	public Set<IOOBNNode> getInputNodes();
	
//	/**
//	 * Changes the encapsulated network of this OOBNClass
//	 * @param net
//	 */
//	public void setNetwork(Network net);
}
