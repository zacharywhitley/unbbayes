package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import unbbayes.prs.mebn.ssbn.Parameters;

/**
 * Parameters for the Laskey's SSBN algorithm. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class LaskeyAlgorithmParameters extends Parameters{
	
	//Select the parts of the algorithm to execute
	
	/**
	 * Do the initialization step. 
	 * <p>
	 * Domain = {true, false} <br> 
	 * Default Value = true <br> 
	 */
	public static final int DO_INITIALIZATION     = 0x0002;
	
	/**
	 * Do the builder grand bn step. 
	 * <p>
	 * Domain = {true, false} <br> 
	 * Default Value = true <br> 
	 */	
	public static final int DO_BUILDER            = 0x0003;
	
	/**
	 * Do the prune grand bn step. 
	 * <p>
	 * Domain = {true, false} <br> 
	 * Default Value = true <br> 
	 */
	public static final int DO_PRUNE              = 0x0004;
	
	/**
	 * Do the cpt generation step. 
	 * <p>
	 * Domain = {true, false} <br> 
	 * Default Value = true <br> 
	 */
	public static final int DO_CPT_GENERATION     = 0x0005;
	
	//Others aspects variables/extensibles of the algorithm
	public static final int USE_USER_INTERATION   = 0x0006; 
	
	//Limits for avoid memory or time overflow. 
	public static final int RECURSIVE_LIMIT       = 0x0007;
	
	/**
	 * Max of nodes to be created in the grand bn generation. 
	 * <p>
	 * Domain = integer <br> 
	 * Default Value = 1000 <br> 
	 */
	public static final int NUMBER_NODES_LIMIT    = 0x0008; 
	
	public LaskeyAlgorithmParameters(){
		addParameter(NUMBER_NODES_LIMIT, "100000");
		
		addParameter(DO_INITIALIZATION, "true"); 
		addParameter(DO_BUILDER, "true"); 
		addParameter(DO_PRUNE, "true"); 
		addParameter(DO_CPT_GENERATION, "true"); 
		
		addParameter(USE_USER_INTERATION, "false"); 
	}
	
}
