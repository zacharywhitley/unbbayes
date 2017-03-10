package unbbayes.gui.umpst.group;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;

public class GroupsMainPanel extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private JSplitPane splitPane;
	private TableGroups groupsPanel;
	private GroupsSearchPanel groupsSearch;
	
	public GroupsMainPanel(UmpstModule janelaPai,UMPSTProject umpstProject) {
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		
		this.setLayout(new FlowLayout());
		this.add(getSplitPane());

	}

	/**
	 * @return the splitPane
	 */
	public JSplitPane getSplitPane() {
		if(splitPane == null){
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					getGroupsPanel(),getGroupsTable());
			splitPane.setDividerLocation(100);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the rulesPanel
	 */
	public TableGroups getGroupsTable() {
		if(groupsPanel == null ){

			groupsPanel = new TableGroups(getFatherPanel(),getUmpstProject());
		}
		return groupsPanel;
	}

	/**
	 * @return the groupsSearch
	 */
	public GroupsSearchPanel getGroupsPanel() {
		if(groupsSearch == null ){
			groupsSearch = new GroupsSearchPanel(getFatherPanel(),getUmpstProject());
			groupsSearch.setBackground(new Color(0xffffff));
		}
		return groupsSearch;
	}
	
	
}
