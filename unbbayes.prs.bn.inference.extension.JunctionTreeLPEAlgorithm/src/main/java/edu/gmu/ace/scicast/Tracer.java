package edu.gmu.ace.scicast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




/** Class used to trace data which will be printed out */
public class Tracer {
	/** the tracer will print node names starting with this prefix */
	private String nodeNamePrefix = "N";
	
	private int iterationNumber = 0;
	private long userId = -1;
	private float addedCash = 0f;
	private boolean isToResolveQuestion = false;
	private boolean isToRevertTrade = false;
	private List<Long> addedQuestions = new ArrayList<Long>();
	private List<Integer> addedQuestionsStateSize = new ArrayList<Integer>();
	private Long targetQuestion = -1L;
	private int targetState = -1;
	private List<Long> assumptionIds = new ArrayList<Long>();
	private List<Integer> assumedStates = new ArrayList<Integer>();
	private List<Float> editLimit = new ArrayList<Float>();
	private List<Float> targetProb = new ArrayList<Float>();
	private Map<Long, List<Float>> probLists = new HashMap<Long, List<Float>>();
	private Map<Long, List<Float>> probListsAfterResolution = new HashMap<Long, List<Float>>();
	private Map<Long, List<Float>> probListsAfterRevert = new HashMap<Long, List<Float>>();
	private List<UserScoreAndCash> userScoreAndCash = new ArrayList<UserScoreAndCash>();
	private List<UserScoreAndCash> userScoreAndCashAfterResolution = new ArrayList<UserScoreAndCash>();
	private List<UserScoreAndCash> userScoreAndCashAfterRevert = new ArrayList<UserScoreAndCash>();
	private List<TradeSpecification> balanceTradeSpecification = new ArrayList<TradeSpecification>();
	private int resolvedState = -1;
	private int iterationNumberRevertTrade = 0;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// to be used to limit float to 4 faction digits
		DecimalFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(0);
		nf.setGroupingUsed(false);
		
		// separator of iterations
		String out = ((iterationNumber>0)?"###===":"");
		
		out += "\n";
		
//		The first line always shows basic information for the current iteration. And there is one line containing exactly '###===' between iterations.
//		Iteration#
//		How many questions added into the model (0 means no question added)
//		User ID
//		Amount of cash aadding to the user (0 means no adding cash)
//		Transaction ID (0:regular trade; 1:balance trade which can be represented in 1 trade; 2:balance trade which can be represented in 2 trades; 3:balance trade which can be represented in 3 trades; ... so on)
//		Whether or not resolving a question (0: no resolving question; 1: resolving a question)
//		Whether or not we do revert trade in this iteration (0: no revert trade; 1: revert trade)
		out+= iterationNumber + " " + addedQuestions.size() + " " + userId + " " + nf.format(addedCash) + " " 
				+ balanceTradeSpecification.size() + " " + (isToResolveQuestion?"1 ":"0 ") +  (isToRevertTrade?"1":"0");
		
		out += "\n";
		
		// added questions
		if (addedQuestions != null && !addedQuestions.isEmpty()) {
			for (Long questionId : addedQuestions) {
				out += getNodeNamePrefix() + questionId + " ";
			}
			out += "\n";
			for (Integer size : addedQuestionsStateSize) {
				out += size + " ";
			}
			out += "\n";
		}
		
		
		// treat normal trades and balance trades equally
		List<TradeSpecification> trades = balanceTradeSpecification;
		if (trades == null || trades.isEmpty()) {
			// normal trade
			if (targetProb.isEmpty()) {
				if (targetProb.isEmpty()) {
					throw new IllegalStateException("Target probability must be specified before calling this method");
				}
			}
			trades = (List)Collections.singletonList(new TradeSpecificationImpl(userId, targetQuestion, targetProb, assumptionIds, assumedStates));
		} 
		
		for (TradeSpecification spec : trades) {
			// (target var. and its state, and the last number means how many assumption)
			out += getNodeNamePrefix() + spec.getQuestionId() + " " + targetState + " " + spec.getAssumptionIds().size();
			out += "\n";
			
			// assumptions
			if (spec.getAssumptionIds() != null && !spec.getAssumptionIds().isEmpty()) {
				for (Long assumptionId : spec.getAssumptionIds()) {
					out += getNodeNamePrefix()+assumptionId + " ";
				}
				out += "\n";
				// assumed states
				for (Integer state : spec.getAssumedStates()) {
					out += state + " ";
				}
				
				out += "\n";
			}
			
			if (balanceTradeSpecification == null || balanceTradeSpecification.isEmpty()) {
				// normal trade needs edit limits
				for (Float limit : editLimit) {
					out += nf.format(limit) + " ";
				}
				out += "\n";
			}
			
			// (target prob. vector)
			if (spec.getProbabilities() == null || spec.getProbabilities().isEmpty()) {
				throw new RuntimeException("Probability was set to empty");
			}
			if (balanceTradeSpecification != null && !balanceTradeSpecification.isEmpty()) {
				// print without using number format (print full precision)
				for (Float prob : spec.getProbabilities()) {
					out += prob + " ";
				}
			} else {
				// print with normal precision (use number format)
				for (Float prob : spec.getProbabilities()) {
					out += nf.format(prob) + " ";
				}
			}
			out += "\n";
		}
		
