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
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

public class AlternativeSSBNGeneratorTest extends TestCase{

	public static final String KB_FINDING_FILE = "examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm";
	public static final String STARTREK_UBF = "examples/mebn/StarTrek52.ubf"; 

	public static void main(String arguments[]){
		
		System.out.println("Begin");
		
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		ISSBNGenerator ssbnGenerator = new AlternativeSSBNGenerator(); 
		
		MultiEntityBayesianNetwork mebn = null;
		
		UbfIO io = UbfIO.getInstance(); 
		try {
			mebn = io.loadMebn(new File(STARTREK_UBF));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IOMebnException e) {
			e.printStackTrace();
		}
		System.out.println("Stattrek UBF loaded");

		
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			kb.createEntityDefinition(entity);
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				kb.createRandomVariableDefinition(resident);
			}
		}
		
		try {
			kb.loadModule(new File(KB_FINDING_FILE)); 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		System.out.println("Knowledge base init and filled");
		
		SSBNNode queryNode = createQueryNode_HarmPotential_ST4_T3(mebn); 
		
		Query query = new Query(kb, queryNode, mebn); 
		query.setMebn(mebn); 
		
		try {
			ssbnGenerator.generateSSBN(query);
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
		}
		catch (ImplementationRestrictionException ei) {
			ei.printStackTrace();
		} catch (MEBNException e) {
			e.printStackTrace();
		} 
		
		System.out.println("SSBN OK");
		
		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(queryNode);
		System.out.println("End");
		
	}
	
	private static SSBNNode createQueryNode_HarmPotential_ST4_T3(MultiEntityBayesianNetwork mebn) {
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName("HarmPotential"); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		try {
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("st"), "ST4");
			queryNode.addArgument(residentNode.getOrdinaryVariableByName("t"), "T3");
		} catch (SSBNNodeGeneralException e1) {
			e1.printStackTrace();
		}
		return queryNode;
	}
}
