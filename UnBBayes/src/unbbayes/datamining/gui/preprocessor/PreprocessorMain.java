package unbbayes.datamining.gui.preprocessor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.gui.FileIcon;
import unbbayes.gui.SimpleFileFilter;

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
  private JButton saveButton = new JButton();
  private JButton helpButton = new JButton();
  private ImageIcon openIcon;
  private ImageIcon saveIcon;
  private ImageIcon helpIcon;
  private BorderLayout borderLayout1 = new BorderLayout();
  //private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JPanel jPanel41 = new JPanel();
  private TitledBorder titledBorder5;
  private Border border5;
  private PreprocessPanel jPanel1 = new PreprocessPanel(this);
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private InstanceSet inst;
  private JMenuItem jMenuFileOpen = new JMenuItem();
  private JMenuItem jMenuFileExit = new JMenuItem();
  private JFileChooser fileChooser;
  private JPanel jPanel3 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  protected IconController iconController = IconController.getInstance();
  JMenuItem jMenuFileSave = new JMenuItem();

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
  { openIcon = iconController.getOpenIcon();
    saveIcon = iconController.getSaveIcon();
    helpIcon = iconController.getHelpIcon();
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
    openButton.setIcon(openIcon);
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openButton_actionPerformed(e);
      }
    });
    openButton.setToolTipText(resource.getString("openFile"));
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveButton_actionPerformed(e);
      }
    });
    saveButton.setEnabled(false);
    saveButton.setToolTipText(resource.getString("saveFile"));
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
    jMenuFileOpen.setIcon(openIcon);
    jMenuFileOpen.setMnemonic(((Character)resource.getObject("openMnemonic")).charValue());
    jMenuFileOpen.setText(resource.getString("open"));
    jMenuFileOpen.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuFileOpen_actionPerformed(e);
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
    jPanel3.setLayout(borderLayout3);
    jMenuFileSave.setEnabled(false);
    jMenuFileSave.setIcon(saveIcon);
    jMenuFileSave.setMnemonic(((Character)resource.getObject("saveMnemonic")).charValue());
    jMenuFileSave.setText(resource.getString("save"));
    jMenuFileSave.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveButton_actionPerformed(e);
      }
    });
    jToolBar.add(openButton);
    jToolBar.add(saveButton);
    jToolBar.add(helpButton);
    jMenuFile.add(jMenuFileOpen);
    jMenuFile.add(jMenuFileSave);
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(jToolBar,  BorderLayout.NORTH);
    contentPane.add(jPanel41,  BorderLayout.SOUTH);
    jPanel41.add(statusBar, BorderLayout.CENTER);
    //jTabbedPane1.add(jPanel1, "jPanel1");
    contentPane.add(jPanel3,  BorderLayout.CENTER);
    //jPanel3.add(jTabbedPane1,BorderLayout.CENTER);
    jPanel3.add(jPanel1,BorderLayout.CENTER);
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

  void saveButton_actionPerformed(ActionEvent e)
  {
    int[] selectedAttributes = jPanel1.getAttributePanel().getSelectedAttributes();
    if (selectedAttributes.length == 0)
    {
      statusBar.setText("Nenhum atributo selecionado");
      return;
    }
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] s1 = {"ARFF"};
    String[] s2 = {"TXT"};
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(this));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
    int returnVal = fileChooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File selectedFile = fileChooser.getSelectedFile();
      try
      {
        String fileName = selectedFile.getName();
        String selectedFilter = fileChooser.getFileFilter().getDescription();
        if (selectedFilter.equals("TxtFiles (*.txt)"))
        {
          if (!fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
          {
            selectedFile = new File(selectedFile.getAbsolutePath()+".txt");
          }
        }
        else if (selectedFilter.equals("ArffFiles (*.arff)"))
        {
          if (!fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
          {
            selectedFile = new File(selectedFile.getAbsolutePath()+".arff");
          }
        }
        FileController.getInstance().saveInstanceSet(selectedFile,inst,selectedAttributes);
        statusBar.setText("Arquivo salvo com sucesso");
      }
      catch (IOException ioe)
      {
        statusBar.setText(resource.getString("errorWritingFile")+selectedFile.getName()+" "+ioe.getMessage());
      }
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
      {
		saveButton.setEnabled(false);
		jMenuFileSave.setEnabled(false);
		inst = FileController.getInstance().getInstanceSet(selectedFile,this);
        if (inst != null)
        {
          String fileName = selectedFile.getName();
          //jTabbedPane1.setSelectedIndex(0);
		  jPanel1.setBaseInstances(inst);
		  statusBar.setText(resource.getString("fileOpened"));
          this.setTitle(resource.getString("preprocessorTitle")+selectedFile.getName());
		  saveButton.setEnabled(true);
		  jMenuFileSave.setEnabled(true);
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

  public void updateInstances(InstanceSet inst)
  {   //jTabbedPane1.setSelectedIndex(0);
      this.inst = inst;
      jPanel1.setBaseInstances(inst);
  }

  public void setStatusBar(String text)
  {   statusBar.setText(text);
  }

  void jMenuFileOpen_actionPerformed(ActionEvent e)
  {   openButton_actionPerformed(e);
  }

  void helpButton_actionPerformed(ActionEvent e)
  {   jMenuHelpAbout_actionPerformed(e);
  }

  /*public JTabbedPane getTabbedPane()
  {   return jTabbedPane1;
  }*/
}