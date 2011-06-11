package unbbayes.model.umpst.entities;

import java.util.HashMap;
import java.util.Map;

import unbbayes.controller.umpst.EnitiesController;


public class AtributeModel {
	
	private String id;
	private String atributeName;
	private String comments;
	private String author;
	private String date;
	private AtributeModel father;
	private EntityModel entityRelated;
	private Map<String, AtributeModel> mapSubAtributes;
	
	
	public AtributeModel(String id,String atributeName, String comments,String author, String date,EntityModel entityRelated,
			AtributeModel father,Map<String,AtributeModel> mapSubAtributes) {
		
		this.id=id;
		this.atributeName = atributeName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.father = father;
		this.entityRelated=entityRelated;
		this.mapSubAtributes=mapSubAtributes;
		if (mapSubAtributes==null){
			this.setMapSubAtributes(new HashMap<String, AtributeModel>());
		}
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the entityRelated
	 */
	public EntityModel getEntityRelated() {
		return entityRelated;
	}


	/**
	 * @param entityRelated the entityRelated to set
	 */
	public void setEntityRelated(EntityModel entityRelated) {
		this.entityRelated = entityRelated;
	}


	/**
	 * @return the mapSubAtributes
	 */
	public Map<String, AtributeModel> getMapSubAtributes() {
		return mapSubAtributes;
	}


	/**
	 * @param mapSubAtributes the mapSubAtributes to set
	 */
	public void setMapSubAtributes(Map<String, AtributeModel> mapSubAtributes) {
		this.mapSubAtributes = mapSubAtributes;
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
	 * @return the father
	 */
	public AtributeModel getFather() {
		return father;
	}


	/**
	 * @param father the father to set
	 */
	public void setFather(AtributeModel father) {
		this.father = father;
	}
	
	
	
}