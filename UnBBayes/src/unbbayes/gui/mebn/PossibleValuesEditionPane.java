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

import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.entity.Entity;

public class PossibleValuesEditionPane extends JPanel{

	private DomainResidentNode residentNode; 
	private MEBNController mebnController; 
	
    private List<Entity> listPossibleValues; 
    
    JPanel jpInformation; 
	
	JLabel name; 
	JTextField txtName; 
	
	JButton jbNew; 
	JButton jbDelete; 	
	
	JToolBar jtbOptions; 	
    
    
    private JList jlPossibleValues; 
    private DefaultListModel listModel;
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    
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
		
		/* a jogadinha abaixo foi feita para setar o tamanho do painel,
		 * pois caso não haja nenhum elemento ele aumentara o frame
		 * no qual este painel esta sendo inserido
		 */
		//TODO fazer isto de uma forma decente... 
		if(listPossibleValues.size() == 0){
		    listModel.addElement(""); 
		}
		
	    jlPossibleValues = new JList(listModel); 
	    jlPossibleValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    jlPossibleValues.setSelectedIndex(0);
	    //jlPossibleValues.addListSelectionListener(this);
	    jlPossibleValues.setLayoutOrientation(JList.VERTICAL);
	    jlPossibleValues.setVisibleRowCount(-1);
	    JScrollPane listScrollPane = new JScrollPane(jlPossibleValues);
	    
	    /* painel of information abaut the OVariable */
	    jpInformation = new JPanel(new GridLayout(3, 0)); 
	    
	    name = new JLabel("Name: "); 
	    txtName = new JTextField(10);
	    
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
	    name.setVisible(false); 
	    txtName.setVisible(false); 
	    
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
  							mebnController.addPossibleValue(residentNode, nameValue); 
  							//name.setVisible(false); 
  						    txtName.setText(""); 
  							//txtName.setVisible(false); 
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
  				name.setVisible(true); 
  				txtName.setVisible(true); 
  			}
  		});
		
		jbDelete.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				int selected = jlPossibleValues.getSelectedIndex(); 
  			    if(selected != -1){
  			    	
  			    }
  			}
  		});
	}

}
