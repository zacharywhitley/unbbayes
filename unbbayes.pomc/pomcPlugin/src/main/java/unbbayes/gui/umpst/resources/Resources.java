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
		{"HpAddHyphotesis" , "Add new Hyphotesis"},
		{"HpAddSubgoal" , "Add new Subgoal"},
		{"HpReuseSubgoal" , "Add existent Subgoal"},
		{"HpReturnMainPanel" , "Return to main panel"},
		{"HpReuseHypothesis" , "Add existent Hypothesis"},
		{"HpReturnPreviousPanel" , "Return to previous goal"},
		{"HpSaveGoal" , "Save this goal"},
		{"HpUpdateGoal" , "Update this goal"},
		{"hpSaveHypothesis" , "Save this hypothesis"},
		{"hpSaveSubHypothesis" , "Save this sub-hypothesis"},
		{"hpUpdateHypothesis" , "Update this hypothesis"},
		{"HpSelectSubHipothesis" , "Select one sub hipothesis"},
		{"HpSelectHipothesis" , "Select one hipothesis"},
		{"hpSelectSubGoals" , "Select one sub goal"},
		{"hpGoalsTab" , "Edit goals,queries and envidences"},
		{"hpEntitiesTab" , "Edit entities, atributtes and relationships"},
		{"hpRulesTab" , "Edit deterministic and stochastic rules"},
		{"hpGroupsTab" , "Edit groups"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		
		//##############       Error Messages        ###################//
		
		{"ErGoalDescriptionEmpty" , "Goal description empty!"},
		{"erHypothesisDescriptionEmpty" , "Hypothesis description empty!"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
		
		//##############       Title Dialog Messages   ###################//
				
		{"TtSubhypothesis" , "Sub Hypothesis"},
		{"TtHypothesis" , "Hypothesis"},
		{"ttSubgoals" , "Sub Goals"},
		{"ttGoals" , "Goals"},
		{"ttEntities" , "Entities"},
		{"ttRules" , "Rules"},
		{"ttGroups" , "Groups"},
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
		{"mnOpen" , "Open"},
		{"mnOpenMnemonic" , "O"},
		{"mnSave" , "Save"},
		{"mnSaveMnemonic" , "S"},
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
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
//		{"HpReturnPreviousPanel" , "Return to previous goal"},
		
	};
	
}