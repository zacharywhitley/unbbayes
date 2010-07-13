package unbbayes.io.xmlbif.version7;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.UBIOException;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;

/** 
 * Manipulates I/O of XMLBIF file format for version 0.7. Main addition to this version is 
 * the distribution CPS, which stands for Conditional Probability Script.
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */

public class BaseXMLBIFIO implements BaseIO {
	
	/** Load resource file from this package */
	private static ResourceBundle resource =
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.io.resources.IoResources.class.getName());
	
	/** Supported file extension with no dots. The value is {"xml"}  */
	public static final String[] SUPPORTED_EXTENSIONS = {"xml"};
	
	private String name = "Core XMLBIF";
	
	/**
	 * Loads a new Probabilistic network from the input file.
	 * 
	 * @param input the input file for the network
	 * @return The loaded network
	 * @throws LoadException If the file doesn't describes a network.
	 * @throws IOException	If an IO error occurs
	 */
	public ProbabilisticNetwork load(File input) throws LoadException, IOException{
		
		int index = input.getName().lastIndexOf('.');
		String id = input.getName().substring(0, index);
		ProbabilisticNetwork pn = new ProbabilisticNetwork(id);	
		try { 
			XMLBIFIO.loadXML(input, pn);
		} catch (Exception e1) {
			try {
				// Try version 0.6.
				// e.printStackTrace();
				unbbayes.io.xmlbif.version6.XMLBIFIO.loadXML(input, pn);
			} catch (Exception e2) {
				try {
					// Try version 0.5.
					// e.printStackTrace();
					unbbayes.io.xmlbif.version5.XMLBIFIO.loadXML(input, pn);
				} catch (Exception e3) {
					try {
						// Try version 0.4.
						// e2.printStackTrace();
						unbbayes.io.xmlbif.version4.XMLBIFIO.loadXML(input, pn);
					} catch (Exception e4) {
						e3.printStackTrace();
						throw new LoadException(resource.getString("UnsupportedError"));
					}
				}
			}
		}
		return pn; 
	}
	
	
	
	/**
	 * Saves a network to the output file.
	 * @param output	The output file to save
	 * @param graph		The network to save.
	 */
	
	public void save(File output, Graph graph) throws IOException{
		
		SingleEntityNetwork net = (SingleEntityNetwork) graph;
		
		FileWriter outputxml = new FileWriter(output);

		// Saving in older version is not supported.
		// unbbayes.io.xmlbif.version4.XMLBIFIO.saveXML(outputxml, net); 
		// unbbayes.io.xmlbif.version5.XMLBIFIO.saveXML(outputxml, net);
		// unbbayes.io.xmlbif.version6.XMLBIFIO.saveXML(outputxml, net);
		try {
			unbbayes.io.xmlbif.version7.XMLBIFIO.saveXML(outputxml, net);
		} catch (JAXBException e) {
			throw new UBIOException(e);
		}

		outputxml.flush();
		outputxml.close();
	}
	
	

	/**
	 * Checks if file extension is compatible to what this i/o expects.
	 * @see #supports(File, boolean)
	 * @param extension
	 * @param isLoadOnly
	 * @return
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS[0].equalsIgnoreCase(extension);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "XMLBIF (.xml)";
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		String fileExtension = null;
		try {
			int index = file.getName().lastIndexOf(".");
			if (index >= 0) {
				fileExtension = file.getName().substring(index + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return this.supports(fileExtension, isLoadOnly);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
//	----------------------------------------------------------------------------------------
	
}