package unbbayes.model.umpst.groups;

import java.util.Set;

import javax.swing.JList;

import unbbayes.model.umpst.requirements.GoalModel;

public class GroupsModel {
	String id;
	String groupName;
	String comments;
	String author;
	String date;
	JList backtrackingGoal;
	JList backtrackingHypothesis;
	
	public GroupsModel(String id,String groupName,String comments,String author,String date, JList backtrackingGoal,
			JList backtrackingHypothesis ){
		this.id=id;
		this.groupName=groupName;
		this.comments=comments;
		this.author=author;
		this.date=date;
		this.backtrackingGoal=backtrackingGoal;
		if (backtrackingGoal==null){
			this.setBacktrackingGoal(new JList());
		}
		this.backtrackingHypothesis=backtrackingHypothesis;
		if (backtrackingHypothesis==null){
			this.setBacktrackingHypothesis(new JList());
		}
		
	}

	/**
	 * @return the backtrackingGoal
	 */
	public JList getBacktrackingGoal() {
		return backtrackingGoal;
	}

	/**
	 * @param backtrackingGoal the backtrackingGoal to set
	 */
	public void setBacktrackingGoal(JList backtrackingGoal) {
		this.backtrackingGoal = backtrackingGoal;
	}

	/**
	 * @return the backtrackingHypothesis
	 */
	public JList getBacktrackingHypothesis() {
		return backtrackingHypothesis;
	}

	/**
	 * @param backtrackingHypothesis the backtrackingHypothesis to set
	 */
	public void setBacktrackingHypothesis(JList backtrackingHypothesis) {
		this.backtrackingHypothesis = backtrackingHypothesis;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	
}
