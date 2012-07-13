/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.List;

/**
 * Classes implementing this interface shall represent
 * information which can explain how {@link MarkovEngineInterface#scoreUserQuestionEv(long, Long, List, List)}
 * was calculated.
 * @author Shou Matsumoto
 *
 */
public interface ScoreSummary {
	
	/**
	 * This is a value which can be also computed by {@link MarkovEngineInterface#scoreUserQuestionEv(long, Long, List, List)}
	 * @return
	 */
	float getScoreEV();

	/**
	 * This is a value which can also be computed b {@link MarkovEngineInterface#getCash(long, List, List)}
	 * @return
	 */
	float getCash();
	
	/**
	 * @return list containing a combination of questions and states
	 * which contributes positively to {@link #getScoreEV()}.
	 * The sum of these values minus the values in {@link #getIntersectionScoreComponents()}
	 * correspond to {@link #getScoreEV()}.
	 * <br/><br/>
	 * Technically (in a Bayes Net context), these values represents assets * probabilities in cliques
	 * of a junction tree.
	 */
	List<SummaryContribution> getScoreComponents();
	
	/**
	 * @return list containing a combination of questions and states
	 * which contributes negatively to {@link #getScoreEV()},
	 * due to the fact that these combinations correspond
	 * to "intersections" of the questions in {@link #getScoreComponents()}.
	 * Because they are intersections, the values here
	 * are subtracted from the sum of {@link #getScoreComponents()}
	 * in order to obtain {@link #getScoreEV()}.
	 * <br/><br/>
	 * Technically (in a Bayes Net context), these values represents assets * probabilities in 
	 * separators of the junction tree.
	 */
	List<SummaryContribution> getIntersectionScoreComponents();
	
	/**
	 * This "contribution" is represented in terms of  a collection of nodes and respective states
	 * because the score is now calculated in terms of combinations of states of nodes 
	 * (e.g. node A in state a1 AND node B in state b2 multiplied by the assets of 
	 * node A in state a1 AND node B in state b2) due to the fact that questions are not
	 * independent each other anymore. 
	 * Because of this reason, the score components is not intuitively described in terms of a single node.
	 */
	public interface SummaryContribution {
		/** Questions involved in this score component */
		List<Long> getQuestions();
		/** List of states involved in this score component. They are related to {@link #getQuestions()} by index */
		List<Integer> getStates();
		/** The score value of this component */
		float getContributionToScoreEV();
	}
}
