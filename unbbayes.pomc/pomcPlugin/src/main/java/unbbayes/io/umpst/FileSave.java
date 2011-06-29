package unbbayes.io.umpst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.rules.RulesModel;

public class FileSave {
	

	public static final String NULL = "null";
	
	private EntityModel entity;
	private RulesModel rule;
	private GroupsModel group;
	private Set<String> keys;
	private TreeSet<String> sortedKeys;
	private RelationshipModel relationship;
	private GoalModel goal;
	
	 public void saveUbf(File file) throws FileNotFoundException{
			
			//File file = new File("images/file.ump");
		 	
/*-- Listing the overall data of the map --*/
			PrintStream printStream = new PrintStream(new FileOutputStream(new File(file.getPath()+".ump")));
			
			keys = UMPSTProject.getInstance().getMapGoal().keySet();
			sortedKeys = new TreeSet<String>(keys);
			printStream.println("Number of goals cadastred");
			printStream.println(UMPSTProject.getInstance().getMapGoal().size());
			if (UMPSTProject.getInstance().getMapGoal().size()>0){
				printStream.println("IDs of all goals");
				for (String key : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getId());
				}
			}
			printStream.println("Number of hypothesis cadastred");
			printStream.println(UMPSTProject.getInstance().getMapHypothesis().size());
			
			if(UMPSTProject.getInstance().getMapHypothesis().size()>0){
				printStream.println("IDs of all hypothesis");
				keys = UMPSTProject.getInstance().getMapHypothesis().keySet();
				sortedKeys = new TreeSet<String>(keys);
				for (String keyHypo : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypo).getId());
				}
			}
			
			
			printStream.println("Number of entites cadastred");
			printStream.println(UMPSTProject.getInstance().getMapEntity().size());
			
			if (UMPSTProject.getInstance().getMapEntity().size()>0){
				printStream.println("IDs of all entities");
				keys = UMPSTProject.getInstance().getMapEntity().keySet();
				sortedKeys = new TreeSet<String>(keys);
				for (String keyEnt : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEnt).getId());
				}
			}
			
			printStream.println("Number of atributes cadastred");
			printStream.println(UMPSTProject.getInstance().getMapAtribute().size());
			
			if (UMPSTProject.getInstance().getMapAtribute().size()>0){
				printStream.println("IDs of all atributes");
				keys = UMPSTProject.getInstance().getMapAtribute().keySet();
				sortedKeys = new TreeSet<String>(keys);
				for (String keyAtr : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtr).getId());
				}
			}
			
			printStream.println("Number of relationship cadastred");
			printStream.println(UMPSTProject.getInstance().getMapRelationship().size());
			
			if (UMPSTProject.getInstance().getMapRelationship().size()>0){
				printStream.println("IDs of all relationship");
				keys = UMPSTProject.getInstance().getMapRelationship().keySet();
				sortedKeys = new TreeSet<String>(keys);
				for (String keyRela : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRela).getId());
				}
			}
			
			printStream.println("Number of rules cadastred");
			printStream.println(UMPSTProject.getInstance().getMapRules().size());
			
			if (UMPSTProject.getInstance().getMapRules().size()>0){
				printStream.println("IDs of all rules");
				keys = UMPSTProject.getInstance().getMapRules().keySet();
				sortedKeys = new TreeSet<String>(keys);
				for (String keyRule : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getId());
				}
			}
			
			printStream.println("Number of Groups cadastred");
			printStream.println(UMPSTProject.getInstance().getMapGroups().size());
			
			if (UMPSTProject.getInstance().getMapGroups().size()>0){
				printStream.println("IDs of all groups");
				keys = UMPSTProject.getInstance().getMapGroups().keySet();
				sortedKeys = new TreeSet<String>(keys);
				for (String keyGroup : sortedKeys){
					printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getId());
				}
			}
			
