import java.io.File;
import java.io.FileWriter;
import java.io.IOException; 
import java.util.List;

import javax.xml.bind.JAXBException;

import edu.gmu.seor.prognos.unbbayesplugin.cps.CPSCompilerMain; 
import edu.gmu.seor.prognos.unbbayesplugin.cps.converter.UDB2SDB;
import edu.gmu.seor.prognos.unbbayesplugin.cps.datastructure.EDB;
 
import unbbayes.io.exception.LoadException;
import unbbayes.io.xmlbif.version7.XMLBIFIO;
import unbbayes.prs.Edge; 
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;  
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
 
public class DMP_with_UBN_Test {
 
	String file = "";
	ProbabilisticNetwork pn = null;
	protected DMP_with_UBN_Test(String s) {
		//Debug.setDebug(false);
		Debug.setDebug(true);
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
	
	public void getLeafNodes(){
		for (Node n : pn.getNodes()){
			if (n.getChildNodes().size() == 0){
				System.out.println(n.getName());
			}
		}
	}
	
	public void test_DMP(){  
		CPSCompilerMain.This().InitCompiler();
		UDB2SDB uToS = new UDB2SDB();  
		uToS.convert(pn); 
		CPSCompilerMain.This().compile("run(DMP);"); 
		// isShipOfInterest_ship1:
        //	BEL.[ True ][ False ][ 0.9499952504365194 ]
        //	BEL.[ True ][ 0.05000474956348062 ]
 	}
	   
	public void test_DMP_SOI(){  
		CPSCompilerMain.This().InitCompiler();
		UDB2SDB uToS = new UDB2SDB();  
		uToS.convert(pn); 
		CPSCompilerMain.This().compile(
									   "defineEvidence( hasResponsiveAIS_ship1, False );"+
									   "defineEvidence( hasWeaponVisible_ship1, True );"+
									   "defineEvidence( isJettisoningCargo_ship1, False );"+
									   "defineEvidence( hasResponsiveRadio_ship1, False );"+
									   "defineEvidence( isCrewVisible_ship1, False );"+
									   "defineEvidence( propellerTurnCount, 85.47197);"+
									   "defineEvidence( shipRCSchange, 54.330338 );"+ 
									   "defineEvidence(cavitation,  37.68292);"+
									   "run(DMP);"); 
		// isShipOfInterest_ship1:
        //	 BEL.[ True ][ False ][ 9.585702479845372E-5 ]
        //	 BEL.[ True ][ 0.9999041429752016 ]
 	}
	
	public void test_DMP_NOT_SOI(){  
		CPSCompilerMain.This().InitCompiler();
		UDB2SDB uToS = new UDB2SDB();  
		uToS.convert(pn); 
		CPSCompilerMain.This().compile(
									   "defineEvidence( hasResponsiveAIS_ship1, True );"+
									   "defineEvidence( hasWeaponVisible_ship1, False );"+
									   "defineEvidence( isJettisoningCargo_ship1, True );"+
									   "defineEvidence( hasResponsiveRadio_ship1, True );"+
									   "defineEvidence( isCrewVisible_ship1, False );"+
									   "defineEvidence( propellerTurnCount, 11.252131);"+
									   "defineEvidence( shipRCSchange, 4.440531 );"+ 
									   "defineEvidence(cavitation,  1.1423578);"+
									   "run(DMP);"); 
		// isShipOfInterest_ship1:
        //	BEL.[ True ][ False ][ 0.9999999519348779 ]
		//	BEL.[ True ][ 4.806512214611154E-8 ]
 	}
	 
	public void test_JT(){ 
		pn.setCreateLog(false); 
		
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
		algorithm.setNetwork(pn);
		algorithm.run(); 
		  
		try { 
        	pn.updateEvidences();
        		
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());               	
        } 
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
	
	/**
	 * It just delegates to UnBBayes' main
	 * @param args
	 */
	public static void main(String[] args) { 
		//String s = "simulation_v12_continuous_2";
		String s = "simulation_v12_continuous_discretized2";
		DMP_with_UBN_Test u = new DMP_with_UBN_Test(s);
	 
		u.NetName(); 
		//u.getLeafNodes();
		//u.test_DMP_NOT_SOI();
		//u.test_DMP1();
		u.test_JT();
		//u.printRatio();
		//u.NetInfo();
	}

}
