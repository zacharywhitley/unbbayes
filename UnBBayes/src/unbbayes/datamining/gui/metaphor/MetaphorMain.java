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
  private BorderLayout borderLayout6 = new BorderLayout();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel5 = new JPanel();
  private MetaphorTree metaphorTree = new MetaphorTree();
  private BorderLayout borderLayout7 = new BorderLayout();
  private ImageIcon openMetaphorIcon;
  private ImageIcon saveMetaphorIcon;
  private ImageIcon diagnosticMetaphorIcon;
  private ProbabilisticNetwork net = new ProbabilisticNetwork();
  private JFileChooser fileChooser;

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
    descriptionTextArea.setEditable(false);
    jPanel4.setLayout(borderLayout6);
    jPanel3.setLayout(borderLayout7);
    openButton.setIcon(openMetaphorIcon);
    openButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        openButton_actionPerformed(e);
      }
    });
    saveButton.setIcon(saveMetaphorIcon);
    diagnosticButton.setIcon(diagnosticMetaphorIcon);
    diagnosticButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        diagnosticButton_actionPerformed(e);
      }
    });
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
    jPanel2.add(descriptionPanel, BorderLayout.CENTER);
    descriptionPanel.add(descriptionTabbedPane,  BorderLayout.CENTER);
    descriptionTabbedPane.add(descriptionScrollPane,   "Descrição do diagnóstico");
    descriptionScrollPane.getViewport().add(descriptionTextArea, null);
    jPanel1.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add(metaphorTree,  BorderLayout.CENTER);
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
          metaphorTree = new MetaphorTree(net);

          statusBar.setText("File opened successfully");
      }
      catch (Exception e)
      {   net = null;
          System.err.print(e.getMessage());
          statusBar.setText(e.getMessage());
      }
  }

}