package unbbayes.prs.mebn.ontology;

import java.io.File;
import java.io.IOException;

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
import unbbayes.prs.mebn.ssbn.ExplosiveSSBNGenerator;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SituationSpecificBayesianNetwork;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

public class StartrekTestSet{

	public static final String KB_FINDING_FILE = "examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm";
	public static final String KB_GENERATIVE_FILE = "examples/mebn/KnowledgeBase/KnowledgeBaseGenerative.plm";
	
	public static final String STARTREK_UBF = "examples/mebn/StarTrek55.ubf"; 

	public static void main(String arguments[]){
		
		System.out.println("Begin");

		ISSBNGenerator ssbnGenerator = new ExplosiveSSBNGenerator(); 
		
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

		KnowledgeBase kb = createGenerativeKnowledgeBase(mebn);
		kb.saveGenerativeMTheory(mebn, new File(KB_GENERATIVE_FILE)); 
		loadFindingModule(kb);
		
		System.out.println("Knowledge base init and filled");
		
		SSBNNode queryNode = createQueryNode_HarmPotential_ST4_T3(mebn); 
		
		Query query = new Query(kb, queryNode, mebn); 
		query.setMebn(mebn); 
		
		try {
			SituationSpecificBayesianNetwork ssbn = ssbnGenerator.generateSSBN(query);
			ssbn.compileAndInitializeSSBN();
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
		}
		catch (ImplementationRestrictionException ei) {
			ei.printStackTrace();
		} catch (MEBNException e) {
			e.printStackTrace();
		} catch (OVInstanceFaultException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		System.out.println("SSBN OK");
		
//		BottomUpSSBNGenerator.printAndSaveCurrentNetwork(queryNode);
		System.out.println("End");
		
	}

	private static void loadFindingModule(KnowledgeBase kb) {
		try {
			kb.loadModule(new File(KB_FINDING_FILE), true); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static KnowledgeBase createGenerativeKnowledgeBase(
			MultiEntityBayesianNetwork mebn) {
		
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			kb.createEntityDefinition(entity);
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				kb.createRandomVariableDefinition(resident);
			}
		}
		return kb;
		
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
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode()); 
		
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
