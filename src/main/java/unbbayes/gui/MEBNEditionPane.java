/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.ArgumentEditionPane;
import unbbayes.gui.mebn.DescriptionPane;
import unbbayes.gui.mebn.EntityEditionPane;
import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.InputNodePane;
import unbbayes.gui.mebn.MTheoryTree;
import unbbayes.gui.mebn.OVariableEditionPane;
import unbbayes.gui.mebn.QueryPanel;
import unbbayes.gui.mebn.ResidentNodePane;
import unbbayes.gui.mebn.ToolBarOrdVariable;
import unbbayes.gui.mebn.auxiliary.ButtonLabel;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.finding.EntityFindingEditionPane;
import unbbayes.gui.mebn.finding.RandomVariableFindingEdtitionPane;
import unbbayes.io.exception.UBIOException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.exception.ReservedWordException;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.util.ResourceController;

/**
 * Pane for edition of MEBN. This is the main panel of
 * the MEBN suport of the UnBBayes. All others painels of MEBN
 * are inside this panel.
 *
 *  @author Laecio Lima dos Santos
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
	// Is it really necessary?!
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
    private RandomVariableFindingEdtitionPane nodeFindingEditionPane;
    private EntityFindingEditionPane entityFindingEditionPane;

    private ResidentNodePane residentNodePane;
    private ArgumentEditionPane editArgumentsTab;

    private final DescriptionPane descriptionPane;

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

    private final JPanel nodeSelectedToolBar;
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
    private ButtonGroup groupButtonsTabs = new ButtonGroup(); 
    private final JToggleButton btnTabOptionTree;
    private final JToggleButton btnTabOptionOVariable;
    private final JToggleButton btnTabOptionEntity;
    private final JToggleButton btnTabOptionEntityFinding;
    private final JToggleButton btnTabOptionNodeFinding;

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
  	private static ResourceBundle resource = ResourceController.RS_GUI; 

  	public MEBNEditionPane(NetworkWindow _netWindow,
            MEBNController _controller) {
        this.netWindow     = _netWindow;
        this.mebnController    = _controller;
        this.setLayout(new BorderLayout());

        topPanel    = new JPanel(new GridLayout(1,1));

        tabsPanel = new JPanel(new BorderLayout());

        jpTabSelected = new JPanel(cardLayout);
        descriptionPane = new DescriptionPane(mebnController);
        jtbTabSelection = new JToolBar();

        nodeSelectedToolBar = new JPanel(cardLayout);

        jtbEmpty = new JToolBar();

        graphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        graphPanel.setDividerSize(1);

        bottomPanel = new JPanel(new GridLayout(1,1));
        status      = new JLabel(resource.getString("statusReadyLabel"));

        txtDescription     = new JTextField(15);

        txtNameMTheory = new JTextField(5);
        txtNameMFrag = new JTextField(5);
        txtNameContext = new JTextField(5);
        txtArguments = new JTextField(10);
        txtFormula = new JTextField(15);

        btnTabOptionTree = new ButtonTab(iconController.getMTheoryNodeIcon());
        btnTabOptionOVariable = new ButtonTab(iconController.getOVariableNodeIcon());
        btnTabOptionEntity = new ButtonTab(iconController.getObjectEntityIcon());
        btnTabOptionEntityFinding = new ButtonTab(iconController.getEntityInstance());
        btnTabOptionNodeFinding = new ButtonTab(iconController.getNodeInstance());

        btnTabOptionTree.setToolTipText(resource.getString("showMTheoryToolTip"));
        btnTabOptionOVariable.setToolTipText(resource.getString("showOVariablesToolTip"));
        btnTabOptionEntity.setToolTipText(resource.getString("showEntitiesToolTip"));
        btnTabOptionEntityFinding.setToolTipText(resource.getString("showEntityInstancesToolTip"));
        btnTabOptionNodeFinding.setToolTipText(resource.getString("showFingingsToolTip"));

        groupButtonsTabs.add(btnTabOptionTree); 
        groupButtonsTabs.add(btnTabOptionOVariable); 
        groupButtonsTabs.add(btnTabOptionEntity); 
        groupButtonsTabs.add(btnTabOptionEntityFinding); 
        groupButtonsTabs.add(btnTabOptionNodeFinding); 
        
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

        nodeSelectedToolBar.add(MTHEORY_BAR, toolBarMTheory);
        nodeSelectedToolBar.add(RESIDENT_BAR, toolBarResidentNode);
        nodeSelectedToolBar.add(CONTEXT_BAR, toolBarContextNode);
        nodeSelectedToolBar.add(INPUT_BAR, toolBarInputNode);
        nodeSelectedToolBar.add(MFRAG_BAR, toolBarMFrag);
        nodeSelectedToolBar.add(EMPTY_BAR, jtbEmpty);
        nodeSelectedToolBar.add(ORDVARIABLE_BAR, toolBarOVariable);

        cardLayout.show(nodeSelectedToolBar, EMPTY_BAR);


        
//        topPanel.add(nodeSelectedToolBar);

        bottomPanel.add(status);
        
        jtbTabSelection.setLayout(new GridLayout(1,5));
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.add(btnTabOptionOVariable);
        jtbTabSelection.add(btnTabOptionEntity);
        jtbTabSelection.add(btnTabOptionEntityFinding);
        jtbTabSelection.add(btnTabOptionNodeFinding);
        jtbTabSelection.setFloatable(false);
        
        /*---------------- Tab panel ----------------------*/

        mTheoryTree = new MTheoryTree(mebnController, netWindow.getGraphPane());
        mTheoryTreeScroll = new JScrollPane(mTheoryTree);
        mTheoryTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
        mTheoryTreeScroll.setBorder(MebnToolkit.getBorderForTabPanel(
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

        nodeFindingEditionPane = new RandomVariableFindingEdtitionPane();
        jpTabSelected.add(NODE_FINDING_TAB, nodeFindingEditionPane);

        cardLayout.show(jpTabSelected, "MTheoryTree");

        /*------------------- Left panel ---------------------*/

        tabsPanel.add(BorderLayout.NORTH, jtbTabSelection);
        tabsPanel.add(BorderLayout.CENTER, jpTabSelected);
        tabsPanel.add(BorderLayout.SOUTH, descriptionPane);
        tabsPanel.add(BorderLayout.EAST, jtbEdition);

        
        centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabsPanel, graphPanel);
        centerPanel.setDividerSize(1);
        centerPanel.setDividerLocation(200); 

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
    public void showTableEditionPane(ResidentNode resident){
    	mebnController.openCPTDialog(resident); 
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

    	JToolBar tb1 = new JToolBar(); 
    	JButton btnTest = new JButton(iconController.getResidentNodeIcon()); 
//    	btnTest.setBackground(MebnToolkit.getColor1());
//    	tb1.setBackground(MebnToolkit.getColor1()); 
    	tb1.add(btnTest); 
    	tb1.setFloatable(false); 
    	
    	JToolBar tb2 = new JToolBar();
    	tb2.setLayout(new BorderLayout()); 
    	
    	JLabel label = new JLabel(mFragName);
    	label.setForeground(Color.BLACK);
    	label.setHorizontalAlignment(JLabel.CENTER);
    	label.setHorizontalTextPosition(JLabel.CENTER);
//    	tb2.setBackground(MebnToolkit.getColor1());
    	tb2.add(label, BorderLayout.CENTER); 
    	JButton jbtn = new JButton(); jbtn.setBackground(Color.white); 
    	JButton jbtn2 = new JButton(); jbtn2.setBackground(Color.white); 
    	jbtn = new JButton(iconController.getMFragIcon()); 
    	jbtn2 =  new JButton(iconController.getMFragIcon()); 
    	tb2.setBackground(new Color(255, 255, 255)); 
//    	tb2.add(jbtn, BorderLayout.LINE_END); 
//    	tb2.add(jbtn2, BorderLayout.LINE_START); 
    	
    	
//    	tb2.setBorder(BorderFactory.createLineBorder(Color.blue)); 
    	tb2.setFloatable(false); 

    	

//    	JToolBar jtb = new JToolBar(); 
//    	jtb.add(new JButton(iconController.getSaveNetIcon())); 
//    	jtb.add(new JButton(iconController.getPrintNetIcon())); 
//    	jtb.setFloatable(false); 
//    	
    	titleMFragPane.add(nodeSelectedToolBar, BorderLayout.PAGE_START); 	
    	titleMFragPane.add(tb2, BorderLayout.PAGE_END); 	
    	
    	if(!isTableEditionPaneShow){
    		this.getGraphPanel().setTopComponent(titleMFragPane);
    		this.getGraphPanel().setBackground(Color.white); 
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
        this.status.validate(); 
        this.status.paintImmediately(this.status.getBounds()); 
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
//        mTheoryTree.updateTree();
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

    public void setInputNodeActive(InputNode input){
    	jpTabSelected.remove(inputNodePane);
		inputNodePane = new InputNodePane(mebnController, input);
    	jpTabSelected.add("InputNodeTab", inputNodePane);
    	cardLayout.show(jpTabSelected, "InputNodeTab");
// 	    unselectButtonsGroupButtonsTabs(); 
    }

    public InputNodePane getInputNodePane(){
    	return inputNodePane;
    }

    public void setInputNodeActive(){
    	cardLayout.show(jpTabSelected, "InputNodeTab");
    }

    public void setArgumentTabActive(){
        cardLayout.show(jpTabSelected, "ArgumentTab");
// 	   unselectButtonsGroupButtonsTabs(); 
    }

    public void setEditArgumentsTabActive(ResidentNode resident){

        jpTabSelected.remove(editArgumentsTab);
    	editArgumentsTab = new ArgumentEditionPane(mebnController, resident);
    	jpTabSelected.add("EditArgumentsTab", editArgumentsTab);

    	cardLayout.show(jpTabSelected, "EditArgumentsTab");
// 	    unselectButtonsGroupButtonsTabs(); 
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

    public void setResidentNodeTabActive(ResidentNode resident){

    	if(mebnController.getCurrentMFrag() != null){
    		jpTabSelected.remove(residentNodePane);
    		residentNodePane = new ResidentNodePane(mebnController, resident);
        	jpTabSelected.add("ResidentNodeTab", residentNodePane);
        	cardLayout.show(jpTabSelected, "ResidentNodeTab");
//     	    unselectButtonsGroupButtonsTabs(); 
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
       nodeFindingEditionPane = new RandomVariableFindingEdtitionPane(mebnController);
       jpTabSelected.add(NODE_FINDING_TAB, nodeFindingEditionPane);
 	   cardLayout.show(jpTabSelected, NODE_FINDING_TAB);
    }


    /* Bar Selected */

    public void setMTheoryBarActive(){
        cardLayout.show(nodeSelectedToolBar, MTHEORY_BAR);
    }

    public void setResidentBarActive(){
        cardLayout.show(nodeSelectedToolBar, RESIDENT_BAR);
    }

    public void setContextBarActive(){
        cardLayout.show(nodeSelectedToolBar, CONTEXT_BAR);
    }

    public void setInputBarActive(){
        cardLayout.show(nodeSelectedToolBar, INPUT_BAR);
    }

    public void setMFragBarActive(){
        cardLayout.show(nodeSelectedToolBar, MFRAG_BAR);
    }

    public void setEmptyBarActive(){
        cardLayout.show(nodeSelectedToolBar, EMPTY_BAR);
    }

    public void setOrdVariableBarActive(OrdinaryVariable ov){
        toolBarOVariable.updateListOfTypes();
        toolBarOVariable.setOrdVariable(ov);
        cardLayout.show(nodeSelectedToolBar, ORDVARIABLE_BAR);
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

	public NetworkWindow getNetworkWindow() {
		return netWindow;
	}
	
	public void setDescriptionText(String text, int type){
		this.descriptionPane.setDescriptionText(text, type); 
	}
	
	public String getDescriptionText(){
		return this.descriptionPane.getDescriptionText(); 
	}
	
	/**
	 * Contains geral mebn buttons: 
	 * - save, load, clear knowledge base
	 * - execute query
	 * - turn to ssbn/edition mode
	 */
	private class ToolBarGlobalOptions extends JToolBar{

		private static final long serialVersionUID = 1L;

	    private JButton btnDoQuery;
	    
	    private JButton btnTurnToSSBNMode; 
	    
	    private JButton btnSaveKB; 
	    private JButton btnLoadKB; 
		private JButton btnClearKB; 
	    
	    public ToolBarGlobalOptions(){
	    	
	    	super(); 
	    	
	    	btnDoQuery = new JButton(iconController.getCompileIcon());
	    	btnTurnToSSBNMode = new JButton(iconController.getSsbnIcon()); 
	    	btnClearKB = new JButton(iconController.getEditDelete()); 

	    	btnLoadKB = new JButton(iconController.getLoadFindingsInstance());
	    	btnSaveKB = new JButton(iconController.getSaveFindingsInstance());
	    	
	    	btnDoQuery.setToolTipText(resource.getString("executeQueryToolTip"));
	    	btnTurnToSSBNMode.setToolTipText(resource.getString("turnToSSBNModeToolTip"));
	    	btnClearKB.setToolTipText(resource.getString("clearKBToolTip"));
	    	btnLoadKB.setToolTipText(resource.getString("loadKBToolTip"));
	    	btnSaveKB.setToolTipText(resource.getString("saveKBToolTip"));
	    	
	    	btnDoQuery.addActionListener(new ActionListener(){
	    		
	    		public void actionPerformed(ActionEvent arg0) {
	    			QueryPanel queryPanel = new QueryPanel(mebnController);
	    			queryPanel.setLocationRelativeTo(netWindow.getDesktopPane()); 
	    			queryPanel.pack();
	    			queryPanel.setVisible(true);
	    		}
	    		
	    	});
	    	
	    	btnTurnToSSBNMode.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					if(!mebnController.turnToSSBNMode()){
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resource.getString("NoSSBN"));
					}
				}
	    		
	    	});

	    	btnClearKB.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					mebnController.clearFindingsIntoGUI(); 
					JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resource.getString("KBClean"));
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
	    						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resource.getString("FileSaveOK"));

	    				}
	    			}
	    			
	    			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    		}
	    	}); 
	    	
	    	btnSaveKB.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent ae) {
	    			doSaveKnowledgeBase(); 
	    		}
	    	}); 
	    	
	    	btnLoadKB.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent ae) {
	    			doLoadKnowledgeBase(); 
	    		}});

	        add(btnDoQuery);
	        
	        addSeparator(); 
	        
	        add(btnLoadKB); 
	        add(btnSaveKB); 
	        add(btnClearKB);
	        
	        addSeparator(new Dimension(10, 10)); 
	        
	        add(btnTurnToSSBNMode); 
	        
	        setFloatable(false);
	    }; 
	    
	    /**
	     * Open the JFileChooser for the user enter with the name of file and 
	     * save the knowledge base (only the findings)
	     */
	    private void doSaveKnowledgeBase(){
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			String[] validSufixes = new String[] {PowerLoomKB.FILE_SUFIX};
			
			JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			chooser.addChoosableFileFilter(new SimpleFileFilter(validSufixes,
					resource.getString("powerloomFileFilter")));
			
			int option = chooser.showSaveDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				
				File file = chooser.getSelectedFile();
				String nameFile = file.getAbsolutePath(); 
				if(!(nameFile.substring(nameFile.length() - 4).equals("." + PowerLoomKB.FILE_SUFIX))){
					file = new File(nameFile + "." + PowerLoomKB.FILE_SUFIX); 
				}
				
				if (file != null) {
						mebnController.saveFindingsFile(file);
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resource.getString("FileSaveOK"));
				}
			}
			
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    
	    /**
	     * Load a knowledge base
	     */
	    private void doLoadKnowledgeBase(){
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			String[] validSufixes = new String[] {PowerLoomKB.FILE_SUFIX};
			
			JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			chooser.addChoosableFileFilter(new SimpleFileFilter(validSufixes,
					resource.getString("powerloomFileFilter")));
			
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (chooser.getSelectedFile() != null) {
					
					File file = chooser.getSelectedFile();
					
					try {
						mebnController.loadFindingsFile(file);
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resource.getString("FileLoadOK"));
					} catch (UBIOException e) {
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), e.getMessage());
					} catch (MEBNException e2) {
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), e2.getMessage());
					}
				}
			}
			
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    }
	    
	}
	
  	public class ToolBarEdition extends JToolBar{

  		private final JToggleButton btnResetCursor; 
  	    private final JToggleButton btnAddMFrag;
  	    private final JToggleButton btnAddContextNode;
  	    private final JToggleButton btnAddInputNode;
  	    private final JToggleButton btnAddResidentNode;
  	    private final JToggleButton btnAddEdge;
  	    private final JToggleButton btnAddOrdinaryVariable;
  	    private final JToggleButton btnEditMTheory;
  	  
  	    //by young2
  	    //private final JToggleButton btnSelectObject;
  	    private final JToggleButton btnDeleteSelectedItem; 
  	    
  	    private final ButtonGroup groupEditionButtons; 
  	    
  		public ToolBarEdition(){
  	        btnEditMTheory = new JToggleButton(iconController.getMTheoryNodeIcon());
  	        btnAddEdge               = new JToggleButton(iconController.getEdgeIcon());
  	        btnAddContextNode = new JToggleButton(iconController.getContextNodeIcon());
  	        btnAddInputNode  = new JToggleButton(iconController.getInputNodeIcon());
  	        btnAddResidentNode  = new JToggleButton(iconController.getResidentNodeIcon());
  	        btnAddOrdinaryVariable = new JToggleButton(iconController.getOVariableNodeIcon());
  	        btnAddMFrag		= new JToggleButton(iconController.getMFragIcon());
  	        
  	        //by young2
  	        //btnSelectObject            = new JToggleButton(iconController.getSelectionIcon());
  	        btnResetCursor = new JToggleButton(iconController.getArrowIcon()); 
  	        btnDeleteSelectedItem = new JToggleButton(iconController.getEditDelete());
  	        
  	        btnEditMTheory.setToolTipText(resource.getString("mTheoryEditionTip"));
  	        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
  	        btnAddMFrag.setToolTipText(resource.getString("mFragInsertToolTip"));
  	        btnAddContextNode.setToolTipText(resource.getString("contextNodeInsertToolTip"));
  	        btnAddInputNode.setToolTipText(resource.getString("inputNodeInsertToolTip"));
  	        btnAddResidentNode.setToolTipText(resource.getString("residentNodeInsertToolTip"));
  	     
  	        //by young2
  	        //btnSelectObject.setToolTipText(resource.getString("selectObjectToolTip"));
  	        btnAddOrdinaryVariable.setToolTipText(resource.getString("ordinaryVariableInsertToolTip"));
  	        btnResetCursor.setToolTipText(resource.getString("resetToolTip"));
  	        btnDeleteSelectedItem.setToolTipText(resource.getString("deleteSelectedItemToolTip"));
  	        
  	        groupEditionButtons = new ButtonGroup(); 
  	        groupEditionButtons.add(btnEditMTheory); 
  	        groupEditionButtons.add(btnAddEdge); 
  	        groupEditionButtons.add(btnAddContextNode); 
  	        groupEditionButtons.add(btnAddInputNode); 
  	        groupEditionButtons.add(btnAddResidentNode); 
  	        groupEditionButtons.add(btnAddOrdinaryVariable); 
  	        groupEditionButtons.add(btnAddMFrag); 
  	        //by young2
  	        //groupEditionButtons.add(btnSelectObject); 
  	        groupEditionButtons.add(btnResetCursor); 
  	        groupEditionButtons.add(btnEditMTheory); 
  	        groupEditionButtons.add(btnDeleteSelectedItem);
  	        
  	        add(btnResetCursor); 
  	        addSeparator(); 
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
  	        add(btnDeleteSelectedItem); 
  	        addSeparator();
  	        //by young2
  	        //add(btnSelectObject);
  	        //addSeparator();

  	        btnResetCursor.addActionListener(new ActionListener(){
  	  			public void actionPerformed(ActionEvent ae){
  	  			    mebnController.setActionGraphNone(); 
  	  			}
  	  		});
  	        
  	  		btnEditMTheory.addActionListener(new ActionListener(){
  	  			public void actionPerformed(ActionEvent ae){
  	  				mebnController.enableMTheoryEdition();
  	  			}
  	  		});

  	  		//ao clicar no botão btnAddEdge setamos as variaveis booleanas e os estados dos butoes
  	  		btnAddEdge.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  				mebnController.setActionGraphCreateEdge(); 
  	  			}
  	  		});

  	  		btnAddMFrag.addActionListener(new ActionListener(){
  	  			public void actionPerformed(ActionEvent ae){
  	  				mebnController.insertDomainMFrag();
  	  			}
  	  		});

  	  		//ao clicar no botao node setamos as variaveis booleanas e os estados dos butoes
  	  		btnAddContextNode.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  				mebnController.setActionGraphCreateContextNode(); 
  	  			}
  	  		});

  	  		//ao clicar no botao btnAddInputNode setamos as variaveis booleanas e os estados dos butoes
  	  		btnAddInputNode.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  			   mebnController.setActionGraphCreateInputNode(); 
  	  			}
  	  		});

  	  		//ao clicar no botao btnAddResidentNode setamos as variaveis booleanas e os estados dos butoes
  	  		btnAddResidentNode.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  			   mebnController.setActionGraphCreateResidentNode(); 
  	  			}
  	  		});

  	  		// ao clicar no botao node setamos as variaveis booleanas e os estados dos butoes
  	  		btnAddOrdinaryVariable.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  				mebnController.setActionGraphCreateOrdinaryVariableNode(); 
  	  			}
  	  		});

  	  		btnDeleteSelectedItem.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {

  	  				//by young
  	  				//	mebnController.deleteSelectedItem(); 
  	  				
  	  				//by young
  	  				netWindow.getGraphPane().DeleteSelectedShape(); 
  	  			}
  	  		});

  	  		//by young2
  	  		/*//ao clicar no botao node setamos as variaveis booleanas e os estados dos butoes
  	  		btnSelectObject.addActionListener(new ActionListener() {
  	  			public void actionPerformed(ActionEvent ae) {
  	  				netWindow.getGraphPane().setAction(GraphAction.SELECT_MANY_OBJECTS);
  	  			}
  	  		});*/

  		}

		public void selectBtnResetCursor() {
			btnResetCursor.setSelected(true); 
		}

		public void selectBtnAddMFrag() {
			btnAddMFrag.setSelected(true); 
		}

		public void selectBtnAddContextNode() {
			btnAddContextNode.setSelected(true); 
		}

		public void selectBtnAddInputNode() {
			btnAddInputNode.setSelected(true); 
		}

		public void selectBtnAddResidentNode() {
			btnAddResidentNode.setSelected(true); 
		}

		public void selectBtnAddEdge() {
			btnAddEdge.setSelected(true); 
		}

		public void selectBtnAddOrdinaryVariable() {
			btnAddOrdinaryVariable.setSelected(true); 
		}

		public void selectBtnEditMTheory() {
			btnEditMTheory.setSelected(true); 
		}

		//by young2
		/*
		public void selectBtnSelectObject() {
			btnSelectObject.setSelected(true); 
		}*/

		public void selectBtnDeleteSelectedItem() {
			btnDeleteSelectedItem.setSelected(true); 
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
  	  							try {
									mebnController.renameMTheory(name);
								} catch (DuplicatedNameException e1) {
	  	  							JOptionPane.showMessageDialog(netWindow,
	  	  									resource.getString("nameError"),
	  	  									resource.getString("nameDuplicated"),
	  	  									JOptionPane.ERROR_MESSAGE);
								} catch (ReservedWordException e2) {
	  	  							JOptionPane.showMessageDialog(netWindow,
	  	  									resource.getString("nameError"),
	  	  									resource.getString("nameReserved"),
	  	  									JOptionPane.ERROR_MESSAGE);
								}
  	  						}  else {
  	  							txtNameMTheory.setBackground(MebnToolkit.getColorTextFieldError());
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
  								txtNameMTheory.setBackground(MebnToolkit.getColorTextFieldError());
  								txtNameMTheory.setForeground(Color.WHITE);
  							}
  							else{
  								txtNameMTheory.setBackground(MebnToolkit.getColorTextFieldSelected());
  								txtNameMTheory.setForeground(Color.BLACK);
  							}
  	  				}
  	  				catch(Exception efd){

  	  				}

  	  			}
  	  		});

  	    	add(btnMTheoryActive);

  	    	JToolBar barName = new JToolBar();
