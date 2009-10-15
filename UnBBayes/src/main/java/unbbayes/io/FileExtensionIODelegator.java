/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unbbayes.io.exception.LoadException;
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
	 * Initializes the {@link #getDelegators()} using the following IO classes:
	 * 		- {@link NetIO};
	 * 		- {@link XMLBIFIO};
	 * 		- {@link DneIO};
	 * @return a new instance of FileExtensionIODelegator.
	 */
	public static FileExtensionIODelegator newInstance() {
		FileExtensionIODelegator ret = new FileExtensionIODelegator();
		ret.setDelegators(new ArrayList<BaseIO>());
		ret.getDelegators().add(new NetIO());
		ret.getDelegators().add(new XMLBIFIO());
		ret.getDelegators().add(new DneIO());
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		if (this.getDelegators() == null || input == null) {
			throw new LoadException();
		}
		
		// extract file extension
		String filename = input.getName();
		String extension = null;
		try {
			extension = filename.substring(filename.lastIndexOf('.')+1, filename.length());
		} catch (Exception e) {
			// assume this is a folder...
		}

		for (BaseIO io : this.getDelegators()) {
			if (io.supportsExtension(extension)) {
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
		
		// extract file extension
		String filename = output.getName();
		String extension = null;
		try {
			extension = filename.substring(filename.lastIndexOf('.')+1, filename.length());
		} catch (Exception e) {
			// assume this is a folder...
		}

		for (BaseIO io : this.getDelegators()) {
			if (io.supportsExtension(extension)) {
				io.save(output, net);
				return;
			}
		}
		
		// if we reach this code, no storing was actually done.
		throw new IOException();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supportsExtension(java.lang.String)
	 */
	public boolean supportsExtension(String extension) {
		if (this.getDelegators() == null) {
			return false;
		}
		
		for (BaseIO io : this.getDelegators()) {
			if (io.supportsExtension(extension)) {
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

}
