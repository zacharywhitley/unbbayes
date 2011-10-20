package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.model.umpst.project.UMPSTProject;

public class Rules extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private JSplitPane splitPane;
	private TableRules rulesPanel;
	private RulesSearch rulesSearch;
	
	public Rules(UmpstModule janelaPai,UMPSTProject umpstProject) {
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
	public RulesSearch getRulesPanel() {
		if(rulesSearch == null ){
			rulesSearch = new RulesSearch(getFatherPanel(),getUmpstProject());
			rulesSearch.setBackground(new Color(0xffffff));
		}
		return rulesSearch;
	}
	
	
}