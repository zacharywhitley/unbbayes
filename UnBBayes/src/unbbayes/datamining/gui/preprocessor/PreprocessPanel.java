package unbbayes.datamining.gui.preprocessor;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import unbbayes.datamining.datamanipulation.*;

public class PreprocessPanel extends JPanel
{ private JPanel jPanel7 = new JPanel();
  private JPanel jPanel6 = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel1 = new JPanel();
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel1 = new JLabel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private BorderLayout borderLayout8 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout13 = new BorderLayout();
  private BorderLayout borderLayout12 = new BorderLayout();
  private BorderLayout borderLayout11 = new BorderLayout();
  private BorderLayout borderLayout10 = new BorderLayout();
  private JPanel jPanel23 = new JPanel();
  private JPanel jPanel22 = new JPanel();
  private JPanel jPanel21 = new JPanel();
  private JPanel jPanel20 = new JPanel();
  private JPanel jPanel19 = new JPanel();
  private JPanel jPanel18 = new JPanel();
  private JPanel jPanel13 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel10 = new JPanel();
  private GridLayout gridLayout3 = new GridLayout();
  private GridLayout gridLayout2 = new GridLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout32 = new BorderLayout();
  private BorderLayout borderLayout31 = new BorderLayout();
  private BorderLayout borderLayout30 = new BorderLayout();
  private JPanel jPanel40 = new JPanel();
  private JPanel jPanel9 = new JPanel();
  private BorderLayout borderLayout29 = new BorderLayout();
  private BorderLayout borderLayout28 = new BorderLayout();
  private BorderLayout borderLayout27 = new BorderLayout();
  private BorderLayout borderLayout26 = new BorderLayout();
  private JPanel jPanel5 = new JPanel();
  private BorderLayout borderLayout25 = new BorderLayout();
  private BorderLayout borderLayout24 = new BorderLayout();
  private BorderLayout borderLayout23 = new BorderLayout();
  private BorderLayout borderLayout22 = new BorderLayout();
  private JPanel jPanel39 = new JPanel();
  private BorderLayout borderLayout21 = new BorderLayout();
  private JPanel jPanel38 = new JPanel();
  private JPanel jPanel37 = new JPanel();
  private JLabel jLabel9 = new JLabel();
  private JPanel jPanel36 = new JPanel();
  private JPanel jPanel35 = new JPanel();
  private JPanel jPanel34 = new JPanel();
  private JPanel jPanel33 = new JPanel();
  private JPanel jPanel32 = new JPanel();
  private JPanel jPanel31 = new JPanel();
  private JPanel jPanel30 = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout17 = new BorderLayout();
  private JPanel jPanel29 = new JPanel();
  private JPanel jPanel28 = new JPanel();
  private JPanel jPanel27 = new JPanel();
  private JLabel jLabel16 = new JLabel();
  private JLabel jLabel15 = new JLabel();
  private JLabel jLabel14 = new JLabel();
  private JLabel jLabel13 = new JLabel();
  private JLabel jLabel12 = new JLabel();
  private JLabel jLabel11 = new JLabel();
  private JLabel jLabel10 = new JLabel();
  private GridLayout gridLayout5 = new GridLayout();
  private ButtonGroup buttonGroup2 = new ButtonGroup();
  private TitledBorder titledBorder4;
  private TitledBorder titledBorder3;
  private TitledBorder titledBorder2;
  private TitledBorder titledBorder1;
  private Border border1;
  private InstanceSet instances;
  private BorderLayout borderLayout34 = new BorderLayout();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private BorderLayout borderLayout35 = new BorderLayout();
  private JTable jTable1 = new JTable();
  private JPanel jPanel2 = new JPanel();
  private PreprocessorMain reference;
  private GridLayout gridLayout4 = new GridLayout();
  private JPanel jPanel14 = new JPanel();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private JPanel jPanel15 = new JPanel();
  private BorderLayout borderLayout14 = new BorderLayout();
  private AttributeSelectionPanel jPanel8 = new AttributeSelectionPanel();

