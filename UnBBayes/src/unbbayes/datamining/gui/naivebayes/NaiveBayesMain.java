package unbbayes.datamining.gui.naivebayes;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.URL;
import java.util.*;
import javax.help.*;
import javax.swing.*;
import javax.swing.border.*;
import unbbayes.controlador.WindowController;
import unbbayes.fronteira.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.io.*;
import unbbayes.jprs.jbn.ProbabilisticNetwork;

public class NaiveBayesMain extends /*JFrame*/JInternalFrame
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
  private JButton jButton7 = new JButton();
  private JButton jButton8 = new JButton();
  private JButton jButton9 = new JButton();
  private JButton jButton10 = new JButton();
  private ImageIcon image1;
  private ImageIcon image2;
  private ImageIcon image3;
  private ImageIcon image4;
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private AttributePanel jPanel4;
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JPanel jPanel1;
  private BorderLayout borderLayout2 = new BorderLayout();
  private JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
  private JPanel jPanel2 = new JPanel();
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
  /**Component initialization*/
  private void jbInit() throws Exception
  { image1 = new ImageIcon("icones/abrir.gif");
    image2 = new ImageIcon("icones/compila.gif");
    image3 = new ImageIcon("icones/help.gif");
    image4 = new ImageIcon("icones/salvar.gif");
    contentPane = (JPanel) this.getContentPane();
    jPanel1 = new JPanel();
    border1 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    jTabbedPane1.setOpaque(true);
    titledBorder1 = new TitledBorder(border1,"Status");
    contentPane.setLayout(borderLayout1);
    this.setJMenuBar(jMenuBar1);
    this.setSize(new Dimension(640,480));
    jMenu1.setMnemonic(((Character)resource.getObject("fileMnemonic")).charValue());
    jMenu1.setText(resource.getString("fileMenu"));
    jMenuItem1.setIcon(image1);
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
    jMenuItem3.setIcon(image3);
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
    jMenuItem4.setIcon(image2);
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
    jMenuItem5.setIcon(image4);
    jMenuItem5.setMnemonic(((Character)resource.getObject("saveNetworkMnemonic")).charValue());
    jMenuItem5.setText(resource.getString("saveNetworkMenu"));
    jMenuItem5.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem5_actionPerformed(e);
      }
    });
    jButton10.setToolTipText(resource.getString("openFileTooltip"));
    jButton10.setIcon(image1);
    jButton10.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton10_actionPerformed(e);
      }
    });
    jButton9.setEnabled(false);
    jButton9.setToolTipText(resource.getString("saveFileTooltip"));
    jButton9.setIcon(image4);
    jButton9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton9_actionPerformed(e);
      }
    });
    jButton8.setEnabled(false);
    jButton8.setToolTipText(resource.getString("learnDataTooltip"));
    jButton8.setIcon(image2);
    jButton8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton8_actionPerformed(e);
      }
    });
    jButton7.setToolTipText(resource.getString("helpFileTooltip"));
    jButton7.setIcon(image3);
    jButton7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton7_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    jPanel1.setLayout(borderLayout2);
    jPanel2.setLayout(borderLayout5);
    statusBar.setText(resource.getString("welcome"));
    jPanel2.setBorder(titledBorder1);
    contentPane.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(jButton10, null);
    jToolBar1.add(jButton9, null);
    jToolBar1.add(jButton8, null);
    jToolBar1.add(jButton7, null);
    contentPane.add(jTabbedPane1, BorderLayout.CENTER);
    jPanel4 = new AttributePanel();
    jTabbedPane1.add(jPanel4,  resource.getString("attributes2"));
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

  private void setBaseInstancesFromFile(File f)
  {   try
      {   jTabbedPane1.setEnabledAt(0,false);
          Reader r = new BufferedReader(new FileReader(f));
	  Loader loader;
          String fileName = f.getName();
          if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
          {   loader = new ArffLoader(r,this);
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
          {   loader = new TxtLoader(r,this);
          }
          else
          {   throw new IOException(resource.getString("fileExtensionException"));
          }
          setTitle("Naive Bayes - "+fileName);
          boolean bool = loader.getInstances().checkNumericAttributes();
          if (bool == true)
              throw new Exception(resource.getString("numericAttributesException"));
          inst = loader.getInstances();
          jPanel4.enableComboBox(true);
          jPanel4.setInstances(inst);
          jTabbedPane1.setEnabledAt(0,true);
          jTabbedPane1.setSelectedIndex(0);
          jTabbedPane1.setEnabledAt(1,false);
          jMenuItem4.setEnabled(true);
          jButton8.setEnabled(true);
          jMenuItem5.setEnabled(false);
          jButton9.setEnabled(false);
          int numAtt = inst.numAttributes();
          statusBar.setText(resource.getString("openFile"));
          r.close();
      }
      catch (NullPointerException npe)
      {   statusBar.setText(resource.getString("errorBD")+f.getName()+" "+npe.getMessage());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText(resource.getString("fileNotFound")+f.getName()+" "+fnfe.getMessage());
      }
      catch (IOException ioe)
      {   statusBar.setText(resource.getString("errorOpen")+f.getName()+" "+ioe.getMessage());
      }
      catch (Exception e)
      {   statusBar.setText(resource.getString("error")+e.getMessage());
      }
  }

  void jMenuItem3_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      try
      {   URL helpSetURL = new URL("file:./help/Naive_Bayes.hs");
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
          statusBar.setText(resource.getString("error2")+evt.getMessage());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jMenuItem4_actionPerformed(ActionEvent e)
  {   if (inst != null)
      {   ComputeProbabilisticNetwork trp = new ComputeProbabilisticNetwork();
          try
          {   trp.setInstances(inst);
              net = trp.getProbabilisticNetwork();
              jMenuItem5.setEnabled(true);
              jTabbedPane1.setEnabledAt(1,true);
              jTabbedPane1.setSelectedIndex(1);
              jButton9.setEnabled(true);

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
              statusBar.setText(resource.getString("learnSuccessful"));
          }
          catch (Exception ex)
          {   statusBar.setText(resource.getString("exception")+ex.getMessage());
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
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(NaiveBayesMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          setBaseInstancesFromFile(selectedFile);
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jMenuItem5_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s2 = {"net"};
      fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
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
          }
          catch (Exception ioe)
          {   statusBar.setText(resource.getString("errorWritingFileException")+selectedFile.getName()+" "+ioe.getMessage());
          }
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

  }

  void jButton7_actionPerformed(ActionEvent e)
  {   jMenuItem3_actionPerformed(e);
  }

  void jButton10_actionPerformed(ActionEvent e)
  {   jMenuItem1_actionPerformed(e);
  }

  void jButton9_actionPerformed(ActionEvent e)
  {   jMenuItem5_actionPerformed(e);
  }

  void jButton8_actionPerformed(ActionEvent e)
  {   jMenuItem4_actionPerformed(e);
  }


}