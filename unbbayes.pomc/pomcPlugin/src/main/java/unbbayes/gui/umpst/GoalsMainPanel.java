package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.model.umpst.requirements.GoalModel;

public class GoalsMainPanel extends IUMPSTPanel{
	
	
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private TableHypothesis hypothesisPanel;
	private GoalsAdd goalsPanel;
	private GoalModel goal,goalFather;
	
	
	public GoalsMainPanel(UmpstModule janelaPai, GoalModel goal, GoalModel goalFather) {
		super(janelaPai);
		
		this.goal=goal;
		this.goalFather=goalFather;
		
		this.setLayout(new FlowLayout());
		this.add(getSplitPane());

	}

	/**
	 * @return the splitPane
	 */
	public JSplitPane getSplitPane() {
		if(splitPane == null){
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					getGoalsPanel(goal),getHypothesisTable(goal));
			splitPane.setDividerLocation(400);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the hypothesisPanel
	 */
	public TableHypothesis getHypothesisTable(GoalModel goal) {
		if(hypothesisPanel == null ){
			hypothesisPanel = new TableHypothesis(getJanelaPai(),goal);
		}
		return hypothesisPanel;
	}

	/**
	 * @return the GoalAdd Panel
	 */
	public GoalsAdd getGoalsPanel(GoalModel goal) {
		if(goalsPanel == null ){
			goalsPanel = new GoalsAdd(getJanelaPai(),goal,goalFather);
			goalsPanel.setBackground(new Color(0xffffff));
		}
		return goalsPanel;
	}
	
	
}
