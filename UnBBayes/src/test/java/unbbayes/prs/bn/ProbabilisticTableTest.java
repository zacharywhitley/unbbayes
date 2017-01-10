/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.prs.INode;

/**
 * @author Shou Matsumoto
 *
 */
public class ProbabilisticTableTest extends TestCase {

	ProbabilisticTable table = null;
	ProbabilisticNode node1 = null;
	ProbabilisticNode node2 = null;
	ProbabilisticNode node3 = null;
	
	/**
	 * @param name
	 */
	public ProbabilisticTableTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		node1 = new ProbabilisticNode();
		node1.setName("node1");
		node1.appendState("true");
		node1.appendState("false");
		node1.appendState("absurd");
		
		node2 = new ProbabilisticNode();
		node2.setName("node2");
		node2.appendState("true");
		node2.appendState("false");
		node2.appendState("absurd");
		
		node3 = new ProbabilisticNode();
		node3.setName("node3");
		node3.appendState("true");
		node3.appendState("false");
		node3.appendState("absurd");
		
		table = new ProbabilisticTable();
		table.addVariable(node1);
		table.addVariable(node2);
		table.addVariable(node3);
		
		for (int i = 0; i < table.tableSize(); i++) {
			table.setValue(i, i);
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	

	/**
	 * Test method for {@link unbbayes.prs.bn.PotentialTable#retainVariables(java.util.Collection, boolean)}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final void testRetainVariables() {
		assertEquals(3, table.getVariablesSize());
		Collection<INode> toRetain = new ArrayList<INode>(2);
		toRetain.add(node2);
		toRetain.add(node3);
		table.retainVariables(toRetain);
		assertEquals(2, table.getVariablesSize());
		assertTrue(table.getVariableIndex(node1) < 0);
		for (int i = 0, iOrig = 0; i < table.tableSize(); i++, iOrig += 3) {
			assertEquals("i = " + i + ", iOrig = " + iOrig, (3*iOrig)+3, table.getValue(i), 0.1);
		}
		
		table.retainVariables(toRetain);
		assertEquals(2, table.getVariablesSize());
		assertTrue(table.getVariableIndex(node1) < 0);
		for (int i = 0, iOrig = 0; i < table.tableSize(); i++, iOrig += 3) {
			assertEquals("i = " + i + ", iOrig = " + iOrig, (3*iOrig)+3, table.getValue(i), 0.1);
		}
		
		table.retainVariables((List)Collections.singletonList(node3));
		assertEquals(1, table.getVariablesSize());
		assertTrue(table.getVariableIndex(node1) < 0);
		assertTrue(table.getVariableIndex(node2) < 0);
		for (int i = 0, iOrig = 0; i < table.tableSize(); i++, iOrig += 9) {
			assertEquals("i = " + i + ", iOrig = " + iOrig, (9*iOrig)+36, table.getValue(i), 0.1);
		}
		
		table.retainVariables((List)Collections.singletonList(node3));
		assertEquals(1, table.getVariablesSize());
		assertTrue(table.getVariableIndex(node1) < 0);
		assertTrue(table.getVariableIndex(node2) < 0);
		for (int i = 0, iOrig = 0; i < table.tableSize(); i++, iOrig += 9) {
			assertEquals("i = " + i + ", iOrig = " + iOrig, (9*iOrig)+36, table.getValue(i), 0.1);
		}
		
		table.retainVariables(null);
		assertEquals(0, table.getVariablesSize());
	}
	

	/**
	 * Test method for {@link unbbayes.prs.bn.PotentialTable#getEntropy(int)}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void testGetEntropy() {
		assertEquals(3, table.getVariablesSize());
		assertEquals(27, table.tableSize());
		try {
			table.getEntropy(-1);
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		try {
			table.getEntropy(3);
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		
		
		// entropy must be -SUM[P(x)*log(P(x))]
		double expectedEntropy = 0;
		PotentialTable clone = table.getTemporaryClone();
		clone.normalize();
		clone.retainVariables((List)Collections.singletonList(node1));
		assertEquals(3, clone.tableSize());
		assertTrue(0f <= clone.getValue(0) && clone.getValue(0) <= 1f);
		assertTrue(0f <= clone.getValue(1) && clone.getValue(1) <= 1f);
		assertTrue(0f <= clone.getValue(2) && clone.getValue(2) <= 1f);
		for (int i = 0; i < clone.tableSize(); i++) {
			expectedEntropy += (clone.getValue(i) * Math.log(clone.getValue(i)));
		}
		expectedEntropy = -expectedEntropy;
		assertEquals(expectedEntropy, table.getEntropy(0), 0.0001);
		assertFalse(table.getEntropy(0) < 0);
		assertFalse(Double.isNaN(table.getEntropy(0)));
		assertFalse(Double.isInfinite(table.getEntropy(0)));
		
		expectedEntropy = 0;
		clone = table.getTemporaryClone();
		clone.normalize();
		clone.retainVariables((List)Collections.singletonList(node2));
		assertEquals(3, clone.tableSize());
		assertTrue(0f <= clone.getValue(0) && clone.getValue(0) <= 1f);
		assertTrue(0f <= clone.getValue(1) && clone.getValue(1) <= 1f);
		assertTrue(0f <= clone.getValue(2) && clone.getValue(2) <= 1f);
		for (int i = 0; i < clone.tableSize(); i++) {
			expectedEntropy += (clone.getValue(i) * Math.log(clone.getValue(i)));
		}
		expectedEntropy = -expectedEntropy;
		assertEquals(expectedEntropy, table.getEntropy(1), 0.0001);
		assertFalse(table.getEntropy(1) < 0);
		assertFalse(Double.isNaN(table.getEntropy(1)));
		assertFalse(Double.isInfinite(table.getEntropy(1)));
		
		expectedEntropy = 0;
		clone = table.getTemporaryClone();
		clone.normalize();
		clone.retainVariables((List)Collections.singletonList(node3));
		assertEquals(3, clone.tableSize());
		assertTrue(0f <= clone.getValue(0) && clone.getValue(0) <= 1f);
		assertTrue(0f <= clone.getValue(1) && clone.getValue(1) <= 1f);
		assertTrue(0f <= clone.getValue(2) && clone.getValue(2) <= 1f);
		for (int i = 0; i < clone.tableSize(); i++) {
			expectedEntropy += (clone.getValue(i) * Math.log(clone.getValue(i)));
		}
		expectedEntropy = -expectedEntropy;
		assertEquals(expectedEntropy, table.getEntropy(2), 0.0001);
		assertFalse(table.getEntropy(2) < 0);
		assertFalse(Double.isNaN(table.getEntropy(2)));
		assertFalse(Double.isInfinite(table.getEntropy(2)));
		
		assertEquals(3, table.getVariablesSize());
		assertEquals(27, table.tableSize());
	}
	

	/**
	 * Test method for {@link unbbayes.prs.bn.PotentialTable#getMutualInformation(int, int)}.
	 */
	public final void testGetMutualInformation() {
		assertEquals(3, table.getVariablesSize());
		assertEquals(27, table.tableSize());
		
		for (int i = 0; i < 3; i++) {
			try {
				table.getMutualInformation(-1, i);
				fail("At " + i);
			} catch (ArrayIndexOutOfBoundsException e) {}
			try {
				table.getMutualInformation(i, -1);
				fail("At " + i);
			} catch (ArrayIndexOutOfBoundsException e) {}
			try {
				table.getMutualInformation(i, 3);
				fail("At " + i);
			} catch (ArrayIndexOutOfBoundsException e) {}
			try {
				table.getMutualInformation(3, i);
				fail("At " + i);
			} catch (ArrayIndexOutOfBoundsException e) {}
		}
		
		
		// mutual info of same node must be equal to entropy
		assertEquals(table.getEntropy(0), table.getMutualInformation(0, 0), 0.0001);
		assertEquals(table.getEntropy(1), table.getMutualInformation(1, 1), 0.0001);
		assertEquals(table.getEntropy(2), table.getMutualInformation(2, 2), 0.0001);
		
		// mutual information must be symmetric, non-negative, non-infinite, and not a NaN
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				// symmetric
				assertEquals("i="+i+",j="+j, table.getMutualInformation(i, j), table.getMutualInformation(j, i), 0.0001);
				// non-negative
				assertFalse("i="+i+",j="+j, table.getMutualInformation(i, j) < 0);
				// non-infinite
				assertFalse("i="+i+",j="+j, Double.isInfinite(table.getMutualInformation(i, j)));
				// not a NaN
				assertFalse("i="+i+",j="+j, Double.isNaN(table.getMutualInformation(i, j)));
			}
		}
		
		assertEquals(3, table.getVariablesSize());
		assertEquals(27, table.tableSize());
	}

}
