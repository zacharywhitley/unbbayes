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
  private ImageIcon returnIcon;
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
  private Border border3;
  private TitledBorder titledBorder3;
  private JPanel tabbedPanelClassify = new JPanel();
  private BorderLayout borderLayout11 = new BorderLayout();
  private InferencePanel inferencePanel = new InferencePanel();
  private JLabel jLabel1 = new JLabel();
  private JButton openModelButton = new JButton();

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
    returnIcon = new ImageIcon(getClass().getResource("/icons/initialize.gif"));
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
    tabbedPanelClassify.setLayout(borderLayout11);
    jLabel1.setToolTipText("");
    jLabel1.setText("   ");
    openModelButton.setIcon(openIcon);
    openModelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openModelButton_actionPerformed(e);
      }
    });
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
    jTabbedPane1.add(tabbedPaneAttributes, /*resource.getString*/( "Atributos"));
    jTabbedPane1.add(tabbedPaneRules,   "Regras");
    jTabbedPane1.add(tabbedPanelClassify,  "Classificar");
    tabbedPanelClassify.add(inferencePanel);
    tabbedPaneRules.add(rulesPanel, BorderLayout.CENTER);
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    panelOptions.add(optionsPanel,  BorderLayout.CENTER);
    tabbedPaneAttributes.add(panelOptions,  BorderLayout.SOUTH);
    jTabbedPane1.setEnabledAt(0,false);
    jTabbedPane1.setEnabledAt(1,false);
    jTabbedPane1.setEnabledAt(2,false);
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

    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    if(instanceSet != null){
      maxOrder = optionsPanel.getMaxOrder();
      confidence = optionsPanel.getConfidence();
      support = optionsPanel.getSupport();

      try{
        combinatorialNetwork = new CombinatorialNeuralModel(maxOrder);
        combinatorialNetwork.buildClassifier(instanceSet);

        rulesPanel.setRulesPanel(combinatorialNetwork, confidence, support);
        inferencePanel.setNetwork(combinatorialNetwork);

        jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setEnabledAt(2,true);
        jTabbedPane1.setSelectedIndex(1);

        saveButton.setEnabled(true);
/*
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
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
      optionsPanel.enableCombos(true);
      jTabbedPane1.setEnabledAt(0,true);
      jTabbedPane1.setSelectedIndex(0);
      jTabbedPane1.setEnabledAt(1,false);
      jTabbedPane1.setEnabledAt(2,false);
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
    String[] cnm = {"cnm"};
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(NeuralModelMain.this));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(cnm, "Modelo Neural Combinatório (*.cnm)"));
    int returnVal = fileChooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION){
      File selectedFile = fileChooser.getSelectedFile();
      try{
        String fileName = selectedFile.getName();
        if (!fileName.regionMatches(true,fileName.length() - 4,".cnm",0,4)){
          selectedFile = new File(selectedFile.getAbsolutePath()+".cnm");
        }

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile));
        out.writeObject(combinatorialNetwork);

        statusBar.setText(/*resource.getString*/("saveModel"));
      } catch (Exception ioe) {
        statusBar.setText(/*resource.getString*/("errorWritingFileException")+selectedFile.getName()+" "+ioe.getMessage());
      }
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void openModelButton_actionPerformed(ActionEvent e) {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] cnm = {"cnm"};
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(/*resource.getString("openModel2")*/"Abrir modelo");
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(this));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(cnm, "Modelo Neural Combinatóri (*.cnm)"));
    int returnVal = fileChooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION){
      File selectedFile = fileChooser.getSelectedFile();
      try{
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
        combinatorialNetwork = null;
        combinatorialNetwork = (CombinatorialNeuralModel)in.readObject();

        tabbedPaneRules.removeAll();
        rulesPanel = new RulesPanel();
        rulesPanel.setRulesPanel(combinatorialNetwork);
        tabbedPaneRules.add(rulesPanel, BorderLayout.CENTER);
        inferencePanel.setNetwork(combinatorialNetwork);
        jTabbedPane1.setEnabledAt(0,false);
        jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setEnabledAt(2,true);
        learnButton.setEnabled(false);
        saveButton.setEnabled(false);
//        jMenuFileBuild.setEnabled(false);
        this.setTitle("CNM - Model "+selectedFile.getName());
        statusBar.setText(/*resource.getString("modelOpenedSuccessfully")*/"Modelo carregado com sucesso.");
        jTabbedPane1.setSelectedIndex(1);
      } catch (IOException ioe) {
        statusBar.setText(/*resource.getString("errorWritingFile")+*/selectedFile.getName()+" "+ioe.getMessage());
      } catch (ClassNotFoundException cnfe) {
        statusBar.setText(cnfe.getMessage());
      }
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
}