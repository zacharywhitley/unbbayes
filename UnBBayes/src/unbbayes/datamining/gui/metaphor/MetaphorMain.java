package unbbayes.datamining.gui.metaphor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.controlador.*;
import unbbayes.fronteira.*;
import unbbayes.io.*;
import unbbayes.jprs.jbn.*;

/**
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.0
 */
public class MetaphorMain extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JToolBar metaphorToolBar = new JToolBar();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JPanel statusPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JLabel statusBar = new JLabel();
  private JButton diagnosticButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton openButton = new JButton();
  private JPanel jPanel1 = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JPanel descriptionPanel = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JTabbedPane descriptionTabbedPane = new JTabbedPane();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JScrollPane descriptionScrollPane = new JScrollPane();
  private JTextArea descriptionTextArea = new JTextArea();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel5 = new JPanel();
  private MetaphorTree metaphorTree;
  private BorderLayout borderLayout7 = new BorderLayout();
  private ImageIcon openMetaphorIcon;
  private ImageIcon saveMetaphorIcon;
  private ImageIcon diagnosticMetaphorIcon;
  private ImageIcon openMetaphorRollOverIcon;
  private ImageIcon saveMetaphorRollOverIcon;
  private ImageIcon diagnosticMetaphorRollOverIcon;
  private ProbabilisticNetwork net = new ProbabilisticNetwork();
  private JFileChooser fileChooser;
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel6 = new JPanel();
  private JPanel jPanel7 = new JPanel();
  private BorderLayout borderLayout6 = new BorderLayout();
  private BorderLayout borderLayout8 = new BorderLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JTextArea jTextArea1 = new JTextArea();
  private JTextArea jTextArea2 = new JTextArea();
  private TitledBorder titledBorder1;
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JTextArea jTextArea3 = new JTextArea();
  private JTextArea jTextArea4 = new JTextArea();
  private JScrollPane jScrollPane3;
  private Border border1;
  private Border border2;
  private Border border3;

  public MetaphorMain()
  { try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  { openMetaphorIcon = new ImageIcon(getClass().getResource("/icones/openMetaphor.gif"));
    saveMetaphorIcon = new ImageIcon(getClass().getResource("/icones/saveMetaphor.gif"));
    diagnosticMetaphorIcon = new ImageIcon(getClass().getResource("/icones/diagnosticMetaphor.gif"));
    openMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icones/openSMetaphor.gif"));
    saveMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icones/saveSMetaphor.gif"));
    diagnosticMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icones/diagnosticSMetaphor.gif"));
    titledBorder1 = new TitledBorder("");
    border1 = BorderFactory.createEmptyBorder(5,5,5,5);
    border2 = BorderFactory.createEmptyBorder(5,5,5,5);
    border3 = BorderFactory.createEmptyBorder(5,5,5,5);
    this.setLayout(borderLayout1);
    statusPanel.setLayout(borderLayout2);
    statusPanel.setBorder(new TitledBorder("Status"));
    statusBar.setToolTipText("");
    statusBar.setText("Welcome");
    metaphorToolBar.setFloatable(false);
    jTabbedPane1.setOpaque(true);
    jPanel1.setLayout(borderLayout3);
    jPanel2.setLayout(borderLayout4);
    descriptionPanel.setLayout(borderLayout5);
    descriptionTextArea.setBackground(new Color(255, 255, 210));
    descriptionTextArea.setEditable(false);
    jPanel4.setLayout(gridLayout1);
    jPanel3.setLayout(borderLayout7);
    openButton.setBorder(border1);
    openButton.setIcon(openMetaphorIcon);
    openButton.setRolloverIcon(openMetaphorRollOverIcon);
    openButton.setPressedIcon(openMetaphorRollOverIcon);
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openButton_actionPerformed(e);
      }
    });
    saveButton.setBorder(border2);
    saveButton.setIcon(saveMetaphorIcon);
    saveButton.setRolloverIcon(saveMetaphorRollOverIcon);
    saveButton.setPressedIcon(saveMetaphorRollOverIcon);
    diagnosticButton.setBorder(border3);
    diagnosticButton.setIcon(diagnosticMetaphorIcon);
    diagnosticButton.setRolloverIcon(diagnosticMetaphorRollOverIcon);
    diagnosticButton.setPressedIcon(diagnosticMetaphorRollOverIcon);
    diagnosticButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        diagnosticButton_actionPerformed(e);
      }
    });
    gridLayout1.setColumns(1);
    gridLayout1.setHgap(5);
    gridLayout1.setRows(2);
    gridLayout1.setVgap(5);
    jPanel6.setLayout(borderLayout6);
    jPanel7.setLayout(borderLayout8);
    jLabel1.setText("Prováveis diagnósticos:");
    jLabel2.setText("Diagnósticos não prováveis:");
    jTextArea1.setBorder(BorderFactory.createEtchedBorder());
    jTextArea1.setEditable(false);
    jTextArea2.setBorder(BorderFactory.createEtchedBorder());
    jTextArea2.setEditable(false);
    borderLayout4.setHgap(5);
    borderLayout6.setVgap(5);
    borderLayout8.setVgap(5);
    jTextArea3.setBackground(new Color(255, 255, 210));
    jTextArea4.setBackground(new Color(255, 255, 210));
    jPanel3.setBorder(BorderFactory.createLoweredBevelBorder());
    this.add(metaphorToolBar, BorderLayout.NORTH);
    metaphorToolBar.add(openButton, null);
    metaphorToolBar.add(saveButton, null);
    metaphorToolBar.add(diagnosticButton, null);
    this.add(jTabbedPane1, BorderLayout.CENTER);
    this.add(statusPanel,  BorderLayout.SOUTH);
    statusPanel.add(statusBar, BorderLayout.NORTH);
    jTabbedPane1.add(jPanel1,  "Entrada de evidencias");
    jTabbedPane1.add(jPanel2,   "Laudo");
    jPanel2.add(jPanel4,  BorderLayout.WEST);
    jPanel4.add(jPanel6, null);
    jPanel6.add(jLabel1,  BorderLayout.NORTH);
    jPanel6.add(jTextArea1, BorderLayout.CENTER);
    jPanel4.add(jPanel7, null);
    jPanel7.add(jLabel2,  BorderLayout.NORTH);
    jPanel7.add(jTextArea2, BorderLayout.CENTER);
    jPanel2.add(descriptionPanel, BorderLayout.CENTER);
    descriptionPanel.add(descriptionTabbedPane,  BorderLayout.CENTER);
    descriptionTabbedPane.add(descriptionScrollPane,   "Descrição do diagnóstico");
    descriptionTabbedPane.add(jScrollPane1,  "Laudo");
    jScrollPane1.getViewport().add(jTextArea3, null);
    descriptionTabbedPane.add(jScrollPane2,  "Frases de Apoio");
    jScrollPane2.getViewport().add(jTextArea4, null);
    descriptionScrollPane.getViewport().add(descriptionTextArea, null);
    jPanel1.add(jPanel3, BorderLayout.CENTER);
    jScrollPane3 = new JScrollPane(metaphorTree);
    jPanel3.add(jScrollPane3,  BorderLayout.CENTER);
    jPanel1.add(jPanel5,  BorderLayout.EAST);
  }

  void diagnosticButton_actionPerformed(ActionEvent e)
  {   jTabbedPane1.setSelectedIndex(1);

  }

  void openButton_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"net"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "Networks (*.net)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          openNetFile(selectedFile);
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void openNetFile(File selectedFile)
  {   try
      {   net = new NetIO().load(selectedFile);
          net.compile();
          metaphorTree = new MetaphorTree(net,false);
          metaphorTree.expandTree();
          jPanel3.removeAll();
          jPanel3.add(metaphorTree,  BorderLayout.CENTER);
          statusBar.setText("File opened successfully");
      }
      catch (Exception e)
      {   net = null;
          System.err.print(e.getMessage());
          statusBar.setText(e.getMessage());
      }
  }

}