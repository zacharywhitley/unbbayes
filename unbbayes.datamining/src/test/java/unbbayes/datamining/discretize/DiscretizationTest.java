package unbbayes.datamining.discretize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import unbbayes.controller.FileController;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;

/**
 * @author Shou Matsumoto
 *
 */
public class DiscretizationTest {

//	private IDiscretizationFactory discretizationFactory = new RangeDiscretizationWithZeroFactory();
	private IDiscretizationFactory discretizationFactory = new FrequencyDiscretizationWithZeroFactory();
	
	private File inputFile;
	{
		URL url = getClass().getResource("dataToDiscretize.txt");
		assertNotNull(url);
		try {
			inputFile = new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private File outputFile = new File("discretized.txt");

	private int maxBinThreshold = 50;

	public DiscretizationTest() {}

	/**
	 * Test of {@link FrequencyDiscretizationWithZero}
	 * @throws Exception 
	 */
	@Test
	public final void testFrequencyDiscretizationWithZero() throws Exception {
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 49;
		
		File file = getInputFile();
		assertTrue(file.exists());
		
		// read initial 4 rows in order to check for properties of the data
		TxtLoader loader = new TxtLoader(file, 4);	
		assertNotNull(loader);
		
		
		// load header only to check if number of headers matches with expected
		loader.buildHeader();
		assertEquals(expectedNumAttributes, loader.getNumAttributes());
		
		expectedNumAttributes = loader.getNumAttributes();
		
		// these are some properties we need to check
		boolean[] attributeIsString = new boolean[expectedNumAttributes];
		byte[] attributeType = new byte[expectedNumAttributes];
		String[] attributeNames = new String[expectedNumAttributes];
		
		Arrays.fill(attributeIsString, true);
		Arrays.fill(attributeType, Attribute.NOMINAL);
//		Arrays.fill(attributeNames, "");
		
		// backup properties of attributes
		InstanceSet dataSet = loader.getInstanceSet();
		for (int attributeIndex = 0; attributeIndex < expectedNumAttributes; attributeIndex++) {
			
			Attribute attribute = dataSet.getAttribute(attributeIndex);
			attributeNames[attributeIndex] = attribute.getAttributeName();
			
			// check if data of current attribute are numeric
			boolean isNumeric = true;
			for (String value : attribute.getDistinticNominalValues()) {
				try {
					Float.parseFloat(value);
				} catch (NumberFormatException e) {
					isNumeric = false;
					break;
				}
			}
			
			// mark this attribute as number or name
			attributeType[attributeIndex] = isNumeric?Attribute.NUMERIC:Attribute.NOMINAL;
			attributeIsString[attributeIndex] = false;	// numbers cannot be string
			
		}
		
		assertEquals("id", attributeNames[0]);
		
		// will reload everything
		loader = new TxtLoader(file, -1);
		assertNotNull(loader);
		
		// configure loading mode (e.g. number, string, etc)
		
		loader.setNumAttributes(expectedNumAttributes);
		loader.setCounterAttribute(-1);			// assume no counter in data
		loader.setAttributeName(attributeNames);
		loader.setAttributeIsString(attributeIsString);
		loader.setAttributeType(attributeType);
		
		
		// load all instances
		while (loader.getInstance());
		
		// re extract full data set
		dataSet = loader.getInstanceSet();
		assertNotNull(dataSet);
		
		// instantiate discretizer
		IDiscretization discretization = getDiscretizationFactory().buildInstance(dataSet);
		dataSet = discretization.getInstances();	// use the exact instance used by discretizer (because it may be a clone)
		assertNotNull(dataSet);
		
		assertEquals(expectedNumData, dataSet.instances.length);
		
		
		// check number of columns
		assertEquals(expectedNumAttributes, dataSet.getAttributes().length);
		// check that 1st column is id
		assertEquals("id", dataSet.getAttribute(0).getAttributeName());
//		assertEquals(Attribute.NOMINAL, dataSet.getAttribute(0).getAttributeType());
		
		
		// check that other columns are numeric
		for (int i = 1; i < dataSet.getAttributes().length; i++) {
			assertEquals(dataSet.getAttribute(i).getAttributeName(), Attribute.NUMERIC, dataSet.getAttribute(i).getAttributeType());
		}
		
		
		AttributeStats[] stats = dataSet.getAttributeStats();
		assertNotNull(stats);
		assertEquals(expectedNumAttributes, stats.length);
		
		// discretize numeric values by frequency
		int[] allAttributeIndexes = new int[expectedNumAttributes];	// keep track of attributes that were visited
		for (int attributeIndex = 0; attributeIndex < expectedNumAttributes; attributeIndex++) {
			
			// extract attribute and stats
			Attribute attribute = dataSet.getAttribute(attributeIndex);
			AttributeStats stat = stats[attributeIndex];
			
			allAttributeIndexes[attributeIndex] = attributeIndex;
			
			if (attribute.getAttributeType() != Attribute.NUMERIC) {
				continue;	// ignore non-numeric
			}
			
			// do not use frequency larger than distinct values
			int numThresholds = stat.getDistinctCount() - 1;
			if (numThresholds > getMaxBinThreshold()) {
				numThresholds = getMaxBinThreshold();
			}
			
			discretization.discretizeAttribute(attribute, numThresholds);
			attribute = dataSet.getAttribute(attributeIndex);
			
			assertEquals(attribute.getAttributeName(), Attribute.NOMINAL, attribute.getAttributeType());
		}
		
		// save discretized
		FileController.getInstance().saveInstanceSet(getOutputFile(), dataSet, allAttributeIndexes, false);
		
	}

	/**
	 * @return the discretizationFactory
	 */
	public IDiscretizationFactory getDiscretizationFactory() {
		return discretizationFactory;
	}

	/**
	 * @param discretizationFactory the discretizationFactory to set
	 */
	public void setDiscretizationFactory(IDiscretizationFactory discretizationFactory) {
		this.discretizationFactory = discretizationFactory;
	}

	/**
	 * @return the inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * @return the outputFile
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * @param outputFile the outputFile to set
	 */
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @return the maxBinThreshold
	 */
	public int getMaxBinThreshold() {
		return maxBinThreshold;
	}

	/**
	 * @param maxBinThreshold the maxBinThreshold to set
	 */
	public void setMaxBinThreshold(int maxBinThreshold) {
		this.maxBinThreshold = maxBinThreshold;
	}

}