/*-- Groups details --*/			
			printStream.println("****************");			
			printStream.println("Groups Details");
			
			Set<String> keysGroups = UMPSTProject.getInstance().getMapGroups().keySet();
			TreeSet<String> sortedKeyGroups = new TreeSet<String>(keysGroups);
			
			for(String keyGroup : sortedKeyGroups){
				
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getId());
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getGroupName());
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getDate());
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getComments());

	/*--Backtacking from goal--*/	
				
				printStream.println("Number of goals related");
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getSize());
				if(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getSize()>0){
					printStream.println("IDs of goals related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingGoal().getModel().getElementAt(i));
					}
				}
				
	/*--Backtacking from hypotheis--*/	
				
				printStream.println("Number of hypothesis related");
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getSize());
				if(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getSize()>0){
					printStream.println("IDs of hypothesis related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingHypothesis().getModel().getElementAt(i));
					}
				}
				
	/*--Backtacking from entities--*/	
				
				printStream.println("Number of entities related");
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getSize());
				if(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getSize()>0){
					printStream.println("IDs of entities related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingEntities().getModel().getElementAt(i));
					}
				}
				
	/*--Backtacking from atributes--*/	
				
				printStream.println("Number of atributes related");
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getSize());
				if(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getSize()>0){
					printStream.println("IDs of atributes related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingAtributes().getModel().getElementAt(i));
					}
				}
				
	/*--Backtacking from relationship--*/	
				
				printStream.println("Number of relationship related");
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getSize());
				if(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getSize()>0){
					printStream.println("IDs of relationship related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRelationship().getModel().getElementAt(i));
					}
				}
				
	/*--Backtacking from rules--*/	
				
				printStream.println("Number of rules related");
				printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getSize());
				if(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getSize()>0){
					printStream.println("IDs of rules related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapGroups().get(keyGroup).getBacktrackingRules().getModel().getElementAt(i));
					}
				}
				
				printStream.println("END OF GROUP");
			}
			
/*-- Rules details --*/			
			printStream.println("****************");			
			printStream.println("Rules Details");
			
			Set<String> keysRules = UMPSTProject.getInstance().getMapRules().keySet();
			TreeSet<String> sortedKeyRules = new TreeSet<String>(keysRules);
			
			for(String keyRule : sortedKeyRules){
				
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getId());
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getRulesName());
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getRuleType());
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getDate());
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getComments());
				
	/*--Backtacking from entities--*/	
				
				printStream.println("Number of entities related");
				printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktracking().getModel().getSize());
				if(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktracking().getModel().getSize()>0){
					printStream.println("IDs of entities related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktracking().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktracking().getModel().getElementAt(i));
					}
				}
			
	/*--Backtacking from atribute--*/	
			
			printStream.println("Number of atributes related");
			printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getSize());
			if(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getSize()>0){
				printStream.println("IDs of atributes related");
				for (int i = 0; i < UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getSize(); i++) {
					printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingAtribute().getModel().getElementAt(i));
				}
			}
		
	 /*--Backtacking from relationship--*/	
		
			printStream.println("Number of relationships related");
			printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getSize());
			if(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getSize()>0){
				printStream.println("IDs of relationships related");
				for (int i = 0; i < UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getSize(); i++) {
					printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getBacktrackingRelationship().getModel().getElementAt(i));
				}
			}
			
	/*--Fowardtracking groups--*/		
			
			printStream	.println("Number of groups related");
			printStream.println(UMPSTProject.getInstance().getMapRules().get(keyRule).getFowardTrackingGroups().size());
			if(UMPSTProject.getInstance().getMapAtribute().get(keyRule).getFowardTrackingGroups().size()>0){
				printStream.println("Fowardtracking groups of this atribute");
				Set<GroupsModel> setAux = UMPSTProject.getInstance().getMapAtribute().get(keyRule).getFowardTrackingGroups();
				for (Iterator<GroupsModel> it = setAux.iterator();it.hasNext();){
					group = it.next();
					printStream.println(group.getId());
				}
			}
			printStream.println("END OF RULE");
		}
			
