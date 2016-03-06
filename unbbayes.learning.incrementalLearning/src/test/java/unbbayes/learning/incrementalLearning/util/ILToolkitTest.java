/**
 * 
 */
package unbbayes.learning.incrementalLearning.util;

import java.util.Random;

import junit.framework.TestCase;

/**
 * @author Shou Matsumoto
 *
 */
public class ILToolkitTest extends TestCase {

	/**
	 * @param name
	 */
	public ILToolkitTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.learning.incrementalLearning.util.ILToolkit#toIntArray(float[][])}.
	 */
	public final void testToIntArray() {
		ILToolkit kit = new ILToolkit();
		
		// do a test on a known array
		float[][] floatArray = {
				{0.1f, 0.6f},
				{1.1f, 1.6f},
				{100.1f, 100.6f},
				{9999.1f, 9999.6f},
				{-10.1f, -10.6f},
		};
		int[][] expected = {
				{0, 1},
				{1, 2},
				{100, 101},
				{9999, 10000},
				{-10, -11},
		};
		
		
		int[][] ret = kit.toIntArray(floatArray);
		
		assertNotNull(ret);
		assertEquals(5,ret.length);
		assertEquals(2,ret[0].length);
		
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[0].length; j++) {
				assertEquals("["+i+"]"+"["+j+"]",expected[i][j], ret[i][j]);
			}
		}
		
		// run test on random array
		
		long seed = System.currentTimeMillis();
		System.out.println("seed = " + seed);
		Random rand = new Random(seed);
		
		int sizeI = 0;
		do {
			sizeI = Math.abs(rand.nextInt()) % 2000;
		} while (sizeI <= 0);
			
		int sizeJ = 0;
		do {
			sizeJ = Math.abs(rand.nextInt()) % 2000;
		} while (sizeJ <= 0);
		
		System.out.println("sizeI = " + sizeI + ", sizeJ = " + sizeJ);
		
		floatArray = new float[sizeI][sizeJ];
		for (int i = 0; i < sizeI; i++) {
			for (int j = 0; j < sizeJ; j++) {
				floatArray[i][j] = rand.nextFloat();
			}
		}
		
		ret = kit.toIntArray(floatArray);
		
		assertNotNull("seed = " + seed, ret);
		assertEquals("seed = " + seed, sizeI, ret.length);
		assertEquals("seed = " + seed, sizeJ, ret[0].length);
		
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[0].length; j++) {
				assertEquals("seed = " + seed + ", ["+i+"]"+"["+j+"]", Math.round(floatArray[i][j]) , ret[i][j]);
			}
		}
		
	}

}
