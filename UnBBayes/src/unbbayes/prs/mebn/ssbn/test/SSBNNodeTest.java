/**
 * 
 */
package unbbayes.prs.mebn.ssbn.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import junit.framework.TestCase;

/**
 * @author shou
 *
 */
public class SSBNNodeTest extends TestCase {

	private MultiEntityBayesianNetwork mebn = null;
	private  DomainMFrag mfrag = null;
	private DomainResidentNode resident = null;
	
	private SSBNNode ssbnnode = null;
	private SSBNNode findingssbnnode = null;
	/**
	 * @param arg0
	 */
	public SSBNNodeTest(String arg0) {
		super(arg0);
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		UbfIO ubf = UbfIO.getInstance();
		try {
			mebn = ubf.loadMebn(new File("examples/mebn/SSBNNodeTest.ubf"));
		} catch (Exception e) {
			fail(e.getMessage());
			return;
		}
		
		if (this.mebn == null) {
			fail("Unable to load file");
			return;
		}
		this.resident = mebn.getDomainResidentNode("HarmPotential");
		
		if (this.resident == null) {
			fail("Unable to retreve resident node");
			return;
		}
		
		this.ssbnnode = SSBNNode.getInstance(this.resident,new ProbabilisticNode());
		this.ssbnnode.addArgument(resident.getOrdinaryVariableList().get(0),"ST4");
		this.ssbnnode.addArgument(resident.getOrdinaryVariableList().get(1),"T0");
		
		this.findingssbnnode = SSBNNode.getInstance(this.resident);
		this.findingssbnnode.addArgument(resident.getOrdinaryVariableList().get(0),"ST4");
		this.findingssbnnode.addArgument(resident.getOrdinaryVariableList().get(1),"T0");
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getInstance(unbbayes.prs.mebn.DomainResidentNode, unbbayes.prs.bn.ProbabilisticNode)}.
	 */
	public void testGetInstanceDomainResidentNodeProbabilisticNode() {
		ProbabilisticNode pnode = new ProbabilisticNode();
		SSBNNode node = SSBNNode.getInstance(this.resident,pnode);
		assertNotNull(node);
		assertEquals(node.getResident(), this.resident);
		assertEquals(node.getProbNode(), pnode);
		assertEquals(3,node.getActualValues().size()); // contains true, false and absurd
		assertEquals(node.getParents().size(),0);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getInstance(unbbayes.prs.mebn.DomainResidentNode)}.
	 */
	public void testGetInstanceDomainResidentNode() {
		SSBNNode node = SSBNNode.getInstance(resident);
		assertNotNull(node);
		assertEquals(node.getResident(), resident);
		assertNull(node.getProbNode());
		assertEquals(1,node.getActualValues().size());	// a SSBNNode without probNode should be a finding
		assertEquals(node.getParents().size(),0);
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getOVs()}.
	 */
	public void testGetOVs() {
		for (OrdinaryVariable ov : this.resident.getOrdinaryVariableList()) {
			try { 
				this.ssbnnode.addArgument(ov, ov.getName());
			} catch (SSBNNodeGeneralException e) {
				fail(e.getMessage());
			}
		}
		assertTrue(this.resident.getOrdinaryVariableList().containsAll(this.ssbnnode.getOVs()));
		assertTrue(this.ssbnnode.getOVs().containsAll(this.resident.getOrdinaryVariableList()));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasOV(unbbayes.prs.mebn.OrdinaryVariable)}.
	 */
	public void testHasOVOrdinaryVariable() {
		assertTrue(this.ssbnnode.hasOV(this.resident.getOrdinaryVariableList().get(0)));
		assertTrue(this.ssbnnode.hasOV(this.resident.getOrdinaryVariableList().get(1)));		
		assertTrue(!this.ssbnnode.hasOV(null));
		assertTrue(!this.ssbnnode.hasOV(this.mebn.getDomainResidentNode("ZoneEShips").getOrdinaryVariableList().get(0)));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasOV(java.lang.String, boolean)}.
	 */
	public void testHasOVStringBoolean() {
		assertTrue(!this.ssbnnode.hasOV(null,true));
		assertTrue(!this.ssbnnode.hasOV(null,false));
		
		assertTrue(this.ssbnnode.hasOV("st", true));
		assertTrue(this.ssbnnode.hasOV("t", true));
		assertTrue(this.ssbnnode.hasOV("ST4", false));
		assertTrue(this.ssbnnode.hasOV("T0", false));
		
		assertTrue(!this.ssbnnode.hasOV("z", true));
		assertTrue(!this.ssbnnode.hasOV("s", true));
		assertTrue(!this.ssbnnode.hasOV("ST2007", false));
		assertTrue(!this.ssbnnode.hasOV("T2000", false));		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasAllOVs(java.util.Collection)}.
	 */
	public void testHasAllOVsCollectionOfOrdinaryVariable() {
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("Terminator");
		} catch(TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}

