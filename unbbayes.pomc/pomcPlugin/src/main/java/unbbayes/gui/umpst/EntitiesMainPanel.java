package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;

import unbbayes.model.umpst.entities.EntityModel;

public class EntitiesMainPanel extends IUMPSTPanel{
	

	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane, topSplitPane;
	private TableAtribute tableAtribute;
	private EntitiesAdd entitiesPanel;
	private TrackingPanel backtrackingPanel;
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
			//topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,getEntityPanel(),getBacktrackingPane());
			//topSplitPane.setDividerLocation(260);
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					getEntityPanel(entity),getAtributeTable(entity));
			splitPane.setDividerLocation(400);
			splitPane.setPreferredSize(new Dimension(1000,600));
			splitPane.setBackground(new Color(0x4169AA));
		}
		return splitPane;
	}

	/**
	 * @return the tableEntities
	 */
	public TableAtribute getAtributeTable(EntityModel entity) {
		if(tableAtribute == null ){
			tableAtribute = new TableAtribute(getJanelaPai(),entity);
		}
		return tableAtribute;
	}

	/**
	 * @return the entitiesPanel
	 */
	public EntitiesAdd getEntityPanel(EntityModel entity) {
		if(entitiesPanel == null ){
			entitiesPanel = new EntitiesAdd(getJanelaPai(),entity);
			entitiesPanel.setBackground(new Color(0xffffff));
		}
		return entitiesPanel;
	}
	
	
	
	
}
