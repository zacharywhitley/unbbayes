package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

public class Goals extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableRequirements menuPanel;
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
	public TableRequirements getGoalsTable() {
		if(menuPanel == null ){
			DefaultTableModel model = new DefaultTableModel();

			menuPanel = new TableRequirements(getJanelaPai());
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
