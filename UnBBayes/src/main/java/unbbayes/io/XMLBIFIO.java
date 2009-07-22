/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.io;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.SaveException;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;

/** 
 * Manipulates I/O of XMLBIF file format.
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @author Laecio Lima dos Santos (laecio@gmail.com) (version 2.0)
 * @author Shigeki (version 1.0)
 * @version 3.0
 */

public class XMLBIFIO implements BaseIO{
	
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
		try { 
			unbbayes.io.xmlbif.version6.XMLBIFIO.loadXML(input, pn);
		} catch (Exception e) {
			try {
				// Try version 0.5.
				e.printStackTrace();
				unbbayes.io.xmlbif.version5.XMLBIFIO.loadXML(input, pn);
			} catch (Exception e2) {
				// Try version 0.4.
				try {
					e2.printStackTrace();
					unbbayes.io.xmlbif.version4.XMLBIFIO.loadXML(input, pn);
				} catch (Exception e3) {
					e3.printStackTrace();
					throw new LoadException(resource.getString("UnsupportedError"));
				}
			}
		}
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
					try {
						unbbayes.io.xmlbif.version6.XMLBIFIO.loadXML(files[i], net);
					} catch (Exception e) {
						// Try version 0.5.
						try {
							e.printStackTrace();
							unbbayes.io.xmlbif.version5.XMLBIFIO.loadXML(files[i], net);
						} catch (Exception e2) {
							// Try version 0.4.
							try {
								e2.printStackTrace();
								unbbayes.io.xmlbif.version4.XMLBIFIO.loadXML(files[i], net);
							} catch (Exception e3) {
								e3.printStackTrace();
								throw new LoadException(resource.getString("UnsupportedError"));
							}
						}
					}
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

		// Saving in older version is not supported.
		// unbbayes.io.xmlbif.version4.XMLBIFIO.saveXML(outputxml, net); 
		// unbbayes.io.xmlbif.version5.XMLBIFIO.saveXML(outputxml, net);
		unbbayes.io.xmlbif.version6.XMLBIFIO.saveXML(outputxml, net);

		outputxml.flush();
		outputxml.close();
	}
	
	/**
	 * Saves a MSBN to the output directory.
	 * @param output The output file to save
	 * @param net		The MSBN to save.
	 * @throws SaveException If the output is not a directory.
	 */
	public void saveMSBN(File output, SingleAgentMSBN msbn) throws FileNotFoundException, IOException, JAXBException, SaveException{
		if (! output.isDirectory()) {
			throw new SaveException(resource.getString("IsNotDirectoryException"));
		}
		for (int i = msbn.getNetCount()-1; i>=0; i--) {
			SingleEntityNetwork net = msbn.getNetAt(i);
			File out = new File(output, net.getId() + ".xml");
			save(out, net);
		}    
	}
	
	
//	----------------------------------------------------------------------------------------
	
}