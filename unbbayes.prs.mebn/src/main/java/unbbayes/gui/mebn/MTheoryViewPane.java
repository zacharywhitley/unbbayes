/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.GraphPane;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ResourceController;

/**
 * Pane for viewing all MFrags together.
 *
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - (feature:3317031)
 */

public class MTheoryViewPane extends JPanel {

	private static final long serialVersionUID = 6194855055129252835L;
	
	private final MEBNNetworkWindow netWindow;

	/* Mostra o painel de edicao do objeto ativo */
	private JPanel tabsPanel;

	/*
	 * Panel que contem:
	 * - O painel de edicao do objeto atual
	 * - O grafo de edicao
	 */
	private JSplitPane centerPanel;

	/*---- TabPanel and tabs ----*/
	private JToolBar jtbTabSelection;
    private JPanel jpTabSelected;

    private MTheoryViewTree mTheoryTree;

	private JScrollPane mTheoryTreeScroll;

    /* Text fields */
    private final MEBNController mebnController;
    private final JSplitPane graphPanel;
    private final JLabel status;
    private final JPanel bottomPanel;

    private final JPanel topPanel;

    private final CardLayout cardLayout = new CardLayout();

    /* Buttons for select the active tab */
    private ButtonGroup groupButtonsTabs = new ButtonGroup(); 
    private final JToggleButton btnTabOptionTree;

    private final String MTHEORY_TREE_TAB = "MTheoryTree";
    
    /* MTheory graph panels */
    private JViewport graphViewport = null;

	private GraphPane graphPane = null;

	private JScrollPane jspGraph = null;

    /* Icon Controller */
    private final IconController iconController = IconController.getInstance();
    
	/* Resource bundle */
  	private ResourceBundle resource; 

  	public MTheoryViewPane(MEBNNetworkWindow _netWindow,
            MEBNController _controller) {
        this.netWindow     = _netWindow;
        this.mebnController    = _controller;
        
        this.resource = ResourceController.newInstance().getBundle(
        		unbbayes.gui.mebn.resources.Resources.class.getName(),
    			Locale.getDefault(),
    			this.getClass().getClassLoader());
        
        this.setLayout(new BorderLayout());
        topPanel    = new JPanel(new GridLayout(1,1));

        tabsPanel = new JPanel(new BorderLayout());

        jpTabSelected = new JPanel(cardLayout);
        jtbTabSelection = new JToolBar();

        graphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        graphPanel.setDividerSize(1);
        
        setUpGraphPane();

        bottomPanel = new JPanel(new GridLayout(1,1));
        status      = new JLabel(resource.getString("statusReadyLabel"));

        btnTabOptionTree = new ButtonTab(iconController.getMTheoryNodeIcon());

        btnTabOptionTree.setToolTipText(resource.getString("showMTheoryToolTip"));

        groupButtonsTabs.add(btnTabOptionTree); 
        
        addActionListenersToButtons();

        topPanel.add(new ToolBarGlobalOptions());
        
        
        /*---- jtbEmpty ----*/
        JTextField txtIsEmpty = new JTextField(resource.getString("whithotMFragActive"));
        txtIsEmpty.setEditable(false);

        bottomPanel.add(status);
        
        jtbTabSelection.setLayout(new GridLayout(1,5));
        jtbTabSelection.add(btnTabOptionTree);
        jtbTabSelection.setFloatable(false);
        
        /*---------------- Tab panel ----------------------*/

        mTheoryTree = new MTheoryViewTree(mebnController, (MTheoryGraphPane)getGraphPane());
        mTheoryTreeScroll = new JScrollPane(mTheoryTree);
        mTheoryTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
        mTheoryTreeScroll.setBorder(MebnToolkit.getBorderForTabPanel(
        		resource.getString("MTheoryTreeTitle")));
        jpTabSelected.add(MTHEORY_TREE_TAB, mTheoryTreeScroll);

        cardLayout.show(jpTabSelected, MTHEORY_TREE_TAB);

        /*------------------- Left panel ---------------------*/

        tabsPanel.add(BorderLayout.NORTH, jtbTabSelection);
        tabsPanel.add(BorderLayout.CENTER, jpTabSelected);
        
        centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabsPanel, getJspGraph());
        centerPanel.setDividerSize(7);
        updateToPreferredSize(); 

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(centerPanel, BorderLayout.CENTER);

