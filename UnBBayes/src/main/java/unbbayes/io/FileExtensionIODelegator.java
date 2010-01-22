/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unbbayes.io.exception.LoadException;
import unbbayes.io.extension.jpf.PluginAwareFileExtensionIODelegator;
import unbbayes.prs.Graph;

/**
 * This class delegates to a specified IO class by comparing the file extensions,
 * using {@link BaseIO#supportsExtension(String)}.
 * @author Shou Matsumoto
 *
 */
public class FileExtensionIODelegator implements BaseIO {

	private List<BaseIO> delegators;
	
	/**
	 * Default constructor is not public. Use {@link #newInstance()} instead
	 */
	protected FileExtensionIODelegator() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor method.
	 * @return {@link PluginAwareFileExtensionIODelegator}.
	 */
	public static FileExtensionIODelegator newInstance() {
		FileExtensionIODelegator ret = PluginAwareFileExtensionIODelegator.newInstance();
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		if (this.getDelegators() == null || input == null) {
			throw new LoadException();
		}
		
		for (BaseIO io : this.getDelegators()) {
			if (io.supports(input, true)) {
				return io.load(input);
			}
		}
		
		throw new LoadException();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph net) throws IOException {
		if (this.getDelegators() == null || output == null) {
			throw new IOException();
		}
		
		for (BaseIO io : this.getDelegators()) {
			if (io.supports(output, false)) {
				io.save(output, net);
				return;
			}
		}
		
		// if we reach this code, no storing was actually done.
		throw new IOException();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		if (this.getDelegators() == null) {
			return false;
		}
		
		for (BaseIO io : this.getDelegators()) {
			if (io.supports(file, isLoadOnly)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @return the delegators
	 */
	public List<BaseIO> getDelegators() {
		return delegators;
	}

	/**
	 * @param delegators the delegators to set
	 */
	public void setDelegators(List<BaseIO> delegators) {
		this.delegators = delegators;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		ArrayList<String> ret = new ArrayList<String>();
		List<BaseIO> delegators = this.getDelegators();
		if (delegators != null) {
			for (BaseIO io : delegators) {
				String [] delegatorExtensions = io.getSupportedFileExtensions(isLoadOnly);
				if (delegatorExtensions != null) {
					for (String ext : delegatorExtensions) {
						ret.add(ext);
					}
				}
			}
		}
		return ret.toArray(new String[ret.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		String ret = new String();
		List<BaseIO> delegators = this.getDelegators();
		if (delegators != null) {
			for (BaseIO io : delegators) {
				String desc = io.getSupportedFilesDescription(isLoadOnly);
				if (desc != null && (desc.trim().length() > 0)) {
					ret += (desc + ", ");
				}
			}
		}
		if (ret.lastIndexOf(", ") < 0) {
			return ret;
		}
		return ret.substring(0, ret.lastIndexOf(", "));
	}

}
