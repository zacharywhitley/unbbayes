package unbbayes.model.umpst.rules;

import javax.swing.JList;


public class RulesModel {
	
	private String id;
	private String rulesName;
	private String ruleType;
	private String comments;
	private String author;
	private String date;
	private JList backtracking;

	
	public RulesModel(String id,String rulesName,String ruleType, String comments,
			String author, String date, JList backtracking) {
		
		this.id=id;
		this.rulesName = rulesName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.backtracking = backtracking;
		if (backtracking==null){
			this.setBacktracking(new JList());
		}
	}

	
	/**
	 * @return the backtracking
	 */
	public JList getBacktracking() {
		return backtracking;
	}


	/**
	 * @param backtracking the backtracking to set
	 */
	public void setBacktracking(JList backtracking) {
		this.backtracking = backtracking;
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
	 * @return the rulesName
	 */
	public String getRulesName() {
		return rulesName;
	}

	/**
	 * @param rulesName the rulesName to set
	 */
	public void setRulesName(String rulesName) {
		this.rulesName = rulesName;
	}

	/**
	 * @return the ruleType
	 */
	public String getRuleType() {
		return ruleType;
	}

	/**
	 * @param ruleType the ruleType to set
	 */
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
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
