package unbbayes.prs.mebn.ontology;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.giaalgorithm.ExplosiveSSBNGenerator;

public class StartrekTestSet extends TestSet{

	//Names of files
	private static final String STARTREK_UBF_FILE = "examples/mebn/StarTrek/StarTrek.ubf";
	private static final String TEST_FILE_NAME = "StarTrekTestSet.log"; 
	private static final String PATH = "examples/mebn/Tests/StarTrekTestSet"; 
	
	private static final String KB_SITUATION1 = "examples/mebn/StarTrek/KnowledgeBase_Situation1.plm";
	
	//Variables
	private MultiEntityBayesianNetwork mebn;	
	private static int testNumber = 0; 
	
	
	public StartrekTestSet(ISSBNGenerator ssbnGenerator){
		super(ssbnGenerator); 
		
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		//Loading the network
		UbfIO io = UbfIO.getInstance(); 
		try {
			mebn = io.loadMebn(new File(STARTREK_UBF_FILE));
		} catch (Exception e) {
			e.printStackTrace();
			logManager.appendln(e.toString());
			finishLog(PATH + "/" + TEST_FILE_NAME); 
			System.exit(1); 
		}
		
		File directory = new File(PATH);  
		if(!directory.exists()){
			directory.mkdir(); 
		}
	}
	
	public static void main(String arguments[]){
		
		ISSBNGenerator ssbnGenerator = new ExplosiveSSBNGenerator();
		
		TestSet testSet = new StartrekTestSet(ssbnGenerator);
		testSet.executeTests(); 
		testSet.finishLog(PATH + "/" + TEST_FILE_NAME); 
		
	}
	
	public void executeTests(){
		executeTest1(); 
	}

	public void executeTest1(){
		Query query = createQueryNode_HarmPotential_ST4_T3(mebn); 
		executeTestCase(
				1, 
				query.getQueryNode().getResident().getName(), 
				query.getQueryNode().getResident().getMFrag().getName(), 
				KB_SITUATION1 , 
				query); 
	}
	
	private void executeTestCase(int index, String nameResidentNode, String nameMFrag, 
			String findingFileName, Query query){
		
		printTestHeader(index, nameResidentNode);
		
		testNumber++; 
		
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		kb.createGenerativeKnowledgeBase(mebn); 
		
		boolean loaded = loadFindingModule(kb, findingFileName); 
		if(!loaded){
			return; 
		}
		
		query.setKb(kb); 
		
		executeQueryAndPrintResults(query, PATH + "/" + "Test" + testNumber + ".xml"); 				
	}

	private Query createQueryNode_HarmPotential_ST4_T3(MultiEntityBayesianNetwork mebn) {
		return createGenericQueryNode(mebn, "Starship_MFrag", "HarmPotential", new String[]{"st", "t"}, new String[]{"ST4", "T3"}, null);
	}	
	
}
