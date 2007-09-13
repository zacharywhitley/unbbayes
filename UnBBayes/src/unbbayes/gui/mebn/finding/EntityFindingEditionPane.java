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
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
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
	
	/* 
	 * indica se o usuario esta adicionando uma nova instancia (true) ou se esta
	 * apenas editando uma instancia já existente (false). 
	 */
	private boolean isAdding = true; 
	
	private ObjectEntityListPane objectEntityListPane; 
	private ObjectEntityInstancePane objectEntityInstancePane; 
	private ObjectEntityInstanceListPane objectEntityInstanceListPane; 
	
	
	private IconController iconController = IconController.getInstance(); 
  	private static ResourceBundle resource = 
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	public EntityFindingEditionPane(){
		super(); 
	}
	
	public EntityFindingEditionPane(MEBNController mebnController){
		super(new BorderLayout()); 
		
		this.mebnController = mebnController; 
		objectEntityListPane = new ObjectEntityListPane(); 
		objectEntityInstancePane = new ObjectEntityInstancePane(); 
		objectEntityInstanceListPane = new ObjectEntityInstanceListPane(); 
		
		add(objectEntityListPane, BorderLayout.NORTH);
		
		JPanel panelCentro = new JPanel(new GridLayout(1,1)); 
		panelCentro.add(objectEntityInstancePane); 
		add(panelCentro, BorderLayout.CENTER);
		
		add(objectEntityInstanceListPane, BorderLayout.SOUTH);
	}
	
	private class ObjectEntityInstancePane extends JPanel{
		
		private JTextField typeObjectEntity; 
		private JTextField nameObjectEntity; 
		
		//Button for add or edit instances
		private JButton btnAddInstance; 
		
		//Button for remove instances
		private JButton btnRemoveInstance; 
		
		private JToolBar barButtons; 
		
		private final JLabel labelType = new JLabel(resource.getString("typeLabel")); 
		private final JLabel labelName = new JLabel(resource.getString("nameLabel")); 
		
	    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
	    private Matcher matcher;	
		
		public ObjectEntityInstancePane(){

			super();
			
			GridBagLayout gridbag = new GridBagLayout(); 
			GridBagConstraints constraints = new GridBagConstraints(); 
			setLayout(gridbag); 
			
			typeObjectEntity = new JTextField(10); 
			typeObjectEntity.setEditable(false); 
			
			nameObjectEntity = new JTextField(10);
			nameObjectEntity.addFocusListener(new FocusListenerTextField());
			nameObjectEntity.addKeyListener(new KeyAdapter() {
	  			public void keyPressed(KeyEvent e) {
	  				
	  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) 
	  						&& (nameObjectEntity.getText().length()>0)) {
	  					try {
	  						String nameValue = nameObjectEntity.getText(0,nameObjectEntity.getText().length());
	  						matcher = wordPattern.matcher(nameValue);
	  						if (matcher.matches()) {
	  							addOrEditInstance(); 
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
			
			barButtons = new JToolBar();
			barButtons.setLayout(new GridLayout(1, 2)); 
			barButtons.add(btnAddInstance); 
			barButtons.add(btnRemoveInstance); 
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
		
		private void removeInstance(){
			if(selected != null){
				if(!isAdding){
					   mebnController.removeEntityInstance((ObjectEntityInstance)selected); 
					   objectEntityInstanceListPane.update();  
					}
				}
		}
		
		public void updateReference(){
			if(selected!=null){
				if(isAdding){					
					    typeObjectEntity.setText(((ObjectEntity)selected).getName());
					    nameObjectEntity.setText("");
					    btnAddInstance.setIcon(iconController.getMoreIcon()); 
					    btnRemoveInstance.setVisible(false); 
				}else{
					typeObjectEntity.setText(((ObjectEntityInstance)selected).getInstanceOf().getName());
					nameObjectEntity.setText(((ObjectEntityInstance)selected).getName());
				    btnAddInstance.setIcon(iconController.getEditIcon());
				    btnRemoveInstance.setVisible(true); 
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
		                	isAdding = true; 
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
	
	
}
