package unbbayes.io.mebn;

import unbbayes.io.mebn.exceptions.IOMebnException;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public interface IProtegeOWLModelUser {
	
	/**
	 * 
	 * @return the last used OWL model
	 */
	public OWLModel getLastOWLModel();
	
	/**
	 * 
	 * @param model OWLModel to set
	 * @throws IOMebnException
	 */
	public void setOWLModelToUse(OWLModel model) throws IOMebnException;
	
}
