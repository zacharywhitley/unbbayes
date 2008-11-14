/**
 * 
 */
package unbbayes.prs.oobn;

import java.util.List;

import unbbayes.prs.bn.SingleEntityNetwork;

/**
 * @author Shou Matsumoto
 * Represents a general OOBN project. It contains a set of OOBN classes.
 */
public interface IObjectOrientedBayesianNetwork {

	/**
	 * Getter for OOBN's title
	 * @return the title of OOBN
	 */
	public String getTitle();
	
	
	/**
	 * Setter for OOBN's title
	 * @param title
	 */
	public void setTitle(String title);
	
	
	/**
	 * Obtains the number of oobn classes loaded within this OOBN project
	 * @return number of oobn network fragments
	 */
	public Integer getOOBNClassCount();
	
	/**
	 * Setter for oobn classes managed by this OOBN project
	 * @param oobnClasses
	 */
	public void setOOBNClassList( List<IOOBNClass> oobnClasses);
	
	/**
	 * This method must return a reference, not a copy, since the add operation should work
	 * as well...
	 * @return obtains a set (list) containing all OOBN classes within this OOBN project
	 */
	public List<IOOBNClass> getOOBNClassList();
	
	
	/**
	 * Obtains a single entity network representation of this OOBN
	 * @return
	 */
	public SingleEntityNetwork getSingleEntityNetwork();
}
