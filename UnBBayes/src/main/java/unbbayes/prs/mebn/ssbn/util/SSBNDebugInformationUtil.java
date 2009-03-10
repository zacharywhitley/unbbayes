package unbbayes.prs.mebn.ssbn.util;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import unbbayes.io.LogManager;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Edge;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.Debug;

public class SSBNDebugInformationUtil {

	/*
	 * This debug method print the informations about the network build by
	 * the SSBN algorithm and save a file xmlbif with the bayesian network built. 
	 * 
	 * @param querynode
	 */
	public static void printNetworkInformation(LogManager logManager, SSBNNode queryNode, 
			 long stepCount, String queryName) {
		
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
		
		System.out.println("Saved: " + file.getAbsolutePath());
		
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
	public static void printParents(LogManager logManager, SSBNNode node, int nivel){
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
	
	public static void printNodeStructureBeforeCPT(SSBNNode ssbnNode){
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
	
	
}
