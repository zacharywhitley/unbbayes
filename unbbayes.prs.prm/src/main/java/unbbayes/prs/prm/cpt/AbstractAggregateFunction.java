/**
 * 
 */
package unbbayes.prs.prm.cpt;

import unbbayes.prs.prm.IDependencyChain;

/**
 * This class sets up default behaviors for aggregate functions (classes
 * implementing {@link IAggregateFunction})
 * @author Shou Matsumoto
 *
 */
public abstract class AbstractAggregateFunction implements IAggregateFunction {

	private String name = "AbstractAggregateFunction - do not use this";
	private IDependencyChain relatedChain;
	
	/**
	 * Default constructor is protected to allow only inheritance.
	 * Implementors must provide constructor methods (e.g. newInstance())
	 * to create objects.
	 */
	protected AbstractAggregateFunction() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#getDependencyChain()
	 */
	public IDependencyChain getDependencyChain() {
		return this.relatedChain;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#setDependencyChain(unbbayes.prs.prm.IDependencyChain)
	 */
	public void setDependencyChain(IDependencyChain dependencyChain) {
		this.relatedChain = dependencyChain;
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			if (this.getName() != null && this.getName().trim().length() > 0) {
				return this.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.toString();
	}
	
	

}
