package unbbayes.io.umpst;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.model.umpst.rules.RulesModel;

public class FileLoad {
	
	//This atribute is only to absorve the comments of the file
	private String comment; 
    private GoalModel goal;
    private HypothesisModel hypothesis;
    private EntityModel entity;
    private GroupsModel group;
    private AtributeModel atribute;
    private RelationshipModel relationship;
    private RulesModel rule;
	
	public  void loadUbf(File file) {

		
		
	    //File file = new File("images/file.ump");
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    BufferedReader bufferReader = null;
	    String st;
	    String id,goalName,author,date,comments,idFather;

	    
	    try {
	      fis = new FileInputStream(file);

	      // Here BufferedInputStream is added for fast reading.
	      bis = new BufferedInputStream(fis);
	      bufferReader = new BufferedReader(new InputStreamReader(fis));
	  
	      // dis.available() returns 0 if the file does not have more lines.
	      
	      comment = bufferReader.readLine();  //"Number of goals in the map:"
	      int numberGoals = Integer.parseInt(bufferReader.readLine());
	      if (numberGoals>0)
	    	  comment = bufferReader.readLine(); //All goals in the map
	      
	      for (int i = 0; i < numberGoals; i++) {
	    	  	id = bufferReader.readLine();
	    	  	goal = new GoalModel(id, "","", "", "" , null, null, null, null, null,null );
	    	  	UMPSTProject.getInstance().getMapGoal().put(goal.getId(), goal);
		  }
	      
	      comment = bufferReader.readLine();//Number of hypothesis cadastred
	      int numberHypothesis = Integer.parseInt(bufferReader.readLine());
	      if (numberHypothesis>0)
	    	  comment = bufferReader.readLine();//IDs of all hypothesis

	      for (int i = 0; i < numberHypothesis; i++) {
	    	  id = bufferReader.readLine();
	    	  hypothesis = new HypothesisModel(id, "", "", "", "", null, null, null, null, null);
	    	  UMPSTProject.getInstance().getMapHypothesis().put(hypothesis.getId(), hypothesis);
	      }
	      
	      comment = bufferReader.readLine(); //Number of entites cadastred
	      int numberEntities = Integer.parseInt(bufferReader.readLine());
	      if (numberEntities>0)
	    	  comment = bufferReader.readLine();//IDs of all entities

	      for (int i = 0; i < numberEntities; i++) {
	    	  id = bufferReader.readLine();
	    	  entity = new EntityModel(id, "", "", "", "", null, null, null, null, null, null);
	    	  UMPSTProject.getInstance().getMapEntity().put(entity.getId(), entity);
	    	  
	      }
	      
	      comment = bufferReader.readLine(); //Number of atributes cadastred
	      int numberAtributes = Integer.parseInt(bufferReader.readLine());
	      if (numberAtributes>0)
	    	  comment = bufferReader.readLine();//IDs of all atributes

	      for (int i = 0; i < numberAtributes; i++) {
	    	  id = bufferReader.readLine();
	    	  atribute = new AtributeModel(id, "", "", "", "", null, null, null, null);
	    	  UMPSTProject.getInstance().getMapAtribute().put(atribute.getId(), atribute);
	    	  
	      }
	      
	      comment = bufferReader.readLine(); //Number of relationship cadastred
	      int numberRelationship = Integer.parseInt(bufferReader.readLine());
	      if (numberRelationship>0)
	    	  comment = bufferReader.readLine();//IDs of all relationship

	      for (int i = 0; i < numberRelationship; i++) {
	    	  id = bufferReader.readLine();
	    	  relationship = new RelationshipModel(id, "", "", "", "", null, null, null);
	    	  UMPSTProject.getInstance().getMapRelationship().put(relationship.getId(), relationship);
	    	  
	      }
	      
	      comment = bufferReader.readLine(); //Number of rules cadastred
	      int numberRules = Integer.parseInt(bufferReader.readLine());
	      if (numberRules>0)
	    	  comment = bufferReader.readLine();//IDs of all rules

	      for (int i = 0; i < numberRules; i++) {
	    	  id = bufferReader.readLine();
	    	  rule = new RulesModel(id, "", "", "", "","", null, null);
	    	  UMPSTProject.getInstance().getMapRules().put(rule.getId(), rule);
	    	  
	      }
	      
	      comment = bufferReader.readLine(); //Number of Groups cadastred
	      int numberGroups = Integer.parseInt(bufferReader.readLine());
	      if (numberGroups>0)
	    	  comment = bufferReader.readLine();//IDs of all groups
	      
	      for (int i = 0; i < numberGroups; i++) {
	    	  id = bufferReader.readLine(); 
	    	  group = new GroupsModel(id, "", "", "", "", null, null);
	    	  UMPSTProject.getInstance().getMapGroups().put(group.getId(), group);
	      }
/*GOALS DETAILS*/	      
	      comment = bufferReader.readLine(); //************
	      comment = bufferReader.readLine(); //goals detail's
	      
	      for (int i = 0; i < numberGoals; i++) {
	    	  	id = bufferReader.readLine();
	    	  	goalName = bufferReader.readLine();
	    	  	author = bufferReader.readLine();
	    	  	date = bufferReader.readLine();
	    	  	comments =bufferReader.readLine();
	    	  	idFather = bufferReader.readLine();
	    	  	
	    	  	goal = UMPSTProject.getInstance().getMapGoal().get(id);
	    	  	goal.setGoalName(goalName);
	    	  	goal.setAuthor(author);
	    	  	goal.setDate(date);
	    	  	goal.setComments(comments);
	    	  	
	    	  	if (!idFather.equals("null")){
	    	 
	    	  		goal.setGoalFather(UMPSTProject.getInstance().getMapGoal().get(idFather));
	    	  	}
	    	  	
	    	  	comment = bufferReader.readLine();//Number of subgoals
	    	  	int numberSubgoals = Integer.parseInt(bufferReader.readLine());
	    	  	
	    	  	if (numberSubgoals>0){
	    	  		comment = bufferReader.readLine();//Subgoals IDs:
	    	  		GoalModel subgoal;
	    	  		for (int j = 0; j < numberSubgoals; j++) {
	    	  			id = bufferReader.readLine();
						subgoal = UMPSTProject.getInstance().getMapGoal().get(id);
						goal.getSubgoals().put(subgoal.getId(), subgoal);
						
					}
	    	  	}
	    	  	
	    	  	comment = bufferReader.readLine();//Number of hypothesis of this goal
	    	  	int numberHypothesisGoal = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberHypothesisGoal>0){
	    	  		comment = bufferReader.readLine();//HypoRelated IDs:
	    	  		HypothesisModel hypoRelated;
	    	  		for (int j = 0; j < numberHypothesisGoal; j++) {
	    	  			id = bufferReader.readLine();
	    	  			hypoRelated = UMPSTProject.getInstance().getMapHypothesis().get(id);
	    	  			goal.getMapHypothesis().put(hypoRelated.getId(), hypoRelated);
						
					}
	    	  	}
	    	  	
	    	  	comment = bufferReader.readLine();//Number of entities related with this goal
	    	  	int numberEntGoal = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberEntGoal>0){
	    	  		comment = bufferReader.readLine();// EntRelated IDs:
	    	  		EntityModel entRelated;
	    	  		for (int j = 0; j < numberEntGoal; j++) {
	    	  			id = bufferReader.readLine();
		    	  		entRelated = UMPSTProject.getInstance().getMapEntity().get(id);
		    	  		goal.getFowardTrackingEntity().add(entRelated);
					}

	    	  	}
	    	 
	    	  	comment = bufferReader.readLine();//Number of groups related with this goal
	    	  	int numberGroupGoal = Integer.parseInt(bufferReader.readLine());//
	    	  	if (numberGroupGoal>0){
	    	  		comment = bufferReader.readLine();//Groups related IDs: 
	    	  		GroupsModel groupRelated;
	    	  		for (int j = 0; j < numberGroupGoal; j++) {
	    	  			id = bufferReader.readLine();
	    	  			groupRelated = UMPSTProject.getInstance().getMapGroups().get(id);
	    	  			goal.getFowardTrackingGroups().add(groupRelated);
						
					}
	    	  	}
	    	  	
	    	  	comment = bufferReader.readLine();//####
		}
/*--Adding hypothesis details--*/	      
	      comment = bufferReader.readLine(); //************
	      comment = bufferReader.readLine(); //hypothesis detail's
	      
