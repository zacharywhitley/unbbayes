package unbbayes.datamining.gui.evaluation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.controlador.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.fronteira.*;
import unbbayes.io.*;
import unbbayes.jprs.jbn.*;

public class EvaluationMain extends JInternalFrame
{ /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;
  private ImageIcon abrirIcon;
  private ImageIcon helpIcon;
  private JPanel contentPane;
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenuFile = new JMenu();
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout = new JMenuItem();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel41 = new JPanel();
  private TitledBorder titledBorder5;
  private Border border5;
  private EvaluationPanel jPanel2 = new EvaluationPanel(this);
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private InstanceSet inst;
  private ProbabilisticNetwork net;
  private Classifier classifier;
  private File selectedFile;
  private boolean instOK = false;
  private JMenuItem jMenuFileExit = new JMenuItem();
  private JFileChooser fileChooser;
  private JToolBar jToolBar1 = new JToolBar();
  private JButton helpButton = new JButton();
  private JButton openButton = new JButton();
  private JMenuItem jMenuItem2 = new JMenuItem();

  /**Construct the frame*/
  public EvaluationMain()
  { super("Evaluation",true,true,true,true);
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.evaluation.resources.EvaluationResource");
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
   * @throws Exception if any error
   * */
  private void jbInit() throws Exception
  { abrirIcon = new ImageIcon(getClass().getResource("/icones/abrir.gif"));
    helpIcon = new ImageIcon(getClass().getResource("/icones/help.gif"));
    contentPane = (JPanel) this.getContentPane();
    titledBorder5 = new TitledBorder(border5,resource.getString("selectProgram"));
    border5 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(640,480));
    jMenuFile.setMnemonic(((Character)resource.getObject("fileMnemonic")).charValue());
    jMenuFile.setText(resource.getString("file"));
    jMenuHelp.setMnemonic(((Character)resource.getObject("helpMnemonic")).charValue());
    jMenuHelp.setText(resource.getString("help"));
    jMenuHelpAbout.setIcon(helpIcon);
    jMenuHelpAbout.setMnemonic(((Character)resource.getObject("helpTopicsMnemonic")).charValue());
    jMenuHelpAbout.setText(resource.getString("helpTopics"));
    jMenuHelpAbout.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jPanel41.setLayout(borderLayout2);
    jPanel41.setBorder(titledBorder5);
    titledBorder5.setTitle(resource.getString("status"));
    statusBar.setText(resource.getString("welcome"));
    jMenuFileExit.setMnemonic(((Character)resource.getObject("fileExitMnemonic")).charValue());
    jMenuFileExit.setText(resource.getString("exit"));
    jMenuFileExit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileExit_actionPerformed(e);
      }
    });
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        helpButton_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    openButton.setToolTipText(resource.getString("openModel"));
    openButton.setIcon(abrirIcon);
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openButton_actionPerformed(e);
      }
    });
    jMenuItem2.setIcon(abrirIcon);
    jMenuItem2.setMnemonic(((Character)resource.getObject("openModelMnemonic")).charValue());
    jMenuItem2.setText(resource.getString("openModelDialog"));
    jMenuItem2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenuFile.add(jMenuItem2);
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(jPanel41,  BorderLayout.SOUTH);
    jPanel41.add(statusBar, BorderLayout.CENTER);
    contentPane.add(jPanel2,BorderLayout.CENTER);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(openButton, null);
    jToolBar1.add(helpButton, null);
  }
  /**File | Exit action performed
   * @param e One ActionEvent
   * */
  public void jMenuFileExit_actionPerformed(ActionEvent e)
  {
    dispose();
  }
  /**Help | About action performed
   * @param e One ActionEvent
   * */
  public void jMenuHelpAbout_actionPerformed(ActionEvent e)
  {   try
      {   FileController.getInstance().openHelp(this);
      }
      catch (Exception evt)
      {   statusBar.setText(resource.getString("error2")+evt.getMessage()+" "+this.getClass().getName());
      }
  }

  public void setStatusBar(String text)
  {   statusBar.setText(text);
  }

  void openTest()
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
      {   selectedFile = fileChooser.getSelectedFile();
          openFile(selectedFile);
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void openFile(File selectedFile)
  {   try
      {   inst = FileController.getInstance().setBaseInstancesFromFile(selectedFile,this);
          boolean bool = inst.checkNumericAttributes();
          if (bool == true)
              throw new Exception(resource.getString("numericAttributesException"));
          instOK = true;
          statusBar.setText("Test file opened sucessfully");
      }
      catch (NullPointerException npe)
      {   statusBar.setText(resource.getString("errorDB")+npe.getMessage());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText(resource.getString("fileNotFound")+fnfe.getMessage());
      }
      catch (IOException ioe)
      {   statusBar.setText(resource.getString("errorOpen")+ioe.getMessage());
      }
      catch(Exception e)
      {   statusBar.setText(resource.getString("error")+e.getMessage());
      }
  }

  void helpButton_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  void openButton_actionPerformed(ActionEvent e)
  {   jMenuItem2_actionPerformed(e);
  }

  void jMenuItem2_actionPerformed(ActionEvent evt)
  {   openModel();
      openTest();
      try
      {   BayesianNetwork bayesianNetwork = new BayesianNetwork(net,inst);
          classifier = bayesianNetwork;
      }
      catch (Exception e)
      {   statusBar.setText(e.getMessage());
          instOK = false;
      }
      if (instOK)
      {   jPanel2.setModel(classifier,inst);
          statusBar.setText(resource.getString("modelOpened"));
          this.setTitle("Evaluation - "+resource.getString("model")+selectedFile.getName());
      }
  }

  private void openModel()
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ID3"};
      String[] s2 = {"NET"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EvaluationMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "Networks (*.net)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   selectedFile = fileChooser.getSelectedFile();
          setModelFromFile(selectedFile);
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void setModelFromFile(File f)
  {   try
      {   String fileName = f.getName();
          if (fileName.regionMatches(true,fileName.length() - 4,".id3",0,4))
          {   ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
              classifier = (Id3)in.readObject();
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".net",0,4))
          {   BaseIO io = new NetIO();
              net = io.load(f);
          }
          else
          {   throw new IOException(resource.getString("fileExtensionNotKnown"));
          }
      }
      catch (NullPointerException npe)
      {   statusBar.setText(resource.getString("errorDB")+npe.getMessage());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText(resource.getString("fileNotFound")+fnfe.getMessage());
      }
      catch (IOException ioe)
      {   statusBar.setText(resource.getString("errorOpen")+ioe.getMessage());
      }
      catch(Exception e)
      {   statusBar.setText(resource.getString("error")+e.getMessage());
          e.printStackTrace();
      }
  }


}