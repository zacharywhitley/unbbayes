package unbbayes.datamining.gui.preprocessor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.help.*;
import javax.swing.*;
import javax.swing.border.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.fronteira.*;

public class PreprocessorMain extends /*JFrame*/JInternalFrame
{
  private JPanel contentPane;
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenuFile = new JMenu();
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout = new JMenuItem();
  private JToolBar jToolBar = new JToolBar();
  private JButton jButton1 = new JButton();
  private JButton jButton3 = new JButton();
  private ImageIcon image1;
  private ImageIcon image2;
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
  private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  public static final int TXT_EXTENSION = 0;
  public static final int ARFF_EXTENSION = 1;
  private int fileExtension = 2;

  /**Construct the frame*/
  public PreprocessorMain()
  { super("Preprocessor",true,true,true,true);
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
  { image1 = new ImageIcon("icones/abrir.gif");
    image2 = new ImageIcon("icones/help.gif");
    jPanel2 = new EditorPanel(this);
    contentPane = (JPanel) this.getContentPane();
    titledBorder5 = new TitledBorder(border5,"Select Program");
    border5 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    contentPane.setLayout(borderLayout1);
    //contentPane.setBackground(new Color(216, 208, 200));
    //jTabbedPane1.setBackground(jPanel41.getBackground());
    jTabbedPane1.setOpaque(true);
	this.setSize(new Dimension(640, 480));
    this.setTitle("PreProcessor");
    jMenuFile.setMnemonic('F');
    jMenuFile.setText("File");
    jMenuHelp.setMnemonic('H');
    jMenuHelp.setText("Help");
    jMenuHelpAbout.setIcon(image2);
    jMenuHelpAbout.setMnemonic('E');
    jMenuHelpAbout.setText("Help Topics");
    jMenuHelpAbout.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jButton1.setIcon(image1);
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jButton1.setToolTipText("Open File");
    jButton3.setIcon(image2);
    jButton3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton3_actionPerformed(e);
      }
    });
    jButton3.setToolTipText("Help");
    jPanel41.setLayout(borderLayout2);
    jToolBar.setFloatable(false);
    jPanel41.setBorder(titledBorder5);
    titledBorder5.setTitle("Status");
    statusBar.setText("Welcome");
    jMenuItem1.setIcon(image1);
    jMenuItem1.setMnemonic('O');
    jMenuItem1.setText("Open ...");
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    jMenuFileExit.setMnemonic('E');
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jPanel2.setEnabled(false);
    jToolBar.add(jButton1);
    jToolBar.add(jButton3);
    jMenuFile.add(jMenuItem1);
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(jToolBar, BorderLayout.NORTH);
    contentPane.add(jPanel41,  BorderLayout.SOUTH);
    jPanel41.add(statusBar, BorderLayout.CENTER);
    contentPane.add(jTabbedPane1, BorderLayout.CENTER);
    jTabbedPane1.add(jPanel1,  "Preprocess");
    jTabbedPane1.add(jPanel2,   "Editor");
    jTabbedPane1.setEnabledAt(1,false);
  }
  /**File | Exit action performed*/
  public void jMenuFileExit_actionPerformed(ActionEvent e)
  {
    dispose();
  }
  /**Help | About action performed*/
  public void jMenuHelpAbout_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      try
      {   URL helpSetURL = new URL("file:./help/Preprocessor.hs");
          HelpSet set = new HelpSet(null, helpSetURL);
          JHelp help = new JHelp(set);
          JFrame f = new JFrame();
          f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          f.setContentPane(help);
          f.setSize(500,400);
          f.setVisible(true);
      }
      catch (Exception evt)
      {   evt.printStackTrace();
          statusBar.setText("Error= "+evt.getMessage());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
  /**Overridden so we can exit when window is closed*/
  /*protected void processWindowEvent(WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      jMenuFileExit_actionPerformed(null);
    }
  }*/

  void jButton1_actionPerformed(ActionEvent e)
  {   openFile();
  }

  private void openFile()
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ARFF"};
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(PreprocessorMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          setBaseInstancesFromFile(selectedFile);
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void setBaseInstancesFromFile(File f)
  {   try
      {   jTabbedPane1.setEnabledAt(1,false);
          Reader r = new BufferedReader(new FileReader(f));
	  Loader loader;
          String fileName = f.getName();
          if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
          {   loader = new ArffLoader(r,this);
              fileExtension = ARFF_EXTENSION;
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
          {   loader = new TxtLoader(r,this);
              fileExtension = TXT_EXTENSION;
          }
          else
          {   throw new IOException(" Extensão de arquivo não conhecida.");
          }
          jTabbedPane1.setSelectedIndex(0);
          inst = loader.getInstances();
          jPanel1.setBaseInstances(inst);
          statusBar.setText("File opened successfully");
          this.setTitle("PreProcessor - "+f.getName());
          r.close();
      }
      catch (NullPointerException npe)
      {   statusBar.setText("NullPointer error "+npe.getMessage());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText("FileNotFound error "+fnfe.getMessage());
      }
      catch (IOException ioe)
      {   statusBar.setText("ErrorOpen error "+ioe.getMessage());
      }
      catch(Exception e)
      {   statusBar.setText("Exception error "+e.getMessage());
          e.printStackTrace();
      }
  }

  public void setStatusBar(String text)
  {   statusBar.setText(text);
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {   openFile();
  }

  void jButton3_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  public void setEditorText(String text)
  {   jPanel2.setText(text);
  }

  public int getFileExtension()
  {   return fileExtension;
  }

  public File getCurrentDirectory()
  {   return fileChooser.getCurrentDirectory();
  }

  public JTabbedPane getTabbedPane()
  {   return jTabbedPane1;
  }
}