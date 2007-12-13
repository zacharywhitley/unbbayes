/**
 * 
 */
package unbbayes.prs.mebn.ssbn.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import unbbayes.io.mebn.UbfIO;
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
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author Shou Matsumoto
 * @author Laécio Lima dos Santos
 */
public class BottomUpSSBNGeneratorTest extends TestCase {

//	public static final String KB_GENERATIVE_FILE = "testeGenerativeStarship.plm"; 
//	public static final String KB_FINDING_FILE = "testeFindingsStarship.plm";  
	public static final String KB_GENERATIVE_FILE = "generative.plm"; 
	public static final String KB_FINDING_FILE = "findings.plm";  
	public static final String STARTREK_UBF = "examples/mebn/StarTrek40.ubf"; 
	
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
		
		BottomUpSSBNGenerator ssbnGenerator = new BottomUpSSBNGenerator(); 
		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
	    KBFacade kbFacade = null; 
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

		kb.loadModule(new File(BottomUpSSBNGeneratorTest.KB_GENERATIVE_FILE)); 
		kb.loadModule(new File(BottomUpSSBNGeneratorTest.KB_FINDING_FILE)); 
		
		kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 		
		
		SSBNNode queryNode = createQueryNode_HarmPotential_ST4_T0(mebn); 
		
		Query query = new Query(kbFacade, queryNode, mebn); 
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
		} 
		
	}

	private static SSBNNode createQueryNode_HarmPotential_ST4_T0(MultiEntityBayesianNetwork mebn) {
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
		return queryNode;
	}
	
	private static SSBNNode createQueryNode_HarmPotential_ST4_T4(MultiEntityBayesianNetwork mebn) {
		DomainMFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		DomainResidentNode residentNode = mFrag.getDomainResidentNodeByName("HarmPotential"); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		try {
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("st"), "ST4");
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("t"), "T4");
		} catch (SSBNNodeGeneralException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return queryNode;
	}
	
}
