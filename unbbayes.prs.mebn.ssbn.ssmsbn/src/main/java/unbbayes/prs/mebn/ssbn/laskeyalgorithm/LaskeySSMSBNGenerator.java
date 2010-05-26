/**
 * 
 */
package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.controller.MSBNController;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.DSeparationPruner;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;
import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.Debug;
import unbbbayes.prs.mebn.ssbn.extension.ssmsbn.SSMSBNBuilderLocalDistribution;

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
		
		// Inicialization with the BuilderLocalDistribution for SSMSBN
		((LaskeySSMSBNGenerator)ret).setBuildLocalDistribution(SSMSBNBuilderLocalDistribution.newInstance());
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#showSSBN(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	protected void showSSBN(SSBN ssbn) {
		AbstractMSBN msbn = (AbstractMSBN)ssbn.getNetwork();
		MSBNController controller = new MSBNController((SingleAgentMSBN)msbn);
		
		this.getMediator().getScreen().getDesktopPane().add(controller.getPanel());
		controller.getPanel().setSize(controller.getPanel().getPreferredSize());
		controller.getPanel().setVisible(true);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#generateSSBN(java.util.List, unbbayes.prs.mebn.kb.KnowledgeBase)
	 */
	public SSBN generateSSBN(List<Query> queryList, KnowledgeBase knowledgeBase) throws SSBNNodeGeneralException, ImplementationRestrictionException, MEBNException, OVInstanceFaultException, InvalidParentException {
		
		SSBN ssbn = null;
		
		try{
			ssbn = super.generateSSBN(queryList, knowledgeBase);
		}catch (NullPointerException e) {
			Debug.println(this.getClass(), "Avoiding erroneous log feature from superclass", e);
		}
		
		try {
			ssbn.compileAndInitializeSSBN();
		} catch (Exception e) {
			throw new MEBNException(e);
		}
		
		// show on display
		this.showSSBN(ssbn);
		
		return ssbn;
	}

	
	
}
