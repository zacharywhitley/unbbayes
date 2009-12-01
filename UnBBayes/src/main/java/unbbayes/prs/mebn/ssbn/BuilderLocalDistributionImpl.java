package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.cptgeneration.CPTForSSBNNodeGenerator;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
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
			// FIXME - Here only one probabilistic network are created... The fix should be
			//here, creating multiples pn's. 
			ssbn.getLogManager().printText(level1, false, "[1.1] Separing the disconnected SSBN networks"); 
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
			
			// Converting the pseudo SSBN above into a pseudo MSBN.
			ssbn.getLogManager().printText(level1, false, "[1.2] Converting the SSBN into a MSBN");
			Map<String, Set<SimpleSSBNNode>> msbn = SimpleSSBNNodeUtils.convertSsbnIntoMsbn(listForQuery);
			Iterator<String> keyIterator = msbn.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				ssbn.getLogManager().printText(level2, false, "Subnetwork " + key);
				Set<SimpleSSBNNode> nodeList = msbn.get(key);
				for (SimpleSSBNNode node : nodeList) {
					ssbn.getLogManager().printText(level3, false, " >" + node);
				}
			}
			ssbn.getLogManager().skipLine(); 
			
			// In the process above we might end up with disconnected subnetworks.
			// Therefore we have to break each disconnected subnetwork in n connected subnetworks.
			ssbn.getLogManager().printText(level1, false, "[1.3] Separating the disconnected MSBN subnetworks");
			Map<String, Set<SimpleSSBNNode>> connectedMsbn = new HashMap<String, Set<SimpleSSBNNode>>();
			keyIterator = msbn.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				List<SimpleSSBNNode> subnetwork = new ArrayList<SimpleSSBNNode>(msbn.get(key));
				List<SimpleSSBNNode>[] subnetworkList = SimpleSSBNNodeUtils.individualizeDisconnectedNetworks(subnetwork);
				for (int i = 0; i < subnetworkList.length; i++) {
					ssbn.getLogManager().printText(level2, false, "Subnetwork " + key + "_" + i);
					connectedMsbn.put(key + "_" + i, new HashSet<SimpleSSBNNode>(subnetworkList[i]));
					for (SimpleSSBNNode node : subnetworkList[i]) {
						ssbn.getLogManager().printText(level3, false, " >" + node);
					}
				}
			}
			msbn = null;
			ssbn.getLogManager().skipLine(); 
			
			// Generating the SSBN network.
			ssbn.getLogManager().printText(level1, false, "[2] Genering the SSBN network"); 
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
			ssbn.getLogManager().skipLine(); 
			
			// Generating the MSBN network.
			ssbn.getLogManager().printText(level1, false, "[3] Genering the MSBN network");
			SingleAgentMSBN msbnNet = new SingleAgentMSBN(nodeOfQuery.getShortName());
			
			// 1. For each subnetwork, create a new SubNetwork and add all nodes from the original PN that are also in the subnetwork
			keyIterator = connectedMsbn.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				SubNetwork subnet = new SubNetwork(key);
				List<SimpleSSBNNode> nodeList = new ArrayList<SimpleSSBNNode>(connectedMsbn.get(key));
				for (SimpleSSBNNode subnetNode : nodeList) {
					String subnetNodeName = SimpleSSBNNodeUtils.correspondencyMap.get(subnetNode).getName();
					// 2. For each node in the original PN (the one generated from the SSBN) check if this is the node to add
					for (Node node : pn.getNodes()) {
						if (node.getName().equals(subnetNodeName)) {
							// FIXME - ROMMEL - I might need to add/remove edges
							subnet.addNode(node);
							break;
						}
					}
				}
				// FIXME - ROMMEL - Check to see the right way to add subnetworks (look at OOBN)
				msbnNet.addNetwork(subnet);
			}
			
			// 3. If the node has parents in the generated SSBN but they are not in the subnetwork, set the 
			// CPT to uniform (hint from Shou Matsumoto - no need to marginalize the parents given the MSBN behavior)
			
			
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