        setVisible(true);
    }
  	
  	private void setUpGraphPane() {
  		
  		this.setGraphViewport(new JViewport());
  		
  		GraphPane pane = new MTheoryGraphPane(mebnController, this.getGraphViewport());
  		
		this.setGraphPane(pane);
		
		this.getGraphPane().addKeyListener(mebnController);
		
		this.setJspGraph(new JScrollPane(this.getGraphViewport()));
		
		this.getJspGraph().getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						getGraphPane().update();
					}
				});

		this.getJspGraph().getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						getGraphPane().update();
					}
				});

		// set default values for jspGraph
		this.getJspGraph().setHorizontalScrollBar(this.getJspGraph().createHorizontalScrollBar());
		this.getJspGraph().setVerticalScrollBar(this.getJspGraph().createVerticalScrollBar());
		this.getJspGraph().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.getJspGraph().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
  	}
  	
  	public void updateToPreferredSize() {

  		
  		int width = tabsPanel.getPreferredSize().width + centerPanel.getDividerSize() + 20;
  		if (width < 200) {
  			width = 200;
  		}
        centerPanel.setDividerLocation(width);
    }


  	private void addActionListenersToButtons(){

  		//ao clicar no botao btnGlobalOption, mostra-se o menu para escolha das opcoes
  		btnTabOptionTree.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				setMTheoryTreeActive();
  			}
  		});

//		// trigger action when we choose a tab to switch between different edition panes (e.g. from plugin to default edition pane)
//		this.addPropertyChangeListener(
//				IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, 
//				new PropertyChangeListener() {
//					public void propertyChange(PropertyChangeEvent evt) {
//						try {
//							// update MEBNEditionPane's entity panel (it seems that this is the only one not being updated...)
//							getToolBarOVariable().updateListOfTypes();
//							getToolBarOVariable().updateUI();
//							getToolBarOVariable().repaint();
//							updateUI();
//							repaint();
//							// add code here to do something when user change tabs
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				});

  	}

  	private class ButtonTab extends JToggleButton{
  		
  		public ButtonTab(ImageIcon image){
  			super(image); 
  			setBackground(MebnToolkit.getColorTabPanelButton()); 
  		}
  		
  	}

  	/**
  	 * Show the table edit in the top component. 
  	 */
    public void showTableEditionPane(ResidentNode resident){
    	mebnController.openCPTDialog(resident); 
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
//    	titleMFragPane.add(nodeSelectedToolBar, BorderLayout.PAGE_START); 	
    	titleMFragPane.add(tb2, BorderLayout.PAGE_END); 	
    	
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

    /**
     *  Retorna o painel do centro onde fica o graph e a table.
     *
     *@return    retorna o centerPanel (<code>JSplitPane</code>)
     *@see       JSplitPane
     */
    public JSplitPane getGraphPanel() {
      return this.graphPanel;
    }

    public MTheoryViewTree getMTheoryTree(){
    	return mTheoryTree;
    }
    
    public void refreshMTheoryTree() {
    	mTheoryTree = new MTheoryViewTree(mebnController, (MTheoryGraphPane)getGraphPane());
    	mTheoryTreeScroll.setViewportView(mTheoryTree);
    }

    /* TabPanel */

    public void setMTheoryTreeActive(){
        cardLayout.show(jpTabSelected, "MTheoryTree");
//        mTheoryTree.updateTree();
    }

    /*---------------------------------------------------------*/

	public MEBNNetworkWindow getNetworkWindow() {
		return netWindow;
	}
	
	/**
	 * Contains geral mtheory buttons: 
	 * - turn to ssbn/edition mode
	 * - save graph to image
	 */
	private class ToolBarGlobalOptions extends JToolBar{

		private static final long serialVersionUID = 1L;

	    private JButton btnTurnToEditMode; 
	    
//	    private JButton btnTurnToMTheoryMode;
	    
		private JButton btnSaveNetImage;

	    public ToolBarGlobalOptions(){
	    	
	    	super(); 
	    	
	    	btnTurnToEditMode = new JButton(iconController.getEditIcon()); 
//	    	btnTurnToMTheoryMode = new JButton(iconController.getSsbnIcon());

	    	btnSaveNetImage = new JButton(iconController.getSaveNetIcon());
	    	
	    	btnTurnToEditMode.setToolTipText(resource.getString("editToolTip"));
//	    	btnTurnToMTheoryMode.setToolTipText(resource.getString("turnToSSBNModeToolTip"));
	    	
	    	btnSaveNetImage.setToolTipText(resource.getString("saveNetImageToolTip"));
	    	
	    	btnTurnToEditMode.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					netWindow.changeToMEBNEditionPane();
				}
	    		
	    	});
	    	
