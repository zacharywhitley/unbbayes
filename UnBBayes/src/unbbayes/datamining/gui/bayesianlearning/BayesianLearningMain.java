package unbbayes.datamining.gui.bayesianlearning;
 
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.controller.*;
import unbbayes.datamining.classifiers.bayesianlearning.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.gui.*;
import unbbayes.gui.*;
import unbbayes.io.*;
import unbbayes.prs.bn.*;

public class BayesianLearningMain extends JInternalFrame
{
  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();
  private InstanceSet inst;
  /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;
  private ProbabilisticNetwork net;
  private JToolBar jToolBar1 = new JToolBar();
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenu1 = new JMenu();
  private JMenuItem jMenuItem1 = new JMenuItem();
  private JMenuItem jMenuItem2 = new JMenuItem();
  private JMenu jMenu2 = new JMenu();
  private JMenuItem jMenuItem3 = new JMenuItem();
  private JMenu jMenu3 = new JMenu();
  private JMenuItem jMenuItem4 = new JMenuItem();
  private JMenuItem jMenuItem5 = new JMenuItem();
  private JButton helpButton = new JButton();
  private JButton learnButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton openButton = new JButton();
  private ImageIcon abrirIcon;
  private ImageIcon compilaIcon;
  private ImageIcon helpIcon;
  private ImageIcon salvarIcon;
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
  private TitledBorder titledBorder2;
  private TitledBorder titledBorder3;
  private TitledBorder titledBorder4;
  private TitledBorder titledBorder5;
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel1;
  private JScrollPane jScrollPane1 = new JScrollPane();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel5 = new JPanel();
  private JPanel jPanel6 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private JPanel jPanel7 = new JPanel();
  private JPanel jPanel8 = new JPanel();
  private JPanel jPanel9 = new JPanel();
  private BorderLayout borderLayout7 = new BorderLayout();
  private JPanel jPanel10 = new JPanel();
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private JPanel jPanel13 = new JPanel();
  private JPanel jPanel14 = new JPanel();
  private JPanel jPanel15 = new JPanel();
  private JPanel jPanel16 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private String[] metrics = {/*"MDL",*/"GH"/*, "GHS"*/};
  private String[] paradigms = {"Ponctuation"/*,"IC"*/};
  private String[] ponctuationAlgorithms = {"K2"/*,"B"*/};
  //private String[] icAlgorithms = {"CBL-A","CBL-B"};
  private JComboBox jComboBox1 = new JComboBox(paradigms);
  private JComboBox jComboBox2 = new JComboBox(ponctuationAlgorithms);
  private JComboBox jComboBox3 = new JComboBox(metrics);


