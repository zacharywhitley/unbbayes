package unbbayes.model.umpst;

import java.io.Serializable;

public class ObjectModel implements Serializable {

	private String id;
	private String name;
	private String comments;
	private String author;
	private String date;
	
	public ObjectModel(String id,String name,
			String comments,
			String author,
			String date){
		
		this.id=id;
		this.name=name;
		this.comments=comments;
		this.author=author;
		this.date=date;
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
	public String getName() {
		return name;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setName(String groupName) {
		this.name = groupName;
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
