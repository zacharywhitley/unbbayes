import java.io.File;
import java.io.FileWriter;
import java.io.IOException; 

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
 
public class DMP_with_UBN_PROGNOS_Discrete_Test {
 
	String file = "";
	ProbabilisticNetwork pn = null;
	protected DMP_with_UBN_PROGNOS_Discrete_Test(String s) {
		//Debug.setDebug(false);
		Debug.setDebug(false);
		EDB.This().printSet(true);
		file = s;
		pn = loadNetworkFile(new File(".\\examples\\"+file+".xml"));
	}

	public void saveNetworkFile(File file, ProbabilisticNetwork pn){
	    XMLBIFIO netIO = new XMLBIFIO(); 
		
		 
		try {
			XMLBIFIO.saveXML(new FileWriter(file), pn);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
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
		System.out.println("node size:	"+pn.getNodeCount());
		
		System.out.println("****Edge list****");
		for (Edge e : pn.getEdges()) {
			System.out.println(e.toString());
		}
		   
		System.out.println(" ");
	}
		
	long time_dmp = 0; 
	public void test_DMP(){
		System.out.println("*** DMP ***"); 
		long time = System.nanoTime(); 
		CPSCompilerMain.This().InitCompiler();
		UDB2SDB uToS = new UDB2SDB();  
		uToS.convert(pn);  
		CPSCompilerMain.This().compile(//"defineEvidence( isShipOfInterest__ship3, true );"+
										//"defineEvidence( Z, 3 );"+
										//"defineEvidence( A, a1 );"+
									   "defineEvidence(hasWeaponVisible_ship1, True);"+
									   "defineEvidence(isJettisoningCargo_ship1, True);"+ 
									   "run(DMP);");  
		time_dmp = (System.nanoTime()- time);
		System.out.println("Total DMP Time:	" + time_dmp + "	nano sec"); 
		
		EDBUnit node = EDB.This().get("ROOT.ENGINES.DMP.NODES.isShipOfInterest_ship1"); 
		EDBUnit bel = node.get("BEL");
		bel.print("isShipOfInterest_ship1");
		
		System.out.println(" "); 
	}
	
	long time_jt = 0; 
	public void test_JT(){
		System.out.println("*** JT ***");
		
		pn.setCreateLog(false);
		long time = System.nanoTime(); 
		
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
		algorithm.setNetwork(pn);
		algorithm.run(); 
		
		// insert evidence (finding) 
		ProbabilisticNode findingNode = (ProbabilisticNode)pn.getNode("isJettisoningCargo_ship1"); 
		findingNode.addFinding(0);
		findingNode = (ProbabilisticNode)pn.getNode("hasWeaponVisible_ship1"); 
		findingNode.addFinding(0);
		 
		// propagate evidence 
		try { 
        	pn.updateEvidences(); 
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());               	
        } 
		 
		time_jt = (System.nanoTime()- time);
		System.out.println("Total JT Time:	" + time_jt + "	nano sec");
		
		ProbabilisticNode resultNode = (ProbabilisticNode)pn.getNode("isShipOfInterest_ship1");
 		System.out.println(resultNode.getDescription());
		for (int i = 0; i < resultNode.getStatesSize(); i++) {
			System.out.println(resultNode.getStateAt(i) + " : " + ((ProbabilisticNode)resultNode).getMarginalAt(i));
		}
		
		System.out.println(" ");
	}
		
	/**
	 * It just delegates to UnBBayes' main
	 * @param args
	 */
	public static void main(String[] args) { 
		String s = "simulation_v12_continuous_discretized2";
	 	DMP_with_UBN_PROGNOS_Discrete_Test u = new DMP_with_UBN_PROGNOS_Discrete_Test(s);
	 
		u.NetName(); 
		u.test_JT(); 
	}
}
