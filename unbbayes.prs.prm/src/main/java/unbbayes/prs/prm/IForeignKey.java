/**
 * 
 */
package unbbayes.prs.prm;

import java.util.List;
import java.util.Set;

/**
 * This interface represents a foreign key
 * reference, containing
 * basically the foreign key, the
 * referenced primary key.
 * @author Shou Matsumoto
 *
 */
public interface IForeignKey {
	
	/**
	 * Name of this foreign key
	 * @return
	 */
	public String getName();
	

	/**
	 * Name of this foreign key
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Gets the class having this foreign key
	 * @return
	 */
	public IPRMClass getClassFrom();
	
	/**
	 * Sets the class having this foreign key
	 * @param prmClassFrom
	 */
	public void setClassFrom(IPRMClass prmClassFrom);
	
	/**
	 * Gets the class where this foreign key points to
	 * @return
	 */
	public IPRMClass getClassTo();
	
	/**
	 * Sets the class where this foreign key points to
	 * @param prmClassTo
	 */
	public void setClassTo(IPRMClass prmClassTo);
	
	/**
	 * Obtains the dependency chain (probabilistic dependency)
	 * related to this foreign key.
	 * @return
	 */
	public List<IDependencyChain> getDependencyChain();
	

	/**
	 * Sets the dependency chain (probabilistic dependency)
	 * related to this foreign key.
	 * @return
	 */
	public void setDependencyChain(List<IDependencyChain> dependencyChains);
	
	/**
	 * Obtains the foreign key attributes of the "from" class
	 * (class containing this foreign key).
	 * It must be compatible to the ones belonging to {@link #getClassFrom()}.
	 * @return a set of attributes belonging to this foreign key
	 */
	public Set<IAttributeDescriptor> getKeyAttributesFrom();
	

	/**
	 * Sets the foreign key attributes of the "from" class
	 * (class containing this foreign key)
	 * @param keyAttributes : a set of attributes belonging to this foreign key.
	 * It must be compatible to the ones belonging to {@link #getClassFrom()}.
	 */
	public void setKeyAttributesFrom(Set<IAttributeDescriptor> keyAttributes);
	
	/**
	 * Obtains the primary key attributes of the "to" class
	 * (class that this foreign key points to).
	 * It must be compatible to the ones belonging to {@link #getClassTo()}.
	 * @return a set of attributes belonging to this primary key
	 */
	public Set<IAttributeDescriptor> getKeyAttributesTo();
	
	/**
	 * Sets the primary key attributes of the "to" class
	 * (class that this foreign key points to).
	 * @param keyAttributes : a set of attributes belonging to this primary key
	 * It must be compatible to the ones belonging to {@link #getClassTo()}.
	 */
	public void setKeyAttributesTo(Set<IAttributeDescriptor> keyAttributes);
}
