package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.controller.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.gui.*;
import unbbayes.gui.*;

public class NeuralModelMain extends JInternalFrame{
  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();
  /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
//  private ResourceBundle resource;
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
  private JFileChooser fileChooser;
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private JPanel tabbedPaneAttributes = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JLabel statusBar = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;
  private OptionsPanel optionsPanel;
  private OptionsPanel optionsPanel2;
//  private JOptionPane paneThreshold = new JOptionPane();
  private JPanel tabbedPaneRules = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private AttributePanel attributePanel;
  private RulesPanel rulesPanel = new RulesPanel();
  private CombinatorialNeuralModel combinatorialNetwork;
  private InstanceSet instanceSet;
  private BorderLayout borderLayout7 = new BorderLayout();
  private JPanel panelOptions = new JPanel();
  private BorderLayout borderLayout8 = new BorderLayout();
  private Border border2;
  private TitledBorder titledBorder2;
  private JPanel panelOptions2 = new JPanel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private JPanel internalPanelOptions2 = new JPanel();
  private JPanel jPanel1 = new JPanel();
  private Border border3;
  private TitledBorder titledBorder3;
  private GridLayout gridLayout2 = new GridLayout();
  private JButton buttonApply = new JButton();
  private JButton buttonRestore = new JButton();
  private BorderLayout borderLayout10 = new BorderLayout();
  private JPanel tabbedPanelClassify = new JPanel();
  private BorderLayout borderLayout11 = new BorderLayout();
  private InferencePanel inferencePanel = new InferencePanel();

