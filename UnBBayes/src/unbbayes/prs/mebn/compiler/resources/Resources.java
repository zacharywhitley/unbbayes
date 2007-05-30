package unbbayes.prs.mebn.compiler.resources;

import java.util.*;

/**
 * @author Laecio Lima dos Santos
 * @version 1.0
 * @since 12/04/2006
 */

public class Resources extends ListResourceBundle {

    /**
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * The resources
	 */
	static final Object[][] contents =
	{	{"NoDefaultDistributionDeclared","A default distribution (else clause) must be declared within table."}, 
		{"InvalidConditionantFound","An invalid conditionant was found within the table declaration."},
		{"InvalidProbabilityRange","The probability distribution is invalid (the sum must be 1)."},
		{"SomeStateUndeclared","All possible states of this node must have an associated probability."},
		{"UnexpectedTokenFound","Unexpected token found."}
		
	};
}
