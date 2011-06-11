package unbbayes.model.umpst.entities;

import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;

public class EntityModel {
	
	private String id;
	private String entityName;
	private String comments;
	private String author;
	private String date;
	private EntityModel father;
	private Map<String,AtributeModel> mapAtributes;
	private JList backtracking;
	
	
	public EntityModel(String id,String entityName, String comments,String author, String date, 
			EntityModel father,Map<String,AtributeModel> mapAtributes, JList backtracking) {
		
		this.id           = id;
		this.entityName   = entityName;
		this.comments     = comments;
		this.author       = author;
		this.date         = date;
		this.father       = father;
		this.mapAtributes = mapAtributes;
		if (mapAtributes==null){
			this.setMapAtributes(new HashMap<String, AtributeModel>());
		}
		this.backtracking = backtracking;
		if (backtracking == null){
			this.setBacktracking(new JList());
		}
		// TODO Auto-generated constructor stub
	}


	
	/**
	 * @return the mapAtributes
	 */
	public Map<String, AtributeModel> getMapAtributes() {
		return mapAtributes;
	}



	/**
	 * @param mapAtributes the mapAtributes to set
	 */
	public void setMapAtributes(Map<String, AtributeModel> mapAtributes) {
		this.mapAtributes = mapAtributes;
	}



	/**
	 * @return the father
	 */
	public EntityModel getFather() {
		return father;
	}


	/**
	 * @param father the father to set
	 */
	public void setFather(EntityModel father) {
		this.father = father;
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


	
	
	
}