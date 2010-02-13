package unbbayes.prs.mebn.ontology;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import unbbayes.io.XMLBIFIO;
import unbbayes.io.log.ILogManager;
import unbbayes.io.log.TextLogManager;
import unbbayes.prs.bn.ProbabilisticNetwork;
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
	
	protected ILogManager logManager; 
	private ISSBNGenerator ssbnGenerator; 
	
	public static boolean compileSSBNGenerated = true; 
	public static boolean printTable = true; 
	
	public TestSet(ISSBNGenerator ssbnGenerator, ILogManager _logManager){
		logManager = _logManager; 
		this.ssbnGenerator = ssbnGenerator; 
	}
	
	public TestSet(ISSBNGenerator ssbnGenerator){
		this(ssbnGenerator, new TextLogManager()); 
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

	private void printTreeVariableTable(ProbabilisticNode query) {
		TreeVariable treeVariable = query; 
		
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
//			saveNetworkFile(file, ssbn.getProbabilisticNetwork()); 
			
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
			if(query.getProbabilisticNode() != null){
				printTreeVariableTable(query.getProbabilisticNode());
			}
			else{
				if(query.getSSBNNode() != null){
					printTreeVariableTable(query.getSSBNNode().getProbNode()); 
				}
			}
			printTestFoot();
		}
		
		return ssbn; 
	
	}
	
	public static void saveNetworkFile(File file, ProbabilisticNetwork pn){
	    XMLBIFIO netIO = new XMLBIFIO(); 
		
		try {
			netIO.save(file, pn);
		} catch (IOException e) {
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
