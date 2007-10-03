package unbbayes.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

import unbbayes.gui.HierarchicTree;
import unbbayes.io.xmlclasses.BIF;
import unbbayes.io.xmlclasses.BIFType;
import unbbayes.io.xmlclasses.ObjectFactory;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.util.ArrayMap;
import unbbayes.util.NodeList;

/** 
 * Manipulates I/O of XMLBIF file format
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @author Shigeki (version 1.0)
 * @version 2.0
 */

public class XMLIO implements BaseIO{
	
	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.io.resources.IoResources");
	
	/**
	 * Loads a new Probabilistic network from the input file.
	 * 
	 * @param input the input file for the network
	 * @return The loaded network
	 * @throws LoadException If the file doesn't describes a network.
	 * @throws IOException	If an IO error occurs
	 */
	public ProbabilisticNetwork load(File input) throws LoadException, IOException, JAXBException{
		
		int index = input.getName().lastIndexOf('.');
		String id = input.getName().substring(0, index);
		ProbabilisticNetwork pn = new ProbabilisticNetwork(id);		
		loadXML(input, pn); 
		return pn; 
	}
	
	/**
	 * Loads a new MSBN from the input DIRECTORY
	 * @param input	Input directory for the MSBN
	 * @return The loaded MSBN
	 * @throws LoadException If the directory doesn't describes a MSBN.
	 * @throws IOException	 If an IO error occurs
	 */
	public SingleAgentMSBN loadMSBN(File input) throws IOException,LoadException, JAXBException {
		if (! input.isDirectory()) {
			throw new LoadException(resource.getString("IsNotDirectoryException"));
		}
		SingleAgentMSBN msbn = new SingleAgentMSBN(input.getName());
		File files[] = input.listFiles();
		for (int i = 0; i < files.length; i++) {			
			if (files[i].isFile()) {
				String fileName = files[i].getName();
				int index = fileName.lastIndexOf('.');
				if (index < 0) {
					throw new RuntimeException();
				}
				if (fileName.substring(index+1).equalsIgnoreCase("xml")) {
					SubNetwork net = new SubNetwork(fileName.substring(0, index));
					loadXML(files[i], net);
					msbn.addNetwork(net);
				}
			}
		}
		return msbn;
	}
	
	/**
	 * Saves a network to the output file.
	 * @param output	The output file to save
	 * @param net		The network to save.
	 */
	
	public void save(File output, SingleEntityNetwork net) throws IOException, JAXBException{
		
		FileWriter outputxml = new FileWriter(output);

		saveXML(outputxml, net); 

		outputxml.flush();
		outputxml.close();
	}
	
	/**
	 * Saves a MSBN to the output directory.
	 * @param output The output file to save
	 * @param net		The MSBN to save.
	 */
	public void saveMSBN(File output, SingleAgentMSBN msbn) throws FileNotFoundException, IOException, JAXBException{
		if (! output.isDirectory()) {
			System.err.println(resource.getString("IsNotDirectoryException"));
			return;			
		}
		
		for (int i = msbn.getNetCount()-1; i>=0; i--) {
			SingleEntityNetwork net = msbn.getNetAt(i);
			File out = new File(output, net.getId() + ".xml");
			save(out, net);
		}    
	}
	
	
//	----------------------------------------------------------------------------------------
	
	
	/* 
	 * Recuperates a net from a XML file
	 * input: file where the net resides
	 * pn: net where the load should be done (note:the net should be already created)
	 * */ 
	
