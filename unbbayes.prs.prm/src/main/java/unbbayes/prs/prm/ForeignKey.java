/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link IForeignKey}
 * @author Shou Matsumoto
 *
 */
public class ForeignKey implements IForeignKey {

	private String name = "FK";
	private Set<IAttributeDescriptor> keyAttributesFrom;
	private Set<IAttributeDescriptor> keyAttributesTo;
	
	private IPRMClass classFrom, classTo;
	
	private List<IDependencyChain> dependencyChain;
	
	/**
	 *  At least one constructor must be visible for subclasses to
	 * allow inheritance
	 */
	protected ForeignKey() {
		this.keyAttributesFrom = new HashSet<IAttributeDescriptor>();
		this.keyAttributesTo = new HashSet<IAttributeDescriptor>();
		this.dependencyChain = new ArrayList<IDependencyChain>();
	}
	
	/**
	 * Default construction method
	 * @return
	 */
	public static ForeignKey newInstance() {
		ForeignKey ret = new ForeignKey();
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#getClassFrom()
	 */
	public IPRMClass getClassFrom() {
		return this.classFrom;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#getClassTo()
	 */
	public IPRMClass getClassTo() {
		return this.classTo;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#getDependencyChain()
	 */
	public List<IDependencyChain> getDependencyChain() {
		return dependencyChain;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#getKeyAttributesFrom()
	 */
	public Set<IAttributeDescriptor> getKeyAttributesFrom() {
		return this.keyAttributesFrom;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#getKeyAttributesTo()
	 */
	public Set<IAttributeDescriptor> getKeyAttributesTo() {
		return this.keyAttributesTo;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#setClassFrom(unbbayes.prs.prm.IPRMClass)
	 */
	public void setClassFrom(IPRMClass prmClassFrom) {
		this.classFrom = prmClassFrom;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#setClassTo(unbbayes.prs.prm.IPRMClass)
	 */
	public void setClassTo(IPRMClass prmClassTo) {
		if (this.classTo != null) {
			this.classTo.getIncomingForeignKeys().remove(this);
		}
		this.classTo = prmClassTo;
		if (this.classTo != null 
				&& this.classTo.getIncomingForeignKeys() != null 
				&& !this.classTo.getIncomingForeignKeys().contains(this)) {
			this.classTo.getIncomingForeignKeys().add(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#setDependencyChain(java.util.List)
	 */
	public void setDependencyChain(List<IDependencyChain> dependencyChains) {
		this.dependencyChain = dependencyChain;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#setKeyAttributesFrom(java.util.Set)
	 */
	public void setKeyAttributesFrom(Set<IAttributeDescriptor> keyAttributes) {
		if (this.keyAttributesFrom != null) {
			// remove inverse link
			for (IAttributeDescriptor attributeDescriptor : this.keyAttributesFrom ) {
				if (this.equals(attributeDescriptor.getForeignKeyReference())) {
					attributeDescriptor.setForeignKeyReference(null);
				}
			}
		}
		this.keyAttributesFrom = keyAttributes;
		if (this.keyAttributesFrom != null) {
			// add inverse link
			for (IAttributeDescriptor attributeDescriptor : this.keyAttributesFrom ) {
				if (!this.equals(attributeDescriptor.getForeignKeyReference())) {
					attributeDescriptor.setForeignKeyReference(this);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#setKeyAttributesTo(java.util.Set)
	 */
	public void setKeyAttributesTo(Set<IAttributeDescriptor> keyAttributes) {
		this.keyAttributesTo = keyAttributes;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IForeignKey#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IForeignKey && this.getName() != null) {
			return super.equals(obj) || this.getName().equals(((IForeignKey)obj).getName());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String ret =  this.getName() + " : ";
		if (this.getKeyAttributesFrom() != null && !this.getKeyAttributesFrom().isEmpty()) {
			ret += this.getClassFrom().getName() + " ({";
			Iterator<IAttributeDescriptor> iterator = this.getKeyAttributesFrom().iterator();
			while (iterator.hasNext()) {
				IAttributeDescriptor key = iterator.next();
				ret += key.getName();
				if (iterator.hasNext()) {
					ret += ", ";
				}
			}
			ret += "})";
		}
		if (this.getKeyAttributesTo() != null && !this.getKeyAttributesTo().isEmpty()) {
			ret += " => ";
			ret += this.getClassTo().getName() + "({";
			Iterator<IAttributeDescriptor> iterator = this.getKeyAttributesTo().iterator();
			while (iterator.hasNext()) {
				IAttributeDescriptor key = iterator.next();
				ret += key.getName();
				if (iterator.hasNext()) {
					ret += ", ";
				}
			}
			ret += "})";
		}
		return ret;
	}

	
	
}
