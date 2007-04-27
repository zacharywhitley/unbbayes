package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;

public class OrdVariableBar extends JToolBar{

	private JLabel name; 
	private JTextField txtName; 
	private JLabel type; 
	private OrdinaryVariable ov; 
		
	private JComboBox jcbType; 
	private String[] types; 	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	public OrdVariableBar(){
		
  		JButton btnOrdVariableActive = new JButton(resource.getString("OrdVariableButton"));  
  		btnOrdVariableActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnOrdVariableActive.setForeground(Color.WHITE); 
		
	    name = new JLabel(resource.getString("nameLabel")); 
	    txtName = new JTextField(10);
	    type = new JLabel(resource.getString("typeLabel")); 
	    
	    //Fill the combo box with the possible labels 
	    types = Type.getListOfTypes().toArray( new String[0] ); 
	    jcbType = new JComboBox(types); 
	    jcbType.setSelectedIndex(0);
	    
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String name = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							if(ov != null){
  							   ov.setName(name); 
  							   ov.updateLabel(); 
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
		
	    jcbType.addActionListener(
           new ActionListener(){
        	   public void actionPerformed(ActionEvent e){
        		   
	    	        String typeName = (String)jcbType.getSelectedItem();
					System.out.println("item selected = " + typeName); 
	    	        
					if(ov != null){
						ov.setValueType(typeName); 
						ov.updateLabel(); 
					}
        	   }
           }
	    );     
	    
	    add(btnOrdVariableActive); 
	    addSeparator();
	    add(name); 
	    add(txtName); 
	    addSeparator();
	    add(type); 
	    add(jcbType); 
	    
	}
	
	public void setOrdVariable(OrdinaryVariable _ov){
		ov = _ov; 
		txtName.setText(ov.getName()); 
		repaint(); 
	}
	
}
