package unbbayes.controller.umpst;

import unbbayes.model.umpst.requirements.GoalModel;


public class RequirementsController {
	
	public RequirementsController(){	
	}
	
	public void createGoal(String name, String comments, GoalModel pai){
		
		GoalModel goal = new GoalModel(name,comments,comments,comments,pai);
		
	}
	
}
