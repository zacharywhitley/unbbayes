package unbbayes.datamining.gui.evaluation;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import unbbayes.datamining.classifiers.*;

public class EvaluationOptions extends JDialog
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel5 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private JRadioButton jRadioButton1 = new JRadioButton();
  private JRadioButton jRadioButton2 = new JRadioButton();
  private JRadioButton jRadioButton3 = new JRadioButton();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JTable jTable1 = new JTable();
  private BayesianNetwork classifier;

  public EvaluationOptions(Classifier classifier)
  { if (classifier instanceof BayesianNetwork)
        this.classifier = (BayesianNetwork)classifier;
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
  {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(borderLayout2);
    jButton1.setText("Cancel");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jButton2.setText("OK");
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setColumns(1);
    gridLayout1.setRows(2);
    jPanel4.setLayout(gridLayout2);
    gridLayout2.setColumns(1);
    gridLayout2.setRows(3);
    jRadioButton1.setSelected(true);
    jRadioButton1.setText("Normal Classification");
    jRadioButton2.setText("Absolute Frequency Classification");
    jRadioButton3.setText("Relative Frequency Classification");
    jPanel5.setLayout(borderLayout3);
    this.getContentPane().add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(jPanel2, BorderLayout.CENTER);
    jPanel2.add(jPanel4, null);
    jPanel4.add(jRadioButton1, null);
    jPanel4.add(jRadioButton3, null);
    jPanel4.add(jRadioButton2, null);
    jPanel2.add(jPanel5, null);
    jPanel5.add(jScrollPane1,  BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTable1, null);
    jPanel1.add(jPanel3,  BorderLayout.SOUTH);
    jPanel3.add(jButton2, null);
    jPanel3.add(jButton1, null);
    Object [] colNames = {"Priority","Class Value","Probability"};
    Object [][] data = new Object [3][3];
    jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jTable1.getTableHeader().setReorderingAllowed(false);
    jTable1.getTableHeader().setResizingAllowed(false);
    jTable1.setColumnSelectionAllowed(false);
    jTable1.setRowSelectionAllowed(false);
    jTable1.setModel(new ValuesTableModel(data, colNames));
    buttonGroup1.add(jRadioButton1);
    buttonGroup1.add(jRadioButton3);
    buttonGroup1.add(jRadioButton2);
  }

  private class ValuesTableModel extends DefaultTableModel
  {   public boolean isCellEditable(int row, int col)
      {   return false;
      }

      public ValuesTableModel()
      {   super();
      }

      public ValuesTableModel(Object[][] data,Object[] colNames)
      {   super(data,colNames);
      }
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   dispose();
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   dispose();
  }
}