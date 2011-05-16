package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class EntityMainPanel extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableRequirements menuPanel;
	private EntitiesSearchPanel requirementsPanel;
	
	public EntityMainPanel(UmpstModule janelaPai) {
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
					getEntityPanel(),getEntityTable());
			splitPane.setDividerLocation(100);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the menuPanel
	 */
	public TableRequirements getEntityTable() {
		if(menuPanel == null ){
			menuPanel = new TableRequirements(getJanelaPai());
		}
		return menuPanel;
	}

	/**
	 * @return the requirementsPanel
	 */
	public EntitiesSearchPanel getEntityPanel() {
		if(requirementsPanel == null ){
			requirementsPanel = new EntitiesSearchPanel(getJanelaPai());
			requirementsPanel.setBackground(new Color(0xffffff));
		}
		return requirementsPanel;
	}
	
	
}
