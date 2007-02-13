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
import unbbayes.controller.NetworkController;
import unbbayes.gui.mebn.ArgumentEditionPane;
import unbbayes.gui.mebn.EntityEditionPane;
import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.InputNodePane;
import unbbayes.gui.mebn.MTheoryTree;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.ResidentNodePane;
import unbbayes.gui.mebn.TableEdition;
import unbbayes.gui.mebn.ToolKitForGuiMebn;
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

	private JPanel tabsPanel; 	
	private JSplitPane centerPanel; 
	
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
    private JTextField txtNameResident; 
    private JTextField txtDescription;
    private JTextField txtFormula;   
    private JTextField txtNameMFrag; 
    private JTextField txtNameContext; 
    private JTextField txtNameInput;     
    private JTextField txtInputOf; 
    private JTextField txtArguments; 
    
    
    private final NetworkController controller;
    private final JSplitPane graphPanel;
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
    
    private final JLabel labelMFragName; 

    private final JButton btnAddMFrag; 
    private final JButton btnAddContextNode;
    private final JButton btnAddInputNode;
    private final JButton btnAddResidentNode;
    private final JButton btnAddEdge;
    
    private final JButton btnSelectObject;
    private final JButton btnGlobalOption;
    
    private final JButton btnTabOptionTree; 
    private final JButton btnTabOptionOVariable; 
    private final JButton btnTabOptionEntity; 
    
    private TableEdition tableEdit; 
    
    /* botoes especificos para cada tipo de no */

    private final JButton btnMFragActive; 
    
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
        
        tabsPanel = new JPanel(new BorderLayout()); 
        
        jpTabSelected = new JPanel(cardLayout); 
        jpDescription = new JPanel(new BorderLayout()); 
        jtbTabSelection = new JToolBar(); 

        jpNodeSelectedOptions = new JPanel(cardLayout); 
        jtbEdition  = new JToolBar();
        
        jtbMFrag = new JToolBar(); 
        jtbEmpty = new JToolBar(); 
        
        graphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        status      = new JLabel(resource.getString("statusReadyLabel"));

        //criar labels e textfields que serão usados no jtbState
        txtDescription     = new JTextField(15);
        
        txtNameResident = new JTextField(5);         
        txtNameMFrag = new JTextField(5); 
        txtNameContext = new JTextField(5); 
        txtArguments = new JTextField(10); 
    
        labelMFragName = new JLabel(resource.getString("nameLabel"));         

        txtFormula = new JTextField(15); 
        
        //criar botões que serão usados nodeList toolbars
        btnAddEdge               = new JButton(iconController.getEdgeIcon());
        btnAddContextNode = new JButton(iconController.getContextNodeIcon());
        btnAddInputNode      = new JButton(iconController.getInputNodeIcon());
        btnAddResidentNode       = new JButton(iconController.getResidentNodeIcon());
        btnAddMFrag		= new JButton(iconController.getMFragIcon()); 
        btnSelectObject            = new JButton(iconController.getSelectionIcon());
        btnGlobalOption      = new JButton(iconController.getGlobalOptionIcon());

        btnTabOptionTree = new JButton(iconController.getEyeIcon());
        btnTabOptionOVariable = new JButton(iconController.getOVariableNodeIcon()); 
        btnTabOptionEntity = new JButton(iconController.getEntityNodeIcon()); 

        btnMFragActive = new JButton(iconController.getBoxMFragIcon()); 

        //setar tooltip para esses botões
        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
        
        btnAddMFrag.setToolTipText(resource.getString("mFragInsertToolTip")); 
        btnAddContextNode.setToolTipText(resource.getString("contextNodeInsertToolTip"));
        btnAddInputNode.setToolTipText(resource.getString("inputNodeInsertToolTip"));
        btnAddResidentNode.setToolTipText(resource.getString("residentNodeInsertToolTip"));;
       
        btnMFragActive.setToolTipText(resource.getString("mFragActiveToolTip")); 
 
        btnSelectObject.setToolTipText(resource.getString("mFragInsertToolTip")); 
        btnGlobalOption.setToolTipText(resource.getString("mFragInsertToolTip")); 

        addActionListeners(); 
        
        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel

        jtbEdition.add(btnAddMFrag); 
        
        jtbEdition.addSeparator(); 

        jtbEdition.add(btnAddResidentNode);
        jtbEdition.add(btnAddInputNode);
        jtbEdition.add(btnAddContextNode);
        jtbEdition.add(btnAddEdge);
        jtbEdition.add(btnSelectObject);

        topPanel.add(jtbEdition);
        
        
        /*---- jtbMFrag ----*/
        
        jtbMFrag.add(btnMFragActive); 
        jtbMFrag.addSeparator();         
        jtbMFrag.add(labelMFragName);
        jtbMFrag.add(txtNameMFrag);        
        
        jtbResident = buildJtbResident(); 
        jtbInput = buildJtbInput(); 
        jtbContext = buildJtbContext(); 

        
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
       
        /*----------------- Icones do Tab Panel ------------*/
        jtbTabSelection.setBackground(ToolKitForGuiMebn.getBorderColor()); 
        jtbTabSelection.setLayout(new GridLayout(1,3)); 
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.add(btnTabOptionOVariable); 
        jtbTabSelection.add(btnTabOptionEntity);      
        jtbTabSelection.setFloatable(false);
        
        /*---------------- Tab panel ----------------------*/
        
        mTheoryTree = new MTheoryTree(controller); 
        mTheoryTreeScroll = new JScrollPane(mTheoryTree); 
        mTheoryTreeScroll.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("MTheoryTreeTitle"))); 
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
        
        tabsPanel.add("North", jtbTabSelection);
        tabsPanel.add("Center", jpTabSelected); 
        tabsPanel.add("South", jpDescription); 
        
        centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabsPanel, graphPanel); 
        

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);        
        this.add(centerPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }
  	
