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
		int level = 0;

		// Level 0
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
		int[] rightOrder = { 0, 2, 1, 3, 4, 6, 5, 7 };
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

}
