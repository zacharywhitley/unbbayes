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
import javax.swing.JCheckBox;
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
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;

/**
 * Panel for selection of the possible values (states) of a
 * resident node. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class PossibleValuesEditionPane extends JPanel{

	private DomainResidentNode residentNode; 
	private MEBNController mebnController; 	
	
	private JPanel jtbOptions;
	private StatesPanel panelStates; 
    private ListStatesPanel listStatesPanel; 
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
    
    /**
     * Create a empty panel. 
     */
    public PossibleValuesEditionPane(){
    	super(); 
    }
    
    /**
     * Create the panel for edition of possible values of the resident node. 
     * @param _controller
     * @param _residentNode
     */
	public PossibleValuesEditionPane(MEBNController _controller, DomainResidentNode _residentNode){
		
		super(new BorderLayout()); 
		
		residentNode = _residentNode;
		mebnController = _controller; 
		
		buildListStates(); 
		jtbOptions = new OptionsPanel(); 
		listStatesPanel = new ListStatesPanel();
		panelStates = new StatesPanel();  
		
		add(jtbOptions, BorderLayout.NORTH); 
		add(listStatesPanel, BorderLayout.CENTER); 
		add(panelStates, BorderLayout.SOUTH); 
	}
	
	public void buildListStates(){
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
	}
	
	/**
	 * ScroolPane contendo a lista de estados. 
	 */
	private class ListStatesPanel extends JScrollPane{
		
		public ListStatesPanel(){
			super(statesJList); 
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
		
	}
	
	private class StatesPanel extends JPanel{
		
		private int selectedPanel; 
		
		public StatesPanel(){
			super();
			cardLayout = new CardLayout(); 
			this.setLayout(cardLayout); 
			
			panelCategoryStates = new CategoryStatesPanel(); 
			panelObjectStates = new ObjectStatesPanel(); 
			panelBooleanStates = new BooleanStatesPane(); 
			
			panelStates.add(PANEL_CATEGORY_STATES, panelCategoryStates); 
			panelStates.add(PANEL_OBJECT_STATES, panelObjectStates); 
			panelStates.add(PANEL_BOOLEAN_STATES, panelBooleanStates); 
			
			switch(residentNode.getTypeOfStates()){
			case ResidentNode.OBJECT_ENTITY:
				cardLayout.show(panelStates, PANEL_OBJECT_STATES);
				selectedPanel = ResidentNode.OBJECT_ENTITY; 
				break; 
			case ResidentNode.CATEGORY_RV_STATES:
				cardLayout.show(panelStates, PANEL_CATEGORY_STATES);
				selectedPanel = ResidentNode.CATEGORY_RV_STATES; 
				break; 
			case ResidentNode.BOOLEAN_RV_STATES: 
				cardLayout.show(panelStates, PANEL_BOOLEAN_STATES);
				selectedPanel = ResidentNode.BOOLEAN_RV_STATES; 
				break; 
			}

		}

		public int getSelectedPanel() {
			return selectedPanel;
		}

		public void setSelectedPanel(int selectedPanel) {
			this.selectedPanel = selectedPanel;
		}
		
	}


	/**
	 * Painel para a edição de estados do tipo categórico. 
	 */
	
	private class CategoryStatesPanel extends JPanel{
		
	    private JButton btnAdd; 
	    private JButton btnRemove; 
	    private JCheckBox checkGloballyExclusive; 
	    
	    private final JTextField txtName = new JTextField(10);
	    
		public CategoryStatesPanel(){
			
			
			/*------------------------- Build Panel ---------------------------*/
			
			super(new GridLayout(3,1)); 
		    btnAdd = new JButton(iconController.getMoreIcon()); 
	    	btnAdd.setToolTipText(resource.getString("addStateTip")); 
	    	btnRemove = new JButton(iconController.getLessIcon()); 
	    	btnRemove.setToolTipText(resource.getString("removeState")); 
	    	
		    JToolBar barOptions = new JToolBar(); 
		    barOptions.setLayout(new GridLayout(1,2));
		    barOptions.setFloatable(false);
		    barOptions.add(btnAdd); 
		    barOptions.add(btnRemove); 
		    
		    JToolBar barName = new JToolBar(); 
		    barName.setFloatable(false); 
		    JLabel labelName = new JLabel(resource.getString("nameLabel") + " "); 
		    barName.add(labelName); 
		    barName.add(txtName); 
		    
		    JToolBar toolGloballyExclusive = new JToolBar();
		    toolGloballyExclusive.setFloatable(false);
		    JLabel labelExclusive = new JLabel(resource.getString("isGloballyExclusive")); 
		    checkGloballyExclusive = new JCheckBox(); 
		    checkGloballyExclusive.setSelected(false); 
		    toolGloballyExclusive.add(checkGloballyExclusive); 
		    toolGloballyExclusive.add(labelExclusive); 
		    
		    add(barOptions); 
		    add(barName); 
		    add(toolGloballyExclusive); 
		    
		    
		    
		    
		    /*--------------------------- Add listeners ----------------------*/
		    
		    txtName.addKeyListener(new KeyAdapter() {
	  			public void keyPressed(KeyEvent e) {
	  				
	  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length() > 0)) {
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
	  						
	  						listStatesPanel.update(); 
	  						
	  					}
	  					catch (javax.swing.text.BadLocationException ble) {
	  						System.out.println(ble.getMessage());
	  					}
	  				}
	  			}
	  		});
	        
		    btnAdd.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent ae) {
		    		
		    		if (txtName.getText().length() > 0) {
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
		    				
		    				listStatesPanel.update();
		    				
		    			}
		    			catch (javax.swing.text.BadLocationException ble) {
		    				System.out.println(ble.getMessage());
		    			}
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
					listStatesPanel.update();
				}
				
			}); 
		}
		
	}
	
	private class ObjectStatesPanel extends JPanel{
		
		final JComboBox comboEntities = new JComboBox(mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity().toArray());; 
    	JButton btnAdd = new JButton(iconController.getMoreIcon()); 
    	
		public ObjectStatesPanel(){
			super(new GridLayout(3,1)); 
			
	    	btnAdd.setToolTipText(resource.getString("addStateTip")); 
			
			JToolBar barEdition = new JToolBar(); 
			barEdition.setFloatable(false); 
			barEdition.add(btnAdd); 
			barEdition.add(comboEntities); 
	    	
	    	add(barEdition); 
	    	add(new JLabel()); 
	    	add(new JLabel()); 
	    	
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
								   listStatesPanel.update();
							}	
						}else{
							residentNode.addPossibleValue((Entity)(comboEntities.getSelectedItem()));
							residentNode.setTypeOfStates(ResidentNode.OBJECT_ENTITY); 
							listStatesPanel.update();
						}
						
					} 
				}
				
			}); 
			
		}
		
	}
	
	
	private class BooleanStatesPane extends JPanel{
		
		private JButton btnAdd;
		private JCheckBox checkGloballyExclusive; 
		private JTextField txtName = new JTextField(); 
		
		public BooleanStatesPane(){
			
			super(new GridLayout(3,1)); 
			
			btnAdd = new JButton(iconController.getMoreIcon());
			btnAdd.setToolTipText(resource.getString("addStateTip")); 
	 	    //JLabel labelNotEditable = new JLabel(" " + resource.getString("insertBooleanStates")); 

	 	    JToolBar barAddStates = new JToolBar(); 
	 	    barAddStates.setFloatable(false); 
	 	    barAddStates.setLayout(new GridLayout(1,3)); 
	 	    barAddStates.add(new JLabel()); 
	 	    barAddStates.add(btnAdd);
	 	    barAddStates.add(new JLabel()); 
	 	    
	 	    //barAddStates.add(labelNotEditable); 
	 	    
		    JToolBar barName = new JToolBar(); 
		    barName.setFloatable(false); 
		    JLabel labelName = new JLabel(resource.getString("nameLabel") + " "); 
		    barName.add(labelName); 
		    barName.add(txtName); 
	 	    
		    JToolBar toolGloballyExclusive = new JToolBar();
		    toolGloballyExclusive.setFloatable(false);
		    JLabel labelExclusive = new JLabel(resource.getString("isGloballyExclusive")); 
		    checkGloballyExclusive = new JCheckBox(); 
		    checkGloballyExclusive.setSelected(false); 
		    toolGloballyExclusive.add(checkGloballyExclusive); 
		    toolGloballyExclusive.add(labelExclusive); 
	 	    
	 	    add(barAddStates); 
	 	    add(barName); 
	 	    add(toolGloballyExclusive); 
	 	    
	 	    
	 	    
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
							listStatesPanel.update();
						}
					}else{
						mebnController.addBooleanAsPossibleValue(residentNode); 
						residentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES);
						listStatesPanel.update(); 
					}
					
				}
	 	    	
	 	    }); 
	 	    
		}
		
	}
	

	
	/**
	 * Painel para seleção do tipo de argumento que o resident node terá. 
	 * Apresenta um botão para cada opção possível e um rótulo indicando a 
	 * opção selecionada. 
	 * 
	 * @author Laecio Lima dos Santos. 
	 */
	private class OptionsPanel extends JPanel{
		
		private JButton btnCategoryStates; 
		private JButton btnObjectStates; 
		private JButton btnBooleanStates; 
		
		public OptionsPanel(){ 

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
			
			
			this.setLayout(new GridLayout(1, 3)); 
			this.add(btnCategoryStates); 
			this.add(btnObjectStates); 
			this.add(btnBooleanStates);
		}
		
	}
	
	/**
	 * Renderizador para celula da lista de estados. 
	 */
	private class StateCellRenderer extends DefaultListCellRenderer{
		
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
