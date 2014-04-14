/**
 * 
 */
package unbbayes.prs.mebn.simulation.sampling;

import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBN.State;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.simulation.sampling.GibbsSampling;

/**
 * @author Shou Matsumoto
 *
 */
public class GibbsSamplingSSBNGenerator extends LaskeySSBNGenerator implements
		ISSBNGeneratorBuilder {
	
	private GibbsSampling gibbsSampler = new GibbsSampling();

	private String name = "SSBN Generator and Gibbs Sampler";
	
	/**
	 * 
	 */
	public GibbsSamplingSSBNGenerator() {
		super(null);
		// initialize laskey algorithm using default parameter values
		LaskeyAlgorithmParameters param = new LaskeyAlgorithmParameters();
		param.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		param.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
		this.setParameters(param);
	}

	/**
	 * @param _parameters
	 */
	public GibbsSamplingSSBNGenerator(LaskeyAlgorithmParameters _parameters) {
		super(_parameters);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This method is customized in order to delegate compilation to {@link GibbsSampling}
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#compileAndInitializeSSBN(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	protected void compileAndInitializeSSBN(SSBN ssbn) throws Exception {
		GibbsSampling sampler = this.getGibbsSampler();
		if (sampler == null) {
			sampler = new GibbsSampling();
		}
		sampler.setNetwork(ssbn.getNetwork());
		// doesn't need to run. We can simply add findings and propagate
//		sampler.run();
		this.addFindings(ssbn);
		sampler.propagate();
		ssbn.setState(State.COMPILED);
	}
	
	/**
	 * Insert the findings to a compiled SSBN.
	 * This is called in {@link #compileAndInitializeSSBN(SSBN)} in order
	 * to convert the MEBN findings to hard evidences in BN.
	 * <br/>
	 * <br/>
	 * Pre-Requisite: 
	 * <br/>
	 * - All the nodes of the list of findings have only one actual value.
	 * 
	 * @param ssbn: a SSBN which has {@link SSBN#getNetwork()} already compiled.
	 * @see #compileAndInitializeSSBN(SSBN)
	 */
	protected void addFindings(SSBN ssbn) throws SSBNNodeGeneralException{
		
		for(SimpleSSBNNode findingNode: ssbn.getFindingList()){
			
			if(findingNode.getProbNode()!=null){ //Not all findings nodes are at the network. 
				TreeVariable node = findingNode.getProbNode();

				String nameState = findingNode.getState().getName(); 

				boolean ok = false; 
				for(int i = 0; i < node.getStatesSize(); i++){
					if(node.getStateAt(i).equals(nameState)){
						if (!node.isMarginalList()) {
							// make sure to initialize marginal list if it was not initialized yet.
							node.initMarginalList();
						}
						node.addFinding(i);
						ok = true; 
						break; 
					}
				}

				if(!ok){
					throw new SSBNNodeGeneralException("Could not add findings to state " + nameState + " of node " + node); 
				}
			}
			
		}
		
		ssbn.setState(State.WITH_FINDINGS); 
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#buildSSBNGenerator()
	 */
	public ISSBNGenerator buildSSBNGenerator() throws InstantiationException {
		return this;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the Gibbs sampler to be used in order to compile the grounded BN (SSBN)
	 * in {@link #compileAndInitializeSSBN(SSBN)}
	 */
	public GibbsSampling getGibbsSampler() {
		return gibbsSampler;
	}

	/**
	 * @param gibbsSampler: the Gibbs sampler to be used in order to compile the grounded BN (SSBN)
	 * in {@link #compileAndInitializeSSBN(SSBN)}
	 */
	public void setGibbsSampler(GibbsSampling gibbsSampler) {
		this.gibbsSampler = gibbsSampler;
	}

}
