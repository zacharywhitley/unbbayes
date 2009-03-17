package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import unbbayes.prs.mebn.ssbn.Parameters;

/**
 * Parameters for the Laskey's SSBN algorithm. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class LaskeyAlgorithmParameters extends Parameters{

	public static final int RECURSIVE_LIMIT       = 0x0001;
	
	//Select the parts of the algorithm to execute
	public static final int DO_BUILDER            = 0x0002; 
	public static final int DO_PRUNE              = 0x0003; 
	public static final int DO_CPT_GENERATION     = 0x0004;
	
	public LaskeyAlgorithmParameters(){
		addParameter(RECURSIVE_LIMIT, "100");
		
		addParameter(DO_BUILDER, "true"); 
		addParameter(DO_PRUNE, "true"); 
		addParameter(DO_CPT_GENERATION, "true"); 
	}
	
}
