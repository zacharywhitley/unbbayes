package unbbayes.gui.umpst.resources;

/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.util.ArrayList;

import unbbayes.gui.resources.GuiResources;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui.umpst package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * @author Laecio Santos (laecio@gmail.com)
 */

public class Resources extends GuiResources {
 
    /** 
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		for (Object[] objects : super.getContents()) {
			list.add(objects);
		}
		for (Object[] objects2 : this.contents) {
			list.add(objects2);
		}
		return list.toArray(new Object[0][0]);
	}
 
	/**
	 * The particular resources for this class
	 */
	static final Object[][] contents =
	{	
		
		//##############       Help Messages         ###################//
		{"HpAddHyphotesis" , "Add new hyphotesis"},
		{"HpAddSubgoal" , "Add new subgoal"},
		{"hpAddGoal" , "Add new goal"},
		{"hpAddEntity" , "Add new entity"}, 
		{"hpAddRelationship" , "Edit relationships"},
		{"hpAddRule" , "Add new rule"},
		{"hpAddGroup" , "Add new group"},
		
		{"HpReuseSubgoal" , "Add existent Subgoal"},
		{"HpReturnMainPanel" , "Return to main panel"},
		{"hpReturnMainPanel" , "Return to main panel"},
		{"HpReuseHypothesis" , "Add existent Hypothesis"},
		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		{"HpSaveGoal" , "Save goal"},
		{"HpUpdateGoal" , "Update goal"},
		{"hpSaveHypothesis" , "Save hypothesis"},
		{"hpSaveSubHypothesis" , "Save sub-hypothesis"},
		{"hpUpdateHypothesis" , "Update hypothesis"},
		{"hpSaveRelationship" , "Save relationship"},
		{"hpUpdateRelationship" , "Update relationship"},
		
		{"hpSaveEntity" , "Save entity"},
		{"hpUpdateEntity" , "Update entity"},
		
		{"hpSaveAttribute" , "Save attribute"},
		{"hpUpdateAttribute" , "Update attribute"},
		
		{"hpSaveRule" , "Save rule"},
		{"hpUpdateRule" , "Update rule"},
		
		{"hpSaveGroup" , "Save group"},
		{"hpUpdateGroup" , "Update group"},
		
		{"hpAddAttribute" , "Add atribute"},
		{"hpAddSubAttribute" , "Add sub atribute"},
		{"hpReuseAttribute" , "Add existent attribute"},
		
		{"hpAddBackEntity" , "Add backtracking entity"},
		{"hpAddBackAttribute" , "Add backtracking attribute"},
		{"hpAddBackRelationship" , "Add backtracking relationship"},
		
		{"HpSelectSubHipothesis" , "Select one sub hipothesis"},
		{"HpSelectHipothesis" , "Select one hipothesis"},
		{"hpSelectSubGoals" , "Select one sub goal"},
		{"hpSelectGoals" , "Select one goal"},
		{"hpSelectAttribute" , "Select one attribute"},
		
		{"hpGoalsTab" , "Edit goals,queries and envidences"},
		{"hpEntitiesTab" , "Edit entities, atributtes and relationships"},
		{"hpRulesTab" , "Edit deterministic and stochastic rules"},
		{"hpGroupsTab" , "Edit groups"},
		
		{"hpSearchGoal" , "Search for a goal"},
		{"hpSearchEntity" , "Search for a entity"},
		{"hpSearchRule" , "Search for a rule"},
		{"hpSearchGroup" , "Search for a group"},
		
		{"hpCleanSearch" , "Clean previous search results"},
		
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		
		//##############       Error Messages        ###################//
		
		{"ErGoalDescriptionEmpty" , "Goal description empty!"},
		{"erEntityDescriptionEmpty" , "Entity description empty!"},
		{"erHypothesisDescriptionEmpty" , "Hypothesis description empty!"},
		{"erIncompatibleVersion" , "File loaded is incompatible whith this version of plugin!"},
		{"erLoadFatal" , "Fatal error in load project."},
		{"erNotUmpFormat" , "This file format is not supported."},
		{"erFileNotFound" , "File not found."},
		{"erSaveFatal" , "Fatal error in save project."},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		{"msSaveSuccessfull" , "File save successfull."},
		{"msLoadSuccessfull" , "File load successfull."},
		
		//##############       Title Dialog Messages   ###################//
				
		{"TtSubhypothesis" , "Sub Hypothesis"},
		{"TtHypothesis" , "Hypothesis"},
		{"ttSubgoals" , "Sub Goals"},
		{"ttGoals" , "Goals"},
		{"ttEntities" , "Entities"},
		{"ttRules" , "Rules"},
		{"ttGroups" , "Groups"},
		{"ttAttributes" , "Attributes"},
		{"ttRule" , "Rule"},
		
		{"ttEntity" , "Entity"},
		{"ttAtribute" , "Atribute"},
		{"ttRelationship" , "Relationship"},
		{"ttRule" , "Rule"},
		{"ttGroup" , "Group"},
		
		{"ttType" , "Type: "},
		
		{"ttRuleDetails" , "Rule Details"},
		{"ttGroupDetails" , "Group Details"},
		
		{"tpDeterministic" , "Deterministic"},
		{"tpNoDeterministic" , "Not Deterministic"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		//##############       Button Messages   ###################//
		
		{"BtnSelectSubHypothesis" , "Select the Sub Hypothesis"},
		{"btnReturn" , "Return"},
		{"btnAdd" , "Add"},
		{"btnUpdate" , "Update"},
		{"btnCopy" , "Copy"},
		{"btnRemove" , "Remove"},
		{"btnSave" , "Save"},
		{"btnSelect" , "Select"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		
		//##############       Menu Messages   ###################//
		
		{"mnFile" , "File"},
		{"mnFileMnemonic" , "F"},
		{"mnNewFile" , "New Project"},
		{"mnNewFileMnemonic" , "N"},
		{"mnOpen" , "Open"},
		{"mnOpenMnemonic" , "O"},
		{"mnSave" , "Save"},
		{"mnSaveMnemonic" , "S"},
		{"mnSaveAs" , "Save as"},
		{"mnSaveAsMnemonic" , "V"},
		{"mnHelp" , "Help"},
		{"mnHelpMnemonic" , "H"},
		{"mnHelpContents" , "Help"},
		{"mnHelpContentsMnemonic" , "H"},
		{"mnAbout" , "About"},
		{"mnAboutMnemonic" , "A"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
	};
	
}