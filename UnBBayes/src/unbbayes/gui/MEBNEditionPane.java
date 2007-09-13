package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.ArgumentEditionPane;
import unbbayes.gui.mebn.EntityEditionPane;
import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.InputNodePane;
import unbbayes.gui.mebn.MTheoryTree;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.OrdVariableToolBar;
import unbbayes.gui.mebn.ResidentNodePane;
import unbbayes.gui.mebn.TableEditionPane;
import unbbayes.gui.mebn.auxiliary.ButtonLabel;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.gui.mebn.finding.EntityFindingEditionPane;
import unbbayes.gui.mebn.finding.RandonVariableFindingEdtitionPane;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.DuplicatedNameException;

/**
 * Pane for edition of MEBN. This is the main panel of 
 * the MEBN suport of the UnBBayes. All others painels of MEBN
 * are inside this panel. 
 * 
 *  @author LaÈcio Lima dos Santos
 *  @author Rommel N. Carvalho    
 *  @version 1.0 06/08/07                              
 */

public class MEBNEditionPane extends JPanel {
	
	private static final long serialVersionUID = 6194855055129252835L;
	
	private final NetworkWindow netWindow;

	/* 
	 * Contem as opcoes gerais do suporte a MEBN: 
	 * - Editar os templates
	 * - Entrar com os findings e queries
	 * - Configuracoes do programa
	 */
	private JToolBar jtbGeneralOptions;
	
	/* Mostra o painel de edicao do objeto ativo */
	private JPanel tabsPanel; 	
	
	/* 
	 * Panel que contem: 
	 * - O painel de edicao do objeto atual
	 * - O grafo de edicao 
	 */
	private JSplitPane centerPanel; 
	
	/*
	 * - Grafo
	 * - Painel de resumo
	 */
	private JSplitPane jspGraphHelper; 
	
	/* 
	 * Painel utilizado para mostrar 
	 * - textos de ajuda para o usuario
	 * - relatorios
	 * - descricoes de erros complexas
	 */
	private JPanel helpPanel; 
	
	/*---- TabPanel and tabs ----*/
	private JToolBar jtbTabSelection; 
    private JPanel jpTabSelected;
    
    private MTheoryTree mTheoryTree; 
    private JScrollPane mTheoryTreeScroll; 
    private FormulaEditionPane formulaEdtion;    
    private EntityEditionPane entityEditionPane;    
    private InputNodePane inputNodePane;      
    private OVariableEditionPane editOVariableTab;
    private RandonVariableFindingEdtitionPane nodeFindingEditionPane; 
    private EntityFindingEditionPane entityFindingEditionPane; 
    
    private ResidentNodePane residentNodePane; 
    private ArgumentEditionPane editArgumentsTab; 
    
    private JPanel jpDescription; 
    
    /*---- Global Options ----*/
    
    private GlobalOptionsDialog go;
    
    /* Text fields */
    
    private JTextField txtNameMTheory; 
    private JTextField txtNameResident; 
    private JTextField txtDescription;
    private JTextField txtFormula;   
    private JTextField txtNameMFrag; 
    private JTextField txtNameContext; 
    private JTextField txtNameInput;     
    private JTextField txtInputOf; 
    private JTextField txtArguments; 
    
    
    private final MEBNController mebnController;
    private final JSplitPane graphPanel;
    private final JLabel status;
    private final JPanel bottomPanel;

    private final JPanel topPanel;
    private final JToolBar jtbEdition;
    
    private final JPanel nodeSelectedBar;
    private final CardLayout cardLayout = new CardLayout(); 
    
    /* Tool bars for each possible edition mode */
    
    private final JToolBar jtbEmpty; 
    private final JToolBar jtbMFrag; 
    private final JToolBar jtbResident;
    private final JToolBar jtbInput; 
    private final JToolBar jtbContext;  
    private final JToolBar jtbMTheory; 
    private final OrdVariableToolBar jtbOVariable;
    
    private final String MTHEORY_BAR = "MTheoryCard"; 
    private final String RESIDENT_BAR = "ResidentCard";
    private final String CONTEXT_BAR = "ContextCard";
    private final String INPUT_BAR = "InputCard";
    private final String MFRAG_BAR = "MFragCard";
    private final String EMPTY_BAR = "EmptyCard";
    private final String ORDVARIABLE_BAR = "OrdVariableCard";

    private final JButton btnAddMFrag; 
    private final JButton btnAddContextNode;
    private final JButton btnAddInputNode;
    private final JButton btnAddResidentNode;
    private final JButton btnAddEdge;
    private final JButton btnAddOrdinaryVariable; 
    private final JButton btnEditMTheory;
    
    private final JButton btnSelectObject;
    private final JButton btnGlobalOption;
    
    private final JButton btnEditingMode; 
    private final JButton btnFindingMode; 
    private final JButton btnQueryMode; 
    
    /* Buttons for select the active tab */
    
    private final JButton btnTabOptionTree;                                    
    private final JButton btnTabOptionOVariable; 
    private final JButton btnTabOptionEntity; 
    private final JButton btnTabOptionEntityFinding; 
    private final JButton btnTabOptionNodeFinding; 
    private JButton btnTabOption1; 
    private JButton btnTabOption2; 
    
    private final int INDEX_POSITION_BUTTON_OPTION_1 = 4; 
    private final int INDEX_POSITION_BUTTON_OPTION_2 = 5; 
    
