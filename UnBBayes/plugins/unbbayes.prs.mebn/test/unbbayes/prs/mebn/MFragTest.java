/**
 * 
 */
package unbbayes.prs.mebn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;

/**
 * @author user
 *
 */
public class MFragTest extends TestCase {

	public static final String STARTREK_UBF = "plugins/unbbayes.prs.mebn/resources/mebn/StarTrek.ubf"; 
	
	/**
	 * @param arg0	
	 */
	public MFragTest(String arg0) {
		super(arg0);
	}
	
	public void testgetContextNodeByOrdinaryVariableRelated(){
		
		MultiEntityBayesianNetwork mebn = loadStartrekOntologyExample();
		
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag");
		Collection<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		OrdinaryVariable ovZ = mFrag.getOrdinaryVariableByName("z"); 
		ovList.add(ovZ); 
		Collection<ContextNode> collection = mFrag.getContextNodeByOrdinaryVariableRelated(ovList); 
		assertEquals(collection.size(),	3); 
		
		mFrag = mebn.getMFragByName("Starship_MFrag");
		ovList = new ArrayList<OrdinaryVariable>(); 
		ovZ = mFrag.getOrdinaryVariableByName("st"); 
		ovList.add(ovZ); 
		collection = mFrag.getContextNodeByOrdinaryVariableRelated(ovList); 
		assertEquals(collection.size(),	3); 
		
		mFrag = mebn.getMFragByName("SensorReport_MFrag");
		ovList = new ArrayList<OrdinaryVariable>(); 
		ovZ = mFrag.getOrdinaryVariableByName("st"); 
		ovList.add(ovZ); 
		collection = mFrag.getContextNodeByOrdinaryVariableRelated(ovList); 
		assertEquals(collection.size(),	2); 
		
		mFrag = mebn.getMFragByName("SensorReport_MFrag");
		ovList = new ArrayList<OrdinaryVariable>(); 
		ovZ = mFrag.getOrdinaryVariableByName("sr"); 
		ovList.add(ovZ); 
		collection = mFrag.getContextNodeByOrdinaryVariableRelated(ovList); 
		assertEquals(collection.size(),	2); 
	}

	private MultiEntityBayesianNetwork loadStartrekOntologyExample() {
		UbfIO io = UbfIO.getInstance(); 
		MultiEntityBayesianNetwork mebn = null; 
		
		try {
			mebn = io.loadMebn(new File(STARTREK_UBF));
		} catch (Exception e) {
			e.printStackTrace();
			fail(); 
		}
		return mebn;
	}
	
	
	public void testContainsNode(){
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("teste");
		MFrag mfrag1 = new MFrag("mfrag1",mebn);
		MFrag mfrag2 = new MFrag("mfrag2",mebn);
		
		ResidentNode resident1 = new ResidentNode("resident1",mfrag1);
		mfrag1.addResidentNode(resident1);
		ResidentNode resident2 = new ResidentNode("resident2",mfrag2);
		mfrag2.addResidentNode(resident2);
		InputNode input1 = new InputNode("resident1",mfrag2);
		mfrag2.addInputNode(input1);
		try {
			input1.setInputInstanceOf(resident1);
		} catch (Exception e) {
			fail("A cycle has been found");
		}
		try {
			resident2.addParent(input1);
		} catch (InvalidParentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Node> temp = mfrag1.getNodeList();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i).getName());
			System.out.println(temp.get(i).getClass());
		}
		System.out.println("===============");
		temp = mfrag2.getNodeList();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i).getName());
			System.out.println(temp.get(i).getClass());
		}
		
		assertTrue(mfrag1.containsNode(resident1));
		assertNotNull(mfrag1.containsNode("resident1"));
		assertTrue(!mfrag1.containsNode(resident2));
		assertNull(mfrag1.containsNode("resident2"));
		//assertTrue(!(mfrag1.containsNode(input1))); // This is searching by node name
		
		assertTrue(mfrag2.containsNode(resident2));
		assertNotNull(mfrag2.containsNode("resident2"));
		//assertTrue(!mfrag2.containsNode(resident1));
		assertNotNull(mfrag2.containsNode("resident1"));
		assertTrue(mfrag2.containsNode(input1));
		
	}

	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for unbbayes.prs.mebn.test.MFragTest");
		//$JUnit-BEGIN$
		suite.addTest(DomainMFragTest.suite());
		suite.addTest(new TestSuite(MFragTest.class));
		return suite;
	}
}