		// (marginal prob. vector in whatever order)
		for (Long questionId : probLists.keySet()) {
			out += getNodeNamePrefix() + questionId + " " + probLists.get(questionId).size();
			out += "\n";
			for (Float prob : probLists.get(questionId)) {
				out += nf.format(prob) + " ";
			}
			out += "\n";
		}
		
		// (all active users scoreEV, cash in whatever order)
		for (UserScoreAndCash scoreCash : userScoreAndCash) {
			out += scoreCash.userId + " " + nf.format(scoreCash.score) + " "+ nf.format(scoreCash.cash);
			out += "\n";
		}
		
		// resolve question
		if (isToResolveQuestion) {
			out += getNodeNamePrefix() + targetQuestion;
			out += "\n";
			out += resolvedState;
			out += "\n";
			
			// prob after resolution
			for (Long questionId : probListsAfterResolution.keySet()) {
				out += getNodeNamePrefix() + questionId + " " + probListsAfterResolution.get(questionId).size();
				out += "\n";
				for (Float prob : probListsAfterResolution.get(questionId)) {
					out += nf.format(prob) + " ";
				}
				out += "\n";
			}
			// (all active users scoreEV, cash in whatever order)
			for (UserScoreAndCash scoreCash : userScoreAndCashAfterResolution) {
				out += scoreCash.userId + " " + nf.format(scoreCash.score) + " "+ nf.format(scoreCash.cash);
				out += "\n";
			}
		}
		
		// revert trade
		if (isToRevertTrade) {
			out += iterationNumberRevertTrade;
			out += "\n";
			
			// (all active users scoreEV, cash in whatever order)
			for (UserScoreAndCash scoreCash : userScoreAndCashAfterRevert) {
				out += scoreCash.userId + " " + nf.format(scoreCash.score) + " "+ nf.format(scoreCash.cash);
				out += "\n";
			}
			// prob after revert
			for (Long questionId : probListsAfterRevert.keySet()) {
				out += getNodeNamePrefix() + questionId + " " + probListsAfterRevert.get(questionId).size();
				out += "\n";
				for (Float prob : probListsAfterRevert.get(questionId)) {
					out += nf.format(prob) + " ";
				}
				out += "\n";
			}
			
		}
		
