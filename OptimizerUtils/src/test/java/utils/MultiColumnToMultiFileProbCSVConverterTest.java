/**
 * 
 */
package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import junit.framework.TestCase;

/**
 * @author Shou Matsumoto
 *
 */
public class MultiColumnToMultiFileProbCSVConverterTest extends TestCase {

	private MultiColumnToMultiFileProbCSVConverter converter = null;
	
	/**
	 * @param name
	 */
	public MultiColumnToMultiFileProbCSVConverterTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		converter = MultiColumnToMultiFileProbCSVConverter.getInstance();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Smoke test of {@link JavaSimulatorWrapper#main(String[])}, with no argument.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public void testMain() throws InterruptedException, IOException {
		
		File temp = Files.createTempDirectory(getClass().getName() + "_").toFile();
		temp.deleteOnExit();
		
		String args[] = new String[5];
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../MC_Sample_Probs_numsim_20000.csv").getPath() + "\"";
		args[2] = "-o";
		args[3] = temp.getPath();
		args[4] = "-d";
		converter.main(args);
		
		
		assertTrue(temp.exists());
		assertTrue(temp.isDirectory());
		assertEquals(100, temp.listFiles().length);
		for (File file : temp.listFiles()) {
			assertTrue(file.getName(),file.getName().startsWith(converter.getProblemID()));
			assertTrue(file.getName(),file.length() > 0);
			assertTrue(file.delete());
		}
		
		assertTrue(temp.delete());
	}

}
