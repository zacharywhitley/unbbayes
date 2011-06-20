package unbbayes.model.umpst.entities;

import java.util.Set;

import javax.swing.JList;

import org.hsqldb.lib.HashSet;

public class RelationshipModel {
	
	String id;
	private String comments;
	private String author;
	private String date;
	private RelationshipModel father;
	private JList backtrackingEntity;
	private JList backtrackingAtribute;

	
	public RelationshipModel(String id,String relationshipName, String comments,String author, String date, RelationshipModel father,
			JList backtrackingEntity, JList backtrackingAtribute) {
		
		this.id=id;
		this.relationshipName = relationshipName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.father = father;
		this.backtrackingAtribute=backtrackingAtribute;
		if(backtrackingAtribute==null){
			this.setBacktrackingAtribute(new JList());
		}
		this.backtrackingEntity=backtrackingEntity;
		if(backtrackingEntity==null){
			this.setBacktrackingEntity(new JList());
		}
		// TODO Auto-generated constructor stub
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


	/**
	 * @return the father
	 */
	public RelationshipModel getPai() {
		return father;
	}


	/**
	 * @param father the father to set
	 */
	public void setPai(RelationshipModel pai) {
		this.father = pai;
	}

	
	
}