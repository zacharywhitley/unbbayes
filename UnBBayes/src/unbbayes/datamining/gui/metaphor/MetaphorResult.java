package unbbayes.datamining.gui.metaphor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.NodeList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Mário Henrique Paes Vieira
 * @version 1.0
 */

public class MetaphorResult extends JPanel
{
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  private JPanel jPanel1 = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private NodeList explanationNodes;
  private JScrollPane jScrollPane3 = new JScrollPane();
  private JTextArea jTextArea3 = new JTextArea();
  private JScrollPane jScrollPane4 = new JScrollPane();
  private JTextArea jTextArea4 = new JTextArea();
  private JList jList1 = new JList();
  private JList jList2 = new JList();
  private DefaultListModel listModel1 = new DefaultListModel();
  private DefaultListModel listModel2 = new DefaultListModel();
  GridLayout gridLayout2 = new GridLayout();

  public MetaphorResult()
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
    this.setLayout(gridLayout2);
    jPanel1.setLayout(gridLayout1);
    jPanel2.setLayout(borderLayout2);
    gridLayout1.setRows(2);
    jPanel3.setLayout(borderLayout1);
    jPanel4.setLayout(borderLayout3);
    jLabel1.setText("Prováveis Diagnósticos:");
    jLabel2.setText("Diagnósticos Não Prováveis:");
    jTextArea3.setBackground(new Color(255, 255, 210));
    jTextArea3.setEditable(false);
    jTextArea4.setBackground(new Color(255, 255, 210));
    jTextArea4.setEditable(false);
    jList1.setModel(listModel1);
    jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        jList_valueChanged(e);
      }
    });
    jList2.setModel(listModel2);
    jList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        jList_valueChanged(e);
      }
    });
    gridLayout2.setColumns(2);
    this.add(jPanel1, null);
    jPanel1.add(jPanel3, null);
    jPanel3.add(jLabel1,  BorderLayout.NORTH);
    jPanel3.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jList1, null);
    jPanel1.add(jPanel4, null);
    jPanel4.add(jLabel2,  BorderLayout.NORTH);
    jPanel4.add(jScrollPane2, BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jList2, null);
    this.add(jPanel2, null);
    jPanel2.add(jTabbedPane1,  BorderLayout.CENTER);
    jTabbedPane1.add(jScrollPane3,    "Descrição do Diagnóstico:");
    jTabbedPane1.add(jScrollPane4,  "Laudo");
    jScrollPane4.getViewport().add(jTextArea4, null);
    jScrollPane3.getViewport().add(jTextArea3, null);
  }

  public void setExplanationNodes(NodeList explanationNodes)
  {   this.explanationNodes = explanationNodes;
      jTextArea3.setText("");
  }

  public void updateResults()
  {   jTextArea3.setText("");
      jTextArea4.setText("");
      flag=false;
      listModel1.clear();
      listModel2.clear();
      flag=true;
      if (explanationNodes!=null)
      {   int size = explanationNodes.size();
          for (int i=0;i<size;i++)
          {   ProbabilisticNode node = (ProbabilisticNode)explanationNodes.get(i);
              int statesSize = node.getStatesSize();
              if (statesSize>1)
              {   listModel1.addElement(new ListObject(node.getDescription(),node.getExplanationDescription()));
                  listModel2.addElement(new ListObject(node.getDescription(),node.getExplanationDescription()));
                  for (int j=0;j<statesSize;j++)
                  {   double marginal = node.getMarginalAt(j);
                      if (marginal >= 0.5)
                      {   listModel1.addElement(new ListObject("        "+node.getStateAt(j)+"        "+Utils.doubleToString((node.getMarginalAt(j)*100),2)+"%",node,ListObject.FINDINGS));
                      }
                      else
                      {   listModel2.addElement(new ListObject("        "+node.getStateAt(j)+"        "+Utils.doubleToString((node.getMarginalAt(j)*100),2)+"%",node,ListObject.WHY_NOT));
                      }
                  }
              }
          }
      }
  }

  private class ListObject
  {   private String description;
      private String name;
      private String diagnostic;
      public static final boolean FINDINGS = true;
      public static final boolean WHY_NOT = false;

      public ListObject(String name,String description)
      {   this.name = name;
          this.description = description;
          this.diagnostic = null;
      }

      public ListObject(String name,ProbabilisticNode node,boolean flag)
      {   this.name = name;
          this.description = node.getExplanationDescription();
          ArrayList keys = node.getPhrasesMap().getKeys();
          int size = keys.size();
          ExplanationPhrase[] phrases = new ExplanationPhrase[size];
          for (int i=0;i<size;i++)
          {   phrases[i]=(ExplanationPhrase)node.getPhrasesMap().get(keys.get(i));
          }
          if (flag == FINDINGS)
          {   calculatePositives(phrases);
          }
          else
          {   calculateNegatives(phrases);
          }
      }

      private void calculatePositives(ExplanationPhrase[] phrases)
      {   int size = phrases.length;
          ExplanationPhrase explanationPhrase;
          ArrayList<ExplanationPhrase> trigger = new ArrayList<ExplanationPhrase>();
          ArrayList<ExplanationPhrase> complementary = new ArrayList<ExplanationPhrase>();
          ArrayList<ExplanationPhrase> necessary = new ArrayList<ExplanationPhrase>();
          for (int i=0;i<size;i++)
          {   explanationPhrase = phrases[i];
              switch(explanationPhrase.getEvidenceType())
              {   case ExplanationPhrase.TRIGGER_EVIDENCE_TYPE:         trigger.add(explanationPhrase);
                                                                        break;
                  case ExplanationPhrase.NECESSARY_EVIDENCE_TYPE:       necessary.add(explanationPhrase);
                                                                        break;
                  case ExplanationPhrase.COMPLEMENTARY_EVIDENCE_TYPE:   complementary.add(explanationPhrase);
                                                                        break;
              }
          }
          StringBuffer sb = new StringBuffer();
          sb.append("(Frase Trigger)\n\n");
          addPhrases(sb,trigger);
          sb.append("(Frase Essencial)\n\n");
          addPhrases(sb,necessary);
          sb.append("(Frase Complementar)\n\n");
          addPhrases(sb,complementary);
          diagnostic=sb.toString();
      }

      private void addPhrases(StringBuffer sb,ArrayList evidence)
      {   ExplanationPhrase explanationPhrase;
          int size = evidence.size();
          if (size>0)
          {   for (int i=0;i<size;i++)
              {   explanationPhrase = (ExplanationPhrase)evidence.get(i);
                  sb.append(explanationPhrase.getNode()+"\n");
                  sb.append(explanationPhrase.getPhrase()+"\n\n");
              }
          }
      }

      private void calculateNegatives(ExplanationPhrase[] phrases)
      {   int size = phrases.length;
          ExplanationPhrase explanationPhrase;
          ArrayList<ExplanationPhrase> exclusive = new ArrayList<ExplanationPhrase>();
          for (int i=0;i<size;i++)
          {   explanationPhrase = phrases[i];
              if (explanationPhrase.getEvidenceType()==ExplanationPhrase.EXCLUSIVE_EVIDENCE_TYPE)
              {   exclusive.add(explanationPhrase);
              }
          }
          StringBuffer sb = new StringBuffer();
          sb.append("(Frase Excludente)\n\n");
          addPhrases(sb,exclusive);
          diagnostic=sb.toString();
      }

      public String toString()
      {   return name;
      }
  }

  void jList_valueChanged(ListSelectionEvent e)
  {   if (flag)
      {   JList source = (JList)e.getSource();
          ListObject list = (ListObject)source.getSelectedValue();

          if (source.equals(jList1))
          {   jTabbedPane1.setTitleAt(1,"Laudo");
          }
          else
          {   jTabbedPane1.setTitleAt(1,"Por que não?");
          }
          if (list.diagnostic!=null)
          {   jTabbedPane1.setSelectedIndex(1);
              jTextArea3.setText(list.description);
              jTextArea4.setText(list.diagnostic);
          }
          else
          {   jTabbedPane1.setSelectedIndex(0);
              jTextArea3.setText(list.description);
              jTextArea4.setText("");
          }
      }
      else
      {}
  }

  private boolean flag = true;
}