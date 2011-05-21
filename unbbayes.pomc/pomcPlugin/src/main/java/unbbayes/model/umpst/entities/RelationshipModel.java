package unbbayes.model.umpst.entities;

public class RelationshipModel {
	
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
	 * @return the pai
	 */
	public RelationshipModel getPai() {
		return pai;
	}


	/**
	 * @param pai the pai to set
	 */
	public void setPai(RelationshipModel pai) {
		this.pai = pai;
	}


	private String comments;
	private String author;
	private String date;
	private RelationshipModel pai;
	
	
	public RelationshipModel(String relationshipName, String comments,String author, String date, RelationshipModel pai) {
		
		this.relationshipName = relationshipName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.pai = pai;
		// TODO Auto-generated constructor stub
	}
	
	
	
}