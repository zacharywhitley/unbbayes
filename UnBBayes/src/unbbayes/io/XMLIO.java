/*
 * Created on 26/06/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import unbbayes.util.*;
import unbbayes.prs.bn.Network;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;

/**
 * @author Shigeki
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XMLIO implements BaseIO {
	
	private static String NET_NAME = "/BIF/HEADER/NAME",
							NODE_SIZE = "/BIF/STATICPROPERTY/NODESIZE",
							VARIABLES = "/BIF/NETWORK/VARIABLES/*";
							
	private static Node makeNode(org.w3c.dom.Node elNode) throws Exception {
		Node node = null;
		node = new ProbabilisticNode();		
		org.w3c.dom.Node tmpNode = XPathAPI.selectSingleNode(elNode, "/VAR/LABEL");
		node.setName(XMLUtil.getValue(tmpNode));
		
		NodeIterator states = XPathAPI.selectNodeIterator(elNode, "VAR/STATENAME");
		while ((elNode = states.nextNode()) != null) {
			node.appendState(XMLUtil.getValue(elNode));
		}
		
		return node;				
	}
	
	private static void makeStructure(ProbabilisticNetwork net, org.w3c.dom.Node elNode) {
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public ProbabilisticNetwork load(File input) throws LoadException, IOException {
		ProbabilisticNetwork net = null;
		org.w3c.dom.Node elNode = null;
		try {			
			InputSource is = new InputSource(new FileReader(input));
			Document doc = XMLUtil.getDocument(is);			
			elNode = XPathAPI.selectSingleNode(doc, NET_NAME);
			net = new ProbabilisticNetwork(XMLUtil.getValue(elNode));
			
			NodeIterator nodeIterator = XPathAPI.selectNodeIterator(doc, VARIABLES);
			while ((elNode = nodeIterator.nextNode()) != null) {
				net.addNode(makeNode(elNode));
			}
			elNode = XPathAPI.selectSingleNode(doc, "/BIF/NETWORK/STRUCTURE");
			makeStructure(net, elNode);
			
		} catch (Exception e) {
			throw new LoadException("Load Error");			
		}
		return net;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#loadMSBN(java.io.File)
	 */
	public SingleAgentMSBN loadMSBN(File input)
		throws LoadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.bn.Network)
	 */
	public void save(File output, Network net) throws FileNotFoundException {		
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#saveMSBN(java.io.File, unbbayes.prs.msbn.SingleAgentMSBN)
	 */
	public void saveMSBN(File output, SingleAgentMSBN net)
		throws FileNotFoundException {
		// TODO Auto-generated method stub

	}

}
