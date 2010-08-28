/**
 * 
 */
package unbbayes.prs.prm.builders;

import unbbayes.prs.prm.AttributeDescriptor;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.PRMClass;

/**
 * Default builder for {@link AttributeDescriptor}
 * @author Shou Matsumoto
 *
 */
public class AttributeDescriptorBuilder implements IAttributeDescriptorBuilder {

	private int counter = 0;
	
	private String name = "NewAttribute";
	
	
	/**
	 * Default constructor 
	 */
	public AttributeDescriptorBuilder() {
	}
	
	/**
	 * Default constructor initializing fields.
	 * @param name : the default name for new {@link AttributeDescriptor}
	 * @param counter : counter to be appended to name for new {@link AttributeDescriptor}
	 */
	public AttributeDescriptorBuilder(String name, int counter) {
		this.counter = counter;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.builders.IAttributeDescriptorBuilder#buildPRMAttributeDescriptor(unbbayes.prs.prm.IPRMClass)
	 */
	public IAttributeDescriptor buildPRMAttributeDescriptor(IPRMClass prmClass) {
		this.counter++;
		return AttributeDescriptor.newInstance(prmClass, this.getName() + this.getCounter());
	}

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


}
