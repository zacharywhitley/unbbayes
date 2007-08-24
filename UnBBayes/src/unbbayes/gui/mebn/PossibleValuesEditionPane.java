package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.BooleanStatesEntity;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;

/**
 * Panel for selection of the possible values (states) of a
 * resident node. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class PossibleValuesEditionPane extends JPanel{

	private DomainResidentNode residentNode; 
	private MEBNController mebnController; 	
	
	private JToolBar jtbOptions;
	
	private JPanel panelStates; 
    private JScrollPane listScrollPane; 
	private CardLayout cardLayout; 
    
    private JPanel panelCategoryStates; 
    private JPanel panelBooleanStates; 
    private JPanel panelObjectStates;
    
    private final String PANEL_CATEGORY_STATES = "category"; 
    private final String PANEL_BOOLEAN_STATES = "boolean"; 
    private final String PANEL_OBJECT_STATES = "object"; 
	
    private List<Entity> statesList; 
    private JList statesJList; 
    private DefaultListModel statesListModel;
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    private final IconController iconController = IconController.getInstance();
    
    public PossibleValuesEditionPane(){
    	
    }
    
	public PossibleValuesEditionPane(MEBNController _controller, DomainResidentNode _residentNode){
		
		super(new BorderLayout()); 
		
		residentNode = _residentNode;
		mebnController = _controller; 
		
		buildPanelOptions(); 
		buildListScrollPane();
		buildPanelStates();  
		
		add(jtbOptions, BorderLayout.NORTH); 
		add(listScrollPane, BorderLayout.CENTER); 
		add(panelStates, BorderLayout.SOUTH); 
	}


	private void buildListScrollPane() {
		statesList = residentNode.getPossibleValueList(); 
		
		statesListModel = new DefaultListModel(); 
		for(Entity entity: statesList){
			statesListModel.addElement(entity.getName()); 
		}
		
		statesJList = new JList(statesListModel); 
		statesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statesJList.setSelectedIndex(0);
		statesJList.setCellRenderer(new StateCellRenderer()); 
		statesJList.setLayoutOrientation(JList.VERTICAL);
		statesJList.setVisibleRowCount(-1);
	    listScrollPane = new JScrollPane(statesJList);
	}

	private void buildPanelStates(){
	
		cardLayout = new CardLayout(); 
		panelStates = new JPanel(cardLayout); 
		
		panelCategoryStates = createPanelCategoryStates(); 
		panelObjectStates = createPanelObjectStates(); 
		panelBooleanStates = createPanelBooleanStates(); 
		
		panelStates.add(PANEL_CATEGORY_STATES, panelCategoryStates); 
		panelStates.add(PANEL_OBJECT_STATES, panelObjectStates); 
		panelStates.add(PANEL_BOOLEAN_STATES, panelBooleanStates); 
		
		switch(residentNode.getTypeOfStates()){
		case ResidentNode.OBJECT_ENTITY:
			cardLayout.show(panelStates, PANEL_OBJECT_STATES);
			break; 
		case ResidentNode.CATEGORY_RV_STATES:
			cardLayout.show(panelStates, PANEL_CATEGORY_STATES);
			break; 
		case ResidentNode.BOOLEAN_RV_STATES: 
			cardLayout.show(panelStates, PANEL_BOOLEAN_STATES);
			break; 
		}
		
	}


	private void buildPanelOptions() {
		
		JButton btnCategoryStates; 
		JButton btnObjectStates; 
		JButton btnBooleanStates; 
		
		btnCategoryStates = new JButton(iconController.getCategoryStateIcon()); 
		btnCategoryStates.setToolTipText(resource.getString("categoryStatesType")); 
		btnObjectStates = new JButton(iconController.getEntityStateIcon()); 
		btnObjectStates.setToolTipText(resource.getString("objectStatesType")); 
		btnBooleanStates = new JButton(iconController.getBooleanStateIcon()); 
		btnBooleanStates.setToolTipText(resource.getString("booleanStatesType")); 
		
		btnCategoryStates.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				cardLayout.show(panelStates, PANEL_CATEGORY_STATES); 
			}
			
		});
		
		btnBooleanStates.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				cardLayout.show(panelStates, PANEL_BOOLEAN_STATES);
			}
			
		});
		
		btnObjectStates.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				cardLayout.show(panelStates, PANEL_OBJECT_STATES);
			}
			
		});		
		
		jtbOptions = new JToolBar(); 
		jtbOptions.setLayout(new GridLayout(1, 3)); 
		jtbOptions.add(btnCategoryStates); 
		jtbOptions.add(btnObjectStates); 
		jtbOptions.add(btnBooleanStates);
		jtbOptions.setFloatable(false); 
	}
	
	private JPanel createPanelCategoryStates(){
		
		JPanel panel; 
	    JButton btnAdd; 
	    JButton btnRemove; 
	    
	    final JPanel jpAddOptions = new JPanel(new BorderLayout());
	    final JTextField txtName = new JTextField(10);
		
	    btnAdd = new JButton(iconController.getMoreIcon()); 
    	btnAdd.setToolTipText(resource.getString("addStateTip")); 
    	btnRemove = new JButton(iconController.getLessIcon()); 
    	btnRemove.setToolTipText(resource.getString("removeState")); 
    	
	    jpAddOptions.add(btnAdd, BorderLayout.LINE_START); 
	    jpAddOptions.add(txtName, BorderLayout.CENTER); 
	    jpAddOptions.add(btnRemove, BorderLayout.LINE_END); 
	    
	    panel = new JPanel(new BorderLayout()); 
	    panel.add(jpAddOptions, BorderLayout.CENTER); 
	   
	    txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String nameValue = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(nameValue);
  						if (matcher.matches()) {
  							boolean teste = mebnController.existsPossibleValue(residentNode, nameValue); 
  							if(teste == false){
  							   if(!(residentNode.getPossibleValueList().isEmpty())&&(residentNode.getTypeOfStates() != ResidentNode.CATEGORY_RV_STATES)){
  								 int answer = JOptionPane.showConfirmDialog(
  										mebnController.getMebnEditionPane(),
  										resource.getString("warningDeletStates"),
  										resource.getString("confirmation"),
  										JOptionPane.YES_NO_OPTION);
  								if(answer == JOptionPane.YES_OPTION){
  									mebnController.removeAllPossibleValues(residentNode); 
   								    mebnController.addPossibleValue(residentNode, nameValue);
   								    residentNode.setTypeOfStates(ResidentNode.CATEGORY_RV_STATES); 
  								}
  							   }else{
  								   mebnController.addPossibleValue(residentNode, nameValue); 	   
  							   }
  							
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
        
		btnAdd.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				try {
						String nameValue = txtName.getText(0,txtName.getText().length());
						matcher = wordPattern.matcher(nameValue);
						if (matcher.matches()) {
							boolean teste = mebnController.existsPossibleValue(residentNode, nameValue); 
							if(teste == false){
							   if(!(residentNode.getPossibleValueList().isEmpty())&&(residentNode.getTypeOfStates() != ResidentNode.CATEGORY_RV_STATES)){
								 int answer = JOptionPane.showConfirmDialog(
										mebnController.getMebnEditionPane(),
										resource.getString("warningDeletStates"),
										resource.getString("confirmation"),
										JOptionPane.YES_NO_OPTION);
								if(answer == JOptionPane.YES_OPTION){
									mebnController.removeAllPossibleValues(residentNode); 
								    mebnController.addPossibleValue(residentNode, nameValue);
								    residentNode.setTypeOfStates(ResidentNode.CATEGORY_RV_STATES); 
								}
							   }else{
								   mebnController.addPossibleValue(residentNode, nameValue); 	   
							   }
							
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
  		});
		
		btnRemove.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if (statesJList.getSelectedValue()!= null){
					mebnController.removePossibleValue(residentNode, statesJList.getSelectedValue().toString()); 
				}else{
					String nameValue;
					try {
						nameValue = txtName.getText(0,txtName.getText().length());
						
						matcher = wordPattern.matcher(nameValue);
						if (matcher.matches()) {
							boolean teste = mebnController.existsPossibleValue(residentNode, nameValue); 
							if(teste == true){
								mebnController.removePossibleValue(residentNode, nameValue); 
							}
						}
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				update();
			}
			
		}); 
	
        return panel; 
	}
	
	private JPanel createPanelObjectStates(){
		
		JPanel panel; 
		final JComboBox comboEntities = new JComboBox(mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity().toArray());; 
    	JButton btnAdd = new JButton(iconController.getMoreIcon()); 
    	btnAdd.setToolTipText(resource.getString("addStateTip")); 
    	
		btnAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(comboEntities.getSelectedItem() != null){
					if(!(residentNode.getPossibleValueList().isEmpty())){
						int answer = JOptionPane.showConfirmDialog(
								mebnController.getMebnEditionPane(),
								resource.getString("warningDeletStates"),
								resource.getString("confirmation"),
								JOptionPane.YES_NO_OPTION);
						if(answer == JOptionPane.YES_OPTION){
							   residentNode.removeAllPossibleValues(); 
							   residentNode.addPossibleValue((Entity)(comboEntities.getSelectedItem()));	
							   residentNode.setTypeOfStates(ResidentNode.OBJECT_ENTITY); 
							   update();
						}	
					}else{
						residentNode.addPossibleValue((Entity)(comboEntities.getSelectedItem()));
						residentNode.setTypeOfStates(ResidentNode.OBJECT_ENTITY); 
						update(); 
					}
					
				} 
			}
			
		}); 
		
		panel = new JPanel(new BorderLayout()); 
		panel.add(btnAdd, BorderLayout.LINE_START); 
		panel.add(comboEntities, BorderLayout.CENTER); 
		
		return panel; 
	}
	
    private JPanel createPanelBooleanStates(){
		
    	JButton btnAdd = new JButton(iconController.getMoreIcon()); 
    	btnAdd.setToolTipText(resource.getString("addStateTip")); 
    	
 	    JLabel labelNotEditable = new JLabel(" " + resource.getString("insertBooleanStates")); 
    	
 	    btnAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(!(residentNode.getPossibleValueList().isEmpty())){
					int answer = JOptionPane.showConfirmDialog(
							mebnController.getMebnEditionPane(),
							resource.getString("warningDeletStates"),
							resource.getString("confirmation"),
							JOptionPane.YES_NO_OPTION);
					if(answer == JOptionPane.YES_OPTION){
						mebnController.removeAllPossibleValues(residentNode); 
						mebnController.addBooleanAsPossibleValue(residentNode); 
						residentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
						update(); 
					}
				}else{
					mebnController.addBooleanAsPossibleValue(residentNode); 
					residentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES);
					update(); 
				}
				
			}
 	    	
 	    }); 
 	    
 	    JPanel panel = new JPanel(new BorderLayout());
 	    panel.add(btnAdd, BorderLayout.LINE_START); 
 	    panel.add(labelNotEditable, BorderLayout.CENTER); 
 	    
 	    
		return panel; 
	}
	
	/**
	 * Update the list of category states. 
	 */
	public void update(){
		
		statesListModel.clear(); 
		
		statesList = residentNode.getPossibleValueList(); 
		
		statesListModel = new DefaultListModel(); 
		for(Entity entity: statesList){
			statesListModel.addElement(entity.getName()); 
		}
		
		statesJList.setModel(statesListModel); 
	}
	
	
	public class StateCellRenderer extends DefaultListCellRenderer{
		
		private ImageIcon iconObjectState = iconController.getObjectEntityIcon();
		private ImageIcon iconCategoryState = iconController.getStateIcon(); 
		private ImageIcon iconBooleanState = iconController.getBooleanIcon(); 
		
		public StateCellRenderer(){
			super(); 
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, 
				                                      boolean isSelected, boolean cellHasFocus){
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
			
			switch(residentNode.getTypeOfStates()){
			case ResidentNode.OBJECT_ENTITY:
				setIcon(iconObjectState); 
				break; 
			case ResidentNode.CATEGORY_RV_STATES:
				setIcon(iconCategoryState); 
				break; 
			case ResidentNode.BOOLEAN_RV_STATES: 
				setIcon(iconBooleanState); 
				break; 
			}
			
			if(isSelected){
			   super.setBorder(BorderFactory.createEtchedBorder()); 
			}
			
			return this; 
		}
		
}

}
