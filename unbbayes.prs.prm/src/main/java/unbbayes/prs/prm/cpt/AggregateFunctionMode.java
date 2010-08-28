/**
 * 
 */
package unbbayes.prs.prm.cpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IDependencyChain;

/**
 * This class represents an aggregate function "mode", which
 * returns the most frequent value of a given collection.
 * If no mode is present (there are 2 or more values with the same
 * frequency), then return the last one found.
 * It considers null values as non-existing (if there is a input having)
 * {@link IAttributeValue#getValue()} == null, it considers as it is not
 * present.
 * @author Shou Matsumoto
 *
 */
public class AggregateFunctionMode implements IAggregateFunction {

	private String name = "Mode";
	private IDependencyChain relatedChain;

	/**
	 * A constructor is visible for subclasses in order to allow
	 * inheritance
	 */
	protected AggregateFunctionMode() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method
	 * @param relatedChain : the dependency chain where this function must be attached to.
	 * @return a new instance
	 */
	public static AggregateFunctionMode newInstance(IDependencyChain relatedChain) {
		AggregateFunctionMode ret = new AggregateFunctionMode();
		ret.relatedChain = relatedChain;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#evaluate(java.util.List)
	 */
	public IAttributeValue evaluate(List<IAttributeValue> values){
		
		// input assertion
		if (values == null || values.isEmpty()) {
			// nothing to evaluate
			return null;
		}
		
		// use a list ignoring null values
		List<IAttributeValue> nonNullValues = new ArrayList<IAttributeValue>();
		for (IAttributeValue val : values) {
			// filter null values
			if (val != null && val.getValue() != null) {
				nonNullValues.add(val);
			}
		}
		
		// asserting existence of input
		if (nonNullValues.isEmpty()) {
			return null;
		}
		
		// a map to count value's frequency
		Map<String, Integer> counterMap = new HashMap<String, Integer>();
		// a map from IAttributeValue#getValue() to the last IAttributeValue in values
		Map<String, IAttributeValue> valueToIAttributeMap = new HashMap<String, IAttributeValue>(); 
		// frequency counter
		Integer counter = 1; 
		
		// fill valueToIAttributeMap 
		for (IAttributeValue attributeValue : nonNullValues) {
			if (attributeValue.getValue() != null) {
				valueToIAttributeMap.put(attributeValue.getValue(), attributeValue);
			}
		}
		
		// fill counter map
		for (IAttributeValue value : nonNullValues) {
			if (value.getValue() == null) {
				// ignore null value
				continue;
			}
			counter = counterMap.get(value.getValue());
			if (counter == null) {
				// first ocurrence of this value
				counter = 1;
			} else {
				counter++;
			}
			counterMap.put(value.getValue(), counter);			
		}
		
		// most frequent key. Note that the initial assertion grants that values != null and !values.isEmpty()
		String mode = nonNullValues.get(0).getValue();
			
		// get the key with largest counter. 
		// Iterate over values (list), because the order of the values is important when there are 2 or more modes
		for (IAttributeValue val : nonNullValues) {
			if (counterMap.get(val.getValue()) >= counterMap.get(mode)) {
				// substitute ret if |key| >= |ret|
				// we use >= so that the last value is selected when there are more than one mode
				mode = val.getValue();
			}
		}
		
		// return the last element in "values" having #getValue() == mode
		return valueToIAttributeMap.get(mode);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#getDependencyChain()
	 */
	public IDependencyChain getDependencyChain() {
		return this.relatedChain;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#setDependencyChain(unbbayes.prs.prm.IDependencyChain)
	 */
	public void setDependencyChain(IDependencyChain dependencyChain) {
		this.relatedChain = dependencyChain;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IAggregateFunction#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	

}
