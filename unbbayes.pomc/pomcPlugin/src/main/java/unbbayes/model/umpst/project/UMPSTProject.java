/**
 *   Classe Singleton que contém todos os mapas em memória. 
 * 
 * 
 * 
 * 
 * */




package unbbayes.model.umpst.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RuleModel;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;

public class UMPSTProject implements Graph, Serializable {
	
	private java.util.Map<String,GoalModel>          mapGoal;
	private java.util.Map<String,HypothesisModel>    mapHypothesis;
	private java.util.Map<String,EntityModel>        mapEntity;
	private java.util.Map<String,AttributeModel>     mapAtribute;
	private java.util.Map<String,RelationshipModel>  mapRelationship;
	private java.util.Map<String, RuleModel>         mapRules;
	private java.util.Map<String, GroupModel>        mapGroups;
	
	private transient java.util.Map<String, SearchModelGoal>   mapSearchGoal;
	private transient java.util.Map<String, SearchModelRules>  mapSearchRules;
	private transient java.util.Map<String, SearchModelGroup>  mapSearchGroups;	
	
	private static UMPSTProject umpstProject;
	
	protected UMPSTProject() {
		
		this.setMapGoal(new HashMap<String,GoalModel>());
		this.setMapHypothesis(new HashMap<String, HypothesisModel>());
		this.setMapEntity(new HashMap<String, EntityModel>());
		this.setMapAtribute(new HashMap<String, AttributeModel>());
		this.setMapRelationship(new HashMap<String, RelationshipModel>());
		this.setMapRules(new HashMap<String, RuleModel>());
		this.setMapGroups(new HashMap<String, GroupModel>());
		this.setMapSearchGoal(new HashMap<String, SearchModelGoal>());
		this.setMapSearchRules(new HashMap<String, SearchModelRules>());
		this.setMapSearchGroups(new HashMap<String, SearchModelGroup>());
		
	}
	
	public static UMPSTProject  newInstance() {
		  
		  return (new UMPSTProject());
	}
	
	public void setUMPSTProject(UMPSTProject umpstProject) {
		UMPSTProject.umpstProject = umpstProject;
	}
	
	public UMPSTProject getUMPSTProject() {
		return UMPSTProject.umpstProject;
	}


	/**
	 * @return the mapGroups
	 */
	public java.util.Map<String, GroupModel> getMapGroups() {
		return mapGroups;
	}



	/**
	 * @param mapGroups the mapGroups to set
	 */
	public void setMapGroups(java.util.Map<String, GroupModel> mapGroups) {
		this.mapGroups = mapGroups;
	}



	/**
	 * @return the mapSearchGroups
	 */
	public java.util.Map<String, SearchModelGroup> getMapSearchGroups() {
		return mapSearchGroups;
	}



	/**
	 * @param mapSearchGroups the mapSearchGroups to set
	 */
	public void setMapSearchGroups(
			java.util.Map<String, SearchModelGroup> mapSearchGroups) {
		this.mapSearchGroups = mapSearchGroups;
	}



	/**
	 * @return the mapRules
	 */
	public java.util.Map<String, RuleModel> getMapRules() {
		return mapRules;
	}



	/**
	 * @param mapRules the mapRules to set
	 */
	public void setMapRules(java.util.Map<String, RuleModel> mapRules) {
		this.mapRules = mapRules;
	}



	/**
	 * @return the mapSearchRules
	 */
	public java.util.Map<String, SearchModelRules> getMapSearchRules() {
		return mapSearchRules;
	}



	/**
	 * @param mapSearchRules the mapSearchRules to set
	 */
	public void setMapSearchRules(
			java.util.Map<String, SearchModelRules> mapSearchRules) {
		this.mapSearchRules = mapSearchRules;
	}



	/**
	 * @return the mapSearchGoal
	 */
	public java.util.Map<String, SearchModelGoal> getMapSearchGoal() {
		return mapSearchGoal;
	}



	/**
	 * @param mapSearchGoal the mapSearchGoal to set
	 */
	public void setMapSearchGoal(
			java.util.Map<String, SearchModelGoal> mapSearchGoal) {
		this.mapSearchGoal = mapSearchGoal;
	}


	/**
	 * @return the mapGoal
	 */
	public java.util.Map<String, GoalModel> getMapGoal() {
		return mapGoal;
	}

	/**
	 * @param mapGoal the mapGoal to set
	 */
	public void setMapGoal(java.util.Map<String, GoalModel> mapGoal) {
		this.mapGoal = mapGoal;
	}

	/**
	 * @return the mapHypothesis
	 */
	public java.util.Map<String, HypothesisModel> getMapHypothesis() {
		return mapHypothesis;
	}

	/**
	 * @param mapHypothesis the mapHypothesis to set
	 */
	public void setMapHypothesis(
			java.util.Map<String, HypothesisModel> mapHypothesis) {
		this.mapHypothesis = mapHypothesis;
	}

	/**
	 * @return the mapEntity
	 */
	public java.util.Map<String, EntityModel> getMapEntity() {
		return mapEntity;
	}

	/**
	 * @param mapEntity the mapEntity to set
	 */
	public void setMapEntity(java.util.Map<String, EntityModel> mapEntity) {
		this.mapEntity = mapEntity;
	}

	/**
	 * @return the mapAtribute
	 */
	public java.util.Map<String, AttributeModel> getMapAtribute() {
		return mapAtribute;
	}

	/**
	 * @param mapAtribute the mapAtribute to set
	 */
	public void setMapAtribute(java.util.Map<String, AttributeModel> mapAtribute) {
		this.mapAtribute = mapAtribute;
	}

	/**
	 * @return the mapRelationship
	 */
	public java.util.Map<String, RelationshipModel> getMapRelationship() {
		return mapRelationship;
	}

	/**
	 * @param mapRelationship the mapRelationship to set
	 */
	public void setMapRelationship(
			java.util.Map<String, RelationshipModel> mapRelationship) {
		this.mapRelationship = mapRelationship;
	}

	public void addEdge(Edge arco) throws Exception {
		// TODO Auto-generated method stub

	}

	public void addNode(Node no) {
		// TODO Auto-generated method stub

	}

	public List<Edge> getEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<Node> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public int hasEdge(Node no1, Node no2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void removeEdge(Edge arco) {
		// TODO Auto-generated method stub

	}

	public void removeNode(Node elemento) {
		// TODO Auto-generated method stub

	}

	public void addProperty(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void removeProperty(String name) {
		// TODO Auto-generated method stub
		
	}

	public void clearProperty() {
		// TODO Auto-generated method stub
		
	}

	public Object getProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
