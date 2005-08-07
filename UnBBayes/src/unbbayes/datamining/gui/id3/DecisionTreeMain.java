package unbbayes.datamining.gui.id3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.controller.*;
import unbbayes.datamining.classifiers.decisiontree.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.gui.*;
import unbbayes.gui.*;

public class DecisionTreeMain extends JInternalFrame
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

  private JPanel contentPane;
  private JMenuBar jMenuBar = new JMenuBar();
  private JMenu jMenuFile = new JMenu();
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout = new JMenuItem();
  private JToolBar jToolBar = new JToolBar();
  private JButton openFileButton = new JButton();
  private JButton learnButton = new JButton();
  private JMenuItem jMenuFileOpen = new JMenuItem();
  private JMenuItem jMenuFileExit = new JMenuItem();
  private JMenuItem jMenuFileBuild = new JMenuItem();
  private InstanceSet inst;
  private ResourceBundle resource;
  private ImageIcon abrirIcon;
  private ImageIcon openModelIcon;
  private ImageIcon compilaIcon;
  private ImageIcon helpIcon;
  private ImageIcon salvarIcon;
  private JMenuItem jMenuItem2 = new JMenuItem();
  private JMenu jMenu1 = new JMenu();
  private JMenuItem jMenuItem1 = new JMenuItem();
  private DecisionTreeLearning id3;
  private JButton saveModelButton = new JButton();
  private JButton openModelButton = new JButton();
  private JButton helpButton = new JButton();
  private JPanel jPanel1 = new JPanel();
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private Border border1;
  private TitledBorder titledBorder1;
  private JFileChooser fileChooser;
  private JPanel jPanel2 = new JPanel();
  private InductionPanel inductionFrame;
  private JTabbedPane jTabbedPane = new JTabbedPane();
  private AttributePanel attributeFrame;
  private BorderLayout borderLayout3 = new BorderLayout();

  /**Construct the frame*/
  public DecisionTreeMain()
  { super("Id3 Classifier",true,true,true,true);
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.id3.resources.DecisiontreeResource");
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
  {
    IconController iconController = IconController.getInstance();
    abrirIcon = iconController.getOpenIcon();
	openModelIcon = iconController.getOpenModelIcon();
    compilaIcon = iconController.getCompileIcon();
    helpIcon = iconController.getHelpIcon();
    salvarIcon = iconController.getSaveIcon();
    contentPane = (JPanel) this.getContentPane();
    titledBorder1 = new TitledBorder(border1,"Status");
    inductionFrame = new InductionPanel();
    attributeFrame = new AttributePanel();
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
    openFileButton.setIcon(abrirIcon);
    openFileButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openFileButton_actionPerformed(e);
      }
    });
    openFileButton.setToolTipText(resource.getString("openTooltip"));
    learnButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        learnButton_actionPerformed(e);
      }
    });
    learnButton.setEnabled(false);
    learnButton.setToolTipText(resource.getString("buildTooltip"));
    learnButton.setIcon(compilaIcon);
    jMenuFileOpen.setText(resource.getString("open"));
    jMenuFileOpen.setIcon(abrirIcon);
    jMenuFileOpen.setMnemonic(((Character)resource.getObject("openMnemonic")).charValue());
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
    jMenuFileBuild.setIcon(compilaIcon);
    jMenuFileBuild.setMnemonic(((Character)resource.getObject("buildMnemonic")).charValue());
    jMenuFileBuild.addActionListener(new java.awt.event.ActionListener()
    {
      	public void actionPerformed(ActionEvent e)
      	{
        	jMenuFileBuild_actionPerformed(e);
      	}
    });

    jToolBar.setFloatable(false);
    jMenuItem2.setEnabled(false);
    jMenuItem2.setIcon(salvarIcon);
    jMenuItem2.setMnemonic(((Character)resource.getObject("saveModelMnemonic")).charValue());
    jMenuItem2.setText(resource.getString("saveModel"));
    jMenuItem2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenu1.setMnemonic(((Character)resource.getObject("learnMnemonic")).charValue());
    jMenu1.setText(resource.getString("learn"));
    jMenuItem1.setIcon(openModelIcon);
    jMenuItem1.setMnemonic(((Character)resource.getObject("openModelMnemonic")).charValue());
    jMenuItem1.setText(resource.getString("openModel"));
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    helpButton.setToolTipText(resource.getString("callHelpFile"));
    helpButton.setIcon(helpIcon);
    helpButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        helpButton_actionPerformed(e);
      }
    });
    openModelButton.setToolTipText(resource.getString("openAModel"));
    openModelButton.setIcon(openModelIcon);
    openModelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openModelButton_actionPerformed(e);
      }
    });
    saveModelButton.setEnabled(false);
    saveModelButton.setToolTipText(resource.getString("saveAModel"));
    saveModelButton.setIcon(salvarIcon);
    saveModelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveModelButton_actionPerformed(e);
      }
    });
    statusBar.setBorder(titledBorder1);
    statusBar.setText(resource.getString("welcome"));
    jPanel1.setLayout(borderLayout2);
    jPanel2.setLayout(borderLayout3);

    Dimension separador = new Dimension(5,0);
    jToolBar.add(openFileButton);
    jToolBar.add(learnButton);
	jToolBar.addSeparator(separador);
    jToolBar.add(openModelButton);
    jToolBar.add(saveModelButton);
	jToolBar.addSeparator(separador);
	jToolBar.add(helpButton);

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
    contentPane.add(jPanel1,  BorderLayout.SOUTH);
    jPanel1.add(statusBar, BorderLayout.CENTER);
    contentPane.add(jPanel2, BorderLayout.CENTER);
    jPanel2.add(jTabbedPane, BorderLayout.CENTER);
    jTabbedPane.add(attributeFrame, resource.getString("attributes"));
    jTabbedPane.add(inductionFrame, resource.getString("inference"));
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
  {   try
      {   FileController.getInstance().openHelp(this);
      }
      catch (Exception evt)
      {   statusBar.setText("Error= "+evt.getMessage()+" "+this.getClass().getName());
      }
  }

  /**File | Open action performed*/
  void openFileButton_actionPerformed(ActionEvent e)
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
          for(int i=0; i<2; i++)
              jTabbedPane.setEnabledAt(i,false);
          learnButton.setEnabled(false);
          jMenuFileBuild.setEnabled(false);
          this.setTitle("Id3 Decision Tree - "+selectedFile.getName());
          jTabbedPane.setEnabledAt(0,true);
          jTabbedPane.setSelectedIndex(0);
          attributeFrame.setInstances(inst);
          attributeFrame.enableComboBox(true);
          learnButton.setEnabled(true);
          jMenuFileBuild.setEnabled(true);
          statusBar.setText(resource.getString("fileOpenedSuccessfully"));
        }
        else
        {
          statusBar.setText("Operação cancelada");
        }
      }
      catch (NullPointerException npe)
      {   statusBar.setText(resource.getString("nullPointerException") + selectedFile.getName());
      }
      catch (FileNotFoundException fnfe)
      {   statusBar.setText(resource.getString("fileNotFoundException") + selectedFile.getName());
      }
      catch (IOException ioe)
      {   statusBar.setText(resource.getString("ioException1") + selectedFile.getName() + resource.getString("ioException2"));
      }
      catch(Exception ex)
      {   statusBar.setText(resource.getString("exception")+ex.getMessage());
      }
  }

  void jMenuFileOpen_actionPerformed(ActionEvent e)
  {   openFileButton_actionPerformed(e);
  }

  void learnButton_actionPerformed(ActionEvent evt)
  {   jTabbedPane.setSelectedIndex(0);
      try
      {   id3 = new Id3();
          id3.buildClassifier(inst);
          jTabbedPane.setEnabledAt(1,true);
          inductionFrame.setInstances(id3);
          id3.getTree();
          jTabbedPane.setSelectedIndex(1);
          jMenuItem2.setEnabled(true);
          saveModelButton.setEnabled(true);
          statusBar.setText(resource.getString("id3Learn"));
      }
      catch(Exception e)
      {   statusBar.setText(e.getMessage());
      	e.printStackTrace();
      }
  }

  void jMenuFileBuild_actionPerformed(ActionEvent e)
  {   learnButton_actionPerformed(e);
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"id3"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setDialogTitle(resource.getString("openModel2"));
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(DecisionTreeMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          try
          {   ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
              id3 = (DecisionTreeLearning)in.readObject();
              id3.getTree();
              jTabbedPane.setEnabledAt(1,true);
              jTabbedPane.setEnabledAt(0,false);
              learnButton.setEnabled(false);
              jMenuFileBuild.setEnabled(false);
              this.setTitle("ID3 Decision Tree - Model "+selectedFile.getName());
              statusBar.setText(resource.getString("modelOpenedSuccessfully"));
              inductionFrame.setInstances(id3);
              jTabbedPane.setSelectedIndex(1);
          }
          catch (IOException ioe)
          {   statusBar.setText(resource.getString("errorWritingFile")+selectedFile.getName()+" "+ioe.getMessage());
          }
          catch (ClassNotFoundException cnfe)
          {   statusBar.setText(cnfe.getMessage());
          }
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jMenuItem2_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"id3"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(DecisionTreeMain.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
      int returnVal = fileChooser.showSaveDialog(this);
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
          {   statusBar.setText(resource.getString("errorWritingFile")+selectedFile.getName()+" "+ioe.getMessage());
          }
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void helpButton_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  void openModelButton_actionPerformed(ActionEvent e)
  {   jMenuItem1_actionPerformed(e);
  }

  void saveModelButton_actionPerformed(ActionEvent e)
  {   jMenuItem2_actionPerformed(e);
  }
}
