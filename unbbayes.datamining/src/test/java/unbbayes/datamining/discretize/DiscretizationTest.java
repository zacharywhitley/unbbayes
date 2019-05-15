package unbbayes.datamining.discretize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import unbbayes.controller.FileController;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.discretize.sample.BinBoundSampler;
import unbbayes.datamining.discretize.sample.ISampler;
import unbbayes.datamining.discretize.sample.TriangularDistributionSampler;
import unbbayes.io.CSVMultiEvidenceIO;

/**
 * @author Shou Matsumoto
 *
 */
public class DiscretizationTest {

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
		
		// use range discretization
//		IDiscretizationFactory discretizationFactory = new RangeDiscretizationWithZeroFactory();
		// use frequency discretization
		IDiscretizationFactory discretizationFactory = new FrequencyDiscretizationWithZeroFactory();
		
		// file to output
		File outputFile = new File("discretizedRange_" + System.currentTimeMillis() + ".txt");
		outputFile.delete();	// delete old file
		
		// input file
		URL url = getClass().getResource("dataToDiscretize.txt");
		assertNotNull(url);
		File file = new File(url.toURI());
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
		IDiscretization discretization = discretizationFactory.buildInstance(dataSet);
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
			if (numThresholds > maxBinThreshold) {
				numThresholds = maxBinThreshold;
			}
			
			discretization.transformAttribute(attribute, numThresholds);
			attribute = dataSet.getAttribute(attributeIndex);
			
