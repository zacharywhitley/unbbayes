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
		
		sim.setRscriptProgramName("C:\\Program Files\\R\\R-3.3.1\\bin\\Rscript");
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
	
	/**
	 * Test method for {@link utils.UserActivitySimulator#generateTransformedData()}.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public final void testExtended() throws IOException, InterruptedException {
		String correlationDataFileFolder = "CorrelationData";
		String rawData = "userActivity_extended.csv";
		String transformedData = "detectorsDays_extended.csv";
		String output = "testDistance_extended.out";
		
		UserActivitySimulator sim = new UserActivitySimulator();
		
		sim.setRawDataOutput(rawData);
		assertTrue(new File(rawData).exists());
		assertFalse(new File(rawData).isDirectory());
		long rawDataLength = new File(rawData).length();
		
		sim.setCorrelationDataFileFolder(correlationDataFileFolder);;
		assertTrue(new File(correlationDataFileFolder).exists());
		assertTrue(new File(correlationDataFileFolder).isDirectory());
		
		sim.setTransformedDataOutput(transformedData);;
		
		new File(output).delete();
//		assertFalse(new File(output).exists());
		sim.setDistanceMetricFileName(output);
		
		// call PCA to generate csv file with transformed (PCA) data 
		sim.generateTransformedData();
		assertTrue(new File(transformedData).exists());
		assertTrue(new File(transformedData).isFile());
		
		// compute the distance of transformed correlation (expected VS actual)
		sim.computeDistance();

		assertTrue(new File(rawData).exists());
		assertEquals(rawDataLength, new File(rawData).length());		
		
		assertTrue(new File(correlationDataFileFolder).exists());
		assertTrue(new File(transformedData).exists());
		assertTrue(new File(output).exists());
		
		assertTrue(new File(output).length() > 0);
		
//		assertTrue(new File(output).delete());
	}

}