//  	    	barName.add(labelMTheoryName);
  	    	barName.add(txtNameMTheory);
  	    	barName.setFloatable(false);
  	    	add(barName);

  	        add(new EmptyPanel());; 
  	        add(new EmptyPanel()); 
  	        add(new EmptyPanel());
  	        
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
      							}
      							catch(DuplicatedNameException dne){
      	  							JOptionPane.showMessageDialog(netWindow,
      	  									resource.getString("nameAlreadyExists"),
      	  									resource.getString("nameException"),
      	  									JOptionPane.ERROR_MESSAGE);
      							}
      							 catch (ReservedWordException e2) {
 	  	  							JOptionPane.showMessageDialog(netWindow,
 	  	  									resource.getString("nameError"),
 	  	  									resource.getString("nameReserved"),
 	  	  									JOptionPane.ERROR_MESSAGE);
 								}
      						}  else {
      							txtNameMFrag.setBackground(MebnToolkit.getColorTextFieldError());
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
    							txtNameMFrag.setBackground(MebnToolkit.getColorTextFieldError());
    							txtNameMFrag.setForeground(Color.WHITE);
    						}
    						else{
    							txtNameMFrag.setBackground(MebnToolkit.getColorTextFieldSelected());
    							txtNameMFrag.setForeground(Color.BLACK);
    						}
      				}
      				catch(Exception efd){

      				}

      			}
      		});

      		add(btnMFragActive);

      		JToolBar barName = new JToolBar();
