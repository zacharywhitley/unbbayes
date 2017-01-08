/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import unbbayes.util.Debug;
import utils.SimulatedUserStatisticsCalculator.SubSamplingMode;
import junit.framework.TestCase;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class SimulatedUserStatisticsCalculatorTest extends TestCase {

	private SimulatedUserStatisticsCalculator calculator = null;
	
	/**
	 * @param name
	 */
	public SimulatedUserStatisticsCalculatorTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.calculator = SimulatedUserStatisticsCalculator.getInstance();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public File createRandomJointProbabilityFile(Random random, int jointStateSize) throws IOException {
		float sum = 0;
		
		float[] jointProb = new float[jointStateSize];
		
		// create random numeric vector
		for (int i = 0; i < jointProb.length; i++) {
			float prob = random.nextFloat();
			sum += prob;
			jointProb[i]= prob;
		}
		
		// normalize
		for (int i = 0; i < jointProb.length; i++) {
			jointProb[i] /= sum;
		}
		
		File output = Files.createTempFile("prob_", ".csv").toFile();
		
		PrintStream printer = new PrintStream(new FileOutputStream(output, false));
		
		for (float prob : jointProb) {
			printer.println(prob);
		}
		
		printer.close();
		
		return output;
	}

	/**
	 * Test method for {@link utils.SimulatedUserStatisticsCalculator#main(java.lang.String[])}.
	 * @throws Exception 
	 */
	public final void testMain() throws Exception {
		DirichletUserSimulator dirichlet = DirichletUserSimulator.getInstance(4212, 500);
		
		String[] args = new String[15];
		
		
		File probFile = new File(getClass().getResource("../testProb.csv").toURI());
		File tempDirichletOutput = Files.createTempDirectory("Users_").toFile();
		tempDirichletOutput.deleteOnExit();
		
		assertTrue(probFile.exists());
		
		args[0] = "-i";
		args[1] = "\"" + probFile.getAbsolutePath() +"\"";
		args[2] = "-o";
		args[3] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";
		args[4] = "-u";
		args[5] = "3000";
		args[6] = "-n";
		args[7] = "100";
		args[8] = "-numI";	
		args[9] = "2"	;
		args[10] = "-numD";	
		args[11] = "2";	
		args[12] = "-a";	
		args[13] = "2";
		args[14] = "-d";
		
		// run dirichlet multinomial sampler
		dirichlet.main(args);
		
		assertTrue(tempDirichletOutput.exists());
		assertEquals(100, tempDirichletOutput.listFiles().length);
		assertTrue(tempDirichletOutput.getTotalSpace() > 0);
		
		File tempQuestionOutput = File.createTempFile("Probabilities_Questions_", ".csv");
		tempQuestionOutput.deleteOnExit();
		
		// set up arguments to calculate probabilities of questions
		args[0] = "-i";
		args[1] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";;
		args[2] = "-o";
		args[3] = "\"" + tempQuestionOutput.getAbsolutePath() +"\"";;
		args[4] = "-numI";	
		args[5] = "2";	
		args[6] = "-d";	
		args[7] = "-s";
		args[8] = "-alertSample";
		args[9] = "-1";
		args[10] = "-c";
		args[11] = "0.6";
		args[12] = "-p";
		args[13] = "";
		args[14] = "";
		calculator.main(args);
		
		/* Format of result:
		 * Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		 * Q01
		 * Q02
		 * Q03
		 * Q04
		 * Q05
		 * Q06
		 * Q07
		 */
		// read the output file and check sanity
		CSVReader reader = new CSVReader(new FileReader(tempQuestionOutput));
		
		String[] csvRow = reader.readNext();
		assertTrue(csvRow.length >= 6);
		// Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		assertEquals("Query", csvRow[0]);
		assertEquals("Average", csvRow[1]);
		assertEquals("Std.Dev.", csvRow[2]);
		assertEquals("Median", csvRow[3]);
		assertEquals("0.6 lower", csvRow[4]);
		assertEquals("0.6 upper", csvRow[5]);
		
		// store results
		Map<String, Map<String, Float>> results = new HashMap<String, Map<String,Float>>();
		for (csvRow = reader.readNext(); csvRow != null; csvRow = reader.readNext()) {
			assertTrue(csvRow[0], csvRow.length >= 6);
			Map<String,Float> value = new HashMap<String, Float>();
			float average = Float.parseFloat(csvRow[1]);
			float lower = Float.parseFloat(csvRow[4]);
			float upper = Float.parseFloat(csvRow[5]);
			assertTrue(csvRow[0], average >= 0 && average <= 1);
			assertTrue(csvRow[0], lower >= 0 && lower <= 1);
			assertTrue(csvRow[0], upper >= 0 && upper <= 1);
			assertTrue(csvRow[0], average >= lower);
			assertTrue(csvRow[0], average <= upper);
			value.put("Average", average);
			value.put("0.6 lower", lower);
			value.put("0.6 upper", upper);
			results.put(csvRow[0], value);
		}
		assertEquals(7, results.size());
		reader.close();
		
		assertTrue(tempQuestionOutput.delete());
		assertTrue(!tempQuestionOutput.exists());
		
		// run again with 30 alert samples
		args[8] = "-alertSample";
		args[9] = "30";
		calculator.setSubSamplingMode(SubSamplingMode.WEIGHTED);
		calculator.main(args);
		assertTrue(tempQuestionOutput.exists());
		
		// read the output file and check sanity
		reader = new CSVReader(new FileReader(tempQuestionOutput));
		
		csvRow = reader.readNext();
		assertTrue(csvRow.length >= 6);
		// Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		assertEquals("Query", csvRow[0]);
		assertEquals("Average", csvRow[1]);
		assertEquals("Std.Dev.", csvRow[2]);
		assertEquals("Median", csvRow[3]);
		assertEquals("0.6 lower", csvRow[4]);
		assertEquals("0.6 upper", csvRow[5]);
		
		// check results
		for (csvRow = reader.readNext(); csvRow != null; csvRow = reader.readNext()) {
			assertTrue(csvRow[0], csvRow.length >= 6);
			Map<String,Float> expected = results.get(csvRow[0]);
			assertNotNull(csvRow[0], expected);
			
			assertEquals(csvRow[0] + ";" + expected, expected.get("Average"), Float.parseFloat(csvRow[1]), 0.05f);
			assertTrue(csvRow[0] + ";" + expected, expected.get("0.6 lower") >= Float.parseFloat(csvRow[4]));
			assertTrue(csvRow[0] + ";" + expected, expected.get("0.6 lower") <= Float.parseFloat(csvRow[5]));
		}
		reader.close();
		
		
		// make sure temporary files are deleted
		for (File innerFile : tempDirichletOutput.listFiles()) {
			assertTrue(innerFile.getAbsolutePath(), innerFile.delete());
		}
		assertTrue(tempDirichletOutput.getAbsolutePath(), tempDirichletOutput.delete());
		assertTrue(tempQuestionOutput.getAbsolutePath(), tempQuestionOutput.delete());
	}
	
	
	/**
	 * Test method for {@link utils.SimulatedUserStatisticsCalculator#main(java.lang.String[])}.
	 * @throws Exception 
	 */
	public final void testMainRandom() throws Exception {
		DirichletUserSimulator dirichlet = DirichletUserSimulator.getInstance(4212, 500);
		
		String[] args = new String[15];
		
		long seed = System.currentTimeMillis();
		Debug.println("Seed = " + seed);
		File probFile = this.createRandomJointProbabilityFile(new Random(seed), 2*2*2*2*2);
		probFile.deleteOnExit();
		assertTrue("Seed=" + seed, probFile.exists());
		assertTrue("Seed=" + seed,probFile.getTotalSpace() > 0);
		
		File tempDirichletOutput = Files.createTempDirectory("Users_").toFile();
		tempDirichletOutput.deleteOnExit();
		
		
		args[0] = "-i";
		args[1] = "\"" + probFile.getAbsolutePath() +"\"";
		args[2] = "-o";
		args[3] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";
		args[4] = "-u";
		args[5] = "3000";
		args[6] = "-n";
		args[7] = "100";
		args[8] = "-numI";	
		args[9] = "2"	;
		args[10] = "-numD";	
		args[11] = "2";	
		args[12] = "-a";	
		args[13] = "2";
		args[14] = "-d";
		
		// run dirichlet multinomial sampler
		dirichlet.main(args);
		
		assertTrue("Seed=" + seed,tempDirichletOutput.exists());
		assertEquals("Seed=" + seed,100, tempDirichletOutput.listFiles().length);
		assertTrue("Seed=" + seed,tempDirichletOutput.getTotalSpace() > 0);
		
		File tempQuestionOutput = File.createTempFile("Probabilities_Questions_", ".csv");
		tempQuestionOutput.deleteOnExit();
		
		// set up arguments to calculate probabilities of questions
		args[0] = "-i";
		args[1] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";;
		args[2] = "-o";
		args[3] = "\"" + tempQuestionOutput.getAbsolutePath() +"\"";;
		args[4] = "-numI";	
		args[5] = "2";	
		args[6] = "-d";	
		args[7] = "-s";
		args[8] = "-alertSample";
		args[9] = "-1";
		args[10] = "-c";
		args[11] = "0.6";
		args[12] = "-p";
		args[13] = "";
		args[14] = "";
		
		calculator.main(args);
		
		/* Format of result:
		 * Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		 * Q01
		 * Q02
		 * Q03
		 * Q04
		 * Q05
		 * Q06
		 * Q07
		 */
		// read the output file and check sanity
		CSVReader reader = new CSVReader(new FileReader(tempQuestionOutput));
		
		String[] csvRow = reader.readNext();
		assertTrue("Seed=" + seed,csvRow.length >= 6);
		// Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		assertEquals("Seed=" + seed,"Query", csvRow[0]);
		assertEquals("Seed=" + seed,"Average", csvRow[1]);
		assertEquals("Seed=" + seed,"Std.Dev.", csvRow[2]);
		assertEquals("Seed=" + seed,"Median", csvRow[3]);
		assertEquals("Seed=" + seed,"0.6 lower", csvRow[4]);
		assertEquals("Seed=" + seed,"0.6 upper", csvRow[5]);
		
		// store results
		Map<String, Map<String, Float>> results = new HashMap<String, Map<String,Float>>();
		for (csvRow = reader.readNext(); csvRow != null; csvRow = reader.readNext()) {
			assertTrue("Seed=" + seed + ";" + csvRow[0], csvRow.length >= 6);
			Map<String,Float> value = new HashMap<String, Float>();
			float average = Float.parseFloat(csvRow[1]);
			float lower = Float.parseFloat(csvRow[4]);
			float upper = Float.parseFloat(csvRow[5]);
			assertTrue(csvRow[0], average >= 0 && average <= 1);
			assertTrue(csvRow[0], lower >= 0 && lower <= 1);
			assertTrue(csvRow[0], upper >= 0 && upper <= 1);
			assertTrue(csvRow[0], average >= lower);
			assertTrue(csvRow[0], average <= upper);
			value.put("Average", average);
			value.put("0.6 lower", lower);
			value.put("0.6 upper", upper);
			results.put(csvRow[0], value);
		}
		assertEquals("Seed=" + seed, 7, results.size());
		reader.close();
		
		assertTrue("Seed=" + seed, tempQuestionOutput.delete());
		assertTrue("Seed=" + seed, !tempQuestionOutput.exists());
		
		// run again with 30 alert samples
		args[8] = "-alertSample";
		args[9] = "30";
		calculator.setSubSamplingMode(SubSamplingMode.WEIGHTED);
		calculator.main(args);
		assertTrue("Seed=" + seed, tempQuestionOutput.exists());
		
		// read the output file and check sanity
		reader = new CSVReader(new FileReader(tempQuestionOutput));
		
		csvRow = reader.readNext();
		assertTrue("Seed=" + seed, csvRow.length >= 6);
		// Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		assertEquals("Seed=" + seed, "Query", csvRow[0]);
		assertEquals("Seed=" + seed, "Average", csvRow[1]);
		assertEquals("Seed=" + seed, "Std.Dev.", csvRow[2]);
		assertEquals("Seed=" + seed, "Median", csvRow[3]);
		assertEquals("Seed=" + seed, "0.6 lower", csvRow[4]);
		assertEquals("Seed=" + seed, "0.6 upper", csvRow[5]);
		
		// check results
		for (csvRow = reader.readNext(); csvRow != null; csvRow = reader.readNext()) {
			assertTrue("Seed=" + seed + ";" + csvRow[0], csvRow.length >= 6);
			Map<String,Float> expected = results.get(csvRow[0]);
			assertNotNull("Seed=" + seed + ";" + csvRow[0], expected);
			
			assertEquals("Seed=" + seed + ";" + csvRow[0] + ";" + expected, expected.get("Average"), Float.parseFloat(csvRow[1]), 0.05f);
			assertTrue("Seed=" + seed + ";" + csvRow[0] + ";" + expected, expected.get("0.6 lower") >= Float.parseFloat(csvRow[4]));
			assertTrue("Seed=" + seed + ";" + csvRow[0] + ";" + expected, expected.get("0.6 lower") <= Float.parseFloat(csvRow[5]));
		}
		reader.close();
		
		
		// make sure temporary files are deleted
		for (File innerFile : tempDirichletOutput.listFiles()) {
			assertTrue("Seed=" + seed + ";" + innerFile.getAbsolutePath(), innerFile.delete());
		}
		assertTrue("Seed=" + seed + ";" + tempDirichletOutput.getAbsolutePath(), tempDirichletOutput.delete());
		assertTrue("Seed=" + seed + ";" + tempQuestionOutput.getAbsolutePath(), tempQuestionOutput.delete());
	}
	
	/**
	 * Test method for {@link utils.SimulatedUserStatisticsCalculator#main(java.lang.String[])}.
	 * @throws Exception 
	 */
	public final void testBetaBinomialRandom() throws Exception {
		DirichletUserSimulator dirichlet = DirichletUserSimulator.getInstance(4212, 500);
		
		String[] args = new String[15];
		
		long seed = System.currentTimeMillis();
		Debug.println("Seed = " + seed);
		File probFile = this.createRandomJointProbabilityFile(new Random(seed), 2*2*2*2*2);
		probFile.deleteOnExit();
		assertTrue("Seed=" + seed, probFile.exists());
		assertTrue("Seed=" + seed,probFile.getTotalSpace() > 0);
		
		File tempDirichletOutput = Files.createTempDirectory("Users_").toFile();
		tempDirichletOutput.deleteOnExit();
		
		
		args[0] = "-i";
		args[1] = "\"" + probFile.getAbsolutePath() +"\"";
		args[2] = "-o";
		args[3] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";
		args[4] = "-u";
		args[5] = "3000";
		args[6] = "-n";
		args[7] = "100";
		args[8] = "-numI";	
		args[9] = "2"	;
		args[10] = "-numD";	
		args[11] = "2";	
		args[12] = "-a";	
		args[13] = "2";
		args[14] = "-d";
		
		// run dirichlet multinomial sampler
		dirichlet.main(args);
		
		assertTrue("Seed=" + seed,tempDirichletOutput.exists());
		assertEquals("Seed=" + seed,100, tempDirichletOutput.listFiles().length);
		assertTrue("Seed=" + seed,tempDirichletOutput.getTotalSpace() > 0);
		
		File tempQuestionOutput = File.createTempFile("Probabilities_Questions_", ".csv");
		tempQuestionOutput.deleteOnExit();
		
		// set up arguments to calculate probabilities of questions
		args[0] = "-i";
		args[1] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";;
		args[2] = "-o";
		args[3] = "\"" + tempQuestionOutput.getAbsolutePath() +"\"";;
		args[4] = "-numI";	
		args[5] = "2";	
		args[6] = "-d";	
		args[7] = "-s";
		args[8] = "-alertSample";
		args[9] = "30";
		args[10] = "-c";
		args[11] = "0.6";
		args[12] = "-p";
		args[13] = "";
		args[14] = "";
		calculator.setSubSamplingMode(SubSamplingMode.BETA_BINOMIAL);
		calculator.main(args);
		
		/* Format of result:
		 * Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		 * Q01
		 * Q02
		 * Q03
		 * Q04
		 * Q05
		 * Q06
		 * Q07
		 */
		// read the output file and check sanity
		CSVReader reader = new CSVReader(new FileReader(tempQuestionOutput));
		
		String[] csvRow = reader.readNext();
		assertTrue("Seed=" + seed,csvRow.length >= 6);
		// Query,	Average,	Std.Dev.,	Median,	0.6 lower,	0.6 upper
		assertEquals("Seed=" + seed,"Query", csvRow[0]);
		assertEquals("Seed=" + seed,"Average", csvRow[1]);
		assertEquals("Seed=" + seed,"Std.Dev.", csvRow[2]);
		assertEquals("Seed=" + seed,"Median", csvRow[3]);
		assertEquals("Seed=" + seed,"0.6 lower", csvRow[4]);
		assertEquals("Seed=" + seed,"0.6 upper", csvRow[5]);
		
		// store results
		Map<String, Map<String, Float>> results = new HashMap<String, Map<String,Float>>();
		for (csvRow = reader.readNext(); csvRow != null; csvRow = reader.readNext()) {
			assertTrue("Seed=" + seed + ";" + csvRow[0], csvRow.length >= 6);
			float average = Float.parseFloat(csvRow[1]);
			float lower = Float.parseFloat(csvRow[4]);
			float upper = Float.parseFloat(csvRow[5]);
			assertTrue(csvRow[0], average >= 0 && average <= 1);
			assertTrue(csvRow[0], lower >= 0 && lower <= 1);
			assertTrue(csvRow[0], upper >= 0 && upper <= 1);
			assertTrue(csvRow[0], average >= lower);
			assertTrue(csvRow[0], average <= upper);
			Map<String,Float> value = new HashMap<String, Float>();
			value.put("Average", average);
			value.put("0.6 lower", lower);
			value.put("0.6 upper", upper);
			results.put(csvRow[0], value);
		}
		assertEquals("Seed=" + seed, 7, results.size());
		reader.close();
		
		assertTrue("Seed=" + seed, tempQuestionOutput.delete());
		assertTrue("Seed=" + seed, !tempQuestionOutput.exists());
		
		// make sure temporary files are deleted
		for (File innerFile : tempDirichletOutput.listFiles()) {
			assertTrue("Seed=" + seed + ";" + innerFile.getAbsolutePath(), innerFile.delete());
		}
		assertTrue("Seed=" + seed + ";" + tempDirichletOutput.getAbsolutePath(), tempDirichletOutput.delete());
	}
	
	/**
	 * Test method for {@link utils.SimulatedUserStatisticsCalculator#main(java.lang.String[])}
	 * which ignores some columns in large csv file of users.
	 * @throws Exception 
	 */
	@SuppressWarnings("static-access")
	public final void testMainIgnoreOption() throws Exception {
		
		String input = getClass().getResource("../Dirichlet_Large").getPath();
		
		
		File tempQuestionOutput = File.createTempFile("Probabilities_Questions_", ".csv");
		tempQuestionOutput.deleteOnExit();
		
		// set up arguments to calculate probabilities of questions
		String[] args = new String[9];
		args[0] = "-i";
		args[1] = "\"" + input +"\"";;
		args[2] = "-o";
		args[3] = "\"" + tempQuestionOutput.getAbsolutePath() +"\"";;
		args[4] = "-numI";	
		args[5] = "4";	
		args[6] = "-ignore";	
		args[7] = "D.*";	
		args[8] = "-d";	
		
		calculator.main(args);
		
		/* Format of result:
		 * "Q01","Q02","Q03","Q04","Q05","Q06","Q07","Q08","Q09","Q10","Q11",
		 * 1.0,0.24390244,0.024564182,1.0,1.0,1.0,1.0,0.24390244,0.30081302,0.13821137,0.13821137,
		 * 1.0,0.24390244,0.024564182,1.0,1.0,1.0,1.0,0.24390244,0.30081302,0.13821137,0.13821137,
		 */
		// read the output file and check sanity
		CSVReader reader = new CSVReader(new FileReader(tempQuestionOutput));
		
		List<String[]> rows = reader.readAll();
		assertEquals(1 + new File(input).listFiles().length, rows.size());	// The number of rows to expect is header + number of input files
		
		// check header:  "Q01","Q02","Q03","Q04","Q05","Q06","Q07","Q08","Q09","Q10","Q11",
		assertTrue(rows.get(0).length >= 11);
		for (int i = 0; i < 11; i++) {
			assertEquals("i="+i, "Q" + String.format("%1$02d", i+1), rows.get(0)[i]);
		}
		
		// check that all data is [0,1] or -1
		for (int i = 1; i < rows.size(); i++) {
			String[] row = rows.get(i);
			assertTrue(row.length >= 11);
			for (int column = 0; column < 11; column++) {
				assertNotNull("column=" + column, row[column]);
				assertFalse("column=" + column, row[column].trim().isEmpty());
				Float value = Float.parseFloat(row[column].trim());
				assertFalse("column=" + column,value.isNaN());
				assertFalse("column=" + column,value.isInfinite());
				if (value < 0) {
					assertEquals("column=" + column, -1f, value, 0.00005);
				} else {
					assertTrue("column=" + column, value >= 0f);
					assertTrue("column=" + column, value <= 1f);
				}
			}
		}
		
		reader.close();
		
		// make sure temporary files are deleted
		assertTrue(tempQuestionOutput.getAbsolutePath(), tempQuestionOutput.delete());
		assertTrue(tempQuestionOutput.getAbsolutePath(),!tempQuestionOutput.exists());
		
	}




}
