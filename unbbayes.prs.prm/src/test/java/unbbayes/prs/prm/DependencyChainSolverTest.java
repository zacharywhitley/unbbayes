/**
 * 
 */
package unbbayes.prs.prm;

import java.util.List;

import unbbayes.controller.prm.IDatabaseController;
import unbbayes.prs.prm.cpt.AggregateFunctionMode;
import junit.framework.TestCase;

/**
 * Test case for {@link DependencyChainSolver}
 * @author Shou Matsumoto
 *
 */
public class DependencyChainSolverTest extends TestCase {
	
	private IDependencyChainSolver chainSolverToTest;
	
	private IDatabaseController databaseController;
	
	private PRM prm;
	private PRMClass classA;
	private AttributeDescriptor attributeAa;
	private AttributeDescriptor attributeAb;
	private AttributeDescriptor attributeAc;
	private AttributeDescriptor attributeApkA;
	private PRMClass classB;
	private AttributeDescriptor attributeBa;
	private AttributeDescriptor attributeBfkC;
	private AttributeDescriptor attributeBfkD;
	private AttributeDescriptor attributeBpkB;
	private PRMClass classC;
	private AttributeDescriptor attributeCa;
	private AttributeDescriptor attributeCfkA;
	private AttributeDescriptor attributeCfkD;
	private AttributeDescriptor attributeCpkC;
	private PRMClass classD;
	private AttributeDescriptor attributeDa;
	private AttributeDescriptor attributeDpkD;
	private ForeignKey fkBC;
	private ForeignKey fkBD;
	private ForeignKey fkCA;
	private ForeignKey fkCD;
	private PRMObject a1;
	private PRMObject b1;
	private IPRMObject b2;
	private IPRMObject c1;
	private IPRMObject c2;
	private PRMObject d1;
	private String pkValueA1 = "a1";
	private String pkValueB1 = "b1";
	private String pkValueB2 = "b2";
	private String pkValueC1 = "c1";
	private String pkValueC2 = "c2";
	private String pkValueD1 = "d1";

	/**
	 * @param name
	 */
	public DependencyChainSolverTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		this.chainSolverToTest = DependencyChainSolver.newInstance(null);

		this.prm = PRM.newInstance("testPRM");

		/*
		 * Creating class schema:
		 *  Possible states/values: "true", "false", and object names for pk* and fk* (e.g. "A1", "B1"...).
		 */
		
		// A: a, b, c, pkA, and possible values: true, false
		this.classA = PRMClass.newInstance(this.prm, "A");
		this.attributeAa = AttributeDescriptor.newInstance(this.classA, "a");		// also automatically add attribute to class
		this.attributeAa.appendState("true");
		this.attributeAa.appendState("false");
		this.attributeAb = AttributeDescriptor.newInstance(this.classA, "b");		// also automatically add attribute to class
		this.attributeAb.appendState("true");
		this.attributeAb.appendState("false");
		this.attributeAc = AttributeDescriptor.newInstance(this.classA, "c");		// also automatically add attribute to class
		this.attributeAc.appendState("true");
		this.attributeAc.appendState("false");
		this.attributeApkA = AttributeDescriptor.newInstance(this.classA, "pkA");	// also automatically add attribute to class
		this.attributeApkA.setPrimaryKey(true);
		this.attributeApkA.setMandatory(true);
		
		// B: a, fkC, fkD, pkB, and possible values: true, false
		this.classB = PRMClass.newInstance(this.prm, "B");
		this.attributeBa = AttributeDescriptor.newInstance(this.classB, "a");		// also automatically add attribute to class
		this.attributeBa.appendState("true");
		this.attributeBa.appendState("false");
		this.attributeBfkC = AttributeDescriptor.newInstance(this.classB, "fkC");	// also automatically add attribute to class
		this.attributeBfkD = AttributeDescriptor.newInstance(this.classB, "fkD");	// also automatically add attribute to class
		this.attributeBpkB = AttributeDescriptor.newInstance(this.classB, "pkB");	// also automatically add attribute to class
		this.attributeBpkB.setPrimaryKey(true);
		this.attributeBpkB.setMandatory(true);
		
