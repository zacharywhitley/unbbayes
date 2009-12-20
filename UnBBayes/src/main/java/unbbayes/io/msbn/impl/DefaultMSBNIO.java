/**
 * 
 */
package unbbayes.io.msbn.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.msbn.IMSBNIO;
import unbbayes.prs.Graph;
import unbbayes.prs.msbn.SingleAgentMSBN;

/**
 * Default implementation of MSBN I/O class using delegator pattern to {@link NetIO}
 * @author Shou Matsumoto
 *
 */
public class DefaultMSBNIO implements IMSBNIO {

	private NetIO delegator;
	
	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.io.resources.IoResources.class.getName());
	

	/**
	 * The constructor is not public. Use the constructor method {@link #newInstance()} instead.
	 */
	protected DefaultMSBNIO() {
		super();
	}
	
	/**
	 * Default construction method.
	 * @return a new instance of DefaultMSBNIO using {@link #getDelegator()} as a instance of 
	 * {@link NetIO}.
	 */
	public static DefaultMSBNIO newInstance() {
		DefaultMSBNIO ret = new DefaultMSBNIO();
		ret.delegator = new NetIO();
		return ret;
	}
	

	/**
	 * @param input
	 * @return
	 * @throws LoadException
	 * @throws IOException
	 * @see unbbayes.io.NetIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException,
			IOException {
		return this.loadMSBN(input);
	}

	/**
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws LoadException
	 * @see unbbayes.io.NetIO#loadMSBN(java.io.File)
	 */
	public SingleAgentMSBN loadMSBN(File input) throws IOException,
			LoadException {
		return delegator.loadMSBN(input);
	}

	/**
	 * @param output
	 * @param graph
	 * @throws FileNotFoundException
	 * @see unbbayes.io.NetIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph graph) throws FileNotFoundException {
		this.saveMSBN(output, (SingleAgentMSBN)graph);
	}

	/**
	 * @param output
	 * @param msbn
	 * @throws FileNotFoundException
	 * @see unbbayes.io.NetIO#saveMSBN(java.io.File, unbbayes.prs.msbn.SingleAgentMSBN)
	 */
	public void saveMSBN(File output, SingleAgentMSBN msbn)
			throws FileNotFoundException {
		delegator.saveMSBN(output, msbn);
	}

	/**
	 * @return the delegator
	 */
	public NetIO getDelegator() {
		return delegator;
	}

	/**
	 * @param delegator the delegator to set
	 */
	public void setDelegator(NetIO delegator) {
		this.delegator = delegator;
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


	

}
