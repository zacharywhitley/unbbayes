/**
 * 
 */
package utils;

/**
 * @author Shou Matsumoto
 *
 */
public class UniformNameConverter {

	/**
	 * 
	 */
	public UniformNameConverter() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Converts a string to a internally valid name.
	 * @param nameToConvert
	 * @return : a valid name.
	 */
	public String convertToName(String nameToConvert) {
		// remove white spaces
		nameToConvert = nameToConvert.replaceAll("\\s", "");
		
		if (nameToConvert.matches("det[0-9]+")) {
			return nameToConvert.replaceAll("det", "Detector");
		} else if (nameToConvert.matches("ADD[0-9]+")) {
			return nameToConvert.replaceAll("ADD", "Detector");
		}
		
		return nameToConvert.replaceAll("AlertDaysDetector", "Detector");
	}

}
