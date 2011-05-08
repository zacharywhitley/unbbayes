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
			// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
			clearSimpleSSBNNodeListAtLPD = Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
					BuilderLocalDistributionImpl.class.getCanonicalName()+".clearSimpleSSBNNodeListAtLPD").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());	
	
	/***
	 *We need to have at leat one visible contructor to extend this class.
	 */
	protected BuilderLocalDistributionImpl(){
		
	}
	
	public static BuilderLocalDistributionImpl newInstance(){
		return new BuilderLocalDistributionImpl();
	}
	
	public void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException {
		
		ProbabilisticNetwork pn; 
		SimpleSSBNNode nodeOfQuery;
		
		
		try {
			// FIXME - Here only one probabilistic network are created... The fix should be
			//here, creating multiples pn's. 
			ssbn.getLogManager().printText(level1, false, "[1] Separing the disconnected SSBN networks"); 
			List<SimpleSSBNNode>[] nodesPerNetworkArray = SimpleSSBNNodeUtils.individualizeDisconnectedNetworks(ssbn.getSimpleSsbnNodeList()); 
			int netId = 0; 
			for(List<SimpleSSBNNode> networkNodesList: nodesPerNetworkArray){
				ssbn.getLogManager().printText(level2, false, "Network " + netId + ":"); netId++; 
				for(SimpleSSBNNode node: networkNodesList){
					ssbn.getLogManager().printText(level3, false, " >" + node);
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
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
	    
	
// TODO - SHOU and ROMMEL - FIND A BETTER DESIGN TO ADD THE SSMSBN GENERATOR
/*
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
			connectedMsbn = new HashMap<String, Set<SimpleSSBNNode>>();
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
	    
	    // Generating the MSBN network.
		ssbn.getLogManager().printText(level1, false, "[3] Genering the MSBN network");
		SingleAgentMSBN msbnNet = new SingleAgentMSBN(nodeOfQuery.getShortName());
		
		// 1. For each subnetwork, create a new SubNetwork and add all nodes from the original PN that are also in the subnetwork
		Iterator<String> keyIterator = connectedMsbn.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
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
		ssbn.setMsbnNetwork(msbnNet);
		ssbn.getLogManager().skipLine();
*/
		
		
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
