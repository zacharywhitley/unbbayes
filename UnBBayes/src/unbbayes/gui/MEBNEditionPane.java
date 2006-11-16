package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.mebn.EditArgumentsTab;
import unbbayes.gui.mebn.EntityTree;
import unbbayes.gui.mebn.FormulaEdtion;
import unbbayes.gui.mebn.InputInstanceOfSelection;
import unbbayes.gui.mebn.MTheoryTree;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.ResidentNode;

public class MEBNEditionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6194855055129252835L;
	
	private final NetworkWindow netWindow;

	private JPanel leftPanel; 	
	
	/*---- TabPanel and tabs ----*/
	private JToolBar jtbTabPanel; 
    private JPanel tabPanel;
    private MTheoryTree mTheoryTree; 
    private JScrollPane mTheoryTreeScroll; 
    private FormulaEdtion formulaEdtion;    
    private EntityTree entityTree;    
    private InputInstanceOfSelection inputInstanceOfSelection;    
    private JScrollPane inputInstanceOfSelectionScroll;    
    
    private EditArgumentsTab editArgumentsTab; 
	
    private JPanel descriptionPanel; 
    
    /*---- Global Options ----*/
    private GlobalOptionsDialog go;
    //private JTable table;
    private JTextField txtName;
    private JTextField txtNameResident; 
    private JTextField txtDescription;
    private JTextField txtFormula;   
    private JTextField txtNameMFrag; 
    private JTextField txtNameContext; 
    private JTextField txtNameInput;     
    private JTextField txtInputOf; 
    private JTextField txtArguments; 
    
    
    private final NetworkController controller;
    //private final JScrollPane jspTable;
    private final JSplitPane centerPanel;
    //private Node tableOwner;
    private final JLabel status;
    private final JPanel bottomPanel;

    private final JPanel topPanel;
    private final JToolBar jtbEdition;
    
    private final JPanel panelNodeSelected;
    private final CardLayout cardLayout = new CardLayout(); 
    private final JToolBar jtbEmpty; 
    private final JToolBar jtbMFrag; 
    private final JToolBar jtbResident;
    private final JToolBar jtbInput; 
    private final JToolBar jtbContext; 

    private final JLabel name;
    private final JLabel description;
    private final JLabel arguments; 
    
    private final JLabel inputOf; 
    private final JLabel formula; 
    
    private final JLabel nameMFrag; 
    private final JLabel nameInput; 
    private final JLabel nameContext; 

    private final JButton btnCompile;
    private final JButton btnAddState;
    private final JButton btnRemoveState;
    private final JButton btnAddEdge;
    private final JButton btnAddMFrag; 
    private final JButton btnAddContextNode;
    private final JButton btnAddInputNode;
    private final JButton btnAddResidentNode;
    private final JButton btnSelectObject;
    private final JButton btnGlobalOption;
    
    private final JButton btnTabOption1; 
    private final JButton btnTabOption2; 
    private final JButton btnTabOption3; 
    private final JButton btnTabOption4; 
    private final JButton btnTabOption5;  
    
    /* botoes especificos para cada tipo de no */
    
    private final JButton btnResidentActive; 
    private final JButton btnInputActive; 
    private final JButton btnMFragActive; 
    private final JButton btnContextActive; 
    
    private final JButton btnAddArgument; 
    
    private final JButton btnEditFormula; 
    
    private final JButton btnInputOf; 
    
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;

    private final IconController iconController = IconController.getInstance();

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

  	public MEBNEditionPane(NetworkWindow _netWindow,
            NetworkController _controller) {
        this.netWindow     = _netWindow;
        this.controller    = _controller;
        this.setLayout(new BorderLayout());

        //table       = new JTable();
        //jspTable    = new JScrollPane(table);
        topPanel    = new JPanel(new GridLayout(0,1));
        
        leftPanel = new JPanel(new BorderLayout()); 
        
        tabPanel = new JPanel(cardLayout); 
        descriptionPanel = new JPanel(new BorderLayout()); 
        jtbTabPanel = new JToolBar(); 
        

        panelNodeSelected = new JPanel(cardLayout); 
        jtbEdition  = new JToolBar();
        
        jtbResident = new JToolBar(); 
        jtbInput = new JToolBar(); 
        jtbContext = new JToolBar(); 
        jtbMFrag = new JToolBar(); 
        jtbEmpty = new JToolBar(); 
        
        centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        status      = new JLabel(resource.getString("statusReadyLabel"));

        //criar labels e textfields que serão usados no jtbState
        name       = new JLabel(resource.getString("nameLabel"));
        description = new JLabel(resource.getString("descriptionLabel"));
        arguments = new JLabel(resource.getString("arguments")); 
        txtName           = new JTextField(10);
        txtDescription     = new JTextField(15);
        
        txtNameResident = new JTextField(5);         
        txtNameMFrag = new JTextField(5); 
        txtNameInput = new JTextField(5); 
        txtNameContext = new JTextField(5); 
        txtInputOf = new JTextField(10); 
        txtArguments = new JTextField(10); 
    
        nameMFrag = new JLabel(resource.getString("nameLabel"));         
        nameInput = new JLabel(resource.getString("nameLabel")); 
        nameContext = new JLabel(resource.getString("nameLabel")); 
        
        inputOf = new JLabel(resource.getString("inputOf")); 
        
        formula = new JLabel(resource.getString("formula"));
        txtFormula = new JTextField(15); 
        

        
        
        //criar botões que serão usados nodeList toolbars
        btnCompile           = new JButton(iconController.getCompileIcon());
        btnAddState              = new JButton(iconController.getMoreIcon());
        btnRemoveState              = new JButton(iconController.getLessIcon());
        btnAddEdge               = new JButton(iconController.getEdgeIcon());
        btnAddContextNode = new JButton(iconController.getContextNodeIcon());
        btnAddInputNode      = new JButton(iconController.getInputNodeIcon());
        btnAddResidentNode       = new JButton(iconController.getResidentNodeIcon());
        btnAddMFrag		= new JButton(iconController.getMFragIcon()); 
        btnSelectObject            = new JButton(iconController.getSelectionIcon());
        btnGlobalOption      = new JButton(iconController.getGlobalOptionIcon());

        btnTabOption1 = new JButton(iconController.getEyeIcon());
        btnTabOption2 = new JButton(iconController.getBoxXIcon());
        btnTabOption3 = new JButton(iconController.getBoxXIcon());
        btnTabOption4 = new JButton(iconController.getBoxXIcon());   
        btnTabOption5 = new JButton(iconController.getBoxXIcon());   
        
        btnResidentActive = new JButton(iconController.getBoxResidentIcon()); 
        btnInputActive = new JButton(iconController.getBoxInputIcon());  
        btnMFragActive = new JButton(iconController.getBoxMFragIcon()); 
        btnContextActive = new JButton(iconController.getBoxContextIcon());
        
        btnAddArgument = new JButton(iconController.getGrayBoxBoxIcon());         
        btnEditFormula = new JButton(iconController.getFunctIcon()); 
        btnInputOf = new JButton(iconController.getBoxSetIcon());         
        
        
        //setar tooltip para esses botões
        btnCompile.setToolTipText(resource.getString("compileToolTip"));
        btnAddState.setToolTipText(resource.getString("moreToolTip"));
        btnRemoveState.setToolTipText(resource.getString("lessToolTip"));
        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
        
        btnAddMFrag.setToolTipText(resource.getString("mFragInsertToolTip")); 
        btnAddContextNode.setToolTipText(resource.getString("contextNodeInsertToolTip"));
        btnAddInputNode.setToolTipText(resource.getString("inputNodeInsertToolTip"));
        btnAddResidentNode.setToolTipText(resource.getString("residentNodeInsertToolTip"));;
       
        btnInputActive.setToolTipText(resource.getString("inputActiveToolTip"));  
        btnMFragActive.setToolTipText(resource.getString("mFragActiveToolTip")); 
        btnContextActive.setToolTipText(resource.getString("contextActiveToolTip")); 
        btnResidentActive.setToolTipText(resource.getString("residentActiveToolTip"));         
        btnAddArgument.setToolTipText(resource.getString("addArgumentToolTip"));        
        btnEditFormula.setToolTipText(resource.getString("editFormulaToolTip"));        
        
        btnSelectObject.setToolTipText(resource.getString("mFragInsertToolTip")); 
        btnGlobalOption.setToolTipText(resource.getString("mFragInsertToolTip")); 

        addActionListeners(); 
        
        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel

        jtbEdition.add(btnAddMFrag); 
        
        jtbEdition.addSeparator(); 
        
        jtbEdition.add(btnAddContextNode);
        jtbEdition.add(btnAddInputNode);
        jtbEdition.add(btnAddResidentNode);
        jtbEdition.add(btnAddEdge);
        jtbEdition.add(btnSelectObject);
        
        //jtbEdition.add(btnCompile);
        //jtbEdition.addSeparator();
        //jtbEdition.add(btnGlobalOption);

        topPanel.add(jtbEdition);

        
        /*---- jtbMFrag ----*/
        
        jtbMFrag.add(btnMFragActive); 
        jtbMFrag.addSeparator();         
        jtbMFrag.add(nameMFrag);
        jtbMFrag.add(txtNameMFrag);        
        
        
        /*---- jtbResident ----*/
        jtbResident.add(btnResidentActive); 
        jtbResident.addSeparator();         
        jtbResident.add(name);
        jtbResident.add(txtNameResident);
        jtbResident.addSeparator();
        jtbResident.addSeparator();
        jtbResident.add(btnAddState);
        jtbResident.addSeparator(); 
        jtbResident.add(btnAddArgument);
        jtbResident.add(arguments); 
        txtArguments.setEditable(false); 
        jtbResident.add(txtArguments); 

        /*----- jtbInput ----*/
        jtbInput.add(btnInputActive); 
        jtbInput.addSeparator();
        jtbInput.add(nameInput);
        txtNameInput.setEditable(false); 
        jtbInput.add(txtNameInput);
        jtbInput.addSeparator(); 
        jtbInput.add(btnInputOf);         
        jtbInput.add(inputOf);
        txtInputOf.setEditable(false); 
        jtbInput.add(txtInputOf); 
        
        /*----- jtbContext ----*/
        jtbContext.add(btnContextActive); 
        jtbContext.addSeparator();         
        jtbContext.add(nameContext);
        txtNameContext.setEditable(false); 
        jtbContext.add(txtNameContext);
        jtbContext.addSeparator(); 
        jtbContext.add(btnEditFormula); 
        jtbContext.add(formula); 
        txtFormula.setEditable(false); 
        jtbContext.add(txtFormula);
       
        
        /*---- jtbEmpty ----*/
        JTextField txtIsEmpty = new JTextField(resource.getString("whithotMFragActive")); 
        txtIsEmpty.setEditable(false); 
        jtbEmpty.addSeparator(); 
        jtbEmpty.add(txtIsEmpty);
        
        /*---- Add card panels in the layout ----*/
        
        panelNodeSelected.add("ResidentCard", jtbResident);
        panelNodeSelected.add("ContextCard", jtbContext); 
        panelNodeSelected.add("InputCard", jtbInput); 
        panelNodeSelected.add("MFragCard", jtbMFrag); 
        panelNodeSelected.add("EmptyCard", jtbEmpty); 
        
        cardLayout.show(panelNodeSelected, "EmptyCard"); 
        
        topPanel.add(panelNodeSelected); 

        //setar o preferred size do jspTable para ser usado pelo SplitPanel
        //jspTable.setPreferredSize(new Dimension(150,50));

        //setar o auto resize off para que a tabela fique do tamanho ideal
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        /* 
         * Esperar Tabela ficar pronta... 
         *
         *
        //adicionar tela da tabela(JScrollPane) da tabela de estados para o painel do centro
        centerPanel.setTopComponent(jspTable);

        //setar o tamanho do divisor entre o jspGraph(vem do NetWindow) e jspTable
        centerPanel.setDividerSize(3);

        //setar os tamanho de cada jsp(tabela e graph) para os seus PreferredSizes
        centerPanel.resetToPreferredSizes();
        */

        bottomPanel.add(status);

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
       
        /*----------------- Icones do Tab Panel ------------*/
        
        jtbTabPanel.add(btnTabOption1);
        jtbTabPanel.add(btnTabOption2); 
        jtbTabPanel.add(btnTabOption3); 
        jtbTabPanel.add(btnTabOption4); 
        jtbTabPanel.add(btnTabOption5); 
        jtbTabPanel.setBackground(Color.black);
        jtbTabPanel.setFloatable(false);
        
        /*---------------- Tab panel ----------------------*/
        
        mTheoryTree = new MTheoryTree(controller); 
        mTheoryTreeScroll = new JScrollPane(mTheoryTree); 
        tabPanel.add("MTheoryTree", mTheoryTreeScroll);
        
        formulaEdtion = new FormulaEdtion(); 
        tabPanel.add("FormulaEdtion", formulaEdtion); 
        
        entityTree = new EntityTree(); 
        tabPanel.add("EntityTree", entityTree); 
        
        inputInstanceOfSelection = new InputInstanceOfSelection(controller); 
        inputInstanceOfSelectionScroll = new JScrollPane(inputInstanceOfSelection); 
        tabPanel.add("InputInstance", inputInstanceOfSelectionScroll); 
        
        editArgumentsTab = new EditArgumentsTab(); 
        tabPanel.add("EditArgumentsTab", editArgumentsTab); 
        
        cardLayout.show(tabPanel, "MTheoryTree");  
        
        /*------------------ Description panel ---------------*/
        
        descriptionPanel.add("North", description);
        
        JTextArea textArea = new JTextArea(5, 10);
        JScrollPane scrollPane = 
            new JScrollPane(textArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        textArea.setEditable(true);
        
        descriptionPanel.add("South", scrollPane); 
        
        /*------------------- Left panel ---------------------*/
        
        leftPanel.add("North", jtbTabPanel);
        leftPanel.add("Center", tabPanel); 
        leftPanel.add("South", descriptionPanel); 
        
        this.add(leftPanel, BorderLayout.WEST);
        
        setVisible(true);
    }
  	
  	private void addActionListeners(){
  		
  		//ao clicar no botão btnGlobalOption, mostra-se o menu para escolha das opções
  		btnGlobalOption.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setCursor(new Cursor(Cursor.WAIT_CURSOR));
  				go = new GlobalOptionsDialog(netWindow.getGraphPane(), controller);
  				go.setVisible(true);
  				netWindow.getGraphPane().update();
  				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  			}
  		});
  		
  		//ao clicar no botão btnCompile, chama-se o método de compilação da rede e
  		//atualiza os toolbars
  		btnCompile.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				if (! controller.compileNetwork()) {
  					return;
  				}
  				netWindow.changeToPNCompilationPane();
  			}
  		});
  		
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
  		
  		
  		//ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
  		btnSelectObject.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				netWindow.getGraphPane().setAction(GraphAction.SELECT_MANY_OBJECTS);
  			}
  		});
  		
  		btnInputOf.addActionListener(new ActionListener(){
  			public void actionPerformed(ActionEvent ae){
  				setInputInstanceOfActive(); 
  			}
  		}); 
  		
  		// listener responsável pela atualização do texto da name do nó
  		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				Object selected = netWindow.getGraphPane().getSelected();
  				if (selected instanceof Node) {
  					Node nodeAux = (Node)selected;
  					if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  						try {
  							String name = txtName.getText(0,txtName.getText().length());
  							matcher = wordPattern.matcher(name);
  							if (matcher.matches()) {
  								nodeAux.setName(name);
  								repaint();
  							}  else {
  								JOptionPane.showMessageDialog(netWindow, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  								txtName.selectAll();
  							}
  						}
  						catch (javax.swing.text.BadLocationException ble) {
  							System.out.println(ble.getMessage());
  						}
  					}
  				}
  			}
  		});
  		
  		txtNameResident.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				Object selected = netWindow.getGraphPane().getSelected();
  				if (selected instanceof Node) {
  					Node nodeAux = (Node)selected;
  					if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameResident.getText().length()>0)) {
  						try {
  							String name = txtNameResident.getText(0,txtNameResident.getText().length());
  							matcher = wordPattern.matcher(name);
  							if (matcher.matches()) {
  								nodeAux.setName(name);
  								repaint();
  							}  else {
  								JOptionPane.showMessageDialog(netWindow, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  								txtNameResident.selectAll();
  							}
  						}
  						catch (javax.swing.text.BadLocationException ble) {
  							System.out.println(ble.getMessage());
  						}
  					}
  				}
  			}
  		});  		
  		
  		txtNameMFrag.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameMFrag.getText().length()>0)) {
  					try {
  						String name = txtNameMFrag.getText(0,txtNameMFrag.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							controller.getMebnController().getCurrentMFrag().setName(name);
  							mTheoryTree.updateTree(); 
  						}  else {
  							JOptionPane.showMessageDialog(netWindow, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  							txtNameMFrag.selectAll();
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  		});
  		
  		//TODO Verificação de atualização de nome para MFrag. 
  		
  		
  		// listener responsável pela atualização do texo da descrição do nó
  		txtDescription.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				Object selected = netWindow.getGraphPane().getSelected();
  				if (selected instanceof Node)
  				{
  					Node nodeAux = (Node)selected;
  					if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtDescription.getText().length()>0)) {
  						try {
  							String name = txtDescription.getText(0,txtDescription.getText().length());
  							matcher = wordPattern.matcher(name);
  							if (matcher.matches()) {
  								nodeAux.setDescription(name);
  								repaint();
  							} else {
  								JOptionPane.showMessageDialog(netWindow, resource.getString("descriptionError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  								txtDescription.selectAll();
  							}
  						}
  						catch (javax.swing.text.BadLocationException ble) {
  							System.out.println(ble.getMessage());
  						}
  					}
  				}
  			}
  		});
  		
  		//ao clicar no botão btnRemoveState, chama-se o metodo removerEstado do controller
  		//para que esse remova um estado do nó
  		btnRemoveState.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				if (netWindow.getGraphPane().getSelected() instanceof Node) {
  					controller.removeState((Node)netWindow.getGraphPane().getSelected());
  				}
  			}
  		});
  		
  		//ao clicar no botão btnRemoveState, chama-se o metodo inserirEstado do controller
  		//para que esse insira um novo estado no nó
  		btnAddState.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				if (netWindow.getGraphPane().getSelected() instanceof Node) {
  					controller.insertState((Node)netWindow.getGraphPane().getSelected());
  				}
  			}
  		});
  		
  		btnAddArgument.addActionListener(new ActionListener(){
  			
  			public void actionPerformed(ActionEvent ae){
  				setEditArgumentsTabActive(); 
  			}
  			
  		});
  		
  		btnEditFormula.addActionListener(new ActionListener(){
  			
  			public void actionPerformed(ActionEvent ae){
  				setFormulaEdtionActive(); 
  			}
  			
  		});  		
  		
  		//ao clicar no botão btnGlobalOption, mostra-se o menu para escolha das opções
  		btnTabOption1.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setMTheoryTreeActive(); 
  			}
  		});  		
  	}  	

    /**
     *  Retorna a tabela de probabilidades.
     *
     *@return    retorna a table (<code>JTable</code>)
     *@see       JTable
     */
  	
    /*
  	public JTable getTable() {
        return table;
    }
    */

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
     *  Substitui a tabela de probabilidades existente pela desejada.
     *
     *@parm      table a nova tabela (<code>JTable</code>) desejada.
     *@see       JTable
     */
    /*
    public void setTable(JTable table) {
        this.table = table;
        jspTable.setViewportView(table);
    }

    public Node getTableOwner() {
        return tableOwner;
    }

    public void setTableOwner(Node node) {
        this.tableOwner = node;
    }
    */

    /**
     * Seta o status exibido na barra de status.
     *
     * @param status mensagem de status.
     */
    public void setStatus(String status) {
        this.status.setText(status);
    }

    /**
     * Seta qual dos paineis de nos é o visivel, de forma a este
     * corresponder ao nodo selecionado. 
     */
    
    public void setPainelNodeVisible(){
    	
    }
    
    /**
     *  Retorna o painel do centro onde fica o graph e a table.
     *
     *@return    retorna o centerPanel (<code>JSplitPane</code>)
     *@see       JSplitPane
     */
    public JSplitPane getCenterPanel() {
      return this.centerPanel;
    }

    public JButton getBtnAddEdge() {
        return this.btnAddEdge;
    }

    public JButton getBtnCompile() {
        return this.btnCompile;
    }

    public JButton getBtnAddInputNode() {
        return this.btnAddInputNode;
    }

    public JLabel getDescription() {
        return this.description;
    }

    public JButton getBtnGlobalOption() {
        return this.btnGlobalOption;
    }

    public JButton getBtnRemoveState() {
        return this.btnRemoveState;
    }

    public JButton getBtnAddState() {
        return this.btnAddState;
    }

    public JButton getBtnAddContextNode() {
        return this.btnAddContextNode;
    }

    public JButton getBtnSelectObject() {
        return this.btnSelectObject;
    }

    public JLabel getname() {
        return this.name;
    }

    public JButton getBtnAddResidentNode() {
        return this.btnAddResidentNode;
    }
    
    
    
    public MTheoryTree getMTheoryTree(){
    	return mTheoryTree; 
    }
    
    public InputInstanceOfSelection getInputInstanceOfSelection(){
    	return inputInstanceOfSelection; 
    }
    
    public EditArgumentsTab getEditArgumentsTab(){
         return editArgumentsTab; 	
    }
    
    /* TabPanel */
    
    public void setMTheoryTreeActive(){
        cardLayout.show(tabPanel, "MTheoryTree");  
        mTheoryTree.updateTree(); 
    }
    
    public void setFormulaEdtionActive(){
        cardLayout.show(tabPanel, "FormulaEdtion");  
    }
    
    public void setFormulaEdtionActive(ContextNode context){
    	   
        tabPanel.remove(formulaEdtion);     	
    	formulaEdtion = new FormulaEdtion(controller, context); 
    	tabPanel.add("FormulaEdtion", formulaEdtion);         
    	cardLayout.show(tabPanel, "FormulaEdtion"); 
    }    

    public void setEntityTreeActive(){
        cardLayout.show(tabPanel, "EntityTree");  
    }    
    
    public void setInputInstanceOfActive(){
        cardLayout.show(tabPanel, "InputInstance");  
    }
    
    public void setArgumentTabActive(){
        cardLayout.show(tabPanel, "ArgumentTab"); 	
    }
    
    public void setEditArgumentsTabActive(ResidentNode resident){
   
        tabPanel.remove(editArgumentsTab);     	
    	editArgumentsTab = new EditArgumentsTab(controller, resident); 
    	tabPanel.add("EditArgumentsTab", editArgumentsTab);         
    	cardLayout.show(tabPanel, "EditArgumentsTab"); 
    }
    
    public void setEditArgumentsTabActive(){
    	   
        cardLayout.show(tabPanel, "EditArgumentsTab"); 
        
    }    
    
    /* Panel Node Selected */

    public void setResidentCardActive(){
        cardLayout.show(panelNodeSelected, "ResidentCard");  
    }    
    
    public void setContextCardActive(){
        cardLayout.show(panelNodeSelected, "ContextCard");  
    }  
    
    public void setInputCardActive(){
        cardLayout.show(panelNodeSelected, "InputCard");  
    } 
    
    public void setMFragCardActive(){
        cardLayout.show(panelNodeSelected, "MFragCard");  
    } 
    
    public void setEmptyCardActive(){
        cardLayout.show(panelNodeSelected, "EmptyCard");  
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

}