//      		barName.add(labelMFragName);
      		barName.add(txtNameMFrag);
      		barName.setFloatable(false);
      		add(barName);

  	        add(new EmptyPanel());; 
  	        add(new EmptyPanel()); 
  	        add(new EmptyPanel());
  	        
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

//	        barName.add(labelInputName);
  	  		barName.add(txtNameInput);
  	     	barName.setFloatable(false);
  	        add(barName);

	        txtInputOf = new JTextField(10);
  	        txtInputOf.setEditable(false);

  	        add(new EmptyPanel());; 
  	        add(new EmptyPanel()); 
  	        add(new EmptyPanel());
  	        

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
//  	        barName.add(labelContextName);
  	        barName.add(txtNameContext);
  	        barName.setFloatable(false);

  	        add(btnContextActive);
  	        add(barName);
  	        
  	        add(new EmptyPanel());; 
  	        add(new EmptyPanel()); 
  	        add(new EmptyPanel());
  	        

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
    	private JTextField txtNameResident;

    	public ToolBarResidentNode(){
    		super();

      		btnResidentActive.addActionListener(new ActionListener() {
      			public void actionPerformed(ActionEvent ae) {
      				setResidentNodeTabActive();
      			}
      		});

            txtNameResident = new JTextField(5);

      		txtNameResident.addKeyListener(new KeyAdapter() {
      			public void keyPressed(KeyEvent e) {
      				ResidentNode nodeAux = mebnController.getResidentNodeActive();

      				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtNameResident.getText().length()>0)) {
      					try {
      						String name = txtNameResident.getText(0,txtNameResident.getText().length());
      						matcher = wordPattern.matcher(name);
      						if (matcher.matches()) {
      							mebnController.renameDomainResidentNode(nodeAux, name);
      						}  else {
    							txtNameResident.setBackground(MebnToolkit.getColorTextFieldError());
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
    					} catch (ReservedWordException e2) {
	  							JOptionPane.showMessageDialog(netWindow,
  	  									resource.getString("nameError"),
  	  									resource.getString("nameReserved"),
  	  									JOptionPane.ERROR_MESSAGE);
							}
      				}
      			}

      			public void keyReleased(KeyEvent e){
      				try{
                        String name = txtNameResident.getText(0,txtNameResident.getText().length());
    						matcher = wordPattern.matcher(name);
    						if (!matcher.matches()) {
    							txtNameResident.setBackground(MebnToolkit.getColorTextFieldError());
    						    txtNameResident.setForeground(Color.WHITE);
    						}
    						else{
    							txtNameResident.setBackground(MebnToolkit.getColorTextFieldSelected());
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
//      		barName.add(labelResidentName);
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

      		this.add(new EmptyPanel());
      		this.add(new EmptyPanel()); 
      		this.add(new EmptyPanel());

      		this.setFloatable(false);

    	}

		public JTextField getJTextFieldName() {
			return txtNameResident;
		}

		public void setNameResident(String txtNameResident) {
			this.txtNameResident.setText(txtNameResident);
		}

    }

    private class ResidentPaneOptions extends JToolBar{

  		ResidentPaneOptions(){
  			super(); 
  			setLayout(new GridLayout(1,5)); 
  			
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
  			
      		add(btnStateEdition);
      		add(btnEditArguments);
      		add(btnEditTable);
      		add(new JPanel());
      		add(new JPanel());
  		}
    }
    

  	private class ButtonTab extends JToggleButton{
  		
  		public ButtonTab(ImageIcon image){
  			super(image); 
  			setBackground(MebnToolkit.getColorTabPanelButton()); 
  		}
  		
  	}
  	
  	/**
  	 * A simple panel for fill the empty spaces in the toolbar of the 
  	 * active object. 
  	 * 
  	 * @author Laecio
  	 */
  	private class EmptyPanel extends JButton{
  		
  		public EmptyPanel(){
  			super(); 
  			setEnabled(false); 
  		}
  		
  	}
  	
	public ToolBarOrdVariable getToolBarOVariable() {
		return toolBarOVariable;
	}


	public ToolBarEdition getJtbEdition() {
		return jtbEdition;
	}


	public ButtonGroup getGroupButtonsTabs() {
		return groupButtonsTabs;
	}

	/**
	 * Unselect all the buttons of the groupButtonsTabs (buttons of tabs)
	 */
	public void unselectButtonsGroupButtonsTabs(){
		Enumeration<AbstractButton>  abEnumeration = groupButtonsTabs.getElements();
		while(abEnumeration.hasMoreElements()){
			abEnumeration.nextElement().setSelected(false); 
		}
	}

}