		// C:  C: a, fkA, fkD, pkC, and possible values: true, false
		this.classC = PRMClass.newInstance(this.prm, "C");
		this.attributeCa = AttributeDescriptor.newInstance(this.classC, "a");		// also automatically add attribute to class
		this.attributeCa.appendState("true");
		this.attributeCa.appendState("false");
		this.attributeCfkA = AttributeDescriptor.newInstance(this.classC, "fkA");	// also automatically add attribute to class
		this.attributeCfkD = AttributeDescriptor.newInstance(this.classC, "fkD");	// also automatically add attribute to class
		this.attributeCpkC = AttributeDescriptor.newInstance(this.classC, "pkC");	// also automatically add attribute to class
		this.attributeCpkC.setPrimaryKey(true);
		this.attributeCpkC.setMandatory(true);
		
		// D: a, pkD and possible values: true, false
		this.classD = PRMClass.newInstance(this.prm, "D");
		this.attributeDa = AttributeDescriptor.newInstance(this.classD, "a");		// also automatically add attribute to class
		this.attributeDa.appendState("true");
		this.attributeDa.appendState("false");
		this.attributeDpkD = AttributeDescriptor.newInstance(this.classD, "pkD");	// also automatically add attribute to class
		this.attributeDpkD.setPrimaryKey(true);
		this.attributeDpkD.setMandatory(true);
		
		// set up foreign keys
		
		// set up foreign key: B -> fkC -> C
		this.fkBC = ForeignKey.newInstance();
		this.fkBC.setName("B.fkC");
		this.fkBC.setClassFrom(this.classB);
		this.fkBC.getKeyAttributesFrom().add(this.attributeBfkC);
		this.attributeBfkC.setForeignKeyReference(fkBC);
		this.fkBC.setClassTo(this.classC);
		this.fkBC.getKeyAttributesTo().add(this.attributeCpkC);
		this.classB.getForeignKeys().add(this.fkBC);
		this.classC.getIncomingForeignKeys().add(this.fkBC);
		
		// set up foreign key: B -> fkD -> D
		this.fkBD = ForeignKey.newInstance();
		this.fkBD.setName("B.fkD");
		this.fkBD.setClassFrom(this.classB);
		this.fkBD.getKeyAttributesFrom().add(this.attributeBfkD);
		this.attributeBfkD.setForeignKeyReference(this.fkBD);
		this.fkBD.setClassTo(this.classD);
		this.fkBD.getKeyAttributesTo().add(this.attributeDpkD);
		this.classB.getForeignKeys().add(this.fkBD);
		this.classD.getIncomingForeignKeys().add(this.fkBD);

		// set up foreign key: C -> fkA -> A
		this.fkCA = ForeignKey.newInstance();
		this.fkCA.setName("C.fkA");
		this.fkCA.setClassFrom(this.classC);
		this.fkCA.getKeyAttributesFrom().add(this.attributeCfkA);
		this.attributeCfkA.setForeignKeyReference(this.fkCA);
		this.fkCA.setClassTo(this.classA);
		this.fkCA.getKeyAttributesTo().add(this.attributeApkA);
		this.classC.getForeignKeys().add(this.fkCA);
		this.classA.getIncomingForeignKeys().add(this.fkCA);
		
		// set up foreign key: C -> fkD -> D
		this.fkCD = ForeignKey.newInstance();
		this.fkCD.setName("C.fkD");
		this.fkCD.setClassFrom(this.classC);
		this.fkCD.getKeyAttributesFrom().add(this.attributeCfkD);
		this.attributeCfkD.setForeignKeyReference(this.fkCD);
		this.fkCD.setClassTo(this.classD);
		this.fkCD.getKeyAttributesTo().add(this.attributeDpkD);
		this.classC.getForeignKeys().add(this.fkCD);
		this.classD.getIncomingForeignKeys().add(this.fkCD);
		
		/*
		 * Class dependency:
		 * 
		 * A.c<-----A.b
		 * \        /
		 *  ->A.a<-                              B.a
		 *      \                        B.FKC__/  |
		 *       \                       /         |
		 *       C.FKA->C.a<-------------         / 
		 *               ^---C.FKD               /
		 *                     \-D.a<--B.FKD-----
		 */
		IDependencyChain depChain = null;

