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
		executeTestCaseSerieC(3, RV_EnlargedSpleen, RV_EnlargedSpleen + "_DMFrag"); 				
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
		
		System.out.println(maria);
		
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
			RandomVariableFinding finding = createBooleanFinding(RV_EnlargedSpleen + "_DMFrag", RV_EnlargedSpleen, true);
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
					createBooleanFinding(RV_EnlargedSpleen + "_DMFrag", RV_EnlargedSpleen, true)
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
					createBooleanFinding(RV_EnlargedSpleen + "_DMFrag", RV_EnlargedSpleen, true), 
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
					createBooleanFinding(RV_EnlargedSpleen + "_DMFrag", RV_EnlargedSpleen, true), 
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
					createBooleanFinding(RV_EnlargedSpleen + "_DMFrag", RV_EnlargedSpleen, true), 
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
	
	
}
