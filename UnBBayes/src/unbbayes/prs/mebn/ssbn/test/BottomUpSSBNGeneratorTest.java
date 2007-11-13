/**
 * 
 */
package unbbayes.prs.mebn.ssbn.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.BottomUpSSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author shou
 *
 */
public class BottomUpSSBNGeneratorTest extends TestCase {

	/**
	 * @param arg0
	 */
	public BottomUpSSBNGeneratorTest(String arg0) {
		super(arg0);
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
	
	/*
	public void testCollectionToArray() {
		
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("Roar of machinary god");
		DomainMFrag mfrag = new DomainMFrag("I am providence", mebn);
		mebn.addDomainMFrag(mfrag);
		
		TypeContainer container = new TypeContainer();
		try{
			container.createType("DeusMachina");
		} catch (TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}
		
		Collection<OrdinaryVariable> col = new ArrayList<OrdinaryVariable>();
		
		col.add(new OrdinaryVariable("Demonbane", container.getType("DeusMachina"),mfrag));
		col.add(new OrdinaryVariable("Aeon", container.getType("DeusMachina"),mfrag));
		col.add(new OrdinaryVariable("Liver Legis", container.getType("DeusMachina"),mfrag));
		col.add(new OrdinaryVariable("Nameless one", container.getType("DeusMachina"),mfrag));
		col.add(new OrdinaryVariable("Legacy of gold", container.getType("DeusMachina"),mfrag));
		
		
		OrdinaryVariable ovs[] = new OrdinaryVariable[0];
		
		ovs = col.toArray(ovs);
		
		assertEquals(ovs.length, col.size());
		for (OrdinaryVariable variable : ovs) {
			assertTrue(col.contains(variable));
		}
	}
	
	
	public void testGetContextOVInstance() {
		BottomUpSSBNGenerator gen = new BottomUpSSBNGenerator();
		TypeContainer container = new TypeContainer();
		try{
			container.createType("BeastHumans");
		} catch (TypeAlreadyExistsException e) {
			fail(e.getMessage());
		}
		
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("Utawarerumono");
		DomainMFrag mfrag = new DomainMFrag("Thuskuru",mebn);
		OrdinaryVariable wh = new OrdinaryVariable("Whitsarunemitea",container.getType("BeastHumans"),mfrag);
		OrdinaryVariable mo = new OrdinaryVariable("MotherOfTheForest",container.getType("BeastHumans"),mfrag);
		OrdinaryVariable ev = new OrdinaryVariable("Evenkhuruga",container.getType("BeastHumans"),mfrag);
		
		
		ContextNode context = new ContextNode("Erurhu", mfrag);
		Argument arg1 = new Argument("Hakuoro",context);
		arg1.setArgNumber(1);
		try {
			arg1.setOVariable(wh);
		} catch (ArgumentNodeAlreadySetException e) {
			fail(e.getMessage());
		}
		Argument arg2 = new Argument("Arurhu",context);
		arg2.setArgNumber(2);
		try {
			arg2.setOVariable(mo);
		} catch (ArgumentNodeAlreadySetException e) {
			fail(e.getMessage());
		}
		
		context.addArgument(arg1);
		context.addArgument(arg2);
		
		OVInstance ovwh = OVInstance.getInstance(wh);
		OVInstance ovmo = OVInstance.getInstance(mo);
		OVInstance ovev = OVInstance.getInstance(ev);
		
		
		Collection<OVInstance> ovis = new ArrayList<OVInstance>();
		ovis.add(ovwh);
		ovis.add(ovmo);
		ovis.add(ovev);
		
		OVInstance[] oviarray =  gen.getContextOVInstance(null, new ArrayList<OVInstance>(ovis));
		assertEquals(0, oviarray.length);
		
		oviarray =  gen.getContextOVInstance(context, null);
		assertEquals(0, oviarray.length);
		
		oviarray =  gen.getContextOVInstance(null, null);
		assertEquals(0, oviarray.length);
		
		
		oviarray =  gen.getContextOVInstance(context, new ArrayList<OVInstance>());
		assertEquals(0, oviarray.length);
		
		oviarray =  gen.getContextOVInstance(context, new ArrayList<OVInstance>(ovis));
		
		assertEquals(2, oviarray.length);
		assertEquals(ovwh, oviarray[0]);
		assertEquals(ovmo, oviarray[1]);
	}

	*/
	
	public static void main(String arguments[]){
		
		BottomUpSSBNGenerator ssbnGenerator = new BottomUpSSBNGenerator(); 
		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
	    KBFacade kbFacade = null; 
		MultiEntityBayesianNetwork mebn = null;
		
		PrOwlIO io = new PrOwlIO(); 
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek37.owl"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOMebnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		kb.loadModule(new File("testeGenerativeStarship.plm")); 
		kb.loadModule(new File("testeFindingsStarship.plm")); 
		
		kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 		
		
		DomainMFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		DomainResidentNode residentNode = mFrag.getDomainResidentNodeByName("HarmPotential"); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		try {
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("st"), "ST4");
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("t"), "T0");
		} catch (SSBNNodeGeneralException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		Query query = new Query(kbFacade, queryNode); 
		query.setMebn(mebn); 
		
		try {
			ssbnGenerator.generateSSBN(query);
		} catch (SSBNNodeGeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} 
		
	}
}
