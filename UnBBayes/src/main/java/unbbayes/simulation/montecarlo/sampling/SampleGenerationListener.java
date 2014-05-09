
package unbbayes.simulation.montecarlo.sampling;

/**
 * This is a listener which will be called when a sample is generated.
 * This can be used to dynamically include new conditions to stop iteration.
 * @author Shou Matsumoto
 */
public interface SampleGenerationListener {

	/**
	 * This method is called by {@link AMonteCarloSampling#start(unbbayes.prs.bn.ProbabilisticNetwork, int, long)
	 * when a sample is generated
	 * @param sampler : the sampler who generated the sample
	 * @param sampleNumber : how many samples were generated so far.
	 * @return if false, sampling process will stop
	 */
	public boolean onSampleGenerated(IMonteCarloSampling sampler, long sampleNumber);
}
