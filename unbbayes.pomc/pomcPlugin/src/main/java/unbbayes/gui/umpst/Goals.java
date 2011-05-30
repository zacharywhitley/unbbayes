package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

public class Goals extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private TableGoals menuPanel;
	private GoalsSearchPanel requirementsPanel;
	
	public Goals(UmpstModule janelaPai) {
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

			menuPanel = new TableGoals(getJanelaPai());
		}
		return menuPanel;
	}

	/**
	 * @return the requirementsPanel
	 */
	public GoalsSearchPanel getGoalsPanel() {
		if(requirementsPanel == null ){
			requirementsPanel = new GoalsSearchPanel(getJanelaPai());
			requirementsPanel.setBackground(new Color(0xffffff));
		}
		return requirementsPanel;
	}
	
	
}
