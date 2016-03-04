/**
 *   Classe Singleton que contem todos os mapas em memoria. 
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

import unbbayes.model.umpst.ObjectModel;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.implementation.algorithm.MTheoryModel;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;

public class UMPSTProject extends ObjectModel implements Graph, Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	private java.util.Map<String, GoalModel>			mapGoal;
	private java.util.Map<String, HypothesisModel>		mapHypothesis;
	private java.util.Map<String, EntityModel>			mapEntity;
	private java.util.Map<String, AttributeModel>		mapAtribute;
	private java.util.Map<String, RelationshipModel>	mapRelationship;
	private java.util.Map<String, RuleModel>			mapRules;
	private java.util.Map<String, GroupModel>			mapGroups;
	private java.util.Map<String, OrdinaryVariableModel>	mapOrdinaryVariable;
	private java.util.Map<String, CauseVariableModel>	mapCauseVariable;
	private java.util.Map<String, EffectVariableModel>	mapEffectVariable;
	
	private MTheoryModel mtheory;

	private String fileName;
	private String modelName; // it is equal of filename
	
	public UMPSTProject() {

		super(null, null, null, null, null); 
		
		this.setMapGoal(new HashMap<String,GoalModel>());
		this.setMapHypothesis(new HashMap<String, HypothesisModel>());
		this.setMapEntity(new HashMap<String, EntityModel>());
		this.setMapAtribute(new HashMap<String, AttributeModel>());
		this.setMapRelationship(new HashMap<String, RelationshipModel>());
		this.setMapRules(new HashMap<String, RuleModel>());
		this.setMapGroups(new HashMap<String, GroupModel>());
		
		this.setMapOrdinaryVariable(new HashMap<String, OrdinaryVariableModel>());
		this.setMapCauseVariable(new HashMap<String, CauseVariableModel>());
		this.setMapEffectVariable(new HashMap<String, EffectVariableModel>());
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
	
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the mapOrdinaryVariable
	 */
	public java.util.Map<String, OrdinaryVariableModel> getMapOrdinaryVariable() {
		return mapOrdinaryVariable;
	}

	/**
	 * @param mapOrdinaryVariable the mapOrdinaryVariable to set
	 */
	public void setMapOrdinaryVariable(java.util.Map<String, OrdinaryVariableModel> mapOrdinaryVariable) {
		this.mapOrdinaryVariable = mapOrdinaryVariable;
	}

	/**
	 * @return the mapCauseVariable
	 */
	public java.util.Map<String, CauseVariableModel> getMapCauseVariable() {
		return mapCauseVariable;
	}

	/**
	 * @param mapCauseVariable the mapCauseVariable to set
	 */
	public void setMapCauseVariable(java.util.Map<String, CauseVariableModel> mapCauseVariable) {
		this.mapCauseVariable = mapCauseVariable;
	}

	/**
	 * @return the mapEffectVariable
	 */
	public java.util.Map<String, EffectVariableModel> getMapEffectVariable() {
		return mapEffectVariable;
	}

	/**
	 * @param mapEffectVariable the mapEffectVariable to set
	 */
	public void setMapEffectVariable(java.util.Map<String, EffectVariableModel> mapEffectVariable) {
		this.mapEffectVariable = mapEffectVariable;
	}

	/**
	 * @return the modelName
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * @param modelName the modelName to set
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * @return the mtheory
	 */
	public MTheoryModel getMtheory() {
		return mtheory;
	}

	/**
	 * @param mtheory the mtheory to set
	 */
	public void setMtheory(MTheoryModel mtheory) {
		this.mtheory = mtheory;
	}

}
