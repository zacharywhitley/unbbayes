package unbbayes.controller.mebn;

import java.util.List;

import unbbayes.io.mebn.MebnIO;
import unbbayes.prs.mebn.kb.KnowledgeBase;

public interface MEBNFactory {

	/**
	 * @return A list with all the reserved words of the implementation of MEBN. 
	 *         This words can't be used in names of MEBN elements.  
	 */
	public List<String> getReservedWords(); 

	/**
	 * @return The knowledge base implementation. 
	 */
	public KnowledgeBase getKnowlegeBase(); 
	
	/**
	 * @return The implementation for the IO of a MEBN model. 
	 */
	public MebnIO getMebnIO(); 
	
}
