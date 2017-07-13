import java.io.File;
import java.io.FileWriter;
import java.io.IOException; 
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import edu.gmu.seor.prognos.unbbayesplugin.cps.CPSCompilerMain; 
import edu.gmu.seor.prognos.unbbayesplugin.cps.converter.UDB2SDB;
import edu.gmu.seor.prognos.unbbayesplugin.cps.datastructure.EDB;
import edu.gmu.seor.prognos.unbbayesplugin.cps.datastructure.EDBUnit;
 
import unbbayes.io.exception.LoadException;
import unbbayes.io.xmlbif.version7.XMLBIFIO;
import unbbayes.prs.Edge; 
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;  
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
 
public class DMP_with_UBN_PROGNOS_Continuous_Test {
 
	String file = "";
	ProbabilisticNetwork continuousNet = null;
	ProbabilisticNetwork discretizedNet = null;
	protected DMP_with_UBN_PROGNOS_Continuous_Test() {
		//Debug.setDebug(false);
		Debug.setDebug(false);
		EDB.This().printSet(true); 
	}
  
	public ProbabilisticNetwork loadNetworkFile(File file){
		ProbabilisticNetwork pn = new ProbabilisticNetwork("");
	    XMLBIFIO netIO = new XMLBIFIO(); 
		  
	    try {
			netIO.loadXML(file, pn) ;
		} catch (LoadException e) { 
			e.printStackTrace();
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (JAXBException e) { 
			e.printStackTrace();
		}
		  
		return pn; 
	}
	
	public void printRatio(){
		double d = (double)time_dmp/(double)time_jt;
		System.out.println("Total DMP Time / Total JT Time:	" + (d)  );
		System.out.println(" ");
	}
	
	public void NetName(){ 
		System.out.println("Net Name:	" + file);  
		System.out.println(" ");
	}
	
	public void NetInfo(){ 
		System.out.println("Net Information of	"+file);
		System.out.println("node size:	"+continuousNet.getNodeCount());
		
		System.out.println("****Edge list****");
		for (Edge e : continuousNet.getEdges()) {
			System.out.println(e.toString());
		}
		   
		System.out.println(" ");
	}
		
	long time_dmp = 0; 
	public void test_DMP(Map<String, String> mapEvindeces){
		System.out.println("*** DMP ***"); 
		long time = System.nanoTime(); 
		
		CPSCompilerMain.This().InitCompiler();
		UDB2SDB uToS = new UDB2SDB();  
		uToS.convert(continuousNet); 
		    
		String ev = "";
		for (String key : mapEvindeces.keySet()){
			String e = mapEvindeces.get(key);
			ev += "defineEvidence("+key+", " + e +");  ";
		}
				
		CPSCompilerMain.This().compile( ev + "run(DMP);");  
		time_dmp = (System.nanoTime()- time);
		System.out.println("Total DMP Time:	" + time_dmp + "	nano sec"); 
		
		EDBUnit node = EDB.This().get("ROOT.ENGINES.DMP.NODES.isShipOfInterest_ship1"); 
		EDBUnit bel = node.get("BEL");
		bel.print("isShipOfInterest_ship1");
		
		System.out.println(" "); 
	}
	
	long time_jt = 0; 
	public void test_JT(Map<String, String> mapEvindeces){
		System.out.println("*** JT ***"); 
		
		discretizedNet.setCreateLog(false);
		long time = System.nanoTime(); 
		
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
		algorithm.setNetwork(discretizedNet);
		algorithm.run();  
		
		for (String key : mapEvindeces.keySet()){
			String e = mapEvindeces.get(key);
			ProbabilisticNode findingNode = (ProbabilisticNode)discretizedNet.getNode(key);
			if (e.equalsIgnoreCase("True"))
				findingNode.addFinding(0);
			else
				findingNode.addFinding(1);
			 
			if (e.equalsIgnoreCase("85.47197"))
				findingNode.addFinding(4);
			else if (e.equalsIgnoreCase("11.252131"))
				findingNode.addFinding(0);  

			if (e.equalsIgnoreCase("54.330338"))
				findingNode.addFinding(4);
			else if (e.equalsIgnoreCase("4.440531"))
				findingNode.addFinding(0); 
			 
			if (e.equalsIgnoreCase("37.68292"))
				findingNode.addFinding(4);
			else if (e.equalsIgnoreCase("1.1423578"))
				findingNode.addFinding(0); 
		}
		 
		// propagate evidence 
		try { 
			discretizedNet.updateEvidences(); 
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());               	
        } 
		 
		time_jt = (System.nanoTime()- time);
		System.out.println("Total JT Time:	" + time_jt + "	nano sec");
		
		ProbabilisticNode resultNode = (ProbabilisticNode)discretizedNet.getNode("isShipOfInterest_ship1");
 		System.out.println(resultNode.getDescription());
		for (int i = 0; i < resultNode.getStatesSize(); i++) {
			System.out.println(resultNode.getStateAt(i) + " : " + ((ProbabilisticNode)resultNode).getMarginalAt(i));
		}
		
		System.out.println(" ");
	}
	
	public void test_SOI(){
		continuousNet = loadNetworkFile(new File(".\\examples\\simulation_v12_continuous_2.xml"));
		discretizedNet = loadNetworkFile(new File(".\\examples\\simulation_v12_continuous_discretized2.xml"));
		
		Map<String, String> mapEvindeces = new HashMap<String, String>();
		mapEvindeces.put("hasResponsiveAIS_ship1", "False");
		mapEvindeces.put("hasWeaponVisible_ship1", "True");
		mapEvindeces.put("isJettisoningCargo_ship1", "False");
		mapEvindeces.put("hasResponsiveRadio_ship1", "False");
		mapEvindeces.put("isCrewVisible_ship1", "False");
		mapEvindeces.put("propellerTurnCount", "85.47197");
		mapEvindeces.put("shipRCSchange", "54.330338");
		mapEvindeces.put("cavitation", "37.68292"); 
		   
		NetName();
		test_DMP(mapEvindeces);
		test_JT(mapEvindeces);
		printRatio();
	} 
	
	public void test_NOT_SOI(){
		continuousNet = loadNetworkFile(new File(".\\examples\\simulation_v12_continuous_2.xml"));
		discretizedNet = loadNetworkFile(new File(".\\examples\\simulation_v12_continuous_discretized2.xml"));
		
		Map<String, String> mapEvindeces = new HashMap<String, String>();
		mapEvindeces.put("hasResponsiveAIS_ship1", "True");
		mapEvindeces.put("hasWeaponVisible_ship1", "False");
		mapEvindeces.put("isJettisoningCargo_ship1", "True");
		mapEvindeces.put("hasResponsiveRadio_ship1", "True");
		mapEvindeces.put("isCrewVisible_ship1", "False");
		mapEvindeces.put("propellerTurnCount", "11.252131");
		mapEvindeces.put("shipRCSchange", "4.440531");
		mapEvindeces.put("cavitation", "1.1423578");  
		   
		NetName();
		test_DMP(mapEvindeces);
		test_JT(mapEvindeces);
		printRatio();
	} 
	
	/**
	 * It just delegates to UnBBayes' main
	 * @param args
	 */
	public static void main(String[] args) { 
		DMP_with_UBN_PROGNOS_Continuous_Test u = new DMP_with_UBN_PROGNOS_Continuous_Test();
		u.test_NOT_SOI(); 
	}

}
