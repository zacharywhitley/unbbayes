package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

public class Groups extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private TableGroups groupsPanel;
	private GroupsSearch groupsSearch;
	
	public Groups(UmpstModule janelaPai) {
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

			groupsPanel = new TableGroups(getJanelaPai());
		}
		return groupsPanel;
	}

	/**
	 * @return the groupsSearch
	 */
	public GroupsSearch getGroupsPanel() {
		if(groupsSearch == null ){
			groupsSearch = new GroupsSearch(getJanelaPai());
			groupsSearch.setBackground(new Color(0xffffff));
		}
		return groupsSearch;
	}
	
	
}