    private final String MTHEORY_TREE_TAB = "MTheoryTree"; 
    private final String ENTITY_EDITION_TAB = "EntityEdtionTab"; 
    private final String OVARIABLE_EDITION_TAB = "EditOVariableTab"; 
    private final String INPUT_NODE_TAB = "InputNodeTab"; 
    private final String ARGUMENTS_EDITION_TAB = "EditArgumentsTab"; 
    private final String RESIDENT_NODE_TAB = "ResidentNodeTab"; 
    private final String FORMULA_TAB = "FormulaEdtion"; 
    private final String ENTITY_FINDING_TAB = "EntityFindingTab"; 
    private final String NODE_FINDING_TAB = "NodeFindingTab"; 
   
    /* Table edition pane */
    private TableEditionPane tableEdit; 
    
    /* Matcher used for verify if a text is a name valid*/
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;

    /* Icon Controller */
    private final IconController iconController = IconController.getInstance();

	/* Load resource file from this package */
  	private static ResourceBundle resource = 
  		    ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
  	
  	public MEBNEditionPane(NetworkWindow _netWindow,
            MEBNController _controller) {
        this.netWindow     = _netWindow;
        this.mebnController    = _controller;
        this.setLayout(new BorderLayout());

        topPanel    = new JPanel(new GridLayout(0,1));
        
        tabsPanel = new JPanel(new BorderLayout()); 
        
        jpTabSelected = new JPanel(cardLayout); 
        jpDescription = new JPanel(new BorderLayout()); 
        jtbTabSelection = new JToolBar(); 

        nodeSelectedBar = new JPanel(cardLayout); 
        jtbEdition  = new JToolBar();

        jtbGeneralOptions = new JToolBar(); 
        
        jtbEmpty = new JToolBar(); 
        
        
        graphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        graphPanel.setDividerSize(1); 
        
        helpPanel = new JPanel(); 
        jspGraphHelper = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, helpPanel); 
        jspGraphHelper.setResizeWeight(1.0); 
        jspGraphHelper.setDividerSize(1); 
        
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        status      = new JLabel(resource.getString("statusReadyLabel"));

        txtDescription     = new JTextField(15);
        
        txtNameMTheory = new JTextField(5);         
        txtNameMFrag = new JTextField(5); 
        txtNameContext = new JTextField(5); 
        txtArguments = new JTextField(10); 
        txtFormula = new JTextField(15); 
        
        btnEditMTheory = new JButton(iconController.getMTheoryNodeIcon()); 
        btnAddEdge               = new JButton(iconController.getEdgeIcon());
        btnAddContextNode = new JButton(iconController.getContextNodeIcon());
        btnAddInputNode  = new JButton(iconController.getInputNodeIcon());
        btnAddResidentNode  = new JButton(iconController.getResidentNodeIcon());
        btnAddOrdinaryVariable = new JButton(iconController.getOVariableNodeIcon());
        btnAddMFrag		= new JButton(iconController.getMFragIcon()); 
        btnSelectObject            = new JButton(iconController.getSelectionIcon());
        btnGlobalOption      = new JButton(iconController.getGlobalOptionIcon());

        btnEditMTheory.setToolTipText(resource.getString("mTheoryEditionTip"));
        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
        btnAddMFrag.setToolTipText(resource.getString("mFragInsertToolTip")); 
        btnAddContextNode.setToolTipText(resource.getString("contextNodeInsertToolTip"));
        btnAddInputNode.setToolTipText(resource.getString("inputNodeInsertToolTip"));
        btnAddResidentNode.setToolTipText(resource.getString("residentNodeInsertToolTip"));;
        btnSelectObject.setToolTipText(resource.getString("mFragInsertToolTip")); 
        btnGlobalOption.setToolTipText(resource.getString("mFragInsertToolTip"));         
        
        btnEditingMode = new JButton(iconController.getMTheoryNodeIcon()); 
        btnFindingMode = new JButton(iconController.getMTheoryNodeIcon()); 
        btnQueryMode = new JButton(iconController.getMTheoryNodeIcon()); 
        
        btnTabOptionTree = new JButton(iconController.getMTheoryNodeIcon());
        btnTabOptionOVariable = new JButton(iconController.getOVariableNodeIcon()); 
        btnTabOptionEntity = new JButton(iconController.getObjectEntityIcon()); 
        btnTabOptionEntityFinding = new JButton(iconController.getEntityInstance()); 
        btnTabOptionNodeFinding = new JButton(iconController.getNodeInstance());  
        
        btnTabOptionTree.setToolTipText(resource.getString("showMTheoryToolTip")); 
        btnTabOptionOVariable.setToolTipText(resource.getString("showOVariablesToolTip"));
        btnTabOptionEntity.setToolTipText(resource.getString("showEntitiesToolTip"));
        
        addActionListenersToButtons(); 
        
        //colocar botoes e controladores do look-and-feel no toolbar e esse no topPanel
        
        jtbEdition.add(btnEditMTheory); 
        jtbEdition.addSeparator(); 
        jtbEdition.add(btnAddMFrag);
        jtbEdition.addSeparator(); 
        jtbEdition.add(btnAddResidentNode);
        jtbEdition.add(btnAddInputNode);
        jtbEdition.add(btnAddContextNode);
        jtbEdition.add(btnAddOrdinaryVariable); 
        jtbEdition.add(btnAddEdge);
        jtbEdition.addSeparator(); 
        jtbEdition.add(btnSelectObject);
        jtbEdition.addSeparator(); 
    
        jtbEdition.setFloatable(true); 
        jtbEdition.setOrientation(JToolBar.VERTICAL); 
        
