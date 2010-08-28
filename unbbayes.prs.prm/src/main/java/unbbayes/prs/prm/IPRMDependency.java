/**
 * 
 */
package unbbayes.prs.prm;

import java.util.List;

import unbbayes.prs.prm.cpt.IPRMCPT;

/**
 * Represents probabilistic dependencies between entities
 * and attributes of a PRM.
 * 
 * It basically separates the concept of attributes from random variables.
 * Every information about randomicity of an attribute will be stored here.
 * @author Shou Matsumoto
 *
 */
public interface IPRMDependency {
	
	/**
	 * Obtains the container (the attribute which contains this random information)
	 * @return
	 */
	public IAttributeDescriptor getAttributeDescriptor();
	
	/**
	 * Sets the container (the attribute which contains this random information)
	 * @param attributeDescriptor
	 */
	public void setAttributeDescriptor(IAttributeDescriptor  attributeDescriptor);
	
	/**
	 * Obtains the CPT of this PRM attribute. If null, then
	 * this attribute is not a random variable.
	 * @return
	 */
	public IPRMCPT getCPT();

	/**
	 * Sets the CPT of this PRM attribute. If null, then
	 * this attribute is not a random variable.
	 * @param cpt
	 */
	public void setCPT(IPRMCPT cpt);
	
	/**
	 * Obtains the dependency chains (that is, the children of a probabilistic node). Since
	 * in PRM a parent is bound to FK reference chain, we do not 
	 * point directly to a parent (a parent is usually another {@link IAttributeDescriptor}).
	 * @return
	 */
	public List<IDependencyChain> getDependencyChains();
	
	/**
	 * Sets the dependency chains (that is, the children of a probabilistic node). Since
	 * in PRM a parent is bound to FK reference chain, we do not 
	 * point directly to a parent (a parent is usually another {@link IAttributeDescriptor}).
	 * @param dependencyChains
	 */
	public void setDependencyChains(List<IDependencyChain> dependencyChains);
	
	/**
	 * Obtains the incoming dependency chains (that is, the parents of a probabilistic node). Since
	 * in PRM a parent is bound to FK reference chain, we do not 
	 * point directly to a parent (a parent is usually another {@link IAttributeDescriptor}).
	 * @return
	 */
	public List<IDependencyChain> getIncomingDependencyChains();
	
	/**
	 * Sets the incoming dependency chains (that is, the parents of a probabilistic node). Since
	 * in PRM a parent is bound to FK reference chain, we do not 
	 * point directly to a parent (a parent is usually another {@link IAttributeDescriptor}).
	 * @param dependencyChains
	 */
	public void setIncomingDependencyChains(List<IDependencyChain> dependencyChains);
}
