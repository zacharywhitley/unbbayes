/**
 * 
 */
package unbbayes.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

/**
 * @author Shou Matsumoto
 *
 */
public class SingleValueListTest extends TestCase {

	/**
	 * @param name
	 */
	public SingleValueListTest(String name) {
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
	 * Test method for {@link unbbayes.util.SingleValueList#size()}.
	 */
	public final void testSize() {
		for (int size = -10; size < 100; size++) {
			try {
				SingleValueList list = new SingleValueList(this, size);
				if (size < 0) {
					fail("Should throw exception when size is " + size);
				}
				assertEquals(size, list.size());
			} catch (Exception e) {
				if (size >= 0) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#isEmpty()}.
	 */
	public final void testIsEmpty() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for (int i = 0; i < 100; i++) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			SingleValueList list = new SingleValueList(this, rand.nextInt(Integer.MAX_VALUE));
			if (rand.nextBoolean()) {
				list = new SingleValueList(this, 0);
			}
			assertEquals("seed=" + seed + ", i="+i, list.size() <= 0 , list.isEmpty());
		}
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#contains(java.lang.Object)}.
	 */
	public final void testContains() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		Integer value = rand.nextInt(100);
		
		assertFalse(new SingleValueList<Integer>(value, 0).contains(value));
		
		SingleValueList<Integer> list = new SingleValueList<Integer>(value, 1+rand.nextInt(100));
		for (int i = 0; i < 100; i++) {
			assertEquals("seed=" + seed + ", i="+i + ", value=" + value, i == value.intValue() , list.contains(i));
		}
		
		list = new SingleValueList<Integer>(value, 1+rand.nextInt(100));
		
		List<Integer> listWithoutValue = new ArrayList<Integer>();
		listWithoutValue.add(value - 1);
		listWithoutValue.add(value - 1);
		listWithoutValue.add(value + 1);
		listWithoutValue.add(value + 1);
		
		List<Integer> listWithValue = new ArrayList<Integer>();
		listWithValue.add(value);
		listWithValue.add(value);
		
		List<Integer> listWithBoth = new ArrayList<Integer>(listWithoutValue);
		listWithBoth.addAll(listWithValue);
		
		
		assertFalse(list.containsAll(listWithoutValue));
		assertTrue(list.containsAll(listWithValue));
		assertFalse(list.containsAll(listWithBoth));
		
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#iterator()}.
	 */
	public final void testIterator() {
		// just check if iterator is iterating correct number of times
		
		// check empty list
		SingleValueList<Object> list = new SingleValueList<Object>(this, 0);
		long iteration = 0;
		for (Object object : list) {
			assertEquals(this,object);
			iteration++;
		}
		assertEquals(0, iteration);
		
		// check singleton list
		list = new SingleValueList<Object>(this, 1);
		iteration = 0;
		for (Object object : list) {
			assertEquals(this,object);
			assertEquals(list.get((int)iteration),object);
			iteration++;
		}
		assertEquals(1, iteration);
		
		// check list with 100 elements
		list = new SingleValueList<Object>(this, 100);
		iteration = 0;
		for (Object object : list) {
			assertEquals(this,object);
			assertEquals(list.get((int)iteration),object);
			iteration++;
		}
		assertEquals(100, iteration);
		
		// check list with max possible number of elements
		list = new SingleValueList<Object>(this, 9999);
		iteration = 0;
		for (Object object : list) {
			assertEquals(this,object);
			assertEquals(list.get((int)iteration),object);
			iteration++;
		}
		assertEquals(9999, iteration);
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#toArray()}.
	 */
	public final void testToArray() {
		// check for different sizes
		SingleValueList<Float> list = new SingleValueList<Float>(666f, 0);
		Object[] array = list.toArray();
		assertEquals(0, array.length);
		
		list = new SingleValueList<Float>(666f, 1);
		array = list.toArray();
		assertEquals(1, array.length);
		for (Object object : array) {
			assertEquals(666f, ((Float)object).floatValue(), 0.0001f);
		}
		
		list = new SingleValueList<Float>(13f, 100);
		array = list.toArray();
		assertEquals(100, array.length);
		for (Object object : array) {
			assertEquals(13f, ((Float)object).floatValue(), 0.0001f);
		}
		
		list = new SingleValueList<Float>(1313f, 100);
		array = list.toArray();
		assertEquals(100, array.length);
		for (Object object : array) {
			assertEquals(1313f, ((Float)object).floatValue(), 0.0001f);
		}
		
		list = new SingleValueList<Float>(999f, 1000);
		array = list.toArray();
		assertEquals(1000, array.length);
		for (Object object : array) {
			assertEquals(999f, ((Float)object).floatValue(), 0.0001f);
		}
	}


	/**
	 * Test method for {@link unbbayes.util.SingleValueList#remove(java.lang.Object)}.
	 */
	public final void testRemove() {
		// check for different sizes
		
		SingleValueList<Integer> list = new SingleValueList<Integer>(666, 0);
		assertFalse(list.remove(new Integer(666)));
		
		list = new SingleValueList<Integer>(666, 1);
		assertFalse(list.remove(new Integer(13)));
		assertFalse(list.isEmpty());
		assertTrue(list.remove(new Integer(666)));
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 100);
		assertFalse(list.remove(new Integer(13)));
		assertFalse(list.isEmpty());
		assertTrue(list.remove(new Integer(666)));
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 1000);
		assertFalse(list.remove(new Integer(13)));
		assertFalse(list.isEmpty());
		assertTrue(list.remove(new Integer(666)));
		assertTrue(list.isEmpty());
		
		List<Integer> listToRemove = new ArrayList<Integer>();
		listToRemove.add(13);
		listToRemove.add(13);
		listToRemove.add(666);
		listToRemove.add(666);
		listToRemove.add(1313);
		listToRemove.add(1313);
		
		List<Integer> listNotToRemove = new ArrayList<Integer>();
		listNotToRemove.add(13);
		listNotToRemove.add(13);
		listNotToRemove.add(1313);
		listNotToRemove.add(1313);
		
		list = new SingleValueList<Integer>(666, 0);
		assertFalse(list.removeAll(listToRemove));
		assertFalse(list.removeAll(listNotToRemove));
		
		list = new SingleValueList<Integer>(666, 1);
		assertFalse(list.removeAll(listNotToRemove));
		assertFalse(list.isEmpty());
		assertTrue(list.removeAll(listToRemove));
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 100);
		assertFalse(list.removeAll(listNotToRemove));
		assertFalse(list.isEmpty());
		assertTrue(list.removeAll(listToRemove));
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 1000);
		assertFalse(list.removeAll(listNotToRemove));
		assertFalse(list.isEmpty());
		assertTrue(list.removeAll(listToRemove));
		assertTrue(list.isEmpty());
		
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#clear()}.
	 */
	public final void testClear() {

		SingleValueList<Integer> list = new SingleValueList<Integer>(666, 0);
		list.clear();
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 1);
		assertFalse(list.isEmpty());
		list.clear();
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 100);
		assertFalse(list.isEmpty());
		list.clear();
		assertTrue(list.isEmpty());
		
		list = new SingleValueList<Integer>(666, 1000);
		assertFalse(list.isEmpty());
		list.clear();
		assertTrue(list.isEmpty());
		
	}
	


