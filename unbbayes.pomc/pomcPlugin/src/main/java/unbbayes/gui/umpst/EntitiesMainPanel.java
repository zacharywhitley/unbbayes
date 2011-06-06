package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import unbbayes.model.umpst.entities.EntityModel;

public class EntitiesMainPanel extends IUMPSTPanel{
	
	private JSplitPane splitPane;
	private TableEntities tableEntities;
	private EntitiesAdd entitiesPanel;
	private EntityModel entity;
	
	public EntitiesMainPanel(UmpstModule janelaPai, EntityModel entity) {
		super(janelaPai);
		this.entity=entity;
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
			splitPane.setDividerLocation(300);
			splitPane.setPreferredSize(new Dimension(800,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the tableEntities
	 */
	public TableEntities getEntityTable() {
		if(tableEntities == null ){
			DefaultTableModel model = new DefaultTableModel();

			tableEntities = new TableEntities(getJanelaPai());
		}
		return tableEntities;
	}

	/**
	 * @return the entitiesPanel
	 */
	public EntitiesAdd getEntityPanel() {
		if(entitiesPanel == null ){
			entitiesPanel = new EntitiesAdd(getJanelaPai(),entity);
			entitiesPanel.setBackground(new Color(0xffffff));
		}
		return entitiesPanel;
	}
	
	
}
