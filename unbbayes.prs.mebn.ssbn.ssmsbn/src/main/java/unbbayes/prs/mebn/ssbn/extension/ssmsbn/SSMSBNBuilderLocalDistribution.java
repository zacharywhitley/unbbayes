/**
 * 
 */
package unbbayes.prs.mebn.ssbn.extension.ssmsbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNodeUtils;
import unbbayes.prs.mebn.ssbn.cptgeneration.CPTForSSBNNodeGenerator;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.extension.ssmsbn.laskeyalgorithm.LaskeySSMSBNGenerator;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.util.GraphLayoutUtil;

/**
 * This class was extended for generation of SSMSBN instead of SSBN
 * 
 * @author rafaelmezzomo
 * @author estevaoaguiar
 */
public class SSMSBNBuilderLocalDistribution extends
		BuilderLocalDistributionImpl {
	

	private IdentationLevel level1 = new IdentationLevel(null); 
	private IdentationLevel level2 = new IdentationLevel(level1); 
	private IdentationLevel level3 = new IdentationLevel(level2); 
	private IdentationLevel level4 = new IdentationLevel(level3); 
	
	private ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());	
	

	/**
	 * This the constructor method and it is using the Factory Method
	 */
	protected SSMSBNBuilderLocalDistribution() {
		super();
	}
	/**
	 * @return a new instance of SSMSBNBuilderLocalDistribution
	 */
	public static SSMSBNBuilderLocalDistribution newInstance(){
		SSMSBNBuilderLocalDistribution ret = new SSMSBNBuilderLocalDistribution();
		
		return ret;
	}
	
	
	/**
	 * Create a MSBN from an SSBN.
	 * 
	 * @param simpleSSBNNodeList List contain the SimpleSSBNNodes, which corresponds to the SSBN. 
	 * @return A map, where the key is the name of the MSBN subnetwork and the value is the list of simple SSBN nodes in that network. 
	 */
	public Map<String, Set<SimpleSSBNNode>> convertSsbnIntoMsbn(
			List<SimpleSSBNNode> nodeList){
		
		Map<String, Set<SimpleSSBNNode>> map = new HashMap<String, Set<SimpleSSBNNode>>();
		
		for (SimpleSSBNNode node : nodeList) {
			// FIXME - ROMMEL - change the name of the MFragInstance so that it is unique!!!
			// We might have more than one instance of the same MFrag. Here we are putting everything together.
			String key = node.getMFragInstance().getMFragOrigin().getName();
			Set<SimpleSSBNNode> msbnSection = map.get(key); 
			if (msbnSection == null) {
				msbnSection = new HashSet<SimpleSSBNNode>();
				map.put(key, msbnSection);
			}
			msbnSection.add(node);
			
			// The main objective here is to add the input nodes in this subnetwork.
			// Since this is a set, it does not hurt to add "again" a resident parent.
			// The only restriction is that the parent has to be in the SSBN in order to show up
			// in the MSBN.
			for (SimpleSSBNNode parent : node.getParents()) {
				if (nodeList.contains(parent)) {
					msbnSection.add(parent);
				}
			}
		}
		
		return map; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl#buildLocalDistribution(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	public void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException {
		
		ProbabilisticNetwork pn; 
		Map<String, Set<SimpleSSBNNode>> connectedMsbn;
		SimpleSSBNNode nodeOfQuery;
		
		
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
			
			nodeOfQuery = ssbn.getQueryList().get(0).getSSBNNode(); 
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
			Map<String, Set<SimpleSSBNNode>> msbn = this.convertSsbnIntoMsbn(listForQuery);
			for (String key : msbn.keySet()) {
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
			connectedMsbn = new HashMap<String, Set<SimpleSSBNNode>>();
			for (String key : msbn.keySet()) {

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
			for (String key : msbn.keySet()) {
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
				ssbn.setNetwork(pn); 
			}else{
				List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(ssbn.getSimpleSsbnNodeList(), pn);
			    ssbn.setSsbnNodeList(listSSBNNode); 
				ssbn.setNetwork(pn); 
			}
			ssbn.getLogManager().skipLine(); 
			
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			throw e; 
		} catch (ImplementationRestrictionException e) {
			//This exception don't should be throw in a correct algorithm. 
			e.printStackTrace();
			throw new RuntimeException(e.getMessage()); 
		} 
		
		ssbn.getLogManager().printText(level2, false, "Simple Nodes translated to SSBNNodes"); 
		
		// clearing simple ssbn nodes
		ssbn.getSimpleSsbnNodeList().clear();
		// clearing memory before we continue
		System.gc();
		
	    CPTForSSBNNodeGenerator build = new CPTForSSBNNodeGenerator(ssbn.getLogManager());
	    
	    if(ssbn.getSsbnNodeList().size() > 0){
			ssbn.getLogManager().printText(level2, false, "Generate CPT for the SSBNNodes");
	    	build.generateCPTForAllSSBNNodes(ssbn.getSsbnNodeList().get(0));
	    }else{
	    	throw new SSBNNodeGeneralException(resource.getString("NotNodeInSSBN")); 
	    }
	    
	    // Generating the MSBN network.
		ssbn.getLogManager().printText(level1, false, "[3] Generating the MSBN network");
		SingleAgentMSBN msbnNet = new SingleAgentMSBN(nodeOfQuery.getShortName());
		
		// 1. For each subnetwork, create a new SubNetwork and add all nodes from the original PN that are also in the subnetwork
		for (String key : connectedMsbn.keySet()) {
			SubNetwork subnet = new SubNetwork(key);
			List<SimpleSSBNNode> nodeList = new ArrayList<SimpleSSBNNode>(connectedMsbn.get(key));
			// First let's add all relevant nodes to the subnetwork
			for (SimpleSSBNNode subnetNode : nodeList) {
				String subnetNodeName = SimpleSSBNNodeUtils.correspondencyMap.get(subnetNode).getName();
				Node nodeToAdd = subnet.getNode(subnetNodeName);
				if (nodeToAdd == null) {
					nodeToAdd = ((ProbabilisticNode)pn.getNode(subnetNodeName)).basicClone();
					((ProbabilisticNode)nodeToAdd).getProbabilityFunction().addVariable(nodeToAdd);
					subnet.addNode(nodeToAdd);
				}
			}
			// 2. Now let's add the edges and define the CPT
			for (Node clonedNode : subnet.getNodes()) {
				// 2.1. Add edges if necessary (if resident)
				boolean isResident = false;
				for (Node parentNode : pn.getNode(clonedNode.getName()).getParents()) {
					Node clonedParentNode = subnet.getNode(parentNode.getName());
					// If the parents are in the subnetwork, then this node is resident. Therefore, we need to add its edges.
					// Otherwise, the node is input (not resident)
					if (clonedParentNode != null) {
						try {
							isResident = true;
							subnet.addEdge(new Edge(clonedParentNode, clonedNode));
						} catch (InvalidParentException e) {
							// This exception should never happen!!!
							e.printStackTrace();
						}
					
					}
				}
				// 2.2. Define CPT
				ProbabilisticNode node = ((ProbabilisticNode)pn.getNode(clonedNode.getName()));
				if (isResident) {
					// If the node is resident, then it has all its parents in the subnetwork, so we just need to clone the CPT
					PotentialTable cpt = node.getProbabilityFunction();
					PotentialTable clonedCpt = ((ProbabilisticNode)clonedNode).getProbabilityFunction();
					for (int i = 0; i < clonedCpt.tableSize(); i++) {
						clonedCpt.setValue(i, cpt.getValue(i));	
					}
				} else {
					// Otherwise, the node is input and it has no parents in this subnetwork.
					// By setting the CPT as uniform, the UnBBayes MSBN implementation behaves as the node has no potential at all
					// and thus, it behaves like it's using the CPT from a node defined in another subnetwork (another network section)
					// which in our case is in the upper instance node. This is what we want.
					float linearValue = 1f / clonedNode.getStatesSize();
					PotentialTable cpt = ((ProbabilisticNode)clonedNode).getProbabilityFunction();
					for (int i = 0; i < cpt.tableSize(); i++) {
						cpt.setValue(i, linearValue);	
					}
				}
				if (node.hasEvidence()) {
					((ProbabilisticNode)clonedNode).addFinding(node.getEvidence());
				}

			}
			// 3. Add subnetwork to MSBN
			msbnNet.addNetwork(subnet);
		}
		
		// Checking if structure was built correctly and fixing position of nodes.
		for (int i = 0; i < msbnNet.getNetCount(); i++) {
			SubNetwork subnet = msbnNet.getNetAt(i);
			(new GraphLayoutUtil(subnet)).doLayout();
			ssbn.getLogManager().printText(level2, false, "Subnetwork " + subnet.getName());
			for (Node node : subnet.getNodes()) {
				ssbn.getLogManager().printText(level3, false, " >" + node.getName());
				for (Node childNode : node.getChildren()) {
					ssbn.getLogManager().printText(level4, false, " Child >" + childNode.getName());
				}
			}
		}
		
		// 4. Set the MSBN network in SSBN class
		ssbn.setNetwork(msbnNet);
		ssbn.getLogManager().skipLine();
		
	}
		
}