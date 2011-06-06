package unbbayes.model.umpst.project;

import java.util.Set;

import unbbayes.model.umpst.requirements.GoalModel;

public class SearchModelGoal {
	
	private String keyWord;
	private Set<GoalModel> goalsRelated;
	
	public SearchModelGoal(String keyWord, Set<GoalModel> goalsRelated){
		this.keyWord = keyWord;
		this.goalsRelated = goalsRelated;
	}

	/**
	 * @return the keyWord
	 */
	public String getKeyWord() {
		return keyWord;
	}

	/**
	 * @param keyWord the keyWord to set
	 */
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	/**
	 * @return the goalsRelated
	 */
	public Set<GoalModel> getGoalsRelated() {
		return goalsRelated;
	}

	/**
	 * @param goalsRelated the goalsRelated to set
	 */
	public void setGoalsRelated(Set<GoalModel> goalsRelated) {
		this.goalsRelated = goalsRelated;
	}

	
	
	
}