			assertEquals(attribute.getAttributeName(), Attribute.NOMINAL, attribute.getAttributeType());
		}
		
		
		// save discretized
		FileController.getInstance().saveInstanceSet(outputFile, dataSet, allAttributeIndexes, false);
		
	}
	
	
	/**
	 * Test of {@link FrequencyDiscretizationWithZero}
	 * @throws Exception 
	 */
	@Test
	public final void testFrequencyDiscretizationWithZero2() throws Exception {
		
		// nominal attributes are: Division	Age	Gender checklink	privacysetting	checkhttps	clickwocheck	phishbefore	phishinlast	loseinfo	dlmalware
		Set<String> nominalAttributes = new HashSet<String>();
		nominalAttributes.add("Division");
		nominalAttributes.add("Age");
		nominalAttributes.add("Gender");
		nominalAttributes.add("checklink");
		nominalAttributes.add("privacysetting");
		nominalAttributes.add("checkhttps");
		nominalAttributes.add("clickwocheck");
		nominalAttributes.add("phishbefore");
		nominalAttributes.add("phishinlast");
		nominalAttributes.add("loseinfo");
		nominalAttributes.add("dlmalware");
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 55;
		
		// use range discretization
//		IDiscretizationFactory discretizationFactory = new RangeDiscretizationWithZeroFactory();
		// use frequency discretization
		IDiscretizationFactory discretizationFactory = new FrequencyDiscretizationWithZeroFactory();
		
		// file to output
		File outputFile = new File("discretizedRange2_" + System.currentTimeMillis() + ".txt");
		outputFile.delete();	// delete old file
		
		// input file
		URL url = getClass().getResource("dataToDiscretize2.txt");
		assertNotNull(url);
		File file = new File(url.toURI());
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
			
			if (nominalAttributes.contains(attributeNames[attributeIndex])) {
				// force this attribute to be considered as nominal
				attributeType[attributeIndex] = Attribute.NOMINAL;
				attributeIsString[attributeIndex] = true;
				continue;
			}
			
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
		
		assertEquals("uid", attributeNames[0]);
		
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
		IDiscretization discretization = discretizationFactory.buildInstance(dataSet);
		dataSet = discretization.getInstances();	// use the exact instance used by discretizer (because it may be a clone)
		assertNotNull(dataSet);
		
		assertEquals(expectedNumData, dataSet.instances.length);
		
		
		// check number of columns
		assertEquals(expectedNumAttributes, dataSet.getAttributes().length);
		// check that 1st column is id
		assertEquals("uid", dataSet.getAttribute(0).getAttributeName());
//		assertEquals(Attribute.NOMINAL, dataSet.getAttribute(0).getAttributeType());
		
		
		// check that other columns are numeric
		for (int i = 1; i < dataSet.getAttributes().length; i++) {
			if (attributeType[i] == 0) {
				assertEquals(dataSet.getAttribute(i).getAttributeName(), Attribute.NUMERIC, dataSet.getAttribute(i).getAttributeType());
			} else {
				assertEquals(dataSet.getAttribute(i).getAttributeName(), Attribute.NOMINAL, dataSet.getAttribute(i).getAttributeType());
			}
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
			if (numThresholds > maxBinThreshold) {
				numThresholds = maxBinThreshold;
			}
			
			discretization.transformAttribute(attribute, numThresholds);
			attribute = dataSet.getAttribute(attributeIndex);
			
			assertEquals(attribute.getAttributeName(), Attribute.NOMINAL, attribute.getAttributeType());
		}
		
		
		// save discretized
		FileController.getInstance().saveInstanceSet(outputFile, dataSet, allAttributeIndexes, false);
		
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
				if (numThresholds > maxBinThreshold) {
					numThresholds = maxBinThreshold;
				}
				
				sampler.transformAttribute(attribute, numThresholds);
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
	 * Test of {@link TriangularDistributionSampler}
	 * @throws Exception 
	 */
	@Test
	public final void testTriangularDistributionSamplerFreqBatch2() throws Exception {
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 54; //54 observables, no uid
//		String regexAttributeNotToSave = "wPrev_.+";
		String prefixAttributeToSave = "wNext_";
		
		File folder = new File(getClass().getResource("samplesFreq2/").toURI());
//		folder = new File(getClass().getResource("samplesRange/").toURI());
		
		// columns to append at right of output file
		File fileToAppend = new File(getClass().getResource("recordsToAppend.txt").toURI());;
		
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
				
//				if (!attribute.getAttributeName().matches(regexAttributeNotToSave)) {
//					attributeIndexesToSave.add(attributeIndex);
//				}
				if (attribute.getAttributeName().startsWith(prefixAttributeToSave)) {
					attributeIndexesToSave.add(attributeIndex);
				}
				
				if (attribute.getAttributeType() != Attribute.NOMINAL) {
					continue;	// ignore numeric
				}
				
				// do not use frequency larger than distinct values
				int numThresholds = stat.getDistinctCount();
				if (numThresholds > maxBinThreshold) {
					numThresholds = maxBinThreshold;
				}
				
				// the method name is discretize, but sampler actually does the opposite
				sampler.transformAttribute(attribute, numThresholds);
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
			
			// append data files
			if (fileToAppend != null) {
				// append suffix to output file name
				File appendedOutput = new File(outFileName.replace(".txt", ".csv"));
				appendedOutput.delete();
				CSVMultiEvidenceIO.appendEvidenceDataByColumn(outputFile, fileToAppend, appendedOutput, ',');
			}
			
		}
		
	}
	
	/**
	 * Test of {@link TriangularDistributionSampler}
	 * @throws Exception 
	 */
	@Test
	public final void testTriangularDistributionSamplerFreqBatch2SampleNA() throws Exception {
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 54; //54 observables, no uid
		String regexAttributeNotToSave = "wPrev_.+";
//		String prefixAttributeToSave = "wNext_";
		
		File folder = new File(getClass().getResource("samplesFreq2/").toURI());
		
		assertTrue(folder.exists());
		assertTrue(folder.isDirectory());
		assertTrue(folder.list().length > 0);
		
		for (File file : folder.listFiles()) {
			// batch process files in folder
			assertTrue(file.exists());
			assertTrue(file.getName().endsWith(".txt"));
			
			// append suffix to output file name
			String outFileName = file.getName().replace(".txt", "_triangular_NoNA.txt");
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
				
				if (!attribute.getAttributeName().matches(regexAttributeNotToSave)) {
					attributeIndexesToSave.add(attributeIndex);
				}
//				if (attribute.getAttributeName().startsWith(prefixAttributeToSave)) {
//					attributeIndexesToSave.add(attributeIndex);
//				}
				
				if (attribute.getAttributeType() != Attribute.NOMINAL) {
					continue;	// ignore numeric
				}
				
				// do not use frequency larger than distinct values
				int numThresholds = stat.getDistinctCount();
				if (numThresholds > maxBinThreshold) {
					numThresholds = maxBinThreshold;
				}
				
				// the method name is discretize, but sampler actually does the opposite
				sampler.transformAttribute(attribute, numThresholds);
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
			
			// generate csv file
			File appendedOutput = new File(outFileName.replace(".txt", ".csv"));
			appendedOutput.delete();
			CSVMultiEvidenceIO.appendEvidenceDataByColumn(outputFile, null, appendedOutput, ',');
		}
		
	}
	
	/**
	 * Test of {@link TriangularDistributionSampler}
	 * @throws Exception 
	 */
	@Test
	public final void testTriangularDistributionSamplerFreqBatch3aSampleNA() throws Exception {
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 56; //54 observables + 2 targets
		String regexAttributeNotToSave = "wPrev_.+";
		
		File folder = new File(getClass().getResource("samples3a/").toURI());
		assertTrue(folder.exists());
		assertTrue(folder.isDirectory());
		assertTrue(folder.list().length > 0);
		
		File evidenceFile = new File(getClass().getResource("evidence3.txt").toURI());
		assertTrue(evidenceFile.exists());
		assertTrue(evidenceFile.isFile());
		
		
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
				
				if (!attribute.getAttributeName().matches(regexAttributeNotToSave)) {
					attributeIndexesToSave.add(attributeIndex);
				}
//				if (attribute.getAttributeName().startsWith(prefixAttributeToSave)) {
//					attributeIndexesToSave.add(attributeIndex);
//				}
				
				if (attribute.getAttributeType() != Attribute.NOMINAL) {
					continue;	// ignore numeric
				}
				
				// do not use frequency larger than distinct values
				int numThresholds = stat.getDistinctCount();
				if (numThresholds > maxBinThreshold) {
					numThresholds = maxBinThreshold;
				}
				
				// the method name is discretize, but sampler actually does the opposite
				sampler.transformAttribute(attribute, numThresholds);
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
			
			// generate csv file
			File appendedOutput = new File(outFileName.replace(".txt", ".csv"));
			appendedOutput.delete();
			CSVMultiEvidenceIO.mergeEvidenceDataByColumn(evidenceFile, outputFile, appendedOutput, ',');
			
		}
		
	}
	
	/**
	 * Test of {@link TriangularDistributionSampler}
	 * @throws Exception 
	 */
	@Test
	public final void testBinBoundsSamplerFreqBatch3aSampleNA() throws Exception {
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 56; //54 observables + 2 targets
		String regexAttributeNotToSave = "wPrev_.+";
		
		File folder = new File(getClass().getResource("samples3a/").toURI());
		assertTrue(folder.exists());
		assertTrue(folder.isDirectory());
		assertTrue(folder.list().length > 0);
		
		File evidenceFile = new File(getClass().getResource("evidence3.txt").toURI());
		assertTrue(evidenceFile.exists());
		assertTrue(evidenceFile.isFile());
		
		
		for (File file : folder.listFiles()) {
			// batch process files in folder
			assertTrue(file.exists());
			assertTrue(file.getName().endsWith(".txt"));
			
			// append suffix to output file name
			String outFileName = file.getName().replace(".txt", "_binBound.txt");
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
			ISampler sampler = new BinBoundSampler(dataSet);
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
				
				if (!attribute.getAttributeName().matches(regexAttributeNotToSave)) {
					attributeIndexesToSave.add(attributeIndex);
				}
//				if (attribute.getAttributeName().startsWith(prefixAttributeToSave)) {
//					attributeIndexesToSave.add(attributeIndex);
//				}
				
				if (attribute.getAttributeType() != Attribute.NOMINAL) {
					continue;	// ignore numeric
				}
				
				// do not use frequency larger than distinct values
				int numThresholds = stat.getDistinctCount();
				if (numThresholds > maxBinThreshold) {
					numThresholds = maxBinThreshold;
				}
				
				// the method name is discretize, but sampler actually does the opposite
				sampler.transformAttribute(attribute, numThresholds);
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
			
			// generate csv file
			File appendedOutput = new File(outFileName.replace(".txt", ".csv"));
			appendedOutput.delete();
			CSVMultiEvidenceIO.mergeEvidenceDataByColumn(evidenceFile, outputFile, appendedOutput, ',');
			
		}
		
	}
	
	/**
	 * Test of {@link TriangularDistributionSampler}
	 * @throws Exception 
	 */
	@Test
	public final void testTriangularDistributionSamplerFreqBatch3bSampleNA() throws Exception {
		
		int expectedNumData = 3582;
		int expectedNumAttributes = 37; //36 observables + target behavior
		
		File folder = new File(getClass().getResource("samples3b/").toURI());
		assertTrue(folder.exists());
		assertTrue(folder.isDirectory());
		assertTrue(folder.list().length > 0);
		
		File evidenceFile = new File(getClass().getResource("evidence3.txt").toURI());
		assertTrue(evidenceFile.exists());
		assertTrue(evidenceFile.isFile());
		
		
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
				
//				if (!attribute.getAttributeName().matches(regexAttributeNotToSave)) {
					attributeIndexesToSave.add(attributeIndex);
//				}
				
				if (attribute.getAttributeType() != Attribute.NOMINAL) {
					continue;	// ignore numeric
				}
				
				// do not use frequency larger than distinct values
				int numThresholds = stat.getDistinctCount();
				if (numThresholds > maxBinThreshold) {
					numThresholds = maxBinThreshold;
				}
				
				// the method name is discretize, but sampler actually does the opposite
				sampler.transformAttribute(attribute, numThresholds);
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
			
			// generate csv file
			File appendedOutput = new File(outFileName.replace(".txt", ".csv"));
			appendedOutput.delete();
			CSVMultiEvidenceIO.mergeEvidenceDataByColumn(evidenceFile, outputFile, appendedOutput, ',');
			
		}
		
	}


}
