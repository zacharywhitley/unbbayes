package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.ArgumentEditionPane;
import unbbayes.gui.mebn.EntityEditionPane;
import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.InputNodePane;
import unbbayes.gui.mebn.MTheoryTree;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.QueryPanel;
import unbbayes.gui.mebn.ResidentNodePane;
import unbbayes.gui.mebn.TableEditionPane;
import unbbayes.gui.mebn.ToolBarOrdVariable;
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
 *  @author La�cio Lima dos Santos
 *  @author Rommel N. Carvalho
 *  @version 1.0 06/08/07
 */

public class MEBNEditionPane extends JPanel {

	private static final long serialVersionUID = 6194855055129252835L;

	private final NetworkWindow netWindow;

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

    private DescriptionPane descriptionPane;

    /* Text fields */

    private JTextField txtNameMTheory;
    private JTextField txtDescription;
    private JTextField txtFormula;
    private JTextField txtNameMFrag;
    private JTextField txtNameContext;
    private JTextField txtArguments;

    private final MEBNController mebnController;
    private final JSplitPane graphPanel;
    private final JLabel status;
    private final JPanel bottomPanel;

    private final JPanel topPanel;
    private final ToolBarEdition jtbEdition;

    private final JPanel nodeSelectedBar;
    private final CardLayout cardLayout = new CardLayout();

    /* Tool bars for each possible edition mode */

    private final JToolBar jtbEmpty;

    private final ToolBarMFrag toolBarMFrag;
    private final ToolBarResidentNode toolBarResidentNode;
    private final ToolBarInputNode toolBarInputNode;
    private final ToolBarContextNode toolBarContextNode;
    private final ToolBarMTheory toolBarMTheory;
    private final ToolBarOrdVariable toolBarOVariable;

    private final String MTHEORY_BAR = "MTheoryCard";
    private final String RESIDENT_BAR = "ResidentCard";
    private final String CONTEXT_BAR = "ContextCard";
    private final String INPUT_BAR = "InputCard";
    private final String MFRAG_BAR = "MFragCard";
    private final String EMPTY_BAR = "EmptyCard";
    private final String ORDVARIABLE_BAR = "OrdVariableCard";

    /* Buttons for select the active tab */

    private final JButton btnTabOptionTree;
    private final JButton btnTabOptionOVariable;
    private final JButton btnTabOptionEntity;
    private final JButton btnTabOptionEntityFinding;
    private final JButton btnTabOptionNodeFinding;

    private final String MTHEORY_TREE_TAB = "MTheoryTree";
    private final String ENTITY_EDITION_TAB = "EntityEdtionTab";
    private final String OVARIABLE_EDITION_TAB = "EditOVariableTab";
    private final String INPUT_NODE_TAB = "InputNodeTab";
    private final String ARGUMENTS_EDITION_TAB = "EditArgumentsTab";
    private final String RESIDENT_NODE_TAB = "ResidentNodeTab";
    private final String FORMULA_TAB = "FormulaEdtion";
    private final String ENTITY_FINDING_TAB = "EntityFindingTab";
    private final String NODE_FINDING_TAB = "NodeFindingTab";

    /* Control if the table edition panel is being showed */
    private boolean isTableEditionPaneShow = false; 

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
        descriptionPane = new DescriptionPane();
        jtbTabSelection = new JToolBar();

        nodeSelectedBar = new JPanel(cardLayout);

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

        btnTabOptionTree = new JButton(iconController.getMTheoryNodeIcon());
        btnTabOptionOVariable = new JButton(iconController.getOVariableNodeIcon());
        btnTabOptionEntity = new JButton(iconController.getObjectEntityIcon());
        btnTabOptionEntityFinding = new JButton(iconController.getEntityInstance());
        btnTabOptionNodeFinding = new JButton(iconController.getNodeInstance());

        btnTabOptionTree.setToolTipText(resource.getString("showMTheoryToolTip"));
        btnTabOptionOVariable.setToolTipText(resource.getString("showOVariablesToolTip"));
        btnTabOptionEntity.setToolTipText(resource.getString("showEntitiesToolTip"));

        addActionListenersToButtons();

        jtbEdition = new ToolBarEdition();
        jtbEdition.setFloatable(true);
        jtbEdition.setOrientation(JToolBar.VERTICAL);


        topPanel.add(new ToolBarGlobalOptions());

