package unbbayes.datamining.gui.evaluation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.help.*;
import javax.swing.*;
import javax.swing.border.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.fronteira.*;

public class EvaluationMain extends /*JFrame*/JInternalFrame
{ private ImageIcon image1;
  private ImageIcon image2;
  private JPanel contentPane;
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenuFile = new JMenu();
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout = new JMenuItem();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JPanel jPanel41 = new JPanel();
  private TitledBorder titledBorder5;
  private Border border5;
  private EvaluationPanel jPanel2 = new EvaluationPanel(this);
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private InstanceSet inst;
  private JMenuItem jMenuItem1 = new JMenuItem();
  private JMenuItem jMenuFileExit = new JMenuItem();
  private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  private JToolBar jToolBar1 = new JToolBar();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();

  /**Construct the frame*/
  public EvaluationMain()
  { super("Evaluation",true,true,true,true);
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
    contentPane = (JPanel) this.getContentPane();
    titledBorder5 = new TitledBorder(border5,"Select Program");
    border5 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    contentPane.setLayout(borderLayout1);
    //jTabbedPane1.setBackground(jPanel41.getBackground());
    jTabbedPane1.setOpaque(true);
	//contentPane.setBackground(new Color(216, 208, 200));
    this.setSize(new Dimension(640,480));
    this.setTitle("Evaluation");
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
    jPanel41.setLayout(borderLayout2);
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
    jButton2.setIcon(image1);
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jButton1.setIcon(image2);
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    jMenuFile.add(jMenuItem1);
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(jPanel41,  BorderLayout.SOUTH);
    jPanel41.add(statusBar, BorderLayout.CENTER);
    contentPane.add(jPanel2,BorderLayout.CENTER);
    contentPane.add(jTabbedPane1, BorderLayout.CENTER);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(jButton2, null);
    jToolBar1.add(jButton1, null);
    jTabbedPane1.add(jPanel2,  "Evaluation");
    jTabbedPane1.setEnabledAt(0,false);
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
      {   URL helpSetURL = new URL("file:./help/Evaluation.hs");
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

  private void setBaseInstancesFromFile(File f)
  {   try
      {   Reader r = new BufferedReader(new FileReader(f));
	  Loader loader;
          String fileName = f.getName();
          if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
          {   loader = new ArffLoader(r,this);
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
          {   loader = new TxtLoader(r,this);
          }
          else
          {   throw new IOException(" Extensão de arquivo não conhecida.");
          }
          inst = loader.getInstances();
          jPanel2 = new EvaluationPanel(this);
          jTabbedPane1.remove(0);
          jTabbedPane1.add(jPanel2,  "Evaluation");
          jPanel2.setInstances(inst);
          jTabbedPane1.setEnabledAt(0,true);
          jTabbedPane1.setSelectedIndex(0);
          statusBar.setText("File opened successfully");
          this.setTitle("Evaluation - "+f.getName());
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
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ARFF"};
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EvaluationMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          setBaseInstancesFromFile(selectedFile);
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   jMenuItem1_actionPerformed(e);
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  public File getCurrentDirectory()
  {   return fileChooser.getCurrentDirectory();
  }

}