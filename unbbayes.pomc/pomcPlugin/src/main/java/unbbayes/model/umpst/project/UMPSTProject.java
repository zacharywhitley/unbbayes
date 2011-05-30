/**
 *   Classe Singleton que contém todos os mapas em memória. 
 * 
 * 
 * 
 * 
 * */




package unbbayes.model.umpst.project;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.gui.umpst.TableGoals;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
 

public class UMPSTProject implements Graph {
	
	private java.util.Map<String,GoalModel> mapGoal;
	private java.util.Map<String,HypothesisModel> mapHypothesis;
	private java.util.Map<String,EntityModel> mapEntity;
	private java.util.Map<String,AtributeModel> mapAtribute;
	private java.util.Map<String,RelationshipModel> mapRelationship;
	
private static UMPSTProject instance = new UMPSTProject();

	
	
	protected UMPSTProject() {
		this.setMapGoal(new HashMap<String,GoalModel>());
		this.setMapHypothesis(new HashMap<String, HypothesisModel>());
		this.setMapEntity(new HashMap<String, EntityModel>());
		this.setMapAtribute(new HashMap<String, AtributeModel>());
		this.setMapRelationship(new HashMap<String, RelationshipModel>());
	}
	
	  
	
	public static  UMPSTProject getInstance(){
	
		return instance;
		
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

}
