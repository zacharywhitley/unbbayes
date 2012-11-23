package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.MFragContextFailException;

public interface IMFragContextNodeAvaliator {

	
	public MFragInstance evaluateMFragContextNodes(MFragInstance mFragInstance) 
            throws ImplementationRestrictionException, 
                   MFragContextFailException;
	
}