/*-- relationship details --*/			
			printStream.println("****************");			
			printStream.println("relationshp Details");
			
			Set<String> keysRelationship = UMPSTProject.getInstance().getMapRelationship().keySet();
			TreeSet<String> sortedKeyRelationship = new TreeSet<String>(keysRelationship);
			
			for(String keyRelationship : sortedKeyRelationship){
				
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getId());
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getRelationshipName());
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getDate());
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getComments());
				
	/*--Backtacking from entities--*/	
				
				printStream.println("Number of entities related");
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingEntity().getModel().getSize());
				if(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingEntity().getModel().getSize()>0){
					printStream.println("IDs of entities related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingEntity().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingEntity().getModel().getElementAt(i));
					}
				}
				
    /*--Backtacking from atribute--*/	
				
				printStream.println("Number of atribute related");
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingAtribute().getModel().getSize());
				if(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingAtribute().getModel().getSize()>0){
					printStream.println("IDs of atribute related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingAtribute().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingAtribute().getModel().getElementAt(i));
					}
				}
				
    /*--Backtacking from goals--*/	
				
				printStream.println("Number of goals related");
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingGoal().getModel().getSize());
				if(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingGoal().getModel().getSize()>0){
					printStream.println("IDs of goals related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingGoal().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingGoal().getModel().getElementAt(i));
					}
				}
				
    /*--Backtacking from hypothesis--*/	
				
				printStream.println("Number of hypothesis related");
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().getModel().getSize());
				if(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().getModel().getSize()>0){
					printStream.println("IDs of hypothesis related");
					for (int i = 0; i < UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getBacktrackingHypothesis().getModel().getElementAt(i));
					}
				}
		/*--Fowardtracking rules--*/		
				
				printStream	.println("Number of rules related");
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getFowardtrackingRules().size());
				if(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getFowardtrackingRules().size()>0){
					printStream.println("Fowardtracking rules of this atribute");
					Set<RulesModel> setAux = UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getFowardtrackingRules();
					for (Iterator<RulesModel> it = setAux.iterator();it.hasNext();){
						rule = it.next();
						printStream.println(rule.getId());
					}
				}
		/*--Fowardtracking groups--*/		
	
				printStream	.println("Number of groups related");
				printStream.println(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getFowardtrackingGroups().size());
				if(UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getFowardtrackingGroups().size()>0){
					printStream.println("Fowardtracking groups of this atribute");
					Set<GroupsModel> setAux = UMPSTProject.getInstance().getMapRelationship().get(keyRelationship).getFowardtrackingGroups();
					for (Iterator<GroupsModel> it = setAux.iterator();it.hasNext();){
						group = it.next();
						printStream.println(group.getId());
					}
				}
				
				printStream.println("END OF RELATIONSHIP");
			}
			
