/**
 *   Classe Singleton que contém todos os mapas em memória. 
 * 
 * 
 * 
 * 
 * */




package unbbayes.model.umpst.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RulesModel;
 

public class UMPSTProject implements Graph {
	
	private java.util.Map<String,GoalModel> mapGoal;
	private java.util.Map<String,HypothesisModel> mapHypothesis;
	private java.util.Map<String,EntityModel> mapEntity;
	private java.util.Map<String,AtributeModel> mapAtribute;
	private java.util.Map<String,RelationshipModel> mapRelationship;
	private java.util.Map<String, RulesModel> mapRules;
	private java.util.Map<String, GroupsModel> mapGroups;
	private java.util.Map<String, SearchModelGoal> mapSearchGoal;
	private java.util.Map<String, SearchModelEntity> mapSearchEntity;
	private java.util.Map<String, SearchModelRules> mapSearchRules;
	private java.util.Map<String, SearchModelGroup> mapSearchGroups;

	
	
//private static UMPSTProject instance = new UMPSTProject();

	
	
	protected UMPSTProject() {
		this.setMapGoal(new HashMap<String,GoalModel>());
		this.setMapHypothesis(new HashMap<String, HypothesisModel>());
		this.setMapEntity(new HashMap<String, EntityModel>());
		this.setMapAtribute(new HashMap<String, AtributeModel>());
		this.setMapRelationship(new HashMap<String, RelationshipModel>());
		this.setMapRules(new HashMap<String, RulesModel>());
		this.setMapGroups(new HashMap<String, GroupsModel>());
		this.setMapSearchGoal(new HashMap<String, SearchModelGoal>());
		this.setMapSearchEntity(new HashMap<String, SearchModelEntity>());
		this.setMapSearchRules(new HashMap<String, SearchModelRules>());
		this.setMapSearchGroups(new HashMap<String, SearchModelGroup>());
		
		
	}
	

	
	  
	static UMPSTProject umpstProject;
	public static UMPSTProject  newInstance() {
		  
		  return (new UMPSTProject());
	}
	
	public void setUMPSTProject(UMPSTProject umpstProject) {
		UMPSTProject.umpstProject = umpstProject;
	}
	
	public UMPSTProject getUMPSTProject() {
		return UMPSTProject.umpstProject;
	}
	
	
	/*public static  UMPSTProject getInstance(){
	
		return instance;
		
	}*/
	
	



	/**
	 * @return the mapGroups
	 */
	public java.util.Map<String, GroupsModel> getMapGroups() {
		return mapGroups;
	}



	/**
	 * @param mapGroups the mapGroups to set
	 */
	public void setMapGroups(java.util.Map<String, GroupsModel> mapGroups) {
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
	public java.util.Map<String, RulesModel> getMapRules() {
		return mapRules;
	}



	/**
	 * @param mapRules the mapRules to set
	 */
	public void setMapRules(java.util.Map<String, RulesModel> mapRules) {
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
	 * @return the mapSearchEntity
	 */
	public java.util.Map<String, SearchModelEntity> getMapSearchEntity() {
		return mapSearchEntity;
	}



	/**
	 * @param mapSearchEntity the mapSearchEntity to set
	 */
	public void setMapSearchEntity(
			java.util.Map<String, SearchModelEntity> mapSearchEntity) {
		this.mapSearchEntity = mapSearchEntity;
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
	public java.util.Map<String, AtributeModel> getMapAtribute() {
		return mapAtribute;
	}





	/**
	 * @param mapAtribute the mapAtribute to set
	 */
	public void setMapAtribute(java.util.Map<String, AtributeModel> mapAtribute) {
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
