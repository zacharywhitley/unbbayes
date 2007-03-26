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

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.entity.Entity;

public class PossibleValuesEditionPane extends JPanel{

	private DomainResidentNode residentNode; 
	private MEBNController mebnController; 
	
    private List<Entity> listPossibleValues; 
    
    JPanel jpInformation; 
    JPanel jpAddOptions; 
	
	JLabel name; 
	JTextField txtName; 
	
	JButton jbNew; 
	JButton jbDelete; 	
	
	JButton jbBooleanStates; 
	
	JToolBar jtbOptions; 	
    
    private JList jlPossibleValues; 
    private DefaultListModel listModel;
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    private final IconController iconController = IconController.getInstance();
    
    public PossibleValuesEditionPane(){
    	
    }
    	
    
	public PossibleValuesEditionPane(NetworkController _controller, DomainResidentNode _residentNode){
		
		super(); 
		setLayout(new BorderLayout()); 
		
		residentNode = _residentNode;
		mebnController = _controller.getMebnController(); 
		
		listPossibleValues = residentNode.getPossibleValueList(); 
		
		listModel = new DefaultListModel(); 
		for(Entity entity: listPossibleValues){
			listModel.addElement(entity.getName()); 
		}
		
	    jlPossibleValues = new JList(listModel); 
	    jlPossibleValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    jlPossibleValues.setSelectedIndex(0);
	    jlPossibleValues.setCellRenderer(new ListCellRenderer(iconController.getStateIcon())); 
	    
	    jlPossibleValues.setLayoutOrientation(JList.VERTICAL);
	    jlPossibleValues.setVisibleRowCount(-1);
	    JScrollPane listScrollPane = new JScrollPane(jlPossibleValues);
	    

	    
	    txtName = new JTextField(10);
	    
	    jtbOptions = new JToolBar(); 
	    jtbOptions.setLayout(new GridLayout(0, 2)); 
	    
	    jbNew = new JButton(iconController.getMoreIcon()); 
	    jbDelete = new JButton(iconController.getLessIcon()); 
	    jtbOptions.add(jbNew);
	    jtbOptions.add(jbDelete); 
	    jtbOptions.setFloatable(false);	    

	    //TODO fazer este painel... 
	    jbBooleanStates = new JButton("B"); 
	    
	    jpAddOptions = new JPanel(new BorderLayout()); 
	    jpAddOptions.add(jbBooleanStates, BorderLayout.LINE_END); 
	    jpAddOptions.add(txtName, BorderLayout.CENTER); 
	    
	    jpInformation = new JPanel(new GridLayout(2, 0)); 	    
	    jpInformation.add(jtbOptions); 
	    jpInformation.add(jpAddOptions);
	    jpAddOptions.setVisible(false); 
	    
        this.add("South", jpInformation); 
        this.add("Center", listScrollPane);

        addListeners(); 
        
	}
	
	public void update(){
		
		listModel.clear(); 
		
		listPossibleValues = residentNode.getPossibleValueList(); 
		
		listModel = new DefaultListModel(); 
		for(Entity entity: listPossibleValues){
			listModel.addElement(entity.getName()); 
		}
		
		jlPossibleValues.setModel(listModel); 
	}
	
	private void addListeners(){
		
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String nameValue = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(nameValue);
  						if (matcher.matches()) {
  							boolean teste = mebnController.existsPossibleValue(residentNode, nameValue); 
  							if(teste == false){
  							   mebnController.addPossibleValue(residentNode, nameValue); 
  							}
  							else{
  								JOptionPane.showMessageDialog(null, resource.getString("nameDuplicated"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  							}
  							
  							txtName.setText(""); 
  							
  						}  else {
  							JOptionPane.showMessageDialog(null, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  							txtName.selectAll();
  						}
  						
  						update(); 
  						
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  		});
        
		jbNew.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				jpAddOptions.setVisible(true); 
  				txtName.requestFocus(); 
  			}
  		});
		
		jbDelete.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  			    String nameValue = (String)jlPossibleValues.getSelectedValue(); 
				mebnController.removePossibleValue(residentNode, nameValue); 
  			    update(); 
  			
  			}
  		});
		
		jbBooleanStates.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				mebnController.addBooleanAsPossibleValue(residentNode);
  				update(); 
  				jpAddOptions.setVisible(false); 
  			}
  		});
	}
	
}