//	    	btnTurnToMTheoryMode.addActionListener(new ActionListener(){
//
//				public void actionPerformed(ActionEvent e) {
//					if(!mebnController.turnToMTheoryMode()){
//						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), resource.getString("NoSSBN"));
//					}
//				}
//	    		
//	    	});

	    	btnSaveNetImage.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	mebnController.saveNetImage(getGraphPane());
	            }
	        });
	    	
	        
	        add(btnTurnToEditMode); 
//	        add(btnTurnToMTheoryMode); 
	        
	        addSeparator(new Dimension(10, 10)); 
	        
	        add(btnSaveNetImage);
	        
	        setFloatable(false);
	    }; 
	    
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

	/**
	 * @return the mTheoryTreeScroll
	 */
	public JScrollPane getMTheoryTreeScroll() {
		return mTheoryTreeScroll;
	}

	/**
	 * @param theoryTreeScroll the mTheoryTreeScroll to set
	 */
	public void setMTheoryTreeScroll(JScrollPane theoryTreeScroll) {
		mTheoryTreeScroll = theoryTreeScroll;
	}

	/**
	 * @return the tabsPanel
	 */
	public JPanel getTabsPanel() {
		return tabsPanel;
	}

	/**
	 * @param tabsPanel the tabsPanel to set
	 */
	public void setTabsPanel(JPanel tabsPanel) {
		this.tabsPanel = tabsPanel;
	}

	/**
	 * @return the jtbTabSelection
	 */
	public JToolBar getJtbTabSelection() {
		return jtbTabSelection;
	}

	/**
	 * @param jtbTabSelection the jtbTabSelection to set
	 */
	public void setJtbTabSelection(JToolBar jtbTabSelection) {
		this.jtbTabSelection = jtbTabSelection;
	}

	/**
	 * @return the btnTabOptionTree
	 */
	public JToggleButton getBtnTabOptionTree() {
		return btnTabOptionTree;
	}

	/**
	 * @return the topPanel
	 */
	public JPanel getTopPanel() {
		return topPanel;
	}

	/**
	 * @return the cardLayout
	 */
	public CardLayout getCardLayout() {
		return cardLayout;
	}

	/**
	 * @return the jpTabSelected
	 */
	public JPanel getJpTabSelected() {
		return jpTabSelected;
	}

	/**
	 * @param jpTabSelected the jpTabSelected to set
	 */
	public void setJpTabSelected(JPanel jpTabSelected) {
		this.jpTabSelected = jpTabSelected;
	}

	/**
	 * @return the mebnController
	 */
	public MEBNController getMebnController() {
		return mebnController;
	}
	
	public JViewport getGraphViewport() {
		return graphViewport;
	}

	public void setGraphViewport(JViewport graphViewport) {
		this.graphViewport = graphViewport;
	}

	public GraphPane getGraphPane() {
		return graphPane;
	}

	public void setGraphPane(GraphPane graphPane) {
		this.graphPane = graphPane;
	}

	public JScrollPane getJspGraph() {
		return jspGraph;
	}

	public void setJspGraph(JScrollPane jspGraph) {
		this.jspGraph = jspGraph;
	}

}