		// from A.b to A.c
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(null);
		depChain.setDependencyFrom(this.attributeAb.getPRMDependency());
		this.attributeAb.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeAc.getPRMDependency());
		this.attributeAc.getPRMDependency().getIncomingDependencyChains().add(depChain);
		
		// from A.b to A.a
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(null);
		depChain.setDependencyFrom(this.attributeAb.getPRMDependency());
		this.attributeAb.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeAa.getPRMDependency());
		this.attributeAa.getPRMDependency().getIncomingDependencyChains().add(depChain);
		
		// from A.c to A.a
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(null);
		depChain.setDependencyFrom(this.attributeAc.getPRMDependency());
		this.attributeAc.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeAa.getPRMDependency());
		this.attributeAa.getPRMDependency().getIncomingDependencyChains().add(depChain);
		
		// from A.a to C.a throuth C.FKA
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(AggregateFunctionMode.newInstance(depChain));
		depChain.setDependencyFrom(this.attributeAa.getPRMDependency());
		this.attributeAa.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeCa.getPRMDependency());
		this.attributeCa.getPRMDependency().getIncomingDependencyChains().add(depChain);
		depChain.getForeignKeyChain().add(this.fkCA);
		
		// from B.a to C.a throuth B.FKC
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(AggregateFunctionMode.newInstance(depChain));
		depChain.setDependencyFrom(this.attributeBa.getPRMDependency());
		this.attributeBa.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeCa.getPRMDependency());
		this.attributeCa.getPRMDependency().getIncomingDependencyChains().add(depChain);
		depChain.getForeignKeyChain().add(this.fkBC);
		
		// from B.a to D.a throuth B.FKD
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(AggregateFunctionMode.newInstance(depChain));
		depChain.setDependencyFrom(this.attributeBa.getPRMDependency());
		this.attributeBa.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeDa.getPRMDependency());
		this.attributeDa.getPRMDependency().getIncomingDependencyChains().add(depChain);
		depChain.getForeignKeyChain().add(this.fkBD);
		
		// from D.a to C.a throuth C.FKD
		depChain = DependencyChain.newInstance();
		depChain.setAggregateFunction(AggregateFunctionMode.newInstance(depChain));
		depChain.setDependencyFrom(this.attributeDa.getPRMDependency());
		this.attributeDa.getPRMDependency().getDependencyChains().add(depChain);
		depChain.setDependencyTo(this.attributeCa.getPRMDependency());
		this.attributeCa.getPRMDependency().getIncomingDependencyChains().add(depChain);
		depChain.getForeignKeyChain().add(this.fkCD);
		
		
		// creating objects: A1, B1, B2, C1, C2, D1
		this.a1 = PRMObject.newInstance(this.classA);
		this.b1 = PRMObject.newInstance(this.classB);
		this.b2 = PRMObject.newInstance(this.classB);
		this.c1 = PRMObject.newInstance(this.classC);
		this.c2 = PRMObject.newInstance(this.classC);
		this.d1 = PRMObject.newInstance(this.classD);
		
		// filling pk of objects
		this.a1.getAttributeValueMap().get(this.attributeApkA).setValue(this.pkValueA1);
		this.b1.getAttributeValueMap().get(this.attributeBpkB).setValue(this.pkValueB1);
		this.b2.getAttributeValueMap().get(this.attributeBpkB).setValue(this.pkValueB2);
		this.c1.getAttributeValueMap().get(this.attributeCpkC).setValue(this.pkValueC1);
		this.c2.getAttributeValueMap().get(this.attributeCpkC).setValue(this.pkValueC2);
		this.d1.getAttributeValueMap().get(this.attributeDpkD).setValue(this.pkValueD1);
		
		/*
		 * Let's fill the FK values to build the following
		 * PRM skeleton:
		 * 
		 * A1.c<-----A1.b
		 * \        /
		 *  ->A1.a<-                B1.a         B2.a
		 *    | \           B1.FKC__/    B2.FKC__/  |
		 *    |  \             /          /         |
		 *    | C1.FKA->C1.a<-------------         / 
		 *    |         ^---C1.FKD                /
		 *   C2.FKA              \-D1.a<--B2.FKD--
		 *     \->C2.a   
		 */
		
		// link C1 to A1 using C1.FKA
		this.c1.getAttributeValueMap().get(this.attributeCfkA).setValue(this.pkValueA1);
		// link C1 to D1 using C1.FKD
		this.c1.getAttributeValueMap().get(this.attributeCfkD).setValue(this.pkValueD1);
		// link C2 to A1 using C2.FKA
		this.c2.getAttributeValueMap().get(this.attributeCfkA).setValue(this.pkValueA1);
		// link B1 to C1 using B1.FKC
		this.b1.getAttributeValueMap().get(this.attributeBfkC).setValue(this.pkValueC1);
		// link B2 to C1 using B2.FKC
		this.b2.getAttributeValueMap().get(this.attributeBfkC).setValue(this.pkValueC1);
		// link B2 to D1 using B2.FKD
		this.b2.getAttributeValueMap().get(this.attributeBfkD).setValue(this.pkValueD1);
		
		// Done.
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		System.gc();
	}

	/**
	 * Test method for {@link unbbayes.prs.prm.DependencyChainSolver#solveChildren(unbbayes.prs.prm.IAttributeValue)}.
	 */
	public final void testSolveChildren() {
		
		List<IAttributeValue> ret = null;
		/*
		 *  Important objects (samples): 
		 *      - No children: C1.a
		 *  	- 2 children from same class: A1.b (A1.c, A1.a)
		 *      - 2 children from direct FK: B2.a (C1.a, D1.a)
		 *      - 2 children form inverse FK: A1.a (C1.a, C2.a)
		 */
		
		// test null
		try {
			ret = this.chainSolverToTest.solveChildren(null);
			assertNotNull("It must return empty list", ret);
			assertTrue("It must return empty list", ret.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("It should return empty list if argument is null, without throwing exception");
		}
		
		
		// test No children: C1.a
		ret = this.chainSolverToTest.solveChildren(this.c1.getAttributeValueMap().get(this.attributeCa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 0 child", 0, ret.size());
		
		// test 2 children from same class: A1.b (A1.c, A1.a)
		ret = this.chainSolverToTest.solveChildren(this.a1.getAttributeValueMap().get(this.attributeAb));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 2 children", 2, ret.size());
		assertTrue("It must contain the expected child.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAc)));
		assertTrue("It must contain the expected child.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAa)));
		
		// test 2 children from direct FK: B2.a (C1.a, D1.a)
		ret = this.chainSolverToTest.solveChildren(this.b2.getAttributeValueMap().get(this.attributeBa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 2 children", 2, ret.size());
		assertTrue("It must contain the expected child.", ret.contains(this.c1.getAttributeValueMap().get(this.attributeCa)));
		assertTrue("It must contain the expected child.", ret.contains(this.d1.getAttributeValueMap().get(this.attributeDa)));
		
		// test 2 children form inverse FK: A1.a (C1.a, C2.a)
		ret = this.chainSolverToTest.solveChildren(this.a1.getAttributeValueMap().get(this.attributeAa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 2 children", 2, ret.size());
		assertTrue("It must contain the expected child.", ret.contains(this.c1.getAttributeValueMap().get(this.attributeCa)));
		assertTrue("It must contain the expected child.", ret.contains(this.c2.getAttributeValueMap().get(this.attributeCa)));
		
		// test removal of the link "C1.FKA"  (remove the object's link, not from the class itself)
		this.c1.getAttributeValueMap().get(this.attributeCfkA).setValue(null);
		ret = this.chainSolverToTest.solveChildren(this.a1.getAttributeValueMap().get(this.attributeAa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 1 children", 1, ret.size());
		assertTrue("It must contain the expected child.", ret.contains(this.c2.getAttributeValueMap().get(this.attributeCa)));
		
		// test the impact of the removal of the PK of C1 on B1
		// before removal
		ret = this.chainSolverToTest.solveChildren(this.b1.getAttributeValueMap().get(this.attributeBa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 1 children", 1, ret.size());
		assertTrue("It must contain the expected child.", ret.contains(this.c1.getAttributeValueMap().get(this.attributeCa)));
		// after removal
		this.c1.getAttributeValueMap().get(this.attributeCpkC).setValue(null);
		ret = this.chainSolverToTest.solveChildren(this.b1.getAttributeValueMap().get(this.attributeBa));
		assertNotNull("It must return a non-null value", ret);
		assertTrue("It must contain no children", ret.isEmpty());
		
	}

	/**
	 * Test method for {@link unbbayes.prs.prm.DependencyChainSolver#solveParents(unbbayes.prs.prm.IAttributeValue)}.
	 */
	public final void testSolveParents() {
		
		List<IAttributeValue> ret = null;
		
		/*
		 *  Important objects (samples): 
		 *      - No parents: B2.a
		 *  	- 2 parents from same class: A1.a (A1.c, A1.b)
		 *      - 2 parents from direct FK: C1.a (A1.a, D1.a)
		 *      - 2 parents from inverse FK: C1.a (B1.a, B2.a)
		 */
		
		// test null
		try {
			ret = this.chainSolverToTest.solveParents(null);
			assertNotNull("It must return empty list", ret);
			assertTrue("It must return empty list", ret.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("It should return empty list if argument is null, without throwing exception");
		}
		
		// test No parents: B2.a
		ret = this.chainSolverToTest.solveParents(this.b2.getAttributeValueMap().get(this.attributeBa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 0 child", 0, ret.size());
		
		// test 2 parents from same class: A1.a (A1.c, A1.b)
		ret = this.chainSolverToTest.solveParents(this.a1.getAttributeValueMap().get(this.attributeAa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 2 parents", 2, ret.size());
		assertTrue("It must contain the expected parent.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAc)));
		assertTrue("It must contain the expected parent.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAb)));
		assertEquals("The parent order is important.", this.a1.getAttributeValueMap().get(this.attributeAb), ret.get(0));
		assertEquals("The parent order is important.", this.a1.getAttributeValueMap().get(this.attributeAc), ret.get(1));
		
		// test 4 parents from direct and inverse FK: C1.a (A1.a, B1.a, B2.a, D1.a)
		ret = this.chainSolverToTest.solveParents(this.c1.getAttributeValueMap().get(this.attributeCa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must contain 2 parents each (total of 4).", 4, ret.size());
		assertTrue("It must contain the expected parent.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.d1.getAttributeValueMap().get(this.attributeDa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.b1.getAttributeValueMap().get(this.attributeBa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.b2.getAttributeValueMap().get(this.attributeBa)));
		assertEquals("The parent order is important.", this.a1.getAttributeValueMap().get(this.attributeAa), ret.get(0));
		assertEquals("The parent order is important.", this.b1.getAttributeValueMap().get(this.attributeBa), ret.get(1));
		assertEquals("The parent order is important.", this.b2.getAttributeValueMap().get(this.attributeBa), ret.get(2));
		assertEquals("The parent order is important.", this.d1.getAttributeValueMap().get(this.attributeDa), ret.get(3));

		// test the removal of object link B1.FKC
		this.b1.getAttributeValueMap().get(this.attributeBfkC).setValue(null);
		ret = this.chainSolverToTest.solveParents(this.c1.getAttributeValueMap().get(this.attributeCa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("B1.a must not be present now", 3, ret.size());
		assertTrue("It must contain the expected parent.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.d1.getAttributeValueMap().get(this.attributeDa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.b2.getAttributeValueMap().get(this.attributeBa)));
		
		// test the removal of B2's PK (but not removing the PK)
		this.b2.getAttributeValueMap().get(this.attributeBpkB).setValue(null);
		ret = this.chainSolverToTest.solveParents(this.c1.getAttributeValueMap().get(this.attributeCa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("B2.a must still be present (because FK was not removed)", 3, ret.size());
		assertTrue("It must contain the expected parent.", ret.contains(this.a1.getAttributeValueMap().get(this.attributeAa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.d1.getAttributeValueMap().get(this.attributeDa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.b2.getAttributeValueMap().get(this.attributeBa)));
		
		// test the removal of A1's PK
		this.a1.getAttributeValueMap().get(this.attributeApkA).setValue(null);
		ret = this.chainSolverToTest.solveParents(this.c1.getAttributeValueMap().get(this.attributeCa));
		assertNotNull("It must return a non-null value", ret);
		assertEquals("It must not contain A1 now", 2, ret.size());
		assertTrue("It must contain the expected parent.", ret.contains(this.d1.getAttributeValueMap().get(this.attributeDa)));
		assertTrue("It must contain the expected parent.", ret.contains(this.b2.getAttributeValueMap().get(this.attributeBa)));
		
	}
	
}