        /* testes... */
        //buildJtbPowerLoom();  
        jtbGeneralOptions.add(btnEditingMode);
        jtbGeneralOptions.add(btnFindingMode); 
        jtbGeneralOptions.add(btnQueryMode);
        
        jtbGeneralOptions.setFloatable(false); 
        topPanel.add(jtbGeneralOptions);
        
        jtbMFrag = buildJtbMFrag(); 
        jtbResident = buildJtbResident(); 
        jtbInput = buildJtbInput(); 
        jtbContext = buildJtbContext(); 
        jtbOVariable = buildJtbOVariable(); 
        
        /*---- jtbMTheory ----*/
        jtbMTheory = buildJtbMTheory();  
        
        
        /*---- jtbEmpty ----*/
        JTextField txtIsEmpty = new JTextField(resource.getString("whithotMFragActive")); 
        txtIsEmpty.setEditable(false); 
        jtbEmpty.addSeparator(); 
        jtbEmpty.add(txtIsEmpty);
        jtbEmpty.setFloatable(false); 
        
        /*---- Add card panels in the layout ----*/
        
        nodeSelectedBar.add(MTHEORY_BAR, jtbMTheory); 
        nodeSelectedBar.add(RESIDENT_BAR, jtbResident);
        nodeSelectedBar.add(CONTEXT_BAR, jtbContext); 
        nodeSelectedBar.add(INPUT_BAR, jtbInput); 
        nodeSelectedBar.add(MFRAG_BAR, jtbMFrag); 
        nodeSelectedBar.add(EMPTY_BAR, jtbEmpty); 
        nodeSelectedBar.add(ORDVARIABLE_BAR, jtbOVariable); 
        
        cardLayout.show(nodeSelectedBar, EMPTY_BAR); 
        
        topPanel.add(nodeSelectedBar); 

        bottomPanel.add(status);
       
        /*----------------- Icones do Tab Panel ------------*/

        jtbTabSelection.setLayout(new GridLayout(1,3)); 
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.add(btnTabOptionOVariable); 
        jtbTabSelection.add(btnTabOptionEntity);   
        jtbTabSelection.add(btnTabOptionEntityFinding); 
        jtbTabSelection.add(btnTabOptionNodeFinding); 
        jtbTabSelection.setFloatable(false);
        
        
        /*---------------- Tab panel ----------------------*/
        
