/**
 * 
 */
package utils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class DirichletUserSimulatorTest extends TestCase {

	private DirichletUserSimulator sim;
	
	/**
	 * @param name
	 */
	public DirichletUserSimulatorTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(true);
		sim = DirichletUserSimulator.getInstance();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link utils.DirichletUserSimulator#loadConditionals(java.lang.String)}.
	 * @throws IOException 
	 * @throws LoadException 
	 */
	public final void testLoadConditionals() throws LoadException, IOException {
		String conditionalProbabilityFileName = getClass().getResource("../conditionalsSimple.net").getPath();
		List<PotentialTable> conditionals = sim.loadConditionals(conditionalProbabilityFileName);
		
		assertNotNull(conditionals);
		assertFalse(conditionals.isEmpty());
		
		assertEquals(3, conditionals.size());
		
		Collections.sort(conditionals, new Comparator<PotentialTable>() {
			public int compare(PotentialTable o1, PotentialTable o2) {
				if (o1.tableSize() == o2.tableSize()) {
					return o1.getVariableAt(0).getName().compareTo(o2.getVariableAt(0).getName());
				}
				return o1.tableSize() - o2.tableSize();
			}
		});
		
		assertEquals("I1", conditionals.get(0).getVariableAt(0).getName());	// I1 x D1 x D2
		assertEquals("Threat", conditionals.get(1).getVariableAt(0).getName());	// threat x I1 x I2
		assertEquals("I2", conditionals.get(2).getVariableAt(0).getName());	// I2 x D3 x D4
		
		assertEquals(2*2*3, conditionals.get(0).tableSize());	// I1 x D1 x D2
		assertEquals(2*3*2, conditionals.get(1).tableSize());	// threat x I1 x I2
		assertEquals(3*4*5, conditionals.get(2).tableSize());	// I2 x D3 x D4
		
		
		

		float[] probI1 = {1f, 0f, 0.75f, 0.25f, 0.6f, 0.4f, 0.4f, 0.6f, 0.25f, 0.75f, 0f, 1f};
		assertEquals(probI1.length, conditionals.get(0).tableSize());
		for (int i = 0; i < probI1.length; i++) {
			assertEquals(""+i, probI1[i], conditionals.get(0).getValue(i), 0.00005);
		}
		
		
		float[] probThreat = {1f, 0f, .5f, .5f, .5f, .5f, .25f, .75f, .25f, .75f, .01f, .99f};
		assertEquals(probThreat.length, conditionals.get(1).tableSize());
		for (int i = 0; i < probThreat.length; i++) {
			assertEquals(""+i, probThreat[i], conditionals.get(1).getValue(i), 0.00005);
		}
		
	}
	
	/**
	 * Test method for {@link DirichletUserSimulator#propagate(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)}
	 * @throws LoadException
	 * @throws IOException
	 */
	public final void testPropagate() throws LoadException, IOException {
		
		long seed = System.currentTimeMillis();
		Debug.println("Seed = " + seed);
		
		Random rand = new Random(seed);
		
		// create 3 nodes
		ProbabilisticNode node1 = new ProbabilisticNode();
		node1.setName("node1");
		node1.appendState("0");
		node1.appendState("1");
		ProbabilisticNode node2 = new ProbabilisticNode();
		node2.setName("node2");
		node2.appendState("0");
		node2.appendState("1");
		node2.appendState("2");
		ProbabilisticNode node3 = new ProbabilisticNode();
		node3.setName("node3");
		node3.appendState("0");
		node3.appendState("1");
		node3.appendState("2");
		node3.appendState("3");
		
		// create 2 cliques: [node1, node2], [node1, node3]
		Clique clique1 = new Clique();
		clique1.getNodesList().add(node1);
		clique1.getNodesList().add(node2);
		clique1.getProbabilityFunction().addVariable(node2);
		clique1.getProbabilityFunction().addVariable(node1);
		Clique clique2 = new Clique();
		clique2.getNodesList().add(node1);
		clique2.getNodesList().add(node3);
		clique2.getProbabilityFunction().addVariable(node3);
		clique2.getProbabilityFunction().addVariable(node1);
		
		clique1.addChild(clique2);
		clique2.setParent(clique1);
		
		// set probability of clique 2 to some random value
		assertEquals(2*4, clique2.getProbabilityFunction().tableSize());
		for (int i = 0; i < clique2.getProbabilityFunction().tableSize(); i++) {
			clique2.getProbabilityFunction().setValue(i, rand.nextFloat());
		}
		clique2.getProbabilityFunction().normalize();
		assertEquals(1f, clique2.getProbabilityFunction().getSum(), 0.00005f);
		
		
		// set evidence in clique 1: node1 = 1, node2 = 2
		clique1.getProbabilityFunction().fillTable(0f);	// set all other states to 0%
		int[] coord = clique1.getProbabilityFunction().getMultidimensionalCoord(0);
		coord[0] = 2;	// node2 = 2
		coord[1] = 1;	// node0 = 1
		clique1.getProbabilityFunction().setValue(coord, 1f);	// set [node1 = 1, node2 = 2] to 100%
		clique1.getProbabilityFunction().copyData();
		
		// propagate the evidence from clique1 to clique2
		sim.propagate(clique1, clique2);
		
		// make sure the tables are still normalized
		assertEquals(1f, clique1.getProbabilityFunction().getSum(), 0.00005f);
		assertEquals(1f, clique2.getProbabilityFunction().getSum(), 0.00005f);
		
		// make sure clique 1 did not change
		assertEquals(2*3, clique1.getProbabilityFunction().tableSize());
		for (int i = 0; i < clique1.getProbabilityFunction().tableSize(); i++) {
			assertEquals(""+i, clique1.getProbabilityFunction().getCopiedValue(i), clique1.getProbabilityFunction().getValue(i), 0.00005f);
		}
		
		// make sure states in clique2 with node0 != 1 is 0%
		for (int i = 0; i < clique2.getProbabilityFunction().tableSize(); i++) {
			float value = clique2.getProbabilityFunction().getValue(i);
			coord = clique2.getProbabilityFunction().getMultidimensionalCoord(i);
			if (coord[1] != 1) {
				assertEquals(""+i, 0f, value, 0.00005f);
			} else {
				assertTrue("value["+i+"]="+value, value > 0);
			}
		}
		
		
		// create a new clique with no intersection with clique 1
		ProbabilisticNode node4 = new ProbabilisticNode();
		node4.setName("node4");
		node4.appendState("0");
		node4.appendState("1");
		node4.appendState("2");
		node4.appendState("3");
		node4.appendState("4");
		Clique clique3 = new Clique();
		clique3.getNodesList().add(node4);
		clique3.getProbabilityFunction().addVariable(node4);
		clique2.setParent(clique1);
		clique1.addChild(clique3);
		// set probability of clique 3 to some random value
		assertEquals(5, clique3.getProbabilityFunction().tableSize());
		for (int i = 0; i < clique3.getProbabilityFunction().tableSize(); i++) {
			clique3.getProbabilityFunction().setValue(i, rand.nextFloat());
		}
		clique3.getProbabilityFunction().normalize();
		assertEquals(1f, clique3.getProbabilityFunction().getSum(), 0.00005f);
		clique3.getProbabilityFunction().copyData();
		
		// propagate the evidence from clique1 to clique3
		sim.propagate(clique1, clique3);
		

		// make sure clique 1 did not change
		assertEquals(2*3, clique1.getProbabilityFunction().tableSize());
		for (int i = 0; i < clique1.getProbabilityFunction().tableSize(); i++) {
			assertEquals(""+i, clique1.getProbabilityFunction().getCopiedValue(i), clique1.getProbabilityFunction().getValue(i), 0.00005f);
		}
		

		// make sure clique 3 did not change
		assertEquals(5, clique3.getProbabilityFunction().tableSize());
		for (int i = 0; i < clique3.getProbabilityFunction().tableSize(); i++) {
			assertEquals(""+i, clique3.getProbabilityFunction().getCopiedValue(i), clique3.getProbabilityFunction().getValue(i), 0.00005f);
		}
	}

}
