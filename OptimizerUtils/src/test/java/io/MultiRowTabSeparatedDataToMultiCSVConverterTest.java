/**
 * 
 */
package io;

import java.io.File;

import junit.framework.TestCase;

/**
 * @author Shou Matsumoto
 *
 */
public class MultiRowTabSeparatedDataToMultiCSVConverterTest extends TestCase {

	private MultiRowTabSeparatedDataToMultiCSVConverter converter;
	
	/**
	 * @param name
	 */
	public MultiRowTabSeparatedDataToMultiCSVConverterTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		converter = new MultiRowTabSeparatedDataToMultiCSVConverter();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link io.MultiRowTabSeparatedDataToMultiCSVConverter#main(java.lang.String[])}.
	 */
	public final void testMain() {
		
		File temp = new File(getClass().getName() + "_" + System.currentTimeMillis());
		temp.mkdirs();
		temp.deleteOnExit();
		
		String args[] = new String[13];
		args[0] = "-i";
		args[1] = "\"" + getClass().getResource("../tab-separated-data.txt").getPath() + "\"";
		args[2] = "-o";
		args[3] = temp.getPath();
		args[4] = "-inames";
		args[5] = "\"I1,I2\"";
		args[6] = "-dnames";
		args[7] = "\"D1,D2\"";
		args[8] = "-threat";
		args[9] = "\"Threat\"";
		args[10] = "-alert";
		args[11] = "\"Alert\"";
		args[12] = "-d";
		converter.main(args);
		
		
		assertTrue(temp.exists());
		assertTrue(temp.isDirectory());
		assertEquals(4, temp.listFiles().length);
		for (File file : temp.listFiles()) {
			assertTrue(file.getName(),file.getName().startsWith(converter.getProblemID()));
			assertTrue(file.getName(),file.length() > 0);
			assertTrue(file.delete());
		}
		
		assertTrue(temp.delete());
	}


}
