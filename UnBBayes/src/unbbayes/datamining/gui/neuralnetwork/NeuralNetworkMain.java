package unbbayes.datamining.gui.neuralnetwork;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import unbbayes.controller.*;
import unbbayes.datamining.gui.*;

public class NeuralNetworkMain extends JInternalFrame {
  private JToolBar toolBar = new JToolBar();
  private JLabel label1 = new JLabel();
  private JLabel label2 = new JLabel();
  private JLabel label3 = new JLabel();
  private JButton openButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton openModelButton = new JButton();
  private JButton helpButton = new JButton();
  private JButton learnButton = new JButton();
  private ImageIcon openIcon;
  private ImageIcon compileIcon;
  private ImageIcon helpIcon;
  private ImageIcon saveIcon;
  private ImageIcon advancedOptionsIcon;
  private IconController iconController = IconController.getInstance();
  private JPanel jPanel1 = new JPanel();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JPanel statusPanel = new JPanel();
  private Border border1;
  private TitledBorder titledBorder1;
  private JPanel settingsPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private NeuralNetworkController controller;
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu fileMenu = new JMenu();
  private JMenuItem openMenu = new JMenuItem();
  private JMenuItem openModelMenu = new JMenuItem();
  private JMenuItem saveModelMenu = new JMenuItem();
  private JMenuItem exitMenu = new JMenuItem();
  private JMenu optionsMenu = new JMenu();
  private JMenuItem trainingMenu = new JMenuItem();
  private JMenu helpMenu = new JMenu();
  private JMenuItem helpTopicsMenu = new JMenuItem();
  private JButton advancedOptionsButton = new JButton();
  private JMenuItem advancedOptionsMenu = new JMenuItem();

  protected AttributePanel attributePanel = new AttributePanel();
  protected TrainingPanel chartPanel = new TrainingPanel();
  protected InferencePanel inferencePanel = new InferencePanel();
  protected OptionsPanel optionsPanel = new OptionsPanel();
  protected AdvancedOptionsPanel advancedOptionsPanel = new AdvancedOptionsPanel();

  public NeuralNetworkMain() {
    super("Neural Network",true,true,true,true);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    openIcon = iconController.getOpenIcon();
    compileIcon = iconController.getCompileIcon();
    helpIcon = iconController.getHelpIcon();
    saveIcon = iconController.getSaveIcon();
    advancedOptionsIcon = iconController.getGlobalOptionIcon();
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(border1,"Status");
    openButton.setIcon(openIcon);
    saveButton.setIcon(saveIcon);
    openModelButton.setIcon(openIcon);
    helpButton.setIcon(helpIcon);
    learnButton.setIcon(compileIcon);
    toolBar.setFloatable(false);
    label1.setText("   ");
    label2.setText("   ");
    label3.setText("   ");
//    openButton.setToolTipText(resource.getString("openFileToolTip"));
    openButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        open_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
//    saveButton.setToolTipText(resource.getString("saveModelToolTip"));
    saveButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        saveModel_actionPerformed(e);
      }
    });
//    openModelButton.setToolTipText(resource.getString("openModelToolTip"));
    openModelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModel_actionPerformed(e);
      }
    });
//    helpButton.setToolTipText(resource.getString("helpFileTooltip"));
    helpButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        help_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
//    learnButton.setToolTipText(resource.getString("learnDataTooltip"));
    learnButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        learn_actionPerformed(e);
      }
    });
