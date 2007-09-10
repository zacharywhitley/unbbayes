package unbbayes.gui.mebn.finding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;

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
 * @version 1.0 (09/07/07)
 *
 */
public class EntityFindingEditionPane extends JPanel{

	private MEBNController mebnController; 
	
	private Object selected; 
	private boolean isAdding = true; 
	
	private ObjectEntityListPane objectEntityListPane; 
	private ObjectEntityInstancePane objectEntityInstancePane; 
	private ObjectEntityInstanceListPane objectEntityInstanceListPane; 
	
	
	private IconController iconController = IconController.getInstance(); 
  	private static ResourceBundle resource = 
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	public EntityFindingEditionPane(){
		
	}
	
	public EntityFindingEditionPane(MEBNController mebnController){
		super(new BorderLayout()); 
		
		this.mebnController = mebnController; 
		objectEntityListPane = new ObjectEntityListPane(); 
		objectEntityInstancePane = new ObjectEntityInstancePane(); 
		objectEntityInstanceListPane = new ObjectEntityInstanceListPane(); 
		
		add(objectEntityListPane, BorderLayout.NORTH); 
		add(objectEntityInstancePane, BorderLayout.CENTER);
		add(objectEntityInstanceListPane, BorderLayout.SOUTH);
	}
	
	private class ObjectEntityInstancePane extends JPanel{
		
		private JTextField typeObjectEntity; 
		private JTextField nameObjectEntity; 
		private JButton addInstance; 
		
	    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
	    private Matcher matcher;	
		
		public ObjectEntityInstancePane(){

			super(new GridLayout(3,1));
			
			typeObjectEntity = new JTextField(); 
			typeObjectEntity.setEditable(false); 
			
			nameObjectEntity = new JTextField();
			nameObjectEntity.addFocusListener(new FocusListenerTextField());
			nameObjectEntity.addKeyListener(new KeyAdapter() {
	  			public void keyPressed(KeyEvent e) {
	  				
	  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) 
	  						&& (nameObjectEntity.getText().length()>0)) {
	  					try {
	  						String nameValue = nameObjectEntity.getText(0,nameObjectEntity.getText().length());
	  						matcher = wordPattern.matcher(nameValue);
	  						if (matcher.matches()) {
	  							processAction(); 
	  						}  else {
	  							nameObjectEntity.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
	  							nameObjectEntity.setForeground(Color.WHITE); 
	  							JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
	  									resource.getString("nameError"), 
	  									resource.getString("nameException"), 
	  									JOptionPane.ERROR_MESSAGE);
	  							nameObjectEntity.selectAll();
	  						}
	  					}
	  					catch (javax.swing.text.BadLocationException ble) {
	  						System.out.println(ble.getMessage());
	  					}
	  				}
	  			}
	  			
	  			public void keyReleased(KeyEvent e){
	  				try{
	                    String name = nameObjectEntity.getText(0,nameObjectEntity.getText().length());
							matcher = wordPattern.matcher(name);
							if (!matcher.matches()) {
								nameObjectEntity.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
								nameObjectEntity.setForeground(Color.WHITE); 
							}
							else{
								nameObjectEntity.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected());
								nameObjectEntity.setForeground(Color.BLACK); 
							}
	  				}
	  				catch(Exception efd){
	  					
	  				}
	  				
	  			}
	  		});
			
			addInstance = new JButton(iconController.getMoreIcon()); 
			addInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					processAction(); 
				}
				
			}); 
			
			add(typeObjectEntity); 
			add(nameObjectEntity); 
			add(addInstance); 
		}
		
		private void processAction(){
			if((selected != null)&&(testName(nameObjectEntity.getText()))){
				if(isAdding){
					try{
						mebnController.createEntityIntance((ObjectEntity)selected, nameObjectEntity.getText()); 
						objectEntityInstanceListPane.update();  
						nameObjectEntity.setText(""); 
					}
					catch(EntityInstanceAlreadyExistsException ex){
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
								resource.getString("nameDuplicated"), 
								resource.getString("nameException"), 
								JOptionPane.ERROR_MESSAGE);
					}
				}else{
					try{
						mebnController.renameEntityIntance((ObjectEntityInstance)selected, nameObjectEntity.getText()); 
						objectEntityInstanceListPane.update();
					}
					catch(EntityInstanceAlreadyExistsException ex){
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
								resource.getString("nameDuplicated"), 
								resource.getString("nameException"), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
		public void updateReference(){
			if(selected!=null){
				if((selected instanceof ObjectEntity)&&(isAdding)){
					typeObjectEntity.setText(((ObjectEntity)selected).getName());
					nameObjectEntity.setText(""); 
				}else{
					typeObjectEntity.setText(((ObjectEntityInstance)selected).getInstanceOf().getName());
					nameObjectEntity.setText(((ObjectEntityInstance)selected).getName());
				}
			}
		}
		
		public boolean testName(String name){
				matcher = wordPattern.matcher(name);
				if (matcher.matches()) {
					return true; 
				}else{
					return false; 
				}
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
		                	
		                	selected = (ObjectEntity)jlistEntity.getSelectedValue(); 
		                	if(selected != null){
		                	   objectEntityInstancePane.updateReference(); 
		                	}
		                }
		            }  	
			 );
		}
	}
	
	private class ObjectEntityInstanceListPane extends JPanel{
		
		private JList jlistEntity; 
		private JScrollPane scrollListObjectEntity; 
		private List<ObjectEntity> listEntity; 
		private DefaultListModel listModel; 
		
		public ObjectEntityInstanceListPane(){
			
			super(new GridLayout(1,1)); 
			
			listEntity = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity(); 
			
			listModel = new DefaultListModel(); 
			
			for(ObjectEntity entity: listEntity){
				for(ObjectEntityInstance instance: entity.getInstanceList()){
				   listModel.addElement(instance); 
				}
			}
			
			jlistEntity = new JList(); 
			jlistEntity.setModel(listModel);
			scrollListObjectEntity = new JScrollPane(jlistEntity); 
			this.add(scrollListObjectEntity);
			
			jlistEntity.setCellRenderer(new ListCellRenderer(iconController.getEntityInstanceIcon())); 
			
			jlistEntity.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	
		                	selected = jlistEntity.getSelectedValue(); 
		                	isAdding = false; 
		                	
		                	if(selected != null){
		                	   objectEntityInstancePane.updateReference(); 
		                	}
		                }
		            }  	
			 );
		}
		
		public void update(){
			
			listModel.clear(); 
			
			listModel = new DefaultListModel(); 
			
			for(ObjectEntity entity: listEntity){
				for(ObjectEntityInstance instance: entity.getInstanceList()){
				   listModel.addElement(instance); 
				}
			}
			
			jlistEntity.setModel(listModel); 
		}
	}	
	
}
