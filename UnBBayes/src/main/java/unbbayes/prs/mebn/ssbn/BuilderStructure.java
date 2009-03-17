package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.kb.KnowledgeBase;

public interface BuilderStructure {

	/**
	 * 
	 * @param ssbn Contains a SSBN object with the queries and the findings into 
	 *             the node list. 
	 * @param kb
	 * @return
	 */
	public boolean buildStructure(SSBN ssbn, 
			KnowledgeBase kb); 
	
}
