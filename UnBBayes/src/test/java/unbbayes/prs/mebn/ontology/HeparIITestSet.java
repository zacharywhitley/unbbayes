package unbbayes.prs.mebn.ontology;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.io.ILogManager;
import unbbayes.io.TextLogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.BooleanStateEntity;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.giaalgorithm.ExplosiveSSBNGenerator;

/**
 * This is a set of case test developer por Cheol Young Park using the Hepar II 
 * Network. The objective is test the ssbn algorithm for situations that don't 
 * envolve context nodes. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class HeparIITestSet  extends TestSet{

	//Names of files
	private static final String HEPARII_UBF_FILE = "examples/mebn/HeparII/HeparII_01.ubf";
	private static final String TEST_FILE_NAME = "HeparIITestSet.log"; 
	
	private static ResourceBundle resourceFiles = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.resources.ResourceFiles");
	
	//Variables
	private MultiEntityBayesianNetwork mebn;	
	private static int testNumber = 0; 
	
	//Mapping the MEBN elements
	private static final String RV_ALT = "ALT"; 
	private static final String RV_AST = "AST"; 
	private static final String RV_AGE = "Age"; 
	private static final String RV_BloodUrea = "BloodUrea"; 
	private static final String RV_EnlargedSpleen = "EnlargedSpleen"; 
	private static final String RV_Fatigue = "Fatigue"; 
	private static final String RV_FunctionalHyperbilirubinemia = "FunctionalHyperbilirubinemia"; 
	private static final String RV_HaemorrhagieDiathesis = "HaemorrhagieDiathesis"; 
	private static final String RV_HepaticEncephalopathy = "HepaticEncephalopathy"; 
	private static final String RV_HepatoxicMeds = "HepatoxicMeds"; 
	private static final String RV_HistoryAlcAbuse = "HistoryAlcAbuse"; 
	private static final String RV_INR = "INR"; 
	private static final String RV_ImpairedConsciousness = "ImpairedConsciousness"; 
	private static final String RV_IncreasedLiverDensity = "IncreasedLiverDensity"; 
	private static final String RV_Itching = "Itching"; 
	private static final String RV_Jaundice = "Jaundice"; 
	private static final String RV_JointsSwelling = "JointsSwelling"; 
	private static final String RV_MusculoSkeletalPain = "MusculoSkeletalPain"; 
	private static final String RV_PBC = "PBC"; 
	private static final String RV_PlateletCount = "PlateletCount"; 
	private static final String RV_Sex = "Sex"; 
	private static final String RV_TotalBilirubin = "TotalBilirubin"; 
	private static final String RV_ToxicHepatitis = "ToxicHepatitis"; 
	private static final String RV_Yellowingoftheskin = "Yellowingoftheskin"; 
	
	private static final String RV_HistoryAlcAbuse_MFrag = "ToxicHepatitis_MFrag"; 
	private static final String RV_HepatoxicMeds_MFrag = "ToxicHepatitis_MFrag"; 
	
	
	private static final String OV_P = "p"; 
	
	public HeparIITestSet(ISSBNGenerator ssbnGenerator){
		this(ssbnGenerator, new TextLogManager()); 
	}
	
	public HeparIITestSet(ISSBNGenerator ssbnGenerator, ILogManager logManager){
		super(ssbnGenerator, logManager); 
		
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		String path = resourceFiles.getString("PathHepparIITestDirectory");
		File directory = new File(path);  
		if(!directory.exists()){
			directory.mkdir(); 
		}
		
		//Loading the network
		UbfIO io = UbfIO.getInstance(); 
		try {
			mebn = io.loadMebn(new File(HEPARII_UBF_FILE));
		} catch (Exception e) {
			e.printStackTrace();
			logManager.appendln(e.toString());
			finishLog(path + "/" + TEST_FILE_NAME); 
			System.exit(1); 
		}


	}
	
	public static void main(String[] args){
		
		ISSBNGenerator ssbnGenerator = new ExplosiveSSBNGenerator();
		
		TestSet testSet = new HeparIITestSet(ssbnGenerator, new TextLogManager());

		testSet.finishLog(resourceFiles.getString("PathHepparIITestDirectory") + "/" + TEST_FILE_NAME); 
		
	}
	

	public void executeTests(){
		//Executing the test
		
		executeTestCase1(); 
		executeTestCase2(); 
		executeTestCase3(); 
		executeTestCase4(); 
		executeTestCase5(); 
		executeTestCase6(); 
		executeTestCase7(); 
		executeTestCase8(); 
		executeTestCase9(); 
		executeTestCase10(); 
		executeTestCase11(); 
		executeTestCase12(); 
		executeTestCase13(); 
		executeTestCase14(); 
		executeTestCase15(); 
		executeTestCase16(); 
		executeTestCase17(); 
		
		executeTestCase18(); 
		executeTestCase19(); 
		executeTestCase20(); 
		executeTestCase21(); 
		executeTestCase22(); 
		executeTestCase23(); 
		executeTestCase24(); 
		executeTestCase25(); 
		executeTestCase26(); 
		executeTestCase27(); 
		executeTestCase28(); 
		
		executeTestCase29(); 
		executeTestCase30(); 
		executeTestCase31(); 
		executeTestCase32(); 
		executeTestCase33(); 
		executeTestCase34(); 
		executeTestCase35(); 
		executeTestCase36(); 
		executeTestCase37(); 
		
		executeTestCase38(); 
		executeTestCase39(); 
		executeTestCase40(); 
		executeTestCase41(); 
		executeTestCase42(); 
		executeTestCase43(); 
		executeTestCase44(); 
		executeTestCase45(); 
		executeTestCase46(); 
		executeTestCase47(); 
		executeTestCase48(); 
		executeTestCase49(); 
		executeTestCase50(); 
		executeTestCase51(); 
		executeTestCase52(); 
		executeTestCase53(); 
		executeTestCase54(); 
		executeTestCase55(); 
		executeTestCase56(); 
		executeTestCase57(); 
		
		executeTestCase58(); 
		executeTestCase59(); 
		executeTestCase60(); 
		executeTestCase61(); 
		executeTestCase62(); 
		executeTestCase63(); 
		executeTestCase64(); 
		executeTestCase65(); 
		executeTestCase66(); 
		executeTestCase67(); 
		executeTestCase68(); 
		executeTestCase69(); 
		executeTestCase70(); 
		executeTestCase71(); 
		executeTestCase72(); 
		executeTestCase73(); 
		executeTestCase74(); 
		executeTestCase75(); 
		executeTestCase76(); 
		executeTestCase77(); 
	
		executeTestCase78(); 
		executeTestCase79(); 
		executeTestCase80(); 
		executeTestCase81(); 
		executeTestCase82(); 
		executeTestCase83(); 
		executeTestCase84(); 
		executeTestCase85(); 
		executeTestCase86(); 
		executeTestCase87(); 
		executeTestCase88(); 
		executeTestCase89(); 
		executeTestCase90(); 
		executeTestCase91(); 
		executeTestCase92(); 
		executeTestCase93(); 
		executeTestCase94(); 
		executeTestCase95(); 
		executeTestCase96(); 
		executeTestCase97(); 
		
		executeTestCase98(); 
		executeTestCase99(); 
		executeTestCase100(); 
		executeTestCase101(); 
		executeTestCase102(); 
		executeTestCase103(); 
		executeTestCase104(); 
		executeTestCase105(); 
		executeTestCase106(); 
		executeTestCase107(); 
		executeTestCase108(); 
		executeTestCase109(); 
		executeTestCase110(); 
		executeTestCase111(); 
		executeTestCase112(); 
		executeTestCase113(); 
		executeTestCase114(); 
		executeTestCase115(); 
		executeTestCase116(); 
		executeTestCase117(); 
		
		executeTestCase118(); 
		executeTestCase119(); 
		executeTestCase120(); 
		executeTestCase121(); 
		executeTestCase122(); 
		executeTestCase123(); 
		executeTestCase124(); 
		executeTestCase125(); 
		executeTestCase126(); 
		executeTestCase127(); 
		executeTestCase128(); 
		executeTestCase129(); 
		executeTestCase130(); 
		executeTestCase131(); 
		executeTestCase132(); 
		executeTestCase133(); 
		executeTestCase134(); 
		executeTestCase135(); 
		executeTestCase136(); 
		executeTestCase137(); 
		executeTestCase138(); 
		executeTestCase139(); 
	}
	
	/**
	 * Record the log informations
	 */
	public void recordLog(){
		this.finishLog(resourceFiles.getString("PathHepparIITestDirectory") + "/" + TEST_FILE_NAME); 
	}
	
	private SSBN executeTestCase(int index, String nameResidentNode, String nameMFrag){
		return executeTestCase(index, nameResidentNode, nameMFrag, null); 			
	}

	private SSBN executeTestCase(int index, String nameResidentNode, String nameMFrag, 
			RandomVariableFinding[] findingList){
		
		printTestHeader(index, nameResidentNode);
		
		testNumber++; 
		
		clearRandomVariableFinding(); 
		
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		
		kb.createGenerativeKnowledgeBase(mebn); 
		
		if(findingList != null){
			for(RandomVariableFinding finding: findingList){
				ResidentNode node = finding.getNode(); 
				node.addRandomVariableFinding(finding); 
				kb.insertRandomVariableFinding(finding); 
			}
		}

		Query query = createGenericQueryNode(mebn, nameMFrag, nameResidentNode, 
				new String[]{"p"}, 
				new String[]{"maria"}, kb); 

		return executeQueryAndPrintResults(query, resourceFiles.getString("PathHepparIITestDirectory") + "/" + "Test" + testNumber + ".xml"); 				
	}
	
	private void clearRandomVariableFinding(){
		for(MFrag mFrag: mebn.getMFragList()){
			for(ResidentNode resident: mFrag.getResidentNodeList()){
				resident.cleanRandomVariableFindingList(); 
			}
		}
	}
	
	private RandomVariableFinding createFinding(String mFragName, String residentNodeName, String state) throws CategoricalStateDoesNotExistException {
		MFrag mFrag = mebn.getMFragByName(mFragName); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName(residentNodeName);
		
		ObjectEntity patient = mebn.getObjectEntityContainer().getObjectEntityByName("Patient"); 
		ObjectEntityInstance maria = new ObjectEntityInstance("maria" , patient);
		
		CategoricalStateEntity entity = mebn.getCategoricalStatesEntityContainer().getCategoricalState(state); 
		
		return  new RandomVariableFinding(residentNode, 
				new ObjectEntityInstance[]{maria}, entity, mebn);
	
	}
	
	private RandomVariableFinding createBooleanFinding(String mFragName, String residentNodeName, boolean state) throws CategoricalStateDoesNotExistException {
		MFrag mFrag = mebn.getMFragByName(mFragName); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName(residentNodeName);
		
		ObjectEntity patient = mebn.getObjectEntityContainer().getObjectEntityByName("Patient"); 
		ObjectEntityInstance maria = new ObjectEntityInstance("maria" , patient); 
		
		BooleanStateEntity entity = null; 

		if(state){
			entity = mebn.getBooleanStatesEntityContainer().getTrueStateEntity(); 
		}else{
			entity = mebn.getBooleanStatesEntityContainer().getFalseStateEntity(); 	
		}

		return  new RandomVariableFinding(residentNode, 
				new ObjectEntityInstance[]{maria}, entity, mebn);

	}
	
	public SSBN executeTestCase1(){
		return executeTestCase(1, RV_AST, RV_AST + "_MFrag"); 				
	}

	public SSBN executeTestCase2(){
		return executeTestCase(2, RV_Fatigue, RV_Fatigue + "_MFrag"); 					
	}
	
	public SSBN executeTestCase3(){
		return executeTestCase(3, RV_EnlargedSpleen, RV_EnlargedSpleen + "_MFrag"); 				
	}
	
	public SSBN executeTestCase4(){
		return executeTestCase(4, RV_ALT, RV_ALT + "_MFrag"); 				
	}
	
	public SSBN executeTestCase5(){
		return executeTestCase(5, RV_HaemorrhagieDiathesis, RV_HaemorrhagieDiathesis + "_MFrag"); 			
	}
	
	public SSBN executeTestCase6(){
		return executeTestCase(6, RV_INR, RV_INR + "_MFrag"); 					
	}

	public SSBN  executeTestCase7(){
		return executeTestCase(7, RV_PlateletCount, RV_PlateletCount + "_MFrag"); 				
	}
	
	public SSBN executeTestCase8(){
		return executeTestCase(8, RV_Yellowingoftheskin, RV_Yellowingoftheskin + "_MFrag"); 				
	}

	public SSBN  executeTestCase9(){
		return executeTestCase(9, RV_Itching, RV_Itching + "_MFrag"); 					
	}
	
	public SSBN  executeTestCase10(){
		return executeTestCase(10, RV_Jaundice, RV_Jaundice + "_MFrag"); 				
	}
	
	public SSBN executeTestCase11(){
		return executeTestCase(11, RV_TotalBilirubin, RV_TotalBilirubin + "_MFrag"); 				
	}
	
	public SSBN  executeTestCase12(){
		return executeTestCase(12, RV_BloodUrea, RV_BloodUrea + "_MFrag"); 			
	}
	
	public SSBN  executeTestCase13(){
		return executeTestCase(13, RV_IncreasedLiverDensity, RV_IncreasedLiverDensity + "_MFrag"); 					
	}

	public SSBN  executeTestCase14(){
		return executeTestCase(14, RV_ImpairedConsciousness, RV_ImpairedConsciousness + "_MFrag"); 				
	}
	
	public SSBN executeTestCase15(){
		return executeTestCase(15, RV_HepaticEncephalopathy, RV_HepaticEncephalopathy + "_MFrag"); 			
	}
	
	public SSBN  executeTestCase16(){
		return executeTestCase(16, RV_MusculoSkeletalPain, RV_MusculoSkeletalPain + "_MFrag"); 					
	}

	public SSBN  executeTestCase17(){
		return executeTestCase(17, RV_JointsSwelling, RV_JointsSwelling + "_MFrag"); 				
	}	
	
	
	public SSBN executeTestCase18(){
		
		try{
			RandomVariableFinding finding = createFinding(RV_AST + "_MFrag", RV_AST, "a700_400");
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			return executeTestCase(18, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
		
		return null; 

	}
	
	public SSBN executeTestCase19(){
		
		try{
			RandomVariableFinding finding = createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true);
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			return executeTestCase(19, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
		
		return null; 

	}
	
	private void executeTestCase20(){
		
		try{
			RandomVariableFinding finding = createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true);
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCase(20, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	private void executeTestCase21(){
		
		try{
			RandomVariableFinding finding = createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200");
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCase(21, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	
	private void executeTestCase22(){
		
		try{
			RandomVariableFinding finding = createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true);
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCase(22, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}	
	
	private void executeTestCase23(){
		
		try{
			RandomVariableFinding finding = createFinding(RV_INR + "_MFrag", RV_INR, "a200_110");
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCase(23, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	public SSBN executeTestCase24(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true)
			}; 

			return executeTestCase(24, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
		
		return null; 

	}
	
	public void executeTestCase25(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true)
			}; 

			executeTestCase(25, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	public void executeTestCase26(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true), 
					createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200")
			}; 

			executeTestCase(26, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	
	public void executeTestCase27(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true), 
					createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCase(27, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	
	public SSBN executeTestCase28(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true), 
					createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200"), 
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			return executeTestCase(28, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
		
		return null; 
	}
	
	public void executeTestCase29(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCase(29, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase30(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCase(30, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase31(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20")
			}; 

			executeTestCase(31, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase32(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)
			}; 

			executeTestCase(32, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase33(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)
			}; 

			executeTestCase(33, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase34(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
			}; 

			executeTestCase(34, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase35(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"),
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCase(35, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase36(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true),
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCase(36, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase37(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true),
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true),
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCase(37, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase38(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_PlateletCount + "_MFrag", RV_PlateletCount, "a597_300")
			}; 

			executeTestCase(38, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	public void executeTestCase39(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCase(39, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	public void executeTestCase40(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a8820")
			}; 

			executeTestCase(40, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	

	public void executeTestCase41(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)
			}; 

			executeTestCase(41, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase42(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)
			}; 

			executeTestCase(42, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase43(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
			}; 

			executeTestCase(43, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase44(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)
			}; 

			executeTestCase(44, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase45(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, "a165_50")
			}; 

			executeTestCase(45, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase46(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true)
			}; 

			executeTestCase(46, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase47(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true)
			}; 

			executeTestCase(47, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase48(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true)
			}; 

			executeTestCase(48, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase49(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true)
			}; 

			executeTestCase(49, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase50(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCase(50, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase51(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)
			}; 

			executeTestCase(51, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase52(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)		
			}; 

			executeTestCase(52, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase53(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)

					}; 

			executeTestCase(53, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase54(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)

					}; 

			executeTestCase(54, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase55(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true), 
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)

					}; 

			executeTestCase(55, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase56(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true) 
					}; 

			executeTestCase(56, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase57(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true) 
					}; 

			executeTestCase(57, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase58(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(58, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase59(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true) 
					}; 

			executeTestCase(59, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase60(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true) 
					}; 

			executeTestCase(60, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase61(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100") 
					}; 

			executeTestCase(61, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase62(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true) 
					}; 

			executeTestCase(62, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public SSBN executeTestCase63(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			return executeTestCase(63, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
		
		return null; 
	}
	
	public void executeTestCase64(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
					}; 

			executeTestCase(64, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase65(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(65, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase66(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
					}; 

			executeTestCase(66, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	public void executeTestCase67(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true)					}; 

			executeTestCase(67, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase68(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)					}; 

			executeTestCase(68, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	public void executeTestCase69(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)					}; 

			executeTestCase(69, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	public void executeTestCase70(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")					}; 

			executeTestCase(70, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase71(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true)					}; 

			executeTestCase(71, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	public void executeTestCase72(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
					}; 

			executeTestCase(72, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	public void executeTestCase73(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(73, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	public void executeTestCase74(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
					}; 
			
			executeTestCase(74, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase75(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)					}; 

			executeTestCase(75, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase76(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)					}; 

			executeTestCase(76, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase77(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")					}; 

			executeTestCase(77, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase78(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")	}; 

			executeTestCase(78, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase79(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)	}; 

			executeTestCase(79, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase80(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true)	}; 

			executeTestCase(80, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase81(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)}; 

			executeTestCase(81, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase82(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")	}; 

			executeTestCase(82, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	private void executeTestCase83(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true)}; 

			executeTestCase(83, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	private void executeTestCase84(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true)}; 

			executeTestCase(84, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase85(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)}; 

			executeTestCase(85, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase86(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)}; 

			executeTestCase(86, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase87(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)}; 

			executeTestCase(87, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase88(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createFinding(RV_AGE+ "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)}; 

			executeTestCase(88, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase89(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)}; 

			executeTestCase(89, RV_PlateletCount,  RV_PlateletCount + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase90(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)}; 

			executeTestCase(90, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase91(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)}; 

			executeTestCase(91, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase92(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)}; 

			executeTestCase(92, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase93(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)}; 

			executeTestCase(93, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase94(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)}; 

			executeTestCase(94, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase95(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true), 
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)		
			}; 

			executeTestCase(95, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase96(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)
			}; 

			executeTestCase(96, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase97(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)
			}; 

			executeTestCase(97, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase98(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)}; 

			executeTestCase(98, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase99(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)}; 

			executeTestCase(99, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase100(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(100, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase101(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
			}; 

			executeTestCase(101, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase102(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
					}; 

			executeTestCase(102, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase103(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)
					}; 

			executeTestCase(103, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase104(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
					}; 

			executeTestCase(104, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase105(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
					}; 

			executeTestCase(105, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase106(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, "a165_50") 
					}; 

			executeTestCase(106, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase107(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true) 
					}; 

			executeTestCase(107, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase108(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true) 
					}; 

			executeTestCase(108, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase109(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, "a165_50"), 
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true)
					}; 

			executeTestCase(109, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase110(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, "a165_50") , 
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true) 
					}; 

			executeTestCase(110, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase111(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true), 
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true)
					}; 

			executeTestCase(111, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase112(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true), 
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true), 
					createFinding(RV_BloodUrea+ "_MFrag", RV_BloodUrea, "a165_50")
					}; 

			executeTestCase(112, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase113(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true) 
					}; 

			executeTestCase(113, RV_JointsSwelling,  RV_JointsSwelling + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase114(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(114, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase115(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(115, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase116(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(116, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase117(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(117, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase118(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(118, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase119(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(119, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase120(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(120, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase121(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCase(121, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase122(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(122, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase123(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(123, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase124(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(124, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase125(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(125, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase126(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"), 
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(126, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase127(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"), 
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(127, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase128(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(128, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	private void executeTestCase129(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(129, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase130(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(130, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase131(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(131, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase132(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(132, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase133(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(133, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase134(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(134, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase135(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling,true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(135, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase136(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling,true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain ,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(136, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase137(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling,true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain ,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(137, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase138(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling,true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain ,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(138, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase139(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling,true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain ,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCase(139, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	
}