	private SingleEntityNetwork loadXML(File input, SingleEntityNetwork pn) throws LoadException, IOException, JAXBException{
		
		int i, j; 	
		
		InputSource isource = new InputSource(new FileInputStream(input));
		
		JAXBContext context = JAXBContext.newInstance("unbbayes.io.xmlclasses");    	
		Unmarshaller unmarshaller = context.createUnmarshaller();
		
		unmarshaller.setValidating(true);
		

			BIF bif = (BIF) unmarshaller.unmarshal(isource); 
			BIFType.HEADERType header = (BIFType.HEADERType) bif.getHEADER().get(0); 
			pn.setName(header.getNAME()); 
			
			// --------------- Static Property -------------------------------------------------
			
			BIFType.STATICPROPERTYType staticProperty = (BIFType.STATICPROPERTYType) bif.getSTATICPROPERTY().get(0);  
			
			pn.setRadius(Double.parseDouble((staticProperty.getNODESIZE()))/2); 		    
			//-----> setNODEFONTNAME();
			//-----> setNODEFONTSIZE();
			UtilityNode.setColor(staticProperty.getCOLORUTILITY());
			DecisionNode.setColor(staticProperty.getCOLORDECISION());
			ProbabilisticNode.setDescriptionColor(staticProperty.getCOLORPROBDESCRIPTION());
			ProbabilisticNode.setExplanationColor(staticProperty.getCOLORPROBEXPLANATION()); 
			
			// ------------------- Hierarchy -------------------------------------------------------
			
			BIFType.HIERARCHYType hierarchy = (BIFType.HIERARCHYType)bif.getHIERARCHY().get(0); 
			
			DefaultMutableTreeNode root; 
			root = loadHierarchicTree( hierarchy );

			// construct tree
			DefaultTreeModel model = new DefaultTreeModel(root);
			HierarchicTree hierarchicTree =
			new HierarchicTree(model);
			
			pn.setHierarchicTree(hierarchicTree);
			
			// ------------------- Variables -------------------------------------------------------
			
			BIFType.NETWORKType network = (BIFType.NETWORKType) bif.getNETWORK().get(0);
			
			BIFType.NETWORKType.VARIABLESType variables = (BIFType.NETWORKType.VARIABLESType) network.getVARIABLES().get(0); 
			
			List vars = (ArrayList)variables.getVAR(); 
			
			for(i = 0; i < vars.size(); i++){
				pn.addNode(makeNode((BIFType.NETWORKType.VARIABLESType.VARType)vars.get(i)));
			}
			
			List utilities = (ArrayList)variables.getUTILITY(); 
			
			for(i = 0; i < utilities.size(); i++){
				pn.addNode(makeNodeUtility((BIFType.NETWORKType.VARIABLESType.UTILITYType)utilities.get(i)));
			}
			
			List decision = (ArrayList)variables.getDECISION(); 
			
			for(i = 0; i < decision.size(); i++){
				pn.addNode(makeNodeDecision((BIFType.NETWORKType.VARIABLESType.DECISIONType)decision.get(i)));
			}			
			
			BIFType.NETWORKType.POTENTIALType potential = (BIFType.NETWORKType.POTENTIALType)network.getPOTENTIAL().get(0); 
			
			List lpot = (ArrayList) potential.getPOT(); 
			
			for(i = 0; i < lpot.size(); i++){
				BIFType.NETWORKType.POTENTIALType.POTType pot = (BIFType.NETWORKType.POTENTIALType.POTType)lpot.get(i); 
				BIFType.NETWORKType.POTENTIALType.POTType.PRIVATEType privatet = ( BIFType.NETWORKType.POTENTIALType.POTType.PRIVATEType)pot.getPRIVATE().get(0); 
				
				Node childNode = pn.getNode(privatet.getNAME());
				
				BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType condset = (BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType)pot.getCONDSET().get(0);
				List lcondlen = (ArrayList)condset.getCONDLEM(); 
				
				for(j = 0; j < lcondlen.size(); j++) {
					
					BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType.CONDLEMType condlen = (BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType.CONDLEMType)lcondlen.get(j); 
					String parent = condlen.getNAME(); 
					Node parentNode = pn.getNode(parent);
					Edge auxEdge = new Edge(parentNode, childNode);
					pn.addEdge(auxEdge); 
				}
				
				if (childNode.getType() == Node.DECISION_NODE_TYPE){
					continue;
				}
				
				BIFType.NETWORKType.POTENTIALType.POTType.DPISType dpis = (BIFType.NETWORKType.POTENTIALType.POTType.DPISType)pot.getDPIS().get(0); 
				
				List ldpi = (ArrayList) dpis.getDPI(); 
				
				PotentialTable table = ((ITabledVariable)childNode).getPotentialTable();	
				
				int statesSize = table.getVariableAt(0).getStatesSize();
				int num = table.tableSize() / statesSize;		
				int indiceLDpis = 0; 
				
				//preenchimento de cada um dos estados para a variavel 
				for (int m = 0; m < num; m++) {
					
					int offset = m*statesSize;
					
					for (int n = 0; n < statesSize ; n++) {
						BIFType.NETWORKType.POTENTIALType.POTType.DPISType.DPIType dpi = (BIFType.NETWORKType.POTENTIALType.POTType.DPISType.DPIType)ldpi.get(n + indiceLDpis);			
						float value = Float.parseFloat(dpi.getValue()); 
						table.setValue(offset + n, value);
					}
					
					indiceLDpis += statesSize;
				}
			}	
			return pn; 
	}
	
