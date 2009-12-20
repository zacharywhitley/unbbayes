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
package unbbayes.datamining.gui.neuralmodel;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import unbbayes.controller.IconController;
import unbbayes.datamining.gui.AttributePanel;

/**
 *  Class that implements the CNM framework start screen.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class NeuralModelMain extends JInternalFrame{
  
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();
  /** Carrega o arquivo de recursos para internacionaliza��o da localidade padr�o */
  private ResourceBundle resource;
  private JToolBar jToolBar1 = new JToolBar();
  private JButton helpButton = new JButton();
  private JButton learnButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton openButton = new JButton();
  private ImageIcon openIcon;
  private ImageIcon openModelIcon;
  private ImageIcon compileIcon;
  private ImageIcon helpIcon;
  private ImageIcon saveIcon;
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private JPanel tabbedPaneAttributes = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JLabel statusBar = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();
  private JPanel panelOptions = new JPanel();
  private BorderLayout borderLayout8 = new BorderLayout();
  private JPanel tabbedPanelClassify = new JPanel();
  private BorderLayout borderLayout11 = new BorderLayout();
  private JLabel jLabel1 = new JLabel();
  private JButton openModelButton = new JButton();
  private NeuralModelController controller = null;
  protected OptionsPanel optionsPanel;
  protected JPanel tabbedPaneRules = new JPanel();
  protected AttributePanel attributePanel;
  protected RulesPanel rulesPanel;
  protected InferencePanel inferencePanel = new InferencePanel();
  protected IconController iconController = IconController.getInstance();
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu fileMenu = new JMenu();
  JMenuItem openMenu = new JMenuItem();
  JMenuItem openModelMenu = new JMenuItem();
  JMenuItem saveModelMenu = new JMenuItem();
  JMenuItem exitMenu = new JMenuItem();
  JMenu learnMenu = new JMenu();
  JMenuItem learnModelMenu = new JMenuItem();
  JMenu helpMenu = new JMenu();
  JMenuItem helpTopicsMenu = new JMenuItem();
  JLabel jLabel2 = new JLabel();

  /**
   * Construct the frame.
   *
   * @param controller the behaviour controller of the framework.
   */
  public NeuralModelMain(NeuralModelController controller){
    super("Combinatorial Neural Model",true,true,true,true);
    this.controller = controller;
    resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource.class.getName());
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try{
      jbInit();
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception{
    openIcon = iconController.getOpenIcon();
    openModelIcon = iconController.getOpenModelIcon();
    compileIcon = iconController.getCompileIcon();
    helpIcon = iconController.getHelpIcon();
    saveIcon = iconController.getSaveIcon();
    contentPane = (JPanel) this.getContentPane();
    titledBorder1 = new TitledBorder(border1,"Status");
    this.setSize(new Dimension(640, 521));
    openButton.setToolTipText(resource.getString("openFileToolTip"));
    openButton.setIcon(openIcon);
    openButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        open_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
    saveButton.setToolTipText(resource.getString("saveModelToolTip"));
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        save_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
    learnButton.setToolTipText(resource.getString("learnDataTooltip"));
    learnButton.setIcon(compileIcon);
    learnButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        learn_actionPerformed(e);
      }
    });
    helpButton.setToolTipText(resource.getString("helpFileTooltip"));
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        help_actionPerformed(e);
      }
    });
    openModelButton.setToolTipText(resource.getString("openModelToolTip"));
    openModelButton.setIcon(openModelIcon);
    openModelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModel_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    jPanel2.setLayout(borderLayout5);
    jPanel3.setLayout(borderLayout6);
    statusBar.setText(resource.getString("welcome"));
    jPanel2.setBorder(titledBorder1);
    tabbedPaneRules.setLayout(borderLayout2);
    tabbedPaneAttributes.setLayout(borderLayout7);
    panelOptions.setLayout(borderLayout8);
    optionsPanel = new OptionsPanel();
    tabbedPanelClassify.setLayout(borderLayout11);

    fileMenu.setText(resource.getString("fileMenu"));
    openMenu.setText(resource.getString("openMenu"));
    openMenu.setIcon(openIcon);
    openMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open_actionPerformed(e);
      }
    });
    openModelMenu.setText(resource.getString("openModelMenu"));
    openModelMenu.setIcon(openModelIcon);
    openModelMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModel_actionPerformed(e);
      }
    });
    saveModelMenu.setText(resource.getString("saveModelMenu"));
    saveModelMenu.setEnabled(false);
    saveModelMenu.setFocusPainted(false);
    saveModelMenu.setIcon(saveIcon);
    saveModelMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save_actionPerformed(e);
      }
    });
    exitMenu.setText(resource.getString("exitMenu"));
    exitMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exitMenu_actionPerformed(e);
      }
    });
    learnMenu.setEnabled(false);
    learnMenu.setText(resource.getString("learnMenu"));
    learnModelMenu.setText(resource.getString("learnModelMenu"));
    learnModelMenu.setIcon(compileIcon);
    learnModelMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        learn_actionPerformed(e);
      }
    });
    helpMenu.setText(resource.getString("helpMenu"));
    helpTopicsMenu.setText(resource.getString("helpTopicsMenu"));
    helpTopicsMenu.setIcon(helpIcon);
    helpTopicsMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        help_actionPerformed(e);
      }
    });
    jLabel1.setToolTipText("");
    jLabel2.setText("   ");
    jLabel1.setText("   ");
    jToolBar1.add(openButton, null);
    jToolBar1.add(learnButton, null);
    jToolBar1.add(jLabel1, null);
    jToolBar1.add(openModelButton, null);
    jToolBar1.add(saveButton, null);
    jToolBar1.add(jLabel2, null);
    jToolBar1.add(helpButton, null);
    contentPane.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    attributePanel = new AttributePanel();
    tabbedPaneAttributes.add(attributePanel,  BorderLayout.CENTER);
    jTabbedPane1.add(tabbedPaneAttributes, resource.getString("tabbedPaneAttributes"));
    jTabbedPane1.add(tabbedPaneRules, resource.getString("tabbedPaneRules"));
    jTabbedPane1.add(tabbedPanelClassify, resource.getString("tabbedPanelClassify"));
    tabbedPanelClassify.add(inferencePanel);
    rulesPanel = new RulesPanel(controller);
    tabbedPaneRules.add(rulesPanel, BorderLayout.CENTER);
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    this.setJMenuBar(jMenuBar1);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    panelOptions.add(optionsPanel,  BorderLayout.CENTER);
    tabbedPaneAttributes.add(panelOptions,  BorderLayout.SOUTH);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(learnMenu);
    jMenuBar1.add(helpMenu);
    fileMenu.add(openMenu);
    fileMenu.addSeparator();
    fileMenu.add(openModelMenu);
    fileMenu.add(saveModelMenu);
    fileMenu.addSeparator();
    fileMenu.add(exitMenu);
    learnMenu.add(learnModelMenu);
    helpMenu.add(helpTopicsMenu);
    jTabbedPane1.setEnabledAt(0,false);
    jTabbedPane1.setEnabledAt(1,false);
    jTabbedPane1.setEnabledAt(2,false);
  }

  /**
   * Used to set the controller of this class.
   *
   * @param controllercontroller the controller.
   */
  public void setController(NeuralModelController controller){
    this.controller = controller;
  }

  void help_actionPerformed(ActionEvent e){
    try{
      controller.help();
    } catch (Exception evt){
      statusBar.setText("Error = " + evt.getMessage() + " " + this.getClass().getName());
    }
  }

  void learn_actionPerformed(ActionEvent e){
    try{
      controller.learn();
    } catch (Exception ex){
      statusBar.setText(resource.getString("exception") + " " + ex.getMessage());
    }

    jTabbedPane1.setEnabledAt(1,true);
    jTabbedPane1.setEnabledAt(2,true);
    jTabbedPane1.setSelectedIndex(1);
    saveButton.setEnabled(true);
    saveModelMenu.setEnabled(true);
  }

  void open_actionPerformed(ActionEvent e){
    boolean success;
    try{
      success = controller.openFile();
      if(success){
        jTabbedPane1.setEnabledAt(0,false);
        optionsPanel.enableCombos(true);
        jTabbedPane1.setEnabledAt(0,true);
        jTabbedPane1.setSelectedIndex(0);
        jTabbedPane1.setEnabledAt(1,false);
        jTabbedPane1.setEnabledAt(2,false);
        learnButton.setEnabled(true);
        learnMenu.setEnabled(true);
        saveButton.setEnabled(false);
        saveModelMenu.setEnabled(false);
        statusBar.setText(resource.getString("openFile"));
      }
    }catch (NullPointerException npe){
      statusBar.setText(resource.getString("errorDB") + " " + npe.getMessage());
    }catch (FileNotFoundException fnfe){
      statusBar.setText(resource.getString("fileNotFound") + " " + fnfe.getMessage());
    }catch (IOException ioe){
      statusBar.setText(resource.getString("errorOpen") + " " + ioe.getMessage());
    }catch (Exception ex){
      statusBar.setText(resource.getString("error") + ex.getMessage());
    }
  }

  void save_actionPerformed(ActionEvent e){
    boolean success;
    try{
      success = controller.saveModel();
      if(success){
        statusBar.setText(resource.getString("saveModel"));
      }
    } catch (Exception ioe) {
      statusBar.setText(resource.getString("errorWritingFileException") + " " + ioe.getMessage());
    }
  }

  void openModel_actionPerformed(ActionEvent e) {
    boolean success;
    try{
      success = controller.openModel();
      if(success){
        jTabbedPane1.setEnabledAt(0,false);
        jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setEnabledAt(2,true);
        learnButton.setEnabled(false);
        learnMenu.setEnabled(false);
        saveButton.setEnabled(false);
        saveModelMenu.setEnabled(false);
        statusBar.setText(resource.getString("modelOpenedSuccessfully"));
        jTabbedPane1.setSelectedIndex(1);
      }
    } catch (IOException ioe) {
      statusBar.setText(resource.getString("errorWritingFileException") + " " + ioe.getMessage());
    } catch (ClassNotFoundException cnfe) {
      statusBar.setText(cnfe.getMessage());
    }
  }

  void exitMenu_actionPerformed(ActionEvent e) {
    dispose();
  }

}