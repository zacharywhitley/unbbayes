package unbbayes.datamining.gui.preprocessor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.controller.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.gui.*;

public class PreprocessorMain extends JInternalFrame
{
  /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;
  private JPanel contentPane;
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenuFile = new JMenu();
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout = new JMenuItem();
  private JToolBar jToolBar = new JToolBar();
  private JButton openButton = new JButton();
  private JButton helpButton = new JButton();
  private ImageIcon abrirIcon;
  private ImageIcon helpIcon;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JPanel jPanel41 = new JPanel();
  private TitledBorder titledBorder5;
  private Border border5;
  private PreprocessPanel jPanel1 = new PreprocessPanel(this);
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private InstanceSet inst;
  private JMenuItem jMenuItem1 = new JMenuItem();
  private JMenuItem jMenuFileExit = new JMenuItem();
  private EditorPanel jPanel2;
  private JFileChooser fileChooser;
  public static final int TXT_EXTENSION = 0;
  public static final int ARFF_EXTENSION = 1;
  private int fileExtension = 2;
  private JPanel jPanel3 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();

  /**Construct the frame*/
  public PreprocessorMain()
  { super("Preprocessor",true,true,true,true);
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.preprocessor.resources.PreprocessorResource");
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
  /**Component initialization*/
  private void jbInit() throws Exception
  { abrirIcon = new ImageIcon(getClass().getResource("/icons/open.gif"));
    helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
    jPanel2 = new EditorPanel(this);
    contentPane = (JPanel) this.getContentPane();
    titledBorder5 = new TitledBorder(border5,resource.getString("selectProgram"));
    border5 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(640, 480));
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
    openButton.setIcon(abrirIcon);
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openButton_actionPerformed(e);
      }
    });
    openButton.setToolTipText(resource.getString("openFile"));
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        helpButton_actionPerformed(e);
      }
    });
    helpButton.setToolTipText(resource.getString("help"));
    jPanel41.setLayout(borderLayout2);
    jToolBar.setFloatable(false);
    jPanel41.setBorder(titledBorder5);
    titledBorder5.setTitle(resource.getString("status"));
    statusBar.setText(resource.getString("welcome"));
    jMenuItem1.setIcon(abrirIcon);
    jMenuItem1.setMnemonic(((Character)resource.getObject("openMnemonic")).charValue());
    jMenuItem1.setText(resource.getString("open"));
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    jMenuFileExit.setMnemonic(((Character)resource.getObject("fileExitMnemonic")).charValue());
    jMenuFileExit.setText(resource.getString("exit"));
    jMenuFileExit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jPanel2.setEnabled(false);
    jPanel3.setLayout(borderLayout3);
    jToolBar.add(openButton);
    jToolBar.add(helpButton);
    jMenuFile.add(jMenuItem1);
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(jToolBar,  BorderLayout.NORTH);
    contentPane.add(jPanel41,  BorderLayout.SOUTH);
    jPanel41.add(statusBar, BorderLayout.CENTER);
    jTabbedPane1.add(jPanel1,resource.getString("preprocess"));
    jTabbedPane1.add(jPanel2,resource.getString("editor"));
    contentPane.add(jPanel3,  BorderLayout.CENTER);
    jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    jTabbedPane1.setEnabledAt(1,false);
  }
  /**File | Exit action performed*/
  public void jMenuFileExit_actionPerformed(ActionEvent e)
  {
    dispose();
  }
  /**Help | About action performed*/
  public void jMenuHelpAbout_actionPerformed(ActionEvent e)
  {   try
      {   FileController.getInstance().openHelp(this);
      }
      catch (Exception evt)
      {   statusBar.setText(resource.getString("errorException")+evt.getMessage()+" "+this.getClass().getName());
      }
  }

  void openButton_actionPerformed(ActionEvent e)
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
      {   inst = FileController.getInstance().setBaseInstancesFromFile(selectedFile,this);
          String fileName = selectedFile.getName();
          if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
          {   fileExtension = ARFF_EXTENSION;
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
          {   fileExtension = TXT_EXTENSION;
          }
          jTabbedPane1.setEnabledAt(1,false);
          jTabbedPane1.setSelectedIndex(0);
          jPanel1.setBaseInstances(inst);
          statusBar.setText(resource.getString("fileOpened"));
          this.setTitle(resource.getString("preprocessorTitle")+selectedFile.getName());
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

  public void updateInstances(InstanceSet inst)
  {   jTabbedPane1.setSelectedIndex(0);
      this.inst = inst;
      jPanel1.setBaseInstances(inst);
  }

  public void setStatusBar(String text)
  {   statusBar.setText(text);
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {   openButton_actionPerformed(e);
  }

  void helpButton_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  public void setEditorText(String text)
  {   jPanel2.setText(text);
  }

  public int getFileExtension()
  {   return fileExtension;
  }

  public JTabbedPane getTabbedPane()
  {   return jTabbedPane1;
  }
}