package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ButtonLabel;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;

/**
 * Bar of edition for one ordinary variable. 
 * 
 * Structure: 
 * Icon of OV - Name of OV - Type of OV
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class OrdVariableToolBar extends JToolBar{

	private MEBNController mebnController; 
	
	private JLabel name; 
	private JTextField txtName; 
	private JLabel type; 
	private OrdinaryVariable ov; 
		
	private JComboBox jcbType; 
	private Type[] types; 	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	public OrdVariableToolBar(MEBNController _mebnController){
		
		mebnController = _mebnController; 
		this.setFloatable(false); 
  		ButtonLabel btnOrdVariableActive = new ButtonLabel(resource.getString("OrdVariableButton"), IconController.getInstance().getOVariableNodeIcon());  
  		
	    name = new JLabel(resource.getString("nameLabel")); 
	    txtName = new JTextField(10);
	    type = new JLabel(resource.getString("typeLabel")); 
	    
	    //Fill the combo box with the possible labels 
	    types = mebnController.getMultiEntityBayesianNetwork().getTypeContainer().getListOfTypes().toArray( new Type[0] ); 
        buildJComboBoxTypes(types); 
	    
        txtName.addFocusListener(new FocusListenerTextField()); 
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String name = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							if(ov != null){
  								
  								if(mebnController.getCurrentMFrag().getOrdinaryVariableByName(name)!= null){
  									JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
  	  	  									resource.getString("nameAlreadyExists"), 
  	  	  									resource.getString("nameException"), 
  	  	  									JOptionPane.ERROR_MESSAGE);
  	  							   	
  								}else{
  	  							   ov.setName(name); 
  							       ov.updateLabel();
  								}
  							}
  						}  else {
  							txtName.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
  							txtName.setForeground(Color.WHITE); 
  							txtName.selectAll();
  							JOptionPane.showMessageDialog(null, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
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
	    
	    add(btnOrdVariableActive); 
	    addSeparator();
	    add(name); 
	    add(txtName); 
	    addSeparator();
	    add(type); 
	    add(jcbType); 
	    
	}
	
	/**
	 * Set the ordinary variable that this tool bar use. 
	 * @param _ov
	 */
	public void setOrdVariable(OrdinaryVariable _ov){
		
		ov = _ov; 
		txtName.setText(ov.getName()); 
		
		//select the type of the OV in the combo box
		for(int i = 0; i < types.length; i++){
			if(types[i].equals(ov.getValueType())){
				jcbType.setSelectedIndex(i); 
				break; 
			}
		}
		
		repaint(); 
	}
	
	/**
	 * Update the types of the list of types
	 */
	
	public void updateListOfTypes(){
		remove(jcbType); 
		types = mebnController.getMultiEntityBayesianNetwork().getTypeContainer().getListOfTypes().toArray( new Type[0] );
		add(buildJComboBoxTypes(types)); 
		validate(); 
	}
	
	private JComboBox buildJComboBoxTypes(Type[] types){
	    jcbType = new JComboBox(types); 
	    jcbType.setSelectedIndex(0);
	    
	    jcbType.addActionListener(
           new ActionListener(){
        	   public void actionPerformed(ActionEvent e){
        		   
	    	        Type typeName = (Type)jcbType.getSelectedItem();
					if(ov != null){
						ov.setValueType(typeName); 
						ov.updateLabel(); 
					}
        	   }
           }
	    );   
	    
	    return jcbType; 
	}
	
}
