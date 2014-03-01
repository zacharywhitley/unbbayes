package unbbayes.gui.umpst.goal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;

public class GoalsMainPanel extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private JSplitPane splitPane;
	private TableGoals menuPanel;
	private GoalsSearchPanel requirementsPanel;
	
	public GoalsMainPanel(UmpstModule janelaPai,
			UMPSTProject umpstProject) {
		
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		
		this.setLayout(new FlowLayout());
		this.add(createSplitPane());
	}

	/**
	 * @return the splitPane
	 */
	public JSplitPane createSplitPane() {
		if(splitPane == null){
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					getGoalsPanel(),getGoalsTable());
			splitPane.setDividerLocation(100);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the menuPanel
	 */
	public TableGoals getGoalsTable() {
		if(menuPanel == null ){

			menuPanel = new TableGoals(getFatherPanel(),getUmpstProject());
		}
		return menuPanel;
	}

	/**
	 * @return the requirementsPanel
	 */
	public GoalsSearchPanel getGoalsPanel() {
		if(requirementsPanel == null ){
			requirementsPanel = new GoalsSearchPanel(getFatherPanel(),getUmpstProject());
			requirementsPanel.setBackground(new Color(0xffffff));
		}
		return requirementsPanel;
	}
	
	
}
