/*
 * Created on 26/06/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import unbbayes.prs.bn.Network;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;

/**
 * @author Shigeki
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XMLIO implements BaseIO {

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public ProbabilisticNetwork load(File input) throws LoadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#loadMSBN(java.io.File)
	 */
	public SingleAgentMSBN loadMSBN(File input)
		throws LoadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.bn.Network)
	 */
	public void save(File output, Network net) throws FileNotFoundException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#saveMSBN(java.io.File, unbbayes.prs.msbn.SingleAgentMSBN)
	 */
	public void saveMSBN(File output, SingleAgentMSBN net)
		throws FileNotFoundException {
		// TODO Auto-generated method stub

	}

}
