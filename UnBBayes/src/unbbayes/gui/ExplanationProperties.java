/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.*;

import unbbayes.prs.bn.*;
import unbbayes.util.*;

/**
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.0
 */
public class ExplanationProperties extends JDialog
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane jTabbedPane = new JTabbedPane();
  private JPanel southPanel = new JPanel();
  private JPanel descriptionPanel = new JPanel();
  private JPanel explanationPanel = new JPanel();
  private JButton okButton = new JButton();
  private Border border1;
  private BorderLayout borderLayout2 = new BorderLayout();
  private Border border2;
  private GridLayout gridLayout2 = new GridLayout();
  private JPanel evidencePhrasePanel = new JPanel();
  private JPanel explanationTopPanel = new JPanel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private BorderLayout borderLayout10 = new BorderLayout();
  private JPanel evidenceTypePanel = new JPanel();
  private JPanel evidenceNodePanel = new JPanel();
  private JPanel evidencePhraseBottomPanel = new JPanel();
  private JLabel evidenceNodeLabel = new JLabel();
  private JComboBox evidenceNodeComboBox = new JComboBox();
  private BorderLayout borderLayout11 = new BorderLayout();
  private Border border3;
  private TitledBorder titledBorder1;
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel notUsedPanel = new JPanel();
  private JPanel exclusivePanel = new JPanel();
  private JPanel necessaryPanel = new JPanel();
  private JPanel naPanel = new JPanel();
  private JPanel complementaryPanel = new JPanel();
  private JPanel triggerPanel = new JPanel();
  private JRadioButton triggerRadioButton = new JRadioButton();
  private JRadioButton complementaryRadioButton = new JRadioButton();
  private JRadioButton naRadioButton = new JRadioButton();
  private JRadioButton necessaryRadioButton = new JRadioButton();
  private JRadioButton exclusiveRadioButton = new JRadioButton();
  private BorderLayout borderLayout12 = new BorderLayout();
  private BorderLayout borderLayout13 = new BorderLayout();
  private BorderLayout borderLayout14 = new BorderLayout();
  private BorderLayout borderLayout15 = new BorderLayout();
  private BorderLayout borderLayout16 = new BorderLayout();
  private BorderLayout borderLayout17 = new BorderLayout();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private Border evidenceTypeBorder;
  private JPanel evidencePhraseTopPanel = new JPanel();
  private JLabel evidencePhraseLabel = new JLabel();
  private BorderLayout borderLayout18 = new BorderLayout();
  private JScrollPane evidencePhraseScrollPane = new JScrollPane();
  private BorderLayout borderLayout19 = new BorderLayout();
  private JTextArea evidencePhraseTextArea = new JTextArea();
  private NetWindow netWindow;
  private ProbabilisticNode node;
  private ProbabilisticNetwork net;
  private JButton cancelButton = new JButton();
  private JTextArea explanationNodeTextArea = new JTextArea();
  private JLabel nodeNameLabel = new JLabel();
  private JScrollPane explanationNodeScrollPane = new JScrollPane();
  private BorderLayout borderLayout7 = new BorderLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private JPanel descriptionTopPanel1 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JPanel descriptionTopPanel2 = new JPanel();
  private JPanel descriptionTopPanel3 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel descriptionTopPanel = new JPanel();
  private JPanel descriptionBottomPanel = new JPanel();
  private JLabel explanationVariableLabel = new JLabel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JLabel descriptionLabel = new JLabel();
  private GridLayout gridLayout4 = new GridLayout();

  public ExplanationProperties(NetWindow netWindow,ProbabilisticNetwork net)
  { this.netWindow = netWindow;
    this.net = net;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  { this.setSize(550, 470);
    border1 = BorderFactory.createEmptyBorder(0,10,0,10);
    border2 = BorderFactory.createEmptyBorder(20,20,20,20);
    border3 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Tipo de Evidência:");
    evidenceTypeBorder = BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Tipo de Evidência:"),BorderFactory.createEmptyBorder(0,10,0,0));
    this.setResizable(false);
    this.setTitle("Propriedades da Variável de Explicação");
    okButton.setMaximumSize(new Dimension(85, 27));
    okButton.setMinimumSize(new Dimension(85, 27));
    okButton.setPreferredSize(new Dimension(85, 27));
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        okButton_actionPerformed(e);
      }
    });
    jTabbedPane.setBorder(border1);
    descriptionPanel.setLayout(borderLayout2);
    explanationPanel.setLayout(gridLayout4);
    gridLayout2.setRows(2);
    gridLayout2.setVgap(10);
    explanationTopPanel.setLayout(borderLayout9);
    evidencePhrasePanel.setLayout(borderLayout10);
    evidenceNodeLabel.setText("Evidencia : ");
    evidenceNodePanel.setLayout(borderLayout11);
    evidenceTypePanel.setBorder(evidenceTypeBorder);
    evidenceTypePanel.setLayout(gridLayout3);
    gridLayout3.setColumns(3);
    gridLayout3.setHgap(10);
    gridLayout3.setRows(2);
    triggerRadioButton.setText("Trigger");
    triggerRadioButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        evidenceRadioButton_actionPerformed(e);
      }
    });
    complementaryRadioButton.setText("Complementar");
    complementaryRadioButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        evidenceRadioButton_actionPerformed(e);
      }
    });
    naRadioButton.setText("N/A");
    naRadioButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        evidenceRadioButton_actionPerformed(e);
      }
    });
    necessaryRadioButton.setText("Essencial");
    necessaryRadioButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        evidenceRadioButton_actionPerformed(e);
      }
    });
    exclusiveRadioButton.setText("Excludente");
    exclusiveRadioButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        evidenceRadioButton_actionPerformed(e);
      }
    });
    triggerPanel.setLayout(borderLayout12);
    complementaryPanel.setLayout(borderLayout13);
    naPanel.setLayout(borderLayout14);
    necessaryPanel.setLayout(borderLayout15);
    exclusivePanel.setLayout(borderLayout16);
    notUsedPanel.setLayout(borderLayout17);
    evidencePhraseLabel.setText("Texto para Explanação :");
    evidencePhraseTopPanel.setLayout(borderLayout18);
    evidencePhraseBottomPanel.setLayout(borderLayout19);
    borderLayout10.setVgap(5);
    borderLayout9.setVgap(10);
    cancelButton.setText("Cancelar");
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelButton_actionPerformed(e);
      }
    });
    evidenceNodeComboBox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        evidenceNodeComboBox_actionPerformed(e);
      }
    });
    evidencePhraseTextArea.addFocusListener(new java.awt.event.FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        evidencePhraseTextArea_focusLost(e);
      }
    });
    borderLayout2.setVgap(5);
    descriptionTopPanel2.setLayout(borderLayout7);
    descriptionTopPanel3.setLayout(borderLayout6);
    gridLayout1.setRows(3);
    descriptionTopPanel.setLayout(gridLayout1);
    descriptionBottomPanel.setLayout(borderLayout5);
    explanationVariableLabel.setText("Variável de explicação : ");
    descriptionPanel.setBorder(border2);
    descriptionTopPanel1.setLayout(borderLayout3);
    descriptionLabel.setText("Descrição:");
    gridLayout4.setRows(2);
    gridLayout4.setVgap(10);
    explanationPanel.setBorder(border2);
    this.getContentPane().add(jTabbedPane,  BorderLayout.CENTER);
    this.getContentPane().add(southPanel,  BorderLayout.SOUTH);
    jTabbedPane.add(descriptionPanel, "Descrição");
    descriptionTopPanel.add(descriptionTopPanel1, null);
    descriptionTopPanel1.add(descriptionLabel,  BorderLayout.CENTER);
    descriptionTopPanel.add(descriptionTopPanel2, null);
    descriptionTopPanel.add(descriptionTopPanel3, null);
    descriptionTopPanel3.add(explanationVariableLabel, BorderLayout.WEST);
    descriptionTopPanel3.add(nodeNameLabel, BorderLayout.CENTER);
    descriptionPanel.add(descriptionBottomPanel,  BorderLayout.CENTER);
    descriptionBottomPanel.add(explanationNodeScrollPane, BorderLayout.CENTER);
    explanationNodeScrollPane.add(explanationNodeTextArea, null);
    descriptionPanel.add(descriptionTopPanel, BorderLayout.NORTH);
    jTabbedPane.add(explanationPanel, "Explanação");
    southPanel.add(okButton, null);
    southPanel.add(cancelButton, null);
    explanationPanel.add(explanationTopPanel, null);
    explanationPanel.add(evidencePhrasePanel, null);
    explanationTopPanel.add(evidenceTypePanel, BorderLayout.CENTER);
    evidenceTypePanel.add(triggerPanel, null);
    triggerPanel.add(triggerRadioButton, BorderLayout.CENTER);
    evidenceTypePanel.add(complementaryPanel, null);
    complementaryPanel.add(complementaryRadioButton, BorderLayout.CENTER);
    evidenceTypePanel.add(naPanel, null);
    naPanel.add(naRadioButton, BorderLayout.CENTER);
    evidenceTypePanel.add(necessaryPanel, null);
    necessaryPanel.add(necessaryRadioButton, BorderLayout.CENTER);
    evidenceTypePanel.add(exclusivePanel, null);
    exclusivePanel.add(exclusiveRadioButton, BorderLayout.CENTER);
    evidenceTypePanel.add(notUsedPanel, null);
    explanationTopPanel.add(evidenceNodePanel, BorderLayout.NORTH);
    evidenceNodePanel.add(evidenceNodeLabel, BorderLayout.WEST);
    evidenceNodePanel.add(evidenceNodeComboBox, BorderLayout.CENTER);
    evidencePhrasePanel.add(evidencePhraseBottomPanel, BorderLayout.CENTER);
    evidencePhraseBottomPanel.add(evidencePhraseScrollPane,  BorderLayout.CENTER);
    evidencePhraseScrollPane.getViewport().add(evidencePhraseTextArea, null);
    evidencePhrasePanel.add(evidencePhraseTopPanel, BorderLayout.NORTH);
    evidencePhraseTopPanel.add(evidencePhraseLabel, BorderLayout.CENTER);
    buttonGroup1.add(triggerRadioButton);
    buttonGroup1.add(complementaryRadioButton);
    buttonGroup1.add(naRadioButton);
    buttonGroup1.add(necessaryRadioButton);
    buttonGroup1.add(exclusiveRadioButton);
    NodeList nodes = net.getDescriptionNodes();
    int size = nodes.size();
    int i;
    String[] stringNodes = new String[size];
    for (i=0; i<size; i++)
    {   stringNodes[i] = nodes.get(i).getDescription();
    }
    Arrays.sort(stringNodes);
    for (i=0; i<size; i++)
    {   evidenceNodeComboBox.addItem(stringNodes[i]);
    }
  }

  public void setProbabilisticNode(ProbabilisticNode node)
  {   this.node = node;
      explanationNodeTextArea.setText(node.getExplanationDescription());
      if (evidenceNodeComboBox.getItemCount() != 0)
        updateExplanationInformation(evidenceNodeComboBox.getItemAt(0).toString());
      nodeNameLabel.setText(node.getName());
  }

  void okButton_actionPerformed(ActionEvent e)
  {   node.setExplanationDescription(explanationNodeTextArea.getText());
      dispose();
  }

  void cancelButton_actionPerformed(ActionEvent e)
  {   dispose();
  }

  void evidenceNodeComboBox_actionPerformed(ActionEvent evt)
  {   JComboBox source = (JComboBox)evt.getSource();
      String item = (String)source.getSelectedItem();
      updateExplanationInformation(item);
  }

  private void updateExplanationInformation(String item)
  {   try
      {   ExplanationPhrase explanationPhrase = node.getExplanationPhrase(item);
          int evidenceType = explanationPhrase.getEvidenceType();
          switch (evidenceType)
          {   case (ExplanationPhrase.TRIGGER_EVIDENCE_TYPE) :        triggerRadioButton.setSelected(true);
                                                                      break;
              case (ExplanationPhrase.NECESSARY_EVIDENCE_TYPE) :      necessaryRadioButton.setSelected(true);
                                                                      break;
              case (ExplanationPhrase.COMPLEMENTARY_EVIDENCE_TYPE) :  complementaryRadioButton.setSelected(true);
                                                                      break;
              case (ExplanationPhrase.EXCLUSIVE_EVIDENCE_TYPE) :      exclusiveRadioButton.setSelected(true);
                                                                      break;
              default : naRadioButton.setSelected(true);
          }
          evidencePhraseTextArea.setText(explanationPhrase.getPhrase());
      }
      catch (Exception e)
      {   naRadioButton.setSelected(true);
          evidencePhraseTextArea.setText("");
      }
  }

  void evidenceRadioButton_actionPerformed(ActionEvent e)
  {   addEvidence(e);
  }

  void evidencePhraseTextArea_focusLost(FocusEvent e)
  {   addEvidence(e);
  }

  private void addEvidence(AWTEvent e)
  {   ExplanationPhrase explanationPhrase = new ExplanationPhrase();
      explanationPhrase.setNode(evidenceNodeComboBox.getSelectedItem().toString());
      if (triggerRadioButton.isSelected())
      {   explanationPhrase.setEvidenceType(ExplanationPhrase.TRIGGER_EVIDENCE_TYPE);
      }
      else if (necessaryRadioButton.isSelected())
      {   explanationPhrase.setEvidenceType(ExplanationPhrase.NECESSARY_EVIDENCE_TYPE);
      }
      else if (complementaryRadioButton.isSelected())
      {   explanationPhrase.setEvidenceType(ExplanationPhrase.COMPLEMENTARY_EVIDENCE_TYPE);
      }
      else if (exclusiveRadioButton.isSelected())
      {   explanationPhrase.setEvidenceType(ExplanationPhrase.EXCLUSIVE_EVIDENCE_TYPE);
      }
      else
      {   explanationPhrase.setEvidenceType(ExplanationPhrase.NA_EVIDENCE_TYPE);
      }
      explanationPhrase.setPhrase(evidencePhraseTextArea.getText());
      node.addExplanationPhrase(explanationPhrase);
  }
}