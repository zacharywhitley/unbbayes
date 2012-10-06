/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.List;

/**
 * This is a default implementation of {@link TradeSpecification}
 * @author Shou Matsumoto
 *
 */
public class TradeSpecificationImpl implements TradeSpecification {

	private Long userId;
	private Long questionId;
	private List<Float> probabilities;
	private List<Long> assumptionIds;
	private List<Integer> assumedStates;
	
	
	/**
	 * Default constructor method using fields
	 * @param userId
	 * @param questionId
	 * @param probabilities
	 * @param assumptionIds
	 * @param assumedStates
	 */
	public TradeSpecificationImpl(Long userId, Long questionId,
			List<Float> probabilities, List<Long> assumptionIds,
			List<Integer> assumedStates) {
		super();
		this.userId = userId;
		this.questionId = questionId;
		this.probabilities = probabilities;
		this.assumptionIds = assumptionIds;
		this.assumedStates = assumedStates;
	}



	/**
	 * Default constructor is at least "protected", in order to allow
	 * easy inheritance
	 */
	protected TradeSpecificationImpl() {
		// TODO Auto-generated constructor stub
	}



	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}



	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}



	/**
	 * @return the questionId
	 */
	public Long getQuestionId() {
		return questionId;
	}



	/**
	 * @param questionId the questionId to set
	 */
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}



	/**
	 * @return the probabilities
	 */
	public List<Float> getProbabilities() {
		return probabilities;
	}



	/**
	 * @param probabilities the probabilities to set
	 */
	public void setProbabilities(List<Float> probabilities) {
		if (probabilities == null || probabilities.isEmpty()) {
			throw new IllegalStateException("Cannot set probabilities to empty/null");
		}
		this.probabilities = probabilities;
	}



	/**
	 * @return the assumptionIds
	 */
	public List<Long> getAssumptionIds() {
		return assumptionIds;
	}



	/**
	 * @param assumptionIds the assumptionIds to set
	 */
	public void setAssumptionIds(List<Long> assumptionIds) {
		this.assumptionIds = assumptionIds;
	}



	/**
	 * @return the assumedStates
	 */
	public List<Integer> getAssumedStates() {
		return assumedStates;
	}



	/**
	 * @param assumedStates the assumedStates to set
	 */
	public void setAssumedStates(List<Integer> assumedStates) {
		this.assumedStates = assumedStates;
	}


}
