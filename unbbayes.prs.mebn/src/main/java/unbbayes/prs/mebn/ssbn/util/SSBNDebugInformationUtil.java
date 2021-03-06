package unbbayes.prs.mebn.ssbn.util;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import unbbayes.io.XMLBIFIO;
import unbbayes.io.log.ILogManager;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;

/**
 * TODO stop relying on static methods, since it
 * will give us a major hedache at distributed or client/server model.
 *
 */
public class SSBNDebugInformationUtil {
	
	private static Boolean enabled = true;
	
	static {
		try {
			// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
    		if (!Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
    				SSBNDebugInformationUtil.class.getCanonicalName()+".enabled").toString())) {
        		enabled = false;
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void printAndSaveCurrentNetwork(SSBN ssbn) {
		if (!enabled) {
			return;
		}
		// the following line was migrated to LaskeySSBNGenerator, because this is not related to debug info.
//		PositionAdjustmentUtils.adjustPositionProbabilisticNetwork(ssbn.getProbabilisticNetwork()); 
		if (ssbn.getLogManager() != null) {
			SSBNDebugInformationUtil.printNetworkInformation(ssbn.getLogManager(), ssbn); 
		}
	}
	
	/**
	 * This debug method print the informations about the network build by
	 * the SSBN algorithm and save a file xmlbif with the bayesian network built. 
	 * 
	 * @param querynode
	 */
	public static void printNetworkInformation(ILogManager logManager, SSBN ssbn) {
		if (!enabled) {
			return;
		}
		
		String netName = "";
		
		
		//The SSBN information will be save at the directory examples/MEBN/SSBN. 
		//If this past don't exists, will be created (the examples pastes already 
		//have to exists. 
		String nameDirectory = "examples" + File.separator + "MEBN" + File.separator + 
		"SSBN";
		
		File directory = new File(nameDirectory); 
		if(!directory.exists()){
			directory.mkdir(); 
		}
		
		for(Query query: ssbn.getQueryList()){
			netName+= query.getResidentNode(); 
			for(OVInstance ov: query.getArguments()){
				netName+= "_" + ov.getEntity().getInstanceName(); 
			}
		}
		
		if (ssbn.getProbabilisticNetwork() != null) {
			ssbn.getProbabilisticNetwork().setName(netName);
		}
		
		nameDirectory = "examples" + File.separator + "MEBN" + File.separator + 
		"SSBN" + File.separator + netName;
		
		directory = new File(nameDirectory); 
		if(!directory.exists()){
			directory.mkdir(); 
		}
		
		File file = new File(nameDirectory + File.separator +  netName  + ".xml");
		
		Debug.println("Saved: " + file.getAbsolutePath());
		
		logManager.appendln("\n"); 
		logManager.appendln("  |-------------------------------------------------------");
		logManager.appendln("  |Network: ");
		logManager.appendln("  |" + netName);
		logManager.appendln("  | (" + file.getAbsolutePath() + ")");
		
		logManager.appendln("  |\n  |Current node's branch: ");
		for(Query query: ssbn.getQueryList()){
			logManager.appendln("  |" + query);
			printParents(logManager, query.getSSBNNode(), 0); 
		}

		
		logManager.appendln("  |\n  |Edges:");
		if (ssbn.getProbabilisticNetwork() != null) {
			for(Edge edge: ssbn.getProbabilisticNetwork().getEdges()){
				logManager.appendln("  |" + edge.toString());
			}
		}
		
		logManager.appendln("  |\n  |Nodes:");
		if (ssbn.getProbabilisticNetwork() != null) {
			for(int i = 0; i < ssbn.getProbabilisticNetwork().getNodes().size(); i++){
				logManager.appendln("  |" + ssbn.getProbabilisticNetwork().getNodeAt(i).toString());
			}
		}
		logManager.appendln("  |-------------------------------------------------------");
		logManager.appendln("\n"); 
		
	    XMLBIFIO netIO = new XMLBIFIO(); 
		
		try {
			netIO.save(file, ssbn.getProbabilisticNetwork());
		} catch (Exception e) {
//			e.printStackTrace();
			Debug.println(SSBNDebugInformationUtil.class, "", e);
		} 
		
		try {
			logManager.writeToDisk(nameDirectory + File.separator +  netName + ".log", false);
		} catch (Exception e) {
//			e.printStackTrace();
			Debug.println(SSBNDebugInformationUtil.class, "", e);
		}
	}
	
	/**
	 * This debug method print the informations about the network build by
	 * the SSBN algorithm and save a file xmlbif with the bayesian network built. 
	 * 
	 * @param querynode
	 */
	public static void printNetworkInformation(ILogManager logManager, SSBNNode queryNode, 
			 long stepCount, String queryName) {
		if (!enabled) {
			return;
		}
		
		//TODO Use a decimal format instead
		String stepCountFormated = "";
		if (stepCount < 10L) {
			stepCountFormated = "00" + stepCount;
		} else if (stepCount < 100L) {
			stepCountFormated = "0" + stepCount;
		} else {
			stepCountFormated = "" + stepCount;
		}
		String netName = queryName + " - Step " + stepCountFormated;
		queryNode.getProbabilisticNetwork().setName(netName);
		
		String nameDirectory = "examples" + File.separator + "MEBN" + File.separator + 
		"SSBN" + File.separator + queryName;
		
		File directory = new File(nameDirectory); 
		if(!directory.exists()){
			directory.mkdir(); 
		}
		
		File file = new File(nameDirectory + File.separator +  netName  + ".xml");
		
		Debug.println("Saved: " + file.getAbsolutePath());
		
		logManager.appendln("\n"); 
		logManager.appendln("  |-------------------------------------------------------");
		logManager.appendln("  |Network: ");
		logManager.appendln("  |" + netName);
		logManager.appendln("  | (" + file.getAbsolutePath() + ")");
		
		logManager.appendln("  |\n  |Current node's branch: ");
		logManager.appendln("  |" + queryNode.getName());
		printParents(logManager, queryNode, 0); 
		
		logManager.appendln("  |\n  |Edges:");
		for(Edge edge: queryNode.getProbabilisticNetwork().getEdges()){
			logManager.appendln("  |" + edge.toString());
		}
		
		logManager.appendln("  |\n  |Nodes:");
		for(int i = 0; i < queryNode.getProbabilisticNetwork().getNodes().size(); i++){
			logManager.appendln("  |" + queryNode.getProbabilisticNetwork().getNodeAt(i).toString());
		}
		logManager.appendln("  |-------------------------------------------------------");
		logManager.appendln("\n"); 
		
	    XMLBIFIO netIO = new XMLBIFIO(); 
		
//		try {
//			netIO.save(file, queryNode.getProbabilisticNetwork());
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
		
		try {
			logManager.writeToDisk("teste.txt", false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * debug method. 
	 */
	public static void printParents(ILogManager logManager, SSBNNode node, int nivel){
		if (!enabled) {
			return;
		}
		for(SSBNNode parent: node.getParents()){
			for(int i = 0; i <= nivel; i++){
				if (i == 0) {
					logManager.append("  |   ");
				} else {
					logManager.append("   ");
				}
			}
			logManager.appendln(parent.toString());
			printParents(logManager, parent, nivel + 1); 
		}
	}
	
	public static void printParents(ILogManager logManager, SimpleSSBNNode node, int nivel){
		if (!enabled) {
			return;
		}
		for(SimpleSSBNNode parent: node.getParents()){
			for(int i = 0; i <= nivel; i++){
				if (i == 0) {
					logManager.append("  |   ");
				} else {
					logManager.append("   ");
				}
			}
			logManager.appendln(parent.toString());
			printParents(logManager, parent, nivel + 1); 
		}
	}
	
	public static void printNodeStructureBeforeCPT(SSBNNode ssbnNode){
		if (!enabled) {
			return;
		}
		Debug.println("--------------------------------------------------");
		Debug.println("- Node: " + ssbnNode.toString());
		Debug.println("- Parents: ");
		for(SSBNNode parent: ssbnNode.getParents()){
			Debug.println("-    " + parent.getName());
			Debug.println("-            Arguments: ");
			for(OVInstance ovInstance: parent.getArguments()){
				Debug.println("-                " + ovInstance.toString());
			}
		}
		Debug.println("--------------------------------------------------");	
	}
	
	public void printTreeVariableTable(ProbabilisticNode probabilisticNode, ILogManager logManager) {
		if (!enabled) {
			return;
		}
		TreeVariable treeVariable = probabilisticNode; 
		
		int statesSize = treeVariable.getStatesSize();
		
		logManager.appendln("Node = " + treeVariable.getDescription()); 
		
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

	/**
	 * @return the enabled
	 */
	public static boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public static void setEnabled(boolean enabled) {
		SSBNDebugInformationUtil.enabled = enabled;
	}

	
	
}
