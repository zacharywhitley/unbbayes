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
		
		
	}
	 
	
	protected void setUp() throws Exception {
		
	}
	
	protected void tearDown() throws Exception {
	}
	
	public void testInitializationWithHepparSet(){

		System.out.println("\n\n---------------------------------------------------");
		System.out.println("                 INICIALIZATION WITH HEPPAR SET        ");
		System.out.println("---------------------------------------------------");
		
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "false"); 
		
		hepparTestSet = new HeparIITestSet(new LaskeySSBNGenerator(parameters)); 
		
		SSBN ssbn = hepparTestSet.executeTestCase1(); 
		System.out.println("HeparTestSet TestCase 01");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		
		System.out.println("....\n\n");

		assertEquals(ssbn.getSsbnNodeList().size(), 1);
				
		ssbn = hepparTestSet.executeTestCase18(); 
		System.out.println("HeparTestSet TestCase 18");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());

		assertEquals(ssbn.getSsbnNodeList().size(), 2); 
		
		ssbn = hepparTestSet.executeTestCase24(); 
		System.out.println("HeparTestSet TestCase 24");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		
		assertEquals(ssbn.getSsbnNodeList().size(), 3); 
		 
	}
	
	public void testBuildWithHepparSet(){
		
		System.out.println("\n\n---------------------------------------------------\n");
		System.out.println("                 BUILD WITH HEPPAR SET                 ");
		System.out.println("---------------------------------------------------");
		
		LaskeyAlgorithmParameters parameters = new LaskeyAlgorithmParameters(); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "false"); 
		parameters.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "false"); 
		
		hepparTestSet = new HeparIITestSet(new LaskeySSBNGenerator(parameters)); 
		
		/* 
		 * Case 1: Expected
		 * AST((p,maria)) 
		 * ToxicHepatitis((p,maria))
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 */
		SSBN ssbn = hepparTestSet.executeTestCase1(); 
		System.out.println("HeparTestSet TestCase 01");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 4); 
		
		System.out.println("\n---------------------------------------------------\n");
		
		/* 
		 * Case 18: Expected
		 * ToxicHepatitis((p,maria))
		 * AST((p,maria))=a700_400 [F] 
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 */
		ssbn = hepparTestSet.executeTestCase18(); 
		System.out.println("HeparTestSet TestCase 18");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 4); 
		
		System.out.println("\n---------------------------------------------------\n");
		
		/*
		 * Test 24: Expected
		 * ToxicHepatitis((p,maria))
		 * Fatigue((p,maria))=true [F] 
		 * AST((p,maria))=a700_400 [F] 
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 */
		ssbn = hepparTestSet.executeTestCase24(); 
		System.out.println("HeparTestSet TestCase 24");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 5); 
		
		System.out.println("\n---------------------------------------------------\n");
		
		/*
		 * Test 28: Expected (11 nodes)
		 * 
		 * AST
		 * Fatigue[F]
		 * EnlargedSpleen[F]
		 * ALT[F]
		 * INR[F]
		 * 
		 * ToxicHepatitis((p,maria))
		 * HistoryAlcAbuse((p,maria))
		 * HepatoxicMeds((p,maria))
		 * 
		 * FunctionalHiperbilirium
		 * Sex
		 * Age
		 * 
		 */
		ssbn = hepparTestSet.executeTestCase28(); 
		System.out.println("HeparTestSet TestCase 28");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 11); 
		 
		/*
		 * Test 63: Expected (7 nodes)
		 * 
		 * INR
		 * HistoryAlcAbuse((p,maria)) [F]
		 * HepatoxicMeds((p,maria)) [F]
		 * Sex [F]
		 * 
		 * ToxicHepatits
		 * FunctionalHiperbilirium
		 * Age
		 * 
		 */
		ssbn = hepparTestSet.executeTestCase63(); 
		System.out.println("HeparTestSet TestCase 63");
		for(SimpleSSBNNode s: ssbn.getSsbnNodeList()){
			System.out.println(s);
		}
		System.out.println("Size=" + ssbn.getSsbnNodeList().size());
		assertEquals(ssbn.getSsbnNodeList().size(), 11); 
		
	}
	
}
