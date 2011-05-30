package unbbayes.model.umpst.requirements;

import java.util.ArrayList;

public class GoalModel {
	
	private String goalName;
	private String comments;
	private String author;
	private String date;
	private GoalModel goalFather;
	private ArrayList<GoalModel> subgoals ;
	private ArrayList<HypothesisModel> hypothesis;
	
	public GoalModel(String goalName, String comments,String author, String date, GoalModel father,ArrayList<GoalModel> children,ArrayList<HypothesisModel> hypothesis) {
		
		
		this.goalName = goalName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.goalFather = father;
		this.subgoals = children;
		this.hypothesis=hypothesis;
		if ( hypothesis==null ){
			hypothesis = new ArrayList<HypothesisModel>();

		}

		// TODO Auto-generated constructor stub
	}
	
	
	

	
	
	/**
	 * @return the goalFather
	 */
	public GoalModel getGoalFather() {
		return goalFather;
	}






	/**
	 * @param goalFather the goalFather to set
	 */
	public void setGoalFather(GoalModel goalFather) {
		this.goalFather = goalFather;
	}






	/**
	 * @return the subgoals
	 */
	public ArrayList<GoalModel> getSubgoals() {
		return subgoals;
	}






	/**
	 * @param subgoals the subgoals to set
	 */
	public void setSubgoals(ArrayList<GoalModel> subgoals) {
		this.subgoals = subgoals;
	}






	/**
	 * @return the hypothesis
	 */
	public ArrayList<HypothesisModel> getHypothesis() {
		return hypothesis;
	}






	/**
	 * @param hypothesis the hypothesis to set
	 */
	public void setHypothesis(ArrayList<HypothesisModel> hypothesis) {
		this.hypothesis = hypothesis;
	}






	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}


	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}


	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}


	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}


	/**
	 * @return the name
	 */
	public String getGoalName() {
		return goalName;
	}


	/**
	 * @param name the name to set
	 */
	public void setGoalName(String goalName) {
		this.goalName = goalName;
	}


	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}


	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}



}
