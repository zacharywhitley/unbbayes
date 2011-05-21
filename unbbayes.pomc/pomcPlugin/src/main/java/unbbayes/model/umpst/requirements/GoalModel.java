package unbbayes.model.umpst.requirements;

public class GoalModel {
	
	private String goalName;
	private String comments;
	private String author;
	private String date;
	private GoalModel pai;
	
	
	public GoalModel(String goalName, String comments,String author, String date, GoalModel pai) {
		
		this.goalName = goalName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.pai = pai;
		// TODO Auto-generated constructor stub
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
	public void setGoalName(String goalNname) {
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


	/**
	 * @return the pai
	 */
	public GoalModel getPai() {
		return pai;
	}


	/**
	 * @param pai the pai to set
	 */
	public void setPai(GoalModel pai) {
		this.pai = pai;
	}
}