/*-- Atributes Details --*/	
			printStream.println("****************");			
			printStream.println("Atributes Details");
			
			Set<String> keysAtributes = UMPSTProject.getInstance().getMapAtribute().keySet();
			TreeSet<String> sortedKeyAtributes = new TreeSet<String>(keysAtributes);
			
			for(String keyAtribute : sortedKeyAtributes){
				
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getId());
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getAtributeName());
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getDate());
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getComments());
				
				if (UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFather()==null){
					printStream.println(NULL);
				}
				else{
					printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFather().getId());
				}
				
		/*--Entities related --*/		
				printStream.println("Number of entities related with this atribute");
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getEntityRelated().size());
				if (UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getEntityRelated().size()>0){
					printStream.println("Entities related with this atribute");
					Set<EntityModel> setAux = UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getEntityRelated();
					for (Iterator<EntityModel> it=setAux.iterator();it.hasNext();){
						entity = it.next();
						printStream.println(entity.getId());
					}
				}
		/*--Subatributes--*/		
				printStream.println("Number of sub-atributes");
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getMapSubAtributes().size());
				if (UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getMapSubAtributes().size()>0){
					printStream.println("Sub-atributes of this atribute");
					keys = UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getMapSubAtributes().keySet();
					sortedKeys = new TreeSet<String>(keys);
					for(String keySubAtr:sortedKeys){
						printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getMapSubAtributes().get(keySubAtr).getId());
					}
				}
		/*--Fowardtracking relationship--*/		
				printStream	.println("Number of relationship related");
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingRelationship().size());
				if(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingRelationship().size()>0){
					printStream.println("Fowardtracking relationship of this atribute");
					Set<RelationshipModel> setAux = UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingRelationship();
					for (Iterator<RelationshipModel> it = setAux.iterator();it.hasNext();){
						relationship = it.next();
						printStream.println(relationship.getId());
					}
				}
		/*--Fowardtracking rules--*/		
	
				printStream	.println("Number of rules related");
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingRules().size());
				if(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingRules().size()>0){
					printStream.println("Fowardtracking rules of this atribute");
					Set<RulesModel> setAux = UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingRules();
					for (Iterator<RulesModel> it = setAux.iterator();it.hasNext();){
						rule = it.next();
						printStream.println(rule.getId());
					}
				}
		/*--Fowardtracking groups--*/		
	
				printStream	.println("Number of groups related");
				printStream.println(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingGroups().size());
				if(UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingGroups().size()>0){
					printStream.println("Fowardtracking groups of this atribute");
					Set<GroupsModel> setAux = UMPSTProject.getInstance().getMapAtribute().get(keyAtribute).getFowardTrackingGroups();
					for (Iterator<GroupsModel> it = setAux.iterator();it.hasNext();){
						group = it.next();
						printStream.println(group.getId());
					}
				}
				
			printStream.println("END OF ATRIBUTE");
			}

				
			/*-- Adding entity details --*/
			printStream.println("****************");			
			printStream.println("Entities Details");
			
			Set<String> keysEntities = UMPSTProject.getInstance().getMapEntity().keySet();
			TreeSet<String> sortedKeyEntities = new TreeSet<String>(keysEntities);
			
			for(String keyEntity : sortedKeyEntities){
				
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getId());
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getEntityName());
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getDate());
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getComments());
				
	/*--Listing atributes of each entity--*/	
				
				printStream.println("Number of atributes of this entity");
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getMapAtributes().size());
				if (UMPSTProject.getInstance().getMapEntity().get(keyEntity).getMapAtributes().size()>0){
					printStream.println("Atributes of this entity");
					keys = UMPSTProject.getInstance().getMapEntity().get(keyEntity).getMapAtributes().keySet();
					sortedKeys = new TreeSet<String>(keys);
					for(String keyAtrEnt : sortedKeys)
						printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getMapAtributes().get(keyAtrEnt).getId());
				}
	/*--Listing backtracking from goals of each entity--*/				
				printStream.println("Number of backtracking from goals");
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktracking().getModel().getSize());
				if (UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktracking().getModel().getSize()>0){
					printStream.println("Backtracking from goals of this entity");
					for (int i = 0; i < UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktracking().getModel().getSize(); i++) {
						printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktracking().getModel().getElementAt(i));
					}
						
					
				}
	/*--Listing backtracking from hypothesis of each entity--*/			
				printStream.println("Number of backtracking from hypothesis");
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getSize());
				if (UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getSize()>0){
					printStream.println("Backtracking from hypothesis of this entity");
					for (int i = 0; i <(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getSize()); i++) {
						printStream.println((UMPSTProject.getInstance().getMapEntity().get(keyEntity).getBacktrackingHypothesis().getModel().getElementAt(i)));
					}
				}
				
	/*--Listing fowardtracking rules of each entity--*/			
				
				printStream.println("Number of fowardtracking rules");
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingRules().size());
				if (UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingRules().size()>0){
					printStream.println("Fowartracking from rules of this entity");
					Set<RulesModel> setAux = UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingRules(); 
					for (Iterator<RulesModel> it = setAux.iterator() ; it.hasNext();){
						rule = it.next();
						printStream.println(rule.getId());
					}
				}
				
	/*-- Listing fowardtracking groups of each entity--*/	
				
				printStream.println("Number of fowardtracking groups");
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingGroups().size());
				if(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingGroups().size()>0){
					printStream.println("Fowardtracking from groups of this entity");
					Set<GroupsModel> setAux = UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingGroups();
					for(Iterator<GroupsModel> it = setAux.iterator();it.hasNext();){
						group = it.next();
						printStream.println(group.getId());
					}
				}
				
	/*--Listing fowardtracking relationship of each entity--*/	
				
				printStream.println("Number of fowardtracking relationship");
				printStream.println(UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingRelationship().size());
				if (UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingRelationship().size()>0){
					printStream.println("Fowardtracking from relationship of this entity");
					Set<RelationshipModel> setAux = UMPSTProject.getInstance().getMapEntity().get(keyEntity).getFowardTrackingRelationship();
					for (Iterator<RelationshipModel> it = setAux.iterator();it.hasNext();){
						relationship = it.next();
						printStream.println(relationship.getId());
					}
				}
			//end of FOR entities
				printStream.println("END OF ENTITY");
			}	

			
