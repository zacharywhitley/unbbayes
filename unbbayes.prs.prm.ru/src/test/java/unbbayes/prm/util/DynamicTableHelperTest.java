package unbbayes.prm.util;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import unbbayes.prm.util.helper.DynamicTableHelper;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;

public class DynamicTableHelperTest {

	ProbabilisticNode parentNode1;
	ProbabilisticNode parentNode2;
	ProbabilisticNode parentNode3;
	ProbabilisticNode childNode;
	private PotentialTable cpt;

	@Before
	public void setUp() throws Exception {
		parentNode1 = new ProbabilisticNode();
		parentNode1.setName("Parent1");
		parentNode1.appendState("A");
		parentNode1.appendState("B");

		parentNode2 = new ProbabilisticNode();
		parentNode2.setName("Parent2");
		parentNode2.appendState("C");
		parentNode2.appendState("D");

		parentNode3 = new ProbabilisticNode();
		parentNode3.setName("Parent3");
		parentNode3.appendState("E");
		parentNode3.appendState("F");

		childNode = new ProbabilisticNode();
		childNode.setName("Child");
		childNode.appendState("T");
		childNode.appendState("F");

		childNode.addParent(parentNode1);
		childNode.addParent(parentNode2);
		childNode.addParent(parentNode3);

		cpt = childNode.getProbabilityFunction();
		cpt.addVariable(childNode);
		cpt.addVariable(parentNode1);
		cpt.addVariable(parentNode2);
		cpt.addVariable(parentNode3);

		System.out.println("Var size " + cpt.getVariablesSize());
		System.out.println("Var count " + cpt.variableCount());
	}

	@Test
	public void statesOrderInCptTest() {
		// Level 0
		int level = 0;
		int[] rightOrder = { 0, 1, 2, 3, 4, 5, 6, 7 };

		int numColumns = DynamicTableHelper.getNumColumns(cpt);
		System.out.println("Number of columns: " + numColumns);

		int numSubStates = DynamicTableHelper.getNumSubStates(level, cpt);
		System.out.println("sub states = " + numSubStates);
		assertTrue(numSubStates == 4);

		int numUpperStates = DynamicTableHelper.getNumUpperStates(level, cpt);
		assertTrue(numUpperStates == 0);

		int[] statesOrderInCpt = DynamicTableHelper.statesOrderInCpt(
				numColumns, childNode.getStatesSize(), numUpperStates);

		System.out.println("Order for level " + level);
		for (int i = 0; i < numColumns; i++) {
			System.out.print(statesOrderInCpt[i] + " ");
			assertTrue(statesOrderInCpt[i] == i);
		}

		// Level 1
		level = 1;
		rightOrder = new int[] { 0, 2, 1, 3, 4, 6, 5, 7 };
		numSubStates = DynamicTableHelper.getNumSubStates(level, cpt);
		numUpperStates = DynamicTableHelper.getNumUpperStates(level, cpt);
		assertTrue(numUpperStates == 2);
		statesOrderInCpt = DynamicTableHelper.statesOrderInCpt(numColumns,
				childNode.getStatesSize(), numUpperStates);

		System.out.println("\nOrder for level " + level);
		for (int i = 0; i < numColumns; i++) {
			System.out.print(statesOrderInCpt[i] + " ");
			assertTrue(statesOrderInCpt[i] == rightOrder[i]);
		}

		// Level 2
		level = 2;
		rightOrder = new int[] { 0, 4, 1, 5, 2, 6, 3, 7 };
		numSubStates = DynamicTableHelper.getNumSubStates(level, cpt);
		numUpperStates = DynamicTableHelper.getNumUpperStates(level, cpt);
		assertTrue(numUpperStates == 4);
		statesOrderInCpt = DynamicTableHelper.statesOrderInCpt(numColumns,
				childNode.getStatesSize(), numUpperStates);

		System.out.println("\nOrder for level " + level);
		for (int i = 0; i < numColumns; i++) {
			System.out.print(statesOrderInCpt[i] + " ");
			assertTrue(statesOrderInCpt[i] == rightOrder[i]);
		}
	}

