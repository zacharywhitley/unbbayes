package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import junit.framework.TestCase;
import unbbayes.prs.mebn.ontology.HeparIITestSet;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;

/**
 * 
 * @author Laecio
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
	
	public void testHepparSet(){
		
		SSBN ssbn = hepparTestSet.executeTestCase1();
		for(SimpleSSBNNode node: ssbn.getSsbnNodeList()){
			
		}
		
		assertTrue(true); 
	}
	
}
