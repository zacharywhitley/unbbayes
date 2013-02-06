package unbbayes.prm.controller.prm;

import unbbayes.prm.model.AggregateFunctionName;

/**
 * 
 * @author David Saldaña
 * 
 */
public class AggregateFunction {

	public static float calculate(AggregateFunctionName af, float num1,
			float num2) throws Exception {			
		
		switch (af) {
		// case mode:
		// break;
		case min:
			return Math.min(num1, num2);
		case max:
			return Math.max(num1, num2);
		case mean:
			return (num1 + num2) / 2;
			// case median:
			// return (num1 + num2) / 2;
		case add:
			return num1 + num2;
		case subtract:
			return num1 - num2;
		default:
			throw new Exception("Unsupported aggregate function");
		}
	}
}
