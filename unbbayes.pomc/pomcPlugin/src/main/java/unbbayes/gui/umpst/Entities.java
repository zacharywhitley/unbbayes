package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

public class Entities extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableEntities menuPanel;
	private EntitiesSearchPanel requirementsPanel;
	
	public Entities(UmpstModule janelaPai) {
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
					getEntitiesPanel(),getEntitiesTable());
			splitPane.setDividerLocation(100);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the menuPanel
	 */
	public TableEntities getEntitiesTable() {
		if(menuPanel == null ){
			DefaultTableModel model = new DefaultTableModel();

			menuPanel = new TableEntities(getFatherPanel());
		}
		return menuPanel;
	}

	/**
	 * @return the requirementsPanel
	 */
	public EntitiesSearchPanel getEntitiesPanel() {
		if(requirementsPanel == null ){
			requirementsPanel = new EntitiesSearchPanel(getFatherPanel());
			requirementsPanel.setBackground(new Color(0xffffff));
		}
		return requirementsPanel;
	}
	
	
}
