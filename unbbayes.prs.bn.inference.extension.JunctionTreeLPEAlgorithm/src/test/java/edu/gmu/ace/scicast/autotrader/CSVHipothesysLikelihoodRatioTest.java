/**
 * 
 */
package edu.gmu.ace.daggre.autotrader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class CSVHipothesysLikelihoodRatioTest extends TestCase {

	/**
	 * @param name
	 */
	public CSVHipothesysLikelihoodRatioTest(String name) {
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
		System.out.println("\n\n[Writing hypothesysLikelihood.csv]");
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource("GreekExit.xml").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// algorithm to add likelihood and propagate probabilities in net
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm(net);
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
			hypothesisCSV = new PrintStream(new FileOutputStream(new File("hypothesisLikelihood.csv")));
			// 1st line has the name of the attributes
			hypothesisCSV.println("date,propagated value");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the likelihoods of the Withdraw node
		CSVReader withdrawNodeReader = null;
		try {
			withdrawNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("196-treated.csv").toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the csv file which contains the likelihoods of the Germany node
		CSVReader germanyNodeReader = null;
		try {
			germanyNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("195-treated.csv").toURI())));
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
//		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		DateFormat dateFormat = DateFormat.getDateInstance();
		
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
					if (withdrawDate.getDate() != germanyDate.getDate()) {
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
		System.out.println("\n\n[Writing gapLikelihood.csv]");
		
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource("GreekExit.xml").toURI()));
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
			gapCSV = new PrintStream(new FileOutputStream(new File("gapLikelihood.csv")));
			// 1st line has the name of the attributes
			gapCSV.println("date,gap");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the probabilities of the hypothesis node from the market
		CSVReader marketReader = null;
		try {
			marketReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("109-treated.csv").toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the hypothesis.csv file (the infered values)
		CSVReader inferredReader = null;
		try {
			inferredReader = new CSVReader(new FileReader(new File("hypothesisLikelihood.csv")));
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
		
		// object to convert string to date
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		
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
					if (inferredDate.getDate() != marketDate.getDate()) {
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
	public final void testWriteHypothesisCSV21True() {
		System.out.println("\n\n[Writing hypothesysLikelihood21True.csv]");
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource("GreekExit.xml").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// algorithm to add likelihood and propagate probabilities in net
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm(net);
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
			hypothesisCSV = new PrintStream(new FileOutputStream(new File("hypothesysLikelihood21True.csv")));
			// 1st line has the name of the attributes
			hypothesisCSV.println("date,propagated value");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the likelihoods of the Withdraw node
		CSVReader withdrawNodeReader = null;
		try {
			withdrawNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("196-treated.csv").toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the csv file which contains the likelihoods of the Germany node
		CSVReader germanyNodeReader = null;
		try {
			germanyNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("195-treated.csv").toURI())));
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
//		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		DateFormat dateFormat = DateFormat.getDateInstance();
		
		// fixed likelihood ratio
		float[] likelihood = {(float) (2.0/3.0), (float) (1.0/3.0)};
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
					if (withdrawDate.getDate() != germanyDate.getDate()) {
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
				withdrawNode.addLikeliHood(likelihood);
				System.out.println("Read = ["+withdrawNodeLine[1]+"]. Likelihood of withdraw = [" + likelihood[0]+" , " + likelihood[1]+"]");
			}
			if (isToTreatGermanyNode) {
				// lines are written in percent. Convert to value within 0 and 1
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
	public final void testWriteGapCSV21True() {
		System.out.println("\n\n[Writing gapLikelihood21True.csv]");
		
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource("GreekExit.xml").toURI()));
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
			gapCSV = new PrintStream(new FileOutputStream(new File("gapLikelihood21True.csv")));
			// 1st line has the name of the attributes
			gapCSV.println("date,gap");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the probabilities of the hypothesis node from the market
		CSVReader marketReader = null;
		try {
			marketReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("109-treated.csv").toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the hypothesis.csv file (the infered values)
		CSVReader inferredReader = null;
		try {
			inferredReader = new CSVReader(new FileReader(new File("hypothesysLikelihood21True.csv")));
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
		
		// object to convert string to date
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		
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
					if (inferredDate.getDate() != marketDate.getDate()) {
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
	public final void testWriteHypothesisCSV21OscilatingLikelihood() {
		System.out.println("\n\n[Writing hypothesysLikelihood21OscilatingLikelihood.csv]");
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource("GreekExit.xml").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// algorithm to add likelihood and propagate probabilities in net
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm(net);
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
			hypothesisCSV = new PrintStream(new FileOutputStream(new File("hypothesisLikelihood21OscilatingLikelihood.csv")));
			// 1st line has the name of the attributes
			hypothesisCSV.println("date,propagated value");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the likelihoods of the Withdraw node
		CSVReader withdrawNodeReader = null;
		try {
			withdrawNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("196-treated.csv").toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the csv file which contains the likelihoods of the Germany node
		CSVReader germanyNodeReader = null;
		try {
			germanyNodeReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("195-treated.csv").toURI())));
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
//		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		DateFormat dateFormat = DateFormat.getDateInstance();
		
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
					if (withdrawDate.getDate() != germanyDate.getDate()) {
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
				if (prob == withdrawNode.getMarginalAt(0)) {
					// don't do anything
				} else {
					float[] likelihood = {.5f, .5f};
					if (prob > withdrawNode.getMarginalAt(0)) {
						likelihood[0] = (float) (2.0/3.0);
						likelihood[1] = (float) (1.0/3.0);
					} else if (prob < withdrawNode.getMarginalAt(0)){
						likelihood[0] = (float) (1.0/3.0);
						likelihood[1] = (float) (2.0/3.0);
					}
					withdrawNode.addLikeliHood(likelihood);
					System.out.println("Read = ["+withdrawNodeLine[1]+"]. Likelihood of withdraw = [" + likelihood[0]+" , " + likelihood[1]+"]");
				}
			}
			if (isToTreatGermanyNode) {
				// lines are written in percent. Convert to value within 0 and 1
				float prob = Float.parseFloat(germanyNodeLine[1])/100f; 
				if (prob == germanyNode.getMarginalAt(0)) {
					// do nothing
				} else {
					float[] likelihood = {.5f, .5f};
					if (prob > germanyNode.getMarginalAt(0)) {
						likelihood[0] = (float) (2.0/3.0);
						likelihood[1] = (float) (1.0/3.0);
					} else if (prob < germanyNode.getMarginalAt(0)){
						likelihood[0] = (float) (1.0/3.0);
						likelihood[1] = (float) (2.0/3.0);
					}
					germanyNode.addLikeliHood(likelihood);
					System.out.println("Read = ["+germanyNodeLine[1]+"]. Likelihood of germany = [" + likelihood[0]+" , " + likelihood[1]+"]");
				}
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
	public final void testWriteGapCSV21OscilatingLikelihood() {
		System.out.println("\n\n[Writing gapLikelihood21OscilatingLikelihood.csv]");
		
		// load the main bayes net
		ProbabilisticNetwork net = null;
		try {
			net = new XMLBIFIO().load(new File(this.getClass().getClassLoader().getResource("GreekExit.xml").toURI()));
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
			gapCSV = new PrintStream(new FileOutputStream(new File("gapLikelihood21OscilatingLikelihood.csv")));
			// 1st line has the name of the attributes
			gapCSV.println("date,gap");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// load the csv file which contains the probabilities of the hypothesis node from the market
		CSVReader marketReader = null;
		try {
			marketReader = new CSVReader(new FileReader(new File(this.getClass().getClassLoader().getResource("109-treated.csv").toURI())));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}	
		
		// load the hypothesis.csv file (the infered values)
		CSVReader inferredReader = null;
		try {
			inferredReader = new CSVReader(new FileReader(new File("hypothesisLikelihood21OscilatingLikelihood.csv")));
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
		
		// object to convert string to date
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		
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
					if (inferredDate.getDate() != marketDate.getDate()) {
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
	

	
}
