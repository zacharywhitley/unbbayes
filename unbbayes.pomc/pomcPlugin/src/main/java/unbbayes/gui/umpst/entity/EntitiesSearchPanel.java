package unbbayes.gui.umpst.entity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class EntitiesSearchPanel extends IUMPSTPanel {
		
	private JLabel labelEntity;
	private JButton buttonSearch;
	private JButton buttonAddEntity,
	                buttonCancel;
	private JButton buttonAddRelationship;
	private JTextField textEntity;
	
	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	public EntitiesSearchPanel(UmpstModule janelaPai,
			UMPSTProject umpstProject){
		
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelEntity());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextEntity());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		//------------------- Button Pane --------------------------------------
		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		
		buttonPane.add(getButtonSearch());
		buttonPane.add(getButtonCancel());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonAddEntity());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonRelationship());
		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}

	private Component getButtonRelationship() {
		if (buttonAddRelationship == null){
			buttonAddRelationship = new JButton (IconController.getInstance().getRelationship());
			buttonAddRelationship.setToolTipText(resource.getString("hpAddRelationship"));
			buttonAddRelationship.setForeground(Color.blue);
			buttonAddRelationship.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					changePanel(new RelationshipEditionPanel(getFatherPanel(),getUmpstProject(), null));
				}
			});			
		}
		
		return buttonAddRelationship;
	}
	

	/**
	 * @return the buttonAddEntity
	 */
	public JButton getButtonAddEntity() {
		
		if (buttonAddEntity == null){
			buttonAddEntity = new JButton (IconController.getInstance().getAddIconP());
			buttonAddEntity.setToolTipText(resource.getString("hpAddEntity"));
			buttonAddEntity.setForeground(Color.blue);
			buttonAddEntity.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					changePanel(getEntitiesPanel(null));
				}
			});			
		}
		
		return buttonAddEntity;
	} 
	
	/**
	 * @return the buttonCancel
	 */
	public JButton getButtonCancel() {
		
		if (buttonCancel == null){
			buttonCancel = new JButton (IconController.getInstance().getEditClear());
			buttonCancel.setToolTipText(resource.getString("hpCleanSearch"));
			buttonCancel.setForeground(Color.blue);
			
			buttonCancel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					textEntity.setText("");
					returnTableEntities();
				}
			});
		}
		
		return buttonCancel;
	} 
	
	
	/**
	 * @return the labelEntity
	 */
	public JLabel getLabelEntity() {
		
		if(labelEntity == null){
			labelEntity = new JLabel("Search for a entity: ");
			labelEntity.setForeground(Color.white);
		}
		
		return labelEntity;
	}


	/**
	 * @return the buttonSearch
	 */
	public JButton getButtonSearch() {
		
		if(buttonSearch == null){
			buttonSearch = new JButton(IconController.getInstance().getSearch());
			buttonSearch.setToolTipText(resource.getString("hpSearchEntity"));
			buttonSearch.setForeground(Color.blue);
		}
		buttonSearch.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (!textEntity.getText().equals("")){
					updateTableEntities();
				}
				else{
					JOptionPane.showMessageDialog(null, "Seach is empty!");
				}
			}
		});
		
		return buttonSearch;
	}


	/**
	 * @return the textEntity
	 */
	public JTextField getTextEntity() {
		
		if (textEntity == null){
			textEntity = new JTextField(10);
		}
		
		textEntity.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (!textEntity.getText().equals("")){
					updateTableEntities();
				}
				else{
					JOptionPane.showMessageDialog(null, "Seach is empty!");
				}
			}
		});
		
		return textEntity;
	}
	
	public void updateTableEntities(){
    	String[] columnNames = {"ID","Entity","",""};
		Set<EntityModel> aux = getUmpstProject().getMapSearchEntity().get(textEntity.getText()).getEntitiesRelated();
		EntityModel entity;
		Object[][] data = new Object[getUmpstProject().getMapSearchEntity().get(textEntity.getText()).getEntitiesRelated().size()][4];
		Integer i=0;
		
	    
    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
    	     entity = it.next();  // No downcasting required.
    	     
    	 	data[i][0] = entity.getId();
			data[i][1] = entity.getEntityName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
    	}
    	
	    
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    EntitiesTable entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
	    JTable table = entitiesTable.createTable(columnNames,data);
	    
	    entitiesTable.getScrollPanePergunta().setViewportView(table);
	    entitiesTable.getScrollPanePergunta().updateUI();
	    entitiesTable.getScrollPanePergunta().repaint();
	    entitiesTable.updateUI();
	    entitiesTable.repaint();
    }
	
	
	   public void returnTableEntities(){
	    	String[] columnNames = {"ID","Entity","",""};	    
		    
			Object[][] data = new Object[getUmpstProject().getMapEntity().size()][4];
			Integer i=0;
		    
			Set<String> keys = getUmpstProject().getMapEntity().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				data[i][0] = getUmpstProject().getMapEntity().get(key).getId();
				data[i][1] = getUmpstProject().getMapEntity().get(key).getEntityName();			
				data[i][2] = "";
				data[i][3] = "";
				i++;
			}
	   
		    UmpstModule pai = getFatherPanel();
		    changePanel(pai.getMenuPanel());
		    
		    EntitiesTable entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
		    JTable table = entitiesTable.createTable(columnNames,data);
		    
		    entitiesTable.getScrollPanePergunta().setViewportView(table);
		    entitiesTable.getScrollPanePergunta().updateUI();
		    entitiesTable.getScrollPanePergunta().repaint();
		    entitiesTable.updateUI();
		    entitiesTable.repaint();
	    }
	
	public EntitiesEditionPanel getEntitiesPanel(EntityModel entity){
		
		EntitiesEditionPanel ret = new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(),entity);
		
		return ret;
		
	}
	

}
