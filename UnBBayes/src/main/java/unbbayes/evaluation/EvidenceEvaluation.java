package unbbayes.evaluation;

import unbbayes.evaluation.exception.EvaluationException;

public class EvidenceEvaluation {

	private String name;

	private float cost = MemoryEfficientEvaluation.UNSET_VALUE;

	// Individual probability of correct classification
	private float individualPCC = MemoryEfficientEvaluation.UNSET_VALUE;

	// Individual local confusion matrix
	private float[][] LCM;

	// Probability of correct classification of the evidence set without
	// this evidence
	private float marginalPCC = MemoryEfficientEvaluation.UNSET_VALUE;

	// Local confusion matrix of the evidence set without this evidence
	private float[][] marginalCM;

	// The evidence set PCC minus the setPCC (PCC of the set without this
	// evidence)
	private float marginalImprovement = MemoryEfficientEvaluation.UNSET_VALUE;

	// Individual PCC divided by its cost
	private float costRate = MemoryEfficientEvaluation.UNSET_VALUE;

	// Evidence set PCC used to compute the marginal improvement
	private float evidenceSetPcc;

	public EvidenceEvaluation(String name, float evidenceSetPcc) {
		this.name = name;
		this.evidenceSetPcc = evidenceSetPcc;
	}

	public EvidenceEvaluation(String name, float evidenceSetPcc, float cost) {
		this(name, evidenceSetPcc);
		this.cost = cost;
	}

	public float getIndividualPCC() throws EvaluationException {
		if (individualPCC == MemoryEfficientEvaluation.UNSET_VALUE) {
			if (LCM == null) {
				throw new EvaluationException(
						"Must calculate individual LCM before computing individual PCC.");
			}
			individualPCC = 0;
			for (int i = 0; i < LCM.length; i++) {
				individualPCC += LCM[i][i];
			}
			individualPCC /= LCM.length;
		}
		return individualPCC;
	}

	public float getMarginalPCC() throws EvaluationException {
		if (marginalPCC == MemoryEfficientEvaluation.UNSET_VALUE) {
			if (marginalCM == null) {
				throw new EvaluationException(
						"Must calculate marginal LCM before computing marginal PCC.");
			}
			marginalPCC = 0;
			for (int i = 0; i < marginalCM.length; i++) {
				marginalPCC += marginalCM[i][i];
			}
			marginalPCC /= marginalCM.length;
		}
		return marginalPCC;
	}

	public float getMarginalImprovement() throws EvaluationException {
		if (marginalImprovement == MemoryEfficientEvaluation.UNSET_VALUE) {
			marginalImprovement = evidenceSetPcc - getMarginalPCC();
		}
		return marginalImprovement;
	}

	public float getCostRate() throws EvaluationException {
		if (costRate == MemoryEfficientEvaluation.UNSET_VALUE) {
			if (cost == MemoryEfficientEvaluation.UNSET_VALUE) {
				throw new EvaluationException(
						"Must set cost before computing cost rate.");
			}
			try {
				costRate = getIndividualPCC() / cost;
			} catch(EvaluationException e) {
				throw new EvaluationException(
				"Must calculate individual Pcc before computing cost rate." + " " + e.getMessage());
			}
		}
		return costRate;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}

	public float[][] getIndividualLCM() {
		return LCM;
	}

	public void setLCM(float[][] LCM) {
		this.LCM = LCM;
	}

	public float[][] getMarginalCM() {
		return marginalCM;
	}

	public void setMarginalCM(float[][] marginalCM) {
		this.marginalCM = marginalCM;
	}

	public String getName() {
		return name;
	}

}
