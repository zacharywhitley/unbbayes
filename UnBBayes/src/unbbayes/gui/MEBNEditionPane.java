package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;

/**
 * Pane for edition of MEBN. This is the main panel of 
 * the MEBN suport of the UnBBayes. All others painels of MEBN
 * are inside this panel. 
 * 
 *  @author Laécio Lima dos Santos
 *  @author Rommel N. Carvalho                                  
 */

public class MEBNEditionPane extends JPanel {
	
	private static final long serialVersionUID = 6194855055129252835L;
	
	private final NetworkWindow netWindow;

	/* 
	 * Contem as opcoes gerais do suporte a MEBN: 
	 * - Editar os templates
	 * - Entrar com os findings e queries
	 * - Configurações do programa
	 */
	private JToolBar jtbGeneralOptions;
	
	/* Mostra o painel de edição do objeto ativo */
	private JPanel tabsPanel; 	
	
	/* 
	 * Panel que contem: 
	 * - O painel de edição do objeto atual
	 * - O grafo de edição 
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
	 * - relatórios
	 * - descrições de erros complexas
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
    
    
    private final MEBNController controller;
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
    
    /* Buttons for select the active tab */
    
    private final JButton btnTabOptionTree;                                    
    private final JButton btnTabOptionOVariable; 
    private final JButton btnTabOptionEntity; 
    private JButton btnTabOption1; 
    private JButton btnTabOption2; 
    
    private final int INDEX_POSITION_BUTTON_OPTION_1 = 4; 
    private final int INDEX_POSITION_BUTTON_OPTION_2 = 5; 
   
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
        this.controller    = _controller;
        this.setLayout(new BorderLayout());

        //table       = new JTable();
        //jspTable    = new JScrollPane(table);
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

        //criar labels e textfields que serão usados no jtbState
        txtDescription     = new JTextField(15);
        
        txtNameMTheory = new JTextField(5);         
        txtNameMFrag = new JTextField(5); 
        txtNameContext = new JTextField(5); 
        txtArguments = new JTextField(10); 
        txtFormula = new JTextField(15); 
        
        //criar botões que serão usados nodeList toolbars
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
        
        btnTabOptionTree = new JButton(iconController.getMTheoryNodeIcon());
        btnTabOptionOVariable = new JButton(iconController.getOVariableNodeIcon()); 
        btnTabOptionEntity = new JButton(iconController.getEntityNodeIcon()); 
        
        btnTabOptionTree.setToolTipText(resource.getString("showMTheoryToolTip")); 
        btnTabOptionOVariable.setToolTipText(resource.getString("showOVariablesToolTip"));
        btnTabOptionEntity.setToolTipText(resource.getString("showEntitiesToolTip"));
        
        addActionListeners(); 
        
        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel
        
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
    
        jtbEdition.setFloatable(false); 
        jtbEdition.setOrientation(JToolBar.VERTICAL); 
        
