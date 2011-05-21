package unbbayes.model.umpst.entities;

import unbbayes.model.umpst.requirements.GoalModel;

public class EntityModel {
	
	private String entityName;
	private String comments;
	private String author;
	private String date;
	private EntityModel pai;
	
	
	public EntityModel(String entityName, String comments,String author, String date, EntityModel pai) {
		
		this.entityName = entityName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.pai = pai;
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}


	/**
	 * @param entityName the entityName to set
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
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
	 * @return the pai
	 */
	public EntityModel getPai() {
		return pai;
	}


	/**
	 * @param pai the pai to set
	 */
	public void setPai(EntityModel pai) {
		this.pai = pai;
	}
	
	
	
}