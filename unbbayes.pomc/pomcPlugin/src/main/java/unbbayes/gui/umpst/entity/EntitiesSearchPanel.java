package unbbayes.gui.umpst.entity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		panel.add(createLabelEntity());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(createTextEntity());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		//------------------- Button Pane --------------------------------------
		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		
		buttonPane.add(createButtonSearch());
		buttonPane.add(createButtonCancel());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(createButtonAddEntity());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(createButtonRelationship());
		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}

	private Component createButtonRelationship() {
		if (buttonAddRelationship == null){
			buttonAddRelationship = new JButton (IconController.getInstance().getRelationshipIcon());
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
	public JButton createButtonAddEntity() {
		
		if (buttonAddEntity == null){
			buttonAddEntity = new JButton (IconController.getInstance().getAddIconP());
			buttonAddEntity.setToolTipText(resource.getString("hpAddEntity"));
			buttonAddEntity.setForeground(Color.blue);
			buttonAddEntity.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					changePanel(createEntitiesPanel(null));
				}
			});			
		}
		
		return buttonAddEntity;
	} 
	
	/**
	 * @return the buttonCancel
	 */
	public JButton createButtonCancel() {
		
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
	public JLabel createLabelEntity() {
		
		if(labelEntity == null){
			labelEntity = new JLabel("Search for a entity: ");
			labelEntity.setForeground(Color.white);
		}
		
		return labelEntity;
	}


	/**
	 * @return the buttonSearch
	 */
	public JButton createButtonSearch() {
		
		if(buttonSearch == null){
			buttonSearch = new JButton(IconController.getInstance().getSearchIcon());
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
	public JTextField createTextEntity() {
		
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

		Pattern pattern = Pattern.compile(textEntity.getText()); 
		Matcher m; 
		
		List<EntityModel> result = new ArrayList<EntityModel>(); 
		
		for(EntityModel entity: getUmpstProject().getMapEntity().values()){
			m = pattern.matcher(entity.getName()); 
			if (m.find()){
				result.add(entity); 
			}
		}
		
		Object[][] data = new Object[result.size()][5];
	
		Integer i=0;
	
    	for (EntityModel entity: result) {
    	     
    	 	data[i][0] = entity.getId();
			data[i][1] = entity.getName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
    	}
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    TableEntities entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
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
				data[i][1] = getUmpstProject().getMapEntity().get(key).getName();			
				data[i][2] = "";
				data[i][3] = "";
				i++;
			}
	   
		    UmpstModule pai = getFatherPanel();
		    changePanel(pai.getMenuPanel());
		    
		    TableEntities entitiesTable = pai.getMenuPanel().getEntitiesPane().getEntitiesTable();
		    JTable table = entitiesTable.createTable(columnNames,data);
		    
		    entitiesTable.getScrollPanePergunta().setViewportView(table);
		    entitiesTable.getScrollPanePergunta().updateUI();
		    entitiesTable.getScrollPanePergunta().repaint();
		    entitiesTable.updateUI();
		    entitiesTable.repaint();
	    }
	
	public EntitiesEditionPanel createEntitiesPanel(EntityModel entity){
		
		EntitiesEditionPanel ret = new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(),entity);
		
		return ret;
		
	}
	

}
