/**
 * 
 */
package utils;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Shou Matsumoto
 *
 */
public class TestUsrActivitySimulator extends TestCase {

	/**
	 * @param name
	 */
	public TestUsrActivitySimulator(String name) {
		super(name);
	}

	/**
	 * Test method for {@link utils.UserActivitySimulator#generateTransformedData()}.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public final void testGenerateTransformedData() throws IOException, InterruptedException {
		String input = "Testdata1.csv";
		String output = "testOut.csv";
		
		UserActivitySimulator sim = new UserActivitySimulator();
		
		sim.setRawDataOutput(input);
		assertTrue(new File(input).exists());
		
		new File(output).delete();
		assertFalse(new File(output).exists());
		sim.setTransformedDataOutput(output);
		
		assertTrue(new File(sim.getRScriptName()).exists());
		
		sim.generateTransformedData();
		
		assertTrue(new File(input).exists());
		assertTrue(new File(output).exists());
		
		assertTrue(new File(output).length() > 0);
		
		
		assertTrue(new File(output).delete());
	}

}
