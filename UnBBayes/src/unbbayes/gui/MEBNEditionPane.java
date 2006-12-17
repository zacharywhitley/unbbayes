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
import unbbayes.gui.mebn.ArgumentEditionPane;
import unbbayes.gui.mebn.EntityTree;
import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.InputInstanceOfTree;
import unbbayes.gui.mebn.MTheoryTree;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.PossibleValuesEditionPane;
import unbbayes.gui.mebn.TableEdition;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.ResidentNode;

public class MEBNEditionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6194855055129252835L;
	
	private final NetworkWindow netWindow;

	private JPanel leftPanel; 	
	
	/*---- TabPanel and tabs ----*/
	private JToolBar jtbTabSelection; 
    private JPanel jpTabSelected;
    
    private MTheoryTree mTheoryTree; 
    private JScrollPane mTheoryTreeScroll; 
    private FormulaEditionPane formulaEdtion;    
    private EntityTree entityTree;    
    private InputInstanceOfTree inputInstanceOfSelection;    
    private JScrollPane inputInstanceOfSelectionScroll;    
    private OVariableEditionPane editOVariableTab; 
    private PossibleValuesEditionPane possibleValuesEditTab; 
    private ArgumentEditionPane editArgumentsTab; 
    
    private TableEdition tableEdit; 
    
    private JPanel jpDescription; 
    
    /*---- Global Options ----*/
    private GlobalOptionsDialog go;
    private JTextField txtNameResident; 
    private JTextField txtDescription;
    private JTextField txtFormula;   
    private JTextField txtNameMFrag; 
    private JTextField txtNameContext; 
    private JTextField txtNameInput;     
    private JTextField txtInputOf; 
    private JTextField txtArguments; 
    
    
    private final NetworkController controller;
    private final JSplitPane centerPanel;
    private final JLabel status;
    private final JPanel bottomPanel;

    private final JPanel topPanel;
    private final JToolBar jtbEdition;
    
    private final JPanel jpNodeSelectedOptions;
    private final CardLayout cardLayout = new CardLayout(); 
    private final JToolBar jtbEmpty; 
    private final JToolBar jtbMFrag; 
    private final JToolBar jtbResident;
    private final JToolBar jtbInput; 
    private final JToolBar jtbContext; 

    private final JLabel labelDescription;
    
    private final JLabel labelResidentName;
    private final JLabel labelArguments; 
 
    private final JLabel labelInputName; 
    private final JLabel labelInputOf; 
    
    private final JLabel labelContextName;     
    private final JLabel labelFormula; 
    
    private final JLabel labelMFragName; 

    private final JButton btnEditStates;
    private final JButton btnEditTable;
    
    private final JButton btnAddMFrag; 
    private final JButton btnAddContextNode;
    private final JButton btnAddInputNode;
    private final JButton btnAddResidentNode;
    private final JButton btnAddEdge;
    
    private final JButton btnSelectObject;
    private final JButton btnGlobalOption;
    
    private final JButton btnTabOption1; 
    private final JButton btnTabOption2; 
    private final JButton btnTabOption3; 
    private final JButton btnTabOption4; 
    private final JButton btnTabOption5;  
    private final JButton btnTabOption6;  
    
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
        
        jpTabSelected = new JPanel(cardLayout); 
        jpDescription = new JPanel(new BorderLayout()); 
        jtbTabSelection = new JToolBar(); 
        

        jpNodeSelectedOptions = new JPanel(cardLayout); 
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
        labelResidentName       = new JLabel(resource.getString("nameLabel"));
        labelDescription = new JLabel(resource.getString("descriptionLabel"));
        labelArguments = new JLabel(resource.getString("arguments")); 
        txtDescription     = new JTextField(15);
        
        txtNameResident = new JTextField(5);         
        txtNameMFrag = new JTextField(5); 
        txtNameInput = new JTextField(5); 
        txtNameContext = new JTextField(5); 
        txtInputOf = new JTextField(10); 
        txtArguments = new JTextField(10); 
    
        labelMFragName = new JLabel(resource.getString("nameLabel"));         
        labelInputName = new JLabel(resource.getString("nameLabel")); 
        labelContextName = new JLabel(resource.getString("nameLabel")); 
        
        labelInputOf = new JLabel(resource.getString("inputOf")); 
        
        labelFormula = new JLabel(resource.getString("formula"));
        txtFormula = new JTextField(15); 
        
        //criar botões que serão usados nodeList toolbars
        btnEditStates              = new JButton(iconController.getMoreIcon());
        btnEditTable              = new JButton(iconController.getEditIcon());
        btnAddEdge               = new JButton(iconController.getEdgeIcon());
        btnAddContextNode = new JButton(iconController.getContextNodeIcon());
        btnAddInputNode      = new JButton(iconController.getInputNodeIcon());
        btnAddResidentNode       = new JButton(iconController.getResidentNodeIcon());
        btnAddMFrag		= new JButton(iconController.getMFragIcon()); 
        btnSelectObject            = new JButton(iconController.getSelectionIcon());
        btnGlobalOption      = new JButton(iconController.getGlobalOptionIcon());

        btnTabOption1 = new JButton(iconController.getEyeIcon());
        btnTabOption2 = new JButton(iconController.getOVariableNodeIcon()); 
        btnTabOption3 = new JButton(iconController.getEntityNodeIcon()); 
        btnTabOption4 = new JButton(iconController.getGrayBorderBoxIcon());   
        btnTabOption5 = new JButton(iconController.getFunctIcon());   
        btnTabOption6 = new JButton(iconController.getBoxSetIcon());   
         
        btnResidentActive = new JButton(iconController.getBoxResidentIcon()); 
        btnInputActive = new JButton(iconController.getBoxInputIcon());  
        btnMFragActive = new JButton(iconController.getBoxMFragIcon()); 
        btnContextActive = new JButton(iconController.getBoxContextIcon());
        
        btnAddArgument = new JButton(iconController.getGrayBoxBoxIcon());         
        btnEditFormula = new JButton(iconController.getFunctIcon()); 
        btnInputOf = new JButton(iconController.getBoxSetIcon());         
        
        //setar tooltip para esses botões
        btnEditStates.setToolTipText(resource.getString("moreToolTip"));
        btnEditTable.setToolTipText(resource.getString("lessToolTip"));
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

        topPanel.add(jtbEdition);

        
        /*---- jtbMFrag ----*/
        
        jtbMFrag.add(btnMFragActive); 
        jtbMFrag.addSeparator();         
        jtbMFrag.add(labelMFragName);
        jtbMFrag.add(txtNameMFrag);        
        
        
        /*---- jtbResident ----*/
        jtbResident.add(btnResidentActive); 
        jtbResident.addSeparator();         
        jtbResident.add(labelResidentName);
        jtbResident.add(txtNameResident);
        jtbResident.addSeparator();
        jtbResident.addSeparator();
        jtbResident.add(btnEditStates);
        jtbResident.add(btnEditTable); 
        jtbResident.add(btnAddArgument);
        jtbResident.addSeparator(); 
        jtbResident.add(labelArguments); 
        txtArguments.setEditable(false); 
        jtbResident.add(txtArguments); 

        /*----- jtbInput ----*/
        jtbInput.add(btnInputActive); 
        jtbInput.addSeparator();
        jtbInput.add(labelInputName);
        txtNameInput.setEditable(false); 
        jtbInput.add(txtNameInput);
        jtbInput.addSeparator(); 
        jtbInput.add(btnInputOf);         
        jtbInput.add(labelInputOf);
        txtInputOf.setEditable(false); 
        jtbInput.add(txtInputOf); 
        
        /*----- jtbContext ----*/
        jtbContext.add(btnContextActive); 
        jtbContext.addSeparator();         
        jtbContext.add(labelContextName);
        txtNameContext.setEditable(false); 
        jtbContext.add(txtNameContext);
        jtbContext.addSeparator(); 
        jtbContext.add(btnEditFormula); 
        jtbContext.add(labelFormula); 
        txtFormula.setEditable(false); 
        jtbContext.add(txtFormula);
       
        
        /*---- jtbEmpty ----*/
        JTextField txtIsEmpty = new JTextField(resource.getString("whithotMFragActive")); 
        txtIsEmpty.setEditable(false); 
        jtbEmpty.addSeparator(); 
        jtbEmpty.add(txtIsEmpty);
        
        /*---- Add card panels in the layout ----*/
        
        jpNodeSelectedOptions.add("ResidentCard", jtbResident);
        jpNodeSelectedOptions.add("ContextCard", jtbContext); 
        jpNodeSelectedOptions.add("InputCard", jtbInput); 
        jpNodeSelectedOptions.add("MFragCard", jtbMFrag); 
        jpNodeSelectedOptions.add("EmptyCard", jtbEmpty); 
        
        cardLayout.show(jpNodeSelectedOptions, "EmptyCard"); 
        
        topPanel.add(jpNodeSelectedOptions); 

        bottomPanel.add(status);

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
       
        /*----------------- Icones do Tab Panel ------------*/
        
        jtbTabSelection.add(btnTabOption1);
        jtbTabSelection.add(btnTabOption2); 
        jtbTabSelection.add(btnTabOption3); 
        jtbTabSelection.add(btnTabOption4); 
        jtbTabSelection.add(btnTabOption5); 
        jtbTabSelection.add(btnTabOption6);         
        jtbTabSelection.setBackground(Color.black);
        jtbTabSelection.setFloatable(false);
        
        /*---------------- Tab panel ----------------------*/
        
        mTheoryTree = new MTheoryTree(controller); 
        mTheoryTreeScroll = new JScrollPane(mTheoryTree); 
        jpTabSelected.add("MTheoryTree", mTheoryTreeScroll);
        
        formulaEdtion = new FormulaEditionPane(); 
        jpTabSelected.add("FormulaEdtion", formulaEdtion); 
        
        entityTree = new EntityTree(); 
        jpTabSelected.add("EntityTree", entityTree); 
        
        inputInstanceOfSelection = new InputInstanceOfTree(controller); 
        inputInstanceOfSelectionScroll = new JScrollPane(inputInstanceOfSelection); 
        jpTabSelected.add("InputInstance", inputInstanceOfSelectionScroll); 
        
        editArgumentsTab = new ArgumentEditionPane(); 
        jpTabSelected.add("EditArgumentsTab", editArgumentsTab); 
        
        possibleValuesEditTab = new PossibleValuesEditionPane(); 
        jpTabSelected.add("PossibleValuesEditTab", possibleValuesEditTab); 
                
        
        cardLayout.show(jpTabSelected, "MTheoryTree");  
        
        /*------------------ Description panel ---------------*/
        
        jpDescription.add("North", labelDescription);
        
        JTextArea textArea = new JTextArea(5, 10);
        JScrollPane scrollPane = 
            new JScrollPane(textArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        textArea.setEditable(true);
        
        jpDescription.add("South", scrollPane); 
        
        /*------------------- Left panel ---------------------*/
        
        leftPanel.add("North", jtbTabSelection);
        leftPanel.add("Center", jpTabSelected); 
        leftPanel.add("South", jpDescription); 
        
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
  		
  		
  		txtNameResident.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				ResidentNode nodeAux = controller.getMebnController().getResidentNodeActive();
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
  		
  		// listener responsável pela atualização do texo da descrição do nó
  		txtDescription.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				//TODO fazer ... 
  			}
  		});
  		
  		//ao clicar no botão btnRemoveState, chama-se o metodo removerEstado do controller
  		//para que esse remova um estado do nó
  		btnEditTable.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				if (netWindow.getGraphPane().getSelected() instanceof Node) {
  					controller.removeState((Node)netWindow.getGraphPane().getSelected());
  				}
  			}
  		});
  		
  		//ao clicar no botão btnRemoveState, chama-se o metodo inserirEstado do controller
  		//para que esse insira um novo estado no nó
  		btnEditStates.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setPossibleValuesEditTabActive(); 
  			}
  		});
  		
  		btnEditTable.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				showTableEdit(); 
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
  		
  		btnTabOption2.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setEditOVariableTabActive(); 
  			}
  		});  	
  		
  		btnTabOption2.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setEditOVariableTabActive(); 
  			}
  		});  
  		
  		btnTabOption3.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				JOptionPane.showMessageDialog(netWindow, "Editor de entidades ainda não implementado...", "..." , JOptionPane.WARNING_MESSAGE);
  			}
  		});  
  		
  		btnTabOption4.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				 if (controller.getMebnController().getResidentNodeActive() != null){
  			         setEditArgumentsTabActive(controller.getMebnController().getResidentNodeActive()); 
  		         }
  				 else{
  					JOptionPane.showMessageDialog(netWindow, "Não há nó resident sendo editado...", "..." , JOptionPane.WARNING_MESSAGE);	 
  				 }
  			}
  		});  
  		
  		btnTabOption5.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
 				 if (controller.getMebnController().getContextNodeActive() != null){
  			         setFormulaEdtionActive(controller.getMebnController().getContextNodeActive()); 
  		         }
  				 else{
  					JOptionPane.showMessageDialog(netWindow, "Não há nó de contexto sendo editado...", "..." , JOptionPane.WARNING_MESSAGE);	 
  				 }
  			}
  		});
  		
  		btnTabOption6.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
 				 if (controller.getMebnController().getInputNodeActive() != null){
  			         setInputInstanceOfActive(); 
  		         }
  				 else{
  					JOptionPane.showMessageDialog(netWindow, "Não há nó de input sendo editado...", "..." , JOptionPane.WARNING_MESSAGE);	 
  				 }
  			}
  		});  
  	}  	

    /**
     * Mostra a tela de edição de tabela. 
     */   
    public void showTableEdit(){
    	DomainResidentNode resident = (DomainResidentNode)controller.getMebnController().getResidentNodeActive(); 
    	tableEdit = new TableEdition(resident); 
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

    public JButton getBtnAddInputNode() {
        return this.btnAddInputNode;
    }

    public JLabel getDescription() {
        return this.labelDescription;
    }

    public JButton getBtnGlobalOption() {
        return this.btnGlobalOption;
    }

    public JButton getBtnRemoveState() {
        return this.btnEditTable;
    }

    public JButton getBtnAddState() {
        return this.btnEditStates;
    }

    public JButton getBtnAddContextNode() {
        return this.btnAddContextNode;
    }

    public JButton getBtnSelectObject() {
        return this.btnSelectObject;
    }

    public JLabel getname() {
        return this.labelResidentName;
    }

    public JButton getBtnAddResidentNode() {
        return this.btnAddResidentNode;
    }
    
    
    
    public MTheoryTree getMTheoryTree(){
    	return mTheoryTree; 
    }
    
    public InputInstanceOfTree getInputInstanceOfSelection(){
    	return inputInstanceOfSelection; 
    }
    
    public ArgumentEditionPane getEditArgumentsTab(){
         return editArgumentsTab; 	
    }
    
    public OVariableEditionPane getEditOVariableTab(){
    	return editOVariableTab; 
    }
    
    public PossibleValuesEditionPane getPossibleValuesEditTab(){
    	return possibleValuesEditTab; 
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
    
    public void setInputInstanceOfActive(){
    	inputInstanceOfSelection.updateTree(); 
        cardLayout.show(jpTabSelected, "InputInstance");  
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
    	
    	if(controller.getMebnController().getCurrentMFrag() != null){
           editOVariableTab = new OVariableEditionPane(controller); 
           jpTabSelected.add("EditOVariableTab", editOVariableTab); 
    	   cardLayout.show(jpTabSelected, "EditOVariableTab"); 
    	}
    }
    
    public void setPossibleValuesEditTabActive(DomainResidentNode resident){
    	
    	if(controller.getMebnController().getCurrentMFrag() != null){
            
    		jpTabSelected.remove(possibleValuesEditTab);     	
    		possibleValuesEditTab = new PossibleValuesEditionPane(controller, resident); 
        	jpTabSelected.add("PossibleValuesEditTab", possibleValuesEditTab);         
        	cardLayout.show(jpTabSelected, "PossibleValuesEditTab"); 	
    	}
    }
    
    public void setPossibleValuesEditTabActive(){
    	
        cardLayout.show(jpTabSelected, "PossibleValuesEditTab"); 
    	
    	
    }
    
    /* Panel Node Selected */

    public void setResidentCardActive(){
        cardLayout.show(jpNodeSelectedOptions, "ResidentCard");  
    }    
    
    public void setContextCardActive(){
        cardLayout.show(jpNodeSelectedOptions, "ContextCard");  
    }  
    
    public void setInputCardActive(){
        cardLayout.show(jpNodeSelectedOptions, "InputCard");  
    } 
    
    public void setMFragCardActive(){
        cardLayout.show(jpNodeSelectedOptions, "MFragCard");  
    } 
    
    public void setEmptyCardActive(){
        cardLayout.show(jpNodeSelectedOptions, "EmptyCard");  
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
