/*
 * Created on 26/06/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import unbbayes.util.*;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.Network;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.Edge;
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
							VARIABLES = "/BIF/NETWORK/VARIABLES/VAR",
							ARCS = "/BIF/NETWORK/STRUCTURE/ARC",
							POTENTIALS = "/BIF/NETWORK/POTENTIAL/POT";
							
	private static Node makeNode(org.w3c.dom.Node elNode) throws Exception {
		// TODO Load Decision and Utility node!
		ProbabilisticNode node = null;
		node = new ProbabilisticNode();		
		node.setName(XPathAPI.selectSingleNode(elNode, "@NAME").getNodeValue());
		org.w3c.dom.Node tmpNode = XPathAPI.selectSingleNode(elNode, "LABEL");
		if (tmpNode != null) {
			node.setDescription(XMLUtil.getValue(tmpNode));
		}
		int xPos = Integer.parseInt(XPathAPI.selectSingleNode(elNode, "@XPOS").getNodeValue());
		int yPos = Integer.parseInt(XPathAPI.selectSingleNode(elNode, "@YPOS").getNodeValue());
		node.getPosition().setLocation(xPos, yPos);
		
		NodeIterator states = XPathAPI.selectNodeIterator(elNode, "STATENAME");
		while ((elNode = states.nextNode()) != null) {
			node.appendState(XMLUtil.getValue(elNode));
		}
		
		PotentialTable auxTabPot = node.getPotentialTable();
		auxTabPot.addVariable(node);
		return node;				
	}
	
	/*
	private static void makeStructure(ProbabilisticNetwork net, NodeIterator arcs) throws Exception {
		org.w3c.dom.Node node;
		while ((node = arcs.nextNode()) != null) {
			String parent = XPathAPI.selectSingleNode(node, "@PARENT").getNodeValue();
			String child = XPathAPI.selectSingleNode(node, "@CHILD").getNodeValue();
			Node parentNode = net.getNode(parent);
			Node childNode = net.getNode(child);
			Edge auxEdge = new Edge(parentNode, childNode);
			net.addEdge(auxEdge);
		}
	}
	*/
	
	private static void readCondSet(Node childNode, ProbabilisticNetwork net, NodeIterator condSet) throws Exception {
		org.w3c.dom.Node node;
		while ((node = condSet.nextNode()) != null) {
			String parent = XPathAPI.selectSingleNode(node, "@NAME").getNodeValue();
			Node parentNode = net.getNode(parent);
			Edge auxEdge = new Edge(parentNode, childNode);
			net.addEdge(auxEdge);
		}
	}
	
	private static void fillTable(PotentialTable table, NodeIterator dpis) throws Exception {
		int numStates = table.getVariableAt(0).getStatesSize();
		int num = table.tableSize() / numStates;		
		for (int i = 0; i < num; i++) {
			org.w3c.dom.Node node = dpis.nextNode();			
			StringTokenizer stk = new StringTokenizer(XMLUtil.getValue(node));
			int offset = i*numStates;
			for (int j = 0; j < numStates ; j++) {
				float value = Float.parseFloat(stk.nextToken());
				table.setValue(offset + j, value);
			}
		}
	}
	
	private static void assignPotentials(ProbabilisticNetwork net, NodeIterator potentials) throws Exception {
		org.w3c.dom.Node node;
		while ((node = potentials.nextNode()) != null) {
			org.w3c.dom.Node tmp = XPathAPI.selectSingleNode(node, "PRIVATE");
			String nodeName = XPathAPI.selectSingleNode(tmp, "@NAME").getNodeValue();
			Node childNode = net.getNode(nodeName);
			
			NodeIterator condSet = XPathAPI.selectNodeIterator(node, "CONDSET/CONDLEM");
			readCondSet(childNode, net, condSet);
						
			PotentialTable table = ((ITabledVariable)childNode).getPotentialTable();			
			NodeIterator dpis = XPathAPI.selectNodeIterator(node, "DPIS/DPI");
			fillTable(table, dpis);
		}
	}
	
	public ProbabilisticNetwork load(Document doc) throws Exception {
			ProbabilisticNetwork net = null;
			org.w3c.dom.Node elNode = XPathAPI.selectSingleNode(doc, NET_NAME);
			net = new ProbabilisticNetwork(XMLUtil.getValue(elNode));
			
			NodeIterator nodeIterator = XPathAPI.selectNodeIterator(doc, VARIABLES);
			while ((elNode = nodeIterator.nextNode()) != null) {
				net.addNode(makeNode(elNode));
			}
			
			/*
			nodeIterator = XPathAPI.selectNodeIterator(doc, ARCS);
			makeStructure(net, nodeIterator);
			*/
			
			nodeIterator = XPathAPI.selectNodeIterator(doc, POTENTIALS);
			assignPotentials(net, nodeIterator);
			return net;
	}


	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public ProbabilisticNetwork load(File input) throws LoadException, IOException {
		try {			
			InputSource is = new InputSource(new BufferedReader(new FileReader(input)));
			Document doc = XMLUtil.getDocument(is);
			return load(doc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new LoadException("Load Error");			
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#loadMSBN(java.io.File)
	 */
	public SingleAgentMSBN loadMSBN(File input)
		throws LoadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static void writeHeader(PrintWriter arq, Network net) {
		arq.println("<HEADER>");
		arq.println("<NAME>" + net.getName() + "</NAME>");
		arq.println("<CREATOR>UnBBayes</CREATOR>");
		arq.println("</HEADER>");
	}
	
	private static void writeStaticProperty(PrintWriter arq, Network net) {
		arq.println("<STATICPROPERTY>");
		int nodeSize = (int) net.getRadius() * 2;
		arq.println("<NODESIZE>" + nodeSize + "," + nodeSize + "</NODESIZE>");
		arq.println("</STATICPROPERTY>");
	}
	
	private static void writeNetwork(PrintWriter arq, Network net) {
		arq.println("<NETWORK>");		
		arq.println("<VARIABLES>");
		for (int i = 0; i < net.getNodeCount(); i++) {
			Node node = net.getNodeAt(i);
			arq.println("<VAR NAME='" + node.getName() + "' TYPE='discrete' " +
			  	    	"XPOS='" + (int) node.getPosition().getX() + "' " +
			  	    	"YPOS='" + (int) node.getPosition().getY() + "'>");
			arq.println("<LABEL>" + node.getDescription() + "</LABEL>");
			for (int j = 0; j < node.getStatesSize(); j++) {
				arq.println("<STATENAME>" + node.getStateAt(j) + "</STATENAME>");
			}
			arq.println("</VAR>");
		}
		arq.println("</VARIABLES>");
		
		/*
		arq.println("<STRUCTURE>");
		
		for (int i = 0; i < net.getEdges().size(); i++) {
			Edge arc = (Edge) net.getEdges().get(i);
			arq.println("<ARC PARENT='" + arc.getOriginNode().getName() + "' " +
					     "CHILD='"+ arc.getDestinationNode().getName() +"'/>");
		}
			
		arq.println("</STRUCTURE>");
		*/
		
		arq.println("<POTENTIAL>");
		for (int i = 0; i < net.getNodeCount(); i++) {
			Node node = net.getNodeAt(i);
			if (node.getType() == Node.DECISION_NODE_TYPE) {
				continue;
			}
			
			arq.println("<POT TYPE='discrete'>");
			arq.println("<PRIVATE NAME='" + node.getName() + "' />");
			arq.println("<CONDSET>");
			
			PotentialTable table = ((ITabledVariable) node).getPotentialTable();			
			for (int j = 1; j < table.variableCount(); j++) {
				Node parent = table.getVariableAt(j);
				arq.println("<CONDLEM NAME='" + parent.getName() + "' />");
			}
			
			arq.println("</CONDSET>");
			arq.println("<DPIS>");
			int statesSize = node.getStatesSize();
			int num = table.tableSize() / statesSize;
			for (int j = 0; j < num; j++) {
				arq.print("<DPI INDEXES='" + j + "'>");
				int offset = j*statesSize;
				for (int k = 0; k < statesSize; k++) {
					if (k != 0) {
						arq.print(" ");						
					}
					arq.print(table.getValue(offset + k));					
				}
				arq.println("</DPI>");
			}
			arq.println("</DPIS>");
			arq.println("</POT>");
		}
		arq.println("</POTENTIAL>");
		arq.println("</NETWORK>");
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.bn.Network)
	 */
	public void save(File output, Network net) throws IOException {
		PrintWriter arq = new PrintWriter(new FileWriter(output));						
		arq.println("<BIF>");
		writeHeader(arq, net);
		writeStaticProperty(arq, net);
		writeNetwork(arq, net);
		arq.println("</BIF>");
		arq.flush();
		arq.close();
	}


	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#saveMSBN(java.io.File, unbbayes.prs.msbn.SingleAgentMSBN)
	 */
	public void saveMSBN(File output, SingleAgentMSBN net)
		throws FileNotFoundException {
		// TODO Auto-generated method stub

	}
}