        toolBarMFrag = new ToolBarMFrag();
        toolBarResidentNode = new ToolBarResidentNode();
        toolBarInputNode = new ToolBarInputNode();
        toolBarContextNode = new ToolBarContextNode();
        toolBarOVariable = new ToolBarOrdVariable(mebnController);
        toolBarMTheory = new ToolBarMTheory();

        /*---- jtbEmpty ----*/
        JTextField txtIsEmpty = new JTextField(resource.getString("whithotMFragActive"));
        txtIsEmpty.setEditable(false);
        jtbEmpty.addSeparator();
        jtbEmpty.add(txtIsEmpty);
        jtbEmpty.setFloatable(false);

        /*---- Add card panels in the layout ----*/

        nodeSelectedBar.add(MTHEORY_BAR, toolBarMTheory);
        nodeSelectedBar.add(RESIDENT_BAR, toolBarResidentNode);
        nodeSelectedBar.add(CONTEXT_BAR, toolBarContextNode);
        nodeSelectedBar.add(INPUT_BAR, toolBarInputNode);
        nodeSelectedBar.add(MFRAG_BAR, toolBarMFrag);
        nodeSelectedBar.add(EMPTY_BAR, jtbEmpty);
        nodeSelectedBar.add(ORDVARIABLE_BAR, toolBarOVariable);

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

        /*------------------- Left panel ---------------------*/

        tabsPanel.add(BorderLayout.NORTH, jtbTabSelection);
        tabsPanel.add(BorderLayout.CENTER, jpTabSelected);
        tabsPanel.add(BorderLayout.SOUTH, descriptionPane);
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


  	private void addActionListenersToButtons(){

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



  	/**
  	 * Show the table edit in the top component. 
  	 */
    public void showTableEditionPane(DomainResidentNode resident){

    	this.getGraphPanel().setTopComponent(new TableEditionPane(resident, mebnController));
    	
    	isTableEditionPaneShow = true; 

    }
    
    /**
     * Hide the top component (while, this top component contains only the table
     * of a resident node). 
     */
    public void hideTopComponent(){

    	this.getGraphPanel().setTopComponent(null);
    	isTableEditionPaneShow = false;
    }
    
    /**
     * 
     */
    public boolean isTableEditionPaneShow(){
    	return isTableEditionPaneShow; 
    }

    public void showTitleGraph(String mFragName){

    	JPanel titleMFragPane = new JPanel(new BorderLayout());
    	titleMFragPane.setBackground(new Color(7, 199, 203));

    	JLabel label = new JLabel(mFragName);
    	label.setForeground(Color.BLACK);
    	label.setHorizontalAlignment(JLabel.CENTER);
    	label.setHorizontalTextPosition(JLabel.CENTER);

    	titleMFragPane.add(label, BorderLayout.CENTER);

    	if(!isTableEditionPaneShow){
    		this.getGraphPanel().setTopComponent(titleMFragPane);
    	}

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
      return toolBarResidentNode.getJTextFieldName();
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
        toolBarOVariable.updateListOfTypes();
        toolBarOVariable.setOrdVariable(ov);
        cardLayout.show(nodeSelectedBar, ORDVARIABLE_BAR);
    }

    /*---------------------------------------------------------*/

    public void setTxtNameResident(String name){
    	toolBarResidentNode.setNameResident(name);
    }

    public void setTxtNameMFrag(String name){
    	txtNameMFrag.setText(name);
    }

    public void setTxtNameInput(String name){
    	toolBarInputNode.setTxtNameInput(name);
    }

    public void setTxtNameContext(String name){
    	txtNameContext.setText(name);
    }

    public void setTxtInputOf(String name){
    	toolBarInputNode.setTxtInputOf(name);
    }

    public void setTxtArguments(String args){
    	txtArguments.setText(args);
    }

	public void setFormula(String formula) {
		this.txtFormula.setText(formula);
	}

	private class ToolBarGlobalOptions extends JToolBar{

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private JButton btnGlobalOption;

	    private JButton btnEditingMode;
	    
	    private JButton btnQueryMode;
	    
	    private JButton btnTurnToSSBNMode; 
	    
	    private JButton btnSaveKB; 
	    private JButton btnLoadKB; 
		private JButton btnClearKB; 
	    
	    public ToolBarGlobalOptions(){
	    	
	    	super(); 
	    	
	    	btnGlobalOption = new JButton(iconController.getGlobalOptionIcon());
	    	btnGlobalOption.setToolTipText(resource.getString("mFragInsertToolTip"));
	    	
	    	btnEditingMode = new JButton(iconController.getGlobalOptionIcon());
	    	btnQueryMode = new JButton(iconController.getCompileIcon());
	    	btnTurnToSSBNMode = new JButton(iconController.getSsbnIcon()); 
	    	btnClearKB = new JButton(iconController.getEditDelete()); 
	    	
	    	btnQueryMode.addActionListener(new ActionListener(){
	    		
	    		public void actionPerformed(ActionEvent arg0) {
	    			QueryPanel queryPanel = new QueryPanel(mebnController);
	    			queryPanel.pack();
	    			queryPanel.setVisible(true);
	    		}
	    		
	    	});
	    	
	    	btnTurnToSSBNMode.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					if(!mebnController.turnToSSBNMode()){
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Não há SSBN gerada anteriormente! Modo não disponivel.");
					}
				}
	    		
	    	});