//    statusBar.setText(resource.getString("welcome"));
    statusPanel.setLayout(borderLayout5);
    statusPanel.setBorder(titledBorder1);
    statusBar.setText("");
    jPanel1.setLayout(borderLayout1);
    settingsPanel.setLayout(borderLayout2);
    borderLayout2.setHgap(4);
    attributePanel.setBorder(BorderFactory.createEtchedBorder());
    fileMenu.setText("Arquivo");
    openMenu.setIcon(openIcon);
    openMenu.setText("Abrir...");
    openMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open_actionPerformed(e);
      }
    });
    openModelMenu.setIcon(openIcon);
    openModelMenu.setText("Abrir Modelo...");
    openModelMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModel_actionPerformed(e);
      }
    });
    saveModelMenu.setEnabled(false);
    saveModelMenu.setIcon(saveIcon);
    saveModelMenu.setText("Salvar Modelo...");
    saveModelMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveModel_actionPerformed(e);
      }
    });
    exitMenu.setText("Sair");
    exitMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exitMenu_actionPerformed(e);
      }
    });
    optionsMenu.setEnabled(false);
    optionsMenu.setIcon(null);
    optionsMenu.setText("Opções");
    trainingMenu.setIcon(compileIcon);
    trainingMenu.setText("Treinar Rede Neural");
    trainingMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        learn_actionPerformed(e);
      }
    });
    helpMenu.setText("Ajuda");
    helpTopicsMenu.setIcon(helpIcon);
    helpTopicsMenu.setText("Tópicos de Ajuda");
    helpTopicsMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        help_actionPerformed(e);
      }
    });

    advancedOptionsButton.setEnabled(false);
    advancedOptionsButton.setIcon(advancedOptionsIcon);
    advancedOptionsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        advancedOptions_actionPerformed(e);
      }
    });
    advancedOptionsMenu.setEnabled(true);
    advancedOptionsMenu.setIcon(advancedOptionsIcon);
    advancedOptionsMenu.setText("Opções Avançadas");
    advancedOptionsMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        advancedOptions_actionPerformed(e);
      }
    });
    toolBar.add(openButton, null);
    toolBar.add(learnButton, null);
    toolBar.add(label1, null);
    toolBar.add(advancedOptionsButton, null);
    toolBar.add(label2, null);
    toolBar.add(openModelButton, null);
    toolBar.add(saveButton, null);
    toolBar.add(label3, null);
    toolBar.add(helpButton, null);

    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jTabbedPane1,  BorderLayout.CENTER);
    jTabbedPane1.add(settingsPanel,   "Settings");
    this.getContentPane().add(statusPanel,  BorderLayout.SOUTH);
    statusPanel.add(statusBar, BorderLayout.CENTER);
    settingsPanel.add(optionsPanel, BorderLayout.WEST);
    settingsPanel.add(attributePanel,  BorderLayout.CENTER);
    jTabbedPane1.add(chartPanel,   "Chart");
    jTabbedPane1.add(inferencePanel,   "Inference");
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(optionsMenu);
    jMenuBar1.add(helpMenu);
    fileMenu.add(openMenu);
    fileMenu.addSeparator();
    fileMenu.add(openModelMenu);
    fileMenu.add(saveModelMenu);
    fileMenu.addSeparator();
    fileMenu.add(exitMenu);
    optionsMenu.add(trainingMenu);
    optionsMenu.add(advancedOptionsMenu);
    helpMenu.add(helpTopicsMenu);
    this.setJMenuBar(jMenuBar1);
    jTabbedPane1.setEnabledAt(1,false);
    jTabbedPane1.setEnabledAt(2,false);
    jTabbedPane1.setSelectedIndex(0);
  }

  /**
   * Used to set the controller of this class.
   *
   * @param controllercontroller the controller.
   */
  public void setController(NeuralNetworkController controller){
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
    jTabbedPane1.setEnabledAt(0,false);
    jTabbedPane1.setEnabledAt(1,true);
    jTabbedPane1.setSelectedIndex(1);
    jTabbedPane1.setEnabledAt(2,true);
    saveButton.setEnabled(true);
    saveModelMenu.setEnabled(true);
    learnButton.setEnabled(false);
    advancedOptionsButton.setEnabled(false);
    optionsMenu.setEnabled(false);

    try{
      controller.learn();
    } catch (Exception ex){
      statusBar.setText(/*resource.getString*/("exception") + " " + ex.getMessage());
      jTabbedPane1.setEnabledAt(0,true);
      jTabbedPane1.setEnabledAt(1,false);
      jTabbedPane1.setSelectedIndex(0);
      jTabbedPane1.setEnabledAt(2,false);
      saveButton.setEnabled(false);
      saveModelMenu.setEnabled(false);
      learnButton.setEnabled(true);
      advancedOptionsButton.setEnabled(true);
      optionsMenu.setEnabled(true);
    }
  }

  void open_actionPerformed(ActionEvent e){
    boolean success;
    try{
      success = controller.openFile();
      if(success){
        jTabbedPane1.setEnabledAt(0,false);
        optionsPanel.setEnabled(true);
        jTabbedPane1.setEnabledAt(0,true);
        jTabbedPane1.setSelectedIndex(0);
        jTabbedPane1.setEnabledAt(1,false);
        jTabbedPane1.setEnabledAt(2,false);
        learnButton.setEnabled(true);
        advancedOptionsButton.setEnabled(true);
        optionsMenu.setEnabled(true);
        saveButton.setEnabled(false);
        saveModelMenu.setEnabled(false);
        statusBar.setText(/*resource.getString*/("openFile"));
      }
 //   }catch (NullPointerException npe){
 //     statusBar.setText(/*resource.getString*/("errorDB") + " " + npe.getMessage());
 //   }catch (FileNotFoundException fnfe){
 //     statusBar.setText(/*resource.getString*/("fileNotFound") + " " + fnfe.getMessage());
 //   }catch (IOException ioe){
 //     statusBar.setText(/*resource.getString*/("errorOpen") + " " + ioe.getMessage());
    }catch (Exception ex){
      ex.printStackTrace();
      statusBar.setText(/*resource.getString*/("error") + ex.getMessage());
    }
  }

  void saveModel_actionPerformed(ActionEvent e){
    boolean success;
    try{
      success = controller.saveModel();
      if(success){
        statusBar.setText(/*resource.getString*/("saveModel"));
      }
    } catch (Exception ioe) {
      statusBar.setText(/*resource.getString*/("errorWritingFileException") + " " + ioe.getMessage());
    }
  }

  void openModel_actionPerformed(ActionEvent e) {
    boolean success;
    try{
      success = controller.openModel();
      if(success){
        jTabbedPane1.setEnabledAt(0,false);
        jTabbedPane1.setEnabledAt(1,false);
        jTabbedPane1.setEnabledAt(2,true);
        jTabbedPane1.setSelectedIndex(2);
        learnButton.setEnabled(false);
        advancedOptionsButton.setEnabled(false);
        optionsMenu.setEnabled(false);
        saveButton.setEnabled(false);
        saveModelMenu.setEnabled(false);
        statusBar.setText(/*resource.getString*/("modelOpenedSuccessfully"));
        jTabbedPane1.setSelectedIndex(2);
      }
    } catch (IOException ioe) {
      statusBar.setText(/*resource.getString*/("errorWritingFileException") + " " + ioe.getMessage());
    } catch (ClassNotFoundException cnfe) {
      statusBar.setText(cnfe.getMessage());
    } catch (Exception ex){
      statusBar.setText(ex.getMessage());
    }
  }

  void exitMenu_actionPerformed(ActionEvent e) {
    dispose();
  }

  void advancedOptions_actionPerformed(ActionEvent e) {
    advancedOptionsPanel.sethiddenLayerSize(controller.getHiddenLayerSize());
    this.hide();
    int options = JOptionPane.showInternalOptionDialog(this, advancedOptionsPanel, "Advanced Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    if(options == JOptionPane.OK_OPTION){
      advancedOptionsPanel.updateValues();
    }
    this.show();
  }
}