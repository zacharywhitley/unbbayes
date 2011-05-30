package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.model.umpst.requirements.GoalModel;

public class SubgoalMainPanel extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private TableHypothesis hypothesisPanel;
	private SubgoalsAdd goalsPanel;
	GoalModel goal,goalFather;
	
	public SubgoalMainPanel(UmpstModule janelaPai,GoalModel goal, GoalModel goalFather) {
		super(janelaPai);
		
		this.goal=goal;
		this.goalFather =goalFather;
		
		this.setLayout(new FlowLayout());
		this.add(getSplitPane());

	}

	/**
	 * @return the splitPane
	 */
	public JSplitPane getSplitPane() {
		if(splitPane == null){
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					getSubgoalsPanel(),getGoalsTable());
			splitPane.setDividerLocation(400);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the hypothesisPanel
	 */
	public TableHypothesis getGoalsTable() {
		if(hypothesisPanel == null ){
			hypothesisPanel = new TableHypothesis(getJanelaPai(),goal);
		}
		return hypothesisPanel;
	}

	/**
	 * @return the GoalAdd Panel
	 */
	public SubgoalsAdd getSubgoalsPanel() {
		if(goalsPanel == null ){
			goalsPanel = new SubgoalsAdd(getJanelaPai(),goal,goalFather);
			goalsPanel.setBackground(new Color(0xffffff));
		}
		return goalsPanel;
	}
	
	
}
