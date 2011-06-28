package unbbayes.model.umpst.entities;


import java.util.HashSet;
import java.util.Set;

import javax.swing.JList;

import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.rules.RulesModel;


public class RelationshipModel {
	
	private String id;
	private String comments;
	private String author;
	private String date;
	private JList backtrackingEntity;
	private JList backtrackingAtribute;
	private JList backtrackingGoal;
	private JList backtrackingHypothesis;
	private Set<RulesModel> fowardtrackingRules;
	private Set<GroupsModel> fowardtrackingGroups;

	
	public RelationshipModel(String id,String relationshipName, String comments,String author, String date,
			JList backtrackingEntity, JList backtrackingAtribute,JList backtrackingGoal,JList backtrackingHypothesis,
			Set<RulesModel> fowardtrackingRules,Set<GroupsModel> fowardtrackingGroups) {
		
		this.id=id;
		this.relationshipName = relationshipName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.backtrackingAtribute=backtrackingAtribute;
		if(backtrackingAtribute==null){
			this.setBacktrackingAtribute(new JList());
		}
		this.backtrackingEntity=backtrackingEntity;
		if(backtrackingEntity==null){
			this.setBacktrackingEntity(new JList());
		}
		this.backtrackingGoal=backtrackingGoal;
		if(backtrackingGoal==null){
			this.setBacktrackingGoal(new JList());
		}
		this.backtrackingHypothesis=backtrackingHypothesis;
		if(backtrackingHypothesis==null){
			this.setBacktrackingHypothesis(new JList());
		}
		this.fowardtrackingRules=fowardtrackingRules;
		if(fowardtrackingRules==null){
			this.setFowardtrackingRules(new HashSet<RulesModel>());
		}
		this.fowardtrackingGroups=fowardtrackingGroups;
		if(fowardtrackingGroups==null){
			this.setFowardtrackingGroups(new HashSet<GroupsModel>());
		}
	}
	
	
	


	/**
	 * @return the fowardtrackingRules
	 */
	public Set<RulesModel> getFowardtrackingRules() {
		return fowardtrackingRules;
	}





	/**
	 * @param fowardtrackingRules the fowardtrackingRules to set
	 */
	public void setFowardtrackingRules(Set<RulesModel> fowardtrackingRules) {
		this.fowardtrackingRules = fowardtrackingRules;
	}





	/**
	 * @return the fowardtrackingGroups
	 */
	public Set<GroupsModel> getFowardtrackingGroups() {
		return fowardtrackingGroups;
	}





	/**
	 * @param fowardtrackingGroups the fowardtrackingGroups to set
	 */
	public void setFowardtrackingGroups(Set<GroupsModel> fowardtrackingGroups) {
		this.fowardtrackingGroups = fowardtrackingGroups;
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
	 * @return the backtrackingEntity
	 */
	public JList getBacktrackingEntity() {
		return backtrackingEntity;
	}


	/**
	 * @param backtrackingEntity the backtrackingEntity to set
	 */
	public void setBacktrackingEntity(JList backtrackingEntity) {
		this.backtrackingEntity = backtrackingEntity;
	}


	/**
	 * @return the backtrackingAtribute
	 */
	public JList getBacktrackingAtribute() {
		return backtrackingAtribute;
	}


	/**
	 * @param backtrackingAtribute the backtrackingAtribute to set
	 */
	public void setBacktrackingAtribute(JList backtrackingAtribute) {
		this.backtrackingAtribute = backtrackingAtribute;
	}





	private String relationshipName;
	/**
	 * @return the relationshipName
	 */
	public String getRelationshipName() {
		return relationshipName;
	}


	/**
	 * @param relationshipName the relationshipName to set
	 */
	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
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