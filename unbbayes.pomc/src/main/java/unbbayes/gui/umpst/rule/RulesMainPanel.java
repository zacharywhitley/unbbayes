package unbbayes.gui.umpst.rule;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;

public class RulesMainPanel extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private JSplitPane splitPane;
	private TableRules rulesPanel;
	private RulesSearchPanel rulesSearch;
	
	public RulesMainPanel(UmpstModule janelaPai,UMPSTProject umpstProject) {
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
					getRulesPanel(),getRulesTable());
			splitPane.setDividerLocation(100);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the rulesPanel
	 */
	public TableRules getRulesTable() {
		if(rulesPanel == null ){

			rulesPanel = new TableRules(getFatherPanel(),getUmpstProject());
		}
		return rulesPanel;
	}

	/**
	 * @return the rulesSearch
	 */
	public RulesSearchPanel getRulesPanel() {
		if(rulesSearch == null ){
			rulesSearch = new RulesSearchPanel(getFatherPanel(),getUmpstProject());
			rulesSearch.setBackground(new Color(0xffffff));
		}
		return rulesSearch;
	}
	
	
}
