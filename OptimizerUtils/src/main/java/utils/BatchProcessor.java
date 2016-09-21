package utils;
import unbbayes.util.Debug;


/**
 * This class simply batch-executes {@link SimulatedUserStatisticsCalculator#main(String[])}
 * @author Shou Matsumoto
 *
 */
public class BatchProcessor {

	public BatchProcessor() {}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Debug.setDebug(false);	// configure debug mode
		
		if (args == null || args.length <= 0) {
			System.out.println("Arguments: (<directory> <num indicators>)+");
		}
		if (args.length % 2 != 0) {
			System.out.println("Arguments must be pairs of <directory> and <num indicators>");
		}
		
		String[] calculatorArgs = new String[15];
		

		// execute calculator without sub-sampling
		for (int i = 0; i < args.length; i+=2) {	// read argument i and i+1
			if (args[i] == null || args[i].trim().isEmpty()
					|| args[i+1] == null || args[i+1].trim().isEmpty()) {
				System.out.println("Empty argument found at index " + i + ", " + (i+1) + ". Ignoring...");
				continue;
			}
			
			// -alert "Alert" -i "RCP4_DEAC_TONY" -o "RCP4_DEAC_TONY_NoSS" -c .6 -p -numI 4 -alertSample "-1" -all -d
			calculatorArgs[0] = "-alert";	calculatorArgs[1] = "\"Alert\"";
			calculatorArgs[2] = "-i";		calculatorArgs[3] = "\"" + args[i] + "\"";
			calculatorArgs[4] = "-o";		calculatorArgs[5] = "\"" + args[i] + "_NoSS\"";
			calculatorArgs[6] = "-c";		calculatorArgs[7] = "\".6\"";
			calculatorArgs[8] = "-p";
			calculatorArgs[9] = "-numI";	calculatorArgs[10] = "\"" + args[i+1] + "\"";
			calculatorArgs[11] = "-alertSample";	calculatorArgs[12] = "\"-1\"";
			calculatorArgs[13] = "-all";
			calculatorArgs[14] = Debug.isDebugMode()?"-d":"";
			
			System.out.print("Executing calculator, without sub-sampling:");
			for (String arg : calculatorArgs) {
				System.out.print(" " + arg);
			}
			System.out.println("");
			
			// Execute calculator. 
			long time = System.currentTimeMillis();
			SimulatedUserStatisticsCalculator.main(calculatorArgs);
			System.out.println("Took " + (System.currentTimeMillis() - time) + "ms.");
		}
		
		// execute calculator with sub-sampling
		for (int i = 0; i < args.length; i+=2) {	// read argument i and i+1
			// -alert "Alert" -i "RCP4_DEAC_TONY" -o "RCP4_DEAC_TONY_SS" -c .6 -p -numI 4 -alertSample "30" -all -d
			calculatorArgs[0] = "-alert";	calculatorArgs[1] = "\"Alert\"";
			calculatorArgs[2] = "-i";		calculatorArgs[3] = "\"" + args[i] + "\"";
			calculatorArgs[4] = "-o";		calculatorArgs[5] = "\"" + args[i] + "_SS\"";
			calculatorArgs[6] = "-c";		calculatorArgs[7] = "\".6\"";
			calculatorArgs[8] = "-p";
			calculatorArgs[9] = "-numI";	calculatorArgs[10] = "\"" + args[i+1] + "\"";
			calculatorArgs[11] = "-alertSample";	calculatorArgs[12] = "\"30\"";
			calculatorArgs[13] = "-all";
			calculatorArgs[14] = Debug.isDebugMode()?"-d":"";
			
			System.out.print("Executing calculator, with sub-sampling:");
			for (String arg : calculatorArgs) {
				System.out.print(" " + arg);
			}
			System.out.println("");
			
			// Execute calculator. 
			long time = System.currentTimeMillis();
			SimulatedUserStatisticsCalculator.main(calculatorArgs);
			System.out.println("Took " + (System.currentTimeMillis() - time) + "ms.");
		}
		
	}

}
