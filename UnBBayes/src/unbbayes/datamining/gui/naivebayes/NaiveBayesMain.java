package unbbayes.datamining.gui.naivebayes;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.gui.AttributePanel;
import unbbayes.gui.FileIcon;
import unbbayes.gui.NetWindow;
import unbbayes.gui.NetWindowEdition;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.bn.ProbabilisticNetwork;

public class NaiveBayesMain extends JInternalFrame
{
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
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
  private AttributePanel jPanel4;
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JPanel jPanel1;
  private BorderLayout borderLayout2 = new BorderLayout();
  private JFileChooser fileChooser;
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JLabel statusBar = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;

  /**Construct the frame*/
  public NaiveBayesMain()
  { super("Naive Bayes Classifier",true,true,true,true);
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
  {
    IconController iconController = IconController.getInstance();
    abrirIcon = iconController.getOpenIcon();
    compilaIcon = iconController.getCompileIcon();
    helpIcon = iconController.getHelpIcon();
    salvarIcon = iconController.getSaveIcon();
    contentPane = (JPanel) this.getContentPane();
    jPanel1 = new JPanel();
    titledBorder1 = new TitledBorder(border1,"Status");
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
    jPanel1.setLayout(borderLayout2);
    jPanel2.setLayout(borderLayout5);
    jPanel3.setLayout(borderLayout6);
    statusBar.setText(resource.getString("welcome"));
    jPanel2.setBorder(titledBorder1);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(openButton, null);
    jToolBar1.add(saveButton, null);
    jToolBar1.addSeparator();
    jToolBar1.add(learnButton, null);
    jToolBar1.addSeparator();
    jToolBar1.add(helpButton, null);
    contentPane.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    jPanel4 = new AttributePanel();
    //jTabbedPane1.add(jPanel4,   "jPanel4");
    //jTabbedPane1.add(jScrollPane1,  "jScrollPane1");
    //jTabbedPane.add(jPanel4, resource.getString("attributes"));
    //jTabbedPane.add(jScrollPane1, resource.getString("inference"));
    jTabbedPane1.add(jPanel4, resource.getString("attributes2"));
    jTabbedPane1.add(jScrollPane1, resource.getString("inference"));
    contentPane.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(statusBar,  BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jPanel1, null);
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
  {   if (inst != null)
      {   try
          {
          	  NaiveBayes naiveBayes = new NaiveBayes();
          	  naiveBayes.buildClassifier(inst);
          	  net = naiveBayes.getProbabilisticNetwork();
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
              edition.getHierarchy().setVisible(false);

              // mostra a nova tela
              jPanel1.removeAll();
              jPanel1.setLayout(new BorderLayout());
              jPanel1.add(netWindow.getContentPane(),BorderLayout.CENTER);
              statusBar.setText(resource.getString("learnSuccessful"));
          }
          catch (Exception ex)
          {   statusBar.setText(resource.getString("exception")+ex.getMessage());
          	ex.printStackTrace();
          }
      }
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
  {   try
      {
        inst = FileController.getInstance().getInstanceSet(selectedFile,this);
        if (inst!=null)
        {
          boolean bool = inst.checkNumericAttributes();
          if (bool == true)
              throw new Exception(resource.getString("numericAttributesException"));
          jTabbedPane1.setEnabledAt(0,false);
          setTitle("Naive Bayes - "+selectedFile.getName());
          jPanel4.enableComboBox(true);
          jPanel4.setInstances(inst);
          jTabbedPane1.setEnabledAt(0,true);
          jTabbedPane1.setSelectedIndex(0);
          jTabbedPane1.setEnabledAt(1,false);
          jMenuItem4.setEnabled(true);
          learnButton.setEnabled(true);
          jMenuItem5.setEnabled(false);
          saveButton.setEnabled(false);
          statusBar.setText(resource.getString("openFile"));
        }
        else
        {
          statusBar.setText("Operação cancelada");
        }
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

  void jMenuItem5_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s2 = {"net"};
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