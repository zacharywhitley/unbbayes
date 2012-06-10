
package edu.gmu.ace.daggre;

/**
 * This is a <amount, description> 
 * pair that explain how the current score of a user was determined.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface#getScoreDetails(long, java.util.List, java.util.List)
 */
public interface ScoreDetail {
	
	float getAmount();
	
	String getDescription();
}
