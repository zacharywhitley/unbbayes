package unbbayes.model.umpst.requirements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupsModel;

public class HypothesisModel {
	
	private String id;
	private String hypothesisName;
	private String comments;
	private String author;
	private String date;
	private Set<GoalModel> goalRelated;
	private HypothesisModel father;
	private Map<String,HypothesisModel> mapSubHypothesis;
	private Set<EntityModel> fowardTrackingEntity;
	private Set<GroupsModel> fowardTrackingGroups;

	
	public HypothesisModel(String id,String hypothesisName, String comments,String author, String date,Set<GoalModel> goalRelated, 
			HypothesisModel father,Map<String,HypothesisModel> subHypothesis,Set<EntityModel> fowardTrackingEntity,
			 Set<GroupsModel> fowardTrackingGroups) {
		
		this.id=id;
		this.hypothesisName = hypothesisName;
		this.comments = comments;
		this.author = author;
		this.date = date;
		this.father = father;
		this.mapSubHypothesis = subHypothesis;
		if(subHypothesis==null){
			this.setMapSubHypothesis(new HashMap<String, HypothesisModel>());			
		}
		this.goalRelated = goalRelated;
		this.fowardTrackingEntity=fowardTrackingEntity;
		if (fowardTrackingEntity==null){
			this.setFowardTrackingEntity(new HashSet<EntityModel>());
		}
		this.fowardTrackingGroups = fowardTrackingGroups;
		if (fowardTrackingGroups==null){
			this.setFowardTrackingGroups(new HashSet<GroupsModel>());
		}
	}


	
	/**
	 * @return the fowardTrackingGroups
	 */
	public Set<GroupsModel> getFowardTrackingGroups() {
		return fowardTrackingGroups;
	}



	/**
	 * @param fowardTrackingGroups the fowardTrackingGroups to set
	 */
	public void setFowardTrackingGroups(Set<GroupsModel> fowardTrackingGroups) {
		this.fowardTrackingGroups = fowardTrackingGroups;
	}



	/**
	 * @return the mapSubHypothesis
	 */
	public Map<String, HypothesisModel> getMapSubHypothesis() {
		return mapSubHypothesis;
	}


	/**
	 * @param mapSubHypothesis the mapSubHypothesis to set
	 */
	public void setMapSubHypothesis(Map<String, HypothesisModel> mapSubHypothesis) {
		this.mapSubHypothesis = mapSubHypothesis;
	}


	/**
	 * @return the fowardTrackingEntity
	 */
	public Set<EntityModel> getFowardTrackingEntity() {
		return fowardTrackingEntity;
	}


	/**
	 * @param fowardTrackingEntity the fowardTrackingEntity to set
	 */
	public void setFowardTrackingEntity(Set<EntityModel> fowardTrackingEntity) {
		this.fowardTrackingEntity = fowardTrackingEntity;
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
	 * @return the goalRelated
	 */
	public Set<GoalModel> getGoalRelated() {
		return goalRelated;
	}


	/**
	 * @param goalRelated the goalRelated to set
	 */
	public void setGoalRelated(Set<GoalModel> goalRelated) {
		this.goalRelated = goalRelated;
	}


	/**
	 * @return the children
	 */
	public Map<String,HypothesisModel> getSubHypothesis() {
		return mapSubHypothesis;
	}


	/**
	 * @param children the children to set
	 */
	public void setSubHypothesis(Map<String,HypothesisModel> subHypothesis) {
		this.mapSubHypothesis = subHypothesis;
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
	 * @return the father
	 */
	public HypothesisModel getFather() {
		return father;
	}


	/**
	 * @param father the father to set
	 */
	public void setFather(HypothesisModel father) {
		this.father = father;
	}
	
}