        mTheoryTree = new MTheoryTree(mebnController, netWindow.getGraphPane()); 
        mTheoryTreeScroll = new JScrollPane(mTheoryTree); 
        mTheoryTreeScroll.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(
        		resource.getString("MTheoryTreeTitle"))); 
        jpTabSelected.add(MTHEORY_TREE_TAB, mTheoryTreeScroll);
        
        entityEditionPane = new EntityEditionPane(mebnController); 
        jpTabSelected.add(ENTITY_EDITION_TAB, entityEditionPane); 
        
    	editOVariableTab = new OVariableEditionPane(); 
        jpTabSelected.add(OVARIABLE_EDITION_TAB, editOVariableTab); 
        
        inputNodePane = new InputNodePane();                 
        jpTabSelected.add(INPUT_NODE_TAB, inputNodePane); 
       
        editArgumentsTab = new ArgumentEditionPane(); 
        jpTabSelected.add(ARGUMENTS_EDITION_TAB, editArgumentsTab); 
        
        residentNodePane = new ResidentNodePane(); 
        jpTabSelected.add(RESIDENT_NODE_TAB, residentNodePane); 
        
        formulaEdtion = new FormulaEditionPane(); 
        jpTabSelected.add(FORMULA_TAB, formulaEdtion); 

        entityFindingEditionPane = new EntityFindingEditionPane(); 
        jpTabSelected.add(ENTITY_FINDING_TAB, entityFindingEditionPane); 
        
        nodeFindingEditionPane = new RandonVariableFindingEdtitionPane(); 
        jpTabSelected.add(NODE_FINDING_TAB, nodeFindingEditionPane); 
        
        cardLayout.show(jpTabSelected, "MTheoryTree");  
        
        /*------------------ Description panel ---------------*/
        
        jpDescription = buildJpDescritpion(); 
        
        /*------------------- Left panel ---------------------*/
        
        tabsPanel.add(BorderLayout.NORTH, jtbTabSelection);
        tabsPanel.add(BorderLayout.CENTER, jpTabSelected); 
        tabsPanel.add(BorderLayout.SOUTH, jpDescription); 
        tabsPanel.add(BorderLayout.EAST, jtbEdition); 
        
        centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabsPanel, jspGraphHelper); 
        //centerPanel.setResizeWeight(0.25);
        centerPanel.setDividerSize(5); 
        
        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);        
        this.add(centerPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }
  	
  	private void turnForFindingMode(){
  		
  		tabsPanel.remove(jtbTabSelection); 
          		
  		jtbTabSelection.removeAll(); 
        jtbTabSelection.setLayout(new GridLayout(1,3)); 
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.add(btnTabOptionOVariable); 
        jtbTabSelection.add(btnTabOptionEntity);
        jtbTabSelection.setFloatable(false);
        
        tabsPanel.add(BorderLayout.NORTH, jtbTabSelection);
  	}
  	
  	private void turnForQueryMode(){
        jtbTabSelection.setLayout(new GridLayout(1,3)); 
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.add(btnTabOptionOVariable); 
        jtbTabSelection.add(btnTabOptionEntity);  
        jtbTabSelection.setFloatable(false);
  	}
  	
  	private void turnForEditionMode(){
        jtbTabSelection.setLayout(new GridLayout(1,3)); 
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.add(btnTabOptionOVariable); 
        jtbTabSelection.add(btnTabOptionEntity);   
        jtbTabSelection.setFloatable(false);
  	}
  	
  	private void turnButtonOptionTab1(JButton btn){
        jtbTabSelection.remove(INDEX_POSITION_BUTTON_OPTION_1);  
        btnTabOption1 = btn; 
        jtbTabSelection.add(btnTabOption1, INDEX_POSITION_BUTTON_OPTION_1); 
  	}
  	
  	private void turnButtonOptionTab2(JButton btn){
        jtbTabSelection.remove(INDEX_POSITION_BUTTON_OPTION_2);  
        btnTabOption2 = btn; 
        jtbTabSelection.add(btnTabOption2, INDEX_POSITION_BUTTON_OPTION_2); 
  	}
  	
  	private JToolBar buildJtbMTheory(){
  		
  		JToolBar jtbMTheory = new JToolBar(); 
  		
  		ButtonLabel btnMTheoryActive = new ButtonLabel(resource.getString("MTheoryButton"), iconController.getMTheoryNodeIcon()); 
  		
    	btnMTheoryActive.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setMTheoryTreeActive(); 
  			}
  		});
    	
    	JLabel labelMTheoryName = new JLabel(resource.getString("nameLabel")); 
    	
    	txtNameMTheory.addFocusListener(new FocusListenerTextField()); 
    	txtNameMTheory.addKeyListener(new KeyAdapter() {
    		
  			public void keyPressed(KeyEvent e) {
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameMTheory.getText().length()>0)) {
  					try {
  						String name = txtNameMTheory.getText(0,txtNameMTheory.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							mebnController.setNameMTheory(name); 
  						}  else {
  							txtNameMTheory.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
  							txtNameMTheory.setForeground(Color.WHITE); 
  							txtNameMTheory.selectAll();
  							JOptionPane.showMessageDialog(netWindow, 
  									resource.getString("nameError"), 
  									resource.getString("nameException"), 
  									JOptionPane.ERROR_MESSAGE);
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  			
  			public void keyReleased(KeyEvent e){
  				try{
                    String name = txtNameMTheory.getText(0,txtNameMTheory.getText().length());
						matcher = wordPattern.matcher(name);
						if (!matcher.matches()) {
							txtNameMTheory.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
							txtNameMTheory.setForeground(Color.WHITE); 
						}
						else{
							txtNameMTheory.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected());
							txtNameMTheory.setForeground(Color.BLACK); 
						}
  				}
  				catch(Exception efd){
  					
  				}
  				
  			}
  		});
    	
    	jtbMTheory.add(btnMTheoryActive); 
    	jtbMTheory.addSeparator(); 
    	jtbMTheory.add(labelMTheoryName); 
    	jtbMTheory.add(txtNameMTheory); 
    	
  		jtbMTheory.setFloatable(false); 
  		
  		return jtbMTheory; 
  	}
  	
    private JToolBar buildJtbMFrag(){
     	
        JToolBar jtbMFrag = new JToolBar(); 
        
    	ButtonLabel btnMFragActive = new ButtonLabel(resource.getString("MFragButton"), iconController.getMFragIcon()); 
  		btnMFragActive.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setMTheoryTreeActive(); 
  			}
  		});
    	
        JLabel labelMFragName = new JLabel(resource.getString("nameLabel") + " "); 

        txtNameMFrag.addFocusListener(new FocusListenerTextField()); 
  		txtNameMFrag.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameMFrag.getText().length()>0)) {
  					try {
  						String name = txtNameMFrag.getText(0,txtNameMFrag.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							try{
  							   mebnController.renameMFrag(mebnController.getCurrentMFrag(), name);
  							   mTheoryTree.updateTree();
  							}
  							catch(DuplicatedNameException dne){
  	  							JOptionPane.showMessageDialog(netWindow, 
  	  									resource.getString("nameAlreadyExists"), 
  	  									resource.getString("nameException"), 
  	  									JOptionPane.ERROR_MESSAGE);
  							}
  						}  else {
  							txtNameMFrag.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
  							txtNameMFrag.setForeground(Color.WHITE); 
  							txtNameMFrag.selectAll();
  							JOptionPane.showMessageDialog(netWindow, 
  									resource.getString("nameError"), 
  									resource.getString("nameException"), 
  									JOptionPane.ERROR_MESSAGE);
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  			
  			public void keyReleased(KeyEvent e){
  				try{
                    String name = txtNameMFrag.getText(0,txtNameMFrag.getText().length());
						matcher = wordPattern.matcher(name);
						if (!matcher.matches()) {
							txtNameMFrag.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
							txtNameMFrag.setForeground(Color.WHITE); 
						}
						else{
							txtNameMFrag.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected());
							txtNameMFrag.setForeground(Color.BLACK); 
						}
  				}
  				catch(Exception efd){
  					
  				}
  				
  			}
  		});
        
  		GridLayout grid = new GridLayout(1,5); 
  		jtbMFrag.setLayout(grid); 
		
  		jtbMFrag.add(btnMFragActive); 
		
  		JToolBar barName = new JToolBar(); 
  		barName.add(labelMFragName); 
  		barName.add(txtNameMFrag); 
  		barName.setFloatable(false);
  		jtbMFrag.add(barName);  
  		
		JPanel emptyPane = new JPanel(); 
		jtbMFrag.add(emptyPane);  
		
		emptyPane = new JPanel(); 
		jtbMFrag.add(emptyPane);
		
		emptyPane = new JPanel(); 
		jtbMFrag.add(emptyPane);
		
		jtbMFrag.setFloatable(false); 
        
        return jtbMFrag; 
        
    }
    
  	private JToolBar buildJtbResident(){
  		
  		JToolBar jtbResident = new JToolBar(); 
  		
  		
  		final JLabel labelResidentName = new JLabel(resource.getString("nameLabel") + " ");
  		
  		final ButtonLabel btnResidentActive = new ButtonLabel(resource.getString("ResidentButton"), iconController.getResidentNodeIcon()); 
  		btnResidentActive.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setResidentNodeTabActive(); 
  			}
  		});
  		
  		final ButtonLabel btnAddArgument = 	 new ButtonLabel(resource.getString("ArgumentsButton"), iconController.getOVariableNodeIcon());  
  		btnAddArgument.setToolTipText(resource.getString("addArgumentToolTip")); 
  		
  		final JLabel labelArguments = new JLabel(resource.getString("arguments")); 

  		
  		btnAddArgument.addActionListener(new ActionListener(){
  			
  			public void actionPerformed(ActionEvent ae){
  				setEditArgumentsTabActive(); 
  			}
  			
  		});

        txtNameResident = new JTextField(5); 
 
  		txtNameResident.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				DomainResidentNode nodeAux = (DomainResidentNode)mebnController.getResidentNodeActive();
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameResident.getText().length()>0)) {
  					try {
  						String name = txtNameResident.getText(0,txtNameResident.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							mebnController.renameDomainResidentNode(nodeAux, name); 
  						}  else {
							txtNameResident.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
						    txtNameResident.setForeground(Color.WHITE); 
  							txtNameResident.selectAll();
  							JOptionPane.showMessageDialog(netWindow, 
  									resource.getString("nameError"), 
  									resource.getString("nameException"), 
  									JOptionPane.ERROR_MESSAGE);
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					} catch (DuplicatedNameException dne) {
							JOptionPane.showMessageDialog(netWindow, 
	  									resource.getString("nameAlreadyExists"), 
	  									resource.getString("nameException"), 
	  									JOptionPane.ERROR_MESSAGE);
					}
  				}
  			}
  			
  			public void keyReleased(KeyEvent e){
  				try{
                    String name = txtNameResident.getText(0,txtNameResident.getText().length());
						matcher = wordPattern.matcher(name);
						if (!matcher.matches()) {
							txtNameResident.setBackground(ToolKitForGuiMebn.getColorTextFieldError()); 
						    txtNameResident.setForeground(Color.WHITE); 
						}
						else{
							txtNameResident.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected());
						    txtNameResident.setForeground(Color.BLACK); 
						}
  				}
  				catch(Exception efd){
  					
  				}
  				
  			}
  		}); 
  		
        txtNameResident.addFocusListener(new FocusListenerTextField()); 

  		txtArguments.setEditable(false);
        
  		/*---- jtbResident ----*/
    	GridLayout grid = new GridLayout(1,5); 
    	jtbResident.setLayout(grid); 
        
  		jtbResident.add(btnResidentActive);
  		
  		JToolBar barName = new JToolBar(); 
  		barName.setFloatable(false); 
  		barName.add(labelResidentName); 
  		barName.add(txtNameResident);
  		jtbResident.add(barName);
  		
