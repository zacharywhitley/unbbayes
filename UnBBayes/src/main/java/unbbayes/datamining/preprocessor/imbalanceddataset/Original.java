package unbbayes.datamining.preprocessor.imbalanceddataset;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 05/09/2007
 */
public class Original extends Batch {

	private static boolean useRatio = false;
	private static boolean useK = false;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;

	public Original(InstanceSet instanceSet) {
		this(instanceSet, null);
	}

	public Original(InstanceSet instanceSet, PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Original";
	}

	@Override
	protected void run() throws Exception {
		/* Do nothing */
	}

	@Override
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) throws Exception {
		setInstanceSet(instanceSet);
	}

}