  public PreprocessPanel(PreprocessorMain reference)
  { this.reference = reference;
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
    titledBorder4 = new TitledBorder(border1,"Continuous attributes");
    titledBorder3 = new TitledBorder(border1,"Attribute info for base relation");
    titledBorder2 = new TitledBorder(border1,"Attributes in file");
    titledBorder1 = new TitledBorder(border1,"File");
    border1 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    jPanel7.setLayout(gridLayout2);
    jPanel4.setLayout(borderLayout3);
    jPanel3.setLayout(gridLayout1);
    jPanel1.setLayout(borderLayout2);
    jLabel6.setText("Attributes: ");
    jLabel5.setText("Instances: ");
    jLabel4.setText("None");
    jLabel3.setText("None");
    jLabel2.setText("None");
    jLabel1.setText("Relation: ");
    jPanel23.setLayout(borderLayout13);
    jPanel22.setLayout(borderLayout12);
    jPanel21.setLayout(borderLayout11);
    jPanel20.setLayout(borderLayout10);
    jPanel19.setLayout(borderLayout9);
    jPanel18.setLayout(borderLayout8);
    jPanel13.setLayout(borderLayout7);
    jPanel12.setLayout(borderLayout6);
    jPanel11.setLayout(borderLayout5);
    jPanel10.setLayout(gridLayout3);
    gridLayout3.setColumns(2);
    gridLayout2.setRows(2);
    gridLayout2.setVgap(3);
    gridLayout1.setColumns(2);
    this.setLayout(borderLayout1);
    jPanel40.setLayout(borderLayout27);
    jPanel9.setLayout(borderLayout17);
    jPanel5.setLayout(borderLayout4);
    jPanel39.setLayout(borderLayout28);
    jPanel38.setLayout(borderLayout29);
    jPanel37.setLayout(borderLayout30);
    jLabel9.setText("Name: ");
    jPanel36.setLayout(borderLayout32);
    jPanel35.setLayout(borderLayout31);
    jPanel34.setLayout(borderLayout26);
    jPanel33.setLayout(borderLayout25);
    jPanel32.setLayout(borderLayout22);
    jPanel31.setLayout(borderLayout23);
    jPanel30.setLayout(borderLayout24);
    jPanel29.setLayout(borderLayout21);
    jPanel28.setLayout(gridLayout5);
    jLabel16.setText("Distinct: ");
    jLabel15.setText("None");
    jLabel14.setText("Missing: ");
    jLabel13.setText("None");
    jLabel12.setText("None");
    jLabel11.setText("Type: ");
    jLabel10.setText("None");
    gridLayout5.setRows(2);
    gridLayout5.setColumns(2);
    jPanel7.setBorder(titledBorder1);
    jPanel6.setBorder(titledBorder2);
    jPanel6.setLayout(borderLayout34);
    jPanel9.setBorder(titledBorder3);
    jPanel27.setLayout(borderLayout35);
    jPanel2.setLayout(gridLayout4);
    jButton1.setEnabled(false);
    jButton1.setText("Discretize attribute ...");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jPanel2.setBorder(titledBorder4);
    jButton2.setEnabled(false);
    jButton2.setText("Instances Editor from Selected Attributes");
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jPanel15.setLayout(borderLayout14);
    jScrollPane2.setBorder(null);
    jPanel8.setToolTipText("Only selected attributes will be used by Instances Editor");
    jPanel3.add(jPanel4, null);
    jPanel4.add(jPanel6, BorderLayout.CENTER);
    jPanel6.add(jPanel15,  BorderLayout.SOUTH);
    jPanel15.add(jButton2, BorderLayout.CENTER);
    jPanel6.add(jPanel8,  BorderLayout.CENTER);
    jPanel4.add(jPanel7, BorderLayout.NORTH);
    jPanel13.add(jPanel22, BorderLayout.CENTER);
    jPanel22.add(jLabel3, BorderLayout.CENTER);
    jPanel13.add(jPanel23, BorderLayout.WEST);
    jPanel23.add(jLabel6, BorderLayout.CENTER);
    jPanel10.add(jPanel12, null);
    jPanel10.add(jPanel13, null);
    jPanel12.add(jPanel20, BorderLayout.CENTER);
    jPanel20.add(jLabel2, BorderLayout.CENTER);
    jPanel12.add(jPanel21, BorderLayout.WEST);
    jPanel21.add(jLabel5, BorderLayout.CENTER);
    jPanel7.add(jPanel11, null);
    jPanel7.add(jPanel10, null);
    jPanel11.add(jPanel18, BorderLayout.WEST);
    jPanel18.add(jLabel1, BorderLayout.CENTER);
    jPanel11.add(jPanel19, BorderLayout.CENTER);
    jPanel19.add(jLabel4, BorderLayout.CENTER);
    jPanel3.add(jPanel5, null);
    jPanel9.add(jPanel27, BorderLayout.CENTER);
    jPanel27.add(jScrollPane2,  BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTable1, null);
    jPanel9.add(jPanel28, BorderLayout.NORTH);
    jPanel29.add(jPanel33, BorderLayout.WEST);
    jPanel33.add(jLabel9, BorderLayout.CENTER);
    jPanel29.add(jPanel34, BorderLayout.CENTER);
    jPanel34.add(jLabel10, BorderLayout.CENTER);
    jPanel28.add(jPanel32, null);
    jPanel28.add(jPanel29, null);
    jPanel32.add(jPanel35, BorderLayout.WEST);
    jPanel35.add(jLabel11, BorderLayout.CENTER);
    jPanel32.add(jPanel36, BorderLayout.CENTER);
    jPanel36.add(jLabel12, BorderLayout.CENTER);
    jPanel28.add(jPanel31, null);
    jPanel31.add(jPanel37, BorderLayout.CENTER);
    jPanel37.add(jLabel13, BorderLayout.CENTER);
    jPanel31.add(jPanel38, BorderLayout.WEST);
    jPanel38.add(jLabel14, BorderLayout.CENTER);
    jPanel28.add(jPanel30, null);
    jPanel30.add(jPanel39, BorderLayout.CENTER);
    jPanel39.add(jLabel15, BorderLayout.CENTER);
    jPanel30.add(jPanel40, BorderLayout.WEST);
    jPanel40.add(jLabel16, BorderLayout.CENTER);
    jPanel5.add(jPanel2, BorderLayout.NORTH);
    jPanel2.add(jPanel14, null);
    jPanel14.add(jButton1, null);
    jPanel5.add(jPanel9, BorderLayout.CENTER);
    this.add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jPanel3, BorderLayout.CENTER);
    jTable1.getTableHeader().setReorderingAllowed(false);
    jTable1.getTableHeader().setResizingAllowed(false);
    jPanel8.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {   public void valueChanged(ListSelectionEvent e)
        {   jPanel8_valueChanged(e);
	}
    });
  }

  public void setBaseInstances(InstanceSet inst)
  {   instances = inst;
      jLabel4.setText(inst.getRelationName());
      jLabel2.setText(inst.numWeightedInstances()+"");
      jLabel3.setText(inst.numAttributes()+"");
      jPanel8.setInstances(instances);
      jButton2.setEnabled(true);
  }

  /**
   * Creates a tablemodel for the attribute being displayed
   */
  protected void setTable(AttributeStats as, int index)
  {   if (as.getNominalCounts() != null)
      {   Attribute att = instances.getAttribute(index);
          Object [] colNames = {"Label", "Count"};
      	  Object [][] data = new Object [as.getNominalCounts().length][2];
      	  for (int i = 0; i < as.getNominalCounts().length; i++)
          {   data[i][0] = att.value(i);
              data[i][1] = new Integer(as.getNominalCounts()[i]);
      	  }
      	  jTable1.setModel(new DefaultTableModel(data, colNames));
      }
      else if (as.getNumericStats() != null)
      {   Object [] colNames = {"Statistic", "Value"};
      	  Object [][] data = new Object [4][2];
          Stats stats = as.getNumericStats();
      	  data[0][0] = "Minimum"; data[0][1] = new Float((float)stats.getMin());
      	  data[1][0] = "Maximum"; data[1][1] = new Float((float)stats.getMax());
      	  data[2][0] = "Mean";    data[2][1] = new Float((float)stats.getMean());
      	  data[3][0] = "StdDev";  data[3][1] = new Float((float)stats.getStdDev());
      	  jTable1.setModel(new DefaultTableModel(data, colNames));
      }
      else
      {   jTable1.setModel(new DefaultTableModel());
      }
  }

  void jPanel8_valueChanged(ListSelectionEvent e)
  {   if (!e.getValueIsAdjusting())
      {   ListSelectionModel lm = (ListSelectionModel) e.getSource();
          for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++)
          {   if (lm.isSelectedIndex(i))
              {   try
                  {   int selectedAttribute = i;
                      Attribute att = instances.getAttribute(selectedAttribute);
                      int type = att.getAttributeType();
                      if (type == att.NOMINAL)
                      {   jLabel12.setText("Nominal");
                          jButton1.setEnabled(false);
                      }
                      else
                      {   jLabel12.setText("Numeric");
                          jButton1.setEnabled(true);
                      }
                      jLabel10.setText(att.getAttributeName());
                      AttributeStats attStats = instances.getAttributeStats(selectedAttribute);
                      long percent = Math.round(100.0 * attStats.getMissingCount() / attStats.getTotalCount());
                      jLabel13.setText("" + attStats.getMissingCount() + " (" + percent + "%)");
                      jLabel15.setText("" + attStats.getDistinctCount());
                      setTable(attStats, selectedAttribute);
                  }
                  catch (NullPointerException npe)
                  {}
                  break;
              }
          }
      }
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   try
      {   if (jPanel8.getSelectedAttributes() != null)
          {   int[] selectedAttributes = jPanel8.getSelectedAttributes();
              if (selectedAttributes != null && selectedAttributes.length > 0)
              {   reference.getTabbedPane().setEnabledAt(1,true);
                  reference.getTabbedPane().setSelectedIndex(1);
                  StringBuffer sb = new StringBuffer();
                  int fileExtension = reference.getFileExtension();
                  int i;
                  int numInstances = instances.numInstances();
                  if (fileExtension == reference.ARFF_EXTENSION)
                  {   sb.append("@relation "+instances.getRelationName()+"\n\n");
                      for (i=0; i<selectedAttributes.length; i++)
                      {   sb.append(instances.getAttribute(selectedAttributes[i])+"\n");
                      }
                      boolean bool = Options.getInstance().getCompactedFile();
                      if (bool == true)
                      {   sb.append("@attribute counter real\n");
                          sb.append("\n"+"@data\n");
                          for(i=0; i<numInstances; i++)
                          {   Instance instance = instances.getInstance(i);
                              for (int j=0; j<selectedAttributes.length; j++)
                              {   sb.append(instance.stringValue(selectedAttributes[j])+",");
                              }
                              sb.append(instance.getWeight()+"\n");
                          }
                      }
                      else
                      {   sb.append("\n"+"@data\n");
                          for(i=0; i<numInstances; i++)
                          {   Instance instance = instances.getInstance(i);
                              for (int j=0; j<selectedAttributes.length; j++)
                              {   if (j == (selectedAttributes.length-1))
                                      sb.append(instance.stringValue(selectedAttributes[j])+"\n");
                                  else
                                      sb.append(instance.stringValue(selectedAttributes[j])+",");
                              }
                          }
                      }
                  }
                  else if (fileExtension == reference.TXT_EXTENSION)
                  {   for (i=0; i<selectedAttributes.length; i++)
                      {   sb.append(instances.getAttribute(i).getAttributeName()+" ");
                      }
                      boolean bool = Options.getInstance().getCompactedFile();
                      if (bool == true)
                      {   sb.append("counter\n");
                          for(i=0; i<numInstances; i++)
                          {   Instance instance = instances.getInstance(i);
                              for (int j=0; j<selectedAttributes.length; j++)
                              {   sb.append(instance.stringValue(selectedAttributes[j])+" ");
                              }
                              sb.append(instance.getWeight()+"\n");
                          }
                      }
                      else
                      {   sb.append("\n");
                          for(i=0; i<numInstances; i++)
                          {   Instance instance = instances.getInstance(i);
                              for (int j=0; j<selectedAttributes.length; j++)
                              {   if (j == (selectedAttributes.length-1))
                                      sb.append(instance.stringValue(selectedAttributes[j])+"\n");
                                  else
                                      sb.append(instance.stringValue(selectedAttributes[j])+" ");
                              }
                          }
                      }
                  }
                  reference.setEditorText(sb.toString());
              }
              else
              {   reference.setStatusBar("Nunhum atributo selecionado.");
              }
          }
          else
          {   reference.setStatusBar("Nunhum atributo selecionado.");
          }
      }
      catch (NullPointerException npe)
      {   reference.setStatusBar("Nunhum atributo selecionado.");
      }
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   DiscretizationPanel dlg = new DiscretizationPanel(reference);
      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = reference.getSize();
      Point loc = reference.getLocation();
      dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
      dlg.setModal(true);
      dlg.show();
  }
}