/**
 * 
 */
package unbbayes.io.umpst.implementation;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.UbfIO2;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * @author diego
 *
 */
public class FileBuildPROWL2 extends SaverPrOwlIO {
	
	private File file;
	private MultiEntityBayesianNetwork mebn;

	/**
	 * @deprecated
	 */
	public FileBuildPROWL2(File file, MultiEntityBayesianNetwork mebn) {
		// TODO Auto-generated constructor stub
		super();
		this.file = file;
		this.mebn = mebn;
//		saveMebn();
	}
}
