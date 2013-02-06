package unbbayes.prm.util;

import static org.junit.Assert.assertTrue;

import java.util.List;

public class ValidatorHelper {

	/**
	 * Validate a list.
	 * 
	 * @param comparableValues
	 * @param rightValues
	 */
	public static void validateStringList(List<?> comparableValues,
			String[] rightValues) {

		assertTrue(comparableValues.size() == rightValues.length);

		for (String nodeName : rightValues) {
			boolean nodeExists = false;

			for (Object node : comparableValues) {
				if (node.toString().equals(nodeName)) {
					nodeExists = true;

					// To make the algorithm faster.
					comparableValues.remove(node);
					break;
				}
			}
			
			if(!nodeExists){
				System.out.println("Different: "+ nodeName);
			}
			
			assertTrue(nodeExists);
		}
	}
}
