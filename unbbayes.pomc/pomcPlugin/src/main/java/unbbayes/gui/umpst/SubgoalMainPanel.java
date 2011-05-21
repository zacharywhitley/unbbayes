package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import unbbayes.model.umpst.requirements.GoalModel;

public class SubgoalMainPanel extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableRequirements menuPanel;
	private SubgoalsAdd goalsPanel;
	GoalModel goal;
	
	public SubgoalMainPanel(UmpstModule janelaPai) {
		super(janelaPai);
		this.setLayout(new FlowLayout());
		this.add(getSplitPane());

	}

	/**
	 * @return the splitPane
	 */
	public JSplitPane getSplitPane() {
		if(splitPane == null){
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					getGoalsPanel(),getGoalsTable());
			splitPane.setDividerLocation(400);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the menuPanel
	 */
	public TableRequirements getGoalsTable() {
		if(menuPanel == null ){
			menuPanel = new TableRequirements(getJanelaPai());
		}
		return menuPanel;
	}

	/**
	 * @return the GoalAdd Panel
	 */
	public SubgoalsAdd getGoalsPanel() {
		if(goalsPanel == null ){
			goalsPanel = new SubgoalsAdd(getJanelaPai(),goal);
			goalsPanel.setBackground(new Color(0xffffff));
		}
		return goalsPanel;
	}
	
	
}
