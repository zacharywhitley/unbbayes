package unbbayes.controller.umpst;

import java.util.ArrayList;

import unbbayes.model.umpst.requirements.GoalModel;


public class RequirementsController {
	
	public RequirementsController(){	
	}
	
	public void createGoal(String name, String comments, GoalModel pai, ArrayList<GoalModel> filhos){
		
		GoalModel goal = new GoalModel(name,comments,comments,comments,pai,filhos,null);
		
	}
	
}
