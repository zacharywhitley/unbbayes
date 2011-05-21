package unbbayes.model.umpst.entities;


public class AtributeModel {
	
	private String atributeName;
	private String comments;
	private String author;
	private String date;
	private AtributeModel pai;
	
	
	public AtributeModel(String atributeName, String comments,String author, String date, AtributeModel pai) {
		
		this.atributeName = atributeName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.pai = pai;
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the atributeName
	 */
	public String getAtributeName() {
		return atributeName;
	}


	/**
	 * @param atributeName the atributeName to set
	 */
	public void setAtributeName(String atributeName) {
		this.atributeName = atributeName;
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
	public AtributeModel getPai() {
		return pai;
	}


	/**
	 * @param pai the pai to set
	 */
	public void setPai(AtributeModel pai) {
		this.pai = pai;
	}
	
	
	
}