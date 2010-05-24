/**
 * 
 */
package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.DSeparationPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;

/**
 * This class implements the algorithm of Dr. Laskey, but resulting in a MSBN
 * 
 * @author rafaelmezzomo
 *
 */
public class LaskeySSMSBNGenerator extends LaskeySSBNGenerator {

	/**
	 * The construtor is procted because we're using fatory method
	 * 
	 * @param parameters
	 */
	protected LaskeySSMSBNGenerator(LaskeyAlgorithmParameters parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
	}
	
	public static ISSBNGenerator newInstance(){
		//Initialize Laskey algorithm usign default parameter values
		LaskeyAlgorithmParameters param = new LaskeyAlgorithmParameters();
		param.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		param.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
		ISSBNGenerator ret = new LaskeySSMSBNGenerator(param);
	
		
		// assure the initialization of prune structure, using default pruners
		List<IPruner> pruners = new ArrayList<IPruner>();
		pruners.add(BarrenNodePruner.newInstance());	// barren node pruning is enabled by default
		pruners.add(DSeparationPruner.newInstance());	// d-separated node pruning is enabled by default
		((LaskeySSMSBNGenerator)ret).setPruneStructure(PruneStructureImpl.newInstance(pruners));
		
		return ret;
	}

}