	/* Saves the net in XML file
	 * net: net to be saved
	 */
	
	private void saveXML(FileWriter arqoutput, SingleEntityNetwork net) throws JAXBException {
		
		PrintWriter arq = new PrintWriter(arqoutput);
		
		ObjectFactory of = new ObjectFactory();
		
		BIF bif = of.createBIF(); 
		
		/*---------------- HEADER ---------------------*/
		
		BIFType.HEADERType header = of.createBIFTypeHEADERType(); 
		
		header.setNAME(net.getName());
		header.setVERSION(1); 
		header.setCREATOR("UnBBayes");
		
		bif.getHEADER().add(header);
		
		/*---------------- STATICPROPERTY ---------------------*/
		
		BIFType.STATICPROPERTYType staticProperty = of.createBIFTypeSTATICPROPERTYType(); 
		
		staticProperty.setNODESIZE("" + ((int) net.getRadius() * 2));
		//-----> setNODEFONTNAME();
		//-----> setNODEFONTSIZE();
		staticProperty.setCOLORUTILITY(UtilityNode.getColor().getRGB());
		staticProperty.setCOLORDECISION(DecisionNode.getColor().getRGB());
		staticProperty.setCOLORPROBDESCRIPTION(ProbabilisticNode.getDescriptionColor().getRGB()); 
		staticProperty.setCOLORPROBEXPLANATION(ProbabilisticNode.getExplanationColor().getRGB()); 
		
		
		bif.getSTATICPROPERTY().add(staticProperty);
		
		/*------------------HIERARCHY---------------------------------------*/
		HierarchicTree ht = net.getHierarchicTree();
		
		BIFType.HIERARCHYType hierarchy = of.createBIFTypeHIERARCHYType();  	
		
		TreeModel model = ht.getModel();	    	
		TreeNode root = (TreeNode)model.getRoot();
		
		processTreeNode(root, model, hierarchy); 
		
		bif.getHIERARCHY().add(hierarchy);
		
		/*------------------NETWORK---------------------------------------*/
		
		BIFType.NETWORKType network = of.createBIFTypeNETWORKType(); 
		
		writeBifNetwork(network, net); 
		
		bif.getNETWORK().add(network); 
		
		
		JAXBContext context = JAXBContext.newInstance("unbbayes.io.xmlclasses");    	
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "US-ASCII");
		marshaller.marshal(bif , arq); 	
	}    
	
	
	private static void writeBifNetwork(BIFType.NETWORKType bifNetwork, SingleEntityNetwork net) throws JAXBException{
		
		ObjectFactory of = new ObjectFactory();
		
		// escrita de VARIABLES
		
		BIFType.NETWORKType.VARIABLESType networkVariables = of.createBIFTypeNETWORKTypeVARIABLESType();    	 
		
		// write of var / utility / decision  
		
		for (int i = 0; i < net.getNodeCount(); i++) {
			
			Node node = net.getNodeAt(i);
			
			if (node.getType() == Node.PROBABILISTIC_NODE_TYPE){
				//atributos
				BIFType.NETWORKType.VARIABLESType.VARType var = of.createBIFTypeNETWORKTypeVARIABLESTypeVARType(); 
				var.setNAME(node.getName());
				var.setTYPE("discrete"); 
				var.setXPOS((int) node.getPosition().getX());
				var.setYPOS((int) node.getPosition().getY());
				
				//elementos de sequence
				var.setLABEL(node.getDescription());
				
				//statename
				
				for (int j = 0; j < node.getStatesSize(); j++) {
					BIFType.NETWORKType.VARIABLESType.VARType.STATENAMEType statename = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeSTATENAMEType();
					statename.setValue(node.getStateAt(j));
					var.getSTATENAME().add(statename);					
				}
				
				
				//Metaphore
				
				if (node.getInformationType() == Node.EXPLANATION_TYPE){
					BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType metaphore = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeMETAPHOREType(); 
					
					//Description
					String explanationDescription = node.getExplanationDescription();
					metaphore.getDESCRIPTION().add(explanationDescription);
					
					//Others informations 
					ArrayMap<String,ExplanationPhrase> arrayMap = node.getPhrasesMap();
					int size = arrayMap.size();
					ArrayList keys = arrayMap.getKeys();
					for (int k = 0; k < size; k++)
					{
						Object key = keys.get(k);
						ExplanationPhrase explanationPhrase = arrayMap.get(key);
						switch(explanationPhrase.getEvidenceType()){
						case ExplanationPhrase.COMPLEMENTARY_EVIDENCE_TYPE:
							BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.COMPLEMENTARYType complementary = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeMETAPHORETypeCOMPLEMENTARYType(); 
							complementary.setNAME(""+explanationPhrase.getNode());
							complementary.setCOMMENTS(""+explanationPhrase.getPhrase());
							metaphore.getCOMPLEMENTARY().add(complementary); 
							break;
						case ExplanationPhrase.EXCLUSIVE_EVIDENCE_TYPE:
							BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.EXCLUDENTType excludent = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeMETAPHORETypeEXCLUDENTType(); 
							excludent.setNAME(""+explanationPhrase.getNode());
							excludent.setCOMMENTS(""+explanationPhrase.getPhrase());
							metaphore.getEXCLUDENT().add(excludent); 
							break; 
						case ExplanationPhrase.NECESSARY_EVIDENCE_TYPE:
							BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.ESSENCIALType essencial = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeMETAPHORETypeESSENCIALType(); 
							essencial.setNAME(""+explanationPhrase.getNode());
							essencial.setCOMMENTS(""+explanationPhrase.getPhrase());
							metaphore.getESSENCIAL().add(essencial); 
							break; 
						case ExplanationPhrase.TRIGGER_EVIDENCE_TYPE:
							BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.TRIGGERType trigger = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeMETAPHORETypeTRIGGERType(); 
							trigger.setNAME(""+explanationPhrase.getNode());
							trigger.setCOMMENTS(""+explanationPhrase.getPhrase());
							metaphore.getTRIGGER().add(trigger); 
							break;
						case ExplanationPhrase.NA_EVIDENCE_TYPE:
							BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.NAType na = of.createBIFTypeNETWORKTypeVARIABLESTypeVARTypeMETAPHORETypeNAType(); 
							na.setNAME(""+explanationPhrase.getNode());
							na.setCOMMENTS(""+explanationPhrase.getPhrase());
							metaphore.getNA().add(na); 
							break;         
						} // switch 
					} // for 
					var.getMETAPHORE().add(metaphore); 
				} //if 
				
				networkVariables.getVAR().add(var); 
			}
			else{
				if (node.getType() == Node.DECISION_NODE_TYPE){
					BIFType.NETWORKType.VARIABLESType.DECISIONType decision = of.createBIFTypeNETWORKTypeVARIABLESTypeDECISIONType();	
					decision.setNAME(node.getName());
					decision.setLABEL(node.getDescription()); 
					decision.setTYPE("discrete"); 
					decision.setXPOS((int) node.getPosition().getX());
					decision.setYPOS((int) node.getPosition().getY());
					
					//statename
					for (int j = 0; j < node.getStatesSize(); j++) {
						BIFType.NETWORKType.VARIABLESType.DECISIONType.STATENAMEType statename = of.createBIFTypeNETWORKTypeVARIABLESTypeDECISIONTypeSTATENAMEType(); 
						statename.setValue(node.getStateAt(j));
						decision.getSTATENAME().add(statename);										
					}							
					networkVariables.getDECISION().add(decision); 
				}
				else{   //UTILITY_NODE_TYPE
					BIFType.NETWORKType.VARIABLESType.UTILITYType utility = of.createBIFTypeNETWORKTypeVARIABLESTypeUTILITYType(); 					
					utility.setNAME(node.getName());
					utility.setLABEL(node.getDescription()); 
					utility.setTYPE("discrete"); 
					utility.setXPOS((int) node.getPosition().getX());
					utility.setYPOS((int) node.getPosition().getY());
					
					//statename
					for (int j = 0; j < node.getStatesSize(); j++) {
						BIFType.NETWORKType.VARIABLESType.UTILITYType.STATENAMEType statename = of.createBIFTypeNETWORKTypeVARIABLESTypeUTILITYTypeSTATENAMEType(); 
						statename.setValue(node.getStateAt(j));
						utility.getSTATENAME().add(statename);					
					}				
					networkVariables.getUTILITY().add(utility); 
				}
			}
		}
		
		bifNetwork.getVARIABLES().add(networkVariables); 
		
		// escrita de STRUCTURE
		BIFType.NETWORKType.STRUCTUREType networkStructure = of.createBIFTypeNETWORKTypeSTRUCTUREType();    	 
		
		//para cada ARC
		for (int i = 0; i < net.getEdges().size(); i++) {
			
			BIFType.NETWORKType.STRUCTUREType.ARCType arc = of.createBIFTypeNETWORKTypeSTRUCTURETypeARCType(); 
			
			Edge edge = (Edge) net.getEdges().get(i);
			arc.setPARENT(edge.getOriginNode().getName());
			arc.setCHILD(edge.getDestinationNode().getName());
			
			networkStructure.getARC().add(arc); 
		}
		
		bifNetwork.getSTRUCTURE().add(networkStructure); 
		
		
		// POTENCIAL
		
		BIFType.NETWORKType.POTENTIALType networkPotential = of.createBIFTypeNETWORKTypePOTENTIALType();
		
		for (int i = 0; i < net.getNodeCount(); i++) {
			
			BIFType.NETWORKType.POTENTIALType.POTType pot = of.createBIFTypeNETWORKTypePOTENTIALTypePOTType();
			
			Node node = net.getNodeAt(i);
			
			
			//PRIVATE
			BIFType.NETWORKType.POTENTIALType.POTType.PRIVATEType potPrivate = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypePRIVATEType(); 
			potPrivate.setNAME(node.getName());   //atributo
			pot.getPRIVATE().add(potPrivate); 
			
			if (node.getType() == Node.DECISION_NODE_TYPE) {
				
				NodeList auxListVa = node.getParents();
				int sizeVa = auxListVa.size();
				
				//CONDSET
				BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType condset = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypeCONDSETType(); 
				
				//CONDLEM			
				
				for (int j = 0; j < sizeVa; j++) {
					Node aux = (Node) auxListVa.get(j);
					BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType.CONDLEMType condlen = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypeCONDSETTypeCONDLEMType();
					condlen.setNAME(aux.getName());
					condset.getCONDLEM().add(condlen); 
				}				
				
				pot.getCONDSET().add(condset); 	
				networkPotential.getPOT().add(pot);				
				continue;
			}
			
			//CONDSET
			BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType condset = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypeCONDSETType(); 
			
			//CONDLEM
			PotentialTable table = ((ITabledVariable) node).getPotentialTable();			
			for (int j = 1; j < table.variableCount(); j++) {
				BIFType.NETWORKType.POTENTIALType.POTType.CONDSETType.CONDLEMType condlen = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypeCONDSETTypeCONDLEMType();
				Node parent = table.getVariableAt(j);
				condlen.setNAME(parent.getName());
				condset.getCONDLEM().add(condlen); 
			}
			
			pot.getCONDSET().add(condset); 
			
			
			//DPIS
			BIFType.NETWORKType.POTENTIALType.POTType.DPISType dpis = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypeDPISType(); 
			
			int statesSize = node.getStatesSize();
			int num = table.tableSize() / statesSize;
			
			for (int j = 0; j < num; j++) {
				
				int offset = j*statesSize;
				
				for (int k = 0; k < statesSize; k++) {
					
					BIFType.NETWORKType.POTENTIALType.POTType.DPISType.DPIType dpi = of.createBIFTypeNETWORKTypePOTENTIALTypePOTTypeDPISTypeDPIType(); 
					dpi.setINDEXES(j);				   
					dpi.setValue("" + table.getValue(offset + k));					
					dpis.getDPI().add(dpi);
				}
				
			} //for
			
			pot.getDPIS().add(dpis);
			
			networkPotential.getPOT().add(pot);
		} //for

		bifNetwork.getPOTENTIAL().add(networkPotential); 
		
	} //class
	
	private Node makeNode(BIFType.NETWORKType.VARIABLESType.VARType var){
		
		ProbabilisticNode node = new ProbabilisticNode(); 
		
		node.setName(var.getNAME());
		node.setDescription(var.getLABEL());
		node.setPosition(var.getXPOS(), var.getYPOS());
		
		List lStatename = (ArrayList)var.getSTATENAME(); 
		
		for (int j = 0; j < lStatename.size(); j++){
			BIFType.NETWORKType.VARIABLESType.VARType.STATENAMEType statename = (BIFType.NETWORKType.VARIABLESType.VARType.STATENAMEType)lStatename.get(j);
			node.appendState(statename.getValue()); 
		}
		
		//Tratamento Metaphore
		ArrayList lmetaphore = (ArrayList)var.getMETAPHORE(); 
		if (!(lmetaphore.isEmpty())){
			
			node.setInformationType(Node.EXPLANATION_TYPE);
			BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType metaphore = (BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType)lmetaphore.get(0); 
			node.setExplanationDescription("" + metaphore.getDESCRIPTION().get(0));
			
			/* COMPLEMENTARY */
			ArrayList lcomplementary = (ArrayList)metaphore.getCOMPLEMENTARY(); 
			
			if(!(lcomplementary.isEmpty())){
				for (int m = 0; m < lcomplementary.size(); m++){
					BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.COMPLEMENTARYType complementary = (BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.COMPLEMENTARYType)lcomplementary.get(m); 
					ExplanationPhrase explanationPhrase = new ExplanationPhrase();
					explanationPhrase.setNode(complementary.getNAME());
					explanationPhrase.setEvidenceType(ExplanationPhrase.COMPLEMENTARY_EVIDENCE_TYPE); 
					explanationPhrase.setPhrase(complementary.getCOMMENTS());
					node.addExplanationPhrase(explanationPhrase);
				}	
			}
			
			/* EXCLUDENT */
			ArrayList lexcludent = (ArrayList)metaphore.getEXCLUDENT(); 
			
			if(!(lexcludent.isEmpty())){
				for (int m = 0; m < lexcludent.size(); m++){
					BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.EXCLUDENTType excludent = (BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.EXCLUDENTType)lexcludent.get(m); 
					ExplanationPhrase explanationPhrase = new ExplanationPhrase();
					explanationPhrase.setNode(excludent.getNAME());
					explanationPhrase.setEvidenceType(ExplanationPhrase.EXCLUSIVE_EVIDENCE_TYPE); 
					explanationPhrase.setPhrase(excludent.getCOMMENTS());
					node.addExplanationPhrase(explanationPhrase);
				}	
			}
			
			/* ESSENCIAL */
			ArrayList lessencial = (ArrayList)metaphore.getESSENCIAL(); 
			
			if(!(lessencial.isEmpty())){
				for (int m = 0; m < lessencial.size(); m++){
					BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.ESSENCIALType essencial = (BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.ESSENCIALType)lessencial.get(m); 
					ExplanationPhrase explanationPhrase = new ExplanationPhrase();
					explanationPhrase.setNode(essencial.getNAME());
					explanationPhrase.setEvidenceType(ExplanationPhrase.NECESSARY_EVIDENCE_TYPE); 
					explanationPhrase.setPhrase(essencial.getCOMMENTS());
					node.addExplanationPhrase(explanationPhrase);
				}	
			}
			
			/* TRIGGER */
            ArrayList ltrigger = (ArrayList)metaphore.getTRIGGER(); 
			
			if(!(ltrigger.isEmpty())){
				for (int m = 0; m < ltrigger.size(); m++){
					BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.TRIGGERType trigger = (BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.TRIGGERType)ltrigger.get(m); 
					ExplanationPhrase explanationPhrase = new ExplanationPhrase();
					explanationPhrase.setNode(trigger.getNAME());
					explanationPhrase.setEvidenceType(ExplanationPhrase.TRIGGER_EVIDENCE_TYPE); 
					explanationPhrase.setPhrase(trigger.getCOMMENTS());
					node.addExplanationPhrase(explanationPhrase);
				}	
			}
			
			/* NA */
			ArrayList lna = (ArrayList)metaphore.getNA(); 
			
			if(!(lna.isEmpty())){
				for (int m = 0; m < lna.size(); m++){
					BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.NAType na = (BIFType.NETWORKType.VARIABLESType.VARType.METAPHOREType.NAType)lna.get(m); 
					ExplanationPhrase explanationPhrase = new ExplanationPhrase();
					explanationPhrase.setNode(na.getNAME());
					explanationPhrase.setEvidenceType(ExplanationPhrase.NA_EVIDENCE_TYPE); 
					explanationPhrase.setPhrase(na.getCOMMENTS());
					node.addExplanationPhrase(explanationPhrase);
				}	
			}
		}
		
		PotentialTable auxTabPot = node.getPotentialTable();
		auxTabPot.addVariable(node);
		
		return node; 
	}
	
	private Node makeNodeUtility(BIFType.NETWORKType.VARIABLESType.UTILITYType utility){
		
		UtilityNode node = new UtilityNode(); 
		
		node.setName(utility.getNAME());
		node.setDescription(utility.getLABEL());
		node.setPosition(utility.getXPOS(), utility.getYPOS());
		
		List lStatename = (ArrayList)utility.getSTATENAME(); 
		
		for (int j = 0; j < lStatename.size(); j++){
			BIFType.NETWORKType.VARIABLESType.UTILITYType.STATENAMEType statename = (BIFType.NETWORKType.VARIABLESType.UTILITYType.STATENAMEType)lStatename.get(j);
			node.appendState(statename.getValue()); 
		}
		
		PotentialTable auxTabPot = node.getPotentialTable();
		auxTabPot.addVariable(node);
		
		return node; 
	}
	
	private Node makeNodeDecision(BIFType.NETWORKType.VARIABLESType.DECISIONType decision){
		
		DecisionNode node = new DecisionNode(); 
		node.setName(decision.getNAME());
		node.setDescription(decision.getLABEL());
		node.setPosition(decision.getXPOS(), decision.getYPOS());
		
		List lStatename = (ArrayList)decision.getSTATENAME(); 
		
		for (int j = 0; j < lStatename.size(); j++){
			BIFType.NETWORKType.VARIABLESType.DECISIONType.STATENAMEType statename = (BIFType.NETWORKType.VARIABLESType.DECISIONType.STATENAMEType)lStatename.get(j);
			node.appendState(statename.getValue()); 
		}
		
		return node; 
	}	
	
	/* 
	 * fills the XML model a root element followed by all its children, and aps logos, recursevely
	 * it does the same for all its children. 
	 */
	private void processTreeNode (
			TreeNode node,
			TreeModel model, 
			BIFType.HIERARCHYType hierarchy) throws JAXBException{
		
		ObjectFactory of = new ObjectFactory(); 
		ArrayList<TreeNode> children = new ArrayList<TreeNode>(); //armazena os filhos do nodo 
		
		BIFType.HIERARCHYType.ROOTType root = of.createBIFTypeHIERARCHYTypeROOTType(); 
		
		root.setNAME(node.toString());
		
		//insert children as level and save'em to be searched after
		int childCount = model.getChildCount(node);
		System.out.print("\nFilhos = " + childCount); 
		if (!node.isLeaf()) {
			for (int i = 0; i < childCount; i++) {
				//Insercao
				BIFType.HIERARCHYType.ROOTType.LEVELType level = of.createBIFTypeHIERARCHYTypeROOTTypeLEVELType(); 
				level.setNAME(((TreeNode)model.getChild(node,i)).toString()); 					
				
				//Armazenar para percorre-lo depois
				children.add((TreeNode)model.getChild(node,i)); 
				root.getLEVEL().add(level); 
				
			}
			
			hierarchy.getROOT().add(root); 
			
			//percorrer filhos
			for (int i = 0; i < childCount; i++){
				//Verificar as hierarchias dos filhos
				processTreeNode((TreeNode) model.getChild(node, i), model, hierarchy);	
			}
		}
		
	}
	
	
	private DefaultMutableTreeNode loadHierarchicTree(BIFType.HIERARCHYType hierarchy){
		
		boolean out = false; 
		
		ArrayList lroot = (ArrayList)hierarchy.getROOT(); 
		BIFType.HIERARCHYType.ROOTType rootXML = (BIFType.HIERARCHYType.ROOTType) lroot.get(0); 
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootXML.getNAME()); //rootXML.getNAME()
		
		System.out.println("rootXML.getNAME()" + rootXML.getNAME()); 
		
		DefaultMutableTreeNode atualNode = root;
		
		ArrayList<DefaultMutableTreeNode> rootsTree = new ArrayList<DefaultMutableTreeNode>(); 
		
		rootsTree.add(atualNode); 
		
		for(int i = 0; i < lroot.size(); i++){
			
			BIFType.HIERARCHYType.ROOTType atualRoot = (BIFType.HIERARCHYType.ROOTType) lroot.get(i); 
			
			atualNode = getAtualNode(rootsTree, atualRoot.getNAME());  
			
			ArrayList lchildrens = (ArrayList) atualRoot.getLEVEL();
			//procurar na pilha por elemento que sera o pai 
			
			//preencher filhos
			for(int j = 0; j < lchildrens.size(); j++){
				BIFType.HIERARCHYType.ROOTType.LEVELType children = (BIFType.HIERARCHYType.ROOTType.LEVELType)lchildrens.get(j); 
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(children.getNAME()); 

				atualNode.add(newNode); 
				rootsTree.add(newNode); 
			}		
		}
		return root; 
	}
	
	/* returns a node with specified name and removes it from the list*/
	private DefaultMutableTreeNode getAtualNode(ArrayList<DefaultMutableTreeNode> rootsTree, String name){
	    boolean achou = false;  
		int index = 0; 
	    
		DefaultMutableTreeNode node = null; 
		while (achou == false){
			if (name.compareTo(rootsTree.get(index).toString()) != 0){
				index++; 
			}
			else{
				achou = true;
				node = rootsTree.get(index); 
			}
		}
		rootsTree.remove(index); 
		
		return node; 	
	}
}