/**
 * 
 */
package unbbayes.io.msbn.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.SaveException;
import unbbayes.io.msbn.IMSBNIO;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;

/**
 * This method contains routines to load/save MSBN
 * from/to file.
 * These codes were moved from XMLBIFIO to here.
 * @author Shou Matsumoto
 *
 */
public class XMLBIFMSBNIO implements IMSBNIO {

	/** Load resource file from this package. Not static, in order to enable hot plug */
	private  ResourceBundle resource;
	
	private String name = "Folder for MSBN project";
	
	/**
	 * Default constructor is made protected, for plugin support
	 * @deprecated use {@link #getInstance()}
	 */
	protected XMLBIFMSBNIO() {
		resource = unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.io.msbn.resources.Resources.class.getName());
	}
	
	/**
	 * Construction method.
	 * @return
	 */
	public static XMLBIFMSBNIO getInstance() {
		return new XMLBIFMSBNIO();
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

	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.XMLBIFIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException,
			IOException {
		try {
			return this.loadMSBN(input);
		} catch (JAXBException e) {
			throw new LoadException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.XMLBIFIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph graph) throws FileNotFoundException {
		try {
			this.saveMSBN(output, (SingleAgentMSBN)graph);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (Throwable t) {
			// translating ordinal exceptions into catchable FileNotFoundException
			FileNotFoundException newException = new FileNotFoundException();
			newException.setStackTrace(t.getStackTrace());
			newException.initCause(t);
			throw newException;
		} 
	}

	/**
	 * Checks if file extension is compatible to what this i/o expects.
	 * @see #supports(File, boolean)
	 * @param extension
	 * @param isLoadOnly
	 * @return
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		// returns true if there is no extension (file is a folder)
		return extension == null || (extension.trim().length() <= 0);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		// return null, indicating that there is no extension (since it is a folder)
		String [] ret = {};
		return ret;
	}

	/**
	 * 
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return this.resource.getString("netFileFilterSaveMSBN");
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


	
	
}