        /* testes... */
        JButton rodarKB = new JButton("PL"); 
        jtbGeneralOptions.add(rodarKB); 
        rodarKB.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				controller.preencherKB(); 
  			}
  		});
        
        JButton entityFinding = new JButton("EF"); 
        jtbGeneralOptions.add(entityFinding); 
        entityFinding.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				String finding = JOptionPane.showInputDialog(null, 
  						"Entre com o entity finding: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				controller.makeEntityAssert(finding); 
  			}
  		});        

        JButton finding = new JButton("RF"); 
        jtbGeneralOptions.add(finding); 
        finding.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				String finding = JOptionPane.showInputDialog(null, 
  						"Entre com o relation finding: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				controller.makeRelationAssert(finding); 
  			}
  		});     
        
        JButton link = new JButton("LK"); 
        jtbGeneralOptions.add(link); 
        link.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				String ovName = JOptionPane.showInputDialog(null, 
  						"Entre com o nome da variavel ordinaria: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				String entityName = JOptionPane.showInputDialog(null, 
  						"Entre com o nome da entidade: ", 
  						"Test Finding", 
  						JOptionPane.QUESTION_MESSAGE); 
  				//controller.linkOrdVariable2Entity(ovName, entityName); 
  			}
  		});  
        
        JButton context = new JButton("CT"); 
        jtbGeneralOptions.add(context); 
        context.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				controller.executeContext(); 
  			}
  		});  
        
        JButton save = new JButton("SV"); 
        jtbGeneralOptions.add(save); 
        save.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) { 
  				controller.saveDefinitionsFile(); 
  			}
  		}); 
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

        jtbTabSelection.setLayout(new GridLayout(1,5)); 
        jtbTabSelection.add(btnTabOptionTree);
        btnTabOptionTree.setBackground(new Color(78, 201, 249)); 
        jtbTabSelection.add(btnTabOptionOVariable); 
        btnTabOptionOVariable.setBackground(new Color(78, 201, 249)); 
        jtbTabSelection.add(btnTabOptionEntity);   
        btnTabOptionEntity.setBackground(new Color(78, 201, 249)); 
        btnTabOption1 = new JButton(" "); 
        btnTabOption1.setBackground(new Color(78, 201, 249)); 
        jtbTabSelection.add(btnTabOption1); 
        btnTabOption2 = new JButton(" "); 
        btnTabOption2.setBackground(new Color(78, 201, 249)); 
        jtbTabSelection.add(btnTabOption2); 
        jtbTabSelection.setFloatable(false);
        
        
        /*---------------- Tab panel ----------------------*/
        
        mTheoryTree = new MTheoryTree(controller, netWindow.getGraphPane()); 
        mTheoryTreeScroll = new JScrollPane(mTheoryTree); 
        mTheoryTreeScroll.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(
        		resource.getString("MTheoryTreeTitle"))); 
        jpTabSelected.add("MTheoryTree", mTheoryTreeScroll);
        
        entityEditionPane = new EntityEditionPane(controller); 
        jpTabSelected.add("EntityEdtionTab", entityEditionPane); 
        
    	editOVariableTab = new OVariableEditionPane(); 
        jpTabSelected.add("EditOVariableTab", editOVariableTab); 
        
        inputNodePane = new InputNodePane();                 
        jpTabSelected.add("InputNodeTab", inputNodePane); 
       
        editArgumentsTab = new ArgumentEditionPane(); 
        jpTabSelected.add("EditArgumentsTab", editArgumentsTab); 
        
        residentNodePane = new ResidentNodePane(); 
        jpTabSelected.add("ResidentNodeTab", residentNodePane); 
        
        formulaEdtion = new FormulaEditionPane(); 
        jpTabSelected.add("FormulaEdtion", formulaEdtion); 
        
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
  	
  	private void turnButtonOptionTab1(JButton btn){
        jtbTabSelection.remove(INDEX_POSITION_BUTTON_OPTION_1);  
        btnTabOption1 = btn; 
        btnTabOption1.setBackground(new Color(78, 201, 249)); 
        jtbTabSelection.add(btnTabOption1, INDEX_POSITION_BUTTON_OPTION_1); 
  	}
  	
  	private void turnButtonOptionTab2(JButton btn){
        jtbTabSelection.remove(INDEX_POSITION_BUTTON_OPTION_2);  
        btnTabOption2 = btn; 
        btnTabOption2.setBackground(new Color(78, 201, 249)); 
        jtbTabSelection.add(btnTabOption2, INDEX_POSITION_BUTTON_OPTION_2); 
  	}
  	
  	private JToolBar buildJtbMTheory(){
  		
  		JToolBar jtbMTheory = new JToolBar(); 
  		
    	JButton btnMTheoryActive = new JButton(resource.getString("MTheoryButton")); 
  		
    	btnMTheoryActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
    	btnMTheoryActive.setForeground(Color.WHITE);
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
  							controller.setNameMTheory(name); 
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
        
    	JButton btnMFragActive = new JButton(resource.getString("MFragButton")); 
  		
    	btnMFragActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
    	btnMFragActive.setForeground(Color.WHITE);
    	btnMFragActive.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setMTheoryTreeActive(); 
  			}
  		});
    	
        JLabel labelMFragName = new JLabel(resource.getString("nameLabel")); 
     

        txtNameMFrag.addFocusListener(new FocusListenerTextField()); 
        
        jtbMFrag.add(btnMFragActive); 
        jtbMFrag.addSeparator(); 
        jtbMFrag.add(labelMFragName);
        jtbMFrag.add(txtNameMFrag);        
    
        jtbMFrag.setFloatable(false); 
        
        return jtbMFrag; 
        
    }
    
  	private JToolBar buildJtbResident(){
  		
  		JToolBar jtbResident = new JToolBar(); 
  		
  		final JLabel labelResidentName = new JLabel(resource.getString("nameLabel"));
  		
  		final JButton btnResidentActive = new JButton(resource.getString("ResidentButton")); 
  		btnResidentActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnResidentActive.setForeground(Color.WHITE);
  		btnResidentActive.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setResidentNodeTabActive(); 
  			}
  		});
  		
  		final JButton btnAddArgument = 	 new JButton(resource.getString("ArgumentsButton"));  
  		btnAddArgument.setToolTipText(resource.getString("addArgumentToolTip")); 
  		
  		final JLabel labelArguments = new JLabel(resource.getString("arguments")); 

  		
  		btnAddArgument.addActionListener(new ActionListener(){
  			
  			public void actionPerformed(ActionEvent ae){
  				setEditArgumentsTabActive(); 
  			}
  			
  		});
  		btnAddArgument.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnAddArgument.setForeground(Color.WHITE);
  		

        txtNameResident = new JTextField(5); 
 
  		txtNameResident.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				DomainResidentNode nodeAux = (DomainResidentNode)controller.getResidentNodeActive();
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameResident.getText().length()>0)) {
  					try {
  						String name = txtNameResident.getText(0,txtNameResident.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							controller.renameDomainResidentNode(nodeAux, name); 
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
        
  		/*---- jtbResident ----*/
  		jtbResident.add(btnResidentActive); 
  		jtbResident.addSeparator();         
  		jtbResident.add(labelResidentName);
  		jtbResident.add(txtNameResident);
  		jtbResident.addSeparator();
  		jtbResident.add(btnAddArgument);
  		txtArguments.setEditable(false); 
  		jtbResident.add(txtArguments); 
  		
  		jtbResident.setFloatable(false); 
  		
  		return jtbResident;  		
  	}
  	
  	private JToolBar buildJtbInput(){
  		
  		JButton btnInputActive; 
  		JLabel labelInputName; 
  		JLabel labelInputOf; 
  		
  		JToolBar jtbInput = new JToolBar(); 
  		
  		btnInputActive = new JButton(resource.getString("InputButton"));    		
  		btnInputActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnInputActive.setForeground(Color.WHITE);
  		btnInputActive.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				setInputNodeActive(); 
  			}
  		}); 
  		
  		labelInputName = new JLabel(resource.getString("nameLabel")); 
  		
  		labelInputOf = new JLabel(resource.getString("inputOf")); 
  		
  		txtNameInput = new JTextField(10); 
        txtNameInput.setEditable(false);
  		
        txtInputOf = new JTextField(10); 
        txtInputOf.setEditable(false); 
        
  		jtbInput.add(btnInputActive);
  		jtbInput.addSeparator(); 
  		jtbInput.add(labelInputOf); 
        jtbInput.add(txtInputOf); 
        jtbInput.addSeparator();     
        jtbInput.add(labelInputName);
        jtbInput.add(txtNameInput);    
        //jtbInput.add(labelInputOf);
  		
        jtbInput.setFloatable(false); 
        
  		return jtbInput; 
  	}
  	
  	private JToolBar buildJtbContext(){
        
  		JToolBar jtbContext = new JToolBar(); 
  		
  		JButton btnContextActive = new JButton(resource.getString("ContextButton"));  
  		btnContextActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnContextActive.setForeground(Color.WHITE); 
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
        
  		OrdVariableToolBar jtbOVariable = new OrdVariableToolBar(); 
  		
        return jtbOVariable; 
   
  	}
  	
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
  	
  	private void addActionListeners(){
  		
  		btnEditMTheory.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				controller.enableMTheoryEdition(); 
  			}
  		}); 
  		
  		//ao clicar no botão btnGlobalOption, mostra-se o menu para escolha das opções
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
  		
  		//ao clicar no botão btnAddEdge setamos as variáveis booleanas e os estados dos butões
  		btnAddEdge.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_EDGE);
  			}
  		});
  		
  		btnAddMFrag.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				//netWindow.getGraphPane().setAction(GraphAction.CREATE_DOMAIN_MFRAG);
  				controller.insertDomainMFrag(); 
  			}
  		}); 
  			
  		//ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
  		btnAddContextNode.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE);
  			}
  		});
  		
  		
  		//ao clicar no botão btnAddInputNode setamos as variáveis booleanas e os estados dos butões
  		btnAddInputNode.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_INPUT_NODE);
  			}
  		});
  		
  		//ao clicar no botão btnAddResidentNode setamos as variáveis booleanas e os estados dos butões
  		btnAddResidentNode.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.CREATE_RESIDENT_NODE);
  			}
  		});
  		
        // ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
    		btnAddOrdinaryVariable.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent ae) {
    				netWindow.getGraphPane().setAction(GraphAction.CREATE_ORDINARYVARIABLE_NODE);
    			}
    		}); 
  		
  		//ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
  		btnSelectObject.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.SELECT_MANY_OBJECTS);
  			}
  		}); 	
  		
  		
  		txtNameMFrag.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameMFrag.getText().length()>0)) {
  					try {
  						String name = txtNameMFrag.getText(0,txtNameMFrag.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							controller.renameMFrag(controller.getCurrentMFrag(), name);
  							mTheoryTree.updateTree(); 
  						}  else {
  							JOptionPane.showMessageDialog(netWindow, 
  									resource.getString("nameError"), 
  									resource.getString("nameException"), 
  									JOptionPane.ERROR_MESSAGE);
  							txtNameMFrag.selectAll();
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  		});
  		
  		txtNameMTheory.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameMTheory.getText().length()>0)) {
  					try {
  						String name = txtNameMFrag.getText(0,txtNameMTheory.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							controller.setNameMTheory(name);
  							mTheoryTree.setMTheoryName(name);
  							mTheoryTree.updateUI(); 
  						}  else {
  							JOptionPane.showMessageDialog(netWindow, 
  									resource.getString("nameError"), 
  									resource.getString("nameException"), 
  									JOptionPane.ERROR_MESSAGE);
  							txtNameMTheory.selectAll();
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  		});
  		
  		// listener responsável pela atualização do texo da descrição do nó
  		txtDescription.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				//TODO fazer ... 
  			}
  		});	
  		
  		//ao clicar no botão btnGlobalOption, mostra-se o menu para escolha das opções
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

  	}  	


  	
    public void showTableEdit(){
    	
    	DomainResidentNode resident = (DomainResidentNode)controller.getResidentNodeActive(); 
    	
    	this.getGraphPanel().setTopComponent(new TableEditionPane(resident, controller)); 
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
     *  Retorna o text field da descrição do nó.
     *
     *@return    retorna a txtDescrição (<code>JTextField</code>)
     *@see       JTextField
     */
    public JTextField getTxtDescription() {
      return this.txtDescription;
    }

    /**
     *  Retorna o text field da name do nó.
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
    	formulaEdtion = new FormulaEditionPane(controller, context); 
    	jpTabSelected.add("FormulaEdtion", formulaEdtion);         
    	cardLayout.show(jpTabSelected, "FormulaEdtion"); 
    }    

    public void setEntityTreeActive(){
        cardLayout.show(jpTabSelected, "EntityTree");  
    }    
    
    public void setInputNodeActive(GenerativeInputNode input){
    	jpTabSelected.remove(inputNodePane);     	
		inputNodePane = new InputNodePane(controller, input); 
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
    	editArgumentsTab = new ArgumentEditionPane(controller, resident); 
    	jpTabSelected.add("EditArgumentsTab", editArgumentsTab);   
    	
    	
    	cardLayout.show(jpTabSelected, "EditArgumentsTab"); 
    }
    
    public void setEditArgumentsTabActive(){
    	editArgumentsTab.update();    
        cardLayout.show(jpTabSelected, "EditArgumentsTab"); 
        
    }
    
    public void setEditOVariableTabActive(){
    	
    	if(controller.getCurrentMFrag() != null){
           cardLayout.removeLayoutComponent(editOVariableTab); 
    	   editOVariableTab = new OVariableEditionPane(controller); 
           jpTabSelected.add("EditOVariableTab", editOVariableTab); 
    	   cardLayout.show(jpTabSelected, "EditOVariableTab"); 
    	}
    }
    
    public void setEntityEditionTabActive(){
    	
    	cardLayout.show(jpTabSelected, "EntityEdtionTab"); 

    }    
    
    public void setResidentNodeTabActive(DomainResidentNode resident){
    	
    	if(controller.getCurrentMFrag() != null){  		
    		jpTabSelected.remove(residentNodePane);     	
    		residentNodePane = new ResidentNodePane(controller, resident); 
        	jpTabSelected.add("ResidentNodeTab", residentNodePane);         
        	cardLayout.show(jpTabSelected, "ResidentNodeTab"); 	
    	}
    }
    
    public void setResidentNodeTabActive(){
    	cardLayout.show(jpTabSelected, "ResidentNodeTab"); 	
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
        cardLayout.show(nodeSelectedBar, ORDVARIABLE_BAR);
        jtbOVariable.setOrdVariable(ov); 
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
	

}