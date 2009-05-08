package unbbayes.prs.mebn.ssbn;


public interface IBuilderLocalDistribution {

	/**
	 * 
	 * @param ssbn Contains a SSBN object with the queries and the findings into 
	 *             the node list. 
	 * @param kb
	 * @return
	 */
	public void buildLocalDistribution(SSBN ssbn); 
	
}
