package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import junit.framework.TestCase;
import unbbayes.prs.mebn.ontology.HeparIITestSet;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;

/**
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class LaskeySSBNGeneratorTest  extends TestCase {

	HeparIITestSet hepparTestSet; 
	
	public LaskeySSBNGeneratorTest(String arg0) {
		super(arg0);
		
		hepparTestSet = new HeparIITestSet(new LaskeySSBNGenerator()); 
	}
	 
	
	protected void setUp() throws Exception {
		
	}
	
	protected void tearDown() throws Exception {
	}
	
	public void testInitializationWithHepparSet(){

		SSBN ssbn = hepparTestSet.executeTestCase1(); 
		System.out.println("HeparTestSet TestCase 01");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		
		
		ssbn = hepparTestSet.executeTestCase18(); 
		System.out.println("HeparTestSet TestCase 18");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		
		ssbn = hepparTestSet.executeTestCase24(); 
		System.out.println("HeparTestSet TestCase 24");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		
		//1 node is the query, the others are findings. 
		assertEquals(hepparTestSet.executeTestCase1().getSsbnNodeList().size(), 1);
		assertEquals(hepparTestSet.executeTestCase18().getSsbnNodeList().size(), 2); 
		assertEquals(hepparTestSet.executeTestCase24().getSsbnNodeList().size(), 3); 
		 
	}
	
}
