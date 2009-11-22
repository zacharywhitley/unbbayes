package unbbayes.prs.mebn.ssbn;

import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.cptgeneration.CPTForSSBNNodeGenerator;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.util.ApplicationPropertyHolder;

public class BuilderLocalDistributionImpl implements IBuilderLocalDistribution {

	IdentationLevel level1 = new IdentationLevel(null); 
	IdentationLevel level2 = new IdentationLevel(level1); 
	IdentationLevel level3 = new IdentationLevel(level2); 
	IdentationLevel level4 = new IdentationLevel(level3); 
	IdentationLevel level5 = new IdentationLevel(level4); 
	IdentationLevel level6 = new IdentationLevel(level5); 
	
	private static boolean clearSimpleSSBNNodeListAtLPD = false;
	static{
		try {
			clearSimpleSSBNNodeListAtLPD = Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
					BuilderLocalDistributionImpl.class.getCanonicalName()+".clearSimpleSSBNNodeListAtLPD").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	
	
	private BuilderLocalDistributionImpl(){
		
	}
	
	public static BuilderLocalDistributionImpl newInstance(){
		return new BuilderLocalDistributionImpl();
	}
	
	public void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException {
		
		ProbabilisticNetwork pn; 
		
		try {
			//FIX - Here only one probabilistic network are created... The fix should be
			//here, creating multiples pn's. 
			ssbn.getLogManager().printText(level1, false, "[1] Separing the desconected networks"); 
			List<SimpleSSBNNode>[] nodesPerNetworkArray = SimpleSSBNNodeUtils.individualizeDisconnectedNetworks(ssbn.getSimpleSsbnNodeList()); 
			int netId = 0; 
			for(List<SimpleSSBNNode> networkNodesList: nodesPerNetworkArray){
				ssbn.getLogManager().printText(level2, false, "Network " + netId + ":"); netId++; 
				for(SimpleSSBNNode node: networkNodesList){
					ssbn.getLogManager().printText(level3, false, " >" + node);
				}
			}
			
			SimpleSSBNNode nodeOfQuery = ssbn.getQueryList().get(0).getSSBNNode(); 
			List<SimpleSSBNNode> listForQuery = null; 
			
			for(List<SimpleSSBNNode> networkNodesList: nodesPerNetworkArray){
				for(SimpleSSBNNode node: networkNodesList){
					if(node.equals(nodeOfQuery)){
						listForQuery = networkNodesList; 
						break; 
					}
				}
			}
			nodesPerNetworkArray = null;
			ssbn.getLogManager().skipLine(); 
			
			ssbn.getLogManager().printText(level1, false, "[2] Genering the network"); 
			pn =  new ProbabilisticNetwork(this.resource.getString("DefaultNetworkName"));
			if(listForQuery!=null){
				List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(listForQuery, pn);
			    ssbn.setSsbnNodeList(listSSBNNode); 
				ssbn.setProbabilisticNetwork(pn); 
			}else{
				List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(ssbn.getSimpleSsbnNodeList(), pn);
			    ssbn.setSsbnNodeList(listSSBNNode); 
				ssbn.setProbabilisticNetwork(pn); 
			}
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			throw e; 
		} catch (ImplementationRestrictionException e) {
			//This exception don't should be throw in a correct algorithm. 
			e.printStackTrace();
			throw new RuntimeException(e.getMessage()); 
		} 
		
		ssbn.getLogManager().printText(level2,false, "Simple Nodes translated to SSBNNodes"); 
		
		// clearing simple ssbn nodes
		if (clearSimpleSSBNNodeListAtLPD) {
			ssbn.getSimpleSsbnNodeList().clear();
			// clearing memory before we continue
			System.gc();
		}
		
	    CPTForSSBNNodeGenerator build = new CPTForSSBNNodeGenerator(ssbn.getLogManager());
	    
	    if(ssbn.getSsbnNodeList().size() > 0){
			ssbn.getLogManager().printText(level2, false, "Generate CPT for the SSBNNodes");
	    	build.generateCPTForAllSSBNNodes(ssbn.getSsbnNodeList().get(0));
	    }else{
	    	throw new SSBNNodeGeneralException(resource.getString("NotNodeInSSBN")); 
	    }
		
	}

	/**
	 * @return the clearSimpleSSBNNodeListAtLPD
	 */
	public static boolean isClearSimpleSSBNNodeListAfterLPD() {
		return clearSimpleSSBNNodeListAtLPD;
	}

	/**
	 * @param clearSimpleSSBNNodeListAtLPD the clearSimpleSSBNNodeListAtLPD to set
	 */
	public static void setClearSimpleSSBNNodeListAfterLPD(
			boolean clearSimpleSSBNNodeListAfterLPD) {
		BuilderLocalDistributionImpl.clearSimpleSSBNNodeListAtLPD = clearSimpleSSBNNodeListAfterLPD;
	}


	
	
}
