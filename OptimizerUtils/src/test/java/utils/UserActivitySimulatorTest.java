/**
 * 
 */
package utils;

import io.IModelCenterWrapperIO;
import io.ModelCenterWrapperIO;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Shou Matsumoto
 *
 */
public class UserActivitySimulatorTest extends TestCase {

	/**
	 * @param name
	 */
	public UserActivitySimulatorTest(String name) {
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
	

	/**
	 * Test method for {@link utils.UserActivitySimulator#generateTransformedData()}.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public final void testComputeDistance() throws IOException {
		String correlationDataFileFolder = "CorrelationData";
		String transformedData = "detectorsDays.csv";
		String output = "testDistance.out";
		
		UserActivitySimulator sim = new UserActivitySimulator();
		
		sim.setCorrelationDataFileFolder(correlationDataFileFolder);;
		assertTrue(new File(correlationDataFileFolder).exists());
		assertTrue(new File(correlationDataFileFolder).isDirectory());
		
		sim.setTransformedDataOutput(transformedData);;
		assertTrue(new File(transformedData).exists());
		assertTrue(new File(transformedData).isFile());
		
		new File(output).delete();
		assertFalse(new File(output).exists());
		sim.setDistanceMetricFileName(output);
		
		sim.computeDistance();
		
		assertTrue(new File(correlationDataFileFolder).exists());
		assertTrue(new File(transformedData).exists());
		assertTrue(new File(output).exists());
		
		assertTrue(new File(output).length() > 0);
		
		assertTrue(new File(output).delete());
	}

}