	@Test
	public void statesOrderInCptTest2() {
		parentNode3.appendState("Z");
		int numColumns = DynamicTableHelper.getNumColumns(cpt);
		assertTrue(numColumns == 12);

		// Level 0
		int level = 0;
		System.out.println("level " + level);

		int[] initialOrder = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		int numUpperStates = DynamicTableHelper.getNumUpperStates(level, cpt);

		int[] statesOrderInCpt = DynamicTableHelper.statesOrderInCpt(
				numColumns, childNode.getStatesSize(), numUpperStates);

		validateArray(statesOrderInCpt, initialOrder);

		// Level 2
		level = 2;
		System.out.println("\n level "+level);
		numUpperStates = DynamicTableHelper.getNumUpperStates(level, cpt);
		statesOrderInCpt = DynamicTableHelper.statesOrderInCpt(numColumns,
				childNode.getStatesSize(), numUpperStates);

		initialOrder = new int[]{ 0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11 };
		
		validateArray(statesOrderInCpt, initialOrder);
	}

	@Test
	public void distributeOrder() {
		int numColumns = DynamicTableHelper.getNumColumns(cpt);

		// Level 0
		int level = 0;
		System.out.println("level " + level);

		int[] initialOrder = { 0, 1, 2, 3, 4, 5, 6, 7 };

		int[] ordered = DynamicTableHelper.addLevel(level, cpt, childNode,
				numColumns, initialOrder);

		int[] finalResult = { 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 4, 5, 6, 7 };

		validateArray(ordered, finalResult);

		// Level 1
		level = 1;
		System.out.println("\nlevel " + level);

		initialOrder = new int[] { 0, 2, 1, 3, 4, 6, 5, 7 };

		ordered = DynamicTableHelper.addLevel(level, cpt, childNode,
				numColumns, initialOrder);

		finalResult = new int[] { 0, 2, 0, 2, 1, 3, 1, 3, 4, 6, 4, 6, 5, 7, 5,
				7 };

		validateArray(ordered, finalResult);

		// Level 2
		level = 2;
		System.out.println("\nlevel " + level);

		initialOrder = new int[] { 0, 4, 1, 5, 2, 6, 3, 7 };

		ordered = DynamicTableHelper.addLevel(level, cpt, childNode,
				numColumns, initialOrder);

		finalResult = new int[] { 0, 0, 4, 4, 1, 1, 5, 5, 2, 2, 6, 6, 3, 3, 7,
				7 };

		validateArray(ordered, finalResult);

	}

	@Test
	public void distributeOrder2() {
		parentNode3.appendState("Z");
		int numColumns = DynamicTableHelper.getNumColumns(cpt);
		assertTrue(numColumns == 12);

		// Level 0
		int level = 0;
		System.out.println("level " + level);

		int[] initialOrder = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

		int[] ordered = DynamicTableHelper.addLevel(level, cpt, childNode,
				numColumns, initialOrder);

		int[] finalResult = { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
				10, 11, 6, 7, 8, 9, 10, 11 };

		validateArray(ordered, finalResult);

		// Level 2
		level = 2;
		int numUpperStates = DynamicTableHelper.getNumUpperStates(level, cpt);
		int[] statesOrderInCpt = DynamicTableHelper.statesOrderInCpt(
				numColumns, childNode.getStatesSize(), numUpperStates);
		// TODO

		ordered = DynamicTableHelper.addLevel(level, cpt, childNode,
				numColumns, statesOrderInCpt);
		finalResult = new int[] { 0, 0, 0, 4, 4, 4, 1, 1, 1, 5, 5, 5, 2, 2, 2,
				6, 6, 6, 3, 3, 3, 7, 7, 7, 8, 8, 8, 12, 12, 12 };

	}

	private void validateArray(int[] ordered, int[] finalResult) {
		for (int i = 0; i < finalResult.length; i++) {
			System.out.print(ordered[i] + " ");
			assertTrue(finalResult[i] == ordered[i]);
		}
	}

}
