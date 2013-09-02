/**
 * 
 */
package edu.gmu.ace.scicast.autotrader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class CSVHypothesysReaderTest extends TestCase {
	
	private static String bnName = "GreekExit.xml";

	private static String[] outputs = {"hypothesisCurrent.csv","gapCurrent.csv"};
	private static Map<String, String> nodeNameToCSVFileMap = new HashMap<String, String>();
	static {
		nodeNameToCSVFileMap.put("Withdraw", "244-treated.csv");
		nodeNameToCSVFileMap.put("Germany", "245-treated.csv");
		nodeNameToCSVFileMap.put("Hypothesis", "246-treated.csv");
	}
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	/**
	 * @param name
	 */
	public CSVHypothesysReaderTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Performs the following procedure:
	 * <br/><br/>
	 * 1. Have the BN read the estimates as likelihoods from the 196-treated.csv 
	 * (for the withdraw node, ordered by date and containing only the values at the end of the date) 
	 * and 197-treated.csv file (for Germany node, ordered by date and containing only the values at the end of the date); 
	 * <br/>
	 * 2. Compute the Hypothesis node of probabilities given the likelihoods from the csv files (should have all dates from both files).
	 * <br/>
	 * 3. Write this in a hypothesis.csv file (should have 2 columns: date, propagated value)
	 */
	public final void testWriteHypothesisCSV() {
		System.out.println("\n\n[Writing "+outputs[0]+"]");
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource(bnName).toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// algorithm to add likelihood and propagate probabilities in net
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm(net);
		((JunctionTreeAlgorithm)algorithm).setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
		algorithm = AssetAwareInferenceAlgorithm.getInstance(algorithm);
		((AssetAwareInferenceAlgorithm)algorithm).setToUpdateAssets(false);
//		algorithm.setInferenceAlgorithmListeners((List)Collections.emptyList());	// clear all special configuration of junction tree algorithm
		assertNotNull(algorithm);
		
		try {
			// compile the main bayes net
			algorithm.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// prepare the nodes to be used
		TreeVariable withdrawNode = (TreeVariable) net.getNode("Withdraw");
		TreeVariable germanyNode = (TreeVariable) net.getNode("Germany");
		TreeVariable hypothesisNode = (TreeVariable) net.getNode("Hypothesis");
		assertNotNull(withdrawNode);
		assertNotNull(germanyNode);
		assertNotNull(hypothesisNode);
		
		
		// this is the output file
		PrintStream hypothesisCSV = null;
		try {
			hypothesisCSV = new PrintStream(new FileOutputStream(new File(outputs[0])));
			// 1st line has the name of the attributes
			hypothesisCSV.println("date,propagated value");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the likelihoods of the Withdraw node
		CSVReader withdrawNodeReader = null;
		try {
			withdrawNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource(nodeNameToCSVFileMap.get("Withdraw")).toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the csv file which contains the likelihoods of the Germany node
		CSVReader germanyNodeReader = null;
		try {
			germanyNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource(nodeNameToCSVFileMap.get("Germany")).toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		
		// these vars will contain the lines read from the csv files
		String [] withdrawNodeLine = null;	// the line read from withdrawNodeReader
		String [] germanyNodeLine = null;	// the line read from germanyNodeReader
		
		boolean isToTreatWithdrawNode = true;	// if false, withdrawNode will not be updated in current iteration (due to different date)
		boolean isToTreatGermanyNode = true;	// if false, germanyNode will not be updated in current iteration (due to different date)
		
		// object to convert string to date
		
		// treat each line
		for (int iteration = 0; iteration < Integer.MAX_VALUE; iteration++) {
			System.out.println("[Iteration "+iteration+"]");
			// read lines
			try {
				if (isToTreatWithdrawNode) {
					withdrawNodeLine = withdrawNodeReader.readNext();
				}
				if (isToTreatGermanyNode) {
					germanyNodeLine = germanyNodeReader.readNext();
				}
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
			// the only end condition is that both csv reached end of file
			if (withdrawNodeLine == null && germanyNodeLine == null){
				System.out.println("[End of iteration]");
				break;
			}
			
			// date to be written in hypothesisCSV
			String dateOfHypothesysCSV = null;	
			
			// update isToReadWithDrawNodeLine and isToReadGermanyNodeLine and dateOfHypothesysCSV
			try {
				// Note: withdrawNodeLine and germanyNodeLine are not null simultaneously
				if (withdrawNodeLine == null) {
					// use germany
					isToTreatWithdrawNode = false;
					isToTreatGermanyNode = true;
					dateOfHypothesysCSV = germanyNodeLine[0];
					System.out.println("Date from germany: ["+dateOfHypothesysCSV+"]");
				} else if (germanyNodeLine == null) {
					// use withdraw
					isToTreatWithdrawNode = true;
					isToTreatGermanyNode = false;
					dateOfHypothesysCSV = withdrawNodeLine[0];
					System.out.println("Date from withdraw: ["+dateOfHypothesysCSV+"]");
				} else {	// compare dates of both lines
					// convert string to date, so that we can compare the day easily
					Date withdrawDate = dateFormat.parse(withdrawNodeLine[0]);
					Date germanyDate = dateFormat.parse(germanyNodeLine[0]);
					if (!withdrawDate.equals(germanyDate)) {
						// dates are different. 
						if (withdrawDate.before(germanyDate)) {
							// we must only treat withdraw node, and the next iteration should not re-read germany node
							isToTreatWithdrawNode = true;
							isToTreatGermanyNode = false;
							dateOfHypothesysCSV = withdrawNodeLine[0];
							System.out.println("Date from withdraw: ["+dateOfHypothesysCSV+"]");
						} else {
							// we must only treat germany node, and the next iteration should not re-read withdraw node
							isToTreatWithdrawNode = false;
							isToTreatGermanyNode = true;
							dateOfHypothesysCSV = germanyNodeLine[0];
							System.out.println("Date from germany: ["+dateOfHypothesysCSV+"]");
						}
					} else {
						// dates are equal. Both nodes should be treated
						isToTreatWithdrawNode = true;
						isToTreatGermanyNode = true;
						dateOfHypothesysCSV = withdrawNodeLine[0];	// pick any
						System.out.println("Date from both: ["+dateOfHypothesysCSV+"]");
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
			// add likelihoods
			if (isToTreatWithdrawNode) {
				// lines are written in percent. Convert to value within 0 and 1
				float prob = Float.parseFloat(withdrawNodeLine[1])/100f; 
				float[] likelihood = {prob, 1-prob};
				withdrawNode.addLikeliHood(likelihood);
				System.out.println("Read = ["+withdrawNodeLine[1]+"]. Likelihood of withdraw = [" + likelihood[0]+" , " + likelihood[1]+"]");
			}
			if (isToTreatGermanyNode) {
				// lines are written in percent. Convert to value within 0 and 1
				float prob = Float.parseFloat(germanyNodeLine[1])/100f; 
				float[] likelihood = {prob, 1-prob};
				germanyNode.addLikeliHood(likelihood);
				System.out.println("Read = ["+germanyNodeLine[1]+"]. Likelihood of germany = [" + likelihood[0]+" , " + likelihood[1]+"]");
			}
			
			// propagate evidence
			algorithm.propagate();
			
			// make sure the next iteration has no garbage likelihood
			assertFalse(withdrawNode.hasLikelihood());
			assertFalse(germanyNode.hasLikelihood());
			
			// write to hypothesisCSV
			hypothesisCSV.println(dateOfHypothesysCSV+ "," +hypothesisNode.getMarginalAt(0)*100);
			
			// trace marginal probabilities
			System.out.println(withdrawNode.getName() + " : " + (withdrawNode.getMarginalAt(0)*100)+"%");
			System.out.println(germanyNode.getName() + " : " + (germanyNode.getMarginalAt(0)*100)+"%");
			System.out.println(hypothesisNode.getName() + " : " + (hypothesisNode.getMarginalAt(0)*100)+"%");
			
			// clear virtual nodes (absorb likelihood evidence).
//			algorithm.clearVirtualNodes(); 
		}
	}
	
	/**
	 * 4. Compare the hypothesis.csv values with the 109-treated.csv values. 
	 * Write the differences in a new gap.csv file (date, gap) (should have all dates from both files). 
	 */
	public final void testWriteGapCSV() {
		System.out.println("\n\n[Writing "+outputs[1]+"]");
		
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource(bnName).toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// algorithm to add likelihood and propagate probabilities in net
		JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm(net);
//		algorithm.setInferenceAlgorithmListeners((List)Collections.emptyList());	// clear all special configuration of junction tree algorithm
		assertNotNull(algorithm);
		
		try {
			// compile the main bayes net
			algorithm.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// this is the output file
		PrintStream gapCSV = null;
		try {
			gapCSV = new PrintStream(new FileOutputStream(new File(outputs[1])));
			// 1st line has the name of the attributes
			gapCSV.println("date,gap");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the probabilities of the hypothesis node from the market
		CSVReader marketReader = null;
		try {
			marketReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource(nodeNameToCSVFileMap.get("Hypothesis")).toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the hypothesis.csv file (the infered values)
		CSVReader inferredReader = null;
		try {
			inferredReader = new CSVReader(new FileReader(new File(outputs[0])));
			// the 1st line contains the name of the attributes, so skip it
			inferredReader.readNext();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		
		// these vars will contain the lines read from the csv files
		String [] inferredLine = null;	
		String [] marketLine = null;	
		
		boolean isToTreatInferredLine = true;	
		boolean isToTreatMarketLine = true;	
		
		
		// the values to compare
		float inferredValue = ((TreeVariable)net.getNode("Hypothesis")).getMarginalAt(0)*100;	// initialize to the value in bayes net
		float marketValue = inferredValue;
		
		// treat each line
		for (int iteration = 0; iteration < Integer.MAX_VALUE; iteration++) {
			System.out.println("[Iteration "+iteration+"]");
			// read lines
			try {
				if (isToTreatInferredLine) {
					inferredLine = inferredReader.readNext();
				}
				if (isToTreatMarketLine) {
					marketLine = marketReader.readNext();
				}
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
			// the only end condition is that both csv reached end of file
			if (inferredLine == null && marketLine == null){
				System.out.println("[End of iteration]");
				break;
			}
			
			// date to be written in gap.csv
			String dateToWrite = null;	
			
			// update isToTreatInferedLine and isToTreatMarketLine and dateToWrite
			try {
				if (inferredLine == null) {
					// use market
					isToTreatInferredLine = false;
					isToTreatMarketLine = true;
					dateToWrite = marketLine[0];
					System.out.println("Date from market: ["+dateToWrite+"]");
					marketValue = Float.parseFloat(marketLine[1]);
				} else if (marketLine == null) {
					// use inferred
					isToTreatInferredLine = true;
					isToTreatMarketLine = false;
					dateToWrite = inferredLine[0];
					System.out.println("Date from inference: ["+dateToWrite+"]");
					inferredValue = Float.parseFloat(inferredLine[1]);
				} else {	// compare dates of both lines
					// convert string to date, so that we can compare the day easily
					Date inferredDate = dateFormat.parse(inferredLine[0]);
					Date marketDate = dateFormat.parse(marketLine[0]);
					if (!inferredDate.equals(marketDate)) {
						// dates are different. 
						if (inferredDate.before(marketDate)) {
							// we must only treat withdraw node, and the next iteration should not re-read germany node
							isToTreatInferredLine = true;
							isToTreatMarketLine = false;
							dateToWrite = inferredLine[0];
							System.out.println("Date from inference: ["+dateToWrite+"]");
							inferredValue = Float.parseFloat(inferredLine[1]);
						} else {
							// we must only treat germany node, and the next iteration should not re-read withdraw node
							isToTreatInferredLine = false;
							isToTreatMarketLine = true;
							dateToWrite = marketLine[0];
							System.out.println("Date from market: ["+dateToWrite+"]");
							marketValue = Float.parseFloat(marketLine[1]);
						}
					} else {
						// dates are equal. Both nodes should be treated
						isToTreatInferredLine = true;
						isToTreatMarketLine = true;
						dateToWrite = inferredLine[0];	// pick any
						System.out.println("Date from both: ["+dateToWrite+"]");
						inferredValue = Float.parseFloat(inferredLine[1]);
						marketValue = Float.parseFloat(marketLine[1]);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
			
			// write to output
			gapCSV.println(dateToWrite+ "," +(inferredValue-marketValue));
			
			// trace values
			System.out.println("Inferred : " + inferredValue);
			System.out.println("Market : " + marketValue);
			System.out.println("Gap : " + (inferredValue-marketValue));
			
			// clear virtual nodes (absorb likelihood evidence).
//			algorithm.clearVirtualNodes(); 
		}
	}

	/**
	 * @return the bnName
	 */
	public static String getBnName() {
		return bnName;
	}

	/**
	 * @param bnName the bnName to set
	 */
	public static void setBnName(String bnName) {
		CSVHypothesysReaderTest.bnName = bnName;
	}

	/**
	 * @return the outputs
	 */
	public static String[] getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public static void setOutputs(String[] outputs) {
		CSVHypothesysReaderTest.outputs = outputs;
	}

	/**
	 * @return the nodeNameToCSVFileMap
	 */
	public static Map<String, String> getNodeNameToCSVFileMap() {
		return nodeNameToCSVFileMap;
	}

	/**
	 * @param nodeNameToCSVFileMap the nodeNameToCSVFileMap to set
	 */
	public static void setNodeNameToCSVFileMap(
			Map<String, String> nodeNameToCSVFileMap) {
		CSVHypothesysReaderTest.nodeNameToCSVFileMap = nodeNameToCSVFileMap;
	}

	/**
	 * @return the dateFormat
	 */
	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public static void setDateFormat(DateFormat dateFormat) {
		CSVHypothesysReaderTest.dateFormat = dateFormat;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String defaultArgs[] = {"GreekExit.xml","244-treated.csv","245-treated.csv","246-treated.csv", "hypothesis.csv", "gap.csv", "yyyy/MM/dd HH:mm:ss"};
			if (args == null || args.length <= 0) {
				args = defaultArgs;
			}
			CSVHypothesysReaderTest test = new CSVHypothesysReaderTest("testWriteHypothesisCSV");
			CSVHypothesysReaderTest test2 = new CSVHypothesysReaderTest("testWriteGapCSV");

			
			if (args.length != 7 ) {
				System.out.println("Usage:");
				System.out.println("java -jar csvhypothesis GreekExit.xml 244-treated.csv 245-treated.csv 246-treated.csv hypothesis.csv gap.csv \"yyyy/MM/dd HH:mm:ss\"");
			}
			bnName = args[0];
			nodeNameToCSVFileMap.put("Withdraw", args[1]);
			nodeNameToCSVFileMap.put("Germany", args[2]);
			nodeNameToCSVFileMap.put("Hypothesis", args[3]);
			outputs[0] = args[4];
			outputs[1] = args[5];
			dateFormat = new SimpleDateFormat(args[6]);
			
			TestResult result = test.run();
			if (!result.wasSuccessful()) {
				System.err.println("Error on test: " + test);
				return ;
			} 
			TestResult result2 = test2.run();
			if (!result2.wasSuccessful()) {
				System.err.println("Error on test: " + test2);
				return ;
			} 
			System.out.println("Success.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