  /**Construct the frame*/
  public NeuralModelMain(){
    super("Combinatorial Neural Model",true,true,true,true);
//    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource");
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try{
      jbInit();
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  /**Component initialization
   * @throws Exception
   * */
  private void jbInit() throws Exception{
    openIcon = new ImageIcon(getClass().getResource("/icons/open.gif"));
    compileIcon = new ImageIcon(getClass().getResource("/icons/learn.gif"));
    helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
    saveIcon = new ImageIcon(getClass().getResource("/icons/save.gif"));
    contentPane = (JPanel) this.getContentPane();
    titledBorder1 = new TitledBorder(border1,"Status");
    border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder2 = new TitledBorder(border2,"Opções");
    border3 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder3 = new TitledBorder(border3,"Opções");
    this.setSize(new Dimension(640, 521));
//    openButton.setToolTipText(resource.getString("openFileTooltip"));
    openButton.setIcon(openIcon);
    openButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        openButton_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
//    saveButton.setToolTipText("Save file"/*resource.getString("saveFileTooltip")*/);
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        saveButton_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
//    learnButton.setToolTipText("Learn Data"/*resource.getString("learnDataTooltip")*/);
    learnButton.setIcon(compileIcon);
    learnButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        learnButton_actionPerformed(e);
      }
    });
//    helpButton.setToolTipText("Help File"/*resource.getString("helpFileTooltip")*/);
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        helpButton_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    jPanel2.setLayout(borderLayout5);
    jPanel3.setLayout(borderLayout6);
    statusBar.setText("Bem vindo."/*resource.getString("welcome")*/);
    jPanel2.setBorder(titledBorder1);
    tabbedPaneRules.setLayout(borderLayout2);
    tabbedPaneAttributes.setLayout(borderLayout7);
    panelOptions.setLayout(borderLayout8);
    optionsPanel = new OptionsPanel();
    optionsPanel.setBorder(titledBorder2);
    panelOptions2.setLayout(borderLayout9);
    internalPanelOptions2.setLayout(borderLayout10);
    optionsPanel2 = new OptionsPanel();
    internalPanelOptions2.setBorder(titledBorder3);
    jPanel1.setLayout(gridLayout2);
    gridLayout2.setHgap(5);
    gridLayout2.setRows(2);
    gridLayout2.setVgap(5);
    buttonApply.setText("Aplicar");
    buttonApply.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonApply_actionPerformed(e);
      }
    });
    buttonRestore.setText("Restaurar");
    buttonRestore.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonRestore_actionPerformed(e);
      }
    });
    borderLayout10.setHgap(5);
    borderLayout10.setVgap(5);
    tabbedPanelClassify.setLayout(borderLayout11);
    internalPanelOptions2.add(optionsPanel2, BorderLayout.WEST);
    internalPanelOptions2.add(jPanel1, BorderLayout.EAST);
    jPanel1.add(buttonApply, null);
    jPanel1.add(buttonRestore, null);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(openButton, null);
    jToolBar1.add(saveButton, null);
    jToolBar1.add(learnButton, null);
    jToolBar1.add(helpButton, null);
    contentPane.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    attributePanel = new AttributePanel();
    tabbedPaneAttributes.add(attributePanel,  BorderLayout.CENTER);
    jTabbedPane1.add(tabbedPaneAttributes, /*resource.getString*/( "Atributos"));
    jTabbedPane1.add(tabbedPaneRules,   "Regras");
    jTabbedPane1.add(tabbedPanelClassify,  "Classificar");
    tabbedPanelClassify.add(inferencePanel);
    tabbedPaneRules.add(rulesPanel, BorderLayout.CENTER);
    tabbedPaneRules.add(panelOptions2,  BorderLayout.SOUTH);
    panelOptions2.add(internalPanelOptions2, BorderLayout.WEST);
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    panelOptions.add(optionsPanel,  BorderLayout.WEST);
    tabbedPaneAttributes.add(panelOptions,  BorderLayout.SOUTH);
    jTabbedPane1.setEnabledAt(1,false);
    jTabbedPane1.setEnabledAt(0,false);

    inferencePanel.setMainController(this);
  }

  void helpButton_actionPerformed(ActionEvent e){
    try{
      //FileController.getInstance().openHelp(this);
    } catch (Exception evt){
      statusBar.setText("Error = " + evt.getMessage() + " " + this.getClass().getName());
    }
  }

  void learnButton_actionPerformed(ActionEvent e){
    int maxOrder;
    int confidence;
    int support;

    if (instanceSet != null){
//      optionsPanel = new OptionsPanel();
//      paneThreshold.showInternalMessageDialog(this, optionsPanel, "CNM", JOptionPane.QUESTION_MESSAGE);
      maxOrder = optionsPanel.getMaxOrder();
      confidence = optionsPanel.getConfidence();
      support = optionsPanel.getSupport();

      try{
        combinatorialNetwork = new CombinatorialNeuralModel(maxOrder);
        combinatorialNetwork.buildClassifier(instanceSet);

        rulesPanel.setRulesPanel(combinatorialNetwork, instanceSet, confidence, support);
        jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setSelectedIndex(1);
/*              saveButton.setEnabled(true);

              NetWindow netWindow = new NetWindow(net);
              NetWindowEdition edition = netWindow.getNetWindowEdition();
              edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());

        // deixa invisíveis alguns botões do unbbayes
              edition.getMore().setVisible(false);
              edition.getLess().setVisible(false);
              edition.getArc().setVisible(false);
              edition.getDecisionNode().setVisible(false);
              edition.getProbabilisticNode().setVisible(false);
              edition.getUtilityNode().setVisible(false);
              edition.getSelect().setVisible(false);

        // mostra a nova tela
              jPanel1.removeAll();
              jPanel1.setLayout(new BorderLayout());
              jPanel1.add(netWindow,BorderLayout.CENTER);
  */ //           statusBar.setText(/*resource.getString*/("learnSuccessful"));

      } catch (Exception ex){
        statusBar.setText(/*resource.getString("exception ") +*/ ex.getMessage());
      }
    }
  }

  void openButton_actionPerformed(ActionEvent e){
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] arff = {"ARFF"};
    String[] txt = {"TXT"};
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(this));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(txt, "TxtFiles (*.txt)"));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(arff, "ArffFiles (*.arff)"));
    int returnValue = fileChooser.showOpenDialog(this);
    if (returnValue == JFileChooser.APPROVE_OPTION){
      File selectedFile = fileChooser.getSelectedFile();
      openFile(selectedFile);
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void openFile(File selectedFile){
    try{
      instanceSet = FileController.getInstance().setBaseInstancesFromFile(selectedFile,this);
      boolean bool = instanceSet.checkNumericAttributes();
      if (bool == true){
        throw new Exception(/*resource.getString*/("numericAttributesException"));
      }
      jTabbedPane1.setEnabledAt(0,false);
      setTitle("CNM - " + selectedFile.getName());
      attributePanel.setInstances(instanceSet);
      attributePanel.enableComboBox(true);
      jTabbedPane1.setEnabledAt(0,true);
      jTabbedPane1.setSelectedIndex(0);
      jTabbedPane1.setEnabledAt(1,false);
      learnButton.setEnabled(true);
      saveButton.setEnabled(false);
      statusBar.setText(/*resource.getString("openFile")*/"Arquivo aberto.");
    }catch (NullPointerException npe){
      statusBar.setText(/*resource.getString*/("errorDB")+selectedFile.getName()+" "+npe.getMessage());
    }catch (FileNotFoundException fnfe){
      statusBar.setText(/*resource.getString*/("fileNotFound")+selectedFile.getName()+" "+fnfe.getMessage());
    }catch (IOException ioe){
      statusBar.setText(/*resource.getString*/("errorOpen")+selectedFile.getName()+" "+ioe.getMessage());
    }catch (Exception ex){
      statusBar.setText(/*resource.getString*/("error")+ex.getMessage());
    }
  }

  void saveButton_actionPerformed(ActionEvent e){
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
/*      String[] s2 = {"net"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(NaiveBayesMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "Networks (*.net)"));
      int returnVal = fileChooser.showSaveDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          try
          {   String fileName = selectedFile.getName();
              if (!fileName.regionMatches(true,fileName.length() - 4,".net",0,4))
              {   selectedFile = new File(selectedFile.getAbsolutePath()+".net");
              }
              BaseIO io = new NetIO();
              io.save(selectedFile,net);
              statusBar.setText(resource.getString("saveModel"));
          }
          catch (Exception ioe)
          {   statusBar.setText(resource.getString("errorWritingFileException")+selectedFile.getName()+" "+ioe.getMessage());
          }
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
    */    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void buttonRestore_actionPerformed(ActionEvent e) {

  }

  void buttonApply_actionPerformed(ActionEvent e) {

  }

  public float[] classify(Instance instance){
    try{
      instance = instanceSet.getInstance(13);
      System.out.println(instance + " " + instanceSet.getClassAttribute().toString());

      float[] r = combinatorialNetwork.distributionForInstance(instance);
      return r;
    } catch (Exception e) {
      System.out.println(e);
    }
    return null;
  }
}