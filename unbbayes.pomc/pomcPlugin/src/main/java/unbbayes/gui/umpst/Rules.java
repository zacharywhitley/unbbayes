package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

public class Rules extends IUMPSTPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private TableRules rulesPanel;
	private RulesSearch rulesSearch;
	
	public Rules(UmpstModule janelaPai) {
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
					getGoalsPanel(),getRulesTable());
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

			rulesPanel = new TableRules(getFatherPanel());
		}
		return rulesPanel;
	}

	/**
	 * @return the rulesSearch
	 */
	public RulesSearch getGoalsPanel() {
		if(rulesSearch == null ){
			rulesSearch = new RulesSearch(getFatherPanel());
			rulesSearch.setBackground(new Color(0xffffff));
		}
		return rulesSearch;
	}
	
	
}
