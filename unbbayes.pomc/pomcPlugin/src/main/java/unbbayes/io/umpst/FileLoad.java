package unbbayes.io.umpst;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RulesModel;

public class FileLoad {

	private GoalModel goal;
	private HypothesisModel hypothesis;
	private EntityModel entity;
	private GroupsModel group;
	private AtributeModel atribute;
	private RelationshipModel relationship;
	private RulesModel rule;

	public  UMPSTProject loadUbf(File file,UMPSTProject umpstProject) {

		//File file = new File("images/file.ump");
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedReader bufferReader = null;
		String id,goalName,author,date,comments,idFather;


		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			bufferReader = new BufferedReader(new InputStreamReader(fis));

			// dis.available() returns 0 if the file does not have more lines.

			bufferReader.readLine();  //"Number of goals in the map:"
			int numberGoals = Integer.parseInt(bufferReader.readLine());
			if (numberGoals>0)
				bufferReader.readLine(); //All goals in the map

			for (int i = 0; i < numberGoals; i++) {
				id = bufferReader.readLine();
				goal = new GoalModel(id, "","", "", "" , null, null, null, null, null,null );
				umpstProject.getMapGoal().put(goal.getId(), goal);
			}

			bufferReader.readLine();//Number of hypothesis cadastred
			int numberHypothesis = Integer.parseInt(bufferReader.readLine());
			if (numberHypothesis>0)
				bufferReader.readLine();//IDs of all hypothesis

			for (int i = 0; i < numberHypothesis; i++) {
				id = bufferReader.readLine();
				hypothesis = new HypothesisModel(id, "", "", "", "", null, null, null, null, null);
				umpstProject.getMapHypothesis().put(hypothesis.getId(), hypothesis);
			}

			bufferReader.readLine(); //Number of entites cadastred
			int numberEntities = Integer.parseInt(bufferReader.readLine());
			if (numberEntities>0)
				bufferReader.readLine();//IDs of all entities

			for (int i = 0; i < numberEntities; i++) {
				id = bufferReader.readLine();
				entity = new EntityModel(id, "", "", "", "", null, null, null, null, null, null);
				umpstProject.getMapEntity().put(entity.getId(), entity);

			}

			bufferReader.readLine(); //Number of atributes cadastred
			int numberAtributes = Integer.parseInt(bufferReader.readLine());
			if (numberAtributes>0)
				bufferReader.readLine();//IDs of all atributes

			for (int i = 0; i < numberAtributes; i++) {
				id = bufferReader.readLine();
				atribute = new AtributeModel(id, "", "", "", "", null, null, null, null,null,null);
				umpstProject.getMapAtribute().put(atribute.getId(), atribute);

			}

			bufferReader.readLine(); //Number of relationship cadastred
			int numberRelationship = Integer.parseInt(bufferReader.readLine());
			if (numberRelationship>0)
				bufferReader.readLine();//IDs of all relationship

			for (int i = 0; i < numberRelationship; i++) {
				id = bufferReader.readLine();
				relationship = new RelationshipModel(id, "", "", "", "", null, null, null, null, null, null);
				umpstProject.getMapRelationship().put(relationship.getId(), relationship);

			}

			bufferReader.readLine(); //Number of rules cadastred
			int numberRules = Integer.parseInt(bufferReader.readLine());
			if (numberRules>0)
				bufferReader.readLine();//IDs of all rules

			for (int i = 0; i < numberRules; i++) {
				id = bufferReader.readLine();
				rule = new RulesModel(id, "", "", "", "","", null, null, null, null);
				umpstProject.getMapRules().put(rule.getId(), rule);

			}

			bufferReader.readLine(); //Number of Groups cadastred
			int numberGroups = Integer.parseInt(bufferReader.readLine());
			if (numberGroups>0)
				bufferReader.readLine();//IDs of all groups

			for (int i = 0; i < numberGroups; i++) {
				id = bufferReader.readLine(); 
				group = new GroupsModel(id, "", "", "", "", null, null, null, null, null, null);
				umpstProject.getMapGroups().put(group.getId(), group);
			}

			/*--Groups details--*/	      

			bufferReader.readLine(); //************
			bufferReader.readLine(); //Groups detail's

			for (int i = 0; i < numberGroups; i++) {
				id = bufferReader.readLine();
				String groupName = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();

				group = umpstProject.getMapGroups().get(id);
				group.setGroupName(groupName);
				group.setAuthor(author);
				group.setDate(date);
				group.setComments(comments);

				/*--Backtacking from goal--*/

				bufferReader.readLine();//Number of backtracking from goal
				int numberBackGoal =Integer.parseInt(bufferReader.readLine());
				if (numberBackGoal>0){
					bufferReader.readLine();//Backtracking from goals of this group
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackGoal; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					group.setBacktrackingGoal(list);
				}

				/*--Backtracking from hypothesis--*/

				bufferReader.readLine();//Number of backtracking from hypothesis
				int numberBackHypo =Integer.parseInt(bufferReader.readLine());
				if (numberBackHypo>0){
					bufferReader.readLine();//Backtracking from hypothesis of this group
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackHypo; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					group.setBacktrackingHypothesis(list);
				}

				/*--Backtracking from entities--*/

				bufferReader.readLine();//Number of backtracking from entities
				int numberBackEnt =Integer.parseInt(bufferReader.readLine());
				if (numberBackEnt>0){
					bufferReader.readLine();//Backtracking from entities of this rule
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackEnt; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					group.setBacktrackingEntities(list);
				}




				/*--Backtracking from atributes--*/

				bufferReader.readLine();//Number of backtracking from atributes
				int numberBackAtr =Integer.parseInt(bufferReader.readLine());
				if (numberBackAtr>0){
					bufferReader.readLine();//Backtracking from atributes of this group
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackAtr; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					group.setBacktrackingAtributes(list);
				}


				/*--Backtracking from relationship--*/

				bufferReader.readLine();//Number of backtracking from relationships
				int numberBackRela =Integer.parseInt(bufferReader.readLine());
				if (numberBackRela>0){
					bufferReader.readLine();//Backtracking from relationships of this group
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackRela; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					group.setBacktrackingRelationship(list);
				}

				/*--Backtracking from rules--*/

				bufferReader.readLine();//Number of backtracking from rules
				int numberBackRules =Integer.parseInt(bufferReader.readLine());
				if (numberBackRules>0){
					bufferReader.readLine();//Backtracking from rules of this group
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackRules; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					group.setBacktrackingRules(list);
				}    	  	

				bufferReader.readLine();//END OF GROUPS  	
			}

			/*--Rules details--*/	      

			bufferReader.readLine(); //************
			bufferReader.readLine(); //Rule detail's

			for (int i = 0; i < numberRules; i++) {
				id = bufferReader.readLine();
				String ruleName = bufferReader.readLine();
				String ruleType = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();

				rule = umpstProject.getMapRules().get(id);
				rule.setRulesName(ruleName);
				rule.setRuleType(ruleType);
				rule.setAuthor(author);
				rule.setDate(date);
				rule.setComments(comments);

				/*--Backtacking from entities--*/

				bufferReader.readLine();//Number of backtracking from entities
				int numberBackEnt =Integer.parseInt(bufferReader.readLine());
				if (numberBackEnt>0){
					bufferReader.readLine();//Backtracking from entities of this rule
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackEnt; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					rule.setBacktracking(list);
				}
				/*--Backtacking from atributes--*/

				bufferReader.readLine();//Number of backtracking from atributes
				int numberBackAtr =Integer.parseInt(bufferReader.readLine());
				if (numberBackAtr>0){
					bufferReader.readLine();//Backtracking atributes names
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackAtr; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					rule.setBacktracking(list);
				}

				/*--Backtacking from relationship--*/

				bufferReader.readLine();//Number of backtracking from relationship
				int numberBackRela =Integer.parseInt(bufferReader.readLine());
				if (numberBackRela>0){
					bufferReader.readLine();//Backtracking relationship names
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackRela; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					rule.setBacktracking(list);
				}
				/*--Fowardtracking groups--*/	  	
				bufferReader.readLine();//Number of groups related
				int numberRulesRelated = Integer.parseInt(bufferReader.readLine());
				if(numberRulesRelated>0){
					bufferReader.readLine();// Fowardtracking groups of this rule
					for (int j = 0; j < numberRulesRelated; j++) {
						String idGroupRelated = bufferReader.readLine();
						rule.getFowardTrackingGroups().add(umpstProject.getMapGroups().get(idGroupRelated));
					}

				}


				bufferReader.readLine();//END OF RULE  	
			}

			/*--Relationship details--*/	      
			bufferReader.readLine(); //************
			bufferReader.readLine(); //Relationship detail's

			for (int i = 0; i < numberRelationship; i++) {
				id = bufferReader.readLine();
				String relationshipName = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();

				relationship = umpstProject.getMapRelationship().get(id);
				relationship.setRelationshipName(relationshipName);
				relationship.setAuthor(author);
				relationship.setDate(date);
				relationship.setComments(comments); 

				/*--Backtacking from entities--*/

				bufferReader.readLine();//Number of backtracking from entities
				int numberBackEnt =Integer.parseInt(bufferReader.readLine());
				if (numberBackEnt>0){
					bufferReader.readLine();//Backtracking from entities of this rule
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackEnt; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					relationship.setBacktrackingEntity(list);
				}

				/*--Backtacking from atributes--*/

				bufferReader.readLine();//Number of backtracking from atributes
				int numberBackAtr =Integer.parseInt(bufferReader.readLine());
				if (numberBackAtr>0){
					bufferReader.readLine();//Backtracking atributes names
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackAtr; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					relationship.setBacktrackingAtribute(list);
				}

				/*--Backtacking from Goals--*/

				bufferReader.readLine();//Number of backtracking from goals
				int numberBackGoal =Integer.parseInt(bufferReader.readLine());
				if (numberBackGoal>0){
					bufferReader.readLine();//Backtracking goals names
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackGoal; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					relationship.setBacktrackingGoal(list);
				}

				/*--Backtacking from Hypothesis--*/

				bufferReader.readLine();//Number of backtracking from hypothesis
				int numberBackHypo =Integer.parseInt(bufferReader.readLine());
				if (numberBackHypo>0){
					bufferReader.readLine();//Backtracking hypothesis names
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackHypo; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					relationship.setBacktrackingGoal(list);
				}

				/*--Fowardtracking rules--*/	  	
				bufferReader.readLine();//Number of rules related
				int numberRulesRelated = Integer.parseInt(bufferReader.readLine());
				if(numberRulesRelated>0){
					bufferReader.readLine();// Fowardtracking rules of this atribute
					for (int j = 0; j < numberRulesRelated; j++) {
						String idRuleRelated = bufferReader.readLine();
						relationship.getFowardtrackingRules().add(umpstProject.getMapRules().get(idRuleRelated));
					}

				}

				/*--Fowardtracking groups--*/	  	
				bufferReader.readLine();//Number of groups related
				int numberGroupsRelated = Integer.parseInt(bufferReader.readLine());
				if(numberGroupsRelated>0){
					bufferReader.readLine();// Fowardtracking groups of this atribute
					for (int j = 0; j < numberGroupsRelated; j++) {
						String idGroupRelated = bufferReader.readLine();
						relationship.getFowardtrackingGroups().add(umpstProject.getMapGroups().get(idGroupRelated));
					}

				}	


				bufferReader.readLine();//END OF RELATIONSHIP
			}
			/*--Atributes details--*/	      
			bufferReader.readLine(); //************
			bufferReader.readLine(); //atribute detail's

			for (int i = 0; i < numberAtributes; i++) {
				id = bufferReader.readLine();
				String atributeName = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();

				atribute = umpstProject.getMapAtribute().get(id);
				atribute.setAtributeName(atributeName);
				atribute.setAuthor(author);
				atribute.setDate(date);
				atribute.setComments(comments);

				idFather = bufferReader.readLine();

				if (!idFather.equals("null")){

					goal.setGoalFather(umpstProject.getMapGoal().get(idFather));
				}
				/*--Entities related with wach atribute--*/	  	
				bufferReader.readLine();//Number of entitiesRelated
				int numberEntRelated = Integer.parseInt(bufferReader.readLine());
				if (numberEntRelated>0){
					bufferReader.readLine();//Entities related with this atribute
					for (int j = 0; j < numberEntRelated; j++) {
						String idEntRelated = bufferReader.readLine();
						atribute.getEntityRelated().add(umpstProject.getMapEntity().get(idEntRelated));
					}	    	  		

				}
				/*--Subatributes of atribute--*/
				bufferReader.readLine();//Number of subatributes
				int numberSubAtributes =Integer.parseInt(bufferReader.readLine());
				if (numberSubAtributes>0){
					bufferReader.readLine();//Sub-atributes of this atribut
					for (int j = 0; j < numberSubAtributes; j++) {
						String idSubAtribute = bufferReader.readLine();
						atribute.getMapSubAtributes().put(idSubAtribute, umpstProject.getMapAtribute().get(idSubAtribute));
					}
				}
				/*--Fowardtracking relationship--*/	  	
				bufferReader.readLine();//Number of relationship related
				int numberRelationshipRelated = Integer.parseInt(bufferReader.readLine());
				if(numberRelationshipRelated>0){
					bufferReader.readLine();// Fowardtracking relationship of this atribute
					for (int j = 0; j < numberRelationshipRelated; j++) {
						String idRelRelated = bufferReader.readLine();
						atribute.getFowardTrackingRelationship().add(umpstProject.getMapRelationship().get(idRelRelated));
					}

				}
				/*--Fowardtracking rules--*/	  	
				bufferReader.readLine();//Number of rules related
				int numberRulesRelated = Integer.parseInt(bufferReader.readLine());
				if(numberRulesRelated>0){
					bufferReader.readLine();// Fowardtracking rules of this atribute
					for (int j = 0; j < numberRulesRelated; j++) {
						String idRuleRelated = bufferReader.readLine();
						atribute.getFowardTrackingRules().add(umpstProject.getMapRules().get(idRuleRelated));
					}

				}
				/*--Fowardtracking groups--*/	  	
				bufferReader.readLine();//Number of groups related
				int numberGroupsRelated = Integer.parseInt(bufferReader.readLine());
				if(numberGroupsRelated>0){
					bufferReader.readLine();// Fowardtracking groups of this atribute
					for (int j = 0; j < numberGroupsRelated; j++) {
						String idGroupRelated = bufferReader.readLine();
						atribute.getFowardTrackingGroups().add(umpstProject.getMapGroups().get(idGroupRelated));
					}

				}

				bufferReader.readLine();//END OF ATRIBUTE	  	
			}
			/*--Adding Entitiyes Details--*/	
			bufferReader.readLine(); //************
			bufferReader.readLine(); //entity detail's

			for (int i = 0; i < numberEntities; i++) {
				id = bufferReader.readLine();
				String entityName = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();

				entity = umpstProject.getMapEntity().get(id);
				entity.setEntityName(entityName);
				entity.setAuthor(author);
				entity.setDate(date);
				entity.setComments(comments);

				/*--Listing atributes of each entity--*/	

				bufferReader.readLine();//Number of atributes of this entity
				int numberAtributesEnt = Integer.parseInt(bufferReader.readLine());
				if (numberAtributesEnt>0){
					bufferReader.readLine();//atributes of this entity
					for (int j = 0; j < numberAtributesEnt; j++) {
						String idAtrEnt = bufferReader.readLine();
						entity.getMapAtributes().put(idAtrEnt, umpstProject.getMapAtribute().get(idAtrEnt));

					}
				}
				/*--Listing backtracking from goals of each entity--*/				
				bufferReader.readLine();//Number of backtracking from goals
				int numberBackGoals =Integer.parseInt(bufferReader.readLine());
				if (numberBackGoals>0){
					bufferReader.readLine();//Backtracking from goals of this entity
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackGoals; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					entity.setBacktracking(list);
				}
				/*--Listing backtracking from hypothesis of each entity--*/			
				bufferReader.readLine();//Number of backtracking from hypothesis
				int numberBackHypo =Integer.parseInt(bufferReader.readLine());
				if (numberBackHypo>0){
					bufferReader.readLine();//Backtracking from hypothesis of this entity
					DefaultListModel listModel = new DefaultListModel();
					for (int j = 0; j < numberBackHypo; j++) {
						listModel.addElement(bufferReader.readLine());
					}
					JList list = new JList(listModel);
					entity.setBacktrackingHypothesis(list);
				}
				/*--Listing fowardtracking rules of each entity--*/			
				bufferReader.readLine();//Number of fowardtracking rules
				int numberFwRules =Integer.parseInt(bufferReader.readLine());
				if (numberFwRules>0){
					bufferReader.readLine();//Fowartracking from rules of this entity
					for (int j = 0; j < numberFwRules; j++) {
						String idFwRule = bufferReader.readLine();
						entity.getFowardTrackingRules().add(umpstProject.getMapRules().get(idFwRule));
					}
				}
				/*-- Listing fowardtracking groups of each entity--*/	
				bufferReader.readLine();//Number of fowardtracking groups
				int numberFwGroups = Integer.parseInt(bufferReader.readLine());
				if (numberFwGroups>0){
					bufferReader.readLine();//Fowardtracking from groups of this entity
					for (int j = 0; j < numberFwGroups; j++) {
						String idFwGroup = bufferReader.readLine();
						entity.getFowardTrackingGroups().add(umpstProject.getMapGroups().get(idFwGroup));
					}
				}
				/*--Listing fowardtracking relationship of each entity--*/	
				bufferReader.readLine();//Number of fowardtracking relationship"
				int numberFwRelationship = Integer.parseInt(bufferReader.readLine());
				if(numberFwRelationship>0){
					bufferReader.readLine();//Fowardtracking from relationship of this entity
					for (int j = 0; j < numberFwRelationship; j++) {
						String idFwRelationship = bufferReader.readLine();
						entity.getFowardTrackingRelationship().add(umpstProject.getMapRelationship().get(idFwRelationship));

					}
				}

				bufferReader.readLine();//END OF ENTITY
			}

			/*--Adding hypothesis details--*/	      
			bufferReader.readLine(); //************
			bufferReader.readLine(); //hypothesis detail's

			for (int i = 0; i < numberHypothesis; i++) {
				id = bufferReader.readLine();
				String hypothesisName = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();
				idFather = bufferReader.readLine();

				hypothesis = umpstProject.getMapHypothesis().get(id);
				hypothesis.setHypothesisName(hypothesisName);
				hypothesis.setAuthor(author);
				hypothesis.setDate(date);
				hypothesis.setComments(comments);

				if (!idFather.equals("null")){

					hypothesis.setFather(umpstProject.getMapHypothesis().get(idFather));
				}
				/*-- Adding goals related --*/    	  	
				bufferReader.readLine(); //number of goals related
				int numberGoalsRelated = Integer.parseInt(bufferReader.readLine());
				if (numberGoalsRelated>0){
					bufferReader.readLine(); //Goals related with this hypothesis
					for (int j = 0; j < numberGoalsRelated; j++) {
						String idGoalRelated = bufferReader.readLine();
						hypothesis.getGoalRelated().add(umpstProject.getMapGoal().get(idGoalRelated));
					}
				}
				/*--Adding sub-hypothesis--*/  
				bufferReader.readLine();//number of subhypothesis
				int numberSubHypothesis = Integer.parseInt(bufferReader.readLine());
				if (numberSubHypothesis>0){
					bufferReader.readLine();// Subhypothesis of this hypothesis
					for (int j = 0; j < numberSubHypothesis; j++) {
						String idSubHypothesis = bufferReader.readLine();
						hypothesis.getMapSubHypothesis().put(idSubHypothesis, umpstProject.getMapHypothesis().get(idSubHypothesis));
					}
				}
				/*--Adding fowardtracking entity--*/
				bufferReader.readLine();//Number of foward tracking entity
				int numberFwEntity = Integer.parseInt(bufferReader.readLine());
				if (numberFwEntity>0){
					bufferReader.readLine();//Foward tracking entity of this hypothesis
					for (int j = 0; j < numberFwEntity; j++) {
						String idFwEntity = bufferReader.readLine();
						hypothesis.getFowardTrackingEntity().add(umpstProject.getMapEntity().get(idFwEntity));
					}
				}
				/*-- Adding foward tracking groups --*/    	  
				bufferReader.readLine();//Number of foward tracking groups
				int numberFwGroups = Integer.parseInt(bufferReader.readLine());
				if (numberFwGroups>0){
					bufferReader.readLine();//Foward tracking groups of this hypothesis
					for (int j = 0; j < numberFwGroups; j++) {
						String idFwGroup = bufferReader.readLine();
						hypothesis.getFowardTrackingGroups().add(umpstProject.getMapGroups().get(idFwGroup));
					}
				}

				bufferReader.readLine();//END OF HYPOTHESIS
			}


			/*GOALS DETAILS*/	      
			bufferReader.readLine(); //************
			bufferReader.readLine(); //goals detail's

			for (int i = 0; i < numberGoals; i++) {
				id = bufferReader.readLine();
				goalName = bufferReader.readLine();
				author = bufferReader.readLine();
				date = bufferReader.readLine();
				comments =bufferReader.readLine();
				idFather = bufferReader.readLine();

				goal = umpstProject.getMapGoal().get(id);
				goal.setGoalName(goalName);
				goal.setAuthor(author);
				goal.setDate(date);
				goal.setComments(comments);

				if (!idFather.equals("null")){

					goal.setGoalFather(umpstProject.getMapGoal().get(idFather));
				}

				bufferReader.readLine();//Number of subgoals
				int numberSubgoals = Integer.parseInt(bufferReader.readLine());

				if (numberSubgoals>0){
					bufferReader.readLine();//Subgoals IDs:
					GoalModel subgoal;
					for (int j = 0; j < numberSubgoals; j++) {
						id = bufferReader.readLine();
						subgoal = umpstProject.getMapGoal().get(id);
						goal.getSubgoals().put(subgoal.getId(), subgoal);

					}
				}

				bufferReader.readLine();//Number of hypothesis of this goal
				int numberHypothesisGoal = Integer.parseInt(bufferReader.readLine());
				if (numberHypothesisGoal>0){
					bufferReader.readLine();//HypoRelated IDs:
					HypothesisModel hypoRelated;
					for (int j = 0; j < numberHypothesisGoal; j++) {
						id = bufferReader.readLine();
						hypoRelated = umpstProject.getMapHypothesis().get(id);
						goal.getMapHypothesis().put(hypoRelated.getId(), hypoRelated);

					}
				}

				bufferReader.readLine();//Number of entities related with this goal
				int numberEntGoal = Integer.parseInt(bufferReader.readLine());
				if (numberEntGoal>0){
					bufferReader.readLine();// EntRelated IDs:
					EntityModel entRelated;
					for (int j = 0; j < numberEntGoal; j++) {
						id = bufferReader.readLine();
						entRelated = umpstProject.getMapEntity().get(id);
						goal.getFowardTrackingEntity().add(entRelated);
					}

				}

				bufferReader.readLine();//Number of groups related with this goal
				int numberGroupGoal = Integer.parseInt(bufferReader.readLine());//
				if (numberGroupGoal>0){
					bufferReader.readLine();//Groups related IDs: 
					GroupsModel groupRelated;
					for (int j = 0; j < numberGroupGoal; j++) {
						id = bufferReader.readLine();
						groupRelated = umpstProject.getMapGroups().get(id);
						goal.getFowardTrackingGroups().add(groupRelated);

					}
				}

				bufferReader.readLine();//####
			}
			// this statement reads the line from the file and print it to
			// the console.


			// dispose all the resources after using them.
			fis.close();
			bis.close();
			bufferReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return umpstProject;
	}

}
