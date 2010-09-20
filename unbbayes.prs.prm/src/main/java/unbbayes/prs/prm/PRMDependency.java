/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.prm.cpt.IPRMCPT;
import unbbayes.prs.prm.cpt.PRMCPT;

/**
 * Default implementation of {@link IPRMDependency}
 * @author Shou Matsumoto
 *
 */
public class PRMDependency implements IPRMDependency {

	private IAttributeDescriptor attributeDescriptor;
	private IPRMCPT prmCPT;
	private List<IDependencyChain> dependencyChains;
	private List<IDependencyChain> incomingDependencyChains;
	
	/**
	 * At least one constructor must be visible to subclasses to allow
	 * inheritance.
	 */
	protected PRMDependency() {
		this.dependencyChains = new ArrayList<IDependencyChain>();
		this.incomingDependencyChains = new ArrayList<IDependencyChain>();
		this.prmCPT = PRMCPT.newInstance(this);
	}
	
	/**
	 * Constructor method using param
	 * @param attributeDescriptor
	 * @return
	 */
	public static PRMDependency newInstance(IAttributeDescriptor attributeDescriptor) {
		PRMDependency ret = new PRMDependency();
		ret.attributeDescriptor = attributeDescriptor;
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#getAttributeDescriptor()
	 */
	public IAttributeDescriptor getAttributeDescriptor() {
		return this.attributeDescriptor;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#getCPT()
	 */
	public IPRMCPT getCPT() {
		return this.prmCPT;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#getDependencyChains()
	 */
	public List<IDependencyChain> getDependencyChains() {
		return this.dependencyChains;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#setAttributeDescriptor(unbbayes.prs.prm.IAttributeDescriptor)
	 */
	public void setAttributeDescriptor(IAttributeDescriptor attributeDescriptor) {
		this.attributeDescriptor = attributeDescriptor;
		if (this.attributeDescriptor != null && !this.equals(this.attributeDescriptor.getPRMDependency())) {
			this.attributeDescriptor.setPRMDependency(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#setCPT(unbbayes.prs.prm.cpt.IPRMCPT)
	 */
	public void setCPT(IPRMCPT cpt) {
		this.prmCPT = cpt;
		if (this.prmCPT != null & !this.equals(this.prmCPT.getPRMDependency())) {
			this.prmCPT.setPRMDependency(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#setDependencyChains(java.util.List)
	 */
	public void setDependencyChains(List<IDependencyChain> dependencyChains) {
		this.dependencyChains = dependencyChains;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#getIncomingDependencyChains()
	 */
	public List<IDependencyChain> getIncomingDependencyChains() {
		return incomingDependencyChains;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMDependency#setIncomingDependencyChains(java.util.List)
	 */
	public void setIncomingDependencyChains(
			List<IDependencyChain> incomingDependencyChains) {
		incomingDependencyChains = incomingDependencyChains;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IPRMDependency) {
			if (this.getAttributeDescriptor() != null
					&& ((IPRMDependency)obj).getAttributeDescriptor() != null) {
				return super.equals(obj) || this.getAttributeDescriptor().equals(((IPRMDependency)obj).getAttributeDescriptor());
			}
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (this.getAttributeDescriptor() != null) {
			return this.getAttributeDescriptor().toString();
		}
		return super.toString();
	}

	
	
}
