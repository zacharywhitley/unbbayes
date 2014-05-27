package unbbayes.io.umpst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

public class FileSave {


	public static final String NULL = "null";

	private EntityModel entity;
	private RuleModel rule;
	private GroupModel group;
	private Set<String> keys;
	private TreeSet<String> sortedKeys;
	private RelationshipModel relationship;
	private GoalModel goal;
	private UMPSTProject umpstProject;
//
//	public void saveUbf(File file,
//			UMPSTProject umpstProject) throws FileNotFoundException{
//
//
//		this.umpstProject = umpstProject;
//		//File file = new File("images/file.ump");
//
//		/*-- Listing the overall data of the map --*/
//		PrintStream printStream = new PrintStream(new FileOutputStream(new File(file.getPath()+".ump")));
//
//		keys = umpstProject.getMapGoal().keySet();
//		sortedKeys = new TreeSet<String>(keys);
//		printStream.println("Number of goals cadastred");
//		printStream.println(umpstProject.getMapGoal().size());
//		if (umpstProject.getMapGoal().size()>0){
//			printStream.println("IDs of all goals");
//			for (String key : sortedKeys){
//				printStream.println(umpstProject.getMapGoal().get(key).getId());
//			}
//		}
//		printStream.println("Number of hypothesis cadastred");
//		printStream.println(umpstProject.getMapHypothesis().size());
//
//		if(umpstProject.getMapHypothesis().size()>0){
//			printStream.println("IDs of all hypothesis");
//			keys = umpstProject.getMapHypothesis().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//			for (String keyHypo : sortedKeys){
//				printStream.println(umpstProject.getMapHypothesis().get(keyHypo).getId());
//			}
//		}
//
//
//		printStream.println("Number of entites cadastred");
//		printStream.println(umpstProject.getMapEntity().size());
//
//		if (umpstProject.getMapEntity().size()>0){
//			printStream.println("IDs of all entities");
//			keys = umpstProject.getMapEntity().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//			for (String keyEnt : sortedKeys){
//				printStream.println(umpstProject.getMapEntity().get(keyEnt).getId());
//			}
//		}
//
//		printStream.println("Number of atributes cadastred");
//		printStream.println(umpstProject.getMapAtribute().size());
//
//		if (umpstProject.getMapAtribute().size()>0){
//			printStream.println("IDs of all atributes");
//			keys = umpstProject.getMapAtribute().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//			for (String keyAtr : sortedKeys){
//				printStream.println(umpstProject.getMapAtribute().get(keyAtr).getId());
//			}
//		}
//
//		printStream.println("Number of relationship cadastred");
//		printStream.println(umpstProject.getMapRelationship().size());
//
//		if (umpstProject.getMapRelationship().size()>0){
//			printStream.println("IDs of all relationship");
//			keys = umpstProject.getMapRelationship().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//			for (String keyRela : sortedKeys){
//				printStream.println(umpstProject.getMapRelationship().get(keyRela).getId());
//			}
//		}
//
//		printStream.println("Number of rules cadastred");
//		printStream.println(umpstProject.getMapRules().size());
//
//		if (umpstProject.getMapRules().size()>0){
//			printStream.println("IDs of all rules");
//			keys = umpstProject.getMapRules().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//			for (String keyRule : sortedKeys){
//				printStream.println(umpstProject.getMapRules().get(keyRule).getId());
//			}
//		}
//
//		printStream.println("Number of Groups cadastred");
//		printStream.println(umpstProject.getMapGroups().size());
//
//		if (umpstProject.getMapGroups().size()>0){
//			printStream.println("IDs of all groups");
//			keys = umpstProject.getMapGroups().keySet();
//			sortedKeys = new TreeSet<String>(keys);
//			for (String keyGroup : sortedKeys){
//				printStream.println(umpstProject.getMapGroups().get(keyGroup).getId());
//			}
//		}
//
//		/*-- Groups details --*/			
//		printStream.println("****************");			
//		printStream.println("Groups Details");
//
//		Set<String> keysGroups = umpstProject.getMapGroups().keySet();
//		TreeSet<String> sortedKeyGroups = new TreeSet<String>(keysGroups);
//
//		for(String keyGroup : sortedKeyGroups){
//
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getId());
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getName());
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getAuthor());
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getDate());
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getComments());
//
//			/*--Backtacking from goal--*/	
//
//			printStream.println("Number of goals related");
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getSize());
//			if(umpstProject.getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getSize()>0){
//				printStream.println("IDs of goals related");
//				for (int i = 0; i < umpstProject.getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from hypotheis--*/	
//
//			printStream.println("Number of hypothesis related");
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getSize());
//			if(umpstProject.getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getSize()>0){
//				printStream.println("IDs of hypothesis related");
//				for (int i = 0; i < umpstProject.getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from entities--*/	
//
//			printStream.println("Number of entities related");
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getSize());
//			if(umpstProject.getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getSize()>0){
//				printStream.println("IDs of entities related");
//				for (int i = 0; i < umpstProject.getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from atributes--*/	
//
//			printStream.println("Number of atributes related");
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getSize());
//			if(umpstProject.getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getSize()>0){
//				printStream.println("IDs of atributes related");
//				for (int i = 0; i < umpstProject.getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from relationship--*/	
//
//			printStream.println("Number of relationship related");
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getSize());
//			if(umpstProject.getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getSize()>0){
//				printStream.println("IDs of relationship related");
//				for (int i = 0; i < umpstProject.getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from rules--*/	
//
//			printStream.println("Number of rules related");
//			printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getSize());
//			if(umpstProject.getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getSize()>0){
//				printStream.println("IDs of rules related");
//				for (int i = 0; i < umpstProject.getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getElementAt(i));
//				}
//			}
//
//			printStream.println("END OF GROUP");
//		}
//
//		/*-- Rules details --*/			
//		printStream.println("****************");			
//		printStream.println("Rules Details");
//
//		Set<String> keysRules = umpstProject.getMapRules().keySet();
//		TreeSet<String> sortedKeyRules = new TreeSet<String>(keysRules);
//
//		for(String keyRule : sortedKeyRules){
//
//			printStream.println(umpstProject.getMapRules().get(keyRule).getId());
//			printStream.println(umpstProject.getMapRules().get(keyRule).getName());
//			printStream.println(umpstProject.getMapRules().get(keyRule).getRuleType());
//			printStream.println(umpstProject.getMapRules().get(keyRule).getAuthor());
//			printStream.println(umpstProject.getMapRules().get(keyRule).getDate());
//			printStream.println(umpstProject.getMapRules().get(keyRule).getComments());
//
//			/*--Backtacking from entities--*/	
//
//			printStream.println("Number of entities related");
//			printStream.println(umpstProject.getMapRules().get(keyRule).getBacktrackingGoalList().getModel().getSize());
//			if(umpstProject.getMapRules().get(keyRule).getBacktrackingGoalList().getModel().getSize()>0){
//				printStream.println("IDs of entities related");
//				for (int i = 0; i < umpstProject.getMapRules().get(keyRule).getBacktrackingGoalList().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapRules().get(keyRule).getBacktrackingGoalList().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from atribute--*/	
//
//			printStream.println("Number of atributes related");
//			printStream.println(umpstProject.getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getSize());
//			if(umpstProject.getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getSize()>0){
//				printStream.println("IDs of atributes related");
//				for (int i = 0; i < umpstProject.getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Backtacking from relationship--*/	
//
//			printStream.println("Number of relationships related");
//			printStream.println(umpstProject.getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getSize());
//			if(umpstProject.getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getSize()>0){
//				printStream.println("IDs of relationships related");
//				for (int i = 0; i < umpstProject.getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getElementAt(i));
//				}
//			}
//
//			/*--Fowardtracking groups--*/		
//
//			printStream	.println("Number of groups related");
//			printStream.println(umpstProject.getMapRules().get(keyRule).getFowardTrackingGroups().size());
//			if(umpstProject.getMapRules().get(keyRule).getFowardTrackingGroups().size()>0){
//				printStream.println("Fowardtracking groups of this atribute");
//				Set<GroupModel> setAux = umpstProject.getMapAtribute().get(keyRule).getFowardTrackingGroups();
//				for (Iterator<GroupModel> it = setAux.iterator();it.hasNext();){
//					group = it.next();
//					printStream.println(group.getId());
//				}
//			}
//			printStream.println("END OF RULE");
//		}
//
//		/*-- relationship details --*/			
//		printStream.println("****************");			
//		printStream.println("relationshp Details");
//
//		Set<String> keysRelationship = umpstProject.getMapRelationship().keySet();
//		TreeSet<String> sortedKeyRelationship = new TreeSet<String>(keysRelationship);
//
//		for(String keyRelationship : sortedKeyRelationship){
//
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getId());
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getName());
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getAuthor());
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getDate());
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getComments());
//
//			/*--Backtacking from entities--*/	
//
//			printStream.println("Number of entities related");
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingEntityList().size());
//			if(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingEntityList().size() > 0){
//				printStream.println("IDs of entities related");
//				for (int i = 0; i < umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingEntityList().size(); i++) {
//					printStream.println(
//							umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingEntityList().get(i));
//				}
//			}
//
//			/*--Backtacking from atribute--*/	
//
//			printStream.println("Number of atribute related");
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingAtribute().size());
//			if(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingAtribute().size() > 0){
//				printStream.println("IDs of atribute related");
//				for (int i = 0; i < umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingAtribute().size(); i++) {
//					printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingAtribute().get(i));
//				}
//			}
//
//			/*--Backtacking from goals--*/	
//
//			printStream.println("Number of goals related");
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingGoal().size());
//			if(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingGoal().size()>0){
//				printStream.println("IDs of goals related");
//				for (int i = 0; i < umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingGoal().size(); i++) {
//					printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingGoal().get(i));
//				}
//			}
//
//			/*--Backtacking from hypothesis--*/	
//
//			printStream.println("Number of hypothesis related");
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().size());
//			if(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().size() > 0){
//				printStream.println("IDs of hypothesis related");
//				for (int i = 0; i < umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().size(); i++) {
//					printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().get(i));
//				}
//			}
//			/*--Fowardtracking rules--*/		
//
//			printStream	.println("Number of rules related");
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getFowardtrackingRules().size());
//			if(umpstProject.getMapRelationship().get(keyRelationship).getFowardtrackingRules().size()>0){
//				printStream.println("Fowardtracking rules of this atribute");
//				Set<RuleModel> setAux = umpstProject.getMapRelationship().get(keyRelationship).getFowardtrackingRules();
//				for (Iterator<RuleModel> it = setAux.iterator();it.hasNext();){
//					rule = it.next();
//					printStream.println(rule.getId());
//				}
//			}
//			/*--Fowardtracking groups--*/		
//
//			printStream	.println("Number of groups related");
//			printStream.println(umpstProject.getMapRelationship().get(keyRelationship).getFowardtrackingGroups().size());
//			if(umpstProject.getMapRelationship().get(keyRelationship).getFowardtrackingGroups().size()>0){
//				printStream.println("Fowardtracking groups of this atribute");
//				Set<GroupModel> setAux = umpstProject.getMapRelationship().get(keyRelationship).getFowardtrackingGroups();
//				for (Iterator<GroupModel> it = setAux.iterator();it.hasNext();){
//					group = it.next();
//					printStream.println(group.getId());
//				}
//			}
//
//			printStream.println("END OF RELATIONSHIP");
//		}
//
//		/*-- Atributes Details --*/	
//		printStream.println("****************");			
//		printStream.println("Atributes Details");
//
//		Set<String> keysAtributes = umpstProject.getMapAtribute().keySet();
//		TreeSet<String> sortedKeyAtributes = new TreeSet<String>(keysAtributes);
//
//		for(String keyAtribute : sortedKeyAtributes){
//
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getId());
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getAtributeName());
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getAuthor());
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getDate());
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getComments());
//
//			if (umpstProject.getMapAtribute().get(keyAtribute).getFather()==null){
//				printStream.println(NULL);
//			}
//			else{
//				printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getFather().getId());
//			}
//
//			/*--Entities related --*/		
//			printStream.println("Number of entities related with this atribute");
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getEntityRelated().size());
//			if (umpstProject.getMapAtribute().get(keyAtribute).getEntityRelated().size()>0){
//				printStream.println("Entities related with this atribute");
//				Set<EntityModel> setAux = umpstProject.getMapAtribute().get(keyAtribute).getEntityRelated();
//				for (Iterator<EntityModel> it=setAux.iterator();it.hasNext();){
//					entity = it.next();
//					printStream.println(entity.getId());
//				}
//			}
//			/*--Subatributes--*/		
//			printStream.println("Number of sub-atributes");
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getMapSubAtributes().size());
//			if (umpstProject.getMapAtribute().get(keyAtribute).getMapSubAtributes().size()>0){
//				printStream.println("Sub-atributes of this atribute");
//				keys = umpstProject.getMapAtribute().get(keyAtribute).getMapSubAtributes().keySet();
//				sortedKeys = new TreeSet<String>(keys);
//				for(String keySubAtr:sortedKeys){
//					printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getMapSubAtributes().get(keySubAtr).getId());
//				}
//			}
//			/*--Fowardtracking relationship--*/		
//			printStream	.println("Number of relationship related");
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingRelationship().size());
//			if(umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingRelationship().size()>0){
//				printStream.println("Fowardtracking relationship of this atribute");
//				Set<RelationshipModel> setAux = umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingRelationship();
//				for (Iterator<RelationshipModel> it = setAux.iterator();it.hasNext();){
//					relationship = it.next();
//					printStream.println(relationship.getId());
//				}
//			}
//			/*--Fowardtracking rules--*/		
//
//			printStream	.println("Number of rules related");
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingRules().size());
//			if(umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingRules().size()>0){
//				printStream.println("Fowardtracking rules of this atribute");
//				Set<RuleModel> setAux = umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingRules();
//				for (Iterator<RuleModel> it = setAux.iterator();it.hasNext();){
//					rule = it.next();
//					printStream.println(rule.getId());
//				}
//			}
//			/*--Fowardtracking groups--*/		
//
//			printStream	.println("Number of groups related");
//			printStream.println(umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingGroups().size());
//			if(umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingGroups().size()>0){
//				printStream.println("Fowardtracking groups of this atribute");
//				Set<GroupModel> setAux = umpstProject.getMapAtribute().get(keyAtribute).getFowardTrackingGroups();
//				for (Iterator<GroupModel> it = setAux.iterator();it.hasNext();){
//					group = it.next();
//					printStream.println(group.getId());
//				}
//			}
//
//			printStream.println("END OF ATRIBUTE");
//		}
//
//
//		/*-- Adding entity details --*/
//		printStream.println("****************");			
//		printStream.println("Entities Details");
//
//		Set<String> keysEntities = umpstProject.getMapEntity().keySet();
//		TreeSet<String> sortedKeyEntities = new TreeSet<String>(keysEntities);
//
//		for(String keyEntity : sortedKeyEntities){
//
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getId());
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getName());
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getAuthor());
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getDate());
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getComments());
//
//			/*--Listing atributes of each entity--*/	
//
//			printStream.println("Number of atributes of this entity");
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getMapAtributes().size());
//			if (umpstProject.getMapEntity().get(keyEntity).getMapAtributes().size()>0){
//				printStream.println("Atributes of this entity");
//				keys = umpstProject.getMapEntity().get(keyEntity).getMapAtributes().keySet();
//				sortedKeys = new TreeSet<String>(keys);
//				for(String keyAtrEnt : sortedKeys)
//					printStream.println(umpstProject.getMapEntity().get(keyEntity).getMapAtributes().get(keyAtrEnt).getId());
//			}
//			/*--Listing backtracking from goals of each entity--*/				
//			printStream.println("Number of backtracking from goals");
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getBacktrackingGoalList().getModel().getSize());
//			if (umpstProject.getMapEntity().get(keyEntity).getBacktrackingGoalList().getModel().getSize()>0){
//				printStream.println("Backtracking from goals of this entity");
//				for (int i = 0; i < umpstProject.getMapEntity().get(keyEntity).getBacktrackingGoalList().getModel().getSize(); i++) {
//					printStream.println(umpstProject.getMapEntity().get(keyEntity).getBacktrackingGoalList().getModel().getElementAt(i));
//				}
//
//
//			}
//			/*--Listing backtracking from hypothesis of each entity--*/			
//			printStream.println("Number of backtracking from hypothesis");
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getSize());
//			if (umpstProject.getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getSize()>0){
//				printStream.println("Backtracking from hypothesis of this entity");
//				for (int i = 0; i <(umpstProject.getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getSize()); i++) {
//					printStream.println((umpstProject.getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getElementAt(i)));
//				}
//			}
//
//			/*--Listing fowardtracking rules of each entity--*/			
//
//			printStream.println("Number of fowardtracking rules");
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getFowardTrackingRules().size());
//			if (umpstProject.getMapEntity().get(keyEntity).getFowardTrackingRules().size()>0){
//				printStream.println("Fowartracking from rules of this entity");
//				Set<RuleModel> setAux = umpstProject.getMapEntity().get(keyEntity).getFowardTrackingRules(); 
//				for (Iterator<RuleModel> it = setAux.iterator() ; it.hasNext();){
//					rule = it.next();
//					printStream.println(rule.getId());
//				}
//			}
//
//			/*-- Listing fowardtracking groups of each entity--*/	
//
//			printStream.println("Number of fowardtracking groups");
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getFowardTrackingGroups().size());
//			if(umpstProject.getMapEntity().get(keyEntity).getFowardTrackingGroups().size()>0){
//				printStream.println("Fowardtracking from groups of this entity");
//				Set<GroupModel> setAux = umpstProject.getMapEntity().get(keyEntity).getFowardTrackingGroups();
//				for(Iterator<GroupModel> it = setAux.iterator();it.hasNext();){
//					group = it.next();
//					printStream.println(group.getId());
//				}
//			}
//
//			/*--Listing fowardtracking relationship of each entity--*/	
//
//			printStream.println("Number of fowardtracking relationship");
//			printStream.println(umpstProject.getMapEntity().get(keyEntity).getFowardTrackingRelationship().size());
//			if (umpstProject.getMapEntity().get(keyEntity).getFowardTrackingRelationship().size()>0){
//				printStream.println("Fowardtracking from relationship of this entity");
//				Set<RelationshipModel> setAux = umpstProject.getMapEntity().get(keyEntity).getFowardTrackingRelationship();
//				for (Iterator<RelationshipModel> it = setAux.iterator();it.hasNext();){
//					relationship = it.next();
//					printStream.println(relationship.getId());
//				}
//			}
//			//end of FOR entities
//			printStream.println("END OF ENTITY");
//		}	
//
//
//		/*-- Adding Hypothesis details --*/
//
//		printStream.println("****************");			
//		printStream.println("Hypothesis Details");
//
//		Set<String> keysHypothesis = umpstProject.getMapHypothesis().keySet();
//		TreeSet<String> sortedKeysHypothesis = new TreeSet<String>(keysHypothesis);
//
//		for (String keyHypothesis : sortedKeysHypothesis){
//
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getId());
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getName());
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getAuthor());
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getDate());
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getComments());
//
//			if (umpstProject.getMapHypothesis().get(keyHypothesis).getFather()==null){
//				printStream.println(NULL);
//			}
//			else{
//				printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getFather().getId());
//			}
//
//			/*--Listing goals related of each hypothesis --*/	
//			printStream.println("Number of goals related");
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getGoalRelated().size());
//			if(umpstProject.getMapHypothesis().get(keyHypothesis).getGoalRelated().size()>0){
//				printStream.println("Goals related with this hypothesis");
//				Set<GoalModel> setAux = umpstProject.getMapHypothesis().get(keyHypothesis).getGoalRelated();
//				for (Iterator<GoalModel> it = setAux.iterator();it.hasNext();){
//					goal = it.next();
//					printStream.println(goal.getId());
//				}
//			}
//
//			/*--Listing subHypothesis of this hypothesis--*/			
//			printStream.println("Number of subHypothesis");
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().size());
//			if (umpstProject.getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().size()>0){
//				printStream.println("Subhypothesis of this hypothesis");
//				keys = umpstProject.getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().keySet();
//				sortedKeys = new TreeSet<String>(keys);
//				for (String key : sortedKeys){
//					printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().get(key).getId());
//				}
//			}
//
//			/*--Listing fowarTracking entity of this hypothesis--*/		
//			printStream.println("Number of foward tracking entity");
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getFowardTrackingEntity().size());
//			if (umpstProject.getMapHypothesis().get(keyHypothesis).getFowardTrackingEntity().size()>0){
//				printStream.println("Foward tracking entity of this hypothesis");
//				Set<EntityModel> setAux = umpstProject.getMapHypothesis().get(keyHypothesis).getFowardTrackingEntity();
//				for (Iterator<EntityModel> it = setAux.iterator();it.hasNext();){
//					entity = it.next();
//					printStream.println(entity.getId());
//				}
//			}
//
//			/*--Listing fowardtracking groups of this hypothesis--*/
//			printStream.println("Number of foward tracking groups");
//			printStream.println(umpstProject.getMapHypothesis().get(keyHypothesis).getFowardTrackingGroups().size());
//			if (umpstProject.getMapHypothesis().get(keyHypothesis).getFowardTrackingGroups().size()>0){
//				printStream.println("Foward tracking groups of this hypohtesis");
//				Set<GroupModel> setAux = umpstProject.getMapHypothesis().get(keyHypothesis).getFowardTrackingGroups();
//				for (Iterator<GroupModel> it=setAux.iterator();it.hasNext();){
//					group = it.next();
//					printStream.println(group.getId());
//				}
//			}
//
//			printStream.println("END OF HYPOTHESIS");
//		}
//
//
//
//
//
//		/*-- Adding each goal in the map --*/
//
//		printStream.println("********************");						
//		printStream.println("Goals details:");	
//		Set<String> keysGoals = umpstProject.getMapGoal().keySet();
//		TreeSet<String> sortedKeysGoals = new TreeSet<String>(keysGoals);
//
//		for (String key : sortedKeysGoals){
//			printStream.println(umpstProject.getMapGoal().get(key).getId());
//			printStream.println(umpstProject.getMapGoal().get(key).getName());
//			printStream.println(umpstProject.getMapGoal().get(key).getAuthor());
//			printStream.println(umpstProject.getMapGoal().get(key).getDate());
//			printStream.println(umpstProject.getMapGoal().get(key).getComments());
//			if (umpstProject.getMapGoal().get(key).getGoalFather()==null){
//				printStream.println(NULL);
//			}
//			else{
//				printStream.println(umpstProject.getMapGoal().get(key).getGoalFather().getId());	
//			}
//
//			/*--  Adding all subgoals of each goal  --*/
//
//			printStream.println("Number of subgoals");
//			printStream.println(umpstProject.getMapGoal().get(key).getSubgoals().size());
//
//			if (umpstProject.getMapGoal().get(key).getSubgoals().size()>0){
//				printStream.println("Subgoals IDs:");
//				keys = umpstProject.getMapGoal().get(key).getSubgoals().keySet();
//				sortedKeys = new TreeSet<String>(keys);
//				for (String keySub : sortedKeys){
//					printStream.println(umpstProject.getMapGoal().get(key).getSubgoals().get(keySub).getId());
//				}
//			}
//
//
//			/*-- Adding all hypothesis of each goal --*/
//
//			printStream.println("Number of hypothesis of this goal");
//			printStream.println(umpstProject.getMapGoal().get(key).getMapHypothesis().size());
//
//			if (umpstProject.getMapGoal().get(key).getMapHypothesis().size()>0){
//				printStream.println("Hypothesis of this goal:");
//				keys = umpstProject.getMapGoal().get(key).getMapHypothesis().keySet();
//				sortedKeys = new TreeSet<String>(keys);
//				for(String keyHypoGoal : sortedKeys){
//					printStream.println(umpstProject.getMapGoal().get(key).getMapHypothesis().get(keyHypoGoal).getId());
//				}
//			}
//
//
//			/*-- Adding all fowardTrackraking of entities of EACH goal --*/
//
//			printStream.println("Number of entities related with this goal");
//			printStream.println(umpstProject.getMapGoal().get(key).getFowardTrackingEntity().size());
//
//			if (umpstProject.getMapGoal().get(key).getFowardTrackingEntity().size()>0){
//				printStream.println("Entity's traceability of this goal");
//				Set<EntityModel> setAux = umpstProject.getMapGoal().get(key).getFowardTrackingEntity();
//				for(Iterator<EntityModel> it = setAux.iterator() ; it.hasNext(); ){
//					entity = it.next();
//					printStream.println(entity.getId());
//				}
//			}
//
//			/*-- Adding all fowardTracking of Groups of EACH goal--*/
//
//
//			printStream.println("Number of groups related with this goal");
//			printStream.println(umpstProject.getMapGoal().get(key).getFowardTrackingGroups().size());
//
//			if (umpstProject.getMapGoal().get(key).getFowardTrackingGroups().size()>0){
//				printStream.println("group's traceability of this goal");
//				Set<GroupModel> setAux = umpstProject.getMapGoal().get(key).getFowardTrackingGroups();
//				for(Iterator<GroupModel> it = setAux.iterator() ; it.hasNext(); ){
//					group = it.next();
//					printStream.println(group.getId());
//				}
//			}
//
//
//			printStream.println("####");
//		}
//		/*--Listing atributes of each entity--*/	
//
//	}
//
//

}