/*-- Adding Hypothesis details --*/
			
			printStream.println("****************");			
			printStream.println("Hypothesis Details");
			
			Set<String> keysHypothesis = UMPSTProject.getInstance().getMapHypothesis().keySet();
			TreeSet<String> sortedKeysHypothesis = new TreeSet<String>(keysHypothesis);
			
			for (String keyHypothesis : sortedKeysHypothesis){
				
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getId());
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getHypothesisName());
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getDate());
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getComments());

				if (UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFather()==null){
					printStream.println(NULL);
				}
				else{
					printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFather().getId());
				}
				
	/*--Listing goals related of each hypothesis --*/	
				printStream.println("Number of goals related");
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getGoalRelated().size());
				if(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getGoalRelated().size()>0){
					printStream.println("Goals related with this hypothesis");
					Set<GoalModel> setAux = UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getGoalRelated();
					for (Iterator<GoalModel> it = setAux.iterator();it.hasNext();){
						goal = it.next();
						printStream.println(goal.getId());
					}
				}
					
	/*--Listing subHypothesis of this hypothesis--*/			
				printStream.println("Number of subHypothesis");
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().size());
				if (UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().size()>0){
					printStream.println("Subhypothesis of this hypothesis");
					keys = UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().keySet();
					sortedKeys = new TreeSet<String>(keys);
					for (String key : sortedKeys){
						printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getMapSubHypothesis().get(key).getId());
					}
				}

	/*--Listing fowarTracking entity of this hypothesis--*/		
				printStream.println("Number of foward tracking entity");
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFowardTrackingEntity().size());
				if (UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFowardTrackingEntity().size()>0){
					printStream.println("Foward tracking entity of this hypothesis");
					Set<EntityModel> setAux = UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFowardTrackingEntity();
					for (Iterator<EntityModel> it = setAux.iterator();it.hasNext();){
						entity = it.next();
						printStream.println(entity.getId());
					}
				}
				
	/*--Listing fowardtracking groups of this hypothesis--*/
				printStream.println("Number of foward tracking groups");
				printStream.println(UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFowardTrackingGroups().size());
				if (UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFowardTrackingGroups().size()>0){
					printStream.println("Foward tracking groups of this hypohtesis");
					Set<GroupsModel> setAux = UMPSTProject.getInstance().getMapHypothesis().get(keyHypothesis).getFowardTrackingGroups();
					for (Iterator<GroupsModel> it=setAux.iterator();it.hasNext();){
						group = it.next();
						printStream.println(group.getId());
					}
				}
				
			printStream.println("END OF HYPOTHESIS");
			}
			
			
			

				
/*-- Adding each goal in the map --*/
			
			printStream.println("********************");						
			printStream.println("Goals details:");	
			Set<String> keysGoals = UMPSTProject.getInstance().getMapGoal().keySet();
			TreeSet<String> sortedKeysGoals = new TreeSet<String>(keysGoals);
			
			for (String key : sortedKeysGoals){
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getId());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getGoalName());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getAuthor());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getDate());
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getComments());
				if (UMPSTProject.getInstance().getMapGoal().get(key).getGoalFather()==null){
					printStream.println(NULL);
				}
				else{
					printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getGoalFather().getId());	
				}
				
    /*--  Adding all subgoals of each goal  --*/
				
				printStream.println("Number of subgoals");
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size());

				if (UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size()>0){
					printStream.println("Subgoals IDs:");
					keys = UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().keySet();
					sortedKeys = new TreeSet<String>(keys);
					for (String keySub : sortedKeys){
						printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().get(keySub).getId());
					}
				}
				

    /*-- Adding all hypothesis of each goal --*/
				
				printStream.println("Number of hypothesis of this goal");
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().size());

				if (UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().size()>0){
					printStream.println("Hypothesis of this goal:");
					keys = UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().keySet();
					sortedKeys = new TreeSet<String>(keys);
					for(String keyHypoGoal : sortedKeys){
						printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getMapHypothesis().get(keyHypoGoal).getId());
					}
				}
				
				
	/*-- Adding all fowardTrackraking of entities of EACH goal --*/
				
				printStream.println("Number of entities related with this goal");
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingEntity().size());
				
				if (UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingEntity().size()>0){
					printStream.println("Entity's traceability of this goal");
					Set<EntityModel> setAux = UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingEntity();
					for(Iterator<EntityModel> it = setAux.iterator() ; it.hasNext(); ){
						entity = it.next();
						printStream.println(entity.getId());
					}
				}
				
	/*-- Adding all fowardTracking of Groups of EACH goal--*/
				
				
				printStream.println("Number of groups related with this goal");
				printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingGroups().size());
				
				if (UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingGroups().size()>0){
					printStream.println("group's traceability of this goal");
					Set<GroupsModel> setAux = UMPSTProject.getInstance().getMapGoal().get(key).getFowardTrackingGroups();
					for(Iterator<GroupsModel> it = setAux.iterator() ; it.hasNext(); ){
						group = it.next();
						printStream.println(group.getId());
					}
				}
				
				
				printStream.println("####");
			}
	/*--Listing atributes of each entity--*/	
			
		}
	 


}