	    	btnClearKB.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					mebnController.clearKnowledgeBase(); 
					JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Base de conhecimento limpa com sucesso");
				}
	    		
	    	});
	    	
	    	btnGlobalOption.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					mebnController.clearKnowledgeBase(); 
					JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Funcionalidade ainda não implementada");
				}
	    		
	    	});
	    	
	    	/*--------------- PowerLoom Options ------------------*/
	    	JButton btnSaveGenerative = new JButton("SVG");
	    	btnSaveGenerative.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent ae) {
	    			setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    			
	    			JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
	    			chooser.setMultiSelectionEnabled(false);
	    			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    			
	    			int option = chooser.showSaveDialog(null);
	    			if (option == JFileChooser.APPROVE_OPTION) {
	    				File file = chooser.getSelectedFile();
	    				if (file != null) {
	    						mebnController.saveGenerativeMTheory(file);
	    						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Arquivo salvo com sucesso");

	    				}
	    			}
	    			
	    			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    		}
	    	}); 
	    	
	    	btnSaveKB = new JButton(iconController.getSaveFindingsInstance());
	    	btnSaveKB.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent ae) {
	    			setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    			
	    			JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
	    			chooser.setMultiSelectionEnabled(false);
	    			chooser
	    			.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    			
	    			int option = chooser.showSaveDialog(null);
	    			if (option == JFileChooser.APPROVE_OPTION) {
	    				File file = chooser.getSelectedFile();
	    				if (file != null) {
	    						mebnController.saveFindingsFile(file);
	    						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Arquivo salvo com sucesso");
	    				}
	    			}
	    			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    		}
	    	}); 
	    	
	    	btnLoadKB = new JButton(iconController.getLoadFindingsInstance());
	    	btnLoadKB.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent ae) {
	    			setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    			JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
	    			chooser.setMultiSelectionEnabled(false);
	    			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    			
	    			int option = chooser.showOpenDialog(null);
	    			if (option == JFileChooser.APPROVE_OPTION) {
	    				if (chooser.getSelectedFile() != null) {
	    					mebnController.loadFindingsFile(chooser.getSelectedFile());
	    					JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), "Arquivo carregado com sucesso");
	    				}
	    			}
	    			
	    			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    		}});

	        add(btnQueryMode);
	        
	        addSeparator(); 
	        
	        add(btnLoadKB); 
	        add(btnSaveKB); 
	        add(btnClearKB);
	        
	        addSeparator(new Dimension(10, 10)); 
	        
	        add(btnTurnToSSBNMode); 
	        
	        addSeparator(new Dimension(10, 10)); 
	        
	        add(btnEditingMode);
	        

	        
	        setFloatable(false);
	    }; 
	    
	}
	
  	private class ToolBarEdition extends JToolBar{

  	    private final JButton btnAddMFrag;
  	    private final JButton btnAddContextNode;
  	    private final JButton btnAddInputNode;
  	    private final JButton btnAddResidentNode;
  	    private final JButton btnAddEdge;
  	    private final JButton btnAddOrdinaryVariable;
  	    private final JButton btnEditMTheory;
  	    private final JButton btnSelectObject;

  		public ToolBarEdition(){

  	        btnEditMTheory = new JButton(iconController.getMTheoryNodeIcon());
  	        btnAddEdge               = new JButton(iconController.getEdgeIcon());
  	        btnAddContextNode = new JButton(iconController.getContextNodeIcon());
  	        btnAddInputNode  = new JButton(iconController.getInputNodeIcon());
  	        btnAddResidentNode  = new JButton(iconController.getResidentNodeIcon());
  	        btnAddOrdinaryVariable = new JButton(iconController.getOVariableNodeIcon());
  	        btnAddMFrag		= new JButton(iconController.getMFragIcon());
  	        btnSelectObject            = new JButton(iconController.getSelectionIcon());

  	        btnEditMTheory.setToolTipText(resource.getString("mTheoryEditionTip"));
  	        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
  	        btnAddMFrag.setToolTipText(resource.getString("mFragInsertToolTip"));
  	        btnAddContextNode.setToolTipText(resource.getString("contextNodeInsertToolTip"));
  	        btnAddInputNode.setToolTipText(resource.getString("inputNodeInsertToolTip"));
  	        btnAddResidentNode.setToolTipText(resource.getString("residentNodeInsertToolTip"));
  	        btnSelectObject.setToolTipText(resource.getString("mFragInsertToolTip"));

  	        add(btnEditMTheory);
  	        addSeparator();
  	        add(btnAddMFrag);
  	        addSeparator();
  	        add(btnAddResidentNode);
  	        add(btnAddInputNode);
  	        add(btnAddContextNode);
  	        add(btnAddOrdinaryVariable);
  	        add(btnAddEdge);
  	        addSeparator();
  	        add(btnSelectObject);
  	        addSeparator();

  	  		btnEditMTheory.addActionListener(new ActionListener(){
  	  			public void actionPerformed(ActionEvent ae){
  	  				mebnController.enableMTheoryEdition();
  	  			}
  	  		});

  	  		//ao clicar no botão btnAddEdge setamos as variaveis booleanas e os estados dos butoes
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

  		}


  	}


  	private class ToolBarMTheory extends JToolBar{

  		public ToolBarMTheory(){

  			super();
  			setLayout(new GridLayout(1,5));

  	  		ButtonLabel btnMTheoryActive = new ButtonLabel(resource.getString("MTheoryButton"), iconController.getMTheoryNodeIcon());

  	    	btnMTheoryActive.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  				setMTheoryTreeActive();
  	  			}
  	  		});

  	    	JLabel labelMTheoryName = new JLabel(resource.getString("nameLabel") + " ");

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

  	    	add(btnMTheoryActive);

  	    	JToolBar barName = new JToolBar();
  	    	barName.add(labelMTheoryName);
  	    	barName.add(txtNameMTheory);
  	    	barName.setFloatable(false);
  	    	add(barName);

  	    	add(new JPanel());
  	    	add(new JPanel());
  	    	add(new JPanel());

  	  		setFloatable(false);

  		}
  	}

    private class ToolBarMFrag extends JToolBar{

    	public ToolBarMFrag(){
    		super();
  			setLayout(new GridLayout(1,5));

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

      		add(btnMFragActive);

      		JToolBar barName = new JToolBar();
      		barName.add(labelMFragName);
      		barName.add(txtNameMFrag);
      		barName.setFloatable(false);
      		add(barName);

    		JPanel emptyPane = new JPanel();
    		add(emptyPane);

    		emptyPane = new JPanel();
    		add(emptyPane);

    		emptyPane = new JPanel();
    		add(emptyPane);

    		setFloatable(false);

    	}

    }

  	private class ToolBarInputNode extends JToolBar{

  		private ButtonLabel btnInputActive;
  		private JLabel labelInputName;
  		private JLabel labelInputOf;
  	    private JTextField txtNameInput;
  	    private JTextField txtInputOf;

  		public ToolBarInputNode(){
  			super();

  	  		btnInputActive = new ButtonLabel(resource.getString("InputButton"), iconController.getInputNodeIcon());
  	  		btnInputActive.addActionListener(new ActionListener(){
  	  			public void actionPerformed(ActionEvent ae){
  	  				setInputNodeActive();
  	  			}
  	  		});

  	  		labelInputName = new JLabel(resource.getString("nameLabel") + " ");
  	  		labelInputOf = new JLabel(resource.getString("inputOf"));

  	    	GridLayout grid = new GridLayout(1,5);
  	    	setLayout(grid);

  	  		add(btnInputActive);

  	  		JToolBar barName = new JToolBar();
  	  		barName.setFloatable(false);

  	  	    txtNameInput = new JTextField(10);
	        txtNameInput.setEditable(false);

	        barName.add(labelInputName);
  	  		barName.add(txtNameInput);
  	     	barName.setFloatable(false);
  	        add(barName);

	        txtInputOf = new JTextField(10);
  	        txtInputOf.setEditable(false);

  	        add(new JPanel());
  	  		add(new JPanel());
  	        add(new JPanel());

  	        setFloatable(false);
  		}

		public void setTxtInputOf(String txtInputOf) {
			this.txtInputOf.setText(txtInputOf);
		}

		public void setTxtNameInput(String txtNameInput) {
			this.txtNameInput.setText(txtNameInput);
		}
  	}

  	private class ToolBarContextNode extends JToolBar{

  		public ToolBarContextNode(){

  			super();
  			setLayout(new GridLayout(1,5));

  	  		ButtonLabel btnContextActive = new ButtonLabel(resource.getString("ContextButton"), iconController.getContextNodeIcon());
  	  		btnContextActive.addActionListener(new ActionListener(){

  	  			public void actionPerformed(ActionEvent ae){
  	  				setFormulaEdtionActive();
  	  			}

  	  		});

  	        final JLabel labelFormula = new JLabel(resource.getString("formula"));
  	        //add(labelFormula);

  	        txtFormula.setEditable(false);
  	        //add(txtFormula);

  	        final JLabel labelContextName = new JLabel(resource.getString("nameLabel") + " ");
  	        txtNameContext.setEditable(false);
  	        JToolBar barName = new JToolBar();
  	        barName.add(labelContextName);
  	        barName.add(txtNameContext);
  	        barName.setFloatable(false);


  	        add(btnContextActive);
  	        add(barName);
  	        add(new JPanel());
  	        add(new JPanel());
  	        add(new JPanel());

  	        setFloatable(false);

  		}
  	}

	/*
	 * Tool Bar for resident node edition.
	 *
	 */
    private class ToolBarResidentNode extends JToolBar{

    	private JLabel labelResidentName = new JLabel(resource.getString("nameLabel") + " ");
    	private ButtonLabel btnResidentActive = new ButtonLabel(resource.getString("ResidentButton"), iconController.getResidentNodeIcon());
    	private ButtonLabel btnAddArgument = new ButtonLabel(resource.getString("ArgumentsButton"), iconController.getOVariableNodeIcon());
        private JTextField txtNameResident;

    	public ToolBarResidentNode(){
    		super();

      		btnResidentActive.addActionListener(new ActionListener() {
      			public void actionPerformed(ActionEvent ae) {
      				setResidentNodeTabActive();
      			}
      		});

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
        	this.setLayout(grid);

        	this.add(btnResidentActive);

      		JToolBar barName = new JToolBar();
      		barName.setFloatable(false);
      		barName.add(labelResidentName);
      		barName.add(txtNameResident);
      		this.add(barName);

      		JToolBar barOptions = new JToolBar();
      		barOptions.setFloatable(false);
      		barOptions.setLayout(new GridLayout(1,5));

      		JButton btnStateEdition = new JButton(iconController.getStateIcon());
      		JButton btnEditTable = new JButton(iconController.getGridIcon());
      		JButton btnEditArguments = new JButton(iconController.getArgumentsIcon());

      		btnStateEdition.addActionListener(new ActionListener() {
      			public void actionPerformed(ActionEvent ae) {
      				setResidentNodeTabActive();
      			}
      		});

      		btnEditTable.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
        			mebnController.setEnableTableEditionView();
            	}
    		});

      		btnEditArguments.addActionListener(new ActionListener(){

      			public void actionPerformed(ActionEvent ae){
      				setEditArgumentsTabActive();
      			}

      		});

      		barOptions.add(btnStateEdition);
      		barOptions.add(btnEditArguments);
      		barOptions.add(btnEditTable);
      		barOptions.add(new JPanel());
      		barOptions.add(new JPanel());

      		this.add(barOptions);

      		this.add(new JPanel());

      		this.add(new JPanel());

      		this.setFloatable(false);

    	}

		public JTextField getJTextFieldName() {
			return txtNameResident;
		}

		public void setNameResident(String txtNameResident) {
			this.txtNameResident.setText(txtNameResident);
		}

    }

    /*
     * Painel que mostra a descri��o do objeto selecionado.
     */

  	private class DescriptionPane extends JPanel{

  		private JTextArea textArea;

  		public DescriptionPane(){

  			super(new BorderLayout());

  			TitledBorder titledBorder;

  			titledBorder = BorderFactory.createTitledBorder(
  					BorderFactory.createLineBorder(Color.BLUE),
  					resource.getString("descriptionLabel"));
  			titledBorder.setTitleColor(Color.BLUE);
  			titledBorder.setTitleJustification(TitledBorder.CENTER);

  			setBorder(titledBorder);

  	        textArea = new JTextArea(5, 10);
  	        JScrollPane scrollPane =
  	            new JScrollPane(textArea,
  	                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
  	                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  	        textArea.setEditable(true);

  	        add(scrollPane, BorderLayout.CENTER);
  		}

  		public void setDescription(String description){
  			textArea.setText(description);
  		}

  		public String getDescriptions(String description){
  			return textArea.getText();
  		}

  	}

	public NetworkWindow getNetworkWindow() {
		return netWindow;
	}

    /*
     * Classe com bot�es para utilizar o powerloom em uma forma simplificada,
     * entrando com os findings e queries como strings na sintaxe do PowerLoom.
     * Permite ainda salvar a MTheory atual e carregar arquivos com MTheorys
     * e findings pr�-definidos.
     *
     * (Apenas para testes)
     */
//  	private class ToolBarPowerLoomButtons extends JToolBar{
//
//  		private final MEBNController mebnController;
//
//  		public ToolBarPowerLoomButtons(MEBNController _mebnController){
//
//  			super();
//
//  			mebnController = _mebnController;
//
//  			JButton rodarKB = new JButton("PL");
//  	        add(rodarKB);
//  	        rodarKB.addActionListener(new ActionListener() {
//  	  			public void actionPerformed(ActionEvent ae) {
//  	  				mebnController.loadGenerativeMEBNIntoKB();
//  	  			    JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(),
//  	  					   "Base de conhecimento criada com sucesso");
//  	  			}
//  	  		});
//
//  	        JButton entityFinding = new JButton("EF");
//  	        add(entityFinding);
//  	        entityFinding.addActionListener(new ActionListener() {
//  	  			public void actionPerformed(ActionEvent ae) {
//  	  				String finding = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(),
//  	  						"Entre com o entity finding: ",
//  	  						"Test Finding",
//  	  						JOptionPane.QUESTION_MESSAGE);
//  	  				if((finding!=null)&&(!finding.equals("")))
//  	  				mebnController.makeEntityAssert(finding);
//  	  			}
//  	  		});
//
//  	        JButton finding = new JButton("RF");
//  	        add(finding);
//  	        finding.addActionListener(new ActionListener() {
//  	  			public void actionPerformed(ActionEvent ae) {
//  	  				String finding = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(),
//  	  						"Entre com o relation finding: ",
//  	  						"Test Finding",
//  	  						JOptionPane.QUESTION_MESSAGE);
//  	  				if((finding!=null)&&(!finding.equals("")))
//  	  				mebnController.makeRelationAssert(finding);
//  	  			}
//  	  		});
//
//  	        JButton link = new JButton("LK");
//  	        add(link);
//  	        link.addActionListener(new ActionListener() {
//  	  			public void actionPerformed(ActionEvent ae) {
//  	  				String ovName = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(),
//  	  						"Entre com o nome da variavel ordinaria: ",
//  	  						"Test Finding",
//  	  						JOptionPane.QUESTION_MESSAGE);
//
//  	  				String entityName = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(),
//  	  						"Entre com o nome da entidade: ",
//  	  						"Test Finding",
//  	  						JOptionPane.QUESTION_MESSAGE);
//
//  	  				if(((ovName!=null)&&(!ovName.equals("")))&&((entityName!=null)&&(!entityName.equals(""))))
//  	  				mebnController.linkOrdVariable2Entity(ovName, entityName);
//  	  			}
//  	  		});
//
//  	        JButton context = new JButton("CT");
//  	        add(context);
//  	        context.addActionListener(new ActionListener() {
//  	  			public void actionPerformed(ActionEvent ae) {
//  	  				mebnController.executeContext();
//  	  			}
//  	  		});
//
//  	        JButton execute = new JButton("X");
//  	        add(execute);
//  	        execute.addActionListener(new ActionListener() {
//  	  			public void actionPerformed(ActionEvent ae) {
//  	  				String command = JOptionPane.showInputDialog(mebnController.getMebnEditionPane(), "Insira o commando a ser executado");
//  	  				if((command != "")&&(command != null)){
//  	  				   String resposta = mebnController.executeCommand(command);
//  	  				   JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resposta);
//  	  				}
//  	  			}
//  	  		});
//
//  		}
//
//  	}

}