/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author Shou Matsumoto
 * @author Laécio Lima dos Santos
 */
public class BottomUpSSBNGeneratorTest extends TestCase {

//	public static final String KB_GENERATIVE_FILE = "testeGenerativeStarship.plm"; 
//	public static final String KB_FINDING_FILE = "testeFindingsStarship.plm";  
//	public static final String KB_FINDING_FILE = "KnowledgeBaseWithStarshipZoneST4.plm";  
//	public static final String KB_GENERATIVE_FILE = "generative.plm"; 
//	public static final String KB_FINDING_FILE = "findings.plm";  
//	public static final String STARTREK_UBF = "examples/mebn/StarTrek46.ubf"; 
	
//	public static final String KB_GENERATIVE_FILE = "examples/mebn/XORGenerative2.plm"; 
//	public static final String KB_FINDING_FILE = "examples/mebn/XORKb.plm";
//	public static final String STARTREK_UBF = "examples/mebn/XORExample2.ubf"; 
	
	public static final String KB_GENERATIVE_FILE = "examples/mebn/XORGenerative.plm"; 
	public static final String KB_FINDING_FILE = "examples/mebn/XORKb.plm";
	public static final String STARTREK_UBF = "examples/mebn/XORExample.ubf"; 
	
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
	
	public static void main(String arguments[]){
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		BottomUpSSBNGenerator ssbnGenerator = new BottomUpSSBNGenerator(); 
		
		MultiEntityBayesianNetwork mebn = null;
		
		UbfIO io = UbfIO.getInstance(); 
		try {
			mebn = io.loadMebn(new File(BottomUpSSBNGeneratorTest.STARTREK_UBF));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOMebnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			kb.loadModule(new File(BottomUpSSBNGeneratorTest.KB_GENERATIVE_FILE)); 
			kb.loadModule(new File(BottomUpSSBNGeneratorTest.KB_FINDING_FILE)); 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		SSBNNode queryNode = createQueryNodeXOR(mebn); 
		
		Query query = new Query(kb, queryNode, mebn); 
		query.setMebn(mebn); 
		
		try {
			ssbnGenerator.generateSSBN(query);
		} catch (SSBNNodeGeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ImplementationRestrictionException ei) {
				// TODO Auto-generated catch block
			ei.printStackTrace();
		} catch (MEBNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	private static SSBNNode createQueryNode_HarmPotential_ST4_T0(MultiEntityBayesianNetwork mebn) {
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName("HarmPotential"); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		try {
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("st"), "ST4");
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("t"), "T0");
		} catch (SSBNNodeGeneralException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return queryNode;
	}
	
	private static SSBNNode createQueryNodeXOR(MultiEntityBayesianNetwork mebn) {
		MFrag mFrag = mebn.getMFragByName("ExampleMFrag"); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName("ResidentSample"); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		try {
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("r"), "R");
		} catch (SSBNNodeGeneralException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return queryNode;
	}
	
	private static SSBNNode createQueryNode_HarmPotential_ST4_T3(MultiEntityBayesianNetwork mebn) {
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName("HarmPotential"); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		try {
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("st"), "ST4");
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("t"), "T3");
		} catch (SSBNNodeGeneralException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return queryNode;
	}
	
}