/* Building the tool bars */
  	//TODO refatoracao das outras barras de ferramentas para melhorar codigo
  	
  	private JToolBar buildJtbResident(){
  		
  		JToolBar jtbResident = new JToolBar(); 
  		
  		final JLabel labelResidentName = new JLabel(resource.getString("nameLabel"));
  		
  		//final JButton btnResidentActive = new JButton(iconController.getBoxResidentIcon());  
  		//TODO resources
  		final JButton btnResidentActive = new JButton("Resident");  
  		btnResidentActive.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnResidentActive.setForeground(Color.WHITE);
  		btnResidentActive.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setResidentNodeTabActive(); 
  			}
  		});
  		
  		
  		//TODO resources se for deixar tesxto mesmo
  		final JButton btnAddArgument = new JButton("Arguments"); 
  		btnAddArgument.setToolTipText(resource.getString("addArgumentToolTip")); 
  		
  		final JLabel labelArguments = new JLabel(resource.getString("arguments")); 

  		
  		btnAddArgument.addActionListener(new ActionListener(){
  			
  			public void actionPerformed(ActionEvent ae){
  				setEditArgumentsTabActive(); 
  			}
  			
  		});
  		btnAddArgument.setBackground(ToolKitForGuiMebn.getBorderColor()); 
  		btnAddArgument.setForeground(Color.WHITE);
  		
  		
  		/*---- jtbResident ----*/
  		jtbResident.add(btnResidentActive); 
  		jtbResident.addSeparator();         
  		jtbResident.add(labelResidentName);
  		jtbResident.add(txtNameResident);
  		jtbResident.addSeparator();
  		jtbResident.add(btnAddArgument);
  		txtArguments.setEditable(false); 
  		jtbResident.add(txtArguments); 
  		
  		return jtbResident;  		
  	}
  	
  	private JToolBar buildJtbInput(){
  		
  		JButton btnInputActive; 
  		JLabel labelInputName; 
  		JLabel labelInputOf; 
  		
  		JToolBar jtbInput = new JToolBar(); 
  		
//  	TODO fazer resource... BoxInputIcon
  		btnInputActive = new JButton("Input");   		
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
  		
  		return jtbInput; 
  	}
  	
  	private JToolBar buildJtbContext(){
        
  		JToolBar jtbContext = new JToolBar(); 
  		
  		JButton btnContextActive = new JButton("Context"); 
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
  
        return jtbContext; 
  	}
  	
  	private JPanel buildJpDescritpion(){
  		
        JPanel jpDescription = new JPanel(new BorderLayout()); 
        
		TitledBorder titledBorder; 
		
		titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE), resource.getString("descriptionLabel")); 
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
  		
  		
  		txtNameResident.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				DomainResidentNode nodeAux = (DomainResidentNode)controller.getMebnController().getResidentNodeActive();
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameResident.getText().length()>0)) {
  					try {
  						String name = txtNameResident.getText(0,txtNameResident.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							controller.getMebnController().renameDomainResidentNode(nodeAux, name); 
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

    /**
     * Mostra a tela de edição de tabela. 
     */   
    public void showTableEdit(){
    	
    	DomainResidentNode resident = (DomainResidentNode)controller.getMebnController().getResidentNodeActive(); 
    	
    	this.getGraphPanel().setTopComponent(new TableEdition(resident)); 
    	
    	/*
    	tableEdit = new TableEdition(resident);
    	*/ 
    }

    /**
     * Esconde a tela de edição de tabela. 
     */   
    public void hideTableEdit(){
    	
    	
    	this.getGraphPanel().setTopComponent(null); 
    	
    	/*
    	tableEdit = new TableEdition(resident);
    	*/ 
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
    	
    	if(controller.getMebnController().getCurrentMFrag() != null){
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
    	
    	if(controller.getMebnController().getCurrentMFrag() != null){  		
    		jpTabSelected.remove(residentNodePane);     	
    		residentNodePane = new ResidentNodePane(controller, resident); 
        	jpTabSelected.add("ResidentNodeTab", residentNodePane);         
        	cardLayout.show(jpTabSelected, "ResidentNodeTab"); 	
    	}
    }
    
    public void setResidentNodeTabActive(){
    	cardLayout.show(jpTabSelected, "ResidentNodeTab"); 	
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
