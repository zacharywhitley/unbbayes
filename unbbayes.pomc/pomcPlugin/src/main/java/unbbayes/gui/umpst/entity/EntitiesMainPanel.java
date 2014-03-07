package unbbayes.gui.umpst.entity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.GuiUtils;

public class EntitiesMainPanel extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableEntities menuPanel;
	private EntitiesSearchPanel requirementsPanel;
	
	public EntitiesMainPanel(UmpstModule parentPanel, 
			UMPSTProject umpstProject) {
		
		super(parentPanel);
		
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
					getEntitiesPanel(),
					getEntitiesTable());
			
			splitPane.setDividerLocation(100);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(GuiUtils.getSearchPanelColor());
		
		}
		
		return splitPane;
	}

	/**
	 * @return the menuPanel
	 */
	public TableEntities getEntitiesTable() {
		if(menuPanel == null ){
			DefaultTableModel model = new DefaultTableModel();

			menuPanel = new TableEntities(getFatherPanel(),getUmpstProject());
		}
		return menuPanel;
	}

	/**
	 * @return the requirementsPanel
	 */
	public EntitiesSearchPanel getEntitiesPanel() {
		if(requirementsPanel == null ){
			requirementsPanel = new EntitiesSearchPanel(getFatherPanel(),getUmpstProject());
			requirementsPanel.setBackground(new Color(0xffffff));
		}
		return requirementsPanel;
	}
	
	
}
