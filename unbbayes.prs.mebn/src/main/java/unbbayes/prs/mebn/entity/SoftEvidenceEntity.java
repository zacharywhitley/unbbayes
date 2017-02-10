/**
 * 
 */
package unbbayes.prs.mebn.entity;

import java.util.List;

import unbbayes.prs.bn.ILikelihoodExtractor;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.LikelihoodExtractor;

/**
 * @author Shou Matsumoto
 *
 */
public class SoftEvidenceEntity extends Entity {

	

	private List<Float> softEvidence;
	
	/** Object to be used for soft evidence (it sets the probability of a node to desired value without changing conditionals of other nodes) */
	public static final ILikelihoodExtractor DEFAULT_SOFT_EVIDENCE_LIKELIHOOD_EXTRACTOR = JeffreyRuleLikelihoodExtractor.newInstance();
	
	/** Object to be used for likelihood evidence (multiplies current probability with a specified likelihood ratio) */
	public static final ILikelihoodExtractor DEFAULT_LIKELIHOOD_EVIDENCE_LIKELIHOOD_EXTRACTOR = LikelihoodExtractor.newInstance();
	
	private ILikelihoodExtractor likelihoodExtractor = DEFAULT_LIKELIHOOD_EVIDENCE_LIKELIHOOD_EXTRACTOR;

	/**
	 * Entity that represents a soft or likelihood evidence.
	 * @param name: name of entity (required by superclass)
	 * @param softEvidence : list of numbers that either represents probability (if soft evidence) or likelihood ratios (if likelihood evidence)
	 * @param likelihoodExtractor : object that will define how soft evidence will use the likelihoods.
	 * Use {@link #DEFAULT_LIKELIHOOD_EVIDENCE_LIKELIHOOD_EXTRACTOR} for likelihood evidence, and
	 * {@link #DEFAULT_SOFT_EVIDENCE_LIKELIHOOD_EXTRACTOR} for soft evidences.
	 */
	public SoftEvidenceEntity(String name, List<Float> softEvidence, ILikelihoodExtractor likelihoodExtractor) {
		super(name, TypeContainer.typeCategoryLabel);
		// TODO Auto-generated constructor stub
		this.softEvidence = softEvidence;
		this.likelihoodExtractor = likelihoodExtractor;
	}
	
	/**
	 * This wrapps {@link #SoftEvidenceEntity(String, List, ILikelihoodExtractor)} for easier access
	 * @param isLikelihoodEvidence : if true, likelihood evidence will be used. If false, soft evidence will be used.
	 * @see #DEFAULT_LIKELIHOOD_EVIDENCE_LIKELIHOOD_EXTRACTOR
	 * @see #DEFAULT_SOFT_EVIDENCE_LIKELIHOOD_EXTRACTOR
	 */
	public SoftEvidenceEntity(String name, List<Float> softEvidence, boolean isLikelihoodEvidence) {
		this(name, softEvidence, null);
		if (isLikelihoodEvidence) {
			this.likelihoodExtractor = DEFAULT_LIKELIHOOD_EVIDENCE_LIKELIHOOD_EXTRACTOR;
		} else {
			this.likelihoodExtractor = DEFAULT_SOFT_EVIDENCE_LIKELIHOOD_EXTRACTOR;
		}
	}

	/**
	 * @return the softEvidence
	 */
	public List<Float> getSoftEvidence() {
		return softEvidence;
	}

	/**
	 * @param softEvidence the softEvidence to set
	 */
	public void setSoftEvidence(List<Float> softEvidence) {
		this.softEvidence = softEvidence;
	}

	/**
	 * @return the likelihoodExtractor
	 */
	public ILikelihoodExtractor getLikelihoodExtractor() {
		return likelihoodExtractor;
	}

	/**
	 * @param likelihoodExtractor the likelihoodExtractor to set
	 */
	public void setLikelihoodExtractor(ILikelihoodExtractor likelihoodExtractor) {
		this.likelihoodExtractor = likelihoodExtractor;
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.entity.Entity#toString()
	 */
	public String toString() {
		String ret = super.toString();
		List<Float> evidence = getSoftEvidence();
		if (evidence != null && !evidence.isEmpty()) {
			ret += "=" + evidence;
		}
		return ret;
	}

}
