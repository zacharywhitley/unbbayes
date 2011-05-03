/**
 * 
 */
package unbbayes.io.mebn;

import java.io.File;

import unbbayes.io.mebn.protege.Protege41CompatiblePROWLIO;


/**
 * This class implements UBFIO for prowl1 written in OWL2, but it
 * only supports file loading.
 * @author Shou Matsumoto
 *
 */
public class UbfIO2ForPROWLInOWL2 extends UbfIO2 {

	/**
	 * @deprecated
	 */
	public UbfIO2ForPROWLInOWL2() {
		super();
		try {
			this.setProwlIO(Protege41CompatiblePROWLIO.newInstance());		// load PR-OWL ontology in OWL2 using protege
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Construction method for {@link UbfIO2ForPROWLInOWL2}
	 * @return {@link UbfIO2ForPROWLInOWL2} instance
	 */
	public static UbfIO2 getInstance() {
		UbfIO2 ret = new UbfIO2ForPROWLInOWL2();
		return ret;
	}

	/**
	 * This class only supports loading.
	 * @see unbbayes.io.mebn.UbfIO2#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		if (isLoadOnly) {
			return super.getSupportedFileExtensions(isLoadOnly);
		}
		return new String[0];
	}

	/**
	 * This class only supports loading.
	 * @see unbbayes.io.mebn.UbfIO#supports(java.lang.String, boolean)
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		if (isLoadOnly) {
			return super.supports(extension, isLoadOnly);
		}
		return false;
	}

	/**
	 * This class only supports loading.
	 * @see unbbayes.io.mebn.UbfIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		if (isLoadOnly) {
			return super.getSupportedFilesDescription(isLoadOnly);
		}
		return "";
	}

	/**
	 * This class only supports loading.
	 * @see unbbayes.io.mebn.UbfIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		if (isLoadOnly) {
			return super.supports(file, isLoadOnly);
		}
		return false;
	}
	
	

}
