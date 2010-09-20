/**
 * 
 */
package unbbayes.prs.prm.cpt;

import java.util.List;

import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IDependencyChain;

/**
 * This class represents an aggregate function "maximum value", which
 * returns the biggest value of a given collection, using compareTo() method
 * of comparables.
 * It considers null values as non-existing (if there is a input having)
 * {@link IAttributeValue#getValue()} == null, it considers as it is not
 * present.
 * @author Shou Matsumoto
 *
 */
public class AggregateFunctionMax  extends AbstractAggregateFunction {

	public static final String DEFAULT_NAME = "Max";
	
	/**
	 * A constructor is visible for subclasses in order to allow
	 * inheritance
	 */
	protected AggregateFunctionMax() {
		this.setName(DEFAULT_NAME);
	}
	
	/**
	 * Default constructor method
	 * @param relatedChain : the dependency chain where this function must be attached to.
	 * @return a new instance
	 */
	public static AggregateFunctionMax newInstance(IDependencyChain relatedChain) {
		AggregateFunctionMax ret = new AggregateFunctionMax();
		ret.setDependencyChain(relatedChain);
		return ret;
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#evaluate(java.util.List)
	 */
	public IAttributeValue evaluate(List<IAttributeValue> parents) {
		// initial assertion
		if (parents == null) {
			return null;
		}
		
		IAttributeValue ret = null;
		for (IAttributeValue attributeValue : parents) {
			if (attributeValue == null || attributeValue.getValue() == null) {
				continue;	// ignore null values
			} else if (ret == null ) {
				ret = attributeValue;	// this is the first valid value found
			} else if (attributeValue.getValue().compareToIgnoreCase(ret.getValue()) > 0) {
				ret = attributeValue;
			}
		}
		
		return ret;
	}


}
