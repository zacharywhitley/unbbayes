package unbbayes.datamining.gui.ban;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.controller.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.gui.*;
import unbbayes.datamining.gui.naivebayes.NaiveBayesMain;
import unbbayes.gui.*;
import unbbayes.io.*;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.*;
import unbbayes.util.NodeList;

public class BanMain extends JInternalFrame
{

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

  private JPanel contentPane;
  private MDIDesktopPane desktop;
  private InstanceSet inst;
  public File selectedFile;
  private ConstructionController cc;
  private int classec;
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
  public BanMain()
  { super("BAN Classifier",true,true,true,true);
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.ban.resources.BanResource");

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
    desktop = new MDIDesktopPane();
    contentPane.add(new JScrollPane(desktop), BorderLayout.CENTER);
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
    	  
    	  		
    	   	  /*NaiveBayes naiveBayes = new NaiveBayes();
          	  naiveBayes.buildClassifier(inst);
          	  net = naiveBayes.getProbabilisticNetwork();
          	  jMenuItem5.setEnabled(true);
              jTabbedPane1.setEnabledAt(1,true);
              jTabbedPane1.setSelectedIndex(1);
              saveButton.setEnabled(true);*/
              /*Edge arco;
              NodeList pais;
              pais.a
              pais.remove()
              net.getNodeAt(1).setParents()
              net.addEdge()*/
              

              /*NetWindow netWindow = new NetWindow(net);
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
              edition.getHierarchy().setVisible(false);*/
    	  classec=inst.getClassIndex()-1;
    	  cc= new ConstructionController(selectedFile,classec,this);
              // mostra a nova tela
              jPanel1.removeAll();
              jPanel1.setLayout(new BorderLayout());
              //
              //jPanel1.add(netWindow.getContentPane(),BorderLayout.CENTER);
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
      {   selectedFile = fileChooser.getSelectedFile();
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
          //cc= new ConstructionController(selectedFile,0);
          jTabbedPane1.setEnabledAt(0,false);
          setTitle("BAN - "+selectedFile.getName());
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
      fileChooser.setFileView(new FileIcon(BanMain.this));
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

  public void addWindow(JInternalFrame newWindow) {
		desktop.add(newWindow);
		}
  
  public ProbabilisticNetwork makeNetwork(NodeList variaveis) {
	  ProbabilisticNetwork net = new ProbabilisticNetwork("learned net");
      Node noFilho = null;
      Node noPai = null;
      Edge arcoAux = null;
      Node aux;
      boolean direction = true;
      for (int i = 0; i < variaveis.size(); i++) {
          noFilho = variaveis.get(i);
          net.addNode(noFilho);
          for (int j = 0; j < noFilho.getParents().size(); j++) {
          	noPai = (Node)noFilho.getParents().get(j);
          	noPai.getChildren().add(noFilho);
              arcoAux = new Edge(noPai, noFilho);
          	for(int k = 0 ; k < noPai.getParents().size() && direction; k++){
          	    aux = (Node)noPai.getParents().get(k);
          	    if(aux == noFilho){
          	        noPai.getParents().remove(k);
          	        direction = false;
          	    }                      		
          	}                 
              arcoAux = new Edge(noPai, noFilho);                
            	arcoAux.setDirection(direction);                	
            	direction = true;
              net.getEdges().add(arcoAux);
          }
      }        		
		return net;
      /*ProbabilisticNetwork net = new ProbabilisticNetwork("learned net");
      Node noFilho = null;
      Node noPai = null;
      Edge arcoAux = null;
      Node aux;
      boolean japai;
      boolean direction = true;
      for (int i = 0; i < variaveis.size(); i++) {
          noFilho = variaveis.get(i);
          japai=false;
          for(int k=0;k<noFilho.getParents().size();k++){
          if(noFilho.getParents().get(k).getName()==variaveis.get(classec).getName()){
        	japai=true;  
          }
          if(japai=false){
        	  NodeList ppais=noFilho.getParents();
        	  ppais.add(variaveis.get(classec));
        	  noFilho.setParents(ppais);
          }
          }
          japai=false;
          for(int k=0;k<noFilho.getChildren().size();k++){
              if(noFilho.getChildren().get(k).getName()==variaveis.get(classec).getName()){
            	japai=true;  
              }
              if(japai=true){
            	  NodeList ppais=noFilho.getChildren();
            	  for(int l=0;l<ppais.size();l++){
            		  if(ppais.get(l).getName()==variaveis.get(classec).getName()){
            	  ppais.remove(l);
            		  }
            	  }
            	  noFilho.setChildren(ppais);
              }
              }
          net.addNode(noFilho);
          for (int j = 0; j < noFilho.getParents().size(); j++) {
          	noPai = (Node)noFilho.getParents().get(j);
          	noPai.getChildren().add(noFilho);
              arcoAux = new Edge(noPai, noFilho);
          	for(int k = 0 ; k < noPai.getParents().size() && direction; k++){
          	    aux = (Node)noPai.getParents().get(k);
          	    if(aux == noFilho){
          	        noPai.getParents().remove(k);
          	        direction = false;
          	    }                      		
          	}                 
              arcoAux = new Edge(noPai, noFilho);                
            	arcoAux.setDirection(direction);                	
            	direction = true;
              net.getEdges().add(arcoAux);
          }
      }        		
		return net;*/
  }
  
 
  public void showNetwork(ProbabilisticNetwork net){

  	NetWindow netWindow = new NetWindow(net);
	/*	if (! netWindow.getWindowController().compileNetwork()) {
          netWindow.changeToNetEdition();            
          
      } else{
          netWindow.changeToNetCompilation();		
		}*/
		//
		//NetWindow netWindow = new NetWindow(net);
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

		//
		//netWindow.show();
		//this.addWindow(netWindow);
		jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout());
        
        jPanel1.add(netWindow.getContentPane(),BorderLayout.CENTER);
		//jPanel1.add(netWindow.getContentPane(),BorderLayout.CENTER);
		    	
  }
	
}
