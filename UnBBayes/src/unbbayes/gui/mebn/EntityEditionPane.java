package unbbayes.gui.mebn;

import java.awt.BorderLayout;
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
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.exception.TypeException;


/**
 * Pane for edition of entities (Create, Edit, View)
 */

public class EntityEditionPane extends JPanel{

	private MEBNController mebnController; 
	
    private List<ObjectEntity> listEntity; 
    
    JPanel jpInformation; 
	
	JLabel name; 
	JTextField txtName; 
	JLabel type; 
	JTextField txtType; 
	
	JToolBar jtbOptions; 	
    
	JButton jbNew; 
	JButton jbDelete; 	
    
    private JList jlEntities; 
    private DefaultListModel listModel;
    private ObjectEntity selected; 
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
    private ObjectEntity entity; 
    
    private final IconController iconController = IconController.getInstance();
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
    
    
	public EntityEditionPane(NetworkController _controller){
		
		super(); 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("EntityTitle"))); 
        
		setLayout(new BorderLayout()); 
		
		mebnController = _controller.getMebnController(); 
		
		listModel = new DefaultListModel(); 
		
	    jlEntities = new JList(listModel); 
	    jlEntities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    jlEntities.setLayoutOrientation(JList.VERTICAL);
	    jlEntities.setVisibleRowCount(-1);
	    
	    selected = null; 
	    update(); 
	    addListListener(); 
	    
	    JScrollPane listScrollPane = new JScrollPane(jlEntities);
	    
	    /* panel of information about the Entity */
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
	    
        this.add("South", jpInformation); 
        this.add("Center", listScrollPane);

        addButtonsListeners(); 
 
	}
	
	/**
	 *  update the list of entities 
	 **/
	
	public void update(){
		
		listModel.clear(); 
		
		listEntity = ObjectEntity.getListEntity(); 
		
		listModel = new DefaultListModel(); 
		for(Entity entity: listEntity){
			listModel.addElement(entity); 
		}
		
		jlEntities.setModel(listModel); 
	}
	
	private void addListListener(){

	    jlEntities.addListSelectionListener(
            new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                	
                	selected = (ObjectEntity)jlEntities.getSelectedValue(); 
                	if(selected != null){
                	   txtName.setText(selected.getName()); 
                	   txtName.setEditable(true); 
                       txtType.setText(selected.getType());
                	}
                }
            }  	
	    );
	   
	}
	
	private void addButtonsListeners(){
		
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String nameValue = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(nameValue);
  						if (matcher.matches()) {
  							try{
  							   mebnController.renameObjectEntity(selected, nameValue);
  							   jlEntities.setSelectedValue(selected, true); 
  		  					   txtName.setText(selected.getName()); 
  		  					   txtName.setEditable(false); 
  		  					   txtType.setText(selected.getType());
  		  					   update();
  							}
  							catch (TypeException typeException){
  								JOptionPane.showMessageDialog(null, resource.getString("nameDuplicated"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  	  							txtName.selectAll();
  							}
  						}  else {
  							JOptionPane.showMessageDialog(null, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  							txtName.selectAll();
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  		});
        
		jbNew.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				try{
				   selected = mebnController.addObjectEntity();
				   update();  
				   jlEntities.setSelectedValue(selected, true); 
				   txtType.setText(selected.getType()); 
				   
				   txtName.setEditable(true); 
				   txtName.setText(selected.getName());
				   txtName.selectAll(); 
				   txtName.requestFocus(); 
  				}
  				catch(TypeException e){
  					JOptionPane.showMessageDialog(null, resource.getString("nameDuplicated"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  				}
  			}
  		});
		
		jbDelete.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  			    if(selected != null){
  			    	mebnController.removeObjectEntity(selected); 
  			    	update(); 
  			    	txtName.setText(" "); 
  			    	txtType.setText(" "); 
  			    	txtName.setEditable(false); 
  			    }
  			}
  		});
	}

}
	
	