  /**Construct the frame*/
  public BayesianLearningMain()
  { super("Bayesian Learning",true,true,true,true);
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.naivebayes.resources.NaiveBayesResource");

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  /**Component initialization
   * @throws Exception
   * */
  private void jbInit() throws Exception
  { abrirIcon = new ImageIcon(getClass().getResource("/icons/open.gif"));
    compilaIcon = new ImageIcon(getClass().getResource("/icons/learn.gif"));
    helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
    salvarIcon = new ImageIcon(getClass().getResource("/icons/save.gif"));
    contentPane = (JPanel) this.getContentPane();
    titledBorder1 = new TitledBorder(border1,"Status");
    titledBorder2 = new TitledBorder(border1,"Main Settings");
    titledBorder3 = new TitledBorder(border1,"Ordenation");
    titledBorder4 = new TitledBorder(border1,"Relations");
    titledBorder5 = new TitledBorder(border1,"Threshold");
    jPanel1 = new JPanel();
    this.setJMenuBar(jMenuBar1);
    this.setSize(new Dimension(640,480));
    jMenu1.setMnemonic(((Character)resource.getObject("fileMnemonic")).charValue());
    jMenu1.setText(resource.getString("fileMenu"));
    jMenuItem1.setIcon(abrirIcon);
    jMenuItem1.setMnemonic(((Character)resource.getObject("openMnemonic")).charValue());
    jMenuItem1.setText(resource.getString("openMenu"));
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    jMenuItem2.setMnemonic(((Character)resource.getObject("exitMnemonic")).charValue());
    jMenuItem2.setText(resource.getString("exit"));
    jMenuItem2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenu2.setMnemonic(((Character)resource.getObject("helpMnemonic")).charValue());
    jMenu2.setText(resource.getString("help"));
    jMenuItem3.setIcon(helpIcon);
    jMenuItem3.setMnemonic(((Character)resource.getObject("helpTopicsMnemonic")).charValue());
    jMenuItem3.setText(resource.getString("helpTopicsMenu"));
    jMenuItem3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem3_actionPerformed(e);
      }
    });
    jMenu3.setMnemonic(((Character)resource.getObject("learningMnemonic")).charValue());
    jMenu3.setText(resource.getString("learningMenu"));
    jMenuItem4.setEnabled(false);
    jMenuItem4.setIcon(compilaIcon);
    jMenuItem4.setMnemonic(((Character)resource.getObject("learnNaiveBayesMnemonic")).charValue());
    jMenuItem4.setText(resource.getString("learnNaiveBayes"));
    jMenuItem4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem4_actionPerformed(e);
      }
    });
    jMenuItem5.setEnabled(false);
    jMenuItem5.setIcon(salvarIcon);
    jMenuItem5.setMnemonic(((Character)resource.getObject("saveNetworkMnemonic")).charValue());
    jMenuItem5.setText(resource.getString("saveNetworkMenu"));
    jMenuItem5.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem5_actionPerformed(e);
      }
    });
    openButton.setToolTipText(resource.getString("openFileTooltip"));
    openButton.setIcon(abrirIcon);
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openButton_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
    saveButton.setToolTipText(resource.getString("saveFileTooltip"));
    saveButton.setIcon(salvarIcon);
    saveButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveButton_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
    learnButton.setToolTipText(resource.getString("learnDataTooltip"));
    learnButton.setIcon(compilaIcon);
    learnButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        learnButton_actionPerformed(e);
      }
    });
    helpButton.setToolTipText(resource.getString("helpFileTooltip"));
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        helpButton_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    jPanel2.setLayout(borderLayout5);
    jPanel3.setLayout(borderLayout6);
    statusBar.setText(resource.getString("welcome"));
    jPanel2.setBorder(titledBorder1);
    jPanel1.setLayout(borderLayout2);
    jPanel4.setLayout(gridLayout1);
    gridLayout1.setColumns(2);
    jPanel6.setLayout(gridLayout2);
    gridLayout2.setRows(2);
    jPanel7.setBorder(titledBorder3);
    jPanel7.setToolTipText("Use up and down arrows to move the "+
                           "field names so that a field does not "+
                           "depend on any of the fields below it.");
    jPanel8.setBorder(titledBorder4);
    jPanel8.setToolTipText("When field A is indirect cause of field B, select A from the first " +
                           "column and B from second column");
    jPanel5.setLayout(borderLayout7);
    jPanel9.setBorder(titledBorder2);
    jPanel9.setLayout(gridLayout3);
    jPanel10.setBorder(titledBorder5);
    gridLayout3.setColumns(2);
    gridLayout3.setRows(3);
    jLabel1.setText("Paradigm:");
    jLabel2.setText("Algorithm:");
    jLabel3.setText("Metric:");
    jComboBox1.setEnabled(false);
    jComboBox2.setEnabled(false);
    jComboBox3.setEnabled(false);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(openButton, null);
    jToolBar1.add(saveButton, null);
    jToolBar1.add(learnButton, null);
    jToolBar1.add(helpButton, null);
    contentPane.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    jTabbedPane1.add(jPanel4,  "Settings");
    jPanel4.add(jPanel5, null);
    jPanel5.add(jPanel9, BorderLayout.CENTER);
    jPanel9.add(jPanel16, null);
    jPanel16.add(jLabel1, null);
    jPanel9.add(jPanel15, null);
    jPanel15.add(jComboBox1, null);
    jPanel9.add(jPanel14, null);
    jPanel14.add(jLabel2, null);
    jPanel9.add(jPanel13, null);
    jPanel13.add(jComboBox2, null);
    jPanel9.add(jPanel12, null);
    jPanel12.add(jLabel3, null);
    jPanel9.add(jPanel11, null);
    jPanel11.add(jComboBox3, null);
    jPanel5.add(jPanel10,  BorderLayout.SOUTH);
    jPanel4.add(jPanel6, null);
    jPanel6.add(jPanel7, null);
    jPanel6.add(jPanel8, null);
    jTabbedPane1.add(jScrollPane1,   "Inference");
    jScrollPane1.getViewport().add(jPanel1, null);
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    jTabbedPane1.setEnabledAt(1,false);
    jTabbedPane1.setEnabledAt(0,false);
    jMenuBar1.add(jMenu1);
    jMenuBar1.add(jMenu3);
    jMenuBar1.add(jMenu2);
    jMenu1.add(jMenuItem1);
    jMenu1.add(jMenuItem5);
    jMenu1.add(jMenuItem2);
    jMenu2.add(jMenuItem3);
    jMenu3.add(jMenuItem4);

  }

  void jMenuItem3_actionPerformed(ActionEvent e)
  {   try
      {   FileController.getInstance().openHelp(this);
      }
      catch (Exception evt)
      {   statusBar.setText("Error= "+evt.getMessage()+" "+this.getClass().getName());
      }
  }

  void jMenuItem4_actionPerformed(ActionEvent e)
  {   /*if (inst != null)
      {   ComputeProbabilisticNetwork trp = new ComputeProbabilisticNetwork();
          try
          {   trp.setInstances(inst);
              net = trp.getProbabilisticNetwork();
              jMenuItem5.setEnabled(true);
              jTabbedPane1.setEnabledAt(1,true);
              jTabbedPane1.setSelectedIndex(1);
              saveButton.setEnabled(true);

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
              jPanel1.add(netWindow.getContentPane(),BorderLayout.CENTER);
              statusBar.setText(resource.getString("learnSuccessful"));
          }
          catch (Exception ex)
          {   statusBar.setText(resource.getString("exception")+ex.getMessage());
          }
      }*/
      AlgorithmController algorithmController = new AlgorithmController(inst,""+jComboBox2.getSelectedItem(),""+jComboBox3.getSelectedItem());
      // mostra a nova tela

      jPanel1.removeAll();
      jPanel1.setLayout(new BorderLayout());
      jPanel1.add(algorithmController.getNetWindow().getContentPane(),BorderLayout.CENTER);
      statusBar.setText("Estrutura Aprendida");
      jTabbedPane1.setEnabledAt(1,true);
      jTabbedPane1.setSelectedIndex(1);
  }

  void jMenuItem2_actionPerformed(ActionEvent e)
  {   dispose();
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ARFF"};
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          openFile(selectedFile);
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void openFile(File selectedFile)
  {
    try
    {
      jTabbedPane1.setEnabledAt(0,false);
      inst = FileController.getInstance().setBaseInstancesFromFile(selectedFile,this);
      boolean bool = inst.checkNumericAttributes();
      if (bool == true)
      {
        throw new Exception(resource.getString("numericAttributesException"));
      }
      setTitle("Bayesian Learnning - "+selectedFile.getName());
      enableScreen();
    }
    catch (NullPointerException npe)
      {   statusBar.setText(resource.getString("errorDB")+selectedFile.getName()+" "+npe.getMessage());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText(resource.getString("fileNotFound")+selectedFile.getName()+" "+fnfe.getMessage());
      }
      catch (IOException ioe)
      {   statusBar.setText(resource.getString("errorOpen")+selectedFile.getName()+" "+ioe.getMessage());
      }
      catch (Exception ex)
      {   statusBar.setText(resource.getString("error")+ex.getMessage());
      }
  }

  private void enableScreen()
  {
    jTabbedPane1.setEnabledAt(0,true);
    jTabbedPane1.setSelectedIndex(0);
    jTabbedPane1.setEnabledAt(1,false);
    jMenuItem4.setEnabled(true);
    learnButton.setEnabled(true);
    jMenuItem5.setEnabled(false);
    saveButton.setEnabled(false);
    jComboBox1.setEnabled(true);
    jComboBox2.setEnabled(true);
    jComboBox3.setEnabled(true);
    statusBar.setText(resource.getString("openFile"));
  }

  void jMenuItem5_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s2 = {"net"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(BayesianLearningMain.this));
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
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void helpButton_actionPerformed(ActionEvent e)
  {   jMenuItem3_actionPerformed(e);
  }

  void openButton_actionPerformed(ActionEvent e)
  {   jMenuItem1_actionPerformed(e);
  }

  void saveButton_actionPerformed(ActionEvent e)
  {   jMenuItem5_actionPerformed(e);
  }

  void learnButton_actionPerformed(ActionEvent e)
  {   jMenuItem4_actionPerformed(e);
  }


}