/**
 * 
 */
package unbbayes.prs.prm;

import java.util.List;

import unbbayes.prs.Graph;
import unbbayes.prs.prm.builders.IPRMClassBuilder;

/**
 * This interface represents a set of PRM classes (set of entities and relationships)
 * @author Shou Matsumoto
 *
 */
public interface IPRM extends Graph {
	
	/**
	 * The name of the PRM project
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * The name of the PRM project
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Obtains a list of classes (entities) forming
	 * this PRM project.
	 * @return
	 */
	public List<IPRMClass> getIPRMClasses();
	

	/**
	 * Sets a list of classes (entities) forming
	 * this PRM project.
	 * @param prmClasses : list to set
	 */
	public void setIPRMClasses(List<IPRMClass> prmClasses);
	
	/**
	 * Obtains a PRM class (entity/table) by its name
	 * @param name
	 * @return
	 */
	public IPRMClass findPRMClassByName(String name);
	
	/**
	 * Adds a class to {@link #getIPRMClasses()}
	 * and ensures consistency
	 * @param prmClass
	 */
	public void addPRMClass(IPRMClass prmClass);
	
	/**
	 * Removes a class from {@link #getIPRMClasses()}
	 * and ensures consistency
	 * @param prmClass
	 */
	public void removePRMClass(IPRMClass prmClass);	
	

	/**
	 * The builder to instantiate a class in this prm
	 * @return the prmClassBuilder
	 */
	public IPRMClassBuilder getPrmClassBuilder();

	/**
	 * The builder to instantiate a class in this prm
	 * @param prmClassBuilder the prmClassBuilder to set
	 */
	public void setPrmClassBuilder(IPRMClassBuilder prmClassBuilder);
}
