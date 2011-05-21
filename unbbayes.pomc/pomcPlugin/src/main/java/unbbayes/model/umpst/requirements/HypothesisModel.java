package unbbayes.model.umpst.requirements;

public class HypothesisModel {
	
	private String hypothesisName;
	private String comments;
	private String author;
	private String date;
	private HypothesisModel pai;
	
	
	public HypothesisModel(String hypothesisName, String comments,String author, String date, HypothesisModel pai) {
		
		this.hypothesisName = hypothesisName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.pai = pai;
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the hypothesisName
	 */
	public String getHypothesisName() {
		return hypothesisName;
	}


	/**
	 * @param hypothesisName the hypothesisName to set
	 */
	public void setHypothesisName(String hypothesisName) {
		this.hypothesisName = hypothesisName;
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
	public HypothesisModel getPai() {
		return pai;
	}


	/**
	 * @param pai the pai to set
	 */
	public void setPai(HypothesisModel pai) {
		this.pai = pai;
	}
	
}