package unbbayes.datamining.gui.evaluation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.help.*;
import javax.swing.*;
import javax.swing.border.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.fronteira.*;
import unbbayes.io.*;
import unbbayes.jprs.jbn.ProbabilisticNetwork;

public class EvaluationMain extends /*JFrame*/JInternalFrame
{ private ImageIcon image1;
  private ImageIcon image2;
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
  private Classifier classifier;
  private File file;
  private boolean instOK = false;
  private JMenuItem jMenuFileExit = new JMenuItem();
  private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  private JToolBar jToolBar1 = new JToolBar();
  private JButton jButton1 = new JButton();
  private JButton jButton3 = new JButton();
  private JMenuItem jMenuItem2 = new JMenuItem();

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
    jMenuFileExit.setMnemonic('E');
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileExit_actionPerformed(e);
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
    jButton3.setToolTipText("Open a model");
    jButton3.setIcon(image1);
    jButton3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton3_actionPerformed(e);
      }
    });
    jMenuItem2.setIcon(image1);
    jMenuItem2.setMnemonic('M');
    jMenuItem2.setText("Open Model ...");
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
    jToolBar1.add(jButton3, null);
    jToolBar1.add(jButton1, null);
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
          instOK = true;
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

  void openTest()
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ARFF"};
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EvaluationMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
      fileChooser.setDialogTitle("Open Test File");
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          setBaseInstancesFromFile(selectedFile);
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  public File getCurrentDirectory()
  {   return fileChooser.getCurrentDirectory();
  }

  void jButton3_actionPerformed(ActionEvent e)
  {   jMenuItem2_actionPerformed(e);
  }

  void jMenuItem2_actionPerformed(ActionEvent e)
  {   openModel();
      openTest();
      if (instOK)
      { jPanel2.setModel(classifier,inst);
        statusBar.setText("Model opened successfully");
        this.setTitle("Evaluation - Model "+file.getName());
      }
  }

  private void openModel()
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ID3"};
      String[] s2 = {"NET"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EvaluationMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "Networks (*.net)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          setModelFromFile(selectedFile);
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void setModelFromFile(File f)
  {   try
      {   String fileName = f.getName();
          file = f;
          if (fileName.regionMatches(true,fileName.length() - 4,".id3",0,4))
          {   ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
              classifier = (Id3)in.readObject();
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".net",0,4))
          {   ProbabilisticNetwork net;
              BaseIO io = new NetIO();
              net = io.load(f);
              /*ComputeNaiveBayes computeNaiveBayes = new ComputeNaiveBayes();
              computeNaiveBayes.setProbabilisticNetwork(net);
              classifier = computeNaiveBayes.getNaiveBayes();*/
              try
              {   BayesianNetwork bayesianNetwork = new BayesianNetwork(net);
                  classifier = bayesianNetwork;
              }
              catch (Exception e)
              {   throw new Exception(e.getMessage());
              }
          }
          else
          {   throw new IOException(" Extensão de arquivo não conhecida.");
          }
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


}