	      for (int i = 0; i < numberHypothesis; i++) {
	    	  	id = bufferReader.readLine();
	    	  	String hypothesisName = bufferReader.readLine();
	    	  	author = bufferReader.readLine();
	    	  	date = bufferReader.readLine();
	    	  	comments =bufferReader.readLine();
	    	  	idFather = bufferReader.readLine();
	    	  	
	    	  	hypothesis = UMPSTProject.getInstance().getMapHypothesis().get(id);
	    	  	hypothesis.setHypothesisName(hypothesisName);
	    	  	hypothesis.setAuthor(author);
	    	  	hypothesis.setDate(date);
	    	  	hypothesis.setComments(comments);
	    	  	
	    	  	if (!idFather.equals("null")){
	    	 
	    	  		hypothesis.setFather(UMPSTProject.getInstance().getMapHypothesis().get(idFather));
	    	  	}
	/*-- Adding goals related --*/    	  	
	    	  	bufferReader.readLine(); //number of goals related
	    	  	int numberGoalsRelated = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberGoalsRelated>0){
	    	  		bufferReader.readLine(); //Goals related with this hypothesis
	    	  		for (int j = 0; j < numberGoalsRelated; j++) {
	    	  			String idGoalRelated = bufferReader.readLine();
	    	  			hypothesis.getGoalRelated().add(UMPSTProject.getInstance().getMapGoal().get(idGoalRelated));
					}
	    	  	}
	/*--Adding sub-hypothesis--*/  
	    	  	bufferReader.readLine();//number of subhypothesis
	    	  	int numberSubHypothesis = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberSubHypothesis>0){
	    	  		bufferReader.readLine();// Subhypothesis of this hypothesis
	    	  		for (int j = 0; j < numberSubHypothesis; j++) {
						String idSubHypothesis = bufferReader.readLine();
						hypothesis.getMapSubHypothesis().put(idSubHypothesis, UMPSTProject.getInstance().getMapHypothesis().get(idSubHypothesis));
					}
	    	  	}
	/*--Adding fowardtracking entity--*/
	    	  	bufferReader.readLine();//Number of foward tracking entity
	    	  	int numberFwEntity = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberFwEntity>0){
	    	  		bufferReader.readLine();//Foward tracking entity of this hypothesis
	    	  		for (int j = 0; j < numberFwEntity; j++) {
	    	  			String idFwEntity = bufferReader.readLine();
	    	  			hypothesis.getFowardTrackingEntity().add(UMPSTProject.getInstance().getMapEntity().get(idFwEntity));
	    	  		}
	    	  	}
	/*-- Adding foward tracking groups --*/    	  
	    	  	bufferReader.readLine();//Number of foward tracking groups
	    	  	int numberFwGroups = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberFwGroups>0){
	    	  		bufferReader.readLine();//Foward tracking groups of this hypothesis
	    	  		for (int j = 0; j < numberFwGroups; j++) {
						String idFwGroup = bufferReader.readLine();
						hypothesis.getFowardTrackingGroups().add(UMPSTProject.getInstance().getMapGroups().get(idFwGroup));
					}
	    	  	}
	    	  	
	    	  	bufferReader.readLine();//END OF HYPOTHESIS
	      }
