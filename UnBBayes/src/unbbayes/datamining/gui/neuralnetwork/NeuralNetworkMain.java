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
  private JLabel jLabel1 = new JLabel();
  private JButton openButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton openModelButton = new JButton();
  private JButton helpButton = new JButton();
  private JButton learnButton = new JButton();
  private ImageIcon openIcon;
  private ImageIcon compileIcon;
  private ImageIcon helpIcon;
  private ImageIcon saveIcon;
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
  protected InferencePanel inferencePanel = new InferencePanel();
  protected OptionsPanel optionsPanel = new OptionsPanel();
  private NeuralNetworkController controller;
  protected AttributePanel attributePanel;
  JPanel jPanel2 = new JPanel();
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu fileMenu = new JMenu();
  JMenuItem openMenu = new JMenuItem();
  JMenuItem openModelMenu = new JMenuItem();
  JMenuItem saveModelMenu = new JMenuItem();
  JMenuItem exitMenu = new JMenuItem();
  JMenu learnMenu = new JMenu();
  JMenuItem trainingMenu = new JMenuItem();
  JMenu helpMenu = new JMenu();
  JMenuItem helpTopicsMenu = new JMenuItem();

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
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(border1,"Status");
    attributePanel = new AttributePanel();
    openButton.setIcon(openIcon);
    saveButton.setIcon(saveIcon);
    openModelButton.setIcon(openIcon);
    helpButton.setIcon(helpIcon);
    learnButton.setIcon(compileIcon);
    toolBar.setFloatable(false);
    jLabel1.setText("   ");
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
    learnMenu.setIcon(null);
    learnMenu.setText("Aprendizagem");
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
    toolBar.add(openButton, null);
    toolBar.add(learnButton, null);
    toolBar.add(helpButton, null);
    toolBar.add(jLabel1, null);
    toolBar.add(openModelButton, null);
    toolBar.add(saveButton, null);
    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jTabbedPane1,  BorderLayout.CENTER);
    jTabbedPane1.add(settingsPanel,   "Settings");
    this.getContentPane().add(statusPanel,  BorderLayout.SOUTH);
    statusPanel.add(statusBar, BorderLayout.CENTER);
    settingsPanel.add(optionsPanel, BorderLayout.WEST);
    settingsPanel.add(attributePanel,  BorderLayout.CENTER);
    jTabbedPane1.add(jPanel2,  "jPanel2");
    jTabbedPane1.add(inferencePanel,   "Inference");
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(learnMenu);
    jMenuBar1.add(helpMenu);
    fileMenu.add(openMenu);
    fileMenu.addSeparator();
    fileMenu.add(openModelMenu);
    fileMenu.add(saveModelMenu);
    fileMenu.addSeparator();
    fileMenu.add(exitMenu);
    learnMenu.add(trainingMenu);
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
    try{
      controller.learn();
    } catch (Exception ex){
      statusBar.setText(/*resource.getString*/("exception") + " " + ex.getMessage());
    }

//    jTabbedPane1.setEnabledAt(1,true);    ///que vai conter o gráfico
    jTabbedPane1.setEnabledAt(2,true);
    jTabbedPane1.setSelectedIndex(2);
    saveButton.setEnabled(true);
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
        saveButton.setEnabled(false);
        statusBar.setText(/*resource.getString*/("openFile"));
      }
    }catch (NullPointerException npe){
      statusBar.setText(/*resource.getString*/("errorDB") + " " + npe.getMessage());
    }catch (FileNotFoundException fnfe){
      statusBar.setText(/*resource.getString*/("fileNotFound") + " " + fnfe.getMessage());
    }catch (IOException ioe){
      statusBar.setText(/*resource.getString*/("errorOpen") + " " + ioe.getMessage());
    }catch (Exception ex){
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
 //       jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setEnabledAt(2,true);
        learnButton.setEnabled(false);
        saveButton.setEnabled(false);
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
}