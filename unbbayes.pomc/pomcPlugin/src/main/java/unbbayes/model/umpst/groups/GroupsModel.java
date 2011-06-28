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
	JList backtrackingEntities;
	JList backtrackingAtributes;
	JList backtrackingRelationship;
	JList backtrackingRules;
	
	public GroupsModel(String id,String groupName,String comments,String author,String date, JList backtrackingGoal,
			JList backtrackingHypothesis,JList backtrackingEntities,JList backtrackingAtributes,JList backtrackingRelationship,
			JList backtrackingRules ){
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
		
		this.backtrackingEntities=backtrackingEntities;
		if (backtrackingEntities==null){
			this.setBacktrackingEntities(new JList());
		}
		this.backtrackingAtributes=backtrackingAtributes;
		if (backtrackingAtributes==null){
			this.setBacktrackingAtributes(new JList());
		}
		this.backtrackingRelationship=backtrackingRelationship;
		if (backtrackingRelationship==null){
			this.setBacktrackingRelationship(new JList());
		}
		this.backtrackingRules=backtrackingRules;
		if (backtrackingRules==null){
			this.setBacktrackingRules(new JList());
		}
		
	}

	
	
	/**
	 * @return the backtrackingEntities
	 */
	public JList getBacktrackingEntities() {
		return backtrackingEntities;
	}



	/**
	 * @param backtrackingEntities the backtrackingEntities to set
	 */
	public void setBacktrackingEntities(JList backtrackingEntities) {
		this.backtrackingEntities = backtrackingEntities;
	}



	/**
	 * @return the backtrackingAtributes
	 */
	public JList getBacktrackingAtributes() {
		return backtrackingAtributes;
	}



	/**
	 * @param backtrackingAtributes the backtrackingAtributes to set
	 */
	public void setBacktrackingAtributes(JList backtrackingAtributes) {
		this.backtrackingAtributes = backtrackingAtributes;
	}



	/**
	 * @return the backtrackingRelationship
	 */
	public JList getBacktrackingRelationship() {
		return backtrackingRelationship;
	}



	/**
	 * @param backtrackingRelationship the backtrackingRelationship to set
	 */
	public void setBacktrackingRelationship(JList backtrackingRelationship) {
		this.backtrackingRelationship = backtrackingRelationship;
	}



	/**
	 * @return the backtrackingRules
	 */
	public JList getBacktrackingRules() {
		return backtrackingRules;
	}



	/**
	 * @param backtrackingRules the backtrackingRules to set
	 */
	public void setBacktrackingRules(JList backtrackingRules) {
		this.backtrackingRules = backtrackingRules;
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