		assertTrue(!this.ssbnnode.hasAllOVs((Collection<OrdinaryVariable>)null));
		
		List<OrdinaryVariable> tempList = new ArrayList<OrdinaryVariable>(this.resident.getOrdinaryVariableList());
		assertTrue(this.ssbnnode.hasAllOVs(tempList));
		
		tempList.add(new OrdinaryVariable("terminator",tcontainer.getType("Terminator"),this.mfrag));
		assertTrue(!this.ssbnnode.hasAllOVs(tempList));
		tempList.remove(2);
		
		tempList.remove(0);	// there should be only the ov "t"
		assertTrue(this.ssbnnode.hasAllOVs(tempList));

		
		tempList = new ArrayList<OrdinaryVariable>();
		assertTrue(!this.ssbnnode.hasAllOVs(tempList));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasAllOVs(unbbayes.prs.mebn.OrdinaryVariable[])}.
	 */
	public void testHasAllOVsOrdinaryVariableArray() {
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("Terminator");
		} catch(TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}

		assertTrue(!this.ssbnnode.hasAllOVs((OrdinaryVariable)null));
		assertTrue(!this.ssbnnode.hasAllOVs((OrdinaryVariable)null,null));
		assertTrue(!this.ssbnnode.hasAllOVs((OrdinaryVariable[])null));
		
		List<OrdinaryVariable> tempList = new ArrayList<OrdinaryVariable>(this.resident.getOrdinaryVariableList());
		OrdinaryVariable[] ovs = {
			tempList.get(0), tempList.get(1) // {"st" , "t"}
		};
		
		assertTrue(this.ssbnnode.hasAllOVs(ovs));
		
		tempList.add(new OrdinaryVariable("terminator",tcontainer.getType("Terminator"),this.mfrag));
		OrdinaryVariable[] ovs2 = {
				tempList.get(0), tempList.get(1), // {"st" , "t"}
				new OrdinaryVariable("terminator",tcontainer.getType("Terminator"),this.mfrag)
		};
		
		assertTrue(!this.ssbnnode.hasAllOVs(ovs2));
		
		OrdinaryVariable[] ovs3 = {
				tempList.get(1), // {"st" }
		};
		assertTrue(this.ssbnnode.hasAllOVs(ovs3));

		OrdinaryVariable[] ovs4 = {}; //empty
		assertTrue(!this.ssbnnode.hasAllOVs(ovs4));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#hasAllOVs(boolean, java.lang.String[])}.
	 */
	public void testHasAllOVsBooleanStringArray() {
		assertTrue(!this.ssbnnode.hasAllOVs(true, null));
		assertTrue(!this.ssbnnode.hasAllOVs(false, null));
		
		assertTrue(this.ssbnnode.hasAllOVs(true, "st", "t"));
		assertTrue(this.ssbnnode.hasAllOVs(true, "t"));
		assertTrue(this.ssbnnode.hasAllOVs(true, "st"));
		
		assertTrue(this.ssbnnode.hasAllOVs(false, "ST4", "T0"));
		assertTrue(this.ssbnnode.hasAllOVs(false, "T0"));
		assertTrue(this.ssbnnode.hasAllOVs(false, "ST4"));
		
		assertTrue(!this.ssbnnode.hasAllOVs(true, "z", "s"));
		assertTrue(!this.ssbnnode.hasAllOVs(true, "x"));
		
		assertTrue(!this.ssbnnode.hasAllOVs(false, "STSTSTSTST", "T2000"));
		assertTrue(!this.ssbnnode.hasAllOVs(false, "T2000"));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#addArgument(unbbayes.prs.mebn.OrdinaryVariable, java.lang.String)}.
	 */
	public void testAddArgumentOrdinaryVariableString() {
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("MobileSuit");
		} catch(TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}
		OrdinaryVariable ov = new OrdinaryVariable("gundam",tcontainer.getType("MobileSuit"),this.mfrag);
		
		assertTrue(!this.ssbnnode.hasOV(ov));
		try{
			this.ssbnnode.addArgument(ov, "GUNDAM");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}		
		assertTrue(this.ssbnnode.hasOV(ov));
		
		try{
			this.ssbnnode.addArgument(null, null);
			fail("Null arguments are not allowed");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
		try{
			this.ssbnnode.addArgument(null, "GUNDAM");
			fail("Null arguments are not allowed");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
		try{
			this.ssbnnode.addArgument(ov, null);
			fail("Null arguments are not allowed");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#addArgument(unbbayes.prs.mebn.OrdinaryVariable, java.lang.String, int)}.
	 */
	public void testAddArgumentOrdinaryVariableStringInt() {
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("MobileSuit");
		} catch(TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}
		OrdinaryVariable ov = new OrdinaryVariable("gundam",tcontainer.getType("MobileSuit"),this.mfrag);
		
		assertTrue(!this.ssbnnode.hasOV(ov));
		try{
			this.ssbnnode.addArgument(ov, "GUNDAM",0);
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}		
		assertTrue(this.ssbnnode.hasOV(ov));
		assertEquals(this.ssbnnode.getArguments().iterator().next().getOv() , ov);
		
		try{
			this.ssbnnode.addArgument(null, null , 0);
			fail("Null arguments are not allowed");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
		try{
			this.ssbnnode.addArgument(null, "GUNDAM" , 500);
			fail("Null arguments are not allowed");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
		try{
			this.ssbnnode.addArgument(ov, null , 0x04);
			fail("Null arguments are not allowed");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setNodeAsFinding(unbbayes.prs.mebn.entity.Entity)}.
	 */
	public void testSetNodeAsFinding() {
		this.ssbnnode.setNodeAsFinding(this.mebn.getCategoricalStatesEntityContainer().createCategoricalEntity("CharZaku"));
		assertEquals(1, this.ssbnnode.getActualValues().size());
		
		try{
			assertEquals(
				this.mebn.getCategoricalStatesEntityContainer().getCategoricalState("CharZaku"),
				this.ssbnnode.getActualValues().iterator().next()
				);
		} catch (CategoricalStateDoesNotExistException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#fillProbabilisticTable()}.
	 */
	public void testFillProbabilisticTable() {
		this.ssbnnode.fillProbabilisticTable();
		//assertNotNull(this.ssbnnode.getProbNode().getPotentialTable());
		// TODO more detailed test
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#addParent(unbbayes.prs.mebn.ssbn.SSBNNode)}.
	 */
	public void testAddParent() {
		
		try {
			this.ssbnnode.addParent(null, true);
			fail ("It should throw null pointer exception");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
		
		try {
			this.ssbnnode.addParent(null, false);
			fail ("It should throw null pointer exception");
		} catch (SSBNNodeGeneralException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// OK
		}
		
		SSBNNode parent = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent, true);
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent));
		
		try {
			this.findingssbnnode.addParent(parent, true);
			fail("A finding should not have a parent");
		} catch(NullPointerException ne) {
			//OK
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		parent = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent, true);
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent));
		
		parent = SSBNNode.getInstance(this.mebn.getDomainResidentNode("OpSpec"), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent, true);
			fail("Consistency failure expected");
		} catch(SSBNNodeGeneralException e) {
			// OK
		}
		assertTrue(!this.ssbnnode.getParents().contains(parent));
		
		// should pass consistency check because its off
		parent = SSBNNode.getInstance(this.mebn.getDomainResidentNode("OpSpec"), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent, false);			
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent));
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#removeParent(unbbayes.prs.mebn.ssbn.SSBNNode)}.
	 */
	public void testRemoveParent() {
		
		this.ssbnnode.removeParent(null);
		
		SSBNNode parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent1));
		
		SSBNNode parent2 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent2, true);
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent2));
		
		this.ssbnnode.removeParent(parent1);
		assertTrue(!this.ssbnnode.getParents().contains(parent1));
		this.ssbnnode.removeParent(parent2);
		assertTrue(!this.ssbnnode.getParents().contains(parent2));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#removeParentByName(java.lang.String)}.
	 */
	public void testRemoveParentByName() {
		this.ssbnnode.removeParentByName(null);
		
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("MobileSuit");
		} catch (Exception e) {
			fail(e.getMessage());
		}		
		OrdinaryVariable ms = new OrdinaryVariable("ms",tcontainer.getType("MobileSuit"),this.mfrag);
		SSBNNode parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
			parent1.addArgument(ms, "GM");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent1));
		
		SSBNNode parent2 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			parent2.addArgument(ms, "GM");
			this.ssbnnode.addParent(parent2, true);
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent2));
		
		assertEquals(this.ssbnnode.getParents().size(),2);
		
		this.ssbnnode.removeParentByName(parent1.getName());
		assertEquals(this.ssbnnode.getParents().size(),1);
		this.ssbnnode.removeParentByName(parent2.getName());
		assertEquals(this.ssbnnode.getParents().size(),0);
		
		parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		assertTrue(this.ssbnnode.getParents().contains(parent1));
		this.ssbnnode.removeParentByName(parent1.getName());
		assertTrue(!this.ssbnnode.getParents().contains(parent1));
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentSetByStrongOV(java.lang.String[])}.
	 */
	public void testGetParentSetByStrongOVStringArray() {
		
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("Starship");
			tcontainer.createType("Timestep");
			tcontainer.createType("Zone");
		} catch (Exception e) {
			fail(e.getMessage());
		}		
		OrdinaryVariable st = new OrdinaryVariable("st",tcontainer.getType("Starship"),this.mfrag);
		OrdinaryVariable t = new OrdinaryVariable("t",tcontainer.getType("Timestep"),this.mfrag);
		OrdinaryVariable z = new OrdinaryVariable("z",tcontainer.getType("Zone"),this.mfrag);
		
		
		SSBNNode parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
			parent1.addArgument(st, "ST1");
			parent1.addArgument(t, "T1");
			parent1.addArgument(z, "Z1");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent2 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent2, true);
			parent2.addArgument(st, "ST2");
			parent2.addArgument(t, "T2");
			parent2.addArgument(z, "Z2");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		
		SSBNNode parent3 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent3, true);
			parent3.addArgument(st, "ST3");
			parent3.addArgument(t, "T3");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent4 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent4, true);
			parent4.addArgument(st, "ST4");
			parent4.addArgument(t, "T4");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent5 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent5, true);
			parent5.addArgument(st, "ST5");
			parent5.addArgument(t, "T5");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent6 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent6, true);
			parent6.addArgument(st, "ST6");
			parent6.addArgument(t, "T6");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		SSBNNode parent7 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent7, true);
			parent7.addArgument(st, "ST7");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent8 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent8, true);
			parent8.addArgument(st, "ST8");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		assertEquals(0,this.ssbnnode.getParentSetByStrongOV(true,"").size());
		assertEquals(0,this.ssbnnode.getParentSetByStrongOV(true,(String[])null).size());
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st","z", "t").contains(parent1));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st","z", "t").contains(parent2));
		assertEquals(2 , this.ssbnnode.getParentSetByStrongOV(true,"st","z", "t").size());
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st","t").contains(parent3));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st","t").contains(parent4));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st","t").contains(parent5));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st","t").contains(parent6));
		assertEquals(4 , this.ssbnnode.getParentSetByStrongOV(true,"st","t").size());
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st").contains(parent7));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,"st").contains(parent8));
		assertEquals(2 , this.ssbnnode.getParentSetByStrongOV(true,"st").size());
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentSetByStrongOV(java.util.Collection)}.
	 */
	public void testGetParentSetByStrongOVCollectionOfOrdinaryVariable() {
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("Starship");
			tcontainer.createType("Timestep");
			tcontainer.createType("Zone");
		} catch (Exception e) {
			fail(e.getMessage());
		}		
		OrdinaryVariable st = new OrdinaryVariable("st",tcontainer.getType("Starship"),this.mfrag);
		OrdinaryVariable t = new OrdinaryVariable("t",tcontainer.getType("Timestep"),this.mfrag);
		OrdinaryVariable z = new OrdinaryVariable("z",tcontainer.getType("Zone"),this.mfrag);
		
		
		SSBNNode parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
			parent1.addArgument(st, "ST1");
			parent1.addArgument(t, "T1");
			parent1.addArgument(z, "Z1");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent2 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent2, true);
			parent2.addArgument(st, "ST2");
			parent2.addArgument(t, "T2");
			parent2.addArgument(z, "Z2");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		
		SSBNNode parent3 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent3, true);
			parent3.addArgument(st, "ST3");
			parent3.addArgument(t, "T3");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent4 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent4, true);
			parent4.addArgument(st, "ST4");
			parent4.addArgument(t, "T4");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent5 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent5, true);
			parent5.addArgument(st, "ST5");
			parent5.addArgument(t, "T5");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent6 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent6, true);
			parent6.addArgument(st, "ST6");
			parent6.addArgument(t, "T6");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		SSBNNode parent7 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent7, true);
			parent7.addArgument(st, "ST7");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent8 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent8, true);
			parent8.addArgument(st, "ST8");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		Collection<OrdinaryVariable> ovs = new ArrayList<OrdinaryVariable>();
		
		assertEquals(0,this.ssbnnode.getParentSetByStrongOV(true,ovs).size());
		assertEquals(0,this.ssbnnode.getParentSetByStrongOV(true,(Collection)null).size());
		
		ovs.add(st);
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent7));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent8));
		assertEquals(2 , this.ssbnnode.getParentSetByStrongOV(true,ovs).size());
		
		ovs.add(t);
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent3));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent4));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent5));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent6));
		assertEquals(4 , this.ssbnnode.getParentSetByStrongOV(true,ovs).size());
		
		ovs.add(z);
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent1));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,ovs).contains(parent2));
		assertEquals(2 , this.ssbnnode.getParentSetByStrongOV(true,ovs).size());
		
		
		
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentSetByStrongOV(unbbayes.prs.mebn.OrdinaryVariable[])}.
	 */
	public void testGetParentSetByStrongOVOrdinaryVariableArray() {

		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("Starship");
			tcontainer.createType("Timestep");
			tcontainer.createType("Zone");
		} catch (Exception e) {
			fail(e.getMessage());
		}		
		OrdinaryVariable st = new OrdinaryVariable("st",tcontainer.getType("Starship"),this.mfrag);
		OrdinaryVariable t = new OrdinaryVariable("t",tcontainer.getType("Timestep"),this.mfrag);
		OrdinaryVariable z = new OrdinaryVariable("z",tcontainer.getType("Zone"),this.mfrag);
		
		
		SSBNNode parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
			parent1.addArgument(st, "ST1");
			parent1.addArgument(t, "T1");
			parent1.addArgument(z, "Z1");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent2 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent2, true);
			parent2.addArgument(st, "ST2");
			parent2.addArgument(t, "T2");
			parent2.addArgument(z, "Z2");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		
		SSBNNode parent3 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent3, true);
			parent3.addArgument(st, "ST3");
			parent3.addArgument(t, "T3");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent4 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent4, true);
			parent4.addArgument(st, "ST4");
			parent4.addArgument(t, "T4");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent5 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent5, true);
			parent5.addArgument(st, "ST5");
			parent5.addArgument(t, "T5");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent6 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent6, true);
			parent6.addArgument(st, "ST6");
			parent6.addArgument(t, "T6");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		SSBNNode parent7 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent7, true);
			parent7.addArgument(st, "ST7");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent8 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent8, true);
			parent8.addArgument(st, "ST8");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		assertEquals(0,this.ssbnnode.getParentSetByStrongOV(true,(OrdinaryVariable)null).size());
		assertEquals(0,this.ssbnnode.getParentSetByStrongOV(true,(OrdinaryVariable[])null).size());
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st,z, t).contains(parent1));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st,z, t).contains(parent2));
		assertEquals(2 , this.ssbnnode.getParentSetByStrongOV(true,st,z, t).size());
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st,t).contains(parent3));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st,t).contains(parent4));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st,t).contains(parent5));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st,t).contains(parent6));
		assertEquals(4 , this.ssbnnode.getParentSetByStrongOV(true,st,t).size());
		
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st).contains(parent7));
		assertTrue(this.ssbnnode.getParentSetByStrongOV(true,st).contains(parent8));
		assertEquals(2 , this.ssbnnode.getParentSetByStrongOV(true,st).size());
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#getParentMapByWeakOV(unbbayes.prs.mebn.OrdinaryVariable[])}.
	 */
	public void testGetParentMapByWeakOV() {
		TypeContainer tcontainer = new TypeContainer();
		try{
			tcontainer.createType("Starship");
			tcontainer.createType("Timestep");
			tcontainer.createType("Zone");
		} catch (Exception e) {
			fail(e.getMessage());
		}		
		OrdinaryVariable st = new OrdinaryVariable("st",tcontainer.getType("Starship"),this.mfrag);
		OrdinaryVariable t = new OrdinaryVariable("t",tcontainer.getType("Timestep"),this.mfrag);
		OrdinaryVariable z = new OrdinaryVariable("z",tcontainer.getType("Zone"),this.mfrag);
		
		
		SSBNNode parent1 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent1, true);
			parent1.addArgument(st, "ST1");
			parent1.addArgument(t, "T1");
			parent1.addArgument(z, "Z1");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent2 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent2, true);
			parent2.addArgument(st, "ST2");
			parent2.addArgument(t, "T2");
			parent2.addArgument(z, "Z2");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		
		SSBNNode parent3 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent3, true);
			parent3.addArgument(st, "ST3");
			parent3.addArgument(t, "T3");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent4 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent4, true);
			parent4.addArgument(st, "ST4");
			parent4.addArgument(t, "T4");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent5 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent5, true);
			parent5.addArgument(st, "ST5");
			parent5.addArgument(t, "T5");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent6 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent6, true);
			parent6.addArgument(st, "ST6");
			parent6.addArgument(t, "T6");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		
		
		SSBNNode parent7 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent7, true);
			parent7.addArgument(st, "ST7");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}
		SSBNNode parent8 = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(1), new ProbabilisticNode());
		try {
			this.ssbnnode.addParent(parent8, true);
			parent8.addArgument(st, "ST8");
		} catch(SSBNNodeGeneralException e) {
			fail(e.getMessage());
		}

		Map<String, Collection<SSBNNode>> map = this.ssbnnode.getParentMapByWeakOV(t);
		assertNotNull(map);
		assertTrue(map.size() > 0);
		
		for (String key : map.keySet()) {
			System.out.println("Key = " + key);
		}
		
		for (Collection<SSBNNode> element : map.values()) {
			System.out.println(">>Parent's set:");
			for (SSBNNode node : element) {
				System.out.println(node.getName());
			}
		}
	}

	
	

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#isUsingDefaultCPT()}.
	 * Test method for {@link unbbayes.prs.mebn.ssbn.SSBNNode#setUsingDefaultCPT(boolean)}.
	 */
	public void testSetIsUsingDefaultCPT() {
		SSBNNode parent = SSBNNode.getInstance((DomainResidentNode)this.resident.getParents().get(0));
		assertTrue(!this.ssbnnode.isUsingDefaultCPT());
		assertTrue(!parent.isUsingDefaultCPT());
		
		this.ssbnnode.setUsingDefaultCPT(true);
		assertTrue(this.ssbnnode.isUsingDefaultCPT());
		assertTrue(parent.isUsingDefaultCPT());
		
		this.ssbnnode.setUsingDefaultCPT(false);
		assertTrue(!this.ssbnnode.isUsingDefaultCPT());
		assertTrue(!parent.isUsingDefaultCPT());
	}

	


	

}