	/**
	 * Test method for {@link unbbayes.util.SingleValueList#retainAll(java.util.Collection)}.
	 */
	public final void testRetainAll() {
		
		List<Integer> listWithoutValue = new ArrayList<Integer>();
		listWithoutValue.add(0);
		listWithoutValue.add(1);
		listWithoutValue.add(1);
		listWithoutValue.add(0);
		
		List<Integer> listWithValue = new ArrayList<Integer>();
		listWithValue.add(666);
		listWithValue.add(666);
		
		List<Integer> listWithBoth = new ArrayList<Integer>(listWithoutValue);
		listWithBoth.addAll(listWithValue);
		
		
		List<Integer>list = new SingleValueList<Integer>(666, 0);
		assertFalse(list.retainAll(Collections.EMPTY_LIST));
		assertFalse(list.retainAll(listWithoutValue));
		assertFalse(list.retainAll(listWithValue));
		assertFalse(list.retainAll(listWithBoth));
		assertEquals(0, list.size());
		
		for (int size = 1; size < 2049; size*=2) {
			list = new SingleValueList<Integer>(666, size);
			assertTrue(list.retainAll(Collections.EMPTY_LIST));
			assertEquals(0, list.size());
			
			list = new SingleValueList<Integer>(666, size);
			assertTrue(list.retainAll(listWithoutValue));
			assertEquals(0, list.size());
			
			list = new SingleValueList<Integer>(666, size);
			assertFalse(list.retainAll(listWithValue));
			assertEquals(size, list.size());
			
			list = new SingleValueList<Integer>(666, size);
			assertFalse(list.retainAll(listWithBoth));
			assertEquals(size, list.size());
		}
	}


