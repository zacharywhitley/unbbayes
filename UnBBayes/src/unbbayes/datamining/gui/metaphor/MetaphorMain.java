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
import unbbayes.util.*;

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
  private JPanel descriptionPanel = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JTabbedPane descriptionTabbedPane = new JTabbedPane();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JScrollPane descriptionScrollPane = new JScrollPane();
  private JTextArea descriptionTextArea = new JTextArea();
  private JPanel jPanel3 = new JPanel();
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
  private Border border1;
  private Border border2;
  private Border border3;
  private JTextArea jTextArea5 = new JTextArea();
  private BorderLayout borderLayout9 = new BorderLayout();
  private BorderLayout borderLayout12 = new BorderLayout();
  private BorderLayout borderLayout11 = new BorderLayout();
  private JPanel jPanel9 = new JPanel();
  private JLabel jLabel3 = new JLabel();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel10 = new JPanel();
  private BorderLayout borderLayout14 = new BorderLayout();
  private BorderLayout borderLayout13 = new BorderLayout();
  private BorderLayout borderLayout10 = new BorderLayout();
  private JLabel jLabel4 = new JLabel();
  private JPanel jPanel13 = new JPanel();
  private JPanel jPanel8 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private JTextArea jTextArea6 = new JTextArea();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

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
    diagnosticMetaphorIcon = new ImageIcon(getClass().getResource("/icones/diagnosticMetaphor2.gif"));
    openMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icones/openSMetaphor.gif"));
    saveMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icones/saveSMetaphor.gif"));
    diagnosticMetaphorRollOverIcon = new ImageIcon(getClass().getResource("/icones/diagnosticSMetaphor2.gif"));
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
    jPanel1.setLayout(gridBagLayout1);
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
    jTextArea5.setBorder(BorderFactory.createLoweredBevelBorder());
    jTextArea5.setEditable(false);
    jPanel9.setLayout(borderLayout9);
    jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel3.setText("Características da Variável Atual");
    jPanel11.setLayout(borderLayout11);
    jPanel10.setLayout(borderLayout12);
    jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel4.setText("Evidências");
    jPanel13.setLayout(borderLayout13);
    jPanel8.setLayout(borderLayout10);
    jPanel12.setLayout(borderLayout14);
    jTextArea6.setBorder(BorderFactory.createLoweredBevelBorder());
    jTextArea6.setEditable(false);
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
    jPanel1.add(jPanel3,   new GridBagConstraints(0, 0, 3, 4, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(jPanel9,   new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jPanel9.add(jPanel10, BorderLayout.CENTER);
    jPanel10.add(jTextArea5, BorderLayout.CENTER);
    jPanel9.add(jPanel11, BorderLayout.NORTH);
    jPanel11.add(jLabel3, BorderLayout.CENTER);
    jPanel1.add(jPanel8,    new GridBagConstraints(3, 1, 1, 3, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jPanel8.add(jPanel12, BorderLayout.CENTER);
    jPanel12.add(jTextArea6, BorderLayout.CENTER);
    jPanel8.add(jPanel13, BorderLayout.NORTH);
    jPanel13.add(jLabel4, BorderLayout.CENTER);
  }

  void diagnosticButton_actionPerformed(ActionEvent e)
  {   jTabbedPane1.setSelectedIndex(1);
      metaphorTree.propagate();
      NodeList explanationNodes = net.getExplanationNodes();
      int size = explanationNodes.size();
      descriptionTextArea.setText("");
      for (int i=0;i<size;i++)
      {   ProbabilisticNode node = (ProbabilisticNode)explanationNodes.get(i);
          descriptionTextArea.append(node.getDescription()+"\n");
          int statesSize = node.getStatesSize();
          for (int j=0;j<statesSize;j++)
          {   descriptionTextArea.append("\t"+node.getMarginalAt(j)+"\n");
          }
      }
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
          //metaphorTree = new MetaphorTree(net,false);
          metaphorTree = new MetaphorTree();
          metaphorTree.setProbabilisticNetwork(net);
          metaphorTree.expandTree();
          JScrollPane jScrollPane3 = new JScrollPane(metaphorTree);
          jPanel3.removeAll();
          jPanel3.add(jScrollPane3, BorderLayout.CENTER);
          jPanel3.updateUI();
          jTabbedPane1.setSelectedIndex(0);
          statusBar.setText("File opened successfully");
      }
      catch (Exception e)
      {   net = null;
          System.err.print(e.getMessage());
          statusBar.setText(e.getMessage());
      }
  }

}