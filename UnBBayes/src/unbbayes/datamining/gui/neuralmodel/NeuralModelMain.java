package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;
import unbbayes.controller.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.*;
import unbbayes.datamining.gui.*;
import unbbayes.gui.*;

public class NeuralModelMain extends JInternalFrame{
  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();
  /** Carrega o arquivo de recursos para internacionaliza��o da localidade padr�o */
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
  private BorderLayout borderLayout5 = new BorderLayout();
  private JLabel statusBar = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;
  private ThresholdPanel thresholdPanel/* = new ThresholdPanel()*/;
  private JOptionPane paneThreshold = new JOptionPane();
  private JPanel tabbedPaneRules = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private AttributePanel attributePanel;
  private RulesPanel rulesPanel = new RulesPanel();
  private CombinatorialNetwork combinatorialNetwork;
  private InstanceSet inst;

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
    this.setSize(new Dimension(640,480));
//    openButton.setToolTipText(resource.getString("openFileTooltip"));
    openButton.setIcon(openIcon);
    openButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        openButton_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
    saveButton.setToolTipText("Save file"/*resource.getString("saveFileTooltip")*/);
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        saveButton_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
    learnButton.setToolTipText("Learn Data"/*resource.getString("learnDataTooltip")*/);
    learnButton.setIcon(compileIcon);
    learnButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        learnButton_actionPerformed(e);
      }
    });
    helpButton.setToolTipText("Help File"/*resource.getString("helpFileTooltip")*/);
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        helpButton_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    jPanel2.setLayout(borderLayout5);
    jPanel3.setLayout(borderLayout6);
    statusBar.setText(/*resource.getString*/("welcome"));
    jPanel2.setBorder(titledBorder1);
    tabbedPaneRules.setLayout(borderLayout2);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(openButton, null);
    jToolBar1.add(saveButton, null);
    jToolBar1.add(learnButton, null);
    jToolBar1.add(helpButton, null);
    contentPane.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    attributePanel = new AttributePanel();
    jTabbedPane1.add(attributePanel, /*resource.getString*/("attributes2"));
    jTabbedPane1.add(tabbedPaneRules,  "Rules");
    tabbedPaneRules.add(rulesPanel, BorderLayout.CENTER);
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    jTabbedPane1.setEnabledAt(1,false);
    jTabbedPane1.setEnabledAt(0,false);
  }

  void helpButton_actionPerformed(ActionEvent e){
    try{
      //FileController.getInstance().openHelp(this);
    } catch (Exception evt){
      statusBar.setText("Error = " + evt.getMessage() + " " + this.getClass().getName());
    }
  }

  void learnButton_actionPerformed(ActionEvent e){
    int threshold;
    int reliability;

    if (inst != null){
      thresholdPanel = new ThresholdPanel();
      paneThreshold.showInternalMessageDialog(this, thresholdPanel, "CNM", JOptionPane.QUESTION_MESSAGE);
      threshold = thresholdPanel.getThreshold();
      reliability = thresholdPanel.getReliability();

      while(threshold > inst.numAttributes() || threshold < 1){
        thresholdPanel = new ThresholdPanel();
        paneThreshold.showInternalMessageDialog(this, "Ordem m�xima inv�lida!", "CNM", JOptionPane.ERROR_MESSAGE);
        paneThreshold.showInternalMessageDialog(this, thresholdPanel, "CNM", JOptionPane.QUESTION_MESSAGE);
        threshold = thresholdPanel.getThreshold();
        reliability = thresholdPanel.getReliability();
      }//nao ta fazendo nada com esses valores por enqunato.


      try{
        CombinatorialNetworkConstructor netConstructor = new CombinatorialNetworkConstructor(inst);
        combinatorialNetwork = netConstructor.generateNetwork();


        rulesPanel.setRulesPanel(combinatorialNetwork, inst);
        jTabbedPane1.setEnabledAt(1,true);
        jTabbedPane1.setSelectedIndex(1);
/*              saveButton.setEnabled(true);

              NetWindow netWindow = new NetWindow(net);
              NetWindowEdition edition = netWindow.getNetWindowEdition();
              edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());

        // deixa invis�veis alguns bot�es do unbbayes
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
  */            statusBar.setText(/*resource.getString*/("learnSuccessful"));

      } catch (Exception ex){
        statusBar.setText(/*resource.getString*/("exception ") + ex.getMessage());
      }
    }
  }

  void openButton_actionPerformed(ActionEvent e){
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] arff = {"ARFF"};
    String[] txt = {"TXT"};
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar �cones de arquivos
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
      inst = FileController.getInstance().setBaseInstancesFromFile(selectedFile,this);
      boolean bool = inst.checkNumericAttributes();
      if (bool == true){
        throw new Exception(/*resource.getString*/("numericAttributesException"));
      }
      jTabbedPane1.setEnabledAt(0,false);
      setTitle("CNM - " + selectedFile.getName());
      attributePanel.setInstances(inst);
      attributePanel.enableComboBox(true);
      jTabbedPane1.setEnabledAt(0,true);
      jTabbedPane1.setSelectedIndex(0);
      jTabbedPane1.setEnabledAt(1,false);
      learnButton.setEnabled(true);
      saveButton.setEnabled(false);
      statusBar.setText(/*resource.getString*/("openFile"));
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
    //adicionar FileView no FileChooser para desenhar �cones de arquivos
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
}