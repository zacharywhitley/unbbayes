/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.Date;
import java.util.List;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineImpl implements MarkovEngineInterface {

	/**
	 * 
	 */
	public MarkovEngineImpl() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#initialize()
	 */
	public boolean initialize() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#startNetworkActions()
	 */
	public long startNetworkActions() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#commitNetworkActions(long)
	 */
	public boolean commitNetworkActions(long transactionKey)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestion(long, java.util.Date, long, int, java.util.List)
	 */
	public boolean addQuestion(long transactionKey, Date occurredWhen,
			long questionId, int numberStates, List<Float> initProbs)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)
	 */
	public boolean addQuestionAssumption(long transactionKey,
			Date occurredWhen, long sourceQuestionId,
			long assumptiveQuestionId, List<Float> cpd)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addCash(long, java.util.Date, long, float, java.lang.String)
	 */
	public boolean addCash(long transactionKey, Date occurredWhen, long userId,
			float assets, String description) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	public List<Float> addTrade(long transactionKey, Date occurredWhen,
			long tradeId, long userId, long questionId, List<Float> oldValues,
			List<Float> newValues, List<Long> assumptionIds,
			List<Integer> assumedStates, Boolean allowNegative)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#resolveQuestion(long, java.util.Date, long, int)
	 */
	public boolean resolveQuestion(long transactionKey, Date occurredWhen,
			long questionID, int settledState) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#revertTrade(long, java.util.Date, java.lang.Long, java.lang.Long)
	 */
	public boolean revertTrade(long transactionKey, Date occurredWhen,
			Long startingTradeId, Long questionId)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbList(long, java.util.List, java.util.List)
	 */
	public List<Float> getProbList(long questionId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getProbsList(java.util.List, java.util.List, java.util.List)
	 */
	public List<List<Float>> getProbsList(List<Long> questionIds,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getPossibleQuestionAssumptions(long, java.util.List)
	 */
	public List<Long> getPossibleQuestionAssumptions(long questionId,
			List<Long> assumptionIds) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getAssetsIfStates(long, long, java.util.List, java.util.List)
	 */
	public List<Float> getAssetsIfStates(long userID, long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getEditLimits(long, long, int, java.util.List, java.util.List)
	 */
	public List<Float> getEditLimits(long userId, long questionId,
			int questionState, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getCash(long, java.util.List, java.util.List)
	 */
	public float getCash(long userId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#assetsCommittedByUserQuestion(long, long, java.util.List, java.util.List)
	 */
	public float assetsCommittedByUserQuestion(long userId, long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#assetsCommittedByUserQuestions(long, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> assetsCommittedByUserQuestions(long userId,
			List<Long> questionId, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)
	 */
	public float scoreUserQuestionEv(long userId, Long questionId,
			List<Long> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#scoreUser(java.util.List, java.util.List, java.util.List)
	 */
	public float scoreUser(List<Long> userIds, List<Long> assumptionIds,
			List<Integer> assumedStates) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#previewTrade(long, long, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	public List<Float> previewTrade(long userID, long questionID,
			List<Float> oldValues, List<Float> newValues,
			List<Integer> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#determineBalancingTrade(long, long, java.util.List, java.util.List)
	 */
	public List<Float> determineBalancingTrade(long userID, long questionID,
			List<Integer> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)
	 */
	public List<QuestionEvent> getQuestionHistory(Long questionID,
			List<Long> assumptionIDs, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.gmu.ace.daggre.MarkovEngineInterface#getScoreDetails(long, java.util.List, java.util.List)
	 */
	public List<ScoreDetail> getScoreDetails(long userId,
			List<Integer> assumptionIds, List<Integer> assumedStates)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
