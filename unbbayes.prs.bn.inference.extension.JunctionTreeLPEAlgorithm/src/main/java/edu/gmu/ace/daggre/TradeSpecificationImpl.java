/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.List;

/**
 * This is a default implementation of {@link TradeSpecification}.
 * {@link #isCliqueSensitive()} will return false by default.
 * @author Shou Matsumoto
 *
 */
public class TradeSpecificationImpl implements TradeSpecification {

	private static final long serialVersionUID = 3800020097173226814L;
	
	private Long userId;
	private Long questionId;
	private List<Float> probabilities;
	private List<Long> assumptionIds;
	private List<Integer> assumedStates;
	private List<Float> oldProbabilities;
	
	
	/**
	 * Default constructor method using fields
	 * @param userId
	 * @param questionId
	 * @param oldProbabilities
	 * @param probabilities
	 * @param assumptionIds
	 * @param assumedStates
	 */
	public TradeSpecificationImpl(Long userId, Long questionId,
			List<Float> oldProbabilities, List<Float> probabilities, List<Long> assumptionIds,
			List<Integer> assumedStates) {
		super();
		this.userId = userId;
		this.questionId = questionId;
		this.probabilities = probabilities;
		this.assumptionIds = assumptionIds;
		this.assumedStates = assumedStates;
		this.oldProbabilities = oldProbabilities;
	}
	
	/**
	 * Default constructor method using fields, but {@link #oldProbabilities} will
	 * be set to null.
	 * @param userId
	 * @param questionId
	 * @param probabilities
	 * @param assumptionIds
	 * @param assumedStates
	 * @see #TradeSpecificationImpl(Long, Long, List, List, List, List)
	 */
	public TradeSpecificationImpl(Long userId, Long questionId,
			List<Float> probabilities, List<Long> assumptionIds,
			List<Integer> assumedStates) {
		this(userId, questionId, probabilities, probabilities, assumptionIds, assumedStates);
		this.oldProbabilities = null;
	}



	/**
	 * Default constructor without fields
	 */
	public TradeSpecificationImpl() {
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



	/**
	 * @return the oldProbabilities
	 */
	public List<Float> getOldProbabilities() {
		return oldProbabilities;
	}



	/**
	 * @param oldProbabilities the oldProbabilities to set
	 */
	public void setOldProbabilities(List<Float> oldProbabilities) {
		this.oldProbabilities = oldProbabilities;
	}


}
