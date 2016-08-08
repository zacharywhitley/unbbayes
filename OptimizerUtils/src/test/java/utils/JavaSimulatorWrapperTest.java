/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import junit.framework.TestCase;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class JavaSimulatorWrapperTest extends TestCase {

	private JavaSimulatorWrapper wrapper = null;
	
	/** ["-i", "Some input file" , "-o" , "Some output file" , "-d"] */
	private String[] args = null;
	
	/**
	 * @param name
	 */
	public JavaSimulatorWrapperTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(true);
		wrapper = JavaSimulatorWrapper.getInstance();
		args = new String[5];
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		wrapper = null;
		args = null;
	}
	
	protected void checkFileSanity(File file) throws IOException {

		// set up file tokenizer
		Reader reader = new BufferedReader(new FileReader(file));
		StreamTokenizer st = new StreamTokenizer(reader);
		st.wordChars('A', 'z');
		st.wordChars('0', '9');
		st.whitespaceChars(' ',' ');
		st.whitespaceChars('\t','\t');
		st.eolIsSignificant(true);	// declaration must be in same line
		st.commentChar('#');

		// read file until we find first non-commented word
		while ((st.nextToken() != st.TT_EOF) && st.ttype != st.TT_WORD);
		assertTrue(st.ttype != st.TT_EOF);
		
		// check that 1st line (which are not commented out) is "Probability Matrix"
		assertEquals("Probability", st.sval);
		assertTrue(st.nextToken() == st.TT_WORD);
		assertEquals("Matrix", st.sval);
		assertTrue(st.nextToken() == st.TT_EOL);
		
		// search next non-commented word
		while ((st.nextToken() != st.TT_EOF) && st.ttype != st.TT_WORD);
		assertTrue(st.ttype != st.TT_EOF);
		st.pushBack();
		
		// check that next line is the list of questions
		for (int questionNumber = 1; st.nextToken() != st.TT_EOL; questionNumber++) {
			assertTrue(st.sval, st.sval.startsWith("Q"));
			assertEquals(st.sval.split("Q")[1], questionNumber, Integer.parseInt(st.sval.split("Q")[1]));
		}
		
		// search at least one non-commented number
		while ((st.nextToken() != st.TT_EOF) && st.ttype != st.TT_NUMBER);
		assertTrue(st.ttype != st.TT_EOF);
		st.pushBack();
		
		// check that lines are a comma-separated list of probabilities
		while ((st.nextToken() != st.TT_EOF) && st.ttype != st.TT_NUMBER) {
			do {
				// number should be probability between 0 and 1, or an invalid entry (-1)
				assertTrue(""+st.nval, (st.nval >= 0 && st.nval <= 1) || (st.nval <= -1) );
				st.nextToken();
				if (st.ttype != st.TT_EOL && st.ttype != st.TT_EOF) {
					assertEquals(',', (char)st.ttype);
				}
			} while (st.nextToken() != st.TT_EOL);
		}
		
		reader.close();
	}
	
	/**
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with no argument.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMain() throws InterruptedException, IOException {
		// delete output file in advance
		String outputFileName = "./JavaSimulatorWrapper.out";
		if (new File(outputFileName).exists()) {
			assertTrue(new File(outputFileName).delete());
		}
		Thread.sleep(500);
		assertFalse(new File(outputFileName).exists());
		
		wrapper.main(null);
		
		assertTrue(new File(outputFileName).exists());
		assertTrue(new File(outputFileName).isFile());
		assertTrue(new File(outputFileName).length() > 0);
		
		// read the output file and check sanity
		this.checkFileSanity(new File(outputFileName));
		
	}
	

	/**
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with argument pointing to an input file with 
	 * {@value JavaSimulatorWrapper#HAS_ALERT_IN_PROB_PROPERTY_NAME} greater than zero,
	 * and to some temporary output file.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMainHasAlert() throws InterruptedException, IOException {
		
		File tempFile = File.createTempFile(getClass().getName() + "_", ".out");
		tempFile.deleteOnExit();
		
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../JavaSimulatorWrapper_hasAlert.in").getPath() + "\"";
		args[2] = "-o";
		args[3] = tempFile.getPath();
		args[4] = "-d";
		wrapper.main(args);
		
		
		assertTrue(tempFile.exists());
		assertTrue(tempFile.isFile());
		assertTrue(tempFile.length() > 0);
		
		// read the output file and check sanity
		this.checkFileSanity(tempFile);
		
		assertTrue(tempFile.delete());
	
	}
	
	/**
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with argument pointing to a simplified input file,
	 * and to some temporary output file.
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMainRedacted() throws IOException {
		
		File tempFile = File.createTempFile(getClass().getName() + "_", ".out");
		tempFile.deleteOnExit();
		
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../JavaSimulatorWrapper_redacted.in").getPath() + "\"";
		args[2] = "-o";
		args[3] = tempFile.getPath();
		args[4] = "-d";
		wrapper.main(args);
		
		
		assertTrue(tempFile.exists());
		assertTrue(tempFile.isFile());
		assertTrue(tempFile.length() > 0);
		
		// read the output file and check sanity
		this.checkFileSanity(tempFile);
		
		assertTrue(tempFile.delete());
	}
	
	/**
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with argument pointing to a input file with invalid probabilities.
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMainInvalidProb() throws IOException {
		
		File tempFile = File.createTempFile(getClass().getName() + "_", ".out");
		tempFile.deleteOnExit();
		
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../JavaSimulatorWrapper_invalidProb.in").getPath() + "\"";
		args[2] = "-o";
		args[3] = tempFile.getPath();
		args[4] = "-d";
		try {
			wrapper.main(args);
			fail("Should throw exception when probabilities are wrong");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("No positive probability found."));
		}
		
		tempFile.delete();
	}
	/**
	 * 
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with argument pointing to a input file with invalid number of users.
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMainInvalidUsers() throws IOException {
		
		File tempFile = File.createTempFile(getClass().getName() + "_", ".out");
		tempFile.deleteOnExit();
		
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../JavaSimulatorWrapper_invalidUsers.in").getPath() + "\"";
		args[2] = "-o";
		args[3] = tempFile.getPath();
		args[4] = "-d";
		try {
			wrapper.main(args);
			fail("Should throw exception when probabilities are wrong");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().toUpperCase().contains(wrapper.getNumberOfUsersPropertyName().toUpperCase()));
		}
		
		tempFile.delete();
	}
	
	/**
	 * 
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with argument pointing to a input file with invalid number of users.
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMainInvalidRuns() throws IOException {
		
		File tempFile = File.createTempFile(getClass().getName() + "_", ".out");
		tempFile.deleteOnExit();
		
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../JavaSimulatorWrapper_invalidRuns.in").getPath() + "\"";
		args[2] = "-o";
		args[3] = tempFile.getPath();
		args[4] = "-d";
		try {
			wrapper.main(args);
			fail("Should throw exception when probabilities are wrong");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().toUpperCase().contains(wrapper.getNumberOfRunsPropertyName().toUpperCase()));
		}
		
		tempFile.delete();
	}

}
