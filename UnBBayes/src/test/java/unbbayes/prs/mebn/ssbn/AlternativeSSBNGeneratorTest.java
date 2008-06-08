package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

public class AlternativeSSBNGeneratorTest extends TestCase{

	public static final String KB_FINDING_FILE = "examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm";

	public static final String STARTREK_UBF = "examples/mebn/StarTrek.ubf"; 

	public static final String KB_GENERATIVE_FILE = "examples/mebn/KnowledgeBase/KnowledgeBaseGenerative.plm";
	


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
		
		kb.saveGenerativeMTheory(mebn, new File(KB_GENERATIVE_FILE)); 
		
		try {
			kb.loadModule(new File(KB_FINDING_FILE), true); 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		System.out.println("Knowledge base init and filled");
		
		SSBNNode queryNode = createQueryNode_HarmPotential_ST4_T0(mebn); 
		
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
		} catch (OVInstanceFaultException e) {
			e.printStackTrace();
		} 
		
		System.out.println("SSBN OK");
		
		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(queryNode);
		System.out.println("End");
		
	}
	
	private static SSBNNode createQueryNode_StarshipClass_ST4(MultiEntityBayesianNetwork mebn) {
		return createGenericQueryNode(mebn, "Starship_MFrag", "StarshipClass", new String[]{"st"}, new String[]{"ST4"});
	}
	private static SSBNNode createQueryNode_HarmPotential_ST4_T3(MultiEntityBayesianNetwork mebn) {
		return createGenericQueryNode(mebn, "Starship_MFrag", "HarmPotential", new String[]{"st", "t"}, new String[]{"ST4", "T3"});
	}
	
	private static SSBNNode createQueryNode_HarmPotential_ST4_T0(MultiEntityBayesianNetwork mebn) {
		return createGenericQueryNode(mebn, "Starship_MFrag", "HarmPotential", new String[]{"st", "t"}, new String[]{"ST4", "T0"});
	}
	
	private static SSBNNode createGenericQueryNode(MultiEntityBayesianNetwork mebn,
			String mFragName, String residentNodeName, 
			String[] ovVariableNameList, String[] instanceNameList){
		
		MFrag mFrag = mebn.getMFragByName(mFragName); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName(residentNodeName); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode(), false); 
		
		try {
			for(int i = 0; i < ovVariableNameList.length; i++){
				queryNode.addArgument(residentNode.getOrdinaryVariableByName(ovVariableNameList[i]), instanceNameList[i]);	
			}
		} catch (SSBNNodeGeneralException e1) {
			e1.printStackTrace();
		}
		
		return queryNode;				
	}
	
}