/*--Adding Entitiyes Details--*/	
	      comment = bufferReader.readLine(); //************
	      comment = bufferReader.readLine(); //entity detail's
	      
	      for (int i = 0; i < numberEntities; i++) {
	    	  	id = bufferReader.readLine();
	    	  	String entityName = bufferReader.readLine();
	    	  	author = bufferReader.readLine();
	    	  	date = bufferReader.readLine();
	    	  	comments =bufferReader.readLine();
	    	  	
	    	  	entity = UMPSTProject.getInstance().getMapEntity().get(id);
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
	    	  			entity.getMapAtributes().put(idAtrEnt, UMPSTProject.getInstance().getMapAtribute().get(idAtrEnt));
						
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
						entity.getFowardTrackingRules().add(UMPSTProject.getInstance().getMapRules().get(idFwRule));
					}
	    	  	}
	/*-- Listing fowardtracking groups of each entity--*/	
	    	  	bufferReader.readLine();//Number of fowardtracking groups
	    	  	int numberFwGroups = Integer.parseInt(bufferReader.readLine());
	    	  	if (numberFwGroups>0){
	    	  		bufferReader.readLine();//Fowardtracking from groups of this entity
	    	  		for (int j = 0; j < numberFwGroups; j++) {
						String idFwGroup = bufferReader.readLine();
						entity.getFowardTrackingGroups().add(UMPSTProject.getInstance().getMapGroups().get(idFwGroup));
					}
	    	  	}
	/*--Listing fowardtracking relationship of each entity--*/	
	    	  	bufferReader.readLine();//Number of fowardtracking relationship"
	    	  	int numberFwRelationship = Integer.parseInt(bufferReader.readLine());
	    	  	if(numberFwRelationship>0){
	    	  		bufferReader.readLine();//Fowardtracking from relationship of this entity
	    	  		for (int j = 0; j < numberFwRelationship; j++) {
	    	  			String idFwRelationship = bufferReader.readLine();
	    	  			entity.getFowardTrackingRelationship().add(UMPSTProject.getInstance().getMapRelationship().get(idFwRelationship));
						
					}
	    	  	}

	    	  	bufferReader.readLine();//END OF ENTITY
	      }
/*--Atributes details--*/	      
	     /** comment = bufferReader.readLine(); //************
	      comment = bufferReader.readLine(); //atribute detail's
	      
	      for (int i = 0; i < numberAtributes; i++) {
	    	  	id = bufferReader.readLine();
	    	  	String atributeName = bufferReader.readLine();
	    	  	author = bufferReader.readLine();
	    	  	date = bufferReader.readLine();
	    	  	comments =bufferReader.readLine();
	    	  	
	    	  	atribute = UMPSTProject.getInstance().getMapAtribute().get(id);
	    	  	atribute.setAtributeName(atributeName);
	    	  	atribute.setAuthor(author);
	    	  	atribute.setDate(date);
	    	  	atribute.setComments(comments);
	    	
	    	  	idFather = bufferReader.readLine();
	    	  	
	    	  	if (!idFather.equals("null")){
	   	    	 
	    	  		goal.setGoalFather(UMPSTProject.getInstance().getMapGoal().get(idFather));
	    	  	}
	    	  	comment = bufferReader.readLine();//Number of entitiesRelated
	    	  	
	      }*/
	      
	      
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
	  }


}
