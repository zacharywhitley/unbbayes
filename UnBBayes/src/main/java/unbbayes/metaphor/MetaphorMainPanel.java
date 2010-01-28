/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.metaphor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileHistoryController;
import unbbayes.controller.IconController;
import unbbayes.gui.FileIcon;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.1 (19/11/06)
 */

public class MetaphorMainPanel extends JPanel
{
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		

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
  private JPanel jPanel3 = new JPanel();
  private MetaphorTree metaphorTree = new MetaphorTree();
  private BorderLayout borderLayout7 = new BorderLayout();
  private ImageIcon openMetaphorIcon;
  private ImageIcon saveMetaphorIcon;
  private ImageIcon diagnosticMetaphorIcon;
  private ImageIcon openMetaphorRollOverIcon;
  private ImageIcon saveMetaphorRollOverIcon;
  private ImageIcon diagnosticMetaphorRollOverIcon;
  private ProbabilisticNetwork net = null;
  private JFileChooser fileChooser;
  private TitledBorder titledBorder1;
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
  private MetaphorResult metaphorResult = new MetaphorResult();

  public MetaphorMainPanel()
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
  {
    IconController iconController = IconController.getInstance();
    openMetaphorIcon = iconController.getOpenMetaphorIcon();
    saveMetaphorIcon = iconController.getSaveMetaphorIcon();
    diagnosticMetaphorIcon = iconController.getDiagnosticMetaphorIcon();
    openMetaphorRollOverIcon = iconController.getOpenMetaphorRollOverIcon();
    saveMetaphorRollOverIcon = iconController.getSaveMetaphorRollOverIcon();
    diagnosticMetaphorRollOverIcon = iconController.getDiagnosticMetaphorRollOverIcon();
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
    jTabbedPane1.add(metaphorResult,"Laudo");
    jPanel1.add(jPanel3,       new GridBagConstraints(0, 0, 3, 4, 30.0, 30.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(jPanel9,   new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jPanel9.add(jPanel10, BorderLayout.CENTER);
    jPanel10.add(jTextArea5, BorderLayout.CENTER);
    jPanel9.add(jPanel11, BorderLayout.NORTH);
    jPanel11.add(jLabel3, BorderLayout.CENTER);
    jPanel1.add(jPanel8,      new GridBagConstraints(3, 1, 1, 3, 2.0, 2.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    jPanel8.add(jPanel12, BorderLayout.CENTER);
    jPanel12.add(jTextArea6, BorderLayout.CENTER);
    jPanel8.add(jPanel13, BorderLayout.NORTH);
    jPanel13.add(jLabel4, BorderLayout.CENTER);
    metaphorResult.setExplanationNodes(null);
  }

  void diagnosticButton_actionPerformed(ActionEvent e)
  {   jTabbedPane1.setSelectedIndex(1);
      metaphorTree.propagate();
      metaphorResult.updateResults();
  }

  void openButton_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"net", "xml"};
      fileChooser = new JFileChooser(FileHistoryController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar Icones de arquivos
      fileChooser.setFileView(new FileIcon(this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "Networks (*.net) or XML-BIF(*.xml)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          openFile(selectedFile);
          FileHistoryController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void openFile(File selectedFile)
  {   try
      {
	    String name = selectedFile.getName().toLowerCase();				
		
		  if (name.endsWith("net")) {
			 net = (ProbabilisticNetwork)new NetIO().load(selectedFile);					
		  } else if (name.endsWith("xml")){
		 	net = new XMLBIFIO().load(selectedFile);				
		  }
		
          net.compile();
          metaphorResult.setExplanationNodes(net.getExplanationNodes());
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