package unbbayes.controller.umpst;

import unbbayes.model.umpst.requirements.Goal;

public class RequirementsController {
	
	public RequirementsController(){	
	}
	
	public void createGoal(String name, String comments, Goal pai){
		
		Goal goal = new Goal(name,comments,pai);
		
	}
	
}
