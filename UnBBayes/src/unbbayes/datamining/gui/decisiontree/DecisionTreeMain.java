package unbbayes.datamining.gui.decisiontree;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.help.*;
import javax.swing.*;
import javax.swing.event.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.classifiers.Id3;
import unbbayes.fronteira.*;
import javax.swing.border.*;

public class DecisionTreeMain extends /*JFrame*/JInternalFrame
{
  private JPanel contentPane;
  private JMenuBar jMenuBar = new JMenuBar();
  private JMenu jMenuFile = new JMenu();
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout = new JMenuItem();
  private JToolBar jToolBar = new JToolBar();
  private JButton jButtonOpen = new JButton();
  private JButton jButtonBuild = new JButton();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane jTabbedPane = new JTabbedPane();
  private JMenuItem jMenuFileOpen = new JMenuItem();
  private JMenuItem jMenuFileExit = new JMenuItem();
  private JMenuItem jMenuFileBuild = new JMenuItem();
  private InstanceSet inst;
  private AttributePanel attributeFrame;
  private InductionPanel inductionFrame;
  private File selectedFile;
  private ResourceBundle resource;
  private JTree id3tree;
  private ImageIcon image1;
  private ImageIcon image2;
  private ImageIcon image3;
  private ImageIcon image4;
  private JMenuItem jMenuItem2 = new JMenuItem();
  private JMenu jMenu1 = new JMenu();
  private JMenuItem jMenuItem1 = new JMenuItem();
  private Id3 id3;
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private JButton jButton4 = new JButton();
  private JPanel jPanel1 = new JPanel();
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private Border border1;
  private TitledBorder titledBorder1;
  private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));

  /**Construct the frame*/
  public DecisionTreeMain()
  { super("Decision Tree",true,true,true,true);
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.decisiontree.resources.DecisiontreeResource");
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
    image2 = new ImageIcon("icones/compila.gif");
    image3 = new ImageIcon("icones/help.gif");
    image4 = new ImageIcon("icones/salvar.gif");
    contentPane = (JPanel) this.getContentPane();
    border1 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    titledBorder1 = new TitledBorder(border1,"Status");
    contentPane.setLayout(borderLayout1);
    //jTabbedPane.setBackground(jPanel1.getBackground());
    jTabbedPane.setOpaque(true);
	//contentPane.setBackground(new Color(216, 208, 200));
    this.setSize(new Dimension(640, 480));
    this.setTitle("ID3 Decision Tree");
    jMenuFile.setMnemonic(((Character)resource.getObject("fileMnemonic")).charValue());
    jMenuFile.setText(resource.getString("file"));
    jMenuHelp.setMnemonic(((Character)resource.getObject("helpMnemonic")).charValue());
    jMenuHelp.setText(resource.getString("help"));
    jMenuHelpAbout.setIcon(image3);
    jMenuHelpAbout.setMnemonic('E');
    jMenuHelpAbout.setText("Help Topics");
    jMenuHelpAbout.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jButtonOpen.setIcon(image1);
    jButtonOpen.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonOpen_actionPerformed(e);
      }
    });
    jButtonOpen.setToolTipText(resource.getString("openTooltip"));
    jButtonBuild.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonBuild_actionPerformed(e);
      }
    });
    jButtonBuild.setEnabled(false);
    jButtonBuild.setToolTipText(resource.getString("buildTooltip"));
    jButtonBuild.setIcon(image2);
    jMenuFileOpen.setText(resource.getString("open"));
    jMenuFileOpen.setIcon(image1);
    jMenuFileOpen.setMnemonic(((Character)resource.getObject("openMnemonic")).charValue());
    //jMenuFileOpen.setAccelerator((KeyStroke)resource.getObject("openAccelerator"));
    jMenuFileOpen.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileOpen_actionPerformed(e);
      }
    });
    jMenuFileExit.setMnemonic(((Character)resource.getObject("exitMnemonic")).charValue());
    jMenuFileExit.setText(resource.getString("exit"));
    jMenuFileExit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jMenuFileBuild.setText(resource.getString("build"));
    jMenuFileBuild.setEnabled(false);
    jMenuFileBuild.setIcon(image2);
    jMenuFileBuild.setMnemonic(((Character)resource.getObject("buildMnemonic")).charValue());
    //jMenuFileBuild.setAccelerator((KeyStroke)resource.getObject("buildAccelerator"));
    jMenuFileBuild.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileBuild_actionPerformed(e);
      }
    });
    jToolBar.setFloatable(false);
    jMenuItem2.setEnabled(false);
    jMenuItem2.setIcon(image4);
    jMenuItem2.setText("Save Model ...");
    jMenuItem2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenu1.setMnemonic('L');
    jMenu1.setText("Learning");
    jMenuItem1.setIcon(image1);
    jMenuItem1.setText("Open Model ...");
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    jButton4.setToolTipText("Call help file");
    jButton4.setIcon(image3);
    jButton4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton4_actionPerformed(e);
      }
    });
    jButton2.setToolTipText("Open a model");
    jButton2.setIcon(image1);
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jButton1.setEnabled(false);
    jButton1.setToolTipText("Save a model");
    jButton1.setIcon(image4);
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    statusBar.setBorder(titledBorder1);
    statusBar.setText(" Welcome");
    jPanel1.setLayout(borderLayout2);
    jToolBar.add(jButtonOpen);
    jToolBar.add(jButtonBuild);
    jToolBar.add(jButton4, null);
    jToolBar.addSeparator();
    jToolBar.add(jButton2, null);
    jToolBar.add(jButton1, null);
    jMenuFile.add(jMenuFileOpen);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuItem1);
    jMenuFile.add(jMenuItem2);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar.add(jMenuFile);
    jMenuBar.add(jMenu1);
    jMenuBar.add(jMenuHelp);
    this.setJMenuBar(jMenuBar);
    contentPane.add(jToolBar,  BorderLayout.NORTH);
    contentPane.add(jTabbedPane, BorderLayout.CENTER);
    attributeFrame = new AttributePanel();
    jTabbedPane.add(attributeFrame,"Attributes");
    inductionFrame = new InductionPanel();
    jTabbedPane.add(inductionFrame,"Inference");
    contentPane.add(jPanel1,  BorderLayout.SOUTH);
    jPanel1.add(statusBar, BorderLayout.CENTER);
    jMenu1.add(jMenuFileBuild);
    for(int i=0; i<2; i++)
        jTabbedPane.setEnabledAt(i,false);
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
      {   URL helpSetURL = new URL("file:./help/Decision_Tree.hs");
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

  /**File | Abrir action performed*/
  void jButtonOpen_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ARFF"};
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setDialogTitle("Open File");
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(DecisionTreeMain.this));
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
          for(int i=0; i<2; i++)
              jTabbedPane.setEnabledAt(i,false);
          jButtonBuild.setEnabled(false);
          jMenuFileBuild.setEnabled(false);
          this.setTitle("ID3 Decision Tree - "+f.getName());
          jTabbedPane.setEnabledAt(0,true);
          jTabbedPane.setSelectedIndex(0);
          attributeFrame.setInstances(inst);
          attributeFrame.enableComboBox(true);
          jButtonBuild.setEnabled(true);
          jMenuFileBuild.setEnabled(true);
          statusBar.setText("File opened successfully");
          r.close();
      }
      catch (NullPointerException npe)
      {   statusBar.setText(resource.getString("nullPointerException") + f.getName());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText(resource.getString("fileNotFoundException") + f.getName());
      }
      catch (IOException ioe)
      {   statusBar.setText(resource.getString("ioException1") + f.getName() + resource.getString("ioException2"));
      }
      catch(Exception e)
      {   statusBar.setText("Exception "+e.getMessage());
      }
  }

  void jMenuFileOpen_actionPerformed(ActionEvent e)
  {   jButtonOpen_actionPerformed(e);
  }

  void jButtonBuild_actionPerformed(ActionEvent evt)
  {   jTabbedPane.setSelectedIndex(0);
      try
      {   id3 = new Id3();
          id3.buildClassifier(inst);
          jTabbedPane.setEnabledAt(1,true);
          inductionFrame.setInstances(id3);
          id3tree = id3.getTree();
          jTabbedPane.setSelectedIndex(1);
          jMenuItem2.setEnabled(true);
          jButton1.setEnabled(true);
          statusBar.setText("ID3 learning successful");
      }
      catch(Exception e)
      {   statusBar.setText(e.getMessage());
      }
  }

  void jMenuFileBuild_actionPerformed(ActionEvent e)
  {   jButtonBuild_actionPerformed(e);
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"id3"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setDialogTitle("Open Model");
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(DecisionTreeMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          try
          {   ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
              id3 = (Id3)in.readObject();
              id3tree = id3.getTree();
              jTabbedPane.setEnabledAt(1,true);
              jTabbedPane.setEnabledAt(0,false);
              jButtonBuild.setEnabled(false);
              jMenuFileBuild.setEnabled(false);
              this.setTitle("ID3 Decision Tree - Model "+selectedFile.getName());
              statusBar.setText("Model opened successfully");
              inductionFrame.setInstances(id3);
              jTabbedPane.setSelectedIndex(1);
          }
          catch (IOException ioe)
          {   statusBar.setText("Error writing file "+selectedFile.getName()+" "+ioe.getMessage());
          }
          catch (ClassNotFoundException cnfe)
          {   statusBar.setText(cnfe.getMessage());
          }
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jMenuItem2_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"id3"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(DecisionTreeMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          try
          {   String fileName = selectedFile.getName();
              if (!fileName.regionMatches(true,fileName.length() - 4,".id3",0,4))
              {   selectedFile = new File(selectedFile.getAbsolutePath()+".id3");
              }
              ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile));
              out.writeObject(id3);
          }
          catch (IOException ioe)
          {   statusBar.setText("Error writing file "+selectedFile.getName()+" "+ioe.getMessage());
          }
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jButton4_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   jMenuItem1_actionPerformed(e);
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   jMenuItem2_actionPerformed(e);
  }
}