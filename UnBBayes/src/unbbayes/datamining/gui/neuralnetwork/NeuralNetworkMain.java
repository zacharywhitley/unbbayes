package unbbayes.datamining.gui.neuralnetwork;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.util.*;
import java.io.*;
import javax.swing.border.*;
import unbbayes.controller.IconController;
import unbbayes.datamining.gui.*;
import unbbayes.datamining.classifiers.*;


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
  protected JPanel optionsPanel = new OptionsPanel();
  private NeuralNetworkController controller;
  protected AttributePanel attributePanel;

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
        openButton_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
//    saveButton.setToolTipText(resource.getString("saveModelToolTip"));
    saveButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        saveButton_actionPerformed(e);
      }
    });
//    openModelButton.setToolTipText(resource.getString("openModelToolTip"));
    openModelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModelButton_actionPerformed(e);
      }
    });
//    helpButton.setToolTipText(resource.getString("helpFileTooltip"));
    helpButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        helpButton_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
//    learnButton.setToolTipText(resource.getString("learnDataTooltip"));
    learnButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        learnButton_actionPerformed(e);
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
    toolBar.add(openButton, null);
    toolBar.add(learnButton, null);
    toolBar.add(helpButton, null);
    toolBar.add(jLabel1, null);
    toolBar.add(openModelButton, null);
    toolBar.add(saveButton, null);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jTabbedPane1,  BorderLayout.CENTER);
    jTabbedPane1.add(settingsPanel,   "Settings");
    this.getContentPane().add(statusPanel,  BorderLayout.SOUTH);
    statusPanel.add(statusBar, BorderLayout.CENTER);
    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    settingsPanel.add(optionsPanel, BorderLayout.WEST);
    settingsPanel.add(attributePanel,  BorderLayout.CENTER);
  }



  /**
   * Used to set the controller of this class.
   *
   * @param controllercontroller the controller.
   */
  public void setController(NeuralNetworkController controller){
    this.controller = controller;
  }

  void helpButton_actionPerformed(ActionEvent e){
    try{
      controller.help();
    } catch (Exception evt){
      statusBar.setText("Error = " + evt.getMessage() + " " + this.getClass().getName());
    }
  }

  void learnButton_actionPerformed(ActionEvent e){
    try{
      controller.learn();
    } catch (Exception ex){
      statusBar.setText(/*resource.getString*/("exception") + " " + ex.getMessage());
    }

//    jTabbedPane1.setEnabledAt(1,true);
//    jTabbedPane1.setEnabledAt(2,true);
//    jTabbedPane1.setSelectedIndex(1);
    saveButton.setEnabled(true);
  }

  void openButton_actionPerformed(ActionEvent e){
    boolean success;
    try{
      success = controller.openFile();
      if(success){
//        jTabbedPane1.setEnabledAt(0,false);
//        optionsPanel.enableCombos(true);
//        jTabbedPane1.setEnabledAt(0,true);
//        jTabbedPane1.setSelectedIndex(0);
//        jTabbedPane1.setEnabledAt(1,false);
//        jTabbedPane1.setEnabledAt(2,false);
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

  void saveButton_actionPerformed(ActionEvent e){
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

  void openModelButton_actionPerformed(ActionEvent e) {
    boolean success;
    try{
      success = controller.openModel();
      if(success){
 //       jTabbedPane1.setEnabledAt(0,false);
 //       jTabbedPane1.setEnabledAt(1,true);
 //       jTabbedPane1.setEnabledAt(2,true);
        learnButton.setEnabled(false);
        saveButton.setEnabled(false);
       statusBar.setText(/*resource.getString*/("modelOpenedSuccessfully"));
        jTabbedPane1.setSelectedIndex(1);
      }
    } catch (IOException ioe) {
      statusBar.setText(/*resource.getString*/("errorWritingFileException") + " " + ioe.getMessage());
    } catch (ClassNotFoundException cnfe) {
      statusBar.setText(cnfe.getMessage());
    } catch (Exception ex){
      statusBar.setText(ex.getMessage());
    }
  }



}