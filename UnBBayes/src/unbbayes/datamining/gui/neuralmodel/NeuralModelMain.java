package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import unbbayes.controller.IconController;
import unbbayes.datamining.gui.*;

/**
 *  Class that implements the CNM framework start screen.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class NeuralModelMain extends JInternalFrame{
  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();
  /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;
  private JToolBar jToolBar1 = new JToolBar();
  private JButton helpButton = new JButton();
  private JButton learnButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton openButton = new JButton();
  private ImageIcon openIcon;
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

  /**
   * Construct the frame.
   *
   * @param controller the behaviour controller of the framework.
   */
  public NeuralModelMain(NeuralModelController controller){
    super("Combinatorial Neural Model",true,true,true,true);
    this.controller = controller;
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource");
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try{
      jbInit();
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception{
    openIcon = iconController.getOpenIcon();
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
        openButton_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
    saveButton.setToolTipText(resource.getString("saveModelToolTip"));
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        saveButton_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
    learnButton.setToolTipText(resource.getString("learnDataTooltip"));
    learnButton.setIcon(compileIcon);
    learnButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        learnButton_actionPerformed(e);
      }
    });
    helpButton.setToolTipText(resource.getString("helpFileTooltip"));
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        helpButton_actionPerformed(e);
      }
    });
    openModelButton.setToolTipText(resource.getString("openModelToolTip"));
    openModelButton.setIcon(openIcon);
    openModelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModelButton_actionPerformed(e);
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
    jLabel1.setText("   ");
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(openButton, null);
    jToolBar1.add(learnButton, null);
    jToolBar1.add(helpButton, null);
    jToolBar1.add(jLabel1, null);
    jToolBar1.add(openModelButton, null);
    jToolBar1.add(saveButton, null);
    contentPane.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    attributePanel = new AttributePanel();
    tabbedPaneAttributes.add(attributePanel,  BorderLayout.CENTER);
    jTabbedPane1.add(tabbedPaneAttributes, resource.getString("attributes"));
    jTabbedPane1.add(tabbedPaneRules, resource.getString("rules"));
    jTabbedPane1.add(tabbedPanelClassify, resource.getString("classify"));
    tabbedPanelClassify.add(inferencePanel);
    rulesPanel = new RulesPanel(controller);
    tabbedPaneRules.add(rulesPanel, BorderLayout.CENTER);
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    panelOptions.add(optionsPanel,  BorderLayout.CENTER);
    tabbedPaneAttributes.add(panelOptions,  BorderLayout.SOUTH);
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

  void helpButton_actionPerformed(ActionEvent e){
    try{
      controller.help();
    } catch (Exception evt){
      statusBar.setText("Error = " + evt.getMessage() + " " + this.getClass().getName());
    }
  }

  void learnButton_actionPerformed(ActionEvent e){
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    try{
      controller.learn();
    } catch (Exception ex){
      statusBar.setText(resource.getString("exception") + " " + ex.getMessage());
    }

    jTabbedPane1.setEnabledAt(1,true);
    jTabbedPane1.setEnabledAt(2,true);
    jTabbedPane1.setSelectedIndex(1);
    saveButton.setEnabled(true);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void openButton_actionPerformed(ActionEvent e){
    boolean success;
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
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
        saveButton.setEnabled(false);
        statusBar.setText(resource.getString("openFile"));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

  void saveButton_actionPerformed(ActionEvent e){
    boolean success;
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    try{
      success = controller.saveModel();
      if(success){
        statusBar.setText(resource.getString("saveModel"));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    } catch (Exception ioe) {
      statusBar.setText(resource.getString("errorWritingFileException") + " " + ioe.getMessage());
    }
  }

  void openModelButton_actionPerformed(ActionEvent e) {
    boolean success;
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    try{
      success = controller.openModel();
      if(success){
        jTabbedPane1.setEnabledAt(0,false);
        jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setEnabledAt(2,true);
        learnButton.setEnabled(false);
        saveButton.setEnabled(false);
        statusBar.setText(resource.getString("modelOpenedSuccessfully"));
        jTabbedPane1.setSelectedIndex(1);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    } catch (IOException ioe) {
      statusBar.setText(resource.getString("errorWritingFileException") + " " + ioe.getMessage());
    } catch (ClassNotFoundException cnfe) {
      statusBar.setText(cnfe.getMessage());
    }
  }
}