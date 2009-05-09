package unbbayes.prs.mebn.ontology;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import unbbayes.io.ILogManager;
import unbbayes.io.TextLogManager;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Contains methods for build test set cases for test the ssbn generation process. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */

public abstract class TestSet {

	private static final String PATH_Tests = "examples/mebn/Tests"; 
	
	protected ILogManager logManager; 
	private ISSBNGenerator ssbnGenerator; 
	
	public static boolean compileSSBNGenerated = false; 
	public static boolean printTable = false; 
	
	public TestSet(ISSBNGenerator ssbnGenerator){
		logManager = new TextLogManager(); 
		this.ssbnGenerator = ssbnGenerator; 
		

		File directory = new File(PATH_Tests); 
		if(!directory.exists()){
			directory.mkdir(); 
		}
	}
	
	public abstract void executeTests(); 
	
	protected void finishLog(String nameOfFile){
		try {
			logManager.writeToDisk(nameOfFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private void printTestFoot() {
		logManager.appendln("-----------------------------------------------"); 
		logManager.appendln("");
	}

	protected void printTestHeader(int i, String NodeName) {
		logManager.appendln("-----------------------------------------------"); 
		logManager.appendln("Test" + i + ":" + NodeName);
	}

	private void printTreeVariableTable(Query query) {
		TreeVariable treeVariable = query.getQueryNode().getProbNode(); 
		
		int statesSize = treeVariable.getStatesSize();
		
		logManager.appendln("States = " + statesSize); 
		
		NumberFormat nf =  NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		for (int j = 0; j < statesSize; j++) {
			String label;
			
			
			label = treeVariable.getStateAt(j)+ ": "
			+ nf.format(treeVariable.getMarginalAt(j) * 100.0);
			logManager.appendln(label); 
		}
	}
	

	private SSBN executeQuery(Query query, String nameOfNetworkGenerated) {
		
		try {
			List<Query> listQueries = new ArrayList<Query>(); 
			listQueries.add(query); 
			
			SSBN ssbn = ssbnGenerator.generateSSBN(listQueries, query.getKb());

			File file = new File(nameOfNetworkGenerated); 
			saveNetworkFile(file, query.getQueryNode()); 
			
			if(compileSSBNGenerated){
				ssbn.compileAndInitializeSSBN();
			}
			
			return ssbn; 
		} catch (Exception e) {
			e.printStackTrace();
			logManager.appendln(e.toString());
		}
		
		return null; 
	}
	

	protected SSBN executeQueryAndPrintResults(Query query, String nameOfNetworkGenerated) {
		SSBN ssbn = executeQuery(query, nameOfNetworkGenerated);
		
		if(printTable){
			printTreeVariableTable(query);
			printTestFoot();
		}
		
		return ssbn; 
	}
	
	
	public static void saveNetworkFile(File file, SSBNNode queryNode){
	    XMLBIFIO netIO = new XMLBIFIO(); 
		
		try {
			netIO.save(file, queryNode.getProbabilisticNetwork());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}		
	}
	
	protected Query createGenericQueryNode(MultiEntityBayesianNetwork mebn,
			String mFragName, String residentNodeName, 
			String[] ovVariableNameList, String[] instanceNameList, KnowledgeBase kb){
		
		MFrag mFrag = mebn.getMFragByName(mFragName); 
		ResidentNode residentNode = mFrag.getDomainResidentNodeByName(residentNodeName); 
		SSBNNode queryNode = SSBNNode.getInstance(null,residentNode, new ProbabilisticNode()); 
		
		Query query = new Query(kb, queryNode, mebn); 
		query.setMebn(mebn);
		
		try {
			for(int i = 0; i < ovVariableNameList.length; i++){
				queryNode.addArgument(residentNode.getOrdinaryVariableByName(ovVariableNameList[i]), instanceNameList[i]);	
				OVInstance ovInstance = OVInstance.getInstance( 
						residentNode.getOrdinaryVariableByName(
								ovVariableNameList[i]), 
								LiteralEntityInstance.getInstance(
										instanceNameList[i] , 
										residentNode.getOrdinaryVariableByName(
												ovVariableNameList[i]).getValueType())); 
				query.getArguments().add(ovInstance); 
			}
		} catch (SSBNNodeGeneralException e1) {
			e1.printStackTrace();
			logManager.appendln(e1.toString());
		}

		return query;				
	}
	
	/**
	 * Load a finding module of a plm file
	 * 
	 * @param kb Knowledge base with generative module pre builded
	 * @param nameOfFile Name of the plm file (with extension)
	 * @return True if the finding module was loaded 
	 *         False if error
	 */
	protected boolean loadFindingModule(KnowledgeBase kb, String nameOfFile) {
		try {
			kb.loadModule(new File(nameOfFile), true);
			return true; 
		} catch (Exception e) {
			e.printStackTrace();
			logManager.appendln(e.toString());
			return false; 
		}
	}
}