//  	jtbResident.add(btnAddArgument); 
  		//jtbResident.add(txtArguments); 
  		
  		JPanel emptyPane; 
  		
  		JToolBar barOptions = new JToolBar(); 
  		barOptions.setFloatable(false); 
  		barOptions.setLayout(new GridLayout(1,5)); 
  		JButton btnStateEdition = new JButton(iconController.getYellowBallIcon()); 
  		JButton btnEditTable = new JButton(iconController.getEditIcon()); 
  		JButton btnEditArguments = new JButton(iconController.getBoxVariablesIcon()); 
  		barOptions.add(btnStateEdition); 
  		barOptions.add(btnEditArguments); 
  		barOptions.add(btnEditTable); 
  		barOptions.add(new JPanel()); 
  		barOptions.add(new JPanel()); 
  		
  		jtbResident.add(barOptions); 
  		
  		emptyPane = new JPanel(); 
  		jtbResident.add(emptyPane); 
  		
  		emptyPane = new JPanel(); 
  		jtbResident.add(emptyPane); 
  		
  		jtbResident.setFloatable(false); 
  		
  		return jtbResident;  		
  	}
  	
  	private JToolBar buildJtbInput(){
  		
  		ButtonLabel btnInputActive; 
  		JLabel labelInputName; 
  		JLabel labelInputOf; 
  		
  		JToolBar jtbInput = new JToolBar(); 
  		
  		btnInputActive = new ButtonLabel(resource.getString("InputButton"), iconController.getInputNodeIcon());    		
  		btnInputActive.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				setInputNodeActive(); 
  			}
  		}); 
  		
  		labelInputName = new JLabel(resource.getString("nameLabel") + " "); 
  		
  		labelInputOf = new JLabel(resource.getString("inputOf")); 
  		
  		txtNameInput = new JTextField(10); 
        txtNameInput.setEditable(false);
  		
        txtInputOf = new JTextField(10); 
        txtInputOf.setEditable(false); 
        
    	GridLayout grid = new GridLayout(1,5); 
    	jtbInput.setLayout(grid); 
        
  		jtbInput.add(btnInputActive);
  		
  		JToolBar barName = new JToolBar(); 
  		barName.setFloatable(false); 
  		barName.add(labelInputName); 
  		barName.add(txtNameInput);
        jtbInput.add(barName);
        
  		jtbInput.add(labelInputOf); 
  		jtbInput.add(txtInputOf); 
        
        JPanel emptyPanel = new JPanel(); 
        jtbInput.add(emptyPanel); 
        
        jtbInput.setFloatable(false); 
        
  		return jtbInput; 
  	}
  	
  	private JToolBar buildJtbContext(){
        
  		JToolBar jtbContext = new JToolBar(); 
  		
  		ButtonLabel btnContextActive = new ButtonLabel(resource.getString("ContextButton"), iconController.getContextNodeIcon());  
  		btnContextActive.addActionListener(new ActionListener(){
  			
  			public void actionPerformed(ActionEvent ae){
  				setFormulaEdtionActive(); 
  			}
  			
  		});  
        jtbContext.add(btnContextActive); 
        
        jtbContext.addSeparator(); 
        
        final JLabel labelFormula = new JLabel(resource.getString("formula")); 
        jtbContext.add(labelFormula); 
        
        txtFormula.setEditable(false); 
        jtbContext.add(txtFormula);
        
        jtbContext.addSeparator();         
        
        final JLabel labelContextName = new JLabel(resource.getString("nameLabel")); 
        jtbContext.add(labelContextName);
        
        txtNameContext.setEditable(false); 
        jtbContext.add(txtNameContext);
  
        jtbContext.setFloatable(false); 
        
        return jtbContext; 
  	}
  	
	private OrdVariableToolBar buildJtbOVariable(){
        
  		OrdVariableToolBar jtbOVariable = new OrdVariableToolBar(mebnController); 
  		
        return jtbOVariable; 
   
  	}
  	
	/*
	 * Build Description panel. This panel show de description of 
	 * the active item (is used for the user for edit this description)
	 */
  	private JPanel buildJpDescritpion(){
  		
        JPanel jpDescription = new JPanel(new BorderLayout()); 
        
		TitledBorder titledBorder; 
		
		titledBorder = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLUE), 
				resource.getString("descriptionLabel")); 
		titledBorder.setTitleColor(Color.BLUE); 
		titledBorder.setTitleJustification(TitledBorder.CENTER); 
		
		jpDescription.setBorder(titledBorder); 
        
        JTextArea textArea = new JTextArea(5, 10);
        JScrollPane scrollPane = 
            new JScrollPane(textArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        textArea.setEditable(true);
        
        jpDescription.add("South", scrollPane); 
        
        return jpDescription; 
 
  	}
  	
  	private void buildJtbPowerLoom(){

        JButton rodarKB = new JButton("PL"); 
        jtbGeneralOptions.add(rodarKB); 
        rodarKB.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				mebnController.preencherKB(); 
  			    JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
  					   "Base de conhecimento criada com sucesso"); 
  			}
  		});
        
        JButton entityFinding = new JButton("EF"); 
        jtbGeneralOptions.add(entityFinding); 
        entityFinding.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				String finding = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), 
  						"Entre com o entity finding: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				if((finding!=null)&&(!finding.equals("")))
  				mebnController.makeEntityAssert(finding); 
  			}
  		});        

        JButton finding = new JButton("RF"); 
        jtbGeneralOptions.add(finding); 
        finding.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				String finding = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), 
  						"Entre com o relation finding: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				if((finding!=null)&&(!finding.equals("")))
  				mebnController.makeRelationAssert(finding); 
  			}
  		});     
        
        JButton link = new JButton("LK"); 
        jtbGeneralOptions.add(link); 
        link.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				String ovName = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), 
  						"Entre com o nome da variavel ordinaria: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				
  				String entityName = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), 
  						"Entre com o nome da entidade: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				
  				if(((ovName!=null)&&(!ovName.equals("")))&&((entityName!=null)&&(!entityName.equals(""))))
  				mebnController.linkOrdVariable2Entity(ovName, entityName); 
  			}
  		});  
        
        JButton context = new JButton("CT"); 
        jtbGeneralOptions.add(context); 
        context.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				mebnController.executeContext(); 
  			}
  		});  
        
        JButton execute = new JButton("X"); 
        jtbGeneralOptions.add(execute); 
        execute.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				String command = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), "Insira o commando a ser executado"); 
  				if((command != "")&&(command != null)){
  				   String resposta = mebnController.executeCommand(command);
  				   JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resposta); 
  				}
  			}
  		}); 
        
        JButton save = new JButton("SV"); 
        jtbGeneralOptions.add(save); 
        save.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				String fileName = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), "Informe o nome do arquivo"); 
  				if((fileName != "")&&(fileName != null)){
  				   mebnController.saveDefinitionsFile(fileName);
  				   JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Arquivo salvo com sucesso"); 
  				}
  			}
  		}); 
        
        JButton load = new JButton("LD"); 
        jtbGeneralOptions.add(load); 
        load.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				String fileName = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), "Informe o nome do arquivo"); 
  				if((fileName != "")&&(fileName != null)){
  				   mebnController.loadDefinitionsFile(fileName);
  				   JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Arquivo carregado com sucesso"); 
  				}
  			}
  		}); 
  	}
  	
  	private void addActionListenersToButtons(){
  		
  		btnEditMTheory.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				mebnController.enableMTheoryEdition(); 
  			}
  		}); 
  		
  		//ao clicar no botao btnGlobalOption, mostra-se o menu para escolha das opcoes
  		/*
  		btnGlobalOption.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setCursor(new Cursor(Cursor.WAIT_CURSOR));
  				go = new GlobalOptionsDialog(netWindow.getGraphPane(), controller);
  				go.setVisible(true);
  				netWindow.getGraphPane().update();
  				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  			}
  		});*/
  		
  		//ao clicar no bot√£o btnAddEdge setamos as variaveis booleanas e os estados dos butoes
  		btnAddEdge.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_EDGE);
  			}
  		});
  		
  		btnAddMFrag.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				//netWindow.getGraphPane().setAction(GraphAction.CREATE_DOMAIN_MFRAG);
  				mebnController.insertDomainMFrag(); 
  			}
  		}); 
  			
  		//ao clicar no botao node setamos as variaveis booleanas e os estados dos butoes
  		btnAddContextNode.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE);
  			}
  		});
  		
  		
  		//ao clicar no botao btnAddInputNode setamos as variaveis booleanas e os estados dos butoes
  		btnAddInputNode.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_INPUT_NODE);
  			}
  		});
  		
  		//ao clicar no botao btnAddResidentNode setamos as variaveis booleanas e os estados dos butoes
  		btnAddResidentNode.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_RESIDENT_NODE);
  			}
  		});
  		
        // ao clicar no botao node setamos as variaveis booleanas e os estados dos butoes
    		btnAddOrdinaryVariable.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent ae) {
    				netWindow.getGraphPane().setAction(GraphAction.CREATE_ORDINARYVARIABLE_NODE);
    			}
    		}); 
  		
  		//ao clicar no botao node setamos as variaveis booleanas e os estados dos butoes
  		btnSelectObject.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.SELECT_MANY_OBJECTS);
  			}
  		}); 	
  		
  		//ao clicar no botao btnGlobalOption, mostra-se o menu para escolha das opcoes
  		btnTabOptionTree.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setMTheoryTreeActive(); 
  			}
  		});  
  		
  		btnTabOptionOVariable.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setEditOVariableTabActive(); 
  			}
  		});  	
  		
  		btnTabOptionEntity.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setEntityEditionTabActive(); 
  			}
  		});  

  		btnTabOptionEntityFinding.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setEntityFindingEditionPaneActive(); 
  			}
  		});  
  		
  		btnTabOptionNodeFinding.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setRandonVariableFindingEditionPaneActive(); 
  			}
  		});  
  		
  	}  	


  	
    public void showTableEdit(){
    	
    	DomainResidentNode resident = (DomainResidentNode)mebnController.getResidentNodeActive(); 
    	
    	this.getGraphPanel().setTopComponent(new TableEditionPane(resident, mebnController)); 
    }

    public void showTitleGraph(String mFragName){
    	
    	JPanel titleMFragPane = new JPanel(new BorderLayout()); 
    	titleMFragPane.setBackground(new Color(7, 199, 203)); 
    	
    	JLabel label = new JLabel(mFragName);
    	label.setForeground(Color.BLACK); 
    	label.setHorizontalAlignment(JLabel.CENTER); 
    	label.setHorizontalTextPosition(JLabel.CENTER);
    	
    	titleMFragPane.add(label, BorderLayout.CENTER); 
    	
    	this.getGraphPanel().setTopComponent(titleMFragPane);
    	
    }
    
    public void hideTopComponent(){
    	
    	this.getGraphPanel().setTopComponent(null); 
    	 
    }    
    
    /**
     *  Retorna o text field da descricao do no.
     *
     *@return    retorna a txtDescricao (<code>JTextField</code>)
     *@see       JTextField
     */
    public JTextField getTxtDescription() {
      return this.txtDescription;
    }

    /**
     *  Retorna o text field da name do no.
     *
     *@return    retorna a txtName (<code>JTextField</code>)
     *@see       JTextField
     */
    public JTextField getTxtNameResident() {
      return this.txtNameResident;
    }

    /**
     * Seta o status exibido na barra de status.
     *
     * @param status mensagem de status.
     */
    public void setStatus(String status) {
        this.status.setText(status);
    }

    public void setNameMTheory(String name){
    	this.txtNameMTheory.setText(name); 
    }
    
    public String getNameMTheory(String name){
    	return this.txtNameMTheory.getText(); 
    }
    
    /**
     *  Retorna o painel do centro onde fica o graph e a table.
     *
     *@return    retorna o centerPanel (<code>JSplitPane</code>)
     *@see       JSplitPane
     */
    public JSplitPane getGraphPanel() {
      return this.graphPanel;
    }

    public JButton getBtnAddEdge() {
        return this.btnAddEdge;
    }

    public JButton getBtnAddInputNode() {
        return this.btnAddInputNode;
    }

    public JButton getBtnGlobalOption() {
        return this.btnGlobalOption;
    }

    public JButton getBtnAddContextNode() {
        return this.btnAddContextNode;
    }

    public JButton getBtnSelectObject() {
        return this.btnSelectObject;
    }

    public JButton getBtnAddResidentNode() {
        return this.btnAddResidentNode;
    }
    
    public MTheoryTree getMTheoryTree(){
    	return mTheoryTree; 
    }
    
    public ArgumentEditionPane getEditArgumentsTab(){
         return editArgumentsTab; 	
    }
    
    public OVariableEditionPane getEditOVariableTab(){
    	return editOVariableTab; 
    }
    
    /* TabPanel */
    
    public void setMTheoryTreeActive(){
        cardLayout.show(jpTabSelected, "MTheoryTree");  
        mTheoryTree.updateTree(); 
    }
    
    public void setFormulaEdtionActive(){
        cardLayout.show(jpTabSelected, "FormulaEdtion");  
    }
    
    public void setFormulaEdtionActive(ContextNode context){
    	   
        jpTabSelected.remove(formulaEdtion);     	
    	formulaEdtion = new FormulaEditionPane(mebnController, context); 
    	jpTabSelected.add("FormulaEdtion", formulaEdtion);         
    	cardLayout.show(jpTabSelected, "FormulaEdtion"); 
    }    

    public void setEntityTreeActive(){
        cardLayout.show(jpTabSelected, "EntityTree");  
    }    
    
    public void setInputNodeActive(GenerativeInputNode input){
    	jpTabSelected.remove(inputNodePane);     	
		inputNodePane = new InputNodePane(mebnController, input); 
    	jpTabSelected.add("InputNodeTab", inputNodePane);         
    	cardLayout.show(jpTabSelected, "InputNodeTab");
    }
    
    public InputNodePane getInputNodePane(){
    	return inputNodePane; 
    }
    
    public void setInputNodeActive(){
    	cardLayout.show(jpTabSelected, "InputNodeTab");    	
    }
    
    public void setArgumentTabActive(){
        cardLayout.show(jpTabSelected, "ArgumentTab"); 	
    }
    
    public void setEditArgumentsTabActive(ResidentNode resident){
   
        jpTabSelected.remove(editArgumentsTab);     	
    	editArgumentsTab = new ArgumentEditionPane(mebnController, resident); 
    	jpTabSelected.add("EditArgumentsTab", editArgumentsTab);   
    	
    	
    	cardLayout.show(jpTabSelected, "EditArgumentsTab"); 
    }
    
    public void setEditArgumentsTabActive(){
    	editArgumentsTab.update();    
        cardLayout.show(jpTabSelected, "EditArgumentsTab"); 
        
    }
    
    public void setEditOVariableTabActive(){
    	
    	if(mebnController.getCurrentMFrag() != null){
           cardLayout.removeLayoutComponent(editOVariableTab); 
    	   editOVariableTab = new OVariableEditionPane(mebnController); 
           jpTabSelected.add("EditOVariableTab", editOVariableTab); 
    	   cardLayout.show(jpTabSelected, "EditOVariableTab"); 
    	}
    }
    
    public void setEntityEditionTabActive(){
    	
    	cardLayout.show(jpTabSelected, "EntityEdtionTab"); 

    }    
    
    public void setResidentNodeTabActive(DomainResidentNode resident){
    	
    	if(mebnController.getCurrentMFrag() != null){  		
    		jpTabSelected.remove(residentNodePane);     	
    		residentNodePane = new ResidentNodePane(mebnController, resident); 
        	jpTabSelected.add("ResidentNodeTab", residentNodePane);         
        	cardLayout.show(jpTabSelected, "ResidentNodeTab"); 	
    	}
    }
    
    public void setResidentNodeTabActive(){
    	cardLayout.show(jpTabSelected, "ResidentNodeTab"); 	
    }

    public void setEntityFindingEditionPaneActive(){
       cardLayout.removeLayoutComponent(entityFindingEditionPane); 
       entityFindingEditionPane = new EntityFindingEditionPane(mebnController); 
       jpTabSelected.add(ENTITY_FINDING_TAB, entityFindingEditionPane); 
 	   cardLayout.show(jpTabSelected, ENTITY_FINDING_TAB); 
    }
    
    public void setRandonVariableFindingEditionPaneActive(){
       cardLayout.removeLayoutComponent(nodeFindingEditionPane); 
       nodeFindingEditionPane = new RandonVariableFindingEdtitionPane(mebnController);  
       jpTabSelected.add(NODE_FINDING_TAB, nodeFindingEditionPane); 
 	   cardLayout.show(jpTabSelected, NODE_FINDING_TAB); 
    }
    
    
    /* Bar Selected */
    
    public void setMTheoryBarActive(){
        cardLayout.show(nodeSelectedBar, MTHEORY_BAR);  
    }    
    
    public void setResidentBarActive(){
        cardLayout.show(nodeSelectedBar, RESIDENT_BAR);  
    }    
    
    public void setContextBarActive(){
        cardLayout.show(nodeSelectedBar, CONTEXT_BAR);  
    }  
    
    public void setInputBarActive(){
        cardLayout.show(nodeSelectedBar, INPUT_BAR);  
    } 
    
    public void setMFragBarActive(){
        cardLayout.show(nodeSelectedBar, MFRAG_BAR);  
    } 
    
    public void setEmptyBarActive(){
        cardLayout.show(nodeSelectedBar, EMPTY_BAR);  
    } 
    
    public void setOrdVariableBarActive(OrdinaryVariable ov){
        jtbOVariable.updateListOfTypes(); 
        jtbOVariable.setOrdVariable(ov); 
        cardLayout.show(nodeSelectedBar, ORDVARIABLE_BAR);
    }
    
    /*---------------------------------------------------------*/
    
    public void setTxtNameResident(String name){
    	txtNameResident.setText(name); 
    }

    public void setTxtNameMFrag(String name){
    	txtNameMFrag.setText(name); 
    }

    public void setTxtNameInput(String name){
    	txtNameInput.setText(name); 
    }

    public void setTxtNameContext(String name){
    	txtNameContext.setText(name); 
    }   

    public void setTxtInputOf(String name){
    	txtInputOf.setText(name); 
    }

    public void setTxtArguments(String args){
    	txtArguments.setText(args); 
    }

	public void setFormula(String formula) {
		this.txtFormula.setText(formula);
	}
	
	public GridBagConstraints getConstraints( 
			int gridx, 
			int gridy, 
			int gridwidth, 
			int gridheight, 
			double weightx,
			double weighty, 
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