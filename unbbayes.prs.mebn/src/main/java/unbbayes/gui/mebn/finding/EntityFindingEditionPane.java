/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui.mebn.finding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.exception.InvalidOperationException;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.ReservedWordException;

/**
 * Pane for the user enter with the entity instances of the MEBN in a 
 * graphical form. 
 * 
 * The painel is divided in three parts: 
 * 1) List of Object Entities of the Generative MEBN
 * 2) Painel for enter with the new instance
 * 3) List of instances of the MEBN
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 2.0 (11/15/07)
 *
 */
public class EntityFindingEditionPane extends JPanel {

	private MEBNController mebnController; 
	
	private List<Object> selected = new ArrayList<Object>(); 
	
	private ObjectEntityInstanceOrdereable last; 
	
	private boolean isAdding = true; //user adding a new instance or only editing a instance previous created. 
	
	private ObjectEntityListPane objectEntityListPane; 
	private ObjectEntityInstancePane objectEntityInstancePane; 
	private ObjectEntityInstanceListPane objectEntityInstanceListPane; 
	
  	private JPanel upperPanel; 
  	private JPanel downPanel; 
	
	private IconController iconController = IconController.getInstance(); 
  	private static ResourceBundle resource = 
  		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.mebn.resources.Resources.class.getName());
  	
	public EntityFindingEditionPane(){
		super(); 
	}
	
	public EntityFindingEditionPane(MEBNController mebnController){
		super(new BorderLayout()); 
		
		this.mebnController = mebnController; 
		objectEntityListPane = new ObjectEntityListPane(); 
		objectEntityInstancePane = new ObjectEntityInstancePane(); 
		setObjectEntityInstanceListPane(new ObjectEntityInstanceListPane()); 
		
		upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(objectEntityListPane, BorderLayout.CENTER); 
		upperPanel.add(objectEntityInstancePane, BorderLayout.SOUTH); 
		
		downPanel = new JPanel(new BorderLayout()); 
		downPanel.add(getObjectEntityInstanceListPane(), BorderLayout.CENTER); 
		
		this.add(upperPanel, BorderLayout.CENTER); 
		this.add(downPanel, BorderLayout.PAGE_END); 
	}
	
	public void showEntityInstanceListPane(ObjectEntity entity){
		downPanel.removeAll(); 
		setObjectEntityInstanceListPane(new ObjectEntityInstanceListPane(entity)); 
		downPanel.add(getObjectEntityInstanceListPane(), BorderLayout.CENTER); 
		downPanel.validate(); 
	}
	
	
	


	private class ObjectEntityInstancePane extends JPanel{
		
		private JTextField typeObjectEntity; 
		private JTextField nameObjectEntity; 
		
		//Button for add or edit instances
		private JButton btnAddInstance; 
		
		private JButton btnRemoveInstance; 
		private JButton btnUpInstance; 
		private JButton btnDownInstance; 
		
		private JToolBar barButtons; 
		
		private final JLabel labelType = new JLabel(resource.getString("typeLabel")); 
		private final JLabel labelName = new JLabel(resource.getString("nameLabel")); 
		
		// the following attributes were migrated to Entity
//	    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
//	    private Matcher matcher;	
		
		public ObjectEntityInstancePane(){

			super();
			
			GridBagLayout gridbag = new GridBagLayout(); 
			setLayout(gridbag); 
			
			typeObjectEntity = new JTextField(10); 
			typeObjectEntity.setEditable(false); 
			
			nameObjectEntity = new JTextField(10);
			nameObjectEntity.addFocusListener(new FocusListenerTextField());
			nameObjectEntity.addKeyListener(new KeyAdapter() {
	  			public void keyPressed(KeyEvent e) {
	  				
	  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) 
	  						&& (nameObjectEntity.getText().length()>0)) {
	  					// user pressed "enter" in order to include new entity instance
	  					
	  					try {
	  						String nameValue = nameObjectEntity.getText(0,nameObjectEntity.getText().length());
	  						
	  						// check if there is one (only one) entity to include new instance
	  						if ((selected == null)
	  								|| !(selected instanceof Collection)
	  								|| (((Collection)selected).size() != 1)) {
	  							// selection (of entity to include new instance) was either empty or more than 1 entities were selected
	  							nameObjectEntity.setBackground(MebnToolkit.getColorTextFieldError()); 
	  							nameObjectEntity.setForeground(Color.WHITE); 
	  							JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
	  									resource.getString("selectOnlyOneEntry"), 
	  									resource.getString("operationFail"), 
	  									JOptionPane.ERROR_MESSAGE);
	  							nameObjectEntity.selectAll();
	  							return;
	  						}
	  						
	  						// at this point, we are sure that only 1 element was selected. Extract it
  							Object selectedEntity = ((Collection)selected).iterator().next();
  							
  							// make sure the selected element is really an entity
  							if ((selectedEntity instanceof Entity)) {
  								if (((Entity) selectedEntity).isValidInstanceName(nameValue) ) {
  									addOrEditInstance(); 
  								} else {
  									// invalid name
  		  							nameObjectEntity.setBackground(MebnToolkit.getColorTextFieldError()); 
  		  							nameObjectEntity.setForeground(Color.WHITE); 
  		  							JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
  		  									resource.getString("nameError"), 
  		  									resource.getString("nameException"), 
  		  									JOptionPane.ERROR_MESSAGE);
  		  							nameObjectEntity.selectAll();
  		  							return;
  								}
  							} 
	  					} catch (Exception ble) {
	  						// first, print to default console
	  						ble.printStackTrace();
	  						// then, print stack trace to a string
	  						StringWriter sw = new StringWriter();
	  						ble.printStackTrace(new PrintWriter(sw));
	  						// show the string (stack trace) to user
	  						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
	  									sw.toString(),
	  									ble.getMessage(), 
	  									JOptionPane.ERROR_MESSAGE);
	  						return;
	  					}
	  				}
	  			}
	  			
	  			public void keyReleased(KeyEvent e){
	  				try{
	                    String name = nameObjectEntity.getText(0,nameObjectEntity.getText().length());
							if ((selected == null) || !((ObjectEntity) selected).isValidInstanceName(name)) {
								nameObjectEntity.setBackground(MebnToolkit.getColorTextFieldError()); 
								nameObjectEntity.setForeground(Color.WHITE); 
							}
							else{
								nameObjectEntity.setBackground(MebnToolkit.getColorTextFieldSelected());
								nameObjectEntity.setForeground(Color.BLACK); 
							}
	  				}
	  				catch(Exception efd){
	  					
	  				}
	  				
	  			}
	  		});
			
			btnAddInstance = new JButton(iconController.getMoreIcon()); 
			btnAddInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					addOrEditInstance(); 
				}
			}); 
			
			btnRemoveInstance = new JButton(iconController.getLessIcon()); 
			btnRemoveInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					removeInstance(); 
				}
			}); 
			
			btnUpInstance = new JButton(iconController.getUpIcon()); 
			btnUpInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					upInstance(); 
				}
			}); 
			
			btnDownInstance = new JButton(iconController.getDownIcon()); 
			btnDownInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					downInstance(); 
				}
			}); 
			
			barButtons = new JToolBar();
			barButtons.setLayout(new GridLayout(1, 4)); 
			barButtons.add(btnAddInstance); 
			barButtons.add(btnRemoveInstance); 
			barButtons.add(btnUpInstance); 
			barButtons.add(btnDownInstance); 
			barButtons.setFloatable(false); 
			
			
			gridbag.setConstraints(labelType, 
					getConstraints(0,0, 1, 1, 40, 35,  GridBagConstraints.BOTH, GridBagConstraints.CENTER)); 
			add(labelType); 
			
			gridbag.setConstraints(typeObjectEntity, 
					getConstraints(1,0, 1, 1, 60, 0,  GridBagConstraints.BOTH, GridBagConstraints.CENTER)); 
			add(typeObjectEntity); 
			
			gridbag.setConstraints(labelName, 
					getConstraints(0,1, 1, 1, 0, 35,  GridBagConstraints.BOTH, GridBagConstraints.CENTER)); 
			add(labelName); 
			
			gridbag.setConstraints(nameObjectEntity, 
					getConstraints(1,1, 1, 1, 0, 0,  GridBagConstraints.BOTH, GridBagConstraints.CENTER)); 
			add(nameObjectEntity); 
			
			gridbag.setConstraints(barButtons, 
					getConstraints(0,2, 2, 1, 0, 30,  GridBagConstraints.BOTH, GridBagConstraints.CENTER)); 
			add(barButtons); 
			
		}
		
		private void addOrEditInstance(){
			
			
			//Validations
			for(Object selec: selected){
				if(!(validName(nameObjectEntity.getText(),(Entity) selec))){
					JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
							resource.getString("nameException"),
							resource.getString("error"), 
							JOptionPane.ERROR_MESSAGE);
					return; 
				}
			}
			
			if((nameObjectEntity.getText() == null) || (nameObjectEntity.getText().trim().length() == 0)){
				JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
						resource.getString("nameEmpty"), 
						resource.getString("error"), 
						JOptionPane.ERROR_MESSAGE);
				return; 
			}
			
			//Setting the names
			for(Object selec: selected){
				
				if(isAdding){
					try{
						ObjectEntity objectEntity = (ObjectEntity)selec; 
						if(!objectEntity.isOrdereable()){
					 	    try {
								try {
									mebnController.createEntityIntance(objectEntity, nameObjectEntity.getText());
								} catch (DuplicatedNameException e) {
									JOptionPane.showMessageDialog(null, 
											resource.getString("nameDuplicated"), 
											resource.getString("nameError"), 
											JOptionPane.ERROR_MESSAGE);
								} catch (ReservedWordException e2) {
	  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
	  	  									resource.getString("nameReserved"),
	  	  									resource.getString("nameError"),
	  	  									JOptionPane.ERROR_MESSAGE);
								}
							} catch (InvalidOperationException e) {
								e.printStackTrace();
							}
						}else{
						    try {
								try {
									mebnController.createEntityIntanceOrdereable(
											objectEntity, nameObjectEntity.getText(), last);
								} catch (DuplicatedNameException e) {
									JOptionPane.showMessageDialog(null, 
											resource.getString("nameDuplicated"), 
											resource.getString("nameError"), 
											JOptionPane.ERROR_MESSAGE);
								} catch (ReservedWordException e2) {
	  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
	  	  									resource.getString("nameReserved"),
	  	  									resource.getString("nameError"),
	  	  									JOptionPane.ERROR_MESSAGE);
								}
							} catch (InvalidOperationException e) {
								e.printStackTrace();
							}	
						}
						getObjectEntityInstanceListPane().update();  
						nameObjectEntity.setText(""); 
					}
					catch(EntityInstanceAlreadyExistsException ex){
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
								resource.getString("nameDuplicated"), 
								resource.getString("nameError"), 
								JOptionPane.ERROR_MESSAGE);
					}
				}else{
					try{
						try {
							mebnController.renameEntityIntance((ObjectEntityInstance)selec, nameObjectEntity.getText());
							getObjectEntityInstanceListPane().update();
						} catch (DuplicatedNameException e) {
							JOptionPane.showMessageDialog(mebnController.getScreen(), 
									resource.getString("nameDuplicated"), 
									resource.getString("nameError"), 
									JOptionPane.ERROR_MESSAGE);
						}  catch (ReservedWordException e2) {
	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
  	  									resource.getString("nameReserved"),
  	  									resource.getString("nameError"),
  	  									JOptionPane.ERROR_MESSAGE);
							}
					}
					catch(EntityInstanceAlreadyExistsException ex){
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
								resource.getString("nameDuplicated"), 
								resource.getString("nameError"), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
		private void removeInstance(){
			for(Object selec: selected){
				if(!isAdding){
					if(selec instanceof ObjectEntityInstanceOrdereable){
						mebnController.removeEntityInstanceOrdereable((ObjectEntityInstanceOrdereable)selec); 
					}
					else{
						mebnController.removeEntityInstance((ObjectEntityInstance)selec); 						
					}
					   getObjectEntityInstanceListPane().update();
				}
			}
		}
		
		private void upInstance(){
			for(Object selec: selected){
				if(!isAdding){
					   mebnController.upEntityInstance((ObjectEntityInstanceOrdereable)selec); 
					   getObjectEntityInstanceListPane().update();  
					}
				}
		}
		
		private void downInstance(){
			Collections.reverse(selected);
			for(Object selec: selected){
				if(!isAdding){
					   mebnController.downEntityInstance((ObjectEntityInstanceOrdereable)selec); 
					   getObjectEntityInstanceListPane().update();  
					}
				}
		}
		
		public void disableUpDownButtons(){
			btnUpInstance.setEnabled(false); 
			btnDownInstance.setEnabled(false); 
		}
		
		public void enableUpDownButtons(){
			btnUpInstance.setEnabled(true); 
			btnDownInstance.setEnabled(true); 
		}
		
		public void updateReference(){
			for(Object selec: selected){
				if(isAdding){					
					    typeObjectEntity.setText(((ObjectEntity)selec).getName());
					    nameObjectEntity.setText("");
					    //btnAddInstance.setIcon(iconController.getMoreIcon()); 
					    btnRemoveInstance.setEnabled(false); 
				}else{
					typeObjectEntity.setText(((ObjectEntityInstance)selec).getInstanceOf().getName());
					nameObjectEntity.setText(((ObjectEntityInstance)selec).getName());
				    //btnAddInstance.setIcon(iconController.getEditIcon());
				    btnRemoveInstance.setEnabled(true); 
				}
			}
		}
		
		public boolean validName(String name, Entity selected){
				return selected.isValidInstanceName(name);
//				matcher = wordPattern.matcher(name);
//				if (matcher.matches()) {
//					return true; 
//				}else{
//					return false; 
//				}
		}
		
	}
	
	private class ObjectEntityListPane extends JPanel{
		
		private JList jlistEntity; 
		private JScrollPane scrollListObjectEntity; 
		private List<ObjectEntity> listEntity; 
		private DefaultListModel listModel; 
		
		public ObjectEntityListPane(){
			
			super(new GridLayout(1,1)); 
			
			listEntity = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity(); 
			
			listModel = new DefaultListModel(); 
			for(Entity entity: listEntity){
				listModel.addElement(entity); 
			}
			jlistEntity = new JList(); 
			jlistEntity.setModel(listModel);
			scrollListObjectEntity = new JScrollPane(jlistEntity); 
			this.add(scrollListObjectEntity);
			
			jlistEntity.setCellRenderer(new ListCellRenderer(iconController.getObjectEntityIcon())); 
			
			jlistEntity.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	selected = (List<Object>)jlistEntity.getSelectedValuesList(); 
		                	isAdding = true; 
		                	for(Object selec: selected){
		                	    objectEntityInstancePane.updateReference(); 
				                showEntityInstanceListPane((ObjectEntity)selec);
				                
				                if(!((ObjectEntity)selec).isOrdereable()){
				                	objectEntityInstancePane.disableUpDownButtons(); 
				                }else{
				                	objectEntityInstancePane.enableUpDownButtons();
				                }
		                	}
		                }
		            }  	
			 );
		}
	}
	
	public class ObjectEntityInstanceListPane extends JPanel{
		
		private JList jlistEntity; 
		private JScrollPane scrollListObjectEntity; 
		private ObjectEntity objectEntity; 
		private DefaultListModel listModel;
		
		public ObjectEntityInstanceListPane(){
			
			super(new GridLayout(1,1)); 
			
			listModel = new DefaultListModel(); 
			
			jlistEntity = new JList(); 
			jlistEntity.setModel(listModel);
			scrollListObjectEntity = new JScrollPane(jlistEntity); 
			this.add(scrollListObjectEntity);
		}
		
		public ObjectEntityInstanceListPane(ObjectEntity objectEntity){
			
			super(new GridLayout(1,1)); 
			
			this.objectEntity = objectEntity; 
			listModel = new DefaultListModel(); 
			jlistEntity = new JList(); 
			
			if(objectEntity.isOrdereable()){
				ArrayList<ObjectEntityInstanceOrdereable> originalList = new ArrayList<ObjectEntityInstanceOrdereable>(); 
				for(ObjectEntityInstance instance: objectEntity.getInstanceList()){
					originalList.add((ObjectEntityInstanceOrdereable)instance);
				}
				
				for(ObjectEntityInstanceOrdereable instance: ObjectEntityInstanceOrdereable.ordererList(originalList)){
					listModel.addElement(instance); 
				}
				
				if(listModel.size() > 0)
				    last = (ObjectEntityInstanceOrdereable)listModel.get(listModel.size()-1);
				else last = null; 
			}else{
				for(ObjectEntityInstance instance: objectEntity.getInstanceList()){
					listModel.addElement(instance); 
				}	
				last = null;
			}
			
			jlistEntity.setModel(listModel);
			scrollListObjectEntity = new JScrollPane(jlistEntity); 
			this.add(scrollListObjectEntity);
			
			jlistEntity.setCellRenderer(new ListCellRenderer(iconController.getEntityInstanceIcon())); 
			
			jlistEntity.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	
		                	selected = jlistEntity.getSelectedValuesList(); 
		                	isAdding = false; 
		                	
		                	if(!selected.isEmpty()){
		                	   objectEntityInstancePane.updateReference(); 
		                	}
		                }
		            }  	
			 );
		}
		
		public void update(){
			
			listModel.clear(); 
			
			listModel = new DefaultListModel(); 
			
			if(objectEntity.isOrdereable()){
				ArrayList<ObjectEntityInstanceOrdereable> originalList = new ArrayList<ObjectEntityInstanceOrdereable>(); 
				for(ObjectEntityInstance instance: objectEntity.getInstanceList()){
					originalList.add((ObjectEntityInstanceOrdereable)instance);
				}
				
				for(ObjectEntityInstanceOrdereable instance: ObjectEntityInstanceOrdereable.ordererList(originalList)){
					listModel.addElement(instance); 
				}
				if(listModel.size() > 0)
				    last = (ObjectEntityInstanceOrdereable)listModel.get(listModel.size()-1);
				else last = null; 
				
			}else{
				for(ObjectEntityInstance instance: objectEntity.getInstanceList()){
					listModel.addElement(instance); 
				}
				last = null;
			}
			
			jlistEntity.setModel(listModel); 
		}
	}	
	
	public GridBagConstraints getConstraints( 
			int gridx, 
			int gridy, 
			int gridwidth, 
			int gridheight, 
			int weightx,
			int weighty, 
			int fill, 
			int anchor){
		
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		constraints.gridx = gridx; 
		constraints.gridy = gridy; 
		constraints.gridwidth = gridwidth; 
		constraints.gridheight = gridheight; 
		constraints.weightx = weightx;
		constraints.weighty = weighty; 
		constraints.fill = fill; 
		constraints.anchor = anchor; 
		
		return constraints; 
	
	}

	/**
	 * @return the objectEntityInstanceListPane
	 */
	public ObjectEntityInstanceListPane getObjectEntityInstanceListPane() {
		return objectEntityInstanceListPane;
	}

	/**
	 * @param objectEntityInstanceListPane the objectEntityInstanceListPane to set
	 */
	protected void setObjectEntityInstanceListPane(
			ObjectEntityInstanceListPane objectEntityInstanceListPane) {
		this.objectEntityInstanceListPane = objectEntityInstanceListPane;
	}
	
	
}
