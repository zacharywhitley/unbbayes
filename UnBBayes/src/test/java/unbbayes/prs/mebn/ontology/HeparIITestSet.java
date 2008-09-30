package unbbayes.prs.mebn.ontology;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import unbbayes.io.LogManager;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
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
import unbbayes.prs.mebn.ssbn.ExplosiveSSBNGenerator;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SituationSpecificBayesianNetwork;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * This is a set of case test developer por Cheol Young Park using the Hepar II 
 * Network. The objective is test the ssbn algorithm for situations that don't 
 * envolve context nodes. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class HeparIITestSet {

	private static final String HEPARII_UBF_FILE = "examples/mebn/HeparII/HeparII_01.ubf";
	private static final String TEST_FILE_NAME = "HeparIITestSet.log"; 
	
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
	
	private static final String RV_HistoryAlcAbuse_MFrag = ""; 
	private static final String RV_HepatoxicMeds_MFrag = ""; 
	
	
	private static final String OV_P = "p"; 
	
	
	private ISSBNGenerator ssbnGenerator; 
	private MultiEntityBayesianNetwork mebn;
	private KnowledgeBase kb;
	
	private LogManager logManager; 
	
	private NumberFormat nf;
	
	public HeparIITestSet(){
		
		//Initialization
		ssbnGenerator = new ExplosiveSSBNGenerator(); 
		logManager = new LogManager(); 
		kb = PowerLoomKB.getNewInstanceKB(); 
		
		nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		//Loading the network
		UbfIO io = UbfIO.getInstance(); 
		try {
			mebn = io.loadMebn(new File(HEPARII_UBF_FILE));
		} catch (Exception e) {
			e.printStackTrace();
			logManager.appendln(e.toString());
			finishLog(); 
			System.exit(1); 
		}
		
		//Executing the test
		createGenerativeKnowledgeBase(mebn); 
		
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
		
		
		
		finishLog(); 
	}
	
	public static void main(String[] args){
		HeparIITestSet heparIITestSet = new HeparIITestSet(); 
	}
	
	private void printTestFoot() {
		logManager.appendln("-----------------------------------------------"); 
		logManager.appendln("");
	}

	private void printTestHeader(int i, String NodeName) {
		logManager.appendln("-----------------------------------------------"); 
		logManager.appendln("Test" + i + ":" + NodeName);
	}

	private void printTreeVariableTable(Query query) {
		TreeVariable treeVariable = query.getQueryNode().getProbNode(); 
		
		int statesSize = treeVariable.getStatesSize();
		for (int j = 0; j < statesSize; j++) {
			String label;
			label = treeVariable.getStateAt(j)+ ": "
			+ nf.format(treeVariable.getMarginalAt(j) * 100.0);
			logManager.appendln(label); 
		}
	}
	
	private void finishLog(){
		try {
			logManager.writeToDisk("HeparIITestSet.log", false);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private void executeQuery(Query query) {
		try {
			SituationSpecificBayesianNetwork ssbn = ssbnGenerator.generateSSBN(query);
			ssbn.compileAndInitializeSSBN();
		} catch (Exception e) {
			e.printStackTrace();
			logManager.appendln(e.toString());
		}
	}
	
	private Query createGenericQueryNode(MultiEntityBayesianNetwork mebn,
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
			logManager.appendln(e1.toString());
		}
		
		Query query = new Query(kb, queryNode, mebn); 
		query.setMebn(mebn); 
		
		return query;				
	}
	
	private KnowledgeBase createGenerativeKnowledgeBase(
			MultiEntityBayesianNetwork mebn) {
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
	
	private void executeQueryAndPrintResults(Query query) {
		executeQuery(query);
		printTreeVariableTable(query);
		printTestFoot();
	}
	
	private void executeTestCaseSerieC(int index, String nameResidentNode, String nameMFrag){
		printTestHeader(index, nameResidentNode);
		
		Query query = createGenericQueryNode(mebn, nameMFrag, nameResidentNode, 
				new String[]{"p"}, 
				new String[]{"maria"} ); 

		executeQueryAndPrintResults(query); 				
	}

	private void executeTestCase1(){
		executeTestCaseSerieC(1, RV_AST, RV_AST + "_MFrag"); 				
	}

	private void executeTestCase2(){
		executeTestCaseSerieC(2, RV_Fatigue, RV_Fatigue + "_MFrag"); 					
	}
	
	private void executeTestCase3(){
		executeTestCaseSerieC(3, RV_EnlargedSpleen, RV_EnlargedSpleen + "_MFrag"); 				
	}
	
	private void executeTestCase4(){
		executeTestCaseSerieC(4, RV_ALT, RV_ALT + "_MFrag"); 				
	}
	
	private void executeTestCase5(){
		executeTestCaseSerieC(5, RV_HaemorrhagieDiathesis, RV_HaemorrhagieDiathesis + "_MFrag"); 			
	}
	
	private void executeTestCase6(){
		executeTestCaseSerieC(6, RV_INR, RV_INR + "_MFrag"); 					
	}

	private void executeTestCase7(){
		executeTestCaseSerieC(7, RV_PlateletCount, RV_PlateletCount + "_MFrag"); 				
	}
	
	private void executeTestCase8(){
		executeTestCaseSerieC(8, RV_Yellowingoftheskin, RV_Yellowingoftheskin + "_MFrag"); 				
	}

	private void executeTestCase9(){
		executeTestCaseSerieC(9, RV_Itching, RV_Itching + "_MFrag"); 					
	}
	
	private void executeTestCase10(){
		executeTestCaseSerieC(10, RV_Jaundice, RV_Jaundice + "_MFrag"); 				
	}
	
	private void executeTestCase11(){
		executeTestCaseSerieC(11, RV_TotalBilirubin, RV_TotalBilirubin + "_MFrag"); 				
	}
	
	private void executeTestCase12(){
		executeTestCaseSerieC(12, RV_BloodUrea, RV_BloodUrea + "_MFrag"); 			
	}
	
	private void executeTestCase13(){
		executeTestCaseSerieC(13, RV_IncreasedLiverDensity, RV_IncreasedLiverDensity + "_MFrag"); 					
	}

	private void executeTestCase14(){
		executeTestCaseSerieC(14, RV_ImpairedConsciousness, RV_ImpairedConsciousness + "_MFrag"); 				
	}
	
	private void executeTestCase15(){
		executeTestCaseSerieC(15, RV_HepaticEncephalopathy, RV_HepaticEncephalopathy + "_MFrag"); 			
	}
	
	private void executeTestCase16(){
		executeTestCaseSerieC(16, RV_MusculoSkeletalPain, RV_MusculoSkeletalPain + "_MFrag"); 					
	}

	private void executeTestCase17(){
		executeTestCaseSerieC(17, RV_JointsSwelling, RV_JointsSwelling + "_MFrag"); 				
	}	
	
	private void executeTestCaseSerieD(int index, String nameResidentNode, String nameMFrag, 
			RandomVariableFinding[] findingList){
		
		printTestHeader(index, nameResidentNode);

		kb.clearKnowledgeBase(); 
		kb = createGenerativeKnowledgeBase(mebn); 

		for(RandomVariableFinding finding: findingList){
			kb.insertRandomVariableFinding(finding); 
		}
		
		Query query = createGenericQueryNode(mebn, nameMFrag, nameResidentNode, 
				new String[]{"p"}, 
				new String[]{"maria"} ); 

		executeQueryAndPrintResults(query); 				
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
		
		System.out.println(maria);
		
		BooleanStateEntity entity = null; 
		
		if(state){
		entity = mebn.getBooleanStatesEntityContainer().getTrueStateEntity(); 
		}else{
			entity = mebn.getBooleanStatesEntityContainer().getFalseStateEntity(); 	
		}
		
		return  new RandomVariableFinding(residentNode, 
				new ObjectEntityInstance[]{maria}, entity, mebn);
	
	}
	
	private void executeTestCase18(){
		
		try{
			RandomVariableFinding finding = createFinding(RV_AST + "_MFrag", RV_AST, "a700_400");
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCaseSerieD(18, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	private void executeTestCase19(){
		
		try{
			RandomVariableFinding finding = createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true);
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCaseSerieD(19, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	private void executeTestCase20(){
		
		try{
			RandomVariableFinding finding = createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true);
			RandomVariableFinding findings[] = new RandomVariableFinding[]{finding}; 

			executeTestCaseSerieD(20, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(21, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(22, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(23, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	private void executeTestCase24(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true)
			}; 

			executeTestCaseSerieD(24, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	private void executeTestCase25(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true)
			}; 

			executeTestCaseSerieD(25, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	private void executeTestCase26(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true), 
					createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200")
			}; 

			executeTestCaseSerieD(26, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	
	private void executeTestCase27(){
		
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true), 
					createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCaseSerieD(27, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}

	}
	
	
	private void executeTestCase28(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AST + "_MFrag", RV_AST, "a700_400"), 
					createBooleanFinding(RV_Fatigue + "_MFrag", RV_Fatigue, true), 
					createBooleanFinding(RV_EnlargedSpleen + "_MFrag", RV_EnlargedSpleen, true), 
					createFinding(RV_ALT + "_MFrag", RV_ALT, "a850_200"), 
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCaseSerieD(28, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase29(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCaseSerieD(29, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase30(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCaseSerieD(30, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase31(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20")
			}; 

			executeTestCaseSerieD(31, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase32(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)
			}; 

			executeTestCaseSerieD(32, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase33(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)
			}; 

			executeTestCaseSerieD(33, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase34(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
			}; 

			executeTestCaseSerieD(34, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase35(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20"),
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCaseSerieD(35, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase36(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true),
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCaseSerieD(36, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase37(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true),
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true),
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110")
			}; 

			executeTestCaseSerieD(37, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase38(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_PlateletCount + "_MFrag", RV_PlateletCount, "a597_300")
			}; 

			executeTestCaseSerieD(38, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	private void executeTestCase39(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCaseSerieD(39, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	private void executeTestCase40(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_TotalBilirubin + "_MFrag", RV_TotalBilirubin, "a88_20")
			}; 

			executeTestCaseSerieD(40, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	

	private void executeTestCase41(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)
			}; 

			executeTestCaseSerieD(41, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase42(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)
			}; 

			executeTestCaseSerieD(42, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase43(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true)
			}; 

			executeTestCaseSerieD(43, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase44(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)
			}; 

			executeTestCaseSerieD(44, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase45(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, "a165_50")
			}; 

			executeTestCaseSerieD(45, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase46(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true)
			}; 

			executeTestCaseSerieD(46, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase47(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true)
			}; 

			executeTestCaseSerieD(47, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase48(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true)
			}; 

			executeTestCaseSerieD(48, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase49(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true)
			}; 

			executeTestCaseSerieD(49, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase50(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true)
			}; 

			executeTestCaseSerieD(50, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase51(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true)
			}; 

			executeTestCaseSerieD(51, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase52(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true)		
			}; 

			executeTestCaseSerieD(52, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase53(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_HaemorrhagieDiathesis + "_MFrag", RV_HaemorrhagieDiathesis, true), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_Itching + "_MFrag", RV_Itching, true), 
					createBooleanFinding(RV_Jaundice + "_MFrag", RV_Jaundice, true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)

					}; 

			executeTestCaseSerieD(53, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase54(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)

					}; 

			executeTestCaseSerieD(54, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase55(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_INR + "_MFrag", RV_INR, "a200_110"), 
					createBooleanFinding(RV_Yellowingoftheskin + "_MFrag", RV_Yellowingoftheskin, true), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain, true), 
					createBooleanFinding(RV_JointsSwelling + "_MFrag", RV_JointsSwelling, true), 
					createBooleanFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, true)

					}; 

			executeTestCaseSerieD(55, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase56(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true) 
					}; 

			executeTestCaseSerieD(56, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase57(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true) 
					}; 

			executeTestCaseSerieD(57, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase58(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCaseSerieD(58, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase59(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true) 
					}; 

			executeTestCaseSerieD(59, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase60(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true) 
					}; 

			executeTestCaseSerieD(60, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase61(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100") 
					}; 

			executeTestCaseSerieD(61, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase62(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true) 
					}; 

			executeTestCaseSerieD(62, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase63(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCaseSerieD(63, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase64(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true), 
					createBooleanFinding(RV_HepatoxicMeds_MFrag, RV_HepatoxicMeds, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_Sex, "65_100")
					}; 

			executeTestCaseSerieD(64, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase65(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCaseSerieD(65, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase66(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_ToxicHepatitis + "_MFrag", RV_ToxicHepatitis, true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true), 
					createFinding(RV_AGE + "_MFrag", RV_Sex, "age65_100")
					}; 

			executeTestCaseSerieD(66, RV_INR,  RV_INR + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	private void executeTestCase67(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true)					}; 

			executeTestCaseSerieD(67, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase68(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true)					}; 

			executeTestCaseSerieD(68, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	private void executeTestCase69(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)					}; 

			executeTestCaseSerieD(69, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}

	private void executeTestCase70(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")					}; 

			executeTestCaseSerieD(70, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase71(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true)					}; 

			executeTestCaseSerieD(71, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}	
	
	private void executeTestCase72(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_FunctionalHyperbilirubinemia + "_MFrag", RV_FunctionalHyperbilirubinemia, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
					}; 

			executeTestCaseSerieD(72, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase73(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createFinding(RV_Sex + "_MFrag", RV_Sex, "65_100")
					}; 

			executeTestCaseSerieD(73, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase74(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_PBC + "_MFrag", RV_PBC, true), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100")
					}; 
			
			executeTestCaseSerieD(74, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(75, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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

			executeTestCaseSerieD(76, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(77, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(78, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(79, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
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

			executeTestCaseSerieD(80, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	private void executeTestCase81(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)	}; 

			executeTestCaseSerieD(81, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
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

			executeTestCaseSerieD(82, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
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

			executeTestCaseSerieD(83, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
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

			executeTestCaseSerieD(84, RV_MusculoSkeletalPain,  RV_MusculoSkeletalPain + "_MFrag", findings); 
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

			executeTestCaseSerieD(85, RV_INR,  RV_INR + "_MFrag", findings); 
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

			executeTestCaseSerieD(86, RV_INR,  RV_INR + "_MFrag", findings); 
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

			executeTestCaseSerieD(87, RV_INR,  RV_INR + "_MFrag", findings); 
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

			executeTestCaseSerieD(88, RV_INR,  RV_INR + "_MFrag", findings); 
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

			executeTestCaseSerieD(89, RV_PlateletCount,  RV_PlateletCount + "_MFrag", findings); 
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

			executeTestCaseSerieD(90, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(91, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(92, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(93, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(94, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(95, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(96, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(97, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(98, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(99, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(100, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(101, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(102, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(103, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(104, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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

			executeTestCaseSerieD(105, RV_TotalBilirubin,  RV_TotalBilirubin + "_MFrag", findings); 
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
					createBooleanFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, true) 
					}; 

			executeTestCaseSerieD(106, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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

			executeTestCaseSerieD(107, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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

			executeTestCaseSerieD(108, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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
					createBooleanFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, true) , 
					createBooleanFinding(RV_IncreasedLiverDensity + "_MFrag", RV_IncreasedLiverDensity, true)
					}; 

			executeTestCaseSerieD(109, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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
					createBooleanFinding(RV_BloodUrea + "_MFrag", RV_BloodUrea, true) , 
					createBooleanFinding(RV_ImpairedConsciousness + "_MFrag", RV_ImpairedConsciousness, true) 
					}; 

			executeTestCaseSerieD(110, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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

			executeTestCaseSerieD(111, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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

			executeTestCaseSerieD(112, RV_HepaticEncephalopathy,  RV_HepaticEncephalopathy + "_MFrag", findings); 
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

			executeTestCaseSerieD(113, RV_JointsSwelling,  RV_JointsSwelling + "_MFrag", findings); 
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

			executeTestCaseSerieD(114, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(115, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(116, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(117, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(118, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(119, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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
					createBooleanFinding(RV_Fatigue, RV_Fatigue, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCaseSerieD(120, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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
					createBooleanFinding(RV_Fatigue, RV_Fatigue, true), 
					createBooleanFinding(RV_HistoryAlcAbuse_MFrag, RV_HistoryAlcAbuse, true) 
					}; 

			executeTestCaseSerieD(121, RV_ToxicHepatitis,  RV_ToxicHepatitis + "_MFrag", findings); 
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

			executeTestCaseSerieD(122, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
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

			executeTestCaseSerieD(123, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
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

			executeTestCaseSerieD(124, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
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

			executeTestCaseSerieD(125, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
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

			executeTestCaseSerieD(126, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
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

			executeTestCaseSerieD(127, RV_FunctionalHyperbilirubinemia,  RV_FunctionalHyperbilirubinemia + "_MFrag", findings); 
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

			executeTestCaseSerieD(128, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(129, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase130(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, "a88_20"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCaseSerieD(130, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	private void executeTestCase131(){
		try{
			RandomVariableFinding findings[] = new RandomVariableFinding[]{
					createFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, "a88_20"), 
					createFinding(RV_AGE + "_MFrag", RV_AGE, "age65_100"), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCaseSerieD(131, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(132, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(133, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(134, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(135, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(136, RV_PBC,  RV_PBC + "_MFrag", findings); 
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

			executeTestCaseSerieD(137, RV_PBC,  RV_PBC + "_MFrag", findings); 
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
					createFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, "a88_20"), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain ,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCaseSerieD(138, RV_PBC,  RV_PBC + "_MFrag", findings); 
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
					createFinding(RV_HepaticEncephalopathy + "_MFrag", RV_HepaticEncephalopathy, "a88_20"), 
					createBooleanFinding(RV_MusculoSkeletalPain + "_MFrag", RV_MusculoSkeletalPain ,true), 
					createBooleanFinding(RV_Sex + "_MFrag", RV_Sex, true)
					}; 

			executeTestCaseSerieD(139, RV_PBC,  RV_PBC + "_MFrag", findings); 
		}
		catch(Exception e){
			e.printStackTrace(); 
			logManager.appendln(e.toString()); 
		}
	}
	
	
	
}
