package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;

public class OVariableEditionPane extends JPanel {

	OVariableTreeForOVariableEdition treeMFrag; 
	JScrollPane jspTreeMFrag; 
	
	JPanel jpInformation; 
	
	JLabel name; 
	JTextField txtName; 
	JButton btnChangeType; 
	JLabel type; 
	JTextField txtType; 
	
	JButton jbNew; 
	JButton jbDelete; 	
	
	JToolBar jtbOptions; 	
	
	MEBNController mebnController; 
	MFrag mFrag; 
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	public OVariableEditionPane(NetworkController _controller){
		
		super(); 
		
		setLayout(new BorderLayout()); 
		
		mebnController = _controller.getMebnController(); 
	    mFrag = mebnController.getCurrentMFrag(); 
	    
	    treeMFrag = new OVariableTreeForOVariableEdition(_controller);
	    jspTreeMFrag = new JScrollPane(treeMFrag);
	    
	    /* painel of information abaut the OVariable */
	    jpInformation = new JPanel(new GridLayout(5, 0)); 
	    
	    name = new JLabel("Name: "); 
	    txtName = new JTextField(10);
	    btnChangeType = new JButton("I"); 
	    type = new JLabel("Type: "); 
	    txtType = new JTextField(10); 
	    txtType.setEditable(false); 
	    
	    jtbOptions = new JToolBar(); 
	    jtbOptions.setLayout(new GridLayout(0, 2)); 
	    
	    //TODO usar resources
	    jbNew = new JButton("NEW"); 
	    jbDelete = new JButton("DEL"); 
	    jtbOptions.add(jbNew);
	    jtbOptions.add(jbDelete); 
	    jtbOptions.setFloatable(false);	    

	    jpInformation.add(jtbOptions); 
	    jpInformation.add(name); 
	    jpInformation.add(txtName); 
	    jpInformation.add(type); 
	    jpInformation.add(txtType); 
	    
	    addListeners(); 
	    
	    this.add("Center", jspTreeMFrag);	    
	    this.add("South", jpInformation);	    

	}

	/**
	 *  Create a empty painel 
	 *  */
	
	public OVariableEditionPane(){
		
	}
		
	public void update(){
	  	treeMFrag.updateTree(); 
	}	
	
	public void setNameOVariableSelected(String name){
		txtName.setText(name);; 
	}
	
	public String getNameOVariable(){
		return txtName.getText(); 
	}
	
	
	public void addListeners(){
		
		jbNew.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				
  				try{
  				   OrdinaryVariable ov = mebnController.addNewOrdinaryVariableInMFrag(); 
  				   treeMFrag.updateTree(); 
  				   treeMFrag.setOVariableActive(ov); 
  				}
  				catch(Exception e){
  					//TODO tratar excessão... 
  				}
  				
  			}
  		});
		
		jbDelete.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				
				if(treeMFrag.getOVariableActive() != null){
	  				mebnController.removeOrdinaryVariableOfMFrag(treeMFrag.getOVariableActive());						   
					treeMFrag.updateTree();
				}				
  				
  			}
  		});
		
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String name = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							if(treeMFrag.getOVariableActive() != null){
  							   treeMFrag.getOVariableActive().setName(name); 
  							   treeMFrag.updateTree();
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
		
		
	}
	
	
	
}
