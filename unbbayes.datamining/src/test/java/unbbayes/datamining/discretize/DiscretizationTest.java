package unbbayes.datamining.discretize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import unbbayes.controller.FileController;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.discretize.sample.ISampler;
import unbbayes.datamining.discretize.sample.TriangularDistributionSampler;

/**
 * @author Shou Matsumoto
 *
 */
public class DiscretizationTest {

	private IDiscretizationFactory discretizationFactory = new RangeDiscretizationWithZeroFactory();
//	private IDiscretizationFactory discretizationFactory = new FrequencyDiscretizationWithZeroFactory();
	
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
			int numThresholds = stat.getDistinctCount();
			if (numThresholds > getMaxBinThreshold()) {
				numThresholds = getMaxBinThreshold();
			}
			
			discretization.discretizeAttribute(attribute, numThresholds);
			attribute = dataSet.getAttribute(attributeIndex);
			
			assertEquals(attribute.getAttributeName(), Attribute.NOMINAL, attribute.getAttributeType());
		}
		
		getOutputFile().delete();	// delete old file
		
		// save discretized
		FileController.getInstance().saveInstanceSet(getOutputFile(), dataSet, allAttributeIndexes, false);
		
	}
	
	
	/**
	 * Test of {@link TriangularDistributionSampler}
	 * @throws Exception 
	 */
	@Test
	public final void testTriangularDistributionSamplerFreqBatch() throws Exception {

		int expectedNumData = 3582;
		int expectedNumAttributes = 48;
		String regexAttributeToSave = "w2_.+";
		
		File folder = new File(getClass().getResource("samplesFreq/").toURI());
//		folder = new File(getClass().getResource("samplesRange/").toURI());
		
		assertTrue(folder.exists());
		assertTrue(folder.isDirectory());
		assertTrue(folder.list().length > 0);
		
		for (File file : folder.listFiles()) {
			// batch process files in folder
			assertTrue(file.exists());
			assertTrue(file.getName().endsWith(".txt"));
			
			// append suffix to output file name
			String outFileName = file.getName().replace(".txt", "_triangular.txt");
			File outputFile = new File(outFileName);
			outputFile.delete();
			
			// read initial 4 rows in order to check for properties of the data
			TxtLoader loader = new TxtLoader(file, 4);	
			assertNotNull(file.getName(), loader);
			
			
			// load header only to check if number of headers matches with expected
			loader.buildHeader();
			assertEquals(file.getName(), expectedNumAttributes, loader.getNumAttributes());
			
			expectedNumAttributes = loader.getNumAttributes();
			
			// these are some properties we need to check
			boolean[] attributeIsString = new boolean[expectedNumAttributes];
			byte[] attributeType = new byte[expectedNumAttributes];
			String[] attributeNames = new String[expectedNumAttributes];
			
			Arrays.fill(attributeIsString, true);
			Arrays.fill(attributeType, Attribute.NOMINAL);
			
			// backup properties of attributes
			InstanceSet dataSet = loader.getInstanceSet();
			for (int attributeIndex = 0; attributeIndex < expectedNumAttributes; attributeIndex++) {
				
				Attribute attribute = dataSet.getAttribute(attributeIndex);
				attributeNames[attributeIndex] = attribute.getAttributeName();
				
				// check if data of current attribute are numeric
				boolean isNumeric = false;
				for (String value : attribute.getDistinticNominalValues()) {
					try {
						Float.parseFloat(value);
						isNumeric = true;
					} catch (NumberFormatException e) {
						isNumeric = false;
						break;
					}
				}
				
				// mark this attribute as number or name
				attributeType[attributeIndex] = isNumeric?Attribute.NUMERIC:Attribute.NOMINAL;
				attributeIsString[attributeIndex] = !isNumeric;	// numbers cannot be string
				
			}
			
			// will reload everything
			loader = new TxtLoader(file, -1);
			assertNotNull(file.getName(), loader);
			
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
			ISampler sampler = new TriangularDistributionSampler(dataSet);
			dataSet = sampler.getInstances();	// use the exact instance used by discretizer (because it may be a clone)
			assertNotNull(file.getName(), dataSet);
			
			assertEquals(file.getName(), expectedNumData, dataSet.instances.length);
			
			
			// check number of columns
			assertEquals(file.getName(), expectedNumAttributes, dataSet.getAttributes().length);
			
			
			// check that columns are not nominal
			for (int i = 0; i < dataSet.getAttributes().length; i++) {
				assertEquals(file.getName() + ", " + dataSet.getAttribute(i).getAttributeName(), Attribute.NOMINAL, dataSet.getAttribute(i).getAttributeType());
			}
			
			
			AttributeStats[] stats = dataSet.getAttributeStats();
			assertNotNull(file.getName(), stats);
			assertEquals(file.getName(), expectedNumAttributes, stats.length);
			
			// sample numeric values 
			List<Integer> attributeIndexesToSave = new ArrayList<Integer>(); // keep track of attributes that were visited
			for (int attributeIndex = 0; attributeIndex < expectedNumAttributes; attributeIndex++) {
				
				// extract attribute and stats
				Attribute attribute = dataSet.getAttribute(attributeIndex);
				AttributeStats stat = stats[attributeIndex];
				
				if (attribute.getAttributeName().matches(regexAttributeToSave)) {
					attributeIndexesToSave.add(attributeIndex);
				}
				
				if (attribute.getAttributeType() != Attribute.NOMINAL) {
					continue;	// ignore numeric
				}
				
				// do not use frequency larger than distinct values
				int numThresholds = stat.getDistinctCount();
				if (numThresholds > getMaxBinThreshold()) {
					numThresholds = getMaxBinThreshold();
				}
				
				sampler.discretizeAttribute(attribute, numThresholds);
				attribute = dataSet.getAttribute(attributeIndex);
				
				assertEquals(file.getName() + ", " + attribute.getAttributeName(), Attribute.NUMERIC, attribute.getAttributeType());
			}
			
			// save method requires indexes to be array of int instead of list
			int[] indexesToSave = new int[attributeIndexesToSave.size()];
			for (int i = 0; i < attributeIndexesToSave.size(); i++) {
				indexesToSave[i] = attributeIndexesToSave.get(i);
			}
			// save output
			FileController.getInstance().saveInstanceSet(outputFile, dataSet, indexesToSave , false);
			assertTrue(outputFile.getName(), outputFile.exists());
			
		}
		
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
