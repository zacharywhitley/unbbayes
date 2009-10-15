/**
 * 
 */
package unbbayes.io.msbn.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supportsExtension(java.lang.String)
	 */
	public boolean supportsExtension(String extension) {
		// returns true if there is no extension (file is a folder)
		return extension == null || (extension.trim().length() <= 0);
	}


	

}
