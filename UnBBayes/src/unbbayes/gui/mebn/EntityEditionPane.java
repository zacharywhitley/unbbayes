package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.exception.TypeException;

/**
 * Pane for edition of object entities : Create, Delete, Edit and View
 */

public class EntityEditionPane extends JPanel{

	private MEBNController mebnController; 
	
    private List<ObjectEntity> listEntity; 
    
    private JPanel jpInformation; 
	
    private JLabel name; 
    private JTextField txtName; 
    private JLabel type; 
    private JTextField txtType; 
	
    private JToolBar jtbOptions; 	
    
    private JButton jbNew; 
    private JButton jbDelete; 	
    
    private JList jlEntities; 
    private DefaultListModel listModel;
    private ObjectEntity selected; 
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
    private ObjectEntity entity; 
    
    private final IconController iconController = IconController.getInstance();
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = 
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
    
    /**
     * 
     * @param mebnController Controller for objects of this pane
     */
	public EntityEditionPane(MEBNController mebnController){
		
		super(); 

		this.mebnController = mebnController; 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(
				resource.getString("EntityTitle"))); 
        
		setLayout(new BorderLayout()); 
		
		buildJlEntities();
		JScrollPane listScrollPane = new JScrollPane(jlEntities);
	    buildJpInformation(); 

        this.add(BorderLayout.SOUTH, jpInformation); 
        this.add(BorderLayout.CENTER, listScrollPane);
        
	    selected = null; 
	    update(); 
	    addListListener(); 
        addButtonsListeners(); 
 
	}

	private void buildJlEntities() {
		listModel = new DefaultListModel(); 
		
	    jlEntities = new JList(listModel); 
	    jlEntities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    jlEntities.setLayoutOrientation(JList.VERTICAL);
	    jlEntities.setVisibleRowCount(-1);
	    jlEntities.setCellRenderer(new ListCellRenderer(iconController.getObjectEntityIcon())); 
	    
	}

	private void buildJpInformation() {
		jpInformation = new JPanel(new GridLayout(5, 0)); 
	    
	    name = new JLabel(resource.getString("nameLabel")); 
	    txtName = new JTextField(10);
	    txtName.setEditable(false); 
	    
	    type = new JLabel(resource.getString("typeLabel")); 
	    txtType = new JTextField(10);
	    txtType.setEditable(false); 
	    
	    jtbOptions = new JToolBar(); 
	    jtbOptions.setLayout(new GridLayout(0, 2)); 
	    
	    jbNew = new JButton(iconController.getMoreIcon()); 
	    jbDelete = new JButton(iconController.getLessIcon());
	    
	    jbNew.setToolTipText(resource.getString("newEntityToolTip")); 
	    jbDelete.setToolTipText(resource.getString("delEntityToolTip")); 
	    
	    jtbOptions.add(jbNew);
	    jtbOptions.add(jbDelete); 
	    jtbOptions.setFloatable(false);	    
	    
	    jpInformation.add(jtbOptions); 
	    jpInformation.add(name); 
	    jpInformation.add(txtName);
	    jpInformation.add(type); 
	    jpInformation.add(txtType);
	}
	
	/**
	 *  update the list of entities 
	 **/
	
	private void update(){
		
		ObjectEntity antSelected = selected; 
		
		listModel.clear(); 
		
		listEntity = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity(); 
		
		listModel = new DefaultListModel(); 
		for(Entity entity: listEntity){
			listModel.addElement(entity); 
		}
		
		jlEntities.setModel(listModel); 
		
		/* 
		 * Warning: Por algum motivo estranho a mim a referencia feita por
		 * selected estava sendo perdida quando se adicionava seguidamente
		 * nos entidades... Esta jogadinha solucionou o problema... 
		 */
		selected = antSelected; 
		
	}
	
	private void addListListener(){

	    jlEntities.addListSelectionListener(
            new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                	
                	selected = (ObjectEntity)jlEntities.getSelectedValue(); 
                	if(selected != null){
                	   txtName.setText(selected.getName()); 
                	   txtName.setEditable(true); 
                       txtType.setText(selected.getType().getName());
                	}
                }
            }  	
	    );
	   
	}
	
	private void addButtonsListeners(){
		
		txtName.addFocusListener(new FocusListenerTextField()); 
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) 
  						&& (txtName.getText().length()>0)) {
  					try {
  						String nameValue = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(nameValue);
  						if (matcher.matches()) {
  							try{
  							   mebnController.renameObjectEntity(selected, nameValue);
  							   jlEntities.setSelectedValue(selected, true); 
  		  					   txtName.setText(selected.getName()); 
  		  					   txtName.setEditable(false); 
  		  					   txtType.setText(selected.getType().getName());
  		  					   update();
  							}
  							catch (TypeException typeException){
  								JOptionPane.showMessageDialog(null, 
  										resource.getString("nameDuplicated"), 
  										resource.getString("nameException"), 
  										JOptionPane.ERROR_MESSAGE);
  	  							txtName.selectAll();
  							}
  						}  else {
  							txtName.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
  							txtName.setForeground(Color.WHITE); 
  							JOptionPane.showMessageDialog(null, 
  									resource.getString("nameError"), 
  									resource.getString("nameException"), 
  									JOptionPane.ERROR_MESSAGE);
  							txtName.selectAll();
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  			
  			public void keyReleased(KeyEvent e){
  				try{
                    String name = txtName.getText(0,txtName.getText().length());
						matcher = wordPattern.matcher(name);
						if (!matcher.matches()) {
							txtName.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
							txtName.setForeground(Color.WHITE); 
						}
						else{
							txtName.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected());
							txtName.setForeground(Color.BLACK); 
						}
  				}
  				catch(Exception efd){
  					
  				}
  				
  			}
  		});
        
		jbNew.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				try{
				   
  				   selected = mebnController.createObjectEntity();
				   
				   update();  
				   
				   jlEntities.setSelectedValue(selected, true); 
				   txtType.setText(selected.getType().getName()); 
				   txtName.setEditable(true); 
				   txtName.setText(selected.getName());
				   txtName.selectAll(); 
				   txtName.requestFocus(); 
  				}
  				catch(TypeException e){
  					JOptionPane.showMessageDialog(null, 
  							resource.getString("nameDuplicated"), 
  							resource.getString("nameException"), 
  							JOptionPane.ERROR_MESSAGE);
  				}
  			}
  		});
		
		jbDelete.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  			    if(selected != null){
  			    	try{
  			    	   mebnController.removeObjectEntity(selected);
  			    	}
  			    	catch(Exception e){
  			    		e.printStackTrace(); 
  			    	}
  			    	update(); 
  			    	txtName.setText(" "); 
  			    	txtType.setText(" "); 
  			    	txtName.setEditable(false); 
  			    }
  			}
  		});
	}

}
	
	

