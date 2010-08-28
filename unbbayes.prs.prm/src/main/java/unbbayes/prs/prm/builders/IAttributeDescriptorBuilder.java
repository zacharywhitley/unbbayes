package unbbayes.prs.prm.builders;

import unbbayes.prs.prm.AttributeDescriptor;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IPRMClass;

public interface IAttributeDescriptorBuilder {

	/**
	 * Builds a new instance of {@link AttributeDescriptor} using 
	 * {@link #getName()} + {@link #getCounter()} as its name
	 * @param prmClass : the class where this new attribute will belong.
	 * @return
	 */
	public abstract IAttributeDescriptor buildPRMAttributeDescriptor(IPRMClass prmClass);

}