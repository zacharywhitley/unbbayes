package unbbayes.datamining.gui.preprocessor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.discretize.*;

public class DiscretizationPanel extends JDialog
{
  private JPanel jPanel1 = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel6 = new JPanel();
  private JComboBox jComboBox2 = new JComboBox();
  private JComboBox jComboBox1 = new JComboBox();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel5 = new JPanel();
  private JLabel jLabel2 = new JLabel();
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel7 = new JPanel();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private PreprocessorMain parent;
  private InstanceSet inst;
  private Attribute selectedAttribute;

  public DiscretizationPanel(PreprocessorMain parent)
  { this.parent = parent;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    pack();
  }
  private void jbInit() throws Exception
  {
    this.setTitle("Discretization");
    jPanel1.setLayout(borderLayout1);
    jPanel6.setLayout(gridLayout1);
    gridLayout1.setColumns(2);
    gridLayout1.setHgap(5);
    gridLayout1.setRows(2);
    gridLayout1.setVgap(5);
    jPanel5.setLayout(borderLayout2);
    jLabel2.setText("Number of States :");
    jPanel4.setLayout(borderLayout3);
    jPanel3.setLayout(borderLayout4);
    jLabel1.setText("Discretization Type :");
    jPanel2.setLayout(borderLayout5);
    jButton1.setMaximumSize(new Dimension(80, 27));
    jButton1.setMinimumSize(new Dimension(80, 27));
    jButton1.setPreferredSize(new Dimension(80, 27));
    jButton1.setMnemonic('C');
    jButton1.setText("Cancel");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jButton2.setMaximumSize(new Dimension(80, 27));
    jButton2.setMinimumSize(new Dimension(80, 27));
    jButton2.setPreferredSize(new Dimension(80, 27));
    jButton2.setMnemonic('O');
    jButton2.setText("OK");
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jComboBox2.setMaximumRowCount(5);
    this.getContentPane().add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(jPanel6,  BorderLayout.CENTER);
    jPanel6.add(jPanel2, null);
    jPanel2.add(jLabel1,  BorderLayout.CENTER);
    jPanel6.add(jPanel5, null);
    jPanel5.add(jComboBox1,  BorderLayout.CENTER);
    jPanel6.add(jPanel4, null);
    jPanel4.add(jLabel2,  BorderLayout.CENTER);
    jPanel6.add(jPanel3, null);
    jPanel3.add(jComboBox2,  BorderLayout.CENTER);
    jPanel1.add(jPanel7,  BorderLayout.SOUTH);
    jPanel7.add(jButton2, null);
    jPanel7.add(jButton1, null);
    inicializeComboBoxes();
  }

  private void inicializeComboBoxes()
  {   jComboBox1.addItem("Range");
      jComboBox1.addItem("Frequency");
      for (int i=0; i<100; i++)
      {   jComboBox2.addItem((i+1)+"");
      }
  }

  public void setBaseInstances(InstanceSet inst,Attribute selectedAttribute)
  {   this.inst = inst;
      this.selectedAttribute = selectedAttribute;
  }

  // OK button
  void jButton2_actionPerformed(ActionEvent e)
  {   if (jComboBox1.getSelectedIndex() == 0)
      {   RangeDiscretization range = new RangeDiscretization(inst);
          try
          {   range.discretizeAttribute(selectedAttribute,(jComboBox2.getSelectedIndex()+1));
              parent.updateInstances(range.getInstances());
              parent.setStatusBar("Range discretization successful");
          }
          catch (Exception ex)
          {   parent.setStatusBar(ex.getMessage());
          }
      }
      else if (jComboBox1.getSelectedIndex() == 1)
      {   FrequencyDiscretization freq = new FrequencyDiscretization(inst);
          try
          {   freq.discretizeAttribute(selectedAttribute,(jComboBox2.getSelectedIndex()+1));
              parent.updateInstances(freq.getInstances());
              parent.setStatusBar("Frequency discretization successful");
          }
          catch (Exception ex)
          {   parent.setStatusBar(ex.getMessage());
          }
      }
      dispose();
  }

  //Cancel button
  void jButton1_actionPerformed(ActionEvent e)
  {   dispose();
  }
}