	/**
	 * Test method for {@link unbbayes.util.SingleValueList#set(int, java.lang.Object)}.
	 */
	public final void testSet() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		
		// check empty list first
		SingleValueList<Integer> list = new SingleValueList<Integer>(13, 0);
		assertTrue(list.isEmpty());
		try {
			list.set(0, 666);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { 
			assertTrue(list.isEmpty());
		}
		
		for (int i = 0; i < 100; i++) {
			// randomly create a list
			Integer value = rand.nextInt();
			int size = 1 + rand.nextInt(100);
			list = new SingleValueList<Integer>(value, size);
			// check content of list before change
			for (int j = 0; j < size; j++) {
				assertEquals("seed=" + seed + ", i=" + i + ", j="+j, value, list.get(j));
			}
			// change the content of some value in the list
			value = rand.nextInt();
			list.set(rand.nextInt(size), value);	// changing element at some random position
			// check the content after change
			for (int j = 0; j < size; j++) {
				assertEquals("seed=" + seed + ", i=" + i + ", j="+j, value, list.get(j));
			}
		}
	}


	/**
	 * Test method for {@link unbbayes.util.SingleValueList#indexOf(java.lang.Object)}.
	 */
	public final void testIndexOf() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		
		// check empty list
		SingleValueList<Integer> list = new SingleValueList<Integer>(13, 0);
		assertTrue(list.isEmpty());
		assertTrue(list.indexOf(13) < 0);
		
		for (int i = 0; i < 100; i++) {
			// randomly create a list
			Integer value = rand.nextInt();
			int size = 1 + rand.nextInt(100);
			list = new SingleValueList<Integer>(value, size);
			assertEquals(0 , list.indexOf(value));
			assertTrue(list.indexOf(value+1) < 0);
			assertTrue(list.indexOf(value-1) < 0);
		}
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#lastIndexOf(java.lang.Object)}.
	 */
	public final void testLastIndexOf() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		
		// check empty list
		SingleValueList<Integer> list = new SingleValueList<Integer>(13, 0);
		assertTrue(list.isEmpty());
		assertTrue(list.lastIndexOf(13) < 0);
		
		for (int i = 0; i < 100; i++) {
			// randomly create a list
			Integer value = rand.nextInt();
			int size = 1 + rand.nextInt(100);
			list = new SingleValueList<Integer>(value, size);
			assertEquals(size-1 , list.lastIndexOf(value));
			assertTrue(list.lastIndexOf(value+1) < 0);
			assertTrue(list.lastIndexOf(value-1) < 0);
		}
	}

	/**
	 * Test method for {@link unbbayes.util.SingleValueList#subList(int, int)}.
	 */
	public final void testSubList() {
		// check empty list first
		SingleValueList list = new SingleValueList(this, 0);
		try {
			list.subList(-1, -1);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { }
		try {
			list.subList(-1, 0);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { }
		try {
			list.subList(0, -1);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { }
		try {
			list.subList(0, 0);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { }
		try {
			list.subList(0, 100);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { }
		try {
			list.subList(100, 0);
			fail("Should throw exception");
		} catch (IndexOutOfBoundsException e) { }
		
		//check for a list with 100 size
		for (int i = -10; i < 20; i++) {
			for (int j = -10; j < 20; j++) {
				list = new SingleValueList(this, 100);
				try {
					List subList = list.subList(i, j);
					if (i < 0 || j < 0
							|| i >= 100 || j > 100
							|| i > j) {
						fail("Should throw exception");
					}
					assertEquals(j-i, subList.size());
					for (int k = 0; k < subList.size(); k++) {
						assertEquals(this, subList.get(k));
					}
				} catch (IndexOutOfBoundsException e) { 
					if (!(i < 0 || j < 0
							|| i >= 100 || j > 100
							|| i > j)) {
						throw e;
					}
				}
			}
		}
		
	}

}