		return out;
	}
	
	/** Triple storing user's score and cash */
	public class UserScoreAndCash {
		public Long userId = -1L;
		public float score = Float.NaN;
		public float cash = Float.NaN;
		public UserScoreAndCash(Long userId, float score, float cash) {
			super();
			this.userId = userId;
			this.score = score;
			this.cash = cash;
		}
	}

	/**
	 * @return the iterationNumber
	 */
	public int getIterationNumber() {
		return iterationNumber;
	}

	/**
	 * @param iterationNumber the iterationNumber to set
	 */
	public void setIterationNumber(int iterationNumber) {
		this.iterationNumber = iterationNumber;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

	/**
	 * @return the addedCash
	 */
	public float getAddedCash() {
		return addedCash;
	}

	/**
	 * @param addedCash the addedCash to set
	 */
	public void setAddedCash(float addedCash) {
		this.addedCash = addedCash;
	}

	/**
	 * @return the isToResolveQuestion
	 */
	public boolean isToResolveQuestion() {
		return isToResolveQuestion;
	}

	/**
	 * @param isToResolveQuestion the isToResolveQuestion to set
	 */
	public void setToResolveQuestion(boolean isToResolveQuestion) {
		this.isToResolveQuestion = isToResolveQuestion;
	}

	/**
	 * @return the isToRevertTrade
	 */
	public boolean isToRevertTrade() {
		return isToRevertTrade;
	}

	/**
	 * @param isToRevertTrade the isToRevertTrade to set
	 */
	public void setToRevertTrade(boolean isToRevertTrade) {
		this.isToRevertTrade = isToRevertTrade;
	}

	/**
	 * @return the addedQuestions
	 */
	public List<Long> getAddedQuestions() {
		return addedQuestions;
	}

	/**
	 * @param addedQuestions the addedQuestions to set
	 */
	public void setAddedQuestions(List<Long> addedQuestions) {
		this.addedQuestions = addedQuestions;
	}

	/**
	 * @return the addedQuestionsStateSize
	 */
	public List<Integer> getAddedQuestionsStateSize() {
		return addedQuestionsStateSize;
	}

	/**
	 * @param addedQuestionsStateSize the addedQuestionsStateSize to set
	 */
	public void setAddedQuestionsStateSize(List<Integer> addedQuestionsStateSize) {
		this.addedQuestionsStateSize = addedQuestionsStateSize;
	}

	/**
	 * @return the targetQuestion
	 */
	public Long getQuestionId() {
		return targetQuestion;
	}

	/**
	 * @param targetQuestion the targetQuestion to set
	 */
	public void setQuestionId(Long targetQuestion) {
		this.targetQuestion = targetQuestion;
	}

	/**
	 * @return the targetState
	 */
	public int getTargetState() {
		return targetState;
	}

	/**
	 * @param targetState the targetState to set
	 */
	public void setTargetState(int targetState) {
		this.targetState = targetState;
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
	 * @return the editLimit
	 */
	public List<Float> getEditLimit() {
		return editLimit;
	}

	/**
	 * @param editLimit the editLimit to set
	 */
	public void setEditLimit(List<Float> editLimit) {
		this.editLimit = editLimit;
	}

	/**
	 * @return the targetProb
	 */
	public List<Float> getTargetProb() {
		return targetProb;
	}

	/**
	 * @param targetProb the targetProb to set
	 */
	public void setTargetProb(List<Float> targetProb) {
		this.targetProb = targetProb;
	}

	/**
	 * @return the probLists
	 */
	public Map<Long, List<Float>> getProbLists() {
		return probLists;
	}

	/**
	 * @param probLists the probLists to set
	 */
	public void setProbLists(Map<Long, List<Float>> probLists) {
		this.probLists = probLists;
	}

	/**
	 * @return the probListsAfterResolution
	 */
	public Map<Long, List<Float>> getProbListsAfterResolution() {
		return probListsAfterResolution;
	}

	/**
	 * @param probListsAfterResolution the probListsAfterResolution to set
	 */
	public void setProbListsAfterResolution(
			Map<Long, List<Float>> probListsAfterResolution) {
		this.probListsAfterResolution = probListsAfterResolution;
	}

	/**
	 * @return the probListsAfterRevert
	 */
	public Map<Long, List<Float>> getProbListsAfterRevert() {
		return probListsAfterRevert;
	}

	/**
	 * @param probListsAfterRevert the probListsAfterRevert to set
	 */
	public void setProbListsAfterRevert(Map<Long, List<Float>> probListsAfterRevert) {
		this.probListsAfterRevert = probListsAfterRevert;
	}

	/**
	 * @return the userScoreAndCash
	 */
	public List<UserScoreAndCash> getUserScoreAndCash() {
		return userScoreAndCash;
	}

	/**
	 * @param userScoreAndCash the userScoreAndCash to set
	 */
	public void setUserScoreAndCash(List<UserScoreAndCash> userScoreAndCash) {
		this.userScoreAndCash = userScoreAndCash;
	}

	/**
	 * @return the userScoreAndCashAfterResolution
	 */
	public List<UserScoreAndCash> getUserScoreAndCashAfterResolution() {
		return userScoreAndCashAfterResolution;
	}

	/**
	 * @param userScoreAndCashAfterResolution the userScoreAndCashAfterResolution to set
	 */
	public void setUserScoreAndCashAfterResolution(
			List<UserScoreAndCash> userScoreAndCashAfterResolution) {
		this.userScoreAndCashAfterResolution = userScoreAndCashAfterResolution;
	}

	/**
	 * @return the userScoreAndCashAfterRevert
	 */
	public List<UserScoreAndCash> getUserScoreAndCashAfterRevert() {
		return userScoreAndCashAfterRevert;
	}

	/**
	 * @param userScoreAndCashAfterRevert the userScoreAndCashAfterRevert to set
	 */
	public void setUserScoreAndCashAfterRevert(
			List<UserScoreAndCash> userScoreAndCashAfterRevert) {
		this.userScoreAndCashAfterRevert = userScoreAndCashAfterRevert;
	}

	/**
	 * @return the balanceTradeSpecification
	 */
	public List<TradeSpecification> getBalanceTradeSpecification() {
		return balanceTradeSpecification;
	}

	/**
	 * @param balanceTradeSpecification the balanceTradeSpecification to set
	 */
	public void setBalanceTradeSpecification(
			List<TradeSpecification> balanceTradeSpecification) {
		this.balanceTradeSpecification = balanceTradeSpecification;
	}


	/**
	 * @return the resolvedState
	 */
	public int getResolvedState() {
		return resolvedState;
	}

	/**
	 * @param resolvedState the resolvedState to set
	 */
	public void setResolvedState(int resolvedState) {
		this.resolvedState = resolvedState;
	}

	/**
	 * @return the iterationNumberRevertTrade
	 */
	public int getIterationNumberRevertTrade() {
		return iterationNumberRevertTrade;
	}

	/**
	 * @param iterationNumberRevertTrade the iterationNumberRevertTrade to set
	 */
	public void setIterationNumberRevertTrade(int iterationNumberRevertTrade) {
		this.iterationNumberRevertTrade = iterationNumberRevertTrade;
	}

	/**
	 * {@link #toString()} will append this prefix to node names. 
	 * @return the nodeNamePrefix
	 */
	public String getNodeNamePrefix() {
		return nodeNamePrefix;
	}

	/**
	 * 
	 * {@link #toString()} will append this prefix to node names.
	 * @param nodeNamePrefix the nodeNamePrefix to set
	 */
	public void setNodeNamePrefix(String nodeNamePrefix) {
		this.nodeNamePrefix = nodeNamePrefix;
	}
	
	
}
