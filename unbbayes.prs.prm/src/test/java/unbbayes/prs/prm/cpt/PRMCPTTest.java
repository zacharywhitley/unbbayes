/**
 * 
 */
package unbbayes.prs.prm.cpt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.prm.AttributeDescriptor;
import unbbayes.prs.prm.DependencyChain;
import unbbayes.prs.prm.ForeignKey;
import unbbayes.prs.prm.IDependencyChain;
import unbbayes.prs.prm.IPRMDependency;
import unbbayes.prs.prm.IPRMObject;
import unbbayes.prs.prm.PRM;
import unbbayes.prs.prm.PRMClass;
import unbbayes.prs.prm.PRMObject;
import junit.framework.TestCase;

/**
 * Test case for {@link PRMCPT}
 * @author Shou Matsumoto
 *
 */
public class PRMCPTTest extends TestCase {
	

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
	public PRMCPTTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
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
		 * 
		 * Note: we assume all inverse FK dependency chain using AggregateFunctionMode
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
		
		// Fill only CPTs of important nodes (test cases):
		
		/*
		 * CASE1: NO PARENT (A1.B)
		 * 				true = .1, 
		 * 				false = .9
		 * 
		 */
		this.attributeAb.getPRMDependency().getCPT().getTableValues().set(0, .1f);
		this.attributeAb.getPRMDependency().getCPT().getTableValues().set(1, .9f);
		
