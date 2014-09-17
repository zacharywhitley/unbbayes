package unbbayes.controller.umpst;

import java.util.List;

import javax.swing.JOptionPane;

import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

public class Controller {

	private static Controller singleton;

	private static UMPSTProject umpstProject;

	private String author = ""; 

	public static Controller getInstance(UMPSTProject _umpstProject) {
		
		if (umpstProject != _umpstProject) {
			singleton = new Controller(_umpstProject);
		}else{
			if(_umpstProject == null ){
				if(singleton == null){
					singleton = new Controller(null);
				}
			}
		}
		
		return singleton;
	}

	private Controller(UMPSTProject _umpstProject){
		this.umpstProject = _umpstProject; 
	}
	
	public void setUMPSTProject(UMPSTProject _umpstProject){
		this.umpstProject = _umpstProject; 
	}

	public void deleteGoal(String key) {
		GoalModel goalToBeDeleted = this.umpstProject.getMapGoal().get(key);

		//Remove external references. 
		if (goalToBeDeleted.getGoalFather()!=null){
			goalToBeDeleted.getGoalFather().getSubgoals().remove(goalToBeDeleted.getId());
		}

		if(goalToBeDeleted.getSubgoals() !=null){
			int numberSubgoal = goalToBeDeleted.getSubgoals().size();
			for (int i = 1; i < numberSubgoal; i++) {
				if (goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).getGoalFather()!=null){
					goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).getGoalFather().getSubgoals().remove(goalToBeDeleted.getId());
				}

				goalToBeDeleted.getSubgoals().get(goalToBeDeleted.getId()+"."+i).setGoalFather(null);
			}
		}

		this.umpstProject.getMapGoal().remove(goalToBeDeleted.getId());
	}
	
	public EntityModel createNewEntity(String titleText, 
			String commentsText,
			String authorText, 
			String dateText){

		String idAux = "";
		int intAux = 0;

		int tamanho = umpstProject.getMapEntity().size()+1;

		if ( umpstProject.getMapEntity().size()!=0){
			idAux = tamanho+"";
		}
		else{
			idAux = "1";
		}

		EntityModel newEntity = new EntityModel(
				idAux,
				titleText,
				commentsText, 
				authorText, 
				dateText);

		umpstProject.getMapEntity().put(newEntity.getId(), 
				newEntity);	

		return newEntity;
	}
	
	public void showErrorMessageDialog(String message){
		JOptionPane.showMessageDialog(null, 
				message, 
				"UnBBayes UMP", 
				JOptionPane.ERROR_MESSAGE); 
	}
	
	public void showSucessMessageDialog(String message){
		JOptionPane.showMessageDialog(null, 
				message, 
				"UnBBayes UMP", 
				JOptionPane.INFORMATION_MESSAGE); 
	}

	public void deleteEntity(String key) {

		EntityModel entityToBeDeleted = umpstProject.getMapEntity().get(key);

		/*Updating goalRelated foward tracking*/
		if (entityToBeDeleted.getBacktrackingGoalList()!=null){

			List<GoalModel> listModels = entityToBeDeleted.getBacktrackingGoalList();

			for(GoalModel goalModel: listModels){
				goalModel.getFowardTrackingEntity().remove(entityToBeDeleted);
			}

		}

		umpstProject.getMapEntity().remove(entityToBeDeleted.getId());
	}
	
	
	public void addGoalToEntityBackTrackingList(EntityModel entity, 
			GoalModel goal){
		
		entity.getBacktrackingGoalList().add(goal); 
		goal.getFowardTrackingEntity().add(entity); 
		
	}
	
	public void removeGoalFromEntityBackTrackingList(EntityModel entity, 
			GoalModel goal){
		
		entity.getBacktrackingGoalList().remove(goal); 
		goal.getFowardTrackingEntity().remove(entity); 
		
	}
	
	public void addHypothesisToEntityBackTrackingList(EntityModel entity, 
			HypothesisModel hypothesis){
		
		entity.getBacktrackingHypothesis().add(hypothesis); 
		hypothesis.getFowardTrackingEntity().add(entity); 
		
	}
	
	public void removeHypothesisFromEntityBackTrackingList(EntityModel entity, 
			HypothesisModel hypothesis){
		
		entity.getBacktrackingHypothesis().remove(hypothesis); 
		hypothesis.getFowardTrackingEntity().remove(entity); 
		
	}
	
	public void addEntityToRuleBackTrackingList(EntityModel entity, 
			RuleModel rule){
		
		rule.addBacktrackingEntity(entity);
		entity.getFowardTrackingRules().add(rule); 
		
	}
	
	public void removeEntityFromRuleBackTrackingList(EntityModel entity, 
			RuleModel rule){
		
		entity.getFowardTrackingRules().remove(rule); 
		rule.removeBacktrackingEntity(entity); 
		
	}

	public void addAttributeToRuleBackTrackingList(AttributeModel attribute, 
			RuleModel rule){

		rule.addBacktrackingAttibute(attribute); 
		attribute.getFowardTrackingRules().add(rule); 
		
	}
	
	public void removeAttributeFromRuleBackTrackingList(AttributeModel attribute, 
			RuleModel rule){
		
		attribute.getFowardTrackingRules().remove(rule); 
		rule.removeBacktrackingAttibute(attribute); 
		
	}
	
	public void addRelationshipToRuleBackTrackingList(RelationshipModel relationship, 
			RuleModel rule){

		rule.addBacktrackingRelationship(relationship); 
		relationship.getFowardtrackingRules().add(rule); 
		
	}
	
	public void removeRelationshipFromRuleBackTrackingList(RelationshipModel relationship, 
			RuleModel rule){
		
		relationship.getFowardtrackingRules().remove(rule); 
		rule.removeBacktrackingRelationship(relationship); 
		
	}

	public void addRuleToRuleBackTrackingList(RuleModel ruleChildren, 
			RuleModel rule){

		ruleChildren.getFatherRuleList().add(rule);  
		rule.getChildrenRuleList().add(ruleChildren);  
		
	}
	
	public void removeRuleFromRuleBackTrackingList(RuleModel ruleModel, 
			RuleModel rule){
		
		ruleModel.getFatherRuleList().remove(rule);  
		rule.getFatherRuleList().remove(ruleModel);  
		
	}
	
	public void removeEntityFromGroupBackTrackingList(EntityModel entity, 
			GroupModel group){
		
		group.getBacktrackingEntities().remove(entity); 
		entity.getFowardTrackingGroups().remove(group); 
		
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

}