		/*
		 * CASE2: PARENTS IN SAME CLASS (A1.A {A1.C, A1.B})
		 * 				_____________________________________
		 * 				|A.c  |     true      |    false     |
		 * 				|A.b  |  true | false | true | false |
		 * 				|====================================|
		 * 				|true |  .9      .8      .3     .4   |
		 * 				|false|  .1      .2      .7     .6   |
		 * 				-------------------------------------
		 */
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(0, .9f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(1, .1f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(2, .8f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(3, .2f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(4, .3f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(5, .7f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(6, .4f);
		this.attributeAa.getPRMDependency().getCPT().getTableValues().set(7, .6f);
		
		/*
		 * CASE3: COMPLEX PARENT CONFIG (C1.A {A1.A, B1.A, B2.A, D1.A}) 
		 * 			{A1.A, D1.a} are referenced by direct FK and {B1.a, B2.a} are referenced by inverse FK (i.e. uses aggregation function)
		 * 				________________________________________________________________________
		 * 				|D.a      |             true             |             false            |
		 * 				|Mode(B.a)|     true      |    false     |     true      |    false     |
		 * 				|A.a      |  true | false | true | false |  true | false | true | false |
		 * 				|=======================================================================|
		 * 				|true     |   1       1       1      1       .8      .7     .3     .5   |
		 * 				|false    |   0       0       0      0       .2      .3     .7     .5   |
		 * 				------------------------------------------------------------------------
		 */
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(0, 1.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(1, 0.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(2, 1.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(3, 0.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(4, 1.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(5, 0.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(6, 1.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(7, 0.0f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(8,  .8f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(9,  .2f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(10, .7f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(11, .3f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(12, .3f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(13, .7f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(14, .5f);
		this.attributeCa.getPRMDependency().getCPT().getTableValues().set(15, .5f);
		
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
	 * Test method for {@link unbbayes.prs.prm.cpt.PRMCPT#getTableValuesByColumn(java.util.Map)}.
	 */
	public final void testGetTableValuesByColumn() {
		
		List<Float> ret = null;
		
		/*
		 * CASE1: NO PARENT (A.b)
		 * 				true = .1, 
		 * 				false = .9
		 * 
		 */

		// check basic consistency
		assertTrue("We are testing PRMCPT", this.attributeAb.getPRMDependency().getCPT() instanceof PRMCPT);
		
		// check null argument and empty argument
		ret = this.attributeAb.getPRMDependency().getCPT().getTableValuesByColumn(null);
		assertNotNull("It must not return null", ret);
		assertEquals("For null or empty argument, it must return the first column.", this.attributeAb.getPRMDependency().getCPT().getTableValues(), ret);
		ret = this.attributeAb.getPRMDependency().getCPT().getTableValuesByColumn(new HashMap<IPRMDependency, String>());
		assertNotNull("It must not return null", ret);
		assertEquals("For null or empty argument, it must return the first column.", this.attributeAb.getPRMDependency().getCPT().getTableValues(), ret);
		
		// check values
		assertEquals("This column contains 2 rows", 2, ret.size());
		assertEquals(.1f, ret.get(0));
		assertEquals(.9f, ret.get(1));
		
		/*
		 * CASE2: COMPLEX PARENT CONFIG (C.A {A.A, B.A, D.A}) 
		 * 			{A1.A, D1.a} are referenced by direct FK and {B1.a, B2.a} are referenced by inverse FK (i.e. uses aggregation function)
		 * 				________________________________________________________________________
		 * 				|D.a      |             true             |             false            |
		 * 				|Mode(B.a)|     true      |    false     |     true      |    false     |
		 * 				|A.a      |  true | false | true | false |  true | false | true | false |
		 * 				|=======================================================================|
		 * 				|true     |   1       1       1      1       .8      .7     .3     .5   |
		 * 				|false    |   0       0       0      0       .2      .3     .7     .5   |
		 * 				------------------------------------------------------------------------
		 */
		
		// check basic consistency
		assertTrue("We are testing PRMCPT", this.attributeCa.getPRMDependency().getCPT() instanceof PRMCPT);
		
		// check null argument and empty argument
		List<Float> firstColumn = this.attributeCa.getPRMDependency().getCPT().getTableValuesByColumn(new HashMap<IPRMDependency, String>());
		ret = this.attributeCa.getPRMDependency().getCPT().getTableValuesByColumn(null);
		assertNotNull("It must not return null", ret);
		assertNotNull("It must not return null", firstColumn);
		assertEquals("Null and empty arguments must return the same column (1st)", ret, firstColumn);
		assertEquals("This column contains 2 rows", 2, ret.size());
		assertEquals(1.0f, ret.get(0));
		assertEquals(0.0f, ret.get(1));
		
		// check 1st column (D.a == true, B.a == true, A.a == true)
		Map<IPRMDependency, String> parentStateMap = new HashMap<IPRMDependency, String>();
		parentStateMap.put(this.attributeDa.getPRMDependency(), "true");
		parentStateMap.put(this.attributeBa.getPRMDependency(), "true");
		parentStateMap.put(this.attributeAa.getPRMDependency(), "true");
		ret = this.attributeCa.getPRMDependency().getCPT().getTableValuesByColumn(parentStateMap);
		assertNotNull("It must not return null", ret);
		assertEquals("This column contains 2 rows", 2, ret.size());
		assertEquals("This must be the first column", firstColumn, ret);
		assertEquals(1.0f, ret.get(0));
		assertEquals(0.0f, ret.get(1));
		
		// check 5th column (D.a == false, B.a == true, A.a == true)
		parentStateMap = new HashMap<IPRMDependency, String>();
		parentStateMap.put(this.attributeAa.getPRMDependency(), "true");
		parentStateMap.put(this.attributeBa.getPRMDependency(), "true");
		parentStateMap.put(this.attributeDa.getPRMDependency(), "false");
		ret = this.attributeCa.getPRMDependency().getCPT().getTableValuesByColumn(parentStateMap);
		assertNotNull("It must not return null", ret);
		assertEquals("This column contains 2 rows", 2, ret.size());
		assertEquals(.8f, ret.get(0));
		assertEquals(.2f, ret.get(1));
		

		// check last column (D.a == false, B.a == false, A.a == false)
		parentStateMap = new HashMap<IPRMDependency, String>();
		parentStateMap.put(this.attributeAa.getPRMDependency(), "false");
		parentStateMap.put(this.attributeBa.getPRMDependency(), "false");
		parentStateMap.put(this.attributeDa.getPRMDependency(), "false");
		ret = this.attributeCa.getPRMDependency().getCPT().getTableValuesByColumn(parentStateMap);
		assertNotNull("It must not return null", ret);
		assertEquals("This column contains 2 rows", 2, ret.size());
		assertEquals(.5f, ret.get(0));
		assertEquals(.5f, ret.get(1));
	}

}
